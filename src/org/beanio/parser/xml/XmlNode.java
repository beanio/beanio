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

import org.beanio.parser.NodeDefinition;

/**
 * This interface is implemented by <tt>NodeDefinition</tt> subclasses for XML
 * formatted streams to expose common XML node settings that can be set at
 * the record, group or field level. 
 * 
 * @author Kevin Seim
 * @since 1.1
 * @see NodeDefinition
 */
public interface XmlNode {

    /**
     * Returns the XML attributes that define this node.
     * @return the XML attribute definition
     */
    public XmlDefinition getXmlDefinition();
    
}
