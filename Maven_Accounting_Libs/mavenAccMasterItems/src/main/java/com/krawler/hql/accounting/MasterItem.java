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
import com.krawler.common.admin.User;
import com.krawler.common.util.LandingCostAllocationType;
import com.krawler.common.admin.LandingCostCategory;
import java.net.URLDecoder;
import java.util.Set;

/**
 *
 * @author krawler-user
 */
public class MasterItem implements Comparable<MasterItem> {
     
    private String ID;
    private String value;
    private String code;// for saving sales person, Agent Code
    private String contactNumber;// for saving sales person 
    private String address;// for saving sales person 
    private MasterGroup masterGroup;
    private boolean ibgActivated;// if Master Item is Paid To (master group == 17) then its value will be considered.
    private Company company;
    private MasterItem parent;
    private Set<MasterItem> children;
    private String emailID;
    private User user;
    private int custVendCategoryType; // For Customer/Vendor Category ---> 0 : Cash Customer/Vendor, 1 : Credit Customer/Vendor
    private String designation;
    private MasterItem driver;
    private boolean defaultToPOS;      //Product category is default to POS 
    private DefaultMasterItem defaultMasterItem;
    private boolean activated;//this flag is adeed for salesperson activation and deactivation
    private double variancePercentage; // Used for Product Category
    private String industryCodeId; // Used for Product Category
    
    private MasterItem propagatedMasteritemID;//This is used to save propagated parent masteritem id 
    
    private String vatcommoditycode; // for saving VAT commodity code
    private String vatscheduleserialno; // for saving VAT Schedule Serial No
    private String vatscheduleno; // for saving VAT Schedule No
    private String vatnotes; // for saving VAT Notes
    private String accID;             //Cashout account For POS Cashout Transaction
    
    private LandingCostCategory lccategoryid;
    private LandingCostAllocationType lcallocationid;
    
    private String BICCode;
    private String bankCode;
    private String branchCode;  
    private boolean isAppendBranchCode;//ERP-31397 - Provided option in Bank details master screen whether to append branch code with account number or not.
    
    public static final String Process = "36";
    public static final String Department = "13";
    public static final String Skill = "54";
    public static final String Bank_Name = "2";
    public static final String Reason = "29";
    public static final String Status_Combo = "11";
    public static final String DeliverStatus_Combo = "10";

    public MasterItem getPropagatedMasteritemID() {
        return propagatedMasteritemID;
    }

    public void setPropagatedMasteritemID(MasterItem propagatedMasteritemID) {
        this.propagatedMasteritemID = propagatedMasteritemID;
    }
    
    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
    
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
        String returnValue = "";
        if (value != null) {
            try {
                returnValue = URLDecoder.decode(value, StaticValues.ENCODING);
            } catch (Exception ex) {
                returnValue = value;
            }
        } else {
            returnValue = value;
        }
        return returnValue;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public MasterItem getParent() {
        return parent;
    }

    public void setParent(MasterItem parent) {
        this.parent = parent;
    }

    public Set<MasterItem> getChildren() {
        return children;
    }

    public void setChildren(Set<MasterItem> children) {
        this.children = children;
    }

    public String getEmailID() {
        return emailID;
    }

    public boolean isIbgActivated() {
        return ibgActivated;
    }

    public void setIbgActivated(boolean ibgActivated) {
        this.ibgActivated = ibgActivated;
    }

    public void setEmailID(String emailID) {
        this.emailID = emailID;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getCustVendCategoryType() {
        return custVendCategoryType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setCustVendCategoryType(int custVendCategoryType) {
        this.custVendCategoryType = custVendCategoryType;
    }
    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public MasterItem getDriver() {
        return driver;
    }

    public void setDriver(MasterItem driver) {
        this.driver = driver;
    }
    
    public boolean isDefaultToPOS() {
        return defaultToPOS;
    }

    public void setDefaultToPOS(boolean defaultToPOS) {
        this.defaultToPOS = defaultToPOS;
    }
    
    public DefaultMasterItem getDefaultMasterItem() {
        return defaultMasterItem;
    }

    public void setDefaultMasterItem(DefaultMasterItem defaultMasterItem) {
        this.defaultMasterItem = defaultMasterItem;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MasterItem other = (MasterItem) obj;
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    @Override
    public int compareTo(MasterItem o) {
        return this.getValue().compareToIgnoreCase(o.getValue());
    }

    public double getVariancePercentage() {
        return variancePercentage;
    }

    public void setVariancePercentage(double variancePercentage) {
        this.variancePercentage = variancePercentage;
    }
    
    public String getVatcommoditycode() {
        return vatcommoditycode;
    }

    public void setVatcommoditycode(String vatcommoditycode) {
        this.vatcommoditycode = vatcommoditycode;
    }
    
    public String getVatnotes() {
        return vatnotes;
    }

    public void setVatnotes(String vatnotes) {
        this.vatnotes = vatnotes;
    }

    public String getVatscheduleno() {
        return vatscheduleno;
    }

    public void setVatscheduleno(String vatscheduleno) {
        this.vatscheduleno = vatscheduleno;
    }

    public String getVatscheduleserialno() {
        return vatscheduleserialno;
    }

    public void setVatscheduleserialno(String vatscheduleserialno) {
        this.vatscheduleserialno = vatscheduleserialno;
    }

    public String getAccID() {
        return accID;
    }

    public void setAccID(String accID) {
        this.accID = accID;
    }

    /**
     * @return the industryCodeId
     */
    public String getIndustryCodeId() {
        return industryCodeId;
    }

    /**
     * @param industryCodeId the industryCodeId to set
     */
    public void setIndustryCodeId(String industryCodeId) {
        this.industryCodeId = industryCodeId;
    }

    public LandingCostAllocationType getLcallocationid() {
        return lcallocationid;
    }

    public void setLcallocationid(LandingCostAllocationType lcallocationid) {
        this.lcallocationid = lcallocationid;
    }

    public LandingCostCategory getLccategoryid() {
        return lccategoryid;
    }

    public void setLccategoryid(LandingCostCategory lccategoryid) {
        this.lccategoryid = lccategoryid;
    }

    public String getBICCode() {
        return BICCode;
    }

    public void setBICCode(String BICCode) {
        this.BICCode = BICCode;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }
    
    public boolean isIsAppendBranchCode() {
        return isAppendBranchCode;
    }

    public void setIsAppendBranchCode(boolean isAppendBranchCode) {
        this.isAppendBranchCode = isAppendBranchCode;
    }
}
