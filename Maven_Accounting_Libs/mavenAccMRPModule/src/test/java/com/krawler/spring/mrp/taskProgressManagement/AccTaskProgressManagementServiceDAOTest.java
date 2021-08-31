/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.taskProgressManagement;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author krawler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/applicationContextList.xml")
public class AccTaskProgressManagementServiceDAOTest {
    @Autowired
    private AccTaskProgressManagementServiceDAO instance;
    
    private static String COMPANY_ID = JUnitConstants.COMPANY_ID;
    
    public AccTaskProgressManagementServiceDAOTest() {
    }

    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getTaskProgressDetails method, of class AccTaskProgressManagementServiceDAO.
     */
    @Test
    public void testGetTaskProgressDetails() throws Exception {
        System.out.println("getTaskProgressDetails");
        Map<String, Object> requestParams = getTaskProgressCommonParameters();
        JSONObject result = instance.getTaskProgressDetails(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetTaskProgressDetails");
        }
    }

    /**
     * Test of getMaterialConsumedDetails method, of class AccTaskProgressManagementServiceDAO.
     */
    @Test
    public void testGetMaterialConsumedDetails() throws Exception {
        System.out.println("getMaterialConsumedDetails");
        Map<String, Object> requestParams = getTaskProgressCommonParameters();
        JSONObject result = instance.getMaterialConsumedDetails(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetMaterialConsumedDetails");
        }
    }
    
    public Map<String, Object> getTaskProgressCommonParameters() {
        Map<String, Object> requestParams = new HashMap<>();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            requestParams.put(Constants.df, df);
            requestParams.put("companyid", COMPANY_ID);
            requestParams.put("requestcontextutilsobj", Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));

        } catch (Exception ex) {
            Logger.getLogger(AccTaskProgressManagementControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }
}
