/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class BankReconcilationDraft {
    
    private String ID;
    private String description;
    private Account account;
    private double newstatementbalance;
    private Date fromdate;
    private Date todate;
    private User createdby;
    private User modifiedby;
    private long createdon;
    private long updatedon;
    private Company company;
    private double clearedChecksAmountinAcc;    //Total of Cleared Checks in Account Currency
    private double unclearedChecksAmountinAcc;    //Total of Uncleared Checks in Account Currency
    private double clearedDepositsAmountinAcc;    //Total of Cleared Deposits in Account Currency
    private double unclearedDepositsAmountinAcc;   //Total of Uncleared Checks in Account Currency
    private double bankBookBalanceinAcc;    //Bank Book Balance in Account Currency
    private double bankStmtBalanceinAcc;        //Balance As per Bank Statement
    private int paymentsReconciled;        //Number of payment items reconciled
    private int depositsReconciled;        //Number of deposit items reconciled
    private double clearingAmount;        //Total clearing amount

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Date getFromdate() {
        return fromdate;
    }

    public void setFromdate(Date fromdate) {
        this.fromdate = fromdate;
    }

    public double getNewstatementbalance() {
        return newstatementbalance;
    }

    public void setNewstatementbalance(double newstatementbalance) {
        this.newstatementbalance = newstatementbalance;
    }

    public Date getTodate() {
        return todate;
    }

    public void setTodate(Date todate) {
        this.todate = todate;
    }
    
    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public User getCreatedby() {
        return createdby;
    }

    public void setCreatedby(User createdby) {
        this.createdby = createdby;
    }

    public long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(long createdon) {
        this.createdon = createdon;
    }

    public User getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(User modifiedby) {
        this.modifiedby = modifiedby;
    }

    public long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(long updatedon) {
        this.updatedon = updatedon;
    }

    public double getBankBookBalanceinAcc() {
        return bankBookBalanceinAcc;
    }

    public void setBankBookBalanceinAcc(double bankBookBalanceinAcc) {
        this.bankBookBalanceinAcc = bankBookBalanceinAcc;
    }

    public double getBankStmtBalanceinAcc() {
        return bankStmtBalanceinAcc;
    }

    public void setBankStmtBalanceinAcc(double bankStmtBalanceinAcc) {
        this.bankStmtBalanceinAcc = bankStmtBalanceinAcc;
    }

    public double getClearedChecksAmountinAcc() {
        return clearedChecksAmountinAcc;
    }

    public void setClearedChecksAmountinAcc(double clearedChecksAmountinAcc) {
        this.clearedChecksAmountinAcc = clearedChecksAmountinAcc;
    }

    public double getClearedDepositsAmountinAcc() {
        return clearedDepositsAmountinAcc;
    }

    public void setClearedDepositsAmountinAcc(double clearedDepositsAmountinAcc) {
        this.clearedDepositsAmountinAcc = clearedDepositsAmountinAcc;
    }

    public double getUnclearedChecksAmountinAcc() {
        return unclearedChecksAmountinAcc;
    }

    public void setUnclearedChecksAmountinAcc(double unclearedChecksAmountinAcc) {
        this.unclearedChecksAmountinAcc = unclearedChecksAmountinAcc;
    }

    public double getUnclearedDepositsAmountinAcc() {
        return unclearedDepositsAmountinAcc;
    }

    public void setUnclearedDepositsAmountinAcc(double unclearedDepositsAmountinAcc) {
        this.unclearedDepositsAmountinAcc = unclearedDepositsAmountinAcc;
    }

    public double getClearingAmount() {
        return clearingAmount;
    }

    public void setClearingAmount(double clearingAmount) {
        this.clearingAmount = clearingAmount;
    }

    public int getDepositsReconciled() {
        return depositsReconciled;
    }

    public void setDepositsReconciled(int depositsReconciled) {
        this.depositsReconciled = depositsReconciled;
    }

    public int getPaymentsReconciled() {
        return paymentsReconciled;
    }

    public void setPaymentsReconciled(int paymentsReconciled) {
        this.paymentsReconciled = paymentsReconciled;
    }

}
