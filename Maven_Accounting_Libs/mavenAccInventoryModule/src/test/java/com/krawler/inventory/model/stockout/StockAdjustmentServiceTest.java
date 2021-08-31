/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 */
package com.krawler.inventory.model.stockout;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
public class StockAdjustmentServiceTest {

    /**
     * Create instance of class.
     */
    @Autowired
    StockAdjustmentService instance;
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
    public static final String STOCK_ADJUSTMENT_ID = JUnitConstants.STOCK_ADJUSTMENT_ID;
    public static final String PRODUCT_UUID = JUnitConstants.PRODUCT_UUID;
    public static final String STORE_UUID = JUnitConstants.STORE_UUID;
    public static final String LOCATION_UUID = JUnitConstants.LOCATION_UUID;
    public static final String STOCK_ADJUSTMENT_DRAFT_ID = JUnitConstants.STOCK_ADJUSTMENT_DRAFT_ID;

    public StockAdjustmentServiceTest() {
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
     * Test of requestStockAdjustment method, of class StockAdjustmentService.
     */
    @Test
    public void testRequestStockAdjustment_User_StockAdjustment() throws Exception {
        System.out.println("requestStockAdjustment");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        User user = (User) res.getEntityList().get(0);

        KwlReturnObject stockAdjustmentRes = accountingHandlerDAOobj.getObject(StockAdjustment.class.getName(), STOCK_ADJUSTMENT_ID);
        StockAdjustment stockAdjustment = (StockAdjustment) stockAdjustmentRes.getEntityList().get(0);

        instance.requestStockAdjustment(user, stockAdjustment);
        assertTrue(true);
    }

    /**
     * Test of requestStockAdjustment method, of class StockAdjustmentService.
     */
    @Test
    public void testRequestStockAdjustment_6args() throws Exception {
        System.out.println("requestStockAdjustment");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        User user = (User) res.getEntityList().get(0);

        KwlReturnObject stockAdjustmentRes = accountingHandlerDAOobj.getObject(StockAdjustment.class.getName(), STOCK_ADJUSTMENT_ID);
        StockAdjustment stockAdjustment = (StockAdjustment) stockAdjustmentRes.getEntityList().get(0);

        boolean allowNegativeInventory = false;
        boolean sendForApproval = false;
        String customfield = "";
        HashMap<String, Object> requestparams = new HashMap<>();

        instance.requestStockAdjustment(user, stockAdjustment, allowNegativeInventory, sendForApproval, customfield, requestparams);
        assertTrue(true);
    }

    /**
     * Test of addStockAdjustmentWithStockMovement method, of class
     * StockAdjustmentService.
     */
    @Test
    public void testAddStockAdjustmentWithStockMovement() throws Exception {
        System.out.println("addStockAdjustmentWithStockMovement");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        User user = (User) res.getEntityList().get(0);

        KwlReturnObject stockAdjustmentRes = accountingHandlerDAOobj.getObject(StockAdjustment.class.getName(), STOCK_ADJUSTMENT_ID);
        StockAdjustment sa = (StockAdjustment) stockAdjustmentRes.getEntityList().get(0);

        boolean allowNegativeInventory = false;
        String smInRemark = "In Remark";
        String smOutRemark = "Out Remark";

        instance.addStockAdjustmentWithStockMovement(user, sa, allowNegativeInventory, smInRemark, smOutRemark);
        assertTrue(true);
    }

    /**
     * Test of approveStockAdjustment method, of class StockAdjustmentService.
     */
    @Test
    public void testApproveStockAdjustment_User_StockAdjustment() throws Exception {
        System.out.println("approveStockAdjustment");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        User user = (User) res.getEntityList().get(0);

        KwlReturnObject stockAdjustmentRes = accountingHandlerDAOobj.getObject(StockAdjustment.class.getName(), STOCK_ADJUSTMENT_ID);
        StockAdjustment stockAdjustment = (StockAdjustment) stockAdjustmentRes.getEntityList().get(0);

        instance.approveStockAdjustment(user, stockAdjustment);
        assertTrue(true);
    }

    /**
     * Test of approveStockAdjustment method, of class StockAdjustmentService.
     */
    @Test
    public void testApproveStockAdjustment_3args() throws Exception {
        System.out.println("approveStockAdjustment");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        User user = (User) res.getEntityList().get(0);

        KwlReturnObject stockAdjustmentRes = accountingHandlerDAOobj.getObject(StockAdjustment.class.getName(), STOCK_ADJUSTMENT_ID);
        StockAdjustment stockAdjustment = (StockAdjustment) stockAdjustmentRes.getEntityList().get(0);

        boolean allowNegativeInventory = false;

        instance.approveStockAdjustment(user, stockAdjustment, allowNegativeInventory);
        assertTrue(true);
    }

    /**
     * Test of getStockAdjustmentById method, of class StockAdjustmentService.
     */
    @Test
    public void testGetStockAdjustmentById() throws Exception {
        System.out.println("getStockAdjustmentById");
        String id = STOCK_ADJUSTMENT_ID;

        StockAdjustment result = instance.getStockAdjustmentById(id);
        assertNotNull(result);
    }

    /**
     * Test of getStockAdjustmentBySequenceNo method, of class
     * StockAdjustmentService.
     */
    @Test
    public void testGetStockAdjustmentBySequenceNo() throws Exception {
        System.out.println("getStockAdjustmentBySequenceNo");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        Company company = (Company) res.getEntityList().get(0);

        String sequenceNo = "doc-11";

        List result = instance.getStockAdjustmentBySequenceNo(company, sequenceNo);
        assertNotNull(result);
    }

    /**
     * Test of getTotalAmountOFSABySequenceNo method, of class
     * StockAdjustmentService.
     */
    @Test
    public void testGetTotalAmountOFSABySequenceNo() throws Exception {
        System.out.println("getTotalAmountOFSABySequenceNo");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        Company company = (Company) res.getEntityList().get(0);

        String sequenceNo = "doc-11";

        double result = instance.getTotalAmountOFSABySequenceNo(company, sequenceNo);
        assertTrue(result >= 0);
    }

    /**
     * Test of getStockAdjustmentList method, of class StockAdjustmentService.
     */
    @Test
    public void testGetStockAdjustmentList() throws Exception {
        System.out.println("getStockAdjustmentList");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        Company company = (Company) res.getEntityList().get(0);

        KwlReturnObject productRes = accountingHandlerDAOobj.getObject(Product.class.getName(), PRODUCT_UUID);
        Product product = (Product) productRes.getEntityList().get(0);

        Set<Store> storeSet = null;
        Set<AdjustmentStatus> status = null;
        String adjustmentType = "";
        Date fromDate = null;
        Date toDate = null;
        String searchString = "";
        Paging paging = null;
        HashMap<String, Object> requestParams = new HashMap<>();

        List result = instance.getStockAdjustmentList(company, storeSet, product, status, adjustmentType, fromDate, toDate, searchString, paging, requestParams);
        assertNotNull(result);
    }

    /**
     * Test of getStockAdjustmentRows method, of class StockAdjustmentService.
     */
    @Test
    public void testGetStockAdjustmentRows() throws Exception {
        System.out.println("getStockAdjustmentRows");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        Company company = (Company) res.getEntityList().get(0);

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("company", company);
        requestParams.put("transactionno", "doc-11");

        List result = instance.getStockAdjustmentRows(requestParams);
        assertNotNull(result);
    }

    /**
     * Test of getStockAdjustmentSummary method, of class
     * StockAdjustmentService.
     */
    @Test
    public void testGetStockAdjustmentSummary() throws Exception {
        System.out.println("getStockAdjustmentSummary");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        Company company = (Company) res.getEntityList().get(0);

        KwlReturnObject productRes = accountingHandlerDAOobj.getObject(Product.class.getName(), PRODUCT_UUID);
        Product product = (Product) productRes.getEntityList().get(0);

        Set<Store> storeSet = new HashSet<>();
        AdjustmentStatus status = null;
        Date fromDate = null;
        Date toDate = null;
        String searchString = "";
        Paging paging = null;

        List result = instance.getStockAdjustmentSummary(company, storeSet, product, status, fromDate, toDate, searchString, paging);
        assertNotNull(result);
    }

    /**
     * Test of createStockAdjustmentDraft method, of class
     * StockAdjustmentService.
     */
    @Test
    public void testCreateStockAdjustmentDraft() throws Exception {
        System.out.println("createStockAdjustmentDraft");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        User user = (User) res.getEntityList().get(0);

        KwlReturnObject stockAdjustmentDraftRes = accountingHandlerDAOobj.getObject(StockAdjustmentDraft.class.getName(), STOCK_ADJUSTMENT_DRAFT_ID);
        StockAdjustmentDraft stockAdjustmentDraft = (StockAdjustmentDraft) stockAdjustmentDraftRes.getEntityList().get(0);

        instance.createStockAdjustmentDraft(user, stockAdjustmentDraft);
        assertTrue(true);
    }

    /**
     * Test of removeDraft method, of class StockAdjustmentService.
     */
    @Test
    public void testRemoveDraft() throws Exception {
        System.out.println("removeDraft");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        User user = (User) res.getEntityList().get(0);

        KwlReturnObject stockAdjustmentDraftRes = accountingHandlerDAOobj.getObject(StockAdjustmentDraft.class.getName(), STOCK_ADJUSTMENT_DRAFT_ID);
        StockAdjustmentDraft stockAdjustmentDraft = (StockAdjustmentDraft) stockAdjustmentDraftRes.getEntityList().get(0);

        instance.removeDraft(user, stockAdjustmentDraft);
        assertTrue(true);
    }

    /**
     * Test of getStockAdjustmentDraftById method, of class
     * StockAdjustmentService.
     */
    @Test
    public void testGetStockAdjustmentDraftById() throws Exception {
        System.out.println("getStockAdjustmentDraftById");
        String id = STOCK_ADJUSTMENT_DRAFT_ID;

        StockAdjustmentDraft result = instance.getStockAdjustmentDraftById(id);
        assertNotNull(result);
    }

    /**
     * Test of getStockAdjustmentDraftList method, of class
     * StockAdjustmentService.
     */
    @Test
    public void testGetStockAdjustmentDraftList() throws Exception {
        System.out.println("getStockAdjustmentDraftList");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        Company company = (Company) res.getEntityList().get(0);

        Paging paging = null;

        List result = instance.getStockAdjustmentDraftList(company, paging);
        assertNotNull(result);
    }

    /**
     * Test of saveSADetailInTemporaryTable method, of class
     * StockAdjustmentService.
     */
    @Test
    public void testSaveSADetailInTemporaryTable() throws Exception {
        System.out.println("saveSADetailInTemporaryTable");
        KwlReturnObject productRes = accountingHandlerDAOobj.getObject(Product.class.getName(), PRODUCT_UUID);
        Product product = (Product) productRes.getEntityList().get(0);

        KwlReturnObject storeRes = accountingHandlerDAOobj.getObject(Store.class.getName(), STORE_UUID);
        Store store = (Store) storeRes.getEntityList().get(0);

        KwlReturnObject locationRes = accountingHandlerDAOobj.getObject(Location.class.getName(), LOCATION_UUID);
        Location location = (Location) locationRes.getEntityList().get(0);

        String batchName = "";
        Map<String, Object> tempTablMap = new HashMap<>();

        instance.saveSADetailInTemporaryTable(product, store, location, batchName, tempTablMap);
        assertTrue(true);
    }

    /**
     * Test of deleteSA method, of class StockAdjustmentService.
     */
    @Test
    public void testDeleteSA() throws Exception {
        System.out.println("deleteSA");
        String saId = STOCK_ADJUSTMENT_ID;
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        Company company = (Company) res.getEntityList().get(0);

        KwlReturnObject userRes = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        User user = (User) userRes.getEntityList().get(0);

        JSONObject result = instance.deleteSA(saId, company, user,false);
        assertNotNull(result);
    }
}