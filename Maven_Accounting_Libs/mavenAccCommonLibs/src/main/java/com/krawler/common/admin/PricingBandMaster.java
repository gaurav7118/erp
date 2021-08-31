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
 * @author krawler
 */
public class PricingBandMaster {

    private String ID;
    private String name;
    private Company company;
    private boolean defaultToPOS;
    private String description;
    private int pricePolicyValue; // 1 - Use Discount, 2 - Use Flat Price
    private boolean volumeDiscount; // true - Price List - Volume Discount, false - Price List - Band
    private boolean isIncludingGST;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultToPOS() {
        return defaultToPOS;
    }

    public void setDefaultToPOS(boolean defaultToPOS) {
        this.defaultToPOS = defaultToPOS;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPricePolicyValue() {
        return pricePolicyValue;
    }

    public void setPricePolicyValue(int pricePolicyValue) {
        this.pricePolicyValue = pricePolicyValue;
    }

    public boolean isVolumeDiscount() {
        return volumeDiscount;
    }

    public void setVolumeDiscount(boolean volumeDiscount) {
        this.volumeDiscount = volumeDiscount;
    }

    public boolean isIsIncludingGST() {
        return isIncludingGST;
    }

    public void setIsIncludingGST(boolean isIncludingGST) {
        this.isIncludingGST = isIncludingGST;
    }
}