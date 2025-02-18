/*
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

import java.io.*;
import java.security.GeneralSecurityException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.beanio.BeanIOException;
import org.beanio.stream.*;

/**
 * A <code>ExcelWriter</code> is used to write record to excel files.
 * Every record is consider a row in the excel file
 * and every field is considered as cell in the excel row
 * <p>
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M3
 */
public class ExcelWriter implements RecordWriter {

    private Workbook wBook;
    private Sheet wSheet;
    private String password;
    private ExcelType excelType;

    private int lineNumber = -1;

    private OutputStream out;

    private boolean writeCompleted = false;

    /**
     * Constructs a new <code>ExcelWriter</code>.
     * 
     * @param out the output stream to write to excel
     */
    public ExcelWriter(OutputStream out) {
        this(out, null);
    }

    /**
     * Constructs a new <code>ExcelWriter</code>.
     * 
     * @param out        the output stream to write to
     * @param sheetIndex the sheet index of the workbook
     */
    public ExcelWriter(OutputStream out, int sheetIndex) {
        this(out, new ExcelParserConfiguration(sheetIndex));
    }

    /**
     * Constructs a new <code>ExcelWriter</code>.
     * 
     * @param out    the output stream to write to
     * @param config the excel parser configuration
     */
    public ExcelWriter(OutputStream out, ExcelParserConfiguration config) {

        if (config == null) {
            config = new ExcelParserConfiguration();
        }
        this.password = config.getPassword();
        this.excelType = ExcelType.get(config.getExcelType());
        this.out = out;

        wBook = excelType.equals(ExcelType.XLSX) ? new XSSFWorkbook() : new HSSFWorkbook();
        wSheet = wBook.createSheet(config.getSheetName() != null ? config.getSheetName() : "output");

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.beanio.line.RecordWriter#write(java.lang.Object)
     */
    @Override
    public void write(Object value) throws IOException, RecordIOException {
        write((String[]) value);
    }

    /**
     * Writes a record to the output stream.
     * 
     * @param record the record to write
     * @throws IOException if an I/O error occurs
     */
    public void write(String[] record) throws IOException {

        Row row = wSheet.createRow(++lineNumber);

        for (int cellIndex = 0; cellIndex < record.length; cellIndex++) {

            Cell cell = row.createCell(cellIndex);

            Object cellValue = record[cellIndex];

            if (cellValue == null) {
                cell.setCellValue("");
            } else {
                cell.setCellValue(cellValue.toString());
            }
        }

    }

    private void write2Workbook() {

        if (password != null & excelType.equals(ExcelType.XLSX)) {

            try {

                File tempFile = File.createTempFile("tempBeanio", "excel");
                OutputStream tempOut = new FileOutputStream(tempFile);
                wBook.write(tempOut);
                wBook.close();
                tempOut.close();

                POIFSFileSystem fs = new POIFSFileSystem();
                EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
                Encryptor enc = info.getEncryptor();
                enc.confirmPassword(password);
                OutputStream os = enc.getDataStream(fs);
                OPCPackage opc = OPCPackage.open(tempFile, PackageAccess.READ_WRITE);
                opc.save(os);
                opc.close();

                IOUtils.write(IOUtils.toByteArray(new FileInputStream(tempFile)), out);

            } catch (IOException | GeneralSecurityException | InvalidFormatException e) {
                throw new BeanIOException("Error while applying password to workbook", e);
            }

        } else if (password != null & excelType.equals(ExcelType.XLS)) {

            try {
                ((HSSFWorkbook) wBook).writeProtectWorkbook(password, "");
                wBook.write(out);
                wBook.close();
            } catch (IOException e) {
                throw new BeanIOException("Error while writing to protected workbook", e);
            }

        } else {

            try {
                wBook.write(out);
                wBook.close();
            } catch (IOException e) {
                throw new BeanIOException("Error while writing to workbook", e);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.beanio.line.RecordWriter#flush()
     */
    @Override
    public void flush() throws IOException {

        if (writeCompleted) {
            throw new UnsupportedOperationException("excel format write do not support mutiple flush");
        }

        if (!writeCompleted) {
            this.write2Workbook();
            writeCompleted = true;
        }
        out.flush();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.beanio.line.RecordWriter#close()
     */
    @Override
    public void close() throws IOException {
        if (!writeCompleted) {
            this.write2Workbook();
            writeCompleted = true;
        }
        out.close();
    }
}
