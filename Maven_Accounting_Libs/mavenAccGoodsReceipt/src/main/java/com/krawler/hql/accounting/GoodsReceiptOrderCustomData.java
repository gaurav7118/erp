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
public class GoodsReceiptOrderCustomData extends AccCustomData {

    private String goodsReceiptOrderId;
    private GoodsReceiptOrder goodsReceiptOrder;
    private String moduleId;

    public String getGoodsReceiptOrderId() {
        return goodsReceiptOrderId;
    }

    public void setGoodsReceiptOrderId(String goodsReceiptOrderId) {
        this.goodsReceiptOrderId = goodsReceiptOrderId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public GoodsReceiptOrder getGoodsReceiptOrder() {
        return goodsReceiptOrder;
    }

    public void setGoodsReceiptOrder(GoodsReceiptOrder goodsReceiptOrder) {
        this.goodsReceiptOrder = goodsReceiptOrder;
    }
}
