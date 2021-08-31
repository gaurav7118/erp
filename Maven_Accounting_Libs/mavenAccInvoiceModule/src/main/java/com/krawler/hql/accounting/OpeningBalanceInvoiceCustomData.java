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
public class OpeningBalanceInvoiceCustomData extends AccCustomData{
    private String OpeningBalanceInvoiceId;
    private Invoice OpeningBalanceInvoice;
    private String moduleId;

    public Invoice getOpeningBalanceInvoice() {
        return OpeningBalanceInvoice;
    }

    public void setOpeningBalanceInvoice(Invoice OpeningBalanceInvoice) {
        this.OpeningBalanceInvoice = OpeningBalanceInvoice;
    }

    public String getOpeningBalanceInvoiceId() {
        return OpeningBalanceInvoiceId;
    }

    public void setOpeningBalanceInvoiceId(String OpeningBalanceInvoiceId) {
        this.OpeningBalanceInvoiceId = OpeningBalanceInvoiceId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
}
