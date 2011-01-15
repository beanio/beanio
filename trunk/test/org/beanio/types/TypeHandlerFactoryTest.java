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

import java.math.*;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * JUnit test cases for the <tt>TypeHandlerFactory</tt> class.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class TypeHandlerFactoryTest {

    @Test
    public void testGetHandler() {
        TypeHandlerFactory factory = new TypeHandlerFactory();
        assertNull(factory.getTypeHandler(getClass()));
        assertNull(factory.getTypeHandler("notfound"));
    }
    
    @Test
    public void testToType() {
        assertEquals(String.class, TypeHandlerFactory.toType("String"));
        assertEquals(boolean.class, TypeHandlerFactory.toType("boolean"));
        assertEquals(Boolean.class, TypeHandlerFactory.toType("Boolean"));
        assertEquals(byte.class, TypeHandlerFactory.toType("byte"));
        assertEquals(Byte.class, TypeHandlerFactory.toType("Byte"));
        assertEquals(char.class, TypeHandlerFactory.toType("char"));
        assertEquals(Character.class, TypeHandlerFactory.toType("Character"));
        assertEquals(short.class, TypeHandlerFactory.toType("short"));
        assertEquals(Short.class, TypeHandlerFactory.toType("Short"));
        assertEquals(int.class, TypeHandlerFactory.toType("int"));
        assertEquals(Integer.class, TypeHandlerFactory.toType("Integer"));
        assertEquals(long.class, TypeHandlerFactory.toType("long"));
        assertEquals(Long.class, TypeHandlerFactory.toType("Long"));
        assertEquals(float.class, TypeHandlerFactory.toType("float"));
        assertEquals(Float.class, TypeHandlerFactory.toType("Float"));
        assertEquals(double.class, TypeHandlerFactory.toType("double"));
        assertEquals(Double.class, TypeHandlerFactory.toType("Double"));
        assertEquals(BigDecimal.class, TypeHandlerFactory.toType("BigDecimal"));
        assertEquals(BigInteger.class, TypeHandlerFactory.toType("BigInteger"));
        assertEquals(getClass(), TypeHandlerFactory.toType(
            "org.beanio.types.TypeHandlerFactoryTest"));
    }
    
    @Test
    public void testToTypeClassNotFound() {
        assertNull(TypeHandlerFactory.toType("org.beanio.types.NoClass"));
    }
}
