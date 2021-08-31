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
public class GroupCompanyCustomerVendorMapping {

    private String ID;
    private String sourceCompany;
    private String destinationCompany;
    private String sourceMasterCode;
    private String sourceMasterId;
    private String destinationMasterCode;
    private boolean isSourceCustomer; //0 means vendor, 1 means customer 
    private String destinationMasterId;

    public String getDestinationMasterId() {
        return destinationMasterId;
    }

    public void setDestinationMasterId(String destinationMasterId) {
        this.destinationMasterId = destinationMasterId;
    }

    public String getSourceMasterId() {
        return sourceMasterId;
    }

    public void setSourceMasterId(String sourceMasterId) {
        this.sourceMasterId = sourceMasterId;
    }

    public String getDestinationMasterCode() {
        return destinationMasterCode;
    }

    public void setDestinationMasterCode(String destinationMasterCode) {
        this.destinationMasterCode = destinationMasterCode;
    }

    public String getSourceMasterCode() {
        return sourceMasterCode;
    }

    public void setSourceMasterCode(String sourceMasterCode) {
        this.sourceMasterCode = sourceMasterCode;
    }


    public boolean isIsSourceCustomer() {
        return isSourceCustomer;
    }

    public void setIsSourceCustomer(boolean isSourceCustomer) {
        this.isSourceCustomer = isSourceCustomer;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDestinationCompany() {
        return destinationCompany;
    }

    public void setDestinationCompany(String destinationCompany) {
        this.destinationCompany = destinationCompany;
    }

    public String getSourceCompany() {
        return sourceCompany;
    }

    public void setSourceCompany(String sourceCompany) {
        this.sourceCompany = sourceCompany;
    }
  
}
