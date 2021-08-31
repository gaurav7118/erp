/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.booking;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public interface BookingDAO {

    public void saveOrUpdateObject(Object object) throws ServiceException;

    public StockBooking getStockBooking(String bookingId) throws ServiceException;

    public List<StockBooking> getPendingStockBookingList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;
    
    public List<StockBooking> getReservedStockList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;
    
    public List<StockBooking> getBookingHistoryList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;
}
