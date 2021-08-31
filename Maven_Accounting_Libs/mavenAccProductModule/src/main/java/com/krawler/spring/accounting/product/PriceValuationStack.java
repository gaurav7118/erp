/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.product;

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.ValuationMethod;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;

/**
 * This class used to calculate price valuation according to IN-OUT transactions
 * for a product
 *
 * <br /><br />use pushTransaction method to add transaction in stack <br
 * />Transactions must be in ascending date sequence <br />Stack saved in Map
 * with storage detail key for iteration optimization <br />It will iterate list
 * only for one storage detail
 *
 * @see pushTransaction(int docType, String transId, ValuationMethod
 * valuationMethod, boolean linkedTransaction, boolean isOpening, double
 * quantity, double price, Map<String, String> storageDetail, String personCode,
 * String personName, String transactionNo, Date tranDate, String billid)
 *
 * @author Vipin Gupta
 */
public class PriceValuationStack {

    /**
     * In map storageDetailTrigger, storage details of a transactions are stored
     * while pushing a transaction into the stack storage details are details of
     * warehouse, location, batch, serial, row, rack, bin Details of a
     * transaction are cleared from map right after pushing the transaction into
     * the stack
     */
    private Map<String, String> storageDetailTrigger  = new HashMap();
    /**
     * boolean flag descOrder indicates the order in which the list of
     * TransactionBatch objects is to be sorted this flag's value is set
     * according to valuation type because list of TransactionBatch objects is
     * required in ascending or descending order for different types of
     * valuations FIFO, AVERAGE -> false LIFO (STANDARD) -> true
     */
    private boolean descOrder;
    /**
     * In storageBatchMap, list of all TransactionBatch objects corresponding to a storage key are kept mapped to the storage key
     * storage key is created by appending together ID's of warehouse, location, batch, serial, row, rack, bin
     * Key: storage key created in method {@link #getStoregeKey(Map storageParams)}
     * Value: List of all TransactionBatch objects corresponding to storage
     */
    private Map<String, List<TransactionBatch>> storageBatchMap = new HashMap();
    /**
     * In transIdStorageMap, storage keys corresponding to storages which are used in a transaction are kept mapped to ID of transaction
     * Key: ID of transaction
     * Value: Set containing storage keys corresponding to all storages used in the transaction
     */
    private Map<String, Set<String>> transIdStorageMap = new HashMap();
    /**
     * In transIdTypeMap, a transaction's type is kept mapped to the ID of transaction
     * Key: ID of transaction
     * Value: Type ID of transaction 
     * Type ID of a transaction is value of transType column in select query in method 'accProductImpl.getStockLedger'
     * All values of Type IDs can be found listed in method {@link #getTransType(int docType)} as values of 'docType' variable
     * Transaction-Module name corresponding to a Type ID can be found in the method {@link #getTransactionModule(int doctype)}
     */
    private Map<String, Integer> transIdTypeMap = new HashMap();
    public static final String isAdvanceSearchTransaction = "isAdvanceSearchTransaction";
    /**
     * this method will remove all data from stack.
     */
    public void clear() {
        storageDetailTrigger.clear();
        storageBatchMap.clear();
        transIdStorageMap.clear();
        transIdTypeMap.clear();
        System.gc();
    }

    /**
     * This method is used to add IN-OUT transactions in Stack and return
     * calculated the price of added transaction
     *
     * <br /><br />if overdue quantity(doQty - availableQtyInStack) is there, It
     * will set overdue quantity with 0 price for DO transaction (LIFO AND FIFO
     * only) and filled GRN price when GRN added next time <br />for Average
     * valuation if there is any IN transaction then calculated average price is
     * applicable for DO all quantity otherwise set overdue quantity.
     *
     * @param docType (required) this is used for document type like GRN, DO
     * etc. see constant DocType in TransactionBatch
     * @param transId (required) transactionid (detail) of transaction.
     * @param valuationMethod (optional) STANDARD(LIFO), FIFO, AVERAGE. if null
     * then default Valuation Method is STANDARD
     * @param linkedTransaction (required) if Purchase Return or Sales Return
     * Document linked to GRN or DO then it will be true otherwise false
     * @param isOpening (required) if transaction is opening then true otherwise
     * false
     * @param quantity (required) quantity in stock uom
     * @param price (required) for IN transaction, it is transaction purchase
     * price. for OUT transaction, it is transaction sales price.
     * @param storageDetail (optional) it will contains information about
     * storage of transaction like warehouseId, locationId, rowId, rackId,
     * binId, batchName, serialName as keys in Map
     * @param personCode Customer/Vendor code
     * @param personName Customer/Vendor name
     * @param transactionNo Transaction number
     * @param tranDate Date of transaction
     * @param billid ID for the transaction
     * @param srNo
     * @param createdon
     * @param isPeriodTransaction (required) If true, consider the transaction as Period Transaction
     * @param stockUOMID ID for UnitOf Measure
     * @param assemblyProductID Assembly Product ID to be used to get the assembly product ID.
     * @param remark Remark for StockMovement
     * @param costCenterID Remark for StockMovement
     * @param memo
     * @return double, return the calculated price for added transaction.
     */
    public double pushTransaction(int docType, String transId, ValuationMethod valuationMethod, boolean linkedTransaction, boolean isOpening, double quantity, double price, Map<String, String> storageDetail, String personCode, String personName, String transactionNo, Date tranDate, String billid, Integer srNo, Long createdon, boolean isPeriodTransaction, String remark, String assemblyProductID, String costCenterID, String stockUOMID, String memo, JSONObject json) {
        storageDetailTrigger.clear();
        if(storageDetail != null && !storageDetail.isEmpty()){
            storageDetailTrigger.putAll(storageDetail);
        }
        TransType transType = getTransType(docType);
        if (transType == null) {
            throw new IllegalArgumentException("invalid docType");
        }
        if (transId == null) {
            throw new IllegalArgumentException("transId is required");
        }
        if (valuationMethod == null) {
            valuationMethod = ValuationMethod.STANDARD;
        }
        transIdTypeMap.put(transId, docType);

        String storageKey = getStoregeKey(storageDetail);

        List<TransactionBatch> pvbList;
        if (storageBatchMap.containsKey(storageKey) && storageBatchMap.get(storageKey) != null) {
            pvbList = storageBatchMap.get(storageKey);
            if (valuationMethod == ValuationMethod.STANDARD) { //LIFO
                descOrder = true;
                Collections.sort(pvbList, new PriceBatchSorter.sortDESC());  // for LIFO
            }else {
                descOrder = false;
//                Collections.sort(pvbList, new PriceBatchSorter.sortASC());  // for FIFO or Average // commented because list is always in asc order sequence
            }
        } else {
            pvbList = new ArrayList();
            storageBatchMap.put(storageKey, pvbList);
        }

        double amount = 0;
        switch (transType) {
            case IN:
                amount = addInTransaction(docType, pvbList, valuationMethod, transId, linkedTransaction, isOpening, quantity, price, storageKey, personCode, personName, transactionNo, tranDate, billid, srNo, createdon, isPeriodTransaction, remark, assemblyProductID, costCenterID, stockUOMID, memo, json);
                break;
            case OUT_RETURN:
                amount = addOutReturnTransaction(docType, pvbList, valuationMethod, transId, linkedTransaction, isOpening, quantity, price, storageKey, personCode, personName, transactionNo, tranDate, billid, srNo, createdon, isPeriodTransaction, remark, assemblyProductID, costCenterID, stockUOMID, memo, json);
                break;
            case OUT:
                amount = addOutTransaction(docType, pvbList, valuationMethod, transId, linkedTransaction, isOpening, quantity, price, storageKey, personCode, personName, transactionNo, tranDate, billid, srNo, createdon, isPeriodTransaction, remark, assemblyProductID, costCenterID, stockUOMID, memo, json);
                break;
            case IN_RETURN:
                amount = addInReturnTransaction(docType, pvbList, valuationMethod, transId, linkedTransaction, isOpening, quantity, price, storageKey, personCode, personName, transactionNo, tranDate, billid, srNo, createdon, isPeriodTransaction, remark, assemblyProductID, costCenterID, stockUOMID, memo, json);
                break;
        }
        return quantity == 0 ? 0 : amount / quantity;

    }

    /**
     * This method will return the all transactions summary in Map (calculated
     * total Quantity and total Amount of a transaction).
     *
     * @return Map, which contains transaction id as Key and Batch object which
     * contains totalAmount, totalQuantity, calculated price of transaction.
     */
    public Map<String, Batch> getAllTransactionBatch() {
        return getAllTransactionBatches(null, null, null, null);
    }
    
    /**
     * This method returns the calculated price of a transaction for all storage
     * details in stack
     *
     * <br /><br />Note: it should use when you want to get updated price after
     * adding all transactions like DO with overdue quantity.
     *
     * @param transactionId
     * @return double calculated price
     */
    public double getTransactionPrice(String transactionId) {
        return getTransactionPrice(transactionId, null);
    }

    /**
     * This method returns the calculated price of a transaction for particular
     * storage details in stack
     *
     * <br /><br />Note: it should use when you want to get updated price after
     * adding all transactions like DO with overdue quantity.
     *
     * @param transactionId
     * @param storageDetail
     *
     * @return double, calculated price
     */
    public double getTransactionPrice(String transactionId, Map<String, String> storageDetail) {
        Batch batch = getTransactionBatch(transactionId, storageDetail);
        if (batch != null) {
            return batch.getPrice();
        }
        return 0;
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of all transaction in stack
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @return Batch
     */
    public Batch getTransactionBatch() {
        return getTransactionBatch(null, null);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of all transaction with particular storage
     * detail in stack
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param storageDetail
     * @return Batch
     */
    public Batch getTransactionBatch(Map<String, String> storageDetail) {
        return getTransactionBatch(null, storageDetail);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of particular transaction with all storage
     * detail in stack
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param transactionId
     * @return Batch
     */
    public Batch getTransactionBatch(String transactionId) {
        return getTransactionBatch(transactionId, null);
    }

    /**
     * This method will return the transaction summary for a transactionId
     * (calculated total Quantity and total Amount of a transaction).
     *
     * @param transactionId transactionid which used to add transaction in stack
     * @param storageDetail stotagedetail which used to add transaction in stack
     * @return Batch, which contains totalAmount, totalQuantity, calculated
     * price of transaction.
     */
    public Batch getTransactionBatch(String transactionId, Map<String, String> storageDetail) {
        return getAllTransactionBatch(transactionId, storageDetail, null, null, null);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of all transaction in stack without
     * considering opening
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @return
     */
    public Batch getPeriodTransactionBatch() {
        return getPeriodTransactionBatch(null, null);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of all transaction with particular storage
     * detail in stack without considering opening
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param storageDetail
     * @return Batch
     */
    public Batch getPeriodTransactionBatch(Map<String, String> storageDetail) {
        return getPeriodTransactionBatch(null, storageDetail);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of particular transaction with all storage
     * detail in stack without considering opening
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param transactionId
     * @return Batch
     */
    public Batch getPeriodTransactionBatch(String transactionId) {
        return getPeriodTransactionBatch(transactionId, null);
    }

    /**
     * This method will return the transaction summary for a transactionId
     * (calculated total Quantity and total Amount of a transaction) without
     * considering opening.
     *
     * @param transactionId transactionid which used to add transaction in stack
     * @param storageDetail stotagedetail which used to add transaction in stack
     * @return Batch, which contains totalAmount, totalQuantity, calculated
     * price of transaction.
     */
    public Batch getPeriodTransactionBatch(String transactionId, Map<String, String> storageDetail) {
        return getAllTransactionBatch(transactionId, storageDetail, false, null, null);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of all opening transaction in stack
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @return Batch
     */
    public Batch getOpeningTransactionBatch() {
        return getOpeningTransactionBatch(null, null);
    }

    public Batch getInitialTransactionBatch() {
        return getInitialTransactionBatch(null, null);
    }

    public Batch getInitialTransactionBatch(String transactionId, Map<String, String> storageDetail) {
        return getAllTransactionBatch(transactionId, storageDetail, null, false, true, true, null);
    }
    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of all opening transaction with particular
     * storage detail in stack
     *
     * <br /><br />note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param storageDetail
     * @return Batch
     */
    public Batch getOpeningTransactionBatch(Map<String, String> storageDetail) {
        return getOpeningTransactionBatch(null, storageDetail);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of particular opening transaction with all
     * storage detail in stack
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param transactionId
     * @return Batch
     */
    public Batch getOpeningTransactionBatch(String transactionId) {
        return getOpeningTransactionBatch(transactionId, null, null);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of particular opening transaction with
     * particular storage detail in stack
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param transactionId
     * @param storageDetail
     * @return Batch
     */
    public Batch getOpeningTransactionBatch(String transactionId, Map<String, String> storageDetail) {
        return getAllTransactionBatch(transactionId, storageDetail, true, null, null);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of particular opening transaction with
     * particular storage detail in stack
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param transactionId
     * @param storageDetail
     * @param isAdvanceSearchForValuation (optional) null - for all
     * transactions, true - for only advance search transactions
     * @return Batch
     */
    public Batch getOpeningTransactionBatch(String transactionId, Map<String, String> storageDetail, Boolean isAdvanceSearchForValuation) {
//        return getAllTransactionBatch(transactionId, storageDetail, true, null, null, null);
        return getAllTransactionBatch(transactionId, storageDetail, false, true, null, null, isAdvanceSearchForValuation);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of all opening transaction in stack
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param isAdvanceSearchForValuation (optional) null - for all
     * transactions, true - for only advance search transactions
     * @return Batch
     */
    public Batch getOpeningTransBatch(Boolean isAdvanceSearchForValuation) {
        return getOpeningTransactionBatch(null, null, isAdvanceSearchForValuation);
    }

    /**
     * This method returns Batch object which contains calculated price,
     * totalQuantity, totalAmount of all transaction in stack without
     * considering opening
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param isAdvanceSearchForValuation
     * @return
     */
    public Batch getPeriodTransBatch(Boolean isAdvanceSearchForValuation) {
        return getPeriodTransactionBatch(null, null, isAdvanceSearchForValuation);
    }

    /**
     * This method will return the transaction summary for a transactionId
     * (calculated total Quantity and total Amount of a transaction) without
     * considering opening.
     *
     * @param transactionId transactionid which used to add transaction in stack
     * @param storageDetail stotagedetail which used to add transaction in stack
     * @param isAdvanceSearchForValuation (optional) null - for all
     * transactions, true - for only advance search transactions
     * @return Batch, which contains totalAmount, totalQuantity, calculated
     * price of transaction.
     */
    public Batch getPeriodTransactionBatch(String transactionId, Map<String, String> storageDetail, Boolean isAdvanceSearchForValuation) {
        return getAllTransactionForPeriodBatch(transactionId, storageDetail, false, null, null, isAdvanceSearchForValuation);
    }
    /**
     * return the map of total amount and quantity batches for all transaction.
     *
     * @param transactionId (optional)
     * @param storageDetail (optional)
     * @param opening (optional) null - for all transaction, true - for only
     * opening transactions, false for periodic transactions
     * @param outTransaction (optional) null - for all transactions, true - for
     * only out transactions, false - for only IN transactions
     * @param initialBatch (optional) null - for all transactions, true - for
     * only Initial Transaction
     * @param isAdvanceSearchForValuation (optional) null - for all
     * transactions, true - for only advance search transactions
     * @return Batch - which contains total quantity and total amount of all
     * transactions
     */
    public Batch getAllTransactionForPeriodBatch(String transactionId, Map<String, String> storageDetail, Boolean opening, Boolean outTransaction, Boolean initialBatch, Boolean isAdvanceSearchForValuation) {
        return getAllTransactionBatch(transactionId, storageDetail, false, opening, outTransaction, initialBatch, isAdvanceSearchForValuation);
    }
    
    public Batch getInitialTransBatch(Boolean isAdvanceSearchForValuation) {
        return getInitialTransactionBatch(null, null, isAdvanceSearchForValuation);
    }

    public Batch getInitialTransactionBatch(String transactionId, Map<String, String> storageDetail, Boolean isAdvanceSearchForValuation) {
        return getAllTransactionBatch(transactionId, storageDetail, null, false, true, true, isAdvanceSearchForValuation);
    }
    /**
     * This method returns Map which contains transactionid as Key and Batch as
     * Value which contains calculated price, totalQuantity, totalAmount of
     * particular transaction in stack
     *
     * <br /><br />Note: it should use when you want to get opening quantity and
     * price for all transactions after adding all transactions.
     *
     * @param (optional)transactionId
     * @param (optional)storageDetail
     * @return Map
     */
    public Map<String, Batch> getAllOpeningTransactionBatches(String transactionId, Map<String, String> storageDetail) {
        return getAllTransactionBatches(transactionId, storageDetail, true, null);
    }

    /**
     * return the map of total amount and quantity batches per transaction.
     *
     * @param transactionId (optional)
     * @param storageDetail (optional)
     * @param opening (optional) null - for all transaction, true - for only
     * opening transactions, false for periodic transactions
     * @param outTransaction (optional) null - for all transactions, true - for
     * only out transactions, false - for only IN transactions
     * @return Map - transactionId as Key and Batch as value which contains
     * total quantity and total amount of that transactionId
     */
    public Map<String, Batch> getAllTransactionBatches(String transactionId, Map<String, String> storageDetail, Boolean opening, Boolean outTransaction) {
        return getAllTransactionBatches(transactionId, storageDetail, false, opening, outTransaction, null);
    }
    
    /**
     * return the map of total amount and quantity batches per transaction.
     *
     * @param transactionId (optional)
     * @param storageDetail (optional)
     * @param opening (optional) null - for all transaction, true - for only
     * opening transactions, false for periodic transactions
     * @param outTransaction (optional) null - for all transactions, true - for
     * only out transactions, false - for only IN transactions
     * @param isAdvanceSearchForValuation (optional) null - for all
     * transactions, true - for only advance search transactions
     * @return Map - transactionId as Key and Batch as value which contains
     * total quantity and total amount of that transactionId
     */
    public Map<String, Batch> getAllTransactionBatches(String transactionId, Map<String, String> storageDetail, boolean forPartialStorage, Boolean opening, Boolean outTransaction, Boolean isAdvanceSearchForValuation) {
        List<TransactionBatch> tbList = getTransactionBatchList(transactionId, storageDetail, forPartialStorage);
        return calculateTransactionPrice(tbList, transactionId, opening, outTransaction, isAdvanceSearchForValuation);
    }
    
    /**
     * return the map of total amount and quantity batches for all transaction.
     *
     * @param transactionId (optional)
     * @param storageDetail (optional)
     * @param opening (optional) null - for all transaction, true - for only
     * opening transactions, false for periodic transactions
     * @param outTransaction (optional) null - for all transactions, true - for
     * only out transactions, false - for only IN transactions
     * @param initialBatch (optional) null - for all transactions, true - for
     * only Initial Transaction
     * @return Batch - which contains total quantity and total amount of all
     * transactions
     */
    public Batch getAllTransactionBatch(String transactionId, Map<String, String> storageDetail, Boolean opening, Boolean outTransaction, Boolean initialBatch) {
        return getAllTransactionBatch(transactionId, storageDetail, false, opening, outTransaction, initialBatch, null);
    }
    public Batch getAllTransactionBatch(String transactionId, Map<String, String> storageDetail, boolean forPartialStorage, Boolean opening, Boolean outTransaction, Boolean initialBatch, Boolean isAdvanceSearchForValuation) {
        Batch batch = new Batch(0, 0);
        Map<String, Batch> openingBatches = getAllTransactionBatches(transactionId, storageDetail, forPartialStorage, opening, outTransaction, isAdvanceSearchForValuation);
        for (Batch openingBatch : openingBatches.values()) {
            if (initialBatch != null && initialBatch) {
                if (openingBatch.getDocType() == TransactionBatch.DocType_INITIAL) {
                    if (!StringUtil.isNullOrEmpty(openingBatch.getCompanyid())) {
                        batch.setAmount(batch.getAmount() + Double.parseDouble(authHandler.getFormattedUnitPrice(openingBatch.getAmount(), openingBatch.getCompanyid())));
                    } else {
                        batch.setAmount(batch.getAmount() + Double.parseDouble(authHandler.getFormattedUnitPrice(openingBatch.getAmount())));
                    }
                    batch.setQuantity(batch.getQuantity() + openingBatch.getQuantity());
                }
            } else {
                if (!StringUtil.isNullOrEmpty(openingBatch.getCompanyid())) {
                    batch.setAmount(batch.getAmount() + Double.parseDouble(authHandler.getFormattedUnitPrice(openingBatch.getAmount(), openingBatch.getCompanyid())));
                } else {
                    batch.setAmount(batch.getAmount() + Double.parseDouble(authHandler.getFormattedUnitPrice(openingBatch.getAmount())));
                }
                batch.setQuantity(Double.parseDouble(authHandler.formattedQuantity(batch.getQuantity() + openingBatch.getQuantity(),openingBatch.getCompanyid())));
                batch.setWithoutlanded(openingBatch.getWithoutlanded());
            }
            /**
             * Transaction Location Map.
             */
            if (batch.getLocationMap() == null && openingBatch.getLocationMap() != null) {
                batch.setLocationMap(openingBatch.getLocationMap());
            } else if (openingBatch.getLocationMap() != null) {
                Map<String, Double> locationMap = batch.getLocationMap();
                Map<String, Double> openBatchLocationMap = openingBatch.getLocationMap();
                for (Map.Entry<String, Double> entrySet : openBatchLocationMap.entrySet()) {
                    String key = entrySet.getKey();
                    Double qty = entrySet.getValue();
                    if (locationMap.containsKey(key)) {
                        double previousQty = locationMap.get(key);
                        locationMap.put(key, previousQty + qty);
                    } else {
                        locationMap.put(key, qty);
                    }
                }
                batch.setLocationMap(locationMap);
            }
            /**
             * Transaction Warehouse Map.
             */
            if (batch.getWarehouseMap() == null && openingBatch.getWarehouseMap() != null) {
                batch.setWarehouseMap(openingBatch.getWarehouseMap());
            } else if (openingBatch.getWarehouseMap() != null) {
                Map<String, Double> warehouseMap = batch.getWarehouseMap();
                Map<String, Double> openBatchWarehouseMap = openingBatch.getWarehouseMap();
                for (Map.Entry<String, Double> entrySet : openBatchWarehouseMap.entrySet()) {
                    String key = entrySet.getKey();
                    Double qty = entrySet.getValue();
                    if (warehouseMap.containsKey(key)) {
                        double previousQty = warehouseMap.get(key);
                        warehouseMap.put(key, previousQty + qty);
                    } else {
                        warehouseMap.put(key, qty);
                    }
                }
                batch.setWarehouseMap(warehouseMap);
            }
            /**
             * Transaction Serial Map.
             */
            if (batch.getSerialMap() == null && openingBatch.getSerialMap() != null) {
                batch.setSerialMap(openingBatch.getSerialMap());
            } else if (openingBatch.getSerialMap() != null) {
                Map<String, Double> serialMap = batch.getSerialMap();
                Map<String, Double> openBatchSerialMap = openingBatch.getSerialMap();
                for (Map.Entry<String, Double> entrySet : openBatchSerialMap.entrySet()) {
                    String key = entrySet.getKey();
                    Double qty = entrySet.getValue();
                    if (serialMap.containsKey(key)) {
                        double previousQty = serialMap.get(key);
                        serialMap.put(key, previousQty + qty);
                    } else {
                        serialMap.put(key, qty);
                    }
                }
                batch.setSerialMap(serialMap);
            }
            /**
             * Transaction Batch Map.
             */
            if (batch.getBatchMap() == null && openingBatch.getBatchMap() != null) {
                batch.setBatchMap(openingBatch.getBatchMap());
            } else if (openingBatch.getBatchMap() != null) {
                Map<String, Double> batchMap = batch.getBatchMap();
                Map<String, Double> openBatchSerialMap = openingBatch.getBatchMap();
                for (Map.Entry<String, Double> entrySet : openBatchSerialMap.entrySet()) {
                    String key = entrySet.getKey();
                    Double qty = entrySet.getValue();
                    if (batchMap.containsKey(key)) {
                        double previousQty = batchMap.get(key);
                        batchMap.put(key, previousQty + qty);
                    } else {
                        batchMap.put(key, qty);
                    }
                }
                batch.setBatchMap(batchMap);
            }
            /**
             * Transaction Location Map for Material In/Out Report.
             */
            if (batch.getMaterialInOutlocationMap() == null && openingBatch.getMaterialInOutlocationMap()!=null) {
                batch.setMaterialInOutlocationMap(openingBatch.getMaterialInOutlocationMap());
            } else if (openingBatch.getMaterialInOutlocationMap() != null) {
                Map<String, Double> batchMap = batch.getMaterialInOutlocationMap();
                Map<String, Double> openingMaterialInOutlocationMap = openingBatch.getMaterialInOutlocationMap();
                for (Map.Entry<String, Double> entrySet : openingMaterialInOutlocationMap.entrySet()) {
                    String key = entrySet.getKey();
                    Double qty = entrySet.getValue();
                    if (batchMap.containsKey(key)) {
                        double previousQty = batchMap.get(key);
                        batchMap.put(key, previousQty + qty);
                    } else {
                        batchMap.put(key, qty);
                    }
                }
            }
        }
        return batch;
    }

    /**
     * This method returns List of added transactions for a particular
     * transaction for external usage.
     *
     * @param transactionId
     * @return List
     */
    public List<TransactionBatch> getTransactionPvbList(String transactionId) {
        return getTransactionBatchList(transactionId, null);
    }
    
    /**
     * This method returns the opening Batch object which contains calculated
     * price, totalQuantity, totalAmount, Inventory Details
     * (Location,Warehouse,Batch,Serial) for opening transaction with particular
     * storage details
     * </br></br>
     * </br>
     * Note: It should be used when you want opening quantity and price with
     * partial storage details.
     *
     * @param storageDetail
     * @return Batch
     */
    public Batch getPartialOpeningTransactionBatch(Map<String, String> storageDetail) {
        return getAllTransactionBatch(null, storageDetail, true, true, null, null, null);
    }
    
    public Batch getPartialOpeningTransactionBatch(Map<String, String> storageDetail, Boolean isAdvanceSearchForValuation) {
        return getAllTransactionBatch(null, storageDetail, true, true, null, null, isAdvanceSearchForValuation);
    }
    
    /**
     * Method is used to get the total amount and quantity for all transaction
     * batches.
     *
     * @param transactionId (optional)
     * @param storageDetail (optional)
     * @param opening (optional) null - for all transaction, true - for only
     * opening transactions, false for periodic transactions
     * @param outTransaction (optional) null - for all transactions, true - for
     * only out transactions, false - for only IN transactions
     * @param initialBatch (optional) null - for all transactions, true - for
     * only Initial Transaction
     * @param forPartialStorage (optional)
     * @return Batch - which contains total quantity and total amount of all
     * transactions
     */
    public Batch getAllTransactionBatch(String transactionId, Map<String, String> storageDetail, Boolean opening, Boolean outTransaction, Boolean initialBatch, boolean forPartialStorage) {
        return getAllTransactionBatch(transactionId, storageDetail, forPartialStorage, opening, outTransaction, initialBatch, null);
    }
    
    public Batch getAllTransactionBatch(String transactionId, Map<String, String> storageDetail, Boolean opening, Boolean outTransaction, Boolean initialBatch, boolean forPartialStorage, Boolean isAdvanceSearchForValuation) {
        return getAllTransactionBatch(transactionId, storageDetail, forPartialStorage, opening, outTransaction, initialBatch, isAdvanceSearchForValuation);
    }
    /**
     * This method returns List of added transactions for a particular
     * transaction and particular storagedetail for external usage.
     *
     * @param transactionId
     * @param storageDetail
     * @return
     */
    public List<TransactionBatch> getTransactionBatchList(String transactionId, Map<String, String> storageDetail) {
        return getTransactionBatchList(transactionId, storageDetail, false);
    }
    
    public List<TransactionBatch> getTransactionBatchList(String transactionId, Map<String, String> storageDetail, boolean forPartialStorage) {
        Set<String> storageKeys;
        if (!StringUtil.isNullOrEmpty(transactionId)) {
            storageKeys = transIdStorageMap.get(transactionId);
            if (storageKeys == null) {
                storageKeys = new HashSet();
            }
        } else {
            storageKeys = new HashSet();
            for (Set<String> sks : transIdStorageMap.values()) {
                storageKeys.addAll(sks);
            }
        }
        List<TransactionBatch> pvbList = new ArrayList();
        if (storageDetail != null && !forPartialStorage) {
            String key = getStoregeKey(storageDetail);
            if (storageKeys.contains(key)) {
                pvbList = storageBatchMap.get(key);
            }
        } else {
            for (String storageKey : storageKeys) {
                if(forPartialStorage){
                    for(TransactionBatch tb : storageBatchMap.get(storageKey)){
                        if(tb.isPartialStorageContains(storageDetail)){
                            pvbList.add(tb);
                        }
                    }
                }else{
                    pvbList.addAll(storageBatchMap.get(storageKey));
                }
                
            }
        }
        return pvbList;
    }

    
    public Map<String, Batch> getStorageDetailwiseQuantityBatch(List<TransactionBatch> transactionBatchList) {
        StorageFilter[] combination = {StorageFilter.WAREHOUSE, StorageFilter.LOCATION, StorageFilter.ROW, StorageFilter.RACK, StorageFilter.BIN};
        return getStorageDetailwiseQuantityBatch(transactionBatchList, combination, null,null);
    }
    public Map<String, Batch> getStorageDetailwiseQuantityBatch(List<TransactionBatch> transactionBatchList, StorageFilter[] storageCombinations, Boolean opening,Boolean period) {

       Map<String, Batch> storageBatch = new HashMap();
        for(TransactionBatch tb : transactionBatchList){
            if (opening != null && ((opening.booleanValue() && !tb.isOpening()) || (!opening.booleanValue() && tb.isOpening()))) {
                continue;
            }
            if (period != null && ((period.booleanValue() && !tb.isPeriodTransaction()) || (!period.booleanValue() && tb.isPeriodTransaction()))) {
                continue;
            }
            String key = tb.getPartialStorageKey(storageCombinations);
            if(storageBatch.containsKey(key)){
                Batch batch = storageBatch.get(key);
                if (tb.isOutEntry()) {
                    batch.getAvailableSerial().remove(tb.getSerialId());
                } else {
                    batch.getAvailableSerial().add(tb.getSerialId());
                }
                batch.setQuantity(batch.getQuantity()+ (tb.isOutEntry() ? -(tb.getQuantity()): tb.getQuantity()));
                if (!StringUtil.isNullOrEmpty(tb.getCompanyid())) {
                    batch.setAmount(Double.parseDouble(authHandler.getFormattedUnitPrice(batch.getAmount(), tb.getCompanyid())) + (tb.isOutEntry() ? -(Double.parseDouble(authHandler.getFormattedUnitPrice(tb.getAmount()))) : Double.parseDouble(authHandler.getFormattedUnitPrice(tb.getAmount(), tb.getCompanyid()))));
                } else {
                    batch.setAmount(Double.parseDouble(authHandler.getFormattedUnitPrice(batch.getAmount())) + (tb.isOutEntry() ? -(Double.parseDouble(authHandler.getFormattedUnitPrice(tb.getAmount()))) : Double.parseDouble(authHandler.getFormattedUnitPrice(tb.getAmount()))));
                }
                
                /**
                 * SDP-14143 
                 * average cost column was not properly calculated in Stock Valuation report average cost should be excluded of landed cost of product
                 * Example - 
                 * (rate)10 + 5(landedcost) = 15 * 10(quantity) = 150 amount inclusive of landed cost average cost is 15 
                 * without landed amount will be rate(10)*10(quantity) = 100 amount without landed cost average cost is 10.
                 */
                double withoutlandedamount = tb.getWithoutlanded() * tb.getQuantity();   //total amount without landed cost (rate * quantity)
                if (!StringUtil.isNullOrEmpty(tb.getCompanyid())) {
                    batch.setWithoutlandedamount(Double.parseDouble(authHandler.getFormattedUnitPrice(batch.getWithoutlandedamount(), tb.getCompanyid())) + (tb.isOutEntry() ? -(withoutlandedamount) : withoutlandedamount));
                } else {
                    batch.setWithoutlandedamount(Double.parseDouble(authHandler.getFormattedUnitPrice(batch.getWithoutlandedamount())) + (tb.isOutEntry() ? -(withoutlandedamount) : withoutlandedamount));
                }
                /**
                 * ERM-447 For Stock valuation report show landed cost transactions with same stores will have fields added in this list.
                 */
                if (tb.getExtraJSON() != null) {
                    batch.setExtraJSON(tb.getExtraJSON());
                    batch.setWithoutlanded(tb.getWithoutlanded());
                    tb.setExtraJSON(null); //releasing memory
                }
            } else {
                Batch batch = null;
                if (!StringUtil.isNullOrEmpty(tb.getCompanyid())) {
                    batch = new Batch((tb.isOutEntry() ? -(tb.getQuantity()) : tb.getQuantity()), (tb.isOutEntry() ? -(Double.parseDouble(authHandler.getFormattedUnitPrice(tb.getAmount(), tb.getCompanyid()))) : Double.parseDouble(authHandler.getFormattedUnitPrice(tb.getAmount(), tb.getCompanyid()))));
                } else {
                    batch = new Batch((tb.isOutEntry() ? -(tb.getQuantity()): tb.getQuantity()), (tb.isOutEntry() ? -(Double.parseDouble(authHandler.getFormattedUnitPrice(tb.getAmount()))): Double.parseDouble(authHandler.getFormattedUnitPrice(tb.getAmount()))));
                }                
                if (tb.isOutEntry()) {
                    batch.getAvailableSerial().remove(tb.getSerialId());
                } else {
                    batch.getAvailableSerial().add(tb.getSerialId());
                }
                if (!StringUtil.isNullOrEmpty(tb.getCompanyid())) {
                    batch.setCompanyid(tb.getCompanyid());
                }
                fillPartialStorageDetailInBatch(batch, tb, storageCombinations);
                storageBatch.put(key, batch);
                
                /**SDP-14143
                 * average cost column was not properly calculated in Stock Valuation report.
                 */
                double withoutlandedamount = tb.getWithoutlanded() * tb.getQuantity();
                batch.setWithoutlandedamount(withoutlandedamount);

                /**ERM-447 For Stock valuation report show landed cost
                 * transactions with same stores will have fields added in this json.
                 */
                if (tb.getExtraJSON()!= null) {
                    batch.setExtraJSON(tb.getExtraJSON());
                    batch.setWithoutlanded(tb.getWithoutlanded());
                    tb.setExtraJSON(null); //releasing memory
                }
            }
        }
        return storageBatch;
    }

    /**
     * This method return the calculated transaction price using existing
     * transactions (IN+OUT) .
     *
     * @param pvbList
     * @param transactionId
     * @param opening
     * @return
     */
    private Map<String, Batch> calculateTransactionPrice(List<TransactionBatch> pvbList, String transactionId, Boolean opening, Boolean outTransaction, Boolean isAdvanceSearchForValuation) {
        Map<String, Batch> transQtyAmountMap = new HashMap();
        if (pvbList == null || pvbList.isEmpty()) {
            return transQtyAmountMap;
        }
        String transId = transactionId;
        ListIterator<TransactionBatch> itr = pvbList.listIterator();
        while (itr.hasNext()) {
            TransactionBatch pvb = itr.next();
            if (StringUtil.isNullOrEmpty(transId)) {
                transactionId = pvb.getTransactionId();
            }
            if (opening != null && ((opening.booleanValue() && !pvb.isOpening()) || (!opening.booleanValue() && pvb.isOpening()))) {
                continue;
            }
            if (outTransaction != null && ((outTransaction.booleanValue() && !pvb.isOutEntry()) || (!outTransaction.booleanValue() && pvb.isOutEntry()))) {
                continue;
            }
            if (!StringUtil.isNullOrEmpty(transactionId) && !transactionId.equals(pvb.getTransactionId())) {
                continue;
            }
            if (!(pvb.isOpening() ||pvb.isPeriodTransaction())) {
                continue;
            }
            if (isAdvanceSearchForValuation != null && isAdvanceSearchForValuation && !pvb.isAdvanceSearchTransaction()) {
                continue;
            }
            double qty = pvb.getQuantity();
            /*  SDP-16195 */
            double amount = Double.parseDouble(authHandler.getFormattedUnitPrice(pvb.getPrice(), pvb.getCompanyid())) * qty;
            if (pvb.isOutEntry()) {
                qty = -qty;
                amount = -amount;
            }
            if (transQtyAmountMap.get(transactionId) != null) {
                Batch batch = transQtyAmountMap.get(transactionId);
                batch.setQuantity(batch.getQuantity() + qty);
                if (!StringUtil.isNullOrEmpty(pvb.getCompanyid())) {
                    batch.setAmount(Double.parseDouble(authHandler.getFormattedUnitPrice(batch.getAmount(), pvb.getCompanyid())) + Double.parseDouble(authHandler.getFormattedUnitPrice(amount, pvb.getCompanyid())));
                } else {
                    batch.setAmount(Double.parseDouble(authHandler.getFormattedUnitPrice(batch.getAmount())) + Double.parseDouble(authHandler.getFormattedUnitPrice(amount)));
                }
                /* Put IN transaction details into OUT transaction batch */
                if (batch.getInTransactionQtyAmountMap() != null && pvb.getInTransactionQtyAmountMap() != null) {
                    Map<String, List> inTransactionQtyMap = batch.getInTransactionQtyAmountMap();
                    inTransactionQtyMap.putAll(pvb.getInTransactionQtyAmountMap());
                    batch.setInTransactionQtyAmountMap(inTransactionQtyMap);
                } else if (pvb.getInTransactionQtyAmountMap() != null) {
                    Map<String, List> inTransactionQtyMap = new HashMap<>();
                    inTransactionQtyMap.putAll(pvb.getInTransactionQtyAmountMap());
                    batch.setInTransactionQtyAmountMap(inTransactionQtyMap);
                }
                /**
                 * Transaction Location Map.
                 */
                if (batch.getLocationMap() == null && !StringUtil.isNullOrEmpty(pvb.getLocationId())) {
                    Map<String, Double> locationMap = new HashMap<>();
                    locationMap.put(pvb.getLocationId(), qty);
                    batch.setLocationMap(locationMap);
                } else if (!StringUtil.isNullOrEmpty(pvb.getLocationId())) {
                    Map<String, Double> locationMap = batch.getLocationMap();
                    if (locationMap.containsKey(pvb.getLocationId())) {
                        double previousQty = locationMap.get(pvb.getLocationId());
                        locationMap.put(pvb.getLocationId(), previousQty + qty);
                    } else {
                        locationMap.put(pvb.getLocationId(), qty);
                    }
                    batch.setLocationMap(locationMap);
                }
                /**
                 * Transaction Warehouse Map.
                 */
                if (batch.getWarehouseMap() == null && !StringUtil.isNullOrEmpty(pvb.getWarehouseId())) {
                    Map<String, Double> warehouseMap = new HashMap<>();
                    warehouseMap.put(pvb.getWarehouseId(), qty);
                    batch.setWarehouseMap(warehouseMap);
                } else if (!StringUtil.isNullOrEmpty(pvb.getWarehouseId())) {
                    Map<String, Double> warehouseMap = batch.getWarehouseMap();
                    if (warehouseMap.containsKey(pvb.getWarehouseId())) {
                        double previousQty = warehouseMap.get(pvb.getWarehouseId());
                        warehouseMap.put(pvb.getWarehouseId(), previousQty + qty);
                    } else {
                        warehouseMap.put(pvb.getWarehouseId(), qty);
                    }
                    batch.setWarehouseMap(warehouseMap);
                }
                /**
                 * Transaction Serial Map.
                 */
                if (batch.getSerialMap() == null && !StringUtil.isNullOrEmpty(pvb.getSerialId())) {
                    Map<String, Double> serialMap = new HashMap<>();
                    StringBuilder serialIdBuilder = new StringBuilder();
                    serialIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getLocationId()) ? " " : pvb.getLocationId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(StringUtil.isNullOrEmpty(pvb.getWarehouseId()) ? " " : pvb.getWarehouseId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(StringUtil.isNullOrEmpty(pvb.getBatchId()) ? " " : pvb.getBatchId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(pvb.getSerialId());
                    serialMap.put(serialIdBuilder.toString(), qty);
                    batch.setSerialMap(serialMap);
                } else if (!StringUtil.isNullOrEmpty(pvb.getSerialId())) {
                    Map<String, Double> serialMap = batch.getSerialMap();
                    StringBuilder serialIdBuilder = new StringBuilder();
                    serialIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getLocationId()) ? " " : pvb.getLocationId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(StringUtil.isNullOrEmpty(pvb.getWarehouseId()) ? " " : pvb.getWarehouseId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(StringUtil.isNullOrEmpty(pvb.getBatchId()) ? " " : pvb.getBatchId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(pvb.getSerialId());
                    if (serialMap.containsKey(serialIdBuilder.toString())) {
                        double previousQty = serialMap.get(serialIdBuilder.toString());
                        serialMap.put(serialIdBuilder.toString(), previousQty + qty);
                    } else {
                        serialMap.put(serialIdBuilder.toString(), qty);
                    }
                    batch.setSerialMap(serialMap);
                }
                /**
                 * Transaction Batch Map.
                 */
                if (batch.getBatchMap() == null && !StringUtil.isNullOrEmpty(pvb.getBatchId())) {
                    Map<String, Double> batchMap = new HashMap<>();
                    StringBuilder batchIdBuilder = new StringBuilder();
                    batchIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getLocationId()) ? " " : pvb.getLocationId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(StringUtil.isNullOrEmpty(pvb.getWarehouseId()) ? " " : pvb.getWarehouseId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(pvb.getBatchId());
                    batchMap.put(batchIdBuilder.toString(), qty);
                    batch.setBatchMap(batchMap);
                } else if (!StringUtil.isNullOrEmpty(pvb.getBatchId())) {
                    Map<String, Double> batchMap = batch.getBatchMap();
                    StringBuilder batchIdBuilder = new StringBuilder();
                    batchIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getLocationId()) ? " " : pvb.getLocationId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(StringUtil.isNullOrEmpty(pvb.getWarehouseId()) ? " " : pvb.getWarehouseId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(pvb.getBatchId());
                    if (batchMap.containsKey(batchIdBuilder.toString())) {
                        double previousQty = batchMap.get(batchIdBuilder.toString());
                        batchMap.put(batchIdBuilder.toString(), previousQty + qty);
                    } else {
                        batchMap.put(batchIdBuilder.toString(), qty);
                    }
                    batch.setBatchMap(batchMap);
                }
                /**
                 * Transaction Location Map for Material In/Out Report. Key to
                 * Map is build as : Location | Batch | Serial | Row | Rack | Bin
                 */
                if (batch.getMaterialInOutlocationMap() == null && !StringUtil.isNullOrEmpty(pvb.getLocationId())) {
                    Map<String, Double> locationMap = new HashMap<>();
                    StringBuilder locationIdBuilder = new StringBuilder();
                    locationIdBuilder.append(pvb.getLocationId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getBatchId()) ? " " : pvb.getBatchId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getSerialId()) ? " " : pvb.getSerialId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getRowId()) ? " " : pvb.getRowId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getRackId()) ? " " : pvb.getRackId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getBinId()) ? " " : pvb.getBinId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationMap.put(locationIdBuilder.toString(), qty);
                    batch.setMaterialInOutlocationMap(locationMap);
                } else if (!StringUtil.isNullOrEmpty(pvb.getLocationId())) {
                    Map<String, Double> map = batch.getMaterialInOutlocationMap();
                    StringBuilder locationIdBuilder = new StringBuilder();
                    locationIdBuilder.append(pvb.getLocationId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getBatchId()) ? " " : pvb.getBatchId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getSerialId()) ? " " : pvb.getSerialId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getRowId()) ? " " : pvb.getRowId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getRackId()) ? " " : pvb.getRackId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getBinId()) ? " " : pvb.getBinId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    if (map.containsKey(locationIdBuilder.toString())) {
                        double previousQty = map.get(locationIdBuilder.toString());
                        map.put(locationIdBuilder.toString(), previousQty + qty);
                    } else {
                        map.put(locationIdBuilder.toString(), qty);
                    }
                }
                /**
                 * Set value to OUT transaction quantity.
                 */
                if (batch.getOutTransactionQtyAmountMap()!= null && pvb.getOutTransactionQtyAmountMap() != null) {
                    Map<String, List<Double>> outTransactionQtyMap = batch.getOutTransactionQtyAmountMap();
                    for (Map.Entry<String, List<Double>> entry : pvb.getOutTransactionQtyAmountMap().entrySet()) {
                        List<Double> value = entry.getValue();
                        /**
                         * If transaction is already present in the outTransactionQtyMap, then
                         * add quantity,price and amount for the transaction
                         * with the existing data in map.
                         */
                        if (outTransactionQtyMap.containsKey(entry.getKey())) {
                            List<Double> outQtyPriceList = outTransactionQtyMap.get(entry.getKey());
                            outQtyPriceList.set(0, (outQtyPriceList.get(0) + value.get(0)));
                            outQtyPriceList.set(1,(outQtyPriceList.get(1) + value.get(1)));
                            outQtyPriceList.set(2, (outQtyPriceList.get(2) + value.get(2)));
                        } else {
                            outTransactionQtyMap.put(entry.getKey(), value);
                        }
                    }
                    batch.setOutTransactionQtyAmountMap(outTransactionQtyMap);
                } else if (pvb.getOutTransactionQtyAmountMap() != null) {
                    Map<String, List<Double>> outTransactionQtyMap = new HashMap<>();
                    outTransactionQtyMap.putAll(pvb.getOutTransactionQtyAmountMap());
                    batch.setOutTransactionQtyAmountMap(outTransactionQtyMap);
                }
                
                batch.setWithoutlanded(pvb.getWithoutlanded());
                if (pvb.getExtraJSON() != null) {
                    batch.setExtraJSON(pvb.getExtraJSON());
                    pvb.setExtraJSON(null); //releasing memory from pvb
                    }                    
            } else {
                Batch batch = null;
                if (!StringUtil.isNullOrEmpty(pvb.getCompanyid())) {
                    batch = new Batch(qty, Double.parseDouble(authHandler.getFormattedUnitPrice(amount, pvb.getCompanyid())));
                } else {
                    batch = new Batch(qty, Double.parseDouble(authHandler.getFormattedUnitPrice(amount)));
                }
                batch.setDocType(transIdTypeMap.get(transactionId));
                batch.setPersonCode(pvb.getPersonCode());
                batch.setPersonName(pvb.getPersonName());
                batch.setTransactionDate(pvb.getTransactionDate());
                batch.setTransactionNo(pvb.getTransactionNo());
                batch.setBillid(pvb.getBillid());
                batch.setSrNo(pvb.getSrNo());
                batch.setCreatedon(pvb.getCreatedon());
                batch.setRemark(pvb.getRemark());
                batch.setMemo(pvb.getMemo());
                batch.setAssemblyProductID(pvb.getAssemblyProductID());
                batch.setCostCenterID(pvb.getCostCenterID());
                batch.setStockUOMID(pvb.getStockUOMID());
                batch.setWithoutlanded(pvb.getWithoutlanded());
                //ERM-447 landing cost values arrive in this JSON field
                if (pvb.getExtraJSON() != null) {
                    batch.setExtraJSON(pvb.getExtraJSON());
                    pvb.setExtraJSON(null); //releasing memory
                    }                    
                
                /**
                 * Transaction Location Map.
                 */
                if (!StringUtil.isNullOrEmpty(pvb.getLocationId())) {
                    Map<String, Double> locationMap = new HashMap<>();
                    locationMap.put(pvb.getLocationId(), qty);
                    batch.setLocationMap(locationMap);
                }
                /**
                 * Transaction Warehouse Map.
                 */
                if (!StringUtil.isNullOrEmpty(pvb.getWarehouseId())) {
                    Map<String, Double> warehouseMap = new HashMap<>();
                    warehouseMap.put(pvb.getWarehouseId(), qty);
                    batch.setWarehouseMap(warehouseMap);
                }
                /**
                 * Transaction Batch Map.
                 */
                if (!StringUtil.isNullOrEmpty(pvb.getBatchId())) {
                    Map<String, Double> batchMap = new HashMap<>();
                    /**
                     * Transaction Batch ID Builder
                     * '|' is used as delimeter to separate IDS.
                     */
                    StringBuilder batchIdBuilder = new StringBuilder();
                    batchIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getLocationId()) ? " " : pvb.getLocationId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(StringUtil.isNullOrEmpty(pvb.getWarehouseId()) ? " " : pvb.getWarehouseId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(pvb.getBatchId());
                    batchMap.put(batchIdBuilder.toString(), qty);
                    batch.setBatchMap(batchMap);
                }
                /**
                 * Transaction Serial Map.
                 */
                if (!StringUtil.isNullOrEmpty(pvb.getSerialId())) {
                    Map<String, Double> serialMap = new HashMap<>();
                    StringBuilder serialIdBuilder = new StringBuilder();
                    /**
                     * Transaction Serial ID Builder '|' is used as delimeter to
                     * separate IDS.
                     */
                    serialIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getLocationId()) ? " " : pvb.getLocationId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(StringUtil.isNullOrEmpty(pvb.getWarehouseId()) ? " " : pvb.getWarehouseId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(StringUtil.isNullOrEmpty(pvb.getBatchId()) ? " " : pvb.getBatchId()).append(Constants.INVENTORY_VALUATION_DELIMETER).append(pvb.getSerialId());
                    serialMap.put(serialIdBuilder.toString(), qty);
                    batch.setSerialMap(serialMap);
                }
                 /**
                 * Transaction Location Map for Material In/Out Report.
                 * Key to Map is build as : Location | Batch | Serial | Row | Rack | Bin
                 */
                if (!StringUtil.isNullOrEmpty(pvb.getLocationId())) {
                    Map<String, Double> locationMap = new HashMap<>();
                    StringBuilder locationIdBuilder = new StringBuilder();
                    locationIdBuilder.append(pvb.getLocationId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getBatchId()) ? " " : pvb.getBatchId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getSerialId()) ? " " : pvb.getSerialId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getRowId()) ? " " : pvb.getRowId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getRackId()) ? " " : pvb.getRackId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationIdBuilder.append(StringUtil.isNullOrEmpty(pvb.getBinId()) ? " " : pvb.getBinId()).append(Constants.INVENTORY_VALUATION_DELIMETER);
                    locationMap.put(locationIdBuilder.toString(), qty);
                    batch.setMaterialInOutlocationMap(locationMap);
                }
                if (!StringUtil.isNullOrEmpty(pvb.getCompanyid())) {
                    batch.setCompanyid(pvb.getCompanyid());
                }
                transQtyAmountMap.put(transactionId, batch);
                /* Put IN transaction details into OUT transaction batch */
                batch.setInTransactionQtyAmountMap(pvb.getInTransactionQtyAmountMap());
                batch.setOutTransactionQtyAmountMap(pvb.getOutTransactionQtyAmountMap());
            }
        }
        return transQtyAmountMap;
    }

    /**
     * This method will return average price by from all (IN + OUT) transactions
     *
     * <br /><br />Note: it will not consider any transaction which have some
     * overdue quantity.
     *
     * @param pvbList
     * @return average price
     */
    private double calculateAveragePrice(List<TransactionBatch> pvbList) {
        double tQty = 0;
        double tAmount = 0;
        for (TransactionBatch pvb : pvbList) {
            if (pvb.isOutEntry()) {
                if (pvb.getQuantityDue() > 0) {
                    continue;
                }
                tQty -= pvb.getQuantity();
                tAmount -= pvb.getQuantity() * pvb.getPrice();
            } else {
                tQty += pvb.getQuantity();
                tAmount += pvb.getQuantity() * pvb.getPrice();
            }
        }
        return tQty != 0 ? tAmount / tQty : 0;
    }

    /**
     * This method builds and return the key from storage detail map by
     * concatinating all details.
     *
     * @param storageParams
     * @return Storage Key as string
     */
    public static String getStoregeKey(Map storageParams) {
        StringBuilder key = new StringBuilder();
        if (storageParams != null && !storageParams.isEmpty()) {
            if (storageParams.containsKey("warehouseId") && storageParams.get("warehouseId") != null) {
                key.append(storageParams.get("warehouseId"));
            }
            if (storageParams.containsKey("locationId") && storageParams.get("locationId") != null) {
                key.append(storageParams.get("locationId"));
            }
            if (storageParams.containsKey("rowId") && storageParams.get("rowId") != null) {
                key.append(storageParams.get("rowId"));
            }
            if (storageParams.containsKey("rackId") && storageParams.get("rackId") != null) {
                key.append(storageParams.get("rackId"));
            }
            if (storageParams.containsKey("binId") && storageParams.get("binId") != null) {
                key.append(storageParams.get("binId"));
            }
            if (storageParams.containsKey("batchName") && storageParams.get("batchName") != null) {
                key.append(storageParams.get("batchName"));
            }
            if (storageParams.containsKey("serialName") && storageParams.get("serialName") != null) {
                key.append(storageParams.get("serialName"));
            }
        }
        return key.toString();
    }

    private void setCommonDetailsToTransactionBatch(TransactionBatch transactionBatch) {
        if (transactionBatch != null && storageDetailTrigger != null && !storageDetailTrigger.isEmpty()) {
            if (storageDetailTrigger.get("warehouseId") != null) {
                transactionBatch.setWarehouseId(storageDetailTrigger.get("warehouseId"));
            }
            if (storageDetailTrigger.get("locationId") != null) {
                transactionBatch.setLocationId(storageDetailTrigger.get("locationId"));
            }
            if (storageDetailTrigger.get("rowId") != null) {
                transactionBatch.setRowId(storageDetailTrigger.get("rowId"));
            }
            if (storageDetailTrigger.get("rackId") != null) {
                transactionBatch.setRackId(storageDetailTrigger.get("rackId"));
            }
            if (storageDetailTrigger.get("binId") != null) {
                transactionBatch.setBinId(storageDetailTrigger.get("binId"));
            }
            if (storageDetailTrigger.get("batchName") != null) {
                transactionBatch.setBatchId(storageDetailTrigger.get("batchName"));
            }
            if (storageDetailTrigger.get("serialName") != null) {
                transactionBatch.setSerialId(storageDetailTrigger.get("serialName"));
            }
        }
    }

    /**
     * This method will fill overdue quantity of DO with given price if any, and
     * add an IN entry by given price and updated removed quantity of its with
     * overdue quantity used in DO.
     *
     * @param docType
     * @param pvbList
     * @param valuationMethod
     * @param transId
     * @param linkedTransaction
     * @param isOpening
     * @param quantity
     * @param price
     * @param storageKey
     * @param personCode Customer/Vendor code
     * @param personName Customer/Vendor name
     * @param transactionNo Transaction number
     * @param tranDate Date of transaction
     * @param billid ID for the transaction
     * @param stockUOMID ID for UnitOf Measure
     * @param assemblyProductID Assembly Product ID to be used to get the
     * assembly product ID.
     * @param remark Remark for StockMovement
     * @param costCenterID Remark for StockMovement
     * @return amount by given price and quantity
     */
    private double addInTransaction(int docType, List<TransactionBatch> pvbList, ValuationMethod valuationMethod, String transId, boolean linkedTransaction, boolean isOpening, double quantity, double price, String storageKey, String personCode, String personName, String transactionNo, Date tranDate, String billid, Integer srNo, Long createdon, boolean isPeriodTransaction, String remark, String assemblyProductID, String costCenterID, String stockUOMID, String memo, JSONObject json) {

        Map<String, List<Double>> outTransactionMap = new HashMap<>();
        double withoutlandedprice = json.optDouble("withoutlanded", 0); //SDP-15715 for updating without landed cost rate of overdue IST transactions
        /**
         * If GRN is sent to QA then don't adjust the outstanding quantity
         * (ERP-35843).
         */
        
        /**
         * If WROK ORDER FINISHED Product is sent to QA then don't adjust the outstanding quantity.
         */        
        double remainingQty = json.optBoolean("isGoodsReceiptOrderDetailSentToQA", false) || json.optBoolean("isWorkOrderComponentDetailSentToQA", false) ? quantity : updateOverdueTransactions(pvbList, quantity, price, transId, outTransactionMap,withoutlandedprice);     
        TransactionBatch pvb = new TransactionBatch(docType, transId, storageKey, price, quantity, isOpening, pvbList.size(), personCode, personName, transactionNo, tranDate, billid, srNo, createdon, isPeriodTransaction);
        setCommonDetailsToTransactionBatch(pvb);
        pvb.setRemovedQty(quantity - remainingQty);
        pvb.setRemark(remark);
        pvb.setAssemblyProductID(assemblyProductID);
        pvb.setCostCenterID(costCenterID);
        pvb.setStockUOMID(stockUOMID);
        pvb.setMemo(memo);
        pvb.setCompanyid(json.optString(Constants.companyid,""));
        pvb.setOutTransactionQtyAmountMap(outTransactionMap);
        pvb.setAdvanceSearchTransaction(json.optBoolean(isAdvanceSearchTransaction, false));
        if (json.optBoolean("isActivateLandedInvAmt", false)) { //set JSON field only for landed cost feature
            pvb.setWithoutlanded(json.optDouble("withoutlanded", 0)); //transaction rate without landed cost
            pvb.setExtraJSON(json);
        }
        pvbList.add(pvb);
        
        updateTransactionStorageMapping(transId, storageKey);

        return price * quantity;
    }

    /**
     * This method will consider quantity and price from OUT transactions and
     * add an IN entry for sales return, price is calculated from OUT
     * transactions
     *
     * <br /><br />Note: If sales return quantity is more than available OUT
     * quantity then extra quantity transaction added by given price that must
     * be purchase price.
     *
     * @param docType
     * @param pvbList
     * @param valuationMethod
     * @param transId
     * @param linkedTransaction
     * @param opening
     * @param quantity
     * @param price
     * @param storageKey
     * @param personCode Customer/Vendor code
     * @param personName Customer/Vendor name
     * @param transactionNo Transaction number
     * @param tranDate Date of transaction
     * @param billid ID for the transaction
      * @param stockUOMID ID for UnitOf Measure
     * @param assemblyProductID Assembly Product ID to be used to get the
     * assembly product ID.
     * @param remark Remark for StockMovement
     * @param costCenterID Remark for StockMovement
     * @return amount of given quantity and calculated price
     */
    private double addOutReturnTransaction(int docType, List<TransactionBatch> pvbList, ValuationMethod valuationMethod, String transId, boolean linkedTransaction, boolean opening, double quantity, double price, String storageKey, String personCode, String personName, String transactionNo, Date tranDate, String billid, Integer srNo, Long createdon, boolean isPeriodTransaction, String remark, String assemblyProductID, String costCenterID, String stockUOMID, String memo, JSONObject json) { // sales return

        double totalAmount = 0;
        double avgPrice = 0;
        if (valuationMethod == ValuationMethod.AVERAGE) {
            avgPrice = calculateAveragePrice(pvbList);
        }
        List<TransactionBatch> pvbOutList = getOutTransListForReturn(pvbList, linkedTransaction);
        List<TransactionBatch> tempList = new ArrayList();
        for (TransactionBatch pvb : pvbOutList) {
            if (quantity > 0) {
                double retQty = 0;
                if (quantity >= pvb.getAvailableQty()) {
                    retQty = pvb.getAvailableQty();
                } else {
                    retQty = quantity;
                }
                quantity -= retQty;
                /**
                 * ERM-996 For product with average valuation in Sales Return if average price is 0 
                 * then take average price of GRN IN transactions
                 * Case - 
                 * 1- GRN IN 5 quantity 
                 * 2- DO OUT 5 quantity 
                 * 3- SR IN 5 quantity(Here avg valuation of SR becomes 0 hence use average price From all GRN in transactions)
                 * 
                 * Check if product has avg valuation and average price is 0.0 then use GRN IN average.
                 */
                double batchPrice = 0.0;
                if (valuationMethod == ValuationMethod.AVERAGE && avgPrice == 0.0) {
                    List<TransactionBatch> GRNList = getINTransactionBatchList(getTransactionPvbList(null));
                    batchPrice = avgPrice = calculateAveragePrice(GRNList);
                } else {
                    batchPrice = valuationMethod == ValuationMethod.AVERAGE ? avgPrice : pvb.getPrice();
                }
                batchPrice = authHandler.roundUnitPrice(batchPrice, pvb.getCompanyid());
                TransactionBatch pbvTemp = new TransactionBatch(docType, transId, storageKey, batchPrice, retQty, opening, tempList.size(), personCode, personName, transactionNo, tranDate, billid, srNo, createdon,isPeriodTransaction);
                setCommonDetailsToTransactionBatch(pbvTemp);
                pbvTemp.setCompanyid(pvb.getCompanyid());
                pbvTemp.setAdvanceSearchTransaction(json.optBoolean(isAdvanceSearchTransaction, false));
                tempList.add(pbvTemp);

                pvb.setRemovedQty(pvb.getRemovedQty() + retQty);
                
                if (json.optBoolean("isActivateLandedInvAmt", false)) { //set JSON field only for landed cost feature
                    pbvTemp.setWithoutlanded(pvb.getWithoutlanded()); //transaction rate without landed cost
                    pbvTemp.setExtraJSON(json);
                }

                totalAmount += batchPrice * retQty;
            } else {
                break;
            }
        }
        if (quantity > 0) {
            // extra quantity considering as normal IN transaction with given price.
            TransactionBatch pbvTemp = new TransactionBatch(docType, transId, storageKey, price, quantity, opening, tempList.size(), personCode, personName, transactionNo, tranDate, billid, srNo, createdon,isPeriodTransaction);
            setCommonDetailsToTransactionBatch(pbvTemp);
            pbvTemp.setRemark(remark);
            pbvTemp.setAssemblyProductID(assemblyProductID);
            pbvTemp.setCostCenterID(costCenterID);
            pbvTemp.setMemo(memo);
            pbvTemp.setStockUOMID(stockUOMID);
            pbvTemp.setCompanyid(json.optString(Constants.companyid,""));
            pbvTemp.setAdvanceSearchTransaction(json.optBoolean(isAdvanceSearchTransaction, false));
            tempList.add(pbvTemp);
            totalAmount += price * quantity;
            if (json.optBoolean("isActivateLandedInvAmt", false)) { //set JSON field only for landed cost feature
                pbvTemp.setWithoutlanded(json.optDouble("withoutlanded")); //transaction rate without landed cost
                pbvTemp.setExtraJSON(json);
            }
        }
        if (!tempList.isEmpty()) {
            addTempListToMain(pvbList, tempList);
        }
        
        updateTransactionStorageMapping(transId, storageKey);

        return totalAmount;
    }

    /**
     * This method will consider quantity and price from IN transactions and add
     * an OUT entry for DO, price is calculated from IN transactions
     *
     * <br /><br />for LIFO or FIFO valuation if DO quantity is more than
     * available IN quantity then extra out quantity transaction added by 0
     * price that is overdue quantity
     *
     * for AVERAGE valuation extra DO quantity consider with average price
     *
     * @param docType
     * @param pvbList
     * @param valuationMethod
     * @param transId
     * @param linkedTransaction
     * @param opening
     * @param quantity
     * @param price
     * @param storageKey
     * @param personCode Customer/Vendor code
     * @param personName Customer/Vendor name
     * @param transactionNo Transaction number
     * @param tranDate Date of transaction
     * @param billid ID for the transaction
     * @param stockUOMID ID for UnitOf Measure
     * @param assemblyProductID Assembly Product ID to be used to get the
     * assembly product ID.
     * @param remark Remark for StockMovement
     * @param costCenterID Remark for StockMovement
     * @return Amount by given quantity and calculated price
     */
    private double addOutTransaction(int docType, List<TransactionBatch> pvbList, ValuationMethod valuationMethod, String transId, boolean linkedTransaction, boolean opening, double quantity, double price, String storageKey, String personCode, String personName, String transactionNo, Date tranDate, String billid, Integer srNo, Long createdon, boolean isPeriodTransaction, String remark, String assemblyProductId, String costCenterID, String stockUOMID, String memo, JSONObject json) { // DO
        List<TransactionBatch> tempList = new ArrayList();
        double totalAmount = 0;
        double avgPrice = 0;
        if (valuationMethod == ValuationMethod.AVERAGE) {
            avgPrice = calculateAveragePrice(pvbList);
        }
        List<TransactionBatch> pvbInTrasList = getInTransListForOut(pvbList, linkedTransaction);
        for (TransactionBatch pvb : pvbInTrasList) { // breaking quantity by batch for given base uom quantity.
            if (quantity > 0) {
                if ((json.optBoolean("considerGRNPrice", false) && !json.optString("groDetailID", "").equals(pvb.getTransactionId()))) {
                    /**
                     * If IST is created for GRN QC then price of IST should be
                     * same as of GRN (ERP-35843)
                     */
                    continue;
                }
                if ((json.optBoolean("considerWODPrice", false) && !json.optString("stockMovementID", "").equals(pvb.getTransactionId()))) {
                    /**
                     * If IST is created for WROK ORDER FINISHED Product QC then price of IST should be
                     * same as of WROK ORDER FINISHED Product price. 
                     */
                    continue;
                }
                
                double outQty = 0;
                if (quantity >= pvb.getAvailableQty()) {
                    outQty = pvb.getAvailableQty();
                } else {
                    outQty = quantity;
                }
                quantity -= outQty;
                double batchPrice = valuationMethod == ValuationMethod.AVERAGE ? avgPrice : pvb.getPrice();
                batchPrice = authHandler.roundUnitPrice(batchPrice, pvb.getCompanyid());
                pvb.setRemovedQty(pvb.getRemovedQty() + outQty);

                TransactionBatch pbvTemp = new TransactionBatch(docType, transId, storageKey, batchPrice, outQty, opening, tempList.size(), personCode, personName, transactionNo, tranDate, billid, srNo, createdon, isPeriodTransaction);
                Map<String, List> inTransactionQtyMap = new HashMap<>();
                List qtyPriceList = new ArrayList();
                qtyPriceList.add(0, outQty);
                qtyPriceList.add(1, batchPrice);
                qtyPriceList.add(2, (batchPrice * outQty));
                inTransactionQtyMap.put(pvb.getTransactionId(), qtyPriceList);
                pbvTemp.setInTransactionQtyAmountMap(inTransactionQtyMap);
                pbvTemp.setOutEntry(true);
                pbvTemp.setCompanyid(pvb.getCompanyid());
                pbvTemp.setRemark(remark);
                pbvTemp.setAssemblyProductID(assemblyProductId);
                pbvTemp.setCostCenterID(costCenterID);
                pbvTemp.setMemo(memo);
                pbvTemp.setStockUOMID(stockUOMID);
                pbvTemp.setAdvanceSearchTransaction(json.optBoolean(isAdvanceSearchTransaction, false));
                /**
                 * ERM-447 landed cost DO out value should contain landed cost if product has a landed invoice passed through extraJSON field.
                 */
                if (json.optBoolean("isActivateLandedInvAmt", false)) { //set JSON field only for landed cost feature
                    if (json.optDouble("withoutlanded", 0) != 0.0) { 
                        pbvTemp.setWithoutlanded(json.optDouble("withoutlanded", 0)); //transaction rate without landed cost
                    } else {
                        pbvTemp.setWithoutlanded(pvb.getWithoutlanded()); //transaction rate without landed cost
                    }
                    pbvTemp.setExtraJSON(json);
                }
                setCommonDetailsToTransactionBatch(pbvTemp);
                tempList.add(pbvTemp);
                Map<String, List<Double>> outTransactionQtyMap = new HashMap<>();
                if (pvb.getOutTransactionQtyAmountMap() != null) {
                    outTransactionQtyMap = pvb.getOutTransactionQtyAmountMap();
                }
                List outQtyPriceList = new ArrayList();
                outQtyPriceList.add(0, outQty);
                outQtyPriceList.add(1, batchPrice);
                outQtyPriceList.add(2, (batchPrice * outQty));
                outTransactionQtyMap.put(transId, outQtyPriceList);
                pvb.setOutTransactionQtyAmountMap(outTransactionQtyMap);
                totalAmount += batchPrice * outQty;
            } else {
                break;
            }
        }
        if (quantity > 0) {
            double extraPrice = 0;
            boolean overdue = false;
            if (valuationMethod == ValuationMethod.AVERAGE && !pvbInTrasList.isEmpty()) {
                extraPrice = avgPrice;
            } else {
                overdue = true;
            }
            TransactionBatch pbvTemp = new TransactionBatch(docType, transId, storageKey, extraPrice, quantity, opening, tempList.size(), personCode, personName, transactionNo, tranDate, billid, srNo, createdon, isPeriodTransaction);
            pbvTemp.setOutEntry(true);
            setCommonDetailsToTransactionBatch(pbvTemp);
            if (overdue) {
                pbvTemp.setQuantityDue(quantity);
            } else {
                totalAmount += extraPrice * quantity;
            }
            pbvTemp.setRemark(remark);
            pbvTemp.setAssemblyProductID(assemblyProductId);
            pbvTemp.setCostCenterID(costCenterID);
            pbvTemp.setStockUOMID(stockUOMID);
            pbvTemp.setMemo(memo);
            pbvTemp.setCompanyid(json.optString(Constants.companyid,""));
            pbvTemp.setAdvanceSearchTransaction(json.optBoolean(isAdvanceSearchTransaction, false));
            if (json.optBoolean("isActivateLandedInvAmt", false)) { //set JSON field only for landed cost feature
                if (json.optDouble("withoutlanded", 0) != 0.0) {
                    pbvTemp.setWithoutlanded(json.optDouble("withoutlanded", 0)); //transaction rate without landed cost
                }
            }
            tempList.add(pbvTemp);
        }
        if (!tempList.isEmpty()) {
            addTempListToMain(pvbList, tempList);
        }

        updateTransactionStorageMapping(transId, storageKey);

        return totalAmount;
    }

    /**
     * This method will consider quantity and price from IN transactions and add
     * an OUT entry for sales return, price is calculated from IN transactions
     *
     * <br /><br />Note: if purchase return quantity is more than available IN
     * quantity then extra quantity transaction added by given price that must
     * be sales price.
     *
     * @param docType
     * @param pvbList
     * @param valuationMethod
     * @param transId
     * @param linkedTransaction
     * @param opening
     * @param quantity
     * @param price
     * @param storageKey
     * @param personCode Customer/Vendor code
     * @param personName Customer/Vendor name
     * @param transactionNo Transaction number
     * @param tranDate Date of transaction
     * @param billid ID for the transaction
      * @param stockUOMID ID for UnitOf Measure
     * @param assemblyProductID Assembly Product ID to be used to get the
     * assembly product ID.
     * @param remark Remark for StockMovement
     * @param costCenterID Remark for StockMovement
     * @return Amount by given quantity and calculated price
     */
    private double addInReturnTransaction(int docType, List<TransactionBatch> pvbList, ValuationMethod valuationMethod, String transId, boolean linkedTransaction, boolean opening, double quantity, double price, String storageKey, String personCode, String personName, String transactionNo, Date tranDate, String billid, Integer srNo, Long createdon, boolean isPeriodTransaction, String remark, String assemblyProductId, String costCenterID, String stockUOMID, String memo, JSONObject json) {// Purchase Return 

        double totalAmount = 0;
        double avgPrice = 0;
        if (valuationMethod == ValuationMethod.AVERAGE) {
            avgPrice = calculateAveragePrice(pvbList);
        }
        List<TransactionBatch> pvbInList = getInTransListForReturn(pvbList, linkedTransaction);
        List<TransactionBatch> tempList = new ArrayList();
        for (TransactionBatch pvb : pvbInList) { // breaking quantity by batch for given base uom quantity.
            if (quantity > 0) {
                double outQty = 0;
                if (quantity >= pvb.getAvailableQty()) {
                    outQty = pvb.getAvailableQty();
                } else {
                    outQty = quantity;
                }
                quantity -= outQty;

                double batchPrice = price;

                pvb.setRemovedQty(pvb.getRemovedQty() + outQty);
                TransactionBatch pbvTemp = new TransactionBatch(docType, transId, storageKey, batchPrice, outQty, opening, tempList.size(), personCode, personName, transactionNo, tranDate, billid, srNo, createdon, isPeriodTransaction);
                Map<String, List> inTransactionQtyMap = new HashMap<>();
                List qtyPriceList = new ArrayList();
                qtyPriceList.add(0, outQty);
                qtyPriceList.add(1, batchPrice);
                qtyPriceList.add(2, (batchPrice * outQty));
                inTransactionQtyMap.put(pvb.getTransactionId(), qtyPriceList);
                pbvTemp.setInTransactionQtyAmountMap(inTransactionQtyMap);
                pbvTemp.setRemark(remark);
                pbvTemp.setAssemblyProductID(assemblyProductId);
                pbvTemp.setCostCenterID(costCenterID);
                pbvTemp.setStockUOMID(stockUOMID);
                pbvTemp.setCompanyid(pvb.getCompanyid());
                pbvTemp.setMemo(memo);
                pbvTemp.setOutEntry(true);
                pbvTemp.setAdvanceSearchTransaction(json.optBoolean(isAdvanceSearchTransaction, false));
                if (json.optBoolean("isActivateLandedInvAmt", false)) { //set JSON field only for landed cost feature
                    if (json.optDouble("withoutlanded", 0) != 0.0) {
                        pbvTemp.setWithoutlanded(json.optDouble("withoutlanded", 0)); //transaction rate without landed cost
                    } else {
                        pbvTemp.setWithoutlanded(pvb.getWithoutlanded());
                    }                    
                    pbvTemp.setExtraJSON(json);
                }
                setCommonDetailsToTransactionBatch(pbvTemp);
                tempList.add(pbvTemp);
                Map<String, List<Double>> outTransactionQtyMap = new HashMap<>();
                if (pvb.getOutTransactionQtyAmountMap() != null) {
                    outTransactionQtyMap = pvb.getOutTransactionQtyAmountMap();
                }
                List outQtyPriceList = new ArrayList();
                outQtyPriceList.add(0, outQty);
                outQtyPriceList.add(1, batchPrice);
                outQtyPriceList.add(2, (batchPrice * outQty));
                outTransactionQtyMap.put(transId, outQtyPriceList);
                pvb.setOutTransactionQtyAmountMap(outTransactionQtyMap);
                totalAmount += batchPrice * outQty;
            } else {
                break;
            }
        }
        if (quantity > 0) {
            double extraPrice = 0;
            boolean overdue = false;
            if (valuationMethod == ValuationMethod.AVERAGE && !pvbInList.isEmpty()) {
                extraPrice = avgPrice;
            } else {
                extraPrice = price;
                overdue = true;
            }
            TransactionBatch pbvTemp = new TransactionBatch(docType, transId, storageKey, extraPrice, quantity, opening, tempList.size(), personCode, personName, transactionNo, tranDate, billid, srNo, createdon, isPeriodTransaction);
            pbvTemp.setOutEntry(true);
            setCommonDetailsToTransactionBatch(pbvTemp);
            if (overdue) {
                pbvTemp.setQuantityDue(quantity);
            } else {
                totalAmount += extraPrice * quantity;
            }
            pbvTemp.setShouldUpdatePrice(false);
            pbvTemp.setRemark(remark);
            pbvTemp.setAssemblyProductID(assemblyProductId);
            pbvTemp.setCostCenterID(costCenterID);
            pbvTemp.setCompanyid(json.optString(Constants.companyid,""));
            if (json.optBoolean("isActivateLandedInvAmt", false)) { //set JSON field only for landed cost feature
                pbvTemp.setWithoutlanded(json.optDouble("withoutlanded", 0)); //transaction rate without landed cost
                pbvTemp.setExtraJSON(json);
            }
            pbvTemp.setMemo(memo);
            pbvTemp.setStockUOMID(stockUOMID);
            pbvTemp.setAdvanceSearchTransaction(json.optBoolean(isAdvanceSearchTransaction, false));
            tempList.add(pbvTemp);

        }
        if (!tempList.isEmpty()) {
            addTempListToMain(pvbList, tempList);
        }

        updateTransactionStorageMapping(transId, storageKey);

        return totalAmount;
    }

    /**
     * This method maintain the mapping between transactionid and storage key
     *
     * @param transId
     * @param storageKey
     */
    private void updateTransactionStorageMapping(String transId, String storageKey) {
        if (transIdStorageMap.containsKey(transId) && transIdStorageMap.get(transId) != null) {
            transIdStorageMap.get(transId).add(storageKey);
        } else {
            Set<String> storageKeyset = new HashSet<>();
            storageKeyset.add(storageKey);
            transIdStorageMap.put(transId, storageKeyset);
        }
    }

    /**
     * This method fill the overdue quantity using price and quantity of IN
     * transaction
     *
     * <br /><br />if given quantity is less than overdue quantity than it will
     * fill partial overdue given quantity and price and create one more
     * entry(similar to overdue entry) with remaining overdue quantity
     *
     * return the unused quantity of given quantity.
     *
     * @param pvbList
     * @param quantity
     * @param price
     * @param intransId IN transaction detail ID. To be used to set the IN
     * @param withoutlandeprice price without landed cost i.e. normal price of product
     * transactions details for OUT transaction.
     * @return unused quantity of given quantity
     */
    private double updateOverdueTransactions(List<TransactionBatch> pvbList, double quantity, double price, String inTransId, Map<String, List<Double>> outTransactionMap,double withoutlandedprice) {
        for (int idx = 0; idx < pvbList.size(); idx++) {
            TransactionBatch pvb = pvbList.get(idx);
            double qtyDue = pvb.getQuantityDue();
            if (qtyDue > 0) {
                if (pvb.getInTransactionQtyAmountMap() != null) {
                    Map<String, List> inTransactionQtyMap = pvb.getInTransactionQtyAmountMap();
                    List list = new ArrayList();
                    list.add(0, qtyDue);
                    if (pvb.isShouldUpdatePrice()) {
                        /**
                         * For purchase return transaction consider transaction
                         * price.
                         */
                        list.add(1, price);
                        list.add(2, (qtyDue * price));
                    } else {
                        /**
                         * Update price for OUT transaction according to
                         * valuation method set for the product.
                         */
                        list.add(1, pvb.getPrice());
                        list.add(2, (qtyDue * pvb.getPrice()));
                    }
                    inTransactionQtyMap.put(inTransId, list);
                    pvb.setInTransactionQtyAmountMap(inTransactionQtyMap);
                } else {
                    Map<String, List> inTransactionQtyMap = new HashMap<>();
                    List list = new ArrayList();
                    list.add(0, qtyDue);
                    if (pvb.isShouldUpdatePrice()) {
                        /**
                         * For purchase return transaction consider transaction
                         * price.
                         */
                        list.add(1, price);
                        list.add(2, (qtyDue * price));
                    } else {
                        /**
                         * Update price for OUT transaction according to
                         * valuation method set for the product.
                         */
                        list.add(1, pvb.getPrice());
                        list.add(2, (qtyDue * pvb.getPrice()));
                    }
                    inTransactionQtyMap.put(inTransId, list);
                    pvb.setInTransactionQtyAmountMap(inTransactionQtyMap);
                }
                if (quantity >= qtyDue) {
                    quantity -= qtyDue;
                    pvb.setQuantityDue(0);
                } else {
                    insertPartialPvb(pvbList, pvb, quantity);
                    quantity = 0;
                }
                if (pvb.isShouldUpdatePrice()) {
                    /**
                     * Update price for OUT transaction according to valuation
                     * method set for the product.
                     */
                    pvb.setPrice(price);
                    pvb.setWithoutlanded(withoutlandedprice); //for stock valuation report avg cost column is without landed cost
                }
                List<Double> list = new ArrayList();
                if (outTransactionMap.containsKey(pvb.getTransactionId())) {
                    List<Double> outQtyPriceList = outTransactionMap.get(pvb.getTransactionId());
                    outQtyPriceList.set(0, (outQtyPriceList.get(0) + qtyDue));
                    if (pvb.isShouldUpdatePrice()) {
                        /**
                         * For purchase return transaction consider transaction
                         * price.
                         */
                        outQtyPriceList.set(1, (outQtyPriceList.get(1) + price));
                        outQtyPriceList.set(2, (outQtyPriceList.get(2) + (qtyDue * price)));
                    } else {
                        /**
                         * Update price for OUT transaction according to
                         * valuation method set for the product.
                         */
                        outQtyPriceList.set(1, (outQtyPriceList.get(1) + pvb.getPrice()));
                        outQtyPriceList.set(2, (outQtyPriceList.get(2) + (qtyDue * pvb.getPrice())));
                    }
                } else {
                    list.add(0, qtyDue);
                    if (pvb.isShouldUpdatePrice()) {
                        /**
                         * For purchase return transaction consider transaction
                         * price.
                         */
                        list.add(1, price);
                        list.add(2, (qtyDue * price));
                    } else {
                        /**
                         * Update price for OUT transaction according to
                         * valuation method set for the product.
                         */
                        list.add(1, price);
                        list.add(2, (qtyDue * pvb.getPrice()));
                    }
                    outTransactionMap.put(pvb.getTransactionId(), list);
                }
            }
            if (quantity == 0) {
                break;
            }
        }
        return quantity;
    }
    
    /**
     * This method create partial overdue entry and add in list next to existing
     * overdue entry (based on valuation Method) and update existing overdue
     * entry.
     *
     * @param pvbList
     * @param pvb
     * @param usedQty
     */
    private void insertPartialPvb(List<TransactionBatch> pvbList, TransactionBatch pvb, double usedQty) {
        double newPvbQty = pvb.getQuantity() - usedQty;
        double newPvbQtyDue = pvb.getQuantityDue() - usedQty;
        double newPvbQtyRemoved = pvb.getRemovedQty() >= newPvbQty ? newPvbQty : pvb.getRemovedQty();

        pvb.setQuantity(pvb.getQuantity() - newPvbQty);
        pvb.setQuantityDue(0);
        pvb.setRemovedQty(pvb.getRemovedQty() - newPvbQtyRemoved);
        
        TransactionBatch partialPvb = new TransactionBatch(pvb.getDocType(), pvb.getTransactionId(), pvb.getKey(), 0, newPvbQty, pvb.isOpening(), pvb.getBatchNo() + 1, pvb.getPersonCode(), pvb.getPersonName(), pvb.getTransactionNo(), pvb.getTransactionDate(), pvb.getBillid(), pvb.getSrNo(), pvb.getCreatedon(), pvb.isPeriodTransaction());
        partialPvb.setOutEntry(pvb.isOutEntry());
        partialPvb.setQuantityDue(newPvbQtyDue);
        partialPvb.setRemovedQty(newPvbQtyRemoved);
        partialPvb.setWarehouseId(pvb.getWarehouseId());
        partialPvb.setLocationId(pvb.getLocationId());
        partialPvb.setRowId(pvb.getRowId());
        partialPvb.setRackId(pvb.getRackId());
        partialPvb.setBinId(pvb.getBinId());
        partialPvb.setBatchId(pvb.getBatchId());
        partialPvb.setSerialId(pvb.getSerialId());
        partialPvb.setCompanyid(pvb.getCompanyid());
        insertProductBatchInMainList(pvbList, partialPvb);

    }

    /**
     * This method inserts transaction batch in list which have batch no in
     * range of main list size
     *
     * <br /><br />It insert in list with index of batchNo and update its next
     * batchNo
     *
     * @param pvbMainList
     * @param newPvb
     */
    private void insertProductBatchInMainList(List<TransactionBatch> pvbMainList, TransactionBatch newPvb) {
        if (descOrder) {
            Collections.sort(pvbMainList, new PriceBatchSorter.sortASC());
        }
        int index = newPvb.getBatchNo();
        pvbMainList.add(index, newPvb);
        for (int idx = index + 1; idx < pvbMainList.size(); idx++) {
            TransactionBatch potherPvb = pvbMainList.get(idx);
            potherPvb.setBatchNo(potherPvb.getBatchNo() + 1);
        }
        if (descOrder) {
            Collections.sort(pvbMainList, new PriceBatchSorter.sortDESC());
        }
    }

    /**
     * This method return Transaction Type according to DocType
     *
     * @param docType
     * @return TransType
     */
    public static TransType getTransType(int docType) {
        TransType transType = null;
        switch (docType) {
            case TransactionBatch.DocType_ASSEMBLY_MAIN:
            case TransactionBatch.DocType_INITIAL:
            case TransactionBatch.DocType_GRN:
            case TransactionBatch.DocType_OPENING:
            case TransactionBatch.DocType_SA_IN:
            case TransactionBatch.DocType_SR_COLLECT:
            case TransactionBatch.DocType_IN_COLLECT:
            case TransactionBatch.DocType_IST_COLLECT:
            case TransactionBatch.DocType_ILT_COLLECT:
            case TransactionBatch.DocType_WO_IN:
                transType = TransType.IN;
                break;
            case TransactionBatch.DocType_ASSEMBLY_SUB:
            case TransactionBatch.DocType_DO:
            case TransactionBatch.DocType_SA_OUT:
            case TransactionBatch.DocType_SR_ISSUE:
            case TransactionBatch.DocType_IN_ISSUE:
            case TransactionBatch.DocType_IST_ISSUE:
            case TransactionBatch.DocType_ILT_ISSUE:
            case TransactionBatch.DocType_WO_OUT:
                transType = TransType.OUT;
                break;
            case TransactionBatch.DocType_SALES_RETURN:
                transType = TransType.OUT_RETURN;
                break;
            case TransactionBatch.DocType_PURCHASE_RETURN:
                transType = TransType.IN_RETURN;
                break;
        }
        return transType;
    }
    
    public static String getTransactionModule(int doctype) {
        String modulename = "";
        switch (doctype) {
            case TransactionBatch.DocType_INITIAL:
                modulename = "New Product";
                break;
            case TransactionBatch.DocType_ASSEMBLY_SUB:
            case TransactionBatch.DocType_ASSEMBLY_MAIN:
                modulename = "Build/UnBuild Assembly";
                break;
            case TransactionBatch.DocType_GRN:
                modulename = "Goods Reciept";
                break;
            case TransactionBatch.DocType_DO:
                modulename = "Delivery Order";
                break;
            case TransactionBatch.DocType_PURCHASE_RETURN:
                modulename = "Purchase Return";
                break;
            case TransactionBatch.DocType_SALES_RETURN:
                modulename = "Sales Return";
                break;
            case TransactionBatch.DocType_SA_IN:
            case TransactionBatch.DocType_SA_OUT:
                modulename = "Stock Adjustment";
                break;
            case TransactionBatch.DocType_IN_ISSUE:
                modulename = "Issue Note Issue";
                break;
            case TransactionBatch.DocType_IN_COLLECT:
                modulename = "Issue Note Collect";
                break;
            case TransactionBatch.DocType_IST_COLLECT:
                modulename = "Inter Store Transfer Collect";
                break;
            case TransactionBatch.DocType_IST_ISSUE:
                modulename = "Inter Store Transfer Issue";
                break;
            case TransactionBatch.DocType_ILT_ISSUE:
                modulename = "Inter Location Transfer Issue";
                break;
            case TransactionBatch.DocType_ILT_COLLECT:
                modulename = "Inter Location Transfer Collect";
                break;
            case TransactionBatch.DocType_SR_ISSUE:
                modulename = "Stock Request Issue";
                break;
            case TransactionBatch.DocType_SR_COLLECT:
                modulename = "Stock Request Collect";
                break;
        }
        return modulename;
    }

    /**
     * This method append temporary list to main list with updated batchNo for
     * temp list
     *
     * @param pvbList
     * @param tempList
     */
    private void addTempListToMain(List<TransactionBatch> pvbList, List<TransactionBatch> tempList) {
        int mainListSize = pvbList.size();
        for (TransactionBatch pvb : tempList) {
            pvb.setBatchNo(mainListSize + pvb.getBatchNo());
            pvbList.add(pvb);
        }
    }
    
    /**
     * This method filter main list of OUT transactions
     *
     * <br /><br />Note: if added transaction is linked transaction then it will
     * consider only DO transaction otherwise consider all OUT transaction which
     * have some available quantity.
     *
     * @param pvbList
     * @param linkedTransaction
     * @return filtered list of transactions
     */
    private List<TransactionBatch> getOutTransListForReturn(List<TransactionBatch> pvbList, boolean linkedTransaction) {
        List<TransactionBatch> tempList = new ArrayList();
        for (TransactionBatch pvb : pvbList) {
            if (!pvb.isOutEntry() || pvb.getAvailableQty() <= 0 || (linkedTransaction && pvb.getDocType() != TransactionBatch.DocType_DO)) {
                continue;
            }
            tempList.add(pvb);
        }
        return tempList;
    }

    /**
     * This method filter main list of IN transactions
     *
     * <br /><br />Note: if added transaction is linked transaction then it will
     * consider only GRN transaction otherwise consider all IN transaction which
     * have some available quantity.
     *
     * @param pvbList
     * @param linkedTransaction
     * @return
     */
    private List<TransactionBatch> getInTransListForReturn(List<TransactionBatch> pvbList, boolean linkedTransaction) {
        List<TransactionBatch> tempList = new ArrayList();
        for (TransactionBatch pvb : pvbList) {
            if (pvb.isOutEntry() || pvb.getAvailableQty() <= 0 || (linkedTransaction && pvb.getDocType() != TransactionBatch.DocType_GRN)) { // considering batch which has IN quantity. Because for DO or out transaction remove quantity from IN quantity
                continue;
            }
            tempList.add(pvb);
        }
        return tempList;
    }
    /**
     * This method will return all the SA and GRN and Opening In transactions from the passed PVB List 
     * <br/><br/><b>Note-</b>This method is only for getting the specific IN transactions SA/GRN/Opening 
     * that have been already added into the PVB list those transactions will be returned always
     * @param pvbList
     * @param DocType
     * @return List
     */
    private List<TransactionBatch> getINTransactionBatchList(List<TransactionBatch> pvbList) {
        List<TransactionBatch> tempList = new ArrayList();
        for (TransactionBatch pvb : pvbList) {
            if (!(pvb.getDocType() == TransactionBatch.DocType_GRN || pvb.getDocType() == TransactionBatch.DocType_SA_IN || pvb.getDocType() == TransactionBatch.DocType_INITIAL)) { // match the doctType in the PVB List with the passed DocType
                continue;
            }
            tempList.add(pvb);
        }
        return tempList;
    }

    /**
     * This method filter main list of IN transactions
     *
     * <br /><br />Note: it will consider all IN transaction which have some
     * available quantity.
     *
     * @param pvbList
     * @param linkedTransaction
     * @return
     */
    private List<TransactionBatch> getInTransListForOut(List<TransactionBatch> pvbList, boolean linkedTransaction) {
        List<TransactionBatch> tempList = new ArrayList();
        for (TransactionBatch pvb : pvbList) {
            if (pvb.isOutEntry() || pvb.getAvailableQty() <= 0) { // considering batch which has IN quantity. Because for DO or out transaction remove quantity from IN quantity
                continue;
            }
            tempList.add(pvb);
        }
        return tempList;
    }

    private void fillPartialStorageDetailInBatch(Batch batch, TransactionBatch transactionBatch, StorageFilter[] storageCombinations) {
        for (PriceValuationStack.StorageFilter combination : storageCombinations) {
            switch (combination) {
                case WAREHOUSE:
                    batch.setWarehouseId(transactionBatch.getWarehouseId());
                    break;
                case LOCATION:
                    batch.setLocationId(transactionBatch.getLocationId());
                    break;
                case ROW:
                    batch.setRowId(transactionBatch.getRowId());
                    break;
                case RACK:
                    batch.setRackId(transactionBatch.getRackId());
                    break;
                case BIN:
                    batch.setBinId(transactionBatch.getBinId());
                    break;
                case BATCH:
                    batch.setBatchId(transactionBatch.getBatchId());
                    break;
                case SERIAL:
                    batch.setSerialId(transactionBatch.getSerialId());
                    break;
            }
        }
    }

    /**
     * This class is used to sort Transaction List.
     */
    private static class PriceBatchSorter {

        /**
         * This class is used to sort Transaction List in Ascending order of
         * batch No (ie 0,1,2,3,4,5....)
         *
         * <br /><br />it is used for FIFO and AVERAGE valuation method because
         * reading transaction from first (transaction inserted first has
         * batchNo 0).
         */
        public static class sortASC implements Comparator<TransactionBatch> {

            @Override
            public int compare(TransactionBatch o1, TransactionBatch o2) {
                return o1.getBatchNo() - o2.getBatchNo();
            }
        }

        /**
         * This class is used to sort Transaction List in Descending order of
         * batch No (ie ....5,4,3,2,1,0)
         *
         * <br /><br />it is used for STANDARD(LIFO) valuation method because
         * reading transaction from last (transaction inserted last has batchNo
         * is maximum ie 5).
         */
        public static class sortDESC implements Comparator<TransactionBatch> {

            @Override
            public int compare(TransactionBatch o1, TransactionBatch o2) {
                return o2.getBatchNo() - o1.getBatchNo();
            }
        }
    }

    /**
     * This is an inner class and used to return calculated price, totalAmount,
     * totalQuantity.
     */
    public static class Batch {

        private double quantity;
        private double amount;
        private int docType;
        private String transactionNo;
        private Date transactionDate;
        private String personCode;
        private String personName;
        private String billid;
        
        private String warehouseId;
        private String locationId;
        private String rowId;
        private String rackId;
        private String binId;
        private String batchId;
        private String serialId;
        private Integer srNo;
        private Long createdon;
        /* IN transaction details for OUT transaction 
         String - IN transaction detail ID,   
         List[0] - Quantity,
         List[1] - Rate, 
         List[2] - Amount i.e. Quantity * Rate */
        private Map<String, List> inTransactionQtyAmountMap; 
        
        private Map<String, Double> locationMap;
        private Map<String, Double> warehouseMap;
        private Map<String, Double> batchMap;
        private Map<String, Double> serialMap;
        private List<String> availableSerial;
        private Map<String, Double> MaterialInOutlocationMap;
        private String remark;
        private String assemblyProductID;
        private String stockUOMID;
        private String costCenterID;
        private String memo;
        private JSONObject extraJSON; //ERM-447 Landed Cost additional values  
        private double withoutlanded;
        private double withoutlandedamount; //SDP-14143 for Stock Valuation report issue on Total amount of Product Excluding landed cost 

        public double getWithoutlandedamount() {
            return withoutlandedamount;
        }

        public void setWithoutlandedamount(double withoutlandedamount) {
            this.withoutlandedamount = withoutlandedamount;
        }
        
        public JSONObject getExtraJSON() {
            return extraJSON;
        }

        public void setExtraJSON(JSONObject extraJSON) {
            this.extraJSON = extraJSON;
        }
        

        //Additional fields for landed invoice amount
        public double getWithoutlanded() {
            return withoutlanded;
        }

        public void setWithoutlanded(double withoutlanded) {
            this.withoutlanded = withoutlanded;
        }
        /* OUT transaction details for IN transaction 
         String - IN transaction detail ID,
         List[0] - Quantity,
         List[1] - Rate, 
         List[2] - Amount i.e. Quantity * Rate */
        private Map<String, List<Double>> outTransactionQtyAmountMap;
        private String companyid;
        public Batch(double quantity, double amount) {
            this.quantity = quantity;
            this.amount = amount;
            this.availableSerial=new ArrayList<>();
        }
        
        public Map<String, List<Double>> getOutTransactionQtyAmountMap() {
            return outTransactionQtyAmountMap;
        }

        public void setOutTransactionQtyAmountMap(Map<String, List<Double>> outTransactionQtyAmountMap) {
            this.outTransactionQtyAmountMap = outTransactionQtyAmountMap;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return this.quantity != 0 ? Math.abs(this.amount / this.quantity) : 0;
        }

        public String getTransactionNo() {
            return transactionNo;
        }

        public void setTransactionNo(String transactionNo) {
            this.transactionNo = transactionNo;
        }

        public Date getTransactionDate() {
            return transactionDate;
        }

        public void setTransactionDate(Date transactionDate) {
            this.transactionDate = transactionDate;
        }

        public String getPersonCode() {
            return personCode;
        }

        public void setPersonCode(String personCode) {
            this.personCode = personCode;
        }

        public String getPersonName() {
            return personName;
        }

        public void setPersonName(String personName) {
            this.personName = personName;
        }

        public String getBillid() {
            return billid;
        }

        public void setBillid(String billid) {
            this.billid = billid;
        }

        public int getDocType() {
            return docType;
        }

        public void setDocType(int docType) {
            this.docType = docType;
        }

        public String getBatchId() {
            return batchId;
        }

        public void setBatchId(String batchId) {
            this.batchId = batchId;
        }

        public String getBinId() {
            return binId;
        }

        public void setBinId(String binId) {
            this.binId = binId;
        }

        public String getLocationId() {
            return locationId;
        }

        public void setLocationId(String locationId) {
            this.locationId = locationId;
        }

        public String getRackId() {
            return rackId;
        }

        public void setRackId(String rackId) {
            this.rackId = rackId;
        }

        public String getRowId() {
            return rowId;
        }

        public void setRowId(String rowId) {
            this.rowId = rowId;
        }

        public String getWarehouseId() {
            return warehouseId;
        }

        public void setWarehouseId(String warehouseId) {
            this.warehouseId = warehouseId;
        }

        public String getSerialId() {
            return serialId;
        }

        public void setSerialId(String serialId) {
            this.serialId = serialId;
        }
        
        public Map<String, List> getInTransactionQtyAmountMap() {
            return inTransactionQtyAmountMap;
        }

        public void setInTransactionQtyAmountMap(Map<String, List> inTransactionQtyAmountMap) {
            this.inTransactionQtyAmountMap = inTransactionQtyAmountMap;
        }

        public Integer getSrNo() {
            return srNo;
        }
    
        public void setSrNo(Integer srNo) {
            this.srNo = srNo;
        }

        public Long getCreatedon() {
            return createdon;
        }

        public void setCreatedon(Long createdon) {
            this.createdon = createdon;
        }

        public Map<String, Double> getLocationMap() {
            return locationMap;
        }

        public void setLocationMap(Map<String, Double> locationMap) {
            this.locationMap = locationMap;
        }

        public Map<String, Double> getWarehouseMap() {
            return warehouseMap;
        }

        public void setWarehouseMap(Map<String, Double> warehouseMap) {
            this.warehouseMap = warehouseMap;
        }

        public Map<String, Double> getBatchMap() {
            return batchMap;
        }

        public void setBatchMap(Map<String, Double> batchMap) {
            this.batchMap = batchMap;
        }

        public Map<String, Double> getSerialMap() {
            return serialMap;
        }

        public void setSerialMap(Map<String, Double> serialMap) {
            this.serialMap = serialMap;
        }

        public List<String> getAvailableSerial() {
            return availableSerial;
        }

        public void setAvailableSerial(List<String> availableSerial) {
            this.availableSerial = availableSerial;
        }

        public Map<String, Double> getMaterialInOutlocationMap() {
            return MaterialInOutlocationMap;
        }

        public void setMaterialInOutlocationMap(Map<String, Double> MaterialInOutlocationMap) {
            this.MaterialInOutlocationMap = MaterialInOutlocationMap;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getAssemblyProductID() {
            return assemblyProductID;
        }

        public void setAssemblyProductID(String assemblyProductID) {
            this.assemblyProductID = assemblyProductID;
        }

        public String getStockUOMID() {
            return stockUOMID;
        }

        public void setStockUOMID(String stockUOMID) {
            this.stockUOMID = stockUOMID;
        }

        public String getCostCenterID() {
            return costCenterID;
        }

        public void setCostCenterID(String costCenterID) {
            this.costCenterID = costCenterID;
        }

        public String getMemo() {
            return memo;
        }

        public void setMemo(String memo) {
            this.memo = memo;
        }

        public String getCompanyid() {
            return companyid;
        }

        public void setCompanyid(String companyid) {
            this.companyid = companyid;
        }
    }
    
    public static enum StorageFilter{
        WAREHOUSE, LOCATION, ROW, RACK, BIN, BATCH, SERIAL; 
    }
}
