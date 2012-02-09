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
package org.beanio.internal.parser.format.csv;

import java.io.*;

import org.beanio.internal.parser.StreamFormatSupport;
import org.beanio.internal.parser.format.delimited.DelimitedStreamFormat;
import org.beanio.stream.*;
import org.beanio.stream.csv.*;

/**
 * A {@link StreamFormatSupport} implementation for the CSV format.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class CsvStreamFormat extends DelimitedStreamFormat {

    /**
     * Constructs a new <tt>CsvStreamFormat</tt>.
     */
    public CsvStreamFormat() { }
    
    @Override
    public RecordReader createDefaultReader(Reader in) {
        return new CsvReader(in);
    }

    @Override
    public RecordWriter createDefaultWriter(Writer out) {
        return new CsvWriter(out);
    }
}
