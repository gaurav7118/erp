/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONObject;
import static junit.framework.Assert.assertEquals;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
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

public class CompanyServiceImplTest {

    @Autowired
    private CompanyService instance;

    public CompanyServiceImplTest() {
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
     * Test of getUserList method, of class CompanyServiceImpl.
     */
    @Test
    public void testGetUserList() throws Exception {
        System.out.println("getUserList");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);

        JSONObject result = instance.getUserList(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetUserList");
        }
    }

    /**
     * Test of getSequenceFormat method, of class CompanyServiceImpl.
     */
    @Test
    public void testGetSequenceFormat() throws Exception {
        System.out.println("getSequenceFormat");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);

        JSONObject result = instance.getSequenceFormat(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetSequenceFormat");
        }
    }

    /**
     * Test of deactivateCompany method, of class CompanyServiceImpl.
     */

    @Test
    public void testDeactivateCompany() throws Exception {
        System.out.println("deactivateCompany");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        JSONObject result = instance.deactivateCompany(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testDeactivateCompany");
        }
    }

    /**
     * Test of getYearLock method, of class CompanyServiceImpl.
     */
    @Test
    public void testGetYearLock() throws Exception {
        System.out.println("getYearLock");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        JSONObject result = instance.getYearLock(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetYearLock");
        }
    }

    /**
     * Test of getUserPermissions method, of class CompanyServiceImpl.
     */
    @Test
    public void testGetUserPermissions() throws Exception {
        System.out.println("getUserPermissions");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        jobj.put("username", "user111");

        JSONObject result = instance.getUserPermissions(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetUserPermissions");
        }
    }

    /**
     * Test of updateCompany method, of class CompanyServiceImpl.
     */
    @Test
    public void testUpdateCompany() throws Exception {
        System.out.println("updateCompany");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        jobj.put("companyname", "Krawler");
        jobj.put("address", "Yerwada");
        jobj.put("city", "Pune");
        jobj.put("state", "4");
        jobj.put("phone", "007");
        jobj.put("fax", "007007");
        jobj.put("zip", "411006");
        jobj.put("website", "www.krawler.com");
        jobj.put("emailid", "abc@krawler.com");
        jobj.put("currency", "1");
        jobj.put("country", "105");
        jobj.put("timezone", "22");
        jobj.put("smtpflow", 1);
        jobj.put("smtppassword", "password");
        jobj.put("smtppath", "path");
        jobj.put("smtppport", "007");

        JSONObject result = instance.updateCompany(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testUpdateCompany");
        }
    }

    /**
     * Test of deleteCompany method, of class CompanyServiceImpl.
     */
//    @Test
    public void testDeleteCompany() throws Exception {
        System.out.println("deleteCompany");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);

        JSONObject result = instance.deleteCompany(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testDeleteCompany");
        }
    }

    /**
     * Test of editUser method, of class CompanyServiceImpl.
     */
    @Test
    public void testEditUser() throws Exception {
        System.out.println("editUser");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        jobj.put("userName", "user111");
        jobj.put("fname", "Kim1");
        jobj.put("lname", "JS1");
        jobj.put("address", "White House");
        jobj.put("emailid", "abc@krawler.com");
        jobj.put("contactno", "007");
        jobj.put("timeZone", 236);

        JSONObject result = instance.editUser(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testEditUser");
        }
    }

    /**
     * Test of getUpdates method, of class CompanyServiceImpl.
     */
//    @Test
    public void testGetUpdates() throws Exception {
        System.out.println("getUpdates");
        JSONObject jobj = null;

        JSONObject result = instance.getUpdates(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetUpdates");
        }
    }

    /**
     * Test of getAccountList method, of class CompanyServiceImpl.
     */
//    @Test
    public void testGetAccountList() throws Exception {
        System.out.println("getAccountList");
        JSONObject jobj = null;

        JSONObject result = instance.getAccountList(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testGetAccountList");
        }
    }

    /**
     * Test of isCompanyActivated method, of class CompanyServiceImpl.
     */
    @Test
    public void testIsCompanyActivated() throws Exception {
        System.out.println("isCompanyActivated");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        boolean result = instance.isCompanyActivated(jobj);
        if (result) {
            assertNull(result);
        } else {
            fail("Test case failed : testIsCompanyActivated");
        }
    }
    
        /**
     * Test of isCompanyActivated method, of class CompanyServiceImpl.
     */
    @Test
    public void testIsCompanyExist() throws Exception {
        System.out.println("testIsCompanyExist");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        JSONObject result = instance.isCompanyExists(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testIsCompanyExist");
        }
    }

    @Test
     public void testDeleteUser() throws Exception {
        System.out.println("testDeleteUser");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        jobj.put("userid", JUnitConstants.USER_ID);

        JSONObject result = instance.deleteUser(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testDeleteUser");
        }
    }
   
    /**
     * Test of VerifyLogin method, of class CompanyServiceImpl.
     */
    @Test
    public void testVerifyLogin() throws Exception {
        System.out.println("testVerifyLogin");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        jobj.put("username", "admin");
        jobj.put("pass", "7110eda4d09e062aa5e4a390b0a572ac0d2c0220");

        JSONObject result = instance.verifyLogin(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testVerifyLogin");
        }
    }
    
    /**
     * Test of CreateUser method, of class CompanyServiceImpl.
     */
    @Test
    public void testCreateUser() throws Exception {
        System.out.println("createUser");
        String uuid = StringUtil.generateUUID();
        JSONObject obj = new JSONObject();
        JSONObject jobj = new JSONObject();
        jobj.put("companyid", JUnitConstants.COMPANY_ID);
        jobj.put("userid", uuid);
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        jobj.put("username", "user111");
        jobj.put("password", "7110eda4d09e062aa5e4a390b0a572ac0d2c0220");
        jobj.put("fname", "Mayur");
        jobj.put("lname", "Bhokase");
        jobj.put("address", "Pune");
        jobj.put("emailid", "mayur.bhokase@krawler.com");
        jobj.put("contactno", "007");
        jobj.put("timeZone", 236);
        obj.put(Constants.data,jobj);

        JSONObject result = instance.createUser(obj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testCreateUser");
        }
    }
    
    /**
     * Test of ActivateDeactivateUser method, of class CompanyServiceImpl.
     */
    @Test
    public void testActivateUser() throws Exception {
        System.out.println("activateUser");
        JSONObject obj = new JSONObject();
        JSONObject jobj = new JSONObject();
        jobj.put("companyid", JUnitConstants.COMPANY_ID);
        jobj.put("username", "user111");
        obj.put("deleteflag",0);
        obj.put(Constants.data,jobj);

        JSONObject result = instance.activateUser(obj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testActivateUser");
        }
    }
    
        /**
     * Test of ActivateDeactivateUser method, of class CompanyServiceImpl.
     */
    @Test
    public void testDeactivateUser() throws Exception {
        System.out.println("deactivateUser");
        JSONObject obj = new JSONObject();
        JSONObject jobj = new JSONObject();
        jobj.put("companyid", JUnitConstants.COMPANY_ID);
        jobj.put("username", "user111");
        obj.put("deleteflag",1);
        obj.put(Constants.data,jobj);

        JSONObject result = instance.deactivateUser(obj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testDeactivateUser");
        }
    }
    
    /**
     * Test of AssignRole method, of class CompanyServiceImpl.
     */
    @Test
    public void testAssignRole() throws Exception {
        System.out.println("assignRole");
        JSONObject obj = new JSONObject();
        JSONObject jobj = new JSONObject();
        jobj.put("companyid", JUnitConstants.COMPANY_ID);
        jobj.put("userid", JUnitConstants.USER_ID);
        jobj.put("role", "a1");
        obj.put(Constants.data,jobj);

        JSONObject result = instance.assignRole(obj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("Test case failed : testAssignRole");
        }
    }
}
