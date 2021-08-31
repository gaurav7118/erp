/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.vendor;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.hql.accounting.CreditNote;
import com.krawler.hql.accounting.DebitNote;
import com.krawler.hql.accounting.Payment;
import com.krawler.hql.accounting.Receipt;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import static org.junit.Assert.assertNotNull;
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
public class accVendorControllerCMNServiceTest {

    /**
     * Create instance of class.
     */
    @Autowired
    private accVendorControllerCMNService instance;
    /**
     * Used to get object of pojo.
     */
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAOobj;
    /**
     * Used to avoid problem of load proxy object.
     */
    @Autowired
    private HibernateTransactionManager txnManager;
    /**
     * Used to maintain status of transaction.
     */
    TransactionStatus status = null;

    public accVendorControllerCMNServiceTest() {
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
     * Test of getRecordsForStore method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetRecordsForStore() throws Exception {
        System.out.println("getRecordsForStore");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");

        String modules = String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId);
        JSONArray jarrRecords = new JSONArray();

        JSONArray result = instance.getRecordsForStore(requestParams, modules, jarrRecords);
        assertNotNull(result);
    }

    /**
     * Test of getColumnsForGrid method, of class accVendorControllerCMNService.
     */
    @Test
    public void testGetColumnsForGrid() throws Exception {
        System.out.println("getColumnsForGrid");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("locale", Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));
        requestParams.put("currencyname", JUnitConstants.GLOBAL_CURRENCY_NAME);
        requestParams.put("reportId", "5");

        String modules = String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
        JSONArray jarrColumns = new JSONArray();

        JSONArray result = instance.getColumnsForGrid(requestParams, modules, jarrColumns);
        assertNotNull(result);
    }

    /**
     * Test of getPurchaseInvoiceInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetPurchaseInvoiceInformation() throws Exception {
        System.out.println("getPurchaseInvoiceInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        Object[] objArr = new Object[]{JUnitConstants.GR_ID};
        List<Object[]> invoices = new ArrayList<>();
        invoices.add(objArr);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getPurchaseInvoiceInformation(requestParams, invoices, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getPurchaseOrdersInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetPurchaseOrdersInformation() throws Exception {
        System.out.println("getPurchaseOrdersInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        Object[] objArr = new Object[]{JUnitConstants.PO_ID};
        List<Object[]> orders = new ArrayList<>();
        orders.add(objArr);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getPurchaseOrdersInformation(requestParams, orders, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getPurchaseReturnInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetPurchaseReturnInformation() throws Exception {
        System.out.println("getPurchaseReturnInformation");
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        Object[] objArr = new Object[]{JUnitConstants.PR_ID};
        List<Object[]> returns = new ArrayList<>();
        returns.add(objArr);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getPurchaseReturnInformation(requestParams, returns, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getQuotationsInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetQuotationsInformation() throws Exception {
        System.out.println("getQuotationsInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        List<Object> vquotations = new ArrayList<>();
        vquotations.add(JUnitConstants.VQ_ID);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getQuotationsInformation(requestParams, vquotations, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getGoodsReceiptInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetGoodsReceiptInformation() throws Exception {
        System.out.println("getGoodsReceiptInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        Object[] objArr = new Object[]{JUnitConstants.GRO_ID};
        List<Object[]> greceipts = new ArrayList<>();
        greceipts.add(objArr);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getGoodsReceiptInformation(requestParams, greceipts, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getDebitNoteInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetDebitNoteInformation() throws Exception {
        System.out.println("getDebitNoteInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        Object[] objArr = new Object[]{"", JUnitConstants.DN_ID};
        List<Object[]> dnotes = new ArrayList<>();
        dnotes.add(objArr);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getDebitNoteInformation(requestParams, dnotes, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getOpeningDebitNoteInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetOpeningDebitNoteInformation() throws Exception {
        System.out.println("getOpeningDebitNoteInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        KwlReturnObject res = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), JUnitConstants.OPENING_DN_ID);
        DebitNote debitNote = (DebitNote) res.getEntityList().get(0);
        List dnotes = new ArrayList();
        dnotes.add(debitNote);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getOpeningDebitNoteInformation(requestParams, dnotes, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getCreditNoteInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetCreditNoteInformation() throws Exception {
        System.out.println("getCreditNoteInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        Object[] objArr = new Object[]{"", JUnitConstants.CN_ID};
        List<Object[]> cnotes = new ArrayList<>();
        cnotes.add(objArr);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getCreditNoteInformation(requestParams, cnotes, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getOpeningCreditNoteInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetOpeningCreditNoteInformation() throws Exception {
        System.out.println("getOpeningCreditNoteInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        KwlReturnObject res = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), JUnitConstants.OPENING_CN_ID);
        CreditNote creditNote = (CreditNote) res.getEntityList().get(0);
        List cnotes = new ArrayList();
        cnotes.add(creditNote);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getOpeningCreditNoteInformation(requestParams, cnotes, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getMadePaymentsInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetMadePaymentsInformation() throws Exception {
        System.out.println("getMadePaymentsInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        KwlReturnObject res = accountingHandlerDAOobj.getObject(Payment.class.getName(), JUnitConstants.PAYMENT_ID);
        Payment payment = (Payment) res.getEntityList().get(0);
        Object[] objArr = new Object[]{payment};
        List<Object[]> payments = new ArrayList<>();
        payments.add(objArr);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getMadePaymentsInformation(requestParams, payments, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getOpeningMadePaymentsInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetOpeningMadePaymentsInformation() throws Exception {
        System.out.println("getOpeningMadePaymentsInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        KwlReturnObject res = accountingHandlerDAOobj.getObject(Payment.class.getName(), JUnitConstants.OPENING_PAYMENT_ID);
        Payment payment = (Payment) res.getEntityList().get(0);
        List payments = new ArrayList();
        payments.add(payment);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getOpeningMadePaymentsInformation(requestParams, payments, DataJArr);
        assertNotNull(result);
    }

    /**
     * Test of getReceivedPaymentsInformation method, of class
     * accVendorControllerCMNService.
     */
    @Test
    public void testGetReceivedPaymentsInformation() throws Exception {
        System.out.println("getReceivedPaymentsInformation");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("reportId", "1");
        requestParams.put("status", "all");
        requestParams.put("isExport", "false");

        KwlReturnObject res = accountingHandlerDAOobj.getObject(Receipt.class.getName(), JUnitConstants.RECEIPT_ID);
        Receipt receipt = (Receipt) res.getEntityList().get(0);
        Object[] objArr = new Object[]{receipt};
        List<Object[]> receipts = new ArrayList<>();
        receipts.add(objArr);

        JSONArray DataJArr = new JSONArray();

        JSONArray result = instance.getReceivedPaymentsInformation(requestParams, receipts, DataJArr);
        assertNotNull(result);
    }
}