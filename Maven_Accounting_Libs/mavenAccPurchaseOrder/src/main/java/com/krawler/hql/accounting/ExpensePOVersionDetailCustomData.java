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
public class ExpensePOVersionDetailCustomData extends AccCustomData{
    private String expensePOVersionDetailID;
    private ExpensePOVersionDetails expensePOVersionDetail;
    private String moduleId;
    private String recdetailId;

    public ExpensePOVersionDetails getExpensePOVersionDetail() {
        return expensePOVersionDetail;
    }

    public void setExpensePOVersionDetail(ExpensePOVersionDetails expensePOVersionDetail) {
        this.expensePOVersionDetail = expensePOVersionDetail;
    }

    public String getExpensePOVersionDetailID() {
        return expensePOVersionDetailID;
    }

    public void setExpensePOVersionDetailID(String expensePOVersionDetailID) {
        this.expensePOVersionDetailID = expensePOVersionDetailID;
    }
    
    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getRecdetailId() {
        return recdetailId;
    }

    public void setRecdetailId(String recdetailId) {
        this.recdetailId = recdetailId;
    }
}
