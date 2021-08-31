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

/**
 *
 * @author krawler
 */
public class ExpensePODetail {
    private String ID;
    private int srno;
    private double rate;
    private double amount;
    private String description;
    private boolean isdebit;
    private double rateIncludingGst;
    private double rowTaxAmount;
    private Company company;    
    private PurchaseOrder purchaseOrder;
    private Tax tax;
    private Account account;
    private Discount discount;
    private double balAmount;
    private boolean isUserModifiedTaxAmount;//ERM-1085 - To identify row tax amount is user modified or system calculated according to adaptive rounding algo.

    public double getBalAmount() {
        return balAmount;
    }

    public void setBalAmount(double balAmount) {
        this.balAmount = balAmount;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public boolean isIsdebit() {
        return isdebit;
    }

    public void setIsdebit(boolean isdebit) {
        this.isdebit = isdebit;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getRateIncludingGst() {
        return rateIncludingGst;
    }

    public void setRateIncludingGst(double rateIncludingGst) {
        this.rateIncludingGst = rateIncludingGst;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public boolean isIsUserModifiedTaxAmount() {
        return isUserModifiedTaxAmount;
    }

    public void setIsUserModifiedTaxAmount(boolean isUserModifiedTaxAmount) {
        this.isUserModifiedTaxAmount = isUserModifiedTaxAmount;
    }
   
}
