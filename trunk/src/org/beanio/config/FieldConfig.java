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
package org.beanio.config;

/**
 * Stores configuration settings for field.  Fields are used
 * to define the layout of a record.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FieldConfig {

    /** Left justification setting */
    public static final String LEFT = "left";
    /** Right justification setting */
    public static final String RIGHT = "right";
    
    private String name;
    private int position = -1;
    private Integer minLength;
    private Integer maxLength; // set to -1 for unbounded
    private boolean trim = false;
    private boolean required = false;
    private boolean recordIdentifier = false;
    private boolean ignored = false;
    private String literal;
    private String regex;
    private String getter;
    private String setter;
    private String type;
    private String handler;
    private String format;
    private String defaultValue;
    private int length = -1;
    private char padding = ' ';
    private String justify = LEFT;

    /**
     * Returns the default textual representation of the value of
     * this field when the field is not present in the input stream.
     * May be <tt>null</tt>.
     * @return the default value (as text)
     */
    public String getDefault() {
        return defaultValue;
    }

    /**
     * Sets the default textual representation of the value of
     * this field when the field is not present in the input stream.
     * May be <tt>null</tt>.
     * @param text the default value (as text)
     */
    public void setDefault(String text) {
        this.defaultValue = text;
    }

    /**
     * Returns the name of this field.  The name of the field is
     * used to get and set the property value from the record bean
     * when a <tt>getter</tt> and <tt>setter</tt> are not set.
     * @return the field name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this field.  The name of the field is
     * used to get and set the property value from the record bean
     * when a <tt>getter</tt> and <tt>setter</tt> are not set.
     * @param name the field name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the position of this field.  For delimited record formats,
     * the position is the index (beginning at 0) of this field in the 
     * record.  For fixed length record formats, the position is the index
     * of the first character in the field.
     * @return the field position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the position of this field.  For delimited record formats,
     * the position is the index (beginning at 0) of this field in the 
     * record.  For fixed length record formats, the position is the index
     * of the first character in the field.
     * @param position the field position
     */
    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Returns the minimum length of this field in characters, or <tt>null</tt>
     * if a minimum length should not be enforced.
     * @return the minimum length, or <tt>null</tt> if not enforced
     */
    public Integer getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length of this field in characters, or <tt>null</tt>
     * if a minimum length should not be enforced.
     * @param minLength the minimum length, or <tt>null</tt> if not enforced
     */
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum length of this field in characters.  Returns
     * <tt>null</tt> if a maximum length will not be enforced.
     * @return the maximum length, or <tt>null</tt> if not enforced
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length of this field in characters.  Set to 
     * <tt>null</tt> if a maximum length should not be enforced.
     * @param maxLength the maximum length, or <tt>null</tt> if not enforced
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Returns <tt>true</tt> if field text will be trimmed before validation
     * and type conversion.
     * @return <tt>true</tt> if field text will be trimmed
     */
    public boolean isTrim() {
        return trim;
    }

    /**
     * Set to <tt>true</tt> if field text should be trimmed before validation
     * and type conversion.
     * @param trim <tt>true</tt> if field text will be trimmed
     */
    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    /**
     * Returns <tt>true</tt> if this field is used to identify the type
     * of record.
     * @return <tt>true</tt> if this field is used to identify the record
     */
    public boolean isRecordIdentifier() {
        return recordIdentifier;
    }

    /**
     * Set to <tt>true</tt> if this field is used to identify the type
     * of record.
     * @param b <tt>true</tt> if this field is used to identify the record
     */
    public void setRecordIdentifier(boolean b) {
        this.recordIdentifier = b;
    }

    /**
     * Returns <tt>true</tt> if this field is not a bean property.
     * @return <tt>true</tt> if this field is not a bean property.
     */
    public boolean isIgnored() {
        return ignored;
    }

    /**
     * Set to <tt>true</tt> if this field is not a bean property.
     * @param ignore <tt>true</tt> if this field is not a bean property.
     */
    public void setIgnored(boolean ignore) {
        this.ignored = ignore;
    }

    /**
     * Returns the static text for this field, or <tt>null</tt> if
     * the field text is not static.  If defined, the field text must
     * match the literal value when reading an input stream, and
     * likewise, the literal value will be written to an output stream.
     * @return the literal text of the field
     */
    public String getLiteral() {
        return literal;
    }

    /**
     * Sets the static text for this field.  Set to <tt>null</tt> if
     * the field text is not static.  If defined, the field text must
     * match the literal value when reading an input stream, and
     * likewise, the literal value will be written to an output stream.
     * @param literal the literal text of the field
     */
    public void setLiteral(String literal) {
        this.literal = literal;
    }

    /**
     * Returns the regular expression pattern for validating this field's text
     * when read from an input stream.  Field text is only validated if its
     * not the empty string after trimming (if enabled). 
     * @return the regular expression pattern
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Sets the regular expression pattern for validating this field's text
     * when read from an input stream.  Field text is only validated if its
     * not the empty string after trimming (if enabled). 
     * @param pattern the regular expression pattern
     */
    public void setRegex(String pattern) {
        this.regex = pattern;
    }

    /**
     * Returns <tt>true</tt> if this field is required when parsing an input
     * stream.  Required fields must have at least one character (after
     * trimming, if enabled).  If this field is not required and no text
     * is parsed from the input stream, no further validations are performed. 
     * @return <tt>true</tt> if this field is required
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Set to <tt>true</tt> if this field is required when parsing an input
     * stream.  Required fields must have at least one character (after
     * trimming, if enabled).  If this field is not required and no text
     * is parsed from the input stream, no further validations are performed. 
     * @param required <tt>true</tt> if this field is required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Returns the name of the getter method to use to retrieve this field's
     * value from the record bean when writing to an output stream.
     * @return the getter method for this field
     */
    public String getGetter() {
        return getter;
    }


    /**
     * Sets the name of the getter method to use to retrieve this field's
     * value from the record bean when writing to an output stream.
     * @param getter the getter method for this field
     */
    public void setGetter(String getter) {
        this.getter = getter;
    }

    /**
     * Returns the name of the setter method to use when setting this field's
     * value on the record bean while reading from an input stream.
     * @return the setter method for this field
     */
    public String getSetter() {
        return setter;
    }

    /**
     * Sets the name of the setter method to use when setting this field's
     * value on the record bean while reading from an input stream.
     * @param setter the setter method for this field
     */
    public void setSetter(String setter) {
        this.setter = setter;
    }

    /**
     * Returns the fully qualified class name or type alias of this field's
     * value.  By default, <tt>null</tt> is returned and the field value type
     * is detected through bean introspection.
     * @return the class name of this field value
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the fully qualified class name or type alias of this field's
     * value.  If set to <tt>null</tt>, the field value type is detected 
     * through bean introspection.
     * @param type the class name of this field value
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the name of the custom type handler used for type 
     * conversion by this field, or <tt>null</tt> if the default
     * type handler is sufficient.
     * @return the name of the custom type handler
     */
    public String getTypeHandler() {
        return handler;
    }

    /**
     * Sets the name of the custom type handler to use for type 
     * conversion by this field.  Set to <tt>null</tt> if the default
     * type handler is sufficient.
     * @param handler the name of the custom type handler
     */
    public void setTypeHandler(String handler) {
        this.handler = handler;
    }

    /**
     * Returns the format pattern used by date and number type handlers.
     * @return the date or number format pattern
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format pattern used by date and number type handlers.
     * @param format the date or number format pattern
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Returns the length of this field in characters.  Applies to
     * fixed length formatted streams only.  
     * @return the length of this field
     */
    public int getLength() {
        return length;
    }

    /**
     * Sets the length of this field in characters.  Applies to
     * fixed length formatted streams only.
     * @param length the length of this field
     */
    public void setLength(int length) {
        this.length = length;
    }

    /**
     * Returns the character used to pad this field.  Applies to
     * fixed length formatted streams only.
     * @return the character used to pad this field
     */
    public char getPadding() {
        return padding;
    }

    /**
     * Sets the character used to pad this field.  Applies to
     * fixed length formatted streams only.
     * @param padding the character used to pad this field
     */
    public void setPadding(char padding) {
        this.padding = padding;
    }

    /**
     * Returns the justification of this field.  Defaults to <tt>left</tt>.  
     * Applies to fixed length formatted streams only.
     * @return {@link #LEFT} or {@link #RIGHT}
     */
    public String getJustify() {
        return justify;
    }

    /**
     * Sets the justification of this field.  Applies to
     * fixed length formatted streams only.
     * @param justify {@link #LEFT} or {@link #RIGHT}
     */
    public void setJustify(String justify) {
        this.justify = justify;
    }
}
