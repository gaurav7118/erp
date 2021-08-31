/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.configuration;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;

/**
 *
 * @author Vipin Gupta
 */
public interface InventoryConfigDAO {

    public void saveOrUpdateConfig(InventoryConfig config) throws ServiceException;

    public InventoryConfig getConfigByCompany(Company company) throws ServiceException;

    public InventoryConfig getConfigById(String configId) throws ServiceException;
    
}
