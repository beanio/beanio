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

public class FieldConfig {

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
    private String defaultValue;
    private int length = -1;
    private char padding = ' ';
    private String justify = "left";

    /**
     * Returns the default textual representation of the value of
     * this field when the field is not present in the input stream.
     * May be <tt>null</tt>.
     * @return the default value (as text)
     */
    public String getDefault() {
        return defaultValue;
    }

    public void setDefault(String s) {
        this.defaultValue = s;
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

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum length of this field in characters, or <tt>null</tt>
     * if a maximum length should not be enforced.
     * @return the maximum length, or <tt>null</tt> if not enforced
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    /**
     * Returns <tt>true</tt> if this field's text will be trimmed.
     * @return <tt>true</tt> if field text should be trimmed
     */
    public boolean isTrim() {
        return trim;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    /**
     * Returns <tt>true</tt> if this field is used to identify the record type.
     * @return <tt>true</tt> if this field is used to identify the record type.
     */
    public boolean isRecordIdentifier() {
        return recordIdentifier;
    }

    public void setRecordIdentifier(boolean key) {
        this.recordIdentifier = key;
    }

    /**
     * Returns <tt>true</tt> if this field is not a bean property.
     * @return <tt>true</tt> if this field is not a bean property.
     */
    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignore) {
        this.ignored = ignore;
    }

    /**
     * Returns the static text for this field, or <tt>null</tt> if
     * the field text is not static.  If defined, the field text must
     * match the literal value when reading the input stream, and the
     * likewise, the literal value will be written to the output stream.
     * @return the literal value of the field
     */
    public String getLiteral() {
        return literal;
    }

    public void setLiteral(String literal) {
        this.literal = literal;
    }

    /**
     * Returns the regular expression pattern for validating this field's textual
     * value when read from an input stream.  Field text is only validated if its
     * not the empty string after trimming (if enabled). 
     * @return the regular expression pattern
     */
    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
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

    public void setSetter(String setter) {
        this.setter = setter;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public char getPadding() {
        return padding;
    }

    public void setPadding(char padding) {
        this.padding = padding;
    }

    public String getJustify() {
        return justify;
    }

    public void setJustify(String justify) {
        this.justify = justify;
    }
}
