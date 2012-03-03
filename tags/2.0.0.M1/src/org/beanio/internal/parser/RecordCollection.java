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
import java.util.*;

import org.beanio.*;

/**
 * A {@link Parser} tree component for parsing a collection of bean objects, where
 * a bean object is mapped to a {@link Record}.
 * 
 * <p>A <tt>RecordCollection</tt> supports a single {@link Record} child.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class RecordCollection extends DelegatingParser implements Selector, Property {

    // the collection type
    private Class<? extends Collection<Object>> type;
    // the property accessor, may be null if not bound
    private PropertyAccessor accessor;
    // the property value
    private Object value = null;   
    
    /**
     * Constructs a new <tt>RecordCollection</tt>.
     */
    public RecordCollection() { }
    
    @Override
    public boolean unmarshal(UnmarshallingContext context) {
        // allow the delegate to unmarshal itself
        boolean result = super.unmarshal(context);
        
        Object aggregatedValue = getSelector().getValue();
        if (aggregatedValue != Value.INVALID) {
            if (value == null) {
                value = createCollection();
            }
            
            getCollection().add(getSelector().getValue());
        }
        
        return result;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#skip(org.beanio.internal.parser.UnmarshallingContext)
     */
    public void skip(UnmarshallingContext context) {
        getSelector().skip(context);
    }

    @Override
    public boolean marshal(MarshallingContext context) throws IOException {
        int minOccurs = getMinOccurs();
        
        Collection<Object> collection = getCollection();
        if (collection == null && minOccurs == 0) {
            return false;
        }
        
        Parser delegate = getParser();
        int maxOccurs = getMaxOccurs();
        int index = 0;
        
        if (collection != null) {
            for (Object value : collection) {
                if (index < maxOccurs) {
                    delegate.setValue(value);
                    delegate.marshal(context);
                    ++index;
                }
                else {
                    return true;
                }
            }
        }
        
        if (index < minOccurs) {
            delegate.setValue(null);
            while (index < minOccurs) {
                delegate.marshal(context);
                ++index;
            }
        }
        
        return true;
    }

    @Override
    public void clearValue() {
        value = null;
    }

    @Override
    public void setValue(Object value) {
        // convert empty collections to null so that parent parsers
        // will consider this property missing during marshalling
        if (value != null && ((Collection<?>)value).isEmpty()) {
            value = null;
        }
        
        this.value = value;
    }
    
    /**
     * Returns the collection value being parsed.
     * @return the {@link Collection}
     */
    @SuppressWarnings("unchecked")
    protected Collection<Object> getCollection() {
        return (Collection<Object>) value;
    }
    
    protected Collection<Object> createCollection() {
        if (type != null) {
            try {
                return type.newInstance();
            }
            catch (Exception ex) {
                throw new BeanIOException("Failed to instantiate class '" + type.getName() + "'");
            }
        }
        return null;
    }

    @Override
    public Object getValue() {
        return value;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getProperty()
     */
    public Property getProperty() {
        // for now, a collection cannot be a property root so its safe to return null here
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#matchNextRecord(org.beanio.internal.parser.UnmarshallingContext)
     */
    public Selector matchNext(UnmarshallingContext context) {
        if (getSelector().matchNext(context) != null) {
            return this;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#matchAny(org.beanio.internal.parser.UnmarshallingContext)
     */
    public Selector matchAny(UnmarshallingContext context) {
        return getSelector().matchAny(context);
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#matchNextBean(org.beanio.internal.parser.MarshallingContext, java.lang.Object)
     */
    public Selector matchNext(MarshallingContext context) {
        return getSelector().matchNext(context);
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#close()
     */
    public Selector close() {
        return getSelector().close();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#reset()
     */
    public void reset() {
        getSelector().reset();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getCount()
     */
    public int getCount() {
        return getSelector().getCount();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#setCount(int)
     */
    public void setCount(int count) {
        getSelector().setCount(count);
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getOrder()
     */
    public int getOrder() {
        return getSelector().getOrder();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#isMaxOccursReached()
     */
    public boolean isMaxOccursReached() {
        return getSelector().isMaxOccursReached();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.util.StatefulWriter#updateState(java.lang.String, java.util.Map)
     */
    public void updateState(String namespace, Map<String, Object> state) {
        getSelector().updateState(namespace, state);
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.util.StatefulWriter#restoreState(java.lang.String, java.util.Map)
     */
    public void restoreState(String namespace, Map<String, Object> state) throws IllegalStateException {
        getSelector().restoreState(namespace, state);
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getMinOccurs()
     */
    public int getMinOccurs() {
        return getSelector().getMinOccurs();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#getMaxOccurs()
     */
    public int getMaxOccurs() {
        return getSelector().getMaxOccurs();
    }
    
    @Override
    protected boolean isSupportedChild(Component child) {
        return child instanceof Selector;
    }
    
    /**
     * Returns the child selector.
     * @return the child {@link Selector}
     */
    public Selector getSelector() {
        return (Selector) getFirst();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Property#getAccessor()
     */
    public PropertyAccessor getAccessor() {
        return accessor;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Property#setAccessor(org.beanio.internal.parser.PropertyAccessor)
     */
    public void setAccessor(PropertyAccessor accessor) {
        this.accessor = accessor;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Property#type()
     */
    public int type() {
        return Property.AGGREGATION_COLLECTION;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Property#createValue()
     */
    public Object createValue() {
        if (value == null) {
            value = createCollection();
        }
        return getValue();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Property#defines(java.lang.Object)
     */
    public boolean defines(Object value) {
        throw new IllegalStateException("A RecordCollection cannot identify a bean");
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Property#setIdentifier(boolean)
     */
    public void setIdentifier(boolean identifier) {
        // a collection cannot be used to identify a bean
    }

    @Override
    public boolean isIdentifier() {
        // a collection cannot be used to identify a bean
        return false;
    }

    /**
     * Sets the collection type.
     * @param collectionType {@link Collection} class type
     */
    @SuppressWarnings("unchecked")
    public void setType(Class<?> collectionType) {
        this.type = (Class<? extends Collection<Object>>) collectionType;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Property#getType()
     */
    public Class<? extends Collection<Object>> getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.Selector#isRecordGroup()
     */
    public boolean isRecordGroup() {
        return false;
    }
    
    @Override
    public boolean hasContent() {
        Collection<Object> collection = getCollection();
        return collection != null && collection.size() > 0; 
    }
}
