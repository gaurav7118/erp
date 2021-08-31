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
package com.krawler.common.admin;

import java.util.Date;

/**
 *
 * @author krawler
 */
public class PricingBandMasterDetail {

    private String ID;
    private PricingBandMaster pricingBandMaster;
    private KWLCurrency currency;
    private String product;
    private double purchasePrice;
    private double salesPrice;
    private Company company;
    private Date applicableDate;
    private int minimumQty;
    private int maximumQty;
    private String discountType; // 0 - 'Flat', 1 - 'Percentage'
    private double discountValue;
    private boolean useCommonDiscount;

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

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public PricingBandMaster getPricingBandMaster() {
        return pricingBandMaster;
    }

    public void setPricingBandMaster(PricingBandMaster pricingBandMaster) {
        this.pricingBandMaster = pricingBandMaster;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(double salesPrice) {
        this.salesPrice = salesPrice;
    }

    public Date getApplicableDate() {
        return applicableDate;
    }

    public void setApplicableDate(Date applicableDate) {
        this.applicableDate = applicableDate;
    }

    public int getMaximumQty() {
        return maximumQty;
    }

    public void setMaximumQty(int maximumQty) {
        this.maximumQty = maximumQty;
    }

    public int getMinimumQty() {
        return minimumQty;
    }

    public void setMinimumQty(int minimumQty) {
        this.minimumQty = minimumQty;
    }

    public String getDiscountType() {
        return discountType;
    }

    public void setDiscountType(String discountType) {
        this.discountType = discountType;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    public boolean isUseCommonDiscount() {
        return useCommonDiscount;
    }

    public void setUseCommonDiscount(boolean useCommonDiscount) {
        this.useCommonDiscount = useCommonDiscount;
    }
}