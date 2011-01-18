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

    private int length;
    private char padding = ' ';
    private char justification = LEFT;

    @Override
    public boolean matches(Record record) {
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
        else if (fieldText.length() != length) {
            record.addFieldError(getName(), fieldText, "length", length);
            return null;
        }
        else {
            return unpad(fieldText);
        }
    }

    @Override
    public String formatValue(Object value) {
        String text = super.formatValue(value);

        int textWidth = text.length();
        if (textWidth > length) {
            return text.substring(0, length);
        }
        else if (textWidth == length) {
            return text;
        }

        int remaining = length - textWidth;
        StringBuffer s = new StringBuffer(length);
        if (justification == LEFT) {
            s.append(text);
            for (int i = 0; i < remaining; i++) {
                s.append(padding);
            }
        }
        else {
            for (int i = 0; i < remaining; i++) {
                s.append(padding);
            }
            s.append(text);
        }
        return s.toString();
    }

    /**
     * Parses this field's text from a record.
     * @param record the record to parse
     * @return the parsed field text
     */
    private String getFieldText(Record record) {
        String recordText = record.getRecordText();

        int position = getPosition();
        int recordLength = recordText.length();
        if (recordLength <= position) {
            return null;
        }

        return recordText.substring(position, Math.min(recordLength, position + length));
    }

    /**
     * Removes padding from the field text.
     * @param fieldText the field text to remove padding
     * @return the unpadded field text
     */
    private String unpad(String fieldText) {
        int start = 0;
        int end = length - 1;
        boolean modified = false;

        if (justification == LEFT) {
            while (end > start && fieldText.charAt(end) == padding) {
                modified = true;
                --end;
            }
        }
        else {
            while (start < end && fieldText.charAt(start) == padding) {
                modified = true;
                ++start;
            }
        }

        if (!modified) {
            return fieldText;
        }
        else if (start == end) {
            return "";
        }
        else {
            return fieldText.substring(start, end + 1);
        }
    }

    /**
     * Returns the length of this field.
     * @return the field length
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the length of this field
     * @param length the field length
     */
    public void setLength(int length) {
        this.length = length;
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
        if (justification != LEFT && justification != RIGHT) {
            throw new IllegalArgumentException("Invalid justification: " + justification);
        }
        this.justification = justification;
    }
}
