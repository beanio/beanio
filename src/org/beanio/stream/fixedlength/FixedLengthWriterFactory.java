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
package org.beanio.stream.fixedlength;

import java.io.Writer;

import org.beanio.stream.*;

/**
 * This record writer factory is used to create and configure a <tt>FixedLengthWriter</tt>.
 * 
 * @author Kevin Seim
 * @since 1.0
 * @see FixedLengthWriter
 */
public class FixedLengthWriterFactory implements RecordWriterFactory {

    private String lineSeparator;
    
	/*
	 * (non-Javadoc)
	 * @see org.beanio.stream.RecordWriterFactory#createWriter(java.io.Writer)
	 */
	public RecordWriter createWriter(Writer out) {
		FixedLengthWriter writer = new FixedLengthWriter(out);
		return writer;
	}

    /**
     * Returns the text used to terminate a record.  By default, the
     * line separator is set using the 'line.separator' system property.
     * @return the line separation text
     */
	public String getLineSeparator() {
		return lineSeparator;
	}

	/**
	 * Sets the text used to terminate a record.  If set to <tt>null</tt>, the default
     * line separator is used based on the 'line.separator' system property.
	 * @param lineSeparator the line separation text
	 */
	public void setLineSeparator(String lineSeparator) {
		this.lineSeparator = lineSeparator;
	}
}
