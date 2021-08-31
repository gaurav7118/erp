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

/**
 *
 * @author training
 */
public class DefaultCompanyAccountPreferences {

    private String ID;
    private DefaultAccount cashAccount;
    private DefaultAccount discountGiven;
    private DefaultAccount discountReceived;
    private DefaultAccount shippingCharges;
    private DefaultAccount otherCharges;
    private DefaultAccount foreignExchange;
    private DefaultAccount depereciationAccount;
    private DefaultAccount salaryExpense;
    private DefaultAccount salaryPayable;
    private DefaultAccount roundingDifference;
    private DefaultAccount customeraccount;
    private DefaultAccount vendoraccount;
    private DefaultAccount unrealisedglaccount;

    public DefaultAccount getSalaryExpense() {
        return salaryExpense;
    }

    public void setSalaryExpense(DefaultAccount salaryExpense) {
        this.salaryExpense = salaryExpense;
    }

    public DefaultAccount getSalaryPayable() {
        return salaryPayable;
    }

    public void setSalaryPayable(DefaultAccount salaryPayable) {
        this.salaryPayable = salaryPayable;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public DefaultAccount getCashAccount() {
        return cashAccount;
    }

    public void setCashAccount(DefaultAccount cashAccount) {
        this.cashAccount = cashAccount;
    }

    public DefaultAccount getDiscountGiven() {
        return discountGiven;
    }

    public void setDiscountGiven(DefaultAccount discountGiven) {
        this.discountGiven = discountGiven;
    }

    public DefaultAccount getDiscountReceived() {
        return discountReceived;
    }

    public void setDiscountReceived(DefaultAccount discountReceived) {
        this.discountReceived = discountReceived;
    }

    public DefaultAccount getOtherCharges() {
        return otherCharges;
    }

    public void setOtherCharges(DefaultAccount otherCharges) {
        this.otherCharges = otherCharges;
    }

    public DefaultAccount getShippingCharges() {
        return shippingCharges;
    }

    public void setShippingCharges(DefaultAccount shippingCharges) {
        this.shippingCharges = shippingCharges;
    }

    public DefaultAccount getForeignExchange() {
        return foreignExchange;
    }

    public void setForeignExchange(DefaultAccount foreignExchange) {
        this.foreignExchange = foreignExchange;
    }

    public DefaultAccount getDepereciationAccount() {
        return depereciationAccount;
    }

    public void setDepereciationAccount(DefaultAccount depereciationAccount) {
        this.depereciationAccount = depereciationAccount;
    }

    public DefaultAccount getRoundingDifference() {
        return roundingDifference;
    }

    public void setRoundingDifference(DefaultAccount roundingDifference) {
        this.roundingDifference = roundingDifference;
    }

    public DefaultAccount getCustomeraccount() {
        return customeraccount;
    }

    public void setCustomeraccount(DefaultAccount customeraccount) {
        this.customeraccount = customeraccount;
    }

    public DefaultAccount getVendoraccount() {
        return vendoraccount;
    }

    public void setVendoraccount(DefaultAccount vendoraccount) {
        this.vendoraccount = vendoraccount;
    }

    public DefaultAccount getUnrealisedglaccount() {
        return unrealisedglaccount;
    }

    public void setUnrealisedglaccount(DefaultAccount unrealisedglaccount) {
        this.unrealisedglaccount = unrealisedglaccount;
    }
}
