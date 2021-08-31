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

/**
 *
 * @author krawler
 */
public class ReceiptAdvanceDetail {
    
  String id;
  Company company;
  double amount;
  double amountDue;
  Receipt receipt;
  private String ROWJEDID;// Used only when custom 
  private int advanceType;//Used for advance payment
  private String description;
  private double exchangeratefortransaction; // If Advance Payment then value = 1 and if received payment against vendor's advance payment then have actual exchangerate value 
  private String advancedetailid;// Used when received payment against vendor's advance payment. So need to maintain advancedetail id to refer advance payment amount and amount due
  private Tax GST;   // Used for receiving payment against customer, for Malaysia Country. 
  private String revalJeId;  //for maintaing relation between realised JE and Invoice 
  int srNoForRow;
  private double taxamount;
  private Tax tax;
  JournalEntryDetail totalJED; // To map ReceiptAdvanceDetail to related JED
  private Product product;  // To save advance taken for specified product.

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

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

    public Receipt getReceipt() {
        return receipt;
    }

    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }

    public String getROWJEDID() {
        return ROWJEDID;
    }

    public void setROWJEDID(String ROWJEDID) {
        this.ROWJEDID = ROWJEDID;
    }
    public int getAdvanceType() {
        return advanceType;
    }

    public void setAdvanceType(int advanceType) {
        this.advanceType = advanceType;
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

    public String getAdvancedetailid() {
        return advancedetailid;
    }

    public void setAdvancedetailid(String advancedetailid) {
        this.advancedetailid = advancedetailid;
    }
    
    public Tax getGST() {
        return GST;
    }

    public void setGST(Tax GST) {
        this.GST = GST;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public double getTaxamount() {
        return taxamount;
    }

    public void setTaxamount(double taxamount) {
        this.taxamount = taxamount;
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
    
}
