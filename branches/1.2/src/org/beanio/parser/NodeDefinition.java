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
package org.beanio.parser;

import java.util.*;

/**
 * The <tt>NodeDefinition</tt> is the base class for classes used to make up
 * a stream definition's internal tree structure.
 * <p>
 * All classes and subclasses used to define a stream may be shared across multiple threads
 * and must be thread-safe.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class NodeDefinition {

	private String name;
	private int minOccurs = 1;
	private int maxOccurs = -1; // -1 for unbounded
	private int order = 1;
	
	/**
	 * Returns a list of this node's children.
	 * @return the list list of the node's children
	 */
	public abstract Collection<NodeDefinition> getChildren();
	
	/**
	 * Returns <tt>true</tt> if this node is a leaf node, or in this case
	 * a record definition.
	 * @return <tt>true</tt> if this node is a leaf node / record definition
	 */
	public abstract boolean isRecordDefinition();
	
	/**
	 * Returns a record definition (i.e. leaf node) capable of formatting the given bean.
	 * @param bean the bean to format
	 * @return the node context capable of formatting the bean, or null if one could not be found
	 */
	public abstract NodeDefinition findDefinitionFor(Object bean);
	
	/**
	 * Returns the name of the node.
	 * @return the name of the node
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets the name of this node.
	 * @param name the new name of this node
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns the minimum number of times this node must appear within
	 * the context of its parent.
	 * @return the minimum number of occurrences
	 */
	public int getMinOccurs() {
		return minOccurs;
	}
	
	/**
	 * Sets the minimum number of times this node must appear within the
	 * context of its parent.
	 * @param minOccurs the new minimum number of occurrences
	 */
	public void setMinOccurs(int minOccurs) {
		this.minOccurs = minOccurs;
	}
	
	/**
	 * Returns the maximum number of time this node must appear within
	 * the context of its parent, or -1 for unbounded.
	 * @return the maximum number of occurrences
	 */
	public int getMaxOccurs() {
		return maxOccurs;
	}
	
	/**
	 * Sets the maximum number of times this node must appear within the
	 * context of its parent.  The value <tt>-1</tt> is used to denote
	 * no boundary.
	 * @param maxOccurs the maximum number of occurrences 
	 */
	public void setMaxOccurs(int maxOccurs) {
		this.maxOccurs = maxOccurs;
	}
	
	/**
	 * Returns the order in which this node must appear within the context
	 * of its parent.  Multiple nodes may have the same order if they
	 * can appear in any order. 
	 * @return the node order
	 */
	public int getOrder() {
		return order;
	}
	
	/**
	 * Sets the order in which this node must appear within the context
	 * of its parent.  Multiple nodes may have the same order if they
	 * can appear in any order. 
	 * @param order
	 */
	public void setOrder(int order) {
		this.order = order;
	}
}
