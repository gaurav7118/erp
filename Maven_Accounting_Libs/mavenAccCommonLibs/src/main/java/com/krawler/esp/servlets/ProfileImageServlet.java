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

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONObject;

public class ProfileImageServlet extends HttpServlet {

    private static final long serialVersionUID = 5547424986127665441L;
    public static final String ImgBasePath = "images/store/";
    private static final String defaultImgPath = "images/defaultuser.png";
    private static final String defaultCompanyImgPath = "images/logo.gif";

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        // Get the absolute path of the image
        ServletContext sc = getServletContext();
        String uri = req.getRequestURI();
        String servletBase = req.getServletPath();

        boolean Companyflag = (req.getParameter("company") != null) ? true : false;
        String imagePath = defaultImgPath;
        String requestedFileName = "";
        if (Companyflag) {
            imagePath = defaultCompanyImgPath;
            String companyId = null;
            try {
                companyId = sessionHandlerImpl.getCompanyid(req);
            } catch (Exception ee) {
            }
            if (StringUtil.isNullOrEmpty(companyId)) {
                String domain = URLUtil.getDomainName(req);
                if (!StringUtil.isNullOrEmpty(domain)) {
                    //@@@ - Uncomment
//					companyId = DBCon.getCompanyid(domain);
                    requestedFileName = "/original_" + companyId + ".png";
                } else {
                    requestedFileName = "logo.gif";
                }
            } else {
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
            if (fileName.contains("_100.")) {
                file = new File(fileName.replaceAll("_100.", "."));
            }
            if (!file.exists()) {
                file = new File(sc.getRealPath(imagePath));
            }
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
    }

    @Deprecated
    public static String getProfileImagePath(HttpServletRequest req, boolean Companyflag, String imgPath) {
        String uri = req.getRequestURI();
        String servletBase = req.getServletPath();

        String imagePath = defaultImgPath;
        String requestedFileName = "";
        if (Companyflag) {
            if (StringUtil.isNullOrEmpty(imgPath)) {
                imagePath = defaultCompanyImgPath;
            } else {
                imagePath = imgPath;
            }
            String companyId = null;
            try {
                companyId = sessionHandlerImpl.getCompanyid(req);
            } catch (Exception ee) {
            }
            if (StringUtil.isNullOrEmpty(companyId)) {
                String domain = URLUtil.getDomainName(req);
                if (!StringUtil.isNullOrEmpty(domain)) {
//					companyId = DBCon.getCompanyid(domain);
                    requestedFileName = "/original_" + companyId + ".png";
                } else {
                    requestedFileName = "logo.gif";
                }
            } else {
                requestedFileName = "/" + companyId + ".png";
            }
        } else {
            requestedFileName = uri.substring(uri.lastIndexOf(servletBase)
                    + servletBase.length());
        }
        String fileName = storageHandlerImpl.GetProfileImgStorePath() + requestedFileName;
        File file = new File(fileName);
        if (!file.exists()) {
            if (fileName.contains("_100.")) {
                file = new File(fileName.replaceAll("_100.", "."));
            }
            if (!file.exists()) {
                if(imagePath.startsWith("/")){
                    file = new File(req.getRealPath(imagePath));
                }
                else{
                    file = new File(req.getRealPath("/"+imagePath));
                }
            }
        }

        return file.getAbsolutePath();
    }
    
    public static String getProfileImagePath(JSONObject jsonObj, boolean Companyflag, String imgPath) {
        String uri = jsonObj.optString(Constants.REQUEST_URI);
        String servletBase = jsonObj.optString(Constants.SERVLET_PATH);

        String imagePath = defaultImgPath;
        String requestedFileName = "";
        if (Companyflag) {
            if (StringUtil.isNullOrEmpty(imgPath)) {
                imagePath = defaultCompanyImgPath;
            } else {
                imagePath = imgPath;
            }
            String companyId = null;
            try {
                companyId = jsonObj.optString(Constants.companyKey);
            } catch (Exception ee) {
            }
            if (StringUtil.isNullOrEmpty(companyId)) {
                String domain = jsonObj.optString("cdomain");
                if (!StringUtil.isNullOrEmpty(domain)) {
//					companyId = DBCon.getCompanyid(domain);
                    requestedFileName = "/original_" + companyId + ".png";
                } else {
                    requestedFileName = "logo.gif";
                }
            } else {
                requestedFileName = "/" + companyId + ".png";
            }
        } else {
            requestedFileName = uri.substring(uri.lastIndexOf(servletBase)
                    + servletBase.length());
        }
        String fileName = storageHandlerImpl.GetProfileImgStorePath() + requestedFileName;
        File file = new File(fileName);
        if (!file.exists()) {
            if (fileName.contains("_100.")) {
                file = new File(fileName.replaceAll("_100.", "."));
            }
            if (!file.exists()) {
                if(imagePath.startsWith("/")){
                    file = new File(jsonObj.optString(Constants.REAL_PATH) + ""+ imagePath);
                    
                }
                else{
                    file = new File(jsonObj.optString(Constants.REAL_PATH) +"/"+ imagePath);
                }
            }
        }
        return file.getAbsolutePath();
    }
}
