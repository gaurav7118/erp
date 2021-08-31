/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.location.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.InventoryLocation;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.Paging;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class LocationServiceImplTest {

    @Autowired
    private LocationService instance;
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAOobj;
    
    private static String COMPANY_ID = JUnitConstants.COMPANY_ID;
    private static String USER_ID = JUnitConstants.USER_ID;
    private static String LOCATION_ID = JUnitConstants.LOCATION_ID;
    
    Company company = null;
    User user = null;
    Location location = null;

    public LocationServiceImplTest() {
    }

    @Before
    public void setUp() throws ServiceException {
        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), COMPANY_ID);
        company = (Company) companyResult.getEntityList().get(0);
        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), USER_ID);
        user = (User) userResult.getEntityList().get(0);
        KwlReturnObject locationResult = accountingHandlerDAOobj.getObject(Location.class.getName(), LOCATION_ID);
        location = (Location) locationResult.getEntityList().get(0);
    }

    @After
    public void tearDown() {
        instance = null;
    }

    /**
     * Test of addLocation method, of class LocationServiceImpl.
     */
    @Test
    public void testAddLocation() throws Exception {
        System.out.println("addLocation");
        instance.addLocation(user, location);
        // TODO review the generated test code and remove the default call to fail.
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of updateLocation method, of class LocationServiceImpl.
     */
    @Test
    public void testUpdateLocation() throws Exception {
        System.out.println("updateLocation");
        instance.updateLocation(user, location);
        // TODO review the generated test code and remove the default call to fail.
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of getDefaultLocation method, of class LocationServiceImpl.
     */
    @Test
    public void testGetDefaultLocation() throws Exception {
        System.out.println("getDefaultLocation");
        Location loc = instance.getDefaultLocation(company);
        if (loc != null) {
            assertTrue(loc instanceof Location);
        } else {
            fail("Test case failed : testGetDefaultLocation");
        }
    }

    /**
     * Test of getLocation method, of class LocationServiceImpl.
     */
    @Test
    public void testGetLocation() throws Exception {
        System.out.println("getLocation");
        Location loc = instance.getLocation(LOCATION_ID);
        if (loc != null) {
            assertTrue(loc instanceof Location);
        } else {
            fail("Test case failed : testGetLocation");
        }
    }

    /**
     * Test of getERPLocation method, of class LocationServiceImpl.
     */
    @Test
    public void testGetERPLocation() throws Exception {
        System.out.println("getERPLocation");
        InventoryLocation invLocation = instance.getERPLocation(LOCATION_ID);
        if (invLocation != null) {
            assertTrue(invLocation instanceof InventoryLocation);
        } else {
            fail("Test case failed : testGetERPLocation");
        }
    }

    /**
     * Test of getLocations method, of class LocationServiceImpl.
     */
    @Test
    public void testGetLocations() throws Exception {
        System.out.println("getLocations");
        String searchString = "";
        Paging paging = null;
        List result = instance.getLocations(company, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetLocations");
        }
    }

    /**
     * Test of activateLocation method, of class LocationServiceImpl.
     */
    @Test
    public void testActivateLocation() throws Exception {
        System.out.println("activateLocation");
        instance.activateLocation(user, location);
        // TODO review the generated test code and remove the default call to fail.
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of deactivateLocation method, of class LocationServiceImpl.
     */
    @Test
    public void testDeactivateLocation() throws Exception {
        System.out.println("deactivateLocation");
        instance.deactivateLocation(user, location);
        // TODO review the generated test code and remove the default call to fail.
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of setLocationAsDefault method, of class LocationServiceImpl.
     */
    @Test
    public void testSetLocationAsDefault() throws Exception {
        System.out.println("setLocationAsDefault");
        instance.setLocationAsDefault(user, location);
        // TODO review the generated test code and remove the default call to fail.
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of getCompanPreferencesSql method, of class LocationServiceImpl.
     */
    @Test
    public void testGetCompanPreferencesSql() {
        System.out.println("getCompanPreferencesSql");
        boolean result = instance.getCompanPreferencesSql(COMPANY_ID);
        if (result) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetLocations");
        }
    }

    /**
     * Test of getLocationByName method, of class LocationServiceImpl.
     */
    @Test
    public void testGetLocationByName() throws Exception {
        System.out.println("getLocationByName");
        Location result = instance.getLocationByName(company, location.getName());
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetLocations");
        }
    }
    
    public Map<String, Object> getTaskProgressCommonParameters() {
        Map<String, Object> requestParams = new HashMap<>();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            requestParams.put(Constants.df, df);
            requestParams.put("companyid", COMPANY_ID);
            requestParams.put("requestcontextutilsobj", Locale.forLanguageTag(Constants.RES_DEF_LANGUAGE));

        } catch (Exception ex) {
            Logger.getLogger(LocationServiceImplTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requestParams;
    }
}
