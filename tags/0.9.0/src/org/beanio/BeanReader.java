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
 * Interface for readers capable of reading bean objects from an input stream.
 * 
 * <p>A <tt>BeanReader</tt> is created using a <tt>StreamFactory</tt> and 
 * a mapping file.</p>
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see StreamFactory
 */
public interface BeanReader {

	/**
	 * Reads a single bean from the input stream.  If the end of the stream is
	 * reached, null is returned.
	 * @return the Java bean read, or null if the end of the stream was reached
	 * @throws BeanReaderIOException if an IOException or other fatal error is caught
	 * @throws MalformedRecordException if the underlying input stream is malformed
	 *   and the record could not be accurately read
	 * @throws UnidentifiedRecordException if the record type could not be identified
	 * @throws UnexpectedRecordException if the record type is out of sequence
	 * @throws InvalidRecordException if the record was identified and failed record
	 *   or field level validations (including field type conversion errors)
	 */
	public Object read() throws BeanReaderIOException, MalformedRecordException,
		UnidentifiedRecordException, UnexpectedRecordException, InvalidRecordException;
	
	/**
	 * Returns the name of the last record read.
	 * @return the name of the last record read
	 */
	public String getRecordName();
	
	/**
	 * The beginning line number of the last record read.
	 * @return the line number
	 */
	public int getLineNumber();
	
	/**
	 * Closes the underlying input stream.
	 * @throws BeanReaderIOException if an IOException is thrown when closing the stream
	 */
    public void close() throws BeanReaderIOException;

    /**
     * Sets the error handler to delegate bean reader exceptions to.
     * @param errorHandler the error handler to delegate exceptions to
     */
    public void setErrorHandler(BeanReaderErrorHandler errorHandler);
}
