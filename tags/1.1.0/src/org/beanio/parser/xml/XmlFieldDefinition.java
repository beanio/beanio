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

import org.beanio.parser.*;
import org.w3c.dom.*;
import org.w3c.dom.Node;

/**
 * Field definition implementation for XML formatted streams.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlFieldDefinition extends FieldDefinition implements XmlNode {

    private static final String NIL = new String("nil");

    private XmlDefinition xml = new XmlDefinition();
    
    /**
     * Constructs a new <tt>XmlFieldDefinition</tt>.
     */
    public XmlFieldDefinition() { }
    
    @Override
    public boolean matches(Record record) {
        if (!isRecordIdentifier())
            return true;
        
        // ignore record identifiers where the xml type is 'none' which are used for
        // writing XML only
        if (xml.getType() == XmlDefinition.XML_TYPE_NONE)
            return true;
       
        // can't call getFieldText() here which depends on the parent being parsed, 
        // but don't have to worry about collection types
        
        XmlRecord domRecord = (XmlRecord) record;
        
        Element parent = (Element) domRecord.getPosition();
        if (parent == null) {
            return false;
        }
        
        String fieldText = null;
        if (xml.getType() == XmlDefinition.XML_TYPE_ATTRIBUTE) {
            fieldText = XmlNodeUtil.getAttribute(parent, xml);
        }
        else if (xml.getType() == XmlDefinition.XML_TYPE_ELEMENT) {
            Element node = XmlNodeUtil.findChild(parent, xml, 0);
            if (node == null) {
                return false;
            }
            fieldText = XmlNodeUtil.getText(node);
        }
        else if (xml.getType() == XmlDefinition.XML_TYPE_TEXT) {
            fieldText = XmlNodeUtil.getText(parent, xml);
        }
        
        return isMatch(fieldText);
    }

    @Override
    protected String parseField(Record record) {
        String text = getFieldText(record);
        // update the record with the raw field text
        record.setFieldText(getName(), text == NIL ? null : text);
        return text;
    }
    
    /**
     * Returns the text for this field from a record.  Returns {@link #NIL}
     * if the XSI nil attribute was set to true, or <tt>null</tt> if the field
     * was not present, or an empty string if the field was present but there
     * was no value (e.g. &lt;field/&gt; or &lt;field&gt;&lt;/field&gt;)
     * @param record the record to parse
     * @return the field text
     */
    private String getFieldText(Record record) {
        XmlRecord rec = ((XmlRecord)record);
        
        Node n = rec.getPosition();
        if (n == null) {
            return null;
        }
        if (n.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        Element parent = (Element) n;
        
        if (xml.getType() == XmlDefinition.XML_TYPE_ATTRIBUTE) {
            return XmlNodeUtil.getAttribute((Element)parent, xml);
        }
        else if (xml.getType() == XmlDefinition.XML_TYPE_ELEMENT) {
            Element node = null;
            if (isCollection()) {
                int offset = rec.getFieldOffset();
                
                if (offset > 0) {
                    node = XmlNodeUtil.findSibling(rec.getPreviousElement(), xml);
                }
                else {
                    // special nil validation for wrapped collections
                    if (xml.isWrapped()) {
                        Element temp = XmlNodeUtil.unwrap(parent, xml);
                        if (temp != null && XmlNodeUtil.isNil(temp)) {
                            return NIL;
                        }
                    }
                    
                    node = XmlNodeUtil.findChild(parent, xml, offset);
                }
                
                if (node == null) {
                    return null;
                }

                rec.setPreviousElement(node);
            }
            else {
                node = XmlNodeUtil.findChild(parent, xml, 0);
                if (node == null) {
                    return null;
                }
            }

            if (XmlNodeUtil.isNil(node)) {
                return NIL;
            }
            
            String fieldText = XmlNodeUtil.getText(node);
            if (fieldText == null) {
                fieldText = "";
            }
            return fieldText;
        }
        else if (xml.getType() == XmlDefinition.XML_TYPE_TEXT) {
            parent = XmlNodeUtil.unwrap((Element)parent, xml);
            if (parent == null) {
                return null;
            }

            if (XmlNodeUtil.isNil(parent)) {
                return NIL;
            }
            
            String fieldText = XmlNodeUtil.getText(parent);
            if (fieldText == null) {
                fieldText = "";
            }
            return fieldText;
        }
        else {
            return null;
        }
    }
    
    @Override
    protected Object parsePropertyValue(Record record, String fieldText) {
        if (fieldText == NIL) {            
            // validate field is nillable
            if (!xml.isNillable()) {
                record.addFieldError(getName(), null, "nillable");
                return INVALID;
            }
            
            // collections are not further validated
            if (isCollection()) {
                return MISSING;
            }
            // validate field is not required
            else if (isRequired()) {
                record.addFieldError(getName(), null, "required");
                return INVALID;
            }
            // return the default value if set
            else if (getDefaultValue() != null) {
                return getDefaultValue();
            }
            else  {
                return null;
            }
        }
        else {
            if (fieldText == null && !isCollection() && getMinOccurs() > 0) {
                record.addFieldError(getName(), null, "minOccurs", getMinOccurs(), getMaxOccurs());
                return INVALID;
            }
            
            return super.parsePropertyValue(record, fieldText);
        }
    }
    
    /*
     * Overridden to return null when 'text' is null and the field is not padded.
     */
    @Override
    protected String formatText(String text) {
        if (text == null && getPaddedLength() <= 0) {
            return null;
        }
        else {
            return super.formatText(text);
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
