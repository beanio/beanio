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

import java.io.*;
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.stream.*;

import org.beanio.stream.RecordWriter;
import org.beanio.util.Settings;
import org.w3c.dom.*;

/**
 * A <tt>XmlWriter</tt> is used to write records to a XML output stream.  A document
 * object model (DOM) is used to represent a record.  Group elements, as indicated
 * by a user data key (see below), are not closed when a record is written.  When
 * <tt>write(null)</tt> is called, an open group element is closed.  Finally, calling
 * <tt>flush()</tt> will close all remaining group elements and complete the document.
 * <p>
 * A <tt>XmlWriter</tt> makes use of the DOM user data feature to pass additional
 * information to and from the parser.  The <tt>IS_GROUP_ELEMENT</tt> user data is 
 * a <tt>Boolean</tt> value added to an element to indicate the element is group.  
 * And the <tt>IS_NAMESPACE_IGNORED</tt> user data is a <tt>Boolean</tt> value set on 
 * elements where the XML namespace should be ignored when writing to the output stream.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlWriter implements RecordWriter {

    /**
     * The DOM user data key to indicate whether the namespace of a DOM element
     * should be ignored when writing to the output stream.  The value must be 
     * of type <tt>java.lang.Boolean</tt>. 
     */
    public static final String IS_NAMESPACE_IGNORED = "isNamespaceIgnored";
    
    /**
     * The DOM user data key to indicate a DOM element is a group element and should
     * be left "open" when the record is written to the output stream.  The value must 
     * of type <tt>java.lang.Boolean</tt>. 
     */
    public static final String IS_GROUP_ELEMENT = "isGroup";
    
    private static final String DEFAULT_LINE_SEPARATOR = System.getProperty("line.separator");
    private static final XMLOutputFactory xmlOutputFactory;
    static {
        xmlOutputFactory = XMLOutputFactory.newInstance();
    }
    
    /* The XML stream writer to write to */
    private XMLStreamWriter out;
    /* XML writer configuration */
    private XmlWriterConfiguration config;
    /* Map of namespace prefixes to namespaces */
    private Map<String,String> namespaceMap = new HashMap<String,String>();
    /* String used to indent new lines of XML */
    private String indentation = "";
    
    private int level = 0;
    private Stack stack;
    /* whether a XML header needs to be output before writing a record */
    private boolean outputHeader = false;
    /* the number of auto generated namespace prefixes */
    private int namespaceCount = 0;
    
    /**
     * Constructs a new <tt>XmlWriter</tt>.
     * @param writer the output stream to write to
     */
    public XmlWriter(Writer writer) {
        this(writer, null);
    }
    
    /**
     * Constructs a new <tt>XmlWriter</tt>.
     * @param writer the output stream to write to
     * @param config the XML writer configuration
     */
    public XmlWriter(Writer writer, XmlWriterConfiguration config) {
        if (writer == null) {
            throw new IllegalArgumentException("writer is null");
        }
        
        if (config == null) {
            // create a default configuration
            this.config = new XmlWriterConfiguration();
        }
        else {
            // the configuration is cloned to prevent changes during execution
            this.config = config.clone();
        }
        init();

        try {
            xmlOutputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, true);
            out = xmlOutputFactory.createXMLStreamWriter(writer);
        }
        catch (XMLStreamException e) {
            throw new IllegalArgumentException("Failed to create XMLStreamWriter: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initializes this writer after the configuration has been set.
     */
    private void init() {
        if (config.getLineSeparator() == null) {
            config.setLineSeparator(DEFAULT_LINE_SEPARATOR);
        }
        
        if (config.isIndentationEnabled()) {
            StringBuffer b = new StringBuffer();
            for (int i=0; i<config.getIndentation(); i++) {
                b.append(' ');
            }
            this.indentation = b.toString();
        }
        
        this.outputHeader = config.isHeaderEnabled();
        this.namespaceMap = new HashMap<String,String>(config.getNamespaceMap());
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordWriter#write(java.lang.Object)
     */
    public void write(Object record) throws IOException {
        try {
            // write the XMl header if needed
            if (outputHeader) {
                String encoding = config.getEncoding();
                if (encoding != null && !"".equals(encoding)) {
                    out.writeStartDocument(encoding, config.getVersion());
                }
                else {
                    out.writeStartDocument(config.getVersion());
                }
                if (config.isIndentationEnabled()) {
                    out.writeCharacters(config.getLineSeparator());
                }
                outputHeader = false;
            }
            
            // a null record indicates we need to close an element
            if (record == null) {
                if (stack != null) {
                    endElement();
                }
            }
            // otherwise we write the record (i.e. DOM tree) to the stream
            else {
                write(((Document) record).getDocumentElement(), config.isIndentationEnabled());
            }
        }
        catch (XMLStreamException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
    }
    
    /**
     * Recursively writes an element to the XML stream writer.
     * @param element the DOM element to write
     * @param indentationEnabled set to <tt>true</tt> if indentation is enabled
     * @throws XMLStreamException
     */
    private void write(Element element, boolean indentationEnabled) throws XMLStreamException {
        
        String name = element.getLocalName();
        String prefix = element.getPrefix();
        
        boolean ignoreNamespace = false;
        String namespace = element.getNamespaceURI();
        if (namespace == null) {
            if (Boolean.TRUE.equals(element.getUserData(IS_NAMESPACE_IGNORED))) {
                ignoreNamespace = true;
            }
            namespace = "";
        }
        
        boolean empty = false;
        
        // start the element
        if (stack == null) {
            if (ignoreNamespace) {
                out.writeStartElement(name);
                push("");
            }
            else if (prefix != null) {
                out.writeStartElement(prefix, name, namespace);
                push(null);
                stack.addNamespace(prefix, namespace);
            }
            else {
                out.writeStartElement(name);
                out.writeNamespace("", namespace);
                push(namespace);
            }
            
            for (Map.Entry<String,String> ns : namespaceMap.entrySet()) {
                out.writeNamespace(ns.getKey(), ns.getValue());
                stack.addNamespace(ns.getKey(), ns.getValue());
            }
        }
        else {
            if (indentationEnabled) {
                newLine();
            }
            
            empty = !element.hasChildNodes();
            
            if (ignoreNamespace || (stack.isNamesapce(namespace)) && prefix == null) {
                if (empty) {
                    out.writeEmptyElement(name);   
                }
                else {
                    out.writeStartElement(name);
                }
                push(stack.ns);
            }
            else {
                boolean addNamespace = false;
                if (prefix == null) {
                    prefix = stack.findPrefix(namespace);
                }
                else {
                    addNamespace = true;
                }
                
                if (prefix == null) {
                    if (empty) {
                        out.writeEmptyElement(name);
                    }
                    else {
                        out.writeStartElement(name);
                    }
                    out.writeNamespace("", namespace);
                    push(namespace);
                }
                else {
                    if (empty) {
                        out.writeEmptyElement(prefix, name, namespace);
                    }
                    else {
                        out.writeStartElement(prefix, name, namespace);
                    }
                    
                    // if the element uses its own prefix, push the parent namespace again
                    if ("".equals(prefix)) {
                        push(namespace);
                    }
                    else {
                        push(stack.ns);
                    }
                }
                
                if (addNamespace) {
                    stack.addNamespace(prefix, namespace);
                }
            }
        }
        
        // write attributes
        NamedNodeMap map = element.getAttributes();
        for (int i=0,j=map.getLength(); i<j; i++) {
            Attr att = (Attr) map.item(i);
            String attName = att.getLocalName();
            String attNamespace = att.getNamespaceURI();
            String attPrefix = att.getPrefix();
            if (attNamespace == null) {
                out.writeAttribute(attName, att.getValue());
            }
            else {
                if (attPrefix == null) {
                    attPrefix = stack.findPrefix(attNamespace);
                }
                if (attPrefix == null) {
                    attPrefix = namespaceMap.get(attNamespace);
                    if (attPrefix == null) {
                        attPrefix = createNamespace(attNamespace);
                    }
                    out.writeAttribute(attPrefix, attNamespace, attName, att.getValue());
                }
                else {
                    out.writeAttribute(attPrefix, attNamespace, attName, att.getValue());
                }
            }
        }
        
        // if the element contains text, we disable indentation 
        if (indentationEnabled) {
            Node child = element.getFirstChild();
            while (child != null) {
                if (child.getNodeType() == Node.TEXT_NODE) {
                    indentationEnabled = false;
                    break;
                }
                child = child.getNextSibling();
            }
        }
        
        boolean isParent = false;
        
        // write children
        Node child = element.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                write((Element) child, indentationEnabled);
                isParent = true;
            }
            else if (child.getNodeType() == Node.TEXT_NODE) {
                out.writeCharacters(((Text)child).getData());
            }
            child = child.getNextSibling();
        }
        
        // end the element if it is not a group
        if (!Boolean.TRUE.equals(element.getUserData(IS_GROUP_ELEMENT))) {
            pop();
            if (!empty) {
                if (isParent && indentationEnabled) {
                    newLine();
                }
                out.writeEndElement();
            }            
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordWriter#flush()
     */
    public void flush() throws IOException {
        try {
            while (stack != null) {
                endElement();
            }
            out.writeEndDocument();
            out.flush();
        }
        catch (XMLStreamException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordWriter#close()
     */
    public void close() throws IOException {
        try {
            out.close();
        }
        catch (XMLStreamException e) {
            throw (IOException) new IOException(e.getMessage()).initCause(e);
        }
    }
    
    /**
     * Auto generates a prefix for a given namespace uri.
     * @param uri the namespace uri
     * @return the unique auto generated namespace prefix
     */
    private String createNamespace(String uri) {
        String prefix;
        if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI.equals(uri)) {
            prefix = Settings.getInstance().getProperty(Settings.DEFAULT_XSI_NAMESPACE_PREFIX);
        }
        else {
            prefix = "ns" + (++namespaceCount);
        }
        
        while (namespaceMap.containsValue(prefix)) {
            prefix = "ns" + (++namespaceCount);
        } 
        
        namespaceMap.put(uri, prefix);
        return prefix;
    }
    
    /**
     * Terminates the current line and indents the start of the next line.
     * @throws XMLStreamException
     */
    private void newLine() throws XMLStreamException {
        if (config.isIndentationEnabled()) {
            out.writeCharacters(config.getLineSeparator());
            for (int i=0,j=level; i<j; i++) {
                out.writeCharacters(indentation);
            }
        }
    }
    
    private void endElement() throws XMLStreamException {
        pop();
        newLine();
        out.writeEndElement();
    }
    
    private void push(String namespace) {
        Stack n = new Stack();
        n.ns = namespace;
        n.parent = stack;
        stack = n;
        ++level;
    }
    
    private Stack pop() {
        Stack node = stack;
        stack = stack.parent;
        --level;
        return node;
    }

    private static class Stack {
        private Stack parent;
        private String ns;
        private Map<String,String> nsMap;
        
        public void addNamespace(String prefix, String namespace) {
            if (nsMap == null) {
                nsMap = new HashMap<String,String>();
            }
            nsMap.put(namespace, prefix);
        }
        
        public String findPrefix(String namespace) {
            String prefix = null;
            if (nsMap  != null) {
                prefix = nsMap.get(namespace);
                if (prefix != null) {
                    return prefix;
                }
            }
            if (parent != null) {
                return parent.findPrefix(namespace);
            }
            return null;
        }
        
        public boolean isNamesapce(String namespace) {
            return ns != null && ns.equals(namespace);
        }
    }
}
