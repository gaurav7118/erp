/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.threshold.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Product;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.threshold.ProductThreshold;
import com.krawler.inventory.model.threshold.ThresholdDAO;
import com.krawler.inventory.model.threshold.ThresholdException;
import com.krawler.inventory.model.threshold.ThresholdService;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Vipin Gupta
 */
public class ThresholdServiceImpl implements ThresholdService {

    private ThresholdDAO thresholdDAO;

    public void setThresholdDAO(ThresholdDAO thresholdDAO) {
        this.thresholdDAO = thresholdDAO;
    }

    @Override
    public void addOrUpdateProductThreshold(Product product, Store store, double thresholdLimit) throws ServiceException {
        if (product == null || store == null) {
            throw new ThresholdException("Product or store cannot be empty for Threshold");
        }
        ProductThreshold productThreshold = getProductThreshold(product, store);
        if (productThreshold != null) {
            productThreshold.setThresholdLimit(thresholdLimit);
        } else {
            productThreshold = new ProductThreshold(product, store, thresholdLimit);
        }
        thresholdDAO.saveOrUpdateProductThreshold(productThreshold);
    }

    @Override
    public void addOrUpdateProductThreshold(ProductThreshold pt) throws ServiceException {
        if (pt == null) {
            throw new ThresholdException("Product or store cannot be empty for saving Threshold");
        }
        if (pt.getCompany() == null || pt.getStore() == null || pt.getProduct() == null) {
            throw new ThresholdException("Company or Product or store cannot be empty for saving Threshold");
        }
        addOrUpdateProductThreshold(pt.getProduct(), pt.getStore(), pt.getThresholdLimit());
    }

    @Override
    public ProductThreshold getProductThreshold(String thresholdId) {
        ProductThreshold pt = null;
        if (!StringUtil.isNullOrEmpty(thresholdId)) {
            pt = thresholdDAO.getProductThreshold(thresholdId);
        }
        return pt;
    }

    @Override
    public ProductThreshold getProductThreshold(Product product, Store store) throws ServiceException {
        if (product == null || store == null) {
            throw new ThresholdException("Product or store cannot be empty for getting Threshold");
        }
        return thresholdDAO.getProductThreshold(product, store);
    }

    @Override
    public List<ProductThreshold> getStoreWiseThresholdList(Store store, String searchString, Paging paging) throws ServiceException {
        if (store == null) {
            throw new ThresholdException("store cannot be empty for getting Threshold by store");
        }
        return thresholdDAO.getStoreWiseThresholdList(store, searchString, paging);
    }

    @Override
    public List<ProductThreshold> getProductWiseThresholdList(Product product, String searchString, Paging paging) throws ServiceException {
        if (product == null) {
            throw new ThresholdException("product cannot be empty for getting Threshold by product");
        }
        return thresholdDAO.getProductWiseThresholdList(product, searchString, paging);
    }

    @Override
    public List<Map<String, Object>> getThresholdStockList(Company company, Store store, String searchString, Paging paging) throws ServiceException  {
        if (store == null) {
            throw new ThresholdException("store is required for getting Threshold Stock List");
        }
        return thresholdDAO.getThresholdStockList(company, store, searchString, paging);
    }
}
