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
package org.beanio.stream.delimited;

import java.io.Writer;

import org.beanio.stream.*;

/**
 * This record writer factory is used to create and configure a <tt>DelimitedWriter</tt>.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see DelimitedWriter
 */
public class DelimitedWriterFactory implements RecordWriterFactory {

    private char delimiter = '\t';
    private char escape = '\\';
    private boolean escapeEnabled = false;
    private String lineSeparator = null;

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordWriterFactory#createWriter(java.io.Writer)
     */
    public RecordWriter createWriter(Writer out) {
        if (escapeEnabled && delimiter == escape)
            throw new IllegalArgumentException("Delimiter cannot match the escape character");

        Character e = null;
        if (escapeEnabled) {
            e = escape;
        }

        return new DelimitedWriter(out, delimiter, e, lineSeparator);
    }

    /**
     * Sets the field delimiter.  By default, the delimiter is a comma.
     * @param c the character used to delimit fields
     */
    public void setDelimiter(char c) {
        this.delimiter = c;
    }

    /**
     * Returns the field delimiter.  By default, the delimiter is a comma.
     * @return the character used to delimit fields
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * Set to <tt>false</tt> to disable the escape character.  By default, escaping is enabled.
     * @param escapeEnabled set to <tt>true</tt> to enable escaping or <tt>false</tt> to disable it
     * @see #getEscape()
     */
    public void setEscapeEnabled(boolean escapeEnabled) {
        this.escapeEnabled = escapeEnabled;
    }

    /**
     * Returns whether the escaping is enabled.  By default, escaping is enabled.
     * @return boolean <tt>true</tt> if escaping is enabled
     * @see #getEscape()
     */
    public boolean isEscapeEnabled() {
        return escapeEnabled;
    }

    /**
     * Sets the escape character.  Quotation marks are escaped within quoted values
     * using the escape character.
     * @param c the new escape character
     */
    public void setEscape(char c) {
        this.escape = c;
    }

    /**
     * Returns the escape character.  Quotation marks are escaped within quoted values
     * using the escape character.
     * @return the escape character
     */
    public char getEscape() {
        return escape;
    }

    /**
     * Returns the text used to terminate a record.  By default, the
     * line separator is set using the 'line.separator' system property.
     * @return the line separation text
     */
    public String getLineSeparator() {
        return lineSeparator;
    }

    /**
     * Sets the text used to terminate a record.  If set to <tt>null</tt>, the default
     * line separator is used based on the 'line.separator' system property.
     * @param lineSeparator the line separation text
     */
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }
}
