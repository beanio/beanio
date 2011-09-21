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
package org.beanio.stream;

import static org.junit.Assert.assertEquals;

import java.io.*;

import org.beanio.stream.delimited.*;
import org.junit.Test;

/**
 * JUnit test cases for testing the <tt>DelimitedWriter</tt> and <tt>DelimitedWriterFactory</tt>.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DelimitedWriterTest {

    private static final String lineSep = System.getProperty("line.separator");

    @Test(expected = IllegalArgumentException.class)
    public void testDelimiterIsEscape() {
        new DelimitedWriter(new StringWriter(), ',', ',');
    }

    @Test
    public void testDefaultConfiguration() throws IOException {
        StringWriter text = new StringWriter();
        RecordWriter out = new DelimitedWriter(text);
        out.write(new String[] { "value1", "value\t2" });
        assertEquals("value1\tvalue\t2" + lineSep, text.toString());
    }

    @Test
    public void testCustomDelimiter() throws IOException {
        StringWriter text = new StringWriter();
        RecordWriter out = new DelimitedWriter(text, ',');
        out.write(new String[] { "value1", "value2\t", "" });
        assertEquals("value1,value2\t," + lineSep, text.toString());
    }

    @Test
    public void testCustomDelimiterAndEscape() throws IOException {
        StringWriter text = new StringWriter();
        RecordWriter out = new DelimitedWriter(text, ',', '\\');
        out.write(new String[] { "value1", "value2," });
        assertEquals("value1,value2\\," + lineSep, text.toString());
    }

    @Test
    public void testDefaultFactoryConfiguration() throws IOException {
        DelimitedWriterFactory factory = new DelimitedWriterFactory();
        StringWriter text = new StringWriter();
        RecordWriter out = factory.createWriter(text);
        out.write(new String[] { "value1", "value\t2" });
        assertEquals("value1\tvalue\t2" + lineSep, text.toString());
    }

    @Test
    public void testCustomFactoryConfiguration() throws IOException {
        DelimitedWriterFactory factory = new DelimitedWriterFactory();
        factory.setDelimiter(',');
        factory.setEscape('\\');
        factory.setRecordTerminator("");
        StringWriter text = new StringWriter();
        RecordWriter out = factory.createWriter(text);
        out.write(new String[] { "value1", "value,2" });
        assertEquals("value1,value\\,2", text.toString());
    }

    @Test
    public void testFlushAndClose() throws IOException {
        DelimitedWriterFactory factory = new DelimitedWriterFactory();
        factory.setDelimiter(',');
        factory.setEscape('\\');
        factory.setRecordTerminator("");
        StringWriter text = new StringWriter();
        RecordWriter out = factory.createWriter(new BufferedWriter(text));
        out.write(new String[] { "v" });
        out.flush();
        assertEquals("v", text.toString());
        out.close();
    }
}
