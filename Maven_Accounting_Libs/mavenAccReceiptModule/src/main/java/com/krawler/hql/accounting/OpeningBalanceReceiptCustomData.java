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
public class OpeningBalanceReceiptCustomData extends AccCustomData{
    private String OpeningBalanceReceiptId;
    private Receipt OpeningBalanceReceipt;
    private String moduleId;

    public Receipt getOpeningBalanceReceipt() {
        return OpeningBalanceReceipt;
    }

    public void setOpeningBalanceReceipt(Receipt OpeningBalanceReceipt) {
        this.OpeningBalanceReceipt = OpeningBalanceReceipt;
    }

    public String getOpeningBalanceReceiptId() {
        return OpeningBalanceReceiptId;
    }

    public void setOpeningBalanceReceiptId(String OpeningBalanceReceiptId) {
        this.OpeningBalanceReceiptId = OpeningBalanceReceiptId;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }
    
    

}
