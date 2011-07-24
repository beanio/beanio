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

/**
 * Property definition that defines a bean class and holds its own list of properties
 * for setting on the bean class.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class BeanDefinition extends PropertyDefinition {

    private List<PropertyDefinition> propertyList = new ArrayList<PropertyDefinition>();
    private boolean propertyTypeMap;
    
    /**
     * Creates a new <tt>BeanDefinition</tt>.
     */
    public BeanDefinition() { }
    
    /**
     * Returns <tt>true</tt> indicating this property definition defines a
     * bean object.
     * @return <tt>true</tt>
     */
    @Override
    public final boolean isBean() {
        return true;
    }
      
    /**
     * Test if a record matches this record definition.
     * @param record the record to test
     * @return <tt>true</tt> if all key fields are matched by this record
     */
    public boolean matches(Record record) {
        // if the record does not have any key fields, its always a match
        for (PropertyDefinition prop : propertyList) {
            if (!prop.matches(record)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Validates and parses a record into a Java bean.
     * @param record the record to parse
     * @return the parsed Java bean
     * @throws InvalidRecordException
     */
    @SuppressWarnings("unchecked")
    protected Object parsePropertyValue(Record record) throws InvalidRecordException {
        Object[] propertyValue = new Object[propertyList.size()];

        // validate and parse each property of this bean
        int index = 0;
        boolean exists = isBeanExistenceKnown();
        for (PropertyDefinition property : propertyList) {
            Object value = property.parseValue(record);

            if (value == MISSING) {
                value = null;
            }
            else if (!property.isCollection()) {
                exists = true;
            }
            
            propertyValue[index++] = value;
        }

        // determine if any bean field was in the input stream
        index = 0;
        if (!exists) {
            // still need to check collections...
            for (PropertyDefinition property : propertyList) {
                if (property.isCollection()) {
                    Object value = propertyValue[index];
                    if (value == null) {
                        continue;
                    }
                    else if (value == INVALID) {
                        exists = true;
                        break;
                    }
                    else if (property.isArray()) {
                        if (Array.getLength(value) > 0) {
                            exists = true;
                            break;
                        }
                    }
                    else {
                        if (!((Collection<Object>)value).isEmpty()) {
                            exists = true;
                            break;
                        }
                    }
                }
                index++;
            }
            if (!exists) {
                return MISSING;
            }
        }
        
        // if field errors were detected, no need to instantiate a bean
        if (record.hasFieldErrors()) {
            return INVALID;
        }

        // if the bean class is null, the record will be ignored and null is returned here
        Class<?> beanClass = getPropertyType();
        if (beanClass == null) {
            return null;
        }
        
        // instantiate the bean
        Object bean;
        try {
            bean = beanClass.newInstance();
        }
        catch (Exception e) {
            throw new BeanReaderIOException(record.getContext(),
                "Failed to instantiate class '" + beanClass.getName() +
                    "' for record '" + getName() + "'", e);
        }

        // set the bean properties
        index = 0;
        for (PropertyDefinition property : propertyList) {
            try {
                setBeanProperty(property, bean, propertyValue[index++]);
            }
            catch (BeanIOException ex) {
                throw new BeanReaderIOException(record.getContext(),
                    ex.getMessage(), ex.getCause());
            }
        }

        return bean;
    }
    
    /**
     * Returns <tt>true</tt> if a bean is known to exist before parsing its child
     * fields.  If <tt>false</tt> is returned, at least one bean field must exist
     * in the stream before the bean is created.
     * @return <tt>true</tt> if a bean is known to exist before parsing its fields
     */
    protected boolean isBeanExistenceKnown() {
        return false;
    }

    /**
     * Sets a property on a bean object created by this definition.
     * @param property the property definition
     * @param bean the bean to set the property on
     * @param value the property value to set
     */
    @SuppressWarnings("unchecked")
    public void setBeanProperty(PropertyDefinition property, Object bean, Object value) {
        if (!property.isProperty()) {
            return;
        }
        
        // set the bean properties
        if (isPropertyTypeMap()) {
            ((Map<String, Object>) bean).put(property.getName(), value);
        }
        else {
            PropertyDescriptor descriptor = property.getPropertyDescriptor();

            Method method = descriptor.getWriteMethod();
            if (method == null) {
                throw new BeanIOException(
                    "There is no writeable property for field '" +
                    getName() + "' on class '" + bean.getClass().getName() + "'");
            }

            try {
                method.invoke(bean, new Object[] { value });
            }
            catch (Exception ex) {
                throw new BeanIOException(
                    "Failed to create bean for record '" + getName() + "'", ex);
            }
        }
    }
    
    /**
     * Retrieves a property from a bean object created by this definition.
     * @param definition the property definition
     * @param bean the bean to get the property from
     * @return the property value
     */
    @SuppressWarnings("unchecked")
    public Object getBeanProperty(PropertyDefinition definition, Object bean) {
        if (!definition.isProperty()) {
            return null;
        }
        else if (isPropertyTypeMap()) {
            return ((Map<String,Object>) bean).get(definition.getName());
        }
        else {
            // determine the getter method to use
            Method getter = null;
            PropertyDescriptor propertyDescriptor = definition.getPropertyDescriptor();
            if (propertyDescriptor != null) {
                getter = propertyDescriptor.getReadMethod();
            }
            if (getter == null) {
                throw new BeanWriterIOException("No getter found for field '" + getName() + 
                    "' on bean class '" + bean.getClass().getName() + "'");
            }

            // user the getter method to extract the field value from the bean class
            try {
                return getter.invoke(bean);
            }
            catch (Exception ex) {
                throw new BeanWriterIOException("Failed to get field '" + getName() +
                    "' from bean class '" + bean.getClass().getName() + "' using " +
                    "getter method '" + getter.getName() + "'", ex);
            }
        }
    }
    
    @Override
    public boolean defines(Object bean) {
        if (bean == null || getPropertyType() == null) {
            return false;
        }
        
        if (isDefinitionForType(bean)) {
            for (PropertyDefinition propertyDefinition : propertyList) {
                // if the child property is not used to identify records, no need to go further
                if (!propertyDefinition.isRecordIdentifier() || !propertyDefinition.isProperty()) {
                    continue;
                }
                    
                Object value = getBeanProperty(propertyDefinition, bean);
                if (!propertyDefinition.defines(value)) {
                    return false;
                }
            }
            return true;
        }
        
        return false;
    }

    /**
     * Tests if this bean definition describes a given bean object.
     * @param bean the bean object to test
     * @return <tt>true</tt> if the bean definition describes the bean object
     * @deprecated use {@link #defines(Object)}
     */
    public boolean isDefinitionFor(Object bean) {
        return defines(bean);
    }
    
    /**
     * Returns <tt>true</tt> if the bean object matches the bean type of this record.
     * @param bean the bean object to test
     * @return <tt>true</tt> if the bean object is assignable from the bean type of
     *   this class
     */
    private boolean isDefinitionForType(Object bean) {
        if (isPropertyTypeMap()) {
            return Map.class.isAssignableFrom(bean.getClass());
        }
        else {
            Class<?> beanClass = getPropertyType();
            return beanClass != null && beanClass.isAssignableFrom(bean.getClass());
        }
    }

    /**
     * Returns a list of all field definition descendants of this bean.
     * @return the list of field definitions
     */
    public List<FieldDefinition> getAllFields() {
        List<FieldDefinition> list = new ArrayList<FieldDefinition>();
        createFieldList(list, this);
        return list;
    }
    
    /* 
     * Recursively build a list of all field descendants 
     */
    private void createFieldList(List<FieldDefinition> list, BeanDefinition definition) {
        for (PropertyDefinition pd : definition.getPropertyList()) {
            if (pd.isBean()) {
                createFieldList(list, (BeanDefinition)pd);
            }
            else if (pd.isField()) {
                list.add((FieldDefinition)pd);
            }
        }
    }
    
    /**
     * Formats a Java bean into a record (whose class type will depend
     * on the type of stream and record writer being used).
     * @param bean the bean to format
     * @return the formatted record
     */
    public abstract Object formatRecord(Object bean);

    /**
     * Adds a property definition to this bean definition.
     * @param f the property definition to add
     */
    public void addProperty(PropertyDefinition f) {
        propertyList.add(f);
    }

    /**
     * Returns the property definition. 
     * @param index the index of the property definition
     * @return the property definition
     * @throws IndexOutOfBoundsException if there is no property definition for the 
     *   given index
     */
    public PropertyDefinition getProperty(int index) {
        return propertyList.get(index);
    }
    
    /**
     * Returns the list of properties that make up this bean.
     * @return the list of properties that make up this bean
     */
    public List<PropertyDefinition> getPropertyList() {
        return propertyList;
    }

    /**
     * Returns the number of properties that make up this bean.
     * @return the number of properties
     */
    public int getPropertyCount() {
        return propertyList.size();
    }

    /**
     * Sets the list of properties that make up this bean.
     * @param list the list of properties that make up this bean
     */
    public void setPropertyList(List<PropertyDefinition> list) {
        if (this.propertyList == list) {
            return;
        }
        this.propertyList.clear();
        if (list != null) {
            this.propertyList.addAll(list);
        }
    }
    
    /**
     * Sets the property type of this bean, or if this bean is a collection, the
     * property type of the collection value.
     * @param type the property type of this bean
     */
    @Override
    public void setPropertyType(Class<?> type) {
        super.setPropertyType(type);
        this.propertyTypeMap = type != null && Map.class.isAssignableFrom(type);
    }

    /**
     * Returns <tt>true</tt> if the property type of this bean object is assignable to
     * <tt>java.util.Map</tt>.
     * @return <tt>true</tt> if the property type of this bean is a <tt>Map</tt>
     */
    public boolean isPropertyTypeMap() {
        return propertyTypeMap;
    }

    /**
     * Returns <tt>true</tt> if any field descendant of this bean definition is used
     * to identify the record.
     * @return <tt>true</tt> if any field descendant of this bean definition is used
     *   to identify the record
     * @deprecated use {@link #isRecordIdentifier()}
     */
    public boolean isRecordIdentifer() {
        return isRecordIdentifier();
    }

    /**
     * Sets whether any field descendant of this bean definition is used to identify
     * the reccord.
     * @param recordIdentifer <tt>true</tt> if any field descendant of this bean 
     *   definition is used to identify the record
     * @deprecated use {@link #setRecordIdentifier(boolean)}
     */
    public void setRecordIdentifer(boolean recordIdentifer) {
        setRecordIdentifier(recordIdentifer);
    }
}
