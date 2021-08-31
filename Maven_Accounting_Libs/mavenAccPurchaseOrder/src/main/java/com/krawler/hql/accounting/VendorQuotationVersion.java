/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.BillingShippingAddresses;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.Projreport_Template;
import com.krawler.common.admin.User;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class VendorQuotationVersion {

    private String ID;
    private String quotationNumber;
    private boolean autoGenerated;
    private Date dueDate;
    private Date quotationDate;
    private Vendor vendor;
    private String memo;
    private Set<VendorQuotationVersionDetail> rows;
    private Company company;
    private User createdby;
    private User modifiedby;
    private long createdon;
    private long updatedon;
    private boolean deleted;
    private Tax tax;
    private int archieve;
    private boolean perDiscount;
    private double discount;
    private Date shipdate;
    private String shipvia;
    private String fob;
    private int linkflag; //values are [0,1,2] 0 means Not Linked , 1 means Linked in Vendor Invoice, 2 means Linked in Pudrchase Order
    private boolean printed;
    private double shiplength;
    private String invoicetype;
    private KWLCurrency currency;
    private Projreport_Template templateid;
    private boolean favourite;
    private int istemplate;
    private String postText;
    private VendorQuotationVersionCustomData quotationCustomData;
    private String billTo;
    private String shipTo;
    private int seqnumber;//Only to store integer part of sequence format
    private SequenceFormat seqformat;
    private String datePreffixValue;//Only to store Date Preffix part of sequence format
    private String dateSuffixValue;//Only to store Date Sufefix part of sequence format
    private Date validdate; //Valid Till field for Quotation
    private BillingShippingAddresses billingShippingAddresses;
    private MasterItem masteragent; //Agent name 
    private boolean gstIncluded;
    private int approvestatuslevel;
    private double externalCurrencyRate;
    private boolean fixedAssetVQ;
    private String version;
    private VendorQuotation quotation;

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
    
    public VendorQuotationVersionCustomData getQuotationCustomData() {
        return quotationCustomData;
    }

    public void setQuotationCustomData(VendorQuotationVersionCustomData quotationCustomData) {
        this.quotationCustomData = quotationCustomData;
    }

    public VendorQuotation getQuotation() {
        return quotation;
    }

    public void setQuotation(VendorQuotation quotation) {
        this.quotation = quotation;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public double getExternalCurrencyRate() {
        return externalCurrencyRate;
    }

    public void setExternalCurrencyRate(double externalCurrencyRate) {
        this.externalCurrencyRate = externalCurrencyRate;
    }

    public String getInvoicetype() {
        return invoicetype;
    }

    public void setInvoicetype(String invoicetype) {
        this.invoicetype = invoicetype;
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

    public Date getValiddate() {
        return validdate;
    }

    public void setValiddate(Date validdate) {
        this.validdate = validdate;
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

    public int getIstemplate() {
        return istemplate;
    }

    public void setIstemplate(int istemplate) {
        this.istemplate = istemplate;
    }

    public String getPostText() {
        return postText;
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

    public int getArchieve() {
        return archieve;
    }

    public void setArchieve(int archieve) {
        this.archieve = archieve;
    }

    public boolean isAutoGenerated() {
        return autoGenerated;
    }

    public void setAutoGenerated(boolean autoGenerated) {
        this.autoGenerated = autoGenerated;
    }

    public int getApprovestatuslevel() {
        return approvestatuslevel;
    }

    public void setApprovestatuslevel(int approvestatuslevel) {
        this.approvestatuslevel = approvestatuslevel;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public boolean isPerDiscount() {
        return perDiscount;
    }

    public void setPerDiscount(boolean perDiscount) {
        this.perDiscount = perDiscount;
    }

    public Date getQuotationDate() {
        return quotationDate;
    }

    public void setQuotationDate(Date quotationDate) {
        this.quotationDate = quotationDate;
    }

    public String getQuotationNumber() {
        return quotationNumber;
    }

    public void setQuotationNumber(String quotationNumber) {
        this.quotationNumber = quotationNumber;
    }

    public Set<VendorQuotationVersionDetail> getRows() {
        return rows;
    }

    public void setRows(Set<VendorQuotationVersionDetail> rows) {
        this.rows = rows;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
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

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
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

    public int getLinkflag() {
        return linkflag;
    }

    public void setLinkflag(int linkflag) {
        this.linkflag = linkflag;
    }

    public boolean isFixedAssetVQ() {
        return fixedAssetVQ;
    }

    public void setFixedAssetVQ(boolean fixedAssetVQ) {
        this.fixedAssetVQ = fixedAssetVQ;
    }
}
