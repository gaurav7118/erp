/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 */
package com.krawler.spring.accounting.inventory;

import com.krawler.common.admin.LocationBatchDocumentMapping;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.util.JUnitConstants;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Product;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.List;
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
public class AccImportServiceTest {

    /**
     * Create instance of class.
     */
    @Autowired
    AccImportService instance;
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
    public static final String UOM_ID = JUnitConstants.UOM_ID;
    public static final String STORE_UUID = JUnitConstants.STORE_UUID;
    public static final String LOCATION_UUID = JUnitConstants.LOCATION_UUID;
    public static final String PRODUCT_UUID = JUnitConstants.PRODUCT_UUID;
    public static final String NEW_PRODUCT_BATCH_ID = JUnitConstants.NEW_PRODUCT_BATCH_ID;
    public static final String INVENTORY_ID = JUnitConstants.INVENTORY_ID;
    public static final String LOCATION_BATCH_DOCUMENT_MAPPING_ID = JUnitConstants.LOCATION_BATCH_DOCUMENT_MAPPING_ID;

    public AccImportServiceTest() {
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
     * Test of isProductUsedInTransaction method, of class AccImportService.
     */
    @Test
    public void testIsProductUsedInTransaction() throws Exception {
        System.out.println("isProductUsedInTransaction");
        String companyId = COMPANY_ID;
        String productId = PRODUCT_UUID;

        instance.isProductUsedInTransaction(companyId, productId);
        assertTrue(true);
    }

    /**
     * Test of saveOrUpdate method, of class AccImportService.
     */
    @Test
    public void testSaveOrUpdate() throws Exception {
        System.out.println("saveOrUpdate");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Product.class.getName(), PRODUCT_UUID);
        Product object = (Product) res.getEntityList().get(0);

        instance.saveOrUpdateObj(object);
        assertTrue(true);
    }

    /**
     * Test of getNewProductBatchById method, of class AccImportService.
     */
    @Test
    public void testGetNewProductBatchById() throws Exception {
        System.out.println("getNewProductBatchById");
        String id = NEW_PRODUCT_BATCH_ID;

        NewProductBatch result = instance.getNewProductBatchById(id);
        assertNotNull(result);
    }

    /**
     * Test of deleteLocationBatchDocumentMappingByBatchMapId method, of class
     * AccImportService.
     */
    @Test
    public void testDeleteLocationBatchDocumentMappingByBatchMapId() throws Exception {
        System.out.println("deleteLocationBatchDocumentMappingByBatchMapId");
        String batchMapId = LOCATION_BATCH_DOCUMENT_MAPPING_ID;

        instance.deleteLocationBatchDocumentMappingByBatchMapId(batchMapId);
        assertTrue(true);
    }

    /**
     * Test of addStockInventorySide method, of class AccImportService.
     */
    @Test
    public void testAddStockInventorySide() throws Exception {
        System.out.println("addStockInventorySide");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Inventory.class.getName(), INVENTORY_ID);
        Inventory inventory = (Inventory) res.getEntityList().get(0);

        List list = new ArrayList();
        list.add(inventory);

        KwlReturnObject newInvObj = new KwlReturnObject(true, "", "", list, 1);

        JSONObject inventoryjson = new JSONObject();
        inventoryjson.put("productid", PRODUCT_UUID);
        inventoryjson.put("companyid", COMPANY_ID);
        inventoryjson.put("uomid", UOM_ID);
        inventoryjson.put("baseuomquantity", 1);

        String productDefaultWarehouseID = STORE_UUID;
        String productDefaultLocationID = LOCATION_UUID;
        double prodInitPurchasePrice = 100.0;

        KwlReturnObject result = instance.addStockInventorySide(newInvObj, inventoryjson, productDefaultWarehouseID, productDefaultLocationID, prodInitPurchasePrice);
        assertNotNull(result);
    }

    /**
     * Test of updateStockInventorySide method, of class AccImportService.
     */
    @Test
    public void testUpdateStockInventorySide() throws Exception {
        System.out.println("updateStockInventorySide");
        KwlReturnObject inventoryRes = accountingHandlerDAOobj.getObject(Inventory.class.getName(), INVENTORY_ID);
        Inventory inventory = (Inventory) inventoryRes.getEntityList().get(0);

        List list = new ArrayList();
        list.add(inventory);

        KwlReturnObject updatedInvObj = new KwlReturnObject(true, "", "", list, 1);
        JSONObject inventoryjson = new JSONObject();

        KwlReturnObject res = accountingHandlerDAOobj.getObject(LocationBatchDocumentMapping.class.getName(), LOCATION_BATCH_DOCUMENT_MAPPING_ID);
        LocationBatchDocumentMapping lbm = (LocationBatchDocumentMapping) res.getEntityList().get(0);

        double prodInitPurchasePrice = 100.0;

        KwlReturnObject result = instance.updateStockInventorySide(updatedInvObj, inventoryjson, lbm, prodInitPurchasePrice);
        assertNotNull(result);
    }

    /**
     * Test of getProductOpeningQtyBatchDetail method, of class
     * AccImportService.
     */
    @Test
    public void testGetProductOpeningQtyBatchDetail() throws Exception {
        System.out.println("getProductOpeningQtyBatchDetail");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Product.class.getName(), PRODUCT_UUID);
        Product product = (Product) res.getEntityList().get(0);

        KwlReturnObject result = instance.getProductOpeningQtyBatchDetail(product);
        assertNotNull(result);
    }

    /**
     * Test of deleteStockAndSMForProduct method, of class AccImportService.
     */
    @Test
    public void testDeleteStockAndSMForProduct() throws Exception {
        System.out.println("deleteStockAndSMForProduct");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Product.class.getName(), PRODUCT_UUID);
        Product product = (Product) res.getEntityList().get(0);

        instance.deleteStockAndSMForProduct(product);
        assertTrue(true);
    }
}