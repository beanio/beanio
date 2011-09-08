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

import javax.xml.XMLConstants;

import org.w3c.dom.*;

/**
 * Utility class for working with a document object model and <tt>XmlDefinition</tt>'s. 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlNodeUtil {

    private XmlNodeUtil() { }
    
    /**
     * Unwraps an XML element when a wrapper is configured for the given XML definition.
     * Otherwise, just returns <tt>parent</tt>.
     * @param parent the parent XML element to unwrap
     * @param definition the definition of the XML node 
     * @return the wrapper child element if found, or <tt>null</tt> if not, or the parent
     *   if the XML node is not wrapped
     */
    public static Element unwrap(Element parent, XmlDefinition definition) {
        XmlDefinition wrapper = definition.getWrapper();
        if (wrapper != null) {
            Node node = findChild(parent, wrapper, 0);
            if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
                return null;
            }
            return (Element) node;
        }
        return parent;
    }
    
    /**
     * Tests if an element is nil.
     * @param element the element to test
     * @return <tt>true</tt> if the element is nil
     */
    public static boolean isNil(Element element) {
        String nil = element.getAttributeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "nil");
        return "true".equals(nil) || "1".equals(nil);
    }
    
    /**
     * Returns the value of an attribute for an element.
     * @param element the element to check
     * @param definition the definition of the attribute to retrieve from the element
     * @return the defined attribute value, or <tt>null</tt> if the attribute was not
     *   found on the element
     */
    public static String getAttribute(Element element, XmlDefinition definition) {
        element = unwrap(element, definition);
        if (element == null) {
            return null;
        }
        
        if (definition.isNamespaceAware()) {
            if (element.hasAttributeNS(definition.getNamespace(), definition.getName())) {
                return element.getAttributeNS(definition.getNamespace(), definition.getName());
            }
        }
        else {
            if (element.hasAttribute(definition.getName())) {
                return element.getAttribute(definition.getName());
            }
        }
        return null;
    }
    
    /**
     * Returns the child text from a DOM node after adjusting for any node wrapping.
     * @param node the node to parse
     * @param definition the definition of the text node
     * @return the node text, or <tt>null</tt> if the node did not contain any text
     */
    public static String getText(Node node, XmlDefinition definition) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return null;
        }
        Node parent = unwrap((Element)node, definition);
        if (parent == null) {
            return null;
        }
        return getText(parent);
    }
    
    /**
     * Returns the child text from a DOM node.
     * @param node the node to parse
     * @return the node text, or <tt>null</tt> if the node did not contain any text
     */
    public static String getText(Node node) {
        StringBuilder s = null;
        Node child = node.getFirstChild();
        while (child != null) {    
            if (child.getNodeType() == Node.TEXT_NODE) {
                if (s == null) {
                    s = new StringBuilder();
                }
                s.append(((Text)child).getTextContent());
            }
            child = child.getNextSibling();
        }
        return s == null ? null : s.toString();
    }
    
    /**
     * Returns a sibling element that matches a given definition, or <tt>null</tt> if
     * no match is found.
     * @param sibling the sibling DOM element to begin the search
     * @param definition the node to search for
     * @return the matching element, or <tt>null</tt> if not found
     */
    public static Element findSibling(Element sibling, XmlDefinition definition) {
        String xmlName = definition.getName();
        String xmlNamespace = definition.getNamespace();
        
        Node node = sibling;
        if (node == null) {
            return null;
        }
        
        while ((node = node.getNextSibling()) != null) {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if (!element.getLocalName().equals(xmlName)) {
                continue;
            }
            if (definition.isNamespaceAware()) {
                String ns = element.getNamespaceURI();
                if (ns == null) {
                    if (xmlNamespace != null) {
                        continue;
                    }
                }
                else {
                    if (!ns.equals(xmlNamespace)) {
                        continue;
                    }
                }
            }
            return element;
        }
        return null;
    }
 
    /**
     * Finds the Nth matching child of a DOM element.
     * @param parent the parent DOM node
     * @param definition the node definition to search for
     * @param offset the occurrence of the matching node
     * @return the matching element, or <tt>null</tt> if no match is found
     */
    public static Element findChild(Node parent, XmlDefinition definition, int offset) {
        
        Node node = parent;
        if (definition.getWrapper() != null) {
            node = findChild(parent, definition.getWrapper(), 0);
        }
        if (node != null) {
            node = node.getFirstChild();
        }
        if (node == null) {
            return null;
        }
        
        String xmlName = definition.getName();
        String xmlNamespace = definition.getNamespace();
        
        int count = 0;
        do {
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if (!element.getLocalName().equals(xmlName)) {
                continue;
            }
            if (definition.isNamespaceAware()) {
                String ns = element.getNamespaceURI();
                if (ns == null) {
                    if (xmlNamespace != null && !"".equals(xmlNamespace)) {
                        continue;
                    }
                }
                else {
                    if (!ns.equals(xmlNamespace)) {
                        continue;
                    }
                }
            }
            if (count == offset) {
                return element;
            }
            ++count;
            
        } while ((node = node.getNextSibling()) != null);
        
        return null;
    }
}
