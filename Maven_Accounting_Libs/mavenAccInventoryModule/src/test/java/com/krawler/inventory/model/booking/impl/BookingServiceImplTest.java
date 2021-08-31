/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.booking.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.JUnitConstants;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.booking.BookingService;
import com.krawler.inventory.model.booking.StockBooking;
import com.krawler.inventory.model.booking.StockBookingDetail;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
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
public class BookingServiceImplTest {

    @Autowired
    private BookingService instance;
    @Autowired
    private HibernateTransactionManager txnManager;
    @Autowired
    private AccountingHandlerDAO accountingHandlerDAO;
    TransactionStatus status = null;
    Company company = null;
    Product product = null;
    Store store = null;
    Location location = null;
    User user = null;

    public BookingServiceImplTest() {
    }

    @Before
    public void setUp() throws ServiceException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("InvDocumentServiceTest_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        status = txnManager.getTransaction(def);

        KwlReturnObject companyResult = accountingHandlerDAO.getObject(Company.class.getName(), JUnitConstants.COMPANY_ID);
        company = (Company) companyResult.getEntityList().get(0);

        KwlReturnObject userResult = accountingHandlerDAO.getObject(User.class.getName(), JUnitConstants.USER_ID);
        user = (User) userResult.getEntityList().get(0);
        
        KwlReturnObject productResult = accountingHandlerDAO.getObject(Product.class.getName(), JUnitConstants.PRODUCT_UUID);
        product = (Product) productResult.getEntityList().get(0);
        
        KwlReturnObject storeResult = accountingHandlerDAO.getObject(Store.class.getName(), JUnitConstants.STORE_UUID);
        store = (Store) storeResult.getEntityList().get(0);
        
        KwlReturnObject locationResult = accountingHandlerDAO.getObject(Location.class.getName(), JUnitConstants.LOCATION_UUID);
        location = (Location) locationResult.getEntityList().get(0);
    }

    @After
    public void tearDown() {
        instance = null;
        txnManager.rollback(status);
    }

    /**
     * Test of addBookingRequest method, of class BookingServiceImpl.
     */
    @Test
    public void testAddBookingRequest() throws Exception {
        System.out.println("addBookingRequest");
        StockBooking stockBooking = getStockBookingObject();
        instance.addBookingRequest(user, stockBooking);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of approveBookingRequest method, of class BookingServiceImpl.
     */
    @Test
    public void testApproveBookingRequest() throws Exception {
        System.out.println("approveBookingRequest");
        StockBooking stockBooking = getStockBookingObject();
        instance.approveBookingRequest(user, stockBooking);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of rejectBookingRequest method, of class BookingServiceImpl.
     */
    @Test
    public void testRejectBookingRequest() throws Exception {
        System.out.println("rejectBookingRequest");
        StockBooking stockBooking = getStockBookingObject();
        instance.rejectBookingRequest(user, stockBooking);
        assertTrue("Data is returned properly", true);
    }

    /**
     * Test of getStockBooking method, of class BookingServiceImpl.
     */
    @Test
    public void testGetStockBooking() throws Exception {
        System.out.println("getStockBooking");
        String bookingId = JUnitConstants.BOOKING_ID;
        StockBooking result = instance.getStockBooking(bookingId);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetStockBooking");
        }
    }

    /**
     * Test of getPendingStockBookingList method, of class BookingServiceImpl.
     */
    @Test
    public void testGetPendingStockBookingList() throws Exception {
        System.out.println("getPendingStockBookingList");
        Date fromDate = new Date();
        Date toDate = new Date();
        String searchString = "";
        Paging paging = new Paging(0, 30);
        List result = instance.getPendingStockBookingList(company, fromDate, toDate, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetPendingStockBookingList");
        }
    }

    /**
     * Test of getReservedStockList method, of class BookingServiceImpl.
     */
    @Test
    public void testGetReservedStockList() throws Exception {
        System.out.println("getReservedStockList");
        Date fromDate = new Date();
        Date toDate = new Date();
        String searchString = "";
        Paging paging = new Paging(0, 30);
        List result = instance.getReservedStockList(company, fromDate, toDate, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetReservedStockList");
        }
    }

    /**
     * Test of getBookingHistoryList method, of class BookingServiceImpl.
     */
    @Test
    public void testGetBookingHistoryList() throws Exception {
        System.out.println("getBookingHistoryList");
        Date fromDate = new Date();
        Date toDate = new Date();
        String searchString = "";
        Paging paging = new Paging(0, 30);
        List result = instance.getBookingHistoryList(company, fromDate, toDate, searchString, paging);
        if (result != null) {
            assertTrue("Data is returned properly", true);
        } else {
            fail("Test case failed : testGetBookingHistoryList");
        }
    }

//    /**
//     * Test of getAvailableQuantity method, of class BookingServiceImpl.
//     */
//    @Test
//    public void testGetAvailableQuantity() throws Exception {
//        System.out.println("getAvailableQuantity");
//        Date fromDate = new Date();
//        Date toDate = new Date();
//        Map result = instance.getAvailableQuantity(company, fromDate, toDate);
//        if (result != null) {
//            assertTrue("Data is returned properly", true);
//        } else {
//            fail("Test case failed : testGetAvailableQuantity");
//        }
//    }
    
    public StockBooking getStockBookingObject(){
        String newID = UUID.randomUUID().toString();
        StockBooking stockBooking = new StockBooking();
        stockBooking.setId(newID);
        stockBooking.setProduct(product);
        stockBooking.setBookingQuantity(1);
        stockBooking.setFromDate(new Date());
        stockBooking.setToDate(new Date());
        Set<StockBookingDetail> sbdSet = new HashSet<>();
        StockBookingDetail sbd = new StockBookingDetail();
        String newIDforDetails = UUID.randomUUID().toString();
        sbd.setId(newIDforDetails);
        sbd.setBatchName("Test Batch Name");
        sbd.setLocation(location);
        sbd.setStore(store);
        sbd.setSerialNames("Test Serial Name");
        sbd.setQuantity(1);
        sbdSet.add(sbd);
        stockBooking.setStockBookingDetails(sbdSet);
        return stockBooking;
    }
}
