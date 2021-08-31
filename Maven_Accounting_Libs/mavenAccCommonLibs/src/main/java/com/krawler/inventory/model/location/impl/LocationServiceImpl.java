/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.location.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.InventoryLocation;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationDAO;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.store.StorageException;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vipin Gupta
 */
public class LocationServiceImpl implements LocationService {

    private LocationDAO locationDAO;

    public void setLocationDAO(LocationDAO locationDAO) {
        this.locationDAO = locationDAO;
    }

    @Override
    public void addLocation(User user, Location location) throws ServiceException {
        try {
            if (location == null) {
                throw new StorageException(StorageException.Type.NULL, "Location is null");
            }
            if (location.getCompany() == null) {
                throw new StorageException(StorageException.Type.NULL, "Company for Location is null");
            }
            
            if (isLocationExists(location)) {
                throw new StorageException(StorageException.Type.NULL, "Location [" + location.getName() + "] already exists.");
            }
            
            List<Location> locationList = getLocations(location.getCompany(), null, null);
            if (locationList.isEmpty()) {
                location.setDefaultLocation(true);
            } else {
                location.setDefaultLocation(false);
            }
            location.setCreatedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            location.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            location.setCreatedBy(user);
            location.setModifiedBy(user);
            locationDAO.saveOrUpdate(location);
            
            InventoryLocation il = new InventoryLocation();
            il.setId(location.getId());
            il.setName(location.getName());
            il.setIsdefault(location.isDefaultLocation());
            il.setCompany(location.getCompany());
            il.setParentId(location.getParentId());
            locationDAO.saveOrUpdate(il);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(LocationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(LocationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void updateLocation(User user, Location location) throws ServiceException {
        try {
            if (location == null) {
                throw new StorageException(StorageException.Type.NULL, "Location is null");
            }
            if (location.getCompany() == null) {
                throw new StorageException(StorageException.Type.NULL, "Company for Location is null");
            }
            Location existingLocation = getOtherLocationByName(location);
            if (existingLocation != null && !(existingLocation.getId().equalsIgnoreCase(location.getId()))) {
                throw new StorageException(StorageException.Type.ALREADY_EXISTS, "Location [" + location.getName() + "] already exists.");
            }
            
            location.setModifiedOn(authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date())));
            location.setModifiedBy(user);
            locationDAO.saveOrUpdate(location);
            
            InventoryLocation il = locationDAO.getERPLocation(location.getId());
            if (il != null) {
                il.setName(location.getName());
                il.setIsdefault(location.isDefaultLocation());
                locationDAO.saveOrUpdate(il);
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(LocationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(LocationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public Location getDefaultLocation(Company company) throws ServiceException {
        if (company == null) {
            throw new StorageException(StorageException.Type.NULL, "Company for Location is null");
        }
        return locationDAO.getDefaultLocation(company);
    }

    @Override
    public Location getLocation(String locationId) throws ServiceException {
        Location location = null;
        if (!StringUtil.isNullOrEmpty(locationId)) {
            location = locationDAO.getLocation(locationId);
        }
        return location;
    }

    @Override
    public InventoryLocation getERPLocation(String locationId) throws ServiceException {
        InventoryLocation il = null;
        if (!StringUtil.isNullOrEmpty(locationId)) {
            il = locationDAO.getERPLocation(locationId);
        }
        return il;
    }

    @Override
    public List<Location> getLocations(Company company, String searchString, Paging paging) throws ServiceException {
        if (company == null) {
            throw new StorageException(StorageException.Type.NULL, "Company for Location is null");
        }
        return locationDAO.getLocations(company, searchString, paging);
    }

    private boolean isLocationExists(Location location) throws ServiceException {
        boolean exists = false;
        Location loc = locationDAO.getLocationByName(location.getCompany(), location.getName());
        if (loc != null && !loc.equals(location)) {
            exists = true;
        }
        return exists;
    }

    private Location getOtherLocationByName(Location location) throws ServiceException {
        if (location == null) {
            throw new StorageException("location cannot be empty");
        }
        if (location.getCompany() == null) {
            throw new StorageException(StorageException.Type.NULL, "Company Object is null.");
        }
        if (StringUtil.isNullOrEmpty(location.getName())) {
            throw new StorageException(StorageException.Type.NULL, "Location name is null or empty.");
        }
        return locationDAO.getOtherLocationByName(location);
    }

    @Override
    public void activateLocation(User user, Location location) throws ServiceException {
        if (location == null) {
            throw new StorageException(StorageException.Type.NULL, "Location Object is null.");
        }
        if (location.isActive()) {
            throw new StorageException(StorageException.Type.NULL, "Location is already active.");
        }

        location.setActive(true);
        location.setModifiedBy(user);
        location.setModifiedOn(new Date());
        locationDAO.saveOrUpdate(location);
    }

    @Override
    public void deactivateLocation(User user, Location location) throws ServiceException {
        if (location == null) {
            throw new StorageException(StorageException.Type.NULL, "Location Object is null.");
        }
        if (!location.isActive()) {
            throw new StorageException(StorageException.Type.NULL, "Location is already deactive.");
        }
        location.setActive(false);
        location.setModifiedBy(user);
        location.setModifiedOn(new Date());
        locationDAO.saveOrUpdate(location);
    }

    @Override
    public void setLocationAsDefault(User user, Location location) throws ServiceException {
        if (location == null) {
            throw new StorageException(StorageException.Type.NULL, "Location Object is null.");
        }
        if (!location.isActive()) {
            throw new StorageException(StorageException.Type.NULL, "You can set location as default only for active location.");
        }
        Location loc = getDefaultLocation(location.getCompany());
        if (loc != null) {
            loc.setDefaultLocation(false);
            locationDAO.saveOrUpdate(location);
        }
        location.setDefaultLocation(true);
        locationDAO.saveOrUpdate(location);
    }

    @Override
    public boolean getCompanPreferencesSql(String companyId) {
        boolean isInventoryTabOn = false;
        try {

            KwlReturnObject presult = locationDAO.getCompanPreferencesSql(companyId);

            if (presult.getEntityList() != null && !presult.getEntityList().isEmpty()) {
                Object[] prefObj = (Object[]) presult.getEntityList().get(0);
                if (prefObj.length > 0) {
                    isInventoryTabOn = (Boolean) prefObj[5];
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(LocationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isInventoryTabOn;
    }

    @Override
    public Location getLocationByName(Company company, String locationName) throws ServiceException {
        if (StringUtil.isNullOrEmpty(locationName)) {
            throw new StorageException(StorageException.Type.NULL, "location name is null or empty.");
        }
        return locationDAO.getLocationByName(company, locationName);
    }
    @Override
     public boolean isStockInLocation(Location location) throws ServiceException {
        
        return locationDAO.isStockInLocation(location);
    }
}
