/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class PurchaseOrderVersionCustomData extends AccountCustomData{
    
    private String poID;
    private PurchaseOrderVersion purchaseOrderVersion;
    private String moduleId;

    public String getPoID() {
        return poID;
    }

    public void setPoID(String poID) {
        this.poID = poID;
    }

    public PurchaseOrderVersion getPurchaseOrderVersion() {
        return purchaseOrderVersion;
    }

    public void setPurchaseOrderVersion(PurchaseOrderVersion purchaseOrderVersion) {
        this.purchaseOrderVersion = purchaseOrderVersion;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
}
