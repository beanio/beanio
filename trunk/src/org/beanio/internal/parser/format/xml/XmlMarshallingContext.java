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
package org.beanio.internal.parser.format.xml;

import java.io.IOException;
import java.util.*;

import org.beanio.internal.parser.MarshallingContext;
import org.beanio.internal.util.DomUtil;
import org.beanio.stream.xml.XmlWriter;
import org.w3c.dom.*;

public class XmlMarshallingContext extends MarshallingContext {

    protected Document document;
    protected Node parent;
    
    private List<XmlNode> groupStack = new ArrayList<XmlNode>();
    private int ungroup = 0;
    
    @Override
    public void clear() {
        setDocument(null);
    }
    
    @Override
    public Object getRecordObject() {
        return document;
    }
    
    public void writeRecord() throws IOException {
        super.clear();
        
        for (int i=0; i<ungroup; i++) {
            getRecordWriter().write(null);
        }
        ungroup = 0;
        super.writeRecord();
    }
    
    public void removeGroup(XmlNode element) {
        ++ungroup;
    }
    
    public void addGroup(XmlNode element) {
        groupStack.add(element);
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
        this.parent = document;
    }

    public Node getParent() {
        if (parent == null) {
            this.document = DomUtil.newDocument();
            this.parent = document;
            
            if (!groupStack.isEmpty()) {
                for (int i=groupStack.size()-1; i>=0; i--) {
                    XmlNode xml = groupStack.get(i);
                    
                    Node node = parent.appendChild(document.createElementNS(
                        xml.getNamespace(), xml.getLocalName()));
                    node.setPrefix(xml.getPrefix());
                    node.setUserData(XmlWriter.IS_GROUP_ELEMENT, Boolean.TRUE, null);
                    if (!xml.isNamespaceAware()) {
                        node.setUserData(XmlWriter.IS_NAMESPACE_IGNORED, Boolean.TRUE, null);
                    }
                    
                    this.parent = node;
                }
                groupStack.clear();
            }
        }
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
}
