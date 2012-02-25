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

import java.io.IOException;
import java.util.Map;

import org.beanio.internal.parser.*;
import org.beanio.internal.util.DomUtil;
import org.beanio.stream.xml.*;
import org.w3c.dom.*;

/**
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class XmlSelectorWrapper extends ParserComponent implements Selector, XmlNode {

    /* map key used to store the state of the 'addToHierarchy' attribute */
    private static final String WRITTEN_KEY = "written";
    
    /* state attributes */
    private boolean written;
    
    /* marshalling flags */
    private boolean group;
    private int depth;
    
    /* xml node attributes */
    private String localName;
    private String prefix;
    private String namespace;
    private boolean namespaceAware;
    
    /**
     * Constructs a new <tt>XmlSelectorWrapper</tt>.
     */
    public XmlSelectorWrapper() { 
        super(1);
    }
    
    /**
     * Creates a DOM made up of all <tt>XmlSelectorWrapper</tt> descendants that wrap
     * a group or record.
     * @return the created {@link Document}
     */
    public Document createBaseDocument() {
        Document document = DomUtil.newDocument();
        createBaseDocument(document, document, this);
        return document;
    }
    private void createBaseDocument(Document document, Node parent, Component node) {
        if (node instanceof XmlSelectorWrapper) {
            XmlSelectorWrapper wrapper = (XmlSelectorWrapper) node;
            if (!wrapper.isGroup()) {
                return;
            }
            Element element = document.createElementNS(wrapper.getNamespace(), wrapper.getLocalName());
            parent.appendChild(element);
            
            if (!wrapper.isNamespaceAware()) {
                element.setUserData(XmlReader.IS_NAMESPACE_IGNORED, Boolean.TRUE, null);
            }
            
            parent = element;
        }
    
        for (Component child : node.getChildren()) {
            createBaseDocument(document, parent, child);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Marshaller#marshal(org.beanio.parser2.MarshallingContext)
     */
    public boolean marshal(MarshallingContext context) throws IOException {
        XmlMarshallingContext ctx = (XmlMarshallingContext) context;
        
        Node parent = ctx.getParent();
        Node node = parent.appendChild(
            ctx.getDocument().createElementNS(getNamespace(), getLocalName()));
        node.setPrefix(getPrefix());
        if (group) {
            node.setUserData(XmlWriter.IS_GROUP_ELEMENT, Boolean.TRUE, null);
            written = true;
        }
        if (!isNamespaceAware()) {
            node.setUserData(XmlWriter.IS_NAMESPACE_IGNORED, Boolean.TRUE, null);
        }
        ctx.setParent(node);
        
        boolean b = getDelegate().marshal(context);
        
        if (group) {
            ((XmlMarshallingContext)context).closeGroup(this);
            written = false;
        }
        ctx.setParent(null);
        
        return b;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#unmarshal(org.beanio.internal.parser.UnmarshallingContext)
     */
    public boolean unmarshal(UnmarshallingContext context) {
        return getDelegate().unmarshal(context);
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#skip(org.beanio.internal.parser.UnmarshallingContext)
     */
    public void skip(UnmarshallingContext context) {
        getDelegate().skip(context);
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#matchNextBean(org.beanio.internal.parser.MarshallingContext, java.lang.Object)
     */
    public Selector matchNext(MarshallingContext context) {
        int initialCount = getCount();
        
        Selector match = getDelegate().matchNext(context);
        if (match == null) {
            if (written) {
                written = false;
                ((XmlMarshallingContext)context).closeGroup(this);
            }
            return null;
        }
        
        if (group) {
            // if the group count increased, we need to close the current group
            // element (by calling remove) and adding a new one
            if (written && getCount() > initialCount) {
                ((XmlMarshallingContext)context).closeGroup(this);
                written = false;
            }
            if (!written) {
                ((XmlMarshallingContext)context).openGroup(this);
                written = true;
            }
            return match;
        }
        else {
            return this;
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#matchNext(org.beanio.internal.parser.UnmarshallingContext)
     */
    public Selector matchNext(UnmarshallingContext context) {
        return match(context, true);
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#matchAny(org.beanio.internal.parser.UnmarshallingContext)
     */
    public Selector matchAny(UnmarshallingContext context) {
        return match(context, false);
    }
    
    /**
     * Matches a child {@link Selector}.
     * @param context the {@link UnmarshallingContext}
     * @param stateful whether to check the state of the matched child
     * @return the matched {@link Selector}, or null if no match was made
     */
    private Selector match(UnmarshallingContext context, boolean stateful) {
        // validate the next element in the document matches this record
        XmlUnmarshallingContext ctx = (XmlUnmarshallingContext) context;

        // update the position in the DOM tree (if null the node is matched)
        Element matchedDomNode = ctx.pushPosition(this, depth, group);
        if (matchedDomNode == null) {
            return null;
        }
        
        Selector match = null;
        try {
            if (stateful) {
                // get the number of times this node was read from the stream for comparing to our group count
                Integer n = (Integer) matchedDomNode.getUserData(XmlReader.GROUP_COUNT);
                /*
                    if the group count is null, it means we expected a group and got a record, therefore no match
                    if (n == null) {
                        return null;
                    }
                */
                if (n != null && n > getCount()) {
                    if (isMaxOccursReached()) {
                        return null;
                    }
                    setCount(n);
                    reset();
                }
            }
            
            // continue matching now that we've updated the DOM position...
            match = getDelegate().matchNext(context);
            
            return match;
        }
        finally {
            // if there was no match, reset the DOM position
            if (match == null) {
                ctx.popPosition();
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#matches(org.beanio.internal.parser.UnmarshallingContext)
     */
    public boolean matches(UnmarshallingContext context) {
        // a group is never used to match a record
        return false;
    }
    
    /**
     * Updates a Map with the current state of the Marshaller.  Used for
     * creating restartable Writers for Spring Batch.
     * @param namespace a String to prefix all state keys with
     * @param state the Map to update with the latest state
     * @since 1.2
     */
    public void updateState(String namespace, Map<String, Object> state) {
        state.put(getKey(namespace, WRITTEN_KEY), written);
        
        // allow children to update their state
        for (Component node : getChildren()) {
            ((Selector)node).updateState(namespace, state);
        }
    }
    
    /**
     * Restores a Map of previously stored state information.  Used for
     * restarting XML writers from Spring Batch.
     * @param namespace a String to prefix all state keys with
     * @param state the Map containing the state to restore
     * @since 1.2
     */
    public void restoreState(String namespace, Map<String, Object> state) {
        String key = getKey(namespace, WRITTEN_KEY); 
        Boolean written = (Boolean) state.get(key);
        if (written == null) {
            throw new IllegalStateException("Missing state information for key '" + key + "'");
        }
        this.written = written;
        
        // allow children to restore their state
        for (Component child : getChildren()) {
            ((Selector)child).restoreState(namespace, state);
        }
    }
    
    /**
     * Returns a Map key for accessing state information for this Node.
     * @param namespace the assigned namespace for the key
     * @param name the state information to access
     * @return the fully qualified key
     */
    protected String getKey(String namespace, String name) {
        return namespace + "." + getName() + "." + name;
    }
    
    /**
     * Returns the child selector of this component wraps.
     * @return the child {@link Selector}
     */
    private final Selector getDelegate() {
        return (Selector) getFirst();
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#getValue()
     */
    public Object getValue() {
        return getDelegate().getValue();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        getDelegate().setValue(value);
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#getSize()
     */
    public int getSize() {
        return getDelegate().getSize();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#close()
     */
    public Selector close() {
        return getDelegate().close();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#reset()
     */
    public void reset() {
        written = false;
        getDelegate().reset();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getCount()
     */
    public int getCount() {
        return getDelegate().getCount();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#setCount(int)
     */
    public void setCount(int count) {
        getDelegate().setCount(count);
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getMinOccurs()
     */
    public int getMinOccurs() {
        return getDelegate().getMinOccurs();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getMaxOccurs()
     */
    public int getMaxOccurs() {
        return getDelegate().getMaxOccurs();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getOrder()
     */
    public int getOrder() {
        return getDelegate().getOrder();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#isMaxOccursReached()
     */
    public boolean isMaxOccursReached() {
        return getDelegate().isMaxOccursReached();
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#clearValue()
     */
    public void clearValue() {
        getDelegate().clearValue();
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#isIdentifier()
     */
    public boolean isIdentifier() {
        return getDelegate().isIdentifier();
    }

    
    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#isLazy()
     */
    public boolean isLazy() {
        return getDelegate().isLazy();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.format.xml.XmlNode#getLocalName()
     */
    public String getLocalName() {
        return localName;
    }


    public void setLocalName(String localName) {
        this.localName = localName;
    }


    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.format.xml.XmlNode#getNamespace()
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * 
     * @param namespace
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.format.xml.XmlNode#getPrefix()
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * 
     * @param prefix
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.format.xml.XmlNode#isNamespaceAware()
     */
    public boolean isNamespaceAware() {
        return namespaceAware;
    }

    /**
     * 
     * @param namespaceAware
     */
    public void setNamespaceAware(boolean namespaceAware) {
        this.namespaceAware = namespaceAware;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.format.xml.XmlNode#getType()
     */
    public int getType() {
        return XmlNode.XML_TYPE_ELEMENT;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.format.xml.XmlNode#isNillable()
     */
    public boolean isNillable() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.format.xml.XmlNode#isRepeating()
     */
    public boolean isRepeating() {
        return false;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getProperty()
     */
    public Property getProperty() {
        return getDelegate().getProperty();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#hasContent()
     */
    public boolean hasContent() {
        return getDelegate().hasContent();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#isRecordGroup()
     */
    public boolean isRecordGroup() {
        return false;
    }
    
    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    protected void toParamString(StringBuilder s) {
        super.toParamString(s);
        s.append(", depth=").append(depth);
        s.append(", group=").append(group);
        s.append(", element=").append(getLocalName());
        if (isNamespaceAware()) {
            s.append(", xmlns=").append(getNamespace());
        }
        else {
            s.append(", xmlns=*");
        }
    }
}