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
package org.beanio.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.*;
import java.util.*;

import org.junit.Test;

/**
 * JUnit test cases for the <tt>TypeUtil</tt> class.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class TypeUtilTest {

    @Test
    public void testToType() {
        assertEquals(String.class, TypeUtil.toType("string"));
        assertEquals(Boolean.class, TypeUtil.toType("boolean"));
        assertEquals(Byte.class, TypeUtil.toType("byte"));
        assertEquals(Character.class, TypeUtil.toType("char"));
        assertEquals(Character.class, TypeUtil.toType("character"));
        assertEquals(Short.class, TypeUtil.toType("short"));
        assertEquals(Integer.class, TypeUtil.toType("int"));
        assertEquals(Integer.class, TypeUtil.toType("integer"));
        assertEquals(Long.class, TypeUtil.toType("long"));
        assertEquals(Float.class, TypeUtil.toType("float"));
        assertEquals(Double.class, TypeUtil.toType("double"));
        assertEquals(BigDecimal.class, TypeUtil.toType("BigDecimal"));
        assertEquals(BigDecimal.class, TypeUtil.toType("decimal"));
        assertEquals(BigInteger.class, TypeUtil.toType("BigInteger"));
        assertEquals(Date.class, TypeUtil.toType("date"));
        assertEquals(Date.class, TypeUtil.toType("time"));
        assertEquals(Date.class, TypeUtil.toType("datetime"));
        assertEquals(getClass(), TypeUtil.toType("org.beanio.util.TypeUtilTest"));
        assertNull(TypeUtil.toType("java.util.List"));
        assertNull(TypeUtil.toType("java.util.AbstractList"));
    }
    
    @Test
    public void testToTypeClassNotFound() {
        assertNull(TypeUtil.toType("org.beanio.types.NoClass"));
    }
    
    @Test
    public void testToCollection() {
        assertEquals(ArrayList.class, TypeUtil.toCollectionType("list"));
        assertEquals(HashSet.class, TypeUtil.toCollectionType("set"));
        assertEquals(TypeUtil.ARRAY_TYPE, TypeUtil.toCollectionType("array"));
        assertEquals(ArrayList.class, TypeUtil.toCollectionType("java.util.ArrayList"));
        assertNull(TypeUtil.toCollectionType("java.util.List"));
        assertNull(TypeUtil.toCollectionType("java.util.AbstractList"));
        assertNull(TypeUtil.toCollectionType("java.util.HashMap"));
        assertNull(TypeUtil.toCollectionType("org.beanio.types.NoClass"));
    }
}
