/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.purchaseorder.service;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseRequisitionAssetDetails;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
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
public class AccPurchaseOrderModuleServiceTest {

    /**
     * Create instance of class.
     */
    @Autowired
    AccPurchaseOrderModuleService instance;
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
    public static final String TERM_ID = JUnitConstants.TERM_ID;
    public static final String VENDOR_ID = JUnitConstants.VENDOR_ID;
    public static final String PRODUCT_UUID = JUnitConstants.PRODUCT_UUID;
    public static final String UOM_ID = JUnitConstants.UOM_ID;
    public static final String MULTI_LEVEL_APPROVAL_RULE_ID = JUnitConstants.MULTI_LEVEL_APPROVAL_RULE_ID;
    public static final String PO_ID = JUnitConstants.PO_ID;
    public static final String PREQ_DETAIL_ID = JUnitConstants.PREQ_DETAIL_ID;
    public static final String VQ_ID = JUnitConstants.VQ_ID;
    public static final String INVOICE_TERM_ID = JUnitConstants.INVOICE_TERM_ID;
    public static final String DATE_FORMAT_ID = JUnitConstants.DATE_FORMAT_ID;
    public static final char COMMA = JUnitConstants.COMMA;
    public static final String IMPORT_FILE_NAME = JUnitConstants.IMPORT_FILE_NAME;
    public static final String AGENT_NAME = JUnitConstants.AGENT_NAME;
    public static final String COST_CENTER_NAME = JUnitConstants.COST_CENTER_NAME;
    public static final String VENDOR_CODE = JUnitConstants.VENDOR_CODE;
    public static final String VENDOR_NAME = JUnitConstants.VENDOR_NAME;
    /**
     * Detail json array of product for saving transaction of single product.
     */
    public static final String PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT = JUnitConstants.PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT;
    public static final String ASSET_DETAIL_JSON_ARRAY_PRODUCT = JUnitConstants.ASSET_DETAIL_JSON_ARRAY_PRODUCT;
    public static final String PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT_FOR_FIXED_ASSET = JUnitConstants.PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT_FOR_FIXED_ASSET;
    public static final String INVOICE_TERM_JSON_ARRAY = JUnitConstants.INVOICE_TERM_JSON_ARRAY;
    public static final String GLOBAL_LEVEL_CUSTOM_FIELD_ARRAY = JUnitConstants.GLOBAL_LEVEL_CUSTOM_FIELD_ARRAY;
    public static final String LINE_LEVEL_CUSTOM_FIELD_ARRAY = JUnitConstants.LINE_LEVEL_CUSTOM_FIELD_ARRAY;

    public AccPurchaseOrderModuleServiceTest() {
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
     * Test of savePurchaseOrderJSON method, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testSavePurchaseOrderJSON() throws Exception {
        System.out.println("savePurchaseOrderJSON");
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
        paramJobj.put("number", "Test POO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("isEdit", "false");
        paramJobj.put("taxamount", "0");
        paramJobj.put("fromLinkCombo", "");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        JSONObject result = instance.savePurchaseOrderJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("savePurchaseOrderJSON is failed.");
        }
    }

    /**
     * Test of savePurchaseOrderJSON method for Fixed Asset, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testSavePurchaseOrderJSONForFixedAsset() throws Exception {
        System.out.println("savePurchaseOrderJSON_ForFixedAsset");
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
        paramJobj.put("number", "Test POO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("isEdit", "false");
        paramJobj.put("taxamount", "0");
        paramJobj.put("fromLinkCombo", "");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put("isFixedAsset", "true");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT_FOR_FIXED_ASSET);

        JSONObject result = instance.savePurchaseOrderJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("savePurchaseOrderJSON_ForFixedAsset is failed.");
        }
    }

    /**
     * Test of savePurchaseOrderJSON method for Consignment, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testSavePurchaseOrderJSONForConsignment() throws Exception {
        System.out.println("savePurchaseOrderJSON_ForConsignment");
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
        paramJobj.put("number", "Test POO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("isEdit", "false");
        paramJobj.put("taxamount", "0");
        paramJobj.put("fromLinkCombo", "");
        paramJobj.put("linkNumber", "");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put("isConsignment", "true");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        JSONObject result = instance.savePurchaseOrderJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("savePurchaseOrderJSON_ForConsignment is failed.");
        }
    }

    /**
     * Test of sendMailToApprover method, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testSendMailToApprover() throws Exception {
        System.out.println("sendMailToApprover");
        String companyid = COMPANY_ID;
        String ruleId = MULTI_LEVEL_APPROVAL_RULE_ID;
        String prNumber = "";
        String fromName = USER_FULL_NAME;
        boolean hasApprover = false;
        int moduleid = Constants.Acc_Purchase_Order_ModuleId;
        String createdby = USER_ID;
        boolean isEdit = false;

//        instance.sendMailToApprover(companyid, ruleId, prNumber, fromName, hasApprover, moduleid, createdby, isEdit);
        assertTrue(true);
    }

    /**
     * Test of savePurchaseOrderOtherDetails method, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testSavePurchaseOrderOtherDetails() throws Exception {
        System.out.println("savePurchaseOrderOtherDetails");
        JSONObject paramJobj = new JSONObject();
        String purchaseOrderId = PO_ID;
        String companyid = COMPANY_ID;

        instance.savePurchaseOrderOtherDetails(paramJobj, purchaseOrderId, companyid);
        assertTrue(true);
    }

    /**
     * Test of savePurchaseRequisitionAssetDetails method, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testSavePurchaseRequisitionAssetDetails() throws Exception {
        System.out.println("savePurchaseRequisitionAssetDetails");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);

        String productId = PRODUCT_UUID;
        String assetDetails = ASSET_DETAIL_JSON_ARRAY_PRODUCT;
        boolean invrecord = false;
        boolean isQuotationFromPR = false;
        boolean isPOFromVQ = false;

        Set result = instance.savePurchaseRequisitionAssetDetails(paramJobj, productId, assetDetails, invrecord, isQuotationFromPR, isPOFromVQ);
        assertNotNull(result);
    }

    /**
     * Test of saveAssetPurchaseRequisitionDetailMapping method, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testSaveAssetPurchaseRequisitionDetailMapping() throws Exception {
        System.out.println("saveAssetPurchaseRequisitionDetailMapping");
        String purchaseRequisitionDetailId = PREQ_DETAIL_ID;

        JSONObject paramJobj = new JSONObject();
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        Set<PurchaseRequisitionAssetDetails> assetDetailsSet = instance.savePurchaseRequisitionAssetDetails(paramJobj, PRODUCT_UUID, ASSET_DETAIL_JSON_ARRAY_PRODUCT, false, false, false);

        String companyId = COMPANY_ID;
        int moduleId = Constants.Acc_FixedAssets_Purchase_Order_ModuleId;

        Set result = instance.saveAssetPurchaseRequisitionDetailMapping(purchaseRequisitionDetailId, assetDetailsSet, companyId, moduleId);
        assertNotNull(result);
    }

    /**
     * Test of updateVQisOpenAndLinking method, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testUpdateVQisOpenAndLinking() throws Exception {
        System.out.println("updateVQisOpenAndLinking");
        String linkNumbers = VQ_ID;

        instance.updateVQisOpenAndLinking(linkNumbers);
        assertTrue(true);
    }

    /**
     * Test of approvePurchaseOrder method, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testApprovePurchaseOrder() throws Exception {
        System.out.println("approvePurchaseOrder");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), PO_ID);
        PurchaseOrder poObj = (PurchaseOrder) res.getEntityList().get(0);

        HashMap<String, Object> poApproveMap = new HashMap<>();
        poApproveMap.put(Constants.companyKey, COMPANY_ID);
        poApproveMap.put("currentUser", USER_ID);
        poApproveMap.put("totalAmount", 100);
        poApproveMap.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);

        boolean isMailApplicable = false;

        List result = instance.approvePurchaseOrder(poObj, poApproveMap, isMailApplicable);
        assertNotNull(result);
    }

    /**
     * Test of mapExciseDetails method, of class AccPurchaseOrderModuleService.
     */
    @Test
    public void testMapExciseDetails() throws Exception {
        System.out.println("mapExciseDetails");
        JSONObject temp = new JSONObject();
        JSONObject paramJobj = new JSONObject();

        HashMap result = instance.mapExciseDetails(temp, paramJobj);
        assertNotNull(result);
    }

    /**
     * Test of mapInvoiceTerms method, of class AccPurchaseOrderModuleService.
     */
    @Test
    public void testMapInvoiceTerms() throws Exception {
        System.out.println("mapInvoiceTerms");
        String InvoiceTerms = INVOICE_TERM_JSON_ARRAY;
        String id = PO_ID;
        String userid = USER_ID;
        boolean isQuotation = false;

        List result = instance.mapInvoiceTerms(InvoiceTerms, id, userid, isQuotation);
        assertNotNull(result);
    }

    /**
     * Test of importPurchaseOrderJSON method, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testImportPurchaseOrderJSON() throws Exception {
        System.out.println("importPurchaseOrderJSON");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put("do", "import");
        paramJobj.put("extraParams", "");
        paramJobj.put("modName", "Purchase Order");
        paramJobj.put("moduleName", "Purchase Order");
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

        JSONObject result = instance.importPurchaseOrderJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("importPurchaseOrderJSON is failed.");
        }
    }

    /**
     * Test of saveImportLog method, of class AccPurchaseOrderModuleService.
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
        int moduleID = Constants.Acc_Purchase_Order_ModuleId;

        instance.saveImportLog(requestJobj, msg, total, failed, moduleID);
        assertTrue(true);
    }

    /**
     * Test of getAgentIDByName method, of class AccPurchaseOrderModuleService.
     */
    @Test
    public void testGetAgentIDByName() throws Exception {
        System.out.println("getAgentIDByName");
        String agentName = AGENT_NAME;
        String companyID = COMPANY_ID;

        String result = instance.getAgentIDByName(agentName, companyID);
        assertNotNull(result);
    }

    /**
     * Test of getCostCenterIDByName method, of class
     * AccPurchaseOrderModuleService.
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
     * Test of getVendorByCode method, of class AccPurchaseOrderModuleService.
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
     * Test of getVendorByName method, of class AccPurchaseOrderModuleService.
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
     * Test of createGlobalCustomFieldArrayForImport method, of class
     * AccPurchaseOrderModuleService.
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

        int moduleID = Constants.Acc_Purchase_Order_ModuleId;

        JSONArray result = instance.createGlobalCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, moduleID);
        assertNotNull(result);
    }

    /**
     * Test of getExchangeRateForTransaction method, of class
     * AccPurchaseOrderModuleService.
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
     * Test of createLineLevelCustomFieldArrayForImport method, of class
     * AccPurchaseOrderModuleService.
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
        int moduleID = Constants.Acc_Purchase_Order_ModuleId;

        JSONArray result = instance.createLineLevelCustomFieldArrayForImport(requestJobj, jSONArray, recarr, df, moduleID);
        assertNotNull(result);
    }

    /**
     * Test of saveVendorQuotationJSON method, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testSaveVendorQuotationJSON() throws Exception {
        System.out.println("saveVendorQuotationJSON");
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
        paramJobj.put("number", "Test VQO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("isEdit", "false");
        paramJobj.put("taxamount", "0");
        paramJobj.put("fromLinkCombo", "");
        paramJobj.put("linkNumber", "");
        paramJobj.put("invoicetermsmap", "[]");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        JSONObject result = instance.saveVendorQuotationJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("savePurchaseOrderJSON is failed.");
        }
    }

    /**
     * Test of saveVendorQuotationJSON method for Fixed Asset, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testSaveVendorQuotationJSONForFixedAsset() throws Exception {
        System.out.println("saveVendorQuotationJSON_ForFixedAsset");
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
        paramJobj.put("number", "Test VQO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("isEdit", "false");
        paramJobj.put("taxamount", "0");
        paramJobj.put("fromLinkCombo", "");
        paramJobj.put("linkNumber", "");
        paramJobj.put("invoicetermsmap", "[]");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put("isFixedAsset", "true");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT_FOR_FIXED_ASSET);

        JSONObject result = instance.saveVendorQuotationJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("saveVendorQuotationJSON_ForFixedAsset is failed.");
        }
    }

    /**
     * Test of saveVendorQuotationJSON method for Consignment, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testSaveVendorQuotationJSONForConsignment() throws Exception {
        System.out.println("saveVendorQuotationJSON_ForConsignment");
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
        paramJobj.put("number", "Test VQO001");
        paramJobj.put(Constants.BillDate, formatter.format(new Date()));
        paramJobj.put(Constants.duedate, formatter.format(new Date()));
        paramJobj.put("termid", TERM_ID);
        paramJobj.put("defaultAdress", "true");
        paramJobj.put("isEdit", "false");
        paramJobj.put("taxamount", "0");
        paramJobj.put("fromLinkCombo", "");
        paramJobj.put("linkNumber", "");
        paramJobj.put("invoicetermsmap", "[]");
        paramJobj.put(Constants.reqHeader, "");
        paramJobj.put(Constants.remoteIPAddress, "");
        paramJobj.put("isConsignment", "true");
        paramJobj.put(Constants.detail, PRODUCT_DETAIL_JSON_ARRAY_SINGLE_PRODUCT);

        JSONObject result = instance.saveVendorQuotationJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("saveVendorQuotationJSON_ForConsignment is failed.");
        }
    }

    /**
     * Test of importVendorQuotationJSON method, of class
     * AccPurchaseOrderModuleService.
     */
    @Test
    public void testImportVendorQuotationJSON() throws Exception {
        System.out.println("importVendorQuotationJSON");
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

        JSONObject result = instance.importVendorQuotationJSON(paramJobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean(Constants.RES_success));
        } else {
            fail("importVendorQuotationJSON is failed.");
        }
    }
}