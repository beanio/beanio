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

import org.beanio.*;
import org.beanio.parser.*;
import org.beanio.stream.xml.XmlReader;
import org.w3c.dom.Element;

/**
 * Group node implementation for XML formatted streams.
 * <p>
 * Unlike a fixed length or delimited stream, XML streams can be configured to
 * match group names to XML elements.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlGroupNode extends GroupNode {

    private XmlGroupDefinition definition;
    
    /**
     * Constructs a new <tt>XmlGroupNode</tt>.
     * @param definition the XML group definition
     */
    public XmlGroupNode(XmlGroupDefinition definition) {
        super(definition);
        this.definition = definition;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.impl.Node#matchAny(org.beanio.parser.Record)
     */
    @Override
    public Node matchAny(Record record) {
        
        if (!definition.getXmlDefinition().isNode()) {
            return super.matchAny(record);
        }
        
        // validate the next element in the document matches this record
        XmlRecord xmlRecord = (XmlRecord) record;
        
        org.w3c.dom.Node previousParent = xmlRecord.getPosition();
        org.w3c.dom.Node parent = previousParent;
        if (parent == null) {
            parent = xmlRecord.getDocument();
        }
        
        // find this node in the DOM tree, if not found, return null to indicate no match
        Element matchedDomNode = XmlNodeUtil.findChild(parent, definition.getXmlDefinition(), 0);
        if (matchedDomNode == null) {
            return null;
        }

        // update the position in the DOM tree
        xmlRecord.setPosition(matchedDomNode);
        
        // search children
        for (Node node : getChildren()) {
            Node match = node.matchAny(record);
            if (match != null)
                return match;
        }
        
        // reset the position in the DOM tree
        xmlRecord.setPosition(previousParent);
        
        return null;
    }

    @Override
    public Node matchNext(Record record) {
        
        // default to superclass if xmlType='none'
        if (!definition.getXmlDefinition().isNode()) {
            return super.matchNext(record);
        }
        
        // validate the next element in the document matches this record
        XmlRecord xmlRecord = (XmlRecord) record;
        
        org.w3c.dom.Node previousParent = xmlRecord.getPosition();
        org.w3c.dom.Node parent = previousParent;
        if (parent == null) {
            parent = xmlRecord.getDocument();
        }
        
        // find this node in the DOM tree, if not found, return null to indicate no match
        Element matchedDomNode = XmlNodeUtil.findChild(parent, definition.getXmlDefinition(), 0);
        if (matchedDomNode == null) {
            return null;
        }
        
        // update the position in the DOM tree
        xmlRecord.setPosition(matchedDomNode);
        
        // get the number of times this node was read from the stream for comparing to our group count
        Integer count = (Integer) matchedDomNode.getUserData(XmlReader.GROUP_COUNT);
        // if count is null, it means we expected a group and got a record, therefore no match
        if (count == null) {
            return null;
        }
        if (count > groupCount) {
            if (isMaxOccursReached()) {
                return null;
            }
            
            ++groupCount;
            reset();
        }

        try {
            // continue matching, if not matched, reset the position in the DOM record
            Node node = matchNextXmlGroup(record);
            if (node == null) {
                xmlRecord.setPosition(previousParent);
            }
            return node;
        }
        catch (BeanIOException ex) {
            xmlRecord.setPosition(previousParent);
            throw ex;
        }
    }
    
    private Node matchNextXmlGroup(Record record) {
        int position;
        Node unsatisfied = null;
        Node match = null;

        /*
         * A matching record is searched for in two stages:
         * 1.  First, we give the last matching node an opportunity to match the next 
         *     record if it hasn't reached it's max occurs.
         * 2.  Second, we search for another matching node at the same position/order
         *     or increment the position until we find a matching node or a min occurs
         *     is not met.
         *     
         * If no match is found, there SHOULD be no changes to the state of this node.
         */

        // check the last matching node - do not check records where the max occurs
        // has already been reached
        if (last != null && !(last.isRecord() && last.isMaxOccursReached())) {
            match = last.matchNext(record);
            if (match != null) {
                return match;
            }
        }

        // set the current position to the order of the last matched node (or default to 1)
        position = (last == null) ? 1 : last.getOrder();

        // iterate over each child
        for (Node node : getChildren()) {
            // skip the last node which was already checked
            if (node == last) {
                continue;
            }
            // skip nodes where their order is less than the current position
            if (node.getOrder() < position) {
                continue;
            }
            // skip nodes where the max occurs has already been met
            if (node.isMaxOccursReached()) {
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
                // the group count is incremented only when first invoked
                if (last == null) {
                    //++groupCount;
                }
                // reset the last group when a new record or group is found
                // at the same level (this has no effect for a record)
                else {
                    last.reset();
                }
                last = node;
                return match;
            }
        }

        // if last was not null, we continued checking for matches at the current position, now
        // we'll check for matches at the beginning (assuming there is no unsatisfied node)
        
        /* this logic does not apply to XML and should be removed eventually...
        if (last != null) {
            if (unsatisfied != null) {
                throw new UnexpectedRecordException(record.getContext(),
                    "Expected record type '" + unsatisfied.getName() + "' at line "
                        + record.getRecordLineNumber());
            }

            // no need to check if the max occurs was already reached
            if (isMaxOccursReached()) {
                return null;
            }

            // if there was no unsatisfied node and we haven't reached the max occurs, 
            // try to find a match from the beginning again so that the parent can 
            // skip this node
            position = 1;
            for (Node node : getChildren()) {
                if (node.getOrder() > position) {
                    if (unsatisfied != null) {
                        return null;
                    }
                    position = node.getOrder();
                }

                if (node.getMinOccurs() > 0) {
                    unsatisfied = node;
                }

                match = node.matchNext(record);
                if (match != null) {
                    reset();
                    //++groupCount;
                    //++node.groupCount;
                    last = node;
                    return match;
                }
            }
        }
        */

        return null;
    }
}
