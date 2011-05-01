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
package org.beanio.parser.xml;

import java.lang.reflect.Array;
import java.util.*;

import javax.xml.XMLConstants;

import org.beanio.InvalidRecordException;
import org.beanio.parser.*;
import org.beanio.stream.xml.XmlWriter;
import org.beanio.util.DomUtil;
import org.w3c.dom.*;
import org.w3c.dom.Node;

/**
 * A bean definition implementation for XML formatted streams.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlBeanDefinition extends BeanDefinition implements XmlNode {

    private XmlDefinition xml = new XmlDefinition();

    @Override
    public boolean matches(Record record) {
        // if the bean xml type is 'none', there are no changes to the matching logic,
        // otherwise we need to check the DOM for a matching bean element
        if (xml.getType() == XmlDefinition.XML_TYPE_NONE) {
            return super.matches(record);
        }
        
        boolean isRecord = getParent() == null;
        
        // validate the current element in the document matches this record
        XmlRecord domRecord = (XmlRecord) record;
        Element parent = (Element) domRecord.getPosition();
        
        Element element = parent;
        if (element == null) {
            element = domRecord.getDocument().getDocumentElement();
        }

        // find a matching child element for this bean
        Element match = XmlNodeUtil.findChild(element, xml, 0);
        if (match == null) {
            if (isRecord) {
                // if a child element is not found and this is the record level bean, 
                // the stream node is not matched
                return false;
            }
            else {
                // if a child element is not found and it is not required,
                // the stream node is matched
                return getMinOccurs() == 0;
            }
        }
        
        // update the DOM position to the matched XML node
        domRecord.setPosition(match);
        
        // allow node values to be matched if necessary
        boolean matched = super.matches(record);
        
        // reset the position if no match or this bean is not the record level bean node 
        if (!matched || !isRecord) {
            domRecord.setPosition(parent);
        }
        
        return matched;
    }

    @Override
    protected Object parsePropertyValue(Record record) throws InvalidRecordException {
        // if this is the record level bean, the parent element is already
        // set on the record and validated
        if (getParent() == null) {
            return super.parsePropertyValue(record);
        }
        // if this bean does not have an XML counterpart, nothing to parse
        else if (xml.getType() == XmlDefinition.XML_TYPE_NONE) {
            return super.parsePropertyValue(record);
        }
        // otherwise we need to position the DOM record before parsing our children
        else {
            XmlRecord domRecord = (XmlRecord) record;
            
            Node parent = domRecord.getPosition();
            Element element;
            
            // position the field
            if (isCollection()) {
                if (record.getFieldOffset() > 0) {
                    element = XmlNodeUtil.findSibling(domRecord.getPreviousElement(), xml);
                }
                else {
                    element = XmlNodeUtil.findChild(parent, xml, 0);
                }
                domRecord.setPreviousElement(element);
            }
            else {
                element = XmlNodeUtil.findChild(parent, xml, 0);
            }
            
            if (element == null) {
                return MISSING;
            }
            
            if (xml.isNillable() && XmlNodeUtil.isNil(element)) {
                return null;
            }
            
            domRecord.setPosition(element);
            try {
                return super.parsePropertyValue(record);
            }
            finally {
                domRecord.setPosition(parent);
            }
        }
    }
    
    @Override
    protected boolean isBeanExistenceKnown() {
        return true;
    }
    
    @Override
    public Object formatRecord(Object bean) {
        return formatRecord(DomUtil.newDocument(), bean);
    }
    
    /**
     * Formats a bean object into a XML DOM tree, with support for collection type beans.
     * @param parent the parent DOM element to append
     * @param bean the bean object
     */
    public Object formatRecord(Node parent, Object bean) {
        if (bean != null) {
            formatBean(parent, bean);
        }
        return parent;
    }
    

    @SuppressWarnings("unchecked")
    private void formatBean(Node parent, Object bean) {
        if (isCollection()) {
            if (bean == null) {
                for (int i=0; i<getMinOccurs(); i++) {
                    formatProperty(parent, (Object)null);
                }
            }
            else if (isArray()) {
                for (int i=0, j=Array.getLength(bean); i<j; i++) {
                    Object value = Array.get(bean, i);
                    formatProperty(parent, value);
                }
            }
            else {
                for (Object obj : (Collection<Object>)bean) {
                    formatProperty(parent, obj);
                }
            }
        }
        else {
            formatProperty(parent, bean);
        }
    }
    
    /**
     * Formats a bean object into a XML DOM tree.
     * @param parent the parent DOM element to append
     * @param value the bean object
     */
    private void formatProperty(Node parent, Object bean) {
        
        if (xml.getType() == XmlDefinition.XML_TYPE_NONE) {
            if (bean == null) {
                return;
            }
        }
        else if (xml.getType() == XmlDefinition.XML_TYPE_ELEMENT) {
            Document document;
            if (parent.getNodeType() == Node.DOCUMENT_NODE) {
                document = (Document) parent;
            }
            else {
                document = parent.getOwnerDocument();
            }
            
            // if the bean is null and minOccurs="0", no element is added to the DOM tree
            if (bean == null && getMinOccurs() == 0) {
                return;
            }
            
            // create an element for the bean
            Element element = document.createElementNS(xml.getNamespace(), xml.getName());
            if (!xml.isNamespaceAware()) {
                element.setUserData(XmlWriter.IS_NAMESPACE_IGNORED, Boolean.TRUE, null);
            }
            else {
                element.setPrefix(xml.getPrefix());
            }
            parent.appendChild(element);
            
            // if the bean is null and nillable="true", add a 'nil' attribute to the bean element
            if (bean == null && xml.isNillable()) {
                element.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil", "true");
            }
            
            if (bean == null) {
                return;
            }
            
            parent = element;
        }
        
        List<PropertyDefinition> fieldList = getPropertyList();
        for (PropertyDefinition property : fieldList) {
            final Object value = bean != null ?  getBeanProperty(property, bean) : null;

            if (property.isBean()) {
                ((XmlBeanDefinition)property).formatBean(parent, value);
            }
            else {
                addField((XmlFieldDefinition) property, parent, value);
            }
        }
    }
    
    /**
     * Sets a field value on it parent DOM element, with support for field collections.
     * @param field the field definition
     * @param parent the parent DOM element
     * @param value the field value
     */
    @SuppressWarnings("unchecked")
    private void addField(XmlFieldDefinition field, Node parent, Object value) {
        if (field.isCollection()) {
            if (value == null) {
                for (int i=0; i<field.getMinOccurs(); i++) {
                    addFieldValue(field, parent, null);
                }
            }
            else if (field.isArray()) {
                for (int i=0, j=Array.getLength(value); i<j; i++) {
                    addFieldValue(field, parent, Array.get(value, i));
                }
            }
            else {
                for (Object obj : (Collection<Object>)value) {
                    addFieldValue(field, parent, obj);
                }
            }
        }
        else {
            addFieldValue(field, parent, value);
        }
    }
    
    /**
     * Sets a field value on it parent DOM element.
     * @param field the field definition
     * @param parent the parent DOM element
     * @param value the field value
     */
    private void addFieldValue(XmlFieldDefinition field, Node parent, Object value) {
        XmlDefinition fieldXml = field.getXmlDefinition();
        
        int type = fieldXml.getType();
        if (type == XmlDefinition.XML_TYPE_ATTRIBUTE) {
            
            if (parent.getNodeType() == Node.ELEMENT_NODE && value != null) {
                Attr att = parent.getOwnerDocument().createAttributeNS(fieldXml.getNamespace(), fieldXml.getName());
                att.setValue(field.formatValue(value));
                att.setPrefix(fieldXml.getPrefix());
                ((Element)parent).setAttributeNode(att);
            }
            
        }
        else if (type == XmlDefinition.XML_TYPE_ELEMENT) {
            // null values are formatted to empty strings
            String text = field.formatValue(value);
            
            if (value == null && field.getMinOccurs() == 0) {
                return;
            }
            
            Element element = parent.getOwnerDocument().createElementNS(fieldXml.getNamespace(), fieldXml.getName());
            if (!fieldXml.isNamespaceAware()) {
                element.setUserData(XmlWriter.IS_NAMESPACE_IGNORED, Boolean.TRUE, null);
            }
            else {
                element.setPrefix(fieldXml.getPrefix());
            }
            parent.appendChild(element);
            
            if (value == null && fieldXml.isNillable()) {
                element.setAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil", "true");
            }
            else if (text != null && !"".equals(text)) {
                element.appendChild(parent.getOwnerDocument().createTextNode(text));
            }
        }
        else if (type == XmlDefinition.XML_TYPE_TEXT) {
            
            if (value != null) {
                parent.setTextContent(value.toString());
            }
            
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser.xml.XmlNode#getXmlDefinition()
     */
    public XmlDefinition getXmlDefinition() {
        return xml;
    }
}
