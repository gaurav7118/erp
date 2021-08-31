/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ProductBatch;

/**
 *
 * @author krawler
 */
public class CreditNoteAgainstVendorGst  implements Comparable<CreditNoteAgainstVendorGst> {
    
    private String ID;
    private int srno;
    private Product product;
    private double actualQuantity;
    private double returnQuantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private GoodsReceiptDetail videtails;
    private double baseuomreturnquantity;
    private double baseuomrate;
    private String description;
    private String remark;
    private Company company;
    private CreditNote creditNote;
    private String invlocid;
    private MasterItem reason;
    private Tax tax;
    private double rowTaxAmount;
    private double discount;
    private int discountispercent;
    private double rate;
    private JournalEntryDetail jedid;
    private JournalEntryDetail gstJED;// To map GST to related JED
    private InvoiceDetail invoiceDetail;

    /**
     * @return the ID
     */
    public String getID() {
        return ID;
    }

    /**
     * @param ID the ID to set
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * @return the srno
     */
    public int getSrno() {
        return srno;
    }
    
    /**
     * Overriding compareTo method to provide implementation
     * Sorting objects according to srno in ascending order
     */
    @Override
    public int compareTo(CreditNoteAgainstVendorGst o) {
        int srNo = o.getSrno();
        if (this.srno == srNo) {
            return 0;
        } else if (this.srno > srNo) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * @param srno the srno to set
     */
    public void setSrno(int srno) {
        this.srno = srno;
    }

    /**
     * @return the product
     */
    public Product getProduct() {
        return product;
    }

    /**
     * @param product the product to set
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * @return the actualQuantity
     */
    public double getActualQuantity() {
        return actualQuantity;
    }

    /**
     * @param actualQuantity the actualQuantity to set
     */
    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    /**
     * @return the returnQuantity
     */
    public double getReturnQuantity() {
        return returnQuantity;
    }

    /**
     * @param returnQuantity the returnQuantity to set
     */
    public void setReturnQuantity(double returnQuantity) {
        this.returnQuantity = returnQuantity;
    }

    /**
     * @return the uom
     */
    public UnitOfMeasure getUom() {
        return uom;
    }

    /**
     * @param uom the uom to set
     */
    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    /**
     * @return the baseuomquantity
     */
    public double getBaseuomquantity() {
        return baseuomquantity;
    }

    /**
     * @param baseuomquantity the baseuomquantity to set
     */
    public void setBaseuomquantity(double baseuomquantity) {
        this.baseuomquantity = baseuomquantity;
    }

    /**
     * @return the baseuomreturnquantity
     */
    public double getBaseuomreturnquantity() {
        return baseuomreturnquantity;
    }

    /**
     * @param baseuomreturnquantity the baseuomreturnquantity to set
     */
    public void setBaseuomreturnquantity(double baseuomreturnquantity) {
        this.baseuomreturnquantity = baseuomreturnquantity;
    }

    /**
     * @return the baseuomrate
     */
    public double getBaseuomrate() {
        return baseuomrate;
    }

    /**
     * @param baseuomrate the baseuomrate to set
     */
    public void setBaseuomrate(double baseuomrate) {
        this.baseuomrate = baseuomrate;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the remark
     */
    public String getRemark() {
        return remark;
    }

    /**
     * @param remark the remark to set
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * @return the company
     */
    public Company getCompany() {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(Company company) {
        this.company = company;
    }

    /**
     * @return the creditNote
     */
    public CreditNote getCreditNote() {
        return creditNote;
    }

    /**
     * @param creditNote the creditNote to set
     */
    public void setCreditNote(CreditNote creditNote) {
        this.creditNote = creditNote;
    }

    /**
     * @return the invlocid
     */
    public String getInvlocid() {
        return invlocid;
    }

    /**
     * @param invlocid the invlocid to set
     */
    public void setInvlocid(String invlocid) {
        this.invlocid = invlocid;
    }

    /**
     * @return the reason
     */
    public MasterItem getReason() {
        return reason;
    }

    /**
     * @param reason the reason to set
     */
    public void setReason(MasterItem reason) {
        this.reason = reason;
    }

    /**
     * @return the tax
     */
    public Tax getTax() {
        return tax;
    }

    /**
     * @param tax the tax to set
     */
    public void setTax(Tax tax) {
        this.tax = tax;
    }

    /**
     * @return the rowTaxAmount
     */
    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    /**
     * @param rowTaxAmount the rowTaxAmount to set
     */
    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }

    /**
     * @return the discount
     */
    public double getDiscount() {
        return discount;
    }

    /**
     * @param discount the discount to set
     */
    public void setDiscount(double discount) {
        this.discount = discount;
    }

    /**
     * @return the discountispercent
     */
    public int getDiscountispercent() {
        return discountispercent;
    }

    /**
     * @param discountispercent the discountispercent to set
     */
    public void setDiscountispercent(int discountispercent) {
        this.discountispercent = discountispercent;
    }

    /**
     * @return the rate
     */
    public double getRate() {
        return rate;
    }

    /**
     * @param rate the rate to set
     */
    public void setRate(double rate) {
        this.rate = rate;
    }

    /**
     * @return the videtails
     */
    public GoodsReceiptDetail getVidetails() {
        return videtails;
    }

    /**
     * @param videtails the videtails to set
     */
    public void setVidetails(GoodsReceiptDetail videtails) {
        this.videtails = videtails;
    }

    public JournalEntryDetail getJedid() {
        return jedid;
    }

    public void setJedid(JournalEntryDetail jedid) {
        this.jedid = jedid;
    }

    public JournalEntryDetail getGstJED() {
        return gstJED;
    }

    public void setGstJED(JournalEntryDetail gstJED) {
        this.gstJED = gstJED;
    }

    public InvoiceDetail getInvoiceDetail() {
        return invoiceDetail;
    }

    public void setInvoiceDetail(InvoiceDetail invoiceDetail) {
        this.invoiceDetail = invoiceDetail;
    }
}