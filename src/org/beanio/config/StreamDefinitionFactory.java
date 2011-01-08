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
import org.beanio.util.Settings;

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
     * Compiles a stream definition from its configuration.
     * @param config the stream configuration
     * @return the stream definition
     * @throws BeanIOConfigurationException if the configuration is invalid
     */
    public StreamDefinition compileStreamDefinition(StreamConfig config) throws BeanIOConfigurationException {
        if (config.getName() == null) {
            throw new BeanIOConfigurationException("Stream name not set");
        }

        StreamDefinition definition = createStreamDefinition(config);
        definition.setName(config.getName());
        definition.setMinOccurs(config.getRootGroupConfig().getMinOccurs());
        definition.setMaxOccurs(config.getRootGroupConfig().getMaxOccurs());

        try {
            // configure the record reader factory
            Bean readerFactoryBean = config.getReaderFactory();
            if (readerFactoryBean != null) {
                RecordReaderFactory factory;
                if (readerFactoryBean.getClassName() == null) {
                    factory = createDefaultRecordReaderFactory();
                }
                else {
                    Object object = BeanUtil.createBean(readerFactoryBean.getClassName());
                    if (!RecordReaderFactory.class.isAssignableFrom(object.getClass())) {
                        throw new BeanIOConfigurationException("Reader class '" +
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
                    factory = createDefaultRecordWriterFactory();
                }
                else {
                    Object object = BeanUtil.createBean(writerFactoryBean.getClassName());
                    if (!RecordReaderFactory.class.isAssignableFrom(object.getClass())) {
                        throw new BeanIOConfigurationException("Writer class '" +
                            writerFactoryBean.getClassName() + "' does not implement RecordReaderFactory");
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
                        bundleName + "' for stream '" + config.getName() + "'", ex);
                }
            }

            compileGroupDefinition(config, config.getRootGroupConfig(), definition.getRootGroupContext());
            return definition;
        }
        catch (BeanIOConfigurationException ex) {
            throw new BeanIOConfigurationException("Invalid configuration for stream '" +
                config.getName() + "': " + ex.getMessage(), ex);
        }
    }

    /**
     * Compiles a group definition.
     * @param streamConfig the parent stream configuration
     * @param groupConfig the group configuration
     * @param definition the group definition to compile
     */
    protected void compileGroupDefinition(StreamConfig streamConfig, GroupConfig groupConfig,
        GroupDefinition definition) {

        // TODO validate each node has at least one required record?

        // by default, order is incremented for each record
        int lastOrder = 0;
        List<NodeConfig> nodeList = groupConfig.getNodeList();
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
                        nodeType + "'" + child.getName() + "' out of order");
                }
                lastOrder = order;
            }
            else {
                // validate node order isn't set if the stream is unordered
                if (child.getOrder() >= 0) {
                    throw new BeanIOConfigurationException(
                        "Order cannot be set on unordered stream for " +
                            nodeType + "'" + child.getName() + "'");
                }
                order = 1;
            }

            NodeDefinition childDefinition;
            char type = child.getType();
            switch (type) {

            case NodeConfig.RECORD:
                childDefinition = compileRecordDefinition((RecordConfig) child);
                compileFieldDefinitions((RecordConfig) child, (RecordDefinition) childDefinition);
                break;

            case NodeConfig.GROUP:
                childDefinition = createGroupDefinition((GroupConfig) child);
                compileGroupDefinition(streamConfig, (GroupConfig) child, (GroupDefinition) childDefinition);
                break;

            default:
                throw new IllegalStateException("Invalid node type: " + type);
            }

            childDefinition.setName(child.getName());
            childDefinition.setOrder(order);
            int minOccurs = child.getMinOccurs() != null ? child.getMinOccurs() : 1; // default to unbounded
            childDefinition.setMinOccurs(minOccurs);
            int maxOccurs = child.getMaxOccurs() != null ? child.getMaxOccurs() : -1; // default to unbounded
            childDefinition.setMaxOccurs(maxOccurs);

            if (maxOccurs == 0) {
                throw new BeanIOConfigurationException("maxOccurs must be 1 or greater on " +
                    nodeType + " '" + child.getName() + "'");
            }
            if (maxOccurs > 0 && maxOccurs < minOccurs) {
                throw new BeanIOConfigurationException("maxOccurs cannot be less than minOccurs on " +
                    nodeType + " '" + child.getName() + "'");
            }

            definition.addChild(childDefinition);
        }
    }

    /**
     * Compiles a record definition from its configuration.
     * @param config the record configuration
     */
    protected RecordDefinition compileRecordDefinition(RecordConfig config) {

        // create the definition
        RecordDefinition definition = createRecordDefinition(config);

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
        return definition;
    }

    /**
     * 
     * @param recordConfig
     * @param recordDefinition
     */
    protected void compileFieldDefinitions(RecordConfig recordConfig, RecordDefinition recordDefinition) {
        for (FieldConfig field : recordConfig.getFieldList()) {
            if (field.getName() == null) {
                throw new BeanIOConfigurationException("Missing field name on record '" + recordConfig.getName() + "'");
            }

            FieldDefinition fieldDefinition = createFieldDefinition(field);
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
                throw new BeanIOConfigurationException("Max length must be greater than or equal to min length");
            }
            if (fieldDefinition.getLiteral() != null) {
                int size = fieldDefinition.getLiteral().length();
                if (size < minLength) {
                    throw new BeanIOConfigurationException("Literal length is less than min length");
                }
                if (maxLength >= 0 && size > maxLength) {
                    throw new BeanIOConfigurationException("Literal length is greater than max length");
                }
            }

            // determine the field class
            Class<?> fieldClass = null;
            if (field.getType() != null) {
                fieldClass = TypeHandlerFactory.toType(field.getType());
            }

            if (field.isIgnored()) {
                fieldDefinition.setProperty(false);
            }
            else if (recordDefinition.isBeanClassMap()) {
                fieldDefinition.setProperty(true);
            }
            else if (recordDefinition.getBeanClass() != null) {
                PropertyDescriptor descriptor = getPropertyDescriptor(field, recordDefinition.getBeanClass());

                fieldDefinition.setProperty(true);
                fieldDefinition.setPropertyDescriptor(descriptor);
                if (fieldClass == null) {
                    fieldClass = descriptor.getPropertyType();
                }
                else {
                    // TODO improve the validation to verify the field class and bean property class
                    // are compatible (primitives, particularly, numbers are a little tricky...)
                }
            }

            // determine the type handler based on the named handler or the field class
            TypeHandler handler = null;
            if (field.getHandler() != null) {
                handler = typeHandlerFactory.getTypeHandler(field.getHandler());
                if (handler == null) {
                    throw new BeanIOConfigurationException("Type handler not found named '" +
                            field.getHandler() + "' on field '" + field.getName() + "'");
                }
            }
            else if (fieldClass != null) {
                handler = typeHandlerFactory.getTypeHandler(fieldClass);
                if (handler == null) {
                    throw new BeanIOConfigurationException("Type handler not found for type '" +
                        fieldClass.getName() + "' on field '" + field.getName() + "'");
                }
            }
            else {
                // default to the string handler
                handler = typeHandlerFactory.getTypeHandler(String.class);
            }

            // set the handler on the field context
            if (handler != null) {
                fieldDefinition.setTypeHandler(handler);

                if (field.getDefault() != null) {
                    try {
                        fieldDefinition.setDefaultValue(handler.parse(field.getDefault()));
                    }
                    catch (TypeConversionException ex) {
                        throw new BeanIOConfigurationException("Type conversion failed for default value '" +
                                field.getDefault() + "'", ex);
                    }
                }
            }
            else {
                fieldDefinition.setDefaultValue(field.getDefault());
            }

            if (fieldDefinition.isRecordIdentifier()) {
                if (fieldDefinition.getLiteral() == null) {
                    throw new BeanIOConfigurationException("Field '" + field.getName() +
                        "' must have a literal or regex value if key=true");
                }
            }

            recordDefinition.addField(fieldDefinition);
        }
    }

    /**
     * Returns the <tt>PropertyDescriptor</tt> for setting/getting the field value from
     * the bean class.
     * @param config the field configuration
     * @param beanClass the bean class the field configuration refers to
     * @return the <tt>PropertyDescriptor</tt>
     */
    protected PropertyDescriptor getPropertyDescriptor(FieldConfig config, Class<?> beanClass) {
        String setter = config.getSetter();
        String getter = config.getGetter();
        if (setter == null && getter == null) {
            try {
                return new PropertyDescriptor(config.getName(), beanClass);
            }
            catch (IntrospectionException e) {
                throw new BeanIOConfigurationException("Property '" + config.getName() +
                    "' not found on bean class '" + beanClass.getName() + "'", e);
            }
        }
        else {
            try {
                return new PropertyDescriptor(config.getName(), beanClass, getter, setter);
            }
            catch (IntrospectionException e) {
                throw new BeanIOConfigurationException("Property '" + config.getName() +
                        "' not found on bean class " + beanClass, e);
            }
        }
    }

    protected abstract FieldDefinition createFieldDefinition(FieldConfig field);

    protected abstract RecordDefinition createRecordDefinition(RecordConfig record);

    protected abstract StreamDefinition createStreamDefinition(StreamConfig stream);

    protected abstract RecordReaderFactory createDefaultRecordReaderFactory();

    protected abstract RecordWriterFactory createDefaultRecordWriterFactory();

    protected GroupDefinition createGroupDefinition(GroupConfig group) {
        return new GroupDefinition();
    }

    public void setTypeHandlerFactory(TypeHandlerFactory typeHandlerFactory) {
        this.typeHandlerFactory = typeHandlerFactory;
    }
}
