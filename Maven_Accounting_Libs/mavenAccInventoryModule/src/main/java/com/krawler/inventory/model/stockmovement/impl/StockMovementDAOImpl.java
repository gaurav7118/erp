/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockmovement.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.NewBatchSerial;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.Producttype;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.inventory.model.stockmovement.StockMovementDAO;
import com.krawler.inventory.model.stockmovement.StockMovementDetail;
import com.krawler.inventory.model.stockmovement.TransactionType;
import com.krawler.inventory.model.store.Store;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin gupta
 */
public class StockMovementDAOImpl extends BaseDAO implements StockMovementDAO {

    @Override
    public StockMovement getStockMovement(String stockMovementId) throws ServiceException {
        return (StockMovement) get(StockMovement.class, stockMovementId);
    }

    @Override
    public void saveOrUpdate(Object object) throws ServiceException {
        super.saveOrUpdate(object);
    }

    @Override
    public void delete(Object object) throws ServiceException {
        super.delete(object);
    }

    @Override
    public List<StockMovement> getStockMovementList(Company company, Store store, Date fromDate, Date toDate, TransactionType transactionType, String searchString, Paging paging) throws ServiceException {
        StringBuilder hqlForProduct = new StringBuilder("SELECT distinct product from StockMovement sm WHERE sm.company = ? AND sm.store = ? AND Date(sm.transactionDate) >= ? AND Date(sm.transactionDate) <= ? ORDER BY sm.product.productid ASC, sm.createdOn ASC");
        List<Product> productList = null;
        List params = new ArrayList();
        params.add(company);
        params.add(store);
        params.add(fromDate);
        params.add(toDate);
        if (paging != null) {
            productList = executeQuery( hqlForProduct.toString(), params.toArray());
            int totalCount = productList.size();
            paging.setTotalRecord(totalCount);
            productList = executeQueryPaging( hqlForProduct.toString(), params.toArray(), paging);
        }
        StringBuilder hql = new StringBuilder("SELECT distinct sm FROM StockMovement sm left join sm.stockMovementDetails smd WHERE sm.company = ? AND sm.store = ? AND Date(sm.transactionDate) >= ? AND Date(sm.transactionDate) <= ? ");



        if (productList != null && !productList.isEmpty() && StringUtil.isNullOrEmpty(searchString)) {
            String PIds = "";
            StringBuilder productIn = new StringBuilder(" AND sm.product.ID in ( ");
            boolean first = true;
            for (Product p : productList) {
                if (first) {
                    productIn.append("'").append(p.getID()).append("'");
                    first = false;
                } else {
                    productIn.append(",").append("'").append(p.getID()).append("'");
                }
            }
            productIn.append(")");
            hql.append(productIn);
        }

        if (transactionType != null) {
            hql.append("AND sm.transactionType = ? ");
            params.add(transactionType);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND ( sm.product.productid LIKE ? OR sm.product.name LIKE ? OR smd.serialNames LIKE ? OR sm.transactionNo LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY sm.product.productid ASC, sm.createdOn ASC");
        List list = executeQuery( hql.toString(), params.toArray());
//        int totalCount = list.size();
//        if (paging != null) {
//            paging.setTotalRecord(totalCount);
//            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
//        }
        return list;

    }

    @Override
    public List<StockMovementDetail> getDetailedStockMovementList(Company company, Store store,Location location, Date fromDate, Date toDate, TransactionType transactionType, String searchString, Paging paging,String productId) throws ServiceException {
        
        List params = new ArrayList();
        params.add(company);
        String storeCondition = "";
        if (store != null) {
            params.add(store);
            storeCondition = " AND stockMovement.store = ? ";
        }
        String locationCondition = "";
        if (location != null) {
            params.add(location);
            locationCondition = " AND location= ?";
        }
        StringBuilder hql = new StringBuilder("FROM StockMovementDetail  WHERE stockMovement.company = ? " + storeCondition + locationCondition + " AND Date(stockMovement.transactionDate) >= ? AND Date(stockMovement.transactionDate) <= ? AND stockMovement.product.asset=? ");
        params.add(fromDate);
        params.add(toDate);
        params.add(false);
        if (transactionType != null) {
            hql.append("AND stockMovement.transactionType = ? ");
            params.add(transactionType);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND ( stockMovement.product.productid LIKE ? OR stockMovement.product.name LIKE ? OR serialNames LIKE ? OR stockMovement.transactionNo LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        if(!StringUtil.isNullOrEmpty(productId)){
            hql.append(" AND ( stockMovement.product.ID = ?) ");
            params.add(productId);
        }
        
        hql.append(" and stockMovement.product.producttype.ID not in ('a839448c-7646-11e6-9648-14dda97925bd','a6a350c4-7646-11e6-9648-14dda97925bd') ");
        
        hql.append(" ORDER BY stockMovement.product.productid ASC, stockMovement.createdOn ASC");
        String countQuery = "SELECT COUNT(*) " + hql.toString();
        List countList = executeQuery( countQuery, params.toArray());
        int totalCount = 0;
        if (!countList.isEmpty()) {
            Long c = countList.get(0) != null ? (Long) countList.get(0) : 0;
            totalCount = c.intValue();
        }
        List list = new ArrayList();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            if (paging.isValid()) {
                list = executeQueryPaging( hql.toString(), params.toArray(), paging);
            }
        } else {
            list = executeQuery( hql.toString(), params.toArray());
        }
        return list;

    }

    @Override
    public List<StockMovement> getStockMovementListByReferenceId(Company company, String moduleRefId) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockMovement WHERE company = ? AND moduleRefId = ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(moduleRefId);

        List list = executeQuery( hql.toString(), params.toArray());

        return list;
    }
    
    @Override
    public List<StockMovement> getStockMovementListOfISTReturnRequest(Company company, String moduleRefId) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockMovement WHERE company = ? AND moduleRefId IN (SELECT id from InterStoreTransferRequest where parentid=? ) ");
        List params = new ArrayList();
        params.add(company);
        params.add(moduleRefId);

        List list = executeQuery( hql.toString(), params.toArray());

        return list;
    }
    
    @Override
    public List<StockMovement> getStockMovementListById(Company company, String moduleRefId) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockMovement WHERE company = ? AND moduleRefId in (SELECT moduleRefId FROM StockMovement WHERE id = ?) ");
        List params = new ArrayList();
        params.add(company);
        params.add(moduleRefId);

        List list = executeQuery( hql.toString(), params.toArray());

        return list;
    }
    
    @Override
    public List<StockMovement> getStockMovementListByReferenceIdForWorkOrder(Company company, String modulerefdetailid) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockMovement WHERE company = ? AND moduleRefDetailId = ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(modulerefdetailid);

        List list = executeQuery( hql.toString(), params.toArray());

        return list;
    }

    @Override
    public Map<String, Integer> getTransactionWiseSerialUsedCount(String moduleRefId, Set<NewBatchSerial> batchSerialSet) throws ServiceException {

        Map<String, Integer> map = new HashMap<String, Integer>();
        List dataList = new ArrayList();
        String fetchDateQry = " SELECT serialid,reusablecount FROM serialdocumentmapping WHERE documentid IN(SELECT  id from srdetails WHERE salesreturn=? And product=?) ";
        List params = new ArrayList();
        params.add(moduleRefId);
        String serials = null;
        if (batchSerialSet != null && !batchSerialSet.isEmpty()) {
            boolean first = true;
            for (NewBatchSerial batchSerial : batchSerialSet) {
                if (first) {
                    serials = "'" + batchSerial.getId() + "'";
                    first = false;
                    params.add(batchSerial.getProduct());
                } else {
                    serials += ",'" + batchSerial.getId() + "'";
                }
            }
        }
        if (!StringUtil.isNullOrEmpty(serials)) {
            fetchDateQry += " AND serialid IN (" + serials + ")";
        }

        dataList = executeSQLQuery( fetchDateQry, params.toArray());
        Iterator itr = dataList.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            String serialid = (String) objs[0];
            int val = ((Double) objs[1]).intValue();
            Integer usedCount = val;
            map.put(serialid, usedCount);

        }
        return map;
    }
    @Override
    public Store getOriginalStoreFromQAStore(String transactionNo, Product product) throws ServiceException{
        
        List dataList = new ArrayList();
        Store store=null;
        String fetchDateQry = " SELECT store FROM  Consignment where transactionNo=? and product=? ";
        List params = new ArrayList();
        params.add(transactionNo);
        params.add(product);
       
        dataList = executeQuery( fetchDateQry, params.toArray());
        Iterator itr = dataList.iterator();
        while (itr.hasNext()) {
            store = (Store) itr.next();
//            store = (Store) objs[0];
        }
        return store;
        
    }
    
    @Override
    public List<Product> getAllProductsForStockMovement(Company company, Store store, Date fromDate, Date toDate, TransactionType transactionType, String searchString, Paging paging) throws ServiceException {
        StringBuilder hqlForProduct = new StringBuilder();
        List params = new ArrayList();
        hqlForProduct.append("SELECT DISTINCT id FROM ( ");
        hqlForProduct.append(" SELECT id, productid FROM product WHERE company = ? AND isasset=0  ");
        params.add(company);
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hqlForProduct.append(" AND (productid LIKE ? OR name LIKE ? ) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        
        //Only Loading assembly, Inventory and Inventory Non Sale Items in Report.
        hqlForProduct.append(" and product.producttype IN (?,?,? ) ");
        params.add(Producttype.ASSEMBLY);
        params.add(Producttype.INVENTORY_PART);
        params.add(Producttype.Inventory_Non_Sales);
        
        hqlForProduct.append(" UNION ");
        hqlForProduct.append(" SELECT sm.product, p.productid FROM in_stockmovement sm ");
        hqlForProduct.append(" INNER JOIN product p ON sm.product = p.id ");
        hqlForProduct.append(" INNER JOIN in_sm_detail smd  ON smd.stockmovement = sm.id ");
        if(store!=null){
            hqlForProduct.append(" WHERE sm.company = ? AND sm.store = ? AND DATE(sm.transaction_date) >= ? AND DATE(sm.transaction_date) <= ? ");
            params.add(company.getCompanyID());
            params.add(store.getId());
            params.add(fromDate);
            params.add(toDate);
        }else{
            hqlForProduct.append(" WHERE sm.company = ? AND DATE(sm.transaction_date) >= ? AND DATE(sm.transaction_date) <= ? ");
            params.add(company.getCompanyID());
            params.add(fromDate);
            params.add(toDate);
        }
        if (transactionType != null) {
            hqlForProduct.append(" AND transaction_type = ? ");
            params.add(transactionType.ordinal());
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hqlForProduct.append(" AND (sm.transactionno LIKE ? OR smd.serialnames  LIKE ?) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        //Only Loading assembly, Inventory and Inventory Non Sale Items in Report.
            hqlForProduct.append(" and p.producttype IN (?,?,? ) ");
            params.add(Producttype.ASSEMBLY );    
            params.add(Producttype.INVENTORY_PART);    
            params.add(Producttype.Inventory_Non_Sales);
            
        hqlForProduct.append(") ut ORDER BY ut.productid");

        List productList = executeSQLQuery( hqlForProduct.toString(), params.toArray());
        if (paging != null) {
            int totalCount = productList.size();
            paging.setTotalRecord(totalCount);
            if (paging.isValid() && totalCount > paging.getLimit()) {
                productList = executeSQLQueryPaging( hqlForProduct.toString(), params.toArray(), paging);
            }
        }
        List<Product> list = new ArrayList();
        for (Object obj : productList) {
            String productId = (String) obj;
            Product product = (Product) get(Product.class, productId);
            list.add(product);
        }
        return list;
    }

    @Override
    public List<StockMovement> getStockMovementByProductList(Company company, List<Product> productList, Store store, Date fromDate, Date toDate, TransactionType transactionType,String searchString) throws ServiceException {
        List list = new ArrayList();
        if (productList != null && !productList.isEmpty()) {
            List params = new ArrayList();
            StringBuilder hql=null;
            params.add(company);
            if(store!=null){
                 hql = new StringBuilder("SELECT distinct sm FROM StockMovement sm left join sm.stockMovementDetails smd WHERE sm.company = ? AND sm.store = ? AND Date(sm.transactionDate) >= ? AND Date(sm.transactionDate) <= ? ");
                params.add(store);
            }else{
                 hql = new StringBuilder("SELECT distinct sm FROM StockMovement sm left join sm.stockMovementDetails smd WHERE sm.company = ? AND Date(sm.transactionDate) >= ? AND Date(sm.transactionDate) <= ? ");
            }
            params.add(fromDate);
            params.add(toDate);

            StringBuilder productIn = new StringBuilder(" AND sm.product.ID in ( ");
            boolean first = true;
            for (Product p : productList) {
                if (first) {
                    productIn.append("'").append(p.getID()).append("'");
                    first = false;
                } else {
                    productIn.append(",").append("'").append(p.getID()).append("'");
                }
            }
            productIn.append(")");
            hql.append(productIn);
            if (!StringUtil.isNullOrEmpty(searchString)) {
                hql.append(" AND smd.serialNames LIKE ? ");
                params.add("%" + searchString + "%");
            }
            if (transactionType != null) {
                hql.append(" AND sm.transactionType = ? ");
                params.add(transactionType);
            }
            hql.append(" ORDER BY sm.product.productid ASC, sm.createdOn ASC,sm.autoSeq ASC");
            list = executeQuery( hql.toString(), params.toArray());
        }
        return list;

    }
    
    @Override
    public List<StockMovement> getStockMovementByProduct(HashMap<String,Object> mpList) throws ServiceException {
        
        Company company = (Company) mpList.get("company");
        Product product = (Product) mpList.get("product");
        Date fromDate = (Date) mpList.get("date");
        String searchString = mpList.get("date").toString();
        Store store = (Store) mpList.get("store");
        String saID = (mpList.get("moduleid")!=null)?mpList.get("moduleid").toString():"";

        List list = new ArrayList();
        if (product != null) {
            StringBuilder hql = new StringBuilder("SELECT distinct sm FROM StockMovement sm left join sm.stockMovementDetails smd WHERE "
                    + " sm.company = ? AND sm.store = ? AND Date(sm.transactionDate) >= ?  ");
            List params = new ArrayList();
            params.add(company);
            params.add(store);
            params.add(fromDate);
//            params.add(saID);
            StringBuilder productIn = new StringBuilder(" AND sm.product.ID in ( ");
            productIn.append("'").append(product.getID()).append("'");
            productIn.append(")");
            hql.append(productIn);
            if (!StringUtil.isNullOrEmpty(searchString)) {
                hql.append(" AND smd.serialNames LIKE ? ");
                params.add("%" + searchString + "%");
            }
            
            if(!StringUtil.isNullOrEmpty(saID)){
                hql.append(" AND modulerefid <> ? ");
                params.add(saID);
            }
            
            hql.append(" ORDER BY sm.product.productid ASC, sm.createdOn ASC,sm.autoSeq ASC");
            list = executeQuery(hql.toString(), params.toArray());
        }
        return list;
    }
    
    
    @Override
    public List<StockMovement> getStockMovementByProduct(Company company, Product product, Store store, Date fromDate, String searchString, String saID) throws ServiceException {
       HashMap<String,Object> hm=new HashMap<>();
       hm.put("company", company);
       hm.put("product", product);
       hm.put("store", store);
       hm.put("date", fromDate);
       hm.put("ss", searchString);
       hm.put("moduleid", saID);
        return getStockMovementByProduct(hm);
    }
    @Override
    public void updateDOMemo(String doId, String memo)throws ServiceException{
        try {
            String query = " UPDATE in_stockmovement SET memo= ? WHERE modulerefid=? and transaction_type=2 ";
//            list = executeQuery( query, new Object[]{invoiceID});
            executeSQLUpdate(query, new Object[]{memo,doId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accInvoiceImpl.deleteInvoiceTermMap:" + ex.getMessage(), ex);
        }
    }
    
    @Override
    public String getDOdetailsremarks(String modulerefdetailid, String transactionmodule) {
        String query = "";
        String remark = "";
        List params = new ArrayList();
        List dodetails = new ArrayList();
        if (transactionmodule.equals("DO")) {
            query = "SELECT d.remark FROM dodetails d where d.id=?";
            params.add(modulerefdetailid);
        }
        if (transactionmodule.equals("SR")) {
            query = "  SELECT srd.remark FROM srdetails srd INNER JOIN salesreturn sr ON sr.id=srd.salesreturn "
                    + "  LEFT JOIN in_consignment cn ON cn.modulerefid=sr.id WHERE (srd.id=? or sr.id=?) ";
            params.add(modulerefdetailid);
            params.add(modulerefdetailid);
        }
        try {
            dodetails = executeSQLQuery(query, params.toArray());
            if (dodetails != null && dodetails.size() > 0) {
                remark = dodetails.get(0).toString();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(StockMovementDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return remark;

    }
    /**
     * Pass the parent Transaction id and check if it has any child/return transactions to update stock movement entries of those transactions
     * @param company
     * @param moduleRefId
     * @return List
     * @throws ServiceException 
     */
    @Override
    public List<StockMovement> getStockMovementListOfSRReturnRequest(Company company, String moduleRefId) throws ServiceException {
        String hql = "FROM StockMovement WHERE company = ? AND (moduleRefId IN (SELECT id from StockRequest where parentid=? ) OR moduleRefId = ? )";
        List params = new ArrayList();
        params.add(company);
        params.add(moduleRefId);
        params.add(moduleRefId);
        List list = executeQuery( hql, params.toArray());
        return list;
    }

}
