/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class InvoiceDocuments {

    private String ID;
    private String docID;
    private String docName;
    private String docType;
    private String crmDocumentID;           

    public String getCrmDocumentID() {                          //Get Document id from CRM
        return crmDocumentID;
    }

    public void setCrmDocumentID(String crmDocumentID) {        //Set CrmDocumenid value in ERP from docid of CRM document 
        this.crmDocumentID = crmDocumentID;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getDocName() {
        return docName;
    }

    public void setDocName(String docName) {
        this.docName = docName;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }
}
