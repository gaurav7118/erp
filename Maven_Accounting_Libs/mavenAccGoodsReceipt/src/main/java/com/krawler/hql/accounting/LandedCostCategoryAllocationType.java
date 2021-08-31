/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 * ERM-447 Class is used to store landing cost allocation categories.
 * @author swapnil.khandre
 */
public enum LandedCostCategoryAllocationType {

    QUANTITY(0), VALUE(1), WEIGHT(2), MANUAL(3);

    private int allocationType;

    private LandedCostCategoryAllocationType(int allocationType) {
        this.allocationType = allocationType;
    }

    public static LandedCostCategoryAllocationType getAllocationType(int categoryAllocationType) {
        LandedCostCategoryAllocationType allocationType = null;
        for (LandedCostCategoryAllocationType st : LandedCostCategoryAllocationType.values()) {
            if (st.ordinal() == categoryAllocationType) {
                allocationType = st;
                break;
            }
        }
        return allocationType;
    }
}
