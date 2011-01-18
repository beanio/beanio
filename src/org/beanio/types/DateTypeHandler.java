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
import java.util.Date;

/**
 * This type handler uses the <tt>SimpleDateFormat</tt> class to parse
 * and format <tt>Date</tt> objects.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see SimpleDateFormat
 */
public class DateTypeHandler implements TypeHandler {

    private String pattern = "MMddyyyy";
    private boolean lenient = false;

    /**
     * Constructs a new <tt>DateTypeHandler</tt> using the default
     * pattern '<tt>MMddyyyy</tt>' and setting <tt>lenient</tt> to
     * <tt>false</tt>.
     */
    public DateTypeHandler() { }

    /*
     * (non-Javadoc)
     * @see org.beanio.types.TypeHandler#parse(java.lang.String)
     */
    public Date parse(String text) throws TypeConversionException {
        if ("".equals(text))
            return null;

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(lenient);

        ParsePosition pp = new ParsePosition(0);
        Date date = sdf.parse(text, pp);
        if (pp.getErrorIndex() >= 0) {
            throw new TypeConversionException("Invalid date");
        }
        if (pp.getIndex() != text.length()) {
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
            return "";
        else
            return new SimpleDateFormat(pattern).format(value);
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
     */
    public void setPattern(String pattern) {
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
