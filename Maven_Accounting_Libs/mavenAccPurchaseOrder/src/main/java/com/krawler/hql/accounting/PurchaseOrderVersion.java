/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.*;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class PurchaseOrderVersion {
    private String ID;
    private String purchaseOrderNumber;
    private boolean autoGenerated;
    private Date dueDate;
    private Date orderDate;
    private Vendor vendor;
    private String memo;
    private Set<PurchaseOrderVersionDetails> rows;
    private Set<ExpensePOVersionDetails> expenserows;
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
    private PurchaseOrderVersionCustomData poVersionCustomData;
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
    private String supplierInvoiceNo;
    private boolean isEmailSent;//flag to update Email Icon,
    private boolean isJobWorkOutOrder;   // True if Job Work Out Order Created other wise False
    private boolean isIndGSTApplied;
    private boolean applyTaxToTerms;
    private boolean linkedSOBlocked;
    private double roundingadjustmentamount;
    private double roundingadjustmentamountinbase;
    private boolean isRoundingAdjustmentApplied;
    private boolean isDropshipDocument;//True ,If DropShip Type PO
    private PurchaseOrder purchaseOrder;
    private String version;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }
    
    public boolean isApplyTaxToTerms() {
        return applyTaxToTerms;
    }

    public void setApplyTaxToTerms(boolean applyTaxToTerms) {
        this.applyTaxToTerms = applyTaxToTerms;
    }

    public int getApprovallevel() {
        return approvallevel;
    }

    public void setApprovallevel(int approvallevel) {
        this.approvallevel = approvallevel;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    public int getApprovestatuslevel() {
        return approvestatuslevel;
    }

    public void setApprovestatuslevel(int approvestatuslevel) {
        this.approvestatuslevel = approvestatuslevel;
    }

    public String getBillTo() {
        return billTo;
    }

    public void setBillTo(String billTo) {
        this.billTo = billTo;
    }

    public BillingShippingAddresses getBillingShippingAddresses() {
        return billingShippingAddresses;
    }

    public void setBillingShippingAddresses(BillingShippingAddresses billingShippingAddresses) {
        this.billingShippingAddresses = billingShippingAddresses;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public CostCenter getCostcenter() {
        return costcenter;
    }

    public void setCostcenter(CostCenter costcenter) {
        this.costcenter = costcenter;
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

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public String getDateAfterPreffixValue() {
        return dateAfterPreffixValue;
    }

    public void setDateAfterPreffixValue(String dateAfterPreffixValue) {
        this.dateAfterPreffixValue = dateAfterPreffixValue;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDisabledPOforSO() {
        return disabledPOforSO;
    }

    public void setDisabledPOforSO(boolean disabledPOforSO) {
        this.disabledPOforSO = disabledPOforSO;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getDiscountinbase() {
        return discountinbase;
    }

    public void setDiscountinbase(double discountinbase) {
        this.discountinbase = discountinbase;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Set<ExpensePOVersionDetails> getExpenserows() {
        return expenserows;
    }

    public void setExpenserows(Set<ExpensePOVersionDetails> expenserows) {
        this.expenserows = expenserows;
    }

    public double getExternalCurrencyRate() {
        return externalCurrencyRate;
    }

    public void setExternalCurrencyRate(double externalCurrencyRate) {
        this.externalCurrencyRate = externalCurrencyRate;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public boolean isFixedAssetPO() {
        return fixedAssetPO;
    }

    public void setFixedAssetPO(boolean fixedAssetPO) {
        this.fixedAssetPO = fixedAssetPO;
    }

    public String getFob() {
        return fob;
    }

    public void setFob(String fob) {
        this.fob = fob;
    }

    public String getGatepass() {
        return gatepass;
    }

    public void setGatepass(String gatepass) {
        this.gatepass = gatepass;
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

    public boolean isIsDropshipDocument() {
        return isDropshipDocument;
    }

    public void setIsDropshipDocument(boolean isDropshipDocument) {
        this.isDropshipDocument = isDropshipDocument;
    }

    public boolean isIsEmailSent() {
        return isEmailSent;
    }

    public void setIsEmailSent(boolean isEmailSent) {
        this.isEmailSent = isEmailSent;
    }

    public boolean isIsExpenseType() {
        return isExpenseType;
    }

    public void setIsExpenseType(boolean isExpenseType) {
        this.isExpenseType = isExpenseType;
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

    public boolean isIsMRPJobWorkOut() {
        return isMRPJobWorkOut;
    }

    public void setIsMRPJobWorkOut(boolean isMRPJobWorkOut) {
        this.isMRPJobWorkOut = isMRPJobWorkOut;
    }

    public boolean isIsOpen() {
        return isOpen;
    }

    public void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }

    public boolean isIsOpeningBalancePO() {
        return isOpeningBalancePO;
    }

    public void setIsOpeningBalancePO(boolean isOpeningBalancePO) {
        this.isOpeningBalancePO = isOpeningBalancePO;
    }

    public boolean isIsPOClosed() {
        return isPOClosed;
    }

    public void setIsPOClosed(boolean isPOClosed) {
        this.isPOClosed = isPOClosed;
    }

    public boolean isIsPoUsed() {
        return isPoUsed;
    }

    public void setIsPoUsed(boolean isPoUsed) {
        this.isPoUsed = isPoUsed;
    }

    public boolean isIsRoundingAdjustmentApplied() {
        return isRoundingAdjustmentApplied;
    }

    public void setIsRoundingAdjustmentApplied(boolean isRoundingAdjustmentApplied) {
        this.isRoundingAdjustmentApplied = isRoundingAdjustmentApplied;
    }

    public boolean isIsconsignment() {
        return isconsignment;
    }

    public void setIsconsignment(boolean isconsignment) {
        this.isconsignment = isconsignment;
    }

    public int getIstemplate() {
        return istemplate;
    }

    public void setIstemplate(int istemplate) {
        this.istemplate = istemplate;
    }

    public InventoryLocation getJobworklocation() {
        return jobworklocation;
    }

    public void setJobworklocation(InventoryLocation jobworklocation) {
        this.jobworklocation = jobworklocation;
    }

    public boolean isLinkedSOBlocked() {
        return linkedSOBlocked;
    }

    public void setLinkedSOBlocked(boolean linkedSOBlocked) {
        this.linkedSOBlocked = linkedSOBlocked;
    }

    public int getLinkflag() {
        return linkflag;
    }

    public void setLinkflag(int linkflag) {
        this.linkflag = linkflag;
    }

    public MasterItem getMasteragent() {
        return masteragent;
    }

    public void setMasteragent(MasterItem masteragent) {
        this.masteragent = masteragent;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public User getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(User modifiedby) {
        this.modifiedby = modifiedby;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getOtherremarks() {
        return otherremarks;
    }

    public void setOtherremarks(String otherremarks) {
        this.otherremarks = otherremarks;
    }

    public int getPendingapproval() {
        return pendingapproval;
    }

    public void setPendingapproval(int pendingapproval) {
        this.pendingapproval = pendingapproval;
    }

    public boolean isPerDiscount() {
        return perDiscount;
    }

    public void setPerDiscount(boolean perDiscount) {
        this.perDiscount = perDiscount;
    }

    public PurchaseOrderVersionCustomData getPoVersionCustomData() {
        return poVersionCustomData;
    }

    public void setPoVersionCustomData(PurchaseOrderVersionCustomData poVersionCustomData) {
        this.poVersionCustomData = poVersionCustomData;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
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

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
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

    public Set<PurchaseOrderVersionDetails> getRows() {
        return rows;
    }

    public void setRows(Set<PurchaseOrderVersionDetails> rows) {
        this.rows = rows;
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

    public String getShipTo() {
        return shipTo;
    }

    public void setShipTo(String shipTo) {
        this.shipTo = shipTo;
    }

    public Date getShipdate() {
        return shipdate;
    }

    public void setShipdate(Date shipdate) {
        this.shipdate = shipdate;
    }

    public double getShiplength() {
        return shiplength;
    }

    public void setShiplength(double shiplength) {
        this.shiplength = shiplength;
    }

    public String getShipmentroute() {
        return shipmentroute;
    }

    public void setShipmentroute(String shipmentroute) {
        this.shipmentroute = shipmentroute;
    }

    public String getShipvia() {
        return shipvia;
    }

    public void setShipvia(String shipvia) {
        this.shipvia = shipvia;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public Projreport_Template getTemplateid() {
        return templateid;
    }

    public void setTemplateid(Projreport_Template templateid) {
        this.templateid = templateid;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public Boolean getTermsincludegst() {
        return termsincludegst;
    }

    public void setTermsincludegst(Boolean termsincludegst) {
        this.termsincludegst = termsincludegst;
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

    public double getTotallineleveldiscount() {
        return totallineleveldiscount;
    }

    public void setTotallineleveldiscount(double totallineleveldiscount) {
        this.totallineleveldiscount = totallineleveldiscount;
    }

    public long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(long updatedon) {
        this.updatedon = updatedon;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public String getWorkorderid() {
        return workorderid;
    }

    public void setWorkorderid(String workorderid) {
        this.workorderid = workorderid;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSupplierInvoiceNo() {
        return supplierInvoiceNo;
    }

    public void setSupplierInvoiceNo(String supplierInvoiceNo) {
        this.supplierInvoiceNo = supplierInvoiceNo;
    }
    
}
