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
package org.beanio.parser;

import java.beans.PropertyDescriptor;
import java.util.regex.*;

import org.beanio.*;
import org.beanio.types.*;
import org.beanio.util.TypeUtil;

/**
 * A <tt>FieldDefinition</tt> is used to parse and format field values that
 * make up a record or bean.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class FieldDefinition extends PropertyDefinition {

    /** Left justification */
    public static final char LEFT = 'L';
    /** Right justification */
    public static final char RIGHT = 'R';

    private int position = -1;
    private boolean trim = true;
    private boolean required = false;
    private boolean property = false;
    private int minLength = 0;
    private int maxLength = -1; // -1 for unbounded
    private String literal = null;
    private Pattern regex;

    private TypeHandler handler;
    private Object defaultValue;
    
    /* padded field settings */
    private boolean padded = false;
    private char padding = ' ';
    private int paddedLength = 0; // 0 if padding is disabled
    private char justification = LEFT;
    private String defaultText = "";
    
    @Override
    public boolean isField() {
        return true;
    }
    
    /**
     * Tests if the field text in the record matches this field definition.
     * @param record the record containing the field to test
     * @return <tt>true</tt> if the text is a match
     */
    public abstract boolean matches(Record record);

    @Override
    public boolean defines(Object value) {
        return isMatch(value);
    }

    /**
     * Tests if the given field value matches this field definition.
     * @param value the field value to test
     * @return <tt>true</tt> if the value matched, <tt>false</tt> otherwise
     */
    public boolean isMatch(Object value) {
        if (value == null) {
            return false;
        }
        
        if (!TypeUtil.isAssignable(getPropertyType(), value.getClass())) {
            return false;
        }
        
        String text = (handler == null) ? value.toString() : handler.format(value);
        return isMatch(text);
    }
    
    /**
     * Returns <tt>true</tt> if the provided field text is a match for this field
     * definition based on the configured literal value or regular expression.
     * @param text the field text to test
     * @return <tt>true</tt> if the field text matches this field definitions constraints,
     *   or <tt>false</tt> if the field text is null or does not match
     */
    protected boolean isMatch(String text) {
        if (text == null)
            return false;
        if (literal != null && !literal.equals(text))
            return false;
        if (regex != null && !regex.matcher(text).matches())
            return false;
        return true;
    }

    /**
     * Parses the raw field text from a record prior to any validation and sets
     * the text on the record. 
     * @param record the record to parse
     * @return the parsed field text, or {@link #INVALID} if the field is invalid,
     *   or <tt>null</tt> if the field is not present in the record
     */
    protected abstract String parseField(Record record);

    /**
     * Parses and validates a field property value from the record.
     * @param record the record to parse
     * @return the parsed field value, or {@link #INVALID} if the field was invalid,
     *   or {@link #MISSING} if the field was not present in the record
     */
    @Override
    protected Object parsePropertyValue(Record record) {
        // parse the field text from the record
        String fieldText = parseField(record);
        if (fieldText == INVALID) {
            return INVALID;
        }

        return parsePropertyValue(record, fieldText);
    }
    
    /**
     * Parses and validates a field property value from the given field text.
     * @param record the record being parsed
     * @param fieldText the field text to parse
     * @return the parsed field value, or {@link #INVALID} if the field was invalid,
     *   or {@link #MISSING} if the field was not present in the record
     */
    protected Object parsePropertyValue(Record record, String fieldText) {
        boolean valid = true;
        String text = fieldText;
        
        // null field text means the field was not present in the record
        if (text == null) {
            // if this field is a collection, return MISSING 
            if (isCollection()) {
                return MISSING;
            }
        }
        else if (trim) {
            // trim if configured
            text = text.trim();
        }
        
        // check if field exists
        if (text == null || "".equals(text)) {
            // validation for required fields
            if (required) {
                record.addFieldError(getName(), fieldText, "required");
                valid = false;
            }
            // return the default value if set
            else if (defaultValue != null) {
                return defaultValue;
            }
            else if (text == null) {
                return MISSING;
            }
        }
        else {
            // validate constant fields
            if (literal != null && !literal.equals(text)) {
                record.addFieldError(getName(), fieldText, "literal", literal);
                valid = false;
            }
            // validate minimum length
            if (minLength > -1 && text.length() < minLength) {
                record.addFieldError(getName(), fieldText, "minLength", minLength, maxLength);
                valid = false;
            }
            // validate maximum length
            if (maxLength > -1 && text.length() > maxLength) {
                record.addFieldError(getName(), fieldText, "maxLength", minLength, maxLength);
                valid = false;
            }
            // validate the regular expression
            if (regex != null && !regex.matcher(text).matches()) {
                record.addFieldError(getName(), fieldText, "regex", regex.pattern());
                valid = false;
            }
        }

        // type conversion is skipped if the text does not pass other validations
        if (!valid) {
            return INVALID;
        }
        
        // perform type conversion and return the result
        try {
            // if there is no type handler, assume its a String
            Object value = (handler == null) ? text : handler.parse(text);
            
            // validate primitive values are not null
            PropertyDescriptor propertyDescriptor = getPropertyDescriptor();
            if (value == null && isProperty() && propertyDescriptor != null) {
                if (isArray()) {
                    if (propertyDescriptor.getPropertyType().getComponentType().isPrimitive()) {
                        record.addFieldError(getName(), fieldText, "type",
                            "Primitive array value cannot be null");
                        return INVALID;
                    }
                }
                else {
                    if (propertyDescriptor.getPropertyType().isPrimitive()) {
                        record.addFieldError(getName(), fieldText, "type",
                            "Primitive bean property cannot be null");
                        return INVALID;
                    }
                }
            }
            
            return value;
        }
        catch (TypeConversionException ex) {
            record.addFieldError(getName(), fieldText, "type", ex.getMessage());
            return INVALID;
        }
        catch (Exception ex) {
            throw new BeanReaderIOException(record.getContext(), 
                "Type conversion failed for field '" + getName() + 
                "' while parsing text '" + fieldText + "'", ex);
        }
    }

    /**
     * Formats the field value.
     * @param value the field value to format
     * @return the formatted field text
     */
    public String formatValue(Object value) {
        if (literal != null) {
            return literal;
        }
        
        String text = null;
        if (handler != null) {
            try {
                text = handler.format(value);
                
                if (text == TypeHandler.NIL) {
                    if (isNilSupported()) {
                        return text;
                    }
                    text = null;
                }
            }
            catch (Exception ex) {
                throw new BeanWriterIOException("Type conversion failed for field '" +
                    getName() + "' while formatting value '" + value + "'", ex);
            }
        }
        else if (value != null) {
            text = value.toString();
        }

        return formatText(text);
    }
    
    /**
     * Returns whether {@link #formatValue(Object)} may return {@link TypeHandler#NIL}.  Returns
     * <tt>false</tt> by default, which will convert <tt>TypeHandler.NIL</tt> to <tt>null</tt>.
     * @return <tt>true</tt> if {@link #formatValue(Object)} may return {@link TypeHandler#NIL}.
     */
    protected boolean isNilSupported() {
        return false;
    }
    
    /**
     * Formats field text.  If the padded length of this field is greater than 0, 
     * text will be truncated if it exceeds the length, or padded with the padding 
     * character if it doesn't.
     * @param text the field text to format
     * @return the formatted field text
     */
    protected String formatText(String text) {
        int toLength = getPaddedLength();
        if (toLength <= 0) {
            return text == null ? "" : text;
        }
        
        // handle padded fields...
        
        int fromLength;
        if (text == null) {
            // if the field isn't required, then we allow padding
            // to be skipped
            if (!isRequired()) {
                return formatPaddedNull();
            }
            text = "";
            fromLength = 0;
        }
        else {
            fromLength = text.length();
            if (fromLength > toLength) {
                return text.substring(0, toLength);
            }
            else if (fromLength == toLength) {
                return text;
            }
        }
    
        int remaining = toLength - fromLength;
        StringBuilder s = new StringBuilder(toLength);
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
     * Returns the field text for a padded field when the property value
     * is null.
     * @return field text for a null property
     */
    protected String formatPaddedNull() {
        return "";
    }
    
    /**
     * Removes padding from the field text.
     * @param fieldText the field text to remove padding
     * @return the unpadded field text
     */
    protected String unpad(String fieldText) {
        int length = fieldText.length();
        
        if (justification == LEFT) {
            int index = fieldText.length();
            while (true) {
                --index;
                
                if (index < 0) {
                    return defaultText;
                }
                else if (fieldText.charAt(index) != padding) {
                    if (index == (length - 1))
                        return fieldText;
                    else
                        return fieldText.substring(0, index + 1);
                }
            }
        }
        else {
            int index = 0;
            while (index < length) {
                if (fieldText.charAt(index) != padding) {
                    if (index == 0)
                        return fieldText;
                    else
                        return fieldText.substring(index, length);
                }
                index++;
            }
            return defaultText;
        }
    }
        
    /**
     * Returns the position of this field within the record.
     * @return the field position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the position of this field within the record beginning at <tt>0</tt>.
     * @param position the field position, starting at <tt>0</tt>
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns <tt>true</tt> if the field text will be trimmed before
     * validation and type conversion.
     * @return <tt>true</tt> if the field text will be trimmed
     */
    public boolean isTrim() {
        return trim;
    }

    /**
     * Set to <tt>true</tt> if the field text should be trimmed before
     * validation and type conversion.
     * @param trim set to <tt>true</tt> to trim the field text
     */
    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    /**
     * Returns the textual literal value the field text must match, or <tt>null</tt> if
     * no literal validation will be performed.
     * @return literal field text
     */
    public String getLiteral() {
        return literal;
    }

    /**
     * Sets the literal text this field must match.  If set to <tt>null</tt>, no
     * literal validation is performed.
     * @param literal the literal field text
     */
    public void setLiteral(String literal) {
        this.literal = literal;
    }

    /**
     * Returns the type handler for this field.  May be <tt>null</tt> if the
     * field value is of type <tt>String</tt>.
     * @return the field type handler
     */
    public TypeHandler getTypeHandler() {
        return handler;
    }

    /**
     * Sets the type handler for this field.  May be set to <tt>null</tt> if the
     * field value is of type <tt>String</tt>.
     * @param handler the new type handler
     */
    public void setTypeHandler(TypeHandler handler) {
        this.handler = handler;
    }

    /**
     * Returns <tt>true</tt> if this field is required.  Required fields cannot
     * match the empty String.  Note that trimming is performed before the required
     * validation is performed. 
     * @return <tt>true</tt> if this field is required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets to <tt>true</tt> if this field is required.  Required fields cannot
     * match the empty String.  Note that trimming is performed before the required
     * validation is performed. 
     * @param required <tt>true</tt> if this field is required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Returns the minimum length in characters of the field text allowed by this field
     * definition after trimming is performed..
     * @return the minimum field length in characters
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length in characters of the field text allowed by this field
     * definition after trimming is performed..
     * @param minLength the minimum length in characters
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum length in characters of the field text allowed by this field
     * definition after trimming is performed.
     * @return the maximum field length in characters
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length in characters of the field text allowed by this field
     * definition after trimming is performed.
     * @param maxLength
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Returns <tt>true</tt> if the value parsed by this field definition is a property
     * of the record bean.
     * @return <tt>true</tt> if the value parsed by this field definition is a property
     *   of the record bean
     */
    public boolean isProperty() {
        return property;
    }

    /**
     * Set to <tt>true</tt> if the value parsed by this field definition is a property
     * of the record bean.
     * @param property <tt>true</tt> if the value parsed by this field definition is a property
     *   of the record bean
     */
    public void setProperty(boolean property) {
        this.property = property;
    }

    /**
     * Returns the regular expression pattern the field text parsed by this field
     * definition must match.
     * @return the regular expression pattern
     */
    public String getRegex() {
        return regex == null ? null : regex.pattern();
    }

    /**
     * Sets the regular expression pattern the field text parsed by this field
     * definition must match.
     * @param pattern the regular expression pattern
     * @throws PatternSyntaxException if the pattern is invalid
     */
    public void setRegex(String pattern) throws PatternSyntaxException {
        if (pattern == null)
            this.regex = null;
        else
            this.regex = Pattern.compile(pattern);
    }

    /**
     * Returns the regular expression the field text parsed by this field
     * definition must match.
     * @return the regular expression
     */
    protected Pattern getRegexPattern() {
        return regex;
    }

    /**
     * Returns the default value for a field parsed by this field definition
     * when the field text is null or the empty string (after trimming).
     * @return default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the default value for a field parsed by this field definition
     * when the field text is null or the empty string (after trimming).
     * @param defaultValue the default value
     */
    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    @Override
    public void setPropertyType(Class<?> type) {
        super.setPropertyType(type);
        this.defaultText = getDefaultTextFor(type, padding);
    }

    /**
     * Returns whether this field is padded.
     * @return <tt>true</tt> if this field is padded
     * @since 1.1.1
     */
    public boolean isPadded() {
        return padded;
    }

    /**
     * Sets whether this field is padded.
     * @param padded <tt>true</tt> if this field is padded
     * @since 1.1.1
     */
    public void setPadded(boolean padded) {
        this.padded = padded;
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
        this.defaultText = getDefaultTextFor(getPropertyType(), padding);
    }

    /**
     * Returns the padded length of this field, or 0 if padding
     * is disabled.
     * @return the padded length of this field
     */
    public int getPaddedLength() {
        return paddedLength;
    }
    
    /**
     * Sets the padded length of this field.  If set to 0, padding
     * is disabled.
     * @param length the new padded length of this field
     */
    public void setPaddedLength(int length) {
        this.paddedLength = length;
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
    
    /**
     * Returns default unpadded text when the entire field is populated by
     * the padding character.  Returns the empty string by default, or the padding
     * character if one of the following conditions apply:
     * <ul>
     * <li>The field property type extends from <tt>Number</tt> and the padding character
     *  is a digit.</li>
     * <li>The field property is of type <tt>Character</tt>.
     * </ul>
     * @param type the field property type
     * @param padding the character used to pad the fixed length field
     * @return the default text for a fully padded field
     */
    protected String getDefaultTextFor(Class<?> type, char padding) {
        if (type == null) {
            return "";
        }
        else if (Character.class.equals(type)) {
            return Character.toString(padding);
        }
        else if (Number.class.isAssignableFrom(type)) {
            if (Character.isDigit(padding)) {
                return Character.toString(padding);
            }
        }
        return "";
    }
}
