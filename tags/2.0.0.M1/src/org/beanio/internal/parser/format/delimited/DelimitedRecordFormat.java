/*
 * Copyright 2011-2012 Kevin Seim
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
package org.beanio.internal.parser.format.delimited;

import org.beanio.internal.parser.*;

/**
 * A {@link RecordFormat} for delimited records.
 * 
 * <p>A delimited record may be configured to validate a record field count by
 * setting a minimum and maximum length.
 * 
 * @author Kevin Seim
 * @since 2.0
 */
public class DelimitedRecordFormat implements RecordFormat {

    private int minLength = 0;
    private int maxLength = Integer.MAX_VALUE;
    
    /**
     * Constructs a new <tt>DelimitedRecordFormat</tt>.
     */
    public DelimitedRecordFormat() { }
    
    /*
     * (non-Javadoc)
     * @see org.beanio.internal.parser.RecordFormat#validate(org.beanio.internal.parser.UnmarshallingContext)
     */
    public void validate(UnmarshallingContext context) {
        int length = ((DelimitedUnmarshallingContext)context).getFieldCount();
        
        if (length < minLength) {
            context.addRecordError("minLength", minLength, maxLength);
        }
        if (length > maxLength) {
            context.addRecordError("maxLength", minLength, maxLength);
        }
    }

    /**
     * Returns the minimum number of fields in the record.  Defaults to 0.
     * @return minimum field count
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Sets the minimum number of fields in the record.
     * @param minLength minimum field count
     */
    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    /**
     * Returns the maximum number of fields in the record.  Defaults
     * to {@link Integer#MAX_VALUE}.
     * @return maximum field count
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Sets the maximum number of fields in the record.
     * @param maxLength maximum field count
     */
    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }
}
