/*
 * Copyright 2012 Kevin Seim
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

/**
 * Base class for parser components capable of aggregating descendant properties.
 * 
 * @author Kevin Seim
 * @since 2.0.1
 */
public abstract class Aggregation extends DelegatingParser implements Property, Iteration {

    // minimum occurrences
    protected int minOccurs = 0;
    // maximum occurrences
    protected int maxOccurs = Integer.MAX_VALUE;
    // the property accessor, may be null if not bound
    private PropertyAccessor accessor;
    
    /**
     * Constructs a new <tt>Aggregation</tt>.
     */
    public Aggregation() { }
    
    /**
     * Returns whether this aggregation is a property of
     * its parent bean object.
     * @return true if this a property, false otherwise
     */
    public abstract boolean isProperty();
    
    @Override
    public boolean isLazy() {
        return minOccurs == 0;
    }
    
    /*
     * Returns false.  Iterations cannot be used to identify records.
     */
    @Override
    public boolean isIdentifier() {
        return false;
    }
    
    /**
     * @throws UnsupportedOperationException
     */
    public void setIdentifier(boolean identifier) { 
        if (identifier) {
            throw new UnsupportedOperationException();
        }
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
}
