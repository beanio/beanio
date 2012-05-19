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

/**
 * Base class for record and record group configuration settings.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class NodeConfig {

    /** The node type of a record */
    public static final char RECORD = 'R';
    /** The node type of a record group */
    public static final char GROUP = 'G';

    private String name;
    private int order = 0;
    private Integer minOccurs; // defaults to 0
    private Integer maxOccurs; // defaults to -1/unbounded

    /**
     * Returns the type of this node.
     * @return {@link #RECORD} or {@link #GROUP}
     */
    public abstract char getType();

    /**
     * Returns the name of this node.
     * @return the node name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this node.
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns this node's order.  Defaults to 0.
     * @return the node order
     */
    public int getOrder() {
        return order;
    }

    /**
     * Sets the order of this node.
     * @param order the node order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Returns the minimum number of times this node must appear in
     * an input stream.  Returns <tt>null</tt> if not configured.
     * @return the minimum occurrences of this node
     */
    public Integer getMinOccurs() {
        return minOccurs;
    }

    /**
     * Sets the minimum number of times this nodes must appear in
     * an input stream.
     * @param minOccurs the minimum occurrences of this node
     */
    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    /**
     * Returns the maximum number of times this node may appear in
     * an input stream.  Returns <tt>null</tt> if not configured.
     * @return the maximum occurrences of this node
     */
    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    /**
     * Sets the maximum number of times this nodes must appear in
     * an input stream.
     * @param maxOccurs the maximum occurrences of this node
     */
    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }
}
