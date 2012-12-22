/*
 * Copyright 2011-2012 Kevin Seim
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
package org.beanio.internal.parser.format.delimited;

import java.util.*;

import org.beanio.internal.parser.MarshallingContext;

/**
 * A {@link MarshallingContext} for delimited records.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class DelimitedMarshallingContext extends MarshallingContext {

    // the index of the last committed field in the record
    private int committed = 0;
    // the list of fields in the record
    private ArrayList<String> record = new ArrayList<String>();
    
    /**
     * Constructs a new <tt>DelimitedMarshallingContext</tt>.
     */
    public DelimitedMarshallingContext() { }
    
    @Override
    public void clear() {
        super.clear();
        
        record.clear();
        committed = 0;
    }
    
    /**
     * Puts the field text in the record.
     * @param position the position of the field in the record.
     * @param fieldText the field text
     * @param commit true to commit the current record, or false
     *   if the field is optional and should not extend the record
     *   unless a subsequent field is later appended to the record 
     */
    public void setField(int position, String fieldText, boolean commit) {
        int index = getAdjustedFieldPosition(position);
        int size = record.size();
        
        if (index == size) {
            record.add(fieldText);
            ++size;
        }
        else if (index < size) {
            record.set(index, fieldText);
        }
        else {
            for (int max=index; size<max; ++size) {
                record.add("");
            }
            record.add(fieldText);
            ++size;
        }
        
        if (commit) {
            committed = size;
        }
    }
    
    @Override
    public Object getRecordObject() {
        int size = record.size();
        if (committed < size) {
            String [] array = new String[committed];
            for (int i=0; i<committed; i++) {
                array[i] = record.get(i);
            }
            return array;
        }
        else {
            String [] array = new String[record.size()];
            record.toArray(array);
            return array;
        }
    }
    
    @Override
    public String[] toArray(Object record) {
        return (String[])record;
    }
    
    @Override
    public List<String> toList(Object record) {
        return Arrays.asList((String[])record);
    }
}
