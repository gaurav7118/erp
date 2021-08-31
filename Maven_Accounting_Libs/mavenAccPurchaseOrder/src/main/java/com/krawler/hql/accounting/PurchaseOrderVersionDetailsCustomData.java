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
public class PurchaseOrderVersionDetailsCustomData extends AccCustomData {

    private String poversionDetailID;
    private PurchaseOrderVersionDetails purchaseorderversionDetail;
    private String moduleId;

    public String getPoversionDetailID() {
        return poversionDetailID;
    }

    public void setPoversionDetailID(String poversionDetailID) {
        this.poversionDetailID = poversionDetailID;
    }

    public PurchaseOrderVersionDetails getPurchaseorderversionDetail() {
        return purchaseorderversionDetail;
    }

    public void setPurchaseorderversionDetail(PurchaseOrderVersionDetails purchaseorderversionDetail) {
        this.purchaseorderversionDetail = purchaseorderversionDetail;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
