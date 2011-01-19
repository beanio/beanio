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

import java.io.Reader;

import org.beanio.stream.*;

/**
 * This reader reader factory is used to create and configure a
 * <tt>CsvReaderFactory</tt>.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class CsvReaderFactory implements RecordReaderFactory {

    private char delimiter = ',';
    private char quote = '"';
    private Character escape = '"';
    private boolean multilineEnabled = false;
    private boolean whitespaceAllowed = false;
    private boolean unquotedQuotesAllowed = false;

    /*
     * (non-Javadoc)
     * @see org.beanio.line.RecordReaderFactory#createReader(java.io.Reader)
     */
    public RecordReader createReader(Reader in) {
        return new CsvReader(in, delimiter, quote, escape, multilineEnabled, whitespaceAllowed,
            unquotedQuotesAllowed);
    }

    /**
     * Sets the field delimiter. By default, the delimiter is a comma.
     * @param c the character used to delimit fields
     */
    public void setDelimiter(char c) {
        this.delimiter = c;
    }

    /**
     * Returns the field delimiter. By default, the delimiter is a comma.
     * @return the character used to delimit fields
     */
    public char getDelimiter() {
        return delimiter;
    }

    /**
     * Returns the character to use for a quotation mark. Defaults to '"'.
     * @return the quotation mark character
     */
    public char getQuote() {
        return quote;
    }

    /**
     * Sets the character to use for a quotation mark.
     * @param quote the new quotation mark character
     */
    public void setQuote(char quote) {
        this.quote = quote;
    }

    /**
     * Sets the escape character. Quotation marks can be escaped within quoted
     * values using the escape character. For example, using the default escape
     * character, '"Hello ""friend"""' is parsed into 'Hello "friend"'.  Set
     * to <tt>null</tt> to disable escaping.
     * @param c new escape character
     */
    public void setEscape(Character c) {
        this.escape = c;
    }

    /**
     * Returns the escape character. Quotation marks can be escaped within
     * quoted values using the escape character. For example, using the default
     * escape character, '"Hello ""friend"""' is parsed into 'Hello "friend"'.
     * Defaults to the quotation mark, <tt>"</tt>.
     * @return the escape character or <tt>null</tt> if escaping is disabled
     */
    public Character getEscape() {
        return escape;
    }
    
    /**
     * Returns whether escaping is enabled. By default, escaping is enabled.
     * @return <tt>true</tt> if an escape character is enabled
     * @see #getEscape()
     */
    public boolean isEscapeEnabled() {
        return escape != null;
    }

    /**
     * Returns whether a record may span multiple lines (when quoted). Defaults
     * to <tt>false</tt>.
     * @return <tt>true</tt> if a record may span multiple lines
     */
    public boolean isMultilineEnabled() {
        return multilineEnabled;
    }

    /**
     * Sets whether a record may span multiple lines (when quoted).
     * @param multilineEnabled set to true <tt>true</tt> to allow records to
     *   span multiple lines
     */
    public void setMultilineEnabled(boolean multilineEnabled) {
        this.multilineEnabled = multilineEnabled;
    }

    /**
     * Returns whether to ignore unquoted whitespace. Returns <tt>false</tt> by
     * default which causes the following record cause an exception:
     * 
     * <pre>
     * "Field1", "Field2"
     *          ^
     *        Unquoted whitespace here
     * </pre>
     * 
     * @return <tt>true</tt> if unquoted whitespace is allowed
     */
    public boolean isWhitespaceAllowed() {
        return whitespaceAllowed;
    }

    /**
     * Sets whether unquoted whitespace is ignored.
     * @param whitespaceAllowed set to <tt>true</tt> to ignore unquoted
     *   whitespace
     */
    public void setWhitespaceAllowed(boolean whitespaceAllowed) {
        this.whitespaceAllowed = whitespaceAllowed;
    }

    /**
     * Returns whether quotes are allowed to appear in an unquoted field. Set to
     * <tt>false</tt> by default which will cause the following record to throw
     * an exception:
     * <pre>
     * Field1,Field"2,Field3
     * </pre>
     * @return <tt>true</tt> if quotes may appear in an unquoted field
     */
    public boolean isUnquotedQuotesAllowed() {
        return unquotedQuotesAllowed;
    }

    /**
     * Sets wheter quotes are allowed to appear in an unquoted field.
     * @param unquotedQuotesAllowed set to <tt>true</tt> if quotes may appear in
     *   an unquoted field
     */
    public void setUnquotedQuotesAllowed(boolean unquotedQuotesAllowed) {
        this.unquotedQuotesAllowed = unquotedQuotesAllowed;
    }
}
