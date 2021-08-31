/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class AssemblySubProductMapping {
    //for storing information of sub product lock quantity if assembly product is locked

    private String ID;
    private double quantity;   //lock quantity of subproduct if assembly type of product is locked in SO
    private Product product;  //Assembly main Product
    private Product subproducts;  //inventory type of subproduct in Assembly product

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
}
