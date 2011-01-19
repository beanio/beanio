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
package org.beanio.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.*;

import org.beanio.*;
import org.beanio.util.IOUtil;

/**
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class ParserTest {

    protected static String lineSeparator = System.getProperty("line.separator");
    
    /**
     * 
     * @param config
     * @return
     * @throws IOException
     */
    protected StreamFactory newStreamFactory(String config) throws IOException {
        StreamFactory factory = StreamFactory.newInstance();
        InputStream in = getClass().getResourceAsStream(config);
        try {
            factory.load(in);
            return factory;
        }
        finally {
            IOUtil.closeQuietly(in);
        }
    }

    protected void assertRecordError(BeanReader in, int lineNumber, String recordName, String message) {
        try {
            in.read();
            fail("Record expected to fail validation");
        }
        catch (InvalidRecordException ex) {
            assertEquals(recordName, in.getRecordName());
            assertEquals(lineNumber, in.getLineNumber());

            BeanReaderContext ctx = ex.getContext();
            assertEquals(recordName, ctx.getRecordName());
            assertEquals(lineNumber, ctx.getRecordLineNumber());
            for (String s : ctx.getRecordErrors()) {
                assertEquals(message, s);
            }
        }
    }

    protected void assertFieldError(BeanReader in, int lineNumber, String recordName,
            String fieldName, String fieldText, String message) {
        try {
            in.read();
            fail("Record expected to fail validation");
        }
        catch (InvalidRecordException ex) {
            assertEquals(recordName, in.getRecordName());
            assertEquals(lineNumber, in.getLineNumber());

            BeanReaderContext ctx = ex.getContext();
            assertEquals(recordName, ctx.getRecordName());
            assertEquals(lineNumber, ctx.getRecordLineNumber());
            assertEquals(fieldText, ctx.getFieldText(fieldName));
            for (String s : ctx.getFieldErrors(fieldName)) {
                assertEquals(message, s);
            }
        }
    }
}
