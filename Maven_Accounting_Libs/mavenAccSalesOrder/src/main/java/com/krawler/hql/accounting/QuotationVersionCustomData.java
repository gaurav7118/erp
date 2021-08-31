
package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class QuotationVersionCustomData extends AccCustomData {

    private String quotationId;
    private QuotationVersion quotation;
    private String moduleId;
    
    public QuotationVersion getQuotation() {
        return quotation;
    }

    public void setQuotation(QuotationVersion quotation) {
        this.quotation = quotation;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(String quotationId) {
        this.quotationId = quotationId;
    }
}
