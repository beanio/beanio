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

import java.io.*;

import org.beanio.stream.*;

/**
 * A <tt>FixedLengthWriter</tt> is used to write records to fixed length
 * flat file or output stream.  A fixed length record is represented using 
 * the {@link String} class. 
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class FixedLengthWriter implements RecordWriter {

	private Writer out;
	private String lineSeparator;
	
	/**
	 * Constructs a new <tt>FixedLegthWriter</tt>.
	 * @param out the output stream to write to
	 */
	public FixedLengthWriter(Writer out) {
		this(out, null);
	}
	
	/**
	 * Constructs a new <tt>FixedLegthWriter</tt>.
	 * @param out the output stream to write to
	 * @param lineSeparator the text used to terminate a record
	 */
	public FixedLengthWriter(Writer out, String lineSeparator) {
		this.out = out;
		if (lineSeparator == null) {
			lineSeparator = System.getProperty("line.separator");
		}
		this.lineSeparator = lineSeparator;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.beanio.line.RecordWriter#write(java.lang.Object)
	 */
	public void write(Object value) throws IOException, RecordIOException {
		out.write(value.toString());
		out.write(lineSeparator);
	}

	/*
	 * (non-Javadoc)
	 * @see org.beanio.line.RecordWriter#flush()
	 */
	public void flush() throws IOException {
		out.flush();
	}

	/*
	 * (non-Javadoc)
	 * @see org.beanio.line.RecordWriter#close()
	 */
	public void close() throws IOException {
		out.close();
	}
}
