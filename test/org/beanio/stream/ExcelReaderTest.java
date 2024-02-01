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

import static org.junit.Assert.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.beanio.stream.excel.*;
import org.beanio.util.ExcelUtilTest;
import org.junit.*;

/**
 * JUnit test cases for <tt>ExcelReader</tt> and
 * <tt>ExcelRecordParserFactory</tt>.
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M3
 */
public class ExcelReaderTest {

    @Test
    public void testBasic() throws IOException {
        ExcelRecordParserFactory factory = new ExcelRecordParserFactory();
        String[] expected = new String[] { "1", "22", "33", "444" };
        ExcelReader in = (ExcelReader) factory.createReader(ExcelUtilTest.convertCSV2Excel("1,22,33,444"));
        assertArrayEquals(expected, in.read());
        assertNull(in.read());
    }

    @Test
    public void testClose() throws IOException {
        ExcelRecordParserFactory factory = new ExcelRecordParserFactory();
        ExcelReader in = (ExcelReader) factory.createReader(ExcelUtilTest.convertCSV2Excel(""));
        in.close();
    }


}
