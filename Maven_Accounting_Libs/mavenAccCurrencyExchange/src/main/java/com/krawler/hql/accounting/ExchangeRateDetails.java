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
import java.util.Date;

public class ExchangeRateDetails {

    private String ID;
    private Date applyDate;
    private double exchangeRate;
    private ExchangeRate exchangeratelink;
    private Company company;
    private Date toDate;
    private long exchangeorder;  //Max exchange order indicates latest exchange rate for a day
    private double foreignToBaseExchangeRate;

    public long getExchangeorder() {
        return exchangeorder;
    }

    public void setExchangeorder(long exchangeorder) {
        this.exchangeorder = exchangeorder;
    }
   
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Date getApplyDate() {
        return applyDate;
    }

    public void setApplyDate(Date applyDate) {
        this.applyDate = applyDate;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public ExchangeRate getExchangeratelink() {
        return exchangeratelink;
    }

    public void setExchangeratelink(ExchangeRate exchangeratelink) {
        this.exchangeratelink = exchangeratelink;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public double getForeignToBaseExchangeRate() {
        return foreignToBaseExchangeRate;
    }

    public void setForeignToBaseExchangeRate(double foreignToBaseExchangeRate) {
        this.foreignToBaseExchangeRate = foreignToBaseExchangeRate;
    }
}
