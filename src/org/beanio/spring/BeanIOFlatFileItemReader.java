/*
 * Copyright 2011-2013 Kevin Seim
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
package org.beanio.spring;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Locale;

import org.beanio.*;
import org.beanio.internal.util.IOUtil;
import org.springframework.batch.item.file.*;
import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.*;

/**
 * A Spring Batch item reader that uses a BeanIO stream mapping file to read items 
 * from a flat file.  Restart capabilities are fully supported.
 * 
 * <p>This implementation requires Spring 2.5 or greater, and Spring Batch 2.1.x.</p>
 * 
 * @author Kevin Seim
 * @since 1.2
 * @param <T> Class type read from the file
 */
public class BeanIOFlatFileItemReader<T> extends AbstractItemCountingItemStreamItemReader<T>
    implements ResourceAwareItemReaderItemStream<T>, InitializingBean {

    private static final String DEFAULT_CHARSET = Charset.defaultCharset().name();
    
    private StreamFactory streamFactory;
    private Resource streamMapping;
    private String streamName;
    private Locale locale;
    private boolean strict = true;
    private boolean useSpringExceptions = false;
    
    private Resource resource;
    private String encoding = DEFAULT_CHARSET;
    
    private BeanReader reader;
    private BeanReaderErrorHandler errorHandler;
    
    /**
     * Constructs a new <tt>BeanIOFlatFileItemReader</tt>.
     */
    public BeanIOFlatFileItemReader() { 
        setName(ClassUtils.getShortName(BeanIOFlatFileItemReader.class));
    }
    
    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(streamName, "Stream name must be set");
        initializeStreamFactory();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected T doRead() throws Exception {
        if (reader == null) {
            return null;
        }
        
        try {
            return (T) reader.read();
        }
        catch (BeanReaderIOException ex) {
            throw ex.getCause();
        }
        catch (BeanReaderException ex) {
            if (useSpringExceptions) {
                RecordContext ctx = ex.getRecordContext();
                if (ctx != null) {
                    throw new FlatFileParseException(ex.getMessage(), ex, 
                        ctx.getRecordText(), ctx.getLineNumber());
                }
                else {
                    throw new FlatFileParseException(ex.getMessage(), ex, null, 0);
                }
            }
            else {
                throw ex;
            }
        }
    }

    @Override
    protected void doOpen() throws Exception {
        Assert.notNull(resource, "Input resource must be set");

        // validate the resource exists
        if (!resource.exists()) {
            if (strict) {
                throw new IllegalStateException("Input resource must exist: " + resource);
            }
            return;
        }

        // open the input stream
        InputStream bin = resource.getInputStream();
        
        // wrap the input stream in a Reader
        Reader in;
        if (encoding == null) {
            in = new InputStreamReader(bin);
        }
        else {
            in = new InputStreamReader(bin, encoding);
        }
        
        // create the BeanReader
        reader = streamFactory.createReader(streamName, in, locale);
        if (errorHandler != null) {
            reader.setErrorHandler(errorHandler);
        }
    }
    
    @Override
    protected void doClose() throws Exception {
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }
    
    @Override
    protected void jumpToItem(int itemIndex) throws Exception {
        if (itemIndex <= 0) {
            return;
        }
        
        int skipped = 0;
        if (reader != null) {
            skipped = reader.skip(itemIndex);
        }
        
        if (skipped < itemIndex) {
            throw new IllegalStateException("Failed to skip " + itemIndex + 
                " items, end of stream reached after " + skipped + " items");
        }
    }

    /**
     * Creates a {@link StreamFactory} if one was not set, loads the stream
     * mapping resource if set, and validates the <tt>StreamFactory</tt> has a
     * mapping for the configured stream name.
     * @throws IllegalStateException if the configured stream name is not found
     *   in the <tt>StreamFactory</tt> after loading the stream mapping
     */
    protected void initializeStreamFactory() throws Exception {
        if (streamFactory == null) {
            streamFactory = StreamFactory.newInstance();
        }
        
        // load the configured stream mapping if the stream name is not already mapped
        if (!streamFactory.isMapped(streamName) && streamMapping != null) {
            InputStream in = streamMapping.getInputStream();
            try {
                streamFactory.load(in);
            }
            finally {
                IOUtil.closeQuietly(in);
            }
        }
        
        if (!streamFactory.isMapped(streamName)) {
            throw new IllegalStateException("No mapping configuration for stream '" + streamName + "'");
        }
    }
    
    /**
     * Returns the record or group name of the most recent bean object
     * read from this reader, or null if the end of the stream was reached.
     * @return the record or group name, or null if the stream is closed
     * @since 2.0.6
     */
    public String getRecordName() {
        return reader != null ? reader.getRecordName() : null;
    }
    
    /**
     * Returns the starting line number of the first record for the most recent bean
     * object read from this reader, or -1 when the end of the stream is reached.  
     * The line number may be zero if new lines are not used to separate characters.  
     * @return the line number, or -1 if the stream is closed
     * @since 2.0.6
     */
    public int getLineNumber() {
        return reader != null ? reader.getLineNumber() : -1;
    }
    
    /**
     * Returns the number of records read from the underlying input stream for the
     * most recent bean object read from this reader.  This typically returns 1
     * unless a bean object was mapped to a record group which may span
     * multiple records.
     * @return the record count, or -1 if the stream is closed
     * @since 2.0.6
     */
    public int getRecordCount() {
        return reader != null ? reader.getRecordCount() : -1;
    }
    
    /**
     * Returns record information for the most recent bean object read from this reader.
     * If a bean object can span multiple records, {@link #getRecordCount()} can be used
     * to determine how many records were read from the stream.
     * @param index the index of the record, starting at 0
     * @return the {@link RecordContext}, or null if the stream is closed
     * @throws IndexOutOfBoundsException if there is no record for the given index
     * @see #getRecordCount()
     * @since 2.0.6
     */
    public RecordContext getRecordContext(int index) throws IndexOutOfBoundsException {
        return reader != null ? reader.getRecordContext(index) : null;
    }
    
    /**
     * Returns the underlying {@link BeanReader}.
     * @return the {@link BeanReader}, or null if the stream is closed
     * @since 2.0.6
     */
    protected BeanReader getBeanReader() {
        return reader;
    }
    
    /*
     * (non-Javadoc)
     * @see org.springframework.batch.item.file.ResourceAwareItemReaderItemStream#setResource(org.springframework.core.io.Resource)
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * In strict mode this reader will throw an exception if the input resource does
     * not exist when opened.  Defaults to <tt>true</tt>.
     * @param strict set to <tt>false</tt> to disable resource validation
     */
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    /**
     * Sets the {@link StreamFactory} for loading stream mapping configurations.  If not set,
     * a new default <tt>StreamFactory</tt> is created.
     * @param streamFactory the <tt>StreamFactory</tt> to use for loading stream
     *   mapping configurations
     */
    public void setStreamFactory(StreamFactory streamFactory) {
        this.streamFactory = streamFactory;
    }

    /**
     * Sets the mapping file resource.  A mapping file is required if a
     * stream factory is not set.
     * @param streamMapping the stream mapping resource
     */
    public void setStreamMapping(Resource streamMapping) {
        this.streamMapping = streamMapping;
    }

    /**
     * Sets the mapping configuration's stream name for reading this input
     * stream.
     * @param streamName the stream name
     */
    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    /**
     * Sets the input stream encoding.  If not set, the default system charset
     * is used to read the input stream.
     * @param encoding input stream encoding
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Set to <tt>true</tt> to force {@link #read()} to wrap BeanIO exceptions
     * to their closest Spring-batch counterpart.  Defaults to <tt>false</tt>.
     * <ul>
     * <li>BeanReaderIOException -> NonTransientResourceException</li>
     * <li>MalformedRecordException -> NonTransientResourceException</li>
     * <li>UnexpectedRecordException -> NonTransientResourceException</li>
     * <li>UnidentifiedRecordException -> UnexpectedInputException</li>
     * <li>InvalidRecordException -> ParseException</li>
     * </ul>
     * @param useSpringExceptions set to <tt>true</tt> to map BeanIO exceptions
     *  to their Spring-batch counterpart
     */
    public void setUseSpringExceptions(boolean useSpringExceptions) {
        this.useSpringExceptions = useSpringExceptions;
    }
    
    /**
     * Sets the BeanIO error handler for handling BeanIO specific exceptions.
     * If no error handler is set, the {@link #read()} method will throw the exception
     * directly.
     * @param errorHandler the {@link BeanReaderErrorHandler} for handling exceptions
     */
    public void setErrorHandler(BeanReaderErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Sets the locale for resolving error messages from a stream mapping's 
     * configured resource bundle.  By default, the system default locale is used.
     * @param locale the locale for resolving error messages
     */
    public void setLocale(String locale) {
        if (locale == null) {
            this.locale = null;
            return;
        }
        
        String [] s = locale.split("_");
        if (s.length >= 3) {
            this.locale = new Locale(s[0], s[1], s[2]);
        }
        else if (s.length == 2){
            this.locale = new Locale(s[0], s[1]);
        }
        else {
            this.locale = new Locale(locale);
        }
    }
}
