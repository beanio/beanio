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
package org.beanio.internal.parser;

import java.io.*;
import java.util.Locale;

import org.beanio.*;
import org.beanio.internal.util.Replicator;

/**
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class Stream {

    /** Stream definition supports reading and writing */
    public static final int READ_WRITE_MODE = 0;
    /** Stream definition supports reading only */
    public static final int READ_ONLY_MODE = 1;
    /** Stream definition supports writing only */
    public static final int WRITE_ONLY_MODE = 2;
    
    private int mode;
    private String name;
    private StreamFormat format;
    private Selector layout;
    private Replicator replicator;
    private MessageFactory messageFactory;

    /**
     * Constructs a new <tt>Stream</tt>.
     * @param format
     */
    public Stream(StreamFormat format) {
        if (format == null) {
            throw new NullPointerException("null format");
        }
        this.format = format;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }
    
    /**
     * Creates a new <tt>BeanReader</tt> for reading from the given input stream.
     * @param in the input stream to read from
     * @param locale the locale to use for rendering error messages
     * @return a new <tt>BeanReader</tt>.
     */
    public BeanReader createBeanReader(Reader in, Locale locale) {
        if (in == null) {
            throw new NullPointerException("null reader");
        }
        
        Selector selector = replicator.replicate(layout);
        
        UnmarshallingContext context = format.createUnmarshallingContext();
        context.setMessageFactory(messageFactory);
        context.setLocale(locale);
        context.setRecordReader(format.createRecordReader(in));
        
        BeanReaderImpl reader = new BeanReaderImpl(context, selector);
        return reader;
    }

    
    /**
     * Creates a new <tt>BeanWriter</tt> for writing to the given output stream.
     * @param out the output stream to write to
     * @return a new <tt>BeanWriter</tt>
     */
    public BeanWriter createBeanWriter(Writer out) {
        if (out == null) {
            throw new NullPointerException("null writer");
        }
        
        Selector mapper = replicator.replicate(layout);
        
        MarshallingContext context = format.createMarshallingContext();
        context.setRecordWriter(format.createRecordWriter(out));
        
        BeanWriterImpl writer = new BeanWriterImpl(context, mapper);
        return writer;
    }

    /**
     * Returns the allowed mode of operation for this stream configuration. 
     * @return {@link #READ_WRITE_MODE} if reading and writing from a stream is allowed,<br />
     *   {@link #READ_ONLY_MODE} if only reading is allowed,<br/>
     *   {@link #WRITE_ONLY_MODE} if only writing is allowed
     */
    public int getMode() {
        return mode;
    }

    /**
     * Sets the allowed mode of operation for this stream configuration.
     * @param mode {@link #READ_WRITE_MODE} if reading and writing from a stream is allowed,<br />
     *   {@link #READ_ONLY_MODE} if only reading is allowed,<br />
     *   {@link #WRITE_ONLY_MODE} if only writing is allowed
     */
    public void setMode(int mode) {
        this.mode = mode;
    }
    
    public Selector getLayout() {
        return layout;
    }

    public void setLayout(Selector layout) {
        this.layout = layout;
    }
    
    public Replicator getReplicator() {
        return replicator;
    }

    public void setReplicator(Replicator replicator) {
        this.replicator = replicator;
    }

    public StreamFormat getFormat() {
        return format;
    }

    public void setFormat(StreamFormat format) {
        this.format = format;
    }

    public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public void setMessageFactory(MessageFactory messageFactory) {
        this.messageFactory = messageFactory;
    }
}
