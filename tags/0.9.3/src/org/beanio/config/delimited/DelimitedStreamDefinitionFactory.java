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
package org.beanio.config.delimited;

import org.beanio.config.*;
import org.beanio.config.flat.FlatStreamDefinitionFactory;
import org.beanio.parser.*;
import org.beanio.parser.delimited.*;
import org.beanio.parser.flat.FlatRecordDefinition;
import org.beanio.stream.*;
import org.beanio.stream.delimited.*;

/**
 * Stream definition factory for delimited formatted streams.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DelimitedStreamDefinitionFactory extends FlatStreamDefinitionFactory {

    @Override
    protected void assignDefaultFieldPositions(RecordConfig recordConfig, FlatRecordDefinition recordDefinition) {
        updateFieldPosition(0, recordConfig.getBean(), recordDefinition.getBeanDefinition());
    }
    
    @Override
    protected void updateFieldDefinition(FieldConfig fieldConfig, FieldDefinition fieldDefinition) {
        fieldDefinition.setLength(1);
        fieldDefinition.setPosition(fieldConfig.getPosition());
    }
    
    /*
     * Recursively updates the position of all fields.
     */
    private int updateFieldPosition(int currentPosition, BeanConfig beanConfig, BeanDefinition beanDefinition) {
        int index = 0;
        for (PropertyConfig propertyConfig : beanConfig.getPropertyList()) {
            if (propertyConfig.isBean()) {
                BeanDefinition child = (BeanDefinition) beanDefinition.getProperty(index);
                
                int start = currentPosition;
                int stop = updateFieldPosition(currentPosition, (BeanConfig) propertyConfig, child);
                
                currentPosition += (stop - start) * child.getMaxOccurs();
            }
            else {
                FieldDefinition fieldDefinition = (FieldDefinition) beanDefinition.getProperty(index);
                fieldDefinition.setPosition(currentPosition);
                currentPosition += fieldDefinition.getMinOccurs();
            }
            ++index;
        }
        return currentPosition;
    }

    @Override
    protected FieldDefinition newFieldDefinition(FieldConfig field) {
        return new DelimitedFieldDefinition();
    }
    
    @Override
    protected BeanDefinition newBeanDefinition(BeanConfig bean) {
        return new DelimitedBeanDefinition();
    }

    @Override
    protected RecordDefinition newRecordDefinition(RecordConfig record) {
        return new DelimitedRecordDefinition();
    }

    @Override
    protected StreamDefinition newStreamDefinition(StreamConfig stream) {
        return new DelimitedStreamDefinition();
    }

    @Override
    protected RecordReaderFactory newRecordReaderFactory() {
        return new DelimitedReaderFactory();
    }

    @Override
    protected RecordWriterFactory newRecordWriterFactory() {
        return new DelimitedWriterFactory();
    }
}
