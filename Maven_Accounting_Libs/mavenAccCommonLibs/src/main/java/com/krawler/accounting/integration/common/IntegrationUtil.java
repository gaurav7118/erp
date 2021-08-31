/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.accounting.integration.common;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ProtocolException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 *
 * @author krawler
 */
public class IntegrationUtil {

    /**
     * Following method posts a request to a third party REST service and
     * returns the response received from REST Service
     *
     * @params: requestURL -> URL to which request is posted
     * @params: methodType -> Type of request method to be used. Value must be
     * one of "GET", "POST", "DELETE", and "PUT"
     * @params: requestJSON -> JSONObject which contains parameters required by
     * REST service method (used only in case of POST type method)
     * @params: integrationPartyId -> Id of third party to which request is
     * posted. This Id is defined in Constants.java for each third party with
     * which REST Integration exists
     * @params: headersMap -> request headers
     * @params: otherReqPropertiesMap -> request properties other than headers.
     * For example: doOutput
     * @param requestURL
     * @param methodType
     * @param payload
     * @param integrationPartyId
     * @param headersMap
     * @param otherReqPropertiesMap
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject postRequestToRestService(String requestURL, String methodType, JSONObject payload, int integrationPartyId, Map<String, String> headersMap, Map<String, Object> otherReqPropertiesMap) throws JSONException, ServiceException {
        return postRequestToRestService(requestURL, methodType, payload.toString(), integrationPartyId, headersMap, otherReqPropertiesMap);
    }

    /**
     * Following method posts a request to a third party REST service and
     * returns the response received from REST Service
     *
     * @params: requestURL -> URL to which request is posted
     * @params: methodType -> Type of request method to be used. Value must be
     * one of "GET", "POST", "DELETE", and "PUT"
     * @params: payload -> JSONArray which contains parameters required by REST
     * service method (used only in case of POST type method)
     * @params: integrationPartyId -> Id of third party to which request is
     * posted. This Id is defined in Constants.java for each third party with
     * which REST Integration exists
     * @params: headersMap -> request headers
     * @params: otherReqPropertiesMap -> request properties other than headers.
     * For example: doOutput
     * @param requestURL
     * @param methodType
     * @param payload
     * @param integrationPartyId
     * @param headersMap
     * @param otherReqPropertiesMap
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject postRequestToRestService(String requestURL, String methodType, JSONArray payload, int integrationPartyId, Map<String, String> headersMap, Map<String, Object> otherReqPropertiesMap) throws JSONException, ServiceException {
        return postRequestToRestService(requestURL, methodType, payload.toString(), integrationPartyId, headersMap, otherReqPropertiesMap);
    }

    /**
     * Following method posts a request to a third party REST service and
     * returns the response received from REST Service
     *
     * @params: requestURL -> URL to which request is posted
     * @params: methodType -> Type of request method to be used. Value must be
     * one of "GET", "POST", "DELETE", and "PUT"
     * @params: payload -> String which contains parameters required by REST
     * service method (used only in case of POST type method)
     * @params: integrationPartyId -> Id of third party to which request is
     * posted. This Id is defined in Constants.java for each third party with
     * which REST Integration exists
     * @params: headersMap -> request headers
     * @params: otherReqPropertiesMap -> request properties other than headers.
     * For example: doOutput
     * @param requestURL
     * @param methodType
     * @param payload
     * @param integrationPartyId
     * @param headersMap
     * @param otherReqPropertiesMap
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject postRequestToRestService(String requestURL, String methodType, String payload, int integrationPartyId, Map<String, String> headersMap, Map<String, Object> otherReqPropertiesMap) throws JSONException, ServiceException {
        JSONObject returnJobj = new JSONObject();
        String response = "";
        boolean success = false;
        int responseCode = 0;
        try {
            HttpsURLConnection urlConn = null;
            try {
                URL url = new URL(requestURL);
                urlConn = (HttpsURLConnection) url.openConnection(); //Return a URL connection and cast to HttpsURLConnection
            } catch (IOException ex) {
                throw ServiceException.FAILURE("IntegrationUtil.postRequestToRestService : request URL is not correct. Please check request URL.", ex);
            }
            urlConn = setRequestProperties(urlConn, headersMap, otherReqPropertiesMap, methodType);

            if (integrationPartyId == IntegrationConstants.integrationPartyId_UPS || integrationPartyId == IntegrationConstants.integrationPartyId_IRAS) {
                urlConn = updateSSLProtocolVersion(urlConn, "TLSv1.2");//To change SSL protocol version from default 1.0 to 1.2; required by UPS web service
            }

            if (StringUtil.equal(methodType, Constants.POST) || StringUtil.equal(methodType, Constants.PUT)) {//Set request method type and accepted data type for POST/PUT request. Write the parameters JSON to URLConnection
                try (OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream())) {
                    wr.write(payload);
                    wr.flush();
                } catch (IOException ex) {
                    Logger.getLogger(IntegrationUtil.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE("IntegrationUtil.postRequestToRestService : " + ex.getMessage(), ex);
                }
            }

            responseCode = urlConn.getResponseCode();
            if (responseCode < HttpsURLConnection.HTTP_BAD_REQUEST) {
                try (InputStream inStream = urlConn.getInputStream()) {
                    response = convertStreamToString(inStream);                           //Response is stored in result variable
                } catch (IOException ex) {
                    Logger.getLogger(IntegrationUtil.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE("IntegrationUtil.postRequestToRestService : " + ex.getMessage(), ex);
                }
            } else {
                InputStream inStream = urlConn.getErrorStream();                    //get the Error Stream from the URL connection to read response
                response = convertStreamToString(inStream);                           //Response is stored in result variable
            }
            success = true;
        } catch (IOException ex) {
            Logger.getLogger(IntegrationUtil.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
            throw ServiceException.FAILURE("IntegrationUtil.postRequestToRestService : " + ex.getMessage(), ex);
        } finally {
            returnJobj.put(IntegrationConstants.response, response);
            returnJobj.put(IntegrationConstants.responseCode, responseCode);
            returnJobj.put(Constants.RES_success, success);
        }

        return returnJobj;
    }

    /**
     * This method reads data from ImputStream and returns the read data in a
     * string
     *
     * @param inputStream
     * @return
     * @throws ServiceException
     */
    private String convertStreamToString(InputStream inputStream) throws ServiceException {
        String streamString = "";
        try {
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            streamString = scanner.next();
        } catch (NoSuchElementException ex) {
            Logger.getLogger(IntegrationUtil.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("IntegrationUtil.convertStreamToString : " + ex.getMessage(), ex);
        }
        return streamString;
    }

    /**
     * This method updates the SSL protocol version from default v1.0 to v1.2
     *
     * @param urlConn
     * @param sslVersion
     * @return
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     */
    private HttpsURLConnection updateSSLProtocolVersion(HttpsURLConnection urlConn, String sslVersion) throws ServiceException {
        try {
            SSLContext sslc = SSLContext.getInstance(sslVersion);
            TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslc.init(null, new TrustManager[]{tm}, new SecureRandom());
            urlConn.setSSLSocketFactory(sslc.getSocketFactory());
        } catch (NoSuchAlgorithmException | KeyManagementException ex) {
            Logger.getLogger(IntegrationUtil.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("IntegrationUtil.updateSSLProtocolVersion : " + ex.getMessage(), ex);
        }

        return urlConn;
    }

    /**
     * Method to set headers and other request properties in URLConnection
     *
     * @param urlConn
     * @param headersMap
     * @param otherReqPropertiesMap
     * @param methodType
     * @return
     * @throws ProtocolException
     */
    private HttpsURLConnection setRequestProperties(HttpsURLConnection urlConn, Map<String, String> headersMap, Map<String, Object> otherReqPropertiesMap, String methodType) throws ProtocolException {
        if (!StringUtil.isNullOrEmpty(methodType)) {
            urlConn.setRequestMethod(methodType);
        }
        if (headersMap != null) {
            Set<String> headersKeys = headersMap.keySet();
            for (String key : headersKeys) {
                urlConn.setRequestProperty(key, headersMap.get(key));
            }
        }
        if (otherReqPropertiesMap != null) {
            if (otherReqPropertiesMap.containsKey(IntegrationConstants.doOutput) && otherReqPropertiesMap.get(IntegrationConstants.doOutput) != null) {
                urlConn.setDoOutput((boolean) otherReqPropertiesMap.get(IntegrationConstants.doOutput));
            }
        }
        return urlConn;
    }

}
