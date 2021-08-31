/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.esp.fileparser.excelparser;

import com.krawler.utils.json.base.JSONObject;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class MsExcelParser {

    public String extractText(String filepath) throws Exception {

        InputStream input = new BufferedInputStream(new FileInputStream(
                filepath));
        String resultText = "";
        HSSFWorkbook wb = new HSSFWorkbook(input);
        if (wb == null) {
            return resultText;
        }

        HSSFSheet sheet;
        HSSFRow row;
        HSSFCell cell;
        int sNum = 0;
        int rNum = 0;
        int cNum = 0;

        sNum = wb.getNumberOfSheets();

        for (int i = 0; i < sNum; i++) {
            if ((sheet = wb.getSheetAt(i)) == null) {
                continue;
            }
            rNum = sheet.getLastRowNum();

            for (int j = 0; j <= rNum; j++) {
                if ((row = sheet.getRow(j)) == null) {
                    continue;
                }
                cNum = row.getLastCellNum();

                for (int k = 0; k < cNum; k++) {
                    try {
                        if ((cell = row.getCell((short) k)) != null) {
                            /*
                             * if(HSSFDateUtil.isCellDateFormatted(cell) ==
                             * true) { resultText +=
                             * cell.getDateCellValue().toString() + " "; } else
                             */
                            if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
                                resultText += "           ";

                            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                resultText += cell.getRichStringCellValue().toString() + " ";
                            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                                Double d = new Double(cell.getNumericCellValue());
                                resultText += d.toString() + " ";
                            }

                            /*
                             * else if(cell.getCellType() ==
                             * HSSFCell.CELL_TYPE_FORMULA){ resultText +=
                             * cell.getCellFormula() + " "; }
                             */
                        }
                    } catch (Exception ex) {
                    }
                }
                resultText += "\n";
            }
        }
        if (input != null) {
            input.close();
        }
        return resultText;
    }

    public String getFormatedJSON(String filepath) throws Exception {
        InputStream input = new BufferedInputStream(new FileInputStream(
                filepath));
        JSONObject jobj = new JSONObject();
        HSSFWorkbook wb = new HSSFWorkbook(input);
        if (wb == null) {
            return ("\"data\":[]");
        }
        HSSFSheet sheet;
        HSSFRow row;
        HSSFCell cell;
        int sNum = 0;
        int rNum = 0;
        int cNum = 0;

        sNum = wb.getNumberOfSheets();

        for (int i = 0; i < sNum; i++) {
            if ((sheet = wb.getSheetAt(i)) == null) {
                continue;
            }
            rNum = sheet.getLastRowNum();

            for (int j = 0; j <= rNum; j++) {
                if ((row = sheet.getRow(j)) == null) {
                    continue;
                }
                cNum = row.getLastCellNum();
                JSONObject temp = new JSONObject();
                for (int k = 0; k < cNum; k++) {
                    try {
                        if ((cell = row.getCell((short) k)) != null) {

                            if (cell.getCellType() == HSSFCell.CELL_TYPE_BLANK) {
                                temp.put("cell" + cell.getCellNum(), "");

                            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                temp.put("cell" + cell.getCellNum(), cell.getRichStringCellValue().toString());
                            } else if (cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
                                Double d = new Double(cell.getNumericCellValue());
                                temp.put("cell" + cell.getCellNum(), d.toString());
                            }
                        }

                    } catch (Exception ex) {
                    }
                }
                jobj.append("data", temp);
                temp = null;
            }
        }
        if (input != null) {
            input.close();
        }
        return jobj.toString();
    }

    public static void main(String args[]) {
        try {
            MsExcelParser msp = new MsExcelParser();
            String str = msp.extractText("g:\\C# Tranning.xls");
            System.out.println(str);

        } catch (Exception ex) {
            System.out.print(ex);
        }
    }
}
