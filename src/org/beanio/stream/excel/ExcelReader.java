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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.beanio.BeanIOException;
import org.beanio.stream.*;

/**
 * A <code>ExcelReader</code> is used to parse excel files into
 * records of <code>String</code> arrays. Every row in the excel sheet is
 * consider
 * a record and every column in field is parsed as field
 * <p>
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M3
 */
public class ExcelReader implements RecordReader {

    private Workbook wBook;

    private Iterator<Row> rowIterator;

    private int recordLineNumber;

    private List<String> fieldList = null;

    /**
     * Constructs a new <code>ExcelReader</code> using 
     * sheet 0 as default
     * 
     * @param in the input stream to read from
     */
    public ExcelReader(InputStream in) {
        this(in, null);
    }

    /**
     * Constructs a new <code>ExcelReader</code>.
     * 
     * @param in     the input stream to read from
     * @param config the reader configuration settings or <code>null</code> to use
     *               default values
     */
    public ExcelReader(InputStream in, ExcelParserConfiguration config) {
        if (config == null) {
            config = new ExcelParserConfiguration();
        }

        try {
            wBook = WorkbookFactory.create(in, config.getPassword());
            Sheet sheet = config.getSheetName()!= null 
                ? wBook.getSheet(config.getSheetName())
                : wBook.getSheetAt(config.getSheetIndex());
            rowIterator = sheet.iterator();
        } catch (Exception e) {
            throw new BeanIOException(e);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.beanio.stream.RecordReader#read()
     */
    @Override
    public String[] read() throws IOException {

        Row row;

        try {

            if (rowIterator.hasNext()) {

                row = rowIterator.next();
                recordLineNumber = row.getRowNum() + 1;
                fieldList = ExcelUtils.readRowValue(row);

            } else {
                fieldList = null;
            }

        } catch (Exception ex) {

            throw new RecordIOException("Error while reading excel record - Row Index: " 
                            + recordLineNumber,
                    ex);
        }

        String[] fields = null;
        if (fieldList != null) {
            fields = new String[fieldList.size()];
            fieldList.toArray(fields);
        }

        return fields;

    }


    @Override
    public void close() throws IOException {
        wBook.close();
    }

     /**
     * Returns the starting line number of the last record.  A value of
     * -1 is returned if the end of the stream was reached.
     * @return the starting line number of the last record
     */
    @Override
    public int getRecordLineNumber() {
        return recordLineNumber;
    }

    /**
     * Returns the raw text of the last record read or null if the end of the
     * stream was reached.
     * @return the raw text of the last record
     */
    @Override
    public String getRecordText() {
        return fieldList != null ? fieldList.toString() : "";
    }

}
