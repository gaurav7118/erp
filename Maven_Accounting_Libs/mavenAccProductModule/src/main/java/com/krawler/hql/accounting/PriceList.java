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
import com.krawler.common.admin.KWLCurrency;
import java.util.Date;

/**
 *
 * @author krawler-user
 */
public class PriceList {

    private String ID;
    private Product product;
    private Date applyDate;
    private boolean carryIn;
    private double price;
    private Company company;
    private String affecteduser;
    private KWLCurrency currency;
    private UnitOfMeasure uomid;
    private boolean initialPrice;

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

    public boolean isCarryIn() {
        return carryIn;
    }

    public void setCarryIn(boolean carryIn) {
        this.carryIn = carryIn;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getAffecteduser() {
        return affecteduser;
    }

    public void setAffecteduser(String affecteduser) {
        this.affecteduser = affecteduser;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public UnitOfMeasure getUomid() {
        return uomid;
    }

    public void setUomid(UnitOfMeasure uomid) {
        this.uomid = uomid;
    }

    public boolean isInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(boolean initialPrice) {
        this.initialPrice = initialPrice;
    }
}
