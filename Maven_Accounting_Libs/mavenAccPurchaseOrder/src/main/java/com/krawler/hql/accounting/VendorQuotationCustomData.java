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
public class VendorQuotationCustomData extends AccCustomData {

    private String vendorQuotationId;
    private VendorQuotation vendorQuotation;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public VendorQuotation getVendorQuotation() {
        return vendorQuotation;
    }

    public void setVendorQuotation(VendorQuotation vendorQuotation) {
        this.vendorQuotation = vendorQuotation;
    }

    public String getVendorQuotationId() {
        return vendorQuotationId;
    }

    public void setVendorQuotationId(String vendorQuotationId) {
        this.vendorQuotationId = vendorQuotationId;
    }
}
