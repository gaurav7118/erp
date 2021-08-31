/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.Company;

/**
 *
 * @author krawler
 */
public class ReplacementProductBatchDetailsMapping {

    private String id;
    private ProductReplacement productReplacement;
    private ProductReplacementDetail productReplacementDetail;
    private NewBatchSerial batchSerial;
    private Company company;

    public NewBatchSerial getBatchSerial() {
        return batchSerial;
    }

    public void setBatchSerial(NewBatchSerial batchSerial) {
        this.batchSerial = batchSerial;
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

    public ProductReplacement getProductReplacement() {
        return productReplacement;
    }

    public void setProductReplacement(ProductReplacement productReplacement) {
        this.productReplacement = productReplacement;
    }

    public ProductReplacementDetail getProductReplacementDetail() {
        return productReplacementDetail;
    }

    public void setProductReplacementDetail(ProductReplacementDetail productReplacementDetail) {
        this.productReplacementDetail = productReplacementDetail;
    }
}
