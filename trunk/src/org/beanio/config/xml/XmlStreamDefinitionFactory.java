/*
 * Copyright 2011 Kevin Seim
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
package org.beanio.config.xml;

import org.beanio.BeanIOConfigurationException;
import org.beanio.config.*;
import org.beanio.parser.*;
import org.beanio.parser.xml.*;
import org.beanio.stream.*;
import org.beanio.stream.xml.*;
import org.beanio.types.TypeHandlerFactory;
import org.beanio.types.xml.*;
import org.beanio.util.*;

/**
 * A <tt>XmlStreamDefinitionFactory</tt> is used to create stream definitions 
 * for XML formatted streams from a BeanIO mapping configuration.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlStreamDefinitionFactory extends StreamDefinitionFactory {

    private static final TypeHandlerFactory defaultTypeHandlerFactory;
    static {
        defaultTypeHandlerFactory = new TypeHandlerFactory();
        defaultTypeHandlerFactory.registerHandlerFor(TypeUtil.DATE_ALIAS, new XmlDateTypeHandler());
        defaultTypeHandlerFactory.registerHandlerFor(TypeUtil.DATETIME_ALIAS, new XmlDateTimeTypeHandler());
        defaultTypeHandlerFactory.registerHandlerFor(TypeUtil.TIME_ALIAS, new XmlTimeTypeHandler());
    }
    
    @Override
    public TypeHandlerFactory getDefaultTypeHandlerFactory() {
        return defaultTypeHandlerFactory;
    }
    
    @Override
    protected boolean isOptionalFieldEnabled() {
        return true;
    }
    
    @Override
    protected boolean isOptionalBeanEnabled() {
        return true;
    }
    
    @Override
    protected void updateGroupDefinition(GroupDefinition parent, GroupConfig config, GroupDefinition definition) {
        
        int xmlType;
        String xmlTypeConfig = config.getXmlType();
        if (xmlTypeConfig == null) {
            xmlType = XmlDefinition.XML_TYPE_ELEMENT;
        }
        else if (XmlTypeConstants.XML_TYPE_ELEMENT.equals(xmlTypeConfig)) {
            xmlType = XmlDefinition.XML_TYPE_ELEMENT;
        }
        else if (XmlTypeConstants.XML_TYPE_NONE.equals(xmlTypeConfig)) {
            xmlType = XmlDefinition.XML_TYPE_NONE;
        }
        else {
            throw new BeanIOConfigurationException("Invalid xml type '" + xmlTypeConfig + "' for a group");
        }
        
        String xmlName = config.getXmlName();
        if (xmlName == null) {
            xmlName = config.getName();
        }
        
        // determine the XML namespace of the group...
        boolean xmlNamespaceAware;
        String xmlNamespace = config.getXmlNamespace();
        String xmlPrefix = config.getXmlPrefix();
        if (xmlNamespace == null) {
            // if a namespace was not configured, default to the parent group
            if (parent != null) {
                XmlDefinition parentXml = ((XmlGroupDefinition)parent).getXmlDefinition();
                xmlNamespace = parentXml.getNamespace();
                xmlPrefix = parentXml.getPrefix();
                xmlNamespaceAware = parentXml.isNamespaceAware();
            }
            // if there is no parent to default too, we assume it is not namespace aware
            else {
                xmlNamespace = null;
                xmlPrefix = null;
                xmlNamespaceAware = false;
            }
        }
        else if ("*".equals(xmlNamespace)) {
            xmlNamespace = null;
            xmlPrefix = null;
            xmlNamespaceAware = false;
        }
        else if ("".equals(xmlNamespace)) {
            xmlNamespace = null;
            xmlNamespaceAware = true;
        }
        else {
            xmlNamespaceAware = true;
        }
        
        if ("".equals(xmlPrefix)) {
            xmlPrefix = null;
        }
        
        XmlDefinition xml = ((XmlGroupDefinition)definition).getXmlDefinition();
        xml.setType(xmlType);
        xml.setName(xmlName);
        xml.setNamespace(xmlNamespace);
        xml.setNamespaceAware(xmlNamespaceAware);
        xml.setPrefix(xmlPrefix);
    }
    
    @Override
    protected void compileRecordDefinition(GroupDefinition group, RecordConfig config, RecordDefinition recordDefinition) {
        super.compileRecordDefinition(group, config, recordDefinition);
        
        XmlBeanDefinition xmlBeanDefinition = (XmlBeanDefinition) recordDefinition.getBeanDefinition();
        XmlDefinition xml = xmlBeanDefinition.getXmlDefinition();
        
        if (!updateXmlAttributes(config.getBean(), xmlBeanDefinition, "element")) {
            XmlDefinition parentXml = ((XmlGroupDefinition) group).getXmlDefinition();    
            xml.setNamespace(parentXml.getNamespace());
            xml.setNamespaceAware(parentXml.isNamespaceAware());
            xml.setPrefix(parentXml.getPrefix());
        }
        
        switch (xml.getType()) {
        case XmlDefinition.XML_TYPE_ELEMENT:
            break;
        default:
            throw new BeanIOConfigurationException("Invalid xml type '" + config.getBean().getXmlType() + "' for a record");
        }
    }

    @Override
    protected void updateBeanDefinition(BeanConfig beanConfig, BeanDefinition beanDefinition) {
        updateXmlAttributes(beanConfig, beanDefinition, "element");
        
        switch (((XmlBeanDefinition)beanDefinition).getXmlDefinition().getType()) {
        case XmlDefinition.XML_TYPE_ELEMENT:
        case XmlDefinition.XML_TYPE_NONE:
            break;
        default:
            throw new BeanIOConfigurationException("Invalid xml type '" + beanConfig.getXmlType() + "' for a bean");
        }
    }
    
    @Override
    protected void updateFieldDefinition(FieldConfig fieldConfig, FieldDefinition fieldDefinition) {
        updateXmlAttributes(fieldConfig, fieldDefinition, Settings.getInstance().getProperty(Settings.DEFAULT_XML_TYPE));
    }
    
    /**
     * Sets the XML type, name, namespace and nillable attributes on a property definition.
     * @param propertyConfig the property configuration
     * @param propertyDefinition the property definition
     * @return <tt>true</tt> if the XML namespace could be set, <tt>false</tt> otherwise
     */
    private boolean updateXmlAttributes(PropertyConfig propertyConfig, PropertyDefinition propertyDefinition, String defaultXmlType) {

        XmlDefinition xml = ((XmlNode) propertyDefinition).getXmlDefinition();
        
        String xmlTypeConfig = propertyConfig.getXmlType();
        if (xmlTypeConfig == null) {
            xmlTypeConfig = defaultXmlType;
        }
        
        int xmlType;
        if (XmlTypeConstants.XML_TYPE_ATTRIBUTE.equals(xmlTypeConfig)) {
            xmlType = XmlDefinition.XML_TYPE_ATTRIBUTE;
        }
        else if (XmlTypeConstants.XML_TYPE_ELEMENT.equals(xmlTypeConfig)) {
            xmlType = XmlDefinition.XML_TYPE_ELEMENT;
        }
        else if (XmlTypeConstants.XML_TYPE_NONE.equals(xmlTypeConfig)) {
            xmlType = XmlDefinition.XML_TYPE_NONE;
        }
        else if (XmlTypeConstants.XML_TYPE_TEXT.equals(xmlTypeConfig)) {
            xmlType = XmlDefinition.XML_TYPE_TEXT;
        }
        else {
            throw new BeanIOConfigurationException("Invalid XML type '" + propertyConfig.getXmlType() + "'");
        }
        xml.setType(xmlType);
        xml.setNillable(propertyConfig.isNillable());
        
        // property collections must be of type 'element'
        if (xml.getType() != XmlDefinition.XML_TYPE_ELEMENT && propertyDefinition.isCollection()) {
            if (propertyDefinition.isCollection()) {
                throw new BeanIOConfigurationException("Invalid XML type '" + 
                    xmlTypeConfig + "' for a collection");
            }
        }
        
        // if the bean/field/record is nillable, it must be of type 'element'
        if (xml.isNillable() && xml.getType() != XmlDefinition.XML_TYPE_ELEMENT)  {
            throw new BeanIOConfigurationException("Configured XML type is not nillable");
        }
        
        String xmlName = propertyConfig.getXmlName();
        if (xmlName == null) {
            xmlName = propertyConfig.getName();
        }
        xml.setName(xmlName);
        
        boolean xmlNamespaceSet = true;
        boolean xmlNamespaceAware = false;
        String xmlPrefix = propertyConfig.getXmlPrefix();
        String xmlNamespace = propertyConfig.getXmlNamespace();
        
        // namespace only allies to XML types attribute and element
        if (xmlNamespace != null) {
            if (xmlType != XmlDefinition.XML_TYPE_ATTRIBUTE && xmlType != XmlDefinition.XML_TYPE_ELEMENT) {
                throw new BeanIOConfigurationException("XML namespace is not applicable to XML type '" +
                    xmlTypeConfig + "'");
            }
        }
        if (xmlPrefix != null && xmlNamespace == null) {
            throw new BeanIOConfigurationException("Missing XML namespace for configured XML prefix");
        }
        
        if (xmlNamespace == null) {
            XmlBeanDefinition parent = (XmlBeanDefinition)propertyDefinition.getParent();
            if (parent == null) {
                xmlNamespaceSet = false;
            }
            else {
                xmlNamespace = parent.getXmlDefinition().getNamespace();
                xmlNamespaceAware = parent.getXmlDefinition().isNamespaceAware();        
                xmlPrefix = parent.getXmlDefinition().getPrefix();
            }
        }
        else if ("*".equals(xmlNamespace)) {
            xmlNamespaceAware = false;
            xmlNamespace = null;
            xmlPrefix = null;
        }
        else {
            xmlNamespaceAware = true;
            if ("".equals(xmlNamespace)) {
                xmlNamespace = null;
            }
        }
        
        if (xmlNamespaceSet) {
            xml.setNamespace(xmlNamespace);
            xml.setNamespaceAware(xmlNamespaceAware);
            if ("".equals(xmlPrefix)) {
                xmlPrefix = null;
            }
            xml.setPrefix(xmlPrefix);
        }
        
        return xmlNamespaceSet;
    }
    
    @Override
    protected GroupDefinition newGroupDefinition(GroupConfig group) {
        return new XmlGroupDefinition();
    }

    @Override
    protected FieldDefinition newFieldDefinition(FieldConfig field) {
        return new XmlFieldDefinition();
    }

    @Override
    protected BeanDefinition newBeanDefinition(BeanConfig bean) {
        return new XmlBeanDefinition();
    }

    @Override
    protected RecordDefinition newRecordDefinition(RecordConfig record) {
        return new XmlRecordDefinition();
    }

    @Override
    protected StreamDefinition newStreamDefinition(StreamConfig stream) {
        return new XmlStreamDefinition();
    }

    @Override
    protected RecordReaderFactory newRecordReaderFactory() {
        return new XmlReaderFactory();
    }

    @Override
    protected RecordWriterFactory newRecordWriterFactory() {
        return new XmlWriterFactory();
    }
}
