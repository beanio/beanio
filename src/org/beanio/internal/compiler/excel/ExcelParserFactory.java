/*
 * Copyright 2023 Vidhya Sagar, Jeevendran
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
package org.beanio.internal.compiler.excel;

import org.beanio.internal.compiler.ParserFactory;
import org.beanio.internal.compiler.delimited.DelimitedParserFactory;
import org.beanio.internal.config.*;
import org.beanio.internal.parser.*;

import org.beanio.internal.parser.format.excel.*;
import org.beanio.stream.*;
import org.beanio.stream.excel.ExcelRecordParserFactory;

/**
 * A {@link ParserFactory} for the excel stream format.
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M3
 */
public class ExcelParserFactory extends DelimitedParserFactory {

    @Override
    public StreamFormat createStreamFormat(StreamConfig config) {
        ExcelStreamFormat format = new ExcelStreamFormat();
        format.setName(config.getName());
        format.setRecordParserFactory(createRecordParserFactory(config));
        return format;
    }

    @Override
    protected RecordParserFactory getDefaultRecordParserFactory() {
        return new ExcelRecordParserFactory();
    }
}
