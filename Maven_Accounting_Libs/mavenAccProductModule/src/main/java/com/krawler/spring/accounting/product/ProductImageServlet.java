/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.product;

import com.krawler.common.util.Constants;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public class ProductImageServlet extends HttpServlet {

    private static final long serialVersionUID = 5547424986127665441L;
    public static final String ImgBasePath = "images/store/";
    private static final String defaultImgPath = "images/defaultuser.png";
    private static final String defaultCompanyImgPath = "images/logo.gif";
    private static final String defaultBlankImgPath = "images/s.gif";

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // Get the absolute path of the image
        ServletContext sc = getServletContext();
        String uri = req.getRequestURI();
        String servletBase = req.getServletPath();

        String requestedFileName = "";

        requestedFileName = uri.substring(uri.lastIndexOf(servletBase)
                + servletBase.length());
        String fileName = null;

        fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+ req.getParameter("fname");
        // Get the MIME type of the image
        String mimeType = sc.getMimeType(fileName);
        if (mimeType == null) {
            sc.log("Could not get MIME type of " + fileName);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Set content type
        resp.setContentType(mimeType);

        // Set content size
        File file = new File(fileName);
//        if (!file.exists()) {
//            if (fileName.contains("_100.")) {
//                file = new File(fileName.replaceAll("_100.", "."));
//            }
//            if (!file.exists()) {
//                file = new File(sc.getRealPath(imagePath));
//            }
//        }

        
        // Open the file and output streams
        FileInputStream in = null; 
        try {
            in = new FileInputStream(file);
        } catch(java.io.FileNotFoundException ex) {
            if(req.getParameter("isDocumentDesignerPrint") != null){
                file = new File(sc.getRealPath(defaultBlankImgPath));
                in = new FileInputStream(file);
            }
        }
        
        resp.setContentLength((int) file.length());
        OutputStream out = resp.getOutputStream();

            // Copy the contents of the file to the output stream
            byte[] buf = new byte[4096];
            int count = 0;
            if(in!=null){
              while ((count = in.read(buf)) >= 0) {
                out.write(buf, 0, count);
              }
                in.close();
            }
                out.flush();
                out.close();
            }
        }
