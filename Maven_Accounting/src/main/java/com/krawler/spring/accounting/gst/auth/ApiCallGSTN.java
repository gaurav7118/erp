/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.gst.auth;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.APICallHanlderServiceImpl;
import com.krawler.esp.utils.ConfigReader;
import static com.krawler.spring.accounting.gst.auth.AESEncryption.decrypt;
import static com.krawler.spring.accounting.gst.auth.AESEncryption.encodeBase64String;
import static com.krawler.spring.accounting.gst.auth.AESEncryption.decodeBase64StringTOByte;
import static com.krawler.spring.accounting.gst.auth.AESEncryption.encryptOTP;
import com.krawler.spring.accounting.gst.services.GSTRConstants;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 *
 * @author krawler
 */
public class ApiCallGSTN extends BaseDAO {

//    private static final String ASPID = "l7xxb46ccb7662284129ae2c5ee2f20001d4";
//    private static final String ASPSECRET = "5649bfa3f36549d5b2d3dd5765501617";
//    private static final String USERNAME = "nsdl.mh.2";
//    private static final String GSTCONFIGID = "402880a35e03c067015e03e677ad0001";
//    private static final String ASPID = "27AACCK5779R034790";
//    private static final String ASPSECRET = "518wd05a9Ej16H5U0q09Y1gKj82C9SjI";
//    private static final String USERNAME = "AACCK5779R-2005";
    private static final String GSTCONFIGID = "402880a35eb8c2db015eb8f539ad0001";

    public static void main(String[] args) {
        ApiCallGSTN api = new ApiCallGSTN();
        try {
//            api.getKey();
//            api.saveGstConfig();
//            api.getOtpRequest();
            JSONObject j = new JSONObject("{\"enc_key\":\"zyrtDpwrkKNc8/1FDRPGYJ3ZC/5mkL9oMJLpIE5Fy+eJ4W1Z1tnGk1aFKhBp5rvs\",\"session_id\":\"5175UBT4JRMUKDQIUK11506341753207\",\"message_id\":\"66d8804c-90ef-48f6-9bd2-a52e42ec961b\",\"message\":\"Signature Matching\",\"status_cd\":1}");
            api.getAuthToken(j);
//            api.saveGstConfig();
        } catch (JSONException ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JSONObject getKey() throws JSONException, ServiceException {

        String endpoint = GSTRConstants.baseURL + GSTRConstants.getKey;

        
        Map<String, String> header = new HashMap<String, String>();
        header.put("aspid", ConfigReader.getinstance().get("aspid"));
        header.put("message-id", UUID.randomUUID().toString());

        JSONObject inputData = new JSONObject();
        String timestamp = "";
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMYYYYHHmmssSSSSSS");
        Date d = new Date();
        Timestamp ts = new Timestamp(d.getTime());
        System.out.println("" + ts.toString());

        timestamp = sdf.format(d);
        inputData.put("timestamp", timestamp);
        System.out.println("timestamp:  " + timestamp);
//        GstConfig config = loadGstConfig();
        try {
            inputData.put("signed_content", new Encryptor().generateSignature(ConfigReader.getinstance().get("aspid") + timestamp));
//            inputData.put("signed_content", new Encryptor().generateSignature(ConfigReader.getinstance().get("aspid") + timestamp, config));
        } catch (Exception ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        }
	System.out.println("inputData : " + inputData);
        JSONObject response = restMethod(endpoint, inputData.toString(), header, GSTRConstants.POST);
//        response.put("gstconfig", config);
        return  response;
//        return null;
    }

    public JSONObject getOtpRequest() throws JSONException, ServiceException {
        JSONObject response = new JSONObject();

        try {
            JSONObject getKeyJson = getKey();
//            GstConfig config = (GstConfig)getKeyJson.get("gstconfig");
            if (StringUtil.isNullObject(getKeyJson)) {
                return new JSONObject();
            }
            System.out.println("getKeyJson :: " + getKeyJson.toString());
            String endpoint = GSTRConstants.baseURL + GSTRConstants.AUTHENTICATE;

            String encKey = getKeyJson.getString("enc_key");
//            String asp_secret = config.getAspSecret();
            String asp_secret = ConfigReader.getinstance().get("asp-secret");
            byte[] enc_key = decrypt(encKey, asp_secret.getBytes());

            String enc_asp_secret = AESEncryption.encryptEK(asp_secret.getBytes(), decodeBase64StringTOByte(encodeBase64String(enc_key)));

            Map<String, String> header = new HashMap<String, String>();
            header.put("aspid", ConfigReader.getinstance().get("aspid"));
            header.put("message-id", "1234567");
//            header.put("message-id", UUID.randomUUID().toString());
            header.put("asp-secret", enc_asp_secret);
            header.put("state-cd", ConfigReader.getinstance().get("state-cd"));
            header.put("ip-usr", ConfigReader.getinstance().get("ip"));
            header.put("txn", "1234");
            header.put("filler1", "filler1");
            header.put("filler2", "filler2");
            header.put("Content-Type", GSTRConstants.contentType);
            header.put("session-id", getKeyJson.getString("session_id"));

            JSONObject inputData = new JSONObject();
            inputData.put("action", "OTPREQUEST");
            inputData.put("username", ConfigReader.getinstance().get("username"));
            inputData.put("app_key", GSTRConstants.encryptAppKey);

            response = restMethod(endpoint, inputData.toString(), header, GSTRConstants.POST);

        } catch (IOException ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    public JSONObject getAuthToken(JSONObject getKeyJson) throws JSONException, ServiceException {
        try {
//            JSONObject getKeyJson = getKey();
//            if (StringUtil.isNullObject(getKeyJson)) {
//                return new JSONObject();
//            }
            System.out.println("getKeyJson :: " + getKeyJson.toString());
            String endpoint = GSTRConstants.baseURL + GSTRConstants.AUTHENTICATE;

            String encKey = getKeyJson.getString("enc_key");
            String asp_secret = GSTRConstants.aspSecret;
            byte[] enc_key = decrypt(encKey, asp_secret.getBytes());

            String enc_asp_secret = AESEncryption.encryptEK(asp_secret.getBytes(), decodeBase64StringTOByte(encodeBase64String(enc_key)));

            Map<String, String> header = new HashMap<String, String>();
            header.put("aspid", GSTRConstants.aspId);
            header.put("message-id", UUID.randomUUID().toString());
            header.put("asp-secret", enc_asp_secret);
            header.put("state-cd", ConfigReader.getinstance().get("state-cd"));
            header.put("ip-usr", ConfigReader.getinstance().get("ip"));
            header.put("txn", "1234");
            header.put("Content-Type", GSTRConstants.contentType);
            header.put("session-id", getKeyJson.getString("session_id"));
            header.put("filler1", "filler1");
            header.put("filler2", "filler2");
            
            JSONObject inputData = new JSONObject();
            inputData.put("action", "AUTHTOKEN");
            inputData.put("username", GSTRConstants.username);
            inputData.put("app_key", GSTRConstants.encryptAppKey);
//            inputData.put("app_key", "HmD/EB7zYQL+IvaqMjS051fTqlD0bcsWLTBNBqFiQ6EJfEAtdfLtAFUPcpydmeqtJU4tXEEh3UT98YqieNICjLpxuX1u0go9GeVDI7s1opxWjJ0+sgS0nxtvJqmHiyYDRsoaBgNv9G/QSMrA6mMoUr4ZbaodXZCJljI/gGExEgujP7EstbDi53RHKaeB3ldOnZeYMDOR2bzFuRmrw5a2USLvSALBvBdTYAwL/EXXBEca60B5iIjZEvaslI1mdAqcFNNpg7JOWzcosFHBElQettQsB4CYl5DDHbId+HvUlfWaCt6DSbgAGe7IC/JxNnmeUy/3+4hu8AskSLRoLyf9IQ==");
            inputData.put("otp", encryptOTP("258953")); // otp got from gst in mobile/email

            restMethod(endpoint, inputData.toString(), header, GSTRConstants.POST);

        } catch (IOException ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONObject();
    }

    public JSONObject restMethod(String endpoint, String inputData, Map<String, String> headers, String requestMethodType) throws JSONException, ServiceException {
        StringBuilder resp = new StringBuilder();
        try {
            URL url = new URL(endpoint);
            System.out.println("inputData : " + inputData);
            System.out.println("**ERP post endpoint-> " + endpoint);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod(requestMethodType);
//            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-type", "application/json");
            if (!StringUtil.isNullObject(headers)) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    System.out.println("Key : " + entry.getKey() + "    value : " + entry.getValue());
                    urlConn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            if (!requestMethodType.contains("GET")) {
                OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
                wr.write(inputData);
                wr.flush();
            }
            int responseCode = urlConn.getResponseCode();
            System.out.println("Response Code : " + responseCode);

            InputStream inStream = urlConn.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
            String line = null;
            while ((line = in.readLine()) != null) {
                resp.append(line);
            }
        } catch (MalformedURLException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("URL is not valid", "e15", false);
        } catch (IOException ex) {
            Logger.getLogger(APICallHanlderServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Error occured while getting response", "e16", false);
        }
        System.out.println("ERP post response: " + resp);
        return new JSONObject(resp.toString());
    }

//    public void saveGstConfig() throws ServiceException {
//        GstConfig gstConfig = new GstConfig();
//        try {
//            gstConfig.setID(UUID.randomUUID().toString());
//            gstConfig.setUserName(GSTRConstants.username);
//            gstConfig.setAspID(GSTRConstants.aspId);
//            gstConfig.setAspSecret(GSTRConstants.aspSecret);
//            gstConfig.setJksfile(readBytesFromFile("/home/krawler/NetBeansProjects/GSTpoc/Keys/keystore.jks"));
//            gstConfig.setJkspassword("123456");
//            gstConfig.setPfxfile(readBytesFromFile("/home/krawler/NetBeansProjects/GSTpoc/Keys/keystore.pfx"));
//            gstConfig.setPfxpassword("123456");
//            gstConfig.setCerfile(readBytesFromFile("/home/krawler/abhi/work/GST/authentication/GST_Sandbox_Public_key/GSTN_G2A_SANDBOX_UAT_public.cer"));
//            save(gstConfig);
//        } catch (IOException ex) {
//            Logger.getLogger(ApiCallGSTN.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

    private byte[] readBytesFromFile(String filePath) throws IOException {

        File inputFile = new File(filePath);
        FileInputStream inputStream = new FileInputStream(inputFile);
        byte[] fileBytes = new byte[(int) inputFile.length()];
        inputStream.read(fileBytes);
        inputStream.close();
        return fileBytes;
    }
    
//    private GstConfig loadGstConfig(){
//        GstConfig config = (GstConfig) get(GstConfig.class, GSTCONFIGID);
//        return config;
//    }
    
}
