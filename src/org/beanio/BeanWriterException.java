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
package org.beanio;

/**
 * A subclass of <tt>BeanWriterException</tt> is thrown for any exception
 * that occurs while using a <tt>BeanWriter</tt> to write to an output stream.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see BeanWriter
 */
public abstract class BeanWriterException extends BeanIOException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new <tt>BeanWriterException</tt>.
     * @param message the error message
     * @param cause the root cause
     */
    public BeanWriterException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new <tt>BeanWriterException</tt>.
     * @param message the error message
     */
    public BeanWriterException(String message) {
        super(message);
    }

    /**
     * Constructs a new <tt>BeanWriterException</tt>.
     * @param cause the root cause
     */
    public BeanWriterException(Throwable cause) {
        super(cause);
    }
}
