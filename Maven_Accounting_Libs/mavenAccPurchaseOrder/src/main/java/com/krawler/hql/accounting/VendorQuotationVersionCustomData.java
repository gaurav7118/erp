
package com.krawler.hql.accounting;

import com.krawler.common.admin.AccCustomData;

/**
 *
 * @author krawler
 */
public class VendorQuotationVersionCustomData extends AccCustomData {

    private String quotationId;
    private VendorQuotationVersion quotation;
    private String moduleId;

    public VendorQuotationVersion getQuotation() {
        return quotation;
    }

    public void setQuotation(VendorQuotationVersion quotation) {
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
