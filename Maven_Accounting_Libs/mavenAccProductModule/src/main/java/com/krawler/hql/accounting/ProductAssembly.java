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
 * @author gaurav
 */
import com.krawler.common.admin.Company;
import java.util.Set;

public class ProductAssembly {

    private String ID;
    private double quantity;
    private double percentage;
    private double actualQuantity;
    private double inventoryQuantity;
    private double remainingQuantity;
    private double recycleQuantity;
    private Product product;
    private Product subproducts;
    private BOMDetail bomdetail;
    private BOMDetail subbom;
    private double wastageInventoryQuantity;
    private int wastageQuantityType; // 0 - 'Flat', 1 - 'Percentage'
    private double wastageQuantity;
    private double crate;
    private int componentType;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public Product getSubproducts() {
        return subproducts;
    }

    public void setSubproducts(Product subproducts) {
        this.subproducts = subproducts;
    }

    public double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public double getInventoryQuantity() {
        return inventoryQuantity;
    }

    public void setInventoryQuantity(double inventoryQuantity) {
        this.inventoryQuantity = inventoryQuantity;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public double getRecycleQuantity() {
        return recycleQuantity;
    }

    public void setRecycleQuantity(double recycleQuantity) {
        this.recycleQuantity = recycleQuantity;
    }

    public double getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(double remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    public double getWastageInventoryQuantity() {
        return wastageInventoryQuantity;
    }

    public void setWastageInventoryQuantity(double wastageInventoryQuantity) {
        this.wastageInventoryQuantity = wastageInventoryQuantity;
    }

    public double getWastageQuantity() {
        return wastageQuantity;
    }

    public void setWastageQuantity(double wastageQuantity) {
        this.wastageQuantity = wastageQuantity;
    }

    public int getWastageQuantityType() {
        return wastageQuantityType;
    }

    public void setWastageQuantityType(int wastageQuantityType) {
        this.wastageQuantityType = wastageQuantityType;
    }

    public BOMDetail getBomdetail() {
        return bomdetail;
    }

    public void setBomdetail(BOMDetail bomdetail) {
        this.bomdetail = bomdetail;
    }

    public BOMDetail getSubbom() {
        return subbom;
    }

    public void setSubbom(BOMDetail subbom) {
        this.subbom = subbom;
    }

    public void setCrate(double crate) {
        this.crate = crate;
    }
    
    public double getCrate() {
        return crate;
    }

    public void setComponentType(int componentType) {
        this.componentType = componentType;
    }
    
    public int getComponentType() {
        return componentType;
    }
}
