/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.sa;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.approval.stocktransfer.StockTransferDetailApproval;
import com.krawler.inventory.model.stockout.StockAdjustment;
import com.krawler.inventory.model.stockrequest.StockRequestDetail;
import com.krawler.inventory.model.store.Store;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public interface SAApprovalService {

    public SAApproval getSAApproval(String saApprovalId) throws ServiceException;

    public SADetailApproval getSADetailApproval(String saDetailApprovalId) throws ServiceException;

    public List<SAApproval> getStockAdjutmentApprovalList(String searchString, Paging paging) throws ServiceException;

    public List<SADetailApproval> getStockAdjutmentDetailApprovalList(SAApproval saApproval, Paging paging) throws ServiceException;

    public void addStockoutApproval(StockAdjustment stockAdjustment,HashMap<String, Object> requestParams) throws ServiceException;

    public void approveStockAdjustmentDetail(User inspector, SADetailApproval saDetailApproval, InspectionDetail inspectionDetail, double quantity, boolean fromRepair) throws ServiceException;

    public void rejectStockAdjustmentDetail(User inspector, SADetailApproval saDetailApproval, InspectionDetail inspectionDetail, double quantity, boolean fromRepair) throws ServiceException;

    public Map rejectedApprovedSAItemsDetail(SADetailApproval sada) throws ServiceException;

    public int getAttachmentCount(Company company, String moduleWiseMainId) throws ServiceException;

    public void createStockMovementForQAApproval(Company company,List<SADetailApproval> approvedRejectedRecords, Store qaStore, Store repairStore) throws ServiceException;
    
    public void createStockMovementForRepairing(Company company,List<SADetailApproval> approvedRejectedRecords, Store repairStore) throws ServiceException;
}
