/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.Modules;
import com.krawler.common.admin.User;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class DeliveryPlanner {

    private String ID;
    private Date pushTime;
    private Invoice referenceNumber; // used to Invoice pushed in delivery planner
    private User fromUser;
    private String deliveryLocation;
    private Date deliveryDate;
    private String deliveryTime;
    private String remarksBySales;
    private String printedBy;
    private String remarksByPlanner;
    private MasterItem vehicleNumber;
    private MasterItem driver;
    private MasterItem tripNumber;
    private String tripDescription;
    private Company company;
    private Integer invoiceOccurance;
    private String documentNo; // used to transaction pushed in delivery planner e.g PO
    private int module; // module id of pushed document
    private DeliveryOrder deliveryOrder; // used to Delivery Order pushed in delivery planner
    private SalesReturn salesReturn; // used to Sales Return pushed in delivery planner

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public MasterItem getDriver() {
        return driver;
    }

    public void setDriver(MasterItem driver) {
        this.driver = driver;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User from) {
        this.fromUser = from;
    }

    public String getPrintedBy() {
        return printedBy;
    }

    public void setPrintedBy(String printedBy) {
        this.printedBy = printedBy;
    }

    public Invoice getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(Invoice referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getRemarksByPlanner() {
        return remarksByPlanner;
    }

    public void setRemarksByPlanner(String remarksByPlanner) {
        this.remarksByPlanner = remarksByPlanner;
    }

    public String getRemarksBySales() {
        return remarksBySales;
    }

    public void setRemarksBySales(String remarksBySales) {
        this.remarksBySales = remarksBySales;
    }

    public Date getPushTime() {
        return pushTime;
    }

    public void setPushTime(Date pushTime) {
        this.pushTime = pushTime;
    }

    public String getTripDescription() {
        return tripDescription;
    }

    public void setTripDescription(String tripDescription) {
        this.tripDescription = tripDescription;
    }

    public MasterItem getTripNumber() {
        return tripNumber;
    }

    public void setTripNumber(MasterItem tripNumber) {
        this.tripNumber = tripNumber;
    }

    public MasterItem getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(MasterItem vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Integer getInvoiceOccurance() {
        return invoiceOccurance;
    }

    public void setInvoiceOccurance(Integer invoiceOccurance) {
        this.invoiceOccurance = invoiceOccurance;
    }

    public String getDocumentNo() {
        return documentNo;
    }

    public void setDocumentNo(String purchaseOrder) {
        this.documentNo = purchaseOrder;
    }

    public int getModule() {
        return module;
    }

    public void setModule(int module) {
        this.module = module;
    }

    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
    }

    public void setDeliveryOrder(DeliveryOrder deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }

    public SalesReturn getSalesReturn() {
        return salesReturn;
    }

    public void setSalesReturn(SalesReturn salesReturn) {
        this.salesReturn = salesReturn;
    }
}
