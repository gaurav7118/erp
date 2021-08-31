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
public class DeliveryOrderDetailCustomData extends AccCustomData {

    private String deliveryOrderDetailId;
    private DeliveryOrderDetail deliveryOrderDetail;
    private String moduleId;

    public DeliveryOrderDetail getDeliveryOrderDetail() {
        return deliveryOrderDetail;
    }

    public void setDeliveryOrderDetail(DeliveryOrderDetail deliveryOrderDetail) {
        this.deliveryOrderDetail = deliveryOrderDetail;
    }

    public String getDeliveryOrderDetailId() {
        return deliveryOrderDetailId;
    }

    public void setDeliveryOrderDetailId(String deliveryOrderDetailId) {
        this.deliveryOrderDetailId = deliveryOrderDetailId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
