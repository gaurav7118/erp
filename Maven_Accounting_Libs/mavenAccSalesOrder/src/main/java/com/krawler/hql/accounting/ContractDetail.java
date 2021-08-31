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
public class ContractDetail {

    private String ID;
    private int srno;
    private Contract contract;
    private Product product;
    private double quantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomrate;
    private double rate;
    private double unitPricePerInvoice;
    private String remark;
    private Company company;
    private Tax tax;
    private String description;
    private double discount;
    private int discountispercent;
    private double rowTaxAmount;
    private double rowTermAmount;    
    private ContractDetailCustomData contractdetailcustomdata;

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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public UnitOfMeasure getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    public double getBaseuomquantity() {
        return baseuomquantity;
    }

    public void setBaseuomquantity(double baseuomquantity) {
        this.baseuomquantity = baseuomquantity;
    }

    public double getBaseuomrate() {
        return baseuomrate;
    }

    public void setBaseuomrate(double baseuomrate) {
        this.baseuomrate = baseuomrate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public int getDiscountispercent() {
        return discountispercent;
    }

    public void setDiscountispercent(int discountispercent) {
        this.discountispercent = discountispercent;
    }

    public double getUnitPricePerInvoice() {
        return unitPricePerInvoice;
    }

    public void setUnitPricePerInvoice(double unitPricePerInvoice) {
        this.unitPricePerInvoice = unitPricePerInvoice;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }
    public double getRowTermAmount() {
        return rowTermAmount;
    }

    public void setRowTermAmount(double rowTermAmount) {
        this.rowTermAmount = rowTermAmount;
    }
    public ContractDetailCustomData getContractdetailcustomdata() {
        return contractdetailcustomdata;
    }

    public void setContractdetailcustomdata(ContractDetailCustomData contractdetailcustomdata) {
        this.contractdetailcustomdata = contractdetailcustomdata;
    }
    
}
