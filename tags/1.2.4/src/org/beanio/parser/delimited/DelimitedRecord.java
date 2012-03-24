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

import org.beanio.parser.Record;

/**
 * The <tt>DelimitedRecord</tt> stores field text in a <tt>String</tt> array
 * and works with either the <tt>CsvReader</tt> or <tt>DeimitedReader</tt>.
 *  
 * @author Kevin Seim
 * @since 1.0
 */
public class DelimitedRecord extends Record {

    private String[] fields;

    /**
     * Constructs a new <tt>DelimitedRecord</tt>.
     */
    public DelimitedRecord() { }

    @Override
    public void clear() {
        super.clear();
        fields = null;
    }

    @Override
    public void setValue(Object value) {
        this.fields = (String[]) value;
    }

    /**
     * Returns the number of fields read from the input stream.
     * @return the number of fields
     */
    public int getFieldCount() {
        return fields.length;
    }

    /**
     * Returns the field text at the given position in the record.
     * @param position the position of the field wihin the record
     * @return the field text
     */
    public String getFieldText(int position) {
        return fields[position];
    }
}
