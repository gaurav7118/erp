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

public class Asset {

    private String ID;
    private Boolean isSale;
    private Boolean isWriteOff;
    private JournalEntry deleteJe;
    private JournalEntry purchaseJe;
    private Account account;
    private Company company;
    /*
     * depreciationMethod = 1 == straight line method depreciationMethod = 2 ==
     * Double Declining Balance Method
     */
    private int depreciationMethod;

    public String getID() {
        return ID;
    }

    public void setID(String iD) {
        ID = iD;
    }

    public Boolean getIsSale() {
        return isSale;
    }

    public void setIsSale(Boolean isSale) {
        this.isSale = isSale;
    }

    public Boolean getIsWriteOff() {
        return isWriteOff;
    }

    public void setIsWriteOff(Boolean isWriteOff) {
        this.isWriteOff = isWriteOff;
    }

    public JournalEntry getDeleteJe() {
        return deleteJe;
    }

    public void setDeleteJe(JournalEntry deleteJe) {
        this.deleteJe = deleteJe;
    }

    public JournalEntry getPurchaseJe() {
        return purchaseJe;
    }

    public void setPurchaseJe(JournalEntry purchaseJe) {
        this.purchaseJe = purchaseJe;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public int getDepreciationMethod() {
        return depreciationMethod;
    }

    public void setDepreciationMethod(int depreciationMethod) {
        this.depreciationMethod = depreciationMethod;
    }
}
