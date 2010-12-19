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
package org.beanio.config;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

import org.beanio.BeanIOConfigurationException;
import org.beanio.types.*;

public class BeanUtil {

	private BeanUtil() { }
	
	/**
	 * 
	 * @param className
	 * @return
	 */
	public static Object createBean(String className) {
		if (className== null)
			throw new BeanIOConfigurationException("Class name not set");
		
		// load the class
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		} 
		catch (ClassNotFoundException e) {
			throw new BeanIOConfigurationException("Class not found '" + className + "'", e);
		}

		// instantiate an instance of the class
		try {
			return clazz.newInstance();
		} 
		catch (InstantiationException e) {
			throw new BeanIOConfigurationException("Cound not instantiate class '" + clazz + "'", e);
		} 
		catch (IllegalAccessException e) {
			throw new BeanIOConfigurationException("Cound not instantiate class '" + clazz + "'", e);
		}
	}
	
	/**
	 * 
	 * @param bean
	 * @param props
	 */
	public static void configure(Object bean, Properties props) {
		// if no properties, we're done...
		if (props == null || props.isEmpty()) {
			return;
		}
		
		Class<?> clazz = bean.getClass();
		
		BeanInfo info;
		try {
			info = Introspector.getBeanInfo(clazz);
		} catch (IntrospectionException e) {
			throw new BeanIOConfigurationException(e);
		}
		
		PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
		for (Map.Entry<Object,Object> entry : props.entrySet()) {
			
			String name = (String) entry.getKey();
			PropertyDescriptor descriptor = null;
			for (int i=0,j=descriptors.length; i<j; i++) {
				if (name.equals(descriptors[i].getName())) {
					descriptor = descriptors[i];
					break;
				}
			}
			if (descriptor == null) {
				throw new BeanIOConfigurationException(
					"Property '" + name + "' not found on class '" + clazz + "'");
			}
			
			Method method = descriptor.getWriteMethod();
			if (method == null) {
				throw new BeanIOConfigurationException(
					"Property '" + name + "' is not writeable on class '" + clazz + "'");
			}
	
			String valueText = (String) entry.getValue();
			
			Class<?> propertyClass = descriptor.getPropertyType();
			TypeHandler propertyTypeHandler = TypeHandlerFactory.getDefault().getTypeHandler(propertyClass);
			if (propertyTypeHandler == null) {
				throw new BeanIOConfigurationException("Type handler not found for property '" +
					name + "' of type '" + propertyClass + "' on class '" + clazz + "'");
			}
			
			try {
				Object value = propertyTypeHandler.parse(valueText);
				if (value != null || !propertyClass.isPrimitive()) {
					method.invoke(bean, new Object[] { value });
				}
			} 
			catch (TypeConversionException e) {
				throw new BeanIOConfigurationException("Type conversion failed for property '" +
					name + "' on class '" + clazz + "'", e);
			} 
			catch (IllegalArgumentException e) {
				throw new BeanIOConfigurationException("Failed to invoke '" + method +
					"' on class '" + clazz + "'", e);
			} 
			catch (InvocationTargetException e) {
				throw new BeanIOConfigurationException("Failed to invoke '" + method +
					"' on class '" + clazz + "'", e);
			}
			catch (IllegalAccessException e) {
				throw new BeanIOConfigurationException("Failed to invoke '" + method +
					"' on class '" + clazz + "'", e);
			}
		}
	}
	
	/**
	 * 
	 * @param handlerFactory
	 * @param config
	 * @return
	 */
	public static Object createBean(String className, Properties props) {
		Object bean = createBean(className);
		configure(bean, props);
		return bean;
	}
}
