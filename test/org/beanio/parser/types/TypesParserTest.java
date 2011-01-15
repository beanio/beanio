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
package org.beanio.parser.types;

import static org.junit.Assert.*;

import java.io.*;
import java.math.*;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.beanio.*;
import org.beanio.parser.ParserTest;
import org.junit.*;

/**
 * JUnit test cases for testing type handlers.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class TypesParserTest extends ParserTest {

    private String lineSep = System.getProperty("line.separator");
    private StreamFactory factory;

    @Before
    public void setup() throws Exception {
        factory = newStreamFactory("types.xml");
    }

    /*
     * Test type handlers for objects.
     */
    @Test
    public void testObjectHandlers() throws Exception {
        StringWriter text;
        BeanReader in = factory.createReader("t1", new InputStreamReader(
                getClass().getResourceAsStream("t1_valid.txt")));
        try {
            ObjectRecord record;
            record = (ObjectRecord) in.read();
            assertEquals(new Byte((byte) 10), record.getByteValue());
            assertEquals(new Short((short) 10), record.getShortValue());
            assertEquals(new Integer(-10), record.getIntegerValue());
            assertEquals(new Long(10), record.getLongValue());
            assertEquals(new Float(10.1f), record.getFloatValue());
            assertEquals(new Double(-10.1), record.getDoubleValue());
            assertEquals(new Character('A'), record.getCharacterValue());
            assertEquals("ABC", record.getStringValue());
            assertEquals(new SimpleDateFormat("MMddyy").parse("010170"), record.getDateValue());
            assertEquals(Boolean.TRUE, record.getBooleanValue());
            assertEquals(new BigInteger("10"), record.getBigIntegerValue());
            assertEquals(new BigDecimal("10"), record.getBigDecimalValue());

            text = new StringWriter();
            factory.createWriter("t1", text).write(record);
            assertEquals("10,10,-10,10,10.1,-10.1,A,ABC,010170,true,10,10" + lineSep, text.toString());

            record = (ObjectRecord) in.read();
            assertNull(record.getByteValue());
            assertNull(record.getShortValue());
            assertNull(record.getIntegerValue());
            assertNull(record.getLongValue());
            assertNull(record.getFloatValue());
            assertNull(record.getDoubleValue());
            assertNull(record.getCharacterValue());
            assertEquals("", record.getStringValue());
            assertNull(record.getDateValue());
            assertNull(record.getBooleanValue());
            assertNull(record.getBigIntegerValue());
            assertNull(record.getBigDecimalValue());

            text = new StringWriter();
            factory.createWriter("t1", text).write(record);
            assertEquals(",,,,,,,,,,," + lineSep, text.toString());
        }
        finally {
            in.close();
        }
    }

    /*
     * Test type handlers for primitives.
     */
    @Test
    public void testPrimitiveHandlers() throws Exception {
        StringWriter text;
        BeanReader in = factory.createReader("t2", new InputStreamReader(
            getClass().getResourceAsStream("t2_valid.txt")));
        try {
            PrimitiveRecord record;
            record = (PrimitiveRecord) in.read();
            assertEquals(10, record.getByteValue());
            assertEquals(10, record.getShortValue());
            assertEquals(-10, record.getIntValue());
            assertEquals(10, record.getLongValue());
            assertEquals("10.1", Float.toString(record.getFloatValue()));
            assertEquals("-10.1", Double.toString(record.getDoubleValue()));
            assertEquals('A', record.getCharValue());
            assertTrue(record.getBooleanValue());

            text = new StringWriter();
            factory.createWriter("t2", text).write(record);
            assertEquals("10,10,-10,10,10.1,-10.1,A,true" + lineSep, text.toString());

            record = (PrimitiveRecord) in.read();
            assertEquals(0, record.getByteValue());
            assertEquals(0, record.getShortValue());
            assertEquals(0, record.getIntValue());
            assertEquals(0, record.getLongValue());
            assertEquals("0.0", Float.toString(record.getFloatValue()));
            assertEquals("0.0", Double.toString(record.getDoubleValue()));
            assertEquals('x', record.getCharValue());
            assertFalse(record.getBooleanValue());

            text = new StringWriter();
            factory.createWriter("t2", text).write(record);
            assertEquals("0,0,0,0,0.0,0.0,x,false" + lineSep, text.toString());
        }
        finally {
            in.close();
        }
    }

    /*
     * Test overridden / stream specific type handler.
     */
    @Test
    public void testStreamTypeHandler() throws Exception {
        StringWriter text;
        BeanReader in = factory.createReader("t3", new InputStreamReader(
            getClass().getResourceAsStream("t3_valid.txt")));
        try {
            ObjectRecord record;
            record = (ObjectRecord) in.read();
            assertEquals(new SimpleDateFormat("MMddyy").parse("010170"), record.getDateValue());

            text = new StringWriter();
            factory.createWriter("t3", text).write(record);
            assertEquals("01-01-1970" + lineSep, text.toString());
        }
        finally {
            in.close();
        }
    }

    /*
     * Test a named type handler.
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testNamedTypeHandler() throws Exception {
        StringWriter text;
        BeanReader in = factory.createReader("t4", new InputStreamReader(
            getClass().getResourceAsStream("t4_valid.txt")));
        try {
            Map record;
            record = (Map) in.read();
            assertEquals(new SimpleDateFormat("hh:mm:ss").parse("12:00:00"), record.get("dateValue"));

            text = new StringWriter();
            factory.createWriter("t4", text).write(record);
            assertEquals("12:00:00" + lineSep, text.toString());
        }
        finally {
            in.close();
        }
    }

    /*
     * Test the string type handler with non default settings.
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void testStringTypeHandler() throws Exception {
        StringWriter text;
        BeanReader in = factory.createReader("t5", new InputStreamReader(
            getClass().getResourceAsStream("t5_valid.txt")));
        try {
            Map record;
            record = (Map) in.read();
            assertEquals("", record.get("field"));

            text = new StringWriter();
            factory.createWriter("t5", text).write(record);
            assertEquals("" + lineSep, text.toString());

            record = (Map) in.read();
            assertEquals("Text", record.get("field"));
        }
        finally {
            in.close();
        }
    }
    
    @Test
    public void testNullPrimitive() throws Exception {
        BeanReader in = factory.createReader("t6", new StringReader("\n"));
        assertFieldError(in, 1, "record", "intValue", "", 
            "Type conversion error: Primitive bean property cannot be null");
    }
}
