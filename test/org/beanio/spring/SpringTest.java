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
package org.beanio.spring;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

import org.beanio.StreamFactory;
import org.beanio.internal.util.IOUtil;
import org.junit.*;
import org.springframework.batch.item.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.*;

/**
 * JUnit test cases for Spring-batch integration.
 * @author Kevin Seim
 * @since 1.2
 */
public class SpringTest {
    
    private ApplicationContext context;

    @Before
    public void setUp() throws Exception {
        context = new ClassPathXmlApplicationContext("org/beanio/spring/application_context.xml");
    }
    
    /**
     * Test stream factory configuration.
     */
    @Test
    public void testStreamFactory() {
        // verify a stream factory is successfully created
        StreamFactory streamFactory = (StreamFactory) context.getBean("streamFactory");
        assertNotNull(streamFactory);
        
        // verify multiple mapping files were loaded
        assertTrue(streamFactory.isMapped("stream1"));
        assertTrue(streamFactory.isMapped("stream2"));
        
        // verify stream factory is a singleton
        assertSame(streamFactory, context.getBean("streamFactory"));
    }
    
    /**
     * Test an empty stream factory configuration.
     */
    @Test
    public void testEmptyStreamFactory() {
        // verify a stream factory is successfully created
        StreamFactory streamFactory = (StreamFactory) context.getBean("streamFactory-empty");
        assertNotNull(streamFactory);
    }
    
    /**
     * Test standalone flat file reader configuration.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testItemReader() throws Exception {
        ItemStreamReader<Map<String,Object>> reader = (ItemStreamReader<Map<String,Object>>) context.getBean("itemReader-standalone");
        assertNotNull(reader);
        
        try {
            reader.open(new ExecutionContext());
            
            Map<String,Object> map = reader.read();
            assertNotNull(map);
            assertEquals(new Integer(1), map.get("id"));
            assertEquals("John", map.get("name"));
            
        }
        finally {
            reader.close();
        }
    }
    
    /**
     * Test shared stream factory configuration for flat file reader.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testItemReaderWithSharedStreamFactory() throws Exception {
        ItemStreamReader<Map<String,Object>> reader = (ItemStreamReader<Map<String,Object>>) context.getBean("itemReader-sharedFactory");
        assertNotNull(reader);
        
        try {
            reader.open(new ExecutionContext());
        }
        finally {
            reader.close();
        }
    }
    
    /*
     * Test BeanIO flat file reader restart.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testRestartItemReader() throws Exception {
        ItemStreamReader<Map<String,Object>> reader = (ItemStreamReader<Map<String,Object>>) context.getBean("itemReader-restart");
        assertNotNull(reader);
        
        try {
            ExecutionContext executionContext = new ExecutionContext();
            executionContext.put("BeanIOFlatFileItemReader.read.count", new Integer(2));
            
            reader.open(executionContext);
            
            Map<String,Object> map = reader.read();
            assertNotNull(map);
            assertEquals(new Integer(3), map.get("id"));
            assertEquals("Joe", map.get("name"));
        }
        finally {
            reader.close();
        }
    }
    
    /**
     * Test BeanIO flat file writer.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testItemWriter() throws Exception {
        ExecutionContext ec = new ExecutionContext();
        
        File tempFile = File.createTempFile("beanio-", "xml");
        tempFile.deleteOnExit();
        
        BeanIOFlatFileItemWriter<Map<String,Object>> writer = (BeanIOFlatFileItemWriter<Map<String,Object>>) 
            context.getBean("itemWriter-standalone");
        writer.setResource(new FileSystemResource(tempFile));
        assertNotNull(writer);
        writer.open(ec);
        
        Map<String,Object> record = new HashMap<String,Object>();
        record.put("id", 1);
        record.put("name", "John");
        
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        list.add(record);
        writer.write(list);
        writer.update(ec);
        
        long position = ec.getLong("BeanIOFlatFileItemWriter.current.count");
        assertTrue(position > 0);
        
        writer.close();
        assertFileMatches("out1.txt", tempFile);
        
        // test appendAllowed = true, and saveState = false
        writer = (BeanIOFlatFileItemWriter<Map<String,Object>>) context.getBean("itemWriter-append");
        writer.setResource(new FileSystemResource(tempFile));
        assertNotNull(writer);
        writer.open(ec);
        
        record.put("id", 2);
        record.put("name", "Joe");
        writer.write(list);
        writer.update(ec);
        assertEquals(position, ec.getLong("BeanIOFlatFileItemWriter.current.count"));
        
        writer.close();
        assertFileMatches("out2.txt", tempFile);
        
        // test restart
        writer = (BeanIOFlatFileItemWriter<Map<String,Object>>) context.getBean("itemWriter-standalone");
        writer.setResource(new FileSystemResource(tempFile));
        assertNotNull(writer);
        writer.open(ec);
        record.put("id", 3);
        record.put("name", "Kevin");
        writer.write(list);
        writer.update(ec);
        assertTrue(ec.getLong("BeanIOFlatFileItemWriter.current.count") > position);
        
        writer.close();
        assertFileMatches("out3.txt", tempFile);
    }
    
    /**
     * Test BeanIO flat file writer for XML.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testRestarbleXmlItemWriter() throws Exception {
        ExecutionContext ec = new ExecutionContext();
        
        File tempFile = File.createTempFile("beanio-", "xml");
        tempFile.deleteOnExit();
        
        BeanIOFlatFileItemWriter<Human> writer = (BeanIOFlatFileItemWriter<Human>) context.getBean("itemWriter-xml");
        writer.setResource(new FileSystemResource(tempFile));
        writer.open(ec);
        
        List<Human> list = new ArrayList<Human>();
        list.add(new Human(Human.FRIEND, "John", 'M'));
        writer.write(list);
        writer.update(ec);
        
        long position = ec.getLong("BeanIOFlatFileItemWriter.current.count");
        assertTrue(position > 0);
        
        list.clear();
        list.add(new Human(Human.COWORKER, "Mike", 'M'));
        list.add(new Human(Human.NEIGHBOR, "Steve", 'M'));
        writer.write(list);
        writer.close();
        assertFileMatches("xout1.xml", tempFile);
        
        // open for restart
        writer = (BeanIOFlatFileItemWriter<Human>) context.getBean("itemWriter-xml");
        writer.setResource(new FileSystemResource(tempFile));
        writer.open(ec);
        
        list.clear();
        list.add(new Human(Human.FRIEND, "Jen", 'F'));
        writer.write(list);
        
        writer.update(ec);
        writer.close();
        assertFileMatches("xout2.xml", tempFile);
    }
    
    
    private void assertFileMatches(String expected, File actual) throws IOException {
        BufferedReader expectedReader = new BufferedReader(new FileReader(getClass().getResource(expected).getFile()));
        BufferedReader actualReader = new BufferedReader(new FileReader(actual));
        int lineNumber = 0;
        String actualLine = null;
        String expectedLine = null;
        try {

            while ((expectedLine = expectedReader.readLine()) != null) {
                ++lineNumber;
                actualLine = actualReader.readLine();
                assertEquals(expectedLine, actualLine);
            }
            
            ++lineNumber;
            actualLine = actualReader.readLine();
            if (actualLine != null) {
                fail("Expected EOF at line " + lineNumber);
            }
        }
        catch (ComparisonFailure ex) {
            throw new ComparisonFailure("Comparison failed at line " + lineNumber + ", ", expectedLine, actualLine);
        }
        finally {
            IOUtil.closeQuietly(expectedReader);
            IOUtil.closeQuietly(actualReader);
        }
    }
}
