/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.location;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.InventoryLocation;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public interface LocationService {

    public void addLocation(User user, Location location) throws ServiceException;

    public void updateLocation(User user, Location location) throws ServiceException;

    public Location getDefaultLocation(Company company) throws ServiceException;

    public Location getLocation(String locationId) throws ServiceException;
    
    public InventoryLocation getERPLocation(String locationId) throws ServiceException;

    public List<Location> getLocations(Company company, String searchString, Paging paging) throws ServiceException;

    public void activateLocation(User user, Location location) throws ServiceException;

    public void deactivateLocation(User user, Location location) throws ServiceException;

    public void setLocationAsDefault(User user, Location location) throws ServiceException;

    public Location getLocationByName(Company company, String locationName) throws ServiceException;
    
    public boolean getCompanPreferencesSql(String companyId);
    
     public boolean isStockInLocation(Location location) throws ServiceException ;

}
