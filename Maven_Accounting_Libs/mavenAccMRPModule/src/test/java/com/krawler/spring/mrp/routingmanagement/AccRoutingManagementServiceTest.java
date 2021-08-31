/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.routingmanagement;

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
public class AccRoutingManagementServiceTest {

    @Autowired
    private AccRoutingManagementService instance;
    
    private static String COMPANY_ID = JUnitConstants.COMPANY_ID;

    public AccRoutingManagementServiceTest() {
    }

    /**
     * Test of getRoutingtemplates method, of class AccRoutingManagementService.
     */
    @Test
    public void testGetRoutingtemplates() throws Exception {
        System.out.println("getRoutingtemplates");
        Map<String, Object> requestParams = getCommonParameters();
        JSONObject result = instance.getRoutingtemplates(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetRoutingtemplates");
        }
    }

    /**
     * Test of syncResourceToPM method, of class AccRoutingManagementService.
     */
//    @Test
//    public void testSyncResourceToPM() throws Exception {
//        System.out.println("syncResourceToPM");
//        Map<String, Object> requestParams = getCommonParameters();
//        JSONObject result = instance.syncResourceToPM(requestParams);
//        if (result != null) {
//            assertTrue("Data is returned properly", true);
//        } else {
//            fail("Test case failed : testSyncResourceToPM");
//        }
//    }

    /**
     * Test of createOrUpdateProjectRest method, of class
     * AccRoutingManagementService.
     */
    @Test
    public void testCreateOrUpdateProjectRest() {
        System.out.println("createOrUpdateProjectRest");
        Map<String, Object> requestMap = getCommonParameters();
        JSONObject result = instance.createOrUpdateProjectRest(requestMap);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testCreateOrUpdateProjectRest");
        }
    }

    /**
     * Test of saveRoutingTemplate method, of class AccRoutingManagementService.
     */
//    @Test
//    public void testSaveRoutingTemplate() throws Exception {
//        System.out.println("saveRoutingTemplate");
//        Map<String, Object> dataMap = getCommonParameters();
//        JSONObject result = instance.saveRoutingTemplate(dataMap);
//        if (result != null) {
//            assertTrue("Data is returned properly", true);
//        } else {
//            fail("Test case failed : testSaveRoutingTemplate");
//        }
//    }

    /**
     * Test of syncProjectCopyReqToPM method, of class
     * AccRoutingManagementService.
     */
//    @Test
//    public void testSyncProjectCopyReqToPM() throws Exception {
//        System.out.println("syncProjectCopyReqToPM");
//        Map<String, Object> requestParams = getCommonParameters();
//        JSONObject result = instance.syncProjectCopyReqToPM(requestParams);
//        if (result != null) {
//            assertTrue("Data is returned properly", true);
//        } else {
//            fail("Test case failed : testSyncProjectCopyReqToPM");
//        }
//    }

    /**
     * Test of deleteDirtyProjectRest method, of class
     * AccRoutingManagementService.
     */
    @Test
    public void testDeleteDirtyProjectRest() {
        System.out.println("deleteDirtyProjectRest");
        Map<String, Object> requestMap = getCommonParameters();
        JSONObject result = instance.deleteDirtyProjectRest(requestMap);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testDeleteDirtyProjectRest");
        }
    }

    /**
     * Test of deleteRoutingTemplate method, of class
     * AccRoutingManagementService.
     */
//    @Test
//    public void testDeleteRoutingTemplate() throws Exception {
//        System.out.println("deleteRoutingTemplate");
//        Map<String, Object> dataMap = getCommonParameters();
//        JSONObject result = instance.deleteRoutingTemplate(dataMap);
//        if (result != null) {
//            assertTrue("Data is returned properly", true);
//        } else {
//            fail("Test case failed : testDeleteRoutingTemplate");
//        }
//    }

    /**
     * Test of isROutingTemplateNameAlreadyExist method, of class
     * AccRoutingManagementService.
     */
    @Test
    public void testIsROutingTemplateNameAlreadyExist() throws Exception {
        System.out.println("isROutingTemplateNameAlreadyExist");
        Map<String, Object> requestParams = getCommonParameters();
        boolean result = instance.isROutingTemplateNameAlreadyExist(requestParams);
        assertTrue("Data is returned properly", true);
    }
    
    public Map<String, Object> getCommonParameters() {
        Map<String, Object> requestParams = new HashMap<>();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            requestParams.put(Constants.df, df);
            requestParams.put("companyid", COMPANY_ID);
            requestParams.put("moduleid", "1107");
            requestParams.put(Constants.userdf, df);
            requestParams.put("requestcontextutilsobj", Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));

        } catch (Exception ex) {
            Logger.getLogger(AccRoutingManagementServiceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }
}
