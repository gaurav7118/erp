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
package com.krawler.common.admin;

/**
 *
 * @author Karthik
 */
public class CustomColumnFormulae {

    private String formulaeid;
    private Company companyid;
    private String moduleid;
    private String fieldname;
    private String formulae;

    public CustomColumnFormulae(String formulaeid, String moduleid, String fieldname, String formulae) {
        this.formulaeid = formulaeid;
        this.moduleid = moduleid;
        this.fieldname = fieldname;
        this.formulae = formulae;
    }

    public CustomColumnFormulae() {
    }

    public Company getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Company companyid) {
        this.companyid = companyid;
    }

    public String getFieldname() {
        return fieldname;
    }

    public void setFieldname(String fieldname) {
        this.fieldname = fieldname;
    }

    public String getFormulae() {
        return formulae;
    }

    public void setFormulae(String formulae) {
        this.formulae = formulae;
    }

    public String getFormulaeid() {
        return formulaeid;
    }

    public void setFormulaeid(String formulaeid) {
        this.formulaeid = formulaeid;
    }

    public String getModuleid() {
        return moduleid;
    }

    public void setModuleid(String moduleid) {
        this.moduleid = moduleid;
    }
}
