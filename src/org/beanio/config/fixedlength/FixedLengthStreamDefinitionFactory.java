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

        FixedLengthRecordDefinition flRecordDefinition =
            (FixedLengthRecordDefinition) recordDefinition;

        // attempt to guess missing position and width settings
        int fieldCount = recordConfig.getFieldList().size();
        FieldConfig[] fieldArray = new FieldConfig[fieldCount];
        recordConfig.getFieldList().toArray(fieldArray);
        int[] position = new int[fieldCount];
        int[] width = new int[fieldCount];
        for (int i = 0; i < fieldCount; i++) {
            position[i] = fieldArray[i].getPosition() - 1;
            width[i] = fieldArray[i].getLength();

            if (width[i] == 0) {
                throw new BeanIOConfigurationException(
                    "Invalid length of field '" + fieldArray[i].getName() + "'");
            }
        }
        if (position[0] < 0) {
            position[0] = 0;
        }
        for (int i = 0; i < fieldCount; i++) {
            if (position[i] < 0) {
                position[i] = position[i - 1] + width[i - 1];
            }
            if (width[i] < 0) {
                if (i < (fieldCount - 1) && position[i + 1] > 0) {
                    width[i] = position[i + 1] - position[i];
                }
                if (width[i] < 0) {
                    throw new BeanIOConfigurationException(
                        "Length not set on field '" + fieldArray[i].getName() + "'");
                }
            }
        }

        int recordWidth = 0;
        int index = 0;
        for (FieldDefinition fieldDefinition : recordDefinition.getFieldList()) {
            FieldConfig fieldConfig = fieldArray[index];

            FixedLengthFieldDefinition flFieldDefinitin =
                (FixedLengthFieldDefinition) fieldDefinition;
            flFieldDefinitin.setPosition(position[index]);
            flFieldDefinitin.setPadding(fieldConfig.getPadding());
            flFieldDefinitin.setLength(width[index]);
            flFieldDefinitin.setJustification("right".equals(fieldConfig.getJustify()) ?
                FixedLengthFieldDefinition.RIGHT : FixedLengthFieldDefinition.LEFT);

            recordWidth = flFieldDefinitin.getPosition() + flFieldDefinitin.getLength();
            ++index;
        }

        // set the min and max record length (default to calculated record width)
        Integer minLength = recordConfig.getMinLength();
        if (minLength == null) {
            minLength = recordWidth;
        }
        Integer maxLength = recordConfig.getMaxLength();
        if (maxLength == null) {
            maxLength = recordWidth;
        }
        flRecordDefinition.setMinLength(minLength);
        flRecordDefinition.setMaxLength(maxLength);
    }

    @Override
    protected FieldDefinition createFieldDefinition(FieldConfig field) {
        return new FixedLengthFieldDefinition();
    }

    @Override
    protected RecordDefinition createRecordDefinition(RecordConfig record) {
        return new FixedLengthRecordDefinition();
    }

    @Override
    protected StreamDefinition createStreamDefinition(StreamConfig stream) {
        return new FixedLengthStreamDefinition();
    }

    @Override
    protected RecordReaderFactory createDefaultRecordReaderFactory() {
        return new FixedLengthReaderFactory();
    }

    @Override
    protected RecordWriterFactory createDefaultRecordWriterFactory() {
        return new FixedLengthWriterFactory();
    }
}
