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

/**
 * Stores configuration settings for a record.  Records and groups
 * are used to define the layout of stream.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class RecordConfig extends NodeConfig {

    private BeanConfig bean;
    private Integer minLength = null;
    private Integer maxLength = null;

    /**
     * Returns {@link NodeConfig#RECORD}
     */
    @Override
    public char getType() {
        return RECORD;
    }

    /**
     * Returns the bean configuration this record is mapped to.
     * @return the bean configuration
     */
    public BeanConfig getBean() {
        return bean;
    }

    /**
     * Sets the bean configuration this record is maps to.
     * @param bean the bean configuration
     */
    public void setBean(BeanConfig bean) {
        this.bean = bean;
    }

    /**
     * Returns the minimum length of the record.  Depending on the type
     * of stream, the length may refer to the number of fields or the 
     * number of characters.
     * @return the minimum record length, or <tt>null</tt> if not set
     */
    public Integer getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum length of the record.  Depending on the type
     * of stream, the length may refer to the number of fields or the 
     * number of characters.
     * @param minLength the minimum record length, or <tt>null</tt> if not set
     */
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum length of the record.  Depending on the type of
     * stream, the length may refer to the number of fields or the number
     * of characters.
     * @return the maximum record length, or <tt>null</tt> if not set
     */
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum length of the record.  Depending on the type of
     * stream, the length may refer to the number of fields or the number
     * of characters.
     * @param maxLength the maximum record length, or <tt>null</tt> if not set
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }
}
