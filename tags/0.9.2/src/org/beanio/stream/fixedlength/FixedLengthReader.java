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
package org.beanio.stream.fixedlength;

import java.io.*;

import org.beanio.stream.*;

/**
 * A <tt>FixedLengthReader</tt> is used to read records from a fixed length
 * file or input stream.  A fixed length record is represented using the
 * {@link String} class. 
 * <p>
 * If a record may span multiple lines, a single line continuation
 * character may be configured.  The line continuation character will not be
 * included in the record text. The character must be the last character on the 
 * line being continued (other than a newline or carriage return).
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthReader implements RecordReader {

    private char lineContinuationChar = '\\';
    private boolean multilineEnabled = false;

    private transient Reader in;
    private transient String recordText;
    private transient int recordLineNumber;
    private transient int lineNumber = 0;
    private transient boolean skipLF = false;
    private transient boolean eof = false;

    /**
     * Constructs a new <tt>FixedLengthReader</tt>.  By default, line
     * continuation is disabled. 
     * @param in the input stream to read from
     */
    public FixedLengthReader(Reader in) {
        this(in, null);
    }

    /**
     * Constructs a new <tt>FixedLengthReader</tt>.
     * @param in the input stream to read from
     * @param lineContinuationCharacter
     */
    public FixedLengthReader(Reader in, Character lineContinuationCharacter) {
        this.in = in;
        if (lineContinuationCharacter == null) {
            this.multilineEnabled = false;
        }
        else {
            this.multilineEnabled = true;
            this.lineContinuationChar = lineContinuationCharacter;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.line.RecordReader#getRecordLineNumber()
     */
    public int getRecordLineNumber() {
        return recordLineNumber;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.line.RecordReader#getRecordText()
     */
    public String getRecordText() {
        return recordText;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.line.RecordReader#read()
     */
    public String read() throws IOException, RecordIOException {
        if (eof) {
            recordText = null;
            recordLineNumber = -1;
            return null;
        }

        ++lineNumber;
        int lineOffset = 0;

        boolean continued = false; // line continuation
        boolean eol = false; // end of record flag
        StringBuffer text = new StringBuffer();
        StringBuffer record = new StringBuffer();

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
                    ++lineNumber;
                    ++lineOffset;
                    continue;
                }
                else if (c == '\r') {
                    skipLF = true;
                    ++lineNumber;
                    ++lineOffset;
                    continue;
                }
                else {
                    record.append(lineContinuationChar);
                }
            }

            if (multilineEnabled && c == lineContinuationChar) {
                continued = true;
            }
            else if (c == '\r') {
                skipLF = true;
                eol = true;
            }
            else if (c == '\n') {
                eol = true;
            }
            else {
                text.append(c);
                record.append(c);
            }
        }

        // update the record line number
        recordLineNumber = lineNumber - lineOffset;
        recordText = text.toString();

        // if eol is true, we're done; if not, then the end of file was reached 
        // and further validation is needed
        if (eol) {
            return record.toString();
        }

        if (continued) {
            throw new RecordIOException("Unexpected end of stream after line continuation at line " + lineNumber);
        }

        if (recordText.length() == 0) {
            eof = true;
            return null;
        }
        else {
            eof = true;
            return record.toString();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.line.RecordReader#close()
     */
    public void close() throws IOException {
        in.close();
    }
}
