/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.utils.json.base.JSONObject;
import org.junit.After;
import org.junit.AfterClass;
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
public class ContractServiceImplTest {

    public ContractServiceImplTest() {
    }

    @Autowired
    private ContractService instance;

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @After
    public final void afterMethod() {
        instance = null;
    }
    private static String company1="smsacc035";
    private static String company2="integration100";
    private static String company3="f1recreationdemo2";
    private static String company4="rahulerp46";

    /**
     * Test of getContractDetails method, of class ContractServiceImpl.
     */
    @Test
    public void testGet1ContractDetailsByCustomer() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company1);
        requestJobj.put("customerid", "ff808081456e830d01456f0bfd9500e0");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractDetails(requestJobj);
        System.out.println("getContractDetailsByCustomer -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getContractDetails method, of class ContractServiceImpl.
     */
    @Test
    public void testGet2ContractDetailsByContract() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company1);
        requestJobj.put("contractid", "ff80808146dcb0720146dcd95ecc00a0");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractDetails(requestJobj);
        System.out.println("getContractDetailsByContract -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getContractTermDetails method, of class ContractServiceImpl.
     */
    @Test
    public void testGet3ContractTermDetails() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company1);
        requestJobj.put("contractid", "ff80808146dcb0720146dcd95ecc00a0");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractTermDetails(requestJobj);
        System.out.println("getContractTermDetails -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getContractInvoiceDetails method, of class ContractServiceImpl.
     */
    @Test
    public void testGet4ContractInvoiceDetails() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company2);
        requestJobj.put("contractid", "4028805047b100590147b4f3edf8025b");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractInvoiceDetails(requestJobj);
        System.out.println("getContractInvoiceDetails -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getContractInvoiceDetails method, of class ContractServiceImpl.
     */
    @Test
    public void testGet5ContractInvoiceDetailsReplacementType() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company3);
        requestJobj.put("contractid", "40288050481bdf0601481c0e3b47002e");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        requestJobj.put("invoicetype", "replacement");
        JSONObject result = instance.getContractInvoiceDetails(requestJobj);
        System.out.println("getContractInvoiceDetailsReplacementtype -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getContractInvoiceDetails method, of class ContractServiceImpl.
     */
    @Test
    public void testGet6ContractInvoiceDetailsMaintenanceType() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company3);
        requestJobj.put("contractid", "40288050481bdf0601481c0e3b47002e");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        requestJobj.put("invoicetype", "maintenance");
        JSONObject result = instance.getContractInvoiceDetails(requestJobj);
        System.out.println("getContractInvoiceDetailsMaintenanceType -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getContractNormalDOItem method, of class ContractServiceImpl.
     */
    @Test
    public void testGet7ContractNormalDOItemByContract() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company2);
        requestJobj.put("contractid", "4028805047b100590147b4f3edf8025b");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractNormalDOItem(requestJobj);
        System.out.println("getContractNormalDOitem -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getContractNormalDOItem method, of class ContractServiceImpl.
     */
    @Test
    public void testGet8ContractNormalDOItemByProdDO() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company4);
        requestJobj.put("contractid", "ff8080815083b5e00150889ddbd61bcc");
        requestJobj.put("productid", "ff808081507fcad901508388063704b6");
        requestJobj.put("doid", "ff8080815088a630015088c259e70006");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractNormalDOItem(requestJobj);
        System.out.println("getContractNormalDOitemRow -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getContractReplacementDOItem method, of class
     * ContractServiceImpl.
     */
    @Test
    public void testGet91ContractReplacementDOItemByContract() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company3);
        requestJobj.put("contractid", "4028805046fc82bc0146fc9035ed0006");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractReplacementDOItem(requestJobj);
        System.out.println("getContractReplacementDOitem -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getContractReplacementDOItem method, of class
     * ContractServiceImpl.
     */
    @Test
    public void testGet92ContractReplacementDOItemByProdDO() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company3);
        requestJobj.put("contractid", "4028805046fc82bc0146fc9035ed0006");
        requestJobj.put("productid", "4028805046fc31e60146fc6d9d65001a");
        requestJobj.put("productReplacementID", "0311e7c4-d186-499f-b765-27a92a9d8758");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractReplacementDOItem(requestJobj);
        System.out.println("getContractReplacementDOitemRow -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getCustomerContractsAgreementDetails method, of class
     * ContractServiceImpl.
     */
    @Test
    public void testGet93ContractAgreementDetails() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company3);
        requestJobj.put("crmaccountid", "c3c9a021-1187-412b-a5e0-20458387c327");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractAgreementDetails(requestJobj);
        System.out.println("getContractAgreement -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getCustomerContractsAgreementDetails method, of class
     * ContractServiceImpl.
     */
    @Test
    public void testGet94CustomerContractsCostAgreementDetails() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company3);
        requestJobj.put("crmaccountid", "c3c9a021-1187-412b-a5e0-20458387c327");
        requestJobj.put("agreementtype", "cost");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractAgreementDetails(requestJobj);
        System.out.println("getContractCostAgreement -> " + result);

        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getCustomerContractsAgreementDetails method, of class
     * ContractServiceImpl.
     */
    @Test
    public void testGet95CustomerContractsServiceAgreementDetails() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company3);
        requestJobj.put("crmaccountid", "c3c9a021-1187-412b-a5e0-20458387c327");
        requestJobj.put("agreementtype", "service");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getContractAgreementDetails(requestJobj);
        System.out.println("getContractServiceAgreement -> " + result);

        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }

    /**
     * Test of getAccountContractDetails method, of class ContractServiceImpl.
     */
    @Test
    public void testGet96AccountContractDetails() throws Exception {

        boolean isSuccess = false;
        JSONObject requestJobj = new JSONObject();
        requestJobj.put("cdomain", company3);
        requestJobj.put("crmaccountid", "c3c9a021-1187-412b-a5e0-20458387c327");
        requestJobj.put("dateformat", "yyyy-MMM-dd");
        JSONObject result = instance.getAccountContractDetails(requestJobj);
        System.out.println("getContractAccount -> " + result);
        if (result != null) {
            if (result.has("success")) {
                assertTrue("Data is returned properly", result.getBoolean("success"));
                isSuccess = true;
            }
        }

        if (!isSuccess) {
            fail("Test case failed");
        }
    }
}
