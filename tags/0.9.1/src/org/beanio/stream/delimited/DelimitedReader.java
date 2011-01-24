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

import java.io.*;
import java.util.*;

import org.beanio.stream.*;

/**
 * A <tt>DelimitedReader</tt> is used to parse delimited flat files into
 * records of <tt>String</tt> arrays.  Fields must be delimited by a single configurable
 * character.
 * <p>
 * If an escape character is configured, the delimiting character can be
 * be escaped in a field by placing the escape character immediately before
 * the delimiter.  The escape character may also be used to escape itself.
 * For example, using a comma delimiter and backslash escape:
 * <pre>
 * Field1,Field2\,Field3,Field\\4,Field\5
 * </pre>
 * The record would be parsed as "Field1", "Field2,Field3", "Field\4", "Field\5"
 * <p>
 * Additionally, if a record may span multiple lines, a single line continuation
 * character can be configured.  The line continuation character will not be
 * included in any field text, but must be the last character on the line being
 * continued (other than a newline or carriage return).
 * For example, using a comma delimiter and backslash line continuation character:
 * <pre>
 * Field1,Field2\
 * Field3
 * <pre>
 * The 2 lines would be parsed in a single record as "Field1", "Field2", "Field3".
 * <p>
 * The same character can be used for line continuation and escaping, but neither
 * can match the delimiter.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DelimitedReader implements RecordReader {

    private char delim = '\t';
    private char escapeChar = '\\';
    private char lineContinuationChar = '\\';
    private boolean multilineEnabled = false;
    private boolean escapeEnabled = false;

    private transient Reader in;
    private transient String recordText;
    private transient int recordLineNumber;
    private transient int lineNumber = 0;
    private transient boolean skipLF = false;
    private transient List<String> fieldList = new ArrayList<String>();

    /**
     * Constructs a new <tt>DelimitedReader</tt> using a tab character for
     * the field delimiter.  Escaping is and line continuation characters
     * are disabled.
     * @param in the input stream to read from
     */
    public DelimitedReader(Reader in) {
        this(in, '\t', null, null);
    }

    /**
     * Constructs a new <tt>DelimitedReader</tt>.  Escaping is and line 
     * continuation characters are disabled.
     * @param in the input stream to read from
     * @param delimiter the field delimiting character
     */
    public DelimitedReader(Reader in, char delimiter) {
        this(in, delimiter, null, null);
    }

    /**
     * Constructs a new <tt>DelimitedReader</tt>.
     * @param in the input stream to read from
     * @param delimiter the field delimiting character
     * @param escapeChar the escape character, or null to disable escaping
     * @param lineContinuationCharacter the line continuation character,
     *   or <tt>null</tt> to disable line continuations
     * @throws IllegalArgumentException if the delimiter matches the escape character or
     *   or the line continuation character
     */
    public DelimitedReader(Reader in, char delimiter, Character escapeChar,
        Character lineContinuationCharacter) {
        if (escapeChar != null && delimiter == escapeChar) {
            throw new IllegalArgumentException("The field delimiter canot match the escape character");
        }
        if (lineContinuationCharacter != null && delimiter == lineContinuationCharacter) {
            throw new IllegalArgumentException("The field delimiter cannot match the line continuation character");
        }

        this.in = in;
        this.delim = delimiter;
        if (escapeChar != null) {
            this.escapeEnabled = true;
            this.escapeChar = escapeChar;
        }
        if (lineContinuationCharacter != null) {
            this.multilineEnabled = true;
            this.lineContinuationChar = lineContinuationCharacter;
        }
    }

    /**
     * Returns the starting line number of the last record record.  A value of
     * -1 is returned if the end of the stream was reached.
     * @return the starting line number of the last record
     */
    public int getRecordLineNumber() {
        return recordLineNumber;
    }

    /**
     * Returns the raw text of the last record read or null if the end of the
     * stream was reached.
     * @return the raw text of the last record
     */
    public String getRecordText() {
        return recordText;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordReader#read()
     */
    public String[] read() throws IOException {
        // fieldList is set to null when the end of stream is reached
        if (fieldList == null) {
            recordText = null;
            recordLineNumber = -1;
            return null;
        }

        ++lineNumber;
        int lineOffset = 0;

        // clear the field list
        fieldList.clear();

        boolean continued = false; // line continuation
        boolean escaped = false; // last character read matched the escape char
        boolean eol = false; // end of record flag
        StringBuffer text = new StringBuffer(); // holds the record text being read
        StringBuffer field = new StringBuffer(); // holds the latest field value being read

        int n;
        while (!eol && (n = in.read()) != -1) {
            char c = (char) n;

            // skip '\n' after a '\r'
            if (skipLF) {
                skipLF = false;
                if (c == '\n') {
                    continue;
                }
            }

            // handle line continuation
            if (continued) {
                continued = false;

                text.append(c);

                if (c == '\n') {
                    escaped = false;
                    ++lineNumber;
                    ++lineOffset;
                    continue;
                }
                else if (c == '\r') {
                    escaped = false;
                    skipLF = true;
                    ++lineNumber;
                    ++lineOffset;
                    continue;
                }
                else if (!escaped) {
                    field.append(lineContinuationChar);
                }
            }
            else if (c != '\n' && c != '\r') {
                text.append(c);
            }

            // handle escaped characters
            if (escaped) {
                escaped = false;

                // an escape character can be used to escape itself or an end quote
                if (c == delim) {
                    field.append(c);
                    continue;
                }
                else if (c == escapeChar) {
                    field.append(escapeChar);
                    continue;
                }
                else {
                    field.append(escapeChar);
                }
            }

            // default handling
            if (escapeEnabled && c == escapeChar) {
                escaped = true;
                if (multilineEnabled && c == lineContinuationChar) {
                    continued = true;
                }
            }
            else if (multilineEnabled && c == lineContinuationChar) {
                continued = true;
            }
            else if (c == delim) {
                fieldList.add(field.toString());
                field = new StringBuffer();
            }
            else if (c == '\r') {
                fieldList.add(field.toString());
                skipLF = true;
                eol = true;
            }
            else if (c == '\n') {
                fieldList.add(field.toString());
                eol = true;
            }
            else {
                field.append(c);
            }
        }

        // update the record line number
        recordLineNumber = lineNumber - lineOffset;
        recordText = text.toString();

        // if eol is true, we're done; if not, then the end of file was reached 
        // and further validation is needed
        if (eol) {
            recordText = text.toString();
            String[] record = new String[fieldList.size()];
            return fieldList.toArray(record);
        }

        if (continued) {
            throw new RecordIOException("Unexpected end of stream after line continuation at line " + lineNumber);
        }

        // handle last escaped char
        if (escaped) {
            field.append(escapeChar);
        }

        if (text.length() > 0) {
            fieldList.add(field.toString());

            String[] record = new String[fieldList.size()];
            record = fieldList.toArray(record);
            recordText = text.toString();
            fieldList = null;
            return record;
        }
        else {
            fieldList = null;
            recordText = null;
            recordLineNumber = -1;
            return null;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordReader#close()
     */
    public void close() throws IOException {
        in.close();
    }
}
