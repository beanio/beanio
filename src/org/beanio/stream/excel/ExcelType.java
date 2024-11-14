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

public enum ExcelType {
    XLS("xls"),
    XLSX("xlsx");

    String ext;

    ExcelType(String ext) {
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }

    public static ExcelType get(String ext) {

        if (ext.toLowerCase().contains(XLSX.getExt())) {
            return XLSX;
        }
        return XLS;
    }

}
