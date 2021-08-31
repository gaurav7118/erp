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
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class Group implements Comparable {

    public static final String OTHER_ASSETS = "1";
    public static final String CREDIT_CARD = "2";
    public static final String OTHER_CURRENT_LIABILITIES = "3";
    public static final String EQUITY = "4";
    public static final String INCOME = "5";
    public static final String COST_OF_GOODS_SOLD = "6";
    public static final String EXPENSES = "7";
    public static final String OTHER_EXPENSE = "8";
    public static final String BANK_ACCOUNT = "9";
    public static final String ACCOUNTS_RECEIVABLE = "10";
    public static final String OTHER_CURRENT_ASSETS = "11";
    public static final String FIXED_ASSETS = "12";
    public static final String ACCOUNTS_PAYABLE = "13";
    public static final String LONG_TERM_LIABILITY = "14";
    public static final String OTHER_INCOME = "15";
    public static final String CURRENT_ASSETS = "18";
    public static final String CASH = "23";
    public static final String BILLS_PAYABLE = "24";
    public static final int NATURE_LIABILITY = 0;
    public static final int NATURE_ASSET = 1;
    public static final int NATURE_EXPENSES = 2;
    public static final int NATURE_INCOME = 3;
    public static final int ACC_TYPE_BALANCESHEET = 1;
    public static final int ACC_TYPE_PROFITLOSS = 0;
    public static final int ACCOUNTTYPE_GL = 1;
    public static final int ACCOUNTTYPE_CASH = 2;
    public static final int ACCOUNTTYPE_BANK = 3;
    public static final int ACCOUNTTYPE_GST = 4;
    public static final String ACC_TYPE_BALANCESHEETSTR = "Balance Sheet";
    public static final String ACC_TYPE_PROFITLOSSSTR = "Profit & Loss";
    public static final String ACCOUNTTYPE_GLSTR = "General Ledger";
    public static final String ACCOUNTTYPE_CASHSTR = "Cash";
    public static final String ACCOUNTTYPE_BANKSTR = "Bank";
    public static final String ACCOUNTTYPE_GSTSTR = "GST";
    public static final String ACCOUNTTYPE_GSTSTRForIndia = "Duties & Taxes"; // Used for INDIA country
    public static final String ACCOUNTTYPE_GSTSTRForPhilippines ="Tax";  // Used for philippines.
    private String ID;
    private String name;
    private int nature;
    private boolean affectGrossProfit;
    private int displayOrder;
    private boolean deleted;
    private Group parent;
    private Set<Group> children;
    private Company company;
    private boolean isMasterGroup;
    private String grpOldId;
    private boolean costOfGoodsSoldGroup; // if "true" include group in Cost of Goods Sold in P & L report
    private Group propagatedgroupid; 
    
    public Group getPropagatedgroupid() {
        return propagatedgroupid;
    }

    public void setPropagatedgroupid(Group propagatedgroupid) {
        this.propagatedgroupid = propagatedgroupid;
    }
    
    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAffectGrossProfit() {
        return affectGrossProfit;
    }

    public void setAffectGrossProfit(boolean affectGrossProfit) {
        this.affectGrossProfit = affectGrossProfit;
    }

    public int getNature() {
        return nature;
    }

    public void setNature(int nature) throws AccountingException {
        if (nature != NATURE_ASSET && nature != NATURE_LIABILITY && nature != NATURE_EXPENSES && nature != NATURE_INCOME) {
            throw new AccountingException("Unknown group nature specified");
        }
        this.nature = nature;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Group getParent() {
        return parent;
    }

    public void setParent(Group parent) {
        this.parent = parent;
    }

    public Set<Group> getChildren() {
        return children;
    }

    public void setChildren(Set<Group> children) {
        this.children = children;
    }

    public int compareTo(Object o) {
        return this.name.compareTo(((Group) o).getName());
    }

    public boolean isIsMasterGroup() {
        return isMasterGroup;
    }

    public void setIsMasterGroup(boolean isMasterGroup) {
        this.isMasterGroup = isMasterGroup;
    }

    public String getGrpOldId() {
        return grpOldId;
    }

    public void setGrpOldId(String grpOldId) {
        this.grpOldId = grpOldId;
    }

    public boolean isCostOfGoodsSoldGroup() {
        return costOfGoodsSoldGroup;
    }

    public void setCostOfGoodsSoldGroup(boolean costOfGoodsSoldGroup) {
        this.costOfGoodsSoldGroup = costOfGoodsSoldGroup;
    }

    
}
