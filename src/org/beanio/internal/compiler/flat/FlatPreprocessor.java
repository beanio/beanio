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
package org.beanio.internal.compiler.flat;

import org.beanio.BeanIOConfigurationException;
import org.beanio.internal.compiler.Preprocessor;
import org.beanio.internal.config.*;

/**
 * Base class for {@link Preprocessor} implementations for flat stream formats 
 * (i.e. CSV, delimited, and fixed length).
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class FlatPreprocessor extends Preprocessor {

    // the current default field position
    private int defaultPosition = 0;
    // position must be set for all fields or for no fields, this attribute
    // is set when the first field is processed and all other fields must adhere to it
    private Boolean positionRequired;
    
    /**
     * Constructs a new <tt>FlatPreprocessor</tt>.
     * @param stream
     */
    public FlatPreprocessor(StreamConfig stream) {
        super(stream);
    }
    
    @Override
    protected void initializeRecord(RecordConfig record) {
        super.initializeRecord(record);
        
        defaultPosition = 0;
        positionRequired = null;
    }
    
    @Override
    protected void finalizeRecord(RecordConfig record) {
        super.finalizeRecord(record);
        
        boolean minSet = record.getMinLength() != null;
        if (stream.isStrict()) {
            if (record.getMinLength() == null) {
                record.setMinLength(record.getMinSize());
            }
            if (record.getMaxLength() == null) {
                record.setMaxLength(record.getMaxSize());
            }
        }
        else {
            if (record.getMinLength() == null) {
                record.setMinLength(0);
            }
            if (record.getMaxLength() == null) {
                record.setMaxLength(Integer.MAX_VALUE);
            }
        }
        // validate maximum record length is not less than the minimum record length
        if (record.getMaxLength() < record.getMinLength()) {
            if (minSet) {
                throw new BeanIOConfigurationException(
                    "Maximum record length cannot be less than minimum record length");                    
            }
            else {
                throw new BeanIOConfigurationException(
                    "Maximum record length must be at least " + record.getMinLength());
            }
        }
    }
    
    @Override
    protected void finalizeSegment(SegmentConfig segment) {
        super.finalizeSegment(segment);
        
        PropertyConfig first = null;
        PropertyConfig last = null;
        int position = 0;
        int minSize = 0;
        int maxSize = -1;
        
        // by default, a segment is not constant
        segment.setConstant(false);
        
        // calculate the maximum size and position of the segment
        for (PropertyConfig config : segment.getPropertyList()) {
            if (config.getComponentType() == PropertyConfig.CONSTANT) {
                continue;
            }
            if (config.getComponentType() == ComponentConfig.SEGMENT &&
                ((SegmentConfig)config).isConstant()) {
                continue;
            }
            if (segment.getComponentType() != ComponentConfig.RECORD && 
                segment.isRepeating() && 
                config.getMinOccurs() == 0) {
                throw new BeanIOConfigurationException("A repeating segment may not contain " +
                    "components where minOccurs=0");
            }
            if (config.getMaxSize() == Integer.MAX_VALUE) {
                maxSize = Integer.MAX_VALUE;
            }
            int n = config.getPosition();
            if (first == null || n < first.getPosition()) {
                first = config;
            }
            if (last == null || n > last.getPosition()) {
                last = config;
            }
        }
        if (last == null) {
            if (segment.getComponentType() == PropertyConfig.RECORD) {
                maxSize = Integer.MAX_VALUE;
            }
            else {
                segment.setConstant(true);
                maxSize = 0;
            }
        }
        else if (maxSize < 0) {
            position = first.getPosition();
            if (position == Integer.MAX_VALUE) {
                throw new BeanIOConfigurationException("A variable occurence segment/field " +
                    "is only allowed at the the end of the record");   
            }
            maxSize = last.getPosition() - first.getPosition() + last.getMaxSize() * last.getMaxOccurs();
        }

        // calculate the minimum size of the segment
        if (last != null) {
            first = null;
            last = null;
            
            for (PropertyConfig config : segment.getPropertyList()) {
                if (config.getComponentType() == PropertyConfig.CONSTANT) {
                    continue;
                }
                
                int n = config.getPosition();
                if (first == null || n < first.getPosition()) {
                    first = config;
                }
                if (config.getMinOccurs() > 0) {
                    if (last == null || n > last.getPosition()) {
                        last = config;
                    }
                }
            }
            
            if (last != null) {
                minSize = last.getPosition() - first.getPosition() + last.getMaxSize() * last.getMinOccurs();
            }
        }
        
        segment.setPosition(position);
        segment.setMaxSize(maxSize);
        segment.setMinSize(minSize);
        
        // calculate the next position
        if (segment.getMaxOccurs() == Integer.MAX_VALUE ||
            segment.getMaxSize() == Integer.MAX_VALUE) {
            defaultPosition = Integer.MAX_VALUE;
        }
        else if (segment.isRepeating() && !segment.getMinOccurs().equals(segment.getMaxOccurs())) {
            defaultPosition = Integer.MAX_VALUE;
        }
        else {
            defaultPosition = segment.getPosition() + segment.getMaxSize() * segment.getMaxOccurs();
        }
        
        // determine the default existence of the segment
        boolean defaultExistence = true;
        for (PropertyConfig child : segment.getPropertyList()) {
            if (child.getComponentType() == PropertyConfig.CONSTANT) {
                continue;
            }
            if (child.getComponentType() == PropertyConfig.SEGMENT) {
                if (((SegmentConfig)child).getDefaultExistence()) {
                    continue;
                }
            }
            defaultExistence = false;
        }
        segment.setDefaultExistence(defaultExistence);
        
        if (segment.getDefaultExistence() && !segment.getMinOccurs().equals(segment.getMaxOccurs())) {
            throw new BeanIOConfigurationException("Repeating segments without any child " +
                "field component must have minOccurs=maxOccurs");
        }
    }
    
    @Override
    protected void handleField(FieldConfig field) {
        super.handleField(field);
        
        // validate and configure padding
        if (isFixedLength()) {
            // if a literal is set and length is not
            if (field.getLiteral() != null) {
                if (field.getLength() == null) {
                    field.setLength(field.getLiteral().length());
                }
                else if (field.getLiteral().length() > field.getLength()) {
                    throw new BeanIOConfigurationException("literal size exceeds the field length");
                }
            }
            else if (field.getLength() == null) {
                throw new BeanIOConfigurationException("length required for fixed length fields");
            }
        }
        // default the padding character to a single space
        if (field.getLength() != null) {
            if (field.getPadding() == null) {
                field.setPadding(' ');
            }
        }
        
        // calculate the size of the field
        int size = getSize(field);
        field.setMaxSize(size);
        field.setMinSize(size);
        
        // calculate the position of this field (size must be calculated first)
        if (positionRequired == null) {
            positionRequired = field.getPosition() != null;
        }
        else if (positionRequired ^ (field.getPosition() != null)) {
            throw new BeanIOConfigurationException("position must be declared for all the fields " +
                "in a record, or for none of them (in which case, all fields must be configured in the " +
                "order they will appear in the stream)");
        }
        if (field.getPosition() == null) {
            calculateDefaultPosition(field);
        }
    }
    
    /**
     * Calculates and sets the default field position.
     * @param config the field configuration to calculate the position for
     */
    private void calculateDefaultPosition(FieldConfig config) {
        if (defaultPosition == Integer.MAX_VALUE) {
            throw new BeanIOConfigurationException("Cannot determine field position, field is preceded by " +
                "a component with indeterminate or unbounded occurences");
        }
        config.setPosition(defaultPosition);
        
        // set the next default position to MAX_VALUE if the occurences of this field is unbounded
        if (config.getMaxOccurs() == Integer.MAX_VALUE) {
            defaultPosition = Integer.MAX_VALUE;
        }
        // or if the number of occurrence is indeterminate
        else if (config.isRepeating() && config.getMinOccurs() != config.getMaxOccurs()) {
            defaultPosition = Integer.MAX_VALUE;
        }
        else {
            defaultPosition = config.getPosition() + config.getMaxSize() * config.getMaxOccurs();
        }
    }
    
    /**
     * Returns the size of a field.
     * @param field the field to size
     * @return the field size
     */
    protected int getSize(FieldConfig field) {
        return isFixedLength() ? field.getLength() : 1;
    }
    
    /**
     * Returns whether the stream format is fixed length.
     * @return true if fixed length, false otherwise
     */
    protected boolean isFixedLength() {
        return false;
    }
}
