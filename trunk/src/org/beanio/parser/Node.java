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
package org.beanio.parser;

import java.util.List;

/**
 * A <tt>Node</tt> is the dynamic counterpart to the <tt>NodeDefinition</tt> and holds
 * the current state of a node while reading or writing the stream.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
abstract class Node {

    protected int groupCount = 0;

    /**
     * Constructs a new <tt>Node</tt>.
     */
    public Node() { }

    /**
     * Returns a list of child nodes.
     * @return the list of child nodes
     */
    public abstract List<Node> getChildren();

    /**
     * Tests if the max occurs has been reached for this node.
     * @return true if max occurs has been reached
     */
    public boolean isMaxOccursReached() {
        return getMaxOccurs() > 0 && groupCount >= getMaxOccurs();
    }

    /**
     * Finds the child node that matches a record, based on the current state of each node.
     * @param record the record to match
     * @return the matched node, or null if no match was found
     */
    public abstract Node matchNext(Record record);

    /**
     * Finds the child node that matches a record, ignoring the current state of each node.
     * @param record the record to match
     * @return the matching leaf node, or <tt>null</tt> if the record could not be matched
     */
    public abstract Node matchAny(Record record);

    /**
     * Checks for missing records.  This method is called when the end of the
     * stream has been reached.
     */
    public abstract Node close();

    /**
     * Called by its parent to reset node statistics.
     */
    public void reset() { }

    /**
     * Returns <tt>true</tt> if this node is a leaf node / record.
     * @return <tt>true</tt> if this node is a record
     */
    public boolean isRecord() {
        return false;
    }

    /**
     * Returns this nodes's definition.
     * @return the node definition
     */
    protected abstract NodeDefinition getNodeContext();

    /**
     * Returns the minimum number of times this node must appear within
     * the context of its parent.
     * @return the minimum number of occurrences
     */
    public int getMinOccurs() {
        return getNodeContext().getMinOccurs();
    }

    /**
     * Returns the maximum number of time this node must appear within
     * the context of its parent, or -1 for unbounded.
     * @return the maximum number of occurrences
     */
    public int getMaxOccurs() {
        return getNodeContext().getMaxOccurs();
    }

    /**
     * Returns the order in which this node must appear within the context
     * of its parent.  Multiple nodes may have the same order if they
     * can appear in any order. 
     * @return the node order
     */
    public int getOrder() {
        return getNodeContext().getOrder();
    }

    /**
     * Returns the name of this node.
     * @return the node name
     */
    public String getName() {
        return getNodeContext().getName();
    }

    /**
     * Returns the number of times this node has repeated within its parent.
     * @return the number of times this node has repeated
     */
    public int getGroupCount() {
        return groupCount;
    }

    /**
     * Sets the number of times this node has repeated within its parent.
     * @param groupCount the new number of times this node has repeated
     */
    public void setGroupCount(int groupCount) {
        this.groupCount = groupCount;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
            "[name=" + getName() +
            ", order=" + getOrder() +
            ", minOccurs=" + getMinOccurs() +
            ", maxOccurs=" + getMaxOccurs() +
            ", groupCount=" + getGroupCount() +
            "]";
    }
}
