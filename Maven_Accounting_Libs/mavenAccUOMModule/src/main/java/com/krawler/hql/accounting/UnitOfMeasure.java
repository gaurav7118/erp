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
public class UnitOfMeasure {

    private String ID;
    private String name;
    private int allowedPrecision;
    private String type;
    private Company company;
    private String inventoryReferId;//This field refer to Inventory System UOM Id
    private DefaultUnitOfMeasure defaultunitofmeasure;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

     public String getNameEmptyforNA() {
        String temp = this.name;
        if (temp.equals("N/A")) {
            return "";
        }
        return temp;
    }
    @Deprecated
     public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAllowedPrecision() {
        return allowedPrecision;
    }

    public void setAllowedPrecision(int allowedPrecision) {
        this.allowedPrecision = allowedPrecision;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getInventoryReferId() {
        return inventoryReferId;
    }

    public void setInventoryReferId(String inventoryReferId) {
        this.inventoryReferId = inventoryReferId;
    }

    public DefaultUnitOfMeasure getDefaultunitofmeasure() {
        return defaultunitofmeasure;
    }

    public void setDefaultunitofmeasure(DefaultUnitOfMeasure defaultunitofmeasure) {
        this.defaultunitofmeasure = defaultunitofmeasure;
    }
    
}
