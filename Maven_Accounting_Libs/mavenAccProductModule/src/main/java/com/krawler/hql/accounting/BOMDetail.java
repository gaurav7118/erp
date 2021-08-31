/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class BOMDetail {

    private String ID;
    private String bomCode;
    private String bomName;
    private boolean isDefaultBOM;
    private Product product;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getBomCode() {
        return bomCode;
    }

    public void setBomCode(String bomCode) {
        this.bomCode = bomCode;
    }

    public String getBomName() {
        return bomName;
    }

    public void setBomName(String bomName) {
        this.bomName = bomName;
    }

    public boolean isIsDefaultBOM() {
        return isDefaultBOM;
    }

    public void setIsDefaultBOM(boolean isDefaultBOM) {
        this.isDefaultBOM = isDefaultBOM;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

}
