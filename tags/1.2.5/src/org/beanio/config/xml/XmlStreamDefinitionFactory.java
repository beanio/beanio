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
        defaultTypeHandlerFactory.registerHandlerFor(Boolean.class, new XmlBooleanTypeHandler());
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
            throw new BeanIOConfigurationException("Invalid xmlType '" + xmlTypeConfig + "'");
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
    }

    @Override
    protected void updateBeanDefinition(BeanConfig beanConfig, BeanDefinition beanDefinition) {
        updateXmlAttributes(beanConfig, beanDefinition, "element");
        
        XmlDefinition xml = ((XmlBeanDefinition)beanDefinition).getXmlDefinition();
        
        switch (xml.getType()) {
        case XmlDefinition.XML_TYPE_ELEMENT:
        case XmlDefinition.XML_TYPE_NONE:
            break;
        default:
            throw new BeanIOConfigurationException("Invalid xmlType '" + beanConfig.getXmlType() + "'");
        }
        
        // default minOccurs is 0 for non-nillable beans in an XML stream
        if (beanConfig.getMinOccurs() == null && !xml.isNillable()) {
            beanDefinition.setMinOccurs(0);
        }
    }
    
    @Override
    protected void updateFieldDefinition(FieldConfig fieldConfig, FieldDefinition fieldDefinition) {
        updateXmlAttributes(fieldConfig, fieldDefinition, Settings.getInstance().getProperty(Settings.DEFAULT_XML_TYPE));
        
        // default minOccurs is 0 for fields in an XML stream
        XmlDefinition xml = ((XmlFieldDefinition)fieldDefinition).getXmlDefinition();
        if (fieldConfig.getMinOccurs() == null && !xml.isNillable()) {
            fieldDefinition.setMinOccurs(0);
        }
        
        if (fieldConfig.getPadding() != null) {
            fieldDefinition.setPadded(true);
            fieldDefinition.setPadding(fieldConfig.getPadding());
            fieldDefinition.setPaddedLength(fieldConfig.getLength());
            fieldDefinition.setJustification(FieldConfig.RIGHT.equals(fieldConfig.getJustify()) ?
                FieldDefinition.RIGHT : FieldDefinition.LEFT);
        }
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
            
            Integer minOccurs = propertyConfig.getMinOccurs();
            if (minOccurs != null && minOccurs > 0) {
                throw new BeanIOConfigurationException("minOccurs must be 0 for xmlType 'none'");
            } 
            propertyDefinition.setMinOccurs(0);
        }
        else if (XmlTypeConstants.XML_TYPE_TEXT.equals(xmlTypeConfig)) {
            xmlType = XmlDefinition.XML_TYPE_TEXT;
        }
        else {
            throw new BeanIOConfigurationException("Invalid xmlType '" + propertyConfig.getXmlType() + "'");
        }
        xml.setType(xmlType);
        xml.setNillable(propertyConfig.isNillable());
        
        // property collections must be of type 'element'
        if (xml.getType() != XmlDefinition.XML_TYPE_ELEMENT && propertyDefinition.isCollection()) {
            if (propertyDefinition.isCollection()) {
                throw new BeanIOConfigurationException("Collection type bean/field must have xmlType 'element'");
            }
        }
        
        // if the bean/field/record is nillable, it must be of type 'element'
        if (xml.isNillable() && xml.getType() != XmlDefinition.XML_TYPE_ELEMENT)  {
            throw new BeanIOConfigurationException("xmlType '" + xmlTypeConfig + "' is not nillable");
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
            // allow beans where xmlType='none' to still apply an XML namespace to its children
            if (!propertyConfig.isBean()) {
                if (xmlType != XmlDefinition.XML_TYPE_ATTRIBUTE && xmlType != XmlDefinition.XML_TYPE_ELEMENT) {
                    throw new BeanIOConfigurationException("XML namespace is not applicable for xmlType '" +
                        xmlTypeConfig + "'");
                }
            }
        }
        if (xmlPrefix != null && xmlNamespace == null) {
            throw new BeanIOConfigurationException("Missing namespace for configured XML prefix");
        }
        
        if (xmlNamespace == null) {
            XmlBeanDefinition parent = (XmlBeanDefinition)propertyDefinition.getParent();
            // parent is null for record elements
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
            xml.setPrefix(xmlPrefix);
            
            String wrapperName = propertyConfig.getXmlWrapper();
            if (wrapperName != null) {
                XmlDefinition wrapper = new XmlDefinition();
                wrapper.setName(wrapperName);
                wrapper.setType(XmlDefinition.XML_TYPE_ELEMENT);
                wrapper.setNamespace(xml.getNamespace());
                wrapper.setPrefix(xml.getPrefix());
                wrapper.setNillable(xml.isNillable());
                wrapper.setNamespaceAware(xml.isNamespaceAware());
                xml.setWrapper(wrapper);
            }
        }
        
        return xmlNamespaceSet;
    }
    
    /**
     * An XML record identifying field of type element or attribute does not need a literal
     * or regular expression configured to identify the record (since the presence of the named
     * field may sufficiently identify the record).
     */
    @Override
    protected void validateRecordIdentifyingCriteria(FieldDefinition fieldDefinition) {
        if (((XmlFieldDefinition)fieldDefinition).getXmlDefinition().getType() == XmlDefinition.XML_TYPE_TEXT) {
            super.validateRecordIdentifyingCriteria(fieldDefinition);
        }
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
