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

import java.util.*;

/**
 * A <tt>GroupDefinition</tt> is the branch in the tree structure that makes
 * up a stream definition.  A group definition can hold record definitions
 * and other group definitions.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public final class GroupDefinition extends NodeDefinition {

    private List<NodeDefinition> children = new ArrayList<NodeDefinition>();

    /**
     * Constructs a new <tt>GroupDefinition</tt>.
     */
    public GroupDefinition() { }

    @Override
    public Collection<NodeDefinition> getChildren() {
        return children;
    }

    @Override
    public final boolean isRecordDefinition() {
        return false;
    }

    @Override
    public NodeDefinition findDefinitionFor(Object bean) {
        for (NodeDefinition node : children) {
            NodeDefinition match = node.findDefinitionFor(bean);
            if (match != null)
                return match;
        }
        return null;
    }

    /**
     * Adds a child to this group definition.
     * @param node the node definition to add
     */
    public void addChild(NodeDefinition node) {
        children.add(node);
    }

    /**
     * Sets this group's children.
     * @param children the group children to set
     */
    public void setChildren(Collection<NodeDefinition> children) {
        children.clear();
        if (children != null) {
            children.addAll(children);
        }
    }

    /**
     * Returns all record definition ancestors of this group node.
     * @return the collection of record definitions
     */
    public Collection<RecordDefinition> getRecordDefinitionAncestors() {
        return addRecordDefinitionAncestors(new ArrayList<RecordDefinition>());
    }

    private Collection<RecordDefinition> addRecordDefinitionAncestors(Collection<RecordDefinition> list) {
        for (NodeDefinition node : children) {
            if (node.isRecordDefinition()) {
                list.add((RecordDefinition) node);
            }
            else {
                ((GroupDefinition) node).addRecordDefinitionAncestors(list);
            }
        }
        return list;
    }
}
