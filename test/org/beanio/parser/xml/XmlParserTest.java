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
package org.beanio.parser.xml;

import java.io.*;

import org.beanio.parser.ParserTest;
import org.beanio.util.IOUtil;

/**
 * Base class for XML parser JUnit test cases.
 * 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlParserTest extends ParserTest {

    /**
     * Loads the contents of a file into a String.
     * @param filename the name of the file to load
     * @return the file contents
     * @throws IOException if an I/O error occurs
     */
    public String load(String filename) throws IOException {
        Reader in = new InputStreamReader(getClass().getResourceAsStream(filename));
        StringBuilder s = new StringBuilder();
        try {
            int n = -1;
            char [] c = new char[1024];
            while ((n = in.read(c)) != -1) {
                s.append(c, 0, n);
            }
            return s.toString();
        }
        finally {
            IOUtil.closeQuietly(in);
        }
    }
}
