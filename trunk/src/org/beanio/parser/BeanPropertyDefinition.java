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

/**
 * This property definition is used to define a constant bean property value that does
 * not directly map to any field in an input or output stream.  This allows a bean
 * property to be populated based on record identification during unmarshalling.  
 * Or likewise during marshalling, for a bean property value that is not mapped to a
 * stream field to identify the mapping definition.
 * 
 * @author Kevin Seim
 * @since 1.2
 */
public class BeanPropertyDefinition extends PropertyDefinition {

    private Object value;
    
    /**
     * Constructs a new <tt>BeanPropertyDefinition</tt>.
     */
    public BeanPropertyDefinition() { }

    @Override
    public boolean isConstant() {
        return true;
    }
    
    @Override
    public boolean matches(Record record) {
        return true;
    }
    
    @Override
    public boolean defines(Object propertyValue) {
        return value.equals(propertyValue);
    }

    @Override
    protected Object parsePropertyValue(Record record) {
        return value;
    }

    /**
     * Returns the property value.
     * @return the property value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the property value.
     * @param value the property value
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
