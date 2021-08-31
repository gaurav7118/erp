/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.valuation;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.LandingCostCategory;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.LandingCostAllocationType;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.ValuationMethod;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stock.StockDAO;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockout.StockAdjustmentDAO;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.product.PriceValuationStack;
import com.krawler.spring.accounting.product.TransactionBatch;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.inventory.model.stockmovement.StockMovementDAO;
import com.krawler.inventory.model.stockmovement.StockMovementService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.journalentry.JournalEntryConstants;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.WorkOrder.WorkOrderComponentDetails;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author Swapnil K.
 */
public class InventoryValuationProcess implements Runnable {

    private ArrayList list = new ArrayList();
    private HibernateTransactionManager txnManager;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accCurrencyDAO currencyDAO;
    private accInvoiceDAO invoiceDAO;
    private accProductDAO productDAO;
    private StockAdjustmentDAO stockAdjustmentDAO;
    private StockDAO stockDAO;
    private StockService stockService;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accJournalEntryDAO journalEntryDAO;
    private StockMovementService stockMovementService;
    private StockMovementDAO stockMovementDAO;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private boolean isworking = false;

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void setStockDAO(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptDAOobj) {
        this.accGoodsReceiptobj = accGoodsReceiptDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }

    public void setaccInvoiceDAO(accInvoiceDAO invoiceDAO) {
        this.invoiceDAO = invoiceDAO;
    }

    public void setaccProductDAO(accProductDAO accProductDAO) {
        this.productDAO = accProductDAO;
    }

    public void setStockAdjustmentDAO(StockAdjustmentDAO stockAdjustmentDAO) {
        this.stockAdjustmentDAO = stockAdjustmentDAO;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO journalEntryDAO) {
        this.journalEntryDAO = journalEntryDAO;
    }
    
    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setStockMovementDAO(StockMovementDAO stockMovementDAO) {
        this.stockMovementDAO = stockMovementDAO;
    }
    /**
     * @param requestMap
     */
    public void add(Map<String, Object> requestMap) {
        list.add(requestMap);

    }

    @Override
    public void run() {
        try {
            while (!list.isEmpty() && !isworking) {
                Map<String, Object> reqMap = (Map<String, Object>) list.get(0);
                isworking = true;
                try {
                    if (reqMap != null && reqMap.containsKey(Constants.companyKey)) {
                        String companyid = (String) reqMap.get(Constants.companyKey);
                        Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.INFO, "Thread execution started for CompanyID - {0}", companyid);
                        getProductsForValuation(reqMap);
                        Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.INFO, "Thread execution completed for CompanyID - {0}", companyid);
                    }
                } catch (Exception ex) {
                    Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    isworking = false;
                    list.remove(reqMap);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param <reqMap> Used to get the request parameters.
     * @Description : Function to be used to get the products for valuation
     * using companyId.
     */
    private void getProductsForValuation(Map<String, Object> reqMap) {
        try {
            String companyid = (String) reqMap.get(Constants.companyKey);
            String basecurrencyid = "";
            boolean updateTransactionAmount = false;
            if (reqMap.containsKey("updateTransactionAmount") && reqMap.get("updateTransactionAmount") != null) {
                updateTransactionAmount = ((Boolean) reqMap.get("updateTransactionAmount"));
            }
            if (reqMap.containsKey(Constants.currencyKey) && reqMap.get(Constants.currencyKey) != null) {
                basecurrencyid = ((String) reqMap.get(Constants.currencyKey));
            }
            boolean updateStockAdjustmentPrice = false;
            if (reqMap.containsKey("updateStockAdjustmentPrice") && reqMap.get("updateStockAdjustmentPrice") != null) {
                updateStockAdjustmentPrice = ((Boolean) reqMap.get("updateStockAdjustmentPrice"));
            }
            List productList = new ArrayList();
            if (reqMap.containsKey("productIds") && reqMap.get("productIds") != null) {
                String productid = ((String) reqMap.get("productIds"));
                String[] productIdArr = productid.split(",");
                productList = Arrays.asList(productIdArr);
            } else {
                KwlReturnObject productResult = productDAO.getProductIdsForCompany(reqMap);
                productList = productResult.getEntityList();
            }
            if (productList != null && !productList.isEmpty()) {
                for (Object object : productList) {
                    String productid = (String) object;
                    HashMap<String, Object> requestParams = new HashMap<>();
                    requestParams.put("companyid", companyid);
                    requestParams.put("productId", productid);
                    requestParams.put("updateTransactionAmount", updateTransactionAmount);
                    requestParams.put(Constants.currencyKey, basecurrencyid);
                    requestParams.put("updateStockAdjustmentPrice", updateStockAdjustmentPrice);
                    performProductWisevaluation(requestParams);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.SEVERE, null, ex.getMessage());
        }
    }

    /**
     * @param <requestParams> Used to get the request parameters.
     * @Description : Function used to product wise valuation.
     */
       private void performProductWisevaluation(HashMap<String, Object> requestParams) {
        try {
            String productid = (String) requestParams.get("productId"); 
            String basecurrency = requestParams.containsKey(Constants.currencyKey) ? (String)requestParams.get(Constants.currencyKey) : null;
            String companyid = (String) requestParams.get(Constants.companyKey);
            Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), productid);
            boolean updateTransactionAmount=false;
            if (requestParams.containsKey("updateTransactionAmount") && requestParams.get("updateTransactionAmount") != null) {
                updateTransactionAmount =((Boolean) requestParams.get("updateTransactionAmount"));
            }
            boolean updateStockAdjustmentPrice=false;
            if (requestParams.containsKey("updateStockAdjustmentPrice") && requestParams.get("updateStockAdjustmentPrice") != null) {
                updateStockAdjustmentPrice =((Boolean) requestParams.get("updateStockAdjustmentPrice"));
            }

            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            
            boolean isActivateMRPModule = false;
            if (extraCompanyPreferences != null) {
                isActivateMRPModule = extraCompanyPreferences.isActivateMRPModule();
            }
            //company preferences term check for landed cost transactions
            boolean istermactivatedforlandedcost = extraCompanyPreferences.islandedcosttermJE();
            requestParams.put(Constants.isLandedCostTermJE, istermactivatedforlandedcost);
            requestParams.put("isActivateMRPModule", isActivateMRPModule);
            if (isActivateMRPModule) {
                requestParams.put("isValuationCall", true);
            }

            KwlReturnObject extrapre = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) extrapre.getEntityList().get(0);
            
            KwlReturnObject companykwl = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company companyobj = (Company) (companykwl.getEntityList().isEmpty()?null:companykwl.getEntityList().get(0));

            boolean isActivateLandedInvAmt = false;
            Map<String,Object> prefmap = new HashMap<>();
            prefmap.put("id", companyid);
            Object cpresult = kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"activelandingcostofitem"},prefmap);
            isActivateLandedInvAmt = (Boolean) (cpresult!=null?cpresult:false);
            if (product != null) {

                PriceValuationStack stack = new PriceValuationStack();
                KwlReturnObject result = productDAO.getStockLedger(requestParams);
                List ledgerList = result.getEntityList();
                if (ledgerList != null && !ledgerList.isEmpty()) {
                    for (Object object : ledgerList) {
                        Object[] row = (Object[]) object;
                        if (row[1].equals(productid)) {
                            double quantity = (Double) row[9];
                            BigInteger transType = (BigInteger) row[0];
                            ValuationMethod valuationMethod = product.getValuationMethod();
                            String detailID = "";
                            if (!StringUtil.isNullOrEmpty((String) row[17])) {
                                detailID = (String) row[17];
                            }
                            boolean linkflag = false;
                            if (row.length >= 30 && !StringUtil.isNullOrEmptyWithTrim((String) row[30])) {
                                linkflag = true;
                            }
                            String currencyid = "";
                            if (!StringUtil.isNullOrEmpty((String) row[13])) {
                                currencyid = (String) row[13];
                            }
                            Date transactionDate = (Date) row[4];
                            double baseUOMRate = (Double) row[11]; // Conversion Factor
                            double grSpotRate = 0.0;
                            try {
                                if (!StringUtil.isNullOrEmpty((String) row[16])) {
                                    grSpotRate = StringUtil.getDouble((String) row[16]);
                                }
                            } catch (java.lang.ClassCastException ex) {
                                if (row[16] != null) {
                                    grSpotRate = (double) row[16];
                                }
                            }
                            double price = 0;
                            if (transType.intValue() != TransactionBatch.DocType_DO) {
                                price = row[10] != null ? (Double) row[10] : 0.0;
                                int discount = 0;
                                if (row[33] instanceof BigInteger) {
                                    BigInteger discountispercent = (BigInteger) row[33];
                                    discount = discountispercent.intValue();
                                } else {
                                    discount = (Integer) row[33];
                                }

                                String gstincluded = row[34] != null ? (String) row[34] : "";
                                if (StringUtil.isNullOrEmptyWithTrim(gstincluded)) {
                                    gstincluded = "F";
                                }
                                if (gstincluded.equals("F")) {
                                    /*
                                     * In Including GST case, discount price is
                                     * already subtracted from the unit price so
                                     * no need to subtract discount
                                     */
                                    double discountPrice;
                                    if (row[32] != null && row[32] instanceof BigInteger) {
                                        BigInteger discP = (BigInteger) row[32];
                                        discountPrice = discP.doubleValue();
                                    } else {
                                        discountPrice = row[32] != null ? (Double) row[32] : 0.0;
                                    }
                                    if (discount != 0) {
                                        discountPrice = discountPrice / 100;
                                        discountPrice = discountPrice * (price * quantity);
                                    }
                                    if (quantity != 0) {
                                        double pricePerQty = ((price * quantity) - discountPrice) / quantity;
                                        price = pricePerQty;
                                    }
                                }
                                KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, price, currencyid, transactionDate, grSpotRate);
                                price = (Double) crresult.getEntityList().get(0);
                                price = price / baseUOMRate;
                                if (!(pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                                    price = Double.parseDouble(authHandler.getFormattedUnitPrice(price, companyid));
                                }
                            }
                            String transactionNumber = (String) row[5];
                            String billid = (String) row[15];
                            JSONObject json = new JSONObject();
                            if (transType.intValue() == TransactionBatch.DocType_IST_ISSUE) {
                                /**
                                 * If InterStoreTransfer is created with GRN QA
                                 * flow or not. If IST is created for GRN QA
                                 * then the price of IST should only be adjusted
                                 * with GRN and not with other document(s) i.e.
                                 * price of IST OUT will be same as of GRN
                                 * (ERP-35843).
                                 */
                                KwlReturnObject kwl = productDAO.getRateAndExchangeRateFromGoodsReceiptOrderDetail(billid, companyid);
                                if (kwl != null && kwl.getEntityList() != null && !kwl.getEntityList().isEmpty()) {
                                    List l = kwl.getEntityList();
                                    if (l != null && !l.isEmpty()) {
                                        Object o = l.get(0);
                                        Object[] objArr = (Object[]) o;
                                        if (objArr != null && objArr.length > 0) {
                                            double groRate = (double) objArr[0];
                                            double groExternalCurrencyRate = (double) objArr[1];
                                            String groCurrency = "";
                                            if (!StringUtil.isNullOrEmptyWithTrim((String) objArr[2])) {
                                                groCurrency = (String) objArr[2];
                                            }
                                            String grodID = (String) objArr[3];
                                            if (!StringUtil.isNullOrEmpty(groCurrency) && (StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(groCurrency))) {
                                                KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, groRate, groCurrency, transactionDate, groExternalCurrencyRate);
                                                price = (Double) crresult.getEntityList().get(0);
                                            } else {
                                                price = groRate;
                                            }
                                            json.put("considerGRNPrice", true);
                                            json.put("groDetailID", grodID);
                                        }
                                    }
                                }
                                /** MRP: WORK ORDER MODULE
                                * If InterStoreTransfer is created with WorkOrder Finished goods product QA flow or
                                * not. If IST is created for WorkOrder Finished goods product QA then the price of
                                * IST should only be adjusted with Produced WorkOrder Finished goods product price and not with
                                * other document(s) i.e. price of IST OUT will be same
                                * as of Produced WorkOrder Finished goods product price
                                * Fetched WOCD initialpurchaseprice as price and stockmovementid as documentid
                                * this price will set to IST OUT
                                */
                                if (isActivateMRPModule) {
                                    KwlReturnObject kwlWOCD = productDAO.getRateFromWorkOrderComponentDetail(billid, companyid);
                                    if (kwlWOCD != null && kwlWOCD.getEntityList() != null && !kwlWOCD.getEntityList().isEmpty()) {
                                        List li = kwlWOCD.getEntityList();
                                        if (li != null && !li.isEmpty()) {
                                            Object o = li.get(0);
                                            Object[] objArray = (Object[]) o;
                                            if (objArray != null && objArray.length > 0) {
                                                double wocdRate = (double) objArray[0];
                                                String stockMovementID = (String) objArray[1];
                                                price = wocdRate;
                                                json.put("considerWODPrice", true);
                                                json.put("stockMovementID", stockMovementID);
                                            }
                                        }
                                    }
                                }
                            } else if (transType.intValue() == TransactionBatch.DocType_GRN) {
                                /**
                                 * Is GoodsReceiptOrder is sent to QA to not. If
                                 * isGoodsReceiptOrderDetailSentToQA is true
                                 * then don't adjust it with outstanding DO
                                 * (ERP-35843).
                                 */
                                boolean isGoodsReceiptOrderDetailSentToQA = productDAO.isGoodsReceiptOrderDetailSentToQA(detailID, companyid);
                                if (isGoodsReceiptOrderDetailSentToQA) {
                                    json.put("isGoodsReceiptOrderDetailSentToQA", isGoodsReceiptOrderDetailSentToQA);
                                }
                            } else if (transType.intValue() == TransactionBatch.DocType_WO_IN) {  
                                 /**
                                 * Is WorkOrder Finished goods product is sent to QA or not. If
                                 * isWorkOrderComponentDetailSentToQA is true
                                 * then adjust it with WorkOrder Finished goods product QA IST OUT transaction.
                                 */
                                KwlReturnObject stockMovementKwlObject = accountingHandlerDAOobj.getObject(StockMovement.class.getName(), detailID);
                                if (stockMovementKwlObject != null && stockMovementKwlObject.getEntityList() != null && !stockMovementKwlObject.getEntityList().isEmpty()) {
                                    StockMovement stockMovementObject = (StockMovement) stockMovementKwlObject.getEntityList().get(0);
                                    if (stockMovementObject != null) {
                                        String wocdetailid = stockMovementObject.getModuleRefDetailId();
                                        boolean isWorkOrderComponentDetailSentToQA = productDAO.isWorkOrderComponentDetailSentToQA(wocdetailid, companyid);
                                        if (isWorkOrderComponentDetailSentToQA) {
                                            json.put("isWorkOrderComponentDetailSentToQA", isWorkOrderComponentDetailSentToQA);
                                        }
                                    }
                                }
                            }
                            String personCode = "";
                            String personName = "";
                            if (!StringUtil.isNullOrEmpty((String) row[6])) {
                                personCode = (String) row[6];
                            }
                            if (!StringUtil.isNullOrEmpty((String) row[7])) {
                                personName = (String) row[7];
                            }

                            String warehouseId = null;
                            String locationId = null;
                            String rowId = null;
                            String rackId = null;
                            String binId = null;
                            String batchName = "";
                            String serialNames = "";
                            boolean openingtransaction = stockService.isOpeingOrPeriodTransaction(transactionDate, null, transType.intValue());
                            Map storageParams = new HashMap();
                            if (row[24] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[24])) { // Warehouse ID
                                storageParams.put("warehouseId", (String) row[24]);
                                warehouseId = (String) row[24];
                            }
                            if (row[23] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[23])) { // Location ID
                                storageParams.put("locationId", (String) row[23]);
                                locationId = (String) row[23];
                            }
                            if (row[25] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[25])) { // Row ID
                                storageParams.put("rowId", (String) row[25]);
                                rowId = (String) row[25];
                            }
                            if (row[26] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[26])) { // Rack ID
                                storageParams.put("rackId", (String) row[26]);
                                rackId = (String) row[26];
                            }
                            if (row[27] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[27])) { // BIN ID
                                storageParams.put("binId", (String) row[27]);
                                binId = (String) row[27];
                            }
                            if (row[28] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[28])) { // Batch Name
                                storageParams.put("batchName", (String) row[28]);
                                batchName = (String) row[28];
                            }
                            if (row[31] != null && !StringUtil.isNullOrEmptyWithTrim((String) row[31])) {// comma separated serials from inventory modules
                                serialNames = (String) row[31];
                            }
                            Integer srNo = null;
                            Long createdon = null;
                            if (row[20] != null && !StringUtil.isNullOrEmptyWithTrim(row[20].toString())) {
                                srNo = Integer.parseInt(row[20].toString());
                            }
                            if (row[21] != null && !StringUtil.isNullOrEmptyWithTrim(row[21].toString())) {
                                try {
                                    createdon = Long.parseLong(row[21].toString());
                                } catch (Exception ex) {
                                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    try {
                                        Date d = f.parse(row[21].toString());
                                        createdon = d.getTime();
                                    } catch (ParseException x) {
                                        Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.SEVERE, null, ex.getMessage());
                                    }
                                }
                            }
                            quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);
                            if (storageParams != null && !storageParams.isEmpty() && row[29] != null) {
                                if (product.isIsSerialForProduct()) {
                                    quantity = 1;
                                } else if (!StringUtil.isNullOrEmptyWithTrim(row[29].toString())) {
                                    quantity = Double.parseDouble(row[29].toString());
//                                quantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate);
                                }
                            }
                            /**
                             * ERM-447 For Landed Invoice calculation.
                             */
                            
                            String invoiceID = (String) row[14];
                            json.put("isActivateLandedInvAmt", isActivateLandedInvAmt);
                            json.put("withoutlanded", price); //initial costing before applying landed cost to this same variable
                            if (isActivateLandedInvAmt && transType.intValue() == TransactionBatch.DocType_GRN) {
                                //If invoice ID is null then GRN could be generated through the flow PO->GRN->PI flow so check if any Invoice is still linked
                                List<Object> purchaseInvoicelist = new ArrayList<>();
                                KwlReturnObject productlandedcategories = productDAO.getLandedCostCategoriesforProduct(productid, companyid);
                                List<Object[]> lccList = productlandedcategories.getEntityList();
                                /**
                                 * Getting landing cost categories for a product.
                                 */
                                double landingcost = 0;
                                double unitlandedcost = 0;
                                double groqty = 0; //goodsreceipt order quantity
                                double invqty = 0;
                                boolean isMalaysiaOrSingaporeCompany = false;
                        
                                String countrycode = companyobj!=null?companyobj.getCountry().getID():"";
                                if (countrycode.equalsIgnoreCase(String.valueOf(Constants.malaysian_country_id)) || countrycode.equalsIgnoreCase(String.valueOf(Constants.SINGAPOREID))) {
                                    isMalaysiaOrSingaporeCompany = true;
                                }
                                boolean isPItoGRNFlow = StringUtil.isNullOrEmpty(invoiceID) ? false:true;  //landed cost flow 1 where PI---->Auto GRN 
                                if (lccList != null && !lccList.isEmpty()) {
                                    if (StringUtil.isNullOrEmpty(invoiceID)) { //if GRN has linked Invoice and is from the flow GRN - > PI then this id will be null
                                        purchaseInvoicelist = accGoodsReceiptobj.getGRIDfromGROID(billid, companyid); //get PIs linked with current GRN if any
                                    } else {
                                        purchaseInvoicelist.add(invoiceID); // here invoice id is from the flow PI-> Auto GRN 
                                    }
                                    Iterator invoicers = purchaseInvoicelist.iterator(); //Iterate on Purchase Invoices linked with current GRN
                                    while (invoicers.hasNext()) {
                                        String purchaseinvoiceid = (String) invoicers.next();
                                        for (Object[] lccobj : lccList) {
                                            String landingcostcategory = (String) (lccobj[0] != null ? lccobj[0] : "");
                                            int lccallocationid = (Integer) (lccobj[1] != null ? lccobj[1] : 4);
                                            /**
                                             * Iterate all expense invoices and calculate total amount in base currency.
                                             */
                                            KwlReturnObject pikwl = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), purchaseinvoiceid);
                                            GoodsReceipt consignmentgrobj = (GoodsReceipt) (pikwl.getEntityList().isEmpty() ? null : pikwl.getEntityList().get(0));

                                            if (consignmentgrobj != null && !consignmentgrobj.isDeleted()) {
                                                KwlReturnObject kwlLCObj = accGoodsReceiptobj.getLandedInviceList(purchaseinvoiceid, landingcostcategory);
                                                List<String> expenseInvoicelist = kwlLCObj.getEntityList();
                                                
                                                for (String expenseInvid : expenseInvoicelist) {
                                                    double rate = 0d; //in transaction currency
                                                    double landingCosttax = 0d; //tax on the expense invoice excluded from inventory side
                                                    double totalamount = 0d; //total amount of all products
                                                    double termtotalamount = 0d; //total amount of all products
                                                    double unitrate = 0d;
                                                    double expenseqty = 0d;
                                                    double pw = product.getProductweight();
                                                    double totalWeight = 0d; //total weight of all products in GRN
                                                    double itemWgt = 0.0; //individual product weight
                                                    double manualproductamt = 0d; //manual type amount assigned by user directly
                                                    double manualbaseamount = 0d;//manual amount in base currency
                                                    double noexpenseitem = 0d; //total quantity of all products in the invoice
                                                    double baserate = 0;
                                                    double expensecharge = 0;
                                                    double totalvalue = 0d; //total value of a single product
                                                    double piexchangerate = 0; //exchangerate based Purchase Invoice JE
                                                    double productlandedcostinJE=0.0d; //product landed cost in posted JE for perpetual only
                                                    String grdetailid = ""; //purhcase invoice detail id
                                                    
                                                    //get the specific product quantity from invoicedetails rows to prevent mismatch issues for stock ledger/financial reports
                                                    JSONObject grreqparams = new JSONObject();
                                                    grreqparams.put("invoiceid", purchaseinvoiceid);
                                                    grreqparams.put("productid", productid);
                                                    grreqparams.put("grodetailid", detailID);
                                                    grreqparams.put("companyid", companyid);
                                                    grreqparams.put("isPITOGRNLinking", isPItoGRNFlow); //Flow of GRN/PI linking true when PI-->AutoGRN
                                                    KwlReturnObject grodkwl = accGoodsReceiptobj.getProductDetailsFromGoodsReceipt(grreqparams);
                                                    Object[] grdo = grodkwl.getEntityList().isEmpty() ? null : (Object[]) grodkwl.getEntityList().get(0);
                                                    if (grdo != null) {
                                                        groqty = Double.parseDouble(grdo[0]!=null?grdo[0].toString():"0.0");
                                                        double grdorate = Double.parseDouble(grdo[1]!=null?grdo[1].toString():"0.0");
                                                        unitrate = authHandler.roundUnitPrice(grdorate, companyid);
                                                        piexchangerate = Double.parseDouble(grdo[2]!=null?grdo[2].toString():"0.0");
                                                        grdetailid = grdo[3]!=null?grdo[3].toString():"";                                                        
                                                    }                                                    
                                                    /**
                                                     * SDP-15928(For perpetual JE case only) For adjustment of the
                                                     * rounding difference we are checking if this product has the rounding value adjusted during JE posting.
                                                     */
                                                    if (pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                                                        HashMap<String, Object> lcdmparams = new HashMap<>();
                                                        lcdmparams.put("grdetailid", grdetailid);
                                                        KwlReturnObject mappingkwl = accGoodsReceiptobj.getLandingCostDetailMapping(lcdmparams);
                                                        Object[] mappingobj = mappingkwl.getEntityList().isEmpty() ? null : (Object[]) mappingkwl.getEntityList().get(0);
                                                        String inventoryjedid = (mappingobj != null ? mappingobj[3].toString() : " "); //jedetail id

                                                        mappingkwl = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), inventoryjedid);
                                                        JournalEntryDetail lcjedetailobj = (JournalEntryDetail) (!mappingkwl.getEntityList().isEmpty() ? mappingkwl.getEntityList().get(0) : null);
                                                        productlandedcostinJE = lcjedetailobj != null ? lcjedetailobj.getAmountinbase() : 0.0d;
                                                    }
                                                    
                                                    //convert unit rate to base currency for landed cost calculation
                                                    if (!StringUtil.isNullOrEmpty(currencyid) && (!StringUtil.isNullOrEmpty(basecurrency) || !basecurrency.equals(currencyid))) {
                                                        KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, unitrate, currencyid, transactionDate, piexchangerate);
                                                        unitrate = authHandler.roundUnitPrice((Double) crresult.getEntityList().get(0), companyid);
                                                    }
                                                    totalvalue = authHandler.roundUnitPrice(groqty * unitrate, companyid);
                                                    
                                                     KwlReturnObject custresult = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), expenseInvid);
                                                     GoodsReceipt expenseInvObj = (GoodsReceipt) custresult.getEntityList().get(0);
                                                    if (expenseInvObj != null) {
                                                        price = row[10] != null ? (Double) row[10] : 0.0;
                                                        KwlReturnObject crresult = currencyDAO.getCurrencyToBaseAmount(requestParams, price, currencyid, transactionDate, grSpotRate);
                                                        price = (Double) (crresult.getEntityList().isEmpty() ? 0 : crresult.getEntityList().get(0));

                                                        //iterate on values that match with the expense invoice and the given landing cost category
                                                        if (landingcostcategory.equalsIgnoreCase(expenseInvObj.getLandingCostCategory().getId())) {
                                                            KwlReturnObject kwlnoEligiableItem = accGoodsReceiptobj.getNumberEligiableItem(expenseInvObj.getID(), landingcostcategory);
                                                            List noEligiableItemList = kwlnoEligiableItem.getEntityList();
                                                            Iterator itrItem = noEligiableItemList.iterator();

                                                            while (itrItem.hasNext()) {
                                                                Object[] valueArray = (Object[]) itrItem.next();
                                                                List<Object> valueObjLit = (valueArray != null) ? new ArrayList(Arrays.asList(valueArray)) : null;
                                                                expenseqty = (valueObjLit.size() > 0 && valueObjLit.get(0) != null) ? (double) valueObjLit.get(0) : 0.0D;
                                                                rate = (valueObjLit.size() > 1 && valueObjLit.get(1) != null) ? (double) valueObjLit.get(1) : 0.0D;
                                                                itemWgt = (valueObjLit.size() > 2 && valueObjLit.get(2) != null) ? (double) valueObjLit.get(2) : 0.0D;
                                                                String goodsrecId = (valueObjLit.size() > 3 && valueObjLit.get(3) != null) ? (String) valueObjLit.get(3) : "";
                                                                //PI Object from the eligible Pis for the expense invoice
                                                                KwlReturnObject custresulttemp = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), goodsrecId);
                                                                GoodsReceipt tempinvoiceObj = (GoodsReceipt) custresulttemp.getEntityList().get(0);

                                                                KWLCurrency currencytemp = (KWLCurrency) tempinvoiceObj.getCurrency();
                                                                String currencyIdtemp = currencytemp.getCurrencyID();

                                                                /**
                                                                 * Due to lazy initialization exception using kwl JE objects from consignmentObj to
                                                                 * get currency rate and entry date.
                                                                 */
                                                                KwlReturnObject jeidkwl = accGoodsReceiptobj.getJEFromGR(tempinvoiceObj.getID(), companyid);
                                                                String jeid = (String) (jeidkwl.getEntityList().isEmpty() ? "" : jeidkwl.getEntityList().get(0));
                                                                if (!StringUtil.isNullOrEmpty(jeid)) {
                                                                    KwlReturnObject jekwl = kwlCommonTablesDAOObj.getObject(JournalEntry.class.getName(), jeid);
                                                                    JournalEntry jeobj = (JournalEntry) jekwl.getEntityList().get(0);
                                                                    Date billDateTemp = jeobj.getEntryDate() == null ? tempinvoiceObj.getFormdate() : jeobj.getEntryDate();
                                                                    double consigncurrencyrate = jeobj.getExternalCurrencyRate();
                                                                    KwlReturnObject ruternBR = currencyDAO.getCurrencyToBaseAmount(requestParams, rate, currencyIdtemp, billDateTemp, consigncurrencyrate);
                                                                    baserate = authHandler.roundUnitPrice((Double) ruternBR.getEntityList().get(0), companyid);
                                                                }
                                                                //total valuations to go in the map for landed cost
                                                                totalamount += (expenseqty * baserate);
                                                                totalWeight += (itemWgt * invqty);
                                                                noexpenseitem += expenseqty;
                                                            }
                                                            //For manual category landed cost expenseinv set value directly as manually allocated by user   
                                                             if (LandingCostAllocationType.getByValue(lccallocationid) == LandingCostAllocationType.MANUAL) {
                                                                KwlReturnObject grdkwl = accGoodsReceiptobj.getGoodsReceiptDetailForLandingCategory(consignmentgrobj.getID(),landingcostcategory);
                                                                List<Object[]> objgrd = grdkwl.getEntityList();
                                                                for(Object[] objarr:objgrd){
                                                                String grdid =(String) (objarr[0]==null?"":objarr[0]);
                                                                String grdproduct =(String) (objarr[4]==null?"":objarr[4]); 
                                                                String grnid =(String) (objarr[5]==null?"":objarr[5]);
                                                                String grodid =(String) (objarr[6]==null?"":objarr[6]);
                                                                
                                                                        if ((grdproduct.equalsIgnoreCase(productid) && billid.equalsIgnoreCase(grnid) && grodid.equalsIgnoreCase(detailID))) {
                                                                            KwlReturnObject kwlreturn = accGoodsReceiptobj.getManualProductCostLCC(expenseInvObj.getID(), grdid);
                                                                            List itemList = kwlreturn.getEntityList();
                                                                            Iterator itemItr = itemList.iterator();
                                                                            while (itemItr.hasNext()) {
                                                                                LccManualWiseProductAmount lccManualWiseProductAmount = (LccManualWiseProductAmount) itemItr.next();
                                                                                manualbaseamount = lccManualWiseProductAmount.isCustomDutyAllocationType() ? lccManualWiseProductAmount.getTaxablevalueforigst() : lccManualWiseProductAmount.getAmount();
                                                                                KWLCurrency currencytemp = (KWLCurrency) expenseInvObj.getCurrency();
                                                                                String currencyIdtemp = currencytemp.getCurrencyID();

                                                                                /**
                                                                                 * Due to lazy initialization exception using kwl JE objects from expenseInvObj
                                                                                 * to get currency rate and date.
                                                                                 */
                                                                                KwlReturnObject jekwl = accGoodsReceiptobj.getJEFromGR(expenseInvObj.getID(), companyid);
                                                                                String expjeid = (String) (jekwl.getEntityList().isEmpty() ? "" : jekwl.getEntityList().get(0));
                                                                                if (!StringUtil.isNullOrEmpty(expjeid)) {
                                                                                    KwlReturnObject expjekwl = kwlCommonTablesDAOObj.getObject(JournalEntry.class.getName(), expjeid);
                                                                                    JournalEntry expjeobj = (JournalEntry) expjekwl.getEntityList().get(0);
                                                                                    Date billDateTemp = expjeobj.getEntryDate() == null ? expenseInvObj.getFormdate() : expjeobj.getEntryDate();
                                                                                    double currencyrate = expjeobj.getExternalCurrencyRate();
                                                                                    KwlReturnObject ruternBRExpan = currencyDAO.getCurrencyToBaseAmount(requestParams, manualbaseamount, currencyIdtemp, billDateTemp, currencyrate);
                                                                                    manualproductamt += authHandler.roundUnitPrice((Double) ruternBRExpan.getEntityList().get(0), companyid);
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                             
                                                            //to calculate row level tax in the invoice and reduce tax amount from landed cost 
                                                            Map<String, Object> taxmap = accGoodsReceiptobj.getGlobalandLineLevelTaxForGoodsReceipt(expenseInvid, companyid);
                                                            if (taxmap.containsKey("globalleveltax") || taxmap.containsKey("lineleveltax")) {
                                                                double globalleveltax = (Double) (taxmap.get("globalleveltax") != null ? taxmap.get("globalleveltax") : 0.0);
                                                                double lineleveltax = 0;
                                                                List<Object> lineleveltaxlist = (ArrayList) taxmap.get("lineleveltax");
                                                                for (Object arr : lineleveltaxlist) {
                                                                    String grdid = arr != null ? arr.toString() : "";
                                                                    KwlReturnObject expkwl = kwlCommonTablesDAOObj.getObject(ExpenseGRDetail.class.getName(), grdid);
                                                                    ExpenseGRDetail expgrd = (ExpenseGRDetail) (expkwl.getEntityList().isEmpty() ? null : expkwl.getEntityList().get(0));
                                                                    if (expgrd != null) { 
                                                                        if (!isMalaysiaOrSingaporeCompany || (isMalaysiaOrSingaporeCompany && expgrd.isIsdebit())) {
                                                                            lineleveltax += expgrd.getRowTaxAmount();
                                                                        } else if(isMalaysiaOrSingaporeCompany && !expgrd.isIsdebit()) { //IF malaysia/singapore company Expense Invoice has credit type account with landed cost tax not included
                                                                            lineleveltax -= expgrd.getRowTaxAmount();
                                                                        }
                                                                    }
                                                                }
                                                                //calculating taxes with input credit not available and reducing this amount from invoice cost
                                                                landingCosttax = globalleveltax + lineleveltax; 
                                                            }
                                                            
//                                                          // Get amount from Invoice Terms of Expense Invoice and exclude this from landed cost
                                                            if (!istermactivatedforlandedcost) {
                                                                HashMap<String, Object> termParams = new HashMap();
                                                                termParams.put("invoiceid", expenseInvObj.getID());
                                                                KwlReturnObject invoicetermkwl = accGoodsReceiptobj.getInvoiceTermMap(termParams);
                                                                List<ReceiptTermsMap> invoicetermlist = invoicetermkwl != null ? invoicetermkwl.getEntityList() : null;
                                                                for (ReceiptTermsMap termmap : invoicetermlist) {
                                                                    if (termmap != null) {
                                                                        termtotalamount += termmap.getTermamount();
                                                                    }
                                                                }
                                                                termtotalamount = (termtotalamount > 0 ? 0 : termtotalamount);
                                                            }
                                                            Map<String, Double> allcactionMthdData = new HashMap<>();
                                                            allcactionMthdData.put("totLandedCost", expenseInvObj.getInvoiceAmountInBase()- landingCosttax - termtotalamount);
                                                            allcactionMthdData.put("noEligiableItem", noexpenseitem);
                                                            allcactionMthdData.put("lineItemQty", groqty);
                                                            allcactionMthdData.put("valueOfItem", totalvalue);
                                                            allcactionMthdData.put("eligiableItemCost", totalamount);
                                                            allcactionMthdData.put("eligiableItemWgt", totalWeight);
                                                            allcactionMthdData.put("itemWght", (pw * groqty));
                                                            allcactionMthdData.put("manualProductAmount", manualproductamt);
                                                            expensecharge = LandingCostAllocationType.getTotalLanddedCost(lccallocationid, allcactionMthdData);
                                                            
                                                            /**
                                                             * SDP-15928(Perpetual Only) Check in the landed cost JE if the posted
                                                             * amount is the same as the one processed here to check if this is the product where
                                                             * rounding difference has been adjusted.
                                                             */
                                                            if (pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD && productlandedcostinJE!=0.0) {
                                                                double roundedlc = authHandler.round(expensecharge, companyid);
                                                                if (productlandedcostinJE != roundedlc) {
                                                                    double roundingdiff = Math.abs(productlandedcostinJE - roundedlc);
                                                                    expensecharge += roundingdiff;
                                                                }
                                                            }
                                                            landingcost += expensecharge;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                }
                                    //update price value with landed cost which will be passed to the stack while posting the JE 
                                    double grnqty = (Double) row[9]; //GRN total quantity
                                    unitlandedcost = (landingcost / grnqty);  //get the unit landed cost
                                    price = (price + unitlandedcost);
                                    }
                                }
                            String remark = "";
                            String assembledProductID = "";
                            String costCenterID = "";
                            String stockUOMID = "";
                            boolean isPeriodTransaction = true;
                            json.put(Constants.companyid, companyid);
                            StringBuilder serialBuilder = new StringBuilder();
                            if (product.isIsSerialForProduct() && (transType.intValue() == 0 || transType.intValue() == 1 || transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4)) {
                                int transactiontype = 28;
                                if (transType.intValue() == 0 || transType.intValue() == 1) {
                                    transactiontype = 28;
                                } else if (transType.intValue() == 2 || transType.intValue() == 3 || transType.intValue() == 5 || transType.intValue() == 4) {
                                    if (transType.intValue() == 2) {
                                        transactiontype = 31;
                                    } else if (transType.intValue() == 3 || transType.intValue() == 5) {
                                        transactiontype = 27;
                                    } else if (transType.intValue() == 4) {
                                        transactiontype = 29;
                                    }
                                }
                                storageParams.put("transactiontype", (transactiontype + ""));
                                storageParams.put("detailid", detailID);
                                KwlReturnObject srno = productDAO.getSerialIdByStorageDetails(storageParams);
                                storageParams.remove("transactiontype");
                                storageParams.remove("detailid");
                                List list2 = srno.getEntityList();
                                for (Object obj : list2) {
                                    serialBuilder.append(obj.toString() + ",");
                                }
                                String[] serialIDs = serialBuilder.toString().split(",");
                                for (String serialID : serialIDs) {
                                    storageParams.put("serialName", serialID);
                                    stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, "", json);
                                }
                            } else if (transType.intValue() == 7 || transType.intValue() == 8 || transType.intValue() == 9 || transType.intValue() == 10 || transType.intValue() == 11 || transType.intValue() == 12 || transType.intValue() == 13 || transType.intValue() == 14 || transType.intValue() == 15 || transType.intValue() == 16) {
                                if (!StringUtil.isNullOrEmpty(serialNames)) {
                                    String srl[] = serialNames.split(",");
                                    for (String s : srl) {
                                        NewProductBatch batchObj = stockDAO.getERPProductBatch(product.getID(), warehouseId, locationId, rowId, rackId, binId, batchName);
                                        NewBatchSerial serialObj = stockDAO.getERPBatchSerial(product, batchObj, s);
                                        if (serialObj != null) {
                                            serialBuilder.append(serialObj.getId()).append(",");
                                        }
                                    }
                                }
                                String[] serialIDs = serialBuilder.toString().split(",");
                                for (String serialID : serialIDs) {
                                    storageParams.put("serialName", serialID);
                                    stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, "", json);
                                }
                            } else {
                                /*
                                 * put details related to the
                                 * location/warehouse/row /rack/bin/batch/serial
                                 */
                                stack.pushTransaction(transType.intValue(), detailID, valuationMethod, linkflag, openingtransaction, quantity, price, storageParams, personCode, personName, transactionNumber, transactionDate, billid, srNo, createdon, isPeriodTransaction, remark, assembledProductID, costCenterID, stockUOMID, "", json);
                            }
                        }
                }
                }
                Map<String, PriceValuationStack.Batch> detailsMap = stack.getAllTransactionBatch();
                if (detailsMap != null) {
                    updateInventoryJE(detailsMap, companyid, updateTransactionAmount, updateStockAdjustmentPrice);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.SEVERE, null, ex.getMessage());
        }
    }

    /**
     * @param detailsMap
     * @Description : Function used to update Inventory JE for the transaction
     * when transaction is added/updated/deleted.
     */
    private void updateInventoryJE(Map<String, PriceValuationStack.Batch> detailsMap, String companyid, boolean updateTransactionAmount, boolean updateStockAdjustmentPrice) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IVP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
            for (Map.Entry<String, PriceValuationStack.Batch> detailMap : detailsMap.entrySet()) {
                String detailID = detailMap.getKey();
                PriceValuationStack.Batch batchDetail = detailMap.getValue();
                if (batchDetail != null) {
                    double price = batchDetail.getAmount();
                    int transtype = batchDetail.getDocType();
                    HashMap<String, Object> doRequestParams = new HashMap<>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    filter_names.add("ID");
                    filter_params.add(detailID); 
                    doRequestParams.put(Constants.filterNamesKey, filter_names);
                    doRequestParams.put(Constants.filterParamsKey, filter_params);
                    if (transtype == TransactionBatch.DocType_GRN && !updateTransactionAmount) { // Goods Receipt
                        KwlReturnObject grodetailResult = accGoodsReceiptobj.getGoodsReceiptOrderDetails(doRequestParams);
                         /**
                         * ERM-447 For landed Cost the GRN JE should not contain landed price.
                         */
                        JSONObject extraJSON = batchDetail.getExtraJSON()==null ? null : batchDetail.getExtraJSON();
                        double withoutlanded = extraJSON != null ? extraJSON.optDouble("withoutlanded", 0) : 0;
                        if (withoutlanded != 0) {
                            price = withoutlanded * batchDetail.getQuantity();
                        } else {
                            price = batchDetail.getAmount();
                        }
                        if (grodetailResult != null && grodetailResult.getEntityList() != null && !grodetailResult.getEntityList().isEmpty()) {
                            GoodsReceiptOrderDetails grodetail = (GoodsReceiptOrderDetails) grodetailResult.getEntityList().get(0);
                            if (grodetail != null) {
                                JournalEntryDetail inventoryJEdetail = grodetail.getInventoryJEdetail();
                                JournalEntryDetail purchaseJEdetail = grodetail.getPurchasesJEDetail();
                                if (inventoryJEdetail != null && purchaseJEdetail != null) {
                                    double amountinbase = price;
                                    amountinbase = authHandler.round(amountinbase, companyid);
                                    HashMap<String, Object> requestParams = new HashMap();
                                    requestParams.put(Constants.companyKey, companyid);
                                    requestParams.put(Constants.globalCurrencyKey, inventoryJEdetail.getCompany().getCurrency().getCurrencyID());
                                    double exchangeRate = grodetail.getGrOrder().getExternalCurrencyRate() != 0 ? grodetail.getGrOrder().getExternalCurrencyRate() : inventoryJEdetail.getJournalEntry().getExternalCurrencyRate();
                                    if (inventoryJEdetail.getCompany().getCurrency().getCurrencyID().equals(inventoryJEdetail.getJournalEntry().getCurrency().getCurrencyID())) {
                                        exchangeRate = 1; // if transaction made in base currency
                                    }
                                    if (exchangeRate == 0) {
                                        Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.WARNING, null, "Exchange rate for Journal Entry ID: " + inventoryJEdetail.getJournalEntry().getID() + " has not set.");
                                    }
                                    KwlReturnObject bAmt = currencyDAO.getBaseToCurrencyAmount(requestParams, amountinbase, inventoryJEdetail.getJournalEntry().getCurrency().getCurrencyID(), inventoryJEdetail.getJournalEntry().getEntryDate(), exchangeRate);
                                    double amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                    //amount=(grodetail.getRate()*grodetail.getDeliveredQuantity());        SDP-14020
                                    JournalEntry entry = inventoryJEdetail.getJournalEntry();
                                    if (amountinbase != inventoryJEdetail.getAmountinbase()) {
                                        JSONObject params = new JSONObject();
                                        params.put(JournalEntryConstants.JEDID, inventoryJEdetail.getID());
                                        params.put(JournalEntryConstants.JEID, entry != null ? entry.getID() : "");
                                        params.put(JournalEntryConstants.TransactionModuleID, entry != null ? entry.getTransactionModuleid() : "");
                                        params.put(JournalEntryConstants.TransactionID, entry != null ? (StringUtil.isNullOrEmpty(entry.getTransactionId()) ? "" : entry.getTransactionId()) : "");
                                        params.put("oldAmountInBase", inventoryJEdetail.getAmountinbase());
                                        params.put("newAmountInBase", amountinbase);
                                        params.put("oldamount", inventoryJEdetail.getAmount());
                                        params.put("newamount", amount);
                                        params.put("exchangerate", exchangeRate);
                                        params.put("updateDate", new Date());
                                        params.put(JournalEntryConstants.COMPANYID, companyid);
                                            journalEntryDAO.saveJournalEntryUpdateHistory(params);
                                        }
                                    if (amountinbase != purchaseJEdetail.getAmountinbase()) {
                                        JSONObject params = new JSONObject();
                                        params.put(JournalEntryConstants.JEDID, purchaseJEdetail.getID());
                                        params.put(JournalEntryConstants.JEID, entry != null ? entry.getID() : "");
                                        params.put(JournalEntryConstants.TransactionModuleID, entry != null ? entry.getTransactionModuleid() : "");
                                        params.put(JournalEntryConstants.TransactionID, entry != null ? (StringUtil.isNullOrEmpty(entry.getTransactionId()) ? "" : entry.getTransactionId()) : "");
                                        params.put("oldAmountInBase", purchaseJEdetail.getAmountinbase());
                                        params.put("newAmountInBase", amountinbase);
                                        params.put("oldamount", purchaseJEdetail.getAmount());
                                        params.put("newamount", amount);
                                        params.put("exchangerate", exchangeRate);
                                        params.put("updateDate", new Date());
                                        params.put(JournalEntryConstants.COMPANYID, companyid);
                                        journalEntryDAO.saveJournalEntryUpdateHistory(params);
                                    }
                                    inventoryJEdetail.setAmount(amount);
                                    inventoryJEdetail.setAmountinbase(amountinbase);
                                    purchaseJEdetail.setAmount(amount);
                                    purchaseJEdetail.setAmountinbase(amountinbase);

                                }
                            }
                        }
                    } else if (transtype == TransactionBatch.DocType_PURCHASE_RETURN  && !updateTransactionAmount) { // Purchase Return
                        KwlReturnObject prdetailResult = accGoodsReceiptobj.getPurchaseReturnDetails(doRequestParams);
                        if (prdetailResult != null && prdetailResult.getEntityList() != null && !prdetailResult.getEntityList().isEmpty()) {
                            PurchaseReturnDetail prdetail = (PurchaseReturnDetail) prdetailResult.getEntityList().get(0);
                            if (prdetail != null) {
                                JournalEntryDetail inventoryJEdetail = prdetail.getInventoryJEdetail();
                                JournalEntryDetail purchaseJEDetail = prdetail.getPurchasesJEDetail();
                                if (inventoryJEdetail != null && purchaseJEDetail != null) {
                                    double amountinbase = price;
                                    amountinbase = authHandler.round(amountinbase, companyid);
                                    HashMap<String, Object> requestParams = new HashMap();
                                    requestParams.put(Constants.companyKey, inventoryJEdetail.getCompany().getCompanyID());
                                    requestParams.put(Constants.globalCurrencyKey, inventoryJEdetail.getCompany().getCurrency().getCurrencyID());
                                    double exchangeRate = prdetail.getPurchaseReturn().getExternalCurrencyRate() != 0 ? prdetail.getPurchaseReturn().getExternalCurrencyRate() : inventoryJEdetail.getJournalEntry().getExternalCurrencyRate();
                                    if (inventoryJEdetail.getCompany().getCurrency().getCurrencyID().equals(inventoryJEdetail.getJournalEntry().getCurrency().getCurrencyID())) {
                                        exchangeRate = 1; // if transaction made in base currency
                                    }
                                    if (exchangeRate == 0) {
                                        Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.WARNING, null, "Exchange rate for Journal Entry ID: " + inventoryJEdetail.getJournalEntry().getID() + " has not set.");
                                    }
                                    KwlReturnObject bAmt = currencyDAO.getBaseToCurrencyAmount(requestParams, amountinbase, inventoryJEdetail.getJournalEntry().getCurrency().getCurrencyID(), inventoryJEdetail.getJournalEntry().getEntryDate(), exchangeRate);
                                    double amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                    if (amount < 0) {
                                        amount = amount * (-1);
                                    }
                                    if (amountinbase < 0) {
                                        amountinbase = amountinbase * (-1);
                                    }
                                    JournalEntry entry = inventoryJEdetail.getJournalEntry();
                                    if (amountinbase != inventoryJEdetail.getAmountinbase()) {
                                        JSONObject params = new JSONObject();
                                        params.put(JournalEntryConstants.JEDID, inventoryJEdetail.getID());
                                        params.put(JournalEntryConstants.JEID, entry != null ? entry.getID() : "");
                                        params.put(JournalEntryConstants.TransactionModuleID, entry != null ? entry.getTransactionModuleid() : "");
                                        params.put(JournalEntryConstants.TransactionID, entry != null ? (StringUtil.isNullOrEmpty(entry.getTransactionId()) ? "" : entry.getTransactionId()) : "");
                                        params.put("oldAmountInBase", inventoryJEdetail.getAmountinbase());
                                        params.put("newAmountInBase", amountinbase);
                                        params.put("oldamount", inventoryJEdetail.getAmount());
                                        params.put("newamount", amount);
                                        params.put("exchangerate", exchangeRate);
                                        params.put("updateDate", new Date());
                                        params.put(JournalEntryConstants.COMPANYID, companyid);
                                        journalEntryDAO.saveJournalEntryUpdateHistory(params);
                                    }
                                    if (amountinbase != purchaseJEDetail.getAmountinbase()) {
                                        JSONObject params = new JSONObject();
                                        params.put(JournalEntryConstants.JEDID, purchaseJEDetail.getID());
                                        params.put(JournalEntryConstants.JEID, entry != null ? entry.getID() : "");
                                        params.put(JournalEntryConstants.TransactionModuleID, entry != null ? entry.getTransactionModuleid() : "");
                                        params.put(JournalEntryConstants.TransactionID, entry != null ? (StringUtil.isNullOrEmpty(entry.getTransactionId()) ? "" : entry.getTransactionId()) : "");
                                        params.put("oldAmountInBase", purchaseJEDetail.getAmountinbase());
                                        params.put("newAmountInBase", amountinbase);
                                        params.put("updateDate", new Date());
                                        params.put("oldamount", purchaseJEDetail.getAmount());
                                        params.put("newamount", amount);
                                        params.put("exchangerate", exchangeRate);
                                        params.put(JournalEntryConstants.COMPANYID, companyid);
                                        journalEntryDAO.saveJournalEntryUpdateHistory(params);
                                    }
                                    inventoryJEdetail.setAmount(amount);
                                    inventoryJEdetail.setAmountinbase(amountinbase);
                                    purchaseJEDetail.setAmount(amount);
                                    purchaseJEDetail.setAmountinbase(amountinbase);
                                }
                            }
                        }
                    } else if (transtype == TransactionBatch.DocType_DO  && !updateTransactionAmount) { // Delivery Order
                        KwlReturnObject dodetailResult = invoiceDAO.getDeliveryOrderDetails(doRequestParams);
                        if (dodetailResult != null && dodetailResult.getEntityList() != null && !dodetailResult.getEntityList().isEmpty()) {
                            DeliveryOrderDetail grodetail = (DeliveryOrderDetail) dodetailResult.getEntityList().get(0);
                            if (grodetail != null) {
                                JournalEntryDetail inventoryJEdetail = grodetail.getInventoryJEdetail();
                                JournalEntryDetail cogsJEdetail = grodetail.getCostOfGoodsSoldJEdetail();
                                if (inventoryJEdetail != null && cogsJEdetail != null) {
                                    double amountinbase = price;
                                    amountinbase = authHandler.round(amountinbase, companyid);
                                    HashMap<String, Object> requestParams = new HashMap();
                                    requestParams.put(Constants.companyKey, inventoryJEdetail.getCompany().getCompanyID());
                                    requestParams.put(Constants.globalCurrencyKey, inventoryJEdetail.getCompany().getCurrency().getCurrencyID());
                                    double exchangeRate = grodetail.getDeliveryOrder().getExternalCurrencyRate() != 0 ? grodetail.getDeliveryOrder().getExternalCurrencyRate() : inventoryJEdetail.getJournalEntry().getExternalCurrencyRate();
                                    if (inventoryJEdetail.getCompany().getCurrency().getCurrencyID().equals(inventoryJEdetail.getJournalEntry().getCurrency().getCurrencyID())) {
                                        exchangeRate = 1; // if transaction made in base currency
                                    }
                                    if (exchangeRate == 0) {
                                        Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.WARNING, null, "Exchange rate for Journal Entry ID: " + inventoryJEdetail.getJournalEntry().getID() + " has not set.");
                                    }
                                    KwlReturnObject bAmt = currencyDAO.getBaseToCurrencyAmount(requestParams, amountinbase, inventoryJEdetail.getJournalEntry().getCurrency().getCurrencyID(), inventoryJEdetail.getJournalEntry().getEntryDate(), exchangeRate);
                                    double amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                    if (amount < 0) { // Out Transaction is having amount as -ve
                                        amount = amount * (-1);
                                    }
                                    if (amountinbase < 0) {
                                        amountinbase = amountinbase * (-1);
                                    }
                                    JournalEntry entry = inventoryJEdetail.getJournalEntry();
                                    if (amountinbase != inventoryJEdetail.getAmountinbase()) {
                                        JSONObject params = new JSONObject();
                                        params.put(JournalEntryConstants.JEDID, inventoryJEdetail.getID());
                                        params.put(JournalEntryConstants.JEID, entry != null ? entry.getID() : "");
                                        params.put(JournalEntryConstants.TransactionModuleID, entry != null ? entry.getTransactionModuleid() : "");
                                        params.put(JournalEntryConstants.TransactionID, entry != null ? (StringUtil.isNullOrEmpty(entry.getTransactionId()) ? "" : entry.getTransactionId()) : "");
                                        params.put("oldAmountInBase", inventoryJEdetail.getAmountinbase());
                                        params.put("newAmountInBase", amountinbase);
                                        params.put("oldamount", inventoryJEdetail.getAmount());
                                        params.put("newamount", amount);
                                        params.put("exchangerate", exchangeRate);
                                        params.put("updateDate", new Date());
                                        params.put(JournalEntryConstants.COMPANYID, companyid);
                                        journalEntryDAO.saveJournalEntryUpdateHistory(params);
                                    }
                                    if (amountinbase != cogsJEdetail.getAmountinbase()) {
                                        JSONObject params = new JSONObject();
                                        params.put(JournalEntryConstants.JEDID, cogsJEdetail.getID());
                                        params.put(JournalEntryConstants.JEID, entry != null ? entry.getID() : "");
                                        params.put(JournalEntryConstants.TransactionModuleID, entry != null ? entry.getTransactionModuleid() : "");
                                        params.put(JournalEntryConstants.TransactionID, entry != null ? (StringUtil.isNullOrEmpty(entry.getTransactionId()) ? "" : entry.getTransactionId()) : "");
                                        params.put("oldAmountInBase", cogsJEdetail.getAmountinbase());
                                        params.put("newAmountInBase", amountinbase);
                                        params.put("updateDate", new Date());
                                        params.put("oldamount", cogsJEdetail.getAmount());
                                        params.put("newamount", amount);
                                        params.put("exchangerate", exchangeRate);
                                        params.put(JournalEntryConstants.COMPANYID, companyid);
                                        journalEntryDAO.saveJournalEntryUpdateHistory(params);
                                    }
                                    inventoryJEdetail.setAmount(amount);
                                    inventoryJEdetail.setAmountinbase(amountinbase);
                                    cogsJEdetail.setAmount(amount);
                                    cogsJEdetail.setAmountinbase(amountinbase);
                                }
                            }
                        }
                    } else if (transtype == TransactionBatch.DocType_WO_OUT) { // Work Order OUT Transaction
                        String orderdetailid = detailID;
                        String stockMovementId = detailID;
                        double blockQuantityUsed = 0.0;
                        double ParentProductPriceTotal = 0.0;
                        double ParentProductPriceUnit=0.0;
                        double ParentProductProduceQuantityOld = 0.0;
                        double subProductprice = 0;
                        double totalcoproductscrapamount = 0;
                        boolean isNeed_TO_Update_Co_Scrap_Product_Price_JE = false;
                        double wocdInitialPurchasePrice = 0;
                        int count = 0;


                        /*
                         * Based on stockMovementId fetch SM Entry for WOCD Entry
                         */

                        KwlReturnObject stockMovementKwlObject = accountingHandlerDAOobj.getObject(StockMovement.class.getName(), stockMovementId);
                        if (stockMovementKwlObject != null && stockMovementKwlObject.getEntityList() != null && !stockMovementKwlObject.getEntityList().isEmpty()) {

                            StockMovement stockMovementObject = (StockMovement) stockMovementKwlObject.getEntityList().get(0);
                            if (stockMovementObject != null) {

                                orderdetailid = stockMovementObject.getModuleRefDetailId();

                                KwlReturnObject detailObject = accountingHandlerDAOobj.getObject(WorkOrderComponentDetails.class.getName(), orderdetailid);
                                if (detailObject != null && detailObject.getEntityList() != null && !detailObject.getEntityList().isEmpty()) {

                                    /**
                                     * Consumed Product WorkOrderComponentDetails Entry.
                                     */
                                    
                                    WorkOrderComponentDetails orderComponentDetails = (WorkOrderComponentDetails) detailObject.getEntityList().get(0);
                                    if (orderComponentDetails != null) {

                                        
                                        if (batchDetail.getPrice() != 0) {

                                            /**
                                             * Update Stock movement price.
                                             */
                                            stockMovementObject.setPricePerUnit(batchDetail.getPrice());
                                            stockMovementDAO.saveOrUpdate(stockMovementObject);

                                            List<StockMovement> smList = null;
                                            smList = stockMovementService.getStockMovementListByReferenceIdForWorkOrder(company, orderdetailid);

                                            if (stockMovementObject.getStock_management_flag() == Constants.MRP_WASTE_STOCK_MANAGEMENT_FLAG || stockMovementObject.getStock_management_flag() == Constants.MRP_RECYCLE_STOCK_MANAGEMENT_FLAG || stockMovementObject.getStock_management_flag() == Constants.MRP_RETURN_STOCK_MANAGEMENT_FLAG) {

                                                /**
                                                 * Update Stock movement price for Waste/Recycle/Return IN Entry based on It's OUT Entry.
                                                 */
                                                if (!smList.isEmpty()) {
                                                    for (StockMovement sm : smList) {
                                                        if (stockMovementObject.getStock_management_flag() == sm.getStock_management_flag()) {
                                                            sm.setPricePerUnit(batchDetail.getPrice());
                                                            stockMovementDAO.saveOrUpdate(sm);
                                                        }
                                                    }
                                                }

                                            }

                                            /**
                                             * calculate initial purchase price for WOCD based on Stock movement price of Consume Quantity.
                                             * Batch  Consumed Quantity  Unit Price  Amount 
                                             *  B1       10                 1         10
                                             *  B2       10                 2         20  
                                             *  B3       10                 3         30
                                             * 
                                             * Average Price = (Sum of Unit Price) / No. of Batch
                                             * initial purchase price in WOCD will be Average Price.
                                             */
                                            if (stockMovementObject.getStock_management_flag() == Constants.MRP_CONSUME_PRODUCE_STOCK_MANAGEMENT_FLAG) {

                                                if (!smList.isEmpty()) {
                                                    for (StockMovement sm : smList) {
                                                        if (stockMovementObject.getStock_management_flag() == sm.getStock_management_flag() && sm.getTransactionType().ordinal() == 2) {
                                                            wocdInitialPurchasePrice += sm.getPricePerUnit();
                                                            count++;
                                                        }
                                                    }
                                                }
                                                
                                                /**
                                                 * UPDATE InitialPurchasePrice in WorkOrderComponentDetail
                                                 */
                                                orderComponentDetails.setInitialPurchasePrice(authHandler.round(wocdInitialPurchasePrice / count, companyid));
                                                accountingHandlerDAOobj.saveOrUpdateObject(orderComponentDetails);

                                            
                                            /**
                                             * For Consumed product, update JE detail.
                                             */
                                            
                                            JournalEntryDetail inventoryJEdetail = orderComponentDetails.getInventoryJEdetail();

                                            if (inventoryJEdetail != null) {

                                                HashMap<String, Object> requestParams = new HashMap();
                                                requestParams.put(Constants.companyKey, inventoryJEdetail.getCompany().getCompanyID());
                                                requestParams.put(Constants.globalCurrencyKey, inventoryJEdetail.getCompany().getCurrency().getCurrencyID());
                                                double exchangeRate = 1;
                                                double amount = authHandler.round(orderComponentDetails.getInitialPurchasePrice() * orderComponentDetails.getBlockQuantityUsed() * exchangeRate, companyid); // change batchDetail.getPrice() to initialPurchasePrice for updating JE based on consume entry price. 
                                                double amountinbase = amount;
                                                if (amount < 0) { // Out Transaction is having amount as -ve
                                                    amount = amount * (-1);
                                                }
                                                if (amountinbase < 0) {
                                                    amountinbase = amountinbase * (-1);
                                                }
                                                JournalEntry entry = inventoryJEdetail.getJournalEntry();
                                                boolean ischange = false;
                                                if (amountinbase != inventoryJEdetail.getAmountinbase()) {
                                                    JSONObject params = new JSONObject();
                                                    params.put(JournalEntryConstants.JEDID, inventoryJEdetail.getID());
                                                    params.put(JournalEntryConstants.JEID, entry != null ? entry.getID() : "");
                                                    params.put(JournalEntryConstants.TransactionModuleID, entry != null ? entry.getTransactionModuleid() : "");
                                                    params.put(JournalEntryConstants.TransactionID, entry != null ? (StringUtil.isNullOrEmpty(entry.getTransactionId()) ? "" : entry.getTransactionId()) : "");
                                                    params.put("oldAmountInBase", inventoryJEdetail.getAmountinbase());
                                                    params.put("newAmountInBase", amountinbase);
                                                    params.put("oldamount", inventoryJEdetail.getAmount());
                                                    params.put("newamount", amount);
                                                    params.put("exchangerate", exchangeRate);
                                                    params.put("updateDate", new Date());
                                                    params.put(JournalEntryConstants.COMPANYID, companyid);
                                                    journalEntryDAO.saveJournalEntryUpdateHistory(params);
                                                    inventoryJEdetail.setAmount(amount);
                                                    inventoryJEdetail.setAmountinbase(amountinbase);
                                                }
                                            }



                                            String wocproductid = orderComponentDetails.getProduct().getID();
                                            String wocParentProid = orderComponentDetails.getParentProduct().getID();

                                            JSONObject paramjsonforsubassproduct = new JSONObject();
                                            paramjsonforsubassproduct.put("productid", wocParentProid);
                                            KwlReturnObject kwrl = productDAO.getSubAssemblyProduct(paramjsonforsubassproduct);
                                            List<ProductAssembly> productAssemblysOFWOD = kwrl.getEntityList();

                                            /**
                                             * Check Whether product BOM Type is
                                             * COMPONENT TYPE If Yes Then
                                             * isNeed_TO_Update_Co_Scrap_Product_Price_JE
                                             * Flag will be True If Flag is True
                                             * Then Co-Product or Scrap Type
                                             * Product PRICE and Journal Entry
                                             * will be UPDATED.
                                             */
                                            
                                            for (ProductAssembly assembly : productAssemblysOFWOD) {
                                                if ((assembly.getProduct().getID().equals(wocParentProid)) && (assembly.getSubproducts().getID().equals(wocproductid))) {
                                                    if (assembly.getComponentType() == 1) {
                                                        isNeed_TO_Update_Co_Scrap_Product_Price_JE = true;
                                                    }
                                                }
                                            }

                                            Set<WorkOrderComponentDetails> wocomponentdetailset = orderComponentDetails.getWorkOrder().getComponentDetails();

                                            /**
                                             * For Parent Assembly product
                                             * Calculate unit price based on its
                                             * Immediate BOM product Block
                                             * quantity Used and Initial
                                             * purchase price.
                                             */
                                            
                                            /**
                                             * IF Co-Product/Scrap Feature is ON
                                             * then This unit Price is
                                             * TotalComponentTypeProductPrice(Needed
                                             * to Calculate Co-Scrap Product
                                             * Price) IF Co-Product/Scrap
                                             * Feature is OFF then This unit
                                             * Price is
                                             * ParentAssemblyProductPrice.
                                             */
                                            for (WorkOrderComponentDetails wocomdetail : wocomponentdetailset) {
                                                if (wocomdetail != null && wocomdetail.getParentProduct() != null) {
                                                    if (wocomdetail.getParentProduct().getID().equals(orderComponentDetails.getParentProduct().getID())) {
                                                        if (!(wocomdetail.getProduct().getProducttype().getID().equals(Constants.INVENTORY_PART) && wocomdetail.getProducedQuantity() != 0)) {
                                                            if (wocomdetail.isBlockQtyUsed()) {
                                                                blockQuantityUsed = wocomdetail.getBlockQuantityUsed();
                                                            } else {
                                                                blockQuantityUsed = wocomdetail.getBlockQuantity();
                                                            }
                                                            subProductprice = wocomdetail.getInitialPurchasePrice();
                                                            ParentProductPriceTotal += authHandler.round(blockQuantityUsed * subProductprice, companyid);
                                                        }
                                                    }
                                                }
                                            }


                                            if (isNeed_TO_Update_Co_Scrap_Product_Price_JE) {

                                                /**
                                                 * For co-product or scrap
                                                 * product, update JE and WOC
                                                 * detail table Price =
                                                 * component-type-products
                                                 * updated price(based on
                                                 * valuation) * (Rate/100).
                                                 */
                                                
                                                for (WorkOrderComponentDetails wocomdetail : wocomponentdetailset) {
                                                    if (wocomdetail != null && wocomdetail.getParentProduct() != null) {
                                                        if (wocomdetail.getParentProduct().getID().equals(orderComponentDetails.getParentProduct().getID())) {
                                                            if (wocomdetail.getProduct().getProducttype().getID().equals(Constants.INVENTORY_PART)) {

                                                                for (ProductAssembly assembly : productAssemblysOFWOD) {
                                                                    if ((assembly.getProduct().getID().equals(wocomdetail.getParentProduct().getID())) && (assembly.getSubproducts().getID().equals(wocomdetail.getProduct().getID()))) {
                                                                        if (assembly.getComponentType() == 2 || assembly.getComponentType() == 3) {

                                                                            double crate = assembly.getCrate();
                                                                            double producedQuantityOfCoProduct_Scrap=0.0;
                                                                            if (wocomdetail.getProducedQuantity() != 0) {
                                                                                producedQuantityOfCoProduct_Scrap = wocomdetail.getProducedQuantity();
                                                                            } else {
                                                                                producedQuantityOfCoProduct_Scrap = authHandler.round((assembly.getQuantity())*(wocomdetail.getWorkOrder().getQuantity()), companyid);
                                                                            }
                                                                            double CoProduct_ScrapAmount = ParentProductPriceTotal * (crate / 100);
                                                                            double CoProduct_ScrapAmountInbase = CoProduct_ScrapAmount;
                                                                            double CoProduct_ScrapUnitPriceForSM= authHandler.round(CoProduct_ScrapAmount/producedQuantityOfCoProduct_Scrap, companyid);
                                                                            wocomdetail.setInitialPurchasePrice(CoProduct_ScrapAmount);
                                                                            accountingHandlerDAOobj.saveOrUpdateObject(wocomdetail);
//                                                                
                                                                /*
                                                                             * Update
                                                                             * JE
                                                                             * Detail
                                                                             * For
                                                                             * CO-Scrap
                                                                             * Product
                                                                             */

                                                                            JournalEntryDetail Wocije = wocomdetail.getInventoryJEdetail();
                                                                            if (Wocije != null) {
                                                                                double exchangeRate_coscrap = 1;
                                                                                JournalEntry entry_coscrap = Wocije.getJournalEntry();
                                                                                if (CoProduct_ScrapAmountInbase != Wocije.getAmountinbase()) {
                                                                                    JSONObject params_coscrap = new JSONObject();
                                                                                    params_coscrap.put(JournalEntryConstants.JEDID, Wocije.getID());
                                                                                    params_coscrap.put(JournalEntryConstants.JEID, entry_coscrap != null ? entry_coscrap.getID() : "");
                                                                                    params_coscrap.put(JournalEntryConstants.TransactionModuleID, entry_coscrap != null ? entry_coscrap.getTransactionModuleid() : "");
                                                                                    params_coscrap.put(JournalEntryConstants.TransactionID, entry_coscrap != null ? (StringUtil.isNullOrEmpty(entry_coscrap.getTransactionId()) ? "" : entry_coscrap.getTransactionId()) : "");
                                                                                    params_coscrap.put("oldAmountInBase", Wocije.getAmountinbase());
                                                                                    params_coscrap.put("newAmountInBase", CoProduct_ScrapAmountInbase);
                                                                                    params_coscrap.put("oldamount", Wocije.getAmount());
                                                                                    params_coscrap.put("newamount", CoProduct_ScrapAmount);
                                                                                    params_coscrap.put("exchangerate", exchangeRate_coscrap);
                                                                                    params_coscrap.put("updateDate", new Date());
                                                                                    params_coscrap.put(JournalEntryConstants.COMPANYID, companyid);
                                                                                    journalEntryDAO.saveJournalEntryUpdateHistory(params_coscrap);

                                                                                }
                                                                                Wocije.setAmount(CoProduct_ScrapAmount);
                                                                                Wocije.setAmountinbase(CoProduct_ScrapAmountInbase);

                                                                            }

                                                                            /**
                                                                             * Update
                                                                             * Stock
                                                                             * movement
                                                                             * price
                                                                             * For
                                                                             * Co-Scrap
                                                                             * Product.
                                                                             */
                                                                            List<StockMovement> smListCoScrap = null;
                                                                            smListCoScrap = stockMovementService.getStockMovementListByReferenceIdForWorkOrder(company, wocomdetail.getID());
                                                                            if (!smListCoScrap.isEmpty()) {
                                                                                for (StockMovement sm : smListCoScrap) {
                                                                                    sm.setPricePerUnit(CoProduct_ScrapUnitPriceForSM);
                                                                                    stockMovementDAO.saveOrUpdate(sm);
                                                                                }
                                                                            }
                                                                            totalcoproductscrapamount += CoProduct_ScrapAmount;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }


                                            String TempwocParentProid = "";
                                            /**
                                             * Update Parent product
                                             * price(Assembly).
                                             */
                                            for (WorkOrderComponentDetails wocomdetail : wocomponentdetailset) {
                                                if (wocomdetail != null) {
                                                    if ((wocomdetail.getProduct().getID()).equals(orderComponentDetails.getParentProduct().getID())) {
                                                        /**
                                                         * IF Co-Product/Scrap
                                                         * Feature is ON then
                                                         * Parent product
                                                         * price(Assembly) is
                                                         * TotalComponentTypeProductPrice
                                                         * -
                                                         * TotalCoScrapTypeProductPrice.
                                                         */
                                                        if (isNeed_TO_Update_Co_Scrap_Product_Price_JE) {
                                                            ParentProductPriceTotal = ParentProductPriceTotal - totalcoproductscrapamount;
                                                        }
                                                        
                                                        TempwocParentProid = wocomdetail.getParentProduct() != null ? wocomdetail.getParentProduct().getID() : "";
                                                        if (wocomdetail.getProducedQuantity() > 0) {
                                                            ParentProductProduceQuantityOld = wocomdetail.getProducedQuantity();
                                                        } else {
                                                            ParentProductProduceQuantityOld = wocomdetail.getRequiredQuantity();
                                                        }
                                                        
                                                        ParentProductPriceUnit=authHandler.round(ParentProductPriceTotal/ParentProductProduceQuantityOld, companyid);
                                                        
                                                        wocomdetail.setInitialPurchasePrice(ParentProductPriceUnit);
                                                        accountingHandlerDAOobj.saveOrUpdateObject(wocomdetail);


                                                        /**
                                                         * For assembly product
                                                         * Update JE Detail.
                                                         */
                                                        
                                                        JournalEntryDetail wojed = wocomdetail.getWorkOrder().getTotalinventoryJEdetail();
                                                        if (wojed != null) {
                                                            double ExchangeRate_assembly = 1;
                                                            double amountforParent = 0;
                                                            amountforParent = ParentProductPriceTotal;
                                                            double amountinbaseforParent = ParentProductPriceTotal;
                                                            JournalEntry woJE = wojed.getJournalEntry();
                                                            if (woJE != null) {
                                                                if (amountinbaseforParent != wojed.getAmountinbase()) {
                                                                    JSONObject params_assembly = new JSONObject();
                                                                    params_assembly.put(JournalEntryConstants.JEDID, wojed.getID());
                                                                    params_assembly.put(JournalEntryConstants.JEID, woJE != null ? woJE.getID() : "");
                                                                    params_assembly.put(JournalEntryConstants.TransactionModuleID, woJE != null ? woJE.getTransactionModuleid() : "");
                                                                    params_assembly.put(JournalEntryConstants.TransactionID, woJE != null ? (StringUtil.isNullOrEmpty(woJE.getTransactionId()) ? "" : woJE.getTransactionId()) : "");
                                                                    params_assembly.put("oldAmountInBase", wojed.getAmountinbase());
                                                                    params_assembly.put("newAmountInBase", amountinbaseforParent);
                                                                    params_assembly.put("oldamount", wojed.getAmount());
                                                                    params_assembly.put("newamount", amountforParent);
                                                                    params_assembly.put("exchangerate", ExchangeRate_assembly);
                                                                    params_assembly.put("updateDate", new Date());
                                                                    params_assembly.put(JournalEntryConstants.COMPANYID, companyid);
                                                                    journalEntryDAO.saveJournalEntryUpdateHistory(params_assembly);

                                                                }

                                                            }
                                                            wojed.setAmount(amountforParent);
                                                            wojed.setAmountinbase(amountinbaseforParent);
                                                        }

                                                        /**
                                                         * Update Stock movement
                                                         * price For Parent
                                                         * Assembly Product.
                                                         */
                                                        
                                                        List<StockMovement> smListParentAssembly = null;
//                                          smListParentAssembly = stockMovementService.getStockMovementListByReferenceId(company, wocomdetail.getID());//change done
                                                        smListParentAssembly = stockMovementService.getStockMovementListByReferenceIdForWorkOrder(company, wocomdetail.getID());
                                                        if (!smListParentAssembly.isEmpty()) {
                                                            for (StockMovement sm : smListParentAssembly) {
                                                                sm.setPricePerUnit(ParentProductPriceUnit);
                                                                stockMovementDAO.saveOrUpdate(sm);
                                                            }
                                                        }
                                                    }
                                                }

                                            }


                                            while (!StringUtil.isNullOrEmpty(TempwocParentProid)) {

                                                double blockQuantityUsednew = 0.0;
                                                double ParentProductPriceTotalnew = 0.0;
                                                double ParentProductProduceQuantity = 0.0;
                                                double ParentProductPriceUnitnew = 0.0;
                                                double subProductpricenew = 0;

                                                for (WorkOrderComponentDetails wocomdetail : wocomponentdetailset) {
                                                    if (wocomdetail != null && wocomdetail.getParentProduct() != null) {
                                                        if (wocomdetail.getParentProduct().getID().equals(TempwocParentProid)) {
                                                            if (!(wocomdetail.getProduct().getProducttype().getID().equals(Constants.INVENTORY_PART) && wocomdetail.getProducedQuantity() != 0)) {
                                                                if (wocomdetail.isBlockQtyUsed()) {
                                                                    blockQuantityUsednew = wocomdetail.getBlockQuantityUsed();
                                                                } else {
                                                                    blockQuantityUsednew = wocomdetail.getRequiredQuantity();
                                                                }
                                                                subProductpricenew = wocomdetail.getInitialPurchasePrice();
                                                                ParentProductPriceTotalnew += authHandler.round(blockQuantityUsednew * subProductpricenew, companyid);
                                                            }
                                                        }
                                                    }
                                                }

                                                for (WorkOrderComponentDetails wocomdetail : wocomponentdetailset) {
                                                    if (wocomdetail != null) {
                                                        if ((wocomdetail.getProduct().getID()).equals(TempwocParentProid)) {

                                                            TempwocParentProid = wocomdetail.getParentProduct() != null ? wocomdetail.getParentProduct().getID() : "";
                                                            if (wocomdetail.getProducedQuantity()>0) {
                                                                ParentProductProduceQuantity = wocomdetail.getProducedQuantity();
                                                            } else {
                                                                ParentProductProduceQuantity = wocomdetail.getRequiredQuantity();
                                                            }
                                                            ParentProductPriceUnitnew=authHandler.round(ParentProductPriceTotalnew/ParentProductProduceQuantity, companyid);
                                                            wocomdetail.setInitialPurchasePrice(ParentProductPriceUnitnew);
                                                            accountingHandlerDAOobj.saveOrUpdateObject(wocomdetail);
                                                            break;
                                                        }
                                                    }
                                                }

                                            }
                                          
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        
                    } else if (transtype == TransactionBatch.DocType_SALES_RETURN && !updateTransactionAmount) { // Sales Return 
                        KwlReturnObject srdetailResult = invoiceDAO.getSalesReturnDetails(doRequestParams);
                        if (srdetailResult != null && srdetailResult.getEntityList() != null && !srdetailResult.getEntityList().isEmpty()) {
                            SalesReturnDetail srdetail = (SalesReturnDetail) srdetailResult.getEntityList().get(0);
                            if (srdetail != null) {
                                JournalEntryDetail inventoryJEdetail = srdetail.getInventoryJEdetail();
                                JournalEntryDetail cogsJEdetail = srdetail.getCostOfGoodsSoldJEdetail();
                                if (inventoryJEdetail != null && cogsJEdetail != null) {
                                    double amountinbase = price;
                                    amountinbase = authHandler.round(amountinbase, companyid);
                                    HashMap<String, Object> requestParams = new HashMap();
                                    requestParams.put(Constants.companyKey, inventoryJEdetail.getCompany().getCompanyID());
                                    requestParams.put(Constants.globalCurrencyKey, inventoryJEdetail.getCompany().getCurrency().getCurrencyID());
                                    double exchangeRate = srdetail.getSalesReturn().getExternalCurrencyRate() != 0 ? srdetail.getSalesReturn().getExternalCurrencyRate() : inventoryJEdetail.getJournalEntry().getExternalCurrencyRate();
                                    if (inventoryJEdetail.getCompany().getCurrency().getCurrencyID().equals(inventoryJEdetail.getJournalEntry().getCurrency().getCurrencyID())) {
                                        exchangeRate = 1; // if transaction made in base currency
                                    }
                                    if (exchangeRate == 0) {
                                        Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.WARNING, null, "Exchange rate for Journal Entry ID: " + inventoryJEdetail.getJournalEntry().getID() + " has not set.");
                                    }
                                    KwlReturnObject bAmt = currencyDAO.getBaseToCurrencyAmount(requestParams, amountinbase, inventoryJEdetail.getJournalEntry().getCurrency().getCurrencyID(), inventoryJEdetail.getJournalEntry().getEntryDate(), exchangeRate);
                                    double amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                    JournalEntry entry = inventoryJEdetail.getJournalEntry();
                                    if (amountinbase != inventoryJEdetail.getAmountinbase()) {
                                        JSONObject params = new JSONObject();
                                        params.put(JournalEntryConstants.JEDID, inventoryJEdetail.getID());
                                        params.put(JournalEntryConstants.JEID, entry != null ? entry.getID() : "");
                                        params.put(JournalEntryConstants.TransactionModuleID, entry != null ? entry.getTransactionModuleid() : "");
                                        params.put(JournalEntryConstants.TransactionID, entry != null ? (StringUtil.isNullOrEmpty(entry.getTransactionId()) ? "" : entry.getTransactionId()) : "");
                                        params.put("oldAmountInBase", inventoryJEdetail.getAmountinbase());
                                        params.put("newAmountInBase", amountinbase);
                                        params.put("oldamount", inventoryJEdetail.getAmount());
                                        params.put("newamount", amount);
                                        params.put("exchangerate", exchangeRate);
                                        params.put("updateDate", new Date());
                                        params.put(JournalEntryConstants.COMPANYID, companyid);
                                        journalEntryDAO.saveJournalEntryUpdateHistory(params);
                                    }
                                    if (amountinbase != cogsJEdetail.getAmountinbase()) {
                                        JSONObject params = new JSONObject();
                                        params.put(JournalEntryConstants.JEDID, cogsJEdetail.getID());
                                        params.put(JournalEntryConstants.JEID, entry != null ? entry.getID() : "");
                                        params.put(JournalEntryConstants.TransactionModuleID, entry != null ? entry.getTransactionModuleid() : "");
                                        params.put(JournalEntryConstants.TransactionID, entry != null ? (StringUtil.isNullOrEmpty(entry.getTransactionId()) ? "" : entry.getTransactionId()) : "");
                                        params.put("oldAmountInBase", cogsJEdetail.getAmountinbase());
                                        params.put("newAmountInBase", amountinbase);
                                        params.put("updateDate", new Date());
                                        params.put("oldamount", cogsJEdetail.getAmount());
                                        params.put("newamount", amount);
                                        params.put("exchangerate", exchangeRate);
                                        params.put(JournalEntryConstants.COMPANYID, companyid);
                                        journalEntryDAO.saveJournalEntryUpdateHistory(params);
                                    }
                                    inventoryJEdetail.setAmount(amount);
                                    inventoryJEdetail.setAmountinbase(amountinbase);
                                    cogsJEdetail.setAmount(amount);
                                    cogsJEdetail.setAmountinbase(amountinbase);
                                }
                            }
                        }
                    } else if (((transtype == TransactionBatch.DocType_SA_IN || transtype == TransactionBatch.DocType_SA_OUT ) && !updateTransactionAmount )) {
                        StockAdjustment stockAdjustment = stockAdjustmentDAO.getStockAdjustmentById(detailID);
                        if (stockAdjustment != null && stockAdjustment.getInventoryJE() != null) {
                            JournalEntry inventoryJE = stockAdjustment.getInventoryJE();
                            if (inventoryJE != null) {
                                double amountinbase = price;
                                amountinbase = authHandler.round(amountinbase, companyid);
                                HashMap<String, Object> requestParams = new HashMap();
                                Set<JournalEntryDetail> details = inventoryJE.getDetails();
                                if (details != null && !details.isEmpty()) {
                                    double exchangeRate = inventoryJE.getExternalCurrencyRate();
                                    if (inventoryJE.getCompany().getCurrency().getCurrencyID().equals(inventoryJE.getCurrency().getCurrencyID())) {
                                        exchangeRate = 1; // if transaction made in base currency
                                    }
                                    KwlReturnObject bAmt = currencyDAO.getBaseToCurrencyAmount(requestParams, amountinbase, inventoryJE.getCurrency().getCurrencyID(), inventoryJE.getEntryDate(), exchangeRate);
                                    requestParams.put(Constants.companyKey, inventoryJE.getCompany().getCompanyID());
                                    requestParams.put(Constants.globalCurrencyKey, inventoryJE.getCompany().getCurrency().getCurrencyID());
                                    double amount= authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                    if (transtype == TransactionBatch.DocType_SA_OUT && amount < 0) {
                                        amount = amount * (-1);
                                    }
                                    if (amountinbase < 0) {
                                        amountinbase = amountinbase * (-1);
                                    }
                                    for (JournalEntryDetail entryDetail : details) {
                                        if (amountinbase != entryDetail.getAmountinbase()) {
                                            JSONObject params = new JSONObject();
                                            params.put(JournalEntryConstants.JEDID, entryDetail.getID());
                                            params.put(JournalEntryConstants.JEID, inventoryJE.getID());
                                            params.put(JournalEntryConstants.TransactionModuleID, inventoryJE.getTransactionModuleid());
                                            params.put(JournalEntryConstants.TransactionID, (StringUtil.isNullOrEmpty(inventoryJE.getTransactionId()) ? "" : inventoryJE.getTransactionId()));
                                            params.put("oldAmountInBase", entryDetail.getAmountinbase());
                                            params.put("newAmountInBase", amountinbase);
                                            params.put("oldamount", entryDetail.getAmount());
                                            params.put("newamount", amount);
                                            params.put("exchangerate", exchangeRate);
                                            params.put("updateDate", new Date());
                                            params.put(JournalEntryConstants.COMPANYID, companyid);
                                            journalEntryDAO.saveJournalEntryUpdateHistory(params);
                                        }
                                        entryDetail.setAmount(amount);
                                        entryDetail.setAmountinbase(amountinbase);
                                    }
                                }
                            }
                        }
                    }
                    if (transtype == TransactionBatch.DocType_SA_OUT) {
                        StockAdjustment stockAdjustment = stockAdjustmentDAO.getStockAdjustmentById(detailID);
                        if (!stockAdjustment.isPriceupdated() || updateStockAdjustmentPrice) {
                                stockAdjustment.setPricePerUnit(batchDetail.getPrice());
                            stockAdjustment.setPriceupdated(true);
                            stockAdjustmentDAO.saveOrUpdateAdjustment(stockAdjustment);
                            List<StockMovement> smList = null;
                            smList = stockMovementService.getStockMovementListByReferenceId(company, detailID);
                            if (!smList.isEmpty()) {
                                for (StockMovement sm : smList) {
                                        sm.setPricePerUnit(batchDetail.getPrice());
                                    stockMovementDAO.saveOrUpdate(sm);
                                }
                            }
                        }
                    }
                    if (transtype == TransactionBatch.DocType_ASSEMBLY_SUB) {
                        /**
                         * Update Price for Build Assembly. Product cost will be
                         * equals to sum of product of all ingredient's quantity
                         * & rate.
                         */
                        try {
                            KwlReturnObject result = productDAO.getProductBuildDetails(detailID);
                            if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                ProductBuildDetails pbd = (ProductBuildDetails) result.getEntityList().get(0);
                                if (pbd != null) {
                                    JournalEntryDetail jed = pbd.getJedetail();
                                    JournalEntryDetail Wastagejed = pbd.getWastagejedetail();
                                    ProductBuild pb = pbd.getBuild();
                                    if (pb != null && pb.isIsBuild()) {
                                        double buildQty = pb.getQuantity();
                                        double inventoryQty = pbd.getInventoryQuantity();
                                        pbd.setRate(batchDetail.getPrice());
                                        double pbdAmount = 0d;
                                        /**
                                         * if assembly products and sub-products having different currencies 
                                         * then amount should be saved according to exchanges rate in base currency.
                                         */
                                        if (pbd.getAproduct().getCurrency() != pb.getProduct().getCurrency() && jed.getExchangeRateForTransaction()!=0) {
                                            pbdAmount = buildQty * inventoryQty * (batchDetail.getPrice() / jed.getExchangeRateForTransaction());
                                        } else {
                                            pbdAmount = buildQty * inventoryQty * batchDetail.getPrice();
                                        }
                                        double pbdwastageAmount = 0;
                                        if (jed != null) {
                                            jed.setAmount(authHandler.round(pbdAmount, companyid));
                                            jed.setAmountinbase(authHandler.round(pbdAmount, companyid));
                                        }
                                        /**
                                         * Updating amount of Wastage JE detail for BOM according to valuation price. 
                                         */
                                        if (pbd.getWastageQuantityType() == 1) { // For Percentage
                                            pbdwastageAmount = authHandler.round(batchDetail.getPrice() * ((pbd.getWastageQuantity() * pbd.getWastageInventoryQuantity()) / 100) * pb.getQuantity(), companyid);
                                        } else {
                                            pbdwastageAmount = authHandler.round(batchDetail.getPrice() * pbd.getWastageQuantity() * pb.getQuantity(), companyid);
                                        }
                                        if (Wastagejed != null) {
                                            Wastagejed.setAmount(authHandler.round(pbdwastageAmount, companyid));
                                            Wastagejed.setAmountinbase(authHandler.round(pbdwastageAmount, companyid));
                                        }
                                        KwlReturnObject resultDetails = productDAO.getAssemblyBuidDetails(pb.getID());
                                        
                                        List<ProductBuildDetails> pbdetails = resultDetails.getEntityList();
                                        double buildCost = 0;
                                        double wastageCost = 0;
                                        for (ProductBuildDetails productBuildDetail : pbdetails) {
                                            if (productBuildDetail.getID().equals(pbd.getID())) {
                                                buildCost += pbdAmount;
                                                wastageCost += pbdwastageAmount;
                                            } else {
                                                JournalEntryDetail pbdjed = productBuildDetail.getJedetail();
                                                JournalEntryDetail pbdWastagejed = productBuildDetail.getWastagejedetail();
                                                if (pbdjed != null) {
                                                    buildCost += pbdjed.getAmountinbase();
                                                    if (pbdWastagejed != null) {
                                                        wastageCost += pbdWastagejed.getAmountinbase();
                                                    }
                                                }else {
                                                    buildCost += pb.getQuantity() * productBuildDetail.getInventoryQuantity() * productBuildDetail.getRate();
                                                }
                                                
                                            }
                                        }
                                        JournalEntryDetail totaljed = pb.getTotaljed();
                                        pb.setProductcost(buildCost);
                                        if (totaljed != null) {
                                            totaljed.setAmount(authHandler.round(buildCost, companyid));
                                            totaljed.setAmountinbase(authHandler.round(buildCost, companyid));
                                        }
                                       /**
                                         * Updating amount of Wastage JE detail for Assembly product according to valuation price. 
                                         */
                                        JournalEntryDetail wastageTotaljed = pb.getWastagetotaljed();
                                        if (wastageTotaljed != null) {
                                            wastageTotaljed.setAmount(authHandler.round(wastageCost, companyid));
                                            wastageTotaljed.setAmountinbase(authHandler.round(wastageCost, companyid));
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    if (transtype == TransactionBatch.DocType_IN_ISSUE || transtype == TransactionBatch.DocType_SR_ISSUE 
                            || transtype == TransactionBatch.DocType_ILT_ISSUE) {
                        /**
                         * Price for IN transaction should be same as OUT
                         * transaction. For example, User has created IST from
                         * Mumbai store to pune store and according to valuation
                         * method set for the product price for OUT transaction
                         * from Mumbai Store is $10 then IN transaction in Pune
                         * store should have the same price i.e. $10.
                         */
                         List<StockMovement> smList = stockMovementService.getStockMovementListByReferenceId(company, detailID);
                        if (smList!=null && !smList.isEmpty()) {
                            for (StockMovement sm : smList) {
                                sm.setPricePerUnit(batchDetail.getPrice());
                                stockMovementDAO.saveOrUpdate(sm);
                            }
                        }
                       // ERP-39060 update Return Request valuations based on parent ID hence get the child SM entries and update price in those
                        smList = stockMovementService.getStockMovementListOfSRReturnRequest(company, batchDetail.getBillid());
                        if (smList != null && !smList.isEmpty()) {
                            for (StockMovement sm : smList) {
                                sm.setPricePerUnit(batchDetail.getPrice());
                                stockMovementDAO.saveOrUpdate(sm);
                            }
                        }
                    } else  if(transtype == TransactionBatch.DocType_IST_ISSUE) {
                        List<StockMovement> smList = stockMovementService.getStockMovementListById(company, detailID);
                        if (smList != null && !smList.isEmpty()) {
                            for (StockMovement sm : smList) {
                                sm.setPricePerUnit(batchDetail.getPrice());
                                stockMovementDAO.saveOrUpdate(sm);
                            }
                        }
                        smList = stockMovementService.getStockMovementListOfISTReturnRequest(company, batchDetail.getBillid());
                        if (smList != null && !smList.isEmpty()) {
                            for (StockMovement sm : smList) {
                                sm.setPricePerUnit(batchDetail.getPrice());
                                stockMovementDAO.saveOrUpdate(sm);
                            }
                        }
                    }
//                    if (transtype == TransactionBatch.DocType_IST_ISSUE) {
//                        List<StockMovement> smList = stockMovementService.getStockMovementListById(company, detailID);
//                        if (smList != null && !smList.isEmpty()) {
//                            for (StockMovement sm : smList) {
//                                sm.setPricePerUnit(batchDetail.getPrice());
//                                stockMovementDAO.saveOrUpdate(sm);
//                            }
//                        }
//                    }
                }
            }
            txnManager.commit(status);
        } catch (NumberFormatException | NullPointerException | ServiceException | TransactionException ex) {
            Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.SEVERE, null, ex);
            txnManager.rollback(status);
        } catch (Exception ex) {
            Logger.getLogger(InventoryValuationProcess.class.getName()).log(Level.SEVERE, null, ex);
            txnManager.rollback(status);
        }
    }
}
