/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class AdvanceDetail {
    
  String id;
  Company company;
  double amount;
  double amountDue;
  private double tdsamount;// For India Country Specific.
  Payment payment;
  private double exchangeratefortransaction; // If Advance Payment then value = 1 and if made payment against customer's advance receipt then have actual exchangerate value 
  ReceiptAdvanceDetail receiptAdvanceDetails; // Used when made payment against customer's advance receipt. So need to maintain receiptadvancedetail object to refer advance receipt amount and amount due
  private String ROWJEDID;// Used only when custom 
  private String revalJeId;  //for maintaing relation between realised JE and Invoice 
  private String description;
  int srNoForRow;
  private boolean istdsamountusedingoodsreceipt;//Used for many debit and credit
  JournalEntryDetail totalJED; // To map AdvanceDetail to related JED
  private Set<TdsDetails> tdsdetails;
  private int tdsPaidFlag;
  private String tdsPayment;
  private int tdsInterestPaidFlag;// To Verify whether TDS Interest is paid or not.
  private String tdsInterestPayment;// If paid then respective TDS Interest Payment id.
  private double tdsInterestRateAtPaymentTime;
  
    /**
     * productId is used to store productid for india country when payment.
     */
  private String productId;
    /**
     * taxamount is used to store tax amount for india country when RCM is applicable in
     * payment.
     */
    private double taxamount;

    
    public int getSrNoForRow() {
        return srNoForRow;
    }

    public void setSrNoForRow(int srNoForRow) {
        this.srNoForRow = srNoForRow;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getROWJEDID() {
        return ROWJEDID;
    }

    public void setROWJEDID(String ROWJEDID) {
        this.ROWJEDID = ROWJEDID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getExchangeratefortransaction() {
        return exchangeratefortransaction;
    }

    public void setExchangeratefortransaction(double exchangeratefortransaction) {
        this.exchangeratefortransaction = exchangeratefortransaction;
    }

    public ReceiptAdvanceDetail getReceiptAdvanceDetails() {
        return receiptAdvanceDetails;
    }

    public void setReceiptAdvanceDetails(ReceiptAdvanceDetail receiptAdvanceDetails) {
        this.receiptAdvanceDetails = receiptAdvanceDetails;
    }

    public JournalEntryDetail getTotalJED() {
        return totalJED;
    }

    public void setTotalJED(JournalEntryDetail totalJED) {
        this.totalJED = totalJED;
    }

    public String getRevalJeId() {
        return revalJeId;
    }

    public void setRevalJeId(String revalJeId) {
        this.revalJeId = revalJeId;
    }
    public double getTdsamount() {
        return tdsamount;
    }
    public void setTdsamount(double tdsamount) {
        this.tdsamount = tdsamount;
    }

    public Set<TdsDetails> getTdsdetails() {
        return tdsdetails;
    }

    public void setTdsdetails(Set<TdsDetails> tdsdetails) {
        this.tdsdetails = tdsdetails;
    }

    public boolean isIstdsamountusedingoodsreceipt() {
        return istdsamountusedingoodsreceipt;
    }
    public void setIstdsamountusedingoodsreceipt(boolean istdsamountusedingoodsreceipt) {
        this.istdsamountusedingoodsreceipt = istdsamountusedingoodsreceipt;
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

    public String getTdsInterestPayment() {
        return tdsInterestPayment;
    }
    public void setTdsInterestPayment(String tdsInterestPayment) {
        this.tdsInterestPayment = tdsInterestPayment;
    }
    public int getTdsInterestPaidFlag() {
        return tdsInterestPaidFlag;
    }
    public void setTdsInterestPaidFlag(int tdsInterestPaidFlag) {
        this.tdsInterestPaidFlag = tdsInterestPaidFlag;
    }
    
    public double getTaxamount() {
        return taxamount;
    }

    public void setTaxamount(double taxamount) {
        this.taxamount = taxamount;
    }

    public double getTdsInterestRateAtPaymentTime() {
        return tdsInterestRateAtPaymentTime;
    }

    public void setTdsInterestRateAtPaymentTime(double tdsInterestRateAtPaymentTime) {
        this.tdsInterestRateAtPaymentTime = tdsInterestRateAtPaymentTime;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}
