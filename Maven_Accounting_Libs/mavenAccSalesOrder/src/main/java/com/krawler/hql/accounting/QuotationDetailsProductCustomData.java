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
public class QuotationDetailsProductCustomData extends AccCustomData{
    
    private String cqDetailID;
    private QuotationDetail cqProductCustomData;
    private String moduleId;

    public String getCqDetailID() {
        return cqDetailID;
    }

    public void setCqDetailID(String cqDetailID) {
        this.cqDetailID = cqDetailID;
    }

    public QuotationDetail getCqProductCustomData() {
        return cqProductCustomData;
    }

    public void setCqProductCustomData(QuotationDetail cqProductCustomData) {
        this.cqProductCustomData = cqProductCustomData;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
}
