/*
 * Copyright 2010 Kevin Seim
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

import java.util.regex.Pattern;

import org.beanio.parser.*;

/**
 * A <tt>DelimitedFieldDefinition</tt> is used to parse and format fields for
 * delimited records.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DelimitedFieldDefinition extends FieldDefinition {

	@Override
	public boolean isMatch(Record record) {
		String text = getFieldText(record);
		if (text == null) {
			return false;
		}
		
		String literal = getLiteral();
		if (literal != null && !literal.equals(text)) {
			return false;
		}
		
		Pattern p = getRegexPattern();
		if (p != null && !p.matcher(text).matches()) {
			return false;
		}
			
		return true;
	}
	
	@Override
	public String parseField(Record record) {
		String text = getFieldText(record);
		// update the record with the raw field text
		record.setFieldText(getName(), text);
		return text;
	}
	
	
	private String getFieldText(Record record) {
		DelimitedRecord rec = ((DelimitedRecord)record);
		return (getPosition() < rec.getFieldCount()) ?  
			rec.getFieldText(getPosition()) : null;
	}
}
