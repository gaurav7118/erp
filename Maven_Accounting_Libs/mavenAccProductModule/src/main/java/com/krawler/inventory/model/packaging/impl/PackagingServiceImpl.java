/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.packaging.impl;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.inventory.model.packaging.PackagingDAO;
import com.krawler.inventory.model.packaging.PackagingService;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public class PackagingServiceImpl implements PackagingService {

    private PackagingDAO packagingDAO;

    public void setPackagingDAO(PackagingDAO packagingDAO) {
        this.packagingDAO = packagingDAO;
    }

    @Override
    public void addOrUpdatePackaging(Packaging packaging) throws ServiceException {

        packagingDAO.saveOrUpdate(packaging);
    }

    @Override
    public Packaging getPackaging(String packagingId) throws ServiceException {
        return packagingDAO.getPackaging(packagingId);
    }

    @Override
    public Packaging createClonePackaging(Packaging p) throws ServiceException {
        Packaging clone = null;
        if (p != null) {
            clone = new Packaging();
            clone.setCompany(p.getCompany());
            clone.setCasingUoM(p.getCasingUoM());
            clone.setInnerUoM(p.getInnerUoM());
            clone.setStockUoM(p.getStockUoM());
            clone.setCasingUomValue(p.getCasingUomValue());
            clone.setInnerUomValue(p.getInnerUomValue());
            clone.setStockUomValue(p.getStockUomValue());
            packagingDAO.saveOrUpdate(clone);
        }
        return clone;
    }

    @Override
    public Packaging createPackagingByStockUom(UnitOfMeasure stockUom) throws ServiceException {
        Packaging packaging = null;
        if (stockUom != null) {
            packaging = new Packaging(stockUom.getCompany(), stockUom, 1);
            packagingDAO.saveOrUpdate(packaging);
        }
        return packaging;

    }

    @Override
    public KwlReturnObject getProductBaseUOMRate(HashMap<String, Object> request) throws ServiceException {
        return packagingDAO.getProductBaseUOMRate(request);
    }
}
