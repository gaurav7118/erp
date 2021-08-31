/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.booking;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.hql.accounting.Product;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public class StockBooking {
    
    private String id;
    private String bookingFor;
    private String referenceNo;
    private Product product;
    private double requestedQuantity;
    private double bookingQuantity;
    private Date fromDate;
    private Date toDate;
    private User requestedBy;
    private Date requestedOn;
    private BookingStatus bookingStatus;
    private User updatedBy;
    private Date updatedOn;
    private Set<StockBookingDetail> stockBookingDetails;
    private Company company;

    public StockBooking() {
        requestedOn = new Date();
        updatedOn = new Date();
        bookingStatus = BookingStatus.PENDING;
        stockBookingDetails = new HashSet<StockBookingDetail>();
    }

    public StockBooking(String referenceNo, String bookingFor, Product product, double requestedQuantity, Date fromDate, Date toDate, User requestedBy) {
        this();
        this.referenceNo = referenceNo;
        this.bookingFor = bookingFor;
        this.product = product;
        this.company = product.getCompany();
        this.requestedQuantity = requestedQuantity;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.requestedBy = requestedBy;
    }

    
    public String getBookingFor() {
        return bookingFor;
    }

    public void setBookingFor(String bookingFor) {
        this.bookingFor = bookingFor;
    }

    public double getBookingQuantity() {
        return bookingQuantity;
    }

    public void setBookingQuantity(double bookingQuantity) {
        this.bookingQuantity = bookingQuantity;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(User requestedBy) {
        this.requestedBy = requestedBy;
    }

    public Date getRequestedOn() {
        return requestedOn;
    }

    public void setRequestedOn(Date requestedOn) {
        this.requestedOn = requestedOn;
    }

    public double getRequestedQuantity() {
        return requestedQuantity;
    }

    public void setRequestedQuantity(double requestedQuantity) {
        this.requestedQuantity = requestedQuantity;
    }

    public Set<StockBookingDetail> getStockBookingDetails() {
        return stockBookingDetails;
    }

    public void setStockBookingDetails(Set<StockBookingDetail> stockBookingDetails) {
        this.stockBookingDetails = stockBookingDetails;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StockBooking other = (StockBooking) obj;
        if ((this.id == null) ? (other.id != null) : !this.id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }
    
}
