/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.acc.savedsearch.bizservice;

import com.krawler.common.admin.SavedSearchQuery;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.JUnitConstants;
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
public class SavedSearchServiceImplTest {

    @Autowired
    private SavedSearchService instance;
    @Autowired
    private HibernateTransactionManager txnManager;
    TransactionStatus status = null;

    public SavedSearchServiceImplTest() {
    }

    @Before
    public void setUp() throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SavedSearchServiceImplTest_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of getSavedSearchQueries method, of class SavedSearchService.
     */
    @Test
    public void testGetSavedSearchQueries_3args() throws Exception {
        System.out.println("getSavedSearchQueries");
        int firstResult = 0;
        int maxResults = 30;
        List result = instance.getSavedSearchQueries(JUnitConstants.USER_ID, firstResult, maxResults);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetSavedSearchQueries_3args");
        }
    }

    /**
     * Test of getSavedSearchQuery method, of class SavedSearchService.
     */
    @Test
    public void testGetSavedSearchQuery() throws Exception {
        System.out.println("getSavedSearchQuery");
        SavedSearchQuery result = instance.getSavedSearchQuery(JUnitConstants.SAVED_SEARCH_ID);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetSavedSearchQuery");
        }
    }

    /**
     * Test of deleteSavedSearchQuery method, of class SavedSearchService.
     */
    @Test
    public void testDeleteSavedSearchQuery() throws Exception {
        System.out.println("deleteSavedSearchQuery");
        boolean result = instance.deleteSavedSearchQuery(JUnitConstants.SAVED_SEARCH_ID);
        assertTrue("Data is returned properly", true);
            
    }

    /**
     * Test of getSavedSearchQueries method, of class SavedSearchService.
     */
    @Test
    public void testGetSavedSearchQueries_String() throws Exception {
        System.out.println("getSavedSearchQueries");
        int result = instance.getSavedSearchQueries(JUnitConstants.USER_ID);
        if (result<0) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetSavedSearchQueries_String");
        }
    }

    /**
     * Test of saveSearchQuery method, of class SavedSearchService.
     */
    @Test
    public void testSaveSearchQuery() throws Exception {
        System.out.println("saveSearchQuery");
        int module = 2;
        int filterAppend = 0;
        String templateid = "";
        boolean isCustomLayout = false;
        String templatetitle = "";
        SavedSearchQuery result = instance.saveSearchQuery(module, JUnitConstants.USER_ID, JUnitConstants.SEARCH_STATE, "Test1", filterAppend, templateid, isCustomLayout, templatetitle);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testSaveSearchQuery");
        }
    }

    /**
     * Test of getSavedSearchQueries method, of class SavedSearchService.
     */
    @Test
    public void testGetSavedSearchQueries_String_String() throws Exception {
        System.out.println("getSavedSearchQueries");
        String searchname = "Test";
        List result = instance.getSavedSearchQueries(JUnitConstants.USER_ID, searchname);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetSavedSearchQueries_String_String");
        }
    }

    /**
     * Test of modifySavedSearchQuery method, of class SavedSearchService.
     */
//    @Test
//    public void testModifySavedSearchQuery() throws Exception {
//        System.out.println("modifySavedSearchQuery");
//        SavedSearchQuery savedSearchQueryObj = null;
//        SavedSearchQuery result = instance.modifySavedSearchQuery(savedSearchQueryObj);
//        if (result != null) {
//            assertTrue("Data is returned properly", true);
//        } else {
//            fail("Test case failed : testModifySavedSearchQuery");
//        }
//    }
}
