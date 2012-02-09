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
 * An <tt>UnidentifiedRecordException</tt> is thrown when the record type
 * of the last record read from the input stream could not be determined.
 * When strict record ordering is enforced by the stream configuration,
 * further reads from the same input stream will likely cause further 
 * exceptions.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class UnidentifiedRecordException extends BeanReaderException {

	private static final long serialVersionUID = 2L;

	/**
	 * Constructs a new <tt>UnidentifiedRecordException</tt>.
	 * @param context the current context of the bean reader
	 * @param message the error message
	 */
	public UnidentifiedRecordException(RecordContext context, String message) {
		super(message);
		setRecordContext(context);
	}
}
