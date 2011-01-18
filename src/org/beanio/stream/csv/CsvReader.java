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
package org.beanio.stream.csv;

import java.io.*;
import java.util.*;

import org.beanio.stream.*;

/**
 * A <tt>CsvReader</tt> is used to parse CSV formatted flat files into records
 * of <tt>String</tt> arrays.  The CSV format supported is defined by specification 
 * RFC 4180.  By default, there is one exception: lines that span multiple records will 
 * throw an exception.  To allow quoted multi-line fields, simply set 
 * <tt>multilineEnabled</tt> to <tt>true</tt> when constructing the reader.
 * <p>
 * The reader also supports the following customizations:
 * <ul>
 * <li>The default quotation mark character, '"', can be overridden.</li>
 * <li>The default escape character, '"', can be overridden or disabled altogether.</li>
 * <li>Whitespace can be allowed outside of quoted values.<li>
 * <li>Quotation marks can be allowed in unquoted fields (as long as the quotation
 *   mark is not the first character in the field</li>
 * </ul>
 * </p>
 * <p>The reader will not recognize an escape character used outside of a quoted 
 * field.</p>
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class CsvReader implements RecordReader {

    private char delim = ',';
    private char quote = '"';
    private char endQuote = '"';
    private char escapeChar = '"';
    private boolean escapeEnabled = true;
    private boolean multilineEnabled = false;
    private boolean whitespaceAllowed = false;
    private boolean unquotedQuotesAllowed = false;

    private transient Reader in;
    private transient String recordText;
    private transient int recordLineNumber;
    private transient int lineNumber = 0;
    private transient boolean skipLF = false;
    private transient List<String> fieldList = new ArrayList<String>();

    /**
     * Constructs a new <tt>CsvReader</tt>.
     * @param in the input stream to read from
     */
    public CsvReader(Reader in) {
        this.in = in;
    }

    /**
     * Constructs a new <tt>CsvReader</tt>.
     * @param in the input stream to read from
     * @param delimiter the field delimiter
     * @param escape the escape character, or <tt>null</tt> to disable escaping
     * @param multilineEnabled set to <tt>true</tt> to allow quoted fields to contain
     *   newline and carriage return characters
     */
    public CsvReader(Reader in, char delimiter, Character escape, boolean multilineEnabled) {
        this(in, delimiter, '"', escape, multilineEnabled, false, false);
    }

    /**
     * Constructs a new <tt>CsvReader</tt>.
     * @param in the input stream to read from
     * @param delimiter the field delimiter
     * @param quote the quotation character
     * @param escape the escape character, or <tt>null</tt> to disable escaping
     * @param multilineEnabled set to <tt>true</tt> to allow quoted fields to contain
     *   newline and carriage return characters
     * @param whitespaceAllowed set to <tt>true</tt> to ignore whitespace outside
     *   of quoted fields
     * @param unquotedQuotesAllowed set to <tt>true</tt> to allow fields containing
     *   quotation marks in unquoted fields, for example: 'Mark said "...'.
     *   Note that if the quotation mark is at the beginning of the field, the
     *   field is considered quoted and a trailing quotation mark is required.
     * @throws IllegalArgumentException if the configuration is invalid
     */
    public CsvReader(Reader in, char delimiter, char quote, Character escape,
        boolean multilineEnabled, boolean whitespaceAllowed, boolean unquotedQuotesAllowed)
        throws IllegalArgumentException {

        this.in = in;
        this.delim = delimiter;
        this.quote = quote;
        this.endQuote = quote;
        if (this.quote == this.delim) {
            throw new IllegalArgumentException("The CSV field delimiter cannot " +
                "match the character used for the quotation mark.");
        }
        this.multilineEnabled = multilineEnabled;
        this.whitespaceAllowed = whitespaceAllowed;
        this.unquotedQuotesAllowed = unquotedQuotesAllowed;
        if (escape != null) {
            this.escapeEnabled = true;
            this.escapeChar = escape;
            if (this.escapeChar == this.delim) {
                throw new IllegalArgumentException(
                    "The CSV field delimiter cannot match the escape character.");
            }
        }
        else {
            this.escapeEnabled = false;
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

    /**
     * Reads the next record from this input stream.
     * @return the array of field values that make up the next record
     *   read from the stream
     * @throws IOException if an I/O error occurs
     */
    public String[] read() throws IOException, RecordIOException {
        // fieldList is set to null when the end of stream is reached
        if (fieldList == null) {
            recordText = null;
            recordLineNumber = -1;
            return null;
        }

        ++lineNumber;
        
        // the record line number is set to the first line of the record
        recordLineNumber = lineNumber;

        // clear the field list
        fieldList.clear();

        int state = 0; // current state
        int whitespace = 0;
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
                    if (state == 1) {
                        field.append(c);
                        text.append(c);
                    }
                    continue;
                }
            }

            // append the raw record text
            if (c != '\n' && c != '\r') {
                text.append(c);
            }

            // handle escaped characters
            if (escaped) {
                escaped = false;

                // an escape character can be used to escape itself or an end quote
                if (c == endQuote) {
                    field.append(c);
                    continue;
                }
                else if (c == escapeChar) {
                    field.append(escapeChar);
                    continue;
                }

                if (escapeChar == endQuote) {
                    fieldList.add(field.toString());
                    field = new StringBuffer();
                    state = 10;
                }
            }

            switch (state) {
            case 0: // initial state (beginning of line, or next value)
                if (c == delim) {
                    fieldList.add(createWhitespace(whitespace));
                    whitespace = 0;
                }
                else if (c == quote) {
                    whitespace = 0;
                    state = 1; // look for trailing quote
                }
                else if (c == ' ') {
                    if (!whitespaceAllowed) {
                        field.append(c);
                        state = 2; // look for next delimiter
                    }
                    else {
                        ++whitespace;
                    }
                }
                else if (c == '\r') {
                    fieldList.add("");
                    skipLF = true;
                    eol = true;
                }
                else if (c == '\n') {
                    fieldList.add("");
                    eol = true;
                }
                else {
                    field.append(createWhitespace(whitespace));
                    whitespace = 0;
                    field.append(c);
                    state = 2; // look for next delimiter
                }
                break;

            case 1: // quoted field, look for trailing quote at end of field
                if (escapeEnabled && c == escapeChar) {
                    escaped = true;
                }
                else if (c == endQuote) {
                    fieldList.add(field.toString());
                    field = new StringBuffer();
                    state = 10; // look for next delimiter
                }
                else if (c == '\r' || c == '\n') {
                    if (multilineEnabled) {
                        skipLF = (c == '\r');
                        ++lineNumber;
                        text.append(c);
                        field.append(c);
                    }
                    else {
                        throw new RecordIOException(
                            "Expected end quotation character '" + endQuote + "' before end of line "
                                + lineNumber);
                    }
                }
                else {
                    field.append(c);
                }
                break;

            case 2: // unquoted field, look for next delimiter
                if (c == delim) {
                    fieldList.add(field.toString());
                    field = new StringBuffer();
                    state = 0;
                }
                else if (c == quote && !unquotedQuotesAllowed) {
                    recover(text);
                    throw new RecordIOException(
                        "Quotation character '" + quote + "' must be quoted at line " + lineNumber);
                }
                else if (c == '\n') {
                    fieldList.add(field.toString());
                    field = new StringBuffer();
                    eol = true;
                }
                else if (c == '\r') {
                    skipLF = true;
                    fieldList.add(field.toString());
                    field = new StringBuffer();
                    eol = true;
                }
                else {
                    field.append(c);
                }
                break;

            case 10: // quoted field, after final quote read
                if (c == ' ') {
                    if (!whitespaceAllowed) {
                        recover(text);
                        throw new RecordIOException(
                            "Invalid whitespace found outside of quoted field at line " + lineNumber);
                    }
                }
                else if (c == delim) {
                    state = 0;
                }
                else if (c == '\r') {
                    skipLF = true;
                    eol = true;
                }
                else if (c == '\n') {
                    eol = true;
                }
                else {
                    recover(text);
                    throw new RecordIOException(
                        "Invalid character found outside of quoted field at line " + lineNumber);
                }

                break;
            }
        }

        // if eol is true, we're done; if not, then the end of file was reached 
        // and further validation is needed
        if (eol) {
            recordText = text.toString();
            String[] record = new String[fieldList.size()];
            return fieldList.toArray(record);
        }

        // handle escaped mode
        if (escaped) {
            if (escapeChar == endQuote) {
                fieldList.add(field.toString());
                state = 10;
            } /* unreachable code:
              else {
                field.append(escapeChar);
              } */
        }

        // validate current state...
        switch (state) {
        case 0:
            // do not create an empty field if we've reached the end of the file and no
            // characters were read on the last line
            if (whitespace > 0 || fieldList.size() > 0)
                fieldList.add(createWhitespace(whitespace));
            break;
        case 1:
            throw new RecordIOException(
                "Expected end quote before end of line at line " + lineNumber);
        case 2:
            fieldList.add(field.toString());
            break;
        case 10:
            break;
        }

        if (fieldList.isEmpty()) {
            fieldList = null;
            recordText = null;
            recordLineNumber = -1;
            return null;
        }
        else {
            String[] record = new String[fieldList.size()];
            record = fieldList.toArray(record);
            recordText = text.toString();
            fieldList = null;
            return record;
        }
    }

    /**
     * Advances the input stream to the end of the record so that subsequent reads
     * might be possible.
     * @param c the last read character
     * @param text the current record text
     * @throws IOException
     */
    private void recover(StringBuffer text) throws IOException {
        int n;
        while ((n = in.read()) != -1) {
            char c = (char) n;
            if (c == '\n') {
                recordText = text.toString();
                return;
            }
            else if (c == '\r') {
                skipLF = true;
                recordText = text.toString();
                return;
            }
            else {
                text.append(c);
            }
        }

        // end of file reached...
        recordText = text.toString();
        fieldList = null;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordReader#close()
     */
    public void close() throws IOException {
        in.close();
    }

    private String createWhitespace(int size) {
        if (size == 0)
            return "";

        StringBuffer b = new StringBuffer(size);
        for (int i = 0; i < size; i++)
            b.append(' ');
        return b.toString();
    }
}
