/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.math.BigInteger;

/**
 *
 * @author krawler
 */
public class ChequeSequenceFormat {

    private String id;
    private Account bankAccount;
    private BigInteger startFrom;
    private int numberOfDigits;
    private boolean showLeadingZero;
    private Company company;
    private String name;
    private boolean isactivate;
    private boolean isdefault;
    private BigInteger chequeEndNumber;
    private String prefix;
    private String suffix;
    private String dateformatinprefix;
    private boolean dateBeforePrefix;
    private String dateformatafterprefix;
    private boolean dateAfterPrefix;
    private String dateFormatAfterSuffix;
    private boolean showDateFormatAfterSuffix;
    private boolean resetCounter;

    public Account getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(Account bankAccount) {
        this.bankAccount = bankAccount;
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

    public int getNumberOfDigits() {
        return numberOfDigits;
    }

    public void setNumberOfDigits(int numberOfDigits) {
        this.numberOfDigits = numberOfDigits;
    }

    public boolean isShowLeadingZero() {
        return showLeadingZero;
    }

    public void setShowLeadingZero(boolean showLeadingZero) {
        this.showLeadingZero = showLeadingZero;
    }

    public BigInteger getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(BigInteger startFrom) {
        this.startFrom = startFrom;
    }
    
    public boolean isIsactivate() {
        return isactivate;
}

    public void setIsactivate(boolean isactivate) {
        this.isactivate = isactivate;
    }

    public BigInteger getChequeEndNumber() {
        return chequeEndNumber;
    }

    public void setChequeEndNumber(BigInteger chequeEndNumber) {
        this.chequeEndNumber = chequeEndNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isIsdefault() {
        return isdefault;
    }

    public void setIsdefault(boolean isdefault) {
        this.isdefault = isdefault;
    }
     public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDateformatinprefix() {
        return dateformatinprefix;
    }

    public void setDateformatinprefix(String dateformatinprefix) {
        this.dateformatinprefix = dateformatinprefix;
    }

    public boolean isDateBeforePrefix() {
        return dateBeforePrefix;
    }

    public void setDateBeforePrefix(boolean dateBeforePrefix) {
        this.dateBeforePrefix = dateBeforePrefix;
    }

    public String getDateformatafterprefix() {
        return dateformatafterprefix;
    }

    public void setDateformatafterprefix(String dateformatafterprefix) {
        this.dateformatafterprefix = dateformatafterprefix;
    }

    public boolean isDateAfterPrefix() {
        return dateAfterPrefix;
    }

    public void setDateAfterPrefix(boolean dateAfterPrefix) {
        this.dateAfterPrefix = dateAfterPrefix;
    }

    public String getDateFormatAfterSuffix() {
        return dateFormatAfterSuffix;
    }

    public void setDateFormatAfterSuffix(String dateFormatAfterSuffix) {
        this.dateFormatAfterSuffix = dateFormatAfterSuffix;
    }

    public boolean isShowDateFormatAfterSuffix() {
        return showDateFormatAfterSuffix;
    }

    public void setShowDateFormatAfterSuffix(boolean showDateFormatAfterSuffix) {
        this.showDateFormatAfterSuffix = showDateFormatAfterSuffix;
    }

    public boolean isResetCounter() {
        return resetCounter;
    }

    public void setResetCounter(boolean resetCounter) {
        this.resetCounter = resetCounter;
    }
}
