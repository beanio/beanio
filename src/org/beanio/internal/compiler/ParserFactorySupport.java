/*
 * Copyright 2010-2012 Kevin Seim
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.beanio.internal.compiler;

import java.beans.*;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.PatternSyntaxException;

import org.beanio.BeanIOConfigurationException;
import org.beanio.internal.config.*;
import org.beanio.internal.parser.*;
import org.beanio.internal.parser.accessor.*;
import org.beanio.internal.parser.message.ResourceBundleMessageFactory;
import org.beanio.internal.util.*;
import org.beanio.stream.*;
import org.beanio.types.*;

/**
 * Base {@link ParserFactory} implementation.
 * 
 * <p>A {@link StreamConfig} is "compiled" into a {@link Stream} in two passes.  First, a
 * {@link Preprocessor} is used to validate and set default configuration settings. And
 * secondly, the finalized configuration is walked again (using a {@link ProcessorSupport},
 * to create the parser and property tree structure.  As components are initialized they can
 * be added to the tree structure using stacks and the {@link #pushParser(Component)} and 
 * {@link #pushProperty(Component)} methods.  After a component is finalized, it should be
 * removed from the stack using the {@link #popParser()} or {@link #popProperty()} method.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public abstract class ParserFactorySupport extends ProcessorSupport implements ParserFactory {

    private Stream stream;
    private String streamFormat;
    private boolean readEnabled = true;
    private boolean writeEnabled = true;
    private TypeHandlerFactory typeHandlerFactory;
    private ClassLoader classLoader;
    
    private Replicator replicator = new Replicator();
    private LinkedList<Component> parserStack = new LinkedList<Component>();
    private LinkedList<Component> propertyStack = new LinkedList<Component>();
    
    /**
     * Constructs a new <tt>ParserFactory</tt>.
     */
    public ParserFactorySupport() { }
    
    /**
     * Creates a new stream definition based on a stream configuration.
     * @param config the stream configuration
     * @return the new {@link Stream}
     * @throws BeanIOConfigurationException if the configuration is invalid
     */
    public Stream createStream(StreamConfig config) throws BeanIOConfigurationException {
        if (config.getName() == null) {
            throw new BeanIOConfigurationException("stream name not configured");
        }
        
        // pre-process configuration settings to set defaults and validate as much as possible 
        createPreprocessor(config).process(config);
        
        process(config);
        
        //((TreeNode)stream.getLayout()).print();
        
        return stream;
    }
    
    /**
     * Creates a stream configuration pre-processor.  May be overridden to return a
     * format specific version.
     * @param config the stream configuration to pre-process
     * @return the new {@link Preprocessor}
     */
    protected Preprocessor createPreprocessor(StreamConfig config) {
        return new Preprocessor(config);
    }
    
    /**
     * Creates a format specific {@link StreamFormat}. 
     * @param config the stream configuration
     * @return the new {@link StreamFormat}
     */
    protected abstract StreamFormat createStreamFormat(StreamConfig config);
    
    /**
     * Creates a format specific {@link RecordFormat}.
     * @param config the record configuration
     * @return the new {@link RecordFormat}
     */
    protected abstract RecordFormat createRecordFormat(RecordConfig config);
    
    /**
     * Creates a format specific {@link FieldFormat}.
     * @param config the field configuration
     * @param type the property type, or null if not bound to a property
     * @return the new {@link FieldFormat}
     */
    protected abstract FieldFormat createFieldFormat(FieldConfig config, Class<?> type);
    
    /**
     * Adds a component to the parser stack.
     * @param component the component to add
     */
    protected void pushParser(Component component) {
        if (!parserStack.isEmpty()) {
            parserStack.getLast().add(component);
        }
        parserStack.addLast(component);
        replicator.register(component);
        //System.out.println("Layout: " + parserStack);
    }
    
    /**
     * Removes the last component added to the parser stack.
     * @return the removed component
     */
    protected Component popParser() {
        return parserStack.removeLast();
    }
    
    /**
     * Adds a component to the property stack.
     * @param component the component to add
     */
    protected void pushProperty(Component component) {
        if (isBound()) {            
            // add properties to their parent bean or Map
            Component parent = propertyStack.getLast();
            switch (((Property)parent).type()) {
                case Property.SIMPLE:
                    throw new IllegalStateException();
                case Property.COMPLEX:
                case Property.MAP:
                    parent.add(component);
            }

            // if the parent property is an array or collection, the parser already holds
            // a reference to the child component when pushParser was called
        }
        
        propertyStack.addLast(component);
        replicator.register(component);
        //System.out.println("Property: " + propertyStack);
    }

    /**
     * Removes the last component added to the property stack.
     * @return the removed component
     */
    protected Property popProperty() {
        Property last = (Property) propertyStack.removeLast();
        if (propertyStack.isEmpty()) {
            // if we popped the last property, initialize the entire tree by calling clearValue 
            last.clearValue();
        }
        else {
            if (last.isIdentifier()) {
                ((Property)propertyStack.getLast()).setIdentifier(true);
            }
        }
        return last;
    }
    
    /**
     * Returns true if a property has been pushed onto the property stack, indicating
     * that further properties will be bound to a parent property.
     * @return true if a property is currently bound to a parent bean object
     */
    protected boolean isBound() {
        return !propertyStack.isEmpty();
    }
    
    @Override
    protected void initializeStream(StreamConfig config) throws BeanIOConfigurationException {
        streamFormat = config.getFormat();
        
        StreamFormat format = createStreamFormat(config);
        
        stream = new Stream(format);
        stream.setName(config.getName());
        
        // set the stream mode, defaults to read/write, the stream mode may be used
        // to enforce or relax validation rules specific to marshalling or unmarshalling
        String mode = config.getMode();
        if (mode == null || StreamConfig.READ_WRITE_MODE.equals(mode)) {
            stream.setMode(Stream.READ_WRITE_MODE);
        }
        else if (StreamConfig.READ_ONLY_MODE.equals(mode)) {
            stream.setMode(Stream.READ_ONLY_MODE);
            writeEnabled = false;
        }
        else if (StreamConfig.WRITE_ONLY_MODE.equals(mode)) {
            stream.setMode(Stream.WRITE_ONLY_MODE);
            readEnabled = false;
        }
        else {
            throw new BeanIOConfigurationException("Invalid mode '" + mode + "'");
        }
                
        ResourceBundleMessageFactory messageFactory = new ResourceBundleMessageFactory();
        
        // load the stream's default resource bundle
        String bundleName = Settings.getInstance().getProperty(
            "org.beanio." + config.getFormat() + ".messages");
        if (bundleName != null) {
            try {
                messageFactory.setDefaultResourceBundle(ResourceBundle.getBundle(bundleName));
            }
            catch (MissingResourceException ex) {
                throw new BeanIOConfigurationException("Missing default resource bundle '" +
                    bundleName + "' for stream format '" + config.getFormat() + "'", ex);
            }
        }

        // load the stream resource bundle
        bundleName = config.getResourceBundle();
        if (bundleName != null) {
            try {
                messageFactory.setResourceBundle(ResourceBundle.getBundle(bundleName));
            }
            catch (MissingResourceException ex) {
                throw new BeanIOConfigurationException("Missing resource bundle '" +
                    bundleName + "'", ex);
            }
        }

        stream.setMessageFactory(messageFactory);
        stream.setReplicator(replicator);
        
        initializeGroup(config);
    }
    
    @Override
    protected void finalizeStream(StreamConfig config) throws BeanIOConfigurationException {
        stream.setLayout((Selector)parserStack.getFirst());
        finalizeGroup(config);
    }
    
    @Override
    protected void initializeGroup(GroupConfig config) throws BeanIOConfigurationException {
        if (config.getChildren().isEmpty()) {
            throw new BeanIOConfigurationException("At least one record or group is required.");
        }
        
        Group group = new Group();
        group.setName(config.getName());
        group.setMinOccurs(config.getMinOccurs());
        group.setMaxOccurs(config.getMaxOccurs());
        group.setPosition(config.getOrder());
        
        // determine and validate the bean class
        Class<?> beanClass = getBeanClass(config);
        if (beanClass != null) {
            boolean isResult = propertyStack.isEmpty();
            
            Bean bean = new Bean();
            bean.setName(config.getName());
            bean.setType(beanClass);
            bean.setRequired(isResult);
            reflectPropertyType(config, bean);
            
            pushProperty(bean);
            
            group.setProperty(bean);
            group.setResult(isResult);
        }
        
        pushParser(group);
    }

    @Override
    protected void finalizeGroup(GroupConfig group) throws BeanIOConfigurationException {
        // pop the Group parser
        popParser();
        
        // pop a property if the group was mapped to a bean object
        if (group.getType() != null) {
            popProperty();
        }
    }

    @Override
    protected void initializeRecord(RecordConfig config) throws BeanIOConfigurationException {
        // determine and validate the bean class
        Bean bean = null;
        Class<?> beanClass = getBeanClass(config);
        if (beanClass != null) {
            bean = new Bean();
            bean.setName(config.getName());
            bean.setType(beanClass);
            bean.setRequired(propertyStack.isEmpty());
        }
        
        // handle records bound to a parent bean object assigned to a group
        if (config.isBound()) {
            // handle repeating records mapped to a collection
            if (config.isRepeating()) {
                initializeRecordIteration(config, bean);
                reflectPropertyType(config, bean);
            }
            // or a record mapped to a property of its parent
            else if (bean != null) {
                reflectPropertyType(config, bean);
            }
        }
        
        initializeRecordMain(config, bean);
    }
    
    protected void initializeRecordIteration(RecordConfig config, Bean bean) {
        // wrap the segment in an iteration
        Component collection = createSelectorIteration(config, bean);

        pushParser(collection);
        if (bean != null) {
            pushProperty(collection);
        }
    }
    
    protected void initializeRecordMain(RecordConfig config, Bean bean) {
        Record record = new Record();
        record.setName(config.getName());
        record.setMinOccurs(config.getMinOccurs());
        record.setMaxOccurs(config.getMaxOccurs());    
        record.setLazy(config.getMinOccurs() < config.getMaxOccurs());
        record.setRepeating(config.isRepeating());
        record.setSize(config.getMaxSize());
        record.setFormat(createRecordFormat(config));
        record.setOrder(config.getOrder());
        record.setIdentifier(config.isIdentifier());
        record.setProperty(bean);
        
        pushParser(record);
        if (bean != null) {
            pushProperty(bean);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.factory.ConfigurationProcessor#finalizeRecord(org.beanio.config.RecordConfig)
     */
    @Override
    protected void finalizeRecord(RecordConfig config) throws BeanIOConfigurationException {
        finalizeRecordMain(config);
        
        if (config.isBound() && config.isRepeating()) {
            finalizeRecordIteration(config);
        }
    }
    
    protected void finalizeRecordMain(RecordConfig config) {
        // pop the record bean from the property stack
        if (config.getType() != null) {
            popProperty();
        }
        // pop the record from the parser stack
        finalizeRecord(config, (Record)popParser());
    }
    
    protected void finalizeRecordIteration(RecordConfig config) {
        // pop the collection from the property stack
        if (config.getType() != null) {
            popProperty();
        }
        
        // pop the iteration from the parser stack
        popParser();
    }
    
    /**
     * Invoked by {@link #finalizeRecord(RecordConfig)} to allow subclasses to perform
     * further finalization of the created {@link Record}.
     * @param config the record configuration
     * @param record the {@link Record} being finalized
     */
    protected void finalizeRecord(RecordConfig config, Record record) { }

    /*
     * (non-Javadoc)
     * @see org.beanio.factory.ConfigurationProcessor#initializeSegment(org.beanio.config.SegmentConfig)
     */
    @Override
    protected final void initializeSegment(SegmentConfig config) throws BeanIOConfigurationException {
        Bean bean = null;
        
        // determine and validate the bean class
        Class<?> beanClass = getBeanClass(config);
        if (beanClass != null) {
            bean = new Bean();
            bean.setName(config.getName());
            bean.setType(beanClass);
            bean.setRequired(config.getMinOccurs() > 0 && !config.isNillable());
        }

        if (config.isRepeating()) {
            initializeSegmentIteration(config, bean);
        }
        initializeSegmentMain(config, bean);
    }
    
    /**
     * Called by {@link #initializeSegment(SegmentConfig)} to initialize segment iteration.
     * @param config the segment configuration
     * @param bean the bean bound to the segment, or null if no bean was bound
     */
    protected void initializeSegmentIteration(SegmentConfig config, Bean bean) {
        // wrap the segment in an iteration
        CollectionParser collection = createParserIteration(config, bean);
        
        pushParser(collection);
        if (bean != null) {
            pushProperty(collection);
        }
    }
    
    /**
     * Called by {@link #initializeSegment(SegmentConfig)} to initialize the segment.
     * @param config the segment configuration
     * @param bean the bean bound to the segment, or null if no bean was bound
     */
    protected void initializeSegmentMain(SegmentConfig config, Bean bean) {
        String name = config.getName();
        if (name == null) {
            throw new BeanIOConfigurationException("Segment name not set");
        }
        
        Segment segment = new Segment();
        segment.setName(config.getName());
        segment.setSize(config.getMaxSize());
        segment.setIdentifier(config.isIdentifier());
        segment.setLazy(config.getMinOccurs() < config.getMaxOccurs());
        segment.setRepeating(config.isRepeating());
        //segment.setLazyMarshalling(bean != null && config.isLazy() /*&& !config.isRepeating()*/);
        segment.setProperty(bean);
        segment.setExistencePredetermined(config.getDefaultExistence());
        
        if (!config.isRepeating() && bean != null) {
            reflectPropertyType(config, bean);
        }
    
        if (bean != null) {
            pushProperty(bean);
        }
        if (isSegmentRequired(config)) {
            pushParser(segment);
        }
    }

    protected boolean isSegmentRequired(SegmentConfig config) {
        if (config.isConstant()) {
            return false;
        }
        return (config.getType() != null);
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.factory.ConfigurationProcessor#finalizeSegment(org.beanio.config.SegmentConfig)
     */
    @Override
    protected final void finalizeSegment(SegmentConfig config) throws BeanIOConfigurationException {
        finalizeSegmentMain(config);
        if (config.isRepeating()) {
            finalizeSegmentIteration(config);
        }
    }
    
    /**
     * Called by {@link #finalizeSegment(SegmentConfig)} to finalize segment iteration.
     * @param config the segment configuration
     */
    protected void finalizeSegmentIteration(SegmentConfig config) {
        popParser();
        if (config.getType() != null) {
            popProperty(); // pop the iteration
        }
    }
    
    /**
     * Called by {@link #finalizeSegment(SegmentConfig)} to finalize the segment component.
     * @param config the segment configuration
     */
    protected void finalizeSegmentMain(SegmentConfig config) {
        if (isSegmentRequired(config)) {
            finalizeSegment(config, (Segment)popParser());  // pop the segment
        }   
        if (config.getType() != null) {     
            popProperty(); // pop the bean property
        }  
    }
    
    /**
     * Invoked by {@link #finalizeSegmentMain(SegmentConfig)} to allow subclasses to perform
     * further finalization of the created {@link Segment}.
     * @param config the segment configuration
     * @param segment the new {@link Segment}
     */
    protected void finalizeSegment(SegmentConfig config, Segment segment) { }

    /*
     * (non-Javadoc)
     * @see org.beanio.factory.ConfigurationProcessor#handleField(org.beanio.config.FieldConfig)
     */
    @Override
    protected void handleField(FieldConfig config) throws BeanIOConfigurationException {
        if (config.getName() == null) {
            throw new BeanIOConfigurationException("Missing field name");
        }
        
        Field field = new Field();
        field.setName(config.getName());
        field.setIdentifier(config.isIdentifier());
        field.setRequired(config.isRequired());
        field.setTrim(config.isTrim());
        field.setLiteral(config.getLiteral());
        field.setMinLength(config.getMinLength());
        field.setMaxLength(config.getMaxLength());
        field.setBound(config.isBound());

        try {
            field.setRegex(config.getRegex());
        }
        catch (PatternSyntaxException ex) {
            throw new BeanIOConfigurationException("Invalid regex pattern", ex);
        }

        // set the property type if explicitly configured
        if (config.getType() != null) {
            Class<?> propertyType = TypeUtil.toType(classLoader, config.getType());
            if (propertyType == null) {
                throw new BeanIOConfigurationException("Invalid type or type alias '" + config.getType() + "'");
            }
            field.setPropertyType(propertyType);
        }
        
        // whether or not this property is bound to a bean property, Collections targets are not
        boolean bind = isBound() && config.isBound() && !config.isRepeating();
        
        CollectionParser repeater = null;
        if (config.isRepeating()) {
            repeater = createParserIteration(config, field);
            
            pushParser(repeater);
            if (repeater.isProperty()) {
                pushProperty(repeater);
            }
        }
        else {
            if (bind) {
                reflectPropertyType(config, field);
            }
        }

        // if not already determined, this will update the field type
        field.setHandler(findTypeHandler(config, field));           
        
        // set the default field value using the configured type handler
        field.setDefaultValue(parseDefaultValue(field, config.getDefault()));

        field.setFormat(createFieldFormat(config, field.getType()));
        
        pushParser(field);
        if (bind) {
            pushProperty(field);
            popProperty();
        }
        popParser();
        
        if (repeater != null) {
            popParser();
            if (repeater.isProperty()) {
                popProperty();
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.factory.ConfigurationProcessor#handleConstant(org.beanio.config.ConstantConfig)
     */
    @Override
    protected void handleConstant(ConstantConfig config) throws BeanIOConfigurationException {
        Constant constant = new Constant();
        constant.setName(config.getName());
        constant.setIdentifier(config.isIdentifier());
        
        // determine the property type
        Class<?> propertyType = null;
        if (config.getType() != null) {
            propertyType = TypeUtil.toType(classLoader, config.getType());
            if (propertyType == null) {
                throw new BeanIOConfigurationException("Invalid type or type alias '" + config.getType() + "'");
            }
        }
        
        reflectPropertyType(config, constant);
        
        TypeHandler handler = findTypeHandler(config, constant);
        
        // set the property value using the configured type handler
        String text = config.getValue();
        if (text != null) {
            try {
                constant.setValue(handler.parse(text));
            }
            catch (TypeConversionException ex) {
                throw new BeanIOConfigurationException("Type conversion failed for " +
                    "configured value '" + text + "': " + ex.getMessage(), ex);
            }
        }
        
        pushProperty(constant);
        popProperty();
    }
    
    /**
     * Creates an iteration for a repeating property.
     * @param config the property configuration
     * @param property the property component, may be null if the iteration is not
     *   a property of its parent bean
     * @return the iteration component
     * @throws BeanIOConfigurationException if the collection type is invalid
     */
    protected CollectionParser createParserIteration(PropertyConfig config, Property property) 
        throws BeanIOConfigurationException
    {
        String collection = config.getCollection();
        
        // determine the collection type
        Class<? extends Collection<Object>> collectionType = null;
        if (collection != null) {
            collectionType = TypeUtil.toCollectionType(collection);
            if (collectionType == null) {
                throw new BeanIOConfigurationException("Invalid collection type or " +
                    "type alias '" + collection + "'");
            }
            collectionType = getConcreteCollectionType(collectionType);
        }
        
        // create the appropriate iteration type
        CollectionParser iteration = null;
        if (collectionType == TypeUtil.ARRAY_TYPE) {
            iteration = new ArrayParser();
        }
        else {
            iteration = new CollectionParser();
        }
        iteration.setName(config.getName());
        iteration.setMinOccurs(config.getMinOccurs());
        iteration.setMaxOccurs(config.getMaxOccurs());
        iteration.setType(collectionType);
        
        // if collection was set, then this is a property of its parent
        if (collectionType != null) {
            PropertyDescriptor descriptor = reflectCollectionType(iteration, property, 
                config.getGetter(), config.getSetter());
            
            // descriptor may be null if the parent was Map or Collection
            if (collectionType == TypeUtil.ARRAY_TYPE) {
                Class<? extends Object> arrayType = property.getType();
                if (descriptor != null) {
                    
                    // use the reflected array type
                    Class<? extends Object> reflectedType = descriptor.getPropertyType().getComponentType();  
                    if (arrayType != null && !TypeUtil.isAssignable(reflectedType, arrayType)) {
                        throw new BeanIOConfigurationException("Type '" + arrayType + 
                            "' is not assignable to array type '" + reflectedType + "'");
                    }
                    arrayType = reflectedType;
                    
                    // override target type if we were able to reflect its value
                    property.setType(reflectedType);
                }
                else if (arrayType == null) {
                    // default to String
                    arrayType = String.class;
                }
                
                ((ArrayParser)iteration).setArrayType(arrayType);
            }
        }
        
        return iteration;
    }
    
    /**
     * Creates an iteration for a repeating record.
     * @param config the record configuration
     * @param property the property component
     * @return the created {@link RecordCollection}
     * @throws BeanIOConfigurationException
     */
    protected RecordCollection createSelectorIteration(PropertyConfig config, Property property) 
        throws BeanIOConfigurationException
    {
        String collection = config.getCollection();
        
        // determine the collection type
        Class<? extends Collection<Object>> collectionType = null;
        if (collection != null) {
            collectionType = TypeUtil.toCollectionType(collection);
            if (collectionType == null) {
                throw new BeanIOConfigurationException("Invalid collection type or " +
                    "type alias '" + collection + "'");
            }
            collectionType = getConcreteCollectionType(collectionType);
        }
        
        // create the appropriate iteration type
        RecordCollection iteration;;
        if (collectionType == TypeUtil.ARRAY_TYPE) {
            iteration = new RecordArray();
        }
        else {
            iteration = new RecordCollection();
        }
        iteration.setName(config.getName());
        iteration.setType(collectionType);
        
        // if collection was set, then this is a property of its parent
        if (collectionType != null) {
            PropertyDescriptor descriptor = reflectCollectionType(iteration, property, 
                config.getGetter(), config.getSetter());
            
            // descriptor may be null if the parent was Map or Collection
            if (collectionType == TypeUtil.ARRAY_TYPE) {
                Class<? extends Object> arrayType = property.getType();
                if (descriptor != null) {
                    
                    // use the reflected array type
                    Class<? extends Object> reflectedType = descriptor.getPropertyType().getComponentType();   
                    if (arrayType != null && !TypeUtil.isAssignable(reflectedType, arrayType)) {
                        throw new BeanIOConfigurationException("Type '" + arrayType + 
                            "' is not assignable to array type '" + reflectedType + "'");
                    }
                    arrayType = reflectedType;
                    
                    // override target type if we were able to reflect its value
                    property.setType(arrayType);
                }
                else if (arrayType == null) {
                    // default to String
                    arrayType = String.class;
                }
                
                ((RecordArray)iteration).setArrayType(arrayType);
            }
        }
        
        return iteration;
    }
   
    /**
     * 
     * @param iteration
     * @param property
     * @param getter
     * @param setter
     * @throws BeanIOConfigurationException
     */
    protected PropertyDescriptor reflectCollectionType(Property iteration, Property property, 
        String getter, String setter) throws BeanIOConfigurationException {
        if (propertyStack.isEmpty()) {
            return null;
        }
        
        Property parent = (Property) propertyStack.getLast();
        switch (parent.type()) {
            case Property.SIMPLE:
                throw new BeanIOConfigurationException("Cannot add property to attribute");
            case Property.COLLECTION:
                return null;
            case Property.MAP:
                iteration.setAccessor(new MapAccessor(property.getName()));
                return null;
        }
        
        // set the property descriptor on the field
        PropertyDescriptor descriptor =  getPropertyDescriptor(property.getName(), getter, setter);
        
        Class<?> type = property.getType();
        
        if (iteration.getType() == TypeUtil.ARRAY_TYPE) {
            if (!descriptor.getPropertyType().isArray()) {
                throw new BeanIOConfigurationException("Collection type 'array' does not " +
                    "match bean property type '" + descriptor.getPropertyType().getName() + "'");
            }
            
            Class<?> arrayType = descriptor.getPropertyType().getComponentType();
            if (type == null) {
                property.setType(arrayType);
            }
            else if (!TypeUtil.isAssignable(arrayType, type)) {
                throw new BeanIOConfigurationException("Configured field array of type '" + 
                    type + "' is not assignable to bean property " +
                    "array of type '" + arrayType.getName() + "'");
                
            }
        }
        else {
            if (!descriptor.getPropertyType().isAssignableFrom(iteration.getType())) {
                Class<?> beanPropertyType = descriptor.getPropertyType();
                String beanPropertyTypeName;
                if (beanPropertyType.isArray()) {
                    beanPropertyTypeName = beanPropertyType.getComponentType().getName() + "[]";
                }
                else {
                    beanPropertyTypeName = beanPropertyType.getName();                
                }
                
                throw new BeanIOConfigurationException("Configured collection type '" +
                    iteration.getType().getName() + "' is not assignable to bean property " +
                    "type '" + beanPropertyTypeName + "'");
            }
        }
        
        iteration.setAccessor(new ReflectionAccessor(descriptor));
        return descriptor;
    }
    
    /**
     * Sets the property type and accessor using bean introspection.
     * @param config the property configuration
     * @param property the property
     * @throws BeanIOConfigurationException if the configured type is not assignable
     *   to the type determined through introspection
     */
    protected void reflectPropertyType(PropertyConfig config, Property property) throws BeanIOConfigurationException {
        if (propertyStack.isEmpty()) {
            return;
        }

        Property parent = (Property) propertyStack.getLast();
        switch (parent.type()) {
            case Property.SIMPLE:
                throw new BeanIOConfigurationException("Cannot add property to attribute");
            case Property.COLLECTION:
                return;
            case Property.MAP:
                property.setAccessor(new MapAccessor(property.getName()));
                return;
        }
        
        // set the property descriptor on the field
        PropertyDescriptor descriptor =  getPropertyDescriptor(property.getName(), 
            config.getGetter(), config.getSetter());

        Class<?> type = property.getType();
        if (type == null) {
            property.setType(descriptor.getPropertyType());
        }
        else if (!TypeUtil.isAssignable(descriptor.getPropertyType(), type)) {
            throw new BeanIOConfigurationException("Property type '" + 
                config.getType() + "' is not assignable to bean property " +
                "type '" + descriptor.getPropertyType().getName() + "'");
        }
        
        property.setAccessor(new ReflectionAccessor(descriptor));
    }
    
    /**
     * Returns the {@link PropertyDescriptor} for getting and setting a property value from
     * current bean class on the property stack.
     * @param property the property name
     * @param getter the getter method name, or null to use the default
     * @param setter the setter method name, or null to use the default
     * @return the <tt>PropertyDescriptor</tt>
     * @throws BeanIOConfigurationException if the property is not found on the bean class, or
     *   no read or write method is discovered
     */
    private PropertyDescriptor getPropertyDescriptor(String property, String getter, String setter) 
        throws BeanIOConfigurationException {
        
        Class<?> beanClass = ((Property)propertyStack.getLast()).getType();
        
        // calling new PropertyDescriptor(...) will throw an exception if either the getter
        // or setter method is not null and not found
        
        PropertyDescriptor descriptor = null;
        try {
            if (setter != null && getter != null) {
                descriptor = new PropertyDescriptor(property, beanClass, getter, setter);
            }
            else {
                // the Introspector class caches BeanInfo so subsequent calls shouldn't be a concern
                BeanInfo info = Introspector.getBeanInfo(beanClass);
                for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                    if (pd.getName().equals(property)) {
                        descriptor = pd;
                        break;
                    }
                }
                
                if (descriptor == null) {
                    if (setter == null && getter == null) {
                        throw new BeanIOConfigurationException("No such property '" + property +
                            "' in class '" + beanClass.getName() + "'");
                    }
                    else {
                        descriptor = new PropertyDescriptor(property, beanClass, getter, setter);
                    }
                }
                else if (setter != null) {
                    if (descriptor.getReadMethod() != null) {
                        getter = descriptor.getReadMethod().getName();
                    }
                    descriptor = new PropertyDescriptor(property, beanClass, getter, setter);
                }
                else if (getter != null) {
                    if (descriptor.getWriteMethod() != null) {
                        setter = descriptor.getWriteMethod().getName();
                    }
                    descriptor = new PropertyDescriptor(property, beanClass, getter, setter);
                }
            }
            
            // validate a read method is found for mapping configurations that write streams
            if (isReadEnabled() && descriptor.getWriteMethod() == null) {
                throw new BeanIOConfigurationException("No writeable method for property '" + property + 
                    "' in class '" + beanClass.getName() + "'");
            }
            // validate a write method is found for mapping configurations that read streams
            if (isWriteEnabled() && descriptor.getReadMethod() == null) {
                throw new BeanIOConfigurationException("No readable method for property '" + property + 
                    "' in class '" + beanClass.getName() + "'");
            }
            
            return descriptor;
        }
        catch (IntrospectionException e) {
            throw new BeanIOConfigurationException("Bean introspection failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Updates a simple property with its type and accessor, and returns a type handler for it.
     * @param config the property configuration
     * @param field the property to update
     * @return a type handler for the property
     */
    private TypeHandler findTypeHandler(SimplePropertyConfig config, Property field) {
        
        Class<?> propertyType = field.getType();
        
        // configure type handler properties
        Properties typeHandlerProperties = null;
        if (config.getFormat() != null) {
            typeHandlerProperties = new Properties();
            typeHandlerProperties.put(ConfigurableTypeHandler.FORMAT_SETTING, config.getFormat());
        }

        // determine the type handler based on the named handler or the field class
        TypeHandler handler = null;
        if (config.getTypeHandler() != null) {
            handler = typeHandlerFactory.getTypeHandler(config.getTypeHandler(), typeHandlerProperties);
            if (handler == null) {
                throw new BeanIOConfigurationException("No configured type handler named '" +
                    config.getTypeHandler() + "'");
            }

            // if the property type was not already determine, use the type from the type handler
            if (propertyType == null) {
                propertyType = handler.getType();
                field.setType(propertyType);
            }
            // otherwise validate the property type is compatible with the type handler
            else if (!TypeUtil.isAssignable(propertyType, handler.getType())) {
                throw new BeanIOConfigurationException("Field property type '" +
                    propertyType.getName() + "' is not compatible " +
                    "with assigned type handler named '" + config.getTypeHandler() + "'");
            }
        }
        else {
            // assume String type if the property type was not determined any other way
            if (propertyType == null) {
                propertyType = String.class;
                field.setType(String.class);
            }

            // get a type handler for the the property type
            String typeName = config.getType();
            try {
                if (typeName == null) {
                    typeName = propertyType.getName();
                    handler = typeHandlerFactory.getTypeHandlerFor(propertyType, streamFormat, typeHandlerProperties);
                }
                else {
                    handler = typeHandlerFactory.getTypeHandlerFor(typeName, streamFormat, typeHandlerProperties);
                }
            }
            catch (IllegalArgumentException ex) {
                throw new BeanIOConfigurationException(ex.getMessage(), ex);
            }
            if (handler == null) {
                throw new BeanIOConfigurationException("Type handler not found for type '" + typeName + "'");
            }
        }
        
        return handler;
    }
    
    /**
     * Returns the bean class for a segment configuration.
     * @param config the property configuration
     * @return the bean class
     * @throws BeanIOConfigurationException if the segment configuration is invalid
     */
    protected Class<?> getBeanClass(PropertyConfig config) {
        // determine the bean class associated with this record
        Class<?> beanClass = null;
        if (config.getType() != null) {
            if ("map".equals(config.getType())) {
                beanClass = HashMap.class;
            }
            else {
                try {
                    beanClass = classLoader.loadClass(config.getType());
                    if (isReadEnabled() && (beanClass.isInterface() || Modifier.isAbstract(beanClass.getModifiers()))) {
                        throw new BeanIOConfigurationException("Class must be concrete unless " +
                            "stream mode is set to '" + StreamConfig.WRITE_ONLY_MODE + "'");
                    }
                }
                catch (ClassNotFoundException ex) {
                    throw new BeanIOConfigurationException(
                        "Invalid bean class '" + config.getType() + "'", ex);
                }
            }
        }
        return beanClass;
    }
    
    /**
     * Returns the default concrete Collection subclass for a Collection type.
     * @param collectionType the configured Collection type
     * @return a concrete Collection subclass
     */
    @SuppressWarnings("unchecked")
    private Class<? extends Collection<Object>> getConcreteCollectionType(
        Class<? extends Collection<Object>> collectionType) {
        
        if (collectionType == null) {
            return null;
        }
        else if (collectionType != TypeUtil.ARRAY_TYPE && 
            (collectionType.isInterface() || Modifier.isAbstract(collectionType.getModifiers()))) {
            
            if (Set.class.isAssignableFrom(collectionType)) {
                return (Class<? extends Collection<Object>>)(Class<?>) HashSet.class;
            }
            else {
                return (Class<? extends Collection<Object>>)(Class<?>) ArrayList.class;
            }
        }
        else {
            return collectionType;
        }
    }
    
    /**
     * Parses a default field value.
     * @param field the field
     * @param text the text to parse
     * @return the default value
     */
    protected Object parseDefaultValue(Field field, String text) {
        if (text == null) {
            return null;
        }
        
        TypeHandler handler = field.getHandler();
        if (handler != null) {
            try {
                return handler.parse(text);
            }
            catch (TypeConversionException ex) {
                throw new BeanIOConfigurationException("Type conversion failed for " +
                    "configured default '" + text + "': " + ex.getMessage(), ex);
            }
        }
        else {
            return text;
        }
    }
    
    /**
     * Creates a default record reader factory.
     * @return the new <tt>RecordReaderFactory</tt>
     */
    protected abstract RecordReaderFactory newRecordReaderFactory();

    /**
     * Creates a default record writer factory
     * @return the new <tt>RecordWriterFactory</tt>
     */
    protected abstract RecordWriterFactory newRecordWriterFactory();

    /**
     * Sets the type handler factory to use to create the stream definition.
     * @param typeHandlerFactory the <tt>TypeHandlerFactory</tt> to use to
     *   create the stream definition
     */
    public void setTypeHandlerFactory(TypeHandlerFactory typeHandlerFactory) {
        this.typeHandlerFactory = typeHandlerFactory;
    }
    
    /**
     * Returns whether the stream definition must support reading
     * an input stream.
     * @return <tt>true</tt> if the stream definition must support reading
     *   an input stream
     * @since 1.2
     */
    public boolean isReadEnabled() {
        return readEnabled;
    }

    /**
     * Returns whether the stream definition must support writing to an
     * output stream.
     * @return <tt>true</tt> if the stream definition must support writing
     *   to an output stream
     * @since 1.2
     */
    public boolean isWriteEnabled() {
        return writeEnabled;
    }
    
    /**
     * Loads the {@link RecordReaderFactory} for a stream configuration.
     * @param config the stream configuration
     * @return the new {@link RecordReaderFactory}
     */
    protected RecordReaderFactory getRecordReaderFactory(StreamConfig config) {
        // configure the record reader factory
        org.beanio.internal.config.BeanConfig readerFactoryBean = config.getReaderFactory();
        if (readerFactoryBean == null) {
            return null;
        }
        
        RecordReaderFactory factory;
        if (readerFactoryBean.getClassName() == null) {
            factory = newRecordReaderFactory();
        }
        else {
            Object object = BeanUtil.createBean(classLoader, readerFactoryBean.getClassName());
            if (!RecordReaderFactory.class.isAssignableFrom(object.getClass())) {
                throw new BeanIOConfigurationException("Configured reader factory class '" +
                    readerFactoryBean.getClassName() + "' does not implement RecordReaderFactory");
            }
            factory = (RecordReaderFactory) object;
        }
        BeanUtil.configure(factory, readerFactoryBean.getProperties());
        return factory;
    }
    
    /**
     * Loads the {@link RecordWriterFactory} for a stream configuration.
     * @param config the stream configuration
     * @return the new {@link RecordWriterFactory}
     */
    protected RecordWriterFactory getRecordWriterFactory(StreamConfig config) {
        // configure the record writer factory
        org.beanio.internal.config.BeanConfig writerFactoryBean = config.getWriterFactory();
        if (writerFactoryBean == null) {
            return null;
        }
        
        RecordWriterFactory factory;
        if (writerFactoryBean.getClassName() == null) {
            factory = newRecordWriterFactory();
        }
        else {
            Object object = BeanUtil.createBean(classLoader, writerFactoryBean.getClassName());
            if (!RecordWriterFactory.class.isAssignableFrom(object.getClass())) {
                throw new BeanIOConfigurationException("Configured writer factory class '" +
                    writerFactoryBean.getClassName() + "' does not implement RecordWriterFactory");
            }
            factory = (RecordWriterFactory) object;
        }
        BeanUtil.configure(factory, writerFactoryBean.getProperties());
        return factory;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.factory.ParserFactory#setClassLoader(java.lang.ClassLoader)
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
