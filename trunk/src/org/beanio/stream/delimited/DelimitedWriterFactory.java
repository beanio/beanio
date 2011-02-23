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
    private Character escape = null;
    private String recordTerminator = null;

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordWriterFactory#createWriter(java.io.Writer)
     */
    public RecordWriter createWriter(Writer out) {
        return new DelimitedWriter(out, delimiter, escape, recordTerminator);
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
     * Returns whether escaping is enabled.  By default, escaping is disabled.
     * @return boolean <tt>true</tt> if escaping is enabled
     * @see #getEscape()
     */
    public boolean isEscapeEnabled() {
        return escape != null;
    }

    /**
     * Sets the escape character.  The escape character can only be used to
     * escape the delimiter or itself.  Set to null to disable escaping.
     * @param c the new escape character
     */
    public void setEscape(Character c) {
        this.escape = c;
    }

    /**
     * Returns the escape character.
     * @return the escape character or null if escaping is disabled
     */
    public Character getEscape() {
        return escape;
    }

    /**
     * Returns the text used to terminate a record.  By default, the record
     * terminator is set to the value of the <tt>line.separator</tt> system property.
     * @return the record termination text
     */
    public String getRecordTerminator() {
        return recordTerminator;
    }

    /**
     * Sets the text used to terminate a record.  If set to <tt>null</tt>, the 
     * the value of the <tt>line.separator</tt> system property is used to terminate
     * records.
     * @param lineSeparator the record termination text
     */
    public void setRecordTerminator(String recordTerminator) {
        this.recordTerminator = recordTerminator;
    }
    
    /**
     * Returns the text used to terminate a record.  By default, the
     * line separator is set using the 'line.separator' system property.
     * @return the line separation text
     * @deprecated
     */
    public String getLineSeparator() {
        return recordTerminator;
    }

    /**
     * Sets the text used to terminate a record.  If set to <tt>null</tt>, the default
     * line separator is used based on the 'line.separator' system property.
     * @param lineSeparator the line separation text
     * @deprecated
     */
    public void setLineSeparator(String lineSeparator) {
        this.recordTerminator = lineSeparator;
    }
}
