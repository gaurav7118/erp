/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

import com.krawler.common.admin.KWLTimeZone;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
public class AccCustomReportServiceImplTest {

    public AccCustomReportServiceImplTest() {
        
    }
    @Autowired
    private AccCustomReportService instance;
    @Autowired
    private HibernateTransactionManager txnManager;
    @Autowired
    private WSUtilService wsUtilService;
    
    TransactionStatus status = null;

    private static String dateFormat ="";
    private static String userDateFormat ="";
   
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
        jobj.put("cdomain", JUnitConstants.SUBDOMAIN);
        jobj.put("userid", JUnitConstants.USER_ID);
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
     * Test of getModuleCategories method, of class AccCustomReportService.
     */
    @Test
    public void testGetModuleCategories() throws Exception {
        System.out.println("getModuleCategories");
        Map<String,Object> paramsMap = new HashMap<>();
        paramsMap.put("companyID", JUnitConstants.COMPANY_ID);
        paramsMap.put("isPivot", false);
        JSONObject result = instance.getModuleCategories(paramsMap);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetModuleCategories");
        }
    }

    /**
     * Test of getModules method, of class AccCustomReportService.
     */
    @Test
    public void testGetModules() throws Exception {
        System.out.println("getModules");
        JSONObject result = instance.getModules(JUnitConstants.MODULE_CATEGORY_ID, JUnitConstants.MODULE_CATEGORY_NAME);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetModules");
        }
    }

    /**
     * Test of getFields method, of class AccCustomReportService.
     */
    @Test
    public void testGetFields() throws Exception {
        System.out.println("getFields");
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.moduleid,JUnitConstants.MODULE_ID);
        requestParams.put(Constants.useridKey, JUnitConstants.USER_ID);
        requestParams.put(Constants.companyKey,JUnitConstants.COMPANY_ID);
        JSONObject result = instance.getFields(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetFields");
        }
    }

    /**
     * Test of saveCustomReport method, of class AccCustomReportService.
     */
    @Test
    public void testSaveCustomReport() throws Exception {
        System.out.println("saveCustomReport");

        HashMap<String, Object> valueMap = new HashMap<>();
        valueMap.put("companyID", JUnitConstants.COMPANY_ID);
        valueMap.put("userId", JUnitConstants.USER_ID);
        valueMap.put("reportName", "Test Report Name");
        valueMap.put("reportDesc", "Test Report Description");
        valueMap.put("moduleID", JUnitConstants.MODULE_ID);
        valueMap.put("moduleCatId", JUnitConstants.MODULE_CATEGORY_ID);
        valueMap.put("deleted", "false");
        valueMap.put("nondeleted", "false");
        valueMap.put("pendingapproval", "false");
        valueMap.put("isLeaseFixedAsset", false);
        
//        JSONArray selectedRowsJSON = new JSONArray(JUnitConstants.CUSTOM_REPORT_ROW);
        valueMap.put("selectedRows", JUnitConstants.CUSTOM_REPORT_ROW);
        JSONArray filterArray=new JSONArray();  
        JSONObject result = instance.saveOrUpdateCustomReport(valueMap,filterArray);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testSaveCustomReport");
        }
    }

    /**
     * Test of getCustomReportList method, of class AccCustomReportService.
     */
    @Test
    public void testGetCustomReportList() throws Exception {
        System.out.println("getCustomReportList");
        String start = "0";
        String limit = "30";
        String browsertz = "";
        DateFormat df = new SimpleDateFormat();
        String Filter = "[]";
        JSONObject paramJobj = new JSONObject();
        paramJobj.put("moduleID",JUnitConstants.MODULE_CATEGORY_ID);
        paramJobj.put("companyid",JUnitConstants.COMPANY_ID);
        paramJobj.put("userid",JUnitConstants.USER_ID);
        paramJobj.put("start",start);
        paramJobj.put("limit",limit);
        paramJobj.put("browsertz",browsertz);
        paramJobj.put("Filter",Filter);
        JSONObject result = instance.getCustomReportList(paramJobj, df);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetCustomReportList");
        }
    }

    /**
     * Test of executeCustomReport method, of class AccCustomReportService.
     */
    @Test
    public void testExecuteCustomReport() throws Exception {
        System.out.println("executeCustomReport");
        
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("gcurrencyid", JUnitConstants.GLOBAL_CURRENCY_ID);
        requestParams.put("reportID", JUnitConstants.REPORT_ID);
        requestParams.put("userdf", USER_DATE_FORMAT);
        requestParams.put("companyID", JUnitConstants.COMPANY_ID);
        requestParams.put("deleted", "false");
        requestParams.put("nondeleted", "false");
        requestParams.put("pendingapproval", "false");
        requestParams.put("userDateFormat", userDateFormat);
        requestParams.put("df", DATE_FORMAT);
        requestParams.put("df1", DATE_FORMAT);
        requestParams.put("start", 0);
        requestParams.put("limit", 30);
        requestParams.put("fromDate", JUnitConstants.FROM_DATE);
        requestParams.put("toDate", JUnitConstants.TO_DATE);
        requestParams.put("isLeaseFixedAsset", false);
        requestParams.put("filter", "[]");
        JSONObject result = instance.executeCustomReport(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testExecuteCustomReport");
        }
    }

    /**
     * Test of deleteCustomReport method, of class AccCustomReportService.
     */
    @Test
    public void testDeleteCustomReport() throws Exception {
        System.out.println("deleteCustomReport");
        HashMap<String, Object> valueMap = new HashMap<>();
        valueMap.put("reportIds", JUnitConstants.REPORT_ID);
        valueMap.put("companyID", JUnitConstants.COMPANY_ID);
        valueMap.put("userId", JUnitConstants.USER_ID);
        boolean result = instance.deleteCustomReport(valueMap);
        if (result) {
            assertTrue("Data is returned properly", result);
        } else {
            fail("Test case failed : testDeleteCustomReport");
        }
    }

    /**
     * Test of executeCustomReportPreview method, of class
     * AccCustomReportService.
     */
    @Test
    public void testExecuteCustomReportPreview() throws Exception {
        System.out.println("executeCustomReportPreview");
        JSONArray selectedRowsJSONData = new JSONArray(JUnitConstants.CUSTOM_REPORT_ROW_PREVIEW);
        Map valueMap = new HashMap<>();
        valueMap.put("companyID", JUnitConstants.COMPANY_ID);
        valueMap.put("moduleID", JUnitConstants.MODULE_ID);
        valueMap.put("currencyid", JUnitConstants.GLOBAL_CURRENCY_ID);
        valueMap.put("userDateFormat", userDateFormat);
        valueMap.put("isLeaseFixedAsset", false);
        valueMap.put("start", 0);
        valueMap.put("limit", 30);
        valueMap.put("deleted", "false");
        valueMap.put("nondeleted", "false");
        valueMap.put("pendingapproval", "false");
        valueMap.put("gcurrencyid", JUnitConstants.GLOBAL_CURRENCY_ID);
        valueMap.put("filter", "[]");
        JSONObject result = instance.executeCustomReportPreview(selectedRowsJSONData, valueMap);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testExecuteCustomReportPreview");
        }
    }

    /**
     * Test of updateCustomReportNameAndDescription method, of class
     * AccCustomReportService.
     */
    @Test
    public void testUpdateCustomReportNameAndDescription() throws Exception {
        System.out.println("updateCustomReportNameAndDescription");
        HashMap<String, Object> valueMap;
        valueMap = new HashMap<>();
        valueMap.put("reportNo", JUnitConstants.REPORT_ID);
        valueMap.put("userDateFormat", userDateFormat);
        valueMap.put("companyID", JUnitConstants.COMPANY_ID);
        valueMap.put("userId", JUnitConstants.USER_ID);
        valueMap.put("reportNewName", "Test Report 1");
        valueMap.put("reportNewDesc", "Test Description 1");
        valueMap.put("isreportNameFieldEdited", "false");
        
        JSONObject result = instance.updateCustomReportNameAndDescription(valueMap);
        if (result != null && result.has("success")) {
            assertTrue("Data is returned properly", result.getBoolean("success"));
        } else {
            fail("Test case failed : testUpdateCustomReportNameAndDescription");
        }
    }


    /**
     * Test of isCustomReportNameExists method, of class AccCustomReportService.
     */
    @Test
    public void testIsCustomReportNameExists() throws Exception {
        System.out.println("isCustomReportNameExists");
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("reportName", "Test Report 1");
        requestParams.put("companyID", JUnitConstants.COMPANY_ID);
        requestParams.put("userId", JUnitConstants.USER_ID);
        boolean result = instance.isCustomReportNameExists(requestParams);
        if (result) {
            assertTrue("Data is returned properly", result);
        } else {
            assertFalse("Data is returned properly", result);
        }
    }

  
    /**
     * Test of showRowLevelFieldsJSONArray method, of class
     * AccCustomReportService.
     */
    @Test
    public void testShowRowLevelFieldsJSONArray() throws Exception {
        System.out.println("showRowLevelFieldsJSONArray");
        JSONArray selectedRowsJSONData = new JSONArray();
        boolean showRowLevelFieldsflag = false;
        Map result = instance.showRowLevelFieldsJSONArray(selectedRowsJSONData, showRowLevelFieldsflag);
        if (result != null) {
            assertTrue("Data is returned properly", result.containsKey("jarrColumns"));
        } else {
            fail("Test case failed : testShowRowLevelFieldsJSONArray");
        }
    }

    /**
     * Test of getTaxPercent method, of class AccCustomReportService.
     */
    @Test
    public void testGetTaxPercent() throws Exception {
        System.out.println("getTaxPercent");
        Date transactiondate = new Date();
        double result = instance.getTaxPercent(JUnitConstants.COMPANY_ID, transactiondate, JUnitConstants.TAX_ID);
        if (result >= 0) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetTaxPercent");
        }
    }

    /**
     * Test of getProductPrice method, of class AccCustomReportService.
     */
    @Test
    public void testGetProductPrice() throws Exception {
        System.out.println("getProductPrice");
        boolean isPurchase = false;
        Date transactiondate = new Date();
        String affecteduser = "";
        KwlReturnObject result = instance.getProductPrice(JUnitConstants.PRODUCT_ID, isPurchase, transactiondate, affecteduser, JUnitConstants.GLOBAL_CURRENCY_ID);
        if (result != null && result.isSuccessFlag()) {
            assertTrue("Data is returned properly", result.isSuccessFlag());
        } else {
            fail("Test case failed : testGetProductPrice");
        }
    }

    /**
     * Test of getTax method, of class AccCustomReportService.
     */
    @Test
    public void testGetTax() throws Exception {
        System.out.println("getTax");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, JUnitConstants.COMPANY_ID);
        requestParams.put("taxid", JUnitConstants.TAX_ID);
        KwlReturnObject result = instance.getTax(requestParams);
        if (result != null && result.isSuccessFlag()) {
            assertTrue("Data is returned properly", result.isSuccessFlag());
        } else {
            fail("Test case failed : testGetTax");
        }
    }

    /**
     * Test of getCustomReportMeasureFieldJsonArray method, of class
     * AccCustomReportService.
     */
    @Test
    public void testGetCustomReportMeasureFieldJsonArray() throws Exception {
        System.out.println("getCustomReportMeasureFieldJsonArray");
        JSONArray ColumnConfigArr = new JSONArray();
        JSONObject paramJObj=new JSONObject();
        JSONArray result = instance.getCustomReportMeasureFieldJsonArray(ColumnConfigArr, JUnitConstants.MODULE_ID,paramJObj);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetCustomReportMeasureFieldJsonArray");
        }
    }

    /**
     * Test of getApprovalStatus method, of class AccCustomReportService.
     */
    @Test
    public void testGetApprovalStatus() throws Exception {
        System.out.println("getApprovalStatus");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, JUnitConstants.COMPANY_ID);
        requestParams.put(Constants.moduleid, JUnitConstants.MODULE_ID);
        requestParams.put(Constants.billid, JUnitConstants.INVOICE_BILL_ID);
        String result = instance.getApprovalStatus(requestParams);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetApprovalStatus");
        }
    }

    /**
     * Test of getTimzeZoneClassObject method, of class AccCustomReportService.
     */
    @Test
    public void testGetTimzeZoneClassObject() throws Exception {
        System.out.println("getTimzeZoneClassObject");
        Object result = instance.getTimzeZoneClassObject(KWLTimeZone.class.getName(), storageHandlerImpl.getDefaultTimeZoneID());
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetTimzeZoneClassObject");
        }
    }
}
