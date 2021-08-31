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
public class GoodsReceiptOrderDetailsCustomDate extends AccCustomData {

    private String goodsReceiptOrderDetailsId;
    private GoodsReceiptOrderDetails goodsReceiptOrderDetails;
    private String moduleId;

    public GoodsReceiptOrderDetails getGoodsReceiptOrderDetails() {
        return goodsReceiptOrderDetails;
    }

    public void setGoodsReceiptOrderDetails(GoodsReceiptOrderDetails goodsReceiptOrderDetails) {
        this.goodsReceiptOrderDetails = goodsReceiptOrderDetails;
    }

    public String getGoodsReceiptOrderDetailsId() {
        return goodsReceiptOrderDetailsId;
    }

    public void setGoodsReceiptOrderDetailsId(String goodsReceiptOrderDetailsId) {
        this.goodsReceiptOrderDetailsId = goodsReceiptOrderDetailsId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
