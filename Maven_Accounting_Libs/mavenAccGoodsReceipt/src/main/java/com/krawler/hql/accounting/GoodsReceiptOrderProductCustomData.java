/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class GoodsReceiptOrderProductCustomData extends AccCustomData {

    private String grDetailID;
    private GoodsReceiptOrderDetails grProductCustomData;
    private String moduleId;

    public String getGrDetailID() {
        return grDetailID;
    }

    public void setGrDetailID(String grDetailID) {
        this.grDetailID = grDetailID;
    }

    public GoodsReceiptOrderDetails getGrProductCustomData() {
        return grProductCustomData;
    }

    public void setGrProductCustomData(GoodsReceiptOrderDetails grProductCustomData) {
        this.grProductCustomData = grProductCustomData;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

}
