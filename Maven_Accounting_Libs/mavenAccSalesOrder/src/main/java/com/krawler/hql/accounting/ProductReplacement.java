/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.BatchSerial;
import com.krawler.common.admin.Company;
import java.util.Set;

/**
 *
 * @author krawler
 */
public class ProductReplacement {

    private String id;
    private String replacementRequestNumber;
    private boolean closed;// after creation of DO of all the replacement items closed flag will be true.
    private Set<ProductReplacementDetail> productReplacementDetails;
//    private boolean isAsset;// indiactes that replacement product is asset or inventory
//    private double replacementQuantity;
//    private boolean addInventoryFlag;// indicates that the product/asset to replace will be visible after replacement with another one.
//    private BatchSerial batchSerial;
//    private Product product;
    private Customer customer;
    private Contract contract;
    private boolean salesContractReplacement;
    private Company company;
    private String description;

    public Contract getContract() {
        return contract;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public String getReplacementRequestNumber() {
        return replacementRequestNumber;
    }

    public void setReplacementRequestNumber(String replacementRequestNumber) {
        this.replacementRequestNumber = replacementRequestNumber;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Set<ProductReplacementDetail> getProductReplacementDetails() {
        return productReplacementDetails;
    }

    public void setProductReplacementDetails(Set<ProductReplacementDetail> productReplacementDetails) {
        this.productReplacementDetails = productReplacementDetails;
    }

    public boolean isSalesContractReplacement() {
        return salesContractReplacement;
    }

    public void setSalesContractReplacement(boolean salesContractReplacement) {
        this.salesContractReplacement = salesContractReplacement;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
