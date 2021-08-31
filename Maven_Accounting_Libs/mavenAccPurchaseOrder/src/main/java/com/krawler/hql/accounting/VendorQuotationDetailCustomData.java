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
public class VendorQuotationDetailCustomData extends AccCustomData {

    private String vendorQuotationDetailId;
    private VendorQuotationDetail vendorQuotationDetail;
    private String moduleId;

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public VendorQuotationDetail getVendorQuotationDetail() {
        return vendorQuotationDetail;
    }

    public void setVendorQuotationDetail(VendorQuotationDetail vendorQuotationDetail) {
        this.vendorQuotationDetail = vendorQuotationDetail;
    }

    public String getVendorQuotationDetailId() {
        return vendorQuotationDetailId;
    }

    public void setVendorQuotationDetailId(String vendorQuotationDetailId) {
        this.vendorQuotationDetailId = vendorQuotationDetailId;
    }
}
