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

import org.beanio.BeanIOConfigurationException;
import org.beanio.config.*;
import org.beanio.parser.*;
import org.beanio.parser.fixedlength.*;
import org.beanio.stream.*;
import org.beanio.stream.fixedlength.*;

/**
 * A <tt>FixedLengthStreamDefinitionFactory</tt> is used to create fixed length
 * stream definitions from a mapping configuration.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthStreamDefinitionFactory extends StreamDefinitionFactory {

    @Override
    protected void compileFieldDefinitions(RecordConfig recordConfig,
            RecordDefinition recordDefinition) {
        
        super.compileFieldDefinitions(recordConfig, recordDefinition);

        FixedLengthRecordDefinition flRecordDefinition = (FixedLengthRecordDefinition) recordDefinition;

        int fieldCount = recordConfig.getFieldList().size();
        FieldConfig[] fieldArray = new FieldConfig[fieldCount];
        recordConfig.getFieldList().toArray(fieldArray);
        
        int[] position = new int[fieldCount];
        int[] length = new int[fieldCount];
        
        // initialize position and length values to what was set
        for (int i = 0; i < fieldCount; i++) {
            position[i] = fieldArray[i].getPosition();
            length[i] = fieldArray[i].getLength();
        }
        
        // by default, the first field position is 0 if not set
        if (position[0] < 0) {
            position[0] = 0;
        }
        
        FixedLengthFieldDefinition previousDefinition = null;
        FixedLengthFieldDefinition currentDefinition = null;
        
        // try to fill in missing position and length information
        for (int i = 0; i < fieldCount; i++) {
            FieldConfig fieldConfig = fieldArray[i];
            currentDefinition = (FixedLengthFieldDefinition) recordDefinition.getFieldList().get(i);
            
            try {
                // if position is not set, use the information from the previous field 
                //     position + length * minOccurs
                if (position[i] < 0) {
                    position[i] = previousDefinition.getPosition() + 
                        previousDefinition.getLength() * previousDefinition.getMinOccurs();
                }
                
                // -1 means not set, but 0 was explicitly set and invalid
                if (length[i] == 0) { 
                    throw new BeanIOConfigurationException("length must be greater than 0");
                }
                
                // if not set, try to determine the length of the field
                if (length[i] < 0) {
                    // if a literal value is set, use the length of the literal
                    String literal = fieldArray[i].getLiteral();
                    if (literal != null) {
                        length[i] = literal.length(); 
                    }
                    // if the position of the next field was set, the length is the difference in positions
                    else if (i < (fieldCount - 1) && position[i + 1] > 0 && currentDefinition.getMinOccurs() == 1) {
                        length[i] = position[i + 1] - position[i];
                    }
                    // give up
                    if (length[i] < 0) {
                        throw new BeanIOConfigurationException("length is required");
                    }
                }
                
                currentDefinition.setPosition(position[i]);
                currentDefinition.setLength(length[i]);
                currentDefinition.setPadding(fieldConfig.getPadding());
                currentDefinition.setJustification("right".equals(fieldConfig.getJustify()) ?
                    FixedLengthFieldDefinition.RIGHT : FixedLengthFieldDefinition.LEFT);
    
                previousDefinition = currentDefinition;
            }
            catch (BeanIOConfigurationException ex) {
                throw new BeanIOConfigurationException("Invalid '" + fieldConfig.getName() +
                    "' field configuration: " + ex.getMessage(), ex);
            }
        }

        // set the minimum length of the record
        Integer minLength = recordConfig.getMinLength();
        if (minLength == null) {
            // calculate the default minimum record length based on minimum record length
            minLength = currentDefinition.getPosition() + 
                currentDefinition.getLength() * currentDefinition.getMinOccurs();
        }
        else if (minLength < 0) {
            throw new BeanIOConfigurationException("Invalid minLength"); 
        }
        
        // set the maximum length of the record
        Integer maxLength = recordConfig.getMaxLength();
        if (maxLength == null) {
            // calculate the default maximum record length
            if (currentDefinition.getMaxOccurs() < 0) {
                maxLength = -1;
            }
            else if (currentDefinition.getMaxOccurs() != 1) {
                maxLength = currentDefinition.getPosition() + 
                    currentDefinition.getLength() * currentDefinition.getMaxOccurs();
            }
            else {
                maxLength = minLength;
            }
        }
        else if (maxLength < 1) {
            throw new BeanIOConfigurationException("Invalid maxLength");
        }
        else if (recordConfig.getMinLength() != null && maxLength < minLength) {
            throw new BeanIOConfigurationException("maxLength must be greater than or equal to minLength");
        }
        
        flRecordDefinition.setMinLength(minLength);
        flRecordDefinition.setMaxLength(maxLength);
    }

    @Override
    protected FieldDefinition newFieldDefinition(FieldConfig field) {
        return new FixedLengthFieldDefinition();
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
