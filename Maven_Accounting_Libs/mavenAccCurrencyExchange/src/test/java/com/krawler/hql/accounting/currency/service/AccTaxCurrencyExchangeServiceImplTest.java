/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting.currency.service;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.spring.accounting.currency.AccTaxCurrencyExchangeDAO;
import com.krawler.spring.accounting.currency.CurrencyContants;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import java.util.*;
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
public class AccTaxCurrencyExchangeServiceImplTest {

    public AccTaxCurrencyExchangeServiceImplTest() {
    }
    @Autowired
    private AccTaxCurrencyExchangeService instance;
    @Autowired
    private AccTaxCurrencyExchangeDAO accTaxCurExchangeDAOObj;
    @Autowired
    private HibernateTransactionManager txnManager;
    
    TransactionStatus status = null;
    
    private static final String COMPANY_ID = JUnitConstants.COMPANY_ID;
    private static final String ERID = JUnitConstants.EXCHANGE_RATE_ID;
    Locale requestcontextutilsobj = new Locale("en", "US");

    @Before
    public void setUp() throws ServiceException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("AccCustomReportServicePropogation_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of saveTaxCurrencyExchange method, of class
     * AccTaxCurrencyExchangeService.
     */
    @Test
    public void testSaveTaxCurrencyExchange() throws Exception {
        System.out.println("saveTaxCurrencyExchange");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put("changerate", false);
        JSONArray jArray = new JSONArray();
        JSONObject jObj = new JSONObject();
        jObj.put(CurrencyContants.APPLYDATE, authHandler.getDateOnlyFormat().format(new Date()));
        jObj.put(CurrencyContants.TODATE, authHandler.getDateOnlyFormat().format(new Date()));
        jObj.put(CurrencyContants.ID, ERID);
        jObj.put(CurrencyContants.EXCHANGERATE, 1);
        jArray.put(jObj);
        requestParams.put(Constants.data, jArray);
        requestParams.put(Constants.companyKey, COMPANY_ID);
        requestParams.put("requestcontextutilsobj", requestcontextutilsobj);
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());

        boolean result = instance.saveTaxCurrencyExchange(requestParams);
        if (result) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testSaveTaxCurrencyExchange");
        }
    }

    /**
     * Test of saveTaxCurrencyExchangeDetail method, of class
     * AccTaxCurrencyExchangeService.
     */
    @Test
    public void testSaveTaxCurrencyExchangeDetails() throws Exception {
        System.out.println("saveTaxCurrencyExchangeDetail");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(CurrencyContants.APPLYDATE, authHandler.getDateOnlyFormat().format(new Date()));
        requestParams.put(CurrencyContants.TODATE, authHandler.getDateOnlyFormat().format(new Date()));
        requestParams.put(CurrencyContants.ID, ERID);
        requestParams.put(CurrencyContants.EXCHANGERATE, 1);
        requestParams.put(Constants.companyKey, COMPANY_ID);
        requestParams.put("requestcontextutilsobj", requestcontextutilsobj);
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        instance.saveTaxCurrencyExchangeDetail(requestParams);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of getTaxCurrencyExchangeJson method, of class
     * AccTaxCurrencyExchangeService.
     */
    @Test
    public void testGetTaxCurrencyExchangeJson() throws Exception {
        System.out.println("getTaxCurrencyExchangeJson");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(CurrencyContants.TRANSACTIONDATE, authHandler.getDateOnlyFormat().format(new Date()));
        requestParams.put(CurrencyContants.COMPANYID, COMPANY_ID);
        requestParams.put(CurrencyContants.FROMCURRENCYID, JUnitConstants.CURRENCY_ID);
        requestParams.put(CurrencyContants.TOCURRENCYID, JUnitConstants.CURRENCY_ID);
        requestParams.put("iscurrencyexchangewindow", "true");
            
        boolean isOnlyBaceCurrencyflag = false;
        KwlReturnObject result = accTaxCurExchangeDAOObj.getTaxCurrencyExchange(requestParams);
        List list = result.getEntityList();
        JSONArray result1 = instance.getTaxCurrencyExchangeJson(requestParams, list, isOnlyBaceCurrencyflag);
        if (result1 != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetTaxCurrencyExchangeJson");
        }
    }

    /**
     * Test of getTaxCurrencyExchangeListJson method, of class
     * AccTaxCurrencyExchangeService.
     */
    @Test
    public void testGetTaxCurrencyExchangeListJson() throws Exception {
        System.out.println("getTaxCurrencyExchangeListJson");
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        requestParams.put(CurrencyContants.COMPANYID, COMPANY_ID);
        requestParams.put(CurrencyContants.ERID, ERID);

        KwlReturnObject result = accTaxCurExchangeDAOObj.getTaxExchangeRateDetails(requestParams, true);
        List list = result.getEntityList();

        JSONArray result1 = instance.getTaxCurrencyExchangeListJson(requestParams, list);
        if (result1 != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetTaxCurrencyExchangeListJson");
        }
    }
}
