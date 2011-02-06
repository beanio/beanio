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
package org.beanio.parser.delimited;

import org.beanio.parser.Record;
import org.beanio.parser.flat.FlatRecordDefinition;

/**
 * A <tt>FixedLengthRecordDefinition</tt> is used to validate delimited records.
 * 
 * @author Kevin Seim
 * @since 1.0
 */
public class DelimitedRecordDefinition extends FlatRecordDefinition {

    @Override
    protected void validateRecord(Record r) {
        super.validateRecord(r);

        DelimitedRecord record = (DelimitedRecord) r;
        
        int size = record.getFieldCount();
        int minLength = getMinLength();
        int maxLength = getMaxLength();
        
        if (minLength > 0 && size < minLength) {
            record.addRecordError("minLength", minLength, maxLength);
        }
        if (maxLength > 0 && size > maxLength) {
            record.addRecordError("maxLength", minLength, maxLength);
        }
    }
}
