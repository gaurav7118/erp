/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
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
public class AccContractManagementServiceDAOTest {

    public AccContractManagementServiceDAOTest() {
    }
    @Autowired
    private AccContractManagementServiceDAO instance;
    private static String COMPANY_ID = JUnitConstants.COMPANY_ID;
    private static String CONTRACT_ID = JUnitConstants.CONTRACT_ID;

    /**
     * Test of getContractMasterDetails method, of class
     * AccContractManagementServiceDAO.
     */
    @Test
    public void testGetContractMasterDetails() throws Exception {
        System.out.println("getContractMasterDetails");
        Map<String, Object> requestParams = getCommonParameters();
        JSONObject result = instance.getContractMasterDetails(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetContractMasterDetails");
        }
    }

    /**
     * Test of getTemporarySavedFiles method, of class
     * AccContractManagementServiceDAO.
     */
    @Test
    public void testGetTemporarySavedFiles() {
        System.out.println("getTemporarySavedFiles");
        Map<String, Object> requestParams = getCommonParameters();
        JSONObject result = instance.getTemporarySavedFiles(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetTemporarySavedFiles");
        }
    }

    /**
     * Test of getMasterContracts method, of class
     * AccContractManagementServiceDAO.
     */
    @Test
    public void testGetMasterContracts() throws Exception {
        System.out.println("getMasterContracts");
        Map<String, Object> requestParams = getCommonParameters();
        JSONObject result = instance.getMasterContracts(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetMasterContracts");
        }
    }

    /**
     * Test of deleteMasterContracts method, of class
     * AccContractManagementServiceDAO.
     */
    @Test
    public void testDeleteMasterContracts() throws Exception {
        System.out.println("deleteMasterContracts");
        Map<String, Object> requestParams = getCommonParameters();
        String[] arrayOfID = new String[] {CONTRACT_ID};
        requestParams.put("idsfordelete", arrayOfID);
        JSONObject result = instance.deleteMasterContracts(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testDeleteMasterContracts");
        }
    }

    /**
     * Test of deleteMasterContractData method, of class
     * AccContractManagementServiceDAO.
     */
    @Test
    public void testDeleteMasterContractData() throws Exception {
        System.out.println("deleteMasterContractData");
        Map<String, Object> requestParams = getCommonParameters();
        instance.deleteMasterContractData(requestParams);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of deleteMasterContractsPermanently method, of class
     * AccContractManagementServiceDAO.
     */
    @Test
    public void testDeleteMasterContractsPermanently() throws Exception {
        System.out.println("deleteMasterContractsPermanently");
        Map<String, Object> requestParams = getCommonParameters();
        JSONObject result = instance.deleteMasterContractsPermanently(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testDeleteMasterContractsPermanently");
        }
    }

    /**
     * Test of getMasterContractLinkingInformation method, of class
     * AccContractManagementServiceDAO.
     */
    @Test
    public void testGetMasterContractLinkingInformation() throws Exception {
        System.out.println("getMasterContractLinkingInformation");
        Map<String, Object> requestParams = getCommonParameters();
        JSONArray jArr = new JSONArray();
        JSONArray result = instance.getMasterContractLinkingInformation(requestParams, jArr);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetMasterContractLinkingInformation");
        }
    }

    /**
     * Test of getMasterContractRows method, of class
     * AccContractManagementServiceDAO.
     */
    @Test
    public void testGetMasterContractRows() throws Exception {
        System.out.println("getMasterContractRows");
        Map<String, Object> requestParams = getCommonParameters();
         requestParams.put("mrpcontractid", CONTRACT_ID);
        JSONObject result = instance.getMasterContractRows(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetMasterContractRows");
        }
    }
    
     public Map<String, Object> getCommonParameters() {
        Map<String, Object> requestParams = new HashMap<>();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            requestParams.put(Constants.df, df);
            requestParams.put("mrpcontractid", CONTRACT_ID);
            requestParams.put("companyid", COMPANY_ID);
            requestParams.put("moduleid", "1107");
            requestParams.put(Constants.userdf, df);
            requestParams.put("gcurrencyid", "1");
            requestParams.put("requestcontextutilsobj", Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));

        } catch (Exception ex) {
            Logger.getLogger(AccContractManagementServiceDAOTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }
}
