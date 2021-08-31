/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author sagar
 */
public class PurchaseOrderDetailsCustomData extends AccCustomData {

    private String poDetailID;
    private PurchaseOrderDetail purchaseorderDetail;
    private String moduleId;

    public String getPoDetailID() {
        return poDetailID;
    }

    public void setPoDetailID(String poDetailID) {
        this.poDetailID = poDetailID;
    }

    public PurchaseOrderDetail getPurchaseorderDetail() {
        return purchaseorderDetail;
    }

    public void setPurchaseorderDetail(PurchaseOrderDetail purchaseorderDetail) {
        this.purchaseorderDetail = purchaseorderDetail;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
