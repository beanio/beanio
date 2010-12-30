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
package org.beanio.parser.fixedlength;

import org.beanio.parser.*;

/**
 * A <tt>FixedLengthFieldDefinition</tt> is used to parse and format fields for
 * fixed length records.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthFieldDefinition extends FieldDefinition {

	/** Left justification */
	public static final char LEFT = 'L';
	/** Right justification */
	public static final char RIGHT = 'R';
	
	private int width;
	private char padding = ' ';
	private char justification = LEFT;
	
	@Override
	public boolean isMatch(Record record) {
		return isMatch(getFieldText(record));
	}

	@Override
	protected String parseField(Record record) {
		String fieldText = getFieldText(record);
		record.setFieldText(getName(), fieldText);
		return fieldText;
	}
	
	@Override
	public String formatValue(boolean isMap, Object bean) {
		String text = super.formatValue(isMap, bean);
		
		int textWidth = text.length();
		if (textWidth > width) {
			return text.substring(0, width);
		}
		else if (textWidth == width) {
			return text;
		}
		int remaining = width - textWidth;
		StringBuffer s = new StringBuffer(width);
		if (justification == LEFT) {
			s.append(text);
			for (int i=0; i<remaining; i++) {
				s.append(padding);
			}
		}
		else {
			for (int i=0; i<remaining; i++) {
				s.append(padding);
			}
			s.append(text);
		}
		return text;
	}
	
	/**
	 * Parses this field's text from a record.
	 * @param record the record to parse
	 * @return the parsed field text
	 */
	private String getFieldText(Record record) {
		int position = getPosition();
		
		String text = record.getRecordText();
		if ((position + width) > text.length())
			return null;
		
		int start = position;
		int end = position + width - 1;
		
		if (justification == LEFT) {
			while (end > start && text.charAt(end) == padding) {
				--end;
			}
		}
		else {
			while (start < end && text.charAt(start) == padding) {
				++start;
			}
		}
		
		if (start == end)
			return "";
		
		return text.substring(start, end + 1);
	}

	/**
	 * Returns the width of this field.
	 * @return the field width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the width of this field
	 * @param width the field width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Returns the character used to pad this field.
	 * @return the padding character
	 */
	public char getPadding() {
		return padding;
	}

	/**
	 * Sets the character used to pad this field.
	 * @param padding the padding character
	 */
	public void setPadding(char padding) {
		this.padding = padding;
	}

	/**
	 * Returns the text justification for this field.
	 * @return the text justification, {@link #LEFT} or {@link #RIGHT}
	 */
	public char getJustification() {
		return justification;
	}

	/**
	 * Sets the text justification for this field.
	 * @param justification the text justification, {@link #LEFT} or {@link #RIGHT}
	 */
	public void setJustification(char justification) {
		this.justification = justification;
	}
}
