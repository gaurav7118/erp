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
 * @author Neeraj
 *
 */
public class QuotationDetail {

    private String ID;
    private int srno;
    private Quotation quotation;
    private Product product;
    private double quantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomrate;
    private double rate;
    private double rateincludegst;
    private String remark;
    private Company company;
    private Tax tax;
    private double discount;
    private int discountispercent;
    private String description;
    private double rowTaxAmount;
    private ProductReplacementDetail productReplacementDetail;
    private QuotationDetailCustomData quotationDetailCustomData;
    private QuotationDetailsProductCustomData quotationDetailProductCustomData;
    private String dependentType;
    private String inouttime;
    private String showquantity;
    private String vendorquotationdetails; 
    private String invstoreid;
    private String invlocid;
    private String priceSource;
    private double rowtermamount;
    private double OtherTermNonTaxableAmount;
    private double mrpIndia;
    private String exciseValuationType;
    private String vatValuationType;
    private UnitOfMeasure reportingUOMExcise;
    private UnitOfMeasure reportingUOMVAT;
    private UOMschemaType reportingSchemaTypeExcise;
    private UOMschemaType reportingSchemaVAT;
    private String discountJson;                                                //Used to store json of discount masters applied in Price band screen for each row ERM-68;
    private String pricingBandMasterid;
    private boolean isUserModifiedTaxAmount;//ERM-1085 - To identify row tax amount is user modified or system calculated according to adaptive rounding algo.

    public String getDiscountJson() {
        return discountJson;
    }

    public void setDiscountJson(String discountJson) {
        this.discountJson = discountJson;
    }
    
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

    public Quotation getQuotation() {
        return quotation;
    }

    public void setQuotation(Quotation quotation) {
        this.quotation = quotation;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getRateincludegst() {
        return rateincludegst;
    }

    public void setRateincludegst(double rateincludegst) {
        this.rateincludegst = rateincludegst;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }
    public ProductReplacementDetail getProductReplacementDetail() {
        return productReplacementDetail;
    }

    public void setProductReplacementDetail(ProductReplacementDetail productReplacementDetail) {
        this.productReplacementDetail = productReplacementDetail;
    }

    public QuotationDetailCustomData getQuotationDetailCustomData() {
        return quotationDetailCustomData;
    }

    public void setQuotationDetailCustomData(QuotationDetailCustomData quotationDetailCustomData) {
        this.quotationDetailCustomData = quotationDetailCustomData;
    }

    public QuotationDetailsProductCustomData getQuotationDetailProductCustomData() {
        return quotationDetailProductCustomData;
    }

    public void setQuotationDetailProductCustomData(QuotationDetailsProductCustomData quotationDetailProductCustomData) {
        this.quotationDetailProductCustomData = quotationDetailProductCustomData;
    }

    
    public String getDependentType() {
        return dependentType;
    }

    public void setDependentType(String dependentType) {
        this.dependentType = dependentType;
    }

    public String getInouttime() {
        return inouttime;
    }

    public void setInouttime(String inouttime) {
        this.inouttime = inouttime;
    }

    public String getShowquantity() {
        return showquantity;
    }

    public void setShowquantity(String showquantity) {
        this.showquantity = showquantity;
    }

    public String getVendorquotationdetails() {
        return vendorquotationdetails;
    }

    public void setVendorquotationdetails(String vendorquotationdetails) {
        this.vendorquotationdetails = vendorquotationdetails;
    }
         public String getInvstoreid() {
        return invstoreid;
    }

    public void setInvstoreid(String invstoreid) {
        this.invstoreid = invstoreid;
    }

    public String getInvlocid() {
        return invlocid;
    }

    public void setInvlocid(String invlocid) {
        this.invlocid = invlocid;
    }

    public String getPriceSource() {
        return priceSource;
    }

    public void setPriceSource(String priceSource) {
        this.priceSource = priceSource;
    }

    public double getRowtermamount() {
        return rowtermamount;
    }

    public void setRowtermamount(double rowtermamount) {
        this.rowtermamount = rowtermamount;
    }

    public double getOtherTermNonTaxableAmount() {
        return OtherTermNonTaxableAmount;
    }

    public void setOtherTermNonTaxableAmount(double OtherTermNonTaxableAmount) {
        this.OtherTermNonTaxableAmount = OtherTermNonTaxableAmount;
    }

    public String getExciseValuationType() {
        return exciseValuationType;
    }

    public void setExciseValuationType(String exciseValuationType) {
        this.exciseValuationType = exciseValuationType;
    }

    public double getMrpIndia() {
        return mrpIndia;
    }

    public void setMrpIndia(double mrpIndia) {
        this.mrpIndia = mrpIndia;
    }

    public UOMschemaType getReportingSchemaTypeExcise() {
        return reportingSchemaTypeExcise;
    }

    public void setReportingSchemaTypeExcise(UOMschemaType reportingSchemaTypeExcise) {
        this.reportingSchemaTypeExcise = reportingSchemaTypeExcise;
    }

    public UOMschemaType getReportingSchemaVAT() {
        return reportingSchemaVAT;
    }

    public void setReportingSchemaVAT(UOMschemaType reportingSchemaVAT) {
        this.reportingSchemaVAT = reportingSchemaVAT;
    }

    public UnitOfMeasure getReportingUOMExcise() {
        return reportingUOMExcise;
    }

    public void setReportingUOMExcise(UnitOfMeasure reportingUOMExcise) {
        this.reportingUOMExcise = reportingUOMExcise;
    }

    public UnitOfMeasure getReportingUOMVAT() {
        return reportingUOMVAT;
    }

    public void setReportingUOMVAT(UnitOfMeasure reportingUOMVAT) {
        this.reportingUOMVAT = reportingUOMVAT;
    }

    public String getVatValuationType() {
        return vatValuationType;
    }

    public void setVatValuationType(String vatValuationType) {
        this.vatValuationType = vatValuationType;
    }

    public String getPricingBandMasterid() {
        return pricingBandMasterid;
    }

    public void setPricingBandMasterid(String pricingBandMasterid) {
        this.pricingBandMasterid = pricingBandMasterid;
    }
    
    public boolean isIsUserModifiedTaxAmount() {
        return isUserModifiedTaxAmount;
    }

    public void setIsUserModifiedTaxAmount(boolean isUserModifiedTaxAmount) {
        this.isUserModifiedTaxAmount = isUserModifiedTaxAmount;
    }
    
}
