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
package com.krawler.esp.servlets;

import com.krawler.utils.json.base.JSONException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.apache.poi.ss.usermodel.*;

/**
 *
 * @author krawler
 */
public class XLSDataExtractor extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        JSONObject jobj = new JSONObject();
        PrintWriter out = response.getWriter();
        try {
            String filename = request.getParameter("filename");
            int sheetNo = Integer.parseInt(request.getParameter("index"));
            jobj = parseXLS(filename, sheetNo);
        } catch (Exception e) {
            Logger.getLogger(XLSDataExtractor.class.getName()).log(Level.SEVERE, null, e);
            try {
                jobj.put("msg", e.getMessage());
                jobj.put("lsuccess", false);
                jobj.put("valid", true);
            } catch (JSONException ex) {
            }
        } finally {
            out.println(jobj);
        }
    }

    public JSONObject parseXLS(String filename, int sheetNo) throws FileNotFoundException, IOException, JSONException {
        JSONObject jobj = new JSONObject();

        int startRow = 0;
        int maxRow = 0;
        int maxCol = 0;
        int noOfRowsDisplayforSample = 20;
        JSONArray jArr = new JSONArray();
        try {

            FileInputStream fs = new FileInputStream(filename);
            Workbook wb = WorkbookFactory.create(fs);
            //        HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
            Sheet sheet = wb.getSheetAt(sheetNo);
            maxRow = sheet.getLastRowNum();
            if (noOfRowsDisplayforSample > sheet.getLastRowNum()) {
                noOfRowsDisplayforSample = sheet.getLastRowNum();
            }
            for (int i = 0; i <= noOfRowsDisplayforSample; i++) {
                Row row = sheet.getRow(i);
                JSONObject obj = new JSONObject();
                JSONObject jtemp1 = new JSONObject();
                if (row == null) {
                    jArr.put(obj);
                    continue;
                }
                if (maxCol < row.getLastCellNum()) {
                    maxCol = row.getLastCellNum();
                }
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) {
                        continue;
                    }
                    String colHeader = new CellReference(i, j).getCellRefParts()[2];
                    String val = null;
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            val = Double.toString(cell.getNumericCellValue());
                            break;
                        case Cell.CELL_TYPE_STRING:
                            val = cell.getRichStringCellValue().getString();
                            break;
                    }
                    if (i == 0) { // List of Headers (Consider first row as Headers)
                        jtemp1 = new JSONObject();
                        jtemp1.put("header", val);
                        jtemp1.put("index", j);
                        jobj.append("Header", jtemp1);
                    }
                    obj.put(colHeader, val);
                }
                jArr.put(obj);
            }

        } catch (Exception ex) {
            Logger.getLogger(XLSDataExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        jobj.put("startrow", startRow);
        jobj.put("maxrow", maxRow);
        jobj.put("maxcol", maxCol);
        jobj.put("index", sheetNo);
        jobj.put("data", jArr);
        jobj.put("filename", filename);

        jobj.put("msg", "Image has been successfully uploaded");
        jobj.put("lsuccess", true);
        jobj.put("valid", true);
        return jobj;
    }

    public JSONObject parseXLS1(String filename, int sheetNo) throws FileNotFoundException, IOException, JSONException {
        JSONObject jobj = new JSONObject();

        ArrayList<String> arr = new ArrayList<String>();
        int startRow = 0;
        int maxRow = 0;
        int maxCol = 0;
        JSONArray jArr = new JSONArray();
        try {
            FileInputStream fs = new FileInputStream(filename);
            Workbook wb = WorkbookFactory.create(fs);
//        HSSFFormulaEvaluator evaluator = new HSSFFormulaEvaluator(wb);
            Sheet sheet = wb.getSheetAt(sheetNo);
            maxRow = sheet.getLastRowNum();
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                JSONObject obj = new JSONObject();
                JSONObject jtemp1 = new JSONObject();
                if (row == null) {
                    jArr.put(obj);
                    continue;
                }
                if (maxCol < row.getLastCellNum()) {
                    maxCol = row.getLastCellNum();
                }
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    String val = null;
                    if (cell == null) {
                        arr.add(val);
                        continue;
                    };
                    String colHeader = new CellReference(i, j).getCellRefParts()[2];
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            val = Double.toString(cell.getNumericCellValue());
                            break;
                        case Cell.CELL_TYPE_STRING:
                            val = cell.getRichStringCellValue().getString();
                            break;
                    }
                    if (i == 0) { // List of Headers (Consider first row as Headers)
                        jtemp1 = new JSONObject();
                        jtemp1.put("header", val);
                        jtemp1.put("index", j);
                        jobj.append("Header", jtemp1);
                        obj.put(colHeader, val);
                        arr.add(val);
                    } else {
                        if (arr.get(j) != null) {
                            obj.put(arr.get(j), val);
                        }
                    }

                }
                jArr.put(obj);
            }
        } catch (Exception ex) {
            Logger.getLogger(XLSDataExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        jobj.put("startrow", startRow);
        jobj.put("maxrow", maxRow);
        jobj.put("maxcol", maxCol);
        jobj.put("index", sheetNo);
        jobj.put("data", jArr);
        jobj.put("filename", filename);

        jobj.put("msg", "Image has been successfully uploaded");
        jobj.put("lsuccess", true);
        jobj.put("valid", true);
        return jobj;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
