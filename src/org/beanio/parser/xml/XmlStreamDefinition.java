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
package org.beanio.parser.xml;

import java.io.*;
import java.util.Map;

import org.beanio.*;
import org.beanio.parser.*;
import org.beanio.stream.*;
import org.beanio.stream.xml.*;
import org.beanio.util.*;
import org.w3c.dom.*;
import org.w3c.dom.Node;

/**
 * Stream definition used to parse and format XML formatted streams.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlStreamDefinition extends StreamDefinition {

    /**
     * Constructs a new <tt>XmlStreamDefinition</tt>.
     */
    public XmlStreamDefinition() {
        super("xml");
    }
    
    @Override
    protected GroupDefinition newGroupDefinition() {
        return new XmlGroupDefinition();
    }
    
    @Override
    protected GroupNode newGroupNode(GroupDefinition definition) {
        return new XmlGroupNode((XmlGroupDefinition) definition);
    }

    @Override
    protected Record createRecord() {
        return new XmlRecord();
    }

    @Override
    public void setReaderFactory(RecordReaderFactory readerFactory) {
        if (readerFactory != null && readerFactory instanceof XmlStreamConfigurationAware) {
            ((XmlStreamConfigurationAware)readerFactory).setConfiguration(new XmlStreamConfiguration() {
                public Document getDocument() {
                    return createBaseDocument();
                }
            });
        }
        super.setReaderFactory(readerFactory);
    }
    
    @Override
    protected RecordReader createDefaultReader(Reader in) {
        Document document = createBaseDocument();
        return new XmlReader(in, document);
    }

    @Override
    protected RecordWriter createDefaultWriter(Writer out) {
        return new XmlWriter(out, null);
    }
    
    /**
     * Creates a new document object model for all group nodes in this stream definition.
     * This becomes the 'base' DOM used by the record reader for distinguishing records
     * from other XML artifacts in the input stream.
     * @return the base document object model
     */
    private Document createBaseDocument() {
        Document document = DomUtil.newDocument();
        createDocument((XmlGroupDefinition) getRootGroupDefinition(), document, document);
        return document;
    }
    
    /**
     * Recursively creates the base document object model.
     * @param group the parent group definition
     * @param document a reference to the document being created for creating new nodes
     * @param parent the parent group node to append
     */
    private void createDocument(XmlGroupDefinition group, Document document, Node parent) {
        XmlDefinition xml = group.getXmlDefinition();
        
        Element element = null;
        if (xml.isNode()) {
            element = document.createElementNS(xml.getNamespace(), xml.getName());
            if (!xml.isNamespaceAware()) {
                element.setUserData(XmlReader.IS_NAMESPACE_IGNORED, Boolean.TRUE, null);
            }
            
            parent.appendChild(element);
            parent = element;
        }
        
        for (NodeDefinition nodeDefinition : group.getChildren()) {
            if (nodeDefinition.isRecordDefinition()) {
                continue;
            }
            createDocument((XmlGroupDefinition)nodeDefinition, document, parent);
        }
    }

    /**
     * Creates a new <tt>BeanWriter</tt> for writing to the given output stream.
     * @param out the output stream to write to
     * @return a new <tt>BeanWriter</tt>
     */
    @Override
    public BeanWriter createBeanWriter(Writer out) {
        if (out == null) {
            throw new NullPointerException();
        }
        
        RecordWriter writer = createRecordWriter(out);
        Marshaller marshaller = createMarshaller(null, (XmlGroupDefinition) getRootGroupDefinition());
        return new XmlBeanWriter(marshaller, writer);
    }
    
    /**
     * Recursively creates a tree of <tt>Marshallers</tt> for mashaling bean
     * objects to a XML formatted stream.
     * @param parent the parent Marshaller or <tt>null</tt> if the root group definition
     *   is supplied
     * @param groupDefinition the group definition of the node to create the marshaler for
     * @return a Marshaller for the group definition
     */
    private Marshaller createMarshaller(Marshaller parent, XmlGroupDefinition groupDefinition) {
        if (parent == null) {
            parent = new GroupMarshaller(null, groupDefinition);
        }
        
        for (NodeDefinition definition : groupDefinition.getChildren()) {
            if (definition.isRecordDefinition()) {
                parent.addChild(new RecordMarshaller(parent, (XmlRecordDefinition) definition));
            }
            else {
                XmlGroupDefinition childGroupDefinition = (XmlGroupDefinition) definition;
                
                Marshaller child = createMarshaller(
                    new GroupMarshaller(parent, childGroupDefinition), childGroupDefinition);
                
                parent.addChild(child);
            }
        }
        
        return parent;
    }

    /**
     * BeanWriter implementation for XML formatted streams.
     */
    private class XmlBeanWriter implements BeanWriter, StatefulWriter {
        
        private Marshaller marshaller;
        private RecordWriter writer;
        
        /**
         * Constructs a new <tt>XmlBeanWriter</tt>.
         * @param marshaller the <tt>Marshaller</tt> to use
         * @param writer the <tt>RecordWriter</tt> to write to
         */
        public XmlBeanWriter(Marshaller marshaller, RecordWriter writer) {
            this.writer = writer;
            this.marshaller = marshaller;
        }
        
        /*
         * (non-Javadoc)
         * @see org.beanio.BeanWriter#write(java.lang.Object)
         */
        public void write(Object bean) throws BeanWriterException {
            try {
                if (!marshaller.write(writer, null, bean)) {
                    throw new BeanWriterIOException("No record mapping found for class '"
                        + bean.getClass() + "' at the current stream position");
                }
            }
            catch (IOException e) {
                throw new BeanWriterIOException(e);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.beanio.BeanWriter#write(java.lang.String, java.lang.Object)
         */
        public void write(String recordName, Object bean) throws BeanWriterException {
            try {
                if (!marshaller.write(writer, recordName, bean)) {
                    throw new BeanWriterIOException("No record mapping found named '" + recordName + 
                        "' at the current stream position");
                }
            }
            catch (IOException e) {
                throw new BeanWriterIOException(e);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.beanio.BeanWriter#flush()
         */
        public void flush() throws BeanWriterIOException {
            try {
                writer.flush();
            }
            catch (IOException e) {
                throw new BeanWriterIOException(e);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.beanio.BeanWriter#close()
         */
        public void close() throws BeanWriterIOException {
            try {
                writer.close();
            }
            catch (IOException e) {
                throw new BeanWriterIOException(e);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.beanio.parser.StatefulBeanWriter#updateState(java.lang.String, java.util.Map)
         */
        public void updateState(String namespace, Map<String, Object> state) {
            marshaller.updateState(namespace + ".m", state);
            if (writer instanceof StatefulWriter) {
                ((StatefulWriter)writer).updateState(namespace + ".w", state);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.beanio.parser.StatefulBeanWriter#restoreState(java.lang.String, java.util.Map)
         */
        public void restoreState(String namespace, Map<String, Object> state) throws IllegalStateException {
            marshaller.restoreState(namespace + ".m", state);
            if (writer instanceof StatefulWriter) {
                ((StatefulWriter)writer).restoreState(namespace + ".w", state);
            }
        }
    }
}
