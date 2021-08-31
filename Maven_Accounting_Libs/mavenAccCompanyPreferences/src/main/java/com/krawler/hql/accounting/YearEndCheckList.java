/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.hql.accounting;

import com.krawler.common.admin.Company;

public class YearEndCheckList {

    private String id;
    private boolean documentRevaluationCompleted;
    private boolean adjustmentForTransactionCompleted;
    private boolean inventoryAdjustmentCompleted;
    private boolean assetDepreciationPosted;
    private YearLock yearlock;
    private Company company;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDocumentRevaluationCompleted() {
        return documentRevaluationCompleted;
    }

    public void setDocumentRevaluationCompleted(boolean documentRevaluationCompleted) {
        this.documentRevaluationCompleted = documentRevaluationCompleted;
    }

    public boolean isAdjustmentForTransactionCompleted() {
        return adjustmentForTransactionCompleted;
    }

    public void setAdjustmentForTransactionCompleted(boolean adjustmentForTransactionCompleted) {
        this.adjustmentForTransactionCompleted = adjustmentForTransactionCompleted;
    }

    public boolean isInventoryAdjustmentCompleted() {
        return inventoryAdjustmentCompleted;
    }

    public void setInventoryAdjustmentCompleted(boolean inventoryAdjustmentCompleted) {
        this.inventoryAdjustmentCompleted = inventoryAdjustmentCompleted;
    }

    public boolean isAssetDepreciationPosted() {
        return assetDepreciationPosted;
    }

    public void setAssetDepreciationPosted(boolean assetDepreciationPosted) {
        this.assetDepreciationPosted = assetDepreciationPosted;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public YearLock getYearlock() {
        return yearlock;
    }

    public void setYearlock(YearLock yearlock) {
        this.yearlock = yearlock;
    }

}
