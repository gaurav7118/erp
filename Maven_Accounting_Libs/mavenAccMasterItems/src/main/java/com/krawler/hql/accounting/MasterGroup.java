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

import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class MasterGroup {

    private String ID;
    private String groupName;
    private boolean isformrp;
    private Set<MasterItem> items;

    public static final String ProductCategory = "19";
    public static final String SalesPerson = "15";

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Set<MasterItem> getItems() {
        return items;
    }

    public void setItems(Set<MasterItem> items) {
        this.items = items;
    }
    public boolean isIsformrp() {
        return isformrp;
    }

    public void setIsformrp(boolean isformrp) {
        this.isformrp = isformrp;
    }
}
