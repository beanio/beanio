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
package org.beanio.types;

import java.text.*;
import java.util.*;

/**
 * This type handler uses a <tt>SimpleDateFormat</tt> class to parse and format 
 * <tt>java.util.Date</tt> objects.  If no pattern is set, <tt>DateFormat.getInstance()</tt> 
 * is used to create a default date format.  By default, <tt>lenient</tt> is false.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see Date
 * @see DateFormat
 * @see SimpleDateFormat
 */
public class DateTypeHandler implements ConfigurableTypeHandler {

    private String pattern = null;
    private boolean lenient = false;

    /**
     * Constructs a new <tt>DateTypeHandler</tt>.
     */
    public DateTypeHandler() { }

    /**
     * Constructs a new <tt>DateTypeHandler</tt>.
     * @param pattern the <tt>SimpleDateFormat</tt> pattern
     */
    public DateTypeHandler(String pattern) {
        this.pattern = pattern;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.types.TypeHandler#parse(java.lang.String)
     */
    public Date parse(String text) throws TypeConversionException {
        if ("".equals(text))
            return null;

        DateFormat dateFormat = createDateFormat();
        dateFormat.setLenient(lenient);
        
        ParsePosition pp = new ParsePosition(0);
        Date date = dateFormat.parse(text, pp);
        if (pp.getErrorIndex() >= 0 || pp.getIndex() != text.length()) {
            throw new TypeConversionException("Invalid date");
        }
        return date;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.types.TypeHandler#format(java.lang.Object)
     */
    public String format(Object value) {
        if (value == null)
            return null;
        else
            return createDateFormat().format(value);
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.types.ConfigurableTypeHandler#newInstance(java.util.Properties)
     */
    public DateTypeHandler newInstance(Properties properties) throws IllegalArgumentException {
        String pattern = properties.getProperty(FORMAT_SETTING);
        if (pattern == null || "".equals(pattern)) {
            return this;
        }
        if (pattern.equals(this.pattern)) {
            return this;
        }
                
        DateTypeHandler handler = new DateTypeHandler();
        handler.setPattern(pattern);
        handler.setLenient(lenient);
        return handler;
    }
    
    /**
     * Creates the <tt>DateFormat</tt> to use to parse and format the field value.
     * @return the <tt>DateFormat</tt> for type conversion
     */
    protected DateFormat createDateFormat() {
        if (pattern == null) {
            return createDefaultDateFormat();
        }
        else {
            return new SimpleDateFormat(pattern);
        }
    }
    
    /**
     * Creates a default date format when no pattern is set.
     * @return the default date format
     */
    protected DateFormat createDefaultDateFormat() {
        return DateFormat.getDateTimeInstance();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.types.TypeHandler#getType()
     */
    public Class<?> getType() {
        return Date.class;
    }

    /**
     * Returns the date pattern used by the <tt>SimpleDateFormat</tt>.
     * @return the date pattern
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Sets the date pattern used by the <tt>SimpleDateFormat</tt>.
     * @param pattern the date pattern
     * @throws IllegalArgumentException if the date pattern is invalid
     */
    public void setPattern(String pattern) throws IllegalArgumentException {
        // validate the pattern
        try {
            if (pattern != null) {
                new SimpleDateFormat(pattern);
            }
        }
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid date format pattern '" + pattern + "': " + ex.getMessage());
        }
        
        this.pattern = pattern;
    }

    /**
     * Returns whether the <tt>SimpleDateFormat</tt> is lenient.
     * @return <tt>true</tt> if lenient, <tt>false</tt> otherwise
     */
    public boolean isLenient() {
        return lenient;
    }

    /**
     * Sets whether the <tt>SimpleDateFormat</tt> is lenient.
     * @param lenient <tt>true</tt> if lenient, <tt>false</tt> otherwise
     */
    public void setLenient(boolean lenient) {
        this.lenient = lenient;
    }
}
