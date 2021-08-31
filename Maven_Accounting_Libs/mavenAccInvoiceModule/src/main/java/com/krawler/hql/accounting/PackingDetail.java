/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class PackingDetail {
    private String ID;
    private int srno;
    private Product product;
    private double actualQuantity;
    private double packingQuantity;
    private String description;
    private String packageNumber;//Package Number field, used in case of UPS integration
    private Company company;
    private Packing packing;
    private Set<ItemDetail> packingdetails;
    private Set<DoDetails> dodetails;
    private DeliveryOrderDetail dodetailid;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getSrno() {
        return srno;
    }

    public void setSrno(int srno) {
        this.srno = srno;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public double getPackingQuantity() {
        return packingQuantity;
    }

    public void setPackingQuantity(double packingQuantity) {
        this.packingQuantity = packingQuantity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPackageNumber() {
        return packageNumber;
    }

    public void setPackageNumber(String packageNumber) {
        this.packageNumber = packageNumber;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Packing getPacking() {
        return packing;
    }

    public void setPacking(Packing packing) {
        this.packing = packing;
    }

    public Set<ItemDetail> getPackingdetails() {
        return packingdetails;
    }

    public void setPackingdetails(Set<ItemDetail> packingdetails) {
        this.packingdetails = packingdetails;
    }

    public Set<DoDetails> getDodetails() {
        return dodetails;
    }

    public void setDodetails(Set<DoDetails> dodetails) {
        this.dodetails = dodetails;
    }

    public DeliveryOrderDetail getDodetailid() {
        return dodetailid;
    }

    public void setDodetailid(DeliveryOrderDetail dodetailid) {
        this.dodetailid = dodetailid;
    }
}
