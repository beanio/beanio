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
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.beanio.BeanIOConfigurationException;
import org.beanio.config.delimited.DelimitedStreamDefinitionFactory;
import org.beanio.config.fixedlength.FixedLengthStreamDefinitionFactory;
import org.beanio.config.xml.XmlConfigurationLoader;
import org.beanio.parser.*;
import org.beanio.stream.RecordReaderFactory;
import org.beanio.types.*;

/**
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DefaultConfigurationFactory implements ConfigurationFactory {

	private ConfigurationLoader configurationLoader;
	private ConfigurationLoader defaultConfigurationLoader;
	
	/**
	 * 
	 */
	public DefaultConfigurationFactory() {
		defaultConfigurationLoader = createDefaultConfigurationLoader();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.beanio.config.ConfigurationFactory#createContext(java.io.InputStream)
	 */
	public Collection<StreamDefinition> loadDefinitions(InputStream in) throws IOException, BeanIOConfigurationException {
		ConfigurationLoader loader = configurationLoader;
		if (loader == null)
			loader = defaultConfigurationLoader;
		
		return loadDefinitions(loader.loadConfiguration(in));
	}

	/**
	 * 
	 * @param config
	 * @return
	 * @throws BeanIOConfigurationException
	 */
	protected Collection<StreamDefinition> loadDefinitions(BeanIOConfig config) throws BeanIOConfigurationException {
		if (config == null) {
			throw new BeanIOConfigurationException("null configuration");
		}
		
		TypeHandlerFactory globalTypeHandlerFactory = getTypeHandlerFactory(
			TypeHandlerFactory.getDefault(), config.getHandlerList());
		
		Collection<StreamConfig> streamConfigList = config.getStreamList();
		Collection<StreamDefinition> streamContextList = new ArrayList<StreamDefinition>(streamConfigList.size());
		
		for (StreamConfig streamConfig : streamConfigList) {
			TypeHandlerFactory typeHandlerFactory = getTypeHandlerFactory(
				globalTypeHandlerFactory, streamConfig.getHandlerList());
			
			StreamDefinitionFactory factory = createStreamDefinitionFactory(streamConfig.getFormat());
			factory.setTypeHandlerFactory(typeHandlerFactory);
			streamContextList.add(factory.compileStreamDefinition(streamConfig));
		}
		return streamContextList;
	}

	protected ConfigurationLoader createDefaultConfigurationLoader() {
		return new XmlConfigurationLoader();
	}
	
	/**
	 * 
	 * @param format
	 * @return
	 */
	protected StreamDefinitionFactory createStreamDefinitionFactory(String format) {
		if ("delimited".equals(format)) {
			return new DelimitedStreamDefinitionFactory();
		}
		else if ("fixedlength".equals(format)) {
			return new FixedLengthStreamDefinitionFactory();
		}
		else {
			throw new BeanIOConfigurationException("Stream format not supported: " + format);
		}
	}
	
	/**
	 * 
	 * @param streamName
	 * @param config
	 * @return
	 */
	protected RecordReaderFactory createRecordReaderFactory(String streamName, Bean config) {
		Object bean;
		try {
			bean = createBean(config);
		}
		catch (BeanIOConfigurationException ex) {
			throw new BeanIOConfigurationException(
				"Failed to create reader for stream '" + streamName + "'", ex);
		}
		
		// validate the configured class is assignable to the target class
		if (!TypeHandler.class.isAssignableFrom(bean.getClass())) {
			throw new BeanIOConfigurationException("Reader class '" + config.getClassName() + 
				"' does not implement the RecordReaderFactory interface");
		}
		
		return (RecordReaderFactory) bean;
	}
	
	/**
	 * 
	 * @param parent
	 * @param configList
	 * @return
	 */
	protected TypeHandlerFactory getTypeHandlerFactory(TypeHandlerFactory parent, List<TypeHandlerConfig> configList) {
		if (configList == null || configList.isEmpty()) {
			return parent;
		}
		
		TypeHandlerFactory handlerFactory = new TypeHandlerFactory(parent);
		
		// parse global type handlers
		for (TypeHandlerConfig hc : configList) {
			if (hc.getName() == null && hc.getType() == null)
				throw new BeanIOConfigurationException("Type handler must specify either 'type' or 'name'");
			
			Object bean;
			try {
				bean = createBean(hc);
			}
			catch (BeanIOConfigurationException ex) {
				if (hc.getName() != null) {
					throw new BeanIOConfigurationException(
						"Failed to load type handler named '" + hc.getName() + "'", ex);
				}
				else {
					throw new BeanIOConfigurationException(
						"Failed to load type handler for type '" + hc.getType() + "'", ex);
				}
			}
			
			// validate the configured class is assignable to the target class
			if (!TypeHandler.class.isAssignableFrom(bean.getClass())) {
				throw new BeanIOConfigurationException("Type handler class '" + hc.getClassName() + 
					"' does not implement TypeHandler interface");
			}
			
			TypeHandler h = (TypeHandler) bean;
			if (hc.getName() != null) {
				handlerFactory.registerHandler(hc.getName(), h);
			}
			
			if (hc.getType() != null) {
				Class<?> clazz = TypeHandlerFactory.toType(hc.getType());
				if (clazz == null) {
					throw new BeanIOConfigurationException("Invalid type handler type '" + hc.getType() + "'");
				}
				handlerFactory.addHandler(clazz, h);
			}
		}
		return handlerFactory;
	}
	
	/**
	 * 
	 * @param handlerFactory
	 * @param config
	 * @return
	 */
	protected Object createBean(Bean config) {
		TypeHandlerFactory handlerFactory = TypeHandlerFactory.getDefault();
		
		if (config.getClassName() == null)
			throw new BeanIOConfigurationException("Class name not set");
		
		// load the class
		Class<?> clazz = null;
		try {
			clazz = Class.forName(config.getClassName());
		} 
		catch (ClassNotFoundException e) {
			throw new BeanIOConfigurationException("Class not found '" + config.getClassName() + "'", e);
		}

		// instantiate an instance of the class
		Object bean = null;
		try {
			bean = clazz.newInstance();
		} 
		catch (InstantiationException e) {
			throw new BeanIOConfigurationException("Cound not instantiate class '" + clazz + "'", e);
		} 
		catch (IllegalAccessException e) {
			throw new BeanIOConfigurationException("Cound not instantiate class '" + clazz + "'", e);
		}

		// if no properties, we're done...
		Properties props = config.getProperties();
		if (props == null || props.isEmpty()) {
			return bean;
		}
		
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
			TypeHandler propertyTypeHandler = handlerFactory.getTypeHandler(propertyClass);
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
		
		return bean;
	}
	

	public ConfigurationLoader getConfigurationLoader() {
		return configurationLoader;
	}

	public void setConfigurationLoader(ConfigurationLoader configurationLoader) {
		this.configurationLoader = configurationLoader;
	}
}
