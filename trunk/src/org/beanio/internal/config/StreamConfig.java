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
package org.beanio.internal.config;

import java.util.*;

/**
 * A stream is the root (a.k.a top or parent) group of a stream mapping configuration.  
 * As such, it contains other attributes that apply to the entire stream.
 * 
 * <p>By default, a stream can be used for both marshalling (write) and unmarshalling
 * (read).  Calling {@link #setMode(String)} can restrict the use of the stream, but
 * will relax some validations on the types of objects that can be read or written.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class StreamConfig extends GroupConfig {

    /** Stream configuration supports reading and writing */
    public static final String READ_WRITE_MODE = "readwrite";
    /** Stream configuration supports reading only */
    public static final String READ_ONLY_MODE = "read";
    /** Stream configuration supports writing only */
    public static final String WRITE_ONLY_MODE = "write";
    
    private String format;
    private String mode;
    private String resourceBundle;
    private boolean strict = false;

    private List<TypeHandlerConfig> handlerList = new ArrayList<TypeHandlerConfig>();
    private BeanConfig readerFactory;
    private BeanConfig writerFactory;

    /**
     * Constructs a new <tt>StreamConfig</tt>.
     */
    public StreamConfig() {
        setMinOccurs(0);
        setMaxOccurs(1);
        setOrder(1);
    }
    
    @Override
    public char getComponentType() {
        return STREAM;
    }

    /**
     * Returns the format of this stream.
     * @return the stream format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format of this stream.
     * @param format the stream format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Returns the allowed mode(s) of operation for this stream. 
     * @return {@link #READ_WRITE_MODE} if reading and writing from a stream is allowed,<br />
     *   {@link #READ_ONLY_MODE} if only reading is allowed,<br/>
     *   {@link #WRITE_ONLY_MODE} if only writing is allowed, <br/> 
     *   or <tt>null</tt> if explicitly set
     */
    public String getMode() {
        return mode;
    }

    /**
     * Sets the allowed mode(s) of operation for this stream.  If not
     * set, or explicitly set to <tt>null</tt>, the stream configuration defaults to
     * read/write.  Some configuration validations are relaxed if set to read or write only.
     * @param mode {@link #READ_WRITE_MODE} if reading and writing from a stream is allowed,<br />
     *   {@link #READ_ONLY_MODE} if only reading is allowed,<br/>
     *   {@link #WRITE_ONLY_MODE} if only writing is allowed
     */
    public void setMode(String mode) {
        this.mode = mode;
    }

    /**
     * Returns the path name of the resource bundle containing customized error
     * messages for this stream.
     * @return the resource bundle name
     */
    public String getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Sets the path name of the resource bundle containing customized error
     * messages for this stream.
     * @param resourceBundle the resource bundle name
     */
    public void setResourceBundle(String resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Adds a custom type handler to this stream.
     * @param handler the type handler to add
     */
    public void addHandler(TypeHandlerConfig handler) {
        handlerList.add(handler);
    }

    /**
     * Returns a list of customized type handlers configured for this stream.
     * @return the list of custom type handlers
     */
    public List<TypeHandlerConfig> getHandlerList() {
        return handlerList;
    }

    /**
     * Returns the record reader factory configuration bean.
     * @return the record reader factory configuration bean.
     */
    public BeanConfig getReaderFactory() {
        return readerFactory;
    }

    /**
     * Sets the record reader factory configuration bean.
     * @param readerFactory the record reader factory configuration bean
     */
    public void setReaderFactory(BeanConfig readerFactory) {
        this.readerFactory = readerFactory;
    }

    /**
     * Returns the record writer factory configuration bean.
     * @return the record writer factory configuration bean.
     */
    public BeanConfig getWriterFactory() {
        return writerFactory;
    }

    /**
     * Sets the record writer factory configuration bean.
     * @param writerFactory the record writer factory configuration bean
     */
    public void setWriterFactory(BeanConfig writerFactory) {
        this.writerFactory = writerFactory;
    }

    public boolean isStrict() {
        return strict;
    }

    public void setStrict(boolean strict) {
        this.strict = strict;
    }
}
