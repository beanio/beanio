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
package org.beanio.config;

import java.util.*;

/**
 * Stores configuration settings for a record.  Records and groups
 * are used to define the layout of stream.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class RecordConfig extends NodeConfig {

    private Integer minLength = null;
    private Integer maxLength = null;
    private String beanClass;
    private List<FieldConfig> fieldList = new ArrayList<FieldConfig>();

    /**
     * Returns {@link NodeConfig#RECORD}
     */
    @Override
    public char getType() {
        return RECORD;
    }

    /**
     * Returns the minimum length of the record.  Depending on the type
     * of stream, the length may refer to the number of fields or the 
     * number of characters.
     * @return the minimum record length, or <tt>null</tt> if not set
     */
    public Integer getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length of the record.  Depending on the type
     * of stream, the length may refer to the number of fields or the 
     * number of characters.
     * @param minLength the minimum record length, or <tt>null</tt> if not set
     */
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum length of the field.  Depending on the type of
     * stream, the length may refer to the number of fields or the number
     * of characters.
     * @return the maximum record length, or <tt>null</tt> if not set
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length of the field.  Depending on the type of
     * stream, the length may refer to the number of fields or the number
     * of characters.
     * @param maxLength the maximum record length, or <tt>null</tt> if not set
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Returns the fully qualified class name of the bean object for
     * this record.  If <tt>null</tt>, matching records are fully validated 
     * according to its field definition but no bean will be created for 
     * the record.
     * @return the bean class, or <tt>null</tt> if bean creation is skipped
     */
    public String getBeanClass() {
        return beanClass;
    }

    /**
     * Sets the fully qualified class name of the bean object for this
     * this record.  If <tt>null</tt>, matching records are fully validated 
     * according to its field definition but no bean will be created for 
     * the record.
     * @param beanClass the bean class, or <tt>null</tt> if bean creation is skipped
     */
    public void setBeanClass(String beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Adds a field to this record.
     * @param fieldConfig the field configuration
     */
    public void addField(FieldConfig fieldConfig) {
        fieldList.add(fieldConfig);
    }

    /**
     * Returns a list of this record's fields.
     * @return the list of fields that make up this record
     */
    public List<FieldConfig> getFieldList() {
        return fieldList;
    }

    /**
     * Sets the list of fields that make up this record.
     * @param fieldList the list of fields
     */
    public void setFieldList(List<FieldConfig> fieldList) {
        if (fieldList == null) {
            this.fieldList.clear();
        }
        else {
            this.fieldList = fieldList;
        }
    }
}
