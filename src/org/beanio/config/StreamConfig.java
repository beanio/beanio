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
 * Stores configuration settings for a stream.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class StreamConfig {

    private String name;
    private String format;
    private String resourceBundle;
    private boolean ordered = true;

    private List<TypeHandlerConfig> handlerList = new ArrayList<TypeHandlerConfig>();
    private GroupConfig root;
    private Bean readerFactory;
    private Bean writerFactory;

    /**
     * Constructs a new <tt>StreamConfig</tt>.
     */
    public StreamConfig() {
        root = new GroupConfig();
        root.setMinOccurs(0);
        root.setMaxOccurs(1);
        root.setOrder(1);
    }

    /**
     * Returns the name of this stream.
     * @return the stream name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this stream.
     * @param name the stream name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the format of this stream.
     * @return the stream format
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the format of this stream.
     * @param format the stream format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * Returns whether stream components (records and groups) are ordered.
     * @return <tt>true</tt> if stream components are ordered
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * Sets whether stream components (records and groups) are ordered.  
     * Defaults to <tt>true</tt>.
     * @param ordered <tt>true</tt> if stream components are ordered
     */
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    /**
     * Returns the path name of the resource bundle containing customized error
     * messages for this stream.
     * @return the resource bundle name
     */
    public String getResourceBundle() {
        return resourceBundle;
    }

    /**
     * Sets the path name of the resource bundle containing customized error
     * messages for this stream.
     * @param resourceBundle the resource bundle name
     */
    public void setResourceBundle(String resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    /**
     * Adds a custom type handler to this stream.
     * @param handler the type handler to add
     */
    public void addHandler(TypeHandlerConfig handler) {
        handlerList.add(handler);
    }

    /**
     * Returns a list of customized type handlers configured for this stream.
     * @return the list of custom type handlers
     */
    public List<TypeHandlerConfig> getHandlerList() {
        return handlerList;
    }

    /**
     * Returns the root group.
     * @return the root group
     */
    public GroupConfig getRootGroupConfig() {
        return root;
    }

    /**
     * Adds a record or group to this stream's root group.
     * @param node the node to add
     */
    public void addNode(NodeConfig node) {
        root.addChild(node);
    }

    /**
     * Sets the list of child nodes for this stream.
     * @param nodeList the list of child nodes
     */
    public void setNodeList(List<NodeConfig> nodeList) {
        root.setChildren(nodeList);
    }

    /**
     * Returns the list of child nodes (records and groups) for this stream.
     * @return the list of child nodes
     */
    public List<NodeConfig> getNodeList() {
        return root.getChildren();
    }

    /**
     * Returns the record reader factory configuration bean.
     * @return the record reader factory configuration bean.
     */
    public Bean getReaderFactory() {
        return readerFactory;
    }

    /**
     * Sets the record reader factory configuration bean.
     * @param readerFactory the record reader factory configuration bean
     */
    public void setReaderFactory(Bean readerFactory) {
        this.readerFactory = readerFactory;
    }

    /**
     * Returns the record writer factory configuration bean.
     * @return the record writer factory configuration bean.
     */
    public Bean getWriterFactory() {
        return writerFactory;
    }

    /**
     * Sets the record writer factory configuration bean.
     * @param writerFactory the record writer factory configuration bean
     */
    public void setWriterFactory(Bean writerFactory) {
        this.writerFactory = writerFactory;
    }

    /**
     * Sets the minimum occurrences of this stream.  Defaults to 0.
     * @param minOccurs the minimum occurrences
     */
    public void setMinOccurs(int minOccurs) {
        this.root.setMinOccurs(minOccurs);
    }

    /**
     * Sets the maximum occurrences of this stream.  Defaults to 1
     * @param maxOccurs ths maximum occurrences
     */
    public void setMaxOccurs(int maxOccurs) {
        this.root.setMaxOccurs(maxOccurs);
    }
}
