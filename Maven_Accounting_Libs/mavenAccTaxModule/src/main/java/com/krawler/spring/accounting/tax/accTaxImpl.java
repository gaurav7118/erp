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
package com.krawler.spring.accounting.tax;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.State;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.DefaultTax1099Category;
import com.krawler.hql.accounting.InvoiceTermsSales;
import com.krawler.hql.accounting.Tax;
import com.krawler.hql.accounting.Tax1099Accounts;
import com.krawler.hql.accounting.Tax1099Category;
import com.krawler.hql.accounting.TaxList;
import com.krawler.spring.accounting.handler.AccountingManager;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXTYPE;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class accTaxImpl extends BaseDAO implements accTaxDAO, TaxConstants {

    public KwlReturnObject getTax(Map<String, Object> requestParams) throws ServiceException {
        String condition = "";
        String maincondition = "";
        ArrayList params = new ArrayList();
        Date transactiondate = (Date) requestParams.get(TRANSACTIONDATE);
        String taxID = (String) requestParams.get(TAXID);
        String companyid = requestParams.containsKey("companyid") && requestParams.get("companyid")!=null ? (String) requestParams.get("companyid"):"";
        Company company = null;
        if(!StringUtil.isNullOrEmpty(companyid)){
            company = (Company) get(Company.class, companyid);
        }
        int countryid = Integer.parseInt(company.getCountry().getID());
        if (transactiondate != null) {
            condition += " and tl1.applyDate <= ?  ";
            params.add(transactiondate);//for 1st subquery
        }
        // For Indian Compliance Only
        if(!StringUtil.isNullOrEmpty(companyid) && countryid == Constants.indian_country_id) {
            if(!StringUtil.isNullOrEmpty(company.getCountry().getID())) {
                condition += " and (tl1.countryid is null or tl1.countryid = '' or tl1.countryid = ? ) ";
                params.add(company.getCountry());//for 1st subquery
            }
        }
        
        if (transactiondate != null) {
            params.add(transactiondate); //for 2nd subquery
        }
        
        if(!StringUtil.isNullOrEmpty(companyid) && countryid == Constants.indian_country_id) {
            if(!StringUtil.isNullOrEmpty(company.getCountry().getID())) {
                params.add(company.getCountry());//for 2nd subquery
            }
        }
        
        params.add((String) requestParams.get(COMPANYID));
        if (taxID != null) {
            maincondition += " and t.ID= ?  ";
            params.add(taxID);

        }
        if (requestParams.containsKey(TAXTYPE)) {
            int taxTYPE = (Integer) requestParams.get(TAXTYPE);
            maincondition += " and ( t.taxtype = ? or  t.taxtype = ?) ";
            params.add(0);
            params.add(taxTYPE);
        }
        if (requestParams.containsKey(TAXTYPEID)) {
            int taxTYPE = Integer.parseInt(requestParams.get(TAXTYPEID).toString());
            maincondition += " and  t.taxtype = ? ";
            params.add(taxTYPE);
        }
        if (requestParams.containsKey("chkDuplicateTax") && ((Boolean) requestParams.get("chkDuplicateTax"))) {
            /**
             * We have allowed user to add duplicate tax code so commented below
             * tax code condition.
             */
//            String taxCode = requestParams.get(TAXCODE).toString();
            String taxName = requestParams.get(TAXNAME).toString();
//            maincondition += " and  (t.taxCode = ? or t.name = ? ) ";
            maincondition += " and  t.name = ? ";
//            params.add(taxCode);
            params.add(taxName);
        }else if (requestParams.containsKey(TAXCODE)) {
            String taxCode = requestParams.get(TAXCODE).toString();
            maincondition += " and  t.taxCode = ? ";
            params.add(taxCode);
        }
        
        if (requestParams.containsKey(Constants.ss) && requestParams.get(Constants.ss) != null) {
            String ss = (String) requestParams.get(SS);
            if (!StringUtil.isNullOrEmpty(ss)) {
                for (int i = 0; i < 2; i++) {
                    params.add("%" + ss + "%");     //SDP-12753
                }
                maincondition += " and ( t.taxCode like ? or t.name like ? )";
            }
        }
        
        /*
         * ERP-40242 : Show only activated taxes in create and copy case and all taxes in edit cases
         */
        if (requestParams.containsKey(Constants.includeDeactivatedTax) && !((Boolean) requestParams.get(Constants.includeDeactivatedTax))) {
            maincondition += " and activated = true";
        }
        
        //Not in query for group company
        if (requestParams.containsKey("notinquery") && requestParams.get("notinquery") != null) {
            String notinquery = (String) requestParams.get("notinquery");
            notinquery = AccountingManager.getFilterInString(notinquery);
            if (!StringUtil.isNullOrEmpty(notinquery)) {
                condition += " and t.taxCode NOT IN " + notinquery;
            }
        }
        
        String query = "select t,"
                + "(select tl1.percent from TaxList tl1 where tax.ID=t.ID  and applyDate in (select max(applyDate) as ld from TaxList where tax.ID=tl1.tax.ID " + condition + " group by tax)),"
                + "(select max(tl1.applyDate) from TaxList tl1 where tax.ID=t.ID  and applyDate in (select max(applyDate) as ld from TaxList where tax.ID=tl1.tax.ID " + condition + " group by tax))"
                + "from Tax t where company.companyID=? and t.deleted=false " + maincondition + "  order by t.name ";
        List list = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getTax1099Category(Map<String, Object> requestParams) throws ServiceException {
        String query = "from Tax1099Category t where t.deleted=false and company.companyID=? order by t.srno ";
        List list = executeQuery( query, requestParams.get(COMPANYID));
        return new KwlReturnObject(true, "", null, list, list.size());
    }

//    @Transactional(propagation = Propagation.REQUIRED)
    public void copyTax1099Category(String companyid) throws ServiceException {
        try {
            String query = "from DefaultTax1099Category";
            List<DefaultTax1099Category> list = executeQuery( query);
            Company company = (Company) get(Company.class, companyid);
            if (list != null) {
                for (DefaultTax1099Category defaulttax : list) {
                    Tax1099Category tax = new Tax1099Category();
                    tax.setCompany(company);
                    tax.setCategory(defaulttax.getCategory());
                    tax.setDeleted(defaulttax.isDeleted());
                    tax.setSrno(defaulttax.getSrno());
                    tax.setThresholdValue(defaulttax.getThresholdValue());
                    save(tax);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyTax1099Category : " + ex.getMessage(), ex);
        }
    }

    public KwlReturnObject getTax1099AccCategory(Map<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(requestParams.get(COMPANYID));
            String condition = "";
            if (requestParams.containsKey(ACCOUNTID)) {
                params.add(requestParams.get(ACCOUNTID));
                condition = " and ta.account.ID =? ";
            }

            String query = "select t from Tax1099Accounts  ta inner join  ta.tax1099Category t where ta.company.companyID=? and t.deleted=false " + condition;

            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getTax1099AccCategory : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject belongsTo1099(String companyid, ArrayList accIDArr) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            params.add(companyid);
            if (!accIDArr.isEmpty()) {
                condition = " and t.account.ID in (";
                for (int i = 0; i < accIDArr.size(); i++) {
                    params.add(accIDArr.get(i));
                    condition += " ?,";
                }
                condition = condition.substring(0, Math.max(0, condition.length() - 1));
                condition += " )";
            }
            String query = "from Tax1099Accounts  t where t.company.companyID=? " + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.belongsTo1099 : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public boolean belongsTo1099Count(String companyid, ArrayList accIDArr) throws ServiceException {
        boolean success = false;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            params.add(companyid);
            if (!accIDArr.isEmpty()) {
                condition = " and t.account.ID in (";
                for (int i = 0; i < accIDArr.size(); i++) {
                    params.add(accIDArr.get(i));
                    condition += " ?,";
                }
                condition = condition.substring(0, Math.max(0, condition.length() - 1));
                condition += " )";
            }
            String query = "select count(*) from Tax1099Accounts t where t.company.companyID=? " + condition;
            List list = executeQuery(query, params.toArray());
            Long totalCount = 0l;
            if (list != null && !list.isEmpty()) {
                totalCount = (Long) list.get(0);
            }
            if (totalCount > 0) {
                success = true;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.belongsTo1099Count : " + ex.getMessage(), ex);
        }
        return success;
    }

    @Override
    public KwlReturnObject getDefaultGSTList(HashMap<String, Object> dataMap) throws ServiceException {

        List list = new ArrayList();
        int count = 0;
        try {
            String condition = "";
            String taxName = "";
            if (dataMap.containsKey("countryid")) {
                String countryid = (String) dataMap.get("countryid");
                condition += " where country=" + countryid;
            } else {
                condition += " where country IS NULL";
            }
            if (dataMap.containsKey("stateid")) {
                String stateid = (String) dataMap.get("stateid");
                condition += " and stateid='" + stateid + "'";
            }
            if (dataMap.containsKey("taxname") && dataMap.get("taxname") != null) {
                taxName = AccountingManager.getFilterInString(dataMap.get("taxname").toString());
                condition += " and name in " + taxName + "";
            }
            String query = "select * from defaultgst" + condition;
            list = executeSQLQuery( query);
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accTaxImpl.getDefaultGSTList : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    public KwlReturnObject getTaxCategoryAccount(String companyid, String taxid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(taxid);
            String condition = " where t.company.companyID=? and t.tax1099Category.ID=? ";
            String query = "select account from Tax1099Accounts  t " + condition;
            list = executeQuery( query, params.toArray());
            count = list.size();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentImpl.getTaxCategoryAccount : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject addTax(Map<String, Object> taxMap) throws ServiceException {
        List list = new ArrayList();
        try {
            Tax tax = new Tax();
            tax.setDeleted(false);
            tax = buildTax(tax, taxMap);
            save(tax);
            list.add(tax);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addTax : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateTax(Map<String, Object> taxMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String taxid = (String) taxMap.get(TAXID);
            Tax tax = (Tax) get(Tax.class, taxid);
            if (tax != null) {
                tax = buildTax(tax, taxMap);
                save(tax);
            }
            list.add(tax);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTax : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax has been updated successfully", null, list, list.size());
    }

    public Tax buildTax(Tax tax, Map<String, Object> taxMap) throws ServiceException {
        try {
            if (taxMap.containsKey("taxname")) {
                tax.setName((String) taxMap.get("taxname"));
            }
            if (taxMap.containsKey(TAXID)) {
                tax.setID((String) taxMap.get(TAXID));
            } else {
                tax.setID(StringUtil.generateUUID());
            }
            if (taxMap.containsKey("taxdescription")) {
                tax.setDescription((String) taxMap.get("taxdescription"));
            }
            if (taxMap.containsKey("taxcode")) {
                tax.setTaxCode((String) taxMap.get("taxcode"));
            }
            if (taxMap.containsKey("taxCodeWithoutPercentage")) {
                tax.setTaxCodeWithoutPercentage((String) taxMap.get("taxCodeWithoutPercentage"));
            }
            if (taxMap.containsKey("taxtypeid")) {
                tax.setTaxtype(Integer.parseInt(taxMap.get("taxtypeid").toString()));
            }
            if (taxMap.containsKey("extrataxtypeid")) {
                tax.setExtrataxtype(Integer.parseInt(taxMap.get("extrataxtypeid").toString()));
            }
            if (taxMap.containsKey(ISINPUTCREDITFORTAX) && !StringUtil.isNullOrEmpty((String) taxMap.get(ISINPUTCREDITFORTAX))) { //ERM-971 taxes for landed cost report 
                tax.setInputCredit(Boolean.parseBoolean(taxMap.get(ISINPUTCREDITFORTAX).toString()));
            }
            if (taxMap.containsKey(ACCOUNTID)) {
                Account account = taxMap.get(ACCOUNTID) == null ? null : (Account) get(Account.class, (String) taxMap.get(ACCOUNTID));
                tax.setAccount(account);
                if (account != null) {
                    String usedin = account.getUsedIn();
                    account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Tax));
                }
            }
            if (taxMap.containsKey(COMPANYID)) {
                Company company = taxMap.get(COMPANYID) == null ? null : (Company) get(Company.class, (String) taxMap.get(COMPANYID));
                tax.setCompany(company);
            }
            if (taxMap.containsKey(ACTIVATED)) {
                tax.setActivated((Boolean) taxMap.get(ACTIVATED));
            }
            if(taxMap.containsKey(DEFAULTTAX) && taxMap.get(DEFAULTTAX)!=null){
                tax.setDefaulttax((String)taxMap.get(DEFAULTTAX));
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildTax : " + e.getMessage(), e);
        }
        return tax;
    }

    public KwlReturnObject deleteTax(String taxid, String companyid) throws ServiceException {
        String delQuery = "delete from Tax  where ID=? and company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{taxid, companyid});
        return new KwlReturnObject(true, "Tax has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject addTaxList(Map<String, Object> taxListMap) throws ServiceException {
        List list = new ArrayList();
        try {
            TaxList taxlist = new TaxList();
            taxlist = buildTaxList(taxlist, taxListMap);
            save(taxlist);
            list.add(taxlist);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addTaxList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax List has been added successfully", null, list, list.size());
    }

    public KwlReturnObject updateTaxList(Map<String, Object> taxListMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String taxlistid = (String) taxListMap.get("taxlistid");
            TaxList taxlist = (TaxList) get(TaxList.class, taxlistid);
            if (taxlist != null) {
                taxlist = buildTaxList(taxlist, taxListMap);
                save(taxlist);
            }
            list.add(taxlist);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTaxList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax List has been updated successfully", null, list, list.size());
    }

    public TaxList buildTaxList(TaxList taxlist, Map<String, Object> taxListMap) throws ServiceException {
        try {
            if (taxListMap.containsKey(APPLYDATE)) {
                taxlist.setApplyDate((Date) taxListMap.get(APPLYDATE));
            }
            if (taxListMap.containsKey(PERCENT)) {
                taxlist.setPercent((Double) taxListMap.get(PERCENT));
            }
            if (taxListMap.containsKey(TAXID)) {
                taxlist.setID((String) taxListMap.get(TAXID));
            }
            if (taxListMap.containsKey(TAXID)) {
                Tax tax = taxListMap.get(TAXID) == null ? null : (Tax) get(Tax.class, (String) taxListMap.get(TAXID));
                taxlist.setTax(tax);
            }
            if (taxListMap.containsKey(COMPANYID)) {
                Company company = taxListMap.get(COMPANYID) == null ? null : (Company) get(Company.class, (String) taxListMap.get(COMPANYID));
                taxlist.setCompany(company);
                taxlist.setCountryid(company.getCountry()!= null ? company.getCountry(): null);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildTaxList : " + e.getMessage(), e);
        }
        return taxlist;
    }

    public KwlReturnObject updateTax1099Category(Map<String, Object> taxCategoryMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String categoryid = (String) taxCategoryMap.get(CATEGORYID);
            Tax1099Category taxcategorylist = (Tax1099Category) get(Tax1099Category.class, categoryid);
            if (taxcategorylist != null) {
                taxcategorylist = buildTax1099CategoryList(taxcategorylist, taxCategoryMap);
                save(taxcategorylist);
            }
            list.add(taxcategorylist);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTaxList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax List has been updated successfully", null, list, list.size());
    }

    public Tax1099Category buildTax1099CategoryList(Tax1099Category taxcategory, Map<String, Object> taxCategoryMap) throws ServiceException {
        try {
            if (taxCategoryMap.containsKey("thresholdvalue")) {
                taxcategory.setThresholdValue((Double) taxCategoryMap.get("thresholdvalue"));
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildTaxList : " + e.getMessage(), e);
        }
        return taxcategory;
    }

    public KwlReturnObject updateTax1099Account(Map<String, Object> taxCategoryAccountMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String categoryid = (String) taxCategoryAccountMap.get(CATEGORYID);
            String companyid = (String) taxCategoryAccountMap.get(COMPANYID);
            Tax1099Accounts taxcategoryacc = null;
            if (categoryid != null) {
                //  deleteTax1099AccountList(categoryid, companyid);
                taxcategoryacc = new Tax1099Accounts();
                taxcategoryacc = buildTax1099AccountList(taxcategoryacc, taxCategoryAccountMap);
                save(taxcategoryacc);
            }
            list.add(taxcategoryacc);
        } catch (Exception e) {
            throw ServiceException.FAILURE("updateTaxList : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Tax List has been updated successfully", null, list, list.size());
    }

    public KwlReturnObject deleteTax1099AccountList(String categoryid, String companyid) throws ServiceException {
        String delQuery = "delete from Tax1099Accounts  where tax1099Category.ID=? and company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{categoryid, companyid});
        return new KwlReturnObject(true, "Tax Category Accounts has been deleted successfully.", null, null, numRows);
    }

    public Tax1099Accounts buildTax1099AccountList(Tax1099Accounts taxaccountscategory, Map<String, Object> taxCategoryAccMap) throws ServiceException {
        try {
            if (taxCategoryAccMap.containsKey(COMPANYID)) {
                Company company = taxCategoryAccMap.get(COMPANYID) == null ? null : (Company) get(Company.class, (String) taxCategoryAccMap.get(COMPANYID));
                taxaccountscategory.setCompany(company);
            }
            if (taxCategoryAccMap.containsKey(ACCOUNTID)) {
                Account taxAccount = taxCategoryAccMap.get(ACCOUNTID) == null ? null : (Account) get(Account.class, (String) taxCategoryAccMap.get(ACCOUNTID));
                taxaccountscategory.setAccount(taxAccount);
                if (taxAccount != null) {
                    String usedin = taxAccount.getUsedIn();
                    taxAccount.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Tax1099_Account));
                }
            }
            if (taxCategoryAccMap.containsKey(CATEGORYID)) {
                Tax1099Category taxCategory = taxCategoryAccMap.get(CATEGORYID) == null ? null : (Tax1099Category) get(Tax1099Category.class, (String) taxCategoryAccMap.get(CATEGORYID));
                taxaccountscategory.setTax1099Category(taxCategory);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("buildTaxList : " + e.getMessage(), e);
        }
        return taxaccountscategory;
    }

    public KwlReturnObject deleteTaxList(String taxid, String companyid) throws ServiceException {
        String delQuery = "delete from TaxList  where tax.ID=? and company.companyID=?";
        int numRows = executeUpdate( delQuery, new Object[]{taxid, companyid});
        return new KwlReturnObject(true, "Tax Details has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject getTaxList(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from TaxList ";

        if (filterParams.containsKey(TAXID)) {
            condition += (condition.length() == 0 ? " where " : " and ") + "tax.ID=?";
            params.add(filterParams.get(TAXID));
        }
        if (filterParams.containsKey(APPLYDATE)) {
            condition += (condition.length() == 0 ? " where " : " and ") + "applyDate=?";
            params.add(filterParams.get(APPLYDATE));
        }
        if (filterParams.containsKey(COMPANYID)) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get(COMPANYID));
        }
        query += condition;
//        query="from TaxList where applyDate=? and tax.ID=?  and company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getTaxFromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from Tax t where account.ID=? and t.company.companyID=?";
        list = executeQuery( q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject getCalculatedTax(Map<String, Object> filterParams) throws ServiceException {
        List returnlist = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        params.add((String) filterParams.get(COMPANYID));
        String ss = (String) filterParams.get(SS);

        if (!StringUtil.isNullOrEmpty(ss)) {
            for (int i = 0; i < 2; i++) {
                params.add(ss + "%");
            }
            condition += " and ( t.taxCode like ? or t.name like ? )";
        }
        if (filterParams.get("isSalesTax") != null) {
            Boolean isSalesTax = (Boolean) filterParams.get("isSalesTax");
            if (isSalesTax) {//0 for old records having no flag, 1 for Purchase ,2 for sales
                params.add(0);
                params.add(2);
            } else {
                params.add(0);
                params.add(1);
            }

            condition += " and ( t.taxtype=  ? or t.taxtype=  ?)";
        }
        String query = "select t,"
                + " (select tl1 from TaxList tl1 where tax.ID=t.ID  and applyDate in (select max(applyDate) as ld from TaxList where tax.ID=tl1.tax.ID  group by tax)) "
                + " from Tax t where company.companyID=? and t.deleted=false " + condition + " order by t.name ";
        returnlist = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    @Override
    public KwlReturnObject getTaxPercent(String companyid, Date transactiondate, String taxid) throws ServiceException {
        List list = new ArrayList();
        double percent = 0;
        boolean isTaxApplicable = false;
        ArrayList params = new ArrayList();
        params.add(taxid);
        params.add(taxid);
        params.add(transactiondate);
        params.add(companyid);
        String query = "select tl1.percent from TaxList tl1 where tl1.tax.ID=?  and applyDate in (select max(applyDate) from TaxList where tax.ID=? and tl1.applyDate <= ? ) and company.companyID=?";
        Iterator itr = executeQuery( query, params.toArray()).iterator();
        while (itr.hasNext()) {
            percent = Double.parseDouble(itr.next().toString());
            isTaxApplicable = true;
        }
        list.add(percent);
        list.add(isTaxApplicable);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public boolean isTermMappedwithTax(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        boolean success = false;
        ArrayList params = new ArrayList();
        params.add(requestParams.get("taxid"));
        params.add(requestParams.get("term"));
        String hqlQuery = "from TaxTermsMapping tx where tx.tax.ID = ? AND tx.invoicetermssales.id=?";
        list = executeQuery( hqlQuery, params.toArray());
        if(list.size()>0){
            success=true;
}
        return success;
    }
    
    @Override
    public List getTerms(String tax) throws ServiceException {
        String query = "select invoicetermssales from taxtermsmapping where tax = ?";
        List list = executeSQLQuery( query, new Object[]{tax});
        return list;
    }
    
    @Override
    public JSONObject getTerms(String tax, JSONObject obj) throws ServiceException {
        String termid = "";
        String termname = "";
        try {
            String query = "select invoicetermssales from taxtermsmapping where tax = ?";
            List l = executeSQLQuery( query, new Object[]{tax});
            Iterator itr = l.iterator();
            while (itr.hasNext()) {
                InvoiceTermsSales invoiceTermsSales = (InvoiceTermsSales) get(InvoiceTermsSales.class, itr.next().toString());
                if (invoiceTermsSales != null) {
                    termid += invoiceTermsSales.getId() + ",";
                    termname += invoiceTermsSales.getTerm() + ",";;
                }
            }
            if (!StringUtil.isNullOrEmpty(termid)) {
                termid = termid.substring(0, termid.length() - 1);
                obj.put("termid", termid);
            }
            if (!StringUtil.isNullOrEmpty(termname)) {
                termname = termname.substring(0, termname.length() - 1);
                obj.put("termname", termname);
            }
        } catch (JSONException ex) {
            Logger.getLogger(accTaxImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return obj;
    }
    
//    TODO : Need to convert getTerms() from SQL to HQL
//    public KwlReturnObject getTaxTerms(String tax) throws ServiceException {
//        List list = new ArrayList();
//        String q = " from TaxTermsMapping t where t.tax.ID = ? ";
//        list = executeQuery( q, new Object[]{tax});
//        return new KwlReturnObject(true, "", null, list, list.size());
//    }
    @Override
    public KwlReturnObject getAllTaxOfCompany(String companyID) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = " from Tax where company.companyID=? ";
            list = executeQuery(query, new Object[]{companyID});
        } catch (Exception ex) {
            Logger.getLogger(accTaxImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    //Update the applydate of all tax
    @Override
    public KwlReturnObject updateApplyDateForTaxes(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            String query = "";
            Date applyDate = null;
            String companyid = (String) requestParams.get("companyid");
            if (requestParams.containsKey("applyDate") && requestParams.get("applyDate")!=null) {
                applyDate = (Date) requestParams.get("applyDate");
            }
            if (applyDate != null && !StringUtil.isNullOrEmpty(companyid)) {
                params.add(applyDate);
                params.add(companyid);
                query = "update taxlist set applydate=? where company=?";
            }
            int numRows = executeSQLUpdate(query, params.toArray());

            return new KwlReturnObject(true, "Apply Date has been updated for taxes", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot update the Apply Date", ex);
        }
    }
    /**
     * Function to get all taxes used in landed cost transactions.
     * @param companyid
     * @return Set
     * @throws ServiceException
     */
    @Override
    public Set<Object> getTaxIdsWithLandedCost(String companyid) throws ServiceException {
        List<Object> resultset = null;
        List params = new ArrayList();
        params.add(companyid);
        String query = "select tx.id from goodsreceipt gr " 
                +" INNER JOIN expenseggrdetails grd ON grd.goodsreceipt = gr.id" 
                +" INNER JOIN journalentry je ON je.id = gr.landedinvoiceje" 
                +" INNER JOIN tax tx ON "
                +" (CASE When gr.tax IS NULL THEN grd.tax = tx.id"  //Global level tax is null 
                +"  WHEN grd.tax IS NULL THEN gr.tax = tx.id END) " //Line level tax is null
                +"  Where gr.company = ? GROUP BY tx.id";
        resultset = executeSQLQuery(query, params.toArray());
        Set<Object> finalset = new HashSet<>(resultset);
        return finalset;
    }
}
