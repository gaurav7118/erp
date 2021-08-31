/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.booking.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.booking.BookingDAO;
import com.krawler.inventory.model.booking.BookingStatus;
import com.krawler.inventory.model.booking.StockBooking;
import java.util.*;

/**
 *
 * @author Vipin Gupta
 */
public class BookingDAOImpl extends BaseDAO implements BookingDAO {

    @Override
    public void saveOrUpdateObject(Object object) throws ServiceException {
        saveOrUpdate(object);
    }

    @Override
    public StockBooking getStockBooking(String bookingId) throws ServiceException {
        return (StockBooking) get(StockBooking.class, bookingId);
    }

    @Override
    public List<StockBooking> getPendingStockBookingList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockBooking WHERE company = ? AND bookingStatus = ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(BookingStatus.PENDING);
        if (fromDate != null && toDate != null) {
            hql.append(" AND ( (DATE(fromDate) >= ? AND DATE(fromDate) <= ?) OR (DATE(toDate) >= ? AND DATE(toDate) <= ?) OR (DATE(fromDate) < ? AND DATE(toDate) > ?)  )");
            params.add(fromDate);
            params.add(toDate);
            params.add(fromDate);
            params.add(toDate);
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (product.productid LIKE ? OR product.name LIKE ? OR referenceNo LIKE ? OR bookingFor LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY fromDate ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalRecord = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalRecord);
            if (paging.isValid() && totalRecord > paging.getLimit()) {
                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            }
        }
        return list;
    }

    @Override
    public List<StockBooking> getReservedStockList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockBooking WHERE company = ? AND bookingStatus = ? AND DATE(toDate) >= DATE(?) ");
        List params = new ArrayList();
        params.add(company);
        params.add(BookingStatus.APPROVED);
        params.add(new Date());
        if (fromDate != null && toDate != null) {
            hql.append(" AND ( (DATE(fromDate) >= ? AND DATE(fromDate) <= ?) OR (DATE(toDate) >= ? AND DATE(toDate) <= ?) OR (DATE(fromDate) < ? AND DATE(toDate) > ?)  )");
            params.add(fromDate);
            params.add(toDate);
            params.add(fromDate);
            params.add(toDate);
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (product.productid LIKE ? OR product.name LIKE ? OR referenceNo LIKE ? OR bookingFor LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY fromDate ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalRecord = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalRecord);
            if (paging.isValid() && totalRecord > paging.getLimit()) {
                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            }
        }
        return list;
    }

    @Override
    public List<StockBooking> getBookingHistoryList(Company company, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockBooking WHERE company = ? AND ( (bookingStatus = ? AND DATE(toDate) < DATE(?)) OR (bookingStatus = ?) )");
        List params = new ArrayList();
        params.add(company);
        params.add(BookingStatus.APPROVED);
        params.add(new Date());
        params.add(BookingStatus.REJECTED);
        if (fromDate != null && toDate != null) {
            hql.append(" AND (  (DATE(fromDate) >= ? AND DATE(fromDate) <= ?) OR (DATE(toDate) >= ? AND DATE(toDate) <= ?) OR (DATE(fromDate) < ? AND DATE(toDate) > ?) )");
            params.add(fromDate);
            params.add(toDate);
            params.add(fromDate);
            params.add(toDate);
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (product.productid LIKE ? OR product.name LIKE ? OR referenceNo LIKE ? OR bookingFor LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY fromDate ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalRecord = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalRecord);
            if (paging.isValid() && totalRecord > paging.getLimit()) {
                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            }
        }
        return list;
    }
}
