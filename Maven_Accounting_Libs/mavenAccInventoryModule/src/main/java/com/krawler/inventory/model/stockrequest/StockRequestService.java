/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockrequest;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.exception.NegativeInventoryException;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.store.Store;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public interface StockRequestService {

    public void addStockOrderRequest(User user, StockRequest stockRequest, Map<String,Object> requestParams) throws ServiceException;

    public void approveStockOrderRequest(User user, StockRequest stockRequest) throws ServiceException;

    public void rejectStockOrderRequest(User user, StockRequest stockRequest) throws ServiceException;

    public void issueStockOrderRequest(User user, StockRequest stockRequest) throws ServiceException, NegativeInventoryException;

    public void issueStockOrderRequest(User user, StockRequest sr, boolean allowNegativeInventory) throws ServiceException, NegativeInventoryException;

    public void collectStockOrderRequest(User user, StockRequest stockRequest) throws ServiceException;
    
    public void collectStockOrderRequest(User user, StockRequest stockRequest, String stockMovementRemark) throws ServiceException;

    public void stockIssueByIssueNote(User user, StockRequest stockRequest) throws ServiceException, NegativeInventoryException, JSONException;

    public void stockIssueByIssueNote(User user, StockRequest stockRequest, boolean allowNegativeInventory, Map<String, Object> requestParams) throws ServiceException, NegativeInventoryException, JSONException;

    public StockRequest getStockRequestById(String stockRequestId) throws ServiceException;

    public List<StockRequest> getPendingStockRequestList(Map<String, Object> request, Company company, User requestedBy, Store store, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;

    public List<StockRequest> getCompletedStockRequestList(Map<String, Object> request, Company company, User requestedBy, Store store, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;

    public List<StockRequest> getStockRequestList(Map<String, Object> request, Company company, User requestedBy, Store store, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;

    public List<StockRequest> getStockRequestDetailBySequenceNo(Company company, String transactionNo, TransactionModule module) throws ServiceException;

    public StockRequestDetail getStockRequestDetail(String detailId) throws ServiceException;

    public Map<String, Double> getTotalOrderedQuantityForProductStore(Company company) throws ServiceException;

    public List<StockRequest> getStorewisePendingStockRequestList(Map <String,Object> requestParams,Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging, boolean isStorewiseStoreOrderList) throws ServiceException;

    public void approveSRReturnRequest(User approver, StockRequest sr) throws ServiceException;

    public void acceptReturnStockRequest(User approver, StockRequest sr) throws ServiceException;
    
    public void acceptReturnStockRequest(User approver, StockRequest sr, String stockMovementRemark) throws ServiceException;
    
    public void sendReturnStockRequestForQA(User approver, StockRequest sr, String[] serialNames) throws ServiceException;

    public void deleteStockRequest(StockRequest stockRequest) throws ServiceException;

    public void cancelStockRequest(User user, StockRequest sr) throws ServiceException;

    public StockRequestDetail getIssuedSrDetailsforSerial(Product product, String batchName, String serialName) throws ServiceException;

    public StockRequest getReturnSrforSerial(Product product, String batchName, String serialName) throws ServiceException;
    
    public JSONObject getSingleStockRequestToLoad(JSONObject params)throws  ServiceException;
    
    public JSONObject deleteStockIssueDetail(String id)throws  ServiceException;
}
