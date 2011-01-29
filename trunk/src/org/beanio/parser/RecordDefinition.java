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
package org.beanio.parser;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;

import org.beanio.*;

/**
 * A <tt>RecordDefinition</tt> is responsible for parsing and formatting
 * records.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class RecordDefinition extends NodeDefinition {

    private Class<?> beanClass;
    private boolean isBeanClassMap;

    private List<FieldDefinition> fieldList = new ArrayList<FieldDefinition>();

    /**
     * Creates a new <tt>RecordDefinition</tt>.
     */
    public RecordDefinition() { }

    @Override
    public final List<NodeDefinition> getChildren() {
        return Arrays.<NodeDefinition> asList(this);
    }

    @Override
    public final boolean isRecordDefinition() {
        return true;
    }

    /**
     * Test if a record matches this record definition.
     * @param record the record to test
     * @return <tt>true</tt> if all key fields are matched by this record
     */
    public boolean matches(Record record) {
        // if the record does not have any key fields, its always a match
        for (FieldDefinition f : fieldList) {
            if (f.isRecordIdentifier() && !f.matches(record)) {
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
    @SuppressWarnings({ "unchecked" })
    public Object parseBean(Record record) throws InvalidRecordException {
        // validate record
        validateRecord(record);
        if (record.hasRecordErrors()) {
            throw new InvalidRecordException(record.getContext(),
                "Invalid '" + record.getRecordName() + "' record at line " +
                    record.getRecordLineNumber());
        }

        Object[] field = new Object[fieldList.size()];

        // validate and parse each field
        int index = 0;
        for (FieldDefinition fieldContext : fieldList) {
            field[index++] = fieldContext.parseValue(record);
        }

        // if field errors were detected, throw an exception
        if (record.hasFieldErrors()) {
            throw new InvalidRecordException(record.getContext(),
                "Invalid '" + record.getRecordName() + "' record at line "
                    + record.getRecordLineNumber());
        }

        // if the bean class is null, the record is ignored and null is returned here
        if (beanClass == null)
            return null;

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
        if (isBeanClassMap) {
            Map<String, Object> map = (Map<String, Object>) bean;
            for (int i = 0, j = fieldList.size(); i < j; i++) {
                FieldDefinition fieldContext = fieldList.get(i);
                Object value = field[i];
                if (fieldContext.isProperty()) {
                    map.put(fieldContext.getName(), value);
                }
            }
        }
        else {
            for (int i = 0, j = fieldList.size(); i < j; i++) {
                FieldDefinition fieldContext = fieldList.get(i);
                if (fieldContext.isProperty()) {
                    Object value = field[i];

                    PropertyDescriptor descriptor = fieldContext.getPropertyDescriptor();

                    Method method = descriptor.getWriteMethod();
                    if (method == null) {
                        throw new BeanReaderIOException(record.getContext(),
                            "There is no writeable property for field '" +
                            fieldContext.getName() + "' on class '" + beanClass.getName() + "'");
                    }

                    try {
                        method.invoke(bean, new Object[] { value });
                    }
                    catch (Exception ex) {
                        throw new BeanReaderIOException(record.getContext(),
                            "Failed to create bean for record '" + getName() + "'", ex);
                    }

                }
            }
        }

        return bean;
    }
    
    /**
     * Performs record level validations.  If a validation error occurs, a record
     * error should be added to the <tt>record</tt>.
     * @param record the record to validate
     */
    protected void validateRecord(Record record) { }

    @Override
    public NodeDefinition findDefinitionFor(Object bean) {
        if (beanClass == null) {
            return null;
        }
        
        if (isMatchForRecordType(bean)) {
            for (FieldDefinition field : fieldList) {
                if (!field.isRecordIdentifier()) {
                    continue;
                }
                if (!field.isMatch(getFieldValue(field, bean))) {
                    return null;   
                }
            }
            return this;
        }
        
        return null;
    }
    
    /**
     * Returns the field value from a record's bean object.
     * @param field the field to extract from the bean
     * @param bean the bean to extract the field from
     * @return the field value
     */
    @SuppressWarnings("rawtypes")
    protected Object getFieldValue(FieldDefinition field, Object bean) {
        if (!field.isProperty()) {
            return null;
        }
        else if (isBeanClassMap) {
            return ((Map) bean).get(field.getName());
        }
        else {
            // determine the getter method to use
            Method getter = null;
            PropertyDescriptor propertyDescriptor = field.getPropertyDescriptor();
            if (propertyDescriptor != null) {
                getter = propertyDescriptor.getReadMethod();
            }
            if (getter == null) {
                throw new BeanWriterIOException("No getter found for field '" + field.getName() + 
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
    
    /**
     * Returns <tt>true</tt> if the bean object matches the bean type of this record.
     * @param bean the bean object to test
     * @return <tt>true</tt> if the bean object is assignable from the bean type of
     *   this class
     */
    private boolean isMatchForRecordType(Object bean) {
        if (isBeanClassMap) {
            return Map.class.isAssignableFrom(bean.getClass());
        }
        else if (beanClass != null) {
            return beanClass.isAssignableFrom(bean.getClass());
        }
        else {
            return false;
        }
    }

    /**
     * Formats a Java bean into a record (whose class type will depend
     * on the type of stream and record writer being used).
     * @param bean the bean to format
     * @return the formatted record
     */
    public abstract Object formatBean(Object bean);

    /**
     * Adds a field definition to this record definition.
     * @param f the field definition to add
     */
    public void addField(FieldDefinition f) {
        fieldList.add(f);
    }

    /**
     * Returns the list of fields that make up this record.
     * @return the list of fields that make up this record
     */
    public List<FieldDefinition> getFieldList() {
        return fieldList;
    }

    /**
     * Returns the number of fields that make up this record.
     * @return the number of fields
     */
    public int getFieldCount() {
        return fieldList.size();
    }

    /**
     * Sets the list of fields that make up this record.
     * @param fieldList the list of fields that make up this record
     */
    public void setFieldList(List<FieldDefinition> fieldList) {
        this.fieldList.clear();
        if (fieldList != null) {
            this.fieldList.addAll(fieldList);
        }
    }

    /**
     * Returns the bean class configured for this record type.  May be <tt>null</tt>
     * if this record is only validated and not returned when reading an input stream.
     * @return the bean class
     */
    public Class<?> getBeanClass() {
        return beanClass;
    }

    /**
     * Sets the bean class configured for this record type.
     * @param clazz the new bean class
     */
    public void setBeanClass(Class<?> clazz) {
        this.beanClass = clazz;
        this.isBeanClassMap = clazz != null && Map.class.isAssignableFrom(beanClass);
    }

    /**
     * Returns true if the bean class is assignable to <tt>java.util.Map</tt>.
     * @return true if the bean class is assignable to <tt>java.util.Map</tt>
     */
    public boolean isBeanClassMap() {
        return isBeanClassMap;
    }
}
