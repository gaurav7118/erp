/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockrequest;

import java.util.Comparator;

/**
 *
 * @author Vipin Gupta
 */
public class SRBufferSort {

    public static class BatchASC implements Comparator<SRStockBuffer> {

        @Override
        public int compare(SRStockBuffer o1, SRStockBuffer o2) {
            return (int)(o1.getBatchNo() - o2.getBatchNo());
        }
    }

    public static class BatchDESC implements Comparator<SRStockBuffer> {

        @Override
        public int compare(SRStockBuffer o1, SRStockBuffer o2) {
            return (int)(o2.getBatchNo() - o1.getBatchNo());
        }
    }
}
