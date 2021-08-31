/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class PurchaseReturnCustomData extends AccCustomData {

    private String purchaseReturnId;
    private PurchaseReturn purchaseReturn;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public PurchaseReturn getPurchaseReturn() {
        return purchaseReturn;
    }

    public void setPurchaseReturn(PurchaseReturn purchaseReturn) {
        this.purchaseReturn = purchaseReturn;
    }

    public String getPurchaseReturnId() {
        return purchaseReturnId;
    }

    public void setPurchaseReturnId(String purchaseReturnId) {
        this.purchaseReturnId = purchaseReturnId;
    }
}
