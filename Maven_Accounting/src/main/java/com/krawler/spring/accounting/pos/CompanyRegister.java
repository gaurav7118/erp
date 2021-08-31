/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.pos;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import java.util.Date;
/**
 *
 * @author krawler
 */
public class CompanyRegister {

    private String ID;
    private String locationid;
    private String currencydenominationsjson;
    private int isopen;
//    private int isclosed;
    private Company company;
    private User userid;
    private Date transactionDate;
    private long transactionDateinLong;
    private double previousclosedbalance;
    private double openingamount;
    private double finalopeningamount;  
    private double addedamount; 
    private double byCash; 
    private double byCheque;
    private double byCard;
    private double byGiftCard; 
    private double variance; 
    private double closingamount;
    private double depositedamount;
    private double finalamount;
    private double cashoutamount;

    public long getTransactionDateinLong() {
        return transactionDateinLong;
    }

    public void setTransactionDateinLong(long transactionDateinLong) {
        this.transactionDateinLong = transactionDateinLong;
    }

    public String getCurrencydenominationsjson() {
        return currencydenominationsjson;
    }

    public void setCurrencydenominationsjson(String currencydenominationsjson) {
        this.currencydenominationsjson = currencydenominationsjson;
    }

//    public int getIsclosed() {
//        return isclosed;
//    }
//
//    public void setIsclosed(int isclosed) {
//        this.isclosed = isclosed;
//    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getAddedamount() {
        return addedamount;
    }

    public void setAddedamount(double addedamount) {
        this.addedamount = addedamount;
    }

    public double getByCard() {
        return byCard;
    }

    public void setByCard(double byCard) {
        this.byCard = byCard;
    }

    public double getByCheque() {
        return byCheque;
    }

    public void setByCheque(double byCheque) {
        this.byCheque = byCheque;
    }

    public double getByGiftCard() {
        return byGiftCard;
    }

    public void setByGiftCard(double byGiftCard) {
        this.byGiftCard = byGiftCard;
    }

    public double getByCash() {
        return byCash;
    }

    public void setByCash(double byCash) {
        this.byCash = byCash;
    }

    public double getCashoutamount() {
        return cashoutamount;
    }

    public void setCashoutamount(double cashoutamount) {
        this.cashoutamount = cashoutamount;
    }

    public double getClosingamount() {
        return closingamount;
    }

    public void setClosingamount(double closingamount) {
        this.closingamount = closingamount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public double getDepositedamount() {
        return depositedamount;
    }

    public void setDepositedamount(double depositedamount) {
        this.depositedamount = depositedamount;
    }

    public double getFinalamount() {
        return finalamount;
    }

    public void setFinalamount(double finalamount) {
        this.finalamount = finalamount;
    }

    public double getFinalopeningamount() {
        return finalopeningamount;
    }

    public void setFinalopeningamount(double finalopeningamount) {
        this.finalopeningamount = finalopeningamount;
    }

    public int getIsopen() {
        return isopen;
    }

    public void setIsopen(int isopen) {
        this.isopen = isopen;
    }

    public String getLocationid() {
        return locationid;
    }

    public void setLocationid(String locationid) {
        this.locationid = locationid;
    }

    public double getOpeningamount() {
        return openingamount;
    }

    public void setOpeningamount(double openingamount) {
        this.openingamount = openingamount;
    }

    public double getPreviousclosedbalance() {
        return previousclosedbalance;
    }

    public void setPreviousclosedbalance(double previousclosedbalance) {
        this.previousclosedbalance = previousclosedbalance;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    public User getUserid() {
        return userid;
    }

    public void setUserid(User userid) {
        this.userid = userid;
    }
}
