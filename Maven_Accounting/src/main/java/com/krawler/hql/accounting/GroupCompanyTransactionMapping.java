
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

public class GroupCompanyTransactionMapping {
    private String ID;
    private String sourceModule;
    private String destinationModule;
    private String destinationTransactionid;
    private String sourceTransactionid;

    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDestinationModule() {
        return destinationModule;
    }

    public void setDestinationModule(String destinationModule) {
        this.destinationModule = destinationModule;
    }

    public String getDestinationTransactionid() {
        return destinationTransactionid;
    }

    public void setDestinationTransactionid(String destinationTransactionid) {
        this.destinationTransactionid = destinationTransactionid;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }

    public String getSourceTransactionid() {
        return sourceTransactionid;
    }

    public void setSourceTransactionid(String sourceTransactionid) {
        this.sourceTransactionid = sourceTransactionid;
    }
    
}
