/*
 * Copyright 2012 Kevin Seim
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
package org.beanio.parser.multiline;

import static org.junit.Assert.*;

import java.io.*;
import java.text.*;
import java.util.*;

import org.beanio.*;
import org.beanio.beans.*;
import org.beanio.parser.ParserTest;
import org.junit.*;

/**
 * JUnit test cases for testing bean objects mapped to a group.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class MultilineRecordTest extends ParserTest {

    private StreamFactory factory;

    @Before
    public void setup() throws Exception {
        factory = newStreamFactory("multiline_mapping.xml");
    }

    //@Test
    public void testRecordGroup() throws ParseException {
        BeanReader in = factory.createReader("ml1", new InputStreamReader(
            getClass().getResourceAsStream("ml1.txt")));
        
        try {
            // read a valid multi-line record
            Order order = (Order) in.read();

            assertEquals(1, in.getLineNumber());
            assertEquals(4, in.getRecordCount());
            assertEquals("orderGroup", in.getRecordName());
            
            RecordContext ctx = in.getRecordContext(1);
            assertEquals(2, ctx.getLineNumber());
            assertEquals("customer", ctx.getRecordName());
            assertEquals("customer,George,Smith", ctx.getRecordText());
            
            assertEquals("100", order.getId());
            assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2012-01-01"), order.getDate());
            
            Person buyer = order.getCustomer();
            assertEquals("George", buyer.getFirstName());
            assertEquals("Smith", buyer.getLastName());
            
            OrderItem item = order.getItems().get(0);
            assertEquals("soda", item.getName());
            assertEquals(2, item.getQuantity());
            
            item = order.getItems().get(1);
            assertEquals("carrots", item.getName());
            assertEquals(5, item.getQuantity());            
            
            StringWriter text = new StringWriter();
            factory.createWriter("ml1", text).write(order);
            assertEquals(
                "order,100,2012-01-01\n" +
                "customer,George,Smith\n" +
                "item,soda,2\n" +
                "item,carrots,5\n", text.toString());
            
            order.setCustomer(null);
            order.setItems(null);
            text = new StringWriter();
            factory.createWriter("ml1", text).write(order);
            assertEquals(
                "order,100,2012-01-01\n" +
                "item,,\n", text.toString());
            
            // read an invalid multi-line record
            try {
                in.read();
                fail("Record expected to fail validation");
            }
            catch (InvalidRecordException ex) {
                assertEquals(5, in.getLineNumber());
                assertEquals(2, in.getRecordCount());
                assertEquals("orderGroup", in.getRecordName());

                ctx = ex.getRecordContext(1);
                assertTrue(ctx.hasFieldErrors());
                assertEquals(6, ctx.getLineNumber());
                assertEquals("item", ctx.getRecordName());
                assertEquals("a", ctx.getFieldText("quantity", 0));
            }
            
            // skip an invalid record
            assertEquals(2, in.skip(2));
            
            // read another valid record
            order = (Order) in.read();
            assertEquals(13, in.getLineNumber());
            assertEquals(3, in.getRecordCount());
            assertEquals("orderGroup", in.getRecordName());
            assertEquals("103", order.getId());
            assertNull(order.getCustomer());
        }
        finally {
            in.close();
        }
    }
    
    @Test
    public void testNestedRecorGroup() throws ParseException {
        BeanReader in = factory.createReader("ml2", new InputStreamReader(
            getClass().getResourceAsStream("ml2.txt")));
        
        try {
            // read batch #1
            OrderBatch batch = (OrderBatch) in.read();
            assertEquals(2, batch.getBatchCount());
            
            List<Order> orderList = batch.getOrders();
            assertEquals(2, orderList.size());
            
            Order order = orderList.get(0);
            assertEquals("100", order.getId());
            Person customer = order.getCustomer();
            assertEquals("George", customer.getFirstName());
            assertEquals("Smith", customer.getLastName());
            
            order = orderList.get(1);
            assertEquals("101", order.getId());
            customer = order.getCustomer();
            assertEquals("Joe", customer.getFirstName());
            assertEquals("Johnson", customer.getLastName());
            
            // read batch #2
            batch = (OrderBatch) in.read();
            assertEquals(1, batch.getBatchCount());
            
            orderList = batch.getOrders();
            assertEquals(1, orderList.size());
            
            order = orderList.get(0);
            assertEquals("103", order.getId());
        }
        finally {
            in.close();
        }
    }
    
    @Test
    @SuppressWarnings("rawtypes")
    public void testNestedRecorGroupCollections() throws ParseException {
        BeanReader in = factory.createReader("ml3", new InputStreamReader(
            getClass().getResourceAsStream("ml3.txt")));
        
        try {
            // read batch #1
            Map map = (Map) in.read();
            List list = (List) map.get("batch");
            assertEquals(2, list.size());
            
            OrderBatch batch = (OrderBatch) list.get(0);
            assertEquals(2, batch.getBatchCount());
            List<Order> orderList = batch.getOrders();
            assertEquals(2, orderList.size());
            assertEquals("100", orderList.get(0).getId());
            assertEquals("101", orderList.get(1).getId());
            
            batch = (OrderBatch) list.get(1);
            assertEquals(1, batch.getBatchCount());
            orderList = batch.getOrders();
            assertEquals(1, orderList.size());
            assertEquals("103", orderList.get(0).getId());
            
            StringWriter text = new StringWriter();
            factory.createWriter("ml3", text).write(map);
            assertEquals(
                "header,2\n" +
                "order,100,2012-01-01\n" +
                "customer,George,Smith\n" +
                "order,101,2012-01-01\n" +
                "customer,John,Smith\n" +
                "header,1\n" +
                "order,103,2012-01-01\n" +
                "customer,Jen,Smith\n", text.toString());
        }
        finally {
            in.close();
        }
    }
}
