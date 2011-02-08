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

    private int position = -1;
    private boolean recordIdentifier = false;
    private boolean trim = true;
    private boolean required = false;
    private boolean property = false;
    private int minLength = 0;
    private int maxLength = -1; // -1 for unbounded
    private String literal = null;
    private Pattern regex;

    private TypeHandler handler;
    private Object defaultValue;
    
    /**
     * Tests if the field text in the record matches this field definition.
     * @param record the record containing the field to test
     * @return <tt>true</tt> if the text is a match
     */
    public abstract boolean matches(Record record);

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
        boolean valid = true;

        // parse the field text from the record
        String fieldText = parseField(record);
        if (fieldText == INVALID) {
            return INVALID;
        }

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
     * Formats field text.  Converts <tt>null</tt> to the empty string.
     * @param text the field text to format
     * @return the formatted field text
     */
    protected String formatText(String text) {
        return text == null ? "" : text;
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
     * Returns <tt>true</tt> if this field is used to identify the
     * record type.
     * @return <tt>true</tt> if this fields is used to identify the record type
     */
    public boolean isRecordIdentifier() {
        return recordIdentifier;
    }

    /**
     * Set to <tt>true</tt> if this field should be used to identify the record type.
     * @param b set to <tt>true</tt> to use this field to identify the reocrd type
     */
    public void setRecordIdentifier(boolean b) {
        this.recordIdentifier = b;
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
}
