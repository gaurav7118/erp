/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import java.util.HashMap;
import java.util.Map;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
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
public class AccScriptServiceImplTest {

    @Autowired
    private AccScriptService instance;
    @Autowired
    private HibernateTransactionManager txnManager;
    TransactionStatus status = null;

    public AccScriptServiceImplTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

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

    /**
     * Test of getCNDNForGainLossNotPosted method, of class AccScriptService.
     */
    @Test
    public void testGetCNDNForGainLossNotPosted() throws Exception {
        System.out.println("getCNDNForGainLossNotPosted");
        Map<String, Object> request = new HashMap<>();
        request.put("companyid", JUnitConstants.COMPANY_ID);
        request.put("filename", JUnitConstants.SCRIPT_FILE_PATH + "CNDNForGainLossNotPosted.csv");
        Map result = instance.getCNDNForGainLossNotPosted(request);
        if (result != null && result.containsKey(Constants.RES_success)) {
            assertEquals(true, result.get("success"));
        } else {
            fail("Test case failed : testGetCNDNForGainLossNotPosted");
        }
    }

    /**
     * Test of getTrnsactionsOtherThanControlAccountForVendor method, of class
     * AccScriptService.
     */
    @Test
    public void testGetTrnsactionsOtherThanControlAccountForVendor() throws Exception {
        System.out.println("getTrnsactionsOtherThanControlAccountForVendor");
        Map<String, Object> request = new HashMap<>();
        request.put("companyid", JUnitConstants.COMPANY_ID);
        request.put("filename", JUnitConstants.SCRIPT_FILE_PATH + "TransactionsOtherThanControlAccountForVendor.csv");
        Map result = instance.getTrnsactionsOtherThanControlAccountForVendor(request);
        if (result != null && result.containsKey(Constants.RES_success)) {
            assertEquals(true, result.get("success"));
        } else {
            fail("Test case failed : testGetTrnsactionsOtherThanControlAccountForVendor");
        }
    }

    /**
     * Test of getTrnsactionsOtherThanControlAccountForCustomer method, of class
     * AccScriptService.
     */
    @Test
    public void testGetTrnsactionsOtherThanControlAccountForCustomer() throws Exception {
        System.out.println("getTrnsactionsOtherThanControlAccountForCustomer");
        Map<String, Object> request = new HashMap<>();
        request.put("companyid", JUnitConstants.COMPANY_ID);
        request.put("filename", JUnitConstants.SCRIPT_FILE_PATH + "TransactionsOtherThanControlAccountForCustomer.csv");
        Map result = instance.getTrnsactionsOtherThanControlAccountForCustomer(request);
        if (result != null && result.containsKey(Constants.RES_success)) {
            assertEquals(true, result.get("success"));
        } else {
            fail("Test case failed : testGetTrnsactionsOtherThanControlAccountForCustomer");
        }
    }

    /**
     * Test of getPaymentReceiptForGainLossNotPosted method, of class
     * AccScriptService.
     */
    @Test
    public void testGetPaymentReceiptForGainLossNotPosted() throws Exception {
        System.out.println("getPaymentReceiptForGainLossNotPosted");
        Map<String, Object> request = new HashMap<>();
        request.put("companyid", JUnitConstants.COMPANY_ID);
        request.put("filename", JUnitConstants.SCRIPT_FILE_PATH + "PaymentReceiptForGainLossNotPosted.csv");
        Map result = instance.getPaymentReceiptForGainLossNotPosted(request);
        if (result != null && result.containsKey(Constants.RES_success)) {
            assertEquals(true, result.get("success"));
        } else {
            fail("Test case failed : testGetPaymentReceiptForGainLossNotPosted");
        }
    }

    /**
     * Test of getJournalEntryRecordForControlAccounts method, of class
     * AccScriptService.
     */
    @Test
    public void testGetJournalEntryRecordForControlAccounts() throws Exception {
        System.out.println("getJournalEntryRecordForControlAccounts");
        Map<String, Object> request = new HashMap<>();
        request.put("companyid", JUnitConstants.COMPANY_ID);
        request.put("filename", JUnitConstants.SCRIPT_FILE_PATH + "JournalEntryRecordForControlAccounts.csv");
        Map result = instance.getJournalEntryRecordForControlAccounts(request);
        if (result != null && result.containsKey(Constants.RES_success)) {
            assertEquals(true, result.get("success"));
        } else {
            fail("Test case failed : testGetJournalEntryRecordForControlAccounts");
        }
    }

    /**
     * Test of getInvoicesAmountDiffThanJEAmount method, of class
     * AccScriptService.
     */
    @Test
    public void testGetInvoicesAmountDiffThanJEAmount() throws Exception {
        System.out.println("getInvoicesAmountDiffThanJEAmount");
        Map<String, Object> request = new HashMap<>();
        request.put("companyid", JUnitConstants.COMPANY_ID);
        request.put("filename", JUnitConstants.SCRIPT_FILE_PATH + "InvoicesAmountDiffThanJEAmount.csv");
        Map result = instance.getInvoicesAmountDiffThanJEAmount(request);
        if (result != null && result.containsKey(Constants.RES_success)) {
            assertEquals(true, result.get("success"));
        } else {
            fail("Test case failed : testGetInvoicesAmountDiffThanJEAmount");
        }
    }

    /**
     * Test of getDifferentPaymentReceiptAndGainLossJEAccount method, of class
     * AccScriptService.
     */
    @Test
    public void testGetDifferentPaymentReceiptAndGainLossJEAccount() throws Exception {
        System.out.println("getDifferentPaymentReceiptAndGainLossJEAccount");
        Map<String, Object> request = new HashMap<>();
        request.put("companyid", JUnitConstants.COMPANY_ID);
        request.put("filename", JUnitConstants.SCRIPT_FILE_PATH + "DifferentPaymentReceiptAndGainLossJEAccount.csv");
        Map result = instance.getDifferentPaymentReceiptAndGainLossJEAccount(request);
        if (result != null && result.containsKey(Constants.RES_success)) {
            assertEquals(true, result.get("success"));
        } else {
            fail("Test case failed : testGetDifferentPaymentReceiptAndGainLossJEAccount");
        }
    }
}
