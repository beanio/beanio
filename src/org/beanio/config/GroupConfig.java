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
package org.beanio.config;

import java.util.*;

/**
 * Stores configuration settings for a record group.  Records and groups
 * are used to define the layout of stream.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class GroupConfig extends NodeConfig {

    private List<NodeConfig> nodeList = new ArrayList<NodeConfig>();

    private String xmlType;
    private String xmlName;
    private String xmlNamespace;
    private String xmlPrefix;
    
    /**
     * Returns {@link NodeConfig#GROUP}.
     */
    @Override
    public char getType() {
        return NodeConfig.GROUP;
    }

    /**
     * Adds a child record or subgroup to this group.
     * @param child the child node to add
     */
    public void addChild(NodeConfig child) {
        nodeList.add(child);
    }

    /**
     * Returns the child nodes of this group.
     * @return the list of child nodes
     */
    public List<NodeConfig> getChildren() {
        return nodeList;
    }

    /**
     * Sets the list of child nodes.
     * @param children the list of child nodes
     */
    public void setChildren(List<NodeConfig> children) {
        if (children == null) {
            this.nodeList.clear();
        }
        else {
            this.nodeList = children;
        }
    }
    
    /**
     * Returns the XML node type of this group.
     * @return the XML node type
     * @since 1.1
     * @see XmlTypeConstants
     */
    public String getXmlType() {
        return xmlType;
    }

    /**
     * Sets the XML node type of this group.
     * @param xmlType the XML node type
     * @since 1.1
     * @see XmlTypeConstants
     */
    public void setXmlType(String xmlType) {
        this.xmlType = xmlType;
    }

    /**
     * Returns XML element local name of this group.
     * @return the XML element local name of this group
     * @since 1.1
     */
    public String getXmlName() {
        return xmlName;
    }

    /**
     * Sets the XML element local name of this group.  If set to <tt>null</tt> 
     * (the default), the XML name defaults to the group name.
     * @param xmlName the XML element local name of this group
     * @since 1.1
     */
    public void setXmlName(String xmlName) {
        this.xmlName = xmlName;
    }

    /**
     * Returns the XML namespace for this group element.
     * @return the XML namespace
     * @since 1.1
     */
    public String getXmlNamespace() {
        return xmlNamespace;
    }

    /**
     * Sets the XML namespace for this group element.  If set to <tt>null</tt>
     * (default), the namespace is inherited from its parent group, or if this group
     * does not have a parent, namespaces are ignored.
     * @param xmlNamespace the XML namespace
     * @since 1.1
     */
    public void setXmlNamespace(String xmlNamespace) {
        this.xmlNamespace = xmlNamespace;
    }

    /**
     * Returns the XML prefix for the namespace assigned to this group element.
     * @return the XML namespace prefix
     * @since 1.1
     */
    public String getXmlPrefix() {
        return xmlPrefix;
    }

    /**
     * Sets the XML prefix for the namespace assigned to this group element.  If set to
     * <tt>null</tt> and a namespace is set, the namespace will replace the default namespace
     * when marshaling the group.  If a namespace is not set, the prefix is ignored.
     * @param xmlPrefix the XML namespace prefix
     * @since 1.1
     */
    public void setXmlPrefix(String xmlPrefix) {
        this.xmlPrefix = xmlPrefix;
    }
}
