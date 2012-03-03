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
package org.beanio.internal.parser;

import java.io.IOException;
import java.util.Map;

import org.beanio.*;

/**
 * A Group holds child nodes including records and other groups.
 * This class is the dynamic counterpart to the <tt>GroupDefinition</tt> and
 * holds the current state of a group node during stream processing. 
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class Group extends ParserComponent implements Selector {

    /* map key used to store the state of the 'lastMatchedChild' attribute */
    private static final String LAST_MATCHED_KEY = "lastMatched";
    
    private int minOccurs = 0;
    private int maxOccurs = Integer.MAX_VALUE;
    private int position = 1;
    private boolean result = false;
    private Property property = null;
    // the current group count
    private int count;
    // the last matched child
    private Selector lastMatched;
    
    /**
     * Constructs a new <tt>Group</tt>.
     */
    public Group() { 
        super(5);
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Marshaller#marshal(org.beanio.parser2.MarshallingContext)
     */
    public boolean marshal(MarshallingContext context) throws IOException {
        // this method is only invoked when this group is configured to
        // marshal a bean object that spans multiple records
        
        boolean marshalled = false;
        for (Component node : getChildren()) {
            marshalled = ((Parser)node).marshal(context) || marshalled;
        }
        
        return marshalled;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#skip(org.beanio.internal.parser.UnmarshallingContext)
     */
    public void skip(UnmarshallingContext context) {
        // this method is only invoked when this group is configured to
        // unmarshal a bean object that spans multiple records
        
        try {
            Selector child = (Selector) lastMatched;
            child.skip(context);
            
            // read the next record
            while (true) {
                context.nextRecord();
                
                if (context.isEOF()) {
                    Selector unsatisfied = close();
                    if (unsatisfied != null) {
                        throw context.newUnsatisfiedRecordException(unsatisfied.getName());
                    }
                    break;
                }
                
                // find the child unmarshaller for the record...
                child = (Selector) matchCurrent(context);
                if (child == null) {
                    reset();
                    break;
                }
                
                child.skip(context);
            }
        }
        catch (UnsatisfiedNodeException ex) {
            throw context.newUnsatisfiedRecordException(ex.getNode().getName());
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Unmarshaller#unmarshal(org.beanio.parser2.UnmarshallingContext)
     */
    public boolean unmarshal(UnmarshallingContext context) {
        // this method is only invoked when this group is configured to
        // unmarshal a bean object that spans multiple records
        
        try {
            Selector child = (Selector) lastMatched;
            child.unmarshal(context);
            
            // read the next record
            while (true) {
                context.nextRecord();
                
                if (context.isEOF()) {
                    Selector unsatisfied = close();
                    if (unsatisfied != null) {
                        throw context.newUnsatisfiedRecordException(unsatisfied.getName());
                    }
                    break;
                }
                
                // find the child unmarshaller for the record...
                child = (Selector) matchCurrent(context);
                if (child == null) {
                    reset();
                    break;
                }
                
                child.unmarshal(context);
            }
            
            if (property != null) {
                property.createValue();
            }
            
            return true;
        }
        catch (UnsatisfiedNodeException ex) {
            throw context.newUnsatisfiedRecordException(ex.getNode().getName());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.RecordMatcher#matchAny(org.beanio.parser2.UnmarshallingContext)
     */
    public Selector matchAny(UnmarshallingContext context) {
        for (Component n : getChildren()) {
            Selector node = (Selector) n;
            
            Selector match = node.matchAny(context);
            if (match != null) {
                return match;
            }
        }
        return null;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.RecordMatcher#matchNext(org.beanio.parser2.UnmarshallingContext)
     */
    public Selector matchNext(UnmarshallingContext context) {
        try {
            return internalMatchNext(context);
        }
        catch (UnsatisfiedNodeException ex) {
            throw context.newUnsatisfiedRecordException(ex.getNode().getName());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.RecordMatcher#matchNextBean(java.lang.Object)
     */
    public Selector matchNext(MarshallingContext context) {
        try {
            if (property == null) {
                return internalMatchNext(context);
            }
            else {
                String componentName = context.getComponentName();
                if (componentName != null && !getName().equals(componentName)) {
                    return null;
                }
                
                Object value = context.getBean();
                if (property.defines(value)) {
                    property.setValue(value);
                    return this;
                }
                
                return null;
            }
        }
        catch (UnsatisfiedNodeException ex) {
            throw new BeanWriterException("Bean identification failed.  Expected record type '" + 
                ex.getNode().getName() + "'", ex);
        }
    }
    
    /**
     * 
     * @return
     * @throws UnsatisfiedNodeException
     */
    private Selector internalMatchNext(ParsingContext context) throws UnsatisfiedNodeException {
        /*
         * A matching record is searched for in 3 stages:
         * 1.  First, we give the last matching node an opportunity to match the next 
         *     record if it hasn't reached it's max occurs.
         * 2.  Second, we search for another matching node at the same position/order
         *     or increment the position until we find a matching node or a min occurs
         *     is not met.
         * 3.  Finally, if all nodes in this group have been satisfied and this group
         *     hasn't reached its max occurs, we search nodes from the beginning again
         *     and increment the group count if a node matches.
         *     
         * If no match is found, there SHOULD be no changes to the state of this node.
         */
        
        //System.out.println("Group '" + getName() + "' -> " +
        //    (last == null ? "null" : last.getName()) + ", count=" + count);
        
        Selector match = matchCurrent(context);
        if (match == null && maxOccurs > 1) {
            match = matchAgain(context);
        }
        if (match != null) {
            return isResult() ? this : match;
        }
        return null;
    }
    
    /**
     * 
     * @return
     * @throws UnsatisfiedNodeException
     */
    private Selector matchCurrent(ParsingContext context) throws UnsatisfiedNodeException {
        Selector match = null;
        Selector unsatisfied = null;
        
        // check the last matching node - do not check records where the max occurs
        // has already been reached
        if (lastMatched != null && !(lastMatched.isMaxOccursReached())) {
            match = matchNext(context, lastMatched);
            if (match != null) {
                return match;
            }
        }
        
        // set the current position to the order of the last matched node (or default to 1)
        int position = (lastMatched == null) ? 1 : lastMatched.getOrder();
        
        // iterate over each child
        for (Component child : getChildren()) {
            Selector node = (Selector) child;
            
            // skip the last node which was already checked
            if (node == lastMatched) {
                continue;
            }
            // skip nodes where their order is less than the current position
            if (node.getOrder() < position) {
                continue;
            }
            // skip nodes where max occurs has already been met
            if (node.isMaxOccursReached()) {
                continue;
            }
            // if no node matched at the current position, increment the position and test the next node
            if (node.getOrder() > position) {
                // before increasing the position, we must validate that all
                // min occurs have been met at the previous position
                if (unsatisfied != null) {
                    if (lastMatched != null) {
                        throw new UnsatisfiedNodeException(unsatisfied);
                    }
                    return null;
                }

                position = node.getOrder();
            }

            // if the min occurs has not been met for the next node, set the unsatisfied flag so we
            // can throw an exception before incrementing the position again
            if (node.getCount() < node.getMinOccurs()) {
                // when marshalling, allow records to be skipped that aren't bound to a property
                if (context.getMode() != ParsingContext.MARSHALLING || node.getProperty() != null) {
                    unsatisfied = node;    
                }
            }
            
            // search the child node for a match
            match = matchNext(context, node);
            if (match != null) {
                // the group count is incremented only when first invoked
                if (lastMatched == null) {
                    ++count;
                }
                // reset the last group when a new record or group is found
                // at the same level (this has no effect for a record)
                else {
                    lastMatched.reset();
                }
                lastMatched = node;
                return match;
            }
        }
        
        // if last was not null, we continued checking for matches at the current position, now
        // we'll check for matches at the beginning (assuming there is no unsatisfied node)
        if (lastMatched != null) {
            if (unsatisfied != null) {
                throw new UnsatisfiedNodeException(unsatisfied);
            }
        }
        
        return null;
    }
    
    /**
     * 
     * @return
     */
    private Selector matchAgain(ParsingContext context) {

        Selector match = null;
        Selector unsatisfied = null;
        int position = 1;
        
        if (lastMatched != null) {
            
            // no need to check if the max occurs was already reached
            if (getCount() >= getMaxOccurs()) {
                return null;
            }
            
            // if there was no unsatisfied node and we haven't reached the max occurs, 
            // try to find a match from the beginning again so that the parent can 
            // skip this node
            position = 1;
            for (Component child : getChildren()) {
                Selector node = (Selector) child;
                
                if (node.getOrder() > position) {
                    if (unsatisfied != null) {
                        return null;
                    }
                    position = node.getOrder();
                }

                if (node.getMinOccurs() > 0) {
                    // when marshalling, allow records to be skipped that aren't bound to a property
                    if (context.getMode() != ParsingContext.MARSHALLING || node.getProperty() != null) {
                        unsatisfied = node;    
                    }
                }

                match = matchNext(context, node);
                if (match != null) {
                    reset();
                    ++count;
                    node.setCount(1);
                    lastMatched = node;
                    
                    return match;
                }
            }
        }

        return null;
    }
    
    /**
     * Matches the next record or bean depending on the type of parsing context.
     * @param context the parsing context
     * @param child the child Selector to invoke
     * @return the matched Selector
     */
    private Selector matchNext(ParsingContext context, Selector child) {
        switch (context.getMode()) {
            case ParsingContext.MARSHALLING:
                return child.matchNext((MarshallingContext) context);
            case ParsingContext.UNMARSHALLING:
                return child.matchNext((UnmarshallingContext) context);
            default:
                throw new IllegalStateException("Invalid mode: " + context.getMode());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#reset()
     */
    public void reset() {
        lastMatched = null;
        for (Component c : getChildren()) {
            Selector node = (Selector) c;
            node.setCount(0);
            node.reset();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.RecordMatcher#close()
     */
    public Selector close() {
        if (lastMatched == null && getMinOccurs() == 0)
            return null;

        int pos = lastMatched == null ? 1 : lastMatched.getOrder();

        // find any unsatisfied group
        for (Component c : getChildren()) {
            Selector node = (Selector) c;
            
            if (node.getOrder() < pos) {
                continue;
            }

            node.close();

            if (node.getCount() < node.getMinOccurs()) {
                return node;
            }
        }
        return null;
    }

    public boolean isUnmarshaller() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Unmarshaller#matches(org.beanio.parser2.UnmarshallingContext)
     */
    public boolean matches(UnmarshallingContext context) {
        return false;
    }
    
    /**
     * Tests if the max occurs has been reached for this node.
     * @return true if max occurs has been reached
     */
    public boolean isMaxOccursReached() {
        return lastMatched == null && count >= getMaxOccurs();
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Unmarshaller#getSize()
     */
    public int getSize() {
        return -1;
    }
    
    /**
     * Updates a Map with the current state of the Marshaller.  Used for
     * creating restartable Writers for Spring Batch.
     * @param namespace a String to prefix all state keys with
     * @param state the Map to update with the latest state
     * @since 1.2
     */
    public void updateState(String namespace, Map<String, Object> state) {
        state.put(getKey(namespace, COUNT_KEY), count);
        
        String lastMatchedChildName = "";
        if (lastMatched != null) {
            lastMatchedChildName = lastMatched.getName();
        }
        state.put(getKey(namespace, LAST_MATCHED_KEY), lastMatchedChildName);
        
        // allow children to update their state
        for (Component node : this) {
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
        String key = getKey(namespace, COUNT_KEY);
        Integer n = (Integer) state.get(key);
        if (n == null) {
            throw new IllegalStateException("Missing state information for key '" + key + "'");
        }
        count = n;
        
        // determine the last matched child
        key = getKey(namespace, LAST_MATCHED_KEY);
        String lastMatchedChildName = (String) state.get(key);
        if (lastMatchedChildName == null) {
            throw new IllegalStateException("Missing state information for key '" + key + "'");
        }
        if (lastMatchedChildName.length() == 0) {
            lastMatched = null;
            lastMatchedChildName = null;
        }
        
        // allow children to restore their state
        for (Component child : getChildren()) {
            if (lastMatchedChildName != null && 
                lastMatchedChildName.equals(child.getName())) {
                lastMatched = (Selector) child;
            }
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
    
    @Override
    public void updateReferences(Map<Object, Object> map) {
        super.updateReferences(map);
        if (property != null) {
            property = (Property) map.get(property);
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#isRecordGroup()
     */
    public boolean isRecordGroup() {
        return true;
    }

    public int getMinOccurs() {
        return minOccurs;
    }
    public void setMinOccurs(int minOccurs) {
        this.minOccurs = minOccurs;
    }
    public int getMaxOccurs() {
        return maxOccurs;
    }
    public void setMaxOccurs(int maxOccurs) {
        this.maxOccurs = maxOccurs;
    }
    public int getOrder() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    public boolean isResult() {
        return result;
    }
    public void setResult(boolean result) {
        this.result = result;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getCount()
     */
    public int getCount() {
        return count;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#setCount(int)
     */
    public void setCount(int count) {
        this.count = count;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Parser#clearValue()
     */
    public void clearValue() {
        if (property != null) {
            property.clearValue();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Parser#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        property.setValue(value);
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Parser#getValue()
     */
    public Object getValue() {
        return property.getValue();
    }
    
    
    public Property getProperty() {
        return property;
    }
    public void setProperty(Property property) {
        this.property = property;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#isLazy()
     */
    public boolean isLazy() {
        return minOccurs == 0;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#isIdentifier()
     */
    public boolean isIdentifier() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Parser#hasContent()
     */
    public boolean hasContent() {
        if (property != null) {
            return property.getValue() != Value.MISSING;
        }
        
        for (Component c : getChildren()) {
            if (((Parser)c).hasContent()) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    protected boolean isSupportedChild(Component child) {
        return child instanceof Selector;
    }
    
    @Override
    protected void toParamString(StringBuilder s) {
        super.toParamString(s);
        s.append(", order=").append(position);
        s.append(", minOccurs=").append(minOccurs);
        s.append(", maxOccurs=").append(maxOccurs);
        s.append(", property=").append(property);
    }

    @SuppressWarnings("serial")
    private static class UnsatisfiedNodeException extends Exception {
        private Selector node;
        public UnsatisfiedNodeException(Selector node) {
            this.node = node;
        }
        
        public Selector getNode() {
            return node;
        }
    }
}
