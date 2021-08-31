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

import com.krawler.esp.utils.ConfigReader;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
import java.io.*;
import java.util.HashMap;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.poi.hssf.usermodel.*;

/**
 *
 * @author krawler
 */
public class exportExcel extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    public void destroy() {
    }
    private HSSFWorkbook wb;
    private boolean outputreport = false;

    public exportExcel() {
        wb = new HSSFWorkbook();
    }

    public void setOutput(boolean flag) {
        this.outputreport = flag;
    }

    /**
     * Processes requests for both HTTP GET and POST methods.
     *
     * @param request servlet request
     * @param response servlet response
     */
    /**
     * outs xls file with the name specified on the response output stream.
     *
     * @param response servlet response.
     * @param rs java.sql.ResultSet object.
     * @param ht java.util.Hashtable object for column header where key is
     * actual columnname as in ResultSet rs.
     * @param sheettitle String to display as a sheet title.
     * @param filename String filename for the xls file <strong>(without
     * extension)</strong>.
     * @return void
     */
    public HashMap extractData(JSONObject obj) throws JSONException {
        HashMap hm = new HashMap();
        if (obj.has("data")) {
            JSONArray jArr = obj.getJSONArray("data");
            hm.put("data", jArr);
            if (jArr.length() > 0) {
                hm.put("header", jArr.getJSONObject(0).names());
            } else {
                hm.put("header", new JSONArray());
            }
        } else if (obj.has("coldata")) {
            JSONArray jArr = obj.getJSONArray("coldata");
            hm.put("data", jArr);
            if (jArr.length() > 0) {
                hm.put("header", jArr.getJSONObject(0).names());
            } else {
                hm.put("header", new JSONArray());
            }
        }
        return hm;
    }

    public void exportexcel(HttpServletResponse response, JSONObject jobj, java.util.Hashtable ht, String sheetTitle, String fileName, JSONArray hdr, JSONArray xlshdr) throws ServletException, IOException {
        try {

            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachement; filename=" + fileName + ".xls");
            HSSFSheet sheet = wb.createSheet(sheetTitle);
            HSSFHeader hh = sheet.getHeader();
            int j = 1;
            int width = 0;
            int maxrowno = 0;
            HSSFRow row1 = sheet.createRow((short) maxrowno);
            HashMap hm = extractData(jobj);
            JSONArray jarr = (JSONArray) hm.get("data");
            JSONObject tempObj;
            for (int k = 0; k < jarr.length(); k++) {
                tempObj = jarr.getJSONObject(k);
                HSSFRow row = sheet.createRow((short) j);
                int cellcount = 0;
                for (int i = 0; i < hdr.length(); i++) {
                    if (ht.containsValue(hdr.getString(i))) {
                        if (j == maxrowno + 1) {
                            HSSFCell cell1 = row1.createCell((short) (cellcount));
                            width = xlshdr.getString(i).length() * 325;
                            if (width > sheet.getColumnWidth((short) (cellcount))) {
                                sheet.setColumnWidth((short) (cellcount), (short) width);
                            }
                            HSSFFont font = wb.createFont();
                            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
                            HSSFRichTextString hst = new HSSFRichTextString(xlshdr.getString(i));
                            hst.applyFont(font);
                            cell1.setCellValue(hst);

                        }
                        HSSFCell cell = row.createCell((short) (cellcount));
                        String colvalue = tempObj.has(hdr.getString(i)) ? tempObj.getString(hdr.getString(i)) : "";
                        width = colvalue.length() * 325;
                        if (width > sheet.getColumnWidth((short) (cellcount))) {
                            sheet.setColumnWidth((short) (cellcount), (short) width);
                        }
                        cell.setCellValue(new HSSFRichTextString(colvalue));
                        cellcount++;
                    }
                }
                j++;

            }

            ConfigReader cr = ConfigReader.getinstance();
            String dirpath = cr.get("store");
            String path = dirpath + "baitheader.png";

//                this.addimage(path,HSSFWorkbook.PICTURE_TYPE_PNG, wb, sheet,0,0,0,0,0,0,12,4);
            if (true) {
                OutputStream out = response.getOutputStream();
                wb.write(out);
                out.close();
            }
        } catch (JSONException ex) {
            Logger.getLogger(exportExcel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(exportExcel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addimage(String imagepath, int pictype, HSSFWorkbook wb, HSSFSheet sheet, int dx1, int dy1, int dx2, int dy2, int col1, int row1, int col2, int row2)
            throws IOException {
        FileInputStream fimage = null;
        ByteArrayOutputStream bos = null;
        try {
            fimage = new FileInputStream(imagepath);
            bos = new ByteArrayOutputStream();
            int c;
            while ((c = fimage.read()) != -1) {
                bos.write(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println(e);
        } finally {
            fimage.close();
        }

        int imgindex = wb.addPicture(bos.toByteArray(), pictype);
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        HSSFClientAnchor anchor;
        anchor = new HSSFClientAnchor(dx1, dy1, dx2, dy2, (short) col1, row1, (short) col2, row2);
        anchor.setAnchorType(2);
        patriarch.createPicture(anchor, imgindex);
    }

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/vnd.ms-excel");
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("new sheet");

        // Create a row and put some cells in it. Rows are 0 based.
        HSSFRow row = sheet.createRow((short) 0);
        HSSFCell cell = row.createCell((short) 0);
        cell.setCellValue(1);

        // Or do it on one line.
        row.createCell((short) 1).setCellValue(1.2);

        row.createCell((short) 3).setCellValue(true);
        // Write the output
        OutputStream out = response.getOutputStream();
        wb.write(out);
        out.close();
    }

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        //processRequest(request, response);
        // exportexcel(request,response);
    }

    /**
     * Handles the HTTP POST method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        // exportexcel(request,response);
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Example to create a workbook in a servlet using HSSF";
    }
}
