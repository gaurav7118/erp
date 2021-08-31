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
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author krawler
 */
public class BillingGoodsReceiptDetail {

    private String ID;
    private int srno;
    private String productDetail;
    private double rate;
    private double quantity;
    private double amount;
    private Discount discount;
    private BillingGoodsReceipt billingGoodsReceipt;
    private Company company;
    private BillingPurchaseOrderDetail purchaseOrderDetail;
    private Tax tax;
    private double rowTaxAmount;
    private boolean wasRowTaxFieldEditable;// this field has been added after making row tax field editable its value will be set always true from its implementation. i.e from 28-jan-2014.REASON -  when Row Tax Amount field was not editable then tax calculation was taking place according to tax percent, as selected From Tax combo in JS Side.
    private JournalEntryDetail debtorEntry;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
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

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public BillingGoodsReceipt getBillingGoodsReceipt() {
        return billingGoodsReceipt;
    }

    public void setBillingGoodsReceipt(BillingGoodsReceipt billingGoodsReceipt) {
        this.billingGoodsReceipt = billingGoodsReceipt;
    }

    public String getProductDetail() {
        return productDetail;
    }

    public void setProductDetail(String productDetail) {
        this.productDetail = productDetail;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public BillingPurchaseOrderDetail getPurchaseOrderDetail() {
        return purchaseOrderDetail;
    }

    public void setPurchaseOrderDetail(BillingPurchaseOrderDetail purchaseOrderDetail) {
        this.purchaseOrderDetail = purchaseOrderDetail;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }

    public boolean isWasRowTaxFieldEditable() {
        return wasRowTaxFieldEditable;
    }

    public void setWasRowTaxFieldEditable(boolean wasRowTaxFieldEditable) {
        this.wasRowTaxFieldEditable = wasRowTaxFieldEditable;
    }

    public JournalEntryDetail getDebtorEntry() {
        return debtorEntry;
    }

    public void setDebtorEntry(JournalEntryDetail debtorEntry) {
        this.debtorEntry = debtorEntry;
    }
}
