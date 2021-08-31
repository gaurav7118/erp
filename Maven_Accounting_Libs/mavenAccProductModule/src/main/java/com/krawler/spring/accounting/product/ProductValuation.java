/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.product;

import com.krawler.common.dao.BaseDAO;
import com.krawler.common.util.ValuationMethod;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Producttype;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigDecimal;
import java.util.*;

/**
 * This class will give the calculated price of the product of given date or
 * today.
 *
 * @author Vipin Gupta
 */
public class ProductValuation extends BaseDAO {

    private Product product;
    private Date startDate;
    private Date endDate;
    private PriceValuationStack priceValuationStack;
    private double assemblyProductPrice;

    /**
     *
     * @param product Any product, for assembly product price is calculated from
     * its subproducts price. Do not need to calculate assembly product price
     * from subproducts externally.
     * @param startDate all transactions of product before start date will
     * consider as opening.
     * @param endDate all transaction between start and end date (both included)
     * will consider as periodic. Also price of the product will calculated on
     * end date.
     */
    public ProductValuation(Product product, Date startDate, Date endDate) {
        if (product == null) {
            throw new IllegalArgumentException("product is required");
        }
        this.product = product;
        this.startDate = startDate;
        this.endDate = endDate;
        this.initStack();
    }

    /**
     * This is overloaded constructor, start and end date is passed as todays
     * date.
     *
     * @param product
     * @see ProductValuation(Product product, Date startDate, Date endDate)
     */
    public ProductValuation(Product product) {
        this(product, new Date(), new Date());
    }

    private void initStack() {
        priceValuationStack = new PriceValuationStack();
        if (Producttype.ASSEMBLY.equals(product.getProducttype().getID())) {
            processStackAsAssemblyProduct();
        }
        processStackAsNormalProduct();
    }

    /**
     * This method is use to push transaction of product in stack.
     */
    private void processStackAsNormalProduct() {

        List<Object[]> transactionList = getAllTransactionData();

        for (Object[] txData : transactionList) {
            int doctype = txData[0] != null ? ((BigDecimal) txData[0]).intValue() : -1;
            String transactionId = txData[1] != null ? (String) txData[1] : "";//"3243dsfdf-4554ddf-4334dfsdf-432dsfs";
            ValuationMethod valuationMethod = txData[2] != null ? ValuationMethod.valueOf((String) txData[2]) : null;//ValuationMethod.STANDARD;
            Date transactionDate = txData[3] != null ? (Date) txData[3] : new Date();
            boolean isLinkedTransaction = txData[4] != null ? (Boolean) txData[4] : false;
            boolean isOpeningTransaction = txData[5] != null ? (Boolean) txData[5] : false;
            double qtyInStockUom = txData[6] != null ? (Double) txData[6] : 0;
            double priceInStockUom = txData[7] != null ? (Double) txData[7] : 0;;
            String warehouseId = txData[8] != null ? (String) txData[8] : "";//"Wh1";
            String locationId = txData[9] != null ? (String) txData[9] : "";//"Loc1";
            String rowId = txData[10] != null ? (String) txData[10] : "";//"Row1";
            String rackId = txData[11] != null ? (String) txData[11] : "";//"Rack1";
            String binId = txData[12] != null ? (String) txData[12] : "";//"Bin1";
            String batchName = txData[13] != null ? (String) txData[13] : "";//"batch1";
            String serialName = txData[14] != null ? (String) txData[14] : "";//"serial1";
            String personCode = txData[15] != null ? (String) txData[15] : "";//"serial1";
            String personName = txData[16] != null ? (String) txData[16] : "";//"serial1";
            String transactionNo = txData[17] != null ? (String) txData[17] : "";//"serial1";
            String billId = txData[18] != null ? (String) txData[18] : "";//"serial1";
            if (transactionDate.before(this.startDate)) {
                isOpeningTransaction = true;
            }
            Map<String, String> storageDetail = new HashMap();
            storageDetail.put("warehouseId", warehouseId);
            storageDetail.put("locationId", locationId);
            storageDetail.put("rowId", rowId);
            storageDetail.put("rackId", rackId);
            storageDetail.put("binId", binId);
            storageDetail.put("batchName", batchName);
            storageDetail.put("serialName", serialName);
            JSONObject json = new JSONObject();
            priceValuationStack.pushTransaction(doctype, transactionId, valuationMethod, isLinkedTransaction, isOpeningTransaction, qtyInStockUom, priceInStockUom, storageDetail, personCode, personName, transactionNo, transactionDate, billId, null, null, false, "", "","","","", json);
        }
    }

    /**
     * This is recursive method for assembly product to find the leaf products
     * price and calculate assembly product price.
     */
    private void processStackAsAssemblyProduct() {
        List<Object[]> subProductList = getAssemblySubProductList();

        for (Object[] spData : subProductList) {
            Product sp = spData[0] != null ? (Product) spData[0] : null;
            double reqQty = spData[1] != null ? (Double) spData[1] : null;

            ProductValuation pv = new ProductValuation(sp, startDate, endDate);

            double p = pv.getProductPrice();
            assemblyProductPrice += p * reqQty;

            pv.getPriceValuationStack().clear();
        }
    }

    /**
     * return all transactions data of the selected product till selected end
     * date
     *
     * @return List of Object[]
     */
    private List<Object[]> getAllTransactionData() {
        // Write query for all transactions till end date and fetch below data.
        List<Object[]> transactionList = new ArrayList();
        return transactionList;
    }

    /**
     * returns the 1st level sub products of Assembly product.
     *
     * @return List of Object[] which contains data as given index, 0 - sub
     * product object, 1-required quantity
     */
    private List<Object[]> getAssemblySubProductList() {
        // Write query to find all subproducts of assembly product and assign to below list
        List<Object[]> subProductList = new ArrayList();
        return subProductList;
    }

    /**
     * returns the stack of selected product
     *
     * <br /><br />Note: for Assembly product it will give assembly product stack not the
     * stack of sub-products of assembly product
     *
     * @return PriceValuationStack
     */
    public PriceValuationStack getPriceValuationStack() {
        return priceValuationStack;
    }

    /**
     * returns the calculated price of the product on end date
     *
     * <br /><br />for normal product it is calculated from its stack for Assembly product
     * price is calculated from its leaf node sub-products stack Assembly
     * product price may differ with its stack price and its sub-products stack
     * price
     *
     * <br /><br />This is mostly use for calculate assembly product price when build an
     * assembly.
     *
     * @return double
     */
    public double getProductPrice() {
        if (Producttype.ASSEMBLY.equals(product.getProducttype().getID())) {
            return this.assemblyProductPrice;
        } else {
            return this.getPriceValuationStack().getTransactionPrice(null);
        }
    }
}
