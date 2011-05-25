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
package org.beanio.config;

/**
 * Stores configuration settings for a property of a Java bean.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class PropertyConfig {

    /** Left justification setting */
    public static final String LEFT = "left";
    /** Right justification setting */
    public static final String RIGHT = "right";
    
    private String name;
    private String type;
    private String getter;
    private String setter;
    private boolean ignored = false;
    
    private String collection;
    private Integer minOccurs;
    private Integer maxOccurs;
    
    private String xmlType;
    private String xmlName;
    private String xmlNamespace;
    private String xmlPrefix;
    private boolean nillable;
    private String xmlWrapper;
    
    /**
     * Returns whether this property defines a bean class with its own list
     * of properties.
     * @return whether this property defines a Java bean
     */
    public boolean isBean() {
        return false;
    }
    
    /**
     * Returns the name of this property.  The name of the property is
     * used to get and set the property value from its enclosing bean
     * when a <tt>getter</tt> and <tt>setter</tt> are not set.
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this property.  The name of the property is
     * used to get and set the property value from its enclosing bean
     * when a <tt>getter</tt> and <tt>setter</tt> are not set.
     * @param name the field name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns <tt>true</tt> if this property is not a bean property.
     * @return <tt>true</tt> if this property is not a bean property.
     */
    public boolean isIgnored() {
        return ignored;
    }

    /**
     * Set to <tt>true</tt> if this property is not a bean property.
     * @param ignore <tt>true</tt> if this property is not a bean property.
     */
    public void setIgnored(boolean ignore) {
        this.ignored = ignore;
    }

    /**
     * Returns the name of the getter method to use to retrieve this property's
     * value from its parent bean when writing to an output stream.
     * @return the getter method for this property
     */
    public String getGetter() {
        return getter;
    }

    /**
     * Sets the name of the getter method to use to retrieve this property's
     * value from it parent bean when writing to an output stream.
     * @param getter the getter method for this property
     */
    public void setGetter(String getter) {
        this.getter = getter;
    }

    /**
     * Returns the name of the setter method to use when setting this property's
     * value on its parent bean while reading from an input stream.
     * @return the setter method for this property
     */
    public String getSetter() {
        return setter;
    }

    /**
     * Sets the name of the setter method to use when setting this property's
     * value on its parent bean while reading from an input stream.
     * @param setter the setter method for this property
     */
    public void setSetter(String setter) {
        this.setter = setter;
    }

    /**
     * Returns the fully qualified class name or type alias of this property's
     * value.  By default, <tt>null</tt> is returned and the property value type
     * is detected through bean introspection.
     * @return the class name of this property value
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the fully qualified class name or type alias of this property's
     * value.  If set to <tt>null</tt>, the property value type is detected 
     * through bean introspection.
     * @param type the class name of this property value
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the collection type, or <tt>null</tt> if this property
     * is not a collection or array. 
     * @return the property collection type
     */
    public String getCollection() {
        return collection;
    }

    /**
     * Sets the collection type.  Set to <tt>null</tt> (default) to indicate
     * the property is not a collection or array.  The value may be set to the 
     * fully qualified class name of a <tt>java.util.Collection</tt> subclass or a 
     * collection type alias, or the value "array" to indicate a Java array.
     * @param collection the collection type
     */
    public void setCollection(String collection) {
        this.collection = collection;
    }

    /**
     * Returns the minimum number of times this property must appear in an
     * input stream.  If <tt>null</tt>, one occurrence is assumed.
     * @return the minimum required occurrences of this property
     */
    public Integer getMinOccurs() {
        return minOccurs;
    }

    /**
     * Sets the minimum number of times this property must appear consecutively in an 
     * input stream.  If set to <tt>null</tt>, one occurrence is assumed.  If set to anything
     * other than one, a collection type is expected.  Must be 0 or greater.
     * @param minOccurs the minimum required occurrences of this property
     */
    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    /**
     * Returns the maximum number of times this property may appear (consecutively) in
     * an input stream.  If <tt>null</tt>, one occurrence is assumed.
     * @return the maximum allowed occurrences of this property, or <tt>-1</tt> if there
     *   is no limit
     */
    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    /**
     * Sets the maximum number of times this property may appear (consecutively) in
     * an input stream.  If set to <tt>null</tt>, one occurrence is assumed.  If set to
     * anything other than one, a collection type is expected.  Must be greater
     * than the minimum occurrences, or set to <tt>-1</tt> to indicate the limit is
     * unbounded.
     * @param maxOccurs the maximum allowed occurrences of this property, or <tt>-1</tt> if
     *   there is no limit
     */
    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }
    
    /**
     * Returns the XML node type of this property.
     * @return the XML node type
     * @since 1.1
     * @see XmlTypeConstants
     */
    public String getXmlType() {
        return xmlType;
    }

    /**
     * Sets the XML node type of this property.
     * @param xmlType the XML node type
     * @since 1.1
     * @see XmlTypeConstants
     */
    public void setXmlType(String xmlType) {
        this.xmlType = xmlType;
    }

    /**
     * Returns XML element or attribute name of this property.
     * @return the XML element or attribute name
     * @since 1.1
     */
    public String getXmlName() {
        return xmlName;
    }

    /**
     * Sets the XML element or attribute name of this property.  If set to <tt>null</tt> 
     * (default), the XML name defaults to the property name.
     * @param xmlName the XML element or attribute name
     * @since 1.1
     */
    public void setXmlName(String xmlName) {
        this.xmlName = xmlName;
    }

    /**
     * Returns the XML namespace for this property element or attribute.
     * @return the XML namespace
     * @since 1.1
     */
    public String getXmlNamespace() {
        return xmlNamespace;
    }

    /**
     * Sets the XML namespace for this property element or attribute.  If set to <tt>null</tt>
     * (default), the namespace is inherited from its parent.
     * @param xmlNamespace the XML namespace
     * @since 1.1
     */
    public void setXmlNamespace(String xmlNamespace) {
        this.xmlNamespace = xmlNamespace;
    }

    /**
     * Returns the XML prefix for the namespace assigned to this property element or attribute.
     * @return the XML namespace prefix
     * @since 1.1
     */
    public String getXmlPrefix() {
        return xmlPrefix;
    }

    /**
     * Sets the XML prefix for the namespace assigned to this property element or attribute.  If 
     * set to <tt>null</tt> and a namespace is set, the namespace will replace the default namespace
     * when marshaling the property.  If a namespace is not set, the prefix is ignored.
     * @param xmlPrefix the XML namespace prefix
     * @since 1.1
     */
    public void setXmlPrefix(String xmlPrefix) {
        this.xmlPrefix = xmlPrefix;
    }

    /**
     * Returns whether this property is nillable.
     * @return <tt>true</tt> if this property is nillable
     * @since 1.1
     */
    public boolean isNillable() {
        return nillable;
    }

    /**
     * Sets whether this property is nillable.
     * @param nillable <tt>true</tt> if this property is nillable
     * @since 1.1
     */
    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }

    /**
     * Returns the name of a XML wrapper element for this property.
     * @return the XML wrapper element name or <tt>null</tt> if this property
     *   is not wrapped by another element
     * @since 1.1
     */
    public String getXmlWrapper() {
        return xmlWrapper;
    }

    /**
     * Sets the name of a XML wrapper element for this property.
     * @param xmlWrapper the XML wrapper element name or <tt>null</tt> if 
     *   this property is not wrapped by another element
     * @since 1.1
     */
    public void setXmlWrapper(String xmlWrapper) {
        this.xmlWrapper = xmlWrapper;
    }
}
