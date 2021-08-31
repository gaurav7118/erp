/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.configuration.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.configuration.InventoryConfig;
import com.krawler.inventory.model.configuration.InventoryConfigDAO;
import com.krawler.inventory.model.configuration.InventoryConfigService;
import java.util.Date;

/**
 *
 * @author Vipin Gupta
 */
public class InventoryConfigServiceImpl implements InventoryConfigService {

    InventoryConfigDAO configDAO;

    public void setConfigDAO(InventoryConfigDAO configDAO) {
        this.configDAO = configDAO;
    }

    @Override
    public void addConfig(User user, InventoryConfig config) throws ServiceException {
        if (config == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Inventory Configuration is null");
        }
        config.setCompany(user.getCompany());
        config.setCreatedBy(user);
        config.setModifiedBy(user);
        config.setCreatedOn(new Date());
        config.setModifiedOn(new Date());
        configDAO.saveOrUpdateConfig(config);
    }

    @Override
    public void updateConfig(User user, InventoryConfig config) throws ServiceException {
        if (config == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Inventory Configuration is null");
        }
        config.setModifiedBy(user);
        config.setModifiedOn(new Date());
        configDAO.saveOrUpdateConfig(config);
    }

    @Override
    public InventoryConfig getConfigById(String configId) throws ServiceException {
        if (StringUtil.isNullOrEmpty(configId)) {
            throw new InventoryException(InventoryException.Type.NULL, "Config id is null");
        }
        InventoryConfig ic = configDAO.getConfigById(configId);
        if (ic == null) {
            ic = new InventoryConfig();
        }
        return ic;
    }

    @Override
    public InventoryConfig getConfigByCompany(Company company) throws ServiceException {
        if (company == null) {
            throw new InventoryException(InventoryException.Type.NULL, "Company is null");
        }
        InventoryConfig ic = configDAO.getConfigByCompany(company);
        if (ic == null) {
            ic = new InventoryConfig();
        }
        return ic;
    }
}
