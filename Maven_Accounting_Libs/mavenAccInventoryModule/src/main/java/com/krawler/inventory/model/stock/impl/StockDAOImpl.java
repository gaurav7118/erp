/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stock.impl;

import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Producttype;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.inspection.InspectionArea;
import com.krawler.inventory.model.inspection.InspectionForm;
import com.krawler.inventory.model.inspection.InspectionFormDetails;
import com.krawler.inventory.model.ist.ISTDetail;
import com.krawler.inventory.model.ist.InterStoreTransferStatus;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stock.Stock;
import com.krawler.inventory.model.stock.StockDAO;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockmovement.TransactionType;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.DateFormatter;
import org.apache.commons.collections.ListUtils;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Vipin Gupta
 */
public class StockDAOImpl extends BaseDAO implements StockDAO {

    @Override
    public Stock getStockById(String stockId) {
        return (Stock) get(Stock.class, stockId);
    }

    @Override
    public void delete(Object object) throws ServiceException {
        super.delete(object);
    }

    @Override
    public void saveOrUpdate(Object object) throws ServiceException {
        super.saveOrUpdate(object);
    }

    @Override
    public double getProductTotalQuantityInStore(Product product, Store store) throws ServiceException {
        double quantity = 0;
        StringBuilder hql = new StringBuilder("SELECT SUM(s.quantity) FROM Stock s WHERE s.company = ? AND s.product = ? AND s.store = ?   GROUP BY s.product, s.store ");
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product);
        params.add(store);
        List list = executeQuery( hql.toString(), params.toArray());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object objs = (Object) itr.next();
            quantity = objs != null ? (Double) objs : 0;
        }
        return authHandler.roundQuantity(quantity,product.getCompany().getCompanyID());
    }

    @Override
    public double getProductTotalQuantity(Product product) throws ServiceException {
        double quantity = 0;
        StringBuilder hql = new StringBuilder("SELECT SUM(s.quantity) FROM Stock s WHERE s.company = ? AND s.product = ?   GROUP BY s.product ");
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product);
        List list = executeQuery( hql.toString(), params.toArray());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object objs = (Object) itr.next();
            quantity = objs != null ? (Double) objs : 0;
        }
        return authHandler.roundQuantity(quantity,product.getCompany().getCompanyID());
    }

    @Override
    public double getProductQuantityUnderParticularStore(Product product, String storeId, boolean isRepair) throws ServiceException {
        double quantity = 0;
        //consignment Qa module
        StringBuilder hql = new StringBuilder("SELECT SUM(cd.quantity) FROM Consignment s join s.consignmentApprovalDetails cd WHERE s.company = ? AND s.product = ?");
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product);
        if (isRepair) {
            hql.append(" And cd.repairStatus in(?,?) ");
            params.add(ApprovalStatus.REPAIRPENDING);
            params.add(ApprovalStatus.RETURNTOREPAIR);
        } else {
            hql.append(" And cd.approvalStatus=? ");
            params.add(ApprovalStatus.PENDING);
        }
        if (!StringUtil.isNullOrEmpty(storeId)) {
            hql.append(" AND s.store.id=? ");
            params.add(storeId);
        }
        List list = executeQuery( hql.toString(), params.toArray());
        if (list.get(0) != null) {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object objs = (Object) itr.next();
                quantity += objs != null ? (Double) objs : 0;
            }
        }
        //stock adjustment QA module
        StringBuilder hql1 = new StringBuilder("SELECT SUM(saad.quantity) FROM SAApproval saa join saa.stockAdjustment sa join saa.SADetailApprovalSet saad WHERE sa.company = ? AND sa.product = ?");
        if (isRepair) {
            hql1.append(" And saad.repairStatus in(?,?) ");

        } else {
            hql1.append(" And saad.approvalStatus=? ");

        }
        if (!StringUtil.isNullOrEmpty(storeId)) {
            hql1.append(" AND sa.store.id=? ");

        }


        List list1 = executeQuery( hql1.toString(), params.toArray());
        if (list1.get(0) != null) {
            Iterator itr1 = list1.iterator();
            while (itr1.hasNext()) {
                Object objs = (Object) itr1.next();
                quantity += objs != null ? (Double) objs : 0;
            }
        }
        //interstore QA module
        StringBuilder sql = new StringBuilder("SELECT SUM(stdad.quantity) FROM in_stocktransfer_approval  stad INNER JOIN  in_interstoretransfer ist on(stad.stocktransferid = ist.id) INNER JOIN in_stocktransfer_detail_approval stdad ON(stdad.stocktransfer_approval=stad.id) WHERE ist.company=? AND ist.product=? AND stad.transaction_module=?");
        List params1 = new ArrayList();
        params1.add(product.getCompany().getCompanyID());
        params1.add(product.getID());
        params1.add(2);
        if (isRepair) {
            sql.append(" AND stdad.repair_status in(?,?)");
            params1.add(4);
            params1.add(7);
        } else {
            sql.append(" AND stdad.approval_status=?");
            params1.add(0);
        }
        if (!StringUtil.isNullOrEmpty(storeId)) {
            sql.append(" AND ist.tostore=? ");
            params1.add(storeId);
        }
        List list2 = executeSQLQuery( sql.toString(), params1.toArray());
        if (list2.get(0) != null) {
            Iterator itr2 = list2.iterator();
            while (itr2.hasNext()) {
                Object objs = (Object) itr2.next();
                quantity += objs != null ? (Double) objs : 0;
            }
        }
        return authHandler.roundQuantity(quantity,product.getCompany().getCompanyID());
    }

    @Override
    public double getProductQuantityInStoreLocation(Product product, Store store, Location location) throws ServiceException {
        double quantity = 0;
        String groupbyCond = "";
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product);
        params.add(store);
        StringBuilder hql = new StringBuilder(" SELECT SUM(s.quantity) FROM Stock s WHERE s.company = ? AND s.product = ? AND s.store = ? ");
        
        if (location != null) {
            hql.append(" AND s.location = ? ");
            groupbyCond = " , s.location ";
            params.add(location);
        }
        hql.append(" GROUP BY s.product, s.store "+groupbyCond);
        List list = executeQuery(hql.toString(), params.toArray());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object objs = (Object) itr.next();
            quantity = objs != null ? (Double) objs : 0;
        }
        return authHandler.roundQuantity(quantity,product.getCompany().getCompanyID());
    }

    @Override
    public Stock getStock(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName) throws ServiceException {
        return getStock(product.getID(), store.getId(), location.getId(), row !=null ? row.getId() : null, rack != null ? rack.getId(): null, bin != null? bin.getId(): null, batchName);
    }
    @Override
    public Stock getStock(String productId, String storeId, String locationId, String rowId, String rackId, String binId, String batchName) throws ServiceException{

        StringBuilder hql = new StringBuilder("FROM Stock WHERE product.ID = ? AND store.id = ? AND location.id = ? ");
        List params = new ArrayList();
        params.add(productId);
        params.add(storeId);
        params.add(locationId);

        if (!StringUtil.isNullOrEmpty(rowId)) {
            hql.append(" AND row.id = ? ");
            params.add(rowId);
        } else {
            hql.append(" AND row IS NULL ");
        }
        if (!StringUtil.isNullOrEmpty(rackId)) {
            hql.append(" AND rack.id = ? ");
            params.add(rackId);
        } else {
            hql.append(" AND rack IS NULL ");
        }
        if (!StringUtil.isNullOrEmpty(binId)) {
            hql.append(" AND bin.id = ? ");
            params.add(binId);
        } else {
            hql.append(" AND bin IS NULL ");
        }

        if (!StringUtil.isNullOrEmpty(batchName)) {
            hql.append(" AND batchName = ? ");
            params.add(batchName);
        } else {
            hql.append(" AND (batchName = ? OR batchName IS NULL )");
            params.add("");
        }

        List list = executeQuery( hql.toString(), params.toArray());

        Stock stock = null;
        if (!list.isEmpty()) {
            stock = (Stock) list.get(0);
        }

        return stock;
    }

    @Override
    public List<Stock> getBatchWiseStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM Stock s WHERE s.company = ?  AND quantity <> 0");
        List params = new ArrayList();
        params.add(company);
        Store store = null;
//        if (store != null) {
//            hql.append(" AND s.store = ? ");
//            params.add(store);
//        }

        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND s.store.id IN ( ");
            for (Store str : storeSet) {
                if (storeSet.size() == 1) {
                    store = str;
                }
                if (first) {
                    storeIn.append("'").append(str.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(str.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql.append(storeIn);
        }
        hql.append(" ORDER BY s.product.productid,s.store ");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging(hql.toString(), params.toArray(), paging);
        }
        return list;
    }
            
    @Override
    public List<Stock> getBatchWiseStockList(Company company, Store store, Location location, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM Stock s WHERE s.company = :c AND s.store = :s AND quantity <> 0");
        Map params = new HashMap();
        params.put("c", company);
        params.put("s", store);
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.product.productid LIKE :ss OR s.product.name LIKE :ss OR s.batchName LIKE :ss OR s.serialNames LIKE :ss ) ");
//            params.add("%" + searchString + "%");
//            params.add("%" + searchString + "%");
//            params.add("%" + searchString + "%");
            params.put("ss","%" + searchString + "%");
        }
        if (location != null) {
            hql.append(" AND s.location = :loc");
//            params.add(location);
            params.put("loc",location);
        }else if(store!=null){
            if(store != null && !store.getLocationSet().isEmpty()){
                Set<Location> locationSet=store.getLocationSet();
//                String locationStr="";
//                boolean isFirst=true;
//                for(Location loc : locationSet){
//                    if(isFirst){
//                        locationStr +="'"+loc.getId()+"'";
//                        isFirst=false;
//                    }else{
//                        locationStr +=",'"+loc.getId()+"'";
//                    }
//                }
                 hql.append(" AND s.location IN ( :loc )");
//                 hql.append(locationStr);
//                 hql.append(" ) ");
                 params.put("loc", locationSet);
            }
        }
        List list;
        if (paging != null) {
            int totalCount = 0;
            if(paging.isValid()){
                String countQuery = "SELECT COUNT(*) " + hql.toString().substring(hql.indexOf("FROM"));
                list = executeCollectionQuery(countQuery, params);
                if(!list.isEmpty()){
                    totalCount = ((Long)list.get(0)).intValue();
                }
                
                hql.append(" ORDER BY s.product.productid, s.store.abbreviation ");
                list = executeCollectionQuery(hql.toString(), params, paging);
            }else{
                hql.append(" ORDER BY s.product.productid, s.store.abbreviation ");
                list = executeCollectionQuery(hql.toString(), params);
                totalCount = list.size();
            }
            paging.setTotalRecord(totalCount);
        } else {
            hql.append(" ORDER BY s.product.productid, s.store.abbreviation ");
            list = executeCollectionQuery(hql.toString(), params);
        }
//        hql.append(" ORDER BY s.product.productid ");
//        List list = HibernateUtil.executeQuery(hibernateTemplate, hql.toString(), params.toArray());
//        int totalCount = list.size();
//        if (paging != null) {
//            paging.setTotalRecord(totalCount);
//            list = HibernateUtil.executeQueryPaging(hibernateTemplate, hql.toString(), params.toArray(), paging);
//        }
        return list;
    }

    @Override
    public List<Stock> getStoreWiseStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging, String inventoryCatType) throws ServiceException {
        
        StringBuilder hql = new StringBuilder("SELECT s.product, s.store, SUM(s.quantity), AVG(s.pricePerUnit) FROM Stock s WHERE s.company = :c AND s.quantity <> 0");
        Map params = new HashMap();
        params.put("c", company);
//        List params = new ArrayList();
//        params.add(company);

        if (storeSet != null && !storeSet.isEmpty()) {
//            boolean first = true;
//            StringBuilder storeIn = new StringBuilder(" AND s.store.id IN ( ");
//            for (Store store : storeSet) {
//                if (first) {
//                    storeIn.append("'").append(store.getId()).append("'");
//                    first = false;
//                } else {
//                    storeIn.append(",").append("'").append(store.getId()).append("'");
//                }
//            }
//            storeIn.append(")");
//            hql.append(storeIn);
            hql.append(" AND s.store IN ( :s )");
            params.put("s", storeSet);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.product.productid LIKE :ss OR s.product.name LIKE :ss  OR s.serialNames LIKE :ss) ");
            params.put("ss", "%" + searchString + "%");
//            params.add("%" + searchString + "%");
//            params.add("%" + searchString + "%");
//            params.add("%" + searchString + "%");
        }
        if (!StringUtil.isNullOrEmpty(inventoryCatType) && !inventoryCatType.equalsIgnoreCase("All")) {
            hql.append(" AND s.product.producttype.ID = :catid");
            params.put("catid", inventoryCatType);
//            params.add(inventoryCatType);
        }
        if (location != null) {
            hql.append(" AND s.location = :loc");
//            params.add(location);
            params.put("loc", location);
            hql.append(" GROUP BY s.product,s.store,s.location ");
        } else if (storeSet != null && storeSet.size() == 1) {
            hql.append(" GROUP BY s.store, s.product ");
        } else {
            hql.append(" GROUP BY s.product ");
        }
        
        List list;
        if (paging != null) {
            int totalCount = 0;
            if(paging.isValid()){
                String countQuery = "SELECT 1 " + hql.toString().substring(hql.indexOf("FROM"));
                list = executeCollectionQuery(countQuery, params);
                totalCount = list.size();
                
                hql.append(" ORDER BY s.product.productid ");
                list = executeCollectionQuery(hql.toString(), params, paging);
            }else{
                hql.append(" ORDER BY s.product.productid ");
                list = executeCollectionQuery(hql.toString(), params);
                totalCount = list.size();
            }
            paging.setTotalRecord(totalCount);
        } else {
            hql.append(" ORDER BY s.product.productid ");
            list = executeCollectionQuery(hql.toString(), params);
        }
        List<Stock> stocks = new ArrayList<>();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            Stock stock = new Stock();
            stock.setProduct((Product) objs[0]);
            stock.setStore((Store) objs[1]);
            stock.setLocation(location);
            stock.setQuantity(authHandler.roundQuantity((Double) objs[2],company.getCompanyID()));
            stock.setPricePerUnit((Double) objs[3]);

            stocks.add(stock);
        }
        return stocks;
    }
    @Override
    public List<Object[]> getStoreWiseProductStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging,String inventoryCatType) throws ServiceException {
        
        int quantityDigit = Constants.QUANTITY_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(company.getCompanyID())) {
            quantityDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(company.getCompanyID()).get(Constants.quantitydecimalforcompany);
        }
        StringBuilder hql = new StringBuilder("SELECT s.product.ID, s.product.productid, s.product.name, s.product.description, uom.name, s.product.itemReusability, SUM(s.quantity), s.product.isSerialForProduct,s.product.isBatchForProduct FROM Stock s ");
        hql.append(" LEFT JOIN s.product.unitOfMeasure uom ");
        hql.append(" WHERE s.company = :c AND FORMAT(s.quantity,"+quantityDigit+") <> 0 AND s.product.deleted='F' ");
        
        Map params = new HashMap();
        params.put("c", company);

        if (storeSet != null && !storeSet.isEmpty()) {
            hql.append(" AND s.store IN ( :s )");
            params.put("s", storeSet);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.product.productid LIKE :ss OR s.product.name LIKE :ss  OR s.serialNames LIKE :ss) ");
            params.put("ss", "%" + searchString + "%");
        }
        if (!StringUtil.isNullOrEmpty(inventoryCatType) && !inventoryCatType.equalsIgnoreCase("All")) {
            hql.append(" AND s.product.producttype.ID = :catid");
            params.put("catid", inventoryCatType);
        }else {
            //Only Loading assembly, Inventory and Inventory Non Sale Items in Report.
            hql.append(" AND s.product.producttype.ID IN (:assembly,:inventory,:nonsale ) ");
            params.put("assembly", Producttype.ASSEMBLY);
            params.put("inventory", Producttype.INVENTORY_PART);
            params.put("nonsale", Producttype.Inventory_Non_Sales);
        }
        if (location != null) {
            hql.append(" AND s.location = :loc");
            params.put("loc", location);
            hql.append(" GROUP BY s.product,s.store,s.location ");
        } else if (storeSet.size() == 1) {
            hql.append(" GROUP BY s.store, s.product ");
        } else {
            hql.append(" GROUP BY s.product ");
        }
        
        List list;
        if (paging != null) {
            int totalCount = 0;
            if(paging.isValid()){
                String countQuery = "SELECT 1 " + hql.toString().substring(hql.indexOf("FROM"));
                list = executeCollectionQuery(countQuery, params);
                totalCount = list.size();
                
                hql.append(" ORDER BY s.product.productid ");
                list = executeCollectionQuery(hql.toString(), params, paging);
            }else{
                hql.append(" ORDER BY s.product.productid ");
                list = executeCollectionQuery(hql.toString(), params);
                totalCount = list.size();
            }
            paging.setTotalRecord(totalCount);
        } else {
            hql.append(" ORDER BY s.product.productid ");
            list = executeCollectionQuery(hql.toString(), params);
        }
        
        return list;
    }
    
  
    public List<Object[]> getWarehouseWiseProductStockList1(Company company, Set<InventoryWarehouse> storeSet, InventoryLocation location, String searchString, Paging paging,String inventoryCatType) throws ServiceException {
        
        StringBuilder hql = new StringBuilder("SELECT s.product.ID, s.product.productid, s.product.name, s.product.description, uom.name, "
                + " s.product.itemReusability, SUM(s.quantitydue), s.product.isSerialForProduct,s.product.isBatchForProduct FROM NewProductBatch s ");
        hql.append(" LEFT JOIN s.product.unitOfMeasure uom ");
        hql.append(" WHERE s.company = :c AND s.quantitydue <> 0 AND s.product.deleted='F' ");
        
        Map params = new HashMap();
        params.put("c", company);

        if (storeSet != null && !storeSet.isEmpty()) {
            hql.append(" AND s.warehouse IN ( :s )");
            params.put("s", storeSet);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.product.productid LIKE :ss OR s.product.name LIKE :ss ) ");
            params.put("ss", "%" + searchString + "%");
        }
        if (!StringUtil.isNullOrEmpty(inventoryCatType) && !inventoryCatType.equalsIgnoreCase("All")) {
            hql.append(" AND s.product.producttype.ID = :catid");
            params.put("catid", inventoryCatType);
        }else {
            //Only Loading assembly, Inventory and Inventory Non Sale Items in Report.
            hql.append(" AND s.product.producttype.ID IN (:assembly,:inventory,:nonsale ) ");
            params.put("assembly", Producttype.ASSEMBLY);
            params.put("inventory", Producttype.INVENTORY_PART);
            params.put("nonsale", Producttype.Inventory_Non_Sales);
        }
        if (location != null) {
            hql.append(" AND s.location = :loc");
            params.put("loc", location);
            hql.append(" GROUP BY s.product,s.store,s.location ");
        } else if (storeSet.size() == 1) {
            hql.append(" GROUP BY s.warehouse, s.product ");
        } else {
            hql.append(" GROUP BY s.product ");
        }
        
        List list;
        if (paging != null) {
            int totalCount = 0;
            if(paging.isValid()){
                String countQuery = "SELECT 1 " + hql.toString().substring(hql.indexOf("FROM"));
                list = executeCollectionQuery(countQuery, params);
                totalCount = list.size();
                
                hql.append(" ORDER BY s.product.productid ");
                list = executeCollectionQuery(hql.toString(), params, paging);
            }else{
                hql.append(" ORDER BY s.product.productid ");
                list = executeCollectionQuery(hql.toString(), params);
                totalCount = list.size();
            }
            paging.setTotalRecord(totalCount);
        } else {
            hql.append(" ORDER BY s.product.productid ");
            list = executeCollectionQuery(hql.toString(), params);
        }
        
        return list;
    }
     @Override
    public List<Object[]> getWarehouseWiseProductStockList(Company company, Set<InventoryWarehouse> storeSet, InventoryLocation location, String searchString, Paging paging, String inventoryCatType) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT p.id,p.productid,p.`name`,p.description,u.`name`,p.itemreusability,SUM(nb.quantitydue),p.isSerialForProduct,p.isBatchForProduct FROM newproductbatch nb "
                + " left JOIN product p ON nb.product=p.id AND p.company=nb.company "
                + " LEFT JOIN uom u ON u.id=p.unitOfMeasure"
                + " LEFT JOIN producttype pt ON p.producttype=pt.id"
                + " WHERE nb.company=? AND nb.quantitydue<>0 and p.deleteflag='F' ");
        List params = new ArrayList();
        params.add(company.getCompanyID());
        
        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder typeIn = new StringBuilder(" AND nb.warehouse IN ( ");

            for (InventoryWarehouse st : storeSet) {
                if (first) {
//                    typeIn.append(st.getId());
                    typeIn.append("'").append(st.getId()).append("'");
                    first = false;
                } else {
//                    typeIn.append(",").append(st.getId());
                    typeIn.append(",'").append(st.getId()).append("'");
                }
            }
            typeIn.append(")");
            hql.append(typeIn);
        }

        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (p.productid LIKE ? OR p.name LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        if (!StringUtil.isNullOrEmpty(inventoryCatType) && !inventoryCatType.equalsIgnoreCase("All")) {
            hql.append(" AND pt.id = ? ");
            params.add(inventoryCatType);
        }else {
            //Only Loading assembly, Inventory and Inventory Non Sale Items in Report.
            hql.append(" AND pt.id IN ('"+Producttype.ASSEMBLY+"','"+Producttype.INVENTORY_PART+"','"+Producttype.Inventory_Non_Sales+"' ) ");
             
        }
        if (location != null) {
            hql.append(" AND nb.location = ? ");
            params.add(location.getId());
            hql.append(" GROUP BY nb.product,nb.warehouse,nb.location ");
        } else if (storeSet.size() == 1) {
            hql.append(" GROUP BY nb.warehouse, nb.product ");
        } else {
            hql.append(" GROUP BY nb.product ");
        }
        hql.append(" ORDER BY nb.product ");

        List list = executeSQLQueryPaging(hql.toString(), params.toArray(),paging);
        
        return list;
    }

    @Override
    public List<Object[]> getStoreWiseProductStockSummaryList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging,String inventoryCatType) throws ServiceException {
        
        StringBuilder hql = new StringBuilder("SELECT s.product.ID, SUM(s.quantity), s.store.id FROM Stock s ");
        hql.append(" LEFT JOIN s.product.unitOfMeasure uom ");
        hql.append(" WHERE s.company = :c AND s.quantity <> 0 AND s.product.deleted='F' ");
        
        Map params = new HashMap();
        params.put("c", company);

        if (storeSet != null && !storeSet.isEmpty()) {
            hql.append(" AND s.store IN ( :s )");
            params.put("s", storeSet);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.product.productid LIKE :ss OR s.product.name LIKE :ss  OR s.serialNames LIKE :ss) ");
            params.put("ss", "%" + searchString + "%");
        }
        if (!StringUtil.isNullOrEmpty(inventoryCatType) && !inventoryCatType.equalsIgnoreCase("All")) {
            hql.append(" AND s.product.producttype.ID = :catid");
            params.put("catid", inventoryCatType);
        }else {
            //Only Loading assembly, Inventory and Inventory Non Sale Items in Report.
            hql.append(" AND s.product.producttype.ID IN (:assembly,:inventory,:nonsale ) ");
            params.put("assembly", Producttype.ASSEMBLY);
            params.put("inventory", Producttype.INVENTORY_PART);
            params.put("nonsale", Producttype.Inventory_Non_Sales);
        }
        if (storeSet.size() == 1) {
            hql.append(" GROUP BY s.store, s.product ");
        } else {
            hql.append(" GROUP BY s.product,s.store ");
        }
        
        List list;
        hql.append(" ORDER BY s.product.productid ");
        list = executeCollectionQuery(hql.toString(), params);
        return list;
    }
    
    @Override
    public List<Object[]> getStoreWiseDetailedStockList(Company company, Set<Store> storeSet, Location location, String searchString, Paging paging,String productId) throws ServiceException {
        int quantityDigit = Constants.QUANTITY_DIGIT_AFTER_DECIMAL;
        if (Constants.CompanyPreferencePrecisionMap.containsKey(company.getCompanyID())) {
            quantityDigit = (Integer) Constants.CompanyPreferencePrecisionMap.get(company.getCompanyID()).get(Constants.quantitydecimalforcompany);
        }
        StringBuilder hql = new StringBuilder("SELECT s.product.ID, s.product.productid, s.product.name, s.product.description, uom.name, "
                + " s.product.itemReusability, s.store.abbreviation, s.store.description, s.location.name, rw.name, rk.name, "
                + " bn.name, s.batchName, s.serialNames, s.quantity,s.store.id,s.location.id FROM Stock s ");
        hql.append(" LEFT JOIN s.product.unitOfMeasure uom ");
        hql.append(" LEFT JOIN s.row rw ");
        hql.append(" LEFT JOIN s.rack rk ");
        hql.append(" LEFT JOIN s.bin bn ");
        hql.append(" WHERE s.company = :c AND  FORMAT(quantity,"+quantityDigit+") <> 0 AND s.product.deleted='F' ");
        Map params = new HashMap();
        params.put("c", company);
        if(storeSet != null && !storeSet.isEmpty()){
            hql.append(" AND s.store IN (:s) ");
            params.put("s", storeSet);
        }
        if (location != null) {
            hql.append(" AND s.location = :loc");
//            params.add(location);
            params.put("loc",location);
        }
//        params.put("s", storeSet);
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (s.product.productid LIKE :ss OR s.product.name LIKE :ss OR s.batchName LIKE :ss OR s.serialNames LIKE :ss ) ");
//            params.add("%" + searchString + "%");
//            params.add("%" + searchString + "%");
//            params.add("%" + searchString + "%");
            params.put("ss","%" + searchString + "%");
        }
        if (!StringUtil.isNullOrEmpty(productId)) {
            hql.append(" AND (s.product.ID = :pid) ");
            params.put("pid",productId);
        }
//       else{
//            if(store != null && !store.getLocationSet().isEmpty()){
//                Set<Location> locationSet=store.getLocationSet();
////                String locationStr="";
////                boolean isFirst=true;
////                for(Location loc : locationSet){
////                    if(isFirst){
////                        locationStr +="'"+loc.getId()+"'";
////                        isFirst=false;
////                    }else{
////                        locationStr +=",'"+loc.getId()+"'";
////                    }
////                }
//                 hql.append(" AND s.location IN ( :loc )");
////                 hql.append(locationStr);
////                 hql.append(" ) ");
//                 params.put("loc", locationSet);
//            }
//        }
        List list;
        if (paging != null) {
            int totalCount = 0;
            if(paging.isValid()){
                String countQuery = "SELECT COUNT(s.id) " + hql.toString().substring(hql.indexOf("FROM"));
                list = executeCollectionQuery(countQuery, params);
                if(!list.isEmpty()){
                    totalCount = ((Long)list.get(0)).intValue();
                }
                
                hql.append(" ORDER BY s.product.productid, s.store.abbreviation ");
                list = executeCollectionQuery(hql.toString(), params, paging);
            }else{
                hql.append(" ORDER BY s.product.productid, s.store.abbreviation ");
                list = executeCollectionQuery(hql.toString(), params);
                totalCount = list.size();
            }
            paging.setTotalRecord(totalCount);
        } else {
            hql.append(" ORDER BY s.product.productid, s.store.abbreviation ");
            list = executeCollectionQuery(hql.toString(), params);
        }
        return list;
    }
        
    @Override
    public List<NewBatchSerial> getProductSerialList(String productid, String companyid, Set<Store> storeSet, String locationId, String storeId, String searchString, Paging paging) throws ServiceException {
        String hql = " FROM NewBatchSerial WHERE company.companyID = ? AND product = ? AND isconsignment='F' AND quantitydue > 0 ";//AND warehouse.id = ? AND quantitydue > 0

        List params = new ArrayList();
        params.add(companyid);
        params.add(productid);

        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND batch.warehouse.id IN ( ");
            for (Store store : storeSet) {
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql += storeIn;
        }

        if (!StringUtil.isNullOrEmpty(locationId)) {
            hql += " AND batch.location.id = ? ";
            params.add(locationId);
        }

        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql += " AND (serialname LIKE ? OR batch.batchname LIKE ?) ";
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql += " ORDER BY batch.batchname";

        List list = executeQuery( hql, params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }

        return list;
    }

    public void updateObject(Object object) throws ServiceException {
        List list = new ArrayList();
        try {
            if (object != null) {
                saveOrUpdate(object);
                list.add(object);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("UpdateObject : " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<NewProductBatch> getERPActiveBatchList(Product product, Store store, Location location) throws ServiceException {
        String hql = " FROM NewProductBatch WHERE company = ? AND product = ? AND warehouse.id = ? AND quantitydue > 0";

        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product.getID());
        params.add(store.getId());
        
        if (location != null) {
            hql += " AND location.id = ? ORDER BY batchname";
            params.add(location.getId());
        } else {
//            if (!product.isIsBatchForProduct() && !product.isIsSerialForProduct()) {
                hql += " GROUP BY batchname,warehouse,location ";
//            }
            if (product.isIsbinforproduct()) {
                hql += ",bin ";
            }
            if (product.isIsrowforproduct()) {
                hql += ",row ";
            }
            if (product.isIsrackforproduct()) {
                hql += ",rack ";
            }
            hql += " ORDER BY location.name ";
        }

        List list = executeQuery( hql, params.toArray());

        return list;
    }
    
     @Override
    public List<Object[]> getERPBatchListWithExpiryDate(Product product, Store store) throws ServiceException {
        String hql = "SELECT b.id, b.location.id, CASE WHEN b.row.id IS NULL THEN '' ELSE b.row.id END , "
                + " CASE WHEN b.rack.id IS NULL THEN '' ELSE b.rack.id END ,  CASE WHEN b.bin.id IS NULL THEN '' ELSE b.bin.id END ,  "
                + " b.batchname , b.expdate FROM NewProductBatch b WHERE b.company = ? AND b.product = ? AND b.warehouse.id = ? AND b.expdate IS NOT NULL ";
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product.getID());
        params.add(store.getId());
        if (!product.isIsBatchForProduct() && !product.isIsSerialForProduct()) {
            hql += " GROUP BY batchname,warehouse,location ";
        }
        hql += " ORDER BY location.name ";
        List list = executeQuery( hql, params.toArray());
        return list;
    }
    
    @Override
    public List<NewBatchSerial> getERPActiveSerialList(Product product, NewProductBatch productBatch, boolean checkQAReject) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM NewBatchSerial WHERE company = ? AND product = ? AND batch = ? AND (quantitydue-lockquantity) = 1");
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product.getID());
        params.add(productBatch);
        if (checkQAReject) {
            hql.append(" AND (qaApprovalstatus IS NULL)");
        } else {
            hql.append(" AND (qaApprovalstatus <> ? OR  qaApprovalstatus IS NULL)");
            params.add(QaApprovalStatus.PENDING);
        }

        hql.append(" ORDER BY serialname");
        List list = executeQuery( hql.toString(), params.toArray());

        return list;
    }

    @Override
    public List<String> getProductBatchWiseSerialList(Company company, Product product, String batchName) throws ServiceException {
        List<String> serialList = new ArrayList<String>();

        String hql = " SELECT S.serialname FROM NewBatchSerial  AS S   "
                + " WHERE S.company.companyID= ?  AND S.product = ? AND  S.batch.batchname = ? AND (quantitydue-lockquantity) = 1";

        List params = new ArrayList();
        params.add(company.getCompanyID());
        params.add(product.getID());
        params.add(batchName);

        List list = executeQuery( hql, params.toArray());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            String serial = (String) itr.next();
            serialList.add(serial);
        }
        return serialList;
    }

     public Map<String, Double> getAvailableQuantity(Company company, boolean includeLocation) throws ServiceException {
         return getAvailableQuantity(company, includeLocation,null,true);
     }
    @Override
    public Map<String, Double> getAvailableQuantity(Company company, boolean includeLocation,StringBuilder productIds,boolean isExport) throws ServiceException {
        Map<String, Double> map = new HashMap<String, Double>();
        StringBuilder hql = new StringBuilder("SELECT s.store, s.product,s.location, SUM(s.quantity) FROM Stock s WHERE s.company = ? ");
        List params = new ArrayList();
        params.add(company);
        
        if (!StringUtil.isNullObject(productIds)&&!isExport) {
            hql.append(productIds);
        }
        hql.append(" GROUP BY s.product, s.store ");
        if (includeLocation) {
            hql.append(", s.location ");
        }

      
        List list = executeQuery(hql.toString(), params.toArray());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            if (objs[0] != null && objs[1] != null && objs[2] != null) {
                String key = "";
                if (includeLocation) {
                    key = ((Store) objs[0]).getId().concat(((Product) objs[1]).getID().toString().concat(((Location) objs[2]).getId().toString()));
//                    key.concat(((Location) objs[2]).getId().toString());
                } else {
                    key = ((Store) objs[0]).getId().concat(((Product) objs[1]).getID().toString());
                }
                map.put(key, authHandler.roundQuantity((Double) objs[3],company.getCompanyID()));
            }
        }
        return map;
    }

    @Override
    public List<Stock> getStockByStoreProduct(Company company, Product product, Store store) throws ServiceException {
        List<Stock> stocks = new ArrayList<Stock>();
        StringBuilder hql = new StringBuilder("SELECT s.id , s.location, SUM(s.quantity) FROM Stock s WHERE s.company = ? AND s.product = ? AND s.store = ? AND s.quantity <> 0  GROUP BY s.product, s.store, s.location ");
        List params = new ArrayList();
        params.add(company);
        params.add(product);
        params.add(store);
        List list = executeQuery( hql.toString(), params.toArray());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            Stock stock = new Stock();
            stock.setCompany(company);
            stock.setProduct(product);
            stock.setId((String) objs[0]);
            stock.setLocation((Location) objs[1]);
            stock.setQuantity((Double) objs[2]);
            stocks.add(stock);
        }
        return stocks;
    }

    @Override
    public NewProductBatch getERPProductBatch(String productId, String storeId, String locationId, String rowId, String rackId, String binId, String batchName) throws ServiceException{
        if (StringUtil.isNullOrEmpty(batchName)) {
            batchName = "";
        }
        StringBuilder hql = new StringBuilder("FROM NewProductBatch WHERE product = ? AND warehouse.id = ? AND location.id = ? ");
        List params = new ArrayList();
        params.add(productId);
        params.add(storeId);
        params.add(locationId);
//        params.add(batchName);

        if (!StringUtil.isNullOrEmpty(batchName)) {
            hql.append(" AND batchname = ? ");
            params.add(batchName);
        }else{
            hql.append(" AND (batchname = '' OR batchname IS NULL )");
        } 
        if (!StringUtil.isNullOrEmpty(rowId)) {
            hql.append(" AND row.id = ? ");
            params.add(rowId);
        } else {
            hql.append(" AND row IS NULL ");
        }
        if (!StringUtil.isNullOrEmpty(rackId)) {
            hql.append(" AND rack.id = ? ");
            params.add(rackId);
        } else {
            hql.append(" AND rack IS NULL ");
        }
        if (!StringUtil.isNullOrEmpty(binId)) {
            hql.append(" AND bin.id = ? ");
            params.add(binId);
        } else {
            hql.append(" AND bin IS NULL ");
        }
        List list = executeQueryPaging( hql.toString(), params.toArray(), new Paging(0, 1));
        NewProductBatch productBatch = null;
        if (!list.isEmpty()) {
            productBatch = (NewProductBatch) list.get(0);
        }
        return productBatch;
    }
    
        @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public NewProductBatch getERPProductBatch(String product, String store, String location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName,String companyId) throws ServiceException {
        if (StringUtil.isNullOrEmpty(batchName)) {
            batchName = "";
        }
        NewProductBatch productBatch = null;
        StringBuilder hql = new StringBuilder("FROM NewProductBatch WHERE company.companyID = ? AND product = ? AND warehouse.id = ? AND location.id = ? ");
        List params = new ArrayList();
        params.add(companyId);
        params.add(product);
        params.add(store);
        params.add(location);
//        params.add(batchName);
        
        if (!StringUtil.isNullOrEmpty(batchName)) {
            hql.append(" AND batchname = ? ");
            params.add(batchName);
        } else {
            hql.append(" AND (batchname = '' OR batchname IS NULL )");
        }
        if (row != null) {
            hql.append(" AND row = ? ");
            params.add(row);
        } else {
            hql.append(" AND row IS NULL ");
        }
        if (rack != null) {
            hql.append(" AND rack = ? ");
            params.add(rack);
        } else {
            hql.append(" AND rack IS NULL ");
        }
        if (bin != null) {
            hql.append(" AND bin = ? ");
            params.add(bin);
        } else {
            hql.append(" AND bin IS NULL ");
        }
        List list = executeQueryPaging(hql.toString(), params.toArray(), new Paging(0, 1));
        if (!list.isEmpty()) {
            productBatch = (NewProductBatch) list.get(0);
        }
        return productBatch;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public NewProductBatch getERPProductBatch(Product product, Store store, Location location, StoreMaster row, StoreMaster rack, StoreMaster bin, String batchName) throws ServiceException {
        return getERPProductBatch(product.getID(), store.getId(), location.getId(),(row != null ?  row.getId() :null ) , (rack != null ?  rack.getId() : null ),(bin != null ?  bin.getId() : null ), batchName);
    }
 @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public NewBatchSerial getERPBatchSerial(String product, NewProductBatch productBatch, String serialName,String companyId) throws ServiceException {

        NewBatchSerial serial = null;
        String hql = "FROM NewBatchSerial WHERE company.companyID = ? AND product = ? AND batch = ? AND serialname = ? and quantitydue=1";
        List params = new ArrayList();
        params.add(companyId);
        params.add(product);
        params.add(productBatch);
        params.add(serialName);
        List list = executeQueryPaging( hql, params.toArray(), new Paging(0, 1));
        if (!list.isEmpty()) {
            serial = (NewBatchSerial) list.get(0);
        }
        return serial;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public NewBatchSerial getERPBatchSerial(Product product, NewProductBatch productBatch, String serialName) throws ServiceException {

        NewBatchSerial serial = null;
        String hql = "FROM NewBatchSerial WHERE company = ? AND product = ? AND batch = ? AND serialname = ?";
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product.getID());
        params.add(productBatch);
        params.add(serialName);
        List list = executeQueryPaging( hql, params.toArray(), new Paging(0, 1));
        if (!list.isEmpty()) {
            serial = (NewBatchSerial) list.get(0);
        }
        return serial;
    }
    
    @Override
    public List getERPSerialFromBatch(String companyid, String productid, String warehouse, String location, String row, String rack, String bin, String batchName, String serialName) throws ServiceException {
        String serialQuery = null;
        if(!StringUtil.isNullOrEmpty(serialName) && serialName.contains(",")){
            String srl[] = serialName.split(",");
            serialQuery="(";
            StringBuilder sb = new StringBuilder(serialQuery);
            for (String serial : srl) {
                sb.append(" nbs.serialname = '").append(serial).append("' OR ");                
            }            
            serialQuery = sb.substring(0,sb.lastIndexOf(" OR "));            
            serialQuery +=")";
        }
        else{
            serialQuery = "nbs.serialname = '"+serialName+"'";
        }
        String sql = "select nbs.id FROM newbatchserial nbs inner join newproductbatch npb on nbs.batch=npb.id and npb.location=? and npb.warehouse=? and npb.product=nbs.product"
                + " WHERE nbs.company = ? AND nbs.product = ? AND "+serialQuery ;
        List params = new ArrayList();
        params.add(location);
        params.add(warehouse);
        params.add(companyid);
        params.add(productid);                
        if(!StringUtil.isNullOrEmpty(row)){
            sql +=" and npb.row=? ";        
            params.add(row);
        }else{
            sql +=" and npb.row is null ";
        }
        if(!StringUtil.isNullOrEmpty(rack)){
            sql +=" and npb.rack =? ";
            params.add(rack);
        }
        else{
            sql +=" and npb.rack is null ";
        }
        if(!StringUtil.isNullOrEmpty(bin)){
            sql +=" and npb.bin =? ";
            params.add(bin);
        }
        else{
            sql +=" and npb.bin is null ";
        }
        if(!StringUtil.isNullOrEmpty(batchName)){
            sql +=" and npb.batchname =? ";
            params.add(batchName);
        }
        else{
            sql +=" and (npb.batchname is null or npb.batchname = '')";
        }
        sql+=" Group By nbs.serialname,nbs.batch";
                
        List list = executeSQLQuery( sql, params.toArray());
        return list;
    }

    public boolean isSerialExists(Product product, String batchName, String serialName) throws ServiceException {
        return isSerialExists(product, batchName, serialName, null, null);
    }
    @Override
    public boolean isSerialExists(Product product, String batchName, String serialName,String module,String documentId) throws ServiceException {
        boolean exists = false;
        String sqlquery = "";
        List list1 = null;
        String hql = "FROM NewBatchSerial WHERE company = ? AND product = ? AND batch.batchname = ? AND serialname = ? AND  quantitydue = 1 AND batch.quantitydue > 0  "; //AND quantitydue = 1 AND batch.quantitydue > 0  
        if (!StringUtil.isNullOrEmpty(module) && Integer.parseInt(module) == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
            hql = "FROM NewBatchSerial WHERE company = ? AND product = ? AND batch.batchname = ? AND serialname = ? AND batch.location IS NOT NULL AND quantitydue = 1 AND batch.quantitydue > 0  "; //AND quantitydue = 1 AND batch.quantitydue > 0  
        }
//        String hql = "FROM NewBatchSerial WHERE company = ? AND product = ? AND batch.batchname = ? AND serialname = ? AND batch.location IS NOT NULL AND quantitydue = 1 AND batch.quantitydue > 0  "; //AND quantitydue = 1 AND batch.quantitydue > 0  
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product.getID());
        params.add(batchName);
        params.add(serialName);
        List list = executeQueryPaging( hql, params.toArray(), new Paging(0, 1));
        if(!StringUtil.isNullOrEmpty(documentId)){ // edit case
            List params1 = new ArrayList();
            params1.add(product.getCompany().getCompanyID());
            params1.add(product.getID());
            params1.add(batchName);
            params1.add(serialName);
            params1.add(documentId);
            sqlquery = "select sd.id from serialdocumentmapping sd "
                    + " inner join newbatchserial nb on sd.serialid=nb.id "
                    + " inner join newproductbatch np on nb.batch = np.id "
                    + " where nb.company = ? and nb.product = ? and np.batchname = ? and nb.serialname = ? "
                    + " and sd.documentid = ? and nb.quantitydue = 1 and np.quantitydue > 0";
            list1 = executeSQLQuery(sqlquery, params1.toArray());
        }        
        if (!list.isEmpty()) {
            exists = true;
            if (list1 != null && !list1.isEmpty()) {
                exists = false;
            }
        }        
        return exists;
    }
    
    @Override
    public boolean isSKUExists(Product product, String batchName, String skuName) throws ServiceException {
        boolean exists = false;
        String hql = "FROM NewBatchSerial WHERE company = ?  AND skufield = ? order by quantitydue desc ";
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(skuName);
        
        List list = executeQuery( hql, params.toArray());
        if (!list.isEmpty()) {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                NewBatchSerial sr=(NewBatchSerial) itr.next();
                 if(sr.getQuantitydue()==1){
                     exists=true;
                     return exists;
                 }else if(sr.getQuantitydue()==0){
                    exists= isSKUExistsinTransait(sr.getProduct(),skuName,sr.getSerialname(),sr.getCompany());
                    if(exists){
                        break;
                    }
                 }
            }
//            exists = true;
        }
        return exists;
    }
     public boolean isSKUExistsinTransait(String product, String skuName, String serialName, Company companyId) throws ServiceException {
        boolean exists = false;
        List params = new ArrayList();
        String hql = "select istd  FROM InterStoreTransferRequest ist "
                + " left join ist.istDetails istd WHERE ist.company = ?  AND (ist.status = ? or ist.status=?) and "
                + " istd.issuedSerialNames LIKE ? and ist.product.ID=? ";
        params.add(companyId);
        params.add(InterStoreTransferStatus.INTRANSIT);
        params.add(InterStoreTransferStatus.RETURNED);
        params.add("%" + serialName + "%");
        params.add(product);
        List list = executeQuery(hql, params.toArray());
        if (!list.isEmpty()) {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                ISTDetail iSTD = (ISTDetail) itr.next();
                String issuedSr = iSTD.getIssuedSerialNames();
                List<String> serList = Arrays.asList(iSTD.getIssuedSerialNames().split(","));
                List<String> delserList = !StringUtil.isNullOrEmpty(iSTD.getDeliveredSerialNames()) ? Arrays.asList(iSTD.getDeliveredSerialNames().split(",")) : null;
                if (delserList != null) {
                    List resultList = ListUtils.subtract(serList, delserList);
//                    serList.removeAll(delserList);
                    if (resultList.contains(serialName)) {
                        exists = true;
                        break;
                    }
                } else if (serList.contains(serialName) && (delserList==null || delserList.size() == 0)) {
                    exists = true;
                    break;
                }
            }
        }
        return exists;
    }

    @Override
    public boolean isAnyStockLiesInStore(Company company, Boolean isQAOrRepair) throws ServiceException {
        boolean exists = false;
        StringBuilder hql = new StringBuilder("FROM Stock s WHERE s.company = ? ");
        List params = new ArrayList();
        String query = "";

        //Consignment Request Approval
        query = "SELECT count(*) as cnt from in_consignment c "
                + "  INNER JOIN in_consignmentdetails stad ON c.id=stad.consignment "
                + "  WHERE c.company=? ";

        params.add(company.getCompanyID());
        if (isQAOrRepair != null && isQAOrRepair) {
            query += " AND stad.approval_status = ?";
            params.add(ApprovalStatus.PENDING.ordinal());
        } else if (isQAOrRepair != null && isQAOrRepair == false) {
            query += " AND stad.repair_status IN (?,?)";
            params.add(ApprovalStatus.REPAIRPENDING.ordinal());
            params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());

        } else {
            query += " AND (stad.approval_status = ? OR stad.repair_status IN (?,?))";
            params.add(ApprovalStatus.PENDING.ordinal());
            params.add(ApprovalStatus.REPAIRPENDING.ordinal());
            params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());
        }



        //Stock Request Approval
        query += " UNION SELECT count(*) as cnt FROM in_stocktransfer_approval sta "
                + " INNER JOIN in_stocktransfer_detail_approval stad ON stad.stocktransfer_approval = sta.id "
                + " INNER JOIN in_goodsrequest gr ON sta.stocktransferid = gr.id AND gr.company = ? "
                + " INNER JOIN product p ON p.id = gr.product ";



        params.add(company.getCompanyID());
        if (isQAOrRepair != null && isQAOrRepair) {
            query += " WHERE stad.approval_status = ?";
            params.add(ApprovalStatus.PENDING.ordinal());
        } else if (isQAOrRepair != null && isQAOrRepair == false) {
            query += " WHERE stad.repair_status IN (?,?)";
            params.add(ApprovalStatus.REPAIRPENDING.ordinal());
            params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());

        } else {
            query += " WHERE (stad.approval_status = ? OR stad.repair_status IN (?,?))";
            params.add(ApprovalStatus.PENDING.ordinal());
            params.add(ApprovalStatus.REPAIRPENDING.ordinal());
            params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());
        }


        //IST Request Approval
        query += " UNION SELECT count(*) as cnt FROM in_stocktransfer_approval sta "
                + " INNER JOIN in_stocktransfer_detail_approval stad ON stad.stocktransfer_approval = sta.id "
                + " INNER JOIN in_interstoretransfer gr ON sta.stocktransferid = gr.id AND gr.company = ? "
                + " INNER JOIN product p ON p.id = gr.product ";

        params.add(company.getCompanyID());
        if (isQAOrRepair != null && isQAOrRepair) {
            query += " WHERE stad.approval_status = ?";
            params.add(ApprovalStatus.PENDING.ordinal());
        } else if (isQAOrRepair != null && isQAOrRepair == false) {
            query += " WHERE stad.repair_status IN (?,?)";
            params.add(ApprovalStatus.REPAIRPENDING.ordinal());
            params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());

        } else {
            query += " WHERE (stad.approval_status = ? OR stad.repair_status IN (?,?))";
            params.add(ApprovalStatus.PENDING.ordinal());
            params.add(ApprovalStatus.REPAIRPENDING.ordinal());
            params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());
        }

        //Stock Adjustment Approval
        query += " UNION SELECT count(*) as cnt  FROM in_sa_approval sta "
                + " INNER JOIN in_sa_detail_approval stad ON stad.sa_approval = sta.id "
                + " INNER JOIN in_stockadjustment gr ON sta.stock_adjustment = gr.id AND gr.company = ? "
                + " INNER JOIN product p ON p.id = gr.product ";

        params.add(company.getCompanyID());
        if (isQAOrRepair != null && isQAOrRepair) {
            query += " WHERE stad.approval_status = ?";
            params.add(ApprovalStatus.PENDING.ordinal());
        } else if (isQAOrRepair != null && isQAOrRepair == false) {
            query += " WHERE stad.repair_status IN (?,?)";
            params.add(ApprovalStatus.REPAIRPENDING.ordinal());
            params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());

        } else {
            query += " WHERE (stad.approval_status = ? OR stad.repair_status IN (?,?))";
            params.add(ApprovalStatus.PENDING.ordinal());
            params.add(ApprovalStatus.REPAIRPENDING.ordinal());
            params.add(ApprovalStatus.RETURNTOREPAIR.ordinal());
        }
        String finalQuery = "SELECT SUM(tbl.cnt) FROM ( " + query + " )AS tbl ";
        List list = executeSQLQuery( finalQuery, params.toArray());
        if (!list.isEmpty()) {
            String count = list.get(0).toString();
            if (Integer.parseInt(count) > 0) {
                exists = true;
            }
        }
        return exists;
    }

    @Override
    public NewProductBatch getBatchDataByProductBatchName(Product product, String batchName) throws ServiceException {
        if (StringUtil.isNullOrEmpty(batchName)) {
            batchName = "";
        }
        NewProductBatch productBatch = null;
        String hql = "FROM NewProductBatch WHERE company = ? AND product = ?  AND batchname = ? AND expdate IS NOT NULL ";
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product.getID());
        params.add(batchName);
        List list = executeQueryPaging( hql, params.toArray(), new Paging(0, 1));
        if (!list.isEmpty()) {
            productBatch = (NewProductBatch) list.get(0);
        }
        return productBatch;
    }

    @Override
    public NewBatchSerial getSerialDataBySerialName(Product product, NewProductBatch productBatch, String serialName) throws ServiceException {

        NewBatchSerial serial = null;
        String hql = "FROM NewBatchSerial WHERE company = ? AND product = ? AND batch = ? AND serialname = ? AND exptodate IS NOT NULL ";
         if (product.isIsSKUForProduct()) {
            hql += " AND skufield IS NOT NULL AND skufield<>'' ";
        }
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product.getID());
        params.add(productBatch);
        params.add(serialName);
        List list = executeQueryPaging( hql, params.toArray(), new Paging(0, 1));
        if (!list.isEmpty()) {
            serial = (NewBatchSerial) list.get(0);
        }
        return serial;
    }
    @Override
    public NewBatchSerial getSerialDataBySerialName(Product product, String serialName) throws ServiceException {

        NewBatchSerial serial = null;
        String hql = "FROM NewBatchSerial WHERE company = ? AND product = ? AND serialname = ?  ";
         if (product.isIsSKUForProduct()) {
            hql += " AND skufield IS NOT NULL AND skufield<>'' ";
        }
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product.getID());
        params.add(serialName);
        List list = executeQueryPaging(hql, params.toArray(), new Paging(0, 1));
        if (!list.isEmpty()) {
            serial = (NewBatchSerial) list.get(0);
        }
        return serial;
    }
    @Override
    public NewBatchSerial getSkuBySerialName(String product, String serialName,String company) throws ServiceException {

        NewBatchSerial serial = null;
        String hql = "FROM NewBatchSerial WHERE company.companyID = ? AND product = ? AND serialname = ?  AND skufield IS NOT NULL AND skufield<>'' group by skufield";
        List params = new ArrayList();
        params.add(company);
        params.add(product);
        params.add(serialName);
       // List list = executeQueryPaging(hql, params.toArray(), new Paging(0, 1));
        List list = executeQuery( hql, params.toArray());
        if (!list.isEmpty()) {
            serial = (NewBatchSerial) list.get(0);
        }
        return serial;
    }

    @Override
    public List<Stock> getBatchSerialListByStoreProductLocation(Product product, Set<Store> storeSet, Location location, Paging paging) throws ServiceException {
        return getBatchSerialListByStoreProductLocation(product.getID(), storeSet, location, paging,null,false);
    }
    @Override
    public List<Stock> getBatchSerialListByStoreProductLocation(String productId, Set<Store> storeSet, Location location, Paging paging,String batchName,boolean  isEdit) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM Stock WHERE product.id = ?   ");
        if(!isEdit){
            hql = new StringBuilder("FROM Stock WHERE product.id = ?  and quantity <>0 ");
        }
        List params = new ArrayList();
        params.add(productId);
        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND store.id IN ( ");
            for (Store store : storeSet) {
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql.append(storeIn);
        }
        if (location != null) {
            hql.append(" AND location = ? ");
            params.add(location);
        }
        if (!StringUtil.isNullOrEmpty(batchName)) {
            hql.append(" AND batchName LIKE ? ");
            params.add("%" + batchName + "%");
        }
        hql.append(" ORDER BY store.abbreviation ASC, location.name ASC, batchName ASC");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            if (paging.isValid() && totalCount > paging.getLimit()) {
                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            }
        }
        return list;
    }

    @Override
    public Map<String, NewProductBatch> getBatchListByStoreProductLocation(Product product, Set<Store> storeSet, Location location, Paging paging,boolean isEdit) throws ServiceException {
        Map<String, NewProductBatch> mpList = new HashMap<String, NewProductBatch>();
        /*
        * Removed lock quantity check from query SDP-9822
        */
        StringBuilder hql = null;
        if(isEdit){
            hql = new StringBuilder("FROM NewProductBatch WHERE  product = ? ");
        }else{
            hql = new StringBuilder("FROM NewProductBatch WHERE  product = ? AND quantitydue>0 ");
        }     
        List params = new ArrayList();
//        params.add(product.getCompany());
        params.add(product.getID());
        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND warehouse.id IN ( ");
            for (Store store : storeSet) {
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql.append(storeIn);
        }
        if (location != null) {
            hql.append(" AND location.id = ? ");
            params.add(location.getId());
        }
        hql.append(" ORDER BY warehouse.name ASC, location.name ASC, batchname ASC");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            if (paging.isValid() && totalCount > paging.getLimit()) {
                list = executeQueryPaging(hql.toString(), params.toArray(), paging);
            }
        }
        if (list.size() > 0) {
            for (Iterator it = list.iterator(); it.hasNext();) {
                NewProductBatch nsr = (NewProductBatch) it.next();
                String row = nsr.getRow() != null ? nsr.getRow().getId() : "";
                String rack = nsr.getRack() != null ? nsr.getRack().getId() : "";
                String bin = nsr.getBin() != null ? nsr.getBin().getId() : "";
                String keyValue = nsr.getProduct() + nsr.getWarehouse().getId() + nsr.getLocation().getId() + row + rack + bin + nsr.getBatchname();
                mpList.put(keyValue, nsr);
            }
        }
        return mpList;
    }

    @Override
    public Map<String, String> getSerialListByStoreProductLocation(Product product, Set<Store> storeSet, Location location, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM NewBatchSerial ns WHERE  ns.product = ? AND ns.lockquantity>0 ");
        List params = new ArrayList();
        Map<String, String> mpList = new HashMap<String, String>();
//        params.add(product.getCompany());
        params.add(product.getID());
        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND ns.batch.warehouse.id IN ( ");
            for (Store store : storeSet) {
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql.append(storeIn);
        }
        if (location != null) {
            hql.append(" AND ns.batch.location.id = ? ");
            params.add(location.getId());
        }
        hql.append(" ORDER BY batch.warehouse.name ASC, batch.location.name ASC, batch.batchname ASC");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            if (paging.isValid() && totalCount > paging.getLimit()) {
                list = executeQueryPaging(hql.toString(), params.toArray(), paging);
            }
        }
        if (list.size() > 0) {
            for (Iterator it = list.iterator(); it.hasNext();) {
                NewBatchSerial nsr = (NewBatchSerial) it.next();
                String loc=nsr.getBatch().getLocation().getId();
                String keyValue = product.isIsBatchForProduct()?nsr.getBatch().getBatchname()+loc+nsr.getBatch().getWarehouse().getId():loc+nsr.getBatch().getWarehouse().getId();
                if (mpList.containsKey(keyValue)) {
                    String ser = mpList.get(keyValue).toString() + "," + nsr.getSerialname();
                    mpList.put(keyValue, ser);
                } else {
                    mpList.put(keyValue, nsr.getSerialname());
                }

            }
        }
        return mpList;
    }

    @Override
    public List<Stock> getStockByProductBatch(Product product, String batchName, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM Stock WHERE company = ? AND product = ?  AND batchName = ? AND quantity <> 0 ");
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product);
        params.add(batchName);
        if (StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND serialNames LIKE ? ");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY batchName ASC");
        List list = executeQuery( hql.toString(), params.toArray());
        if (paging != null) {
            int totalCount = list.size();
            paging.setTotalRecord(totalCount);
            if (paging.isValid() && totalCount > paging.getLimit()) {
                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            }
        }
        return list;
    }

    @Override
    public Map<Product, Double> getDateWiseStockList(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT sm.product, SUM(CASE WHEN sm.transactionType = ? THEN -(smd.quantity) ELSE smd.quantity END) FROM StockMovement sm INNER JOIN sm.stockMovementDetails AS smd WHERE sm.company = ? AND DATE(sm.transactionDate) <= DATE(?) AND sm.product.deleted='F' ");
        List params = new ArrayList();
        params.add(TransactionType.OUT);
        params.add(company);
        params.add(date);
        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND sm.store.id IN ( ");
            for (Store store : storeSet) {
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql.append(storeIn);
        }
        if (location != null) {
            hql.append(" AND smd.location = ? ");
            params.add(location);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (sm.product.productid LIKE ? OR sm.product.name LIKE ?  OR smd.serialNames LIKE ?) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" GROUP BY sm.product HAVING SUM(CASE WHEN sm.transactionType = ? THEN -(smd.quantity) ELSE smd.quantity END) <> 0");
        params.add(TransactionType.OUT);
        hql.append(" ORDER BY sm.product.productid ASC");
        List list = executeQuery( hql.toString(), params.toArray());
        if (paging != null) {
            int totalCount = list.size();
            paging.setTotalRecord(totalCount);
            if (paging.isValid() && totalCount > paging.getLimit()) {
                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            }
        }
        Map<Product, Double> productQuantityMap = new HashMap<>();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            Product product = objs[0] != null ? (Product) objs[0] : null;
            double quantity = objs[1] != null ? (Double) objs[1] : 0;
            productQuantityMap.put(product, quantity);
        }
        return productQuantityMap;
    }
    @Override
    public List getDateWiseStockInventory(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("SELECT sm.product, SUM(CASE WHEN sm.transactionType = :tt THEN -(smd.quantity) ELSE smd.quantity END) FROM StockMovement sm INNER JOIN sm.stockMovementDetails AS smd WHERE sm.company = :c AND DATE(sm.transactionDate) <= :td AND s.product.deleted='F'");
        Map params = new HashMap<>();
        params.put("tt",TransactionType.OUT);
        params.put("c",company);
        params.put("td", date);
        if (storeSet != null && !storeSet.isEmpty()) {
//            boolean first = true;
//            StringBuilder storeIn = new StringBuilder(" AND sm.store.id IN ( ");
//            for (Store store : storeSet) {
//                if (first) {
//                    storeIn.append("'").append(store.getId()).append("'");
//                    first = false;
//                } else {
//                    storeIn.append(",").append("'").append(store.getId()).append("'");
//                }
//            }
//            storeIn.append(")");
            hql.append("AND sm.store IN ( :s)");
            params.put("s", storeSet);
        }
        if (location != null) {
            hql.append(" AND smd.location = :loc ");
            params.put("loc",location);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (sm.product.productid LIKE :ss OR sm.product.name LIKE :ss  OR smd.serialNames LIKE :ss) ");
            params.put("ss","%" + searchString + "%");
        }
        hql.append(" GROUP BY sm.product HAVING SUM(CASE WHEN sm.transactionType = :tt THEN -(smd.quantity) ELSE smd.quantity END) <> 0");
//        hql.append(" ORDER BY sm.product.productid ASC");
        List list = null;
        if (paging != null) {
            int totalCount = 0;
            if(paging.isValid()){
                String countQuery = "SELECT COUNT(s.id) " + hql.toString().substring(hql.indexOf("FROM"));
                list = executeCollectionQuery(countQuery, params);
                if(!list.isEmpty()){
                    totalCount = ((Long)list.get(0)).intValue();
                }
                
                hql.append(" ORDER BY sm.product.productid ");
                list = executeCollectionQuery(hql.toString(), params, paging);
            }else{
                hql.append(" ORDER BY sm.product.productid ");
                list = executeCollectionQuery(hql.toString(), params);
                totalCount = list.size();
            }
            paging.setTotalRecord(totalCount);
        } else {
            hql.append(" ORDER BY sm.product.productid ");
            list = executeCollectionQuery(hql.toString(), params);
        }
//        Map<Product, Double> productQuantityMap = new HashMap<>();
//        Iterator itr = list.iterator();
//        while (itr.hasNext()) {
//            Object[] objs = (Object[]) itr.next();
//            Product product = objs[0] != null ? (Product) objs[0] : null;
//            double quantity = objs[1] != null ? (Double) objs[1] : 0;
//            productQuantityMap.put(product, quantity);
//        }
        return list;
    }

    
    @Override
    public Map<String, Object[]> getDateWiseStockDetailList(Company company, Set<Store> storeSet, Location location, Date date, boolean outData, String searchString, Paging paging) throws ServiceException {
        return getDateWiseStockDetailList1(company, null, storeSet, location, null, date, outData, searchString, paging);
    }
    @Override
    public Map<String, Object[]> getDateWiseStockDetailListForProduct(Product product, Set<Store> storeSet, Location location, Date date, boolean outData, String searchString, Paging paging) throws ServiceException {
        return getDateWiseStockDetailList1(product.getCompany(), product, storeSet, location, null, date, outData, searchString, paging);
    }
    @Override
    public Map<String, Object[]> getDateWiseStockDetailListForProduct(Product product, Set<Store> storeSet, Location location, Date fromDate, Date toDate, boolean outData, String searchString, Paging paging) throws ServiceException{
        return getDateWiseStockDetailList1(product.getCompany(), product, storeSet, location, fromDate, toDate, outData, searchString, paging);
    }
    
    private Map<String, Object[]> getDateWiseStockDetailList1(Company company, Product product, Set<Store> storeSet, Location location, Date fromDate, Date toDate, boolean outData, String searchString, Paging paging) throws ServiceException {
        Map productStockDetailMap = new HashMap();
        StringBuilder hql = new StringBuilder();
        hql.append("SELECT sm.product, stm.abbrev, loc.name, rw.name AS rowname, rk.name AS rackname, bn.name AS binname, smd.batchname, "
                +" GROUP_CONCAT(smd.serialnames)  as serials, SUM(smd.quantity), stm.description, uom.name, sm.store, smd.location, smd.`row`, smd.rack, smd.bin  ");
        hql.append(" FROM in_stockmovement sm ");
        hql.append(" INNER JOIN in_sm_detail smd ON sm.id = smd.stockmovement ");
        hql.append(" INNER JOIN in_storemaster stm ON stm.id = sm.store ");
        hql.append(" INNER JOIN in_location loc ON loc.id = smd.location ");
        hql.append(" INNER JOIN product p ON p.id = sm.product ");
        hql.append(" LEFT JOIN storemaster rw ON rw.id = smd.row ");
        hql.append(" LEFT JOIN storemaster rk ON rk.id = smd.rack ");
        hql.append(" LEFT JOIN storemaster bn ON bn.id = smd.bin ");
        hql.append(" LEFT JOIN uom ON uom.id = p.unitOfMeasure ");
        hql.append(" WHERE sm.company = ? "); 
        hql.append("and p.deleteflag = 'F'");
        List params = new ArrayList();
        params.add(company.getCompanyID());
        if (fromDate != null) {
            hql.append(" AND DATE(transaction_date) >= Date(?) ");
            params.add(fromDate);
        }
        if (toDate != null) {
            hql.append(" AND DATE(transaction_date) <= Date(?) ");
            params.add(toDate);
        }
        if(product != null){
            hql.append(" AND sm.product = ?");
            params.add(product.getID());
        }
        if (outData) {
            hql.append(" AND sm.transaction_type = ?");
        } else {
            hql.append(" AND sm.transaction_type <> ?");
        }
        params.add(TransactionType.OUT.ordinal());
        
        if (storeSet != null && !storeSet.isEmpty() && !storeSet.contains(null)) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND sm.store IN ( ");
            for (Store store : storeSet) {
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql.append(storeIn);
        }
        if (location != null) {
            hql.append(" AND smd.location = ? ");
            params.add(location.getId());
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (p.productid LIKE ? OR p.name LIKE ?  OR smd.serialnames LIKE ?) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" GROUP BY sm.product, sm.store, smd.location, smd.row, smd.rack, smd.bin, smd.batchname");
        
        int totalCount = 0;
        List list = null;
        if(paging != null && paging.isValid()){
            String countQuery = "SELECT COUNT(1) " + hql.toString().substring(hql.indexOf("FROM"));
            list = executeSQLQuery( countQuery, params.toArray());
            totalCount = list.size();
            hql.append(" ORDER BY p.productid ASC, p.name, stm.abbrev, loc.name, smd.batchname");
            list = executeSQLQueryPaging(hql.toString(), params.toArray(), paging);
        }else{
            hql.append(" ORDER BY p.productid ASC, p.name, stm.abbrev, loc.name, smd.batchname");
            list = executeSQLQuery(hql.toString(), params.toArray());
            totalCount = list.size();
        }
        if (paging != null) {
            paging.setTotalRecord(totalCount);
        } 
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            String productId = objs[0] != null ? (String) objs[0] : null;
            String storeAbbrev = objs[1] != null ? (String) objs[1] : null;
            String locationName = objs[2] != null ? (String) objs[2] : null;
            String rowName = objs[3] != null ? (String) objs[3] : null;
            String rackName = objs[4] != null ? (String) objs[4] : null;
            String binName = objs[5] != null ? (String) objs[5] : null;
            String batchName = objs[6] != null ? (String) objs[6] : null;
//            String serialnames = objs[7] != null ? (String) objs[7] : null;
//            double quantity = objs[8] != null ? (Double) objs[8] : 0;
//            String storeDesc = objs[9] != null ? (String) objs[9] : null;
//            String uomName = objs[10] != null ? (String) objs[10] : null;
            String key = productId + storeAbbrev + locationName
                    + (!StringUtil.isNullOrEmpty(rowName) ? rowName : "")
                    + (!StringUtil.isNullOrEmpty(rackName) ? rackName : "")
                    + (!StringUtil.isNullOrEmpty(binName) ? binName : "")
                    + batchName;
//            Stock stock = new Stock();
//            Product prod = (Product) hibernateTemplate.get(Product.class, productId);
//            stock.setProduct(prod);
//            stock.setStore((Store) hibernateTemplate.get(Store.class, storeDescription));
//            stock.setLocation((Location) hibernateTemplate.get(Location.class, locationName));
//            if (prod.isIsrowforproduct() && !StringUtil.isNullOrEmpty(rowName)) {
//                stock.setRow((StoreMaster) hibernateTemplate.get(StoreMaster.class, rowName));
//            }
//            if (prod.isIsrackforproduct() && !StringUtil.isNullOrEmpty(rackName)) {
//                stock.setRack((StoreMaster) hibernateTemplate.get(StoreMaster.class, rackName));
//            }
//            if (prod.isIsbinforproduct() && !StringUtil.isNullOrEmpty(binName)) {
//                stock.setBin((StoreMaster) hibernateTemplate.get(StoreMaster.class, binName));
//            }
//            stock.setBatchName(batchName);
//            stock.setSerialNames(serialnames);
//            stock.setQuantity(quantity);

            productStockDetailMap.put(key, objs);

        }
        return productStockDetailMap;
    }
    @Override
    public Map<String, Product> getProductDetailsForDateWiseStock(Company company, Set<Store> storeSet, Location location, Date date, String searchString, Paging paging) throws ServiceException{
        Map productStockDetailMap = new HashMap();
        StringBuilder hql = new StringBuilder();
        hql.append("SELECT DISTINCT(p) FROM StockMovementDetail smd ");
        hql.append(" INNER JOIN smd.stockMovement sm ");
        hql.append(" INNER JOIN sm.product p ");
        hql.append(" WHERE sm.company = :c AND DATE(sm.transactionDate) <= :td  AND sm.product.deleted='F' ");
        Map params = new HashMap();
        params.put("c", company);
        params.put("td", date);
        
        if (storeSet != null && !storeSet.isEmpty() && !storeSet.contains(null)) {
            hql.append(" AND sm.store IN (:s )");
            params.put("s", storeSet);
        }
        if (location != null) {
            hql.append(" AND smd.location = :loc ");
            params.put("loc",location);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (p.productid LIKE :ss OR p.name LIKE :ss  OR smd.serialNames LIKE :ss) ");
            params.put("ss","%" + searchString + "%");
        }
        hql.append(" GROUP BY sm.product, sm.store, smd.location, smd.row, smd.rack, smd.bin, smd.batchName");

        List<Product> list = executeCollectionQuery(hql.toString(), params, paging);
       
        for(Product product : list) {
            productStockDetailMap.put(product.getID(), product);
        }
        return productStockDetailMap;
    }
    private Map<String, Stock> getDateWiseStockDetailList(Company company, Product product, Set<Store> storeSet, Location location, Date date, boolean outData, String searchString, Paging paging) throws ServiceException {
        Map productStockDetailMap = new HashMap();
        StringBuilder hql = new StringBuilder();
        hql.append("SELECT product, sm.store, smd.location, smd.row, smd.rack, smd.bin,  smd.batchname,  GROUP_CONCAT(smd.serialnames)  as serials, SUM(smd.quantity) ");
        hql.append(" FROM in_stockmovement sm ");
        hql.append(" INNER JOIN in_sm_detail smd ON sm.id = smd.stockmovement ");
        hql.append(" INNER JOIN in_storemaster stm ON stm.id = sm.store ");
        hql.append(" INNER JOIN in_location loc ON loc.id = smd.location ");
        hql.append(" INNER JOIN product p ON p.id = sm.product ");
        hql.append(" WHERE sm.company = ? AND DATE(transaction_date) <= ? ");
        List params = new ArrayList();
        params.add(company.getCompanyID());
        params.add(date);
        if(product != null){
            hql.append(" AND sm.product = ?");
            params.add(product.getID());
        }
        if (outData) {
            hql.append(" AND sm.transaction_type = ?");
        } else {
            hql.append(" AND sm.transaction_type <> ?");
        }
        params.add(TransactionType.OUT.ordinal());
        
        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND sm.store IN ( ");
            for (Store store : storeSet) {
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql.append(storeIn);
        }
        if (location != null) {
            hql.append(" AND smd.location = ? ");
            params.add(location.getId());
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (p.productid LIKE ? OR p.name LIKE ?  OR smd.serialnames LIKE ?) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" GROUP BY sm.product, sm.store, smd.location, smd.row, smd.rack, smd.bin, smd.batchname");
        
        List list = null;
        if (paging != null) {
            int totalCount = 0;
            if(paging.isValid()){
                String countQuery = "SELECT COUNT(1) " + hql.toString().substring(hql.indexOf("FROM"));
                list = executeSQLQuery(countQuery, params.toArray());
                totalCount = list.size();
                
                hql.append(" ORDER BY p.productid ASC, p.name, stm.abbrev, loc.name, smd.batchname");
                list = executeSQLQueryPaging(hql.toString(), params.toArray(), paging);
            }else{
                hql.append(" ORDER BY p.productid ASC, p.name, stm.abbrev, loc.name, smd.batchname");
                list = executeSQLQuery( hql.toString(), params.toArray());
                totalCount = list.size();
            }
            paging.setTotalRecord(totalCount);
        } else {
            hql.append(" ORDER BY p.productid ASC, p.name, stm.abbrev, loc.name, smd.batchname");
            list = executeSQLQuery(hql.toString(), params.toArray());
        }
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            String productId = objs[0] != null ? (String) objs[0] : null;
            String storeId = objs[1] != null ? (String) objs[1] : null;
            String locationId = objs[2] != null ? (String) objs[2] : null;
            String rowId = objs[3] != null ? (String) objs[3] : null;
            String rackId = objs[4] != null ? (String) objs[4] : null;
            String binId = objs[5] != null ? (String) objs[5] : null;
            String batchName = objs[6] != null ? (String) objs[6] : null;
            String serialnames = objs[7] != null ? (String) objs[7] : null;
            double quantity = objs[8] != null ? (Double) objs[8] : 0;
            String key = productId + storeId + locationId
                    + (!StringUtil.isNullOrEmpty(rowId) ? rowId : "")
                    + (!StringUtil.isNullOrEmpty(rackId) ? rackId : "")
                    + (!StringUtil.isNullOrEmpty(binId) ? binId : "")
                    + batchName;
            Stock stock = new Stock();
            Product prod = (Product) get(Product.class, productId);
            stock.setProduct(prod);
            stock.setStore((Store) get(Store.class, storeId));
            stock.setLocation((Location) get(Location.class, locationId));
            if (prod.isIsrowforproduct() && !StringUtil.isNullOrEmpty(rowId)) {
                stock.setRow((StoreMaster) get(StoreMaster.class, rowId));
            }
            if (prod.isIsrackforproduct() && !StringUtil.isNullOrEmpty(rackId)) {
                stock.setRack((StoreMaster) get(StoreMaster.class, rackId));
            }
            if (prod.isIsbinforproduct() && !StringUtil.isNullOrEmpty(binId)) {
                stock.setBin((StoreMaster) get(StoreMaster.class, binId));
            }
            stock.setBatchName(batchName);
            stock.setSerialNames(serialnames);
            stock.setQuantity(quantity);
//            stock.setPricePerUnit(quantity != 0 ? Math.abs(amount / quantity) : 0);

            productStockDetailMap.put(key, stock);

        }
        return productStockDetailMap;
    }
    
    @Override
    public Map<String, Stock> getPendingApprovalStock(Product product, Store store) throws ServiceException {
        Map productStockDetailMap = new HashMap();
        List params = new ArrayList();
        StringBuilder hql = new StringBuilder();
        boolean isStoreQAStore=false;
        ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences)get(ExtraCompanyPreferences.class, product.getCompany().getCompanyID()); ;
        String qaStoreId=extracompanyobj.getInspectionStore();
        
        if(!StringUtil.isNullOrEmpty(qaStoreId) && store != null){
            isStoreQAStore=store.getId().equals(qaStoreId);
        }

        hql.append(" SELECT product, store, location, `row`, rack, bin, batchname, GROUP_CONCAT(serialname), SUM(quantity) FROM (");

        hql.append(" SELECT sa.product, sa.store, sad.location, sad.`row`, sad.rack, sad.bin, sad.batchname, sada.serialname, sada.quantity FROM in_sa_detail_approval  sada ");
        hql.append(" INNER JOIN in_sa_approval saa ON  sada.sa_approval = saa.id ");
        hql.append(" INNER JOIN in_sa_detail sad ON  sad.id = sada.stock_adjustment_detail ");
        hql.append(" INNER JOIN in_stockadjustment sa ON sa.id = saa.stock_adjustment ");
        hql.append(" WHERE  sa.company = ? AND (sada.approval_status = 0 OR sada.repair_status IN (4,6) )");
        hql.append(" AND sa.product = ? AND sa.store = ? ");

        hql.append(" UNION ALL ");

        hql.append(" SELECT st.product, st.tostore, std.delivered_location AS location, std.delivered_row AS `row`, std.delivered_rack AS rack, std.delivered_bin AS bin, std.batchname, stda.serialname, stda.quantity FROM in_stocktransfer_detail_approval  stda ");
        hql.append(" INNER JOIN in_stocktransfer_approval sta ON  stda.stocktransfer_approval = sta.id ");
        hql.append(" INNER JOIN in_ist_detail std ON  std.id = stda.stocktransfer_detail_id ");
        hql.append(" INNER JOIN in_interstoretransfer st ON st.id = sta.stocktransferid ");
        hql.append(" WHERE  st.company = ? AND (stda.approval_status = 0 OR stda.repair_status IN (4,6) ) ");
        hql.append(" AND st.product = ? AND st.tostore = ? ");

        hql.append(" UNION ALL ");

        hql.append(" SELECT st.product, st.fromstore, std.delivered_location AS location, std.delivered_row AS `row`, std.delivered_rack AS rack, std.delivered_bin AS bin, std.batchname, stda.serialname, stda.quantity FROM in_stocktransfer_detail_approval  stda ");
        hql.append(" INNER JOIN in_stocktransfer_approval sta ON  stda.stocktransfer_approval = sta.id ");
        hql.append(" INNER JOIN in_sr_detail std ON  std.id = stda.stocktransfer_detail_id ");
        hql.append(" INNER JOIN in_goodsrequest st ON st.id = sta.stocktransferid ");
        hql.append(" WHERE  st.company = ? AND (stda.approval_status = 0 OR stda.repair_status IN (4,6) ) ");
        hql.append(" AND st.product = ? AND st.fromstore = ? ");
        
        hql.append(" UNION ALL ");
        
        if(isStoreQAStore){//ERP-25065
            hql.append(" SELECT  ca.product,'"+qaStoreId+"', pb.location, pb.`row`, pb.rack, pb.bin, cda.batchName AS batchname, cda.serialName AS serialname, cda.quantity FROM in_consignmentdetails  cda ");
        }else{
            hql.append(" SELECT  ca.product, ca.store, pb.location, pb.`row`, pb.rack, pb.bin, cda.batchName AS batchname, cda.serialName AS serialname, cda.quantity FROM in_consignmentdetails  cda ");
        }
        hql.append(" INNER JOIN in_consignment ca ON  cda.consignment = ca.id ");
        hql.append(" LEFT JOIN newproductbatch pb ON pb.id = cda.batch ");
        hql.append(" WHERE  ca.company = ? AND (cda.approval_status = 0 OR cda.repair_status IN (4,6) ) ");
        if(isStoreQAStore){
            hql.append(" AND ca.product = ? ");//ERP-25065
        }else{
            hql.append(" AND ca.product = ? AND ca.store = ? ");
        }
        hql.append(") combineTbl GROUP BY product, store,  location, `row`, rack, bin, batchname");
        Company company = product.getCompany();
        params.add(company);
        params.add(product);
        params.add(store);
        params.add(company);
        params.add(product);
        params.add(store);
        params.add(company);
        params.add(product);
        params.add(store);
        params.add(company);
        params.add(product);
        if(!isStoreQAStore){
            params.add(store);//ERP-25065
        }
        List list = executeSQLQuery( hql.toString(), params.toArray());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            String productId = objs[0] != null ? (String) objs[0] : null;
            String storeId = objs[1] != null ? (String) objs[1] : null;
            String locationId = objs[2] != null ? (String) objs[2] : "";
            String rowId = objs[3] != null ? (String) objs[3] : "";
            String rackId = objs[4] != null ? (String) objs[4] : "";
            String binId = objs[5] != null ? (String) objs[5] : "";
            String batchName = objs[6] != null ? (String) objs[6] : "";
            String serialnames = objs[7] != null ? (String) objs[7] : null;
            double quantity = objs[8] != null ? (Double) objs[8] : 0;
            String key = productId + storeId + locationId + rowId + rackId + binId + batchName;
//            String key = productId + locationId + rowId + rackId + binId + batchName;//ERP-25065
            Stock stock = new Stock();
            stock.setProduct((Product) get(Product.class, productId));
            stock.setStore((Store) get(Store.class, storeId));
            stock.setBatchName(batchName);
            stock.setSerialNames(serialnames);
            stock.setQuantity(quantity);

            productStockDetailMap.put(key, stock);

        }
        return productStockDetailMap;
    }

    @Override
    public Map<String, Double> getProductBlockedQuantity(Company company, Store store, Location location,String searchString) throws ServiceException {
        Map productStockDetailMap = new HashMap();
        List params = new ArrayList();
        String storeCondi = "";
        String locaCond = "";
        String searchCond = "";
        if (store != null) {
            storeCondi = "AND nb.warehouse='" + store.getId() + "'";
        }
        if (location != null) {
            locaCond = " AND nb.location='" + location.getId() + "'";
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            searchCond = " AND (p.productid LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%')";
        }
        StringBuilder hql = new StringBuilder(" SELECT tb.product,SUM(tb.lockquantity) FROM (SELECT p.id AS product,SUM(lockquantity) AS lockquantity,nb.warehouse,nb.location FROM newproductbatch nb "
                + " INNER JOIN product p ON nb.product=p.id AND p.isSerialForProduct='F'"
                + " WHERE p.company=? AND nb.warehouse IS NOT NULL AND nb.location IS NOT NULL AND nb.lockquantity>0 " + storeCondi + locaCond + searchCond
                + " GROUP BY nb.warehouse,nb.location,product "
                + " UNION ALL "
                + " SELECT p.id AS product,SUM(nsr.lockquantity) AS lockquantity,nb.warehouse,nb.location FROM newproductbatch nb "
                + " INNER JOIN newbatchserial nsr ON nsr.batch=nb.id AND nsr.product=nb.product"
                + " INNER JOIN product p ON nb.product=p.id AND p.isSerialForProduct='T'"
                + " WHERE p.company=? AND nb.warehouse IS NOT NULL AND nb.location IS NOT NULL AND nsr.lockquantity>0 " + storeCondi + locaCond + searchCond
                + " GROUP BY nb.warehouse,nb.location,product) AS tb GROUP BY tb.product");
        params.add(company.getCompanyID());
        params.add(company.getCompanyID());

        List list = executeSQLQuery(hql.toString(), params.toArray());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            String productId = objs[0] != null ? (String) objs[0] : null;
            double quantity = objs[1] != null ? (Double) objs[1] : 0;
            productStockDetailMap.put(productId, authHandler.roundQuantity(quantity,company.getCompanyID()));
        }
        return productStockDetailMap;
    }

    @Override
    public Map<String, Double> getProductBlockedQuantityWithInStore(Company company, Store store,String searchString) throws ServiceException {
        Map productStockDetailMap = new HashMap();
        List params = new ArrayList();
        String storeCondi = "";
        String locaCond = "";
        String searchCond = "";
        if (store != null) {
            storeCondi = "AND nb.warehouse='" + store.getId() + "'";
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            searchCond = " AND (p.productid LIKE '%" + searchString + "%' OR p.name LIKE '%" + searchString + "%')";
        }
        StringBuilder hql = new StringBuilder(" SELECT CONCAT(tb.product,tb.warehouse),SUM(tb.lockquantity) FROM (SELECT p.id AS product,SUM(lockquantity) AS lockquantity,nb.warehouse,nb.location FROM newproductbatch nb "
                + " INNER JOIN product p ON nb.product=p.id AND p.isSerialForProduct='F'"
                + " WHERE p.company=? AND nb.warehouse IS NOT NULL AND nb.location IS NOT NULL AND nb.lockquantity>0 " + storeCondi + locaCond + searchCond
                + " GROUP BY nb.warehouse,product "
                + " UNION ALL "
                + " SELECT p.id AS product,SUM(nsr.lockquantity) AS lockquantity,nb.warehouse,nb.location FROM newproductbatch nb "
                + " INNER JOIN newbatchserial nsr ON nsr.batch=nb.id AND nsr.product=nb.product"
                + " INNER JOIN product p ON nb.product=p.id AND p.isSerialForProduct='T'"
                + " WHERE p.company=? AND nb.warehouse IS NOT NULL AND nb.location IS NOT NULL AND nsr.lockquantity>0 " + storeCondi + locaCond + searchCond
                + " GROUP BY nb.warehouse,product) AS tb GROUP BY tb.product");
        params.add(company.getCompanyID());
        params.add(company.getCompanyID());

        List list = executeSQLQuery(hql.toString(), params.toArray());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            String productIdStoreId = objs[0] != null ? (String) objs[0] : null;
            double quantity = objs[1] != null ? (Double) objs[1] : 0;
            productStockDetailMap.put(productIdStoreId, authHandler.roundQuantity(quantity,company.getCompanyID()));
        }
        return productStockDetailMap;
    }
    
    @Override
    public List<Stock> getReorderReportList(Company company, Set<Store> storeSet, String searchString, boolean isBelowReorderFilter, Paging paging) throws ServiceException {
        List<Stock> stocks = new ArrayList<Stock>();
        StringBuilder hql = new StringBuilder("SELECT sp, s.store, SUM(s.quantity),sp.reorderLevel  FROM Stock s ");
        List params = new ArrayList();


        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" join s.store ss WITH  ss.id IN ( ");
            for (Store store : storeSet) {
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(") ");
            hql.append(storeIn);
        }
        hql.append("right join s.product sp  WHERE sp.company = ? and sp.iswarehouseforproduct='T' and sp.islocationforproduct='T' and sp.deleted='F'");
        params.add(company);
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (sp.productid LIKE ? OR sp.name LIKE ?) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }

        if (storeSet.size() == 1) {
            hql.append(" GROUP BY sp.id");
        } else {
            hql.append(" GROUP BY sp.id");
        }
        if (isBelowReorderFilter) {
            hql.append(" HAVING (SUM(s.quantity)<sp.reorderLevel)");
        }
        hql.append(" ORDER BY sp.id ");
        String countQuery = "SELECT 1,sp.reorderLevel " + hql.toString().substring(hql.indexOf("FROM"));
        List list = executeQuery( countQuery, params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        } else {
            list = executeQuery( hql.toString(), params.toArray());
        }

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            Stock stock = new Stock();
            stock.setProduct((Product) objs[0]);
            stock.setStore(objs[1] != null ? (Store) objs[1] : null);
            double qty = objs[2] != null ? (Double) objs[2] : 0;
            stock.setQuantity(qty);
            stocks.add(stock);
        }
        return stocks;
    }
    @Override
    public List<Stock> getBatchSerialListByStore(Store store) throws ServiceException{
        String query = "FROM Stock WHERE store = ?";
        List list = executeQuery(query, store);
        return list;
    }
    
    @Override
    public List getStockForPendingApprovalSerial(String productId, String batchName, String serialName) throws ServiceException{
        StringBuilder hql = new StringBuilder();
        List params = new ArrayList();

        hql.append(" SELECT sada.id, 3 AS transModule FROM in_sa_detail_approval  sada ");
        hql.append(" INNER JOIN in_sa_approval saa ON  sada.sa_approval = saa.id ");
        hql.append(" INNER JOIN in_sa_detail sad ON  sad.id = sada.stock_adjustment_detail ");
        hql.append(" INNER JOIN in_stockadjustment sa ON sa.id = saa.stock_adjustment ");
        hql.append(" WHERE sada.approval_status = ? ");
        hql.append(" AND sa.product = ? AND sad.batchname = ? AND sada.serialname = ? ");

        hql.append(" UNION ALL ");

        hql.append(" SELECT stda.id, 2 AS transModule FROM in_stocktransfer_detail_approval  stda ");
        hql.append(" INNER JOIN in_stocktransfer_approval sta ON  stda.stocktransfer_approval = sta.id ");
        hql.append(" INNER JOIN in_ist_detail std ON  std.id = stda.stocktransfer_detail_id ");
        hql.append(" INNER JOIN in_interstoretransfer st ON st.id = sta.stocktransferid ");
        hql.append(" WHERE stda.approval_status = ?");
        hql.append(" AND st.product = ? AND std.batchname = ? AND stda.serialname = ?");

        hql.append(" UNION ALL ");

        hql.append(" SELECT stda.id, 0 AS transModule FROM in_stocktransfer_detail_approval  stda ");
        hql.append(" INNER JOIN in_stocktransfer_approval sta ON  stda.stocktransfer_approval = sta.id ");
        hql.append(" INNER JOIN in_sr_detail std ON  std.id = stda.stocktransfer_detail_id ");
        hql.append(" INNER JOIN in_goodsrequest st ON st.id = sta.stocktransferid ");
        hql.append(" WHERE stda.approval_status = ? ");
        hql.append(" AND st.product = ? AND std.batchname = ? AND stda.serialname = ?");

        hql.append(" UNION ALL ");

        hql.append(" SELECT cda.id, 7 AS transModule FROM in_consignmentdetails  cda ");
        hql.append(" INNER JOIN in_consignment ca ON  cda.consignment = ca.id ");
        hql.append(" LEFT JOIN newproductbatch pb ON pb.id = cda.batch ");
        hql.append(" WHERE cda.approval_status = ? ");
        hql.append(" AND ca.product = ? AND cda.batchName = ? AND cda.serialName = ?");

        params.add(ApprovalStatus.PENDING.ordinal());
        params.add(productId);
        params.add(batchName);
        params.add(serialName);
        params.add(ApprovalStatus.PENDING.ordinal());
        params.add(productId);
        params.add(batchName);
        params.add(serialName);
        params.add(ApprovalStatus.PENDING.ordinal());
        params.add(productId);
        params.add(batchName);
        params.add(serialName);
        params.add(ApprovalStatus.PENDING.ordinal());
        params.add(productId);
        params.add(batchName);
        params.add(serialName);
        
        List list = executeSQLQuery( hql.toString(), params.toArray());
        return list;
       
    }
    @Override
    public List getStockForPendingRepairSerial(String productId, String batchName, String serialName) throws ServiceException{
        StringBuilder hql = new StringBuilder();
        List params = new ArrayList();

        hql.append(" SELECT sada.id, 3 AS transModule FROM in_sa_detail_approval  sada ");
        hql.append(" INNER JOIN in_sa_approval saa ON  sada.sa_approval = saa.id ");
        hql.append(" INNER JOIN in_sa_detail sad ON  sad.id = sada.stock_adjustment_detail ");
        hql.append(" INNER JOIN in_stockadjustment sa ON sa.id = saa.stock_adjustment ");
        hql.append(" WHERE sada.repair_status IN (?,?) ");
        hql.append(" AND sa.product = ? AND sad.batchname = ? AND sada.serialname = ?");

        hql.append(" UNION ALL ");

        hql.append(" SELECT stda.id, 2 AS transModule FROM in_stocktransfer_detail_approval  stda ");
        hql.append(" INNER JOIN in_stocktransfer_approval sta ON  stda.stocktransfer_approval = sta.id ");
        hql.append(" INNER JOIN in_ist_detail std ON  std.id = stda.stocktransfer_detail_id ");
        hql.append(" INNER JOIN in_interstoretransfer st ON st.id = sta.stocktransferid ");
        hql.append(" WHERE stda.repair_status IN (?,?) ");
        hql.append(" AND st.product = ? AND std.batchname = ? AND stda.serialname = ? ");

        hql.append(" UNION ALL ");

        hql.append(" SELECT stda.id, 0 AS transModule FROM in_stocktransfer_detail_approval  stda ");
        hql.append(" INNER JOIN in_stocktransfer_approval sta ON  stda.stocktransfer_approval = sta.id ");
        hql.append(" INNER JOIN in_sr_detail std ON  std.id = stda.stocktransfer_detail_id ");
        hql.append(" INNER JOIN in_goodsrequest st ON st.id = sta.stocktransferid ");
        hql.append(" WHERE stda.repair_status IN (?,?) ");
        hql.append(" AND st.product = ? AND std.batchname = ? AND stda.serialname = ? ");

        hql.append(" UNION ALL ");

        hql.append(" SELECT cda.id, 7 AS transModule FROM in_consignmentdetails  cda ");
        hql.append(" INNER JOIN in_consignment ca ON  cda.consignment = ca.id ");
        hql.append(" LEFT JOIN newproductbatch pb ON pb.id = cda.batch ");
        hql.append(" WHERE cda.repair_status IN (?,?)  ");
        hql.append(" AND ca.product = ? AND cda.batchName = ? AND cda.serialName = ?");
        
        params.add(ApprovalStatus.REPAIRPENDING.ordinal());
        params.add(ApprovalStatus.REPAIRREJECT.ordinal());
        params.add(productId);
        params.add(batchName);
        params.add(serialName);
        
        params.add(ApprovalStatus.REPAIRPENDING.ordinal());
        params.add(ApprovalStatus.REPAIRREJECT.ordinal());
        params.add(productId);
        params.add(batchName);
        params.add(serialName);
        
        params.add(ApprovalStatus.REPAIRPENDING.ordinal());
        params.add(ApprovalStatus.REPAIRREJECT.ordinal());
        params.add(productId);
        params.add(batchName);
        params.add(serialName);
        
        params.add(ApprovalStatus.REPAIRPENDING.ordinal());
        params.add(ApprovalStatus.REPAIRREJECT.ordinal());
        params.add(productId);
        params.add(batchName);
        params.add(serialName);
        
        List list = executeSQLQuery( hql.toString(), params.toArray());
        return list;

    }

    @Override
    public List<Stock> getStockByProductBatchStore(Product product, Store store, String batchName, String serialName) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM Stock WHERE company = ? AND product = ?  AND batchName = ? AND quantity <> 0 ");
        List params = new ArrayList();
        params.add(product.getCompany());
        params.add(product);
        params.add(batchName);
        if (StringUtil.isNullOrEmpty(serialName)) {
            hql.append(" AND serialNames LIKE ? ");
            params.add("%" + serialName + "%");
        }
        hql.append(" ORDER BY batchName ASC");
        List list = executeQuery( hql.toString(), params.toArray());
        return list;
    }
    @Override
    public Map<String, String> getSerialSkuMap(String productId, String batchName, String[] serialArray) throws ServiceException{
        StringBuilder hql = new StringBuilder(" SELECT nbs.serialname, nbs.skufield FROM NewBatchSerial nbs");
        hql.append(" WHERE nbs.product = :p AND nbs.serialname IN (:snames)");
        hql.append(" AND (nbs.batch IS NULL OR (nbs.batch IS NOT NULL AND  nbs.batch.batchname = :bn)) ");
        hql.append(" GROUP BY nbs.serialname, nbs.skufield");
        
        Map params = new HashMap();
        params.put("p", productId);
        params.put("snames", Arrays.asList(serialArray));
        params.put("bn", batchName);
        
        List<Object[]> list = executeCollectionQuery(hql.toString(), params);
        Map<String, String> data = new HashMap();
        for(Object[] objs : list){
            String serialName = objs[0] != null ? (String) objs[0] : null;
            String skuName = objs[1] != null ? (String) objs[1] : null;
            data.put(serialName, skuName);
        }
        return data;
    }
    @Override
    public List getAssetDetailList(HashMap<String, Object> jsonRequestParam, Paging paging) {
        String companyId = jsonRequestParam.containsKey("company") ? (String) jsonRequestParam.get("company") : "";
        String location = jsonRequestParam.containsKey("locationid") ? (String) jsonRequestParam.get("locationid") : "";
        String  ss = (jsonRequestParam.containsKey("ss") ? jsonRequestParam.get("ss").toString() : null);
        String  storeId = (jsonRequestParam.containsKey("Customer") ? jsonRequestParam.get("Customer").toString() : "");
        List list = new ArrayList();
        if (!StringUtil.isNullOrEmpty(companyId)) {
            companyId = (String) jsonRequestParam.get("company");
        }        
        Set<Store> storeSet = (Set<Store>) (jsonRequestParam.containsKey("storeset") ? jsonRequestParam.get("storeset") : null);
        List params = new ArrayList();
         StringBuilder sql=new StringBuilder("");
        if (!("Customer".equals(storeId))) {
            sql.append("SELECT '-' AS donumber,p.productid,p.description,nb.batchname AS Batchname,sr.serialname,sr.skufield, ");
            sql.append(" iw.`name` AS warehouse,lc.`name` AS location,'-' AS customer,sr.quantitydue,p.name,'-' as orderdate,'-' AS loanfrmdate,");
            sql.append("'-' AS loantodate,'-' AS cid,'-' AS soid,sr.id as srid,p.id,'-' as wid,'-' as lid,nb.expdate,p.isBatchForProduct,p.isSerialForProduct,sr.id as serailid   ");
            sql.append(" FROM newbatchserial sr ");
            sql.append(" INNER JOIN product p ON p.id=sr.product AND p.company=sr.company ");
            sql.append(" INNER JOIN newproductbatch nb ON nb.id=sr.batch AND sr.product=nb.product");
            sql.append(" INNER JOIN inventorywarehouse iw ON nb.warehouse=iw.id");
            sql.append(" INNER JOIN in_location lc ON lc.id=nb.location");
            sql.append(" WHERE p.company=? AND p.isSerialForProduct='T' AND sr.quantitydue=1 AND nb.quantitydue>0 ");
            sql.append(" AND nb.location IS not NULL  ");
            params.add(companyId);
            Store store = null;
            if (storeSet != null && !storeSet.isEmpty()) {
                boolean first = true;
                StringBuilder storeIn = new StringBuilder(" AND iw.id IN ( ");
                for (Store str : storeSet) {
                    if (storeSet.size() == 1) {
                        store = str;
                    }
                    if (first) {
                        storeIn.append("'").append(str.getId()).append("'");
                        first = false;
                    } else {
                        storeIn.append(",").append("'").append(str.getId()).append("'");
                    }
                }
                storeIn.append(")");
                sql.append(storeIn);
            }
            if (!StringUtil.isNullOrEmpty(location)) {
                sql.append(" AND nb.location='" + location + "' ");
            }
        }
        if ((storeSet != null && storeSet.size() > 1) || "Customer".equals(storeId)) {
            if (!StringUtil.isNullOrEmpty(sql.toString())) {
                sql.append(" UNION ALL ");
            }
            sql.append(" SELECT d.donumber,p.productid,p.description,nb.batchname AS Batchname,srl.serialname,srl.skufield,'-' AS warehouse,'-' AS location,");
            sql.append(" c.`name` AS customer,srl.quantitydue,p.name,d.orderdate,DATE(sor.fromdate) AS loanfrmdate,DATE(sor.todate) AS loantodate,");
            sql.append(" c.id as cid,sor.id soid,srl.id as srid,p.id,sor.requestwarehouse as wid,sor.requestlocation as lid,nb.expdate,p.isBatchForProduct,p.isSerialForProduct,srl.id as serailid  ");
            sql.append(" FROM deliveryorder d ");
            sql.append(" INNER JOIN dodetails dtl ON dtl.deliveryorder=d.id ");
            sql.append(" inner JOIN product p ON p.id=dtl.product ");
            sql.append(" INNER JOIN serialdocumentmapping srmp ON srmp.documentid=dtl.id AND srmp.transactiontype=28 ");
            sql.append(" INNER JOIN newbatchserial srl ON srl.id=srmp.serialid ");
            sql.append(" INNER JOIN newproductbatch nb ON nb.id=srl.batch AND srl.product=nb.product");
            sql.append(" INNER JOIN inventorywarehouse iw ON nb.warehouse=iw.id ");
            sql.append(" INNER JOIN customer c ON c.id=d.customer ");
            sql.append(" LEFT JOIN sodetails sodtl ON sodtl.id=dtl.sodetails ");
            sql.append(" LEFT JOIN salesorder sor ON sor.id=sodtl.salesorder ");
            
            sql.append(" WHERE d.isdoclosed='F' AND dtl.islineitemclosed='F' AND d.deleteflag='F' AND isopeninsr = 'T'  AND d.company=? AND srl.quantitydue=1   AND p.isSerialForProduct='T' ");
            params.add(companyId);
        }
//        sql.append(" GROUP BY sr.product,serialname  ");
        StringBuilder query = new StringBuilder(" SELECT * FROM (");
        query.append(sql);
        query.append(" ) AS tb ");
        if (!StringUtil.isNullOrEmpty(ss)) {
            query.append(" where (tb.productid LIKE ? OR tb.name LIKE ? OR tb.donumber LIKE ? OR tb.serialname LIKE ? ) ");
            params.add("%" + ss + "%");
            params.add("%" + ss + "%");
            params.add("%" + ss + "%");
            params.add("%" + ss + "%");
        }
        query.append(" ORDER BY productid ");
        
        try {
            list = executeSQLQuery(query.toString(), params.toArray());
            int totalCount = list.size();
            if (paging != null) {
                paging.setTotalRecord(totalCount);
                list = executeSQLQueryPaging(query.toString(), params.toArray(), paging);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(StockDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    @Override
    public HashMap<String, String> getCustomerAddress(String companyId) {
        HashMap<String, String> addmap = new HashMap<>();
        List params = new ArrayList();
        try {
            StringBuilder sql = new StringBuilder("SELECT customerid,country FROM customeraddressdetails WHERE company=? GROUP BY customerid");
            params.add(companyId);
            List list = executeSQLQuery(sql.toString(), params.toArray());
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] objs = (Object[]) itr.next();
                String customerId = objs[0] != null ? (String) objs[0] : "";
                String country = objs[1] != null ? (String) objs[1] : "";
                addmap.put(customerId, country);
            }
        } catch (Exception ex) {
        }
        return addmap;
    }

    @Override
    public List getAssetStockDetailList(HashMap<String, Object> jsonRequestParam, Paging paging) {
        String companyId = jsonRequestParam.containsKey("company") ? (String) jsonRequestParam.get("company") : "";
        String location = jsonRequestParam.containsKey("locationid") ? (String) jsonRequestParam.get("locationid") : "";
        String ss = (jsonRequestParam.containsKey("ss") ? jsonRequestParam.get("ss").toString() : null);
        String storeId = (jsonRequestParam.containsKey("Customer") ? jsonRequestParam.get("Customer").toString() : "");
        List list = new ArrayList();
        if (!StringUtil.isNullOrEmpty(companyId)) {
            companyId = (String) jsonRequestParam.get("company");
        }
        Set<Store> storeSet = (Set<Store>) (jsonRequestParam.containsKey("storeset") ? jsonRequestParam.get("storeset") : null);
        List params = new ArrayList();

        StringBuilder sql = new StringBuilder("");
        if (!("Customer".equals(storeId))) {
            sql.append(" SELECT '-' AS donumber,p.productid,p.description,nb.batchname AS Batchname,'-' AS serialname,'-' AS skufield,iw.`name` AS warehouse,");
            sql.append(" lc.`name` AS location,'-' AS customer,nb.quantitydue  as qty,p.name,'-' as orderdate,'-' AS loanfrmdate,'-' AS loantodate,");
            sql.append(" '-' AS cid,'-' AS soid,'-' as srid,p.id,'-' as wid,'-' as lid ,nb.expdate,p.isBatchForProduct,p.isSerialForProduct,null as serailid ");
            sql.append(" FROM newproductbatch nb");
            sql.append(" INNER JOIN product p ON p.id=nb.product AND p.company=nb.company ");
            sql.append(" INNER JOIN inventorywarehouse iw ON nb.warehouse=iw.id ");
            sql.append(" INNER JOIN in_location lc ON lc.id=nb.location ");
            sql.append("WHERE p.company=? AND p.isSerialForProduct='F' AND nb.quantitydue>0  AND nb.location IS not NULL  ");
            params.add(companyId);

            Store store = null;
            if (storeSet != null && !storeSet.isEmpty()) {
                boolean first = true;
                StringBuilder storeIn = new StringBuilder(" AND iw.id IN ( ");
                for (Store str : storeSet) {
                    if (storeSet.size() == 1) {
                        store = str;
                    }
                    if (first) {
                        storeIn.append("'").append(str.getId()).append("'");
                        first = false;
                    } else {
                        storeIn.append(",").append("'").append(str.getId()).append("'");
                    }
                }
                storeIn.append(")");
                sql.append(storeIn);
            }
            if (!StringUtil.isNullOrEmpty(location)) {
                sql.append(" AND nb.location='" + location + "' ");
            }
        }
        if ((storeSet != null && storeSet.size() > 1) || "Customer".equals(storeId)) {
            if (!StringUtil.isNullOrEmpty(sql.toString())) {
                sql.append(" UNION ALL ");
            }
            sql.append(" SELECT d.donumber,p.productid,p.description,nb.batchname AS Batchname,'-' AS serialname,'-' AS skufield,'-' AS warehouse,'-' AS location,c.`name` AS customer,");
            sql.append(" (srmp.quantity-IFNULL(SUM(lvm.quantity),0)) as qty,p.name,d.orderdate,DATE(sor.fromdate) AS loanfrmdate,DATE(sor.todate) AS loantodate,");
            sql.append(" c.id as cid,sor.id soid,'-' as srid,p.id ,sor.requestwarehouse as wid,sor.requestlocation as lid ,nb.expdate,p.isBatchForProduct,p.isSerialForProduct,null as serailid  ");
            sql.append(" FROM deliveryorder d  ");
            sql.append(" INNER JOIN dodetails dtl ON dtl.deliveryorder=d.id ");
            sql.append(" LEFT JOIN srdetails srdl ON srdl.dodetails=dtl.id ");
            sql.append(" inner JOIN product p ON p.id=dtl.product ");
            sql.append(" INNER JOIN locationbatchdocumentmapping srmp ON srmp.documentid=dtl.id AND srmp.transactiontype=28  ");
            sql.append(" LEFT JOIN locationbatchdocumentmapping lvm ON lvm.documentid=srdl.id AND srmp.batchmapid=lvm.batchmapid ");
            sql.append(" INNER JOIN newproductbatch nb ON nb.id=srmp.batchmapid ");
            sql.append(" INNER JOIN inventorywarehouse iw ON nb.warehouse=iw.id ");
            sql.append(" INNER JOIN customer c ON c.id=d.customer ");
            sql.append(" LEFT JOIN sodetails sodtl ON sodtl.id=dtl.sodetails ");
            sql.append(" LEFT JOIN salesorder sor ON sor.id=sodtl.salesorder ");
            sql.append(" WHERE d.isdoclosed='F' AND dtl.islineitemclosed='F' AND d.deleteflag='F'  AND isopeninsr = 'T'  AND d.company=? AND nb.quantitydue>0 AND p.isSerialForProduct='F'");
//            sql.append(" AND (srmp.quantity-IFNULL(srdl.baseuomquantity,0))>0 GROUP BY d.donumber,srmp.batchmapid ");
            sql.append("   GROUP BY d.donumber,dtl.id,srmp.batchmapid ");
            params.add(companyId);
        }

        StringBuilder query = new StringBuilder(" SELECT * FROM (");
        query.append(sql);
        query.append(" ) AS tb where tb.qty>0 ");
        if (!StringUtil.isNullOrEmpty(ss)) {
            query.append(" and (tb.productid LIKE ? OR tb.name LIKE ? OR tb.donumber LIKE ? OR tb.batchname LIKE ? ) ");
            params.add("%" + ss + "%");
            params.add("%" + ss + "%");
            params.add("%" + ss + "%");
            params.add("%" + ss + "%");
        }
        query.append(" ORDER BY productid ");
        try {
            list = executeSQLQuery(query.toString(), params.toArray());
            int totalCount = list.size();
            if (paging != null) {
                paging.setTotalRecord(totalCount);
                list = executeSQLQueryPaging(query.toString(), params.toArray(), paging);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(StockDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
    
    /**
     * @Desc : Get Stock Store Transfer Data using DO
     * @param json
     * @return
     * @throws ServiceException
     */
    @Override   
    public KwlReturnObject getGRODetailISTMapping(JSONObject json) throws ServiceException {
        List params = new ArrayList();
        StringBuilder condition = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(json.optString("grodid"))) {
            condition.append("where grodistmapping.groDetail=?");
            params.add(json.optString("grodid"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("istRequest"))) {
            if (condition.indexOf("where") != -1) {
                condition.append(" and ist.id = ? ");
            } else {
                condition.append(" where ist.id = ? ");
            }
            params.add(json.optString("istRequest"));
        }
        String query = "select grodistmapping from GRODetailISTMapping grodistmapping inner join grodistmapping.interStoreTransferRequest ist  " + condition.toString();
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "get Batch Remaining Qty From IST.", "", list, list.size());
    }
    
    
    /**
     * @Desc : Get Stock Store Transfer Data using wocdid or istRequest
     * @param json
     * @return
     * @throws ServiceException
     */
    @Override   
    public KwlReturnObject getWOCDetailISTMapping(JSONObject json) throws ServiceException {
        List params = new ArrayList();
        StringBuilder condition = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(json.optString("wocdid"))) {
            condition.append("where wocdistmapping.wocDetail=?");
            params.add(json.optString("wocdid"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("istRequest"))) {
            if (condition.indexOf("where") != -1) {
                condition.append(" and ist.id = ? ");
            } else {
                condition.append(" where ist.id = ? ");
            }
            params.add(json.optString("istRequest"));
        }
        String query = "select wocdistmapping from WOCDetailISTMapping wocdistmapping inner join wocdistmapping.interStoreTransferRequest ist  " + condition.toString();
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "get Batch Remaining Qty From IST.", "", list, list.size());
    }
    
    @Override
    public String getGoodsReceiptOrderNumberUsingMapping(JSONObject json) throws ServiceException {
        String groNumber = null;
        StringBuilder condition = new StringBuilder();
        List params = new ArrayList();
        if (!StringUtil.isNullOrEmpty(json.optString("mappingid"))) {
            condition.append("where mapping.id = ? ");
            params.add(json.optString("mappingid"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString(Constants.companyKey))) {
            if (condition.indexOf("where") != -1) {
                condition.append("and gro.company = ? ");
            } else {
                condition.append("where gro.company = ? ");
            }
            params.add(json.optString(Constants.companyKey));
        }
        String query = "select gro.gronumber from grorder gro inner join grodetails grod on grod.grorder = gro.id inner join grodetailistmapping mapping on mapping.grodetail = grod.id " + condition.toString();
        List list = executeSQLQuery(query, params.toArray());
        if (list != null && !list.isEmpty()) {
            groNumber = (String) list.get(0);
        }
        return groNumber;
    }
    
    
    @Override
    public String getWorkOrderNumberUsingMapping(JSONObject json) throws ServiceException {
        String workorderid = null;
        StringBuilder condition = new StringBuilder();
        List params = new ArrayList();
        if (!StringUtil.isNullOrEmpty(json.optString("wocdistmapping"))) {
            condition.append("where wocdistmapping.id = ? ");
            params.add(json.optString("wocdistmapping"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString(Constants.companyKey))) {
            if (condition.indexOf("where") != -1) {
                condition.append("and wo.company = ? ");
            } else {
                condition.append("where wo.company = ? ");
            }
            params.add(json.optString(Constants.companyKey));
        }
        String query = "select wo.workorderid from workorder wo inner join workordercomponentdetail wocd on wocd.workorder = wo.id inner join wocdetailistmapping wocdistmapping on wocdistmapping.wocdetail = wocd.id " + condition.toString();
        List list = executeSQLQuery(query, params.toArray());
        if (list != null && !list.isEmpty()) {
            workorderid = (String) list.get(0);
        }
        return workorderid;
    }
    
    /**
     * @Desc : Get Stock Store Transfer Data using DO
     * @param JSONObject parameters
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getDODetailISTMapping(JSONObject json) throws ServiceException {
        List params = new ArrayList();
        StringBuilder condition = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(json.optString("dodetailid"))) {
            condition.append("where dodqcistmapping.dodetailID=?");
            params.add(json.optString("dodetailid"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("istrequest"))) {
            if (condition.indexOf("where") != -1) {
                condition.append(" and ist.id = ? ");
            } else {
                condition.append(" where ist.id = ? ");
            }
            params.add(json.optString("istrequest"));
        }
        String joinQuery = "";
        if (condition.indexOf("where") != -1) {
            condition.append(" and ist.id = dodqcistmapping.qcInterStoreTransferRequest ");
        } else {
            condition.append(" where ist.id =  dodqcistmapping.qcInterStoreTransferRequest ");
        }
        if (!StringUtil.isNullOrEmpty(json.optString("istRequestId"))) {
            params.add(json.optString("istRequestId"));
            joinQuery += ", DeliveryDetailInterStoreLocationMapping dodistm ";
            if (condition.indexOf("where") != -1) {
                condition.append(" and dodistm.dodqcistmapping = dodqcistmapping.ID  ");
            } else {
                condition.append(" where dodistm.dodqcistmapping = dodqcistmapping.ID  ");
            }
            if (condition.indexOf("where") != -1) {
                condition.append(" and dodistm.interStoreTransferRequest.id = ? ");
            } else {
                condition.append(" where dodistm.interStoreTransferRequest.id = ? ");
            }
        }
        String query = "select dodqcistmapping from DODQCISTMapping dodqcistmapping,InterStoreTransferRequest ist  " + joinQuery + condition.toString();
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "Get DODQCISTMapping Details.", "", list, list.size());
    }
    
    /**
     * Method is used to get delivery order number.
     *
     * @param json
     * @return
     * @throws ServiceException
     */
    @Override
    public String getDeliveryOrderNumberUsingMapping(JSONObject json) throws ServiceException {
        String groNumber = null;
        StringBuilder condition = new StringBuilder();
        List params = new ArrayList();
        if (!StringUtil.isNullOrEmpty(json.optString("mappingid"))) {
            condition.append("where mapping.id = ? ");
            params.add(json.optString("mappingid"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString(Constants.companyKey))) {
            if (condition.indexOf("where") != -1) {
                condition.append("and do.company = ? ");
            } else {
                condition.append("where do.company = ? ");
            }
            params.add(json.optString(Constants.companyKey));
        }
        String query = "select do.donumber from deliveryorder do inner join dodetails dod on dod.deliveryorder = do.id inner join dodqcistmapping mapping on mapping.dodetailid = dod.id " + condition.toString();
        List list = executeSQLQuery(query, params.toArray());
        if (list != null && !list.isEmpty()) {
            groNumber = (String) list.get(0);
        }
        return groNumber;
    }
    /**
     * @Desc : Get DODQCISTMapping
     * @param JSONObject parameters
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getDODQCISTMapping(JSONObject json) throws ServiceException {
        List params = new ArrayList();
        StringBuilder condition = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(json.optString("istRequestId"))) {
            condition.append("where dodqcistmapping.qcInterStoreTransferRequest.id=?");
            params.add(json.optString("istRequestId"));
        }
        String query = "select dodqcistmapping from DODQCISTMapping dodqcistmapping " + condition.toString();
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "Get DODQCISTMapping Details.", "", list, list.size());
    }
    /**
     * @Desc : Get Rejected DODQCISTMapping
     * @param JSONObject parameters
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getRejectedDODQCISTMapping(JSONObject json) throws ServiceException {
        List params = new ArrayList();
        StringBuilder condition = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(json.optString("istRequestId"))) {
            condition.append("where rejecteddodqcistmapping.repairInterStoreTransferRequest.id=?");
            params.add(json.optString("istRequestId"));
        }
        String query = "select rejecteddodqcistmapping from RejectedDODQCISTMapping rejecteddodqcistmapping " + condition.toString();
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "Get RejectedDODQCISTMapping Details.", "", list, list.size());
    }
//    /**
//     * Save or Update Inspection Form
//     * @param params
//     * @return
//     * @throws ServiceException 
//     */
//    @Override
//    public KwlReturnObject saveOrUpdateInspectionForm(JSONObject params) throws ServiceException {
//        List list = new ArrayList();
//        try{
//            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            String inspectionDate = params.optString("inspectionDate", "");
//            String modelName = params.optString("modelName", "");
//            String consignmentReturnNo = params.optString("consignmentReturnNo", "");
//            String department = params.optString("department", "");
//            String customerName = params.optString("customerName", "");
//            String inspectionFormId = params.optString("inspectionFormId", "");
//            Date inspDate = df.parse(inspectionDate);
//            //Load Inspection Form Object
//            InspectionForm inspectionForm = null;
//            if(StringUtil.isNullOrEmpty(inspectionFormId)){
//                inspectionForm = new InspectionForm();
//            } else{
//                inspectionForm = (InspectionForm) get(InspectionForm.class, inspectionFormId);
//            }
//            inspectionForm.setInspectionDate(inspDate);
//            inspectionForm.setCustomerName(customerName);
//            inspectionForm.setDepartment(department);
//            inspectionForm.setConsignmentReturnNo(consignmentReturnNo);
//            inspectionForm.setModelName(modelName);
//            
//            saveOrUpdate(inspectionForm);
//            list.add(inspectionForm);
//        } catch (Exception ex) {
//            throw ServiceException.FAILURE("saveOrUpdateInspectionForm : " + ex.getMessage(), ex);
//        }
//        return new KwlReturnObject(true, "Save Inspection Form.", "", list, list.size());
//    }
//    /**
//     * Delete Inspection Form Details
//     * @param InspectionFormId
//     * @return
//     * @throws ServiceException 
//     */
//    @Override
//    public KwlReturnObject deleteInspectionFormDetails(String InspectionFormId) throws ServiceException {
//        String delQuery = "delete from in_inspection_form_details where inspectionformid = ?";
//        int numRows = executeSQLUpdate( delQuery, new Object[]{InspectionFormId});
//
//        return new KwlReturnObject(true, "", null, null, numRows);
//    }
//    /**
//     * Save Inspection Form Details
//     * @param inspectionFormDetailsMap
//     * @return
//     * @throws ServiceException 
//     */
//    @Override
//    public KwlReturnObject saveInspectionFormDetails(HashMap<String, Object> inspectionFormDetailsMap) throws ServiceException {
//        List list = new ArrayList();
//        try {
//            InspectionFormDetails inspectionFormDetails = new InspectionFormDetails();
//            if (inspectionFormDetailsMap.containsKey("id")) {
//                inspectionFormDetails = inspectionFormDetailsMap.get("id") == null ? null : (InspectionFormDetails) get(InspectionFormDetails.class, (String) inspectionFormDetailsMap.get("id"));
//            }
//            if(inspectionFormDetailsMap.containsKey("inspectionFormId")){
//                InspectionForm inspectionForm = inspectionFormDetailsMap.get("inspectionFormId") == null ? null : (InspectionForm) get(InspectionForm.class, (String) inspectionFormDetailsMap.get("inspectionFormId"));
//                inspectionFormDetails.setInspectionForm(inspectionForm);
//            }
//            if(inspectionFormDetailsMap.containsKey("areaId")){
//                InspectionArea inspectionArea = inspectionFormDetailsMap.get("areaId") == null ? null : (InspectionArea) get(InspectionArea.class, (String) inspectionFormDetailsMap.get("areaId"));
//                inspectionFormDetails.setInspectionArea(inspectionArea);
//            }
//            if(inspectionFormDetailsMap.containsKey("areaName")){
//                inspectionFormDetails.setInspectionAreaValue((String) inspectionFormDetailsMap.get("areaName"));
//            }
//            if(inspectionFormDetailsMap.containsKey("status")){
//                inspectionFormDetails.setInspectionStatus((String) inspectionFormDetailsMap.get("status"));
//            }
//            if(inspectionFormDetailsMap.containsKey("faults")){
//                inspectionFormDetails.setFaults((String) inspectionFormDetailsMap.get("faults"));
//            }
//            
//            save(inspectionFormDetails);
//            list.add(inspectionFormDetails);
//        } catch (Exception e) {
//            throw ServiceException.FAILURE("saveInspectionFormDetails : " + e.getMessage(), e);
//        }
//        return new KwlReturnObject(true, "Inspection Form Deails has been added successfully", null, list, list.size());
//    }
    
    @Override
    public void updateDeliveryOrderStatus(JSONObject params) throws ServiceException {
        if (!StringUtil.isNullOrEmpty(params.optString("dodetailid")) && !StringUtil.isNullOrEmpty(params.optString(Constants.companyKey))) {
            String sqlQuery = "update deliveryorder do inner join dodetails dod on do.id = dod.deliveryorder set do.status = ? where do.company = ? and dod.id = ?";
            executeSQLUpdate(sqlQuery, new Object[]{params.optString("statusID"), params.optString(Constants.companyKey), params.optString("dodetailid")});
        }
    }
    
     @Override
    public boolean isSerialExistsinDO(Product product, String batchName, String serialName) throws ServiceException {
        boolean exists = false;
        String sql = "select nbs.id from newproductbatch npb inner join newbatchserial nbs on (npb.id=nbs.batch) "
                + " inner join serialdocumentmapping sdm on (nbs.id=sdm.serialid) "
                + " inner join dodetails dod on (sdm.documentid=dod.id) "
                + " inner join deliveryorder do on (do.id=dod.deliveryorder) "
                + " where npb.batchname=? and nbs.serialname =? and npb.company=? and npb.location is null "
                + " and npb.product=? and do.isconsignment='T' and nbs.quantitydue=1 and do.deleteflag='F' and do.isdoclosed='F' ";
        List params = new ArrayList();
        params.add(batchName);
        params.add(serialName);
        params.add(product.getCompany());
        params.add(product.getID());
        List list = executeSQLQuery( sql, params.toArray());
        if (!list.isEmpty()) {
            exists = true;
        }
        return exists;
    }
     public List getAllAssetStockDetailList(HashMap<String, Object> jsonRequestParam, Paging paging) {
         
        String companyId = jsonRequestParam.containsKey("company") ? (String) jsonRequestParam.get("company") : "";
        String location = jsonRequestParam.containsKey("locationid") ? (String) jsonRequestParam.get("locationid") : "";
        String ss = (jsonRequestParam.containsKey("ss") ? jsonRequestParam.get("ss").toString() : null);
        String storeId = (jsonRequestParam.containsKey("Customer") ? jsonRequestParam.get("Customer").toString() : "");
        List list = new ArrayList();
        if (!StringUtil.isNullOrEmpty(companyId)) {
            companyId = (String) jsonRequestParam.get("company");
        }
        Set<Store> storeSet = (Set<Store>) (jsonRequestParam.containsKey("storeset") ? jsonRequestParam.get("storeset") : null);
        List params = new ArrayList();
        StringBuilder storeIn = new StringBuilder(" AND iw.id IN ( ");
        Store store = null;
        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;

            for (Store str : storeSet) {
                if (storeSet.size() == 1) {
                    store = str;
                }
                if (first) {
                    storeIn.append("'").append(str.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(str.getId()).append("'");
                }
            }
            storeIn.append(")");
        }
        StringBuilder sql = new StringBuilder("");
         if (!("Customer".equals(storeId))) {
             sql.append("SELECT ' ' AS donumber,p.productid,p.description,nb.batchname AS Batchname,sr.serialname,sr.skufield, ");
             sql.append(" iw.`name` AS warehouse,lc.`name` AS location,' ' AS customer,sr.quantitydue as qty,p.name,' ' as orderdate,' ' AS loanfrmdate,");
             sql.append("' ' AS loantodate,' ' AS cid,' ' AS soid,p.id,' ' as wid,' ' as lid,sr.id as serailid,nb.expdate,p.isBatchForProduct,p.isSerialForProduct ");
//            sql.append("'-' AS loantodate,'-' AS cid,'-' AS soid, sr.id ");
             sql.append(" FROM newbatchserial sr ");
             sql.append(" INNER JOIN product p ON p.id=sr.product AND p.company=sr.company ");
             sql.append(" INNER JOIN newproductbatch nb ON nb.id=sr.batch AND sr.product=nb.product");
             sql.append(" INNER JOIN inventorywarehouse iw ON nb.warehouse=iw.id");
             sql.append(" INNER JOIN in_location lc ON lc.id=nb.location");
             sql.append(" WHERE p.company=? AND p.isSerialForProduct='T' AND sr.quantitydue=1 AND nb.quantitydue>0 ");
             sql.append(" AND nb.location IS not NULL  ");
             params.add(companyId);

             sql.append(storeIn);

            if (!StringUtil.isNullOrEmpty(location)) {
                sql.append(" AND nb.location='" + location + "' ");
            }
         }
         if ((storeSet != null && storeSet.size() > 1) || "Customer".equals(storeId)) {
            if (!StringUtil.isNullOrEmpty(sql.toString())) {
                sql.append(" UNION ALL ");
            }
            sql.append(" SELECT d.donumber,p.productid,p.description,nb.batchname AS Batchname,srl.serialname,srl.skufield,' ' AS warehouse,' ' AS location,");
            sql.append(" c.`name` AS customer,srl.quantitydue as qty,p.name,d.orderdate,DATE(sor.fromdate) AS loanfrmdate,DATE(sor.todate) AS loantodate,");
//              sql.append(" c.`name` AS customer,srl.quantitydue,p.name,d.orderdate,DATE(sor.fromdate) AS loanfrmdate,DATE(sor.todate) AS loantodate,c.id as cid,sor.id  as soid , srl.id ");
            sql.append(" c.id as cid,sor.id soid,p.id,sor.requestwarehouse as wid,sor.requestlocation as lid, srl.id as serailid,nb.expdate,p.isBatchForProduct,p.isSerialForProduct ");
            sql.append(" FROM deliveryorder d ");
            sql.append(" INNER JOIN dodetails dtl ON dtl.deliveryorder=d.id ");
            sql.append(" inner JOIN product p ON p.id=dtl.product ");
            sql.append(" INNER JOIN serialdocumentmapping srmp ON srmp.documentid=dtl.id AND srmp.transactiontype=28 ");
            sql.append(" INNER JOIN newbatchserial srl ON srl.id=srmp.serialid ");
            sql.append(" INNER JOIN newproductbatch nb ON nb.id=srl.batch AND srl.product=nb.product");
            sql.append(" INNER JOIN inventorywarehouse iw ON nb.warehouse=iw.id ");
            sql.append(" INNER JOIN customer c ON c.id=d.customer ");
            sql.append(" LEFT JOIN sodetails sodtl ON sodtl.id=dtl.sodetails ");
            sql.append(" LEFT JOIN salesorder sor ON sor.id=sodtl.salesorder ");
            
            sql.append(" WHERE d.isdoclosed='F' AND dtl.islineitemclosed='F' AND d.deleteflag='F' AND isopeninsr = 'T'  AND d.company=? AND srl.quantitydue=1   AND p.isSerialForProduct='T' ");
            params.add(companyId);
            }
         if (!("Customer".equals(storeId))) {
             if (!StringUtil.isNullOrEmpty(sql.toString())) {
                 sql.append(" UNION ALL ");
             }
             sql.append(" SELECT ' ' AS donumber,p.productid,p.description,nb.batchname AS Batchname,' ' AS serialname,' ' AS skufield,iw.`name` AS warehouse,");
             sql.append(" lc.`name` AS location,' ' AS customer,nb.quantitydue  as qty,p.name,' ' as orderdate,' ' AS loanfrmdate,' ' AS loantodate,");
             sql.append(" ' ' AS cid,' ' AS soid,p.id,' ' as wid,' ' as lid, null as serailid,nb.expdate,p.isBatchForProduct,p.isSerialForProduct  ");
             sql.append(" FROM newproductbatch nb");
             sql.append(" INNER JOIN product p ON p.id=nb.product AND p.company=nb.company ");
             sql.append(" INNER JOIN inventorywarehouse iw ON nb.warehouse=iw.id ");
             sql.append(" INNER JOIN in_location lc ON lc.id=nb.location ");
             sql.append(" WHERE p.company=? AND p.isSerialForProduct='F' AND nb.quantitydue>0  AND nb.location IS not NULL  ");
             params.add(companyId);
             
             sql.append(storeIn);
             if (!StringUtil.isNullOrEmpty(location)) {
                 sql.append(" AND nb.location='" + location + "' ");
         }
         }
        if ((storeSet != null && storeSet.size() > 1) || "Customer".equals(storeId)) {
            if (!StringUtil.isNullOrEmpty(sql.toString())) {
                sql.append(" UNION ALL ");
            }
            sql.append(" SELECT d.donumber,p.productid,p.description,nb.batchname AS Batchname,' ' AS serialname,' ' AS skufield,' ' AS warehouse,' ' AS location,c.`name` AS customer,");
            sql.append(" (srmp.quantity-IFNULL(SUM(lvm.quantity),0)) as qty,p.name,d.orderdate,DATE(sor.fromdate) AS loanfrmdate,DATE(sor.todate) AS loantodate,");
            sql.append("c.id as cid,sor.id soid,p.id ,sor.requestwarehouse as wid,sor.requestlocation as lid,null as serailid,nb.expdate,p.isBatchForProduct,p.isSerialForProduct   ");
            sql.append(" FROM deliveryorder d  ");
            sql.append(" INNER JOIN dodetails dtl ON dtl.deliveryorder=d.id ");
            sql.append(" LEFT JOIN srdetails srdl ON srdl.dodetails=dtl.id ");
            sql.append(" inner JOIN product p ON p.id=dtl.product ");
            sql.append(" INNER JOIN locationbatchdocumentmapping srmp ON srmp.documentid=dtl.id AND srmp.transactiontype=28  ");
            sql.append(" LEFT JOIN locationbatchdocumentmapping lvm ON lvm.documentid=srdl.id AND srmp.batchmapid=lvm.batchmapid ");
            sql.append(" INNER JOIN newproductbatch nb ON nb.id=srmp.batchmapid ");
            sql.append(" INNER JOIN inventorywarehouse iw ON nb.warehouse=iw.id ");
            sql.append(" INNER JOIN customer c ON c.id=d.customer ");
            sql.append(" LEFT JOIN sodetails sodtl ON sodtl.id=dtl.sodetails ");
            sql.append(" LEFT JOIN salesorder sor ON sor.id=sodtl.salesorder ");
            sql.append(" WHERE d.isdoclosed='F' AND dtl.islineitemclosed='F' AND d.deleteflag='F'  AND isopeninsr = 'T'  AND d.company=? AND nb.quantitydue>0 AND p.isSerialForProduct='F'");
//            sql.append(" AND (srmp.quantity-IFNULL(srdl.baseuomquantity,0))>0 GROUP BY d.donumber,srmp.batchmapid ");
            sql.append("   GROUP BY d.donumber,dtl.id,srmp.batchmapid ");
            params.add(companyId);
        }
        
         StringBuilder query = new StringBuilder(" SELECT * FROM (");
         query.append(sql);
         query.append(" ) AS tb where tb.qty>0 ");

        if (!StringUtil.isNullOrEmpty(ss)) {
            query.append(" and (tb.productid LIKE ? OR tb.name LIKE ? OR tb.donumber LIKE ? OR tb.batchname LIKE ? ) ");
            params.add("%" + ss + "%");
            params.add("%" + ss + "%");
            params.add("%" + ss + "%");
            params.add("%" + ss + "%");
        }
        query.append(" ORDER BY productid ");
        try {
            list = executeSQLQuery(query.toString(), params.toArray());
            int totalCount = list.size();
            if (paging != null) {
                paging.setTotalRecord(totalCount);
                list = executeSQLQueryPaging(query.toString(), params.toArray(), paging);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(StockDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }
     
     @Override
      public List getbatchserialdetails(HashMap<String, String> batchserialMap,TransactionModule transModule,Product product) throws ServiceException {
        List detailList=new ArrayList<>();
         String documentid = "";
        String serialids = "";
        String batchname = "";
        String serialname = "";
        String warehouse = "";
        String location = "";
        String row = "";
        String rack = "";
        String bin = "";
        Date expdate = null;
        String query = null;
        String repairstoreflag=null;
        ArrayList params = new ArrayList();
        
        repairstoreflag=batchserialMap.containsKey("fromrepair")?batchserialMap.get("fromrepair"):"";
        documentid=batchserialMap.containsKey("moduleid")?batchserialMap.get("moduleid"):"";
        serialids = batchserialMap.containsKey("seriallist")?batchserialMap.get("seriallist"):"";
        
         if ((transModule.equals(TransactionModule.ERP_DO) || transModule.equals(TransactionModule.ERP_Consignment_DO)) && !StringUtil.isNullOrEmpty(documentid)) {
             params.add(documentid);
             if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(serialids)) {
                 query = "select nb.serialname,se.exptodate,nb.skufield from serialdocumentmapping se "
                         + " left join newbatchserial nb on se.serialid=nb.id "
                         + " where documentid = ? and serialid in('"+ serialids +"')";

             }
             else {
                 String condition = "";
                 if (batchserialMap.containsKey("batchname")) {
                    batchname = batchserialMap.get("batchname");
                    batchname = batchname.replaceAll("'", "''");  //ERP-37766 batchname contions ' because of that unable to execute Query
                    batchname = batchname.replace("\\", "\\\\");
                      if (product.isIsBatchForProduct() && !StringUtil.isNullOrEmpty(batchname)) {
                      condition = " and n.batchname = ?";
                      params.add(batchname);
                 }                    
                 }                
                 query = "select distinct ' ' as serialname,l.expdate,' ' as skufield from locationbatchdocumentmapping l "
                         + " left join newproductbatch n on l.batchmapid=n.id"
                         + " where l.documentid = ?" + condition;
             }

         } else if (StringUtil.isNullOrEmpty(serialids)
                 && repairstoreflag.equalsIgnoreCase("1")
                 && batchserialMap.containsKey("serialname")
                 && !StringUtil.isNullOrEmpty(batchserialMap.get("serialname"))) {
             params.add(product.getID());
             if (batchserialMap.containsKey("serialname")) {
                 serialname = batchserialMap.get("serialname");
                 if (product.isIsSerialForProduct() && !StringUtil.isNullOrEmpty(serialname)) {
                     query = "select distinct sr.serialname,sr.exptodate,sr.skufield "
                             + "from newbatchserial sr where product=? and serialname in ('" + serialname + "')";
                 }
             }
         }
         else if(StringUtil.isNullOrEmpty(serialids) && StringUtil.isNullOrEmpty(batchserialMap.get("serialname"))){
             String condition = "";
             params.add(product.getID());
             if (batchserialMap.containsKey("warehouse")) {
                 warehouse = batchserialMap.get("warehouse");
                 if(!StringUtil.isNullOrEmpty(warehouse)){
                    params.add(warehouse); 
                    condition+=" and nb.warehouse=?";
                 }                 
             }
             if (batchserialMap.containsKey("location")) {
                 location = batchserialMap.get("location");
                  if(!StringUtil.isNullOrEmpty(location)){
                    params.add(location); 
                    condition+=" and nb.location=?";
                 }                
             }
             if (batchserialMap.containsKey("row")) {
                 row = batchserialMap.get("row");
                 if(!StringUtil.isNullOrEmpty(row)){
                    params.add(row); 
                    condition+=" and nb.row=?";
                 }                 
             }
             if (batchserialMap.containsKey("rack")) {
                 rack = batchserialMap.get("rack");
                  if(!StringUtil.isNullOrEmpty(rack)){
                    params.add(rack); 
                    condition+=" and nb.rack=?";
                 }                
             }
             if (batchserialMap.containsKey("bin")) {
                 bin = batchserialMap.get("bin");
                 if(!StringUtil.isNullOrEmpty(bin)){
                    params.add(bin); 
                    condition+=" and nb.bin=?";
                 }
             }            
             if (product.isIsBatchForProduct()) {
                 batchname = batchserialMap.get("batchname");
                 batchname = batchname.replaceAll("'", "''");            //ERP-37766
                 batchname = batchname.replace("\\", "\\\\");
                 if(!StringUtil.isNullOrEmpty(batchname)){
                   condition += " and nb.batchname='" + batchname + "'";  
                 }                 
             }             
             query = "select distinct ' ' as serialname,nb.expdate,' ' as skufield from newproductbatch nb "
                     + " where nb.product=?" + condition;
                   
             
         }else if(!StringUtil.isNullOrEmpty(serialids)){
             
             query = "select sr.serialname,sr.exptodate,sr.skufield "
                     + "from newbatchserial sr where id in ('"+ serialids +"')";
             
         } else if (!StringUtil.isNullOrEmpty(batchserialMap.get("serialname"))) {
             String condition = "";
             String appendserial = "";
             params.add(product.getID());
             serialname = batchserialMap.get("serialname");
             if(transModule.equals(TransactionModule.STOCK_ADJUSTMENT) && !StringUtil.isNullOrEmpty(serialname)){
                 StringBuilder Serial = new StringBuilder();                 
                 String[] serials = serialname.split(",");
                 for (int i = 0; i < serials.length; i++) {
                     Serial.append('"' + serials[i] + '"' + ",");
                 }
                 serialname = Serial.substring(0, Serial.length() - 1);
             }             
             if (product.isIsBatchForProduct()) {
                 batchname = batchserialMap.get("batchname");
                 batchname = batchname.replaceAll("'", "''");    //ERP-37766
                 batchname = batchname.replace("\\", "\\\\");
                 if (!StringUtil.isNullOrEmpty(batchname)) {
                     condition += " and nb.batchname='" + batchname + "'";
                 }
             }
             if (batchserialMap.containsKey("warehouse")) {
                 warehouse = batchserialMap.get("warehouse");
                 if (!StringUtil.isNullOrEmpty(warehouse)) {
                     params.add(warehouse);
                     condition += " and nb.warehouse=?";
                 }
             }
             if (batchserialMap.containsKey("location")) {
                 location = batchserialMap.get("location");
                 if (!StringUtil.isNullOrEmpty(location)) {
                     params.add(location);
                     condition += " and nb.location=?";
                 }
             }
             if (batchserialMap.containsKey("row")) {
                 row = batchserialMap.get("row");
                 if (!StringUtil.isNullOrEmpty(row)) {
                     params.add(row);
                     condition += " and nb.row=?";
                 }
             }
             if (batchserialMap.containsKey("rack")) {
                 rack = batchserialMap.get("rack");
                 if (!StringUtil.isNullOrEmpty(rack)) {
                     params.add(rack);
                     condition += " and nb.rack=?";
                 }
             }
             if (batchserialMap.containsKey("bin")) {
                 bin = batchserialMap.get("bin");
                 if (!StringUtil.isNullOrEmpty(bin)) {
                     params.add(bin);
                     condition += " and nb.bin=?";
                 }
             }
             if(transModule.equals(TransactionModule.STOCK_ADJUSTMENT)){
               appendserial = "(" + serialname + ")";
             }else{
             appendserial = "('" + serialname + "')";
             }   
             query = "select sr.serialname,sr.exptodate,sr.skufield "
                     + "from newbatchserial sr inner join newproductbatch nb on sr.batch=nb.id where nb.product=? and sr.serialname in "+ appendserial + condition;
         }       
        List list = executeSQLQuery(query, params.toArray());
        
        String serial="";
        String serialS="";
        String expDate="";
        String expDates = "";
        String skuFields="";
        String skuField="";
        
         if (list != null && !list.isEmpty() && list.size() > 0) {
             for (int i = 0; i < list.size(); i++) {
                 serial = "";
                 expDate = "";
                 skuField = "";
                 if (list.get(i) != null) {
                    {
                         Object[] obj = (Object[]) list.get(i);
                         serial = (obj[0] != null) ? obj[0].toString() : "";
                         serialS += serial + ",";
                         expDate = (obj[1] != null ? obj[1].toString() : "");
                         expDates += expDate + ",";
                         skuField = (obj[2] != null) ? obj[2].toString() : "";
                         skuFields += skuField + ",";

                     } 
                 }
             }
         }
        if (transModule.equals(TransactionModule.INTER_STORE_TRANSFER)){
            String expdatecondition="";
            ArrayList moduleid_params = new ArrayList();              
                moduleid_params.add(documentid);
                if(!StringUtil.isNullOrEmpty(batchname)){
                   expdatecondition += " and batchname='" + batchname + "'";  
                 }                 
            String expdatequery="select expdate from in_ist_detail where istrequest=?"+expdatecondition;
            List expdates = executeSQLQuery(expdatequery, moduleid_params.toArray());
            if (expdates != null && !expdates.isEmpty() && expdates.size() > 0) {
                expDates="";
                for (int i = 0; i < expdates.size(); i++) {
                    if (expdates.get(i) != null) {
                       {
                            Object obj = (Object) expdates.get(i);
                            expDate = (obj != null ? obj.toString() : "");
                            expDates += expDate + ",";
                       } 
                    }
                }
            }
        }
        
        detailList.add(serialS);
        detailList.add(expDates);
        detailList.add(skuFields);

        return detailList;
    }

    @Override
    public List<String> getbatchidFromDocumentId(String documentId) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList param = new ArrayList();
//            param.add(documentId);
            String selQuery = " SELECT batchmapid FROM locationbatchdocumentmapping WHERE documentid='"+documentId+"' ";
            List expdates = executeSQLQuery(selQuery);
            if(expdates!=null && !expdates.isEmpty()){
                  Iterator itr=expdates.iterator();
                  while(itr.hasNext()){
                      list.add(itr.next());
                  }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Error while processing", ex);
        }
        return list;
    }
    }