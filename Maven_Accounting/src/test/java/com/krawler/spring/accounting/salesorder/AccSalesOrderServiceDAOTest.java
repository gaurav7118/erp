/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.salesorder;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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
public class AccSalesOrderServiceDAOTest {

    /**
     * Create instance of class.
     */
    @Autowired
    AccSalesOrderServiceDAO instance;
    /**
     * Used to get object of pojo.
     */
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAOobj;
    /**
     * Used to get params related to company.
     */
    @Autowired
    private WSUtilService wsUtilService;
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
    public static final String SUBDOMAIN = JUnitConstants.SUBDOMAIN;
    public static final String PO_ID = JUnitConstants.PO_ID;
    public static final String VQ_ID = JUnitConstants.VQ_ID;
    public static final String SO_ID = JUnitConstants.SO_ID;
    public static final String SO_DETAIL_ID = JUnitConstants.SO_DETAIL_ID;
    public static final String CQ_ID = JUnitConstants.CQ_ID;
    public static final String PRODUCT_UUID = JUnitConstants.PRODUCT_UUID;
    public static final String UOM_ID = JUnitConstants.UOM_ID;
    public static final String DOCUMENT_ID = JUnitConstants.DOCUMENT_ID;

    public AccSalesOrderServiceDAOTest() {
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
     * Test of getQuotationsJson method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetQuotationsJson() throws Exception {
        System.out.println("getQuotationsJson");
        JSONObject params = new JSONObject();
        params.put(Constants.RES_CDOMAIN, SUBDOMAIN);
        JSONObject paramJobj = wsUtilService.populateAdditionalInformation(params);

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.globalCurrencyKey, ""));
        requestParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey, ""));

        List list = new ArrayList();
        JSONArray jArr = new JSONArray();

        JSONArray result = instance.getQuotationsJson(requestParams, list, jArr);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getTermDetails method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetTermDetails() throws Exception {
        System.out.println("getTermDetails");
        String id = SO_ID;
        boolean isOrder = false;

        JSONArray result = instance.getTermDetails(id, isOrder);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getQuotationDetailStatusSO method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetQuotationDetailStatusSO() throws Exception {
        System.out.println("getQuotationDetailStatusSO");
        QuotationDetail quod = new QuotationDetail();

        double result = instance.getQuotationDetailStatusSO(quod);
        if (result < 0) {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getQuotationDetailStatusINV method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetQuotationDetailStatusINV() throws Exception {
        System.out.println("getQuotationDetailStatusINV");
        QuotationDetail quod = new QuotationDetail();

        double result = instance.getQuotationDetailStatusINV(quod);
        if (result < 0) {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getQuotationRows method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetQuotationRows() throws Exception {
        System.out.println("getQuotationRows");
        JSONObject params = new JSONObject();
        params.put(Constants.RES_CDOMAIN, SUBDOMAIN);
        JSONObject paramJobj = wsUtilService.populateAdditionalInformation(params);

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.globalCurrencyKey, ""));
        requestParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey, ""));
        JSONObject jobj = new JSONObject();
        requestParams.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(jobj));
        requestParams.put("dataFormatValue", authHandler.getDateOnlyFormat());
        requestParams.put("isLeaseFixedAsset", false);
        requestParams.put("bills", CQ_ID);
        requestParams.put("dtype", "");
        requestParams.put("sopolinkflag", "false");
        requestParams.put("isOrder", false);

        JSONObject result = instance.getQuotationRows(requestParams);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getTimeIntervalForProduct method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetTimeIntervalForProduct() throws Exception {
        System.out.println("getTimeIntervalForProduct");
        String inouttime = "2016-01-01 00:00,2016-09-12 00:00";

        String result = instance.getTimeIntervalForProduct(inouttime);
        if (StringUtil.isNullOrEmpty(result)) {
            fail("The test case is a prototype.");
        }
    }
//
//    /**
//     * Test of getSalesOrdersMap method, of class AccSalesOrderServiceDAO.
//     */
//    @Test
//    public void testGetSalesOrdersMap() throws Exception {
//        System.out.println("getSalesOrdersMap");
//        HttpServletRequest request = null;
//        HashMap expResult = null;
//        HashMap result = instance.getSalesOrdersMap(request);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getPOCount method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetPOCount() throws Exception {
        System.out.println("getPOCount");
        JSONObject params = new JSONObject();
        params.put(Constants.RES_CDOMAIN, SUBDOMAIN);
        JSONObject paramJobj = wsUtilService.populateAdditionalInformation(params);

        HashMap<String, Object> orderParams = new HashMap<>();
        orderParams.put("currentuomid", UOM_ID);
        orderParams.put("productId", PRODUCT_UUID);
        orderParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey, ""));

        double result = instance.getPOCount(orderParams);
        if (result < 0) {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getSOCount method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSOCount() throws Exception {
        System.out.println("getSOCount");
        JSONObject params = new JSONObject();
        params.put(Constants.RES_CDOMAIN, SUBDOMAIN);
        JSONObject paramJobj = wsUtilService.populateAdditionalInformation(params);

        HashMap<String, Object> orderParams = new HashMap<>();
        orderParams.put("currentuomid", UOM_ID);
        orderParams.put("productId", PRODUCT_UUID);
        orderParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey, ""));

        double result = instance.getSOCount(orderParams);
        if (result < 0) {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getSalesOrdersJsonMerged method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSalesOrdersJsonMerged() throws Exception {
        System.out.println("getSalesOrdersJsonMerged");
        JSONObject params = new JSONObject();
        params.put(Constants.RES_CDOMAIN, SUBDOMAIN);
        JSONObject jobj = wsUtilService.populateAdditionalInformation(params);
        JSONObject paramJobj = new JSONObject();
        jobj.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(paramJobj));

        List<Object[]> list = new ArrayList<>();
        JSONArray jArr = new JSONArray();

        JSONArray result = instance.getSalesOrdersJsonMerged(jobj, list, jArr);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getSalesOrderStatus method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSalesOrderStatus() throws Exception {

        System.out.println("getSalesOrderStatus");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), SO_ID);
        SalesOrder so = (SalesOrder) res.getEntityList().get(0);

        String result = instance.getSalesOrderStatus(so);
        if (!StringUtil.isNullOrEmpty(result) && (result.equalsIgnoreCase("Open") || result.equalsIgnoreCase("Closed"))) {
            assertEquals(true, true);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getSalesOrderStatusNew method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSalesOrderStatusNew() throws Exception {
        System.out.println("getSalesOrderStatusNew");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), SO_ID);
        SalesOrder so = (SalesOrder) res.getEntityList().get(0);
        Set<SalesOrderDetail> orderset = so.getRows();
        KwlReturnObject prefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), so.getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) prefResult.getEntityList().get(0);
        String companyid = so.getCompany().getCompanyID();

        String result = instance.getSalesOrderStatusNew(so, orderset, pref, companyid);
        if (!StringUtil.isNullOrEmpty(result) && (result.equalsIgnoreCase("Open") || result.equalsIgnoreCase("Closed"))) {
            assertEquals(true, true);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getSalesOrderRows method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSalesOrderRows() throws Exception {
        System.out.println("getSalesOrderRows");
        JSONObject params = new JSONObject();
        params.put(Constants.RES_CDOMAIN, SUBDOMAIN);
        JSONObject paramJobj = wsUtilService.populateAdditionalInformation(params);
        paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
        paramJobj.put("bills", SO_ID);

        JSONObject result = instance.getSalesOrderRows(paramJobj);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getSalesOrderBalanceQuantity method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSalesOrderBalanceQuantity() throws Exception {
        System.out.println("getSalesOrderBalanceQuantity");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), SO_DETAIL_ID);
        SalesOrderDetail salesOrderDetail = (SalesOrderDetail) res.getEntityList().get(0);

        double result = instance.getSalesOrderBalanceQuantity(salesOrderDetail);
        if (result < 0) {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getNewBatchJson method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetNewBatchJson() throws Exception {
        System.out.println("getNewBatchJson");
        JSONObject params = new JSONObject();
        params.put(Constants.RES_CDOMAIN, SUBDOMAIN);
        JSONObject paramJobj = wsUtilService.populateAdditionalInformation(params);

        KwlReturnObject res = accountingHandlerDAOobj.getObject(Product.class.getName(), PRODUCT_UUID);
        Product product = (Product) res.getEntityList().get(0);
        String documentid = DOCUMENT_ID;

        String result = instance.getNewBatchJson(product, paramJobj, documentid);
        if (!StringUtil.isNullOrEmpty(result)) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getSalesOrderDetailStatusForDO method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSalesOrderDetailStatusForDO() throws Exception {
        System.out.println("getSalesOrderDetailStatusForDO");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), SO_DETAIL_ID);
        SalesOrderDetail sod = (SalesOrderDetail) res.getEntityList().get(0);

        double result = instance.getSalesOrderDetailStatusForDO(sod);
        if (result < 0) {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getSalesOrderDetailStatus method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSalesOrderDetailStatus() throws Exception {
        System.out.println("getSalesOrderDetailStatus");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), SO_DETAIL_ID);
        SalesOrderDetail sod = (SalesOrderDetail) res.getEntityList().get(0);

        double result = instance.getSalesOrderDetailStatus(sod);
        if (result < 0) {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getSalesOrderJsonForLinking method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSalesOrderJsonForLinking() throws Exception {
        System.out.println("getSalesOrderJsonForLinking");
        JSONArray jsonArray = new JSONArray();
        List salesorders = new ArrayList();
        KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), SO_ID);
        SalesOrder so = (SalesOrder) res.getEntityList().get(0);
        salesorders.add(new Object[]{so, 6});
        KWLCurrency currency = so.getCurrency();
        DateFormat df = authHandler.getDateOnlyFormat();

        JSONArray result = instance.getSalesOrderJsonForLinking(jsonArray, salesorders, currency, df);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getPurchaseOrderJsonForLinking method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetPurchaseOrderJsonForLinking() throws Exception {
        System.out.println("getPurchaseOrderJsonForLinking");
        JSONArray jsonArray = new JSONArray();
        List purchaseorder = new ArrayList();
        KwlReturnObject res = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), PO_ID);
        PurchaseOrder po = (PurchaseOrder) res.getEntityList().get(0);
        purchaseorder.add(po);
        KWLCurrency currency = po.getCurrency();
        DateFormat df = authHandler.getDateOnlyFormat();

        JSONArray result = instance.getPurchaseOrderJsonForLinking(jsonArray, purchaseorder, currency, df);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getVendorQuotationJsonForLinking method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetVendorQuotationJsonForLinking() throws Exception {
        System.out.println("getVendorQuotationJsonForLinking");
        JSONArray jsonArray = new JSONArray();
        List vendorquotation = new ArrayList();
        KwlReturnObject res = accountingHandlerDAOobj.getObject(VendorQuotation.class.getName(), VQ_ID);
        VendorQuotation vq = (VendorQuotation) res.getEntityList().get(0);
        vendorquotation.add(vq.getID());
        KWLCurrency currency = vq.getCurrency();
        DateFormat df = authHandler.getDateOnlyFormat();
        String companyid = vq.getCompany().getCompanyID();

        JSONArray result = instance.getVendorQuotationJsonForLinking(jsonArray, vendorquotation, currency, df, companyid);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getCustomerQuotationJsonForLinking method, of class
     * AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetCustomerQuotationJsonForLinking() throws Exception {
        System.out.println("getCustomerQuotationJsonForLinking");
        JSONArray jsonArray = new JSONArray();
        List listcq = new ArrayList();
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Quotation.class.getName(), CQ_ID);
        Quotation cq = (Quotation) res.getEntityList().get(0);
        listcq.add(cq.getID());
        KWLCurrency currency = cq.getCurrency();
        JSONObject paramJobj = new JSONObject();
        DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
        DateFormat df = authHandler.getDateOnlyFormat();
        int linkType = 6;

        JSONArray result = instance.getCustomerQuotationJsonForLinking(jsonArray, listcq, currency, userdf, df, linkType);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

//    /**
//     * Test of getSODetailsItemJSON method, of class AccSalesOrderServiceDAO.
//     */
//    @Test
//    public void testGetSODetailsItemJSON() throws Exception {
//        System.out.println("getSODetailsItemJSON");
//        HttpServletRequest request = null;
//        String companyid = "";
//        String SOID = "";
//        HashMap<String, Integer> FieldMap = null;
//        HashMap<String, String> replaceFieldMap = null;
//        HashMap<String, Integer> DimensionFieldMap = null;
//        HashMap<String, Integer> LineLevelCustomFieldMap = null;
//        HashMap<String, Integer> ProductLevelCustomFieldMap = null;
//        JSONArray expResult = null;
//        JSONArray result = instance.getSODetailsItemJSON(request, companyid, SOID, FieldMap, replaceFieldMap, DimensionFieldMap, LineLevelCustomFieldMap, ProductLevelCustomFieldMap);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    /**
     * Test of getSOStatus method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSOStatus() throws Exception {
        System.out.println("getSOStatus");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), SO_ID);
        SalesOrder so = (SalesOrder) res.getEntityList().get(0);
        KwlReturnObject prefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), so.getCompany().getCompanyID());
        CompanyAccountPreferences pref = (CompanyAccountPreferences) prefResult.getEntityList().get(0);
        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), so.getCompany().getCompanyID());
        ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
        String result = instance.getSOStatus(so, pref,extraCompanyPreferences);
        if (!StringUtil.isNullOrEmpty(result) && (result.equalsIgnoreCase("Open") || result.equalsIgnoreCase("Closed"))) {
            assertEquals(true, true);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getSalesOrdersMapJson method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testGetSalesOrdersMapJson() throws Exception {
        System.out.println("getSalesOrdersMapJson");
        JSONObject params = new JSONObject();
        params.put(Constants.RES_CDOMAIN, SUBDOMAIN);
        JSONObject paramJobj = wsUtilService.populateAdditionalInformation(params);
        paramJobj.put(Constants.df, authHandler.getDateOnlyFormat());
        paramJobj.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(paramJobj));

        HashMap result = instance.getSalesOrdersMapJson(paramJobj);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of approveSalesOrder method, of class AccSalesOrderServiceDAO.
     */
    @Test
    public void testApproveSalesOrder() throws Exception {
        System.out.println("approveSalesOrder");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), SO_ID);
        SalesOrder soObj = (SalesOrder) res.getEntityList().get(0);
        HashMap<String, Object> soApproveMap = new HashMap<>();
        boolean isMailApplicable = false;

        List result = instance.approveSalesOrder(soObj, soApproveMap, isMailApplicable);
        if (result != null && !result.isEmpty()) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }
//    /**
//     * Test of getDailySalesReportByCustomer method, of class AccSalesOrderServiceDAO.
//     */
//    @Test
//    public void testGetDailySalesReportByCustomer() throws Exception {
//        System.out.println("getDailySalesReportByCustomer");
//        HttpServletRequest request = null;
//        Map<String, Object> requestParams = null;
//        JSONObject expResult = null;
//        JSONObject result = instance.getDailySalesReportByCustomer(request, requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getMonthlySalesOrdesByCustomer method, of class AccSalesOrderServiceDAO.
//     */
//    @Test
//    public void testGetMonthlySalesOrdesByCustomer() throws Exception {
//        System.out.println("getMonthlySalesOrdesByCustomer");
//        HttpServletRequest request = null;
//        Map<String, Object> requestParams = null;
//        JSONObject expResult = null;
//        JSONObject result = instance.getMonthlySalesOrdesByCustomer(request, requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getYearlySalesOrdersByCustomer method, of class AccSalesOrderServiceDAO.
//     */
//    @Test
//    public void testGetYearlySalesOrdersByCustomer() throws Exception {
//        System.out.println("getYearlySalesOrdersByCustomer");
//        HttpServletRequest request = null;
//        Map<String, Object> requestParams = null;
//        JSONObject expResult = null;
//        JSONObject result = instance.getYearlySalesOrdersByCustomer(request, requestParams);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}