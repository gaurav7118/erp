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
package com.krawler.spring.accounting.account;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.*;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.FieldConstants;
import com.krawler.common.util.StringUtil;
import static com.krawler.common.util.StringUtil.getCustomFieldSearchArray;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class accAccountDAOImpl extends BaseDAO implements accAccountDAO {

    private boolean sortOnType = false;
    private boolean directionDesc = false;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private fieldManagerDAO fieldManagerDAOobj;
    private fieldDataManager fieldDataManagerNew;

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
     public void setFieldDataManager(fieldDataManager fieldDataManagerNew) {
        this.fieldDataManagerNew = fieldDataManagerNew;
    }
    
    @Override
    public KwlReturnObject getAccountFromName(String companyId, String accname) throws ServiceException {
        String query = " select acc from Account acc where acc.company.companyID =  ? and acc.name =  ? and acc.deleted = false ";
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(accname);
        List list = executeQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }

    @Override
    public KwlReturnObject getAccountsFromName(String companyId, Set<String> accname) throws ServiceException {
        String query = "SELECT acc FROM Account acc WHERE acc.company.companyID =  '" + companyId + "' AND acc.name IN(:accountsname) AND acc.deleted = false ";
        Map params = new HashMap();
        params.put("accountsname", accname);
        List list = executeCollectionQuery(query.toString(), params, null);
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    @Override
    public KwlReturnObject getAccountFromCode(String companyId, String acccode) throws ServiceException {
        String query = " select acc from Account acc where acc.company.companyID =  ? and acc.acccode =  ? and acc.deleted = false ";
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(acccode);
        List list = executeQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    @Override
    public KwlReturnObject getTaxFromCode(String companyId, String acccode) throws ServiceException {
        String query = " select tx from Tax tx where tx.company.companyID =  ? and tx.taxCode =  ? and tx.deleted = false ";
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(acccode);
        List list = executeQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    @Override
    public boolean isTaxActivated(String companyId, String taxId) throws ServiceException {
        boolean isActivated = false;
        try {
            String query = "select activated from Tax where company.companyID = ? and id = ?";
            ArrayList params = new ArrayList();
            params.add(companyId);
            params.add(taxId);
            List list = executeQuery(query, params.toArray());
            if (!list.isEmpty() && list.size() > 0) {
                isActivated = (boolean) list.get(0);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDAOImpl.isTaxActivated", ex);
        }
        return isActivated;
    }

    public KwlReturnObject getAccountEntry(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String selectedAccountIds = "";
        String ss = "";
        try {
            if (filterParams.containsKey("ss") && filterParams.get("ss") != null) {
                ss = (String) filterParams.get("ss");
            }

            if (filterParams.containsKey("selectedAccountIds") && filterParams.get("selectedAccountIds") != null) {
                selectedAccountIds = (String) filterParams.get("selectedAccountIds");
            }

            String condition = "where deleted=false";
            String query = "from Account ";

            if (filterParams.containsKey("id")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "ID=?";
                params.add(filterParams.get("id"));
            }
            if (filterParams.containsKey("groupid")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "group.ID=?";
                params.add(filterParams.get("groupid"));
            }
            if (filterParams.containsKey("costOfGoodsSoldGroup")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "group.costOfGoodsSoldGroup=?";
                params.add(filterParams.get("costOfGoodsSoldGroup"));
            }

            // It is the case when multiple companies accounts need to be fetched at a same time. this variable now coming only from method getConsolidationReport() . other wise normal company check will be applied 
            if (filterParams.containsKey("companyGroupIDs") && filterParams.get("companyGroupIDs") != null && !StringUtil.isNullOrEmpty(filterParams.get("companyGroupIDs").toString())) {
                String companyids = AccountingManager.getFilterInString(filterParams.get("companyGroupIDs").toString());
                condition += " and company.companyID in" + companyids + " ";
            } else if (filterParams.containsKey("companyid")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
                params.add(filterParams.get("companyid"));
            }
            
            if (!StringUtil.isNullOrEmpty(ss)) {// Search on name
                String[] searchcol = new String[]{"name","acccode"};
                Map map = StringUtil.insertParamSearchStringMap(params, ss, 2);
                StringUtil.insertParamSearchString(map);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }

            if (filterParams.containsKey("parent")) {
                if (filterParams.get("parent") == null) {
                    condition += (condition.length() == 0 ? " where " : " and ") + "parent is null";
                }
            }

            if (!StringUtil.isNullOrEmpty(selectedAccountIds)) {
                selectedAccountIds = AccountingManager.getFilterInString(selectedAccountIds);
                condition += " and ID in" + selectedAccountIds + " ";
            }
            
            /**
             * Advance Search For Default Form fields
             */
            String filterConjuctionCriteria = Constants.and;
            if (filterParams.containsKey("filterConjuctionCriteria") && filterParams.get("filterConjuctionCriteria") != null) {
                filterConjuctionCriteria = filterParams.get("filterConjuctionCriteria").toString().toLowerCase();
            }
            if (filterParams.containsKey("defaultSearchJson") && filterParams.get("defaultSearchJson") != null) {
                String searchJSONArray = String.valueOf(filterParams.get("defaultSearchJson"));
                String searchJoin = "";
                String searchDefaultFieldSQL = "";
                if (!StringUtil.isNullOrEmpty(searchJSONArray)) {
                    try {
                        JSONArray defaultSearchFieldArray = new JSONArray(searchJSONArray);
                        if (defaultSearchFieldArray.length() > 0) {
                            
                            ArrayList tableArray = new ArrayList();
                            tableArray.add("customer"); //this table array used to identified wheather join exists on table or not                         
                            Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, Constants.Account_ModuleId, tableArray, filterConjuctionCriteria);
                            searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                            searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                            searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("accountRef.", "");
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                condition += " " + searchDefaultFieldSQL;
            }


            if (filterParams.containsKey("order_by")) {
                condition += " order by " + filterParams.get("order_by");
            }
            query += condition;
//        query="from Account ac where ac.company.companyID=?";
            returnList = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    public KwlReturnObject getAccountGroupInfo(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        /*
         * Default Values: nondeleted= true, deleted = false
         */
        boolean deleted = (filterParams.get("deleted") != null) ? (Boolean) filterParams.get("deleted") : false;
        boolean nondeleted = (filterParams.get("nondeleted") != null) ? (Boolean) filterParams.get("nondeleted") : true;
        String condition = "";
        String query = "Select acc, acc.group.ID from Account acc ";
        /*
         * Changes made for Group Detail Report.
         */
        if (nondeleted) {
            condition += " where acc.deleted=false ";
        } else if (deleted) {
            condition += " where acc.deleted=true ";
        }

        if (filterParams.containsKey("id")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ID=?";
            params.add(filterParams.get("id"));
        }
        if (filterParams.containsKey("groupid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "group.ID=?";
            params.add(filterParams.get("groupid"));
        }
        if (filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        if (filterParams.containsKey("parent")) {
            if (filterParams.get("parent") == null) {
                condition += (condition.length() == 0 ? " where " : " and ") + "parent is null";
            }
        }
        if (filterParams.containsKey("name")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "group.name=?";
            params.add(filterParams.get("name"));
        }
        if (filterParams.containsKey(Constants.IS_GROUP_DETAIL_REPORT) && filterParams.containsKey("accountids") && filterParams.get("accountids") != null) {
            String accountIdArr = (String) filterParams.get("accountids");
            String accountIds[] = accountIdArr.split(",");
            String val = "";
            for (int index = 0; index < accountIds.length; index++) {
                val += "'" + accountIds[index] + "',";
            }
            if (val.length() > 1) {
                val = val.substring(0, val.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(val)) {
                condition += (condition.length() == 0 ? " where " : " and ") + "acc.ID  IN (" + val + ")";
            }
        }
        
        if (filterParams.containsKey(Constants.IS_GROUP_DETAIL_REPORT) && filterParams.containsKey("mastertypeid") && filterParams.get("mastertypeid") != null) {
            int mastertypeid = Integer.parseInt(filterParams.get("mastertypeid").toString());
            if (mastertypeid != 0) { // ALL
                if (mastertypeid == 1) { //"Exclude Bank Accounts"
                    condition += (condition.length() == 0 ? " where " : " and ") + " acc.mastertypevalue!=3 ";
                } else if (mastertypeid == 2) { //"Exclude Cash Accounts"
                    condition += (condition.length() == 0 ? " where " : " and ") + " acc.mastertypevalue!=2 ";
                }
            }
        }
        if (filterParams.containsKey(Constants.IS_GROUP_DETAIL_REPORT) && filterParams.containsKey("typeid") && filterParams.get("typeid") != null) {
            int typeid = Integer.parseInt(filterParams.get("typeid").toString());
            if (typeid == 1) { // Balance Sheet
                condition += (condition.length() == 0 ? " where " : " and ") + " acc.accounttype = 1 ";
            } else if (typeid == 2) { // Profit & Loss
                condition += (condition.length() == 0 ? " where " : " and ") + " acc.accounttype = 0 ";
            }
        }
        String filterConjuctionCriteria = Constants.and;
        if (filterParams.containsKey("filterConjuctionCriteria") && filterParams.get("filterConjuctionCriteria") != null) {
            filterConjuctionCriteria = filterParams.get("filterConjuctionCriteria").toString().toLowerCase();
        }
        if (filterParams.containsKey(Constants.IS_GROUP_DETAIL_REPORT) && filterParams.containsKey("defaultSearchJson") && filterParams.get("defaultSearchJson") != null) {
            String searchJSONArray = String.valueOf(filterParams.get("defaultSearchJson"));
            String searchJoin = "";
            String searchDefaultFieldSQL = "";
            if (!StringUtil.isNullOrEmpty(searchJSONArray)) {
                try {
                    JSONArray defaultSearchFieldArray = new JSONArray(searchJSONArray);
                    if (defaultSearchFieldArray.length() > 0) {
                        /*
                         * Advance Search For Default Form fields
                         */
                        ArrayList tableArray = new ArrayList();
                        tableArray.add("customer"); //this table array used to identified wheather join exists on table or not                         
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, Constants.Account_ModuleId, tableArray, filterConjuctionCriteria);
                        searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("accountRef", "acc");
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            condition += " " + searchDefaultFieldSQL;
        }
        
        if (filterParams.containsKey("order_by")) {
            condition += " order by " + filterParams.get("order_by");
        }
        query += condition;
//        query="from Account ac where ac.company.companyID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getAccountDatewise(String companyid, Date startDate, Date endDate, boolean onlyPnLAccounts) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(companyid);
        if (startDate == null) {
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        /**
         * Ledger report - need to fetch data with date and timestamp.
         */
        params.add(authHandler.minDate(startDate));
        params.add(authHandler.maxDate(endDate));
        String query = "from Account ac where ac.company.companyID=? and ac.creationDate between ? and ? and ac.deleted=false ";
        if (onlyPnLAccounts) {  
            /* "true" get only P&L type of accounts (Currently using in BS and TB) */
            query += " and accounttype = ?";
            params.add(0);
        }
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getAccountDatewiseMerged(String companyid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(companyid);
        if (startDate == null) {
            startDate = new Date(0);
        }
        if (endDate == null) {
            endDate = new Date();
        }
        /**
         * Trial Balance report - need to fetch data with date and timestamp.
         */
        params.add(authHandler.minDate(startDate));
        params.add(authHandler.maxDate(endDate));
        String query = "from Account ac where ac.company.companyID=? and ac.creationDate between ? and ? and ac.deleted=false ";
        if (eliminateflag) {
            query += " and ac.eliminateflag=false ";
        }
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getGroupForProfitNloss(String companyid, int nature, boolean affectGrossProfit, boolean isForTradingAndProfitLoss, boolean isCostOfGoodsSold) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        params.add(nature);
        params.add(affectGrossProfit);
        params.add(companyid);
        String query = "";
        if (isForTradingAndProfitLoss) {
            params.add(isCostOfGoodsSold);
            query = "from Group where parent is null and nature =? and affectGrossProfit=? and (company is null or company.companyID=?) and costOfGoodsSoldGroup= ? order by nature desc, displayOrder";
        } else {
            query = "from Group where parent is null and nature =? and affectGrossProfit=? and (company is null or company.companyID=?) order by nature desc, displayOrder";
        }
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getCustomsGroupsForTotal(String groupid) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();

        params.add(groupid);
        String query = "from Groupmapfortotal where groupidtotal.ID=?";
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getCustomLayoutGroups(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String orderBy="";
        boolean isAdminSubdomain = false;
        if (filterParams.containsKey("isAdminSubdomain") && filterParams.get("isAdminSubdomain") != null) {
            isAdminSubdomain = Boolean.parseBoolean(filterParams.get("isAdminSubdomain").toString());
        }
        if (filterParams.containsKey("companyid") && !isAdminSubdomain) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        if (filterParams.containsKey("templateid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "template.ID=?";
            params.add(filterParams.get("templateid"));
        }
        if (filterParams.containsKey("parentid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "parent.ID=?";
            params.add(filterParams.get("parentid"));
        }
        if (filterParams.containsKey("levelZeroFlag")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "parent is null";
        }
        
        if (filterParams.containsKey("nature") && filterParams.get("nature") != null) {
            condition += (condition.length() == 0 ? " where " : " and ") + " nature = " + filterParams.get("nature").toString();
        }
       if (filterParams.containsKey("isdecorder")) {
            orderBy="order by sequence desc" ;
        }else{
           orderBy= " order by sequence";
        }
        String query = "";
        if (!isAdminSubdomain) {
            query = "from LayoutGroup " + condition + orderBy;
        } else {
            query = "from DefaultLayoutGroup " + condition + orderBy;
        }
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getNextCustomLayoutSequence(Map<String, Object> filterParams) throws ServiceException {
        int autoNumber = 0;
        ArrayList params = new ArrayList();
        List returnList = new ArrayList();
        String condition = "";
        String query = "";
        boolean isAdminSubdomain = false;
        if(filterParams.containsKey("isAdminSubdomain") && filterParams.get("isAdminSubdomain") != null){
            isAdminSubdomain = Boolean.parseBoolean(filterParams.get("isAdminSubdomain").toString());
        }
        
        if (!isAdminSubdomain && filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        if (filterParams.containsKey("templateid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "template.ID=?";
            params.add(filterParams.get("templateid"));
        }
        
        if(isAdminSubdomain){
            query = "select max(sequence) from DefaultLayoutGroup " + condition;
        }else{
            query = "select max(sequence) from LayoutGroup " + condition;
        }
        List list = executeQuery( query, params.toArray());
        if (!list.isEmpty() && list.get(0) != null) {
            autoNumber = Integer.parseInt(list.get(0).toString());
        }

        autoNumber++;
        returnList.add(autoNumber);
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getGroupForProfitNlossMerged(String companyid, int nature, boolean affectGrossProfit, boolean defaulttypeflag) throws ServiceException {
        List returnList = new ArrayList();
        String query = "";
        ArrayList params = new ArrayList();
        params.add(nature);
        params.add(affectGrossProfit);
        if (defaulttypeflag) {
            query = "from Group where parent is null and nature =? and affectGrossProfit=? and (company is null) order by nature desc, displayOrder";
        } else {
            params.add(companyid);
            query = "from Group where parent is null and nature =? and affectGrossProfit=? and (company.companyID=?) order by nature desc, displayOrder";
        }

        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    @Override
    public KwlReturnObject addAccount(JSONObject accjson) throws ServiceException {
        List list = new ArrayList();
        try {
            Account account = new Account();
            account.setDeleted(false);
            account.setActivate(true);
            if (accjson.has("creationdate")) {
                account.setCreationDate((Date) accjson.get("creationdate"));
            }
            if (accjson.has("life")) {
                account.setLife(accjson.getDouble("life"));
            }
            if (accjson.has("salvage")) {
                account.setSalvage(accjson.getDouble("salvage"));
            }
            if (accjson.has("budget")) {
                account.setBudget(accjson.getDouble("budget"));
            }
            if (accjson.has("taxid") && !StringUtil.isNullOrEmpty(accjson.getString("taxid"))) {
                account.setTaxid(accjson.getString("taxid"));
            } else{
                account.setTaxid(null);
            }
            if (accjson.has("acccode") && !StringUtil.isNullOrEmpty(accjson.getString("acccode"))) {
                account.setAcccode(accjson.getString("acccode"));
            }
            if (accjson.has("ifsccode") && !StringUtil.isNullOrEmpty(accjson.optString("ifsccode",""))) {
                account.setIfsccode(accjson.getString("ifsccode"));
            }
            if (accjson.has("micrcode") && !StringUtil.isNullOrEmpty(accjson.optString("micrcode",""))) {
                account.setMicrcode(accjson.getString("micrcode"));
            }
            if (accjson.has("mvatcode") && !StringUtil.isNullOrEmpty(accjson.optString("mvatcode",""))) {
                account.setMVATCode(accjson.getString("mvatcode"));
            }
            if (accjson.has("name")) {
                String accountName = "";
                if (!StringUtil.isNullOrEmpty(accjson.getString("name"))) {
                    accountName = accjson.getString("name");
                }
                account.setName(accountName);
            }
            if (accjson.has("balance")) {
                if (accjson.has("isFixedAsset")) {
                    if (accjson.getBoolean("isFixedAsset")) {
                        account.setOpeningBalance(0);
                    } else {
                        account.setOpeningBalance(accjson.getDouble("balance"));
                    }
                    account.setPresentValue(accjson.getDouble("balance"));
                } else {
                    account.setOpeningBalance(accjson.getDouble("balance"));
                    account.setPresentValue(accjson.getDouble("balance"));
                }
            }
            if (accjson.has("minbudget")) {
                account.setCustMinBudget(accjson.getDouble("minbudget"));
            }
            if (accjson.has("companyid")) {
                account.setCompany((Company) get(Company.class, accjson.getString("companyid")));
            }
            if (accjson.has("parentid")) {
                updateHeaderaccountField(accjson.getString("parentid"), true);
                account.setParent((Account) get(Account.class, accjson.getString("parentid")));
            } else {
                account.setParent(null);
            }
//            if (accjson.has("depaccountid")) {
//                account.setDepreciationAccont((Account) get(Account.class, accjson.getString("depaccountid")));
//            }
            if (accjson.has("groupid")) {
                account.setGroup((Group) get(Group.class, accjson.getString("groupid")));
            }
            if (accjson.has("currencyid")) {
                account.setCurrency((KWLCurrency) get(KWLCurrency.class, accjson.getString("currencyid")));
            }
            if (accjson.has("category")) {
                account.setCategory((MasterItem) get(MasterItem.class, accjson.getString("category")));
            }
            if (accjson.has("department")) {
                account.setDepartment((MasterItem) get(MasterItem.class, accjson.getString("department")));
            }
            if (accjson.has("location")) {
                account.setLocation((MasterItem) get(MasterItem.class, accjson.getString("location")));
            }
            if (accjson.has("installation")) {
                account.setInstallation(accjson.getString("installation"));
            }
            if (accjson.has("userId")) {
                account.setUser((User) get(User.class, accjson.getString("userId")));
            }
            if (accjson.has("isdepreciable")) {
                account.setDepreciable(accjson.getBoolean("isdepreciable"));
            }
            if (accjson.has("costCenterId")) {
                account.setCostcenter((CostCenter) get(CostCenter.class, accjson.getString("costCenterId")));
            }
            if (accjson.has("eliminateflag")) {
                account.setEliminateflag(accjson.getBoolean("eliminateflag"));
            }
            if (accjson.has("intercompanyflag")) {
                account.setIntercompanyflag(accjson.getBoolean("intercompanyflag"));
            }
            if (accjson.has("accounttype")) {
                account.setAccounttype(accjson.getInt("accounttype"));
            }
            if (accjson.has("mastertypevalue")) {
                account.setMastertypevalue(accjson.getInt("mastertypevalue"));
            }
            if (accjson.has("intercompanytype")) {
                account.setIntercompanytype((MasterItem) get(MasterItem.class, accjson.getString("intercompanytype")));
            }
            if (accjson.has("aliascode") && !StringUtil.isNullOrEmpty(accjson.getString("aliascode"))) {
                account.setAliascode(accjson.getString("aliascode"));
            }
            if (accjson.has("accdesc") && !StringUtil.isNullOrEmpty(accjson.getString("accdesc"))) {
                account.setDescription(accjson.getString("accdesc"));
            }
            if (accjson.has("purchaseType")) {
                account.setPurchaseType(accjson.getString("purchaseType"));
            }
            /*Used for INDIA country DVAT Form 31*/
            if (accjson.has("salesType")) {
                account.setSalesType(accjson.getString("salesType"));
            }
            if (accjson.has(Constants.IBG_BANK_TYPE)) {
                account.setIBGBank(accjson.getBoolean(Constants.IS_IBG_BANK));
                account.setIbgBankType(accjson.optInt("ibgbanktype",0));
            }
            if (accjson.has("parentCompanyAccountID")) {
                account.setPropagatedAccountID((Account) get(Account.class, accjson.getString("parentCompanyAccountID")));
            }
            
            // ======== Used for INDIA country -At SetupWizard Creation/Update ============
            if (accjson.has("bankbranchname")) {
                account.setBankbranchname(accjson.getString("bankbranchname"));
            }
            if (accjson.has("accountno")) {
                account.setAccountno(accjson.getString("accountno"));
            }
            if (accjson.has("bankbranchaddress")) {
                account.setBankbranchaddress(accjson.getString("bankbranchaddress"));
            }
            if (accjson.has("branchstate")) {
                account.setBranchstate((State) get(State.class, accjson.getString("branchstate")));
            }
            if (accjson.has("bsrcode")) {
                account.setBsrcode(Integer.parseInt(accjson.getString("bsrcode")));
            }
            if (accjson.has("pincode")) {
                account.setPincode(Integer.parseInt(accjson.getString("pincode")));
            }
            // ====================== =======================  =====================
            saveOrUpdate(account);
            list.add(account);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject updateAccount(JSONObject accjson) throws ServiceException {
        Account parentAccount = null;
        List list = new ArrayList();
        try {
            String accountid = accjson.getString("accountid");
            Account account = new Account();
            account = (Account) get(Account.class, accountid);
            if (account != null) {
                if (accjson.has("name")) {
                    account.setName(accjson.getString("name"));
                }
                if (accjson.has("balance")) {
                    if (accjson.has("isFixedAsset")) {
                        if (accjson.getBoolean("isFixedAsset")) {
                            account.setOpeningBalance(0);
                        }
                    } else {
                        account.setOpeningBalance(accjson.getDouble("balance"));
                    }
                }
                if (accjson.has("minbudget")) {
                    account.setCustMinBudget(accjson.getDouble("minbudget"));
                }
                if (accjson.has("life")) {
                    account.setLife(accjson.getDouble("life"));
                }
                if (accjson.has("salvage")) {
                    account.setSalvage(accjson.getDouble("salvage"));
                }
                if (accjson.has("budget")) {
                    account.setBudget(accjson.getDouble("budget"));
                }
                if (accjson.has("taxid")  && !StringUtil.isNullOrEmpty(accjson.getString("taxid"))) {
                    account.setTaxid(accjson.getString("taxid"));
                } else{
                    account.setTaxid(null);
                }
                if (accjson.has("acccode") && !StringUtil.isNullOrEmpty(accjson.getString("acccode"))) {
                    account.setAcccode(accjson.getString("acccode"));
                } else {
                    account.setAcccode(null);
                }
                if (accjson.has("ifsccode") && !StringUtil.isNullOrEmpty(accjson.optString("ifsccode",""))) {
                    account.setIfsccode(accjson.getString("ifsccode"));
                }
                if (accjson.has("companyid")) {
                    account.setCompany((Company) get(Company.class, accjson.getString("companyid")));
                }
                if (account.getParent() != null) {
                    parentAccount = account.getParent();
                }
                if ((parentAccount != null && accjson.has("parentid") && (!accjson.getString("parentid").equalsIgnoreCase(parentAccount.getID()))) || (parentAccount != null && (!accjson.has("parentid")))) {
                    if (parentAccount.getChildren().size() == 1) {
                        updateHeaderaccountField(parentAccount.getID(), false);
                    }
                }
                if (accjson.has("parentid")) {
                    updateHeaderaccountField(accjson.getString("parentid"), true);
                    account.setParent((Account) get(Account.class, accjson.getString("parentid")));
                } else if (!accjson.has("parentid")) {
                    account.setParent(null);
                }
                if (accjson.has("groupid")) {
                    account.setGroup((Group) get(Group.class, accjson.getString("groupid")));
                }
//                if (accjson.has("depaccountid")) {
//                    account.setDepreciationAccont((Account) get(Account.class, accjson.getString("depaccountid")));
//                }
                if (accjson.has("currencyid")) {
                    account.setCurrency((KWLCurrency) get(KWLCurrency.class, accjson.getString("currencyid")));
                }
                if (accjson.has("category")) {
                    account.setCategory((MasterItem) get(MasterItem.class, accjson.getString("category")));
                }
                if (accjson.has("department")) {
                    account.setDepartment((MasterItem) get(MasterItem.class, accjson.getString("department")));
                }
                if (accjson.has("location")) {
                    account.setLocation((MasterItem) get(MasterItem.class, accjson.getString("location")));
                }
                if (accjson.has("installation")) {
                    account.setInstallation(accjson.getString("installation"));
                }
                if (accjson.has("userId")) {
                    account.setUser((User) get(User.class, accjson.getString("userId")));
                }
                if (accjson.has("isdepreciable")) {
                    account.setDepreciable(accjson.getBoolean("isdepreciable"));
                }
                if (accjson.has("costCenterId")) {
                    account.setCostcenter((CostCenter) get(CostCenter.class, accjson.getString("costCenterId")));
                }
                if (accjson.has("creationdate")) {
                    account.setCreationDate((Date) accjson.get("creationdate"));
                }
                if (accjson.has("eliminateflag")) {
                    account.setEliminateflag(accjson.getBoolean("eliminateflag"));
                }
                if (accjson.has("intercompanyflag")) {
                    account.setIntercompanyflag(accjson.getBoolean("intercompanyflag"));
                }
                if (accjson.has("accounttype")) {
                    account.setAccounttype(accjson.getInt("accounttype"));
                }
                if (accjson.has("mastertypevalue")) {
                    account.setMastertypevalue(accjson.getInt("mastertypevalue"));
                }
                if (accjson.has("accaccountcustomdataref")) {
                    AccountCustomData accountCustomData = null;
                    accountCustomData = (AccountCustomData) get(AccountCustomData.class, accjson.getString("accaccountcustomdataref"));
                    account.setAccAccountCustomData(accountCustomData);
                }
                if (accjson.has("intercompanytype")) {
                    account.setIntercompanytype((MasterItem) get(MasterItem.class, accjson.getString("intercompanytype")));
                }
                //removed the isempty check here for aliascode and accdesc because user should be able to remove an existing alias code and description by updating it as blank
                if (accjson.has("aliascode") && !StringUtil.isNullObject(accjson.getString("aliascode"))) {
                    account.setAliascode(accjson.getString("aliascode"));
                }
                if (accjson.has("accdesc") && !StringUtil.isNullObject(accjson.getString("accdesc"))) {
                    account.setDescription(accjson.getString("accdesc"));
                }
                if (accjson.has(Constants.IS_IBG_BANK)) {
                    account.setIBGBank(accjson.getBoolean(Constants.IS_IBG_BANK));
                }
                if(accjson.has("usedin")){
                    if(!StringUtil.isNullOrEmpty(accjson.getString("usedin"))){
                        account.setUsedIn(accjson.getString("usedin"));
                    }
                }
                if (accjson.has("parentCompanyAccountID")) {
                    account.setPropagatedAccountID((Account) get(Account.class, accjson.getString("parentCompanyAccountID")));
                }
                /*Used for INDIA country DVAT Form 31*/
                if (accjson.has("salesType")) {
                    account.setSalesType(accjson.getString("salesType"));
                }
                // ======== Used for INDIA country -At SetupWizard Creation/Update ============
                if (accjson.has("purchaseType")) {
                    account.setPurchaseType(accjson.getString("purchaseType"));
                }
                if (accjson.has("bankbranchname")) {
                    account.setBankbranchname(accjson.getString("bankbranchname"));
                }
                if (accjson.has("bankbranchaddress")) {
                    account.setBankbranchaddress(accjson.getString("bankbranchaddress"));
                }
                if (accjson.has("branchstate")) {
                    account.setBranchstate((State) get(State.class, accjson.getString("branchstate")));
                }
                if (accjson.has("bsrcode")) {
                    account.setBsrcode(Integer.parseInt(accjson.getString("bsrcode")));
                }
                if (accjson.has("pincode")) {
                    account.setPincode(Integer.parseInt(accjson.getString("pincode")));
                }
                if (accjson.has("mvatcode") && !StringUtil.isNullOrEmpty(accjson.optString("mvatcode", ""))) {
                    account.setMVATCode(accjson.getString("mvatcode"));
                }
                // ====================== =======================  =====================
                if (accjson.has(Constants.IBG_BANK_TYPE)) {
                    account.setIBGBank(accjson.getBoolean(Constants.IS_IBG_BANK));
                    account.setIbgBankType(accjson.optInt("ibgbanktype",0));
                }
                saveOrUpdate(account);
            }
            list.add(account);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject saveOrupdateIBGBankDetail(HashMap<String, Object> ibgBankDetailParams) throws ServiceException {
        List list = new ArrayList();
        try {
            IBGBankDetails ibgBankDetail = null;
            String ibgDetailID = (String) ibgBankDetailParams.get(Constants.IBG_BANK_DETAIL_ID);
            if (!StringUtil.isNullOrEmpty(ibgDetailID)) {
                ibgBankDetail = (IBGBankDetails) get(IBGBankDetails.class, ibgDetailID);
            } else {
                ibgBankDetail = new IBGBankDetails();
            }

            if (ibgBankDetailParams.containsKey(Constants.IBG_BANK)) {
                ibgBankDetail.setIbgbank((Integer) ibgBankDetailParams.get(Constants.IBG_BANK));
            }

            if (ibgBankDetailParams.containsKey(Constants.BANK_CODE)) {
                ibgBankDetail.setBankCode((String) ibgBankDetailParams.get(Constants.BANK_CODE));
            }

            if (ibgBankDetailParams.containsKey(Constants.BRANCH_CODE)) {
                ibgBankDetail.setBranchCode((String) ibgBankDetailParams.get(Constants.BRANCH_CODE));
            }

            if (ibgBankDetailParams.containsKey(Constants.ACCOUNT_NUMBER)) {
                ibgBankDetail.setAccountNumber((String) ibgBankDetailParams.get(Constants.ACCOUNT_NUMBER));
            }

            if (ibgBankDetailParams.containsKey(Constants.ACCOUNT_NAME)) {
                ibgBankDetail.setAccountName((String) ibgBankDetailParams.get(Constants.ACCOUNT_NAME));
            }

            if (ibgBankDetailParams.containsKey(Constants.SENDERS_COMPANYID)) {
                ibgBankDetail.setSendersCompanyID((String) ibgBankDetailParams.get(Constants.SENDERS_COMPANYID));
            }

            if (ibgBankDetailParams.containsKey(Constants.BANK_DAILY_LIMIT)) {
                ibgBankDetail.setBankDailyLimit((Double) ibgBankDetailParams.get(Constants.BANK_DAILY_LIMIT));
            }

            if (ibgBankDetailParams.containsKey(Constants.companyid)) {
                Company company = (Company) get(Company.class, (String) ibgBankDetailParams.get(Constants.companyid));
                ibgBankDetail.setCompany(company);
            }
            if (ibgBankDetailParams.containsKey(Constants.Acc_Accountid)) {
                Account account = (Account) get(Account.class, (String) ibgBankDetailParams.get(Constants.Acc_Accountid));
                ibgBankDetail.setAccount(account);
            }

            saveOrUpdate(ibgBankDetail);

            list.add(ibgBankDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveAccount(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            String accountid = (String) dataMap.get("accountid");
            Account account = new Account();
            if (!StringUtil.isNullOrEmpty(accountid)) {
                account = (Account) get(Account.class, accountid);
            }
            if (dataMap.containsKey("name")) {
                account.setName((String) dataMap.get("name"));
            }
            if (dataMap.containsKey("balance")) {
                account.setOpeningBalance((Double) dataMap.get("balance"));
            }
            if (dataMap.containsKey("life")) {
                account.setLife((Double) dataMap.get("life"));
            }
            if (dataMap.containsKey("salvage")) {
                account.setSalvage((Double) dataMap.get("salvage"));
            }
            if (dataMap.containsKey("creationdate")) {
                account.setCreationDate((Date) dataMap.get("creationdate"));
            }
            if (dataMap.containsKey("companyid")) {
                Company company = dataMap.get("companyid") == null ? null : (Company) get(Company.class, (String) dataMap.get("companyid"));
                account.setCompany(company);
            }
            if (dataMap.containsKey("parentid")) {
                Account paccount = dataMap.get("parentid") == null ? null : (Account) get(Account.class, (String) dataMap.get("parentid"));
                account.setParent(paccount);
            }
            if (dataMap.containsKey("groupid")) {
                Group group = dataMap.get("groupid") == null ? null : (Group) get(Group.class, (String) dataMap.get("groupid"));
                account.setGroup(group);
            }
//            if (dataMap.containsKey("depaccountid")) {
//                Account daccount = dataMap.get("depaccountid") == null ? null : (Account) get(Account.class, (String) dataMap.get("depaccountid"));
//                account.setDepreciationAccont(daccount);
//            }
            if (dataMap.containsKey("currencyid")) {
                KWLCurrency currency = dataMap.get("currencyid") == null ? null : (KWLCurrency) get(KWLCurrency.class, (String) dataMap.get("currencyid"));
                account.setCurrency(currency);
            }
            if (dataMap.containsKey("category")) {
                account.setCategory(dataMap.get("category") == null ? null : (MasterItem) get(MasterItem.class, (String) dataMap.get("category")));
            }
            if (dataMap.containsKey("costCenterId")) {
                account.setCostcenter(dataMap.get("costCenterId") == null ? null : (CostCenter) get(CostCenter.class, (String) dataMap.get("costCenterId")));
            }
            if (dataMap.containsKey("eliminateflag")) {
                account.setEliminateflag((Boolean) dataMap.get("eliminateflag"));
            }
            if (dataMap.containsKey("intercompanyflag")) {
                account.setIntercompanyflag((Boolean) dataMap.get("intercompanyflag"));
            }
            if (dataMap.containsKey("intercompanytype")) {
                account.setIntercompanytype((MasterItem) get(MasterItem.class, dataMap.get("intercompanytype").toString()));
            }
            saveOrUpdate(account);
            list.add(account);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject getAccount(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from Account";
        return buildNExecuteQuery( query, requestParams);
    }

    @Override
    public KwlReturnObject getIBGDetailsForAccount(String accountID, String companyID) throws ServiceException {
        String query = " from IBGBankDetails where account.ID = ? and company.companyID = ? ";
        List list = executeQuery( query, new Object[]{accountID, companyID});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getIBGDetailsForAccountSQL(String accountID, String companyID) throws ServiceException {
        String query = "select bankdailylimit from ibgbankdetails where account = ? and company = ? ";
        List list = executeSQLQuery( query, new Object[]{accountID, companyID});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCIMBDetailsForAccount(String accountID, String companyID) throws ServiceException {
        String query = " from CIMBBankDetails where account.ID = ? and company.companyID = ? ";
        List list = executeQuery( query, new Object[]{accountID, companyID});
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateAccountCurrency(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            String query="";
            Date creationdate=null;
            String currencyID = (String) requestParams.get("currencyid");
            String companyid = (String) requestParams.get("companyid");
            if (requestParams.containsKey("applyDate")){
                creationdate = (Date) requestParams.get("applyDate");
            }
            if (!StringUtil.isNullOrEmpty(currencyID)) {
                params.add(currencyID);
                query = " update account acc set acc.currency=? where acc.company=? ";
            } else if (creationdate != null) {
                params.add(creationdate);
                query = " update account acc set acc.creationdate=? where acc.company=? ";
            }
            params.add(companyid);
            int numRows = executeSQLUpdate( query, params.toArray());

            return new KwlReturnObject(true, "Currency has been updated in account", null, null, numRows);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot update the Currency ", ex);
        }

    }

    public KwlReturnObject getSundryAccount(String companyId, boolean isVendor) throws ServiceException {
        String query = "select acc.id from " + (isVendor ? "vendor v " : "customer v ") + "  right join account acc  ON v.id = acc.id where acc.company =  ?  and v.ID is  null  and acc.name =  ? ";
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(isVendor ? Constants.SUNDRY_VENDOR : Constants.SUNDRY_CUSTOMER);
        List list = executeSQLQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }

    public KwlReturnObject getGroup(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from Group";
        return buildNExecuteQuery( query, requestParams);
    }

    public KwlReturnObject getAccounts(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            int start = 0;
            int limit = 15;
            boolean pagingFlag = false;
            boolean quickSearchFlag = false;
            //int currencyType=2; // 0- Base Currency and 1-Foreign Currency
            String selectedAccountIds = "";
            String accGroupIDs = "";
            String selectedBalPLId = "";
            boolean isForBSPLtoGL = false;

            boolean generalLedgerFlag = false;
            boolean isExportingSelectedRecord = false;
            boolean ignoreBankAccounts = requestParams.get("ignoreBankAccounts") != null;
            boolean ignoreCashAccounts = requestParams.get("ignoreCashAccounts") != null;
            boolean showAllAccountsInGl = false;
            if (requestParams.containsKey("showAllAccountsInGl") && requestParams.get("showAllAccountsInGl") != null) {
                showAllAccountsInGl = Boolean.parseBoolean((requestParams.get("showAllAccountsInGl").toString()));
            }
          
            if (requestParams.containsKey("selectedAccountIds") && requestParams.get("selectedAccountIds") != null) {
                selectedAccountIds = (String) requestParams.get("selectedAccountIds");
            }
            if (requestParams.containsKey("accgroupids") && requestParams.get("accgroupids") != null) {
                accGroupIDs = requestParams.get("accgroupids").toString();
            }
            
            if (requestParams.containsKey("selectedBalPLId") && requestParams.get("selectedBalPLId") != null) {
                selectedBalPLId = (String) requestParams.get("selectedBalPLId");
            }
            if (requestParams.containsKey("generalLedgerFlag") && requestParams.get("generalLedgerFlag") != null) {
                generalLedgerFlag = Boolean.parseBoolean(requestParams.get("generalLedgerFlag").toString());
            }
            if (requestParams.containsKey("isExportingSelectedRecord") && requestParams.get("isExportingSelectedRecord") != null) {
                isExportingSelectedRecord = Boolean.parseBoolean(requestParams.get("isExportingSelectedRecord").toString());
            }

            if (requestParams.containsKey("isForBSPLtoGL") && requestParams.get("isForBSPLtoGL") != null) {
                isForBSPLtoGL = Boolean.parseBoolean(requestParams.get("isForBSPLtoGL").toString());
            }
//            if (requestParams.containsKey("currencytype") && requestParams.get("generalLedgerFlag") != null) {
//                currencyType  = Integer.parseInt(requestParams.get("currencytype").toString());
//            }
            if (requestParams.containsKey("start") && requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString())) {
                start = Integer.parseInt(requestParams.get("start").toString());
                limit = Integer.parseInt(requestParams.get("limit").toString());
                pagingFlag = true;
            }
            if (!showAllAccountsInGl && generalLedgerFlag) {
                pagingFlag = false;
            }
            ArrayList params = new ArrayList();
            String[] groups = (String[]) requestParams.get("group");

            String condition = (requestParams.get("ignore") == null ? "" : " not ");
            String[] nature = (String[]) requestParams.get("nature");
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);
            boolean deleted = Boolean.parseBoolean((String) requestParams.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) requestParams.get("nondeleted"));
            if (groups != null) {
                String qMarks = "?";
                params.add("null");
                for (int i = 0; i < groups.length; i++) {
                    qMarks += ",?";
                    params.add(groups[i]);
                }
                condition = " and ac.group.ID " + condition + " in (" + qMarks + ") ";
            } else if (nature != null) {
                String qMarks = "?";
                params.add(5);//not a nature
                for (int i = 0; i < nature.length; i++) {
                    qMarks += ",?";
                    params.add(Integer.parseInt(nature[i]));
                }
                condition += " and ac.group.nature " + condition + " in (" + qMarks + ") ";
            }
//            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
//            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
//            if(ignoreCustomers || ignoreVendors) {
//                String qMarks="";
//                if(ignoreCustomers){
//                    qMarks="?";
//                    params.add(Group.ACCOUNTS_RECEIVABLE);
//                }
//                if(ignoreVendors) {
//                    if(!StringUtil.isNullOrEmpty(qMarks)) {
//                        qMarks+=",?";
//                    } else {
//                        qMarks="?";
//                    }
//                    params.add(Group.ACCOUNTS_PAYABLE);
//                }
//                condition += " and ac.group.ID not in ("+qMarks+") ";
//            }
//            if(generalLedgerFlag && currencyType < 2){
//                if(currencyType==0)  //For base Currency
//                    condition += " and ac.currency.currencyID in ( ac.company.currency.currencyID )";
//                else    // for Forign Currency
//                    condition += " and ac.currency.currencyID not in ( ac.company.currency.currencyID )";
//            }
            String costCenterId = (String) requestParams.get("costCenterId");
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                params.add(costCenterId);
                condition += " and ac.costcenter.ID=?";
            }

            if (ignoreBankAccounts) {
                condition += " and ac.mastertypevalue!=3 ";
            }
            if (ignoreCashAccounts) {
                condition += " and ac.mastertypevalue!=2 ";
            }

            if (requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String ss = requestParams.get("ss").toString();
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"ac.name", "ac.acccode", "ac.aliascode"};
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 3);
                        StringUtil.insertParamSearchString(map);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    condition += searchQuery;
                    quickSearchFlag = true;
                }
            }
            if (requestParams.containsKey("isFromCOA")) {
                quickSearchFlag = (boolean) requestParams.get("isFromCOA");
            }

            if (!StringUtil.isNullOrEmpty(selectedAccountIds)) {
                selectedAccountIds = AccountingManager.getFilterInString(selectedAccountIds);
                condition += " and ac.ID in" + selectedAccountIds + " ";
            }
            
            if (!StringUtil.isNullOrEmpty(accGroupIDs)) {
                accGroupIDs = AccountingManager.getFilterInString(accGroupIDs);
                condition += " and ac.group.ID in" + accGroupIDs + " ";
            }

            if (!StringUtil.isNullOrEmpty(selectedBalPLId)) {
                if (selectedBalPLId.equals("1")) {
                    condition += " and (ac.group.nature=0 or ac.group.nature=1) ";
                }
                if (selectedBalPLId.equals("2")) {
                    condition += " and (ac.group.nature=2 or ac.group.nature=3) ";
                }
            }

            String orderBy = "";
            if (requestParams.containsKey("dir") && requestParams.containsKey("sort")) {
                String Col_Name = requestParams.get("sort").toString();
                String Col_Dir = requestParams.get("dir").toString();
                orderBy = sortColumnAccount(Col_Name, Col_Dir);
            } else {
                orderBy = " order by ac.acccode, ac.name asc, ac.aliascode ";
            }

            boolean noActivityAcc = false;
            if (requestParams.containsKey("acctypes") && requestParams.get("acctypes") != null) {
                if (StringUtil.equal(requestParams.get("acctypes").toString(), "3")) {
                    //quickSearchFlag = true;
                    noActivityAcc = true;
                }
            }

            if (nondeleted) {
                condition += " and ac.deleted=false ";
            } else if (deleted) {
                condition += " and ac.deleted=true ";
            }


//            if(!quickSearchFlag && !generalLedgerFlag){
            if (!quickSearchFlag && !isForBSPLtoGL && !isExportingSelectedRecord) {// if this is not a quick search and not exporting selected record
                condition += " and ac.parent is null ";
            }

            boolean controlAccounts = false;

            if (requestParams.containsKey("controlAccounts") && requestParams.get("controlAccounts") != null) {
                controlAccounts = (Boolean) requestParams.get("controlAccounts");
            }
            if (!controlAccounts) {
                params.add(controlAccounts);
                condition += " and ac.controlAccounts=?";
            }
            String mySearchFilterString = "", mySearchFilterStringAsset = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
                if (requestParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            boolean isSplitOpeningBalanceSearch = true;
            boolean isSplitOpeningBalanceAmount = false;
            if (generalLedgerFlag && requestParams.containsKey("isSplitOpeningBalanceAmount") && requestParams.get("isSplitOpeningBalanceAmount") != null) {
                isSplitOpeningBalanceAmount = Boolean.parseBoolean(requestParams.get("isSplitOpeningBalanceAmount").toString());
            }
            if (requestParams.get("isSplitOpeningBalanceSearch") != null) {
                isSplitOpeningBalanceSearch = Boolean.parseBoolean(requestParams.get("isSplitOpeningBalanceSearch").toString());
            }
                 /**
             * This Function will use when Users Visibility Feature is Enable
             * Append user condition while querying data
             */
            String usercondition = "";
            String customdatajoin = "";
            if (requestParams.containsKey("isUserVisibilityFlow") && requestParams.get("isUserVisibilityFlow") != null) {
                usercondition = (String) requestParams.get("appendusercondtion");
                customdatajoin = " inner join ac.accAccountCustomData acd ";
            }
            ArrayList accountparams = new ArrayList(params);
            String searchJoin = "";
            String searchDefaultFieldSQL = "";
            JSONArray customSearchFieldArray = new JSONArray();
            JSONArray defaultSearchFieldArray = new JSONArray();
            if (!isSplitOpeningBalanceAmount && !isSplitOpeningBalanceSearch) {
                if (requestParams.containsKey(Constants.moduleid) && requestParams.get(Constants.moduleid) != null && !StringUtil.isNullOrEmpty(String.valueOf(requestParams.get(Constants.moduleid)))) {
                    String appendCase = "and";
                    String Searchjson = "";
                    if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                        Searchjson = requestParams.get("searchJson").toString();

                        if (!StringUtil.isNullOrEmpty(Searchjson)) {
                            JSONObject serachJobj = new JSONObject(Searchjson);
                            StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);
                            if (defaultSearchFieldArray.length() > 0) {
                                /*
                                 Advance Search For Default Form fields
                                 */
                                ArrayList tableArray = new ArrayList();
                                tableArray.add("customer"); //this table array used to identified wheather join exists on table or not                         
                                Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, Constants.Account_ModuleId, tableArray, filterConjuctionCriteria);
                                searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
//                        searchJoin += " left join solinking on solinking.docid=salesorder.id and solinking.sourceflag = 1 ";
                                searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                                searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("accountRef", "ac");
                            }
                            if (customSearchFieldArray.length() > 0) {   //Advance search case for Custome field
                                requestParams.put(Constants.Searchjson, Searchjson);
                                requestParams.put(Constants.appendCase, appendCase);
                                requestParams.put("filterConjuctionCriteria", filterConjuctionCriteria);
                                mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, true).get(Constants.myResult));
                                mySearchFilterString = mySearchFilterString.replaceAll("AccountCustomData", "ac.accAccountCustomData");
                                customdatajoin="";
                                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                            }
                            mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                        }
                    }
                }
            }else{      // In COA Report if advance search coming then we need to ignore paging as we are calculating runtime records
                if (!generalLedgerFlag && requestParams.get("searchJson") != null &&  !StringUtil.isNullOrEmpty((String)requestParams.get("searchJson"))) {
                    pagingFlag = false;
                }
            }
            
            String datefilter = "";
            boolean isSearchFieldForAsset = false;
            if (requestParams.containsKey("isSearchFieldForAsset")) {
                isSearchFieldForAsset = Boolean.parseBoolean(requestParams.get("isSearchFieldForAsset").toString());
            }
            
            ArrayList paramsAsset = new ArrayList(params);
            String tempSearchjson="";
            if (generalLedgerFlag) {
                String Searchjson = "";
                if (requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null) {
                    Searchjson = requestParams.get("searchJson").toString();
                }
                if (isSearchFieldForAsset && requestParams.containsKey("tempSearchjson") && requestParams.get("tempSearchjson") != null) {
                    tempSearchjson = requestParams.get("tempSearchjson").toString();
                }
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    customSearchFieldArray = new JSONArray();
                    defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);

                    if (defaultSearchFieldArray.length() > 0) {
                        /*
                         * Advance Search For Default Form fields
                         */
                        ArrayList tableArray = new ArrayList();
                        tableArray.add("customer"); //this table array used to identified wheather join exists on table or not                         
                        Map<String, Object> map = buildSqlDefaultFieldAdvSearch(defaultSearchFieldArray, params, Constants.Account_ModuleId, tableArray, filterConjuctionCriteria);
                        searchJoin += map.containsKey("searchjoin") ? map.get("searchjoin") : "";
//                        searchJoin += " left join solinking on solinking.docid=salesorder.id and solinking.sourceflag = 1 ";
                        searchDefaultFieldSQL = (String) (map.containsKey("condition") ? map.get("condition") : "");
                        searchDefaultFieldSQL = searchDefaultFieldSQL.replaceAll("accountRef", "ac");
                    }
                    if (customSearchFieldArray.length() > 0) {

                            HashMap<String, Object> request = new HashMap<String, Object>();
                            Searchjson = getJsornStringForSearch(Searchjson, companyid, false);
                            request.put(Constants.Searchjson, Searchjson);
                            request.put(Constants.appendCase, "and");
                            request.put(Constants.moduleid, "100");
                            request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                            try {
                                mySearchFilterString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                                if (mySearchFilterString.contains(Constants.AccJECustomData)) {
                                    customdatajoin += " inner join jed.journalEntry je left join je.accBillInvCustomData jecd ";
                                    mySearchFilterString = mySearchFilterString.replaceAll("AccJECustomData", "jecd");
                                }
                                if (mySearchFilterString.contains(Constants.AccJEDetailCustomData)) {
                                    customdatajoin += " left join jed.accJEDetailCustomData jedcd ";
                                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailCustomData", "jedcd");
                                }
                                if (mySearchFilterString.contains(Constants.AccJEDetailsProductCustomData)) {
                                    customdatajoin += " left join jed.accJEDetailsProductCustomData jepcd ";
                                    mySearchFilterString = mySearchFilterString.replaceAll("AccJEDetailsProductCustomData", "jepcd");
                                }
                                
                                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                            } catch (JSONException ex) {
                                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (ParseException ex) {
                                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            if(isSearchFieldForAsset && !StringUtil.isNullOrEmpty(tempSearchjson)){
                                HashMap<String, Object> requestAsset = new HashMap<String, Object>();
                                requestAsset.put(Constants.Searchjson, tempSearchjson);
                                requestAsset.put(Constants.appendCase, "and");
                                requestAsset.put(Constants.moduleid, Constants.Acc_FixedAssets_Details_ModuleId);
                                requestAsset.put("filterConjuctionCriteria", filterConjuctionCriteria);
                                try {
                                    mySearchFilterStringAsset = String.valueOf(StringUtil.getMyAdvanceSearchString(requestAsset, true).get(Constants.myResult));
                                    mySearchFilterStringAsset = mySearchFilterStringAsset.replaceAll("assetdetailcustomdata", "ad.assetDetailsCustomData");    //SDP-8046
                                    mySearchFilterStringAsset = mySearchFilterStringAsset.replaceAll("AccJEDetailCustomData", "ad.assetDetailsCustomData");
                                    mySearchFilterStringAsset = mySearchFilterStringAsset.replaceAll("AccJEDetailsProductCustomData", "ad.assetDetailsCustomData");
                                    StringUtil.insertParamAdvanceSearchString1(paramsAsset, tempSearchjson);
                                } catch (JSONException | ParseException ex) {
                                    Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }

    //                    Date startDate = requestParams.get("startDate") != null ? (Date) requestParams.get("startDate") : null;
                            Date endDate = requestParams.get("endDate") != null ? (Date) requestParams.get("endDate") : null;
                            datefilter = "and jed.journalEntry.pendingapproval = 0 and jed.journalEntry.draft=false and jed.journalEntry.istemplate != 2 and jed.journalEntry.entryDate>=? and jed.journalEntry.entryDate<? ";

                            Date startDate = new Date(0);//done for ERP-13428 ticket
                            if (endDate == null) {
                                endDate = new Date();
                            }

                            if (!isSearchFieldForAsset) {
                                params.add(startDate);
                                params.add(endDate);
                            }
                        }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
            boolean exportGLCSV=false;
            if(requestParams.containsKey("exportGLCSV") && requestParams.get("exportGLCSV")!=null){
                exportGLCSV = (Boolean) requestParams.get("exportGLCSV");
            }
            boolean showChildAccountsInGl = false;
            if (requestParams.containsKey("showChildAccountsInGl") && requestParams.get("showChildAccountsInGl") != null) {
                showChildAccountsInGl = Boolean.parseBoolean(requestParams.get("showChildAccountsInGl").toString());
            }
            if (!quickSearchFlag && generalLedgerFlag && showChildAccountsInGl) {
                /*
                 * if "showChildAccountsInGl" is true then we have to show child account in the hierarchy of parent account. 
                 * So we have included only parent account if "showChildAccountsInGl" is true.
                 */
                condition += " and ac.parent is null ";
            }
            if (isSplitOpeningBalanceAmount && generalLedgerFlag && requestParams.containsKey("isSplitOpeningBalanceSearch") && requestParams.get("isSplitOpeningBalanceSearch") != null) {
                if (isSplitOpeningBalanceSearch) {
                    mySearchFilterString = "";
                    params = accountparams;
                }
            }
//            if (isSearchFieldForAsset && generalLedgerFlag) {
//                mySearchFilterString = "";
//                params = accountparams;
//            }
            Long totalCount = 0l;
            String query = "", countquery = "",queryAsset = "", countqueryAsset = "";
                if (isSplitOpeningBalanceAmount && isSplitOpeningBalanceSearch)  {
                    if (generalLedgerFlag && requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null && !StringUtil.isNullOrEmpty((String) requestParams.get("searchJson")) && !isSplitOpeningBalanceSearch) {
                        query = "select distinct ac from Account ac,com.krawler.hql.accounting.JournalEntryDetail jed " + customdatajoin + " where ac.id = jed.account and ac.company.companyID=? " + condition + mySearchFilterString + datefilter + "  " + orderBy;

                        countquery = "select count(distinct ac.ID) as cnt from Account ac,com.krawler.hql.accounting.JournalEntryDetail jed " + customdatajoin + " where ac.id = jed.account and ac.company.companyID=? " + condition + mySearchFilterString + datefilter + "  " + orderBy;
                    } else {
                        query = "from Account ac where ac.company.companyID=? " + condition + mySearchFilterString + "  " + orderBy;//"order by ac.acccode, ac.name";

                        countquery = "select count(ac.ID) as cnt from Account ac where ac.company.companyID=? " + condition + mySearchFilterString + "  " + orderBy;//"order by ac.acccode, ac.name";
                    }
                } else if (generalLedgerFlag && isSearchFieldForAsset) {
                    query = "select distinct ac from Account ac,com.krawler.hql.accounting.JournalEntryDetail jed " + customdatajoin + " where ac.id = jed.account and ac.company.companyID=? " + condition + mySearchFilterString + "  " + orderBy;//"order by ac.acccode, ac.name";

                    countquery = "select count(ac.ID) as cnt from Account ac,com.krawler.hql.accounting.JournalEntryDetail jed " + customdatajoin + " where ac.id = jed.account and ac.company.companyID=? " + condition + mySearchFilterString + "  " + orderBy;//"order by ac.acccode, ac.name";

                    if(!StringUtil.isNullOrEmpty(tempSearchjson)){
                        queryAsset = "select distinct ac from Account ac, AssetDetails ad where (ac.id = ad.product.purchaseAccount or ac.id = ad.product.depreciationProvisionGLAccount) and ac.company.companyID=? " + condition + mySearchFilterStringAsset + "  " + orderBy;//"order by ac.acccode, ac.name";

                        countqueryAsset = "select count(ac.ID) as cnt from Account ac, AssetDetails ad where (ac.id = ad.product.purchaseAccount or ac.id = ad.product.depreciationProvisionGLAccount) and ac.company.companyID=? " + condition + mySearchFilterStringAsset + "  " + orderBy;//"order by ac.acccode, ac.name";
                    }
                } else {
                    if (generalLedgerFlag && requestParams.containsKey("searchJson") && requestParams.get("searchJson") != null && !StringUtil.isNullOrEmpty((String) requestParams.get("searchJson")) && customSearchFieldArray.length() > 0) {
                        query = "select distinct ac from Account ac,com.krawler.hql.accounting.JournalEntryDetail jed " + customdatajoin + " where ac.id = jed.account and ac.company.companyID=? " + condition + mySearchFilterString + datefilter + "  " + orderBy;

                        countquery = "select count(distinct ac.ID) as cnt from Account ac,com.krawler.hql.accounting.JournalEntryDetail jed " + customdatajoin + " where ac.id = jed.account and ac.company.companyID=? " + condition + mySearchFilterString + datefilter + "  " + orderBy;
                    } else {
                        query = "select ac from Account ac "+customdatajoin+"where ac.company.companyID=? " + condition + usercondition + mySearchFilterString + "  " + orderBy;//"order by ac.acccode, ac.name";

                        countquery = "select count(ac.ID) as cnt from Account ac" + customdatajoin +" where ac.company.companyID=? " + condition + usercondition  + mySearchFilterString + "  " + orderBy;//"order by ac.acccode, ac.name";
                    }
                }
            
            List list = new ArrayList();
            List listAsset = new ArrayList();
            if(!exportGLCSV){
                list = executeQuery( countquery, params.toArray());
                if (list != null && !list.isEmpty()) {
                    totalCount = (Long) list.get(0);
                }
                if(isSearchFieldForAsset && !StringUtil.isNullOrEmpty(tempSearchjson) && !StringUtil.isNullOrEmpty(countqueryAsset)){
                    listAsset = executeQuery( countqueryAsset, paramsAsset.toArray());
                    if (listAsset != null && !listAsset.isEmpty()) {
                        totalCount += (Long) listAsset.get(0);
                    }
                }
            }
            if (pagingFlag && !noActivityAcc) {
                list = executeQueryPaging( query, params.toArray(), new Integer[]{start, limit});
            } else {
                list = executeQuery( query, params.toArray());
                if(isSearchFieldForAsset && !StringUtil.isNullOrEmpty(tempSearchjson) && !StringUtil.isNullOrEmpty(queryAsset)){
                    listAsset = executeQuery( queryAsset, paramsAsset.toArray());
                    if (listAsset != null && !listAsset.isEmpty()) {
                        for(Object obj : listAsset){
                            if(!list.contains(obj)){
                                list.add(obj);
                            }
                        }
                    }
                }
                if(exportGLCSV){
                    Integer count=list.size();
                    totalCount=count.longValue();
                }
            }
            List resultlist = getAccountArrayList(list, requestParams, quickSearchFlag, noActivityAcc);
            result = new KwlReturnObject(true, null, null, resultlist, totalCount.intValue());
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccounts:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
    
    public String getJsornStringForSearch(String Searchjson, String companyId, boolean isFixedAsset) throws ServiceException {
        String returnStr = "";
        try {
            JSONArray jArray = new JSONArray();
            JSONObject jSONObject = new JSONObject();
            JSONObject jobjSearch = new JSONObject(Searchjson);
            int count = jobjSearch.getJSONArray(Constants.root).length();
            for (int i = 0; i < count; i++) {
                KwlReturnObject result = null;
                KwlReturnObject resultdata = null;
                JSONObject jobj1 = jobjSearch.getJSONArray(Constants.root).getJSONObject(i);
                String[] arr = null;
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel));
                requestParams.put(Constants.filter_values, Arrays.asList(companyId, StringUtil.DecodeText(jobj1.getString("columnheader"))));
                result = getFieldParams(requestParams);
                List lst = result.getEntityList();
                Iterator ite = lst.iterator();
                int noOfModules = 0;
                while (ite.hasNext()) {
                    noOfModules++;
                    FieldParams tmpcontyp = null;
                    tmpcontyp = (FieldParams) ite.next();
                    if (!isFixedAsset) {                        
                        JSONObject jobj = new JSONObject();
    
                        jobj.put("column", tmpcontyp.getId());
                        jobj.put("refdbname", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                        jobj.put("xfield", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                        jobj.put("iscustomcolumn", jobj1.getString("iscustomcolumn"));
                        jobj.put("iscustomcolumndata", tmpcontyp.isIsForKnockOff() ? (tmpcontyp.getCustomcolumn() == 1 ? "true" : "false") : jobj1.getString("iscustomcolumndata"));
                        jobj.put("isfrmpmproduct", jobj1.getString("isfrmpmproduct"));
                        jobj.put("fieldtype", tmpcontyp.getFieldtype());
                        if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12) {
                            arr = jobj1.getString("searchText").split(",");
                            String Searchstr = "";
                            HashMap<String, Object> requestParamsdata = null;
                            for (String key : arr) {
                                FieldComboData fieldComboData1 = (FieldComboData) get(FieldComboData.class, key);
                                requestParamsdata = new HashMap<String, Object>();
                                requestParamsdata.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value"));
                                try {
                                    requestParamsdata.put(Constants.filter_values, Arrays.asList(tmpcontyp.getId(), StringUtil.DecodeText(fieldComboData1.getValue())));
                                } catch (Exception e) {
                                    requestParamsdata.put(Constants.filter_values, Arrays.asList(tmpcontyp.getId(), fieldComboData1.getValue()));
                                }

                                resultdata = getFieldParamsComboData(requestParamsdata);
                                List lstdata = resultdata.getEntityList();
                                Iterator itedata = lstdata.iterator();
                                if (itedata.hasNext()) {
                                    FieldComboData fieldComboData = null;
                                    fieldComboData = (FieldComboData) itedata.next();
                                    Searchstr += fieldComboData.getId().toString() + ",";
                                }
                            }
                            jobj.put("searchText", Searchstr);
                            jobj.put("search", Searchstr);
                        } else {
                            jobj.put("searchText", jobj1.getString("searchText"));
                            jobj.put("search", jobj1.getString("searchText"));
                        }
                        jobj.put("columnheader", StringUtil.DecodeText(jobj1.optString("columnheader")));
                        try {
                            jobj.put("combosearch", StringUtil.DecodeText(jobj1.optString("combosearch")));
                        } catch (Exception e) {
                            jobj.put("combosearch", jobj1.getString("combosearch"));
                        }
                        jobj.put("isinterval", jobj1.getString("isinterval"));
                        jobj.put("interval", jobj1.getString("interval"));
                        jobj.put("isbefore", jobj1.getString("isbefore"));
                        jobj.put("xtype", StringUtil.getXtypeVal(tmpcontyp.getFieldtype()));
                        jArray.put(jobj);
                        if (tmpcontyp.getCustomcolumn() == 1 && tmpcontyp.getCustomfield() == 0) {
                            JSONObject jobjOnlyForDimention = new JSONObject(jobj.toString());
                            jobjOnlyForDimention.remove("iscustomcolumndata");
                            jobjOnlyForDimention.put("iscustomcolumndata", "true");
                            jArray.put(jobjOnlyForDimention);
                    }
                }
                    else{
                        if (tmpcontyp.getModuleid() == Constants.Acc_FixedAssets_Details_ModuleId) {
                            JSONObject jobj = new JSONObject();
                    jobj.put("column", tmpcontyp.getId());
                    jobj.put("refdbname", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                    jobj.put("xfield", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                    jobj.put("iscustomcolumn", jobj1.getString("iscustomcolumn"));
                    jobj.put("iscustomcolumndata", tmpcontyp.isIsForKnockOff() ? (tmpcontyp.getCustomcolumn() == 1 ? "true" : "false") : jobj1.getString("iscustomcolumndata"));
                    jobj.put("isfrmpmproduct", jobj1.getString("isfrmpmproduct"));
                    jobj.put("fieldtype", tmpcontyp.getFieldtype());
                    if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12) {
                                String[] coldataArray = StringUtil.DecodeText(jobj1.optString("combosearch")).split(",");
                        String Searchstr = "";
                                String Coldata = "";
                                for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                    Coldata += "'" + coldataArray[countArray] + "',";
                            }
                                Coldata = Coldata.substring(0, Coldata.length() - 1);
                                Searchstr = fieldManagerDAOobj.getIdsUsingParamsValue(tmpcontyp.getId(), Coldata.replaceAll("'", ""));
                        jobj.put("searchText", Searchstr);
                        jobj.put("search", Searchstr);
                    } else {
                        jobj.put("searchText", jobj1.getString("searchText"));
                        jobj.put("search", jobj1.getString("searchText"));
                    }
                    jobj.put("columnheader", StringUtil.DecodeText(jobj1.optString("columnheader")));
                        jobj.put("combosearch", StringUtil.DecodeText(jobj1.optString("combosearch")));
                    jobj.put("isinterval", jobj1.getString("isinterval"));
                    jobj.put("interval", jobj1.getString("interval"));
                    jobj.put("isbefore", jobj1.getString("isbefore"));
                    jobj.put("xtype", StringUtil.getXtypeVal(tmpcontyp.getFieldtype()));
                    jArray.put(jobj);
                    if (tmpcontyp.getCustomcolumn() == 1 && tmpcontyp.getCustomfield() == 0) {
                        JSONObject jobjOnlyForDimention = new JSONObject(jobj.toString());
                        jobjOnlyForDimention.remove("iscustomcolumndata");
                        jobjOnlyForDimention.put("iscustomcolumndata", "true");
                        jArray.put(jobjOnlyForDimention);
                    }
                            break;
                        } else {
                            if (noOfModules == lst.size()) {
                                jArray.put(jobj1);
                }
            }
                    
                    }
            }
            }
            jSONObject.put("root", jArray);
            returnStr = jSONObject.toString();
        } catch (JSONException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnStr;
    }

    @Override
    public String getAdvanceSearchStringForMultiEntity(String Searchjson, String companyId) throws ServiceException {
        String returnStr = "";
        try {
            JSONArray jArray = new JSONArray();
            JSONObject jSONObject = new JSONObject();
            JSONObject jobjSearch = new JSONObject(Searchjson);
            int count = jobjSearch.getJSONArray(Constants.root).length();
            for (int i = 0; i < count; i++) {
                KwlReturnObject result = null;
                KwlReturnObject resultdata = null;
                JSONObject jobj1 = jobjSearch.getJSONArray(Constants.root).getJSONObject(i);
                String[] arr = null;
                HashMap<String, Object> requestParams = new HashMap<>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel));
                requestParams.put(Constants.filter_values, Arrays.asList(companyId, StringUtil.DecodeText(jobj1.getString("columnheader"))));
                requestParams.put(Constants.isMultiEntity, 1);
                result = getFieldParams(requestParams);
                List<FieldParams> fieldParamsList = result.getEntityList();
                for (FieldParams fieldParams : fieldParamsList) {
                    int moduleId = fieldParams.getModuleid();
                    if (moduleId == Constants.Acc_Vendor_Invoice_ModuleId || moduleId == Constants.Acc_Make_Payment_ModuleId || moduleId == Constants.Acc_Debit_Note_ModuleId || moduleId == Constants.Acc_GENERAL_LEDGER_ModuleId
                            || moduleId == Constants.Acc_Invoice_ModuleId || moduleId == Constants.Acc_Receive_Payment_ModuleId || moduleId == Constants.Acc_Credit_Note_ModuleId || moduleId == Constants.Acc_Delivery_Order_ModuleId) {
                        JSONObject jobj = new JSONObject();
                        jobj.put("column", fieldParams.getId());
                        jobj.put("refdbname", Constants.Custom_Column_Prefix + fieldParams.getColnum());
                        jobj.put("xfield", Constants.Custom_Column_Prefix + fieldParams.getColnum());
                        jobj.put("iscustomcolumn", jobj1.getString("iscustomcolumn"));
                        jobj.put("iscustomcolumndata", fieldParams.isIsForKnockOff() ? (fieldParams.getCustomcolumn() == 1 ? "true" : "false") : jobj1.getString("iscustomcolumndata"));
                        jobj.put("isfrmpmproduct", jobj1.getString("isfrmpmproduct"));
                        jobj.put("fieldtype", fieldParams.getFieldtype());
                        if (fieldParams.getFieldtype() == 4 || fieldParams.getFieldtype() == 7 || fieldParams.getFieldtype() == 12) {
                            arr = jobj1.getString("searchText").split(",");
                            String Searchstr = "";
                            HashMap<String, Object> requestParamsdata = null;
                            for (String key : arr) {
                                FieldComboData fieldComboData1 = (FieldComboData) get(FieldComboData.class, key);
                                requestParamsdata = new HashMap<>();
                                requestParamsdata.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value"));
                                try {
                                    requestParamsdata.put(Constants.filter_values, Arrays.asList(fieldParams.getId(), StringUtil.DecodeText(fieldComboData1.getValue())));
                                } catch (Exception e) {
                                    requestParamsdata.put(Constants.filter_values, Arrays.asList(fieldParams.getId(), fieldComboData1.getValue()));
                                }
                                resultdata = getFieldParamsComboData(requestParamsdata);
                                List lstdata = resultdata.getEntityList();
                                Iterator itedata = lstdata.iterator();
                                if (itedata.hasNext()) {
                                    FieldComboData fieldComboData = null;
                                    fieldComboData = (FieldComboData) itedata.next();
                                    Searchstr += fieldComboData.getId() + ",";
                                }
                            }
                            jobj.put("searchText", Searchstr);
                            jobj.put("search", Searchstr);
                        } else {
                            jobj.put("searchText", jobj1.getString("searchText"));
                            jobj.put("search", jobj1.getString("searchText"));
                        }
                        jobj.put("columnheader", StringUtil.DecodeText(jobj1.optString("columnheader")));
                        try {
                            jobj.put("combosearch", StringUtil.DecodeText(jobj1.optString("combosearch")));
                        } catch (Exception e) {
                            jobj.put("combosearch", jobj1.getString("combosearch"));
                        }
                        jobj.put("isinterval", jobj1.getString("isinterval"));
                        jobj.put("interval", jobj1.getString("interval"));
                        jobj.put("isbefore", jobj1.getString("isbefore"));
                        jobj.put("xtype", StringUtil.getXtypeVal(fieldParams.getFieldtype()));
                        jArray.put(jobj);
                    }
                }
            }
            jSONObject.put("root", jArray);
            returnStr = jSONObject.toString();
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAdvanceSearchStringForMultiEntity : " + ex.getMessage(), ex);
        }
        return returnStr;
    }
    
    public KwlReturnObject getFieldParamsComboData(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from FieldComboData ";
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
            if (requestParams.containsKey("customfield") && (Integer) requestParams.get("customfield") != null) {
                hql += " and customfield = 1";
            }
            if (requestParams.containsKey("relatedmoduleid")) {
                hql += " and relatedmoduleid like '%" + requestParams.get("relatedmoduleid") + "%'";
            }
            list = executeQuery( hql, value.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public String sortColumnAccount(String Col_Name, String Col_Dir) throws ServiceException {
        String String_Sort = "";
        if (Col_Name.equals("groupname")) {
            String_Sort = " order by ac.group.name " + Col_Dir;
        } else if (Col_Name.equals("accname")) {
            String_Sort = " order by ac.name " + Col_Dir;
        } else if (Col_Name.equals("accounttypestring")) {
            if (Col_Dir.equals("ASC")) { // we are saving 0 for profit & loss, 1 for balnce sheet.so reversing order to sort alphabetically
                String_Sort = " order by ac.accounttype DESC";
            } else {
                String_Sort = " order by ac.accounttype ASC";
            }
        } else if (Col_Name.equals("mastertypevaluestring")) {
            String_Sort = " order by ac.mastertypevalue " + Col_Dir;
        } else if (Col_Name.equals("creationDate")) {
            String_Sort = " order by ac.creationDate " + Col_Dir;
        } else if (Col_Name.equals("currencyname")) {
            String_Sort = " order by ac.currency.name " + Col_Dir;
        } else if (Col_Name.equals("acccode")) {
            String_Sort = " order by ac.acccode " + Col_Dir;
        } else if (Col_Name.equals("aliascode")) {
            String_Sort = " order by ac.aliascode " + Col_Dir;
        } else {
            String_Sort = " order by ac.acccode, ac.name asc, ac.aliascode ";
        }
        return String_Sort;
    }

    public String sortColumnAccountOptimized(String Col_Name, String Col_Dir) throws ServiceException {
        String String_Sort = "";
        if (Col_Name.equals("groupname")) {
            String_Sort = " order by grp.name " + Col_Dir;
        } else if (Col_Name.equals("accname")) {
            String_Sort = " order by ac.name " + Col_Dir;
        } else if (Col_Name.equals("accounttypestring")) {
            if (Col_Dir.equals("ASC")) { // we are saving 0 for profit & loss, 1 for balnce sheet.so reversing order to sort alphabetically
                String_Sort = " order by ac.accounttype DESC";
            } else {
                String_Sort = " order by ac.accounttype ASC";
            }
        } else if (Col_Name.equals("mastertypevaluestring")) {
            String_Sort = " order by ac.mastertypeid " + Col_Dir;
        } else if (Col_Name.equals("creationDate")) {
            String_Sort = " order by ac.creationdate " + Col_Dir;
        } else if (Col_Name.equals("currencyname")) {
            String_Sort = " order by cr.name " + Col_Dir;
        } else if (Col_Name.equals("acccode")) {
            String_Sort = " order by ac.acccode " + Col_Dir;
        } else if (Col_Name.equals("aliascode")) {
            String_Sort = " order by ac.aliascode " + Col_Dir;
        } else {
            String_Sort = " order by ac.acccode, ac.name asc, ac.aliascode ";
        }
        return String_Sort;
    }

    public KwlReturnObject getAccountsForCombo(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            String start = (String) requestParams.get(Constants.start);
            String limit = (String) requestParams.get(Constants.limit);
            
            ArrayList params = new ArrayList();
            String[] groups = (String[]) requestParams.get("group");

            String condition = "";
            String selectedBalPLId="";
              if (requestParams.containsKey("selectedBalPLId") && requestParams.get("selectedBalPLId") != null) {
                selectedBalPLId = (String) requestParams.get("selectedBalPLId");
            }
            String[] nature = (String[]) requestParams.get("nature");
            String companyid = (String) requestParams.get("companyid");
            boolean deleted = Boolean.parseBoolean((String) requestParams.get("deleted"));
            boolean nondeleted = Boolean.parseBoolean((String) requestParams.get("nondeleted"));
            boolean headerAdded = Boolean.parseBoolean((String) requestParams.get("headerAdded"));
            boolean onlyBalancesheet = Boolean.parseBoolean((String) requestParams.get("onlyBalancesheet"));
            boolean bankBookSumarryReport = Boolean.parseBoolean((String)requestParams.get("bankBookSumarryReport"));    
            boolean isIBGType = false;
            boolean ignorePaging = false;

            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            
            params.add(companyid);

            if (nature != null) {
                String qMarks = "?";
                params.add(5);//not a nature
                for (int i = 0; i < nature.length; i++) {
                    qMarks += ",?";
                    params.add(Integer.parseInt(nature[i]));
                }
                condition += " and grp.nature " + condition + " in (" + qMarks + ") ";
            }
            
            boolean ignoreGLAccounts = requestParams.get("ignoreGLAccounts") != null;
            boolean ignoreCashAccounts = requestParams.get("ignoreCashAccounts") != null;
            boolean ignoreBankAccounts = requestParams.get("ignoreBankAccounts") != null;
            boolean ignoreGSTAccounts = requestParams.get("ignoreGSTAccounts") != null;
            boolean showGSTAndExpenseGLAccounts = false;
            if (requestParams.containsKey("showGSTAndExpenseGLAccounts") && requestParams.get("showGSTAndExpenseGLAccounts") != null) {
                showGSTAndExpenseGLAccounts = (boolean) requestParams.get("showGSTAndExpenseGLAccounts");
            }
            boolean intercompanyflag = (requestParams.containsKey("intercompanyflag") && requestParams.get("intercompanyflag") != null) ? Boolean.parseBoolean(requestParams.get("intercompanyflag").toString()) : false;
            
            if (ignoreGLAccounts) {
                condition += " and ac.mastertypeid!=1 ";
            }
            if (ignoreCashAccounts) {
                condition += " and ac.mastertypeid!=2 ";
            }
            if (ignoreBankAccounts) {
                condition += " and ac.mastertypeid!=3 ";
            }
            if (ignoreGSTAccounts) {
                condition += " and ac.mastertypeid!=4 ";
            }
            if (onlyBalancesheet) {
                condition += " and ac.accounttype=1";   // for Showing the balance sheet type account in Customer and Vendor creation
            }
            if (showGSTAndExpenseGLAccounts) {
                condition += " and ( ac.mastertypeid = 4 or (ac.mastertypeid = 1 and grp.nature = 2 )) ";     //ERP-33428
            }
            
            if (!StringUtil.isNullOrEmpty(selectedBalPLId)) {
                if (selectedBalPLId.equals("1")) {
                    condition += " and (grp.nature=0 or grp.nature=1) ";
                }
                if (selectedBalPLId.equals("2")) {
                    condition += " and (grp.nature=2 or grp.nature=3) ";
                }
            }
            boolean bankBook = (requestParams.containsKey("bankBook") && requestParams.get("bankBook") != null) ? Boolean.parseBoolean(requestParams.get("bankBook").toString()) : false;
            if (bankBook) {
                condition += " and ac.mastertypeid=3 ";
            }
            
            if (requestParams.containsKey("ss")) {
                String ss = (String) requestParams.get("ss");
                if (!StringUtil.isNullOrEmpty(ss)) {
                    if (bankBookSumarryReport) {
                        condition += StringUtil.getSearchString(ss, "and", new String[]{"ac.name"});
                        Map map = StringUtil.insertParamSearchStringMap(params, ss, 1);
                        StringUtil.insertParamSearchString(map);
                    } else {
                        condition += StringUtil.getSearchString(ss, "and", new String[]{"ac.name", "ac.acccode", "grp.name"});
                        Map map = StringUtil.insertParamSearchStringMap(params, ss, 3);
                        StringUtil.insertParamSearchString(map);
                    }
                }
            }
            
            if (requestParams.containsKey("isIBGTypeOnly") && requestParams.get("isIBGTypeOnly") != null) {
                condition += " and ac.ibgbank=?";
                params.add((Boolean) requestParams.get("isIBGTypeOnly"));
            }
            
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                condition += " and ac.creationdate>=? and ac.creationdate<=? ";
                params.add(df.parse(startDate));
                params.add(df.parse(endDate));
            }
            
            if (nondeleted) {
                condition += " and ac.deleteflag='F' ";
            } else if (deleted) {
                condition += " and ac.deleteflag='T' ";
            }
            if (!headerAdded) {
                condition += " and ac.isheaderaccount=false ";
            }
            if (intercompanyflag) {
                condition += " and ac.intercompanyflag=true ";
                if (requestParams.containsKey("intercompanytypeid") && !requestParams.get("intercompanytypeid").toString().equals("All")) {
                    condition += " and ac.intercompanytype=? ";
                    params.add(requestParams.get("intercompanytypeid"));
                }
            }
            
            if (requestParams.get("templateid") != null) {//  Custom Layout - filter accounts if already mapped in the selected template
                condition += " and ac.id not in (select grm.account from groupaccmap grm inner join layoutgroup lg on lg.id=grm.layoutgroup where lg.template = ? )";
                params.add(requestParams.get("templateid"));
            }
            if (requestParams.get("ignoreTaggedAccounts") != null) {
                boolean ignoreTaggedAccounts = Boolean.parseBoolean((String) requestParams.get("ignoreTaggedAccounts"));
                if (ignoreTaggedAccounts) {
                    condition += " and (ac.id not in (select c.account from customer c where c.company = ? ) and ac.id not in (select v.account from vendor v where v.company = ? )) ";
                    params.add(companyid);
                    params.add(companyid);
                }
            }
            
            boolean controlAccounts = false;
            if (requestParams.containsKey("controlAccounts") && requestParams.get("controlAccounts") != null) {
                controlAccounts = (Boolean) requestParams.get("controlAccounts");
            }
            if (!controlAccounts) {
                params.add(controlAccounts);
                condition += " and ac.controlaccounts=?";
            }

            if (requestParams.containsKey("ids") && requestParams.get("ids")!=null) {
                String ids = requestParams.get("ids").toString();
                ids = AccountingManager.getFilterInString(ids);
                condition += " and ac.id in " + ids;
            }
            
            if(requestParams.containsKey("isFromReport") && requestParams.get("isFromReport")!=null){
                if (requestParams.containsKey("cogaid") && requestParams.get("cogaid") != null) {
                    String cogaid = requestParams.get("cogaid").toString();
                    condition += " and ac.id = ? ";
                    params.add(cogaid);
                }
            }
            
            if (requestParams.get("transactionCurrency") != null) {
                condition += " and ac.currency = ? ";
                params.add(requestParams.get("transactionCurrency"));
            }
            if (requestParams.get("ignorePaging") != null) {//    ERP-13570
                 ignorePaging = Boolean.parseBoolean(requestParams.get("ignorePaging").toString()); 
            }
            
            String orderBy = "";
            
            if (requestParams.containsKey("accountsortingflag") && requestParams.get("accountsortingflag") != null) {
                int accountsortingflag = Integer.parseInt(requestParams.get("accountsortingflag").toString());
                if (accountsortingflag == 0) {
                    if (requestParams.containsKey("dir") && requestParams.containsKey("sort")) {
                        String Col_Name = requestParams.get("sort").toString();
                        String Col_Dir = requestParams.get("dir").toString();
                        orderBy = sortColumnAccountOptimized(Col_Name, Col_Dir);
                    } else {
                        orderBy = " order by ac.name ";
                    }
                } else if (accountsortingflag == 1) {
                    orderBy = " order by ac.acccode";
                }
            }
            
            String query = "";
            String usercondition = "";
            String customdatajoin = "";

              /**
             * This Function will use when Users Visibility Feature is Enable
             * Append user condition while querying data
             */
            if (requestParams.containsKey("isUserVisibilityFlow") && requestParams.get("isUserVisibilityFlow") != null) {
                usercondition = (String) requestParams.get("appendusercondtion");
                customdatajoin = " inner join accountcustomdata acd on acd.accountId=ac.id ";

            }
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(new Date());
            // removing account.natureOfPayment,account.typeofpayment and passing empty to select
            query = "select ac.id,ac.name,ac.acccode,ac.description,ac.taxid,grp.id as groupid,grp.name as groupname,ac.activate,ac.usedin,ac.wanttopostje,ac.creationdate,"
                    + "ac.mastertypeid,ac.currency,ac.taxid,ac.aliascode,ac.accounttype,'','',ac.parent,ac.deleteflag,grp.nature,cr.symbol,cr.currencycode,cr.name,ac.ibgbank,ac.ibgbanktype,ac.activate "
                    + "from account ac"
                    + " inner join accgroup grp on grp.id=ac.groupname " 
                    + " inner join currency cr on cr.currencyid=ac.currency " + customdatajoin 
                    + " where ac.company=? " + condition + usercondition + orderBy;

            List list = executeSQLQuery(query, params.toArray());
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(new Date());
            long diff = cal2.getTimeInMillis() - cal1.getTimeInMillis();
            System.out.println("Query End Seconds = "+diff/1000%60+" | Milli Seconds = "+diff);
            int totalCount = list.size();
             if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false && !ignorePaging) {        
                list = executeSQLQueryPaging(query, params.toArray(),new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
            }
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (NumberFormatException | SQLException | ParseException | ServiceException ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccountsForCombo:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
    
//    public KwlReturnObject getAccountsForCombo(HashMap<String, Object> requestParams) throws ServiceException {
//        KwlReturnObject result;
//        try {
////            int start=0;
////            int limit =30;
//            String offset="";
//            String start = (String) requestParams.get(Constants.start);
//            String limit = (String) requestParams.get(Constants.limit);
////            if(requestParams.containsKey("start")&&requestParams.containsKey("limit")){
////                 start=Integer.parseInt(""+requestParams.get("start"));
////                 limit=Integer.parseInt(""+requestParams.get("limit"));
////            }
//            ArrayList params = new ArrayList();
//            String[] groups = (String[]) requestParams.get("group");
//
//            String condition = "";
//            String selectedBalPLId="";
//              if (requestParams.containsKey("selectedBalPLId") && requestParams.get("selectedBalPLId") != null) {
//                selectedBalPLId = (String) requestParams.get("selectedBalPLId");
//            }
//            String[] nature = (String[]) requestParams.get("nature");
//            String companyid = (String) requestParams.get("companyid");
//            boolean deleted = Boolean.parseBoolean((String) requestParams.get("deleted"));
//            boolean nondeleted = Boolean.parseBoolean((String) requestParams.get("nondeleted"));
//            boolean headerAdded = Boolean.parseBoolean((String) requestParams.get("headerAdded"));
//            boolean onlyBalancesheet = Boolean.parseBoolean((String) requestParams.get("onlyBalancesheet"));
//            boolean bankBookSumarryReport = Boolean.parseBoolean((String)requestParams.get("bankBookSumarryReport"));    
//            boolean isIBGType = false;
//            boolean ignorePaging = false;
//
//            DateFormat df = (DateFormat) requestParams.get(Constants.df);
//
//            params.add(companyid);
//
//            /*
//             * if(groups!=null){ String qMarks="?"; params.add("null"); for(int
//             * i=0;i<groups.length;i++){ qMarks+=",?"; params.add(groups[i]);
//             * Object object=get(Group.class, groups[i]);
//             * if(object!=null){ Group group = (Group)object; Iterator itr =
//             * group.getChildren().iterator(); while(itr.hasNext()){ Group
//             * childGroup=(Group)itr.next(); qMarks+=",?";
//             * params.add(childGroup.getID()); } } } condition=" and ac.group.ID
//             * "+condition+" in ("+qMarks+") ";
//            }
//             */
//            if (nature != null) {
//                String qMarks = "?";
//                params.add(5);//not a nature
//                for (int i = 0; i < nature.length; i++) {
//                    qMarks += ",?";
//                    params.add(Integer.parseInt(nature[i]));
//                }
//                condition += " and ac.group.nature " + condition + " in (" + qMarks + ") ";
//            }
//
//            boolean ignoreGLAccounts = requestParams.get("ignoreGLAccounts") != null;
//            boolean ignoreCashAccounts = requestParams.get("ignoreCashAccounts") != null;
//            boolean ignoreBankAccounts = requestParams.get("ignoreBankAccounts") != null;
//            boolean ignoreGSTAccounts = requestParams.get("ignoreGSTAccounts") != null;
//            boolean showGSTAndExpenseGLAccounts = false;
//            if (requestParams.containsKey("showGSTAndExpenseGLAccounts") && requestParams.get("showGSTAndExpenseGLAccounts") != null) {
//                showGSTAndExpenseGLAccounts = (boolean) requestParams.get("showGSTAndExpenseGLAccounts");
//            }
//
////            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
////            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
////            boolean ignoreAssets = requestParams.get("ignoreAssets")!=null;
//            boolean intercompanyflag = (requestParams.containsKey("intercompanyflag") && requestParams.get("intercompanyflag") != null) ? Boolean.parseBoolean(requestParams.get("intercompanyflag").toString()) : false;
////            if(ignoreCustomers || ignoreVendors || ignoreAssets) {
////                String qMarks="";
////                if(ignoreCustomers){
////                    qMarks="?";
////                    params.add(Group.ACCOUNTS_RECEIVABLE);
////                }
////                if(ignoreVendors) {
////                    if(!StringUtil.isNullOrEmpty(qMarks)) {
////                        qMarks+=",?";
////                    } else {
////                        qMarks="?";
////                    }
////                    params.add(Group.ACCOUNTS_PAYABLE);
////                }
////                if(ignoreAssets) {
////                    if(!StringUtil.isNullOrEmpty(qMarks)) {
////                        qMarks+=",?";
////                    } else {
////                        qMarks="?";
////                    }
////                    params.add(Group.FIXED_ASSETS);
////                }
////                condition += " and ac.group.ID not in ("+qMarks+") ";
////            }
//            if (ignoreGLAccounts) {
//                condition += " and ac.mastertypevalue!=1 ";
//            }
//            if (ignoreCashAccounts) {
//                condition += " and ac.mastertypevalue!=2 ";
//            }
//            if (ignoreBankAccounts) {
//                condition += " and ac.mastertypevalue!=3 ";
//            }
//            if (ignoreGSTAccounts) {
//                condition += " and ac.mastertypevalue!=4 ";
//            }
//            if (onlyBalancesheet) {
//                condition += " and ac.accounttype=1";   // for Showing the balance sheet type account in Customer and Vendor creation
//            }
//            if (showGSTAndExpenseGLAccounts) {
//                condition += " and ( ac.mastertypevalue = 4 or (ac.mastertypevalue = 1 and ac.group.nature = 2 )) ";     //ERP-33428
//            }
//            if (!StringUtil.isNullOrEmpty(selectedBalPLId)) {
//                if (selectedBalPLId.equals("1")) {
//                    condition += " and (ac.group.nature=0 or ac.group.nature=1) ";
//                }
//                if (selectedBalPLId.equals("2")) {
//                    condition += " and (ac.group.nature=2 or ac.group.nature=3) ";
//                }
//            }
//            boolean bankBook = (requestParams.containsKey("bankBook") && requestParams.get("bankBook") != null) ? Boolean.parseBoolean(requestParams.get("bankBook").toString()) : false;
//            if (bankBook) {
////                String qMarks="";
////                if(!StringUtil.isNullOrEmpty(qMarks)) {
////                        qMarks+=",?";
////                    } else {
////                        qMarks="?";
////                    }
////                params.add(Group.BANK_ACCOUNT);
//                condition += " and ac.mastertypevalue=3 ";
//            }
//
//           if (requestParams.containsKey("ss")) {
//                String ss = (String) requestParams.get("ss");
//                if (!StringUtil.isNullOrEmpty(ss)) {
//                    if (bankBookSumarryReport) {
//                        condition += StringUtil.getSearchString(ss, "and", new String[]{"ac.name"});
//                        Map map = StringUtil.insertParamSearchStringMap(params, ss, 1);
//                        StringUtil.insertParamSearchString(map);
//                    } else {
//                        condition += StringUtil.getSearchString(ss, "and", new String[]{"ac.name", "ac.acccode", "ac.group.name"});
//                        Map map = StringUtil.insertParamSearchStringMap(params, ss, 3);
//                        StringUtil.insertParamSearchString(map);
//                    }
//                }
//            }
//
//            if (requestParams.containsKey("isIBGTypeOnly") && requestParams.get("isIBGTypeOnly") != null) {
//                condition += " and ac.IBGBank=?";
//                params.add((Boolean) requestParams.get("isIBGTypeOnly"));
//            }
//
//            String startDate = (String) requestParams.get(Constants.REQ_startdate);
//            String endDate = (String) requestParams.get(Constants.REQ_enddate);
//            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
//                condition += " and ac.creationDate>=? and ac.creationDate<=? ";
//                params.add(df.parse(startDate));
//                params.add(df.parse(endDate));
//            }
//
//            if (nondeleted) {
//                condition += " and ac.deleted=false ";
//            } else if (deleted) {
//                condition += " and ac.deleted=true ";
//            }
//            if (!headerAdded) {
//                condition += " and ac.headeraccountflag=false ";
//            }
//            if (intercompanyflag) {
//                condition += " and ac.intercompanyflag=true ";
//                if (requestParams.containsKey("intercompanytypeid") && !requestParams.get("intercompanytypeid").toString().equals("All")) {
//                    condition += " and ac.intercompanytype.ID=? ";
//                    params.add(requestParams.get("intercompanytypeid"));
//                }
//            }
//            if (requestParams.get("templateid") != null) {//  Custom Layout - filter accounts if already mapped in the selected template
//                condition += " and ac.ID not in (select account.ID from GroupAccMap where layoutgroup.template.ID = ? )";
//                params.add(requestParams.get("templateid"));
//            }
//            if (requestParams.get("ignoreTaggedAccounts") != null) {
//                boolean ignoreTaggedAccounts = Boolean.parseBoolean((String) requestParams.get("ignoreTaggedAccounts"));
//                if (ignoreTaggedAccounts) {
//                    condition += " and (ac.ID not in (select c.account.ID from Customer c where c.company.companyID = ? ) and ac.ID not in (select v.account.ID from Vendor v where v.company.companyID = ? )) ";
//                    params.add(companyid);
//                    params.add(companyid);
//                }
//            }
//
//            boolean controlAccounts = false;
//            if (requestParams.containsKey("controlAccounts") && requestParams.get("controlAccounts") != null) {
//                controlAccounts = (Boolean) requestParams.get("controlAccounts");
//            }
//            if (!controlAccounts) {
//                params.add(controlAccounts);
//                condition += " and ac.controlAccounts=?";
//            }
//
//            if (requestParams.get("transactionCurrency") != null) {
//                condition += "and ac.currency.currencyID = ?";
//                params.add(requestParams.get("transactionCurrency"));
//            }
//            if (requestParams.get("ignorePaging") != null) {//    ERP-13570
//                 ignorePaging = Boolean.parseBoolean(requestParams.get("ignorePaging").toString()); 
//            }
//            String orderBy = "";
//            if (requestParams.containsKey("dir") && requestParams.containsKey("sort")) {
//                String Col_Name = requestParams.get("sort").toString();
//                String Col_Dir = requestParams.get("dir").toString();
//                orderBy = sortColumnAccount(Col_Name, Col_Dir);
//            } else {
//                orderBy = " order by ac.name";
//            }
//            
//            String query = "";
//            String usercondition = "";
//            String customdatajoin = "";
//
//              /**
//             * This Function will use when Users Visibility Feature is Enable
//             * Append user condition while querying data
//             */
//            if (requestParams.containsKey("isUserVisibilityFlow") && requestParams.get("isUserVisibilityFlow") != null) {
//                usercondition = (String) requestParams.get("appendusercondtion");
//                customdatajoin = " inner join ac.accAccountCustomData acd ";
//
//            }
//            query = "select ac from Account ac " + customdatajoin + " where ac.company.companyID=? " + condition + usercondition + orderBy;
//             
//            
//            
//            
//            List list = executeQuery( query, params.toArray());
//            int totalCount = list.size();
//             if (StringUtil.isNullOrEmpty(start) == false && StringUtil.isNullOrEmpty(limit) == false && !ignorePaging) {        
//                list = executeQueryPaging( query, params.toArray(),new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
//            }
//            result = new KwlReturnObject(true, null, null, list, totalCount);
//        } catch (Exception ex) {
//            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccounts:" + ex.getMessage(), null, null, 0);
//        }
//        return result;
//    }

    @Override
    public KwlReturnObject getAccountsForJE(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        int totalCount = 0;
        try {
            String start = (String) requestParams.get(Constants.start);
            String limit = (String) requestParams.get(Constants.limit);
            
            ArrayList accountParams = new ArrayList();
            ArrayList params = new ArrayList();
            
            String accountQuery = "";
            String accountTotalCountQuery = "";
            String accountCondition = "";
            String finalQuery = "";
            String totalCountQuery = "";
            
            String userCondition = "";
            String customDataJoin = "";
            
            String selectedBalPLId = "";
            if (requestParams.containsKey("selectedBalPLId") && requestParams.get("selectedBalPLId") != null) {
                selectedBalPLId = (String) requestParams.get("selectedBalPLId");
            }
            String[] nature = (String[]) requestParams.get("nature");
            String companyid = (String) requestParams.get("companyid");
            String recordids = (String) requestParams.get("recordids");
            int accountsortingflag=Integer.parseInt(requestParams.get("accountsortingflag").toString());
            boolean deleted = requestParams.get("deleted") != null ? Boolean.parseBoolean(requestParams.get("deleted").toString()) : false;
            boolean nondeleted = requestParams.get("nondeleted") != null ? Boolean.parseBoolean(requestParams.get("nondeleted").toString()) : false;
            boolean headerAdded = requestParams.get("headerAdded") != null ? Boolean.parseBoolean(requestParams.get("headerAdded").toString()) : false;
            boolean onlyBalancesheet = requestParams.get("onlyBalancesheet") != null ? Boolean.parseBoolean(requestParams.get("onlyBalancesheet").toString()) : false;
            boolean ignoreCustomers = requestParams.get("ignorecustomers") != null ? Boolean.parseBoolean(requestParams.get("ignorecustomers").toString()) : false;
            boolean ignoreVendors = requestParams.get("ignorevendors") != null ? Boolean.parseBoolean(requestParams.get("ignorevendors").toString()) : false;
            boolean ignoreGLAccounts = requestParams.get("ignoreGLAccounts") != null ? Boolean.parseBoolean(requestParams.get("ignoreGLAccounts").toString()) : false;
            boolean ignoreCashAccounts = requestParams.get("ignoreCashAccounts") != null ? Boolean.parseBoolean(requestParams.get("ignoreCashAccounts").toString()) : false;
            boolean ignoreBankAccounts = requestParams.get("ignoreBankAccounts") != null ? Boolean.parseBoolean(requestParams.get("ignoreBankAccounts").toString()) : false;
            boolean ignoreGSTAccounts = requestParams.get("ignoreGSTAccounts") != null ? Boolean.parseBoolean(requestParams.get("ignoreGSTAccounts").toString()) : false;
            boolean showGSTAndExpenseGLAccounts = false;
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            
            accountCondition += " WHERE acc.company = ? ";
            accountParams.add(companyid);
            
            if (nature != null) {
                String qMarks = "?";
                accountParams.add(5);
                for (int i = 0; i < nature.length; i++) {
                    qMarks += ", ? ";
                    accountParams.add(Integer.parseInt(nature[i]));
                }
                accountCondition += " AND grp.nature " + accountCondition + " IN (" + qMarks + ") ";
            }
            
            if(!StringUtil.isNullOrEmpty(recordids)) {
                accountCondition += " AND acc.id IN (" + recordids + ") ";
            }
            
            if (requestParams.containsKey("showGSTAndExpenseGLAccounts") && requestParams.get("showGSTAndExpenseGLAccounts") != null) {
                showGSTAndExpenseGLAccounts = (boolean) requestParams.get("showGSTAndExpenseGLAccounts");
            }
            boolean intercompanyflag = requestParams.get("intercompanyflag") != null ? Boolean.parseBoolean(requestParams.get("intercompanyflag").toString()) : false;

            if (ignoreGLAccounts) {
                accountCondition += " AND acc.mastertypeid != 1 ";
            }
            if (ignoreCashAccounts) {
                accountCondition += " AND acc.mastertypeid != 2 ";
            }
            if (ignoreBankAccounts) {
                accountCondition += " AND acc.mastertypeid != 3 ";
            }
            if (ignoreGSTAccounts) {
                accountCondition += " AND acc.mastertypeid != 4 ";
            }
            if (onlyBalancesheet) {
                accountCondition += " AND acc.accounttype = 1 ";   // for Showing the balance sheet type account in Customer AND Vendor creation
            }
            if (showGSTAndExpenseGLAccounts) {
                accountCondition += " AND (acc.mastertypeid = 4 OR (acc.mastertypeid = 1 AND grp.nature = 2)) ";     //ERP-33428
            }
            
            if (!StringUtil.isNullOrEmpty(selectedBalPLId)) {
                if (selectedBalPLId.equals("1")) {
                    accountCondition += " AND (grp.nature = 0 OR grp.nature = 1) ";
                }
                if (selectedBalPLId.equals("2")) {
                    accountCondition += " AND (grp.nature = 2 OR grp.nature = 3) ";
                }
            }
            boolean bankBook = requestParams.get("bankBook") != null ? Boolean.parseBoolean(requestParams.get("bankBook").toString()) : false;
            if (bankBook) {
                accountCondition += " AND acc.mastertypeid = 3 ";
            }

            if (requestParams.containsKey("isIBGTypeOnly") && requestParams.get("isIBGTypeOnly") != null) {
                accountCondition += " AND acc.ibgbank = ? ";
                accountParams.add((Boolean) requestParams.get("isIBGTypeOnly"));
            }
            
            String startDate = (String) requestParams.get(Constants.REQ_startdate);
            String endDate = (String) requestParams.get(Constants.REQ_enddate);
            if (!StringUtil.isNullOrEmpty(startDate) && !StringUtil.isNullOrEmpty(endDate)) {
                accountCondition += " AND acc.creationdate >= ? AND acc.creationdate <= ? ";
                accountParams.add(df.parse(startDate));
                accountParams.add(df.parse(endDate));
            }

            if (nondeleted) {
                accountCondition += " AND acc.deleteflag = 'F' ";
            } else if (deleted) {
                accountCondition += " AND acc.deleteflag = 'T' ";
            }
            if (!headerAdded) {
                accountCondition += " AND acc.isheaderaccount = false ";
            }
            if (intercompanyflag) {
                accountCondition += " AND acc.intercompanyflag = true ";
                if (requestParams.containsKey("intercompanytypeid") && !requestParams.get("intercompanytypeid").toString().equals("All")) {
                    accountCondition += " AND acc.intercompanytype = ? ";
                    accountParams.add(requestParams.get("intercompanytypeid"));
                }
            }
            
            if (requestParams.get("templateid") != null) {
                accountCondition += " AND acc.id NOT IN (SELECT grm.account FROM groupaccmap grm INNER JOIN layoutgroup lg ON lg.id = grm.layoutgroup WHERE lg.template = ? )";
                accountParams.add(requestParams.get("templateid"));
            }
            if (requestParams.get("ignoreTaggedAccounts") != null) {
                boolean ignoreTaggedAccounts = Boolean.parseBoolean((String) requestParams.get("ignoreTaggedAccounts"));
                if (ignoreTaggedAccounts) {
                    accountCondition += " AND (acc.id NOT IN (SELECT c.account FROM customer c WHERE c.company = ? ) AND acc.id NOT IN (SELECT v.account FROM vendor v WHERE v.company = ? )) ";
                    accountParams.add(companyid);
                    accountParams.add(companyid);
                }
            }
            
            if (requestParams.containsKey("query")) {
                String query = (String) requestParams.get("query");
                if (!StringUtil.isNullOrEmpty(query)) {
                    accountCondition += StringUtil.getSearchString(query, "AND", new String[]{"acc.name", "acc.acccode", "grp.name"});
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(accountParams, query, 3);
                        StringUtil.insertParamSearchString(SearchStringMap);
                    }
                }
            
            boolean controlAccounts = false;
            if (requestParams.containsKey("controlAccounts") && requestParams.get("controlAccounts") != null) {
                controlAccounts = (Boolean) requestParams.get("controlAccounts");
            }
            if (!controlAccounts) {
                accountParams.add(controlAccounts);
                accountCondition += " AND acc.controlaccounts = ? ";
            }

            if (requestParams.containsKey("ids") && requestParams.get("ids") != null) {
                String ids = requestParams.get("ids").toString();
                ids = AccountingManager.getFilterInString(ids);
                accountCondition += " AND acc.id IN ? ";
                accountParams.add(requestParams.get("ids"));
            }

            if (requestParams.get("transactionCurrency") != null) {
                accountCondition += " AND acc.currency = ? ";
                accountParams.add(requestParams.get("transactionCurrency"));
            }
            /**
             * query to fetch account using default account id
             */
            if (requestParams.containsKey("defaultaccountid") && requestParams.get("defaultaccountid") != null) {
                String defaultaccountid = (String) requestParams.get("defaultaccountid");
                accountCondition += " AND acc.defaultaccountid IN (" + defaultaccountid + ") ";
            }
            /**
             * This Function will use when Users Visibility Feature is Enable
             * Append user condition while querying data
             */
            if (requestParams.containsKey("isUserVisibilityFlow") && requestParams.get("isUserVisibilityFlow") != null) {
                userCondition = (String) requestParams.get("appendusercondtion");
                customDataJoin = " INNER JOIN accountcustomdata acd ON acd.accountId = acc.id ";
            }
            
            accountQuery = "SELECT acc.id AS accId, acc.acccode AS accCode, acc.name AS accName, acc.id AS mappedAccId, acc.mastertypeid AS masterTypeValue, acc.activate AS hasAccess, acc.usedin AS usedIn, acc.wanttopostje AS haveToPostJE, grp.name AS groupname, acc.currency AS currencyId, cr.symbol AS currencySymbol, 0 AS accountPersonType "
                    + " FROM account acc "
                    + " INNER JOIN accgroup grp ON grp.id = acc.groupname "
                    + " INNER JOIN currency cr ON cr.currencyid = acc.currency " + customDataJoin
                    + accountCondition + userCondition;
            
            accountTotalCountQuery = "SELECT COUNT(1) AS 'COUNT' "
                    + " FROM account acc "
                    + " INNER JOIN accgroup grp ON grp.id = acc.groupname "
                    + " INNER JOIN currency cr ON cr.currencyid = acc.currency " + customDataJoin
                    + accountCondition + userCondition;
            
            finalQuery += accountQuery;
            totalCountQuery += accountTotalCountQuery;
            params.addAll(accountParams);
            
            if (!ignoreCustomers) {
                String customerQuery = "";
                String customerTotalCountQuery = "";
                String customerCondition = "";

                ArrayList customerParams = new ArrayList();
                customerCondition += " WHERE c.company = ? ";
                customerParams.add(companyid);

                if (requestParams.containsKey("query")) {
                    String query = (String) requestParams.get("query");
                    if (!StringUtil.isNullOrEmpty(query)) {
                        customerCondition += StringUtil.getSearchString(query, "AND", new String[]{"c.name", "c.acccode", "grp.name"});
                        Map SearchStringMap = StringUtil.insertParamSearchStringMap(customerParams, query, 3);
                        StringUtil.insertParamSearchString(SearchStringMap);
                    }
                }

                if (requestParams.containsKey("accontid")) {
                    customerCondition += " AND c.account = ? ";
                    customerParams.add((String) requestParams.get("accontid"));
                }
                
                if (!StringUtil.isNullOrEmpty(recordids)) {
                    customerCondition += " AND c.id IN (" + recordids + ") ";
                }

                customerQuery = " SELECT c.id AS accId, c.acccode AS accCode, c.name AS accName, c.account AS mappedAccId, '' AS masterTypeValue, c.activate AS hasAccess, '' AS usedIn, '' AS haveToPostJE, grp.name AS groupname, c.currency AS currencyId, cr.symbol AS currencySymbol, 1 AS accountPersonType "
                        + " FROM customer c "
                        + " INNER JOIN account acc on acc.id = c.account "
                        + " INNER JOIN currency cr on cr.currencyid = c.currency "
                        + " INNER JOIN accgroup grp on grp.id = acc.groupname "
                        + customerCondition;
                
                customerTotalCountQuery = " SELECT COUNT(1) AS 'COUNT' "
                        + " FROM customer c "
                        + " INNER JOIN account acc on acc.id = c.account "
                        + " INNER JOIN currency cr on cr.currencyid = c.currency "
                        + " INNER JOIN accgroup grp on grp.id = acc.groupname "
                        + customerCondition;

                finalQuery += " UNION " + customerQuery;
                totalCountQuery += " UNION " + customerTotalCountQuery;
                params.addAll(customerParams);
            }
            
            if (!ignoreVendors) {
                String vendorQuery = "";
                String vendorTotalCountQuery = "";
                String vendorCondition = "";

                ArrayList vendorParams = new ArrayList();
                vendorCondition += " WHERE v.company = ? ";
                vendorParams.add(companyid);

                if (requestParams.containsKey("query")) {
                    String query = (String) requestParams.get("query");
                    if (!StringUtil.isNullOrEmpty(query)) {
                        vendorCondition += StringUtil.getSearchString(query, "AND", new String[]{"v.name", "v.acccode", "grp.name"});
                        Map SearchStringMap = StringUtil.insertParamSearchStringMap(vendorParams, query, 3);
                        StringUtil.insertParamSearchString(SearchStringMap);
                    }
                }

                if (requestParams.containsKey("accountid") && requestParams.containsKey("isAccActivateDeactivate")) {
                    if ((boolean) requestParams.get("isAccActivateDeactivate")) {
                        vendorCondition += " and v.account = ? ";
                        vendorParams.add((String) requestParams.get("accountid"));
                    }
                }
                
                if (!StringUtil.isNullOrEmpty(recordids)) {
                    vendorCondition += " AND v.id IN (" + recordids + ") ";
                }

                vendorQuery = " SELECT v.id AS accId, v.acccode AS accCode, v.name AS accName, v.account AS mappedAccId, '' AS masterTypeValue, v.activate AS hasAccess, '' AS usedIn, '' AS haveToPostJE, grp.name AS groupname, v.currency AS currencyId, cr.symbol AS currencySymbol, 2 AS accountPersonType "
                        + " FROM vendor v "
                        + " INNER JOIN account acc on v.account = acc.id "
                        + " INNER JOIN currency cr on cr.currencyid = v.currency "
                        + " INNER JOIN accgroup grp on grp.id = acc.groupname "
                        + vendorCondition;
                
                vendorTotalCountQuery = " SELECT COUNT(1) AS 'COUNT' "
                        + " FROM vendor v "
                        + " INNER JOIN account acc on v.account = acc.id "
                        + " INNER JOIN currency cr on cr.currencyid = v.currency "
                        + " INNER JOIN accgroup grp on grp.id = acc.groupname "
                        + vendorCondition;

                finalQuery += " UNION " + vendorQuery;
                totalCountQuery += " UNION " + vendorTotalCountQuery;
                params.addAll(vendorParams);
            }
            
            totalCountQuery = "SELECT SUM(COUNT) FROM (" + totalCountQuery + ") AS totalCount";
            if (accountsortingflag == 0) {
                finalQuery += " ORDER BY accName ASC ";
            } else {
                finalQuery += " ORDER BY accCode ASC ";
            }
            Calendar c1 = Calendar.getInstance();
            list = executeSQLQuery(totalCountQuery, params.toArray());
            Calendar c2 = Calendar.getInstance();
            System.out.println("time -> "+(c2.getTimeInMillis()-c1.getTimeInMillis()));
            if (list.size() > 0 && list.get(0) != null) {
                totalCount = Integer.parseInt(list.get(0).toString());
            }
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                c1 = Calendar.getInstance();
                list = executeSQLQueryPaging(finalQuery, params.toArray(), new Integer[]{Integer.parseInt(start), Integer.parseInt(limit)});
                c2 = Calendar.getInstance();
                System.out.println("time paging-> "+(c2.getTimeInMillis()-c1.getTimeInMillis()));
            } else {
                list = executeSQLQuery(finalQuery, params.toArray());
            }
        } catch (Exception ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, null, null, list, totalCount);
    }
    
    public KwlReturnObject getWarehouseIDByName(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
           ArrayList params = new ArrayList();
        try {
          String query = "from InventoryWarehouse inv where inv.name=? and inv.company.companyID=? ";
          params.add(requestParams.get("wareHouseName"));
          //params.add(requestParams.get("customer"));
          params.add(requestParams.get("companyid"));
           List list = executeQuery( query, params.toArray());
           int totalCount = list.size();
           result = new KwlReturnObject(true, null, null, list, totalCount);
         
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getWarehouseName:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

     public KwlReturnObject getStoreIDByName(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
           ArrayList params = new ArrayList();
        try {
          String query = "from Store str where str.abbreviation=? and str.company.companyID=? ";
          params.add(requestParams.get("storeName"));
          params.add(requestParams.get("companyid"));
           List list = executeQuery( query, params.toArray());
           int totalCount = list.size();
           result = new KwlReturnObject(true, null, null, list, totalCount);
         
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getStoreIDByName:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
     
    public KwlReturnObject getStoreForIsDefaultNot(String companyID) throws ServiceException {
        KwlReturnObject result;
        ArrayList params = new ArrayList();
        try {
            String query = "from Store str where str.defaultStore=false and str.company.companyID=? ";
            params.add(companyID);
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);

        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getStoreForIsDefaultNot:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public KwlReturnObject getLocationForIsDefaultNot(String companyID) throws ServiceException {
        KwlReturnObject result;
        ArrayList params = new ArrayList();
        try {
            String query = "from Location ltr where ltr.defaultLocation=false and ltr.company.companyID=? ";
            params.add(companyID);
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);

        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getLocationForIsDefaultNot:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
    
     public KwlReturnObject getStoreMasterData(String companyID) throws ServiceException {
        KwlReturnObject result;
        ArrayList params = new ArrayList();
        try {
            String query = "from StoreMaster str where str.company.companyID=? ";
            params.add(companyID);
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);

        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getStoreMasterData:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
         
      public KwlReturnObject getCustomerNameByCustomerCode(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
           ArrayList params = new ArrayList();
        try {
          String query = "from Customer str where str.acccode=? and str.company.companyID=? ";
          params.add(requestParams.get("customercode"));
          params.add(requestParams.get("companyid"));
           List list = executeQuery( query, params.toArray());
           int totalCount = list.size();
           result = new KwlReturnObject(true, null, null, list, totalCount);
         
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getCustomerNameByID:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
      
     public KwlReturnObject getAccountIDByCode(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        ArrayList params = new ArrayList();
        try {
            String query = "";
            if (requestParams.containsKey("accountCode") && !StringUtil.isNullOrEmpty((String) requestParams.get("accountCode"))) {
                query = "from Account acc where acc.acccode=? and acc.company.companyID=? ";
                params.add(requestParams.get("accountCode"));
                params.add(requestParams.get("companyid"));
            }else{
                query = "from Account acc where acc.name=? and acc.company.companyID=? ";
                params.add(requestParams.get("accountName"));
                params.add(requestParams.get("companyid"));
            } 
           
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);

        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccountIDByCode:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
    public KwlReturnObject getCustomerForCombo(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);
            String condition = " where c.company=? ";
            if (requestParams.containsKey("ss")) {
                String ss = (String) requestParams.get("ss");
                if (!StringUtil.isNullOrEmpty(ss)) {
                    condition += StringUtil.getSearchString(ss, "and", new String[]{"c.name"});
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(map);
                }
            }
            if (requestParams.containsKey("searchstartwith")) {
                // search on Customer code and name starting with
                String sw = (String) requestParams.get("searchstartwith");
                condition += " and c.acccode like '" + sw + "%' ";
            }
            if(requestParams.containsKey("accontid")){
                condition += " and c.account = ? ";
                params.add((String)requestParams.get("accontid"));
            }
            String query = "select c.id,c.name,c.account,c.acccode,c.currency,cr.symbol,cr.currencycode,cr.name,acc.deleteflag,acc.acccode,acc.groupname,ag.name,ag.nature,acc.creationdate from customer c "
                    + " inner join currency cr on cr.currencyid=c.currency inner join account acc on c.account = acc.id "
                    + " inner join accgroup ag on ag.id=acc.groupname "+condition+" order by c.name";
            List list = executeSQLQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccounts:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public KwlReturnObject getVendorForCombo(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            params.add(companyid);
            String condition = " where v.company=? ";
            if (requestParams.containsKey("ss")) {
                String ss = (String) requestParams.get("ss");
                if (!StringUtil.isNullOrEmpty(ss)) {
                    condition += StringUtil.getSearchString(ss, "and", new String[]{"v.name"});
                    Map map = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(map);
                }
            }
            if (requestParams.containsKey("searchstartwith")) {
                // search on Customer code and name starting with 
                String sw = (String) requestParams.get("searchstartwith");
                condition += " and v.acccode like '" + sw + "%' ";
            }
            if (requestParams.containsKey("accountid") && requestParams.containsKey("isAccActivateDeactivate")) {
                if ((boolean) requestParams.get("isAccActivateDeactivate")) {
                    condition += " and v.account = ? ";
                    params.add((String) requestParams.get("accountid"));
                }
            }
            String query = "select v.id,v.name,v.account,v.acccode,v.currency,cr.symbol,cr.currencycode,cr.name,acc.deleteflag,acc.acccode,acc.groupname,ag.name,ag.nature,acc.creationdate from vendor v "
                    + "inner join currency cr on cr.currencyid=v.currency inner join account acc on v.account = acc.id  "
                    + "inner join accgroup ag on ag.id=acc.groupname "+condition+"  order by v.name";
            List list = executeSQLQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccounts:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public KwlReturnObject getLayoutGroupsFortotalgroupmap(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            String condition = "";
            params.add(companyid);
            if (requestParams.containsKey("totalgroupid") && requestParams.get("totalgroupid") != null) {
                String totalgroupid = (String) requestParams.get("totalgroupid");
                params.add(totalgroupid);
                params.add(totalgroupid);

                    condition += " and (ID != ? and ID not in (select groupid.ID from Groupmapfortotal where groupidtotal.ID = ?)) ";
                }
            if (requestParams.containsKey("sequence_from") && requestParams.get("sequence_from") != null) {
                int sequence = Integer.parseInt(requestParams.get("sequence_from").toString());
                params.add(sequence);
                condition += " and sequence < ? ";
            }
            if (requestParams.containsKey("templateid") && requestParams.get("templateid") != null) {
                params.add(requestParams.get("templateid"));
                condition += " and template.ID = ? ";
            }

            String query = "from LayoutGroup where company.companyID=? " + condition + " and (parent is null or (nature != 6 and nature != 7 and nature != 8 and nature != 9))  order by sequence";
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccounts:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
    
    public KwlReturnObject getDefaultLayoutGroupsFortotalgroupmap(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            ArrayList params = new ArrayList();
            String condition = "";
            if (requestParams.containsKey("totalgroupid") && requestParams.get("totalgroupid") != null) {
                String totalgroupid = (String) requestParams.get("totalgroupid");
                params.add(totalgroupid);
                params.add(totalgroupid);

                condition += condition.length()<=0 ? " where " : " and ";
                condition += " (ID != ? and ID not in (select groupid.ID from Groupmapfortotal where groupidtotal.ID = ?)) ";
            }
            if (requestParams.containsKey("sequence_from") && requestParams.get("sequence_from") != null) {
                int sequence = Integer.parseInt(requestParams.get("sequence_from").toString());
                params.add(sequence);
                
                condition += condition.length()<=0 ? " where " : " and ";
                condition += " sequence < ? ";
            }
            if (requestParams.containsKey("templateid") && requestParams.get("templateid") != null) {
                params.add(requestParams.get("templateid"));
                
                condition += condition.length()<=0 ? " where " : " and ";
                condition += " template.ID = ? ";
            }

            String query = "from DefaultLayoutGroup " + condition + " order by sequence";
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getDefaultLayoutGroupsFortotalgroupmap:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public KwlReturnObject getMappedLayoutGroupsforgrouptotal(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            String totalgroupid = (String) requestParams.get("totalgroupid");
            params.add(totalgroupid);
            params.add(companyid);


            String query = "from Groupmapfortotal where groupidtotal.ID = ? and  groupidtotal.company.companyID=? order by groupid.sequence";
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccounts:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public KwlReturnObject getMappedDefaultLayoutGroupsforgrouptotal(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            ArrayList params = new ArrayList();
            String totalgroupid = (String) requestParams.get("totalgroupid");
            params.add(totalgroupid);

            String query = "from DefaultGroupMapForTotal where groupidtotal.ID = ? order by groupid.sequence";
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getMappedDefaultLayoutGroupsforgrouptotal:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public KwlReturnObject getAccountsFormappedPnL(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get("companyid");
            String templateid = (String) requestParams.get("templateid");

            Templatepnl templatepnl = (Templatepnl) get(Templatepnl.class, templateid);

            int templateMapid = templatepnl.getTemplateid();
            int isincome = 0;
            if (requestParams.containsKey("isincome")) {
                condition = " isincome = ? and ";
                isincome = (Integer) requestParams.get("isincome");

                params.add(isincome);
            }

            if (requestParams.containsKey("accountid")) {
                condition = " account.ID = ? and ";

                params.add((String) requestParams.get("accountid"));
            }

            params.add(templateMapid);
            params.add(companyid);

            String query = "from PnLAccountMap where " + condition + " templateid = ? and company.companyID=?  order by account.name";
            List list = executeQuery( query, params.toArray());

            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccounts:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public KwlReturnObject getAccountsForLayoutGroup(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            ArrayList params = new ArrayList();
            String groupid = (String) requestParams.get("groupid");
            String companyid = (String) requestParams.get("companyid");

            params.add(groupid);
            params.add(companyid);

            String query = "from GroupAccMap where layoutgroup.ID = ? and company.companyID=?  order by account.acccode+'0'";
            List list = executeQuery( query, params.toArray());

            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccounts:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public KwlReturnObject getAccountsForDefaultLayoutGroup(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            ArrayList params = new ArrayList();
            String groupid = (String) requestParams.get("groupid");

            params.add(groupid);

            String query = "from DefaultGroupAccMap where defaultlayoutgroup.ID = ? ";
            List list = executeQuery(query, params.toArray());

            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getAccountsForDefaultLayoutGroup:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public KwlReturnObject getDefaultAccountFromName(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            ArrayList params = new ArrayList();
            String accountname = (String) requestParams.get("accountname");
            String groupname = (String) requestParams.get("groupname");
            
            String condition = "";
            params.add(accountname);
            if(!StringUtil.isNullOrEmpty(groupname)){
                condition += " and group.name = ? ";
                params.add(groupname);
            }

            String query = "from DefaultAccount where name = ? " + condition;
            List list = executeQuery(query, params.toArray());

            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accAccountDAOImpl.getDefaultAccountFromName:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }

    public ArrayList getAccountArrayList(List list, HashMap<String, Object> requestParams, boolean quickSearchFlag, boolean noactivityAcc) throws ServiceException {
        ArrayList resultlist = new ArrayList();
        boolean ignoreCustomers = requestParams.get("ignorecustomers") != null;
        boolean ignoreVendors = requestParams.get("ignorevendors") != null;
        String excludeaccountid = (String) requestParams.get("accountid");
        String includeaccountid = (String) requestParams.get("includeaccountid");
        String includeparentid = (String) requestParams.get("includeparentid");
        String customerCpath = ConfigReader.getinstance().get("Customer");
        String vendorCpath = ConfigReader.getinstance().get("Vendor");
        boolean deleted = Boolean.parseBoolean((String) requestParams.get("deleted"));
        boolean nondeleted = Boolean.parseBoolean((String) requestParams.get("nondeleted"));
//        boolean getSundryCustomer = Boolean.parseBoolean((String) requestParams.get("getSundryCustomer"));
//        boolean getSundryVendor = Boolean.parseBoolean((String) requestParams.get("getSundryVendor"));
        boolean generalLedgerFlag = false;
        boolean showChildAccountsInGl = false;
        boolean includeExcludeChildBalances = true;
        if (requestParams.containsKey("generalLedgerFlag") && requestParams.get("generalLedgerFlag") != null) {
            generalLedgerFlag = Boolean.parseBoolean(requestParams.get("generalLedgerFlag").toString());
        }
        if (requestParams.containsKey("showChildAccountsInGl") && requestParams.get("showChildAccountsInGl") != null) {
            showChildAccountsInGl = Boolean.parseBoolean(requestParams.get("showChildAccountsInGl").toString());
        }
        if (requestParams.containsKey("includeExcludeChildBalances") && requestParams.get("includeExcludeChildBalances") != null) {
            includeExcludeChildBalances = Boolean.parseBoolean(requestParams.get("includeExcludeChildBalances").toString());
        }
        requestParams.put("quickSearchFlag",quickSearchFlag);
        String currencyid = (String) requestParams.get("currencyid");
        KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, currencyid);
        Map<String,Object[]> accountOccuranceMap = new HashMap<String,Object[]>();
        if (list.size() > 0) {
            Iterator itr = list.iterator();
            int level = 0;
            boolean isExciseApplicable = false;
            if(requestParams.containsKey("isExciseApplicable") && requestParams.get("isExciseApplicable") !=null){
                isExciseApplicable = Boolean.parseBoolean(requestParams.get("isExciseApplicable").toString());
            }
            while (itr.hasNext()) {
                Object listObj = itr.next();
                Account account = (Account) listObj;
                if(!isExciseApplicable && account.getAccountName().equals("PLA (Personal Ledger Account) ")){
                    continue;
                }
                if (excludeaccountid != null && account.getID().equals(excludeaccountid)) {
                    continue;
                }
                if ((includeparentid != null && (!account.getID().equals(includeparentid) || (account.getParent() != null && !account.getParent().getID().equals(includeparentid))))) {
                    continue;
                } else if ((includeaccountid != null && !account.getID().equals(includeaccountid))) {
                    continue;
                }

                Object c,v;
                try {
                    c = get(Class.forName(customerCpath), account.getID());

                    v = get(Class.forName(vendorCpath), account.getID());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE(ex.getMessage(), ex);
                }

//To do - Need to check for customer and vendor path
                Object tmplist[] = new Object[8];
                tmplist[0] = listObj;
                tmplist[1] = c;
                tmplist[2] = v;
                tmplist[3] = level;
                if (quickSearchFlag && generalLedgerFlag && showChildAccountsInGl) {
                    if (!accountOccuranceMap.containsKey(account.getID())) {
                        resultlist.add(tmplist);
                        accountOccuranceMap.put(account.getID(), tmplist);
                    }
                } else {
                    resultlist.add(tmplist);
                }
                int resultListSize = resultlist.size();
                if (quickSearchFlag) {
                    boolean isLeaf = true;
                    if (generalLedgerFlag && showChildAccountsInGl) { // SAME CHECK FOR QUICK SEARCH ALSO SDP-6161
                        /*
                         * if "showChildAccountsInGl" is true then we have to show child account in the hierarchy of parent account.
                         * Child account is not fetched from the query so we are iterating the child accounts and add them to resultset.
                         */
                        isLeaf = getChildAccounts(account, resultlist, level, excludeaccountid, includeaccountid, ignoreCustomers, ignoreVendors, currency, customerCpath, vendorCpath, nondeleted, deleted, tmplist, noactivityAcc,accountOccuranceMap,requestParams);
                    }
               
                    tmplist[4] = isLeaf;
                              if (noactivityAcc && (!checkInActiveAccounts(account))) {
                        resultlist.remove(tmplist);
                    }
                } else {
                    boolean isLeaf = false;
                    if (generalLedgerFlag) {
                        /*
                         * if "showChildAccountsInGl" is true then we have to show child account in the hierarchy of parent account.
                         * Child account is not fetched from the query so we are iterating the child accounts and add them to resultset.
                         */
                        if (showChildAccountsInGl) {
                            isLeaf = getChildAccounts(account, resultlist, level, excludeaccountid, includeaccountid, ignoreCustomers, ignoreVendors, currency, customerCpath, vendorCpath, nondeleted, deleted, tmplist, noactivityAcc,accountOccuranceMap,requestParams);
                        }
                    } else {
                        isLeaf = getChildAccounts(account, resultlist, level, excludeaccountid, includeaccountid, ignoreCustomers, ignoreVendors, currency, customerCpath, vendorCpath, nondeleted, deleted, tmplist, noactivityAcc,accountOccuranceMap,requestParams);
                    }
                    if (noactivityAcc && (!account.getChildren().isEmpty()) && resultlist.size() == resultListSize) {
                        resultlist.remove(tmplist);
                    }
                    tmplist[4] = isLeaf;
                }
                tmplist[5] = currency;
                tmplist[6] = account.getParent();//Parent Account  //issueERP-9388 in quick serach parent accout noyt setted
                tmplist[7] = isPosted(account);
            }
        }
        return resultlist;
    }

    @Override
    public KwlReturnObject getJEDTrasactionfromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.company.companyID=?";
        list = executeQuery( q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject updateHeaderaccountField(String accountid, boolean flag) throws ServiceException {
        try {
            Account account = (Account) get(Account.class, accountid);
            account.setHeaderaccountflag(false);
            update(account);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, null, 0);
    }

    @Override
    public void updateAccountTemplateCode(String accountid, String companyid) throws ServiceException {
        try {
            int templateCode = 0;
            String query = "from PnLAccountMap where account.ID = ? and  company.companyID=?";
            List list = executeQuery( query, new Object[]{accountid, companyid});

            Iterator it = list.iterator();
            while (it.hasNext()) {
                PnLAccountMap obj = (PnLAccountMap) it.next();
                templateCode += (Math.pow(2, obj.getTemplateid()));
            }

            Account account = (Account) get(Account.class, accountid);
            account.setTemplatepermcode(Long.parseLong(String.valueOf(templateCode)));
            update(account);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    @Override
    public void saveAssetHistory(String assetId, String details, Date auditTime, String ipAddress, String userId) throws ServiceException {
        try {
            Account account = null;
            User user = null;
            if (!StringUtil.isNullOrEmpty(assetId)) {
                account = (Account) get(Account.class, assetId);
            }
            if (!StringUtil.isNullOrEmpty(userId)) {
                user = (User) load(User.class, userId);
            }
            AssetHistory assetHistory = new AssetHistory();
            assetHistory.setAccount(account);
            assetHistory.setAuditTime(auditTime);
            assetHistory.setDetails(details);
            assetHistory.setIPAddress(ipAddress);
            assetHistory.setUser(user);
            saveOrUpdate(assetHistory);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject getAssetHistory(HashMap<String, Object> requestParams) throws ServiceException {
        int start = 0;
        int limit = 30;
        String assetId = "";
        List list = new ArrayList();
        ArrayList al = new ArrayList();
        assetId = requestParams.get("assetId").toString();
        al.add(assetId);
        if (requestParams.containsKey("start") && requestParams.containsKey("limit") && !StringUtil.isNullOrEmpty(requestParams.get("start").toString())) {
            start = Integer.parseInt(requestParams.get("start").toString());
            limit = Integer.parseInt(requestParams.get("limit").toString());
        }
        String query = "from AssetHistory ash where ash.account.ID=?";
        if (!StringUtil.isNullOrEmpty(assetId)) {
            list = executeQueryPaging( query, al.toArray(), new Integer[]{start, limit});
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public List saveMasterItemDataSequence(JSONArray jsonArray, String groupid, String companyid) throws ServiceException {
       List list=new ArrayList();
       try{
            String id="";
            String name="";
            int itemSequence=0;
            String query="";
            List value = new ArrayList();
            String fieldlabel = "";
            FieldParams fieldParams = (FieldParams)load(FieldParams.class, groupid);
            if(fieldParams!=null){
                fieldlabel = fieldParams.getFieldlabel();
                List ll=new ArrayList();
                query = " select id from FieldParams fp where fp.fieldlabel = ? and fp.company.companyID = ? ";
                value.clear();
                value.add(fieldlabel);
                value.add(companyid);
                ll = executeQuery( query, value.toArray());
                Iterator ite = ll.iterator();
                while (ite.hasNext()) {
                    Object listObj = ite.next();
                    if(listObj!=null){
                        String field = listObj.toString();
                        query = " from FieldComboData fcd where fcd.field.id = ? ";
                        value.clear();
                        value.add(field);
                        List ll1 = new ArrayList();
                        ll1 = executeQuery( query, value.toArray());
                        Iterator itr = ll1.iterator();
                        while (itr.hasNext()) {
                            Object listObj1 = itr.next();
                            FieldComboData fieldComboData = (FieldComboData) listObj1;
                            for(int i=0; i<jsonArray.length();i++){
                                JSONObject obj = jsonArray.getJSONObject(i);               
                                id = obj.getString("id");
                                name = obj.getString("name");
                                itemSequence = obj.getInt("itemsequence");
                                if(name.equals(fieldComboData.getValue())){
                                    fieldComboData.setItemsequence(itemSequence);
                                    saveOrUpdate(fieldComboData);
                                    list.add(fieldComboData);
                                }
                            }
                        }
                    }
                }
            }
       } catch(Exception ex){
            throw ServiceException.FAILURE(ex.getMessage(), ex);
       }
       
       return list;
    }

    @Override
    public KwlReturnObject getFieldComboDatabyFieldID(String fieldid, String companyid) throws ServiceException {
        List list = new ArrayList();
        ArrayList filter_params = new ArrayList();
        try {
            filter_params.add(fieldid);
            filter_params.add(companyid);
            String query = "from FieldComboData fcd where fcd.field.id = ? and fcd.field.company.companyID= ? and  fcd.deleteflag=0";
            list = executeQuery( query, filter_params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);

        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
       @Override
    public KwlReturnObject getPropagatedAccounts(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            String condition = "";
            String propagatedCustomerID = requestParams.containsKey("propagatedAccountID") ? (String) requestParams.get("propagatedAccountID") : "";

            condition += "  ac.deleted=false ";
            if (!StringUtil.isNullOrEmpty(propagatedCustomerID)) {
                condition += " and ac.propagatedAccountID.ID=? ";
                params.add(propagatedCustomerID);
            }
            String query = "select ac from Account ac where  " + condition;
            list = executeQuery(query, params.toArray());

            result = new KwlReturnObject(true, null, null, list, list.size());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDaoimpl.getPropagatedAccounts :" + ex.getMessage(), ex);
        }
        return result;
    }

   /* Get custom field "field label" wise */
    @Override
    public List getFieldParamsFieldLabelWise(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;

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
            
            /* To fetch Product custom field used in Invoice*/
            if (requestParams.get("isFromProduct") != null) {
                hql += " and relatedmoduleid like '%2%'";

            }
            list = executeQuery(hql, value.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDaoimpl.getFieldParamsFieldLabelWise :" + ex.getMessage(), ex);
        }
        return list;
    }

    private class accountComp implements Comparator<Account> {

        private boolean sortOnType;
        private boolean directionDesc;

        private accountComp(boolean sortOnType1, boolean direction1) {
            sortOnType = sortOnType1;
            directionDesc = direction1;
        }

        @Override
        public int compare(Account o1, Account o2) {
            String o1_Code = (o1.getAcccode() == null) ? "" : o1.getAcccode();
            String o2_Code = (o2.getAcccode() == null) ? "" : o2.getAcccode();

            if (sortOnType) {
                if (StringUtil.equal(o1.getGroup().getName(), o2.getGroup().getName())) {
                    if (StringUtil.equal(o1_Code, o2_Code)) {
                        return directionDesc ? o2.getName().compareTo(o1.getName()) : o1.getName().compareTo(o2.getName());
                    } else {
                        return directionDesc ? o2_Code.compareTo(o1_Code) : o1_Code.compareTo(o2_Code);
                    }
                } else {
                    return directionDesc ? o2.getGroup().getName().compareTo(o1.getGroup().getName()) : o1.getGroup().getName().compareTo(o2.getGroup().getName());
                }
            } else {
                if (StringUtil.equal(o1_Code, o2_Code)) {
                    return directionDesc ? o2.getName().compareTo(o1.getName()) : o1.getName().compareTo(o2.getName());
                } else {
                    return directionDesc ? o2_Code.compareTo(o1_Code) : o1_Code.compareTo(o2_Code);
                }
            }
        }
    }

    public boolean getChildAccounts(Account account, List resultlist, int level, String excludeaccountid, String includeaccountid, boolean ignoreCustomers, boolean ignoreVendors, KWLCurrency currency, String customerCpath, String vendorCpath, boolean nondeleted, boolean deleted, Object tmplist1[], boolean noactivityAcc,Map<String,Object[]> accountOccuranceMap,HashMap<String, Object> requestParams) throws ServiceException {
        boolean leaf = true;
        //Iterator<Account> itr = new TreeSet(account.getChildren()).iterator();
//        List<Account> ll = new ArrayList(account.getChildren());//Added code for sorting on account code, account name
        
        boolean generalLedgerFlag = false;
        boolean showChildAccountsInGl = false;
        boolean quickSearchFlag = false;
        if (requestParams.containsKey("generalLedgerFlag") && requestParams.get("generalLedgerFlag") != null) {
            generalLedgerFlag = Boolean.parseBoolean(requestParams.get("generalLedgerFlag").toString());
        }
        if (requestParams.containsKey("showChildAccountsInGl") && requestParams.get("showChildAccountsInGl") != null) {
            showChildAccountsInGl = Boolean.parseBoolean(requestParams.get("showChildAccountsInGl").toString());
        }
        if (requestParams.containsKey("quickSearchFlag") && requestParams.get("quickSearchFlag") != null) {
            quickSearchFlag = Boolean.parseBoolean(requestParams.get("quickSearchFlag").toString());
        }
        String q = "from Account where parent=?";
        List<Account> ll = executeQuery( q, account);
        if(ll != null && !ll.isEmpty()) {
            Collections.sort(ll, new accountComp(sortOnType, directionDesc));
        }
        
        Collections.sort(ll, new accountComp(sortOnType, directionDesc));

        level++;
        for (Account child : ll) {
            if ((excludeaccountid != null && child.getID().equals(excludeaccountid)) || (child.isDeleted() && nondeleted) || (!child.isDeleted() && deleted)) {
                continue;
            }
            if ((includeaccountid != null && !child.getID().equals(includeaccountid) || (child.isDeleted() && nondeleted) || (!child.isDeleted() && deleted))) {
                continue;
            }
            Object c,v;
            try {
                c = get(Class.forName(customerCpath), account.getID());

                v = get(Class.forName(vendorCpath), account.getID());
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }

            leaf = false;
//To do - Need to check customer and vendor class path dependency.
            Object tmplist[] = new Object[8];
            tmplist[0] = child;
            tmplist[1] = c;
            tmplist[2] = v;
            tmplist[3] = level;
            if (quickSearchFlag && generalLedgerFlag && showChildAccountsInGl) {
                if (!accountOccuranceMap.containsKey(child.getID())) {
                    accountOccuranceMap.put(child.getID(), tmplist);
                    resultlist.add(tmplist);
                } else {
                    resultlist.remove(accountOccuranceMap.get(child.getID()));
                    accountOccuranceMap.put(child.getID(), tmplist);
                    resultlist.add(tmplist);
                }
            } else {
                resultlist.add(tmplist);
            }
            int resultListSize = resultlist.size();
            tmplist[4] = getChildAccounts(child, resultlist, level, excludeaccountid, excludeaccountid, ignoreCustomers, ignoreVendors, currency, customerCpath, vendorCpath, nondeleted, deleted, tmplist, noactivityAcc,accountOccuranceMap,requestParams);
            if (noactivityAcc && (!child.getChildren().isEmpty()) && resultlist.size() == resultListSize) {
                resultlist.remove(tmplist);
            }
            tmplist[5] = currency;
            tmplist[6] = account;//Parent Account
            tmplist[7] = isPosted(child);
        }
        if (noactivityAcc && leaf == true && (!checkInActiveAccounts(account) || account.isDeleted())) {
            resultlist.remove(tmplist1);
        }
        return leaf;
    }

    @Override
    public boolean checkInActiveAccounts(Account account) throws ServiceException {
        boolean isInactive = true;
        try {
            isInactiveAccount(account.getID(), account.getCompany().getCompanyID());
            isInactive = true;
        } catch (AccountingException ex) {
            isInactive = false;
        }
        return isInactive;
    }

    public KwlReturnObject getGroups(HashMap<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result;
        try {
            String groupid = (String) requestParams.get("groupid");
            boolean ignoreCustomer = StringUtil.getBoolean((String) requestParams.get("ignorecustomers"));
            boolean ignoreVendor = StringUtil.getBoolean((String) requestParams.get("ignorevendors"));
            String companyid = (String) requestParams.get("companyid");
            String[] groups = (String[]) requestParams.get("group");
            String ignoreGrp = (String) requestParams.get("ignore");
            String[] nature = (String[]) requestParams.get("nature");
            boolean isMasterGroup = Boolean.parseBoolean((String) requestParams.get("isMasterGroup"));
            boolean defaultgroup = false;
            defaultgroup = Boolean.parseBoolean((String) requestParams.get("defaultgroup"));
            boolean isGroupDetailReport = false; // "TRUE" for Group Detail Report
            boolean includeChildAccounts = true; // Do not include child group for Group Detail Report 
            if (requestParams.containsKey(Constants.IS_GROUP_DETAIL_REPORT) && !StringUtil.isNullOrEmpty(requestParams.get(Constants.IS_GROUP_DETAIL_REPORT).toString())) {
                isGroupDetailReport = Boolean.parseBoolean(requestParams.get(Constants.IS_GROUP_DETAIL_REPORT).toString());
            }
            
             /*
             * Country id is passed in case of Company set up
             */
            int countryid=0;
            if (requestParams.containsKey("country")) {
                countryid= Integer.parseInt((String) requestParams.get("country"));
            }
            String condition = "";
            ArrayList params = new ArrayList();
            ignoreGrp = (ignoreGrp == null ? "" : " not ");
            if (groups != null) {
                String qMarks = "?";
                params.add("null");
                for (int i = 0; i < groups.length; i++) {
                    qMarks += ",?";
                    params.add(groups[i]);
                }
                condition = " and ID " + ignoreGrp + " in (" + qMarks + ") ";
            } else if (nature != null) {
                String qMarks = "?";
                params.add(5);//not a nature
                for (int i = 0; i < nature.length; i++) {
                    qMarks += ",?";
                    params.add(Integer.parseInt(nature[i]));
                }
                condition = " and nature in (" + qMarks + ") ";
            }
            //To do - Need to find out its usage and test.
//            if(ignoreCustomer)
//                condition+=" and ID != '"+Group.ACCOUNTS_RECEIVABLE+"'";
//            if(ignoreVendor)
//                condition+=" and ID != '"+Group.ACCOUNTS_PAYABLE+"'";
            String orderBy = " order by name ";
            if (requestParams.containsKey("dir") && requestParams.containsKey("sort")) {
                String Col_Name = requestParams.get("sort").toString();
                String Col_Dir = requestParams.get("dir").toString();
                orderBy = sortGroupsColumn(Col_Name, Col_Dir);
            }
            String query = "";
            if (isMasterGroup) {
                params.add(companyid);
//                query = "from Group where deleted=false and company.companyID=?  and isMasterGroup=true "+orderBy;
                query = "from Group where company.companyID=?  and isMasterGroup=true "+orderBy;
            } else {

                if (defaultgroup) {//Called in company setup wizard
//                    query = "from Group where deleted=false and parent is null " + condition + " and company is null "+orderBy;
                        if (countryid == Constants.indian_country_id) {
                            query = "from Group where  parent is null " + condition + " and id not in(28,29) and company is null " + orderBy;

                        } else {
                            query = "from Group where  parent is null " + condition + " and company is null " + orderBy;
                        }             
                    } else if(isGroupDetailReport){
                    params.add(companyid);
                    query = "from Group where parent is null " + condition + " and  (company is null or company.companyID=?) " + orderBy;
                    includeChildAccounts = false;
                } else {//Called to load account group combo and group master grid.
                    params.add(companyid);
//                    query = "from Group where deleted=false " + condition + " and parent is null and  company.companyID=? "+orderBy;
                    query = "from Group where parent is null " + condition + " and  company.companyID=? "+ orderBy;
                }
            }
            List list = executeQuery( query, params.toArray());
            Iterator itr = list.iterator();
            List resultlist = new ArrayList();
            int level = 0;
            while (itr.hasNext()) {
                Object listObj = itr.next();
                Group group = (Group) listObj;
                if (group.getID().equals(groupid)) {
                    continue;
                }

                Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
                tmplist[0] = listObj;
                tmplist[1] = level;
                resultlist.add(tmplist);
                tmplist[2] = (!isMasterGroup) ? (includeChildAccounts ? getChildGroups(group, resultlist, level, groupid, companyid) : true) : true; //For each master Group Leaf is true
                tmplist[3] = null;//parent group

            }
            result = new KwlReturnObject(true, null, null, resultlist, resultlist.size());
        } catch (Exception ex) {
            result = new KwlReturnObject(false, "accCustomerDAOImpl.getGroups:" + ex.getMessage(), null, null, 0);
        }
        return result;
    }
    
    public String sortGroupsColumn(String Col_Name, String Col_Dir) throws ServiceException {
        String orderBy = "";
        if (Col_Name.equals("groupname")) {
            orderBy = " order by name " + Col_Dir;
        } else if (Col_Name.equals("affectgp")) {
            orderBy = " order by affectGrossProfit " + Col_Dir;
        }
        return orderBy;
    }

    public boolean getChildGroups(Group group, List resultlist, int level, String groupid, String companyid) throws ServiceException {
        boolean leaf = true;
        Iterator<Group> itr = getChildrenAccount(group, companyid);
        level++;
        while (itr.hasNext()) {
            Object listObj = itr.next();
            Group child = (Group) listObj;
            Company company = child.getCompany();
//            if ((company != null && !company.getCompanyID().equals(companyid)) || child.getID().equals(groupid) || child.isDeleted()) 
            if ((company != null && !company.getCompanyID().equals(companyid)) || child.getID().equals(groupid) ) {
                continue;
            }
            leaf = false;

            Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
            tmplist[0] = listObj;
            tmplist[1] = level;
            resultlist.add(tmplist);
            tmplist[2] = getChildGroups(child, resultlist, level, groupid, companyid);
            tmplist[3] = group;//parent group
        }
        return leaf;
    }

    public Iterator<Group> getChildrenAccount(Group group, String companyid) throws ServiceException {
//        String selQuery = "from Group where deleted=false and parent.ID =? and company.companyID=?";
        String selQuery = "from Group where parent.ID =? and company.companyID=?";
        ArrayList params = new ArrayList();
        params.add(group.getID());
        params.add(companyid);
        List list = executeQuery( selQuery, params.toArray());
        Iterator<Group> itrgroup = list.iterator();
        return itrgroup;
    }

    public void updateChildrenAccount(Account account) throws ServiceException {
        Set<Account> children = account.getChildren();
        if (children == null) {
            return;
        }
        Iterator<Account> itr = children.iterator();
        while (itr.hasNext()) {
            Account child = itr.next();
            child.setGroup(account.getGroup());
            updateChildrenAccount(child);
        }
    }

    public KwlReturnObject deleteAccount(HashMap request, String companyid) throws ServiceException {
        KwlReturnObject result = null;
        String selQuery = "";
        ArrayList params1 = new ArrayList();
        ArrayList params2 = new ArrayList();
        ArrayList params3 = new ArrayList();
        try {
            JSONArray jArr = new JSONArray((String) request.get("data"));
            String qMarks = "";
            Company company = (Company) get(Company.class, companyid);
            for (int j = 0; j < 5; j++) {
                qMarks = "";
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (!StringUtil.isNullOrEmpty(jobj.getString("accid"))) {
                        if (j == 0) {
                            params1.add(jobj.getString("accid"));
                        }
                        if (j <= 1) {
                            params3.add(jobj.getString("accid"));
                        }
                        params2.add(jobj.getString("accid"));
                        qMarks += "?,";
                    }
                }
                qMarks = qMarks.substring(0, Math.max(0, qMarks.length() - 1));

            }
            params1.add(company.getCompanyID());
            params2.add(company.getCompanyID());
            params3.add(company.getCompanyID());
            selQuery = "from JournalEntryDetail jed where account.ID in( " + qMarks + ") and jed.company.companyID=?";
            List list = executeQuery( selQuery, params1.toArray());
            int count = list.size();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
            }
            selQuery = "from Product pr where (purchaseAccount.ID in ( " + qMarks + ") or salesAccount.ID in ( " + qMarks + ") ) and pr.company.companyID=?";
            list = executeQuery( selQuery, params3.toArray());
            count = list.size();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Account Preferences. So it cannot be deleted.");
            }
            selQuery = "from CompanyAccountPreferences acp where (discountGiven.ID in ( " + qMarks + ") or discountReceived.ID in ( " + qMarks + ") or shippingCharges.ID in ( " + qMarks + ")  or cashAccount.ID in ( " + qMarks + ")) and acp.company.companyID=?";// (discountGiven.ID in ( "+qMarks +")
            list = executeQuery( selQuery, params2.toArray());
            count = list.size();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Product(s). So it cannot be deleted.");
            }
            selQuery = "from PaymentMethod pm where account.ID in ( " + qMarks + ")  and pm.company.companyID=?";
            list = executeQuery( selQuery, params1.toArray());
            count = list.size();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Term(s). So it cannot be deleted.");
            }
            selQuery = "from Tax t where account.ID in ( " + qMarks + ")  and t.company.companyID=?";
            list = executeQuery( selQuery, params1.toArray());
            count = list.size();
            if (count > 0) {
                throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
            }

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("accid")) == false) {
                    if (jobj.getDouble("openbalance") != 0) {
                        throw new AccountingException("Selected record(s) is having the Opening Balance. So it cannot be deleted");
                    } else {
                        Account account = (Account) get(Account.class, jobj.getString("accid"));
                        //account.setDeleted(true);
                        // update(account);
                        result = deleteAccount(account.getID(), companyid);

                    }
                }
            }
        } catch (AccountingException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Selected record(s) is currently used in the transaction(s).", ex);
        }
        return result;
    }

    
    @Override
    public KwlReturnObject activateDeactivateAccounts(HashMap request) throws ServiceException{
        List list = new ArrayList();
        String msg="";
        boolean isSuccess = true;
        StringBuilder usedAcc = new StringBuilder();
        StringBuilder accountCompleted = new StringBuilder();
        try {
            Map<String,String> usedIn = new HashMap<>();
            if (request.containsKey("usedIn")) {
                usedIn = (Map<String, String>) request.get("usedIn");
            }
            JSONArray jArr = new JSONArray((String) request.get("data"));
            boolean coaActivateDeactivateFlag = (Boolean) request.get("coaActivateDeactivateFlag");
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("accid"))) {
                    Account account = (Account) get(Account.class, jobj.getString("accid"));
                    if (account != null) {
                        if (coaActivateDeactivateFlag) {
                            if (usedIn.containsKey(jobj.getString("accid"))) {
                                if (usedAcc.toString().length() > 0) {
                                    usedAcc.append(",");
                                }
                                usedAcc.append(StringUtil.isNullOrEmpty(account.getAcccode()) ? account.getName() : account.getAcccode()).append(" is Used in [").append(usedIn.get(jobj.getString("accid"))).append("]");
                            } else {
                                if (accountCompleted.toString().length() > 0) {
                                    accountCompleted.append(",");
                                }
                                accountCompleted.append(StringUtil.isNullOrEmpty(account.getAcccode()) ? account.getName() : account.getAcccode()).append(" ");
                                account.setActivate(false);
                            }
                        } else {
                            if (accountCompleted.toString().length() > 0) {
                                    accountCompleted.append(",");
                            }
                            accountCompleted.append(StringUtil.isNullOrEmpty(account.getAcccode()) ? account.getName() : account.getAcccode()).append(" ");
                            account.setActivate(true);
                        }
                        update(account);
                        list.add(account);
                    }
                }
            }
            if (coaActivateDeactivateFlag) {
                if (usedAcc.length() > 0 && accountCompleted.length() > 0) {
                    msg = "Some account(s) deactivated successfully : " + accountCompleted + ", except " + usedAcc + ".";
                } else if (accountCompleted.length() > 0) {
                    msg = "Selected account(s) deactivated successfully : " + accountCompleted + ".";
                } else if (usedAcc.length() > 0) {
                    msg = "Selected account(s) cannot be deactivated : " + usedAcc;
                    isSuccess = false;
                }
            } else {
                msg = "Selected account(s) activated successfully : " + accountCompleted + ".";
            }
        }  catch (JSONException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(isSuccess, msg, null, list, list.size());
    }

    public void isInactiveAccount(String accountid, String companyid) throws AccountingException, ServiceException {

        String selQuery = "";

        ArrayList params = new ArrayList();
        params.add(accountid);
        params.add(companyid);


        selQuery = "from JournalEntryDetail jed where account.ID = ? and jed.journalEntry.deleted=false  and jed.company.companyID=?";
        List list = executeQuery( selQuery, new Object[]{accountid, companyid});
        int count = list.size();
        if (count > 0) {
            throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
        }
        /*
         * selQuery = "from Product pr where (purchaseAccount.ID = ? or
         * salesAccount.ID = ? ) and pr.company.companyID=?"; list =
         * executeQuery( selQuery, new
         * Object[]{accountid, accountid, companyid}); count = list.size(); if
         * (count > 0) { throw new AccountingException("Selected record(s) is
         * currently used in the Account Preferences. So it cannot be
         * deleted."); } selQuery = "from CompanyAccountPreferences acp where
         * (discountGiven.ID = ? or discountReceived.ID = ? or
         * shippingCharges.ID = ? or otherCharges.ID = ? or cashAccount.ID = ?)
         * and acp.company.companyID=?";// (discountGiven.ID in ( "+qMarks +")
         * list = executeQuery( selQuery, new
         * Object[]{accountid, accountid, accountid, accountid, accountid,
         * companyid}); count = list.size(); if (count > 0) { throw new
         * AccountingException("Selected record(s) is currently used in the
         * Product(s). So it cannot be deleted."); } selQuery = "from
         * PaymentMethod pm where account.ID = ? and pm.company.companyID=?";
         * list = executeQuery( selQuery, new
         * Object[]{accountid, companyid}); count = list.size(); if (count > 0)
         * { throw new AccountingException("Selected record(s) is currently used
         * in the Term(s). So it cannot be deleted."); } selQuery = "from Tax t
         * where account.ID = ? and t.company.companyID=?"; list =
         * executeQuery( selQuery, new
         * Object[]{accountid, companyid}); count = list.size(); if (count > 0)
         * { throw new AccountingException("Selected record(s) is currently used
         * in the Tax(s). So it cannot be deleted.");
        }
         */

        Account account = (Account) get(Account.class, accountid);
        if (account.getOpeningBalance() != 0) {
            throw new AccountingException("Selected record(s) is having the Opening Balance. So it cannot be deleted");
        }
    }

    public KwlReturnObject deleteAccount(String accountid, String companyid) throws ServiceException {
        String delQuery = "delete from jedetail_optimized where account=? and company=? ";
        int numRows = executeSQLUpdate( delQuery, new String[]{accountid, companyid});
        String delQuery0 = "delete from bankreconciliation where account=? and company=? ";
        int numRows0 = executeSQLUpdate( delQuery0, new String[]{accountid, companyid});
        String delQuery1 = "delete from Account a where a.ID=? and a.company.companyID=?";
        int numRows1 = executeUpdate( delQuery1, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "Account has been deleted successfully.", null, null, numRows1);
    }

    public KwlReturnObject deleteAccount(String accountid, boolean flag) throws ServiceException {
        List list = new ArrayList();
        try {
            Account account = (Account) get(Account.class, accountid);
            account.setDeleted(flag);
            update(account);
            list.add(account);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Account has been deleted successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject deleteIBGBankDetail(String accountid, String companyid) throws ServiceException {
        String delQuery = "delete from IBGBankDetails where account.ID = ? and company.companyID = ? ";
        int numRows = executeUpdate( delQuery, new Object[]{accountid, companyid});
        
        delQuery = "delete from CIMBBankDetails where account.ID = ? and company.companyID = ? ";
        numRows += executeUpdate( delQuery, new Object[]{accountid, companyid});
        
        delQuery = "delete from UOBBankDetails where account.ID = ? and company.companyID = ? ";
        numRows += executeUpdate(delQuery, new Object[]{accountid, companyid});

        delQuery = "delete from OCBCBankDetails where account.ID = ? and company.companyID = ? ";
        numRows += executeUpdate(delQuery, new Object[]{accountid, companyid});
        
        return new KwlReturnObject(true, "IBG Bank Detail has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteGroup(String groupid, boolean isPermDel) throws ServiceException {
        List list = new ArrayList();
        String msg = "";
        boolean flag = true;
        try {
            String q = "from Account where group.ID=?";
            list = executeQuery( q, groupid);
            if (list.size() <= 0) {
                Group group = (Group) get(Group.class, groupid);
                if (isPermDel) {
                    if (group.getChildren() != null && group.getChildren().size() > 0) { // check if account group is parent group
                        msg = "acc.account.group.parent.group.deleted";
                        flag = false;
                    } else { //delete the group
                        delete(group);
                        list.add(group);
                        msg = "acc.account.group.deleted.permanently";
                    }
                } else { //delete the account group temporary
                    group.setDeleted(true);
                    update(group);
                    list.add(group);
                    msg = "acc.account.group.deleted.temporary";
                }
            } else {
                msg = "acc.account.group.mapped.deleted";
                flag = false;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(flag, msg, null, list, list.size());
    }
   
    @Override
    public KwlReturnObject addGroup(JSONObject groupjson) throws ServiceException {
        List list = new ArrayList();
        try {
            Group group = new Group();
            group.setDeleted(false);

            if (groupjson.has("name")) {
                group.setName(groupjson.getString("name"));
            }
            if (groupjson.has("nature")) {
                group.setNature(groupjson.getInt("nature"));
            }
            if (groupjson.has("disporder")) {
                group.setDisplayOrder(groupjson.getInt("disporder"));
            }
            if (groupjson.has("affectgp")) {
                group.setAffectGrossProfit(groupjson.getBoolean("affectgp"));
            }
            if (groupjson.has("isMasterGroup")) {
                group.setIsMasterGroup(groupjson.getBoolean("isMasterGroup"));
            }
            if (groupjson.has("parentid")) {
                group.setParent((Group) get(Group.class, groupjson.getString("parentid")));
            }
            if (groupjson.has("companyid")) {
                group.setCompany((Company) get(Company.class, groupjson.getString("companyid")));
            }
            if (groupjson.has("grpOldId")) {
                group.setGrpOldId(groupjson.getString("grpOldId"));
            }
            if (groupjson.has("parentCompanyGroupID")) {
                group.setPropagatedgroupid((Group) get(Group.class, groupjson.getString("parentCompanyGroupID")));
            }
            if (groupjson.has("isCostOfGoodsSoldGroup")) {
                group.setCostOfGoodsSoldGroup(groupjson.getBoolean("isCostOfGoodsSoldGroup"));
            }
            saveOrUpdate(group);
            list.add(group);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updateGroup(JSONObject groupjson) throws ServiceException {
        List list = new ArrayList();
        try {
            String groupid = groupjson.getString("groupid");
            Group group = (Group) get(Group.class, groupid);
            if (group != null) {
                if (groupjson.has("name")) {
                    group.setName(groupjson.getString("name"));
                }
                if (groupjson.has("nature")) {
                    group.setNature(groupjson.getInt("nature"));
                }
                if (groupjson.has("disporder")) {
                    group.setDisplayOrder(groupjson.getInt("disporder"));
                }
                if (groupjson.has("affectgp")) {
                    group.setAffectGrossProfit(groupjson.getBoolean("affectgp"));
                }
                if (groupjson.has("parentid")) {
                    group.setParent((Group) get(Group.class, groupjson.getString("parentid")));
                }
                if (groupjson.has("companyid")) {
                    group.setCompany((Company) get(Company.class, groupjson.getString("companyid")));
                }
                if (groupjson.has("grpOldId")) {
                    group.setGrpOldId(groupjson.getString("grpOldId"));
                }
                if (groupjson.has("parentCompanyGroupID")) {
                    group.setPropagatedgroupid((Group) get(Group.class, groupjson.getString("parentCompanyGroupID")));
                }
                if (groupjson.has("isCostOfGoodsSoldGroup")) {
                    group.setCostOfGoodsSoldGroup(groupjson.getBoolean("isCostOfGoodsSoldGroup"));
                }
                saveOrUpdate(group);
            }
            list.add(group);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public List updateParentGroup(Group group) throws ServiceException {
        List<Group> list = new ArrayList<Group>();
        try {
            saveOrUpdate(group);
            list.add(group);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return list;
    }

    public void updateChildrenGroup(Group group) throws ServiceException {
        try {
            Set<Group> children = group.getChildren();
            if (children == null) {
                return;
            }
            Iterator<Group> itr = children.iterator();
            while (itr.hasNext()) {
                Group child = itr.next();
                child.setNature(group.getNature());
                child.setAffectGrossProfit(group.isAffectGrossProfit());
                child.setCostOfGoodsSoldGroup(group.isCostOfGoodsSoldGroup());
                update(child);
                updateChildrenGroup(child);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public KwlReturnObject getMaxGroupDisplayOrder() throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String query = "select max(displayOrder) from Group";
        list = executeQuery( query);
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

   @Transactional(propagation = Propagation.REQUIRED)
    public KwlReturnObject copyAccounts(String companyid, String currencyid, String companyType, String countryId, HashMap accounthm,String stateId,boolean mrpActivated) throws ServiceException {
        List returnlist = new ArrayList();
        HashMap hm = new HashMap();
        try {
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, StorageHandler.getDefaultCurrencyID());
            KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, currencyid);
            Company company = (Company) get(Company.class, companyid);
            String query = "from DefaultAccount where parent is null ";
            List list = null;
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
            Date newUserDate = new Date();//Server's new Date();
            String countryQry = "",stateQry="";
            if (countryId != null) {
                countryQry = " AND (country = '" + countryId + "' OR country is null) ";
            }
            if (stateId != null) {
                stateQry = " AND (state = '" + stateId + "' OR state is null) ";
            }
            if (!mrpActivated) {
                query += " and (mrpAccount = false)";
            } else {
                query += " and (mrpAccount=true OR mrpAccount = false)";
            }
            if (StringUtil.isNullOrEmpty(companyType)) {
                query += "and companytype is null";
                list = executeQuery( query + countryQry + stateQry);
            } else {
                query += "and ( companytype=? OR companytype = '' )";//statewise default account has no company type, as it is to be applied for all industry type.
                list = executeQuery( query + countryQry + stateQry , companyType);
            }
            
            if (company.getCreator() != null) {
                newUserDate = authHandler.getUserNewDate(null, company.getCreator().getTimeZone()!=null?company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference());
            }

            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                DefaultAccount defaultAccount = (DefaultAccount) iter.next();
                /**
                 * Check If Account is present or not if Present then update same account
                 * Ticket - ERP-35391
                 */
                HashMap<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put(Constants.filter_names, new ArrayList<String>(Arrays.asList("company.companyID", "name")));
                filterParams.put(Constants.filter_params, new ArrayList<String>(Arrays.asList(companyid, defaultAccount.getName())));
                KwlReturnObject resultList = getAccount(filterParams);
                Account account = new Account();
                if (resultList != null && resultList.getEntityList()!=null && !resultList.getEntityList().isEmpty()) {
                    account = (Account)resultList.getEntityList().get(0);
                }
                account.setCompany(company);
                account.setDeleted(false);
                account.setActivate(true);
                Group group = null;
                if (accounthm != null && accounthm.containsKey(defaultAccount.getGroup().getID())) {
                    group = (Group) accounthm.get(defaultAccount.getGroup().getID());
                } else if(defaultAccount.getGroup() != null){
                    group = (Group)kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.Group", defaultAccount.getGroup().getID());
                    
                }
                //0 - Asset, 1 - Liability, 2 - Expense, 3 - Income This is Account Type
                //1 - GL, 2 - Cash, 3 - Bank, 4 - GST Tax This is Account Master type Type
                int masterType = 1;
                int accountType = 0;
                int natureVal = 0;
                String accountTypeName = "";
                if(group != null ){
                    natureVal = group.getNature();
                    if(!StringUtil.isNullOrEmpty(group.getName())){
                        accountTypeName = group.getName();
                    }
                }
                
//                String accountTypeName = defaultAccount.getGroup().getName();
                if (natureVal == 0 || natureVal == 1) {
                    accountType = Group.ACCOUNTTYPE_GL;
                }
                try {
                    masterType = defaultAccount.getMastertypevalue();
                } catch (Exception e) {
                    if (accountTypeName.equalsIgnoreCase("Cash")) {
                        masterType = Group.ACCOUNTTYPE_CASH;
                    } else if (accountTypeName.equalsIgnoreCase("Bank")) {
                        masterType = Group.ACCOUNTTYPE_BANK;
                    }
                    if (!StringUtil.isNullOrEmpty(company.getCountry().getID()) && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id && Constants.TDSDefaultChartOfAccountsINDIA.contains(defaultAccount.getName())) {
                        masterType = Group.ACCOUNTTYPE_GST;
                    }
                }
                
                account.setGroup(group);
                account.setName(defaultAccount.getName());
                account.setCurrency(currency);
                account.setAccounttype(accountType);
                account.setMastertypevalue(masterType);
                //For account Table, creation date is saved according to user's timezone diff.
                account.setCreationDate(newUserDate);
                account.setPresentValue(defaultAccount.getPresentValue());
                account.setOpeningBalance(defaultAccount.getOpeningBalance());
                if (!StringUtil.isNullOrEmpty(company.getCountry().getID()) && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id) {
                    account.setDefaultaccountID(defaultAccount.getID());
                }
                save(account);   
                hm.put(defaultAccount.getID(), account.getID());
                hm.putAll(saveChildren(account, defaultAccount));
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyAccounts : " + ex.getMessage(), ex);
        }
        returnlist.add(hm);
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }
   
    public Group getAccountGroup(String companyid, String groupName) throws ServiceException {
        Group group = null;
        try {
            String query = "from Group where company.companyID=? and name=?";
            List list = executeQuery( query, new Object[]{companyid, groupName});
            if (!list.isEmpty()) {
                group = (Group) list.get(0);
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return group;
    }

    public KwlReturnObject getDefaultAccount(String companyType, String countryId, String stateId, boolean isAdminSubdomain, String[] nature) throws ServiceException {
        String query = "from DefaultAccount where ";
        List returnlist = new ArrayList();
        ArrayList params = new ArrayList();
        String countryQry = "", condition = "";
        String stateQry = "";
        try {
            if (countryId != null) {
                if (isAdminSubdomain) {
                    countryQry = " country = '" + countryId + "' ";
                } else {
                    countryQry = " AND (country = '" + countryId + "' OR country is null) ";
                }
            }
            if (isAdminSubdomain && nature != null) {
                String qMarks = "?";
                params.add(5);//not a nature
                for (int i = 0; i < nature.length; i++) {
                    qMarks += ",?";
                    params.add(Integer.parseInt(nature[i]));
                }
                condition += " and group.nature in (" + qMarks + ") ";
            }
            if (stateId != null) {
                stateQry = " AND (state = '" + stateId + "' OR state is null) ";
            }
            if (!isAdminSubdomain) {
                if (StringUtil.isNullOrEmpty(companyType)) {
                    query += " companytype is null";
                } else {
                    query += "( companytype=?  OR companytype = '' )";//statewise default account has no company type, as it is to be applied for all industry type.
                    params.add(companyType);
                }
            }
            condition += "  and mrpAccount = false "; // do not include MRP accounts in default account //ERROR : Query got break. So added this part in condition - Vaibhav P.

            query += countryQry + stateQry + condition;
            returnlist = executeQuery(query, params.toArray());
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }

    private HashMap saveChildren(Account account, DefaultAccount defaultAccount) throws ServiceException {
        HashMap hm = new HashMap();
        try {
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, StorageHandler.getDefaultCurrencyID());
            Set set = defaultAccount.getChildren();
            Iterator iter = set.iterator();
            while (iter.hasNext()) {
                DefaultAccount da = (DefaultAccount) iter.next();
                Account acc = new Account();
                acc.setCompany(account.getCompany());
                acc.setDeleted(false);
                acc.setActivate(true);
                acc.setGroup(account.getGroup());
                acc.setName(da.getName());
                acc.setOpeningBalance(da.getOpeningBalance());
                acc.setParent(account);
                acc.setCurrency(account.getCurrency());
                save(acc);
                hm.put(da.getID(), acc.getID());
                HashMap hm_children = saveChildren(account, defaultAccount);
                if (hm_children.size() > 0 ) hm.putAll(hm_children);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveChildren : " + ex.getMessage(), ex);
        }
        return hm;
    }

    public boolean isPosted(Account account) throws ServiceException {
        String query = "from DepreciationDetail where account.ID=? and company.companyID=?";
        List list = executeQuery( query, new Object[]{account.getID(), account.getCompany().getCompanyID()});
        return !list.isEmpty();
    }

    @Override
    public boolean isChild(String ParentID, String childID) throws ServiceException {
        String query = "select id from account where parent=? and id=?";
        List list = executeSQLQuery( query, new Object[]{ParentID, childID});
        return !list.isEmpty();
    }
    
    public JSONObject getUnMappedAccounts(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject resultJobj = new JSONObject();
        String result = "";
        try {
            ArrayList params = new ArrayList();
            ArrayList searchparams=new ArrayList();
            if (requestParams.get("templateid") != null) {
                params.add(requestParams.get("templateid").toString());
                searchparams.add(requestParams.get("templateid").toString());
            }
            if (requestParams.get("companyid") != null) {
                params.add(requestParams.get("companyid").toString());
            }
            String condition="";
            String seachtemplatetype="select templatetype,dontshowmsg from templatepnl where id=?";
            List<Object[]> searchlist = executeSQLQuery( seachtemplatetype,searchparams.toArray());
//            Iterator searchitr=searchlist.iterator();
            int type=0;
            boolean dontshowmsg=false;
            for (Object[] obj : searchlist) {
                type = (Integer) obj[0];
                dontshowmsg = (String.valueOf(obj[1])).equalsIgnoreCase("T");
            }
            if(!dontshowmsg){
            
            if (type == 0) {
                condition = "And accounttype=0";
            } else if (type == 1) {
                condition = "And accounttype=1";
            }
            String query = "select account,layoutgroup.showchildacc from groupaccmap INNER JOIN layoutgroup on groupaccmap.layoutgroup=layoutgroup.id  where layoutgroup.template=? And layoutgroup.company=?";
            List<Object[]> layoutAccountIdlist = executeSQLQuery( query, params.toArray());
            
            query = "select id from account where id NOT in (select account from groupaccmap INNER JOIN layoutgroup on groupaccmap.layoutgroup=layoutgroup.id  where layoutgroup.template=?) And account.company=?"+condition;
            List list = executeSQLQuery( query, params.toArray());
            
            for (Object accId[] : layoutAccountIdlist) {
                Account account = (Account) get(Account.class, String.valueOf(accId[0]));
                boolean isShowChildAccounts = String.valueOf(accId[1]).trim().equals("1");
                if (isShowChildAccounts) {
                    removeMappedChildAcc(account, list);
                }
            }
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Account account = (Account) get(Account.class, (String) itr.next());
                if (account != null) {
                    result += account.getName();
                }
                if(account.getAcccode()!=null){
                    result +="("+account.getAcccode() + ")" + ", ";
                }else{
                    result+=", ";
                }
            }
            }
            resultJobj.put("msg",!result.equals("") ? result.substring(0, result.length() - 2) : result);
            resultJobj.put("dontshowmsg",dontshowmsg);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getUnMappedAccounts : " + ex.getMessage(), ex);
        }
        return resultJobj;
    }
    
    @Override
    public int setDontShowFlagCustomLayout(HashMap<String, Object> requestParams) throws ServiceException {
        int numRows = 0;
        try {
            ArrayList params = new ArrayList();
            if (requestParams.get("templateid") != null) {
                params.add(requestParams.get("templateid").toString());
            }
            if (requestParams.get("companyid") != null) {
                params.add(requestParams.get("companyid").toString());
            }
            String updateQuery = "update Templatepnl set dontshowmsg = true where ID = ? and company.companyID = ?";
            numRows = executeUpdate(updateQuery, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getUnMappedAccounts : " + ex.getMessage(), ex);
        }
        return numRows;
    }
    
    public void removeMappedChildAcc(Account account, List unMappedAccList) {
        if (account.getChildren() != null) {
            Set<Account> childAcc = account.getChildren();
            for (Account acc : childAcc) {
                if (unMappedAccList.contains(acc.getID())) {
                    unMappedAccList.remove(acc.getID());
                }
                removeMappedChildAcc(acc, unMappedAccList);
            }
        }
    }

    @Override
    public List isChildorGrandChild(String childID) throws ServiceException {
        String query = "from Account where ID=? and parent != null";
        List Result = executeQuery( query, new Object[]{childID});
        return Result;
    }

    @Override
    public List isChildorGrandChildForCustomer(String childID) throws ServiceException {
        String query = "from Customer where ID=? and parent != null";
        List Result = executeQuery( query, new Object[]{childID});
        return Result;
    }

    @Override
    public List isChildorGrandChildForVendor(String childID) throws ServiceException {
        String query = "from Vendor where ID=? and parent != null";
        List Result = executeQuery( query, new Object[]{childID});
        return Result;
    }

    @Override
    public List isChildforDelete(String childID) throws ServiceException {
        String query = "from Account where parent.ID=?";
        List Result = executeQuery( query, new Object[]{childID});
        return Result;
    }

    public Group getNewGroupFromOldId(String id, String companyid) throws ServiceException {
        Group group = null;
        String query = "from Group where grpOldId=? and company.companyID=?";
        List Result = executeQuery( query, new Object[]{id, companyid});
        if (!Result.isEmpty()) {
            group = (Group) Result.get(0);
        }
        return group;
    }

    @Override
    public List getMappedAccountsForReports(String parentaccountid) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        //String condition = "where deleted=false";
        String query = " select accountmapping.childaccountid, accountmapping.parentaccountid, account.name as groupaccname, account.acccode as groupacccode from accountmapping "
                + " left join account on account.id = accountmapping.parentaccountid where parentaccountid = ? ";
        params.add(parentaccountid);
        returnList = executeSQLQuery( query, params.toArray());
        return returnList;
    }

    @Override
    public List getMappedAccounts(Account childAccObj, String parentcompanyid, boolean autoMap) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String childacccode = (childAccObj.getAcccode() != null ? childAccObj.getAcccode() : "");
        String childaccid = childAccObj.getID();
        String groupid = childAccObj.getGroup().getID();
        String query = " select account.id as parentaccid, account.name as parentaccname, account.acccode as parentacccode, account.groupname as parentaccgroup, 1 as mappedFlag, accountmapping.id as mappingid from accountmapping "
                + " inner join account on account.id = accountmapping.parentaccountid where childaccountid = ? and account.company = ? ";
        params.add(childaccid);
        params.add(parentcompanyid);

//        if(filterParams.containsKey("order_by")) {
//            condition += " order by "+filterParams.get("order_by");
//        }
//        query += condition;
        returnList = executeSQLQuery( query, params.toArray());
        if (autoMap && returnList.size() < 1) {
            if (!StringUtil.isNullOrEmpty(childacccode)) {
                params.clear();
                query = " select account.id as parentaccid, account.name as parentaccname, account.acccode as parentacccode, account.groupname as parentaccgroup, 0 as mappedFlag,0 as mappingid from account "
                        + " where acccode = ? and company = ? and deleteflag=false and groupname = ?";
                params.add(childacccode);
                params.add(parentcompanyid);
                params.add(groupid);
                returnList = executeSQLQuery( query, params.toArray());
            }
        }
        return returnList;
    }

    @Override
    public KwlReturnObject addMonthlyBudget(JSONObject accjson, int year) throws ServiceException {
        List list = new ArrayList();
        try {
            AccountBudget accbudget = new AccountBudget();

            if (accjson.has("id")) {
                if (!accjson.get("id").equals("0")) {
                    accbudget=(AccountBudget) get(AccountBudget.class, (String) accjson.get("id"));
                }else{
                    accbudget=new AccountBudget();
                }
            }
            
            if (accjson.has("accountid")) {
                Account account = (accjson.get("accountid") == null ? null : (Account) get(Account.class, (String) accjson.get("accountid")));
                accbudget.setAccount(account);
            }
            
            if (accjson.has("jan") && !StringUtil.isNullOrEmpty(accjson.getString("jan")) && accjson.getDouble("jan") >= 0) {
                double budget = Double.parseDouble(accjson.get("jan").toString());
                accbudget.setJan(budget);
            } else {
                accbudget.setJan(-1);
            }
            if (accjson.has("feb") && !StringUtil.isNullOrEmpty(accjson.getString("feb")) && accjson.getDouble("feb") >= 0) {
                double budget = Double.parseDouble(accjson.get("feb").toString());
                accbudget.setFeb(budget);
            } else {
                accbudget.setFeb(-1);
            }
            if (accjson.has("march") && !StringUtil.isNullOrEmpty(accjson.getString("march")) && accjson.getDouble("march") >= 0) {
                double budget = Double.parseDouble(accjson.get("march").toString());
                accbudget.setMarch(budget);
            } else {
                accbudget.setMarch(-1);
            }
            if (accjson.has("april") && !StringUtil.isNullOrEmpty(accjson.getString("april")) && accjson.getDouble("april") >= 0) {
                double budget = Double.parseDouble(accjson.get("april").toString());
                accbudget.setApril(budget);
            } else {
                accbudget.setApril(-1);
            }
            if (accjson.has("may") && !StringUtil.isNullOrEmpty(accjson.getString("may")) && accjson.getDouble("may") >= 0) {
                double budget = Double.parseDouble(accjson.get("may").toString());
                accbudget.setMay(budget);
            } else {
                accbudget.setMay(-1);
            }
            if (accjson.has("june") && !StringUtil.isNullOrEmpty(accjson.getString("june")) && accjson.getDouble("june") >= 0) {
                double budget = Double.parseDouble(accjson.get("june").toString());
                accbudget.setJune(budget);
            } else {
                accbudget.setJune(-1);
            }
            if (accjson.has("july") && !StringUtil.isNullOrEmpty(accjson.getString("july")) && accjson.getDouble("july") >= 0) {
                double budget = Double.parseDouble(accjson.get("july").toString());
                accbudget.setJuly(budget);
            } else {
                accbudget.setJuly(-1);
            }
            if (accjson.has("aug") && !StringUtil.isNullOrEmpty(accjson.getString("aug")) && accjson.getDouble("aug") >= 0) {
                double budget = Double.parseDouble(accjson.get("aug").toString());
                accbudget.setAug(budget);
            } else {
                accbudget.setAug(-1);
            }
            if (accjson.has("sept") && !StringUtil.isNullOrEmpty(accjson.getString("sept")) && accjson.getDouble("sept") >= 0) {
                double budget = Double.parseDouble(accjson.get("sept").toString());
                accbudget.setSept(budget);
            } else {
                accbudget.setSept(-1);
            }
            if (accjson.has("oct") && !StringUtil.isNullOrEmpty(accjson.getString("oct")) && accjson.getDouble("oct") >= 0) {
                double budget = Double.parseDouble(accjson.get("oct").toString());
                accbudget.setOct(budget);
            } else {
                accbudget.setOct(-1);
            }
            if (accjson.has("nov") && !StringUtil.isNullOrEmpty(accjson.getString("nov")) && accjson.getDouble("nov") >= 0) {
                double budget = Double.parseDouble(accjson.get("nov").toString());
                accbudget.setNov(budget);
            } else {
                accbudget.setNov(-1);
            }
            if (accjson.has("dec") && !StringUtil.isNullOrEmpty(accjson.getString("dec")) && accjson.getDouble("dec") >= 0) {
                double budget = Double.parseDouble(accjson.get("dec").toString());
                accbudget.setDecember(budget);
            } else {
                accbudget.setDecember(-1);
            }
            
            if (accjson.has("dimensionvalue") && !StringUtil.isNullOrEmpty(accjson.optString("dimensionvalue", null))) {
                accbudget.setDimensionValue(accjson.optString("dimensionvalue"));
            }

            if (accjson.has("dimension") && !StringUtil.isNullOrEmpty(accjson.optString("dimension", null))) {
                accbudget.setDimension(accjson.optString("dimension"));
            }
            
            if(year != 0){
                accbudget.setYear(year);
            }

            saveOrUpdate(accbudget);
            list.add(accbudget);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addMonthlyBudget : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Monthly Budget has been added successfully", null, list, list.size());
    }

    @Override
    public KwlReturnObject addMonthlyForecast(JSONObject accjson) throws ServiceException {
        List list = new ArrayList();
        try {
            AccountForecast accountForecast = new AccountForecast();

            if (accjson.has("accountid")) {
                Account account = (accjson.get("accountid") == null ? null : (Account) get(Account.class, (String) accjson.get("accountid")));
                accountForecast.setAccount(account);
            }
            if (accjson.has("id")) {
                if (!accjson.get("id").equals("0")) {
                    accountForecast.setID(accjson.get("id").toString());
                }
            }
            if (accjson.has("jan") && !StringUtil.isNullOrEmpty(accjson.getString("jan")) && accjson.getDouble("jan") >= 0) {
                double budget = Double.parseDouble(accjson.get("jan").toString());
                accountForecast.setJan(budget);
            } else {
                accountForecast.setJan(-1);
            }
            if (accjson.has("feb") && !StringUtil.isNullOrEmpty(accjson.getString("feb")) && accjson.getDouble("feb") >= 0) {
                double budget = Double.parseDouble(accjson.get("feb").toString());
                accountForecast.setFeb(budget);
            } else {
                accountForecast.setFeb(-1);
            }
            if (accjson.has("march") && !StringUtil.isNullOrEmpty(accjson.getString("march")) && accjson.getDouble("march") >= 0) {
                double budget = Double.parseDouble(accjson.get("march").toString());
                accountForecast.setMarch(budget);
            } else {
                accountForecast.setMarch(-1);
            }
            if (accjson.has("april") && !StringUtil.isNullOrEmpty(accjson.getString("april")) && accjson.getDouble("april") >= 0) {
                double budget = Double.parseDouble(accjson.get("april").toString());
                accountForecast.setApril(budget);
            } else {
                accountForecast.setApril(-1);
            }
            if (accjson.has("may") && !StringUtil.isNullOrEmpty(accjson.getString("may")) && accjson.getDouble("may") >= 0) {
                double budget = Double.parseDouble(accjson.get("may").toString());
                accountForecast.setMay(budget);
            } else {
                accountForecast.setMay(-1);
            }
            if (accjson.has("june") && !StringUtil.isNullOrEmpty(accjson.getString("june")) && accjson.getDouble("june") >= 0) {
                double budget = Double.parseDouble(accjson.get("june").toString());
                accountForecast.setJune(budget);
            } else {
                accountForecast.setJune(-1);
            }
            if (accjson.has("july") && !StringUtil.isNullOrEmpty(accjson.getString("july")) && accjson.getDouble("july") >= 0) {
                double budget = Double.parseDouble(accjson.get("july").toString());
                accountForecast.setJuly(budget);
            } else {
                accountForecast.setJuly(-1);
            }
            if (accjson.has("aug") && !StringUtil.isNullOrEmpty(accjson.getString("aug")) && accjson.getDouble("aug") >= 0) {
                double budget = Double.parseDouble(accjson.get("aug").toString());
                accountForecast.setAug(budget);
            } else {
                accountForecast.setAug(-1);
            }
            if (accjson.has("sept") && !StringUtil.isNullOrEmpty(accjson.getString("sept")) && accjson.getDouble("sept") >= 0) {
                double budget = Double.parseDouble(accjson.get("sept").toString());
                accountForecast.setSept(budget);
            } else {
                accountForecast.setSept(-1);
            }
            if (accjson.has("oct") && !StringUtil.isNullOrEmpty(accjson.getString("oct")) && accjson.getDouble("oct") >= 0) {
                double budget = Double.parseDouble(accjson.get("oct").toString());
                accountForecast.setOct(budget);
            } else {
                accountForecast.setOct(-1);
            }
            if (accjson.has("nov") && !StringUtil.isNullOrEmpty(accjson.getString("nov")) && accjson.getDouble("nov") >= 0) {
                double budget = Double.parseDouble(accjson.get("nov").toString());
                accountForecast.setNov(budget);
            } else {
                accountForecast.setNov(-1);
            }
            if (accjson.has("dec") && !StringUtil.isNullOrEmpty(accjson.getString("dec")) && accjson.getDouble("dec") >= 0) {
                double budget = Double.parseDouble(accjson.get("dec").toString());
                accountForecast.setDecember(budget);
            } else {
                accountForecast.setDecember(-1);
            }

            saveOrUpdate(accountForecast);
            list.add(accountForecast);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addMonthlyForecast : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Monthly Forecast has been added successfully", null, list, list.size());
    }

  @Override  
    public KwlReturnObject deleteMonthlyBudget(String id, String dimension,int year,HashMap<String, Object> filterParams) throws ServiceException {
        StringBuilder conditionString = new StringBuilder();
        String deleteid = id;
        if (!StringUtil.isNullOrEmpty(id)) {
            if (conditionString.length() > 0) {
                conditionString.append(" and p.ID=? s");
            } else {
                conditionString.append(" where p.ID=? ");
            }
        }
        if (!StringUtil.isNullOrEmpty(dimension)) {
            deleteid = dimension;
            if (conditionString.length() > 0) {
                conditionString.append(" and p.dimension=? ");
            } else {
                conditionString.append(" where p.dimension=? ");
            }
        }
        
        if(year!=AccountBudget.BLANK_YEAR){
          if (conditionString.length() > 0) {
                conditionString.append(" and p.year="+year+" ");
            } else {
                conditionString.append(" where p.year="+year+" ");
            }
        }
        if (filterParams.containsKey("accountvalue") && filterParams.get("accountvalue") != null) {
            String accountidvalues = AccountingManager.getFilterInString((String) filterParams.get("accountvalue"));
            if (conditionString.length() > 0) {
                conditionString.append(" and ");
            } else {
                conditionString.append(" where ");
            }
            conditionString.append(" p.account.ID IN " + accountidvalues);
        }
        
        String delQuery = "delete from AccountBudget p " + conditionString.toString();
        int numRows = executeUpdate(delQuery, new Object[]{deleteid});
        return new KwlReturnObject(true, "Monthly Budget has been deleted successfully.", null, null, numRows);
    }
    
    public KwlReturnObject deleteMonthlyForecast(String id) throws ServiceException {
        String delQuery = "delete from AccountForecast p where p.ID=?";
        int numRows = executeUpdate( delQuery, new Object[]{id});
        return new KwlReturnObject(true, "Monthly Account Forecast has been deleted successfully.", null, null, numRows);
    }

@Override
    public KwlReturnObject getMonthlyBudget(HashMap<String, Object> filterParams) throws ServiceException, JSONException {
        List returnList = new ArrayList();
        List<AccountBudget> accBudgetList=new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from AccountBudget ab ";
        StringBuilder queryBuilder=new StringBuilder();

        if (filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.account.company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        if (filterParams.containsKey("accountid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.account.ID=?";
            params.add((String) filterParams.get("accountid"));
        }
        if (filterParams.containsKey("year")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.year=?";
            params.add((Integer) filterParams.get("year"));
        }
        if (filterParams.containsKey("startyear") && filterParams.get("startyear") != null && filterParams.containsKey("endyear") && filterParams.get("endyear") != null) {
            int startyear = (Integer) filterParams.get("startyear");
            int endyear = (Integer) filterParams.get("endyear");
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.year BETWEEN " + startyear + " and " + endyear + " ";
        }
        if (filterParams.containsKey("dimensionvalue") && filterParams.get("dimensionvalue") != null) {
            String dimensionvalues = AccountingManager.getFilterInString((String) filterParams.get("dimensionvalue"));
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.dimensionValue IN " + dimensionvalues;
        }
        if (filterParams.containsKey("accountvalue") && filterParams.get("accountvalue") != null) {
            String accountidvalues = AccountingManager.getFilterInString((String) filterParams.get("accountvalue"));
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.account.ID IN " + accountidvalues;
        }

        if (filterParams.containsKey("dimension") && filterParams.get("dimension") != null) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.dimension=?";
            params.add((String) filterParams.get("dimension"));
        }
        if (filterParams.containsKey("id") && filterParams.get("id") != null) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.ID=?";
            params.add((String) filterParams.get("id"));
        }
        
        queryBuilder.append(query + condition);
        String Searchjson = "";
//        String mySearchFilterString = "";
        int searchJsonsize = 0;
        boolean avoidAndClauseFlag = false;
        HashSet<String> accountBudgetIds = new HashSet<>();
//        if (filterParams.get("accountvalue").toString().equals("40288050614ca54301614fcdbc7e0083")) {
            StringBuilder dimensionString = new StringBuilder();
            if (filterParams.containsKey(Constants.Acc_Search_Json) && filterParams.get(Constants.Acc_Search_Json) != null) {
                Searchjson = filterParams.get(Constants.Acc_Search_Json).toString();
                String filterConjuctionCriteria = Constants.and;
                if (filterParams.containsKey("filterConjuctionCriteria") && filterParams.get("filterConjuctionCriteria") != null) {
                    if (filterParams.get("filterConjuctionCriteria").toString().equalsIgnoreCase("OR")) {
                        filterConjuctionCriteria = Constants.or;
                    }
                }
                JSONObject SearchJsonObj = new JSONObject(Searchjson);
                JSONArray SearchJsonArray = SearchJsonObj.getJSONArray("root");
                searchJsonsize = SearchJsonArray.length();
                if (searchJsonsize > 0) {
                    for (int j = 0; j < searchJsonsize; j++) {
                        JSONObject compareObj = SearchJsonArray.optJSONObject(j);
                        boolean iscustomfield = Boolean.parseBoolean(compareObj.optString("iscustomfield"));
                        boolean isDimensionFlag = !iscustomfield;
                        int xtype = Integer.parseInt(compareObj.optString("fieldtype", "1"));
                        if (xtype == 4 && isDimensionFlag) {//only for dimensions
                            String dimId = compareObj.optString("column");
                            String dimIDvalues = compareObj.optString("search");
                            dimIDvalues = AccountingManager.getFilterInString(dimIDvalues);
                            dimId = AccountingManager.getFilterInString(dimId);
                            if (filterConjuctionCriteria.equalsIgnoreCase(Constants.or)) {
                                if (dimensionString.length() > 0) {
                                    dimensionString.append(" " + filterConjuctionCriteria + " ");
                                }
                                dimensionString.append(" (ab.dimension IN " + dimId + " and ab.dimensionValue IN " + dimIDvalues + ")");
                            } else if (filterConjuctionCriteria.equalsIgnoreCase(Constants.and)) {
                                avoidAndClauseFlag = true;
                                StringBuilder queStringBuilder = new StringBuilder();
                                StringBuilder mySearchFilterStringBuilder = new StringBuilder();
                                queStringBuilder.append(" (ab.dimension IN " + dimId + " and ab.dimensionValue IN " + dimIDvalues + ") ");
                                mySearchFilterStringBuilder.append(queryBuilder.toString()+" and " + queStringBuilder.toString() + " ");
                                List<AccountBudget> accList = executeQuery(mySearchFilterStringBuilder.toString(), params.toArray());
                                for (AccountBudget acc : accList) {
                                    //adding dimension id in set to keep track as how many query has been executed. If accountBudgetIds.SIZE =searchJson.size then and query will be executed else it will not return anything
                                    accountBudgetIds.add(dimId);
                                    accBudgetList.add(acc);
                                }
                            }
                        }
                    }//end of for loop of search json for loop
                    if (filterConjuctionCriteria.equalsIgnoreCase(Constants.and) && accountBudgetIds.size()==searchJsonsize) {
                        returnList = accBudgetList;
                    }
                }
                if (filterConjuctionCriteria.equalsIgnoreCase(Constants.or)) {
                    queryBuilder.append(" and (" + dimensionString.toString() + ") ");
                }
            }
//        }

        if (!avoidAndClauseFlag) {//will execute other than AND Clause
            returnList = executeQuery(queryBuilder.toString(), params.toArray());
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    public KwlReturnObject getMonthlyForecast(HashMap<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "from AccountForecast ab ";

        if (filterParams.containsKey("companyid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.account.company.companyID=?";
            params.add(filterParams.get("companyid"));
        }
        if (filterParams.containsKey("accountid")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "ab.account.ID=?";
            params.add((String) filterParams.get("accountid"));
        }
        query += condition;
        returnList = executeQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    @Override
    public KwlReturnObject saveUpdateAccountMapping(JSONObject accjson) throws ServiceException {
        List list = new ArrayList();
        try {
            AccountMapping accountMapping = new AccountMapping();


            if (accjson.has("id")) {
                if (!accjson.get("id").equals("0")) {
                    accountMapping.setId(accjson.get("id").toString());
                }
            }
            if (accjson.has("accid")) {
                Account account = (accjson.get("accid") == null ? null : (Account) get(Account.class, (String) accjson.get("accid")));
                accountMapping.setChildAccountId(account);
            }
            if (accjson.has("parentaccid")) {
                Account account = (accjson.get("parentaccid") == null ? null : (Account) get(Account.class, (String) accjson.get("parentaccid")));
                accountMapping.setParentAccountId(account);
            }


            saveOrUpdate(accountMapping);
            list.add(accountMapping);
        } catch (Exception e) {
            throw ServiceException.FAILURE("addMonthlyBudget : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Account mapping has been done successfully.", null, list, list.size());
    }

    public KwlReturnObject getPnLTemplates(HashMap<String, Object> filterParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String companyid = (String) filterParams.get("companyid");

        boolean paging = false;
        int start = 0;
        int limit = 0;
        String condition = "";
        if (filterParams.containsKey("templateid")) {
            condition = " ID = ? and ";
            params.add((String) filterParams.get("templateid"));

        }

        if (filterParams.containsKey("isdropdown") && (Boolean) filterParams.get("isdropdown")) {
            condition = " status = ? and ";
            params.add(0);

        }
        if (filterParams.containsKey("isDefault") && (Boolean) filterParams.get("isDefault")) {
            condition = " defaultTemplate = ? and ";
            params.add(true);

        }
        if (filterParams.containsKey("templatetype") && filterParams.get("templatetype") != null) {
            condition += " templatetype = ? and ";
            params.add((Integer) filterParams.get("templatetype"));
        }

        if (filterParams.containsKey("start") && filterParams.containsKey("limit")) {
            start = (Integer) filterParams.get("start");
            limit = (Integer) filterParams.get("limit");
            paging = true;
        }
            params.add(companyid);

        String query = "from Templatepnl where " + condition + " deleted = false and company.companyID = ? order by name ";
        
        List list = executeQuery( query, params.toArray());
        int count = list.size();
        if (paging) {
            list = executeQueryPaging( query, params.toArray(), new Integer[]{start, limit});
        }

        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject deleteAccountMapPnL(String templateid, String companyid) throws ServiceException {
        Templatepnl templatepnl = (Templatepnl) get(Templatepnl.class, templateid);

        int mappedTemplateId = templatepnl.getTemplateid();
        String delQuery = "delete from PnLAccountMap where templateid = ? and company.companyID = ?";
        int numRows = executeUpdate( delQuery, new Object[]{mappedTemplateId, companyid});
        return new KwlReturnObject(true, "Mapping has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteCustomTemplate(String templateid, String companyid) throws ServiceException {

        String delQuery = "update Templatepnl set deleted = true where ID = ? and company.companyID = ?";
        int numRows = executeUpdate( delQuery, new Object[]{templateid, companyid});
        return new KwlReturnObject(true, "Custom template has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteDefaultCustomTemplate(String templateid) throws ServiceException {

        String delQuery = "update DefaultTemplatePnL set deleted = true where ID = ? ";
        int numRows = executeUpdate( delQuery, new Object[]{templateid});
        return new KwlReturnObject(true, "Custom template has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteLayoutGroup(String groupid, String companyid) throws ServiceException {

        String delQuery = "delete from LayoutGroup where ID = ? and company.companyID = ?";
        int numRows = executeUpdate( delQuery, new Object[]{groupid, companyid});
        return new KwlReturnObject(true, "Group has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteDefaultLayoutGroup(String groupid) throws ServiceException {

        String delQuery = "delete from DefaultLayoutGroup where ID = ? ";
        int numRows = executeUpdate( delQuery, new Object[]{groupid});
        return new KwlReturnObject(true, "Group has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteLayoutGroupAccount(String groupid, String companyid) throws ServiceException {

        String delQuery = "delete from GroupAccMap where layoutgroup.ID = ? and company.companyID = ?";
        int numRows = executeUpdate( delQuery, new Object[]{groupid, companyid});

        return new KwlReturnObject(true, "Group Accounts has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteDefaultLayoutGroupAccount(String groupid) throws ServiceException {

        String delQuery = "delete from DefaultGroupAccMap where defaultlayoutgroup.ID = ? ";
        int numRows = executeUpdate( delQuery, new Object[]{groupid});

        return new KwlReturnObject(true, "Group Accounts has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteLayoutGroupsofTotalGroup(String groupid, String companyid) throws ServiceException {

        String delQuery = "delete from Groupmapfortotal where groupidtotal.ID = ? ";
        int numRows = executeUpdate( delQuery, new Object[]{groupid});

        return new KwlReturnObject(true, "Group Accounts has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject deleteDefaultLayoutGroupsofTotalGroup(String groupid) throws ServiceException {

        String delQuery = "delete from DefaultGroupMapForTotal where groupidtotal.ID = ? ";
        int numRows = executeUpdate( delQuery, new Object[]{groupid});

        return new KwlReturnObject(true, "Group Accounts has been deleted successfully.", null, null, numRows);
    }

    public KwlReturnObject saveAccountMapPnL(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            PnLAccountMap pnLAccountMap = new PnLAccountMap();

            if (dataMap.containsKey("templateid")) {
                pnLAccountMap.setTemplateid((Integer) dataMap.get("templateid"));
            }

            if (dataMap.containsKey("isincome")) {
                pnLAccountMap.setIsincome((Integer) dataMap.get("isincome"));
            }

            if (dataMap.containsKey("accountid")) {
                Account account = (Account) get(Account.class, (String) dataMap.get("accountid"));
                pnLAccountMap.setAccount(account);
            }

            if (dataMap.containsKey("companyid")) {
                Company company = (Company) get(Company.class, (String) dataMap.get("companyid"));
                pnLAccountMap.setCompany(company);
            }

            save(pnLAccountMap);
            list.add(pnLAccountMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveInvoice : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveLayoutGroupAccountMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            GroupAccMap groupMap = new GroupAccMap();

            if (dataMap.containsKey("groupid")) {
                LayoutGroup group = (LayoutGroup) get(LayoutGroup.class, (String) dataMap.get("groupid"));
                groupMap.setLayoutgroup(group);
            }

            if (dataMap.containsKey("accountid")) {
                Account account = (Account) get(Account.class, (String) dataMap.get("accountid"));
                groupMap.setAccount(account);
            }

            if (dataMap.containsKey("companyid")) {
                Company company = (Company) get(Company.class, (String) dataMap.get("companyid"));
                groupMap.setCompany(company);
            }

            saveOrUpdate(groupMap);
            list.add(groupMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveInvoice : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveDefaultLayoutGroupAccountMap(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            DefaultGroupAccMap groupMap = new DefaultGroupAccMap();

            if (dataMap.containsKey("groupid")) {
                DefaultLayoutGroup group = (DefaultLayoutGroup) get(DefaultLayoutGroup.class, (String) dataMap.get("groupid"));
                groupMap.setDefaultlayoutgroup(group);
            }

            if (dataMap.containsKey("accountid")) {
                DefaultAccount account = (DefaultAccount) get(DefaultAccount.class, (String) dataMap.get("accountid"));
                if(account!=null){
                    groupMap.setAccountname(account.getName());
//                    groupMap.setGroupname(account.getGroup().getName());                    
                }
            }

            saveOrUpdate(groupMap);
            list.add(groupMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveDefaultLayoutGroupAccountMap : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveLayoutGroupMapForGroupTotal(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            Groupmapfortotal groupMap = new Groupmapfortotal();

            if (dataMap.containsKey("groupidtotal")) {
                LayoutGroup group = (LayoutGroup) get(LayoutGroup.class, (String) dataMap.get("groupidtotal"));
                groupMap.setGroupidtotal(group);
            }

            if (dataMap.containsKey("groupid")) {
                LayoutGroup group = (LayoutGroup) get(LayoutGroup.class, (String) dataMap.get("groupid"));
                groupMap.setGroupid(group);
            }

            if (dataMap.containsKey("action")) {
                groupMap.setAction((String) dataMap.get("action"));
            }

            saveOrUpdate(groupMap);
            list.add(groupMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveInvoice : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveDefaultLayoutGroupMapForGroupTotal(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            DefaultGroupMapForTotal groupMap = new DefaultGroupMapForTotal();

            if (dataMap.containsKey("groupidtotal")) {
                DefaultLayoutGroup group = (DefaultLayoutGroup) get(DefaultLayoutGroup.class, (String) dataMap.get("groupidtotal"));
                groupMap.setGroupidtotal(group);
            }

            if (dataMap.containsKey("groupid")) {
                DefaultLayoutGroup group = (DefaultLayoutGroup) get(DefaultLayoutGroup.class, (String) dataMap.get("groupid"));
                groupMap.setGroupid(group);
            }

            if (dataMap.containsKey("action")) {
                groupMap.setAction((String) dataMap.get("action"));
            }

            saveOrUpdate(groupMap);
            list.add(groupMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveDefaultLayoutGroupMapForGroupTotal : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveLayoutGroup(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            LayoutGroup group = null;

            if (dataMap.containsKey("id")) {
                group = (LayoutGroup) get(LayoutGroup.class, (String) dataMap.get("id"));
            } else {
                group = new LayoutGroup();
            }

            if (dataMap.containsKey("name")) {
                group.setName((String) dataMap.get("name"));
            }

            if (dataMap.containsKey("nature")) {
                group.setNature((Integer) dataMap.get("nature"));
            }

            if (dataMap.containsKey("sequence")) {
                group.setSequence((Integer) dataMap.get("sequence"));
            }

            if (dataMap.containsKey("showtotal")) {
                group.setShowtotal((Integer) dataMap.get("showtotal"));
            }

            if (dataMap.containsKey("showchild")) {
                group.setShowchild((Integer) dataMap.get("showchild"));
            }

            if (dataMap.containsKey("showchildacc")) {
                group.setShowchildacc((Integer) dataMap.get("showchildacc"));
            }
            if (dataMap.containsKey("excludeChildBalances")) {
                group.setExcludeChildAccountBalances((Boolean) dataMap.get("excludeChildBalances"));
            }
            if (dataMap.containsKey("addBlankRowBefore")) {
                group.setNumberofrows((Integer) dataMap.get("addBlankRowBefore"));
            }
            if (dataMap.containsKey("companyid")) {
                Company company = (Company) get(Company.class, (String) dataMap.get("companyid"));
                group.setCompany(company);
            }

            if (dataMap.containsKey("templateid")) {
                Templatepnl templatepnl = (Templatepnl) get(Templatepnl.class, (String) dataMap.get("templateid"));
                group.setTemplate(templatepnl);
            }

            if (dataMap.containsKey("parentid") && dataMap.get("parentid") != null) {
                group.setParent((LayoutGroup) get(LayoutGroup.class, (String) dataMap.get("parentid")));
            } else {
                group.setParent(null);
            }

            saveOrUpdate(group);
            list.add(group);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveInvoice : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject saveDefaultLayoutGroup(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            DefaultLayoutGroup group = null;
            if (dataMap.containsKey("id")) {
                group = (DefaultLayoutGroup) get(DefaultLayoutGroup.class, (String) dataMap.get("id"));
            } else {
                group = new DefaultLayoutGroup();
            }

            if (dataMap.containsKey("name")) {
                group.setName((String) dataMap.get("name"));
            }

            if (dataMap.containsKey("nature")) {
                group.setNature((Integer) dataMap.get("nature"));
            }

            if (dataMap.containsKey("sequence")) {
                group.setSequence((Integer) dataMap.get("sequence"));
            }

            if (dataMap.containsKey("showtotal")) {
                group.setShowtotal((Integer) dataMap.get("showtotal"));
            }

            if (dataMap.containsKey("showchild")) {
                group.setShowchild((Integer) dataMap.get("showchild"));
            }

            if (dataMap.containsKey("showchildacc")) {
                group.setShowchildacc((Integer) dataMap.get("showchildacc"));
            }
            if (dataMap.containsKey("excludeChildBalances")) {
                group.setExcludeChildAccountBalances((Boolean) dataMap.get("excludeChildBalances"));
            }

            if (dataMap.containsKey("templateid")) {
                DefaultTemplatePnL templatepnl = (DefaultTemplatePnL) get(DefaultTemplatePnL.class, (String) dataMap.get("templateid"));
                group.setTemplate(templatepnl);
            }

            if (dataMap.containsKey("parentid") && dataMap.get("parentid") != null) {
                group.setParent((DefaultLayoutGroup) get(DefaultLayoutGroup.class, (String) dataMap.get("parentid")));
            } else {
                group.setParent(null);
            }

            saveOrUpdate(group);
            list.add(group);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveInvoice : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject updatePnLTemplate(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            Templatepnl templatepnl = null;

            if (dataMap.containsKey("id")) {
                templatepnl = (Templatepnl) get(Templatepnl.class, (String) dataMap.get("id"));
            } else {
                templatepnl = new Templatepnl();
            }

            if (dataMap.containsKey("name")) {
                templatepnl.setName((String) dataMap.get("name"));
            }

            if (dataMap.containsKey("templatetitle")) {
                templatepnl.setTemplatetitle((String) dataMap.get("templatetitle"));
            }
            
            if (dataMap.containsKey("templateheading") && !StringUtil.isNullOrEmpty((String)dataMap.get("templateheading"))) {
                templatepnl.setTemplateheading((String) dataMap.get("templateheading"));
            }

            if (dataMap.containsKey("templateid")) {
                templatepnl.setTemplateid((Integer) dataMap.get("templateid"));
            }

            if (dataMap.containsKey("templatetype")) {
                templatepnl.setTemplatetype((Integer) dataMap.get("templatetype"));
            }

            if (dataMap.containsKey("status")) {
                templatepnl.setStatus((Integer) dataMap.get("status"));
            }
            if (dataMap.containsKey("isDefault")) {
                templatepnl.setDefaultTemplate((Boolean) dataMap.get("isDefault"));
            }

            if (dataMap.containsKey("companyid")) {
                Company company = (Company) get(Company.class, (String) dataMap.get("companyid"));
                templatepnl.setCompany(company);
            }

            if (dataMap.containsKey("deleted")) {
                templatepnl.setDeleted((Boolean) dataMap.get("deleted"));
            } else {
                templatepnl.setDeleted(false);
            }

            save(templatepnl);
            list.add(templatepnl);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveInvoice : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public int getMaxTemplateId(String companyid, boolean isAdminSubdomain, String countryid) throws ServiceException {
        int returnId = 0;
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        String query = "";        

        if(isAdminSubdomain){
            query = "select (max(templateid) + 1) from DefaultTemplatePnL where country.ID = ?";
            params.add(countryid);
        }else{
            query = "select (max(templateid) + 1) from Templatepnl where company.companyID = ?";
            params.add(companyid);
        }

        list = executeQuery( query, params.toArray());

        Iterator it = list.iterator();
        if (it.hasNext()) {
            Object obj = it.next();
            if (obj != null) {
                returnId = (Integer) obj;
            }
        }
        return returnId;
    }

    public boolean checkNameAlreadyExists(String templateid, String name, String companyid, String countryid, boolean isAdminSubdomain, int templateType) {
        boolean returnFlag = false;
        try {

            ArrayList params = new ArrayList();
            params.add(name);
            if (!isAdminSubdomain) {
            params.add(companyid);
            } else {
                params.add(countryid);
            }

            String condition = "";
            if (!StringUtil.isNullOrEmpty(templateid)) {
                condition = " and ID != ? ";
                params.add(templateid);
            }
            /* ERP-33433 same template name can be used for other template type
             * e.g. If template type is Profit and Loss and template name is CustomTemplate
             *  then I can use CustomTemplate as template name for template type Balance sheet also
             */
            condition += " and  templatetype= ? ";
            params.add(templateType);
            
            String query = "";
            if (!isAdminSubdomain) {
                query = "from Templatepnl where deleted = false and name = ? and company.companyID = ?" + condition;
            } else {
                query = "from DefaultTemplatePnL where deleted = false and name = ? and country.ID = ?" + condition;
            }
            List list = executeQuery( query, params.toArray());

            Iterator it = list.iterator();
            if (it.hasNext()) {
                returnFlag = true;
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnFlag;
    }
    
    public boolean updateDefaultTemplate(String companyid,int templatetype) throws ServiceException{
        boolean returnFlag = false;
        try {

            ArrayList params = new ArrayList();
            params.add(companyid);

            String condition = "";

            condition = " and templatetype = ? ";
            params.add(templatetype);

            String query = "";
            query = "update Templatepnl set defaultTemplate = false where deleted = false and defaultTemplate = true and company.companyID = ?" + condition;
            int count = executeUpdate(query, params.toArray());

//            Iterator it = list.iterator();
            if (count > 0) {
                returnFlag = true;
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDAOImpl.defaultAlreadyExists :" + ex.getMessage(), ex);
        }
        return returnFlag;
    }

    public boolean checkLayoutGroupNameAlreadyExists(String id, String name, String companyid, String templateid) {
        boolean returnFlag = false;
        try {

            ArrayList params = new ArrayList();
            params.add(name);
            params.add(templateid);
            params.add(companyid);
            String condition = "";
            if (!StringUtil.isNullOrEmpty(id)) {
                params.add(id);
                condition = " and ID != ? ";
            }
            String query = "from LayoutGroup where name = ? and template.ID = ? and company.companyID = ? " + condition;
            List list = executeQuery( query, params.toArray());

            Iterator it = list.iterator();
            if (it.hasNext()) {
                returnFlag = true;
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnFlag;
    }

    public boolean checkDefaultLayoutGroupNameAlreadyExists(String id, String name, String companyid, String templateid) {
        boolean returnFlag = false;
        try {
            ArrayList params = new ArrayList();
            params.add(name);
            params.add(templateid);
            String condition = "";
            if (!StringUtil.isNullOrEmpty(id)) {
                params.add(id);
                condition = " and ID != ? ";
            }
            String query = "from DefaultLayoutGroup where name = ? and template.ID = ? " + condition;
            List list = executeQuery( query, params.toArray());
            Iterator it = list.iterator();
            if (it.hasNext()) {
                returnFlag = true;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnFlag;
    }

    public void updateExistingSequnceNo(int sequence, String companyid, String templateid,String operator,String groupid) {
        boolean returnFlag = false;
        try {

            ArrayList params = new ArrayList();
            params.add(sequence);
            params.add(templateid);
            params.add(companyid);
            String query = "from LayoutGroup where sequence = ? and template.ID = ? and company.companyID = ?";
            List list = executeQuery( query, params.toArray());

            Iterator it = list.iterator();
            if (it.hasNext()) {
                Object obj = it.next();
                LayoutGroup group = (LayoutGroup) obj;
                /*
                *While editing sequence number, if new sequence number is less than the old sequence number, 
                *then old sequence number occured twice. Hence, Added group id check when result contains 
                *more than one result. In such case it updates sequence number for only one result, not other.
                *Due to this, updated group and other group in result have same sequence number.
                */
                if (group.getID().equals(groupid) && it.hasNext()) {
                    obj = it.next();
                    group = (LayoutGroup) obj;
                }
                updateExistingSequnceNo((sequence + 1), companyid, templateid,operator,groupid);
                if (operator.equals("addition")) {
                    group.setSequence(sequence + 1);
                } else if (operator.equals("substraction")) {
                    group.setSequence(sequence - 1);
            }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void updateDefaultLayoutGroupExistingSequnceNo(int sequence, String companyid, String templateid,String operator) {
        try {
            ArrayList params = new ArrayList();
            params.add(sequence);
            params.add(templateid);
            String query = "from DefaultLayoutGroup where sequence = ? and template.ID = ? ";
            List list = executeQuery( query, params.toArray());
            Iterator it = list.iterator();
            if (it.hasNext()) {
                Object obj = it.next();
                DefaultLayoutGroup group = (DefaultLayoutGroup) obj;
                updateDefaultLayoutGroupExistingSequnceNo((sequence + 1), companyid, templateid,operator);
                if (operator.equals("addition")) {
                    group.setSequence(sequence + 1);
                } else if (operator.equals("substraction")) {
                    group.setSequence(sequence - 1);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public KwlReturnObject updateCustomColumnmfield(String fieldlabel, String ModuleIDS, int lineitem, int maxlength, String fieldtooltip, String defaultval, boolean isAutoPopulateDefaultValue, boolean isForSalesCommission, boolean isForKnockOff) throws ServiceException {
        int numRows = 0;
        try {
            String Query = "", setQuery = "";
            ArrayList params = new ArrayList();
            params.add(fieldlabel);
            params.add(Constants.Custom_Record_Prefix + fieldlabel);
            params.add(maxlength);
            params.add(fieldtooltip);
            params.add(defaultval);
            params.add(isAutoPopulateDefaultValue);
            params.add(isForSalesCommission);
            params.add(isForKnockOff);

            if (!isForKnockOff) {
                /* ERP-32814
                 * Field is created for "isForKnockOff" Feature 
                 */
                setQuery += " , customcolumn = ? ";
                params.add(lineitem);
            }
            Query = "update FieldParams set fieldlabel = ?, fieldname = ?, maxlength = ?, fieldtooltip = ?, defaultValue = ?, isAutoPopulateDefaultValue = ?, isForSalesCommission = ?, isForKnockOff = ? " + setQuery + " where id in (" + ModuleIDS + ")";
            numRows = executeUpdate(Query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDAOImpl.updateCustomColumnmfield :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Custom field has been updated successfully.", null, null, numRows);
    }
    public KwlReturnObject updateCustomFieldActivation(int activation, String moduleid) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(activation);
        params.add(moduleid);
        String Query = "";
        Query = "update FieldParams set isActivated=? where id=?";
        int numRows = executeUpdate( Query, params.toArray());
        return new KwlReturnObject(true, "Custom field has been updated successfully.", null, null, numRows);
    }

    public KwlReturnObject updateDimensionParent(String fieldlabel, String ModuleIDS, int lineitem, String parentId) throws ServiceException {
        String Query = "";
        List list = new ArrayList();
        FieldParams fieldparams = (FieldParams) get(FieldParams.class, ModuleIDS);
        FieldParams fieldparamsParent = (FieldParams) get(FieldParams.class, parentId);
        if (fieldparams != null) {
            if (!StringUtil.isNullOrEmpty(parentId)) {
                fieldparams.setParentid(parentId);
                fieldparams.setParent(fieldparamsParent);
            }
            saveOrUpdate(fieldparams);
            list.add(fieldparams);
        }
        return new KwlReturnObject(true, "Custom field has been updated successfully.", null, list, list.size());
    }

    public int updateCustomProductfield(String ModuleIDS, String RelatedFieldIDs, String companyid) throws ServiceException {
        String Query = "";
        int numRows = 0;
        String relatedmoduleid = "";
        Query = "select relatedmoduleid from fieldparams  where  id in (" + ModuleIDS + ") and moduleid='30' and companyid='" + companyid + "'";
        List fieldParamObj = executeSQLQuery( Query);
        try {
            Iterator iterator = fieldParamObj.iterator();
            Object temp1 = (Object) iterator.next();
            relatedmoduleid = String.valueOf(temp1) != "null" ? String.valueOf(temp1) : "";
        } catch (Exception e) {
            return 0;
        }
        if (!StringUtil.isNullOrEmpty(relatedmoduleid)) {
            RelatedFieldIDs = relatedmoduleid + "," + RelatedFieldIDs;
        }
        Query = "UPDATE fieldparams SET relatedmoduleid ='" + RelatedFieldIDs + "'  where  id in (" + ModuleIDS + ") and moduleid='30' and companyid='" + companyid + "'";
        numRows = executeSQLUpdate( Query, new Object[]{});
        return numRows;
    }
    
    @Override
    public int updateCustomProductfieldIsAllowedToEdit(String ModuleIDS, int relatedModuleIsAllowEdit, String companyid) throws ServiceException {
        String Query = "";
        int numRows = 0;
        Query = "UPDATE fieldparams SET relatedmoduleisallowedit ='" + relatedModuleIsAllowEdit + "'  where  id in (" + ModuleIDS + ") and moduleid='30' and companyid='" + companyid + "'";
        numRows = executeSQLUpdate( Query, new Object[]{});
//        Query = "UPDATE fieldparams SET relatedmoduleisallowedit = ?  where  id in (?) and moduleid='30' and companyid=?";
//        numRows = executeSQLUpdate( Query, new Object[]{relatedModuleIsAllowEdit,ModuleIDS,companyid});
        return numRows;
    }

    public KwlReturnObject updateCustomfield(HashMap<String, Object> fieldmap) throws ServiceException {
        List list = new ArrayList();
        try {
            String fieldID = (String) fieldmap.get("id");
            FieldParams fieldparams = (FieldParams) get(FieldParams.class, fieldID);
            if (fieldparams != null) {
                if (fieldmap.containsKey("fieldlabel")) {
                    fieldparams.setFieldlabel((String) fieldmap.get("fieldlabel"));
                }
                saveOrUpdate(fieldparams);
                list.add(fieldparams);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDAOImpl.updatefieldlabel :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "Custom field has been updated successfully.", null, list, list.size());
    }

    public KwlReturnObject deleteAccountMapping(String mappingid) throws ServiceException {
        List list = new ArrayList();
        try {
            AccountMapping accountMapping = (AccountMapping) get(AccountMapping.class, mappingid);

            if (accountMapping != null) {
                delete(accountMapping);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("deleteAccountMapping : " + e.getMessage(), e);
        }
        return new KwlReturnObject(true, "Account mapping has been deleted successfully.", null, list, list.size());
    }

    public HashMap<String, Integer> getFieldParamsMap(HashMap<String, Object> requestParams, HashMap<String, String> replaceFieldMap) {
        // Following function is used to build Map of columns and Fields for specified modules which will be used to fetch json for each record from custom data table
        KwlReturnObject result = null;
        boolean skipRichTextArea=false;
        if(requestParams.containsKey("skipRichTextArea")){
            skipRichTextArea=(Boolean)requestParams.get("skipRichTextArea");
        }
        HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
        result = getFieldParams(requestParams);
        List lst = result.getEntityList();
        Iterator ite = lst.iterator();
        while (ite.hasNext()) {
            FieldParams tmpcontyp = (FieldParams) ite.next();
            Integer isref = -1;//Isreferencefield(tmpcontyp.getFieldtype());
            if (tmpcontyp.getFieldtype() == 4) {
                isref = 0;
            }
            if (tmpcontyp.getFieldtype() == 3) {
                isref = 3;
            }
            if (tmpcontyp.getFieldtype() == 7) {
                isref = 7;
            }
            if (tmpcontyp.getFieldtype() == 15) { // Rich Text Type
                if (skipRichTextArea) {
                    /*
                     * skipping Rich Text Type field 
                     */
                    continue;
                }
                isref = 15;
            }
            FieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getColnum());
            FieldMap.put(tmpcontyp.getFieldname() + "#" + tmpcontyp.getColnum(), isref);// added '#' while creating map collection for custom fields.
            // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
            replaceFieldMap.put(tmpcontyp.getFieldname(), "custom_" + tmpcontyp.getId());
        }
        return FieldMap;
    }

    @Override
    public HashMap<String, Object> getMaxGSTMappingColumn(String companyid) {
         HashMap<String, Object> FieldMap = new HashMap<String, Object>();
        try {
           //this function will return next column number for isForGST Custom Dimensions
            ArrayList params = new ArrayList();
            params.add(companyid);
            int autoNumber = 0;
            boolean success = false;
            String query = "select max(GSTMappingColnum) from FieldParams where companyid = ?";
            List list = executeQuery( query, params.toArray());
            if (!list.isEmpty() && list.get(0) != null) {
                autoNumber = Integer.parseInt(list.get(0).toString());
                success=true;
            }
            
            
            FieldMap.put("colNum", autoNumber);
            FieldMap.put("success", success);
           
        } catch (ServiceException ex) {
            FieldMap.put("success", false);
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return FieldMap;
    } 
    
    /*
     * getSumofChallanUsedQuantity used to get used quantity for Aged order work report.
     */
    
    public double getSumofChallanUsedQuantity(String interstoretransfer) throws ServiceException {
        double qty = 0;
        String query = "select sum(outqty) from grodstockoutistmapping where interstoretransfer=?";
        List list = executeSQLQuery(query, new Object[]{interstoretransfer});
        if (list.size() > 0 && list.get(0) != null) {
            qty = (double) list.get(0);
        }
        return qty;

    }
    public HashMap<String, Integer> getFieldParamsCustomMap(HashMap<String, Object> requestParams, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap) {
        // Following function is used to build Map of columns and Fields for specified modules which will be used to fetch json for each record from custom data table
        KwlReturnObject result = null;
        HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
        result = getFieldParams(requestParams);
        List lst = result.getEntityList();
        if(lst != null){
        Iterator ite = lst.iterator();
        while (ite.hasNext()) {
            FieldParams tmpcontyp = (FieldParams) ite.next();
            Integer isref = -1;//Isreferencefield(tmpcontyp.getFieldtype());
            if (tmpcontyp.getFieldtype() == 4) {
                isref = 0;
            }
            if (tmpcontyp.getFieldtype() == 3) {
                isref = 3;
            }
            FieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getColnum());
            FieldMap.put(tmpcontyp.getFieldname() + "#" + tmpcontyp.getColnum(), isref);// added '#' while creating map collection for custom fields.
            // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
            replaceFieldMap.put(tmpcontyp.getFieldname(), "custom_" + tmpcontyp.getId());
            if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype()==7 || tmpcontyp.getFieldtype()==12) // 4 - Drop Down, 7 - Multiselect drop down, 12 - checklist
            {
                customFieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
            }
            if (tmpcontyp.getFieldtype() == 3) {
                customDateFieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
            }
        }
        }
        return FieldMap;
    }
       public HashMap<String, Integer> getFieldParamsCustomMap(HashMap<String, Object> requestParams, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, String> customRichTextMap) {
        // Following function is used to build Map of columns and Fields for specified modules which will be used to fetch json for each record from custom data table
        KwlReturnObject result = null;
        HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
        result = getFieldParams(requestParams);
        List lst = result.getEntityList();
        if (lst != null) {
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                FieldParams tmpcontyp = (FieldParams) ite.next();
                Integer isref = -1;//Isreferencefield(tmpcontyp.getFieldtype());
                if (tmpcontyp.getFieldtype() == 4) {
                    isref = 0;
                }
                if (tmpcontyp.getFieldtype() == 3) {
                    isref = 3;
                }
                FieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getColnum());
                FieldMap.put(tmpcontyp.getFieldname() + "#" + tmpcontyp.getColnum(), isref);// added '#' while creating map collection for custom fields.
                // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
                replaceFieldMap.put(tmpcontyp.getFieldname(), "custom_" + tmpcontyp.getId());
                if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12) // 4 - Drop Down, 7 - Multiselect drop down, 12 - checklist
                {
                    customFieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
                }
                if (tmpcontyp.getFieldtype() == 3) {
                    customDateFieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
                }
                if (tmpcontyp.getFieldtype() == Constants.RICHTEXTAREA) {
                    customRichTextMap.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
                }
            }
        }
        return FieldMap;
    }

     public HashMap<String, Integer> getFieldParamsCustomMapForRows(HashMap<String, Object> requestParams, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap) {
        // Following function is used to build Map of columns and Fields for specified modules which will be used to fetch json for each record from custom data table
        KwlReturnObject result = null;
        HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
        result = getFieldParams(requestParams);
        List lst = result.getEntityList();
        if(lst !=null){
        Iterator ite = lst.iterator();
        while (ite.hasNext()) {
            FieldParams tmpcontyp = (FieldParams) ite.next();
            Integer isref = -1;//Isreferencefield(tmpcontyp.getFieldtype());
            if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7) {
                isref = 0;
            }
            if (tmpcontyp.getFieldtype() == 3) {
                isref = 3;
            }
            FieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getColnum());
            FieldMap.put(tmpcontyp.getFieldname() + "#" + tmpcontyp.getColnum(), isref);// added '#' while creating map collection for custom fields.
            // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
            replaceFieldMap.put(tmpcontyp.getFieldname(), "custom_" + tmpcontyp.getId());
            if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12)//|| tmpcontyp.getFieldtype()==7
            {
                customFieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
            }
            if (tmpcontyp.getFieldtype() == 3) {
                customDateFieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
            }
        }
        }
        return FieldMap;
    }
     /**
      * getFieldParamsCustomMapForRows function is overloaded to handle Rich Text field
      * @param requestParams
      * @param replaceFieldMap
      * @param customFieldMap
      * @param customDateFieldMap
      * @param customRichTextMap
      * @return 
      */
     public HashMap<String, Integer> getFieldParamsCustomMapForRows(HashMap<String, Object> requestParams, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap,  HashMap<String, String> customRichTextMap,HashMap<String, Integer> customRefcolMap) {
        // Following function is used to build Map of columns and Fields for specified modules which will be used to fetch json for each record from custom data table
        KwlReturnObject result = null;
        HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
        result = getFieldParams(requestParams);
        List lst = result.getEntityList();
        if(lst !=null){
        Iterator ite = lst.iterator();
        while (ite.hasNext()) {
            FieldParams tmpcontyp = (FieldParams) ite.next();
            Integer isref = -1;//Isreferencefield(tmpcontyp.getFieldtype());
            if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7) {
                isref = 0;
            }
            if (tmpcontyp.getFieldtype() == 3) {
                isref = 3;
            }
            FieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getColnum());
            FieldMap.put(tmpcontyp.getFieldname() + "#" + tmpcontyp.getColnum(), isref);// added '#' while creating map collection for custom fields.
            // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
            replaceFieldMap.put(tmpcontyp.getFieldname(), "custom_" + tmpcontyp.getId());
            customRefcolMap.put(tmpcontyp.getFieldname(), tmpcontyp.getRefcolnum());
            if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12)//|| tmpcontyp.getFieldtype()==7
            {
                customFieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
            }
            if (tmpcontyp.getFieldtype() == 3) {
                customDateFieldMap.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
            }
            if (tmpcontyp.getFieldtype() == Constants.RICHTEXTAREA) {
                customRichTextMap.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
            }
        }
        }
        return FieldMap;
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
            
            if (requestParams.containsKey(Constants.xtype) && requestParams.get(Constants.xtype) != null) {
                hql += " and fieldtype = " + (Integer) requestParams.get(Constants.xtype);
            }
            if (requestParams.containsKey(Constants.isforformulabuilder) && requestParams.get(Constants.isforformulabuilder) != null) {
                hql += " and customcolumn=0";
            }
            
            int moduleId = 0;
            if(requestParams.containsKey("moduleid")){
                moduleId = requestParams.get("moduleid")!=null ? Integer.parseInt(requestParams.get("moduleid").toString()) : 0;
            }
            
            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }
    
            if (requestParams.containsKey("customfield") && (Integer) requestParams.get("customfield") != null) {
                hql += " and customfield = 1";
            }
            if (requestParams.containsKey("globallevelfields") && Boolean.parseBoolean(requestParams.get("globallevelfields").toString())) {
                hql += " and customcolumn = 0";
            }            
            if (requestParams.containsKey("isforgstrulemapping") && Boolean.parseBoolean(requestParams.get("isforgstrulemapping").toString())) {
                hql += " and (gstconfigtype=4 or gstconfigtype=3)" ;
            }
//            else if (requestParams.containsKey("linelevelfields") && Boolean.parseBoolean(requestParams.get("linelevelfields").toString())) {
//                hql += " and customcolumn = 1";
//            }
            if (requestParams.containsKey("isActivated") && (Integer) requestParams.get("isActivated") != null) {
                int activatedFlag=(Integer) requestParams.get("isActivated");
                hql += " and isactivated = "+activatedFlag;
            }
            if (requestParams.containsKey("relatedmoduleid")) {
                hql += " and relatedmoduleid like '%" + requestParams.get("relatedmoduleid") + "%'";
            }
            if (requestParams.containsKey("parentid")) {
                hql += " and parentid = '" + requestParams.get("parentid") + "'";
            }
            if (requestParams.containsKey("checkForParent")) {
                hql += " and parentid is not null ";
            }
            if (moduleId != 0) {
                value.add(moduleId);
                hql += " and moduleid = ? ";
            }
            
            if (requestParams.containsKey("fieldids")) {
                hql += " and id in ("+requestParams.get("fieldids")+")";
            }
            
            /*When any custom field is created for Product & also available in CQ at line level 
            
             then sending it to CRM Side*/
            
            if (requestParams.containsKey("ProductrelatedModuleid")) {
                value.add((String) requestParams.get("companyId"));
                hql += " or (moduleid=30 and relatedmoduleid like '%22%' and customcolumn=0 and companyid=?)";
            }
            
            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }

            list = executeQuery( hql, value.toArray());


        } catch (Exception ex) {
             ex.printStackTrace();

        }
        return new KwlReturnObject(true, "", null, list, (list != null) ? list.size() : 0);
    }

    @Override
    public KwlReturnObject getFieldParamsIds(Map<String, Object> requestParams) {
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            
            hql = " select id from FieldParams ";
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

            list = executeQuery( hql, value.toArray());
           

        } catch (Exception ex) {
            ex.printStackTrace();

        }
       return new KwlReturnObject(true, "", null, list, (list != null) ? list.size() : 0);
    }
            
    public KwlReturnObject getFieldParamsUsingSql(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "select distinct fieldlabel as fieldlabel,id , colnum ,fieldtype from fieldparams ";
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
  
            if (requestParams.containsKey("customfield") && (Integer) requestParams.get("customfield") != null) {
                hql += " and customfield = 1";
            }
            if (requestParams.containsKey("isCustomDetailReport") && (Boolean) requestParams.get("isCustomDetailReport")) {
                if (requestParams.containsKey("isLinedetailReport") && (Boolean) requestParams.get("isLinedetailReport")) {
                    hql += " and moduleid in (2,6,24,10,12)";//featching only CI,VI,JE,CN,DN Moduleid fields
                } else {
                hql += " and moduleid in (2,6,24)";//featching only CI,VI,JE Moduleid fields
            }
            }
            if (requestParams.containsKey("reportid") && requestParams.get("reportid")!=null && Integer.parseInt(requestParams.get("reportid").toString())==Constants.CUSTOMER_REVENUE_REPORT) {
                if(requestParams.containsKey("recInModule") && requestParams.get("recInModule")!=null){
                    hql += " and moduleid in "+requestParams.get("recInModule");//featching only customer invoice, disposal invoice, consignment invoice, lease sales invoice
                }
            }
            if (requestParams.containsKey("reportid") && requestParams.get("reportid")!=null 
                    && (Integer.parseInt(requestParams.get("reportid").toString())==Constants.ACC_FIXED_ASSET_DETAILS_REPORTID 
                       || Integer.parseInt(requestParams.get("reportid").toString())==Constants.ACC_FIXED_DISPOSED_ASSET_REPORTID 
                        || Integer.parseInt(requestParams.get("reportid").toString())==Constants.ACC_FIXED_DEPRECIATION_DETAILS_REPORTID )) {
                hql += " and ((moduleid="+Constants.Acc_FixedAssets_AssetsGroups_ModuleId+" and customcolumn=0) or (moduleid="+Constants.Acc_FixedAssets_Details_ModuleId+" and customcolumn=1)) ";
            }
            // Check is for JE related thansaction only
            if (requestParams.containsKey("isJETransactions") && Boolean.parseBoolean(requestParams.get("isJETransactions").toString())) {
                hql += " and moduleid in ( 2,6,24,10,12,14,16,38,39 ) "; //Fetching only CI,VI,JE,CN,DN,MP,RP Moduleid fields
            }
            
            if (requestParams.containsKey("customdimension") && (Integer) requestParams.get("customdimension") != null) {
                hql += " and customfield = 0";
            }
            
            if (requestParams.containsKey("customcolumn") && (Integer) requestParams.get("customdimension") != null) {
                hql += " and customfield = 0";
            }
            if (requestParams.containsKey("iscustomcolumn")) {// Filter of Global and line level dimension
                hql += " and customcolumn = "+(Integer) requestParams.get("iscustomcolumn");
            }
            
            if (requestParams.containsKey("relatedmoduleid")) {
                hql += " and relatedmoduleid like '%" + requestParams.get("relatedmoduleid") + "%'";
            }
            if (requestParams.containsKey("excludeModule") && requestParams.get("excludeModule")!=null) {
                hql += " and moduleid != ? ";
                value.add(requestParams.get("excludeModule"));
            }
            if (requestParams.containsKey(Constants.isMultiEntity) && (Boolean) requestParams.get(Constants.isMultiEntity)) {
                hql += " and gstconfigtype = 1";
            }
            if (requestParams.containsKey("isForSalesSchema") && (Boolean) requestParams.get("isForSalesSchema")) {
                hql += " and isforsalescommission = '1' and moduleid=6";
            }
            boolean AvoidRedundent = false;
            if (requestParams.containsKey("AvoidRedundent") && (Boolean) requestParams.get("AvoidRedundent")) {
                AvoidRedundent = true;
            }
            if ((!((requestParams.containsKey("isCustomDetailReport") && (Boolean) requestParams.get("isCustomDetailReport"))
                    || (requestParams.get("reportid") != null && Integer.parseInt(requestParams.get("reportid").toString()) == Constants.CUSTOMER_REVENUE_REPORT))) || AvoidRedundent) {
                hql += " group by fieldlabel";
            }
            
            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
//            list = executeQuery( hql, value.toArray());
            list = executeSQLQuery( hql, value.toArray());
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    public KwlReturnObject getFieldParamsForCombo(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            ArrayList name = null;
            String hql = "";
            String queryString = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "select id,fieldlabel,id,moduleid,isessential from fieldparams ";
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                queryString = com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = queryString.indexOf("(");

                if (ind > -1) {
                    int index = Integer.valueOf(queryString.substring(ind + 1, ind + 2));
                    queryString = queryString.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                    value.remove(index);
                }
            }
            hql += queryString;
            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }

            if (requestParams.containsKey("customfield") &&  requestParams.get("customfield") != null) {
                hql += " and customfield = " + (Integer)requestParams.get("customfield");
            }
            if (requestParams.containsKey("relatedmoduleid")) {
                hql += " and relatedmoduleid like '%" + requestParams.get("relatedmoduleid") + "%'";
            }
            if (requestParams.containsKey("fieldlabel")) {
                hql += " and fieldlabel= '" + StringUtil.jsEncode(requestParams.get("fieldlabel"))+"'";
            }
            if (requestParams.containsKey("name") &&  requestParams.get("name") != null) {
                hql += " and fieldlabel != ? ";
                value.add(requestParams.get("name") );
            }
            if (requestParams.containsKey("isGroupby") && Boolean.parseBoolean(requestParams.get("isGroupby").toString())) {
                hql += " group by fieldname";
            }
            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            list = executeSQLQuery( hql, value.toArray());
            int count = list.size();


        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getFieldParamsforEdit(String fieldlabel, String ModuleIds, String companyid) throws ServiceException {
        List list = null;
        ArrayList params = new ArrayList();
        params.add(fieldlabel);
        params.add(companyid);
        String hql = "from FieldParams where fieldlabel=? and id not in (" + ModuleIds + ") and companyid=? ";
        list = executeQuery( hql, params.toArray());

        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws ServiceException 
     * @DESC : get Id of field combo data using value 
     */
    public KwlReturnObject getFieldCombo(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList name = null;
        List list = null;
        String hql = "";
        String queryString = "";
        ArrayList value = null;
        ArrayList orderby = null;
        ArrayList ordertype = null;
        String[] searchCol = null;
        hql = "from FieldComboData  ";
        if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
            name = new ArrayList((List<String>) requestParams.get("filter_names"));
            value = new ArrayList((List<Object>) requestParams.get("filter_values"));
            queryString = com.krawler.common.util.StringUtil.filterQuery(name, "where");
            int ind = queryString.indexOf("(");

            if (ind > -1) {
                int index = Integer.valueOf(queryString.substring(ind + 1, ind + 2));
                queryString = queryString.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
                value.remove(index);
            }
        }
        hql += queryString;
        list = executeQuery(hql, value.toArray());

        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public KwlReturnObject getDefaultHeaders(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        try {
            List<Integer> indexList = new ArrayList<Integer>();
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from DefaultHeader ";
            if (requestParams.get("filter_names") != null && requestParams.get("filter_values") != null) {
                name = new ArrayList((List<String>) requestParams.get("filter_names"));
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                hql += com.krawler.common.util.StringUtil.filterQuery(name, "where");
                int ind = hql.indexOf("(");

                while (ind > -1) {
                    int index = Integer.valueOf(hql.substring(ind + 1, ind + 2));
                    hql = hql.replace("(" + index + ")", "(" + value.get(index).toString() + ")");
//                    value.remove(index);
                    indexList.add(index);
                    
                    ind = hql.indexOf("(", ind+1);
                }
                Collections.reverse(indexList);
                for (Integer ctr : indexList){
                    value.remove(ctr.intValue());
                }
            }

            if (requestParams.containsKey(Constants.xtype) && requestParams.get(Constants.xtype) != null) {
                hql += " and xtype = " + (Integer) requestParams.get(Constants.xtype);
            }
            
            if (requestParams.containsKey(Constants.isforformulabuilder) && requestParams.get(Constants.isforformulabuilder) != null) {
                hql += " and islineitem='F'";
            }
            
            if (requestParams.get("searchcol") != null && requestParams.get("ss") != null) {
                searchCol = (String[]) requestParams.get("searchcol");
                hql += StringUtil.getSearchquery(requestParams.get("ss").toString(), searchCol, value);
            }
            if (requestParams.get("inDefault") != null && requestParams.get("defaultHeader") != null) {
                hql += " OR " + requestParams.get("inDefault").toString() + " in (" + requestParams.get("defaultHeader").toString() + ") and allowadvancesearch = 'T' ";
            }    
            if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
            }
            list = executeQuery( hql, value.toArray());


        } catch (Exception ex) {
            ex.printStackTrace();

        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getTextRangeFilterFields(Map<String, Object> requestParams) {
        List returnList = null;
        String hql = "";
        ArrayList params = new ArrayList();
        int listSize = 0;
        try {
            if (requestParams.containsKey("reportOrModuleId")) {
                params.add(requestParams.get("reportOrModuleId").toString());
                hql = " From RangeTextFiltersForAdvanceSearch where reportOrModule = ?";
                returnList = executeQuery(hql, params.toArray());
                listSize = returnList.size();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, returnList, listSize);
    }

    @Override
    public KwlReturnObject getCustomizeReportHeader(HashMap<String, Object> requestParams) {
         List returnList = null;
        try {
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;

            ArrayList params = new ArrayList();
            String condition = "";
            hql = "select crh from CustomizeReportHeader crh LEFT JOIN crh.defaultheader d ";
            
            if (requestParams.containsKey("reportId") && requestParams.get("reportId") != null) {
                condition += (condition.length() == 0 ? " where " : " and ") + "crh.reportId=?";
                params.add(Integer.parseInt((String) requestParams.get("reportId")));
            }
            
            if (requestParams.containsKey("moduleId")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "crh.moduleId=?";
                params.add(Integer.parseInt((String) requestParams.get("moduleId")));
            }
            
            if (requestParams.containsKey("isFormField")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "crh.formField=?";
                params.add((Boolean) requestParams.get("isFormField"));
            } else {
                if (!requestParams.containsKey("importHideShow")) {
                    condition += (condition.length() == 0 ? " where " : " and ") + "crh.formField=?";
                    params.add(false);
                }

            }
            if(requestParams.containsKey("countryId")) {
                condition += (condition.length() == 0 ? " where " : " and ") + " (d.countryID = ? OR d.countryID is null OR d.countryID = 0)";
                params.add(requestParams.get("countryId").toString());
            }
            hql += condition;
            returnList = executeQuery(hql, params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new KwlReturnObject(true, "", null, returnList, returnList.size());

    }

        @Override
    public KwlReturnObject getDefaultHeadersModuleJoinReference(String module) throws ServiceException {
          List list=null;
           try{
               ArrayList params = new ArrayList();
               String query = "from DefaultHeaderModuleJoinReference dhmjr where  dhmjr.module=?";
               params.add(module);               
               list = executeQuery( query, params.toArray());
           }catch(Exception ex){
               Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
           }
        return new KwlReturnObject(true, "", null, list, list.size());  
    }
        
    public KwlReturnObject getUsersByCompanyid(HashMap<String, Object> requestParams) {
        List returnList = null;
        try {
            String hql = "";

            hql = "from User us where us.company.companyID=?";

            ArrayList params = new ArrayList();
            params.add(String.valueOf(requestParams.get("companyid")));
            returnList = executeQuery( hql, params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    @Override
    public KwlReturnObject getCustomizeReportMapping(HashMap<String, Object> requestParams) {
        List returnList = null;
        try {
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            ArrayList name = null;
            hql = "from CustomizeReportMapping";
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
            ArrayList params = new ArrayList();
            String condition = "";

            if (requestParams.containsKey("moduleId")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "moduleId=?";
                params.add(Integer.parseInt((String) requestParams.get("moduleId")));
            }

            if (requestParams.containsKey("reportId")) {
                condition += (condition.length() == 0 ? " where " : " and ") + "reportId=?";
                params.add(Integer.parseInt((String) requestParams.get("reportId")));
            }

//            if (requestParams.containsKey("userId")) {
//                condition += (condition.length() == 0 ? " where " : " and ") + "user.userID=?";
//                params.add((String) requestParams.get("userId"));
//            }
//            if (requestParams.containsKey("companyid")) {  //companyid is already present in  Arraylist value 
//                condition += (condition.length() == 0 ? " where " : " and ") + "company.companyID=?";
//                params.add((String) requestParams.get("companyid"));
//            }
            hql += condition;
//        query="from PaymentMethod where company.companyID=?";
            returnList = executeQuery( hql, value.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new KwlReturnObject(true, "", null, returnList, returnList.size());

    }

    @Override
    public KwlReturnObject saveCustomizedReportFields(HashMap<String, Object> requestParams) {
        List list = new ArrayList();
        try {
            int moduleId = requestParams.containsKey("moduleId") ? Integer.parseInt(requestParams.get("moduleId").toString()) : 0;
            int reportId = requestParams.containsKey("reportId") ? Integer.parseInt(requestParams.get("reportId").toString()) : 0;
            boolean isFormField = requestParams.containsKey("isFormField") ? ((Boolean) requestParams.get("isFormField")) : false;
            boolean isLineField = requestParams.containsKey("isLineField") ? ((Boolean) requestParams.get("isLineField")) : false;
            boolean isOrderCustOrDimFields = requestParams.containsKey("isOrderCustOrDimFields") ? ((Boolean) requestParams.get("isOrderCustOrDimFields")) : false;
            String companyId = requestParams.get("companyId").toString();
            String userId = requestParams.get("userId").toString();
            if (isOrderCustOrDimFields) {
                if (requestParams.containsKey("data")) {
                    JSONArray jSONArray = (JSONArray) requestParams.get("data");
                    for (int i = 0; i < jSONArray.length(); i++) {
                        JSONObject jSONObject = new JSONObject(jSONArray.getString(i));
                        FieldParams fieldParams = (FieldParams) get(FieldParams.class, jSONObject.optString("id", ""));
                        if(fieldParams!=null){
                            fieldParams.setSequence(Integer.parseInt(jSONObject.optString("sequence", "0")));
                            saveOrUpdate(fieldParams);
                        }
                    }
                }
            } else {
//                String isformstr = "";
//                if (moduleId == Constants.Acc_Contract_Order_ModuleId || moduleId == Constants.Acc_Lease_Contract) {
//                    isformstr = " ";
//                } else {
//                    isformstr = " and reportId="+reportId+" and formField='" + isFormField + "'";
//                }
                List<CustomizeReportMapping> customizeReportMappings = find("from CustomizeReportMapping where moduleId=" + moduleId + " and reportId=" + reportId + " and formField=" + isFormField + " and company.companyID='" + companyId + "'");
//            if(customizeReportMappings.size()>0)
//                deleteAll(customizeReportMappings);
//            

                List<String> customizeReportMappingSet = new ArrayList<>();
//            Set<CustomizeReportMapping> customizeReportMappingObjectSet = new HashSet(customizeReportMappings);
                for (CustomizeReportMapping customizeReportMapping : customizeReportMappings) {
                    customizeReportMappingSet.add(customizeReportMapping.getDataIndex());
                }
                if (requestParams.containsKey("data")) {
                    JSONArray jSONArray = (JSONArray) requestParams.get("data");

                    if (moduleId == Constants.Acc_Product_ModuleId) {
                        deleteProductFieldsAndModulesMapping(companyId);
                    }
                    
                    for (int i = 0; i < jSONArray.length(); i++) {
                        
                        CustomizeReportMapping customizeReportMapping = new CustomizeReportMapping();
                        JSONObject jSONObject = new JSONObject(jSONArray.getString(i));
                        if (!StringUtil.isNullOrEmpty(jSONObject.optString("fieldDataIndex"))) {
                            if (customizeReportMappingSet.contains(jSONObject.optString("fieldDataIndex"))) {// code for existing data index for respected module.
                                List<CustomizeReportMapping> customizeMappingContainsList = find("from CustomizeReportMapping where moduleId=" + moduleId + " and reportId=" + reportId + " and formField=" + isFormField +" and isForProductandService=" + jSONObject.optBoolean("isForProductandServices", false) + " and company.companyID='" + companyId + "' and dataIndex='" + jSONObject.getString("fieldDataIndex") + "'");
                                customizeReportMapping = customizeMappingContainsList.get(0);
                                customizeReportMapping.setLineField(isLineField);
                                customizeReportMapping.setHidden(jSONObject.optBoolean("hidecol", false));
                                customizeReportMapping.setIsForProductandService(jSONObject.optBoolean("isForProductandServices", false));
                                if (jSONObject.has("id")) {
                                    CustomizeReportHeader crh = (CustomizeReportHeader) get(CustomizeReportHeader.class, jSONObject.getString("id"));
                                    if (crh != null) {
                                        customizeReportMapping.setCustomizeReportHeader(crh);
                                    }
                                }
                                User usr = (User) get(User.class, userId);
                                if (usr != null) {
                                        customizeReportMapping.setUser(usr);
                                }
                                customizeReportMapping.setReadOnlyField(jSONObject.optBoolean("isreadonlycol", false));
                                customizeReportMapping.setReportField(jSONObject.optBoolean("hidefieldfromreport", false));
                                //customizeReportMapping.setReadOnlyField(jSONObject.optBoolean("isreadonlycol", false));
                                customizeReportMapping.setUserManadatoryField(jSONObject.optBoolean("isUserManadatoryField", false));
                                customizeReportMapping.setFieldLabelText(jSONObject.optString("fieldlabeltext", ""));
                                customizeReportMappingSet.remove(jSONObject.optString("fieldDataIndex"));
                            } else {// if data index not exist in crm table.
                                customizeReportMapping.setDataHeader(jSONObject.optString("fieldname"));
                                customizeReportMapping.setDataIndex(jSONObject.optString("fieldDataIndex"));
                                customizeReportMapping.setManadatoryField(jSONObject.optBoolean("isManadatoryField", false));
                                customizeReportMapping.setHidden(jSONObject.optBoolean("hidecol", false));
                                customizeReportMapping.setReadOnlyField(jSONObject.optBoolean("isreadonlycol", false));
                                customizeReportMapping.setReportField(jSONObject.optBoolean("hidefieldfromreport", false));
                                customizeReportMapping.setUserManadatoryField(jSONObject.optBoolean("isUserManadatoryField", false));
                                customizeReportMapping.setFieldLabelText(jSONObject.optString("fieldlabeltext", ""));
                                customizeReportMapping.setIsForProductandService(jSONObject.optBoolean("isForProductandServices", false));
                                if (jSONObject.has("id")) {
                                    CustomizeReportHeader crh = (CustomizeReportHeader) get(CustomizeReportHeader.class, jSONObject.getString("id"));
                                    if (crh != null) {
                                        customizeReportMapping.setCustomizeReportHeader(crh);
                                    }
                                }
                                customizeReportMapping.setModuleId(moduleId);
                                customizeReportMapping.setReportId(reportId);
                                customizeReportMapping.setFormField(isFormField);
                                customizeReportMapping.setLineField(isLineField);
                                customizeReportMapping.setCompany((Company) get(Company.class, companyId));
                                User user = (User) get(User.class, userId);
                                if (user != null) {
                                    customizeReportMapping.setUser(user);
                                }
                            }
                            if (moduleId == Constants.Acc_Product_ModuleId) {
                                if (jSONObject.has("modulestoshowintheirforms") && !StringUtil.isNullOrEmpty(jSONObject.getString("modulestoshowintheirforms"))) {
                                    String[] moduleIDs = jSONObject.getString("modulestoshowintheirforms").split(",");
                                    Set<ProductFieldsAndModulesMapping> modulemapping = new HashSet();
                                    ProductFieldsAndModulesMapping pfm = null;
                                    for (int s = 0; s < moduleIDs.length; s++) {
                                        pfm = new ProductFieldsAndModulesMapping();
                                        pfm.setModuleid(Integer.parseInt(moduleIDs[s]));
                                        pfm.setFieldid(customizeReportMapping);
                                        modulemapping.add(pfm);
                                    }
                                    customizeReportMapping.setModulesMapping(modulemapping);
                                }
                            }
                        }
                        saveOrUpdate(customizeReportMapping);
                        list.add(customizeReportMapping);
                    }
                    boolean callfromImport = false; // check if call this function from import
                    if (requestParams.containsKey("callfromImport")) {
                        callfromImport = (Boolean) requestParams.get("callfromImport");
                    }
                    if (!callfromImport) {
                        List<String> totalStringList = new ArrayList<String>();
                        for (String customizeReportMappingObj : customizeReportMappingSet) {
                            totalStringList.add(customizeReportMappingObj);
                        }
                        List<CustomizeReportMapping> customizeReportMappingList = new ArrayList<CustomizeReportMapping>();
                        if (!totalStringList.isEmpty()) {
                            
                            
                            String query = "from CustomizeReportMapping where moduleId=" + moduleId + " and reportId=" + reportId + " and formField=" + isFormField + " and user.userID='" + userId + "' and dataIndex in (:totalStringList)";
                            List<List> values = new ArrayList<List>();
                            values.add(totalStringList);
                            customizeReportMappingList = executeCollectionQuery(query, Collections.singletonList("totalStringList"), values);

                            for (CustomizeReportMapping customizeReportMapping : customizeReportMappingList) {
                                customizeReportMapping.setHidden(false);
                            }
                        }
                        saveAll(customizeReportMappingList);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public int deleteProductFieldsAndModulesMapping(String companyId) throws ServiceException {
        int numofRows = 0;
        String hql = "";
        try {
            String delQuery = " delete pfm from productfieldsandmodulesmapping pfm inner join customizereportmapping crm on crm.id=pfm.fieldid  where crm.company = ? ";
            numofRows = executeSQLUpdate( delQuery, new String[]{companyId});
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDaoImpl.deleteProductFieldsAndModulesMapping : " + ex.getMessage(), ex);
        }
        return numofRows;
    }
    
    public KwlReturnObject getCustomCombodata(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        List list = null;
        List resultlist = new ArrayList();
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            boolean isDDflag = false;
            hql = "from FieldComboData ";
            String searchText = "",roleid="",fieldid="";
            int level = 0;
            boolean isNotSearchText = true;
            String mysqlquery="",userid="";
            List params = new ArrayList();
            List list1 = new ArrayList();
            boolean usersVisibilityFlow=false;
            boolean usersspecificinfoFlow=false;
            if(requestParams.containsKey("usersVisibilityFlow") && requestParams.get("usersVisibilityFlow")!=null ){
               usersVisibilityFlow= (Boolean)requestParams.get("usersVisibilityFlow"); 
            }
            if(requestParams.containsKey("usersspecificinfoFlow") && requestParams.get("usersspecificinfoFlow")!=null ){
               usersspecificinfoFlow= (Boolean)requestParams.get("usersspecificinfoFlow"); 
            }
            if(requestParams.containsKey("roleid")){
                roleid =requestParams.get("roleid").toString();
            }
            if(requestParams.containsKey("isDDflag") && requestParams.get("isDDflag")!=null ){
               isDDflag = (Boolean)requestParams.get("isDDflag"); 
            }
            if(requestParams.containsKey("searchText")){
                searchText = requestParams.get("searchText")!=null ? requestParams.get("searchText").toString() : "";
            }            
            if(requestParams.containsKey("fieldid")){
                fieldid =requestParams.get("fieldid").toString();
            }
            /*
               here we get the user specific result
               user can access the data to which he is mapped
               this condition will be executed when usersVisibilityFlow and usersspecificinfoFlow are on
            
            */
            if (!(roleid.equalsIgnoreCase("1")) && usersVisibilityFlow && usersspecificinfoFlow &&requestParams.containsKey("userid") && !requestParams.containsKey("parentid")) {
                mysqlquery = "select fieldcombodata.id from usergroupfieldcombomapping "
                        + "INNER JOIN  fieldcombodata on fieldcombodata.id=usergroupfieldcombomapping.fieldcombodata "
                        + "INNER JOIN usersgroup  on  usergroupfieldcombomapping.usersgroup=usersgroup.id "
                        + "INNER JOIN usersgroupmapping ON usersgroupmapping.usersgroup=usersgroup.id "
                        + "WHERE usersgroupmapping.user=? and fieldcombodata.fieldid=? ";

                userid = (String) requestParams.get("userid");
                params.add(userid);
                params.add(fieldid);
                list1 = executeSQLQuery(mysqlquery, params.toArray());
                Iterator itr = list1.iterator();
                while (itr.hasNext()) {
                    String id = (String) itr.next();
                    FieldComboData fieldComboData = (FieldComboData) get(FieldComboData.class, id);
                    Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
                    tmplist[0] = fieldComboData;
                    tmplist[1] = level;
                    resultlist.add(tmplist);
                    if (isNotSearchText && !isDDflag ) {
                        tmplist[2] = getChildGroups(fieldComboData, resultlist, level); //For each master Group Leaf is true
                        tmplist[3] = null;//parent group

                    }

                }
            } else if (requestParams.containsKey("parentid")) {
                hql = "from FieldComboDataMapping fdm where fdm.child.fieldid =  ?  and fdm.child.deleteflag =  ?  and fdm.child.parent is null and fdm.parent.id=? ";
                value = new ArrayList((List<Object>) requestParams.get("filter_values"));
                value.add(2, (String) requestParams.get("parentid"));
       
                if (requestParams.get("ss") != null) {
                    isNotSearchText = false;
                    searchCol = new String[]{"fdm.child.value", "fdm.child.itemdescription"};
                    Map map = StringUtil.insertParamSearchStringMap(value, requestParams.get("ss").toString(), 2);
                    StringUtil.insertParamSearchString(map);
                    hql += StringUtil.getSearchString(requestParams.get("ss").toString(), "and", searchCol);
                }

                hql += " ORDER BY fdm.child.itemsequence asc ";
                list = executeQuery( hql, value.toArray());
                Iterator itr = list.iterator();

                level = 0;
                while (itr.hasNext()) {
                    Object listObj = itr.next();
                    FieldComboDataMapping fieldComboDataMapping = (FieldComboDataMapping) listObj;
                    FieldComboData fieldComboData = fieldComboDataMapping.getChild();
                    Object listObj1 = fieldComboDataMapping.getChild();
                    Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
                    tmplist[0] = listObj1;
                    tmplist[1] = level;
                    resultlist.add(tmplist);
                    tmplist[2] = getChildGroups(fieldComboData, resultlist, level); //For each master Group Leaf is true
                    tmplist[3] = null;//parent group

                }
            } else {
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
                
                /*
                  Below if block is used to search the Dimemsion based on the Dimension value or Item Description
                */
                if (requestParams.get("ss") != null) {
                    isNotSearchText = false;
                        searchCol = new String[]{"value","itemdescription"};
                        Map map = StringUtil.insertParamSearchStringMap(value, requestParams.get("ss").toString(), 2);
                        StringUtil.insertParamSearchString(map);
                    hql += StringUtil.getSearchString(requestParams.get("ss").toString(), "and", searchCol);
                }
                if (!StringUtil.isNullOrEmpty(searchText)) {
                    String valArr[] = searchText.split(",");
                    String val = "";
                    for (int index = 0; index < valArr.length; index++) {
                        val += "'" + valArr[index] + "',";
                    }
                    if (val.length() > 1) {
                        val = val.substring(0, val.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(val)) {
                        hql += " and value IN (" + val + ")";
                    }
                }
                if (isNotSearchText && !isDDflag) {
                    hql += " and parent is null";
                }
                if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
                    orderby = new ArrayList((List<String>) requestParams.get("order_by"));
                    ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
                    hql += com.krawler.common.util.StringUtil.orderQuery(orderby, ordertype);
                }
                list = executeQuery( hql, value.toArray());
                Iterator itr = list.iterator();

                level = 0;
                while (itr.hasNext()) {
                    Object listObj = itr.next();
                    FieldComboData fieldComboData = (FieldComboData) listObj;
                    Object tmplist[] = new Object[4]; //[0:List, 1:level, 2:leaf, 3:parent group]
                    tmplist[0] = listObj;
                    tmplist[1] = level;
                    resultlist.add(tmplist);
                    if (isNotSearchText && !isDDflag ) {
                        tmplist[2] = getChildGroups(fieldComboData, resultlist, level); //For each master Group Leaf is true
                        tmplist[3] = null;//parent group
                    }

                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();

        } finally {
            return new KwlReturnObject(true, "", null, resultlist, resultlist.size());
        }
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

    @Override
    public KwlReturnObject insertfield(HashMap<String, Object> requestParams) throws ServiceException {
        List<FieldParams> list = new ArrayList<FieldParams>();
        boolean success = false;
        try {
            FieldParams user = (FieldParams) setterMethod( requestParams, "com.krawler.common.admin.FieldParams", "Id");
            list.add(user);
            success = true;
        } catch (Exception e) {
            success = false;
            System.out.println("Error is " + e);
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            return new KwlReturnObject(success, "New column created successfully.<br/> Please close the tab and open again to use the new field", "-1", list, list.size());
        }
    }
    
    /**
      * This method saves and updates Address fields mapped against Dimensions which are created "isForGST"
      */
    @Override 
    public KwlReturnObject saveOrUpdateAddressFieldForGSTDimension(HashMap<String, Object> reqMap) throws ServiceException {
        List list = new ArrayList();
        try {
            AddressFieldDimensionMapping addressFieldDimensionMapping = null;
            if (reqMap.containsKey("id") && reqMap.get("id") != null) {
                addressFieldDimensionMapping = (AddressFieldDimensionMapping) get(AddressFieldDimensionMapping.class, (String) reqMap.get("id"));
            } else {
                addressFieldDimensionMapping = new AddressFieldDimensionMapping();
            }
            if (reqMap.containsKey("addressField") && reqMap.get("addressField") != null) {                
                addressFieldDimensionMapping.setAddressField((String) reqMap.get("addressField"));
            }
            if (reqMap.containsKey("fieldId") && reqMap.get("fieldId") != null) {
                FieldParams comboData = (FieldParams) get(FieldParams.class, (String) reqMap.get("fieldId"));
                addressFieldDimensionMapping.setDimension(comboData);
            }
            if (reqMap.containsKey("company") && reqMap.get("company") != null) {
                Company company = (Company) get(Company.class, (String) reqMap.get("company"));
                addressFieldDimensionMapping.setCompany(company);
            }
            saveOrUpdate(addressFieldDimensionMapping);
            list.add(addressFieldDimensionMapping);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    public KwlReturnObject getDimensionMappedAddressFieldID(HashMap<String, Object> requestParams){        
        List list = new ArrayList();
        boolean success=false;
        try {            
            ArrayList params = new ArrayList();
            String dimension=requestParams.get("fieldId").toString();
            String company=requestParams.get("company").toString();
            params.add(dimension);
            params.add(company);
            String query = "from AddressFieldDimensionMapping where dimension.id=? and company.companyID=?";
            list = executeQuery(query, params.toArray());
            success=true;
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
           return new KwlReturnObject(success, null, null, list, list.size());        
        }
    }
    /*
     * This method returns record from AddressFieldDimensionMapping with provided addressfield
     */
    public KwlReturnObject getDimensionMappedWithSameAddressField(HashMap<String, Object> requestParams) throws ServiceException{        
        List list = new ArrayList();
        boolean success=false;                  
            ArrayList params = new ArrayList();
            String dimension=requestParams.get("fieldId").toString();
            String company=requestParams.get("company").toString();
            String addressField=requestParams.get("addressField").toString();
//            params.add(dimension);
            params.add(company);
            params.add(addressField);
            String query = "from AddressFieldDimensionMapping where company.companyID=? and addressField=?";
            list = executeQuery(query, params.toArray());
            success=true;       
        return new KwlReturnObject(success, null, null, list, list.size());    
    }
    
    @Override
    public boolean updateField(HashMap<String, Object> requestParams) throws ServiceException {
        boolean success = false;
        try {
            String fieldLabel = requestParams.get("Fieldlabel").toString();
            String fieldId = requestParams.get("fieldId").toString();
            String companyId = requestParams.get("companyId").toString();
            String updateQuery = "update fieldparams set fieldlabel=?  where id=? and companyid=?";
            int i = executeSQLUpdate( updateQuery, new Object[]{fieldLabel, fieldId, companyId});
            if (i > 0) {
                success = true;
            }
        } catch (Exception e) {
            success = false;
            System.out.println("Error is " + e);
        } finally {
            return success;
        }
    }

    @Override
    public void storeDefaultCstmData(HashMap<String, Object> requestParams) throws ServiceException {
        if (requestParams.get(Constants.RES_success).toString().equals("1") && requestParams.get(Constants.defaultvalue) != null && !StringUtil.isNullOrEmpty(requestParams.get(Constants.defaultvalue).toString())) {
            String refcolumnname = null;
            if (requestParams.containsKey(Constants.Refcolnum)) {
                refcolumnname = Constants.Custom_column_Prefix + requestParams.get(Constants.Refcolnum).toString();
            }
            String Colnum = Constants.Custom_column_Prefix + requestParams.get(Constants.Colnum).toString();
            String defaultvalue = requestParams.get(Constants.defaultvalue).toString();
            Integer xtype = Integer.parseInt(requestParams.get(Constants.Fieldtype).toString());
            try {
                String companyid = (String) requestParams.get("companyid");
                Integer moduleid = (Integer) requestParams.get(Constants.moduleid);
                storeDefaultCstmData(companyid, refcolumnname, moduleid, Colnum, defaultvalue, xtype);
            } catch (Exception ex) {
                throw ServiceException.FAILURE("AccCostCenterImpl.storeDefaultCstmData : " + ex.getMessage(), ex);
                // throw ServiceException.FAILURE(
                // "fieldManagerDAOImpl.storeDefaultCstmData", ex);
            }
        }
    }

    public boolean storeDefaultCstmData(String companyid, String refcolumnname, int moduleid, String fieldcolumn, String fieldValue, int xtype) throws JSONException, com.krawler.utils.json.base.JSONException {

        String tablename = Constants.Acc_BillInv_custom_data_classpath;
        String primarycolumn = "id";//AccCostCenterController.getPrimarycolumn(moduleid);
        String modulename = Constants.Acc_BillInv_custom_data_classpath;
        String cstmcolumn = "accBillInvCustomData";//AccCostCenterController.getmoduledataRefName(moduleid);
        String mainTableName = "com.krawler.hql.accounting.JournalEntry";//AccCostCenterController.getmoduledataRefName(moduleid);
        String primarycolumnid = "journalentryId";
        if (!StringUtil.isNullOrEmpty(tablename)) {
            if (moduleid == Constants.Acc_Product_Master_ModuleId) {
                modulename = Constants.Acc_Product_custom_data_classpath;
            }
            try {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                String updatequery = null;
                fieldcolumn = fieldcolumn.replace(Constants.C, Constants.c);
                String where = " where  company.companyID = '" + companyid + "' and moduleId='" + moduleid + "'  ";
                if (!StringUtil.isNullOrEmpty(refcolumnname)) {
                    if (!StringUtil.isNullOrEmpty(fieldValue))//Update sort column and values column in case of multiselect drop-down for the leads whose entry present in the customdata table
                    {
                        updatequery = "update " + modulename + " set " + fieldcolumn + "='" + fieldValue + "' , " + refcolumnname + "='" + fieldValue.split(",")[0] + "' ";
                    }
                } else {
                    if (!StringUtil.isNullOrEmpty(fieldValue))//Update value column for the leads whose entry present in the customdata table
                    {
                        updatequery = "update " + modulename + " set " + fieldcolumn + "='" + fieldValue + "' ";
                    }
                }
                JSONObject resultJson = new JSONObject();
                requestParams = new HashMap<String, Object>();
                KwlReturnObject kmsg = null;
                if (updatequery != null) {
                    updatequery += where;
                    requestParams.put(Constants.hql, updatequery);
                    kmsg = storeDefaultCustmData(requestParams);
                    resultJson.put(Constants.success1, kmsg.isSuccessFlag());
                    resultJson.put(Constants.msg1, kmsg.getMsg());
                }
                where = " and company = '" + companyid + "'  and deleteflag=0 ";
                if (!StringUtil.isNullOrEmpty(fieldValue)) {
                    //Batch insert for the leads whose entry not present in the customdata table.
                    String query = returnQuery(moduleid);
                    if (moduleid != (Constants.Acc_Product_Master_ModuleId) && !StringUtil.isNullOrEmpty(query)) {
                        String hqlselectquery = " select id,company from  journalentry  where accjecustomdataref is  null and id in(" + query + ") " + where;
                        String updateJeQuery = "update journalentry set accjecustomdataref=id  where accjecustomdataref is  null and id =?";
                        List list = getColumnsForInsert(hqlselectquery);
                        String hqlquery = "insert into accjecustomdata (journalentryId,company,moduleId," + fieldcolumn + ") values(?,?,?,?)";
                        Iterator<Object> it = list.iterator();
                        while (it.hasNext()) {
                            Object[] temp = (Object[]) it.next();
                            int a = executeSQLUpdate( hqlquery, new String[]{temp[0].toString(), temp[1].toString(), String.valueOf(moduleid), fieldValue});
                            a = executeSQLUpdate( updateJeQuery, new String[]{temp[0].toString()});
                        }
                    } else if(!StringUtil.isNullOrEmpty(query)){
                        String hqlselectquery = " select id,company from  product  where accproductcustomdataref is  null and id in(" + query + ") " + where;
                        String updateJeQuery = "update product set accproductcustomdataref=id  where accproductcustomdataref is  null and id =?";
                        List list = getColumnsForInsert(hqlselectquery);
                        String hqlquery = "insert into accproductcustomdata (productId,company,moduleId," + fieldcolumn + ") values(?,?,?,?)";
                        Iterator<Object> it = list.iterator();
                        while (it.hasNext()) {
                            Object[] temp = (Object[]) it.next();
                            int a = executeSQLUpdate( hqlquery, new String[]{temp[0].toString(), temp[1].toString(), String.valueOf(moduleid), fieldValue});
                            a = executeSQLUpdate( updateJeQuery, new String[]{temp[0].toString()});
                        }
                    }

                } else {
                }


                /*
                 * where = " and company.companyID = '" + companyid + "' and
                 * deleteflag=0 "; if (!StringUtil.isNullOrEmpty(fieldValue)) {
                 * //Batch insert for the leads whose entry not present in the
                 * customdata table. String hqlselectquery = " select " +
                 * primarycolumn + ",company,'" + fieldValue + "' from " +
                 * tablename + " where " + cstmcolumn + " is null ";
                 *
                 * String hqlquery = "insert into " + modulename + " (" +
                 * primarycolumn + ",company," + fieldcolumn + ") " +
                 * hqlselectquery; requestParams.clear();
                 * requestParams.put(Constants.hql, hqlquery + where);
                 * resultJson = new JSONObject(); kmsg =
                 * storeDefaultCstmData(requestParams);
                 * resultJson.put(Constants.success2, kmsg.isSuccessFlag());
                 * resultJson.put(Constants.msg2, kmsg.getMsg());
                 *
                 * //Update customdata field in crmlead table updatequery =
                 * "update " + tablename + " set " + cstmcolumn + "=" +
                 * primarycolumn + " where " + cstmcolumn + " is null ";
                 *
                 * requestParams.clear(); requestParams.put(Constants.hql,
                 * updatequery + where); resultJson = new JSONObject(); kmsg =
                 * storeDefaultCstmData(requestParams);
                 * resultJson.put(Constants.RES_success, kmsg.isSuccessFlag());
                 * resultJson.put(Constants.RES_msg, kmsg.getMsg()); } else {
                }
                 */
//                }
                return true;
            } catch (ServiceException e) {
                e.printStackTrace();
//                logger.warn(e.getMessage(), e);
                return false;
            }
        }
        return false;
    }
    
    private String returnQuery(int moduleid) {
        String query = "";
        switch (moduleid) {
            case 2:
                query = "select journalentry from invoice union select journalentry from billinginvoice";
                break;
            case 6:
                query = "select journalentry from goodsreceipt union select journalentry from billinggr";
                break;
            case 10:
                query = "select journalentry from debitnote union select journalentry from billingdebitnote";
                break;
            case 12:
                query = "select journalentry from creditnote union select journalentry from billingcreditnote";
                break;
            case 14:
                query = "select journalentry from payment union select journalentry from billingpayment";
                break;
            case 16:
                query = "select journalentry from receipt union select journalentry from billingreceipt";
                break;
            case 30:
                query = "select id from product";
                break;
        }

        return query;

    }

    public KwlReturnObject storeDefaultCustmData(HashMap<String, Object> requestParams) throws ServiceException {
        List<Integer> list = new ArrayList<Integer>();
        String hql = (String) requestParams.get("hql");
        ArrayList params = requestParams.containsKey("params") ? (ArrayList) requestParams.get("params") : new ArrayList();
        boolean success = false;
        try {
            int user = executeUpdate( hql, params.toArray());
            list.add(user);
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            System.out.println("Error is " + e);
        } finally {
            return new KwlReturnObject(success, "Field default data added successfully", "-1", list, list.size());
        }
    }

    public List getColumnsForInsert(String selectQuery) throws ServiceException {
        List<Integer> list = new ArrayList<Integer>();
        try {
            list = executeSQLQuery( selectQuery);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public List checkDuplicateNameOfCustomColumn(String moduleName, String companyid, String fieldlabel) throws ServiceException {

        List<Integer> list = new ArrayList<Integer>();
        try {
            String selectQuery = "select dh.id from default_header dh inner join modules mo on mo.id = dh.module where mo.modulename = ? "
                    + "and (dh.customflag = 'F' or dh.customflag = '0') and dh.id not in (select defaultHeader "
                    + "from column_header where company = ?)  and defaultHeader = ?"
                    + " UNION "
                    + "select ch.id from column_header ch inner join default_header dh on dh.id = ch.defaultHeader "
                    + "inner join modules mo on mo.id = dh.module where company = ? and mo.modulename = ? and "
                    + "(ch.newHeader = ? or dh.defaultHeader = ?)";

            list = executeSQLQuery( selectQuery, new String[]{moduleName, companyid, fieldlabel, companyid, moduleName, fieldlabel, fieldlabel});

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return list;
    }

    public int storeDefaultCstmDataSQL(String query) throws ServiceException {

        int a = 0;
        try {
            a = executeSQLUpdate( query);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 1;
    }

    public KwlReturnObject insertfieldcombodata(HashMap<String, Object> comborequestParams) throws ServiceException {
        List<FieldComboData> list = new ArrayList<FieldComboData>();
        boolean success = false;
        try {
            FieldComboData user = (FieldComboData) setterMethod( comborequestParams, "com.krawler.common.admin.FieldComboData", "Id");
            list.add(user);
            success = true;
        } catch (Exception e) {
            success = false;
            System.out.println("Error is " + e);
        } finally {
            return new KwlReturnObject(success, "FieldComboData added successfully", "-1", list, list.size());
        }
    }

    @Override
    public String getfieldcombodatabyids(String FieldIDs) throws ServiceException {
        List list = new ArrayList<FieldComboData>();
        boolean success = false;
        String Values = "";
        try {

            String query = "from FieldComboData where id in (" + FieldIDs + ")";
            list = executeQuery( query);
            if (list.size() > 0) {
                Iterator ite = list.iterator();
                while (ite.hasNext()) {
                    FieldComboData FieldValue = (FieldComboData) ite.next();
                    Values += FieldValue.getValue() + ",";
                }
            }
            if (!Values.isEmpty()) {
                Values = Values.substring(0, Values.length() - 1);
            }
            success = true;
        } catch (Exception e) {
            success = false;
            System.out.println("Error is " + e);
        } finally {
            return Values;
        }
    }

    @Override
    public KwlReturnObject getIBGBankDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        String accCompanyId = (String) dataMap.get("companyId");
        List params = new ArrayList();
        params.add(accCompanyId);

        String condition = "";

        if (dataMap.containsKey("bankAccountId") && dataMap.get("bankAccountId") != null) {
            condition += " and ibd.account.ID=? ";
            params.add((String) dataMap.get("bankAccountId"));
        }

        String query = "From IBGBankDetails ibd WHERE ibd.company.companyID=? " + condition;
        list = executeQuery( query, params.toArray());

        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getfieldcombodata(HashMap<String, Object> comborequestParams) throws ServiceException {
        List list = new ArrayList<FieldComboData>();
        boolean success = false;
        try {
            ArrayList params = new ArrayList();
            params.add(comborequestParams.get("Fieldid"));
            params.add(comborequestParams.get("Value"));
            String query = "from FieldComboData where Fieldid=? and Value=? ";
//            FieldComboData user = (FieldComboData) setterMethod( comborequestParams, "com.krawler.common.admin.FieldComboData", "Id");
            list = executeQuery( query, params.toArray());
//            list.add(user);
            success = true;
        } catch (Exception e) {
            success = false;
            System.out.println("Error is " + e);
        } finally {
            return new KwlReturnObject(success, "", "-1", list, list.size());
        }
    }
    
    /*
      Below method is used get Item description of existing module
    */
    @Override
    public KwlReturnObject getfieldcomboItemDesc(HashMap<String, Object> comborequestParams) throws ServiceException {
        List list = new ArrayList();
        boolean success = false;
        String conditionQuery="";
        try {
            ArrayList params = new ArrayList();
            if (comborequestParams.containsKey("Fieldlabel")) {
                params.add(comborequestParams.get("Fieldlabel"));
                conditionQuery+=" fd.fieldlabel=? ";
            }
            if (comborequestParams.containsKey("Companyid")) {
                params.add(comborequestParams.get("Companyid"));
                conditionQuery+=" and fd.companyid= ?  ";
            }
            String query = "select fcd.value,fcd.itemdescription,fcd.activatedeactivatedimensionvalue from fieldcombodata fcd inner join fieldparams fd on fd.id=fcd.fieldid where "+ conditionQuery +" group by fcd.value ";
            list = executeSQLQuery(query,params.toArray());
            success = true;
        } catch (Exception e) {
            success = false;
            System.out.println("Error is " + e);
        } finally {
            return new KwlReturnObject(success, "", "-1", list, list.size());
        }
    }

    public KwlReturnObject insertdefaultheader(HashMap<String, Object> requestParams) throws ServiceException {
        List<DefaultHeader> list = new ArrayList<DefaultHeader>();
        boolean success = false;
        try {
            DefaultHeader dh = (DefaultHeader) setterMethod( requestParams, "com.krawler.common.admin.DefaultHeader", "Id");
            list.add(dh);
            success = true;
        } catch (Exception e) {
            success = false;
            System.out.println("Error is " + e);
        } finally {
            return new KwlReturnObject(success, "Field added successfully", "-1", list, list.size());
        }
    }

    public KwlReturnObject insertcolumnheader(HashMap<String, Object> requestParams) throws ServiceException {
        List<ColumnHeader> list = new ArrayList<ColumnHeader>();
        boolean success = false;
        try {
            ColumnHeader user = (ColumnHeader) setterMethod( requestParams, "com.krawler.common.admin.ColumnHeader", "Id");
            list.add(user);
            success = true;
        } catch (Exception e) {
            success = false;
            System.out.println("Error is " + e);
        } finally {
            return new KwlReturnObject(success, "Field added successfully", "-1", list, list.size());
        }
    }

    public KwlReturnObject getModules(HashMap<String, Object> requestParams) {
        KwlReturnObject result = null;
        boolean success = false;
        try {
            ArrayList name = null;
            String hql = "";
            ArrayList value = null;
            ArrayList orderby = null;
            ArrayList ordertype = null;
            String[] searchCol = null;
            hql = "from Modules ";
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
            result = executePagingQuery(requestParams, searchCol, hql, value);
            success = true;

        } catch (Exception ex) {
            result.isSuccessFlag();
            success = false;
//            result = false;
            ex.printStackTrace();

        } finally {
            return new KwlReturnObject(success, "", "-1", result.getEntityList(), result.getRecordTotalCount());
        }
    }

    public KwlReturnObject executePagingQuery(HashMap<String, Object> requestParams, String[] searchcol, String hql, ArrayList params) {
        boolean success = false;
        List lst = null;
        int count = 0;
        try {
            String allflag = "true";
            if (requestParams.containsKey("allflag")) {
                allflag = requestParams.get("allflag").toString();
            }
            int start = 0;
            int limit = 0;

            if (allflag.equals("false")) {
                start = Integer.parseInt(requestParams.get("start").toString());
                limit = Integer.parseInt(requestParams.get("limit").toString());
            }

            lst = executeQuery(hql, params.toArray());
            count = lst.size();
            if (allflag.equals("false")) {
                lst = executeQueryPaging( hql, params.toArray(), new Integer[]{start, limit});
            }
            success = true;
        } catch (Exception e) {
            success = false;
            e.printStackTrace();
//            logger.warn(e.getMessage(), e);
        } finally {
            return new KwlReturnObject(success, "", "-1", lst, count);
        }
    }
  /**
     * Description : Below Method is used to add Add default terms for company 
     * default company setup
     * @param <defaultCompSetupMap> used to get common setup parameters
     * @param <setUpData> used to get default set up data
        * Terms treated as Tax for Handle Multiple tax in INDIA
        * terms Type as follows
        * VAT  term type = 1
        * Excise term type = 2 
        * CST term type = 3
        * Service tax term type = 4
        * Swachh Bharat Cess  term type =5
        * Krishi Kalyan Cess term type = 6
        */
    @Override
    public void copyDefaultTerms(HashMap<String, Object> defaultCompSetupMap, JSONObject setUpData) throws ServiceException {

        /*
         * Variable declaration
         */
        String companyid = "",countryId="",stateId="";
        try {

            if (defaultCompSetupMap.containsKey(Constants.companyid) && defaultCompSetupMap.get(Constants.companyid) != null) {
                companyid = (String) defaultCompSetupMap.get(Constants.companyid);
            }
//            
            ArrayList params = new ArrayList();
            params.add(0);
            params.add(companyid);
            String Accountquery = "from Account where accounttype= ? and company.companyID=?";
            List SalesAccountlist = executeQuery(Accountquery, params.toArray());
            params.clear();
            params.add(1);  
            params.add(companyid);
            Accountquery = "from Account where accounttype= ? and company.companyID=?";
            List purchaseAccountlist = executeQuery(Accountquery, params.toArray());
            params.clear();
            String defaultquery = "from DefaultTerms ";
            String countryQry = "",stateQry="";
             if(defaultCompSetupMap.containsKey("country") && !StringUtil.isNullOrEmpty((String)defaultCompSetupMap.get("country"))){
                 countryId = defaultCompSetupMap.get("country").toString();
                 if(defaultCompSetupMap.containsKey("state") && !StringUtil.isNullOrEmpty((String)defaultCompSetupMap.get("state"))){
                     stateId = defaultCompSetupMap.get("state").toString();
                 }
             }
            if (!StringUtil.isNullOrEmpty(countryId)) {
                countryQry = " where country = '" + countryId + "'  ";
                /**
                 * Added US country terms,
                 * Existing function only for INDIA so handle check by INDIA Country id
                 */
                if (Constants.indian_country_id == Integer.parseInt(countryId)) {
                    if (!StringUtil.isNullOrEmpty(stateId)) {
                        stateQry = " AND (state = '" + stateId + "' OR state is null) ";
                    } else {// In Other Case, no any term is needed.
                        stateQry = " AND (state is null) ";
                    }
                }
            }
            List defaultlist = executeQuery(defaultquery + countryQry + stateQry );

            if (defaultlist != null && !defaultlist.isEmpty()) {
                Iterator itr = defaultlist.iterator();
                while (itr.hasNext()) {
                    Object DefautTermsObject = itr.next();
                    DefaultTerms Defterm = (DefaultTerms) DefautTermsObject;
                    LineLevelTerms invoiceTerm = new LineLevelTerms();
                    /**
                     * Check If LineLevelTerms is present or not if Present then
                     * update same LineLevelTerms Ticket - ERP-35391
                     */
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    /**
                     * ERP-41436
                     * Added SalesorPurchase column to check Sales/ Purchase side term present or not.
                     * Problem  : CESS term need to create for Sales and Purchase but if this flag not added(SalesorPurchase), 
                     * because of this Sales side CESS term created and then Same term override for Purchase side
                     */
                    filterRequestParams.put(Constants.filter_names, new ArrayList(Arrays.asList("company.companyID", "term","salesOrPurchase")));
                    filterRequestParams.put(Constants.filter_params, new ArrayList(Arrays.asList(companyid, Defterm.getTerm(),Defterm.isSalesOrPurchase())));
                    KwlReturnObject resultList = getLineLevelTerms(filterRequestParams);
                    if (resultList != null && resultList.getEntityList() != null && !resultList.getEntityList().isEmpty()) {
                        invoiceTerm = (LineLevelTerms) resultList.getEntityList().get(0);
                    }
                    invoiceTerm.setCompany((Company) get(Company.class, companyid));
                    invoiceTerm.setCreator((User) get(User.class, defaultCompSetupMap.get("userid").toString()));
                    invoiceTerm.setTerm(Defterm.getTerm());
                    invoiceTerm.setFormula(Defterm.getFormula());
                    invoiceTerm.setSign(Defterm.getSign());
                    invoiceTerm.setSalesOrPurchase(Defterm.isSalesOrPurchase());
                    invoiceTerm.setDeleted(0);
                    invoiceTerm.setPercentage(Defterm.getPercentage());
                    invoiceTerm.setTermType(Defterm.getTermtype());
                    invoiceTerm.setTermSequence(Defterm.getTermSequence());
                    invoiceTerm.setTaxType(Defterm.getTaxType());
                    invoiceTerm.setIsDefault(Defterm.isIsDefault());
                    invoiceTerm.setDefaultTerms(Defterm);
                    ArrayList accountParams = new ArrayList();
                    accountParams.add(companyid);
                    accountParams.add(Defterm.getAccountname());
                    String termMapeAccountQuery = "from Account where company.companyID=? and name = ?";
                    List accountForTermMap = executeQuery(termMapeAccountQuery, accountParams.toArray());
                    if (!accountForTermMap.isEmpty()) {
                        Object accObject = accountForTermMap.get(0);
                        Account acc = (Account) accObject;
                        acc.setControlAccounts(true);
                        String usedin = acc.getUsedIn();
                        acc.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Term_Account));
                        invoiceTerm.setAccount(acc);
                    } else {
                        if (Defterm.isSalesOrPurchase()) {
                            if (!SalesAccountlist.isEmpty()) {
                                Object accObject = SalesAccountlist.get(0);
                                Account acc = (Account) accObject;
                                acc.setControlAccounts(true);
                                String usedin = acc.getUsedIn();
                                acc.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Term_Account));
                                invoiceTerm.setAccount(acc);
                                SalesAccountlist.remove(0);
                            }
                        } else {
                            if (!purchaseAccountlist.isEmpty()) {
                                Object accObject = purchaseAccountlist.get(0);
                                Account acc = (Account) accObject;
                                acc.setControlAccounts(true);
                                String usedin = acc.getUsedIn();
                                acc.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Term_Account));
                                invoiceTerm.setAccount(acc);
                                purchaseAccountlist.remove(0);
                            }
                        }
                    }
                    
                    accountParams.clear();
                    accountParams.add(companyid);
                    accountParams.add(Defterm.getAdvancPayableAccountName());
                     if(!StringUtil.isNullOrEmpty(Defterm.getAdvancPayableAccountName())) {
                         accountForTermMap = executeQuery(termMapeAccountQuery, accountParams.toArray());
                         if (!accountForTermMap.isEmpty()) {
                             Object accObject = accountForTermMap.get(0);
                             Account acc = (Account) accObject;
                             acc.setControlAccounts(true);
                             String usedin = acc.getUsedIn();
                             acc.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Term_Account));
                             invoiceTerm.setPayableAccount(acc);
                         }
                     }
                    save(invoiceTerm);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        }
    }
    @Override
    public KwlReturnObject getInvoiceTerms(HashMap<String, String> hm) {
        List list = Collections.EMPTY_LIST;
        int listSize = 0;
        try {
            ArrayList params = new ArrayList();
            String companyId = hm.get("companyid") + "";
            boolean salesOrPurchaseFlag = false;
            boolean showActiveDeactiveTerms = false;
            boolean  isCopy=false;
            if (hm.containsKey("salesOrPurchaseFlag") && hm.get("salesOrPurchaseFlag")!=null && !StringUtil.isNullOrEmpty(hm.get("salesOrPurchaseFlag"))) {
                salesOrPurchaseFlag = Boolean.parseBoolean(hm.get("salesOrPurchaseFlag"));
            }
            String condition="where mt.company.companyID=? and mt.deleted=0 and mt.salesOrPurchase=? ";
            params.add(companyId);
            params.add(salesOrPurchaseFlag);
            
            if (hm.containsKey("term") && hm.get("term")!=null && !StringUtil.isNullOrEmpty(hm.get("term"))) {
                String term = hm.get("term").toString();
                condition +="and mt.term=? ";
                params.add(term);
            }
            if (hm.containsKey("isCopy") && hm.get("isCopy") != null && !StringUtil.isNullOrEmpty(hm.get("isCopy"))) {
                isCopy = Boolean.parseBoolean(hm.get("isCopy"));
            }
            if (hm.containsKey("showActiveDeactiveTerms") && hm.get("showActiveDeactiveTerms") != null && !StringUtil.isNullOrEmpty(hm.get("showActiveDeactiveTerms"))) {
                showActiveDeactiveTerms = Boolean.parseBoolean(hm.get("showActiveDeactiveTerms"));

            }
            if (!showActiveDeactiveTerms || isCopy) {
                condition += "and mt.isTermActive='T' ";
            }
            if (hm.containsKey(Constants.ss) && hm.get(Constants.ss) != null) {
                String ss = (String) hm.get(Constants.ss);
                if (!StringUtil.isNullOrEmpty(ss)) {
                    String[] searchcol = new String[]{"mt.term"};
                    Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1);
                    StringUtil.insertParamSearchString(SearchStringMap);
                    String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                    condition += searchQuery;
                }
            }
            
            //Not in query for group company
            if (hm.containsKey("notinquery") && hm.get("notinquery") != null) {
                String notinquery = (String) hm.get("notinquery");
                notinquery = AccountingManager.getFilterInString(notinquery);
                if (!StringUtil.isNullOrEmpty(notinquery)) {
                    condition += " and mt.term NOT IN " + notinquery;
                }
            }
            String hql = "from InvoiceTermsSales mt "+condition+"order by mt.termSequence ASC";
            if (!StringUtil.isNullOrEmpty(companyId)) {
                list = executeQuery( hql, params.toArray());
            }
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    @Override
    public KwlReturnObject getLinkedTermTax(HashMap<String, Object>  requestParams){
        List list = null;
        String query="";
        int listSize = 0;
        String condition = "";
        String termID = "";
        if (requestParams.containsKey("termid") && requestParams.get("termid") != null) {
             termID = (String) requestParams.get("termid");
        }
        /*
         * ERP-40242 : Show only activated taxes in create and copy case and all taxes in edit cases
         */

        if (requestParams.containsKey(Constants.includeDeactivatedTax) && !((Boolean) requestParams.get(Constants.includeDeactivatedTax))) {
            condition += " and t.activated = 1";
        }

        
        try {
            query = " SELECT t.id,t.name,tl.percent,t.activated FROM taxtermsmapping ttm "
                    +" INNER JOIN tax t on t.id=ttm.tax "
                    +" INNER JOIN taxlist tl on tl.tax=t.id "
                    + " WHERE ttm.invoicetermssales = ?" + condition;
            list = executeSQLQuery(query, new Object[]{termID});
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new KwlReturnObject(true, "", null, list, listSize);
    }
    @Override
    public KwlReturnObject getIndianTermsCompanyLevel(HashMap<String, String> hm) {
        List list = null;
        int listSize = 0;
        try {
            String companyId = hm.get("companyid") + "";
            String condition = "";
            boolean salesOrPurchaseFlag = false;
            if (!StringUtil.isNullOrEmpty(hm.get("salesOrPurchaseFlag"))) {
                salesOrPurchaseFlag = Boolean.parseBoolean(hm.get("salesOrPurchaseFlag"));
            }
            boolean isAdditionalTax = false;
            if (!StringUtil.isNullOrEmpty(hm.get("isAdditionalTax"))) {
                isAdditionalTax = Boolean.parseBoolean(hm.get("isAdditionalTax"));
            }
            if (hm.containsKey("termType") && !StringUtil.isNullOrEmpty(hm.get("termType"))) {
                condition = " and mt.termType = ?";
            }
            ArrayList params = new ArrayList();
            String hql = "from LineLevelTerms mt where mt.company.companyID=? and mt.deleted=0 and mt.salesOrPurchase=? "+ condition +" order by mt.termSequence";
            if (!StringUtil.isNullOrEmpty(companyId)) {
                params.add(companyId);
                params.add(salesOrPurchaseFlag);
               // params.add(isAdditionalTax);
                if(hm.containsKey("termType") && !StringUtil.isNullOrEmpty(hm.get("termType"))){
                    params.add(Integer.parseInt(hm.get("termType")));
                }
                list = executeQuery( hql, params.toArray());
            }
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    @Override
    public KwlReturnObject getInvoiceTermFormulaName(HashMap<String, String> hm) {
        List list = null;
        int listSize = 0;
        try {
            String companyId = hm.get("companyid") + "";

            ArrayList params = new ArrayList();
            String hql = "from InvoiceTermsSales mt where mt.company.companyID=?";
            if (!StringUtil.isNullOrEmpty(companyId)) {
                params.add(companyId);
                list = executeQuery( hql, params.toArray());
            }
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }

    public KwlReturnObject saveInvoiceTerm(HashMap<String, Object> termMap) throws ServiceException {
        List list = new ArrayList();
        try {
            InvoiceTermsSales invoiceTerm = new InvoiceTermsSales();
            if (termMap.containsKey("id")) {
                invoiceTerm = (InvoiceTermsSales) get(InvoiceTermsSales.class, termMap.get("id").toString());
            }

            if (termMap.containsKey("term")) {
                invoiceTerm.setTerm(termMap.get("term").toString());
            }
           
            if (termMap.containsKey("formula")) {
                invoiceTerm.setFormula(termMap.get("formula").toString());
            }
            
            if (termMap.containsKey("category")) {
                invoiceTerm.setCategory(termMap.get("category").toString());
            }

            if (termMap.containsKey("includegst")) {
                invoiceTerm.setIncludegst(Integer.parseInt(termMap.get("includegst").toString()));
            }

            if (termMap.containsKey("proft")) {
                invoiceTerm.setIncludeprofit(Integer.parseInt(termMap.get("proft").toString()));
            }

            if (termMap.containsKey("sign")) {
                invoiceTerm.setSign(Integer.parseInt(termMap.get("sign").toString()));
            }
            
            if (termMap.containsKey("suppressamount")) {
                invoiceTerm.setSupressamount(Integer.parseInt(termMap.get("suppressamount").toString()));
            }

            invoiceTerm.setDeleted(0);
            if (termMap.containsKey("deleted")) {
                invoiceTerm.setDeleted(Integer.parseInt(termMap.get("deleted").toString()));
            }
//
            if (termMap.containsKey("createdon")) {
                invoiceTerm.setCreatedOn(((Date) termMap.get("creationdate")).getTime());
            }
            if (termMap.containsKey("companyid")) {
                invoiceTerm.setCompany((Company) get(Company.class, termMap.get("companyid").toString()));
            }
            if (termMap.containsKey("accountid")) {
                invoiceTerm.setAccount((Account) get(Account.class, termMap.get("accountid").toString()));
            }
            if (termMap.containsKey("userId")) {
                invoiceTerm.setCreator((User) get(User.class, termMap.get("userId").toString()));
            }
            if (termMap.containsKey("salesOrPurchaseFlag")) {
                invoiceTerm.setSalesOrPurchase((Boolean) termMap.get("salesOrPurchaseFlag"));
            }
            
            if (termMap.containsKey("isTermActive")) {
               invoiceTerm.setIsTermActive((Boolean) termMap.get("isTermActive"));
            }

            if (termMap.containsKey("percent")) {
                invoiceTerm.setPercentage(Double.parseDouble(termMap.get("percent").toString()));
            }
            if (termMap.containsKey("termamount")) {
                invoiceTerm.setTermAmount(Double.parseDouble(termMap.get("termamount").toString()));
            }
            if (termMap.containsKey("termtype")) {
                invoiceTerm.setTermType(Integer.parseInt(termMap.get("termtype").toString()));
            }
            if (termMap.containsKey("termsequence")) {
                invoiceTerm.setTermSequence(Integer.parseInt(termMap.get("termsequence").toString()));
            }
            if (termMap.containsKey("formulaids")) {
                invoiceTerm.setFormulaids(termMap.get("formulaids").toString());
            }
            if (termMap.containsKey("purchasevalueorsalevalue")) {
                invoiceTerm.setPurchaseValueOrSaleValue(Double.parseDouble(termMap.get("purchasevalueorsalevalue").toString()));
            }
            if (termMap.containsKey("deductionorabatementpercent")) {
                invoiceTerm.setDeductionOrAbatementPercent(Double.parseDouble(termMap.get("deductionorabatementpercent").toString()));
            }
            if (termMap.containsKey("taxtype")) {
                invoiceTerm.setTaxType(Integer.parseInt(termMap.get("taxtype").toString()));
            }
            if (termMap.containsKey("isDefault")) {
                invoiceTerm.setIsDefault((Boolean) termMap.get("isDefault"));
            }
            if (termMap.containsKey("includeInTDSCalculation")) {
                invoiceTerm.setIncludeInTDSCalculation((Boolean) termMap.get("includeInTDSCalculation"));
            }

            saveOrUpdate(invoiceTerm);
            list.add(invoiceTerm);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    public KwlReturnObject findDimensionForEclaim(String companyid) throws ServiceException {
        String query = "select id from fieldparams where isforeclaim=1 and companyid = '"+ companyid + "'";

        List list = executeSQLQuery(query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
        
    public KwlReturnObject findTermUsedInFormula(String termid) throws ServiceException {
        String query = "select term from invoicetermssales where formula REGEXP '" + termid + ".*$' > 0 and deleted = 0";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTermUsedInTax(String termid) throws ServiceException {
        String query = "select id from taxtermsmapping where invoicetermssales = '" + termid + "'";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTermUsedInQuotation(String termid) throws ServiceException {
        String query = "select id from quotationtermmap where term = '" + termid + "'";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTermUsedInSO(String termid) throws ServiceException {
        String query = "select id from salesordertermmap where term = '" + termid + "'";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTermUsedInPI(String termid) throws ServiceException {
        String query = "select id from receipttermsmap where term = '" + termid + "'";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTermUsedInVQ(String termid) throws ServiceException {
        String query = "select id from vendorquotationtermmap where term = '" + termid + "'";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTermUsedInPO(String termid) throws ServiceException {
        String query = "select id from purchaseordertermmap where term = '" + termid + "'";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTermUsedInDO(String termid) throws ServiceException {
        String query = "select id from deliveryordertermmap where term = '" + termid + "'";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTermUsedInGRO(String termid) throws ServiceException {
        String query = "select id from goodsreceiptordertermmap where term = '" + termid + "'";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }

        public KwlReturnObject findTermUsedInTransaction(String termid) throws ServiceException {
        String query = "select id from invoicetermsmap where term = ? and invoice is not null";
//                + "UNION (select id from receipttermsmap where term = ? and goodsreceipt is not null) "
//                + "UNION (select id from deliveryordertermmap where term = ? and deliveryorder is not null) "
//                + "UNION (select id from goodsreceiptordertermmap where term = ? and goodsreceiptorder is not null) ";

        List list = executeSQLQuery( query, new Object[]{termid});

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTaxUsedInCSSI(String taxids) throws ServiceException {
        //String query = "SELECT distinct inv.id FROM invoice inv inner join invoicetermsmap invtm WHERE inv.tax in ("+taxids+") AND inv.id=invtm.invoice";
        //commented bcoz, if the tax is mapped previously with no terms then the condition becomes tax is unused and then if we map terms then the amounts will mismatch. So, instead checked tax is used or not
        String query = "SELECT distinct inv.id FROM invoice inv WHERE inv.tax in ("+taxids+")";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTaxUsedInCPPI(String taxids) throws ServiceException {
        //String query = "SELECT distinct cppi.id FROM goodsreceipt cppi inner join receipttermsmap cppitm WHERE cppi.tax in ("+taxids+") AND cppi.id=cppitm.goodsreceipt";
        //commented bcoz, if the tax is mapped previously with no terms then the condition becomes tax is unused and then if we map terms then the amounts will mismatch. So, instead checked tax is used or not
        String query = "SELECT distinct cppi.id FROM goodsreceipt cppi WHERE cppi.tax in ("+taxids+")";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTaxUsedInCQ(String taxids) throws ServiceException {
        //String query = "SELECT distinct qtn.id FROM quotation qtn inner join quotationtermmap qtntm WHERE qtn.tax in ("+taxids+") AND qtn.id=qtntm.quotation";
        //commented bcoz, if the tax is mapped previously with no terms then the condition becomes tax is unused and then if we map terms then the amounts will mismatch. So, instead checked tax is used or not
        String query = "SELECT distinct qtn.id FROM quotation qtn WHERE qtn.tax in ("+taxids+")";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTaxUsedInVQ(String taxids) throws ServiceException {
        //String query = "SELECT distinct vq.id FROM vendorquotation vq inner join vendorquotationtermmap vqtm WHERE vq.tax in ("+taxids+") AND vq.id=vqtm.vendorquotation";
        //commented bcoz, if the tax is mapped previously with no terms then the condition becomes tax is unused and then if we map terms then the amounts will mismatch. So, instead checked tax is used or not
        String query = "SELECT distinct vq.id FROM vendorquotation vq WHERE vq.tax in ("+taxids+")";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTaxUsedInSO(String taxids) throws ServiceException {
        //String query = "SELECT distinct so.id FROM salesorder so inner join salesordertermmap sotm WHERE so.tax in ("+taxids+") AND so.id=sotm.salesorder";
        //commented bcoz, if the tax is mapped previously with no terms then the condition becomes tax is unused and then if we map terms then the amounts will mismatch. So, instead checked tax is used or not
        String query = "SELECT distinct so.id FROM salesorder so WHERE so.tax in ("+taxids+")";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject findTaxUsedInPO(String taxids) throws ServiceException {
        //String query = "SELECT distinct po.id FROM purchaseorder po inner join purchaseordertermmap potm WHERE po.tax in ("+taxids+") AND po.id=potm.purchaseorder";
        //commented bcoz, if the tax is mapped previously with no terms then the condition becomes tax is unused and then if we map terms then the amounts will mismatch. So, instead checked tax is used or not
        String query = "SELECT distinct po.id FROM purchaseorder po WHERE po.tax in ("+taxids+")";

        List list = executeSQLQuery( query);

        int count = list.size();

        return new KwlReturnObject(true, "", null, list, count);
    }

    public KwlReturnObject addActiveDateRange(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        String id = "";
        ExtraCompanyPreferences extracompanypreferences = null;
        try {
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                id = requestParams.get("companyid").toString();
                if (get(ExtraCompanyPreferences.class, id) != null) {
                    extracompanypreferences = (ExtraCompanyPreferences) load(ExtraCompanyPreferences.class, id);
                } else {
                    extracompanypreferences = new ExtraCompanyPreferences();
                }
            }
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                extracompanypreferences.setId((String) requestParams.get("companyid"));
            }
            if (requestParams.containsKey("company")) {
                String companyid = (String) requestParams.get("company");
                if (!StringUtil.isNullOrEmpty(companyid)) {
                    extracompanypreferences.setCompany((Company) get(Company.class, companyid));
                } else {
                    extracompanypreferences.setCompany(null);
                }
            }
            if (requestParams.containsKey("fromdate")) {
                if (requestParams.get("fromdate") != null) {
                    extracompanypreferences.setActiveFromDate((Date) requestParams.get("fromdate"));
                } else {
                    extracompanypreferences.setActiveFromDate(null);
                }
            }
//            else{
//                extracompanypreferences.setActiveFromDate(null);
//            }
            if (requestParams.containsKey("todate")) {
                if (requestParams.get("todate") != null) {
                    extracompanypreferences.setActiveToDate((Date) requestParams.get("todate"));
                } else {
                    extracompanypreferences.setActiveToDate(null);
                }
            }
//            else{
//                extracompanypreferences.setActiveToDate(null);
//            }
            save(extracompanypreferences);
            list.add(extracompanypreferences);
        } catch (Exception e) {
            throw ServiceException.FAILURE("ProfileHandler.addActiveDateRange", e);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    //TO CHECK FOR QUotationisdecimal from companyaccountprefrences

    public KwlReturnObject quotationindecimalforcompany(String companyId) throws ServiceException {
        String query = "select quotationindecimalformat,quantitydigitafterdecimal,amountdigitafterdecimal,unitpricedigitafterdecimal from compaccpreferences where id=?";
         ArrayList params = new ArrayList();
        params.add(companyId);
        List list = executeSQLQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }

    @Override
    public KwlReturnObject updateAccountCreationDate(Date fyfrom, String companyid) throws ServiceException {
        ArrayList params = new ArrayList();
        params.add(fyfrom);
        params.add(companyid);
        String query = " update account acc set acc.creationdate=? where acc.company=? ";
        int count = executeSQLUpdate( query, params.toArray());
        return new KwlReturnObject(true, "Creation Date has been updated in accounts", null, null, count);
    }
    
    public boolean isDuplicateSalesTerm(HashMap<String, String> termMap) throws ServiceException{
        List list = null;
        boolean isDuplicateTerm = false;
        int count = 0;
        try{
            String companyId = termMap.get("companyid") + "";
            String term = termMap.get("term");
            boolean salesOrPurchaseFlag = false;
            if (!StringUtil.isNullOrEmpty(termMap.get("salesOrPurchaseFlag"))) {
                salesOrPurchaseFlag = Boolean.parseBoolean(termMap.get("salesOrPurchaseFlag"));
            }           
            ArrayList params = new ArrayList();
            String query = "select id from invoicetermssales where term=? and company=? and deleted=0 and salesorpurchase=?";
            if (!StringUtil.isNullOrEmpty(companyId)) {
                params.add(term);
                params.add(companyId);
                params.add(salesOrPurchaseFlag);
                list = executeSQLQuery( query, params.toArray());
            }  
            
            if(list!=null)
            count = list.size();
            
            if(count>0)
                isDuplicateTerm = true;            
        }catch(ServiceException ex){
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        }      
        return isDuplicateTerm;
    }
    
    public KwlReturnObject getAccountChilds(Account account) throws ServiceException {
        String query = " select acc from Account acc where parent = ? ";
        List list = executeQuery( query, account);;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }

    @Override
    public void saveOrUpdateAll(List<Object> objectList) throws ServiceException {
        saveAll(objectList);
    }
    
    @Override
    public KwlReturnObject getTaxesFromAccountId(HashMap<String, Object> taxFromAccountParams) throws ServiceException {
        
        String condition = "";
        /*
         * ERP-40242 : Show only activated taxes in create and copy case and all taxes in edit cases
         */
        if (taxFromAccountParams.containsKey(Constants.includeDeactivatedTax) && !((Boolean) taxFromAccountParams.get(Constants.includeDeactivatedTax))) {
            condition += " and activated = 1";
        }

        String query = "select * from tax where account = ? and company = ? and deleteflag = false" + condition;
        ArrayList params = new ArrayList();
        params.add((String) taxFromAccountParams.get("accountid"));
        params.add((String) taxFromAccountParams.get("companyid"));
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
    public KwlReturnObject getTaxesAndTermsUsingAccountId(String accountID, String companyId) throws ServiceException {
        String sqlQuery = "select * from ("
                + "select id,'tax'as type from tax where account = ? and company = ? and deleteflag = false "
                + " UNION "
                + "select id,'term' as type from linelevelterms where (account = ? or payableaccount = ?) and company = ? and deleted = 0) as t1 ";
        ArrayList params = new ArrayList();
        params.add(accountID);
        params.add(companyId);
        params.add(accountID);
        params.add(accountID);
        params.add(companyId);
        List list = executeSQLQuery(sqlQuery, params.toArray());
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
    public List sortOnParent(List lst){
        List ll = new ArrayList();
        List list1 = new ArrayList();//For dimension fields having parents
        List list2 = new ArrayList();
        List temp = null;
        if(lst!=null){
            temp = lst;
            for(Object obj : temp) {
                FieldParams tmpcontyp = (FieldParams) obj;
                if(tmpcontyp.getParent()!=null){
                    list1.add(tmpcontyp);
                }else{
                    list2.add(tmpcontyp);
                }
            }
            
            for(Object obj : list2) {
                FieldParams tmpcontyp = (FieldParams) obj;
                ll.add(tmpcontyp);
                ll = getChildField(ll, list1);
            }
            
        }
        return ll;
        
//TODO: Need to optimize above code if possible. Following is the commented code for the same.  
        
//    public List sortOnParent(List mainlist){
//        List finallist = new ArrayList();
//        List templist1 = null;
//        
//        //Add all parent params in final list
//        for(Object obj : mainlist) {
//            FieldParams tmpcontyp = (FieldParams) obj;
//            if(tmpcontyp.getParent()==null){
//                finallist.add(tmpcontyp);
//            }
//        }
//        
//        //Loop on mainlist to add child params
//        for(Object obj : mainlist) {
//            FieldParams tmpcontyp = (FieldParams) obj;
//            if(finallist.contains(tmpcontyp)){
//                continue;
//            }else{
//                
//                
//                //Temp array to store final list
//                templist1 = new ArrayList();
//                templist1.addAll(finallist);
//                
//                Iterator<Object> ite = templist1.iterator();
//                int index = -1;
////                for(Object object : templist1) {
//                while(ite.hasNext()) {
//                    FieldParams fp = (FieldParams) ite.next();
////                    FieldParams fp = (FieldParams) object;
//                    if(tmpcontyp.getParent()!=null && tmpcontyp.getParent().getId().equals(fp.getId())){
//                        index = templist1.indexOf(fp);
//                        break;
//                    }
//                }
//                
//                List templist = new ArrayList();
//                List list = new ArrayList();
//                
//                if(index!=-1){
//                    list.addAll(finallist.subList(0, index + 1)) ;
//                    list.add(tmpcontyp);
//                    list.addAll(list.size(), finallist.subList(index + 1, finallist.size()));
//                    
//                }
//                
//                if(list!=null && !list.isEmpty()){
//                    finallist = new ArrayList();
//                    finallist.addAll(list);
//                }
//            }
//        }
//        return finallist;
    }
    
    public List getChildField(List ll, List list1){
        for(Object obj1 : list1) {
            FieldParams fp1 = (FieldParams) obj1;
            for(Object obj : ll) {
                FieldParams fp = (FieldParams) obj;
                if(fp1.getParent()!=null && fp1.getParent().getId().equals(fp.getId())){
                    ll.add(fp1);
                    list1.remove(fp1);
                    return getChildField(ll, list1);
                }
            }
        }
        return ll;
    } 
    public KwlReturnObject getSplitAccountAmount(String fieldComboDataId, String accid) throws ServiceException {
        List list = null;
        int listSize = 0;
        try {
            ArrayList params = new ArrayList();
            String hql = "from DistributeBalance db where db.accountid.ID=? and db.comboid.id=?";
            params.add(accid);
            params.add(fieldComboDataId);
            list = executeQuery( hql, params.toArray());
            if (list != null) {
                listSize = list.size();
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, listSize);
        }
    }
    public KwlReturnObject fieldForOpeningBalance(String fieldLabel, int moduleid,String companyid) throws ServiceException {
        List list = null;
        boolean isPresent = false;
        try {
            ArrayList params = new ArrayList();
            String hql = "from FieldParams fp where fp.fieldlabel=? and fp.moduleid=? and fp.company.companyID=?";
            params.add(fieldLabel);
            params.add(moduleid);
            params.add(companyid);
            list = executeQuery( hql, params.toArray());
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        } finally {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }
    public String getComboIdForAccount(String value,String fieldid) throws ServiceException {
        List list = null;
        String comboid="";
        try {
            ArrayList params = new ArrayList();
            String hql = "select id from fieldcombodata where value=? and fieldid=?";
            params.add(value);
            params.add(fieldid);
            list = executeSQLQuery( hql, params.toArray());
            if (list != null && !list.isEmpty()) {
                comboid = list.get(0).toString();
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        } finally {
            return comboid;
        }
    }
    @Override
    public KwlReturnObject distributeOpeningBalance(HashMap<String, Object> requestParams) throws ServiceException {
        List list = new ArrayList();
        try{
            String companyid = requestParams.get("companyid").toString();
            boolean debitType = requestParams.containsKey("debitType") ? Boolean.parseBoolean(requestParams.get("debitType").toString()) : false;
            Account account = (Account) requestParams.get("account");
            if(requestParams.containsKey("distributedopeningbalancearray")){
                JSONArray distributedopeningbalancearray = (JSONArray) requestParams.get("distributedopeningbalancearray");
                if (distributedopeningbalancearray != null) {
                    for (int i = 0; i < distributedopeningbalancearray.length(); i++) {
                        JSONObject jobj = distributedopeningbalancearray.getJSONObject(i);
                        if (jobj.has("comboid") && !StringUtil.isNullOrEmpty(jobj.getString("comboid"))) {
                            String delQuery = " delete from distributebalance where accountid = ? and comboid = ? ";
                            int numRows = executeSQLUpdate( delQuery, new String[]{account.getID(), jobj.getString("comboid")});
                        }
                    }

                    List<FieldParams> idlist = new ArrayList<FieldParams>();
                    for (int i = 0; i < distributedopeningbalancearray.length(); i++) {
                        JSONObject jobj = distributedopeningbalancearray.getJSONObject(i);
                        if ((jobj.has("comboid") && !StringUtil.isNullOrEmpty(jobj.getString("comboid"))) 
                                && (jobj.has("distributedopeningbalanace") && !StringUtil.isNullOrEmpty(jobj.getString("distributedopeningbalanace")) && Double.parseDouble(jobj.getString("distributedopeningbalanace"))!=0)) {
                            DistributeBalance distributeBalance = new DistributeBalance();
                            FieldComboData fieldComboData = (FieldComboData) get(FieldComboData.class, jobj.getString("comboid"));
                            if(fieldComboData!=null){
                                distributeBalance.setComboid(fieldComboData);
                            }
                            if(fieldComboData.getField()!=null){
                                FieldParams fieldParams = (FieldParams) get(FieldParams.class, fieldComboData.getField().getId());
                                if(fieldParams!=null){
                                    distributeBalance.setField(fieldParams);
                                    idlist.add(fieldParams);
                                }
                            }
                            if(account!=null){
                                distributeBalance.setAccountid(account);
                            }
                            if(jobj.has("distributedopeningbalanace") && !StringUtil.isNullOrEmpty(""+jobj.getDouble("distributedopeningbalanace"))){
                                debitType = jobj.optBoolean("debitType");
                                double distributedopeningbalanace = debitType ? jobj.getDouble("distributedopeningbalanace") : -jobj.getDouble("distributedopeningbalanace");
                                distributeBalance.setOpeningbal(distributedopeningbalanace);
                            }                        
                            save(distributeBalance);
                            list.add(distributeBalance);
                        }
                    }
                    if(idlist.size()>0){
                        idlist = new ArrayList<FieldParams>(new LinkedHashSet<FieldParams>(idlist));
                        for(FieldParams fieldParams : idlist) {
                            if(!(fieldParams.isFieldOfGivenGSTConfigType(Constants.isformultientity) || fieldParams.getIsessential()==1)){
                                int colnum = fieldParams.getColnum();
                                AccountCustomData accountCustomData = (AccountCustomData) get(AccountCustomData.class, account.getID());
                                if(accountCustomData!=null){
                                    accountCustomData.setCol(colnum, null);
                                    saveOrUpdate(accountCustomData);
                                }
                            }
                        }
                    }
                }
            }
    
            if(requestParams.containsKey("distributeddeletefieldarray")){
                JSONArray distributeddeletefieldarray = (JSONArray) requestParams.get("distributeddeletefieldarray");
                if (distributeddeletefieldarray != null) {
                    for (int i = 0; i < distributeddeletefieldarray.length(); i++) {
                        JSONObject jobj = distributeddeletefieldarray.getJSONObject(i);
                        if (jobj.has("fieldid") && !StringUtil.isNullOrEmpty(jobj.getString("fieldid"))) {
                            String delQuery = " delete from distributebalance where accountid = ? and field = ? ";
                            int numRows = executeSQLUpdate( delQuery, new String[]{account.getID(), jobj.getString("fieldid")});
                        }
                    }
                }
            }
        }catch(Exception ex){
            throw ServiceException.FAILURE("accAccountDAOImpl.distributeOpeningBalance", ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
    public KwlReturnObject getDistributedOpeningBalance(HashMap<String, Object> requestParams) {
        List list = null;
        try {
            String accountid = "";
            String fieldid = "";
            String comboid = "";
            if(requestParams.containsKey("accountid")){
                accountid = requestParams.get("accountid").toString();
                String hql = " select db.comboid.id, db.openingbal, db.field.id "; 
                if(requestParams.containsKey("fieldid")){
                    fieldid = requestParams.get("fieldid").toString();
                    hql += " from DistributeBalance db where db.accountid.ID = ? and db.field.id = ? "; 
                    list = executeQuery( hql, new Object[]{accountid, fieldid});
                }else if(requestParams.containsKey("accountid") && requestParams.containsKey("comboid")){
                    comboid = requestParams.get("comboid").toString();
                    hql += " from DistributeBalance db where db.accountid.ID = ? and db.comboid.id = ? "; 
                    list = executeQuery( hql, new Object[]{accountid, comboid});
                }else{
                    hql += " , db.id from DistributeBalance db where db.accountid.ID = ? "; 
                    list = executeQuery( hql, new Object[]{accountid});
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return new KwlReturnObject(true, "", null, list, list.size());
        }
    }
     public KwlReturnObject getCustomLayoutfromAccount(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from GroupAccMap t where account.ID=? and t.company.companyID=?";
        list = executeQuery( q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    public void updateSplitOpeningAmount(String comboId, double amount, String accountId) throws ServiceException {
        String query = "update distributebalance set openingbal=openingbal + " + amount + " where comboid=? and accountid=?";
        ArrayList arrayList = new ArrayList();
        arrayList.add(comboId);
        arrayList.add(accountId);
        int numRows = executeSQLUpdate( query, arrayList.toArray());
    }

    public void updateAccountOpeningAmount(String accountId, double amount) throws ServiceException {
        String query = "update account set openingbalance=openingbalance +" + amount + " where id=?";
        ArrayList arrayList = new ArrayList();
        arrayList.add(accountId);
        int numRows = executeSQLUpdate( query, arrayList.toArray());
    }
    
    public double getSplitBalanceForComboId(String comboId, String fieldId, String accountId) throws ServiceException {
        double balance = 0;
        String query = "select openingbal from distributebalance where comboid=? and accountid=?";
        ArrayList arrayList = new ArrayList();
        arrayList.add(comboId);
        arrayList.add(accountId);
        List list = executeSQLQuery( query, arrayList.toArray());
        if (list.size() > 0 && list.get(0) != null) {
            balance = Double.parseDouble(list.get(0).toString());
        }
        return balance;
    }
    
    public void mapDefaultHiddenFields(String companyid) throws ServiceException {
//        String customizereportheaderPIID = "7e6fae80-c00e-11e5-8614-c03fd5ab06a3";        //PI
//        String customizereportheaderPOID = "3669dbc2-c010-11e5-8614-c03fd5ab06a3";        //PO
        String headerquery = "SELECT id, moduleid FROM customizereportheader WHERE dataIndex='permit' AND reportid=1 AND isformfield=1 AND islinefield=1 ORDER BY moduleid ASC";
        ArrayList arrayList = new ArrayList();
        List headerlist = executeSQLQuery( headerquery);
        String mapQuery = "INSERT INTO customizereportmapping (id,reportid,hidden,dataheader,dataIndex,isformfield,isreadonlyfield,islinefield, isusermanadatoryfield, customizereportheader, moduleid, company) VALUES(UUID(), 1, 1, 'Permit No.', 'permit', 1, 0, 1, 0, ?, ?, ?)";
        try {
            Iterator<Object> it = headerlist.iterator();
            while (it.hasNext()) {
                Object[] temp = (Object[]) it.next();
                int moduleid = Integer.valueOf(temp[1].toString());
                int a = executeSQLUpdate( mapQuery, new Object[]{(String) temp[0], moduleid, companyid});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Error while mapping customizereportheader id in customizereportmapping", ex);
        }
    }
    
    @Override
    public KwlReturnObject saveOrupdateCIMBBankDetail(HashMap<String, Object> ibgBankDetailParams) throws ServiceException {
         List list = new ArrayList();
        try {
            CIMBBankDetails bankDetail = null;
            String detailID = (String) ibgBankDetailParams.get(Constants.CIMB_BANK_DETAIL_ID);
            if (!StringUtil.isNullOrEmpty(detailID)) {
                bankDetail = (CIMBBankDetails) get(CIMBBankDetails.class, detailID);
            } else {
                bankDetail = new CIMBBankDetails();
            }
            
            if (ibgBankDetailParams.containsKey(Constants.SERVICE_CODE)) {
                bankDetail.setServiceCode((String) ibgBankDetailParams.get(Constants.SERVICE_CODE));
            }

            if (ibgBankDetailParams.containsKey(Constants.ORDERER_NAME)) {
                bankDetail.setOrdererName((String) ibgBankDetailParams.get(Constants.ORDERER_NAME));
            }

            bankDetail.setCurrencyCode(Constants.SGD_CURRENCY_CODE);

            if (ibgBankDetailParams.containsKey(Constants.SETTELEMENT_MODE)) {
                bankDetail.setSettelementMode(Integer.parseInt(ibgBankDetailParams.get(Constants.SETTELEMENT_MODE).toString()));
            }

            if (ibgBankDetailParams.containsKey(Constants.POSTING_INDICATOR)) {
                bankDetail.setPostingIndicator(Integer.parseInt(ibgBankDetailParams.get(Constants.POSTING_INDICATOR).toString()));
            }
            
            if (ibgBankDetailParams.containsKey(Constants.BANK_Account_Number)) {
                bankDetail.setBankAccountNumber((String) ibgBankDetailParams.get(Constants.BANK_Account_Number));
            }
            
            if (ibgBankDetailParams.containsKey(Constants.Acc_Accountid)) {
                Account account = (Account) get(Account.class, (String) ibgBankDetailParams.get(Constants.Acc_Accountid));
                bankDetail.setAccount(account);
            }
            
            if (ibgBankDetailParams.containsKey(Constants.companyid)) {
                Company company = (Company) get(Company.class, (String) ibgBankDetailParams.get(Constants.companyid));
                bankDetail.setCompany(company);
            }
            
            saveOrUpdate(bankDetail);

            list.add(bankDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCIMBBankDetails(HashMap<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        String accCompanyId = (String) dataMap.get("companyId");
        List params = new ArrayList();
        params.add(accCompanyId);

        String condition = "";

        if (dataMap.containsKey("bankAccountId") && dataMap.get("bankAccountId") != null) {
            condition += " and ibd.account.ID=? ";
            params.add((String) dataMap.get("bankAccountId"));
        }

        String query = "From CIMBBankDetails ibd WHERE ibd.company.companyID=? " + condition;
        list = executeQuery( query, params.toArray());

        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /* Method used to copy layout groups
     * @param templatepnl 
     * @param params Parameters to be used for data manipulation
     * @return Map for newly created layout and copied layout
     */
    public KwlReturnObject copyLayoutGroups(Templatepnl templatepnl, Map<String, Object> params) {
        List list = new ArrayList();
        Map<LayoutGroup,LayoutGroup> hm = new HashMap();
        List<LayoutGroup> grouptotals=new ArrayList<>();
        try {
            Map<String, Object> reqMap = new HashMap<>();
            params.put("levelZeroFlag",true); // will not include child accounts
            KwlReturnObject result = getCustomLayoutGroups(params);
            List layoutlist = result.getEntityList();
            Iterator itr = layoutlist.iterator();
            while (itr.hasNext()) {
                LayoutGroup copygroup = (LayoutGroup) itr.next();
                LayoutGroup group = new LayoutGroup();
                group.setTemplate(templatepnl);
                group.setSequence(copygroup.getSequence());
                group.setCompany(copygroup.getCompany());
                group.setName(copygroup.getName());
                group.setShowtotal(copygroup.getShowtotal());
                group.setNature(copygroup.getNature());
                group.setShowchild(copygroup.getShowchild());
                group.setShowchildacc(copygroup.getShowchildacc());
                group.setExcludeChildAccountBalances(copygroup.isExcludeChildAccountBalances());
                save(group);
                hm.put(group, copygroup);
                if (group.getNature() == 5) { //define total
                    grouptotals.add(group);
                } else { // map respective accounts to the groups
                    reqMap.clear();
                    reqMap.put("groupid", copygroup.getID());
                    reqMap.put("companyid", copygroup.getCompany().getCompanyID());
                    KwlReturnObject accountlist = getAccountsForLayoutGroup(reqMap);
                    List<GroupAccMap> groupaccmaplist = accountlist.getEntityList();
                    for (GroupAccMap groupAccMap : groupaccmaplist) {
                        Account account = groupAccMap.getAccount();
                        Company company = groupAccMap.getCompany();
                        GroupAccMap accMap=new GroupAccMap();
                        accMap.setLayoutgroup(group);
                        accMap.setAccount(account);
                        accMap.setCompany(company);
                        save(accMap);
                    }
                }
                hm.putAll(saveChildLayoutGroups(group,copygroup,grouptotals)); 
            }
            list.add(hm);
            list.add(grouptotals);
            Map<LayoutGroup,LayoutGroup> reverseMap=new HashMap<>();
            for (Map.Entry<LayoutGroup, LayoutGroup> entry : hm.entrySet()) {
                LayoutGroup group = entry.getKey();
                LayoutGroup copygroup = entry.getValue();
                reverseMap.put(copygroup, group);
            }
            if(grouptotals!=null && !grouptotals.isEmpty()){
                for (LayoutGroup group : grouptotals) {
                    LayoutGroup copygroup = hm.containsKey(group) ? hm.get(group) : null;
                    if (copygroup != null) {
                        KwlReturnObject totallist = getCustomsGroupsForTotal(copygroup.getID());
                        List<Groupmapfortotal> totalMap = totallist.getEntityList();
                        for (Groupmapfortotal groupmapfortotal : totalMap) {
                            if (reverseMap.containsKey(groupmapfortotal.getGroupid())) {
                                Groupmapfortotal g = new Groupmapfortotal();
                                g.setGroupidtotal(group);
                                g.setAction(groupmapfortotal.getAction());
                                g.setGroupid((LayoutGroup) reverseMap.get(groupmapfortotal.getGroupid()));
                                save(g);
                            } else {
                                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.INFO, groupmapfortotal.getGroupid().getName());
                            }
                        }
                    } else {
                        Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.INFO, group.getName());
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    public KwlReturnObject copyDefaultLayoutGroups(DefaultTemplatePnL templatepnl, Map<String, Object> params) {
        List list = new ArrayList();
        Map<DefaultLayoutGroup, DefaultLayoutGroup> hm = new HashMap();
        List<DefaultLayoutGroup> grouptotals=new ArrayList<>();
        try {
            Map<String, Object> reqMap = new HashMap<>();
            KwlReturnObject result = getCustomLayoutGroups(params);
            List layoutlist = result.getEntityList();
            Iterator itr = layoutlist.iterator();
            
            while (itr.hasNext()) {
                DefaultLayoutGroup copygroup = (DefaultLayoutGroup) itr.next();
                DefaultLayoutGroup group = new DefaultLayoutGroup();
                group.setTemplate(templatepnl);
                group.setSequence(copygroup.getSequence());
                group.setName(copygroup.getName());
                group.setShowtotal(copygroup.getShowtotal());
                group.setNature(copygroup.getNature());
                group.setShowchild(copygroup.getShowchild());
                group.setShowchildacc(copygroup.getShowchildacc());
                group.setExcludeChildAccountBalances(copygroup.isExcludeChildAccountBalances());
                
                save(group);
                hm.put(group, copygroup);
                
                if (group.getNature() == 5) { //define total
                    grouptotals.add(group);
                } else { // map respective accounts to the groups
                    reqMap.clear();
                    reqMap.put("groupid", copygroup.getID());
                    KwlReturnObject accountlist = getAccountsForDefaultLayoutGroup(reqMap);
                    List<DefaultGroupAccMap> groupaccmaplist = accountlist.getEntityList();
                    for (DefaultGroupAccMap groupAccMap : groupaccmaplist) {
                        String accountname = groupAccMap.getAccountname();
                        String groupname = groupAccMap.getGroupname();
                        
                        DefaultGroupAccMap accMap=new DefaultGroupAccMap();
                        accMap.setDefaultlayoutgroup(group);
                        accMap.setAccountname(accountname);
                        accMap.setGroupname(groupname);
                        save(accMap);
                    }
                }
                hm.putAll(saveDefaultChildLayoutGroups(group,copygroup,grouptotals)); 
            }
            list.add(hm);
            list.add(grouptotals);
            Map<DefaultLayoutGroup,DefaultLayoutGroup> reverseMap=new HashMap<>();
            for (Map.Entry<DefaultLayoutGroup, DefaultLayoutGroup> entry : hm.entrySet()) {
                DefaultLayoutGroup group = entry.getKey();
                DefaultLayoutGroup copygroup = entry.getValue();
                reverseMap.put(copygroup, group);
            }
            if(grouptotals!=null && !grouptotals.isEmpty()){
                for (DefaultLayoutGroup group : grouptotals) {
                    DefaultLayoutGroup copygroup = hm.containsKey(group) ? hm.get(group) : null;
                    if (copygroup != null) {
                        KwlReturnObject totallist = getCustomsGroupsForTotal(copygroup.getID());
                        List<DefaultGroupMapForTotal> totalMap = totallist.getEntityList();
                        for (DefaultGroupMapForTotal groupmapfortotal : totalMap) {
                            if (reverseMap.containsKey(groupmapfortotal.getGroupid())) {
                                DefaultGroupMapForTotal g = new DefaultGroupMapForTotal();
                                g.setGroupidtotal(group);
                                g.setAction(groupmapfortotal.getAction());
                                g.setGroupid((DefaultLayoutGroup) reverseMap.get(groupmapfortotal.getGroupid()));
                                save(g);
                            } else {
                                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.INFO, groupmapfortotal.getGroupid().getName());
                            }
                        }
                    } else {
                        Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.INFO, group.getName());
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    /** Method is used to store the child layout group
     * @param parenttemplate Parent Layout
     * @param copytemplate Layout which needs to be copied 
     * @param grouptotals List of layout which are defined as total
     * @return Map for newly created layout and copied layout
     */
    private Map<LayoutGroup,LayoutGroup> saveChildLayoutGroups(LayoutGroup parenttemplate,LayoutGroup copytemplate,List<LayoutGroup> grouptotals) {
        Map<LayoutGroup,LayoutGroup> map = new HashMap();
        try {
            Map<String, Object> reqMap = new HashMap<>();
            HashMap<String, Object> groupMap = new HashMap<>();
            Set<LayoutGroup> child = copytemplate.getChildren();
            for (LayoutGroup copygroup : child) {
                LayoutGroup group = new LayoutGroup();
                group.setTemplate(parenttemplate.getTemplate());
                group.setSequence(copygroup.getSequence());
                group.setCompany(copygroup.getCompany());
                group.setName(copygroup.getName());
                group.setShowtotal(copygroup.getShowtotal());
                group.setNature(copygroup.getNature());
                group.setShowchild(copygroup.getShowchild());
                group.setShowchildacc(copygroup.getShowchildacc());
                group.setExcludeChildAccountBalances(copygroup.isExcludeChildAccountBalances());
                group.setParent(parenttemplate);
                save(group);
                map.put(group,copygroup);
                if (group.getNature() == 5) { //define total
                    grouptotals.add(group);
                } else {
                    reqMap.clear();
                    reqMap.put("groupid", copygroup.getID());
                    reqMap.put("companyid", copygroup.getCompany().getCompanyID());
                    KwlReturnObject accountlist = getAccountsForLayoutGroup(reqMap);
                    List<GroupAccMap> groupaccmaplist = accountlist.getEntityList();
                    for (GroupAccMap groupAccMap : groupaccmaplist) {
                        Account account = groupAccMap.getAccount();
                        Company company = groupAccMap.getCompany();
                        groupMap.clear();
//                        groupMap.put("groupid", group.getID());
//                        groupMap.put("accountid", account.getID());
//                        groupMap.put("companyid", company.getCompanyID());
//                        saveLayoutGroupAccountMap(groupMap);GroupAccMap accMap=new GroupAccMap();
                        GroupAccMap accMap = new GroupAccMap();
                        accMap.setLayoutgroup(group);
                        accMap.setAccount(account);
                        accMap.setCompany(company);
                        save(accMap);
                        
                    }
                }
                map.putAll(saveChildLayoutGroups(group,copygroup,grouptotals));
            }
        } catch (Exception ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        } finally {
            return map;
        }
    }
    
    private Map<DefaultLayoutGroup,DefaultLayoutGroup> saveDefaultChildLayoutGroups(DefaultLayoutGroup parenttemplate,DefaultLayoutGroup copytemplate,List<DefaultLayoutGroup> grouptotals) {
        Map<DefaultLayoutGroup,DefaultLayoutGroup> map = new HashMap();
        try {
            Map<String, Object> reqMap = new HashMap<>();
            HashMap<String, Object> groupMap = new HashMap<>();
            Set<DefaultLayoutGroup> child = copytemplate.getChildren();
            for (DefaultLayoutGroup copygroup : child) {
                DefaultLayoutGroup group = new DefaultLayoutGroup();
                group.setTemplate(parenttemplate.getTemplate());
                group.setSequence(copygroup.getSequence());
                group.setName(copygroup.getName());
                group.setShowtotal(copygroup.getShowtotal());
                group.setNature(copygroup.getNature());
                group.setShowchild(copygroup.getShowchild());
                group.setShowchildacc(copygroup.getShowchildacc());
                group.setExcludeChildAccountBalances(copygroup.isExcludeChildAccountBalances());
                group.setParent(parenttemplate);
                save(group);
                map.put(group,copygroup);
                if (group.getNature() == 5) { //define total
                    grouptotals.add(group);
                } else {
                    reqMap.clear();
                    reqMap.put("groupid", copygroup.getID());
                    KwlReturnObject accountlist = getAccountsForDefaultLayoutGroup(reqMap);
                    List<DefaultGroupAccMap> groupaccmaplist = accountlist.getEntityList();
                    for (DefaultGroupAccMap groupAccMap : groupaccmaplist) {
                        String accountname = groupAccMap.getAccountname();
                        String groupname = groupAccMap.getGroupname();
                        
                        DefaultGroupAccMap accMap = new DefaultGroupAccMap();
                        accMap.setDefaultlayoutgroup(group);
                        accMap.setAccountname(accountname);
                        accMap.setGroupname(groupname);
                        save(accMap);
                        
                    }
                }
                map.putAll(saveDefaultChildLayoutGroups(group,copygroup,grouptotals));
            }
        } catch (Exception ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        } finally {
            return map;
        }
    }
    
    @Override
    public KwlReturnObject getAllAccountsFromName(String companyId, String accname) throws ServiceException {
        ArrayList params = new ArrayList();
        String query = " select acc from Account acc where acc.company.companyID =  ? and acc.name =  ? ";
        params.add(companyId);
        params.add(accname);
        List list = executeQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    @Override
    public KwlReturnObject getAccountsForPM(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        List list = null;
        String condition = "";
        try {
            String companyID = requestParams.get("companyid").toString();
            params.add(companyID);
            
             if(requestParams.containsKey("nature") && !StringUtil.isNullObject(requestParams.get("nature"))){
                String natureList = requestParams.get("nature").toString();
                if(!StringUtil.isNullOrEmpty(natureList)){
                    String[] nature = (String[]) natureList.split(",");

                    if (nature != null) {
                        String qMarks = "?";
                        params.add(5);//not a nature
                        for (int i = 0; i < nature.length; i++) {
                            qMarks += ",?";
                            params.add(Integer.parseInt(nature[i]));
                        }
                        condition += " and  accgroup.nature in (" + qMarks + ") ";
                    }
                }
            }
             
            if (requestParams.containsKey("mastertype")&& !StringUtil.isNullObject(requestParams.get("mastertype")) && !StringUtil.isNullOrEmpty(requestParams.get("mastertype").toString())) {
//                params.add(Integer.parseInt(requestParams.get("mastertype").toString()));
                condition += " and  account.mastertypeid in(" + requestParams.get("mastertype").toString() + ") ";
            }
            
            if (requestParams.containsKey("accountid") && !StringUtil.isNullOrEmpty(requestParams.get("accountid").toString())) {
                condition += " and  account.id=? ";
                params.add(requestParams.get("accountid").toString());
            }
             
            String query = "SELECT account.id,account.name,account.acccode,account.mastertypeid from account INNER JOIN accgroup ON account.groupname=accgroup.id WHERE account.company=? and account.deleteflag='F' and account.isheaderaccount = 'F' " + condition + " ORDER BY account.name";
            list = executeSQLQuery(query, params.toArray());
        } catch (NumberFormatException | ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getDefaultCustomTemplate(String countryID, String id) {
        List list = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            String condition = "";
            
            String templateQuery = "select id,name,templateid,deleted,status,templatetype,templatetitle from defaulttemplatepnl where country=? and deleted=?";
            params.add(countryID);
            params.add('F');
            
            if(!StringUtil.isNullOrEmpty(id)){
                condition += " and id = ? ";
                params.add(id);
            }
            
            templateQuery += condition;
                    
            list = executeSQLQuery(templateQuery, params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
}
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public boolean copyDefaultCustomLayout(Map<String, Object> params, Templatepnl templatepnl) {
        boolean success = true;
        Map<String, String> groupMap = new HashMap<>();
        List<String> totalGroupList = new ArrayList<>();
        List<String> layoutgroupIDDone=new ArrayList<>();
        try {
            String companyid = (String) params.get(Constants.companyKey);
            Company company = (Company) get(Company.class, companyid);
            KwlReturnObject result = getDefaultLayoutGroups(params);
            Map<String, Object> reqMap = new HashMap<>(); 
            List groupList = result.getEntityList();
            if (groupList != null && !groupList.isEmpty()) {
                for (Object object : groupList) {
                    Object[] groupArr = (Object[]) object;
                    if (layoutgroupIDDone.contains(groupArr[0].toString())) {
                        continue;
                    }
                    LayoutGroup group = new LayoutGroup();
                    group.setTemplate(templatepnl);
                    group.setSequence(Integer.parseInt(groupArr[3].toString()));
                    group.setCompany(company);
                    group.setName(groupArr[1].toString());
                    group.setShowtotal(Integer.parseInt(groupArr[6].toString()));
                    group.setNature(Integer.parseInt(groupArr[2].toString()));
                    group.setShowchild(Integer.parseInt(groupArr[7].toString()));
                    group.setShowchildacc(Integer.parseInt(groupArr[8].toString()));
                    if (groupArr[9].toString().equalsIgnoreCase("T")) {
                        group.setExcludeChildAccountBalances(true);
                    } else {
                        group.setExcludeChildAccountBalances(false);
                    }
                    saveOrUpdate(group);
                    layoutgroupIDDone.add( groupArr[0].toString());
                    groupMap.put(group.getID(), groupArr[0].toString());
                    if (group.getNature() == 5) {
                        totalGroupList.add(group.getID());
                    } else {
                        reqMap.clear();
                        reqMap.put("defaultlayoutgroup", groupArr[0].toString());
                        KwlReturnObject returnObject = getMappedAccount(reqMap);
                        List mappedAccountList = returnObject.getEntityList();
                        if (mappedAccountList != null && !mappedAccountList.isEmpty()) {
                            for (Object mappedAccount : mappedAccountList) {
                                Object[] mappedAccountArr = (Object[]) mappedAccount;
                                String accountName = mappedAccountArr[2].toString();
                                String groupname = mappedAccountArr[3] != null ? mappedAccountArr[3].toString() : null;
                                if (!StringUtil.isNullOrEmpty(accountName)) {
                                    reqMap.clear();
                                    reqMap.put("name", accountName);
                                    if (!StringUtil.isNullOrEmpty(groupname)) {
                                        reqMap.put("accgroupname", groupname);
                                    }
                                    reqMap.put("nature", group.getNature());
                                    reqMap.put(Constants.companyKey, companyid);
                                    List accountlist = getAccount(reqMap);
                                    if (accountlist != null && !accountlist.isEmpty()) {
                                        Account account = (Account) accountlist.get(0);
                                        GroupAccMap accMap = new GroupAccMap();
                                        accMap.setLayoutgroup(group);
                                        accMap.setAccount(account);
                                        accMap.setCompany(company);
                                        save(accMap);
                                    }
                                }
                            }
                        }
                    }
                    groupMap=saveDefaultLayoutChildGroups(group, groupArr[0].toString(), totalGroupList,layoutgroupIDDone,groupMap);
                }
                Map<String, String> reverseMap = new HashMap<>();
                for (Map.Entry<String, String> entry : groupMap.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    reverseMap.put(value, key);
                }
                if (totalGroupList != null && !totalGroupList.isEmpty()) {
                    for (Object groupid : totalGroupList) {
                        String copygroupid = groupMap.containsKey(groupid) ? groupMap.get(groupid) : null;
                        if (!StringUtil.isNullOrEmpty(copygroupid)) {
                            KwlReturnObject totalgroups = getDefaultCustomsGroupsForTotal(copygroupid);
                            List totalGroupsList = totalgroups.getEntityList();
                            if (totalGroupsList != null && !totalGroupsList.isEmpty()) {
                                for (Object object : totalGroupsList) {
                                    Object[] totalGroupsListArr = (Object[]) object;
                                    String group = totalGroupsListArr[2].toString();
                                    String action = totalGroupsListArr[3].toString();
                                    if (reverseMap.containsKey(group)) {
                                        Groupmapfortotal g = new Groupmapfortotal();
                                        g.setGroupidtotal((LayoutGroup) get(LayoutGroup.class, groupid.toString()));
                                        g.setAction(action);
                                        g.setGroupid((LayoutGroup) get(LayoutGroup.class, reverseMap.get(group)));
                                        saveOrUpdate(g);
                                    } else {
                                        Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.INFO, group);
                                    }
                                }
                            } else {
                                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.INFO, groupid.toString());
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            success = false;
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        } finally {
            return success;
        }
    }

    public KwlReturnObject getDefaultLayoutGroups(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("select id,name,nature,sequence,template,parent,showtotal,showchild,showchildacc,excludechildaccountbalances from defaultlayoutgroup ");
            String condition = "";
            if (filterParams.containsKey("templateid")) {
                condition = " where template=?";
                params.add(filterParams.get("templateid"));
            }
            if (filterParams.containsKey("parent")) {
                condition = " where parent=?";
                params.add(filterParams.get("parent"));
            }
            builder.append(condition);
            builder.append(" order by sequence");
            returnList = executeSQLQuery(builder.toString(), params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    private KwlReturnObject getMappedAccount(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("select id, defaultlayoutgroup, accountname, groupname from defaultgroupaccmap");
            String condition = "";
            if (filterParams.containsKey("defaultlayoutgroup")) {
                condition = " where defaultlayoutgroup=?";
                params.add(filterParams.get("defaultlayoutgroup"));
            }
            builder.append(condition);
            returnList = executeSQLQuery(builder.toString(), params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }

    private List getAccount(Map<String, Object> reqMap) {
        List list = new ArrayList();
        List parameters = new ArrayList();
        try {
            StringBuilder builder = new StringBuilder();
            builder.append("select a from Account a inner join a.group g where a.company.companyID = ? and a.name=?");
            parameters.add(reqMap.get(Constants.companyKey).toString());
            parameters.add(reqMap.get("name").toString());
            if (reqMap.containsKey("accgroupname") && reqMap.get("accgroupname") != null) {
                builder.append(" and g.name = ? ");
                parameters.add(reqMap.get("accgroupname").toString());
            }
            if (reqMap.containsKey("nature") && reqMap.get("nature") != null) {
                builder.append(" and g.nature=?");
                parameters.add(Integer.parseInt(reqMap.get("nature").toString()));
            }
            list = executeQuery(builder.toString(), parameters.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        } finally {
            return list;
        }
    }

    private Map<String, String> saveDefaultLayoutChildGroups(LayoutGroup parenttemplate, String copytemplate, List<String> grouptotals,List<String> layoutgroupIDDone,Map<String, String> groupMap) {
        try {
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("parent", copytemplate);
            KwlReturnObject returnObject = getDefaultLayoutGroups(reqMap);
            List children = returnObject.getEntityList();
            if (children != null && !children.isEmpty()) {
                for (Object object : children) {
                    Object[] groupArr = (Object[]) object;
                    if (layoutgroupIDDone.contains(groupArr[0].toString())) {
                        continue;
                    }
                    LayoutGroup group = new LayoutGroup();
                    group.setTemplate(parenttemplate.getTemplate());
                    group.setParent(parenttemplate);
                    group.setSequence(Integer.parseInt(groupArr[3].toString()));
                    group.setCompany(parenttemplate.getCompany());
                    group.setName(groupArr[1].toString());
                    group.setShowtotal(Integer.parseInt(groupArr[6].toString()));
                    group.setNature(Integer.parseInt(groupArr[2].toString()));
                    group.setShowchild(Integer.parseInt(groupArr[7].toString()));
                    group.setShowchildacc(Integer.parseInt(groupArr[8].toString()));
                    if (groupArr[9].toString().equalsIgnoreCase("T")) {
                        group.setExcludeChildAccountBalances(true);
                    } else {
                        group.setExcludeChildAccountBalances(false);
                    }
                    saveOrUpdate(group);
                    layoutgroupIDDone.add(groupArr[0].toString());
                    groupMap.put(group.getID(), groupArr[0].toString());
                    if (group.getNature() == 5) {
                        grouptotals.add(group.getID());
                    } else {
                        reqMap.clear();
                        reqMap.put("defaultlayoutgroup", groupArr[0].toString());
                        returnObject = getMappedAccount(reqMap);
                        List accountList = returnObject.getEntityList();
                        if (accountList != null && !accountList.isEmpty()) {
                            for (Object mappedAccount : accountList) {
                                Object[] mappedAccountArr = (Object[]) mappedAccount;
                                String accountName = mappedAccountArr[2].toString();
                                String groupname = mappedAccountArr[3] != null ? mappedAccountArr[3].toString() : null;
                                if (!StringUtil.isNullOrEmpty(accountName)) {
                                    reqMap.clear();
                                    reqMap.put("name", accountName);
                                    if (!StringUtil.isNullOrEmpty(groupname)) {
                                        reqMap.put("accgroupname", groupname);
                                    }
                                    reqMap.put("nature", group.getNature());
                                    reqMap.put(Constants.companyKey, parenttemplate.getCompany().getCompanyID());
                                    List accountlist = getAccount(reqMap);
                                    if (accountList != null && !accountlist.isEmpty()) {
                                        Account account = (Account) accountlist.get(0);
                                        GroupAccMap accMap = new GroupAccMap();
                                        accMap.setLayoutgroup(group);
                                        accMap.setAccount(account);
                                        accMap.setCompany(parenttemplate.getCompany());
                                        saveOrUpdate(accMap);
                                    }
                                }
                            }
                        }
                    }
                    groupMap = saveDefaultLayoutChildGroups(group, groupArr[0].toString(), grouptotals, layoutgroupIDDone, groupMap);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        } finally {
            return groupMap;
        }
    }

    private KwlReturnObject getDefaultCustomsGroupsForTotal(String groupid) {
        List returnList = new ArrayList();
        try {
            List params = new ArrayList();
            StringBuilder builder = new StringBuilder("select id,groupidtotal, groupid, action from defaultgroupmapfortotal ");
            if (!StringUtil.isNullOrEmpty(groupid)) {
                builder.append(" where groupidtotal = ?");
                params.add(groupid);
            }
            returnList = executeSQLQuery(builder.toString(), params.toArray());
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        } finally {
            return new KwlReturnObject(true, "", null, returnList, returnList.size());
        }

    }
    public KwlReturnObject getDefaultPnLTemplates(HashMap<String, Object> filterParams){
        List list = new ArrayList();
        int count = 0;
        try {
            ArrayList params = new ArrayList();
            boolean paging = false;
            int start = 0;
            int limit = 0;
            String condition = "";
            if (filterParams.containsKey("templateid")) {
                condition = " ID = ? and ";
                params.add((String) filterParams.get("templateid"));
}
            if (filterParams.containsKey("templatetype") && filterParams.get("templatetype") != null) {
                condition = " templatetype = ? and ";
                params.add((Integer) filterParams.get("templatetype"));
            }
            if (filterParams.containsKey("start") && filterParams.containsKey("limit")) {
                start = (Integer) filterParams.get("start");
                limit = (Integer) filterParams.get("limit");
                paging = true;
            }
            String query = "from DefaultTemplatePnL where " + condition + " deleted = false order by name ";
            list = executeQuery(query, params.toArray());
            count = list.size();
            if (paging) {
                list = executeQueryPaging(query, params.toArray(), new Integer[]{start, limit});
            }
        } catch (Exception ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    public KwlReturnObject updateDefaultPnLTemplate(Map<String, Object> dataMap) throws ServiceException {
        List list = new ArrayList();
        try {
            DefaultTemplatePnL templatepnl = null;

            if (dataMap.containsKey("id")) {
                templatepnl = (DefaultTemplatePnL) get(DefaultTemplatePnL.class, (String) dataMap.get("id"));
            } else {
                templatepnl = new DefaultTemplatePnL();
            }

            if (dataMap.containsKey("name")) {
                templatepnl.setName((String) dataMap.get("name"));
            }

            if (dataMap.containsKey("templatetitle")) {
                templatepnl.setTemplatetitle((String) dataMap.get("templatetitle"));
            }

            if (dataMap.containsKey("templateid")) {
                templatepnl.setTemplateid((Integer) dataMap.get("templateid"));
            }

            if (dataMap.containsKey("templatetype")) {
                templatepnl.setTemplatetype((Integer) dataMap.get("templatetype"));
            }

            if (dataMap.containsKey("status")) {
                templatepnl.setStatus((Integer) dataMap.get("status"));
            }

            if (dataMap.containsKey("countryid")) {
                Country country = (Country) get(Country.class, (String) dataMap.get("countryid"));
                templatepnl.setCountry(country);
            }

            if (dataMap.containsKey("deleted")) {
                templatepnl.setDeleted((Boolean) dataMap.get("deleted"));
            } else {
                templatepnl.setDeleted(false);
            }

            save(templatepnl);
            list.add(templatepnl);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("saveInvoice : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    public void updateAccountOpeningBalance(String accountId, double amount) throws ServiceException {
        String query = "update account set openingbalance=" + amount + " where id=?";
        ArrayList arrayList = new ArrayList();
        arrayList.add(accountId);
        int numRows = executeSQLUpdate(query, arrayList.toArray());
    }
    
    @Override
    public void updateAccountOpeningBalance(String dbName,String accountId, double amount) throws ServiceException {
        String query = "update "+dbName+".account as acc set acc.openingbalance=" + amount + " where acc.id=?";
        ArrayList arrayList = new ArrayList();
        arrayList.add(accountId);
        int numRows = executeSQLUpdate(query, arrayList.toArray());
    }
    
     public KwlReturnObject saveIndianTermsCompanyLevel(HashMap<String, Object> termMap) throws ServiceException {
        List list = new ArrayList();
        try {
            LineLevelTerms invoiceTerm = new LineLevelTerms();
            if (termMap.containsKey("id")) {
                invoiceTerm = (LineLevelTerms) get(LineLevelTerms.class, termMap.get("id").toString());
            }

            if (termMap.containsKey("term")) {
                invoiceTerm.setTerm(termMap.get("term").toString());
            }

            if (termMap.containsKey("formula")) {
                invoiceTerm.setFormula(termMap.get("formula").toString());
            }
            if (termMap.containsKey("sign")) {
                invoiceTerm.setSign(Integer.parseInt(termMap.get("sign").toString()));
            }
            invoiceTerm.setDeleted(0);
            if (termMap.containsKey("deleted")) {
                invoiceTerm.setDeleted(Integer.parseInt(termMap.get("deleted").toString()));
            }
//
            if (termMap.containsKey("createdon")) {
                invoiceTerm.setCreatedOn(((Date) termMap.get("creationdate")).getTime());
            }
            if (termMap.containsKey("companyid")) {
                invoiceTerm.setCompany((Company) get(Company.class, termMap.get("companyid").toString()));
            }
            if (termMap.containsKey("accountid")) {
                Account termAccount = (Account) get(Account.class, termMap.get("accountid").toString());
                termAccount.setControlAccounts(true);
                termAccount.setUsedIn(StringUtil.getUsedInValue(termAccount.getUsedIn(), Constants.Term_Account));
                invoiceTerm.setAccount(termAccount);
            }
            if (termMap.containsKey("payableaccountid")) {
                Account termAccount = (Account) get(Account.class, termMap.get("payableaccountid").toString());
                termAccount.setControlAccounts(true);
                termAccount.setUsedIn(StringUtil.getUsedInValue(termAccount.getUsedIn(), Constants.Term_Account));
                invoiceTerm.setPayableAccount(termAccount);
            }
            if (termMap.containsKey("masteritem")) {
                invoiceTerm.setMasteritem((MasterItem) get(MasterItem.class, termMap.get("masteritem").toString()));
            }
            if (termMap.containsKey("userId")) {
                invoiceTerm.setCreator((User) get(User.class, termMap.get("userId").toString()));
            }
            if (termMap.containsKey("salesOrPurchaseFlag")) {
                invoiceTerm.setSalesOrPurchase(Boolean.valueOf(termMap.get("salesOrPurchaseFlag").toString()));
            }
            if (termMap.containsKey("percent")) {
                invoiceTerm.setPercentage(Double.parseDouble(termMap.get("percent").toString()));
            }
            if (termMap.containsKey("termamount")) {
                invoiceTerm.setTermAmount(Double.parseDouble(termMap.get("termamount").toString()));
            }
            if (termMap.containsKey("termtype")) {
                invoiceTerm.setTermType(Integer.parseInt(termMap.get("termtype").toString()));
            }
            if (termMap.containsKey("termsequence")) {
                invoiceTerm.setTermSequence(Integer.parseInt(termMap.get("termsequence").toString()));
            }
            if (termMap.containsKey("formulaids")) {
                invoiceTerm.setFormulaids(termMap.get("formulaids").toString());
            }
            if (termMap.containsKey("purchasevalueorsalevalue")) {
                invoiceTerm.setPurchaseValueOrSaleValue(Double.parseDouble(termMap.get("purchasevalueorsalevalue").toString()));
            }
            if (termMap.containsKey("deductionorabatementpercent")) {
                invoiceTerm.setDeductionOrAbatementPercent(Double.parseDouble(termMap.get("deductionorabatementpercent").toString()));
            }
            if (termMap.containsKey("taxtype")) {
                invoiceTerm.setTaxType(Integer.parseInt(termMap.get("taxtype").toString()));
            }
            if (termMap.containsKey("isDefault")) {
                invoiceTerm.setIsDefault(Boolean.valueOf(termMap.get("isDefault").toString()));
            }
            if (termMap.containsKey("IsOtherTermTaxable")) {
                invoiceTerm.setOtherTermTaxable(Boolean.valueOf(termMap.get("IsOtherTermTaxable").toString()));
            }
            if (termMap.containsKey("isAdditionalTax")) {
                invoiceTerm.setIsAdditionalTax(Boolean.valueOf(termMap.get("isAdditionalTax").toString()));
            }
            if (termMap.containsKey("includeInTDSCalculation")) {
                invoiceTerm.setIncludeInTDSCalculation(Boolean.valueOf(termMap.get("includeInTDSCalculation").toString()));
            }
            if (termMap.containsKey("creator")) {
                invoiceTerm.setCreator((User)get(User.class,termMap.get("creator").toString()));
            }
            if (termMap.containsKey("formType")) {
                invoiceTerm.setFormType(termMap.get("formType").toString());
            }
            if (termMap.containsKey("creditnotavailedaccount")) {
                invoiceTerm.setCreditNotAvailedAccount((Account) get(Account.class, termMap.get("creditnotavailedaccount").toString()));
            }
            saveOrUpdate(invoiceTerm);
            list.add(invoiceTerm);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }
     public boolean isDuplicateLineLevelTerm(HashMap<String, Object> termMap) throws ServiceException{
        List list = null;
        boolean isDuplicateTerm = false;
        int count = 0;
        try{
            String companyId = termMap.get("companyid") + "";
            String term = termMap.get("term").toString();
            boolean salesOrPurchaseFlag = false;
            if (!StringUtil.isNullOrEmpty(termMap.get("salesOrPurchaseFlag").toString())) {
                salesOrPurchaseFlag = Boolean.parseBoolean(termMap.get("salesOrPurchaseFlag").toString());
            }           
            ArrayList params = new ArrayList();
            String query = "select id from linelevelterms where term=? and company=? and deleted=0 and salesorpurchase=?";
            if (!StringUtil.isNullOrEmpty(companyId)) {
                params.add(term);
                params.add(companyId);
                params.add(salesOrPurchaseFlag);
                list = executeSQLQuery( query, params.toArray());
            }  
            
            if(list!=null)
            count = list.size();
            
            if(count>0)
                isDuplicateTerm = true;            
        }catch(ServiceException ex){
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        }      
        return isDuplicateTerm;
    }
     
    @Override
    public boolean insertDefaultCustomeFields(HashMap<String, Object> requestParams) throws ServiceException {
        boolean successFlag = false;
        try {
            ArrayList params = new ArrayList();
            KwlReturnObject result = null;
            Map<String,String> defaultValues=new HashMap<>();
            String companyid = (String) requestParams.get(Constants.companyid);
            String stateid = requestParams.containsKey(Constants.STATE_ID) ? (String) requestParams.get(Constants.STATE_ID) : null;
            String countryid = requestParams.containsKey(Constants.COUNTRY_ID) ? (String) requestParams.get(Constants.COUNTRY_ID) : null;
            Boolean isMultiEntity = (requestParams.containsKey(Constants.isMultiEntity) ? Boolean.parseBoolean(requestParams.get(Constants.isMultiEntity).toString()) : false);
            String defaultvalue = requestParams.containsKey("DefaultValue") ? (String) requestParams.get("DefaultValue") : null;
            boolean skippForProductModule=false;
            boolean afterSetUp=false,isForGSTMigration=false;
            if (requestParams.containsKey("skippForProductModule")) { 
                skippForProductModule = (boolean) requestParams.get("skippForProductModule");
            }
            if (requestParams.containsKey("afterSetUp")) {   // to identify call is after company setup..
                afterSetUp = (boolean) requestParams.get("afterSetUp");
            }
            if (requestParams.containsKey("isForGSTMigration")) {   // to identify call is after company setup..
                isForGSTMigration = (boolean) requestParams.get("isForGSTMigration");
            }            
            String stateName[]= "Andaman and Nicobar Islands,Andhra Pradesh,Andhra Pradesh (New),Arunachal Pradesh,Assam,Bihar,Chandigarh,Chattisgarh,Dadra and Nagar Haveli,Daman and Diu,Delhi,Goa,Gujarat,Haryana,Himachal Pradesh,Jammu and Kashmir,Jharkhand,Karnataka,Kerala,Lakshadweep Islands,Madhya Pradesh,Maharashtra,Manipur,Meghalaya,Mizoram,Nagaland,Odisha,Pondicherry,Punjab,Rajasthan,Sikkim,Tamil Nadu,Telangana,Tripura,Uttar Pradesh,Uttarakhand,West Bengal".split(",");
//            String CountryQuery = "";
            String subQuery = ""; // India Country Specific Check which is applicable for other compliances as well
//            String stateQuery = " stateid.ID IS NULL and ";
            String orderQuery = " order by insertsequence asc ";

            if (!StringUtil.isNullOrEmpty(stateid)) {
                subQuery = subQuery+ " ( stateid.ID IS NULL or stateid.ID = ? ) ";
                params.add(stateid);
            } else {
                subQuery = subQuery+ " stateid.ID IS NULL ";
            }
            if (!StringUtil.isNullOrEmpty(countryid)) {
                subQuery =subQuery+  " and countryid.ID = ? ";
                params.add(countryid);
            } else {
                subQuery = subQuery+  " and countryid.ID IS NULL ";
            }
            if (isMultiEntity) {
                subQuery =subQuery+ " and gstconfigtype = '1' ";
            } else {
                subQuery =subQuery+ " and gstconfigtype != '1'";
            } 

            String hql = " from DefaultCustomFields where " + subQuery + orderQuery;
            List<DefaultCustomFields> list1 = executeQuery(hql, params.toArray());
//            if(afterSetUp && (Integer.parseInt(countryid)==Constants.indian_country_id)){
//                List<DefaultCustomFields> twisteList=new ArrayList<DefaultCustomFields>();
//                for (DefaultCustomFields customFields : list1) {
//                    if(customFields.getFieldlabel().equals(Constants.GSTProdCategory) || customFields.getFieldlabel().equals("State") || customFields.getFieldlabel().equals(Constants.GST_UNIT_QUANTITY_CODE))
//                        twisteList.add(customFields); // here only Product Tax Class need to be created so discarding other entries..
//                }    
//                list1=twisteList;
//            }
//            if(isForGSTMigration && (Integer.parseInt(countryid)==Constants.indian_country_id)){
//                List<DefaultCustomFields> twisteList=new ArrayList<DefaultCustomFields>();
//                List<String> feilds=Arrays.asList(("GSTIN,TIN,PAN,HSN/SAC Code,Transport Mode,Distance level (Km),Transporter Name,Transporter ID,Transporter Doc No,Transportation Date,Vehicle No,Supply Type,Sub Type,Document Type,E-Way Unit,Reason,"
//                        +Constants.GST_E_Commerce_Operator+","+Constants.GSTR1_SHIPPING_BILL_NO+","+Constants.GSTR1_SHIPPING_DATE+","+Constants.GSTR1_SHIPPING_PORT).split(","));
//                for (DefaultCustomFields customFields : list1) {                                                              
//                    if(feilds.contains(customFields.getFieldlabel()))
//                        twisteList.add(customFields); // here only Product Tax Class need to be created so discarding other entries..
//                }    
//                list1=twisteList;
//            }

            Integer colnum = 1, moduleflag = 0;
            for (DefaultCustomFields customFields : list1) {
                HashMap fieldParamsMap = new HashMap<>();
                HashMap<String, Object> duplicateCheckParams = new HashMap<>();
                if (!StringUtil.isNullOrEmpty(customFields.getDefaultValue())) {
                    /*
                     * If default value is provided in DefaultCustomFields table then this value is used as default value.
                     */
                    defaultvalue = customFields.getDefaultValue();
                }
                // here default value for Product Category and State Code is avoided
                String moduleSpecificDefaultValue = (customFields.getFieldlabel().equals(Constants.GSTProdCategory) || customFields.getFieldlabel().equals(Constants.GST_UNIT_QUANTITY_CODE))?null:defaultvalue;                  
                if (customFields.getModuleid() == null) {
                    continue;       //if moduleid is null
                }
                List<String> moduleIDsList = Arrays.asList(customFields.getModuleid().split(","));
                for (String moduleId : moduleIDsList) {
                    fieldParamsMap = new HashMap<>();
                    if(Integer.parseInt(moduleId)==30 && skippForProductModule){
                        continue;
                    }
                    /**
                     * Validate Is Field present or not if present then update
                     * the same field
                     */
                    Map<String, Object> dupCheckRequestParams = new HashMap<String, Object>();
                    dupCheckRequestParams.put(Constants.filter_names, new ArrayList<String>(Arrays.asList(Constants.fieldlabel, "company.companyID", Constants.moduleid)));
                    dupCheckRequestParams.put(Constants.filter_values, new ArrayList<Object>(Arrays.asList(customFields.getFieldlabel(), companyid, Integer.valueOf(moduleId))));
                    KwlReturnObject resultDupCheck = getFieldParamsIds(dupCheckRequestParams);
                    if (resultDupCheck != null && resultDupCheck.getEntityList()!= null &&  resultDupCheck.getEntityList().size() > 0) {
                       String fieldP = (String) resultDupCheck.getEntityList().get(0);
                       fieldParamsMap.put("Id", fieldP);
                    }
                    fieldParamsMap.put("Maxlength", customFields.getMaxlength());
                    /**
                     * For Field Label HSN/ SAC code is not mandatory in 1200(GST Modules) and 16(Receive Payment) module 
                     */
                    if (!StringUtil.isNullOrEmpty(countryid)) {
                        
                        if ((Integer.parseInt(countryid) == Constants.indian_country_id) && customFields.getFieldlabel().equals(Constants.HSN_SACCODE)) {
                            if(Integer.parseInt(moduleId) == Constants.Acc_FixedAssets_AssetsGroups_ModuleId || Integer.parseInt(moduleId) == Constants.Acc_Product_Master_ModuleId){
                                fieldParamsMap.put("Isessential", 1);
                            } else {
                                fieldParamsMap.put("Isessential", 0);    
                            }
                        } else {
                            fieldParamsMap.put("Isessential", customFields.getIsessential());
                        }
                        if ((Integer.parseInt(countryid) == Constants.indian_country_id) && customFields.getFieldlabel().equals(Constants.STATE)) {
                            if(Integer.parseInt(moduleId) == Constants.Acc_Customer_ModuleId || Integer.parseInt(moduleId) == Constants.Acc_Vendor_ModuleId){
                                fieldParamsMap.put("AllowInDocumentDesigner", 0);
                            } else {
                                fieldParamsMap.put("AllowInDocumentDesigner", 1);
                            }
                        } else {
                            fieldParamsMap.put("AllowInDocumentDesigner", 1);
                        }
                    }
                    fieldParamsMap.put("sendNotification", 0);
                    fieldParamsMap.put("isforproject", 0);
                    fieldParamsMap.put("Fieldtype", customFields.getFieldtype());
                    fieldParamsMap.put("Isforeclaim", 0);
                    fieldParamsMap.put("Customregex", "");
                    fieldParamsMap.put("Fieldname", Constants.Custom_Record_Prefix + customFields.getFieldlabel());
                    fieldParamsMap.put("Fieldlabel", customFields.getFieldlabel());
                    fieldParamsMap.put("Fieldtooltip", !StringUtil.isNullOrEmpty(customFields.getFieldtooltip())?customFields.getFieldtooltip():"");
                    fieldParamsMap.put("Companyid", companyid);
                    fieldParamsMap.put("Moduleid", Integer.parseInt(moduleId));
                    fieldParamsMap.put("Customfield", customFields.getCustomfield());
                    fieldParamsMap.put("Customcolumn", customFields.getCustomcolumn());
                    fieldParamsMap.put("IsActivated", 1);//For newly created default activation will be 1
//                    fieldParamsMap.put("IsForMultiEntity", customFields.isIsformultientity()); // 
                    fieldParamsMap.put("Moduleflag", 0);
                    fieldParamsMap.put("Iseditable", "true");
                    fieldParamsMap.put("isfortask", 0);
                    fieldParamsMap.put("DefaultValue", moduleSpecificDefaultValue); //For Product Category Default Value is not given
                    fieldParamsMap.put("IsAutoPopulateDefaultValue", customFields.isIsAutoPopulateDefaultValue());  
                    /**
                     * Added one more column in Default custom field i.e GSTConfigType
                     */
                    fieldParamsMap.put("GSTConfigType", customFields.getGSTConfigType());  
                    /**
                     * Added two column in Default custom field for GST INDIA and US
                     */
                    fieldParamsMap.put("GSTMappingColnum", customFields.getGSTMappingColnum());
                    fieldParamsMap.put(Constants.RELATED_MODULE_IS_ALLOW_EDIT, customFields.getRelatedModuleIsAllowEdit());
                    fieldParamsMap.put(Constants.relatedmoduleid, customFields.getRelatedmoduleid());
//                    fieldParamsMap.put("IsForGSTRuleMapping", customFields.isIsForGSTRuleMapping());
                  /*  if(customFields.getFieldlabel().equals("State")){                    
                        fieldParamsMap.put("IsForGSTRuleMapping","true");
                        fieldParamsMap.put("GSTMappingColnum", 1);
//                        HashMap<String, Object> gstRefColParams = null;
//                        //this is block is added to get next col nummber for GST custom fields
//                        int GSTMappingColnum = 0;                     
//                            gstRefColParams = getMaxGSTMappingColumn(companyid);
//                            if ((boolean) gstRefColParams.get("success")) {
//                            GSTMappingColnum = (int) gstRefColParams.get("colNum");                        
//                            fieldParamsMap.put("GSTMappingColnum", GSTMappingColnum);                            
//                        }

                    }*/

                    colnum = (Integer) getcolumn_number(companyid, Integer.parseInt(moduleId), customFields.getFieldtype(), moduleflag).get("column_number");
                    fieldParamsMap.put("Colnum", colnum);
                    KwlReturnObject kmsg = null;

                    List list = null;   
                    dupCheckRequestParams = new HashMap<String, Object>();
                    duplicateCheckParams.put("filter_names", Arrays.asList("companyid", "moduleid", "fieldlabel"));
                    duplicateCheckParams.put("filter_values", Arrays.asList(companyid, Integer.parseInt(moduleId),customFields.getFieldlabel()));
                    KwlReturnObject duplicate=getFieldParams(duplicateCheckParams);
                    list=duplicate.getEntityList();
                    if(!list.isEmpty()){
                        continue;
                    }                                                            
                    FieldParams fp = null;
                    kmsg = insertfield(fieldParamsMap);
                    
                    if(customFields.getFieldlabel().equals("State") && countryid.equalsIgnoreCase(String.valueOf(Constants.indian_country_id))){
                        defaultValues.clear();
                        KwlReturnObject kmsg1 = getStateCodes(Constants.INDIA_COUNTRYID);                        
                        List<DefaultStateValues> states= kmsg1.getEntityList();
                        for(DefaultStateValues state:states){
                            defaultValues.put(state.getStateName(),state.getStateCode());
                        }
                        moduleSpecificDefaultValue="";
                    }else if (!customFields.getFieldlabel().equals(Constants.GSTProdCategory) && !customFields.getFieldlabel().equals(Constants.ENTITY) && countryid.equalsIgnoreCase(String.valueOf(Constants.indian_country_id)) && customFields.getFieldtype()==Constants.SINGLESELECTCOMBO) {
                        defaultValues.clear();
                        KwlReturnObject kmsg1 = getFieldComboValuesForDefaultCustomFields(customFields.getId());                        
                        List<DefaultFieldComboValues> values= kmsg1.getEntityList();
                        for(DefaultFieldComboValues comboValue:values){
                            defaultValues.put(comboValue.getFieldComboValue(),"");
                        }
                        moduleSpecificDefaultValue="";
                    } else if (countryid != null & countryid.equalsIgnoreCase(Constants.INDONESIAN_COUNTRYID)) {
                        defaultValues.clear();
                        KwlReturnObject kmsg1 = getFieldComboValuesForDefaultCustomFields(customFields.getId());
                        List<DefaultFieldComboValues> values = kmsg1.getEntityList();
                        for (DefaultFieldComboValues comboValue : values) {
                            defaultValues.put(comboValue.getFieldComboValue(), "");
                        }
                    }else{
                        defaultValues.clear();
                        defaultValues.put(defaultvalue,null);                        
                        
                    }
                    
                    /**
                     * Copy Address field for GST rule mapping 
                     */
                    if (kmsg.isSuccessFlag() && (customFields.getGSTConfigType()==Constants.GST_CONFIG_ISFORGST) && Integer.parseInt(moduleId) == Constants.GSTModule) {
                        HashMap<String , Object> paramMap = new HashMap<String , Object>();
                        fp = (FieldParams) kmsg.getEntityList().get(0);
                        paramMap.put("addressField", customFields.getFieldlabel());
                        paramMap.put("fieldId", fp.getId());
                        paramMap.put("company", companyid);
                        /**
                         * Check If AddressFieldDimensionMapping is present or not if Present
                         * then update same AddressFieldDimensionMapping
                         * Ticket - ERP-35391
                         */
                        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                        filterRequestParams.put(Constants.filter_names, new ArrayList<String>(Arrays.asList("company.companyID", "addressfield","dimension.id")));
                        filterRequestParams.put(Constants.filter_params, new ArrayList<String>(Arrays.asList(companyid, customFields.getFieldlabel(),fp.getId())));
                        KwlReturnObject resultList = getAddressFieldDimensionMapping(filterRequestParams);
                        if (resultList != null && resultList.getEntityList() != null && !resultList.getEntityList().isEmpty()) {
                            AddressFieldDimensionMapping addFieldMapping = (AddressFieldDimensionMapping) resultList.getEntityList().get(0);
                            paramMap.put("id", addFieldMapping.getId());
                        }
                        saveOrUpdateAddressFieldForGSTDimension(paramMap);
                    }
                    if (kmsg.isSuccessFlag() && !StringUtil.isNullObject(moduleSpecificDefaultValue)) {
                        fp = (FieldParams) kmsg.getEntityList().get(0);                        
                        for (Map.Entry<String, String> defaultValue : defaultValues.entrySet()) {                                                                                    
                            HashMap<String, Object> comborequestParams = new HashMap<>();
                            comborequestParams.put("Fieldid", fp.getId());
                            comborequestParams.put("Value", defaultValue.getKey());
                            comborequestParams.put("Itemdescription", defaultValue.getValue()!=null?defaultValue.getValue():null);                            
                            comborequestParams.put("Activatedeactivatedimensionvalue", true);//By default Dimension value will be activated

                            String Defaultcombodata = "";
                            KwlReturnObject resultkmsg = getfieldcombodata(comborequestParams);

                            if (resultkmsg.getEntityList().isEmpty() && customFields.getFieldtype() == 4) {
                                KwlReturnObject kmsg1 = insertfieldcombodata(comborequestParams);

                                FieldComboData fc = null;
                                fc = (FieldComboData) kmsg1.getEntityList().get(0);
                                if (!StringUtil.isNullOrEmpty(defaultvalue)) {
                                    Defaultcombodata = fc.getId();
                                } else {
                                    Defaultcombodata = "";
                                }

                                HashMap<String, Object> defaultParams = new HashMap<>();
                                defaultParams.put(Constants.RES_success, "1");
                                defaultParams.put(Constants.defaultvalue, Defaultcombodata);
                                defaultParams.put(Constants.Colnum, colnum);
                                defaultParams.put(Constants.Fieldtype, customFields.getFieldtype());
                                defaultParams.put(Constants.moduleid, Integer.parseInt(moduleId));
                                defaultParams.put(Constants.companyid, companyid);
                                if (!requestParams.containsKey("skippForProductModule")) {
                                    storeDefaultCstmData(defaultParams);
                                }
                            }
                            successFlag = true;// true when custom field created successfully
                        }
                    }
                    }
                }
        } catch (Exception ex) {
            successFlag = false;//false when custom field creation failed
            throw ServiceException.FAILURE("Error occurred while inserting Custom Field(s)/Dimension(s).", ex);//+ex.getMessage(), ex);
        }
        return successFlag;
    }
    @Override
    public boolean insertFieldcomboValues(HashMap<String, Object> requestParams) throws ServiceException {
        boolean successFlag = false;
        try {
            ArrayList params = new ArrayList();
            String companyid = (String) requestParams.get(Constants.companyid);
            String countryid = requestParams.containsKey(Constants.COUNTRY_ID) ? (String) requestParams.get(Constants.COUNTRY_ID) : null;
            String subQuery = "";
            if (!StringUtil.isNullOrEmpty(countryid)) {
                subQuery = " countryid.ID = ? ";
                params.add(countryid);
            }
            /*
              Fetching all defaultCustomFields (ERM-1108)
            */
            String hql = " from DefaultCustomFields where" + subQuery;
            List<DefaultCustomFields> list1 = executeQuery(hql, params.toArray());
            if(!list1.isEmpty()){
            for (DefaultCustomFields customFields : list1) {  // Iterate each defaultcustomfield (ERM-1108)
                HashMap<String, Object> checkParams = new HashMap<>();
                List<String> moduleIDsList = Arrays.asList(customFields.getModuleid().split(","));
                for (String moduleId : moduleIDsList) {
                    List<FieldParams> list = null;
                    String fp="";
                    checkParams.put("filter_names", Arrays.asList("companyid", "moduleid", "fieldlabel"));
                    checkParams.put("filter_values", Arrays.asList(companyid, Integer.parseInt(moduleId), customFields.getFieldlabel()));
                    /*
                     *   Find entry from fieldparams for defaultcustomfield, by providing it's FieldLabel,companyid and moduleid (ERM-1108)
                     */
                    KwlReturnObject FieldParamsResult = getFieldParams(checkParams);
                    list = FieldParamsResult !=null && FieldParamsResult.getEntityList()!=null && !FieldParamsResult.getEntityList().isEmpty() ? FieldParamsResult.getEntityList() : null;
                    if (list !=null && countryid.equalsIgnoreCase(String.valueOf(Constants.indian_country_id))) { // Iterate each entry of fieldparams  (ERM-1108)                      
                        fp=list.get(0)!=null && (String)list.get(0).getId()!=null?(String)list.get(0).getId():"";  
                        /*
                         * Find defaultFieldcomboValue for defaultCustomField (ERM-1108)
                         */
                        KwlReturnObject kmsg1 = getFieldComboValuesForDefaultCustomFields(customFields.getId());
                        List<DefaultFieldComboValues> values = (kmsg1 != null && kmsg1.getEntityList() != null) ? kmsg1.getEntityList() : null;
                        for (DefaultFieldComboValues comboValue : values) { // Iterate each entry of defaultFieldcomboValue  (ERM-1108)
                            HashMap<String, Object> comborequestParams = new HashMap<>();
                            comborequestParams.put("Fieldid", fp);
                            comborequestParams.put("Value", comboValue.getFieldComboValue()!=null?comboValue.getFieldComboValue():"");
                            comborequestParams.put("Activatedeactivatedimensionvalue", true); //By default Dimension value will be activated
                            KwlReturnObject resultkmsg = getfieldcombodata(comborequestParams);
                            /*
                             * Adding entry in fieldComboData if value is not present for same field. (ERM-1108)
                             */
                            if (resultkmsg!=null && resultkmsg.getEntityList().isEmpty()) {
                                KwlReturnObject fcdData = insertfieldcombodata(comborequestParams);
                            }
                        }
                    }
                }
            }
        }
            successFlag = true;// true when entry inserted in fieldcombodata successfully

        } catch (Exception ex) {
            successFlag = false;//false when insertion in fieldcombodata failed
            throw ServiceException.FAILURE("Error occurred while inserting Custom Field(s)/Dimension(s) data.", ex);//+ex.getMessage(), ex);
        }
        return successFlag;
    }
    
    public KwlReturnObject getStateCodes(String countryId) throws ServiceException{            
        String query = "From DefaultStateValues where country.id=? ";
         ArrayList params = new ArrayList();
        params.add(countryId);
        List list = executeQuery( query, params.toArray());
        KwlReturnObject result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    }    
    public KwlReturnObject getFieldComboValuesForDefaultCustomFields(String defaultCustomFieldId) throws ServiceException{            
        String query = "From DefaultFieldComboValues where customFields.id=? ";
         ArrayList params = new ArrayList();
        params.add(defaultCustomFieldId);
        List list = executeQuery( query, params.toArray());
        KwlReturnObject result = new KwlReturnObject(true, null, null, list, list.size());
        return result;
    } 
    @Override
    public int updateExtraComPreferences(HashMap<String, Object> requestParams) throws ServiceException {                
        int numRows =0;        
        try {
            if (requestParams.containsKey("companyid") && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {
                String companyid=requestParams.get("companyid").toString();                
                String querString="update extracompanypreferences set isnewgst= 'T' , lineleveltermflag='1' where id=?";
                //numRows = executeSQLUpdate(query, new Object[]{hm.get("accountid"), hm.get("taxtype"), termid});
                numRows = executeSQLUpdate(querString, new Object[]{companyid});                                             
            }                             
        } catch (Exception e) {
            throw ServiceException.FAILURE("Error Occurred while updating ExtraCompanyPreferences.", e);
        }
        return numRows;
    }
    @Override
    public KwlReturnObject getMasterItemfromAccount(String accountid, String companyid) throws ServiceException {
          String query = "select * from masteritem where accid = ? and company = ? ";  
        ArrayList params = new ArrayList();
        params.add(accountid);
        params.add(companyid);
        List list = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    @Override
    public HashMap<String, Object> getcolumn_number(String companyid, Integer moduleid, Integer fieldtype, int moduleflag) throws SessionExpiredException, JSONException {
        KwlReturnObject result = null;
        JSONObject jobj = new JSONObject();
        boolean Notreachedlimit = true;
        HashMap<String, Object> requestParams = new HashMap<>();
        try {
            Integer colcount = 1;
            Integer custom_column_start = 0, Custom_Column_limit = 0;

            switch (fieldtype) {
                case 1: //text field
                case 2: //Number field
//                case 3: //Date
                case 5:
                case 9://  auto number
                case 6:
                case 13: // Text Area
                case 12:
                    custom_column_start = Constants.Custom_Column_Normal_start;
                    Custom_Column_limit = Constants.Custom_Column_Normal_limit;
                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype", ">colnum", "<=colnum"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "1,2,3,5,6,7,9,12,13", custom_column_start, custom_column_start + Custom_Column_limit));
                    break;
                case Constants.DATEFIELD: //Date 
                    custom_column_start = Constants.Custom_Column_Date_start;
                    Custom_Column_limit = Constants.Custom_Column_Date_limit;
                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype", ">colnum", "<=colnum"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "3", custom_column_start, custom_column_start + Custom_Column_limit));
                    break;    
                case 11:
                    custom_column_start = Constants.Custom_Column_Check_start;
                    Custom_Column_limit = Constants.Custom_Column_Check_limit;
                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype", ">colnum", "<=colnum"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "11", custom_column_start, custom_column_start + Custom_Column_limit));
                    break;

                case 4:
                case 7:
                    custom_column_start = Constants.Custom_Column_Combo_start;
                    Custom_Column_limit = Constants.Custom_Column_Combo_limit;
                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "4,7"));
                    break;
                case 8:
                    if (moduleflag == 1) {
                        custom_column_start = Constants.Custom_Column_User_start;
                        Custom_Column_limit = Constants.Custom_Column_User_limit;
                    } else {
                        custom_column_start = Constants.Custom_Column_Master_start;
                        Custom_Column_limit = Constants.Custom_Column_Master_limit;
                    }

                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "fieldtype", "moduleflag"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, fieldtype, moduleflag));
                    break;
            }

            result = getFieldParams(requestParams);
            List lst = result.getEntityList();
            colcount = lst.size();
            if (colcount == Custom_Column_limit) {
                jobj.put("success", "msg");
                jobj.put("title", "Alert");
                jobj.put("msg", "Cannot add new field. Maximum custom field limit reached.");
                Notreachedlimit = false;
            }
            if (Notreachedlimit) {
                Iterator ite = lst.iterator();
                int[] countchk = new int[Custom_Column_limit + 1];
                while (ite.hasNext()) {
                    FieldParams tmpcontyp = (FieldParams) ite.next();

                    if ((fieldtype == 4 || fieldtype == 7) && tmpcontyp.getFieldtype() == 7) {
                        countchk[tmpcontyp.getRefcolnum() - custom_column_start] = 1;
                    } else {
                        countchk[tmpcontyp.getColnum() - custom_column_start] = 1;
                    }
                }
                for (int i = 1; i <= Custom_Column_limit; i++) {
                    if (countchk[i] == 0) {
                        colcount = i;
                        break;
                    }
                }
            }
            requestParams.put("response", jobj);
            requestParams.put("column_number", colcount + custom_column_start);
            requestParams.put("success", Notreachedlimit ? "True" : "false");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestParams;
    }
    
    public KwlReturnObject getAccountUsedInExpenesePo(String accountid, String companyid) throws ServiceException {
        List list = new ArrayList();
        String q = "from ExpensePODetail where account.ID=? and company.companyID=?";
        list = executeQuery( q, new Object[]{accountid, companyid});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getChildAccountfromAccount(String accountid, String companyid) throws ServiceException {
        String query = "select * from account where parent = ? and company = ? ";
        ArrayList params = new ArrayList();
        params.add(accountid);
        params.add(companyid);
        List list = executeSQLQuery(query, params.toArray());
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    
    //This method used to modify the Custom Date long value to browser specific timezone added long value.
    public String getBrowserSpecificCustomDateLongValue(String longdateval, String browsertz){
        String colData = "";
        DateFormat df = null;
        Date customDate = null;
        try {
            customDate = new java.util.Date(Long.parseLong(longdateval));
            df = new SimpleDateFormat();
            String dateStr = df.format(customDate);
            df.setTimeZone(TimeZone.getTimeZone("GMT" + browsertz));
            customDate = df.parse(dateStr);
            colData = String.valueOf(customDate.getTime());
        } catch (Exception ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return colData;
    }
    @Override
    public int updateIndianTermsProductLevel(String termid, HashMap<String, Object> hm) throws ServiceException {
        int numRows = 0;
        String updateString = "";

        try {
            if (!StringUtil.isNullOrEmpty(termid) && hm.containsKey("accountid") && hm.containsKey("taxtype")) {
                int termtype = (Integer) hm.get("taxtype");
                if (hm.containsKey("percent")) {
                    if (termtype == 1) {
                      updateString = "percentage='"+hm.get("percent")+"',termamount='0',";
                    }
                    
                }
                if (hm.containsKey("termamount")) {
                    if (termtype == 0) {
                        updateString="termamount='"+hm.get("termamount")+"',percentage='"+hm.get("termamount")+"',";
                    }
                }
                String query = "update producttermsmap set "+updateString+" account=?,taxtype=? where term = ?";
                numRows = executeSQLUpdate(query, new Object[]{hm.get("accountid"), hm.get("taxtype"), termid});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return numRows;
    }
    @Override
    public boolean checkTermusedInTransaction(String termid, boolean isSalesOrPurchase) throws ServiceException {

        KwlReturnObject result = null;
        boolean isTermUsed = false;
        if (isSalesOrPurchase) {

            // check term is used in any Sales Invoices
            result = findTermUsedInTransaction(termid);
            if (result.getRecordTotalCount() > 0) {
                return true;
            }
            // check term is used in any Customer Quotation
            result = findTermUsedInQuotation(termid);
            if (result.getRecordTotalCount() > 0) {
                return true;
            }
            // check term is used in any SO
            result = findTermUsedInSO(termid);
            if (result.getRecordTotalCount() > 0) {
                return true;
            }

        } else {
            // check term is used in any Purchase Invoices
            result = findTermUsedInPI(termid);
            if (result.getRecordTotalCount() > 0) {
                return true;
            }
            // check term is used in any Vendor Quotation
            result = findTermUsedInVQ(termid);
            if (result.getRecordTotalCount() > 0) {
                return true;
            }
            // check term is used in any SO
            result = findTermUsedInPO(termid);
            if (result.getRecordTotalCount() > 0) {
                return true;
            }
        }

        // check term is used in any DO
        result = findTermUsedInDO(termid);
        if (result.getRecordTotalCount() > 0) {
            return true;
        }
        // check term is used in any GRO
        result = findTermUsedInGRO(termid);
        if (result.getRecordTotalCount() > 0) {
            return true;
        }
        return isTermUsed;
    }
    
    /*
     * Getting the UOB receiving details
     */
    @Override
    public KwlReturnObject getUOBReceivingBankDetails(HashMap requestParams) throws ServiceException {
        String condition = "";
        ArrayList params = new ArrayList();
        params.add((String) requestParams.get("companyId"));

        if (requestParams.containsKey("customer") && requestParams.get("customer") != null) {
            params.add((String) requestParams.get("customer"));
            condition += " and rbd.customer.ID=? ";
        }

        if (requestParams.containsKey("customerBankAccountType") && requestParams.get("customerBankAccountType") != null) {
            params.add((String) requestParams.get("customerBankAccountType"));
            condition += " and rbd.customerBankAccountType.ID=? ";
        }
        if (requestParams.containsKey("activated") && requestParams.get("activated") != null) {
            params.add((Boolean) requestParams.get("activated"));
            condition += " and rbd.activated = ? ";
        }
        
        String query = "From UOBReceivingDetails rbd where rbd.company.companyID=? " + condition;

        List list = executeQuery( query, params.toArray());

        KwlReturnObject result = new KwlReturnObject(true, null, null, list, list.size());

        return result;
    }
    /*
     * Method for saving the coa level data for UOB bank
     */
    @Override
    public KwlReturnObject saveOrupdateUOBBankDetail(HashMap<String, Object> ibgBankDetailParams) throws ServiceException {
        List list = new ArrayList();
        try {
            UOBBankDetails bankDetail = null;
            String detailID = (String) ibgBankDetailParams.get(Constants.UOB_BANK_DETAIL_ID);
            if (!StringUtil.isNullOrEmpty(detailID)) {
                bankDetail = (UOBBankDetails) get(UOBBankDetails.class, detailID);
            } else {
                bankDetail = new UOBBankDetails();
            }
            
            if (ibgBankDetailParams.containsKey(Constants.UOB_Originating_BIC_Code)) {
                bankDetail.setOriginatingBICCode((String) ibgBankDetailParams.get(Constants.UOB_Originating_BIC_Code));
            }

            if (ibgBankDetailParams.containsKey(Constants.UOB_Currency_Code)) {
                bankDetail.setCurrencyCode((String) ibgBankDetailParams.get(Constants.UOB_Currency_Code));
            }
            
            if (ibgBankDetailParams.containsKey(Constants.UOB_Originating_Account_Number)) {
                bankDetail.setOriginatingAccountNumber((String) ibgBankDetailParams.get(Constants.UOB_Originating_Account_Number));
            }
            
            if (ibgBankDetailParams.containsKey(Constants.UOB_Originating_Account_Name)) {
                bankDetail.setOriginatingAccountName((String) ibgBankDetailParams.get(Constants.UOB_Originating_Account_Name));
            }
            
            if (ibgBankDetailParams.containsKey(Constants.UOB_Ultimate_Originating_Customer)) {
                bankDetail.setUltimateOriginatingCustomer((String) ibgBankDetailParams.get(Constants.UOB_Ultimate_Originating_Customer));
            }
            if (ibgBankDetailParams.containsKey(Constants.UOB_CompanyID) && ibgBankDetailParams.get(Constants.UOB_CompanyID) != null) {
                bankDetail.setUOBCompanyID((String) ibgBankDetailParams.get(Constants.UOB_CompanyID));
            }
            
            if (ibgBankDetailParams.containsKey(Constants.Acc_Accountid)) {
                Account account = (Account) get(Account.class, (String) ibgBankDetailParams.get(Constants.Acc_Accountid));
                bankDetail.setAccount(account);
            }
            
            if (ibgBankDetailParams.containsKey(Constants.companyid)) {
                Company company = (Company) get(Company.class, (String) ibgBankDetailParams.get(Constants.companyid));
                bankDetail.setCompany(company);
            }
            
            saveOrUpdate(bankDetail);

            list.add(bankDetail);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    /*
     * Method for getting UOB details associated with some account
     */
    @Override
    public KwlReturnObject getUOBDetailsForAccount(String accountID, String companyID) throws ServiceException {
        String query = " from UOBBankDetails where account.ID = ? and company.companyID = ? ";
        List list = executeQuery( query, new Object[]{accountID, companyID});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public KwlReturnObject saveOrupdateOCBCBankDetail(HashMap<String, Object> ibgBankDetailParams) throws ServiceException {
        List list = new ArrayList();
        try {
            OCBCBankDetails bankDetails = new OCBCBankDetails();
            String bankDetailsId = (String) ibgBankDetailParams.get(Constants.OCBC_BANK_DETAIL_ID);
            if (!StringUtil.isNullOrEmpty(bankDetailsId)) {
                bankDetails = (OCBCBankDetails) get(OCBCBankDetails.class, bankDetailsId);
            }
            if (ibgBankDetailParams.containsKey(Constants.OCBC_AccountNumber) && ibgBankDetailParams.get(Constants.OCBC_AccountNumber) != null) {
                bankDetails.setAccountNumber((String) ibgBankDetailParams.get(Constants.OCBC_AccountNumber));
            }
            if (ibgBankDetailParams.containsKey(Constants.OCBC_OriginatingBankCode) && ibgBankDetailParams.get(Constants.OCBC_OriginatingBankCode) != null) {
                bankDetails.setOriginatingBankCode((String) ibgBankDetailParams.get(Constants.OCBC_OriginatingBankCode));
            }
            if (ibgBankDetailParams.containsKey(Constants.OCBC_ReferenceNumber) && ibgBankDetailParams.get(Constants.OCBC_ReferenceNumber) != null) {
                bankDetails.setReferenceNumber((String) ibgBankDetailParams.get(Constants.OCBC_ReferenceNumber));
            }
            if (ibgBankDetailParams.containsKey(Constants.Acc_Accountid) && ibgBankDetailParams.get(Constants.Acc_Accountid) != null) {
                Account account = (Account) get(Account.class, (String) ibgBankDetailParams.get(Constants.Acc_Accountid));
                bankDetails.setAccount(account);
            }
            if (ibgBankDetailParams.containsKey(Constants.companyKey) && ibgBankDetailParams.get(Constants.companyKey) != null) {
                Company company = (Company) get(Company.class, (String) ibgBankDetailParams.get(Constants.companyKey));
                bankDetails.setCompany(company);
            }
            saveOrUpdate(bankDetails);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDAOImpl.saveOrupdateOCBCBankDetail:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, list.size());
    }

    @Override
    public KwlReturnObject getOCBCBankDetailsForAccount(String accountId, String companyId) throws ServiceException {
        String query = " from OCBCBankDetails where account.ID = ? and company.companyID = ? ";
        List list = executeQuery(query, new Object[]{accountId, companyId});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
 @Override   
    public HashMap validaterecorsingledHB(String moduleid, String recordid, String companyid) throws ServiceException, JSONException {
        JSONObject returnObj = new JSONObject();
        boolean successFlag = false;
        List<String> columnList = new ArrayList<String>();
        HashMap<String, Object> returnMap = new HashMap<String, Object>();

        try {
            String mainTable = null;
            String tableid = "id";
            ArrayList params = new ArrayList();

            if (moduleid.equalsIgnoreCase(Constants.Vendor_MODULE_UUID)) {
                mainTable = "vendor";
            } else if (moduleid.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID)) {
                mainTable = "customer";
            } else if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Invoice_ModuleId))) {
                mainTable = "invoice";
            } else if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                mainTable = "payment";
            }
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select ");

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put(Constants.filter_names, Arrays.asList("module", "ismandatory"));
            map.put(Constants.filter_values, Arrays.asList(moduleid, 'T'));
            map.put("order_by", Arrays.asList("defaultHeader"));
            map.put("order_type", Arrays.asList("asc"));
            KwlReturnObject dhEmptyRefModule = getDefaultHeaders(map);
            if (dhEmptyRefModule != null) {
                List<DefaultHeader> dhList = dhEmptyRefModule.getEntityList();
                for (DefaultHeader defaultHeaderObj : dhList) {
                    String dbcolumnname = defaultHeaderObj.getDbcolumnname();
                    columnList.add(dbcolumnname);

                    if (!StringUtil.isNullOrEmpty(dbcolumnname)) {
                        queryBuilder.append(dbcolumnname);
                    }
                }
                queryBuilder.append(" ");
            }
            queryBuilder.append("from " + mainTable + "  where company=?");
            params.add(companyid);
            if (!StringUtil.isNullOrEmpty(recordid)) {
                queryBuilder.append("and " + tableid + "=? ");
                params.add(recordid);
            }
            String query = queryBuilder.toString();
            List newlist = executeSQLQuery(query, params.toArray());
            Object[] objArray = (Object[]) newlist.get(0);

            for (int i = 0; i < columnList.size(); i++) {
                String value = objArray[i] != null ? (String) objArray[i].toString() : "";
                if (StringUtil.isNullOrEmpty(value)) {
                    returnMap.put(columnList.get(i), value);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            returnObj.put(Constants.RES_success, successFlag);
        }
        return returnMap;
    }
    
  @Override  
    public KwlReturnObject getJsonKeyMapping(String defaultHeaderid) throws ServiceException {
        String query = "select jsonmappingkey from default_header_rest where defaultheaderid=?";
        ArrayList params = new ArrayList();
        params.add(defaultHeaderid);
        List list = executeSQLQuery(query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    /*
     * Method for getting Accounts mapped to Customer or vendor
     */
    @Override
    public KwlReturnObject getAccountsMappedToCustomerVendor(String companyid, boolean isCustomer) throws ServiceException {
        String query = "";
        if (isCustomer) {
            query = "select distinct c.account from Customer as c where c.company.companyID = ? ";
        } else {
            query = "select distinct v.account from Vendor as v where v.company.companyID = ? ";
        }
        List list = executeQuery(query, new Object[]{companyid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    public int updateAccountDefaultGroup(HashMap<String, Object> requestParams) throws ServiceException {
        try {
            ArrayList params = new ArrayList();
            String query="";
            String oldGroupId = requestParams.get("oldgroupid")!=null?requestParams.get("oldgroupid").toString():"";
            String newGroupId = requestParams.get("newgroupid")!=null?requestParams.get("newgroupid").toString():"";
            String companyid = requestParams.get("companyid")!=null?requestParams.get("companyid").toString():"";
            int numRows=0;
            if(!StringUtil.isNullOrEmpty(oldGroupId) && !StringUtil.isNullOrEmpty(newGroupId) && !StringUtil.isNullOrEmpty(companyid)){
                query = " update account acc set acc.groupname=? where acc.company=? and acc.groupname=?";
                params.add(newGroupId);
                params.add(companyid);
                params.add(oldGroupId);
                numRows = executeSQLUpdate( query, params.toArray());
}
            return numRows;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Cannot update the default group ", ex);
        }
    }     
    
    @Override
    public KwlReturnObject getLineLevelTermsfromAccount(String accountid, String companyid) throws ServiceException {
        String query = " from LineLevelTerms where company.companyID = ? and (account.ID = ?  or creditNotAvailedAccount.ID = ? or payableAccount.ID = ?)";
        List list = executeQuery(query, new Object[]{companyid, accountid, accountid, accountid});
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    @Override
    public KwlReturnObject getLineLevelTerms(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from LineLevelTerms ";
        return buildNExecuteQuery(query, requestParams);
    }
    @Override
    public KwlReturnObject getAddressFieldDimensionMapping(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from AddressFieldDimensionMapping ";
        return buildNExecuteQuery(query, requestParams);
    }
    
    
    public KwlReturnObject getGSTTermFromName(String companyId, String term , boolean salesOrPurchase) throws ServiceException {
        String query = " select llterm from LineLevelTerms llterm where llterm.company.companyID =  ? and llterm.term =  ? and llterm.deleted = 0 and llterm.salesOrPurchase = ? ";
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(term);
        /**
         * ERP-41436 Added SalesorPurchase column to check Sales/ Purchase side
         * term present or not. Problem : CESS term need to create for Sales and
         * Purchase but if this flag not added(SalesorPurchase), because of this
         * Sales side CESS term created and then Same term override for Purchase
         * side
         */
        params.add(salesOrPurchase);
        List list = executeQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    @Override
    public void copyDefaultIndiaGSTTermsOnMigration(HashMap<String, Object> defaultCompSetupMap) throws ServiceException {

        /*
         * Variable declaration
         */
        String companyid = "";
        try {

            if (defaultCompSetupMap.containsKey(Constants.companyid) && defaultCompSetupMap.get(Constants.companyid) != null) {
                companyid = (String) defaultCompSetupMap.get(Constants.companyid);
            }
            String countryid="";
            String countryid_Query ="";
            if (defaultCompSetupMap.containsKey(Constants.COUNTRY_ID) && defaultCompSetupMap.get(Constants.COUNTRY_ID) != null) {
                countryid = (String) defaultCompSetupMap.get(Constants.COUNTRY_ID);
            }
            if(!StringUtil.isNullOrEmpty(countryid)){
                countryid_Query= " and country.ID ='" + countryid + "' "; 
            }
            String defaultquery = "from DefaultTerms where termtype=7 " + countryid_Query;
            List<DefaultTerms> defaultlist = executeQuery(defaultquery);

            if (defaultlist != null && !defaultlist.isEmpty()) {
                for (DefaultTerms Defterm : defaultlist) {
                    /**
                     * ERP-41436
                     * Added SalesorPurchase column to check Sales/ Purchase side term present or not.
                     * Problem  : CESS term need to create for Sales and Purchase but if this flag not added(SalesorPurchase), 
                     * because of this Sales side CESS term created and then Same term override for Purchase side
                     */
                    KwlReturnObject chkAccount = getGSTTermFromName(companyid, Defterm.getTerm(), Defterm.isSalesOrPurchase());
                    if (chkAccount.getEntityList().size() == 0) {
                        Company company = (Company) get(Company.class, companyid);
                        LineLevelTerms invoiceTerm = new LineLevelTerms();
                        invoiceTerm.setCompany(company);
                        invoiceTerm.setCreator(company.getCreator());
                        invoiceTerm.setTerm(Defterm.getTerm());
                        invoiceTerm.setFormula(Defterm.getFormula());
                        invoiceTerm.setSign(Defterm.getSign());
                        invoiceTerm.setSalesOrPurchase(Defterm.isSalesOrPurchase());
                        invoiceTerm.setDeleted(0);
                        invoiceTerm.setPercentage(Defterm.getPercentage());
                        invoiceTerm.setTermType(Defterm.getTermtype());
                        invoiceTerm.setTermSequence(Defterm.getTermSequence());
                        invoiceTerm.setTaxType(Defterm.getTaxType());
                        invoiceTerm.setIsDefault(Defterm.isIsDefault());
                        ArrayList accountParams = new ArrayList();
                        accountParams.add(companyid);
                        accountParams.add(Defterm.getAccountname());
                        String termMapeAccountQuery = "from Account where company.companyID=? and name = ?";
                        List accountForTermMap = executeQuery(termMapeAccountQuery, accountParams.toArray());
                        if (!accountForTermMap.isEmpty()) {
                            Object accObject = accountForTermMap.get(0);
                            Account acc = (Account) accObject;
                            invoiceTerm.setAccount(acc);
                        }
                        accountParams.clear();
                        accountParams.add(companyid);
                        accountParams.add(Defterm.getCreditNotAvailedAccountName());
                        accountForTermMap = executeQuery(termMapeAccountQuery, accountParams.toArray());
                        if (!accountForTermMap.isEmpty()) {
                            Object accObject = accountForTermMap.get(0);
                            Account acc = (Account) accObject;
                            invoiceTerm.setCreditNotAvailedAccount(acc);
                        }
                    /*
                     * Mapping AdvancPayableAccount with terms
                     * 
                     */
                        accountParams.clear();
                        accountParams.add(companyid);
                        accountParams.add(Defterm.getAdvancPayableAccountName());
                        accountForTermMap = executeQuery(termMapeAccountQuery, accountParams.toArray());
                        if (!accountForTermMap.isEmpty()) {
                            Object accObject = accountForTermMap.get(0);
                            Account acc = (Account) accObject;
                            invoiceTerm.setPayableAccount(acc);
                        }
                        save(invoiceTerm);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        }
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void copyIndiaGSTTermsOnMigration(HashMap<String, Object> defaultCompSetupMap) throws ServiceException {
       
          String companyid = "";
            String currencyid = "";
        try{
             if (defaultCompSetupMap.containsKey(Constants.companyid) && defaultCompSetupMap.get(Constants.companyid) != null) {
                companyid = (String) defaultCompSetupMap.get(Constants.companyid);
            }
               if (defaultCompSetupMap.containsKey("currencyid") && defaultCompSetupMap.get("currencyid") != null) {
                currencyid = (String) defaultCompSetupMap.get("currencyid");
            }
             
        KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, currencyid);
            Company company = (Company) get(Company.class, companyid);
            String query = "from DefaultAccount where creationdate = '2017-07-01' and country='105'";
            List<DefaultAccount> list = null;
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date newUserDate = sdf.parse("2017-07-01");//Server's new Date();
            list = executeQuery(query);

            for (DefaultAccount defaultAccount : list) {
                KwlReturnObject chkAccount = getAccountFromName(companyid, defaultAccount.getName());
                if (chkAccount.getEntityList().size() == 0) {
                    Account account = new Account();
                    account.setCompany(company);
                    account.setDeleted(false);
                    account.setActivate(true);
                    Group group = null;
                    if (defaultAccount.getGroup() != null) {
                        group = (Group) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.Group", defaultAccount.getGroup().getID());
                    }
                    //0 - Asset, 1 - Liability, 2 - Expense, 3 - Income This is Account Type
                    //1 - GL, 2 - Cash, 3 - Bank, 4 - GST Tax This is Account Master type Type
                    int masterType = 1;
                    int accountType = 0;
                    int natureVal = 0;
                    String accountTypeName = "";
                    if (group != null) {
                        natureVal = group.getNature();
                        if (!StringUtil.isNullOrEmpty(group.getName())) {
                            accountTypeName = group.getName();
                        }
                    }

//                String accountTypeName = defaultAccount.getGroup().getName();
                    if (natureVal == 0 || natureVal == 1) {
                        accountType = Group.ACCOUNTTYPE_GL;
                    }
                    try {
                        masterType = defaultAccount.getMastertypevalue();
                    } catch (Exception e) {
                        if (accountTypeName.equalsIgnoreCase("Cash")) {
                            masterType = Group.ACCOUNTTYPE_CASH;
                        } else if (accountTypeName.equalsIgnoreCase("Bank")) {
                            masterType = Group.ACCOUNTTYPE_BANK;
                        }
                        if (!StringUtil.isNullOrEmpty(company.getCountry().getID()) && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id && Constants.TDSDefaultChartOfAccountsINDIA.contains(defaultAccount.getName())) {
                            masterType = Group.ACCOUNTTYPE_GST;
                        }
                    }

                    account.setGroup(group);
                    account.setName(defaultAccount.getName());
                    account.setCurrency(currency);
                    account.setAccounttype(accountType);
                    account.setMastertypevalue(masterType);
                    //For account Table, creation date is saved according to user's timezone diff.
                    account.setCreationDate(newUserDate);
                    account.setPresentValue(defaultAccount.getPresentValue());
                    account.setOpeningBalance(defaultAccount.getOpeningBalance());
                    if (!StringUtil.isNullOrEmpty(company.getCountry().getID()) && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id) {
                        account.setDefaultaccountID(defaultAccount.getID());
                    }
                    save(account);
               
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyIndiaGSTDefaultAccounts : " + ex.getMessage(), ex);
        }
      
        try {

           
            String defaultquery = "from DefaultTerms where termtype=7 and country.ID = '105'";
            List<DefaultTerms> defaultlist = executeQuery(defaultquery);

            if (defaultlist != null && !defaultlist.isEmpty()) {
                for (DefaultTerms Defterm : defaultlist) {
                    /**
                     * ERP-41436
                     * Added SalesorPurchase column to check Sales/ Purchase side term present or not.
                     * Problem  : CESS term need to create for Sales and Purchase but if this flag not added(SalesorPurchase), 
                     * because of this Sales side CESS term created and then Same term override for Purchase side
                     */
                    KwlReturnObject chkAccount = getGSTTermFromName(companyid, Defterm.getTerm(), Defterm.isSalesOrPurchase());
                    if (chkAccount.getEntityList().size() == 0) {
                        Company company = (Company) get(Company.class, companyid);
                        LineLevelTerms invoiceTerm = new LineLevelTerms();
                        invoiceTerm.setCompany(company);
                        invoiceTerm.setCreator(company.getCreator());
                        invoiceTerm.setTerm(Defterm.getTerm());
                        invoiceTerm.setFormula(Defterm.getFormula());
                        invoiceTerm.setSign(Defterm.getSign());
                        invoiceTerm.setSalesOrPurchase(Defterm.isSalesOrPurchase());
                        invoiceTerm.setDeleted(0);
                        invoiceTerm.setPercentage(Defterm.getPercentage());
                        invoiceTerm.setTermType(Defterm.getTermtype());
                        invoiceTerm.setTermSequence(Defterm.getTermSequence());
                        invoiceTerm.setTaxType(Defterm.getTaxType());
                        invoiceTerm.setIsDefault(Defterm.isIsDefault());
                        ArrayList accountParams = new ArrayList();
                        accountParams.add(companyid);
                        accountParams.add(Defterm.getAccountname());
                        String termMapeAccountQuery = "from Account where company.companyID=? and name = ?";
                        List accountForTermMap = executeQuery(termMapeAccountQuery, accountParams.toArray());
                        if (!accountForTermMap.isEmpty()) {
                            Object accObject = accountForTermMap.get(0);
                            Account acc = (Account) accObject;
                            invoiceTerm.setAccount(acc);
                        }
                        accountParams.clear();
                        accountParams.add(companyid);
                        accountParams.add(Defterm.getCreditNotAvailedAccountName());
                        accountForTermMap = executeQuery(termMapeAccountQuery, accountParams.toArray());
                        if (!accountForTermMap.isEmpty()) {
                            Object accObject = accountForTermMap.get(0);
                            Account acc = (Account) accObject;
                            invoiceTerm.setCreditNotAvailedAccount(acc);
                        }
                        /*
                         * Mapping AdvancPayableAccount with terms
                         *
                         */
                        accountParams.clear();
                        accountParams.add(companyid);
                        accountParams.add(Defterm.getAdvancPayableAccountName());
                        accountForTermMap = executeQuery(termMapeAccountQuery, accountParams.toArray());
                        if (!accountForTermMap.isEmpty()) {
                            Object accObject = accountForTermMap.get(0);
                            Account acc = (Account) accObject;
                            invoiceTerm.setPayableAccount(acc);
                        }
                        save(invoiceTerm);
                    }
                    /*
                     * If terms are already coppied
                     * ERP-41436
                     * Added SalesorPurchase column to check Sales/ Purchase side term present or not.
                     * Problem  : CESS term need to create for Sales and Purchase but if this flag not added(SalesorPurchase), 
                     * because of this Sales side CESS term created and then Same term override for Purchase side
                     */
                    KwlReturnObject chkAccount1 = getGSTTermFromName(companyid, Defterm.getTerm() , Defterm.isSalesOrPurchase());

                    if (chkAccount1.getEntityList().size() != 0) {

                        ArrayList<LineLevelTerms> list1 = (ArrayList<LineLevelTerms>) chkAccount1.getEntityList();
                        for (LineLevelTerms lineLevelTerms : list1) {
                            boolean isUpdate =false;
                            if (lineLevelTerms.getPayableAccount() == null) {
                                ArrayList accountParams = new ArrayList();
                                accountParams.clear();
                                accountParams.add(companyid);
                                accountParams.add(Defterm.getAdvancPayableAccountName());
                                List accountForTermMap = null;
                                String termMapeAccountQuery = "from Account where company.companyID=? and name = ?";
                                accountForTermMap = executeQuery(termMapeAccountQuery, accountParams.toArray());
                                if (!accountForTermMap.isEmpty()) {
                                    Object accObject = accountForTermMap.get(0);
                                    Account acc = (Account) accObject;
                                    lineLevelTerms.setPayableAccount(acc);
                                    isUpdate =true;
                                }
                            }

                            if (lineLevelTerms.getCreditNotAvailedAccount() == null) {
                                ArrayList accountParams = new ArrayList();
                                accountParams.clear();
                                accountParams.add(companyid);
                                accountParams.add(Defterm.getCreditNotAvailedAccountName());
                                List accountForTermMap = null;
                                String termMapeAccountQuery = "from Account where company.companyID=? and name = ?";
                                accountForTermMap = executeQuery(termMapeAccountQuery, accountParams.toArray());
                                if (!accountForTermMap.isEmpty()) {
                                    Object accObject = accountForTermMap.get(0);
                                    Account acc = (Account) accObject;
                                    lineLevelTerms.setCreditNotAvailedAccount(acc);
                                    isUpdate =true;
                                }
                            }
                            if (isUpdate) {
                                saveOrUpdate(lineLevelTerms);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("Gives error while fetching InvoiceTerms.", ex);//+ex.getMessage(), ex);
        }
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public KwlReturnObject copyIndiaGSTDefaultAccounts(String companyid, String currencyid, Map<String, Object> defaultCompSetupMap) throws ServiceException {
        List returnlist = new ArrayList();
        HashMap hm = new HashMap();
        try {
//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, StorageHandler.getDefaultCurrencyID());

            String countryid="";
            String countryid_Query ="";
            if (defaultCompSetupMap.containsKey(Constants.COUNTRY_ID) && defaultCompSetupMap.get(Constants.COUNTRY_ID) != null) {
                countryid = (String) defaultCompSetupMap.get(Constants.COUNTRY_ID);
            }
            if(!StringUtil.isNullOrEmpty(countryid)){
                countryid_Query= " and country='" + countryid + "' "; 
            }
            KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, currencyid);
            Company company = (Company) get(Company.class, companyid);
//            String query = "from DefaultAccount where creationdate = '2017-07-01' and country='105'";
            String query = "from DefaultAccount where creationdate = '2017-07-01' " + countryid_Query;
            List<DefaultAccount> list = null;
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date newUserDate = sdf.parse("2017-07-01");//Server's new Date();
            list = executeQuery(query);

            for (DefaultAccount defaultAccount : list) {
                KwlReturnObject chkAccount = getAccountFromName(companyid, defaultAccount.getName());
                if (chkAccount.getEntityList().size() == 0) {
                    Account account = new Account();
                    account.setCompany(company);
                    account.setDeleted(false);
                    account.setActivate(true);
                    Group group = null;
                    if (defaultAccount.getGroup() != null) {
                        group = (Group) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.Group", defaultAccount.getGroup().getID());
                    }
                    //0 - Asset, 1 - Liability, 2 - Expense, 3 - Income This is Account Type
                    //1 - GL, 2 - Cash, 3 - Bank, 4 - GST Tax This is Account Master type Type
                    int masterType = 1;
                    int accountType = 0;
                    int natureVal = 0;
                    String accountTypeName = "";
                    if (group != null) {
                        natureVal = group.getNature();
                        if (!StringUtil.isNullOrEmpty(group.getName())) {
                            accountTypeName = group.getName();
                        }
                    }

//                String accountTypeName = defaultAccount.getGroup().getName();
                    if (natureVal == 0 || natureVal == 1) {
                        accountType = Group.ACCOUNTTYPE_GL;
                    }
                    try {
                        masterType = defaultAccount.getMastertypevalue();
                    } catch (Exception e) {
                        if (accountTypeName.equalsIgnoreCase("Cash")) {
                            masterType = Group.ACCOUNTTYPE_CASH;
                        } else if (accountTypeName.equalsIgnoreCase("Bank")) {
                            masterType = Group.ACCOUNTTYPE_BANK;
                        }
                        if (!StringUtil.isNullOrEmpty(company.getCountry().getID()) && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id && Constants.TDSDefaultChartOfAccountsINDIA.contains(defaultAccount.getName())) {
                            masterType = Group.ACCOUNTTYPE_GST;
                        }
                    }

                    account.setGroup(group);
                    account.setName(defaultAccount.getName());
                    account.setCurrency(currency);
                    account.setAccounttype(accountType);
                    account.setMastertypevalue(masterType);
                    //For account Table, creation date is saved according to user's timezone diff.
                    account.setCreationDate(newUserDate);
                    account.setPresentValue(defaultAccount.getPresentValue());
                    account.setOpeningBalance(defaultAccount.getOpeningBalance());
                    if (!StringUtil.isNullOrEmpty(company.getCountry().getID()) && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id) {
                        account.setDefaultaccountID(defaultAccount.getID());
                    }
                    save(account);
                    hm.put(defaultAccount.getID(), account.getID());
                    hm.putAll(saveChildren(account, defaultAccount));
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyIndiaGSTDefaultAccounts : " + ex.getMessage(), ex);
        }
        returnlist.add(hm);
        return new KwlReturnObject(true, "", null, returnlist, returnlist.size());
    }
    @Override
    public void addMasterItemsToProductTaxClass(HashMap<String, Object> defaultCompSetupMap, JSONObject setUpData) throws ServiceException {
        ArrayList<String> defaultValues = new ArrayList<>();
        String companyid = "";
        boolean successFlag = false;
        if (defaultCompSetupMap.containsKey(Constants.companyid) && defaultCompSetupMap.get(Constants.companyid) != null) {
            companyid = (String) defaultCompSetupMap.get(Constants.companyid);
        }

        defaultValues.add("Product @ 0%");
        defaultValues.add("Product @ 5%");
        defaultValues.add("Product @ 12%");
        defaultValues.add("Product @ 18%");
        defaultValues.add("Product @ 28%");
        defaultValues.add(FieldComboData.TaxClass_Exempted);
        defaultValues.add(FieldComboData.TaxClass_Non_GST_Product);
        List list = null;
        HashMap<String, Object> duplicateCheckParams = new HashMap<>();
        duplicateCheckParams.put("filter_names", Arrays.asList("companyid", "fieldlabel"));
        duplicateCheckParams.put("filter_values", Arrays.asList(companyid, Constants.GSTProdCategory));
        KwlReturnObject duplicate = getFieldParams(duplicateCheckParams);
        list = duplicate.getEntityList();
        FieldParams fp = null;
        if (!list.isEmpty()) {
            for (int count = 0; count < list.size(); count++) {

                fp = (FieldParams) list.get(count);
                for (String defaultValueNew : defaultValues) {
                    HashMap<String, Object> comborequestParams = new HashMap<>();
                    comborequestParams.put("Fieldid", fp.getId());
                    comborequestParams.put("Value", defaultValueNew);
                    comborequestParams.put("Activatedeactivatedimensionvalue", true);//By default Dimension value will be activated
                    /**
                     * Code for insert Value type for any dimension value
                     * Ex :  Exempt Type of Tax Class India = 1
                     */
                    if (defaultValueNew.equalsIgnoreCase(FieldComboData.TaxClass_Exempted)) {
                        comborequestParams.put("ValueType", 1);
                    }else if (defaultValueNew.equalsIgnoreCase(FieldComboData.TaxClass_Non_GST_Product)) {
                        comborequestParams.put("ValueType", 2);
                    } else if (defaultValueNew.equalsIgnoreCase(FieldComboData.TaxClass_ZeroPercenatge)) {
                        comborequestParams.put("ValueType", 3);
                    }
                    String Defaultcombodata = "";
                    KwlReturnObject resultkmsg = getfieldcombodata(comborequestParams);

                    if (resultkmsg.getEntityList().isEmpty()) {
                        KwlReturnObject kmsg1 = insertfieldcombodata(comborequestParams);

                    }
                    successFlag = true;// true when custom field created successfully
                }
            }
        }
    }
    /**
     * Get GST Configuration : 1) Master Data : FiledParams, FieldComboData.
     * @param paramObj
     * @return 
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public KwlReturnObject getMasterDataForGSTFields(JSONObject paramObj) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String companyid = "";
            String query = "";
            String condition = "";
            ArrayList params = new ArrayList();
            if (paramObj.has(Constants.companyid)) {
                companyid = paramObj.optString(Constants.companyid, "");
            }
            Company company = (Company) get(Company.class, companyid);
            /**
             * For INDIAN Company : Entity,State
             * For US Company : Entity,State,City,County.
             */
            if (Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id) {
                condition = " fp.fieldlabel in (?, ?, ?)";
                params.add(Constants.GST_ENTITY_KEY);
                params.add(Constants.GST_ADDRESS_STATE_KEY);
                params.add(Constants.HSN_SACCODE);
            } else if (Integer.parseInt(company.getCountry().getID()) == Constants.USA_country_id) {
                condition = " fp.fieldlabel in (?, ?, ?, ?)";
                params.add(Constants.GST_ENTITY_KEY);
                params.add(Constants.GST_ADDRESS_STATE_KEY);
                params.add(Constants.GST_ADDRESS_CITY_KEY);
                params.add(Constants.GST_ADDRESS_COUNTY_KEY); 
            } else {
                throw new Exception(" Please Check Company ");
            }
            query = " from FieldParams fp WHERE ((("+ condition +") and fp.moduleid = ?) or (fieldlabel = 'Product Tax Class' and fp.moduleid = ?)) and fp.company.companyID = ? ";
            params.add(Constants.GSTModule);
            params.add(Constants.Acc_Product_Master_ModuleId);
            params.add(companyid);
            if (!StringUtil.isNullOrEmpty(query)) {
                list = executeQuery(query, params.toArray());
                count = list.size();
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getMasterDataForGSTFields : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getMasterDataForGSTFields : " + ex.getMessage(), ex);
        }
        
        return new KwlReturnObject(true, "", "", list, count);
    }
    
    @Override
    public void copyTDSPayableAccountAndMapToMasterItemsNOP(HashMap<String, Object> defaultCompSetupMap) throws ServiceException {
        String companyid = "";
        String currencyid = "";
        try{
            if (defaultCompSetupMap.containsKey(Constants.companyid) && defaultCompSetupMap.get(Constants.companyid) != null) {
                companyid = (String) defaultCompSetupMap.get(Constants.companyid);
            }
            if (defaultCompSetupMap.containsKey("currencyid") && defaultCompSetupMap.get("currencyid") != null) {
                currencyid = (String) defaultCompSetupMap.get("currencyid");
            }
            Company company = (Company) get(Company.class, companyid);
            KWLCurrency currency = (KWLCurrency) get(KWLCurrency.class, currencyid);
            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date newUserDate = sdf.parse("2017-07-01");//Server's new Date();
            HashMap hmAcc = new HashMap();
            
            // Query to get TDS payable account that is account with account name containing value as 'TDS Payable'
            String query = "from DefaultAccount where name like'%TDS Payable%'";
            List<DefaultAccount> list = executeQuery(query);
            for (DefaultAccount defaultAccount : list) {
                KwlReturnObject chkAccount = getAccountFromName(companyid, defaultAccount.getName());
                if (chkAccount.getEntityList().size() == 0) {
                    Account account = new Account();
                    account.setCompany(company);
                    account.setDeleted(false);
                    account.setActivate(true);
                    Group group = null;
                    if (defaultAccount.getGroup() != null) {
                        group = (Group) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.Group", defaultAccount.getGroup().getID());
                    }
                    //0 - Asset, 1 - Liability, 2 - Expense, 3 - Income This is Account Type
                    //1 - GL, 2 - Cash, 3 - Bank, 4 - GST Tax This is Account Master type Type
                    int masterType = 1;
                    int accountType = 0;
                    int natureVal = 0;
                    String accountTypeName = "";
                    if (group != null) {
                        natureVal = group.getNature();
                        if (!StringUtil.isNullOrEmpty(group.getName())) {
                            accountTypeName = group.getName();
                        }
                    }

//                String accountTypeName = defaultAccount.getGroup().getName();
                    if (natureVal == 0 || natureVal == 1) {
                        accountType = Group.ACCOUNTTYPE_GL;
                    }
                    try {
                        masterType = defaultAccount.getMastertypevalue();
                    } catch (Exception e) {
                        if (accountTypeName.equalsIgnoreCase("Cash")) {
                            masterType = Group.ACCOUNTTYPE_CASH;
                        } else if (accountTypeName.equalsIgnoreCase("Bank")) {
                            masterType = Group.ACCOUNTTYPE_BANK;
                        }
                        if (!StringUtil.isNullOrEmpty(company.getCountry().getID()) && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id && Constants.TDSDefaultChartOfAccountsINDIA.contains(defaultAccount.getName())) {
                            masterType = Group.ACCOUNTTYPE_GST;
                        }
                    }

                    account.setGroup(group);
                    account.setName(defaultAccount.getName());
                    account.setCurrency(currency);
                    account.setAccounttype(accountType);
                    account.setMastertypevalue(masterType);
                    //For account Table, creation date is saved according to user's timezone diff.
                    account.setCreationDate(newUserDate);
                    account.setPresentValue(defaultAccount.getPresentValue());
                    account.setOpeningBalance(defaultAccount.getOpeningBalance());
                    if (!StringUtil.isNullOrEmpty(company.getCountry().getID()) && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id) {
                        account.setDefaultaccountID(defaultAccount.getID());
                    }
                    save(account);
                    hmAcc.put(defaultAccount.getID(), account.getID());
                } else {
                    Account account = (Account) chkAccount.getEntityList().get(0);
                    hmAcc.put(defaultAccount.getID(), account.getID());
                }
            }
            
            // Get master item list of nop then get default master item then get default accountid then check and get account id and map to that masteritem
            String getNatureOfPaymentQuery = "from MasterItem where masterGroup.ID=? AND company.companyID=?";
            List<MasterItem> list2 = executeQuery(getNatureOfPaymentQuery, new Object[]{Constants.NatureofPaymentGroup, companyid});
            for (MasterItem masteritemNOPobj : list2) {
                // If account is not mapped to master item nop then map as per defaultaccountid
                if(StringUtil.isNullOrEmpty(masteritemNOPobj.getAccID())){
                    if(masteritemNOPobj.getDefaultMasterItem() != null && !StringUtil.isNullOrEmpty(masteritemNOPobj.getDefaultMasterItem().getDefaultAccID())){
                        if(hmAcc.containsKey(masteritemNOPobj.getDefaultMasterItem().getDefaultAccID())){
                            masteritemNOPobj.setAccID(hmAcc.get(masteritemNOPobj.getDefaultMasterItem().getDefaultAccID()).toString());
                            save(masteritemNOPobj);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("copyTDSPayableAccountAndMapToMasterItemsNOP : " + ex.getMessage(), ex);
        }
    }
    
    public List getTransactionBasedOpeningBalance(Map<String, Object> requestParams) throws ServiceException, JSONException {
        String companyid = (String) requestParams.get("companyid");
        ArrayList params = new ArrayList();
        String accountCondition = "";
        String filterConjuctionCriteria = (String) requestParams.get("filterConjuctionCriteria");
        if(StringUtil.isNullOrEmpty(filterConjuctionCriteria)){
            filterConjuctionCriteria = " AND ";
        }
        String advSearchInvoiceString = "", advSearchInvoiceJoinString = "";
        String advSearchReceiptString = "", advSearchReceiptJoinString = "";
        String advSearchGoodsReceiptString = "", advSearchGoodsReceiptJoinString = "";
        String advSearchPaymentString = "", advSearchPaymentJoinString = "";
        String advSearchCNString = "", advSearchCNJoinString = "";
        String advSearchDNString = "", advSearchDNJoinString = "";
        String advSearchAssetString = "", advSearchAssetJoinString = "";
        if (requestParams.containsKey("Searchjson") && !StringUtil.isNullOrEmpty((String) requestParams.get("Searchjson"))) {
            String Searchjson = (String) requestParams.get("Searchjson");
            Map<String, Object> request = new HashMap<String, Object>();
            request.put("isOpeningBalance", true);
            request.put(Constants.companyKey, companyid);
            request.put(Constants.Acc_Search_Json, Searchjson);
            request.put(Constants.Filter_Criteria, filterConjuctionCriteria);
            request.put(Constants.moduleid, 2);
            request.put("isOpeningBalance", true);
            
            try {
                Searchjson = getSearchJsonByModule(request);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            request.put(Constants.Searchjson, Searchjson);
            request.put(Constants.appendCase, "and");
            request.put("filterConjuctionCriteria", filterConjuctionCriteria);
            request.put("isOpeningBalance", true);
            request.put(Constants.moduleid, 2);
            advSearchInvoiceString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
            advSearchInvoiceString = advSearchInvoiceString.replaceAll("AccJEDetailCustomData", "openingbalanceinvoicecustomdata");
            advSearchInvoiceString = advSearchInvoiceString.replaceAll("accproductcustomdata", "openingbalanceinvoicecustomdata");
            advSearchInvoiceString = advSearchInvoiceString.replaceAll("AccJEDetailsProductCustomData", "openingbalanceinvoicecustomdata");
            advSearchInvoiceJoinString = " inner join openingbalanceinvoicecustomdata on openingbalanceinvoicecustomdata.openingbalanceinvoiceid=inv.id ";
            try {
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (ParseException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            request.put(Constants.moduleid, 16);
            try {
                Searchjson = getSearchJsonByModule(request);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            request.put(Constants.Searchjson, Searchjson);
            request.put(Constants.moduleid, 16);
            advSearchReceiptString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
            advSearchReceiptString = advSearchReceiptString.replaceAll("AccJEDetailCustomData", "openingbalancereceiptcustomdata");
            advSearchReceiptString = advSearchReceiptString.replaceAll("accproductcustomdata", "openingbalancereceiptcustomdata");
            advSearchReceiptString = advSearchReceiptString.replaceAll("OpeningBalanceReceiptCustomData", "openingbalancereceiptcustomdata");
            advSearchReceiptString = advSearchReceiptString.replaceAll("AccJEDetailsProductCustomData", "openingbalancereceiptcustomdata");
            advSearchReceiptJoinString = " inner join openingbalancereceiptcustomdata on openingbalancereceiptcustomdata.openingbalancereceiptid=r.id ";
            try {
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (ParseException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            
            request.put(Constants.moduleid, 6);
            try {
                Searchjson = getSearchJsonByModule(request);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            request.put(Constants.Searchjson, Searchjson);

            request.put(Constants.moduleid, 6);
            advSearchGoodsReceiptString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
            advSearchGoodsReceiptString = advSearchGoodsReceiptString.replaceAll("AccJEDetailCustomData", "openingbalancevendorinvoicecustomdata");
            advSearchGoodsReceiptString = advSearchGoodsReceiptString.replaceAll("AccJEDetailsProductCustomData", "openingbalancevendorinvoicecustomdata");
            advSearchGoodsReceiptString = advSearchGoodsReceiptString.replaceAll("accproductcustomdata", "openingbalancevendorinvoicecustomdata");
            advSearchGoodsReceiptJoinString = " inner join openingbalancevendorinvoicecustomdata on openingbalancevendorinvoicecustomdata.openingbalancevendorinvoiceid=gr.id ";

            try {
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (ParseException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            
            request.put(Constants.moduleid, 14);
            try {
                Searchjson = getSearchJsonByModule(request);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            request.put(Constants.Searchjson, Searchjson);

            request.put(Constants.moduleid, 14);
            advSearchPaymentString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
            advSearchPaymentString = advSearchPaymentString.replaceAll("AccJEDetailCustomData", "openingbalancemakepaymentcustomdata");
            advSearchPaymentString = advSearchPaymentString.replaceAll("OpeningBalanceMakePaymentCustomData", "openingbalancemakepaymentcustomdata");
            advSearchPaymentString = advSearchPaymentString.replaceAll("AccJEDetailsProductCustomData", "openingbalancemakepaymentcustomdata");
            advSearchPaymentString = advSearchPaymentString.replaceAll("accproductcustomdata", "openingbalancemakepaymentcustomdata");
            advSearchPaymentJoinString = " inner join openingbalancemakepaymentcustomdata on openingbalancemakepaymentcustomdata.openingbalancemakepaymentid=p.id ";
            try {
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (ParseException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            
            request.put(Constants.moduleid, 12);
            try {
                Searchjson = getSearchJsonByModule(request);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            request.put(Constants.Searchjson, Searchjson);
            request.put(Constants.moduleid, 12);
            request.put("filterConjuctionCriteria", filterConjuctionCriteria);
            advSearchCNString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
            advSearchCNString = advSearchCNString.replaceAll("AccJEDetailCustomData", "openingbalancecreditnotecustomdata");
            advSearchCNString = advSearchCNString.replaceAll("OpeningBalanceCreditNoteCustomData", "openingbalancecreditnotecustomdata");
            advSearchCNString = advSearchCNString.replaceAll("AccJEDetailsProductCustomData", "openingbalancecreditnotecustomdata");
            advSearchCNString = advSearchCNString.replaceAll("accproductcustomdata", "openingbalancecreditnotecustomdata");
            advSearchCNJoinString = " inner join openingbalancecreditnotecustomdata on openingbalancecreditnotecustomdata.openingbalancecreditnoteid=cn.id ";
            try {
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (ParseException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            
            request.put(Constants.moduleid, 10);
            try {
                Searchjson = getSearchJsonByModule(request);
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            request.put(Constants.Searchjson, Searchjson);
            request.put(Constants.moduleid, 10);
            advSearchDNString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
            advSearchDNString = advSearchDNString.replaceAll("AccJEDetailCustomData", "openingbalancedebitnotecustomdata");
            advSearchDNString = advSearchDNString.replaceAll("OpeningBalanceDebitNoteCustomData", "openingbalancedebitnotecustomdata");
            advSearchDNString = advSearchDNString.replaceAll("AccJEDetailsProductCustomData", "openingbalancedebitnotecustomdata");
            advSearchDNString = advSearchDNString.replaceAll("accproductcustomdata", "openingbalancedebitnotecustomdata");
            advSearchDNJoinString = " inner join openingbalancedebitnotecustomdata on openingbalancedebitnotecustomdata.openingbalancedebitnoteid=dn.id ";
            try {
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
            } catch (ParseException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            Searchjson = getJsornStringForSearch((String) requestParams.get("Searchjson"), companyid, true);            
            request = new HashMap<String, Object>();
                request.put(Constants.Searchjson, Searchjson);
                request.put(Constants.appendCase, "and");
            request.put(Constants.moduleid, Constants.Acc_FixedAssets_Details_ModuleId);
                request.put("filterConjuctionCriteria", filterConjuctionCriteria);
                advSearchAssetString = String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
                advSearchAssetString = advSearchAssetString.replaceAll("AccJEDetailCustomData", "assetdetailcustomdata");
                advSearchAssetString = advSearchAssetString.replaceAll("AccJEDetailsProductCustomData", "assetdetailcustomdata");
                advSearchAssetString = advSearchAssetString.replaceAll("accproductcustomdata", "assetdetailcustomdata");
                advSearchAssetJoinString = " inner join assetdetailcustomdata on assetdetail.id=assetdetailcustomdata.assetDetailsId ";
                try {
                    StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                StringUtil.insertParamAdvanceSearchString1(params, Searchjson);
                } catch (JSONException ex) {
                    Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
                    Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
                }

            
        }
        String query = "select if(accgroup.nature=0,-SUM(a.amt),SUM(a.amt)), a.account,a.consider from (\n"
                + "select COALESCE(SUM(originalopeningbalancebaseamount),0) as amt, account, 'T' as consider from invoice inv " + advSearchInvoiceJoinString + " where inv.isopeningbalenceinvoice=1 AND inv.deleteflag='F' AND inv.company='" + companyid + "' " + advSearchInvoiceString + " group by account\n"
                + "UNION\n"
                + "select COALESCE(-SUM(r.originalopeningbalancebaseamount),0) as amt, account, 'T' as consider from receipt r " + advSearchReceiptJoinString + " where r.isopeningbalencereceipt=1 AND r.deleteflag='F' AND r.company='" + companyid + "' " + advSearchReceiptString + "  AND r.isnormalreceipt=0 group by account\n"
                + "UNION\n"
                + "select COALESCE(SUM(gr.originalopeningbalancebaseamount),0) as amt, account, 'T' as consider from goodsreceipt gr  " + advSearchGoodsReceiptJoinString + " where gr.isopeningbalenceinvoice=1 AND gr.deleteflag='F' AND gr.company='" + companyid + "' " + advSearchGoodsReceiptString + "  group by account\n"
                + "UNION\n"
                + "select COALESCE(-SUM(p.originalopeningbalancebaseamount),0) as amt, account, 'T' as consider from payment p " + advSearchPaymentJoinString + " where p.isopeningbalencepayment=1 AND p.deleteflag='F' AND p.company='" + companyid + "' " + advSearchPaymentString + "  AND p.isnormalpayment=0 group by account\n"
                + "UNION\n"
                + "select COALESCE(-SUM(cn.originalopeningbalancebaseamount),0) as amt, account, 'T' as consider from creditnote cn " + advSearchCNJoinString + " where cn.isopeningbalencecn=1 AND cn.iscnforcustomer=1 AND cn.deleteflag='F' AND cn.company='" + companyid + "' " + advSearchCNString + "  AND cn.isnormalcn=0 group by account\n"
                + "UNION\n"
                + "select COALESCE(sum(cn.originalopeningbalancebaseamount),0) as amt, account, 'T' as consider from creditnote cn " + advSearchCNJoinString + " where cn.isopeningbalencecn=1 AND cn.iscnforcustomer=0 AND cn.deleteflag=false AND cn.company='" + companyid + "' " + advSearchCNString + "  AND cn.isnormalcn=0 group by account\n"
                + "UNION\n"
                + "select COALESCE(SUM(dn.originalopeningbalancebaseamount),0) as amt, account, 'T' as consider from debitnote dn " + advSearchDNJoinString + " where dn.isopeningbalencedn=1 AND dn.isdnforvendor=0 AND dn.deleteflag='F' AND dn.company='" + companyid + "' " + advSearchDNString + "  AND dn.isnormaldn=0 group by account\n"
                + "UNION\n"
                + "select COALESCE(-SUM(dn.originalopeningbalancebaseamount),0) as amt, account, 'T' as consider from debitnote dn " + advSearchDNJoinString + " where dn.isopeningbalencedn=1 AND dn.isdnforvendor=1 AND dn.deleteflag=false AND dn.company='" + companyid + "' " + advSearchDNString + "  AND dn.isnormaldn=0 group by account\n"
                + "UNION\n"
                + "select -SUM(openingdepreciation) as amt, depreciationprovisionglaccount as account, if(SUM(openingdepreciation) > 0 ,'T','F') as consider from assetdetail  "+advSearchAssetJoinString+"\n"
                + "inner join product on assetdetail.product = product.id where product.company = '"+companyid+"'  "+advSearchAssetString+" \n"
                + "group by product.depreciationprovisionglaccount\n"
                + "UNION\n"
                + "select SUM(cost) as amt, product.purchaseaccount as account, if(SUM(cost) > 0 ,'T','F') as consider from assetdetail "+advSearchAssetJoinString+"\n"
                + "inner join product on assetdetail.product = product.id where product.company = '"+companyid+"' and assetdetail.iscreatedfromopeningform='1' "+advSearchAssetString+" group by product.purchaseaccount \n"
                + ") a \n"
                + "inner join account acc on acc.id=a.account " + accountCondition + " \n"
                + "inner join accgroup on acc.groupname=accgroup.id \n"
                + "group by a.account";

        List list = executeSQLQuery(query, params.toArray());
        return list;
    }
    
    public List getDefaultAccountOpeningBalance(Map<String, Object> requestParams) throws ServiceException, JSONException, SessionExpiredException {
        String companyid = (String) requestParams.get("companyid");
        ArrayList params = new ArrayList();
        params.add(companyid);
        String filterConjuctionCriteria = (String) requestParams.get("filterConjuctionCriteria");
        String advSearchJoinString = "", advSearchString = "";
        String accountAmountSelect = "account.openingbalance,account.openingbalance";
        String accountDateQuery = "";
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if(requestParams.containsKey("isTrialBalance") && (Boolean)requestParams.get("isTrialBalance")){
            if(requestParams.containsKey("accountstartdate") && requestParams.containsKey("accountenddate")){
                String startDateStr = df.format((Date)requestParams.get("accountstartdate"));
                String endDateStr = df.format((Date)requestParams.get("accountenddate"));
                accountDateQuery += " and account.creationdate between  '"+startDateStr +"' and '"+endDateStr+"'";
            }
        }
        
        if (requestParams.containsKey("Searchjson") && !StringUtil.isNullOrEmpty((String) requestParams.get("Searchjson")) ) {
            String Searchjson = (String) requestParams.get("Searchjson");
            Map<String, Object> request = new HashMap<String, Object>();
            filterConjuctionCriteria = (String) requestParams.get(Constants.Filter_Criteria);
            request.put("isOpeningBalance", true);
            request.put(Constants.companyKey, requestParams.get(Constants.companyKey));
            request.put(Constants.Acc_Search_Json, Searchjson);
            request.put(Constants.Filter_Criteria, filterConjuctionCriteria);
            request.put(Constants.moduleid, Constants.Account_Statement_ModuleId);
            String searchjson = null;
            try {
                searchjson = getSearchJsonByModule(request);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            JSONObject jobj = new JSONObject(searchjson);
            JSONArray customFieldArray = jobj.getJSONArray(Constants.root);
            customFieldArray = StringUtil.getCustomFieldSearchArray(customFieldArray);
            int count = customFieldArray.length();
            for (int i = 0; i < count; i++) {
                JSONObject tmp = customFieldArray.getJSONObject(i);
                tmp.put(Constants.iscustomcolumndata, false);
                tmp.put(Constants.isfrmpmproduct, false);
                if (tmp.has("column") && tmp.has("search")) {
                    String comboid = tmp.getString("search");
                    if (comboid.endsWith(",")) {
                        comboid = comboid.substring(0, comboid.length() - 1);
                    }
                    if(i== 0){
                        advSearchString += " (distributebalance.field='" + tmp.getString("column") + "' and distributebalance.comboid = '" + comboid + "') ";
                    }
                    else{
                        advSearchString += " "+filterConjuctionCriteria+ " (distributebalance.field='" + tmp.getString("column") + "' and distributebalance.comboid = '" + comboid + "') ";
                    }
                }
            }
            if(!StringUtil.isNullOrEmpty(advSearchString)){
                advSearchJoinString += " LEFT JOIN distributebalance on distributebalance.accountid=account.id ";
                advSearchString = "and (( pref.splitopeningbalanceamount='T'  and " +advSearchString +") ";            
            }
            
            request.put(Constants.appendCase, "or");
            request.put(Constants.Acc_Search_Json, searchjson);
            request.put(Constants.Searchjson, jobj.toString());
            String advSearchCustomData ="";
            
            advSearchCustomData= String.valueOf(StringUtil.getMyAdvanceSearchString(request, true).get(Constants.myResult));
            if (!StringUtil.isNullOrEmptyWithTrim(advSearchCustomData)) {
                advSearchCustomData = advSearchCustomData.substring(0, advSearchCustomData.lastIndexOf(")")) + " and pref.splitopeningbalanceamount='F')";
                advSearchString +=advSearchCustomData;
                advSearchString = advSearchString.replaceAll("AccountCustomData", "accountcustomdata");
                advSearchJoinString += " LEFT JOIN accountcustomdata on accountcustomdata.accountid=account.id ";
                accountAmountSelect = "COALESCE(distributebalance.openingbal,account.openingbalance,0),COALESCE(distributebalance.openingbal,account.openingbalance,0)";
            }
            if (!StringUtil.isNullOrEmpty(advSearchString)) {
                advSearchString += ")";
            }
            try {
                StringUtil.insertParamAdvanceSearchString1(params, searchjson);
            } catch (ParseException ex) {
                Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

//        String accountDateQuery = "";
//        if(requestParams.containsKey("accountDateCheck") && (Boolean)requestParams.get("accountDateCheck") && requestParams.containsKey("start")){
//            accountDateQuery += " and account.creationdate <= ?";
//            Date start = (Date) requestParams.get("start");
//            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//            
//            params.add(df.format(start));
//        }
        String query = "select if(account.currency=company.currency," + accountAmountSelect + "/COALESCE(exchangerate_calc(account.company,account.creationdate, company.currency,account.currency),1)),account.id from account\n"
                + " inner join company on account.company = company.companyid \n"
                + "inner join extracompanypreferences pref on pref.id=company.companyid \n"
                + advSearchJoinString + "\n"
                + " where account.company=? " + advSearchString + accountDateQuery+ " group by account.id";
        List list = executeSQLQuery(query, params.toArray());
        return list;
    }

    public String getSearchJsonByModule(Map<String, Object> requestParams) throws SessionExpiredException, UnsupportedEncodingException {
        JSONObject resultObj = null;
        JSONArray dataJArrObj = new JSONArray();
        boolean removeProductCustomFilter = false;   //Default true
        if (requestParams.containsKey("removeProductCustomFilter")) {
            removeProductCustomFilter = Boolean.parseBoolean(requestParams.get("removeProductCustomFilter").toString());
        }
        String Searchjson =  requestParams.get(Constants.Acc_Search_Json).toString();
        String filterCriteria = requestParams.get(Constants.Filter_Criteria).toString();
        String companyid = requestParams.get(Constants.companyKey)!=null? requestParams.get(Constants.companyKey).toString() : "";
        int moduleid = requestParams.get(Constants.moduleid)!=null? Integer.parseInt(requestParams.get(Constants.moduleid).toString()): 0;
        try{
            JSONObject jObj = new JSONObject(Searchjson);
            if(!StringUtil.isNullOrEmpty(Searchjson) && !StringUtil.isNullOrEmpty(filterCriteria) && moduleid!=0){
                int count = jObj.getJSONArray(Constants.root).length();
                for (int i = 0; i < count; i++) {
                    JSONObject jobj1 = jObj.getJSONArray(Constants.root).getJSONObject(i);
                    boolean mastersearch=false;
                    int mastermoduleid = 0;
                    if(jobj1.optString(Constants.moduleid).equalsIgnoreCase(""+Constants.Acc_Customer_ModuleId)){
                        mastersearch=true;
                        mastermoduleid = Constants.Acc_Customer_ModuleId;
                    } else if(jobj1.optString(Constants.moduleid).equalsIgnoreCase(""+Constants.Acc_Vendor_ModuleId)){
                        mastersearch=true;
                        mastermoduleid = Constants.Acc_Vendor_ModuleId;
                    }
                    if(removeProductCustomFilter && jobj1.optBoolean("isfrmpmproduct",false)){
                        jobj1.put("isfrmpmproduct", false);
                    }
                    if(((jobj1.getString("fieldtype").equalsIgnoreCase("4") || jobj1.getString("fieldtype").equalsIgnoreCase("7") || jobj1.getString("fieldtype").equalsIgnoreCase("12")) && jobj1.getString("xtype").equalsIgnoreCase("select")) && !mastersearch){
                        String fieldlabel = jobj1.get("columnheader")!=null? jobj1.get("columnheader").toString() : "";
                        fieldlabel = StringUtil.DecodeText(fieldlabel);
                        String searchText = jobj1.get("combosearch")!=null? jobj1.get("combosearch").toString() : "";
                        searchText =StringUtil.DecodeText(searchText);

                        KwlReturnObject result = null;
                        HashMap<String, Object> reqPar = new HashMap<String, Object>();
                        reqPar.put("filter_names", Arrays.asList(Constants.companyKey, "fieldlabel"));
                        reqPar.put("filter_values", Arrays.asList(companyid, fieldlabel));
                        reqPar.put(Constants.moduleid, mastersearch?mastermoduleid:moduleid);
                        result = getFieldParams(reqPar);
                        List lst = result.getEntityList();
                        Iterator ite = lst.iterator();
                        while (ite.hasNext()) {
                            FieldParams tmpcontyp = new FieldParams();
                            tmpcontyp = (FieldParams) ite.next();
                            String fieldid = tmpcontyp.getId();
                            int columnNo=tmpcontyp.getColnum();
                            jobj1.remove("column");
                            jobj1.put("column",fieldid);
                            jobj1.remove("refdbname");
                            jobj1.put("refdbname","Col"+columnNo);
                            jobj1.remove("xfield");
                            jobj1.put("xfield", "Col"+columnNo);
                            if (!mastersearch) {
                                jobj1.remove(Constants.moduleid);
                                jobj1.put(Constants.moduleid, tmpcontyp.getModuleid());
                            }

                            HashMap<String, Object> reqParams = new HashMap<String, Object>();
                            reqParams.put(Constants.filter_names, Arrays.asList("fieldid", FieldConstants.Crm_deleteflag));
                            reqParams.put(Constants.filter_values, Arrays.asList(fieldid, 0));
                            reqParams.put("searchText", searchText);
                            ArrayList order_by = new ArrayList();
                            ArrayList order_type = new ArrayList();
                            order_by.add("itemsequence");
                            order_type.add("asc");
                            reqParams.put("order_by", order_by);
                            reqParams.put("order_type", order_type);

                            KwlReturnObject result1 = getCustomCombodata(reqParams);
                            List lst1 = result1.getEntityList();
                            String comboDataIds = "";
                            Iterator ite1 = lst1.iterator();
                            while (ite1.hasNext()) {
                                Object[] row = (Object[]) ite1.next();
                                FieldComboData comboDataObj = (FieldComboData) row[0];
                                comboDataIds = comboDataIds + comboDataObj.getId() + ","; 
                            }
                            comboDataIds = comboDataIds.length()>0 ? comboDataIds.substring(0,comboDataIds.length()-1) : "";
                            jobj1.remove("searchText");
                            jobj1.remove("search");
                            jobj1.put("searchText",comboDataIds);   
                            jobj1.put("search",comboDataIds);   
                        }
                    }
                        dataJArrObj.put(jobj1);
                }
                jObj.put(Constants.root, dataJArrObj);
            } else {
                jObj = new JSONObject(Searchjson);
            }
            resultObj = jObj;
        }        
        catch (JSONException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultObj.toString();
    }

    @Override
    public KwlReturnObject getLineLevelTerms(JSONObject json) throws ServiceException {
        StringBuilder hqlQuery = new StringBuilder();
        hqlQuery.append("from LineLevelTerms where company.companyID = ? ");
        List params = new ArrayList();
        params.add(json.optString(Constants.companyKey, ""));
        if (!StringUtil.isNullOrEmpty(json.optString("defaultterm", null))) {
            hqlQuery.append(" and defaultterms = ? ");
            params.add(json.optString("defaultterm", null));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("salesorpurchase", null))) {
            hqlQuery.append(" and salesorpurchase = ? ");
            params.add(json.optInt("salesorpurchase", 0));
        }
        if (!StringUtil.isNullOrEmpty(json.optString("accountid", null))) {
            hqlQuery.append(" and account.ID  = ? ");
            params.add(json.optString("accountid", null));
        }
        List list = executeQuery(hqlQuery.toString(), params.toArray());
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    @Override
    public JSONObject getBudgetVsCostReportDetails(HashMap<String, Object> requestMap) throws ServiceException {
        List params = new ArrayList();
        JSONObject jobj = new JSONObject();
        try {
            String query = "";
            String innerjoin = "";
            String whereclouse = "";
            List listso = null;
            JSONObject jobjGlobalCombo = requestMap.containsKey("globalCombodata") ? (JSONObject) requestMap.get("globalCombodata") : null;
            //ERP-41557
            int amountDigit = Constants.AMOUNT_DIGIT_AFTER_DECIMAL;
            if (requestMap.containsKey(Constants.amountdecimalforcompany) && requestMap.get(Constants.amountdecimalforcompany) != null) {
                amountDigit = (Integer) requestMap.get(Constants.amountdecimalforcompany);
            }
            
            requestMap.put("customfield", "0");
            List listComboso = (List)requestMap.get("mastrComboDataList");
            Iterator itr = listComboso.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                
                /*** Custom Data From Sales Order ***/
                if ((short) row[2] == Constants.Acc_Sales_Order_ModuleId) {
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    whereclouse = "";
                    //ERP-41557
//                    query = "Select sum((sodetails.rate*sodetails.quantity - IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount))/salesorder.externalcurrencyrate) as amount, "
                    query = "Select sum((ROUND(sodetails.rate*sodetails.quantity,"+amountDigit+") - ROUND(IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount),"+amountDigit+"))/salesorder.externalcurrencyrate) as amount, "
                            + "SUM(totalcost) as totalcost from salesorderdetailcustomdata ";
                    innerjoin += " Inner join sodetails on sodetails.id = salesorderdetailcustomdata.soDetailID ";
                    innerjoin += " LEFT join sodetailsvendormapping on sodetailsvendormapping.id = sodetails.id ";
                    innerjoin += " INNER JOIN salesorder on salesorder.id = sodetails.salesorder ";
                    whereclouse += " WHERE salesorderdetailcustomdata." + "col" + row[1] + " =? ";
                    params.add(row[0]);
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Sales_Order_ModuleId")) {
                        innerjoin += " INNER JOIN salesordercustomdata on salesordercustomdata.soID = salesorder.id ";
                        whereclouse += " AND salesordercustomdata.col" + jobjGlobalCombo.getString("Acc_Sales_Order_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Sales_Order_ModuleId"));
                    }
                    whereclouse += " And sodetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    whereclouse += " And salesorder.orderdate >= ? ";
                    params.add(requestMap.get("startDate"));
                    whereclouse += " And salesorder.orderdate <= ? ";
                    params.add(requestMap.get("endDate"));
                    whereclouse += " And salesorder.deleteflag = 'F' ";
                    whereclouse += " And salesorder.approvestatuslevel >= '11' ";
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("salesOrderDetails", listso);
                    }
                }
                
                /*** Custom Data From Purchase Order ***/
                if ((short) row[2] == Constants.Acc_Purchase_Order_ModuleId) {
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    String innerjoin2 = "";
                    whereclouse = "";
                    //ERP-41557
//                    query = "Select ((podetails.rate*podetails.quantity - IF(podetails.discountispercent,((podetails.rate*podetails.quantity)*podetails.discount/100),podetails.discount))/purchaseorder.externalcurrencyrate) as amount    from purchaseorderdetailcustomdata  ";
                    query = "Select ((ROUND(podetails.rate*podetails.quantity,"+amountDigit+")- ROUND(IF(podetails.discountispercent,((podetails.rate*podetails.quantity)*podetails.discount/100),podetails.discount),"+amountDigit+"))/purchaseorder.externalcurrencyrate) as amount    from purchaseorderdetailcustomdata  ";
                    innerjoin += " Inner join podetails on podetails.id =purchaseorderdetailcustomdata.poDetailID  ";
                    innerjoin += " INNER JOIN purchaseorder on purchaseorder.id = podetails.purchaseorder  ";
                    innerjoin2 += " LEFT JOIN polinking on purchaseorder.id = polinking.docid  ";
                    innerjoin2 += " LEFT JOIN goodsreceiptorderlinking on purchaseorder.id = goodsreceiptorderlinking.linkeddocid  ";
                    innerjoin2 += " LEFT JOIN goodsreceiptlinking on purchaseorder.id = goodsreceiptlinking.linkeddocid  ";
                    whereclouse += " WHERE purchaseorderdetailcustomdata." + "col" + row[1] + " =? ";
                    params.add(row[0]);
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Purchase_Order_ModuleId")) {
                        innerjoin += " INNER JOIN purchaseordercustomdata on purchaseordercustomdata.poID = purchaseorder.id ";
                        whereclouse += " AND purchaseordercustomdata.col" + jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleId"));
                    }
                    whereclouse += " And podetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    whereclouse += " And purchaseorder.deleteflag = 'F' ";
                    whereclouse += " And purchaseorder.approvestatuslevel >= '11' ";
                    //ERP-42066
                    String linkingFlag = " And  ((polinking.moduleid <>?) OR polinking.moduleid IS NULL) ";
                    linkingFlag += " AND (SELECT COUNT(* )  from goodsreceiptorderlinking  as grol where docid IN(goodsreceiptorderlinking.docid)  AND  grol.moduleid=6 and sourceflag=0) = 0  ";
                    linkingFlag += " AND (select count(*) from goodsreceiptlinking as viLinking where viLinking.linkeddocid IN(purchaseorder.id))=0 ";
                    String groupby = " GROUP BY ponumber, podetails.id ";
                    params.add(Constants.Acc_Vendor_Invoice_ModuleId);
                    listso = executeSQLQuery("SELECT SUM(amount) FROM (" + query + innerjoin + innerjoin2 + whereclouse + linkingFlag + groupby + ") as tbl", params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("purchaseOrderDetailsSpentCommitted", listso);
                    }
                    params.remove(params.size()-1);
                    //ERP-41557
//                    query = "Select sum((podetails.rate*podetails.quantity - IF(podetails.discountispercent,((podetails.rate*podetails.quantity)*podetails.discount/100),podetails.discount))/purchaseorder.externalcurrencyrate) as amount    from purchaseorderdetailcustomdata  ";
                    query = "Select sum((ROUND(podetails.rate*podetails.quantity,"+amountDigit+") - ROUND(IF(podetails.discountispercent,((podetails.rate*podetails.quantity)*podetails.discount/100),podetails.discount),"+amountDigit+"))/purchaseorder.externalcurrencyrate) as amount    from purchaseorderdetailcustomdata  ";
                    whereclouse += " And purchaseorder.orderdate >= ? ";
                    params.add(requestMap.get("startDate"));
                    whereclouse += " And purchaseorder.orderdate <= ? ";
                    params.add(requestMap.get("endDate"));
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("purchaseOrderDetails", listso);
                    }

                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    innerjoin2 = "";
                    whereclouse = "";
                    //ERP-41557
//                    query = "Select (if(expensepodetails.isdebit='T',expensepodetails.amount,(expensepodetails.amount*-1))/purchaseorder.externalcurrencyrate) as amount    from expensepodetailcustomdata ";
                    query = "Select (ROUND(if(expensepodetails.isdebit='T',expensepodetails.amount,(expensepodetails.amount*-1)),"+amountDigit+")/purchaseorder.externalcurrencyrate) as amount    from expensepodetailcustomdata ";
                    innerjoin = " Inner join expensepodetails on expensepodetails.id =expensepodetailcustomdata.expensepodetailid ";
                    innerjoin += " INNER JOIN purchaseorder on purchaseorder.id = expensepodetails.purchaseorder ";
                    innerjoin2 += " LEFT JOIN polinking on purchaseorder.id = polinking.docid  ";
                    innerjoin2 += " LEFT JOIN goodsreceiptorderlinking on purchaseorder.id = goodsreceiptorderlinking.linkeddocid  ";
                    innerjoin2 += " LEFT JOIN goodsreceiptlinking on purchaseorder.id = goodsreceiptlinking.linkeddocid  ";
                    whereclouse += " WHERE expensepodetailcustomdata." + "col" + row[1] + " =? ";
                    params.add(row[0]);
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Purchase_Order_ModuleId")) {
                        innerjoin += " INNER JOIN purchaseordercustomdata on purchaseordercustomdata.poID = purchaseorder.id ";
                        whereclouse += " AND purchaseordercustomdata.col" + jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleId"));
                    }
                    whereclouse += " And expensepodetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    whereclouse += " And purchaseorder.deleteflag = 'F' ";
                    whereclouse += " And purchaseorder.approvestatuslevel >= '11' ";
                     //ERP-42066
                    linkingFlag = " And  ((polinking.moduleid <>?) OR polinking.moduleid IS NULL) ";
                    linkingFlag += " AND (SELECT COUNT(* )  from goodsreceiptorderlinking  as grol where docid IN(goodsreceiptorderlinking.docid)  AND  grol .moduleid=6 and sourceflag=0) = 0  ";
                    linkingFlag += " AND (select count(*) from goodsreceiptlinking as viLinking where viLinking.linkeddocid IN(purchaseorder.id))=0 ";
                    groupby = " GROUP BY ponumber, expensepodetails.id ";
                    params.add(Constants.Acc_Vendor_Invoice_ModuleId);
                    listso = executeSQLQuery("SELECT SUM(amount) from (" + query + innerjoin + innerjoin2 + whereclouse + linkingFlag + groupby + ") as tbl", params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("expensePODetailsSpentCommitted", listso);
                    }
                    params.remove(params.size()-1);
                    //ERP-41557
//                    query = "Select sum(if(expensepodetails.isdebit='T',expensepodetails.amount,(expensepodetails.amount*-1))/purchaseorder.externalcurrencyrate) as amount    from expensepodetailcustomdata ";
                    query = "Select sum(ROUND(if(expensepodetails.isdebit='T',expensepodetails.amount,(expensepodetails.amount*-1)),"+amountDigit+")/purchaseorder.externalcurrencyrate) as amount    from expensepodetailcustomdata ";
                    whereclouse += " And purchaseorder.orderdate >= ? ";
                    params.add(requestMap.get("startDate"));
                    whereclouse += " And purchaseorder.orderdate <= ? ";
                    params.add(requestMap.get("endDate"));
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("expensePODetails", listso);
                    }

                }
                
                /*** Custom Data From Purchase Invoice ***/
                if ((short) row[2] == Constants.Acc_Vendor_Invoice_ModuleId) {
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    whereclouse = "";
                    //ERP-41557
//                    query = "Select sum((grdetails.rate*inventory.quantity  - IF((discount.id IS NOT NULL), IF(discount.inpercent='T',((grdetails.rate*inventory.quantity)*discount.discount/100),discount.discount),0))/exchangeratedetails.exchangerate) as amount    from accjedetailcustomdata  ";
                    query = "Select sum(ROUND(grdetails.rate*inventory.quantity,"+amountDigit+") - ROUND(IF((discount.id IS NOT NULL), IF(discount.inpercent='T',((grdetails.rate*inventory.quantity)*discount.discount/100),discount.discount),0)/exchangeratedetails.exchangerate,"+amountDigit+")) as amount    from accjedetailcustomdata  ";
                    innerjoin += " INNER JOIN jedetail on accjedetailcustomdata.jedetailId = jedetail.id    ";
                    innerjoin += " Inner join grdetails on grdetails.id = accjedetailcustomdata.recdetailId  ";
                    innerjoin += " INNER JOIN inventory on grdetails.id = inventory.id  ";
                    innerjoin += " INNER JOIN goodsreceipt on goodsreceipt.id = grdetails.goodsreceipt  ";
                    innerjoin += " LEFT JOIN discount on discount.id = grdetails.discount  ";
                    innerjoin += " INNER JOIN exchangeratedetails on goodsreceipt.exchangeratedetail = exchangeratedetails.id ";
                    innerjoin += " INNER JOIN journalentry on journalentry.id = goodsreceipt.journalentry  ";
                    whereclouse += " WHERE accjedetailcustomdata." + "col" + row[1] + " =?   and jedetail.debit ='T' ";
                    params.add(row[0]);
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Vendor_Invoice_ModuleId")) {
                        innerjoin += " INNER JOIN accjecustomdata on journalentry.accjecustomdataref = accjecustomdata.journalentryId ";
                        whereclouse += " AND accjecustomdata.col" + jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleId"));
                    }
                    whereclouse += " And grdetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    whereclouse += " And goodsreceipt.deleteflag = 'F' ";
                    whereclouse += " And goodsreceipt.approvestatuslevel >= '11' ";
                    
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("goodsReceiptDetailsWithoutDate", listso);
                    }
                    
                    whereclouse += " And journalentry.entrydate >= ? ";
                    params.add(requestMap.get("startDate"));
                    whereclouse += " And journalentry.entrydate <= ? ";
                    params.add(requestMap.get("endDate"));
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("goodsReceiptDetails", listso);
                    }
                    
                    
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    innerjoin = "";
                    whereclouse = "";
                    //ERP-41557
//                    query = "Select sum(if(expenseggrdetails.isdebit='T',expenseggrdetails.amount,(expenseggrdetails.amount*-1))/exchangeratedetails.exchangerate) as amount    from accjedetailcustomdata  ";
                    query = "Select sum(ROUND(if(expenseggrdetails.isdebit='T',expenseggrdetails.amount,(expenseggrdetails.amount*-1))/exchangeratedetails.exchangerate,"+amountDigit+")) as amount    from accjedetailcustomdata  ";
                    innerjoin += " INNER JOIN jedetail on accjedetailcustomdata.jedetailId = jedetail.id ";
                    innerjoin += " Inner join expenseggrdetails on expenseggrdetails.id = accjedetailcustomdata.recdetailId ";
                    innerjoin += " INNER JOIN goodsreceipt on goodsreceipt.id = expenseggrdetails.goodsreceipt ";
                    innerjoin += " INNER JOIN exchangeratedetails on goodsreceipt.exchangeratedetail = exchangeratedetails.id ";
                    innerjoin += " INNER JOIN journalentry on journalentry.id = goodsreceipt.journalentry ";
                    whereclouse += " WHERE accjedetailcustomdata." + "col" + row[1] + " =? ";//and jedetail.debit ='T'
                    params.add(row[0]);
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Vendor_Invoice_ModuleId")) {
                        innerjoin += " INNER JOIN accjecustomdata on journalentry.accjecustomdataref = accjecustomdata.journalentryId ";
                        whereclouse += " AND accjecustomdata.col" + jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleId"));
                    }
                    whereclouse += " And expenseggrdetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    whereclouse += " And goodsreceipt.deleteflag = 'F' ";
                    whereclouse += " And goodsreceipt.approvestatuslevel >= '11' ";
                    
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("goodsReceiptExpenseDetailsWithOutdateFilter", listso);
                    }
                    
                    whereclouse += " And journalentry.entrydate >= ? ";
                    params.add(requestMap.get("startDate"));
                    whereclouse += " And journalentry.entrydate <= ? ";
                    params.add(requestMap.get("endDate"));
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("goodsReceiptExpenseDetails", listso);
                    }
                    
                    
                }
                
                /*** Custom Data From Sales Invoice ***/
                if ((short) row[2] == Constants.Acc_Invoice_ModuleId) {
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    whereclouse = "";
                    //ERP-41557
//                    query = "Select SUM(invoicedetails.rate*inventory.quantity  -  IF((discount.id IS NOT NULL),IF(discount.inpercent='T',((invoicedetails.rate*inventory.quantity)*discount.discount/100),discount.discount),0)/exchangeratedetails.exchangerate) as amount    from accjedetailcustomdata  ";
                    query = "Select SUM(ROUND(invoicedetails.rate*inventory.quantity,"+amountDigit+")  - ROUND(IF((discount.id IS NOT NULL),IF(discount.inpercent='T',((invoicedetails.rate*inventory.quantity)*discount.discount/100),discount.discount),0)/exchangeratedetails.exchangerate,"+amountDigit+")) as amount    from accjedetailcustomdata  ";
                    innerjoin += " INNER JOIN jedetail on accjedetailcustomdata.jedetailId = jedetail.id    ";
                    innerjoin += " Inner join invoicedetails on invoicedetails.id = accjedetailcustomdata.recdetailId  ";
                    innerjoin += " LEFT JOIN discount on discount.id = invoicedetails.discount   ";
                    innerjoin += " INNER JOIN inventory on invoicedetails.id = inventory.id  ";
                    innerjoin += " INNER JOIN invoice on invoice.id = invoicedetails.invoice  ";
                    innerjoin += " INNER JOIN exchangeratedetails on invoice.exchangeratedetail = exchangeratedetails.id ";
                    innerjoin += " INNER JOIN journalentry on journalentry.id = invoice.journalentry  ";
                    whereclouse += " WHERE accjedetailcustomdata." + "col" + row[1] + " =? and jedetail.debit ='F' ";
                    params.add(row[0]);
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Vendor_Invoice_ModuleId")) {
                    innerjoin += " INNER JOIN accjecustomdata on journalentry.accjecustomdataref = accjecustomdata.journalentryId ";
                    whereclouse += " AND accjecustomdata.col" + jobjGlobalCombo.getString("Acc_Invoice_ModuleCol") + "=? ";
                    params.add(jobjGlobalCombo.getString("Acc_Invoice_ModuleId"));
                    }
                    whereclouse += " And invoicedetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    whereclouse += " And journalentry.entrydate >= ? ";
                    params.add(requestMap.get("startDate"));
                    whereclouse += " And journalentry.entrydate <= ? ";
                    params.add(requestMap.get("endDate"));
                    whereclouse += " And invoice.deleteflag = 'F' ";
                    whereclouse += " And invoice.approvestatuslevel >= '11' ";
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("invoiceDetails", listso);
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    @Override
    public JSONObject getActualVsBudgetReportDetails(HashMap<String, Object> requestMap) throws ServiceException {
        List params = new ArrayList();
        JSONObject jobj = new JSONObject();
        try {
            String query = "";
            String innerjoin = "";
            String whereclouse = "";
            List listso = null;
            JSONObject jobjGlobalCombo = requestMap.containsKey("globalCombodata") ? (JSONObject) requestMap.get("globalCombodata") : null;
            JSONObject globalCustomField = requestMap.containsKey("globalCustomField") ? (JSONObject) requestMap.get("globalCustomField") : null;
            requestMap.put("customfield", "0");
            //ERP-41557
            int amountDigit = Constants.AMOUNT_DIGIT_AFTER_DECIMAL;
            if (requestMap.containsKey(Constants.amountdecimalforcompany) && requestMap.get(Constants.amountdecimalforcompany) != null) {
                amountDigit = (Integer) requestMap.get(Constants.amountdecimalforcompany);
            }
            List listComboso = (List)requestMap.get("mastrComboDataList");
            Iterator itr = listComboso.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                
                /*** Custom Data From Sales Order ***/
                if ((short) row[2] == Constants.Acc_Sales_Order_ModuleId) {// Done
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    whereclouse = "";
                    //ERP-41557
//                    query = "Select sum((sodetails.rate*sodetails.quantity - IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount))/salesorder.externalcurrencyrate) as amount, SUM(totalcost) as totalcost from salesorderdetailcustomdata ";
                    query = "Select sum((ROUND(sodetails.rate*sodetails.quantity,"+amountDigit+") - ROUND(IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount),"+amountDigit+"))/salesorder.externalcurrencyrate) as amount, SUM(totalcost) as totalcost from salesorderdetailcustomdata ";
                    innerjoin += " Inner join sodetails on sodetails.id = salesorderdetailcustomdata.soDetailID ";
                    innerjoin += " LEFT join sodetailsvendormapping on sodetailsvendormapping.id = sodetails.id ";
                    innerjoin += " INNER JOIN salesorder on salesorder.id = sodetails.salesorder ";
                    whereclouse += " WHERE salesorderdetailcustomdata." + "col" + row[1] + " =? ";
                    params.add(row[0]);
                    
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Sales_Order_ModuleId")) {
                        innerjoin += " INNER JOIN salesordercustomdata on salesordercustomdata.soID = salesorder.id ";
                        whereclouse += " AND salesordercustomdata.col" + jobjGlobalCombo.getString("Acc_Sales_Order_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Sales_Order_ModuleId"));
                    }
                    if(requestMap.containsKey("cogaAccount") && requestMap.get("cogaAccount")!=null && !(requestMap.get("cogaAccount").toString()).equals("")){
                        innerjoin += " INNER JOIN product on sodetails.product = product.id ";
                        whereclouse += " AND IF(producttype='"+Constants.SERVICE+"', purchaseAccount= ? , cogsaccount=?) ";
                        params.add(requestMap.get("cogaAccount"));
                        params.add(requestMap.get("cogaAccount"));
                    }
                    
                    whereclouse += " And salesorder.approvestatuslevel >= '11' ";
                    whereclouse += " And salesorder.deleteflag = 'F' ";
                    whereclouse += " And sodetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    whereclouse += " And salesorder.orderdate >= ? ";
                    params.add(requestMap.get("startDate"));
                    whereclouse += " And salesorder.orderdate <= ? ";
                    params.add(requestMap.get("endDate"));
                        listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                            jobj.put("salesOrderDetails", listso);
                        }
                    if (listso != null && listso.size() > 0) {
                            jobj.put("salesOrderDetailsApproved", listso);
                        }
                    }
                
                /*** Custom Data From Purchase Order ***/
                if ((short) row[2] == Constants.Acc_Purchase_Order_ModuleId) {// Done
                    // Purcahse Order - Product
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    String innerjoin2 = "";
                    whereclouse = "";
                    //ERP-41557
//                    query = "Select ((podetails.rate*podetails.quantity - IF(podetails.discountispercent,((podetails.rate*podetails.quantity)*podetails.discount/100),podetails.discount))/purchaseorder.externalcurrencyrate) as amount    from purchaseorderdetailcustomdata  ";
                    query = "Select ((ROUND(podetails.rate*podetails.quantity,"+amountDigit+") - ROUND(IF(podetails.discountispercent,((podetails.rate*podetails.quantity)*podetails.discount/100),podetails.discount),"+amountDigit+"))/purchaseorder.externalcurrencyrate) as amount    from purchaseorderdetailcustomdata  ";
                    innerjoin += " Inner join podetails on podetails.id =purchaseorderdetailcustomdata.poDetailID  ";
                    innerjoin += " INNER JOIN purchaseorder on purchaseorder.id = podetails.purchaseorder  ";
                    innerjoin2 += " LEFT JOIN polinking on purchaseorder.id = polinking.docid  ";
                    innerjoin2 += " LEFT JOIN goodsreceiptorderlinking on purchaseorder.id = goodsreceiptorderlinking.linkeddocid  ";
                    innerjoin2 += " LEFT JOIN goodsreceiptlinking on purchaseorder.id = goodsreceiptlinking.linkeddocid  ";
                    whereclouse += " WHERE purchaseorderdetailcustomdata." + "col" + row[1] + " =? ";
                    params.add(row[0]);
                    
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Purchase_Order_ModuleId")) {
                        innerjoin += " INNER JOIN purchaseordercustomdata on purchaseordercustomdata.poID = purchaseorder.id ";
                        whereclouse += " AND purchaseordercustomdata.col" + jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleId"));
                    }
                    if(requestMap.containsKey("cogaAccount") && requestMap.get("cogaAccount")!=null && !(requestMap.get("cogaAccount").toString()).equals("")){
                        innerjoin += " INNER JOIN product on podetails.product = product.id ";
                        whereclouse += " AND IF(producttype='"+Constants.SERVICE+"', purchaseAccount= ? , cogsaccount=?) ";
                        params.add(requestMap.get("cogaAccount"));
                        params.add(requestMap.get("cogaAccount"));
                    }
                    whereclouse += " And purchaseorder.deleteflag = 'F' ";
                    whereclouse += " And purchaseorder.approvestatuslevel >= '11' ";
                    whereclouse += " And podetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    //ERP-42066
                    String linkingFlag = " And  ((polinking.moduleid <>?) OR polinking.moduleid IS NULL) ";
                    linkingFlag += " AND (SELECT COUNT(* )  from goodsreceiptorderlinking  as grol where docid IN(goodsreceiptorderlinking.docid)  AND  grol.moduleid=6 and sourceflag=0) = 0  ";
                    linkingFlag += " AND (select count(*) from goodsreceiptlinking as viLinking where viLinking.linkeddocid IN(purchaseorder.id))=0 ";
                    String groupby = " GROUP BY ponumber, podetails.id ";
                    params.add(Constants.Acc_Vendor_Invoice_ModuleId);
                    listso = executeSQLQuery("SELECT SUM(amount) FROM ("+query + innerjoin +innerjoin2+ whereclouse + linkingFlag + groupby+") as tbl", params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("purchaseOrderDetails", listso);
                    }
                    
                    // Purcahse Order - Expense
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    innerjoin2 = "";
                    whereclouse = "";
                    //ERP-41557
//                    query = "Select (if(expensepodetails.isdebit='T',expensepodetails.amount,(expensepodetails.amount*-1))/purchaseorder.externalcurrencyrate) as amount    from expensepodetailcustomdata ";
                    query = "Select (ROUND(if(expensepodetails.isdebit='T',expensepodetails.amount,(expensepodetails.amount*-1)),"+amountDigit+")/purchaseorder.externalcurrencyrate) as amount    from expensepodetailcustomdata ";
                    innerjoin =" Inner join expensepodetails on expensepodetails.id =expensepodetailcustomdata.expensepodetailid ";
                    innerjoin +=" INNER JOIN purchaseorder on purchaseorder.id = expensepodetails.purchaseorder ";
                    innerjoin2 += " LEFT JOIN polinking on purchaseorder.id = polinking.docid  ";
                    innerjoin2 += " LEFT JOIN goodsreceiptorderlinking on purchaseorder.id = goodsreceiptorderlinking.linkeddocid  ";
                    innerjoin2 += " LEFT JOIN goodsreceiptlinking on purchaseorder.id = goodsreceiptlinking.linkeddocid  ";
                    whereclouse += " WHERE expensepodetailcustomdata." + "col" + row[1] + " =? ";
                    params.add(row[0]);
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Purchase_Order_ModuleId")) {
                        innerjoin += " INNER JOIN purchaseordercustomdata on purchaseordercustomdata.poID = purchaseorder.id ";
                        whereclouse += " AND purchaseordercustomdata.col" + jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleId"));
                    }
                    if(requestMap.containsKey("cogaAccount") && requestMap.get("cogaAccount")!=null && !(requestMap.get("cogaAccount").toString()).equals("")){
                        whereclouse += " AND expensepodetails.account = ? ";
                        params.add(requestMap.get("cogaAccount"));
                    }
                    whereclouse += " And purchaseorder.deleteflag = 'F' ";
                    whereclouse += " And purchaseorder.approvestatuslevel >= '11' ";
                    whereclouse += " And expensepodetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    //ERP-42066
                    linkingFlag = " And  ((polinking.moduleid <>?) OR polinking.moduleid IS NULL) ";
                    linkingFlag += " AND (SELECT COUNT(* )  from goodsreceiptorderlinking  as grol where docid IN(goodsreceiptorderlinking.docid)  AND  grol .moduleid=6 and sourceflag=0) = 0  ";
                    linkingFlag += " AND (select count(*) from goodsreceiptlinking as viLinking where viLinking.linkeddocid IN(purchaseorder.id))=0 ";
                    groupby = " GROUP BY ponumber, expensepodetails.id ";
                    params.add(Constants.Acc_Vendor_Invoice_ModuleId);
                    listso = executeSQLQuery("SELECT SUM(amount) from ("+query + innerjoin + innerjoin2 + whereclouse + linkingFlag + groupby+") as tbl" , params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("expensePODetails", listso);
                    }
                }
                
                /*** Custom Data From Purchase Invoice ***/
                if ((short) row[2] == Constants.Acc_Vendor_Invoice_ModuleId) {//Done
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    whereclouse = "";
                    String linkingFlag = "";
                    //ERP-41557
//                    query = "Select sum((grdetails.rate*inventory.quantity  - IF((discount.id IS NOT NULL), IF(discount.inpercent='T',((grdetails.rate*inventory.quantity)*discount.discount/100),discount.discount),0))/exchangeratedetails.exchangerate) as amount    from accjedetailcustomdata  ";
                    query = "Select sum((ROUND(grdetails.rate*inventory.quantity,"+amountDigit+")  - ROUND(IF((discount.id IS NOT NULL), IF(discount.inpercent='T',((grdetails.rate*inventory.quantity)*discount.discount/100),discount.discount),0),"+amountDigit+"))/exchangeratedetails.exchangerate) as amount    from accjedetailcustomdata  ";
                    innerjoin += " INNER JOIN jedetail on accjedetailcustomdata.jedetailId = jedetail.id    ";
                    innerjoin += " Inner join grdetails on grdetails.id = accjedetailcustomdata.recdetailId  ";
                    innerjoin += " LEFT JOIN discount on discount.id = grdetails.discount  ";
                    innerjoin += " INNER JOIN inventory on grdetails.id = inventory.id  ";
                    innerjoin += " INNER JOIN goodsreceipt on goodsreceipt.id = grdetails.goodsreceipt  ";
                    innerjoin += " INNER JOIN exchangeratedetails on goodsreceipt.exchangeratedetail = exchangeratedetails.id ";
                    innerjoin += " INNER JOIN journalentry on journalentry.id = goodsreceipt.journalentry  ";
                    whereclouse += " WHERE accjedetailcustomdata." + "col" + row[1] + " =?   and jedetail.debit ='T' ";
                    params.add(row[0]);
                    
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Vendor_Invoice_ModuleId")) {
                        innerjoin += " INNER JOIN accjecustomdata on journalentry.accjecustomdataref = accjecustomdata.journalentryId ";
                        whereclouse += " AND accjecustomdata.col" + jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleId"));
                    }
                    if(requestMap.containsKey("cogaAccount") && requestMap.get("cogaAccount")!=null && !(requestMap.get("cogaAccount").toString()).equals("")){
                        innerjoin += " INNER JOIN product on inventory.product = product.id ";
                        whereclouse += " AND IF(producttype='"+Constants.SERVICE+"', purchaseAccount= ? , cogsaccount=?) ";
                        params.add(requestMap.get("cogaAccount"));
                        params.add(requestMap.get("cogaAccount"));
                    }
                    whereclouse += " And goodsreceipt.deleteflag = 'F' ";
                    whereclouse += " And goodsreceipt.approvestatuslevel >= '11' ";
                    whereclouse += " And grdetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    listso = executeSQLQuery(query + innerjoin + whereclouse + linkingFlag, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("goodsReceiptDetails", listso);
                    }
//                    params.remove(params.size()-1);
                    whereclouse += " And journalentry.entrydate >= ? ";
                    params.add(requestMap.get("startDate"));
                    whereclouse += " And journalentry.entrydate <= ? ";
                    params.add(requestMap.get("endDate"));
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("goodsReceiptDetailsDateFilter", listso);
                    }
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    whereclouse = "";
                    linkingFlag = "";
                    //ERP-41557
//                    query = "Select sum(if(expenseggrdetails.isdebit='T',expenseggrdetails.amount,(expenseggrdetails.amount*-1))/exchangeratedetails.exchangerate) as amount    from accjedetailcustomdata  ";
                    query = "Select sum(ROUND(if(expenseggrdetails.isdebit='T',expenseggrdetails.amount,(expenseggrdetails.amount*-1)),"+amountDigit+")/exchangeratedetails.exchangerate) as amount    from accjedetailcustomdata  ";
                    innerjoin += " INNER JOIN jedetail on accjedetailcustomdata.jedetailId = jedetail.id ";
                    innerjoin += " Inner join expenseggrdetails on expenseggrdetails.id = accjedetailcustomdata.recdetailId ";
                    innerjoin += " INNER JOIN goodsreceipt on goodsreceipt.id = expenseggrdetails.goodsreceipt ";
                    innerjoin += " INNER JOIN exchangeratedetails on goodsreceipt.exchangeratedetail = exchangeratedetails.id ";
                    innerjoin += " INNER JOIN journalentry on journalentry.id = goodsreceipt.journalentry ";
                    whereclouse += " WHERE accjedetailcustomdata." + "col" + row[1] + " =? ";//  and jedetail.debit ='T' ";
                    params.add(row[0]);
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Vendor_Invoice_ModuleId")) {
                        innerjoin += " INNER JOIN accjecustomdata on journalentry.accjecustomdataref = accjecustomdata.journalentryId ";
                        whereclouse += " AND accjecustomdata.col" + jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleId"));
                    }
                    if(requestMap.containsKey("cogaAccount") && requestMap.get("cogaAccount")!=null && !(requestMap.get("cogaAccount").toString()).equals("")){
                        whereclouse += " And expenseggrdetails.account = ? ";
                        params.add(requestMap.get("cogaAccount"));
                    }
                    whereclouse += " And goodsreceipt.deleteflag = 'F' ";
                    whereclouse += " And goodsreceipt.approvestatuslevel >= '11' ";
                    whereclouse += " And expenseggrdetails.company = ? ";
                    params.add(requestMap.get("companyid"));
                    listso = executeSQLQuery(query + innerjoin + whereclouse + linkingFlag, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("goodsReceiptExpenseDetails", listso);
                    }
//                    params.remove(params.size()-1);
                    whereclouse += " And journalentry.entrydate >= ? ";
                    params.add(requestMap.get("startDate"));
                    whereclouse += " And journalentry.entrydate <= ? ";
                    params.add(requestMap.get("endDate"));
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("goodsReceiptExpenseDetailsDateFilter", listso);
                    }
                }
                
                /*** Custom Data From Customer Quotation ***/
                if ((short) row[2] == Constants.Acc_Customer_Quotation_ModuleId) {//Done
                    params = new ArrayList();
                    query = "";
                    innerjoin = "";
                    whereclouse = "";
                    //ERP-41557
//                    query = " SELECT SUM((quotationdetails.rate*quotationdetails.quantity - IF(quotationdetails.discountispercent,((quotationdetails.rate*quotationdetails.quantity)*quotationdetails.discount/100),quotationdetails.discount))/quotation.externalcurrencyrate) as amount,SUM(quotationdetailsvendormapping.totalcost) as totalcost from quotationdetailscustomdata ";
                    query = " SELECT SUM((ROUND(quotationdetails.rate*quotationdetails.quantity,"+amountDigit+") - ROUND(IF(quotationdetails.discountispercent,((quotationdetails.rate*quotationdetails.quantity)*quotationdetails.discount/100),quotationdetails.discount),"+amountDigit+"))/quotation.externalcurrencyrate) as amount,SUM(quotationdetailsvendormapping.totalcost) as totalcost from quotationdetailscustomdata ";
                    innerjoin += " INNER JOIN quotationdetails ON quotationdetails .id = quotationdetailscustomdata.quotationdetailsid  ";
                    innerjoin += " INNER JOIN quotation ON quotationdetails .quotation = quotation.id  ";
                    innerjoin += " LEFT JOIN quotationdetailsvendormapping ON quotationdetails.id = quotationdetailsvendormapping.id  ";
                    whereclouse += " WHERE quotationdetailscustomdata.col" + row[1] + " =?";
                    params.add(row[0]);

                    innerjoin += " INNER JOIN quotationcustomdata ON quotation.accquotationcustomdataref = quotationcustomdata.quotationid ";
                    whereclouse += " AND quotationcustomdata.col" + globalCustomField.getString("Customer_Quotation_customfieldCol") + "=? ";
                    params.add(globalCustomField.getString("Customer_Quotation_customfieldId"));
                    
                    if (jobjGlobalCombo != null && jobjGlobalCombo.has("Acc_Customer_Quotation_ModuleId")) {
                        whereclouse += " AND quotationcustomdata.col" + jobjGlobalCombo.getString("Acc_Customer_Quotation_ModuleCol") + "=? ";
                        params.add(jobjGlobalCombo.getString("Acc_Customer_Quotation_ModuleId"));
                    }
                    if(requestMap.containsKey("cogaAccount") && requestMap.get("cogaAccount")!=null && !(requestMap.get("cogaAccount").toString()).equals("")){
                        innerjoin += "  INNER JOIN product on quotationdetails.product = product.id ";
                        whereclouse += " AND IF(producttype='"+Constants.SERVICE+"', purchaseAccount= ? , cogsaccount=?) ";
                        params.add(requestMap.get("cogaAccount"));
                        params.add(requestMap.get("cogaAccount"));
                    }
                    whereclouse += " And quotation.deleteflag = 'F' ";
                    whereclouse += " AND quotation.company =? ";
                    params.add(requestMap.get("companyid"));
                    whereclouse += " AND quotation.quotationdate>=? ";
                    params.add(requestMap.get("startDate"));
                    whereclouse += " AND quotation.quotationdate<=? ";
                    params.add(requestMap.get("endDate"));
                    whereclouse += " AND quotation.approvestatuslevel <'11' AND quotation.approvestatuslevel <>'-1'";// Not rejected and not approved 
                    listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());
                    if (listso != null && listso.size() > 0) {
                        jobj.put("quotationDetails", listso);
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    @Override
    public KwlReturnObject getFieldParams(String fieldname,int moduleid,String companyID) throws ServiceException{
            List params = new ArrayList();
            String hqlQuery = "SELECT distinct fp From FieldParams fp where fieldlabel =? AND company.companyID=?";
            params.add(fieldname);
            params.add(companyID);
            List listso = executeQuery(hqlQuery.toString(), params.toArray());

            return new KwlReturnObject(true, null, null, listso, listso.size());
    }
    @Override
    public List getFieldComboData(HashMap<String, Object> requestMap) throws ServiceException{
            List params = new ArrayList();
            String queryForColValue = " SELECT fieldcombodata.id,fieldparams.colnum,fieldparams.moduleid,fieldcombodata.value from fieldcombodata ";
            if(requestMap.get("linedimensionid")!=null){
                queryForColValue = " SELECT IF('"+requestMap.get("linedimensionid")+"'='1234','1234',fieldcombodata.id) as id,fieldparams.colnum,fieldparams.moduleid from fieldcombodata ";
            }
            queryForColValue +=  " INNER JOIN fieldparams on fieldcombodata.fieldid = fieldparams.id "
            + " WHERE fieldparams.moduleid IN (?,?,?,?,?) AND customfield=?";
            params.add(Constants.Acc_Sales_Order_ModuleId);
            params.add(Constants.Acc_Purchase_Order_ModuleId);
            params.add(Constants.Acc_Vendor_Invoice_ModuleId);
            params.add(Constants.Acc_Invoice_ModuleId);
            params.add(Constants.Acc_Customer_Quotation_ModuleId);
            params.add(requestMap.get("customfield"));
            if(requestMap.containsKey("customfieldName")){
                queryForColValue += " AND fieldparams.fieldlabel = ? ";
                params.add(requestMap.get("customfieldName"));
            }
            if(requestMap.containsKey("dimension")){
                queryForColValue += " AND fieldcombodata.value = ? ";
                params.add(requestMap.get("dimension"));
            }
            List listComboso = executeSQLQuery(queryForColValue, params.toArray());
            
            return listComboso;
    }
    @Override
    public JSONObject getMasterItemsDimension(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject object = new JSONObject();
        try {
            KwlReturnObject result;
            ArrayList filter_params = new ArrayList();
            List resultlist = new ArrayList();
            int level = 0;
            FieldComboData fld = new FieldComboData();
            fld.setId("1234");
            fld.setValue("None");
            Object tmplist1[] = new Object[4];
            tmplist1[0] = fld;
            tmplist1[1] = level;
            
            /** For System Defined None Type Dimension -- Start **/
            HashMap<String, Object> requestLineMap1 = new HashMap<String, Object>();
            requestLineMap1.put("dimension", "None");
            requestLineMap1.put("linedimensionid", "1234");
            requestLineMap1.put("customfield", "0");
            List listMstrCmbDDate = getFieldComboData(requestLineMap1);
            tmplist1[2] = listMstrCmbDDate;
            resultlist.add(tmplist1);
            /** For System Defined None Type Dimension -- End **/
            
            filter_params = (ArrayList) requestParams.get("filter_params");
            String query = "from FieldComboData where field.id =  ?  and parent is null order by itemsequence,value asc";
            List list = executeQuery(query, filter_params.toArray());
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object listObj = itr.next();
                FieldComboData fieldComboData = (FieldComboData) listObj;
                Object tmplist[] = new Object[5];
                tmplist[0] = listObj;
                tmplist[1] = level;
                HashMap<String, Object> requestLineMap = new HashMap<String, Object>();
                requestLineMap.put(query, listObj);
                requestLineMap.put("dimension", fieldComboData.getValue());
                requestLineMap.put("linedimensionid", fieldComboData.getId());
                requestLineMap.put("customfield", "0");
                List listMstrCmbDDate1 = getFieldComboData(requestLineMap); // Find Each Dimension's Combo Value, Column Number and its Ids Which is used in outer function (BudgetVsCostReport and AcutalVsBudgetReport)
                tmplist[2] = listMstrCmbDDate1;
                resultlist.add(tmplist);
            }
            result = new KwlReturnObject(true, null, null, resultlist, resultlist.size());
            object.put("kwlReturnObject", result);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }
    @Override
     public JSONObject getForecastingReportDetails(HashMap<String, Object> requestMap) throws ServiceException {
        JSONObject object = new JSONObject();
        try {
            
            //** Purchase Invoice Details**//
            JSONObject jobjGlobalCombo = requestMap.containsKey("globalCombodata") ? (JSONObject) requestMap.get("globalCombodata") : null;
            JSONObject globalCustomField = requestMap.containsKey("globalCustomField") ? (JSONObject) requestMap.get("globalCustomField") : null;
            //ERP-41557
            int amountDigit = Constants.AMOUNT_DIGIT_AFTER_DECIMAL;
            if (requestMap.containsKey(Constants.amountdecimalforcompany) && requestMap.get(Constants.amountdecimalforcompany) != null) {
                amountDigit = (Integer) requestMap.get(Constants.amountdecimalforcompany);
            }
            List params = new ArrayList();
            String datefilter="";
            String query = "";
            String innerjoin = "";
            String whereclouse = " WHERE ";
            String linkingFlag = "";
            //ERP-41557
//            query = "Select SUM((grdetails.rate*inventory.quantity  - IF((discount.id IS NOT NULL), IF(discount.inpercent='T',((grdetails.rate*inventory.quantity)*discount.discount/100),discount.discount),0))/exchangeratedetails.exchangerate) as amount    from goodsreceipt  ";
            query = "Select SUM((ROUND(grdetails.rate*inventory.quantity,"+amountDigit+")  - ROUND(IF((discount.id IS NOT NULL), IF(discount.inpercent='T',((grdetails.rate*inventory.quantity)*discount.discount/100),discount.discount),0),"+amountDigit+"))/exchangeratedetails.exchangerate) as amount    from goodsreceipt  ";
            innerjoin += " INNER JOIN grdetails on goodsreceipt.id = grdetails.goodsreceipt  ";
            innerjoin += " LEFT JOIN discount on discount.id = grdetails.discount  ";
            innerjoin += " INNER JOIN inventory on grdetails.id = inventory.id  ";
            innerjoin += " INNER JOIN exchangeratedetails on goodsreceipt.exchangeratedetail = exchangeratedetails.id ";
            innerjoin += " INNER JOIN journalentry on journalentry.id = goodsreceipt.journalentry  ";
            innerjoin += " INNER JOIN accjecustomdata on journalentry.accjecustomdataref = accjecustomdata.journalentryId ";
            innerjoin += " INNER JOIN product on inventory.product = product.id ";
            whereclouse += " accjecustomdata.col" + jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleId"));
            whereclouse += " AND IF(producttype='" + Constants.SERVICE + "', purchaseAccount= ? , cogsaccount=?) ";
            params.add(requestMap.get("account"));
            params.add(requestMap.get("account"));
            whereclouse += " And goodsreceipt.deleteflag = 'F' ";
            whereclouse += " And goodsreceipt.approvestatuslevel >= '11' ";
            whereclouse += " And grdetails.company = ? ";
            params.add(requestMap.get("companyid"));
            List listso = executeSQLQuery(query + innerjoin + whereclouse + linkingFlag, params.toArray());// Without date filter
            if (listso != null && listso.size() > 0) {
                object.put("goodsReceiptDetails", listso);
            }
            whereclouse += " And journalentry.entrydate >= ? ";
            params.add(requestMap.get("startDate"));
            whereclouse += " And journalentry.entrydate <= ? ";
            params.add(requestMap.get("endDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());// Withdate filter
            if (listso != null && listso.size() > 0) {
                object.put("goodsReceiptDetailsDateFilter", listso);
            }
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("firstdateofyear"));
            params.add(requestMap.get("currentdate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());// Start date of Year to current date
            if (listso != null && listso.size() > 0) {
                object.put("goodsReceiptDetailsYTD", listso);
            }
            
            //** Purchase Invoice Expense Details **//
            params = new ArrayList();
            query = "";
            innerjoin = "";
            whereclouse = " WHERE ";
            linkingFlag = "";
            //ERP-41557
//            query = "Select sum(if(expenseggrdetails.isdebit='T',expenseggrdetails.amount,(expenseggrdetails.amount*-1))/exchangeratedetails.exchangerate) as amount from goodsreceipt  ";
            query = "Select sum(ROUND(if(expenseggrdetails.isdebit='T',expenseggrdetails.amount,(expenseggrdetails.amount*-1))/exchangeratedetails.exchangerate,"+amountDigit+")) as amount from goodsreceipt  ";
            innerjoin += " INNER JOIN expenseggrdetails on goodsreceipt.id = expenseggrdetails.goodsreceipt ";
            innerjoin += " INNER JOIN exchangeratedetails on goodsreceipt.exchangeratedetail = exchangeratedetails.id ";
            innerjoin += " INNER JOIN journalentry on journalentry.id = goodsreceipt.journalentry ";
            innerjoin += " INNER JOIN accjecustomdata on journalentry.accjecustomdataref = accjecustomdata.journalentryId ";
            whereclouse += " accjecustomdata.col" + jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Vendor_Invoice_ModuleId"));
            whereclouse += " And expenseggrdetails.account = ? ";
            params.add(requestMap.get("account"));
            whereclouse += " And goodsreceipt.deleteflag = 'F' ";
            whereclouse += " And goodsreceipt.approvestatuslevel >= '11' ";
            whereclouse += " And expenseggrdetails.company = ? ";
            params.add(requestMap.get("companyid")); 
            listso = executeSQLQuery(query + innerjoin + whereclouse + linkingFlag, params.toArray());// Without date filter
            if (listso != null && listso.size() > 0) {
                object.put("goodsReceiptExpenseDetails", listso);
            }
            whereclouse += " And journalentry.entrydate >= ? ";
            params.add(requestMap.get("startDate"));
            whereclouse += " And journalentry.entrydate <= ? ";
            params.add(requestMap.get("endDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());// Withdate filter
            if (listso != null && listso.size() > 0) {
                object.put("goodsReceiptExpenseDetailsDateFilter", listso);
            }
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("firstdateofyear"));
            params.add(requestMap.get("currentdate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse, params.toArray());// Start date of Year to current date
            if (listso != null && listso.size() > 0) {
                object.put("goodsReceiptExpenseDetailsYTD", listso);
            }
            //** Purcahse order Details **//
            params = new ArrayList();
            query = "";
            innerjoin = "";
            String innerjoin2 = "";
            whereclouse = " where ";
            linkingFlag = "";
            //ERP-41557
//            query = "Select ((podetails.rate*podetails.quantity - IF(podetails.discountispercent,((podetails.rate*podetails.quantity)*podetails.discount/100),podetails.discount))/purchaseorder.externalcurrencyrate) as amount    from purchaseorder  ";
            query = "Select ((ROUND(podetails.rate*podetails.quantity,"+amountDigit+") - ROUND(IF(podetails.discountispercent,((podetails.rate*podetails.quantity)*podetails.discount/100),podetails.discount),"+amountDigit+"))/purchaseorder.externalcurrencyrate) as amount    from purchaseorder  ";
            innerjoin += " INNER JOIN podetails on purchaseorder.id = podetails.purchaseorder  ";
            innerjoin2 += " LEFT JOIN polinking on purchaseorder.id = polinking.docid  ";
            innerjoin2 += " LEFT JOIN goodsreceiptorderlinking on purchaseorder.id = goodsreceiptorderlinking.linkeddocid  ";
            innerjoin2 += " LEFT JOIN goodsreceiptlinking on purchaseorder.id = goodsreceiptlinking.linkeddocid  ";
            innerjoin += " INNER JOIN purchaseordercustomdata on purchaseordercustomdata.poID = purchaseorder.id ";
            whereclouse += " purchaseordercustomdata.col" + jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleId"));
            innerjoin += " INNER JOIN product on podetails.product = product.id ";
            whereclouse += " AND IF(producttype='" + Constants.SERVICE + "', purchaseAccount= ? , cogsaccount=?) ";
            params.add(requestMap.get("account"));
            params.add(requestMap.get("account"));
            whereclouse += " And purchaseorder.deleteflag = 'F' ";
            whereclouse += " And purchaseorder.approvestatuslevel >= '11' ";
            whereclouse += " And podetails.company = ? ";
            params.add(requestMap.get("companyid"));
            //ERP-42066
            linkingFlag = " And  ((polinking.moduleid <>?) OR polinking.moduleid IS NULL) ";
            params.add(Constants.Acc_Vendor_Invoice_ModuleId);
            linkingFlag += " AND (SELECT COUNT(* )  from goodsreceiptorderlinking  as grol where docid IN(goodsreceiptorderlinking.docid)  AND  grol.moduleid=6 and sourceflag=0) = 0  ";
            linkingFlag += " AND (select count(*) from goodsreceiptlinking as viLinking where viLinking.linkeddocid IN(purchaseorder.id))=0 ";
            String groupby = " GROUP BY ponumber, podetails.id ";
            listso = executeSQLQuery("SELECT SUM(amount) FROM ("+query + innerjoin +innerjoin2+ whereclouse + linkingFlag + groupby+") as tbl", params.toArray());
            if (listso != null && listso.size() > 0) {
                object.put("purchaseOrderDetails", listso);
            }
            params.remove(params.size() - 1);
            //ERP-41557
//            query = "Select sum((podetails.rate*podetails.quantity - IF(podetails.discountispercent,((podetails.rate*podetails.quantity)*podetails.discount/100),podetails.discount))/purchaseorder.externalcurrencyrate) as amount    from purchaseorder  ";
            query = "Select sum((ROUND(podetails.rate*podetails.quantity,"+amountDigit+") - ROUND(IF(podetails.discountispercent,((podetails.rate*podetails.quantity)*podetails.discount/100),podetails.discount),"+amountDigit+"))/purchaseorder.externalcurrencyrate) as amount    from purchaseorder  ";
            datefilter = " AND purchaseorder.orderdate>=? ";
            params.add(requestMap.get("startDate"));
            datefilter += " AND purchaseorder.orderdate<=? ";
            params.add(requestMap.get("endDate"));
            // Current Month PO
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("currentMonthStartDate"));
            params.add(requestMap.get("currentMonthEndDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Current Month Data
            if (listso != null && listso.size() > 0) {
                object.put("purchaseOrderDetails_CurrentMonth", listso);
            }
            //Last Month PO
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("lastMonthStartDate"));
            params.add(requestMap.get("lastMonthEndDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Last Month Data
            if (listso != null && listso.size() > 0) {
                object.put("purchaseOrderDetails_LastMonth", listso);
            }
            

            // Purcahse Order - Expense
            params = new ArrayList();
            query = "";
            innerjoin = "";
            innerjoin2 = "";
            whereclouse = " where ";
            //ERP-41557
//            query = "Select (ROUND(if(expensepodetails.isdebit='T',expensepodetails.amount,(expensepodetails.amount*-1))/purchaseorder.externalcurrencyrate,"+amountDigit+")) as amount    from purchaseorder ";
            query = "Select (ROUND(if(expensepodetails.isdebit='T',expensepodetails.amount,(expensepodetails.amount*-1)),"+amountDigit+")/purchaseorder.externalcurrencyrate) as amount    from purchaseorder ";
            innerjoin += " INNER JOIN expensepodetails on purchaseorder.id = expensepodetails.purchaseorder ";
            innerjoin2 += " LEFT JOIN polinking on purchaseorder.id = polinking.docid  ";
            innerjoin2 += " LEFT JOIN goodsreceiptorderlinking on purchaseorder.id = goodsreceiptorderlinking.linkeddocid  ";
            innerjoin2 += " LEFT JOIN goodsreceiptlinking on purchaseorder.id = goodsreceiptlinking.linkeddocid  ";
            innerjoin += " INNER JOIN purchaseordercustomdata on purchaseordercustomdata.poID = purchaseorder.id ";
            whereclouse += " purchaseordercustomdata.col" + jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Purchase_Order_ModuleId"));
            whereclouse += " AND expensepodetails.account = ? ";
            params.add(requestMap.get("account"));
            whereclouse += " And purchaseorder.deleteflag = 'F' ";
            whereclouse += " And purchaseorder.approvestatuslevel >= '11' ";
            whereclouse += " And expensepodetails.company = ? ";
            params.add(requestMap.get("companyid"));
             //ERP-42066
            linkingFlag = " And  ((polinking.moduleid <>?) OR polinking.moduleid IS NULL) ";
            params.add(Constants.Acc_Vendor_Invoice_ModuleId);
            linkingFlag += " AND (SELECT COUNT(* )  from goodsreceiptorderlinking  as grol where docid IN(goodsreceiptorderlinking.docid)  AND  grol .moduleid=6 and sourceflag=0) = 0  ";
            linkingFlag += " AND (select count(*) from goodsreceiptlinking as viLinking where viLinking.linkeddocid IN(purchaseorder.id))=0 ";
            groupby = " GROUP BY ponumber, expensepodetails.id ";
            listso = executeSQLQuery("SELECT SUM(amount) from ("+query + innerjoin + innerjoin2 + whereclouse + linkingFlag + groupby+") as tbl" , params.toArray());
            if (listso != null && listso.size() > 0) {
                object.put("expensePODetails", listso);
            }
            params.remove(params.size() - 1);
            //ERP-41557
//            query = "Select sum(ROUND(if(expensepodetails.isdebit='T',expensepodetails.amount,(expensepodetails.amount*-1))/purchaseorder.externalcurrencyrate,"+amountDigit+")) as amount    from purchaseorder ";
            query = "Select sum(ROUND(if(expensepodetails.isdebit='T',expensepodetails.amount,(expensepodetails.amount*-1)),"+amountDigit+")/purchaseorder.externalcurrencyrate) as amount    from purchaseorder ";
            datefilter = " AND purchaseorder.orderdate>=? ";
            params.add(requestMap.get("startDate"));
            datefilter += " AND purchaseorder.orderdate<=? ";
            params.add(requestMap.get("endDate"));
//            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// With date filter
//            if (listso != null && listso.size() > 0) {
//                object.put("expensePODetails_DateFilter", listso);
//            }
            // Current Month PO
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("currentMonthStartDate"));
            params.add(requestMap.get("currentMonthEndDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Current Month Data
            if (listso != null && listso.size() > 0) {
                object.put("expensePODetails_CurrentMonth", listso);
            }
            //Last Month PO
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("lastMonthStartDate"));
            params.add(requestMap.get("lastMonthEndDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Last Month Data
            if (listso != null && listso.size() > 0) {
                object.put("expensePODetails_LastMonth", listso);
            }
            
            //** Sales Order Details **//
            params = new ArrayList();
            query = "";
            innerjoin = "";
            whereclouse = "";
            //ERP-41557
//            query = "Select sum((sodetails.rate*sodetails.quantity - IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount))/salesorder.externalcurrencyrate) as amount, SUM(totalcost) as totalcost from salesorder ";
            query = "Select sum((ROUND(sodetails.rate*sodetails.quantity,"+amountDigit+") -ROUND(IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount),"+amountDigit+"))/salesorder.externalcurrencyrate) as amount, SUM(totalcost) as totalcost from salesorder ";
            innerjoin += " INNER JOIN sodetails on salesorder.id = sodetails.salesorder ";
            innerjoin += " LEFT join sodetailsvendormapping on sodetailsvendormapping.id = sodetails.id ";
            innerjoin += " INNER JOIN salesordercustomdata on salesordercustomdata.soID = salesorder.id ";
            whereclouse += " AND salesordercustomdata.col" + jobjGlobalCombo.getString("Acc_Sales_Order_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Sales_Order_ModuleId"));
            innerjoin += " INNER JOIN product on sodetails.product = product.id ";
            whereclouse += " AND IF(producttype='" + Constants.SERVICE + "', purchaseAccount= ? , cogsaccount=?) ";
            params.add(requestMap.get("account"));
            params.add(requestMap.get("account"));

            whereclouse += " And salesorder.approvestatuslevel >= '11' ";
            whereclouse += " And salesorder.deleteflag = 'F' ";
            whereclouse += " And sodetails.company = ? ";
            params.add(requestMap.get("companyid"));
            whereclouse += " And salesorder.isdraft = 0 ";
            datefilter = " And salesorder.orderdate >= ? ";
            params.add(requestMap.get("startDate"));
            datefilter += " And salesorder.orderdate <= ? ";
            params.add(requestMap.get("endDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Wih Date Filter
            if (listso != null && listso.size() > 0) {
                object.put("salesOrderDetails", listso);
            }
            String querytax = "Select salesorderdetailtermmap.termamount,linelevelterms.term from salesorder ";  
            String innerjoinTax = " INNER join salesorderdetailtermmap on salesorderdetailtermmap.salesorderdetail = sodetails.id ";
            innerjoinTax += " INNER join linelevelterms on salesorderdetailtermmap.term = linelevelterms.id ";
            listso = executeSQLQuery(querytax + innerjoin + innerjoinTax + whereclouse + datefilter + "ORDER BY linelevelterms.termsequence ", params.toArray());// Line Level Output Tax - GST
            if (listso != null && listso.size() > 0) {
                object.put("salesOrderDetailsTax", listso);
            }
            // Current Month SO
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("currentMonthStartDate"));
            params.add(requestMap.get("currentMonthEndDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray()); // Current Month Details
            if (listso != null && listso.size() > 0) {
                object.put("salesOrderDetails_CurrentMonth", listso);
            }
            //Last Month SO
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("lastMonthStartDate"));
            params.add(requestMap.get("lastMonthEndDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Last Month Details
            if (listso != null && listso.size() > 0) {
                object.put("salesOrderDetails_LastMonth", listso);
            }
            
            /*** Custom Data From Customer Quotation ***/
            params = new ArrayList();
            query = "";
            innerjoin = "";
            whereclouse = " where ";
            linkingFlag = "";
            //ERP-41557
//            query = " SELECT SUM((quotationdetails.rate*quotationdetails.quantity - IF(quotationdetails.discountispercent,((quotationdetails.rate*quotationdetails.quantity)*quotationdetails.discount/100),quotationdetails.discount))/quotation.externalcurrencyrate) as amount,SUM(quotationdetailsvendormapping.totalcost) as totalcost from quotation ";
            query = " SELECT SUM((ROUND(quotationdetails.rate*quotationdetails.quantity,"+amountDigit+") -ROUND(IF(quotationdetails.discountispercent,((quotationdetails.rate*quotationdetails.quantity)*quotationdetails.discount/100),quotationdetails.discount),"+amountDigit+"))/quotation.externalcurrencyrate) as amount,SUM(quotationdetailsvendormapping.totalcost) as totalcost from quotation ";
            innerjoin += " INNER JOIN quotationdetails ON quotationdetails .quotation = quotation.id  ";
            innerjoin += " LEFT JOIN quotationdetailsvendormapping ON quotationdetails.id = quotationdetailsvendormapping.id  ";
            innerjoin += " INNER JOIN quotationcustomdata ON quotation.accquotationcustomdataref = quotationcustomdata.quotationid ";
            whereclouse += "  quotationcustomdata.col" + globalCustomField.getString("Customer_Quotation_customfieldCol") + "=? ";
            params.add(globalCustomField.getString("Customer_Quotation_customfieldId"));
            whereclouse += " AND quotationcustomdata.col" + jobjGlobalCombo.getString("Acc_Customer_Quotation_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Customer_Quotation_ModuleId"));
            innerjoin += "  INNER JOIN product on quotationdetails.product = product.id ";
            whereclouse += " AND IF(producttype='" + Constants.SERVICE + "', purchaseAccount= ? , cogsaccount=?) ";
            params.add(requestMap.get("account"));
            params.add(requestMap.get("account"));
            whereclouse += " And quotation.deleteflag = 'F' ";
            whereclouse += " AND quotation.company =? ";
            params.add(requestMap.get("companyid"));
            datefilter = " AND quotation.quotationdate>=? ";
            params.add(requestMap.get("startDate"));
            datefilter += " AND quotation.quotationdate<=? ";
            params.add(requestMap.get("endDate"));
            whereclouse += " AND quotation.approvestatuslevel <'11' AND quotation.approvestatuslevel <>'-1'";// Not rejected and not approved 
            whereclouse += " AND quotation.isdraft=0 ";
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// With Date Filter
            if (listso != null && listso.size() > 0) {
                object.put("quotationDetails", listso);
            }
            querytax = "Select quotationdetailtermmap.termamount,linelevelterms.term from quotation ";  
            innerjoinTax = " INNER join quotationdetailtermmap on quotationdetailtermmap.quotationdetail = quotationdetails.id ";
            innerjoinTax += " INNER join linelevelterms on quotationdetailtermmap.term = linelevelterms.id ";
            listso = executeSQLQuery(querytax + innerjoin + innerjoinTax + whereclouse + datefilter + "ORDER BY linelevelterms.termsequence ", params.toArray());// Line Level Output Tax - GST
            if (listso != null && listso.size() > 0) {
                object.put("quotationDetailsTax", listso);
            }
            // Current Month SO
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("currentMonthStartDate"));
            params.add(requestMap.get("currentMonthEndDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Current Month Data
            if (listso != null && listso.size() > 0) {
                object.put("quotationDetails_CurrentMonth", listso);
            }
            //Last Month SO
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("lastMonthStartDate"));
            params.add(requestMap.get("lastMonthEndDate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Last Month Data
            if (listso != null && listso.size() > 0) {
                object.put("quotationDetails_LastMonth", listso);
            }
        } catch (JSONException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }                
    @Override
    public JSONObject getChangeOrderStatusReportDetails(HashMap<String, Object> requestMap) throws ServiceException {
        JSONObject object = new JSONObject();
        try {
            JSONObject jobjGlobalCombo = requestMap.containsKey("globalCombodata") ? (JSONObject) requestMap.get("globalCombodata") : null;
            JSONObject globalCustomField = requestMap.containsKey("globalCustomField") ? (JSONObject) requestMap.get("globalCustomField") : null;
            JSONObject quotesStatusField = requestMap.containsKey("quotesStatusField") ? (JSONObject) requestMap.get("quotesStatusField") : null;
            JSONObject globalCustomFieldPCIType = requestMap.containsKey("globalCustomFieldPCIType") ? (JSONObject) requestMap.get("globalCustomFieldPCIType") : null;
            //ERP-41557
            int amountDigit = Constants.AMOUNT_DIGIT_AFTER_DECIMAL;
            if (requestMap.containsKey(Constants.amountdecimalforcompany) && requestMap.get(Constants.amountdecimalforcompany) != null) {
                amountDigit = (Integer) requestMap.get(Constants.amountdecimalforcompany);
            }
            
            List params = new ArrayList();
            String datefilter="";
            String query = "";
            String innerjoin = "";
            String whereclouse = " WHERE ";
            
            //** Sales Order Details For Box 2 **//
            //ERP-41557
//            query = "Select SUM((sodetails.rate*sodetails.quantity - IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount))/salesorder.externalcurrencyrate) as amount, SUM(totalcost) from salesorder ";
            query = "Select SUM((ROUND(sodetails.rate*sodetails.quantity,"+amountDigit+") - ROUND(IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount),"+amountDigit+"))/salesorder.externalcurrencyrate) as amount, SUM(totalcost) from salesorder ";
            innerjoin += " INNER JOIN sodetails on salesorder.id = sodetails.salesorder ";
            innerjoin += " LEFT join sodetailsvendormapping on sodetailsvendormapping.id = sodetails.id ";
            innerjoin += " INNER JOIN salesordercustomdata on salesordercustomdata.soID = salesorder.id ";
            whereclouse += " salesordercustomdata.col" + jobjGlobalCombo.getString("Acc_Sales_Order_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Sales_Order_ModuleId"));
            innerjoin += " INNER JOIN product on sodetails.product = product.id ";
            whereclouse += " And salesorder.approvestatuslevel >= '11' ";
            whereclouse += " And salesorder.deleteflag = 'F' ";
            whereclouse += " And sodetails.company = ? ";
            params.add(requestMap.get("companyid"));
            whereclouse += " And salesorder.isdraft = 0 ";
            datefilter = " GROUP BY (salesorder.id) ORDER BY salesorder.orderdate, salesorder.createdon ASC LIMIT 1 ";
            List listso  = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray()); // First Sales Order Related to Project Code
            if (listso != null && listso.size() > 0) {
                object.put("salesOrderDetails_FST", listso);
            }
            //ERP-41557
//            query = "Select sum((sodetails.rate*sodetails.quantity - IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount))/salesorder.externalcurrencyrate) as amount, COUNT(DISTINCT(salesorder.id))as count from salesorder ";
            query = "Select sum((ROUND(sodetails.rate*sodetails.quantity,"+amountDigit+") -ROUND(IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount),"+amountDigit+") )/salesorder.externalcurrencyrate) as amount, COUNT(DISTINCT(salesorder.id))as count from salesorder ";
            whereclouse += " AND salesordercustomdata.col" + globalCustomField.getString("Sales_Order_customfieldCol") + "=? ";
            params.add(globalCustomField.getString("Sales_Order_customfieldId"));
            datefilter = " And salesorder.orderdate >= ? ";
            params.add(requestMap.get("startDate"));
            datefilter += " And salesorder.orderdate <= ? ";
            params.add(requestMap.get("endDate"));
            listso  = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Total Sales Order Details and Number of count of Sales order.
            if (listso != null && listso.size() > 0) {
                object.put("salesOrderDetails_SUMCOUNT", listso);
            }
            
            params = new ArrayList();
            query = "";
            innerjoin = "";
            whereclouse = " where ";
            //ERP-41557
//            query = "Select SUM((((sodetails.rate*sodetails.quantity)/salesorder.externalcurrencyrate) -  (IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount)/salesorder.externalcurrencyrate))- totalcost) as amount from salesorder ";
            query = "Select SUM((ROUND(((sodetails.rate*sodetails.quantity)/salesorder.externalcurrencyrate),"+amountDigit+") - ROUND((IF(sodetails.discountispercent,((sodetails.rate*sodetails.quantity)*sodetails.discount/100),sodetails.discount)/salesorder.externalcurrencyrate),"+amountDigit+"))- totalcost) as amount from salesorder ";
            innerjoin += " INNER JOIN sodetails on salesorder.id = sodetails.salesorder ";
            innerjoin += " LEFT join sodetailsvendormapping on sodetailsvendormapping.id = sodetails.id ";
            innerjoin += " INNER JOIN salesordercustomdata on salesordercustomdata.soID = salesorder.id ";
            whereclouse += " salesordercustomdata.col" + jobjGlobalCombo.getString("Acc_Sales_Order_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Sales_Order_ModuleId"));
            innerjoin += " INNER JOIN product on sodetails.product = product.id ";
            whereclouse += " And salesorder.approvestatuslevel >= '11' ";
            whereclouse += " And salesorder.deleteflag = 'F' ";
            whereclouse += " And sodetails.company = ? ";
            params.add(requestMap.get("companyid"));
            whereclouse += " And salesorder.isdraft = 0 ";
            datefilter = " And salesorder.orderdate >= ? ";
            params.add(requestMap.get("startDate"));
            datefilter += " And salesorder.orderdate <= ? ";
            params.add(requestMap.get("endDate"));
            listso  = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Total Of Profit Margin
            if (listso != null && listso.size() > 0) {
                object.put("salesOrderDetails_PROFIT", listso);
            }
            
            //** Quotation Details **//
            params = new ArrayList();
            query = "";
            innerjoin = "";
            String groupby = "";
            whereclouse = " where ";
            //ERP-41557
//            query = " SELECT SUM((quotationdetails.rate*quotationdetails.quantity - IF(quotationdetails.discountispercent,((quotationdetails.rate*quotationdetails.quantity)*quotationdetails.discount/100),quotationdetails.discount))/quotation.externalcurrencyrate) as amount,SUM(quotationdetailsvendormapping.totalcost) as totalcost, COUNT(DISTINCT(quotation.id))as count,quotationcustomdata.col" + quotesStatusField.getString("QuotationStatusCol") + " from quotation ";
            query = " SELECT SUM((ROUND(quotationdetails.rate*quotationdetails.quantity,"+amountDigit+") - ROUND(IF(quotationdetails.discountispercent,((quotationdetails.rate*quotationdetails.quantity)*quotationdetails.discount/100),quotationdetails.discount),"+amountDigit+"))/quotation.externalcurrencyrate) as amount,SUM(quotationdetailsvendormapping.totalcost) as totalcost, COUNT(DISTINCT(quotation.id))as count,quotationcustomdata.col" + quotesStatusField.getString("QuotationStatusCol") + " from quotation ";
            innerjoin += " INNER JOIN quotationdetails ON quotationdetails .quotation = quotation.id  ";
            innerjoin += " LEFT JOIN quotationdetailsvendormapping ON quotationdetails.id = quotationdetailsvendormapping.id  ";
            innerjoin += " INNER JOIN quotationcustomdata ON quotation.accquotationcustomdataref = quotationcustomdata.quotationid ";
            whereclouse += "  quotationcustomdata.col" + globalCustomField.getString("Customer_Quotation_customfieldCol") + "=? ";
            params.add(globalCustomField.getString("Customer_Quotation_customfieldId"));
            whereclouse += " AND quotationcustomdata.col" + jobjGlobalCombo.getString("Acc_Customer_Quotation_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Customer_Quotation_ModuleId"));
            whereclouse += " AND quotationcustomdata.col" + globalCustomFieldPCIType.getString("Customer_Quotation_PCI_customfieldCol") + "=? ";
            params.add(globalCustomFieldPCIType.getString("Customer_Quotation_PCI_customfieldId"));
            whereclouse += " And quotation.deleteflag = 'F' ";
            whereclouse += " AND quotation.company =? ";
            params.add(requestMap.get("companyid"));
            datefilter = " AND quotation.quotationdate>=? ";
            params.add(requestMap.get("startDate"));
            datefilter += " AND quotation.quotationdate<=? ";
            params.add(requestMap.get("endDate"));
            whereclouse += " AND quotationcustomdata.col6 IS NOT NULL AND quotationcustomdata.col6 <>''";
            whereclouse += " AND quotation.isdraft=0 ";
            groupby += " GROUP BY (quotationcustomdata.col" + quotesStatusField.getString("QuotationStatusCol") + ")";
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter +groupby, params.toArray());// Total Amount of Quotation based on Custom field of Quotation Status and its number of Documents
            if (listso != null && listso.size() > 0) {
                object.put("quotationstatusDetails", listso);
            }
            
            params = new ArrayList();
            query = "";
            innerjoin = "";
            whereclouse = "";
            //ERP-41557
//            query = "Select SUM(invoicedetails.rate*inventory.quantity  -  IF((discount.id IS NOT NULL),IF(discount.inpercent='T',((invoicedetails.rate*inventory.quantity)*discount.discount/100),discount.discount),0)/exchangeratedetails.exchangerate) as amount, SUM(rowtermamount) as taxamount,SUM(invoice.invoiceamountdueinbase)   from accjedetailcustomdata  ";
            query = "Select SUM(ROUND(invoicedetails.rate*inventory.quantity,"+amountDigit+")  - ROUND(IF((discount.id IS NOT NULL),IF(discount.inpercent='T',((invoicedetails.rate*inventory.quantity)*discount.discount/100),discount.discount),0),"+amountDigit+")/exchangeratedetails.exchangerate) as amount, SUM(rowtermamount) as taxamount,SUM(invoice.invoiceamountdueinbase)   from accjedetailcustomdata  ";
            innerjoin += " INNER JOIN jedetail on accjedetailcustomdata.jedetailId = jedetail.id    ";
            innerjoin += " Inner join invoicedetails on invoicedetails.id = accjedetailcustomdata.recdetailId  ";
            innerjoin += " LEFT JOIN discount on discount.id = invoicedetails.discount   ";
            innerjoin += " INNER JOIN inventory on invoicedetails.id = inventory.id  ";
            innerjoin += " INNER JOIN invoice on invoice.id = invoicedetails.invoice  ";
            innerjoin += " INNER JOIN exchangeratedetails on invoice.exchangeratedetail = exchangeratedetails.id ";
            innerjoin += " INNER JOIN journalentry on journalentry.id = invoice.journalentry  ";
            whereclouse += " WHERE jedetail.debit ='F' ";
            innerjoin += " INNER JOIN accjecustomdata on journalentry.accjecustomdataref = accjecustomdata.journalentryId ";
            whereclouse += " AND accjecustomdata.col" + jobjGlobalCombo.getString("Acc_Invoice_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Invoice_ModuleId"));
            whereclouse += " And invoicedetails.company = ? ";
            params.add(requestMap.get("companyid"));
            datefilter = " And journalentry.entrydate >= ? ";
            params.add(requestMap.get("startDate"));
            datefilter += " And journalentry.entrydate <= ? ";
            params.add(requestMap.get("endDate"));
            whereclouse += " And invoice.deleteflag = 'F' ";
            whereclouse += " And invoice.approvestatuslevel >= '11' ";
            whereclouse += " And invoice.isdraft=0 ";
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());
            if (listso != null && listso.size() > 0) {
                object.put("invoiceBillingDetails", listso);// Sales Invoice Details 
            }
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("firstdateofyear"));
            params.add(requestMap.get("currentdate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Details of Sales Invoice of First Date of current month to current date 
            if (listso != null && listso.size() > 0) {
                object.put("invoiceAmountYearToDate", listso);
            }
            
            params = new ArrayList();
            query = "";
            innerjoin = "";
            whereclouse = "";
            query = "Select SUM(invoice.invoiceamountdueinbase) from invoice  ";
            innerjoin += " INNER JOIN exchangeratedetails on invoice.exchangeratedetail = exchangeratedetails.id ";
            innerjoin += " INNER JOIN journalentry on journalentry.id = invoice.journalentry  ";
            innerjoin += " INNER JOIN accjecustomdata on journalentry.accjecustomdataref = accjecustomdata.journalentryId ";
            whereclouse += " WHERE accjecustomdata.col" + jobjGlobalCombo.getString("Acc_Invoice_ModuleCol") + "=? ";
            params.add(jobjGlobalCombo.getString("Acc_Invoice_ModuleId"));
            whereclouse += " And invoice.company = ? ";
            params.add(requestMap.get("companyid"));
            datefilter = " And journalentry.entrydate >= ? ";
            params.add(requestMap.get("startDate"));
            datefilter += " And journalentry.entrydate <= ? ";
            params.add(requestMap.get("endDate"));
            whereclouse += " And invoice.deleteflag = 'F' ";
            whereclouse += " And invoice.approvestatuslevel >= '11' ";
            whereclouse += " And invoice.isdraft=0 ";
            whereclouse+=" AND invoice.invoiceamountdueinbase>0 ";
            listso = executeSQLQuery(query + innerjoin + whereclouse +datefilter, params.toArray()); // Total Outstanding Amount(Unpaid Amount)
            if (listso != null && listso.size() > 0) {
                object.put("totalOutstandingAmount", listso);
            }
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("30days"));
            params.add(requestMap.get("currentdate"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Data of last 30 days
            if (listso != null && listso.size() > 0) {
                object.put("currentoutstanging", listso);
            }
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("60days"));
            params.add(requestMap.get("31days"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());// Data of last 30 to 60 days
            if (listso != null && listso.size() > 0) {
                object.put("over30Outstanding", listso);
            }
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            params.add(requestMap.get("90days"));
            params.add(requestMap.get("61days"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray()); // Data of last 60 to 90 days
            if (listso != null && listso.size() > 0) {
                object.put("over60Outstanding", listso);
            }
            params.remove(params.size() - 1);
            params.remove(params.size() - 1);
            datefilter = " And journalentry.entrydate < ? ";
            params.add(requestMap.get("90days"));
            listso = executeSQLQuery(query + innerjoin + whereclouse + datefilter, params.toArray());
            if (listso != null && listso.size() > 0) {
                object.put("over90Outstanding", listso);
            }
            
        } catch (JSONException ex) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }        
    @Override
    public List getLimitedAccountsOfMasterForm(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        try {
            String companyId = requestParams.containsKey("companyid") ? (StringUtil.isNullOrEmpty(requestParams.get("companyid").toString()) ? "" : requestParams.get("companyid").toString()) : "";
            String fieldId = requestParams.containsKey("fieldId") ? (StringUtil.isNullOrEmpty(requestParams.get("fieldId").toString()) ? "" : requestParams.get("fieldId").toString()) : "";
            String condition = "";
            if(!StringUtil.isNullOrEmpty(fieldId)){
                condition += " where masterform = '" + fieldId + "'";
            }
            if(!StringUtil.isNullOrEmpty(companyId)){
                condition += (StringUtil.isNullOrEmpty(condition) ? " where" : " and") + " company = '" + companyId + "'";
            }
            String query = "select accountid from limitedaccountsmapping" + condition;
            
            list = executeSQLQuery(query, new Object[]{});
        } catch (Exception e) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return list;
    }
    
    @Override
    public JSONObject saveLimitedAccounts(HashMap<String, Object> requestParams) throws ServiceException, JSONException{
        JSONObject returnJobj = new JSONObject();
        boolean success = false;
        try {
            String companyId = requestParams.containsKey("companyid") ? (StringUtil.isNullOrEmpty(requestParams.get("companyid").toString()) ? "" : requestParams.get("companyid").toString()) : "";
            String fieldId = requestParams.containsKey("fieldId") ? (StringUtil.isNullOrEmpty(requestParams.get("fieldId").toString()) ? "" : requestParams.get("fieldId").toString()) : "";
            String accountids = requestParams.containsKey("accountValues") ? (StringUtil.isNullOrEmpty(requestParams.get("accountValues").toString()) ? "" : requestParams.get("accountValues").toString()) : "";
            /**
             * check whether other than mapped accounts are already present in transactions
             */
            String accountCheckQuery1 = "", accountCheckQuery2 = "";
            if(fieldId.equals("customerAccounts")){
                accountCheckQuery1 = "select DISTINCT account from customer where company='"+companyId+"' and account not in ('"+accountids.replaceAll(",", "','")+"')";
            } else if(fieldId.equals("vendorAccounts")){
                accountCheckQuery1 = "select DISTINCT account from vendor where company='"+companyId+"' and account not in ('"+accountids.replaceAll(",", "','")+"')";
            } else if(fieldId.equals("productSalesAccounts")){
                accountCheckQuery1 = "select DISTINCT salesAccount from product where company='"+companyId+"' and salesAccount not in ('"+accountids.replaceAll(",", "','")+"')";
                accountCheckQuery2 = "select DISTINCT salesReturnAccount from product where company='"+companyId+"' and salesReturnAccount not in ('"+accountids.replaceAll(",", "','")+"')";
            } else if(fieldId.equals("productPurchaseAccounts")){
                accountCheckQuery1 = "select DISTINCT purchaseAccount from product where company='"+companyId+"' and purchaseAccount not in ('"+accountids.replaceAll(",", "','")+"')";
                accountCheckQuery2 = "select DISTINCT purchaseReturnAccount from product where company='"+companyId+"' and purchaseReturnAccount not in ('"+accountids.replaceAll(",", "','")+"')";
            }
            
            List<String> list1 = executeSQLQuery(accountCheckQuery1, new Object[]{});
            List<String> list2 = null;
            if(!StringUtil.isNullOrEmpty(accountCheckQuery2)){
                list2 = executeSQLQuery(accountCheckQuery2, new Object[]{});
            }
            /**
             * If no any old mapping exist then proceed to limitation of accounts
             */
            if(list1.isEmpty() && (list2 == null || (list2 != null && list2.isEmpty()))){
                //delete existing all mapped accounts
                String deleteQuery = "delete from limitedaccountsmapping where company = ? and masterform = ?";
                executeSQLUpdate(deleteQuery, new Object[]{companyId, fieldId});
                //insert all mapped accounts
                String query = "insert into limitedaccountsmapping (id,accountid, masterform, company) values(?, ?, ?, ?)";
                if(!StringUtil.isNullOrEmpty(accountids)){
                    String[] accountArr = accountids.split(",");
                    for (String account : accountArr) {
                        String accId = account.trim();
                        executeSQLUpdate(query, new Object[]{UUID.randomUUID().toString(), accId, fieldId, companyId});
                    }
                }
                success = true;
            } else{
                /**
                 * get all account ids and send it to map those accounts from UI
                 */
                String accids = "";
                for(String accountid : list1){
                    accids += accountid + ",";
                }
                if(list2 != null){
                    for(String accountid : list2){
                        accids += accountid + ",";
                    }
                }
                if(!StringUtil.isNullOrEmpty(accids)){
                    accids = accids.substring(0, accids.length()-1);
                }
                returnJobj.put("accountids", accids);
                success = false;
            }
            returnJobj.put("success", success);
        } catch (Exception e) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, e);
            returnJobj.put("success", false);
            returnJobj.put("msg", "Some error occurred. Please try again.");
        }
        return returnJobj;
    }
    
    @Override
    public List getLimitedAccountsForCombo(HashMap<String, Object> requestParams) throws ServiceException {
        List list = null;
        try {
            String companyId = requestParams.containsKey("companyid") ? (StringUtil.isNullOrEmpty(requestParams.get("companyid").toString()) ? "" : requestParams.get("companyid").toString()) : "";
            String fieldId = requestParams.containsKey("fieldId") ? (StringUtil.isNullOrEmpty(requestParams.get("fieldId").toString()) ? "" : requestParams.get("fieldId").toString()) : "";
            String condition = "";
            if(!StringUtil.isNullOrEmpty(fieldId)){
                condition += " and lam.masterform = '" + fieldId + "'";
            }
            if(!StringUtil.isNullOrEmpty(companyId)){
                condition += " and lam.company = '" + companyId + "'";
            }
//            String query = "select acc.id as accid, acc.name as accname, acc.acccode as accountcode from account acc inner join limitedaccountsmapping lam where acc.id = lam.accountid" + condition;
            String query = "select acc.id as accid, acc.name as accname, acc.acccode as accountcode, accgr.`name` as groupname from account acc inner join limitedaccountsmapping lam inner join accgroup accgr on acc.groupname=accgr.id where acc.id = lam.accountid" + condition;
            
            list = executeSQLQuery(query, new Object[]{});
        } catch (Exception e) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return list;
    }
    /**
     * Check and Filter and return those Accounts Used In any Module Like
     * Product, Expense PO, Expense GR Function for ActualVsBudgetReport
     *
     * @param list (accounts used and not used)
     * @param companyId
     * @return list (accounts used) 
    *
     */
    @Override
    public List isAccountsUsed(List ls,String companyid) throws ServiceException {
        List<Object> list = new ArrayList<Object>();;
        try {
            Iterator<Object[]> itracc = ls.iterator();
            while (itracc.hasNext()) {
                Object[] rowacc = (Object[]) itracc.next();
                String accountid = rowacc[0].toString();
                List listcount = new ArrayList();
//                String q = "from JournalEntryDetail jed where account.ID=? and jed.journalEntry.deleted=false and jed.company.companyID=?";
//                listcount = executeQuery(q, new Object[]{accountid, companyid});
//                if (listcount.size() > 0) {
//                    list.add(rowacc);
//                    continue;
//                }
                listcount = new ArrayList();
                String q = "from Product pr where (purchaseAccount.ID=? or costOfGoodsSoldAccount.ID=?) and pr.company.companyID=?";
                listcount = executeQuery(q, new Object[]{accountid, accountid, companyid});
                if (listcount.size() > 0) {
                    list.add(rowacc);
                    continue;
                }
                listcount = new ArrayList();
                q = "from ExpensePODetail epo where account.ID=? and epo.company.companyID=?";
                listcount = executeQuery(q, new Object[]{accountid, companyid});
                if (listcount.size() > 0) {
                    list.add(rowacc);
                    continue;
                }
                listcount = new ArrayList();
                q = "from ExpenseGRDetail egr where account.ID=? and egr.company.companyID=?";
                listcount = executeQuery(q, new Object[]{accountid, companyid});
                if (listcount.size() > 0) {
                    list.add(rowacc);
                    continue;
                }
            }
        } catch (Exception e) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return list;
    }
    /**
     * Remove entry from usedin column of account table if now account is not mapped in any transaction
     * @param accType
     * @param companyId
     * @param coaId
     * @return
     * @throws ServiceException 
     */
    @Override
    public boolean removeEntryFromAccountUsedIn(String accType, String companyId, String coaId) throws ServiceException{
        boolean returnSuccess = false;
        try {
            /**
             * get count of transactions in which same account is used
             */
            String accountCheckQuery = "";
            if(accType.equals(Constants.Customer_Default_Account)){
                accountCheckQuery = "select count(*) from Customer where company.companyID=? and account.ID=?";
            } else if(accType.equals(Constants.Vendor_Default_Account)){
                accountCheckQuery = "select count(*) from Vendor where company.companyID=? and account.ID=?";
            }
            
            String usedIn = "";
            List list = executeQuery(accountCheckQuery, new Object[]{companyId, coaId});
            if (!list.isEmpty()) {
                //get and check count value
                Long c = list.get(0) != null ? (Long) list.get(0) : 0;
                int count = c.intValue();
                //If count is 0(zero means not used in other transactions) then get usedin value to update
                if(count == 0){
                    List<String> accountList = executeSQLQuery("select usedin from account where company=? and id=?", new Object[]{companyId, coaId});
                    if(!accountList.isEmpty()){
                        usedIn = accountList.get(0);
                    }
                }
            }
            //if usedin value available then update it
            if(!StringUtil.isNullOrEmpty(usedIn)){
                usedIn = StringUtil.replaceUsedIn(usedIn, accType);
                int numRec = executeSQLUpdate("update account set usedin=? where company=? and id=?", new Object[]{usedIn, companyId, coaId});
            }
            
            returnSuccess = true;
        } catch (Exception e) {
            Logger.getLogger(accAccountDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnSuccess;
    }
    
    /**
     * @Info Create JsonArray Containing field and its value and pass it while
     * saving Stock Adjustment
     * @param accCustomData
     * @param extraparams
     * @return
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ServiceException
     * @throws ParseException
     */
    @Override
    public JSONArray createCustomFieldValueArray(AccCustomData accCustomData, HashMap<String, Object> extraparams) throws JSONException, SessionExpiredException, ServiceException, ParseException {
        int moduleid = 0, customcolumn = 0, customfield = 0, linkModuleId = 0;
        String billid = "", companyid = "", browsertz = "", userdateformat = "";
        boolean iscustomfield = false;

        JSONObject requestJobj = new JSONObject();
        JSONArray jArray = new JSONArray();

        HashMap<String, Object> requestParams = new HashMap<String, Object>();

        if (extraparams.containsKey(Constants.companyid)) {
            companyid = (String) extraparams.get(Constants.companyid);
        }

        if (extraparams.containsKey(Constants.moduleid)) {
            moduleid = (Integer) extraparams.get(Constants.moduleid);
        }

        if (extraparams.containsKey(Constants.linkModuleId)) {
            linkModuleId = (Integer) extraparams.get(Constants.linkModuleId);
        }

        if (extraparams.containsKey(Constants.customcolumn)) {
            customcolumn = (Integer) extraparams.get(Constants.customcolumn);
        }

//        if (extraparams.containsKey(Constants.customfield)) {
//            customfield = (Integer) extraparams.get(Constants.customfield);
//        }
        if (extraparams.containsKey(Constants.userdateformat)) {
            userdateformat = (String) extraparams.get(Constants.userdateformat);
        }

        requestJobj.put(Constants.companyid, companyid);
        requestJobj.put(Constants.linkModuleId, linkModuleId);
        requestJobj.put(Constants.userdateformat, userdateformat);

        // Get custom fields of Stock Adjustment
        HashMap<String, String> customFieldMap = new HashMap<>();
        HashMap<String, String> customDateFieldMap = new HashMap<>();
        HashMap<String, String> customRichTextMap = new HashMap<>();
        HashMap<String, Integer> customRefcolMap = new HashMap<>();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, linkModuleId, customcolumn));
        HashMap<String, String> replaceFieldMap = new HashMap<>();
        HashMap<String, Integer> fieldMap = getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap, customRichTextMap, customRefcolMap);

        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
        requestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, customcolumn));
        KwlReturnObject result = getFieldParams(requestParams);
        List lst = result.getEntityList();

        Iterator ite = lst.iterator();

        while (ite.hasNext()) {
            String field = "", data = "", fieldValue = "";
            FieldParams tmpcontyp = (FieldParams) ite.next();
            field = tmpcontyp.getFieldlabel();
            JSONObject temp1 = new JSONObject();

            /**
             * Create JsonArray Containing field and its value and pass it while
             * saving Stock Adjustment
             */
            if (fieldMap.containsKey(tmpcontyp.getFieldname())) {
                fieldValue = getCustomData(accCustomData, tmpcontyp, requestJobj); // Get fieldvalue of SA module

                temp1.put("filedid", replaceFieldMap.get(tmpcontyp.getFieldname()).substring(7));
                temp1.put("refcolumn_name", Constants.Custom_Column_Prefix + customRefcolMap.get(tmpcontyp.getFieldname()));
                temp1.put("fieldname", tmpcontyp.getFieldname());
                temp1.put("xtype", tmpcontyp.getFieldtype());
                temp1.put("fieldid", replaceFieldMap.get(tmpcontyp.getFieldname()).substring(7));
                temp1.put(tmpcontyp.getFieldname(), Constants.Custom_Column_Prefix + fieldMap.get(tmpcontyp.getFieldname()));
                temp1.put(Constants.Custom_Column_Prefix + fieldMap.get(tmpcontyp.getFieldname()), fieldValue);
                jArray.put(temp1);
            }
        }
        return jArray;
    }

    /**
     * @Info Get module specific value
     * @param accCustomData
     * @param field
     * @param params
     * @return
     * @throws ParseException
     * @throws JSONException
     */
    public String getCustomData(AccCustomData accCustomData, FieldParams field, JSONObject params) throws ParseException, JSONException {
        String data = "";
        HashMap<String, Object> CustomRequestParams = new HashMap<String, Object>();
        int linkModuleId = params.optInt("linkModuleId");
        String companyid = params.optString("companyid");
        int customcolumn = params.optInt("customcolumn", 1);
        DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(params);//User Date Formatter
        if (accCustomData != null) {
            String coldata = accCustomData.getCol(field.getColnum());
            int fieldType = field.getFieldtype();
            if (!StringUtil.isNullOrEmpty(coldata)) {
                switch (fieldType) {
                    case 3: // Date field
                        //long milliSeconds = Long.parseLong(coldata);
                        DateFormat dateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateWithNoTZ = dateFormat.parse(coldata);
                        data = dateFormat.format(dateWithNoTZ);
//                                }
                        break;
                    case 4:  // Single select Drop Down
                        CustomRequestParams.clear();
                        CustomRequestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_id, FieldConstants.Crm_deleteflag));
                        CustomRequestParams.put(Constants.filter_values, Arrays.asList(coldata, 0));
                        KwlReturnObject customresult = getCustomCombodata(CustomRequestParams);
                        if (customresult != null) {
                            List customDataList = customresult.getEntityList();
                            Iterator cite = customDataList.iterator();
                            while (cite.hasNext()) {
                                String valueForReport = "";
                                Object[] row = (Object[]) cite.next();
                                FieldComboData combodata = (FieldComboData) row[0];
                                if (combodata != null) {
                                    valueForReport += combodata.getValue();
                                }
                                if (!StringUtil.isNullOrEmpty(valueForReport)) {
                                    data = valueForReport;
                                }
                            }
                        }

                        data = fieldDataManagerNew.getValuesForLinkRecords(linkModuleId, companyid, field.getFieldname(), data, field.getCustomcolumn());

                        break;
                    case 7: //Multiple select drop down
                        String[] valueData = coldata.split(",");
                        for (String value : valueData) {
                            CustomRequestParams.clear();
                            CustomRequestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_id, FieldConstants.Crm_deleteflag));
                            CustomRequestParams.put(Constants.filter_values, Arrays.asList(value, 0));
                            KwlReturnObject customresult1 = getCustomCombodata(CustomRequestParams);
                            if (customresult1 != null) {
                                List customDataList = customresult1.getEntityList();
                                Iterator cite = customDataList.iterator();
                                while (cite.hasNext()) {
                                    Object[] row = (Object[]) cite.next();
                                    FieldComboData combodata = (FieldComboData) row[0];
                                    data += combodata.getValue() + ",";
                                }
                            }
                        }
                        data = data.length() > 0 ? data.substring(0, data.length() - 1) : data;

                        data = fieldDataManagerNew.getValuesForLinkRecords(linkModuleId, companyid, field.getFieldname(), data, field.getCustomcolumn());

                        break;
                    case 12: // Check List
                        HashMap<String, Object> checkListRequestParams = new HashMap<String, Object>();
                        String Colsplit[] = coldata.split(",");
                        for (int i = 0; i < Colsplit.length; i++) {
                            coldata = Colsplit[i];
                            checkListRequestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_id, FieldConstants.Crm_deleteflag));
                            checkListRequestParams.put(Constants.filter_values, Arrays.asList(coldata, 0));
                            ArrayList order_by = new ArrayList();
                            ArrayList order_type = new ArrayList();
                            order_by.add("itemsequence");
                            order_type.add("asc");
                            checkListRequestParams.put("order_by", order_by);
                            checkListRequestParams.put("order_type", order_type);
                            KwlReturnObject checkListresult = getCustomCombodata(checkListRequestParams);
                            List checklst = checkListresult.getEntityList();
                            Iterator checkite = checklst.iterator();
                            while (checkite.hasNext()) {
                                Object[] row = (Object[]) checkite.next();
                                FieldComboData checkfield = (FieldComboData) row[0];
                                data += checkfield.getValue() + ",";
                            }
                        }
                        data = data.length() > 0 ? data.substring(0, data.length() - 1) : data;

                        data = fieldDataManagerNew.getValuesForLinkRecords(linkModuleId, companyid, field.getFieldname(), data, field.getCustomcolumn());

                        break;
                    case 13:
                        data = coldata.replaceAll("(\r\n|\n\r|\r|\n)", "<br />");
                        break;
                    default:
                        data = coldata;
                        break;
                }
            } else {
                if (fieldType == 2) {// If Number field
                    data = "0";
                } else {
                    data = "";
                }
            }
        }
        return data;
    }
    
     public void setInvoiceTermsSalesActive(HashMap hm) throws ServiceException
     {
         try {
            if (hm.containsKey("termId")) {
                String termId = (String) hm.get("termId");
                boolean isTermActive = (hm.containsKey("isTermActive")) ? Boolean.parseBoolean(hm.get("isTermActive").toString()) : false;
                InvoiceTermsSales termobj = (InvoiceTermsSales) get(InvoiceTermsSales.class, termId);
                termobj.setIsTermActive(isTermActive);
                saveOrUpdate(termobj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDAOImpl.setInvoiceTermsSalesActive:" + ex.getMessage(), ex);
        } 
     }
    
    @Override
    public KwlReturnObject getBankAccountMappingDetails(JSONObject paramsJobj) throws ServiceException {
        String hql = " from BankAccountCOAMapping ";
        String conditionQuery = "";
        List params = new ArrayList();
        String companyid = paramsJobj.optString(Constants.companyKey);
        if (!StringUtil.isNullOrEmpty(companyid)) {
            conditionQuery += StringUtil.isNullOrEmpty(conditionQuery) ? "" : " and ";
            conditionQuery += " company.companyID = ? ";
            params.add(companyid);
        }
        String bankid = paramsJobj.optString(IntegrationConstants.BANK_ID);
        if (!StringUtil.isNullOrEmpty(bankid)) {
            conditionQuery += StringUtil.isNullOrEmpty(conditionQuery) ? "" : " and ";
            conditionQuery += " bankid = ? ";
            params.add(bankid);
        }
        String accountNumber = paramsJobj.optString(IntegrationConstants.BANK_ACCOUNT_NUMBER);
        if (!StringUtil.isNullOrEmpty(accountNumber)) {
            conditionQuery += StringUtil.isNullOrEmpty(conditionQuery) ? "" : " and ";
            conditionQuery += " bankaccountnumber = ? ";
            params.add(accountNumber);
        }
        if (!StringUtil.isNullOrEmpty(conditionQuery)) {
            hql += " where " + conditionQuery;
        }
        List list = executeQuery(hql, params.toArray());
        return new KwlReturnObject(true, "", null, list, list != null ? list.size() : 0);
    }
    
     
     
    @Override
    public void saveOrUpdateBankAccountMappingDetails(Map<String, Object> requestParams) throws ServiceException {
        try {
            String deskerAaccountID = null;
            BankAccountCOAMapping bankAccountCOAMapping;

            if (requestParams.containsKey("deskeraaccount")) {
                deskerAaccountID = (String) requestParams.get("deskeraaccount");
            }
            
            if (!StringUtil.isNullOrEmpty(deskerAaccountID)) {
                Account account = (requestParams.get("deskeraaccount") == null ? null : (Account) get(Account.class, (String) requestParams.get("deskeraaccount")));
                if (account != null) {
                    bankAccountCOAMapping = (BankAccountCOAMapping) get(BankAccountCOAMapping.class, deskerAaccountID);
                    if (bankAccountCOAMapping == null) {
                        bankAccountCOAMapping = new BankAccountCOAMapping();
                        bankAccountCOAMapping.setDeskeraAccount(account);
                    }
                    if (requestParams.containsKey("bankid")) {
                        bankAccountCOAMapping.setBankID((String) requestParams.get("bankid"));
                    }
                    if (requestParams.containsKey("bankaccountname")) {
                        bankAccountCOAMapping.setBankAccountName((String) requestParams.get("bankaccountname"));
                    }
                    if (requestParams.containsKey("bankaccountnumber")) {
                        bankAccountCOAMapping.setBankAccountNumber((String) requestParams.get("bankaccountnumber"));
                    }
                    if (requestParams.containsKey("bankaccountdetails")) {
                        bankAccountCOAMapping.setBankAccountDetails((String) requestParams.get("bankaccountdetails"));
                    }
                    if (requestParams.containsKey("companyid")) {
                        Company cmp = (requestParams.get("companyid") == null ? null : (Company) get(Company.class, (String) requestParams.get("companyid")));
                        bankAccountCOAMapping.setCompany(cmp);
                    }
                    saveOrUpdate(bankAccountCOAMapping);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDAOImpl.saveBankAccountMappingDetails:" + ex.getMessage(), ex);
        }
    }
    
    @Override
    public void deleteBankAccountMappingDetails(JSONObject paramsJobj) throws ServiceException {
        String sql = " DELETE from bankaccountcoamapping ";
        String conditionQuery = "";
        List params = new ArrayList();
        String companyid = paramsJobj.optString(Constants.companyKey);
        try {
            if (!StringUtil.isNullOrEmpty(companyid)) {
                conditionQuery += StringUtil.isNullOrEmpty(conditionQuery) ? "" : " and ";
                conditionQuery += " company = ? ";
                params.add(companyid);
            }
            String bankid = paramsJobj.optString(IntegrationConstants.BANK_ID);
            if (!StringUtil.isNullOrEmpty(bankid)) {
                conditionQuery += StringUtil.isNullOrEmpty(conditionQuery) ? "" : " and ";
                conditionQuery += " bankid = ? ";
                params.add(bankid);
            }
            String ids = paramsJobj.optString("ids");
            if (!StringUtil.isNullOrEmpty(ids)) {
                conditionQuery += StringUtil.isNullOrEmpty(conditionQuery) ? "" : " and ";
                conditionQuery += " ID IN ( " + ids + ") ";
            }
            if (!StringUtil.isNullOrEmpty(conditionQuery)) {
                sql += " where " + conditionQuery;
            }
            int cnt = executeSQLUpdate(sql, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accAccountDAOImpl.deleteBankAccountMappingDetails:" + ex.getMessage(), ex);
        }
    }

}
