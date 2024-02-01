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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

/**
 * Excel Operations Utility
 * 
 * @author Vidhya Sagar, Jeevendran
 * @since 3.0.0.M3
 */
public class ExcelUtils {

    public static List<String> readRowValue(Row row) {
        Iterable<Cell> cellIterable = () -> row.cellIterator();
        return StreamSupport.stream(cellIterable.spliterator(), false)
                .map(cell -> ExcelUtils.readCellValue(cell))
                .collect(Collectors.toList());
    }

    /*
     * reads the value in the specific cell
     */
    public static String readCellValue(Cell cell) {

        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {

            case FORMULA:

                if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                    return getNumericCellValue(cell);
                } else {
                    return cell.getStringCellValue();
                }

            case BOOLEAN:

                return Boolean.toString(cell.getBooleanCellValue());

            case NUMERIC:

                return getNumericCellValue(cell);

            case STRING:
            case BLANK:
            default:

                return cell.toString();

        }

    }

    /*
     * reads numeric cell value as text (string)
     */
    public static String getNumericCellValue(Cell cell) {
        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.toString();
        }
        DecimalFormat format = new DecimalFormat("#.#");
        format.setRoundingMode(RoundingMode.FLOOR);
        return format.format(cell.getNumericCellValue());
    }

}
