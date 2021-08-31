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
import java.util.Set;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author krawler
 */
public class ExpenseGRDetail implements Comparable<ExpenseGRDetail> {

    private String ID;
    private int srno;
    private Account account;
    private double rate;
    private double amount;
    private Discount discount;
    private GoodsReceipt goodsReceipt;
    private Tax tax;
    private boolean isdebit;
    private double rateExcludingGst;//This varialbe save the rate Excluding GST so renamed this as rateExcludingGst to avoid confusion.
    private double rowTaxAmount;
    private boolean wasRowTaxFieldEditable;// this field has been added after making row tax field editable its value will be set always true from its implementation. i.e from 28-jan-2014.REASON -  when Row Tax Amount field was not editable then tax calculation was taking place according to tax percent, as selected From Tax combo in JS Side.
    private Company company;
    private String description;
    private JournalEntryDetail purchaseJED; // To map ExpenseGR detail to related JED 
    private JournalEntryDetail gstJED; // To map GST to related JED
    private ExpensePODetail expensePODetail; // For linking information with expense PO
    private double lineLevelTermAmount;
    private double gstCurrencyRate;//Only used if the country is Singapore and the base currency is not SGD.
    private double tdsAssessableAmount;
    private MasterItem natureOfPayment;
    private int tdsRuleId;
    private double tdsRate;
    private double tdsLineAmount;
    private int tdsPaidFlag;
    private String tdsPayment;
    private int tdsInterestPaidFlag;// To Verify whether TDS Interest is paid or not.
    private String tdsInterestPayment;// If paid then respective TDS Interest Payment id.
    private Account tdsPayableAccount;// Used for INDIA country
    private TdsJEMapping tdsJEMapping;// Used for INDIA country
    private Set<GoodsReceiptDetailPaymentMapping> goodsReceiptDetailPaymentMapping;
    private double tdsInterestRateAtPaymentTime;
    
    private JournalEntryDetail landedInvoiceJED; // Used for Manual Landed Cost Category 
    private boolean isUserModifiedTaxAmount;//ERM-1085 - To identify row tax amount is user modified or system calculated according to adaptive rounding algo.

    /**
     * Overriding compareTo method to provide implementation Sorting objects
     * according to srno in ascending order.
     *
     * @param expenseGRDetail
     */
    @Override
    public int compareTo(ExpenseGRDetail expenseGRDetail) {
        int srNo = expenseGRDetail.getSrno();
        if (this.srno == srNo) {
            return 0;
        } else if (this.srno > srNo) {
            return 1;
        } else {
            return -1;
        }
    }
    
    public JournalEntryDetail getLandedInvoiceJED() {
        return landedInvoiceJED;
    }

    public void setLandedInvoiceJED(JournalEntryDetail landedInvoiceJED) {
        this.landedInvoiceJED = landedInvoiceJED;
    }
    
    public ExpensePODetail getExpensePODetail() {
        return expensePODetail;
    }

    public void setExpensePODetail(ExpensePODetail expensePODetail) {
        this.expensePODetail = expensePODetail;
    }
    
    public GoodsReceipt getGoodsReceipt() {
        return goodsReceipt;
    }

    public void setGoodsReceipt(GoodsReceipt goodsReceipt) {
        this.goodsReceipt = goodsReceipt;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public double getRowTaxAmount() {
        return rowTaxAmount;
    }

    public void setRowTaxAmount(double rowTaxAmount) {
        this.rowTaxAmount = rowTaxAmount;
    }

    public boolean isWasRowTaxFieldEditable() {
        return wasRowTaxFieldEditable;
    }

    public void setWasRowTaxFieldEditable(boolean wasRowTaxFieldEditable) {
        this.wasRowTaxFieldEditable = wasRowTaxFieldEditable;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public String getDescription() {
        return description;
    }

    public String getEXPIDescription() {
        return getDescription();
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public JournalEntryDetail getGstJED() {
        return gstJED;
    }

    public void setGstJED(JournalEntryDetail gstJED) {
        this.gstJED = gstJED;
    }

    public JournalEntryDetail getPurchaseJED() {
        return purchaseJED;
    }

    public void setPurchaseJED(JournalEntryDetail purchaseJED) {
        this.purchaseJED = purchaseJED;
    }

    /**
     * @return the isdebit
     */
    public boolean isIsdebit() {
        return isdebit;
    }

    /**
     * @param isdebit the isdebit to set
     */
    public void setIsdebit(boolean isdebit) {
        this.isdebit = isdebit;
    }

    public double getRateExcludingGst() {
        return rateExcludingGst;
    }

    public void setRateExcludingGst(double rateExcludingGst) {
        this.rateExcludingGst = rateExcludingGst;
    }

    public double getLineLevelTermAmount() {
        return lineLevelTermAmount;
    }

    public void setLineLevelTermAmount(double lineLevelTermAmount) {
        this.lineLevelTermAmount = lineLevelTermAmount;
    }

    public double getGstCurrencyRate() {
        return gstCurrencyRate;
    }
 
    public void setGstCurrencyRate(double gstCurrencyRate) {
        this.gstCurrencyRate = gstCurrencyRate;
    }

    public double getTdsAssessableAmount() {
        return tdsAssessableAmount;
    }

    public void setTdsAssessableAmount(double tdsAssessableAmount) {
        this.tdsAssessableAmount = tdsAssessableAmount;
    }

    public MasterItem getNatureOfPayment() {
        return natureOfPayment;
    }

    public void setNatureOfPayment(MasterItem natureOfPayment) {
        this.natureOfPayment = natureOfPayment;
    }

    public int getTdsRuleId() {
        return tdsRuleId;
    }

    public void setTdsRuleId(int tdsRuleId) {
        this.tdsRuleId = tdsRuleId;
    }

    public double getTdsRate() {
        return tdsRate;
    }

    public void setTdsRate(double tdsRate) {
        this.tdsRate = tdsRate;
    }

    public double getTdsLineAmount() {
        return tdsLineAmount;
    }

    public void setTdsLineAmount(double tdsLineAmount) {
        this.tdsLineAmount = tdsLineAmount;
    }

    public int getTdsPaidFlag() {
        return tdsPaidFlag;
    }

    public void setTdsPaidFlag(int tdsPaidFlag) {
        this.tdsPaidFlag = tdsPaidFlag;
    }

    public String getTdsPayment() {
        return tdsPayment;
    }

    public void setTdsPayment(String tdsPayment) {
        this.tdsPayment = tdsPayment;
    }

    public int getTdsInterestPaidFlag() {
        return tdsInterestPaidFlag;
    }

    public void setTdsInterestPaidFlag(int tdsInterestPaidFlag) {
        this.tdsInterestPaidFlag = tdsInterestPaidFlag;
    }

    public String getTdsInterestPayment() {
        return tdsInterestPayment;
    }

    public void setTdsInterestPayment(String tdsInterestPayment) {
        this.tdsInterestPayment = tdsInterestPayment;
    }

    public Account getTdsPayableAccount() {
        return tdsPayableAccount;
    }

    public void setTdsPayableAccount(Account tdsPayableAccount) {
        this.tdsPayableAccount = tdsPayableAccount;
    }

    public TdsJEMapping getTdsJEMapping() {
        return tdsJEMapping;
    }

    public void setTdsJEMapping(TdsJEMapping tdsJEMapping) {
        this.tdsJEMapping = tdsJEMapping;
    }

    public Set<GoodsReceiptDetailPaymentMapping> getGoodsReceiptDetailPaymentMapping() {
        return goodsReceiptDetailPaymentMapping;
    }

    public void setGoodsReceiptDetailPaymentMapping(Set<GoodsReceiptDetailPaymentMapping> goodsReceiptDetailPaymentMapping) {
        this.goodsReceiptDetailPaymentMapping = goodsReceiptDetailPaymentMapping;
    }

    public double getTdsInterestRateAtPaymentTime() {
        return tdsInterestRateAtPaymentTime;
    }

    public void setTdsInterestRateAtPaymentTime(double tdsInterestRateAtPaymentTime) {
        this.tdsInterestRateAtPaymentTime = tdsInterestRateAtPaymentTime;
    }

    public boolean isIsUserModifiedTaxAmount() {
        return isUserModifiedTaxAmount;
    }

    public void setIsUserModifiedTaxAmount(boolean isUserModifiedTaxAmount) {
        this.isUserModifiedTaxAmount = isUserModifiedTaxAmount;
    }
    
}
