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
package org.beanio.config.xml;

import java.util.*;

import org.beanio.config.*;

/**
 * Stores parsing information about an XML mapping file. 
 * @author Kevin Seim
 * @since 1.2.1
 * @see XmlMappingParser
 */
public class XmlMapping {

    public static final int TYPE_HANDLER_NAMESPACE = 0;
    
    private String name;
    private String location;
    private XmlMapping parent;
    private BeanIOConfig config = new BeanIOConfig();
    private List<XmlMapping> importList = new LinkedList<XmlMapping>();
    
    /**
     * Constructs a new <tt>XmlMapping</tt>.
     */
    public XmlMapping() { }
    
    /**
     * Constructs a new <tt>XmlMapping</tt>.
     * @param name the mapping file name used for error messages
     * @param location the location of the mapping (this should be
     *   the absolute URL location of the file so that the same
     *   mapping file will always have the same the location)
     * @param parent the parent mapping
     */
    public XmlMapping(String name, String location, XmlMapping parent) {
        this.name = name;
        this.location = location;
        this.parent = parent;
    }
    
    /**
     * Returns the name of this mapping file.
     * @return the name of the mapping file
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the location of this mapping file (in URL format).
     * @return the absolute URL location of this mapping file
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Returns the BeanIO configuration for this mapping file.
     * @return the BeanIO configuration
     */
    public BeanIOConfig getConfiguration() {
        return config;
    }
    
    /**
     * Returns the parent mapping file that imported this mapping file, 
     * or <tt>null</tt> if this file is the "root" mapping file.
     * @return the parent mapping file
     */
    public XmlMapping getParent() {
        return parent;
    }
    
    /**
     * Adds an imported mapping file to this mapping file.
     * @param child the imported mapping file
     */
    public void addImport(XmlMapping child) {
        importList.add(child);
    }
    
    /**
     * Returns whether a given mapping file is being actively loaded
     * using its location to identify it.  This is used for detecting 
     * circular references.
     * @param url the mapping file location to check
     * @return <tt>true</tt> if the given location is being actively
     *   loaded, and thus the mapping file contains a circular reference
     */
    public boolean isLoading(String url) {
        return url.equals(this.location) ||
            (parent != null && parent.isLoading(url));
    }

    /**
     * Recursively adds type handlers from all imported mapping files,
     * and from this mapping file, to a given list.
     * @param list the list to add all type handlers too
     */
    public void addTypeHandlers(List<TypeHandlerConfig> list) {
        // add children first, so that type handlers declared in
        // a parent mapping file override its children
        for (XmlMapping m : importList) {
            m.addTypeHandlers(list);
        }
        list.addAll(config.getTypeHandlerList());
    }
    
    public boolean isName(int type, String name) {
        switch (type) {
        case TYPE_HANDLER_NAMESPACE:
            for (TypeHandlerConfig handler : config.getTypeHandlerList()) {
                if (name.equals(handler.getName())) {
                    return true;
                }
            }
            break;
            
        default:
            throw new IllegalArgumentException("Invalid namespace");
        }
        
        for (XmlMapping m : importList) {
            if (m.isName(type, name)) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
