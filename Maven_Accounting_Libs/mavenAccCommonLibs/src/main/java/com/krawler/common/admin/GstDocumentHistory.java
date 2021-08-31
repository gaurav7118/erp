/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author Suhas C
 * Save Customer/Vendor GST fields data at transaction level.
 */
public class GstDocumentHistory {

    private String id;
    private String custvenTypeId;  // Customer / Vendor Type
    private String gstrType;  //  Customer / Vendor GSTR type
    private String gstin;  //  Customer / Vendor GSTR type
    private String refDocId;   // Transaction Id i.e. Quoation id 
    private int moduleId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustvenTypeId() {
        return custvenTypeId;
    }

    public void setCustvenTypeId(String custvenTypeId) {
        this.custvenTypeId = custvenTypeId;
    }

    public String getGstrType() {
        return gstrType;
    }

    public void setGstrType(String gstrType) {
        this.gstrType = gstrType;
    }

    public String getGstin() {
        return gstin;
    }

    public void setGstin(String gstin) {
        this.gstin = gstin;
    }

    public String getRefDocId() {
        return refDocId;
    }

    public void setRefDocId(String refDocId) {
        this.refDocId = refDocId;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

}
