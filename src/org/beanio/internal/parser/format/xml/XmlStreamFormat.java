/*
 * Copyright 2011-2012 Kevin Seim
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
package org.beanio.internal.parser.format.xml;

import java.io.*;

import org.beanio.internal.parser.*;
import org.beanio.internal.util.DomUtil;
import org.beanio.stream.*;
import org.beanio.stream.xml.*;
import org.w3c.dom.Document;

/**
 * A {@link StreamFormatSupport} implementation for the XML stream format.
 * 
 * <p>This implementation requires a reference to the stream layout (i.e. the root {@link Selector}
 * in the parser tree).  The layout is not modified in any way allowing this class to be thread
 * safe. 
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class XmlStreamFormat extends StreamFormatSupport {

    private Selector layout;
    
    /**
     * Constructs a new <tt>XmlStreamFormat</tt>.
     */
    public XmlStreamFormat() { }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.StreamFormat#createUnmarshallingContext()
     */
    public UnmarshallingContext createUnmarshallingContext() {
        return new XmlUnmarshallingContext();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.StreamFormat#createMarshallingContext()
     */
    public MarshallingContext createMarshallingContext() {
        return new XmlMarshallingContext();
    }

    @Override
    public void setReaderFactory(RecordReaderFactory readerFactory) {
        if (readerFactory != null && readerFactory instanceof XmlStreamConfigurationAware) {
            ((XmlStreamConfigurationAware)readerFactory).setConfiguration(new XmlStreamConfiguration() {
                public Document getDocument() {
                    return createBaseDocument(layout);
                }
            });
        }
        super.setReaderFactory(readerFactory);
    }
    
    @Override
    protected RecordReader createDefaultReader(Reader in) {
        return new XmlReader(in, createBaseDocument(layout));
    }

    @Override
    public RecordWriter createDefaultWriter(Writer out) {
        return new XmlWriter(out);
    }

    /**
     * Sets the stream layout.
     * @param layout the root {@link Selector} node in the parser tree
     */
    public void setLayout(Selector layout) {
        this.layout = layout;
    }
    
    /**
     * Creates a DOM made up of any group nodes in the parser tree.
     * @return the new {@link Document} made up of group nodes
     */
    protected Document createBaseDocument(Selector layout) {
        if (layout instanceof XmlSelectorWrapper) {
            XmlSelectorWrapper wrapper = (XmlSelectorWrapper) layout;
            
            Document document = wrapper.createBaseDocument();
            return document;
        }
        else {
            return DomUtil.newDocument();
        }
    }
}
