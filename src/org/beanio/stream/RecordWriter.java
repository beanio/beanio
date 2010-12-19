/*
 * Copyright 2010 Kevin Seim
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

/**
 * A <tt>RecordWriter</tt> is used to write records to an output stream.
 * The class used to implement a "record" is implementation specific and
 * dependent on the format of the output stream. 
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public interface RecordWriter {

	/**
	 * This method writes line termination characters to the given stream.
	 * @param out stream to write tos
	 */
	public void write(Object record) throws IOException;
	
	/**
	 * Flushes the output stream.
	 * @throws IOException if an I/O error occurs flushing the stream
	 */
	public void flush() throws IOException;
	
	/**
	 * Closes the output stream.
	 * @throws IOException if an I/O error occurs closing the stream
	 */
	public void close() throws IOException;
}
