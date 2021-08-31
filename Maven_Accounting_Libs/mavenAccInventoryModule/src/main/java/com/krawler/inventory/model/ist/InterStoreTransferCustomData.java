/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class InterStoreTransferCustomData extends AccCustomData {
    
    private String ISTId;
    private InterStoreTransferRequest ISTRequest;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getISTId() {
        return ISTId;
    }

    public void setISTId(String ISTId) {
        this.ISTId = ISTId;
    }

    public InterStoreTransferRequest getISTRequest() {
        return ISTRequest;
    }

    public void setISTRequest(InterStoreTransferRequest ISTRequest) {
        this.ISTRequest = ISTRequest;
    }
    
}
