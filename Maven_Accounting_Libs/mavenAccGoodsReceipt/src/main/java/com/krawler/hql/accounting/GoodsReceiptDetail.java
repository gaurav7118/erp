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
import com.krawler.common.admin.Projreport_Template;
import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class GoodsReceiptDetail implements Comparable<GoodsReceiptDetail> {

    private String ID;
    private int srno;
    private Discount discount;
    private Inventory inventory;
    private GoodsReceipt goodsReceipt;
    private double rate;
    private double rateincludegst;
    private Company company;
    private Tax tax;
    private PurchaseOrderDetail purchaseorderdetail;
    private String permit;
    private GoodsReceiptOrderDetails goodsReceiptOrderDetails; 
    private Projreport_Template templateid;
    private VendorQuotationDetail vendorQuotationDetail;
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
    private String supplierpartnumber;
    private String dependentType;
    private String inouttime;
    private String showquantity;
    private String priceSource;
    private double gstCurrencyRate;//Only used if the country is Singapore and the base currency is not SGD.
    JournalEntryDetail purchaseJED; // To map GR detail to related JED 
    JournalEntryDetail gstJED; // To map GST to related JED
    private  double mrpIndia;
    private  String exciseValuationType;
    private  String vatValuationType;
    private  UnitOfMeasure reportingUOMExcise;
    private  UnitOfMeasure reportingUOMVAT;
    private  UOMschemaType reportingSchemaTypeExcise;
    private  UOMschemaType reportingSchemaVAT;
    private double lineLevelTermAmount;
    private double tdsAssessableAmount;
    private MasterItem natureOfPayment;
    private int tdsRuleId;
    private double tdsRate;
    private double tdsLineAmount;
    private int tdsPaidFlag;
    private String tdsPayment;
    private int tdsInterestPaidFlag;// To Verify whether TDS Interest is paid or not.
    private String tdsInterestPayment;// If paid then respective TDS Interest Payment id.
    private Account tdsPayableAccount;// Used for INDIA country
    private TdsJEMapping tdsJEMapping;// Used for INDIA country
    private Set<GoodsReceiptDetailPaymentMapping> goodsReceiptDetailPaymentMapping;
    private double tdsInterestRateAtPaymentTime;
    private String pricingBandMasterid;
    private boolean isUserModifiedTaxAmount;//ERM-1085 - To identify row tax amount is user modified or system calculated according to adaptive rounding algo.
    private int itcType; // used for ITC type for Indian GST ERP-41416
    
    
    /**
     * Overriding compareTo method to provide implementation Sorting objects
     * according to srno in ascending order.
     * @param grd
     */
    @Override
    public int compareTo(GoodsReceiptDetail grd) {
        int srNo = grd.getSrno();
        if (this.srno == srNo) {
            return 0;
        } else if (this.srno > srNo) {
            return 1;
        } else {
            return -1;
        }
    }
    
    public String getPermit() {
        return permit;
    }

    public void setPermit(String permit) {
        this.permit = permit;
    }

    public PurchaseOrderDetail getPurchaseorderdetail() {
        return purchaseorderdetail;
    }

    public void setPurchaseorderdetail(PurchaseOrderDetail purchaseorderdetail) {
        this.purchaseorderdetail = purchaseorderdetail;
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

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public GoodsReceipt getGoodsReceipt() {
        return goodsReceipt;
    }

    public void setGoodsReceipt(GoodsReceipt goodsReceipt) {
        this.goodsReceipt = goodsReceipt;
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
    
    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public GoodsReceiptOrderDetails getGoodsReceiptOrderDetails() {
        return goodsReceiptOrderDetails;
    }

    public void setGoodsReceiptOrderDetails(GoodsReceiptOrderDetails goodsReceiptOrderDetails) {
        this.goodsReceiptOrderDetails = goodsReceiptOrderDetails;
    }

    public Projreport_Template getTemplateid() {
        return templateid;
    }

    public void setTemplateid(Projreport_Template templateid) {
        this.templateid = templateid;
    }

    public VendorQuotationDetail getVendorQuotationDetail() {
        return vendorQuotationDetail;
    }

    public void setVendorQuotationDetail(VendorQuotationDetail vendorQuotationDetail) {
        this.vendorQuotationDetail = vendorQuotationDetail;
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

    public String getPIDetailDescription() {
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

    public String getSupplierpartnumber() {
        return supplierpartnumber;
    }

    public void setSupplierpartnumber(String supplierpartnumber) {
        this.supplierpartnumber = supplierpartnumber;
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

    public JournalEntryDetail getPurchaseJED() {
        return purchaseJED;
    }

    public void setPurchaseJED(JournalEntryDetail purchaseJED) {
        this.purchaseJED = purchaseJED;
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

    public double getTdsAssessableAmount() {
        return tdsAssessableAmount;
    }

    public void setTdsAssessableAmount(double tdsAssessableAmount) {
        this.tdsAssessableAmount = tdsAssessableAmount;
    }
//
    public MasterItem getNatureOfPayment() {
        return natureOfPayment;
    }

    public void setNatureOfPayment(MasterItem natureOfPayment) {
        this.natureOfPayment = natureOfPayment;
    }

    public int getTdsRuleId() {
        return tdsRuleId;
    }

    public void setTdsRuleId(int tdsRuleId) {
        this.tdsRuleId = tdsRuleId;
    }

    public double getTdsRate() {
        return tdsRate;
    }

    public void setTdsRate(double tdsRate) {
        this.tdsRate = tdsRate;
    }

    public double getTdsLineAmount() {
        return tdsLineAmount;
    }

    public void setTdsLineAmount(double tdsLineAmount) {
        this.tdsLineAmount = tdsLineAmount;
    }

    public int getTdsPaidFlag() {
        return tdsPaidFlag;
    }

    public void setTdsPaidFlag(int tdsPaidFlag) {
        this.tdsPaidFlag = tdsPaidFlag;
    }

    public String getTdsPayment() {
        return tdsPayment;
    }

    public void setTdsPayment(String tdsPayment) {
        this.tdsPayment = tdsPayment;
    }

    public int getTdsInterestPaidFlag() {
        return tdsInterestPaidFlag;
    }

    public void setTdsInterestPaidFlag(int tdsInterestPaidFlag) {
        this.tdsInterestPaidFlag = tdsInterestPaidFlag;
    }

    public String getTdsInterestPayment() {
        return tdsInterestPayment;
    }

    public void setTdsInterestPayment(String tdsInterestPayment) {
        this.tdsInterestPayment = tdsInterestPayment;
    }
    
    public Account getTdsPayableAccount() {
        return tdsPayableAccount;
    }

    public void setTdsPayableAccount(Account tdsPayableAccount) {
        this.tdsPayableAccount = tdsPayableAccount;
    }

    public TdsJEMapping getTdsJEMapping() {
        return tdsJEMapping;
    }

    public void setTdsJEMapping(TdsJEMapping tdsJEMapping) {
        this.tdsJEMapping = tdsJEMapping;
    }

    public Set<GoodsReceiptDetailPaymentMapping> getGoodsReceiptDetailPaymentMapping() {
        return goodsReceiptDetailPaymentMapping;
    }

    public void setGoodsReceiptDetailPaymentMapping(Set<GoodsReceiptDetailPaymentMapping> goodsReceiptDetailPaymentMapping) {
        this.goodsReceiptDetailPaymentMapping = goodsReceiptDetailPaymentMapping;
    }

    public double getTdsInterestRateAtPaymentTime() {
        return tdsInterestRateAtPaymentTime;
    }

    public void setTdsInterestRateAtPaymentTime(double tdsInterestRateAtPaymentTime) {
        this.tdsInterestRateAtPaymentTime = tdsInterestRateAtPaymentTime;
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

    public int getItcType() {
        return itcType;
    }

    public void setItcType(int itcType) {
        this.itcType = itcType;
    }
}
