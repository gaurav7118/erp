/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stock;

import java.util.Comparator;

/**
 *
 * @author Vipin Gupta
 */
public class StockSort {

    public static class BatchDESC implements Comparator<Stock> {

        @Override
        public int compare(Stock o1, Stock o2) {
            return (int) (o2.getBatchNo() - o1.getBatchNo());
        }
    }
    public static class BatchASC implements Comparator<Stock> {

        @Override
        public int compare(Stock o1, Stock o2) {
            return (int) (o1.getBatchNo() - o2.getBatchNo());
        }
    }
}
