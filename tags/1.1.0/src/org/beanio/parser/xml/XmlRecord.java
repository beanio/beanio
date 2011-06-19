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

import java.util.LinkedList;

import org.beanio.parser.Record;
import org.w3c.dom.*;

/**
 * Record used for parsing XML input streams.  A XML record is represented
 * by a document object model (DOM).
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlRecord extends Record {

    /* The DOM to parse */
    private Document document;
    /* The last parsed node in the document, which is the parent node of the next field/bean to parse */
    private Node position;
    /* This stack of elements is used to store the last XML node parsed for a field or bean collection. */
    private LinkedList<Element> elementStack = new LinkedList<Element>();
    
    /**
     * Constructs a new <tt>XmlRecord</tt>. 
     */
    public XmlRecord() { }
    
    @Override
    public void clear() {
        super.clear();
        this.document = null;
        this.position = null;
    }

    @Override
    public void setValue(Object value) {
        this.document = (Document) value;
    }
    
    @Override
    public void pushField() {
        super.pushField();
        elementStack.addFirst(null);
    }
    
    @Override
    public void popField() {
        super.popField();
        elementStack.removeFirst();
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
    public Node getPosition() {
        return position;
    }

    /**
     * Sets the parent node currently being parsed in the DOM tree.
     * @param position the current parent DOM node
     */
    public void setPosition(Node position) {
        this.position = position;
    }
}
