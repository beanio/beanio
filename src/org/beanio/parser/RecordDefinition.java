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

import java.util.*;

import org.beanio.InvalidRecordException;

/**
 * A <tt>RecordDefinition</tt> is used to parse and format a record.  Exception
 * for record level validation, most of the work is delegated to a bean definition.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see BeanDefinition
 */
public abstract class RecordDefinition extends NodeDefinition {

    private BeanDefinition beanDefinition;
    
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

    @Override
    public NodeDefinition findDefinitionFor(Object bean) {
        return (beanDefinition.isDefinitionFor(bean)) ? this : null;
    }
    
    /**
     * Test if a record matches this record definition.
     * @param record the record to test
     * @return <tt>true</tt> if all key fields are matched by this record
     */
    public boolean matches(Record record) {
        return beanDefinition.matches(record);
    }

    /**
     * Validates and parses a record into a Java bean.
     * @param record the record to parse
     * @return the parsed Java bean
     * @throws InvalidRecordException
     */
    public Object parseBean(Record record) throws InvalidRecordException {
        
        // validate record
        validateRecord(record);
        if (record.hasRecordErrors()) {
            throw new InvalidRecordException(record.getContext(),
                "Invalid '" + record.getRecordName() + "' record at line " +
                    record.getRecordLineNumber());
        }

        Object value = beanDefinition.parseValue(record);
        if (value == BeanDefinition.MISSING) {
            value = null;
        }
            
        // if field errors were detected, throw an exception
        if (record.hasFieldErrors()) {
            throw new InvalidRecordException(record.getContext(),
                "Invalid '" + record.getRecordName() + "' record at line "
                    + record.getRecordLineNumber());
        }
        
        return value;
    }
    
    /**
     * Performs record level validations.  If a validation error occurs, a record
     * error should be added to the <tt>record</tt>.
     * @param record the record to validate
     */
    protected void validateRecord(Record record) { }
    
    /**
     * Formats a Java bean into a record (whose class type will depend
     * on the type of stream and record writer being used).
     * @param bean the bean to format
     * @return the formatted record
     */
    public Object formatBean(Object bean) {
        return beanDefinition.formatRecord(bean);
    }
    
    /**
     * Returns the bean definition used to parse and format this record.
     * @return the bean definition
     */
    public BeanDefinition getBeanDefinition() {
        return beanDefinition;
    }

    /**
     * Sets the bean definition used to parse and format this record.
     * @param beanDefinition the bean definition
     */
    public void setBeanDefinition(BeanDefinition beanDefinition) {
        this.beanDefinition = beanDefinition;
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + 
            "beanDefinition= " + beanDefinition +
            "]";
    }
}
