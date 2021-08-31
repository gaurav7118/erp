/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.common;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import static org.junit.Assert.*;

/**
 *
 * @author krawler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/applicationContextList.xml")
public class FieldManagerServiceImplTest {

    @Autowired
    private FieldManagerService instance;
    @Autowired
    private HibernateTransactionManager txnManager;
    TransactionStatus status = null;
    public static final String COMPANYID = JUnitConstants.COMPANY_ID;

    public FieldManagerServiceImplTest() {
    }

    @Before
    public void setUp() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("FieldManagerServiceImplPropogation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of getColumnHeadersConfigList method, of class FieldManagerService.
     */
    @Test
    public void testGetColumnHeadersConfigList() throws Exception {
        System.out.println("getColumnHeadersConfigList");
        JSONObject paramsjobj = new JSONObject();
        paramsjobj.put(Constants.moduleIds, "2");
        paramsjobj.put(Constants.companyKey, COMPANYID);
        JSONArray result = instance.getColumnHeadersConfigList(paramsjobj);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetMobileFieldsConfig");
        }
    }
}
