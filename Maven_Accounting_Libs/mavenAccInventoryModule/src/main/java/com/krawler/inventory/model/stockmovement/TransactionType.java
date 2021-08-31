/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockmovement;

import com.krawler.spring.accounting.product.TransactionBatch;

/**
 *
 * @author Vipin Gupta
 */
public enum TransactionType {

    OPENING, IN, OUT;

    public static TransactionType getTransactionType(int doctype) {
        TransactionType transType = null;
        switch (doctype) {
            case TransactionBatch.DocType_INITIAL:
                transType = TransactionType.OPENING;
                break;
            case TransactionBatch.DocType_ASSEMBLY_MAIN:
            case TransactionBatch.DocType_GRN:
            case TransactionBatch.DocType_OPENING:
            case TransactionBatch.DocType_SA_IN:
            case TransactionBatch.DocType_SR_COLLECT:
            case TransactionBatch.DocType_IN_COLLECT:
            case TransactionBatch.DocType_IST_COLLECT:
            case TransactionBatch.DocType_ILT_COLLECT:
            case TransactionBatch.DocType_SALES_RETURN:
            case TransactionBatch.DocType_WO_IN:    
                transType = TransactionType.IN;
                break;
            case TransactionBatch.DocType_ASSEMBLY_SUB:
            case TransactionBatch.DocType_DO:
            case TransactionBatch.DocType_SA_OUT:
            case TransactionBatch.DocType_SR_ISSUE:
            case TransactionBatch.DocType_IN_ISSUE:
            case TransactionBatch.DocType_IST_ISSUE:
            case TransactionBatch.DocType_ILT_ISSUE:
            case TransactionBatch.DocType_PURCHASE_RETURN:
            case TransactionBatch.DocType_WO_OUT:    
                transType = TransactionType.OUT;
                break;
        }
        return transType;
    }

}
