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
 * @author krawler
 */
public class DefaultChequeLayout {
    private String id;
    private DefaultAccount defaultaccount;
    private String defaultcoordinateinfo;

    
    public DefaultAccount getDefaultaccount() {
        return defaultaccount;
    }

    public void setDefaultaccount(DefaultAccount defaultaccount) {
        this.defaultaccount = defaultaccount;
    }


    public String getDefaultcoordinateinfo() {
        return defaultcoordinateinfo;
    }

    public void setDefaultcoordinateinfo(String defaultcoordinateinfo) {
        this.defaultcoordinateinfo = defaultcoordinateinfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    
}
