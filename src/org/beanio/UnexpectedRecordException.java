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
package org.beanio;

/**
 * An <tt>UnexpectedRecordException</tt> is thrown when the record type of
 * last record read from the underlying input stream is out of order as
 * defined by the stream configuration.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class UnexpectedRecordException extends BeanReaderException {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Constructs a new <tt>UnexpectedRecordException</tt>.
	 * @param context the current context of the bean reader
	 * @param message the error message
	 */
	public UnexpectedRecordException(BeanReaderContext context, String message) {
		super(context, message);
	}
}
