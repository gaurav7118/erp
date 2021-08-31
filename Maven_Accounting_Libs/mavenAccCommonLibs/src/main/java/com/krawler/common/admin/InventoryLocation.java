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
public class InventoryLocation {

    private String id;
    private String name;
    private Company company;
    private boolean isdefault; //for defaultlocation 
    private InventoryLocation parent;
    private Set<InventoryWarehouse> children;
    private String parentId;

    public boolean isIsdefault() {
        return isdefault;
    }

    public void setIsdefault(boolean isdefault) {
        this.isdefault = isdefault;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InventoryLocation getParent() {
        return parent;
    }

    public void setParent(InventoryLocation parent) {
        this.parent = parent;
    }

    public Set<InventoryWarehouse> getChildren() {
        return children;
    }

    public void setChildren(Set<InventoryWarehouse> children) {
        this.children = children;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    
}
