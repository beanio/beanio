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
package org.beanio.parser.delimited;

import java.lang.reflect.Array;
import java.util.*;

import org.beanio.parser.*;

/**
 * A <tt>FixedLengthRecordDefinition</tt> is used to parse and format delimited
 * records.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DelimitedRecordDefinition extends RecordDefinition {

    private int minLength = 0;
    private int maxLength = -1;

    @Override
    protected void validateRecord(Record r) {
        super.validateRecord(r);

        DelimitedRecord record = (DelimitedRecord) r;

        int size = record.getFieldCount();
        if (minLength > 0 && size < minLength) {
            record.addRecordError("minLength", minLength, maxLength);
        }
        if (maxLength > 0 && size > maxLength) {
            record.addRecordError("maxLength", minLength, maxLength);
        }
    }

    
    @Override
    @SuppressWarnings("unchecked")
    public Object formatBean(Object bean) {
        int index = 0;
        List<FieldDefinition> fieldList = getFieldList();
        Object[] fieldValue = new Object[fieldList.size()];
        
        // determine the total number of fields in the delimited record and get field values
        int size = fieldList.size();
        for (FieldDefinition field : fieldList) {
            Object value = getFieldValue(field, bean);
            if (field.isCollection()) {
                if (value == null) {
                    size += field.getMinOccurs() - 1;
                }
                else if (field.isArray()) {
                    size += Array.getLength(value) - 1;
                }
                else {
                    size += ((Collection<?>)value).size() - 1;
                }
            }
            fieldValue[index++] = value;
        }
        
        // format each field value into field text
        index = 0;
        int valueIndex = 0;
        String[] record = new String[size];
        for (FieldDefinition field : fieldList) {
            Object value = fieldValue[valueIndex++];
            if (field.isCollection()) {
                if (value == null) {
                    for (int i=0; i<field.getMinOccurs(); i++) {
                        record[index++] = field.formatValue(null);
                    }
                }
                else if (field.isArray()) {
                    for (int i=0, j=Array.getLength(value); i<j; i++) {
                        record[index++] = field.formatValue(Array.get(value, i));
                    }
                }
                else {
                    for (Object obj : (Collection<Object>)value) {
                        record[index++] = field.formatValue(obj);
                    }
                }
            }
            else {
                record[index++] = field.formatValue(value);
            }
        }
        return record;
    }

    /**
     * Returns the minimum number of fields this record must have to be valid.
     * @return the minimum number of fields.
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum number of fields the record must have to be valid.
     * @param minLength the minimum number of fields
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum number of fields this record can have to be valid.  
     * A value of <tt>-1</tt> indicates there is no limit.
     * @return the maximum number of fields
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum number of fields this record can have.  A value of 
     * <tt>-1</tt> indicates there is no limit.
     * @param maxLength the number of fields
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}
