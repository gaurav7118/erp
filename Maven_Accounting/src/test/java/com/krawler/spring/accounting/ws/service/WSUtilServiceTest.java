/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.utils.json.base.JSONObject;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Configuration for JUnit class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:config/applicationContextList.xml")
public class WSUtilServiceTest {

    /**
     * Create instance of class.
     */
    @Autowired
    WSUtilService instance;
    /**
     * Constants used in class.
     */
    public static final String SUBDOMAIN = JUnitConstants.SUBDOMAIN;

    public WSUtilServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of populateAdditionalInformation method, of class WSUtilService.
     */
    @Test
    public void testPopulateAdditionalInformation() throws Exception {
        System.out.println("populateAdditionalInformation");
        JSONObject jobj = new JSONObject();
        jobj.put(Constants.RES_CDOMAIN, SUBDOMAIN);

        JSONObject result = instance.populateAdditionalInformation(jobj);
        assertNotNull(result);
    }

    /**
     * Test of getErrorResponse method, of class WSUtilService.
     */
    @Test
    public void testGetErrorResponse() {
        System.out.println("getErrorResponse");
        String errorCode = "e01";
        JSONObject jobj = new JSONObject();
        String errorMsg = "Insufficient data.";

        JSONObject result = instance.getErrorResponse(errorCode, jobj, errorMsg);
        assertNotNull(result);
    }

    /**
     * Test of isCompanyExists method, of class WSUtilService.
     */
    @Test
    public void testIsCompanyExists() throws Exception {
        System.out.println("isCompanyExists");
        JSONObject jobj = new JSONObject();
        jobj.put(Constants.RES_CDOMAIN, SUBDOMAIN);

        boolean result = instance.isCompanyExists(jobj);
        assertTrue(result);
    }
}