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
package org.beanio.config.xml;

import java.io.*;
import java.util.Collection;

import org.beanio.BeanIOConfigurationException;
import org.beanio.config.*;

/**
 * Loads BeanIO mapping files in XML format.  This class is made thread safe
 * by delegating most of the parsing logic to {@link XmlMappingParser}, for
 * which a new instance is created for each input stream that requires parsing.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class XmlConfigurationLoader implements ConfigurationLoader {

    private XmlMappingReader reader = new XmlMappingReader();
    
    /*
     * (non-Javadoc)
     * @see org.beanio.config.MultiConfigurationLoader#loadConfigurations(java.io.InputStream)
     */
    public Collection<BeanIOConfig> loadConfiguration(InputStream in) throws IOException, BeanIOConfigurationException {
        return createParser().loadConfiguration(in);
    }
    
    /**
     * Creates a {@link XmlMappingParser} for reading an mapping input stream.
     * @return a new XML mapping parser
     */
    protected XmlMappingParser createParser() {
        return new XmlMappingParser(getReader());
    }
    
    /**
     * Returns the {@link XmlMappingReader} for reading XML mapping files
     * into a document object model (DOM).
     * @return the XML mapping reader
     */
    protected XmlMappingReader getReader() {
        return reader;
    }
}
