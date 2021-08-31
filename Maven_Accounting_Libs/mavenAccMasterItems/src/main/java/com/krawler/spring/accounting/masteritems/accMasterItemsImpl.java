/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.accounting.masteritems;

import com.krawler.common.admin.*;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.LandingCostAllocationType;
import com.krawler.common.admin.LandingCostCategory;
import static com.krawler.common.admin.ProjectFeature.Product;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.Paging;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.DefaultMasterItem;
import com.krawler.hql.accounting.MasterGroup;
import com.krawler.hql.accounting.MasterItem;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.beans.Encoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class accMasterItemsImpl extends BaseDAO implements accMasterItemsDAO {

    public KwlReturnObject addMasterItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            MasterItem masterItem = new MasterItem();
            if (itemmap.containsKey("name")) {
                masterItem.setValue(URLEncoder.encode((String)itemmap.get("name"), Constants.DECODE_ENCODE_FORMAT));//Allow chinese characters to save ERP-24608
            }
            if (itemmap.containsKey("code") && itemmap.get("code") != null) {
                masterItem.setCode((String) itemmap.get("code"));
            }
            if (itemmap.containsKey("salesPersonContactNumber") && itemmap.get("salesPersonContactNumber") != null) {
                masterItem.setContactNumber((String) itemmap.get("salesPersonContactNumber"));
            }
            if (itemmap.containsKey("salesPersonAddress") && itemmap.get("salesPersonAddress") != null) {
                masterItem.setAddress((String) itemmap.get("salesPersonAddress"));
            }
            if (itemmap.containsKey("salesPersonDesignation") && itemmap.get("salesPersonDesignation") != null) {
                masterItem.setDesignation((String) itemmap.get("salesPersonDesignation"));
            }
            if (itemmap.containsKey("groupid")) {
                MasterGroup group = itemmap.get("groupid") == null ? null : (MasterGroup) get(MasterGroup.class, (String) itemmap.get("groupid"));
                masterItem.setMasterGroup(group);
            }
            if (itemmap.containsKey("parentid")) {
                MasterItem item1 = (MasterItem) get(MasterItem.class, (String) itemmap.get("parentid"));
                masterItem.setParent(item1);
            }
            if (itemmap.containsKey("isIBGActivated") && itemmap.get("isIBGActivated") != null) {
                masterItem.setIbgActivated((Boolean) itemmap.get("isIBGActivated"));
            }
            if (itemmap.containsKey("emailid")) {
                masterItem.setEmailID((String) itemmap.get("emailid"));
            }
            if (itemmap.containsKey("userid")) {
                User userObj = itemmap.get("userid") == null ? null : (User) get(User.class, (String) itemmap.get("userid"));
                masterItem.setUser(userObj);
            }
            if (itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                masterItem.setCompany(company);
            }
            if (itemmap.containsKey("accid")) {
                masterItem.setAccID((String) itemmap.get("accid"));
            }
            if (itemmap.containsKey("custVendCategoryTypeId") && itemmap.get("custVendCategoryTypeId") != null) {
                masterItem.setCustVendCategoryType((Integer) itemmap.get("custVendCategoryTypeId"));
            }
            if (itemmap.containsKey("driverID") && itemmap.get("driverID") != null) {
                MasterItem driver = (MasterItem) get(MasterItem.class, (String) itemmap.get("driverID"));
                masterItem.setDriver(driver);
            }
            if (itemmap.containsKey("isDefaultToPOS") && itemmap.get("isDefaultToPOS") != null) {
                masterItem.setDefaultToPOS((Boolean)itemmap.get("isDefaultToPOS"));
            }
            if (itemmap.containsKey("activated") && itemmap.get("activated") != null) {
                masterItem.setActivated((Boolean) itemmap.get("activated"));
            }
            if (itemmap.containsKey("variancePercentage") && itemmap.get("variancePercentage") != null) {
                masterItem.setVariancePercentage((Double) itemmap.get("variancePercentage"));
            }
            if (itemmap.containsKey("industryCodeId") && itemmap.get("industryCodeId") != null) {
                masterItem.setIndustryCodeId((String) itemmap.get("industryCodeId"));
            }
            if (itemmap.containsKey("parentCompanysMasterItemId") && itemmap.get("parentCompanysMasterItemId") !=null) {
                MasterItem item1 = (MasterItem) get(MasterItem.class, (String) itemmap.get("parentCompanysMasterItemId"));
                masterItem.setPropagatedMasteritemID(item1);
            }
            if (itemmap.containsKey("vatcommoditycode") && itemmap.get("vatcommoditycode") != null) {
                masterItem.setVatcommoditycode((String) itemmap.get("vatcommoditycode"));
            }
            if (itemmap.containsKey("vatscheduleno") && itemmap.get("vatscheduleno") != null) {
                masterItem.setVatscheduleno((String) itemmap.get("vatscheduleno"));
            }
            if (itemmap.containsKey("vatscheduleserialno") && itemmap.get("vatscheduleserialno") != null) {
                masterItem.setVatscheduleserialno((String) itemmap.get("vatscheduleserialno"));
            }
            if (itemmap.containsKey("vatnotes") && itemmap.get("vatnotes") != null) {
                masterItem.setVatnotes((String) itemmap.get("vatnotes"));
            }
            if (itemmap.containsKey("lcallocationid") && itemmap.get("lcallocationid") != null) {
                int temp=Integer.parseInt(itemmap.get("lcallocationid").toString());
                masterItem.setLcallocationid(LandingCostAllocationType.getByValue(temp));
            }
            if (itemmap.containsKey("lccategoryid") && itemmap.get("lccategoryid") != null) {
                int lccategoryid = Integer.parseInt(itemmap.get("lccategoryid").toString());
                masterItem.setLccategoryid((LandingCostCategory) get(LandingCostCategory.class, lccategoryid));
            }
            if (itemmap.containsKey("BICCode") && itemmap.get("BICCode") != null) {
                masterItem.setBICCode((String) itemmap.get("BICCode"));
            }
            if (itemmap.containsKey("bankCode") && itemmap.get("bankCode") != null) {
                masterItem.setBankCode((String) itemmap.get("bankCode"));
            }
            if (itemmap.containsKey("branchCode") && itemmap.get("branchCode") != null) {
                masterItem.setBranchCode((String) itemmap.get("branchCode"));
            }
            if (itemmap.containsKey("isAppendBranchCode") && !StringUtil.isNullOrEmpty((String)itemmap.get("isAppendBranchCode"))) {
                masterItem.setIsAppendBranchCode(Boolean.parseBoolean((String)itemmap.get("isAppendBranchCode")));
            }
            save(masterItem);
            list.add(masterItem);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }

    public KwlReturnObject addLocationItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            InventoryLocation inventoryLocation = new InventoryLocation();
            User user=(User)itemmap.get("user");
            boolean isEdit=false;
            String itemID = (String) itemmap.get("id");
            if (itemmap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                inventoryLocation = (InventoryLocation) get(InventoryLocation.class, itemID);
                if (inventoryLocation == null) {
                    inventoryLocation = new InventoryLocation();
                    inventoryLocation.setId(itemID);
                }
                isEdit=true;
            } else {
                String newitemID = UUID.randomUUID().toString();
                inventoryLocation.setId(newitemID);
            }
            if (itemmap.containsKey("name")) {
                inventoryLocation.setName((String) itemmap.get("name"));
            }
             if (itemmap.containsKey("parentid")) {
                inventoryLocation.setParentId((String) itemmap.get("parentid"));
            }
            if (itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                inventoryLocation.setCompany(company);
            }
            if (itemmap.containsKey("isdefault")) {
                inventoryLocation.setIsdefault((Boolean) itemmap.get("isdefault"));
            }
            
            if (itemmap.containsKey("parent") && itemmap.get("parent") != null) {
                InventoryLocation group = itemmap.get("parent") == null ? null : (InventoryLocation) get(InventoryLocation.class, (String) itemmap.get("parent"));
                inventoryLocation.setParent(group);
            }
                      
            if (inventoryLocation != null) {
                Location location = null;
                location = (Location) get(Location.class, inventoryLocation.getId());
                if (location == null) {
                    location = new Location();
                    location.setId(inventoryLocation.getId());

                }
                location.setCompany(inventoryLocation.getCompany());
                //            location.setStores(inventoryLocation.getChildren());
                location.setName(inventoryLocation.getName());
                location.setParentId(inventoryLocation.getParentId());
                location.setDefaultLocation(inventoryLocation.isIsdefault());
                if (isEdit) {
                    location.setModifiedOn(new Date());
                    location.setModifiedBy(user);
                } else {
                    location.setCreatedOn(new Date());
                    location.setModifiedOn(new Date());
                    location.setCreatedBy(user);
                    location.setModifiedBy(user);
                }
                saveOrUpdate(location);
            }
            saveOrUpdate(inventoryLocation);
            list.add(inventoryLocation);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }

    @Override
    public KwlReturnObject saveDepartmentItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            Department department = new Department();
            String itemID = (String) itemmap.get("id");
            if (itemmap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                department = (Department) get(Department.class, itemID);
                if (department == null) {
                    department = new Department();
                    department.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                department.setId(newitemID);
            }
            if (itemmap.containsKey("name")) {
                department.setName((String) itemmap.get("name"));
            }
            if (itemmap.containsKey("parentid")) {
                department.setParentId((String) itemmap.get("parentid"));
            }
            if (itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                department.setCompany(company);
            }
            saveOrUpdate(department);
            list.add(department);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }

    public KwlReturnObject addWarehouseItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            InventoryWarehouse inventoryWarehouse = new InventoryWarehouse();
            String itemID = (String) itemmap.get("id");
            if (itemmap.containsKey("id") && !StringUtil.isNullOrEmpty(itemID)) {
                inventoryWarehouse = (InventoryWarehouse) get(InventoryWarehouse.class, itemID);
                if (inventoryWarehouse == null) {
                    inventoryWarehouse = new InventoryWarehouse();
                    inventoryWarehouse.setId(itemID);
                }
            } else {
                String newitemID = UUID.randomUUID().toString();
                inventoryWarehouse.setId(newitemID);
            }
            if (itemmap.containsKey("name")) {
                inventoryWarehouse.setName((String) itemmap.get("name"));
            }
            if (itemmap.containsKey("parentid")) {
                inventoryWarehouse.setParentId((String) itemmap.get("parentid"));
            }
            if (itemmap.containsKey("isdefault")) {
                inventoryWarehouse.setIsdefault((Boolean) itemmap.get("isdefault"));
            }
            if (itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                inventoryWarehouse.setCompany(company);
            }
            if (itemmap.containsKey("parent") && itemmap.get("parent") != null) {
                InventoryWarehouse group = itemmap.get("parent") == null ? null : (InventoryWarehouse) get(InventoryWarehouse.class, (String) itemmap.get("parent"));
                inventoryWarehouse.setParent(group);
            }
            if (itemmap.containsKey("location") && itemmap.get("location") != null) {
                InventoryLocation group = itemmap.get("location") == null ? null : (InventoryLocation) get(InventoryLocation.class, (String) itemmap.get("location"));
                inventoryWarehouse.setLocation(group);
            }
            saveOrUpdate(inventoryWarehouse);
            list.add(inventoryWarehouse);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }

    public KwlReturnObject getFieldParamsUsingSql(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            executeSQLUpdate("SET SESSION group_concat_max_len = 999999999;");//maximum length of the result of GROUP_CONCAT is 1024 characters. so increasing limit upto 2048
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "select  fieldlabel,id, GROUP_CONCAT(id SEPARATOR ',') as moduleIds from fieldparams ";

            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
            }

            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }
            if (requestParams.get("search_values")!= null) {
                hql += "and fieldlabel="+org.springframework.util.StringUtils.quote(requestParams.get("search_values").toString());
            }
            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            if (requestParams.containsKey("customfield") && (Integer) requestParams.get("customfield") != null) {
                hql += " and customfield = 1";
            }
            if (requestParams.containsKey("relatedmoduleid")) {
                hql += " and relatedmoduleid like '%" + requestParams.get("relatedmoduleid") + "%'";
            }
                hql += " group by fieldlabel, fieldtype, customfield ";
//            list = executeQuery(hql, value.toArray());
            list = executeSQLQuery(hql, value.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject updateMasterItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            String itemID = (String) itemmap.get("id");
            MasterItem masterItem = (MasterItem) get(MasterItem.class, itemID);
            if (masterItem != null) {
                if (itemmap.containsKey("name")) {
                    masterItem.setValue(URLEncoder.encode((String) itemmap.get("name"), Constants.DECODE_ENCODE_FORMAT));
                }
                if (itemmap.containsKey("code") && itemmap.get("code") != null) {
                    masterItem.setCode((String) itemmap.get("code"));
                }
                if (itemmap.containsKey("salesPersonContactNumber") && itemmap.get("salesPersonContactNumber") != null) {
                    masterItem.setContactNumber((String) itemmap.get("salesPersonContactNumber"));
                }
                if (itemmap.containsKey("salesPersonAddress") && itemmap.get("salesPersonAddress") != null) {
                    masterItem.setAddress((String) itemmap.get("salesPersonAddress"));
                }
                 if (itemmap.containsKey("salesPersonDesignation") && itemmap.get("salesPersonDesignation") != null) {
                    masterItem.setDesignation((String) itemmap.get("salesPersonDesignation"));
                }
                if (itemmap.containsKey("groupid")) {
                    MasterGroup group = itemmap.get("groupid") == null ? null : (MasterGroup) get(MasterGroup.class, (String) itemmap.get("groupid"));
                    masterItem.setMasterGroup(group);
                }
                if (itemmap.containsKey("parentid")) {
                    MasterItem item1 = (MasterItem) get(MasterItem.class, (String) itemmap.get("parentid"));
                    masterItem.setParent(item1);
                }
                if (itemmap.containsKey("accid")) {
                    masterItem.setAccID((String) itemmap.get("accid"));
                }
                if (itemmap.containsKey("emailid")) {
                    masterItem.setEmailID((String) itemmap.get("emailid"));
                }
                if (itemmap.containsKey("industryCodeId") && itemmap.get("industryCodeId") != null) {
                    masterItem.setIndustryCodeId((String) itemmap.get("industryCodeId"));
                }
                if (itemmap.containsKey("companyid")) {
                    Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                    masterItem.setCompany(company);
                }
                if (itemmap.containsKey("userid")) {
                    User userObj = itemmap.get("userid") == null ? null : (User) get(User.class, (String) itemmap.get("userid"));
                    masterItem.setUser(userObj);
                }
                if (itemmap.containsKey("isIBGActivated") && itemmap.get("isIBGActivated") != null) {
                    masterItem.setIbgActivated((Boolean) itemmap.get("isIBGActivated"));
                }
                if (itemmap.containsKey("custVendCategoryTypeId") && itemmap.get("custVendCategoryTypeId") != null) {
                    masterItem.setCustVendCategoryType((Integer) itemmap.get("custVendCategoryTypeId"));
                }
                if (itemmap.containsKey("driverID") && itemmap.get("driverID") != null) {
                    MasterItem driver = (MasterItem) get(MasterItem.class, (String) itemmap.get("driverID"));
                    masterItem.setDriver(driver);
                }
                 if (itemmap.containsKey("isDefaultToPOS") && itemmap.get("isDefaultToPOS") != null) {
                    masterItem.setDefaultToPOS((Boolean) itemmap.get("isDefaultToPOS"));
                }
                if (itemmap.containsKey("variancePercentage") && itemmap.get("variancePercentage") != null) {
                    masterItem.setVariancePercentage((Double) itemmap.get("variancePercentage"));
                }
                if (itemmap.containsKey("vatcommoditycode") && itemmap.get("vatcommoditycode") != null) {
                    masterItem.setVatcommoditycode((String) itemmap.get("vatcommoditycode"));
                }
                if (itemmap.containsKey("vatscheduleno") && itemmap.get("vatscheduleno") != null) {
                    masterItem.setVatscheduleno((String) itemmap.get("vatscheduleno"));
                }
                if (itemmap.containsKey("vatscheduleserialno") && itemmap.get("vatscheduleserialno") != null) {
                    masterItem.setVatscheduleserialno((String) itemmap.get("vatscheduleserialno"));
                }
                if (itemmap.containsKey("vatnotes") && itemmap.get("vatnotes") != null) {
                    masterItem.setVatnotes((String) itemmap.get("vatnotes"));
                }
                if (itemmap.containsKey("lcallocationid") && itemmap.get("lcallocationid") != null) {
                    int temp = Integer.parseInt(itemmap.get("lcallocationid").toString());
                    masterItem.setLcallocationid(LandingCostAllocationType.getByValue(temp));
                }
                if (itemmap.containsKey("lccategoryid") && itemmap.get("lccategoryid") != null) {
                    int lccategoryid = Integer.parseInt(itemmap.get("lccategoryid").toString());
                    masterItem.setLccategoryid((LandingCostCategory) get(LandingCostCategory.class, lccategoryid));
                }
                if (itemmap.containsKey("BICCode") && itemmap.get("BICCode") != null) {
                    masterItem.setBICCode((String) itemmap.get("BICCode"));
                }
                if (itemmap.containsKey("bankCode") && itemmap.get("bankCode") != null) {
                    masterItem.setBankCode((String) itemmap.get("bankCode"));
                }
                if (itemmap.containsKey("branchCode") && itemmap.get("branchCode") != null) {
                    masterItem.setBranchCode((String) itemmap.get("branchCode"));
                }
                if (itemmap.containsKey("isAppendBranchCode") && !StringUtil.isNullOrEmpty((String)itemmap.get("isAppendBranchCode"))) {
                    masterItem.setIsAppendBranchCode(Boolean.parseBoolean((String)itemmap.get("isAppendBranchCode")));
                }
                saveOrUpdate(masterItem);
                list.add(masterItem);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.updateMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been update successfully.", null, list, list.size());
    }

    public String daleteMasterItem(String itemid) throws ServiceException,AccountingException {
        boolean successflag = false;
        String retVal="";
//        try {
            MasterItem mdata = (MasterItem) get(MasterItem.class, itemid);
            if (mdata != null) {
                Set<MasterItem> children = mdata.getChildren();
                if (children != null && !children.isEmpty()) {
                    throw new AccountingException("Please delete child item before deleting selected master item.");
                }
                delete(mdata);
                retVal=mdata.getValue();
                successflag = true;
            }
//        } catch (Exception ex) {
//            throw ServiceException.FAILURE("accMasterItemsImpl.daleteMasterItem :" + ex.getMessage(), ex);
//        }
        return retVal;
    }
    
    public KwlReturnObject getSalesPersonMappedWithCustomer(String itemid) throws ServiceException {
        try {

            String query = "from SalesPersonMapping where salesperson.ID=?";
            KwlReturnObject result;
            ArrayList params = new ArrayList();
            params.add(itemid);
            List list = executeQuery(query, params.toArray());
            result = new KwlReturnObject(true, null, null, list, list.size());
            return result;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getSalesPersonMappedWithCustomer :" + ex.getMessage(), ex);
        }

    }
    
    public KwlReturnObject getMasterItemsForProductCategory(String itemid, String companyId, String groupId) throws ServiceException {
        KwlReturnObject result;
        ArrayList filter_params = new ArrayList();
        String query = "from MasterItem where masterGroup.ID = ? and company.companyID = ? and industryCodeId=?";
        ArrayList params = new ArrayList();
        params.add(groupId);
        params.add(companyId);
        params.add(itemid);
        List list = executeQuery(query, params.toArray());
        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }
    
     public KwlReturnObject getIndustryCodesMappedWithProduct(String itemid) throws ServiceException {
        try {
            String query = "from Product where industryCodeId=?";
            KwlReturnObject result;
            ArrayList params = new ArrayList();
            params.add(itemid);
            List list = executeQuery(query, params.toArray());
            result = new KwlReturnObject(true, null, null, list, list.size());
            return result;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getIndustryCodesMappedWithProduct :" + ex.getMessage(), ex);
        }

    }
     
     public KwlReturnObject getIndustryCodesMappedWithMultiEntity(String itemId,String companyId) throws ServiceException {
        try {
            String query = "select id from multientitymapping where industrycode=? and company=?";
            KwlReturnObject result;
            ArrayList params = new ArrayList();
            params.add(itemId);
            params.add(companyId);
            List list = executeSQLQuery( query, params.toArray());
            result = new KwlReturnObject(true, null, null, list, list.size());
            return result;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getIndustryCodesMappedWithProduct :" + ex.getMessage(), ex);
        }

    }
      public KwlReturnObject getProductMappedWithProductCategory(String itemId)throws ServiceException,AccountingException {
            String query = "select pcm.productid, p.productid as prodID from productcategorymapping pcm inner join product p on p.id=pcm.productid where productcategory = ? ";
            KwlReturnObject result;
            ArrayList params = new ArrayList();
            params.add(itemId);
            List list = executeSQLQuery( query, params.toArray());
//            if (list != null && !list.isEmpty()) {
//                throw new AccountingException("You cannot delete selected master item as it is already mapped with Product(s).");
//            }
            result = new KwlReturnObject(true, null, null, list, list.size());
            return result;

    }

      public KwlReturnObject getGstRegistrationTypeMappedWithCustomersAndVendors(String itemId)throws ServiceException,AccountingException {
            KwlReturnObject result;
            ArrayList params = new ArrayList();
            params.add(itemId);
            String query = "SELECT id from customer WHERE gstregistrationtype = ? ";
            List list = executeSQLQuery( query, params.toArray());
            if(list.isEmpty()){     
                /*
                 * if not used in customers then only check usage in vendors.
                 */
                query = "SELECT id from vendor WHERE gstregistrationtype = ? ";
                list = executeSQLQuery( query, params.toArray());
            }
            if (list != null && !list.isEmpty()) {
                throw new AccountingException("You cannot delete selected master item as it is already mapped with Customers/Vendors.");
            }
            result = new KwlReturnObject(true, null, null, list, list.size());
            return result;
    }     
      
      public KwlReturnObject getGstCustomerVendorTypeMappedWithCustomersAndVendors(String itemId)throws ServiceException,AccountingException {
            KwlReturnObject result;
            ArrayList params = new ArrayList();
            params.add(itemId);
            String query = "SELECT id from customer WHERE gstcustomertype = ? ";
            List list = executeSQLQuery( query, params.toArray());
            if(list.isEmpty()){       
                /*
                 * if not used in customers then only check usage in vendors.
                 */
                query = "SELECT id from vendor WHERE gstvendortype = ? ";
                list = executeSQLQuery( query, params.toArray());
            }
            if (list != null && !list.isEmpty()) {
                throw new AccountingException("You cannot delete selected master item as it is already mapped with Customers/Vendors.");
            }
            result = new KwlReturnObject(true, null, null, list, list.size());
            return result;
    }
    public KwlReturnObject deleteLocationItem(String itemid) throws ServiceException {
        boolean successflag = false;
        try {
            InventoryLocation mdata = (InventoryLocation) get(InventoryLocation.class, itemid);
            if (mdata != null) {
                delete(mdata);
                successflag = true;
            }
            
            if (get(Location.class, itemid) != null && successflag) {
                Location mdata1 = (Location) get(Location.class, itemid);
                delete(mdata1);
                successflag = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteLocationItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "Location item has been deleted successfully.", null, null, 1);
    }

    public KwlReturnObject deleteWarehouseItem(String itemid) throws ServiceException {
        boolean successflag = false;
        try {
            InventoryWarehouse mdata = (InventoryWarehouse) get(InventoryWarehouse.class, itemid);
            if (mdata != null) {
                delete(mdata);
                successflag = true;
            }
            
            if (get(Store.class, itemid) != null && successflag) {
                Store mdata1 = (Store) get(Store.class, itemid);
                delete(mdata1);
                successflag = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteLocationItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "Warehouse item has been deleted successfully.", null, null, 1);
    }
    
    public KwlReturnObject deleteDepartmentItem(String itemid) throws ServiceException {
        boolean successflag = false;
        try {
            Department deptObj = (Department) get(Department.class, itemid);
            if (deptObj != null) {
                delete(deptObj);
                successflag = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteDepartmentItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "Department item has been deleted successfully.", null, null, 1);
    }

//    public KwlReturnObject getMasterItems(String groupid, String companyid) throws ServiceException {
//        List list = new ArrayList();
//        try {
//            String query="from MasterItem me where masterGroup.ID=? and company.companyID=? order by me.value";
//            list = executeQuery(query, new Object[]{groupid, companyid});
//        } catch (Exception ex) {
//            throw ServiceException.FAILURE("accMasterItemsImpl.getMasterItems :"+ ex.getMessage(), ex);
//        }
//        return new KwlReturnObject(true, "", null, list, list.size());
//    }
    public KwlReturnObject saveActivateDeactivateDimensionFields(HashMap<String, Object> requestparams) {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String  data="";
        boolean activateDeactivateDimFlag = false;
        if (requestparams.containsKey("activateDeactivateDimFlag")) {
            activateDeactivateDimFlag = (boolean) requestparams.get("activateDeactivateDimFlag");
        }
        if (requestparams.containsKey("data")) {
            data = (String) requestparams.get("data");
        }
        int count = 0;
        try {
            com.krawler.utils.json.base.JSONArray jArr = new com.krawler.utils.json.base.JSONArray(data);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jSONObject = jArr.getJSONObject(i);
                if (!StringUtil.isNullObject(jSONObject.get("id"))) {
                    params.clear();
                    FieldComboData fieldComboData = (FieldComboData) get(FieldComboData.class, (String) jSONObject.get("id"));
                    if (activateDeactivateDimFlag) {
                        params.add("T");
                    } else {
                        params.add("F");
                    }
                    params.add(fieldComboData.getField().getFieldlabel());
                    params.add(fieldComboData.getField().getCompanyid());
                    params.add(fieldComboData.getValue());

                    String query = "update fieldcombodata fcd inner join fieldparams fp on fcd.fieldid = fp.id set fcd.activatedeactivatedimensionvalue = ? where fp.fieldlabel = ? and fp.companyid = ? and fcd.value = ?";
                    count = executeSQLUpdate(query, params.toArray());
                }
            }
        } catch (Exception ex) {
        }
        return new KwlReturnObject(true, "Dimension Field activated successfully", null, list, count);
    }
    
    public KwlReturnObject addMasterItemPrice(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            MasterItemPrice masterItemPrice = new MasterItemPrice();
            if (itemmap.containsKey("name")) {
                masterItemPrice.setValue((String) itemmap.get("name"));
            }

            if (itemmap.containsKey("typeid")) {
                PriceType priceType = itemmap.get("typeid") == null ? null : (PriceType) get(PriceType.class, (String) itemmap.get("typeid"));
                masterItemPrice.setType(priceType);
            }
            if (itemmap.containsKey("price")) {
                double price = Double.parseDouble((String) itemmap.get("price"));
                masterItemPrice.setPrice(price);
            }
            save(masterItemPrice);
            list.add(masterItemPrice);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }

    public KwlReturnObject addMasterPriceDependentItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            PriceType priceType = new PriceType();
            if (itemmap.containsKey("name")) {
                priceType.setValue((String) itemmap.get("name"));
            }

            if (itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                priceType.setCompany(company);
            }
            if (itemmap.containsKey("typeid")) {
                int typeId = Integer.parseInt((String) itemmap.get("typeid"));
                priceType.setType(typeId);
            }
            save(priceType);
            list.add(priceType);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }

    public KwlReturnObject updateMasterPriceDependentItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            String itemID = (String) itemmap.get("id");
            PriceType priceType = (PriceType) get(PriceType.class, itemID);
            if (priceType != null) {
                if (itemmap.containsKey("name")) {
                    priceType.setValue((String) itemmap.get("name"));
                }

                if (itemmap.containsKey("companyid")) {
                    Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                    priceType.setCompany(company);
                }
                if (itemmap.containsKey("typeid")) {
                    int typeId = Integer.parseInt((String) itemmap.get("typeid"));
                    priceType.setType(typeId);
                }
                saveOrUpdate(priceType);
                list.add(priceType);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.updateMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been update successfully.", null, list, list.size());
    }

    public KwlReturnObject saveMasterItemPriceFormula(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {

            MasterItemPriceFormula masterItemPriceFormula = new MasterItemPriceFormula();

            if (itemmap.containsKey("lowerlimitvalue")) {
                int typeId = Integer.parseInt((String) itemmap.get("lowerlimitvalue"));
                masterItemPriceFormula.setLowerlimitvalue(typeId);
            }
            if (itemmap.containsKey("upperlimitvalue")) {
                int typeId = Integer.parseInt((String) itemmap.get("upperlimitvalue"));
                masterItemPriceFormula.setUpperlimitvalue(typeId);
            }
            if (itemmap.containsKey("basevalue")) {
                int typeId = Integer.parseInt((String) itemmap.get("basevalue"));
                masterItemPriceFormula.setBasevalue(typeId);
            }
            if (itemmap.containsKey("incvalue")) {
                int typeId = Integer.parseInt((String) itemmap.get("incvalue"));
                masterItemPriceFormula.setIncvalue(typeId);
            }
            if (itemmap.containsKey("typeid")) {
                PriceType priceType = itemmap.get("typeid") == null ? null : (PriceType) get(PriceType.class, (String) itemmap.get("typeid"));
                masterItemPriceFormula.setType(priceType);
            }
            save(masterItemPriceFormula);
            list.add(masterItemPriceFormula);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.saveMasterItemPriceFormula :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item Price Formula has been added successfully.", null, list, list.size());
    }

    public KwlReturnObject getMasterPriceDependentItem(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from PriceType";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getMasterItemPrice(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from MasterItemPrice";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getMasterItemPriceFormula(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from MasterItemPriceFormula";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getSerialItems(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ReturnSerialMapping";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getMasterItems(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from MasterItem";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getMasterItem(String id) throws ServiceException {
        KwlReturnObject result;
        String query = "from MasterItem where id = ?";
        ArrayList params = new ArrayList();
        params.add(id);
        List list = executeQuery(query, params.toArray());
        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }

    
    public KwlReturnObject getSerials(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        String mysqlQuery = "";
        int count = 0;
        boolean duplicatecheck = false;
        if (requestParams.containsKey("duplicatecheck") && requestParams.get("duplicatecheck") != null) { //flag to check serial no is nduplicate or not
            duplicatecheck = (Boolean) requestParams.get("duplicatecheck");
        }

        ArrayList params = new ArrayList();
        if (!StringUtil.isNullOrEmpty((String) requestParams.get("companyid"))) {
            params.add((String) requestParams.get("productid"));
            params.add((String) requestParams.get("companyid"));
            if (!StringUtil.isNullOrEmpty((String) requestParams.get("batch"))) {
                params.add((String) requestParams.get("batch"));
                int transactionid = Integer.parseInt((String) requestParams.get("transactionid"));
                if (transactionid == 2 || transactionid == 4) {  //for Purchase return and DO
                    mysqlQuery = " select distinct(batchserial.id),name,expfromdate,exptodate,batch from batchserial  left join batchserialmapping on batchserial.id=batchserialmapping.purchaseSerial left join returnserialmapping on batchserial.id=returnserialmapping.maptoserial where product=? and company=? and batch=? and purchaseSerial is null and maptoserial is null";
                } else if (transactionid == 3) {  //for sales return
                    mysqlQuery = " select distinct(batchserial.id),name,expfromdate,exptodate,batch from batchserial  where  product=? and company=? and batch=? ";
                }
            } else if (!duplicatecheck) {
                if (!StringUtil.isNullOrEmpty((String) requestParams.get("transactionid"))) {
                    int transactionid = Integer.parseInt((String) requestParams.get("transactionid"));
                    if (transactionid == 2 || transactionid == 4) {  //for Purchase return and DO
                        mysqlQuery = "select distinct(batchserial.id),name,expfromdate,exptodate,batch from batchserial  left join returnserialmapping on batchserial.id=returnserialmapping.maptoserial left join batchserialmapping on batchserial.id=batchserialmapping.purchaseSerial where product=? and company=?  and maptoserial is null and ispurchase='T' and purchaseSerial is null ";
                    } else if (transactionid == 3) {  //for sales return
                        mysqlQuery = " select distinct(batchserial.id),name,expfromdate,exptodate,batch from batchserial  left join batchserialmapping on batchserial.id=batchserialmapping.purchaseSerial left join returnserialmapping on batchserial.id=returnserialmapping.maptoserial  where product=? and company=? and purchaseSerial is null and ispurchase='F' and mapserial is null and transactiontype=3 ";
                    }
                }
            } else {
                mysqlQuery = "select id,name,expfromdate,exptodate,batch from batchserial where product=? and company=? ";
            }
        }

        list = executeSQLQuery( mysqlQuery, params.toArray());
        count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    public KwlReturnObject getSerialPurchaseDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        String mysqlQuery = "";
        int count = 0;
        ArrayList params = new ArrayList();
        params.add((String) requestParams.get("serialid"));
        params.add((String) requestParams.get("productid"));
        mysqlQuery = "select newbatchserial.id,isopening,documentid,serialdocumentmapping.id as mappingid,rate from newbatchserial "
                + " inner join serialdocumentmapping on newbatchserial.id=serialdocumentmapping.serialid "
                + " inner join grodetails on grodetails.id=serialdocumentmapping.documentid "
                + " where newbatchserial.id=? and newbatchserial.product= ? ";


        list = executeSQLQuery( mysqlQuery, params.toArray());
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    public KwlReturnObject getNewSerials(HashMap<String, Object> requestParams, String companyId, String productid, String batchId, boolean requestRejectedCheck) throws ServiceException {
        return getNewSerials(requestParams,  companyId,  productid,  batchId,  requestRejectedCheck,false) ;
    }
    @Override
    public KwlReturnObject getNewSerials(HashMap<String, Object> requestParams, String companyId, String productid, String batchId, boolean requestRejectedCheck,boolean fromVendStock) throws ServiceException {
        ArrayList name = null;
        ArrayList l = new ArrayList();

        /*
         * Calulating stockout serial names.
         */
        List<String> stockOutSerialNames = new ArrayList<String>();
        String stockOutQuery = "select in_sa_detail.serialnames,in_sa_detail.batchname,in_stockadjustment.store,in_sa_detail.location from in_sa_detail "
                + "inner join in_stockadjustment on in_sa_detail.stockadjustment=in_stockadjustment.id "
                + " inner join product on in_stockadjustment.product=product.id "
                + " where in_stockadjustment.company=? and in_stockadjustment.product=? and in_stockadjustment.adjustment_type='Stock Out' and product.isSerialForProduct='T'";
//                + " and product.isBatchForProduct='T'";
        List stockOutList = executeSQLQuery( stockOutQuery, new Object[]{companyId, productid});
        Iterator itr = stockOutList.iterator();
        if (stockOutList.size() > 0) {
            while (itr.hasNext()) {
                Object[] listObj = (Object[]) itr.next();
                String serialNames = (String) listObj[0];
                String batchNames = (String) listObj[1];
                String serialNamesArr[] = serialNames.split(",");
                for (String st : serialNamesArr) {
                    String serialIdQuery = "select newbatchserial.id from newbatchserial inner join newproductbatch on newbatchserial.batch=newproductbatch.id "
                            + " where newbatchserial.serialname=? and newbatchserial.company=? and newbatchserial.product=? and newbatchserial.quantitydue<=0 "
                            + "and newproductbatch.batchname=? and newproductbatch.warehouse=? and newproductbatch.location=?";
                    List serialIdList = executeSQLQuery( serialIdQuery, new Object[]{st, companyId, productid, batchNames, listObj[2], listObj[3]});
                    if (serialIdList.size() > 0) {
                        String stockOutSerialIds = (String) serialIdList.get(0);
                        stockOutSerialNames.add(stockOutSerialIds);
                    }
                }
            }
        }
        String hql = "select nbs from NewBatchSerial nbs ";
        ArrayList value = null;
        ArrayList orderby = null;
        ArrayList ordertype = null;
        String[] searchCol = null;


        if (requestParams.get("filter_names") != null && requestParams.get("filter_params") != null) {
            name = new ArrayList((List<String>) requestParams.get("filter_names"));
            value = new ArrayList((List<Object>) requestParams.get("filter_params"));
            hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
//                int ind = hql.indexOf("(");
//
//                if (ind > -1) {
//                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
//                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
//                    value.remove(index);
//                }
        }
        if (!fromVendStock) {
        hql += " and ((qaApprovalstatus <> 0 and qaApprovalstatus <> 2) OR qaApprovalstatus IS NULL)";// excluding rejected and pending
        } else {
            hql += " and ((qaApprovalstatus <> 0 and qaApprovalstatus <> 2) OR qaApprovalstatus IS NULL) AND nbs.consignquantity= 1 ";
        }
       
        if (requestRejectedCheck) {
            hql += " and nbs not in ( select serialid From SerialDocumentMapping  where serialid.company.companyID = '" + companyId + "'  and serialid.product='" + productid + "' and serialid.batch.id='" + batchId + "'  and  requestApprovalStatus = 2 )"; // rejected request are excluded from list 
        }else{
            hql += " and nbs.serialname not in ( SELECT serialName From ConsignmentApprovalDetails  WHERE batch.product = '" + productid + "'  AND (approvalStatus= 0 OR repairStatus IN(4,6)) )"; // ERP-25065
        }
        
        List list = null;
        if (stockOutList.size() > 0 && stockOutSerialNames != null && stockOutSerialNames.size()>0) {
            hql += " and nbs.id not in ( :stockOutSerialNames)";        //stock out serial names
            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            Map<String, Object> namedParams = new HashMap<String, Object>();
                namedParams.put("stockOutSerialNames", stockOutSerialNames);
                list = executeQuery(hql, value.toArray(), namedParams);
        } else {
            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            list = executeQuery(hql, value.toArray());
        }

        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    /**
     * below method is sql form of getSerialsForDocuments.
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject getSerialsForDocumentsSql(HashMap<String, Object> requestParams) throws ServiceException {
        String documentid = "", batchId = "", stocktype = "", transactiontype = "", condition = "", companyid = "";
        List params = new ArrayList();
        if (requestParams.get("companyid") != null && requestParams.get("companyid") != null) {
            companyid = (String) requestParams.get("companyid").toString();
            condition = " WHERE ns.company=? ";
            params.add(companyid);
        }
        if (requestParams.get("documentid") != null && requestParams.get("documentid") != null) {
            documentid = (String) requestParams.get("documentid").toString();
            condition += " AND nbs.documentid =  ? ";
            params.add(documentid);
        }
        if (requestParams.get("stocktype") != null && requestParams.get("stocktype") != null) {
            stocktype = requestParams.get("stocktype").toString();
            condition += " AND nbs.stocktype =  ? ";
            params.add(stocktype);
        }
        if (requestParams.get("batchId") != null && requestParams.get("batchId") != null) {
            condition += " AND ns.batch =  ? ";
            batchId = requestParams.get("batchId").toString();
            params.add(batchId);
        }
        if (requestParams.get("transactiontype") != null && requestParams.get("transactiontype") != null) {
            transactiontype = (String) requestParams.get("transactiontype").toString();
            condition += " AND transactiontype = ? ";
            params.add(transactiontype);
        }
        condition += " order by  nbs.exptodate asc ";
        String query = " SELECT serialid from serialdocumentmapping nbs INNER JOIN newbatchserial ns ON ns.id=nbs.serialid  " + condition;
        List list = executeSQLQuery(query, params.toArray());

        Iterator itr = list.iterator();

        String seialIds = "";

        if (list != null && !list.isEmpty() && list.size() > 0) {

            while (itr.hasNext()) {
                String seriId = (String) itr.next();
                if (!StringUtil.isNullOrEmpty(seialIds)) {
                    seialIds += ",'" + seriId + "'";
                } else {
                    seialIds = "'" + seriId + "'";
                }
            }
        }
        String hqlquery = "from NewBatchSerial where id in (" + seialIds + ") ";
        List serialList = null;
        if (!StringUtil.isNullOrEmpty(seialIds)) {
            serialList = executeQuery(hqlquery);
        }
        return new KwlReturnObject(true, "Fetch Document Batch  Serial  List.", "", serialList, serialList != null ? serialList.size() : 0);
    }
    public KwlReturnObject getSerialsForDocuments(HashMap<String, Object> requestParams) throws ServiceException {
         ArrayList name = null;
            String hql = "from SerialDocumentMapping nbs";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;            
            
        if (requestParams.get("filter_names") != null && requestParams.get("filter_params") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_params"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
            }
        if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
            orderby = new ArrayList((List<String>) requestParams.get("order_by"));
            ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
            hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
        }
        List  list = executeQuery(hql, value.toArray());
        return new KwlReturnObject(true, "Fetch Document Batch  Serial  List.", "", list, list.size());
    }
    public List getSerialsForConsignmentEdit(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String conditional="";
        if (requestParams.get("billid") != null && requestParams.get("billid") != null) {
            params.add(requestParams.get("billid"));
        }
        if (requestParams.get("batch") != null && requestParams.get("batch") != null && !StringUtil.isNullOrEmpty("" + requestParams.get("batch"))) {
            conditional = " AND  lcm.batchmapid=? ";
            params.add(requestParams.get("batch"));
        }
        if (requestParams.get("product") != null && requestParams.get("product") != null) {
             params.add(requestParams.get("product"));
        }
        String hql = " SELECT srmp.serialid FROM serialdocumentmapping srmp  "
                + " INNER JOIN srdetails srdtl  ON srmp.documentid=srdtl.id "
                + " INNER JOIN locationbatchdocumentmapping lcm ON lcm.documentid=srmp.documentid  AND lcm.transactiontype=29"
                + " WHERE srdtl.salesreturn=? AND srmp.isconsignment='T' AND srdtl.product=? " + conditional;
        List list = executeSQLQuery(hql, params.toArray());
        return list;
    }

    public KwlReturnObject getSerialsinEdit(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        String mysqlQuery = "";
        int count = 0, transactionid = 0;
        String conditionSQL = "";
        ArrayList params = new ArrayList();
        if (!StringUtil.isNullOrEmpty((String) requestParams.get("transactionid"))) {
            transactionid = Integer.parseInt((String) requestParams.get("transactionid"));
        }
        if (!StringUtil.isNullOrEmpty((String) requestParams.get("billid"))) {
            params.add((String) requestParams.get("billid"));
            if (transactionid == 4) { //for DO
//                mysqlQuery = "select id,name,expfromdate,exptodate,batch from batchserial where id in(select purchaseSerial from batchserialmapping where salesSerial in(select id from batchserial where batch in(select batch from dodetails where deliveryorder=?))) ";
                mysqlQuery = "select bs.id,bs.name,bs.expfromdate,bs.exptodate,bs.batch from batchserial bs inner join batchserialmapping bsm on bsm.purchaseSerial=bs.id inner join batchserial bs1 on bsm.salesSerial=bs1.id inner join dodetails dod on dod.batch=bs1.batch where dod.deliveryorder=? ";
            } else if (transactionid == 2) { //purchase return
//                mysqlQuery = " select id,name,expfromdate,exptodate,batch from batchserial where id in(select maptoserial from returnserialmapping where mapserial in(select id from batchserial where batch in(select batch from prdetails where purchasereturn=?))) ";
                mysqlQuery = " select bs.id,bs.name,bs.expfromdate,bs.exptodate,bs.batch from batchserial bs inner join returnserialmapping rsm on bs.id=rsm.maptoserial inner join batchserial bs1 on rsm.mapserial=bs1.id inner join prdetails prd on prd.batch=bs1.batch where prd.purchasereturn=?";
            } else if (transactionid == 3) { //sales return
//                mysqlQuery = " select id,name,expfromdate,exptodate,batch from batchserial where id in(select maptoserial from returnserialmapping where mapserial in(select id from batchserial where batch in(select batch from srdetails where salesreturn=?))) ";
                mysqlQuery = "select  bs.id,bs.name,bs.expfromdate,bs.exptodate,bs.batch from batchserial bs inner join returnserialmapping rsm on bs.id=rsm.maptoserial inner join batchserial bs1 on rsm.mapserial=bs1.id inner join srdetails srd on srd.batch=bs1.batch where srd.salesreturn=? ";
            }
        }
        list = executeSQLQuery( mysqlQuery, params.toArray());
        count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getBatches(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ProductBatch";
        return buildNExecuteQuery(query, requestParams);
    }
    public KwlReturnObject getNewSerialsForConsignmentReturn(String doId,String batchId) throws ServiceException {
        ArrayList params = new ArrayList();
        String condition="";
        params.add(doId);
        if(!StringUtil.isNullOrEmpty(batchId)){  //for getting the serial of particulat batch
            condition= " AND serialid.batch.id=? ";
            params.add(batchId);
        }
        String query = "from SerialDocumentMapping WHERE documentid= ? AND transactiontype=27 AND serialid.quantitydue=0 AND (qaApprovalstatus <> 0 OR qaApprovalstatus IS NULL) "+condition;
        
        List list = executeQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    public KwlReturnObject getNewSerialsForDocuments(String doId,String batchId,int transType) throws ServiceException {
        ArrayList params = new ArrayList();
        String condition="";
        doId=StringUtil.isNullOrEmpty(doId)?"":AccountingManager.getFilterInString(doId);
//        params.add(doId);
        if(!StringUtil.isNullOrEmpty(batchId)){  //for getting the serial of particulat batch
            condition= " AND serialid.batch.id=? ";
            params.add(batchId);
        }
        if(transType==Constants.Acc_ConsignmentSalesReturn_ModuleId){
            condition+= " AND serialid.isconsignment=?  AND serialid.isForconsignment=?  ";
//            condition+= " AND serialid.isconsignment=?  AND serialid.isForconsignment=? AND serialid.consignquantity = ? ";
            params.add(true);
            params.add(false);
//            params.add(1.0);
        }
        if(transType==Constants.Acc_Sales_Return_ModuleId){
            condition+= "AND serialid.quantitydue = 0 ";
        }else{
            condition+= "AND serialid.quantitydue = 1 ";
        }
        String query = "from SerialDocumentMapping WHERE documentid in "+doId+condition;

        List list = executeQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
        
    public KwlReturnObject getNewSerialDocumentMapping(String doId, String batchId, int transType) throws ServiceException {
        ArrayList params = new ArrayList();
        String condition = "";
        params.add(doId);
        if (!StringUtil.isNullOrEmpty(batchId)) {  //for getting the serial of particulat batch
            condition = " AND serialid.batch.id=? ";
            params.add(batchId);
        }
        if (transType == Constants.Acc_ConsignmentSalesReturn_ModuleId) {
            condition += " AND serialid.isconsignment=?  AND serialid.isForconsignment=?  ";
//            condition+= " AND serialid.isconsignment=?  AND serialid.isForconsignment=? AND serialid.consignquantity = ? ";
            params.add(true);
            params.add(false);
//            params.add(1.0);
        }

        String query = "from SerialDocumentMapping WHERE documentid= ? AND (qaApprovalstatus <> 0 OR qaApprovalstatus IS NULL) " + condition;

        List list = executeQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
     public String getDocumentsIdForInvoice(HashMap<String, Object> request) {
        List returnList = new ArrayList();
        String documentIds="";
        int totalCount = 0;
        try {
            String customerid="";
            String custWarehouse="";
            String custcondition="";
            String wareconditin="";
            String prodconditin="";
            ArrayList params= new  ArrayList();
            params.add(request.get("company"));
            if (!StringUtil.isNullOrEmpty((String) request.get("customerID"))) {
                customerid = (String) request.get("customerID");
                custcondition+= " and  do.customer=? ";
                 params.add(customerid);
            }
            
            if (!StringUtil.isNullOrEmpty((String) request.get("custwarehouse"))) {
                custWarehouse = (String) request.get("custwarehouse");
                wareconditin+= " and do.custWarehouse=? ";
                params.add(custWarehouse);
            }
            
            if (!StringUtil.isNullOrEmpty((String) request.get("productid"))) {
//                custWarehouse = (String) request.get("productid");
                prodconditin+= " and dod.product=? ";
                params.add((String) request.get("productid"));
            }
       
            String mysqlQuery = "select GROUP_CONCAT(dod.id) from dodetails as dod inner join product pro on pro.id=dod.product inner join deliveryorder do on do.id=dod.deliveryorder where do.isconsignment='T' and do.company=? and do.deleteflag='F'"+custcondition+wareconditin+prodconditin; 
            returnList = executeSQLQuery( mysqlQuery, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (returnList.size() > 0) {
                documentIds  = returnList.get(0).toString();
                
            }
        return documentIds;
    }
    public KwlReturnObject checkDuplicateSerialforProduct(String productid,String batchid,String serialName,String company) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = "from NewBatchSerial WHERE product= ? AND transactiontype=28 AND batch.id=? and serialname=? and company.companyID=? AND quantitydue=1 AND (qaApprovalstatus <> 0 OR qaApprovalstatus IS NULL) ";//serialid.quantitydue=0 AND (qaApprovalstatus <> 0 OR qaApprovalstatus IS NULL)
        params.add(productid); 
        params.add(batchid);
        params.add(serialName);
        params.add(company);
        List list = executeQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject getNewBatchForConsignmentReturn(String doId) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = "from LocationBatchDocumentMapping WHERE documentid= ? AND transactiontype=27 ";
        params.add(doId);
        List list = executeQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getDeliveryOrderDetails(String doId, String productId) throws ServiceException {

        ArrayList params = new ArrayList();
        String query = "from DeliveryOrderDetail WHERE deliveryOrder.ID= ? AND product.ID=?";
        params.add(doId);
        params.add(productId);
        List list = executeQuery(query, params.toArray());
        int count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    public KwlReturnObject CheckRuleForPendingApproval(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        int count = 0;
            String condition="";
            String LocCondition="";
            ArrayList params = new ArrayList();
            params.add((String)requestParams.get("companyid"));
            params.add(ApprovalType.REQUEST);//Featching data for Request type Rules
            
            if(requestParams.containsKey("warehouse") && requestParams.get("warehouse")!=null){
              condition=" and inventoryWarehouse.id=? ";
              params.add((String)requestParams.get("warehouse"));
            }
            
            if(requestParams.containsKey("requestorid") && requestParams.get("requestorid")!=null){
              condition+=" and requester.userID=? ";
              params.add((String)requestParams.get("requestorid"));
            }
            if(requestParams.containsKey("location") && requestParams.get("location")!=null){
              LocCondition+=" and ID in (select consignmentrequest from ConsignmentRequestLocationMapping where inventorylocation.id=? ) ";
              params.add((String)requestParams.get("location"));
            }
           
            String query = "from ConsignmentRequestApprovalRule where company.companyID=?  and approvalType= ? "+condition+LocCondition;
            list = executeQuery(query, params.toArray());
            count = list.size();
         return new KwlReturnObject(true, "", null, list, count);
    }
    public KwlReturnObject getNewBatches(HashMap<String, Object> requestParams,boolean isOnlyBatchflag ,boolean isOnlyStockflag) throws ServiceException {
        ArrayList name = null;
            String hql = "from NewProductBatch ";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String conditionSql="";
            String[] searchCol = null;
        if (requestParams.get("filter_names") != null && requestParams.get("filter_params") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_params"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
//                int ind = hql.indexOf("(");
//
//                if (ind > -1) {
//                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
//                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
//                    value.remove(index);
//                }
            }
        String query="";
        if(isOnlyStockflag){
           query= hql+ " and batchname!='' and (quantitydue-consignquantity) > 0";
        }else if (isOnlyBatchflag){
          query =hql+ " and batchname!=''";  
        }else{
            query =hql;
        }
        List  list = executeQuery(query, value.toArray());
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    public KwlReturnObject getLocationBatchDocumentMp(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = "from LocationBatchDocumentMapping WHERE documentid= ? AND batchmapid.id=? ";
        params.add(requestParams.get("documentid"));
        params.add(requestParams.get("batchmapid"));
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    public KwlReturnObject getNewBatchesForVendor(HashMap<String, Object> requestParams,boolean isOnlyBatchflag ,boolean isOnlyStockflag) throws ServiceException {
        ArrayList name = null;
            String hql = "from NewProductBatch ";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
                    ;
            String conditionSql="";
            String[] searchCol = null;
        if (requestParams.get("filter_names") != null && requestParams.get("filter_params") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_params"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
//                int ind = hql.indexOf("(");
//
//                if (ind > -1) {
//                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
//                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
//                    value.remove(index);
//                }
            }
        String query="";
        if(isOnlyStockflag){
           query= hql+ " and batchname!='' and consignquantity > 0";
        }else if (isOnlyBatchflag){
          query =hql+ " and batchname!=''";  
        }else{
            query =hql;
        }
        List  list = executeQuery(query, value.toArray());
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
    public KwlReturnObject getBatchesForDocuments(HashMap<String, Object> requestParams) throws ServiceException {
         ArrayList name = null;
            String hql = "from LocationBatchDocumentMapping locBatchDocMap";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;            
            
        if (requestParams.get("filter_names") != null && requestParams.get("filter_params") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_params"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
            }
        List  list = executeQuery(hql, value.toArray());
        return new KwlReturnObject(true, "Fetch Document Location Batch  Map  List.", "", list, list.size());
    }
    public double getBatcheQuantityForreturn(String documentId, String batchMapId) throws ServiceException {
        double quantity = 0;
        String mysqlQuery = "SELECT if(p.isSerialForProduct='T',0,lcm.quantity) AS quantity FROM locationbatchdocumentmapping lcm "
                + " INNER JOIN srdetails srdl ON lcm.documentid=srdl.id "
                + " INNER JOIN product p ON p.id=srdl.product "
                + " WHERE srdl.dodetails=? AND lcm.batchmapid=? AND transactiontype=29 ";
        if (!StringUtil.isNullOrEmpty(documentId) && !StringUtil.isNullOrEmpty(batchMapId)) {
            ArrayList params = new ArrayList();
            params.add(documentId);
            params.add(batchMapId);
            List list =executeSQLQuery(mysqlQuery, params.toArray());
            if (list.size() > 0) {
                if (!StringUtil.isNullObject(list.get(0))) {
                    quantity = Double.parseDouble(list.get(0).toString());
                }
            }
        }

        return quantity;
    }
    public KwlReturnObject getPRBatchQuantity(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList name = null;
            String hql = "from LocationBatchDocumentMapping ";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null
                    ;
            String[] searchCol = null;
        if (requestParams.get("filter_names") != null && requestParams.get("filter_params") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_params"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
//                int ind = hql.indexOf("(");
//
//                if (ind > -1) {
//                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
//                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
//                    value.remove(index);
//                }
            }
        
        String query =hql+ " and batchname!=''";
        List  list = executeQuery(hql, value.toArray());
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    }
/**
 * @Desc : Get Batch Remaining qty from DO-IST
 * @param requestParams
 * @return
 * @throws ServiceException 
 */
    public KwlReturnObject getBatchRemainingQtyFromIST(Map<String, Object> requestParams) throws ServiceException {
        List params = new ArrayList();
        if(requestParams.containsKey("dodid")){
            params.add((String)requestParams.get("dodid"));
        }
        if(requestParams.containsKey("locationid")){
            params.add((String)requestParams.get("locationid"));
        }
        if(requestParams.containsKey("warehouseid")){
            params.add((String)requestParams.get("warehouseid"));
        }
        String query = " select istd.issuedqty from in_ist_detail istd inner join in_interstoretransfer ins on ins.id=istd.istrequest"
                + " inner join dodistmapping dom on dom.ist=ins.id where dom.dod=? and istd.issued_location=? and ins.fromstore=?";
        List list = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "get Batch Remaining Qty From IST.", "", list, list.size());
    }
    public KwlReturnObject getLocationItems(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from InventoryLocation";
        return buildNExecuteQuery(query, requestParams);
    }
    
    @Override
    public KwlReturnObject getDepartments(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from Department";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getWarehouseItems(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "Select s from InventoryWarehouse s ";
        ArrayList filter_names = new ArrayList();
        if (requestParams.containsKey("filter_names")) {
            filter_names = (ArrayList) requestParams.get("filter_names");
            if (filter_names.contains("m.id")) {
                query += " join s.movementTypeSet AS m ";
            }
        }
        return buildNExecuteQuery(query, requestParams);
    }
    
    @Override
    public KwlReturnObject getLocationsFromStore(String storeid,String companyid) {
        List list = null;
        try {
            String hql ="";
            if(!StringUtil.isNullOrEmpty(storeid)){
                hql = "select DISTINCT locationid from in_store_location inner join in_location on "
                        + "in_store_location.locationid=in_location.id where in_location.company='" + companyid  + "' and storeid in('" + storeid  + "') ";
            }else{
                hql = "select DISTINCT locationid from in_store_location inner join in_location on "
                        + "in_store_location.locationid=in_location.id and in_location.company='" + companyid  + "'";
            }

            list = executeSQLQuery( hql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getMasterItemsHire(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        int totalCount=0;
        ArrayList filter_params = new ArrayList();
        filter_params = (ArrayList) requestParams.get("filter_params");
        StringBuilder queryBuild=new StringBuilder();
        queryBuild.append("from MasterItem where masterGroup.ID = ? and company.companyID = ? and parent is null ");
        
        String conditionalQuery = "";
        if (requestParams.containsKey("ss") && !StringUtil.isNullOrEmpty(requestParams.get("ss").toString())) {
            String[] ssfieldnames = (String[]) requestParams.get("ss_names");
            String ss = (String) requestParams.get("ss");
            conditionalQuery = StringUtil.getSearchString(ss," and ", ssfieldnames);
            try {
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(filter_params, ss, 1);
                StringUtil.insertParamSearchString(SearchStringMap);
            } catch (SQLException ex) {
                Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
            queryBuild.append(conditionalQuery);
        }
        
       String query = queryBuild.toString()+" order by value asc";
        List<MasterItem> list = executeQuery(query, filter_params.toArray());
        totalCount=list.size();
        if (requestParams.containsKey("start") && requestParams.get("start") != null && requestParams.containsKey("limit") && requestParams.get("limit") != null) {
            int start = Integer.parseInt(requestParams.get("start").toString());
            int limit = Integer.parseInt(requestParams.get("limit").toString());
            list = executeQueryPaging(query, filter_params.toArray(), new Integer[]{start, limit});
        }
        
        List resultlist = new ArrayList();
        int level = 0;
        for (MasterItem listObj : list) {
            Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
            tmplist[0] = listObj;
            tmplist[1] = level;
            resultlist.add(tmplist);
            tmplist[2] = getChildGroups(listObj, resultlist, level); //For each master Group Leaf is true
            tmplist[3] = null;//parent group

        }
        result = new KwlReturnObject(true, null, null, resultlist, totalCount);
        return result;
    }

    public boolean getChildGroups(MasterItem itemData, List resultlist, int level) {
        boolean leaf = true;
        Set<MasterItem> childrenSet = itemData.getChildren();

        TreeSet<MasterItem> sortedChildrenSet = new TreeSet<MasterItem>(childrenSet);

        Iterator<MasterItem> itr = sortedChildrenSet.iterator();
        level++;
        while (itr.hasNext()) {
            Object listObj = itr.next();
            MasterItem child = (MasterItem) listObj;
            leaf = false;
            Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
            tmplist[0] = listObj;
            tmplist[1] = level;
            resultlist.add(tmplist);
            tmplist[2] = getChildGroups(child, resultlist, level);
            tmplist[3] = itemData;//parent group
        }
        return leaf;
    }
    
    public List getUniqueDimensionValues(Map<String, Object> paramMap){
        
        String query = "select distinct fcd.value from FieldComboData fcd, FieldParams fp where fcd.fieldid =fp.id and fp.companyid =? and fp.fieldlabel=?";

        List paramList = new ArrayList();
        paramList.add(paramMap.get(Constants.companyKey));
        paramList.add(paramMap.get("dimension"));
            List response = new ArrayList<String>();
        try {
            response = executeQuery(query,paramList.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(accMasterItemsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
            return response;
    }

    public KwlReturnObject getMasterItemsForCustom(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from FieldComboData";
        return buildNExecuteQuery(query, requestParams);
    }
    
    public List getMasterItemsForRemoteAPI(ArrayList value, boolean callForTasks) throws ServiceException {
        String conditionSQL = "";
        if (callForTasks) {
            conditionSQL = " and taskid=? ";
        }
        String query = "from FieldComboData where projectid=? and field.id=? " + conditionSQL;
        return executeQuery(query, value.toArray());
    }
        
    public KwlReturnObject getMasterItemsParentDimensionValue(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from FieldComboDataMapping";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getMasterItemsForCustomHire(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        ArrayList filter_params = new ArrayList();
        String condition = "";
        int totalCount = 0;
        try {
            filter_params = (ArrayList) requestParams.get("filter_params");
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String searchString = (String) requestParams.get("ss");
                if (!StringUtil.isNullOrEmpty(searchString)) {
                    String[] searchcol = new String[]{"value"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(filter_params, searchString, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    condition += searchQuery;
                }
            }
            
            String orderBy = " order by itemsequence,value asc ";
            String query = "from FieldComboData where field.id =  ?  and parent is null " + condition + orderBy ;
            List list = executeQuery(query, filter_params.toArray());
            totalCount = list.size();
            // Paging Query
            if (requestParams.containsKey("start") && requestParams.get("start") != null && requestParams.containsKey("limit") && requestParams.get("limit") != null) {
                if (!StringUtil.isNullOrEmpty(requestParams.get("start").toString()) && !StringUtil.isNullOrEmpty(requestParams.get("limit").toString())) {
                    int start = Integer.parseInt(requestParams.get("start").toString());
                    int limit = Integer.parseInt(requestParams.get("limit").toString());
                    list = executeQueryPaging(query, filter_params.toArray(), new Integer[]{start, limit});

                }
            }
            Iterator itr = list.iterator();
            List resultlist = new ArrayList();
            int level = 0;
            while (itr.hasNext()) {
                Object listObj = itr.next();
                FieldComboData fieldComboData = (FieldComboData) listObj;
                Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
                tmplist[0] = listObj;
                tmplist[1] = level;
                resultlist.add(tmplist);
                tmplist[2] = getChildGroups(fieldComboData, resultlist, level); //For each master Group Leaf is true
                tmplist[3] = null;//parent group

            }
            result = new KwlReturnObject(true, null, null, resultlist, totalCount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getPricingBandItems :" + ex.getMessage(), ex);
        }

        return result;
    }

    public KwlReturnObject getUserGroupmappingId(HashMap<String, Object> requestParams) throws ServiceException{
        KwlReturnObject result = null;
        List params = new ArrayList();
        List list1 = new ArrayList();
        String parentVal="";
        if(requestParams.containsKey("parentValueid")){
            parentVal=(String)requestParams.get("parentValueid");
        }
        String query ="from UserGroupFieldComboMapping where fieldComboData.id= ? ";
        params.add(parentVal);
        list1 = executeQuery(query,params.toArray());
        result = new KwlReturnObject(true, null, null, list1, list1.size());
        return result; 
    }
    
    public KwlReturnObject getParentMappingToChild(HashMap<String, Object> Params) throws ServiceException{
        KwlReturnObject result = null;
        List params = new ArrayList();
        
        String masterItem="";
        List list = new ArrayList();
//        if(Params.containsKey("masterItem")){
//            masterItem=(String)Params.get("masterItem");
//        }
        
//        String[] value = masterItem.split(",");
        String query="from FieldComboDataMapping where parent.id in(:masterItemList)";
        //params.add(value[0]);
        list=executeCollectionQuery(query,Params);
        result = new KwlReturnObject(true, null, null, list, list.size());
        return result; 
    }
    public boolean getChildGroups(FieldComboData fieldComboData, List resultlist, int level) {
        boolean leaf = true;
        Set<FieldComboData> childrenSet = fieldComboData.getChildren();

        TreeSet<FieldComboData> sortedChildrenSet = new TreeSet<FieldComboData>(childrenSet);

        Iterator<FieldComboData> itr = sortedChildrenSet.iterator();
        level++;
        while (itr.hasNext()) {
            Object listObj = itr.next();
            FieldComboData child = (FieldComboData) listObj;
            leaf = false;

            Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
            tmplist[0] = listObj;
            tmplist[1] = level;
            resultlist.add(tmplist);
            tmplist[2] = getChildGroups(child, resultlist, level);
            tmplist[3] = fieldComboData;//parent group
        }
        return leaf;
    }

    public KwlReturnObject addMasterGroup(HashMap<String, Object> groupmap) throws ServiceException {
        List list = new ArrayList();
        try {
            MasterGroup mastergroup = new MasterGroup();
            if (groupmap.containsKey("name")) {
                mastergroup.setGroupName((String) groupmap.get("name"));
            }
            save(mastergroup);
            list.add(mastergroup);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterGroup :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master Group has been added successfully.", null, list, list.size());
    }

    public KwlReturnObject updateMasterGroup(HashMap<String, Object> groupmap) throws ServiceException {
        List list = new ArrayList();
        try {
            String groupID = (String) groupmap.get("id");
            MasterGroup mastergroup = (MasterGroup) get(MasterGroup.class, groupID);
            if (mastergroup != null) {
                if (groupmap.containsKey("name")) {
                    mastergroup.setGroupName((String) groupmap.get("name"));
                }
                saveOrUpdate(mastergroup);
                list.add(mastergroup);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.updateMasterGroup :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master Group has been updated successfully.", null, list, list.size());
    }

    public KwlReturnObject getMasterGroups() throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "from MasterGroup";
            list = executeQuery(query);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getMasterGroups :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject deleteMasterGroup(String groupid) throws ServiceException {
        try {
            // Code for deletion of Master Group --- pending
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteMasterGroup :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master Group has been deleted successfully.", null, null, 1);
    }

//    @Transactional(propagation = Propagation.REQUIRED)
    // Account mapping hashmap is passed to set to master item as per default accountid mapped to default masteritem
    public void copyMasterItems(String companyid, HashMap hmAcc) throws ServiceException {
        try {
            String query = "SELECT dmi,dmi.masterGroup from DefaultMasterItem dmi where dmi.country is null";
            List list = executeQuery(query);
            Iterator iter = list.iterator();
            Company company = (Company) get(Company.class, companyid);
            while (iter.hasNext()) {
                Object[] obj = (Object[]) iter.next();

                DefaultMasterItem defaultMasterItem = (DefaultMasterItem) obj[0];
                MasterGroup masterGroup = (MasterGroup) obj[1];
                MasterItem masterItem = new MasterItem();
                /**
                 * Check If MasterItem is present or not if Present then update same MasterItem Ticket - ERP-35391
                 */
                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                filterRequestParams.put(Constants.filter_names, new ArrayList<String>(Arrays.asList("masterGroup.groupName", "company.companyID", "value")));
                filterRequestParams.put(Constants.filter_params, new ArrayList<String>(Arrays.asList(masterGroup.getGroupName(), companyid, defaultMasterItem.getValue())));
                KwlReturnObject resultList= getMasterItems(filterRequestParams);
                if (resultList != null && resultList.getEntityList() != null && !resultList.getEntityList().isEmpty()) {
                    masterItem = (MasterItem) resultList.getEntityList().get(0);
                }
                masterItem.setCompany(company);
                masterItem.setMasterGroup(masterGroup);
                masterItem.setValue(defaultMasterItem.getValue());
                masterItem.setCode(defaultMasterItem.getCode());
                masterItem.setDefaultMasterItem(defaultMasterItem);
                if(masterGroup.getID().equals(Constants.NatureofPaymentGroup)){
                    if(hmAcc.containsKey(defaultMasterItem.getDefaultAccID())){
                        masterItem.setAccID(hmAcc.get(defaultMasterItem.getDefaultAccID()).toString());
                    }
                }
                save(masterItem);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyMasterItems : " + ex.getMessage(), ex);
        }
    }
    /**
     * Link Master Item with Account, Function is company and DefaultMasterItem table's master group specific
     * @param requestParams
     * @throws ServiceException 
     */
    @Override
    public void mapMasterItemWithAccount(String companyid, HashMap hmAcc, String masterGroup) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            params.add(masterGroup);
            params.add(companyid);
            String query = "from MasterItem where defaultMasterItem.masterGroup.ID =? AND company.companyID=?";
            List list = executeQuery(query, params.toArray());
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                MasterItem masterItem = (MasterItem) iter.next();
                DefaultMasterItem defaultMasterItem = masterItem.getDefaultMasterItem();
                if (hmAcc.containsKey(defaultMasterItem.getDefaultAccID())) {
                    masterItem.setAccID(hmAcc.get(defaultMasterItem.getDefaultAccID()).toString());
                }
                save(masterItem);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("LinkMasterItemWithDefaultAccount : " + ex.getMessage(), ex);
        }
    }
    
    /**
     * Added line level Terms type in master for country specific
     * @param requestParams
     * @throws ServiceException 
     */
    @Override
    public void copyMasterItemsCountrySpecifics(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String companyid = requestParams.containsKey("companyid") ? requestParams.get("companyid").toString() : "";
            String country = requestParams.containsKey("country") ? requestParams.get("country").toString() : "";
            //String state = requestParams.containsKey("state") ? requestParams.get("state").toString() : "";
            String query = "SELECT dmi,dmi.masterGroup from DefaultMasterItem dmi where dmi.country.ID = ? ";
            ArrayList params = new ArrayList();
            params.add(country);
            List list = executeQuery(query, params.toArray());
            Iterator iter = list.iterator();
            Company company = (Company) get(Company.class, companyid);
            while (iter.hasNext()) {
                Object[] obj = (Object[]) iter.next();
                DefaultMasterItem defaultMasterItem = (DefaultMasterItem) obj[0];
                MasterGroup masterGroup = (MasterGroup) obj[1];
                MasterItem masterItem = new MasterItem();
                
                /**
                 * Check If MasterItem is present or not if Present then update same MasterItem Ticket - ERP-35391
                 */
                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                filterRequestParams.put(Constants.filter_names, new ArrayList<String>(Arrays.asList("masterGroup.groupName", "company.companyID", "value")));
                filterRequestParams.put(Constants.filter_params, new ArrayList<String>(Arrays.asList(masterGroup.getGroupName(), companyid, defaultMasterItem.getValue())));
                KwlReturnObject resultList= getMasterItems(filterRequestParams);
                if (resultList != null && resultList.getEntityList() != null && !resultList.getEntityList().isEmpty()) {
                    masterItem = (MasterItem) resultList.getEntityList().get(0);
                }
                masterItem.setCompany(company);
                masterItem.setMasterGroup(masterGroup);
                masterItem.setValue(defaultMasterItem.getValue());
                masterItem.setCode(defaultMasterItem.getCode());
                masterItem.setDefaultMasterItem(defaultMasterItem);
                save(masterItem);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyMasterItems : " + ex.getMessage(), ex);
        }
    }
    
    @Override
    public void copyDefaultTDSRates(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String companyid = requestParams.containsKey("companyid") ? requestParams.get("companyid").toString() : "";
            JSONObject jobj = new JSONObject();
            ArrayList paramsDeductee = new ArrayList();
            String masterItem = "from MasterItem where masterGroup.ID = ? AND company.companyID=?";
            paramsDeductee.add(IndiaComplianceConstants.DEDUCTEE_MASTERGROUP);
            paramsDeductee.add(companyid);
            List listDeducteeType = executeQuery(masterItem, paramsDeductee.toArray());
            Iterator iterDeductee = listDeducteeType.iterator();
            while (iterDeductee.hasNext()) {
                MasterItem masterIterm = (MasterItem) iterDeductee.next();
                jobj.put(masterIterm.getDefaultMasterItem().getID(), masterIterm.getID());
            }
            String query = "from DefaultTDSRate";
            List list = executeQuery(query);
            Iterator iter = list.iterator();
            Company company = (Company) get(Company.class, companyid);
            while (iter.hasNext()) {
                DefaultTDSRate defaultTdsRate = (DefaultTDSRate) iter.next();
                TDSRate tdsRate = new TDSRate();
                if (jobj.length() <= 0 || !jobj.has(defaultTdsRate.getDefaultMasterDeducteetype())) {
                    continue;
                }
                tdsRate.setDeducteetype(jobj.getString(defaultTdsRate.getDefaultMasterDeducteetype()));
                tdsRate.setNatureofpayment(defaultTdsRate.getNatureOfPayment());
                tdsRate.setResidentialstatus(defaultTdsRate.getResidentialStatus());
                tdsRate.setRate(Double.toString(defaultTdsRate.getRate()));
                tdsRate.setFromdate(defaultTdsRate.getFromDate());
                tdsRate.setTodate(defaultTdsRate.getToDate());
                tdsRate.setBasicexemptionperannum(defaultTdsRate.getBasicexEmptionPerAnnum());
                tdsRate.setBasicexemptionpertransaction(defaultTdsRate.getBasicExemptionPerTransaction());
                tdsRate.setFromamount(0);
                tdsRate.setToamount(0);
                tdsRate.setTdsrateifpannotavailable(0);
                tdsRate.setDeleted(false);
                tdsRate.setCompany(company);
                save(tdsRate);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyDefaultTDSRates : " + ex.getMessage(), ex);
        }
    }
    
    public KwlReturnObject getFieldParams(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from FieldParams ";
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = hql.indexOf("(");

                if (ind > -1) {
                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    value.remove(index);
                }
            }

            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }

            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            list = executeQuery(hql, value.toArray());


        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject addUpdateMasterCustomItem(HashMap<String, Object> itemmap, boolean isEdit) throws ServiceException {
        List list = new ArrayList();
        try {
            FieldComboData fieldComboData = null;
            String itemID = "";

            if (!StringUtil.isNullOrEmpty((String) itemmap.get("id"))) {
                itemID = (String) itemmap.get("id");
                fieldComboData = (FieldComboData) get(FieldComboData.class, (String) itemmap.get("id"));
                if (fieldComboData == null) {
                    fieldComboData = new FieldComboData();
                    fieldComboData.setId(itemID);
                }
            } else {
                fieldComboData = new FieldComboData();
                itemID = UUID.randomUUID().toString().replace("-", "");
                fieldComboData.setActivatedeactivatedimensionvalue(true); // ERP-31946
                fieldComboData.setId(itemID);
            }
            if (itemmap.containsKey("name")) {
                fieldComboData.setValue((String) itemmap.get("name"));
            }
            if (itemmap.containsKey("itemdescription")) {
                fieldComboData.setItemdescription((String) itemmap.get("itemdescription"));
            }
            if (itemmap.containsKey(Constants.SEQFORMAT)) {
                fieldComboData.setSeqformat((SequenceFormat) get(SequenceFormat.class, (String) itemmap.get(Constants.SEQFORMAT)));
            }
            if (itemmap.containsKey(Constants.SEQNUMBER)) {
                fieldComboData.setSeqnumber(Integer.parseInt(itemmap.get(Constants.SEQNUMBER).toString()));
            }
            if (itemmap.containsKey("autogenerated")) {
                fieldComboData.setAutoGenerated((Boolean) itemmap.get("autogenerated"));
            }
            if (itemmap.containsKey("parentid")) {
                FieldComboData fieldComboData1 = (FieldComboData) get(FieldComboData.class, (String) itemmap.get("parentid"));
                fieldComboData.setParent(fieldComboData1);
            }
            if (itemmap.containsKey("groupid")) {
                FieldParams fieldParams = (FieldParams) get(FieldParams.class, (String) itemmap.get("groupid"));
                fieldComboData.setFieldid((String) itemmap.get("groupid"));
                fieldComboData.setField(fieldParams);
            }
            if (itemmap.containsKey("projectid")) {
                fieldComboData.setProjectid((String) itemmap.get("projectid"));
            }
            if (itemmap.containsKey("eclaimid")) {
                fieldComboData.setEclaimid((String) itemmap.get("eclaimid"));
            }
            if (itemmap.containsKey("mapwithfieldid") && itemmap.get("mapwithfieldid") != null) {
                fieldComboData.setId((String) itemmap.get("mapwithfieldid"));
            }
            if (itemmap.containsKey("taskid")) {
                fieldComboData.setTaskid((String) itemmap.get("taskid"));
            }
            if (itemmap.containsKey("activatedeactivateflg")) {
                fieldComboData.setActivatedeactivatedimensionvalue((Boolean)(itemmap.get("activatedeactivateflg")));
            }
              if (itemmap.containsKey("parentCompanysFieldCombodataId")) {
                FieldComboData fieldComboData1 = (FieldComboData) get(FieldComboData.class, (String) itemmap.get("parentCompanysFieldCombodataId"));
                fieldComboData.setPropagatedfieldcomboID(fieldComboData1);
            }
            if (isEdit) {
                saveOrUpdate(fieldComboData);
            } else {
                saveOrUpdate(fieldComboData);
            }
            list.add(fieldComboData);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }

    public boolean SaveProjectTaskMapping(HashMap<String, String> itemmap) throws ServiceException {
        KwlReturnObject result = null;
        try {
            List ProjectFieldId = find("from FieldComboData where projectid='" + itemmap.get("projectid") + "' and fieldid='" + itemmap.get("fieldid") + "'");
            FieldComboData ProjectFieldComboId = (FieldComboData) ProjectFieldId.get(0);
            HashMap<String, Object> ProjectTaskMapping = new HashMap<String, Object>();
            ProjectTaskMapping.put("parentValueid", ProjectFieldComboId.getId());
            ProjectTaskMapping.put("chieldValueId", itemmap.get("taskFeildComboId"));
            result = addUpdateMasterCustomItemMapping(ProjectTaskMapping);
            return false;
        } catch (Exception ex) {
            return true;
        }
    }

    public KwlReturnObject addUpdateMasterCustomItemMapping(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            FieldComboDataMapping fieldComboDataMapping = new FieldComboDataMapping();
            String itemID = UUID.randomUUID().toString();
            fieldComboDataMapping.setId(itemID);

            if (itemmap.containsKey("parentValueid")) {
                FieldComboData fieldComboData1 = (FieldComboData) get(FieldComboData.class, (String) itemmap.get("parentValueid"));
                fieldComboDataMapping.setParent(fieldComboData1);
            }
            if (itemmap.containsKey("chieldValueId")) {
                FieldComboData fieldComboData1 = (FieldComboData) get(FieldComboData.class, (String) itemmap.get("chieldValueId"));
                fieldComboDataMapping.setChild(fieldComboData1);
            }

            save(fieldComboDataMapping);
            list.add(fieldComboDataMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }

    public KwlReturnObject deleteMasterCustomItemMapping(String childid) throws ServiceException {
        boolean successflag = false;
        String delQuery = "delete from FieldComboDataMapping f where f.child.id=?";
        int numRows = executeUpdate(delQuery, new Object[]{childid});
        successflag = true;
        return new KwlReturnObject(successflag, "Master item has been deleted successfully.", null, null, 1);
    }

    public KwlReturnObject daleteMasterCustomItem(String itemid) throws ServiceException {
        boolean successflag = false;
        String masteritemName = "";
        try {
            FieldComboData mdata = (FieldComboData) get(FieldComboData.class, itemid);
            if (mdata != null) {
                masteritemName = mdata.getValue();
                delete(mdata);
                successflag = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.daleteMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "'"+masteritemName+"' Master item has been deleted successfully from eClaim side.", null, null, 1);
    }

    public KwlReturnObject deleteMasterItemPrice(String itemid) throws ServiceException {
        boolean successflag = false;
        try {
            MasterItemPrice mdata = (MasterItemPrice) get(MasterItemPrice.class, itemid);
            if (mdata != null) {
                delete(mdata);
                successflag = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.daleteMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "Master item has been deleted successfully.", null, null, 1);
    }

    public KwlReturnObject deleteMasterItemPriceFormula(String itemid) throws ServiceException {
        boolean successflag = false;
        try {
            MasterItemPriceFormula mdata = (MasterItemPriceFormula) get(MasterItemPriceFormula.class, itemid);
            if (mdata != null) {
                delete(mdata);
                successflag = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteMasterItemPriceFormula :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "Master item Price Formula has been deleted successfully.", null, null, 1);
    }

    public KwlReturnObject deleteDimension(String itemid) throws ServiceException {
        boolean successflag = false;
        try {
            FieldParams mdata = (FieldParams) get(FieldParams.class, itemid);
            if (mdata != null) {
                delete(mdata);
                successflag = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.daleteMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "Dimension has been deleted successfully.", null, null, 1);
    }

    public KwlReturnObject deleteAddressFieldAgainstDimension(String groupid) throws ServiceException{    
        boolean successflag=false;
        AddressFieldDimensionMapping addressFieldDimensionMapping=(AddressFieldDimensionMapping)get(AddressFieldDimensionMapping.class,groupid);
        if(addressFieldDimensionMapping!=null){
            delete(addressFieldDimensionMapping);
            successflag=true;
        }
        return new KwlReturnObject(successflag, "Address Mapping has been deleted successfully.", null, null, 1);
    }
    
    public KwlReturnObject deleteNotificationRuleOnDimensionDelete(String itemid) throws ServiceException {
        boolean successflag = false;
        try {
            String hql = "delete from NotificationRules where fieldid = ?";
            executeUpdate(hql, itemid);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteNotificationRuleOnDimensionDelete :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "Notification Rule has been deleted successfully.", null, null, 1);
    }

    public boolean isUsedMasterCustomItem(String itemid, String grpId) throws ServiceException {
        boolean successflag = false;
        try {
            String q="select moduleid,colnum,companyid,gstmappingcolnum from fieldparams where id=?";
            ArrayList params1 = new ArrayList();
            params1.add(grpId);
            List<Object[]> l=executeSQLQuery( q,params1.toArray());
            String module = "";
            String colnum = "";
            String companyId = "";
            String gstmappingcolnum="0";
            if (l.size() > 0) {
                for(Object[] oj : l) {
                    module = oj[0].toString();
                    colnum = oj[1].toString();
                    companyId = oj[2].toString();
                    gstmappingcolnum = oj[3].toString();

                }
            }
            int moduleId=Integer.parseInt(module);
//            FieldParams fieldParams = (FieldParams) get(FieldParams.class, grpId);
            String query = "";
            ArrayList params = new ArrayList();
            List list = Collections.EMPTY_LIST;
            switch (moduleId) {
                case Constants.Acc_FixedAssets_DeliveryOrder_ModuleId://Acc_FixedAssets_DeliveryOrder_ModuleId
                case Constants.Acc_ConsignmentDeliveryOrder_ModuleId:
                case Constants.Acc_Lease_DO:
                case Constants.Acc_Delivery_Order_ModuleId:
                    // dodetailscustomdata
                    query = "select dodetailsid from dodetailscustomdata a where a.moduleId="+moduleId+" and a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }

                    // deliveryordercustomdata
                    query = "select deliveryOrderId from deliveryordercustomdata a where a.moduleId="+moduleId+" and a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Account_Statement_ModuleId:
                    // accountcustomdata
                    query = "select accountId from accountcustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_Contract_Order_ModuleId:
                case Constants.Acc_Lease_Contract:
                    // contractcustomdata
                    query = "select contractid from contractcustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_FixedAssets_AssetsGroups_ModuleId://Wtf.Acc_FixedAssets_AssetsGroups_ModuleId = 42;
                case Constants.Acc_Product_Master_ModuleId:
                    // accproductcustomdata
                    query = "select productId from accproductcustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum+ " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_Vendor_ModuleId:
                    // vendorcustomdata
                    query = "select vendorId from vendorcustomdata a where a.col" + colnum+ " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_Customer_ModuleId:
                    // customercustomdata
                    query = "select customerId from customercustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_FixedAssets_GoodsReceipt_ModuleId: //Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId    
                case Constants.Acc_Goods_Receipt_ModuleId:
                case Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId:
                    // grordercustomdata
                    query = "select goodsreceiptorderid from grordercustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }

                    // grodetailscustomdata
                    query = "select grodetailsid from grodetailscustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_Sales_Return_ModuleId:
                case Constants.Acc_FixedAssets_Sales_Return_ModuleId:
                case Constants.Acc_ConsignmentSalesReturn_ModuleId:
                case Constants.Acc_Lease_Return:
                    // srdetailscustomdata
                    query = "select srdetailsid from srdetailscustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }

                    // salesreturncustomdata
                    query = "select salesreturnid from salesreturncustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_Lease_Order_ModuleId:    //leaseorder
                case Constants.Acc_Sales_Order_ModuleId:
                case Constants.Acc_ConsignmentRequest_ModuleId:
                    // salesorderdetailcustomdata
                    query = "select soDetailID from salesorderdetailcustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }

                    // salesordercustomdata
                    query = "select soID from salesordercustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_Customer_Quotation_ModuleId:
                case Constants.Acc_Lease_Quotation:
                    // quotationdetailscustomdata
                    query = "select quotationdetailsid from quotationdetailscustomdata a where a.col" + colnum+ " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }

                    // quotationcustomdata
                    query = "select quotationid from quotationcustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_Purchase_Return_ModuleId:
                case Constants.Acc_FixedAssets_Purchase_Return_ModuleId:
                case Constants.Acc_ConsignmentPurchaseReturn_ModuleId:
                    // purchasereturncustomdata
                    query = "select purchasereturnid from purchasereturncustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }

                    // prdetailscustomdata
                    query = "select prdetailsid from prdetailscustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_Purchase_Requisition_ModuleId:
                case Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId:
                    // purchaserequisitioncustomdata
                    query = "select purchaserequisitionid from purchaserequisitioncustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    query = "select purchaserequisitiondetailid from purchaserequisitiondetailcustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_Purchase_Order_ModuleId:
                case Constants.Acc_ConsignmentVendorRequest_ModuleId:
                case Constants.Acc_FixedAssets_Purchase_Order_ModuleId:
                    // purchaseordercustomdata
                    query = "select poID from purchaseordercustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    // purchaseorderdetailcustomdata
                    query = "select poDetailID from purchaseorderdetailcustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_Vendor_Quotation_ModuleId:
                case Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId:
                    // vendorquotationdetailscustomdata
                    query = "select vendorquotationdetailsid from vendorquotationdetailscustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }

                    // vendorquotationcustomdata
                    query = "select vendorquotationid from vendorquotationcustomdata a where a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
       
                case Constants.GSTModule:
                    if (!gstmappingcolnum.equalsIgnoreCase("0")) {
                        query = "select id from entitybasedlineleveltermsrate a where a.shippedloc" + gstmappingcolnum + " like ?";
                        params = new ArrayList();
                        params.add("%" + itemid + "%");
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }
                    break;
                default:
                    query = "select journalentryId from accjecustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    // accjedetailcustomdata
                    query = "select jedetailId from accjedetailcustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                    params = new ArrayList();
                    params.add("%" + itemid + "%");
                    params.add(companyId);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    
            }
            
            if(moduleId==Constants.Acc_Invoice_ModuleId){//Opening Sales Invoice
                query = "select openingbalanceinvoiceid from openingbalanceinvoicecustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                params = new ArrayList();
                params.add("%" + itemid + "%");
                params.add(companyId);
                list = executeSQLQuery( query, params.toArray());
                if (list.size() > 0) {
                    return true;
                }
            } else if(moduleId==Constants.Acc_Vendor_Invoice_ModuleId){//Opening Purchase Invoice
                query = "select openingbalancevendorinvoiceid from openingbalancevendorinvoicecustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                params = new ArrayList();
                params.add("%" + itemid + "%");
                params.add(companyId);
                list = executeSQLQuery( query, params.toArray());
                if (list.size() > 0) {
                    return true;
                }
            } else if(moduleId==Constants.Acc_Receive_Payment_ModuleId){//Opening Receive Payment
                query = "select openingbalancereceiptid from openingbalancereceiptcustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                params = new ArrayList();
                params.add("%" + itemid + "%");
                params.add(companyId);
                list = executeSQLQuery( query, params.toArray());
                if (list.size() > 0) {
                    return true;
                }
            } else if(moduleId==Constants.Acc_Make_Payment_ModuleId){//Opening Make Payment
                query = "select openingbalancemakepaymentid from openingbalancemakepaymentcustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                params = new ArrayList();
                params.add("%" + itemid + "%");
                params.add(companyId);
                list = executeSQLQuery( query, params.toArray());
                if (list.size() > 0) {
                    return true;
                }
            } else if(moduleId==Constants.Acc_Credit_Note_ModuleId){//Opening Credit Note
                query = "select openingbalancecreditnoteid from openingbalancecreditnotecustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                params = new ArrayList();
                params.add("%" + itemid + "%");
                params.add(companyId);
                list = executeSQLQuery( query, params.toArray());
                if (list.size() > 0) {
                    return true;
                }
            } else if(moduleId==Constants.Acc_Debit_Note_ModuleId){//Opening Debit Note
                query = "select openingbalancedebitnoteid from openingbalancedebitnotecustomdata a where a.moduleId="+moduleId+" and  a.col" + colnum + " like ? and a.company=?";
                params = new ArrayList();
                params.add("%" + itemid + "%");
                params.add(companyId);
                list = executeSQLQuery( query, params.toArray());
                if (list.size() > 0) {
                    return true;
                }
            }

            // accjedetailcustomdata
//            query = "select * from accjedetailcustomdata a where a.moduleid="+fieldParams.getModuleid()+" and  a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // accountcustomdata
//            query = "select * from accountcustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // accproductcustomdata
//            query = "select * from accproductcustomdata a where a.moduleid="+fieldParams.getModuleid()+" and  a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // deliveryordercustomdata
//            query = "select * from deliveryordercustomdata a where a.moduleid="+fieldParams.getModuleid()+" and  a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // grordercustomdata
//            query = "select * from grordercustomdata a where a.moduleid="+fieldParams.getModuleid()+" and  a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // grodetailscustomdata
//            query = "select * from grodetailscustomdata a where a.moduleid="+fieldParams.getModuleid()+" and a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//           
//            // purchase_requisition_detailscustomdata
//            query = "select * from purchaserequisitioncustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // salesreturncustomdata
//            query = "select * from salesreturncustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // salesorderdetailcustomdata
//            query = "select * from salesorderdetailcustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // salesordercustomdata
//            query = "select * from salesordercustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // quotationdetailscustomdata
//            query = "select * from quotationdetailscustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // quotationcustomdata
//            query = "select * from quotationcustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // prdetailscustomdata
//            query = "select * from prdetailscustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            //purchasereturncustomdata
//            query = "select * from purchasereturncustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//            // purchaseordercustomdata
//            query = "select * from purchaseordercustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // vendorquotationdetailscustomdata
//            query = "select * from vendorquotationdetailscustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // vendorquotationcustomdata
//            query = "select * from vendorquotationcustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            // srdetailscustomdata
//            query = "select * from srdetailscustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            //vendorcustomdata
//            query = "select * from vendorcustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
//
//            //customercustomdata
//            query = "select * from customercustomdata a where a.col" + fieldParams.getColnum() + "=?";
//            params = new ArrayList();
//            params.add(itemid);
//            list = executeSQLQuery( query, params.toArray());
//            if (list.size() > 0) {
//                return true;
//            }
            return false;

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.daleteMasterItem :" + ex.getMessage(), ex);
        }

    }
    /*
     * Method checks if custom data against entity is used in terms.
     */
    @Override
    public boolean isUsedMasterCustomItemForEntity(JSONObject paramObj) throws ServiceException, JSONException{            
        String query="",entityCustomValue="",entityId="";
        int gstMappingColumn=0,colNum=0;
        ArrayList params = new ArrayList();
        List list = Collections.EMPTY_LIST;               
        if(paramObj.has("gstMappingColumn") && paramObj.get("gstMappingColumn")!=null){
            gstMappingColumn=Integer.parseInt(String.valueOf(paramObj.get("gstMappingColumn")));
        }
        if(paramObj.has("colNum") && paramObj.get("colNum")!=null){
            colNum=Integer.parseInt(String.valueOf(paramObj.get("colNum")));
        }
        if(paramObj.has("entityCustomValue") && paramObj.get("entityCustomValue")!=null){
            entityCustomValue=String.valueOf(paramObj.get("entityCustomValue"));
        }
        if(paramObj.has("entityId") && paramObj.get("entityId")!=null){
            entityId=String.valueOf(paramObj.get("entityId"));
        }        
        if (gstMappingColumn != 0) {
            query = "select id from entitybasedlineleveltermsrate a where a.shippedloc" + gstMappingColumn + " like ? and entity = ? limit 1";
            params.add("%" + entityCustomValue + "%");
            params.add(entityId);
            list = executeSQLQuery(query, params.toArray());
            if (list.size() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isUsedMasterCustomField(String grpId) throws ServiceException {
        boolean successflag = false;
        try {
            FieldParams fieldParams = (FieldParams) get(FieldParams.class, grpId);
            String query = "";
            String qattach = " <> '' "; //to check column is not null or empty
            String compid = fieldParams.getCompanyid();
            
            if (fieldParams.getFieldtype() == 11) {// fOr Checkbox type check null and false; 
                qattach += " and a.col" + fieldParams.getColnum() + "='true';";
            }
            
            ArrayList params = new ArrayList();
            List list = Collections.EMPTY_LIST;
            switch (fieldParams.getModuleid()) {
                case Constants.Acc_FixedAssets_DeliveryOrder_ModuleId://Acc_FixedAssets_DeliveryOrder_ModuleId
                case 27:
                    if(fieldParams.getCustomcolumn() == 1){ // for line level items Customcolumn field is 1 and for global leve 0
                        // dodetailscustomdata
                        query = "select * from dodetailscustomdata a where company=? and a.moduleid="+fieldParams.getModuleid()+" and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }else{
                        // deliveryordercustomdata
                        query = "select * from deliveryordercustomdata a where company=? and a.moduleid="+fieldParams.getModuleid()+" and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }
                    break;
                case 34:
                    // accountcustomdata
                    query = "select * from accountcustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case 35:
                    // contractcustomdata
                    query = "select * from contractcustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_FixedAssets_AssetsGroups_ModuleId://Wtf.Acc_FixedAssets_AssetsGroups_ModuleId = 42;
                case 30:
                    // accproductcustomdata
                    query = "select * from accproductcustomdata a where company=?  and a.moduleid="+fieldParams.getModuleid()+" and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case 26:
                    // vendorcustomdata
                    query = "select * from vendorcustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case 25:
                    // customercustomdata
                    query = "select * from customercustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case Constants.Acc_FixedAssets_GoodsReceipt_ModuleId: //Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId
                case 28:
                    if(fieldParams.getCustomcolumn() == 0){
                        // grordercustomdata
                        query = "select * from grordercustomdata a where company=?  and a.moduleid="+fieldParams.getModuleid()+" and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }else{
                        // grodetailscustomdata
                        query = "select * from grodetailscustomdata a where company=? and a.moduleid="+fieldParams.getModuleid()+" and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }
                    break;
                case 29:
                    if(fieldParams.getCustomcolumn() == 1){
                        // srdetailscustomdata
                        query = "select * from srdetailscustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }else{
                        // salesreturncustomdata
                        query = "select * from salesreturncustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }
                    break;
                case Constants.Acc_Lease_Order_ModuleId:    //leaseorder
                case 20:
                    if(fieldParams.getCustomcolumn() == 1){
                        // salesorderdetailcustomdata
                        query = "select * from salesorderdetailcustomdata a where company=?  and a.moduleid="+fieldParams.getModuleid()+" and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }else{
                        // salesordercustomdata
                        query = "select * from salesordercustomdata a where company=? and a.moduleid="+fieldParams.getModuleid()+" and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }
                    break;
                case 22:
                   if(fieldParams.getCustomcolumn() == 1){
                        // quotationdetailscustomdata
                        query = "select * from quotationdetailscustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                   }else{
                        // quotationcustomdata
                        query = "select * from quotationcustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                   }
                   break;
                case 31:
                    if(fieldParams.getCustomcolumn() == 0){
                        // purchasereturncustomdata
                        query = "select * from purchasereturncustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }else{
                        // prdetailscustomdata
                        query = "select * from prdetailscustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }
                    break;
                case 32:
                    // purchaserequisitioncustomdata
                    query = "select * from purchaserequisitioncustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery( query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case 18:
                    if(fieldParams.getCustomcolumn() == 0){
                        // purchaseordercustomdata
                        query = "select * from purchaseordercustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }else{
                        // purchaseorderdetailcustomdata
                        query = "select * from purchaseorderdetailcustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }
                    break;
                case 23:
                    if(fieldParams.getCustomcolumn() == 1){
                        // vendorquotationdetailscustomdata
                        query = "select * from vendorquotationdetailscustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }else{
                        // vendorquotationcustomdata
                        query = "select * from vendorquotationcustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }
                    break;
                case 1101:
                    // labourcustomdata
                    query = "select * from labourcustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case 1102:
                    // workcentrecustomdata
                    query = "select * from workcentrecustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case 1103:
                    // machinecustomdata
                    query = "select * from machinecustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case 1105:
                    // workordercustomdata
                    query = "select * from workordercustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case 1106:
                    // mrpcontractcustomdata
                    query = "select * from mrpcontractcustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                case 1107:
                    // routingtemplatecustomdata
                    query = "select * from routingtemplatecustomdata a where company=? and a.col" + fieldParams.getColnum() + qattach;
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 0) {
                        return true;
                    }
                    break;
                    
                default:
                    if(fieldParams.getCustomcolumn() == 0){
                        query = "select * from accjecustomdata a where a.moduleid="+fieldParams.getModuleid()+" and a.company=? and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }else{
                        // accjedetailcustomdata
                        query = "select * from accjedetailcustomdata a where a.company=? and a.moduleid="+fieldParams.getModuleid()+" and a.col" + fieldParams.getColnum() + qattach;
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery( query, params.toArray());
                        if (list.size() > 0) {
                            return true;
                        }
                    }
            }

            return false;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteMasterItem :" + ex.getMessage(), ex);
        }
    }
    
    @Override
    public boolean isSameDefaultValueForTransaction(String grpId) throws ServiceException {
        boolean successflag = false;
        try {
            FieldParams fieldParams = (FieldParams) get(FieldParams.class, grpId);
            String query = "";
            String compid = fieldParams.getCompanyid();

            ArrayList params = new ArrayList();
            List list = Collections.EMPTY_LIST;
            switch (fieldParams.getModuleid()) {
                case Constants.Acc_FixedAssets_DeliveryOrder_ModuleId://Acc_FixedAssets_DeliveryOrder_ModuleId
                case 27:
                    if (fieldParams.getCustomcolumn() == 1) { // for line level items Customcolumn field is 1 and for global leve 0
                        // dodetailscustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from dodetailscustomdata a where company=? and a.moduleid=" + fieldParams.getModuleid() + " and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    } else {
                        // deliveryordercustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from deliveryordercustomdata a where company=? and a.moduleid=" + fieldParams.getModuleid() + " and a.col"+fieldParams.getColnum()+" is not null  GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    }
                    break;
                case 34:
                    // accountcustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from accountcustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case 35:
                    // contractcustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from contractcustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case Constants.Acc_FixedAssets_AssetsGroups_ModuleId://Wtf.Acc_FixedAssets_AssetsGroups_ModuleId = 42;
                case 30:
                    // accproductcustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from accproductcustomdata a where company=?  and a.moduleid=" + fieldParams.getModuleid() + " and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case 26:
                    // vendorcustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from vendorcustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case 25:
                    // customercustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from customercustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case Constants.Acc_FixedAssets_GoodsReceipt_ModuleId: //Wtf.Acc_FixedAssets_GoodsReceipt_ModuleId
                case 28:
                    if (fieldParams.getCustomcolumn() == 0) {
                        // grordercustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from grordercustomdata a where company=?  and a.moduleid=" + fieldParams.getModuleid() + "  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    } else {
                        // grodetailscustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from grodetailscustomdata a where company=? and a.moduleid=" + fieldParams.getModuleid() + "  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    }
                    break;
                case 29:
                    if (fieldParams.getCustomcolumn() == 1) {
                        // srdetailscustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from srdetailscustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    } else {
                        // salesreturncustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from salesreturncustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    }
                    break;
                case Constants.Acc_Lease_Order_ModuleId:    //leaseorder
                case 20:
                    if (fieldParams.getCustomcolumn() == 1) {
                        // salesorderdetailcustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from salesorderdetailcustomdata a where company=?  and a.moduleid=" + fieldParams.getModuleid() + "  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    } else {
                        // salesordercustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from salesordercustomdata a where company=? and a.moduleid=" + fieldParams.getModuleid() + "  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    }
                    break;
                case 22:
                    if (fieldParams.getCustomcolumn() == 1) {
                        // quotationdetailscustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from quotationdetailscustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    } else {
                        // quotationcustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from quotationcustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    }
                    break;
                case 31:
                    if (fieldParams.getCustomcolumn() == 0) {
                        // purchasereturncustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from purchasereturncustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    } else {
                        // prdetailscustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from prdetailscustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    }
                    break;
                case 32:
                    // purchaserequisitioncustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from purchaserequisitioncustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case 18:
                    if (fieldParams.getCustomcolumn() == 0) {
                        // purchaseordercustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from purchaseordercustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    } else {
                        // purchaseorderdetailcustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from purchaseorderdetailcustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    }
                    break;
                case 23:
                    if (fieldParams.getCustomcolumn() == 1) {
                        // vendorquotationdetailscustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from vendorquotationdetailscustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    } else {
                        // vendorquotationcustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from vendorquotationcustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    }
                    break;
                case 1101:
                    // labourcustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from labourcustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case 1102:
                    // workcentrecustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from workcentrecustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case 1103:
                    // machinecustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from machinecustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case 1105:
                    // workordercustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from workordercustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case 1106:
                    // mrpcontractcustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from mrpcontractcustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;
                case 1107:
                    // routingtemplatecustomdata
                    query = "select a.col" + fieldParams.getColnum() + " from routingtemplatecustomdata a where company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                    params = new ArrayList();
                    params.add(compid);
                    list = executeSQLQuery(query, params.toArray());
                    if (list.size() > 1) {
                        return false;
                    }
                    break;

                default:
                    if (fieldParams.getCustomcolumn() == 0) {
                        query = "select a.col" + fieldParams.getColnum() + " from accjecustomdata a where a.moduleid=" + fieldParams.getModuleid() + " and a.company=?  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    } else {
                        // accjedetailcustomdata
                        query = "select a.col" + fieldParams.getColnum() + " from accjedetailcustomdata a where a.company=? and a.moduleid=" + fieldParams.getModuleid() + "  and a.col"+fieldParams.getColnum()+" is not null GROUP BY a.col" + fieldParams.getColnum();
                        params = new ArrayList();
                        params.add(compid);
                        list = executeSQLQuery(query, params.toArray());
                        if (list.size() > 1) {
                            return false;
                        }
                    }
            }

            return true;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteMasterItem :" + ex.getMessage(), ex);
        }

    }

    @Override
    public KwlReturnObject unlinkcustomFieldFromTransaction(String itemid, JSONArray array) throws ServiceException {
        boolean successflag = false;
        JSONObject obj = new JSONObject();
        try {
            FieldParams mdata = (FieldParams) get(FieldParams.class, itemid);

            int moduleid = mdata.getModuleid();
            int colnum = mdata.getColnum();
            String companyid = mdata.getCompanyid();
            int islineitem = 0;
            if (mdata.getCustomcolumn() == 0) {
                islineitem = 0;
            } else {
                islineitem = 1;
            }

            obj.put("module", moduleid);
            obj.put("lineitem", islineitem);
            
            JSONArray arrayObj = StringUtil.findJsonArray(array, obj);
            JSONObject jObj = arrayObj.getJSONObject(0);
            String tableName = jObj.optString("reftable","");
            String primarykey = jObj.optString("refprimarykey","");
            int module = Integer.parseInt(jObj.optString("module","-1"));

            udpateCustomTable(primarykey, tableName, colnum, module, companyid);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.daleteMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "Dimension has been deleted successfully.", null, null, 1);
    }

    @Override
    public KwlReturnObject getCustomTableName(com.krawler.utils.json.base.JSONObject reqParams) throws JSONException, ServiceException {
        List list = null;
        String query = " select module,reftable,lineitem,refprimarykey,reftableclasspath from customtables ";
        list = executeSQLQuery(query);
        return new KwlReturnObject(true, "Address field fetched successfully.", null, list, list.size());
    }

    @Override
    public JSONArray createJsonForCustomTableList(List<Object> tableList) throws JSONException {
        JSONArray bulkData = new JSONArray();
        for (Object object : tableList) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("module", (Integer) data[0]);
            jSONObject.put("reftable", data[1].toString());
            jSONObject.put("lineitem", (Integer) data[2]);
            jSONObject.put("refprimarykey", data[3].toString());
            jSONObject.put("reftableclasspath", data.length>4 && data[4]!=null ? data[4].toString() : "");
            bulkData.put(jSONObject);
        }
        return bulkData;
    }

    int udpateCustomTable(String customgolbaltableid, String customgolbaltable, long column, int module, String company) {
        String recvalue = "";
        int count = 0;
        List list = null;
        ArrayList params = new ArrayList();
        try {
            params.add(module);
            params.add(company);

            String colquery = "select " + customgolbaltableid + " from " + customgolbaltable + " where col" + column + " is not NULL and moduleId=? and company=?";

            list = executeSQLQuery(colquery, params.toArray());
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                ArrayList newParams = new ArrayList();
                recvalue = (String) itr.next();
                newParams.add(recvalue);
                newParams.add(company);

                String insertQuery = "update " + customgolbaltable + " set col" + column + "=NULL where " + customgolbaltableid + "=? and company=? ";

                int no = executeSQLUpdate(insertQuery, newParams.toArray());

                count++;
            }
        } catch (Exception ex) {

        }
        return count;
    }

    @Override
    public HashMap<String, String> getParentIds(String[] comIds, String Id) {
        HashMap<String, String> allParent = new HashMap<String, String>();
        try {
            String sql = "select value from FieldComboData f where f.id=?";
            List listVlaue = executeQuery(sql, Id);
            Iterator it = listVlaue.iterator();
            String value = "";
            while (it.hasNext()) {
                // FieldComboData fcd = (FieldComboData)it.next();  444
                value = (String) it.next();
            }
            for (int i = 0; i < comIds.length; i++) {

                String hql = "select id from FieldComboData f where f.value= ? and f.fieldid = ? ";
                List list = executeQuery(hql, new Object[]{value, comIds[i]});
                Iterator it1 = list.iterator();
                while (it1.hasNext()) {
                    // FieldComboData fcd = (FieldComboData) it1.next();
                    String idp = (String) it1.next();
                    allParent.put(comIds[i], idp);
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accMasterItemsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return allParent;
    }

    @Override
    public KwlReturnObject getallModuleNamesUsingSql(String allIds) {
        String ids = allIds.replace(",", "','");
        List list = null;
        try {
            String hql = "select moduleid,relatedmoduleid,isactivated,relatedmoduleisallowedit from fieldparams where id in('" + ids + "') ";
            list = executeSQLQuery( hql);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getValueIfFieldInOtherModule(List list, String groupBy) {
        String allValue = "";
        String allids = "'cc'";
        List list1 = null;
        Iterator it = list.iterator();
        while (it.hasNext()) {
            FieldParams field = (FieldParams) it.next();
            allids = allids + ",'" + field.getId() + "'";
        }

        try {
            String hql = " select f.value, fp.moduleid, fieldid from fieldcombodata f, fieldparams fp where f.fieldid in(" + allids + ")  and f.fieldid= fp.id  group by " + groupBy;
            list1 = executeSQLQuery( hql);
        } catch (ServiceException ex) {
            Logger.getLogger(accMasterItemsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new KwlReturnObject(true, "Master item has been added successfully.", null, list1, list1.size());
    }

    @Override
    public void insertNewValues(HashSet fieldids, String allValues, String oldValue) {
        try {
            Iterator it = fieldids.iterator();
            String[] values = allValues.replace(oldValue, "").split(";"); //[0].equals("");
            while (it.hasNext()) {
                String fieldid = ((String) it.next()).toString();
                for (int j = 0; j < values.length; j++) {
                    if (values[j].equals("")) {
                        continue;
                    }
                    FieldComboData fieldComboData = new FieldComboData();
                    fieldComboData.setId(UUID.randomUUID().toString().replace("-", ""));
                    fieldComboData.setValue(values[j]);
                    fieldComboData.setField((FieldParams) get(FieldParams.class, fieldid));
                    fieldComboData.setFieldid(fieldid);
                    save(fieldComboData);
                    //update(fieldComboData);
                    System.out.println("value " + values[j] + " and id is " + fieldid);
//               String hql = "INSERT into fieldcombodata (id, value, fieldid) value(?,?,?)";
//               String id= UUID.randomUUID().toString().replace("-", "");
//               executeQuery(hql, new Object[]{ id, values[j], fieldid });
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public String getParentFieldId(String companyid, String parentname, int moduleid) {
        String parentid = "";
        List<FieldParams> list = Collections.EMPTY_LIST;
        try {
            String hql = "from FieldParams fp where fp.company.companyID= '" + companyid + "' and fp.fieldlabel='" + parentname + "' and fp.moduleid=" + moduleid;
            list = executeQuery(hql);
            if (list.size() > 0) {
                FieldParams fp = list.get(0);
                parentid = fp.getId();
            }
        } catch (Exception ex) {
            Logger.getLogger(accMasterItemsImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parentid;
    }

    @Override
    public KwlReturnObject saveSalesComissionScehma(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {

            SalesComissionScehma salesComissionScehma = new SalesComissionScehma();
            
            if (itemmap.containsKey("id") && itemmap.get("id") != null) {
                salesComissionScehma = (SalesComissionScehma) get(SalesComissionScehma.class, (String) itemmap.get("id"));
            }

            if (itemmap.containsKey("lowerlimit")) {
                double lowerlimit = Double.parseDouble((String) itemmap.get("lowerlimit"));
                salesComissionScehma.setLowerlimit(lowerlimit);
            }
            if (itemmap.containsKey("upperlimit")) {
                double upperlimit = Double.parseDouble((String) itemmap.get("upperlimit"));
                salesComissionScehma.setUpperlimit(upperlimit);
            }
            if (itemmap.containsKey("commissiontype")) {
                int commissiontype = (Integer) itemmap.get("commissiontype");
                salesComissionScehma.setCommissiontype(commissiontype);
            }
            if (itemmap.containsKey("percentage")) {
                int typeId = Integer.parseInt((String) itemmap.get("percentage"));
                salesComissionScehma.setPercentageType(typeId);
            }
            if (itemmap.containsKey("amount")) {
                double typeId = Double.parseDouble((String) itemmap.get("amount"));
                salesComissionScehma.setAmount(typeId);
            }
            if (itemmap.containsKey("itemid")) {
                salesComissionScehma.setSchemaItem((String) itemmap.get("itemid"));
            }
            if (itemmap.containsKey("categoryid")) {
                salesComissionScehma.setCategoryid((String) itemmap.get("categoryid"));
            }
            if (itemmap.containsKey("productid")){
                salesComissionScehma.setProductId((String) itemmap.get("productid"));
            }
            if (itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                salesComissionScehma.setCompany(company);
            }
            saveOrUpdate(salesComissionScehma);
            list.add(salesComissionScehma);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.saveSalesComissionScehma :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item Price Formula has been added successfully.", null, list, list.size());
    }

    @Override
    public KwlReturnObject getsalesComissionScehma(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from SalesComissionScehma";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject deleteSalesComissionScehma(String itemid) throws ServiceException {
        boolean successflag = false;
        try {
            SalesComissionScehma salesComissionScehma = (SalesComissionScehma) get(SalesComissionScehma.class, itemid);
            if (salesComissionScehma != null) {
                delete(salesComissionScehma);
                successflag = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteSalesComissionScehma  :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, "Sales Comission Scehma has been deleted successfully.", null, null, 1);
    }
    
     public KwlReturnObject getPackages(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();       
        ArrayList params = new ArrayList();
        String condition = "";               
        String query = " from Packages where company.companyID=?";
        params.add(filterParams.get("companyid"));        
        returnList = executeQuery(query, params.toArray());       
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
     public KwlReturnObject isPackageUsedInTransaction(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();       
        ArrayList params = new ArrayList();
        params.add(filterParams.get("packageid"));        
        params.add(filterParams.get("companyid")); 
        
        String query = " from ItemPackingDetail where packages.packageid=? and company.companyID=?";
        returnList = executeQuery(query, params.toArray());       
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
     
      public KwlReturnObject deletePackage(String packageid, String companyid) throws ServiceException {
        String delQuery = "delete from Packages p where p.packageid=? and p.company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{packageid, companyid});
        return new KwlReturnObject(true, "Package entry has been deleted successfully.", null, null, numRows);
    }
     
      public KwlReturnObject addPackages(HashMap<String, Object> packageMap) throws ServiceException {
        List list = new ArrayList();
        try {
            Packages packages = new Packages();
            packages = buildPackage(packages, packageMap);
            save(packages);
            list.add(packages);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addPackage : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Package has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updatePackages(HashMap<String, Object> packageMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String packageid = (String) packageMap.get("packageid");
            Packages packages = (Packages) get(Packages.class, packageid);
            if (packages != null) {
                packages = buildPackage(packages, packageMap);
                saveOrUpdate(packages);
            }
            list.add(packages);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updatePackage : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Package has been updated successfully", null, list, list.size());
    }
    
     public Packages buildPackage(Packages packages, HashMap<String, Object> packageMap) {
        if (packageMap.containsKey("packagename")) {
            packages.setPackagename((String)packageMap.get("packagename"));
        }
        if (packageMap.containsKey("measurement")) {
            packages.setMeasurement((String) packageMap.get("measurement"));
        }
        if (packageMap.containsKey("packageweight")) {
            packages.setPackageweight((Double) packageMap.get("packageweight"));
        }
        if (packageMap.containsKey("companyid")) {
            Company company = packageMap.get("companyid") == null ? null : (Company) get(Company.class, (String) packageMap.get("companyid"));
            packages.setCompany(company);
        }
        return packages;
    }
     
    @Override
    public KwlReturnObject addPricingBandItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            PricingBandMaster pricingBandMaster;
            String deliveryPlannerID = (String) itemmap.get("id");
            if (!StringUtil.isNullOrEmpty(deliveryPlannerID)) {
                pricingBandMaster = (PricingBandMaster) get(PricingBandMaster.class, deliveryPlannerID);
            } else {
                pricingBandMaster = new PricingBandMaster();
            }
            if (itemmap.containsKey("name")) {
                pricingBandMaster.setName((String) itemmap.get("name"));
            }
            if (itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                pricingBandMaster.setCompany(company);
            }
            if (itemmap.containsKey("isDefaultToPOS") && itemmap.get("isDefaultToPOS") != null) {
                pricingBandMaster.setDefaultToPOS((Boolean) itemmap.get("isDefaultToPOS"));
            }
            if (itemmap.containsKey("isIncludingGst") && itemmap.get("isIncludingGst") != null) {
                pricingBandMaster.setIsIncludingGST((Boolean) itemmap.get("isIncludingGst"));
            }
            
            saveOrUpdate(pricingBandMaster);
            list.add(pricingBandMaster);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addPricingBandItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }

    @Override
    public KwlReturnObject getPricingBandItems(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        int count=0;
        try {
            String start = "";
            if (requestParams.containsKey("start") && requestParams.get("start") != null)  {
                start = (String) requestParams.get("start");
            }
            String limit = "";
            if (requestParams.containsKey("limit") && requestParams.get("limit") != null)  {
                limit = (String) requestParams.get("limit");
            }
            
            boolean isDefaultToPOS = false;
            if (requestParams.containsKey("isDefaultToPOS") && requestParams.get("isDefaultToPOS") != null) {
                isDefaultToPOS = (Boolean) requestParams.get("isDefaultToPOS");
            }
            ArrayList params = new ArrayList();
            String condition = "";
            
            params.add(requestParams.get("companyid"));
            
            if (requestParams.containsKey("name")) {
                params.add(requestParams.get("name"));
                condition += " and name = ? ";
            }
            
            if (isDefaultToPOS) {
                condition += " and defaultToPOS = true ";
            }
            
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String searchString = (String) requestParams.get("ss");
                if (!StringUtil.isNullOrEmpty(searchString)) {
                    String[] searchcol = new String[]{"name"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, searchString, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    condition += searchQuery;
                }
            }
            
            String orderBy = " order by name asc ";
            
            String query = " from PricingBandMaster where volumeDiscount = false and company.companyID = ? " + condition + orderBy;
            
            list = executeQuery(query, params.toArray());
            count=list.size();
            
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getPricingBandItems :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getProductsForPricingBandMasterDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            String productid = "";
            if (requestParams.containsKey("productid") && requestParams.get("productid") != null) {
                productid = (String) requestParams.get("productid");
            }

            ArrayList params = new ArrayList();
            String condition = "";
            params.add((String) requestParams.get("companyID"));
            
            if (!StringUtil.isNullOrEmpty(productid) && !productid.equalsIgnoreCase("All")) {
                productid = AccountingManager.getFilterInString(productid);
                condition += " and p.id in " + productid + "  ";
            }
            
            String searchString = (String) requestParams.get("ss");
            if (!StringUtil.isNullOrEmpty(searchString)) {
                String[] searchcol = new String[]{"p.name", "p.productid"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, searchString, 2);
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                condition += searchQuery;
            }
            
             String query = " select p.id, p.name, p.productid from product p where p.deleteflag = 'F' and p.company = ? " + condition;
            
            returnList = executeSQLQuery( query, params.toArray());
            totalCount = returnList.size();

            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                returnList = executeSQLQueryPaging( query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }
    
    @Override
    public KwlReturnObject saveOrUpdatePricingBandMasterDetails(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            PricingBandMasterDetail pricingBandMasterDetail;
            String rowid = (String) requestParams.get("rowid");
            if (!StringUtil.isNullOrEmpty(rowid)) {
                pricingBandMasterDetail = (PricingBandMasterDetail) get(PricingBandMasterDetail.class, rowid);
            } else {
                pricingBandMasterDetail = new PricingBandMasterDetail();
            }
            
            if (requestParams.containsKey("pricingBandMasterID") && requestParams.get("pricingBandMasterID") != null) {
                PricingBandMaster pricingBandMaster = (PricingBandMaster) get(PricingBandMaster.class, (String) requestParams.get("pricingBandMasterID"));
                pricingBandMasterDetail.setPricingBandMaster(pricingBandMaster);
            }
            
            if (requestParams.containsKey("currencyID") && requestParams.get("currencyID") != null) {
                KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, (String) requestParams.get("currencyID"));
                pricingBandMasterDetail.setCurrency(currency);
            }

            if (requestParams.containsKey("productID") && requestParams.get("productID") != null) {
                pricingBandMasterDetail.setProduct((String) requestParams.get("productID"));
            }

            if (requestParams.containsKey("purchasePrice") && requestParams.get("purchasePrice") != null) {
                pricingBandMasterDetail.setPurchasePrice(Double.parseDouble((String) requestParams.get("purchasePrice")));
            }

            if (requestParams.containsKey("salesPrice") && requestParams.get("salesPrice") != null) {
                pricingBandMasterDetail.setSalesPrice(Double.parseDouble((String) requestParams.get("salesPrice")));
            }

            if (requestParams.containsKey("companyID") && requestParams.get("companyID") != null) {
                Company company = (Company) get(Company.class, (String) requestParams.get("companyID"));
                pricingBandMasterDetail.setCompany(company);
            }
            
            if (requestParams.containsKey("applicableDate") && requestParams.get("applicableDate") != null) {
                pricingBandMasterDetail.setApplicableDate((Date) requestParams.get("applicableDate"));
            }
            
            if (requestParams.containsKey("minimumQty") && requestParams.get("minimumQty") != null) {
                pricingBandMasterDetail.setMinimumQty((Integer) requestParams.get("minimumQty"));
            }
            
            if (requestParams.containsKey("maximumQty") && requestParams.get("maximumQty") != null) {
                pricingBandMasterDetail.setMaximumQty((Integer) requestParams.get("maximumQty"));
            }
            
            if (requestParams.containsKey("discountType") && requestParams.get("discountType") != null) {
                pricingBandMasterDetail.setDiscountType((String) requestParams.get("discountType"));
            }
            
            if (requestParams.containsKey("disocuntValue") && requestParams.get("disocuntValue") != null) {
                pricingBandMasterDetail.setDiscountValue(Double.parseDouble((String) requestParams.get("disocuntValue")));
            }
            
            if (requestParams.containsKey("useCommonDiscount") && requestParams.get("useCommonDiscount") != null) {
                pricingBandMasterDetail.setUseCommonDiscount((Boolean) requestParams.get("useCommonDiscount"));
            }
            
            saveOrUpdate(pricingBandMasterDetail);

            list.add(pricingBandMasterDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPricingBandFromCustomer(String pricingBandID, String companyID) throws ServiceException {
        String selQuery = " from Customer where pricingBandMaster.ID = ? and company.companyID = ? ";
        List list = executeQuery(selQuery, new Object[]{pricingBandID, companyID});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPricingBandFromVendor(String pricingBandID, String companyID) throws ServiceException {
        String selQuery = " from Vendor where pricingBandMaster.ID = ? and company.companyID = ? ";
        List list = executeQuery(selQuery, new Object[]{pricingBandID, companyID});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * 
     * @param VolumediscountID
     * @return
     * @throws ServiceException 
     */
    //for updating newly mapped price bands with volume discount
    @Override
    public KwlReturnObject deletePricingListBandMapping(String VolumediscountID) throws ServiceException {
        int numRows = 0;
        String delQuery = "delete from PricingBandmappingWithVolumeDisc pbv where pbv.volumediscountid.ID=?";
        numRows += executeUpdate(delQuery, new Object[]{VolumediscountID});
        return new KwlReturnObject(true, "price band Mapped with volume discount has been deleted successfully.", null, null, numRows);
    }
    /**
     * 
     * @param volParams
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject savePricingListBandMapping(Map<String, Object> volParams) throws ServiceException {
        List list = new ArrayList();
        String pricingBandID = "", volumediscountid = "", pricingBandMasterDetailid = "";
        if (volParams.containsKey("pricingBandID")) {
            pricingBandID = (String) volParams.get("pricingBandID");
        }
        if (volParams.containsKey("volumediscountid")) {
            volumediscountid = (String) volParams.get("volumediscountid");
        }
        try {
            PricingBandmappingWithVolumeDisc bandmappingWithVolumeDisc = new PricingBandmappingWithVolumeDisc();
            if (pricingBandID != null) {
                bandmappingWithVolumeDisc.setPricebandid((PricingBandMaster) get(PricingBandMaster.class, pricingBandID));
            }
            if (volumediscountid != null) {
                bandmappingWithVolumeDisc.setVolumediscountid((PricingBandMaster) get(PricingBandMaster.class, volumediscountid));
            }
            save(bandmappingWithVolumeDisc);
            list.add(bandmappingWithVolumeDisc);
        } catch (Exception e) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.saveCustomerProductMapping", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
    public KwlReturnObject deletePricingBandDetails(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(requestParams.get("pricingBandID"));
        params.add(requestParams.get("companyID"));
        
        String delQuery = "delete  from PricingBandMasterDetail where pricingBandMaster.ID = ? and company.companyID = ? ";
        int numRows = executeUpdate(delQuery, params.toArray());

        return new KwlReturnObject(true, "Pricing Band Details has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject deletePricingBand(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(requestParams.get("pricingBandID"));
        params.add(requestParams.get("companyID"));
        
        String delQuery = "delete  from PricingBandMaster where ID = ? and company.companyID = ? ";
        int numRows = executeUpdate(delQuery, params.toArray());

        return new KwlReturnObject(true, "Pricing Band has been deleted successfully.", null, null, numRows);
    }
    
     @Override
    public KwlReturnObject getPriceOfProductForPricingBandAndCurrency(HashMap<String, Object> requestParams) throws ServiceException {
       List returnList = new ArrayList();
        int totalCount = 0;
        try {
            boolean useCommonDiscount = false;
            if (requestParams.containsKey("useCommonDiscount") && requestParams.get("useCommonDiscount") != null) {
                useCommonDiscount = (Boolean) requestParams.get("useCommonDiscount");
            }
            boolean isPricePolicyUseDiscount = false;
            if (requestParams.containsKey("isPricePolicyUseDiscount") && requestParams.get("isPricePolicyUseDiscount") != null) {
                isPricePolicyUseDiscount = (Boolean) requestParams.get("isPricePolicyUseDiscount");
            }
            boolean isVolumeDiscount = false;
            if (requestParams.containsKey("isVolumeDiscount") && requestParams.get("isVolumeDiscount") != null) {
                isVolumeDiscount = (Boolean) requestParams.get("isVolumeDiscount");
            }
            boolean isSavePricingBandMasterDetails = false;
            if (requestParams.containsKey("isSavePricingBandMasterDetails") && requestParams.get("isSavePricingBandMasterDetails") != null) {
                isSavePricingBandMasterDetails = (Boolean) requestParams.get("isSavePricingBandMasterDetails");
            }
            boolean isPriceListBandReport = false;
            if (requestParams.containsKey(Constants.IS_PRICE_LIST_BAND_REPORT) && requestParams.get(Constants.IS_PRICE_LIST_BAND_REPORT) != null) {
                isPriceListBandReport = (Boolean) requestParams.get(Constants.IS_PRICE_LIST_BAND_REPORT);
            }
            String currencyID = requestParams.get("currencyID") != null? (String) requestParams.get("currencyID") : "";
            
            ArrayList params = new ArrayList();
            String query = "";
            String conditionSubQuery = "";
            String conditionSQL = "";
            
            if (isPricePolicyUseDiscount) {
                params.add((String) requestParams.get("companyID"));
                params.add((String) requestParams.get("pricingBandMasterID"));
                
                if (!StringUtil.isNullOrEmpty(currencyID)) {
                    params.add(currencyID);
                    conditionSQL += " and pbmd.currency = ? ";
                }
                
                if (requestParams.containsKey("productID") && requestParams.get("productID") != null) {
                    params.add((String) requestParams.get("productID"));
                    conditionSQL += " and pbmd.product = ? ";
                }
                
                if (requestParams.containsKey("applicableDate") && requestParams.get("applicableDate") != null && isSavePricingBandMasterDetails) {
                    params.add((Date) requestParams.get("applicableDate"));
                    conditionSQL += " and pbmd.applicabledate = ? ";
                } else if (requestParams.containsKey("applicableDate") && requestParams.get("applicableDate") != null) {
                    params.add((Date) requestParams.get("applicableDate"));
                    String conditionSubString = useCommonDiscount? "" : " and product=pbmd.product group by product ";
                    conditionSQL += " and pbmd.applicabledate in (select max(applicabledate) as ld from pricingbandmasterdetails where company=pbmd.company and pricingbandmaster=pbmd.pricingbandmaster and applicabledate <= ? " + conditionSubString + ") ";
                }
                
                if (useCommonDiscount) {
                    conditionSQL += " and pbmd.usecommondiscount = 'T' ";
                }
                
                query = " select pbmd.id from pricingbandmasterdetails pbmd "
                        + " left join pricingbandmaster pbm on pbm.id = pbmd.pricingbandmaster "
                        + " where pbm.volumediscount = 'T' and pbmd.company = ? and pbmd.pricingbandmaster = ? "
                        + conditionSQL;
            } else {
                params.add((String) requestParams.get("companyID"));
                params.add((String) requestParams.get("pricingBandMasterID"));
                params.add((String) requestParams.get("productID"));                

                if (!StringUtil.isNullOrEmpty(currencyID)) {
                    params.add(currencyID);
                    conditionSQL += " and pbmd.currency = ? ";
                }

                if (isVolumeDiscount) {
                    conditionSQL += " and pbm.volumediscount = 'T' ";
                }

                if (requestParams.containsKey("applicableDate") && requestParams.get("applicableDate") != null && isSavePricingBandMasterDetails) {
                    params.add((Date) requestParams.get("applicableDate"));
                    conditionSubQuery = " and pbmd.applicabledate = ? ";
                } else if (requestParams.containsKey("applicableDate") && requestParams.get("applicableDate") != null) {
                    if (isPriceListBandReport) {
                        params.add((String) requestParams.get("companyID"));
                        params.add((String) requestParams.get("pricingBandMasterID"));
                        params.add(currencyID);
                        params.add((String) requestParams.get("productID"));
                        params.add((Date) requestParams.get("applicableDate"));
                        conditionSubQuery = " and pbmd.applicabledate = (select max(applicabledate) as ld from pricingbandmasterdetails where company=? and pricingbandmaster=? and currency=? and product=? and applicabledate <= ? group by product) ";
                    } else {
                        params.add((Date) requestParams.get("applicableDate"));
                        conditionSubQuery = " and pbmd.applicabledate in (select max(applicabledate) as ld from pricingbandmasterdetails where company=pbmd.company and pricingbandmaster=pbmd.pricingbandmaster and currency=pbmd.currency and product=pbmd.product and applicabledate <= ? group by product) ";
                    }
                }
                
                query = " select pbmd.purchaseprice, pbmd.salesprice, pbmd.id, pbmd.minimumqty, pbmd.maximumqty from pricingbandmasterdetails pbmd "
                        + " inner join pricingbandmaster pbm on pbm.id = pbmd.pricingbandmaster "
                        + " where pbmd.company = ? and pbmd.pricingbandmaster = ? and pbmd.product = ? "
                        +  conditionSQL
                        +  conditionSubQuery;
            }
            
            returnList = executeSQLQuery( query, params.toArray());
            
            if (returnList.isEmpty()) {
                if (!isPricePolicyUseDiscount) {
                    if ((requestParams.containsKey("applicableDate") && requestParams.get("applicableDate") != null && isSavePricingBandMasterDetails) || !isPriceListBandReport) {
                        query = " select pbmd.purchaseprice, pbmd.salesprice,'', pbmd.minimumqty, pbmd.maximumqty from pricingbandmasterdetails pbmd "
                                + " left join pricingbandmaster pbm on pbm.id = pbmd.pricingbandmaster "
                                + " where pbmd.company = ? and pbmd.pricingbandmaster = ? and pbmd.product = ? "
                        +  conditionSQL
                        +" and pbmd.applicabledate in (select max(applicabledate) as ld from pricingbandmasterdetails where company=pbmd.company and pricingbandmaster=pbmd.pricingbandmaster and currency=pbmd.currency and product=pbmd.product and applicabledate <= ? group by product) ";

                        returnList = executeSQLQuery( query, params.toArray());
                    }
                }
                
                if (!isPricePolicyUseDiscount && returnList.isEmpty()) {
                    Object[] objArr = new Object[]{0.0, 0.0, "", 0, 0};
                    returnList.add(objArr);
                }
            }
            totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }
    
    @Override
    public List getExistingVolumes(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        String pricebandidsmappedwithvol="";
        try {
            boolean isSavePricingBandMasterDetails = false;
            if (requestParams.containsKey("isSavePricingBandMasterDetails") && requestParams.get("isSavePricingBandMasterDetails") != null) {
                isSavePricingBandMasterDetails = (Boolean) requestParams.get("isSavePricingBandMasterDetails");
            }
            String currencyID = requestParams.get("currencyID") != null ? (String) requestParams.get("currencyID") : "";

            ArrayList params = new ArrayList();
            String query = "";
            String conditionSubQuery = "";
            String conditionSQL = "";

            params.add((String) requestParams.get("companyID"));
            params.add((String) requestParams.get("productID"));

            if (!StringUtil.isNullOrEmpty(currencyID)) {
                params.add(currencyID);
                conditionSQL += " and pbmd.currency = ? ";
            }
       
            conditionSQL += " and pbm.volumediscount = 'T'";

            if (requestParams.containsKey("pricebandidsmappedwithvol") && requestParams.get("pricebandidsmappedwithvol") != null && requestParams.get("pricebandidsmappedwithvol")!="") {
                String pricebandidsmappedwithvolIDs = (String) requestParams.get("pricebandidsmappedwithvol");
                pricebandidsmappedwithvol = AccountingManager.getFilterInString(pricebandidsmappedwithvolIDs);
                 conditionSQL += " and pbmv.pricebandid in "+pricebandidsmappedwithvol;
            }
            if (requestParams.containsKey("applicableDate") && requestParams.get("applicableDate") != null && isSavePricingBandMasterDetails) {
                params.add((Date) requestParams.get("applicableDate"));
                conditionSubQuery = " and pbmd.applicabledate = ? ";
            }
            /**
             * inner join with pricingbandmappingwithvolumedisc:- To get volume discount for given product and mapped
             * with (pricebandidsmappedwithvol) price band.
             */
            query = " select pbmd.id from pricingbandmasterdetails pbmd "
                    + " left join pricingbandmaster pbm on pbm.id = pbmd.pricingbandmaster "
                    + " inner join pricingbandmappingwithvolumedisc pbmv on pbmv.volumediscountid=pbm.id "
                    + " where pbmd.company = ? and pbmd.product = ? "
                    + conditionSQL
                    + conditionSubQuery;

            returnList = executeSQLQuery( query, params.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return returnList;
    }
    
    @Override
    public KwlReturnObject getPriceOfBandForProductAndCurrency(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        try {
            String currencyID = requestParams.get("currencyID") != null? (String) requestParams.get("currencyID") : "";
            ArrayList params = new ArrayList();
            String query = "";
            String conditionSubQuery = "";
            String conditionSQL = "";
            
            params.add((String) requestParams.get("companyID"));
            params.add((String) requestParams.get("pricingBandMasterID"));
            params.add((String) requestParams.get("productID"));
            
            if (!StringUtil.isNullOrEmpty(currencyID)) {
                params.add(currencyID);
                conditionSQL += " and pbmd.currency = ? ";
            }

            if (requestParams.containsKey("applicableDate") && requestParams.get("applicableDate") != null) {
                params.add((Date) requestParams.get("applicableDate"));
                conditionSubQuery = " and pbmd.applicabledate in (select max(applicabledate) as ld from pricingbandmasterdetails where company=pbmd.company and pricingbandmaster=pbmd.pricingbandmaster and currency=pbmd.currency and product=pbmd.product and applicabledate <= ? group by product) ";
            }

            query = " select pbmd.purchaseprice, pbmd.salesprice, pbmd.id, pbmd.minimumqty, pbmd.maximumqty from pricingbandmasterdetails pbmd "
                    + " left join pricingbandmaster pbm on pbm.id = pbmd.pricingbandmaster "
                    + " where pbmd.company = ? and pbmd.pricingbandmaster = ? and pbmd.product = ? "
                    + conditionSQL
                    + conditionSubQuery;

            returnList = executeSQLQuery( query, params.toArray());
            totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }
    
   @Override
    public KwlReturnObject getPOSProductsPrice(HashMap<String, Object> requestParams) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount = 0;
        String limitQuery="";
        String selQuery="";
        try {
            ArrayList params = new ArrayList();
            String condition = "",innerCondition="";
            params.add(requestParams.get("companyID"));
            
            if (requestParams.containsKey("isSyncToPOS") && requestParams.get("isSyncToPOS") != null) {
                params.add((Boolean) requestParams.get("isSyncToPOS"));
                condition += " and ppbmd.pricingBandMaster.defaultToPOS = ? ";
            }
            
            if (requestParams.containsKey("productIds") && requestParams.get("productIds") != null) {
                String filterProductIds = AccountingManager.getFilterInString(requestParams.get("productIds").toString());
                condition += " and ppbmd.product in "+filterProductIds+" ";
            }
            
            if (requestParams.containsKey("currencyID") && requestParams.get("currencyID") != null) {
                params.add(requestParams.get("currencyID"));
                condition += " and ppbmd.currency.currencyID = ? ";
                innerCondition += " and ppbmd.currency.currencyID = pbmd.currency.currencyID ";
            }
            if (requestParams.containsKey("applicableDate") && requestParams.get("applicableDate") != null) {
                params.add((Date) requestParams.get("applicableDate"));
                condition += "and ppbmd.applicableDate in (select max(pbmd.applicableDate) as ld from PricingBandMasterDetail pbmd where ppbmd.company.companyID =pbmd.company.companyID and ppbmd.pricingBandMaster = pbmd.pricingBandMaster and ppbmd.product = pbmd.product  " + innerCondition + " and pbmd.applicableDate <= ? group by product) ";
            }
            if (requestParams.containsKey("offset") && requestParams.get("offset") != null && requestParams.get("firstRequest")!= null && (Boolean)requestParams.get("firstRequest") == true) {    // this code for get count to send product price to pos side
                selQuery = " select 1 from PricingBandMasterDetail ppbmd where company.companyID = ? " + condition;
            } else if(requestParams.get("fromPOS") == null) {
                selQuery = "from PricingBandMasterDetail ppbmd where ppbmd.company.companyID = ? " + condition;
            }
            if (!StringUtil.isNullOrEmpty(selQuery)) {
                returnList = executeQuery(selQuery, params.toArray());
                totalCount = returnList.size();
            }
             if (requestParams.containsKey("offset") && requestParams.get("offset") != null && requestParams.get("fromPOS") != null) { // this code for get count to send product price to pos side
                int start=Integer.parseInt(requestParams.get("offset").toString());
                int limit=Integer.parseInt(requestParams.get("limit").toString());
//                Paging paging = new Paging(start, limit);
                selQuery ="Select product,purchasePrice,salesPrice,currency from PricingBandMasterDetail ppbmd where ppbmd.company.companyID = ? " + condition;
                returnList = executeQueryPaging(selQuery, params.toArray(),new Integer[]{start,limit});
            }
            
        }  catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getLocationLevelMapping(String companyId) throws ServiceException {
       List returnList=new ArrayList();
       int totalCount=0;
       try{
           String query="from LocationLevelMapping where company.companyID=?";
           returnList=executeQuery(query,companyId);
       }catch(Exception e){
           throw ServiceException.FAILURE(e.getMessage(), e);
       }
       return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getLocationLevel() throws ServiceException {
          List returnList=new ArrayList();
       int totalCount=0;
       try{
           String query="from LocationLevel";
           returnList=executeQuery(query);
       }catch(Exception e){
           throw ServiceException.FAILURE(e.getMessage(), e);
       }
       return new KwlReturnObject(true, "", null, returnList, totalCount);
    }
    
    @Override
    public KwlReturnObject getPriceListVolumeDiscountItems(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String start = "";
            if (requestParams.containsKey("start") && requestParams.get("start") != null) {
                start = (String) requestParams.get("start");
            }
            String limit = "";
            if (requestParams.containsKey("limit") && requestParams.get("limit") != null) {
                limit = (String) requestParams.get("limit");
            }
            
            boolean isFlatPriceListVolumeDiscount = (requestParams.get("isFlatPriceListVolumeDiscount") != null)? (Boolean) requestParams.get("isFlatPriceListVolumeDiscount") : false;
            ArrayList params = new ArrayList();
            String condition = "";

            params.add(requestParams.get("companyid"));

            if (requestParams.containsKey("name")) {
                params.add(requestParams.get("name"));
                condition += " and name = ? ";
            }
            
            if (isFlatPriceListVolumeDiscount) {
                condition += " and pricePolicyValue = 2 ";
            }
            
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String searchString = (String) requestParams.get("ss");
                if (!StringUtil.isNullOrEmpty(searchString)) {
                    String[] searchcol = new String[]{"name"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, searchString, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(searchString, "and", searchcol);
                    condition += searchQuery;
                }
            }

            String orderBy = " order by name asc ";

            String query = " from PricingBandMaster where volumeDiscount = true and company.companyID = ? " + condition + orderBy;

            list = executeQuery(query, params.toArray());
            
            if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false) {
                list = executeQueryPaging(query, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getPriceListVolumeDiscountItems :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /**
     *
     * @param pricingVolumelistID
     * @return
     * @throws ServiceException
     */
    //get volue discount which mapped with priceband
    public KwlReturnObject getPricingVolumeDiscountMapped(String pricingVolumelistID) throws ServiceException {
        KwlReturnObject result;
        ArrayList params = new ArrayList();
        params.add(pricingVolumelistID);

        String query = "from PricingBandmappingWithVolumeDisc where pricebandid.ID =  ? ";
        List list = executeQuery(query, params.toArray());

        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }

    /**
     *
     * @param pricingVolumelistID
     * @return
     * @throws ServiceException
     */
    //get price band mapped with volume discount
    public KwlReturnObject getPricingbandMapped(String pricingVolumelistID) throws ServiceException {
        KwlReturnObject result;
        ArrayList params = new ArrayList();
        params.add(pricingVolumelistID);

        String query = "from PricingBandmappingWithVolumeDisc where volumediscountid.ID =  ? ";
        List list = executeQuery(query, params.toArray());

        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }

    /**
     *
     * @param pricingVolumelistID
     * @return
     * @throws ServiceException
     */
    //get price band IDs mapped with volume discount
    public String getPricingbandMappedwithvolumeDisc(String pricingVolumelistID) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String valuesStr = "";
        boolean issuccess = false;
        try {
            KwlReturnObject result = getPricingbandMapped(pricingVolumelistID);
            List list = result.getEntityList();
            Iterator itr = list.iterator();

            while (itr.hasNext()) {
                PricingBandmappingWithVolumeDisc row = (PricingBandmappingWithVolumeDisc) itr.next();
                PricingBandMaster bandMaster = row.getPricebandid();
                if (itr.hasNext()) {
                    valuesStr += bandMaster.getID() + ",";
                } else {
                    valuesStr += bandMaster.getID();
                }
            }
            issuccess = true;
        } catch (Exception e) {
            try {
                throw ServiceException.FAILURE(e.getMessage(), e);
            } catch (ServiceException ex) {
                Logger.getLogger(accMasterItemsImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, "");
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return valuesStr;
    }

    @Override
    public KwlReturnObject addPriceListVolumeDiscountItem(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            PricingBandMaster pricingBandMaster;
            String ID = (String) itemmap.get("id");
            if (!StringUtil.isNullOrEmpty(ID)) {
                pricingBandMaster = (PricingBandMaster) get(PricingBandMaster.class, ID);
            } else {
                pricingBandMaster = new PricingBandMaster();
            }
            
            if (itemmap.containsKey("name")) {
                pricingBandMaster.setName((String) itemmap.get("name"));
            }
            
            if (itemmap.containsKey("pricePolicyValue") && itemmap.get("pricePolicyValue") != null) {
                pricingBandMaster.setPricePolicyValue((Integer) itemmap.get("pricePolicyValue"));
            }
            
            if (itemmap.containsKey("desc") && itemmap.get("desc") != null) {
                pricingBandMaster.setDescription((String) itemmap.get("desc"));
            }
            
            if (itemmap.containsKey("volumeDiscount") && itemmap.get("volumeDiscount") != null) {
                pricingBandMaster.setVolumeDiscount((Boolean) itemmap.get("volumeDiscount"));
            }
            
            if (itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                pricingBandMaster.setCompany(company);
            }

            saveOrUpdate(pricingBandMaster);
            list.add(pricingBandMaster);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addPriceListVolumeDiscountItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Price List - Volume Discount item has been added successfully.", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject deletePriceListVolumeDiscount(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(requestParams.get("priceListVolumeDiscountID"));
        params.add(requestParams.get("companyID"));

        String delQuery = "delete  from PricingBandMaster where ID = ? and company.companyID = ? ";
        int numRows = executeUpdate(delQuery, params.toArray());

        return new KwlReturnObject(true, "Price List - Volume Discount has been deleted successfully.", null, null, numRows);
    }

    @Override
    public KwlReturnObject updateMasterSetting(HashMap<String, Object> requestParams) throws ServiceException {
         List list = new ArrayList();
        try {
            LocationLevelMapping levelMapping;

            if (requestParams.containsKey("id")) {
                levelMapping = (LocationLevelMapping) get(LocationLevelMapping.class, (String) requestParams.get("id"));
            } else {
                levelMapping = new LocationLevelMapping();
            }
            
            if (requestParams.containsKey("newLevelName")) {
                levelMapping.setNewLevelNm((String)requestParams.get("newLevelName"));
            }
             if (requestParams.containsKey("parent")) {
                levelMapping.setParent((String)requestParams.get("parent"));
            }
              if (requestParams.containsKey("activate")) {
                levelMapping.setActivate((!StringUtil.isNullOrEmpty((String)requestParams.get("activate"))) ? Boolean.parseBoolean((String)requestParams.get("activate")) : false);
            }
              if (requestParams.containsKey("levelId")) {
                   LocationLevel lLevel = requestParams.get("levelId") == null ? null : (LocationLevel) get(LocationLevel.class, Integer.parseInt((String)requestParams.get("levelId")));
                levelMapping.setLlevelid(lLevel);
            }
              if(requestParams.containsKey("company")){
                  Company company = requestParams.get("company") == null ? null : (Company) get(Company.class, (String) requestParams.get("company"));
                  levelMapping.setCompany(company);
              }
            

            saveOrUpdate(levelMapping);   //problem with saveorupdate
            list.add(levelMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addPriceListVolumeDiscountItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
 @Override
    public KwlReturnObject addUpdateStoreMasterItem(HashMap<String, Object> requestParams) throws ServiceException {
         List list = new ArrayList();
        try {
            StoreMaster storeMaster;

            if (requestParams.containsKey("id")) {
                storeMaster = (StoreMaster) get(StoreMaster.class, (String) requestParams.get("id"));
            } else {
                storeMaster = new StoreMaster();
            }
            
            if (requestParams.containsKey("name")) {
                storeMaster.setName((String)requestParams.get("name"));
            }
            
            if (requestParams.containsKey("parentid")) {
                storeMaster.setParentId((String)requestParams.get("parentid"));
            }
            if (requestParams.containsKey("type")) {
                storeMaster.setType(Integer.parseInt(requestParams.get("type")+""));
            }
            if (requestParams.containsKey("companyid")) {
                Company company = requestParams.get("companyid") == null ? null : (Company) get(Company.class, (String) requestParams.get("companyid"));
                storeMaster.setCompany(company);
            }
            saveOrUpdate(storeMaster);   
            list.add(storeMaster);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addPriceListVolumeDiscountItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject buildQueryForgetStoreMasters(Map<String, Object> requestParams) throws ServiceException {
        HashMap<String, Object> filterRequestParams = new HashMap<>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
        if(requestParams.containsKey(Constants.companyKey) &&requestParams.get(Constants.companyKey)!= null){
            filter_names.add("company.companyID");
            filter_params.add(requestParams.get(Constants.companyKey));
        }
        if(requestParams.containsKey("type") &&requestParams.get("type")!= null){
            filter_names.add("type");
            filter_params.add(requestParams.get("type"));    
        }
        if(requestParams.containsKey("name") &&requestParams.get("name")!= null){
            filter_names.add("name");
            filter_params.add(requestParams.get("name"));    
        }
        
        order_by.add("name");
        order_type.add("asc");
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        filterRequestParams.put("order_by", order_by);
        filterRequestParams.put("order_type", order_type);
        return getStoreMasters(filterRequestParams);
    }                    
    @Override
    public KwlReturnObject getStoreMasters(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from StoreMaster";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public KwlReturnObject deleteStoreMasterItem(String itemid) throws ServiceException {
         boolean successflag = false;
         String name="";
        try {
            StoreMaster storeMasterObj = (StoreMaster) get(StoreMaster.class, itemid);
            name=storeMasterObj.getName();
            if (storeMasterObj != null) {
                delete(storeMasterObj);
                successflag = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteStoreMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(successflag, name+" has been deleted successfully.", null, null, 1);
    }
    
     @Override
    public KwlReturnObject getLLevelMappingFrmLevlId(String companyId,int levelid) throws ServiceException {
       List returnList=new ArrayList();
       int totalCount=0;
       try{
           String query="from LocationLevelMapping where company.companyID=? and llevelid.id=? ";
           returnList=executeQuery(query,new Object[]{companyId,levelid});
       }catch(Exception e){
           throw ServiceException.FAILURE(e.getMessage(), e);
       }
       return new KwlReturnObject(true, "", null, returnList, totalCount);
    }

    @Override
    public KwlReturnObject getCompanPreferencesSql(String companyID) throws ServiceException {
        List returnList = new ArrayList();
        int totalCount=0;
          ArrayList params = new ArrayList();
        try{
          params.add(companyID);
          HashMap<String,String> compPref;
          String query = "select c.islocationcompulsory,c.iswarehousecompulsory,c.isrowcompulsory ,c.israckcompulsory, c.isbincompulsory,extracomp.activateInventoryTab from CompanyAccountPreferences c,ExtraCompanyPreferences extracomp where c.ID=extracomp.id and c.company.companyID = ? ";
          returnList = executeQuery( query, params.toArray());
          totalCount = returnList.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, totalCount);
    }
    
    @Override
    public KwlReturnObject deletePriceListVolumeDiscountDetails(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(requestParams.get("priceListVolumeDiscountID"));
        params.add(requestParams.get("companyID"));

        String delQuery = "delete  from PricingBandMasterDetail where pricingBandMaster.ID = ? and company.companyID = ? ";
        int numRows = executeUpdate(delQuery, params.toArray());

        return new KwlReturnObject(true, "Price List - Volume Discount Details has been deleted successfully.", null, null, numRows);
}

    @Override
    public KwlReturnObject getMasterItemFromCustomerID(String customerid) throws ServiceException {
        //Accessing master item records when customerid is known. 
        ArrayList params = new ArrayList();
        params.add(customerid);
        params.add(customerid);
//        String query = "from MasterItem where id in(select salesperson from SalesPersonMapping where customerID.ID=?) ";
        String query = "from MasterItem where id in(select salesperson from SalesPersonMapping where customerID.ID=?) or id in (select mappingSalesPerson from Customer where ID=?) ";
        List list = executeQuery(query, params.toArray());
        KwlReturnObject result = new KwlReturnObject(true, null, null, list, list.size());
        
        return result;
    }
      @Override
    public KwlReturnObject getMasterItemFromVendorID(String vendorid) throws ServiceException {
        //Accessing master item records when customerid is known. 
        ArrayList params = new ArrayList();
        params.add(vendorid);
        params.add(vendorid);
//        String query = "from MasterItem where id in(select agent from VendorAgentMapping where vendorID.ID=?) ";
        String query = "from MasterItem where id in(select agent from VendorAgentMapping where vendorID.ID=?) or id in (select mappingAgent from Vendor where ID=?) ";
        List list = executeQuery(query, params.toArray());
        KwlReturnObject result = new KwlReturnObject(true, null, null, list, list.size());
        
        return result;
    }

    @Override
    public KwlReturnObject isStoreMasterSettingUsed(String companyID) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(companyID);
        String query = "from NewProductBatch where (location.id is not null or warehouse.id is not null or row.id is not null or rack.id is not null or bin.id is not null ) and company.companyID=? ";
        List list = executeQueryPaging(query, params.toArray(),new Integer[]{0,1});
        KwlReturnObject result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }
    public KwlReturnObject getdefault_warehouse(String warehouseid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from InventoryWarehouse iw where iw.id=? and iw.company.companyID=? and isdefault=1" ;
        list = executeQuery(q, new Object[]{warehouseid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

        public KwlReturnObject getProductsusedinWarehouses(String warehouseid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Product p where p.warehouse.id=? and p.company.companyID=? " ;
        list = executeQuery(q, new Object[]{warehouseid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
}
     
        public KwlReturnObject getBatches_warehouses(String warehouseid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from NewProductBatch p where p.warehouse.id=? and p.company.companyID=? " ;
        list = executeQuery(q, new Object[]{warehouseid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
   
        public KwlReturnObject getInvoice_warehouses(String warehouseid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Invoice inv where inv.custWarehouse.id=? and inv.company.companyID=? " ;
        list = executeQuery(q, new Object[]{warehouseid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
        public KwlReturnObject getSO_warehouses(String warehouseid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from SalesOrder so where (so.custWarehouse.id=? or so.requestWarehouse.id=?) and so.company.companyID=? " ;
        list = executeQuery(q, new Object[]{warehouseid,warehouseid,companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
      
        public KwlReturnObject getDO_warehouses(String warehouseid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from DeliveryOrder do where do.custWarehouse.id=? and do.company.companyID=? " ;
        list = executeQuery(q, new Object[]{warehouseid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
        public KwlReturnObject getSR_warehouses(String warehouseid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from SalesReturn sr where sr.custWarehouse.id=? and sr.company.companyID=? " ;
        list = executeQuery(q, new Object[]{warehouseid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
        
        public KwlReturnObject getcustomer_warehouses(String warehouseid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from CustomerWarehouseMap cw where cw.inventoryWarehouse.id=? " ;
        list = executeQuery(q, new Object[]{warehouseid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
        
        
       public KwlReturnObject getdefault_location(String locationid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from InventoryLocation iw where iw.id=? and iw.company.companyID=? and isdefault=1";
        list = executeQuery(q, new Object[]{locationid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
       public KwlReturnObject getProductsusedinlocations(String locationid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Product p where p.location.id=? and p.company.companyID=? " ;
        list = executeQuery(q, new Object[]{locationid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
      public KwlReturnObject getBatches_locations(String locationid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from NewProductBatch p where p.location.id=? and p.company.companyID=? ";
        list = executeQuery(q, new Object[]{locationid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
      
        public KwlReturnObject getSO_locations(String locationid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from SalesOrder so where  so.requestLocation.id=? and so.company.companyID=? " ;
        list = executeQuery(q, new Object[]{locationid,companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
        public KwlReturnObject getWarehouses_locations(String locationid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from InventoryWarehouse inw where inw.location.id=? and inw.company.companyID=? " ;
        list = executeQuery(q, new Object[]{locationid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
       public KwlReturnObject getConsignmentRequest_locations(String locationid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from ConsignmentRequestLocationMapping cw where cw.inventorylocation.id=? ";
        list = executeQuery(q, new Object[]{locationid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getIBGReceivingBankDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String condition = "";
        ArrayList params = new ArrayList();
        params.add((String) requestParams.get("companyId"));

        if (requestParams.containsKey("vendorId") && requestParams.get("vendorId") != null) {
            params.add((String) requestParams.get("vendorId"));
            condition += " and rbd.vendor.ID=? ";
        }

        if (requestParams.containsKey("masterItemId") && requestParams.get("masterItemId") != null) {
            params.add((String) requestParams.get("masterItemId"));
            condition += " and rbd.masterItem=? ";
        }

        String query = "Select id, receivingbankcode, receivingbankname, receivingbranchcode, receivingaccountnumber, receivingaccountname  From ibgreceivingbankdetails rbd where rbd.company=? " + condition;

        List list = executeSQLQuery( query, params.toArray());

        KwlReturnObject result = new KwlReturnObject(true, null, null, list, list.size());

        return result;
    }
    public KwlReturnObject deleteIBGReceivingBankDetails(HashMap<String, Object> requestMap) throws ServiceException {

        List params = new ArrayList();
        String companyId="",masterItemId="";
        if (requestMap.containsKey("companyid") && requestMap.get("companyid") != null) {
            companyId = (String) requestMap.get("companyid");
            params.add(companyId);
        }
        if (requestMap.containsKey("masterItemId") && requestMap.get("masterItemId") != null) {
            masterItemId = (String) requestMap.get("masterItemId");
            params.add(masterItemId);
        }
        String deleteQuery = "DELETE FROM IBGReceivingBankDetails rbd where rbd.company.companyID=? and rbd.masterItem.ID=? ";
        int numRows = executeUpdate(deleteQuery, params.toArray());
        return new KwlReturnObject(true, "", null, null, numRows);
    }
    
    public KwlReturnObject getDeliveryPlanner(HashMap<String, Object> requestMap) throws ServiceException {
        List list = null;
        try {
            executeSQLUpdate("SET SESSION group_concat_max_len = 2048;");//maximum length of the result of GROUP_CONCAT is 1024 characters. so increasing limit upto 2048
            List params = new ArrayList();
            String companyId = "", masterItemId = "";
            ArrayList name = null;
            String hql = "select count(*) FROM deliveryplanner";
            ArrayList value = null;
            if (requestMap.get("filter_names") != null && requestMap.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestMap.get("filter_names"));
                value = new ArrayList((List<Object>) requestMap.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
            }
            list = executeSQLQuery(hql, value.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getSalesOrdersBySalesPerson(HashMap<String, Object> requestParams) throws ServiceException{
        List list = new ArrayList();
        List params = new ArrayList();
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            String companyId = (String) requestParams.get("companyid");
            params.add(companyId);
        }
        if (requestParams.containsKey("salespersonid") && requestParams.get("salespersonid") != null) {
            String salespersonid = (String) requestParams.get("salespersonid");
            params.add(salespersonid);
        }
        String q = "from SalesOrder so where   so.company.companyID=? and so.salesperson.ID=?" ;
        list = executeQuery(q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getWCByWCMasterItem(HashMap<String, Object> requestParams) throws ServiceException{
        List list = new ArrayList();
        List params = new ArrayList();
        String type =  "";
        String id = "";
        String conditionSql = "";
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            String companyId = (String) requestParams.get("companyid");
            conditionSql += " where wc.company.companyID=? ";
            params.add(companyId);
        } 
        if (requestParams.containsKey("type") && requestParams.get("type") != null) {
            type = (String) requestParams.get("type");
        }
        if (requestParams.containsKey("id") && requestParams.get("id") != null) {
            id = (String) requestParams.get("id");
            params.add(id);
        }
        if (type.equalsIgnoreCase(Constants.WORK_CENTRE_LOCATION)) {
            conditionSql += " and wc.workcenterlocation.ID=? ";
        } else if (type.equalsIgnoreCase(Constants.WORK_CENTRE_MANAGER)) {
            conditionSql += " and wc.workcentermanager.ID=? ";
        } else if (type.equalsIgnoreCase(Constants.WORK_CENTRE_TYPE)) {
            conditionSql += " and wc.workcentertype.ID=? ";
        } else if (type.equalsIgnoreCase(Constants.WORK_TYPE)) {
            conditionSql += " and wc.worktype.ID=? ";
        }
        String q = "from WorkCentre wc " + conditionSql ;
        list = executeQuery(q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
      public KwlReturnObject getQualityByWCMasterItem(HashMap<String, Object> requestParams) throws ServiceException{
        List list = new ArrayList();
        List params = new ArrayList();
        String type =  "";
        String id = "";
        String conditionSql = "";
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            String companyId = (String) requestParams.get("companyid");
            conditionSql += " where qc.company.companyID=? ";
            params.add(companyId);
        } 
      
        if (requestParams.containsKey("id") && requestParams.get("id") != null) {
            id = (String) requestParams.get("id");
            params.add(id);
        }
        
        if (requestParams.containsKey("type") && requestParams.get("type") != null && (requestParams.get("type").toString().equals(Constants.QUALITY_PARAMETER))) {
            conditionSql += " and qc.qcparameter.ID=? ";
        } else {
            conditionSql += " and qc.qcgroup.ID=? ";
        }    
        String q = "from QualityControl qc " + conditionSql ;
        list = executeQuery(q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    } 
      
    @Override
    public KwlReturnObject checkSalesPersonUsedInAnyTransaction(HashMap<String, Object> requestMap) throws ServiceException, AccountingException {
        List params = new ArrayList();
        String companyId = "", masterItemId = "";
        if (requestMap.containsKey("companyid") && requestMap.get("companyid") != null) {
            companyId = (String) requestMap.get("companyid");
            params.add(companyId);
        }
        if (requestMap.containsKey("masterItemId") && requestMap.get("masterItemId") != null) {
            masterItemId = (String) requestMap.get("masterItemId");
            params.add(masterItemId);
        }
        String hqlQuery = "from Invoice inv where inv.company.companyID=? and inv.masterSalesPerson.ID=? ";
        List list = executeQuery(hqlQuery, params.toArray());
        if (list == null || list.isEmpty()) {
            hqlQuery = "from DeliveryOrder do where do.company.companyID=? and do.salesperson.ID=? ";
            list = executeQuery(hqlQuery, params.toArray());

            if (list == null || list.isEmpty()) {
                hqlQuery = "from SalesOrder so where so.company.companyID=? and so.salesperson.ID=? ";
                list = executeQuery(hqlQuery, params.toArray());
            }
            if (list == null || list.isEmpty()) {
                hqlQuery = "from Quotation cq where cq.company.companyID=? and cq.salesperson.ID=? ";
                list = executeQuery(hqlQuery, params.toArray());
            }

        }
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getCustomerQuotationsBySalesPerson(HashMap<String, Object> requestParams) throws ServiceException{
        List list = new ArrayList();
        List params = new ArrayList();
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            String companyId = (String) requestParams.get("companyid");
            params.add(companyId);
        }
        if (requestParams.containsKey("salespersonid") && requestParams.get("salespersonid") != null) {
            String salespersonid = (String) requestParams.get("salespersonid");
            params.add(salespersonid);
        }
        String q = "from Quotation q where   q.company.companyID=? and q.salesperson.ID=?" ;
        list = executeQuery(q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getSalesInvoicesBySalesPerson(HashMap<String, Object> requestParams) throws ServiceException{
        List list = new ArrayList();
        List params = new ArrayList();
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            String companyId = (String) requestParams.get("companyid");
            params.add(companyId);
        }
        if (requestParams.containsKey("salespersonid") && requestParams.get("salespersonid") != null) {
            String salespersonid = (String) requestParams.get("salespersonid");
            params.add(salespersonid);
        }
        String q = "from Invoice inv where   inv.company.companyID=? and inv.masterSalesPerson.ID=?" ;
        list = executeQuery(q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getDeliveryOrdersBySalesPerson(HashMap<String, Object> requestParams) throws ServiceException{
        List list = new ArrayList();
        List params = new ArrayList();
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            String companyId = (String) requestParams.get("companyid");
            params.add(companyId);
        }
        if (requestParams.containsKey("salespersonid") && requestParams.get("salespersonid") != null) {
            String salespersonid = (String) requestParams.get("salespersonid");
            params.add(salespersonid);
        }
        String q = "from DeliveryOrder do where   do.company.companyID=? and do.salesperson.ID=?" ;
        list = executeQuery(q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getCreditNotesBySalesPerson(HashMap<String, Object> requestParams) throws ServiceException{
        List list = new ArrayList();
        List params = new ArrayList();
        if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
            String companyId = (String) requestParams.get("companyid");
            params.add(companyId);
        }
        if (requestParams.containsKey("salespersonid") && requestParams.get("salespersonid") != null) {
            String salespersonid = (String) requestParams.get("salespersonid");
            params.add(salespersonid);
        }
        String q = "from CreditNote cn where   cn.company.companyID=? and cn.salesPerson.ID=?" ;
        list = executeQuery(q, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getCIMBReceivingBankDetails(HashMap<String, Object> requestParams) throws ServiceException {
        String condition = "";
        ArrayList params = new ArrayList();
        params.add((String) requestParams.get("companyId"));

        if (requestParams.containsKey("vendorId") && requestParams.get("vendorId") != null) {
            params.add((String) requestParams.get("vendorId"));
            condition += " and rbd.vendor.ID=? ";
        }

        if (requestParams.containsKey("masterItemId") && requestParams.get("masterItemId") != null) {
            params.add((String) requestParams.get("masterItemId"));
            condition += " and rbd.masterItem=? ";
        }

        String query = "Select id, collectionaccountnumber, collectionaccountname, girobiccode, referencenumber, emailforgiro from cimbreceivingdetails rbd where rbd.company=? " + condition;

        List list = executeSQLQuery( query, params.toArray());

        KwlReturnObject result = new KwlReturnObject(true, null, null, list, list.size());

        return result;
    }
    
    public KwlReturnObject deleteCIMBReceivingBankDetails(HashMap<String, Object> requestMap) throws ServiceException {

        List params = new ArrayList();
        String companyId="",masterItemId="";
        if (requestMap.containsKey("companyid") && requestMap.get("companyid") != null) {
            companyId = (String) requestMap.get("companyid");
            params.add(companyId);
        }
        if (requestMap.containsKey("masterItemId") && requestMap.get("masterItemId") != null) {
            masterItemId = (String) requestMap.get("masterItemId");
            params.add(masterItemId);
        }
        String deleteQuery = "DELETE FROM CIMBReceivingDetails rbd where rbd.company.companyID=? and rbd.masterItem.ID=? ";
        int numRows = executeUpdate(deleteQuery, params.toArray());
        return new KwlReturnObject(true, "", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject activateDeactivateSalesperson(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            String arr[] = (String[]) requestParams.get("data");
            boolean salespersonActivateDeactivateFlag = (Boolean) requestParams.get("salespersonActivateDeactivateFlag");
            for (int i = 0; i < arr.length; i++) {
                if (!StringUtil.isNullOrEmpty(arr[i])) {
                    MasterItem masteritemObj = (MasterItem) get(MasterItem.class, arr[i]);
                    if (masteritemObj != null) {
                        masteritemObj.setActivated(salespersonActivateDeactivateFlag);
                        update(masteritemObj);
                        list.add(masteritemObj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject checkAgentUsedInAnyTransaction(HashMap<String, Object> requestMap) throws ServiceException, AccountingException {
        List params = new ArrayList();
        String companyId = "", masterItemAgentId = "";
        if (requestMap.containsKey("companyid") && requestMap.get("companyid") != null) {
            companyId = (String) requestMap.get("companyid");
            params.add(companyId);
        }
        if (requestMap.containsKey("agentid") && requestMap.get("agentid") != null) {
            masterItemAgentId = (String) requestMap.get("agentid");
            params.add(masterItemAgentId);
        }
        String hqlQuery = "from VendorAgentMapping Vr where  Vr.agent.ID=? ";   //Vendor
        List list = executeQuery(hqlQuery,new Object[]{masterItemAgentId});
        if (list == null || list.isEmpty()) {
            hqlQuery = "from VendorQuotation vq where vq.company.companyID=? and vq.masteragent.ID=? ";  //vendor Quotation
            list = executeQuery(hqlQuery, params.toArray());

            if (list == null || list.isEmpty()) {
                hqlQuery = "from PurchaseOrder po where po.company.companyID=? and po.masteragent.ID=? "; //purchase order
                list = executeQuery(hqlQuery, params.toArray());
            }
            if (list == null || list.isEmpty()) {
                hqlQuery = "from GoodsReceiptOrder gro where gro.company.companyID=? and gro.masterAgent.ID=? ";//goods receipt order
                list = executeQuery(hqlQuery, params.toArray());
            }
              if (list == null || list.isEmpty()) {
               hqlQuery = "from GoodsReceipt gr where gr.company.companyID=? and gr.masterAgent.ID=? ";   //purchase Invoice
               list = executeQuery(hqlQuery, params.toArray());
            }
        }
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject checkDOGRStatusUsedInAnyTransaction(Map<String,Object> requestMap) throws ServiceException, AccountingException {
        List params = new ArrayList();
        List list = null;
        String companyId = "", masterItemStatusId = "";
        String hqlQuery="";
        boolean isDOStatus=false,isGRStatus=false;
        if (requestMap.containsKey("companyid") && requestMap.get("companyid") != null) {
            companyId = (String) requestMap.get("companyid");
            params.add(companyId);
        }
        if (requestMap.containsKey("statusid") && requestMap.get("statusid") != null) {
            masterItemStatusId = (String) requestMap.get("statusid");
            params.add(masterItemStatusId);
        }
        if (requestMap.containsKey("isdeliveryorder") && requestMap.get("isdeliveryorder") != null) {
            isDOStatus = (Boolean) requestMap.get("isdeliveryorder");
        }else if (requestMap.containsKey("isgoodsreceipt") && requestMap.get("isgoodsreceipt") != null) {
            isGRStatus = (Boolean) requestMap.get("isgoodsreceipt");
        }
        
        if (isDOStatus) {
            hqlQuery = "from DeliveryOrder do where  do.company.companyID=? and do.status.ID=? ";   //Vendor
        } else if (isGRStatus) {
            hqlQuery = "from GoodsReceiptOrder gr where  gr.company.companyID=? and  gr.status.ID=? ";
        }
        list = executeQuery(hqlQuery, params.toArray());

        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPaymentsWithCimb(HashMap<String, Object> requestParams) throws ServiceException {
        String condition = "";
        ArrayList params = new ArrayList();
        params.add((String) requestParams.get("companyid"));
        if (requestParams.containsKey("cimbReceivingBankDetailId") && requestParams.get("cimbReceivingBankDetailId") != null) {
            params.add((String) requestParams.get("cimbReceivingBankDetailId"));
            condition += " and p.cimbreceivingbankdetails.id=? ";
        }
        String query = "Select p.ID from Payment p where p.company.companyID = ? "+condition;
        
        List list = executeQuery(query,params.toArray());
        
        KwlReturnObject result = new KwlReturnObject(true, null, null, list, list.size());

        return result;
    }
    
    @Override
    public KwlReturnObject getFieldComboDataByFieldName(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        String companyid = (String) requestParams.get("companyid");
        String fieldName = (String) requestParams.get("fieldname");
        int moduleid = (Integer) requestParams.get("moduleid");
        try {
            String query = "SELECT fcd.id FROM fieldcombodata fcd INNER JOIN fieldparams fp ON fp.id=fcd.fieldid WHERE fp.companyid=? AND fp.fieldname=? AND fp.moduleid=?";
            list = executeSQLQuery(query, new Object[]{companyid, fieldName, moduleid});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getFieldComboDataByFieldName :" + ex.getMessage(), ex);
        }
            return new KwlReturnObject(true, "", null, list, list.size());
    }
     @Override
    public KwlReturnObject getMasterItemByNameorID(String companyid, String value, String masterGroupID,String fetchColumn,String conditionColumn) throws ServiceException {
          List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(masterGroupID);
            params.add(companyid);
            params.add(value);
            String query = "SELECT "+fetchColumn +" from MasterItem mst where  mst.masterGroup.ID=? and mst.company.companyID=? and "+conditionColumn+"=? ";
            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getMasterItemByNameorID", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getTermForCompany(String companyid) throws ServiceException {
        List list = new ArrayList();
        String selQuery = "from Term where company.companyID=?";
        list = executeQuery( selQuery, new Object[]{companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPricingBandMasterDetailsList(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from PricingBandMasterDetail";
        return buildNExecuteQuery(query, requestParams);
    }
    
    @Override
    public KwlReturnObject deleteProductBrandDiscountDetails(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String condition = "";
        
        params.add((String) requestParams.get("companyID"));
        
        if (requestParams.containsKey("pricingBandID") && requestParams.get("pricingBandID") != null) {
            condition += " and pricingBandMaster.ID=? ";
            params.add((String) requestParams.get("pricingBandID"));
        }
        
        if (requestParams.containsKey("productBrandID") && requestParams.get("productBrandID") != null) {
            condition += " and productBrand.id=? ";
            params.add((String) requestParams.get("productBrandID"));
        }
        
        if (requestParams.containsKey("customerCategoryID") && requestParams.get("customerCategoryID") != null) {
            condition += " and customerCategory.ID=? ";
            params.add((String) requestParams.get("productBrandID"));
        }
        
        String delQuery = "delete from ProductBrandDiscountDetails where company.companyID=? " + condition;
        int numRows = executeUpdate(delQuery, params.toArray());
        
        return new KwlReturnObject(true, "Discount Details has been deleted successfully.", null, null, numRows);
    }
    
    @Override
    public KwlReturnObject checkProductBrandUsedInAnyTransaction(HashMap<String, Object> requestMap) throws ServiceException, AccountingException {
        List params = new ArrayList();
        String companyId = "", productBrandID = "";
        if (requestMap.containsKey("companyID") && requestMap.get("companyID") != null) {
            companyId = (String) requestMap.get("companyID");
            params.add(companyId);
        }
        if (requestMap.containsKey("productBrandID") && requestMap.get("productBrandID") != null) {
            productBrandID = (String) requestMap.get("productBrandID");
            params.add(productBrandID);
        }
        
        String hqlQuery = "from Product where company.companyID = ? and productBrand.ID=? ";
        List list = executeQuery(hqlQuery, params.toArray());
        
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPaymentMethodIdFromName(String methodName, String companyId) throws ServiceException {
        KwlReturnObject result = null;
        if (!StringUtil.isNullOrEmpty(companyId) && !StringUtil.isNullOrEmpty(methodName)) {
            ArrayList params = new ArrayList();
            params.add(methodName);
            params.add(companyId);
            String query = "from PaymentMethod where methodName=? and company.companyID=?";
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        }
        return result;
    }
    
    @Override
    public KwlReturnObject getCustomerList(HashMap<String, Object> requestParams) throws ServiceException {
        String initialQuery = "from Customer";
        return buildNExecuteQuery(initialQuery, requestParams);
    }
    
    @Override
    public KwlReturnObject getDeliveryOrderList(HashMap<String, Object> requestParams) throws ServiceException {
        String initialQuery = "from DeliveryOrder";
        return buildNExecuteQuery(initialQuery, requestParams);
    }
    
    @Override
    public KwlReturnObject checkVehicleNumberUsedInAnyTransaction(HashMap<String, Object> requestMap) throws ServiceException, AccountingException {
        List params = new ArrayList();
        if (requestMap.containsKey("companyID") && requestMap.get("companyID") != null) {
            String companyID = (String) requestMap.get("companyID");
            params.add(companyID);
        }
        if (requestMap.containsKey("vehicleNoID") && requestMap.get("vehicleNoID") != null) {
            String vehicleNoID = (String) requestMap.get("vehicleNoID");
            params.add(vehicleNoID);
        }
        
        String hqlQuery = "from DeliveryPlanner where company.companyID=? and vehicleNumber.ID=? "; // Delivery Planner
        List list = executeQuery(hqlQuery, params.toArray());

        if (list == null || list.isEmpty()) {
            hqlQuery = "from Customer where company.companyID=? and vehicleNo.ID=? "; // Customer
            list = executeQuery(hqlQuery, params.toArray());
        }
        
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public List getUsedBatchAssemblyProduct(HashMap<String, Object> params) throws ServiceException {
        List usedBatchList = new ArrayList();
        String productid = "", subproduct = "", companyid = "" ;
        String warehouse = "", location = "", appendQuery="";
        try {
            if(params!=null && params.containsKey("productid") && params.get("productid")!=null){
                productid = (String)params.get("productid");
            }
            if(params!=null && params.containsKey("companyid") && params.get("companyid")!=null){
                companyid = (String)params.get("companyid");
            }
            if(params!=null && params.containsKey("subproduct") && params.get("subproduct")!=null){
                subproduct = (String)params.get("subproduct");
            }
            if(params!=null && params.containsKey("warehouse") && !StringUtil.isNullOrEmpty(params.get("warehouse").toString())){
                warehouse = (String)params.get("warehouse");
                appendQuery = " AND npb.warehouse='"+warehouse+"' ";
            }
            if(params!=null && params.containsKey("location") && !StringUtil.isNullOrEmpty(params.get("location").toString())){
                location = (String)params.get("location");
                appendQuery = appendQuery + " AND npb.location='"+location+"' ";
            }
            String query_Batch = "select DISTINCT npb.id from newproductbatch npb "
                    + "INNER JOIN locationbatchdocumentmapping lbdm ON npb.id = lbdm.batchmapid "
                    + "INNER JOIN pbdetails pbd ON lbdm.documentid = pbd.id "
                    + "INNER JOIN productbuild pb ON pbd.build=pb.id "
                    + "where pb.product= ? and aproduct = ? and pb.refno <> \"\" and pb.company = ? "
                    + "AND pb.isBuild='T' "+appendQuery;
            List list_Batch = executeSQLQuery(query_Batch, new Object[]{productid, subproduct, companyid});

            for (int i = 0; i < list_Batch.size(); i++) {
                String listObj_Batch = (String) list_Batch.get(i);
                if (!StringUtil.isNullOrEmpty(listObj_Batch)) {
                    NewProductBatch newproductbatch = (NewProductBatch) get(NewProductBatch.class, listObj_Batch);
                    if (newproductbatch != null) {
                        JSONObject jobj = new JSONObject();
                        jobj.put("id", newproductbatch.getId());
                        jobj.put("batch", newproductbatch.getId());  //This field used as purchasebatchid in JS side.
                        jobj.put("batchname", newproductbatch.getBatchname());
                        jobj.put("productid", newproductbatch.getProduct());
                        jobj.put("warehouse", newproductbatch.getWarehouse()!=null ? newproductbatch.getWarehouse().getId() : "");
                        jobj.put("location", newproductbatch.getLocation()!=null ? newproductbatch.getLocation().getId() : "");
                        jobj.put("row", newproductbatch.getRow()!=null ? newproductbatch.getRow().getId() : "");
                        jobj.put("rack", newproductbatch.getRack()!=null ? newproductbatch.getRack().getId() : "");
                        jobj.put("mfgdate", newproductbatch.getMfgdate()!=null ? newproductbatch.getMfgdate() : "");
                        jobj.put("expdate", newproductbatch.getExpdate()!=null ? newproductbatch.getExpdate() : "");
                        jobj.put("bin", newproductbatch.getBin()!=null ? newproductbatch.getBin().getId() : "");
                        usedBatchList.add(jobj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateInitialInventory:" + ex.getMessage(), ex);
        }
        return usedBatchList;
    }
    
    public List getUsedSerialNoAssemblyProduct(HashMap<String, Object> params) throws ServiceException {
        List usedSrNoList = new ArrayList();
        String productid = "";
        String companyid = "";
        String subproduct = "";
        String batch = "";
        String refno="";
        String batchQuery = "";
        try {
            if(params!=null && params.containsKey("productid") && params.get("productid")!=null){
                productid = (String)params.get("productid");
            }
            if(params!=null && params.containsKey("companyid") && params.get("companyid")!=null){
                companyid = (String)params.get("companyid");
            }
            if(params!=null && params.containsKey("subproduct") && params.get("subproduct")!=null){
                subproduct = (String)params.get("subproduct");
            }
            if(params!=null && params.containsKey("batch") && !StringUtil.isNullOrEmpty(params.get("batch").toString())){
                batch = (String)params.get("batch");
                batchQuery = " AND nbs.batch='"+batch+"' ";
            }
            if(params!=null && params.containsKey("refno") && !StringUtil.isNullOrEmpty(params.get("refno").toString())){
                refno = (String)params.get("refno");
            }
            String query_Serial = "select DISTINCT nbs.id from newbatchserial nbs "
                    + "INNER JOIN serialdocumentmapping sdm ON nbs.id = sdm.serialid "
                    + "INNER JOIN pbdetails pbd ON sdm.documentid = pbd.id "
                    + "INNER JOIN productbuild pb ON pbd.build=pb.id "
                    + "INNER JOIN locationbatchdocumentmapping lmp ON lmp.documentid=pb.id  "
                    + "where pb.product= ? and aproduct = ? and pb.refno <> \"\" and pb.company = ? and pb.isBuild='T' "+batchQuery;
            List list_Serial = executeSQLQuery(query_Serial, new Object[]{productid, subproduct, companyid});

            for (int i = 0; i < list_Serial.size(); i++) {
                String listObj_Serial = (String) list_Serial.get(i);
                if (!StringUtil.isNullOrEmpty(listObj_Serial)) {
                    NewBatchSerial newbatchserial = (NewBatchSerial) get(NewBatchSerial.class, listObj_Serial);
                    if(newbatchserial!=null){
                        JSONObject jobj = new JSONObject();
                        jobj.put("id", newbatchserial.getId());
                        jobj.put("serialno", newbatchserial.getSerialname());
                        jobj.put("serialnoid", newbatchserial.getId());
                        jobj.put("expstart", newbatchserial.getExpfromdate()!=null ? newbatchserial.getExpfromdate():new Date());
                        jobj.put("expend", newbatchserial.getExptodate()!=null ? newbatchserial.getExptodate() : new Date());
                        jobj.put("skufield", (newbatchserial.getSkufield() != null) ? newbatchserial.getSkufield() : "");
                        jobj.put("product", newbatchserial.getProduct());
                        jobj.put("purchaseserialid", newbatchserial.getId());
                        jobj.put("purchasebatchid", (newbatchserial.getBatch() != null) ? newbatchserial.getBatch().getId() : "");
                        //Get NewBatch Object - Extract Location & Warehouse.//Future Use ERP-23242
                        NewProductBatch newProductBatch = newbatchserial.getBatch();
                        if (newProductBatch != null) {
                            String warehouseID = newProductBatch.getWarehouse() != null ? newProductBatch.getWarehouse().getId() : "";
                            String warehouseName = newProductBatch.getWarehouse() != null ? newProductBatch.getWarehouse().getName() : "";
                            String locationID = newProductBatch.getLocation() != null ? newProductBatch.getLocation().getId() : "";
                            String locationName = newProductBatch.getLocation() != null ? newProductBatch.getLocation().getName() : "";

                            jobj.put("warehouseid", warehouseID);
                            jobj.put("warehousename", warehouseName);
                            jobj.put("locationid", locationID);
                            jobj.put("locationname", locationName);
                        }
                        usedSrNoList.add(jobj);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateInitialInventory:" + ex.getMessage(), ex);
        }
        return usedSrNoList;
    }   
    @Override
    public KwlReturnObject getStateIdByName(String name) throws ServiceException{
        List list=new ArrayList();
        try{
            String query = "from State where stateName = ?";
            list = executeQuery(query, new Object[]{name});
        }catch(Exception ex){
            System.out.println("" + ex.getLocalizedMessage());
        }
       return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getUserIdByName(String name,String company) throws ServiceException{
        List list=new ArrayList();
        try{
            String query = "from User where firstName = ? and company.companyID=?";
            list = executeQuery(query, new Object[]{name,company});
        }catch(Exception ex){
            System.out.println("" + ex.getLocalizedMessage());
        }
       return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
   public KwlReturnObject checkreceivedFromUsedInAnyTransaction(HashMap<String, Object> requestMap)throws ServiceException, AccountingException{
          List params = new ArrayList();
        if (requestMap.containsKey("companyID") && requestMap.get("companyID") != null) {
            String companyID = (String) requestMap.get("companyID");
            params.add(companyID);
        }
        if (requestMap.containsKey("receivedFromID") && requestMap.get("receivedFromID") != null) {
            String receivedFromID = (String) requestMap.get("receivedFromID");
            params.add(receivedFromID);
        }
        
        String hqlQuery = "from Receipt where company.companyID=? and receivedFrom.ID=? "; 
        List list = executeQuery(hqlQuery, params.toArray());
        if(list==null ||list.isEmpty()){
        hqlQuery = "from Vendor where company.companyID=? and mappingReceivedFrom.ID=? "; 
        list = executeQuery(hqlQuery, params.toArray());  
        }
        if(list==null ||list.isEmpty()){
        hqlQuery = "from Customer where company.companyID=? and mappingReceivedFrom.ID=? "; 
        list = executeQuery(hqlQuery, params.toArray());  
        }
        
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
        }
       return new KwlReturnObject(true, "", null, list, list.size());
       
   }
       @Override
   public KwlReturnObject checkpaidToUsedInAnyTransaction(HashMap<String, Object> requestMap)throws ServiceException, AccountingException{
          List params = new ArrayList();
        if (requestMap.containsKey("companyID") && requestMap.get("companyID") != null) {
            String companyID = (String) requestMap.get("companyID");
            params.add(companyID);
        }
        if (requestMap.containsKey("PaidToID") && requestMap.get("PaidToID") != null) {
            String paidToID = (String) requestMap.get("PaidToID");
            params.add(paidToID);
        }
        
        String hqlQuery = "from Payment where company.companyID=? and paidTo.ID=? "; 
        List list = executeQuery(hqlQuery, params.toArray());
        if(list==null ||list.isEmpty()){
        hqlQuery = "from Vendor where company.companyID=? and mappingPaidTo.ID=? "; 
        list = executeQuery(hqlQuery, params.toArray());  
        }
        if(list==null ||list.isEmpty()){
        hqlQuery = "from Customer where company.companyID=? and mappingPaidTo.ID=? ";
        list = executeQuery(hqlQuery, params.toArray());  
        }
        
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
        }
       return new KwlReturnObject(true, "", null, list, list.size());
       
   }
    public KwlReturnObject checkBankNameUsedInAnyTransaction(HashMap<String, Object> requestMap)throws ServiceException, AccountingException{
          List params = new ArrayList();
        if (requestMap.containsKey("companyID") && requestMap.get("companyID") != null) {
            String companyID = (String) requestMap.get("companyID");
            params.add(companyID);
        }
        if (requestMap.containsKey("BankNameID") && requestMap.get("BankNameID") != null) {
            String bankNameID = (String) requestMap.get("BankNameID");
            params.add(bankNameID);
        }
        
        String hqlQuery = "from Cheque where company.companyID=? and BankMasterItem.ID=? "; 
        List list = executeQuery(hqlQuery, params.toArray());
        
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
        }
       return new KwlReturnObject(true, "", null, list, list.size());
       
   }
    
    @Override
    public KwlReturnObject checkReasonUsedInAnyTransaction(HashMap<String, Object> requestMap) throws ServiceException, AccountingException {
        List params = new ArrayList();
        String companyID = "", ReasonID = "";
        if (requestMap.containsKey("companyID") && requestMap.get("companyID") != null) {
            companyID = (String) requestMap.get("companyID");
        }
        if (requestMap.containsKey("ReasonID") && requestMap.get("ReasonID") != null) {
            ReasonID = (String) requestMap.get("ReasonID");
        }
        params.add(companyID);
        params.add(ReasonID);
        params.add(companyID);
        params.add(ReasonID);
        params.add(companyID);
        params.add(ReasonID);
        params.add(companyID);
        params.add(ReasonID);
        String sqlQuery = "select dn.id from dntaxentry dn where company = ? and reason = ? "
                + " UNION "
                + " select cn.id from cntaxentry cn where company = ? and reason = ? "
                + " UNION "
                + " select srd.id from srdetails srd where company = ? and reason = ?"
                + " UNION "
                + " select prd.id from prdetails prd where company = ? and reason = ?"  ;  
                

        List list = executeSQLQuery(sqlQuery, params.toArray());

        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
        }
        return new KwlReturnObject(true, "", null, list, list.size());

    }
   /*
    * MRP - If assembled and ready to use inventory product is not freed from newproductbatch entry 
    * then it should not come in blocked quantity
    * 
    */ 
   public KwlReturnObject getLBDMforConsumedUnfreeWODetail(HashMap<String, Object> requestParams) throws ServiceException, AccountingException {
          List params = new ArrayList();
        if (requestParams.containsKey("product") && requestParams.get("product") != null) {
            String productID = (String) requestParams.get("product");
            params.add(productID);
        }
        String hqlQuery = " select lbdm.id from locationbatchdocumentmapping lbdm inner join workordercomponentdetail wocd on lbdm.documentid = wocd.id where wocd.isblockqtyused = 'T' and wocd.product = ?" ; 
        List list = executeSQLQuery(hqlQuery, params.toArray());
        
       return new KwlReturnObject(true, "", null, list, list.size());
   }
    /**
     * Added line level Terms type in master for country specific
     * @param requestParams
     * @throws ServiceException 
     */
    @Override
    public void copyMasterItemsNopList(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            String companyid = requestParams.containsKey("companyid") ? requestParams.get("companyid").toString() : "";
            String country = requestParams.containsKey("country") ? requestParams.get("country").toString() : "";
            String nop = requestParams.containsKey("nopid") ? requestParams.get("nopid").toString() : "";
            String masterGroup = requestParams.containsKey("masterGroup") ? requestParams.get("masterGroup").toString() : "";
            //String state = requestParams.containsKey("state") ? requestParams.get("state").toString() : "";
             String query = "from DefaultMasterItem where ID = ? and masterGroup.ID = ?";
            ArrayList params = new ArrayList();
            params.add(nop);
            params.add(masterGroup);
            List list = executeQuery(query, params.toArray());
            Iterator iter = list.iterator();
            Company company = (Company) get(Company.class, companyid);
            while (iter.hasNext()) {
                DefaultMasterItem defaultMasterItem = (DefaultMasterItem) iter.next();
                MasterItem masterItem = new MasterItem();
                masterItem.setCompany(company);
                masterItem.setMasterGroup(defaultMasterItem.getMasterGroup());
                masterItem.setValue(defaultMasterItem.getValue());
                masterItem.setCode(defaultMasterItem.getCode());
                masterItem.setDefaultMasterItem(defaultMasterItem);
                save(masterItem);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyMasterItems : " + ex.getMessage(), ex);
        }
    }
    
    public KwlReturnObject checkDefaultMasterItemsNOPlist(HashMap<String, Object> requestParams) throws ServiceException, AccountingException {
        String nop = requestParams.containsKey("nopid") ? requestParams.get("nopid").toString() : "";
        String masterGroup = requestParams.containsKey("masterGroup") ? requestParams.get("masterGroup").toString() : "";
        //String state = requestParams.containsKey("state") ? requestParams.get("state").toString() : "";
        String query = "from DefaultMasterItem where ID = ? and masterGroup.ID = ?";
        ArrayList params = new ArrayList();
        params.add(nop);
        params.add(masterGroup);
        List list = executeQuery(query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getMasterItemFromLandingCostCategory(String id ,String company) throws ServiceException {
        KwlReturnObject result;
        String query = "from LandingCostCategory lccategoryid where lccategoryid.company.companyID= ? ";
        ArrayList params = new ArrayList();
        params.add(company);
        if(id != null){
            query += " and lccategoryid.id = ? ";
            params.add(id);
        }
        List list = executeQuery(query, params.toArray());
        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }
    
    public KwlReturnObject getMasterItemFromLandingCostCategory(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        String id = "", lccname = "", company = "";
        int totalCount=0;
        if (requestParams.containsKey("id") && requestParams.get("id") != null) {
            id = (String) requestParams.get("id");
        }
        if (requestParams.containsKey("lccname") && requestParams.get("lccname") != null) {
            lccname = (String) requestParams.get("lccname");
        }
        if (requestParams.containsKey(Constants.companyKey) && requestParams.get(Constants.companyKey) != null) {
            company = (String) requestParams.get(Constants.companyKey);
        }
        
        String query = "from LandingCostCategory lccategoryid where lccategoryid.company.companyID= ? ";
        ArrayList params = new ArrayList();
        params.add(company);
        if (!StringUtil.isNullOrEmpty(id)) {
            query += " and lccategoryid.id = ? ";
            params.add(id);
        }
        if (!StringUtil.isNullOrEmpty(lccname)) {
            query += " and lccategoryid.lccName = ? ";
            params.add(lccname);
        }
        List list = executeQuery(query, params.toArray());
        totalCount=list.size();
        if (requestParams.containsKey("start") && requestParams.get("start") != null && requestParams.containsKey("limit") && requestParams.get("limit") != null) {
            int start = Integer.parseInt(requestParams.get("start").toString());
            int limit = Integer.parseInt(requestParams.get("limit").toString());
            list = executeQueryPaging(query, params.toArray(), new Integer[]{start, limit});
        }
        
        result = new KwlReturnObject(true, null, null, list,totalCount);
        return result;
    }
    public KwlReturnObject getLandingCostCategoryInTranscation(String id,String company) throws ServiceException {
        KwlReturnObject result;
        String query = "from GoodsReceipt where landingCostCategory.id = ? and company.companyID= ? ";
        ArrayList params = new ArrayList();
        params.add(id);
        params.add(company);
        List list = executeQuery(query, params.toArray());
        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }
    public KwlReturnObject getLandingCostCategoryInProduct(String id,String company) throws ServiceException {
        KwlReturnObject result;
        String query = "select * from productid_landingcostcategoryid where lccategoryid = ?";
        ArrayList params = new ArrayList();
        params.add(id);
        List list = executeSQLQuery(query, params.toArray());
        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }
    public KwlReturnObject checkLandingCostCategoryRec(HashMap<String, Object> itemmap) throws ServiceException {
        KwlReturnObject result;
        String query = "select * from t_landingcostcategory ";
        String conditions="";
        ArrayList params = new ArrayList();
        if (itemmap.containsKey("lccategory")) {
            if(!StringUtil.isNullOrEmpty(conditions)){
                conditions+= " and ";
            }else{
                conditions+= " where ";
            }
            conditions+="  lccName = ?";
            params.add(itemmap.get("lccategory").toString());
        }
        if (itemmap.containsKey("companyid")) {
           if(!StringUtil.isNullOrEmpty(conditions)){
                conditions+= " and ";
            }else{
                conditions+= " where ";
            }
            conditions+=" company = ?";
            params.add(itemmap.get("companyid").toString());
        }
        if (itemmap.containsKey("lcallocationid") && itemmap.get("lcallocationid")!= null) {
            if(!StringUtil.isNullOrEmpty(conditions)){
                conditions+= " and ";
            }else{
                conditions+= " where ";
            }
            conditions += " allocationid = ?";
           params.add(itemmap.get("lcallocationid").toString());
        }
        if (itemmap.containsKey("id") && itemmap.get("id")!= null) {
            if(!StringUtil.isNullOrEmpty(conditions)){
                conditions+= " and ";
            }else{
                conditions+= " where ";
            }
            conditions += " ID = ?";
           params.add(itemmap.get("id").toString());
        }
        query+=conditions;
        List list = executeSQLQuery(query,params.toArray());
        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }

    public KwlReturnObject addLandingCostOfCategory(HashMap<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            LandingCostCategory landingCostCetagory = new LandingCostCategory();
            if(itemmap.containsKey("itemID")){
                landingCostCetagory.setId((String) itemmap.get("itemID"));
            }
            if (itemmap.containsKey("lcallocationid")) {
                landingCostCetagory.setLcallocationid(Integer.valueOf((String) itemmap.get("lcallocationid")));
            }
            if (itemmap.containsKey("lccategory")) {
                landingCostCetagory.setLccName((String) itemmap.get("lccategory"));
            }
            if (itemmap.containsKey("companyid")) {
                landingCostCetagory.setCompany((Company) get(Company.class,(String)itemmap.get("companyid")));
            }
            saveOrUpdate(landingCostCetagory);
            list.add(landingCostCetagory);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }
    public KwlReturnObject daleteLandingCostCategoryItem(String id ,String companyid) throws ServiceException {
        List list = new ArrayList();
        List params = new ArrayList();
        try {
            LandingCostCategory lcc=(LandingCostCategory)get(LandingCostCategory.class,id);
            
            String delQuery = "delete from LandingCostCategory where id = ? and company.companyID=? ";
            params.add(id);
            params.add(companyid);
            int numRows = executeUpdate(delQuery, params.toArray());
            if(numRows>0){
                list.add(lcc);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addMasterItem :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been added successfully.", null, list, list.size());
    }
    /*
     * Method for checking if customer bank account type is used anywhere
     */
    @Override
    public KwlReturnObject checkCustomerBankAccountTypeUsedInAnyTransaction(HashMap<String, Object> requestMap ) throws ServiceException,AccountingException{
       List params = new ArrayList();
        if (requestMap.containsKey("companyID") && requestMap.get("companyID") != null) {
            String companyID = (String) requestMap.get("companyID");
            params.add(companyID);
        }
        if (requestMap.containsKey("customerBankAccountTypeId") && requestMap.get("customerBankAccountTypeId") != null) {
            String customerBankAccountTypeId = (String) requestMap.get("customerBankAccountTypeId");
            params.add(customerBankAccountTypeId);
        }
        
        String hqlQuery = "from Invoice where company.companyID=? and CustomerBankAccountType.ID=? "; 
        List list = executeQuery(hqlQuery, params.toArray());
        
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
        } else {
            hqlQuery = "from UOBReceivingDetails where company.companyID=? and customerBankAccountType.ID=? "; 
            list = executeQuery(hqlQuery, params.toArray());
            if (list != null && !list.isEmpty()) {
                throw new AccountingException("You cannot delete selected master item as it is already used in transaction.");
            }
        }
       return new KwlReturnObject(true, "", null, list, list.size());
   }
    public Map<String,Object> getNextAutoNumber_Modified(String companyid, int from, String format, boolean oldflag, Date creationDate) throws ServiceException, AccountingException {
        String autoNumber = "";
        String table = "", field = "", pattern = "";
        String sqltable = "", sqlfield = "";
        int startfrom = 1;
        boolean claim=false;
        boolean recover=false;
        Map<String, Object> seqNumberMap = new HashMap<String, Object>();

        switch (from) {
            case StaticValues.AUTONUM_JOURNALENTRY:
                table = "JournalEntry";
                field = "entryNumber";
                sqltable = "journalentry";
                sqlfield = "entryno";
                pattern = format;
                break;
            case StaticValues.AUTONUM_SALESORDER:
                table = "SalesOrder";
                field = "salesOrderNumber";
                sqltable = "salesorder";
                sqlfield = "sonumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_INVOICE:
                table = "Invoice";
                field = "invoiceNumber";
                sqltable = "invoice";
                sqlfield = "invoicenumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_CASHSALE:
                table = "Invoice";
                field = "invoiceNumber";
                sqltable = "invoice";
                sqlfield = "invoicenumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_CREDITNOTE:
                table = "CreditNote";
                field = "creditNoteNumber";
                sqltable = "creditnote";
                sqlfield = "cnnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_RECEIPT:
                table = "Receipt";
                field = "receiptNumber";
                sqltable = "receipt";
                sqlfield = "receiptnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_PURCHASEORDER:
                table = "PurchaseOrder";
                field = "purchaseOrderNumber";
                sqltable = "purchaseorder";
                sqlfield = "ponumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_GOODSRECEIPT:
                table = "GoodsReceipt";
                field = "goodsReceiptNumber";
                sqltable = "goodsreceipt";
                sqlfield = "grnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_CASHPURCHASE:
                table = "GoodsReceipt";
                field = "goodsReceiptNumber";
                sqltable = "goodsreceipt";
                sqlfield = "grnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_DEBITNOTE:
                table = "DebitNote";
                field = "debitNoteNumber";
                sqltable = "debitnote";
                sqlfield = "dnnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_PAYMENT:
                table = "Payment";
                field = "paymentNumber";
                sqltable = "payment";
                sqlfield = "paymentnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGINVOICE:
                table = "BillingInvoice";
                field = "billingInvoiceNumber";
                sqltable = "billinginvoice";
                sqlfield = "billinginvoicenumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGRECEIPT:
                table = "BillingReceipt";
                field = "billingReceiptNumber";
                sqltable = "billingreceipt";
                sqlfield = "billingreceiptnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGCASHSALE:
                table = "BillingInvoice";
                field = "billingInvoiceNumber";
                sqltable = "billinginvoice";
                sqlfield = "billinginvoicenumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGGOODSRECEIPT:
                table = "BillingGoodsReceipt";
                field = "billingGoodsReceiptNumber";
                sqltable = "billinggr";
                sqlfield = "billinggrnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGPAYMENT:
                table = "BillingPayment";
                field = "billingPaymentNumber";
                sqltable = "billingpayment";
                sqlfield = "billingpaymentnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGCASHPURCHASE:
                table = "BillingGoodsReceipt";
                field = "billingGoodsReceiptNumber";
                sqltable = "billinggr";
                sqlfield = "billinggrnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGPURCHASEORDER:
                table = "BillingPurchaseOrder";
                field = "purchaseOrderNumber";
                sqltable = "billingpurchaseorder";
                sqlfield = "ponumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGSALESORDER:
                table = "BillingSalesOrder";
                field = "salesOrderNumber";
                sqltable = "billingsalesorder";
                sqlfield = "sonumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGDEBITNOTE:
                table = "BillingDebitNote";
                field = "debitNoteNumber";
                sqltable = "billingdebitnote";
                sqlfield = "dnnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BILLINGCREDITNOTE:
                table = "BillingCreditNote";
                field = "creditNoteNumber";
                sqltable = "billingcreditnote";
                sqlfield = "cnnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_CONTRACT:
                table = "Contract";
                field = "contractNumber";
                sqltable = "contract";
                sqlfield = "contractnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_QUOTATION:
                table = "Quotation";
                field = "quotationNumber";
                sqltable = "quotation";
                sqlfield = "quotationnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_VENQUOTATION:
                table = "VendorQuotation";
                field = "quotationNumber";
                sqltable = "vendorquotation";
                sqlfield = "quotationnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_REQUISITION:
                table = "PurchaseRequisition";
                field = "prNumber";
                sqltable = "purchaserequisition";
                sqlfield = "prnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_RFQ:
                table = "RequestForQuotation";
                field = "rfqNumber";
                sqltable = "requestforquotation";
                sqlfield = "rfqnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_PRODUCTID:
                table = "Product";
                field = "productid";
                sqltable = "product";
                sqlfield = "productid";
                pattern = format;
                break;
            case StaticValues.AUTONUM_DELIVERYORDER:
                table = "DeliveryOrder";
                field = "deliveryOrderNumber";
                sqltable = "deliveryorder";
                sqlfield = "donumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_GOODSRECEIPTORDER:
                table = "GoodsReceiptOrder";
                field = "goodsReceiptOrderNumber";
                sqltable = "grorder";
                sqlfield = "gronumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_SALESRETURN:
                table = "SalesReturn";
                field = "salesReturnNumber";
                sqltable = "salesreturn";
                sqlfield = "srnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_PURCHASERETURN:
                table = "PurchaseReturn";
                field = "purchaseReturnNumber";
                sqltable = "purchasereturn";
                sqlfield = "prnumber";
                pattern = format;
                break;
            case StaticValues.AUTONUM_CUSTOMER:
                table = "Customer";
                field = "acccode";
                sqltable = "customer";
                sqlfield = "acccode";
                pattern = format;
                break;
            case StaticValues.AUTONUM_VENDOR:
                table = "Vendor";
                field = "acccode";
                sqltable = "vendor";
                sqlfield = "acccode";
                pattern = format;
                break;
            case StaticValues.AUTONUM_BADDEBTINVOICECLAIM:
                table = "baddebtinvoicemapping";
                field = "invoice";
                sqltable = "baddebtinvoicemapping";
                sqlfield = "invoice";
                pattern = format;
                claim=true;
                break;
            case StaticValues.AUTONUM_BADDEBTINVOICERECOVER:
                table = "baddebtinvoicemapping";
                field = "invoice";
                sqltable = "baddebtinvoicemapping";
                sqlfield = "invoice";
                pattern = format;
                recover=true;
                break;
            case StaticValues.AUTONUM_BADDEBTPURCHASECLAIM:
                table = "BadDebtPurchaseInvoiceMapping";
                field = "goodsReceipt";
                sqltable = "baddebtpurchaseinvoicemapping";
                sqlfield = "goodsreceipt";
                pattern = format;
                claim=true;
                break;
            case StaticValues.AUTONUM_BADDEBTPURCHASERECOVER:
                table = "BadDebtPurchaseInvoiceMapping";
                field = "goodsReceipt";
                sqltable = "baddebtpurchaseinvoicemapping";
                sqlfield = "goodsreceipt";
                pattern = format;
                recover=true;
                break;
            case StaticValues.AUTONUM_BUILDASSEMBLY:
                table = "ProductBuild";
                sqltable = "productbuild";
                pattern = format;
                break;
            case StaticValues.AUTONUM_ASSETGROUP:
                table = "Product";
                sqltable = "product";
                pattern = format;
                break;
            case StaticValues.AUTONUM_Loan_Management:
                table = "Disbursement";
                sqltable = "disbursement";
                pattern = format;
                break;
            case StaticValues.AUTONUM_MACHINE_Management:
                table = "Machine";
                sqltable = "machine";
                pattern = format;
                break;
            case StaticValues.AUTONUM_LABOUR:
                table = "Labour";
                sqltable = "labour";
                pattern = format;
                break;
                case StaticValues.AUTONUM_MRPCONTRACT:
                table = "MRPContract";
                sqltable = "mrpcontract";
                pattern = format;
                break;
            case StaticValues.AUTONUM_MRP_JOBWORK:
                table = "JobWork";
                field = "jobordernumber";
                sqltable = "mrp_job_order";
                sqlfield = "jobordernumber";
                pattern = format;
                break;    
            case StaticValues.AUTONUM_MRP_WORKCENTRE:
                table = "WorkCentre";
                field = "workcenterid";
                sqltable = "workcenter";
                sqlfield = "workcenterid";
                pattern = format;
                break;    
            case StaticValues.AUTONUM_MRP_WORKORDER:
                table = "WorkOrder";
                field = "workOrderID";
                sqltable = "workorder";
                sqlfield = "workorderid";
                pattern = format;
                break;    
            case StaticValues.AUTONUM_MRP_ROUTECODE:
                table = "RoutingTemplate";
                sqltable = "routing_template";
                pattern = format;
                break;
            case StaticValues.AUTONUM_DEALER_EXCISE_RG23D_NUMBER:
                table = "DealerExciseDetails";
                sqltable = "dealerexcisedetails";
                pattern = format;
                break;
            case StaticValues.AUTONUM_DIMENSION:
                table = "FieldComboData";
                sqltable = "fieldcombodata";
                pattern = format;
                break;
        }

        if (StringUtil.isNullOrEmpty(pattern)) {
            seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, "");//complete number
            seqNumberMap.put(Constants.SEQNUMBER, "0");//interger part
            seqNumberMap.put(Constants.DATEPREFIX, "");
            seqNumberMap.put(Constants.DATESUFFIX, "");
            return seqNumberMap;
        }

        String query = "";
        List list = new ArrayList();

//                String sqlseqnumberfield = "seqnumber";
//                String sqlseqformatfield = "seqformat";
        String seqformatid = format;
        SequenceFormat seqFormat = (SequenceFormat) get(SequenceFormat.class, seqformatid);
        startfrom = seqFormat.getStartfrom();
        boolean datebeforePrefix=seqFormat.isDateBeforePrefix();
        boolean isdateafterPrefix=seqFormat.isDateAfterPrefix();
        boolean dateAfterSuffix=seqFormat.isShowDateFormatAfterSuffix();
        String selectedDateFormatAfterPrefix = StringUtil.isNullOrEmpty(seqFormat.getDateformatafterprefix())?"":seqFormat.getDateformatafterprefix();
        String selectedSuffixDate = StringUtil.isNullOrEmpty(seqFormat.getDateFormatAfterSuffix())?"":seqFormat.getDateFormatAfterSuffix();
        String prefix = seqFormat.getPrefix();
        String selecteddateformat = StringUtil.isNullOrEmpty(seqFormat.getDateformatinprefix())?"":seqFormat.getDateformatinprefix();
        String suffix = seqFormat.getSuffix();
        int numberofdigit = seqFormat.getNumberofdigit();
        boolean showleadingzero = seqFormat.isShowleadingzero();
        
        String datePrefix = "";
        String dateSuffix = "";
        String dateAfterPrefix = "";
        if (datebeforePrefix || dateAfterSuffix || isdateafterPrefix) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");  //ERP-8689     
            if(creationDate == null){
//                creationDate=getCurrentDateWithCompanyCreatorTimeZone(companyid);
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(creationDate);
            int year = cal.get(Calendar.YEAR);
            int yy = Math.abs(year) % 100; // Get YY value from year    
            DecimalFormat mFormat = new DecimalFormat("00");
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DAY_OF_MONTH);
            if (datebeforePrefix) {
                if (selecteddateformat.equalsIgnoreCase("YYYY")) {
                    datePrefix = "" + year;
                } else if (selecteddateformat.equalsIgnoreCase("YYYYMM")) {
                    datePrefix = "" + year + mFormat.format(month);
                } else if (selecteddateformat.equalsIgnoreCase("YY")) {
                    datePrefix = "" + mFormat.format(yy) ;
                } else if (selecteddateformat.equalsIgnoreCase("YYMM")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selecteddateformat.equalsIgnoreCase("YYMMDD")) {
                    datePrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } else { //for YYYYMMDD this will default case
                    datePrefix = sdf.format(creationDate);
                }
            }
            if (isdateafterPrefix) {
                if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYYY")) {
                    dateAfterPrefix = "" + year;
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYYYMM")) {
                    dateAfterPrefix = "" + year + mFormat.format(month);
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YY")) {
                    dateAfterPrefix = "" + mFormat.format(yy) ;
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYMM")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selectedDateFormatAfterPrefix.equalsIgnoreCase("YYMMDD")) {
                    dateAfterPrefix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } else { //for YYYYMMDD this will default case
                    dateAfterPrefix = sdf.format(creationDate);
                }
            }
            if (dateAfterSuffix) {
                if (selectedSuffixDate.equalsIgnoreCase("YYYY")) {
                    dateSuffix = "" + year;
                } else if (selectedSuffixDate.equalsIgnoreCase("YYYYMM")) {
                    dateSuffix = "" + year + mFormat.format(month);
                } else if (selectedSuffixDate.equalsIgnoreCase("YY")) {
                    dateSuffix = "" + mFormat.format(yy);
                } else if (selectedSuffixDate.equalsIgnoreCase("YYMM")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month);
                } else if (selectedSuffixDate.equalsIgnoreCase("YYMMDD")) {
                    dateSuffix = "" + mFormat.format(yy) + mFormat.format(month) + mFormat.format(day);
                } else { //for YYYYMMDD this will default case
                    dateSuffix = sdf.format(creationDate);
                }
            }
        }

      //logic to find maximum counter for the sequence format
        List paramslist = new ArrayList();
        paramslist.add(companyid);
        String condition = "";
        
        if (seqFormat.isResetCounter()) { //when reset option is selected/true 
            if (!StringUtil.isNullOrEmpty(datePrefix) && !StringUtil.isNullOrEmpty(dateSuffix) && !StringUtil.isNullOrEmpty(dateAfterPrefix)) { //when suffix and prefix both exist
                paramslist.add(dateSuffix);
                paramslist.add(datePrefix);
                paramslist.add(dateAfterPrefix);
                condition += " and datesuffixvalue = ? and datepreffixvalue = ? and dateafterpreffixvalue = ? ";
            } else if (!StringUtil.isNullOrEmpty(datePrefix)) { // when only prefix exist
                paramslist.add(datePrefix);
                condition += " and datepreffixvalue = ? ";
            } else if (!StringUtil.isNullOrEmpty(dateAfterPrefix)) { // when only date after prefix exist
                paramslist.add(dateAfterPrefix);
                condition += " and dateafterpreffixvalue = ? ";
            } else if (!StringUtil.isNullOrEmpty(dateSuffix)) { // when only suffix exist
                paramslist.add(dateSuffix);
                condition += " and datesuffixvalue = ? ";
            }
        }
        
        condition += " and seqnumber >= " + startfrom + " ";
        if (!StringUtil.equal(table, "Product")) {
            condition += " and autogen = 'T' ";
        }
        if (!StringUtil.isNullOrEmpty(seqformatid)) {
            condition += " and seqformat = ? ";
            paramslist.add(seqformatid);
        }
        if (claim) {
            condition += " and baddebttype=0";
        }
        if (recover) {
            condition += " and baddebttype=1";
        }
        if (from==StaticValues.AUTONUM_DIMENSION) {
            query = "select max(seqnumber) from " + sqltable + " where fieldid =  ? " + condition;
        } else {
            query = "select max(seqnumber) from " + sqltable + " where company =  ? " + condition;
        }
        list = executeSQLQuery(query, paramslist.toArray());
        int nextNumber = startfrom;
        if (!list.isEmpty()) {
            if (list.get(0) != null) {
                nextNumber = Integer.parseInt(list.get(0).toString()) + 1;
            }
        }
        String nextNumTemp = nextNumber + "";
        if (showleadingzero) {
            while (nextNumTemp.length() < numberofdigit) {
                nextNumTemp = "0" + nextNumTemp;
            }
        }

        //Building the complete number
        if (datebeforePrefix || dateAfterSuffix || isdateafterPrefix) {
            autoNumber = datePrefix + prefix + dateAfterPrefix + nextNumTemp + suffix + dateSuffix;
        } else {
            autoNumber = prefix + nextNumTemp + suffix;
        }
        seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, autoNumber);//complete number
        seqNumberMap.put(Constants.SEQNUMBER, nextNumTemp);//interger part 
        seqNumberMap.put(Constants.DATEPREFIX, datePrefix);
        seqNumberMap.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
        seqNumberMap.put(Constants.DATESUFFIX, dateSuffix);
        return seqNumberMap;
    }
    
    /**
     *
     * @param salesCommissionSchemaId
     * @param companyId
     * @return schemaMasterList
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getSalesCommissionSchema(String salesCommissionSchemaId, String companyId) throws ServiceException {
        List schemaMasterList = null;
        try {
            ArrayList params = new ArrayList();
            if (!StringUtil.isNullOrEmpty(salesCommissionSchemaId)) {
                params.add(salesCommissionSchemaId);
            }
            if (!StringUtil.isNullOrEmpty(companyId)) {
                params.add(companyId);
            }
            String query = "select schemamaster from salescommissionschemamaster scsm inner join fieldcombodata fcd on scsm.id = fcd.salescommissionschemamaster where fcd.salescommissionschemamaster = ? and scsm.company = ?";
            schemaMasterList = executeSQLQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getSalesCommissionSchema :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, schemaMasterList, 0);
    }
    /**
     * @Desc : Get ERP product batch using serials
     * @param productId
     * @param storeId
     * @param locationId
     * @param rowId
     * @param rackId
     * @param binId
     * @param batchName
     * @return
     * @throws ServiceException
     */
    public NewProductBatch getERPProductBatch(String productId, String storeId, String locationId, String rowId, String rackId, String binId, String batchName) throws ServiceException {
        if (StringUtil.isNullOrEmpty(batchName)) {
            batchName = "";
        }
        StringBuilder hql = new StringBuilder("FROM NewProductBatch WHERE product = ? AND warehouse.id = ? AND location.id = ? ");
        List params = new ArrayList();
        params.add(productId);
        params.add(storeId);
        params.add(locationId);
//        params.add(batchName);

        if (!StringUtil.isNullOrEmpty(batchName)) {
            hql.append(" AND batchname = ? ");
            params.add(batchName);
        } else {
            hql.append(" AND (batchname = '' OR batchname IS NULL )");
        }
        if (!StringUtil.isNullOrEmpty(rowId)) {
            hql.append(" AND row.id = ? ");
            params.add(rowId);
        } else {
            hql.append(" AND row IS NULL ");
        }
        if (!StringUtil.isNullOrEmpty(rackId)) {
            hql.append(" AND rack.id = ? ");
            params.add(rackId);
        } else {
            hql.append(" AND rack IS NULL ");
        }
        if (!StringUtil.isNullOrEmpty(binId)) {
            hql.append(" AND bin.id = ? ");
            params.add(binId);
        } else {
            hql.append(" AND bin IS NULL ");
        }
        List list = executeQueryPaging(hql.toString(), params.toArray(), new Paging(0, 1));
        NewProductBatch productBatch = null;
        if (!list.isEmpty()) {
            productBatch = (NewProductBatch) list.get(0);
        }
        return productBatch;
    }

    /**
     * @Desc : Get ERP batch serials using batch id
     * @param productId
     * @param productBatch
     * @param serialName
     * @param companyid
     * @return
     * @throws ServiceException
     */
    public NewBatchSerial getERPBatchSerial(String productId, NewProductBatch productBatch, String serialName, String companyid) throws ServiceException {

        NewBatchSerial serial = null;
        String hql = "FROM NewBatchSerial WHERE company.companyID = ? AND product = ? AND batch = ? AND serialname = ? and quantitydue > 0.0";
        List params = new ArrayList();
        params.add(companyid);
        params.add(productId);
        params.add(productBatch);
        params.add(serialName);
        List list = executeQueryPaging(hql, params.toArray(), new Paging(0, 1));
        if (!list.isEmpty()) {
            serial = (NewBatchSerial) list.get(0);
        }
        return serial;
    }
    
    public List checksEntryNumberForSequenceNumber(int moduleid, String entryNumber,String custom) throws ServiceException {
        List ll = new ArrayList();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("moduleid", moduleid);
            map.put("custom", custom);
            String formatid = "";
            String formatName = "";
            int intPartValue = 0;
            int intStartFromValue = 0;
            boolean isSeqnum = false;
            boolean isvalidEntryNumber = true;
            KwlReturnObject result = getSequenceFormat(map);
            List<SequenceFormat> formats = result.getEntityList();
            for (SequenceFormat format : formats) {
                String selecteddateformat = "";
                String selectedSuffixdateformat = "";
                boolean isDateBeforePrefix = false;
                boolean isDateAfterSuffix = false;
                formatName = format.getName() != null ? format.getName() : "";
                String preffix = format.getPrefix() != null ? format.getPrefix() : "";                
                String suffix = format.getSuffix() != null ? format.getSuffix() : "";
                preffix=preffix.toLowerCase();
                suffix=suffix.toLowerCase();
                isDateBeforePrefix=format.isDateBeforePrefix();
                if (isDateBeforePrefix) {
                    selecteddateformat = format.getDateformatinprefix() != null ? format.getDateformatinprefix() : "";
                }
                isDateAfterSuffix = format.isShowDateFormatAfterSuffix();
                if (isDateAfterSuffix) {
                    selectedSuffixdateformat = format.getDateFormatAfterSuffix() != null ? format.getDateFormatAfterSuffix() : "";
                }
                
                String lowerEntryNumber=entryNumber.toLowerCase(); 
          
                if ((isDateBeforePrefix || isDateAfterSuffix)) {//if sequnece format have date
                    if (lowerEntryNumber.length() == (selecteddateformat.length() + formatName.length() + selectedSuffixdateformat.length())) { //when lenght of number as well as lenght of format with date matches
                        if (isDateBeforePrefix) {
                            String datePrefix = lowerEntryNumber.substring(0, selecteddateformat.length());
                            DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                            if (selecteddateformat.equalsIgnoreCase("YYYY")) {
                                sdf = new SimpleDateFormat("yyyy");
                            } else if (selecteddateformat.equalsIgnoreCase("YYYYMM")) {
                                sdf = new SimpleDateFormat("yyyyMM");
                            }
                            try {
                                sdf.setLenient(false);//make date validation more strictly.
                                sdf.parse(datePrefix);//If datePrefix is sucessfully parsed it means it is datevalue otherwise this number will not generate from this sequence format so continue
                            } catch (Exception ex) {
                                continue;
                            }
                            //If date is valid date then checking for year. If it is less than current year then such entry number should be acceptable
                            // because sequence number does not generate preveious year number
                            Date prefixdate = sdf.parse(datePrefix);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(prefixdate);
                            
                            Calendar todayCal = Calendar.getInstance();
                            todayCal.setTime(new Date());
                            
                            if (cal.get(Calendar.YEAR) < todayCal.get(Calendar.YEAR)) {
                                continue;// continue because this number can not be generated sequence format
                            }
                        }
                        if (isDateAfterSuffix) {
                            String dateSuffix = lowerEntryNumber.substring((selecteddateformat.length() + formatName.length()), lowerEntryNumber.length());
                            DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                            if (selectedSuffixdateformat.equalsIgnoreCase("YYYY")) {
                                sdf = new SimpleDateFormat("yyyy");
                            } else if (selectedSuffixdateformat.equalsIgnoreCase("YYYYMM")) {
                                sdf = new SimpleDateFormat("yyyyMM");
                            }
                            try {
                                sdf.setLenient(false);//make date validation more strictly.
                                sdf.parse(dateSuffix);//If dateSuffix is sucessfully parsed it means it is datevalue otherwise entrynumber will not generate from this sequence format so continue
                            } catch (Exception ex) {
                                continue;
                            }
                            //If date is valid date then checking for year. If it is less than current year then such entry number should be acceptable
                            // because sequence number does not generate preveious year number
                            Date suffixdate = sdf.parse(dateSuffix);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(suffixdate);

                            Calendar todayCal = Calendar.getInstance();
                            todayCal.setTime(new Date());

                            if (cal.get(Calendar.YEAR) < todayCal.get(Calendar.YEAR)) {
                                continue;// continue because this number can not be generated sequence format
                            }
                        }
                        String lowerEntryNumberWithoutDate = lowerEntryNumber.substring(selecteddateformat.length(), (lowerEntryNumber.length() - selectedSuffixdateformat.length()));//removed prefix and suffix date
                        if (lowerEntryNumberWithoutDate.length() == formatName.length() && lowerEntryNumberWithoutDate.startsWith(preffix) && lowerEntryNumberWithoutDate.endsWith(suffix)) {
                            String intPart = lowerEntryNumberWithoutDate.substring(preffix.length(), (lowerEntryNumberWithoutDate.length() - suffix.length()));
                            try {
                                intPartValue = Integer.parseInt(intPart);
                            } catch (Exception ex) {
                                continue;
                            }
                            formatid = format.getID();
                            intStartFromValue=format.getStartfrom();
                            isSeqnum = true;
                            formatName = selecteddateformat + formatName + selectedSuffixdateformat;
                            break;//once the sequnce format found no need to chek for other sequnece format
                        }
                    }
                } else {
                    if (lowerEntryNumber.length() == formatName.length() && lowerEntryNumber.startsWith(preffix) && lowerEntryNumber.endsWith(suffix)) {
                        String intPart = entryNumber.substring(preffix.length(), (entryNumber.length() - suffix.length()));
                        try {
                            intPartValue = Integer.parseInt(intPart);
                        } catch (Exception ex) {
                            continue;
                        }
                        formatid = format.getID();
                        intStartFromValue=format.getStartfrom();
                        isSeqnum = true;
                        break;
                    }
                }
            }
            if (isSeqnum) {
                String sqltable = "fieldcombodata";
                
                int maxseqnum = intStartFromValue-1;// Initialize with start from number to check sequence no. can be generated from available sequence format or not.
                ArrayList params = new ArrayList();
                params.add(formatid);
                String query = "select max(seqnumber) from " + sqltable + " where seqformat = ?";
                List list = executeSQLQuery( query, params.toArray());
                if (!list.isEmpty() && list.get(0) != null) {
                    maxseqnum = Integer.parseInt(list.get(0).toString());
                }
                if (intPartValue > maxseqnum) {// user entered number can also be generated by sequence number
                    isvalidEntryNumber = false;
                }
            }
            ll.add(isvalidEntryNumber);
            ll.add(formatName);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCompanyPreferencesImpl.checksEntryNumberForSequenceNumber : " + ex.getMessage(), ex);
        }
        return ll;
    }
    
    public KwlReturnObject getSequenceFormat(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from SequenceFormat y where y.deleted=false ";

        if (filterParams.containsKey("companyid")) {
            condition += " and y.company.companyID=? ";
            params.add(filterParams.get("companyid"));
        }
        if (filterParams.containsKey("custom")) {
            condition += " and y.custom=? ";
            params.add(filterParams.get("custom"));
        }
        if (filterParams.containsKey("id")) {
            condition += " and y.ID=? ";
            params.add(filterParams.get("id"));
        }
        if (filterParams.containsKey("modulename")) {
            condition += " and y.modulename=? ";
            params.add(filterParams.get("modulename"));
        }
        if (filterParams.containsKey("moduleid")) {
            condition += " and y.moduleid=? ";
            params.add(filterParams.get("moduleid"));
        }
        if (filterParams.containsKey("name")) {
            condition += " and y.name=? ";
            params.add(filterParams.get("name"));
        }
        if (filterParams.containsKey("dateFormatinPrefix")) {
            condition += " and y.dateformatinprefix=? ";
            params.add(filterParams.get("dateFormatinPrefix"));
        }
        if (filterParams.containsKey("dateFormatAfterPrefix")) {
            condition += " and y.dateformatafterprefix=? ";
            params.add(filterParams.get("dateFormatAfterPrefix"));
        }
        if (filterParams.containsKey("selectedsuffixdateformat")) {
            condition += " and y.dateFormatAfterSuffix=? ";
            params.add(filterParams.get("selectedsuffixdateformat"));
        }
        if (filterParams.containsKey("isChecked")) {
            condition += " and y.isactivate=? ";
            params.add(filterParams.get("isChecked"));
        }
        if (filterParams.containsKey("isdefaultFormat")) {
            boolean defFormat = filterParams.get("isdefaultFormat") == null ? false : (Boolean) filterParams.get("isdefaultFormat");
            if (defFormat) {
                condition += "and y.isdefaultformat=? ";
                params.add(defFormat);
            }
        }
        if (filterParams.containsKey("masterid")) {
            condition += " and y.custom=? ";
            params.add(filterParams.get("masterid"));
        }
        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    /**
     * Desc : Add User GRP
     *
     * @param itemmap
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject addUsersGroup(Map<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            UsersGroup usersGroup = null;
            if (itemmap.containsKey("usergroup") && itemmap.get("usergroup") != null) {
                usersGroup = (UsersGroup) get(UsersGroup.class, (String) itemmap.get("usergroup"));
            } else {
                usersGroup = new UsersGroup();
            }

            if (itemmap.containsKey("groupname")) {
                usersGroup.setName((String) itemmap.get("groupname"));
            }
            if (itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                usersGroup.setCompany(company);
            }
            if (itemmap.containsKey("groupMappings")) {
                usersGroup.setUsersGroupMappings((Set<UsersGroupMapping>) itemmap.get("groupMappings"));
            }
            saveOrUpdate(usersGroup);
            list.add(usersGroup);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addUsersGroup :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Users Group has been added successfully.", null, list, list.size());
    }

    /**
     * Desc : Add User GRP Mapping
     *
     * @param itemmap
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject addUsersGroupMapping(Map<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            UsersGroupMapping usersGroupMapping = new UsersGroupMapping();
            if (itemmap.containsKey("usergroup")) {
                UsersGroup usersGroup = itemmap.get("usergroup") == null ? null : (UsersGroup) get(UsersGroup.class, (String) itemmap.get("usergroup"));
                usersGroupMapping.setUsersGroup(usersGroup);
            }
            if (itemmap.containsKey("userid")) {
                User user = itemmap.get("userid") == null ? null : (User) get(User.class, (String) itemmap.get("userid"));
                usersGroupMapping.setUser(user);
            }
            save(usersGroupMapping);
            list.add(usersGroupMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addUsersGroupMapping :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Users Group Mapping has been added successfully.", null, list, list.size());
    }

    /**
     * Desc : Add User GRP and FCD Mapping
     *
     * @param itemmap
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject addUsersGroupFCDMapping(Map<String, Object> itemmap) throws ServiceException {
        List list = new ArrayList();
        try {
            UserGroupFieldComboMapping userGroupFieldComboMapping = new UserGroupFieldComboMapping();
            if (itemmap.containsKey("userGroup")) {
                UsersGroup usersGroup = itemmap.get("userGroup") == null ? null : (UsersGroup) get(UsersGroup.class, (String) itemmap.get("userGroup"));
                userGroupFieldComboMapping.setUsersGroup(usersGroup);
            }
            if (itemmap.containsKey("fcdid")) {
                FieldComboData comboData = itemmap.get("fcdid") == null ? null : (FieldComboData) get(FieldComboData.class, (String) itemmap.get("fcdid"));
                userGroupFieldComboMapping.setFieldComboData(comboData);
            }
            if (itemmap.containsKey("companyid")) {
                Company company = itemmap.get("companyid") == null ? null : (Company) get(Company.class, (String) itemmap.get("companyid"));
                userGroupFieldComboMapping.setCompany(company);
            }
            if (itemmap.containsKey("colnum")) {
                int colnum = (int) itemmap.get("colnum");
                userGroupFieldComboMapping.setColnum(colnum);
            }
            if (itemmap.containsKey("moduleid")) {
                int colnum = (int) itemmap.get("moduleid");
                userGroupFieldComboMapping.setModuleid(colnum);
            }
            save(userGroupFieldComboMapping);
            list.add(userGroupFieldComboMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addUsersGroupFCDMapping :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Users Group FCD Mapping has been added successfully.", null, list, list.size());
    }

    /**
     * Desc : Get User GRP
     *
     * @param itemmap
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject getUsersGroup(Map<String, Object> itemmap) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            params.add((String) itemmap.get("companyid"));
            String query = "from UsersGroup where company.companyID=?";
            KwlReturnObject result;
            List list = executeQuery(query, params.toArray());
            result = new KwlReturnObject(true, null, null, list, list.size());
            return result;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getUsersGroup :" + ex.getMessage(), ex);
        }
    }

    /**
     * Desc : Delete User GRP Mapping
     *
     * @param itemmap
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject deleteUsersGroupMapping(Map<String, Object> itemmap) throws ServiceException {
        boolean successflag = false;
        String usergrp = (String) itemmap.get("usergroup");
        String delQuery = "delete from UsersGroupMapping ugm where ugm.usersGroup.ID=?";
        int numRows = executeUpdate(delQuery, new Object[]{usergrp});
        successflag = true;
        return new KwlReturnObject(successflag, "User Group Mapping has been deleted successfully.", null, null, 1);
    }

    /**
     * Desc : Delete Users GRP and FCD Mapping using UG
     *
     * @param itemmap
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject deleteUsersGroupFieldComboMapping(Map<String, Object> itemmap) throws ServiceException {
        boolean successflag = false;
        String usergrp = (String) itemmap.get("usergroup");
        String delQuery = "delete from UserGroupFieldComboMapping ufcd where ufcd.usersGroup.ID=?";
        int numRows = executeUpdate(delQuery, new Object[]{usergrp});
        successflag = true;
        return new KwlReturnObject(successflag, "User Group Mapping has been deleted successfully.", null, null, 1);
    }

    /**
     * Desc : Delete Users GRP and FCD Mapping using FCD Id
     *
     * @param itemmap
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject deleteUsersGroupFieldComboMappingUsingFCD(Map<String, Object> itemmap) throws ServiceException {
        boolean successflag = false;
        String masterItem = (String) itemmap.get("masterItem");
        String delQuery = "delete from UserGroupFieldComboMapping ufcd where ufcd.fieldComboData.id=?";
        int numRows = executeUpdate(delQuery, new Object[]{masterItem});
        successflag = true;
        return new KwlReturnObject(successflag, "User Group Mapping has been deleted successfully.", null, null, 1);
    }

    /**
     * Desc : Delete Users GRP
     *
     * @param itemmap
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject deleteUsersGroup(Map<String, Object> itemmap) throws ServiceException {
        boolean successflag = false;
        String usergrp = (String) itemmap.get("usergroup");
        String companyid = (String) itemmap.get("companyid");
        deleteUsersGroupMapping(itemmap);
        deleteUsersGroupFieldComboMapping(itemmap);
        String delQuery = "delete from UsersGroup ug where ug.ID=? and ug.company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{usergrp, companyid});
        successflag = true;
        return new KwlReturnObject(successflag, "User Group has been deleted successfully.", null, null, 1);
    }

    /**
     * @Desc : Get Field ID for Customer /Vendor. Product Master
     * @param map
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject getFieldIdForMaster(Map<String, Object> map) throws ServiceException {
        String fieldlable = (String) map.get("fieldlabel");
        String companyid = (String) map.get("companyid");
        String query = " select id from fieldparams where fieldlabel = ? and companyid = ? and customfield=0 ";
        List list = executeSQLQuery(query, new Object[]{fieldlable, companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /**
     * @Desc : Get Documents count attached to batch
     * @param dataMap
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject getBatchDocuments(HashMap<String, Object> dataMap) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            ArrayList params = new ArrayList();

            params.add((String) dataMap.get(Constants.companyKey));

            String conditionSQL = " where invoicedoccompmap.company=?";

            String invoiceId = (String) dataMap.get("invoiceID");
            if (dataMap.containsKey("invoiceID") && !StringUtil.isNullOrEmpty(invoiceId)) {
                params.add(invoiceId);
                conditionSQL += " and invoicedoccompmap.invoiceid=?";
            }

            String mysqlQuery = "select invoicedocuments.docname  as docname,invoicedocuments.doctypeid as doctypeid,invoicedocuments.docid as docid,invoicedocuments.id "
                    + "from invoicedoccompmap inner join invoicedocuments on invoicedoccompmap.documentid=invoicedocuments.id " + conditionSQL;

            list = executeSQLQuery( mysqlQuery, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accCustomerDAOImpl.getinvoiceDocuments:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    /**
     * Description:This method is used to Add OR Update Location with Warehouse imported from Import Assembly Functionality
     * @param HashMap<String, Object> requestParams
     * @return KwlReturnObject
     * @throws ServiceException
     */    
    public KwlReturnObject addUpdateLocation(HashMap<String, Object> map) throws ServiceException {
        List list = new ArrayList();
        String locid = "";
        Set<Store> stores = null;
        Location location = null;
        try {
            if (map.containsKey("locid") && map.get("locid") != null) {
                locid = (String) map.get("locid");
            }
            if (map.containsKey("stores") && map.get("stores") != null) {
                stores = (Set<Store>) map.get("stores");
            }
            location = (Location) get(Location.class, locid);
            location.setStores(stores);
            saveOrUpdate(location);
            list.add(location);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.addUpdateLocation :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Master item has been updated successfully.", null, list, list.size());
    }

    /**
     *  This method retrieves Address Field mapped with Dimension.       
     */     
    public KwlReturnObject getAddressMappingForDimension(com.krawler.utils.json.base.JSONObject reqParams) throws JSONException, ServiceException{
        String dimension = (String) reqParams.optString("dimension");
        String companyid = (String) reqParams.optString("companyid");
        ArrayList params = new ArrayList();
            params.add(dimension);
            params.add(companyid);
            String query = "from AddressFieldDimensionMapping where dimension.id=? and company.companyID=?";
            List list = executeQuery(query, params.toArray());
            //List list = executeSQLQuery(query, new Object[]{dimension,companyid});
            return new KwlReturnObject(true, "Address field fetched successfully.", null, list, list.size());
    }    

    /**
     *  This method retrieves FieldParams id for "MultiEntityDimension" module.       
     */
    public KwlReturnObject getFieldParamsForDimension(com.krawler.utils.json.base.JSONObject reqParams) throws JSONException, ServiceException{
    ArrayList params = new ArrayList();
    FieldParams fieldParams=null;
    String fieldlabel="";
    String dimension = (String) reqParams.optString("dimension");   
    fieldParams = (FieldParams) get(FieldParams.class, (String) dimension);
    fieldlabel=fieldParams.getFieldlabel();
    params.add(fieldlabel);
    params.add(Constants.GSTModule);
    params.add(reqParams.optString("companyid"));
    String query = "from FieldParams where fieldlabel=? and moduleid=? and companyid=?";
    List list = executeQuery(query, params.toArray());
    return new KwlReturnObject(true, "Address field fetched successfully.", null, list, list.size());    
    }  
    
     /**
     * Description:This method is used to Get Discount for product
     * @param HashMap<String, Object> requestParams
     * @return KwlReturnObject
     * @throws ServiceException
     */
    public KwlReturnObject getDiscountOfProductForPricingBand(JSONObject requestParamsJson) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String productId = "";
        String pricingBandMasterId = "";
        String companyid = "";
        String conditionSQL = "";
        String currencyId = "";
        boolean isFromSave=false;
        try {
            if (requestParamsJson.has("companyid") && requestParamsJson.get("companyid") != null) {
                companyid = requestParamsJson.getString("companyid");
            }
            params.add(companyid);
            
            if (requestParamsJson.has("productId") && requestParamsJson.get("productId") != null) {
                productId = requestParamsJson.getString("productId");
                params.add(productId);
                conditionSQL += " and productid = ? ";
            }
            
            if (requestParamsJson.has("pricingBandMasterId") && requestParamsJson.get("pricingBandMasterId") != null) {
                pricingBandMasterId = requestParamsJson.getString("pricingBandMasterId");
                params.add(pricingBandMasterId);
                conditionSQL += " and pricingbandmasterid = ? ";
            }
            
            if (requestParamsJson.has("isFromSave") && requestParamsJson.get("isFromSave") != null) {
                isFromSave = requestParamsJson.getBoolean("isFromSave");
            }
            
            if (requestParamsJson.has("currencyId") && requestParamsJson.get("currencyId") != null) {
                currencyId = requestParamsJson.optString("currencyId", "");
                conditionSQL += " and currencyid = ? ";
                params.add(currencyId);
            }
            
            if (requestParamsJson.has("applicableDate") && requestParamsJson.get("applicableDate") != null && isFromSave) {
                params.add((Date) requestParamsJson.get("applicableDate"));
                conditionSQL += " and applicabledate = ? ";
            }else if (requestParamsJson.has("applicableDate") && requestParamsJson.get("applicableDate") != null) {
                conditionSQL += " and applicabledate in (select max(applicabledate) as ld from productdiscountmapping where companyid = ? and pricingbandmasterid = ? and productid = ? and currencyid = ? and applicabledate <= ?) ";
                params.add(companyid);
                params.add(pricingBandMasterId);
                params.add(productId);
                params.add(currencyId);
                params.add((Date) requestParamsJson.get("applicableDate"));                    
            }
            
            String query = " select discountmasterid from productdiscountmapping where companyid = ? " + conditionSQL;
            list = executeSQLQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getDiscountOfProductForPricingBand :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    /**
     * setProductDiscountMapping method inserts entry into productdiscountmapping table when user maps discount to a specific product on price band screen
     * @param paramObj
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject setProductDiscountMapping(JSONObject paramObj) throws ServiceException {
        List list = new ArrayList();
        boolean isSuccess=false;
        try {
            String query = "insert into productdiscountmapping (id, productid, pricingbandmasterid, companyid, discountmasterid, applicabledate, currencyid) values (?,?,?,?,?,?,?)";
            String companyid = paramObj.optString("companyid", "");
            String productid = paramObj.optString("productId", "");
            String pricingBandMasterId = paramObj.optString("pricingBandMasterId", "");
            String discountMasterId = paramObj.optString("discountMasterId", "");
            if(paramObj.optBoolean("isDeleted",false)){
                discountMasterId=null;
            }
            Date applicableDate = (Date) paramObj.get("applicableDate");
            String currencyId = paramObj.optString("currencyId", "");
            String uuid = UUID.randomUUID().toString();
            executeSQLUpdate(query, new Object[]{uuid, productid, pricingBandMasterId, companyid, discountMasterId, applicableDate, currencyId});
            isSuccess=true;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.setProductDiscountMapping :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(isSuccess, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject deleteProductDiscountMapping(JSONObject paramObj) throws ServiceException {
        List list = new ArrayList();
        boolean isSuccess=false;
        try {
            String query = "DELETE from productdiscountmapping where companyid=? and productid=? and pricingbandmasterid=? and applicabledate=? and currencyid=? ";
            String companyid = paramObj.optString("companyid", "");
            String productid = paramObj.optString("productId", "");
            String pricingBandMasterId = paramObj.optString("pricingBandMasterId", "");
            String discountMasterId = paramObj.optString("discountMasterId", "");
            String currencyId = paramObj.optString("currencyId", "");
            Date applicableDate = (Date) paramObj.get("applicableDate");
            executeSQLUpdate(query, new Object[]{companyid, productid, pricingBandMasterId, applicableDate, currencyId});
            isSuccess=true;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteProductDiscountMapping :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(isSuccess, "", null, list, list.size());
    }
    /**
     * delete product discount mapping details
     * @param productId
     * @param companyId
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject deleteProductDiscountMapping(String productId, String companyId) throws ServiceException {
        List list = new ArrayList();
        boolean isSuccess=false;
        try {
            String query = "DELETE from productdiscountmapping where companyid=? and productid=?";
            int rowCnt = executeSQLUpdate(query, new Object[]{companyId, productId});
            isSuccess=true;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.deleteProductDiscountMapping :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(isSuccess, "", null, list, list.size());
    }
    
    /**
     * Get NewBatchSerial IDs of the product in a Pick Pack Ship DO.
     * @param dodetailid,productid
     * @return KwlReturnObject
     * @throws ServiceException
     **/
    public KwlReturnObject getserialsforPickPackShipDO(String dodetailid, String productid) throws ServiceException {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        Map<String, Object> batchmap = new HashMap<>();
        String serialnames = "";
        String batchname = "";
        String warehouse = "";
        String location = "";
        StringBuilder sqlquery = new StringBuilder("");

        /**
         * Getting the issued serial names which are comma separated and may
         * contain same or different batches.
         */
        try {
            sqlquery = new StringBuilder("SELECT istd.issued_serialnames,inst.tostore,istd.delivered_location, istd.batchname FROM in_interstoretransfer inst "
                    + " INNER JOIN in_ist_detail istd ON inst.id = istd.istrequest "
                    + " LEFT JOIN dodistmapping dodist ON istd.istrequest = dodist.ist "
                    + " WHERE dodist.dod=?");
            params.add(dodetailid);
            list = executeSQLQuery(sqlquery.toString(), params.toArray());
            int listindex = 0;
            /**
             * If the list size is greater than 1 then there are two different
             * batches or warehouses or locations for the same product Prepare
             * comma separated fields depending on this and pass it to the next
             * query below.
             */
            if (list.size() > 1) {
                while (listindex < list.size()) {
                    Object[] obj = (Object[]) list.get(listindex);
                    if (listindex > 0 && (!StringUtil.isNullOrEmpty(batchname) && !batchname.equals(obj[3].toString()))) {
                        batchname = batchname + ',' + (obj[3] != null ? obj[3].toString() : "");
                    } else {
                        batchname = obj[3] != null ? obj[3].toString() : "";
                    }
                    if (listindex > 0 && (!StringUtil.isNullOrEmpty(location) && !location.equals(obj[2].toString()))) {
                        location = location + ',' + (obj[2] != null ? obj[2].toString() : "");
                    } else {
                        location = obj[2] != null ? obj[2].toString() : "";
                    }
                    if (listindex > 0 && (!StringUtil.isNullOrEmpty(warehouse) && !warehouse.equals(obj[1].toString()))) {
                        warehouse = warehouse + ',' + (obj[1] != null ? obj[1].toString() : "");
                    } else {
                        warehouse = obj[1] != null ? obj[1].toString() : "";
                    }
                    if (listindex > 0 && (!StringUtil.isNullOrEmpty(serialnames) && !serialnames.equals(obj[0].toString()))) {
                        serialnames = serialnames + ',' + (obj[0] != null ? obj[0].toString() : "");
                    } else {
                        serialnames = obj[0] != null ? obj[0].toString() : "";
                    }
                    listindex++;
                }
            } else {//If there are no multiple batches then list size will be 1 directly save these details and pass to the query below
                Object[] obj = (Object[]) list.get(0);
                serialnames = obj[0] != null ? obj[0].toString() : "";
                warehouse = obj[1] != null ? obj[1].toString() : "";
                location = obj[2] != null ? obj[2].toString() : "";
                batchname = obj[3] != null ? obj[3].toString() : "";
            }

            /**
             * Replace commas (,) with  (',') to use them in the IN('') . 
             */
              
            
                if (!StringUtil.isNullOrEmpty(serialnames)) {
                    serialnames = serialnames.replaceAll(",", "','");
                }
                if (!StringUtil.isNullOrEmpty(warehouse)) {
                    warehouse = warehouse.replaceAll(",", "','");
                }
                if (!StringUtil.isNullOrEmpty(location)) {
                    location = location.replaceAll(",", "','");
                }
                if (!StringUtil.isNullOrEmpty(batchname)) {
                    batchname = batchname.replaceAll("'", "''");
                    batchname = batchname.replace("\\", "\\\\");
                    batchname = batchname.replaceAll(",", "','");
                }
            
            /**
             * Based on the comma separated serials in the previous query now we
             * get the newbatchserial ids of each of these serials.
             */
            params = new ArrayList();
            sqlquery = new StringBuilder(" SELECT nbs.id FROM newbatchserial nbs LEFT JOIN newproductbatch npb on nbs.batch = npb.id where npb.product = ? "
                    + "  AND nbs.quantitydue=1");
            params.add(productid);
            sqlquery.append(" AND batchname IN ('").append(batchname).append("')");
            sqlquery.append(" AND warehouse IN ('").append(warehouse).append("')");
            sqlquery.append(" AND location IN ('").append(location).append("')");
            sqlquery.append(" AND serialname IN ('").append(serialnames).append("')");
            list = executeSQLQuery(sqlquery.toString(), params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accMasterItemsImpl.getserialsforPickPackShipDo :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, Product, Product, list, list.size());
    }
    
    @Override
     public List<NewBatchSerial> getExcludeSerials(com.krawler.utils.json.base.JSONObject reqParams) throws JSONException, ServiceException{
         
                  
         String documentid = (String) reqParams.optString("documentid");
         String docrowid = (String) reqParams.optString("docrowid");
         String batchid = (String) reqParams.optString("batchId");
         String companyid = (String) reqParams.optString(Constants.companyid);
         boolean isFromSI = (boolean) reqParams.optBoolean("isFromSI",false);
         String selQuery ="";
        List params = new ArrayList();
        List list1=null;
         
        params.add(documentid);
        params.add(companyid);
        
//         String selQuery = " SELECT serialid FROM serialdocumentmapping WHERE documentid IN (SELECT dod.id FROM  invoicedetails  invd "
//                 + " inner JOIN dodetails dod ON dod.cidetails=invd.id "
//                 + " WHERE salesorderdetail=? AND invd.id  <> ? ) ";
        if (!isFromSI) {
            selQuery = " SELECT serialid FROM serialdocumentmapping WHERE documentid IN ( SELECT sod.id FROM  sodetails  sod "
                    + " INNER JOIN dodetails dod ON dod.sodetails=sod.id WHERE dod.id= ? AND dod.company= ? )";
        } else {
            params.add(companyid);
            selQuery = " SELECT serialid FROM serialdocumentmapping WHERE documentid IN ( "
                    + " SELECT sod.id from sodetails sod INNER JOIN ( "
                    + " SELECT dod.id,invd.salesorderdetail FROM  invoicedetails  invd "
                    + " INNER JOIN dodetails dod ON dod.cidetails=invd.id  WHERE dod.id = ? AND dod.company= ? )  AS t ON t.salesorderdetail= sod.id WHERE sod.company= ? ) ";
        }
         
          List list = executeSQLQuery(selQuery,params.toArray());
        
          Iterator itr=list.iterator();
        
          String seialIds="";
         
          if(list!=null && !list.isEmpty() && list.size()>0){
         
              while(itr.hasNext()){
                  String seriId=(String)itr.next();
                  if(!StringUtil.isNullOrEmpty(seialIds)){
                      seialIds += ",'"+seriId+"'";
                  }else{
                      seialIds="'"+seriId+"'";
                  }
              }
          }
            params = new ArrayList();
//            params.add(seialIds);
          
            String query = "from NewBatchSerial where id in ("+seialIds+") ";
            
          if (!StringUtil.isNullOrEmpty(batchid)) {
            params.add(batchid);
            query += " and batch.id=? ";
        }
          if(!StringUtil.isNullOrEmpty(seialIds)){
          list1 = executeQuery(query, params.toArray());
          }
        //List list = executeSQLQuery(query, new Object[]{dimension,companyid});
        return list1;
    } 
     public KwlReturnObject getChallanForJobWorkAssembly(JSONObject requestParams) throws ServiceException {
      //<editor-fold defaultstate="collapsed" desc="method used to get bomcode wise batch for product ">
         String sqlquery = "";
        List params = new ArrayList();

        String productid = (String) requestParams.optString("productid");
        String warehouseid = (String) requestParams.optString("warehouseid");
        String locationid = (String) requestParams.optString("locationid");
        String companyid = (String) requestParams.optString("companyid");
        String bomid = (String) requestParams.optString("bomid");
        
        sqlquery = " SELECT npb.id FROM productbuild pb "
                 + " INNER JOIN in_stockmovement sm ON (pb.id=sm.modulerefid AND pb.product=sm.product)"
                 + " INNER JOIN in_sm_detail smd ON smd.stockmovement=sm.id "
                 + " INNER JOIN newproductbatch npb ON (npb.product=sm.product and smd.location=npb.location and smd.batchname=npb.batchname and sm.store=npb.warehouse)"
                 + " WHERE pb.company=? AND pb.product=? AND pb.bomdetail=? AND isbuild='T' AND (pb.ispendingforqa='F' OR pb.approvedquantity>0) AND sm.store=? AND smd.location=? AND npb.quantitydue<>0 "
                 + " GROUP BY npb.id";
        
        params.add(companyid);
        params.add(productid);
        params.add(bomid);
        params.add(warehouseid);
        params.add(locationid);
        
        List  list = executeSQLQuery(sqlquery, params.toArray());
        
        String batchid = "";
        
        for ( int i=0 ; i<list.size() ; i++) {
            batchid +=  "'"+ list.get(i) +"'" 
                    + ",";
}
        String batchids = batchid.substring(0,batchid.length()-1);
        String hql = " from NewProductBatch WHERE id in (" + batchids + ")";
        list = executeQuery(hql);
        return new KwlReturnObject(true, "Fetch Batch Serial List.", "", list, list.size());
    //</editor-fold>
        
     }
     
          public double getBOMRemainingQuantityForJobWorkAssembly(String productid, String companyid, String warehouseid, String locationid, String batch, String bomid) throws ServiceException {
      //<editor-fold defaultstate="collapsed" desc="method used to get bomcode wise available quantity for product ">
         String sqlquery = "";
        List params = new ArrayList();
        double quantity = 0.0;
        
        /* To get stock added through build assembly for selected batch and bomcode.*/
        
        sqlquery = " SELECT sum(case when cin='2' then -qty else qty end) as quantity from ( " 
                 + " SELECT smd.quantity as qty, sm.transaction_type as cin FROM productbuild pb "
                 + " INNER JOIN in_stockmovement sm ON (pb.id=sm.modulerefid and pb.product=sm.product) "
                 + " INNER JOIN in_sm_detail smd ON smd.stockmovement=sm.id "
                 + " WHERE pb.company=? AND pb.product=? AND pb.bomdetail=? AND pb.isbuild='T' AND (pb.ispendingforqa='F' OR pb.approvedquantity>0) AND sm.store=? AND smd.location=? AND smd.batchname=? ";
        params.add(companyid);
        params.add(productid);
        params.add(bomid);
        params.add(warehouseid);
        params.add(locationid);
        params.add(batch);
        
                // To get stock removed through Delivery Order for selected batch and bomcode 
        
        sqlquery += " UNION "       
                 + " SELECT smd.quantity as qty, sm.transaction_type as cin FROM dodetails dod "
                 + " INNER JOIN in_stockmovement sm ON dod.id=sm.modulerefdetailid "
                 + " INNER JOIN in_sm_detail smd ON smd.stockmovement=sm.id "
                 + " WHERE dod.company=? AND dod.product=? AND dod.bomcode=? AND sm.store=? AND smd.location=? AND smd.batchname=? ";
        
        sqlquery +=  " ) as invent";
        
        params.add(companyid);
        params.add(productid);
        params.add(bomid);
        params.add(warehouseid);
        params.add(locationid);
        params.add(batch);
        
        
        List  list = executeSQLQuery(sqlquery, params.toArray());
        
        if (list.size() > 0) {
                if (!StringUtil.isNullObject(list.get(0))) {
                    quantity = Double.parseDouble(list.get(0).toString());
                }
            }

        return quantity;
    //</editor-fold>
        
     }
          
     public KwlReturnObject checkVendorCategoryUsedInAnyTransaction(String vendorCategoryId) throws ServiceException, AccountingException {
        KwlReturnObject result;
        ArrayList params = new ArrayList();
        params.add(vendorCategoryId);
        String query = "SELECT id from vendorcategorymapping WHERE vendorcategory = ? ";
        List list = executeSQLQuery(query, params.toArray());
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already mapped with Vendor(s).");
        }
        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }
     
    public KwlReturnObject checkCustomerCategoryUsedInAnyTransaction(String customerCategoryId) throws ServiceException, AccountingException {
        KwlReturnObject result;
        ArrayList params = new ArrayList();
        params.add(customerCategoryId);
        String query = "SELECT id from customercategorymapping WHERE customercategory = ? ";
        List list = executeSQLQuery(query, params.toArray());
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already mapped with Customer(s).");
        }
        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }
    
    @Override
    public KwlReturnObject checkTaxTypeUsedInAnyTransaction(HashMap<String, Object> requestParams) throws ServiceException, AccountingException {
        KwlReturnObject result;
        ArrayList params = new ArrayList();

        String query = "select v.id from vendor v where v.company=? and v.gstvendortype = ? "
                + " UNION "
                + " select c.id from customer c where c.company = ? and c.gstcustomertype = ? ";
        
        params.add(requestParams.get(Constants.companyKey));
        params.add(requestParams.get(Constants.TAXTYPE));
        params.add(requestParams.get(Constants.companyKey));
        params.add(requestParams.get(Constants.TAXTYPE));

        List list = executeSQLQuery(query, params.toArray());
        if (list != null && !list.isEmpty()) {
            throw new AccountingException("You cannot delete selected master item as it is already mapped with Vendor(s)/Customer(s).");
        }
        result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }
}
