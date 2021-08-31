/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;

/**
 *
 * @author krawler-user
 */
public class DebitNoteDetail {

    private String ID;
    private int srno;
    private Discount discount;
    private GoodsReceiptDetail goodsReceiptRow;
    private Inventory inventory;
    private DebitNote debitNote;
    private String memo;
    private double quantity;
    private Double taxAmount;
    private Company company;
    private Double totalDiscount;
    private GoodsReceipt goodsReceipt;
    private String creditNoteId;//Used for DNs linked with CNs
    private String remark;
    private int paidinvflag;//0 - for DN against unpaid invoice,  1 - for DN against paid invoice 
    private String invstoreid;
    private String invlocid;
    private double amountToAdjust;
    private double taxAmountToAdjust;
    private double adjustedAmount;
    private String linkedGainLossJE;
    private double exchangeRateForTransaction;
    private InvoiceDetail invoiceRow;    
    private Invoice invoice;
    private JournalEntryDetail totalJED; // To map DebitNoteDetail to related JED 
    private JournalEntryDetail gstJED; // To map GST to related JED
    private Date grLinkDate; //it is max(PI date,DN date, current date). this field used to calculate due of DN on runtime for Aged/SOA report
    private String revalJeId;  //for maintaing relation between realised JE and Debit Note 
    private String revalJeIdInvoice; //for maintaing relation between realised JE and Invoice 
    
    public Date getGrLinkDate() {
        return grLinkDate;
    }

    public void setGrLinkDate(Date grLinkDate) {
        this.grLinkDate = grLinkDate;
    }
    
    public double getAdjustedAmount() {
        return adjustedAmount;
    }

    public void setAdjustedAmount(double adjustedAmount) {
        this.adjustedAmount = adjustedAmount;
    }

    public double getAmountToAdjust() {
        return amountToAdjust;
    }

    public void setAmountToAdjust(double amountToAdjust) {
        this.amountToAdjust = amountToAdjust;
    }

    public double getTaxAmountToAdjust() {
        return taxAmountToAdjust;
    }

    public void setTaxAmountToAdjust(double taxAmountToAdjust) {
        this.taxAmountToAdjust = taxAmountToAdjust;
    }
    
    public int getPaidinvflag() {
        return paidinvflag;
    }

    public void setPaidinvflag(int paidinvflag) {
        this.paidinvflag = paidinvflag;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public DebitNote getDebitNote() {
        return debitNote;
    }

    public void setDebitNote(DebitNote debitNote) {
        this.debitNote = debitNote;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public GoodsReceiptDetail getGoodsReceiptRow() {
        return goodsReceiptRow;
    }

    public void setGoodsReceiptRow(GoodsReceiptDetail goodsReceiptRow) {
        this.goodsReceiptRow = goodsReceiptRow;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(Double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public GoodsReceipt getGoodsReceipt() {
        return goodsReceipt;
    }

    public void setGoodsReceipt(GoodsReceipt goodsReceipt) {
        this.goodsReceipt = goodsReceipt;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getInvstoreid() {
        return invstoreid;
    }

    public void setInvstoreid(String invstoreid) {
        this.invstoreid = invstoreid;
    }

    public String getInvlocid() {
        return invlocid;
    }

    public void setInvlocid(String invlocid) {
        this.invlocid = invlocid;
    }
    public String getLinkedGainLossJE() {
        return linkedGainLossJE;
}

    public void setLinkedGainLossJE(String linkedGainLossJE) {
        this.linkedGainLossJE = linkedGainLossJE;
    }

    public double getExchangeRateForTransaction() {
        return exchangeRateForTransaction;
    }

    public void setExchangeRateForTransaction(double exchangeRateForTransaction) {
        this.exchangeRateForTransaction = exchangeRateForTransaction;
    }

    public JournalEntryDetail getGstJED() {
        return gstJED;
    }

    public void setGstJED(JournalEntryDetail gstJED) {
        this.gstJED = gstJED;
    }

    public JournalEntryDetail getTotalJED() {
        return totalJED;
    }

    public void setTotalJED(JournalEntryDetail totalJED) {
        this.totalJED = totalJED;
    }

    public InvoiceDetail getInvoiceRow() {
        return invoiceRow;
    }

    public void setInvoiceRow(InvoiceDetail invoiceRow) {
        this.invoiceRow = invoiceRow;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public String getRevalJeId() {
        return revalJeId;
    }

    public void setRevalJeId(String revalJeId) {
        this.revalJeId = revalJeId;
    }

    public String getRevalJeIdInvoice() {
        return revalJeIdInvoice;
    }

    public void setRevalJeIdInvoice(String revalJeIdInvoice) {
        this.revalJeIdInvoice = revalJeIdInvoice;
    }

    public String getCreditNoteId() {
        return creditNoteId;
    }

    public void setCreditNoteId(String creditNoteId) {
        this.creditNoteId = creditNoteId;
    }
    
}
