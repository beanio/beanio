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
package org.beanio.parser.csv;

import java.io.*;

import org.beanio.parser.delimited.DelimitedStreamDefinition;
import org.beanio.stream.*;
import org.beanio.stream.csv.*;

/**
 * A <tt>CsvStreamDefinition</tt> is used to parse and format streams
 * that use a fixed length record format.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class CsvStreamDefinition extends DelimitedStreamDefinition {

    /**
     * Constructs a new <tt>CsvStreamDefinition</tt>.
     */
    public CsvStreamDefinition() {
        super("csv");
    }

    @Override
    protected RecordReader createDefaultReader(Reader in) {
        return new CsvReader(in);
    }

    @Override
    protected RecordWriter createDefaultWriter(Writer out) {
        return new CsvWriter(out);
    }
}
