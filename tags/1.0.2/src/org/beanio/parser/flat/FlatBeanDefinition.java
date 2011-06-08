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
package org.beanio.parser.flat;

import java.lang.reflect.Array;
import java.util.*;

import org.beanio.parser.*;

/**
 * Provides support for formatting bean objects for delimited and fixed length formatted records.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class FlatBeanDefinition extends BeanDefinition {

    /**
     * Recursively formats a bean object into a record.
     * @param record the record object
     * @param bean the bean to format
     */
    protected void formatRecord(Object record, Object bean) {
        formatBean(0, record, bean, null);
    }
       
    @SuppressWarnings("unchecked")
    private int formatBean(int offset, final Object record, Object bean, List<Runnable> backfill) {
        if (isCollection()) {
            if (bean == null) {
                for (int i=0; i<getMinOccurs(); i++) {
                    formatProperty(offset + getLength() * i, record, (Object)null, backfill);
                }
            }
            else if (isArray()) {
                for (int i=0, j=Array.getLength(bean); i<j; i++) {
                    Object value = Array.get(bean, i);
                    formatProperty(offset + getLength() * i, record, value, backfill);
                }
            }
            else {
                int i=0;
                for (Object obj : (Collection<Object>)bean) {
                    formatProperty(offset + getLength() * i, record, obj, backfill);
                    ++i;
                }
            }
        }
        else {
            formatProperty(offset, record, bean, backfill);
        }
        return 0;
    }
    
    private void formatProperty(int offset, final Object record, Object bean, List<Runnable> backfill) {
        List<PropertyDefinition> fieldList = getPropertyList();
        for (PropertyDefinition property : fieldList) {
            
            final Object value = bean != null ?  getBeanProperty(property, bean) : null;
            if (property.isBean()) {
                ((FlatBeanDefinition)property).formatBean(offset, record, value, backfill);
            }
            else {
                final FieldDefinition field = (FieldDefinition) property;
                final int position = field.getPosition() + offset;
                
                if (value != null || !field.isLazy()) {
                    // when we encounter a required field or non-null value, write all previous
                    // optional fields to the stream
                    if (backfill != null && !backfill.isEmpty()) {
                        for (Runnable r : backfill) {
                            r.run();
                        }
                        backfill.clear();
                    }
                    addField(field, position, record, value);
                }
                else {
                    // if the value is null and the field isn't required, skip it for now
                    // until a required field is found
                    if (backfill == null) {
                        backfill = new LinkedList<Runnable>();
                    }
                    backfill.add(new Runnable() {
                        public void run() {
                            addField(field, position, record, value);
                        }
                    });
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void addField(FieldDefinition field, int position, Object record, Object value) {
        if (field.isCollection()) {
            int fieldLength = field.getLength();
            
            if (value == null) {
                String text = field.formatValue(null);
                for (int i=0; i<field.getMinOccurs(); i++) {
                    updateRecord(record, position + i*fieldLength, text);
                }
            }
            else if (field.isArray()) {
                for (int i=0, j=Array.getLength(value); i<j; i++) {
                    updateRecord(record, position + i*fieldLength, field.formatValue(Array.get(value, i)));
                }
            }
            else {
                int i=0;
                for (Object obj : (Collection<Object>)value) {
                    updateRecord(record, position + i*fieldLength, field.formatValue(obj));
                    ++i;
                }
            }
        }
        else {
            updateRecord(record, position, field.formatValue(value));
        }
        
    }
    
    /**
     * This method is called by <tt>formatRecord</tt> to set the field text on the record
     * object being formatted.
     * @param record the record object
     * @param position the field position
     * @param text the field text
     */
    protected abstract void updateRecord(Object record, int position, String text);

}
