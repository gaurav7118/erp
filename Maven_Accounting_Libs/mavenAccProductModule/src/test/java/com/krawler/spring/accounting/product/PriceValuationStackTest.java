/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.product;

import com.krawler.common.util.ValuationMethod;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Vipin Gupta
 */
public class PriceValuationStackTest {

    @Test
    public void checkLifoValuationTransaction() {
        PriceValuationStack pvs = new PriceValuationStack();
        Map<String, String> storageDetail1 = new HashMap();
        storageDetail1.put("warehouseId", "1");
        storageDetail1.put("locationId", "2");

        double doprice1 = pvs.pushTransaction(TransactionBatch.DocType_DO, "100000", ValuationMethod.STANDARD, false, true, 2, 30, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        Assert.assertEquals("Checking price after DO-100000", 0.0, doprice1, 0.001);

        double price1 = pvs.pushTransaction(TransactionBatch.DocType_GRN, "100001", ValuationMethod.STANDARD, false, true, 3, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        Assert.assertEquals("Checking price after GRN-100001", 100.0, price1, 0.001);

        doprice1 = pvs.getTransactionPrice("100000");
        Assert.assertEquals("Checking price after DO-100000", 100.0, doprice1, 0.001);

        double price2 = pvs.pushTransaction(TransactionBatch.DocType_GRN, "100002", ValuationMethod.STANDARD, false, true, 6, 150, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        Assert.assertEquals("Checking price after GRN-100002", 150.0, price2, 0.001);

        double doprice2 = pvs.pushTransaction(TransactionBatch.DocType_DO, "100003", ValuationMethod.STANDARD, false, true, 4, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        //(4*150)/(4)
        Assert.assertEquals("Checking price after DO-100003", 150.0, doprice2, 0.001);



        pvs.clear();

    }

    @Test
    public void checkFifoValuationTransaction() {
        PriceValuationStack pvs = new PriceValuationStack();
        Map<String, String> storageDetail1 = new HashMap();
        storageDetail1.put("warehouseId", "1");
        storageDetail1.put("locationId", "2");

        double doprice1 = pvs.pushTransaction(TransactionBatch.DocType_DO, "100000", ValuationMethod.FIFO, false, true, 2, 30, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        Assert.assertEquals("Checking price after DO-100000", 0.0, doprice1, 0.001);

        double price1 = pvs.pushTransaction(TransactionBatch.DocType_GRN, "100001", ValuationMethod.FIFO, false, true, 3, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        Assert.assertEquals("Checking price after GRN-100001", 100.0, price1, 0.001);

        doprice1 = pvs.getTransactionPrice("100000");
        Assert.assertEquals("Checking price after DO-100000", 100.0, doprice1, 0.001);

        double price2 = pvs.pushTransaction(TransactionBatch.DocType_GRN, "100002", ValuationMethod.FIFO, false, true, 6, 150, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        Assert.assertEquals("Checking price after GRN-100002", 150.0, price2, 0.001);

        double doprice2 = pvs.pushTransaction(TransactionBatch.DocType_DO, "100003", ValuationMethod.FIFO, false, true, 4, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        //(1*100 + 3*150)/(1+3)
        Assert.assertEquals("Checking price after DO-100003", 137.5, doprice2, 0.001);

        pvs.clear();
    }

    @Test
    public void checkAverageValuationTransaction() {
        PriceValuationStack pvs = new PriceValuationStack();
        Map<String, String> storageDetail1 = new HashMap();
        storageDetail1.put("warehouseId", "1");
        storageDetail1.put("locationId", "2");
        double doprice1 = pvs.pushTransaction(TransactionBatch.DocType_DO, "100000", ValuationMethod.AVERAGE, false, true, 2, 30, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        Assert.assertEquals("Checking price after DO-100000", 0.0, doprice1, 0.001);

        double price1 = pvs.pushTransaction(TransactionBatch.DocType_GRN, "100001", ValuationMethod.AVERAGE, false, true, 3, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        Assert.assertEquals("Checking price after GRN-100001", 100.0, price1, 0.001);

        doprice1 = pvs.getTransactionPrice("100000");
        Assert.assertEquals("Checking price after overdue filling DO-100000", 100.0, doprice1, 0.001);

        double price2 = pvs.pushTransaction(TransactionBatch.DocType_GRN, "100002", ValuationMethod.AVERAGE, false, true, 6, 150, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        Assert.assertEquals("Checking price after GRN-100002", 150.0, price2, 0.001);

        double doprice2 = pvs.pushTransaction(TransactionBatch.DocType_DO, "100003", ValuationMethod.AVERAGE, false, true, 4, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        //(-2*100+3*100+6*150)/(-2+3+6)
        Assert.assertEquals("Checking price after DO-100003", 142.857, doprice2, 0.001);



        pvs.clear();
    }

    @Test
    public void checkLifoValuationAfterAllTransactions() {
        PriceValuationStack pvs = new PriceValuationStack();
        ValuationMethod valuationMethod = ValuationMethod.STANDARD;
        Map<String, String> storageDetail1 = new HashMap();
        storageDetail1.put("warehouseId", "1");
        storageDetail1.put("locationId", "2");
        Map<String, String> storageDetail2 = new HashMap();
        storageDetail2.put("warehouseId", "2");
        storageDetail2.put("locationId", "4");
        storageDetail2.put("rowId", "12");
        storageDetail2.put("rackId", "13");
        storageDetail2.put("binId", "14");

        pvs.pushTransaction(TransactionBatch.DocType_INITIAL, "99999", valuationMethod, false, true, 2, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_INITIAL, "99999", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100000", valuationMethod, false, true, 5, 200, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_OUT, "100001", valuationMethod, false, true, 2, 0, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_IN, "100002", valuationMethod, false, true, 1, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_IN, "100002", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_DO, "100003", valuationMethod, false, false, 2, 30, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SALES_RETURN, "100004", valuationMethod, false, false, 2, 300, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SALES_RETURN, "100004", valuationMethod, false, false, 3, 300, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_PURCHASE_RETURN, "100005", valuationMethod, false, false, 1, 400, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_PURCHASE_RETURN, "100005", valuationMethod, false, false, 2, 400, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());

        double openingPriceAll = pvs.getOpeningTransactionBatch().getPrice();
        Assert.assertEquals("Checking Opening Price for all", openingPriceAll, 137.5, 0.01);

        double openingPrice1 = pvs.getOpeningTransactionBatch(storageDetail1).getPrice();
        Assert.assertEquals("Checking Opening Price for storage 1", openingPrice1, 150, 0.01);

        double openingPrice2 = pvs.getOpeningTransactionBatch(storageDetail2).getPrice();
        Assert.assertEquals("Checking Opening Price for storage 2", openingPrice2, 100, 0.01);

        double periodPriceAll = pvs.getPeriodTransactionBatch().getPrice();
        Assert.assertEquals("Checking Period Price for all", periodPriceAll, 0, 0.01);

        double periodicPrice1 = pvs.getPeriodTransactionBatch(storageDetail1).getPrice();
        Assert.assertEquals("Checking Period Price for storage 1", periodicPrice1, 100, 0);

        double periodicPrice2 = pvs.getPeriodTransactionBatch(storageDetail2).getPrice();
        Assert.assertEquals("Checking Period Price for storage 2", periodicPrice2, 300, 0);

        double allTransPriceAll = pvs.getTransactionBatch().getPrice();
        Assert.assertEquals("Checking Transaction Price for all", allTransPriceAll, 162.5, 0.01);

        double allTransPrice1 = pvs.getTransactionBatch(storageDetail1).getPrice();
        Assert.assertEquals("Checking Transaction Price for storage 1", allTransPrice1, 160, 0.01);

        double allTransPrice2 = pvs.getTransactionBatch(storageDetail2).getPrice();
        Assert.assertEquals("Checking Transaction Price for storage 2", allTransPrice2, 166.67, 0.01);

        pvs.clear();

    }

    @Test
    public void checkFifoValuationAfterAllTransactions() {
        PriceValuationStack pvs = new PriceValuationStack();
        ValuationMethod valuationMethod = ValuationMethod.FIFO;
        Map<String, String> storageDetail1 = new HashMap();
        storageDetail1.put("warehouseId", "1");
        storageDetail1.put("locationId", "2");
        Map<String, String> storageDetail2 = new HashMap();
        storageDetail2.put("warehouseId", "2");
        storageDetail2.put("locationId", "4");
        storageDetail2.put("rowId", "12");
        storageDetail2.put("rackId", "13");
        storageDetail2.put("binId", "14");

        pvs.pushTransaction(TransactionBatch.DocType_INITIAL, "99999", valuationMethod, false, true, 2, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_INITIAL, "99999", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100000", valuationMethod, false, true, 5, 200, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_OUT, "100001", valuationMethod, false, true, 2, 0, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_IN, "100002", valuationMethod, false, true, 1, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_IN, "100002", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_DO, "100003", valuationMethod, false, false, 2, 30, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SALES_RETURN, "100004", valuationMethod, false, false, 2, 300, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SALES_RETURN, "100004", valuationMethod, false, false, 3, 300, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_PURCHASE_RETURN, "100005", valuationMethod, false, false, 1, 400, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_PURCHASE_RETURN, "100005", valuationMethod, false, false, 2, 400, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());

        double openingPriceAll = pvs.getOpeningTransactionBatch().getPrice();
        Assert.assertEquals("Checking Opening Price for all", openingPriceAll, 162.5, 0.01);

        double openingPrice1 = pvs.getOpeningTransactionBatch(storageDetail1).getPrice();
        Assert.assertEquals("Checking Opening Price for storage 1", openingPrice1, 183.34, 0.01);

        double openingPrice2 = pvs.getOpeningTransactionBatch(storageDetail2).getPrice();
        Assert.assertEquals("Checking Opening Price for storage 2", openingPrice2, 100, 0.01);

        double periodPriceAll = pvs.getPeriodTransactionBatch().getPrice();
        Assert.assertEquals("Checking Period Price for all", periodPriceAll, 0, 0.01);

        double periodicPrice1 = pvs.getPeriodTransactionBatch(storageDetail1).getPrice();
        Assert.assertEquals("Checking Period Price for storage 1", periodicPrice1, 400, 0);

        double periodicPrice2 = pvs.getPeriodTransactionBatch(storageDetail2).getPrice();
        Assert.assertEquals("Checking Period Price for storage 2", periodicPrice2, 700, 0);

        double allTransPriceAll = pvs.getTransactionBatch().getPrice();
        Assert.assertEquals("Checking Transaction Price for all", allTransPriceAll, 200, 0.01);

        double allTransPrice1 = pvs.getTransactionBatch(storageDetail1).getPrice();
        Assert.assertEquals("Checking Transaction Price for storage 1", allTransPrice1, 140, 0.01);

        double allTransPrice2 = pvs.getTransactionBatch(storageDetail2).getPrice();
        Assert.assertEquals("Checking Transaction Price for storage 2", allTransPrice2, 300, 0.01);

        pvs.clear();

    }

    @Test
    public void checkAverageValuationAfterAllTransactions() {
        PriceValuationStack pvs = new PriceValuationStack();
        ValuationMethod valuationMethod = ValuationMethod.AVERAGE;
        Map<String, String> storageDetail1 = new HashMap();
        storageDetail1.put("warehouseId", "1");
        storageDetail1.put("locationId", "2");
        Map<String, String> storageDetail2 = new HashMap();
        storageDetail2.put("warehouseId", "2");
        storageDetail2.put("locationId", "4");
        storageDetail2.put("rowId", "12");
        storageDetail2.put("rackId", "13");
        storageDetail2.put("binId", "14");

        pvs.pushTransaction(TransactionBatch.DocType_INITIAL, "99999", valuationMethod, false, true, 2, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_INITIAL, "99999", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100000", valuationMethod, false, true, 5, 200, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_OUT, "100001", valuationMethod, false, true, 2, 0, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_IN, "100002", valuationMethod, false, true, 1, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_IN, "100002", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_DO, "100003", valuationMethod, false, false, 2, 30, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SALES_RETURN, "100004", valuationMethod, false, false, 2, 300, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SALES_RETURN, "100004", valuationMethod, false, false, 3, 300, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_PURCHASE_RETURN, "100005", valuationMethod, false, false, 1, 400, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_PURCHASE_RETURN, "100005", valuationMethod, false, false, 2, 400, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());

        double openingPriceAll = pvs.getOpeningTransactionBatch().getPrice();
        Assert.assertEquals("Checking Opening Price for all", openingPriceAll, 144.64, 0.01);

        double openingPrice1 = pvs.getOpeningTransactionBatch(storageDetail1).getPrice();
        Assert.assertEquals("Checking Opening Price for storage 1", openingPrice1, 159.52, 0.01);

        double openingPrice2 = pvs.getOpeningTransactionBatch(storageDetail2).getPrice();
        Assert.assertEquals("Checking Opening Price for storage 2", openingPrice2, 100, 0.01);

        double periodPriceAll = pvs.getPeriodTransactionBatch().getPrice();
        Assert.assertEquals("Checking Period Price for all", periodPriceAll, 0, 0.01);

        double periodicPrice1 = pvs.getPeriodTransactionBatch(storageDetail1).getPrice();
        Assert.assertEquals("Checking Period Price for storage 1", periodicPrice1, 159.52, 0.01);

        double periodicPrice2 = pvs.getPeriodTransactionBatch(storageDetail2).getPrice();
        Assert.assertEquals("Checking Period Price for storage 2", periodicPrice2, 460, 0);

        double allTransPriceAll = pvs.getTransactionBatch().getPrice();
        Assert.assertEquals("Checking Transaction Price for all", allTransPriceAll, 182.20, 0.01);

        double allTransPrice1 = pvs.getTransactionBatch(storageDetail1).getPrice();
        Assert.assertEquals("Checking Transaction Price for storage 1", allTransPrice1, 159.52, 0.01);

        double allTransPrice2 = pvs.getTransactionBatch(storageDetail2).getPrice();
        Assert.assertEquals("Checking Transaction Price for storage 2", allTransPrice2, 220, 0.01);

        pvs.clear();

    }
    
    @Test
    public void testPartialStorage(){
        PriceValuationStack pvs = new PriceValuationStack();
        
        ValuationMethod valuationMethod = ValuationMethod.STANDARD;
        
        Map<String, String> storageDetail1 = new HashMap();
        storageDetail1.put("warehouseId", "1");
        storageDetail1.put("locationId", "2");
        storageDetail1.put("batchName", "BAt");
        Map<String, String> storageDetail2 = new HashMap();
        storageDetail2.put("warehouseId", "2");
        storageDetail2.put("locationId", "4");
        storageDetail2.put("batchName", "BAT1");
        Map<String, String> storageDetail3 = new HashMap();
        storageDetail3.put("warehouseId", "1");
        storageDetail3.put("locationId", "4");
        storageDetail3.put("batchName", "BAT1");
        Map<String, String> storageDetail4 = new HashMap();
        storageDetail4.put("warehouseId", "2");
        storageDetail4.put("locationId", "4");
        storageDetail4.put("batchName", "14");
        
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "99999", valuationMethod, false, true, 2, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100003", valuationMethod, false, false, 2, 30, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100005", valuationMethod, false, false, 2, 400, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100004", valuationMethod, false, false, 2, 300, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100005", valuationMethod, false, false, 2, 400, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "99999", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100002", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100002", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100000", valuationMethod, false, true, 5, 200, storageDetail3,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100004", valuationMethod, false, false, 3, 300, storageDetail3,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100005", valuationMethod, false, false, 1, 400, storageDetail3,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100001", valuationMethod, false, true, 2, 300, storageDetail4,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100005", valuationMethod, false, false, 2, 400, storageDetail4,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100005", valuationMethod, false, false, 2, 400, storageDetail4,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        
        Map<String, String> pstorageDetail = new HashMap();
        pstorageDetail.put("warehouseId", "2");
        List list = pvs.getTransactionBatchList(null, pstorageDetail, true);
        Assert.assertEquals("Checking warehouse 2 partial list size", 7, list.size(), 0);
        
        pstorageDetail.clear();
        pstorageDetail.put("locationId", "4");
        list = pvs.getTransactionBatchList(null, pstorageDetail, true);
        Assert.assertEquals("Checking location 4 partial list size", 10, list.size(), 0);
        
        pstorageDetail.clear();
        pstorageDetail.put("rowId", "Row1");
        list = pvs.getTransactionBatchList(null, pstorageDetail, true);
        Assert.assertEquals("Checking row Row1 partial list size", 0, list.size(), 0);
        
        pstorageDetail.clear();
        pstorageDetail.put("batchName", "BAt");
        list = pvs.getTransactionBatchList(null, pstorageDetail, true);
        Assert.assertEquals("Checking batch BAt partial list size", 4, list.size(), 0);
        
        pstorageDetail.clear();
        list = pvs.getTransactionBatchList(null, pstorageDetail, true);
        Assert.assertEquals("Checking without storage partial list size", 14, list.size(), 0);
        
        pstorageDetail.clear();
        pstorageDetail.put("warehouseId", "2");
        pstorageDetail.put("batchName", "BAT1");
        list = pvs.getTransactionBatchList(null, pstorageDetail, true);
        Assert.assertEquals("Checking warehouse 2 and batch BAT1 partial list size", 4, list.size(), 0);
        
        pstorageDetail.clear();
        pstorageDetail.put("locationId", "4");
        pstorageDetail.put("batchName", "BAT1");
        list = pvs.getTransactionBatchList(null, pstorageDetail, true);
        Assert.assertEquals("Checking location 4 and batch BAT1 partial list size", 7, list.size(), 0);
        
        pstorageDetail.clear();
        pstorageDetail.put("rowId", "Row1");
        pstorageDetail.put("batchName", "BAT1");
        list = pvs.getTransactionBatchList(null, pstorageDetail, true);
        Assert.assertEquals("Checking row Row1 partial and batch BAT1 list size", 0, list.size(), 0);
        
        pvs.clear();
        
    }
    
    @Test
    public void checkFifoValuationPartial() {
        PriceValuationStack pvs = new PriceValuationStack();
        ValuationMethod valuationMethod = ValuationMethod.FIFO;
        
        Map<String, String> storageDetail1 = new HashMap();
        storageDetail1.put("warehouseId", "1");
        storageDetail1.put("locationId", "2");
        storageDetail1.put("batchName", "4");
        
        Map<String, String> storageDetail2 = new HashMap();
        storageDetail2.put("warehouseId", "2");
        storageDetail2.put("locationId", "7");
        storageDetail2.put("batchName", "4");
        
        Map<String, String> storageDetail3 = new HashMap();
        storageDetail3.put("warehouseId", "1");
        storageDetail3.put("locationId", "2");
        storageDetail3.put("batchName", "4");
        
        Map<String, String> storageDetail4 = new HashMap();
        storageDetail4.put("warehouseId", "2");
        storageDetail4.put("locationId", "4");
        storageDetail4.put("batchName", "4");

        pvs.pushTransaction(TransactionBatch.DocType_INITIAL, "99999", valuationMethod, false, true, 2, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_INITIAL, "99999", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100000", valuationMethod, false, true, 5, 200, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_OUT, "100001", valuationMethod, false, true, 2, 0, storageDetail3,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_IN, "100002", valuationMethod, false, true, 1, 100, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_IN, "100002", valuationMethod, false, true, 1, 100, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_DO, "100003", valuationMethod, false, false, 2, 30, storageDetail1,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SALES_RETURN, "100004", valuationMethod, false, false, 2, 300, storageDetail4,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SALES_RETURN, "100004", valuationMethod, false, false, 3, 300, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_PURCHASE_RETURN, "100005", valuationMethod, false, false, 1, 400, storageDetail3,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_PURCHASE_RETURN, "100005", valuationMethod, false, false, 2, 400, storageDetail4,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100005", valuationMethod, false, false, 4, 400, storageDetail3,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_GRN, "100005", valuationMethod, false, false, 2, 400, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_DO, "100005", valuationMethod, false, false, 1, 400, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_IN, "100005", valuationMethod, false, false, 2, 400, storageDetail2,"","","",null,"", null, null,false,"","","","", "", new JSONObject());
        pvs.pushTransaction(TransactionBatch.DocType_SA_OUT, "100005", valuationMethod, false, false, 1, 400, storageDetail3,"","","",null,"", null, null,false,"","","","", "", new JSONObject());

        Map<String, String> pstorageDetail = new HashMap();
        pstorageDetail.put("warehouseId", "2");
        pstorageDetail.put("batchName", "4");
        List<TransactionBatch> tbList = pvs.getTransactionBatchList(null, pstorageDetail, true);
        Map<String, PriceValuationStack.Batch> batchMap = pvs.getStorageDetailwiseQuantityBatch(tbList, new PriceValuationStack.StorageFilter[]{PriceValuationStack.StorageFilter.WAREHOUSE, PriceValuationStack.StorageFilter.BATCH}, false, true);
        Iterator<String> itr = batchMap.keySet().iterator();
        double qty = 0;
        while(itr.hasNext()){
            PriceValuationStack.Batch batch = batchMap.get(itr.next());
            qty += batch.getQuantity();
        }
        Assert.assertEquals("Checking partial quantity", 6, qty,0);
        pvs.clear();

    }
}
