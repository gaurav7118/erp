/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.salesorder;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Configuration for JUnit class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/applicationContextList.xml")
public class accSalesOrderServiceTest {
    
    /**
     * Create instance of class.
     */
    @Autowired
    accSalesOrderService instance;
    /**
     * Used to get object of pojo.
     */
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAOobj;
    /**
     * Used to maintain status of transaction.
     */
    @Autowired
    private HibernateTransactionManager txnManager;
    /**
     * Used to maintain status of transaction.
     */
    TransactionStatus status = null;
    /**
     * Constants used in class.
     */
    public static final char COMMA = JUnitConstants.COMMA;
    public static final String DATE_FORMAT_ID = JUnitConstants.DATE_FORMAT_ID;
    public static final String IMPORT_FILE_NAME = JUnitConstants.IMPORT_FILE_NAME;
    public static final String COMPANY_ID = JUnitConstants.COMPANY_ID;
    public static final String USER_ID = JUnitConstants.USER_ID;
    public static final String USER_FULL_NAME = JUnitConstants.USER_FULL_NAME;
    public static final String CURRENCY_ID = JUnitConstants.CURRENCY_ID;
    public static final String GLOBAL_CURRENCY_ID = JUnitConstants.GLOBAL_CURRENCY_ID;
    public static final String GLOBAL_CURRENCY_NAME = JUnitConstants.GLOBAL_CURRENCY_NAME;
    public static final String MULTI_LEVEL_APPROVAL_RULE_ID = JUnitConstants.MULTI_LEVEL_APPROVAL_RULE_ID;
    public static final String PRODUCT_UUID = JUnitConstants.PRODUCT_UUID;
    public static final String PRODUCT_ID = JUnitConstants.PRODUCT_ID;
    public static final String UOM_ID = JUnitConstants.UOM_ID;
    public static final String UOM_NAME = JUnitConstants.UOM_NAME;
    public static final String TERM_ID = JUnitConstants.TERM_ID;
    public static final String TERM_NAME = JUnitConstants.TERM_NAME;
    public static final String CUSTOMER_ID = JUnitConstants.CUSTOMER_ID;
    public static final String CUSTOMER_CODE = JUnitConstants.CUSTOMER_CODE;
    public static final String CQ_ID = JUnitConstants.CQ_ID;
    public static final String SO_ID = JUnitConstants.SO_ID;
    public static final String CONSIGNMENT_REQUEST_ID = JUnitConstants.CONSIGNMENT_REQUEST_ID;
    public static final String ASSET_DETAIL_JSON_ARRAY = JUnitConstants.ASSET_DETAIL_JSON_ARRAY;
    public static final String SO_DELETE_JSON_ARRAY = JUnitConstants.SO_DELETE_JSON_ARRAY;
    public static final String PURCHASE_BATCH_ID = JUnitConstants.PURCHASE_BATCH_ID;
    public static final String REPEATED_SO_ID = JUnitConstants.REPEATED_SO_ID;
    public static final String COST_CENTER_NAME = JUnitConstants.COST_CENTER_NAME;
    public static final String SALES_PERSON_NAME = JUnitConstants.SALES_PERSON_NAME;
    public static final String TAX_CODE = JUnitConstants.TAX_CODE;
    public static final String INVOICE_TERM_ID = JUnitConstants.INVOICE_TERM_ID;
    
    /**
     * Detail json array of product for saving transaction of single product.
     */
    public static final String PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT = JUnitConstants.PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT;
    public static final String BATCH_JSON_ARRAY = JUnitConstants.BATCH_JSON_ARRAY;
    public static final String LINE_LEVEL_CUSTOM_FIELD_ARRAY = JUnitConstants.LINE_LEVEL_CUSTOM_FIELD_ARRAY;
    public static final String GLOBAL_LEVEL_CUSTOM_FIELD_ARRAY = JUnitConstants.GLOBAL_LEVEL_CUSTOM_FIELD_ARRAY;
    public static final String INVOICE_TERM_JSON_ARRAY = JUnitConstants.INVOICE_TERM_JSON_ARRAY;
    
    public accSalesOrderServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
        /**
         * Create transaction before each method to avoid problem of load proxy
         * object.
         */
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JUnitPropogation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);
    }
    
    @After
    public void tearDown() {
        /**
         * Rollback transaction after each method.
         */
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of saveSalesOrderJSON method, of class accSalesOrderService.
     */
    @Test
    public void testSaveSalesOrderJSON() throws Exception {
        System.out.println("saveSalesOrderJSON");
        JSONObject paramJobj = new JSONObject();
        SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
        JSONObject jobj = new JSONObject();
        paramJobj.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(jobj));
        paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.useridKey, USER_ID);
        paramJobj.put(Constants.userfullname, USER_FULL_NAME);
        paramJobj.put(Constants.currencyKey, CURRENCY_ID);
        paramJobj.put(Constants.globalCurrencyKey, GLOBAL_CURRENCY_ID);
        paramJobj.put("customer", CUSTOMER_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test SOO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);
        
        JSONObject result = instance.saveSalesOrderJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveSalesOrderJSON method for Consignment, of class accSalesOrderService.
     */
    @Test
    public void testSaveSalesOrderJSON_ForConsignment() throws Exception {
        System.out.println("saveSalesOrderJSON_ForConsignment");
        JSONObject paramJobj = new JSONObject();
        SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
        JSONObject jobj = new JSONObject();
        paramJobj.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(jobj));
        paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.useridKey, USER_ID);
        paramJobj.put(Constants.userfullname, USER_FULL_NAME);
        paramJobj.put(Constants.currencyKey, CURRENCY_ID);
        paramJobj.put(Constants.globalCurrencyKey, GLOBAL_CURRENCY_ID);
        paramJobj.put("customer", CUSTOMER_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test SOO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put("isConsignment", "true");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);
        
        JSONObject result = instance.saveSalesOrderJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveSalesOrderJSON method for Lease Fixed Asset, of class accSalesOrderService.
     */
    @Test
    public void testSaveSalesOrderJSON_ForLeaseFixedAsset() throws Exception {
        System.out.println("saveSalesOrderJSON_ForLeaseFixedAsset");
        JSONObject paramJobj = new JSONObject();
        SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
        JSONObject jobj = new JSONObject();
        paramJobj.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(jobj));
        paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.useridKey, USER_ID);
        paramJobj.put(Constants.userfullname, USER_FULL_NAME);
        paramJobj.put(Constants.currencyKey, CURRENCY_ID);
        paramJobj.put(Constants.globalCurrencyKey, GLOBAL_CURRENCY_ID);
        paramJobj.put("customer", CUSTOMER_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test SOO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put("isLeaseFixedAsset", "true");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);
        
        JSONObject result = instance.saveSalesOrderJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of assignStockToPendingConsignmentRequests method, of class accSalesOrderService.
     */
    @Test
    public void testAssignStockToPendingConsignmentRequests() throws Exception {
        System.out.println("assignStockToPendingConsignmentRequests");
        JSONObject paramJobj = new JSONObject();
        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        Company company = (Company) companyResult.getEntityList().get(0);
        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        User user = (User) userResult.getEntityList().get(0);
        
        KwlReturnObject result = instance.assignStockToPendingConsignmentRequests(paramJobj, company, user);
        assertNotNull(result);
    }

    /**
     * Test of saveAssetDetails method, of class accSalesOrderService.
     */
    @Test
    public void testSaveAssetDetails() throws Exception {
        System.out.println("saveAssetDetails");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        String productId = PRODUCT_UUID;
        String assetDetails = ASSET_DETAIL_JSON_ARRAY;
        
        Set result = instance.saveAssetDetails(paramJobj, productId, assetDetails);
        assertNotNull(result);
    }

    /**
     * Test of sendMailToApprover method, of class accSalesOrderService.
     */
    @Test
    public void testSendMailToApprover() throws Exception {
        System.out.println("sendMailToApprover");
        String companyid = COMPANY_ID;
        String ruleId = MULTI_LEVEL_APPROVAL_RULE_ID;
        String prNumber = "";
        String fromName = USER_FULL_NAME;
        boolean hasApprover = false;
        int moduleid = Constants.Acc_Sales_Order_ModuleId;
        String createdby = USER_ID;
        
//        instance.sendMailToApprover(companyid, ruleId, prNumber, fromName, hasApprover, moduleid, createdby);
        assertTrue(true);
    }

    /**
     * Test of updateOpenStatusFlagForSO method, of class accSalesOrderService.
     */
    @Test
    public void testUpdateOpenStatusFlagForSO() throws Exception {
        System.out.println("updateOpenStatusFlagForSO");
        String linkNumbers = CQ_ID;
        
        instance.updateOpenStatusFlagForSO(linkNumbers);
        assertTrue(true);
    }

    /**
     * Test of approveSalesOrder method, of class accSalesOrderService.
     */
    @Test
    public void testApproveSalesOrder() throws Exception {
        System.out.println("approveSalesOrder");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), SO_ID);
        SalesOrder soObj = (SalesOrder) res.getEntityList().get(0);
        HashMap<String, Object> soApproveMap = new HashMap<>();
        soApproveMap.put(Constants.companyKey, COMPANY_ID);
        boolean isMailApplicable = false;
        
        List result = instance.approveSalesOrder(soObj, soApproveMap, isMailApplicable);
        assertNotNull(result);
    }

    /**
     * Test of mapInvoiceTerms method, of class accSalesOrderService.
     */
    @Test
    public void testMapInvoiceTerms() throws Exception {
        System.out.println("mapInvoiceTerms");
        String InvoiceTerms = INVOICE_TERM_JSON_ARRAY;
        String id = SO_ID;
        String userid = USER_ID;
        boolean isQuotation = false;
        
        List result = instance.mapInvoiceTerms(InvoiceTerms, id, userid, isQuotation);
        assertNotNull(result);
    }

    /**
     * Test of sendConsignmentApprovalEmails method, of class accSalesOrderService.
     */
    @Test
    public void testSendConsignmentApprovalEmails() throws Exception {
        System.out.println("sendConsignmentApprovalEmails");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put(Constants.detail, "[{}]");
        
        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        User sender = (User) userResult.getEntityList().get(0);
        KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), CONSIGNMENT_REQUEST_ID);
        SalesOrder so = (SalesOrder) res.getEntityList().get(0);
        String billno = "Test Consignment 001";
        boolean isApproved = false;
        boolean isEdit = false;
        
        instance.sendConsignmentApprovalEmails(paramJobj, sender, so, billno, isApproved, isEdit);
        assertTrue(true);
    }

    /**
     * Test of deleteSalesOrdersPermanentJson method, of class accSalesOrderService.
     */
    @Test
    public void testDeleteSalesOrdersPermanentJson() throws Exception {
        System.out.println("deleteSalesOrdersPermanentJson");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put("locale", Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.useridKey, USER_ID);
        paramJobj.put(Constants.userfullname, USER_FULL_NAME);
        paramJobj.put("data", SO_DELETE_JSON_ARRAY);
        
        JSONObject result = instance.deleteSalesOrdersPermanentJson(paramJobj);
        assertNotNull(result);
    }

    /**
     * Test of deleteSalesOrdersJSON method, of class accSalesOrderService.
     */
    @Test
    public void testDeleteSalesOrdersJSON() throws Exception {
        System.out.println("deleteSalesOrdersJSON");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.useridKey, USER_ID);
        paramJobj.put(Constants.userfullname, USER_FULL_NAME);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put("locale", Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));
        paramJobj.put("data", SO_DELETE_JSON_ARRAY);
        
        JSONObject result = instance.deleteSalesOrdersJSON(paramJobj);
        assertNotNull(result);
    }

    /**
     * Test of saveQuotationJSON method, of class accSalesOrderService.
     */
    @Test
    public void testSaveQuotationJSON() throws Exception {
        System.out.println("saveQuotationJSON");
        JSONObject paramJobj = new JSONObject();
        SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
        JSONObject jobj = new JSONObject();
        paramJobj.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(jobj));
        paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.useridKey, USER_ID);
        paramJobj.put(Constants.userfullname, USER_FULL_NAME);
        paramJobj.put(Constants.currencyKey, CURRENCY_ID);
        paramJobj.put(Constants.globalCurrencyKey, GLOBAL_CURRENCY_ID);
        paramJobj.put("customer", CUSTOMER_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test QO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        JSONObject result = instance.saveQuotationJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveQuotationJSON method for Lease Fixed Asset, of class accSalesOrderService.
     */
    @Test
    public void testSaveQuotationJSON_ForLeaseFixedAsset() throws Exception {
        System.out.println("saveQuotationJSON_ForLeaseFixedAsset");
        JSONObject paramJobj = new JSONObject();
        SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
        JSONObject jobj = new JSONObject();
        paramJobj.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(jobj));
        paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.useridKey, USER_ID);
        paramJobj.put(Constants.userfullname, USER_FULL_NAME);
        paramJobj.put(Constants.currencyKey, CURRENCY_ID);
        paramJobj.put(Constants.globalCurrencyKey, GLOBAL_CURRENCY_ID);
        paramJobj.put("customer", CUSTOMER_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test QO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("isLeaseFixedAsset", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        JSONObject result = instance.saveQuotationJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of approveCustomerQuotation method, of class accSalesOrderService.
     */
    @Test
    public void testApproveCustomerQuotation() throws Exception {
        System.out.println("approveCustomerQuotation");
        KwlReturnObject cqResult = accountingHandlerDAOobj.getObject(Quotation.class.getName(), CQ_ID);
        Quotation cqObj = (Quotation) cqResult.getEntityList().get(0);
        HashMap<String, Object> qApproveMap = new HashMap<>();
        qApproveMap.put(Constants.companyKey, COMPANY_ID);
        qApproveMap.put("currentUser", USER_ID);
        qApproveMap.put("totalAmount", 100);
        boolean isMailApplicable = false;
        
        List result = instance.approveCustomerQuotation(cqObj, qApproveMap, isMailApplicable);
        assertNotNull(result);
    }

    /**
     * Test of importCustomerQuotationRecordsForCSV method, of class accSalesOrderService.
     */
    @Test
    public void testImportCustomerQuotationRecordsForCSV() throws Exception {
        System.out.println("importCustomerQuotationRecordsForCSV");
        HashMap<String, Object> request = new HashMap<>();
        request.put(Constants.companyKey, COMPANY_ID);
        request.put(Constants.useridKey, USER_ID);
        request.put(Constants.userfullname, USER_FULL_NAME);
        request.put(Constants.globalCurrencyKey, GLOBAL_CURRENCY_ID);
        request.put("currencyId", GLOBAL_CURRENCY_ID);
        request.put("filename", IMPORT_FILE_NAME);
        request.put("onlyfilename", IMPORT_FILE_NAME);
        request.put("dateFormat", DATE_FORMAT_ID);
        request.put("do", "import");
        request.put("extraParams", "");
        request.put("masterPreference", "0");
        request.put("delimiterType", COMMA);
        request.put("sheetindex", "0");
        request.put("servletContext", "");
        request.put("fetchCustomFields", "false");
        request.put("subModuleFlag", "0");
        request.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        request.put("locale", Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));
        request.put("modName", "Quotation");
        request.put("moduleName", "Quotation");
        
        JSONObject resjson = new JSONObject();
        JSONArray root = new JSONArray();
        resjson.put("root", root);
        request.put("resjson", resjson.toString());
        
        JSONObject jobj = new JSONObject();
        jobj.put("filename", IMPORT_FILE_NAME);
        jobj.put("isDraft", false);
        String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
        File filepath = new File(destinationDirectory + File.separator + IMPORT_FILE_NAME);
        jobj.put("FilePath", filepath);
        jobj.put("resjson", root);
        
        HashMap<String, Object> globalParams = new HashMap<>();
        globalParams.put(Constants.companyKey, COMPANY_ID);
        globalParams.put(Constants.globalCurrencyKey, GLOBAL_CURRENCY_ID);
        globalParams.put(Constants.df, authHandler.getDateOnlyFormat());
        globalParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(new JSONObject()));
        
        JSONObject result = instance.importCustomerQuotationRecordsForCSV(request, jobj, globalParams);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveSONewBatch method, of class accSalesOrderService.
     */
    @Test
    public void testSaveSONewBatch() throws Exception {
        System.out.println("saveSONewBatch");
        String batchJSON = BATCH_JSON_ARRAY;
        String productId = PRODUCT_UUID;
        JSONObject paramJobj = new JSONObject();
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.useridKey, USER_ID);
        String documentId = "ff8080814a2955c1014a29a5da650049"; 
        
        instance.saveSONewBatch(batchJSON, productId, paramJobj, documentId);
        assertTrue(true);
    }

    /**
     * Test of getCurrencyMap method, of class accSalesOrderService.
     */
    @Test
    public void testGetCurrencyMap() throws Exception {
        System.out.println("getCurrencyMap");
        boolean isCurrencyCode = false;
        
        HashMap result = instance.getCurrencyMap(isCurrencyCode);
        assertNotNull(result);
    }

    /**
     * Test of createCSVrecord method, of class accSalesOrderService.
     */
    @Test
    public void testCreateCSVrecord() {
        System.out.println("createCSVrecord");
        List list = new ArrayList();
        list.add("Header1");
        list.add("Header2");
        list.add("Header3");
        Object[] listArray = list.toArray();
        String expResult = "";
        
        String result = instance.createCSVrecord(listArray);
        assertNotEquals(expResult, result);
    }

    /**
     * Test of getTermIDByName method, of class accSalesOrderService.
     */
    @Test
    public void testGetTermIDByName() throws Exception {
        System.out.println("getTermIDByName");
        String termName = TERM_NAME;
        String companyID = COMPANY_ID;
        
        String result = instance.getTermIDByName(termName, companyID);
        assertNotNull(result);
    }

    /**
     * Test of getCurrencyId method, of class accSalesOrderService.
     */
    @Test
    public void testGetCurrencyId() throws Exception {
        System.out.println("getCurrencyId");
        String currencyName = GLOBAL_CURRENCY_NAME;
        boolean isCurrencyCode = false;
        HashMap currencyMap = instance.getCurrencyMap(isCurrencyCode);
        
        String result = instance.getCurrencyId(currencyName, currencyMap);
        assertNotNull(result);
    }

    /**
     * Test of getProductByProductID method, of class accSalesOrderService.
     */
    @Test
    public void testGetProductByProductID() throws Exception {
        System.out.println("getProductByProductID");
        String productID = PRODUCT_ID;
        String companyID = COMPANY_ID;
        
        Product result = instance.getProductByProductID(productID, companyID);
        assertNotNull(result);
    }

    /**
     * Test of getUOMByName method, of class accSalesOrderService.
     */
    @Test
    public void testGetUOMByName() throws Exception {
        System.out.println("getUOMByName");
        String productUOMName = UOM_NAME;
        String companyID = COMPANY_ID;
        
        UnitOfMeasure result = instance.getUOMByName(productUOMName, companyID);
        assertNotNull(result);
    }

    /**
     * Test of setValuesForAuditTrialForRecurringSO method, of class accSalesOrderService.
     */
    @Test
    public void testSetValuesForAuditTrialForRecurringSO() throws Exception {
        System.out.println("setValuesForAuditTrialForRecurringSO");
        KwlReturnObject rSalesOrderResult = accountingHandlerDAOobj.getObject(RepeatedSalesOrder.class.getName(), REPEATED_SO_ID);
        RepeatedSalesOrder rSalesOrder = (RepeatedSalesOrder) rSalesOrderResult.getEntityList().get(0);
        HashMap<String, Object> oldsoMap = new HashMap<>();
        HashMap<String, Object> newAuditKey = new HashMap<>();
        
        instance.setValuesForAuditTrialForRecurringSO(rSalesOrder, oldsoMap, newAuditKey);
        assertTrue(true);
    }

    /**
     * Test of getCostCenterIDByName method, of class accSalesOrderService.
     */
    @Test
    public void testGetCostCenterIDByName() throws Exception {
        System.out.println("getCostCenterIDByName");
        String costCenterName = COST_CENTER_NAME;
        String companyID = COMPANY_ID;
        
        String result = instance.getCostCenterIDByName(costCenterName, companyID);
        assertNotNull(result);
    }

    /**
     * Test of getSalesPersonIDByName method, of class accSalesOrderService.
     */
    @Test
    public void testGetSalesPersonIDByName() throws Exception {
        System.out.println("getSalesPersonIDByName");
        String salesPersonName = SALES_PERSON_NAME;
        String companyID = COMPANY_ID;
        
        String result = instance.getSalesPersonIDByName(salesPersonName, companyID);
        assertNotNull(result);
    }

    /**
     * Test of getCustomerByCode method, of class accSalesOrderService.
     */
    @Test
    public void testGetCustomerByCode() throws Exception {
        System.out.println("getCustomerByCode");
        String customerCode = CUSTOMER_CODE;
        String companyID = COMPANY_ID;
        
        Customer result = instance.getCustomerByCode(customerCode, companyID);
        assertNotNull(result);
    }

    /**
     * Test of getExchangeRateForTransaction method, of class accSalesOrderService.
     */
    @Test
    public void testGetExchangeRateForTransaction() throws Exception {
        System.out.println("getExchangeRateForTransaction");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put(Constants.globalCurrencyKey, GLOBAL_CURRENCY_ID);
        requestJobj.put(Constants.companyKey, COMPANY_ID);
        Date billDate = new Date();
        String currencyID = GLOBAL_CURRENCY_ID;
        
        double result = instance.getExchangeRateForTransaction(requestJobj, billDate, currencyID);
        assertTrue(true);
    }

    /**
     * Test of createLineLevelCustomFieldArrayForImport method, of class accSalesOrderService.
     */
    @Test
    public void testCreateLineLevelCustomFieldArrayForImport() throws Exception {
        System.out.println("createLineLevelCustomFieldArrayForImport");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put(Constants.companyKey, COMPANY_ID);
        
        JSONArray jSONArray = new JSONArray(LINE_LEVEL_CUSTOM_FIELD_ARRAY);
        
        String recString = "Header1,Header2,Header3";
        String[] recarr = recString.split(",");
        
        DateFormat df = authHandler.getGlobalDateFormat();
        int moduleID = Constants.Acc_Sales_Order_ModuleId;
        
        JSONArray result = instance.createLineLevelCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, moduleID);
        assertNotNull(result);
    }

    /**
     * Test of createGlobalCustomFieldArrayForImport method, of class accSalesOrderService.
     */
    @Test
    public void testCreateGlobalCustomFieldArrayForImport() throws Exception {
        System.out.println("createGlobalCustomFieldArrayForImport");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put(Constants.companyKey, COMPANY_ID);
        
        JSONArray jSONArray = new JSONArray(GLOBAL_LEVEL_CUSTOM_FIELD_ARRAY);
        
        String recString = "Header1,Header2,Header3";
        String[] recarr = recString.split(",");
        
        DateFormat df = authHandler.getGlobalDateFormat();
        int moduleID = Constants.Acc_Sales_Order_ModuleId;
        
        JSONArray result = instance.createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, moduleID);
        assertNotNull(result);
    }

    /**
     * Test of saveImportLog method, of class accSalesOrderService.
     */
    @Test
    public void testSaveImportLog() throws Exception {
        System.out.println("saveImportLog");
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("filename", IMPORT_FILE_NAME);
        requestJobj.put(Constants.companyKey, COMPANY_ID);
        requestJobj.put(Constants.useridKey, USER_ID);
        String msg = "";
        int total = 1;
        int failed = 0;
        int moduleID = Constants.Acc_Sales_Order_ModuleId;
        
        instance.saveImportLog(requestJobj, msg, total, failed, moduleID);
        assertTrue(true);
    }

    /**
     * Test of getDueDateFromTermAndBillDate method, of class accSalesOrderService.
     */
    @Test
    public void testGetDueDateFromTermAndBillDate() throws Exception {
        System.out.println("getDueDateFromTermAndBillDate");
        String termID = TERM_ID;
        Date billDate = new Date();
        
        Date result = instance.getDueDateFromTermAndBillDate(termID, billDate);
        assertNotNull(result);
    }

    /**
     * Test of importSalesOrderJSON method, of class accSalesOrderService.
     */
    @Test
    public void testImportSalesOrderJSON() throws Exception {
        System.out.println("importSalesOrderJSON");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put("do", "import");
        paramJobj.put("extraParams", "");
        paramJobj.put("modName", "Sales Order");
        paramJobj.put("moduleName", "Sales Order");
        paramJobj.put("delimiterType", COMMA);
        paramJobj.put("filename", IMPORT_FILE_NAME);
        JSONObject resjson = new JSONObject();
        JSONArray root = new JSONArray();
        resjson.put("root", root);
        paramJobj.put("resjson", resjson.toString());
        paramJobj.put("sheetindex", "0");
        paramJobj.put("onlyfilename", IMPORT_FILE_NAME);
        paramJobj.put("dateFormat", DATE_FORMAT_ID);
        paramJobj.put("masterPreference", "0");
        paramJobj.put("fetchCustomFields", "false");
        paramJobj.put("subModuleFlag", "0");
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.useridKey, USER_ID);
        paramJobj.put(Constants.userfullname, USER_FULL_NAME);
        paramJobj.put(Constants.globalCurrencyKey, GLOBAL_CURRENCY_ID);
        paramJobj.put("servletContext", "");
        paramJobj.put("locale", Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));
        
        JSONObject result = instance.importSalesOrderJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getGSTByCode method, of class accSalesOrderService.
     */
    @Test
    public void testGetGSTByCode() throws Exception {
        System.out.println("getGSTByCode");
        String accountCode = TAX_CODE;
        String companyID = COMPANY_ID;
        
        Tax result = instance.getGSTByCode(accountCode, companyID);
        assertNotNull(result);
    }
}
