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

import java.util.*;

/**
 * A subclass of <tt>BeanReaderException</tt> is thrown for any error
 * that occurs while using the bean reader from the input stream.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see BeanReader
 */
public abstract class BeanReaderException extends BeanIOException {

	private static final long serialVersionUID = 1453590555984013399L;
	
	private BeanReaderContext context;
	
	/**
	 * Constructs a new <tt>BeanReaderException</tt>.
	 * @param context the current context of the bean reader
	 * @param message the error message
	 */
	public BeanReaderException(BeanReaderContext context, String message) {
		this(context, message, null);
	}

	public BeanReaderException(BeanReaderContext context, String message, Throwable cause) {
		super(message, cause);
		this.context = context;
	}
	
	/**
	 * Returns the current context of the bean reader.
	 * @return bean reader context
	 */
	public BeanReaderContext getContext() {
		return context;
	}

	@Override
	public String toString() {
		String message = super.toString(); 
		if (context == null || !(context.hasFieldErrors() || context.hasRecordErrors())) {
			return message;
		}
		
		StringBuffer s = new StringBuffer(message);
		
		if (context.hasRecordErrors()) {
			for (String error : context.getRecordErrors()) {
				s.append("\n  ==> Invalid '");
				s.append(context.getRecordName());
				s.append("' record: ");
				s.append(error);
				s.append("\n");
			}
		}
		if (context.hasFieldErrors()) {
			for (Map.Entry<String, Collection<String>> entry : context.getFieldErrors().entrySet()) {
				String fieldName = entry.getKey();
				for (String error : entry.getValue()) {
					s.append("\n  ==> Invalid '");
					s.append(fieldName);
					s.append("' field: ");
					s.append(error);
				}
			}
		}
		return s.toString();
	}
	
	
	
}
