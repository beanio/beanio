/*
 * Copyright 2011 Kevin Seim
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
package org.beanio.types.xml;

import java.util.*;

import javax.xml.datatype.*;
import javax.xml.namespace.QName;

import org.beanio.types.*;

/**
 * Base class for <tt>java.util.Date</tt> type handlers based on the W3C XML Schema 
 * datatype specification.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public abstract class AbstractXmlDateTypeHandler extends DateTypeHandler {

    protected static final DatatypeFactory dataTypeFactory;
    static {
        try {
            dataTypeFactory = DatatypeFactory.newInstance();
        }
        catch (DatatypeConfigurationException e) {
            throw new IllegalStateException("Failed to create DatatypeFactory instance", e);
        } 
    }
    
    private TimeZone timeZone = null;
    private boolean timeZoneAllowed = true;
    
    /**
     * Constructs a new <tt>AbstractXmlDateTypeHandler</tt>.
     */
    public AbstractXmlDateTypeHandler() { }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.types.DateTypeHandler#parse(java.lang.String)
     */
    @Override
    public Date parse(String text) throws TypeConversionException {
        if ("".equals(text)) {
            return null;
        }
        
        QName type = getDatatypeQName();
        
        try {
            XMLGregorianCalendar xcal = dataTypeFactory.newXMLGregorianCalendar(text);
            if (!xcal.getXMLSchemaType().equals(type)) {
                throw new TypeConversionException("Invalid XML " + type.getLocalPart());
            }
            
            if (!isTimeZoneAllowed() && xcal.getTimezone() !=  DatatypeConstants.FIELD_UNDEFINED) {
                throw new TypeConversionException("Invalid XML " + type.getLocalPart() + 
                    ", time zone not allowed");
            }
            
            return xcal.toGregorianCalendar().getTime();
        }
        catch (IllegalArgumentException ex) {
            throw new TypeConversionException("Invalid XML " + type.getLocalPart());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.types.DateTypeHandler#format(java.lang.Object)
     */
    @Override
    public abstract String format(Object value);

    /**
     * Returns the expected XML Schema datatype when <tt>parse</tt> is called.
     * @return the expected XML schema datatype <tt>QName</tt>
     */
    protected abstract QName getDatatypeQName();
    
    /**
     * Creates a new calendar using the configured time zone (if set).
     * @return a new <tt>Calendar</tt> instance
     */
    protected Calendar newCalendar() {
        return timeZone == null ? Calendar.getInstance() : Calendar.getInstance(timeZone);
    }
    
    /**
     * Returns the time zone offset in minutes for the given date, 
     * or {@link DatatypeConstants#FIELD_UNDEFINED} if a time zone was not configured.
     * @param date the date on which to determine the time zone offset
     * @return the time zone offset in minutes, or {@link DatatypeConstants#FIELD_UNDEFINED}
     */
    protected int getTimeZoneOffset(Date date) {
        if (timeZone == null) {
            return DatatypeConstants.FIELD_UNDEFINED;
        }
        else {
            return timeZone.getOffset(date.getTime()) / 60000;
        }
    }

    /**
     * Sets the time zone for interpreting dates.  If not set, the system default 
     * time zone is used.
     * @param name the time zone ID
     * @see TimeZone
     */
    public void setTimeZoneId(String name) {
        if (name == null || "".equals(name)) {
            timeZone = null;
        }
        else {
            timeZone = TimeZone.getTimeZone(name);
        }
    }
    
    /**
     * Returns the time zone used to interpret dates, or <tt>null</tt> if the default
     * time zone will be used.
     * @return the time zone ID
     * @see TimeZone
     */
    public String getTimeZoneId() {
        return timeZone == null ? null : timeZone.getID();
    }

    public boolean isTimeZoneAllowed() {
        return timeZoneAllowed;
    }

    public void setTimeZoneAllowed(boolean timeZoneAllowed) {
        this.timeZoneAllowed = timeZoneAllowed;
    }
}
