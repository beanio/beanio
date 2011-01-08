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
import java.util.Collection;

import org.beanio.BeanIOConfigurationException;

/**
 * A <tt>ConfigurationFactory</tt> is used by the <tt>DefaultStreamFactory</tt>
 * to load stream definitions.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public interface ConfigurationFactory {

    /**
     * Loads a stream mapping configuration from an input stream.
     * @param in the input stream to read
     * @return the collection of stream definitions loaded from the input stream
     * @throws IOException if an I/O error occurs
     * @throws BeanIOConfigurationException if the configuration is invalid
     */
    public Collection<StreamDefinition> loadDefinitions(InputStream in) throws IOException,
        BeanIOConfigurationException;

}
