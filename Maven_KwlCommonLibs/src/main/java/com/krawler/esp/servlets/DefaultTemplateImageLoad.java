/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.esp.servlets;

import com.krawler.common.util.KrawlerLog;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author krawler
 */
public class DefaultTemplateImageLoad extends HttpServlet {

    static final int BUFFER = 2048;
    private static final long serialVersionUID = 5547424986127665441L;
    public static final String ImgBasePath = "images/store/";
    private static final String defaultCompanyImgPath = "images/deskera-logo2.png";

    protected void processRequest(HttpServletRequest req,
            HttpServletResponse resp) throws ServletException, IOException {
        try {
            ServletContext sc = getServletContext();
            String uri = req.getRequestURI();
            String servletBase = req.getServletPath();

            boolean Companyflag = (req.getParameter("cid") != null) ? true : false;
            String imagePath = defaultCompanyImgPath;
            String requestedFileName = "";
            if (Companyflag) {
                String companyId = null;
                try {
                    companyId = req.getParameter("cid");
                } catch (Exception ee) {
                }
                if (!StringUtil.isNullOrEmpty(companyId)) {
                    requestedFileName = "/" + companyId + ".png";
                }
            } else {
                requestedFileName = uri.substring(uri.lastIndexOf(servletBase)
                        + servletBase.length());
            }
            String fileName = null;

            fileName = storageHandlerImpl.GetProfileImgStorePath() + requestedFileName;
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
            if (!file.exists()) {
                file = new File(sc.getRealPath(imagePath));
            }

            resp.setContentLength((int) file.length());

            // Open the file and output streams
            FileInputStream in = new FileInputStream(file);
            OutputStream out = resp.getOutputStream();

            // Copy the contents of the file to the output stream
            byte[] buf = new byte[4096];
            int count = 0;
            while ((count = in.read(buf)) >= 0) {
                out.write(buf, 0, count);
            }
            in.close();
            out.close();
        } catch (Exception ex) {
            KrawlerLog.op.warn("Unable To Download File :" + ex.toString());
        }
    }

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    public String getServletInfo() {
        return "Short description";
    }
}
