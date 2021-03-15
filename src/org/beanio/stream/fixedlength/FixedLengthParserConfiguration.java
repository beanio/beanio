/*
 * Copyright 2012 Kevin Seim
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

/**
 * Stores configuration settings for parsing fixed length formatted streams.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class FixedLengthParserConfiguration {

    private Character lineContinuationCharacter = null;
    private String recordTerminator = null;
    private String[] comments;

    /**
     * Returns the line continuation character.  By default, line continuation
     * is disabled and <code>null</code> is returned.
     * @return the line continuation character or <code>null</code> if disabled
     */
    public Character getLineContinuationCharacter() {
        return lineContinuationCharacter;
    }

    /**
     * Sets the line continuation character.  Set to <code>null</code> to disable
     * line continuation.
     * @param lineContinuationCharacter the line continuation character
     */
    public void setLineContinuationCharacter(Character lineContinuationCharacter) {
        this.lineContinuationCharacter = lineContinuationCharacter;
    }

    /**
     * Returns whether the line continuation character is enabled.  By default,
     * line continuation is disabled.
     * @return <code>true</code> if the line continuation character is enabled
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
    public String getRecordTerminator() {
        return recordTerminator;
    }

    /**
     * Sets the character used to mark the end of a record.  If set to <code>null</code>,
     * a carriage return (CR), line feed (LF), or CRLF sequence is used.
     * @param recordTerminator the record termination character
     */
    public void setRecordTerminator(String recordTerminator) {
        this.recordTerminator = recordTerminator;
    }
    
    /**
     * Returns the array of comment prefixes.  If a line read from a stream begins
     * with a configured comment prefix, the line is ignored.  By default, no lines
     * are considered commented.
     * @return the array of comment prefixes
     */
    public String[] getComments() {
        return comments;
    }

    /**
     * Sets the array of comment prefixes.  If a line read from a stream begins
     * with a configured comment prefix, the line is ignored. 
     * @param comments the array of comment prefixes
     */
    public void setComments(String[] comments) {
        this.comments = comments;
    }
    
    /**
     * Returns whether one or more comment prefixes have been configured.
     * @return <code>true</code> if one or more comment prefixes have been configured
     */
    public boolean isCommentEnabled() {
        return comments != null && comments.length > 0;
    }
    
    /**
     * Returns the text used to terminate a record.  By default, the line
     * separator is set to the value of the  <code>line.separator</code> system property.
     * @return the line separation text
     * @deprecated
     */
    @Deprecated
    public String getLineSeparator() {
        return recordTerminator;
    }

    /**
     * Sets the text used to terminate a record.  If set to <code>null</code>, the
     * the value of the <code>line.separator</code> system property is used to terminate
     * records.
     * @param lineSeparator the line separation text
     * @deprecated
     */
    @Deprecated
    public void setLineSeparator(String lineSeparator) {
        this.recordTerminator = lineSeparator;
    }
    
}
