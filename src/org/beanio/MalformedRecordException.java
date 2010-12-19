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
 * This exception is thrown when an identified record cannot be parsed.
 * @author Kevin Seim
 * @since 1.0
 */
public class MalformedRecordException extends BeanReaderException {

	private static final long serialVersionUID = 1648408377215305643L;

	/**
	 * 
	 * @param context
	 * @param message
	 */
	public MalformedRecordException(BeanReaderContext context, String message) {
		super(context, null);
	}

	/**
	 * 
	 * @param context
	 * @param message
	 * @param cause
	 */
	public MalformedRecordException(BeanReaderContext context, String message, Throwable cause) {
		super(context, message, cause);
	}
	
}
