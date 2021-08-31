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
public class VendorQuotationDetailsProductCustomData extends AccCustomData{
    
    private String vqDetailID;
    private VendorQuotationDetail vqProductCustomData;
    private String moduleId;

    public String getVqDetailID() {
        return vqDetailID;
    }

    public void setVqDetailID(String vqDetailID) {
        this.vqDetailID = vqDetailID;
    }

    public VendorQuotationDetail getVqProductCustomData() {
        return vqProductCustomData;
    }

    public void setVqProductCustomData(VendorQuotationDetail vqProductCustomData) {
        this.vqProductCustomData = vqProductCustomData;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
}
