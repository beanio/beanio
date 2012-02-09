/*
 * Copyright 2011-2012 Kevin Seim
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
package org.beanio.internal.parser.format.xml;

import java.util.LinkedList;

import org.beanio.internal.parser.*;
import org.w3c.dom.*;

/**
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class XmlUnmarshallingContext extends UnmarshallingContext {

    /* The DOM to parse */
    private Document document;
    /* The last parsed node in the document, which is the parent node of the next field/bean to parse */
    private Element position;
    /* This stack of elements is used to store the last XML node parsed for a field or bean collection. */
    private LinkedList<Element> elementStack = new LinkedList<Element>();
    
    @Override
    public void setRecordValue(Object value) {
        this.document = (Document) value;
        this.position = null;
    }
    
    @Override
    public void pushIteration(Iteration b) {
        super.pushIteration(b);
        elementStack.addFirst(null);
    }
    
    @Override
    public Iteration popIteration() {
        elementStack.removeFirst();
        return super.popIteration();
    }

    /**
     * Returns the last parsed DOM element for a field or bean collection.
     * @return the last parsed element
     */
    public Element getPreviousElement() {
        return elementStack.getFirst();
    }
    
    /**
     * Sets the last parsed DOM element for a field or bean collection.
     * @param e the last parsed element
     */
    public void setPreviousElement(Element e) {
        elementStack.set(0, e);
    }
    
    /**
     * Returns the XML document object model (DOM) for the current record.
     * @return the XML document object model
     */
    public Document getDocument() {
        return document;
    }
    
    /**
     * Returns the parent node currently being parsed in the DOM tree. 
     * @return the current parent DOM node
     */
    public Element getPosition() {
        return position;
    }

    /**
     * Sets the parent node currently being parsed in the DOM tree.
     * @param position the current parent DOM node
     */
    public void setPosition(Element position) {
        this.position = position;
    }
    
    /**
     * 
     * @param node
     * @return the previous position, or null if not found
     */
    public Element updatePosition(XmlNode node) {
        Element parent = position;
        
        Element element = findElement(node);
        if (element == null) {
            return null;
        }
        else {
            position = element;
            return parent;
        }
    }
    
    public Element findElement(XmlNode node) {
        Element parent = position;
        
        Element element;
        if (node.isRepeating()) {
            int index = getAdjustedFieldIndex();
            
            if (index > 0) {
                element = XmlNodeUtil.findSibling(getPreviousElement(), node);
            }
            else {
                element = XmlNodeUtil.findChild(parent, node, index);
            }
            if (element != null) {
                setPreviousElement(element);
            }
        }
        else {
            if (parent == null) {
                element = XmlNodeUtil.findChild(document, node, 0);
            }
            else {
                element = XmlNodeUtil.findChild(parent, node, 0);
            }
        }
        return element;
    }
}
