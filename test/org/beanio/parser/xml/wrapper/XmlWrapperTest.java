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
package org.beanio.parser.xml.wrapper;

import static org.junit.Assert.*;

import java.io.*;
import java.util.List;

import org.beanio.*;
import org.beanio.parser.xml.*;
import org.junit.*;

/**
 * JUnit test cases for testing XML wrapper elements.
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlWrapperTest extends XmlParserTest {

    private StreamFactory factory;

    @Before
    public void setup() throws Exception {
        factory = newStreamFactory("wrapper_mapping.xml");
    }
    
    /**
     * Test a xmlWrapper configuration.
     */
    @Test
    public void testFieldCollection() throws Exception {
        BeanReader in = factory.createReader("stream", new InputStreamReader(
            getClass().getResourceAsStream("w1_in.xml")));
        
        StringWriter s = new StringWriter();
        BeanWriter out = factory.createWriter("stream", s);
        try {
            Person person = (Person) in.read();
            assertEquals("John", person.getFirstName());
            assertEquals("Smith", person.getLastName());
            List<String> list = person.getColor();
            assertEquals(3, list.size());
            assertEquals("Red", list.get(0));
            assertEquals("Blue", list.get(1));
            assertEquals("Green", list.get(2));
            List<Address> addressList = person.getAddressList();
            assertEquals(0, addressList.size());
            out.write(person);
            
            person = (Person) in.read();
            assertNull(person.getFirstName());
            assertNull(person.getLastName());
            assertEquals(0, person.getColor().size());
            addressList = person.getAddressList();
            assertEquals(2, addressList.size());
            assertEquals("CO", addressList.get(0).getState());
            assertEquals("IL", addressList.get(1).getState());
            out.write(person);
            
            out.flush();
            assertEquals(load("w1_in.xml"), s.toString());
        }
        finally {
            in.close();
        }
    }
}
