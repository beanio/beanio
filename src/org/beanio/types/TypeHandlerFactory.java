/*
 * Copyright 2010 Kevin Seim
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
	private Map<String,TypeHandler> handlerMap = new HashMap<String,TypeHandler>();
	
	/* The default type handler factory */
	private final static TypeHandlerFactory defaultFactory;
	static {
		defaultFactory = new TypeHandlerFactory(null);
		
		TypeHandler h;
		
		h = new CharacterTypeHandler();
		defaultFactory.addHandler(char.class, h);
		defaultFactory.addHandler(Character.class, h);	
		h = new StringTypeHandler();
		defaultFactory.addHandler(String.class, h);

		h = new ByteTypeHandler();
		defaultFactory.addHandler(byte.class, h);
		defaultFactory.addHandler(Byte.class, h);			
		h = new ShortTypeHandler();
		defaultFactory.addHandler(short.class, h);
		defaultFactory.addHandler(Short.class, h);		
		h = new IntegerTypeHandler();
		defaultFactory.addHandler(int.class, h);
		defaultFactory.addHandler(Integer.class, h);
		h = new LongTypeHandler();
		defaultFactory.addHandler(long.class, h);
		defaultFactory.addHandler(Long.class, h);
		h = new FloatTypeHandler();
		defaultFactory.addHandler(float.class, h);
		defaultFactory.addHandler(Float.class, h);
		h = new DoubleTypeHandler();
		defaultFactory.addHandler(double.class, h);
		defaultFactory.addHandler(Double.class, h);	
		h = new BigDecimalTypeHandler();
		defaultFactory.addHandler(BigDecimal.class, h);	
		h = new BigIntegerTypeHandler();
		defaultFactory.addHandler(BigInteger.class, h);
		
		h = new BooleanTypeHandler();
		defaultFactory.addHandler(boolean.class, h);
		defaultFactory.addHandler(Boolean.class, h);	
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
		TypeHandler handler = null;
		TypeHandlerFactory factory = this;
		while (factory != null) {
			handler = factory.handlerMap.get(name);
			if (handler != null) {
				return handler;
			}
			factory = factory.parent;
		}
		return null;
	}
	
	/**
	 * Returns a type handler for a given class, or null if there is no type handler 
	 * configured for the class name in this factory or any of its ancestors
	 * @param clazz the target class to find a type handler for
	 * @return the type handler, or null if the class is not supported
	 */
	public TypeHandler getTypeHandler(Class<?> clazz) {
		return getTypeHandler(clazz.getName());
	}
	
	/**
	 * Registers a type handler in this factory.
	 * @param name the name to register the type handler under
	 * @param handler the type handler to register
	 */
	public void registerHandler(String name, TypeHandler handler) {
		handlerMap.put(name, handler);
	}
	
	/**
	 * Registers a type handler in this factory.
	 * @param clazz the target class to register the type handler under
	 * @param handler the type handler to register
	 */
	public void addHandler(Class<?> clazz, TypeHandler handler) {
		handlerMap.put(clazz.getName(), handler);
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
		if ("string".equals(type))
			return String.class;
		else if ("boolean".equals(type))
			return boolean.class;
		else if ("Boolean".equals(type))
			return Boolean.class;
		else if ("byte".equals(type))
			return byte.class;
		else if ("Byte".equals(type))
			return Byte.class;
		else if ("char".equalsIgnoreCase(type))
			return char.class;
		else if ("Character".equalsIgnoreCase(type))
			return Character.class;
		else if ("short".equalsIgnoreCase(type))
			return short.class;
		else if ("Short".equalsIgnoreCase(type))
			return Short.class;		
		else if ("int".equals(type))
			return int.class;
		else if ("Integer".equals(type))
			return Integer.class;
		else if ("long".equals(type))
			return long.class;
		else if ("Long".equals(type))
			return Long.class;
		else if ("float".equalsIgnoreCase(type))
			return float.class;
		else if ("Float".equalsIgnoreCase(type))
			return Float.class;		
		else if ("double".equalsIgnoreCase(type))
			return double.class;
		else if ("Double".equalsIgnoreCase(type))
			return Double.class;
		else if ("bigdecimal".equalsIgnoreCase(type))
			return BigDecimal.class;
		else if ("biginteger".equalsIgnoreCase(type))
			return BigInteger.class;

		try {
			return Class.forName(type);
		} 
		catch (ClassNotFoundException e) {
			return null;
		}
	}
}
