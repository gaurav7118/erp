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
public class QuotationDetailCustomData extends AccCustomData {

    private String quotationDetailId;
    private QuotationDetail quotationDetail;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public QuotationDetail getQuotationDetail() {
        return quotationDetail;
    }

    public void setQuotationDetail(QuotationDetail quotationDetail) {
        this.quotationDetail = quotationDetail;
    }

    public String getQuotationDetailId() {
        return quotationDetailId;
    }

    public void setQuotationDetailId(String quotationDetailId) {
        this.quotationDetailId = quotationDetailId;
    }
}
