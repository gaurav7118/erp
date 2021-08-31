/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.machinemanagement;

import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author krawler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/applicationContextList.xml")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccMachineManagementServiceImplTest {
    
    private final String SUCCESS = "Success";
    private String companyId = "c418e082-ddc0-46bd-aa9b-5b3c5146b433";
    
    @Autowired
    AccMachineManagementServiceDAO instance;
    
    public AccMachineManagementServiceImplTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
        instance = null;
    }


    /**
     * Test of saveMachineMaster method, of class AccMachineManagementServiceImpl.
     * @throws java.lang.Exception
     */
    @Test
    public void testSaveMachineMaster() throws Exception {
        System.out.println("saveMachineMaster");
        Map<String, Object> requestParams =  new HashMap<>();
        JSONObject expResult = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");      
        Date dateofpurchase = sdf.parse(sdf.format(new Date()));
        String processId="40288095545200000154520ab110000a";
        requestParams.put("machinename", "JCB");
        requestParams.put("machineid", "JCB10001");
        requestParams.put("machineserialno", "JC111101101");
        requestParams.put("processid", processId.split(","));
        requestParams.put("purchaseaccount","ff808081538d675401538e15bd2c0620" );
        requestParams.put("issubstitutemachine", "false");
        requestParams.put("companyid", companyId);
        requestParams.put("dateofpurchase", dateofpurchase);
        requestParams.put("insuranceduedate", dateofpurchase);
        requestParams.put("dateofinstallation", dateofpurchase);
        requestParams.put("ageofmachine", "12");
        requestParams.put("machineoperatingcapacity", "10");
        requestParams.put("vendorid", "ff808081538d675401538e165990067d");
        requestParams.put("machineserialno", "Machine-101");
        requestParams.put("machineusescount", "1001");
        
        expResult = instance.saveMachineMaster(requestParams);
        if (expResult != null) {
            assertEquals(SUCCESS, SUCCESS);
        } else {
            fail("The object returned is null");
        }
    }
    
    @Test
    public void testGetMachineMasterDetails() throws Exception {
        System.out.println("getMachineMasterDetails");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("companyid", companyId);
        JSONObject result = instance.getMachineMasterDetails(requestParams);
        if (result != null && result.length() > 0) {
            assertEquals(SUCCESS, SUCCESS);
        } else {
            fail("The object returned is null");
        }
    }

    /**
     * Test of deleteMachineMasterPermanently method, of class AccMachineManagementServiceImpl.
     */
    @Test
    public void testDeleteMachineMasterPermanently() throws Exception {
        System.out.println("deleteMachineMasterPermanently");
        String [] arrayOfID={"402880c454b8c3b70154b8c3c5070001"};
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("companyid", companyId);
        requestParams.put("idsfordelete", arrayOfID);
        JSONObject expResult = null;
        expResult = instance.deleteMachineMasterPermanently(requestParams);
        if (expResult != null) {
            assertEquals(SUCCESS, SUCCESS);
        } else {
           fail("The object returned is null");

        }
    }
    
}
