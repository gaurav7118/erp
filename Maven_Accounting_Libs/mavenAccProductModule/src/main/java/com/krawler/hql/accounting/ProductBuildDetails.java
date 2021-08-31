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
public class ProductBuildDetails {

    private String ID;
    private ProductBuild build;
    private double rate;
    private Product aproduct;
    private double aquantity;
    private double percentage;
    private double actualQuantity;
    private double inventoryQuantity;
    private double remainingQuantity;
    private double recycleQuantity;
    private boolean unbuild;
    private Inventory inventory;
    private double wastageInventoryQuantity;
    private int wastageQuantityType; // 0 - 'Flat', 1 - 'Percentage'
    private double wastageQuantity;
    private JournalEntryDetail jedetail;
    private JournalEntryDetail wastagejedetail;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Product getAproduct() {
        return aproduct;
    }

    public void setAproduct(Product aproduct) {
        this.aproduct = aproduct;
    }

    public double getAquantity() {
        return aquantity;
    }

    public void setAquantity(double aquantity) {
        this.aquantity = aquantity;
    }

    public ProductBuild getBuild() {
        return build;
    }

    public void setBuild(ProductBuild build) {
        this.build = build;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
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

    public boolean isUnbuild() {
        return unbuild;
    }

    public void setUnbuild(boolean unbuild) {
        this.unbuild = unbuild;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
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

    public JournalEntryDetail getJedetail() {
        return jedetail;
    }

    public void setJedetail(JournalEntryDetail jedetail) {
        this.jedetail = jedetail;
    }

    public JournalEntryDetail getWastagejedetail() {
        return wastagejedetail;
    }

    public void setWastagejedetail(JournalEntryDetail wastagejedetail) {
        this.wastagejedetail = wastagejedetail;
    }
}
