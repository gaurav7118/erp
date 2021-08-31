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
public class PurchaseReturnDetailCustomDate extends AccCustomData {

    private String purchaseReturnDetailId;
    private PurchaseReturnDetail purchaseReturnDetail;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public PurchaseReturnDetail getPurchaseReturnDetail() {
        return purchaseReturnDetail;
    }

    public void setPurchaseReturnDetail(PurchaseReturnDetail purchaseReturnDetail) {
        this.purchaseReturnDetail = purchaseReturnDetail;
    }

    public String getPurchaseReturnDetailId() {
        return purchaseReturnDetailId;
    }

    public void setPurchaseReturnDetailId(String purchaseReturnDetailId) {
        this.purchaseReturnDetailId = purchaseReturnDetailId;
    }
}
