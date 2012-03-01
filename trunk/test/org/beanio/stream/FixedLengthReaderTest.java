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

import static org.junit.Assert.*;

import java.io.*;

import org.beanio.stream.fixedlength.*;
import org.junit.Test;

/**
 * JUntil test cases for the <tt>FixedLengthReader</tt> and <tt>FixedLengthRecordParserFactory</tt>.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthReaderTest {

    @Test
    public void testBasic() throws IOException {
        FixedLengthRecordParserFactory factory = new FixedLengthRecordParserFactory();
        FixedLengthReader in = createReader(factory, "1111\n2222");
        assertEquals(in.read(), "1111");
        assertEquals(in.read(), "2222");
        assertNull(in.read());
    }

    @Test
    public void testLineContinuation() throws IOException {
        FixedLengthRecordParserFactory factory = new FixedLengthRecordParserFactory();
        factory.setLineContinuationCharacter('\\');
        FixedLengthReader in = createReader(factory, "11\\\n22\n33\\\r\n\\44");
        assertEquals("1122", in.read());
        assertEquals(1, in.getRecordLineNumber());
        assertEquals("33\\44", in.read());
        assertEquals(3, in.getRecordLineNumber());
        assertNull(in.read());
    }

    @Test
    public void testCustomLineContinuationChar() throws IOException {
        FixedLengthRecordParserFactory factory = new FixedLengthRecordParserFactory();
        factory.setLineContinuationCharacter('#');
        FixedLengthReader in = createReader(factory, "11#\n22\n33");
        assertEquals(in.read(), "1122");
        assertEquals(1, in.getRecordLineNumber());
        assertEquals(in.read(), "33");
        assertEquals(3, in.getRecordLineNumber());
        assertNull(in.read());
    }

    @Test(expected = RecordIOException.class)
    public void testLineContinuationError() throws IOException {
        FixedLengthRecordParserFactory factory = new FixedLengthRecordParserFactory();
        factory.setLineContinuationCharacter('\\');
        FixedLengthReader in = createReader(factory, "11\\");
        assertEquals(in.read(), "1122");
        assertEquals(1, in.getRecordLineNumber());
        assertEquals(in.read(), "33");
        assertEquals(3, in.getRecordLineNumber());
        assertNull(in.read());
    }

    @Test
    public void testCR() throws IOException {
        FixedLengthRecordParserFactory factory = new FixedLengthRecordParserFactory();
        FixedLengthReader in = createReader(factory, "1111\r2222");
        assertEquals(in.read(), "1111");
        assertEquals(1, in.getRecordLineNumber());
        assertEquals(in.read(), "2222");
        assertEquals(2, in.getRecordLineNumber());
        assertNull(in.read());
    }

    @Test
    public void testLF() throws IOException {
        FixedLengthRecordParserFactory factory = new FixedLengthRecordParserFactory();
        FixedLengthReader in = createReader(factory, "1111\n2222");
        assertEquals(in.read(), "1111");
        assertEquals(1, in.getRecordLineNumber());
        assertEquals(in.read(), "2222");
        assertEquals(2, in.getRecordLineNumber());
        assertNull(in.read());
    }

    @Test
    public void testCRLF() throws IOException {
        FixedLengthRecordParserFactory factory = new FixedLengthRecordParserFactory();
        FixedLengthReader in = createReader(factory, "1111\r\n2222");
        assertEquals(in.read(), "1111");
        assertEquals(1, in.getRecordLineNumber());
        assertEquals(in.read(), "2222");
        assertEquals(2, in.getRecordLineNumber());
        assertNull(in.read());
    }
    
    @Test
    public void testRecordTerminator() throws IOException {
        FixedLengthRecordParserFactory factory = new FixedLengthRecordParserFactory();
        factory.setLineContinuationCharacter('\\');
        factory.setRecordTerminator("*");
        FixedLengthReader in = createReader(factory, "11\\*22*33\\44*");
        assertEquals("1122", in.read());
        assertEquals(0, in.getRecordLineNumber());
        assertEquals("33\\44", in.read());
        assertEquals(0, in.getRecordLineNumber());
        assertNull(in.read());
        assertEquals(-1, in.getRecordLineNumber());
    }
    
    @Test
    public void testEmpty() throws IOException {
        assertNull(new FixedLengthReader(new StringReader("")).read());
    }
    
    @Test
    public void testComments() throws IOException {
        FixedLengthParserConfiguration config = new FixedLengthParserConfiguration();
        config.setComments(new String[] { "#", "//" });
        config.setRecordTerminator("+");
        
        StringReader input = new StringReader(
            "# comment+" +
            "one+" +
            "/+" +
            "+" +
            "// ignored+" +
            "//");
        
        FixedLengthReader in = new FixedLengthReader(input, config);
        assertEquals("one", in.read());
        assertEquals("/", in.read());
        assertEquals("", in.read());
        assertNull(in.read());
    }

    private FixedLengthReader createReader(FixedLengthRecordParserFactory factory, String input) {
        return (FixedLengthReader) factory.createReader(createInput(input));
    }

    private Reader createInput(String s) {
        return new StringReader(s);
    }
}
