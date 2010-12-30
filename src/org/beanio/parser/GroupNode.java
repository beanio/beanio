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

import org.beanio.UnexpectedRecordException;

/**
 * A <tt>GroupNode</tt> holds child nodes including records and other groups.
 * This class is the dynamic counterpart to the <tt>GroupDefinition</tt> and
 * holds the current state of a group node during stream processing. 
 * 
 * @author Kevin Seim
 * @since 1.0
 */
class GroupNode extends Node {

	private GroupDefinition context;
	private List<Node> children = new ArrayList<Node>();

	protected Node last = null;
	
	/**
	 * Constructs a new <tt>GroupNode</tt>.
	 * @param definition the group definition
	 */
	public GroupNode(GroupDefinition definition) {
		this.context = definition;
	}
	
	@Override
	protected NodeDefinition getNodeContext() {
		return context;
	}

	@Override
	public List<Node> getChildren() {
		return children;
	}
	
	/**
	 * Adds a child node to this group.
	 * @param node the node to add
	 */
	public void addChild(Node node) {
		children.add(node);
	}

	/**
	 * Sets this nodes's children.
	 * @param children the new node children
	 */
	public void setChildren(List<Node> children) {
		this.children.clear();
		if (children != null) {
			this.children.addAll(children);
		}
	}

	@Override
	public void reset() {
		super.reset();
		last = null;
		for (Node g : children) {
			g.groupCount = 0;
		}
	}
	
	/**
	 * Checks for any unsatisfied node before the stream is closed.
	 */
	public Node close() {
		if (last == null && getMinOccurs() == 0)
			return null;
		
		int pos = last == null ? 1 : last.getOrder();
		
		// TODO remove:
		//System.out.println("Group '" + getName() + "' -> " + (last == null ? "null" : last.getName()));
		
		// find any unsatisfied group
		for (Node node : children) {
			if (node.getOrder() < pos) {
				continue;
			}
			
			node.close();
		
			if (node.getGroupCount() < node.getMinOccurs()) {
				return node;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.beanio.impl.Node#matchAny(org.beanio.parser.Record)
	 */
	@Override
	public Node matchAny(Record record) {
		for (Node node : children) {
			Node match = node.matchAny(record);
			if (match != null)
				return match;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.beanio.impl.Node#matchNext(org.beanio.parser.Record)
	 */
	@Override
	public Node matchNext(Record record) {
		int position;
		Node unsatisfied = null;
		Node match = null;
		
		/*
		 * A matching record is searched for in 3 stages:
		 * 1.  First, we give the last matching node an opportunity to match the next 
		 *     record if it hasn't reached it's max occurs.
		 * 2.  Second, we search for another matching node at the same position/order
		 *     or increment the position until we find a matching node or a min occurs
		 *     is not met.
		 * 3.  Finally, if all nodes in this group have been satisfied and this group
		 *     hasn't reached its max occurs, we search nodes from the beginning again
		 *     and increment the group count if a node matches.
		 */
		
		System.out.println("Group '" + getName() + "' -> " + 
				(last == null ? "null" : last.getName()) + ", count=" + groupCount);
		
		// check the last matching node - do not check records where the max occurs
		// has already been reached
		if (last != null && (!last.isRecord() || !last.isMaxOccursReached())) {
			match = last.matchNext(record);
			if (match != null) {
				return match;
			}
		}
		
		// set the current position to the order of the last matched node (or default to 1)
		position = (last == null) ? 1 : last.getOrder();
		
		// iterate over each child
		for (Node node : children) {
			// skip the last node which was already checked
			if (node == last) {
				continue;
			}
			// skip nodes where their order is less than the current position
			if (node.getOrder() < position) {
				continue;
			}
			// skip records where the max occurs have already been met
			if (node.isRecord() && node.isMaxOccursReached()) {
				continue;
			}
			// if no node matched at the current position, increment the position and test the next node
			if (node.getOrder() > position) {
				// before increasing the position, we must validate that all
				// min occurs have been met at the previous position
				if (unsatisfied != null) {
					if (last != null) {
						throw new UnexpectedRecordException(record.getContext(),
							"Expected record type '" + unsatisfied.getName() + "' at line " + 
							record.getRecordLineNumber());
					}
					return null;
				}
				
				position = node.getOrder();
			}
			
			// if the min occurs has not been met for the next node, set the unsatisfied flag so we
			// can throw an exception before incrementing the position again
			if (node.getGroupCount() < node.getMinOccurs()) {
				unsatisfied = node;
			}
			
			// search the child node for a match
			match = node.matchNext(record);
			if (match != null) {
				
				/* TODO is this still needed?
				if (node.getMaxOccurs() > 0 && node.groupCount > node.getMaxOccurs()) {
					throw new IllegalStateException("Too many groups '" + node.getName() + "': " + node.groupCount);
				}
				*/
				
				// the group count is incremented only when first invoked
				if (last == null)
					++groupCount;
				last = node;
				return match;
			}
		}
		
		// if last was not null, we continued checking for matches at the current position, now
		// we'll check for matches at the beginning (assuming there is no unsatisfied node)
		if (last != null) {
			if (unsatisfied != null) {
				throw new UnexpectedRecordException(record.getContext(),
					"Expected record type '" + unsatisfied.getName() + "' at line " + record.getRecordLineNumber());
			}

			// no need to check if the max occurs was already reached
			if (isMaxOccursReached())
				return null;
			
			// if there was no unsatisfied node and we haven't reached the max occurs 
			// try to find a match from the beginning again so
			// that the parent can skip this node
			position = 1;
			for (Node node : children) {
				System.out.println("  " + position + "->" + node);
				if (node.getOrder() > position) {
					if (unsatisfied != null) {
						return null;
					}
					if (node.getGroupCount() < node.getMinOccurs()) {
						unsatisfied = node;
					}
					position = node.getOrder();
				}
				
				if (node.getMinOccurs() > 0) {
					unsatisfied = node;
				}
				
				match = node.matchNext(record);
				if (match != null) {
					//if (node.getMaxOccurs() > 0 && node.groupCount > node.getMaxOccurs())
					//	throw new IllegalStateException("Too many groups '" + node.getName() + "': " + node.groupCount);
					
					reset();
					++groupCount;
					++node.groupCount;
					last = node;
					return match;
				}
			}
		}
		
		return null;
	}
}
