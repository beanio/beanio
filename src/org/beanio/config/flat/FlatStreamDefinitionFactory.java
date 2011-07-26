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
        if (!set) {
            assignDefaultFieldPositions(recordConfig, definition);
        }
        else {
            // sort bean properties for more efficient formatting...
            sortBeanProperties(beanDefinition);
        }
        
        //printStack(beanDefinition, 0);

        // validate there are no field gaps for descendants of collections
        validateCollections(beanDefinition, false);
        // validate min and max occurs will not confuse the parser
        validateOccurrences(beanDefinition);

        // set bean lengths and calculate the default record min & max length
        calculateRecordLength(beanDefinition);
        
        // calculate default record minimum and maximum length
        int minLength = 0;
        int maxLength = 0;
        for (FieldDefinition field : fieldList) {
            minLength = Math.max(minLength, getRecordMinLength(field));
            maxLength = Math.max(maxLength, getRecordMaxLength(field));
        }
        if (maxLength == Integer.MAX_VALUE) {
            maxLength = -1;
        }
        
        // validate and set the minimum length of the record
        if (recordConfig.getMinLength() == null) {
            // do nothing, minLength already set
        }
        else {
            if (recordConfig.getMinLength() < 0) {
                throw new BeanIOConfigurationException(
                    "minLength must be at least 0 on record '" + recordConfig.getName() + "'");
            }
            minLength = recordConfig.getMinLength();
        }
        definition.setMinLength(minLength);

        // validate and set the maximum length of the record
        if (recordConfig.getMaxLength() == null) {
            maxLength = maxLength < 0 ? maxLength : Math.max(minLength, maxLength);
        }
        // handle unbounded
        else if (recordConfig.getMaxLength() < 0) {
            maxLength = -1;
        }
        else if (recordConfig.getMaxLength() > 0 && recordConfig.getMaxLength() < minLength) {
            if (recordConfig.getMinLength() == null) {
                throw new BeanIOConfigurationException("maxLength must be at least " + minLength);                
            }
            else {
                throw new BeanIOConfigurationException("maxLength must be greater than or " +
                    "equal to minLength on record '" + recordConfig.getName() + "'");
            }
        }
        else {
            maxLength = recordConfig.getMaxLength();
        }
        definition.setMaxLength(maxLength);
        
        // perform the last validations after all fields have been constructed and ordered
        for (FieldDefinition field : fieldList) {
            // set lazy to true for any field with a position greater than the minimum length of the record
            field.setLazy(!(field.getPosition() < minLength));
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
     * Validates there are no field gaps in any descendant of a collection.
     * @param beanDefinition the bean definition to validate
     * @param isCollection <tt>true</tt> if any ancestor of <tt>beanDefinition</tt> is a collection
     */
    protected void validateCollections(BeanDefinition beanDefinition, boolean isCollection) {
        isCollection = isCollection || beanDefinition.isCollection();
        
        int min = Integer.MAX_VALUE;
        int max = 0;
        int length = 0;
        PropertyDefinition previous = null;
        
        for (PropertyDefinition property : beanDefinition.getPropertyList()) {
            if (property.isBean()) {
                FlatBeanDefinition child = (FlatBeanDefinition)property;
                validateCollections(child, isCollection);
            }
            else if (property.isField()) {
                if (previous != null) {
                    if (previous.isCollection()) {
                        // assume min and max occurs are the same (which is validated elsewhere)
                        length += previous.getMaxOccurs() * previous.getLength();
                    }
                    else {
                        length += previous.getLength();
                    }
                }
                
                FieldDefinition field = (FieldDefinition) property;
                min = Math.min(field.getPosition(), min);
                max = Math.max(field.getPosition(), max);
                
                previous = property;
            }
        }
        
        if (isCollection && (max - min) != length) {
            throw new BeanIOConfigurationException("Invalid '" + beanDefinition.getName()  +
                "' bean configuration: field gaps not allowed for children of collections");
        }
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
     * Returns the minimum length of the record based solely on the given field.
     * Collection type bean definitions must have a valid length setting prior
     * to calling this method.
     * @param field the field to use to determine the minimum record length
     * @return the minimum record length
     */
    private int getRecordMinLength(FieldDefinition field) {
        if (field.getMinOccurs() == 0) {
            return 0;
        }
        
        int n = field.getPosition() + field.getLength() * (field.getMinOccurs() - 1);
        PropertyDefinition property = field;
        while ((property = property.getParent()) != null) {
            if (property.getMinOccurs() == 0) {
                return 0;
            }
            
            n += property.getLength() * (property.getMinOccurs() - 1);
        }
        
        return n + field.getLength();
    }
    
    /**
     * Returns the maximum length of the record based solely on the given field.
     * Collection type bean definitions must have a valid length setting prior
     * to calling this method.
     * @param field the field to use to determine the maximum record length
     * @return the maximum record length
     */
    private int getRecordMaxLength(FieldDefinition field) {
        if (field.getMaxOccurs() < 0) {
            return Integer.MAX_VALUE;
        }
        
        int n = field.getPosition() + field.getLength() * (field.getMaxOccurs() - 1);
        PropertyDefinition property = field;
        while ((property = property.getParent()) != null) {
            if (property.getMaxOccurs() < 0) {
                return Integer.MAX_VALUE;
            }
            
            n += property.getLength() * (property.getMaxOccurs() - 1);
        }
        
        return n + field.getLength();
    }
    
    /**
     * Calculate and set bean lengths.  And determine the default min and max record length.
     * @param beanDefinition the record level bean definition
     * @return the record block containing the min and max length
     */
    private Block calculateRecordLength(BeanDefinition beanDefinition) {
        Block block = new Block();
        for (PropertyDefinition property : beanDefinition.getPropertyList()) {
            if (property.isBean()) {
                BeanDefinition bean = (BeanDefinition) property;
                Block beanSize = calculateRecordLength(bean);
                bean.setLength(beanSize.getLength());
                block.update(bean, beanSize);
            }
            else if (property.isField()) {
                block.update((FieldDefinition) property);
            }
        }
        return block;
    }
    
    /* this implementation assumes the same field is not reused */
    private static class Block {
        private int max = 0;
        private FieldDefinition firstField;
        private FieldDefinition lastField;
        
        public int getLength() {
            if (max < 0) {
                return -1;
            }
            else if (lastField == null) {
                return max;
            }
            else if (lastField.getMaxOccurs() < 0) {
                return -1;
            }
            else {
                return max + lastField.getPosition() + 
                    lastField.getLength() * lastField.getMaxOccurs() - firstField.getPosition();
            }
        }
        
        public void update(FieldDefinition property) {
            if (firstField == null) {
                firstField = property;
            }
            else if (property.getPosition() < firstField.getPosition()) {
                firstField = property;
            }
            
            if (lastField == null) {
                lastField = property;
            }
            else if (property.getPosition() > lastField.getPosition()) {
                lastField = property;
            }
        }
        
        public void update(BeanDefinition bean, Block size) {
            if (bean.getMaxOccurs() < 0)
                this.max = -1;
            else
                this.max += size.getLength() * bean.getMaxOccurs();
        }
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
    
    /*
    private void printStack(BeanDefinition bean, int level) {
        for (int i=0; i<level; i++)
            System.out.print("  ");
        System.out.println(bean.getName());
        ++level;
        for (PropertyDefinition prop : bean.getPropertyList()) {
            if (prop.isBean()) {
                printStack((BeanDefinition) prop, level);
            }
            else {
                for (int i=0; i<level; i++)
                    System.out.print("  ");
                System.out.println(prop.getName());
            }
        }
    }
    */
}
