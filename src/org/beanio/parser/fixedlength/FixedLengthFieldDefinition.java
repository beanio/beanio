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
package org.beanio.parser.fixedlength;

import org.beanio.parser.Record;
import org.beanio.parser.flat.FlatFieldDefinition;

/**
 * A <tt>FixedLengthFieldDefinition</tt> is used to parse and format fields for
 * fixed length records.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthFieldDefinition extends FlatFieldDefinition {
    
    @Override
    public boolean matches(Record record) {     
        if (!isRecordIdentifier()) {
            return true;
        }
        
        String text = getFieldText(record);
        if (text.length() != getLength()) {
            return false;
        }
        else {
            return isMatch(unpad(text));
        }
    }

    @Override
    protected String parseField(Record record) {
        String fieldText = getFieldText(record);
        record.setFieldText(getName(), fieldText);

        if (fieldText == null) {
            return null;
        }
        else if (fieldText.length() != getLength()) {
            record.addFieldError(getName(), fieldText, "length", getLength());
            return INVALID;
        }
        else {
            return unpad(fieldText);
        }
    }

    /**
     * Parses this field's text from a record.
     * @param record the record to parse
     * @return the parsed field text
     */
    private String getFieldText(Record record) {
        String recordText = record.getRecordText();

        int position = getCurrentPosition(record);
        int recordLength = recordText.length();
        if (recordLength <= position) {
            return null;
        }

        return recordText.substring(position, Math.min(recordLength, position + getLength()));
    }
}
