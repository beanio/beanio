/*
 * Copyright 2010-2011 Kevin Seim
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
package org.beanio.config.fixedlength;

import java.util.List;

import org.beanio.BeanIOConfigurationException;
import org.beanio.config.*;
import org.beanio.config.flat.FlatStreamDefinitionFactory;
import org.beanio.parser.*;
import org.beanio.parser.fixedlength.*;
import org.beanio.parser.flat.FlatRecordDefinition;
import org.beanio.stream.*;
import org.beanio.stream.fixedlength.*;

/**
 * A <tt>FixedLengthStreamDefinitionFactory</tt> is used to create fixed length
 * stream definitions from a mapping configuration.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthStreamDefinitionFactory extends FlatStreamDefinitionFactory {
    
    @Override
    protected void updateFieldDefinition(FieldConfig fieldConfig, FieldDefinition fieldDefinition) {
        int length = fieldConfig.getLength();
        // -1 means not set, but 0 was explicitly set and invalid
        if (length == 0) { 
            throw new BeanIOConfigurationException("length must be greater than 0");
        }
        // if not set, try to determine the length of the field
        else if (length < 0) {
            // if a literal value is set, use the length of the literal
            String literal = fieldConfig.getLiteral();
            if (literal != null) {
                length = literal.length(); 
            }
            else {
                throw new BeanIOConfigurationException("length is required");
            }
        }
        
        Character padding = fieldConfig.getPadding();
        
        fieldDefinition.setLength(length);
        fieldDefinition.setPadded(true);
        fieldDefinition.setPaddedLength(length);
        fieldDefinition.setPosition(fieldConfig.getPosition());
        fieldDefinition.setPadding(padding != null ? padding : ' ');
        fieldDefinition.setJustification(FieldConfig.RIGHT.equals(fieldConfig.getJustify()) ?
            FieldDefinition.RIGHT : FieldDefinition.LEFT);
    }
    
    @Override
    protected void assignDefaultFieldPositions(RecordConfig recordConfig, FlatRecordDefinition recordDefinition) {
        updateFieldPositionAndLength(0, recordConfig.getBean(), recordDefinition.getBeanDefinition());
    }
    
    /*
     * Recursively updates the position of all fields.
     */
    private int updateFieldPositionAndLength(int nextPosition, BeanConfig beanConfig,
        BeanDefinition beanDefinition) {
        
        int i = 0;
        int startPosition = nextPosition;
        List<PropertyConfig> propertyList = beanConfig.getPropertyList();
        for (PropertyConfig property : propertyList) {
            if (property.isBean()) {
                BeanDefinition childBeanDefinition = (BeanDefinition) beanDefinition.getProperty(i);
                nextPosition = updateFieldPositionAndLength(nextPosition, (BeanConfig) property, 
                    childBeanDefinition);
            }
            else if (property.isField()) {
                FieldConfig field = (FieldConfig) property;
                FixedLengthFieldDefinition currentDefinition = (FixedLengthFieldDefinition) beanDefinition.getProperty(i);
                
                int position = field.getPosition();
                try {
                    // if position is not set, use the next position
                    if (position < 0) {
                        position = nextPosition;
                    }
                    currentDefinition.setPosition(position);
                    nextPosition = nextPosition + currentDefinition.getLength() * currentDefinition.getMinOccurs();
                }
                catch (BeanIOConfigurationException ex) {
                    throw new BeanIOConfigurationException("Invalid '" + field.getName() +
                        "' field configuration: " + ex.getMessage(), ex);
                }
            }
            ++i;
        }
        
        // adjust next position for recurring beans
        if (beanDefinition.getMinOccurs() > 1) {
            nextPosition += (nextPosition - startPosition) * (beanDefinition.getMinOccurs() - 1);
        }
        
        return nextPosition;
    }
    
    @Override
    protected FieldDefinition newFieldDefinition(FieldConfig field) {
        return new FixedLengthFieldDefinition();
    }
    
    @Override
    protected BeanDefinition newBeanDefinition(BeanConfig bean) {
        return new FixedLengthBeanDefinition();
    }

    @Override
    protected RecordDefinition newRecordDefinition(RecordConfig record) {
        return new FixedLengthRecordDefinition();
    }

    @Override
    protected StreamDefinition newStreamDefinition(StreamConfig stream) {
        return new FixedLengthStreamDefinition();
    }

    @Override
    protected RecordReaderFactory newRecordReaderFactory() {
        return new FixedLengthReaderFactory();
    }

    @Override
    protected RecordWriterFactory newRecordWriterFactory() {
        return new FixedLengthWriterFactory();
    }
}
