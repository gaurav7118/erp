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

import java.util.Date;

/**
 *
 * @author krawler
 */
public class DefaultTDSRate {
    
    private String id;
    private String natureOfPayment;
    private String defaultMasterDeducteetype;
    private String residentialStatus;
    private double rate;
    private Date fromDate;
    private Date toDate;
    private double basicExemptionPerTransaction; 
    private double basicexEmptionPerAnnum; 

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNatureOfPayment() {
        return natureOfPayment;
    }

    public void setNatureOfPayment(String natureOfPayment) {
        this.natureOfPayment = natureOfPayment;
    }

    public String getDefaultMasterDeducteetype() {
        return defaultMasterDeducteetype;
    }

    public void setDefaultMasterDeducteetype(String defaultMasterDeducteetype) {
        this.defaultMasterDeducteetype = defaultMasterDeducteetype;
    }

    public String getResidentialStatus() {
        return residentialStatus;
    }

    public void setResidentialStatus(String residentialStatus) {
        this.residentialStatus = residentialStatus;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public double getBasicExemptionPerTransaction() {
        return basicExemptionPerTransaction;
    }

    public void setBasicExemptionPerTransaction(double basicExemptionPerTransaction) {
        this.basicExemptionPerTransaction = basicExemptionPerTransaction;
    }

    public double getBasicexEmptionPerAnnum() {
        return basicexEmptionPerAnnum;
    }

    public void setBasicexEmptionPerAnnum(double basicexEmptionPerAnnum) {
        this.basicexEmptionPerAnnum = basicexEmptionPerAnnum;
    }
    
    
    
}
