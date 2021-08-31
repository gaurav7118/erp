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

import com.krawler.esp.handlers.StorageHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 *
 * @author krawler
 */
public class fileUploadXLS extends HttpServlet {

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
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("success", true);
            FileItemFactory factory = new DiskFileItemFactory(4096, new File("/tmp"));
            ServletFileUpload upload = new ServletFileUpload(factory);
            upload.setSizeMax(10000000);
            List fileItems = upload.parseRequest(request);
            Iterator i = fileItems.iterator();
            String destinationDirectory = StorageHandler.GetDocStorePath() + "xlsfiles";
            String fileName = null;
            while (i.hasNext()) {
                FileItem fi = (FileItem) i.next();
                if (fi.isFormField()) {
                    continue;
                }
                fileName = fi.getName();

                fi.write(new File(destinationDirectory, fileName));
            }

            FileInputStream fs = new FileInputStream(destinationDirectory + "/" + fileName);
            Workbook wb = WorkbookFactory.create(fs);
            int count = wb.getNumberOfSheets();
            JSONArray jArr = new JSONArray();
            for (int x = 0; x < count; x++) {
                JSONObject obj = new JSONObject();
                obj.put("name", wb.getSheetName(x));
                obj.put("index", x);
                jArr.put(obj);
            }
            jobj.put("file", destinationDirectory + "/" + fileName);
            jobj.put("data", jArr);
            jobj.put("msg", "Image has been successfully uploaded");
            jobj.put("lsuccess", true);
            jobj.put("valid", true);
        } catch (Exception e) {
            Logger.getLogger(fileUploadXLS.class.getName()).log(Level.SEVERE, null, e);
            try {
                jobj.put("msg", e.getMessage());
                jobj.put("lsuccess", false);
                jobj.put("valid", true);
            } catch (Exception ex) {
            }
        } finally {
            out.println(jobj);
        }
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
