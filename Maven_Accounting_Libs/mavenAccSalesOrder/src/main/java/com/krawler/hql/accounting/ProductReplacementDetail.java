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
public class ProductReplacementDetail {

    private String id;
    private ProductReplacement productReplacement;
    private boolean isAsset;// indiactes that replacement product is asset or inventory
    private double replacementQuantity;
    private double replacedQuantity; // quantities which has been delivered i.e. do has been made by selecting product
    private boolean addInventoryFlag;// indicates that the product/asset to replace will be visible after replacement with another one.
    private Product product;
    private Company company;
    private Contract contract;
    private Set<ReplacementProductBatchDetailsMapping> replacementProductBatchDetailsMappings;// in case of asset group one Product can have multiple batch serials according to asset.

    public boolean isAddInventoryFlag() {
        return addInventoryFlag;
    }

    public void setAddInventoryFlag(boolean addInventoryFlag) {
        this.addInventoryFlag = addInventoryFlag;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isIsAsset() {
        return isAsset;
    }

    public void setIsAsset(boolean isAsset) {
        this.isAsset = isAsset;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ProductReplacement getProductReplacement() {
        return productReplacement;
    }

    public void setProductReplacement(ProductReplacement productReplacement) {
        this.productReplacement = productReplacement;
    }

    public Set<ReplacementProductBatchDetailsMapping> getReplacementProductBatchDetailsMappings() {
        return replacementProductBatchDetailsMappings;
    }

    public void setReplacementProductBatchDetailsMappings(Set<ReplacementProductBatchDetailsMapping> replacementProductBatchDetailsMappings) {
        this.replacementProductBatchDetailsMappings = replacementProductBatchDetailsMappings;
    }

    public double getReplacementQuantity() {
        return replacementQuantity;
    }

    public void setReplacementQuantity(double replacementQuantity) {
        this.replacementQuantity = replacementQuantity;
    }

    public double getReplacedQuantity() {
        return replacedQuantity;
    }

    public void setReplacedQuantity(double replacedQuantity) {
        this.replacedQuantity = replacedQuantity;
    }

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }
}
