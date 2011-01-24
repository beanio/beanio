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
package org.beanio;

import java.util.*;

/**
 * When a <tt>BeanReaderException</tt> is thrown, all errors and the current state of the 
 * <tt>BeanReader</tt> can be accessed from the <tt>BeanReaderContext</tt>.  Depending on the type
 * of exception, some information may be missing.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see BeanReaderException
 */
public interface BeanReaderContext {

    /**
     * Returns the line number of the failed record.
     * @return the line number of the failed record
     */
    public int getRecordLineNumber();

    /**
     * Returns the raw text of the record being parsed.
     * @return the raw text of the record
     */
    public String getRecordText();

    /**
     * Returns the name of the record from the stream configuration.  The record name
     * may be null if was not determined before the exception occurred.
     * @return the name of the record from the stream configuration
     */
    public String getRecordName();

    /**
     * Returns <tt>true</tt> if there are one or more record level errors.
     * @return <tt>true</tt> if there are one or more record level errors
     */
    public boolean hasRecordErrors();

    /**
     * Returns the collection of record level error messages.
     * @return the collection of record level error messages
     */
    public Collection<String> getRecordErrors();

    /**
     * Returns the unparsed text of a field in this record.  Field text may be null
     * if a record level exception was thrown before a field was parsed.
     * @param fieldName the name of the field to get the text for
     * @return the unparsed field text
     */
    public String getFieldText(String fieldName);

    /**
     * Returns <tt>true</tt> if there are one or more field level errors.
     * @return <tt>true</tt> if there are one or more field level errors.
     */
    public boolean hasFieldErrors();

    /**
     * Returns a Map of all field errors, where the Map key is the field name.
     * @return a Map of all field errors
     */
    public Map<String, Collection<String>> getFieldErrors();

    /**
     * Returns the field errors for a given field.
     * @param fieldName the name of the field
     * @return the collection of field errors for the named field
     */
    public Collection<String> getFieldErrors(String fieldName);

}
