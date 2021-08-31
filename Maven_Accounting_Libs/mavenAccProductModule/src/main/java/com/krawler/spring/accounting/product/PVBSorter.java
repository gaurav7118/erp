/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.product;

import java.util.Comparator;

/**
 *
 * @author Vipin Gupta
 */
public class PVBSorter {
    
    public static class sortASC implements Comparator<TransactionBatch> {

        @Override
        public int compare(TransactionBatch o1, TransactionBatch o2) {
            return o1.getBatchNo() - o2.getBatchNo();
        }
        
    }
    public static class sortDESC implements Comparator<TransactionBatch> {

        @Override
        public int compare(TransactionBatch o1, TransactionBatch o2) {
            return o2.getBatchNo() - o1.getBatchNo();
        }
        
    }
    
}
