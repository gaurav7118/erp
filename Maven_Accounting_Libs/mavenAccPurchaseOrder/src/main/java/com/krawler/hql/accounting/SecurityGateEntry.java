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
public class SecurityGateEntry {
    
    private String ID;
    private String securityNumber;
    private boolean autoGenerated;
    private Date dueDate;
    private Date securityDate;
    private Vendor vendor;
    private Set<SecurityGateDetails> rows;
    private String memo;
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
    private int linkflag; //values are [0,1,2] 0 means Not Linked , 1 means Linked in vendor Invoice, 2 means Linked in Goods Receipt Order
    private String postText;
    private SecurityGateEntryCustomData sgeCustomData;

    private String billTo;
    private String shipTo;
    private Term term;
    private User createdby;
    private User modifiedby;
    private long createdon;
    private long updatedon;
    private int seqnumber;//Only to store integer part of sequence format
    private SequenceFormat seqformat;
    private String datePreffixValue;//Only to store Date Preffix part of sequence format
    private String dateSuffixValue;//Only to store Date Sufefix part of sequence format
    private String dateAfterPreffixValue;//Only to store Date After Prefix part of sequence format
    private double shiplength;
    private BillingShippingAddresses billingShippingAddresses;
    private MasterItem masteragent;
    private boolean gstIncluded;
    private double externalCurrencyRate;
    private Boolean termsincludegst; // flag of calculated the GST amount on Basic or on adding other terms amount
    private int approvestatuslevel;
    private boolean isOpen; // flag For Linked is open or closed 
    private double totalamount;
    private double totalamountinbase;
    private double discountinbase;
    private double totallineleveldiscount;
    private boolean disabledPOforSO;//flag for identifying whether PO is free for SO or not value->'T' means disable
    private boolean isPOClosed;//flag for identifying whether PO is Closed manually or not value->'F' means not closed
   
    // Fields for MRP 
    private Product product;
    private double productquantity;
    private Date dateofdelivery;
    private Date dateofshipment;
    private String supplierInvoiceNo;//SDP-4510
    private boolean isEmailSent;//flag to update Email Icon
    
    public SecurityGateEntryCustomData getSgeCustomData() {
        return sgeCustomData;
    }

    public void setSgeCustomData(SecurityGateEntryCustomData sgeCustomData) {
        this.sgeCustomData = sgeCustomData;
    }

    public boolean isIsEmailSent() {
        return isEmailSent;
    }

    public void setIsEmailSent(boolean isEmailSent) {
        this.isEmailSent = isEmailSent;
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

    public SecurityGateEntry() {
        isOpen = true;
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


    public double getShiplength() {
        return shiplength;
    }

    public void setShiplength(double shiplength) {
        this.shiplength = shiplength;
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

    /**
     * @return the termsincludegst
     */
    public Boolean getTermsincludegst() {
        return termsincludegst;
    }

    /**
     * @param termsincludegst the termsincludegst to set
     */
    public void setTermsincludegst(Boolean termsincludegst) {
        this.termsincludegst = termsincludegst;
    }

    /**
     * @return the securityNumber
     */
    public String getSecurityNumber() {
        return securityNumber;
    }

    /**
     * @param securityNumber the securityNumber to set
     */
    public void setSecurityNumber(String securityNumber) {
        this.securityNumber = securityNumber;
    }

    /**
     * @return the rows
     */
    public Set<SecurityGateDetails> getRows() {
        return rows;
    }

    /**
     * @param rows the rows to set
     */
    public void setRows(Set<SecurityGateDetails> rows) {
        this.rows = rows;
    }

    /**
     * @return the securityDate
     */
    public Date getSecurityDate() {
        return securityDate;
    }

    /**
     * @param securityDate the securityDate to set
     */
    public void setSecurityDate(Date securityDate) {
        this.securityDate = securityDate;
    }

    

}