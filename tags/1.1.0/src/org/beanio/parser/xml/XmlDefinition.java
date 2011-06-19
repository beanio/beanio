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

/**
 * Stores common attributes used to define a XML node.
 * <p>
 * Although not enforced, <tt>namespace</tt> and <tt>prefix</tt> should be set to <tt>null</tt>
 * if this node is not namespace aware.  Note that the document object model (DOM) does not 
 * support empty string namespaces to indicate xmlns="".
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlDefinition {

    /** Text XML node type */
    public final static int XML_TYPE_TEXT = 3;
    /** Element XML node type */
    public final static int XML_TYPE_ELEMENT = 2;
    /** Attribute XML node type */
    public final static int XML_TYPE_ATTRIBUTE = 1;
    /** XML type indicating no node */
    public final static int XML_TYPE_NONE = 0;
    
    private int type = XML_TYPE_NONE;
    private String name;
    private String namespace;
    private String prefix;
    private boolean namespaceAware;
    private boolean nillable;
    private XmlDefinition wrapper;
    
    /**
     * Constructs a new <tt>XmlDefinition</tt>.
     */
    public XmlDefinition() { }
    
    /**
     * Returns the XML node type.
     * @return one of 
     *   {@link #XML_TYPE_NONE},
     *   {@link #XML_TYPE_ELEMENT},
     *   {@link #XML_TYPE_ATTRIBUTE}, or
     *   {@link #XML_TYPE_TEXT}
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the type of XML node.
     * @param xmlType one of 
     *   {@link #XML_TYPE_NONE},
     *   {@link #XML_TYPE_ELEMENT},
     *   {@link #XML_TYPE_ATTRIBUTE}, or
     *   {@link #XML_TYPE_TEXT}
     */
    public void setType(int xmlType) {
        this.type = xmlType;
    }
    
    /**
     * Returns the XML local name for this node.
     * @return the XML local name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the XML local name for this node.
     * @param name the XML local name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the namespace of this node.  If there is no namespace for this
     * node, or this node is not namespace aware, <tt>null</tt> is returned.
     * @return the XML namespace of this node
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Sets the namespace of this node.  If there is no namespace for this
     * node, or this node is not namespace aware, the namespace must be <tt>null</tt>.
     * @param namespace the XML namespace
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    /**
     * Sets whether this node is namespace aware.
     * @param namespaceAware <tt>true</tt> if this node uses a namespace for 
     *   unmarshaling and marshaling this node
     */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }
    
    /**
     * Returns <tt>true</tt> if a namespace was configured for this node, and is
     * therefore used to unmarshal and marshal the node.
     * @return <tt>true</tt> if this node uses a namespace for matching and 
     *   formatting this node
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * Returns the namespace prefix for marshaling this node, or <tt>null</tt>
     * if the namespace should override the default namespace.
     * @return the namespace prefix
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the namespace prefix for marshaling the node.  If set to <tt>null</tt>
     * the namespace will override the default namespace when this node is marshaled.
     * @param prefix the namespace prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    /**
     * Returns whether this node is nillable.
     * @return <tt>true</tt> if this node is nillable
     */
    public boolean isNillable() {
        return nillable;
    }

    /**
     * Sets whether this node is nillable.
     * @param nillable set to <tt>true</tt> if this node is nillable
     */
    public void setNillable(boolean nillable) {
        this.nillable = nillable;
    }
    
    /**
     * Returns <tt>true</tt> if the XML node type is not none.
     * @return <tt>true</tt> if the XML node type is not none
     */
    public boolean isNode() {
        return type != XmlDefinition.XML_TYPE_NONE;
    }

    /**
     * Returns <tt>true</tt> if the XML node type is element.
     * @return <tt>true</tt> if the XML node type is element
     */
    public boolean isElement() {
        return type == XmlDefinition.XML_TYPE_ELEMENT;
    }
    
    /**
     * Returns <tt>true</tt> if the XML node type is attribute.
     * @return <tt>true</tt> if the XML node type is attribute
     */
    public boolean isAttribute() {
        return type == XmlDefinition.XML_TYPE_ATTRIBUTE;
    }
    
    /**
     * Returns the XML element for wrapping this XML node.
     * @return the XML wrapper element
     */
    public XmlDefinition getWrapper() {
        return wrapper;
    }

    /**
     * Sets the XML element for wrapping this XML node.
     * @param wrapper the XML wrapper element
     */
    public void setWrapper(XmlDefinition wrapper) {
        this.wrapper = wrapper;
    }
    
    /**
     * Returns whether this XML node is wrapped by another element.
     * @return <tt>true</tt> if this XML node is wrapped by another element
     */
    public boolean isWrapped() {
        return wrapper != null;
    }

    @Override
    public String toString() { 
        return getClass().getSimpleName() +
            "{type=" + type +
            ", name=" + name + 
            ", prefix=" + prefix +
            ", namespace=" + namespace +
            ", namespaceAware=" + namespaceAware +
            ", nillable=" + nillable +
            ", wrapper=" + wrapper +
            "}";
    }
}
