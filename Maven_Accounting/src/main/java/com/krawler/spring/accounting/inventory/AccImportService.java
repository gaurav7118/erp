/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.accounting.inventory;

import com.krawler.common.admin.LocationBatchDocumentMapping;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.admin.StoreMaster;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Product;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccImportService {
    
    public boolean isProductUsedInTransaction(String companyId,String productId) throws ServiceException;
    
    public void saveOrUpdateObj(Object object) throws ServiceException;
    
    public NewProductBatch getNewProductBatchById(String id) throws ServiceException;
    
    public void deleteLocationBatchDocumentMappingByBatchMapId(String batchMapId) throws ServiceException;
    
    public KwlReturnObject deleteProductBatchSerialDetails(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getProductOpeningQtyFromBatchSerial(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject addStockInventorySide(KwlReturnObject newInvObj,JSONObject inventoryjson,String productDefaultWarehouseID,String productDefaultLocationID,double prodInitPurchasePrice) throws ServiceException;
    
    public KwlReturnObject updateStockInventorySide(KwlReturnObject updatedInvObj,JSONObject inventoryjson, LocationBatchDocumentMapping lbm,double prodInitPurchasePrice) throws ServiceException,AccountingException;
    
    public KwlReturnObject getProductOpeningQtyBatchDetail(Product product) throws ServiceException;
    
    public void deleteStockAndSMForProduct(Product product) throws ServiceException; // need to call only if product is not used in transaction ie.product is not used

}
