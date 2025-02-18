/*
 * Copyright 2023 Vidhya Sagar, Jeevendran
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
package org.beanio.stream.excel;

import java.util.*;

import org.beanio.stream.*;

/**
 * A combined {@link RecordMarshaller} and {@link RecordUnmarshaller}
 * implementation for excel row records.
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M3
 */
public class ExcelRecordParser implements RecordUnmarshaller, RecordMarshaller {

    char delim = ',';
    private List<String> fieldList = new ArrayList<>();

    /**
     * Constructs a new <code>DelimitedRecordParser</code>.
     */
    public ExcelRecordParser() {
        this(new ExcelParserConfiguration());
    }

    /**
     * Constructs a new <code>ExcelRecordParser</code>.
     * 
     * @param config the parser configuration settings
     */
    public ExcelRecordParser(ExcelParserConfiguration config) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.beanio.stream.RecordParser#parse(java.lang.String)
     */
    @Override
    public String[] unmarshal(String text) {
        fieldList.clear();

        StringBuilder field = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (c == delim) {
                fieldList.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }

        fieldList.add(field.toString());

        String[] record = new String[fieldList.size()];
        fieldList.toArray(record);
        return record;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.beanio.stream.RecordMarshaller#marshal(java.lang.Object)
     */
    @Override
    public String marshal(Object record) {
        return marshal((String[]) record);
    }
    

    /**
     * Marshals a <code>String</code> array into a delimited record.
     * 
     * @param record the <code>String[]</code> to marshal
     * @return the formatted record text
     */
    public String marshal(String[] record) {
        StringBuilder text = new StringBuilder();

        int pos = 0;
        for (String field : record) {
            if (pos++ > 0) {
                text.append(delim);
            }
            text.append(field);
        }

        return text.toString();
    }
}
