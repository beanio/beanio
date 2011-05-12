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
import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;

import org.beanio.*;
import org.beanio.config.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * A configuration loader implementation for loading XML formatted mapping files.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class XmlConfigurationLoader implements ConfigurationLoader {

    private static final String BEANIO_XMLNS = "http://www.beanio.org/2011/01";
    private static final String BEANIO_XSD = "/org/beanio/xsd/2011/01/mapping.xsd";

    private static final EntityResolver defaultEntityResolver = new DefaultEntityResolver();

    /**
     * Constructs a new <tt>XmlConfigurationLoader</tt>.
     */
    public XmlConfigurationLoader() { }

    /*
     * (non-Javadoc)
     * @see org.beanio.config.ConfigurationLoader#loadConfiguration(java.io.InputStream)
     */
    public BeanIOConfig loadConfiguration(InputStream in) throws IOException,
        BeanIOConfigurationException {
        DocumentBuilderFactory factory = createDocumentBuilderFactory();

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(createEntityResolver());

            final List<String> errorMessages = new ArrayList<String>();

            builder.setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException exception) throws SAXException {
                    errorMessages.add("Error at line " + exception.getLineNumber() +
                        ": " + exception.getMessage());
                }

                public void error(SAXParseException exception) throws SAXException {
                    errorMessages.add("Error at line " + exception.getLineNumber() +
                        ": " + exception.getMessage());
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });

            Document document = builder.parse(in);
            if (!errorMessages.isEmpty()) {
                StringBuffer message = new StringBuffer();
                message.append("Invalid mapping file");
                for (String s : errorMessages) {
                    message.append("\n  ==> ");
                    message.append(s);
                }
                throw new BeanIOConfigurationException(message.toString());
            }

            return loadConfiguration(document.getDocumentElement());
        }
        catch (SAXException ex) {
            throw new BeanIOConfigurationException("Malformed mapping file", ex);
        }
        catch (ParserConfigurationException ex) {
            throw new BeanIOConfigurationException("Failed to load suitable DOM implementation", ex);
        }
    }

    /**
     * Parses BeanIO configuration from a DOM element.
     * @param element the DOM element to parse
     * @return the parsed BeanIO configuration
     */
    protected BeanIOConfig loadConfiguration(Element element) {
        BeanIOConfig config = new BeanIOConfig();

        NodeList children = element.getChildNodes();
        for (int i = 0, j = children.getLength(); i < j; i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element child = (Element) node;
            String name = child.getTagName();
            if ("typeHandler".equals(name)) {
                config.addTypeHandler(createHandlerConfig(child));
            }
            else if ("stream".equals(name)) {
                config.addStream(createStreamConfig(child));
            }
        }

        return config;
    }

    /**
     * Parses a <tt>TypeHandlerConfig</tt> from a DOM element. 
     * @param element the DOM element to parse
     * @return the new <tt>TypeHandlerConfig</tt>
     */
    protected TypeHandlerConfig createHandlerConfig(Element element) {
        TypeHandlerConfig config = new TypeHandlerConfig();
        config.setName(getAttribute(element, "name"));
        config.setType(getAttribute(element, "type"));
        config.setClassName(getAttribute(element, "class"));
        config.setProperties(createProperties(element));
        return config;
    }

    /**
     * Parses a <tt>Bean</tt> from a DOM element. 
     * @param element the DOM element to parse
     * @return the new <tt>Bean</tt>
     */
    protected Bean createBeanFactory(Element element) {
        Bean config = new Bean();
        config.setClassName(getAttribute(element, "class"));
        config.setProperties(createProperties(element));
        return config;
    }

    /**
     * Parses <tt>Properties</tt> from a DOM element. 
     * @param element the DOM element to parse
     * @return the new <tt>Properties</tt>
     */
    protected Properties createProperties(Element element) {
        Properties props = null;

        NodeList children = element.getChildNodes();
        for (int i = 0, j = children.getLength(); i < j; i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element child = (Element) node;
            String name = child.getTagName();
            if ("property".equals(name)) {
                if (props == null) {
                    props = new Properties();
                }

                props.put(
                    child.getAttribute("name"),
                    child.getAttribute("value"));
            }
        }
        return props;
    }

    /**
     * Parses a <tt>StreamConfig</tt> from a DOM element. 
     * @param element the DOM element to parse
     * @return the new <tt>StreamConfig</tt>
     */
    protected StreamConfig createStreamConfig(Element element) {
        StreamConfig config = new StreamConfig();
        config.setName(getAttribute(element, "name"));
        config.setFormat(getAttribute(element, "format"));
        config.setOrdered(getBooleanAttribute(element, "ordered", true));
        config.setResourceBundle(getAttribute(element, "resourceBundle"));
        config.setMinOccurs(getIntAttribute(element, "minOccurs", 0));
        Integer maxOccurs = getUnboundedIntegerAttribute(element, "maxOccurs", -1);
        if (maxOccurs == null)
            maxOccurs = 1;
        config.setMaxOccurs(maxOccurs);
        
        config.getRootGroupConfig().setXmlName(getAttribute(element, "xmlName"));
        config.getRootGroupConfig().setXmlNamespace(getOptionalAttribute(element, "xmlNamespace"));
        config.getRootGroupConfig().setXmlPrefix(getOptionalAttribute(element, "xmlPrefix"));
        config.getRootGroupConfig().setXmlType(getOptionalAttribute(element, "xmlType"));
        
        NodeList children = element.getChildNodes();
        for (int i = 0, j = children.getLength(); i < j; i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element child = (Element) node;
            String name = child.getTagName();
            if ("typeHandler".equals(name)) {
                config.addHandler(createHandlerConfig(child));
            }
            else if ("reader".equals(name)) {
                config.setReaderFactory(createBeanFactory(child));
            }
            else if ("writer".equals(name)) {
                config.setWriterFactory(createBeanFactory(child));
            }
            else if ("record".equals(name)) {
                config.addNode(createRecordConfig(child));
            }
            else if ("group".equals(name)) {
                config.addNode(createGroupConfig(child));
            }
        }

        return config;
    }

    /**
     * Parses a group configuration from a DOM element.
     * @param element the DOM element to parse
     * @return the parsed group configuration
     */
    protected GroupConfig createGroupConfig(Element element) {
        GroupConfig config = new GroupConfig();
        config.setName(getAttribute(element, "name"));
        config.setOrder(getIntAttribute(element, "order", -1));
        config.setMinOccurs(getIntegerAttribute(element, "minOccurs"));
        config.setMaxOccurs(getUnboundedIntegerAttribute(element, "maxOccurs", -1));
        config.setXmlName(getAttribute(element, "xmlName"));
        config.setXmlNamespace(getOptionalAttribute(element, "xmlNamespace"));
        config.setXmlPrefix(getOptionalAttribute(element, "xmlPrefix"));
        config.setXmlType(getOptionalAttribute(element, "xmlType"));
        
        NodeList children = element.getChildNodes();
        for (int i = 0, j = children.getLength(); i < j; i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element child = (Element) node;
            String name = child.getTagName();
            if ("record".equals(name)) {
                config.addChild(createRecordConfig(child));
            }
            else if ("group".equals(name)) {
                config.addChild(createGroupConfig(child));
            }
        }

        return config;
    }

    /**
     * Parses a record configuration from the given DOM element.
     * @param record the DOM element to parse
     * @return the parsed record configuration
     */
    protected RecordConfig createRecordConfig(Element record) {
        RecordConfig config = new RecordConfig();
        config.setName(getAttribute(record, "name"));
        config.setOrder(getIntAttribute(record, "order", -1));
        config.setMinOccurs(getIntegerAttribute(record, "minOccurs"));
        config.setMaxOccurs(getUnboundedIntegerAttribute(record, "maxOccurs", -1));
        config.setMinLength(getIntegerAttribute(record, "minLength"));
        config.setMaxLength(getUnboundedIntegerAttribute(record, "maxLength", -1));
        
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setName(config.getName());
        beanConfig.setType(getAttribute(record, "class"));
        beanConfig.setXmlName(getAttribute(record, "xmlName"));
        beanConfig.setXmlNamespace(getOptionalAttribute(record, "xmlNamespace"));
        beanConfig.setXmlPrefix(getOptionalAttribute(record, "xmlPrefix"));
        
        NodeList children = record.getChildNodes();
        for (int i = 0, j = children.getLength(); i < j; i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element child = (Element) node;
            String name = child.getTagName();
            if ("field".equals(name)) {
                beanConfig.addProperty(createFieldConfig(child));
            }
            else if ("bean".equals(name)) {
                beanConfig.addProperty(createBeanConfig(child));
            }
        }
        
        config.setBean(beanConfig);
        return config;
    }
    
    private void populatePropertyConfig(PropertyConfig config, Element element) {
        config.setName(getAttribute(element, "name"));
        config.setGetter(getAttribute(element, "getter"));
        config.setSetter(getAttribute(element, "setter"));
        config.setCollection(getAttribute(element, "collection"));
        config.setMinOccurs(getIntegerAttribute(element, "minOccurs"));
        config.setMaxOccurs(getUnboundedIntegerAttribute(element, "maxOccurs", -1));
    }
    
    /**
     * Parses a bean configuration from a DOM element.
     * @param element the DOM element to parse
     * @return the parsed bean configuration
     */
    protected BeanConfig createBeanConfig(Element element) {
        BeanConfig config = new BeanConfig();
        populatePropertyConfig(config, element);
        config.setType(getAttribute(element, "class"));
        config.setXmlName(getAttribute(element, "xmlName"));
        config.setXmlNamespace(getOptionalAttribute(element, "xmlNamespace"));
        config.setXmlPrefix(getOptionalAttribute(element, "xmlPrefix"));
        config.setXmlType(getOptionalAttribute(element, "xmlType"));
        config.setNillable(getBooleanAttribute(element, "nillable", config.isNillable()));
        
        NodeList children = element.getChildNodes();
        for (int i = 0, j = children.getLength(); i < j; i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            Element child = (Element) node;
            String name = child.getTagName();
            if ("field".equals(name)) {
                config.addProperty(createFieldConfig(child));
            }
            else if ("bean".equals(name)) {
                config.addProperty(createBeanConfig(child));
            }
        }
        
        return config;
    }

    /**
     * Parses a field configuration from a DOM element.
     * @param element the DOM element to parse
     * @return the parsed field configuration
     */
    protected FieldConfig createFieldConfig(Element element) {
        FieldConfig config = new FieldConfig();
        populatePropertyConfig(config, element);
        config.setPosition(getIntAttribute(element, "position", config.getPosition()));
        config.setMinLength(getIntegerAttribute(element, "minLength"));
        config.setMaxLength(getUnboundedIntegerAttribute(element, "maxLength", -1));
        config.setRegex(getAttribute(element, "regex"));
        config.setLiteral(getAttribute(element, "literal"));
        config.setTypeHandler(getAttribute(element, "typeHandler"));
        config.setType(getAttribute(element, "type"));
        config.setFormat(getAttribute(element, "format"));
        config.setDefault(getAttribute(element, "default"));
        config.setRequired(getBooleanAttribute(element, "required", config.isRequired()));
        config.setTrim(getBooleanAttribute(element, "trim", config.isTrim()));
        config.setRecordIdentifier(getBooleanAttribute(element, "rid", 
            config.isRecordIdentifier()));
        config.setIgnored(getBooleanAttribute(element, "ignore", config.isIgnored()));
        config.setLength(getIntAttribute(element, "length", config.getLength()));
        config.setPadding(getCharacterAttribute(element, "padding"));
        config.setJustify(getAttribute(element, "justify"));
        config.setXmlType(getAttribute(element, "xmlType"));
        config.setXmlName(getAttribute(element, "xmlName"));
        config.setXmlNamespace(getOptionalAttribute(element, "xmlNamespace"));
        config.setXmlPrefix(getOptionalAttribute(element, "xmlPrefix"));
        config.setNillable(getBooleanAttribute(element, "nillable", config.isNillable()));
        return config;
    }

    /**
     * Creates an XML document builder factory.
     * @return the new <tt>DocumentBuilderFactory</tt>
     */
    protected DocumentBuilderFactory createDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setCoalescing(true);
        factory.setNamespaceAware(true);
        factory.setValidating(true);

        try {
            factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                XMLConstants.W3C_XML_SCHEMA_NS_URI);
        }
        catch (IllegalArgumentException ex) {
            throw new BeanIOException("Unable to validate using XSD: JAXP provider [" +
                factory + "] does not support XML Schema.", ex);
        }
        return factory;
    }

    /**
     * Returns the XML entity resolver for loading the BeanIO schema definition or 
     * other reference entities.
     * @return XML entity resolver
     */
    protected EntityResolver createEntityResolver() {
        return defaultEntityResolver;
    }

    private static class DefaultEntityResolver implements EntityResolver {
        public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
            IOException {
            if (publicId == null && (BEANIO_XMLNS.equals(systemId) ||
                (BEANIO_XMLNS + "/mapping.xsd").equals(systemId))) {
                return new InputSource(XmlConfigurationLoader.class.getResourceAsStream(BEANIO_XSD));
            }
            else {
                return null;
            }
        }
    }
    
    private String getOptionalAttribute(Element element, String name) {
        Attr att = element.getAttributeNode(name);
        if (att == null) {
            return null;
        }
        else {
            return att.getTextContent();
        }
    }
    
    private String getAttribute(Element element, String name) {
        String value = element.getAttribute(name);
        if ("".equals(value))
            value = null;
        return value;
    }

    private int getIntAttribute(Element element, String name, int defaultValue) {
        String text = getAttribute(element, name);
        if (text == null)
            return defaultValue;
        return Integer.parseInt(text);
    }

    /*
    private char getCharAttribute(Element element, String name, char defaultValue) {
        String text = getAttribute(element, name);
        if (text == null || text.length() == 0)
            return defaultValue;
        return text.charAt(0);
    }
    */

    private Character getCharacterAttribute(Element element, String name) {
        String text = getAttribute(element, name);
        if (text == null || text.length() == 0)
            return null;
        return text.charAt(0);
    }
    
    private Integer getIntegerAttribute(Element element, String name) {
        String text = getAttribute(element, name);
        if (text == null)
            return null;
        return Integer.parseInt(text);
    }

    private Integer getUnboundedIntegerAttribute(Element element, String name, int unboundedValue) {
        String text = getAttribute(element, name);
        if (text == null)
            return null;
        if ("unbounded".equals(text))
            return unboundedValue;
        return Integer.parseInt(text);
    }

    private boolean getBooleanAttribute(Element element, String name, boolean defaultValue) {
        String text = getAttribute(element, name);
        if (text == null)
            return defaultValue;
        return "true".equals(text) || "1".equals(text);
    }
}
