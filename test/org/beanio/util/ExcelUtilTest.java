package org.beanio.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ExcelUtilTest {

    public static String convertExcelStream2CSV(InputStream in) throws IOException {
        return convertExcelStream2CSV(in, null);
    }

    public static String convertExcelStream2CSV(InputStream in, String password) throws IOException {

        try (Workbook workbook = WorkbookFactory.create(in, password)) {

            Sheet sheet = workbook.getSheetAt(0);
            StringWriter csvWriter = new StringWriter();
            for (Row row : sheet) {
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    csvWriter.append(cell.toString());
                    if (cellIterator.hasNext()) {
                        csvWriter.append(",");
                    }
                }
                csvWriter.append("\n");
            }
            return csvWriter.toString();
        }
    }

    public static InputStream convertCSV2Excel(String s) throws IOException {
        List<String> values = Arrays.asList(s.split(","));

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row row = sheet.createRow(0);
        for (int i = 0; i < values.size(); i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(values.get(i));
        }

        File tempFile = Files.createTempFile("temp_excel_", ".xls").toFile();
        tempFile.deleteOnExit();
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            workbook.write(fileOutputStream);
            workbook.close();
        }

        return new FileInputStream(tempFile);
    }

}
