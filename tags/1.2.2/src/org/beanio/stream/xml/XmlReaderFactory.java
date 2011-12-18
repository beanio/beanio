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
package org.beanio.stream.xml;

import java.io.Reader;

import org.beanio.stream.*;
import org.w3c.dom.Document;

/**
 * This record reader factory is used to create and configure a <tt>XmlReader</tt>.
 * 
 * @author Kevin Seim
 * @since 1.1
 * @see XmlReader
 */
public class XmlReaderFactory implements RecordReaderFactory, XmlStreamConfigurationAware {

    private XmlStreamConfiguration source;

    /**
     * Constructs a new <tt>XmlReaderFactory</tt>.
     */
    public XmlReaderFactory() { }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordReaderFactory#createReader(java.io.Reader)
     */
    public RecordReader createReader(Reader in) throws IllegalArgumentException {
        Document base = null;
        if (source != null) {
            base = source.getDocument();
        }
        
        return new XmlReader(in, base);
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.xml.XmlDocumentAware#setSource(org.beanio.stream.xml.XmlDocumentSource)
     */
    public void setConfiguration(XmlStreamConfiguration source) {
        this.source = source;
    }
}
