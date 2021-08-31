/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.TDSRate;

/*
 *
 * @author krawler
 */
public class TdsDetails {

    private String tdsid;
    private String documenttype;
    private String documentid;
    private String documentdetails;
    private boolean includetaxamount;
    private double tdspercentage;
    private double tdsamount;
    private double enteramount;
    private double tdsAssessableAmount;
    private TDSRate ruleid;
    private Account account;
    private Account tdspayableaccount;
    private Company company;
    private PaymentDetail paymentdetail;
    private AdvanceDetail advanceDetail;
    private CreditNotePaymentDetails creditnotepaymentdetail;
    private PaymentDetailOtherwise paymentdetailotherwise;
    private String journalEntryDetail;
    private MasterItem natureOfPayment;
    

    public double getEnteramount() {
        return enteramount;
    }

    public void setEnteramount(double enteramount) {
        this.enteramount = enteramount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getTdsid() {
        return tdsid;
    }

    public void setTdsid(String tdsid) {
        this.tdsid = tdsid;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getDocumentdetails() {
        return documentdetails;
    }

    public void setDocumentdetails(String documentdetails) {
        this.documentdetails = documentdetails;
    }

    public String getDocumentid() {
        return documentid;
    }

    public void setDocumentid(String documentid) {
        this.documentid = documentid;
    }

    public String getDocumenttype() {
        return documenttype;
    }

    public void setDocumenttype(String documenttype) {
        this.documenttype = documenttype;
    }

    public boolean isIncludetaxamount() {
        return includetaxamount;
    }

    public void setIncludetaxamount(boolean includetaxamount) {
        this.includetaxamount = includetaxamount;
    }

    public double getTdsamount() {
        return tdsamount;
    }

    public void setTdsamount(double tdsamount) {
        this.tdsamount = tdsamount;
    }

    public double getTdspercentage() {
        return tdspercentage;
    }

    public void setTdspercentage(double tdspercentage) {
        this.tdspercentage = tdspercentage;
    }
    
    public TDSRate getRuleid() {
        return ruleid;
    }

    public void setRuleid(TDSRate ruleid) {
        this.ruleid = ruleid;
    }

    public PaymentDetail getPaymentdetail() {
        return paymentdetail;
    }

    public void setPaymentdetail(PaymentDetail paymentdetail) {
        this.paymentdetail = paymentdetail;
    }
    
    public AdvanceDetail getAdvanceDetail() {
        return advanceDetail;
    }

    public void setAdvanceDetail(AdvanceDetail advanceDetail) {
        this.advanceDetail = advanceDetail;
    }
    
    public CreditNotePaymentDetails getCreditnotepaymentdetail() {
        return creditnotepaymentdetail;
    }
    
    public void setCreditnotepaymentdetail(CreditNotePaymentDetails creditnotepaymentdetail) {
        this.creditnotepaymentdetail = creditnotepaymentdetail;
    }
    
    public PaymentDetailOtherwise getPaymentdetailotherwise() {
        return paymentdetailotherwise;
    }

    public void setPaymentdetailotherwise(PaymentDetailOtherwise paymentdetailotherwise) {
        this.paymentdetailotherwise = paymentdetailotherwise;
    }

    public String getJournalEntryDetail() {
        return journalEntryDetail;
    }

    public void setJournalEntryDetail(String journalEntryDetail) {
        this.journalEntryDetail = journalEntryDetail;
    }

    public Account getTdspayableaccount() {
        return tdspayableaccount;
    }

    public void setTdspayableaccount(Account tdspayableaccount) {
        this.tdspayableaccount = tdspayableaccount;
    }

    public double getTdsAssessableAmount() {
        return tdsAssessableAmount;
    }

    public void setTdsAssessableAmount(double tdsAssessableAmount) {
        this.tdsAssessableAmount = tdsAssessableAmount;
    }

    public MasterItem getNatureOfPayment() {
        return natureOfPayment;
    }

    public void setNatureOfPayment(MasterItem natureOfPayment) {
        this.natureOfPayment = natureOfPayment;
    }
    
    
}
