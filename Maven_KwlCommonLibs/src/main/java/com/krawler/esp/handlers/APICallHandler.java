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
package com.krawler.esp.handlers;

import com.krawler.common.admin.Apiresponse;
import com.krawler.common.admin.Company;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.PreparedStatement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateTemplate;

public class APICallHandler {

    private static String apistr = "remoteapi.jsp";

    public static JSONObject callApp(Session session, String appURL, JSONObject jData, String companyid, String action) {
        JSONObject resObj = new JSONObject();
        boolean result = false;
        try {
            PreparedStatement pstmt = null;
            String uid = UUID.randomUUID().toString();
            Apiresponse apires = new Apiresponse();
            apires.setApiid(uid);
            apires.setCompanyid((Company) session.get(Company.class, companyid));
            apires.setApirequest("action=" + action + "&data=" + jData.toString());
            apires.setStatus(0);
            session.save(apires);
            apires=null; //ERP-18753
            String res = "{}";
            InputStream iStream = null;
            try {
                String strSandbox = appURL + apistr;
                URL u = new URL(strSandbox);
                URLConnection uc = u.openConnection();
                uc.setDoOutput(true);
                uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
                pw.println("action=" + action + "&data=" + URLEncoder.encode(jData.toString()));
                pw.close();
                iStream = uc.getInputStream();
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
                res = StringUtil.DecodeText(in.readLine());
                in.close();
                iStream.close();
            } catch (IOException iex) {
                Logger.getLogger(APICallHandler.class.getName()).log(Level.SEVERE, "IO Exception In API Call", iex);
            } finally {
                if (iStream != null) {
                    try {
                        iStream.close();
                    } catch (Exception e) {
                    }
                }
            }
            resObj = new JSONObject(res);

            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                result = true;

            } else {
                result = false;
            }

             apires = (Apiresponse) session.load(Apiresponse.class, uid);
            apires.setApiresponse(res);
            apires.setStatus(1);
            session.save(apires);

        } catch (JSONException ex) {
            Logger.getLogger(APICallHandler.class.getName()).log(Level.SEVERE, "JSON Exception In API Call", ex);
            result = false;

        } catch (Exception ex) {
            Logger.getLogger(APICallHandler.class.getName()).log(Level.SEVERE, "Exception In API Call", ex);
            result = false;
        }
        return resObj;
    }

    public static JSONObject callApp(HibernateTemplate hibernateTemplate, String appURL, JSONObject jData, String companyid, String action) {
        JSONObject resObj = new JSONObject();
        boolean result = false;
        try {
            PreparedStatement pstmt = null;
            String uid = UUID.randomUUID().toString();
            Apiresponse apires = new Apiresponse();
            apires.setApiid(uid);
            apires.setCompanyid((Company) hibernateTemplate.get(Company.class, companyid));
            apires.setApirequest("action=" + action + "&data=" + jData.toString());
            apires.setStatus(0);
            hibernateTemplate.save(apires);

            String res = "{}";
            InputStream iStream = null;
            try {
                String strSandbox = appURL + apistr;
                URL u = new URL(strSandbox);
                URLConnection uc = u.openConnection();
                uc.setDoOutput(true);
                uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
                pw.println("action=" + action + "&data=" + jData.toString());
                pw.close();
                iStream = uc.getInputStream();
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
                res = in.readLine();
                in.close();
                iStream.close();
            } catch (IOException iex) {
                Logger.getLogger(APICallHandler.class.getName()).log(Level.SEVERE, "IO Exception In API Call", iex);
            } finally {
                if (iStream != null) {
                    try {
                        iStream.close();
                    } catch (Exception e) {
                    }
                }
            }
            resObj = new JSONObject(res);

            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                result = true;

            } else {
                result = false;
            }

            apires = (Apiresponse) hibernateTemplate.load(Apiresponse.class, uid);
            apires.setApiresponse(res);
            apires.setStatus(1);
            hibernateTemplate.save(apires);

        } catch (JSONException ex) {
            Logger.getLogger(APICallHandler.class.getName()).log(Level.SEVERE, "JSON Exception In API Call", ex);
            result = false;

        } catch (Exception ex) {
            Logger.getLogger(APICallHandler.class.getName()).log(Level.SEVERE, "Exception In API Call", ex);
            result = false;
        }
        return resObj;
    }
}