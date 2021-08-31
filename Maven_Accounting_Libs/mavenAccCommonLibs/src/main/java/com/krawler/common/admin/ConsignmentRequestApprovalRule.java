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

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class ConsignmentRequestApprovalRule {
    
    private String ID;
    private String ruleName;
    private User requester;
    private InventoryWarehouse inventoryWarehouse;
    private Set<InventoryLocation> inventoryLocationsSet;
//    private Set<User> approverSet;
    private User createdby;
    private User modifiedby;
    private long createdon;
    private long updatedon;
    private ApprovalType approvalType;
    private Company company;

    public ConsignmentRequestApprovalRule() {
        inventoryLocationsSet = new HashSet<InventoryLocation>();
//        approverSet = new HashSet<User>();
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

//    public Set<User> getApproverSet() {
//        return approverSet;
//    }
//
//    public void setApproverSet(Set<User> approverSet) {
//        this.approverSet = approverSet;
//    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<InventoryLocation> getInventoryLocationsSet() {
        return inventoryLocationsSet;
    }

    public void setInventoryLocationsSet(Set<InventoryLocation> inventoryLocationsSet) {
        this.inventoryLocationsSet = inventoryLocationsSet;
    }

    public InventoryWarehouse getInventoryWarehouse() {
        return inventoryWarehouse;
    }

    public void setInventoryWarehouse(InventoryWarehouse inventoryWarehouse) {
        this.inventoryWarehouse = inventoryWarehouse;
    }

    public User getRequester() {
        return requester;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public User getCreatedby() {
        return createdby;
    }

    public void setCreatedby(User createdby) {
        this.createdby = createdby;
    }

    public long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(long createdon) {
        this.createdon = createdon;
    }

    public User getModifiedby() {
        return modifiedby;
    }

    public void setModifiedby(User modifiedby) {
        this.modifiedby = modifiedby;
    }

    public long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(long updatedon) {
        this.updatedon = updatedon;
    }

    public ApprovalType getApprovalType() {
        return approvalType;
    }

    public void setApprovalType(ApprovalType approvalType) {
        this.approvalType = approvalType;
    }

       
}

