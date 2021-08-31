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
public class AccJEDetailsProductCustomData extends AccCustomData {

    private String jedetailId;
    private JournalEntryDetail jedetail;
    private String recdetailId;
    private String productId;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getJedetailId() {
        return jedetailId;
    }

    public void setJedetailId(String jedetailId) {
        this.jedetailId = jedetailId;
    }

    public JournalEntryDetail getJedetail() {
        return jedetail;
    }

    public void setJedetail(JournalEntryDetail jedetail) {
        this.jedetail = jedetail;
    }

    public String getRecdetailId() {
        return recdetailId;
    }

    public void setRecdetailId(String recdetailId) {
        this.recdetailId = recdetailId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
