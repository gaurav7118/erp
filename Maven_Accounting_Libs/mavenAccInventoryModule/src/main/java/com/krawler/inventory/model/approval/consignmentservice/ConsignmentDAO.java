/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.approval.consignmentservice;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.inventory.model.approval.ApprovalStatus;
import com.krawler.inventory.model.approval.InspectionDetail;
import com.krawler.inventory.model.approval.consignment.Consignment;
import com.krawler.inventory.model.approval.consignment.ConsignmentApprovalDetails;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface ConsignmentDAO {

    public Consignment getConsingmentById(String consignmentId);

    public ConsignmentApprovalDetails getConsingmentDetailsById(String consignmentId);

    public void saveOrUpdateConsignment(Object object) throws ServiceException;
    
    public void deletePreviousConsignmentQAForSR(Company company, String salesReturnId) throws ServiceException;

    public List<Consignment> getConsingmentList(String searchString, Paging paging) throws ServiceException;

    public int isQAApprovePermissionForUser(Company company, String storeid, String locationid, String userId) throws ServiceException;

    public KwlReturnObject getAllQAList(String companyId, Date fromDate, Date toDate, String moduleType, String statusType, Set<Store> storeSet, String searchString, Paging paging,String tzdiff,boolean isQAapprovalForBuildAssembly,boolean  isisJobWorkOrderInQA) throws ServiceException;

    public KwlReturnObject getAllQARepairPendingList(String companyId, Date fromDate, Date toDate, String moduleType, String statusType, Set<Store> storeSet, String searchString, Paging paging,String tzdiff,boolean isQAapprovalForBuildAssembly) throws ServiceException;

    public KwlReturnObject getSODetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAllRepairList(String companyId, String statusType, Store store, Date fromDate, Date toDate, String searchString, Paging paging,String tzdiff,boolean isQAapprovalForBuildAssembly) throws ServiceException;

    public boolean isPendingForApproval(String consignmentReturnId) throws ServiceException;
    
    public String getSalesPersonEmailIdBySRDetailId(String srDetailid,String companyId) throws ServiceException;

    public KwlReturnObject getBuildProductsAssemblyQaDetails(Map<String, String> requestMap)throws ServiceException;
    // Added Functions for Document designer
    public KwlReturnObject getConsignmentObjectByTransactionNumber(Map<String, Object> requestMap) throws ServiceException; 
    
    public KwlReturnObject getBuildAssemblyObjectByTransactionNumber(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getSAApprovedObjectByTransactionNumber(Map<String, Object> requestMap) throws ServiceException; 
    
    public KwlReturnObject getStoreOrInterStockObjectByTransactionNumber(Map<String, Object> requestMap) throws ServiceException; 
    
    public KwlReturnObject getStockTransfer(Map<String, Object> requestMap) throws ServiceException; 
    
    public KwlReturnObject getStockTransferDetail(Map<String, Object> requestMap) throws ServiceException; 
    
    public String getSalesReturnMemo(Company company, String salesReturnId) throws ServiceException;
    
}
