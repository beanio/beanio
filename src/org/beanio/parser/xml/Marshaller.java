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

import java.io.IOException;
import java.util.Map;

import org.beanio.parser.*;
import org.beanio.stream.RecordWriter;
import org.w3c.dom.*;
import org.w3c.dom.Node;

/**
 * Base class for XML group and record node marshallers. 
 * <p>
 * A <tt>Marshaller</tt> instance implements a linked list of child marshallers and holds
 * a reference to a parent marshaller to form a tree that defines the structure of the XML 
 * document being marshalled.
 *   
 * @author Kevin Seim
 * @since 1.1
 */
public abstract class Marshaller {

    /* map key used to store the state of the 'count' attribute */
    private static final String COUNT_KEY = "count";
    
    private NodeDefinition definition;
    
    private Marshaller parent;
    private Marshaller childHead;
    private Marshaller childTail;
    private Marshaller next;
    
    protected int count = 0;
    
    /**
     * Constructs a new <tt>Marshaller</tt>.
     * @param parent the parent Marshaller node
     * @param definition the node definition marshaled by this marshaler
     */
    public Marshaller(Marshaller parent, NodeDefinition definition) {
        this.parent = parent;
        this.definition = definition;
    }
    
    /**
     * Returns the node definition wrapped by this marshaler.
     * @return the node definition
     */
    public NodeDefinition getNodeDefinition() {
        return definition;
    }
    
    /**
     * Returns the number of times this node has been marshaled.  The count
     * is reset each time its parent group is repeated.
     * @return the number of times this node has been marshaled
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Returns whether the marshal count of this node has reached the configured
     * maximum occurrences.
     * @return <tt>true</tt> if the maximum occurrences of this node has been
     *   reached 
     */
    public boolean isMaxOccursReached() {
        int maxOccurs = definition.getMaxOccurs();
        return maxOccurs > 0 && count >= maxOccurs;
    }

    /**
     * Resets the occurrences of this node and/or its children.
     */
    public abstract void reset();
    
    /**
     * Recursively creates the XML hierarchy of group nodes.
     * @param document the document object model to append
     * @return the parent document node
     */
    public abstract Node createHierarchy(Document document);
    
    /**
     * Marshals this node to a <tt>RecordWriter</tt>.
     * @param out the <tt>RecordWriter</tt> to write to
     * @param recordName the name of the record to marshal (may be <tt>null</tt> if unknown)
     * @param bean the record bean to marshal
     * @return <tt>true</tt> if the bean was marshal by this node or one of its children, 
     *   <tt>false</tt> otherwise
     * @throws IOException if an I/O error is thrown by the <tt>RecordWriter</tt>
     */
    public abstract boolean write(RecordWriter out, String recordName, Object bean) throws IOException;
    
    /**
     * Returns this marshaler's parent marshaler.
     * @return the parent marshaler
     */
    public Marshaller getParent() {
        return parent;
    }

    /**
     * Adds a child to this marshaler.
     * @param child the child <tt>Marshaller</tt> to add
     */
    public void addChild(Marshaller child) {
        if (childHead == null) {
            childHead = child;
            childTail = child;
        }
        else {
            childTail.next = child;
            childTail = child;
            
            if (childHead.next == null) {
                childHead.next = childTail;
            }
        }
    }
    
    /**
     * Returns the first child of this marshaler, or null if this marshaler
     * does not have any children.
     * @return the first child of this marshaler
     */
    public Marshaller getFirstChild() {
        return childHead;
    }

    /**
     * Returns the next sibling to this marshaler, or <tt>null</tt> if none
     * exists.
     * @return the next sibling <tt>Marshaller</tt> to this marshaler
     */
    public Marshaller getNextSibling() {
        return next;
    }
    
    /**
     * Removes all children of this marshaler.
     */
    public void removeAllChildren() {
        this.childHead = null;
        this.childTail = null;
    }

    /**
     * Updates a Map with the current state of the Marshaller.  Used for
     * creating restartable Writers for Spring Batch.
     * @param namespace a String to prefix all state keys with
     * @param state the Map to update with the latest state
     * @since 1.2
     */
    public void updateState(String namespace, Map<String, Object> state) {
        state.put(getKey(namespace, COUNT_KEY), count);
    }

    /**
     * Restores a Map of previously stored state information.  Used for
     * restarting XML writers from Spring Batch.
     * @param namespace a String to prefix all state keys with
     * @param state the Map containing the state to restore
     * @since 1.2
     */
    public void restoreState(String namespace, Map<String, Object> state) {
        String key = getKey(namespace, COUNT_KEY);
        Integer n = (Integer) state.get(key);
        if (n == null) {
            throw new IllegalStateException("Missing state information for key '" + key + "'");
        }
        count = n;
    }
    
    /**
     * Returns a Map key for accessing state information for this Node.
     * @param namespace the assigned namespace for the key
     * @param name the state information to access
     * @return the fully qualified key
     */
    protected String getKey(String namespace, String name) {
        return namespace + "." + definition.getName() + "." + name;
    }
}
