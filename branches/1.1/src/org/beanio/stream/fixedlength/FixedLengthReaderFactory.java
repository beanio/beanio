/*
 * Copyright 2010 Kevin Seim
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

import java.io.Reader;

import org.beanio.stream.*;

/**
 * This record reader factory is used to create and configure a <tt>FixedLengthReader</tt>.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see FixedLengthReader
 */
public class FixedLengthReaderFactory implements RecordReaderFactory {

    private Character lineContinuationCharacter = null;
    private Character recordTerminator = null;
    
    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordReaderFactory#createReader(java.io.Reader)
     */
    public RecordReader createReader(Reader in) {
        return new FixedLengthReader(in, lineContinuationCharacter, recordTerminator);
    }

    /**
     * Returns the line continuation character.  By default, line continuation
     * is disabled and <tt>null</tt> is returned.
     * @return the line continuation character or <tt>null</tt> if disabled
     */
    public Character getLineContinuationCharacter() {
        return lineContinuationCharacter;
    }

    /**
     * Sets the line continuation character.  Set to <tt>null</tt> to disable
     * line continuation.
     * @param lineContinuationCharacter the line continuation character
     */
    public void setLineContinuationCharacter(Character lineContinuationCharacter) {
        this.lineContinuationCharacter = lineContinuationCharacter;
    }

    /**
     * Returns whether the line continuation character is enabled.  By default,
     * line continuation is disabled.
     * @return <tt>true</tt> if the line continuation character is enabled
     */
    public boolean isLineContinationEnabled() {
        return lineContinuationCharacter != null;
    }
    
    /**
     * Returns the character used to mark the end of a record.  By default,
     * a carriage return (CR), line feed (LF), or CRLF sequence is used to
     * signify the end of the record.
     * @return the record termination character
     */
    public Character getRecordTerminator() {
        return recordTerminator;
    }

    /**
     * Sets the character used to mark the end of a record.  If set to <tt>null</tt>,
     * a carriage return (CR), line feed (LF), or CRLF sequence is used.
     * @param recordTerminator the record termination character
     */
    public void setRecordTerminator(Character recordTerminator) {
        this.recordTerminator = recordTerminator;
    }
}
