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
import java.util.Map;

import org.beanio.BeanWriterIOException;
import org.beanio.stream.RecordWriter;
import org.beanio.stream.xml.XmlWriter;
import org.w3c.dom.*;

/**
 * A <tt>Marshaller</tt> implementation for marshaling group XML nodes.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class GroupMarshaller extends Marshaller {

    /* map key used to store the state of the 'addToHierarchy' attribute */
    private static final String WRITTEN_KEY = "written";
    /* map key used to store the state of the 'lastMatchedChild' attribute */
    private static final String LAST_MATCHED_KEY = "lastMatched";
    
    private XmlGroupDefinition groupDefinition;
    
    private Marshaller lastMatchedChild;
    private boolean addToHierarchy = true;
    
    /**
     * Constructs a new <tt>GroupMarshaller</tt>
     * @param parent the parent <tt>Marshaller</tt>
     * @param group the group node definition
     */
    public GroupMarshaller(Marshaller parent, XmlGroupDefinition group) {
        super(parent, group);
        this.groupDefinition = group;
    }

    @Override
    public void reset() {
        lastMatchedChild = null;
        
        Marshaller child = getFirstChild();
        while (child != null) {
            child.reset();
            child = child.getNextSibling();
        }
    }
    
    @Override
    public Node createHierarchy(Document document) {
        if (!addToHierarchy) {
            return null;
        }
        else {
            XmlDefinition xml = groupDefinition.getXmlDefinition();
            
            Node parentNode = null;
            if (getParent() != null) {
                parentNode = getParent().createHierarchy(document);
            }
            
            if (parentNode == null) {
                parentNode = document;
            }
            
            addToHierarchy = false;
            
            if (!xml.isNode()) {
                return parentNode;
            }
            
            Node child = parentNode.appendChild(document.createElementNS(
                xml.getNamespace(), xml.getName()));
            child.setUserData(XmlWriter.IS_GROUP_ELEMENT, Boolean.TRUE, null);
            if (!xml.isNamespaceAware()) {
                child.setUserData(XmlWriter.IS_NAMESPACE_IGNORED, Boolean.TRUE, null);
            }
            else {
                if ("".equals(xml.getPrefix())) {
                    child.setUserData(XmlWriter.IS_DEFAULT_NAMESPACE, Boolean.TRUE, null);
                }
                else {
                    child.setPrefix(xml.getPrefix());
                }
            }
            
            return child;
        }
    }
    
    @Override
    public boolean write(RecordWriter out, String recordName, Object bean) throws IOException {
        int position = 1;
        boolean matched = false;
        
        if (lastMatchedChild != null) {
            if (!lastMatchedChild.isMaxOccursReached()) {
                matched = lastMatchedChild.write(out, recordName, bean);
                if (matched) {
                    return true;
                }
            }
            
            position = lastMatchedChild.getNodeDefinition().getOrder();
        }
        
        Marshaller unsatifiedChild = null;
        
        Marshaller child = getFirstChild();
        while (child != null) {
            if (child == lastMatchedChild ||
                child.getNodeDefinition().getOrder() < position ||
                child.isMaxOccursReached()) {
                // continue
            }
            else {
                
                if (child.getNodeDefinition().getOrder() > position) {
                    // before increasing the position, we must validate that all
                    // min occurs have been met at the previous position
                    if (unsatifiedChild != null) {
                        if (lastMatchedChild != null) {
                            throw new BeanWriterIOException("Expected record type '" + 
                                unsatifiedChild.getNodeDefinition().getName() + "'");
                        }
                        return false;
                    }

                    position = child.getNodeDefinition().getOrder();
                }
                
                // validate all min occurs is satisfied
                if (child.getCount() < child.getNodeDefinition().getMinOccurs()) {
                    unsatifiedChild = child;
                }
                
                matched = child.write(out, recordName, bean);
                if (matched) {
                    if (lastMatchedChild == null) {
                        ++count;
                    }
                    else {
                        lastMatchedChild.reset();
                    }
                    
                    lastMatchedChild = child;
                    return true;
                }
            }
            
            child = child.getNextSibling();
        }
        
        // if the last matched child was not null, we continued checking for matches at the current position, 
        // now we'll check for matches at the beginning (assuming there is no unsatisfied node)
        if (lastMatchedChild != null) {
            if (unsatifiedChild != null) {
                throw new BeanWriterIOException("Expected record type '" + 
                    unsatifiedChild.getNodeDefinition().getName());
            }

            // no need to check if the max occurs was already reached
            if (isMaxOccursReached()) {
                return false;
            }
            
            // close the tag before matching earlier records
            close(out);
            
            // if there was no unsatisfied node and we haven't reached the max occurs, 
            // try to find a match from the beginning again so that the parent can 
            // skip this node
            position = 1;
            child = getFirstChild();
            while (child != null) {
                if (child.getNodeDefinition().getOrder() > position) {
                    if (unsatifiedChild != null) {
                        return false;
                    }
                    position = child.getNodeDefinition().getOrder();
                }
                
                // we've already tried to match beyond the last matched position...
                if (position > lastMatchedChild.getNodeDefinition().getOrder()) {
                    break;
                }

                if (child.getNodeDefinition().getMinOccurs() > 0) {
                    unsatifiedChild = child;
                }

                matched = child.write(out, recordName, bean);
                if (matched) {
                    reset();
                    ++count;
                    
                    // need to re-increment the child count because we just 
                    // called reset which sets it back to 0
                    ++child.count;
                    
                    lastMatchedChild = child;
                    return true;
                }
                
                child = child.getNextSibling();
            }
        }
        
        return false;
    }
    
    private void close(RecordWriter out) throws IOException {
        if (groupDefinition.getXmlDefinition().getType() != XmlDefinition.XML_TYPE_NONE) {
            out.write(null);
            addToHierarchy = true;
        }
    }
    
    @Override
    public void updateState(String namespace, Map<String, Object> state) {
        super.updateState(namespace, state);
        
        String lastMatchedChildName = "";
        if (lastMatchedChild != null) {
            lastMatchedChildName = lastMatchedChild.getNodeDefinition().getName();
        }
        state.put(getKey(namespace, LAST_MATCHED_KEY), lastMatchedChildName);
        
        state.put(getKey(namespace, WRITTEN_KEY), !addToHierarchy);
        
        // allow children to update their state
        Marshaller child = getFirstChild();
        while (child != null) {
            child.updateState(namespace, state);
            child = child.getNextSibling();
        }
    }

    @Override
    public void restoreState(String namespace, Map<String, Object> state) {
        super.restoreState(namespace, state);
        
        // determine the last matched child
        String key = getKey(namespace, LAST_MATCHED_KEY);
        String lastMatchedChildName = (String) state.get(key);
        if (lastMatchedChildName == null) {
            throw new IllegalStateException("Missing state information for key '" + key + "'");
        }
        if (lastMatchedChildName.length() == 0) {
            lastMatchedChild = null;
            lastMatchedChildName = null;
        }
        
        // determine the state of 'addToHierarchy'
        key = getKey(namespace, WRITTEN_KEY); 
        Boolean written = (Boolean) state.get(key);
        if (written == null) {
            throw new IllegalStateException("Missing state information for key '" + key + "'");
        }
        addToHierarchy = !written;
        
        // allow children to restore their state
        Marshaller child = getFirstChild();
        while (child != null) {    
            if (lastMatchedChildName != null && 
                lastMatchedChildName.equals(child.getNodeDefinition().getName())) {
                lastMatchedChild = child;
            }
            
            child.restoreState(namespace, state);
            child = child.getNextSibling();
        }
    }
}
