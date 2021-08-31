/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import java.util.Comparator;

/**
 *
 * @author Vipin Gupta
 */
public class ISTBufferSort {

    public static class BatchASC implements Comparator<ISTStockBuffer> {

        @Override
        public int compare(ISTStockBuffer o1, ISTStockBuffer o2) {
            return (int) (o1.getBatchNo() - o2.getBatchNo());
        }
    }

    public static class BatchDESC implements Comparator<ISTStockBuffer> {

        @Override
        public int compare(ISTStockBuffer o1, ISTStockBuffer o2) {
            return (int) (o2.getBatchNo() - o1.getBatchNo());
        }
    }
}
