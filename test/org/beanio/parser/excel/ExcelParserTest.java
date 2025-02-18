/*
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
package org.beanio.parser.excel;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import org.beanio.*;
import org.beanio.parser.ParserTest;
import org.beanio.util.ExcelUtilTest;
import org.junit.*;

/**
 * JUnit test cases for excel streams.
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M3
 */
public class ExcelParserTest extends ParserTest {

    private StreamFactory factory;

    @Before
    public void setup() throws Exception {
        factory = newStreamFactory("excel.xml");
    }

    @Test
    public void testRequiredField() {
        BeanReader in = factory.createReader("e1",
                getClass().getResourceAsStream("e1_recordErrors.xlsx"));

        try {
            assertRecordError(in, 3, "record1", "Too many fields 4");
            assertFieldError(in, 4, "record1", "field4", null, "Required field not set");
        } finally {
            in.close();
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testOptionalField() {
        BeanReader in = factory.createReader("e2",
                getClass().getResourceAsStream("e2_optionalField.xlsx"));

        try {
            Map map = (Map) in.read();
            assertEquals("value1", map.get("field1"));
            assertEquals("value2", map.get("field2"));
            assertNull(map.get("field3"));
            assertNull(map.get("field4"));
        } finally {
            in.close();
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testPadding() throws IOException {

        BeanReader in = factory.createReader("e3",
                getClass().getResourceAsStream("e3_padding.xlsx"));

        try {
            Map map = (Map) in.read();
            assertArrayEquals(new String[] { "1", "2", "3", "" }, (String[]) map.get("field1"));

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BeanWriter writer = factory.createWriter("e3", out);
            writer.write(map);
            assertEquals("xx1,xx2,xx3,xxx\n", ExcelUtilTest
                    .convertExcelStream2CSV(new ByteArrayInputStream(out.toByteArray())));
        } finally {
            in.close();
        }
    }

    @Test
    public void testPasswordProetection() throws IOException {

        Map<String, String[]> record1 = new HashMap<>();
        record1.put("field1", new String[] { "1", "2", "3", "4" });

        Map<String, String[]> record2 = new HashMap<>();
        record2.put("field1", new String[] { "5", "6", "7", "8" });

        Map<String, String[]> record3 = new HashMap<>();
        record3.put("field1", new String[] { "9", "10" });

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BeanWriter writer = factory.createWriter("e4", out);
        writer.write(record1);
        writer.write(record2);
        writer.write(record3);
        writer.close();

        assertEquals("1,2,3,4\n5,6,7,8\n9,10\n", ExcelUtilTest
                .convertExcelStream2CSV(new ByteArrayInputStream(out.toByteArray()), "testPassword"));

        BeanReader in = factory.createReader("e4", new ByteArrayInputStream(out.toByteArray()));

        assertArrayEquals((String[]) record1.get("field1"), ((String[]) ((Map) in.read()).get("field1")));
        assertArrayEquals((String[]) record2.get("field1"), ((String[]) ((Map) in.read()).get("field1")));
        assertArrayEquals((String[]) record3.get("field1"), ((String[]) ((Map) in.read()).get("field1")));

    }

}
