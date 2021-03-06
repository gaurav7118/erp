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

import com.krawler.common.admin.*;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class PurchaseOrder {

    private String ID;
    private String purchaseOrderNumber;
    private boolean autoGenerated;
    private Date dueDate;
    private Date orderDate;
    private Vendor vendor;
    private String memo;
    private Set<PurchaseOrderDetail> rows;
    private Set<ExpensePODetail> expenserows;
    private Company company;
    private boolean deleted;
    private CostCenter costcenter;
    private Tax tax;
    private KWLCurrency currency;
    private Projreport_Template templateid;
    private Date shipdate;
    private String shipvia;
    private String fob;
    private int pendingapproval;
    private boolean favourite;
    private User approver;
    private boolean perDiscount;
    private double discount;
    private int approvallevel;
    private int istemplate;
    private PurchaseOrderCustomData poCustomData;
    private int linkflag; //values are [0,1,2] 0 means Not Linked , 1 means Linked in vendor Invoice, 2 means Linked in Goods Receipt Order
    private String postText;
    private String billTo;
    private String shipTo;
    private Term term;
    private User createdby;
    private User modifiedby;
    private long createdon;
    private long updatedon;
    private boolean isOpeningBalancePO;
    private int seqnumber;//Only to store integer part of sequence format
    private SequenceFormat seqformat;
    private String datePreffixValue;//Only to store Date Preffix part of sequence format
    private String dateSuffixValue;//Only to store Date Sufefix part of sequence format
    private String dateAfterPreffixValue;//Only to store Date After Prefix part of sequence format
    private double shiplength;
    private String invoicetype;
    private BillingShippingAddresses billingShippingAddresses;
    private MasterItem masteragent;
    private boolean printed;
    private boolean gstIncluded;
    private double externalCurrencyRate;
    private boolean isconsignment;
    private Boolean termsincludegst; // flag of calculated the GST amount on Basic or on adding other terms amount
    private boolean fixedAssetPO;
    private int approvestatuslevel;
    private boolean isOpen; // flag For Linked is open or closed 
    private boolean isPoUsed; // flag For Linked is open or closed 
    private double totalamount;
    private double totalamountinbase;
    private double discountinbase;
    private double totallineleveldiscount;
    private boolean disabledPOforSO;//flag for identifying whether PO is free for SO or not value->'T' means disable
    private boolean isPOClosed;//flag for identifying whether PO is Closed manually or not value->'F' means not closed
    private boolean isExpenseType;//Flag used to identify wheather PO is expense type or normal product grid.
    // Fields for MRP 
    private boolean isMRPJobWorkOut;
    private Product product;
    private String workorderid; // Work Order Object is Not Accsessible
    private double productquantity;
    private InventoryLocation jobworklocation;
    private String shipmentroute;
    private String gatepass;
    private String otherremarks;
    private Date dateofdelivery;
    private Date dateofshipment;
    private double excisedutychargees;
    private String formtype;
    private String supplierInvoiceNo;//SDP-4510
    private boolean gtaapplicable;//For India Country Only.
    private boolean isEmailSent;//flag to update Email Icon,
    private boolean isJobWorkOutOrder;   // True if Job Work Out Order Created other wise False
    private boolean isIndGSTApplied;
    private boolean applyTaxToTerms;
    private boolean linkedSOBlocked;
    private double roundingadjustmentamount;
    private double roundingadjustmentamountinbase;
    private boolean isRoundingAdjustmentApplied;
    private boolean isDropshipDocument;//True ,If DropShip Type PO
    private boolean isMerchantExporter;//For India Country Only.
    private boolean isDraft;// flag for identify draft
    
    public boolean isIsDraft() {
        return isDraft;
    }

    public void setIsDraft(boolean isDraft) {
        this.isDraft = isDraft;
    }

    public boolean isIsDropshipDocument() {
        return isDropshipDocument;
    }

    public void setIsDropshipDocument(boolean isDropshipDocument) {
        this.isDropshipDocument = isDropshipDocument;
    }

    public boolean isIsRoundingAdjustmentApplied() {
        return isRoundingAdjustmentApplied;
    }

    public void setIsRoundingAdjustmentApplied(boolean isRoundingAdjustmentApplied) {
        this.isRoundingAdjustmentApplied = isRoundingAdjustmentApplied;
    }

    public double getRoundingadjustmentamount() {
        return roundingadjustmentamount;
    }

    public void setRoundingadjustmentamount(double roundingadjustmentamount) {
        this.roundingadjustmentamount = roundingadjustmentamount;
    }

    public double getRoundingadjustmentamountinbase() {
        return roundingadjustmentamountinbase;
    }

    public void setRoundingadjustmentamountinbase(double roundingadjustmentamountinbase) {
        this.roundingadjustmentamountinbase = roundingadjustmentamountinbase;
    }

    public boolean isIsIndGSTApplied() {
        return isIndGSTApplied;
    }

    public void setIsIndGSTApplied(boolean isIndGSTApplied) {
        this.isIndGSTApplied = isIndGSTApplied;
    }
    public boolean isIsJobWorkOutOrder() {
        return isJobWorkOutOrder;
    }

    public void setIsJobWorkOutOrder(boolean isJobWorkOutOrder) {
        this.isJobWorkOutOrder = isJobWorkOutOrder;
    }

    public boolean isIsEmailSent() {
        return isEmailSent;
    }

    public void setIsEmailSent(boolean isEmailSent) {
        this.isEmailSent = isEmailSent;
    }

    public String getFormtype() {
        return formtype;
    }

    public void setFormtype(String formtype) {
        this.formtype = formtype;
    }
    
    public boolean isIsExpenseType() {
        return isExpenseType;
    }

    public void setIsExpenseType(boolean isExpenseType) {
        this.isExpenseType = isExpenseType;
    }

    public boolean isIsPOClosed() {
        return isPOClosed;
    }

    public void setIsPOClosed(boolean isPOClosed) {
        this.isPOClosed = isPOClosed;
    }
   
    public String getDatePreffixValue() {
        return datePreffixValue;
    }

    public void setDatePreffixValue(String datePreffixValue) {
        this.datePreffixValue = datePreffixValue;
    }

    public String getDateSuffixValue() {
        return dateSuffixValue;
    }

    public void setDateSuffixValue(String dateSuffixValue) {
        this.dateSuffixValue = dateSuffixValue;
    }

    public boolean isDisabledPOforSO() {
        return disabledPOforSO;
    }

    public void setDisabledPOforSO(boolean disabledPOforSO) {
        this.disabledPOforSO = disabledPOforSO;
    }

    public double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(double totalamount) {
        this.totalamount = totalamount;
    }

    public double getTotalamountinbase() {
        return totalamountinbase;
    }

    public void setTotalamountinbase(double totalamountinbase) {
        this.totalamountinbase = totalamountinbase;
    }

    public double getDiscountinbase() {
        return discountinbase;
    }

    public void setDiscountinbase(double discountinbase) {
        this.discountinbase = discountinbase;
    }

    public double getTotallineleveldiscount() {
        return totallineleveldiscount;
    }

    public void setTotallineleveldiscount(double totallineleveldiscount) {
        this.totallineleveldiscount = totallineleveldiscount;
    }

    public PurchaseOrder() {
        isOpen = true;
        isPoUsed=false;
    }
    public boolean isIsOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
  
    public double getExternalCurrencyRate() {
        return externalCurrencyRate;
    }

    public void setExternalCurrencyRate(double externalCurrencyRate) {
        this.externalCurrencyRate = externalCurrencyRate;
    }                

    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    public MasterItem getMasteragent() {
        return masteragent;
    }

    public void setMasteragent(MasterItem masteragent) {
        this.masteragent = masteragent;
    }

    public BillingShippingAddresses getBillingShippingAddresses() {
        return billingShippingAddresses;
    }

    public void setBillingShippingAddresses(BillingShippingAddresses billingShippingAddresses) {
        this.billingShippingAddresses = billingShippingAddresses;
    }

    public SequenceFormat getSeqformat() {
        return seqformat;
    }

    public void setSeqformat(SequenceFormat seqformat) {
        this.seqformat = seqformat;
    }

    public int getSeqnumber() {
        return seqnumber;
    }

    public void setSeqnumber(int seqnumber) {
        this.seqnumber = seqnumber;
    }

    public int getIstemplate() {
        return istemplate;
    }

    public void setIstemplate(int istemplate) {
        this.istemplate = istemplate;
    }

    public String getPostText() {
        return postText;
    }

    public String getBillTo() {
        return billTo;
    }

    public void setBillTo(String billTo) {
        this.billTo = billTo;
    }

    public String getShipTo() {
        return shipTo;
    }

    public void setShipTo(String shipTo) {
        this.shipTo = shipTo;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public Projreport_Template getTemplateid() {
        return templateid;
    }

    public void setTemplateid(Projreport_Template templateid) {
        this.templateid = templateid;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getApprovallevel() {
        return approvallevel;
    }

    public void setApprovallevel(int approvallevel) {
        this.approvallevel = approvallevel;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public Set<PurchaseOrderDetail> getRows() {
        return rows;
    }

    public void setRows(Set<PurchaseOrderDetail> rows) {
        this.rows = rows;
    }

    public Set<ExpensePODetail> getExpenserows() {
        return expenserows;
    }

    public void setExpenserows(Set<ExpensePODetail> expenserows) {
        this.expenserows = expenserows;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public CostCenter getCostcenter() {
        return costcenter;
    }

    public void setCostcenter(CostCenter costcenter) {
        this.costcenter = costcenter;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public int getPendingapproval() {
        return pendingapproval;
    }

    public void setPendingapproval(int pendingapproval) {
        this.pendingapproval = pendingapproval;
    }

    public String getFob() {
        return fob;
    }

    public void setFob(String fob) {
        this.fob = fob;
    }

    public String getShipvia() {
        return shipvia;
    }

    public void setShipvia(String shipvia) {
        this.shipvia = shipvia;
    }

    public Date getShipdate() {
        return shipdate;
    }

    public void setShipdate(Date shipdate) {
        this.shipdate = shipdate;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public int getLinkflag() {
        return linkflag;
    }

    public void setLinkflag(int linkflag) {
        this.linkflag = linkflag;
    }

    public PurchaseOrderCustomData getPoCustomData() {
        return poCustomData;
    }

    public void setPoCustomData(PurchaseOrderCustomData poCustomData) {
        this.poCustomData = poCustomData;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public boolean isPerDiscount() {
        return perDiscount;
    }

    public void setPerDiscount(boolean perDiscount) {
        this.perDiscount = perDiscount;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public boolean isIsOpeningBalancePO() {
        return isOpeningBalancePO;
    }

    public void setIsOpeningBalancePO(boolean isOpeningBalancePO) {
        this.isOpeningBalancePO = isOpeningBalancePO;
    }

    public User getCreatedby() {
        return createdby;
    }

    public void setCreatedby(User createdby) {
        this.createdby = createdby;
    }

    public long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(long createdon) {
        this.createdon = createdon;
    }

    public User getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(User modifiedby) {
        this.modifiedby = modifiedby;
    }

    public long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(long updatedon) {
        this.updatedon = updatedon;
    }

    public boolean isGstIncluded() {
        return gstIncluded;
    }

    public void setGstIncluded(boolean gstIncluded) {
        this.gstIncluded = gstIncluded;
    }

    public String getInvoicetype() {
        return invoicetype;
    }

    public void setInvoicetype(String invoicetype) {
        this.invoicetype = invoicetype;
    }

    public double getShiplength() {
        return shiplength;
    }

    public void setShiplength(double shiplength) {
        this.shiplength = shiplength;
    }

    public boolean isIsconsignment() {
        return isconsignment;
    }

    public void setIsconsignment(boolean isconsignment) {
        this.isconsignment = isconsignment;
    }
    
    public Boolean getTermsincludegst() {
        return termsincludegst;
    }

    public Date getDateofdelivery() {
        return dateofdelivery;
    }

    public void setDateofdelivery(Date dateofdelivery) {
        this.dateofdelivery = dateofdelivery;
    }

    public Date getDateofshipment() {
        return dateofshipment;
    }

    public void setDateofshipment(Date dateofshipment) {
        this.dateofshipment = dateofshipment;
    }

    public double getExcisedutychargees() {
        return excisedutychargees;
    }

    public void setExcisedutychargees(double excisedutychargees) {
        this.excisedutychargees = excisedutychargees;
    }

    public String getGatepass() {
        return gatepass;
    }

    public void setGatepass(String gatepass) {
        this.gatepass = gatepass;
    }

    public boolean isIsMRPJobWorkOut() {
        return isMRPJobWorkOut;
    }

    public void setIsMRPJobWorkOut(boolean isMRPJobWorkOut) {
        this.isMRPJobWorkOut = isMRPJobWorkOut;
    }

    public InventoryLocation getJobworklocation() {
        return jobworklocation;
    }

    public void setJobworklocation(InventoryLocation jobworklocation) {
        this.jobworklocation = jobworklocation;
    }

    public String getOtherremarks() {
        return otherremarks;
    }

    public void setOtherremarks(String otherremarks) {
        this.otherremarks = otherremarks;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getProductquantity() {
        return productquantity;
    }

    public void setProductquantity(double productquantity) {
        this.productquantity = productquantity;
    }

    public String getShipmentroute() {
        return shipmentroute;
    }

    public void setShipmentroute(String shipmentroute) {
        this.shipmentroute = shipmentroute;
    }

    public String getWorkorderid() {
        return workorderid;
    }

    public void setWorkorderid(String workorderid) {
        this.workorderid = workorderid;
    }

    public void setTermsincludegst(Boolean termsincludegst) {
        this.termsincludegst = termsincludegst;
    }

    public boolean isFixedAssetPO() {
        return fixedAssetPO;
    }

    public void setFixedAssetPO(boolean fixedAssetPO) {
        this.fixedAssetPO = fixedAssetPO;
    }

    public int getApprovestatuslevel() {
        return approvestatuslevel;
    }

    public void setApprovestatuslevel(int approvestatuslevel) {
        this.approvestatuslevel = approvestatuslevel;
    }
    
    public String getDateAfterPreffixValue() {
        return dateAfterPreffixValue;
    }

    public void setDateAfterPreffixValue(String dateAfterPreffixValue) {
        this.dateAfterPreffixValue = dateAfterPreffixValue;
    }

    public String getSupplierInvoiceNo() {
        return supplierInvoiceNo;
    }

    public void setSupplierInvoiceNo(String supplierInvoiceNo) {
        this.supplierInvoiceNo = supplierInvoiceNo;
    }

    public boolean isGtaapplicable() {
        return gtaapplicable;
    }

    public void setGtaapplicable(boolean gtaapplicable) {
        this.gtaapplicable = gtaapplicable;
    }

    
    public static final String JOBWORKOUTID = "id";
    public static final String JOBORDERNAME = "jobordername";
    public static final String JOBORDERNUMBER = "jobordernumber";

    public static final String SEQUENCEFORMAT = "seqformat";
    public static final String JOBWORKDATE = "jobworkdate";
    public static final String DATEOFDELIVERY = "dateofdelivery";

    public static final String VENDORID = "vendorid";
    public static final String VENDORCODE = "vendorcode";
    public static final String VENDORNAME = "vendorname";
    public static final String DATEOFSHIPMENT = "dateofshipment";
    public static final String EXCISEDUTYCHARGES = "excisedutychargees";

    public static final String JOBWORKLOCATION = "jobworklocation";
    public static final String JOBWORKLOCATIONID="jobworklocationid";
    public static final String SHIPMENTROUTE = "shipmentroute";
    public static final String GATEPASS = "gatepass";

    public static final String OTHERREMARKS = "otherremarks";
    public static final String COMPANYID = "companyid";
    public static final String USERID = "userid";

    public static final String WORKORDERNAME = "workordername";
    public static final String WORKORDERID = "workorderid";
    public static final String WORKORDERCODE = "workordercode";

    public static final String WORKCENTERNAME = "workcentrename";
    public static final String WORKCENTERID = "workcentreid";
    public static final String WORKCENTERCODE = "workcentrecode";

    public static final String PRODUCTID = "productid";
    public static final String PRODUCTCODE = "productcode";
    public static final String PRODUCTUOMNAME = "uomname";
    public static final String PRODUCTTYPE ="type";
    public static final String PRODUCTQUANTITY = "productquantity";

    public static final String WORKCENTERTYPE = "workcentretype";
    public static final String MACHINEID = "machineid";
    public static final String LABOURID = "labourid";
    public static final String MATERIALID = "materialid";
//    public static final String POJONAME = "JobWork";
    public static final String POJONAME = "PurchaseOrder";
    public static final String DB_WORKORDERID = "workorderid.ID";
//    public static final String CUSTOMFIELD  = "customfield";
//    public static final String ACCJOBWORKCUSTOMDATAREF  = "accjobworkcustomdataref";

    public static final String KEY = "name";

    /**
     * @return the isPoUsed
     */
    public boolean isIsPoUsed() {
        return isPoUsed;
    }

    /**
     * @param isPoUsed the isPoUsed to set
     */
    public void setIsPoUsed(boolean isPoUsed) {
        this.isPoUsed = isPoUsed;
    }

    public boolean isApplyTaxToTerms() {
        return applyTaxToTerms;
    }

    public void setApplyTaxToTerms(boolean applyTaxToTerms) {
        this.applyTaxToTerms = applyTaxToTerms;
    }
    
    public boolean isLinkedSOBlocked() {
        return linkedSOBlocked;
    }

    public void setLinkedSOBlocked(boolean linkedSOBlocked) {
        this.linkedSOBlocked = linkedSOBlocked;
    }

    public boolean isIsMerchantExporter() {
        return isMerchantExporter;
    }

    public void setIsMerchantExporter(boolean isMerchantExporter) {
        this.isMerchantExporter = isMerchantExporter;
    }
}
