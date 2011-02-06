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
package org.beanio.parser.fixedlength;

import org.beanio.parser.flat.FlatBeanDefinition;

/**
 * A <tt>FixedLengthRecordDefinition</tt> is used to parse and format fixed length
 * formatted beans.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthBeanDefinition extends FlatBeanDefinition {

    private char filler = ' ';
    
    @Override
    public Object formatRecord(Object bean) {
        StringBuffer record = new StringBuffer();
        super.formatRecord(record, bean);
        return record.toString();
    }

    @Override
    protected void updateRecord(Object recordObject, int position, String text) {
        StringBuffer record = (StringBuffer) recordObject;
        
        int size = record.length();
        if (position == size) {
            record.append(text);
        }
        else if (position < size) {
            record.replace(position, position + text.length(), text);
        }
        else {
            for (int i=size, j=position; i<j; i++) {
                record.append(filler);
            }
            record.append(text);            
        }
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
