/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.APICallHandler;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.APICallHanlderServiceImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.compression.CompressionCodecs;
import java.net.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author krawler
 */
public class TestTBD {

    public static void main(String[] args) throws JSONException {
        JSONObject accjson = new JSONObject();
        accjson.put("currencyCode", "USD");
        accjson.put("cdomain", "erptest01");
        accjson.put("userName", "admin");
        accjson.put("CashAndInvoice", true);
        accjson.put("nondeleted", true);
//            System.out.println(accjson);

        TestTBD t = new TestTBD();
        Locale locale = Locale.forLanguageTag("en-US");
        try {
            //            System.out.println("*******"+locale.getCountry());
//        t.testSaveCustomer();
//        generateToken("123");
//        t.testGetCompany();
//        t.createCompany();
            createCompanyFIN();
        } catch (ServiceException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static JSONObject createCompanyFIN() throws JSONException, ServiceException {
        JSONObject jobj = new JSONObject();
        jobj.put("lname", "q");
        jobj.put("userid", "83a4c0de-7d4a-43a3-ac2a-822b39b74b3a");
        jobj.put("iscommit", true);
        jobj.put("password", "7110eda4d09e062aa5e4a390b0a572ac0d2c0220");
        jobj.put("companyname", "rt3");
        jobj.put("currency", 1);
        jobj.put("country", "244");
        jobj.put("emailid", "abhijit.hibare@deskera.com");
        jobj.put("companyid", "8eba0624-f651-4188-a562-3c0441c3c201");
        jobj.put("timezone", 23);
        jobj.put("username", "admin");
        jobj.put("createnew", true);
        jobj.put("creatorid", "83a4c0de-7d4a-43a3-ac2a-822b39b74b3a");
        jobj.put("email", "abhijit.hibare@deskera.com");
        jobj.put("subdomain", "rt3");
        jobj.put("referralkey", 0);
        jobj.put("remoteapikey", "krawler");
        jobj.put("fname", "q");
        
        APICallHandlerService api = new APICallHanlderServiceImpl();

        JSONObject response = api.restPostMethod("http://192.168.0.35:8080/Accounting/rest/v1/company", jobj.toString());
//        JSONObject response = APICallHandler.restGetMethod("http://192.168.0.32:8096/HQLCrm/rest/v1/company/isexist", jobj.toString());
        return response;
    }

    private void testGetCompany() {
        String endpoint = URLUtil.buildRestURL("accURL");
        endpoint += "company/isexist";
        JSONObject data = new JSONObject();
        try {
            data.put("cdomain", "rahulerp26");
        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        endpoint +="?request="+data.toString();
        System.out.println("final endpoint -> "+endpoint);
        postData(endpoint, data.toString(), "GET");
    }

    private static String generateToken(String id) {
        Map<String, Object> claims = new HashMap<String, Object>();
//        claims.put("cid", id);

        claims.put("cdomain", "singapore10");
//        claims.put("id", "fffaf1d5-11c1-40e9-b832-01d86040ea0a");
//        claims.put("userid", "fffaf1d5-11c1-40e9-b832-01d86040ea0a");
        Date date = new Date();
        String key = "krawler";

        long exp = date.getTime() + 600000;
        Date expD = new Date(exp);
        String compactJws = Jwts.builder()
                //                .setSubject("Joe")
                .setClaims(claims)
                .setExpiration(expD)
                .compressWith(CompressionCodecs.DEFLATE)
                .signWith(SignatureAlgorithm.HS256, key.getBytes())
                .compact();
        System.out.println(compactJws);
        return compactJws;
    }
    
    public void testDeleteProductReplacement() {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "rahulerp1");
            JSONArray maintenanceIdArr = new JSONArray();
            maintenanceIdArr.put("aa802f5d-f834-4cd1-a211-893fcd6594c3");
            inputJsonObj.put("maintainanceids", maintenanceIdArr);
            System.out.println("request: " + inputJsonObj);
            String endpoint = "http://localhost:8084/Accounting/rest/master/productMaintenance?request=" + inputJsonObj.toString();
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("DELETE");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);

            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }
            System.out.println(urlConn.getResponseCode());

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSaveProductReplacement() throws JSONException {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "rahulerp1");
            inputJsonObj.put("replacemenetno", "REP654321");
            inputJsonObj.put("accountid", "8e8e13bd-e6db-4c0c-aa22-2743d5e9c0cb");
            inputJsonObj.put("contractid", "402880494ec9f8eb014eca1ebb9d0005");
//            inputJsonObj.put("replacementid", "19801da4-0509-4d2c-a712-07b899fd259d");
            JSONArray productDataArr = new JSONArray();
            JSONObject productDataObj = new JSONObject();
            JSONArray serialArr = new JSONArray();
            JSONObject serialObj = new JSONObject();
            serialObj.put("serialno", "12");
            serialArr.put(serialObj);
            productDataObj.put("productid", "402880494e04e038014e0514b41c0003");
            productDataObj.put("qty", 3);
            productDataObj.put("serialnodata", serialArr);
            productDataArr.put(productDataObj);
            inputJsonObj.put("productdata", productDataArr);

            String endpoint = "http://localhost:8084/Accounting/rest/master/productReplacement";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testDeleteProductMaintenance() {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "rahulerp1");
            JSONArray maintenanceIdArr = new JSONArray();
            maintenanceIdArr.put("aa802f5d-f834-4cd1-a211-893fcd6594c3");
            inputJsonObj.put("maintainanceids", maintenanceIdArr);
            System.out.println("request: " + inputJsonObj);
            String endpoint = "http://localhost:8084/Accounting/rest/master/productMaintenance?request=" + inputJsonObj.toString();
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("DELETE");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);

            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }
            System.out.println(urlConn.getResponseCode());

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSaveProductMaintenance() throws JSONException {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "rahulerp1");
            inputJsonObj.put("maintenancenumber", "654321");
            inputJsonObj.put("maintainanceamt", "1500");
            inputJsonObj.put("accountid", "8e8e13bd-e6db-4c0c-aa22-2743d5e9c0cb");
            inputJsonObj.put("contractid", "402880494ec9f8eb014eca1ebb9d0005");

            String endpoint = "http://localhost:8084/Accounting/rest/master/product-maintenance";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSaveCustomerAccountId() throws JSONException {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "erptest01");
            inputJsonObj.put("onlycrmaccountid", true);
            JSONArray dataArr = new JSONArray();
            JSONObject dataObj = new JSONObject();
            dataObj.put("customerid", "402880a0547fd1d201547fd48e500001");
            dataObj.put("crmaccountid", "1234578");
            dataArr.put(dataObj);
            dataObj = new JSONObject();
            dataObj.put("customerid", "402880a0547fd1d201547fdb1f1f0007");
            dataObj.put("crmaccountid", "654321");
            dataArr.put(dataObj);
            inputJsonObj.put("data", dataArr);
            System.out.println(inputJsonObj);

            String endpoint = "http://localhost:8084/Accounting/rest/master/customer";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSaveCustomer() throws JSONException {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            JSONObject obj = new JSONObject();
            inputJsonObj.put("cdomain", "rindia01");
            obj.put("dateformat", "yyyy-MM-dd");
            obj.put("name", "KapilGupta22");
//            inputJsonObj.put("address", "Yerawada");
//            inputJsonObj.put("email", "KapilG@test12345.com");
//            inputJsonObj.put("contactno", "123456789");
            obj.put("accountcode", "KapilGUPTA22");
//            inputJsonObj.put("isVendor", false);
            obj.put("accountcreationdate", "2016-06-09");
            JSONArray arr = new JSONArray();
            arr.put(obj);
            inputJsonObj.put("data", arr);

            String endpoint = "http://localhost:8084/Accounting/rest/master/customer";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testDeleteQuotation() {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "testjune");
            JSONArray quotationIdArr = new JSONArray();
            quotationIdArr.put("ea7870f6-6b8f-43e6-8e9f-962488ed9875");
            inputJsonObj.put("quotationids", quotationIdArr);
            System.out.println("request: " + inputJsonObj);
            String endpoint = "http://localhost:8084/Accounting/rest/transaction/quotation?request=" + inputJsonObj.toString();
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("DELETE");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);

            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }
            System.out.println(urlConn.getResponseCode());

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSaveJEOld() throws JSONException {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "erptest01");
            inputJsonObj.put("currencyCode", "USD");
            inputJsonObj.put("language", "en-US");
            JSONObject dataObj = new JSONObject();
            JSONArray jedata = new JSONArray();
            JSONObject datamap = new JSONObject();
            JSONArray details = new JSONArray();
            JSONObject j = new JSONObject();
            j.put("amount", "1011");
            j.put("accountid", "ff80808153ea18f40153ea220181007c");
            j.put("debit", true);
            j.put("description", "TEST rest10");
            details.put(j);
            j = new JSONObject();
            j.put("amount", "1011");
            j.put("accountid", "ff80808153ea18f40153ea220189008a");
            j.put("debit", false);
            j.put("description", "TEST rest10");
            details.put(j);
            datamap.put("details", details);
            jedata.put(datamap);
            dataObj.put("jedata", jedata);
            inputJsonObj.put("data", dataObj);

            String endpoint = "http://localhost:8084/Accounting/rest/transaction/journalentry";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSaveJE() throws JSONException {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "erptest01");
//            inputJsonObj.put("language", "en-US");
            inputJsonObj.put("userName", "admin");
            inputJsonObj.put("currencyCode", "USD");
//            inputJsonObj.put("byCompanyPreference", true);
            JSONArray jedata = new JSONArray();
            JSONObject datamap = new JSONObject();
            datamap = new JSONObject();
            datamap.put("amount", "2013");
            datamap.put("description", "TEST rest company same structure");
            datamap.put("accountid", "ff80808153ea18f40153ea220181007c");
            datamap.put("debit", true);
            jedata.put(datamap);
            datamap = new JSONObject();
            datamap = new JSONObject();
            datamap.put("amount", "2013");
            datamap.put("description", "TEST rest company same structure");
            datamap.put("accountid", "ff80808153ea18f40153ea220189008a");
            datamap.put("debit", false);
            jedata.put(datamap);
            inputJsonObj.put("jedata", jedata);

            String endpoint = "http://localhost:8084/Accounting/rest/transaction/journalentry";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSaveJECompanyPreference() throws JSONException {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "erptest01");
            inputJsonObj.put("language", "en-US");
            inputJsonObj.put("currencyCode", "USD");
            inputJsonObj.put("byCompanyPreference", true);
            JSONArray jedata = new JSONArray();
            JSONObject datamap = new JSONObject();
            datamap = new JSONObject();
            datamap.put("amount", "1020");
            datamap.put("description", "TEST rest company");
            jedata.put(datamap);
            inputJsonObj.put("jedata", jedata);
            String endpoint = "http://localhost:8084/Accounting/rest/transaction/journalentry";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testGetPaymentmethod() {
        try {
            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "erptest01");
            JSONArray jarr = new JSONArray();
            for (int i = 0; i < 10; i++) {
                JSONObject obj = new JSONObject();
                obj.put("id", "ABC-" + i);
                jarr.put(obj);

            }
            inputJsonObj.put("array", jarr);
            String endpoint = "http://localhost:8084/Accounting/rest/transaction/payment-method?request=" + inputJsonObj.toString();
//            System.out.println(inputJsonObj);
            postData(endpoint, null, "GET");
        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void postData(String endpoint, String data, String type) {
        try {
            URL url = new URL(endpoint);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod(type);
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            if (!type.equals("GET")) {
                OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());

                wr.write(data);
                wr.flush();
            }
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);

            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testGetInvoice() {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("accid", "ff8080814e7b9e3a014e86c2b0cf01bd");
            inputJsonObj.put("nondeleted", true);
            inputJsonObj.put("currencyfilterfortrans", "6");
            inputJsonObj.put("upperLimitDate", "Apr 12, 2016 12:00:00 AM");
            inputJsonObj.put("includeFixedAssetInvoicesFlag", true);
            inputJsonObj.put("filterForClaimedDateForPayment", true);
            inputJsonObj.put("isReceipt", true);
            inputJsonObj.put("startdate", "Dec 31, 2015 12:00:00 AM");
            inputJsonObj.put("enddate", "Apr 12, 2016 12:00:00 AM");

            String endpoint = "http://localhost:8084/Accounting/rest/transaction/invoice";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "GET");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testSaveDFInvoice() throws JSONException {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "erptest01");
//            inputJsonObj.put("language", "en-US");
//            inputJsonObj.put("currencyCode", "USD");
//            inputJsonObj.put("userName", "admin");
//            JSONArray invDataArr = new JSONArray();
//            JSONObject detailObj = new JSONObject();
            JSONArray detailArr = new JSONArray();
            JSONObject detail1Obj = new JSONObject();
//            detail1Obj.put("amountwithouttax", 2000);
//            detail1Obj.put("currencyrate", "1.0");
            detail1Obj.put("productid", "ff80808153f089910153f0a1b7cb0004");
//            detail1Obj.put("currencysymbol", "SGD");
//            detail1Obj.put("copybaseuomrate", "0.0");
//            detail1Obj.put("baseuomquantity", "1");
//            detail1Obj.put("prtaxname", "Sale+Tax");
//            detail1Obj.put("taxpercent", "0");
//            detail1Obj.put("amount", 2000);
//            detail1Obj.put("rateinbase",2000);
            detail1Obj.put("rate", 2900);
//            detail1Obj.put("taxamount", 0);
//            detail1Obj.put("amountwithtax", 2000);
//            detail1Obj.put("prtaxid", "42fef3ec-5aba-45c1-bf87-b52d59c7e67f");
//            detail1Obj.put("prdiscount", 0);
//            detail1Obj.put("discountispercent", "0");
//            detail1Obj.put("discamount", "0.0");
//            detail1Obj.put("productname", "Course+registration");
            inputJsonObj.put("incash", true);
            detailArr.put(detail1Obj);
            inputJsonObj.put("detail", detailArr);
//            inputJsonObj.put("memo", "Invoice(test)+for+course");
//            inputJsonObj.put("invoiceid", "402880a0541375740154139090510006");
//            inputJsonObj.put("billdateStr", "2016-04-13");
            inputJsonObj.put("userName", "admin");
//            inputJsonObj.put("totalSUM", 2000);
//            inputJsonObj.put("transactiondateStr", "2016-04-13");
            inputJsonObj.put("transactiondate", "20-04-2016");
//            inputJsonObj.put("accid", "ff80808153ea18f40153ea20a657000e");
            inputJsonObj.put("billdate", "11-1-2016");
            inputJsonObj.put("duedate", "11-12-2016");
//            inputJsonObj.put("costcenter", "");
//            inputJsonObj.put("shipdateStr", "2016-04-13");
            inputJsonObj.put("number", "Test-Inv-rest-18");
            inputJsonObj.put("shipdate", "11-11-2016");
            inputJsonObj.put("dateformat", "dd-MM-yyyy");
//            inputJsonObj.put("duedateStr", "2016-04-22");
//            inputJsonObj.put("includeprotax", false);
            inputJsonObj.put("currencyCode", "SGD");
//            inputJsonObj.put("externalcurrencyrate", "1");
//            inputJsonObj.put("gcurrencyCode", "SGD");
//            inputJsonObj.put("termid", "ff80808153ea18f40153ea20a6380009");
            inputJsonObj.put("acccode", "Test Customer");
//            invDataArr.put(detailObj);
//            inputJsonObj.put("invoicedata", invDataArr);

            String endpoint = "http://localhost:8084/Accounting/rest/transaction/invoice";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSaveInvoice() throws JSONException {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "erptest01");
//            inputJsonObj.put("language", "en-US");
//            inputJsonObj.put("currencyCode", "USD");
//            inputJsonObj.put("userName", "admin");
//            JSONArray invDataArr = new JSONArray();
//            JSONObject detailObj = new JSONObject();
            JSONArray detailArr = new JSONArray();
            JSONObject detail1Obj = new JSONObject();
//            detail1Obj.put("amountwithouttax", 2000);
//            detail1Obj.put("currencyrate", "1.0");
            detail1Obj.put("productid", "ff80808153f089910153f0a1b7cb0004");
//            detail1Obj.put("currencysymbol", "SGD");
//            detail1Obj.put("copybaseuomrate", "0.0");
//            detail1Obj.put("baseuomquantity", "1");
//            detail1Obj.put("prtaxname", "Sale+Tax");
//            detail1Obj.put("taxpercent", "0");
//            detail1Obj.put("amount", 2000);
//            detail1Obj.put("rateinbase",2000);
            detail1Obj.put("rate", 2900);
//            detail1Obj.put("taxamount", 0);
//            detail1Obj.put("amountwithtax", 2000);
//            detail1Obj.put("prtaxid", "42fef3ec-5aba-45c1-bf87-b52d59c7e67f");
//            detail1Obj.put("prdiscount", 0);
//            detail1Obj.put("discountispercent", "0");
//            detail1Obj.put("discamount", "0.0");
//            detail1Obj.put("productname", "Course+registration");
            inputJsonObj.put("incash", true);
            detailArr.put(detail1Obj);
            inputJsonObj.put("detail", detailArr);
//            inputJsonObj.put("memo", "Invoice(test)+for+course");
//            inputJsonObj.put("invoiceid", "402880a0541375740154139090510006");
//            inputJsonObj.put("billdateStr", "2016-04-13");
            inputJsonObj.put("userName", "admin");
//            inputJsonObj.put("totalSUM", 2000);
//            inputJsonObj.put("transactiondateStr", "2016-04-13");
//            inputJsonObj.put("transactiondate", (new Date()).getTime());
//            inputJsonObj.put("accid", "ff80808153ea18f40153ea20a657000e");
            inputJsonObj.put("billdate", (new Date()).getTime());
//            inputJsonObj.put("duedate", 1461321760010L);
//            inputJsonObj.put("costcenter", "");
//            inputJsonObj.put("shipdateStr", "2016-04-13");
            inputJsonObj.put("number", "Test-Inv-rest-15");
            inputJsonObj.put("shipdate", (new Date()).getTime());
//            inputJsonObj.put("duedateStr", "2016-04-22");
//            inputJsonObj.put("includeprotax", false);
            inputJsonObj.put("currencyCode", "SGD");
//            inputJsonObj.put("externalcurrencyrate", "1");
//            inputJsonObj.put("gcurrencyCode", "SGD");
//            inputJsonObj.put("termid", "ff80808153ea18f40153ea20a6380009");
            inputJsonObj.put("acccode", "Test Customer");
//            invDataArr.put(detailObj);
//            inputJsonObj.put("invoicedata", invDataArr);

            String endpoint = "http://localhost:8084/Accounting/rest/transaction/invoice";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testSaveReceipt() throws JSONException {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "erptest01");
            inputJsonObj.put("language", "en-US");
            inputJsonObj.put("currencyCode", "USD");
            inputJsonObj.put("userName", "admin");
            inputJsonObj.put("acccode", "Test Customer");
//            inputJsonObj.put("advanceAmountFlag", true);
            inputJsonObj.put("amount", 100);
            inputJsonObj.put("creationdate", "10-10-2016");
            inputJsonObj.put("dateformat", "dd-MM-yyyy");
//            inputJsonObj.put("creationdateStr","2016-04-1");
            inputJsonObj.put("no", "TestRec-08-test21");
            inputJsonObj.put("pmtmethod", "ff80808153ea18f40153ea20a6760024");
            JSONObject detailObj = new JSONObject();
//            detailObj.put("creationdate", (new Date().getTime()));            
//            detailObj.put("creationdateStr","2016-04-13");
//            detailObj.put("amountdue", 7000);
//            detailObj.put("entryno", "JE000020-test");
            detailObj.put("amount", 100);
            detailObj.put("invoiceid", "402880a054758e2d0154758e59aa000d");
//            detailObj.put("amountDueOriginalSaved", 7000);
            detailObj.put("transactionno", "Test-Inv-rest-21-paid");
//            detailObj.put("transactionno", "145");
            detailObj.put("payment", 100);
//            detailObj.put("journalentryid", "402880a0540ddd5501540e1b6a43004b");
            JSONArray detailarr = new JSONArray();
            detailarr.put(detailObj);
            inputJsonObj.put("detail", detailarr);

            String endpoint = "http://localhost:8084/Accounting/rest/transaction/receipt";
            System.out.println(inputJsonObj);
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testDeleteInvoice() {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "erptest01");
//            inputJsonObj.put("language", "en-US");
            JSONArray invArr = new JSONArray();
            JSONObject invO = new JSONObject();
            invO.put("invoiceno", "Test-Inv-rest-4");
            invArr.put(invO);
            invO = new JSONObject();
            invO.put("invoiceno", "Test-Inv-rest-18");
            invArr.put(invO);

            inputJsonObj.put("data", invArr);
            System.out.println("request: " + inputJsonObj);
            String endpoint = "http://localhost:8084/Accounting/rest/transaction/invoice?request=" + inputJsonObj.toString();
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("DELETE");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);

            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }
            System.out.println(urlConn.getResponseCode());

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /*public void testSaveTaxwithJersey() {
     try {
     ClientConfig config = new DefaultClientConfig();
     Client client = Client.create(config);
     client.addFilter(new LoggingFilter());
     WebResource service = client.resource(getBaseURI() + "Accounting/");
     JSONObject inputJsonObj = new JSONObject();
     inputJsonObj.put("companyid", "04575a0c-b33c-11e3-986d-001e670e14yu");
     JSONObject innerO = new JSONObject();
     innerO.put("id", "0d68ed8f-f108-412a-8415-79a04c317ab0");
     innerO.put("taxname", "aaaa");
     innerO.put("taxcode", "aaaa@22%");
     innerO.put("applydateStr", (new Date()).toString());
     innerO.put("percent", "33");
     JSONArray array = new JSONArray();
     array.put(innerO);
     inputJsonObj.put("taxdetials", array);

     Form f = new Form();
     f.add("data", inputJsonObj);
     //            System.out.println(service.path("rest").path("master").path("saveTax").accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(ClientResponse.class, inputJsonObj.toString()));
     System.out.println(service.path("rest").path("master").path("saveTax").accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).post(String.class, f));
     } catch (JSONException ex) {
     Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
     }
     }
     */
    public void testMasterSaveTax() {
        String result = null;
        try {

            String endpoint = "http://localhost:8080/Accounting/rest/master/tax";
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setRequestProperty("Accept", "*/*");
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "assettest");
            inputJsonObj.put("dateformat", "");
            JSONObject innerO = new JSONObject();
            String taxid = UUID.randomUUID().toString();
            System.out.println("taxid : " + taxid);
            innerO.put("taxid", taxid);
            innerO.put("taxname",  StringUtil.DecodeText("a11"));
            innerO.put("taxdescription",  StringUtil.DecodeText("a11-desc"));
            innerO.put("taxcode", "aaaa12@12%");
            innerO.put("taxCodeWithoutPercentage", StringUtil.DecodeText("a11@11"));//ERP-10979
            innerO.put("taxtypeid", "1");
            innerO.put("applydateStr", (new Date()).toString());
            innerO.put("percent", "441");
            JSONArray array = new JSONArray();
            array.put(innerO);
            inputJsonObj.put("taxdetails", array);
            System.out.println("input ::: " + inputJsonObj.toString());
            wr.write(inputJsonObj.toString());
            wr.flush();
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);
//                result = convertStreamToString(inStream);
                result = resp.toString().replace("\\", "");
                System.out.println("result:" + result);
            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testMasterSaveProjectDetails() {
        String result = null;
        try {
            String endpoint = "http://localhost:8080/Accounting/rest/master/project";
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setRequestProperty("Accept", "*/*");
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            JSONObject inputJsonObj = new JSONObject();
//            inputJsonObj.put("companyid", "04575a03-b33c-11e3-986d-001e670e1452");
            inputJsonObj.put("cdomain", "pmacc011");
            JSONArray array = new JSONArray();
            JSONObject innerO = new JSONObject();
            innerO.put("projectid", "pid001");
            innerO.put("projectname", "pname001");
            innerO.put("isEdit", "true");

            array.put(innerO);
            inputJsonObj.put("projects", array);
            System.out.println("input :: " + inputJsonObj.toString());
            wr.write(inputJsonObj.toString());
            wr.flush();
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);
//                result = convertStreamToString(inStream);
                result = resp.toString().replace("\\", "");
                System.out.println("result:" + result);
            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testMasterDeleteProjectDetails() {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
//            inputJsonObj.put("companyid", "04575a03-b33c-11e3-986d-001e670e1452");
            inputJsonObj.put("cdomain", "pmacc011");
            JSONArray array = new JSONArray();
            JSONObject innerO = new JSONObject();
            innerO.put("projectid", "pid001");
            innerO.put("projectname", "pname001");
            innerO.put("isEdit", "true");

            array.put(innerO);
            inputJsonObj.put("projects", array);

            String endpoint = "http://localhost:8080/Accounting/rest/master/project?request=" + inputJsonObj.toString();
            System.out.println("endpoint.toString() :: " + endpoint.toString());
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("DELETE");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setRequestProperty("Accept", "*/*");
//            urlConn.setRequestProperty("X-HTTP-Method-Override", "DELETE");
            urlConn.setDoInput(true);

            urlConn.setDoOutput(true);
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);

            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }
            System.out.println("urlConn.getResponseCode() :  " + urlConn.getResponseCode());
//            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
//            wr.write(inputJsonObj.toString());
//            wr.flush();
            System.out.println(urlConn.getResponseCode());

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void testMasterSaveTerm() {
        String result = null;
        try {
            String endpoint = "http://localhost:8080/Accounting/rest/master/term";
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setRequestProperty("Accept", "*/*");
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            JSONObject inputJsonObj = new JSONObject();
//            inputJsonObj.put("companyid", "04575a0c-b33c-11e3-986d-001e670e14yu");
            inputJsonObj.put("cdomain", "assettest");
            JSONObject innerO = new JSONObject();
//                innerO.put("id", "0d68ed8f-f108-412a-8415-79a04c317ab0");
            innerO.put("crmtermid", "cccz");
            innerO.put("termname", "cccz");
            innerO.put("termdays", "1");
            JSONArray array = new JSONArray();
            array.put(innerO);
            inputJsonObj.put("termdetails", array);
            System.out.println("inout :: " + inputJsonObj.toString());
            wr.write(inputJsonObj.toString());
            wr.flush();
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);
//                result = convertStreamToString(inStream);
                result = resp.toString().replace("\\", "");
                System.out.println("result:" + result);
            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void createCompany(){
        try {
//            String endpoint = "http://192.168.0.35:8080/KrawlerGenericLMS/rest/v1/company";  // lms
//            String endpoint = "http://192.168.0.35:8080/eleave/rest/v1/company";  // eleave
            String endpoint = "http://192.168.0.35:8080/Accounting/rest/v1/company";
            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "aaz312");
            inputJsonObj.put("subdomain", "aaz312");
            inputJsonObj.put("companyname", "Krawler");
            inputJsonObj.put("address", "Yerwada");
            inputJsonObj.put("city", "Pune");
            inputJsonObj.put("state", "4");
            inputJsonObj.put("phone", "007");
            inputJsonObj.put("fax", "007007");
            inputJsonObj.put("zip", "411006");
            inputJsonObj.put("website", "www.krawler.com");
            inputJsonObj.put("emailid", "abc@krawler.com");
            inputJsonObj.put("currency", "5");
            inputJsonObj.put("country", "105");
            inputJsonObj.put("timezone", "22");
//                inputJsonObj.put("image", "");
            inputJsonObj.put("smtpflow", 1);
            inputJsonObj.put("smtppassword", "password");
            inputJsonObj.put("smtppath", "path");
            inputJsonObj.put("smtppport", "007");
            inputJsonObj.put("createnew", true);
                    inputJsonObj.put("lname","q");
        inputJsonObj.put("userid","33a4c0de-7d4a-43a3-ac2a-822b39b74ba4");
        inputJsonObj.put("password","7110eda4d09e062aa5e4a390b0a572ac0d2c0220");
//        inputJsonObj.put("companyid","2eba0624-f651-4188-a562-3c0441c3c201");
        inputJsonObj.put("username","admin");
//        inputJsonObj.put("creatorid","33a4c0de-7d4a-43a3-ac2a-822b39b74b3a");
        inputJsonObj.put("referralkey",0);
        inputJsonObj.put("remoteapikey","krawler");
        inputJsonObj.put("fname","q");
//        JSONObject inputJsonObj = new JSONObject();
        inputJsonObj.put("lname","q");
        inputJsonObj.put("iscommit",true);inputJsonObj.put("password","7110eda4d09e062aa5e4a390b0a572ac0d2c0220");
//        inputJsonObj.put("companyname","rt3");
        inputJsonObj.put("currency",1);
        inputJsonObj.put("country","244");
        inputJsonObj.put("emailid","abhijit.hibare@deskera.com");
        inputJsonObj.put("email","abhijit.hibare@deskera.com"); // eleave
        inputJsonObj.put("companyid","3eba0624-f651-4188-a562-3c0441c3c212");
        inputJsonObj.put("timezone",23);
        inputJsonObj.put("username","admin");
        inputJsonObj.put("createnew",true);
//        inputJsonObj.put("creatorid","33a4c0de-7d4a-43a3-ac2a-822b39b74b3a");
        
            APICallHanlderServiceImpl obj = new APICallHanlderServiceImpl();
            obj.restPostMethod(endpoint, inputJsonObj.toString());
        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public void createCompanyLeave(){
        try {
//            String endpoint = "http://192.168.0.35:8080/eleave/rest/v1/company";  // eleave
            String endpoint = "http://192.168.0.35:8080/Accounting/rest/v1/company";
            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("lname", "s");
            inputJsonObj.put("userid", "fd4ae6e8-3be9-4a3e-8183-ff27a8badcbm");
            inputJsonObj.put("iscommit", true);
            inputJsonObj.put("password", "7110eda4d09e062aa5e4a390b0a572ac0d2c0220");
            inputJsonObj.put("companyname", "rt44m");
            inputJsonObj.put("currency", 1);
            inputJsonObj.put("country", "244");
            inputJsonObj.put("emailid", "abhijit.hibare@deskera.com");
            inputJsonObj.put("companyid", "58937e02-7c18-45fd-83c4-219b8badcbam");
            inputJsonObj.put("timezone", 23);
            inputJsonObj.put("username", "admin");
            inputJsonObj.put("createnew", true);
            inputJsonObj.put("creatorid", "fd4ae6e8-3be9-4a3e-8183-ff27a8badcbm");
            inputJsonObj.put("email", "abhijit.hibare@deskera.com");
            inputJsonObj.put("subdomain", "rt44m");
            inputJsonObj.put("referralkey", 0);
            inputJsonObj.put("remoteapikey", "krawler");
            inputJsonObj.put("fname", "s");
            APICallHanlderServiceImpl obj = new APICallHanlderServiceImpl();
            obj.restPostMethod(endpoint, inputJsonObj.toString());
        }catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void testCompanyUpdateCompany() {
        String result = null;
        try {
//            String restUrl = URLUtil.buildRestURL("acc")
            String endpoint = "http://192.168.0.35:8080/Accounting/rest/v1/company";
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setRequestProperty("Accept", "*/*");
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            //******************************prepare input start ******************************//
            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "optus");
            inputJsonObj.put("companyname", "Krawler");
            inputJsonObj.put("address", "Yerwada");
            inputJsonObj.put("city", "Pune");
            inputJsonObj.put("state", "4");
            inputJsonObj.put("phone", "007");
            inputJsonObj.put("fax", "007007");
            inputJsonObj.put("zip", "411006");
            inputJsonObj.put("website", "www.krawler.com");
            inputJsonObj.put("emailid", "abc@krawler.com");
            inputJsonObj.put("currency", "5");
            inputJsonObj.put("country", "105");
            inputJsonObj.put("timezone", "22");
//                inputJsonObj.put("image", "");
            inputJsonObj.put("smtpflow", 1);
            inputJsonObj.put("smtppassword", "password");
            inputJsonObj.put("smtppath", "path");
            inputJsonObj.put("smtppport", "007");
            inputJsonObj.put("createnew", true);
            System.out.println("payload " + inputJsonObj.toString());
            //******************************prepare input end ******************************//
            wr.write(inputJsonObj.toString());
            wr.flush();
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);
//                result = convertStreamToString(inStream);
                result = resp.toString().replace("\\", "");
                System.out.println("result:" + result);
            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testCompanyEditUser() {
        String result = null;
        try {
            String endpoint = "http://localhost:8080/Accounting/rest/company/user";
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setRequestProperty("Accept", "*/*");
            urlConn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
            //******************************prepare input start ******************************//
            JSONObject inputJsonObj = new JSONObject();
//            inputJsonObj.put("userid", "0796ad1c-b33c-11e3-986d-001e670e14yu");
            inputJsonObj.put("cdomain", "fastenhardware");
            inputJsonObj.put("userName", "Sales05");
            inputJsonObj.put("fname", "Kim1");
            inputJsonObj.put("lname", "JS1");
            inputJsonObj.put("address", "White House");
            inputJsonObj.put("emailid", "abc@krawler.com");
            inputJsonObj.put("contactno", "007");
            inputJsonObj.put("timeZone", 236);
            System.out.println("input :: " + inputJsonObj.toString());
            //******************************prepare input end ******************************//
            wr.write(inputJsonObj.toString());
            wr.flush();
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);
//                result = convertStreamToString(inStream);
                result = resp.toString().replace("\\", "");
                System.out.println("result:" + result);
            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testCompanyDeleteCompany() {
        String result = null;
        try {

            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "dmp");

            String endpoint = "http://localhost:8080/Accounting/rest/company?request=" + inputJsonObj.toString();
            System.out.println("endpoint :: " + endpoint);
            URL url = new URL(endpoint.toString());
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("DELETE");
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setRequestProperty("Accept", "*/*");
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            System.out.println(urlConn.getResponseCode());

        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();

        } catch (JSONException ex) {
            Logger.getLogger(TestTBD.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void saveSalesOrder() {
        String result = null;
        try {
            JSONObject inputJsonObj = new JSONObject();
            inputJsonObj.put("cdomain", "sonamintegration");
            inputJsonObj = JsonCreate(inputJsonObj);
            String inputData = java.net.URLEncoder.encode(inputJsonObj.toString(), "UTF-8");
//            String endpoint = "http://localhost:8084/Accounting/rest/transaction/salesorder?request=" + inputData;
            String endpoint = "http://localhost:8084/Accounting/rest/transaction/salesorder";
            postData(endpoint, inputJsonObj.toString(), "POST");

        } catch (Exception e) {
            e.printStackTrace();

        }

    }

    public JSONObject JsonCreate(JSONObject obj) throws JSONException {

        obj.put("companyid", "030c3196-2310-4b98-9f30-8b7089f7c5bb");
        obj.put("userid", "9a7deb4e-a273-4a5b-b579-7b3691007552");
        obj.put("OrderDate", "Feb 25, 2016 12:00:00 AM");
        obj.put("billingAddress", "A");
        obj.put("billingAddressType", "Billing Address1");
        obj.put("billingCity", "B");
        obj.put("billingContactPerson", "C");
        obj.put("billingContactPersonDesignation", "D");
        obj.put("billingContactPersonNumber", "5676767");
        obj.put("billingCountry", "E");
        obj.put("billingEmail", "c@gmail.com");
        obj.put("billingFax", "030c31");
        obj.put("billingMobile", "768768678643454353");
        obj.put("discount", "0");
        obj.put("duedate", "Mar 08, 2016 12:00:00 AM");
        obj.put("fob", "fobvalue new testcxzcxzczxa");
        obj.put("billid", "402880ce5534e9590155351630960032");
        obj.put("isEdit", "true");
        obj.put("isDraft", "false");
        obj.put("memo", "Memo value for edit case");
        obj.put("salesOrderNumber", "SalesOrder1");
        obj.put("sequenceformat", "NA");
//        obj.put("sequenceformat", "402880ce552a14ec01552a368718000d");
        obj.put("isOpeningBalanceOrder", "false");
        obj.put("salesperson", "ff80808150409290015041f7665405a9");
        obj.put("prdiscount", "false");
        obj.put("shippingAddress", "AASSasasa");
        obj.put("shippingAddressType", "Shipping Address1");
        obj.put("shippingCity", "DDSDS");
        obj.put("shippingContactPerson", "G");
        obj.put("shippingContactPersonDesignation", "DSDSS");
        obj.put("shippingContactPersonNumber", "78978978");
        obj.put("shippingCountry", "DDSDS");
        obj.put("shippingEmail", "b@gmail.com");
        obj.put("shippingFax", "DDSDS");
        obj.put("shippingMobile", "4353453");
        obj.put("shippingPhone", "3343434343");
        obj.put("shippingPostal", "4333");
        obj.put("shippingRecipientName", "sss");
        obj.put("shippingRoute", "");
        obj.put("shippingState", "saddsada");
        obj.put("shippingWebsite", "www.googlaaaae.com");
        obj.put("shipvia", "sadadsadasdsada");
        obj.put("subTotal", "610");
        obj.put("taxamount", "0");
        obj.put("term", "ff8080814d4cdde1014d50ea331d01450");
        obj.put("termid", "ff8080814d4cdde1014d50ea331d01450");
        obj.put("isdefaultHeaderMap", true);
        obj.put("costcenter", "ff808081516110cd015161df4bf917e1");
        obj.put("copyInv", "false");
        obj.put("currencyName", "6");
        obj.put("customerName", "ff8080814d574828014d57999b750066");
        obj.put("externalcurrencyrate", "1");
        obj.put("customerPORefNo", "Customer ref value");
        obj.put("duedate", "Mar 08, 2016 12:00:00 AM");

        JSONArray jsoncus = new JSONArray();
        JSONObject j1 = new JSONObject();
        j1.put("fieldid", "1c514642-c3b9-4e81-b845-3e40c32d777f");
        j1.put("refcolumn_name", "Col0");
        j1.put("fieldname", "Custom_Global Dropdown");
        j1.put("xtype", 4);
        j1.put("fieldDataVal", "7bb9647c-b216-496a-a076-3c8d2d6262f4");
        j1.put("Col1", "7bb9647c-b216-496a-a076-3c8d2d6262f4");
        j1.put("Custom_Global Dropdown", "Col1");
        jsoncus.put(j1);

        j1 = new JSONObject();
        j1.put("fieldid", "4923aa4f-b46d-4f45-aeda-3b44ab7ce8e8");
        j1.put("refcolumn_name", "Col0");
        j1.put("fieldname", "Custom_Global Date");
        j1.put("xtype", 3);
        j1.put("fieldDataVal", "1465497000000");
        j1.put("Col1004", "1465497000000");
        j1.put("Custom_Global Date", "Col1004");
        jsoncus.put(j1);
//
        j1 = new JSONObject();
        j1.put("fieldid", "16b7a7b4e-6488-4832-9b73-7af450ed85ae");
        j1.put("refcolumn_name", "Col0");
        j1.put("fieldname", "Custom_Global Numeric");
        j1.put("xtype", 2);
        j1.put("fieldDataVal", "5444444");
        j1.put("Col1002", "5444444");
        j1.put("Custom_Global Numeric", "Col1002");
        jsoncus.put(j1);

        j1 = new JSONObject();
        j1.put("fieldid", "8cbd9fbe-8097-40ea-b820-5e1958bb8c6c");
        j1.put("refcolumn_name", "Col2");
        j1.put("fieldname", "Custom_Global Multiselect Dropdown");
        j1.put("xtype", 7);
        j1.put("fieldDataVal", "cf5af189-dbda-40b4-a202-cfe5e1279bc5,5b719426-f086-460c-b8cc-cba535246239");
        j1.put("Col1003", "cf5af189-dbda-40b4-a202-cfe5e1279bc5,5b719426-f086-460c-b8cc-cba535246239");
        j1.put("Custom_Global Multiselect Dropdown", "Col1003");
        jsoncus.put(j1);

        j1 = new JSONObject();
        j1.put("fieldid", "b2d87b65-a648-42f9-8397-80f2f4677be4");
        j1.put("refcolumn_name", "Col0");
        j1.put("fieldname", "Custom_Global Text Area");
        j1.put("xtype", 13);
        j1.put("fieldDataVal", "Global Text Area Value");
        j1.put("Col1005", "Global Text Area Value");
        j1.put("Custom_Global Text Area", "Col1005");
        jsoncus.put(j1);
//
        j1 = new JSONObject();
        j1.put("fieldid", "f9d5dbbe-9e6e-4c41-8c6e-85956626c294");
        j1.put("refcolumn_name", "Col0");
        j1.put("fieldname", "Custom_Test");
        j1.put("xtype", 1);
        j1.put("fieldDataVal", "frgdfgdf");
        j1.put("Col1001", "frgdfgdf");
        j1.put("Custom_Test", "Col1001");
        jsoncus.put(j1);

        obj.put("customfield", jsoncus);

        /*
         * Line Item Details
         */
        JSONArray profield = new JSONArray();
        JSONObject product = new JSONObject();
        product.put("rowid", "402880ce552575c30155259d5d090008");
        product.put("productname", "TestProduct1");
        product.put("billid", "402880ce5524874b0155248b7cd80002");
        product.put("savedrowid", "");

        product.put("salesOrderNumber", "SalesOrder1");
        product.put("productID", "ff808081530e4b8001530ea06fb80003");
        product.put("desc", "Descrition1ewwewe");
        product.put("quantity", "2");
        product.put("showquantity", "");
        product.put("baseuomquantity", "2");
        product.put("baseuomname", "Unit");
        product.put("baseuomid", "ff8080814d4cdde1014d50ea331e0148");
        product.put("uomid", "ff8080814d4cdde1014d50ea331e0148");
        product.put("baseuomrate", "1");
        product.put("copyquantity", "2");
        product.put("rate", "100");
        product.put("rateIncludingGst", "0");
        product.put("isRateIncludingGstEnabled", "");
        product.put("rateinbase", "");
        product.put("discountType", "1");
        product.put("discount", "0");
        product.put("prtaxid", "a387bd36-572a-48f1-abe3-19ca588e9718");
        product.put("prtaxpercent", "7");
        product.put("taxamount", "14");
        product.put("amount", "214");
        product.put("amountwithtax", "");
        product.put("amountwithouttax", "200");
        product.put("taxpercent", "7");
        product.put("remark", "");
        product.put("currencysymbol", "SGD");
        product.put("currencyrate", "1");
        product.put("externalcurrencyrate", "");
        product.put("linkto", "");
        product.put("linkid", "");
        product.put("linktype", "-1");
        product.put("batchdetails", "");
        product.put("recTermAmount", "0");
        product.put("OtherTermNonTaxableAmount", "0");
        product.put("LineTermdetails", "");
        product.put("ProductTermdetails", "");
        product.put("islockQuantityflag", "");
        product.put("gridRemark", "");
        product.put("productcustomfield", "[{}]");
        product.put("rowTaxAmount", "14");

        product.put("type", "InventoryPart");
        product.put("priceSource", "");
        product.put("warehouse", "");
        product.put("location", "");
        product.put("vendorcurrexchangerate", "false");
        product.put("totalcost", "0");
        product.put("srno", "1");

        JSONArray jsonlincus = new JSONArray();
        JSONObject j2 = new JSONObject();
        j2.put("refcolumn_name", "Col4");
        j2.put("fieldname", "Custom_LIneMultiselectDropdown");
        j2.put("Col1009", "0699cfb5-ee3b-4edb-92ff-3d92d9ae4988,636d71bf-9ec9-41f9-a884-ad3cf604c3dd");
        j2.put("Custom_LIneMultiselectDropdown", "Col1009");
        j2.put("filedid", "51c2cd7a-8ae6-4fb5-a274-ce0051c87ee3");
        j2.put("xtype", "7");
        jsonlincus.put(j2);

        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col0");
        j2.put("fieldname", "Custom_LineTextAREA");
        j2.put("Col1010", "");
        j2.put("Custom_LineTextAREA", "Col1010");
        j2.put("filedid", "47ae0735-15d4-49a5-a8b1-60d9f8fda027");
        j2.put("xtype", "13");
        jsonlincus.put(j2);
//
        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col0");
        j2.put("fieldname", "Custom_Line Dropdown");
        j2.put("Col3", "ccf0dfd5-74f7-4c84-b682-2c4efc572ab5");
        j2.put("Custom_Line Dropdown", "Col3");
        j2.put("filedid", "83b97603-4dc7-4a99-88c5-9141622ec84c");
        j2.put("xtype", "4");
        jsonlincus.put(j2);
//
        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col4");
        j2.put("fieldname", "Custom_LIne TextField");
        j2.put("Col1006", "rewrwe");
        j2.put("Custom_LIne TextField", "Col1006");
        j2.put("filedid", "8e4948d9-7b0f-4f94-a2b1-5a154be9b216");
        j2.put("xtype", "1");
        jsonlincus.put(j2);
//
        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col4");
        j2.put("fieldname", "Custom_LIne NumericField");
        j2.put("Col1007", "45465");
        j2.put("Custom_LIne NumericField", "Col1007");
        j2.put("filedid", "dd1c3c5a-4e63-46d3-9995-9237381cbd32");
        j2.put("xtype", "2");
        jsonlincus.put(j2);
//
        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col4");
        j2.put("fieldname", "Custom_Line Custom Date");
        j2.put("Col1008", "1465669800000");
        j2.put("Custom_Line Custom Date", "Col1008");
        j2.put("filedid", "e5d15d7e-3416-4b1e-aaad-79cc8a367fa4");
        j2.put("xtype", "3");
        jsonlincus.put(j2);
        product.put("customfield", jsonlincus);
        profield.put(product);

        //2nd product
        product = new JSONObject();
        product.put("rowid", "402880ce552575c30155259d5d090008");
        product.put("productname", "TestProduct1");
        product.put("billid", "402880ce5524874b0155248b7cd80002");
        product.put("savedrowid", "");
//        product.put("billno", "S111112222");
        product.put("salesOrderNumber", "SA00027SZ");
        product.put("productID", "ff808081530e4b8001530ea06fb80003");
        product.put("desc", "Descrition1");
        product.put("quantity", "2");
        product.put("showquantity", "");
        product.put("baseuomquantity", "2");
        product.put("baseuomname", "Unit");
        product.put("baseuomid", "ff8080814d4cdde1014d50ea331e0148");
        product.put("uomid", "ff8080814d4cdde1014d50ea331e0148");
        product.put("baseuomrate", "1");
        product.put("copyquantity", "2");
        product.put("rate", "100");
        product.put("rateIncludingGst", "0");
        product.put("isRateIncludingGstEnabled", "");
        product.put("rateinbase", "");
        product.put("discountType", "1");
        product.put("discount", "0");
        product.put("prtaxid", "a387bd36-572a-48f1-abe3-19ca588e9718");
        product.put("prtaxpercent", "7");
        product.put("taxamount", "14");
        product.put("amount", "214");
        product.put("amountwithtax", "");
        product.put("amountwithouttax", "200");
        product.put("taxpercent", "7");
        product.put("remark", "");
        product.put("currencysymbol", "SGD");
        product.put("currencyrate", "1");
        product.put("externalcurrencyrate", "");
        product.put("linkto", "");
        product.put("linkid", "");
        product.put("linktype", "-1");
        product.put("batchdetails", "");
        product.put("recTermAmount", "0");
        product.put("OtherTermNonTaxableAmount", "0");
        product.put("LineTermdetails", "");
        product.put("ProductTermdetails", "");
        product.put("islockQuantityflag", "");
        product.put("gridRemark", "");
        product.put("productcustomfield", "[{}]");
        product.put("rowTaxAmount", "14");

        product.put("type", "InventoryPart");
        product.put("priceSource", "");
        product.put("warehouse", "");
        product.put("location", "");
        product.put("vendorcurrexchangerate", "false");
        product.put("totalcost", "0");
        product.put("srno", "1");

        jsonlincus = new JSONArray();
        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col4");
        j2.put("fieldname", "Custom_LIneMultiselectDropdown");
        j2.put("Col1009", "0699cfb5-ee3b-4edb-92ff-3d92d9ae4988,636d71bf-9ec9-41f9-a884-ad3cf604c3dd");
        j2.put("Custom_LIneMultiselectDropdown", "Col1009");
        j2.put("filedid", "51c2cd7a-8ae6-4fb5-a274-ce0051c87ee3");
        j2.put("xtype", "7");
        jsonlincus.put(j2);

        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col0");
        j2.put("fieldname", "Custom_LineTextAREA");
        j2.put("Col1010", "");
        j2.put("Custom_LineTextAREA", "Col1010");
        j2.put("filedid", "47ae0735-15d4-49a5-a8b1-60d9f8fda027");
        j2.put("xtype", "13");
        jsonlincus.put(j2);

        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col0");
        j2.put("fieldname", "Custom_Line Dropdown");
        j2.put("Col3", "ccf0dfd5-74f7-4c84-b682-2c4efc572ab5");
        j2.put("Custom_Line Dropdown", "Col3");
        j2.put("filedid", "83b97603-4dc7-4a99-88c5-9141622ec84c");
        j2.put("xtype", "4");
        jsonlincus.put(j2);
//
        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col4");
        j2.put("fieldname", "Custom_LIne TextField");
        j2.put("Col1006", "rewrwe");
        j2.put("Custom_LIne TextField", "Col1006");
        j2.put("filedid", "8e4948d9-7b0f-4f94-a2b1-5a154be9b216");
        j2.put("xtype", "1");
        jsonlincus.put(j2);

        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col4");
        j2.put("fieldname", "Custom_LIne NumericField");
        j2.put("Col1007", "45465");
        j2.put("Custom_LIne NumericField", "Col1007");
        j2.put("filedid", "dd1c3c5a-4e63-46d3-9995-9237381cbd32");
        j2.put("xtype", "2");
        jsonlincus.put(j2);

        j2 = new JSONObject();
        j2.put("refcolumn_name", "Col4");
        j2.put("fieldname", "Custom_Line Custom Date");
        j2.put("Col1008", "1465669800000");
        j2.put("Custom_Line Custom Date", "Col1008");
        j2.put("filedid", "e5d15d7e-3416-4b1e-aaad-79cc8a367fa4");
        j2.put("xtype", "3");
        jsonlincus.put(j2);
        product.put("customfield", jsonlincus);
        profield.put(product);
        obj.put("detail", profield);
        StringWriter out = new StringWriter();
        obj.write(out);

        String jsonText = out.toString();
        System.out.print(jsonText);
        return obj;
    }
    
   
 public String getAuthToken(String clientId, String clientSecret, String platformurl) throws MalformedURLException, ProtocolException, IOException, JSONException{
                String token = null;
                StringBuilder resp = new StringBuilder();
                StringBuilder buildUrl = new StringBuilder();
                String endpoint = URLUtil.buildRestURL("platformURL");
                buildUrl.append(endpoint);
                buildUrl.append("company/token");
                buildUrl.append("?" + Constants.CLIENT_ID + "=" + clientId + "&" + Constants.CLIENT_SECRET + "=" + clientSecret);
                endpoint = buildUrl.toString();
            //                endpoint = endpoint + "?" + Constants.CLIENT_ID + "=" + clientId + "&" + Constants.CLIENT_SECRET + "=" + clientSecret;
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
        return token;
    } 
 
     public String saveTransactions(String data,String accUrl, String token) throws MalformedURLException, ProtocolException, IOException, JSONException{
        String type = "POST";
           // String data = inputJsonObj.toString();
//            String endpoint = "http://192.168.0.78:8084/Accounting/rest/v1/transaction/invoice";
            StringBuilder buildUrl = new StringBuilder();
//            String endpoint = URLUtil.buildRestURL("accUrl");
            buildUrl.append(accUrl+"rest/v1/");
            buildUrl.append("transaction/lead-invoice-receipt");
            buildUrl.append("?token=" + token);
            String endpoint = buildUrl.toString();
//            String endpoint = accUrl+"rest/v1/transaction/lead-invoice-receipt?token="+token;
            URL url = new URL(endpoint);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod(type);
            urlConn.setRequestProperty("Content-type", "application/json");//"application/json" content-type is required.
            urlConn.setDoOutput(true);
            urlConn.setDoInput(true);
                OutputStreamWriter wr = new OutputStreamWriter(urlConn.getOutputStream());
                wr.write(data);
                wr.flush();
            StringBuilder resp = new StringBuilder();
            InputStream inStream = urlConn.getInputStream();
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
                String line = null;
                while ((line = in.readLine()) != null) {
                    resp.append(line);
                }
                System.out.println("response: " + resp);

            } catch (Exception e) {
                System.out.println("Error Parsing: - ");
            }
            return type;
    } 
     
    public void saveje(){
            
//                JSONObject userData2 = new JSONObject();
//            userData2.put("jedata", jsonArray);
//            userData2.put("currencyid", cp.getCompany().getCurrency().getCurrencyID());
//            userData2.put("gcurrencyid", cp.getCompany().getCurrency().getCurrencyID());
//            userData2.put("iscommit", true);
//            userData2.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
//            userData2.put("companyid", companyid);
//            
//            
//            appdata2 = APICallHandler.restPostMethod(endpoint, userData2.toString());
    }
}
