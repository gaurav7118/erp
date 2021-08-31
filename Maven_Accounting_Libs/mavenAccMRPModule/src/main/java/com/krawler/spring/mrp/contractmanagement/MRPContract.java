/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.admin.Company;
import com.krawler.hql.accounting.*;
import java.util.Date;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class MRPContract {

    private String ID;
    private Company company;
    private String contractid;
    private String contractname;
    private Customer customer;
    private Set<MRPContractDetails> rows;
    private Date creationdate;
    private MasterItem sellertype;
    private Date contractstartdate;
    private Date contractenddate;
    private String contractterm;
    private MasterItem contractstatus;
    private MRPContract parentcontractid;
    private String parentcontractname;
    private boolean deleteflag;
    private int seqnumber;//Only to store integer part of sequence format
    private SequenceFormat seqformat;//Sequence format
    private String datepreffixvalue;//Only to store Date Preffix part of sequence format
    private String datesuffixvalue;//Only to store Date Sufefix part of sequence format
    private String dateAfterPreffixValue;//Only to store Date After Prefix part of sequence format
    private boolean autogen;
    private PaymentMethod paymentmethodname;
    private String accountname;
    private String detailstype;
    private String autopopulate;
    private String shownincsorcp;
    private String bankname;
    private String bankaccountnumber;
    private String bankaddress;
    private Term paymenttermname;
//    private int paymenttermdays;
    private Date paymenttermdate;
    private String contractorname;
    private String contractorteename;
    private String pannumber;
    private String tannumber;
    private Date dateofaggrement;
    private MasterItem countryaggrement;
    private String stateaggrement;
    private String previouscontractid;
    private String documentrequiredremarks;
    private String actualattachment;
    private MRPContractCustomData accMRPContractCustomData;

    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }

    public String getActualattachment() {
        return actualattachment;
    }

    public void setActualattachment(String actualattachment) {
        this.actualattachment = actualattachment;
    }

    public String getAutopopulate() {
        return autopopulate;
    }

    public void setAutopopulate(String autopopulate) {
        this.autopopulate = autopopulate;
    }

    public String getBankaccountnumber() {
        return bankaccountnumber;
    }

    public void setBankaccountnumber(String bankaccountnumber) {
        this.bankaccountnumber = bankaccountnumber;
    }

    public String getBankaddress() {
        return bankaddress;
    }

    public void setBankaddress(String bankaddress) {
        this.bankaddress = bankaddress;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getContractenddate() {
        return contractenddate;
    }

    public void setContractenddate(Date contractenddate) {
        this.contractenddate = contractenddate;
    }

    public String getContractid() {
        return contractid;
    }

    public void setContractid(String contractid) {
        this.contractid = contractid;
    }

    public String getContractname() {
        return contractname;
    }

    public void setContractname(String contractname) {
        this.contractname = contractname;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getContractorname() {
        return contractorname;
    }

    public void setContractorname(String contractorname) {
        this.contractorname = contractorname;
    }

    public String getContractorteename() {
        return contractorteename;
    }

    public void setContractorteename(String contractorteename) {
        this.contractorteename = contractorteename;
    }

    public Date getContractstartdate() {
        return contractstartdate;
    }

    public void setContractstartdate(Date contractstartdate) {
        this.contractstartdate = contractstartdate;
    }

    public MasterItem getContractstatus() {
        return contractstatus;
    }

    public void setContractstatus(MasterItem contractstatus) {
        this.contractstatus = contractstatus;
    }

    public String getContractterm() {
        return contractterm;
    }

    public void setContractterm(String contractterm) {
        this.contractterm = contractterm;
    }

    public MasterItem getCountryaggrement() {
        return countryaggrement;
    }

    public void setCountryaggrement(MasterItem countryaggrement) {
        this.countryaggrement = countryaggrement;
    }

    public Date getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(Date creationdate) {
        this.creationdate = creationdate;
    }

    public Date getDateofaggrement() {
        return dateofaggrement;
    }

    public void setDateofaggrement(Date dateofaggrement) {
        this.dateofaggrement = dateofaggrement;
    }

    public boolean isDeleteflag() {
        return deleteflag;
    }

    public void setDeleteflag(boolean deleteflag) {
        this.deleteflag = deleteflag;
    }

    public String getDetailstype() {
        return detailstype;
    }

    public void setDetailstype(String detailstype) {
        this.detailstype = detailstype;
    }

    public String getDocumentrequiredremarks() {
        return documentrequiredremarks;
    }

    public void setDocumentrequiredremarks(String documentrequiredremarks) {
        this.documentrequiredremarks = documentrequiredremarks;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getPannumber() {
        return pannumber;
    }

    public void setPannumber(String pannumber) {
        this.pannumber = pannumber;
    }

    public MRPContract getParentcontractid() {
        return parentcontractid;
    }

    public void setParentcontractid(MRPContract parentcontractid) {
        this.parentcontractid = parentcontractid;
    }

    public String getParentcontractname() {
        return parentcontractname;
    }

    public void setParentcontractname(String parentcontractname) {
        this.parentcontractname = parentcontractname;
    }

    public PaymentMethod getPaymentmethodname() {
        return paymentmethodname;
    }

    public void setPaymentmethodname(PaymentMethod paymentmethodname) {
        this.paymentmethodname = paymentmethodname;
    }

    public Date getPaymenttermdate() {
        return paymenttermdate;
    }

    public void setPaymenttermdate(Date paymenttermdate) {
        this.paymenttermdate = paymenttermdate;
    }

//    public int getPaymenttermdays() {
//        return paymenttermdays;
//    }
//
//    public void setPaymenttermdays(int paymenttermdays) {
//        this.paymenttermdays = paymenttermdays;
//    }

    public Term getPaymenttermname() {
        return paymenttermname;
    }

    public void setPaymenttermname(Term paymenttermname) {
        this.paymenttermname = paymenttermname;
    }

    public String getPreviouscontractid() {
        return previouscontractid;
    }

    public void setPreviouscontractid(String previouscontractid) {
        this.previouscontractid = previouscontractid;
    }

    public MasterItem getSellertype() {
        return sellertype;
    }

    public void setSellertype(MasterItem sellertype) {
        this.sellertype = sellertype;
    }

    public String getShownincsorcp() {
        return shownincsorcp;
    }

    public void setShownincsorcp(String shownincsorcp) {
        this.shownincsorcp = shownincsorcp;
    }

    public String getStateaggrement() {
        return stateaggrement;
    }

    public void setStateaggrement(String stateaggrement) {
        this.stateaggrement = stateaggrement;
    }

    public String getTannumber() {
        return tannumber;
    }

    public void setTannumber(String tannumber) {
        this.tannumber = tannumber;
    }

    public boolean isAutogen() {
        return autogen;
    }

    public void setAutogen(boolean autogen) {
        this.autogen = autogen;
    }

    public String getDatepreffixvalue() {
        return datepreffixvalue;
    }

    public void setDatepreffixvalue(String datepreffixvalue) {
        this.datepreffixvalue = datepreffixvalue;
    }

    public String getDatesuffixvalue() {
        return datesuffixvalue;
    }

    public void setDatesuffixvalue(String datesuffixvalue) {
        this.datesuffixvalue = datesuffixvalue;
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

    public Set<MRPContractDetails> getRows() {
        return rows;
    }

    public void setRows(Set<MRPContractDetails> rows) {
        this.rows = rows;
    }

    public MRPContractCustomData getAccMRPContractCustomData() {
        return accMRPContractCustomData;
    }

    public void setAccMRPContractCustomData(MRPContractCustomData accMRPContractCustomData) {
        this.accMRPContractCustomData = accMRPContractCustomData;
    }
    
    public String getDateAfterPreffixValue() {
        return dateAfterPreffixValue;
    }

    public void setDateAfterPreffixValue(String dateAfterPreffixValue) {
        this.dateAfterPreffixValue = dateAfterPreffixValue;
    }
}
