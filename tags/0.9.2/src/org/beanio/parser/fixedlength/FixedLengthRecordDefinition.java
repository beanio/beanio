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
package org.beanio.parser.fixedlength;

import java.lang.reflect.Array;
import java.util.Collection;

import org.beanio.parser.*;

/**
 * A <tt>FixedLengthRecordDefinition</tt> is used to parse and format fixed length
 * records.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthRecordDefinition extends RecordDefinition {

    private int minLength = 0;
    private int maxLength = -1;
    private char filler = ' ';

    @Override
    @SuppressWarnings("unchecked")
    public Object formatBean(Object bean) {
        StringBuffer record = new StringBuffer();
        for (FieldDefinition field : getFieldList()) {
            // add filler for undefined spaces
            int fillerLength = field.getPosition() - record.length();
            for (int i = 0; i < fillerLength; i++) {
                record.append(filler);
            }

            Object value = getFieldValue(field, bean);
            if (field.isCollection()) {
                if (value == null) {
                    for (int i=0; i<field.getMinOccurs(); i++) {
                        record.append(field.formatValue(null));
                    }
                }
                else if (field.isArray()) {
                    for (int i=0, j=Array.getLength(value); i<j; i++) {
                        record.append(field.formatValue(Array.get(value, i)));
                    }
                }
                else {
                    for (Object obj : (Collection<Object>)value) {
                        record.append(field.formatValue(obj));
                    }
                }
            }
            else {
                record.append(field.formatValue(value));
            }
        }
        return record;
    }

    @Override
    protected void validateRecord(Record record) {
        int length = record.getRecordText().length();
        if (minLength > 0 && length < minLength) {
            record.addRecordError("minLength", minLength, maxLength);
        }
        if (maxLength > 0 && length > maxLength) {
            record.addRecordError("maxLength", minLength, maxLength);
        }
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

    /**
     * Returns the filler character used to fill undefined spaces in the record.
     * Defaults to a space.
     * @return the filler character
     */
    public char getFiller() {
        return filler;
    }

    /**
     * Sets the filler character to use to fill undefined spaces in the record.
     * @param filler the filler character
     */
    public void setFiller(char filler) {
        this.filler = filler;
    }
}
