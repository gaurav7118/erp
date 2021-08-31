/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class ClosingAccountBalance {

    String id;
    Account account;
    double amount;
    Date creationDate;
    Company company;
    YearLock yearLock;
    int yearId;
    boolean stockInHand;
    boolean netProfitAndLossWithStock;
    boolean netProfitAndLossWithOutStock;

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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getYearId() {
        return yearId;
    }

    public void setYearId(int yearId) {
        this.yearId = yearId;
    }

    public YearLock getYearLock() {
        return yearLock;
    }

    public void setYearLock(YearLock yearLock) {
        this.yearLock = yearLock;
    }

    public boolean isStockInHand() {
        return stockInHand;
    }

    public void setStockInHand(boolean stockInHand) {
        this.stockInHand = stockInHand;
    }

    public boolean isNetProfitAndLossWithStock() {
        return netProfitAndLossWithStock;
    }

    public void setNetProfitAndLossWithStock(boolean netProfitAndLossWithStock) {
        this.netProfitAndLossWithStock = netProfitAndLossWithStock;
    }

    public boolean isNetProfitAndLossWithOutStock() {
        return netProfitAndLossWithOutStock;
    }

    public void setNetProfitAndLossWithOutStock(boolean netProfitAndLossWithOutStock) {
        this.netProfitAndLossWithOutStock = netProfitAndLossWithOutStock;
    }

}
