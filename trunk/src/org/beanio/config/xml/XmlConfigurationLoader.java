/*
 * Copyright 2010 Kevin Seim
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
import java.util.Properties;

import javax.xml.XMLConstants;
import javax.xml.parsers.*;

import org.beanio.*;
import org.beanio.config.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class XmlConfigurationLoader implements ConfigurationLoader {

	private static final String BEANIO_XMLNS = "http://www.beanio.org/2011/10";
	private static final String BEANIO_XSD = "beanio-201110.xsd";
	
	private static final EntityResolver defaultEntityResolver = new DefaultEntityResolver();
	
	/**
	 * Constructs a new <tt>XmlConfigurationLoader</tt>.
	 */
	public XmlConfigurationLoader() { }
	
	/*
	 * (non-Javadoc)
	 * @see org.beanio.config.ConfigurationLoader#loadConfiguration(java.io.InputStream)
	 */
	public BeanIOConfig loadConfiguration(InputStream in) throws IOException {
		DocumentBuilderFactory factory = createDocumentBuilderFactory();
		
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			builder.setEntityResolver(createEntityResolver());
			
			// TODO set error handler
			
			Document document = builder.parse(in);
			return loadConfiguration(document.getDocumentElement());
		}
		catch (SAXException ex) {
			throw new BeanIOConfigurationException("Malformed XML", ex);
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
		for (int i=0,j=children.getLength(); i<j; i++) {
			Node node = children.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element child = (Element) node;
			String name = child.getTagName();
			if ("typeHandler".equals(name)) {
				config.addHandler(createHandlerConfig(child));
			}
			else if ("stream".equals(name)) {
				config.addStream(createStreamConfig(child));
			}
		}
		
		return config;
	}
	
	/**
	 * 
	 * @param element
	 * @return
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
	 * 
	 * @param element
	 * @return
	 */
	protected Bean createBeanFactory(Element element) {
		Bean config = new Bean();
		config.setClassName(getAttribute(element, "class"));
		config.setProperties(createProperties(element));
		return config;
	}
	
	/**
	 * 
	 * @param element
	 * @return
	 */
	protected Properties createProperties(Element element) {
		Properties props = null;
		
		NodeList children = element.getChildNodes();
		for (int i=0,j=children.getLength(); i<j; i++) {
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
	 * 
	 * @param stream
	 * @return
	 */
	protected StreamConfig createStreamConfig(Element stream) {
		StreamConfig config = new StreamConfig();
		config.setName(getAttribute(stream, "name"));
		config.setFormat(getAttribute(stream, "format"));
		config.setOrdered(getBooleanAttribute(stream, "ordered", true));
		config.setResourceBundle(getAttribute(stream, "resourceBundle"));
		
		NodeList children = stream.getChildNodes();
		for (int i=0,j=children.getLength(); i<j; i++) {
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
		
		NodeList children = element.getChildNodes();
		for (int i=0,j=children.getLength(); i<j; i++) {
			Node node = children.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element child = (Element) node;
			String name = child.getTagName();
			if ("record".equals(name)) {
				config.addNode(createRecordConfig(child));
			}
			else if("group".equals(name)) {
				config.addNode(createGroupConfig(child));
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
		config.setBeanClass(getAttribute(record, "class"));
		
		NodeList children = record.getChildNodes();
		for (int i=0,j=children.getLength(); i<j; i++) {
			Node node = children.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element child = (Element) node;
			String name = child.getTagName();
			if ("field".equals(name)) {
				config.addField(createFieldConfig(child));
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
		config.setName(getAttribute(element, "name"));
		config.setPosition(getIntAttribute(element, "position", config.getPosition()));
		config.setMinLength(getIntegerAttribute(element, "minLength"));
		config.setMaxLength(getUnboundedIntegerAttribute(element, "maxLength", -1));
		config.setRegex(getAttribute(element, "regex"));
		config.setLiteral(getAttribute(element, "literal"));
		config.setGetter(getAttribute(element, "getter"));
		config.setSetter(getAttribute(element, "setter"));
		config.setHandler(getAttribute(element, "typeHandler"));
		config.setType(getAttribute(element, "type"));
		config.setDefault(getAttribute(element, "default"));
		config.setRequired(getBooleanAttribute(element, "required", config.isRequired()));
		config.setTrim(getBooleanAttribute(element, "trim", config.isTrim()));
		config.setRecordIdentifier(getBooleanAttribute(element, "rid", config.isRecordIdentifier()));
		config.setIgnored(getBooleanAttribute(element, "ignored", config.isIgnored()));
		config.setWidth(getIntAttribute(element, "width", config.getWidth()));
		config.setPadding(getCharAttribute(element, "padding", config.getPadding()));
		config.setJustify(getAttribute(element, "justify"));
		return config;
	}
	
	/**
	 * 
	 * @return
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
	
	protected EntityResolver createEntityResolver() {
		return defaultEntityResolver;
	}

	private static class DefaultEntityResolver implements EntityResolver {
		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			if (BEANIO_XMLNS.equals(systemId)) {
				return new InputSource(XmlConfigurationLoader.class.getResourceAsStream(BEANIO_XSD));
			}
			else {
				return null;
			}
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

	private char getCharAttribute(Element element, String name, char defaultValue) {
		String text = getAttribute(element, name);
		if (text == null || text.length() == 0)
			return defaultValue;
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
