/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class ItemPackingDetail {

    private String ID;
    private int srno;
    private Product product;
    private double packageQuantity;
    private double itemPerPackage;
    private String description;
    private Company company;
    private PackingDoList packingDoList;
    private PackingDoListDetail packingDoListDetails;
    private double totalItems;
    private double grossWeight;
    private Packages packages;

    public Packages getPackages() {
        return packages;
    }

    public void setPackages(Packages packages) {
        this.packages = packages;
    }
        
    public void setTotalItems(double totalItems) {
        this.totalItems = totalItems;
    }

    public double getTotalItems() {
        return totalItems;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setGrossWeight(double grossWeight) {
        this.grossWeight = grossWeight;
    }

    public void setItemPerPackage(double itemPerPackage) {
        this.itemPerPackage = itemPerPackage;
    }

    public void setPackageQuantity(double packageQuantity) {
        this.packageQuantity = packageQuantity;
    }

    public void setPackingDoList(PackingDoList packingDoList) {
        this.packingDoList = packingDoList;
    }

    public void setPackingDoListDetails(PackingDoListDetail packingDoListDetails) {
        this.packingDoListDetails = packingDoListDetails;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public String getID() {
        return ID;
    }

    public Company getCompany() {
        return company;
    }

    public String getDescription() {
        return description;
    }

    public double getGrossWeight() {
        return grossWeight;
    }

    public double getItemPerPackage() {
        return itemPerPackage;
    }

    public double getPackageQuantity() {
        return packageQuantity;
    }

    public PackingDoList getPackingDoList() {
        return packingDoList;
    }

    public PackingDoListDetail getPackingDoListDetails() {
        return packingDoListDetails;
    }

    public Product getProduct() {
        return product;
    }

    public int getSrno() {
        return srno;
    }
}
