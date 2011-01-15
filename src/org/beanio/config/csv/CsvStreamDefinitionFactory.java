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
package org.beanio.config.csv;

import org.beanio.config.StreamConfig;
import org.beanio.config.delimited.DelimitedStreamDefinitionFactory;
import org.beanio.parser.StreamDefinition;
import org.beanio.parser.csv.CsvStreamDefinition;
import org.beanio.stream.*;
import org.beanio.stream.csv.*;

/**
 * Stream definition factory for CSV formatted streams.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class CsvStreamDefinitionFactory extends DelimitedStreamDefinitionFactory {

    @Override
    protected StreamDefinition createStreamDefinition(StreamConfig stream) {
        return new CsvStreamDefinition();
    }

    @Override
    protected RecordReaderFactory createDefaultRecordReaderFactory() {
        return new CsvReaderFactory();
    }

    @Override
    protected RecordWriterFactory createDefaultRecordWriterFactory() {
        return new CsvWriterFactory();
    }
}
