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

import org.beanio.parser.RecordDefinition;
import org.w3c.dom.Node;

/**
 * Record definition for XML formatted streams.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlRecordDefinition extends RecordDefinition {

    /**
     * Constructs a new <tt>XmlRecordDefinition</tt>.
     */
    public XmlRecordDefinition() { }
    
    /**
     * Formats the given bean into XML and appends the resulting DOM nodes
     * to the given parent node.
     * @param parent the parent node to append the formatted XML to
     * @param bean the bean object to format
     * @return the parent node
     */
    public Object formatBean(Node parent, Object bean) {
        return ((XmlBeanDefinition)getBeanDefinition()).formatRecord(parent, bean);
    }
}
