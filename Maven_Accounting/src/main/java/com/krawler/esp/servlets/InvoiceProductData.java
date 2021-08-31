/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.esp.servlets;

import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.Product;
import java.util.Date;

/**
 *
 * @author krawler
 */
public class InvoiceProductData {
    private String productID;
    private String productName;
    private double productPurchasePrice;
    private double productPrice;
    private double productQuantity;
    private double actualQuantity;
    private String checknumber;
    private String customerName;
    private Product product;
    private Date businessDate;
    private double totalTax;
    private double totalDiscount;
    private Discount discount;
    private int fromInventory;

    public double getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(double actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getChecknumber() {
        return checknumber;
    }

    public void setChecknumber(String checknumber) {
        this.checknumber = checknumber;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public double getProductQuantity() {
        return productQuantity;
    }

    public void setProductQuantity(double productQuantity) {
        this.productQuantity = productQuantity;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getProductPurchasePrice() {
        return productPurchasePrice;
    }

    public void setProductPurchasePrice(double productPurchasePrice) {
        this.productPurchasePrice = productPurchasePrice;
    }

    public Date getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(Date businessDate) {
        this.businessDate = businessDate;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(double totalTax) {
        this.totalTax = totalTax;
    }

    public Discount getDiscount() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public int getFromInventory() {
        return fromInventory;
    }

    public void setFromInventory(int fromInventory) {
        this.fromInventory = fromInventory;
    }
    
}
