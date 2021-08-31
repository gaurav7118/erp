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
public class PurchaseReturnDetailProductCustomData extends AccCustomData{
    
    private String prDetailID;
    private PurchaseReturnDetail prProductCustomData;
    private String moduleId;

    public String getPrDetailID() {
        return prDetailID;
    }

    public void setPrDetailID(String prDetailID) {
        this.prDetailID = prDetailID;
    }

    public PurchaseReturnDetail getPrProductCustomData() {
        return prProductCustomData;
    }

    public void setPrProductCustomData(PurchaseReturnDetail prProductCustomData) {
        this.prProductCustomData = prProductCustomData;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
    
}
