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

/**
 * Stores configuration settings for parsing excel formatted streams.
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M3
 */
public class ExcelParserConfiguration {

    private int sheetIndex = 0;

    private String sheetName = null;

    private String password = null;

    private String excelType = ExcelType.XLS.getExt();

    /**
     * Constructs a new <code>ExcelParserConfiguration</code>.
     */
    public ExcelParserConfiguration() {
    }

    /**
     * Constructs a new <code>ExcelParserConfiguration</code>.
     * 
     * @param sheetIndex the index of the sheet form the workbook
     */
    public ExcelParserConfiguration(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    /**
     * Constructs a new <code>ExcelParserConfiguration</code>.
     * 
     * @param sheetName the name of the sheet form the workbook
     */
    public ExcelParserConfiguration(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * Returns the index of the sheet. Defaults to 0.
     * 
     * @return the index of the sheet form the workbook
     */
    public int getSheetIndex() {
        return sheetIndex;
    }

    /**
     * Sets the field sheetIndex of the sheet to use.
     * 
     * @param sheetIndex the index of the sheet form the workbook
     */
    public void setSheetIndex(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    /**
     * Returns the name of the sheet. Defaults to 0.
     * 
     * @return the name of the sheet form the workbook
     */
    public String getSheetName() {
        return sheetName;
    }

    /**
     * Sets the field sheetName of the sheet to use.
     * 
     * @param sheetName the name of the sheet form the workbook
     */
    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    /**
     * Returns the type of the sheet. Defaults to XLS.
     * 
     * @return the name of the sheet form the workbook
     */
    public String getExcelType() {
        return excelType;
    }

    /**
     * Sets the field excelType of the sheet to use.
     * 
     * @param excelType the type of the sheet form the workbook
     */
    public void setExcelType(String excelType) {
        this.excelType = ExcelType.get(excelType).getExt();
    }

    /**
     * Returns the password of the excel. Defaults to XLS.
     * 
     * @return the password of the excel form the workbook
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the field password of the excel to use.
     * 
     * @param password the password of the excel workbook
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
