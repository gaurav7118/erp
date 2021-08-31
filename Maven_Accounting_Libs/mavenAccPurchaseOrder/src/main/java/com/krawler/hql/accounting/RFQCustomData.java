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
public class RFQCustomData extends AccCustomData{
    
    private String rfqId;
    private RequestForQuotation requestForQuotation;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public RequestForQuotation getRequestForQuotation() {
        return requestForQuotation;
    }

    public void setRequestForQuotation(RequestForQuotation requestForQuotation) {
        this.requestForQuotation = requestForQuotation;
    }

    public String getrfqId() {
        return rfqId;
    }

    public void setrfqId(String rfqId) {
        this.rfqId = rfqId;
    }

  

    
}
