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
public class RequestForQuotationDetailCustomData extends AccCustomData{
    private String requestForQuotationDetailId;
    private RequestForQuotationDetail requestForQuotationDetail;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public RequestForQuotationDetail getRequestForQuotationDetail() {
        return requestForQuotationDetail;
    }

    public void setRequestForQuotationDetail(RequestForQuotationDetail requestForQuotationDetail) {
        this.requestForQuotationDetail = requestForQuotationDetail;
    }

  
    public String getRequestForQuotationDetailId() {
        return requestForQuotationDetailId;
    }

    public void setRequestForQuotationDetailId(String requestForQuotationDetailId) {
        this.requestForQuotationDetailId = requestForQuotationDetailId;
    }
    
    
}
