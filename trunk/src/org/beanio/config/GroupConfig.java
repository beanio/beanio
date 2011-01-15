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
}
