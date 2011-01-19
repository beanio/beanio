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
    private Character escape = null;
    private Character lineContinuationCharacter = null;

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordReaderFactory#createReader(java.io.Reader)
     */
    public RecordReader createReader(Reader in) {
        return new DelimitedReader(in, delimiter, escape, lineContinuationCharacter);
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
     * By default, escaping is disabled.
     * @return the escape character or <tt>null</tt> if escaping is disabled
     */
    public Character getEscape() {
        return escape;
    }

    /**
     * Sets the character used by the input stream to escape delimiters and itself.
     * If set to null, escaping is disabled.
     * @param escapeCharacter the new escape character
     */
    public void setEscape(Character escapeCharacter) {
        this.escape = escapeCharacter;
    }

    /**
     * Returns the line continuation character or <tt>null</tt> if line
     * continuation is disabled.  By default, line continuation is disabled.
     * @return the line continuation character or <tt>null</tt> if disabled
     */
    public Character getLineContinuationCharacter() {
        return lineContinuationCharacter;
    }

    /**
     * Sets the line continuation character.  May be set to <tt>null</tt> to
     * disable line continuation.
     * @param lineContinuationCharacter the line continuation character
     */
    public void setLineContinuationCharacter(Character lineContinuationCharacter) {
        this.lineContinuationCharacter = lineContinuationCharacter;
    }

    /**
     * Returns whether an escape character is enabled.
     * @return <tt>true</tt> if enabled, false otherwise
     */
    public boolean isEscapeEnabled() {
        return escape != null;
    }

    /**
     * Returns whether the line continuation character is enabled.
     * @return <tt>true</tt> if the line continuation character is enabled
     */
    public boolean isLineContinationEnabled() {
        return lineContinuationCharacter  != null;
    }
}
