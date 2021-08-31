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
 * @author training
 */
public class Tax {

    private String ID;
    private String name;
    private String description;
    private String taxCode;
    private boolean deleted;
    private Account account;
    private Company company;
    private int taxtype; // 0 - all taxes, 1 - purchase taxes, 2 - sales taxes 
    private String taxCodeWithoutPercentage; // Only tax code (without percentages appened with name)
    private int extrataxtype;
    private boolean activated;
    private boolean inputCredit;   //ERM-971 adding input credit flag to include/exclude at landed cost Tax level
    private String defaulttax;     //ERP-41973  map defaultgst id in main tax table
    
    public Tax() {
        this.activated = true;
    }

    public int getExtrataxtype() {
        return extrataxtype;
    }

    public void setExtrataxtype(int extrataxtype) {
        this.extrataxtype = extrataxtype;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getTaxtype() {
        return taxtype;
    }

    public void setTaxtype(int taxtype) {
        this.taxtype = taxtype;
    }

    public String getTaxCodeWithoutPercentage() {
        return taxCodeWithoutPercentage;
    }

    public void setTaxCodeWithoutPercentage(String taxCodeWithoutPercentage) {
        this.taxCodeWithoutPercentage = taxCodeWithoutPercentage;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
    
    public boolean isInputCredit() {
        return inputCredit;
    }

    public void setInputCredit(boolean inputCredit) {
        this.inputCredit = inputCredit;
    }

    public String getDefaulttax() {
        return defaulttax;
    }

    public void setDefaulttax(String defaulttax) {
        this.defaulttax = defaulttax;
    }
    
  }
