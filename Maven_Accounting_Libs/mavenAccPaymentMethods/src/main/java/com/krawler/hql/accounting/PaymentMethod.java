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
 * @author krawler-user
 */
public class PaymentMethod {

    public static final int TYPE_CASH = 0;
    public static final int TYPE_CARD = 1;
    public static final int TYPE_BANK = 2;
    private String ID;
    private String methodName;
    private int detailType;
    private Account account;
    private Company company;
    private boolean autoPopulateInLoan;
    private boolean autoPopulate;//used to select the flow for entering amount in payment.
                                 //False :Need to enter amount in the amount in payment first then select invoices
                                 //True :can directly select invoices or credit notes and the amount will get populated directly in the payment directly
    private boolean autoPopulateInCPCS; //used to populate payment method in CP and CS module
    private boolean autoPopulateInIBGGeneration;//SDP-8073 - Filter only IBG payment methods while generating IBG file

    private int srno; //ERP-32755 : ERM-65 - used to reorder payment method

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public int getDetailType() {
        return detailType;
    }

    public void setDetailType(int detailType) {
        this.detailType = detailType;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isAutoPopulate() {
        return autoPopulate;
    }

    public void setAutoPopulate(boolean autoPopulate) {
        this.autoPopulate = autoPopulate;
    }

    public boolean isAutoPopulateInCPCS() {
        return autoPopulateInCPCS;
    }

    public void setAutoPopulateInCPCS(boolean autoPopulateInCPCS) {
        this.autoPopulateInCPCS = autoPopulateInCPCS;
    }

    /**
     * @return the autoPopulateInLoan
     */
    public boolean isAutoPopulateInLoan() {
        return autoPopulateInLoan;
    }

    /**
     * @param autoPopulateInLoan the autoPopulateInLoan to set
     */
    public void setAutoPopulateInLoan(boolean autoPopulateInLoan) {
        this.autoPopulateInLoan = autoPopulateInLoan;
    }

    public boolean isAutoPopulateInIBGGeneration() {
        return autoPopulateInIBGGeneration;
    }

    public void setAutoPopulateInIBGGeneration(boolean autoPopulateInIBGGeneration) {
        this.autoPopulateInIBGGeneration = autoPopulateInIBGGeneration;
    }
}
