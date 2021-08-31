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
import com.krawler.common.admin.RequestApprovalStatus;
import com.krawler.common.admin.User;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.inspection.InspectionTemplate;
import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class SalesOrderDetail {

    private String ID;
    private int srno;
    private SalesOrder salesOrder;
    private Product product;
    private double quantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double lockquantity;
    private double lockQuantityInSelectedUOM;
    private double baseuomrate;
    private double rate;
    private double rateincludegst;
    private String remark;
    private Company company;
    private Tax tax;
    private String description;
    private double discount;
    private int discountispercent;
    private double rowTaxAmount;
    private double rowTermTaxAmount;
    private double rowTermTaxAmountInBase;
    private SalesOrderDetailsCustomData soDetailCustomData;
    private QuotationDetail quotationDetail;
    private ProductReplacementDetail productReplacementDetail;
    private String dependentType;
    private String inouttime;
    private String showquantity;
    private String purchaseorderdetailid;
    private String mrpcontractdetailid;
    private String invstoreid;
    private String invlocid;
    private String priceSource;
    private double lockquantitydue;
    private double approvedQuantity;//for Consignment Approval Flow
    private double rejectedQuantity;//for Consignment Approval Flow
    private Set<User> approverSet;//for Consignment Approval Flow
    private RequestApprovalStatus requestApprovalStatus;//for Consignment Approval Flow   
    private User rejectedby;
    private double balanceqty;   //this is used to identify renmaning quantity after purchase return linked with GR order which is created by linking PO 
    private double rowtermamount; 
    private double OtherTermNonTaxableAmount;
    private boolean isLineItemClosed;//flag for identifying whether sodetail is Closed manually or not value->'F' means not closed
    private boolean isLineItemRejected;//flag for identifying whether sodetail is Rejected or not.  value->'F' means not Rejected
    private String rejectionreason; // used to store reason of rejection
    private  double mrpIndia;
    private  String exciseValuationType;
    private  String vatValuationType;
    private  UnitOfMeasure reportingUOMExcise;
    private  UnitOfMeasure reportingUOMVAT;
    private  UOMschemaType reportingSchemaTypeExcise;
    private  UOMschemaType reportingSchemaVAT;
    private double lineLevelTermAmount;
    private BOMDetail bomcode;
    private boolean jobOrderItem;     
    private String jobOrderItemNumber;
    private String discountJson ;                                               //Used to store json of discount masters applied in Price band screen for each row ERM-68
    private String sourcePurchaseOrderDetailsid;
    private InspectionTemplate inspectionTemplate;
    private boolean isUserModifiedTaxAmount;//ERM-1085 - To identify row tax amount is user modified or system calculated according to adaptive rounding algo.
    private InspectionForm inspectionForm;
    private String pricingBandMasterid;

    public InspectionTemplate getInspectionTemplate() {
        return inspectionTemplate;
    }

    public void setInspectionTemplate(InspectionTemplate inspectionTemplate) {
        this.inspectionTemplate = inspectionTemplate;
    }

    public InspectionForm getInspectionForm() {
        return inspectionForm;
    }

    public void setInspectionForm(InspectionForm inspectionForm) {
        this.inspectionForm = inspectionForm;
    }

    public String getSourcePurchaseOrderDetailsid() {
        return sourcePurchaseOrderDetailsid;
    }

    public void setSourcePurchaseOrderDetailsid(String sourcePurchaseOrderDetailsid) {
        this.sourcePurchaseOrderDetailsid = sourcePurchaseOrderDetailsid;
    }

    public String getDiscountJson() {
        return discountJson;
    }

    public void setDiscountJson(String discountJson) {
        this.discountJson = discountJson;
    }
    
    public boolean isIsLineItemRejected() {
        return isLineItemRejected;
    }

    public void setIsLineItemRejected(boolean isLineItemRejected) {
        this.isLineItemRejected = isLineItemRejected;
    }

    public String getRejectionreason() {
        return rejectionreason;
    }

    public void setRejectionreason(String rejectionreason) {
        this.rejectionreason = rejectionreason;
    }
    
    public boolean isIsLineItemClosed() {
        return isLineItemClosed;
    }

    public void setIsLineItemClosed(boolean isLineItemClosed) {
        this.isLineItemClosed = isLineItemClosed;
    }
   

    public QuotationDetail getQuotationDetail() {
        return quotationDetail;
    }

    public void setQuotationDetail(QuotationDetail quotationDetail) {
        this.quotationDetail = quotationDetail;
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

    public SalesOrder getSalesOrder() {
        return salesOrder;
    }

    public void setSalesOrder(SalesOrder salesOrder) {
        this.salesOrder = salesOrder;
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

    public double getLockquantity() {
        return lockquantity;
    }

    public void setLockquantity(double lockquantity) {
        this.lockquantity = lockquantity;
    }

    public double getLockQuantityInSelectedUOM() {
        return lockQuantityInSelectedUOM;
    }

    public void setLockQuantityInSelectedUOM(double lockQuantityInSelectedUOM) {
        this.lockQuantityInSelectedUOM = lockQuantityInSelectedUOM;
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

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
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

    public ProductReplacementDetail getProductReplacementDetail() {
        return productReplacementDetail;
    }

    public void setProductReplacementDetail(ProductReplacementDetail productReplacementDetail) {
        this.productReplacementDetail = productReplacementDetail;
    }

    public SalesOrderDetailsCustomData getSoDetailCustomData() {
        return soDetailCustomData;
    }

    public void setSoDetailCustomData(SalesOrderDetailsCustomData soDetailCustomData) {
        this.soDetailCustomData = soDetailCustomData;
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

    public String getPurchaseorderdetailid() {
        return purchaseorderdetailid;
    }

    public void setPurchaseorderdetailid(String purchaseorderdetailid) {
        this.purchaseorderdetailid = purchaseorderdetailid;
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

    public double getLockquantitydue() {
        return lockquantitydue;
    }

    public void setLockquantitydue(double lockquantitydue) {
        this.lockquantitydue = lockquantitydue;
    }

    public double getApprovedQuantity() {
        return approvedQuantity;
    }

    public void setApprovedQuantity(double approvedQuantity) {
        this.approvedQuantity = approvedQuantity;
    }

    public Set<User> getApproverSet() {
        return approverSet;
    }

    public void setApproverSet(Set<User> approverSet) {
        this.approverSet = approverSet;
    }

    public double getRejectedQuantity() {
        return rejectedQuantity;
    }

    public void setRejectedQuantity(double rejectedQuantity) {
        this.rejectedQuantity = rejectedQuantity;
    }

    public User getRejectedby() {
        return rejectedby;
    }

    public void setRejectedby(User rejectedby) {
        this.rejectedby = rejectedby;
    }

    public RequestApprovalStatus getRequestApprovalStatus() {
        return requestApprovalStatus;
    }

    public void setRequestApprovalStatus(RequestApprovalStatus requestApprovalStatus) {
        this.requestApprovalStatus = requestApprovalStatus;
    }

    public double getBalanceqty() {
        return balanceqty;
    }

    public void setBalanceqty(double balanceqty) {
        this.balanceqty = balanceqty;
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

    public String getMrpcontractdetailid() {
        return mrpcontractdetailid;
    }

    public void setMrpcontractdetailid(String mrpcontractdetailid) {
        this.mrpcontractdetailid = mrpcontractdetailid;
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

    public BOMDetail getBomcode() {
        return bomcode;
    }

    public void setBomcode(BOMDetail bomcode) {
        this.bomcode = bomcode;
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
