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
package org.beanio.parser.delimited;

import java.util.*;

import org.beanio.parser.flat.FlatBeanDefinition;

/**
 * A <tt>DelimitedBeanDefinition</tt> is used to parse and format delimited
 * formatted beans.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DelimitedBeanDefinition extends FlatBeanDefinition {

    @Override
    public Object formatRecord(Object bean) {
        ArrayList<String> record = new ArrayList<String>();
        formatRecord(record, bean);
        
        String [] array = new String[record.size()];
        record.toArray(array);
        return array;
    }
    
    @Override
    protected void updateRecord(Object recordObject, int index, String value) {
        @SuppressWarnings("unchecked")
        List<String> record = (List<String>) recordObject;
        
        int size = record.size();
        if (index == size) {
            record.add(value);
        }
        else if (index < size) {
            record.set(index, value);
        }
        else {
            for (int i=size, j=index; i<j; i++) {
                record.add("");
            }
            record.add(value);            
        }
    }
}
