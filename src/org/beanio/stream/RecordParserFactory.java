/*
 * Copyright 2012 Kevin Seim
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
package org.beanio.stream;

import java.io.*;

import org.beanio.*;

/**
 * Factory interface for creating record parsers.
 * 
 * <p>The following table shows the required parser based on the requested BeanIO interface.</p>
 * 
 * <table border="1">
 * <tr>
 *   <th>Requests For</th>
 *   <th>Requires Parser</th>
 * </tr>
 * <tr>
 *   <td>{@link BeanReader}</td>
 *   <td>{@link RecordReader}</td>
 * </tr>
 * <tr>
 *   <td>{@link BeanWriter}</td>
 *   <td>{@link RecordWriter}</td>
 * </tr>
 * <tr>
 *   <td>{@link Unmarshaller}</td>
 *   <td>{@link RecordUnmarshaller}</td>
 * </tr>
 * <tr>
 *   <td>{@link Marshaller}</td>
 *   <td>{@link RecordMarshaller}</td>
 * </tr>
 * </table>
 * 
 * <p>Once configured, implementations must be thread safe.</p>
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public interface RecordParserFactory {

    /**
     * Initializes the factory.  This method is called when a mapping file is loaded after
     * all parser properties have been set, and is therefore ideally used to preemptively
     * validate parser configuration settings.
     * @throws IllegalArgumentException if the parser configuration is invalid
     */
    public void init() throws IllegalArgumentException;
    
    /**
     * Creates a parser for reading records from an input stream.
     * @param in the input stream to read from
     * @return the created {@link RecordReader}
     * @throws IllegalArgumentException if this factory is improperly configured
     *   and a {@link RecordReader} cannot be created
     */
    public RecordReader createReader(Reader in) throws IllegalArgumentException;
    
    /**
     * Creates a parser for writing records to an output stream.
     * @param out the output stream to write to
     * @return the new {@link RecordWriter}
     * @throws IllegalArgumentException if this factory is improperly configured
     *   and a {@link RecordWriter} cannot be created
     */
    public RecordWriter createWriter(Writer out) throws IllegalArgumentException;
    
    /**
     * Creates a parser for marshalling records.
     * @return the created {@link RecordMarshaller}
     * @throws IllegalArgumentException if this factory is improperly configured and
     *   a {@link RecordMarshaller} cannot be created
     */
    public RecordMarshaller createMarshaller() throws IllegalArgumentException;

    /**
     * Creates a parser for unmarshalling records.
     * @return the created {@link RecordUnmarshaller}
     * @throws IllegalArgumentException if this factory is improperly configured and
     *   a {@link RecordUnmarshaller} cannot be created
     */
    public RecordUnmarshaller createUnmarshaller() throws IllegalArgumentException;
    
}
