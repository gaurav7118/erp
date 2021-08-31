/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.companypreferences;

import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AssetDetails;
import com.krawler.hql.accounting.ClosingAccountBalance;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.YearLock;
import com.krawler.hql.accounting.companypreferenceservice.AccCompanyPreferencesServiceImpl;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingDashboardDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.reports.AccReportsService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.joda.time.DateTime;

/**
 *
 * @author krawler
 */
public class AccCompanyPreferencesControllerCMNServiceImpl implements AccCompanyPreferencesControllerCMNService {

    private AccProductService accProductService;
    private accProductDAO accProductDAO;
    private AccountingHandlerDAO accountingHandlerDAO;
    private accCompanyPreferencesDAO accCompanyPreferencesDAO;
    private AccReportsService accReportsService;
    private AccountingDashboardDAO accountingDashboardDAO;

    /**
     *
     * @param accProductService
     */
    public void setAccProductService(AccProductService accProductService) {
        this.accProductService = accProductService;
    }

    /**
     *
     * @param accProductDAO
     */
    public void setAccProductDAO(accProductDAO accProductDAO) {
        this.accProductDAO = accProductDAO;
    }

    /**
     *
     * @param accountingHandlerDAO
     */
    public void setAccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAO) {
        this.accountingHandlerDAO = accountingHandlerDAO;
    }

    /**
     *
     * @param accCompanyPreferencesDAO
     */
    public void setAccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesDAO) {
        this.accCompanyPreferencesDAO = accCompanyPreferencesDAO;
    }

    /**
     *
     * @param accReportsService
     */
    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }

    public void setAccountingDashboardDAO(AccountingDashboardDAO accountingDashboardDAO) {
        this.accountingDashboardDAO = accountingDashboardDAO;
    }

    /**
     * Method is used to validate the year-end checklist i.e. there should not
     * be negative stock present for selected year and depreciation for the
     * asset is posted.
     *
     * @param requestJSON
     * @return
     * @throws SessionExpiredException
     * @throws ParseException
     * @throws JSONException
     * @throws ServiceException
     */
    @Override
    public JSONObject checkYearEndClosingCheckList(JSONObject requestJSON) throws SessionExpiredException, ParseException, JSONException, ServiceException {
        JSONObject json = new JSONObject();
        String companyid = requestJSON.optString(Constants.companyKey);
        SimpleDateFormat df = (SimpleDateFormat) authHandler.getUserDateFormatterJson(requestJSON);
        String startDateString = requestJSON.optString(Constants.REQ_startdate);
        String endDateString = requestJSON.optString(Constants.REQ_enddate);
        if (!StringUtil.isNullOrEmpty(startDateString) && !StringUtil.isNullOrEmpty(endDateString) && !StringUtil.isNullOrEmpty(companyid)) {
            HashMap<String, Object> map = new HashMap<>();
            map.put(Constants.companyKey, companyid);
            map.put(Constants.REQ_enddate, endDateString);
            map.put(Constants.REQ_startdate, startDateString);
            map.put(Constants.df, df);
            boolean isNegativeStockPresent = accProductService.isNegativeStockPresent(map);
            json.put("negativeStockPresent", isNegativeStockPresent);

            boolean isDepreciationNotPosted = isDepreciationNotPosted(requestJSON);
            json.put("isDepreciationNotPosted", isDepreciationNotPosted);

            KwlReturnObject pendingdocuments = accountingDashboardDAO.getPendingApprovalDetails(map);
            int pendingDocumentCount = pendingdocuments.getRecordTotalCount();
            json.put("isPendingDocumentsPresent", (pendingDocumentCount > 0));
            /**
             * do not check drafted documents While closing the year refer SDP-11608 .
             */
//            KwlReturnObject draftdocuments = accountingDashboardDAO.getDraftDocuments(map);
//            int draftDocumentCount = draftdocuments.getRecordTotalCount();
            json.put("isDraftDocumentsPresent", false);

            KwlReturnObject recurringdocuments = accountingDashboardDAO.getrecurringdocuments(map);
            int recurringdocumentsCount = recurringdocuments.getRecordTotalCount();
            json.put("isrecurringtransactionNotPosted", (recurringdocumentsCount > 0));

            /*
             * checks difference in opening bal while closing the year ERP-33696
             */
            double bals[] = accReportsService.getOpeningBalancesWithDate(requestJSON, companyid, new Date(1970), df.parse(endDateString));
            double balance = bals[0] + bals[1];
            balance = authHandler.round(balance, requestJSON.optString("company"));
            json.put("isDiffInOpeningBalancePresent", (balance != 0));

            /* check if book is closed or not */
            if (!StringUtil.isNullOrEmpty(requestJSON.optString("yearid"))) {
                int yearid = requestJSON.optInt("yearid");
                KwlReturnObject compaccprefKwlReturnObject = accountingHandlerDAO.getObject(CompanyAccountPreferences.class.getName(), requestJSON.optString(Constants.companyKey));
                CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) compaccprefKwlReturnObject.getEntityList().get(0);
                Date finanDate = companyAccountPreferences.getFirstFinancialYearFrom() != null ? companyAccountPreferences.getFirstFinancialYearFrom() : companyAccountPreferences.getFinancialYearFrom();
                Calendar fyDate = Calendar.getInstance();
                fyDate.setTime(finanDate);
                int tempYearId = fyDate.get(Calendar.YEAR);
                if (yearid > tempYearId) {
                    for (; yearid > tempYearId; tempYearId++) {
                        requestJSON.put("yearid", tempYearId);
                        boolean isBookClose = accCompanyPreferencesDAO.isBookClose(requestJSON);
                        if (!isBookClose) {
                            json.put("isBookCloseInvalid", true);
                            break;
                        }
                    }
                }
            }
        }
        return json;
    }

    /**
     * Method is used to check the depreciation is posted for the year or not.
     *
     * @param requestJSON
     * @return <code>true</code> If there is depreciation to be posted. <code>false</code> If depreciation is already posted for the year.
     * @throws SessionExpiredException
     * @throws ParseException
     * @throws ServiceException
     */
    private boolean isDepreciationNotPosted(JSONObject requestJSON) throws ServiceException, ParseException, SessionExpiredException, JSONException {
        boolean isDepreciationNotPosted = false;
        try {
            SimpleDateFormat df = (SimpleDateFormat) authHandler.getUserDateFormatterJson(requestJSON);
            KwlReturnObject jeresult = accountingHandlerDAO.getObject(ExtraCompanyPreferences.class.getName(), requestJSON.optString(Constants.companyKey));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) jeresult.getEntityList().get(0);
            int depreciationCalculationType = extraCompanyPreferences.getAssetDepreciationCalculationType();
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyId", requestJSON.optString(Constants.companyKey));
            requestParams.put("invrecord", true);
            requestParams.put("fromYearEndClose", true);
            KwlReturnObject result = accProductDAO.getAssetDetails(requestParams);
            Date startDate = (Date) df.parse(requestJSON.optString(Constants.REQ_startdate));
            Date endDate = (Date) df.parse(requestJSON.optString(Constants.REQ_enddate));
            List<AssetDetails> list = result.getEntityList();
            for (AssetDetails assetDetails : list) {
                if (isDepreciationNotPosted) {
                    break;
                }
                /*Below code is used to check if asset is already expired or not*/
                boolean isAssetExpiryBeforeStartDate = false, isAssetExpiryBeforeEndDate = false;
                Date assetEndLifeDate = null;
                List expireListStartDate = accProductDAO.isAssetExpire(assetDetails, startDate);
                List expireListEndDate = accProductDAO.isAssetExpire(assetDetails, endDate);
                if (expireListStartDate != null && expireListStartDate.size() > 0) {
                    isAssetExpiryBeforeStartDate = (Boolean) expireListStartDate.get(0);
                }
                if (expireListEndDate != null && expireListEndDate.size() > 0) {
                    isAssetExpiryBeforeEndDate = (Boolean) expireListEndDate.get(0);
                }
                Calendar startDateCal = Calendar.getInstance();
                startDateCal.setTime(startDate);
                Calendar endDateCal = Calendar.getInstance();
                endDateCal.setTime(endDate);
                Calendar assetInstallationDateCal = Calendar.getInstance();
                assetInstallationDateCal.setTime(assetDetails.getInstallationDate());
                Calendar assetLifeEndDateCal = Calendar.getInstance();
                int startYear = startDateCal.get(Calendar.YEAR), endYear = endDateCal.get(Calendar.YEAR);
                String backyears = "";
                while (startYear <= endYear) {
                    backyears += startYear + ",";
                    startYear++;
                }
                List<String> yearList = Arrays.asList(backyears.split("\\s*,\\s*"));
                Collections.sort(yearList);
                int yeardiffDep = 0, k = 0;
                int periodDep = -1;
                for (yeardiffDep = 0, k = 0; k < yearList.size(); k++, yeardiffDep++) {
                    if (isDepreciationNotPosted) {
                        break;
                    }
                    int startMonth = 0;
                    int endMonth = 11;
                    int year = Integer.parseInt(yearList.get(k)); //Back Years
                    int assetInstallationYear = assetInstallationDateCal.get(Calendar.YEAR);           //Installation Year
                    yeardiffDep = year - assetInstallationYear;
                    if (assetInstallationYear < year) {
                        startMonth = 0;
                        if (!isAssetExpiryBeforeEndDate && !isAssetExpiryBeforeStartDate) { // asset is not expired in selected year
                            if ((yearList.size() - 1) == k) {
                                endMonth = endDateCal.get(Calendar.MONTH);
                            } else {
                                endMonth = 11;
                            }
                        } else if (!isAssetExpiryBeforeStartDate && isAssetExpiryBeforeEndDate) { // asset is going to expire in selected year 
                            long assetenddateinlong = (Long) expireListEndDate.get(1);
                            assetEndLifeDate = new Date(assetenddateinlong);
                            assetLifeEndDateCal.setTime(assetEndLifeDate);
                            int assetLifeEndYear = assetLifeEndDateCal.get(Calendar.YEAR);
                            if (assetLifeEndYear < year) {// if asset life already ended before the year
                                endMonth = -1;
                            } else if (assetLifeEndYear == year) {
                                endMonth = assetLifeEndDateCal.get(Calendar.MONTH);
                            } else {
                                endMonth = 11;
                            }
                        } else { // asset is already expired
                            startMonth = 0;
                            endMonth = -1;
                        }
                    } else if (assetInstallationYear == year) {
                        if (assetInstallationDateCal.get(Calendar.MONTH) < startDateCal.get(Calendar.MONTH)) {
                            startMonth = startDateCal.get(Calendar.MONTH);
                        } else {
                            startMonth = assetInstallationDateCal.get(Calendar.MONTH);
                        }
                        if (!isAssetExpiryBeforeEndDate && !isAssetExpiryBeforeStartDate) { // asset is not expired in selected year
                            if ((yearList.size() - 1) == k) {
                                endMonth = endDateCal.get(Calendar.MONTH);
                            } else {
                                endMonth = 11;
                            }
                        } else if (!isAssetExpiryBeforeStartDate && isAssetExpiryBeforeEndDate) { // asset is going to expire in selected year 
                            long assetenddateinlong = (Long) expireListEndDate.get(1);
                            assetEndLifeDate = new Date(assetenddateinlong);
                            assetLifeEndDateCal.setTime(assetEndLifeDate);
                            int assetLifeEndYear = assetLifeEndDateCal.get(Calendar.YEAR);
                            if (assetLifeEndYear < year) { // if asset life already ended before the year
                                endMonth = -1;
                            } else if (assetLifeEndYear == year) {
                                endMonth = assetLifeEndDateCal.get(Calendar.MONTH);
                            } else {
                                endMonth = 11;
                            }
                        } else { // asset is already expired
                            startMonth = 0;
                            endMonth = -1;
                        }
                    } else { // if asset installation date > than the year
                        continue;
                    }
                    if (depreciationCalculationType == 0) {
                        if (yeardiffDep != 0) {
                            yeardiffDep = - 1;
                        }
                    }
                    for (int i = startMonth; i < endMonth; i++) {
                        if (depreciationCalculationType == 0) {
                            if (yeardiffDep < 0) {  //if selected year is less than the cretion year then there will be no depreciation to show
                                continue;
                            }
                            periodDep = yeardiffDep + 1;
                        } else {
                            periodDep = (12 * yeardiffDep) + i + 1;
                            periodDep = assetInstallationDateCal.get(Calendar.MONTH) != 0 ? periodDep - assetInstallationDateCal.get(Calendar.MONTH) : periodDep;
                        }
                        HashMap<String, Object> filters = new HashMap<>();
                        filters.put("period", periodDep);
                        filters.put("assetDetailsId", assetDetails.getId());
                        filters.put("companyid", requestJSON.optString(Constants.companyKey));
                        filters.put("assetDetails", true);
                        KwlReturnObject dresult = accProductDAO.getAssetDepreciationDetail(filters);
                        if (dresult.getEntityList().isEmpty()) {
                            isDepreciationNotPosted = true;
                            break;
                        }
                    }
                }
            }
        } catch (ServiceException | NumberFormatException ex) {
            isDepreciationNotPosted = true;
        }
        return isDepreciationNotPosted;
    }

    /**
     * Calculate Stock In Hand, Net Profit/Loss(With Stock), Net
     * Profit/Loss(Without Stock) and store it in ClosingAccountBalance.
     *
     * @param yearLock
     * @param requestJSON
     * @param extraCompanyPreferences
     * @param companyAccountPreferences
     * @throws ServiceException
     * @throws SessionExpiredException
     */
    @Override
    public void calculateAndStoreClosingAccountBalance(YearLock yearLock, JSONObject requestJSON, ExtraCompanyPreferences extraCompanyPreferences, CompanyAccountPreferences companyAccountPreferences) throws ServiceException, SessionExpiredException {
        try {
            if (yearLock.isIsLock()) {
                List<ClosingAccountBalance> closingAccountBalances = new ArrayList<>();
                /**
                 * Get the startdate and enddate for the closing year using FY
                 * date
                 */
                Calendar startFinYearCal = Calendar.getInstance();
                Calendar endFinYearCal = Calendar.getInstance();
                startFinYearCal.setTime(companyAccountPreferences.getFirstFinancialYearFrom() != null ? companyAccountPreferences.getFirstFinancialYearFrom() : companyAccountPreferences.getFinancialYearFrom());
                endFinYearCal.set(Calendar.YEAR, yearLock.getYearid() + 1);
                endFinYearCal.set(Calendar.DATE, startFinYearCal.get(Calendar.DAY_OF_MONTH) - 1);
                endFinYearCal.set(Calendar.MONTH, startFinYearCal.get(Calendar.MONTH));
                startFinYearCal.set(Calendar.YEAR, yearLock.getYearid());
                Date startDate = startFinYearCal.getTime();
//                Date endDate = endFinYearCal.getTime();
                Date endDate = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(endFinYearCal.getTime()));
                Date openBalEndDate = new DateTime(startDate).minusDays(1).toDate();
                /**
                 * Calculate closing for the closing year
                 */
                HashMap<String, Object> requestParam = new HashMap<>();
                requestParam.put(Constants.REQ_startdate, authHandler.getDateOnlyFormat().format(startDate));
                requestParam.put(Constants.REQ_enddate, authHandler.getDateOnlyFormat().format(endDate));
                requestParam.put(Constants.companyKey, requestJSON.optString(Constants.companyKey));
                requestParam.put(Constants.df, authHandler.getDateOnlyFormat());
                double[] valuation = accProductService.getInventoryValuationDataForFinancialReports(requestParam);
                double closingStock = valuation[5];
                Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.INFO, "Closing Stock: {0}", closingStock);

                /**
                 * Add record for Stock In Hand for closing year
                 */
                ClosingAccountBalance stockInHand = new ClosingAccountBalance();
                stockInHand.setAmount(authHandler.round(closingStock,requestJSON.optString(Constants.companyKey)));
                stockInHand.setYearLock(yearLock);
                stockInHand.setCompany(companyAccountPreferences.getCompany());
                Date date = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(Calendar.getInstance().getTime()));
                stockInHand.setCreationDate(date);
                stockInHand.setYearId(yearLock.getYearid());
                stockInHand.setStockInHand(true);
                closingAccountBalances.add(stockInHand);
                /**
                 * Calculate Net Profit/Loss without stock figures if Show stock
                 * valuation in financial report is "FALSE"
                 */
                double[] profitAndLossList  = accReportsService.calculateProfitLossForTrialBalance(requestJSON, startDate, endDate, openBalEndDate, true, true, false, false, null,null);
                double openingNetPnLWithoutStock = profitAndLossList[0] + profitAndLossList[1];
                profitAndLossList = accReportsService.calculateProfitLossForTrialBalance(requestJSON, startDate, endDate, startDate, false, true, true, false, null,null);
                double periodNetPnLWithoutStock = profitAndLossList[0] + profitAndLossList[1];
                double endingNetPnLWithoutStock = openingNetPnLWithoutStock + periodNetPnLWithoutStock;
                Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.INFO, "Net Profit/Loss without stock valuation: {0}", endingNetPnLWithoutStock);
                /**
                 * Add record for Net Profit/Loss without stock for closing year
                 */
                ClosingAccountBalance netPnlWithoutStock = new ClosingAccountBalance();
                netPnlWithoutStock.setAmount(authHandler.round(endingNetPnLWithoutStock, requestJSON.optString(Constants.companyKey)));
                netPnlWithoutStock.setYearLock(yearLock);
                netPnlWithoutStock.setCompany(companyAccountPreferences.getCompany());
                netPnlWithoutStock.setCreationDate(date);
                netPnlWithoutStock.setYearId(yearLock.getYearid());
                netPnlWithoutStock.setNetProfitAndLossWithOutStock(true);
                closingAccountBalances.add(netPnlWithoutStock);
                /**
                 * Calculate Net Profit/Loss with stock figures if Show stock
                 * valuation in financial report is "TRUE"
                 */
                profitAndLossList = accReportsService.calculateProfitLossForTrialBalance(requestJSON, startDate, endDate, openBalEndDate, true, true, false, true, null,null);
                double openingNetPnLWithStock = profitAndLossList[0] + profitAndLossList[1];
                profitAndLossList = accReportsService.calculateProfitLossForTrialBalance(requestJSON, startDate, endDate, startDate, false, true, true, true, null,null);
                double periodNetPnLWithStock = profitAndLossList[0] + profitAndLossList[1];
                double endingNetPnLWithStock = openingNetPnLWithStock + periodNetPnLWithStock;
                Logger.getLogger(AccCompanyPreferencesServiceImpl.class.getName()).log(Level.INFO, "Net Profit/Loss with stock valuation: {0}", endingNetPnLWithStock);
                /**
                 * Add record for Net Profit/Loss with stock for closing year
                 */
                ClosingAccountBalance netPnLwithStock = new ClosingAccountBalance();
                netPnLwithStock.setAmount(authHandler.round(endingNetPnLWithStock, requestJSON.optString(Constants.companyKey)));
                netPnLwithStock.setYearLock(yearLock);
                netPnLwithStock.setCompany(companyAccountPreferences.getCompany());
                netPnLwithStock.setCreationDate(date);
                netPnLwithStock.setYearId(yearLock.getYearid());
                netPnLwithStock.setNetProfitAndLossWithStock(true);
                closingAccountBalances.add(netPnLwithStock);
                accCompanyPreferencesDAO.saveOrUpdateClosingBalanceObj(closingAccountBalances);
            } else {
                accCompanyPreferencesDAO.deleteClosingBalance(yearLock);
            }
        } catch (ServiceException | ParseException exception) {
            Logger.getLogger(AccCompanyPreferencesControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, exception);
            throw ServiceException.FAILURE("calculateAndStoreClosingAccountBalance : " + exception.getMessage(), exception);
        }
    }
    /**
     * Method to check- Sales trasnactions transaction are present
     * or not.
     *
     */
    public boolean isSalesSideTransactionPresent(JSONObject requestParamsJson) throws ServiceException {
        boolean isSalesSideTransactionPresent = false;
        try {
            KwlReturnObject salesresult = accCompanyPreferencesDAO.checkSalesSideTransactionPresent(requestParamsJson);
            if (salesresult != null && !salesresult.getEntityList().isEmpty()) {
                isSalesSideTransactionPresent = true;
            }
        } catch (Exception ex) {
            isSalesSideTransactionPresent = true;
        }
        return isSalesSideTransactionPresent;
    }

    /**
     * Method to check-  Purchase transaction are present or not.
     *
     */
    public boolean isPurchaseSideTransactionPresent(JSONObject requestParamsJson) throws ServiceException {
        boolean isPurchaseSideTransactionPresent = false;
        try {
            KwlReturnObject Purchaseresult = accCompanyPreferencesDAO.checkPurchaseSideTransactionPresent(requestParamsJson);
            if (Purchaseresult != null && !Purchaseresult.getEntityList().isEmpty()) {
                isPurchaseSideTransactionPresent = true;
            }

        } catch (Exception ex) {
            isPurchaseSideTransactionPresent = true;
        }
        return isPurchaseSideTransactionPresent;

    }
}
