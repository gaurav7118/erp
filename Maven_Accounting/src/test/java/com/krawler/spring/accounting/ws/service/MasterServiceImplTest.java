/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import junit.framework.TestCase;
import org.junit.After;
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
public class MasterServiceImplTest extends TestCase {

    public MasterServiceImplTest() {
    }

    @Autowired
    private MasterService instance;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private static String customerId;
    private static String productId;
    private static String cdomain = "erptest01";
    private static String cdomain1="rahulerp1";
    private static String maintenanceId;

    @After
    public final void afterMethod() {
        instance = null;
    }

    /**
     * Test of getProduct method, of class MasterServiceImpl.
     *
     * @return
     */
    @Test
    public void testGetProduct() throws Exception {
        System.out.println("getProduct");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", cdomain);

        JSONObject result = instance.getProduct(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
            if (result.has("data")) {
                JSONArray prodArr = result.getJSONArray("data");
                if (prodArr.length() > 0) {
                    productId = prodArr.getJSONObject(0).getString("assetgroupid");
                }
            }
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getProductByContract method, of class MasterServiceImpl.
     */
    @Test
    public void testGetProductByContract() throws Exception {
        System.out.println("getProductByContract");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", cdomain);
        jobj.put("contractnumber", "CID3");

        JSONObject result = instance.getProductByContract(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getCustomers method, of class MasterServiceImpl.
     *
     * @return
     */
    @Test
    public void testGetCustomers() throws Exception {
        System.out.println("**************************************************getCustomers");
        JSONObject jobj = new JSONObject();

        jobj.put("cdomain", cdomain);
        JSONObject result = instance.getCustomers(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
            if (result.has("data")) {
                JSONArray custArr = result.getJSONArray("data");
                if (custArr.length() > 0) {
                    customerId = custArr.getJSONObject(0).getString("customerid");
                }
            }
        } else {
            fail("The test case is a prototype.");
        }
        // TODO review the generated test code and remove the default call to fail.        
    }

    /**
     * Test of getAsset method, of class MasterServiceImpl.
     */
    @Test
    public void testGetAssetbycdomain() throws Exception {
        System.out.println("getAsset");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", cdomain);

        JSONObject result = instance.getAsset(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getAsset method, of class MasterServiceImpl.
     */
    @Test
    public void testGetAssetbyCdomainandAssetids() throws Exception {
        System.out.println("getAsset");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", cdomain);
        ArrayList<String> list = new ArrayList<String>();
        list.add("12");
        list.add("13");
        list.add("14");
        list.add("15");
        list.add("22");
        jobj.put("assetids", list);

        JSONObject result = instance.getAsset(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getTax method, of class MasterServiceImpl.
     */
    @Test
    public void testGetTax() throws Exception {
        System.out.println("getTax");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", "optus");
        jobj.put("username", "anton");
        jobj.put("isforlms", "false");
        jobj.put("isSales", "false");

        JSONObject result = instance.getTax(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getCurrencyExchange method, of class MasterServiceImpl.
     */
    @Test
    public void testGetCurrencyExchange() throws Exception {
        System.out.println("getCurrencyExchange");
        JSONObject jobj = new JSONObject();
        jobj.put("cdomain", cdomain);
        jobj.put("currencycode", "SGD");
//        jobj.put("transacationdateStr", "");

        JSONObject result = instance.getCurrencyExchange(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getCostCenter method, of class MasterServiceImpl.
     */
    @Test
    public void testGetCostCenter() throws Exception {
        System.out.println("getCostCenter");
        JSONObject jobj = new JSONObject();
        JSONObject result = instance.getCostCenter(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getTerm method, of class MasterServiceImpl.
     */
    @Test
    public void testGetTerm() throws Exception {
        System.out.println("getTerm");
        String cdomain = "myplace";
        JSONObject jobj = new JSONObject();
        JSONObject result = instance.getTerm(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getAllCountry method, of class MasterServiceImpl.
     */
    @Test
    public void testGetAllCountry() throws Exception {
        System.out.println("getAllCountry");

        JSONObject result = instance.getAllCountry();
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getAllCurrency method, of class MasterServiceImpl.
     */
    @Test
    public void testGetAllCurrency() throws Exception {
        System.out.println("getAllCurrency");
        JSONObject jobj=new JSONObject();
        JSONObject result = instance.getAllCurrency(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getAllTimeZone method, of class MasterServiceImpl.
     */
    @Test
    public void testGetAllTimeZone() throws Exception {
        System.out.println("getAllTimeZone");

        JSONObject result = instance.getAllTimeZone();
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getAllStates method, of class MasterServiceImpl.
     */
    @Test
    public void testGetAllStates() throws Exception {
        System.out.println("getAllStates");
        String countryid = "105";

        JSONObject result = instance.getAllStates(countryid);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of getDateFormat method, of class MasterServiceImpl.
     */
    @Test
    public void testGetDateFormat() throws Exception {
        System.out.println("getDateFormat");

        JSONObject result = instance.getDateFormat();
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveTax method, of class MasterServiceImpl.
     */
    @Test
    public void testSaveTax() throws Exception {
        System.out.println("saveTax");
        JSONObject inputJsonObj = new JSONObject();
        inputJsonObj.put("cdomain", "assettest");
        inputJsonObj.put("dateformat", "");
        JSONObject innerO = new JSONObject();
        String taxid = UUID.randomUUID().toString();
        System.out.println("taxid : " + taxid);
        innerO.put("taxid", taxid);
        innerO.put("taxname", StringUtil.DecodeText("a11"));
        innerO.put("taxdescription", StringUtil.DecodeText("a11-desc"));
        innerO.put("taxcode", "aaaa12@12%");
        innerO.put("taxCodeWithoutPercentage", StringUtil.DecodeText("a11@11"));//ERP-10979
        innerO.put("taxtypeid", "1");
        innerO.put("applydateStr", (new Date()).toString());
        innerO.put("percent", "441");
        JSONArray array = new JSONArray();
        array.put(innerO);
        inputJsonObj.put("taxdetails", array);

        JSONObject result = instance.saveTax(inputJsonObj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveTerm method, of class MasterServiceImpl.
     */
    @Test
    public void testSaveTerm() throws Exception {
        System.out.println("saveTerm");
        JSONObject inputJsonObj = new JSONObject();
        inputJsonObj.put("cdomain", "assettest");
        JSONObject innerO = new JSONObject();
        innerO.put("crmtermid", "cccz");
        innerO.put("termname", "cccz");
        innerO.put("termdays", "1");
        JSONArray array = new JSONArray();
        array.put(innerO);
        inputJsonObj.put("termdetails", array);

        JSONObject result = instance.saveTerm(inputJsonObj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of saveProjectDetails method, of class MasterServiceImpl.
     */
    @Test
    public void testSaveProjectDetails() throws Exception {
        System.out.println("saveProjectDetails");
        JSONObject jobj = null;
        jobj.put("cdomain", "pmacc011");
        JSONArray array = new JSONArray();
        JSONObject innerO = new JSONObject();
        innerO.put("projectid", "pid001");
        innerO.put("projectname", "pname001");
        innerO.put("isEdit", "true");

        array.put(innerO);
        jobj.put("projects", array);

        JSONObject result = instance.saveProjectDetails(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of deleteProjectDetails method, of class MasterServiceImpl.
     */
    @Test
    public void testDeleteProjectDetails() throws Exception {
        System.out.println("deleteProjectDetails");
        JSONObject jobj = null;
        jobj.put("cdomain", "pmacc011");
        JSONArray array = new JSONArray();
        JSONObject innerO = new JSONObject();
        innerO.put("projectid", "pid001");
        innerO.put("projectname", "pname001");
        innerO.put("isEdit", "true");
        array.put(innerO);
        jobj.put("projects", array);

        JSONObject result = instance.deleteProjectDetails(jobj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }

    public Map<String, Object> getTransactionAttributes(MasterService instance) throws Exception {
        this.instance = instance;
        Map<String, Object> transactionAttributes = new HashMap<String, Object>();
        testGetCustomers();
//        testGetProduct();
        transactionAttributes.put("customerid", customerId);
        transactionAttributes.put("productid", null);
        return transactionAttributes;
    }

    /**
     * Test of saveCustomer method, of class MasterServiceImpl.
     */
    @Test
    public void testSaveCustomer() throws Exception {
        System.out.println("saveCustomer");
        JSONObject inputJsonObj = new JSONObject();
        inputJsonObj.put("cdomain", "erptest01");
        inputJsonObj.put("dateformat", "yyyy-MM-dd");
        inputJsonObj.put("name", "KapilGupta");
        inputJsonObj.put("address", "Yerawada");
        inputJsonObj.put("email", "KapilG@test12345.com");
        inputJsonObj.put("contactno", "123456789");
        inputJsonObj.put("accountcode", "KapilGUPTA");
        inputJsonObj.put("isVendor", false);
        inputJsonObj.put("accountcreationdate", "2016-05-05");
        JSONObject result = instance.saveCustomer(inputJsonObj);
        System.out.println("saveCustomer -> " + result);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case has failed.");
        }
    }

    /**
     * Test of saveProductMaintenance method, of class MasterServiceImpl.
     */
    @Test
    public void test1SaveProductMaintenance() throws Exception {
        System.out.println("saveProductMaintenance");
        JSONObject inputJsonObj = new JSONObject();
        inputJsonObj.put("cdomain", cdomain1);
        inputJsonObj.put("maintenancenumber", "654321");
        inputJsonObj.put("maintainanceamt", "1500");
        inputJsonObj.put("accountid", "8e8e13bd-e6db-4c0c-aa22-2743d5e9c0cb");
        inputJsonObj.put("contractid", "402880494ec9f8eb014eca1ebb9d0005");

        JSONObject result = instance.saveProductMaintenance(inputJsonObj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
            if(result.has("maintenanceid")){
                maintenanceId = result.getString("maintenanceid");
            }
        } else {
            fail("The test case is a prototype.");
        }
    }

    /**
     * Test of deleteProductMaintenance method, of class MasterServiceImpl.
     */
    @Test
    public void test2DeleteProductMaintenance() throws Exception {
        System.out.println("deleteProductMaintenance");
        JSONObject inputJsonObj = new JSONObject();
        inputJsonObj.put("cdomain", cdomain1);
        JSONArray maintenanceIdArr = new JSONArray();
        maintenanceIdArr.put(maintenanceId);
        inputJsonObj.put("maintainanceids", maintenanceIdArr);

        JSONObject result = instance.deleteProductMaintenance(inputJsonObj);
        if (result != null && result.has(Constants.RES_success)) {
            assertEquals(true, result.getBoolean("success"));
        } else {
            fail("The test case is a prototype.");
        }
    }
}
