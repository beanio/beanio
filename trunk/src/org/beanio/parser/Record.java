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
package org.beanio.parser;

import java.text.MessageFormat;
import java.util.*;

import org.beanio.BeanReaderContext;
import org.beanio.stream.RecordReader;

/**
 * A <tt>Record</tt> holds state information about the last record read from an 
 * input stream.  A single <tt>Record</tt> instance is used repeatedly while
 * reading the input stream.  Before each record is read, <tt>clear()</tt> is
 * called to clear the state of the last record.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class Record {

    private int lineNumber;
    private String recordText;
    private String recordName;

    private Map<String, String> fieldMap = new HashMap<String, String>();
    private Map<String, Collection<String>> fieldErrors;
    private Collection<String> recordErrors;

    private Locale locale;
    private MessageFactory messageContext;

    /**
     * Constructs a <tt>Record</tt>.
     */
    public Record() {
    }

    /**
     * Clears the current state of the record before the next record is read.
     */
    public void clear() {
        this.recordText = null;
        this.fieldMap.clear();
        this.fieldErrors = null;
        this.recordErrors = null;
    }

    /**
     * Sets the value of the record as returned from the <tt>RecordReader</tt>
     * @param value the record value read by a record reader
     * @see RecordReader
     */
    public void setValue(Object value) {
    }

    /**
     * Returns the current state of the bean reader stored by this class.
     * @return the current state of the reader
     */
    public BeanReaderContext getContext() {
        BeanReaderContextImpl ctx = new BeanReaderContextImpl();
        ctx.lineNumber = lineNumber;
        ctx.recordText = recordText;
        ctx.recordName = recordName;
        ctx.recordErrors = recordErrors;
        ctx.fieldErrorMap = fieldErrors;

        // the same field map is used over and over by this implementation, so a copy is returned
        if (fieldMap != null && !fieldMap.isEmpty()) {
            ctx.fieldTextMap = new HashMap<String, String>(fieldMap);
        }

        return ctx;
    }

    /**
     * Returns <tt>true</tt> if a field error was reported while parsing
     * this record.
     * @return <tt>true</tt> if a field error was reported
     */
    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }

    /**
     * Returns <tt>true</tt> if a record level error was reported while parsing
     * this record.
     * @return <tt>true</tt> if a record level error was reported
     */
    public boolean hasRecordErrors() {
        return recordErrors != null && !recordErrors.isEmpty();
    }

    /**
     * Adds a field error to this record.
     * @param fieldName the name of the field in error
     * @param fieldText the invalid field text
     * @param rule the name of the failed validation rule
     * @param params an optional list of parameters for formatting the error message 
     */
    public void addFieldError(String fieldName, String fieldText, String rule, Object... params) {
        String recordLabel = messageContext.getRecordLabel(recordName);
        String fieldLabel = messageContext.getFieldLabel(recordName, fieldName);

        if (recordLabel == null)
            recordLabel = "'" + recordName + "' record";
        if (fieldLabel == null)
            fieldLabel = "'" + fieldName + "' field";

        Object[] messageParams;
        if (params.length == 0) {
            messageParams = new Object[] { lineNumber, recordLabel, fieldLabel, fieldText };
        }
        else {
            messageParams = new Object[4 + params.length];
            messageParams[0] = lineNumber;
            messageParams[1] = recordLabel;
            messageParams[2] = fieldLabel;
            messageParams[3] = fieldText;
            System.arraycopy(params, 0, messageParams, 4, params.length);
        }

        String message = messageContext.getFieldErrorMessage(recordName, fieldName, rule);
        MessageFormat mf = new MessageFormat(message, locale);
        addFieldErrorMessage(fieldName, mf.format(messageParams));
    }

    /**
     * Adds a record level error to this record.
     * @param rule the name of the failed validation rule
     * @param params an optional list of parameters for formatting the error message 
     */
    public void addRecordError(String rule, Object... params) {
        String recordLabel = messageContext.getRecordLabel(recordName);
        if (recordLabel == null) {
            recordLabel = "'" + recordName + "' record";
        }

        Object[] messageParams;
        if (params.length == 0) {
            messageParams = new Object[] { lineNumber, recordLabel, recordText };
        }
        else {
            messageParams = new Object[3 + params.length];
            messageParams[0] = lineNumber;
            messageParams[1] = recordLabel;
            messageParams[2] = recordText;
            System.arraycopy(params, 0, messageParams, 3, params.length);
        }

        String message = messageContext.getRecordErrorMessage(recordName, rule);
        MessageFormat mf = new MessageFormat(message, locale);
        addRecordError(mf.format(messageParams));
    }

    /**
     * Adds a field error message.
     * @param fieldName the name of the field 
     * @param message the error message to add
     */
    protected void addFieldErrorMessage(String fieldName, String message) {
        if (fieldErrors == null) {
            fieldErrors = new HashMap<String, Collection<String>>();
        }
        Collection<String> errors = fieldErrors.get(fieldName);
        if (errors == null) {
            errors = new ArrayList<String>();
            errors.add(message);
            fieldErrors.put(fieldName, errors);
        }
        else {
            errors.add(message);
        }
    }

    /**
     * Adds a record level error message.
     * @param message the error message to add
     */
    protected void addRecordError(String message) {
        if (recordErrors == null) {
            recordErrors = new ArrayList<String>(3);
        }
        recordErrors.add(message);
    }

    /**
     * Returns the raw text of the last record read from the record reader.
     * @return the raw text of the last record read
     */
    public String getRecordText() {
        return recordText;
    }

    /**
     * Sets the raw text of the last record read from the record reader.
     * @param text the raw text of the last record read
     */
    public void setRecordText(String text) {
        this.recordText = text;
    }

    /**
     * Returns the starting line number of the last record read from the record reader.
     * @return the line number of the last record
     */
    public int getRecordLineNumber() {
        return lineNumber;
    }

    /**
     * Sets the starting line number of the last record read from the record reader.
     * @param lineNumber the line number of the last record
     */
    public void setLineNumber(int lineNumber) {
        if (lineNumber > 0)
            this.lineNumber = lineNumber;
    }

    /**
     * Returns the name of the last record read from the record reader, 
     * or <tt>null</tt> if not known.
     * @return the name of the record
     */
    public String getRecordName() {
        return recordName;
    }

    /**
     * Sets the name of the last record read from the record reader.
     * @param recordName
     */
    public void setRecordName(String recordName) {
        this.recordName = recordName;
    }

    /**
     * Returns the locale used to format error messages.
     * @return the locale used to format erorr messages
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale used to format error messages.
     * @param locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Sets the <tt>MessageContext</tt> to use to format erorr messages.
     * @param messageContext the <tt>MessageContext</tt> to use to format
     *   erorr messages
     */
    public void setMessageContext(MessageFactory messageContext) {
        this.messageContext = messageContext;
    }

    /**
     * Sets the raw field text for a named field.
     * @param fieldName the name of the field
     * @param text the raw field text
     */
    public void setFieldText(String fieldName, String text) {
        fieldMap.put(fieldName, text);
    }

    /**
     * Returns the raw field text for a named field.
     * @param fieldName the name of the field
     * @return the raw field text
     */
    public String getFieldText(String fieldName) {
        return fieldMap.get(fieldName);
    }

    @Override
    public String toString() {
        String name = recordName == null ? "?" : recordName;
        return "Record '" + name + "' at line " + lineNumber +
            " {" + recordText + "}";
    }

    /*
     * BeanReaderContext implementation. 
     */
    private static class BeanReaderContextImpl implements BeanReaderContext {
        int lineNumber;
        String recordText;
        String recordName;
        Collection<String> recordErrors;
        Map<String, String> fieldTextMap;
        Map<String, Collection<String>> fieldErrorMap;

        public int getRecordLineNumber() {
            return lineNumber;
        }

        public String getRecordText() {
            return recordText;
        }

        public String getRecordName() {
            return recordName;
        }

        public boolean hasRecordErrors() {
            return recordErrors != null && !recordErrors.isEmpty();
        }

        public Collection<String> getRecordErrors() {
            if (recordErrors == null)
                recordErrors = Collections.emptyList();
            return recordErrors;
        }

        public String getFieldText(String fieldName) {
            if (fieldTextMap == null)
                return null;
            else
                return fieldTextMap.get(fieldName);
        }

        public boolean hasFieldErrors() {
            return fieldErrorMap != null && !fieldErrorMap.isEmpty();
        }

        public Map<String, Collection<String>> getFieldErrors() {
            if (fieldErrorMap == null)
                fieldErrorMap = Collections.emptyMap();
            return fieldErrorMap;
        }

        public Collection<String> getFieldErrors(String fieldName) {
            if (fieldErrorMap == null)
                return null;
            else
                return fieldErrorMap.get(fieldName);
        }
    }
}
