/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.goodsreceipt.service;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.hql.accounting.GoodsReceiptOrder;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
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
public class accGoodsReceiptModuleServiceTest {

    /**
     * Create instance of class.
     */
    @Autowired
    private accGoodsReceiptModuleService instance;
    private ImportPurchaseInvoice thread;
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
    public static final String COMPANY_ID = JUnitConstants.COMPANY_ID;
    public static final String USER_ID = JUnitConstants.USER_ID;
    public static final String USER_FULL_NAME = JUnitConstants.USER_FULL_NAME;
    public static final String CURRENCY_ID = JUnitConstants.CURRENCY_ID;
    public static final String GLOBAL_CURRENCY_ID = JUnitConstants.GLOBAL_CURRENCY_ID;
    public static final String GLOBAL_CURRENCY_NAME = JUnitConstants.GLOBAL_CURRENCY_NAME;
    public static final String PRODUCT_UUID = JUnitConstants.PRODUCT_UUID;
    public static final String UOM_ID = JUnitConstants.UOM_ID;
    public static final String TERM_ID = JUnitConstants.UOM_ID;
    public static final String GRO_ID = JUnitConstants.GRO_ID;
    public static final String PO_ID = JUnitConstants.PO_ID;
    public static final String GR_ID = JUnitConstants.GR_ID;
    public static final String VENDOR_ID = JUnitConstants.VENDOR_ID;
    public static final String VENDOR_CODE =JUnitConstants.VENDOR_CODE;
    public static final String VENDOR_NAME = JUnitConstants.VENDOR_NAME;
    public static final String VI_NUMBER = JUnitConstants.VI_NUMBER;
    public static final String JE_ID = JUnitConstants.JE_ID;
    public static final String DISCOUNT_ID = JUnitConstants.DISCOUNT_ID;
    public static final String MACHINE_ID = JUnitConstants.MACHINE_ID;
    public static final String ASSET_DETAIL_ID = JUnitConstants.ASSET_DETAIL_ID;
    public static final String DATE_FORMAT_ID = JUnitConstants.DATE_FORMAT_ID;
    public static final char COMMA = JUnitConstants.COMMA;
    public static final String IMPORT_FILE_NAME = JUnitConstants.IMPORT_FILE_NAME;
    /**
     * Detail json array of product for saving transaction of single product.
     */
    public static final String PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT = JUnitConstants.PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT;
    public static final String ASSET_DETAIL_JSON_ARRAY_PRODUCT = JUnitConstants.ASSET_DETAIL_JSON_ARRAY_PRODUCT;
    public static final String PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT_FOR_FIXED_ASSET = JUnitConstants.PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT_FOR_FIXED_ASSET;

    public accGoodsReceiptModuleServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
//        System.setProperty("com.krawler.config.location", "/home/krawler/AccountingProjects/Financials2/Maven_Accounting/src/main/resources");
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
     * Test of saveGoodsReceipt method, of class accGoodsReceiptModuleService.
     */
    @Test
    public void testSaveGoodsReceipt() throws Exception {
        System.out.println("saveGoodsReceipt");
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
        paramJobj.put("vendor", VENDOR_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test GR001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        Map<String, String> map = new HashMap<>();

        List result = instance.saveGoodsReceipt(paramJobj, map);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveGoodsReceipt method for Fixed Asset scenario, of class accGoodsReceiptModuleService.
     */
    @Test
    public void testSaveGoodsReceiptForFixedAsset() throws Exception {
        System.out.println("saveGoodsReceipt_ForFixedAsset");
        Map<String, Object> requestParams = new HashMap<>();
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
        paramJobj.put("vendor", VENDOR_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test GR001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put("isFixedAsset", "true");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT_FOR_FIXED_ASSET);

        Map<String, String> map = new HashMap<>();

        List result = instance.saveGoodsReceipt(paramJobj, map);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveGoodsReceipt method for Consignment scenario, of class accGoodsReceiptModuleService.
     */
    @Test
    public void testSaveGoodsReceiptForConsignment() throws Exception {
        System.out.println("saveGoodsReceipt_ForConsignment");
        Map<String, Object> requestParams = new HashMap<>();
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
        paramJobj.put("vendor", VENDOR_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test GR001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put("isConsignment", "true");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        Map<String, String> map = new HashMap<>();

        List result = instance.saveGoodsReceipt(paramJobj, map);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveGoodsReceiptOrder method, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testSaveGoodsReceiptOrder_3args() throws Exception {
        System.out.println("saveGoodsReceiptOrder");
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
        paramJobj.put("vendor", VENDOR_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test GRO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        String invoiceid = "";
        Map<String, String> map = new HashMap<>();

        List result = instance.saveGoodsReceiptOrder(paramJobj, invoiceid, map);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveGoodsReceiptOrder method, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testSaveGoodsReceiptOrder_JSONObject() throws Exception {
        System.out.println("saveGoodsReceiptOrder");
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
        paramJobj.put("vendor", VENDOR_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test GRO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        JSONObject result = instance.saveGoodsReceiptOrder(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveGoodsReceiptOrder method for Fixed Asset scenario, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testSaveGoodsReceiptOrderForFixedAsset_JSONObject() throws Exception {
        System.out.println("saveGoodsReceiptOrder_ForFixedAsset");
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
        paramJobj.put("vendor", VENDOR_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test GRO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put("isFixedAsset", "true");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT_FOR_FIXED_ASSET);

        JSONObject result = instance.saveGoodsReceiptOrder(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveGoodsReceiptOrder method for Consignment scenario, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testSaveGoodsReceiptOrderForConsignment_JSONObject() throws Exception {
        System.out.println("saveGoodsReceiptOrder_ForConsignment");
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
        paramJobj.put("vendor", VENDOR_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        paramJobj.put(Constants.sequenceformat, "NA");
        paramJobj.put("number", "Test GRO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put("isConsignment", "true");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        JSONObject result = instance.saveGoodsReceiptOrder(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of importGoodsReceiptOrdersJSON method, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testImportGoodsReceiptOrdersJSON() throws Exception {
        System.out.println("importGoodsReceiptOrdersJSON");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put("do", "import");
        paramJobj.put("extraParams", "");
        paramJobj.put("modName", "GoodsReceiptOrder");
        paramJobj.put("moduleName", "GoodsReceiptOrder");
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

        JSONObject result = instance.importGoodsReceiptOrdersJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of updatePOisOpenAndLinkingWithGR method, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testUpdatePOisOpenAndLinkingWithGR() throws Exception {
        System.out.println("updatePOisOpenAndLinkingWithGR");
        String linking = PO_ID;
        String grorderId = GRO_ID;

        instance.updatePOisOpenAndLinkingWithGR(linking, grorderId);
        assertEquals(true, true);
    }

    /**
     * Test of updatePIisOpenAndLinkingWithGR method, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testUpdatePIisOpenAndLinkingWithGR() throws Exception {
        System.out.println("updatePIisOpenAndLinkingWithGR");
        String linking = GR_ID;

        instance.updatePIisOpenAndLinkingWithGR(linking);
        assertEquals(true, true);
    }

    /**
     * Test of approveGRO method, of class accGoodsReceiptModuleService.
     */
    @Test
    public void testApproveGRO() throws Exception {
        System.out.println("approveGRO");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), GRO_ID);
        GoodsReceiptOrder groObj = (GoodsReceiptOrder) res.getEntityList().get(0);
        HashMap<String, Object> grApproveMap = new HashMap<>();
        grApproveMap.put(Constants.companyKey, COMPANY_ID);
        grApproveMap.put("currentUser", USER_ID);
        grApproveMap.put("totalAmount", 100);
        grApproveMap.put(Constants.moduleid, Constants.Acc_Goods_Receipt_ModuleId);
        boolean isMailApplicable = false;

        List result = instance.approveGRO(groObj, grApproveMap, isMailApplicable);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getCurrencyMap method, of class accGoodsReceiptModuleService.
     */
    @Test
    public void testGetCurrencyMap() throws Exception {
        System.out.println("getCurrencyMap");
        boolean isCurrencyCode = false;

        HashMap result = instance.getCurrencyMap(isCurrencyCode);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getCurrencyId method, of class accGoodsReceiptModuleService.
     */
    @Test
    public void testGetCurrencyId() throws Exception {
        System.out.println("getCurrencyId");
        String currencyName = GLOBAL_CURRENCY_NAME;
        boolean isCurrencyCode = false;
        HashMap currencyMap = instance.getCurrencyMap(isCurrencyCode);

        String expResult = GLOBAL_CURRENCY_ID;
        String result = instance.getCurrencyId(currencyName, currencyMap);
        assertEquals(expResult, result);
    }

    /**
     * Test of createCSVrecord method, of class accGoodsReceiptModuleService.
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
     * Test of getVendorByCode method, of class accGoodsReceiptModuleService.
     */
    @Test
    public void testGetVendorByCode() throws Exception {
        System.out.println("getVendorByCode");
        String vendorCode = VENDOR_CODE;
        String companyID = COMPANY_ID;

        Vendor result = instance.getVendorByCode(vendorCode, companyID);
        assertNotNull(result);
    }

    /**
     * Test of getVendorByName method, of class accGoodsReceiptModuleService.
     */
    @Test
    public void testGetVendorByName() throws Exception {
        System.out.println("getVendorByName");
        String vendorName = VENDOR_NAME;
        String companyID = COMPANY_ID;

        Vendor result = instance.getVendorByName(vendorName, companyID);
        assertNotNull(result);
    }

    /**
     * Test of importPurchaseInvoiceJSON method, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testImportPurchaseInvoiceJSON() throws Exception {
        System.out.println("importPurchaseInvoiceJSON");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put("do", "import");
        paramJobj.put("extraParams", "");
        paramJobj.put("modName", "Vendor Invoice");
        paramJobj.put("moduleName", "Vendor Invoice");
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

        JSONObject result = thread.importPurchaseInvoiceJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of importPurchaseInvoiceRecordsForCSV method, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testImportPurchaseInvoiceRecordsForCSV() throws Exception {
        System.out.println("importPurchaseInvoiceRecordsForCSV");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put("do", "import");
        paramJobj.put("extraParams", "");
        paramJobj.put("modName", "Vendor Invoice");
        paramJobj.put("moduleName", "Vendor Invoice");
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

        JSONObject result = thread.importPurchaseInvoiceRecordsForCSV(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of deleteEntryInTemp method, of class accGoodsReceiptModuleService.
     */
    @Test
    public void testDeleteEntryInTemp() {
        System.out.println("deleteEntryInTemp");
        Map deleteparam = new HashMap();
        deleteparam.put("invoiceno", VI_NUMBER);
        deleteparam.put("grno", "");
        deleteparam.put(Constants.companyKey, COMPANY_ID);

        instance.deleteEntryInTemp(deleteparam);
        assertEquals(true, true);
    }

    /**
     * Test of deleteEditedGoodsReceiptJE method, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testDeleteEditedGoodsReceiptJE() throws Exception {
        System.out.println("deleteEditedGoodsReceiptJE");
        String oldjeid = JE_ID;
        String companyid = COMPANY_ID;

        instance.deleteEditedGoodsReceiptJE(oldjeid, companyid);
        assertEquals(true, true);
    }

    /**
     * Test of deleteEditedGoodsReceiptDiscount method, of class
     * accGoodsReceiptModuleService.
     */
    @Test
    public void testDeleteEditedGoodsReceiptDiscount() throws Exception {
        System.out.println("deleteEditedGoodsReceiptDiscount");
        ArrayList discArr = new ArrayList<>();
        discArr.add(DISCOUNT_ID);
        String companyid = COMPANY_ID;

        instance.deleteEditedGoodsReceiptDiscount(discArr, companyid);
        assertEquals(true, true);
    }

    /**
     * Test of savemachineAsset method, of class accGoodsReceiptModuleService.
     */
    @Test
    public void testSavemachineAsset() throws Exception {
        System.out.println("savemachineAsset");
        Map<String, String> map = new HashMap<>();
        map.put(MACHINE_ID, ASSET_DETAIL_ID);
        String company = COMPANY_ID;

        instance.savemachineAsset(map, company);
        assertEquals(true, true);
    }
}
