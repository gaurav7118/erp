/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockrequest;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ModuleTemplate;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.store.Store;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public interface StockRequestDAO {

    public void saveOrUpdate(Object object) throws ServiceException;

    public void delete(Object object) throws ServiceException;

    public List<StockRequest> getStockRequestList(Map<String, Object> request, Company company, User requestBy, RequestStatus[] statusList, Store store, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;

    public List<StockRequest> getStorewisePendingStockRequestList(Map <String,Object> requestParams,Company company, Set<Store> storeSet, RequestStatus[] statusList, Date fromDate, Date toDate, String searchString, Paging paging,boolean isStorewiseStoreOrderList) throws ServiceException;

    public StockRequest getStockRequestById(String stockRequestId) throws ServiceException;

    public List<SRStockBuffer> getIssuedStocks(StockRequest stockRequest) throws ServiceException;

    public List<SRStockBuffer> getSRStockBuffer(StockRequestDetail stockRequestDetail, Location issuedLocation) throws ServiceException;

    public double getBufferAveragePrice(StockRequest stockRequest) throws ServiceException;

    public StockRequestDetail getStockRequestDetail(String detailId) throws ServiceException;

    public Map<String, Double> getTotalOrderedQuantityForProductStore(Company company) throws ServiceException;

    public List<StockRequest> getStockRequestDetailBySequenceNo(Company company, String transactionNo,TransactionModule module) throws ServiceException;

    public List<StockRequestDetail> getSrDetailsForSerialByStatus(RequestStatus mainRequestStatus, Product product, String batchName, String serialName) throws ServiceException;
    
    public List getStockRequestTemplate(JSONObject params) throws ServiceException;

}
