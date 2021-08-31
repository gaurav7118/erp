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

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
/**
 *
 * @author Krawler
 */
public class APICallHanlderServiceImpl implements APICallHandlerService {

    private static String REMOTE_API_STR = "remoteapi.jsp";
    private APICallHandlerDAO apiCallHandlerDAO;
    private static Log logger = LogFactory.getLog(APICallHanlderServiceImpl.class);

    /**
     * @param apiCallHandlerDAO the apiCallHandlerDAO to set
     */
    public void setApiCallHandlerDAO(APICallHandlerDAO apiCallHandlerDAO) {
        this.apiCallHandlerDAO = apiCallHandlerDAO;
    }

    public JSONObject callApp(String appURL, JSONObject jData, String companyid, String action) {
        JSONObject resObj = new JSONObject();
        boolean result = false;
        try {
            String uid = UUID.randomUUID().toString();
            if(!StringUtil.isNullOrEmpty(companyid)){
                apiCallHandlerDAO.addAPIResponse(uid,action,jData,companyid);
            }
            String res = "{}";
            InputStream iStream = null;
            try {
                String strSandbox = appURL + REMOTE_API_STR;
                URL u = new URL(strSandbox);
                URLConnection uc = u.openConnection();
                uc.setDoOutput(true);
                uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                java.io.PrintWriter pw = new java.io.PrintWriter(uc.getOutputStream());
                
                //Following try-catch to handle Un-supported ASCII character exception.     //VP
                try {
                    pw.println("action=" + action + "&data=" + URLEncoder.encode(jData.toString(), "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    pw.println("action=" + action + "&data=" + jData.toString());
                }
                pw.close();
                iStream = uc.getInputStream();
                java.io.BufferedReader in = new java.io.BufferedReader(new java.io.InputStreamReader(iStream));
                
                //Following try-catch to handle Un-supported ASCII character exception.
                try {
                    res =  StringUtil.DecodeText(in.readLine());
                } catch (UnsupportedEncodingException ex) {
                    res = in.readLine();
                }
                in.close();
                iStream.close();
            } catch (IOException iex) {
                Logger.getLogger(APICallHandlerService.class.getName()).log(Level.SEVERE, "IO Exception In API Call", iex);
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
             if(!StringUtil.isNullOrEmpty(companyid)){
                apiCallHandlerDAO.updateAPIResponse(uid,res);
             }

        } catch (JSONException ex) {
            Logger.getLogger(APICallHandlerService.class.getName()).log(Level.SEVERE, "JSON Exception In API Call", ex);
            result = false;

        } catch (Exception ex) {
            Logger.getLogger(APICallHandlerService.class.getName()).log(Level.SEVERE, "Exception In API Call", ex);
            result = false;
        }
        return resObj;
    }
 
    @Override
    public ByteArrayOutputStream restGetMethodForFile(String endpoint, String inputData) throws JSONException, MalformedURLException, IOException {
        System.out.println("before encode endpoint :: "+inputData);   
        inputData = java.net.URLEncoder.encode(inputData, "UTF-8");
        System.out.println("afetr encode endpoint :: "+inputData);  
        endpoint=endpoint+"?"+Constants.RES_REQUEST+"="+inputData;
        URL url = new URL(endpoint);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod("GET");
        urlConn.setRequestProperty("Content-type", "application/json");
        urlConn.setDoOutput(true);
        urlConn.setDoInput(true);

        InputStream inStream = urlConn.getInputStream();
        int length = 0;
        byte[] buffer = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((length = inStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        if(inStream != null){
            inStream.close();
        }
        return baos;
    }

    @Override
    public JSONObject restGetMethod(String endpoint, String inputData) {
        JSONObject response = new JSONObject();
        try {
            // Code commented due to response size goes out of bound - will be handled later
//            String uid = UUID.randomUUID().toString();
//            JSONObject jobj = null;
//            if(!StringUtil.isNullOrEmpty(inputData)){
//                jobj = new JSONObject(inputData);
//                if(jobj.has(Constants.companyKey)){
//                    apiCallHandlerDAO.addAPIResponse(uid,endpoint,jobj,jobj.getString(Constants.companyKey));
//                }
//            }
            response = restGetMethod(endpoint, inputData, null);
//            if(jobj != null){
//                apiCallHandlerDAO.updateAPIResponse(uid,response.toString());
//            }
        } catch (JSONException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }    

    @Override
    public JSONObject restGetMethod(String endpoint, String inputData, Map<String, String> parameters) throws JSONException, ServiceException {
        StringBuilder resp = new StringBuilder();
        logger.info("before encode endpoint :: " + inputData);
        try{
        inputData = java.net.URLEncoder.encode(inputData, "UTF-8");
        logger.info("after encode endpoint :: " + endpoint);
        endpoint = endpoint + "?" + Constants.RES_REQUEST + "=" + inputData;
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        
        parameters.put(Constants.REST_AUTH_TOKEN, getAuthToken());
        endpoint = StringUtil.appendParametersToURL(endpoint, parameters);

        logger.info("endpoint :: " + endpoint);
System.out.println("**ERP get endpoint-> "+endpoint);
        URL url = new URL(endpoint);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod("GET");
        urlConn.setRequestProperty("Content-type", "application/json");
        urlConn.setDoOutput(true);
        urlConn.setDoInput(true);

        InputStream inStream = urlConn.getInputStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
        String line = null;
        while ((line = in.readLine()) != null) {
            resp.append(line);
        }
        }catch(MalformedURLException ex){
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("URL is not valid", "e15", false);
        } catch (IOException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Error occured while getting response", "e16", false);
        }
        System.out.println("ERP get response: " + resp);
        return new JSONObject(resp.toString());
    }

    @Override
    public JSONObject restDeleteMethod(String endpoint, String inputData) {
        JSONObject response = new JSONObject();
        try {
            // Code commented due to response size goes out of bound - will be handled later
//            String uid = UUID.randomUUID().toString();
//            JSONObject jobj = null;
//            if(!StringUtil.isNullOrEmpty(inputData)){
//                jobj = new JSONObject(inputData);
//                if(jobj.has(Constants.companyKey)){
//                    apiCallHandlerDAO.addAPIResponse(uid,endpoint,jobj,jobj.getString(Constants.companyKey));
//                }
//            }            
            response = restDeleteMethod(endpoint, inputData, null);
//            if(jobj != null){
//                apiCallHandlerDAO.updateAPIResponse(uid,response.toString());
//            }            
        } catch (JSONException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    @Override
    public JSONObject restDeleteMethod(String endpoint, String inputData, Map<String, String> parameters) throws JSONException, ServiceException {
        StringBuilder resp = new StringBuilder();
        logger.info("before encode endpoint :: " + inputData);
        try{
        inputData = java.net.URLEncoder.encode(inputData, "UTF-8");
        logger.info("after encode endpoint :: " + endpoint);
        endpoint = endpoint + "?" + Constants.RES_REQUEST + "=" + inputData;
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        
        parameters.put(Constants.REST_AUTH_TOKEN, getAuthToken());
        endpoint = StringUtil.appendParametersToURL(endpoint, parameters);
        logger.info("endpoint :: " + endpoint);
        System.out.println("**ERP delete endpoint-> "+endpoint);
        URL url = new URL(endpoint);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod("DELETE");
        urlConn.setRequestProperty("Content-type", "application/json");
        urlConn.setDoOutput(true);
        urlConn.setDoInput(true);

        InputStream inStream = urlConn.getInputStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
        String line = null;
        while ((line = in.readLine()) != null) {
            resp.append(line);
        }
        }catch(MalformedURLException ex){
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("URL is not valid", "e15", false);
        } catch (IOException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Error occured while getting response", "e16", false);
        }
        System.out.println("ERP delete response: " + resp);
        return new JSONObject(resp.toString());
    }

    @Override
    public JSONObject restPostMethod(String endpoint, String inputData) {
        JSONObject response = new JSONObject();
        try {
            // Code commented due to response size goes out of bound - will be handled later
//            String uid = UUID.randomUUID().toString();
//            JSONObject jobj = null;
//            if(!StringUtil.isNullOrEmpty(inputData)){
//                jobj = new JSONObject(inputData);
//                if(jobj.has(Constants.companyKey)){
//                    apiCallHandlerDAO.addAPIResponse(uid,endpoint,jobj,jobj.getString(Constants.companyKey));
//                }
//            }             
            response = restPostMethod(endpoint, inputData, null);
//            if(jobj != null){
//                apiCallHandlerDAO.updateAPIResponse(uid,response.toString());
//            }            
        } catch (JSONException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }     

    @Override
    public JSONObject restPostMethod(String endpoint, String inputData, Map<String, String> parameters) throws JSONException, ServiceException {
        StringBuilder resp = new StringBuilder();
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        
        try{
            parameters.put(Constants.REST_AUTH_TOKEN, getAuthToken());
        endpoint = StringUtil.appendParametersToURL(endpoint, parameters);
        URL url = new URL(endpoint);
            System.out.println("**ERP post endpoint-> "+endpoint);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setRequestProperty("Content-type", "application/json");
        urlConn.setDoOutput(true);
        urlConn.setDoInput(true);
        OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
        wr.write(inputData);
        wr.flush();

        InputStream inStream = urlConn.getInputStream();

        BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
        String line = null;
        while ((line = in.readLine()) != null) {
            resp.append(line);
        }
        }catch(MalformedURLException ex){
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("URL is not valid", "e15", false);
        } catch (IOException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Error occured while getting response", "e16", false);
        }
        System.out.println("ERP post response: " + resp);
        return new JSONObject(resp.toString());
    }

    private String getAuthToken() throws UnsupportedEncodingException, MalformedURLException, IOException, JSONException {
        String token = null;
            String restauthapply = ConfigReader.getinstance().get("restauthapply");
            if (!StringUtil.isNullOrEmpty(restauthapply) && restauthapply.equals("1")) {
                StringBuilder resp = new StringBuilder();
                String endpoint = URLUtil.buildRestURL(Constants.PLATFORM_URL) + "company/token";
                String clientId = ConfigReader.getinstance().get(Constants.CLIENT_ID);
                String clientSecret = ConfigReader.getinstance().get(Constants.CLIENT_SECRET);
                endpoint = endpoint + "?" + Constants.CLIENT_ID + "=" + clientId + "&" + Constants.CLIENT_SECRET + "=" + clientSecret;
//        endpoint = java.net.URLEncoder.encode(endpoint, "UTF-8");
                System.out.println("getAuthToken endpoint-> " + endpoint);
                URL url = new URL(endpoint);
                HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setRequestMethod("GET");
                urlConn.setRequestProperty("Content-type", "application/json");
                urlConn.setDoOutput(true);
                urlConn.setDoInput(true);

                InputStream inStream = urlConn.getInputStream();

                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);
                JSONObject jobj = new JSONObject(resp.toString());
                if (jobj.has(Constants.REST_AUTH_TOKEN)) {
                    token = jobj.getString(Constants.REST_AUTH_TOKEN);
                }
            }        
        return token;
    }

    @Override
    public ByteArrayOutputStream restPostMethodForBAOS(String endpoint, String inputData) throws JSONException {
        ByteArrayOutputStream baos = null;
        Map<String, String> parameters = new HashMap<String, String>();
        try{
        parameters.put(Constants.REST_AUTH_TOKEN, getAuthToken());
        endpoint = StringUtil.appendParametersToURL(endpoint, parameters);
        URL url = new URL(endpoint);
        System.out.println("**ERP post endpoint-> " + endpoint);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod("POST");
        urlConn.setRequestProperty("Content-type", "application/json");
        urlConn.setDoOutput(true);
        urlConn.setDoInput(true);
        OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
        wr.write(inputData);
        wr.flush();
        System.out.println("APICallHandlerServiceImpl.restPostMethodForBAOS() call started....");
        InputStream inStream = urlConn.getInputStream();
        int length = 0;
        byte[] buffer = new byte[4096];
        baos = new ByteArrayOutputStream();
        while ((length = inStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        if (inStream != null) {
            inStream.close();
        }
        System.out.println("APICallHandlerServiceImpl.restPostMethodForBAOS() call ended....");
        } catch(MalformedURLException ex){
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return baos;
    }
}