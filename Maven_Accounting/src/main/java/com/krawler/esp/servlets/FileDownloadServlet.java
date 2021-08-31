/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.esp.servlets;

import com.krawler.common.util.StringUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.*;
import java.io.PrintWriter;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.sessionHandler.CompanySessionClass;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public class FileDownloadServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {        
        ServletOutputStream os=null;
        FileInputStream fis=null;
        try {
                String path ="";
                String filename = "";
                String companyid = sessionHandlerImpl.getCompanyid(request);
                boolean is_attendance=false;
                filename=request.getParameter("url");
                String moduleid=request.getParameter("moduleid");
                Boolean isQuotationSyncedFromCrm = Boolean.parseBoolean(request.getParameter("isQuotationSyncedFromCrm"));    //Quotation is Synched from CRM or not?
//                if(request.getParameterMap().containsKey("attendance")){
//                	is_attendance=Boolean.parseBoolean(request.getParameter("attendance"));
//                	if(is_attendance){
//                		path =  StorageHandler.GetAttendanceDocStorePath() + StorageHandler.GetFileSeparator()+"importattendance"+StorageHandler.GetFileSeparator()+filename;
//                	}else{
//                		path =  StorageHandler.getLeaveMDocPath(request.getParameter("storeindex"))+StorageHandler.GetFileSeparator()+filename;
//                	}
//                }else{
                	//path =  StorageHandler.GetDocStorePath()+StorageHandler.GetFileSeparator()+filename;
            if (!StringUtil.isNullOrEmpty(moduleid) &&  moduleid.equalsIgnoreCase("64")) {//contract moduleid
                path = StorageHandler.GetSharedDocStorePath() + filename;

                boolean check = new File(path).exists();    //Check source file is available in ERP folder or not for old entries which has been not synced in CRM yet
                if (!check) {
                    path = StorageHandler.GetDocStorePath() + filename;
                }

            } else {
                path = StorageHandler.GetDocStorePath() + filename;
            }
            /*----Download path for document synced from CRM----*/  
            if(isQuotationSyncedFromCrm){
                path = StorageHandler.GetSharedDocStorePath() + companyid +"/" +"QuotationDocuments/"+ filename;       //shared locations path to download the required document
            }
              
                //}
                 File fp = new File(path);
                 if(fp.exists()){
                 byte buff[] = new byte[(int) fp.length()];
                 fis = new FileInputStream(fp);
                 int read = fis.read(buff);
                 response.setContentLength((int) fp.length());
                 filename = request.getParameter("docname");
                 filename = filename.replace(" ", "_");
                 response.setHeader("Content-Disposition", request.getParameter("dtype") + "; filename=" + filename + ";");
                 os=response.getOutputStream();
                 os.write(buff);
                 os.flush();
                 }
		} catch(Exception ex){
                //KrawlerLog.op.warn("Unable To Download File :" + ex.toString());                
        }finally{
          if(os!=null){
              os.close();
          }
          if(fis!=null){
              fis.close();
          }
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
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
     * Handles the HTTP <code>POST</code> method.
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
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
