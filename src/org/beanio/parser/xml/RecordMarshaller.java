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

import java.io.IOException;

import org.beanio.stream.RecordWriter;
import org.beanio.util.DomUtil;
import org.w3c.dom.*;

/**
 * A <tt>Marshaller</tt> implementation for marshaling record XML nodes.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class RecordMarshaller extends Marshaller {

    private XmlRecordDefinition recordDefinition;
    
    /**
     * Constructs a new <tt>RecordMarshaller</tt>.
     * @param parent
     * @param definition
     */
    public RecordMarshaller(Marshaller parent, XmlRecordDefinition definition) {
        super(parent, definition);
        this.recordDefinition = definition;
    }
    
    @Override
    public void reset() {
        count = 0;
    }

    @Override
    public Node createHierarchy(Document document) {
        if (getParent() == null) {
            return document;
        }
        else {
            Node parent = getParent().createHierarchy(document);
            if (parent == null) {
                parent = document;
            }
            return parent;
        }
    }

    @Override
    public boolean write(RecordWriter out, String recordName, Object bean) throws IOException {
        // first check if this record type is a match for the named record or bean type
        boolean matched = false;
        
        // match based on record name if passed
        if (recordName != null && recordName.equals(recordDefinition.getName())) {
            matched = true;
        }
        // otherwise delegate to the record definition
        else if (recordDefinition.findDefinitionFor(bean) != null) {
            matched = true;
        }
        
        // no need to go further if not matched
        if (!matched) {
            return false;
        }
        
        // increment the instance count
        ++count;
        
        // create and format a document object model for passing to the record writer
        Document document = DomUtil.newDocument(); 
        Node parentNode = createHierarchy(document);
        ((XmlRecordDefinition)recordDefinition).formatBean(parentNode, bean);
        
        // write the document and return true to indicate a match
        out.write(document);
        return true;
    }
}
