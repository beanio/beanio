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
package org.beanio.types;

/**
 * A type handler implementation for the <tt>String</tt> class that does no
 * translation of the text or value.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class StringTypeHandler implements TypeHandler {

	/**
	 * Returns the unmodified text.
	 */
	public String parse(String text) throws TypeConversionException {
		return text;
	}

	/**
	 * Formats the value by calling {@link Object#toString()}.
	 * @param value the value to format
	 * @returns the formatted value, or the empty string if <tt>value</tt>
	 *     was <tt>null</tt>
	 */
	public String format(Object value) {
		if (value == null) 
			return "";
		return value.toString();
	}

}
