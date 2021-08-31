/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.configuration;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.JUnitConstants;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
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
public class InventoryConfigServiceImplTest {

    @Autowired
    private InventoryConfigService instance;
    @Autowired
    private HibernateTransactionManager txnManager;
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAO;
    TransactionStatus status = null;
    Company company = null;
    User user = null;
    InventoryConfig config = null;

    public InventoryConfigServiceImplTest() {
    }

    @Before
    public void setUp() throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("InventoryConfigServiceImplTest_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);

        KwlReturnObject companyResult = accountingHandlerDAO.getObject(Company.class.getName(), JUnitConstants.COMPANY_ID);
        company = (Company) companyResult.getEntityList().get(0);

        KwlReturnObject userResult = accountingHandlerDAO.getObject(User.class.getName(), JUnitConstants.USER_ID);
        user = (User) userResult.getEntityList().get(0);

        KwlReturnObject configResult = accountingHandlerDAO.getObject(InventoryConfig.class.getName(), JUnitConstants.INVENTORY_CONFIG_ID);
        config = (InventoryConfig) configResult.getEntityList().get(0);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of addConfig method, of class InventoryConfigService.
     */
    @Test
    public void testAddConfig() throws Exception {
        System.out.println("addConfig");
        instance.addConfig(user, config);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of updateConfig method, of class InventoryConfigService.
     */
    @Test
    public void testUpdateConfig() throws Exception {
        System.out.println("updateConfig");
        instance.updateConfig(user, config);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of getConfigById method, of class InventoryConfigService.
     */
    @Test
    public void testGetConfigById() throws Exception {
        System.out.println("getConfigById");
        InventoryConfig result = instance.getConfigById(JUnitConstants.INVENTORY_CONFIG_ID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetConfigById");
        }
    }

    /**
     * Test of getConfigByCompany method, of class InventoryConfigService.
     */
    @Test
    public void testGetConfigByCompany() throws Exception {
        System.out.println("getConfigByCompany");
        InventoryConfig result = instance.getConfigByCompany(company);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetConfigByCompany");
        }
    }
}
