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
package org.beanio.parser.xml.bean;

import static org.junit.Assert.*;

import java.io.*;

import org.beanio.*;
import org.beanio.parser.xml.*;
import org.junit.*;

/**
 * JUnit test cases related to reading and writing beans in an XML formatted stream. 
 * @author Kevin Seim
 * @since 1.1
 */
public class XmlBeansTest extends XmlParserTest {

    private StreamFactory factory;

    @Before
    public void setup() throws Exception {
        factory = newStreamFactory("beans_mapping.xml");
    }
    
    /**
     * Test a nillable child bean.
     */
    @Test
    public void testNillableBean() throws Exception {
        BeanReader in = factory.createReader("stream", new InputStreamReader(
            getClass().getResourceAsStream("b1_in.xml")));
        
        StringWriter s = new StringWriter();
        BeanWriter out = factory.createWriter("stream", s);
        try {
            Person person = (Person) in.read();
            assertEquals("John", person.getFirstName());
            Address address = person.getAddress();
            assertEquals("IL", address.getState());
            assertEquals("60610", address.getZip());
            out.write(person);
            
            person = (Person) in.read();
            assertEquals("Mary", person.getFirstName());
            assertNull(person.getAddress());
            out.write(person);
            
            out.flush();
            assertEquals(load("b1_out.xml"), s.toString());
        }
        finally {
            in.close();
        }
    }
    
    /**
     * Test an optional (i.e. minOccurs="0") bean with a namespace.
     */
    @Test
    public void testOptionalBeanWithNamespace() throws Exception {
        BeanReader in = factory.createReader("stream2", new InputStreamReader(
            getClass().getResourceAsStream("b2_in.xml")));
        
        StringWriter s = new StringWriter();
        BeanWriter out = factory.createWriter("stream2", s);
        try {
            Person person = (Person) in.read();
            assertEquals("John", person.getFirstName());
            Address address = person.getAddress();
            assertEquals("IL", address.getState());
            assertEquals("60610", address.getZip());
            out.write(person);
            
            person = (Person) in.read();
            assertEquals("Mary", person.getFirstName());
            assertNull(person.getAddress());
            out.write(person);

            person = (Person) in.read();
            assertEquals("George", person.getFirstName());
            assertNull(person.getAddress());
            
            out.flush();
            assertEquals(load("b2_out.xml"), s.toString());
        }
        finally {
            in.close();
        }
    }
    
    /**
     * Test a nillable child bean.
     */
    @Test
    public void testBeanCollection() throws Exception {
        BeanReader in = factory.createReader("stream3", new InputStreamReader(
            getClass().getResourceAsStream("b3_in.xml")));
        
        StringWriter s = new StringWriter();
        BeanWriter out = factory.createWriter("stream3", s);
        try {
            Person person = (Person) in.read();
            assertEquals("John", person.getFirstName());
            assertEquals(3, person.getAddressList().size());
            int i=0;
            for (Address address : person.getAddressList()) {
                switch (++i) {
                case 1: assertEquals("IL", address.getState()); break;
                case 2: assertEquals("CO", address.getState()); break;
                case 3: assertEquals("MN", address.getState()); break;
                }
            }
            out.write(person);
            
            person = (Person) in.read();
            assertEquals("Mary", person.getFirstName());
            assertEquals(0, person.getAddressList().size());
            out.write(person);
            
            out.flush();
            assertEquals(load("b3_in.xml"), s.toString());
        }
        finally {
            in.close();
        }
    }
    
    /**
     * Test a bean where xmlType="none".
     */
    @Test
    public void testXmlTypeNoneBean() throws Exception {
        BeanReader in = factory.createReader("stream4", new InputStreamReader(
            getClass().getResourceAsStream("b4_in.xml")));
        
        StringWriter s = new StringWriter();
        BeanWriter out = factory.createWriter("stream4", s);
        try {
            Person person = (Person) in.read();
            assertEquals("John", person.getFirstName());
            Address address = person.getAddress();
            assertEquals("IL", address.getState());
            assertEquals("60610", address.getZip());
            out.write(person);
            
            person = (Person) in.read();
            assertEquals("Mary", person.getFirstName());
            address = person.getAddress();
            assertNotNull(address);
            assertNull(address.getState());
            assertEquals("", address.getZip());
            address.setZip(null);
            out.write(person);
            
            out.flush();
            assertEquals(load("b4_in.xml"), s.toString());
        }
        finally {
            in.close();
        }
    }
}
