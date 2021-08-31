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
import java.util.Date;

/**
 *
 * @author krawler-user
 */
public class Inventory {

    private String ID;
    private Product product;
    private double quantity;
    private UnitOfMeasure uom;
    private double baseuomquantity;
    private double baseuomrate;
    private double actquantity;
    private String description;
    private boolean carryIn;
    private boolean newInv;
    private boolean invrecord;
    private boolean defective;
    private Company company;
    private Date updateDate;
    private boolean deleted;
    private boolean openingInventory;
    private boolean leaseFlag;
    private boolean isconsignment;
    private boolean isjobworkorder;   
    private double consignuomquantity;
    private double venconsignuomquantity;
    private double productWeightPerStockUom;
    private double productWeightIncludingPakagingPerStockUom;
    private double productVolumePerStockUom;
    private double productVolumeIncludingPakagingPerStockUom;

    public double getProductWeightIncludingPakagingPerStockUom() {
        return productWeightIncludingPakagingPerStockUom;
    }

    public void setProductWeightIncludingPakagingPerStockUom(double productWeightIncludingPakagingPerStockUom) {
        this.productWeightIncludingPakagingPerStockUom = productWeightIncludingPakagingPerStockUom;
    }

    public double getProductWeightPerStockUom() {
        return productWeightPerStockUom;
    }

    public void setProductWeightPerStockUom(double productWeightPerStockUom) {
        this.productWeightPerStockUom = productWeightPerStockUom;
    }

    public double getConsignuomquantity() {
        return consignuomquantity;
    }

    public void setConsignuomquantity(double consignuomquantity) {
        this.consignuomquantity = consignuomquantity;
    }
    
    public boolean isIsconsignment() {
        return isconsignment;
    }

    public void setIsconsignment(boolean isconsignment) {
        this.isconsignment = isconsignment;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public boolean isCarryIn() {
        return carryIn;
    }

    public void setCarryIn(boolean carryIn) {
        this.carryIn = carryIn;
    }

    public boolean isNewInv() {
        return newInv;
    }

    public void setNewInv(boolean newInv) {
        this.newInv = newInv;
    }

    public boolean isInvrecord() {
        return invrecord;
    }

    public void setInvrecord(boolean invrecord) {
        this.invrecord = invrecord;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isDefective() {
        return defective;
    }

    public void setDefective(boolean defective) {
        this.defective = defective;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public double getActquantity() {
        return actquantity;
    }

    public void setActquantity(double actquantity) {
        this.actquantity = actquantity;
    }

    public UnitOfMeasure getUom() {
        return uom;
    }

    public void setUom(UnitOfMeasure uom) {
        this.uom = uom;
    }

    public double getBaseuomquantity() {
        return baseuomquantity;
    }

    public void setBaseuomquantity(double baseuomquantity) {
        this.baseuomquantity = baseuomquantity;
    }

    public double getBaseuomrate() {
        return baseuomrate;
    }

    public void setBaseuomrate(double baseuomrate) {
        this.baseuomrate = baseuomrate;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isOpeningInventory() {
        return openingInventory;
    }

    public void setOpeningInventory(boolean openingInventory) {
        this.openingInventory = openingInventory;
    }

    public boolean isLeaseFlag() {
        return leaseFlag;
    }

    public void setLeaseFlag(boolean leaseFlag) {
        this.leaseFlag = leaseFlag;
    }

    public double getVenconsignuomquantity() {
        return venconsignuomquantity;
    }

    public void setVenconsignuomquantity(double venconsignuomquantity) {
        this.venconsignuomquantity = venconsignuomquantity;
    }

    public double getProductVolumePerStockUom() {
        return productVolumePerStockUom;
    }

    public void setProductVolumePerStockUom(double productVolumePerStockUom) {
        this.productVolumePerStockUom = productVolumePerStockUom;
    }

    public double getProductVolumeIncludingPakagingPerStockUom() {
        return productVolumeIncludingPakagingPerStockUom;
    }

    public void setProductVolumeIncludingPakagingPerStockUom(double productVolumeIncludingPakagingPerStockUom) {
        this.productVolumeIncludingPakagingPerStockUom = productVolumeIncludingPakagingPerStockUom;
    }

    public boolean isIsjobworkorder() {
        return isjobworkorder;
    }

    public void setIsjobworkorder(boolean isjobworkorder) {
        this.isjobworkorder = isjobworkorder;
    }
    }
