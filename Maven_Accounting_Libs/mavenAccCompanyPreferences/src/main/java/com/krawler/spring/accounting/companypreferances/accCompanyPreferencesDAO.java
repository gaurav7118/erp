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
package com.krawler.spring.accounting.companypreferances;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.ClosingAccountBalance;
import com.krawler.hql.accounting.ChequeSequenceFormat;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.hql.accounting.YearEndCheckList;
import com.krawler.hql.accounting.YearLock;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accCompanyPreferencesDAO {

    public KwlReturnObject addPreferences(HashMap<String, Object> prefMap) throws ServiceException; 

    public KwlReturnObject updatePreferences(HashMap<String, Object> prefMap) throws ServiceException;

    public KwlReturnObject addOrUpdateExtraPreferences(HashMap<String, Object> prefMap) throws ServiceException;
    
    public KwlReturnObject addOrUpdateMRPPreferences(HashMap<String, Object> prefMap) throws ServiceException;
    
    public KwlReturnObject addOrUpdateIndiaComplianceExtraPreferences(HashMap<String, Object> prefMap) throws ServiceException;
     
    public KwlReturnObject addOrUpdateDocumentEmailSettings(HashMap<String, Object> prefMap) throws ServiceException;

    public KwlReturnObject getCompanyPreferences(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getCompanyPreferencesFieldForExport() throws ServiceException;

    public String getNextAutoNumber(String companyid, int from) throws ServiceException, AccountingException;
    
    public String getNextAutoNumber_manually(Map<String, Object> seqNumberMap, int nextAutoNoIntpart) throws ServiceException, AccountingException;
    
    public Map<String,Object> getNextChequeNumber(JSONObject Jobj) throws ServiceException;

    public String getNextAutoNumber(String companyid, int from, String format) throws ServiceException, AccountingException;
    
    public Map<String,Object> getNextAutoNumber_Modified(String companyid, int from, String format, boolean oldflag, Date creationDate) throws ServiceException, AccountingException;

    public KwlReturnObject getPreferencesFromAccount(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject addYearLock(Map<String, Object> yearLockMap) throws ServiceException;

    public KwlReturnObject updateYearLock(Map<String, Object> yearLockMap) throws ServiceException;

    public KwlReturnObject getYearLock(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getClosingBalanceList(YearLock yearlock, int yearId, String companyId) throws ServiceException;

    public KwlReturnObject getClosingBalanceListMinYear(String companyId) throws ServiceException;

    public void saveOrUpdateClosingBalanceObj(List<ClosingAccountBalance> closingAccountBalanceList) throws ServiceException;

    public void deleteClosingBalance(YearLock yearLock) throws ServiceException;

    public KwlReturnObject getAccount(String name, String companyId) throws ServiceException;
    
    public KwlReturnObject getAccountObjectById(String id, String companyId) throws ServiceException;

    public void setAccountPreferences(String companyid, HashMap hm, Date curDate) throws ServiceException;
    
    public void saveDefaultSequenceFormat(Company company) throws ServiceException; 

    public void setNewYear(Date time, Date financialdate, String companyid) throws ServiceException;

    public void setCurrentYear(int presentYear, int previousYear, String companyid) throws ServiceException;

    public List getMappedCompanies(String parentcompanyid) throws ServiceException;
    
    public int getMappedCompaniesCount(String parentcompanyid) throws ServiceException;

    public KwlReturnObject getYearLockforPreferences(Map<String, Object> filterParams) throws ServiceException;

    public boolean saveCompanyPreferencesObj(CompanyAccountPreferences companyAccountPreferencesObj) throws ServiceException;

    public SequenceFormat saveSequenceFormat(HashMap<String, Object> dataMap) throws ServiceException;

    public ChequeSequenceFormat saveChequeSequenceFormat(HashMap<String, Object> dataMap);

    public int deleteChequeSequenceFormat(String id, String companyId) throws ServiceException;

    public KwlReturnObject getSequenceFormat(Map<String, Object> filterParams) throws ServiceException;
    
    public JSONObject saveAllowZeroQtyForProduct(JSONObject paramJobj) throws ServiceException;
    
    public KwlReturnObject getChequeSequenceFormatList(Map<String, Object> filterParams) throws ServiceException;
    
     public List getCustomerNameByID(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getAccountList(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getTransactionFormsFieldHideShowProperty(int moduleId, String companyId) throws ServiceException;

    public ExtraCompanyPreferences saveExtraCompanyPreferences(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getExtraCompanyPreferences(Map<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getIndiaComplianceExtraCompanyPreferences(Map<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getMRPCompanyPreferences(Map<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getDocumentEmailSettings(Map<String, Object> filterParams) throws ServiceException;

    public ExtraCompanyPreferences updateExtraCompanyPreferences(HashMap<String, Object> prefMap) throws ServiceException;

    public KwlReturnObject checktransactionforbookbeginningdate(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkOpeningTransactionsForFirstFinancialYearDate(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONObject updateOpeningTransactionDates(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject checkYearLockpresentindb(Map<String, Object> filterParams) throws ServiceException;

    public SequenceFormat updateSequenceFormat(HashMap<String, Object> dataMap) throws ServiceException, AccountingException;
    
    public ChequeSequenceFormat updateChequeSequenceFormat(HashMap<String, Object> dataMap) throws ServiceException, AccountingException;

    public List checkSequenceFormat(String id, String companyId, String module) throws ServiceException, AccountingException;
    
    public List checkDimensionSequenceFormat(String id, String companyId, String module) throws ServiceException, AccountingException;
    
    public KwlReturnObject getAllCompanyUsersEmailIds(String companyid) throws ServiceException;
    
    public KwlReturnObject getCompanyAdminUsersEmailIds(String loginUserId, String companyid) throws ServiceException;
    
    public KwlReturnObject setUserActiveDays(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getUserActiveDaysDetails(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteUserActiveDaysDetails(String userID, boolean isAllUser, String companyID) throws ServiceException;
    
    public List checksEntryNumberForSequenceNumber(int mduleid, String entryNumber, String companyid)throws ServiceException;
    
    public List checksChequeNumberForSequenceNumber(String chequeNumber, String companyid) throws ServiceException;
    
   public JSONArray checkTransactionmatchedwithSequenceFormat(HashMap requestParams)throws ServiceException; 
     
   public KwlReturnObject getCustomerFromPreferences(String accountid, String companyid) throws ServiceException;
    
    public boolean getDepreciationCount(String companyID);
    
    public boolean getopeningDepreciationPostedCount(String companyID);
    
    public KwlReturnObject getCompanyList(String[]  subdomainArray) throws ServiceException;

    public KwlReturnObject saveCompanyAddressDetails(HashMap<String, Object> addrMap)throws ServiceException;

    public KwlReturnObject deleteCompanyAddressDetails(String companyid) throws ServiceException;
    
    public KwlReturnObject getExtraPreferencesFromAccount(String accountid, String companyid) throws ServiceException;

//    public KwlReturnObject getCompanyAddressDetails(HashMap<String, Object> requestParams)throws ServiceException;
    
    public boolean isCompanyActivated(String companyID) throws ServiceException;
    
    public List isCompanyExistWithCompanyID(String companyID) throws ServiceException;
    
    public List isCompanyExistWithSubDomain(String subdomain) throws ServiceException;
    
    public List isAnotherCompanyExistWithSameSubDomain(String subdomain, String companyID) throws ServiceException;
    
    public void deleteCompanyData(String subdomain) throws ServiceException;
    
    public List getYearID(String companyid) throws ServiceException;
    
    public KwlReturnObject getActivatedSequenceFormat(String companyid) throws ServiceException;
    
    public KwlReturnObject getSequenceFormatforModuleid(Map<String,Object> requestParams) throws ServiceException;
    
    public List getExtraCompanyPreferencesForMalaysia() throws ServiceException;
    
    public boolean isMRPModuleActivated(String companyID) throws ServiceException;
    
    public KwlReturnObject addReportToWidgetView(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject removeReportFromWidgetView(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkTransactionsForManufacturingModule(Map<String, Object> requestParams);
    
    public  Map<String,Object> getTermSummary(Map<String, Object> requestMap) throws ServiceException;
    
    public  Map<String,Object> getTermUsedIn(Map<String, Object> requestMap) throws ServiceException;
    
    public  Map<String,Object> getTDSSummary(Map<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject isTDSUsedInTransactions(Map<String, Object> requestMap) throws ServiceException;
    
    public boolean isExciseApplicable(String companyID) throws ServiceException;
    
    public int getCountryID(String companyID) throws ServiceException ;
    
    /**
     * @param requestParams  HashMap should include parameters required by this
     * method
     * @description DAO method addRemoveFavouriteReport which 
     * add or remove report in or from  favourite list respectively
     * @return KwlReturnObject This will return the total count and list of reports 
     */
    public KwlReturnObject addRemoveFavouriteReport(HashMap<String, Object> requestParams) throws ServiceException;
    
    /**
     * @param requestParams  Map should include parameters required by this
     * method
     * @description DAO method to get FavouriteReportMasterObject which 
     * retrieve an existing instance of FavouriteReportMaster
     * @return KwlReturnObject This will return the total count and list of reports based on requestParams 
     */
    public KwlReturnObject getFavouriteReportMasterObject(Map<String, Object> requestParams) throws ServiceException ;
    
    public KwlReturnObject getCompanyList(Map<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getAccountUsedForFreeGift(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject addYearEndCheckList(JSONObject checkListJSON) throws ServiceException;
    
    public YearEndCheckList getYearEndCheckList(String id);
    
    public void deleteYearEndCheckList(String id, String companyid) throws ServiceException;
    
    public KwlReturnObject getMaxYearLockDetails(String companyid, int year) throws ServiceException;
    
    public List<ClosingAccountBalance> getClosingAccountBalance(JSONObject requestJSON);
    
    public boolean isBookClose(JSONObject jsonObject) ;
    
   public KwlReturnObject getMaxChequeSequenceNumber(HashMap hm) throws ServiceException;
    
    public String getCreatorFullName(String companyID);
    
    public KwlReturnObject getPerpetualInventoryActivatedCompanyList(Map<String, Object> filterParams) throws ServiceException;
    
    public JSONObject isChequeNumberAvailable(HashMap hm) throws ServiceException, JSONException;
    
    public KwlReturnObject getMultiGroupCompanyList(String companyid) throws ServiceException;
    
    public KwlReturnObject getCompanyInventoryAccountPreferences(JSONObject requestJSON) throws ServiceException;
    
    public KwlReturnObject checkSalesSideTransactionPresent(JSONObject requestJSON) throws  JSONException, ServiceException;
    
    public KwlReturnObject checkPurchaseSideTransactionPresent(JSONObject requestJSON) throws JSONException, ServiceException;
    
    public KwlReturnObject getAgedDateFilter(String userId) throws JSONException, ServiceException;
    
    public KwlReturnObject getIntegrationParties(JSONObject paramsJobj) throws ServiceException;
    
    public KwlReturnObject getIntegrationPartyCountryMapping(JSONObject paramsJobj) throws ServiceException;
    
    public boolean isBookClosed(Date finanDate, String companyid) throws JSONException;
}
