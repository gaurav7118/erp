/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 * Table to store mapping of Deskera Products with Items on AvaTax side
 * Used only when Avalara Integration is enabled
 */
public class ProductAvalaraIdMapping {

    private String productID;//UUID of product in Deksera
    private Product product;
    private String avalaraItemId;//ID of corresponding item in Avatax

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getAvalaraItemId() {
        return avalaraItemId;
    }

    public void setAvalaraItemId(String avalaraItemId) {
        this.avalaraItemId = avalaraItemId;
    }
}
