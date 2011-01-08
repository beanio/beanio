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

import java.io.Reader;

import org.beanio.stream.*;

/**
 * This record reader factory is used to create and configure a <tt>DelimitedReader</tt>.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see DelimitedReader
 */
public class DelimitedReaderFactory implements RecordReaderFactory {

    private char delimiter = '\t';
    private char escape = '\\';
    private char lineContinuationCharacter = '\\';
    private boolean escapeEnabled = false;
    private boolean lineContinuationEnabled = false;

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordReaderFactory#createReader(java.io.Reader)
     */
    public RecordReader createReader(Reader in) {
        Character e = null;
        if (escapeEnabled) {
            e = escape;
        }
        Character lineContinuation = null;
        if (lineContinuationEnabled) {
            lineContinuation = lineContinuationCharacter;
        }

        return new DelimitedReader(in, delimiter, e, lineContinuation);
    }

    /**
     * Returns the field delimiting character.  Defaults to tab.
     * @return the record delimiter
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * Sets the field delimiting character to use.
     * @param delimiter the record delimiting character
     */
    public void setDelimiter(char delimiter) {
        this.delimiter = delimiter;
    }

    /**
     * Returns the character used by the input stream to escape delimiters and itself.
     * The default escape character is the backslash, '\'. 
     * @return the escape character
     */
    public char getEscape() {
        return escape;
    }

    /**
     * Sets the character used by the input stream to escape delimiters and itself.
     * @param escapeCharacter the new escape character
     */
    public void setEscape(char escapeCharacter) {
        this.escape = escapeCharacter;
    }

    /**
     * Returns the line continuation character.  By default, the line continuation
     * character is the backslash.
     * @return the line continuation character
     */
    public char getLineContinuationCharacter() {
        return lineContinuationCharacter;
    }

    /**
     * Sets the line continuation character.
     * @param lineContinuationCharacter the line continuation character
     */
    public void setLineContinuationCharacter(char lineContinuationCharacter) {
        this.lineContinuationCharacter = lineContinuationCharacter;
    }

    /**
     * Returns whether the escape character is enabled.
     * @return <tt>true</tt> if enabled, false otherwise
     */
    public boolean isEscapeEnabled() {
        return escapeEnabled;
    }

    /**
     * Sets whether the escape character is enabled.
     * @param escapeEnabled set to <tt>true</tt> to enable the escape character
     */
    public void setEscapeEnabled(boolean escapeEnabled) {
        this.escapeEnabled = escapeEnabled;
    }

    /**
     * Returns whether the line continuation character is enabled.
     * @return <tt>true</tt> if the line continuation character is enabled
     */
    public boolean isLineContinationEnabled() {
        return lineContinuationEnabled;
    }

    /**
     * Sets whether the line continuation character is enabled.
     * @param enabled set to <tt>true</tt> to enable the line continuation character
     */
    public void setLineContinuationEnabled(boolean enabled) {
        this.lineContinuationEnabled = enabled;
    }
}
