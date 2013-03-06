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
import org.junit.Test;
import org.springframework.batch.item.*;
import org.springframework.core.io.ClassPathResource;

/**
 * JUnit test cases for the {@link BeanIOFlatFileItemReader} class.
 * @author Kevin Seim
 * @since 1.2
 */
public class BeanIOFlatFileItemReaderTest {

    @Test
    public void testInputFileNotFound() throws Exception {
        BeanIOFlatFileItemReader<Object> reader = new BeanIOFlatFileItemReader<Object>();
        reader.setStrict(false);
        reader.setStreamName("stream1");
        reader.setStreamMapping(new ClassPathResource("spring_mapping1.xml", getClass()));
        reader.setResource(new ClassPathResource("doesnotexist.txt", getClass()));
        reader.afterPropertiesSet();
        
        ExecutionContext ctx = new ExecutionContext();
        
        reader.open(ctx);
        assertNull(reader.read());
    }
    
    @Test(expected=ItemStreamException.class)
    public void testInputFileNotFoundAndStrict() throws Exception {
        BeanIOFlatFileItemReader<Object> reader = new BeanIOFlatFileItemReader<Object>();
        reader.setStreamName("stream1");
        reader.setStreamMapping(new ClassPathResource("spring_mapping1.xml", getClass()));
        reader.setResource(new ClassPathResource("doesnotexist.txt", getClass()));
        reader.afterPropertiesSet();
        reader.open(new ExecutionContext());
    }
    
    @Test(expected=IllegalStateException.class)
    public void testInvalidStreamName() throws Exception {
        BeanIOFlatFileItemReader<Object> reader = new BeanIOFlatFileItemReader<Object>();
        reader.setStreamName("xxx");
        reader.setStreamMapping(new ClassPathResource("spring_mapping1.xml", getClass()));
        reader.setResource(new ClassPathResource("doesnotexist.txt", getClass()));
        reader.afterPropertiesSet();
    }
}
