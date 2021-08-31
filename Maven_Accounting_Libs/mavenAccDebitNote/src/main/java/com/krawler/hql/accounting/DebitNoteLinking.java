/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class DebitNoteLinking {
    
    private String id;
    private int ModuleID;
    private String LinkedDocNo;
    private String LinkedDocID;
    private int SourceFlag;
    DebitNote DocID;

    public DebitNote getDocID() {
        return DocID;
    }

    public void setDocID(DebitNote DocID) {
        this.DocID = DocID;
    }

    public String getLinkedDocID() {
        return LinkedDocID;
    }

    public void setLinkedDocID(String LinkedDocID) {
        this.LinkedDocID = LinkedDocID;
    }

    public String getLinkedDocNo() {
        return LinkedDocNo;
    }

    public void setLinkedDocNo(String LinkedDocNo) {
        this.LinkedDocNo = LinkedDocNo;
    }

    public int getModuleID() {
        return ModuleID;
    }

    public void setModuleID(int ModuleID) {
        this.ModuleID = ModuleID;
    }

    public int getSourceFlag() {
        return SourceFlag;
    }

    public void setSourceFlag(int SourceFlag) {
        this.SourceFlag = SourceFlag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    
}
