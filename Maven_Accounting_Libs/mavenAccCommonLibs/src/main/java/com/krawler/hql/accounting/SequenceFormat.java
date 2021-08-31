/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

/**
 *
 * @author Ashim
 */
public class SequenceFormat {

    private String ID;
    private String name;
    private String prefix;
    private String suffix;
    private int numberofdigit;
    private int startfrom;
    private int moduleid;
    private String modulename;    
    private boolean showleadingzero;
    private boolean deleted;
    private Company company;
    private boolean isdefaultformat;
    private String dateformatinprefix;
    private boolean dateBeforePrefix;
    private String dateformatafterprefix;
    private boolean dateAfterPrefix;
    private String dateFormatAfterSuffix;
    private boolean showDateFormatAfterSuffix;
    private boolean resetCounter;
    private String custom;
//    private Company company;
    private boolean isactivate;

    public static final String RECEIVE_PAYMENT_MODULENAME = "autoreceipt";
    public static final String MAKE_PAYMENT_MODULENAME = "autopayment";
    public static final String GOODS_RECEIPT_ORDER_MODULENAME = "autogro";
    public static final String PURCHASE_ORDER_MODULENAME = "autopo";
    public static final String CREDIT_NOTE_MODULENAME = "autocreditmemo";
    public static final String JOURNAL_ENTRY_MODULENAME = "autojournalentry";
    
    
    public boolean isResetCounter() {
        return resetCounter;
    }

    public void setResetCounter(boolean resetCounter) {
        this.resetCounter = resetCounter;
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

    public boolean isIsdefaultformat() {
        return isdefaultformat;
    }

    public void setIsdefaultformat(boolean isdefaultformat) {
        this.isdefaultformat = isdefaultformat;
    }

    public String getDateformatinprefix() {
        return dateformatinprefix;
    }

    public void setDateformatinprefix(String dateformatinprefix) {
        this.dateformatinprefix = dateformatinprefix;
    }
    
    public String getModulename() {
        return modulename;
    }

    public void setModulename(String modulename) {
        this.modulename = modulename;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getNumberofdigit() {
        return numberofdigit;
    }

    public void setNumberofdigit(int numberofdigit) {
        this.numberofdigit = numberofdigit;
    }

    public int getStartfrom() {
        return startfrom;
    }

    public void setStartfrom(int startfrom) {
        this.startfrom = startfrom;
    }

    public int getModuleid() {
        return moduleid;
    }

    public void setModuleid(int moduleid) {
        this.moduleid = moduleid;
    }

    public boolean isShowleadingzero() {
        return showleadingzero;
    }

    public void setShowleadingzero(boolean showleadingzero) {
        this.showleadingzero = showleadingzero;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isDateBeforePrefix() {
        return dateBeforePrefix;
    }

    public void setDateBeforePrefix(boolean dateBeforePrefix) {
        this.dateBeforePrefix = dateBeforePrefix;
    }

    public boolean isIsactivate() {
        return isactivate;
    }

    public void setIsactivate(boolean isactivate) {
        this.isactivate = isactivate;
    }
    
    public boolean isDateAfterPrefix() {
        return dateAfterPrefix;
    }

    public void setDateAfterPrefix(boolean dateAfterPrefix) {
        this.dateAfterPrefix = dateAfterPrefix;
    }

    public String getDateformatafterprefix() {
        return dateformatafterprefix;
    }

    public void setDateformatafterprefix(String dateformatafterprefix) {
        this.dateformatafterprefix = dateformatafterprefix;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }
    
}
