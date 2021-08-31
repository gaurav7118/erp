/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.inventory;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Inventory;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.inventory.model.stockmovement.StockMovementDAO;
import com.krawler.inventory.model.stockmovement.StockMovementDetail;
import com.krawler.inventory.model.stockmovement.StockMovementService;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockmovement.TransactionType;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import org.hibernate.HibernateException;
import org.springframework.dao.DataAccessException;

/**
 *
 * @author krawler
 */
public class AccImportServiceImpl extends BaseDAO implements AccImportService {

    private StockService stockServiceObj;
    private StockMovementService stockMovementServiceObj;
    private AccountingHandlerDAO accountingHandlerDAO;
    private StockMovementDAO stockMovementDAO;


    public void setStockServiceObj(StockService stockServiceObj) {
        this.stockServiceObj = stockServiceObj;
    }

    public void setStockMovementServiceObj(StockMovementService stockMovementServiceObj) {
        this.stockMovementServiceObj = stockMovementServiceObj;
    }

    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    public void setStockMovementDAO(StockMovementDAO stockMovementDAO) {
        this.stockMovementDAO = stockMovementDAO;
    }
    
    @Override
    public void saveOrUpdateObj(Object object) throws ServiceException {
        saveOrUpdate(object);
    }
    
    @Override
    public void deleteLocationBatchDocumentMappingByBatchMapId(String batchMapId) throws ServiceException {
        String fetchAllSerialsQry = "DELETE from locationbatchdocumentmapping WHERE batchmapid = ?";
        int deleted= executeSQLUpdate( fetchAllSerialsQry, new Object[]{batchMapId});
    }
    
    @Override
    public KwlReturnObject getProductOpeningQtyBatchDetail(Product product) throws ServiceException{
        KwlReturnObject result = new KwlReturnObject(false, "Error occurred", null, null, 0);
        List params=new ArrayList();
        String hqlQry = " FROM LocationBatchDocumentMapping  lbm  WHERE lbm.documentid= ? AND lbm.batchmapid IS NOT NULL ";
        params.add(product.getID());
        
        List ftchList = executeQuery( hqlQry, params.toArray());
        result = new KwlReturnObject(true, "Product Opening warehouse,location fetched successfully.", null, ftchList, ftchList.size());
        return result;
    }
    
    public NewProductBatch getNewProductBatchById(String id) throws ServiceException{
        NewProductBatch npb=null;
        List params=new ArrayList();
        String hqlQry = " FROM NewProductBatch  npb  WHERE npb.id= ? ";
        params.add(id);
        
        List ftchList = executeQuery( hqlQry, params.toArray());
        if(ftchList != null && !ftchList.isEmpty()){
            npb=(NewProductBatch)ftchList.get(0);
        }
        return npb;
    }
    
    public KwlReturnObject deleteProductBatchSerialDetails(Map<String, Object> requestParams) throws ServiceException {
        String delQuery = "", delQuery1 = "", delQuery2 = "", delQuerypb = "", delQueryBs = "";
        int numtotal = 0, numRows5 = 0;
        List list = new ArrayList();
        double quantity=0;
        String serialmapids = "", docids = "",serialstringids="";
        String batchmapids = "";
        String warehouse=(String)requestParams.get("warehouse");
        String location=(String)requestParams.get("location");
        String batchName=(String)requestParams.get("batch");
        docids=(String)requestParams.get("productid");
        docids="'"+docids+"'";
        String conditionSQL="";
         ArrayList params14 = new ArrayList();
        if (!StringUtil.isNullOrEmpty(docids)) {
            conditionSQL=" and npb.product in (" + docids + ") ";
            if(!StringUtil.isNullOrEmpty(warehouse)){
                conditionSQL += " and npb.warehouse = ? ";
                params14.add(warehouse);
            }
            if(!StringUtil.isNullOrEmpty(location)){
                conditionSQL += " and npb.location = ? ";
                params14.add(location);
            }
            if(!StringUtil.isNullOrEmpty(batchName)){
                conditionSQL += " and npb.batchname = ? ";
                params14.add(batchName);
            }
            
            boolean locatiobatchMapping= false;
            String myquery4 = " select lbm.batchmapid,lbm.quantity,lbm.id  from locationbatchdocumentmapping lbm inner join newproductbatch npb "
                    + "on npb.id=lbm.batchmapid where lbm.documentid in (" + docids + ") "+conditionSQL;
            String myquery5 = " select sdm.serialid,sdm.id from serialdocumentmapping sdm inner join newbatchserial nbs on sdm.serialid=nbs.id "
                    + "inner join newproductbatch npb on npb.id=nbs.batch where documentid in (" + docids + ") "+conditionSQL;
            List list4 = executeSQLQuery( myquery4, params14.toArray());
            Iterator itr4 = list4.iterator();
            while (itr4.hasNext()) {
                Object[] objArr = (Object[]) itr4.next();
                batchmapids += "'" + objArr[0] + "',";
                quantity += (Double) objArr[1];
                locatiobatchMapping=true;
                LocationBatchDocumentMapping locationBatchDocumentMapping = (LocationBatchDocumentMapping) get(LocationBatchDocumentMapping.class, (String) objArr[2]);
                HashMap<String, Object> batchUpdateQtyMap = new HashMap<String, Object>();
                if (locationBatchDocumentMapping != null) {
                    batchUpdateQtyMap.put("qty", -locationBatchDocumentMapping.getQuantity());
                    batchUpdateQtyMap.put("quantity", -locationBatchDocumentMapping.getQuantity());
                }
                batchUpdateQtyMap.put("id", locationBatchDocumentMapping.getBatchmapid().getId());
                saveBatchAmountDue(batchUpdateQtyMap);
            }
             if (!StringUtil.isNullOrEmpty(batchmapids)) {
                batchmapids = batchmapids.substring(0, batchmapids.length() - 1);
            }
            List list5 = executeSQLQuery( myquery5, params14.toArray());
            Iterator itr5 = list5.iterator();
            while (itr5.hasNext()) {
                Object[] objArr = (Object[]) itr5.next();
                serialmapids += "'" + objArr[0] + "',";
            }
            if (!StringUtil.isNullOrEmpty(serialmapids)) {
                serialstringids = serialmapids.substring(0, serialmapids.length() - 1);
            }
            String serialDocumentMappingId = getSerialDocumentIds(list5);
            if (!StringUtil.isNullOrEmpty(serialDocumentMappingId)) {
                serialDocumentMappingId = serialDocumentMappingId.substring(0, serialDocumentMappingId.length() - 1);
                ArrayList params1 = new ArrayList();
                delQuery1 = "delete  from serialcustomdata where serialdocumentmappingid in (" + serialDocumentMappingId + ")";
                int numRows1 = executeSQLUpdate( delQuery1, params1.toArray());
            }

        }
        ArrayList params15 = new ArrayList();
        delQuerypb = "delete from locationbatchdocumentmapping where id in ( select * from ( select lbm.id from locationbatchdocumentmapping lbm "
                + " inner join newproductbatch npb on npb.id=lbm.batchmapid where lbm.documentid in (" + docids + ") "+ conditionSQL + " ) as t)";
        int numRows = executeSQLUpdate( delQuerypb, params14.toArray());

        delQuerypb = "delete from serialdocumentmapping where id in ( select * from ( select sdm.id from serialdocumentmapping sdm "
                + "inner join newbatchserial nbs on sdm.serialid=nbs.id inner join newproductbatch npb on npb.id=nbs.batch "
                + "where documentid in (" + docids + ") "+conditionSQL+ " ) as t)";
        numRows = executeSQLUpdate( delQuerypb, params14.toArray());

//        if (!StringUtil.isNullOrEmpty(batchmapids)) {
//            params15 = new ArrayList();
//            delQuerypb = "delete  from newproductbatch where id in (" + batchmapids + ") ";
//            int numRows8 = executeSQLUpdate( delQuerypb, params15.toArray());
//
//        }

        if (!StringUtil.isNullOrEmpty(serialmapids)) {
            ArrayList paramsSerial = new ArrayList();
            delQueryBs = " delete from newbatchserial where id in(" + serialstringids + ") ";
            int numRowsSerial = executeSQLUpdate( delQueryBs, paramsSerial.toArray());
        }
        list.add(quantity);
        return new KwlReturnObject(true, "Product Batch Details has been deleted successfully.", null, list, list.size());
    }
     public void saveBatchAmountDue(HashMap<String, Object> productbatchMap) throws ServiceException {
        try {
            NewProductBatch productBatch = new NewProductBatch();
            String itemID = (String) productbatchMap.get("id");
            if (productbatchMap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                productBatch = (NewProductBatch) get(NewProductBatch.class, itemID);
                Double itemQty = (Double) productbatchMap.get("qty");
                if (productbatchMap.containsKey("quantity") && productbatchMap.get("quantity") != null) {
                    Double quantity = (Double) productbatchMap.get("quantity");
                    productBatch.setQuantity(productBatch.getQuantity() + quantity);
                }
                productBatch.setQuantitydue(authHandler.roundQuantity((productBatch.getQuantitydue() + itemQty),productBatch.getCompany().getCompanyID()));

            }
             //If we are updating for same batch no need to delete
            if(productBatch.getQuantity()==0 && productBatch.getQuantitydue()==0 && productBatch.getId().equalsIgnoreCase(itemID)){
                delete(productBatch);
            }else{
                saveOrUpdate(productBatch);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCommonTablesDAOImpl.saveBatchAmountDue : " + ex.getMessage(), ex);
        }
    }
     public String getSerialDocumentIds(List list) {
        String serialDocument = "";
        String serialDocumentMappingId = "";
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objArr = (Object[]) itr.next();
            for (int i = 0; i < objArr.length; i++) {
                SerialDocumentMapping serialDocumentMapping = (SerialDocumentMapping) get(SerialDocumentMapping.class, (String) objArr[i]);
                if (serialDocumentMapping != null) {
                    serialDocument = serialDocumentMapping.getId().toString();
                    serialDocumentMappingId += "'" + serialDocument + "',";

                }
            }

        }
        return serialDocumentMappingId;
    }
    public KwlReturnObject getProductOpeningQtyFromBatchSerial(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        double quantity=0;
        String  docids = "";
        docids=(String)requestParams.get("productid");
        docids="'"+docids+"'";
        if (!StringUtil.isNullOrEmpty(docids)) {
            ArrayList params14 = new ArrayList();
            String myquery4 = " select sum(quantity) from LocationBatchDocumentMapping where documentid in (" + docids + ") ";
            List listResult = executeQuery( myquery4, params14.toArray());
            if (listResult!= null && listResult.get(0) != null) {
                quantity += (double) listResult.get(0);
            }
            
           list.add(quantity);
        }
        return new KwlReturnObject(true, "Product Batch Details has been deleted successfully.", null, list, list.size());
    } 
    @Override
    public void deleteStockAndSMForProduct(Product product) throws ServiceException {
        List par1 = new ArrayList();
        String fetchStockQry = " FROM Stock WHERE product = ? ";
        par1.add(product);
        List ftchStkList = executeQuery( fetchStockQry, par1.toArray());
        if (ftchStkList != null && !ftchStkList.isEmpty()) {

            String deleteSMDQry = "FROM StockMovementDetail smd  WHERE smd.stockMovement.product = ? ";
            List ftchSMDList = executeQuery( deleteSMDQry, new Object[]{product});

            String deleteSMQry = " FROM StockMovement sm  WHERE sm.product = ? ";
            List ftchSMList = executeQuery( deleteSMQry, new Object[]{product});

            Iterator it1 = ftchSMDList.iterator();
            while (it1.hasNext()) {
                StockMovementDetail smd = (StockMovementDetail) it1.next();
                stockMovementDAO.delete(smd);
            }
            it1 = ftchSMList.iterator();
            while (it1.hasNext()) {
                StockMovement smm = (StockMovement) it1.next();
                stockMovementDAO.delete(smm);
            }
            it1 = ftchStkList.iterator();
            while (it1.hasNext()) {
                Stock stk = (Stock) it1.next();
                stockMovementDAO.delete(stk);
            }

        }
    }
    
    @Override
    public boolean isProductUsedInTransaction(String companyId,String productId) throws ServiceException{
        
        boolean isUsed=false;
        String fetchInvIdQry = "SELECT product from in_stockadjustment  where product=? AND company=?"
                            + " UNION SELECT product from in_interstoretransfer where product=? AND company=?"
                            + " UNION SELECT product from in_goodsrequest where product=? AND company=?"
//                            + " UNION SELECT product from  podetails where product=? AND company=?"
                            + " UNION SELECT product from sodetails where product=? AND company=? and lockquantity > 0 " //if quantity is locked in SO then we can't allow to update intial quantity
                            + " UNION SELECT product from grodetails where product=? AND company=?"
                            + " UNION SELECT product from  dodetails where product=? AND company=?"
                            + " UNION SELECT product from srdetails where product=? AND company=?"
                            + " UNION SELECT product from prdetails where product=? AND company=?"
//                            + " UNION SELECT product from  vendorquotationdetails where product=? AND company=?"
                            + " UNION SELECT inventory.product FROM	grdetails INNER JOIN inventory ON grdetails.id = inventory.id WHERE inventory.product =? AND grdetails.company=?"
                            + " UNION SELECT aproduct from pbdetails where aproduct=? ";
                    List prm = new ArrayList();

                    prm.add(productId);
                    prm.add(companyId);
                    prm.add(productId);
                    prm.add(companyId);
                    prm.add(productId);
                    prm.add(companyId);
                    prm.add(productId);
                    prm.add(companyId);
                    prm.add(productId);
                    prm.add(companyId);
                    prm.add(productId);
                    prm.add(companyId);
                    prm.add(productId);
                    prm.add(companyId);
                    prm.add(productId);
                    prm.add(companyId);
                    prm.add(productId);
                    prm.add(companyId);
                    prm.add(productId);

                    List ftchList = executeSQLQuery( fetchInvIdQry, prm.toArray());
                    if (ftchList != null && !ftchList.isEmpty()) {
                        isUsed=true;
                    }
                    return isUsed;
    }
    
    @Override
    public KwlReturnObject addStockInventorySide(KwlReturnObject newInvObj, JSONObject inventoryjson, String productDefaultWarehouseID, String productDefaultLocationID, double prodInitPurchasePrice) throws ServiceException {
        KwlReturnObject result = new KwlReturnObject(false, "Error occurred", null, null, 0);
        List list = new ArrayList();
        String invId = null;

        list = newInvObj.getEntityList();
        if (list != null && !list.isEmpty()) {
            Inventory inv = (Inventory) list.get(0);
            invId = inv.getID();
        }
        try {

            String productId = inventoryjson.optString("productid",null);
            String companyId = inventoryjson.optString("companyid",null);
            String stockUoMId = inventoryjson.optString("uomid",null);
            Company company = null;
            Product product = null;
            Store store = null;
            Location location = null;
            UnitOfMeasure stockUoM = null;
            double quantity = 0;

            if (!StringUtil.isNullOrEmpty(companyId)) {
                company = (Company) get(Company.class, companyId);
            }
            if (!StringUtil.isNullOrEmpty(productId)) {
                product = (Product) get(Product.class, productId);
            }
            if (!StringUtil.isNullOrEmpty(productDefaultWarehouseID)) {
                store = (Store) get(Store.class, productDefaultWarehouseID);
            }
            if (!StringUtil.isNullOrEmpty(productDefaultLocationID)) {
                location = (Location)get(Location.class, productDefaultLocationID);
            }
            if (!StringUtil.isNullOrEmpty(stockUoMId)) {
                stockUoM = (UnitOfMeasure)  get(UnitOfMeasure.class, stockUoMId);
            }
            quantity = inventoryjson.getDouble("baseuomquantity");

            if (!StringUtil.isNullOrEmpty(invId) && product != null && store != null && location != null) {

                stockServiceObj.increaseInventory(product, store, location, "", null, quantity);

                if (quantity > 0) {
                    // Query to add stock movement entry if qty > 0

                    StockMovement sm = new StockMovement();

                    sm.setCompany(company);
                    sm.setModuleRefId(productId);
                    sm.setModuleRefDetailId(productId);
                    sm.setProduct(product);
                    sm.setPricePerUnit(prodInitPurchasePrice);
                    sm.setQuantity(quantity);
                    sm.setRemark("Opening stock added through Import");
                    sm.setStockUoM(stockUoM);
                    sm.setStore(store);
                    sm.setTransactionDate(new Date());
                    sm.setCreatedOn(new Date());
                    sm.setTransactionModule(TransactionModule.ERP_PRODUCT);
                    sm.setTransactionNo(null);
                    sm.setTransactionType(TransactionType.IN);

                    StockMovementDetail smd = new StockMovementDetail();
                    smd.setBatchName("");
                    smd.setLocation(location);
                    smd.setQuantity(quantity);
                    smd.setSerialNames(null);
                    smd.setStockMovement(sm);
                    sm.getStockMovementDetails().add(smd);

                    stockMovementDAO.saveOrUpdate(sm);

                }
                result = new KwlReturnObject(true, "Stock Added Inventory Side.", null, list, list.size());
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;

    }

    @Override
    public KwlReturnObject updateStockInventorySide(KwlReturnObject updatedInvObj, JSONObject inventoryjson, LocationBatchDocumentMapping lbm, double prodInitPurchasePrice) throws ServiceException, AccountingException {
        KwlReturnObject result = new KwlReturnObject(false, "Error occurred", null, null, 0);
        List list = new ArrayList();
        List<StockMovement> smList = null;
        StockMovement sm = null;
        Stock stock = null;
        String batchWarehouseId=lbm.getBatchmapid().getWarehouse().getId();
        String batchLocationId=lbm.getBatchmapid().getLocation().getId();
        double batchQty=lbm.getQuantity();

        try {

            list = updatedInvObj.getEntityList();
            if (list != null && !list.isEmpty()) {
                Inventory inv = (Inventory) list.get(0);
                if (inv != null) {

                    String companyId = inv.getCompany().getCompanyID();
                    String productId = inv.getProduct().getID();

                    Company company = null;
                    Product product = null;
                    Store store = null;
                    Location location = null;
                    double quantity = 0;
                    
                    String batchName="";
                    String serialNames=null;
                    StoreMaster row= lbm.getBatchmapid().getRow();
                    StoreMaster rack= lbm.getBatchmapid().getRack();
                    StoreMaster bin= lbm.getBatchmapid().getBin();
                    

                    if (!StringUtil.isNullOrEmpty(companyId)) {
                        company = (Company) get(Company.class, companyId);
                    }
                    if (!StringUtil.isNullOrEmpty(productId)) {
                        product = (Product) get(Product.class, productId);
                    }
                    if (!StringUtil.isNullOrEmpty(batchWarehouseId)) {
                        store = (Store) get(Store.class, batchWarehouseId);
                    }
                    if (!StringUtil.isNullOrEmpty(batchLocationId)) {
                        location = (Location) get(Location.class, batchLocationId);
                    }

                    Date updateDate = new Date();
                    if (inventoryjson.has("updatedate")) {
                        updateDate = inventoryjson.get("updatedate") != null ? (Date) inventoryjson.get("updatedate") : updateDate;
                    }
//                    if (inventoryjson.has("baseuomquantity")) {
//                        quantity = inventoryjson.getDouble("baseuomquantity");
//                    }
                    quantity = batchQty;
                    boolean isProductUsedInTransaction=isProductUsedInTransaction(companyId,product.getID());
                    
                    if (isProductUsedInTransaction) {
                        throw new AccountingException("Product is already used in transaction. So can't update.");
                    }
                    
                    if(product.isIsSerialForProduct() && !product.isIsBatchForProduct()){
                        batchName="";
                        String fetchAllSerialsQry = "select GROUP_CONCAT(s.serialname) FROM newbatchserial s  WHERE s.batch= ? GROUP BY s.batch";
                        List ftchSrls = executeSQLQuery( fetchAllSerialsQry, new Object[]{lbm.getBatchmapid()});
                        if(ftchSrls !=null && !ftchSrls.isEmpty()){
                            serialNames=(String)ftchSrls.get(0);
                        }
                    }else if(!product.isIsSerialForProduct() && product.isIsBatchForProduct()){
                        serialNames=null;
                        String fetchAllBtchQry = "select batchname FROM NewProductBatch b  WHERE b.id= ? ";
                        List ftchBtchs = executeQuery( fetchAllBtchQry, new Object[]{lbm.getBatchmapid().getId()});
                        if(ftchBtchs !=null && !ftchBtchs.isEmpty()){
                            batchName=(String)ftchBtchs.get(0);
                        }
                    }else if(product.isIsSerialForProduct() && product.isIsBatchForProduct()){
                        String fetchAllSerialsQry = "select GROUP_CONCAT(s.serialname),b.batchname FROM newbatchserial s INNER JOIN newproductbatch b ON b.id=s.batch WHERE s.batch= ? ";
                        List ftchSrls = executeSQLQuery( fetchAllSerialsQry, new Object[]{lbm.getBatchmapid()});
                        if(ftchSrls !=null && !ftchSrls.isEmpty()){
                            Object[] res=(Object[])ftchSrls.get(0);
                            serialNames=(String)res[0];
                            batchName=(String)res[1];
                        }
                    }else{
                        batchName="";
                        serialNames=null;
                    }
                    
                   
                    stock = stockServiceObj.getStock(product, store, location,row, rack, bin, batchName);
                    smList = stockMovementServiceObj.getStockMovementListByReferenceId(company, productId);
                    if (!smList.isEmpty()) {
                        sm = smList.get(0); // here 0 static because in case of opening there will be only one entry
                    }

                    if (stock == null) {
                        //this is for case suppose product is imported with initial qty 0 and then it is imported(updated) with quantity 7,then make entry in in_stock and sm 
//                        throw new AccountingException("Stock is not found on inventory side.");

                        stockServiceObj.increaseInventory(product, store, location, row, rack, bin, batchName, serialNames, quantity);

                        if (quantity > 0) {
                            // Query to add stock movement entry if qty > 0

                            StockMovement stMvt = new StockMovement();
                            Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();

                            stMvt.setCompany(company);
                            stMvt.setModuleRefId(productId);
                            stMvt.setModuleRefDetailId(productId);
                            stMvt.setProduct(product);
                            stMvt.setPricePerUnit(prodInitPurchasePrice);
                            stMvt.setQuantity(quantity);
                            stMvt.setRemark("Opening stock added through Import");
                            stMvt.setStockUoM(inv.getUom());
                            stMvt.setStore(store);
                            stMvt.setTransactionDate(updateDate);
                            stMvt.setCreatedOn(new Date());
                            stMvt.setTransactionModule(TransactionModule.ERP_PRODUCT);
                            stMvt.setTransactionNo(null);
                            stMvt.setTransactionType(TransactionType.OPENING);

                            StockMovementDetail smd = new StockMovementDetail();
                            smd.setBatchName(batchName);
                            smd.setLocation(location);
                            smd.setQuantity(quantity);
                            smd.setSerialNames(serialNames);
                            smd.setBin(bin);
                            smd.setRack(rack);
                            smd.setRow(row);
                            smd.setStockMovement(stMvt);
                            smdSet.add(smd);
                            stMvt.setStockMovementDetails(smdSet);

                            stockMovementDAO.saveOrUpdate(stMvt);
                        }

                    } else {
                        stock.setQuantity(authHandler.roundQuantity(quantity,companyId));
                        stock.setPricePerUnit(prodInitPurchasePrice);
                        stock.setModifiedOn(new Date());
                        stockMovementDAO.saveOrUpdate(stock);

                        if (quantity != 0) {
                            // Query to update stock movement entry  if qty > 0

                            if (sm != null) {
                                //this case is for : eg. suppose prev qty = 5 and now qty=7,ie update SM entry 
                                sm.setQuantity(quantity);
                                sm.setPricePerUnit(prodInitPurchasePrice);
//                                sm.setTransactionDate(new Date());
//                                sm.setCreatedOn(new Date());
                                if (sm.getStockMovementDetails().size() == 1) {
                                    for (StockMovementDetail smd  : sm.getStockMovementDetails()) {
                                        smd.setQuantity(quantity);
                                    }
                                    stockMovementDAO.saveOrUpdate(sm);
                                }

                            } else {
                                //this case is for : eg. suppose prev qty = 0 and now qty=7,ie no SM entry previously as qty=0 ,so insert now in SM 
                                StockMovement stkMvt = new StockMovement();
                                Set<StockMovementDetail> smdSet = new HashSet<StockMovementDetail>();

                                stkMvt.setCompany(company);
                                stkMvt.setModuleRefId(productId);
                                stkMvt.setModuleRefDetailId(productId);
                                stkMvt.setProduct(product);
                                stkMvt.setPricePerUnit(prodInitPurchasePrice);
                                stkMvt.setQuantity(quantity);
                                stkMvt.setRemark("Opening stock added through Import");
                                stkMvt.setStockUoM(inv.getUom());
                                stkMvt.setStore(store);
                                stkMvt.setTransactionDate(new Date());
                                stkMvt.setCreatedOn(new Date());
                                stkMvt.setTransactionModule(TransactionModule.ERP_PRODUCT);
                                stkMvt.setTransactionNo(null);
                                stkMvt.setTransactionType(TransactionType.OPENING);

                                StockMovementDetail smd = new StockMovementDetail();
                                smd.setBatchName(batchName);
                                smd.setLocation(location);
                                smd.setQuantity(quantity);
                                smd.setSerialNames(serialNames);
                                smd.setBin(bin);
                                smd.setRack(rack);
                                smd.setRow(row);
                                smd.setStockMovement(stkMvt);
                                smdSet.add(smd);
                                stkMvt.setStockMovementDetails(smdSet);

                                stockMovementDAO.saveOrUpdate(stkMvt);
                            }

                        } else {
                            // Query to delete stock movement entry  if qty = 0 and product not used in any transaction
                            //this case is for : eg. suppose prev qty = 5 and now qty=0 and not used in transaction ,ie delete entry from SM as now qty = 0
                            stockMovementDAO.delete(sm);
                        }
                    }

                    result = new KwlReturnObject(true, "Stock updated Inventory Side.", null, list, list.size());
                }

            }

        } catch (DataAccessException | ServiceException | HibernateException | JSONException ex) {
            System.out.println("Error : " + ex.getMessage());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return result;
    }

}