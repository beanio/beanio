/*
 * Copyright 2010-2012 Kevin Seim
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
package org.beanio;

/**
 * Exception thrown when an invalid BeanIO configuration file is loaded. Specially
 * when a repeating definition is given with a none xml type.
 * 
 * @author Cornelis Hoeflake
 * @since 3.0.0
 */
public class BeanIOConfigurationRepeatingNoneException extends BeanIOConfigurationException {
    private static final long serialVersionUID = 3L;

    /**
     * Constructs a new <code>BeanIOConfigurationRepeatingNoneException</code>.
     * @param message the error message
     */
    public BeanIOConfigurationRepeatingNoneException(String message) {
        super(message);
    }
}
