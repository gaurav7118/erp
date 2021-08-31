/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.inventory.model.packaging;

import com.krawler.common.util.JUnitConstants;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
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
public class PackagingServiceTest {

    /**
     * Create instance of class.
     */
    @Autowired
    private PackagingService instance;
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
    /**
     * Constants used in class.
     */
    public static final String COMPANYID = JUnitConstants.COMPANY_ID;
    public static final String PACKAGING_ID = JUnitConstants.PACKAGING_ID;
    public static final String UOM_ID = JUnitConstants.UOM_ID;

    public PackagingServiceTest() {
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
     * Test of addOrUpdatePackaging method, of class PackagingService.
     */
    @Test
    public void testAddOrUpdatePackaging() throws Exception {
        System.out.println("addOrUpdatePackaging");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Packaging.class.getName(), PACKAGING_ID);
        Packaging packaging = (Packaging) res.getEntityList().get(0);

        instance.addOrUpdatePackaging(packaging);
        if (packaging != null) {
            assertNotNull(packaging);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getPackaging method, of class PackagingService.
     */
    @Test
    public void testGetPackaging() throws Exception {
        System.out.println("getPackaging");

        Packaging result = instance.getPackaging(PACKAGING_ID);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of createClonePackaging method, of class PackagingService.
     */
    @Test
    public void testCreateClonePackaging() throws Exception {
        System.out.println("createClonePackaging");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(Packaging.class.getName(), PACKAGING_ID);
        Packaging packaging = (Packaging) res.getEntityList().get(0);

        Packaging result = instance.createClonePackaging(packaging);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of createPackagingByStockUom method, of class PackagingService.
     */
    @Test
    public void testCreatePackagingByStockUom() throws Exception {
        System.out.println("createPackagingByStockUom");
        KwlReturnObject res = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), UOM_ID);
        UnitOfMeasure stockUom = (UnitOfMeasure) res.getEntityList().get(0);

        Packaging result = instance.createPackagingByStockUom(stockUom);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getProductBaseUOMRate method, of class PackagingService.
     */
    @Test
    public void testGetProductBaseUOMRate() throws Exception {
        System.out.println("getProductBaseUOMRate");
        HashMap<String, Object> request = new HashMap<>();
        request.put("uomschematypeid", "");
        request.put("currentuomid", "");
        request.put("carryin", true);
        request.put("companyid", COMPANYID);

        KwlReturnObject result = instance.getProductBaseUOMRate(request);
        if (result != null) {
            assertNotNull(result);
        } else {
            fail("The test case is a prototype.");
        }
    }
}
