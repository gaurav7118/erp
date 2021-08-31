/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.FieldComboData;

/**
 *ERP-32829 
 * @author Suhas Chaware
 */
public class ProductCategoryGstRulesMappping {

    private String id;
    private FieldComboData prodCategory;  // Product category dimension
    private EntitybasedLineLevelTermRate entitybasedLineLevelTermRate;  // GST rule

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FieldComboData getProdCategory() {
        return prodCategory;
    }

    public void setProdCategory(FieldComboData prodCategory) {
        this.prodCategory = prodCategory;
    }

    public EntitybasedLineLevelTermRate getEntitybasedLineLevelTermRate() {
        return entitybasedLineLevelTermRate;
    }

    public void setEntitybasedLineLevelTermRate(EntitybasedLineLevelTermRate entitybasedLineLevelTermRate) {
        this.entitybasedLineLevelTermRate = entitybasedLineLevelTermRate;
    }

}
