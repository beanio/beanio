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
import java.util.Collection;

import org.beanio.BeanIOException;

/**
 * A CollectionParser adds iteration support for a child {@link Segment} or {@link Field},
 * and is optionally bound to a {@link Collection} property value.
 * 
 * <p>A CollectionParser may only have a single child parser.
 *  
 * @author Kevin Seim
 * @since 2.0
 */
public class CollectionParser extends DelegatingParser implements Property, Iteration {

    // minimum occurrences
    private int minOccurs = 0;
    // maximum occurrences
    private int maxOccurs = Integer.MAX_VALUE;
    // the collection type
    private Class<? extends Collection<Object>> type;
    // the property accessor, may be null if not bound
    private PropertyAccessor accessor;
    // the property value
    private Object value = null;    
    // the current iteration index
    private int index = 0;
    
    /**
     * Constructs a new <tt>CollectionParser</tt>.
     */
    public CollectionParser() { }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.DelegatingParser#clear()
     */
    public void clearValue() {
        value = null;
    }
       
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Property#defines(java.lang.Object)
     */
    public boolean defines(Object value) {
        // collections cannot be used to identify bean objects
        return false;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser.DelegatingParser#matches(org.beanio.parser.UnmarshallingContext)
     */
    public boolean matches(UnmarshallingContext context) {
        // matching repeating fields is not supported
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Marshaller#marshal(org.beanio.parser2.MarshallingContext)
     */
    public boolean marshal(MarshallingContext context) throws IOException {
        Collection<Object> collection = getCollection();
        if (collection == null && minOccurs == 0) {
            return false;
        }
        
        Parser delegate = getParser();

        context.pushIteration(this);
        try {
            index = 0;
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
        finally {
            context.popIteration();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Field#unmarshal(org.beanio.parser2.UnmarshallingContext)
     */
    public boolean unmarshal(UnmarshallingContext context) {
        Parser delegate = getParser();
        
        Collection<Object> collection = createCollection();
        
        boolean invalid = false;
        int count = 0;
        try {
            context.pushIteration(this);
            
            Object fieldValue = null;
            for (index=0; index < maxOccurs; index++) {
                
                // unmarshal the field
                boolean found = delegate.unmarshal(context);
                if (!found) {
                    delegate.clearValue();
                    break;
                }
                
                // collect the field value and add it to our buffered list
                fieldValue = delegate.getValue();
                if (fieldValue == Value.INVALID) {
                    invalid = true;
                }
                else {
                    if (collection != null) {
                        collection.add(fieldValue);
                    }
                }
                
                delegate.clearValue();
                ++count;
            }
        }
        finally {
            context.popIteration();
        }
        
        // validate minimum occurrences have been met
        if (count < getMinOccurs()) {
            context.addFieldError(getName(), null, "minOccurs", getMinOccurs(), getMaxOccurs());
            value = Value.INVALID;
        }
        else if (invalid) {
            value = Value.INVALID;
        }
        else {
            value = collection;
        }
        
        return count > 0;
    }
    
    @Override
    public boolean isLazy() {
        return minOccurs == 0;
    }
    
    /*
     * Returns false.  Iterations cannot be used to identify records.
     */
    public boolean isIdentifier() {
        return false;
    }
    
    /**
     * Returns whether this iteration is a property of a bean object.
     * @return true if this iteration is a property, false otherwise
     */
    public boolean isProperty() {
        return type != null;
    }
    
    /**
     * Returns whether this iteration contained invalid values when last unmarshalled.
     * @return true if this iteration contained invalid values
     */
    protected boolean isInvalid() {
        return value == Value.INVALID;
    }
    
    /**
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Collection<Object> getCollection() {
        if (isInvalid()) {
            return null;
        }
        return (Collection<Object>) value;
    }
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    public void setType(Class<?> collectionType) {
        this.type = (Class<? extends Collection<Object>>) collectionType;
    }
    
    /*
     * Returns the property type.
     */
    public Class<? extends Collection<Object>> getType() {
        return type;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Property#create()
     */
    public Object createValue() {
        if (value == null) {
            value = createCollection();
        }
        return getValue();
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.DelegatingParser#getValue()
     */
    public Object getValue() {
        return value == null ? Value.MISSING : value;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.DelegatingParser#setValue(java.lang.Object)
     */
    public void setValue(Object value) {
        // convert empty collections to null so that parent parsers
        // will consider this property missing during marshalling
        if (value != null && ((Collection<?>)value).isEmpty()) {
            value = null;
        }
        
        this.value = value;
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
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Property#type()
     */
    public int type() {
        return Property.COLLECTION;
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Property#setIdentifier(boolean)
     */
    public void setIdentifier(boolean identifier) {
        throw new UnsupportedOperationException();
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Property#setAccessor(org.beanio.parser2.PropertyAccessor)
     */
    public void setAccessor(PropertyAccessor accessor) {
        this.accessor = accessor;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Property#getAccessor()
     */
    public PropertyAccessor getAccessor() {
        return accessor;
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Iteration#getIterationSize()
     */
    public int getIterationSize() {
        return getSize();
    }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.parser2.Iteration#getIterationIndex()
     */
    public int getIterationIndex() {
        return index;
    }

    @Override
    public boolean hasContent() {
        Collection<Object> collection = getCollection();
        return collection != null && collection.size() > 0; 
    }

    @Override
    protected void toParamString(StringBuilder s) {
        super.toParamString(s);
        s.append(", minOccurs=").append(minOccurs);
        s.append(", maxOccurs=").append(maxOccurs);
    }
}
