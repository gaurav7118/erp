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
 * @author krawler
 */
public class Term {

    private String ID;
    private int srno;
    private String termname;
    private int termdays;
    private Company company;
    private boolean isdefault;
    private String crmtermid;
    private DiscountMaster discountName;
    private int applicableDays;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getTermname() {
        return termname;
    }

    public void setTermname(String termname) {
        this.termname = termname;
    }

    public int getTermdays() {
        return termdays;
    }

    public void setTermdays(int termdays) {
        this.termdays = termdays;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
    public boolean isIsdefault() {
        return isdefault;
    }

    public void setIsdefault(boolean isdefault) {
        this.isdefault = isdefault;
    }

    public String getCrmtermid() {
        return crmtermid;
    }

    public void setCrmtermid(String crmtermid) {
        this.crmtermid = crmtermid;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public DiscountMaster getDiscountName() {
        return discountName;
    }

    public void setDiscountName(DiscountMaster discountName) {
        this.discountName = discountName;
    }

    public int getApplicableDays() {
        return applicableDays;
    }

    public void setApplicableDays(int applicableDays) {
        this.applicableDays = applicableDays;
    }

}
