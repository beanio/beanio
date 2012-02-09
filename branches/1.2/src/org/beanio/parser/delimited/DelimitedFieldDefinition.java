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
import org.beanio.parser.flat.FlatFieldDefinition;

/**
 * A <tt>DelimitedFieldDefinition</tt> is used to parse and format fields for
 * delimited records.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DelimitedFieldDefinition extends FlatFieldDefinition {

	@Override
	public boolean matches(Record record) {
	    if (!isRecordIdentifier())
	        return true;
	    
		return isMatch(getFieldText(record));
	}
	
	@Override
	public String parseField(Record record) {
		String text = getFieldText(record);
		
		// update the record with the raw field text
		record.setFieldText(getName(), text);
		
		if (text == null) {
		    return null;
		}
		else if (isPadded()) {
		    if (text.length() == 0) {
		        // this will either cause a required validation error or map
		        // to a null value depending on the value of 'required'
		        return "";
		    }
            else if (getPaddedLength() > 0 && text.length() != getPaddedLength()) {
                record.addFieldError(getName(), text, "length", getPaddedLength());
                return INVALID;
            }
            else {
                return unpad(text);
            }
		}
		else {
		    return text;
		}
	}
	
	private String getFieldText(Record record) {
	    int pos = getCurrentPosition(record);
		DelimitedRecord rec = ((DelimitedRecord)record);
		return (pos < rec.getFieldCount()) ? rec.getFieldText(pos) : null;
	}
	
	@Override
    public int getLength() {
        return 1;
    }
}
