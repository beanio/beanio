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
package org.beanio.stream;

import static org.junit.Assert.assertEquals;

import java.io.*;

import org.beanio.stream.excel.*;
import org.beanio.util.ExcelUtilTest;
import org.junit.*;

/**
 * JUnit test cases for testing the {@link ExcelWriter} and
 * {@link ExcelRecordParserFactory}.
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M3
 */
public class ExcelWriterTest {

    private ExcelRecordParserFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new ExcelRecordParserFactory();
    }

    @Test
    public void testBasic() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        RecordWriter out = factory.createWriter(outStream);
        out.write(new String[] { "value1", "value\t2" });
        out.flush();
        assertEquals("value1,value\t2\n", ExcelUtilTest
                .convertExcelStream2CSV(new ByteArrayInputStream(outStream.toByteArray())).toString());
    }

    @Test
    public void testFlushAndClose() throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        RecordWriter out = factory.createWriter(outStream);
        out.write(new String[] { "v" });
        out.flush();
        assertEquals("v\n", ExcelUtilTest
                .convertExcelStream2CSV(new ByteArrayInputStream(outStream.toByteArray())).toString());
        out.close();
    }

}
