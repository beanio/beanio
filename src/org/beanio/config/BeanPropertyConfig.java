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
package org.beanio.config;

/**
 * A bean property can be added to a {@link BeanConfig} to set a property
 * value on the bean object that is not found in the input stream.  A bean property
 * can also be used to identify the record mapping for marshalling a bean object by
 * setting <tt>rid</tt> to true.
 * 
 * <p>The following properties inherited from {@link PropertyConfig} are ignored:</p>
 * <ul>
 * <li>ignored</li>
 * <li>collection</li>
 * <li>minOccurs</li>
 * <li>maxOccurs</li>
 * <li>xmlName</li>
 * <li>xmlType</li>
 * <li>xmlNamespace</li>
 * <li>xmlPrefix</li>
 * <li>nillable</li>
 * <li>xmlWrapper</li>
 * </ul>
 * @author Kevin Seim
 * @since 1.2
 */
public class BeanPropertyConfig extends PropertyConfig {

    private String value;
    private String handler;
    private boolean recordIdentifier;
    private String format;
    
    /**
     * Constructs a new <tt>BeanPropertyConfig</tt>.
     */
    public BeanPropertyConfig() { }

    /**
     * Returns the textual representation of the fixed property value.
     * @return the property value text
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the textual representation of the fixed property value.
     * @param value the property value text
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns the name of the custom type handler used to convert the
     * property value to a Java object, or <tt>null</tt> if the default
     * type handler is sufficient.
     * @return the name of the custom type handler
     */
    public String getTypeHandler() {
        return handler;
    }

    /**
     * Sets the name of the custom type handler to use to convert the
     * property value to a Java object.  Set to <tt>null</tt> if the default
     * type handler is sufficient.
     * @param handler the name of the custom type handler
     */
    public void setTypeHandler(String handler) {
        this.handler = handler;
    }

    /**
     * Returns <tt>true</tt> if this property is used to identify the record
     * mapping for marshalling a bean object when writing to an output stream.
     * @return <tt>true</tt> if this property is used to identify the record
     *   mapping during marshalling
     */
    public boolean isRecordIdentifier() {
        return recordIdentifier;
    }

    /**
     * Set to <tt>true</tt> if this property is used to identify the record
     * mapping for marshalling a bean object when writing to an output stream.
     * @param recordIdentifier <tt>true</tt> if this property is used to identify 
     *   the record mapping during marshalling
     */
    public void setRecordIdentifier(boolean recordIdentifier) {
        this.recordIdentifier = recordIdentifier;
    }
    
    /**
     * Returns the format pattern used by date and number type handlers.
     * @return the date or number format pattern
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format pattern used by date and number type handlers.
     * @param format the date or number format pattern
     */
    public void setFormat(String format) {
        this.format = format;
    }
}
