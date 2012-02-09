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
package org.beanio.internal.config;

import java.util.*;

/**
 * A segment is used to combine fields, constants and other segments.  Wrapper
 * component may also be added to segment.
 * 
 * <p>A segment can be bound to a bean object by calling {@link #setType(String)}.
 * (The <tt>bound</tt> attribute is ignored for segments- setting <tt>type</tt>
 * to null has the same effect as setting <tt>bound</tt> to false.)
 * 
 * <p>A segment may repeat if its maximum occurrences is greater than one, and be
 * bound to a collection or array by calling {@link #setCollection(String)}.
 * 
 * <p>Segments will have their position calculated automatically during compilation.
 * 
 * <p>The <tt>constant</tt> attribute is set during compilation, and is meant for 
 * internal use only.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class SegmentConfig extends PropertyConfig {

    private boolean constant;
    private boolean defaultExistence;
    
    /**
     * Constructs a new <tt>SegmentConfig</tt>.
     */
    public SegmentConfig() { }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.config.PropertyConfig#type()
     */
    public char getComponentType() {
        return SEGMENT;
    }
    
    /**
     * Returns a list of all immediate children including segments, fields and
     * constants and the immediate children of any wrapper child.
     * @return list of children
     */
    public List<PropertyConfig> getPropertyList() {
        List<PropertyConfig> list = new ArrayList<PropertyConfig>(getChildren().size());
        toPropertyList(this, list);
        return list;
    }
    
    private void toPropertyList(ComponentConfig parent, List<PropertyConfig> list) {
        for (ComponentConfig component : parent) {
            switch (component.getComponentType()) {
            case ComponentConfig.SEGMENT:
            case ComponentConfig.FIELD:
            case ComponentConfig.CONSTANT:
                list.add((PropertyConfig)component);
                break;
            case ComponentConfig.WRAPPER:
                toPropertyList(component, list);
                break;
            }
        }
    }

    /**
     * Returns whether this segment is used to define a bean constant.
     * @return true if there is no field descendant of this segment
     */
    public boolean isConstant() {
        return constant;
    }

    /**
     * Sets whether this segment is used to define a bean constant.
     * @param constant true if there is no field descendant of this segment
     */
    public void setConstant(boolean constant) {
        this.constant = constant;
    }
    
    @Override
    protected boolean isSupportedChild(ComponentConfig child) {
        switch (child.getComponentType()) {
            case ComponentConfig.SEGMENT:
            case ComponentConfig.FIELD:
            case ComponentConfig.CONSTANT:
            case ComponentConfig.WRAPPER:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns the default existence setting for this segment as calculated
     * during pre-processing.
     * @return the default existence
     */
    public boolean getDefaultExistence() {
        return defaultExistence;
    }

    /**
     * Sets the default existence for this segment, which is calculated
     * during pre-processing.
     * @param defaultExistence the default existence
     */
    public void setDefaultExistence(boolean defaultExistence) {
        this.defaultExistence = defaultExistence;
    }
}
