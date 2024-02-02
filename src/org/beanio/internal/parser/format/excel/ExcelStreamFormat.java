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
package org.beanio.internal.parser.format.excel;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import org.beanio.internal.parser.*;
import org.beanio.internal.parser.format.delimited.DelimitedStreamFormat;
import org.beanio.stream.RecordReader;
import org.beanio.stream.RecordWriter;

/**
 * A {@link StreamFormatSupport} implementation for the excel stream format.
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M3
 */
public class ExcelStreamFormat extends DelimitedStreamFormat {

    @Override
    public RecordReader createRecordReader(Reader in) {
        throw new UnsupportedOperationException("character sequence processing not supported by excel formal");
    }

    @Override
    public RecordWriter createRecordWriter(Writer out) {
        throw new UnsupportedOperationException("character sequence processing not supported by excel formal");
    }

    @Override
    public RecordReader createRecordReader(InputStream in) {
        return super.getRecordParserFactory().createReader(in);
    }

    @Override
    public RecordWriter createRecordWriter(OutputStream out) {
        return super.getRecordParserFactory().createWriter(out);
    }

}
