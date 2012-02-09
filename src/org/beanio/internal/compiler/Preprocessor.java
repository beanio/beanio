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
package org.beanio.internal.compiler;

import org.beanio.BeanIOConfigurationException;
import org.beanio.internal.config.*;

/**
 * A Preprocesser is responsible for validating a stream configuration, setting
 * default configuration values, and populating any calculated values before the
 * {@link ParserFactorySupport} compiles the configuration into parser components.  
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class Preprocessor extends ProcessorSupport {

    protected StreamConfig stream;
    protected PropertyConfig propertyRoot;
    
    /**
     * Constructs a new <tt>Preprocessor</tt>.
     * @param stream the stream configuration to preprocess
     */
    public Preprocessor(StreamConfig stream) {
        this.stream = stream;
    }
    
    /**
     * Initializes a stream configuration before its children have been processed.
     * @param stream the stream configuration to process
     */
    protected void initializeStream(StreamConfig stream) throws BeanIOConfigurationException { 
        initializeGroup(stream);
    }
    
    /**
     * Finalizes a stream configuration after its children have been processed.
     * @param stream the stream configuration to finalize
     */
    protected void finalizeStream(StreamConfig stream) throws BeanIOConfigurationException { 
        finalizeGroup(stream);
    }
    
    /**
     * Initializes a group configuration before its children have been processed.
     * @param group the group configuration to process
     */
    protected void initializeGroup(GroupConfig group) throws BeanIOConfigurationException {
        if (group.getMinOccurs() == null) {
            group.setMinOccurs(0);
        }
        if (group.getMaxOccurs() == null) {
            group.setMaxOccurs(Integer.MAX_VALUE);
        }
        // validate occurrences
        if (group.getMaxOccurs() < group.getMinOccurs()) {
            throw new BeanIOConfigurationException("Maximum occurences cannot be less than mininum occurences");
        }

        if (propertyRoot != null) {
            group.setBound(true);
            
            if (group.getCollection() != null && group.getType() == null) {
                throw new BeanIOConfigurationException("Class required if collection is set");
            }
            if (group.getType() != null && 
                group.getMaxOccurs() > 1 &&
                group.getCollection() == null) {
                throw new BeanIOConfigurationException("Collection required when maxOccurs is greater than 1 and class is set");
            }
            if (group.isRepeating() && group.getCollection() == null) {
                group.setBound(false);
            }            
        }
        
        if (propertyRoot == null && group.getType() != null) {
            propertyRoot = group;
        }
    }
    
    /**
     * Finalizes a group configuration after its children have been processed.
     * @param group the group configuration to finalize
     */
    protected void finalizeGroup(GroupConfig group) throws BeanIOConfigurationException {
        
        // order must be set for all group children, or for none of them
        // if order is specified...
        //   -validate group children are in ascending order
        // otherwise if order is not specified...
        //   -if strict, all children have current order incremented
        //   -if not, all children have order set to 1
        
        int lastOrder = 0;
        Boolean orderSet = null;
        for (ComponentConfig node : group.getChildren()) {
            SelectorConfig child = (SelectorConfig)node;
            
            String typeDescription = child.getComponentType() == ComponentConfig.RECORD ? "record" : "group";

            if (child.getOrder() != null && child.getOrder() < 0) {
                throw new BeanIOConfigurationException("Order must be 1 or greater");
            }
            
            if (orderSet == null) {
                orderSet = child.getOrder() != null;
            }
            else if (orderSet ^ (child.getOrder() != null)) {
                throw new BeanIOConfigurationException(
                    "Order must be set all children at a group level, or none at all");                
            }
            
            if (orderSet) {
                if (child.getOrder() < lastOrder) {
                    throw new BeanIOConfigurationException("'" + child.getName() + 
                        "' " + typeDescription + " configuration is out of order");   
                }
                lastOrder = child.getOrder();
            }
            else {
                if (stream.isStrict()) {
                    child.setOrder(++lastOrder);
                }
                else {
                    child.setOrder(1);
                }
            }
        }
        
        if (propertyRoot == group) {
            propertyRoot = null;
        }
    }
    
    /**
     * Initializes a record configuration before its children have been processed.
     * @param record the record configuration to process
     */
    protected void initializeRecord(RecordConfig record) throws BeanIOConfigurationException {
        // assign default min and max occurs
        if (record.getMinOccurs() == null) {
            record.setMinOccurs(0);
        }
        if (record.getMaxOccurs() == null) {
            record.setMaxOccurs(Integer.MAX_VALUE);
        }
     
        if (propertyRoot == null) {
            propertyRoot = record;
        }
        
        initializeSegment(record);
    }
    
    /**
     * Finalizes a record configuration after its children have been processed.
     * @param record the record configuration to process
     */
    protected void finalizeRecord(RecordConfig record) throws BeanIOConfigurationException {
        finalizeSegment(record);
        
        if (propertyRoot == record) {
            propertyRoot = null;
        }
    }
    
    /**
     * Initializes a segment configuration before its children have been processed.
     * @param segment the segment configuration to process
     */
    protected void initializeSegment(SegmentConfig segment) throws BeanIOConfigurationException {

        // set default occurrences and validate
        if (segment.getMinOccurs() == null) {
            segment.setMinOccurs(1);
        }
        if (segment.getMaxOccurs() == null) {
            segment.setMaxOccurs(1);
        }
        if (segment.getMaxOccurs() <= 0) {
            throw new BeanIOConfigurationException("Maximum occurrences must be greater than 0");
        }
        if (segment.getMaxOccurs() < segment.getMinOccurs()) {
            throw new BeanIOConfigurationException("Maximum occurrences cannot be less than mininum occurrences");
        }
        
        if (segment.getCollection() != null && segment.getType() == null) {
            throw new BeanIOConfigurationException("Class required if collection is set");
        }
        
        if (propertyRoot == null || propertyRoot != segment) {
            segment.setBound(true);
            
            if (segment.getType() != null && 
                segment.getMaxOccurs() > 1 &&
                segment.getCollection() == null) {
                throw new BeanIOConfigurationException("Collection required when maxOccurs is greater 1 and class is set");
            }
        }
        else {
            if (segment.getCollection() != null) {
                throw new BeanIOConfigurationException("Collection cannot be set on unbound record or segment.");
            }
        }
    }
    
    /**
     * Finalizes a segment configuration after its children have been processed.
     * @param segment the segment configuration to process
     */
    protected void finalizeSegment(SegmentConfig segment) throws BeanIOConfigurationException {
        for (PropertyConfig child : segment.getPropertyList()) {
            if (child.isIdentifier()) {
                segment.setIdentifier(true);
                break;
            }
        }
    }
    
    /**
     * Processes a field configuration.
     * @param field the field configuration to process
     */
    protected void handleField(FieldConfig field) throws BeanIOConfigurationException {
        
        // set and validate occurrences
        if (field.getMinOccurs() == null) {
            field.setMinOccurs(1);
        }
        if (field.getMaxOccurs() == null) {
            field.setMaxOccurs(Math.max(field.getMinOccurs(), 1));
        }
        if (field.getMaxOccurs() == 0) {
            throw new BeanIOConfigurationException("maxOccurs must be greater than 0");
        }
        if (field.getMaxOccurs() < field.getMinOccurs()) {
            throw new BeanIOConfigurationException("maxOccurs cannot be less than minOccurs");
        }
        //field.setLazy(field.getMinOccurs() == 0);
        
        // set and validate min and max length
        if (field.getMinLength() == null) {
            field.setMinLength(0);
        }
        if (field.getMaxLength() == null) {
            field.setMaxLength(Integer.MAX_VALUE);
        }
        if (field.getMaxLength() < field.getMinLength()) {
            throw new BeanIOConfigurationException("maxLength must be greater than or equal to minLength");
        }
        if (field.getLiteral() != null) {
            int literalLength = field.getLiteral().length();
            if (literalLength < field.getMinLength()) {
                throw new BeanIOConfigurationException("literal text length is less than minLength");
            }
            if (literalLength > field.getMaxLength()) {
                throw new BeanIOConfigurationException("literal text length is greater than maxLength");
            }
        }
        
        if (field.isRepeating() && field.isIdentifier()) {
            throw new BeanIOConfigurationException("repeating fields cannot be " +
                "used as identifiers");   
        }
        
        if (field.isIdentifier()) {
            validateRecordIdentifyingCriteria(field);
        }
    }
    
    /**
     * Processes a property/constant configuration.
     * @param field the property/constant configuration to process
     */
    protected void handleConstant(ConstantConfig constant) throws BeanIOConfigurationException {
        constant.setBound(true);
        
        if (constant.getName() == null) {
            throw new BeanIOConfigurationException("Missing property name");
        }
    }
    
    /**
     * This method validates a record identifying field has a literal or regular expression
     * configured for identifying a record.
     * @param fieldDefinition the record identifying field definition to validate
     */
    protected void validateRecordIdentifyingCriteria(FieldConfig field) throws BeanIOConfigurationException {
        // validate regex or literal is configured for record identifying fields
        if (field.getLiteral() == null && field.getRegex() == null) {
            throw new BeanIOConfigurationException("Literal or regex pattern required " +
                "for identifying fields");
        }
    }
}
