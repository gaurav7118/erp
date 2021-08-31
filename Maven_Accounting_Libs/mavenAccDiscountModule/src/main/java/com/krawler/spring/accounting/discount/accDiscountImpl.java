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
package com.krawler.spring.accounting.discount;

import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Discount;
import com.krawler.hql.accounting.DiscountMaster;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class accDiscountImpl extends BaseDAO implements accDiscountDAO {

    private AccountingHandlerDAO accountingHandlerDAOobj;

    public AccountingHandlerDAO getAccountingHandlerDAOobj() {
        return accountingHandlerDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public KwlReturnObject deleteDiscount(String discountid, String companyid) throws ServiceException {
        String delQuery = "delete from Discount where ID=? and company.companyID=?";
        int numRows = executeUpdate(delQuery, new Object[]{discountid, companyid});

        return new KwlReturnObject(true, "Discount has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteDiscountEntry(String discountid, String companyid) throws ServiceException {
        String query = "update Discount set deleted=true where ID=? and company.companyID=?";
        int numRows = executeUpdate(query, new Object[]{discountid, companyid});
        return new KwlReturnObject(true, "Discount Entry has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject addDiscount(JSONObject json) throws ServiceException {
        List list = new ArrayList();
        Discount discount = new Discount();
        try {
            if (json.has("discountid")) {
                discount = (Discount) get(Discount.class, json.getString("jeid"));
            }
            if (json.has("discount")) {
                discount.setDiscount(json.getDouble("discount"));
            }
            if (json.has("inpercent")) {
                discount.setInPercent(json.getBoolean("inpercent"));
            }
            if (json.has("originalamount")) {
                discount.setOriginalAmount(json.getDouble("originalamount"));
            }
            if (json.has("companyid")) {
                discount.setCompany((Company) get(Company.class, json.getString("companyid")));
            }
            if (json.has("amountinInvCurrency")) {
                discount.setAmountinInvCurrency(json.getDouble("amountinInvCurrency"));
            }
            if(json.has("typeOfFigure")){
                discount.setTypeOfFigure(json.optInt("typeOfFigure",1));
            }
            if(json.has("typeFigure")){
                discount.setTypeFigure(json.optDouble("typeFigure",0.0));
            }
            save(discount);
            list.add(discount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDiscountImpl.addDiscount : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateDiscount(Map requestParam) throws ServiceException {
        List list = new ArrayList();
        Discount discount = null;
        try {
            String discountid = (String) requestParam.get("discountid");
            if (StringUtil.isNullOrEmpty(discountid)) {
                discount = new Discount();
            } else {
                discount = (Discount) get(Discount.class, discountid);
            }

            if (requestParam.containsKey("discount")) {
                discount.setDiscount((Double) requestParam.get("discount"));
            }
            if (requestParam.containsKey("inpercent")) {
                discount.setInPercent((Boolean) requestParam.get("inpercent"));
            }
            if (requestParam.containsKey("originalamount")) {
                discount.setOriginalAmount((Double) requestParam.get("originalamount"));
            }
            if (requestParam.containsKey("companyid")) {
                discount.setCompany((Company) get(Company.class, (String) requestParam.get("companyid")));
            }
            if (requestParam.containsKey("amountinInvCurrency")) {
                discount.setAmountinInvCurrency((Double) requestParam.get("amountinInvCurrency"));
            }
            save(discount);
            list.add(discount);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDiscountImpl.addDiscount : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    /**
     * Method to get the list of all discount master
     * @param requestParam
     * @return KwlReturnObject
     * @throws ServiceException 
     */
    public KwlReturnObject getDiscountMaster(Map requestParam) throws ServiceException {
        List list = new ArrayList();
        StringBuilder condition = new StringBuilder();
        ArrayList params = new ArrayList();
        String query = " from DiscountMaster dm ";
        int count=0;
        try {
            String start = "";
            String limit = "";
            if (requestParam.containsKey(Constants.companyKey) && !StringUtil.isNullObject(requestParam.get(Constants.companyKey))) {
                condition.append(" where dm.company.companyID=? ");
                params.add(requestParam.get(Constants.companyKey).toString());
            }
            if (requestParam.containsKey(Constants.start) && requestParam.get(Constants.start) != null) {
                start = (String) requestParam.get(Constants.start);
            }
            if (requestParam.containsKey(Constants.limit) && requestParam.get(Constants.limit) != null) {
                limit = (String) requestParam.get(Constants.limit);
            }
            list = executeQuery(query + condition + " ORDER BY dm.name ASC ", params.toArray());
            count=list.size();
            
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                list = executeQueryPaging(query  + condition +" ORDER BY dm.name ASC ", params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            } 
                
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDiscountImpl.getDiscountMaster : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    /**
     * Method to get the list of all terms and mapped discounts to respective terms.
     * @param requestParam
     * @return KwlReturnObject
     * @throws ServiceException
     */
    public KwlReturnObject getDiscountsAndTermsMasters(Map requestParam) throws ServiceException {
        List list = new ArrayList();
        StringBuilder condition = new StringBuilder();
        ArrayList params = new ArrayList();
        String query = "SELECT ct.termid, dm.id, ct.applicabledays, dm.name, dm.value, dm.discounttype, dm.account from creditterm ct inner join discountmaster dm on "
                + " ct.discountname=dm.id ";
        int count = 0;
        try {
            if (requestParam.containsKey(Constants.companyKey) && !StringUtil.isNullObject(requestParam.get(Constants.companyKey))) {
                condition.append(" where ct.company=? ");
                params.add(requestParam.get(Constants.companyKey).toString());
            }
            list = executeSQLQuery(query + condition, params.toArray());
            count = list.size();

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accDiscountImpl.getDiscountsAndTermsMasters : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    public KwlReturnObject deleteDiscountMaster(Map requestParam) throws ServiceException {
        List list = new ArrayList();
        int numRows = 0;
        String msg = "";
        String discountid = requestParam.get("discountid").toString();
        String discountname = requestParam.get("discountname").toString();
        String companyid = requestParam.get("companyid").toString();

        boolean isSuccess = false;
        try {
            String delQuery = "delete from DiscountMaster dm where dm.id=? and dm.company.companyID=?";
            numRows = executeUpdate(delQuery, new Object[]{discountid, companyid});
            isSuccess = true;
            msg = "Discount Master has been deleted successfully.";
        } catch (Exception ex) {
            isSuccess = false;
            msg = "Problem Occured while deleting Discount Master.";
            throw ServiceException.FAILURE("accDiscountImpl.getDiscountMaster : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(isSuccess, msg, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveDiscountMaster(Map requestParam) throws ServiceException {
        boolean isSuccess = false;
        String msg = "";
        List list = new ArrayList();
        DiscountMaster discountMaster = new DiscountMaster();
        try {

            if (requestParam.containsKey("id") && requestParam.get("id") != null && !StringUtil.isNullOrEmpty(requestParam.get("id").toString())) {
                KwlReturnObject discountMasterKwlobj = accountingHandlerDAOobj.getObject(DiscountMaster.class.getName(), requestParam.get("id").toString());
                if (discountMasterKwlobj.getEntityList() != null && !discountMasterKwlobj.getEntityList().isEmpty()) {
                    discountMaster = (DiscountMaster) discountMasterKwlobj.getEntityList().get(0);
                }
            } else {
                String discountMasterId = StringUtil.generateUUID();
                discountMaster.setId(discountMasterId);
            }
            if (requestParam.containsKey("name") && requestParam.get("name") != null) {
                discountMaster.setName(requestParam.get("name").toString());
            }
            if (requestParam.containsKey("description") && requestParam.get("description") != null) {
                discountMaster.setDescription(requestParam.get("description").toString());
            }
            if (requestParam.containsKey("company") && requestParam.get("company") != null) {
                KwlReturnObject companyData = accountingHandlerDAOobj.getObject(Company.class.getName(), requestParam.get("company").toString());
                Company company = (Company) companyData.getEntityList().get(0);
                discountMaster.setCompany(company);
            }
            if (requestParam.containsKey("account") && requestParam.get("account") != null) {
                discountMaster.setAccount(requestParam.get("account").toString());
            }
            if (requestParam.containsKey("discounttype") && requestParam.get("discounttype") != null) {
                discountMaster.setDiscounttype(requestParam.get("discounttype").toString().equals("1"));
            }
            if (requestParam.containsKey("value") && requestParam.get("value") != null) {
                discountMaster.setValue(Double.parseDouble(requestParam.get("value").toString()));
            }
            saveOrUpdate(discountMaster);
            isSuccess = true;
            msg = "Discount Master has been saved successfully.";
        } catch (Exception ex) {
            msg = ex.getMessage();
            throw ServiceException.FAILURE("accDiscountImpl.getDiscountMaster : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(isSuccess, msg, null, list, list.size());
    }

    public KwlReturnObject checkUniqueDiscountMaster(Map requestParam) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        if (requestParam.containsKey("company") && requestParam.get("company") != null) {
            condition += " where company.companyID=? ";
            params.add(requestParam.get("company").toString());
        }
        if (requestParam.containsKey("name") && requestParam.get("name") != null) {
            condition += " and name=? ";
            params.add(requestParam.get("name").toString());
        }
        String query = "from DiscountMaster ";

        if (!StringUtil.isNullOrEmpty(requestParam.get("id").toString())) {
            condition += " and id!=?";
            params.add(requestParam.get("id").toString());
        }
        returnList = executeQuery(query + condition, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    @Override
    public KwlReturnObject getLinkedDiscountMasters(Map requestParam) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        if (requestParam.containsKey("company") && requestParam.get("company") != null) {
            condition += " where companyid=? ";
            params.add(requestParam.get("company").toString());
        }
        if (requestParam.containsKey("id") && requestParam.get("id") != null) {
            condition += " and discountmasterid=? ";
            params.add(requestParam.get("id").toString());
        }
        String query = "select id from productdiscountmapping  ";
        returnList = executeSQLQuery(query + condition, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
}
