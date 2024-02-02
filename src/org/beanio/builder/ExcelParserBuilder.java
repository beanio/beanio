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
package org.beanio.builder;

import org.beanio.internal.config.BeanConfig;
import org.beanio.stream.RecordParserFactory;
import org.beanio.stream.excel.ExcelRecordParserFactory;

/**
 * Builder for excel parsers.
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M2
 */
public class ExcelParserBuilder extends ParserBuilder {

    private ExcelRecordParserFactory parser = new ExcelRecordParserFactory();

    public ExcelParserBuilder() {
    }

    public ExcelParserBuilder(int sheetIndex) {
        parser.setSheetIndex(sheetIndex);
    }

    public ExcelParserBuilder sheetIndex(int sheetIndex) {
        parser.setSheetIndex(sheetIndex);
        return this;
    }

    public ExcelParserBuilder sheetName(String sheetName) {
        parser.setSheetName(sheetName);
        return this;
    }

    public ExcelParserBuilder password(String password) {
        parser.setPassword(password);
        return this;
    }

    public ExcelParserBuilder excelType(String excelType) {
        parser.setExcelType(excelType);
        return this;
    }

    @Override
    public BeanConfig<RecordParserFactory> build() {
        BeanConfig<RecordParserFactory> config = new BeanConfig<>();
        config.setInstance(parser);
        return config;
    }
}
