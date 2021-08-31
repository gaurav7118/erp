/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.stockout;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.approval.sa.SADetailApproval;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.inventory.model.stockmovement.TransactionType;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.*;

/**
 *
 * @author Vipin Gupta
 */
public interface StockAdjustmentDAO {

    public void saveOrUpdateAdjustment(StockAdjustment stockAdjustment) throws ServiceException;

    public StockAdjustment getStockAdjustmentById(String id) throws ServiceException;
    
    public void deleteSAPermanently(StockAdjustment stockAdjustment) throws ClassNotFoundException,  ServiceException;
    
    public List<StockAdjustment> getStockAdjustmentBySequenceNo(Company company,String sequenceNo) throws ServiceException;
    
    public List getStockAdjustmentByProductBuild(Company company,String productbuild) throws ServiceException;
    
    public double getTotalAmountOFSABySequenceNo(Company company,String sequenceNo) throws ServiceException;

    public List<StockAdjustment> getStockAdjustmentList(Company company, Set<Store> storeSet, Product product, Set<AdjustmentStatus> status,String adjustmentType, Date fromDate, Date todate, String searchString, Paging paging, HashMap<String, Object> requestParams) throws ServiceException;

    public List<StockAdjustment> getStockAdjustmentSummary(Company company, Set<Store> storsSet, Product product, AdjustmentStatus status, Date fromDate, Date toDate, String searchString, Paging paging) throws ServiceException;

    public void saveOrUpdateDraft(StockAdjustmentDraft stockAdjustmentDraft) throws ServiceException;

    public StockAdjustmentDraft getStockAdjustmentDraftById(String id) throws ServiceException;

    public List<StockAdjustmentDraft> getStockAdjustmentDraftList(Company company, Paging paging) throws ServiceException;

    public void removeDraft(StockAdjustmentDraft stockAdjustmentDraft) throws ServiceException;

    public StockAdjustmentDraft getDraftByStoreAndBussinessDate(Company company, Store store, Date businessDate) throws ServiceException;

    public void saveSADetailInTemporaryTable(String product, String store, String location, String batchName, String serialNames, Date mfgdate, Date expdate, Date warrantyexpfromdate, Date warrantyexptodate,String sku)throws ServiceException;
    
    public List getBatchSerialDataFromTemporaryTable(String product,String store,String location,String batchName, String serialName)throws ServiceException;
    
    public void setBatchSerialDateDetail(Company company)throws ServiceException;
    
    public KwlReturnObject getStockAdjustmentJEs(Map<String, Object> request) throws ServiceException;
    
    public List<StockAdjustment> getStockAdjustmentRows(Map<String, Object> request) throws ServiceException;
    
    public List<SADetailApproval> getStockAdjustmentApprovalDetail(String stockAdjustmentDetailID) throws ServiceException;
    
    public KwlReturnObject getSAfromGRN(JSONObject nObject) throws ServiceException;
    
    public KwlReturnObject getSAfromDO(JSONObject nObject) throws ServiceException;
    
    public KwlReturnObject checkStockAdjustmentForQC(JSONObject params) throws ServiceException;
    
}
