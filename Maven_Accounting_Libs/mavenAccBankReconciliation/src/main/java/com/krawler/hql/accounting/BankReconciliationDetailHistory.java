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
 * @author krawler
 */
public class BankReconciliationDetailHistory {    
    private String ID;
    private BankReconciliation bankReconciliation;
    private Company company;
    private Date reconciledate;
    private Date date;
    private String accountname;
    private String paidto;
    private String chequeno;
    private Date chequedate;
    private String description;
    private String entryno;
    private String jeid;
    private String transactionid;
    private String transactionNumber;        
    private double amountintransactioncurrency;
    private double amountinacc;
    private double amount;
    private boolean debit;
    private boolean isopeningtransaction;
    private int moduleid;
    private int clearedstatus;
    private String reportname;
    private String transcurrsymbol;
    private String accountcurrencysymbol;    

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
    
    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber(String transactionNumber) {
        this.transactionNumber = transactionNumber;
    }
    
    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmountinacc() {
        return amountinacc;
    }

    public void setAmountinacc(double amountinacc) {
        this.amountinacc = amountinacc;
    }

    public double getAmountintransactioncurrency() {
        return amountintransactioncurrency;
    }

    public void setAmountintransactioncurrency(double amountintransactioncurrency) {
        this.amountintransactioncurrency = amountintransactioncurrency;
    }

    public BankReconciliation getBankReconciliation() {
        return bankReconciliation;
    }

    public void setBankReconciliation(BankReconciliation bankReconciliation) {
        this.bankReconciliation = bankReconciliation;
    }

    public Date getChequedate() {
        return chequedate;
    }

    public void setChequedate(Date chequedate) {
        this.chequedate = chequedate;
    }

    public String getChequeno() {
        return chequeno;
    }

    public void setChequeno(String chequeno) {
        this.chequeno = chequeno;
    }

    public int getClearedstatus() {
        return clearedstatus;
    }

    public void setClearedstatus(int clearedstatus) {
        this.clearedstatus = clearedstatus;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isDebit() {
        return debit;
    }

    public void setDebit(boolean debit) {
        this.debit = debit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEntryno() {
        return entryno;
    }

    public void setEntryno(String entryno) {
        this.entryno = entryno;
    }

    public boolean isIsopeningtransaction() {
        return isopeningtransaction;
    }

    public void setIsopeningtransaction(boolean isopeningtransaction) {
        this.isopeningtransaction = isopeningtransaction;
    }

    public String getJeid() {
        return jeid;
    }

    public void setJeid(String jeid) {
        this.jeid = jeid;
    }

    public int getModuleid() {
        return moduleid;
    }

    public void setModuleid(int moduleid) {
        this.moduleid = moduleid;
    }

    public String getPaidto() {
        return paidto;
    }

    public void setPaidto(String paidto) {
        this.paidto = paidto;
    }

    public Date getReconciledate() {
        return reconciledate;
    }

    public void setReconciledate(Date reconciledate) {
        this.reconciledate = reconciledate;
    }

    public String getTransactionid() {
        return transactionid;
    }

    public void setTransactionid(String transactionid) {
        this.transactionid = transactionid;
    }
    
    public String getReportname() {
        return reportname;
    }

    public void setReportname(String reportname) {
        this.reportname = reportname;
    }
    
    public String getAccountcurrencysymbol() {
        return accountcurrencysymbol;
    }

    public void setAccountcurrencysymbol(String accountcurrencysymbol) {
        this.accountcurrencysymbol = accountcurrencysymbol;
    }

    public String getTranscurrsymbol() {
        return transcurrsymbol;
    }

    public void setTranscurrsymbol(String transcurrsymbol) {
        this.transcurrsymbol = transcurrsymbol;
    }
}
