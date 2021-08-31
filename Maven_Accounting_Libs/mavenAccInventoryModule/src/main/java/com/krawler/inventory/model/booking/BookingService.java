/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.booking;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public interface BookingService {

    public void addBookingRequest(User requestor, StockBooking stockBooking) throws ServiceException;

    public void approveBookingRequest(User approver, StockBooking stockBooking) throws ServiceException;

    public void rejectBookingRequest(User approver, StockBooking stockBooking) throws ServiceException;

    public StockBooking getStockBooking(String bookingId) throws ServiceException;

    public List<StockBooking> getPendingStockBookingList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;
    
    public Map<Product, Double> getAvailableQuantity(Company company, Date fromDate, Date toDate) throws ServiceException;
    
    public List<StockBooking> getReservedStockList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;

    public List<StockBooking> getBookingHistoryList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;
}
