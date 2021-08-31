/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.ist.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class InterStoreTransferDAOImpl extends BaseDAO implements InterStoreTransferDAO {

    @Override
    public void saveOrUpdate(Object object) throws ServiceException {
        super.saveOrUpdate(object);
    }

    @Override
    public void delete(Object object) throws ServiceException {
        super.delete(object);
    }

    @Override
    public List<InterStoreTransferRequest> getInterStoreTransferList(Company company,Set<Store> fromStoreSet,Set<Store> toStoreSet, InterStoreTransferStatus[] statusList, Date fromDate, Date toDate, String searchString, Paging paging,String TZdiff,Map<String, Object> reqMap) throws ServiceException {
        StringBuilder hql = new StringBuilder("select distinct ist  FROM InterStoreTransferRequest ist left join ist.istDetails istd WHERE ist.company = ? AND ist.transactionModule=?");
        ArrayList params = new ArrayList();
        String searchJson = "";
        params.add(company);
        params.add(TransactionModule.INTER_STORE_TRANSFER);
         String mySearchFilterString = "";
        boolean isJobWorkOutRemain =false;
        if (reqMap.containsKey("searchJson") && reqMap.get("searchJson") != null) {
                searchJson = reqMap.get("searchJson").toString();
        }
        if (reqMap.containsKey("isJobWorkOutRemain") && reqMap.get("isJobWorkOutRemain") != null) {
                isJobWorkOutRemain = true;
        }
        String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
        if (reqMap.containsKey("filterConjuctionCriteria") && reqMap.get("filterConjuctionCriteria") != null) {
            if (reqMap.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                filterConjuctionCriteria = com.krawler.common.util.Constants.or;
            }
        }
        if (fromStoreSet != null && !fromStoreSet.isEmpty() && toStoreSet != null && !toStoreSet.isEmpty()) {
            boolean first = true;
            
            StringBuilder fromstoreIn = new StringBuilder("ist.fromStore.id IN ( ");
            Iterator<Store> itr = fromStoreSet.iterator();
            while (itr.hasNext()) {
                Store store = itr.next();
                if (first) {
                    fromstoreIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    fromstoreIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            fromstoreIn.append(")");
            first=true;
            
            StringBuilder tostoreIn = new StringBuilder(" ist.toStore.id IN ( ");
            Iterator<Store> itr1 = toStoreSet.iterator();
            while (itr1.hasNext()) {
                Store store = itr1.next();
                if (first) {
                    tostoreIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    tostoreIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            tostoreIn.append(")");
            
            hql.append(" AND ("+fromstoreIn+" OR "+tostoreIn+") ");
        }else if (fromStoreSet != null && !fromStoreSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND ist.fromStore.id IN ( ");
            Iterator<Store> itr = fromStoreSet.iterator();
            while (itr.hasNext()) {
                Store store = itr.next();
                if (first) {
                    storeIn.append("'").append(store.getId()).append("'");
                    first = false;
                } else {
                    storeIn.append(",").append("'").append(store.getId()).append("'");
                }
            }
            storeIn.append(")");
            hql.append(storeIn);
        }else  if (toStoreSet != null && !toStoreSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND ist.toStore.id IN ( ");
            Iterator<Store> itr = toStoreSet.iterator();
            while (itr.hasNext()) {
                Store store = itr.next();
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
        
        if (fromDate != null && toDate != null) {
            // First convert GMT date into User's timezone date & then compare.
            hql.append(" AND Date(convert_tz(ist.businessDate,'+00:00','"+ TZdiff +"')) >= ? AND Date(convert_tz(ist.businessDate,'+00:00','"+ TZdiff +"')) <= ? ");
//            hql.append(" AND Date(ist.createdOn) >= ? AND Date(ist.createdOn) <= ? ");
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (ist.transactionNo LIKE ? OR istd.issuedSerialNames LIKE ? OR istd.deliveredSerialNames LIKE ? "
                      + "OR ist.product.productid LIKE ? OR ist.product.name LIKE ? )");

            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        HashMap<String, Object> requestParams=new HashMap<>();
        
        if (!StringUtil.isNullOrEmpty(searchJson)) {
            try {
                JSONObject serachJobj = new JSONObject(searchJson);
                JSONArray customSearchFieldArray = new JSONArray();
                JSONArray defaultSearchFieldArray = new JSONArray();
                StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);

                if (customSearchFieldArray.length() > 0) {
                    /*
                     * Advance Search For Custom fields
                     */
                    requestParams.put(Constants.Searchjson, searchJson);
                    requestParams.put(Constants.appendCase, "AND");
                    requestParams.put(Constants.moduleid, Constants.Acc_Product_Master_ModuleId);
                    requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);

                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(requestParams, true).get(Constants.myResult));
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "ist.product.productCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "ist.product.productCustomData");
                    mySearchFilterString = mySearchFilterString.replaceAll("accproductcustomdata", "ist.product.productCustomData");
                    StringUtil.insertParamAdvanceSearchString1(params, searchJson);

                }
                mySearchFilterString = StringUtil.combineCustomAndDefaultSearch("", mySearchFilterString, filterConjuctionCriteria);
                hql.append(mySearchFilterString);

            } catch (Exception ex) {
                Logger.getLogger(InterStoreTransferDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (statusList != null && statusList.length > 0) {
            boolean first = true;
            StringBuilder statusIn = new StringBuilder(" AND ist.status IN ( ");
            for (InterStoreTransferStatus ists : statusList) {
                if (first) {
                    statusIn.append(ists.ordinal());
                    first = false;
                } else {
                    statusIn.append(",").append(ists.ordinal());
                }
            }
            statusIn.append(")");
            hql.append(statusIn);
        }
        if (reqMap.containsKey("isJobWorkStockOut") && reqMap.get("isJobWorkStockOut") != null) {
            hql.append("AND isJobWorkStockTransfer = 'T' ");
        } else {
            hql.append("AND isJobWorkStockTransfer = 'F' ");
        }
        hql.append(" ORDER BY ist.businessDate DESC ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null&&!isJobWorkOutRemain) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;
    }
    
    @Override
    public KwlReturnObject getVendorNameAndJWONo(String podid,String companyid)throws ServiceException{
        List list = new ArrayList();
        String query = "SELECT v.name,  po.ponumber,v.id as vid,po.id as pid,po.currency FROM  purchaseorder po " +
                        " INNER JOIN podetails pod ON po.id = pod.purchaseorder " +
                        " INNER JOIN vendor v on v.id = po.vendor " +
                        " AND po.company = ? " +
                        " AND po.isjobworkoutorder = 'T' " +
                        " AND pod.id= ? ";
        list = executeSQLQuery(query, new Object[]{companyid,podid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public List<InterStoreTransferRequest> getISTIncommingRequestList(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging,String TZdiff,Map<String, Object> reqMap) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM InterStoreTransferRequest WHERE company = ? AND transactionModule=? ");
        List params = new ArrayList();
        params.add(company);
        params.add(TransactionModule.INTER_STORE_TRANSFER);
        if (fromDate != null && toDate != null) {
//            hql.append(" AND Date(createdOn) >= ? AND Date(createdOn) <= ?  ");
            // First convert GMT date into User's timezone date & then compare.
            hql.append(" AND Date(convert_tz(businessDate,'+00:00','"+ TZdiff +"')) >= ? AND Date(convert_tz(businessDate,'+00:00','"+ TZdiff +"')) <= ?  ");
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (transactionNo LIKE ? OR product.productid LIKE ? OR product.name LIKE ?) ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }

        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND toStore.id IN ( ");
            Iterator<Store> itr = storeSet.iterator();
            while (itr.hasNext()) {
                Store store = itr.next();
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
        hql.append("AND (status = ? OR status = ?)");
        params.add(InterStoreTransferStatus.INTRANSIT);
        params.add(InterStoreTransferStatus.RETURNED);
        if (reqMap.containsKey("isJobWorkStockOut") && reqMap.get("isJobWorkStockOut") != null) {
            hql.append("AND isJobWorkStockTransfer = 'T' ");
        } else {
            hql.append("AND isJobWorkStockTransfer = 'F' ");
        }
        hql.append(" ORDER BY businessDate DESC,transactionNo DESC ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;
    }

    @Override
    public List<InterStoreTransferRequest> getISTOutgoingRequestList(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging,String TZdiff,Map<String, Object> reqMap) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM InterStoreTransferRequest WHERE company = ? AND transactionModule=?");
        List params = new ArrayList();
        params.add(company);
        params.add(TransactionModule.INTER_STORE_TRANSFER);
        if (fromDate != null && toDate != null) {
            // First convert GMT date into User's timezone date & then compare.
//            hql.append(" AND Date(createdOn) >= ? AND Date(createdOn) <= ?  ");
            hql.append(" AND Date(convert_tz(businessDate,'+00:00','"+ TZdiff +"')) >= ? AND Date(convert_tz(businessDate,'+00:00','"+ TZdiff +"')) <= ?  ");
            params.add(fromDate);
            params.add(toDate);
            }  
            if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND transactionNo LIKE ? OR product.productid LIKE ? OR product.name LIKE ? ");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }

        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND fromStore.id IN ( ");
            Iterator<Store> itr = storeSet.iterator();
            while (itr.hasNext()) {
                Store store = itr.next();
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
        if(reqMap.containsKey("isJobWorkStockOut") && reqMap.get("isJobWorkStockOut")!=null){
            hql.append("AND isJobWorkStockTransfer = 'T' ");
        }else{
            hql.append("AND isJobWorkStockTransfer = 'F' ");
        }
        hql.append("AND status = ? ");
        params.add(InterStoreTransferStatus.INTRANSIT);
        hql.append(" ORDER BY businessDate DESC,transactionNo DESC ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;
    }
    
    @Override
    public List<InterStoreTransferRequest> getInterStoreTransferBySequenceNo(Company company,String sequenceNo) throws ServiceException{
            
        StringBuilder hql = new StringBuilder("FROM InterStoreTransferRequest WHERE company = ? AND transactionNo = ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(sequenceNo);
        List<InterStoreTransferRequest> list = executeQuery( hql.toString(), params.toArray());
        
        return list;
    }

    @Override
    public InterStoreTransferRequest getInterStoreTransferById(String interStoreTransferId) throws ServiceException {
        return (InterStoreTransferRequest) get(InterStoreTransferRequest.class, interStoreTransferId);
    }

    @Override
    public ISTDetail getISTDetailById(String istDetailId) throws ServiceException {
        return (ISTDetail) get(ISTDetail.class, istDetailId);
    }

    @Override
    public List<ISTStockBuffer> getISTStockBuffer(ISTDetail istDetail, Location issuedLocation) throws ServiceException {
        String hql = "FROM ISTStockBuffer WHERE istd = ? AND location = ? ";
        List list = executeQuery( hql, new Object[]{istDetail, issuedLocation});
        return list;
    }

    @Override
    public double getBufferAveragePrice(InterStoreTransferRequest ist) throws ServiceException {
        String hql = "SELECT SUM(istsb.quantity * istsb.pricePerUnit), SUM(istsb.quantity) FROM ISTStockBuffer istsb WHERE ist = ?  GROUP BY ist ";
        List list = executeQuery( hql, ist);
        Iterator itr = list.iterator();
        double pricePerUnit = 0;
        if (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            double amount = (Double) objs[0];
            double quantity = (Double) objs[1];
            if (quantity != 0) {
                pricePerUnit = amount / quantity;
            }
        }
        return Math.abs(pricePerUnit);
    }
    @Override
     public List<InterStoreTransferRequest> getInterLocationTransferList(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging,String TZdiff) throws ServiceException{
        StringBuilder hql = new StringBuilder("select DISTINCT ist  FROM InterStoreTransferRequest ist left join ist.istDetails istd WHERE ist.company = ? And ist.transactionModule=? AND ist.status <> ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(TransactionModule.INTER_LOCATION_TRANSFER);
        params.add(InterStoreTransferStatus.DELETED);
          if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND ist.fromStore.id IN ( ");
            Iterator<Store> itr = storeSet.iterator();
            while (itr.hasNext()) {
                Store store = itr.next();
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
        
        if (fromDate != null && toDate != null) {
            // First convert GMT date into User's timezone date & then compare.
//            hql.append(" AND Date(ist.createdOn) >= ? AND Date(ist.createdOn) <= ? ");
            hql.append(" AND Date(convert_tz(ist.createdOn,'+00:00','"+ TZdiff +"')) >= ? AND Date(convert_tz(ist.createdOn,'+00:00','"+ TZdiff +"')) <= ?  ");
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (ist.transactionNo LIKE ? OR istd.issuedSerialNames LIKE ? OR istd.deliveredSerialNames LIKE ? OR ist.product.productid LIKE ? OR ist.product.name LIKE ?)");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }

       
        hql.append(" ORDER BY ist.createdOn DESC ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;
    }
    
     @Override
     public List<ISTDetail> getInterLocationTransferListByDetailwise(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging,String TZdiff) throws ServiceException{
        StringBuilder hql = new StringBuilder("select istd  FROM InterStoreTransferRequest ist left join ist.istDetails istd WHERE ist.company = ? And ist.transactionModule=? AND ist.status <> ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(TransactionModule.INTER_LOCATION_TRANSFER);
        params.add(InterStoreTransferStatus.DELETED);
          if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder(" AND ist.fromStore.id IN ( ");
            Iterator<Store> itr = storeSet.iterator();
            while (itr.hasNext()) {
                Store store = itr.next();
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
        
        if (fromDate != null && toDate != null) {
            // First convert GMT date into User's timezone date & then compare.
//            hql.append(" AND Date(ist.createdOn) >= ? AND Date(ist.createdOn) <= ? ");
            hql.append(" AND Date(convert_tz(ist.createdOn,'+00:00','"+ TZdiff +"')) >= ? AND Date(convert_tz(ist.createdOn,'+00:00','"+ TZdiff +"')) <= ?  ");
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (ist.transactionNo LIKE ? OR istd.issuedSerialNames LIKE ? OR istd.deliveredSerialNames LIKE ? OR ist.product.productid LIKE ? OR ist.product.name LIKE ?)");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }

       
        hql.append(" ORDER BY ist.createdOn DESC ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;
    }
    
    @Override
    public List<ISTDetail> getIstDetailsForSerialByStatus(InterStoreTransferStatus status, Product product, String batchName, String serialName) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM ISTDetail WHERE istRequest.product = ? AND batchName = ? AND issuedSerialNames LIKE ? AND istRequest.status = ? ");
        List params = new ArrayList();
        params.add(product);
        params.add(batchName);
        params.add("%"+serialName+"%");
        params.add(status);
        List<ISTDetail> list = executeQuery( hql.toString(), params.toArray());
        
        return list;
    }
    
    /**
     * Method is used to save the Goods Receipt Order Details and IST detail
     * mapping if QA approval flow is activated in Goods Receipt Note.
     *
     * @param json
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public void saveGROISTDetailMapping(JSONObject json) throws ServiceException, JSONException {
        GRODetailISTMapping mapping;
        if (!StringUtil.isNullOrEmpty(json.optString("mappingid"))) {
            mapping = (GRODetailISTMapping) get(GRODetailISTMapping.class, json.optString("mappingid"));
        } else {
            mapping = new GRODetailISTMapping();
        }
        if (!StringUtil.isNullOrEmpty(json.optString("grodid"))) {
            String grodid = json.optString("grodid");
            mapping.setGroDetail(grodid);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("istrequest"))) {
            InterStoreTransferRequest interStoreTransferRequest = (InterStoreTransferRequest) json.get("istrequest");
            mapping.setInterStoreTransferRequest(interStoreTransferRequest);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("approvedIstRequest"))) {
            InterStoreTransferRequest outInterStoreTransferRequest = (InterStoreTransferRequest) json.get("approvedIstRequest");
            if (mapping.getApprovedInterStoreTransferRequests() == null) {
                mapping.setApprovedInterStoreTransferRequests(new HashSet<InterStoreTransferRequest>());
            }
            mapping.getApprovedInterStoreTransferRequests().add(outInterStoreTransferRequest);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("repairGRODetailISTMapping"))) {
            RepairGRODetailISTMapping repairGRODetailISTMapping = (RepairGRODetailISTMapping) json.get("repairGRODetailISTMapping");
            if (mapping.getRejectedInterStoreTransferRequests() == null) {
                mapping.setRejectedInterStoreTransferRequests(new HashSet<RepairGRODetailISTMapping>());
            }
            mapping.getRejectedInterStoreTransferRequests().add(repairGRODetailISTMapping);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("quantitydue"))) {
            double quantitydue = json.optDouble("quantitydue");
            mapping.setQuantityDue(quantitydue);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("actualquantity"))) {
            double actualquantity = json.optDouble("actualquantity");
            mapping.setActualQty(actualquantity);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("approvedQty"))) {
            double approvedQty = json.optDouble("approvedQty");
            mapping.setApprovedQty(approvedQty);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("approvedSerials"))) {
            mapping.setApprovedSerials(json.optString("approvedSerials"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedSerials"))) {
            mapping.setRejectedSerials(json.optString("rejectedSerials"));
        }
        saveOrUpdate(mapping);
    }

    /**
     * Method is used to store mapping RepairGRODetailISTMapping. It is used if
     * QA flow is activated in Goods Receipt Note.
     *
     * @param json
     * @param repairRequest
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public RepairGRODetailISTMapping saveRepairGRODetailISTMapping(JSONObject json, InterStoreTransferRequest repairRequest) throws ServiceException, JSONException {
        RepairGRODetailISTMapping mapping;
        if (!StringUtil.isNullOrEmpty(json.optString("repairid"))) {
            mapping = (RepairGRODetailISTMapping) get(RepairGRODetailISTMapping.class, json.optString("repairid"));
        } else {
            mapping = new RepairGRODetailISTMapping();
        }
        if (repairRequest != null) {
            mapping.setInterStoreTransferRequest(repairRequest);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("repairedIstRequest"))) {
            InterStoreTransferRequest outInterStoreTransferRequest = (InterStoreTransferRequest) json.get("repairedIstRequest");
            Set<InterStoreTransferRequest> repInterStoreTransferRequests = mapping.getRepairRejectedISTRequest();
            if (repInterStoreTransferRequests == null) {
                repInterStoreTransferRequests = new HashSet<InterStoreTransferRequest>();
            }
            repInterStoreTransferRequests.add(outInterStoreTransferRequest);
            mapping.setRepairRejectedISTRequest(repInterStoreTransferRequests);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedStockOutRequest"))) {
            StockAdjustment rejectedStockOutRequest = (StockAdjustment) json.get("rejectedStockOutRequest");
            if (mapping.getRejectedStockOuts() == null) {
                mapping.setRejectedStockOuts(new HashSet<StockAdjustment>());
            }
            mapping.getRejectedStockOuts().add(rejectedStockOutRequest);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedQty"))) {
            double rejectedQty = json.optDouble("rejectedQty");
            mapping.setRejectedQty(rejectedQty);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("mappingid"))) {
             mapping.setGrodistmapping((GRODetailISTMapping) get(GRODetailISTMapping.class, json.optString("mappingid")));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedQtyDue"))) {
            double rejectedQtyDue = json.optDouble("rejectedQtyDue");
            mapping.setRejectedQuantityDue(rejectedQtyDue);
        }
        saveOrUpdate(mapping);
        return mapping;
    }
    
    /**
     * Method is used to save the Work Order Component Detail and IST detail
     * mapping if QA approval flow is activated in WORK ORDER.
     *
     * @param json
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public void saveWOCDetailISTMapping(JSONObject json) throws ServiceException, JSONException {
        WOCDetailISTMapping mapping;
        if (!StringUtil.isNullOrEmpty(json.optString("wocdistmapping"))) {
            mapping = (WOCDetailISTMapping) get(WOCDetailISTMapping.class, json.optString("wocdistmapping"));
        } else {
            mapping = new WOCDetailISTMapping();
        }
        if (!StringUtil.isNullOrEmpty(json.optString("wocdetailid"))) {
            String wocdetailid = json.optString("wocdetailid");
            mapping.setWocDetail(wocdetailid);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("istrequest"))) {
            InterStoreTransferRequest interStoreTransferRequest = (InterStoreTransferRequest) json.get("istrequest");
            mapping.setInterStoreTransferRequest(interStoreTransferRequest);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("approvedIstRequest"))) {
            InterStoreTransferRequest outInterStoreTransferRequest = (InterStoreTransferRequest) json.get("approvedIstRequest");
            if (mapping.getApprovedInterStoreTransferRequests() == null) {
                mapping.setApprovedInterStoreTransferRequests(new HashSet<InterStoreTransferRequest>());
            }
            mapping.getApprovedInterStoreTransferRequests().add(outInterStoreTransferRequest);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("repairWOCDISTMapping"))) {
            RepairWOCDISTMapping repairWOCDISTMapping = (RepairWOCDISTMapping) json.get("repairWOCDISTMapping");
            if (mapping.getRejectedInterStoreTransferRequests() == null) {
                mapping.setRejectedInterStoreTransferRequests(new HashSet<RepairWOCDISTMapping>());
            }
            mapping.getRejectedInterStoreTransferRequests().add(repairWOCDISTMapping);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("quantitydue"))) {
            double quantitydue = json.optDouble("quantitydue");
            mapping.setQuantityDue(quantitydue);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("actualquantity"))) {
            double actualquantity = json.optDouble("actualquantity");
            mapping.setActualQty(actualquantity);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("approvedQty"))) {
            double approvedQty = json.optDouble("approvedQty");
            mapping.setApprovedQty(approvedQty);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("approvedSerials"))) {
            mapping.setApprovedSerials(json.optString("approvedSerials"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedSerials"))) {
            mapping.setRejectedSerials(json.optString("rejectedSerials"));
        }
        saveOrUpdate(mapping);
    }
    
    /**
     * Method is used to store mapping RepairWOCDISTMapping. It is used if
     * QA flow is activated in WORK ORDER
     *
     * @param json
     * @param repairRequest
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public RepairWOCDISTMapping saveRepairWOCDISTMapping(JSONObject json, InterStoreTransferRequest repairRequest) throws ServiceException, JSONException {
        RepairWOCDISTMapping mapping;
        if (!StringUtil.isNullOrEmpty(json.optString("repairid"))) {
            mapping = (RepairWOCDISTMapping) get(RepairWOCDISTMapping.class, json.optString("repairid"));
        } else {
            mapping = new RepairWOCDISTMapping();
        }
        if (repairRequest != null) {
            mapping.setInterStoreTransferRequest(repairRequest);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("repairedIstRequest"))) {
            InterStoreTransferRequest outInterStoreTransferRequest = (InterStoreTransferRequest) json.get("repairedIstRequest");
            Set<InterStoreTransferRequest> repInterStoreTransferRequests = mapping.getRepairRejectedISTRequest();
            if (repInterStoreTransferRequests == null) {
                repInterStoreTransferRequests = new HashSet<InterStoreTransferRequest>();
            }
            repInterStoreTransferRequests.add(outInterStoreTransferRequest);
            mapping.setRepairRejectedISTRequest(repInterStoreTransferRequests);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedStockOutRequest"))) {
            StockAdjustment rejectedStockOutRequest = (StockAdjustment) json.get("rejectedStockOutRequest");
            if (mapping.getRejectedStockOuts() == null) {
                mapping.setRejectedStockOuts(new HashSet<StockAdjustment>());
            }
            mapping.getRejectedStockOuts().add(rejectedStockOutRequest);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedQty"))) {
            double rejectedQty = json.optDouble("rejectedQty");
            mapping.setRejectedQty(rejectedQty);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("wocdistmapping"))) {
             mapping.setWocdistmapping((WOCDetailISTMapping) get(WOCDetailISTMapping.class, json.optString("wocdistmapping")));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedQtyDue"))) {
            double rejectedQtyDue = json.optDouble("rejectedQtyDue");
            mapping.setRejectedQuantityDue(rejectedQtyDue);
        }
        saveOrUpdate(mapping);
        return mapping;
    }
    
    /**
     * Method is used to save the Delivery Order Details and IST detail mapping
     * if QA approval flow is activated in Delivery Order.
     *
     * @param json
     * @return 
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public DODQCISTMapping saveDODQCISTMapping(JSONObject json) throws ServiceException, JSONException {
        DODQCISTMapping mapping;
        if (!StringUtil.isNullOrEmpty(json.optString("id"))) {
            mapping = (DODQCISTMapping) get(DODQCISTMapping.class, json.optString("id"));
        } else {
            mapping = new DODQCISTMapping();
        }
        if (!StringUtil.isNullOrEmpty(json.optString("approvedQty"))) {
            mapping.setApprovedQty(json.optDouble("approvedQty"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("quantity"))) {
            mapping.setQuantity(json.optDouble("quantity"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("quantityDue"))) {
            mapping.setQuantityDue(json.optDouble("quantityDue"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("pickedQty"))) {
            mapping.setPickedQty(json.optDouble("pickedQty"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedQty"))) {
            mapping.setRejectedQty(json.optDouble("rejectedQty"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("approvedSerials"))) {
            mapping.setApprovedSerials(json.optString("approvedSerials"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedSerials"))) {
            mapping.setRejectedSerials(json.optString("rejectedSerials"));
        }

        if (!StringUtil.isNullOrEmpty(json.optString("dodetailid"))) {
            mapping.setDodetailID(json.optString("dodetailid"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("istrequest"))) {
            InterStoreTransferRequest interStoreTransferRequest = (InterStoreTransferRequest) json.get("istrequest");
            mapping.setQcInterStoreTransferRequest(interStoreTransferRequest);
        }

        if (!StringUtil.isNullOrEmpty(json.optString("approvedStockOut"))) {
            StockAdjustment approvedStockOut = (StockAdjustment) json.get("approvedStockOut");
            if (mapping.getApprovedStockOuts() == null) {
                mapping.setApprovedStockOuts(new HashSet<StockAdjustment>());
            }
            mapping.getApprovedStockOuts().add(approvedStockOut);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedDODQCISTMapping"))) {
            RejectedDODQCISTMapping rejectedDODQCISTMapping = (RejectedDODQCISTMapping) json.get("rejectedDODQCISTMapping");
            if (mapping.getRejectedDODQCISTMappings() == null) {
                mapping.setRejectedDODQCISTMappings(new HashSet<RejectedDODQCISTMapping>());
            }
            mapping.getRejectedDODQCISTMappings().add(rejectedDODQCISTMapping);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("pickedMapping"))) {
            DeliveryDetailInterStoreLocationMapping pickedMapping = (DeliveryDetailInterStoreLocationMapping) json.get("pickedMapping");
            if (mapping.getRejectedDODQCISTMappings() == null) {
                mapping.setPickedMapping(new HashSet<DeliveryDetailInterStoreLocationMapping>());
            }
            mapping.getPickedMapping().add(pickedMapping);
        }
        saveOrUpdate(mapping);
        return mapping;
    }
    
        /**
     * Method is used to save the Delivery Order Details and IST detail mapping
     * if QA approval flow is activated in Delivery Order.
     *
     * @param json
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public RejectedDODQCISTMapping saveRejectedDODQCISTMapping(JSONObject json) throws ServiceException, JSONException {
        RejectedDODQCISTMapping mapping;
        if (!StringUtil.isNullOrEmpty(json.optString("repairid"))) {
            mapping = (RejectedDODQCISTMapping) get(RejectedDODQCISTMapping.class, json.optString("repairid"));
        } else {
            mapping = new RejectedDODQCISTMapping();
        }
        if (!StringUtil.isNullOrEmpty(json.optString("quantity"))) {
            mapping.setQuantity(json.optDouble("quantity"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("quantityDue"))) {
            mapping.setQuantityDue(json.optDouble("quantityDue"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("pickedQty"))) {
            mapping.setPickedQty(json.optDouble("pickedQty"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectQuantity"))) {
            mapping.setRejectedQty(json.optDouble("rejectQuantity"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("repairedQty"))) {
            mapping.setRepairedQty(json.optDouble("repairedQty"));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("repairInterStoreTransferRequest"))) {
            InterStoreTransferRequest interStoreTransferRequest = (InterStoreTransferRequest) json.get("repairInterStoreTransferRequest");
            mapping.setRepairInterStoreTransferRequest(interStoreTransferRequest);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("approvedStockOut"))) {
            StockAdjustment approvedStockOut = (StockAdjustment) json.get("approvedStockOut");
            if (mapping.getApprovedStockOuts() == null) {
                mapping.setApprovedStockOuts(new HashSet<StockAdjustment>());
            }
            mapping.getApprovedStockOuts().add(approvedStockOut);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("rejectedStockOut"))) {
            StockAdjustment rejectedStockOut = (StockAdjustment) json.get("rejectedStockOut");
            if (mapping.getRejectedStockOuts()== null) {
                mapping.setRejectedStockOuts(new HashSet<StockAdjustment>());
            }
            mapping.getRejectedStockOuts().add(rejectedStockOut);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("dodqcistmapping"))) {
            DODQCISTMapping dodqcistm = (DODQCISTMapping) json.get("dodqcistmapping");
            mapping.setDodqcistmapping(dodqcistm);
        } else if (!StringUtil.isNullOrEmpty(json.optString("dodqcistmappingid"))) {
            DODQCISTMapping dodqcistm = (DODQCISTMapping) get(DODQCISTMapping.class, json.optString("dodqcistmappingid"));
            mapping.setDodqcistmapping(dodqcistm);
        }
        if (!StringUtil.isNullOrEmpty(json.optString("pickedMapping"))) {
            DeliveryDetailInterStoreLocationMapping pickedMapping = (DeliveryDetailInterStoreLocationMapping) json.get("pickedMapping");
            if (mapping.getPickedMappings() == null) {
                mapping.setPickedMappings(new HashSet<DeliveryDetailInterStoreLocationMapping>());
            }
            mapping.getPickedMappings().add(pickedMapping);
        }
        saveOrUpdate(mapping);
        return mapping;
    }
    /**
     * @Info This method copies all the data of custom field from IST to Returned IST Request. 
     * @param New_JE_ID Return IST transaction ID
     * @param Old_JE_ID Old IST transaction ID
     * @param moduleId
     * @return
     * @throws ServiceException 
     */
    @Override
    public int saveCustomDataForReturnTransactions(String New_JE_ID, String Old_JE_ID, int moduleId) throws ServiceException {
        int NoOFRecords = 0;
        String query = "";
        String conditionSQL = "";
        String selectColumns = "";
        String tableName = "";
        String column = "";
        try {
            for (int i = Constants.Custom_Column_Combo_start + 1; i <= (Constants.Custom_Column_Combo_start + Constants.Custom_Column_Combo_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            for (int i = Constants.Custom_Column_Master_start + 1; i <= (Constants.Custom_Column_Master_start + Constants.Custom_Column_Master_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }

            for (int i = Constants.Custom_Column_User_start + 1; i <= (Constants.Custom_Column_User_start + Constants.Custom_Column_User_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }

            for (int i = Constants.Custom_Column_Normal_start + 1; i <= (Constants.Custom_Column_Normal_start + Constants.Custom_Column_Normal_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            for (int i = Constants.Custom_Column_Check_start + 1; i <= (Constants.Custom_Column_Check_start + Constants.Custom_Column_Check_limit); i++) {
                conditionSQL = conditionSQL + "acc.col" + i + ",";
                selectColumns = selectColumns + "col" + i + ",";
            }
            conditionSQL += "acc.company,acc.deleted,acc.moduleId";
            selectColumns += "company, deleted, moduleId";

            switch (moduleId) {
                case Constants.Acc_InterStore_ModuleId:
                    tableName = "in_interstoretransfer_customdata";
                    column = "acc.istid";
                    selectColumns = "istid," + selectColumns;
                    break;

            }
            query += "insert into " + tableName + " (" + selectColumns + ") (select '" + New_JE_ID + "'," + conditionSQL + " from " + tableName + " as acc where " + column + "='" + Old_JE_ID + "')";
            NoOFRecords = executeSQLUpdate(query, new String[]{});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveAccJECustomData : " + ex.getMessage(), ex);
        }
        return NoOFRecords;
    }

}
