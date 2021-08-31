/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.packaging;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public interface PackagingService {

    public void addOrUpdatePackaging(Packaging packaging) throws ServiceException;

    public Packaging getPackaging(String packagingId) throws ServiceException;

    public Packaging createClonePackaging(Packaging packaging) throws ServiceException;

    public Packaging createPackagingByStockUom(UnitOfMeasure stockUom) throws ServiceException;

    public KwlReturnObject getProductBaseUOMRate(HashMap<String, Object> request) throws ServiceException;
}
