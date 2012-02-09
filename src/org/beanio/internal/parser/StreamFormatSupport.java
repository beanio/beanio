/*
 * Copyright 2011 Kevin Seim
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
package org.beanio.internal.parser;

import java.io.*;

import org.beanio.stream.*;

public abstract class StreamFormatSupport implements StreamFormat {

    private String name;
    private RecordReaderFactory readerFactory;
    private RecordWriterFactory writerFactory;
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    /**
     * Creates a new <tt>RecordReader</tt> to read from the given input stream.
     * This method delegates to the configured reader factory, or if null, 
     * it calls {@link #createDefaultReader(Reader)}.
     * @param in the input stream to read from
     * @return a new <tt>RecordReader</tt>
     */
    public RecordReader createRecordReader(Reader in) {
        if (readerFactory == null)
            return createDefaultReader(in);
        else
            return readerFactory.createReader(in);
    }

    /**
     * Creates a new <tt>RecordWriter</tt> for writing to the given output stream.
     * This method delegates to the configured record writer factory, or if null, 
     * it calls {@link #createDefaultWriter(Writer)}.
     * @param out the output stream to write to
     * @return a new <tt>RecordWriter</tt>
     */
    public RecordWriter createRecordWriter(Writer out) {
        if (writerFactory == null)
            return createDefaultWriter(out);
        else
            return writerFactory.createWriter(out);
    }
    
    protected abstract RecordReader createDefaultReader(Reader in);

    protected abstract RecordWriter createDefaultWriter(Writer out);

    public RecordReaderFactory getReaderFactory() {
        return readerFactory;
    }

    public void setReaderFactory(RecordReaderFactory readerFactory) {
        this.readerFactory = readerFactory;
    }

    public RecordWriterFactory getWriterFactory() {
        return writerFactory;
    }

    public void setWriterFactory(RecordWriterFactory writerFactory) {
        this.writerFactory = writerFactory;
    }
}
