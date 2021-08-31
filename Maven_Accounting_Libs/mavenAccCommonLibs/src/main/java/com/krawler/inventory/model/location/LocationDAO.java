/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.location;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.InventoryLocation;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.spring.common.KwlReturnObject;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public interface LocationDAO {

    public void saveOrUpdate(Object object) throws ServiceException;

    public InventoryLocation getERPLocation(String locationId) throws ServiceException;

    public Location getLocation(String locationId) throws ServiceException;

    public List<Location> getLocations(Company company, String searchString, Paging paging) throws ServiceException;

    public Location getLocationByName(Company company, String name) throws ServiceException;

    public Location getDefaultLocation(Company company) throws ServiceException;

    public KwlReturnObject getCompanPreferencesSql(String companyID) throws ServiceException;

    public Location getOtherLocationByName(Location location) throws ServiceException;

    public boolean isStockInLocation(Location location) throws ServiceException;
}
