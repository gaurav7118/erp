/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/applicationContextList.xml")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransactionServiceImplTest {
   
    public TransactionServiceImplTest() {
    }

    @Autowired
    private TransactionService instance;
    
    @Autowired
    private HibernateTransactionManager txnManager;
    TransactionStatus status = null;

    @Autowired
    private MasterService masterIstance;
    
    private static String cdomain = JUnitConstants.SUBDOMAIN;
    private static String cdomain1 = "sonamintegration";
    private static String userName = "admin";
    private static String jeCreated;
    private static String invCreated;
    private static String accountid1;
    private static String accountid2;
    private static String acccode;
    private static String paymentMethod;
    private static String customerId;
    private static String productId;
    private static String invoiceNoForDeletion;
    private static String crmquoteid;
    private static String quotationId;

    @Before
    public void setUp() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AccScriptServicePropogation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }
    
    private void populateAdditionalAttributes() throws Exception {
        MasterServiceImplTest masterTest = new MasterServiceImplTest();
        Map<String, Object> transactionAttributes = masterTest.getTransactionAttributes(masterIstance);
        productId = (String) transactionAttributes.get("productid");
        customerId = (String) transactionAttributes.get("customerid");
        productId = "ff80808153f089910153f0a1b7cb0004";
//        customerId = "ff80808153f089910153f0a07c270002";
    }


    /**
     * Test of getAccountList method, of class TransactionServiceImpl.
     */
    @Test
    public void test1GetAccountList() throws Exception {
        System.out.println("getAccountList");
        boolean isSuccess = false;
        populateAdditionalAttributes();
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put("currencycode", "USD");
        JSONObject result = instance.getAccountList(requestJobj);
        System.out.println("getAccountList -> "+result);
        if (result != null) {
            if (result.has("success") && result.getBoolean("success") && result.has("totalCount")) {
                assertTrue("Data is returned properly", result.getInt("totalCount") > 0);
                isSuccess = true;
                if (result.has("data")) {
                    JSONArray dataArray = result.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObj = dataArray.getJSONObject(i);
                        if (acccode ==null && dataObj.has("acccode") && !StringUtil.isNullOrEmpty(dataObj.getString("acccode"))) {
                            acccode = dataObj.getString("acccode");
                        }
                        if (accountid1 == null) {
                            accountid1 = dataObj.getString("accid");

                        } else if (accountid2 == null) {
                            accountid2 = dataObj.getString("accid");;
                        }
                         else if(accountid1 != null && accountid2 != null && acccode!=null){
                            break;
                        }

                    }
                }
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test1GetAccountList");
        }
    }

    /**
     * Test of getPaymentMethod method, of class TransactionServiceImpl.
     */
    @Test
    public void test2GetPaymentMethod() throws Exception {
        System.out.println("getPaymentMethod");
        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        JSONObject result = instance.getPaymentMethod(requestJobj);
        System.out.println("getPaymentMethod -> "+result);
        if (result != null) {
            if (result.has("success") && result.getBoolean("success") && result.has("totalCount")) {
                assertTrue("Data is returned properly", result.getInt("totalCount") > 0);
                isSuccess = true;
                if (result.has("data")) {
                    JSONArray dataArray = result.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObj = dataArray.getJSONObject(i);
                        if (dataObj.has("methodid") && !StringUtil.isNullOrEmpty(dataObj.getString("methodid"))) {
                            paymentMethod = dataObj.getString("methodid");
                        }
                    }
                }
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test2GetPaymentMethod");
        }
    }

    /**
     * Test of getSalesOrder method, of class TransactionServiceImpl.
     */
    @Test
    public void test3GetSalesOrder() throws Exception {
        System.out.println("getSalesOrder");

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put("currencycode", "SGD");
        requestJobj.put("customerid", customerId);
        JSONObject result = instance.getSalesOrder(requestJobj);
        System.out.println("getSalesOrder -> "+result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test3GetSalesOrder");
        }
    }

    /**
     * Test of saveJournalEntry method, of class TransactionServiceImpl.
     */
    @Test
    public void test4SaveJournalEntryByCompanyPreference() throws Exception {
        System.out.println("saveJournalEntry");
        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put("currencycode", "USD");
        requestJobj.put("username", userName);
        requestJobj.put("byCompanyPreference", true);
        JSONArray jedataArray = new JSONArray();
        JSONObject jedata = new JSONObject();
        jedata.put("amount", "1013");
        jedata.put("description", "TEST  rest company");
        jedataArray.put(jedata);
        requestJobj.put("jedata", jedataArray);
        JSONObject result = instance.saveJournalEntry(requestJobj);
        System.out.println("saveJECompanyPref -> "+result);
        if (result != null) {
            if (result.has("success") && result.has("jedetails")) {
                JSONArray jedetailArray = result.getJSONArray("jedetails");
                assertTrue("Data is returned properly", jedetailArray.length() > 0);
                isSuccess = true;
                JSONObject resultJedetail = jedetailArray.getJSONObject(0);
                jeCreated = resultJedetail.getString("jeid");
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test4SaveJournalEntryByCompanyPreference");
        }
    }

    /**
     * Test of saveJournalEntry method, of class TransactionServiceImpl.
     */
    @Test
    public void test5SaveJournalEntry() throws Exception {
        System.out.println("saveJournalEntry");
        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put("language", "en-US");
        requestJobj.put("username", userName);
        requestJobj.put("currencycode", "USD");
        JSONArray jedata = new JSONArray();
        JSONObject datamap = new JSONObject();
        datamap = new JSONObject();
        datamap.put("amount", "2013");
        datamap.put("description", "TEST rest company same structure");
        datamap.put("accountid", accountid1);
        datamap.put("debit", true);
        jedata.put(datamap);
        datamap = new JSONObject();
        datamap = new JSONObject();
        datamap.put("amount", "2013");
        datamap.put("description", "TEST rest company same structure");
        datamap.put("accountid", accountid2);
        datamap.put("debit", false);
        jedata.put(datamap);
        requestJobj.put("jedata", jedata);
        JSONObject result = instance.saveJournalEntry(requestJobj);
        System.out.println("saveJE -> "+result);
        if (result != null) {
            if (result.has("success") && result.has("jedetails")) {
                JSONArray jedetailArray = result.getJSONArray("jedetails");
                assertTrue("Data is returned properly", jedetailArray.length() > 0);
                isSuccess = true;
                JSONObject resultJedetail = jedetailArray.getJSONObject(0);
                jeCreated = resultJedetail.getString("jeid");
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test5SaveJournalEntry");
        }
    }

    /**
     * Test of getJournalEntry method, of class TransactionServiceImpl.
     */
    @Test
    public void test6GetJournalEntry() throws Exception {
        System.out.println("getJournalEntry");
        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put("currencycode", "USD");
        JSONObject result = instance.getJournalEntry(requestJobj);
        System.out.println("getJE -> "+result);
        if (result != null) {
            if (result.has("success") && result.has("data")) {
                JSONArray resultDataArray = result.getJSONArray("data");
                for (int i = 0; i < resultDataArray.length(); i++) {
                    JSONObject resultDataObj = resultDataArray.getJSONObject(i);
                    if (resultDataObj.has("jeDetails")) {
                        JSONArray resultJedetailArray = resultDataObj.getJSONArray("jeDetails");
                        for (int j = 0; j < resultJedetailArray.length(); j++) {
                            JSONObject resultJedetailObj = resultJedetailArray.getJSONObject(j);
                            if (resultJedetailObj.has("jeId") && jeCreated.equals(resultJedetailObj.getString("jeId"))) {
                                assertTrue("Data is returned properly", jeCreated.equals(resultJedetailObj.getString("jeId")));
                                isSuccess = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test6GetJournalEntry");
        }
    }

    /**
     * Test of saveInvoice method, of class TransactionServiceImpl.
     */
    @Test
    public void test7SaveInvoice() throws Exception {
        System.out.println("saveInvoice");

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        JSONArray detailArr = new JSONArray();
        JSONObject detail1Obj = new JSONObject();
        detail1Obj.put("productid", productId);
        detail1Obj.put("rate", 2900);
        requestJobj.put("incash", true);
        detailArr.put(detail1Obj);
        requestJobj.put("detail", detailArr);
        requestJobj.put("username", userName);
        requestJobj.put("transactiondate", "20-04-2016");
        requestJobj.put("billdate", "11-1-2016");
        requestJobj.put("duedate", "11-12-2016");
        if (invoiceNoForDeletion == null) {
            invoiceNoForDeletion = "Test-Inv-" + (new Date()).getTime();
        }
        requestJobj.put("number", invoiceNoForDeletion);
        requestJobj.put("shipdate", "11-11-2016");
        requestJobj.put("dateformat", "dd-MM-yyyy");
        requestJobj.put("currencycode", "SGD");
        requestJobj.put("acccode", acccode);
        JSONObject result = instance.saveInvoice(requestJobj);
        System.out.println("saveInvoice -> "+result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
                if (result.has("invoiceid")) {
                    invCreated = result.getString("invoiceid");
                }
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test7SaveInvoice");
        }
    }

    /**
     * Test of getInvoice method, of class TransactionServiceImpl.
     */
    @Test
    public void test8GetInvoice() throws Exception {
        System.out.println("getInvoice");
        boolean isSuccess = false;

        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put("currencycode", "USD");
        requestJobj.put("customerid", customerId);
        JSONObject result = instance.getInvoice(requestJobj);
        System.out.println("getInvoice -> "+result);
        if (result != null) {
            if (result.has("success") && result.has("data")) {
                JSONArray dataArray = result.getJSONArray("data");
                for (int i = 0; i < dataArray.length(); i++) {
                    JSONObject dataObj = dataArray.getJSONObject(i);
                    if (dataObj.has("billid") && invCreated.equals(dataObj.getString("billid"))) {
                        assertTrue("Data is returned properly", invCreated.equals(dataObj.getString("billid")));
                        isSuccess = true;
                        break;
                    }
                }
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test8GetInvoice");
        }
    }

    /**
     * Test of saveReceipt method, of class TransactionServiceImpl.
     */
    @Test
    public void test91SaveReceipt() throws Exception {
        System.out.println("saveReceipt");
        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put("language", "en-US");
        requestJobj.put("currencyCode", "USD");
        requestJobj.put("userName", userName);
        requestJobj.put("acccode", acccode);
        requestJobj.put("amount", 100);
        requestJobj.put("creationdate", "10-10-2016");
        requestJobj.put("dateformat", "dd-MM-yyyy");
        requestJobj.put("no", "TestRec-"+invCreated);
        requestJobj.put("pmtmethod", paymentMethod);
        JSONObject detailObj = new JSONObject();
        detailObj.put("amount", 100);
        detailObj.put("invoiceid", invCreated);
        detailObj.put("transactionno", "TestRec"+invCreated+"-paid");
        detailObj.put("payment", 100);
        JSONArray detailarr = new JSONArray();
        detailarr.put(detailObj);
        requestJobj.put("detail", detailarr);
        JSONObject result = instance.saveReceiptPayment(requestJobj);
        System.out.println("saveReceipt -> "+result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test91SaveReceipt");
        }
    }

    /**
     * Test of deleteInvoice method, of class TransactionServiceImpl.
     */
    @Test
    public void test92DeleteInvoice() throws Exception {
        System.out.println("deleteInvoice");
        boolean isSuccess = false;
        invoiceNoForDeletion = "Test-Inv-" + (new Date()).getTime();
        test7SaveInvoice();
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put("language", "en-US");
        JSONArray invArr = new JSONArray();
        JSONObject invO = new JSONObject();
        invO.put("invoiceno", invoiceNoForDeletion);
        invArr.put(invO);
        requestJobj.put("data", invArr);
        JSONObject result = instance.deleteInvoice(requestJobj);
        System.out.println("deleteInvoice -> "+result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test92DeleteInvoice");
        }
    }

    /**
     * Test of getQuotations method, of class TransactionServiceImpl.
     */
    @Test
    public void test11getQuotations() throws Exception {
        System.out.println("getQuotations");
        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain1);
        JSONObject result = instance.getQuotations(requestJobj);
        System.out.println("getQuotations -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
                if (result.has("data") && result.has("totalCount") && result.getInt("totalCount") > 0) {
                    JSONArray dataArray = result.getJSONArray("data");
                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObj = dataArray.getJSONObject(i);
                        if (dataObj.has("crmquoteid")) {
                            crmquoteid = dataObj.getString("crmquoteid");
                            break;
                        }
                        quotationId= dataObj.getString("billid");
                    }
                }
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test11getQuotations");
        }
    }

    /**
     * Test of getInvoiceDetailfromCRMQuotation method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void test12GetInvoiceDetailfromCRMQuotation() throws Exception {
        System.out.println("getInvoiceDetailfromCRMQuotation");
        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain1);
        requestJobj.put("quotationid", crmquoteid);
        JSONObject result = instance.getInvoiceDetailfromCRMQuotation(requestJobj);
        System.out.println("getInvoiceDetailfromCRMQuotation -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test12GetInvoiceDetailfromCRMQuotation");
        }
    }

    /**
     * Test of deleteQuotation method, of class TransactionServiceImpl.
     */
    @Test
    public void test13DeleteQuotation() throws Exception {
        System.out.println("deleteQuotation");
        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain1);
        JSONArray quotationIdArr = new JSONArray();
        quotationIdArr.put(crmquoteid);
        requestJobj.put("quotationids", quotationIdArr);
        JSONObject result = instance.deleteQuotation(requestJobj);
        System.out.println("deleteQuotation -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed : test13DeleteQuotation");
        }
    }
    
    /**
     * Test of getIndividualProductPrice method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testGetIndividualProductPrice() throws Exception {
        System.out.println("getIndividualProductPrice");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put(Constants.globalCurrencyKey, JUnitConstants.GLOBAL_CURRENCY_ID);
        requestJobj.put(Constants.companyKey, JUnitConstants.COMPANY_ID);
        requestJobj.put(Constants.timezonedifference, "");
        requestJobj.put(Constants.productid, JUnitConstants.PRODUCT_ID);
        requestJobj.put("currency", JUnitConstants.CURRENCY_ID);
        requestJobj.put(Constants.isdefaultHeaderMap, "");
        requestJobj.put("transactiondate", authHandler.getDateOnlyFormat().format(new Date()));

        JSONObject result = instance.getIndividualProductPrice(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetIndividualProductPrice");
        }
    }
    
    /**
     * Test of saveSalesOrder method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testSaveSalesOrder() throws Exception {
        System.out.println("saveSalesOrder");
        JSONObject requestJobj = new JSONObject();
        JSONObject jobj = new JSONObject();
        SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
        requestJobj.put(Constants.remoteIPAddress, JUnitConstants.REMOTE_IP_ADDRESS);
        requestJobj.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(jobj));
        requestJobj.put(Constants.df, formatter);
        requestJobj.put(Constants.companyKey, JUnitConstants.COMPANY_ID);
        requestJobj.put(Constants.useridKey, JUnitConstants.USER_ID);
        requestJobj.put(Constants.currencyKey, JUnitConstants.CURRENCY_ID);
        requestJobj.put(Constants.globalCurrencyKey, JUnitConstants.GLOBAL_CURRENCY_ID);
        requestJobj.put("customer", JUnitConstants.CUSTOMER_ID);
        requestJobj.put(Constants.sequenceformat, "NA");
        requestJobj.put("number", "Test_SO0000001");
        requestJobj.put(Constants.BillDate, formatter.format(new Date()));
        requestJobj.put(Constants.duedate, formatter.format(new Date()));
        requestJobj.put("termid", JUnitConstants.TERM_ID);
        requestJobj.put("defaultAdress", "true");
        requestJobj.put("linkNumber", "");
        requestJobj.put(Constants.deleted, false);
       
        JSONArray detailArr = new JSONArray();
        JSONObject detailObj = new JSONObject();
        detailObj.put(Constants.productid, JUnitConstants.PRODUCT_ID);
        detailObj.put("desc", "sample Product Description");
        detailObj.put(Constants.QUENTITY, 1.0000);
        detailObj.put("baseuomquantity", 1.0000);
        detailObj.put("baseuomrate", 1);
        detailObj.put("rate", 200.00);
        detailObj.put("prdiscount", 0);
        detailObj.put("discountispercent", 1);
        detailObj.put("prtaxid", "");
        detailObj.put("taxamount", "0");
        detailObj.put("taxpercent", "0");
        detailObj.put("customfield", "");
        detailObj.put("productcustomfield", "");
       
        detailArr.put(detailObj);
        requestJobj.put(Constants.detail, detailArr.toString());
        
        JSONObject result = instance.saveSalesOrder(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testSaveSalesOrder");
        }
    }
    
    /**
     * Test of deleteSalesOrder method, of class TransactionServiceImpl.
     */
    @Test
    public void testDeleteSalesOrder() throws Exception {
        System.out.println("deleteSalesOrder");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.useridKey, JUnitConstants.USER_ID);
        requestJobj.put(Constants.remoteIPAddress, JUnitConstants.REMOTE_IP_ADDRESS);
        requestJobj.put(Constants.deletepermanentflag, true);
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        jobj.put(Constants.billid, "402880c95723d2b7015723d866610002");
        jarr.put(jobj);
        requestJobj.put(Constants.RES_data, jarr);
        JSONObject result = instance.deleteSalesOrder(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testDeleteSalesOrder");
        }
    }
    
    /**
     * Test of saveSalesReturn method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testSaveSalesReturn() throws Exception {
        System.out.println("saveSalesReturn");
        JSONObject requestJobj = new JSONObject();
        JSONObject jobj = new JSONObject();
        SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.remoteIPAddress, JUnitConstants.REMOTE_IP_ADDRESS);
        requestJobj.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(jobj));
        requestJobj.put(Constants.df, formatter);
        requestJobj.put(Constants.companyKey, JUnitConstants.COMPANY_ID);
        requestJobj.put(Constants.useridKey, JUnitConstants.USER_ID);
        requestJobj.put(Constants.currencyKey, JUnitConstants.CURRENCY_ID);
        requestJobj.put(Constants.globalCurrencyKey, JUnitConstants.GLOBAL_CURRENCY_ID);
        requestJobj.put("customer", JUnitConstants.CUSTOMER_ID);
        requestJobj.put(Constants.sequenceformat, "NA");
        requestJobj.put("number", "Test_SR0000001");
        requestJobj.put(Constants.BillDate, formatter.format(new Date()));
        requestJobj.put(Constants.duedate, formatter.format(new Date()));
        requestJobj.put("termid", JUnitConstants.TERM_ID);
        requestJobj.put("defaultAdress", "true");
        requestJobj.put("linkNumber", "");
        requestJobj.put(Constants.deleted, false);
       
        JSONArray detailArr = new JSONArray();
        JSONObject detailObj = new JSONObject();
        detailObj.put(Constants.productid, JUnitConstants.PRODUCT_ID);
        detailObj.put("desc", "sample Product Description");
        detailObj.put(Constants.QUENTITY, 1.0000);
        detailObj.put("baseuomquantity", 1.0000);
        detailObj.put("baseuomrate", 1);
        detailObj.put("rate", 200.00);
        detailObj.put("prdiscount", 0);
        detailObj.put("discountispercent", 1);
        detailObj.put("prtaxid", "");
        detailObj.put("taxamount", "0");
        detailObj.put("taxpercent", "0");
        detailObj.put("customfield", "");
        detailObj.put("productcustomfield", "");
       
        detailArr.put(detailObj);
        requestJobj.put(Constants.detail, detailArr.toString());
        
        
        JSONObject result = instance.saveSalesReturn(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testSaveSalesReturn");
        }
    }
    
    /**
     * Test of getSalesReturn method, of class TransactionServiceImpl.
     */
    @Test
    public void testGetSalesReturn() throws Exception {
        System.out.println("getSalesReturn");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.getlineItemDetailsflag, "false");
        requestJobj.put(Constants.moduleid, 29);
        requestJobj.put(Constants.moduleIds, "29");
        requestJobj.put(Constants.isdefaultHeaderMap, "false");
        JSONObject result = instance.getSalesReturn(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetSalesReturn");
        }
    }
    
    /**
     * Test of deleteSalesReturn method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testDeleteSalesReturn() throws Exception {
        System.out.println("deleteSalesReturn");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.useridKey, JUnitConstants.USER_ID);
        requestJobj.put(Constants.remoteIPAddress, JUnitConstants.REMOTE_IP_ADDRESS);
        requestJobj.put(Constants.deletepermanentflag, true);
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        jobj.put(Constants.billid, "402880c957278ef501572818c0640001");
        jarr.put(jobj);
        requestJobj.put(Constants.RES_data, jarr);
        JSONObject result = instance.deleteSalesReturn(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testDeleteSalesReturn");
        }
    }
    
    /**
     * Test of getCreditNote method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testGetCreditNote() throws Exception {
        System.out.println("getCreditNote");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.useridKey, JUnitConstants.USER_ID);
        requestJobj.put(Constants.remoteIPAddress, JUnitConstants.REMOTE_IP_ADDRESS);
        requestJobj.put(Constants.moduleid, 12);
        requestJobj.put(Constants.moduleIds, "12");
        JSONObject result = instance.getCreditNote(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetCreditNote");
        }
    }
    
    /**
     * Test of postSalaryJE method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testPostSalaryJE() throws Exception {
        System.out.println("postSalaryJE");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.currencyKey, JUnitConstants.CURRENCY_ID);
        requestJobj.put(Constants.globalCurrencyKey, JUnitConstants.GLOBAL_CURRENCY_ID);
        JSONArray jArr = new JSONArray();
        JSONObject jObj = new JSONObject();
        jObj.put("month", "September");
        jObj.put("name", "Test Name");
        jObj.put("salaryPayable", "100");
        jObj.put("salaryExpense", "100");
        jObj.put("cpfEmployerExpense", "100");
        jObj.put("cpfPayable", "100");
        jArr.put(jObj);
        requestJobj.put("jarr", jArr);

        JSONObject result = instance.postSalaryJE(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testPostSalaryJE");
        }
    }
    
    /**
     * Test of postReverseSalaryJE method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testPostReverseSalaryJE() throws Exception {
        System.out.println("postReverseSalaryJE");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.currencyKey, JUnitConstants.CURRENCY_ID);
        requestJobj.put(Constants.globalCurrencyKey, JUnitConstants.GLOBAL_CURRENCY_ID);
        JSONArray jArr = new JSONArray();
        JSONObject jObj = new JSONObject();
        jObj.put("jeid", "00000000570389bd015704c1c8d60007");
        jObj.put("salary", "100");
        jArr.put(jObj);
        requestJobj.put("jarr", jArr);

        JSONObject result = instance.postReverseSalaryJE(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testPostReverseSalaryJE");
        }
    }
    
    /**
     * Test of getCashRevenueTask method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testGetCashRevenueTask() throws Exception {
        System.out.println("getCashRevenueTask");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.currencyKey, JUnitConstants.CURRENCY_ID);
        requestJobj.put(Constants.globalCurrencyKey, JUnitConstants.GLOBAL_CURRENCY_ID);
        requestJobj.put("projectid", "402880c957330a790157330adf110001");
        requestJobj.put("taskid", "402880c957330a790157330adf110001");
        JSONObject result = instance.getCashRevenueTask(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetCashRevenueTask");
        }
    }
    
    /**
     * Test of getCashAndPurchaseRevenue method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testGetCashAndPurchaseRevenue() throws Exception {
        System.out.println("getCashAndPurchaseRevenue");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.currencyKey, JUnitConstants.CURRENCY_ID);
        requestJobj.put(Constants.globalCurrencyKey, JUnitConstants.GLOBAL_CURRENCY_ID);
        requestJobj.put(Constants.REQ_startdate, authHandler.getDateOnlyFormat().format(new Date()));
        requestJobj.put(Constants.REQ_enddate, authHandler.getDateOnlyFormat().format(new Date()));
        requestJobj.put("projectid", "402880c957330a790157330adf110001");
        JSONObject result = instance.getCashAndPurchaseRevenue(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetCashAndPurchaseRevenue");
        }
    }
    
    /**
     * Test of getVendorInvoicesReport method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testGetVendorInvoicesReport() throws Exception {
        System.out.println("getVendorInvoicesReport");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.currencyKey, JUnitConstants.CURRENCY_ID);
        requestJobj.put(Constants.globalCurrencyKey, JUnitConstants.GLOBAL_CURRENCY_ID);
        requestJobj.put(Constants.REQ_startdate, authHandler.getDateOnlyFormat().format(new Date()));
        requestJobj.put(Constants.REQ_enddate, authHandler.getDateOnlyFormat().format(new Date()));
        requestJobj.put("projectid", "402880c957330a790157330adf110001");
        JSONObject result = instance.getVendorInvoicesReport(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetVendorInvoicesReport");
        }
    }
    
    /**
     * Test of postAmountJE method, of class
     * TransactionServiceImpl.
     */
    @Test
    public void testPostAmountJE() throws Exception {
        System.out.println("postAmountJE");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", cdomain);
        requestJobj.put(Constants.currencyKey, JUnitConstants.CURRENCY_ID);
        requestJobj.put(Constants.globalCurrencyKey, JUnitConstants.GLOBAL_CURRENCY_ID);
        
        JSONArray jArr = new JSONArray();
        JSONObject jObj = new JSONObject();
        jObj.put("glDebitAccountId", "402880c954c2654c0154c35e963c0001");
        jObj.put("glCreditAccountId", "402880c954c2654c0154c3945cc30009");
        jObj.put("JEMemo", "Test memo for JE");
        jArr.put(jObj);
        requestJobj.put("data", jArr);
        
        JSONObject result = instance.postAmountJE(requestJobj);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testPostAmountJE");
        }
    }
    
}
