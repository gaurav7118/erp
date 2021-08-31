/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.configuration.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.inventory.model.configuration.InventoryConfig;
import com.krawler.inventory.model.configuration.InventoryConfigDAO;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public class InventoryConfigDAOImpl extends BaseDAO implements InventoryConfigDAO{

    @Override
    public void saveOrUpdateConfig(InventoryConfig config) throws ServiceException {
        super.saveOrUpdate(config);
    }

    @Override
    public InventoryConfig getConfigByCompany(Company company) throws ServiceException {
        InventoryConfig inventoryConfig = null;
        String hql = "FROM InventoryConfig WHERE company = ? ";
        List list = executeQuery( hql, company);
        if(!list.isEmpty()){
            inventoryConfig = (InventoryConfig) list.get(0);
        }
        return inventoryConfig;
    }

    @Override
    public InventoryConfig getConfigById(String configId) throws ServiceException {
        return (InventoryConfig) get(InventoryConfig.class, configId);
    }
    
}
