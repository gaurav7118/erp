/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author Suhas C
 * 
 * Save Product Tax Class history at document level
 */
public class GstTaxClassHistory {

    private String id;
    private String productTaxClass;   // Tax Class Id  i.e. FCD ID
    private String refDocId;  // Transaction details ID e.g. Invoice details ID
    private int moduleId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductTaxClass() {
        return productTaxClass;
    }

    public void setProductTaxClass(String productTaxClass) {
        this.productTaxClass = productTaxClass;
    }

    public String getRefDocId() {
        return refDocId;
    }

    public void setRefDocId(String refDocId) {
        this.refDocId = refDocId;
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

}
