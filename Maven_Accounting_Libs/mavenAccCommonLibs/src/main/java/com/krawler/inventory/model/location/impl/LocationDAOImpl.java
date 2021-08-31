/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.inventory.model.location.impl;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.InventoryLocation;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationDAO;
import com.krawler.spring.common.KwlReturnObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Vipin Gupta
 */
public class LocationDAOImpl extends BaseDAO implements LocationDAO {

    @Override
    public void saveOrUpdate(Object object) throws ServiceException {
        super.saveOrUpdate(object);
    }

    @Override
    public InventoryLocation getERPLocation(String locationId) throws ServiceException {
        return (InventoryLocation) get(InventoryLocation.class, locationId);
    }

    @Override
    public Location getLocation(String locationId) throws ServiceException {
        return (Location) get(Location.class, locationId);
    }

    @Override
    public List<Location> getLocations(Company company, String searchString, Paging paging) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM Location WHERE company = ? ");
        List params = new ArrayList();
        params.add(company);
        if (!StringUtil.isNullOrEmpty(searchString)) {
            hql.append(" AND name LIKE ? ");
            params.add("%" + searchString + "%");
        }
        hql.append(" ORDER BY name ");
        List list = executeQuery(hql.toString(), params.toArray());
        int totalCount = list.size();
        if (paging != null) {
            paging.setTotalRecord(totalCount);
            list = executeQueryPaging(hql.toString(), params.toArray(), paging);
        }
        return list;
    }

    @Override
    public Location getLocationByName(Company company, String name) throws ServiceException {
        Location location = null;
        StringBuilder hql = new StringBuilder("FROM Location WHERE company = ? AND name= ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(name);

        Paging paging = new Paging(0, 1);
        List list = executeQueryPaging(hql.toString(), params.toArray(), paging);
        if (!list.isEmpty()) {
            location = (Location) list.get(0);
        }
        return location;
    }

    @Override
    public Location getDefaultLocation(Company company) throws ServiceException {
        Location location = null;
        StringBuilder hql = new StringBuilder("FROM Location WHERE company = ? AND defaultLocation = ? ");
        List params = new ArrayList();
        params.add(company);
        params.add(true);

        Paging paging = new Paging(0, 1);
        List list = executeQueryPaging(hql.toString(), params.toArray(), paging);
        if (!list.isEmpty()) {
            location = (Location) list.get(0);
        }
        return location;
    }
    @Override
    public KwlReturnObject getCompanPreferencesSql(String companyID) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        ArrayList params = new ArrayList();
        try {
            params.add(companyID);
            HashMap<String, String> compPref;
            String query = "select c.islocationcompulsory,c.iswarehousecompulsory,c.isrowcompulsory ,c.israckcompulsory, c.isbincompulsory,extracomp.activateInventoryTab from CompanyAccountPreferences c,ExtraCompanyPreferences extracomp where c.ID=extracomp.id and c.company.companyID = ? ";
            returnList = executeQuery(query, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public Location getOtherLocationByName(Location location) throws ServiceException {
        StringBuilder hql = new StringBuilder("FROM Location WHERE company = ? AND name = ? AND id <> ? ");
        List params = new ArrayList();
        params.add(location.getCompany());
        params.add(location.getName());
        params.add(location.getId());
        List list = executeQueryPaging(hql.toString(), params.toArray(), new Paging(0, 1));
        Location otherLocation = null;
        if (!list.isEmpty()) {
            otherLocation = (Location) list.get(0);
        }
        return otherLocation;
    }
    @Override
    public boolean isStockInLocation(Location location) throws ServiceException {
        boolean isAvialableStock=false;
        StringBuilder hql = new StringBuilder("FROM Stock WHERE company = ? AND location = ? AND quantity >0 ");
        List params = new ArrayList();
        params.add(location.getCompany());
        params.add(location);
        List list = executeQueryPaging(hql.toString(), params.toArray(), new Paging(0, 1));
        Location otherLocation = null;
        if (!list.isEmpty() && list!=null) {
           if(list.size()>0){
               isAvialableStock=true;
           }
        }
        return isAvialableStock;
    }
}
