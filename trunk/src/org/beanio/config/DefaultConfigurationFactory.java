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
package org.beanio.config;

import java.io.*;
import java.util.*;

import org.beanio.BeanIOConfigurationException;
import org.beanio.config.xml.XmlConfigurationLoader;
import org.beanio.parser.*;
import org.beanio.types.*;
import org.beanio.util.Settings;

/**
 * Default configuration factory implementation.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DefaultConfigurationFactory implements ConfigurationFactory {

    private ConfigurationLoader configurationLoader;
    private ConfigurationLoader defaultConfigurationLoader;

    /**
     * Constructs a new <tt>DefaultConfigurationFactory</tt>.
     */
    public DefaultConfigurationFactory() {
        defaultConfigurationLoader =  new XmlConfigurationLoader();
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.config.ConfigurationFactory#createContext(java.io.InputStream)
     */
    public Collection<StreamDefinition> loadDefinitions(InputStream in) throws IOException,
        BeanIOConfigurationException {
        ConfigurationLoader loader = configurationLoader;
        if (loader == null) {
            loader = getDefaultConfigurationLoader();
        }
        
        return createStreamDefinitions(loader.loadConfiguration(in));
    }
    
    /**
     * Returns the default mapping configuration loader implementation.
     * @return the default mapping configuration
     */
    protected ConfigurationLoader getDefaultConfigurationLoader() {
        return defaultConfigurationLoader;
    }

    /**
     * Creates stream definitions from a BeanIO stream mapping configuration.
     * @param config the BeanIO stream mapping configuration
     * @return the collection of stream definitions
     * @throws BeanIOConfigurationException if a configuration setting is invalid
     */
    protected Collection<StreamDefinition> createStreamDefinitions(BeanIOConfig config)
        throws BeanIOConfigurationException {
        if (config == null) {
            throw new BeanIOConfigurationException("null configuration");
        }

        TypeHandlerFactory globalTypeHandlerFactory = getTypeHandlerFactory(
            TypeHandlerFactory.getDefault(), config.getTypeHandlerList());

        Collection<StreamConfig> streamConfigList = config.getStreamList();
        Collection<StreamDefinition> streamContextList = new ArrayList<StreamDefinition>(
            streamConfigList.size());

        for (StreamConfig streamConfig : streamConfigList) {
            TypeHandlerFactory typeHandlerFactory = getTypeHandlerFactory(
                globalTypeHandlerFactory, streamConfig.getHandlerList());

            StreamDefinitionFactory factory = createStreamDefinitionFactory(streamConfig
                .getFormat());
            factory.setTypeHandlerFactory(typeHandlerFactory);
            streamContextList.add(factory.compileStreamDefinition(streamConfig));
        }
        return streamContextList;
    }

    /**
     * Instantiates the factory implementation to create the stream definition.
     * @param format the stream format
     * @return the stream definition factory
     */
    protected StreamDefinitionFactory createStreamDefinitionFactory(String format) {
        String clazz = Settings.getInstance().getProperty(
            "org.beanio." + format + ".streamDefinitionFactory");

        if (clazz == null) {
            throw new BeanIOConfigurationException("A stream definition factory " +
                " is not configured for format '" + format + "'");
        }

        return (StreamDefinitionFactory) BeanUtil.createBean(clazz);
    }

    /**
     * Creates a type handler factory.
     * @param parent the parent type handler factory
     * @param configList the list of customized type handler configurations
     * @return the type handler factory
     */
    protected TypeHandlerFactory getTypeHandlerFactory(TypeHandlerFactory parent,
        List<TypeHandlerConfig> configList) {
        if (configList == null || configList.isEmpty()) {
            return parent;
        }

        TypeHandlerFactory handlerFactory = new TypeHandlerFactory(parent);

        // parse global type handlers
        for (TypeHandlerConfig hc : configList) {
            if (hc.getName() == null && hc.getType() == null)
                throw new BeanIOConfigurationException(
                    "Type handler must specify either 'type' or 'name'");

            Object bean;
            try {
                bean = BeanUtil.createBean(hc.getClassName(), hc.getProperties());
            }
            catch (BeanIOConfigurationException ex) {
                if (hc.getName() != null) {
                    throw new BeanIOConfigurationException(
                        "Failed to create type handler named '" + hc.getName() + "'", ex);
                }
                else {
                    throw new BeanIOConfigurationException(
                        "Failed to create type handler for type '" + hc.getType() + "'", ex);
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
                    throw new BeanIOConfigurationException("Invalid type handler type '"
                        + hc.getType() + "'");
                }
                handlerFactory.registerHandler(clazz, h);
            }
        }
        return handlerFactory;
    }

    /**
     * Returns the mapping configuration loader.
     * @return the mapping configuration loader 
     */
    public ConfigurationLoader getConfigurationLoader() {
        return configurationLoader;
    }

    /**
     * Sets the mapping configuration loader.
     * @param configurationLoader the mapping configuration loader
     */
    public void setConfigurationLoader(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }
}
