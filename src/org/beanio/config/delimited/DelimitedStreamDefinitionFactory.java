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

import java.util.List;

import org.beanio.BeanIOConfigurationException;
import org.beanio.config.*;
import org.beanio.parser.*;
import org.beanio.parser.delimited.*;
import org.beanio.stream.*;
import org.beanio.stream.delimited.*;

/**
 * Stream definition factory for delimited formatted streams.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DelimitedStreamDefinitionFactory extends StreamDefinitionFactory {

    @Override
    protected void compileFieldDefinitions(RecordConfig config, RecordDefinition recordDefinition) {
        super.compileFieldDefinitions(config, recordDefinition);

        DelimitedRecordDefinition definition = (DelimitedRecordDefinition) recordDefinition;

        List<FieldDefinition> fieldList = definition.getFieldList();

        // set field positions
        int lastPosition = -1, index = 0;
        for (FieldConfig field : config.getFieldList()) {
            int position = field.getPosition();
            if (position < 0) {
                position = lastPosition + 1;
            }
            else if (position <= lastPosition) {
                throw new BeanIOConfigurationException("Field definition '" + field.getName() +
                    "' is out of order on record '" + config.getName() + "'");
            }
            lastPosition = position;
            fieldList.get(index++).setPosition(position);
        }

        // validate and set the minimum length of the record
        int recordLength = fieldList.get(fieldList.size() - 1).getPosition() + 1;
        if (config.getMinLength() == null) {
            definition.setMinLength(recordLength);
        }
        else if (config.getMinLength() < 0) {
            throw new BeanIOConfigurationException(
                "minLength must be at least 0 on record '" + config.getName() + "'");
        }
        else {
            definition.setMinLength(config.getMinLength());
        }

        // validate and set the maximum length of the record
        if (config.getMaxLength() == null) {
            definition.setMaxLength(recordLength);
        }
        else if (config.getMaxLength() > 0 && config.getMaxLength() < definition.getMinOccurs()) {
            throw new BeanIOConfigurationException(
                "maxLength must be greater than or equal to min length on record '"
                    + config.getName() + "'");
        }
        else {
            definition.setMaxLength(config.getMaxLength());
        }
    }

    @Override
    protected FieldDefinition newFieldDefinition(FieldConfig field) {
        return new DelimitedFieldDefinition();
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
