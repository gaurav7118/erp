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
public class SalesReturnCustomData extends AccCustomData {

    private String salesReturnId;
    private SalesReturn salesReturn;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public SalesReturn getSalesReturn() {
        return salesReturn;
    }

    public void setSalesReturn(SalesReturn salesReturn) {
        this.salesReturn = salesReturn;
    }

    public String getSalesReturnId() {
        return salesReturnId;
    }

    public void setSalesReturnId(String salesReturnId) {
        this.salesReturnId = salesReturnId;
    }
}
