/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
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
public class AccContractManagementServiceDAOCMNTest {

    @Autowired
    private AccContractManagementServiceDAOCMN instance;
    
    @Autowired
    private HibernateTransactionManager txnManager;
   
    TransactionStatus status = null;

    public AccContractManagementServiceDAOCMNTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("ContractManagementServicePropogation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of getMasterContractRows method, of class
     * AccContractManagementServiceDAOCMN.
     */
    @Test
    public void testGetMasterContractRows() throws Exception {
        System.out.println("getMasterContractRows");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        requestParams.put("dataFormatValue", authHandler.getDateOnlyFormat());
        requestParams.put(Constants.globalCurrencyKey, "6");
        requestParams.put("bills", JUnitConstants.MASTER_CONTRACT_ID);
        requestParams.put("isOrder", false);
        JSONObject result = instance.getMasterContractRows(requestParams);
        if (result != null) {
            assertTrue(result.length()>=0);
        } else {
            fail("Test case failed : testGetMasterContractRows");
        }
    }

    /**
     * Test of saveFileMapping method, of class
     * AccContractManagementServiceDAOCMN.
     */
    @Test
    public void testSaveFileMapping() throws Exception {
        System.out.println("saveFileMapping");
        String uuid = StringUtil.generateUUID();
        Map<String, Object> filemap = new HashMap<>();
        filemap.put("id", uuid);
        filemap.put(Constants.companyid, JUnitConstants.COMPANY_ID);
        filemap.put("documentid", uuid);
        int result = instance.saveFileMapping(filemap);
        if (result >= 0) {
            assertTrue(result >= 0);
        } else {
            fail("Test case failed : testSaveFileMapping");
        }
    }

    /**
     * Test of saveMasterContract method, of class
     * AccContractManagementServiceDAOCMN.
     */
    @Test
    public void testSaveMasterContract() throws Exception {
        System.out.println("saveMasterContract");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyid, JUnitConstants.COMPANY_ID);

        DateFormat df = authHandler.getDateOnlyFormat();

        String jsonDataStr = "{}";
        String paymenttermsdata = "{}";
        String billingcontractdata = "{}";
        String documentrequireddata = "{}";
        String details = "{'detailsObject': [{'productid': '"+JUnitConstants.PRODUCT_ID+"','quantity': 1,'desc': 'sdfdsf','customfield': [{}],"
                + "'uomid': '"+JUnitConstants.UOM_ID+"','baseuomrate': 1,'baseuomquantity': 1,'rate': 200,'discamount': 200,'deliverymode': '','totalnoofunit': '',"
                + "'totalquantity': '','shippingperiodfrom': '','shippingperiodto': '','partialshipmentallowed': '','shipmentstatus': '','shippingagent': '',"
                + "'loadingportcountry': '','loadingport': '','transshipmentallowed': '','dischargeportcountry': '','dischargeport': '','finaldestination': '',"
                + "'postalcode': '','budgetfreightcost': '','shipmentcontratremarks': '','shippingaddress': [],'unitweightvalue': '','unitweight': '','packagingtype': '',"
                + "'certificaterequirement': '','certificate': '','shippingmarksdetails': '','shipmentmode': '','percontainerload': '','palletmaterial': '',"
                + "'packagingprofiletype': '','marking': '','drumorbagdetails': '','drumorbagsize': '','numberoflayers': '','heatingpad': '','palletloadcontainer': ''"
                + "}]}";

        String customfield = "[]";

        requestParams.put("contractdetailsdata", jsonDataStr);
        requestParams.put("billingcontractdata", billingcontractdata);
        requestParams.put("paymenttermsdata", paymenttermsdata);
        requestParams.put("documentrequireddata", documentrequireddata);
        requestParams.put("details", details);
        requestParams.put(Constants.df, df);
        requestParams.put("mastercontractid", JUnitConstants.MASTER_CONTRACT_ID);
        requestParams.put("customfield", customfield);

        JSONObject result = instance.saveMasterContract(requestParams);
        if (result != null) {
            assertTrue(result.length() >= 0);
        } else {
            fail("Test case failed : testSaveMasterContract");
        }
    }
}
