/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.configuration;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.inventory.exception.InventoryException;

/**
 *
 * @author Vipin Gupta
 */
public interface InventoryConfigService {

    public void addConfig(User user, InventoryConfig config) throws ServiceException;

    public void updateConfig(User user, InventoryConfig config) throws ServiceException;

    public InventoryConfig getConfigById(String configId) throws ServiceException;

    public InventoryConfig getConfigByCompany(Company company) throws ServiceException;
}
