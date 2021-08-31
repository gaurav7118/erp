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
package com.krawler.spring.accounting.costCenter;

import com.krawler.common.admin.ApprovalRules;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.CostCenter;
import com.krawler.common.admin.SalesCommission;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.common.KwlReturnObject;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AccCostCenterImpl extends BaseDAO implements AccCostCenterDAO {
  
    public KwlReturnObject getCostCenter(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from CostCenter";
        return buildNExecuteQuery(query, requestParams);
    }

    public Object saveCostCenter(HashMap<String, Object> dataMap) throws ServiceException {
        try {
            return setterMethod(dataMap, CostCenter.class.getName(), "ID");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DataInvalidateException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public KwlReturnObject deleteCostCenter(String ccid, String companyid) throws ServiceException {
        int numRows = 0;
        String msg = "";
        boolean isSuccess = false;
        try {
            String delQuery = "delete from CostCenter cc where cc.ID=? and cc.company.companyID=?";
            numRows = executeUpdate(delQuery, new Object[]{ccid, companyid});
            isSuccess = true;
            msg = "Cost Center has been deleted successfully.";
        } catch (Exception ex) {
            isSuccess = false;
            msg = "Problem Occured while deleting Cost center.";
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(isSuccess, msg, null, null, numRows);
    }
    
    public KwlReturnObject checkUniqueCostCenter(String id, String ccid, String name, String companyid) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from CostCenter where company.companyID=? and (ccid=? or name=?)";
        params.add(companyid);
        params.add(ccid);
        params.add(name);

        if (!StringUtil.isNullOrEmpty(id)) {
            condition = " and id!=?";
            params.add(id);
        }
        returnList = executeQuery(query + condition, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    @Override
    public Object saveSalesCommission(HashMap<String, Object> dataMap) throws ServiceException {
        try {
            return setterMethod(dataMap, SalesCommission.class.getName(), "ID");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (DataInvalidateException ex) {
            Logger.getLogger(AccCostCenterImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public KwlReturnObject saveApprovalRules(HashMap<String, Object> dataMap) throws ServiceException {

        List list = new ArrayList();
        ApprovalRules approvalRules = new ApprovalRules();
        try {
//           if (dataMap.containsKey("ID")) {                 
//                    approvalRules = (ApprovalRules) get(ApprovalRules.class, (String) dataMap.get("ID"));
//                 }
//            }
            if (dataMap.containsKey("ID")) {
                approvalRules.setID((String) dataMap.get("ID"));
            }
            if (dataMap.containsKey("RuleName")) {
                approvalRules.setRuleName((String) dataMap.get("RuleName"));
            }
            if (dataMap.containsKey("Typeid")) {
                approvalRules.setTypeid((String) dataMap.get("Typeid"));
            }
            if (dataMap.containsKey("FieldType")) {
                approvalRules.setFieldType((String) dataMap.get("FieldType"));
            }
            if (dataMap.containsKey("Value")) {
                approvalRules.setValue((String) dataMap.get("Value"));
            }

            if (dataMap.containsKey("approvallevel")) {
                approvalRules.setApprovallevel((Integer) dataMap.get("approvallevel"));
            }

            if (dataMap.containsKey("discount")) {
                approvalRules.setDiscountamount(Double.parseDouble((String) dataMap.get("discount")));
            }

            if (dataMap.containsKey("Company")) {
                Company company = dataMap.get("Company") == null ? null : (Company) get(Company.class, (String) dataMap.get("Company"));
                approvalRules.setCompany(company);
            }
//            if (hm.containsKey(AUTOGENERATED)) {
//                receipt.setAutoGenerated((Boolean) hm.get(AUTOGENERATED));
//            }            
            save(approvalRules);
            list.add(approvalRules);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCostCenterImpl.saveApprovalRules : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Approval rule has been added successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject editApprovalRules(HashMap<String, Object> dataMap) throws ServiceException {

        List list = new ArrayList();
//        ApprovalRules approvalRules = new ApprovalRules();
        try {
//           if (dataMap.containsKey("ID")) {                 
//                    approvalRules = (ApprovalRules) get(ApprovalRules.class, (String) dataMap.get("ID"));
//                 }
//            }
            String id = (String) dataMap.get("ID");
            ApprovalRules approvalRules = (ApprovalRules) get(ApprovalRules.class, id);
//            if (dataMap.containsKey("ID")) {
//                approvalRules.setID((String) dataMap.get("ID"));
//            }
            if (dataMap.containsKey("RuleName")) {
                approvalRules.setRuleName((String) dataMap.get("RuleName"));
            }
            if (dataMap.containsKey("Typeid")) {
                approvalRules.setTypeid((String) dataMap.get("Typeid"));
            }
            if (dataMap.containsKey("FieldType")) {
                approvalRules.setFieldType((String) dataMap.get("FieldType"));
            }
            if (dataMap.containsKey("Value")) {
                approvalRules.setValue((String) dataMap.get("Value"));
            }
            if (dataMap.containsKey("discount")) {
                approvalRules.setDiscountamount(Double.parseDouble((String) dataMap.get("discount")));
            }
            if (dataMap.containsKey("approvallevel")) {
                approvalRules.setApprovallevel((Integer) dataMap.get("approvallevel"));
            }
//            if (dataMap.containsKey("Company")) {
//                Company company = dataMap.get("Company")==null?null:(Company) get(Company.class, (String) dataMap.get("Company"));
//                approvalRules.setCompany(company);
//            }
//            if (hm.containsKey(AUTOGENERATED)) {
//                receipt.setAutoGenerated((Boolean) hm.get(AUTOGENERATED));
//            }            
            saveOrUpdate(approvalRules);
            list.add(approvalRules);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccCostCenterImpl.saveApprovalRules : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Approval rule has been edited successfully", null, list, list.size());
    }

    public KwlReturnObject deleteApprovalRules(String companyid, String ruleid) throws ServiceException {
        String delQuery = "delete from ApprovalRules ar where ar.company.companyID=? and id=?";
        int numRows = executeUpdate(delQuery, new Object[]{companyid, ruleid});
        return new KwlReturnObject(true, "Sales Commission has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteSalesCommission(String companyid) throws ServiceException {
        String delQuery = "delete from SalesCommission sc where sc.company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{companyid});
        return new KwlReturnObject(true, "Sales Commission has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getSalesCommission(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from SalesCommission";
        return buildNExecuteQuery(query, requestParams);
    }

    public KwlReturnObject getApprovalRules(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from ApprovalRules";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public List getProductName(String productid) throws ServiceException {

        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        //String condition = "where deleted=false";
        String query = " select name from product where id= ? ";
        params.add(productid);
        returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    
    @Override
    public KwlReturnObject getProductCategoryNameByID(String productCategoryId, String companyId) throws ServiceException {
        KwlReturnObject result = null;
        if (!StringUtil.isNullOrEmpty(companyId) && !StringUtil.isNullOrEmpty(productCategoryId)) {
            ArrayList params = new ArrayList();
            params.add(productCategoryId);
            params.add(companyId);
            String query = "select value from MasterItem where id=? and company.companyID=? ";
            List list = executeQuery(query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
}
        return result;
    }
    
    @Override
    public List getCustomerNameByID(HashMap<String, Object> dataMap) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String module = (String) dataMap.get("module");
        String query = "select cstr.name,cstr.acccode from "+module+" cstr where cstr.id=? and cstr.company=?";
        String customerID = (String) dataMap.get("customerID");
        String companyid = (String) dataMap.get("companyid");
        params.add(customerID);
        params.add(companyid);
        returnList = executeSQLQuery( query, params.toArray());;
        return returnList;
    }

    @Override
    public List getPaymentTermNameByID(String termid,String companyid) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String query = "select termname from creditterm where termid= ? and company=? ";
        params.add(termid);
        params.add(companyid);
        returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
    
    @Override
    public List getAccountNameByID(String companyId, String accid) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String query = "select name from account where company=? and id= ?";
        params.add(companyId);
        params.add(accid);
        returnList = executeSQLQuery(query, params.toArray());
        return returnList;
    }
}
