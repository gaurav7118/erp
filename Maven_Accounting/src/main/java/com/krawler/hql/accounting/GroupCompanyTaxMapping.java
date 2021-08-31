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
public class GroupCompanyTaxMapping {
    
    private String ID;
    private String sourceCompany;
    private String destinationCompany;
    private String sourceTaxCode;
    private String sourceTaxId;
    private String destinationTaxCode;
    private String destinationTaxId;

    public String getDestinationTaxId() {
        return destinationTaxId;
    }

    public void setDestinationTaxId(String destinationTaxId) {
        this.destinationTaxId = destinationTaxId;
    }

    public String getSourceTaxId() {
        return sourceTaxId;
    }

    public void setSourceTaxId(String sourceTaxId) {
        this.sourceTaxId = sourceTaxId;
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

    public String getDestinationTaxCode() {
        return destinationTaxCode;
    }

    public void setDestinationTaxCode(String destinationTaxCode) {
        this.destinationTaxCode = destinationTaxCode;
    }

    public String getSourceCompany() {
        return sourceCompany;
    }

    public void setSourceCompany(String sourceCompany) {
        this.sourceCompany = sourceCompany;
    }

    public String getSourceTaxCode() {
        return sourceTaxCode;
    }

    public void setSourceTaxCode(String sourceTaxCode) {
        this.sourceTaxCode = sourceTaxCode;
    }
    
}
