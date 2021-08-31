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

import com.krawler.common.admin.Country;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.State;

import java.util.Date;
import java.util.Set;

public class DefaultAccount {

    private String ID;
    private DefaultAccount parent;
    private String name;
    private double openingBalance;
    private KWLCurrency currency;
    private Group group;
    private Date creationDate;
    private double life;
    private double salvage;
    private double presentValue;
    private String companyType;
    private Country country;
    private State state;// Used for INDIA country
    private boolean controlAccounts;
    private int mastertypevalue;//1 - GL, 2 - Cash, 3 - Bank, 4 - Normal GST /INDAI (duties and taxes)
    private boolean mrpAccount; // default accounts if MRP is activated

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }
    private Set<DefaultAccount> children;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(double openingBalance) {
        this.openingBalance = openingBalance;
    }

    public DefaultAccount getParent() {
        return parent;
    }

    public void setParent(DefaultAccount parent) {
        this.parent = parent;
    }

    public Set<DefaultAccount> getChildren() {
        return children;
    }

    public void setChildren(Set<DefaultAccount> children) {
        this.children = children;
    }

    public KWLCurrency getCurrency() {
        return currency;
    }

    public void setCurrency(KWLCurrency currency) {
        this.currency = currency;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public double getLife() {
        return life;
    }

    public void setLife(double life) {
        this.life = life;
    }

    public double getPresentValue() {
        return presentValue;
    }

    public void setPresentValue(double presentValue) {
        this.presentValue = presentValue;
    }

    public double getSalvage() {
        return salvage;
    }

    public void setSalvage(double salvage) {
        this.salvage = salvage;
    }

    public boolean isControlAccounts() {
        return controlAccounts;
    }

    public void setControlAccounts(boolean controlAccounts) {
        this.controlAccounts = controlAccounts;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
    public int getMastertypevalue() {
        return mastertypevalue;
    }

    public void setMastertypevalue(int mastertypevalue) {
        this.mastertypevalue = mastertypevalue;
    }
    
    public boolean isMrpAccount() {
        return mrpAccount;
}

    public void setMrpAccount(boolean mrpAccount) {
        this.mrpAccount = mrpAccount;
    }

}
