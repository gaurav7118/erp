/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.defaultfieldsetup;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
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
public class AccFieldSetupServiceDaoImplTest {

    public AccFieldSetupServiceDaoImplTest() {
    }
    @Autowired
    private AccFieldSetupServiceDao instance;
    @Autowired
    private HibernateTransactionManager txnManager;
    @Autowired
    private WSUtilService wsUtilService;
    TransactionStatus status = null;
    
    private static final String COMPANY_ID = JUnitConstants.COMPANY_ID;
    private static final String USER_ID = JUnitConstants.USER_ID;
    private static final String DATA = JUnitConstants.FIELD_SETUP_DATA;
    private static final String TYPE = "summaryView";
    private static final String SUBDOMAIN = JUnitConstants.SUBDOMAIN;
    private static final String MODULE_ID = JUnitConstants.MODULE_ID;
    private static final String MODULE_IDS = JUnitConstants.MULTIPLE_MODULE_ID;
    
    private static String dateFormat = "";
    private static String userDateFormat = "";
    JSONObject finalObj;
    SimpleDateFormat DATE_FORMAT;
    SimpleDateFormat USER_DATE_FORMAT;

    @Before
    public void setUp() throws ServiceException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AccCustomReportServicePropogation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);

        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", SUBDOMAIN);
        jobj.put("userid", USER_ID);
        finalObj = wsUtilService.populateAdditionalInformation(jobj);

        dateFormat = finalObj.optString("dateformat", "yyyy-MM-dd");
        DATE_FORMAT = new SimpleDateFormat(dateFormat);

        userDateFormat = finalObj.optString("userdateformat", "yyyy-MM-dd");
        USER_DATE_FORMAT = new SimpleDateFormat(userDateFormat);

    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of saveMobileFieldsConfigSettings method, of class
     * AccFieldSetupServiceDao.
     */
    @Test
    public void testSaveMobileFieldsConfigSettings() throws Exception {
        System.out.println("saveMobileFieldsConfigSettings");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put(Constants.type, TYPE);
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.moduleid, MODULE_ID);
        paramJobj.put(Constants.data, DATA);
        KwlReturnObject result = instance.saveMobileFieldsConfigSettings(paramJobj);
        if (result != null && result.isSuccessFlag()) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testSaveMobileFieldsConfigSettings");
        }
    }

    /**
     * Test of getMobileFieldsConfig method, of class AccFieldSetupServiceDao.
     */
    @Test
    public void testGetMobileFieldsConfig() throws JSONException {
        System.out.println("getMobileFieldsConfig");
        JSONObject paramJobj = new JSONObject();
        paramJobj.put(Constants.companyKey, COMPANY_ID);
        paramJobj.put(Constants.moduleIds, MODULE_IDS);
        JSONArray result = instance.getMobileFieldsConfig(paramJobj);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetMobileFieldsConfig");
        }
    }
}
