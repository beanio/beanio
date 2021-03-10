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
package org.beanio.types;

/**
 * A type handler implementation for the <code>String</code> class.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class StringTypeHandler implements TypeHandler {

    private boolean trim = false;
    private boolean nullIfEmpty = false;

    /**
     * Parses a <code>String</code> from the given text.
     * @param text the text to parse
     * @return the parsed <code>String</code>
     */
    public String parse(String text) {
        if (text != null) {
            if (trim) {
                text = text.trim();
            }
            if (nullIfEmpty && text.length() == 0) {
                text = null;
            }
        }
        return text;
    }

    /**
     * Formats the value by calling {@link Object#toString()}.
     * @param value the value to format
     * @return the formatted value, or <code>null</code> if <code>value</code> is <code>null</code>
     */
    public String format(Object value) {
        if (value == null)
            return null;
        return value.toString();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.types.TypeHandler#getType()
     */
    public Class<?> getType() {
        return String.class;
    }

    /**
     * Returns <code>true</code> if <code>parse(String)</code> should trim the text.
     * By default, <code>trim</code> is <code>false</code> which allows trimming to
     * be controlled by the field definition.
     * @return <code>true</code> if parsed text is trimmed
     */
    public boolean isTrim() {
        return trim;
    }

    /**
     * Set to <code>true</code> to trim text when parsing.
     * @param trim <code>true</code> if text should be trimmed when parsed
     */
    public void setTrim(boolean trim) {
        this.trim = trim;
    }

    /**
     * Returns <code>true</code> if empty string values are parsed as <code>null</code>.
     * Defaults to <code>false</code>.
     * @return <code>true</code> to convert the empty string to <code>null</code>
     */
    public boolean isNullIfEmpty() {
        return nullIfEmpty;
    }

    /**
     * Set to <code>true</code> if the parsed empty strings should be converted to <code>null</code>.
     * @param nullIfEmpty <code>true</code> to convert empty string to <code>null</code>
     */
    public void setNullIfEmpty(boolean nullIfEmpty) {
        this.nullIfEmpty = nullIfEmpty;
    }
}
