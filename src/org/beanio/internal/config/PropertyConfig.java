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
package org.beanio.internal.config;

/**
 * A base class for configuration components that can be bound to a property
 * of a bean object. 
 *
 * <p>The following attributes are set during compilation, and are meant for 
 * internal use only:
 * <ul>
 * <li>minSize</li>
 * <li>maxSize</li>
 * </ul>
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public abstract class PropertyConfig extends ComponentConfig {
        
    private String type;
    private String getter;
    private String setter;
    private boolean bound;
    private boolean identifier;
    
    private Integer position;
    private Integer minOccurs;
    private Integer maxOccurs;
    private String collection;
    
    /* attributes specific to xml */
    private String xmlType;
    private boolean nillable;
    
    /* derived attributes */
    private int minSize;
    private int maxSize;
    
    /**
     * Constructs a new <tt>PropertyConfig</tt>.
     */
    public PropertyConfig() { }

    /**
     * Returns the fully qualified class name or type alias of this property.
     * By default, <tt>null</tt> is returned and the property value type
     * is detected through bean introspection.
     * @return the class name of this property value
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the fully qualified class name or type alias of this property.
     * If set to <tt>null</tt>, the property value type will be detected 
     * through bean introspection if possible.
     * @param type the class name of this property value
     */
    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Returns the name of the getter method for retrieving this property's
     * value from its parent bean object during marshalling.
     * @return the getter method for this property
     */
    public String getGetter() {
        return getter;
    }

    /**
     * Sets the name of the getter method for retrieving this property's
     * value from it parent bean object during marshalling.  If <tt>null</tt>,
     * the getter method may be discovered through bean introspection if possible.
     * @param getter the getter method for this property
     */
    public void setGetter(String getter) {
        this.getter = getter;
    }

    /**
     * Returns the name of the setter method to use when setting this property's
     * value on its parent bean object during unmarshalling.
     * @return the setter method for this property
     */
    public String getSetter() {
        return setter;
    }

    /**
     * Sets the name of the setter method to use when setting this property's
     * value on its parent bean object during unmarshalling.  If <tt>null</tt>,
     * the setter method may be discovered through bean introspection if possible.
     * @param setter the setter method for this property
     */
    public void setSetter(String setter) {
        this.setter = setter;
    }

    /**
     * Returns whether this property is bound to its parent bean object.
     * @return true if bound, false otherwise
     */
    public boolean isBound() {
        return bound;
    }

    /**
     * Sets whether this property is bound to its parent bean object.
     * @param bound true if bound, false otherwise
     */
    public void setBound(boolean bound) {
        this.bound = bound;
    }
    
    /**
     * Returns the position of this component.  For delimited record formats,
     * the position is the index (beginning at 0) of this component in the 
     * record.  For fixed length record formats, the position is the index
     * of the first character in the component.
     * @return the field position
     */
    public Integer getPosition() {
        return position;
    }

    /**
     * Sets the position of this component.  For delimited record formats,
     * the position is the index (beginning at 0) of this component in the 
     * record.  For fixed length record formats, the position is the index
     * of the first character in the component.
     * @param position the field position
     */
    public void setPosition(Integer position) {
        this.position = position;
    }
    
    /**
     * Returns the collection type, or <tt>null</tt> if this component
     * is not bound to a collection or array. 
     * @return the collection type
     */
    public String getCollection() {
        return collection;
    }

    /**
     * Sets the collection type.  Set to <tt>null</tt> (default) to indicate
     * this component is not bound to a collection or array.  The value may be set to the 
     * fully qualified class name of a <tt>java.util.Collection</tt> subclass or a 
     * collection type alias, or the value "<tt>array</tt>" to indicate a Java array.
     * @param collection the collection type
     */
    public void setCollection(String collection) {
        this.collection = collection;
    }

    /**
     * Returns the minimum number of times this component must appear in the stream.
     * @return the minimum occurrences of this component
     */
    public Integer getMinOccurs() {
        return minOccurs;
    }

    /**
     * Sets the minimum number of times this component must consecutively appear in a
     * stream.  If set to any value greater than one, a collection type is expected.  
     * Must be 0 or greater.
     * @param minOccurs the minimum occurrences of this component
     */
    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    /**
     * Returns the maximum number of times this component may consecutively appear in
     * a stream.  If <tt>null</tt>, one occurrence is assumed.
     * @return the maximum occurrences of this component, or <tt>-1</tt> to 
     *   indicate no limit
     */
    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    /**
     * Sets the maximum number of times this component may consecutively appear in
     * a stream.  If set to <tt>null</tt>, one occurrence is assumed.  If set to
     * any value greater than one, a collection type is expected.  Must be greater
     * than the minimum occurrences, or set to <tt>-1</tt> to indicate the limit is
     * unbounded.
     * @param maxOccurs the maximum occurrences of this component, or <tt>-1</tt> to
     *   indicate no limit
     */
    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }
    
    /**
     * Returns the XML node type of this component.
     * @return the XML node type
     * @see XmlTypeConstants
     */
    public String getXmlType() {
        return xmlType;
    }

    /**
     * Sets the XML node type of this component.
     * @param xmlType the XML node type
     * @see XmlTypeConstants
     */
    public void setXmlType(String xmlType) {
        this.xmlType = xmlType;
    }
    
    /**
     * Returns whether this component is nillable.
     * @return <tt>true</tt> if this component is nillable
     */
    public boolean isNillable() {
        return nillable;
    }

    /**
     * Sets whether this component is nillable.
     * @param nillable <tt>true</tt> if this component is nillable
     */
    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }
    
    /**
     * Returns whether this component is used to identify a record during
     * unmarshalling or a bean during marshalling.  If this component is
     * a record or segment, true is returned if any descendent is used for
     * identification.
     * @return <tt>true</tt> if this component is used for identification
     */
    public boolean isIdentifier() {
        return identifier;
    }

    /**
     * Sets whether this component is used to identify a reocrd during
     * unmarshalling or a bean during marshalling.
     * @param b <tt>true</tt> if this component is used for identification
     */
    public void setIdentifier(boolean b) {
        this.identifier = b;
    }

    /**
     * Returns the minimum size of this component (based on its field length
     * or the field length of its descendants).
     * @return the minimum size of this component
     */
    public int getMinSize() {
        return minSize;
    }

    /**
     * Sets the minimum size of this component (based on its field length
     * or the field length of its descendants).
     * @param minSize the minimum size of this component
     */
    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }
    
    /**
     * Returns the maximum size of one occurrence of this component (based on its field length
     * or the field length of its descendants).
     * @return the maximum size of this component
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Sets the maximum size of one occurrence of this component (based on its field length
     * or the field length of its descendants).
     * @param maxSize the maximum size of this component
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
 
    /**
     * Returns whether this component may optionally exist in the stream
     * (based on the configured minimum occurrences).
     * @return true if this component is optional
     */
    public boolean isLazy() {
        return minOccurs != null && minOccurs == 0;
    }
    
    /**
     * Returns whether this component is bound to a collection or array.
     * @return true if this component is bound to a collection or array
     */
    public boolean isCollection() {
        return collection != null;
    }
    
    /**
     * Returns whether this component repeats in a stream.  The component
     * is assumed to repeat if bound to a collection or the maximum
     * occurrences is greater than one.
     * @return true if this component repeats
     */
    public boolean isRepeating() {
        return isCollection() || (maxOccurs != null && maxOccurs > 1);
    }
    
    @Override
    protected boolean isSupportedChild(ComponentConfig child) {
        return false;
    }
}
