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

import com.krawler.common.admin.AccCustomData;
import com.krawler.common.admin.AddressFieldDimensionMapping;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.hql.accounting.Account;
import com.krawler.hql.accounting.DefaultTemplatePnL;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.Templatepnl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.util.*;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author krawler
 */
public interface accAccountDAO {

    public KwlReturnObject getAccountFromName(String companyId, String accname) throws ServiceException;

    public KwlReturnObject getAccountsFromName(String companyId, Set<String> accname) throws ServiceException;

    public KwlReturnObject getAccountFromCode(String companyId, String acccode) throws ServiceException;
    public KwlReturnObject getTaxFromCode(String companyId, String acccode) throws ServiceException;
    
    public boolean isTaxActivated(String companyId, String taxId) throws ServiceException;

    public KwlReturnObject deleteAccount(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject deleteIBGBankDetail(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject saveAccount(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getAccount(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getIBGDetailsForAccount(String accountID, String companyID) throws ServiceException;
    
    public KwlReturnObject getIBGDetailsForAccountSQL(String accountID, String companyID) throws ServiceException;

    public KwlReturnObject updateAccountCurrency(HashMap<String, Object> requestParams) throws ServiceException; //update the currency of all account

    public KwlReturnObject getSundryAccount(String companyId, boolean isVendor) throws ServiceException;

    public KwlReturnObject addAccount(JSONObject accjson) throws ServiceException;

    public KwlReturnObject updateAccount(JSONObject accjson) throws ServiceException;

    public KwlReturnObject saveOrupdateIBGBankDetail(HashMap<String, Object> ibgBankDetailParams) throws ServiceException;

    public void updateChildrenAccount(Account account) throws ServiceException;

    public KwlReturnObject getAccountsForCombo(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAccountsForJE(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getWarehouseIDByName(HashMap<String, Object> requestParams) throws ServiceException;
    
     public KwlReturnObject getStoreForIsDefaultNot(String companyID) throws ServiceException;
     
     public KwlReturnObject getLocationForIsDefaultNot(String companyID) throws ServiceException;
     
     public KwlReturnObject getStoreMasterData(String companyID) throws ServiceException;
    
    public KwlReturnObject getStoreIDByName(HashMap<String, Object> requestParams) throws ServiceException;
    
     public KwlReturnObject getCustomerNameByCustomerCode(HashMap<String, Object> requestParams) throws ServiceException;
     
     public KwlReturnObject getAccountIDByCode(HashMap<String, Object> requestParams) throws ServiceException; 
   
    public KwlReturnObject getCustomerForCombo(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getVendorForCombo(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAccounts(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteAccount(HashMap request, String companyid) throws ServiceException;

    public KwlReturnObject deleteAccount(String accountid, boolean flag) throws ServiceException;

    public KwlReturnObject activateDeactivateAccounts(HashMap request) throws ServiceException;

    public KwlReturnObject getGroup(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject addGroup(JSONObject groupjson) throws ServiceException;

    public KwlReturnObject updateGroup(JSONObject groupjson) throws ServiceException;

    public List updateParentGroup(Group group) throws ServiceException;

    public void updateChildrenGroup(Group group) throws ServiceException;

    public KwlReturnObject getGroups(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteGroup(String groupid, boolean isPermDel) throws ServiceException;

    public KwlReturnObject getMaxGroupDisplayOrder() throws ServiceException;

    public KwlReturnObject getAccountEntry(HashMap<String, Object> filterParams) throws ServiceException;
    
    public KwlReturnObject getAccountGroupInfo(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getAccountDatewise(String companyid, Date startDate, Date endDate, boolean onlyPnLAccounts) throws ServiceException;

    public KwlReturnObject getGroupForProfitNloss(String companyid, int nature, boolean affectGrossProfit,boolean isForTradingAndProfitLoss, boolean isCostOfGoodsSold) throws ServiceException;

    public ArrayList getAccountArrayList(List list, HashMap<String, Object> requestParams, boolean quickSearchFlag, boolean noactivityAcc) throws ServiceException;

    public KwlReturnObject copyAccounts(String companyid, String currencyid, String companyType, String countryId, HashMap accountsgrp,String stateId,boolean mrpActivated) throws ServiceException;

    public KwlReturnObject getDefaultAccount(String companyType, String countryId, String stateId, boolean isAdminSubdomain, String[] nature) throws ServiceException;

    public boolean isChild(String ParentID, String childID) throws ServiceException;

    public List isChildorGrandChild(String childID) throws ServiceException;

    public Group getAccountGroup(String companyid, String name) throws ServiceException;

    public Group getNewGroupFromOldId(String id, String companyid) throws ServiceException;

    public List isChildorGrandChildForVendor(String childID) throws ServiceException;

    public List isChildorGrandChildForCustomer(String childID) throws ServiceException;

    public List isChildforDelete(String childID) throws ServiceException;

    public KwlReturnObject getJEDTrasactionfromAccount(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject updateHeaderaccountField(String parentid, boolean flag) throws ServiceException;

    public KwlReturnObject getAccountDatewiseMerged(String companyid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException;

    public KwlReturnObject getGroupForProfitNlossMerged(String companyid, int nature, boolean affectGrossProfit, boolean defaulttypeflag) throws ServiceException;

    public List getMappedAccounts(Account childAccObj, String parentcompanyid, boolean autoMap) throws ServiceException;

    public List getMappedAccountsForReports(String parentaccountid) throws ServiceException;

    public KwlReturnObject getMonthlyBudget(HashMap<String, Object> filterParams) throws ServiceException,JSONException ;

    public KwlReturnObject getMonthlyForecast(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject addMonthlyBudget(JSONObject accjson,int year) throws ServiceException;

    public KwlReturnObject addMonthlyForecast(JSONObject accjson) throws ServiceException;

    public KwlReturnObject deleteMonthlyBudget(String id,String dimension,int year,HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteMonthlyForecast(String id) throws ServiceException;

    public KwlReturnObject saveUpdateAccountMapping(JSONObject accjson) throws ServiceException;

    public KwlReturnObject deleteAccountMapping(String mappingid) throws ServiceException;

    public void saveAssetHistory(String assetId, String details, Date auditTime, String ipAddress, String userId) throws ServiceException;

    public KwlReturnObject getAssetHistory(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteAccountMapPnL(String templateid, String companyid) throws ServiceException;

    public KwlReturnObject saveAccountMapPnL(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getAccountsFormappedPnL(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPnLTemplates(HashMap<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject updatePnLTemplate(Map<String, Object> dataMap) throws ServiceException;

    public int getMaxTemplateId(String companyid, boolean isAdminSubdomain, String countryid) throws ServiceException;

    public void updateAccountTemplateCode(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject deleteCustomTemplate(String templateid, String companyid) throws ServiceException;

    public KwlReturnObject deleteDefaultCustomTemplate(String templateid) throws ServiceException;

    public boolean checkNameAlreadyExists(String templateid, String name, String companyid, String countryid, boolean isAdminSubdomain, int templateType) ;
    
    public boolean updateDefaultTemplate(String companyid,int templatetype) throws ServiceException;

    public KwlReturnObject getFieldParams(HashMap<String, Object> requestParams);
    
    public List getFieldParamsFieldLabelWise(HashMap<String, Object> requestParams) throws ServiceException; 
    
    public KwlReturnObject getFieldParamsIds(Map<String, Object> requestParams);
    
    public KwlReturnObject getFieldParamsForCombo(HashMap<String, Object> requestParams);

    public KwlReturnObject getFieldParamsUsingSql(HashMap<String, Object> requestParams);

    public KwlReturnObject getFieldParamsforEdit(String fieldlabel, String ModuleIds, String companyid) throws ServiceException;

    public KwlReturnObject getDefaultHeaders(HashMap<String, Object> requestParams);
    
    public KwlReturnObject getTextRangeFilterFields(Map<String, Object> requestParams);

    public KwlReturnObject getCustomizeReportHeader(HashMap<String, Object> requestParams);

    public KwlReturnObject getCustomizeReportMapping(HashMap<String, Object> requestParams);

    public KwlReturnObject saveCustomizedReportFields(HashMap<String, Object> requestParams);

    public HashMap<String, Integer> getFieldParamsMap(HashMap<String, Object> fieldrequestParams, HashMap<String, String> replaceFieldMap);

    public HashMap<String, Object> getMaxGSTMappingColumn(String companyid);
    
    public double getSumofChallanUsedQuantity(String interstoretransfer) throws ServiceException;
    
    public HashMap<String, Integer> getFieldParamsCustomMap(HashMap<String, Object> requestParams, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap);
    
    public HashMap<String, Integer> getFieldParamsCustomMap(HashMap<String, Object> requestParams, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap,HashMap<String, String> customRichTextMap);

    public HashMap<String, Integer> getFieldParamsCustomMapForRows(HashMap<String, Object> requestParams, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap);
    
    public HashMap<String, Integer> getFieldParamsCustomMapForRows(HashMap<String, Object> requestParams, HashMap<String, String> replaceFieldMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, String> customRichTextMap,HashMap<String, Integer> customRefcolMap);

    public KwlReturnObject getCustomCombodata(HashMap<String, Object> requestParams);

    public List checkDuplicateNameOfCustomColumn(String moduleName, String companyid, String fieldlabel) throws ServiceException;

    public void storeDefaultCstmData(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getUsersByCompanyid(HashMap<String, Object> requestParams);

    public KwlReturnObject insertfield(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveOrUpdateAddressFieldForGSTDimension(HashMap<String, Object> requestparams) throws ServiceException;
    
    public KwlReturnObject getDimensionMappedAddressFieldID(HashMap<String, Object> requestParams);
    
    public KwlReturnObject getDimensionMappedWithSameAddressField(HashMap<String, Object> requestParams) throws ServiceException;

    public boolean updateField(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject insertfieldcombodata(HashMap<String, Object> comborequestParams) throws ServiceException;

    public KwlReturnObject getfieldcombodata(HashMap<String, Object> comborequestParams) throws ServiceException;
    
    /**
     * Desc: This method is used to get field combo Item description
     * @param comborequestParams
     * @return
     * @throws ServiceException 
     */
    public KwlReturnObject getfieldcomboItemDesc(HashMap<String, Object> comborequestParams) throws ServiceException;

    public String getfieldcombodatabyids(String FieldIDs) throws ServiceException;

    public KwlReturnObject getModules(HashMap<String, Object> requestParams);

    public KwlReturnObject insertdefaultheader(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject insertcolumnheader(HashMap<String, Object> requestParams) throws ServiceException;

    /*
     * Custom Layout Groups
     */
//    public KwlReturnObject getLayoutGroups(HashMap<String, Object> requestParams) throws ServiceException;
    public KwlReturnObject getAccountsForLayoutGroup(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getAccountsForDefaultLayoutGroup(Map<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDefaultAccountFromName(Map<String, Object> requestParams) throws ServiceException;

    public boolean checkLayoutGroupNameAlreadyExists(String id, String name, String companyid, String templateid);

    public boolean checkDefaultLayoutGroupNameAlreadyExists(String id, String name, String companyid, String templateid);

    public void updateExistingSequnceNo(int sequence, String companyid, String templateid,String operator,String groupid);

    public void updateDefaultLayoutGroupExistingSequnceNo(int sequence, String companyid, String templateid,String operator);

    public KwlReturnObject updateCustomfield(HashMap<String, Object> fieldmap) throws ServiceException;

    public KwlReturnObject saveLayoutGroupAccountMap(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveDefaultLayoutGroupAccountMap(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveLayoutGroup(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveDefaultLayoutGroup(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteLayoutGroup(String groupid, String companyid) throws ServiceException;

    public KwlReturnObject deleteDefaultLayoutGroup(String groupid) throws ServiceException;

    public KwlReturnObject deleteLayoutGroupAccount(String groupid, String companyid) throws ServiceException;

    public KwlReturnObject deleteDefaultLayoutGroupAccount(String groupid) throws ServiceException;

    public KwlReturnObject saveLayoutGroupMapForGroupTotal(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject saveDefaultLayoutGroupMapForGroupTotal(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getMappedLayoutGroupsforgrouptotal(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject getMappedDefaultLayoutGroupsforgrouptotal(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject deleteLayoutGroupsofTotalGroup(String groupid, String companyid) throws ServiceException;

    public KwlReturnObject deleteDefaultLayoutGroupsofTotalGroup(String groupid) throws ServiceException;

    public KwlReturnObject getLayoutGroupsFortotalgroupmap(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDefaultLayoutGroupsFortotalgroupmap(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomLayoutGroups(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getNextCustomLayoutSequence(Map<String, Object> filterParams) throws ServiceException;

    public KwlReturnObject getCustomsGroupsForTotal(String groupid) throws ServiceException;

    public KwlReturnObject getInvoiceTerms(HashMap<String, String> hm);
    
    public void setInvoiceTermsSalesActive(HashMap hm) throws ServiceException;
    
    public KwlReturnObject getLinkedTermTax(HashMap<String, Object>  requestParams);

    public KwlReturnObject findDimensionForEclaim(String companyid) throws ServiceException;
    
    public KwlReturnObject findTermUsedInFormula(String termid) throws ServiceException;
    
    public KwlReturnObject findTermUsedInTax(String termid) throws ServiceException;
    
    public KwlReturnObject findTermUsedInQuotation(String termid) throws ServiceException;
    
    public KwlReturnObject findTermUsedInSO(String termid) throws ServiceException;
    
    public KwlReturnObject findTermUsedInPI(String termid) throws ServiceException;
    
    public KwlReturnObject findTermUsedInVQ(String termid) throws ServiceException;
    
    public KwlReturnObject findTermUsedInPO(String termid) throws ServiceException;

    public KwlReturnObject findTermUsedInTransaction(String termid) throws ServiceException;
    
    public KwlReturnObject findTermUsedInDO(String termid) throws ServiceException;
    
    public KwlReturnObject findTermUsedInGRO(String termid) throws ServiceException;
    
    public KwlReturnObject findTaxUsedInCSSI(String taxids) throws ServiceException;
    
    public KwlReturnObject findTaxUsedInCPPI(String taxids) throws ServiceException;
    
    public KwlReturnObject findTaxUsedInCQ(String taxids) throws ServiceException;
    
    public KwlReturnObject findTaxUsedInVQ(String taxids) throws ServiceException;
    
    public KwlReturnObject findTaxUsedInSO(String taxids) throws ServiceException;
    
    public KwlReturnObject findTaxUsedInPO(String taxids) throws ServiceException;

    public KwlReturnObject getInvoiceTermFormulaName(HashMap<String, String> hm);

    public KwlReturnObject saveInvoiceTerm(HashMap<String, Object> hm) throws ServiceException;

    public KwlReturnObject addActiveDateRange(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updateCustomColumnmfield(String fieldlabel, String ModuleIDS, int lineitem, int maxlength, String fieldtooltip,String defaultval,boolean isAutoPopulateDefaultValue,boolean isForSalesCommission,boolean isForKnockOff) throws ServiceException;

    public int updateCustomProductfield(String ModuleIDS, String RelatedFieldIDs, String companyid) throws ServiceException;
    
    public int updateCustomProductfieldIsAllowedToEdit(String ModuleIDS, int relatedModuleIsAllowEdit, String companyid) throws ServiceException;

    public KwlReturnObject updateDimensionParent(String fieldlabel, String ModuleIDS, int lineitem, String parentId) throws ServiceException;

    public KwlReturnObject quotationindecimalforcompany(String companyid) throws ServiceException;

    public KwlReturnObject updateAccountCreationDate(Date fyfrom, String companyid) throws ServiceException;

    public KwlReturnObject getIBGBankDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public boolean isDuplicateSalesTerm(HashMap<String, String> termMap) throws ServiceException;
    
    public boolean isDuplicateLineLevelTerm(HashMap<String, Object> termMap) throws ServiceException;

    public KwlReturnObject getDefaultHeadersModuleJoinReference(String module) throws ServiceException;

    public KwlReturnObject updateCustomFieldActivation(int activation, String moduleid) throws ServiceException;

    public JSONObject getUnMappedAccounts(HashMap<String, Object> requestParams) throws ServiceException;
    
    public int setDontShowFlagCustomLayout(HashMap<String, Object> requestParams) throws ServiceException;
    
    public void saveOrUpdateAll(List<Object> objectList) throws ServiceException;
    
    public KwlReturnObject getAccountChilds(Account account) throws ServiceException;

    public KwlReturnObject getTaxesFromAccountId(HashMap<String, Object> taxFromAccountParams) throws ServiceException;
    
    public List sortOnParent(List lst);
    
    public List saveMasterItemDataSequence(JSONArray jSONArray, String groupid, String companyid) throws ServiceException;
    
    public KwlReturnObject getSplitAccountAmount(String Searchjson,String accid) throws ServiceException;
    
    public KwlReturnObject fieldForOpeningBalance(String fieldlabel, int moduleid,String companyid) throws ServiceException;
    
    public String getComboIdForAccount(String value,String fieldid) throws ServiceException;
    
    public KwlReturnObject distributeOpeningBalance(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getDistributedOpeningBalance(HashMap<String, Object> requestParams);
    
    public KwlReturnObject getCustomLayoutfromAccount(String accountid,String companyid) throws ServiceException ;
    
    public void updateSplitOpeningAmount(String comboId,double amount,String accountId) throws ServiceException;
    
    public void updateAccountOpeningAmount(String accountId,double amount) throws ServiceException;
    
    public double getSplitBalanceForComboId(String comboId,String fieldId,String accountId) throws ServiceException;
    
    public void mapDefaultHiddenFields(String companyid) throws ServiceException;

    public KwlReturnObject getFieldComboDatabyFieldID(String fieldid,String companyid) throws ServiceException; 

    public KwlReturnObject getPropagatedAccounts(HashMap<String, Object> requestParams1)throws ServiceException; 
    
    public KwlReturnObject saveOrupdateCIMBBankDetail(HashMap<String, Object> ibgBankDetailParams) throws ServiceException;
    
    public KwlReturnObject getCIMBDetailsForAccount(String accountID, String companyID) throws ServiceException;
    
    public KwlReturnObject getCIMBBankDetails(HashMap<String, Object> dataMap) throws ServiceException;
    
    public KwlReturnObject copyLayoutGroups(Templatepnl templatepnl, Map<String, Object> params);

    public KwlReturnObject copyDefaultLayoutGroups(DefaultTemplatePnL templatepnl, Map<String, Object> params);

    public KwlReturnObject getAllAccountsFromName(String companyId, String accname) throws ServiceException;
    
    public KwlReturnObject getAccountsForPM(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getDefaultCustomTemplate(String countryID, String id);
    
    public boolean copyDefaultCustomLayout(Map<String, Object> params,Templatepnl templatepnl);
    
    public KwlReturnObject getDefaultPnLTemplates(HashMap<String, Object> filterParams);

    public KwlReturnObject updateDefaultPnLTemplate(Map<String, Object> dataMap) throws ServiceException;
    
    public void copyDefaultTerms(HashMap<String, Object> defaultCompSetupMap, JSONObject setUpData)throws ServiceException;
    
    public void copyDefaultIndiaGSTTermsOnMigration(HashMap<String, Object> defaultCompSetupMap)throws ServiceException;
    
    public void copyIndiaGSTTermsOnMigration(HashMap<String, Object> defaultCompSetupMap)throws ServiceException;
    
    public KwlReturnObject copyIndiaGSTDefaultAccounts(String companyid, String currencyid,Map<String, Object> defaultCompSetupMap) throws ServiceException;

    public KwlReturnObject getIndianTermsCompanyLevel(HashMap<String, String> hm);
    
    public KwlReturnObject saveIndianTermsCompanyLevel(HashMap<String, Object> hm) throws ServiceException;
    
    public void updateAccountOpeningBalance(String accountId, double amount) throws ServiceException;
    
    public void updateAccountOpeningBalance(String dbName,String accountId, double amount) throws ServiceException;
    
    public boolean insertDefaultCustomeFields(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean insertFieldcomboValues(HashMap<String, Object> requestParams) throws ServiceException;
    
    public HashMap<String, Object> getcolumn_number(String companyid, Integer moduleid, Integer fieldtype, int moduleflag) throws SessionExpiredException, JSONException;
    
    public int updateExtraComPreferences(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getMasterItemfromAccount(String accountid,String companyid) throws ServiceException ;
    
    public KwlReturnObject getChildAccountfromAccount(String accountid,String companyid) throws ServiceException ;
    
    public KwlReturnObject getAccountUsedInExpenesePo(String accountid, String companyid) throws ServiceException;
    
    //This method used to modify the Custom Date long value to browser specific timezone added long value.
    public String getBrowserSpecificCustomDateLongValue(String longdateval, String browsertz);
        
    public int updateIndianTermsProductLevel(String productTermid,HashMap<String, Object> hm) throws ServiceException;
    
    public boolean checkTermusedInTransaction(String termid,boolean isSalesOrPurchase) throws ServiceException;
    
    public KwlReturnObject saveOrupdateUOBBankDetail(HashMap<String, Object> ibgBankDetailParams) throws ServiceException;
    
    public KwlReturnObject getUOBDetailsForAccount(String accountID, String companyID) throws ServiceException;
    
    public KwlReturnObject getUOBReceivingBankDetails(HashMap dataMap) throws ServiceException;
    
    public KwlReturnObject saveOrupdateOCBCBankDetail(HashMap<String, Object> ibgBankDetailParams) throws ServiceException;
    
    public KwlReturnObject getOCBCBankDetailsForAccount(String accountId, String companyId) throws ServiceException;
    
    public KwlReturnObject getFieldCombo(HashMap<String, Object> requestParams) throws ServiceException;
    
    public boolean checkInActiveAccounts(Account account) throws ServiceException;
    
    public HashMap validaterecorsingledHB(String moduleid, String recordid, String companyid) throws ServiceException, JSONException ;
    
    public KwlReturnObject getJsonKeyMapping(String defaultHeaderid) throws ServiceException; 
     
    public KwlReturnObject getAccountsMappedToCustomerVendor(String companyid,boolean isCustomer) throws ServiceException;
    
    public int updateAccountDefaultGroup(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getLineLevelTermsfromAccount(String accountid, String companyid) throws ServiceException ;
    
    public KwlReturnObject getLineLevelTerms(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getAddressFieldDimensionMapping(HashMap<String, Object> requestParams) throws ServiceException;
    
    public void addMasterItemsToProductTaxClass(HashMap<String, Object> defaultCompSetupMap, JSONObject setUpData)throws ServiceException;
    
    public KwlReturnObject getTaxesAndTermsUsingAccountId(String accountID, String companyId) throws ServiceException;
    
    public KwlReturnObject getMasterDataForGSTFields(JSONObject paramJobj) throws ServiceException ;
    
    public void copyTDSPayableAccountAndMapToMasterItemsNOP(HashMap<String, Object> defaultCompSetupMap)throws ServiceException;

    public List getTransactionBasedOpeningBalance(Map<String, Object> requestParams) throws ServiceException, JSONException;
    
    public String getAdvanceSearchStringForMultiEntity(String Searchjson, String companyId) throws ServiceException;
    
    public List getDefaultAccountOpeningBalance(Map<String, Object> requestParams) throws ServiceException, JSONException, SessionExpiredException ;
    
    public KwlReturnObject getLineLevelTerms(JSONObject json) throws ServiceException;
    
    public JSONObject getBudgetVsCostReportDetails(HashMap<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getFieldParams(String fieldname,int moduleid,String companyID) throws ServiceException;
    
    public JSONObject getMasterItemsDimension(HashMap<String, Object> requestParams) throws ServiceException;
    
    public List getFieldComboData(HashMap<String, Object> requestMap) throws ServiceException;
    
    public JSONObject getActualVsBudgetReportDetails(HashMap<String, Object> requestMap) throws ServiceException;
    
    public JSONObject getForecastingReportDetails(HashMap<String, Object> requestMap) throws ServiceException;
    
    public JSONObject getChangeOrderStatusReportDetails(HashMap<String, Object> requestMap) throws ServiceException;
    
    public List getLimitedAccountsOfMasterForm(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONObject saveLimitedAccounts(HashMap<String, Object> requestParams) throws ServiceException, JSONException;
    
    public List getLimitedAccountsForCombo(HashMap<String, Object> requestParams) throws ServiceException;
    
    public List isAccountsUsed(List ls,String companyid) throws ServiceException;
    
    public boolean removeEntryFromAccountUsedIn(String accType, String companyId, String coaId) throws ServiceException;
    
    public JSONArray createCustomFieldValueArray(AccCustomData accCustomData, HashMap<String,Object> params) throws JSONException, SessionExpiredException, ServiceException,ParseException;
    
    public KwlReturnObject getBankAccountMappingDetails(JSONObject paramsJobj) throws ServiceException;
    
    public void saveOrUpdateBankAccountMappingDetails(Map<String, Object> requestParams) throws ServiceException;
    
    public void deleteBankAccountMappingDetails(JSONObject paramsJobj) throws ServiceException;
}
