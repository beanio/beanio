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
package org.beanio.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.beanio.internal.util.TypeHandlerFactory;
import org.beanio.internal.util.TypeUtil;
import org.junit.Test;

/**
 * JUnit test cases for the <tt>DateTypeHandler</tt> class.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DateTypeHandlerTest {

    @Test
    public void testLenient() throws TypeConversionException {
        DateTypeHandler handler = new DateTypeHandler();
        handler.setLenient(true);
        assertTrue(handler.isLenient());
        
        String pattern = "MM-dd-yyyy";
        handler.setPattern(pattern);
        assertEquals(pattern, handler.getPattern());
        
        Date date = handler.parse("01-32-2000");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        assertEquals(1, cal.get(Calendar.MONTH));
        assertEquals(1, cal.get(Calendar.DATE));
        assertEquals(2000, cal.get(Calendar.YEAR));
    }
    
    @Test(expected=TypeConversionException.class)
    public void testParsePositionPastDate() throws TypeConversionException {
        DateTypeHandler handler = new DateTypeHandler();
        handler.setLenient(false);
        handler.setPattern("MM-dd-yyyy");
        handler.parse("01-01-2000abc");
    }
    
    @Test(expected=TypeConversionException.class)
    public void testParsePosition() throws TypeConversionException {
        DateTypeHandler handler = new DateTypeHandler();
        handler.setLenient(false);
        handler.setPattern("MM-dd-yyyy");
        handler.parse("01-32-2000");
    }
    
    @Test
    public void testNewInstance() {
        DateTypeHandler handler = new DateTypeHandler();
        handler.setLenient(true);
        
        Properties props = new Properties();
        assertEquals(handler, handler.newInstance(props));
        props.setProperty(ConfigurableTypeHandler.FORMAT_SETTING, "");
        assertEquals(handler, handler.newInstance(props));
        
        props.setProperty(ConfigurableTypeHandler.FORMAT_SETTING, "yyyy-MM-dd");
        DateTypeHandler handler2 = (DateTypeHandler) handler.newInstance(props);
        assertEquals("yyyy-MM-dd", handler2.getPattern());
        assertEquals(handler.isLenient(), handler2.isLenient());
        
        handler.setPattern("yyyy-MM-dd");
        assertEquals(handler, handler.newInstance(props));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testInvalidPattern() {
        DateTypeHandler handler = new DateTypeHandler();
        handler.setPattern("xxx");
    }

    @Test
    public void testThreadSafety() throws InterruptedException {
        // Given
        int numThreads = 20;
        final int iterations = 100_000;

        Properties properties = new Properties();
        properties.setProperty(ConfigurableTypeHandler.FORMAT_SETTING, "yyyy-MM-dd");

        final DateTypeHandlerSupport dateTypeHandler = ((DateTypeHandler) TypeHandlerFactory.getDefault()
            .getTypeHandlerFor(TypeUtil.DATE_ALIAS))
            .newInstance(properties);

        final Queue<String> convertedDates = new ConcurrentLinkedQueue<>();
        final List<Throwable> exceptions = new CopyOnWriteArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(numThreads);

        // When
        for (int i = 0; i < numThreads; i++) {
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < iterations; j++) {
                        try {
                            long randomDate = ThreadLocalRandom.current().nextLong(0, System.currentTimeMillis());
                            convertedDates.add(dateTypeHandler.format(new Date(randomDate)));
                        } catch (Throwable e) {
                            exceptions.add(e);
                        }
                    }
                }
            });
        }

        pool.shutdown();
        boolean wasOk = pool.awaitTermination(10, TimeUnit.SECONDS);

        // Then
        assertTrue(wasOk);
        assertEquals(0, exceptions.size());
        assertEquals(numThreads * iterations, convertedDates.size());
    }
}
