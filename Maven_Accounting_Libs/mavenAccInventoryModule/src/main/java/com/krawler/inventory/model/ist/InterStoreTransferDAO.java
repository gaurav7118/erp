/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.ist;

import com.krawler.inventory.model.ist.InterStoreTransferStatus;
import com.krawler.inventory.model.ist.ISTStockBuffer;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.stockrequest.SRStockBuffer;
import com.krawler.inventory.model.stockrequest.RequestStatus;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.ist.ISTDetail;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stockrequest.StockRequestDetail;
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
public interface InterStoreTransferDAO {

    public void saveOrUpdate(Object object) throws ServiceException;

    public void delete(Object object) throws ServiceException;

    public List<InterStoreTransferRequest> getInterStoreTransferList(Company company, Set<Store> fromStoreSet, Set<Store> toStoreSet, InterStoreTransferStatus[] statusList, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff,Map<String, Object> reqMap) throws ServiceException;

    public List<InterStoreTransferRequest> getISTIncommingRequestList(Company company, Set<Store> store, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff,Map<String, Object> reqMap) throws ServiceException;

    public List<InterStoreTransferRequest> getISTOutgoingRequestList(Company company, Set<Store> store, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff,Map<String, Object> reqMap) throws ServiceException;

    public List<InterStoreTransferRequest> getInterLocationTransferList(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging, String TZdiff) throws ServiceException;

    public List<ISTDetail> getInterLocationTransferListByDetailwise(Company company, Set<Store> storeSet, Date fromDate, Date toDate, String searchString, Paging paging,String TZdiff) throws ServiceException;
    
    public List<InterStoreTransferRequest> getInterStoreTransferBySequenceNo(Company company, String sequenceNo) throws ServiceException;

    public InterStoreTransferRequest getInterStoreTransferById(String interStoreTransferId) throws ServiceException;

    public ISTDetail getISTDetailById(String istDetailId) throws ServiceException;

    public List<ISTStockBuffer> getISTStockBuffer(ISTDetail istDetail, Location issuedLocation) throws ServiceException;

    public double getBufferAveragePrice(InterStoreTransferRequest interStoreTransferRequest) throws ServiceException;

    public List<ISTDetail> getIstDetailsForSerialByStatus(InterStoreTransferStatus interStoreTransferStatus, Product product, String batchName, String serialName) throws ServiceException;
    
    public void saveGROISTDetailMapping(JSONObject json) throws ServiceException, JSONException;
    
    public RepairGRODetailISTMapping saveRepairGRODetailISTMapping(JSONObject json, InterStoreTransferRequest repairRequest) throws ServiceException, JSONException;
    
    public DODQCISTMapping saveDODQCISTMapping(JSONObject json) throws ServiceException, JSONException;
    
    public RejectedDODQCISTMapping saveRejectedDODQCISTMapping(JSONObject json) throws ServiceException, JSONException ;
    
    public KwlReturnObject getVendorNameAndJWONo(String podid,String companyid)throws ServiceException;
    
    public int saveCustomDataForReturnTransactions(String New_JE_ID, String Old_JE_ID, int moduleId) throws ServiceException;
    
    public void saveWOCDetailISTMapping(JSONObject json) throws ServiceException, JSONException;
    
    public RepairWOCDISTMapping saveRepairWOCDISTMapping(JSONObject json, InterStoreTransferRequest repairRequest) throws ServiceException, JSONException;
}
