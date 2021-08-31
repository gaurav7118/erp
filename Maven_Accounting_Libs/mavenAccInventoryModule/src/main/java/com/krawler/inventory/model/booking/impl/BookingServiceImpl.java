/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.booking.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.booking.*;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public class BookingServiceImpl implements BookingService{
    
    private BookingDAO bookingDAO;

    public void setBookingDAO(BookingDAO bookingDAO) {
        this.bookingDAO = bookingDAO;
    }

    @Override
    public void addBookingRequest(User requestor, StockBooking stockBooking) throws ServiceException {
        if(isInvalidStockBooking(stockBooking)){
            throw new BookingException("Invalid booking");
        }
        stockBooking.setBookingStatus(BookingStatus.PENDING);
        stockBooking.setRequestedBy(requestor);
        stockBooking.setUpdatedBy(requestor);
        bookingDAO.saveOrUpdateObject(stockBooking);
    }

    @Override
    public void approveBookingRequest(User approver, StockBooking stockBooking) throws ServiceException {
        if(isInvalidStockBooking(stockBooking)){
            throw new BookingException("Invalid booking");
        }
        if(stockBooking.getStockBookingDetails().isEmpty()){
            throw new BookingException("booking detail is empty");
        }
        stockBooking.setUpdatedBy(approver);
        stockBooking.setUpdatedOn(new Date());
        bookingDAO.saveOrUpdateObject(stockBooking);
        
    }

    @Override
    public void rejectBookingRequest(User approver, StockBooking stockBooking) throws ServiceException {
        if(isInvalidStockBooking(stockBooking)){
            throw new BookingException("Invalid booking");
        }
        stockBooking.setBookingStatus(BookingStatus.REJECTED);
        stockBooking.setUpdatedBy(approver);
        stockBooking.setUpdatedOn(new Date());
        bookingDAO.saveOrUpdateObject(stockBooking);
    }

    @Override
    public StockBooking getStockBooking(String bookingId)  throws ServiceException{
        StockBooking stockBooking = null;
        if(!StringUtil.isNullOrEmpty(bookingId)){
            stockBooking = bookingDAO.getStockBooking(bookingId);
        }
        return stockBooking;
    }

    @Override
    public List<StockBooking> getPendingStockBookingList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        return bookingDAO.getPendingStockBookingList(company, fromDate, toDate, searchString, paging);
    }

    @Override
    public List<StockBooking> getReservedStockList(Company company, Date fromDate, Date toDate, String searchString, Paging paging)  throws ServiceException{
        return bookingDAO.getReservedStockList(company,  fromDate, toDate, searchString, paging);
    }

    @Override
    public List<StockBooking> getBookingHistoryList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        return bookingDAO.getBookingHistoryList(company, fromDate, toDate, searchString, paging);
    }

    private boolean isInvalidStockBooking(StockBooking stockBooking){
        boolean invalid = false;
        if(stockBooking == null){
            invalid = true;
        }else if(stockBooking.getProduct() == null){
            invalid = true;
        }else if(stockBooking.getBookingQuantity() < 0){
            invalid = true;
        }else if(stockBooking.getFromDate() == null){
            invalid = true;
        }else if(stockBooking.getToDate() == null){
            invalid = true;
        }
        return invalid;
    }

    @Override
    public Map<Product, Double> getAvailableQuantity(Company company, Date fromDate, Date toDate) throws ServiceException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
