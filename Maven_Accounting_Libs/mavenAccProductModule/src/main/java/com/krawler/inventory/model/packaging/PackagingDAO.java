/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.packaging;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public interface PackagingDAO {

    public void saveOrUpdate(Object object);

    public Packaging getPackaging(String packagingId);

    public KwlReturnObject getProductBaseUOMRate(HashMap<String, Object> request) throws ServiceException;
}
