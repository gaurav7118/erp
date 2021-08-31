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
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface ConsignmentService {

    public Consignment getConsingmentById(String consignmentId);

    public ConsignmentApprovalDetails getConsingmentDetailsById(String consignmentId);

    public List<Consignment> getConsingmentList(String searchString, Paging paging) throws ServiceException;

    public void approveConsignmentDetail(User inspector, ConsignmentApprovalDetails consignment, InspectionDetail inspDTL, Company company, double returnqty, boolean fromRepair, Store qaStore, Store repairStore) throws ServiceException;

    public void rejectConsignmentDetail(User inspector, ConsignmentApprovalDetails consignment, InspectionDetail inspDTL, Company company, double qty,
            boolean fromRepair, Store qaStore, Store repairStore) throws ServiceException;

    public Map rejectedApprovedItemsDetail(ConsignmentApprovalDetails consignment) throws ServiceException;

    public void addOrUpdateConsignment(Company company, String moduleRefId, List<Consignment> consignment) throws ServiceException;

    public void deletePreviousConsignmentQAForSR(Company company, String salesReturnId) throws ServiceException;

    public int isQAApprovePermissionForUser(Company company, String storeid, String locationid, String userId) throws ServiceException;

    public KwlReturnObject getAllQAList(String companyId, Date fromDate, Date toDate, String moduleType, String statusType, Set<Store> storeSet, String searchString, Paging paging,String TZDiff,boolean isQAapprovalForBuildAssembly,boolean  isisJobWorkOrderInQA) throws ServiceException;

    public KwlReturnObject getAllQARepairPendingList(String companyId, Date fromDate, Date toDate, String moduleType, String statusType, Set<Store> storeSet, String searchString, Paging paging,String TZDiff,boolean isQAapprovalForBuildAssembly) throws ServiceException;

    public KwlReturnObject getAllRepairList(String companyId, String statusType, Store store, Date fromDate, Date toDate, String searchString, Paging paging,String TZDiff,boolean isQAapprovalForBuildAssembly) throws ServiceException;

    @Deprecated
    public KwlReturnObject assignStockToPendingConsignmentRequests(HttpServletRequest request) throws ServiceException, SessionExpiredException;
    
    public KwlReturnObject assignStockToPendingConsignmentRequests(JSONObject request) throws ServiceException, SessionExpiredException;

    @Deprecated
    public KwlReturnObject assignStockToPendingConsignmentRequests(HttpServletRequest request, Company company, User user) throws ServiceException,SessionExpiredException;
    
    public KwlReturnObject assignStockToPendingConsignmentRequests(JSONObject request, Company company, User user) throws ServiceException;

    public void creatStockmovementForConsignmentQAApproval(Company company, List<ConsignmentApprovalDetails> approrejectList, Store qaStore, Store repairStore) throws ServiceException;

    public void createStockMovementForRepairing(Company company, List<ConsignmentApprovalDetails> approrejectList, Store repairStore) throws ServiceException;

    public boolean isPendingForApproval(String consignmentReturnId) throws ServiceException;
    
    public String getSalesPersonEmailIdBySRDetailId(String srDetailid,String companyId) throws ServiceException;

    public KwlReturnObject getBuildAssemblyProductQaDetails(Map<String,String>requestMap) throws ServiceException;
    
    public String getSalesReturnMemo(Company company, String salesReturnId) throws ServiceException;
}
