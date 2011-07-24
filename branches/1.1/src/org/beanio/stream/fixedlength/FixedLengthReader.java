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
 * {@link String} class.  Records must be terminated by a single 
 * configurable character, or by default, any of the following: line feed (LF), 
 * carriage return (CR), or CRLF combination.
 * <p>
 * If a record may span multiple lines, a single line continuation
 * character may be configured.  The line continuation character must immediately 
 * precede the record termination character.  Note that line continuation characters 
 * are not included in the record text. 
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthReader implements RecordReader {

    private char lineContinuationChar = '\\';
    private boolean multilineEnabled = false;
    private char recordTerminator = 0;
    
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
     * @param lineContinuationCharacter the line continuation character,
     *   or <tt>null</tt> to disable line continuations
     */
    public FixedLengthReader(Reader in, Character lineContinuationCharacter) {
        this(in, lineContinuationCharacter, null);
    }
    
    /**
     * Constructs a new <tt>FixedLengthReader</tt>.
     * @param in the input stream to read from
     * @param lineContinuationCharacter the line continuation character,
     *   or <tt>null</tt> to disable line continuations
     * @param recordTerminator the character used to signify the end of a record
     */
    public FixedLengthReader(Reader in, Character lineContinuationCharacter, Character recordTerminator) {
        this.in = in;
        
        if (lineContinuationCharacter == null) {
            this.multilineEnabled = false;
        }
        else {
            if (recordTerminator != null && lineContinuationCharacter == recordTerminator) {
                throw new IllegalArgumentException("The line continuation character and recrod terminator cannot match.");
            }    
            this.multilineEnabled = true;
            this.lineContinuationChar = lineContinuationCharacter;
        }
        
        if (recordTerminator != null) {
            this.recordTerminator = recordTerminator;
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.line.RecordReader#getRecordLineNumber()
     */
    public int getRecordLineNumber() {
        if (recordLineNumber < 0) {
            return recordLineNumber;
        }
        return recordTerminator == 0 ? recordLineNumber : 0;
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

                if (endOfRecord(c)) {
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
            else if (endOfRecord(c)) {
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
            recordText = null;
            recordLineNumber = -1;
            return null;
        }
        else {
            eof = true;
            return record.toString();
        }
    }

    /**
     * Returns <tt>true</tt> if the given character matches the record separator.  This
     * method also updates the internal <tt>skipLF</tt> flag.
     * @param c the character to test
     * @return <tt>true</tt> if the character signifies the end of the record
     */
    private boolean endOfRecord(char c) {
        if (recordTerminator == 0) {
            if (c == '\r') {
                skipLF = true;
                return true;
            }
            else if (c == '\n') {
                return true;
            }
            return false;
        }
        else {
            return c == recordTerminator;
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
