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
package org.beanio.config;

import java.util.*;

/**
 * A property configuration that describes a Java bean object.  The bean class type is
 * set using the <tt>type</tt> attribute, and its properties can be configured by
 * adding child <tt>PropertyConfig</tt> instances.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class BeanConfig extends PropertyConfig {

    private List<PropertyConfig> propertyList = new ArrayList<PropertyConfig>();
    
    /**
     * Constructs a new <tt>BeanConfig</tt>.
     */
    public BeanConfig() { }
    
    /**
     * Returns <tt>true</tt> to indicate this property is a bean.
     * @return <tt>true</tt>
     */
    @Override
    public boolean isBean() {
        return true;
    }
    
    /**
     * Adds a property to this bean.
     * @param fieldConfig the field configuration
     */
    public void addProperty(PropertyConfig fieldConfig) {
        propertyList.add(fieldConfig);
    }
    
    /**
     * Returns a list of this bean's properties.
     * @return the list of properties that make up this bean
     */
    public List<PropertyConfig> getPropertyList() {
        return propertyList;
    }
    
    /**
     * Returns the property configuration at the given index.
     * @param index the property index
     * @return the property configuration
     * @throws IndexOutOfBoundsException if there is no property at the specified index
     */
    public PropertyConfig getProperty(int index) {
        return propertyList.get(index);
    }

    /**
     * Sets the list of properties that make up this bean.
     * @param list the list of properties
     */
    public void setPropertyList(List<PropertyConfig> list) {
        if (list == null) {
            this.propertyList.clear();
        }
        else {
            this.propertyList = list;
        }
    }
}
