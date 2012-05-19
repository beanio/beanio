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

    private String recordTerminator;
    
	/*
	 * (non-Javadoc)
	 * @see org.beanio.stream.RecordWriterFactory#createWriter(java.io.Writer)
	 */
	public RecordWriter createWriter(Writer out) {
		return new FixedLengthWriter(out, recordTerminator);
	}

    /**
     * Returns the text used to terminate a record.  By default, the record
     * terminator is set to the value of the  <tt>line.separator</tt> system property.
     * @return the record termination text
     */
    public String getRecordTerminator() {
        return recordTerminator;
    }

    /**
     * Sets the text used to terminate a record.  If set to <tt>null</tt>, the 
     * the value of the <tt>line.separator</tt> system property is used to terminate
     * records.
     * @param recordTerminator the record termination text
     */
    public void setRecordTerminator(String recordTerminator) {
        this.recordTerminator = recordTerminator;
    }
	
    /**
     * Returns the text used to terminate a record.  By default, the line
     * separator is set to the value of the  <tt>line.separator</tt> system property.
     * @return the line separation text
     * @deprecated
     */
	public String getLineSeparator() {
		return recordTerminator;
	}

	/**
	 * Sets the text used to terminate a record.  If set to <tt>null</tt>, the 
	 * the value of the <tt>line.separator</tt> system property is used to terminate
	 * records.
	 * @param lineSeparator the line separation text
	 * @deprecated
	 */
	public void setLineSeparator(String lineSeparator) {
		this.recordTerminator = lineSeparator;
	}
}
