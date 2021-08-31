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
public class SalesOrderDetailsCustomData extends AccCustomData {

    private String soDetailID;
    private SalesOrderDetail salesorderDetail;
    private String moduleId;

    public String getSoDetailID() {
        return soDetailID;
    }

    public void setSoDetailID(String soDetailID) {
        this.soDetailID = soDetailID;
    }

    public SalesOrderDetail getSalesorderDetail() {
        return salesorderDetail;
    }

    public void setSalesorderDetail(SalesOrderDetail salesorderDetail) {
        this.salesorderDetail = salesorderDetail;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
}
