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
 * @author krawler-user
 */
public class InvoiceDetail implements Comparable<InvoiceDetail> {

    private String ID;
    private int srno;
    private Discount discount;
    private Inventory inventory;
    private Invoice invoice;
    /*
     * isJobWorkOutRemain,jobworkId,interstoretransferId are used if sales invoice is creating from Aged order work report.
     */
    private  String jobworkId;
    private boolean isjobWorkWitoutGrn;
    private String interstoretransferId;
    private double rate;
    private double rateincludegst;
    private double partamount;
    private double partialDiscount;
    private Tax tax;
    private Company company;
    private SalesOrderDetail salesorderdetail;
    private DeliveryOrderDetail deliveryOrderDetail;
    private QuotationDetail quotationDetail;
    private double rowTaxAmount;
    private double rowTaxAmountInBase;
    private double rowTermTaxAmount;
    private double rowTermTaxAmountInBase;
    private double rowExcludingGstAmount; 
    private double rowExcludingGstAmountInBase;
    private double rowTermAmount;
    private double OtherTermNonTaxableAmount;
    private boolean wasRowTaxFieldEditable;// this field has been added after making row tax field editable its value will be set always true from its implementation. i.e from 28-jan-2014.REASON -  when Row Tax Amount field was not editable then tax calculation was taking place according to tax percent, as selected From Tax combo in JS Side.
    private String description;
    private String invstoreid;
    private String invlocid;
    private String deferredJeDetailId;
    private String dependentType;
    private String inouttime;
    private String showquantity;
    private String priceSource;
    private double gstCurrencyRate;//Only used if the country is Singapore and the base currency is not SGD.
    JournalEntryDetail salesJED; // To map invoice detail to related JED 
    JournalEntryDetail gstJED; // To map GST to related JED
    private  double mrpIndia;
    private  String exciseValuationType;
    private  String vatValuationType;
    private  UnitOfMeasure reportingUOMExcise;
    private  UnitOfMeasure reportingUOMVAT;
    private  UOMschemaType reportingSchemaTypeExcise;
    private  UOMschemaType reportingSchemaVAT;
    private double lineLevelTermAmount;
    private boolean istimeinterval;
    private boolean jobOrderItem;
    private String jobOrderItemNumber;
    private String discountJson;                                                //Used to store json of discount masters applied in Price band screen for each row ERM-68
    private String pricingBandMasterid;
    private boolean isUserModifiedTaxAmount;//ERM-1085 - To identify row tax amount is user modified or system calculated according to adaptive rounding algo.

    @Override
    /**
     * Overriding compareTo method to provide implementation Sorting objects
     * according to srno in ascending order.
     *
     * @param inv
     */
    public int compareTo(InvoiceDetail inv) {
        int srNo = inv.getSrno();
        if (this.srno == srNo) {
            return 0;
        } else if (this.srno > srNo) {
            return 1;
        } else {
            return -1;
        }
    }

    public String getDiscountJson() {
        return discountJson;
    }

    public void setDiscountJson(String discountJson) {
        this.discountJson = discountJson;
    }

    public SalesOrderDetail getSalesorderdetail() {
        return salesorderdetail;
    }

    public void setSalesorderdetail(SalesOrderDetail salesorderdetail) {
        this.salesorderdetail = salesorderdetail;
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

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
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

    public double getPartamount() {
        return partamount;
    }

    public void setPartamount(double partamount) {
        this.partamount = partamount;
    }

    public double getPartialDiscount() {
        return partialDiscount;
    }

    public void setPartialDiscount(double partialDiscount) {
        this.partialDiscount = partialDiscount;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public DeliveryOrderDetail getDeliveryOrderDetail() {
        return deliveryOrderDetail;
    }

    public void setDeliveryOrderDetail(DeliveryOrderDetail deliveryOrderDetail) {
        this.deliveryOrderDetail = deliveryOrderDetail;
    }

    public QuotationDetail getQuotationDetail() {
        return quotationDetail;
    }

    public void setQuotationDetail(QuotationDetail quotationDetail) {
        this.quotationDetail = quotationDetail;
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

    public String getDescription() {
        return description;
    }

    public String getInvDetailDescription() {
        return getDescription();
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getDeferredJeDetailId() {
        return deferredJeDetailId;
    }

    public void setDeferredJeDetailId(String deferredJeDetailId) {
        this.deferredJeDetailId = deferredJeDetailId;
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

    public String getPriceSource() {
        return priceSource;
    }
   
    public void setPriceSource(String priceSource) {
        this.priceSource = priceSource;
    }
    
    public double getGstCurrencyRate() {
        return gstCurrencyRate;
    }

    public void setGstCurrencyRate(double gstCurrencyRate) {
        this.gstCurrencyRate = gstCurrencyRate;
    }

    public JournalEntryDetail getGstJED() {
        return gstJED;
    }

    public void setGstJED(JournalEntryDetail gstJED) {
        this.gstJED = gstJED;
    }

    public JournalEntryDetail getSalesJED() {
        return salesJED;
    }

    public void setSalesJED(JournalEntryDetail salesJED) {
        this.salesJED = salesJED;
    }
    public double getRowTermAmount() {
        return rowTermAmount;
    }
    public void setRowTermAmount(double rowTermAmount) {
        this.rowTermAmount = rowTermAmount;
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

    public double getLineLevelTermAmount() {
        return lineLevelTermAmount;
    }

    public void setLineLevelTermAmount(double lineLevelTermAmount) {
        this.lineLevelTermAmount = lineLevelTermAmount;
    }

    public double getRowTaxAmountInBase() {
        return rowTaxAmountInBase;
    }

    public void setRowTaxAmountInBase(double rowTaxAmountInBase) {
        this.rowTaxAmountInBase = rowTaxAmountInBase;
    }

    public double getRowExcludingGstAmount() {
        return rowExcludingGstAmount;
    }

    public void setRowExcludingGstAmount(double rowExcludingGstAmount) {
        this.rowExcludingGstAmount = rowExcludingGstAmount;
    }

    public double getRowExcludingGstAmountInBase() {
        return rowExcludingGstAmountInBase;
    }

    public void setRowExcludingGstAmountInBase(double rowExcludingGstAmountInBase) {
        this.rowExcludingGstAmountInBase = rowExcludingGstAmountInBase;
    }
    public boolean isIstimeinterval() {
        return istimeinterval;
}

    public void setIstimeinterval(boolean istimeinterval) {
        this.istimeinterval = istimeinterval;
    }
    public String getJobOrderItemNumber() {
        return jobOrderItemNumber;
    }

    public void setJobOrderItemNumber(String jobOrderItemNumber) {
        this.jobOrderItemNumber = jobOrderItemNumber;
    }

    public boolean isJobOrderItem() {
        return jobOrderItem;
    }

    public void setJobOrderItem(boolean jobOrderItem) {
        this.jobOrderItem = jobOrderItem;
    }

    public double getRowTermTaxAmount() {
        return rowTermTaxAmount;
    }

    public void setRowTermTaxAmount(double rowTermTaxAmount) {
        this.rowTermTaxAmount = rowTermTaxAmount;
    }

    public double getRowTermTaxAmountInBase() {
        return rowTermTaxAmountInBase;
    }

    public void setRowTermTaxAmountInBase(double rowTermTaxAmountInBase) {
        this.rowTermTaxAmountInBase = rowTermTaxAmountInBase;
    }

    /**
     * @return the jobworkId
     */
    public String getJobworkId() {
        return jobworkId;
    }

    /**
     * @param jobworkId the jobworkId to set
     */
    public void setJobworkId(String jobworkId) {
        this.jobworkId = jobworkId;
    }

    /**
     * @return the isjobWorkWitoutGrn
     */
    public boolean isIsjobWorkWitoutGrn() {
        return isjobWorkWitoutGrn;
    }

    /**
     * @param isjobWorkWitoutGrn the isjobWorkWitoutGrn to set
     */
    public void setIsjobWorkWitoutGrn(boolean isjobWorkWitoutGrn) {
        this.isjobWorkWitoutGrn = isjobWorkWitoutGrn;
    }

    /**
     * @return the interstoretransferId
     */
    public String getInterstoretransferId() {
        return interstoretransferId;
    }

    /**
     * @param interstoretransferId the interstoretransferId to set
     */
    public void setInterstoretransferId(String interstoretransferId) {
        this.interstoretransferId = interstoretransferId;
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
