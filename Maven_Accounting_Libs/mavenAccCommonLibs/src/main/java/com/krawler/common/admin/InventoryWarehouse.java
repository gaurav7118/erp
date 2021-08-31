/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Set;

/**
 *
 * @author krawler
 */
public class InventoryWarehouse {

    private String id;
    private String name;
    private Company company;
    private boolean isdefault; //for defaultlocation 
    private boolean isForCustomer; //for defaultlocation 
    private InventoryWarehouse parent;
    private InventoryLocation location;
    private Set<InventoryWarehouse> children;
    private String customer;
    private String parentId;
    private Set<String> movementTypeSet;
    private boolean active;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    public boolean isIsdefault() {
        return isdefault;
    }

    public void setIsdefault(boolean isdefault) {
        this.isdefault = isdefault;
    }

    public Set<InventoryWarehouse> getChildren() {
        return children;
    }

    public void setChildren(Set<InventoryWarehouse> children) {
        this.children = children;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public InventoryLocation getLocation() {
        return location;
    }

    public void setLocation(InventoryLocation location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InventoryWarehouse getParent() {
        return parent;
    }

    public void setParent(InventoryWarehouse parent) {
        this.parent = parent;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public boolean isIsForCustomer() {
        return isForCustomer;
    }

    public void setIsForCustomer(boolean isForCustomer) {
        this.isForCustomer = isForCustomer;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Set<String> getMovementTypeSet() {
        return movementTypeSet;
    }

    public void setMovementTypeSet(Set<String> movementTypeSet) {
        this.movementTypeSet = movementTypeSet;
    }
}
