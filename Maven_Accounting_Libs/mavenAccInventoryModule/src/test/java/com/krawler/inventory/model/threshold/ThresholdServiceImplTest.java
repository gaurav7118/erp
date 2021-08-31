/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.threshold;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.util.List;
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
public class ThresholdServiceImplTest {

    @Autowired
    private ThresholdService instance;
    @Autowired
    private HibernateTransactionManager txnManager;
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAO;
    
    TransactionStatus status = null;
    Product product = null;
    Company company = null;
    Store store = null;

    public ThresholdServiceImplTest() {
    }

    @Before
    public void setUp() throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ThresholdServiceImplTest_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);

        KwlReturnObject companyResult = accountingHandlerDAO.getObject(Company.class.getName(), JUnitConstants.COMPANY_ID);
        company = (Company) companyResult.getEntityList().get(0);

        KwlReturnObject productResult = accountingHandlerDAO.getObject(Product.class.getName(), JUnitConstants.PRODUCT_UUID);
        product = (Product) productResult.getEntityList().get(0);

        KwlReturnObject storeResult = accountingHandlerDAO.getObject(Store.class.getName(), JUnitConstants.STORE_UUID);
        store = (Store) storeResult.getEntityList().get(0);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of addOrUpdateProductThreshold method, of class ThresholdService.
     */
    @Test
    public void testAddOrUpdateProductThreshold_3args() throws Exception {
        System.out.println("addOrUpdateProductThreshold");
        double thresholdLimit = 1.0;
        instance.addOrUpdateProductThreshold(product, store, thresholdLimit);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of addOrUpdateProductThreshold method, of class ThresholdService.
     */
    @Test
    public void testAddOrUpdateProductThreshold_ProductThreshold() throws Exception {
        System.out.println("addOrUpdateProductThreshold");
        ProductThreshold productThreshold = instance.getProductThreshold(JUnitConstants.THRESHOLD_ID);
        instance.addOrUpdateProductThreshold(productThreshold);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of getProductThreshold method, of class ThresholdService.
     */
    @Test
    public void testGetProductThreshold_String() {
        System.out.println("getProductThreshold");
        ProductThreshold result = instance.getProductThreshold(JUnitConstants.THRESHOLD_ID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetProductThreshold_String");
        }
    }

    /**
     * Test of getProductThreshold method, of class ThresholdService.
     */
    @Test
    public void testGetProductThreshold_Product_Store() throws Exception {
        System.out.println("getProductThreshold");
        ProductThreshold result = instance.getProductThreshold(product, store);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetProductThreshold_Product_Store");
        }
    }

    /**
     * Test of getStoreWiseThresholdList method, of class ThresholdService.
     */
//    @Test
//    public void testGetStoreWiseThresholdList() throws Exception {
//        System.out.println("getStoreWiseThresholdList");
//        String searchString = "";
//        Paging paging = new Paging(0, 30);
//        List result = instance.getStoreWiseThresholdList(store, searchString, paging);
//        if (result != null) {
//            assertTrue("Data is returned properly", true);
//        } else {
//            fail("Test case failed : testGetStoreWiseThresholdList");
//        }
//    }

    /**
     * Test of getProductWiseThresholdList method, of class ThresholdService.
     */
    @Test
    public void testGetProductWiseThresholdList() throws Exception {
        System.out.println("getProductWiseThresholdList");
        String searchString = "";
        Paging paging = new Paging(0, 30);
        List result = instance.getProductWiseThresholdList(product, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetProductWiseThresholdList");
        }
    }

    /**
     * Test of getThresholdStockList method, of class ThresholdService.
     */
    @Test
    public void testGetThresholdStockList() throws Exception {
        System.out.println("getThresholdStockList");
        String searchString = "";
        Paging paging = new Paging(0, 30);
        List result = instance.getThresholdStockList(company, store, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetThresholdStockList");
        }
    }
}
