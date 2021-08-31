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
public class PurchaseRequisitionDetailCustomData extends AccCustomData{
    private String purchaseRequisitionDetailId;
    private PurchaseRequisitionDetail purchaseRequisitionDetail;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public PurchaseRequisitionDetail getPurchaseRequisitionDetail() {
        return purchaseRequisitionDetail;
    }

    public void setPurchaseRequisitionDetail(PurchaseRequisitionDetail purchaseRequisitionDetail) {
        this.purchaseRequisitionDetail = purchaseRequisitionDetail;
    }

    public String getPurchaseRequisitionDetailId() {
        return purchaseRequisitionDetailId;
    }

    public void setPurchaseRequisitionDetailId(String purchaseRequisitionDetailId) {
        this.purchaseRequisitionDetailId = purchaseRequisitionDetailId;
    }
    
}
