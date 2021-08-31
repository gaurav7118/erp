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
public class PurchaseOrderDetailProductCustomData extends AccCustomData {

    private String poDetailID;
    private PurchaseOrderDetail purchaseorderDetail;
    private String moduleId;
    private String recdetailId;
    private String productId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getPoDetailID() {
        return poDetailID;
    }

    public void setPoDetailID(String poDetailID) {
        this.poDetailID = poDetailID;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public PurchaseOrderDetail getPurchaseorderDetail() {
        return purchaseorderDetail;
    }

    public void setPurchaseorderDetail(PurchaseOrderDetail purchaseorderDetail) {
        this.purchaseorderDetail = purchaseorderDetail;
    }

    public String getRecdetailId() {
        return recdetailId;
    }

    public void setRecdetailId(String recdetailId) {
        this.recdetailId = recdetailId;
    }
}
