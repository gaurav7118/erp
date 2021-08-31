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

/**
 *
 * @author training
 */
public class DefaultMasterItem {

    private String ID;
    private String value;
    private MasterGroup masterGroup;
    private String code;
    private String defaultAccID;             //TDS Payable account For Default Nature of Payment
    private Country country;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public MasterGroup getMasterGroup() {
        return masterGroup;
    }

    public void setMasterGroup(MasterGroup masterGroup) {
        this.masterGroup = masterGroup;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getDefaultAccID() {
        return defaultAccID;
    }

    public void setDefaultAccID(String defaultAccID) {
        this.defaultAccID = defaultAccID;
    }

}
