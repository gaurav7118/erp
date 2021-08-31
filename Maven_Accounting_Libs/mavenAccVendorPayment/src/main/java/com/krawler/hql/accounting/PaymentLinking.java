/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class PaymentLinking {
    private String id;
    private int ModuleID;
    private String LinkedDocNo;
    private String LinkedDocID;
    private int SourceFlag;
    Payment DocID;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getModuleID() {
        return ModuleID;
    }

    public void setModuleID(int ModuleID) {
        this.ModuleID = ModuleID;
    }

    public String getLinkedDocNo() {
        return LinkedDocNo;
    }

    public void setLinkedDocNo(String LinkedDocNo) {
        this.LinkedDocNo = LinkedDocNo;
    }

    public String getLinkedDocID() {
        return LinkedDocID;
    }

    public void setLinkedDocID(String LinkedDocID) {
        this.LinkedDocID = LinkedDocID;
    }

    public int getSourceFlag() {
        return SourceFlag;
    }

    public void setSourceFlag(int SourceFlag) {
        this.SourceFlag = SourceFlag;
    }

    public Payment getDocID() {
        return DocID;
    }

    public void setDocID(Payment DocID) {
        this.DocID = DocID;
    }
    
    
}
