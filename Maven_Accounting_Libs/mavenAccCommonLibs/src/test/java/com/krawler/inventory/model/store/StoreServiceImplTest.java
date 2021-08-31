/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.store;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.InventoryWarehouse;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.Paging;
import com.krawler.inventory.model.location.Location;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
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
public class StoreServiceImplTest {

    @Autowired
    private StoreService instance;
    @Autowired
    private HibernateTransactionManager txnManager;
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAO;
    
    TransactionStatus status = null;
    Company company = null;
    User user = null;
    Location location = null;
    Store store = null;

    public StoreServiceImplTest() {
    }

    @Before
    public void setUp() throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("InvDocumentServiceTest_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);

        KwlReturnObject companyResult = accountingHandlerDAO.getObject(Company.class.getName(), JUnitConstants.COMPANY_ID);
        company = (Company) companyResult.getEntityList().get(0);

        KwlReturnObject userResult = accountingHandlerDAO.getObject(User.class.getName(), JUnitConstants.USER_ID);
        user = (User) userResult.getEntityList().get(0);

        KwlReturnObject storeResult = accountingHandlerDAO.getObject(Store.class.getName(), JUnitConstants.STORE_UUID);
        store = (Store) storeResult.getEntityList().get(0);

        KwlReturnObject locationResult = accountingHandlerDAO.getObject(Location.class.getName(), JUnitConstants.LOCATION_UUID);
        location = (Location) locationResult.getEntityList().get(0);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of getStoreType method, of class StoreService.
     */
    @Test
    public void testGetStoreType() {
        System.out.println("getStoreType");
        int ordinal = 0;
        StoreType result = instance.getStoreType(ordinal);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getERPWarehouse method, of class StoreService.
     */
    @Test
    public void testGetERPWarehouse() throws Exception {
        System.out.println("getERPWarehouse");
        InventoryWarehouse result = instance.getERPWarehouse(JUnitConstants.STORE_UUID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoreById method, of class StoreService.
     */
    @Test
    public void testGetStoreById() throws Exception {
        System.out.println("getStoreById");
        Store result = instance.getStoreById(JUnitConstants.STORE_UUID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getDefaultStore method, of class StoreService.
     */
    @Test
    public void testGetDefaultStore() throws Exception {
        System.out.println("getDefaultStore");
        Store result = instance.getDefaultStore(company);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoreByAbbreviation method, of class StoreService.
     */
    @Test
    public void testGetStoreByAbbreviation() throws Exception {
        System.out.println("getStoreByAbbreviation");
        Store result = instance.getStoreByAbbreviation(company, JUnitConstants.STORE_ABBREVIATION);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of addStore method, of class StoreService.
     */
    @Test
    public void testAddStore() throws Exception {
        System.out.println("addStore");
        instance.addStore(user, store);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of updateStore method, of class StoreService.
     */
    @Test
    public void testUpdateStore() throws Exception {
        System.out.println("updateStore");
        instance.updateStore(user, store);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of removeStore method, of class StoreService.
     */
    @Test
    public void testRemoveStore() throws Exception {
        System.out.println("removeStore");
        instance.removeStore(user, store);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of activateStore method, of class StoreService.
     */
    @Test
    public void testActivateStore() throws Exception {
        System.out.println("activateStore");
        instance.activateStore(user, store);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of deactivateStore method, of class StoreService.
     */
    @Test
    public void testDeactivateStore() throws Exception {
        System.out.println("deactivateStore");
        instance.deactivateStore(user, store);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of getStores method, of class StoreService.
     */
    @Test
    public void testGetStores_3args() throws Exception {
        System.out.println("getStores");
        String searchString = "";
        Paging paging = new Paging(0, 30);
        List result = instance.getStores(company, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStores method, of class StoreService.
     */
    @Test
    public void testGetStores_4args() throws Exception {
        System.out.println("getStores");
        Boolean isActive = true;
        String searchString = "";
        Paging paging = new Paging(0, 30);
        List result = instance.getStores(company, isActive, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoresByTypes method, of class StoreService.
     */
    @Test
    public void testGetStoresByTypes() throws Exception {
        System.out.println("getStoresByTypes");
        Boolean isActive = true;
        StoreType[] storeTypes = StoreType.values();
        String searchString = "";
        Paging paging = new Paging(0, 30);
        boolean isForAvailableWarehouse = true;
        List result = instance.getStoresByTypes(company, isActive, storeTypes, searchString, paging, isForAvailableWarehouse, true, true);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoresByStoreManagers method, of class StoreService.
     */
    @Test
    public void testGetStoresByStoreManagers() throws Exception {
        System.out.println("getStoresByStoreManagers");
        Boolean isActive = true;
        StoreType[] storeTypes = StoreType.values();
        String searchString = "";
        Paging paging = new Paging(0, 30);
        List result = instance.getStoresByStoreManagers(user, isActive, storeTypes, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoresByStoreExecutives method, of class StoreService.
     */
    @Test
    public void testGetStoresByStoreExecutives() throws Exception {
        System.out.println("getStoresByStoreExecutives");
        Boolean isActive = true;
        StoreType[] storeTypes = StoreType.values();
        String searchString = "";
        Paging paging = new Paging(0, 30);
        List result = instance.getStoresByStoreExecutives(user, isActive, storeTypes, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoresByStoreExecutivesAndManagers method, of class
     * StoreService.
     */
    @Test
    public void testGetStoresByStoreExecutivesAndManagers() throws Exception {
        System.out.println("getStoresByStoreExecutivesAndManagers");
        Boolean isActive = true;
        StoreType[] storeTypes = StoreType.values();
        String searchString = "";
        Paging paging = new Paging(0, 30);
        List result = instance.getStoresByStoreExecutivesAndManagers(user, isActive, storeTypes, searchString, paging,true,true);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoresByUser method, of class StoreService.
     */
    @Test
    public void testGetStoresByUser() throws Exception {
        System.out.println("getStoresByUser");
        Boolean isActive = true;
        StoreType[] storeTypes = StoreType.values();
        String searchString = "";
        Paging paging = new Paging(0, 30);
        boolean excludeQARepair = false;
        boolean includePickandPackStore= false;
        List result = instance.getStoresByUser(JUnitConstants.USER_ID, isActive, storeTypes, excludeQARepair, searchString, paging,includePickandPackStore);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoresByQAPerson method, of class StoreService.
     */
    @Test
    public void testGetStoresByQAPerson() throws Exception {
        System.out.println("getStoresByQAPerson");
        List result = instance.getStoresByQAPerson(user);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

//    /**
//     * Test of getMovementTypeName method, of class StoreService.
//     */
//    @Test
//    public void testGetMovementTypeName() throws Exception {
//        System.out.println("getMovementTypeName");
//        String movementTypeId = "";
//        String result = instance.getMovementTypeName(movementTypeId);
//        if (result != null) {
//            assertTrue("Data is returned properly", true);
//        } else {
//            fail("Test case failed : testGetStockBooking");
//        }
//    }

    /**
     * Test of getUnitNameFromWarehouseid method, of class StoreService.
     */
    @Test
    public void testGetUnitNameFromWarehouseid() throws Exception {
        System.out.println("getUnitNameFromWarehouseid");
        String result = instance.getUnitNameFromWarehouseid(JUnitConstants.WAREHOUSE_ID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getInventoryProductDetails method, of class StoreService.
     */
    @Test
    public void testGetInventoryProductDetails() throws Exception {
        System.out.println("getInventoryProductDetails");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("transactionno", "");
        requestParams.put("companyid", JUnitConstants.COMPANY_ID);
        requestParams.put(Constants.moduleid, JUnitConstants.MODULE_ID);
        KwlReturnObject result = instance.getInventoryProductDetails(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getInventoryBatchDetailsid method, of class StoreService.
     */
    @Test
    public void testGetInventoryBatchDetailsid() throws Exception {
        System.out.println("getInventoryBatchDetailsid");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("stockrequestbillid", "");
        KwlReturnObject result = instance.getInventoryBatchDetailsid(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoreMaster method, of class StoreService.
     */
    @Test
    public void testGetStoreMaster() throws Exception {
        System.out.println("getStoreMaster");
        StoreMaster result = instance.getStoreMaster(JUnitConstants.STOREMASTER_ID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoreMasterByName method, of class StoreService.
     */
    @Test
    public void testGetStoreMasterByName() throws Exception {
        System.out.println("getStoreMasterByName");
        String name = "Row 1";
        int type = 1;
        StoreMaster result = instance.getStoreMasterByName(name, JUnitConstants.COMPANY_ID, type);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of saveStoreManagerMapping method, of class StoreService.
     */
    @Test
    public void testSaveStoreManagerMapping() throws Exception {
        System.out.println("saveStoreManagerMapping");
        String[] agents = {JUnitConstants.USER_ID};
        Set result = instance.saveStoreManagerMapping(JUnitConstants.COMPANY_ID, agents);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoreMapping method, of class StoreService.
     */
    @Test
    public void testGetStoreMapping() throws Exception {
        System.out.println("getStoreMapping");
        String[] storeIds = {JUnitConstants.STORE_UUID};
        Set result = instance.getStoreMapping(JUnitConstants.COMPANY_ID, storeIds);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getStoreJson method, of class StoreService.
     */
    @Test
    public void testGetStoreJson() throws Exception {
        System.out.println("getStoreJson");
        JSONObject result = instance.getStoreJson(store);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }
}
