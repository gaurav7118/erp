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
public class OpeningBalanceVendorInvoiceCustomData extends AccCustomData{
    private String OpeningBalanceVendorInvoiceId;
    private GoodsReceipt OpeningBalanceVendorInvoice;
    private String moduleId;

    public GoodsReceipt getOpeningBalanceVendorInvoice() {
        return OpeningBalanceVendorInvoice;
    }

    public void setOpeningBalanceVendorInvoice(GoodsReceipt OpeningBalanceVendorInvoice) {
        this.OpeningBalanceVendorInvoice = OpeningBalanceVendorInvoice;
    }

    public String getOpeningBalanceVendorInvoiceId() {
        return OpeningBalanceVendorInvoiceId;
    }

    public void setOpeningBalanceVendorInvoiceId(String OpeningBalanceVendorInvoiceId) {
        this.OpeningBalanceVendorInvoiceId = OpeningBalanceVendorInvoiceId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
}
