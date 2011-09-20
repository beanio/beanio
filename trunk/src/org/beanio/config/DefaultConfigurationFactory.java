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
        
        Collection<BeanIOConfig> configList = loader.loadConfiguration(in);
        if (configList.isEmpty()) {
            return Collections.emptyList();
        }
        
        // check for duplicate stream names...
        HashSet<String> set = new HashSet<String>();
        for (BeanIOConfig config : configList) {
            for (StreamConfig streamConfig : config.getStreamList()) {
                if (!set.add(streamConfig.getName())) {
                    throw new BeanIOConfigurationException("Duplicate stream name '" + 
                        streamConfig.getName() + "'");
                }
            }
        }
        set = null;
        
        // create the stream definitions
        if (configList.size() == 1) {
            return createStreamDefinitions(configList.iterator().next());
        }
        else {
            List<StreamDefinition> list = new ArrayList<StreamDefinition>();
            for (BeanIOConfig config : configList) {
                list.addAll(createStreamDefinitions(config));
            }
            return list;
        }
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
        
        TypeHandlerFactoryManager manager = new TypeHandlerFactoryManager();

        // create global type handlers
        createTypeHandlers(manager, config.getTypeHandlerList());
        
        Collection<StreamConfig> streamConfigList = config.getStreamList();
        Collection<StreamDefinition> streamDefinitionList = new ArrayList<StreamDefinition>(
            streamConfigList.size());
        
        for (StreamConfig streamConfig : streamConfigList) {
            StreamDefinitionFactory factory = createStreamDefinitionFactory(streamConfig.getFormat());
            
            // allow a stream factory to override default type handlers...
            TypeHandlerFactory typeHandlerFactory = factory.getDefaultTypeHandlerFactory();
            if (typeHandlerFactory == null) {
                typeHandlerFactory = TypeHandlerFactory.getDefault();
            }
            
            // instruct the manager to create a new type handler factory for this stream
            typeHandlerFactory = manager.createStreamTypeHandlerFactory(typeHandlerFactory, streamConfig.getFormat());

            // create stream specific type handlers
            createTypeHandlers(manager, streamConfig.getHandlerList());
            factory.setTypeHandlerFactory(typeHandlerFactory);
            
            try {
                streamDefinitionList.add(factory.createStreamDefinition(streamConfig));
            }
            catch (BeanIOConfigurationException ex) {
                if (config.getSource() != null) {
                    throw new BeanIOConfigurationException("Invalid mapping file '" +
                        config.getSource() + "': " + ex.getMessage());
                }
                else {
                    throw ex;
                }
            }
            
            // clear out the stream specific type handler
            manager.clearStreamTypeHandlerFactory();
        }
        return streamDefinitionList;
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

        Object factory = BeanUtil.createBean(clazz);
        if (!StreamDefinitionFactory.class.isAssignableFrom(factory.getClass())) {
            throw new BeanIOConfigurationException("Configured stream definition factory '" +
                clazz + "' does not implement '" + StreamDefinitionFactory.class.getName() + "'");
        }
        
        return (StreamDefinitionFactory) factory;
    }

    /**
     * Creates type handlers and adds them to the type handler factory provided by the type handler
     * factory manager.
     * @param manager the type handler factory manager
     * @param configList the list of customized type handler configurations
     * @since 1.2
     */
    private void createTypeHandlers(TypeHandlerFactoryManager manager,
        List<TypeHandlerConfig> configList) {
        if (configList == null) {
            return;
        }

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
                // named type handlers are always registerred globally
                manager.getTypeHandlerFactory(null).registerHandler(hc.getName(), h);
            }

            if (hc.getType() != null) {
                try {
                    // type handlers configured for java types may be registered for a specific stream format
                    manager.getTypeHandlerFactory(hc.getFormat()).registerHandlerFor(hc.getType(), h);
                }
                catch (IllegalArgumentException ex) {
                    throw new BeanIOConfigurationException("Invalid type handler configuration", ex);
                }
            }
        }
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
