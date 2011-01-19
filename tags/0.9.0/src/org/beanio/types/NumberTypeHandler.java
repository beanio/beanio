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
 * Base class for type handlers that parse objects extending from <tt>Number</tt>.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class NumberTypeHandler implements TypeHandler {

    /**
     * Parses a <tt>Number</tt> from the given text.
     * @param text the text to parse
     * @return the parsed Number, or null if <tt>text</tt> was <tt>null</tt>
     *    or an empty string
     * @throws TypeConversionException if the text is not a valid number
     */
    public final Number parse(String text) throws TypeConversionException {
        if (text == null || "".equals(text)) {
            return null;
        }

        try {
            return createNumber(text);
        }
        catch (NumberFormatException ex) {
            throw new TypeConversionException("Invalid number '" + text + "'", ex);
        }
    }

    /**
     * Parses a <tt>Number</tt> from text.
     * @param text the text to convert to a Number
     * @return the parsed Number
     * @throws NumberFormatException if the text is not a valid number
     */
    protected abstract Number createNumber(String text) throws NumberFormatException;

    /**
     * Formats a <tt>Number</tt> by calling <tt>toString()</tt>.  If <tt>value</tt> is
     * null, <tt>null</tt> is returned.
     * @param value the number to format
     * @return the formatted number
     */
    public String format(Object value) {
        if (value == null)
            return null;
        else
            return ((Number) value).toString();
    }
}
