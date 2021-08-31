/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.stocktransfer;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.ist.ISTDetail;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stockmovement.TransactionModule;
import com.krawler.inventory.model.stockrequest.StockRequest;
import com.krawler.inventory.model.stockrequest.StockRequestDetail;
import com.krawler.inventory.model.store.Store;
import java.util.List;
import java.util.Map;
import com.krawler.utils.json.base.JSONObject;

/**
 *
 * @author Vipin Gupta
 */
public interface StockTransferApprovalService {

    public List<StockTransferApproval> getStockTransferApprovalList(TransactionModule transactionModule, String searchString, Paging paging) throws ServiceException;

    public List<StockTransferDetailApproval> getStockTransferDetailApprovalList(StockTransferApproval stockTransferApproval, Paging paging) throws ServiceException;

    public void addStockReturnApproval(InterStoreTransferRequest istRequest, String[] approvalSerialNames) throws ServiceException;

    public void addStockReturnApproval(StockRequest stockRequest, String[] serialNames) throws ServiceException;

    public void approveStockTransferDetail(User inspector, StockTransferDetailApproval stockTransferDetailApproval, InspectionDetail inspDTL, boolean fromRepair, double retQty) throws ServiceException;

    public void rejectStockTransferDetail(User inspector, StockTransferDetailApproval stockTransferDetailApproval, InspectionDetail inspDTL, boolean fromRepair, double retQty) throws ServiceException;

    public Map rejectedApprovedItemsDetail(StockTransferDetailApproval stockTransferDetailApproval) throws ServiceException;

    public StockTransferDetailApproval getStockTransferDetailApproval(String id);

    public void createStockMovementForQAApproval(Company company, List<StockTransferDetailApproval> approvedRejectedRecords, Store qaStore, Store repairStore) throws ServiceException;

    public void createStockMovementForRepairing(Company company, List<StockTransferDetailApproval> stockTransferDetailApprovalList, Store repaieStore) throws ServiceException;

    public void createStockMovementForQAApprovalIntr(Company company, List<StockTransferDetailApproval> approvedRejectedRecords, Store qaStore, Store repairStore) throws ServiceException;

    public void createStockMovementForRepairingIntr(Company company, List<StockTransferDetailApproval> stockTransferDetailApprovalList, Store repaieStore) throws ServiceException;

    public String approveRejectGoodsReceipt(Map<String,Object> requestParams, Store repairStore, Location repairLocation) throws ServiceException;    
    
    public void createStockOutInventoryJEforQAtransaction(JSONObject params) throws ServiceException ;    

}
