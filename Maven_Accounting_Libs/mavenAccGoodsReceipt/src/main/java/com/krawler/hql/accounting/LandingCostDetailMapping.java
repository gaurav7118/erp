/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.LandingCostCategory;

/**
 * Class is used to store landing cost detail mapping. Purchase Invoice is
 * linked to expense invoice (ERM-447).
 *
 * @author swapnil.khandre
 */
public class LandingCostDetailMapping {

    public static final String LANDING_COST_DETAIL_MAPPING_ID = "id";
    public static final String GOODSRECEIPT_DETAIL_ID = "grDetailID";
    public static final String EXPENSE_INVOICE_ID = "expenseInvoiceID";
    public static final String LANDING_COST = "amount";
    public static final String LANDING_CATEGORY_ID = "landingCategoryID";
    public static final String INVENTORY_JED = "inventoryJED";

    private String ID;
    private GoodsReceiptDetail goodsReceiptDetail;
    private GoodsReceipt expenseInvoice;
    private double amount;
    private JournalEntryDetail inventoryJED;
    private LandingCostCategory landingCostCategory;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public GoodsReceiptDetail getGoodsReceiptDetail() {
        return goodsReceiptDetail;
    }

    public void setGoodsReceiptDetail(GoodsReceiptDetail goodsReceiptDetail) {
        this.goodsReceiptDetail = goodsReceiptDetail;
    }

    public GoodsReceipt getExpenseInvoice() {
        return expenseInvoice;
    }

    public void setExpenseInvoice(GoodsReceipt expenseInvoice) {
        this.expenseInvoice = expenseInvoice;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public JournalEntryDetail getInventoryJED() {
        return inventoryJED;
    }

    public void setInventoryJED(JournalEntryDetail inventoryJED) {
        this.inventoryJED = inventoryJED;
    }

    public LandingCostCategory getLandingCostCategory() {
        return landingCostCategory;
    }

    public void setLandingCostCategory(LandingCostCategory landingCostCategory) {
        this.landingCostCategory = landingCostCategory;
    }
}
