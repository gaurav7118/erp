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
import com.krawler.common.util.Constants;
import com.krawler.spring.authHandler.authHandler;

public class Discount {

    private String ID;
    private double originalAmount;
    private double discount;
    private boolean inPercent;
    private Company company;
    private boolean deleted;
    private double amountinInvCurrency;
    private int typeOfFigure; // Type of figure = 1 for flat amount (equals to invoice amount due) or = 2 if it is in percentage of invoice amount due. 
    private double typeFigure ; // value referring to typeOfFigure
    public double getAmountinInvCurrency() {
        return amountinInvCurrency;
    }

    public void setAmountinInvCurrency(double amountinInvCurrency) {
        this.amountinInvCurrency = amountinInvCurrency;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isInPercent() {
        return inPercent;
    }

    public void setInPercent(boolean inPercent) {
        this.inPercent = inPercent;
    }

    public double getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
    }

    public double getDiscountValue() {
        if (inPercent && discount > 0 && discount <= 100.0) {
            return authHandler.round((originalAmount * discount / 100), company.getCompanyID());
        }
        return authHandler.round(discount, company.getCompanyID());
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public double getTypeFigure() {
        return typeFigure;
    }

    public void setTypeFigure(double typeFigure) {
        this.typeFigure = typeFigure;
    }

    public int getTypeOfFigure() {
        return typeOfFigure;
    }

    public void setTypeOfFigure(int typeOfFigure) {
        this.typeOfFigure = typeOfFigure;
    }
    
}
