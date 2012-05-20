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
package org.beanio.internal.parser.format.fixedlength;

import org.beanio.internal.parser.MarshallingContext;

/**
 * A {@link MarshallingContext} for a fixed length formatted stream.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class FixedLengthMarshallingContext extends MarshallingContext {

    // the current record
    private StringBuilder record = new StringBuilder();
    // the filler character for missing fields
    private char filler = ' ';
    // the committed length of the record, aka the size of the record after
    // appending the last required field
    private int committed = 0;
    
    /**
     * Constructs a new <tt>FixedLengthMarshallingContext</tt>.
     */
    public FixedLengthMarshallingContext() { }
    
    @Override
    public Object getRecordObject() {
        if (committed == record.length()) {
            return record.toString();
        }
        else {
            return record.substring(0, committed).toString();
        }
    }
    
    @Override
    public void clear() {
        super.clear();
        
        record = new StringBuilder();
        committed = 0;
    }
    
    /**
     * Inserts field text into the record being marshalled.
     * @param position the position of the field in the record
     * @param text the field text to insert
     * @param commit true to commit the current field length, or false
     *   if the field is optional and should not extend the record length
     *   unless a subsequent field is appended to the record 
     */
    public void setFieldText(int position, String text, boolean commit) {
        position = getAdjustedFieldPosition(position);

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
        
        if (commit) {
            committed = record.length();
        }
    }
}
