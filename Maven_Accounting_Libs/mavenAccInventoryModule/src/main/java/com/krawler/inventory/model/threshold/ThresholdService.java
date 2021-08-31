/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.threshold;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.store.Store;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public interface ThresholdService {

    public void addOrUpdateProductThreshold(Product product, Store store, double thresholdLimit) throws ServiceException;

    public void addOrUpdateProductThreshold(ProductThreshold productThreshold) throws ServiceException;

    public ProductThreshold getProductThreshold(String thresholdId);

    public ProductThreshold getProductThreshold(Product product, Store store) throws ServiceException;

    public List<ProductThreshold> getStoreWiseThresholdList(Store store, String searchString, Paging paging) throws ServiceException;

    public List<ProductThreshold> getProductWiseThresholdList(Product product, String searchString, Paging paging) throws ServiceException;

    public List<Map<String, Object>> getThresholdStockList(Company company, Store store, String searchString, Paging paging) throws ServiceException ;
}
