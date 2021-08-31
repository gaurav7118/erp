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
public class SalesReturnDetailCustomData extends AccCustomData {

    private String salesReturnDetailId;
    private SalesReturnDetail salesReturnDetail;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public SalesReturnDetail getSalesReturnDetail() {
        return salesReturnDetail;
    }

    public void setSalesReturnDetail(SalesReturnDetail salesReturnDetail) {
        this.salesReturnDetail = salesReturnDetail;
    }

    public String getSalesReturnDetailId() {
        return salesReturnDetailId;
    }

    public void setSalesReturnDetailId(String salesReturnDetailId) {
        this.salesReturnDetailId = salesReturnDetailId;
    }
}
