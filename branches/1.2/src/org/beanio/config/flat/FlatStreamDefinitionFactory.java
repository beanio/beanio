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
package org.beanio.config.flat;

import java.util.*;

import org.beanio.BeanIOConfigurationException;
import org.beanio.config.*;
import org.beanio.parser.*;
import org.beanio.parser.flat.*;

/**
 * Base class for stream definition factories for flat formatted streams (i.e. delimited
 * or fixed length).
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public abstract class FlatStreamDefinitionFactory extends StreamDefinitionFactory {
    
    /* Comparator for sorting bean properties */
    private static final Comparator<PropertyDefinition> propertyComparator = new PropertyDefinitionComparator();
    
    @Override
    protected void compileFieldDefinitions(RecordConfig recordConfig, RecordDefinition recordDefinition) {
        super.compileFieldDefinitions(recordConfig, recordDefinition);

        BeanDefinition beanDefinition = recordDefinition.getBeanDefinition();
        FlatRecordDefinition definition = (FlatRecordDefinition) recordDefinition;

        // validate a position is set for all fields or none of them
        Boolean set = null;
        List<FieldDefinition> fieldList = beanDefinition.getAllFields();
        for (FieldDefinition field : fieldList) {
            if (set == null) {
                set = field.getPosition() >= 0;
            }
            else if (set ^ (field.getPosition() >= 0)) {
                throw new BeanIOConfigurationException("position must be declared for all the fields " +
                    "in a record, or none of them (in which case, all fields must be configured in the " +
                    "order they will appear in the stream)");
            }
        }
        
        // if positions are not set, assign default positions to each field
        if (set == null || !set) {
            assignDefaultFieldPositions(recordConfig, definition);
        }
        else {
            // sort bean properties for more efficient formatting...
            sortBeanProperties(beanDefinition);
        }
        
        //printStack(beanDefinition, 0);

        // validate min and max occurs will not confuse the parser
        validateOccurrences(beanDefinition);
        
        // calculate nested bean lengths, and the overall default record min and max length
        Segment s = calculateRecordLengths(beanDefinition, false);
        
        // handle the special case where the record did not have any fields
        if (s.position == Integer.MAX_VALUE) {
            s.position = 0;
        }
        
        // adjust segment for position
        s.minLength += s.position;
        if (s.maxLength < Integer.MAX_VALUE) {
            s.maxLength += s.position;
        }
        else {
            s.maxLength = -1;
        }
        
        // config can override
        if (recordConfig.getMinLength() != null) {
            if (recordConfig.getMinLength() < 0) {
                throw new BeanIOConfigurationException(
                    "minLength must be at least 0 on record '" + recordConfig.getName() + "'");
            }
            
            s.minLength = recordConfig.getMinLength();
        }
        if (recordConfig.getMaxLength() != null) {
            if (recordConfig.getMaxLength() > 0 && recordConfig.getMaxLength() < s.minLength) {
                if (recordConfig.getMinLength() == null) {
                    throw new BeanIOConfigurationException("maxLength must be at least " + s.minLength);                
                }
                else {
                    throw new BeanIOConfigurationException("maxLength must be greater than or " +
                        "equal to minLength on record '" + recordConfig.getName() + "'");
                }
            }
            s.maxLength = recordConfig.getMaxLength();
        }
        
        definition.setMinLength(s.minLength);
        definition.setMaxLength(s.maxLength);

        // perform the last validations after all fields have been constructed and ordered
        for (FieldDefinition field : fieldList) {
            // set lazy to true for any field with a position greater than the minimum length of the record
            field.setLazy(!(field.getPosition() < s.minLength));
        }
    }
       
    /**
     * Assigns a default position to all record fields.
     * @param recordConfig the record configuration
     * @param recordDefinition the record definition
     */
    protected abstract void assignDefaultFieldPositions(RecordConfig recordConfig, FlatRecordDefinition recordDefinition);
    
    /**
     * Recursively sorts all bean properties.
     * @param the bean definition to sort
     */
    private void sortBeanProperties(BeanDefinition bean) {
        List<PropertyDefinition> list = bean.getPropertyList();
        Collections.sort(list, propertyComparator);
        
        // not necessary, but safer in case the reference to the internal list is not returned
        bean.setPropertyList(list);  
        
        for (PropertyDefinition prop : list) {
            if (prop.isBean()) {
                sortBeanProperties((BeanDefinition) prop);
            }
        }
    }
    
    /**
     * Holds length information about a segment.
     */
    protected static class Segment {
        protected int position;
        protected int minLength;
        protected int maxLength;
        protected boolean isVariableLength() { return minLength != maxLength; }
    }
    
    /**
     * Calculates the default minimum and maximum record length, and sets the
     * length of nested beans in the process.
     * @param beanDefinition the definition to calculate lengths for
     * @param isCollection whether the definition is a collection
     * @return the segment lengths
     */
    protected Segment calculateRecordLengths(BeanDefinition beanDefinition, boolean isCollection) {
        /*
         * Calculates the length of the record two different ways and takes the maximum
         * of the two:
         * 1.  by summing up the parts
         * 2.  by subtracting the segment position from the last field in the segment
         */
        
        isCollection = isCollection || beanDefinition.isCollection();
        
        int start = Integer.MAX_VALUE;
        int end = 0;
        int min = 0;
        int max = 0;
        
        for (PropertyDefinition property : beanDefinition.getPropertyList()) {
            if (property.isBean()) {
                FlatBeanDefinition childBean = (FlatBeanDefinition)property;
                Segment s = calculateRecordLengths(childBean, isCollection);
                
                start = Math.min(start, s.position);
                
                min += childBean.getMinOccurs() * s.minLength;
                
                if (childBean.getMaxOccurs() < 0) {
                    max = Integer.MAX_VALUE;
                }
                else if (max < Integer.MAX_VALUE && s.maxLength < Integer.MAX_VALUE) {
                    max += childBean.getMaxOccurs() * s.maxLength;
                }
                
                if (end < Integer.MAX_VALUE) {
                    if (s.maxLength < Integer.MAX_VALUE && childBean.getMaxOccurs() < Integer.MAX_VALUE) {
                        end = Math.max(end, s.position + s.maxLength * childBean.getMaxOccurs());
                    }
                    else {
                        end = Integer.MAX_VALUE;
                    }
                }
            }
            else if (property.isField()) {
                FieldDefinition childField = (FieldDefinition) property;

                start = Math.min(childField.getPosition(), start);
                
                min += childField.getLength() * childField.getMinOccurs();
                
                if (childField.getMaxOccurs() < 0) {
                    max = Integer.MAX_VALUE;
                }
                else if (max < Integer.MAX_VALUE) {
                    max += childField.getMaxOccurs() * childField.getLength();
                }
                
                if (end < Integer.MAX_VALUE) {
                    if (childField.getMaxOccurs() < Integer.MAX_VALUE) {
                        end = Math.max(end, childField.getPosition() + 
                            childField.getLength() * childField.getMaxOccurs());
                    }
                    else {
                        end = Integer.MAX_VALUE;
                    }
                }
            }
        }
        
        Segment segment = new Segment();
        segment.position = start;
        segment.minLength = min;
        segment.maxLength = max;
        
        int length = segment.maxLength;
        if (end == Integer.MAX_VALUE || length == Integer.MAX_VALUE) {
            length = -1;
        }
        else {
            length = Math.max(length, end - start);
        }
        beanDefinition.setLength(length);
        
        segment.maxLength = length < 0 ? Integer.MAX_VALUE : length;
        return segment;
    }
    
    /**
     * Recursively checks bean definitions to validate that any variable occurrence is at the end 
     * of the record.
     * @param beanDefinition the bean definition to check recursively
     * @return <tt>true</tt> if the bean definition is of variable occurrence
     */
    protected boolean validateOccurrences(BeanDefinition beanDefinition) {
        boolean indeterminate = beanDefinition.getMinOccurs() != beanDefinition.getMaxOccurs();
        
        int index = 0;
        int size = beanDefinition.getPropertyList().size();
        for (PropertyDefinition property : beanDefinition.getPropertyList()) {
            ++index;
            
            if (property.isBean()) {
                FlatBeanDefinition child = (FlatBeanDefinition)property;
                
                boolean b = validateOccurrences(child);
                if (b) {
                    if (indeterminate) {
                        throw new BeanIOConfigurationException(
                            "A bean definition of variable occurrence cannot hold another " +
                            "bean definition of variable occurrence");  
                    }
                    else if (index < size) {
                        throw new BeanIOConfigurationException("A bean definition " +
                            "of variable occurence is only allowed at the the end of the record");    
                    }
                }
            }
            else if (property.isField()) {
                if (property.getMinOccurs() != property.getMaxOccurs()) {
                    if (indeterminate) {
                        throw new BeanIOConfigurationException(
                            "A bean definition of variable occurrence cannot hold a " +
                            "field definition of variable occurrence");  
                    }
                    else if (index < size) {
                        throw new BeanIOConfigurationException("Field '" + property.getName() +
                            "' with variable occurence is only allowed at the the end of the record");   
                    }
                    return true;
                }
            }
        }
        
        return indeterminate;
    }
        
    /**
     * Comparator for sorting property definitions.
     */
    private static class PropertyDefinitionComparator implements Comparator<PropertyDefinition> {
        /*
         * (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(PropertyDefinition o1, PropertyDefinition o2) {
            return new Integer(getPosition(o1)).compareTo(new Integer(getPosition(o2)));
        }
        
        private int getPosition(PropertyDefinition pd) {
            if (pd.isBean()) {
                int min = Integer.MAX_VALUE;
                BeanDefinition bean = (BeanDefinition) pd;
                for (PropertyDefinition prop : bean.getPropertyList()) {
                    min = Math.min(min, getPosition(prop));
                }
                return min;
            }
            else if (pd.isField()) {
                return ((FieldDefinition)pd).getPosition();
            }
            else {
                return -1;
            }
        }
    }
    
    @SuppressWarnings("unused")
    private void printStack(BeanDefinition bean, int level) {
        for (int i=0; i<level; i++)
            System.out.print("  ");
        System.out.println(bean.getName());
        ++level;
        for (PropertyDefinition prop : bean.getPropertyList()) {
            if (prop.isBean()) {
                printStack((BeanDefinition) prop, level);
            }
            else if (prop.isField()) {
                for (int i=0; i<level; i++)
                    System.out.print("  ");
                System.out.println(prop.getName() + " " + ((FieldDefinition)prop).getPosition());
            }
        }
    }
}
