/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.exception.NegativeInventoryException;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
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
public interface InterStoreTransferService {

    public void addInterStoreTransferRequest(User user, InterStoreTransferRequest interStoreTransferRequest) throws ServiceException, NegativeInventoryException;

    public JSONObject saveInterStoreTransferRequest(JSONObject paramJobj);
    public JSONObject acceptInterStoreTransferRequest(JSONObject paramJobj);
    
    public void checkItemAlreadyProcessed(boolean isQAReject, String moduleType, String qaApprovalDetailId)throws InventoryException, ServiceException;  
    
    public void addInterStoreTransferRequest(User user, InterStoreTransferRequest interStoreTransferRequest, boolean allowNegativeInventory, Map<String,Object> requestParams) throws ServiceException, NegativeInventoryException;

    public void acceptInterStoreTransferRequest(User user, InterStoreTransferRequest interStoreTransferRequest) throws ServiceException;
    
    public void acceptInterStoreTransferRequest(User user, InterStoreTransferRequest interStoreTransferRequest, String stockMovementRemark) throws ServiceException;

    public void rejectInterStoreTransferRequest(User user, InterStoreTransferRequest interStoreTransferRequest) throws ServiceException;

    public List<InterStoreTransferRequest> getIncommingInterStoreTransferList(User user, Store store, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff,Map<String, Object> reqMap) throws ServiceException;

    public List<InterStoreTransferRequest> getOutgoingInterStoreTransferList(User user, Store store, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff,Map<String, Object> reqMap) throws ServiceException;

    public List<InterStoreTransferRequest> getCompletedInterStoreTransferList(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff,Map<String, Object> reqMap) throws ServiceException;

    public List<InterStoreTransferRequest> getInterLocationTransferList(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff) throws ServiceException;

    public List<ISTDetail> getInterLocationTransferListByDetailwise(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging,String TZdiff) throws ServiceException;
    
    public InterStoreTransferRequest getInterStoreTransferById(String interStoreTransferId) throws ServiceException;

    public ISTDetail getISTDetailById(String istDetailId) throws ServiceException;

    public void addInterLocationTransfer(User user, InterStoreTransferRequest interStoreTransfer, Map<String,Object> requestParams) throws ServiceException;

    public void acceptISTReturnRequest(User user, InterStoreTransferRequest interStoreTransfer) throws ServiceException;
    
    public void saveGlobalAndLineLevelTransferCustomData(InterStoreTransferRequest interStoreTransfer,Map<String, Object> istParams) throws ServiceException;

    public void acceptISTReturnRequest(User user, InterStoreTransferRequest istReturn, String stockMovementRemark) throws ServiceException;

    public void sendISTReturnRequestForQA(User user, InterStoreTransferRequest interStoreTransfer, String[] serialNames) throws ServiceException;

    public List<InterStoreTransferRequest> getInterStoreTransferBySequenceNo(Company company, String sequenceNo) throws ServiceException;

    public void deleteISTRequest(InterStoreTransferRequest istRequest) throws ServiceException;
    
    public void deleteAcceptedISTRequests(InterStoreTransferRequest istRequest) throws ServiceException;

    public void deleteILocationTRequest(InterStoreTransferRequest istRequest) throws ServiceException;

    public void cancelISTRequest(User user, InterStoreTransferRequest istRequest) throws ServiceException;

    public ISTDetail getIntransitIstDetailForSerial(Product product, String batchName, String serialName) throws ServiceException;

    public InterStoreTransferRequest getReturnTransactionforSerial(Product product, String batchName, String serialName) throws ServiceException;
    
    public DeliveryDetailInterStoreLocationMapping saveDODISTMapping(Map<String ,Object>  mappingParams) throws ServiceException;
    
    public KwlReturnObject saveChallanNumber(Map<String, Object> map) throws ServiceException;
    
    public RepairGRODetailISTMapping saveRepairGRODetailISTMapping(JSONObject json, InterStoreTransferRequest repairRequest) throws ServiceException, JSONException;

    public RepairWOCDISTMapping saveRepairWOCDISTMapping(JSONObject json, InterStoreTransferRequest repairRequest) throws ServiceException, JSONException;

    public DODQCISTMapping saveDODQCISTMapping(JSONObject json) throws ServiceException, JSONException;

    public RejectedDODQCISTMapping saveRejectedDODQCISTMapping(JSONObject json) throws ServiceException, JSONException;
    
    public KwlReturnObject getVendorNameAndJWONo(String podid,String companyid)throws ServiceException;
}
