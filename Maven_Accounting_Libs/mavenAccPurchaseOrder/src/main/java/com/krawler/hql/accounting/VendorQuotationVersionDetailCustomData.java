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
public class VendorQuotationVersionDetailCustomData extends AccCustomData {

    private String quotationDetailId;
    private VendorQuotationVersionDetail quotationVersionDetail;
    private String moduleId;

    public VendorQuotationVersionDetail getQuotationVersionDetail() {
        return quotationVersionDetail;
    }

    public void setQuotationVersionDetail(VendorQuotationVersionDetail quotationVersionDetail) {
        this.quotationVersionDetail = quotationVersionDetail;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getQuotationDetailId() {
        return quotationDetailId;
    }

    public void setQuotationDetailId(String quotationDetailId) {
        this.quotationDetailId = quotationDetailId;
    }
}
