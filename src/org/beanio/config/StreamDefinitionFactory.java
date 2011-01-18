/*
 * Copyright 2010-2011 Kevin Seim
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
package org.beanio.config;

import java.beans.*;
import java.util.*;
import java.util.regex.PatternSyntaxException;

import org.beanio.BeanIOConfigurationException;
import org.beanio.parser.*;
import org.beanio.stream.*;
import org.beanio.types.*;
import org.beanio.util.*;

/**
 * A <tt>StreamDefinitionFactory</tt> is used to convert a stream configuration into a
 * <tt>StreamDefinition</tt> for parsing an I/O stream.
 *   
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class StreamDefinitionFactory {

    private TypeHandlerFactory typeHandlerFactory;

    /**
     * Constructs a new <tt>StreamDefinitionFactory</tt>.
     */
    public StreamDefinitionFactory() { }

    /**
     * Creates a new stream definition based on a stream configuration.
     * @param config the stream configuration
     * @return the new <tt>StreamDefinition</tt>
     * @throws BeanIOConfigurationException if the configuration is invalid
     */
    public StreamDefinition createStreamDefinition(StreamConfig config) throws BeanIOConfigurationException {
        if (config.getName() == null) {
            throw new BeanIOConfigurationException("stream name not configured");
        }
        
        try {
            StreamDefinition definition = newStreamDefinition(config);
            compileStreamDefinition(config, definition);
            return definition;
        }
        catch (BeanIOConfigurationException ex) {
            throw new BeanIOConfigurationException("Invalid '" + config.getName() +
                "' stream configuration: " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Compiles a stream definition from its configuration.
     * @param config the stream configuration
     * @param definition the stream definition to compile
     * @throws BeanIOConfigurationException if the configuration is invalid
     */
    protected void compileStreamDefinition(StreamConfig config, StreamDefinition definition) 
        throws BeanIOConfigurationException {

        definition.setName(config.getName());
        definition.setMinOccurs(config.getRootGroupConfig().getMinOccurs());
        definition.setMaxOccurs(config.getRootGroupConfig().getMaxOccurs());

        // configure the record reader factory
        Bean readerFactoryBean = config.getReaderFactory();
        if (readerFactoryBean != null) {
            RecordReaderFactory factory;
            if (readerFactoryBean.getClassName() == null) {
                factory = newRecordReaderFactory();
            }
            else {
                Object object = BeanUtil.createBean(readerFactoryBean.getClassName());
                if (!RecordReaderFactory.class.isAssignableFrom(object.getClass())) {
                    throw new BeanIOConfigurationException("Configured reader factory class '" +
                        readerFactoryBean.getClassName() + "' does not implement RecordReaderFactory");
                }
                factory = (RecordReaderFactory) object;
            }
            BeanUtil.configure(factory, readerFactoryBean.getProperties());
            definition.setReaderFactory(factory);
        }

        // configure the record writer factory
        Bean writerFactoryBean = config.getWriterFactory();
        if (writerFactoryBean != null) {
            RecordWriterFactory factory;
            if (writerFactoryBean.getClassName() == null) {
                factory = newRecordWriterFactory();
            }
            else {
                Object object = BeanUtil.createBean(writerFactoryBean.getClassName());
                if (!RecordReaderFactory.class.isAssignableFrom(object.getClass())) {
                    throw new BeanIOConfigurationException("Configured writer factory class '" +
                        writerFactoryBean.getClassName() + "' does not implement RecordWriterFactory");
                }
                factory = (RecordWriterFactory) object;
            }
            BeanUtil.configure(factory, writerFactoryBean.getProperties());
            definition.setWriterFactory(factory);
        }

        // load the stream's default resource bundle
        String bundleName = Settings.getInstance().getProperty(
            "org.beanio." + definition.getFormat() + ".messages");
        if (bundleName != null) {
            try {
                definition.setDefaultResourceBundle(ResourceBundle.getBundle(bundleName));
            }
            catch (MissingResourceException ex) {
                throw new BeanIOConfigurationException("Missing default resource bundle '" +
                    bundleName + "' for stream format '" + definition.getFormat() + "'", ex);
            }
        }

        // load the stream resource bundle
        bundleName = config.getResourceBundle();
        if (bundleName != null) {
            try {
                definition.setResourceBundle(ResourceBundle.getBundle(bundleName));
            }
            catch (MissingResourceException ex) {
                throw new BeanIOConfigurationException("Missing resource bundle '" +
                    bundleName + "'", ex);
            }
        }

        compileGroupDefinition(config, config.getRootGroupConfig(), definition.getRootGroupDefinition());
    }

    /**
     * Compiles a group definition from its configuration.
     * @param streamConfig the parent stream configuration
     * @param groupConfig the group configuration
     * @param definition the group definition to compile
     */
    protected final void compileGroupDefinition(StreamConfig streamConfig, GroupConfig groupConfig,
        GroupDefinition definition) {

        List<NodeConfig> nodeList = groupConfig.getChildren();
        if (nodeList.isEmpty()) {
            throw new BeanIOConfigurationException("At least one record or record group is required.");
        }
        
        // by default, order is incremented for each record
        int lastOrder = 0;
        for (NodeConfig child : nodeList) {
            String nodeType = child.getType() == NodeConfig.RECORD ? "record" : "group";

            int order;
            if (streamConfig.isOrdered()) {
                order = child.getOrder();
                // defaults the order to the last order plus one
                if (order < 1) {
                    order = lastOrder + 1;
                }
                // validate all nodes are in order
                else if (order < lastOrder) {
                    throw new BeanIOConfigurationException(
                        "'" + child.getName() + "' " + nodeType + " configuration is out of order");
                }
                lastOrder = order;
            }
            else {
                // validate node order isn't set if the stream is unordered
                if (child.getOrder() >= 0) {
                    throw new BeanIOConfigurationException(
                        "Invalid '" + child.getName() + "' " + nodeType + " configuration: " +
                        "Order cannot be set when ordered=\"false\"");
                }
                order = 1;
            }

            NodeDefinition childDefinition;
            switch (child.getType()) {

            case NodeConfig.RECORD:
                try {
                    childDefinition = newRecordDefinition((RecordConfig)child);
                    compileNodeDefinition(child, childDefinition, order);
                    compileRecordDefinition((RecordConfig) child, (RecordDefinition) childDefinition);
                    compileFieldDefinitions((RecordConfig) child, (RecordDefinition) childDefinition);
                }
                catch (BeanIOConfigurationException ex) {
                    throw new BeanIOConfigurationException("Invalid '" + child.getName() + 
                        "' record configuration: " + ex.getMessage(), ex);
                }
                break;

            case NodeConfig.GROUP:
                try {
                    childDefinition = newGroupDefinition((GroupConfig) child);
                    compileNodeDefinition(child, childDefinition, order);
                    compileGroupDefinition(streamConfig, (GroupConfig) child, (GroupDefinition) childDefinition);
                }
                catch (BeanIOConfigurationException ex) {
                    throw new BeanIOConfigurationException("Invalid '" + child.getName() + 
                        "' group configuration: " + ex.getMessage(), ex);
                }
                break;

            default:
                throw new IllegalStateException("Invalid node type: " + child.getType());
            }

            definition.addChild(childDefinition);
        }
    }
    
    private void compileNodeDefinition(NodeConfig config, NodeDefinition definition, int order) {
        definition.setName(config.getName());
        definition.setOrder(order);
        int minOccurs = config.getMinOccurs() != null ? config.getMinOccurs() : 1; // default to unbounded
        definition.setMinOccurs(minOccurs);
        int maxOccurs = config.getMaxOccurs() != null ? config.getMaxOccurs() : -1; // default to unbounded
        definition.setMaxOccurs(maxOccurs);

        if (maxOccurs == 0) {
            throw new BeanIOConfigurationException("maxOccurs must be 1 or greater");
        }
        if (maxOccurs > 0 && maxOccurs < minOccurs) {
            throw new BeanIOConfigurationException("maxOccurs cannot be less than minOccurs");
        }
    }

    /**
     * Compiles a record definition from its configuration.
     * @param config the record configuration
     * @param definition the record definition
     */
    protected void compileRecordDefinition(RecordConfig config, RecordDefinition definition) {
        // determine the bean class associated with this record
        Class<?> beanClass = null;
        if (config.getBeanClass() != null) {
            if ("map".equals(config.getBeanClass())) {
                definition.setBeanClass(HashMap.class);
            }
            else {
                try {
                    beanClass = Class.forName(config.getBeanClass());
                    definition.setBeanClass(beanClass);
                }
                catch (ClassNotFoundException ex) {
                    throw new BeanIOConfigurationException(
                        "Invalid record class '" + config.getBeanClass() + "'", ex);
                }
            }
        }
    }

    /**
     * Compiles field definitions for a record.  The compiled field definitions are
     * added to the record definition.
     * @param recordConfig the record configuration
     * @param recordDefinition the record definition to update
     */
    protected void compileFieldDefinitions(RecordConfig recordConfig, RecordDefinition recordDefinition) {
        for (FieldConfig field : recordConfig.getFieldList()) {
            if (field.getName() == null) {
                throw new BeanIOConfigurationException("Missing field name");
            }

            try {
                FieldDefinition fieldDefinition = newFieldDefinition(field);
                fieldDefinition.setName(field.getName());
                fieldDefinition.setRecordIdentifier(field.isRecordIdentifier());
                fieldDefinition.setRequired(field.isRequired());
                fieldDefinition.setTrim(field.isTrim());
                fieldDefinition.setLiteral(field.getLiteral());
                try {
                    fieldDefinition.setRegex(field.getRegex());
                }
                catch (PatternSyntaxException ex) {
                    throw new BeanIOConfigurationException("Invalid regex pattern", ex);
                }
    
                int minLength = field.getMinLength() != null ? field.getMinLength() : 0;
                fieldDefinition.setMinLength(minLength);
                int maxLength = field.getMaxLength() != null ? field.getMaxLength() : -1;
                fieldDefinition.setMaxLength(maxLength);
    
                if (maxLength > 0 && maxLength < minLength) {
                    throw new BeanIOConfigurationException("maxLength must be greater than or equal to minLength");
                }
                if (fieldDefinition.getLiteral() != null) {
                    int size = fieldDefinition.getLiteral().length();
                    if (size < minLength) {
                        throw new BeanIOConfigurationException("literal text length is less than minLength");
                    }
                    if (maxLength >= 0 && size > maxLength) {
                        throw new BeanIOConfigurationException("literal text length is greater than maxLength");
                    }
                }
                
                // validate regex or literal is configured for record identifying fields
                if (fieldDefinition.isRecordIdentifier()) {
                    if (fieldDefinition.getLiteral() == null && fieldDefinition.getRegex() == null) {
                        throw new BeanIOConfigurationException("literal or regex pattern required " + 
                            " for record identifying fields");
                    }
                }
    
                updateFieldType(field, fieldDefinition, recordDefinition);
                
                // set the default field value using the configured type handler
                String defaultText = field.getDefault();
                if (defaultText != null) {
                    TypeHandler handler = fieldDefinition.getTypeHandler();
                    if (handler != null) {
                        try {
                            fieldDefinition.setDefaultValue(handler.parse(defaultText));
                        }
                        catch (TypeConversionException ex) {
                            throw new BeanIOConfigurationException("Type conversion failed for " +
                                "configured default '" + defaultText + "': " + ex.getMessage(), ex);
                        }
                    }
                    else {
                        fieldDefinition.setDefaultValue(defaultText);
                    }
                }
                
                recordDefinition.addField(fieldDefinition);
            }
            catch (BeanIOConfigurationException ex) {
                throw new BeanIOConfigurationException("Invalid '" + field.getName() +
                    "' field configuration: " + ex.getMessage(), ex);
            }
        }
    }
    
    /**
     * Updates a field definition's property type, property descriptor and type handler
     * attributes.
     * @param config the field configuration
     * @param fieldDefinition the field definition to update
     * @param recordDefinition the record definition the field belongs to
     */
    private void updateFieldType(FieldConfig config, FieldDefinition fieldDefinition, 
        RecordDefinition recordDefinition) {
        
        // handle ignored fields
        if (config.isIgnored() || recordDefinition.getBeanClass() == null) {
            fieldDefinition.setProperty(false);
            fieldDefinition.setPropertyType(null);
            fieldDefinition.setPropertyDescriptor(null);
            return;
        }
        
        // update the definition to indicate the field is a bean property
        fieldDefinition.setProperty(true);
        
        // determine the field class
        Class<?> propertyType = null;
        if (config.getType() != null) {
            propertyType = TypeHandlerFactory.toType(config.getType());
        }
        
        // set the property descriptor on the field
        PropertyDescriptor descriptor = null;
        if (!recordDefinition.isBeanClassMap()) {
            descriptor = getPropertyDescriptor(config, recordDefinition.getBeanClass());
            
            if (propertyType == null) {
                propertyType = descriptor.getPropertyType();
            }
            else {
                if (!TypeUtil.isAssignable(descriptor.getPropertyType(), propertyType)) {
                    throw new BeanIOConfigurationException("Configured type '" + propertyType.getName() +
                        "' is not compatible with bean property type '" + descriptor.getPropertyType().getName() + "'");
                }
            }
        }
        fieldDefinition.setPropertyDescriptor(descriptor);
        
        // determine the type handler based on the named handler or the field class
        TypeHandler handler = null;
        if (config.getTypeHandler() != null) {
            handler = typeHandlerFactory.getTypeHandler(config.getTypeHandler());
            if (handler == null) {
                throw new BeanIOConfigurationException("No configured type handler named '" +
                    config.getTypeHandler() + "'");
            }
            
            // if the property type was not already determine, use the type from the type handler
            if (propertyType == null) {
                propertyType = handler.getType();
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
            }
            
            // get a type handler for the the property type
            handler = typeHandlerFactory.getTypeHandler(propertyType);
            if (handler == null) {
                throw new BeanIOConfigurationException("Type handler not found for type '" +
                    propertyType.getName() + "'");
            }
        }
        
        fieldDefinition.setPropertyType(propertyType);
        fieldDefinition.setTypeHandler(handler);
    }

    /**
     * Returns the <tt>PropertyDescriptor</tt> for setting and getting the field value from
     * the bean class.
     * @param config the field configuration
     * @param beanClass the bean class the field configuration refers to
     * @return the <tt>PropertyDescriptor</tt>
     */
    private PropertyDescriptor getPropertyDescriptor(FieldConfig config, Class<?> beanClass) {
        String setter = config.getSetter();
        String getter = config.getGetter();
        
        try {
            if (setter != null && getter != null) {
                if ("".equals(setter)) {
                    setter = null;
                }
                if ("".equals(getter)) {
                    getter = null;
                }
                return new PropertyDescriptor(config.getName(), beanClass, getter, setter);
            }
            else if (setter == null && getter == null) {
                return new PropertyDescriptor(config.getName(), beanClass);
            }
            else {
                PropertyDescriptor descriptor = null;
                BeanInfo info = Introspector.getBeanInfo(beanClass, Object.class);
                for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                    if (pd.getName().equals(config.getName())) {
                        descriptor = pd;
                        break;
                    }
                }
                if (descriptor == null) {
                    throw new BeanIOConfigurationException("No such property '" + config.getName() + 
                        "' on class '" + beanClass.getName() + "'");
                }
                if (getter != null) {
                    try {
                        if ("".equals(getter))
                            descriptor.setReadMethod(null);
                        else
                            descriptor.setReadMethod(beanClass.getDeclaredMethod(getter, (Class[]) null));
                    }
                    catch (NoSuchMethodException ex) {
                        throw new BeanIOConfigurationException("getter method not found: " + 
                            ex.getMessage() , ex);
                    }
                }
                if (setter != null) {
                    try {
                        if ("".equals(setter))
                            descriptor.setWriteMethod(null);
                        else
                            descriptor.setWriteMethod(beanClass.getDeclaredMethod(setter, 
                                new Class[] { descriptor.getPropertyType() }));
                    }
                    catch (NoSuchMethodException ex) {
                        throw new BeanIOConfigurationException("setter method not found: " +
                            ex.getMessage(), ex);
                    }
                }
                return descriptor;
            }
        }
        catch (IntrospectionException e) {
            throw new BeanIOConfigurationException("Bean introspection error: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new <tt>FieldDefinition</tt>.
     * @param field the field configuration
     * @return the new <tt>FieldDefinition</tt>
     */
    protected abstract FieldDefinition newFieldDefinition(FieldConfig field);

    /**
     * Creates a new <tt>RecordDefinition</tt>.
     * @param record the record configuration
     * @return the new <tt>RecordDefinition</tt>
     */
    protected abstract RecordDefinition newRecordDefinition(RecordConfig record);

    /**
     * Creates a new <tt>GroupDefinition</tt>.
     * @param group the group configuration
     * @return the new <tt>GroupDefinition</tt>
     */
    protected GroupDefinition newGroupDefinition(GroupConfig group) {
        return new GroupDefinition();
    }
    
    /**
     * Creates a new <tt>StreamDefinition</tt>.
     * @param stream the stream configuration
     * @return the new <tt>StreamDefinition</tt>
     */
    protected abstract StreamDefinition newStreamDefinition(StreamConfig stream);

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
}
