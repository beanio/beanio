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
package org.beanio.spring;

import java.io.*;
import java.util.List;

import org.beanio.*;
import org.beanio.util.IOUtil;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

/**
 * A Spring factory bean for creating shared {@link StreamFactory} instances for
 * BeanIO item readers and writers.  Using a shared <tt>StreamFactory</tt> can improve
 * performance by preventing mapping files from being loaded more than once when
 * multiple item readers and writers are repeatedly invoked or share the same mapping
 * configuration.
 * 
 * @author Kevin Seim
 * @since 1.2
 * @see StreamFactory
 * @see BeanIOFlatFileItemReader
 * @see BeanIOFlatFileItemWriter
 */
public class BeanIOStreamFactory implements FactoryBean {

    private StreamFactory streamFactory;
    private List<Resource> streamMappings;
    
    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public StreamFactory getObject() throws Exception {
        if (streamFactory == null) {
            streamFactory = createStreamFactory();
        }
        return streamFactory;
    }
    
    /**
     * Creates a new {@link StreamFactory} and loads configured stream mapping resources.
     * @return the new <tt>StreamFactory</tt>
     * @throws BeanIOConfigurationException if a stream mapping resource does not exist
     *   or is invalid 
     * @throws IOException if an I/O error occurs
     */
    protected StreamFactory createStreamFactory() throws IOException, BeanIOConfigurationException {
        StreamFactory factory = StreamFactory.newInstance();
        
        if (streamMappings != null) {
            for (Resource res : streamMappings) {
                if (!res.exists()) {
                    throw new BeanIOConfigurationException("Mapping file not found: " + res);
                }
                
                InputStream in = res.getInputStream();
                try {
                    factory.load(in);
                }
                catch (BeanIOConfigurationException ex) {
                    throw new BeanIOConfigurationException("Error in mapping file '" + res + "': " +
                        ex.getMessage(), ex);
                }
                finally {
                    IOUtil.closeQuietly(in);
                }
            }
        }
        
        return factory;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class<?> getObjectType() {
        return StreamFactory.class;
    }

    /*
     * (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * Sets the list of mapping files to load into this stream factory.
     * @param streamMappings the list of mapping files
     */
    public void setStreamMappings(List<Resource> streamMappings) {
        this.streamMappings = streamMappings;
    }
}
