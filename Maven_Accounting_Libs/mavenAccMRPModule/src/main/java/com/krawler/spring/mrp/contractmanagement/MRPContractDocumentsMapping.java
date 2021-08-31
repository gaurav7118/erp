/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class MRPContractDocumentsMapping {

    private String ID;
    private MRPContract mrpContractID;
    private MRPContractDocuments documentID;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public MRPContract getMrpContractID() {
        return mrpContractID;
    }

    public void setMrpContractID(MRPContract mrpContractID) {
        this.mrpContractID = mrpContractID;
    }

    public MRPContractDocuments getDocumentID() {
        return documentID;
    }

    public void setDocumentID(MRPContractDocuments documentID) {
        this.documentID = documentID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
