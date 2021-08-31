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
 * @author gaurav
 */
public class Producttype {

//     assembly: "e4611696-515c-102d-8de6-001cc0794cfa",
//    invpart: "d8a50d12-515c-102d-8de6-001cc0794cfa",
//    noninvpart: "f071cf84-515c-102d-8de6-001cc0794cfa",
//    service: "4efb0286-5627-102d-8de6-001cc0794cfa"
    public static final String ASSEMBLY = "e4611696-515c-102d-8de6-001cc0794cfa";
    public static final String INVENTORY_PART = "d8a50d12-515c-102d-8de6-001cc0794cfa";
    public static final String NON_INVENTORY_PART = "f071cf84-515c-102d-8de6-001cc0794cfa";
    public static final String SERVICE = "4efb0286-5627-102d-8de6-001cc0794cfa";
    public static final String CUSTOMER_ASSEMBLY = "a6a350c4-7646-11e6-9648-14dda97925bd";
    public static final String CUSTOMER_INVENTORY = "a839448c-7646-11e6-9648-14dda97925bd";
    public static final String Inventory_Non_Sales = "ff8080812f5c78bb012f5cfe7edb000c9cfa";
    
    public static final String SERVICE_Name = "Service";
    public static final String INVENTORY_PART_Name= "Inventory Part";
    private String ID;
    private String name;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
