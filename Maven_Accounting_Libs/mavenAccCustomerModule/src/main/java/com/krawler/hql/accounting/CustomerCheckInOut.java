/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */

package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import java.util.Date;

public class CustomerCheckInOut{
    private String Id;
    private Customer customer;
    private Date checkintime;
    private Date checkouttime;
    private String location;
    private String inLatitude;
    private String inLongitude;
    private String outLatitude;
    private String outLongitude;
    private User checkinby;
    private User checkoutby;
    private Company company;

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public User getCheckinby() {
        return checkinby;
    }

    public void setCheckinby(User checkinby) {
        this.checkinby = checkinby;
    }

    public Date getCheckintime() {
        return checkintime;
    }

    public void setCheckintime(Date checkintime) {
        this.checkintime = checkintime;
    }

    public User getCheckoutby() {
        return checkoutby;
    }

    public void setCheckoutby(User checkoutby) {
        this.checkoutby = checkoutby;
    }

    public Date getCheckouttime() {
        return checkouttime;
    }

    public void setCheckouttime(Date checkouttime) {
        this.checkouttime = checkouttime;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getInLatitude() {
        return inLatitude;
}

    public void setInLatitude(String inLatitude) {
        this.inLatitude = inLatitude;
    }

    public String getInLongitude() {
        return inLongitude;
    }

    public void setInLongitude(String inLongitude) {
        this.inLongitude = inLongitude;
    }

    public String getOutLatitude() {
        return outLatitude;
    }

    public void setOutLatitude(String outLatitude) {
        this.outLatitude = outLatitude;
    }

    public String getOutLongitude() {
        return outLongitude;
    }

    public void setOutLongitude(String outLongitude) {
        this.outLongitude = outLongitude;
    }

}