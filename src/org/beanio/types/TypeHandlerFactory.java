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
import java.util.*;

/**
 * A <tt>TypeHandlerFactory</tt> is used to find a <tt>TypeHandler</tt> for 
 * converting field text to a field value object.
 * <p>
 * A <tt>TypeHandler</tt> can be retrieved by the target class, or by a configured handler 
 * name.  Note that the same namespace is used for both so that registering a type handler 
 * under the name "<tt>java.lang.Integer</tt>" will replace the handler for the 
 * Integer class.
 * <p>
 * By default, a <tt>TypeHandlerFactory</tt> will hold a reference to a parent
 * factory.  If a factory cannot find a type handler, its parent will be checked
 * recursively until there is no parent to check.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class TypeHandlerFactory {

    private TypeHandlerFactory parent;
    private Map<String, TypeHandler> handlerMap = new HashMap<String, TypeHandler>();

    /* The default type handler factory */
    private final static TypeHandlerFactory defaultFactory;
    static {
        defaultFactory = new TypeHandlerFactory(null);

        TypeHandler h;

        h = new CharacterTypeHandler();
        defaultFactory.registerHandler(char.class, h);
        defaultFactory.registerHandler(Character.class, h);
        h = new StringTypeHandler();
        defaultFactory.registerHandler(String.class, h);

        h = new ByteTypeHandler();
        defaultFactory.registerHandler(byte.class, h);
        defaultFactory.registerHandler(Byte.class, h);
        h = new ShortTypeHandler();
        defaultFactory.registerHandler(short.class, h);
        defaultFactory.registerHandler(Short.class, h);
        h = new IntegerTypeHandler();
        defaultFactory.registerHandler(int.class, h);
        defaultFactory.registerHandler(Integer.class, h);
        h = new LongTypeHandler();
        defaultFactory.registerHandler(long.class, h);
        defaultFactory.registerHandler(Long.class, h);
        h = new FloatTypeHandler();
        defaultFactory.registerHandler(float.class, h);
        defaultFactory.registerHandler(Float.class, h);
        h = new DoubleTypeHandler();
        defaultFactory.registerHandler(double.class, h);
        defaultFactory.registerHandler(Double.class, h);
        h = new BigDecimalTypeHandler();
        defaultFactory.registerHandler(BigDecimal.class, h);
        h = new BigIntegerTypeHandler();
        defaultFactory.registerHandler(BigInteger.class, h);

        h = new BooleanTypeHandler();
        defaultFactory.registerHandler(boolean.class, h);
        defaultFactory.registerHandler(Boolean.class, h);
    }

    /**
     * Constructs a new <tt>TypeHandlerFactory</tt> using the default type handler factory
     * for its parent.
     */
    public TypeHandlerFactory() {
        parent = getDefault();
    }

    /**
     * Constructs a new <tt>TypeHandlerFactory</tt>.
     * @param parent the parent <tt>TypeHandlerFactory</tt>
     */
    public TypeHandlerFactory(TypeHandlerFactory parent) {
        this.parent = parent;
    }

    /**
     * Returns a named type handler, or null if there is no type handler configured
     * for the given name in this factory or any of its ancestors.
     * @param name the name of type handler
     * @return the type handler, or null if there is no configured type handler
     *    for the name
     */
    public TypeHandler getTypeHandler(String name) {
        return getHandler("name:" + name);
    }

    /**
     * Returns a type handler for a given class, or null if there is no type handler 
     * configured for the class name in this factory or any of its ancestors
     * @param clazz the target class to find a type handler for
     * @return the type handler, or null if the class is not supported
     */
    public TypeHandler getTypeHandler(Class<?> clazz) {
        return getHandler("class:" + clazz.getName());
    }

    private TypeHandler getHandler(String key) {
        TypeHandler handler = null;
        TypeHandlerFactory factory = this;
        while (factory != null) {
            handler = factory.handlerMap.get(key);
            if (handler != null) {
                return handler;
            }
            factory = factory.parent;
        }
        return null;
    }

    /**
     * Registers a type handler in this factory.
     * @param name the name to register the type handler under
     * @param handler the type handler to register
     */
    public void registerHandler(String name, TypeHandler handler) {
        handlerMap.put("name:" + name, handler);
    }

    /**
     * Registers a type handler in this factory.
     * @param clazz the target class to register the type handler under
     * @param handler the type handler to register
     */
    public void registerHandler(Class<?> clazz, TypeHandler handler) {
        handlerMap.put("class:" + clazz.getName(), handler);
    }

    /**
     * Returns the default <tt>TypeHandlerFactory</tt>.
     * @return the default <tt>TypeHandlerFactory</tt>
     */
    public static TypeHandlerFactory getDefault() {
        return defaultFactory;
    }

    /**
     * Converts a type or type alias to a class.  The following aliases are
     * supported:
     * <table>
     * <tr><th>Alias</th><th>Class or Primitive</th></tr>
     * <tr><td>string</td><td>java.lang.String</td></tr>
     * <tr><td>boolean</td><td>boolean</td></tr>
     * <tr><td>Booleann</td><td>java.lang.Boolean</td></tr>
     * <tr><td>byte</td><td>byte</td></tr>
     * <tr><td>Byte</td><td>java.lang.Byte</td></tr>
     * <tr><td>int</td><td>int</td></tr>
     * <tr><td>Integer</td><td>java.lang.Integer</td></tr>
     * <tr><td>short</td><td>short</td></tr>
     * <tr><td>short</td><td>java.lang.Short</td></tr>
     * <tr><td>char</td><td>char</td></tr>
     * <tr><td>Character</td><td>java.lang.Character</td></tr>
     * <tr><td>long</td><td>long</td></tr>
     * <tr><td>Long</td><td>java.lang.Long</td></tr>
     * <tr><td>float</td><td>float</td></tr>
     * <tr><td>Float</td><td>java.lang.Float</td></tr>
     * <tr><td>double</td><td>double</td></tr>
     * <tr><td>Double</td><td>java.lang.Double</td></tr>
     * <tr><td>bigdecimal</td><td>java.math.BigDecimal</td></tr>
     * <tr><td>biginteger</td><td>java.math.BigInteger</td></tr>
     * </table>
     * 
     * @param type the fully qualified class name or type alias
     * @return the class, or null if the type name is invalid
     */
    public static Class<?> toType(String type) {
        if ("String".equals(type))
            return String.class;
        else if ("boolean".equals(type))
            return boolean.class;
        else if ("Boolean".equals(type))
            return Boolean.class;
        else if ("byte".equals(type))
            return byte.class;
        else if ("Byte".equals(type))
            return Byte.class;
        else if ("char".equals(type))
            return char.class;
        else if ("Character".equals(type))
            return Character.class;
        else if ("short".equals(type))
            return short.class;
        else if ("Short".equals(type))
            return Short.class;
        else if ("int".equals(type))
            return int.class;
        else if ("Integer".equals(type))
            return Integer.class;
        else if ("long".equals(type))
            return long.class;
        else if ("Long".equals(type))
            return Long.class;
        else if ("float".equals(type))
            return float.class;
        else if ("Float".equals(type))
            return Float.class;
        else if ("double".equals(type))
            return double.class;
        else if ("Double".equals(type))
            return Double.class;
        else if ("BigDecimal".equals(type))
            return BigDecimal.class;
        else if ("BigInteger".equals(type))
            return BigInteger.class;

        try {
            return Class.forName(type);
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }
}
