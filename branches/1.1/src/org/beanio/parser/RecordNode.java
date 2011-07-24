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
 * Holds state information for records being read from a input stream.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class RecordNode extends Node {

    private RecordDefinition definition;
    private int recordCount;

    /**
     * Constructs a new <tt>RecordNode</tt>.
     * @param definition the record definition
     */
    public RecordNode(RecordDefinition definition) {
        this.definition = definition;
    }

    /**
     * Returns the record definition of this node.
     * @return the record definition
     */
    public RecordDefinition getRecordDefinition() {
        return definition;
    }

    @Override
    protected NodeDefinition getNodeDefinition() {
        return definition;
    }

    @Override
    public List<Node> getChildren() {
        return Arrays.<Node> asList(this);
    }

    @Override
    public Node close() {
        return getGroupCount() < getMinOccurs() ? this : null;
    }

    @Override
    public Node matchAny(Record record) {
        return definition.matches(record) ? this : null;
    }

    @Override
    public Node matchNext(Record record) {
        if (definition.matches(record)) {
            recordCount++;
            groupCount++;
            return this;
        }
        return null;
    }

    @Override
    public boolean isRecord() {
        return true;
    }

    /**
     * Returns the number of records matched by this node.
     * @return the number of records matched by this node
     */
    public int getRecordCount() {
        return recordCount;
    }
}
