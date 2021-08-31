/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.view;

import com.krawler.inventory.model.booking.BookingService;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 *
 * @author Vipin Gupta
 */
public class BookingController extends MultiActionController {

    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    private static final Logger lgr = Logger.getLogger(BookingController.class.getName());
    private HibernateTransactionManager txnManager;
    private String successView;
    private BookingService bookingService;
    private StoreService storeService;
    private LocationService locationService; 
    private AccountingHandlerDAO accountingHandlerDAO;

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setBookingService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    
    

}
