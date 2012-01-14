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

import java.io.*;
import java.util.*;

import org.beanio.*;
import org.beanio.config.*;
import org.beanio.parser.StreamDefinition;

/**
 * The <tt>DefaultStreamFactory</tt> stores configured stream definitions used
 * to create bean readers and writers.  The factory is thread safe and the
 * same stream definition can be used to concurrently read and write multiple streams.
 *  
 * @author Kevin Seim
 * @since 1.0
 */
public class DefaultStreamFactory extends StreamFactory {

    private ConfigurationFactory configurationFactory = new DefaultConfigurationFactory();

    private Map<String, StreamDefinition> contextMap = new HashMap<String, StreamDefinition>();

    /**
     * Constructs a new <tt>DefaultStreamFactory</tt>.
     */
    public DefaultStreamFactory() { }

    /*
     * (non-Javadoc)
     * @see org.beanio.StreamManager#configure(java.io.InputStream)
     */
    public void load(InputStream in) throws IOException, BeanIOConfigurationException {
        Collection<StreamDefinition> streams = configurationFactory.loadDefinitions(in);
        for (StreamDefinition stream : streams) {
            addStream(stream);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.StreamManager#createBeanReader(java.lang.String, java.io.Reader, java.util.Locale)
     */
    public BeanReader createReader(String name, Reader in, Locale locale) {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        
        StreamDefinition stream = getStream(name);
        switch (stream.getMode()) {
            case StreamDefinition.READ_WRITE_MODE:
            case StreamDefinition.READ_ONLY_MODE:
                return stream.createBeanReader(in, locale);
            default:
                throw new IllegalArgumentException("Read mode not supported for stream mapping '" + name + "'");
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.StreamManager#createBeanWriter(java.lang.String, java.io.Writer)
     */
    public BeanWriter createWriter(String name, Writer out) {
        StreamDefinition stream = getStream(name);
        switch (stream.getMode()) {
            case StreamDefinition.READ_WRITE_MODE:
            case StreamDefinition.WRITE_ONLY_MODE:
                return stream.createBeanWriter(out);
            default:
                throw new IllegalArgumentException("Write mode not supported for stream mapping '" + name + "'");
        }
    }

    /**
     * Returns the named stream.
     * @param name the name of the stream
     * @return the stream context
     * @throws IllegalArgumentException if there is no stream configured for the given name
     */
    protected StreamDefinition getStream(String name) throws IllegalArgumentException {
        StreamDefinition definition = contextMap.get(name);
        if (definition == null) {
            throw new IllegalArgumentException("No stream mapping configured for name '" + name + "'");
        }
        return definition;
    }

    /**
     * Adds a stream context to this manager.
     * @param context the stream context to add
     */
    public void addStream(StreamDefinition context) {
        contextMap.put(context.getName(), context);
    }

    /**
     * Removes the named stream from this manager.
     * @param name the name of the stream to remove
     * @return the removed <tt>StreamDefinition</tt>, or <tt>null</tt> if
     *   the there was no stream context for the given name
     */
    public StreamDefinition removeStream(String name) {
        return contextMap.remove(name);
    }

    /**
     * Sets the configuration factory to use to load stream definitions.
     * @param configurationFactory the configuration factory
     */
    public void setConfigurationFactory(ConfigurationFactory configurationFactory) {
        this.configurationFactory = configurationFactory;
    }

    @Override
    protected void validateStreamName(String name) throws IllegalArgumentException {
        if (!isMapped(name)) {
            throw new IllegalArgumentException("No stream mapping configured for name '" + name + "'");
        }
    }

    @Override
    public boolean isMapped(String streamName) {
        return contextMap.containsKey(streamName);
    }
}
