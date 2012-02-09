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
 * Interface for writers capable of writing bean objects to an output stream.
 * 
 * <p>A <tt>BeanWriter</tt> is created using a <tt>StreamFactory</tt> and 
 * a mapping file.</p>
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see StreamFactory
 */
public interface BeanWriter {

    /**
     * Writes a Java bean object to this output stream.
     * @param bean the bean object to write
     * @throws BeanWriterException if an I/O error or other fatal error is caught
     */
    public void write(Object bean) throws BeanWriterException;

    /**
     * Writes a Java bean object to this output stream.
     * @param recordName the name of the record definition for formatting the bean object
     * @param bean the bean object to write
     * @throws BeanWriterException if an I/O error or other fatal error is caught
     */
    public void write(String recordName, Object bean) throws BeanWriterException;

    /**
     * Flushes this output stream.
     * @throws BeanIOException if the IOException is thrown when the output
     *   stream is flushed
     */
    public void flush() throws BeanWriterIOException;

    /**
     * Closes this output stream.
     * @throws BeanWriterIOException if the IOException is thrown when the output
     *   stream is closed
     */
    public void close() throws BeanWriterIOException;

}
