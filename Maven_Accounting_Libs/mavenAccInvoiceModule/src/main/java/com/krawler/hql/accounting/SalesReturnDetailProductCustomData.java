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
public class SalesReturnDetailProductCustomData extends AccCustomData{
    private String srDetailID;
    private SalesReturnDetail srProductCustomData;
    private String moduleId;

    public String getSrDetailID() {
        return srDetailID;
    }

    public void setSrDetailID(String srDetailID) {
        this.srDetailID = srDetailID;
    }

    public SalesReturnDetail getSrProductCustomData() {
        return srProductCustomData;
    }

    public void setSrProductCustomData(SalesReturnDetail srProductCustomData) {
        this.srProductCustomData = srProductCustomData;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
    
}
