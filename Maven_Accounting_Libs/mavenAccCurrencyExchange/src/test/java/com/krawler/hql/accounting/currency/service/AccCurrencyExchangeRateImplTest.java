/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting.currency.service;

import com.krawler.common.util.JUnitConstants;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;
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
public class AccCurrencyExchangeRateImplTest {

    public AccCurrencyExchangeRateImplTest() {
    }
    @Autowired
    private AccCurrencyExchangeRate instance;
    
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
     * Test of getUpdatedExchangeRates method, of class AccCurrencyExchangeRate.
     */
    @Test
    public void testGetUpdatedExchangeRates() throws Exception {
        System.out.println("getUpdatedExchangeRates");
        Map<String, Object> requestMap=new HashMap<>();
        requestMap.put("baseCurrency", JUnitConstants.CURRENCY_CODE);
        JSONObject result = instance.getUpdatedExchangeRates(requestMap);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetUpdatedExchangeRates");
        }
    }
}
