/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.authHandler;

import com.krawler.common.util.JUnitConstants;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import org.junit.AfterClass;
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
@ContextConfiguration(locations = "classpath:config/applicationContextList.xml")

public class authHandlerDAOImplTest {
    private final String SUCCESS = "Success";
    public authHandlerDAOImplTest() {
    }

    @Autowired
    private authHandlerDAO instance;
    
    @BeforeClass
    public static void setUpClass() {
//        System.setProperty("com.krawler.config.location", "/home/krawler/NetBeansProjects/FIN-Junit/Financials/deskera/config");
    }

    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of verifyLogin method, of class authHandlerDAOImpl.
     */
    @Test
    public void testVerifyLogin() throws Exception {
        System.out.println("verifyLogin");
        HashMap<String, Object> requestParams = new HashMap<String,Object>();
        requestParams.put("subdomain", JUnitConstants.SUBDOMAIN);
        requestParams.put("user", "admin");
        requestParams.put("pass", "7110eda4d09e062aa5e4a390b0a572ac0d2c0220");
        KwlReturnObject result = instance.verifyLogin(requestParams);
        if(result != null && result.getEntityList()!=null){
            assertEquals(result.getMsg(), SUCCESS);
        }
        else{
            fail("The object returned is null");
        }
    }

}
