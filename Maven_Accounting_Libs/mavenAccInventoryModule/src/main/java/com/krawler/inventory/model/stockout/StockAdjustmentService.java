/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockout;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.exception.NegativeInventoryException;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Vipin Gupta
 */
public interface StockAdjustmentService {

    public void requestStockAdjustment(User user, StockAdjustment stockAdjustment) throws ServiceException, NegativeInventoryException;

    public void requestStockAdjustment(User user, StockAdjustment stockAdjustment, boolean allowNegativeInventory, boolean sendForApproval,String customfield,HashMap<String,Object> requestparams) throws ServiceException, NegativeInventoryException;

    public void addStockAdjustmentWithStockMovement(User user, StockAdjustment sa, boolean allowNegativeInventory, String smInRemark, String smOutRemark) throws ServiceException;
            
    public void approveStockAdjustment(User user, StockAdjustment stockAdjustment) throws ServiceException, NegativeInventoryException;

    public void approveStockAdjustment(User user, StockAdjustment stockAdjustment, boolean allowNegativeInventory) throws ServiceException, NegativeInventoryException;

//    public void rejectStockAdjustment(User user, StockAdjustment stockAdjustment) throws ServiceException;
    public StockAdjustment getStockAdjustmentById(String id) throws ServiceException;

    public List<StockAdjustment> getStockAdjustmentBySequenceNo(Company company, String sequenceNo) throws ServiceException;
    
    public double getTotalAmountOFSABySequenceNo(Company company,String sequenceNo) throws ServiceException;

    public List<StockAdjustment> getStockAdjustmentList(Company company, Set<Store> storeSet, Product product, Set<AdjustmentStatus> status, String adjustmentType, Date fromDate, Date toDate, String searchString, Paging paging, HashMap<String, Object> requestParams) throws ServiceException;
    
    public List<StockAdjustment> getStockAdjustmentRows(HashMap<String, Object> requestParams) throws ServiceException;

    public List<StockAdjustment> getStockAdjustmentSummary(Company company, Set<Store> storeSet, Product product, AdjustmentStatus status, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;

    public void createStockAdjustmentDraft(User user, StockAdjustmentDraft stockAdjustmentDraft) throws ServiceException;

    public void removeDraft(User user, StockAdjustmentDraft stockAdjustmentDraft) throws ServiceException;

    public StockAdjustmentDraft getStockAdjustmentDraftById(String id) throws ServiceException;

    public List<StockAdjustmentDraft> getStockAdjustmentDraftList(Company company, Paging paging) throws ServiceException;

    public void saveSADetailInTemporaryTable(Product product, Store store, Location location, String batchName, Map<String, Object> tempTablMap) throws ServiceException, ParseException;
    public void saveSADetailInTemporaryTable(Product product, Store store, Location location, String batchName, Map<String, Object> tempTablMap,SimpleDateFormat df) throws ServiceException, ParseException;

    public JSONObject deleteSA(String saId, Company company, User user, boolean isPermanent) throws ServiceException, ParseException;
    
    public int stockOutCreatedFromOtherTransaction(JSONObject jSONObject) throws ServiceException;
    
    public void deleteStockadjustmentForBuildassemby(Company company, String productbuildid,User user) throws ServiceException;

}
