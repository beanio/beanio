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
 * A <tt>BeanReaderIOException</tt> is thrown when a BeanReader's underlying
 * input stream throws an <tt>IOException</tt> or another otherwise fatal error
 * occurs during the parsing of an input stream.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class BeanReaderIOException extends BeanReaderException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new <tt>BeanReaderIOException</tt>.
     * @param message the error message
     * @param cause the root cause
     */
    public BeanReaderIOException(String message, Throwable cause) {
        super(null, message, cause);
    }

    /**
     * Constructs a new <tt>BeanReaderIOException</tt>.
     * @param context the current context of the bean reader
     * @param message the error message
     * @param cause the root cause
     */
    public BeanReaderIOException(BeanReaderContext context, String message, Throwable cause) {
        super(context, message, cause);
    }

    /**
     * Constructs a new <tt>BeanReaderIOException</tt>.
     * @param context the current context of the bean reader
     * @param message the error message
     */
    public BeanReaderIOException(BeanReaderContext context, String message) {
        super(context, message);
    }
}
