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
public class PackingDoList {

    private String ID;
    private String packNumber;
    private Date packingDate;
    private String memo;
    private String customer;
    private boolean deleted;
    private Set<PackingDoListDetail> rows;
    private Company company;
    private MasterItem status;
    private String customerid;
    private Date dateOfLc;
    private String letterOfCn;
    private String partialShipment;
    private String transhipment;
    private String portOfLoading;
    private String portOfDischarge;
    private String vessel;
    private String incoterms;

    public void setDateOfLc(Date dateOfLc) {
        this.dateOfLc = dateOfLc;
    }

    public void setIncoterms(String incoterms) {
        this.incoterms = incoterms;
    }

    public void setLetterOfCn(String letterOfCn) {
        this.letterOfCn = letterOfCn;
    }

    public void setPartialShipment(String partialShipment) {
        this.partialShipment = partialShipment;
    }

    public void setPortOfDischarge(String portOfDischarge) {
        this.portOfDischarge = portOfDischarge;
    }

    public void setPortOfLoading(String portOfLoading) {
        this.portOfLoading = portOfLoading;
    }

    public void setTranshipment(String transhipment) {
        this.transhipment = transhipment;
    }

    public void setVessel(String vessel) {
        this.vessel = vessel;
    }

    public Date getDateOfLc() {
        return dateOfLc;
    }

    public String getIncoterms() {
        return incoterms;
    }

    public String getLetterOfCn() {
        return letterOfCn;
    }

    public String getPartialShipment() {
        return partialShipment;
    }

    public String getPortOfDischarge() {
        return portOfDischarge;
    }

    public String getPortOfLoading() {
        return portOfLoading;
    }

    public String getTranshipment() {
        return transhipment;
    }

    public String getVessel() {
        return vessel;
    }
    
    public void setCustomerid(String customerid) {
        this.customerid = customerid;
    }

    public String getCustomerid() {
        return customerid;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setPackNumber(String packNumber) {
        this.packNumber = packNumber;
    }

    public void setPackingDate(Date packingDate) {
        this.packingDate = packingDate;
    }

    public void setRows(Set<PackingDoListDetail> rows) {
        this.rows = rows;
    }

    public void setStatus(MasterItem status) {
        this.status = status;
    }

    public String getID() {
        return ID;
    }

    public Company getCompany() {
        return company;
    }

    public String getCustomer() {
        return customer;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getMemo() {
        return memo;
    }

    public String getPackNumber() {
        return packNumber;
    }

    public Date getPackingDate() {
        return packingDate;
    }

    public Set<PackingDoListDetail> getRows() {
        return rows;
    }

    public MasterItem getStatus() {
        return status;
    }

}
