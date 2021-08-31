/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockrequest.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ModuleTemplate;
import com.krawler.common.admin.User;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockrequest.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class StockRequestDAOImpl extends BaseDAO implements StockRequestDAO {

    @Override
    public void saveOrUpdate(Object object) throws ServiceException {
        super.saveOrUpdate(object);
    }

    @Override
    public void delete(Object object) throws ServiceException {
        super.delete(object);
    }

    @Override
    public List<StockRequest> getStockRequestList(Map<String, Object> request, Company company, User requestBy, RequestStatus[] statusList, Store store, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException {
        ArrayList params = new ArrayList();
        StringBuilder hql = new StringBuilder("FROM StockRequest WHERE company = ? ");
        params.add(company);
        String Searchjson = "";
        String appendCase = "and";
        String mySearchFilterString = "";
        int statusVal = 0;
        int reportId=0;
        String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
        if (request.containsKey("filterConjuctionCriteria") && request.get("filterConjuctionCriteria") != null) {
            if (request.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                filterConjuctionCriteria = com.krawler.common.util.Constants.or;
            }
        }
        if (request.containsKey("searchJson") && request.get("searchJson") != null) {
            Searchjson = request.get("searchJson").toString();
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                request.put(Constants.Searchjson, Searchjson);
                request.put(Constants.appendCase, appendCase);
                request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                try {
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                    hql.append(mySearchFilterString);
                
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                } catch (JSONException ex) {
                    Logger.getLogger(StockRequestDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(StockRequestDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if (store != null) {
            hql.append(" AND (fromStore = ? OR toStore = ? ) ");
            params.add(store);
            params.add(store);
        }
        if (fromDate != null && toDate != null) {
            hql.append(" AND ( Date(bussinessDate) >= ? AND Date(bussinessDate) <= ? ) ");
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (transactionNo LIKE ? or product.name LIKE ? or product.productid LIKE ?)");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
//        if (requestBy != null) {
//            hql.append(" AND requestedBy = ? ");
//            params.add(requestBy);
//        }
         if (!StringUtil.isNullOrEmpty(request.get("filterStatusValue").toString()) &&!StringUtil.isNullOrEmpty(request.get("reportId").toString())) {
            statusVal = Integer.parseInt(request.get("filterStatusValue").toString());
            reportId=Integer.parseInt(request.get("reportId").toString());
        }
         if (statusVal == 1 && reportId==Constants.FULFILLED_ORDER) {
            hql.append(" AND status = ?");
            params.add(RequestStatus.COLLECTED);
        } else if (statusVal == 2 && reportId==Constants.FULFILLED_ORDER) {
            hql.append(" AND status = ?");
            params.add(RequestStatus.REJECTED);
        } else if(statusVal == 3 && reportId==Constants.FULFILLED_ORDER){
            hql.append(" AND status = ?");
            params.add(RequestStatus.RETURNED);
        } else if(statusVal == 4 && reportId==Constants.FULFILLED_ORDER){
            hql.append(" AND status = ?");
            params.add(RequestStatus.DELETED);
        } else if (statusVal == 1 && reportId==Constants.STORE_ORDER) {
            hql.append(" AND status = ?");
            params.add(RequestStatus.ORDERED);
        } else if (statusVal == 2 && reportId==Constants.STORE_ORDER) {
            hql.append(" AND status = ?");
            params.add(RequestStatus.ISSUED);
        }else if (statusList != null && statusList.length > 0) {
            boolean first = true;
            StringBuilder statusIn = new StringBuilder(" AND status IN ( ");
            for (RequestStatus rs : statusList) {
                if (first) {
                    statusIn.append(rs.ordinal());
                    first = false;
                } else {
                    statusIn.append(",").append(rs.ordinal());
                }
            }
            statusIn.append(")");
            hql.append(statusIn);
        }
        if(request.containsKey("isTemplate")){
            hql.append(" AND istemplate = ?");
            params.add(1);
        }else{
            hql.append(" AND istemplate = ?");
            params.add(0);
        }
        hql.append(" ORDER BY requestedOn DESC ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;
    }

    @Override
    public StockRequest getStockRequestById(String stockRequestId) {
        Object obj = get(StockRequest.class, stockRequestId);
        StockRequest stockRequest = (StockRequest) obj;
        return stockRequest;
    }

    @Override
    public List<SRStockBuffer> getIssuedStocks(StockRequest stockRequest) throws ServiceException {
        String hql = "FROM SRStockBuffer WHERE issuedReq = ? ";
        List list = executeQuery( hql, stockRequest);
        return list;
    }

    @Override
    public List<SRStockBuffer> getSRStockBuffer(StockRequestDetail stockRequestDetail, Location issuedLocation) throws ServiceException {
        String hql = "FROM SRStockBuffer WHERE stockRequestDetail = ? AND location = ?";
        List list = executeQuery( hql, new Object[]{stockRequestDetail, issuedLocation});
        return list;
    }

    @Override
    public double getBufferAveragePrice(StockRequest stockRequest) throws ServiceException {
        String hql = "SELECT SUM(srsb.quantity * srsb.pricePerUnit), SUM(srsb.quantity) FROM SRStockBuffer srsb WHERE stockRequest = ?  GROUP BY stockRequest ";
        List list = executeQuery( hql, stockRequest);
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
    public StockRequestDetail getStockRequestDetail(String detailId) throws ServiceException {
        return (StockRequestDetail) get(StockRequestDetail.class, detailId);
    }

    @Override
    public Map<String, Double> getTotalOrderedQuantityForProductStore(Company company) throws ServiceException {
        Map<String, Double> map = new HashMap<String, Double>();
        StringBuilder hql = new StringBuilder("SELECT CONCAT(SR.fromStore.id,SR.product.ID),SUM(SR.orderedQty) FROM StockRequest SR WHERE SR.status in(0,1)  AND SR.company = ?  AND SR.fromStore IS NOT NULL  AND SR.istemplate = 0 GROUP BY SR.product,SR.fromStore ");
        List params = new ArrayList();
        params.add(company);

        List list = executeQuery( hql.toString(), params.toArray());

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Object[] objs = (Object[]) itr.next();
            if (objs[0] != null && objs[1] != null) {
                //String key = ((Store) objs[0]).getId().concat(((Product) objs[1]).getID().toString());
                String key = (objs[0].toString());
                map.put(key, (Double) objs[1]);
            }
        }
        return map;
    }

    @Override
    public List<StockRequest> getStorewisePendingStockRequestList(Map <String,Object> requestParams,Company company, Set<Store> storeSet, RequestStatus[] statusList, Date fromDate, Date toDate, String searchString, Paging paging, boolean isStorewiseStoreOrderList) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockRequest WHERE company = ? ");
        ArrayList params = new ArrayList();
        params.add(company);
        String Searchjson = "";
        String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
        String appendCase = "and";
        int statusVal = 0;
        int reportId=0;
        String mySearchFilterString = "";
        if (fromDate != null && toDate != null) {
            hql.append(" AND ( Date(bussinessDate) >= ? AND Date(bussinessDate) <= ? ) ");
            params.add(fromDate);
            params.add(toDate);
        }
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND (transactionNo LIKE ? or product.name LIKE ? or product.productid LIKE ?)");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
            params.add("%" + searchString + "%");
        }
        if (!StringUtil.isNullOrEmpty(requestParams.get("filterStatusValue").toString()) && !StringUtil.isNullOrEmpty(requestParams.get("reportId").toString())) {
            statusVal = Integer.parseInt(requestParams.get("filterStatusValue").toString());
            reportId=Integer.parseInt(requestParams.get("reportId").toString());
        }
        if (storeSet != null && !storeSet.isEmpty()) {
            boolean first = true;
            StringBuilder storeIn = new StringBuilder();
            if (isStorewiseStoreOrderList) {
                storeIn = new StringBuilder(" AND toStore.id IN ( ");
            } else {
                storeIn = new StringBuilder(" AND fromStore.id IN ( ");
            }
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
        if (requestParams.containsKey("isTemplate")) {
            hql.append(" AND istemplate = ?");
            params.add(1);
        } else {
            hql.append(" AND istemplate = ?");
            params.add(0);
        }
        if (statusVal == 1 && reportId==Constants.GOODS_PENDING_ORDER) {
            hql.append(" AND status = ?");
            params.add(RequestStatus.ORDERED);
        } else if (statusVal == 2 && reportId==Constants.GOODS_PENDING_ORDER) {
            hql.append(" AND status = ?");
            params.add(RequestStatus.ISSUED);
        } else if(statusVal == 3 && reportId==Constants.GOODS_PENDING_ORDER){
            hql.append(" AND status = ?");
            params.add(RequestStatus.RETURN_REQUEST);
        } else if (statusList != null && statusList.length > 0) {
            boolean first = true;
            StringBuilder statusIn = new StringBuilder(" AND status IN ( ");
            for (RequestStatus rs : statusList) {
                if (first) {
                    statusIn.append(rs.ordinal());
                    first = false;
                } else {
                    statusIn.append(",").append(rs.ordinal());
                }
            }
            statusIn.append(")");
            hql.append(statusIn);
        }
         if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
            Searchjson = requestParams.get("searchJson").toString();
            if (!StringUtil.isNullOrEmpty(Searchjson)) {
                requestParams.put(Constants.Searchjson, Searchjson);
                requestParams.put(Constants.appendCase, appendCase);
                requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                try {
                    mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(requestParams, true).get(Constants.myResult));
                    hql.append(mySearchFilterString);
                
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                } catch (JSONException ex) {
                    Logger.getLogger(StockRequestDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(StockRequestDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        hql.append(" ORDER BY requestedOn DESC ");
        List list = executeQuery( hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging( hql.toString(), params.toArray(), paging);
        }
        return list;
    }

    @Override
    public List<StockRequest> getStockRequestDetailBySequenceNo(Company company, String transactionNo,TransactionModule module) throws ServiceException {
           
        StringBuilder hql = new StringBuilder("FROM StockRequest WHERE company = ? AND transactionNo = ? AND module= ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(transactionNo);
        params.add(module);
        List<StockRequest> list = executeQuery( hql.toString(), params.toArray());
        
        return list;
    }

    @Override
    public List<StockRequestDetail> getSrDetailsForSerialByStatus(RequestStatus mainRequestStatus, Product product, String batchName, String serialName) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM StockRequestDetail WHERE stockRequest.product = ? AND batchName = ? AND issuedSerialNames LIKE ? AND stockRequest.status = ? ");
        List params = new ArrayList();
        params.add(product);
        params.add(batchName);
        params.add("%"+serialName+"%");
        params.add(mainRequestStatus);
        List<StockRequestDetail> list = executeQuery( hql.toString(), params.toArray());
        
        return list;
    }
    /**
     * 
     * @param params
     * @return : Function to return Stock Request Template
     * @throws ServiceException 
     */
    public List getStockRequestTemplate(JSONObject params) throws ServiceException {
        String query = "select sr.id from moduletemplate mt inner join in_goodsrequest sr on sr.transactionno=mt.modulerecordid where sr.company=? and mt.templatename=?";;
        List param = new ArrayList();
        param.add(params.opt("companyid"));
        param.add(params.opt("templatename"));
        List list = executeSQLQuery(query, param.toArray());
        return list;
    }
}
