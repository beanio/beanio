/*
 * Copyright 2011 Kevin Seim
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

import java.util.*;

import org.beanio.BeanIOConfigurationException;
import org.beanio.types.TypeHandlerFactory;

/**
 * A type handler factory manager is used manage the following hierarchy of type
 * handler factories (in order of priority):
 * 
 * <ol>
 * <li>The stream specific type handler factory</li>
 * <li>The format specific global type handler factory</li> 
 * <li>The global type handler factory</li>
 * <li>The format specific default type handler factory</li>
 * <li>The default type handler factory</li>
 * </ol>
 * 
 * @author Kevin Seim
 * @since 1.2
 */
class TypeHandlerFactoryManager {

    /* global type handler factories */
    private TypeHandlerFactory globalTypeHandlerFactory = new TypeHandlerFactory(null);
    private Map<String,TypeHandlerFactory> globalTypeHandlerFactoryByFormat = new HashMap<String,TypeHandlerFactory>();
    
    /* stream specific type handler factory */
    private TypeHandlerFactory streamTypeHandlerFactory = null;
    private String streamFormat = null;
    
    /**
     * Constructs a new <tt>TypeHandlerFactoryManager</tt>.
     */
    public TypeHandlerFactoryManager() { }
    
    /**
     * Returns a type hander factory for the given format.
     * @param format the stream format
     * @return the type handler factory for the format
     */
    public TypeHandlerFactory getTypeHandlerFactory(String format) {
        if (streamTypeHandlerFactory != null) {
            if (format != null && !streamFormat.equals(format)) {
                throw new BeanIOConfigurationException("Type handler format does not match stream format");
            }
            return streamTypeHandlerFactory;
        }
        else {
            return getGlobalTypeHandlerFactory(format);
        }
    }
    
    /**
     * Creates a stream specific type handler factory.  Once this method is called,
     * {@link #getTypeHandlerFactory(String)} will return the newly created factory
     * until {@link #clearStreamTypeHandlerFactory()} is called.
     * @param parent the parent default type handler factory
     * @param format the stream format
     * @return the newly created type handler factory
     */
    public TypeHandlerFactory createStreamTypeHandlerFactory(TypeHandlerFactory parent, String format) {
        globalTypeHandlerFactory.setParent(parent);
        
        TypeHandlerFactory f = globalTypeHandlerFactoryByFormat.get(format);
        if (f == null) {
            f = globalTypeHandlerFactory;
        }
        
        streamFormat = format;
        streamTypeHandlerFactory = new TypeHandlerFactory(f);
        
        return streamTypeHandlerFactory;
    }
    
    /**
     * Clears the stream type factory last created.
     */
    public void clearStreamTypeHandlerFactory() {
        streamTypeHandlerFactory = null;
        streamFormat = null;
    }
    
    /**
     * Returns the global type handler factory for the given stream format.
     * @param format the stream format
     * @return the global type handler factory for the given stream format
     */
    public TypeHandlerFactory getGlobalTypeHandlerFactory(String format) {
        if (format == null) {
            return getGlobalTypeHandlerFactory();
        }
        else {
            TypeHandlerFactory f = globalTypeHandlerFactoryByFormat.get(format);
            if (f == null) {
                f = new TypeHandlerFactory(getGlobalTypeHandlerFactory());
                globalTypeHandlerFactoryByFormat.put(format, f);
            }
            return f;
        }
    }
    
    protected TypeHandlerFactory getGlobalTypeHandlerFactory() {
        return globalTypeHandlerFactory;
    }
}
