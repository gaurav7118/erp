/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class DeliveryOrderDetailProductCustomData extends AccountCustomData{
    private String doDetailID;
    private DeliveryOrderDetail deliveryorderDetail;
    private String moduleId;
    private String recdetailId;
    private String productId;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public DeliveryOrderDetail getDeliveryorderDetail() {
        return deliveryorderDetail;
    }

    public void setDeliveryorderDetail(DeliveryOrderDetail deliveryorderDetail) {
        this.deliveryorderDetail = deliveryorderDetail;
    }

    public String getDoDetailID() {
        return doDetailID;
    }

    public void setDoDetailID(String doDetailID) {
        this.doDetailID = doDetailID;
    }

    public String getRecdetailId() {
        return recdetailId;
    }

    public void setRecdetailId(String recdetailId) {
        this.recdetailId = recdetailId;
    }
    @Override
    public String getModuleId() {
        return moduleId;
    }

    @Override
    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
