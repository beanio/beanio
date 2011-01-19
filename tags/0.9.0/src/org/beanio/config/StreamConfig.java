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
package org.beanio.config;

import java.util.*;

/**
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
	
	public StreamConfig() {
		root = new GroupConfig();
		root.setMinOccurs(0);
		root.setMaxOccurs(1);
		root.setOrder(1);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public boolean isOrdered() {
		return ordered;
	}
	public void setOrdered(boolean ordered) {
		this.ordered = ordered;
	}

	public String getResourceBundle() {
		return resourceBundle;
	}
	public void setResourceBundle(String resourceBundle) {
		this.resourceBundle = resourceBundle;
	}
	
	public void addHandler(TypeHandlerConfig handler) {
		handlerList.add(handler);
	}
	public List<TypeHandlerConfig> getHandlerList() {
		return handlerList;
	}
	
	public GroupConfig getRootGroupConfig() {
		return root;
	}
	public void addNode(NodeConfig node) {
		root.addChild(node);
	}
	public void setNodeList(List<NodeConfig> nodeList) {
		root.setChildren(nodeList);
	}
	public List<NodeConfig> getNodeList() {
		return root.getChildren();
	}

	public Bean getReaderFactory() {
		return readerFactory;
	}

	public void setReaderFactory(Bean readerFactory) {
		this.readerFactory = readerFactory;
	}

	public Bean getWriterFactory() {
		return writerFactory;
	}
	public void setWriterFactory(Bean writerFactory) {
		this.writerFactory = writerFactory;
	}
	
	public void setMinOccurs(int minOccurs) {
		this.root.setMinOccurs(minOccurs);
	}

	public void setMaxOccurs(int maxOccurs) {
		this.root.setMaxOccurs(maxOccurs);
	}
}
