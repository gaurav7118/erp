/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class LccManualWiseProductAmount {
    
    //Instance variable 
    private String ID;
    private GoodsReceiptDetail grdetailid;
    private GoodsReceipt expenseInvoiceid;
    private double percentage;
    private double amount;
    /**
     * Below fields are used for CUSTOMDUTY type of landed cost.
     */
    private double taxablevalueforcustomduty;
    private double customdutyandothercharges;
    private double taxablevalueforigst;
    private double igstrate;
    private double igstamount;
    private boolean customDutyAllocationType;
    /**
     * This will be used to saved asset Id for manual/custom duty expense
     * invoice. We show asset Id as product name for asset purchase invoice.
     */
    private AssetDetails assetDetails; 
    
    // Getter And Setter Method

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public GoodsReceipt getExpenseInvoiceid() {
        return expenseInvoiceid;
    }

    public void setExpenseInvoiceid(GoodsReceipt expenseInvoiceid) {
        this.expenseInvoiceid = expenseInvoiceid;
    }

    public GoodsReceiptDetail getGrdetailid() {
        return grdetailid;
    }

    public void setGrdetailid(GoodsReceiptDetail grdetailid) {
        this.grdetailid = grdetailid;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double getTaxablevalueforcustomduty() {
        return taxablevalueforcustomduty;
    }

    public void setTaxablevalueforcustomduty(double taxablevalueforcustomduty) {
        this.taxablevalueforcustomduty = taxablevalueforcustomduty;
    }

    public double getCustomdutyandothercharges() {
        return customdutyandothercharges;
    }

    public void setCustomdutyandothercharges(double customdutyandothercharges) {
        this.customdutyandothercharges = customdutyandothercharges;
    }

    public double getTaxablevalueforigst() {
        return taxablevalueforigst;
    }

    public void setTaxablevalueforigst(double taxablevalueforigst) {
        this.taxablevalueforigst = taxablevalueforigst;
    }

    public double getIgstrate() {
        return igstrate;
    }

    public void setIgstrate(double igstrate) {
        this.igstrate = igstrate;
    }

    public double getIgstamount() {
        return igstamount;
    }

    public void setIgstamount(double igstamount) {
        this.igstamount = igstamount;
    }

    public boolean isCustomDutyAllocationType() {
        return customDutyAllocationType;
    }

    public void setCustomDutyAllocationType(boolean customDutyAllocationType) {
        this.customDutyAllocationType = customDutyAllocationType;
    }

    public AssetDetails getAssetDetails() {
        return assetDetails;
    }

    public void setAssetDetails(AssetDetails assetDetails) {
        this.assetDetails = assetDetails;
    }
}
