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
package org.beanio.parser;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;

import org.beanio.*;
import org.beanio.util.TypeUtil;

/**
 * Base class for bean property settings.  A property could be a simple field or a
 * bean containing a list of its own properties.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class PropertyDefinition {

    /** Constant indicating the field did not pass validation. */
    protected static final String INVALID = new String("invalid");
    /** Constant indicating the  field was not present in the stream */
    protected static final String MISSING = new String("missing");
    
    private String name;
    private boolean property = false;
    private boolean recordIdentifier = false;
    private Class<?> propertyType;
    private PropertyDescriptor propertyDescriptor;
    private int length;
    private boolean lazy = true;
    private PropertyDefinition parent = null;
    
    /* collection support */
    private Class<? extends Collection<Object>> collectionType;
    private int minOccurs = 1;
    private int maxOccurs = 1;  // -1 for unbounded
      
    /**
     * Constructs a new <tt>PropertyDefinition</tt>.
     */
    public PropertyDefinition() { }
    
    /**
     * Returns whether this property describes a complex bean object that
     * holds its own list of properties.
     * @return <tt>true</tt> if this property describes a bean object
     */
    public boolean isBean() {
        return false;
    }
    
    /**
     * Returns whether this property describes a field in the mapped stream.
     * @return <tt>true</tt> if this property describes a field in the mapped stream 
     * @since 1.2
     */
    public boolean isField() {
        return false;
    }
    
    /**
     * Tests if the field text in the record matches this field definition.
     * @param record the record containing the field to test
     * @return <tt>true</tt> if the text is a match
     */
    public abstract boolean matches(Record record);

    /**
     * Tests whether this definition is used to map the given bean or property value.
     * If this property is not used to identify records, the return value of this method
     * is undefined.
     * @param value the bean or property value to test
     * @return <tt>true</tt> if this definition is used to map the given value
     * @since 1.2
     */
    public abstract boolean defines(Object value);
    
    /**
     * Validates and parses the value of this field from a record.  If field validation
     * fails, appropriate field errors are set on the record, and null is returned. 
     * @param record the record to parse and update with any field errors
     * @return the field value, or <tt>null</tt> if validation failed, or 
     *   {@link #MISSING} if the field was not present in the record
     */
    public Object parseValue(Record record) {
        if (!isCollection()) {
            Object value = parsePropertyValue(record);
            if (value == INVALID) {
                value = null;
            }
            return value;
        }
        
        Collection<Object> collection = isArray() ?
            new ArrayList<Object>() : createCollection();
        
        int fieldIndex = 0;
        boolean invalid = false;
        try {
            record.pushField();
            while (maxOccurs < 0 || fieldIndex < maxOccurs) {
                Object value = parsePropertyValue(record);
                
                // abort if the value is missing (i.e. end of record reached)
                if (value == MISSING) {
                    break;
                }
                else if (value != INVALID) {
                    collection.add(value);
                }
                else {
                    invalid = true;
                }
                record.setFieldOffset(++fieldIndex);
            }
        }
        finally {
            record.popField();
        }
        
        // no need to go further if invalid
        if (invalid) {
            return null;
        }
        // no need to go further if its not a property
        else if (!isProperty()) {
            return null;
        }
        
        // validate minimum occurrences have been met
        if (collection.size() < getMinOccurs()) {
            record.addFieldError(getName(), null, "minOccurs", getMinOccurs(), getMaxOccurs());
            return INVALID;
        }
        
        // convert collection to array if necessary
        if (isArray()) {
            Class<?> arrayType = propertyDescriptor == null ? getPropertyType() :
                propertyDescriptor.getPropertyType().getComponentType();
            
            int index = 0;
            Object array = Array.newInstance(arrayType, collection.size());
            
            for (Object obj : collection) {
                Array.set(array, index++, obj);
            }
            return array;
        }
        else {
            return collection;
        }
    }
    
    /**
     * Parses and validates a field property value from the record.
     * @param record the record to parse
     * @return the parsed field value, or {@link #INVALID} if the field was invalid,
     *   or {@link #MISSING} if the field was not present in the record
     */
    protected abstract Object parsePropertyValue(Record record);
    
    /**
     * Creates a new <tt>Collection</tt> for this field based on the configure collection type.
     * @return the new <tt>Collection</tt>
     */
    private Collection<Object> createCollection() {
        try {
            return getCollectionType().newInstance();
        }
        catch (Exception ex) {
            throw new BeanReaderIOException("Failed to instantiate collection '" + 
                getCollectionType().getName() + "' for field '" + getName() + "'", ex);
        }
    }
    
    /**
     * Returns the property name.
     * @return the property name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the property name.
     * @param name the new property name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the bean property descriptor for getting and setting a value for this
     * property from its enclosing bean class.  May be <tt>null</tt> if the property is not
     * a property of its enclosing bean class.
     * @return the bean property descriptor
     */
    public PropertyDescriptor getPropertyDescriptor() {
        return propertyDescriptor;
    }

    /**
     * Sets the bean property descriptor for getting and setting a value for this
     * property from its enclosing bean class.  May be set to <tt>null</tt> if this property
     * is not a property of its enclosing bean class.
     * @param propertyDescriptor the bean property descriptor
     */
    public void setPropertyDescriptor(PropertyDescriptor propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    /**
     * Returns <tt>true</tt> if the value parsed by this property definition is a property
     * of its enclosing bean.
     * @return <tt>true</tt> if the value parsed by this property definition is a property
     *   of its enclosing bean
     */
    public boolean isProperty() {
        return property;
    }

    /**
     * Set to <tt>true</tt> if the value parsed by this property definition is a property
     * of its enclosing bean.
     * @param property <tt>true</tt> if the value parsed by this property definition is a property
     *   of its enclosing bean
     */
    public void setProperty(boolean property) {
        this.property = property;
    }

    /**
     * Returns the class type of this property, or if this property is a collection, the 
     * class type of the collection value.  May be <tt>null</tt> if this property is
     * not a property of its enclosing bean.
     * @return the class type of this property
     */
    public Class<?> getPropertyType() {
        return propertyType;
    }

    /**
     * Sets the class type of this property, or if this property is a collection, the
     * class type of the collection value.  If this property is not a property of its
     * enclosing bean, the class type may be set to <tt>null</tt>.
     * @param type the class type of this property
     */
    public void setPropertyType(Class<?> type) {
        this.propertyType = type;
    }

    /**
     * Returns the maximum length of this property.
     * @return the maximum length of this property
     * @see #setLength(int)
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the (maximum) length of this property.
     * 
     * <p>Delimited fields always have a length of 1, and fixed length fields have a configurable
     *   
     * <p>For delimited records, the length is the number of fields.  For fixed length records, 
     * the length is the number of characters.  Thus a delimited field always has length of 1,
     * while a fixed length field is configurable.  For bean definitions, the length is the sum 
     * of all of its children.</p>
     * 
     * <p>Length is used to offset repeating fields that belong to a collection.  All bean definitions
     * for a collection type of with a collection type for a parent must have a fixed length.
     * The value <tt>-1</tt> may be returned in other cases.</p>
     * 
     * @param length the length of the fields that make up this bean definition
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Sets the parent property definition.
     * @param parent the parent property definition
     */
    public void setParent(PropertyDefinition parent) {
        this.parent = parent;
    }
    
    /**
     * Returns the parent property definition.
     * @return the parent property definition
     */
    public PropertyDefinition getParent() {
        return parent;
    }
    
    /**
     * Returns the collection type of this property, or <tt>null</tt> if this property is not
     * a collection or array.
     * @return the collection type, or {@link TypeUtil#ARRAY_TYPE} if this property is an array,
     *   or <tt>null</tt>
     */
    public Class<? extends Collection<Object>> getCollectionType() {
        return collectionType;
    }

    /**
     * Sets the collection type of this property.  Or if this property is an array, the collection type 
     * should be set to {@link TypeUtil#ARRAY_TYPE}.  The collection type must be a concrete Collection
     * implementation, and cannot be abstract or an interface.
     * @param collectionType the collection type of this property, or {@link TypeUtil#ARRAY_TYPE} for arrays
     */
    public void setCollectionType(Class<? extends Collection<Object>> collectionType) {
        this.collectionType = collectionType;
    }
    
    /**
     * Returns <tt>true</tt> if this property definition is for a collection or array value.
     * @return <tt>true</tt> if this property is a collection type
     */
    public boolean isCollection() {
        return collectionType != null;
    }
    
    /**
     * Returns <tt>true</tt> if this property definition is for an array value.
     * @return <tt>true</tt> if this property is an array type
     */
    public boolean isArray() {
        return collectionType == TypeUtil.ARRAY_TYPE;
    }

    /**
     * Returns the minimum occurrences of this property in a stream.  Always 1 unless
     * this property is a collection.
     * @return the minimum occurrences of this property
     */
    public int getMinOccurs() {
        return minOccurs;
    }

    /**
     * Sets the minimum occurrences of this property in a stream.  Must be 1 unless
     * this property is a collection.
     * @param minOccurs the minimum occurrences of this property
     */
    public void setMinOccurs(int minOccurs) {
        this.minOccurs = minOccurs;
    }

    /**
     * Returns the maximum occurrences of this property in a stream.  Always 1 unless
     * this property is a collection.
     * @return the maximum occurrences of this property
     */
    public int getMaxOccurs() {
        return maxOccurs;
    }

    /**
     * Sets the maximum occurrences of this property in a stream.  Must be 1 unless
     * this property is a collection.
     * @param maxOccurs the maximum occurrences of this property
     */
    public void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    /**
     * Returns <tt>true</tt> if this property is only written to an output stream if
     * the property value is not null.
     * @return <tt>true</tt> if writing null property values to an output stream is suppressed
     */
    public boolean isLazy() {
        return lazy;
    }

    /**
     * Set to <tt>true</tt> if this property is only written to an output stream if
     * the property value is not null.
     * @param lazy <tt>true</tt> to suppress writing null property values to an output stream
     */
    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }
    
    /**
     * Returns <tt>true</tt> if the property or any descendant of this property definition 
     * is used to identify records.
     * @return <tt>true</tt> if this property or any descendant of this property definition is used
     *   to identify records
     * @since 1.2
     */
    public boolean isRecordIdentifier() {
        return recordIdentifier;
    }

    /**
     * Sets whether this property or any descendant of this property definition 
     * is used to identify records.
     * @param recordIdentifier <tt>true</tt> if this property or any descendant of this property 
     *   definition is used to identify records
     * @since 1.2
     */
    public void setRecordIdentifier(boolean recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }
}
