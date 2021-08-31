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
public class SalesOrderDetailProductCustomData extends AccCustomData{
    private String soDetailID;
    private SalesOrderDetail salesorderDetail;
    private String moduleId;
    private String recdetailId;
    private String productId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getRecdetailId() {
        return recdetailId;
    }

    public void setRecdetailId(String recdetailId) {
        this.recdetailId = recdetailId;
    }

    public SalesOrderDetail getSalesorderDetail() {
        return salesorderDetail;
    }

    public void setSalesorderDetail(SalesOrderDetail salesorderDetail) {
        this.salesorderDetail = salesorderDetail;
    }

    public String getSoDetailID() {
        return soDetailID;
    }

    public void setSoDetailID(String soDetailID) {
        this.soDetailID = soDetailID;
    }
    
}
