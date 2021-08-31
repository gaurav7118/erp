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
package com.krawler.spring.accounting.productmodule.service;

import com.krawler.accounting.integration.common.IntegrationCommonService;
import com.krawler.common.admin.PricingBandmappingWithVolumeDisc;
import com.krawler.common.admin.PricingBandMasterDetail;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.util.LicenseType;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.frequency.Frequency;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.packaging.Packaging;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductController;
import static com.krawler.spring.accounting.product.accProductController.getActualFileName;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.uom.accUomDAO;
import com.krawler.spring.accounting.uom.service.AccUomService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.lang.mutable.MutableInt;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class AccProductModuleServiceImpl implements AccProductModuleService {

    private accProductDAO accProductObj;
    private accAccountDAO accAccountDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private auditTrailDAO auditTrailObj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accCustomerDAO accCustomerDAOobj;
    private MessageSource messageSource;
    private IntegrationCommonService integrationCommonService;
    private accCurrencyDAO accCurrencyDAOobj;
    private exportMPXDAOImpl exportDaoObj;
    private ImportDAO importDao;
    private fieldManagerDAO fieldManagerDAOobj;
    private accUomDAO accUomObj;
    private AccUomService accUomService;
    
    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    
    public void setIntegrationCommonService(IntegrationCommonService integrationCommonService) {
        this.integrationCommonService = integrationCommonService;
    }
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }
    
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    
    public exportMPXDAOImpl getExportDaoObj() {
        return exportDaoObj;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    public void setaccUomDAO(accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
    }
    public void setaccUomService(AccUomService accUomService) {
        this.accUomService = accUomService;
    }
 @Override
    public JSONObject getIndividualProductPrice(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateOnlyFormat = authHandler.getDateOnlyFormatter(request);
            String date = (String) requestParams.get("transactiondate");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean carryin = Boolean.parseBoolean(request.getParameter("carryin"));
            Date transactionDate = null;
            try {
                transactionDate = (date == null ? null : df.parse(date));
            } catch (ParseException ex) {
                try {
                    Calendar cal = Calendar.getInstance();
                    long dateValue = (long) Long.parseLong(date.toString());
                    cal.setTimeInMillis(dateValue);
                    transactionDate = cal.getTime();
                    String tdate=authHandler.getDateOnlyFormat().format(transactionDate);
                    try{
                        transactionDate=authHandler.getDateOnlyFormat().parse(tdate);
                    }catch(ParseException e){
                        transactionDate = cal.getTime();
                    }
                } catch (Exception exx) {
                    Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE("getProducts : " + exx.getMessage(), exx);
                }
            }

            String productid[] = ((String) requestParams.get(Constants.productid)).split(",");

            for (int i = 0; i < productid.length; i++) {
                JSONObject obj = new JSONObject();
                KwlReturnObject result = accProductObj.getProductPrice(productid[i], carryin, transactionDate, (String) requestParams.get("affecteduser"), "");
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                if (itr.hasNext()) {
                    Object row = itr.next();
                    if (row == null) {
                        obj.put("price", 0);
                    } else {
                        obj.put("price", row);
                    }
                } else {
                    obj.put("price", 0);
                }
                obj.put(Constants.productid, productid[i]);
                jobj.append("data", obj);
            }
            if (productid.length == 1) {

                transactionDate = (transactionDate == null) ? new Date() : transactionDate;
                HashMap<String, Object> basicParams = new HashMap();
                basicParams.put("productId", productid[0]);
                basicParams.put("companyId", companyid);
                basicParams.put("transactionDate", transactionDate);
                basicParams.put("dateFormat", dateOnlyFormat);

                HashMap<String, Object> fieldrequestParams = new HashMap();
                Map<String, Object> variableMap = new HashMap<String, Object>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                AccProductCustomData obj = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), productid[0]);
                if (obj != null) {
                    productHandler.setCustomColumnValuesForProduct(obj, FieldMap, replaceFieldMap, variableMap);
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            JSONObject jsonObj = new JSONObject();
                            jsonObj.put(varEntry.getKey(), coldata);
                            jsonObj.put("key", varEntry.getKey());
                            jobj.append("data", jsonObj);
                        }
                    }
                }
            }
            //jobj = productHandler.getIndividualProductsPriceJson(request, listFinal);

            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    
    
    @Override
    public AssetMaintenanceSchedulerObject saveAssetMaintenanceSchedule(HttpServletRequest request, String contractId) throws SessionExpiredException, ServiceException, AccountingException {
        AssetMaintenanceSchedulerObject schedulerObject = null;
        String auditmsg = " and following events has ";
        String startenddate = "";
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            int scheduleType = 0;

            if (!StringUtil.isNullOrEmpty(contractId)) {// if contract schedule then scheduleType==1
                scheduleType = 1;
            }

            String assetId = request.getParameter("assetId");
            String scheduleNumber = request.getParameter("scheduleNumber");
            
            String type = request.getParameter("type");
            int maintenanceType = 0;
            if (!StringUtil.isNullOrEmpty(type)) {// if contract schedule then scheduleType==1
                maintenanceType = Integer.parseInt(type);
            }
            String scheduleId = request.getParameter("scheduleId");

            DateFormat df = authHandler.getDateOnlyFormat(request);

            Date scheduleStartDate = null;

            String scheduleStartDateStr = request.getParameter("scheduleStartDate");

            if (!StringUtil.isNullOrEmpty(scheduleStartDateStr)) {
                scheduleStartDate = authHandler.getDateOnlyFormat(request).parse(scheduleStartDateStr);
            }

            Date currentDate = null;

            String currentDateStr = request.getParameter("hiddenCurrentDate");

            if (!StringUtil.isNullOrEmpty(currentDateStr)) {
                currentDate = authHandler.getDateOnlyFormat(request).parse(currentDateStr);
            }


            Date scheduleEndDate = null;

            String scheduleEndDateStr = request.getParameter("scheduleEndDate");

            if (!StringUtil.isNullOrEmpty(scheduleEndDateStr)) {
                scheduleEndDate = authHandler.getDateOnlyFormat(request).parse(scheduleEndDateStr);
            }

            boolean isAdhocSchedule = false;
             /*
              *If user is edit total event then  isScheduleEdit is true
              */ 
            boolean isScheduleEdit = false;

            if (!StringUtil.isNullOrEmpty(request.getParameter("isAdHocSchedule"))) {
                isAdhocSchedule = Boolean.parseBoolean(request.getParameter("isAdHocSchedule"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("isScheduleBtn"))) {
                isScheduleEdit = Boolean.parseBoolean(request.getParameter("isScheduleBtn"));
            }

            boolean isTotalEventsStopCondition = false;

            if (!StringUtil.isNullOrEmpty(request.getParameter("totalEventsStopCondition"))) {
                isTotalEventsStopCondition = Boolean.parseBoolean(request.getParameter("totalEventsStopCondition"));
            }

            boolean isEndDateStopCondition = false;

            if (!StringUtil.isNullOrEmpty(request.getParameter("endDateStopCondition"))) {
                isEndDateStopCondition = Boolean.parseBoolean(request.getParameter("endDateStopCondition"));
            }

            int frequency = 0;

            if (!StringUtil.isNullOrEmpty(request.getParameter("repeatInterval"))) {
                frequency = Integer.parseInt(request.getParameter("repeatInterval"));
            }

            String frequencyType = "";

            if (!StringUtil.isNullOrEmpty(request.getParameter("intervalType"))) {
                frequencyType = request.getParameter("intervalType");
            }

            int totalSchedules = 0;

            if (!StringUtil.isNullOrEmpty(request.getParameter("totalEvents"))) {
                totalSchedules = Integer.parseInt(request.getParameter("totalEvents"));
            }

            int scheduleDuration = 0;

            if (!StringUtil.isNullOrEmpty(request.getParameter("scheduleDuration"))) {
                scheduleDuration = Integer.parseInt(request.getParameter("scheduleDuration"));
            }

            HashMap<String, Object> requestMap = new HashMap<String, Object>();



            if (!StringUtil.isNullOrEmpty(scheduleId)) {// for edit case

                KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceSchedulerObject.class.getName(), scheduleId);
                schedulerObject = (AssetMaintenanceSchedulerObject) scObj.getEntityList().get(0);

                if ((scheduleType != 1) && (df.parse(df.format(schedulerObject.getStartDate())).equals(currentDate) || df.parse(df.format(schedulerObject.getStartDate())).before(currentDate))) {// if schedule start date is equal to current date or before current date it should not be edit. i.e if on schedule has been started then it cannot be commit
                    throw new AccountingException(messageSource.getMessage("acc.maintenance.schedule.name", null, RequestContextUtils.getLocale(request))+" '" + scheduleNumber + "' "+ messageSource.getMessage("acc.MaintenanceSchedules.hasbeenstartedsoitcannotbeEdit", null, RequestContextUtils.getLocale(request)));
                }

                // Check any work order exist for this schedule or not, if exist then it cannot be edit
                if (!schedulerObject.getAssetMaintenanceSchedulers().isEmpty()) {
                    Set<AssetMaintenanceScheduler> maintenanceSchedulers = schedulerObject.getAssetMaintenanceSchedulers();
                    for (AssetMaintenanceScheduler maintenanceScheduler : maintenanceSchedulers) {
                        HashMap<String, Object> woMap = new HashMap<String, Object>();

                        woMap.put("scheduleId", maintenanceScheduler.getId());

                        woMap.put("companyId", companyId);

                        KwlReturnObject woResult = accProductObj.getAssetMaintenanceWorkOrders(woMap);

                        if (woResult != null && !woResult.getEntityList().isEmpty()) {
                            AssetMaintenanceWorkOrder workOrder = (AssetMaintenanceWorkOrder) woResult.getEntityList().get(0);
                            throw new AccountingException("Schedule number '" + scheduleNumber + "' has Work Order '" + workOrder.getWorkOrderNumber() + "' Linked with it. so it cannot be delete");
                        }
                    }
                }
                //delete data of this schedule

                HashMap<String, Object> dataMap = new HashMap<String, Object>();

                dataMap.put("id", scheduleId);

                dataMap.put("companyId", companyId);

                KwlReturnObject result = accProductObj.deleteAssetMaintenanceSchedule(dataMap);

                requestMap.put("id", scheduleId);
            } else {
                // check weather schedule number exist or not
                KwlReturnObject wocnt = accProductObj.getScheduleNumberCount(scheduleNumber, companyId);

                if (wocnt.getRecordTotalCount() > 0) {
                    throw new AccountingException("Schedule number '" + scheduleNumber + "' already exists.");
                }
            }
            // Save AssetMaintenanceSchedulerObject First
            requestMap.put("scheduleNumber", scheduleNumber);
            requestMap.put("startDate", scheduleStartDate);
            if (!isAdhocSchedule) {// in case of adhoc schedule there will be no any end date of schedule
                requestMap.put("endDate", scheduleEndDate);
            }
            requestMap.put("isAdhocSchedule", isAdhocSchedule);
            requestMap.put("frequency", frequency);
            requestMap.put("frequencyType", frequencyType);
            requestMap.put("totalSchedules", totalSchedules);
            requestMap.put("scheduleDuration", scheduleDuration);
            requestMap.put("companyId", companyId);
            requestMap.put("scheduleType", scheduleType);
            requestMap.put("maintenanceType", maintenanceType);
            if (scheduleType == 0) {// for asset schedule
                requestMap.put("assetId", assetId);
            } else if (scheduleType == 1) {// for Contrract schedule
                requestMap.put("contractId", contractId);
            }
            

            KwlReturnObject scResult = accProductObj.saveMaintenanceSchedulerObject(requestMap);

            schedulerObject = (AssetMaintenanceSchedulerObject) scResult.getEntityList().get(0);

            Date startDate = scheduleStartDate;

            Set<AssetMaintenanceScheduler> assetMaintenanceSchedulers = new HashSet<AssetMaintenanceScheduler>();
             /*
              *If user is edit total event then  isScheduleEdit is true and it work as adhoc functionality
              */ 
            if (isAdhocSchedule||(isScheduleEdit&&!isEndDateStopCondition)) {

                String adHocEventDetailsStr = request.getParameter("adHocEventDetails");

                JSONArray detailArray = new JSONArray(adHocEventDetailsStr);

                for (int i = 0; i < detailArray.length(); i++) {
                    JSONObject jobj = detailArray.getJSONObject(i);

                    Date eventStartDate = df.parse(jobj.getString("eventStartDate"));
                    Date eventEndDate = df.parse(jobj.getString("eventEndDate"));

                    HashMap<String, Object> dataMap = new HashMap<String, Object>();

                    dataMap.put("startDate", eventStartDate);
                    dataMap.put("endDate", eventEndDate);
                    dataMap.put("actualStartDate", eventStartDate);
                    dataMap.put("actualEndDate", eventEndDate);
                    dataMap.put("isAdhocSchedule", isAdhocSchedule);
                    dataMap.put("frequency", frequency);
                     /*
                      *If user is edit total event then  isScheduleEdit is true
                      */ 
                    if(isScheduleEdit){
                        dataMap.put("isScheduleEdit", true);
                    }
                    dataMap.put("frequencyType", frequencyType);
                    dataMap.put("totalSchedules", totalSchedules);
                    dataMap.put("scheduleDuration", scheduleDuration);
                    dataMap.put("companyId", companyId);
                    dataMap.put("scheduleType", scheduleType);
                    dataMap.put("maintenanceType", maintenanceType);
                    if (scheduleType == 0) {
                        dataMap.put("assetId", assetId);
                    }
                    dataMap.put("schedulerObjectId", schedulerObject.getId());

                    KwlReturnObject result = accProductObj.saveMaintenanceSchedule(dataMap);

                    AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) result.getEntityList().get(0);

                    assetMaintenanceSchedulers.add(scheduler);

                    SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
                    Date sd = sdf.parse(df.format(eventStartDate));
                    Date ed = sdf.parse(df.format(eventEndDate));
                    startenddate += " start date " + sdf.format(sd) + " and end date " + sdf.format(ed) + ",";
                }
                schedulerObject.setScheduleStopCondition(0);

            } else {

                if (isTotalEventsStopCondition) {
                    for (int i = 0; i < totalSchedules; i++) {

                        Date eventStartDate = null;
                        Date eventEndDate = null;
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(startDate);

                        // Calculate start date
                        if (frequencyType.equalsIgnoreCase("day")) {
                            cal.add(Calendar.DATE, i * frequency);
                        } else if (frequencyType.equalsIgnoreCase("week")) {
                            cal.add(Calendar.WEEK_OF_MONTH, i * frequency);
                        } else if (frequencyType.equalsIgnoreCase("month")) {
                            cal.add(Calendar.MONTH, i * frequency);
                        } else if (frequencyType.equalsIgnoreCase("year")) {
                            cal.add(Calendar.YEAR, i * frequency);
                        }

                        eventStartDate = cal.getTime();
                        String sdate=df.format(eventStartDate);
                        try{
                            eventStartDate=df.parse(sdate);
                        }catch(ParseException e){
                            eventStartDate = cal.getTime();
                        }
                        Calendar endcal = Calendar.getInstance();
                        endcal.setTime(eventStartDate);
                        endcal.add(Calendar.DATE, scheduleDuration - 1);//inclusing both days

                        eventEndDate = endcal.getTime();
                        String edate=df.format(eventEndDate);
                        try{
                            eventEndDate=df.parse(edate);
                        }catch(ParseException e){
                            eventEndDate = cal.getTime();
                        }
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();

                        dataMap.put("startDate", eventStartDate);
                        dataMap.put("endDate", eventEndDate);
                        dataMap.put("actualStartDate", eventStartDate);
                        dataMap.put("actualEndDate", eventEndDate);
                        dataMap.put("isAdhocSchedule", isAdhocSchedule);
                        dataMap.put("frequency", frequency);
                        dataMap.put("frequencyType", frequencyType);
                        dataMap.put("totalSchedules", totalSchedules);
                        dataMap.put("scheduleDuration", scheduleDuration);
                        dataMap.put("companyId", companyId);
                        dataMap.put("scheduleType", scheduleType);
                        if (scheduleType == 0) {
                            dataMap.put("assetId", assetId);
                        }
                        dataMap.put("schedulerObjectId", schedulerObject.getId());

                        KwlReturnObject result = accProductObj.saveMaintenanceSchedule(dataMap);

                        AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) result.getEntityList().get(0);

                        assetMaintenanceSchedulers.add(scheduler);

                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
                        Date sd = sdf.parse(df.format(eventStartDate));
                        Date ed = sdf.parse(df.format(eventEndDate));
                        startenddate += (" start date " + sdf.format(sd) + " and end date " + sdf.format(ed) + ",");

                    }
                    schedulerObject.setScheduleStopCondition(1);
                } else if (isEndDateStopCondition) {
                    totalSchedules = 0;
                    Calendar scal = Calendar.getInstance();
                    scal.setTime(startDate);

                    Calendar frequencyPeriodEndcal = Calendar.getInstance();
                    frequencyPeriodEndcal.setTime(startDate);


                    Calendar ecal = Calendar.getInstance();
                    ecal.setTime(scheduleEndDate);

                    int i = 0;
                    
                    String fdate=df.format(frequencyPeriodEndcal.getTime());
                    Date fpedate;
                    try{
                        fpedate=df.parse(fdate);
                    }catch(ParseException ex){
                        fpedate=frequencyPeriodEndcal.getTime();
                    }
                    
                    String edate=df.format(ecal.getTime());
                    Date ecaldate;
                    try{
                        ecaldate=df.parse(edate);
                    }catch(ParseException ex){
                        ecaldate=ecal.getTime();
                    }
                    while (fpedate.before(ecaldate) && ((ecal.getTime().getTime() - frequencyPeriodEndcal.getTime().getTime()) / (1000 * 60 * 60 * 24)) >= (scheduleDuration - 1)) {// if start date is before schedule end date and difference between start date and endDate(inclusive both dates) must be equal or greater than schedule event duration

                        scal.setTime(startDate);

                        if (frequencyType.equalsIgnoreCase("day")) {
                            scal.add(Calendar.DATE, i * frequency);
                        } else if (frequencyType.equalsIgnoreCase("week")) {
                            scal.add(Calendar.WEEK_OF_MONTH, i * frequency);
                        } else if (frequencyType.equalsIgnoreCase("month")) {
                            scal.add(Calendar.MONTH, i * frequency);
                        } else if (frequencyType.equalsIgnoreCase("year")) {
                            scal.add(Calendar.YEAR, i * frequency);
                        }
//                        // Calculating end date of frequency Period
                        frequencyPeriodEndcal.setTime(scal.getTime());

                        if (frequencyType.equalsIgnoreCase("day")) {
                            frequencyPeriodEndcal.add(Calendar.DATE, frequency);
                        } else if (frequencyType.equalsIgnoreCase("week")) {
                            frequencyPeriodEndcal.add(Calendar.WEEK_OF_MONTH, frequency);
                        } else if (frequencyType.equalsIgnoreCase("month")) {
                            frequencyPeriodEndcal.add(Calendar.MONTH, frequency);
                        } else if (frequencyType.equalsIgnoreCase("year")) {
                            frequencyPeriodEndcal.add(Calendar.YEAR, frequency);
                        }



                        Calendar endcal = Calendar.getInstance();
                        endcal.setTime(scal.getTime());
                        endcal.add(Calendar.DATE, scheduleDuration - 1);//inclusing both days

                        Date eventEndDate = endcal.getTime();
                        
                        String endate=df.format(eventEndDate);
                        try{
                            eventEndDate=df.parse(endate);
                        }catch(ParseException ex){
                            eventEndDate=endcal.getTime();
                        }
                        
                        HashMap<String, Object> dataMap = new HashMap<String, Object>();
                        
                        Date strtDate = scal.getTime();
                        
                        String sdate=df.format(strtDate);
                        try{
                            strtDate=df.parse(sdate);
                        }catch(ParseException ex){
                            strtDate=scal.getTime();
                        }
                        
                        dataMap.put("startDate", strtDate);
                        dataMap.put("endDate", eventEndDate);
                        dataMap.put("actualStartDate", strtDate);
                        dataMap.put("actualEndDate", eventEndDate);
                        dataMap.put("isAdhocSchedule", isAdhocSchedule);
                        dataMap.put("frequency", frequency);
                        dataMap.put("frequencyType", frequencyType);
                        dataMap.put("totalSchedules", totalSchedules);
                        dataMap.put("scheduleDuration", scheduleDuration);
                        dataMap.put("companyId", companyId);
                        dataMap.put("scheduleType", scheduleType);
                        if (scheduleType == 0) {
                            dataMap.put("assetId", assetId);
                        }
                        dataMap.put("schedulerObjectId", schedulerObject.getId());

                        KwlReturnObject result = accProductObj.saveMaintenanceSchedule(dataMap);

                        AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) result.getEntityList().get(0);

                        assetMaintenanceSchedulers.add(scheduler);

                        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
                        Date sd = sdf.parse(df.format(scal.getTime()));
                        Date ed = sdf.parse(df.format(eventEndDate));
                        startenddate += (" start date " + sdf.format(sd) + " and end date " + sdf.format(ed) + ",");

                        i++;
                        totalSchedules++;
                    }

                    schedulerObject.setTotalEvents(totalSchedules);
                    schedulerObject.setScheduleStopCondition(2);
                }
            }

            schedulerObject.setAssetMaintenanceSchedulers(assetMaintenanceSchedulers);

            String action = "Added";
            String auditaction = AuditAction.ASSET_MAINTENANCE_SCHEDULE_ADDED;
            if (!StringUtil.isNullOrEmpty(scheduleId)) {
                action = "Updated";
                auditaction = AuditAction.ASSET_MAINTENANCE_SCHEDULE_UPDATED;
            }

            if (scheduleType == 0) {// Asset Maintenance Schedule
                String assetid = "";

                KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetDetails.class.getName(), assetId);
                AssetDetails asset = (AssetDetails) scObj.getEntityList().get(0);
                assetid = asset.getAssetId();

                auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Asset Maintenance Schedule " + scheduleNumber + " " + " of Asset " + assetid + " " + auditmsg + action + " " + (java.util.Arrays.toString(startenddate.split(","))), request, companyId);
            } else if (scheduleType == 1) {// Contract Maintenance Schedule

                auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " Contract Maintenance Schedule " + scheduleNumber + " " + auditmsg + action + " " + (java.util.Arrays.toString(startenddate.split(","))), request, companyId);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccProductModuleServiceImpl.saveAssetMaintenanceSchedule() -: " + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccProductModuleServiceImpl.saveAssetMaintenanceSchedule() -: " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccProductModuleServiceImpl.saveAssetMaintenanceSchedule() -: " + ex.getMessage(), ex);
        }

        return schedulerObject;

    }
    
    @Override
    public JSONObject getQualityControlJSON(List<QualityControl> list) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            for (QualityControl qualityControl : list) {
                JSONObject obj = new JSONObject();
                obj.put("qcid", qualityControl.getID());
                obj.put("qcbomcodeid", qualityControl.getBom()!=null ? qualityControl.getBom().getID() : "");
                obj.put("qcuom", qualityControl.getQcuom()!=null ? qualityControl.getQcuom().getID(): "");
                obj.put("uomname", qualityControl.getQcuom()!=null ? qualityControl.getQcuom().getNameEmptyforNA(): "");  //ERP-35191 : Measurement UOM Name
                obj.put("qcbomcode", qualityControl.getBom()!=null ? qualityControl.getBom().getBomCode() : "Default");
                obj.put("qcgroupid", qualityControl.getQcgroup()!=null ? qualityControl.getQcgroup().getID() : "");
                obj.put("qcgroup", qualityControl.getQcgroup()!=null ? qualityControl.getQcgroup().getValue() : "");
                obj.put("qcparameterid", qualityControl.getQcparameter()!=null ? qualityControl.getQcparameter().getID() : "");
                obj.put("qcparameter", qualityControl.getQcparameter()!=null ? qualityControl.getQcparameter().getValue() : "");
                obj.put("qcvalue", qualityControl.getQcvalue());
                obj.put("qcdescription", qualityControl.getQcdescription());                
                
                jsonArray.put(obj);
            }
            jobj.put("data", jsonArray);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getQualityControlJSON : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    
    // Function used to get the BOMDetail JSON
    public JSONObject getBOMDetailJSON(HttpServletRequest request, List<BOMDetail> list) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            for (BOMDetail bomdetail : list) {
                JSONObject obj = new JSONObject();
                obj.put("bomid", bomdetail.getID());
                obj.put("bomcode", bomdetail.getBomCode());
                obj.put("bomCode", bomdetail.getBomCode()); //This line added temporarily to handle BOM Code not getting in edit case issue in MRP module
                obj.put("bomName", bomdetail.getBomName());
                obj.put("isdefaultbom", bomdetail.isIsDefaultBOM());
                obj.put(Constants.productid, bomdetail.getProduct() != null ? bomdetail.getProduct().getID() : ""); //isAutoAssembly
                obj.put("isAutoAssembly", bomdetail.getProduct().isAutoAssembly());
                HashMap<String, Object> assemblyMap = new HashMap<>();
                assemblyMap.put(Constants.productid, bomdetail.getProduct() != null ? bomdetail.getProduct().getID() : "");
                assemblyMap.put("bomdetailid", bomdetail.getID());
                assemblyMap.put("currencyid", currencyid);
                assemblyMap.put("isdefaultbom", bomdetail.isIsDefaultBOM());  
                KwlReturnObject result = accProductObj.getAssemblyItems(assemblyMap);
                 /*Get request parameters */
                JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
                JSONObject assemblyJSON = getAssemblyItemsJson(paramJobj, result.getEntityList(),0);
                obj.put("bomAssemblyDetails", assemblyJSON.getJSONArray("data").toString());
                jsonArray.put(obj);
            }
            jobj.put("data", jsonArray);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getAssemblyItemsJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
     @Override
    public JSONObject getAssemblyItemsJson(JSONObject paramJobj, List list, int levelCount) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;
            String location = "";
            String warehouse = "";
            boolean isAssemblyProduct = false;
            boolean isForCompAvailablity = false;
            boolean isMRPJOBWORKOUT = false;
            double mrpProductQuantity = 0.0;
            double totalLockQuantity = 0.0;
            String productid = "";
           String subBOMid = "";
            if (paramJobj.optString("mrproductquantity",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("mrproductquantity"))) {
                mrpProductQuantity = Double.parseDouble(paramJobj.optString("mrproductquantity"));
            }
            if (paramJobj.optString("isForCompAvailablity",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("isForCompAvailablity"))) {
                isForCompAvailablity = Boolean.parseBoolean(paramJobj.optString("isForCompAvailablity"));
            }
            if (paramJobj.optString("isMRPJOBWORKOUT",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("isMRPJOBWORKOUT"))) {
                isMRPJOBWORKOUT = Boolean.parseBoolean(paramJobj.optString("isMRPJOBWORKOUT"));
            }
            String mainProductid = paramJobj.optString(Constants.productid,null) != null ? paramJobj.optString(Constants.productid) :"";
            JSONArray jArr = new JSONArray();
            Product product=null;
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                totalLockQuantity = 0.0;
                isAssemblyProduct = false;
                Object[] row = (Object[]) itr.next();
                ProductAssembly passembly = (ProductAssembly) row[0];
                JSONObject obj = new JSONObject();
                String subProductid = passembly.getSubproducts().getID();
                double availableRecyclableQuantity = 0;
                if (!StringUtil.isNullOrEmpty(passembly.getSubproducts().getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), passembly.getSubproducts().getID());
                    product = (Product) prodresult.getEntityList().get(0);
                    isLocationForProduct = product.isIslocationforproduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();
                    if (isForCompAvailablity) {
                        if (product.getProducttype().getID().equals(Producttype.ASSEMBLY)) {
                            isAssemblyProduct = true;
                            productid = product.getID();
                            subBOMid = passembly.getSubbom() != null ? passembly.getSubbom().getID() : "";
                        }
                        KwlReturnObject result2 = accProductObj.getAssemblyLockQuantity(product.getID());
                        Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));

                        KwlReturnObject result1 = accProductObj.getLockQuantity(product.getID());
                        Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));

                        KwlReturnObject woresult =accProductObj. getWOLockQuantity(product.getID());
                        Double WOLockQuantity = (Double) (woresult.getEntityList().get(0) == null ? 0.0 : woresult.getEntityList().get(0));

                        totalLockQuantity = assmblyLockQuantity + SoLockQuantity + WOLockQuantity;
                    }
                    isBatchForProduct = product.isIsBatchForProduct();
                    isSerialForProduct = product.isIsSerialForProduct();
                    isRowForProduct = product.isIsrowforproduct();
                    isRackForProduct = product.isIsrackforproduct();
                    isBinForProduct = product.isIsbinforproduct();
                    location = product.getLocation() != null ? product.getLocation().getId() : "";
                    warehouse = product.getWarehouse() != null ? product.getWarehouse().getId() : "";
                    availableRecyclableQuantity = product.getRecycleQuantity();
                }
                
                int ComponentType=0;
                ComponentType=passembly.getComponentType();
                
                double crate=0;
                crate=passembly.getCrate();
                
                obj.put("componentType", ComponentType);
                obj.put("crate", crate);
                obj.put("isLocationForProduct", isLocationForProduct);
                obj.put("isWarehouseForProduct", isWarehouseForProduct);
                obj.put("isBatchForProduct", isBatchForProduct);
                obj.put("isSerialForProduct", isSerialForProduct);
                obj.put("isRowForProduct", isRowForProduct);
                obj.put("isRackForProduct", isRackForProduct);
                obj.put("isBinForProduct", isBinForProduct);
                obj.put("location", location);
                obj.put("warehouse", warehouse);
                obj.put("id", passembly.getID());
                
                obj.put("pid", passembly.getSubproducts() != null ?passembly.getSubproducts().getProductid():"");
                obj.put(Constants.productid, passembly.getSubproducts().getID());            
                obj.put("uomname", product.getUnitOfMeasure() == null ? "" : product.getUnitOfMeasure().getNameEmptyforNA());
                obj.put("uomid", product.getUnitOfMeasure() == null ? "" : product.getUnitOfMeasure().getID());
                if(isMRPJOBWORKOUT){
                    obj.put("baseuomid", product.getUnitOfMeasure() == null ? "" : product.getUnitOfMeasure().getID());
                    obj.put("baseuomname", product.getUnitOfMeasure() == null ? "" : product.getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("multiuom", false); // Currently Not Handling for Multi UOM
                    obj.put("baseuomrate", 1);
                    obj.put("baseuomquantity",  passembly.getQuantity() * mrpProductQuantity);
                    obj.put("quantity",  passembly.getQuantity() * mrpProductQuantity);
                    obj.put("prdiscount", 0);
                    obj.put("discountispercent", 1);
                    obj.put("linkto", "");
                    obj.put("linkid", "");
                    obj.put("linktype", -1);
                }else{
                    obj.put("quantity", passembly.getQuantity());
                }
                obj.put("hasAccess", passembly.getSubproducts().isIsActive());
                obj.put("productname", passembly.getSubproducts().getName());
                obj.put("desc", passembly.getSubproducts().getDescription()==null?"": passembly.getSubproducts().getDescription());
                obj.put("producttype", passembly.getSubproducts().getProducttype().getID());
                obj.put("type", passembly.getSubproducts().getProducttype().getName());
                obj.put("isWastageApplicable", passembly.getSubproducts().isWastageApplicable());
                obj.put("isRecyclable", passembly.getSubproducts() != null ? passembly.getSubproducts().isRecyclable() : false );
                obj.put("recycleQuantity", passembly.getSubproducts()!= null ? passembly.getSubproducts().getRecycleQuantity(): 0 );
                /**
                 * row[1](Initial purchase price or purchase price) is null when product is made
                 * in foreign currency.(code for Product in edit mode from Product Master)
                 */
                if (row[1] == null && paramJobj.optBoolean(Constants.displayInitialPrice)) {
                    /**
                     * get Initial purchase prize.
                     */
                    KwlReturnObject result = accProductObj.getInitialPrice(subProductid, true);
                    if (!result.getEntityList().isEmpty() && result.getEntityList().get(0) != null) {
                        /**
                         * get Inventory Opening Balance date.
                         */
                        Date endDate = null;
                        KwlReturnObject rtObj = accProductObj.getInventoryOpeningBalanceDate(paramJobj.optString(Constants.companyKey));
                        List lst = rtObj.getEntityList();
                        Iterator ite = lst.iterator();
                        if (ite.hasNext()) {
                            endDate = (Date) ite.next();
                        }
                        if (endDate != null) {
                            /**
                             * Convert to base currency.
                             */
                            Map currencyParams = new HashMap();
                            currencyParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
                            currencyParams.put(Constants.globalCurrencyKey, paramJobj.optString(Constants.globalCurrencyKey));
                            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(currencyParams, (Double) result.getEntityList().get(0), product.getCurrency().getCurrencyID(), endDate, 0);

                            obj.put("purchaseprice", authHandler.roundUnitPrice((Double) bAmt.getEntityList().get(0), paramJobj.optString(Constants.companyKey)));
                        } else {
                            obj.put("purchaseprice", 0);
                        }
                    } else {
                        obj.put("purchaseprice", 0);
                    }
                } else {
                    obj.put("purchaseprice", row[1] == null ? 0 : row[1]);
                }
                obj.put("saleprice", row[2] == null ? 0 : row[2]);              
                double availableQuantity = passembly.getSubproducts() != null ? passembly.getSubproducts().getAvailableQuantity() : 0.0;
                double availableQuantityForUse = availableQuantity - totalLockQuantity;
                if (availableQuantityForUse < 0) {
                    availableQuantityForUse = 0.0;
                }
                obj.put("availablequantity", availableQuantityForUse);
                obj.put("outstandingquantity", totalLockQuantity);
                obj.put("blockquantity", 0.0);
                obj.put("actualquantity", passembly.getQuantity());
                double reqQuantity=0;
                double minpercentquantity=0;
                if (ComponentType == 2 || ComponentType == 3) {
                    reqQuantity = 0;
                    minpercentquantity = 0;
                } else {
                    reqQuantity = passembly.getQuantity() * mrpProductQuantity;
                    minpercentquantity = 100;
                }
                obj.put("requiredquantity", reqQuantity);
                double shortfallQuantity = reqQuantity - availableQuantityForUse;
                double reorderQuantity = passembly.getSubproducts() != null ? passembly.getSubproducts().getReorderQuantity() : 0.0;
                if (shortfallQuantity < 0) {
                    shortfallQuantity = shortfallQuantity < 0 ? 0.0 : shortfallQuantity;
                } else {
                    shortfallQuantity = shortfallQuantity < 0 ? 0.0 : shortfallQuantity;
                }
                obj.put("shortfallquantity", shortfallQuantity);
                obj.put("orderquantity", reorderQuantity);
                if (shortfallQuantity > 0) {
                    obj.put("genpo", "yes");
                    obj.put("genpocheck", true);

                    if (isForCompAvailablity) {
                        obj.put("genpo", "yes");
                        obj.put("genpocheck", false);
                    }
                } else {
                    obj.put("genpo", "no");
                    obj.put("genpocheck", false);
                }
                              
                obj.put("inventoryquantiy", passembly.getQuantity());
                obj.put("percentage", passembly.getPercentage());
                obj.put("availablerecylequantity", availableRecyclableQuantity);
                obj.put("subbomid", passembly.getSubbom() != null ? passembly.getSubbom().getID() : "");
                obj.put("subbomcode", passembly.getSubbom() != null ? passembly.getSubbom().getBomCode() : "");
                Double availableQty = (Double) (row[3] == null ? 0.0 : row[3]);  //iis the actual available wuantity for product
                KwlReturnObject result2 = accProductObj.getAssemblyLockQuantityForBuild(mainProductid, subProductid);
                Double assmblyLockQuantity = (Double) (result2.getEntityList().get(0) == null ? 0.0 : result2.getEntityList().get(0));  //it is the lock quantity in assembly product locked in SO

                KwlReturnObject result1 = accProductObj.getLockQuantity(subProductid); //for geting a locked quantity of inventory product used in salesorder
                Double SoLockQuantity = (Double) (result1.getEntityList().get(0) == null ? 0.0 : result1.getEntityList().get(0));   //it is the lock quantity of product locked in SO
                Double lockquantity = assmblyLockQuantity + SoLockQuantity;   //total lock quantity
                obj.put("lockquantity", lockquantity);
                obj.put("onhand", availableQty - lockquantity);   //its actual quantity available for user
                obj.put("wastageInventoryQuantity", passembly.getWastageInventoryQuantity());
                obj.put("wastageQuantityType", passembly.getWastageQuantityType());
                obj.put("wastageQuantity", passembly.getWastageQuantity());
                obj.put("level", levelCount);
                obj.put("minpercentquantity", minpercentquantity);
                obj.put("parentproductid", passembly.getProduct() != null ? passembly.getProduct().getID() : "");
                
                //ERP-37246 : Get Available Quntity of BOM Product at its Default Location & Warehouse (OR at provided Location and Warehouse)
                double availQtyAtDefaultLocWH = 0;
                if (passembly.getSubproducts() != null && (passembly.getSubproducts().getLocation() != null && passembly.getSubproducts().getWarehouse() != null)) {
                    HashMap<String, Object> locwhMap = new HashMap<>();
                    locwhMap.put("location", passembly.getSubproducts() != null ? passembly.getSubproducts().getLocation().getId() : "");
                    locwhMap.put("warehouse", passembly.getSubproducts() != null ? passembly.getSubproducts().getWarehouse().getId() : "");
                    locwhMap.put("company", passembly.getSubproducts() != null ? passembly.getSubproducts().getCompany().getCompanyID() : "");
                    locwhMap.put("product", passembly.getSubproducts() != null ? passembly.getSubproducts().getID() : "");
                    KwlReturnObject locwhresult = accProductObj.getLocationWarehouseWiseAvailableQuantity(locwhMap);
                    if (locwhresult.getEntityList() != null && !locwhresult.getEntityList().isEmpty()) {
                        availQtyAtDefaultLocWH = (Double) locwhresult.getEntityList().get(0);
                    }
                }
                obj.put("availQtyofdefaultlocwarehouse",availQtyAtDefaultLocWH);
                
                jArr.put(obj);
                if (isForCompAvailablity) {
                    if (isAssemblyProduct) {
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put(Constants.productid, productid);
                        requestParams.put("bomdetailid", subBOMid);
                        requestParams.put(Constants.currencyKey,paramJobj.optString(Constants.globalCurrencyKey,"1"));
                        KwlReturnObject result = accProductObj.getAssemblyItems(requestParams);
                        int tmpLevel = levelCount + 1;
                        paramJobj.put("mrproductquantity", reqQuantity);
                        JSONObject tempJobj = getAssemblyItemsJson(paramJobj, result.getEntityList(), tmpLevel);
                        JSONArray tempJarr = tempJobj.getJSONArray("data");
                        for (int count = 0; count < tempJarr.length(); count++) {
                            jArr.put(tempJarr.get(count));
                        }
                    }
                }
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getAssemblyItemsJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    
    @Override
    public List isProductUsedintransction(String productid, String companyid, HttpServletRequest request,boolean isProductAndServices) throws SessionExpiredException, AccountingException, ServiceException {
        ArrayList listObj = new ArrayList();
        String modulesProductUsedIn = "";
        boolean isusedinTransaction = false;

        boolean unBuild = Boolean.parseBoolean(request.getParameter("unBuild"));
        try {

            KwlReturnObject result = accProductObj.getPO_Product(productid, companyid); //Is used in Purchase Order ?
            BigInteger bigInteger1 = (BigInteger) result.getEntityList().get(0);
            int count1 = 0;
            if (bigInteger1.intValue() > 0) {
                count1 = bigInteger1.intValue();
            }
            modulesProductUsedIn += count1>0? "Purchase Order, " : "";
            if(isProductAndServices && count1>0){
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getSO_Product(productid, companyid);  // Is used in Sales Order ?
            BigInteger bigInteger2 = (BigInteger) result.getEntityList().get(0);
            int count2 = 0;
            if (bigInteger2.intValue() > 0) {
                count2 = bigInteger2.intValue();
            }
            modulesProductUsedIn += count2>0? "Sales Order, " : "";
            if (isProductAndServices && count2 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getGoodsReceipt_Product(productid, companyid); // Is Used in Vendor Invoice?
            BigInteger bigInteger3 = (BigInteger) result.getEntityList().get(0);
            int count3 = 0;
            if (bigInteger3.intValue() > 0) {
                count3 = bigInteger3.intValue();
            }
            modulesProductUsedIn += count3>0? "Vendor Invoice, " : "";
            if (isProductAndServices && count3 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getInvoice_Product(productid, companyid);  // Is used in Customer Invoice?
            BigInteger bigInteger4= (BigInteger) result.getEntityList().get(0);
            int count4 = 0;
            if (bigInteger4.intValue() > 0) {
                count4 = bigInteger4.intValue();
            }
            modulesProductUsedIn += count4>0? "Customer Invoice, " : "";
            if (isProductAndServices && count4 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.checkSubProductforAssembly(productid); //Is used in Assembly Product? 
            BigInteger bigInteger5 = (BigInteger) result.getEntityList().get(0);
            int count5 = 0;
            if (bigInteger5.intValue() > 0) {
                count5 = bigInteger5.intValue();
            }
            modulesProductUsedIn += count5>0? "Assembly Product, " : "";
            if (isProductAndServices && count5 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getGR_Product(productid, companyid); // Is used in Goods Receipt
            BigInteger bigInteger7 = (BigInteger) result.getEntityList().get(0);
            int count7 = 0;
            if (bigInteger7.intValue() > 0) {
                count7 = bigInteger7.intValue();
            }
            modulesProductUsedIn += count7>0? "Goods Receipt, " : "";
            if (isProductAndServices && count7 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getDO_Product(productid, companyid); //Is used in Delivery Order?
            BigInteger bigInteger8 = (BigInteger) result.getEntityList().get(0);
            int count8 = 0;
            if (bigInteger8.intValue() > 0) {
                count8 = bigInteger8.intValue();
            }
            modulesProductUsedIn += count8>0? "Delivery Order, " : "";
            if (isProductAndServices && count8 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getPR_Product(productid, companyid); //Is used in Purchase Requisition?
            BigInteger bigInteger9 = (BigInteger) result.getEntityList().get(0);
            int count9 = 0;
            if (bigInteger9.intValue() > 0) {
                count9 = bigInteger9.intValue();
            }
            modulesProductUsedIn += count9>0? "Purchase Requisition, " : "";
            if (isProductAndServices && count9 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getVQ_Product(productid, companyid); // Is used in Vendor Quotation
            BigInteger bigInteger10 = (BigInteger) result.getEntityList().get(0);
            int count10 = 0;
            if (bigInteger10.intValue() > 0) {
                count10 = bigInteger10.intValue();
            }
            modulesProductUsedIn += count10>0? "Vendor Quotation, " : "";
            if (isProductAndServices && count10 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getCQ_Product(productid, companyid); //Is used in Customer Quotation
            BigInteger bigInteger11 = (BigInteger) result.getEntityList().get(0);
            int count11 = 0;
            if (bigInteger11.intValue() > 0) {
                count11= bigInteger11.intValue();
            }
            modulesProductUsedIn += count11>0? "Customer Quotation, " : "";
            if (isProductAndServices && count11 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getPurchaseReturn_Product(productid, companyid); //Is used in Purchase Return?
            BigInteger bigInteger12 = (BigInteger) result.getEntityList().get(0);
            int count12 = 0;
            if (bigInteger12.intValue() > 0) {
                count12 = bigInteger12.intValue();
            }
            modulesProductUsedIn += count12>0? "Purchase Return, " : "";
            if (isProductAndServices && count12 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getSalesReturn_Product(productid, companyid); //Is used in Sales Return?
            BigInteger bigInteger13 = (BigInteger) result.getEntityList().get(0);
            int count13 = 0;
            if (bigInteger13.intValue() > 0) {
                count13 = bigInteger13.intValue();
            }
            modulesProductUsedIn += count13>0? "Sales Return, " : "";
            if (isProductAndServices && count13 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getInventoryTransaction_Product(productid, companyid); //Is used in Inventory side transaction?
            BigInteger bigInteger14 = (BigInteger) result.getEntityList().get(0);
            int count14 = 0;
            if (bigInteger14.intValue() > 0) {
                count14 = bigInteger14.intValue();
            }
            modulesProductUsedIn += count14>0? "Inventory, " : "";
            if (isProductAndServices && count14 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            

            if (count1 > 0 || count2 > 0 || count3 > 0 || count4 > 0 || count7 > 0 || count8 > 0 || count9 > 0 || count10 > 0 || count11 > 0 || count12 > 0 || count13 > 0 || count14 > 0) {
                isusedinTransaction = true;
            }

            if (count5 > 0) {
                isusedinTransaction = true;
            }
         

            //Delete product from All Assemblies
            KwlReturnObject kwlReturnObject_SPA = accProductObj.selectSubProductFromBuildAssembly(productid);
            BigInteger bigInteger15 = (BigInteger) kwlReturnObject_SPA.getEntityList().get(0);
            int count15 = 0;
            if (bigInteger15.intValue() > 0) {
                count15 = bigInteger15.intValue();
            }
            result = accProductObj.checkIfParentProduct(productid);
            BigInteger bigInteger6 = (BigInteger) result.getEntityList().get(0);
            int count6 = 0;
            if (bigInteger6.intValue() > 0) {
                count6 = bigInteger6.intValue();
            }
            if ((count6 > 0 || count15 > 0) && !unBuild) {
                isusedinTransaction = true;
            }

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("AccProductModuleServiceImpl.isProductUsedintransction() -: " + ex.getMessage(), ex);
        }
        listObj.add(isusedinTransaction);
        listObj.add(modulesProductUsedIn);
        return listObj;                     //returned the list object with both values
    }
    @Override
    public List isProductUsedintransction(String productid, String companyid, HashMap<String, Object> reqHashMap) throws SessionExpiredException, AccountingException, ServiceException {
        ArrayList listObj = new ArrayList();
        String modulesProductUsedIn = "";
        boolean isusedinTransaction = false;
        String prodNames = "";
        Product prd = null;
        boolean unBuild = false;
        if (reqHashMap.containsKey("unBuild") && reqHashMap.get("unBuild") != null) {
            unBuild = (Boolean) reqHashMap.get("unBuild");
        }
        try {

            KwlReturnObject result = accProductObj.getPO_Product(productid, companyid); //Is used in Purchase Order ?
            BigInteger bigInteger1 = (BigInteger) result.getEntityList().get(0);
            int count1 = 0;
            if (bigInteger1.intValue() > 0) {
                count1 = bigInteger1.intValue();
            }
            modulesProductUsedIn += count1 > 0 ? "Purchase Order, " : "";

            result = accProductObj.getSO_Product(productid, companyid);  // Is used in Sales Order ?
            BigInteger bigInteger2 = (BigInteger) result.getEntityList().get(0);
            int count2 = 0;
            if (bigInteger2.intValue() > 0) {
                count2 = bigInteger2.intValue();
            }
            modulesProductUsedIn += count2 > 0 ? "Sales Order, " : "";

            result = accProductObj.getGoodsReceipt_Product(productid, companyid); // Is Used in Vendor Invoice?
            BigInteger bigInteger3 = (BigInteger) result.getEntityList().get(0);
            int count3 = 0;
            if (bigInteger3.intValue() > 0) {
                count3 = bigInteger3.intValue();
            }
            modulesProductUsedIn += count3 > 0 ? "Vendor Invoice, " : "";

            result = accProductObj.getInvoice_Product(productid, companyid);  // Is used in Customer Invoice?
            BigInteger bigInteger4 = (BigInteger) result.getEntityList().get(0);
            int count4 = 0;
            if (bigInteger4.intValue() > 0) {
                count4 = bigInteger4.intValue();
            }
            modulesProductUsedIn += count4 > 0 ? "Customer Invoice, " : "";

            result = accProductObj.checkSubProductforAssembly(productid); //Is used in Assembly Product? 
            BigInteger bigInteger5 = (BigInteger) result.getEntityList().get(0);
            int count5 = 0;
            if (bigInteger5.intValue() > 0) {
                count5 = bigInteger5.intValue();
            }
            modulesProductUsedIn += count5 > 0 ? "Assembly Product, " : "";

            result = accProductObj.getGR_Product(productid, companyid); // Is used in Goods Receipt
            BigInteger bigInteger7 = (BigInteger) result.getEntityList().get(0);
            int count7 = 0;
            if (bigInteger7.intValue() > 0) {
                count7 = bigInteger7.intValue();
            }
            modulesProductUsedIn += count7 > 0 ? "Goods Receipt, " : "";

            result = accProductObj.getDO_Product(productid, companyid); //Is used in Delivery Order?
            BigInteger bigInteger8 = (BigInteger) result.getEntityList().get(0);
            int count8 = 0;
            if (bigInteger8.intValue() > 0) {
                count8 = bigInteger8.intValue();
            }
            modulesProductUsedIn += count8 > 0 ? "Delivery Order, " : "";

            result = accProductObj.getPR_Product(productid, companyid); //Is used in Purchase Requisition?
            BigInteger bigInteger9 = (BigInteger) result.getEntityList().get(0);
            int count9 = 0;
            if (bigInteger9.intValue() > 0) {
                count9 = bigInteger9.intValue();
            }
            modulesProductUsedIn += count9 > 0 ? "Purchase Requisition, " : "";

            result = accProductObj.getVQ_Product(productid, companyid); // Is used in Vendor Quotation
            BigInteger bigInteger10 = (BigInteger) result.getEntityList().get(0);
            int count10 = 0;
            if (bigInteger10.intValue() > 0) {
                count10 = bigInteger10.intValue();
            }
            modulesProductUsedIn += count10 > 0 ? "Vendor Quotation, " : "";

            result = accProductObj.getCQ_Product(productid, companyid); //Is used in Customer Quotation
            BigInteger bigInteger11 = (BigInteger) result.getEntityList().get(0);
            int count11 = 0;
            if (bigInteger11.intValue() > 0) {
                count11 = bigInteger11.intValue();
            }
            modulesProductUsedIn += count11 > 0 ? "Customer Quotation, " : "";

            result = accProductObj.getPurchaseReturn_Product(productid, companyid); //Is used in Purchase Return?
            BigInteger bigInteger12 = (BigInteger) result.getEntityList().get(0);
            int count12 = 0;
            if (bigInteger12.intValue() > 0) {
                count12 = bigInteger12.intValue();
            }
            modulesProductUsedIn += count12 > 0 ? "Purchase Return, " : "";

            result = accProductObj.getSalesReturn_Product(productid, companyid); //Is used in Sales Return?
            BigInteger bigInteger13 = (BigInteger) result.getEntityList().get(0);
            int count13 = 0;
            if (bigInteger13.intValue() > 0) {
                count13 = bigInteger13.intValue();
            }
            modulesProductUsedIn += count13 > 0 ? "Sales Return, " : "";

            if (count1 > 0 || count2 > 0 || count3 > 0 || count4 > 0 || count7 > 0 || count8 > 0 || count9 > 0 || count10 > 0 || count11 > 0 || count12 > 0 || count13 > 0) {
                isusedinTransaction = true;
            }

            if (count5 > 0) {
                isusedinTransaction = true;
            }

            KwlReturnObject rtObj = accProductObj.getProductByID(productid, companyid);
            prd = ((Product) rtObj.getEntityList().get(0));
            prodNames += ", " + prd.getName();

            MasterItem prodMasterItemObj = accProductObj.getProductsMasterItem(companyid, productid);
            //Delete product from All Assemblies

            KwlReturnObject kwlReturnObject_SPA = accProductObj.selectSubProductFromBuildAssembly(productid);
            BigInteger bigInteger15 = (BigInteger) kwlReturnObject_SPA.getEntityList().get(0);
            int count15 = 0;
            if (bigInteger15.intValue() > 0) {
                count15 = bigInteger15.intValue();
            }
            KwlReturnObject kwlReturnObject_I = accProductObj.selectInventoryByProduct(productid, companyid);
            int inv = kwlReturnObject_I.getRecordTotalCount();
            if (inv > 0) {
                isusedinTransaction = true;
            }
            result = accProductObj.checkIfParentProduct(productid);
            BigInteger bigInteger6 = (BigInteger) result.getEntityList().get(0);
            int count6 = 0;
            if (bigInteger6.intValue() > 0) {
                count6 = bigInteger6.intValue();
            }
            if ((count6 > 0 || count15 > 0) && !unBuild) {
                isusedinTransaction = true;
            }

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("AccProductModuleServiceImpl.isProductUsedintransction() -: " + ex.getMessage(), ex);
        }
        listObj.add(isusedinTransaction);
        listObj.add(modulesProductUsedIn);
        return listObj;                     //returned the list object with both values
    }
    
 @Override   
    public boolean isProductUsedinBatchSerialtransction(String productid, String companyid) throws ServiceException {
        Long count = 0l, count1 = 0l;
        boolean isUsedInBatchSerial = false;
        KwlReturnObject result = accProductObj.getBatchesfor_Product(productid, companyid); // Is used in Goods Receipt
        List list = result.getEntityList();
        if (!list.isEmpty() && list.size() > 0 && !list.contains(null)) {
            count = (Long) list.get(0);
        }
        if (count > 0) {
            isUsedInBatchSerial = true;
            return isUsedInBatchSerial;
        }
        KwlReturnObject result1 = accProductObj.getSerialsfor_Product(productid, companyid); // Is used in Goods Receipt
        List list1 = result1.getEntityList();
        if (!list1.isEmpty() && list1.size() > 0 && !list1.contains(null)) {
            count1 = (Long) list1.get(0);
        }

        if ((count > 0 || count1 > 0)) {
            isUsedInBatchSerial = true;
        }
        return isUsedInBatchSerial;
    }
    
    @Override
    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
//            String s = (listArray[i]==null)?"":listArray[i].toString();
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }
    
    @Override
    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accProductObj.getCurrencies();
        List currencyList = returnObject.getEntityList();
        if (currencyList != null && !currencyList.isEmpty()) {
            Iterator iterator = currencyList.iterator();
            while (iterator.hasNext()) {
                KWLCurrency currency = (KWLCurrency) iterator.next();
               if(isCurrencyCode){
                currencyMap.put(currency.getCurrencyCode(), currency.getCurrencyID());
               }else{
                  currencyMap.put(currency.getName(), currency.getCurrencyID()); 
               }
            }
        }
        return currencyMap;
    }
 
 @Override   
    public Map<Integer, Frequency> getCCFrequencyMap() throws ServiceException {
        Map<Integer, Frequency> fMap = new HashMap<>();
        List<Frequency> frequencyList = accProductObj.getFrequencies();
        for (Frequency frequency : frequencyList) {
            fMap.put(frequency.getId(), frequency);
        }
        return fMap;
    }
    
    @Override
    public Producttype getProductTypeByName(String productTypeName) throws AccountingException {
        Producttype producttype = null;
        try {
            if (!StringUtil.isNullOrEmpty(productTypeName)) {
                KwlReturnObject retObj = accProductObj.getProductTypeByName(productTypeName);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    producttype = (Producttype) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Type");
        }
        return producttype;
    }
    
    /*
     * Method to get assembly products with batch serial details.
     * Also we can perform quick search on serial no of main and sub products.
     */
    @Override
    public JSONObject getAssembyProductDetails(JSONObject paramJobj) throws ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject resultObj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            Map<String, JSONObject> serialDetails = null;
            boolean exportfalg = paramJobj.optBoolean("exportfalg",false);
            String appendString = exportfalg ? " \n " : " <br> ";
//            paramJobj.put("getProductSerialDetails",true);
//            serialDetails = getBatchSerialDetailsOfAssembyProduct(paramJobj);
//            paramJobj.remove("getProductSerialDetails");
            
            int count = 0;
            paramJobj.put("getSummaryOfAssemblyProduct",true);
            JSONObject detailsJobj = null;
            KwlReturnObject retObj = accProductObj.getAssembyProductDetails(paramJobj);
            if (retObj != null && !retObj.getEntityList().isEmpty()) {
                List<Object[]> list = retObj.getEntityList();
                count = retObj.getRecordTotalCount();
                for (Object[] row : list) {
                    String productBuildId = (row[0] !=null && row[0] instanceof String) ? (String)row[0] : "";
                    String warehouse = (row[14] != null && row[14] instanceof String) ? (String) row[14] : "";
                    String location = (row[15] != null && row[15] instanceof String) ? (String) row[15] : "";
                    String batch = (row[16] != null && row[16] instanceof String) ? (String) row[16] : "";
                    JSONObject obj = new JSONObject();
//                    if (serialDetails != null && serialDetails.containsKey(productBuildId)) {
//                        detailsJobj = serialDetails.get(productBuildId);
//                        obj.put("warehouse", detailsJobj.optString("warehouse"));
//                        obj.put("location", detailsJobj.optString("location"));
//                        obj.put("batch", detailsJobj.optString("batch"));
//                        obj.put("serial", detailsJobj.optString("serial"));
//                    }
                    obj.put("productid", productBuildId);
                    obj.put("billid", productBuildId);    //ERM-26 to use generic export functionality for print button in Document Designer
                    obj.put("mainproductid", (row[2] != null && row[2] instanceof String) ? (String) row[2] : "");
                    obj.put("productname", (row[3] != null && row[3] instanceof String) ? (String) row[3] : "");
                    obj.put("quantity", (row[4] != null && row[4] instanceof Double) ? (Double) row[4] : "");
                    obj.put("productrefno", (row[5] != null && row[5] instanceof String) ? (String) row[5] : "");
                    obj.put("sequenceformatid", (row[6] != null && row[6] instanceof String) ? (String) row[6] : "");
                    obj.put("memo", (row[7] != null && row[7] instanceof String) ? (String) row[7] : "");
                    obj.put("entrydate", (row[1] != null && row[1] instanceof Date) ? df.format((Date) row[1]) : "");
                    obj.put("description", (row[8] != null && row[8] instanceof String) ? (String) row[8] : "");
                    obj.put("buildProdId", (row[9] != null && row[9] instanceof String) ? (String) row[9] : "");
                    obj.put("journalentryid", (row[10] != null && row[10] instanceof String) ? (String) row[10] : "");
                    obj.put("entryno", (row[11] != null && row[11] instanceof String) ? (String) row[11] : "");
                    obj.put("bomCode", (row[12] != null && row[12] instanceof String) ? (String) row[12] : "");
                    obj.put("bomdetailid", (row[13] != null && row[13] instanceof String) ? (String) row[13] : "");
                    obj.put("warehouse", warehouse);
                    obj.put("location", location);
                    obj.put("batch", batch);
                    if (row[17] != null && row[17] instanceof String) {
                        String[] serialsArr = ((String) row[17] ).split(",");
                        String serialStr = "";
                        StringBuilder sb = new StringBuilder();
                        for(String serial:serialsArr){
                            serialStr = serial +" [Warehouse : " +warehouse+", Location : " +location+", Batch : " + batch+"]";
                            if (!sb.toString().contains(serial)) {
                                sb.append(serialStr).append(appendString);
                            }
                        }
                        obj.put("serial", sb.toString());
                    }
                    jArr.put(obj);
                }
            }
            resultObj.put(Constants.RES_data, jArr);
            resultObj.put(Constants.RES_TOTALCOUNT, count);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
        }
        return resultObj;
    }
    
    @Override
    public Map getBatchSerialDetailsOfAssembyProduct(JSONObject paramJobj) throws ServiceException {
        Map<String, JSONObject> storageDetail = new HashMap<>();
        try {
            String productId = paramJobj.optString(Constants.productid);
            String companyId = paramJobj.optString(Constants.companyKey);
            boolean exportfalg = paramJobj.optBoolean("exportfalg",false);
            String appendString = exportfalg ? " \n " : " <br> ";
            JSONObject detailsJobj = null;
            String locationDetails="",batchDetails="",serailDetails="";
            if (!StringUtil.isNullOrEmpty(productId) || !StringUtil.isNullOrEmpty(companyId)) {
//                String details = "";
                KwlReturnObject retObj = accProductObj.getAssembyProductDetails(paramJobj);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    List<Object[]> list = retObj.getEntityList();
                    for (Object[] row : list) {
//                        StringBuilder detail = new StringBuilder();
                        String detailid = (row[0] != null && row[0] instanceof String) ? (String) row[0] : "";
                        String warehouse = (row[3] != null && row[3] instanceof String) ? (String) row[3] : "";
                        String location = (row[5] != null && row[5] instanceof String) ? (String) row[5] : "";
                        String batch = (row[6] != null && row[6] instanceof String) ? (String) row[6] : "";
                        String serial = (row[7] != null && row[7] instanceof String) ? (String) row[7] : "";

//                        if (!StringUtil.isNullOrEmpty(warehouse)) {
//                            detail.append("Warehouse : ").append(warehouse);
//                        }
//                        if (!StringUtil.isNullOrEmpty(location)) {
//                            detail.append(", Location : ").append(location);
//                        }
//                        if (!StringUtil.isNullOrEmpty(batch)) {
//                            detail.append(", Batch : ").append(batch);
//                        }
//
//                        StringBuilder serialSb = new StringBuilder();
//
//                        if (!StringUtil.isNullOrEmpty(serialName)) {
//                            serialSb.append(serialName).append(" [").append(detail.toString()).append("]");
//                        }
//                        details = StringUtil.isNullOrEmpty(serialSb.toString()) ? detail.toString() : serialSb.toString();
                        if (!StringUtil.isNullOrEmpty(location)) {
                            locationDetails = location + " [ Warehouse : " + warehouse + "]";
                        }
                        if (!StringUtil.isNullOrEmpty(batch)) {
                            batchDetails = (batch + " [ Warehouse : " + warehouse + ", Location : " + location + "]");
                        }
                        if (!StringUtil.isNullOrEmpty(serial)) {
                            serailDetails = serial + " [ Warehouse : " + warehouse + ", Location : " + location + ", Batch : " + batch + "]";
                        }
                        
                        if (storageDetail.containsKey(detailid)) {
                            detailsJobj = storageDetail.get(detailid);
                            
                            if (!StringUtil.isNullOrEmpty(warehouse) && (!detailsJobj.optString("warehouse").equals(warehouse))) {
                                detailsJobj.put("warehouse", detailsJobj.has("warehouse") ? detailsJobj.getString("warehouse") + appendString + warehouse : warehouse);
                            }
                            if (!StringUtil.isNullOrEmpty(locationDetails) && (!detailsJobj.optString("location").equals(locationDetails))) {
                                detailsJobj.put("location", detailsJobj.has("location") ? detailsJobj.getString("location") + appendString + locationDetails : locationDetails);
                            }
                            if (!StringUtil.isNullOrEmpty(batchDetails) && (!detailsJobj.optString("batch").equals(batchDetails))) {
                                detailsJobj.put("batch", detailsJobj.has("batch") ? detailsJobj.getString("batch") + appendString + batchDetails : batchDetails);
                            }
                            if (!StringUtil.isNullOrEmpty(serailDetails) && (!detailsJobj.optString("serial").equals(serailDetails))) {
                                detailsJobj.put("serial", detailsJobj.has("serial") ? detailsJobj.getString("serial") + appendString + serailDetails : serailDetails);
                            }
                            
                            storageDetail.put(detailid, detailsJobj);
                        } else {
                            detailsJobj = new JSONObject();
                            if (!StringUtil.isNullOrEmpty(warehouse)) {
                                detailsJobj.put("warehouse", warehouse);
                            }
                            if (!StringUtil.isNullOrEmpty(locationDetails)) {
                                detailsJobj.put("location", locationDetails);
                            }
                            if (!StringUtil.isNullOrEmpty(batchDetails)) {
                                detailsJobj.put("batch", batchDetails);
                            }
                            if (!StringUtil.isNullOrEmpty(serailDetails)) {
                                detailsJobj.put("serial", serailDetails);
                            }
                            storageDetail.put(detailid, detailsJobj);
                        }
                    }
//                    storageDetail.put("resultList", list);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return storageDetail;
    }
    
    
    @Override
    public UnitOfMeasure getUOMByName(String productUOMName, String companyID) throws AccountingException {
        UnitOfMeasure uom = null;
        try {
            if (!StringUtil.isNullOrEmpty(productUOMName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getUOMByName(productUOMName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    uom = (UnitOfMeasure) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Unit of Measure");
        }
        return uom;
    }
    
    @Override
    public Product getProductByProductID(String productID, String companyID) throws AccountingException {
        Product product = null;
        try {
            if (!StringUtil.isNullOrEmpty(productID) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getProductByProductID(productID, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    product = (Product) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product");
        }
        return product;
    }
    
    @Override
    public Account getAccountByName(String accountName, String companyID) throws AccountingException {
        Account account = null;
        try {
            if (!StringUtil.isNullOrEmpty(accountName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getAccountByName(accountName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    Iterator itr = retObj.getEntityList().iterator();
                    if (itr != null && itr.hasNext()) {
                        String accountID = (String) itr.next();
                        KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountID);
                        account = (Account) custumObjresult.getEntityList().get(0);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Account");
        }
        return account;
    }
    
    @Override
    public Vendor getVendorByName(String vendorName, String companyID) throws AccountingException {
        Vendor vendor = null;
        try {
            if (!StringUtil.isNullOrEmpty(vendorName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getVendorByName(vendorName, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    vendor = (Vendor) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Vendor");
        }
        return vendor;
    }

    @Override
    public InventoryLocation getInventoryLocationByName(String inventoryLocation, String companyID) throws AccountingException {
        InventoryLocation invLoc = null;
        try {
            if (!StringUtil.isNullOrEmpty(inventoryLocation) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getInventoryLocationByName(inventoryLocation, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    invLoc = (InventoryLocation) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Inventory Location");
        }
        return invLoc;
    }
    
    /**
     * Description:This method is used to get the Location Object by passing location id.
     * @param String locationid, String companyid
     * @return Location class object
     * @throws ServiceException
     */
    @Override
    public Location getLocationByID(String locID, String companyID) throws AccountingException {
        Location loc = null;
        try {
            if (!StringUtil.isNullOrEmpty(locID) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getLocationByID(locID, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    loc = (Location) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Location");
        }
        return loc;
    }

    @Override
    public InventoryWarehouse getInventoryWarehouseByName(String inventoryWarehouse, String companyID) throws AccountingException {
        InventoryWarehouse invWHouse = null;
        try {
            if (!StringUtil.isNullOrEmpty(inventoryWarehouse) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getInventoryWarehouseByName(inventoryWarehouse, companyID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    invWHouse = (InventoryWarehouse) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Inventory Warehouse");
        }
        return invWHouse;
    }
    
    @Override
    public String getCurrencyId(String currencyName, HashMap currencyMap) {
        String currencyId = "";
        if (currencyMap != null && currencyMap.containsKey(currencyName)) {
            currencyId = currencyMap.get(currencyName).toString();
        }
        return currencyId;
    }
    
    @Override
    public void maintainCustomFieldHistoryForProduct(HttpServletRequest request, HashMap<String, Object> customrequestParams) {
        try {
            JSONArray jcustomarray = (JSONArray) customrequestParams.get("customarray");
            String loginId = sessionHandlerImpl.getUserid(request);
            Date applyDate = authHandler.getDateOnlyFormatter(request).parse(authHandler.getDateOnlyFormatter(request).format(new Date()));
            String countryid = sessionHandlerImpl.getCountryId(request);
            Date gstapplieddate=null;
            /**
             * Get request params in JSON 
             */
            JSONObject paramsJObj = StringUtil.convertRequestToJsonObject(request);
            int moduleId = 30;
            if (countryid.equalsIgnoreCase("" + Constants.indian_country_id) && !StringUtil.isNullOrEmpty(request.getParameter("gstapplieddate"))) {
//                String gstapplieddate = request.getParameter("gstapplieddate");
                gstapplieddate = authHandler.getDateOnlyFormat().parse(request.getParameter("gstapplieddate"));
                boolean isFixedAsset = false;
                if (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) {
                    isFixedAsset = Boolean.parseBoolean(request.getParameter("isFixedAsset"));
                    if (isFixedAsset) {
                        moduleId = Constants.Acc_FixedAssets_AssetsGroups_ModuleId;
                    }
                }
            }
  
            for (int i = 0; i < jcustomarray.length(); i++) {
                HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                String productId = "";
                String fieldId = "";
                
                String value = "";
                JSONObject jobj = jcustomarray.getJSONObject(i);
                if (jobj != null) {
                    productId = (String) customrequestParams.get("productId");
                    fieldId = jobj.getString("fieldid");
                    int fieldType = jobj.getInt("xtype");
                    value = jobj.has("fieldDataVal")?jobj.getString("fieldDataVal"):"";
                    requestParams.put("productId", productId);
                    requestParams.put("fieldId", fieldId);
                    requestParams.put("value", value);
                    requestParams.put("applyDate", applyDate);
                    requestParams.put("moduleId", moduleId);
                    requestParams.put("creationDate", applyDate);
                    requestParams.put("loginId", loginId);
                    if ((fieldType == 1 || fieldType == 2) && !StringUtil.isNullOrEmpty(value)) {
                        KwlReturnObject fieldReturnObject = accProductObj.getCustomFieldHistoryForProduct(requestParams);
                    List list = fieldReturnObject.getEntityList();
                        if (list.size() > 0) {
                            accProductObj.deleteCustomFieldHistoryForProduct(requestParams);
                        }
                        KwlReturnObject returnObject = accProductObj.maintainCustomFieldHistoryForProduct(requestParams);
                    } else if (countryid.equalsIgnoreCase("" + Constants.indian_country_id) && fieldType == 4 && jobj.optString("fieldname").equalsIgnoreCase("Custom_" + Constants.GSTProdCategory)) {
                        /**
                         * Save Product tax class history.
                         */
                        requestParams.put("creationDate", gstapplieddate);
                        requestParams.put("applyDate", gstapplieddate);
                        boolean isgstdetailsupdated = !StringUtil.isNullOrEmpty("isgstdetailsupdated") ? Boolean.parseBoolean(request.getParameter("isgstdetailsupdated")) : false;
                        boolean isEdit = !StringUtil.isNullOrEmpty(request.getParameter("productid")) ? true : false;
                        if (!isEdit || isgstdetailsupdated) {
                            String fieldname = jobj.optString("fieldname");
                            String coldata = jobj.optString(fieldname);
                            value = jobj.optString(coldata);
                            requestParams.put("value", value);
                            List histList = accProductObj.getGstProductHistory(requestParams);
                            if (!histList.isEmpty() && histList.get(0) != null) {
                                requestParams.put("id", (String) histList.get(0));
                            }
                            /**
                             * Save audit trail entry for Product Tax Class History
                             */
                            paramsJObj.put(Constants.productid,productId);
                            paramsJObj.put("ProductTaxClassValue",value);
                            saveProductGSTHistoryAuditTrail(paramsJObj);
                            /**
                             * Save history for Product tax Class.
                             */
                            KwlReturnObject returnObject = accProductObj.maintainCustomFieldHistoryForProduct(requestParams);
                        }
                    }
                }

            }
        } catch (Exception e) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
    }
 
    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            java.io.FileOutputStream failurefileOut = new java.io.FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            failurefileOut.write(failedRecords.toString().getBytes());
            failurefileOut.flush();
            failurefileOut.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }
    
 @Override   
    public String getCustomerIDByCode(String customerCode, String companyID) throws AccountingException {
        String customerID = "";
        try {
            if (!StringUtil.isNullOrEmpty(customerCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getCustomerIDByCode(customerCode, companyID);
                List list = retObj.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    customerID = (String) itr.next();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Customer");
        }
        return customerID;
    }
 
   @Override     
    public String getVendorIDByCode(String vendorCode, String companyID) throws AccountingException {
        String vendorID = "";
        try {
            if (!StringUtil.isNullOrEmpty(vendorCode) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getVendorIDByCode(vendorCode, companyID);
                List list = retObj.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    vendorID = (String) itr.next();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Vendor");
        }
        return vendorID;
    }

 @Override   
    public Product getProductByProductName(String companyid, String productTypeID) throws AccountingException {
        Product product = null;
        try {
            if (!StringUtil.isNullOrEmpty(productTypeID)) {
                KwlReturnObject retObj = accProductObj.getProductByProductName(companyid, productTypeID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    product = (Product) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Details");
        }
        return product;
    }  
 
 @Override    
    public String getProductCategoryIDByName(String productCategoryName, String companyID) throws AccountingException {
        String productCategoryID = "";
        try {
            if (!StringUtil.isNullOrEmpty(productCategoryName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getProductCategoryIDByName(productCategoryName, companyID);
                List list = retObj.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    productCategoryID = (String) itr.next();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Category");
        }
        return productCategoryID;
    }
 
    /*
     * Moved code to save product category from accProductController.java to
     * AccProductModuleServiceImpl.java
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class, ServiceException.class})
    public JSONObject saveProductCategoryMapping(JSONObject paramJobj) throws ServiceException,SessionExpiredException{
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isAccountingExe = false;
        boolean isSameIndCode = true;
        String auditMsg = "", auditID = "", productName = "", productId = "", codeVal = "", msg = "";

        //Create transaction
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("CF_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//
//        TransactionStatus status = txnManager.getTransaction(def);
        try {
//            String companyid = sessionHandlerImpl.getCompanyid(request);
            String[] productList = paramJobj.optString("productList","").split(",");
            String[] productCategory = paramJobj.optString("productCategory","").split(",");
            String companyid = paramJobj.optString(Constants.companyKey);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            String industryCode = "";
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("industryCode","")) && !paramJobj.optString("industryCode","").equals("-1")) {
                industryCode = paramJobj.optString("industryCode");
                KwlReturnObject indCode = accProductObj.getObject(MasterItem.class.getName(), industryCode);
                MasterItem code = (MasterItem) indCode.getEntityList().get(0);
                if (code != null) {
                    codeVal = code.getValue();
                    for (int i = 0; i < productCategory.length; i++) {
                        KwlReturnObject category = accProductObj.getObject(MasterItem.class.getName(), productCategory[i]);
                        MasterItem categoryIndCode = (MasterItem) category.getEntityList().get(0);
                        if (!productCategory[i].equalsIgnoreCase("None")) {
                            if (categoryIndCode.getIndustryCodeId() != null && !categoryIndCode.getIndustryCodeId().equals(industryCode) && !categoryIndCode.getIndustryCodeId().equals("-1")) {
                                isSameIndCode = false;
                            }
                        }
                    }
                }
                if (!isSameIndCode) {
                    isAccountingExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.productcate.code", null, StringUtil.getLocale(paramJobj.optString(Constants.language))));
                }
            }

            if (productList.length > 0) {
                for (int i = 0; i < productList.length; i++) {
                    if (!StringUtil.isNullOrEmpty(productList[i])) {
                        accProductObj.deleteProductCategoryMappingDtails(productList[i]);
                    }
                }
            }

            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            
            if (productList.length > 0 && productCategory.length > 0) {
                for (int i = 0; i < productList.length; i++) {
                    for (int j = 0; j < productCategory.length; j++) {
                        if (!StringUtil.isNullOrEmpty(productList[i]) && !StringUtil.isNullOrEmpty(productCategory[j])) {
                            String productCategoryId = StringUtil.equal(productCategory[j], "None") ? null : productCategory[j];
                            accProductObj.saveProductCategoryMapping(productList[i], productCategoryId);

                            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productList[i]);
                            Product product = (Product) prodresult.getEntityList().get(0);

                            KwlReturnObject categoryresult = accProductObj.getObject(MasterItem.class.getName(), productCategory[j]);
                            MasterItem ccategory = (MasterItem) categoryresult.getEntityList().get(0);
                            String categoryName = StringUtil.equal(productCategory[j], "None") ? "None" : ccategory.getValue();
                            productName = product.getName();
                            productId = product.getID();
                            auditMsg = " added new product category " + categoryName + " to ";
                            auditID = AuditAction.PRODUCT_CATEGORY_CHANGED;

                            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + auditMsg + product.getName(), auditRequestParams, product.getID());
                        }
                    }
                    auditMsg = " Updated new Industry Code " + codeVal + " to ";
                    auditID = AuditAction.PRODUCT_INDUSRTYCODE_CHANGED;
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("industryCode","")) && Integer.parseInt(company.getCountry().getID()) == Constants.malaysian_country_id) {
                        accProductObj.updateProductCategoryIndustryCode(productList[i], industryCode);
                        auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + auditMsg + productName, auditRequestParams, productId);
                    }

                }
            }
            issuccess = true;
//            txnManager.commit(status);
        } catch (Exception e) {
            throw ServiceException.FAILURE("saveProductCategoryMapping : " + e.getMessage(), e);
//            txnManager.rollback(status);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put("accException", isAccountingExe);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("saveProductCategoryMapping : " + ex.getMessage(), ex);
            }
        }
        return jobj;
    }

 @Override    
    public Producttype getProductTypeByProductID(String productTypeID) throws AccountingException {
        Producttype producttype = null;
        try {
            if (!StringUtil.isNullOrEmpty(productTypeID)) {
                KwlReturnObject retObj = accProductObj.getProductTypeByProductID(productTypeID);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    producttype = (Producttype) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Product Type");
        }
        return producttype;
    }

    public double calMonthwiseDepreciation(double openingbalance, double salvage, double month) throws ServiceException {
        double amount;
        try {
            amount = (openingbalance - salvage) / month;
        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("calMonthwiseDepreciation : " + ne.getMessage(), ne);
        }
        return amount;
    }

    public double getFormatedNumber(double number) {
        NumberFormat nf = new DecimalFormat("0.00");
        String formatedStringValue = nf.format(number);
        double formatedValue = Double.parseDouble(formatedStringValue);
        return formatedValue;
    }

    public double calDoubleDepreciationPercent(double openingbalance, double month) throws ServiceException {
        double doubleDepreciationPercent = 0d;
        try {
            double oneMonthDepriciationPercent = ((openingbalance / month) / openingbalance) * 100;
            doubleDepreciationPercent = oneMonthDepriciationPercent * 2;
            doubleDepreciationPercent = getFormatedNumber(doubleDepreciationPercent);

        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("calMonthwiseDepreciation : " + ne.getMessage(), ne);
        }
        return doubleDepreciationPercent;
    }
    @Override
    public Map<String, Object> exportStockLedger(JSONArray dataJarr, DateFormat df, String companyid) throws JSONException {
        Map<String, Object> stockLedgerMap = new HashMap<>();
        Map<String, Object> detailsMap = new HashMap<>();
        List< Map<String, Object>> stockLedgerMapList = new ArrayList<>();
        //SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        for (int i = 0; i <= dataJarr.length()-1; i++) {
            JSONObject obj = dataJarr.getJSONObject(i);
            int transType = obj.optInt("transType", -1);
            stockLedgerMap = new HashMap<>();
            stockLedgerMap.put("id", obj.optString(Constants.productid, ""));
            stockLedgerMap.put("prodcode", obj.optString("pid", ""));
            stockLedgerMap.put("prodname", obj.optString("productDesc", ""));
            if (transType != 6 && transType != -1) {
                stockLedgerMap.put("date", df.format(new Date(obj.optString("transactionDate"))));
            }else{
                stockLedgerMap.put("date","");
            }
            stockLedgerMap.put("documentno", obj.optString("transactionNumber", ""));
            stockLedgerMap.put("code", obj.optString("personCode", ""));
            stockLedgerMap.put("party", obj.optString("personName", ""));

            stockLedgerMap.put("received", authHandler.roundQuantity(obj.optDouble("received", 0), companyid));

            stockLedgerMap.put("delivered", authHandler.roundQuantity(obj.optDouble("delivered", 0), companyid));

            stockLedgerMap.put("stockrate", authHandler.roundUnitPrice(obj.optDouble("stockRate", 0), companyid));
            
            if (transType == 0 || transType == 1 || transType == 4 || transType == 6 || transType == -1 || transType== 7) {
                    // For GR/SR Module OR Opening
                    stockLedgerMap.put("recvalue", authHandler.round(obj.optDouble("value", 0), companyid));
                    stockLedgerMap.put("delvalue",authHandler.round(0, companyid));
                } else {
                    // For DO/PR Module
                     stockLedgerMap.put("delvalue", authHandler.round(obj.optDouble("value", 0), companyid));
                     stockLedgerMap.put("recvalue",authHandler.round(0, companyid));
                }
            stockLedgerMap.put("stockvalue", authHandler.round(obj.optDouble("value", 0), companyid));
            stockLedgerMap.put("quantityOnHand", authHandler.roundQuantity(obj.optDouble("QtyOnHandJasper", 0), companyid));
            stockLedgerMap.put("valuation", authHandler.round(obj.optDouble("ValuationJasper", 0), companyid));
            stockLedgerMap.put("isQtyAndValuationJSON", obj.optInt("isQtyAndValuationJSON", 0) );
            stockLedgerMap.put("ValuationMethod", obj.optString("ValuationMethod", "") );

            stockLedgerMap.put("balance", authHandler.round(obj.optDouble("balance", 0), companyid));
            stockLedgerMapList.add(stockLedgerMap);
        }

        detailsMap.put("StockLedgerSubReportData", new JRBeanCollectionDataSource(stockLedgerMapList));
        return  detailsMap;
    }
    
    @Override
    public JSONObject getDefaultColumns(HashMap<String, Object> params) {
        JSONObject jresult = new JSONObject();
        JSONArray jarr = new JSONArray();
        try {
            String moduleid = (String) params.get(Constants.moduleid);
            HashMap<String, Object> filters = new HashMap<String, Object>();
            List filter_names = new ArrayList();
            List filter_params = new ArrayList();
            filter_names.add("module");
            filter_params.add(moduleid);
            filters.put("filter_names", filter_names);
            filters.put("filter_values", filter_params);
            KwlReturnObject dresult = accAccountDAOobj.getDefaultHeaders(filters);
            Iterator ite = dresult.getEntityList().iterator();
            while (ite.hasNext()) {
                JSONObject jobj = new JSONObject();
                DefaultHeader tmpcontyp = (DefaultHeader) ite.next();
                
                if(StringUtil.equalIgnoreCase("11", tmpcontyp.getXtype()) || StringUtil.equalIgnoreCase("12", tmpcontyp.getXtype())) {//skipped iteration if fieldtype is Checkbox or Checklist
                    continue;
                }
                jobj.put("fieldlabel", tmpcontyp.getDefaultHeader());
                jobj.put("isessential", tmpcontyp.isRequired() ? 1:0);
                jobj.put("maxlength", tmpcontyp.getMaxLength());
                jobj.put("validationtype", tmpcontyp.getValidateType());
                jobj.put("fieldid", tmpcontyp.getId());
                jobj.put("moduleid", tmpcontyp.getModule().getId());
                jobj.put("fieldtype", (StringUtil.equalIgnoreCase("13", tmpcontyp.getXtype()) ? "1" : tmpcontyp.getXtype()));//13 is for TextArea and instead of it sent as TextField
                jobj.put("iseditable", !tmpcontyp.isIsreadonly());
                jobj.put("dataindex", tmpcontyp.getDataIndex());
                jobj.put("iscustomfield", false);
                jobj.put("iscustomcolumn", false);
                jarr.put(jobj);
            }
            jresult.put("data", jarr);
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
        return jresult;
    }
     
    /**
     * Description : Method to calculate the Depreciation Rate
     * @param <cost> Cost of Asset
     * @param <salvage> Scrap Value
     * @param <life> Asset Life
     * @return : doubleDepreciationPercent
     */
    private double calulateWDVRate(double cost, double salvage, double life) throws ServiceException {
        double doubleDepreciationPercent = 0d;
        try {
            double percent = (1 - Math.pow((salvage / cost), (1 / life))) * 100;
            doubleDepreciationPercent = percent / 12;
        } catch (NumberFormatException ne) {
            throw ServiceException.FAILURE("calulateWDVRate : " + ne.getMessage(), ne);
        }
        return doubleDepreciationPercent;
    }
    
    @Override
    public KwlReturnObject saveProductTerms(HashMap<String, Object> productTermMap) throws ServiceException {
        KwlReturnObject saveProductTerm = null;
        try {
            saveProductTerm = accProductObj.saveProductTermsMap(productTermMap);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccProductModuleServiceImpl.saveProductTerms : " + ex.getMessage(), ex);
        }
        return saveProductTerm;
    }
    /**
     * 
     * @param paramsjobj
     * @return 
     * @Flags=
     * isPriceFromBand - Takes price from price band
     * isPriceFromVolumeDiscount - Takes price from volume discount
     * isPriceFromUseDiscount - Checks volume discount having use discount type or flat price
     */  
    @Override
    public JSONObject getIndividualProductPrice(JSONObject paramsjobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            String currencyID = paramsjobj.optString(Constants.globalCurrencyKey, "");
            String companyid = paramsjobj.getString(Constants.companyKey);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            boolean isPriceFromBand = false, isPriceBandMappedWithVolDisc = false;// isPriceBandMappedWithVolDisc - Flag to check volume discount is mapped with price band or not
            boolean isbandPriceNotAvailable = false; // Flag is used to warn user that pricing not available
            boolean isPriceFromVolumeDiscount = false;
            boolean isPriceFromUseDiscount = false;
            String discountType = "", volumeDiscountID = "";
            double discountValue = 0;
            double qty=0d;
            boolean isIncludingGst = false;
            boolean isSpecialRateExist = false;
            HashMap<String, Object> requestParams = productHandler.getProductRequestMapfromJson(paramsjobj);
            boolean skipRichTextArea=false;
            if(paramsjobj.has("skipRichTextArea")){
                skipRichTextArea= Boolean.parseBoolean(String.valueOf(paramsjobj.get("skipRichTextArea")));
            }

            DateFormat df = (DateFormat) requestParams.get("df");
            DateFormat dateOnlyFormat = authHandler.getDateOnlyFormatter(paramsjobj);
            String date = (String) requestParams.get("transactiondate");
            int  moduleid =paramsjobj.optInt("moduleid",0);
            //carryin : This will be true for vendor documents, false for customer documents  
            boolean carryin =!StringUtil.isNullOrEmpty(paramsjobj.optString("carryin", null)) ? Boolean.parseBoolean(paramsjobj.getString("carryin")) : false;
            String affecteduser = !StringUtil.isNullOrEmpty((String) requestParams.get("affecteduser"))?(String) requestParams.get("affecteduser"):"";
            Date transactionDate = null;
            try {
                transactionDate = (date == null ? null : df.parse(date));
            } catch (ParseException ex) {
                Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("getProducts : " + ex.getMessage(), ex);
            }
            /**
             *Special Rate check for purchase side
             */
            boolean isBandsWithSpecialRateForPurchase = false;
            JSONObject columnPrefjObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                columnPrefjObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                if (columnPrefjObj.has("bandsWithSpecialRateForPurchase") && columnPrefjObj.get("bandsWithSpecialRateForPurchase") != null && columnPrefjObj.optBoolean("bandsWithSpecialRateForPurchase", false)) {
                    isBandsWithSpecialRateForPurchase = true;
                }
            }
            
            String productid[] = ((String) requestParams.get(Constants.productid)).split(",");
            if (!StringUtil.isNullOrEmpty(paramsjobj.optString(Constants.productid))) {
                for (int i = 0; i < productid.length; i++) {
                    JSONObject obj = new JSONObject();
                    KwlReturnObject result = null;

                    // for get price from pricing band
                    if (extraCompanyPreferences != null && ((carryin && extraCompanyPreferences.isProductPricingOnBands()) || (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales()))) {
                       /**
                         * if Price List band with special rate for sale/purchase activated then first check for special rate
                         * */
                        if ((!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales() && extraCompanyPreferences.isBandsWithSpecialRateForSales()) || (carryin && extraCompanyPreferences.isProductPricingOnBands() && isBandsWithSpecialRateForPurchase)) {
                            KwlReturnObject specialRateResult = accProductObj.getSpecialRateofProduct(productid[i], carryin, transactionDate, (String) requestParams.get("affecteduser"), (String) requestParams.get("forCurrency"));
                            if (specialRateResult.getEntityList() != null && !specialRateResult.getEntityList().isEmpty() && specialRateResult.getEntityList().get(0) != null) {
                                isSpecialRateExist = true;
                                double specialRate = (Double) specialRateResult.getEntityList().get(0);

                                obj.put("price", specialRate);
                                if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                    obj.put("rate", specialRate);
                                }
                                obj.put(Constants.productid, productid[i]);
                                jobj.append("data", obj);
                            }
                        }

                        if (!isSpecialRateExist) {
                            String pricingBandMasterID = "";
                            KwlReturnObject affectedUserResult = null;

                            boolean isVolumeDisocuntExist = false;
                            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                            filterRequestParams.put("companyid", companyid);
                            KwlReturnObject volmResult = accMasterItemsDAOobj.getPriceListVolumeDiscountItems(filterRequestParams);
                            if (volmResult != null && !volmResult.getEntityList().isEmpty()) {
                                isVolumeDisocuntExist = true;
                            }
                            obj.put("isVolumeDisocuntExist", isVolumeDisocuntExist);

                            // to get price from price list volume discount - use discount
                            HashMap<String, Object> pricingDiscountRequestParams = new HashMap<String, Object>();
                            pricingDiscountRequestParams.put("isPricePolicyUseDiscount", true);
                            pricingDiscountRequestParams.put("productID", productid[i]);
                            pricingDiscountRequestParams.put("isPurchase", carryin);
                            pricingDiscountRequestParams.put("applicableDate", transactionDate);
                            pricingDiscountRequestParams.put("companyID", companyid);
                            pricingDiscountRequestParams.put("currencyID", requestParams.get("currency"));
                            if (requestParams.containsKey("quantity") && requestParams.get("quantity") != null) {
                                qty=Double.parseDouble(requestParams.get("quantity").toString());
                                pricingDiscountRequestParams.put("quantity", requestParams.get("quantity"));
                            }
                           /**
                             *check price band is mapped with customer/vendor or not.
                             */
                            if (!carryin) { 
                                affectedUserResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), (String) requestParams.get("affecteduser"));
                                Customer customer = affectedUserResult != null ? (Customer) affectedUserResult.getEntityList().get(0) : null;
                                if (customer != null && customer.getPricingBandMaster() != null) {
                                    pricingBandMasterID = customer.getPricingBandMaster().getID();
                                    isPriceFromBand = true;
                                    isIncludingGst = customer.getPricingBandMaster().isIsIncludingGST();
                                } else {
                                    isPriceFromBand = false;
                                }
                            } else { // check price band is mapped with vendor or not
                                affectedUserResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), (String) requestParams.get("affecteduser"));
                                Vendor vendor = affectedUserResult != null ? (Vendor) affectedUserResult.getEntityList().get(0) : null;
                                if (vendor != null && vendor.getPricingBandMaster() != null) {
                                    pricingBandMasterID = vendor.getPricingBandMaster().getID();
                                    isPriceFromBand = true;
                                    isIncludingGst = vendor.getPricingBandMaster().isIsIncludingGST();
                                } else {
                                    isPriceFromBand = false;
                                }
                            }
                            /* For purchase requisition is priceband where customer and vendor is not present so
                             * making isPriceFromBand true.
                             */
                            if (moduleid == Constants.Acc_Purchase_Requisition_ModuleId || moduleid == Constants.Acc_RFQ_ModuleId) {
                                isPriceFromBand = true;
                            }
                            /*
                             change mapped customer masterband to selected band from drop down
                             */
                            if ( paramsjobj.has("pricingbandmaster") && !StringUtil.isNullOrEmpty(paramsjobj.getString("pricingbandmaster"))) {
                                pricingBandMasterID = paramsjobj.optString("pricingbandmaster", pricingBandMasterID);
                            }
                            /**
                             *getting the volume discount which fall under quantity and also mapped with customer/vendor. 
                             */
                            result = accProductObj.getProductPriceFromPriceListVolumeDiscount(pricingDiscountRequestParams);
                            JSONObject JVolObj = getMappedVolumeDiscountwithBand(result,volumeDiscountID,pricingBandMasterID,qty);
                            int volumlistCount = JVolObj.optInt("volumlistCount");
                            String matchedvolumeDiscountID = JVolObj.optString("volumeDiscountID");
                            isPriceBandMappedWithVolDisc = JVolObj.optBoolean("isPriceBandMappedWithVolDisc");
                            if (result.getEntityList() != null && !result.getEntityList().isEmpty() && !StringUtil.isNullOrEmpty(matchedvolumeDiscountID)) { // For Use Discount
                                /**
                                 * volumlistCount : Get productdetails record matched with volume discount.
                                 */
                                Object[] rowObj = (Object[]) result.getEntityList().get(volumlistCount); 

                                KwlReturnObject detailResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) rowObj[5]);
                                PricingBandMasterDetail pricingBandMasterDetail = detailResult != null ? (PricingBandMasterDetail) detailResult.getEntityList().get(0) : null;

                                discountType = pricingBandMasterDetail.getDiscountType();
                                discountValue = pricingBandMasterDetail.getDiscountValue();
                                volumeDiscountID = pricingBandMasterDetail.getPricingBandMaster().getID();
                                isPriceFromUseDiscount = true;
                                obj.put("isPriceFromUseDiscount", true);
                                obj.put("priceSourceUseDiscount", pricingBandMasterDetail.getPricingBandMaster().getName());

                                isPriceFromBand = true;
                                isPriceFromVolumeDiscount = false;
                            } else { // For Flat Price Volume Discount
                                obj.put("isPriceFromUseDiscount", false);
                                // to get price from price list volume discount
                                pricingDiscountRequestParams.put("currencyID", requestParams.get("currency"));
                                pricingDiscountRequestParams.put("isPricePolicyUseDiscount", false);

                                result = accProductObj.getProductPriceFromPriceListVolumeDiscount(pricingDiscountRequestParams);
                                /**
                                 * getting the volume discount which fall under
                                 * quantity and also mapped with
                                 * customer/vendor.
                                 */
                                JVolObj = getMappedVolumeDiscountwithBand(result, volumeDiscountID, pricingBandMasterID, qty);
                                volumlistCount = JVolObj.optInt("volumlistCount");
                                volumeDiscountID = JVolObj.optString("volumeDiscountID");
                                isPriceBandMappedWithVolDisc = JVolObj.optBoolean("isPriceBandMappedWithVolDisc");
                                if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                   Object[] row = (Object[]) result.getEntityList().get(volumlistCount); 
                                    KwlReturnObject detailResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) row[5]);
                                    PricingBandMasterDetail pricingBandMasterDetail = detailResult != null ? (PricingBandMasterDetail) detailResult.getEntityList().get(0) : null;
                                    volumeDiscountID = pricingBandMasterDetail.getPricingBandMaster().getID();
                                    if (row == null) {
                                        obj.put("isVolumeDisocunt", false);
                                        obj.put("price", 0);
                                        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                            obj.put("rate", 0);
                                        }
                                        obj.put("priceSource", "");
                                    } else {
                                        isPriceFromVolumeDiscount = true;
                                        obj.put("isVolumeDisocunt", true);
                                        obj.put("price", row[0]);
                                        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                            obj.put("rate", row[0]);
                                        }
//                                        obj.put("priceSource", row[4]);
                                        obj.put("priceSourceUseDiscount", pricingBandMasterDetail.getPricingBandMaster().getName());
                                        obj.put("purchaseprice", ((Double) row[6]) >= 0 ? (Double) row[6] : 0);
                                    }

                                    obj.put(Constants.productid, productid[i]);
                                    /*
                                     When quantity is zero in that case no need to put data
                                     */
                                    if (qty > 0) {
                                        jobj.append("data", obj);
                                    }
                                }
                            }
                           
                            obj.put("pricingbandmasterid", pricingBandMasterID);
                            /*
                            If price band selected from drop down
                            */
                            if(!StringUtil.isNullObject(pricingBandMasterID)){
                                isPriceFromBand = true;
                            }
                            if (isPriceFromBand) {
                                /**
                                 * Scenario 3 
                                 * -->Price is only available in USD (Document Currency)
                                 * -->Transaction is entered in USD (Document Currency)
                                 * Result: take USD Pricing
                                 */
                                HashMap<String, Object> pricingBandRequestParams = new HashMap<String, Object>();
                                pricingBandRequestParams.put("productID", productid[i]);
                                pricingBandRequestParams.put("isPurchase", carryin);
                                pricingBandRequestParams.put("pricingBandMasterID", pricingBandMasterID);
                                pricingBandRequestParams.put("applicableDate", transactionDate);
                                pricingBandRequestParams.put("currencyID", requestParams.get("currency"));
                                pricingBandRequestParams.put("companyID", companyid);
                                // checked  which priceband is exist fot that product
                               result = accProductObj.getProductPriceFromPricingBand(pricingBandRequestParams);
                               isbandPriceNotAvailable = true;
                                if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                    isbandPriceNotAvailable=false;
                                    if (!isPriceBandMappedWithVolDisc) {
                                        obj.put("priceSource", "");
                                        obj.put("isVolumeDisocunt", false);
                                        obj.put("isPriceFromUseDiscount", false);
                                    }else{//put matched volume discount id to JS side
                                        obj.put("matchvolumeDiscountid", volumeDiscountID);
                                    }
                                    isPriceFromBand = true;
                                    Object[] row = (Object[]) result.getEntityList().get(0);

                                    if (row == null) {
                                        obj.put("isPriceListBand", false);
                                        obj.put("price", 0);
                                        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                            obj.put("rate", 0);
                                        }
                                        obj.put("priceSource", "");
                                        obj.put("pricingbandmasterid", "");
                                    } else {
                                        double price = (Double) row[0];
                                        double purchaseprice = (Double) row[3];
                                        if ((isPriceFromUseDiscount && requestParams.containsKey("quantity") && requestParams.get("quantity") != null) && (isPriceBandMappedWithVolDisc)) {
                                            if (discountType.equalsIgnoreCase("0")) { // for Flat discount
                                                price = price - discountValue;
                                                purchaseprice = purchaseprice - discountValue;
                                            } else if (discountType.equalsIgnoreCase("1")) { // for Percent discount
                                                price = price - ((price * discountValue) / 100);
                                                purchaseprice = purchaseprice - ((purchaseprice * discountValue) / 100);
                                            }
                                        }
                                        /*
                                        if volume discount is applied to given qty
                                         */
                                        if ((isPriceFromVolumeDiscount && requestParams.containsKey("quantity") && requestParams.get("quantity") != null) && (isPriceBandMappedWithVolDisc)) {
                                            obj.put("priceSource", row[2]);
                                        } else {
                                            obj.put("price", (price >= 0) ? price : 0);
                                            obj.put("purchaseprice", purchaseprice >= 0 ? purchaseprice : 0);
                                            obj.put("priceSource", row[2]);
                                        }
                                        
                                        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                            obj.put("rate",  (price >= 0) ? price : 0);
                                        }
                                        obj.put("isPriceListBand", true);
                                        obj.put("isIncludingGst", isIncludingGst);
                                        obj.put("pricingbandmasterid", pricingBandMasterID);
                                        if (!carryin && !StringUtil.isNullOrEmpty((String) requestParams.get("affecteduser"))) {
                                            getProductBrandDiscountForBand(productid[i], (String) requestParams.get("affecteduser"), companyid, (String) requestParams.get("currency"), transactionDate, obj, pricingBandMasterID);
                                        }
                                    }
                                    obj.put(Constants.productid, productid[i]);
                                    jobj.append("data", obj);
                                } else {
                                    /**
                                     * Scenario 1
                                     * -->Price is only available in SGD (Base Currency)
                                       -->Transaction is entered in USD (Document Currency)
                                       * Result: transaction should convert the SGD price to USD using applicable rate with a warning to user "USD Price is not available, price are converted from Base Currency"
                                     */
                                    if (requestParams.containsKey("currency") && requestParams.get("currency") != null && !requestParams.get("currency").toString().equalsIgnoreCase(currencyID)) {
                                            /**
                                             *isPriceFromBand:- this flag is true when value of priceband  is not present for foreign currency.
                                             * then price is converted from  base currency of priceband.
                                             * And below function checks the band is mapped with which volume discount.
                                             */ 
//                                        isbandPriceNotAvailable = false;
                                        List returnList = getPriceListBandPriceInDocumentCurrencyConvertedFromBaseCurrency(paramsjobj, pricingBandRequestParams, requestParams, productid[i], carryin, currencyID, obj, isPriceFromBand, isPriceFromUseDiscount, isPriceBandMappedWithVolDisc, isIncludingGst, transactionDate, discountType, discountValue, pricingBandMasterID, companyid, jobj);
                                        if (returnList != null && !returnList.isEmpty()) {
                                            isPriceFromBand = (Boolean) returnList.get(0);
                                        } else {
                                            isPriceFromBand = false;
                                            isbandPriceNotAvailable = true;
                                        }
                                    } else {
                                        isPriceFromBand = false;
                                        isbandPriceNotAvailable = true;
                                    }
                                }
                            }
                            /*
                             reset all if any volume discount not applied to band
                             */
                            if (!isPriceBandMappedWithVolDisc) {
                                affectedUserResult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), pricingBandMasterID);
                                PricingBandMaster bandMaster = affectedUserResult != null ? (PricingBandMaster) affectedUserResult.getEntityList().get(0) : null;
                                if (bandMaster != null && !isbandPriceNotAvailable) {
                                    obj.put("priceSource", bandMaster.getName());
                                    obj.put("isPriceListBand", true);
                                    obj.put("pricingbandmasterid", pricingBandMasterID);
                                } else {
                                    obj.put("priceSource", "");
                                    obj.put("isPriceListBand", false);
                                    obj.put("pricingbandmasterid", "");
                                }
                                obj.put("isVolumeDisocunt", false);
                                obj.put("isPriceFromUseDiscount", false);
                                obj.put("isVolumeDisocuntExist", false);
                            }
                        }
                    }
                    KwlReturnObject kwlReturnObject = accountingHandlerDAOobj.getObject(Product.class.getName(), productid[i]);
                    Product product = (Product) kwlReturnObject.getEntityList().get(0);
                    boolean isAssemblyProduct=product.getProducttype().getID().equals(Producttype.ASSEMBLY);
                    /**
                     * Checking if UOM id is passed as param from js if not then
                     * setting products stock uomid and checking if price is
                     * mapped to uom, if not then passing uomid as empty to
                     * fetch the initial price of product.
                     * ERM-389 / ERP-35140
                     */
                    String uomid = "";
                    if (!StringUtil.isNullOrEmpty(paramsjobj.optString("uomid", ""))) {
                        uomid = paramsjobj.optString("uomid", "");
                    } else {
                        uomid = product != null ? product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getID() : "" : "";
                    }
                    boolean isUOMPresent = false;
                    if (!StringUtil.isNullOrEmpty(uomid)) {
                        String currency = !StringUtil.isNullOrEmpty((String) requestParams.get("forCurrency")) ? (String) requestParams.get("forCurrency") : (String) requestParams.get("currency");
                        isUOMPresent = accProductObj.checkIfPriceIsMappedToUOMInPriceList(productid[i], carryin, transactionDate, (String) requestParams.get("affecteduser"), currency, uomid);
                        if (!isUOMPresent) {
                            uomid = "";
                        }
                    }
                    obj.put("isPriceMappedToUOM", isUOMPresent);
                    // if pricing band is not activated or band price for product is not available and (if special rate not exist)  then check from price list
                    
                    /*
                    Adding (!isPriceFromVolumeDiscount || isbandPriceNotAvailable) = Volume discount not applied to qty and band price not available 
                    but thr might be case i.e. isPriceFromVolumeDiscount is true and isbandPriceNotAvailable is true in that case if block should be executed.
                    */
                     if ((((carryin && extraCompanyPreferences.isProductPricingOnBands()) || (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales())) && !isPriceFromBand && (!isPriceFromVolumeDiscount || isbandPriceNotAvailable) && !isPriceBandMappedWithVolDisc && (requestParams.get("currency").toString().equalsIgnoreCase(currencyID) || extraCompanyPreferences.isProductPriceinMultipleCurrency()) || (carryin && !extraCompanyPreferences.isProductPricingOnBands() || !carryin && !extraCompanyPreferences.isProductPricingOnBandsForSales())) && !isSpecialRateExist) {
                        /**
                         * If price list band is activated and band is not set
                         * for vendor/customer then populate initial price in
                         * the purchase/sales transactions. ERP-32054
                         */
                        if (carryin && !extraCompanyPreferences.isProductPricingOnBands() || !carryin && !extraCompanyPreferences.isProductPricingOnBandsForSales()) {
                            /**
                             * If price list band is not activated, then take
                             * the price from pricelist.
                             */
                            obj.put("isPriceMappedToUOM", isUOMPresent);
                            boolean excludeInitialPrice = product.isAsset() ? false : true;
                            String currency = !StringUtil.isNullOrEmpty((String) requestParams.get("forCurrency")) ? (String) requestParams.get("forCurrency") : (String) requestParams.get("currency");
                            result = accProductObj.getProductPrice(productid[i], carryin, transactionDate, affecteduser, currency,uomid,excludeInitialPrice);
                        } else {
                            /**
                             * If band is activated and band is not set for the
                             * vendor/customer then populate initial price.
                             */
                            if (requestParams.get("currency") != null && product.getCurrency() != null && product.getCurrency().getCurrencyID().equals((String) requestParams.get("currency"))) {
                                result = accProductObj.getInitialPrice(productid[i], carryin);
                            } else {
                                result = null;
                            }
                        }
                        List list = result != null ? result.getEntityList() : new ArrayList();
                        Iterator itr = list.iterator();
                        if (itr.hasNext()) {
                            Object row = itr.next();
                            if (row == null) {
                                isbandPriceNotAvailable = true;
                                obj.put("price", 0);
                                if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                    obj.put("rate", 0);
                                }
                            } else {
                                isbandPriceNotAvailable = false;
                                double price = (Double) row;
                                /**
                                 * If price list band is activated and band is
                                 * not set for vendor/customer then populate
                                 * initial price in the purchase/sales
                                 * transactions. ERP-32054
                                 */
                                KwlReturnObject purchase = null;
                                if (carryin && !extraCompanyPreferences.isProductPricingOnBands() || !carryin && !extraCompanyPreferences.isProductPricingOnBandsForSales()) {
                                    /**
                                     * If price list band is not activated, then
                                     * take the price from pricelist.
                                     */
                                    String currency = !StringUtil.isNullOrEmpty((String) requestParams.get("forCurrency")) ? (String) requestParams.get("forCurrency") : (String) requestParams.get("currency");
                                    boolean excludeInitialPrice = product.isAsset() ? false : true;
                                    purchase = accProductObj.getProductPrice(productid[i], true, transactionDate, affecteduser, currency, uomid, excludeInitialPrice);
                                } else {
                                    /**
                                     * If band is activated and band is not set
                                     * for the vendor/customer then populate
                                     * initial price.
                                     */
                                    if (requestParams.get("currency") != null && product.getCurrency() != null && product.getCurrency().getCurrencyID().equals((String) requestParams.get("currency"))) {
                                        purchase = accProductObj.getInitialPrice(productid[i], true);
                                    } else {
                                        purchase = null;
                                    }
                                }
                                double purchaseprice = purchase != null ? (purchase.getEntityList().isEmpty() ? 0 : (purchase.getEntityList().get(0) == null ? 0 : (Double) purchase.getEntityList().get(0))) : 0;
                                if (isPriceBandMappedWithVolDisc && isPriceFromUseDiscount && requestParams.containsKey("quantity") && requestParams.get("quantity") != null) {
                                    if (discountType.equalsIgnoreCase("0")) { // for Flat discount
                                        price = price - discountValue;
                                        purchaseprice = purchaseprice - discountValue;
                                    } else if (discountType.equalsIgnoreCase("1")) { // for Percent discount
                                        price = price - ((price * discountValue) / 100);
                                        purchaseprice = purchaseprice - ((purchaseprice * discountValue) / 100);
                                    }
                                }
                                obj.put("price", (price >= 0) ? price : 0);
                                /**
                                 * If the product is inventory Assembly type of product then following scenarios will apply:
                                 * 1 If price band is active and band price is set for Assembly type of product then it will take band price
                                 * 2 If band price is not active or band price not given for Assembly type of product and price is given in price list then it will take price list price
                                 * 3 If band price is not given and price list price is also not given then the price will be taken as sum of its BOM products.
                                 */
                                if (isAssemblyProduct && row==null && !carryin && !isSpecialRateExist && (extraCompanyPreferences.isProductPricingOnBandsForSales() && !isPriceFromBand && (!isPriceFromVolumeDiscount || isbandPriceNotAvailable) && !isPriceBandMappedWithVolDisc || (!extraCompanyPreferences.isProductPricingOnBandsForSales()))) {
                                    JSONObject assmPrdPriceObj = getAssemblyProductSalesPrice(paramsjobj, productid[i]);
                                    double priceForAssembly = assmPrdPriceObj.optDouble("price", 0);
                                    obj.put("price", priceForAssembly);
                                    if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                        obj.put("rate", priceForAssembly);
                                    }
                                    jobj.append("data", obj);
                                    if (price != 0) {
                                        isbandPriceNotAvailable = false;
                                    }
                                }
                                if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                    obj.put("rate", (price >= 0) ? price : 0);
                                }
                                obj.put("purchaseprice", purchaseprice >= 0 ? purchaseprice : 0);
                            }
                        } else {
                            isbandPriceNotAvailable = true;
                            obj.put("price", 0);
                            if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                                obj.put("rate", 0);
                            }
                        }
                        obj.put(Constants.productid, productid[i]);
                        jobj.append("data", obj);
                    } else if ((((carryin && extraCompanyPreferences.isProductPricingOnBands()) || (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales())) && !isPriceFromBand && !isPriceFromVolumeDiscount && !isPriceFromUseDiscount) && !isSpecialRateExist) {
                        isbandPriceNotAvailable = true;
                        obj.put("price", 0);                          //For Foreign currency if product isproductpricingonband is true
                        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                            obj.put("rate", 0);
                        }
                        obj.put(Constants.productid, productid[i]);
                        jobj.append("data", obj);
                    }
                    if (product != null) {
                        obj.put("producttype", product.getProducttype().getID());
                        obj.put("isAsset", product.isAsset());
                    }
                    /**
                     *Below code will execute when amend price fuctionality is activated.
                     */
                    accProductObj.getamendingPurchaseprice(productid[i], paramsjobj.optString("userid"),transactionDate,(String) requestParams.get("forCurrency"), uomid,obj);
                    
                    /**
                     * Scenario 2
                        -->Price is available in USD (Other than Base Currency)
                        -->Transaction is entered in SGD (Base Currency) or any other currency aside from USD
                        * Result: warn user "Price is not available, please maintain your pricing"
                     */
                    if (isbandPriceNotAvailable && extraCompanyPreferences != null && ((carryin && extraCompanyPreferences.isProductPricingOnBands()) || (!carryin && extraCompanyPreferences.isProductPricingOnBandsForSales()))) {
                        obj.put("isbandPriceNotAvailable", isbandPriceNotAvailable);
                    }
                    /**
                     * get baseuom rate saved in uomschema.
                     */
                    if (!StringUtil.isNullOrEmpty(productid[i])) {
                        String uomschematypeid = product.getUomSchemaType()!=null?product.getUomSchemaType().getID():"";
                        if (!StringUtil.isNullOrEmpty(uomschematypeid)) {
                            requestParams.put("uomschematypeid", uomschematypeid);
                            requestParams.put("currentuomid", paramsjobj.optString("uomid"));
                            requestParams.put("companyid", companyid);
                            requestParams.put("carryin", carryin);
                            KwlReturnObject res = accProductObj.getProductBaseUOMRate(requestParams);
                            List list = res.getEntityList();
                            Iterator itr = list.iterator();
                            if (itr.hasNext()) {
                                UOMSchema row = (UOMSchema) itr.next();
                                if (row == null) {
                                    obj.put("baseuomrate", 1);
                                } else {
                                    obj.put("baseuomrate", row.getBaseuomrate());
                }
                                jobj.append("data", obj);
                            } else {
                                obj.put("baseuomrate", 1);
                                jobj.append("data", obj);
                            }
                        } else {
                            obj.put("baseuomrate", 1);
                            jobj.append("data", obj);
                        }
                        jobj.append("data", obj);
                    }
                }

                if (productid.length == 1) {
                    transactionDate = (transactionDate == null) ? new Date() : transactionDate;
                    HashMap<String, Object> basicParams = new HashMap();
                    basicParams.put("productId", productid[0]);
                    basicParams.put("companyId", companyid);
                    basicParams.put("transactionDate", transactionDate);
                    basicParams.put("dateFormat", dateOnlyFormat);

                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                    fieldrequestParams.put("skipRichTextArea", skipRichTextArea);
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);
                    AccProductCustomData obj = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), productid[0]);
                    if (obj != null) {
                        productHandler.setCustomColumnValuesForProduct(obj, FieldMap, replaceFieldMap, variableMap);
                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                JSONObject jsonObj = new JSONObject();
                                jsonObj.put(varEntry.getKey(), coldata);
                                jsonObj.put("key", varEntry.getKey());
                                jobj.append("data", jsonObj);
                            }
                        }
                    }
                }
            }
            /**
             * Below code is for getting multiple discount applied on product
             * and passing it as JSON Object in discountData key
             */
            JSONObject jObj = null;
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref()) && !isSpecialRateExist && !carryin) {
                jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                if (jObj.has(Constants.DISCOUNTMASTER) && jObj.get(Constants.DISCOUNTMASTER) != null && jObj.optBoolean(Constants.DISCOUNTMASTER,false)) {
                    JSONObject params = new JSONObject();
                    JSONObject resultJobj = jobj.getJSONArray("data").getJSONObject(0);
                    params.put("pricingBandMasterId", resultJobj.optString("pricingbandmasterid", ""));
                    params.put("productId", resultJobj.optString("productid", ""));
                    params.put("companyid", companyid);
                    params.put("applicableDate", transactionDate);
                    params.put("currencyId", requestParams.get("currency"));            //passing the transaction currency to get discount

                    JSONObject productDiscount = getIndividualProductDiscount(params);
                    JSONArray productDiscountJArr = productDiscount.getJSONArray("data");
                    if (productDiscountJArr.length() > 0) {
                        jobj.put("discountData", productDiscountJArr);
                    }
                }
            }
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    /**
     * Method to get the volume discount which fall under quantity and also mapped
     * with customer/vendor.
     */
    public JSONObject getMappedVolumeDiscountwithBand(KwlReturnObject result, String volumeDiscountID, String pricingBandMasterID, double qty) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        int volumlistCount = 0;
        boolean isPriceBandMappedWithVolDisc = false;
        /**
         * checks volume discount matches with entered qty.
         */
        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
            List allmatchedVolumedisclist = result.getEntityList();//get all matched volume discounts fall under the quantity.
            KwlReturnObject volmapped = accMasterItemsDAOobj.getPricingVolumeDiscountMapped(pricingBandMasterID);
            if (volmapped != null && volmapped.getEntityList() != null && !volmapped.getEntityList().isEmpty() && !allmatchedVolumedisclist.isEmpty() && qty > 0) {
                List<PricingBandmappingWithVolumeDisc> volMappedList = volmapped.getEntityList();
                for (PricingBandmappingWithVolumeDisc voldiscid : volMappedList) {
                    for (volumlistCount = 0; volumlistCount < allmatchedVolumedisclist.size(); volumlistCount++) {
                        Object[] pricingdetailrow = (Object[]) allmatchedVolumedisclist.get(volumlistCount);
                        String mappedvolumediscountid = (String) pricingdetailrow[7];
                        if (mappedvolumediscountid.equals(voldiscid.getVolumediscountid().getID())) {
                            isPriceBandMappedWithVolDisc = true;
                            volumeDiscountID = mappedvolumediscountid;
                            break;
                        }
                    }
                    if (isPriceBandMappedWithVolDisc) {
                        break;
                    }
                }
            }
        }
        jobj.put("volumeDiscountID", volumeDiscountID);
        jobj.put("isPriceBandMappedWithVolDisc", isPriceBandMappedWithVolDisc);
        jobj.put("volumlistCount", isPriceBandMappedWithVolDisc ? volumlistCount:0);
        return jobj;
    }
    @Override
    public JSONObject getAssemblyProductSalesPrice(JSONObject paramsjobj, String productid) throws JSONException, ServiceException {
        JSONObject obj = new JSONObject();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.productid, productid);
        requestParams.put(Constants.currencyKey, paramsjobj.get(Constants.globalCurrencyKey));
        KwlReturnObject result = accProductObj.getAssemblyItems(requestParams);

        JSONObject requestJSON = new JSONObject(paramsjobj.toString());
        double price = 0;
        if (result != null && !result.getEntityList().isEmpty()) {
            Iterator itr = result.getEntityList().iterator();
            while (itr.hasNext()) {//iterating for each assemly items and fetching its sales price and doing sumation for final price 
                Object[] row = (Object[]) itr.next();
                ProductAssembly passembly = (ProductAssembly) row[0];
                if (passembly != null && passembly.getSubproducts() != null) {
                    requestJSON.put(Constants.productid, passembly.getSubproducts().getID());
                    if (!StringUtil.isNullOrEmpty(paramsjobj.optString("quantity", null))) {
                        double prdEnteredQuantity = Double.parseDouble(paramsjobj.getString("quantity"));
                        double itemQuantity = prdEnteredQuantity * passembly.getQuantity();
                        requestJSON.put("quantity", itemQuantity);
                    }
                    JSONObject jobj = getIndividualProductPrice(requestJSON);//calling this method to get sales price of each assembly items 
                    JSONArray dataArr = jobj.getJSONArray(Constants.data);
                    for (int index = 0; index < dataArr.length(); index++) {
                        JSONObject productPriceObj = dataArr.getJSONObject(index);
                        price += productPriceObj.optDouble("price", 0) * passembly.getQuantity();
                    }
                }
            }
        }
        
        obj.put("price", price);
        if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
            obj.put("rate", price);
        }
        return obj;
    }
    
    /**
     * Description: Method is used to set Price List Band Price in Base Currency to jobj and return isPriceFromBand flag
     * @param paramsjobj
     * @param pricingBandRequestParams
     * @param requestParams
     * @param productid
     * @param carryin
     * @param globalCurrencyKey
     * @param obj
     * @param isPriceFromBand
     * @param isPriceFromUseDiscount
     * @param isPriceBandMappedWithVolDisc
     * @param isIncludingGst
     * @param transactionDate
     * @param discountType
     * @param discountValue
     * @param pricingBandMasterID
     * @param companyid
     * @param jobj
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    @Override
    public List getPriceListBandPriceInDocumentCurrencyConvertedFromBaseCurrency(JSONObject paramsjobj, HashMap<String, Object> pricingBandRequestParams, HashMap<String, Object> requestParams, String productid, boolean carryin, String globalCurrencyKey, JSONObject obj, boolean isPriceFromBand, boolean isPriceFromUseDiscount, boolean isPriceBandMappedWithVolDisc, boolean isIncludingGst, Date transactionDate, String discountType, double discountValue, String pricingBandMasterID, String companyid, JSONObject jobj) throws ServiceException, JSONException {
        List returnList = new ArrayList();
        boolean isBandPriceConvertedFromBaseCurrency = false;
        // check price in base curruncy in price list band
        pricingBandRequestParams.put("currencyID", globalCurrencyKey);
        // checked  which priceband is exist fot that product
        KwlReturnObject result = accProductObj.getProductPriceFromPricingBand(pricingBandRequestParams);
        if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
            isPriceFromBand = true;
            Object[] row = (Object[]) result.getEntityList().get(0);

            if (row == null) {
                obj.put("isPriceListBand", false);
                obj.put("price", 0);
                if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    obj.put("rate", 0);
                }
                obj.put("priceSource", "");
            } else {
                double price = row[0] !=null ? (Double) row[0] : 0;
                // to convert price in document currency from base currency
                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, price, (String) requestParams.get("currency"), transactionDate, 0);
                double priceAmt = bAmt.getEntityList()!= null ? bAmt.getEntityList().get(0)!=null ? (Double) bAmt.getEntityList().get(0) : 0 : 0;
                price = authHandler.round(priceAmt, companyid);
                isBandPriceConvertedFromBaseCurrency = true;

                double purchaseprice = (Double) row[3];
                if ((isPriceFromUseDiscount && requestParams.containsKey("quantity") && requestParams.get("quantity") != null) && (isPriceBandMappedWithVolDisc)) {
                    if (discountType.equalsIgnoreCase("0")) { // for Flat discount
                        price = price - discountValue;
                        purchaseprice = purchaseprice - discountValue;
                    } else if (discountType.equalsIgnoreCase("1")) { // for Percent discount
                        price = price - ((price * discountValue) / 100);
                        purchaseprice = purchaseprice - ((purchaseprice * discountValue) / 100);
                    }
                }

                obj.put("price", (price >= 0) ? price : 0);
                if (paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
                    obj.put("rate", (price >= 0) ? price : 0);
                }
                obj.put("isPriceListBand", true);
                obj.put("isIncludingGst", isIncludingGst);
                obj.put("priceSource", row[2]);
                obj.put("purchaseprice", purchaseprice >= 0 ? purchaseprice : 0);
                obj.put("isBandPriceConvertedFromBaseCurrency", isBandPriceConvertedFromBaseCurrency);

                if (!carryin && !StringUtil.isNullOrEmpty((String) requestParams.get("affecteduser"))) {
                    getProductBrandDiscountForBand(productid, (String) requestParams.get("affecteduser"), companyid, (String) requestParams.get("currency"), transactionDate, obj, pricingBandMasterID);
                }
            }
            obj.put(Constants.productid, productid);
            jobj.append("data", obj);
        } else {
            isPriceFromBand = false;
        }
        
        returnList.add(isPriceFromBand); // index 0
                
        return returnList;
    }
    
    
    /**
     * Description: Method used to get Product Brand Discount For Price List - Band
     * @param productID
     * @param customerID
     * @param companyID
     * @param currencyID
     * @param transactionDate
     * @param obj
     * @throws ServiceException 
     * @throws JSONException 
     */
 @Override   
    public void getProductBrandDiscountForBand(String productID, String customerID, String companyID, String currencyID, Date transactionDate, JSONObject obj, String pricingBandMasterID) throws ServiceException, JSONException {
        KwlReturnObject kwlReturnObject = accountingHandlerDAOobj.getObject(Product.class.getName(), productID);
        Product product = (Product) kwlReturnObject.getEntityList().get(0);
        
//        if (product.getProductBrand() != null) {
            // for getting product brand discount rule for sales transaction
            boolean isCustomerCategoryRecordExist = false;
            boolean isAnyRuleRecordExist = false;
            HashMap<String, Object> params = new HashMap<>();
            ArrayList filt_names = new ArrayList(), filt_params = new ArrayList();
            filt_names.add("company.companyID");
            filt_params.add(companyID);
            filt_names.add("pricingBandMaster.ID");
            filt_params.add(pricingBandMasterID);
            params.put("filter_names", filt_names);
            params.put("filter_params", filt_params);
            String columnno="";
            KwlReturnObject resultList = accProductObj.getProductBrandDiscountDetailsList(params);
            if (resultList.getEntityList() != null && !resultList.getEntityList().isEmpty()) {
                ProductBrandDiscountDetails productBrandDiscountDetails = (ProductBrandDiscountDetails) resultList.getEntityList().get(0);
                isCustomerCategoryRecordExist = productBrandDiscountDetails.isIsCustomerCategory();
                isAnyRuleRecordExist = true;
                /**
                 * Get Column number of dimension
                 */
                columnno=""+productBrandDiscountDetails.getProductBrand().getField().getColnum();
                
            }

            if (isAnyRuleRecordExist) {
                /**
                 * Need to get dimension value tagged to product from custom table
                 */
                
                String dimesnionValue="";
                Map<String, Object> requestMap = new HashMap<>();
                requestMap.put("productid", productID);
                requestMap.put("colnum", columnno);
                kwlReturnObject=accProductObj.getProductCustomData(requestMap);
                if (kwlReturnObject.getEntityList() != null && !kwlReturnObject.getEntityList().isEmpty() && kwlReturnObject.getEntityList().get(0) != null) {
                    dimesnionValue=kwlReturnObject.getEntityList().get(0).toString();
                }
                requestMap.put("bandID", pricingBandMasterID);
                requestMap.put("isCustomerCategory", isCustomerCategoryRecordExist);
                requestMap.put("currencyID", currencyID);
                requestMap.put("productBrandID",dimesnionValue);
                requestMap.put("applicableDate", transactionDate);
                requestMap.put("companyID", companyID);
                if (isCustomerCategoryRecordExist) {
                    KwlReturnObject custCategoryObject = accCustomerDAOobj.getCustomerCategoryIDs(customerID);
                    if (custCategoryObject.getEntityList() != null && !custCategoryObject.getEntityList().isEmpty()) {
                        CustomerCategoryMapping customerCategoryMapping = (CustomerCategoryMapping) custCategoryObject.getEntityList().get(0);
                        requestMap.put("customerCategoryID", customerCategoryMapping.getCustomerCategory().getID());

                        KwlReturnObject detailResult = accProductObj.getProductBrandDiscountDetails(requestMap);
                        if (detailResult.getEntityList() != null && !detailResult.getEntityList().isEmpty()) {
                            KwlReturnObject ProductBrandDiscountDetailsObject = accountingHandlerDAOobj.getObject(ProductBrandDiscountDetails.class.getName(), (String) detailResult.getEntityList().get(0));
                            ProductBrandDiscountDetails productBrandDiscountDetails = (ProductBrandDiscountDetails) ProductBrandDiscountDetailsObject.getEntityList().get(0);
                            obj.put("discountType", productBrandDiscountDetails.getDiscountType());
                            obj.put("discountValue", productBrandDiscountDetails.getDiscountValue());
                        }
                    }
                } else {
                    requestMap.put("customerID", customerID);

                    KwlReturnObject detailResult = accProductObj.getProductBrandDiscountDetails(requestMap);
                    if (detailResult.getEntityList() != null && !detailResult.getEntityList().isEmpty()) {
                        KwlReturnObject ProductBrandDiscountDetailsObject = accountingHandlerDAOobj.getObject(ProductBrandDiscountDetails.class.getName(), (String) detailResult.getEntityList().get(0));
                        ProductBrandDiscountDetails productBrandDiscountDetails = (ProductBrandDiscountDetails) ProductBrandDiscountDetailsObject.getEntityList().get(0);
                        obj.put("discountType", productBrandDiscountDetails.getDiscountType());
                        obj.put("discountValue", productBrandDiscountDetails.getDiscountValue());
                    }
                }
            }
//        }
    } 
    
  @Override  
    public JSONObject getProducts(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = productHandler.getProductRequestMapfromJson(paramJobj);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("includeParent", null))) {
                requestParams.put("includeParent", paramJobj.getString("includeParent"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("wcid", null))) {
                requestParams.put("wcid", paramJobj.getString("wcid"));
            }
 
            /**
             * This Function will use when Users Visibility Feature is Enable
             * Append user condition while querying data
             */
            /*
            If User is not Admin then It will fecth the Asset Group details..
            */
       
          boolean isFixedAsset = paramJobj.has("isFixedAsset") && !StringUtil.isNullObject(paramJobj.opt("isFixedAsset")) ? Boolean.parseBoolean(paramJobj.get("isFixedAsset").toString()) : false;
          ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", paramJobj.optString("companyid"));
            if (!isFixedAsset && extraPref != null && extraPref.isUsersVisibilityFlow() && !paramJobj.optBoolean(Constants.reportFlag, false)) {
                KwlReturnObject object = accountingHandlerDAOobj.getObject(User.class.getName(), paramJobj.optString("userid"));
                User user = object.getEntityList().size() > 0 ? (User) object.getEntityList().get(0) : null;
                    if (!AccountingManager.isCompanyAdmin(user)) {
                        /**
                         * if Users visibility enable and current user is not admin
                         */
                        Map<String, Object> reqMap = new HashMap();
                        requestParams.put("isUserVisibilityFlow", true);
                        reqMap.put("companyid", paramJobj.optString("companyid"));
                        reqMap.put("userid", paramJobj.optString("userid"));
                        reqMap.put("jointable", "pcd");
                        reqMap.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                        String custcondition = fieldManagerDAOobj.appendUsersCondition(reqMap);
                        if (!StringUtil.isNullOrEmpty(custcondition)) {
                            /**
                             * If mapping found with dimension
                             */
                            String usercondition = " and (" + custcondition + ")";
                            requestParams.put("appendusercondtion", usercondition);
                        } else {
                            /**
                             * If no Mapping found for current ser then return
                             * function call
                             */
                            jobj.put(Constants.RES_success, true);
                            jobj.put(Constants.RES_msg, msg);
                            jobj.put(Constants.RES_data, new com.krawler.utils.json.JSONArray());
                            jobj.put(Constants.RES_TOTALCOUNT, 0);
                            return jobj;
                        }
                    }
                }
            KwlReturnObject result = accProductObj.getProducts(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();

            JSONArray DataJArr = getProductsJson(paramJobj, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_TOTALCOUNT, count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
 @Override
    public JSONArray getProductsJson(JSONObject paramJobj, List list) throws JSONException, ServiceException, SessionExpiredException, AccountingException, ParseException {

        //If you are changing anything in this function then make same changes in the getProductsJson function which is available in productHandler file
        JSONArray jArr = new JSONArray();
        Producttype producttype = new Producttype();
        String productid = paramJobj.optString(Constants.productid, null);
        boolean isForProductQuantityDetailsReport = false;
        String ss = "";
        boolean isForProductMaster = false;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isForProductQuantityDetailsReport",null))) {
            isForProductQuantityDetailsReport = Boolean.parseBoolean(paramJobj.getString("isForProductQuantityDetailsReport"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isForProductMaster",null))) {
            isForProductMaster = Boolean.parseBoolean(paramJobj.getString("isForProductMaster"));
        }
        String[] selectedUOMsArray = null;
        if (isForProductQuantityDetailsReport) {
            String selectedUOMs = paramJobj.optString("selectedUOMs", "");
            selectedUOMsArray = selectedUOMs.split(",");
        }

        Boolean isSearch = false;
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isUsedInTransaction = false;
        boolean isUsedInBatchSerial = false;
        String companyid = paramJobj.getString(Constants.companyKey);
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        KwlReturnObject companyObj1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company compObj = (Company) companyObj1.getEntityList().get(0);
        if (paramJobj.optString("ss",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("ss",null))) {
            isSearch = true;
            ss = paramJobj.getString("ss");
        }
        Date transactionDate = null;
        String transDate = StringUtil.isNullOrEmpty(paramJobj.optString("transactiondate",null)) ? "" : paramJobj.getString("transactiondate");
        if (!StringUtil.isNullOrEmpty(transDate)) {  //Used same date formatter which have used to save currency exchange
            transactionDate = authHandler.getDateOnlyFormat().parse(transDate);
        }
        String companyCurrencyID = paramJobj.getString(Constants.globalCurrencyKey);
        boolean isdefaultHeaderMap = paramJobj.optBoolean(Constants.isdefaultHeaderMap, false);
        boolean isProductView = paramJobj.optBoolean("isProductView",false);
        Boolean isFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isFixedAsset,null))) ? Boolean.parseBoolean(paramJobj.getString(Constants.isFixedAsset)) : false;
        Boolean nonSaleInventory = Boolean.parseBoolean( paramJobj.optString("loadInventory",""));
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, (isFixedAsset ? Constants.Acc_FixedAssets_AssetsGroups_ModuleId : Constants.Acc_Product_Master_ModuleId)));
        FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        try {
            Map<String, Double> blockedQuantityList = accProductObj.getProductBlockedQuantity(compObj, null, null, ss);
            Map<String, Double> quantityUnderQA = accProductObj.getProductQuantityUnderQA(compObj, null);
            Map<String, Double> quantityUnderRepair = accProductObj.getProductQuantityUnderRepair(compObj, null);
            KwlReturnObject extraObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraObj.getEntityList().get(0);
            Map<String, Double> quantityUnderQAForGRNAndDO = new HashMap<>();
            if (extraCompanyPreferences != null && extraCompanyPreferences.isPickpackship()) {
                /**
                 * If Pick, Pack & Ship flow is activated then we don't add
                 * entry in inventory table. Get quantity present in QC store
                 * for Delivery Order.
                 */
                boolean isGRNOnly = false;
                quantityUnderQAForGRNAndDO = accProductObj.getProductQuantityUnderQAForGRNAndDO(compObj, null, isGRNOnly);
            } else {
                boolean isGRNOnly = true;
                /**
                 * If Pick, Pack & Ship flow is deactivated then we add entry in
                 * inventory table So, no need to get quantity present in QC
                 * store for delivery order separately.
                 */
                quantityUnderQAForGRNAndDO = accProductObj.getProductQuantityUnderQAForGRNAndDO(compObj, null, isGRNOnly);
            }
            
            
            Map<String, Double> quantityUnderQAForWO = new HashMap<>();
            if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateMRPModule()) {
                boolean isWOOnly = true;
                /**
                 * If MRPModule is activated then need to get quantity present in QC
                 * store for WORK ORDER.
                 */
                quantityUnderQAForWO = accProductObj.getProductQuantityUnderQAForWO(compObj, null, isWOOnly);

            }                                                                                                                                        
            
            
            Map<String, Double> quantityUnderRepairForGRNAndDO = new HashMap<>();
            if (extraCompanyPreferences != null && extraCompanyPreferences.isPickpackship()) {
                /**
                 * If Pick, Pack & Ship flow is activated then we don't add
                 * entry in inventory table. Get quantity present in QC store
                 * for Delivery Order.
                 */
                quantityUnderRepairForGRNAndDO = accProductObj.getProductQuantityUnderRepairForGRNAndDO(compObj, null, false);
            } else {
                /**
                 * If Pick, Pack & Ship flow is deactivated then we add entry in
                 * inventory table So, no need to get quantity present in QC
                 * store for delivery order separately.
                 */
                quantityUnderRepairForGRNAndDO = accProductObj.getProductQuantityUnderRepairForGRNAndDO(compObj, null, true);
            }
            
            Map<String, Double> quantityUnderRepairForWO = new HashMap<>();
            if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateMRPModule()) {
                boolean isWOOnly = true;
                /**
                 * If MRPModule is activated then need to get quantity present in REPAIR
                 * store for WORK ORDER.
                 */
                quantityUnderRepairForWO = accProductObj.getProductQuantityUnderRepairForWO(compObj, null, isWOOnly);

            }  
            
            
            Map<String, Double> doApprovedOrRejectedOrPickedQtyMap = new HashMap<>();
            if (!extraCompanyPreferences.isPickpackship()) {
                doApprovedOrRejectedOrPickedQtyMap = accProductObj.getProductApprovedOrRejectedOrPickedQty(compObj, null);
            }
            
            Map<String, Double> blkQuantityList = accProductObj.getProductBlockedQuantity(compObj,  null,  null,  ss);
            String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(paramJobj.optString("cdomain",""), false);
            for (Object object : list) {
                try {
                    isUsedInTransaction = false;
                    isUsedInBatchSerial = false;
                    Object[] row = (Object[]) object;
                    Product product = (Product) row[0];
                    if (product.getID().equals(productid)) {
                        continue;
                    }
                    String productType = (product.getProducttype() != null ? product.getProducttype().getID() : "");
                    if (nonSaleInventory && productType.equals(producttype.Inventory_Non_Sales)) {
                        continue;
                    }
                    Product parentProduct = product.getParent();
                    String productCurrencyID = product.getCurrency() == null ? "" : product.getCurrency().getCurrencyID();
                    JSONObject obj = new JSONObject();
                    obj.put(Constants.productid, product.getID());
                    obj.put("productname", product.getName());
                    obj.put("purchasetaxId", StringUtil.isNullOrEmpty(product.getPurchasetaxid()) ? "" : product.getPurchasetaxid());
                    obj.put("salestaxId",  StringUtil.isNullOrEmpty(product.getSalestaxid()) ? "" : product.getSalestaxid());
                    
                    /*---------Execute only for malaysian company----------   */
                    if (!StringUtil.isNullOrEmpty(product.getPurchasetaxid())) {
                        KwlReturnObject taxresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), product.getPurchasetaxid());
                        Tax tax = (Tax) taxresult.getEntityList().get(0);
                        obj.put("purchasetax", tax != null ? tax.getTaxCode() : "");
                    } else {
                        obj.put("purchasetax", "");
                    }
                    
                    if (!StringUtil.isNullOrEmpty(product.getSalestaxid())) {
                        KwlReturnObject taxresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), product.getSalestaxid());
                        Tax tax = (Tax) taxresult.getEntityList().get(0);
                        obj.put("salestax", tax != null ? tax.getTaxCode() : "");
                    } else {
                        obj.put("salestax", "");
                    }
                    
                    if (isProductView) {
                        if (row[17] != null) {
                            obj.put("productCategory", row[17]);
                        }else{
                            obj.put("productCategory", "None");
                        }
                    }
                    
                    obj.put("description", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                    obj.put("desc", product.getDescription());
                    obj.put("tariffname", product.getTariffName());
                    obj.put("hsncode", product.getHSNCode());
                    obj.put("reportinguom", product.getReportinguom());
                    UnitOfMeasure uom = product.getUnitOfMeasure();
                    UnitOfMeasure purchaseuom = product.getPurchaseUOM();
                    UnitOfMeasure salesuom = product.getSalesUOM();
                    UnitOfMeasure displayUoM = product.getDisplayUoM();
                    obj.put("displayUoMid", displayUoM == null ? "" : displayUoM.getID());
                    obj.put("uomid", uom == null ? "" : uom.getID());
                    obj.put("currencyid", productCurrencyID);
                    double qaQuantity = 0, repairQuantity = 0, qaQuantityForGRNAndDO = 0, repairQuantityForGRNAndDO = 0, doApprovedOrRejectedOrPickedQty = 0, qaQuantityForWO=0,repairQuantityForWO=0;
                    if (quantityUnderQA.containsKey(product.getID()) && quantityUnderQA.get(product.getID()) != null) {
                        qaQuantity = quantityUnderQA.get(product.getID());
                    }
                    if (quantityUnderRepair.containsKey(product.getID()) && quantityUnderRepair.get(product.getID()) != null) {
                        repairQuantity = quantityUnderRepair.get(product.getID());
                    }
                    if (quantityUnderQAForGRNAndDO.containsKey(product.getID()) && quantityUnderQAForGRNAndDO.get(product.getID()) != null) {
                        qaQuantityForGRNAndDO = quantityUnderQAForGRNAndDO.get(product.getID());
                    }
                    if (quantityUnderRepairForGRNAndDO.containsKey(product.getID()) && quantityUnderRepairForGRNAndDO.get(product.getID()) != null) {
                        repairQuantityForGRNAndDO = quantityUnderRepairForGRNAndDO.get(product.getID());
                    }
                    if (quantityUnderQAForWO.containsKey(product.getID()) && quantityUnderQAForWO.get(product.getID()) != null) {
                        qaQuantityForWO = quantityUnderQAForWO.get(product.getID());
                    }
                    if (quantityUnderRepairForWO.containsKey(product.getID()) && quantityUnderRepairForWO.get(product.getID()) != null) {
                        repairQuantityForWO = quantityUnderRepairForWO.get(product.getID());
                    }
                    if (doApprovedOrRejectedOrPickedQtyMap.containsKey(product.getID()) && doApprovedOrRejectedOrPickedQtyMap.get(product.getID()) != null) {
                        doApprovedOrRejectedOrPickedQty = doApprovedOrRejectedOrPickedQtyMap.get(product.getID());
                    }
                    obj.put("qaquantity", qaQuantity);
                    obj.put("repairquantity", repairQuantity);
                    obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                    obj.put("displayUoMName", displayUoM == null ? "" : displayUoM.getNameEmptyforNA());
                    obj.put("purchaseuom", purchaseuom == null ? "" : purchaseuom.getID());
                    obj.put("salesuom", salesuom == null ? "" : salesuom.getID());
                    obj.put("stockpurchaseuomvalue", purchaseuom == null || product.getPackaging() == null ? 1 : product.getPackaging().getStockUomQtyFactor(purchaseuom));
                    obj.put("stocksalesuomvalue", salesuom == null || product.getPackaging() == null ? 1 : product.getPackaging().getStockUomQtyFactor(salesuom));
                    obj.put("precision", uom == null ? 0 : (Integer) uom.getAllowedPrecision());
                    obj.put("leadtime", product.getLeadTimeInDays());
                    obj.put("QAleadtime", product.getQALeadTimeInDays());
                    obj.put("hsCode", product.getHSCode());
                    obj.put("warrantyperiod", product.getWarrantyperiod());
                    obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
                    obj.put("asofdate", product.getAsOfDate());
                    obj.put("industryCodeId", product.getIndustryCodeId());
                    obj.put("supplier", product.getSupplier());
                    obj.put("coilcraft", product.getCoilcraft());
                    obj.put("interplant", product.getInterplant());
                    obj.put("syncable", product.isSyncable());
                    obj.put("deleted", product.isDeleted());
                    obj.put("multiuom", product.isMultiuom());
                    obj.put("blockLooseSell", product.isblockLooseSell());
                    obj.put("uomschematypeid", product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "");
                    obj.put("autoAssembly", product.isAutoAssembly());
                    obj.put("isLocationForProduct", product.isIslocationforproduct());
                    obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
                    obj.put("isRowForProduct", product.isIsrowforproduct());
                    obj.put("isRackForProduct", product.isIsrackforproduct());
                    obj.put("isBinForProduct", product.isIsbinforproduct());
                    obj.put("isBatchForProduct", product.isIsBatchForProduct());
                    obj.put("isSerialForProduct", product.isIsSerialForProduct());
                    obj.put("isSKUForProduct", product.isIsSKUForProduct());
                    obj.put("isRecyclable", product.isRecyclable());
                    obj.put("recycleQuantity", product.getRecycleQuantity());
                    obj.put("qaenable", product.isQaenable());
                    obj.put("reorderlevel", product.getReorderLevel() == 0 ? "" : product.getReorderLevel());
                    obj.put("reorderquantity", product.getReorderQuantity() == 0 ? "" : product.getReorderQuantity());
                    obj.put("minorderingquantity", product.getMinOrderingQuantity());
                    obj.put("maxorderingquantity", product.getMaxOrderingQuantity());
                    obj.put("purchaseaccountid", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : ""));
                    obj.put("salesaccountid", (product.getSalesAccount() != null ? product.getSalesAccount().getID() : ""));
                    obj.put("shelfLocationId", (product.getShelfLocation() != null ? product.getShelfLocation().getId() : ""));
                    obj.put("location", (product.getLocation() != null ? product.getLocation().getId() : ""));
                    obj.put("warehouse", (product.getWarehouse() != null ? product.getWarehouse().getId() : ""));
                    obj.put("locationName", (product.getLocation() != null ? product.getLocation().getName() : ""));
                    obj.put("warehouseName", (product.getWarehouse() != null ? product.getWarehouse().getName() : ""));
                    obj.put("activateProductComposition", product.isActivateProductComposition());
                    obj.put("purchaseaccountname", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getName() : ""));
                    obj.put("salesaccountname", (product.getSalesAccount() != null ? product.getSalesAccount().getName() : ""));
                    obj.put("purchaseretaccountname", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getName() : ""));
                    obj.put("salesretaccountname", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getName() : ""));
                    obj.put("interStatePurAccID", (product.getInterStatePurchaseAccount() != null ? product.getInterStatePurchaseAccount().getID() : ""));
                    obj.put("interStatePurAccCformID", (product.getInterStatePurchaseAccountCForm() != null ? product.getInterStatePurchaseAccountCForm().getID() : ""));
                    obj.put("interStatePurReturnAccID", (product.getInterStatePurchaseReturnAccount() != null ? product.getInterStatePurchaseReturnAccount().getID() : ""));
                    obj.put("interStatePurReturnAccCformID", (product.getInterStatePurchaseAccountCForm() != null ? product.getInterStatePurchaseAccountCForm().getID() : ""));
                    obj.put("interStateSalesAccID", (product.getInterStateSalesAccount() != null ? product.getInterStateSalesAccount().getID() : ""));
                    obj.put("interStateSalesAccCformID", (product.getInterStateSalesAccountCForm() != null ? product.getInterStateSalesAccountCForm().getID() : ""));
                    obj.put("interStateSalesReturnAccID", (product.getInterStateSalesReturnAccount() != null ? product.getInterStateSalesReturnAccount().getID() : ""));
                    obj.put("interStateSalesReturnAccCformID", (product.getInterStateSalesReturnAccountCForm() != null ? product.getInterStateSalesReturnAccountCForm().getID() : ""));
                    obj.put("inputVAT", (product.getInputVAT() != null ? product.getInputVAT().getID() : ""));
                    obj.put("cstVATattwo", (product.getCstVATattwo() != null ? product.getCstVATattwo().getID() : ""));
                    obj.put("cstVAT", (product.getCstVAT() != null ? product.getCstVAT().getID() : ""));
                    obj.put("inputVATSales", (product.getInputVATSales() != null ? product.getInputVATSales().getID() : ""));
                    obj.put("cstVATattwoSales", (product.getCstVATattwoSales() != null ? product.getCstVATattwoSales().getID() : ""));
                    obj.put("cstVATSales", (product.getCstVATSales() != null ? product.getCstVATSales().getID() : ""));
                    obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
                    obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
                    obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                    obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                    obj.put("salesRevenueRecognitionAccountid", (product.getSalesRevenueRecognitionAccount() != null ? product.getSalesRevenueRecognitionAccount().getID() : ""));
                    obj.put("revenueRecognitionProcess", (product.isRevenueRecognitionProcess()));
                    obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                    
                    String filePathString = baseUrl + "productimage?fname=" + product.getID() + ".png&isDocumentDesignerPrint=true";
                    obj.put(Constants.imageTag, filePathString);
                    if (isFixedAsset) {   //For Fixed Asset Group, type will be "Asset"
                        obj.put("type", "Asset");
                    } else {
                        obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName() : ""));
                    }

                    //for Multi Group Company Flag-putting vendor to which products are mapped
                    if (extraCompanyPreferences.isActivateGroupCompaniesFlag()) {
                        //getting the json vendor mapped products
                        JSONObject returnJobj = accProductObj.getVendorsMappedProduct(product.getID());
                        if (returnJobj.has("vendor") && !StringUtil.isNullOrEmpty(returnJobj.optString("vendor", null))) {
                            obj.put("vendor", returnJobj.optString("vendor"));
                        }

                        if (returnJobj.has("vendorname") && !StringUtil.isNullOrEmpty(returnJobj.optString("vendorname", null))) {
                            obj.put("vendorname", returnJobj.optString("vendorname"));
                        }

                        if (returnJobj.has("vendorcode") && !StringUtil.isNullOrEmpty(returnJobj.optString("vendorcode", null))) {
                            obj.put("vendorcode", returnJobj.optString("vendorcode"));
                        }
                    } else {
                        obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                        obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                        obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
                        obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                    }
                    
                    
                    if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {
                        obj.put("dependenttype", (product.getDependenttype() != null ? product.getDependenttype().getID() : ""));
                        obj.put("dependenttypename", (product.getDependenttype() != null ? product.getDependenttype().getValue() : ""));
                        obj.put("intervalfield", product.isIntervalfield());
                        obj.put("timeinterval", product.getTimeinterval());
                        obj.put("addshiplentheithqty", product.isAddshiplentheithqty());
                        obj.put("noofqty", product.getNoofquqntity());
                        obj.put("qtyUOM", product.getNoofqtyvalue());
                    }
                    obj.put("pid", product.getProductid());
                    if (product.getWarrantyperiod() == 0) {
                        obj.put("warranty", "N/A");
                    } else {
                        obj.put("warranty", product.getWarrantyperiod());
                    }
                    if (product.getWarrantyperiodsal() == 0) {
                        obj.put("warrantysal", "N/A");
                    } else {
                        obj.put("warrantysal", product.getWarrantyperiodsal());
                    }
                    obj.put("sequenceformatid", product.getSeqformat() != null ? product.getSeqformat().getID() : "");
                    obj.put("parentuuid", parentProduct == null ? "" : parentProduct.getID());
                    obj.put("parentid", parentProduct == null ? "" : parentProduct.getProductid());
                    obj.put("parentname", parentProduct == null ? "" : parentProduct.getName());
                    if (isSearch) {
                        obj.put("level", 0);
                        obj.put("leaf", true);
                    } else {
                        obj.put("level", row[1]);
                        obj.put("leaf", row[2]);
                    }

                    Double purchasePrice = row[3] != null ? (Double) row[3] : 0;
                    Double salesPrice = row[4] != null ? (Double) row[4] : 0;

                    obj.put("purchaseprice", authHandler.roundUnitPrice(purchasePrice, companyid));
                    obj.put("assetSaleGL", (product.getSellAssetGLAccount() != null) ? product.getSellAssetGLAccount().getID() : "");
                    obj.put("depreciationGLAccount", (product.getDepreciationGLAccount() != null) ? product.getDepreciationGLAccount().getID() : "");
                    obj.put("depreciationMethod", product.getDepreciationMethod());
                    obj.put("depreciationRate", product.getDepreciationRate());
                    obj.put("sellAssetGLAccount", product.getSellAssetGLAccount() != null ? product.getSellAssetGLAccount().getID() : "");
                    obj.put("depreciationProvisionGLAccount", (product.getDepreciationProvisionGLAccount() != null) ? product.getDepreciationProvisionGLAccount().getID() : "");
                    obj.put("saleprice", authHandler.roundUnitPrice(salesPrice, companyid));
                    obj.put("salespriceinpricecurrency", row[4] == null ? 0 : row[4]);
                    if (product.getCurrency() != null) {
                        obj.put("currencysymbol", product.getCurrency().getCurrencyCode());
                    }
                    double availbleQuantity = 0;
                    if (product.isblockLooseSell() && !isForProductMaster) {
                        KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(),uom != null ? uom.getID() : "");
                        availbleQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                        availbleQuantity = (availbleQuantity - (qaQuantityForGRNAndDO + repairQuantityForGRNAndDO + qaQuantityForWO + repairQuantityForWO) + doApprovedOrRejectedOrPickedQty);
                        obj.put("quantity", availbleQuantity);
                    } else {
                        availbleQuantity = row[5] == null ? 0 : (Double) row[5];
                        availbleQuantity = (availbleQuantity - (qaQuantityForGRNAndDO + repairQuantityForGRNAndDO + qaQuantityForWO + repairQuantityForWO) + doApprovedOrRejectedOrPickedQty);
                        obj.put("quantity", availbleQuantity);
                    }
                    /* Fetching Data for "Reserve Stock" column in Product Master*/
                    obj.put("reservestock", accProductObj.getReserveQuantityTaggedInQuotation(product.getID(), companyid));

                    obj.put("leasedQuantity", (row[14] == null ? 0 : row[14]));
                    obj.put("consignquantity", (row[15] == null ? 0 : row[15]));
                    obj.put("venconsignquantity", (row[16] == null ? 0 : row[16]));
                    obj.put("initialquantity", (row[6] == null ? 0 : row[6]));
                    obj.put("initialprice", (row[7] == null ? 0 : row[7]));
                    obj.put("salespricedatewise", (row[9] == null ? 0 : row[9]));
                    obj.put("purchasepricedatewise", (row[10] == null ? 0 : row[10]));
                    obj.put("initialsalesprice", (row[11] == null ? 0 : row[11]));
                    double lockQuantityInSelectedUOM = 0;
                    if (product.isblockLooseSell() && blkQuantityList.containsKey(product.getID()) && !isForProductMaster) {
//                        KwlReturnObject qtyResult = accProductObj.getLockQuantityInSelectedUOM(product.getID(), uom != null ? uom.getID() : "");
//                        lockQuantityInSelectedUOM = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
//                        obj.put("lockquantity", (lockQuantityInSelectedUOM));
                        obj.put("lockquantity", (blkQuantityList.get(product.getID())));
                    } else {
                        lockQuantityInSelectedUOM = row[13] == null ? 0 : (Double) row[13];
                        obj.put("lockquantity", (lockQuantityInSelectedUOM));
                    }
                    if (blockedQuantityList.containsKey(product.getID())) {
                        obj.put("lockquantity", blockedQuantityList.get(product.getID()));
                        lockQuantityInSelectedUOM = blockedQuantityList.get(product.getID());
                    }
                    /**
                     * If Pick Pack & Ship is activated then we do not post
                     * entry in inventory table. So, we need to subtract the
                     * balance quantity if QA and Pick Pack & Ship features are
                     * activated.
                     * 
                     * Balance Qty= Total Available Qty + Qty. in QA Store +
                     * Qty. in repair store.
                     */
                    double balanceQuantity = availbleQuantity - (lockQuantityInSelectedUOM + qaQuantity + repairQuantity);
                    obj.put("balancequantity", (balanceQuantity));
                    obj.put("productweight", product != null ? (Double) product.getProductweight() : "");
                    obj.put("netproductweight", product != null ? (Double) (product.getProductweight() * availbleQuantity) : "");
                    obj.put("productweightperstockuom", product != null ? product.getProductWeightPerStockUom() : "");
                    obj.put("productweightincludingpakagingperstockuom", product != null ? product.getProductWeightIncludingPakagingPerStockUom() : "");
                    obj.put("productvolumeperstockuom", product.getProductVolumePerStockUom());
                    obj.put("productvolumeincludingpakagingperstockuom", product.getProductVolumeIncludingPakagingPerStockUom());
                    obj.put("barcodefield", product.getBarcodefield() + "");

                    //General Tab extra added fields
                    obj.put("barcode", (product.getBarcode() != null) ? product.getBarcode() : "");
                    obj.put("additionaldescription", (product.getAdditionalDesc() != null) ? product.getAdditionalDesc() : "");
                    obj.put("foreigndescription", (product.getDescInForeign() != null) ? product.getDescInForeign() : "");
                    obj.put("itemgroup", (product.getItemGroup() != null) ? product.getItemGroup() : "");
                    obj.put("itempricelist", (product.getPriceList() != null) ? product.getPriceList() : "");
                    obj.put("shippingtype", (product.getShippingType() != null) ? product.getShippingType() : "");
                    obj.put("isActiveItem", product.isIsActive());
                    obj.put("isKnittingItem", product.isIsKnittingItem());
                    obj.put("isWastageApplicable", product.isWastageApplicable());
                    obj.put("wastageAccount", (product.getWastageAccount() != null) ? product.getWastageAccount().getID() : "");
                    obj.put("serviceTaxCode", (product.getServiceTaxCode() != null) ? product.getServiceTaxCode() : "");
                    obj.put("excisemethod", (product.getExcisemethodmain()));
                    obj.put("natureOfStockItem", (product.getNatureofStockItem()));
                    obj.put("excisemethodsubtype", (product.getExcisemethodsubtype()));
                    obj.put("exciserate", (product.getExciserate()));
                    obj.put("reportingSchemaType", product.getReportingSchemaType() != null ? (product.getReportingSchemaType().getID()) : "");
                    obj.put("vatabatementrate", (product.getVatAbatementRate()));
                    obj.put("vatabatementperiodfromdate", product.getVatAbatementPeriodFromDate());
                    obj.put("vatabatementperiodtodate", product.getVatAbatementPeriodToDate());
                    obj.put("vatMethodType", product.getVatMethodType());
                    obj.put("reportingUOMVAT", product.getReportinguomVAT());
                    obj.put("reportingSchemaVAT", product.getReportingSchemaTypeVAT() != null ? (product.getReportingSchemaTypeVAT().getID()) : "");
                    if (isForProductQuantityDetailsReport) {//This flag is true for both inventory-->"Stock details on Uom basis" and for "Product quantity details"

                        boolean isFromQuantityDetailsReport = false;
                        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isFromQuantityDetailsReport", ""))) {
                            isFromQuantityDetailsReport = Boolean.parseBoolean(paramJobj.getString("isFromQuantityDetailsReport"));//This flag is true from "Product quantity details" and false from inventory call
                        }
                        if (isFromQuantityDetailsReport) {//If true then run code of Block loose quantity details.

                            for (int i = 0; i < selectedUOMsArray.length; i++) {
                                // get quantity in this uom
                                String selectedUOM = selectedUOMsArray[i];
                                KwlReturnObject uomresult = accProductObj.getObject(UnitOfMeasure.class.getName(), selectedUOM);
                                UnitOfMeasure unitOfMeasure = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                if (unitOfMeasure != null) {
                                    KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), selectedUOM);
                                    double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                                    obj.put(selectedUOM, availableQuantity);
                                }

                            }

                        } else {//If false then run code for ERM-687
                            double newQuantity = 0.0;
                            for (int i = 0; i < selectedUOMsArray.length; i++) {
                                // get quantity in this uom
                                String selectedUOM = selectedUOMsArray[i];
                                KwlReturnObject uomresult = accProductObj.getObject(UnitOfMeasure.class.getName(), selectedUOM);
                                UnitOfMeasure unitOfMeasure = (UnitOfMeasure) uomresult.getEntityList().get(0);
                                UOMschemaType uOMschemaType = product.getUomSchemaType();
//                            KwlReturnObject baseQtyResult = accProductObj.getAvailableBaseQuantityInSelectedUOM(product.getID(), selectedUOM);
//                            double baseUOMQuantity = baseQtyResult.getEntityList().get(0) == null ? 0 : (Double) baseQtyResult.getEntityList().get(0);
                                if (!StringUtil.isNullObject(uOMschemaType)) {
                                    HashMap<String, Object> requestParamForUOMSchema = new HashMap<>();
                                    requestParamForUOMSchema.put("uomschematypeid", uOMschemaType.getID());
                                    requestParamForUOMSchema.put("currentuomid", selectedUOM);
                                    requestParamForUOMSchema.put("companyid", companyid);
                                    requestParamForUOMSchema.put("carryin", false);
                                    KwlReturnObject res = accProductObj.getProductBaseUOMRate(requestParamForUOMSchema);
                                    List uomSchemaList = res.getEntityList();
                                    Iterator itr = uomSchemaList.iterator();
                                    if (itr.hasNext()) {
                                        UOMSchema uOMSchema = (UOMSchema) itr.next();
                                        if (uOMSchema == null) {
                                            newQuantity = 0.0;
                                        } else {
                                            newQuantity = uOMSchema.getBaseuomrate() != 0 ? authHandler.roundQuantity((obj.optDouble("quantity", 0.0) / uOMSchema.getBaseuomrate()),companyid) : 0.0;
                                        }
                                    }
                                }
                                if (unitOfMeasure != null) {
//                                KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(product.getID(), selectedUOM);
//                                double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                                    if (product != null && product.getUnitOfMeasure() != null && unitOfMeasure.getID().equals(product.getUnitOfMeasure().getID())) {
                                        obj.put(selectedUOM, (obj.optDouble("quantity", 0.0)));
                                    } else {
                                        obj.put(selectedUOM, (newQuantity));
                                        newQuantity = 0.0;
                                    }
                                }

                            }
                        }
                    }
                    
                    obj.put("itemReusability", product.getItemReusability() != null ? product.getItemReusability().ordinal() : "");
                    obj.put("reusabilitycount", product.getReusabilityCount());
                    obj.put("valuationmethod", product.getValuationMethod() != null ? product.getValuationMethod() : "");
                    obj.put("licensetype", product.getLicenseType() != null ? product.getLicenseType() : LicenseType.NONE);
                    obj.put("licensecode", product.getLicenseCode() != null ? product.getLicenseCode() : "");
                    obj.put("itemissuecount", product.getTotalIssueCount());
                    obj.put("customercategory", (product.getCustomerCategory() != null) ? product.getCustomerCategory() : "");
                    obj.put("inspectionTemplate", (product.getInspectionTemplate() != null) ? product.getInspectionTemplate().getId() : "");
                    obj.put("substituteProductId", (product.getSubstituteProduct() != null) ? product.getSubstituteProduct().getID() : "");
                    obj.put("substituteProductName", (product.getSubstituteProduct() != null) ? product.getSubstituteProduct().getName() : "");
                    obj.put("substituteQty", product.getSubstituteQty());
                    obj.put("productBrandId", (product.getProductBrand() != null) ? product.getProductBrand().getID() : "");
                    obj.put("productBrandName", (product.getProductBrand() != null) ? product.getProductBrand().getValue() : "");
                    if (product.isAsset()) {
                        obj.put("depreciationRate", product.getDepreciationRate());
                        obj.put("depreciationMethod", product.getDepreciationMethod());
                        obj.put("depreciationCostLimit", product.getDepreciationCostLimit());
                        obj.put("depreciationGL", (product.getDepreciationGLAccount() != null) ? product.getDepreciationGLAccount().getID() : "");
                        obj.put("provisionGL", (product.getDepreciationProvisionGLAccount() != null) ? product.getDepreciationProvisionGLAccount().getID() : "");
                        obj.put("assetSaleGL", (product.getSellAssetGLAccount() != null) ? product.getSellAssetGLAccount().getID() : "");
                        obj.put("writeoffassetaccount", (product.getWriteOffAssetAccount()!= null) ? product.getWriteOffAssetAccount().getID() : "");
                        obj.put("depreciationGLAccount", (product.getDepreciationGLAccount() != null) ? product.getDepreciationGLAccount().getID() : "");
                        obj.put("depreciationProvisionGLAccount", (product.getDepreciationProvisionGLAccount() != null) ? product.getDepreciationProvisionGLAccount().getID() : "");
                    }
                    // Purchase Tab fields

                    obj.put("catalogNo", (product.getCatalogNo() != null) ? product.getCatalogNo() : "");

                    if (purchaseuom != null) {
                        obj.put("purchaseuomid", (purchaseuom.getID() != null) ? purchaseuom.getID() : "");
                        obj.put("purchaseuomname", (purchaseuom.getID() != null) ? purchaseuom.getNameEmptyforNA() : "");
                    } else {
                        obj.put("purchaseuomid", "");
                    }

                    obj.put("itempurchaseheight", product.getItemPurchaseHeight());
                    obj.put("itempurchasewidth", product.getItemPurchaseWidth());
                    obj.put("itempurchaselength", product.getItemPurchaseLength());
                    obj.put("itempurchasevolume", product.getItemPurchaseVolume());
                    obj.put("purchasemfg", (product.getPurchaseMfg() != null) ? product.getPurchaseMfg() : "");


                    // Sales Tab fields
                    if (salesuom != null) {
                        obj.put("salesuomid", (salesuom.getID() != null) ? salesuom.getID() : "");
                        obj.put("salesuomname", (salesuom.getID() != null) ? salesuom.getNameEmptyforNA() : "");
                    } else {
                        obj.put("salesuomid", "");
                    }
                    obj.put("itemsalesheight", product.getItemSalesHeight());
                    obj.put("itemsaleswidth", product.getItemSalesWidth());
                    obj.put("itemsaleslength", product.getItemSalesLength());
                    obj.put("itemsalesvolume", product.getItemSalesVolume());

                    obj.put("alternateproductid", (product.getAlternateProduct() != null) ? product.getAlternateProduct() : "");

                    if (!StringUtil.isNullOrEmpty(product.getID())) {
                        List listObj = isProductUsedintransction(product.getID(), companyid, paramJobj, true); // true: Product & Services Report
                        isUsedInTransaction = (Boolean) listObj.get(0);    //always boolean value
                    }
                    obj.put("isUsedInTransaction", isUsedInTransaction);
                    isBatchForProduct = product.isIsBatchForProduct();
                    isSerialForProduct = product.isIsSerialForProduct();
                    if (!StringUtil.isNullOrEmpty(product.getID()) && (isSerialForProduct || isBatchForProduct)) {
                        isUsedInBatchSerial = isProductUsedinBatchSerialtransction(product.getID(), companyid);
                    }
                    obj.put("isUsedInBatchSerial", isUsedInBatchSerial);
                    //  Properties Tab fields
                    obj.put("itemheight", product.getItemHeight());
                    obj.put("itemwidth", product.getItemWidth());
                    obj.put("itemlength", product.getItemLength());
                    obj.put("itemvolume", product.getItemVolume());
                    obj.put("itemcolor", (product.getItemColor() != null) ? product.getItemColor() : "");

                    //  Remarks Tab fields
                    obj.put("additionalfreetext", (product.getAdditionalFreeText() != null) ? product.getAdditionalFreeText() : "");

                    //  Inventory Data Tab fields
                    Packaging packaging = product.getPackaging();
                    if (packaging != null && packaging.getId() != null) {
                        obj.put("casinguomid", (packaging.getCasingUoM() != null) ? packaging.getCasingUoM().getID() : "");
                        obj.put("casinguomvalue", (packaging.getCasingUomValue()));
                        obj.put("inneruomid", (packaging.getInnerUoM() != null) ? packaging.getInnerUoM().getID() : "");
                        obj.put("inneruomvalue", (packaging.getInnerUomValue()));
                        obj.put("stockuomid", (packaging.getStockUoM() != null) ? packaging.getStockUoM().getID() : "");
                        obj.put("stockuomvalue", (packaging.getStockUomValue()));
                        obj.put("packagingId", (packaging.getId() != null) ? packaging.getId() : "");
                        obj.put("packagingValue", (packaging.toString() != null) ? packaging.toString() : "");
                        obj.put(Constants.packaging, (packaging.toString() != null) ? packaging.toString() : "");
                    } else {
                        obj.put("casinguomid", "");
                        obj.put("casinguomvalue", 0);
                        obj.put("inneruomid", "");
                        obj.put("inneruomvalue", 0);
                        obj.put("stockuomid", "");
                        obj.put("stockuomvalue", 0);
                        obj.put("packagingId", "");
                        obj.put("packagingValue", "");
                        obj.put(Constants.packaging, "");
                    }

                    obj.put("stockadjustmentaccountid", (product.getStockAdjustmentAccount() != null ? product.getStockAdjustmentAccount().getID() : ""));
                    obj.put("inventoryaccountid", (product.getInventoryAccount() != null ? product.getInventoryAccount().getID() : ""));
                    obj.put("cogsaccountid", (product.getCostOfGoodsSoldAccount() != null ? product.getCostOfGoodsSoldAccount().getID() : ""));
                    obj.put("valuationmethod", product.getValuationMethod() != null ? product.getValuationMethod().ordinal() : 0);
                    //ERP-20637
                    Set<LandingCostCategory> lccCategoryidSet = product.getLccategoryid();
                    String landingCostCategoryId = "";
                    for (LandingCostCategory lccObj : lccCategoryidSet) {
                        if (!StringUtil.isNullOrEmpty(landingCostCategoryId)) {
                            landingCostCategoryId += ",";
                        }
                        landingCostCategoryId += lccObj.getId();
                    }

                    if (landingCostCategoryId != null) {
                        obj.put("landingcostcategoryid", landingCostCategoryId);
                    }

                    if (product.getOrderingUOM() != null) {
                        obj.put("orderinguomid", (product.getOrderingUOM().getID() != null) ? product.getOrderingUOM().getID() : "");
                        obj.put("orderinguomname", (product.getOrderingUOM().getID() != null) ? product.getOrderingUOM().getNameEmptyforNA() : "");
                    } else {
                        obj.put("orderinguomid", "");
                    }

                    if (product.getTransferUOM() != null) {
                        obj.put("transferuomid", (product.getTransferUOM().getID() != null) ? product.getTransferUOM().getID() : "");
                        obj.put("transferuomname", (product.getTransferUOM().getID() != null) ? product.getTransferUOM().getNameEmptyforNA() : "");
                    } else {
                        obj.put("transferuomid", "");
                    }

                    obj.put("WIPoffset", (product.getWIPOffset() != null) ? product.getWIPOffset() : "");
                    obj.put("Inventoryoffset", (product.getInventoryOffset() != null) ? product.getInventoryOffset() : "");

                    if (product.getVatcommoditycode() != null) {
                        obj.put("vatcommoditycode", product.getVatcommoditycode() != null ? product.getVatcommoditycode().getID() : "");
                    } else {
                        obj.put("vatcommoditycode", "");
                    }
                    obj.put("vatonmrp", product.isVatonmrp());
                    obj.put("mrprate", product.getMrprate());
                    obj.put("sac", product.getSAC());
                    obj.put("countable", product.isCountable());
                    obj.put("rcmapplicable", product.isRcmApplicable());
                    obj.put("itcaccountid", product.getItcAccount()!=null?product.getItcAccount().getID():"");
                    obj.put("itctype", product.getItcType());
                    Set<Frequency> frequencies = product.getCycleCountFrequencies();

                    if (frequencies != null) {
                        String fqs = "";
                        boolean first = true;
                        for (Frequency frequency : frequencies) {
                            if (first) {
                                fqs = String.valueOf(frequency.getId());
                                first = false;
                            } else {
                                fqs += "," + String.valueOf(frequency.getId());
                            }
                        }
                        obj.put("CCFrequency", fqs);
                    }
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    AccProductCustomData jeDetailCustom = (AccProductCustomData) product.getProductCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                            if (customFieldMap.containsKey(varEntry.getKey())) {
                                String value = "";
                                String fieldId = "";
                                String Ids[] = coldata.split(",");
                                for (int i = 0; i < Ids.length; i++) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                    if (fieldComboData != null) {
                                        if (fieldComboData.getField().getFieldtype() == 12) {
                                            value += fieldComboData.getValue() != null ? fieldComboData.getId() + "," : ",";
                                        } else {
                                            value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                            fieldId = fieldComboData.getId();
                                        }
                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(value)) {
                                    value = value.substring(0, value.length() - 1);
                                }
                                if (isdefaultHeaderMap) { //Used for Android Services
                                    obj.put(varEntry.getKey(), coldata);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                    obj.put(varEntry.getKey() + "Value", value);
                                } else {
                                obj.put(varEntry.getKey(), value);
                                }
                                if (isProductView) {
                                    obj.put(varEntry.getKey() + "_value", fieldId);
                                }

                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                obj.put(varEntry.getKey(), (coldata));
                            } else {
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    obj.put(varEntry.getKey(), coldata);
                                }
                            }
                        }
                    }
                    if (product.isActivateProductComposition()) {
                        JSONArray productCompositionArr = new JSONArray();
                        HashMap<String, Object> doRequestParams = new HashMap<String, Object>();
                        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();//, order_by = new ArrayList(), order_type = new ArrayList();
                        filter_names.add("product.ID");
                        doRequestParams.put("filter_names", filter_names);
                        doRequestParams.put("filter_params", filter_params);
                        filter_params.clear();
                        filter_params.add(product.getID());
                        KwlReturnObject productCompositionRes = accProductObj.getProductCompositionDetails(doRequestParams);
                        List<ProductComposition> prodcomplist = productCompositionRes.getEntityList();
                        for (ProductComposition pc : prodcomplist) {
                            JSONObject obj3 = new JSONObject();
                            obj3.put("id", pc.getID());
                            obj3.put("srno", pc.getSrno());
                            obj3.put("ingredients", pc.getIngredients());
                            obj3.put("strength", pc.getStrength());
                            productCompositionArr.put(obj3);
                        }
                        obj.put("productCompositionDetails", productCompositionArr);
                    }

                    /*
                     * Fetching product terms mapped at the time of edit product
                     */
//                    companyid = (String) requestParams.get("companyid");
//                    KwlReturnObject extracompanyObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
//                    ExtraCompanyPreferences extraCompanyPref = (ExtraCompanyPreferences) extracompanyObj.getEntityList().get(0);
//
//                    if (extraCompanyPref.getLineLevelTermFlag() == 1) { // For India Country 
//
//                        JSONArray ProductTermPurchaseArr = new JSONArray();
//                        JSONArray ProductTermSalesArr = new JSONArray();
//                        JSONArray ProductTermAdditionPurchaseArr = new JSONArray();
//                        JSONArray ProductTermAdditionSalesArr = new JSONArray();
//
//                        HashMap<String, Object> hashMap = new HashMap<String, Object>();
//                        hashMap.put("salesOrPurchase", false);//For Purchase
//                        hashMap.put(Constants.productid, product.getID());
//                        hashMap.put("isAdditional", false);
//                        ProductTermPurchaseArr = callProductTermMapJSONArray(hashMap);
//                        hashMap.remove("isAdditional");
//                        hashMap.put("isAdditional", true);
//                        ProductTermAdditionPurchaseArr = callProductTermMapJSONArray(hashMap);
//                        hashMap.remove("isAdditional");
//                        hashMap.put("isAdditional", false);
//                        hashMap.remove("salesOrPurchase");//For Sales
//                        hashMap.put("salesOrPurchase", true);//For Sales
//                        ProductTermSalesArr = callProductTermMapJSONArray(hashMap);
//                        hashMap.remove("isAdditional");
//                        hashMap.put("isAdditional", true);
//                        ProductTermAdditionSalesArr = callProductTermMapJSONArray(hashMap);
//                        obj.put("ProductTermPurchaseMapp", ProductTermPurchaseArr);
//                        obj.put("ProductTermSalesMapp", ProductTermSalesArr);
//                        obj.put("ProductTermAdditionalPurchaseMapp", ProductTermAdditionPurchaseArr);
//                        obj.put("ProductTermAdditionalSalesMapp", ProductTermAdditionSalesArr);
//                    }
                     /**
                    * If product has landing cost category and its being used in a purchase invoice then not allow to edit landing cost category .
                    */
                    if (extraCompanyPreferences.isActivelandingcostofitem() && product.getLccategoryid() != null && !product.getLccategoryid().isEmpty()) {
                        KwlReturnObject landedkwl = accProductObj.getLandedInvoiceListForProduct(product.getID(), companyid);
                        List<String> landedinvlist = landedkwl.getEntityList();
                        if (landedinvlist!=null && !landedinvlist.isEmpty()) {
                            obj.put("landingcostcategoryusedintransaction", true);
                        } else {
                            obj.put("landingcostcategoryusedintransaction", false);
                        }
                    } else {
                        obj.put("landingcostcategoryusedintransaction", false);
                    }
                    // End new properties
                    if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                        // Do Nothing
                    } else {
                        jArr.put(obj);
                    }
                    String limit = paramJobj.optString("limit");
                    if (!StringUtil.isNullOrEmpty(limit)) {
                        if (jArr.length() == Integer.parseInt(limit)) {
//                            break;
                        }
                    }
                } catch (UnsupportedEncodingException ex) {
                    throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
                } catch (Exception e) {
                    throw ServiceException.FAILURE("getProductsJson : " + e.getMessage(), e);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
        } finally {
            customFieldMap = null; //ERP-18753
            customDateFieldMap = null;
            fieldrequestParams = null;
            replaceFieldMap = null;
            FieldMap = null;
            requestParams = null;
        }
        return jArr;
    }

@Override
    public JSONArray callProductTermMapJSONArray(HashMap<String, Object> data) {
        JSONArray productData = new JSONArray();
        try {
            KwlReturnObject result = accProductObj.getProductTermDetails(data);
            ArrayList<ProductTermsMap> ProductTermPurchaselist = (ArrayList<ProductTermsMap>) result.getEntityList();
            for (ProductTermsMap mt : ProductTermPurchaselist) {
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm().getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("accountid", mt.getAccount().getID());
                jsonobj.put("glaccountname", !StringUtil.isNullOrEmpty(mt.getAccount().getName()) ? mt.getAccount().getName() : "");
                jsonobj.put("accode", !StringUtil.isNullOrEmpty(mt.getAccount().getAcccode()) ? mt.getAccount().getAcccode() : "");
                jsonobj.put("sign", mt.getTerm().getSign());
                jsonobj.put("formula", mt.getTerm().getFormula());
                jsonobj.put("isDefault", mt.isIsDefault());
                jsonobj.put("IsOtherTermTaxable", mt.getTerm().isOtherTermTaxable());
                jsonobj.put("termid", mt.getTerm().getId());
                jsonobj.put("formulaids", mt.getTerm().getFormula());
                jsonobj.put("termpercentage", mt.getPercentage());
                jsonobj.put("termtype", mt.getTerm().getTermType());
                jsonobj.put("termsequence", mt.getTerm().getTermSequence() + "");
                jsonobj.put(Constants.productid, mt.getProduct().getID());
                jsonobj.put("producttermmapid", mt.getId());
                jsonobj.put("purchasevalueorsalevalue", mt.getPurchaseValueOrSaleValue());
                jsonobj.put("deductionorabatementpercent", mt.getDeductionOrAbatementPercent());
                jsonobj.put("formType", !StringUtil.isNullOrEmpty(mt.getTerm().getFormType()) ? mt.getTerm().getFormType() : "1"); // 1 for without form
                jsonobj.put("taxtype", mt.getTaxType());
//                    jsonobj.put("taxvalue",  mt.getTaxType()==0 ? mt.getTermAmount() : mt.getPercentage());
                jsonobj.put("isTermTaxable", mt.getTerm().isOtherTermTaxable());
                jsonobj.put("taxvalue", mt.getPercentage());
                productData.put(jsonobj);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return productData;
    }
    
 @Override
    public List isProductUsedintransction(String productid, String companyid, JSONObject paramJobj, boolean isProductAndServices) throws SessionExpiredException, AccountingException, ServiceException {
        ArrayList listObj = new ArrayList();
        String modulesProductUsedIn = "";
        boolean isusedinTransaction = false;

        boolean unBuild = Boolean.parseBoolean(paramJobj.optString("unBuild",""));
        try {

            KwlReturnObject result = accProductObj.getPO_Product(productid, companyid); //Is used in Purchase Order ?
            BigInteger bigInteger1 = (BigInteger) result.getEntityList().get(0);
            int count1 = 0;
            if (bigInteger1.intValue() > 0) {
                count1 = bigInteger1.intValue();
            }
            modulesProductUsedIn += count1 > 0 ? "Purchase Order, " : "";
            if (isProductAndServices && count1 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getSO_Product(productid, companyid);  // Is used in Sales Order ?
            BigInteger bigInteger2 = (BigInteger) result.getEntityList().get(0);
            int count2 = 0;
            if (bigInteger2.intValue() > 0) {
                count2 = bigInteger2.intValue();
            }
            modulesProductUsedIn += count2 > 0 ? "Sales Order, " : "";
            if (isProductAndServices && count2 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getGoodsReceipt_Product(productid, companyid); // Is Used in Vendor Invoice?
            BigInteger bigInteger3 = (BigInteger) result.getEntityList().get(0);
            int count3 = 0;
            if (bigInteger3.intValue() > 0) {
                count3 = bigInteger3.intValue();
            }
            modulesProductUsedIn += count3 > 0 ? "Vendor Invoice, " : "";
            if (isProductAndServices && count3 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getInvoice_Product(productid, companyid);  // Is used in Customer Invoice?
            BigInteger bigInteger4 = (BigInteger) result.getEntityList().get(0);
            int count4 = 0;
            if (bigInteger4.intValue() > 0) {
                count4 = bigInteger4.intValue();
            }
            modulesProductUsedIn += count4 > 0 ? "Customer Invoice, " : "";
            if (isProductAndServices && count4 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.checkSubProductforAssembly(productid); //Is used in Assembly Product? 
            BigInteger bigInteger5 = (BigInteger) result.getEntityList().get(0);
            int count5 = 0;
            if (bigInteger5.intValue() > 0) {
                count5 = bigInteger5.intValue();
            }
            modulesProductUsedIn += count5 > 0 ? "Assembly Product, " : "";
            if (isProductAndServices && count5 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getGR_Product(productid, companyid); // Is used in Goods Receipt
            BigInteger bigInteger7 = (BigInteger) result.getEntityList().get(0);
            int count7 = 0;
            if (bigInteger7.intValue() > 0) {
                count7 = bigInteger7.intValue();
            }
            modulesProductUsedIn += count7 > 0 ? "Goods Receipt, " : "";
            if (isProductAndServices && count7 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getDO_Product(productid, companyid); //Is used in Delivery Order?
            BigInteger bigInteger8 = (BigInteger) result.getEntityList().get(0);
            int count8 = 0;
            if (bigInteger8.intValue() > 0) {
                count8 = bigInteger8.intValue();
            }
            modulesProductUsedIn += count8 > 0 ? "Delivery Order, " : "";
            if (isProductAndServices && count8 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getPR_Product(productid, companyid); //Is used in Purchase Requisition?
            BigInteger bigInteger9 = (BigInteger) result.getEntityList().get(0);
            int count9 = 0;
            if (bigInteger9.intValue() > 0) {
                count9 = bigInteger9.intValue();
            }
            modulesProductUsedIn += count9 > 0 ? "Purchase Requisition, " : "";
            if (isProductAndServices && count9 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getVQ_Product(productid, companyid); // Is used in Vendor Quotation
            BigInteger bigInteger10 = (BigInteger) result.getEntityList().get(0);
            int count10 = 0;
            if (bigInteger10.intValue() > 0) {
                count10 = bigInteger10.intValue();
            }
            modulesProductUsedIn += count10 > 0 ? "Vendor Quotation, " : "";
            if (isProductAndServices && count10 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getCQ_Product(productid, companyid); //Is used in Customer Quotation
            BigInteger bigInteger11 = (BigInteger) result.getEntityList().get(0);
            int count11 = 0;
            if (bigInteger11.intValue() > 0) {
                count11 = bigInteger11.intValue();
            }
            modulesProductUsedIn += count11 > 0 ? "Customer Quotation, " : "";
            if (isProductAndServices && count11 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getPurchaseReturn_Product(productid, companyid); //Is used in Purchase Return?
            BigInteger bigInteger12 = (BigInteger) result.getEntityList().get(0);
            int count12 = 0;
            if (bigInteger12.intValue() > 0) {
                count12 = bigInteger12.intValue();
            }
            modulesProductUsedIn += count12 > 0 ? "Purchase Return, " : "";
            if (isProductAndServices && count12 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            result = accProductObj.getSalesReturn_Product(productid, companyid); //Is used in Sales Return?
            BigInteger bigInteger13 = (BigInteger) result.getEntityList().get(0);
            int count13 = 0;
            if (bigInteger13.intValue() > 0) {
                count13 = bigInteger13.intValue();
            }
            modulesProductUsedIn += count13 > 0 ? "Sales Return, " : "";
            if (isProductAndServices && count13 > 0) {
                listObj.add(true);
                listObj.add(modulesProductUsedIn);
                return listObj;
            }
            
            result = accProductObj.getInventoryTransactions(productid, companyid); // Is used in inventory transaction.
            List list14 = result.getEntityList();
            int count14 = list14.size();

            if (count1 > 0 || count2 > 0 || count3 > 0 || count4 > 0 || count7 > 0 || count8 > 0 || count9 > 0 || count10 > 0 || count11 > 0 || count12 > 0 || count13 > 0 || count14 > 0) {
                isusedinTransaction = true;
            }

            if (count5 > 0) {
                isusedinTransaction = true;
            }
            //Delete product from All Assemblies
            KwlReturnObject kwlReturnObject_SPA = accProductObj.selectSubProductFromBuildAssembly(productid);
            BigInteger bigInteger15 = (BigInteger) kwlReturnObject_SPA.getEntityList().get(0);
            int count15 = 0;
            if (bigInteger15.intValue() > 0) {
                count15 = bigInteger15.intValue();
            }

            result = accProductObj.checkIfParentProduct(productid);
            BigInteger bigInteger6 = (BigInteger) result.getEntityList().get(0);
            int count6 = 0;
            if (bigInteger6.intValue() > 0) {
                count6 = bigInteger6.intValue();
            }
            if ((count6 > 0 || count15 > 0) && !unBuild) {
                isusedinTransaction = true;
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("AccProductModuleServiceImpl.isProductUsedintransction() -: " + ex.getMessage(), ex);
        }
        listObj.add(isusedinTransaction);
        listObj.add(modulesProductUsedIn);
        return listObj;
    } 
    
    
    /*
    Below method is used to create disposal invoice data map
    */
    @Override
    public Map<String, Object> getDisposalInvoiceDetailsFromAssetDetailID(Map<String, Object> request) throws ServiceException {
        KwlReturnObject resultList = null;
        List<Object[]> dataList = null;
        List tempList = null;
        Map<String, Object> dataMap = new HashMap<>();
        String assetId = "";
        try {
            resultList = accProductObj.getDisposalInvoiceDetailsFromAssetDetailID(request);
            dataList = resultList.getEntityList();
            if (dataList != null && dataList.size() > 0) {
                for (Object[] dataArray : dataList) {
                    tempList = new ArrayList();
                    tempList.add(dataArray[0]); //get Asset ID
                    assetId = (String) dataArray[0];
                    tempList.add(dataArray[1]); //get invoice number
                    tempList.add(dataArray[2]); //get Invoice date
                    tempList.add(dataArray[3]); //get Disposal Invoice JE ID
                    tempList.add(dataArray[4]); //get Disposal Invoice JE Number
                    dataMap.put(assetId, tempList);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataMap;
    }
   
    
    
    
    private String getPriceListBandIDByName(String priceListBandName, String companyID) throws AccountingException {
        String priceListBandID = "";
        try {
            if (!StringUtil.isNullOrEmpty(priceListBandName) && !StringUtil.isNullOrEmpty(companyID)) {
                KwlReturnObject retObj = accProductObj.getPriceListBandIDByName(priceListBandName, companyID);
                List list = retObj.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    priceListBandID = (String) itr.next();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Price List - Band");
        }
        return priceListBandID;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    private void savePriceListData(int cnt, String companyid, String currencyId, boolean isCurrencyCode, String[] recarr, HashMap currencyMap, HashMap<String, Integer> columnConfig, HashMap<String, Object> requestParams, MutableInt total, MutableInt failed, Locale locale, StringBuilder failedRecords, DateFormat df) {
        if (cnt != 0) {
            try {
                Product product = null;
                if (columnConfig.containsKey(Constants.productid)) {
                    String productID = recarr[(Integer) columnConfig.get(Constants.productid)].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(productID)) {                       
                        product = getProductByProductID(productID, companyid);

                        if (product == null) {
                            throw new AccountingException("Product ID is not found for " + productID);
                        }
                    } else {
                        throw new AccountingException("Product ID is not available.");
                    }
                } else {
                    throw new AccountingException("Product ID column is not found.");
                }

                String priceListBandID = "";
                if (columnConfig.containsKey("priceListBand")) {
                    String priceListBandName = recarr[(Integer) columnConfig.get("priceListBand")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(priceListBandName)) {
                        priceListBandID = getPriceListBandIDByName(priceListBandName, companyid);

                        if (StringUtil.isNullOrEmpty(priceListBandID)) {
                            throw new AccountingException("Price List - Band is not found for " + priceListBandName);
                        }
                    } else {
                        throw new AccountingException("Price List - Band is not available.");
                    }
                } else {
                    throw new AccountingException("Price List - Band column is not found.");
                }

                String productPurchasePrice = "";
                if (columnConfig.containsKey("purchasePrice")) {
                    productPurchasePrice = recarr[(Integer) columnConfig.get("purchasePrice")].replaceAll("\"", "").trim();
                    if (StringUtil.isNullOrEmpty(productPurchasePrice)) {
                        throw new AccountingException("Product Purchase Price is not available");
                    }
                } else {
                    throw new AccountingException("Purchase Price column is not found.");
                }

                String productSalesPrice = "";
                if (columnConfig.containsKey("salesPrice")) {
                    productSalesPrice = recarr[(Integer) columnConfig.get("salesPrice")].replaceAll("\"", "").trim();

                    if (StringUtil.isNullOrEmpty(productSalesPrice)) {
                        throw new AccountingException("Product Sales Price is not available");
                    }
                } else {
                    throw new AccountingException("Sales Price column is not found.");
                }

                if (isCurrencyCode ? columnConfig.containsKey("currencyCode") : columnConfig.containsKey("currencyName")) {
                    String productPriceCurrencyStr = isCurrencyCode ? recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim() : recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(productPriceCurrencyStr)) {
                        currencyId = getCurrencyId(productPriceCurrencyStr, currencyMap);

                        if (StringUtil.isNullOrEmpty(currencyId)) {
                            throw new AccountingException(messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, locale));
                        }
                    } else {
                        throw new AccountingException("Currency is not available.");
                    }
                }

                Date applicableDate = null;
                if (columnConfig.containsKey("applicableDate")) {
                    String applicableDateStr = recarr[(Integer) columnConfig.get("applicableDate")].replaceAll("\"", "").trim();

                    if (StringUtil.isNullOrEmpty(applicableDateStr)) {
                        throw new AccountingException("Applicable Date is not available");
                    } else {
                        applicableDate = df.parse(applicableDateStr);
                    }
                } else {
                    throw new AccountingException("Applicable Date column is not found.");
                }

                        // For save Purchase and Sales Price
                requestParams.put("pricingBandMasterID", priceListBandID);
                requestParams.put("purchasePrice", productPurchasePrice);
                requestParams.put("salesPrice", productSalesPrice);
                requestParams.put("currencyID", currencyId);
                requestParams.put("productID", product.getID());
                requestParams.put("companyID", companyid);
                requestParams.put("applicableDate", applicableDate);
                requestParams.put("isSavePricingBandMasterDetails", true);

                KwlReturnObject result = accMasterItemsDAOobj.getPriceOfProductForPricingBandAndCurrency(requestParams);

                if (result.getEntityList() != null && !result.getEntityList().isEmpty()) { // for edit case
                    Object[] priceObj = (Object[]) result.getEntityList().get(0);
                    requestParams.put("rowid", priceObj[2]);
                }

                accMasterItemsDAOobj.saveOrUpdatePricingBandMasterDetails(requestParams);
            } catch (Exception ex) {
                failed.increment();
                String errorMsg = ex.getMessage(), invalidColumns = "";
                try {
                    JSONObject errorLog = new JSONObject(errorMsg);
                    errorMsg = errorLog.getString("errorMsg");
                    invalidColumns = errorLog.getString("invalidColumns");
                } catch (JSONException jex) {
                }
                failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
            }
            total.increment();
        }
    }

    
    public JSONObject importPriceListBandPriceRecords(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("import_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//        TransactionStatus status = txnManager.getTransaction(def);
//        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        MutableInt total= new MutableInt(0), failed = new MutableInt(0);
        int failedInt=0, totalInt = 0;
        
        String currencyId = sessionHandlerImpl.getCurrencyID(request);
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("filename");
        String masterPreference = request.getParameter("masterPreference");

        JSONObject returnObj = new JSONObject();

        try {
            String dateFormat = null, dateFormatId = request.getParameter("dateFormat");
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
            boolean isCurrencyCode=extraPref.isCurrencyCode();
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }

            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
//            sdf.setTimeZone(TimeZone.getTimeZone("GMT" + sessionHandlerImpl.getTimeZoneDifference(request)));
            DateFormat df = sdf;
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);

                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

            HashMap currencyMap = getCurrencyMap(isCurrencyCode);
            currencyId = sessionHandlerImpl.getCurrencyID(request);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            Locale locale = RequestContextUtils.getLocale(request);

            while ((record = br.readLine()) != null) {
                String[] recarr = record.split(",");
                if (cnt == 0) {
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");
                }
                savePriceListData(cnt, companyid, currencyId, isCurrencyCode, recarr, currencyMap, columnConfig, requestParams, total, failed, locale, failedRecords, df);
                cnt++;
            }
            failedInt = failed.intValue();
            totalInt = total.intValue();
            if (failedInt > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = totalInt - failedInt;
            if (totalInt == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
//                issuccess = false;
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == totalInt) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failedInt == 0 ? "." : messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failedInt + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failedInt > 1 ? "s" : "") + ".");
            }

//            try {
//                txnManager.commit(status);
//            } catch (Exception ex) {
//                commitedEx = true;
//                throw ex;
//            }
        } catch (Exception ex) {
//            if (!commitedEx) { // if exception occurs during commit then dont call rollback
//                txnManager.rollback(status);
//            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

//            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
//            ldef.setName("import_Tx");
//            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                // Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failedInt>0?"csv":"");
                logDataMap.put("TotalRecs", totalInt);
                logDataMap.put("Rejected", failedInt);
                logDataMap.put("Module", Constants.Acc_Price_List_Band_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

//                txnManager.commit(lstatus);
            } catch (Exception ex) {
//                txnManager.rollback(lstatus);
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put(Constants.RES_success, issuccess);
                returnObj.put(Constants.RES_msg, msg);
                returnObj.put("totalrecords", totalInt);
                returnObj.put("successrecords", totalInt - failedInt);
                returnObj.put("failedrecords", failedInt);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
  
    /**
     * this method processes the batch-serial details Json and 
     * converts it into format required for further processing
     * @param batchJSON
     * @param productId
     * @param paramJobj
     * @return
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ServiceException 
     */
    @Override
    public JSONObject manipulateBatchDetailsforMobileApps(String batchJSON, String productId, JSONObject paramJobj) throws JSONException, SessionExpiredException, ServiceException {
        JSONObject returnjobj = new JSONObject();
        String modifiedbatchdetailsJSON = null;
        Set<String> warehouseSet = new HashSet<String>();
        Set<String> locationSet = new HashSet<String>();
        Set<String> batchSet = new HashSet<String>();

        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isBatchForProduct = false;
        DateFormat df = authHandler.getDateOnlyFormat();

        if (!StringUtil.isNullOrEmpty(productId)) {
            KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), productId);
            Product product = (Product) prodresult.getEntityList().get(0);
            isLocationForProduct = product.isIslocationforproduct();
            isWarehouseForProduct = product.isIswarehouseforproduct();
            isBatchForProduct = product.isIsBatchForProduct();
        }
        String batchKey = "purchasebatchid";
        String serialKey = "purchaseserialid";
        if (StringUtil.equal(paramJobj.optString(Constants.moduleid), String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
            /**
             * For GoodsReceiptOrder we use below keys for batchDetails manipulation because purchasebatchid and purchaseserialid are not always present
             */
            batchKey = "batch";
            serialKey = "serialno";
        }
        JSONArray jArr = new JSONArray(batchJSON);
        try {
            for (int cnt = 0; cnt < jArr.length(); cnt++) {
                JSONObject jobj = jArr.getJSONObject(cnt);

                if (jobj.has(batchKey) && jobj.get(batchKey) != null && !StringUtil.isNullOrEmpty(jobj.optString(batchKey, null))) {
                    if (batchSet.contains(jobj.optString(batchKey, null))) {
                    } else {
                        batchSet.add(jobj.optString(batchKey));
                    }
                }
            }

            Map<String, List> batchMap = new HashMap();
            for (String batch : batchSet) {
                List seriallist = new ArrayList();
                for (int cnt1 = 0; cnt1 < jArr.length(); cnt1++) {
                    JSONObject jobj = jArr.getJSONObject(cnt1);

                    if (jobj.has(serialKey) && jobj.get(serialKey) != null && !StringUtil.isNullOrEmpty(jobj.optString(serialKey, null)) && batch.equalsIgnoreCase(jobj.optString(batchKey, null))) {
                        seriallist.add(jobj.optString(serialKey));
                    }
                }
                batchMap.put(batch, seriallist);
            }

            JSONArray jArray = new JSONArray();
            Set<String> batchSet1 = new HashSet<String>();
            for (int cnt2 = 0; cnt2 < jArr.length(); cnt2++) {
                JSONObject jobj = jArr.getJSONObject(cnt2);
                boolean isnewbatch = false;
                if (!StringUtil.isNullOrEmpty(jobj.optString(batchKey, null)) && isBatchForProduct) {
                    if (batchSet1.contains(jobj.optString(batchKey, ""))) {
                        jobj.put(batchKey, "");
                        jobj.put("quantity", "");
                        isnewbatch = false;
                    } else {
                        batchSet1.add(jobj.optString(batchKey));
                        List<String> seriallist = (List<String>) batchMap.get(jobj.optString(batchKey));
                        jobj.put("quantity", seriallist.size());
                        isnewbatch = true;
                    }
                }

                if (!StringUtil.isNullOrEmpty(jobj.optString("warehouse", null)) && isWarehouseForProduct) {
                    if (warehouseSet.contains(jobj.optString("warehouse", "")) && !isnewbatch) {
                        jobj.put("warehouse", "");
                        jobj.put("quantity", "");
                    } else {
                        warehouseSet.add(jobj.optString("warehouse"));
                    }
                }

                if (!StringUtil.isNullOrEmpty(jobj.optString("location", null)) && isLocationForProduct) {
                    if (locationSet.contains(jobj.optString("location", "")) && !isnewbatch) {
                        jobj.put("location", "");
                        jobj.put("quantity", "");
                    } else {
                        locationSet.add(jobj.optString("location"));
                    }
                }
                jArray.put(jobj);
            }
            modifiedbatchdetailsJSON = jArray.toString();

        } catch (Exception ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            returnjobj.put("batchdetails", modifiedbatchdetailsJSON);
        }

        return returnjobj;
    }
    
    
    @Override
    public Map<String, Object> validateBatchSerialDetail(Map<String, Object> requestMap) {
        Map<String, Object> returnMap = new HashMap<>();
        try {          
            boolean isGenerateGoodsReceipt=false;
            Product product=null;
            HashMap<String, Integer> columnConfig=new HashMap<>();
            String[] recarr=null;
            String companyID=null;
            boolean isSalesTransaction=false;
            String failureMsg = "";
            String masterPreference="";
            DateFormat df=null;
            String dateFormat = null;
            
           if(requestMap.containsKey("isGenerateGoodsReceipt") && requestMap.get("isGenerateGoodsReceipt")!= null){
               isGenerateGoodsReceipt=(boolean)requestMap.get("isGenerateGoodsReceipt");
           } 
           if(requestMap.containsKey("isSalesTransaction") && requestMap.get("isSalesTransaction")!= null){
               isSalesTransaction=(boolean)requestMap.get("isSalesTransaction");
           }  
           if(requestMap.containsKey("product") && requestMap.get("product")!= null){
               product=(Product)requestMap.get("product");
           }  
           if(requestMap.containsKey("columnConfig") && requestMap.get("columnConfig")!= null){
               columnConfig=(HashMap<String, Integer>)requestMap.get("columnConfig");
           }  
           if(requestMap.containsKey("recarr") && requestMap.get("recarr")!= null){
               recarr=(String [])requestMap.get("recarr");
           }  
           if(requestMap.containsKey("companyID") && requestMap.get("companyID")!= null){
               companyID=(String)requestMap.get("companyID");
           }  
           if(requestMap.containsKey("failureMsg") && requestMap.get("failureMsg")!= null){
               failureMsg=(String )requestMap.get("failureMsg");
           }  
           if(requestMap.containsKey("masterPreference") && requestMap.get("masterPreference")!= null){
               masterPreference=(String)requestMap.get("masterPreference");
           }  
           if(requestMap.containsKey("df") && requestMap.get("df")!= null){
               df=(DateFormat)requestMap.get("df");
           }  
          
            
            boolean isBatch = false;
            boolean isserial = false;
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;
            String warehouse = null;
            String location = null;
            double batchquantity = 0;
            String serialName = "";
            String batchName = "";
            String rowName = "";
            String rackName = "";
            String binName = "";

            InventoryWarehouse warehouseObj = null;
            InventoryLocation locationObj = null;
            NewProductBatch productBatchObj=null;
            NewBatchSerial newBatchSerial=null;
            StoreMaster rowObj = null;
            StoreMaster rackObj = null;
            StoreMaster binObj = null;

            if (product != null) {
                isWarehouseForProduct = product.isIswarehouseforproduct();
                isLocationForProduct = product.isIslocationforproduct();
                isBatchForProduct = product.isIsBatchForProduct();
                isSerialForProduct = product.isIsSerialForProduct();
                isRowForProduct = product.isIsrowforproduct();
                isRackForProduct = product.isIsrackforproduct();
                isBinForProduct = product.isIsbinforproduct();

                if (isWarehouseForProduct && columnConfig.containsKey("warehouse")) {
                    warehouse = recarr[(Integer) columnConfig.get("warehouse")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(warehouse)) {
                        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                        filter_names.add("company.companyID");
                        filter_params.add(companyID);
                        filter_names.add("name");
                        filter_params.add(warehouse);
                        filterRequestParams.put("filter_names", filter_names);
                        filterRequestParams.put("filter_params", filter_params);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.getWarehouseItems(filterRequestParams);
                        List list = cntResult.getEntityList();
                        if (list.isEmpty()) {
                            if (!masterPreference.equalsIgnoreCase("1")) {
                                failureMsg += "Warehouse is not found for " + warehouse + ". ";
                            }
                        } else {
                            warehouseObj = cntResult.getEntityList().get(0) != null ? (InventoryWarehouse) cntResult.getEntityList().get(0) : null;
                        }

                    } else {
                        if (!masterPreference.equalsIgnoreCase("1")) {
                            failureMsg += "Product Warehouse is not available. ";
                        }
                    }
                }
                if (isLocationForProduct && columnConfig.containsKey("location")) {
                    location = recarr[(Integer) columnConfig.get("location")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(location)) {
                        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                        filter_names.add("company.companyID");
                        filter_params.add(companyID);
                        filter_names.add("name");
                        filter_params.add(location);
                        filterRequestParams.put("filter_names", filter_names);
                        filterRequestParams.put("filter_params", filter_params);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.getLocationItems(filterRequestParams);
                        List list = cntResult.getEntityList();
                        if (list.isEmpty()) {
                            if (!masterPreference.equalsIgnoreCase("1")) {
                                failureMsg += "Location is not found for " + location + ". ";
                            }
                        } else {
                            locationObj = cntResult.getEntityList().get(0) != null ? (InventoryLocation) cntResult.getEntityList().get(0) : null;
                        }
                    } else {
                        if (!masterPreference.equalsIgnoreCase("1")) {
                            failureMsg += "Product Location is not available. ";
                        }
                    }
                }


                if ((isBatchForProduct || isSalesTransaction) ) {
                    batchName = ( isBatchForProduct && columnConfig.containsKey("batch") ) ? recarr[(Integer) columnConfig.get("batch")].replaceAll("\"", "").trim(): "";
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                    filter_names.add("company.companyID");
                    filter_params.add(companyID);

                    if (locationObj != null && !StringUtil.isNullOrEmpty(locationObj.getId())) {
                        filter_names.add("location.id");
                        filter_params.add(locationObj.getId());
                    }

                    if (warehouseObj != null && !StringUtil.isNullOrEmpty(warehouseObj.getId())) {
                        filter_names.add("warehouse.id");
                        filter_params.add(warehouseObj.getId());
                    }
                    if (!StringUtil.isNullOrEmpty(product.getID())) {
                        filter_names.add("product");
                        filter_params.add(product.getID());
                    }
//                                    if (!StringUtil.isNullOrEmpty(paramJObj.optString("ispurchase", null))) {
//                                        filter_names.add("ispurchase");
//                                        filter_params.add(Boolean.parseBoolean(paramJObj.optString("ispurchase")));
//                                    }
                    if (!StringUtil.isNullOrEmpty(batchName)) {
                        filter_names.add("batchname");
                        filter_params.add(batchName);
                    }

                    order_by.add("name");
                    order_type.add("asc");
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    filterRequestParams.put("order_by", order_by);
                    filterRequestParams.put("order_type", order_type);
                    KwlReturnObject result = accMasterItemsDAOobj.getNewBatches(filterRequestParams, false, false);
                    productBatchObj =  ( result != null && !result.getEntityList().isEmpty() && result.getEntityList().get(0) != null ) ? (NewProductBatch) result.getEntityList().get(0) : null;

                    if (isBatchForProduct && StringUtil.isNullOrEmpty(batchName)) {
                        failureMsg += "Product ID '" + product.getProductid() + "' has batch activated. Please specify valid batch name. ";
                    }
                    if (isSalesTransaction && isBatchForProduct && productBatchObj == null) {
                        failureMsg += "Product Batch is not available for "+ batchName +". ";
                    }
                    
                }

                
                
                if (isSerialForProduct && columnConfig.containsKey("serial")) {
                    serialName = recarr[(Integer) columnConfig.get("serial")].replaceAll("\"", "").trim();
                    if (StringUtil.isNullOrEmpty(serialName)) {
                        failureMsg += "Product ID '" + product.getProductid() + "' has serial activated. Please specify valid serial number. ";
                    } else {
                        int duplicateCount = 0;
                        KwlReturnObject result=null;
                        if (productBatchObj != null) {
                            result = accMasterItemsDAOobj.checkDuplicateSerialforProduct(product.getID(), productBatchObj.getId(), serialName, companyID);
                            duplicateCount = result.getRecordTotalCount();
                        } else {
                            result = accMasterItemsDAOobj.checkDuplicateSerialforProduct(product.getID(), "", serialName, companyID);
                            duplicateCount = result.getRecordTotalCount();
                        }
                        if(!isSalesTransaction && duplicateCount > 0 ){
                            failureMsg += "Serial "+serialName +" is already exists. ";
                        }else if(isSalesTransaction && duplicateCount == 0){
                            failureMsg += "Serial "+serialName +" is not exists. ";
                        }else if(isSalesTransaction && duplicateCount ==1){
                            newBatchSerial= result.getEntityList().get(0) != null ? (NewBatchSerial) result.getEntityList().get(0) : null;
                        }

                    }
                } else if(isSerialForProduct){
                    failureMsg += "Product ID '" + product.getProductid() + "' has serial activated. Please specify valid serial number. ";
                }

                if (isRowForProduct && columnConfig.containsKey("row")) {
                    rowName = recarr[(Integer) columnConfig.get("row")].replaceAll("\"", "").trim();
                    if (StringUtil.isNullOrEmpty(rowName)) {
                        failureMsg += "Product Row is not available.";
                    } else {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put(Constants.companyKey, companyID);
                        dataMap.put("type", 1);  //Tpe of row is 1
                        dataMap.put("name", rowName);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.buildQueryForgetStoreMasters(dataMap);
                        List list = cntResult.getEntityList();
                        if (list.isEmpty()) {
                            if (!masterPreference.equalsIgnoreCase("1")) {
                                failureMsg += "Row is not found for " + rowName + ". ";
                            }
                        } else {
                            rowObj = cntResult.getEntityList().get(0) != null ? (StoreMaster) cntResult.getEntityList().get(0) : null;
                        }
                    }
                }
                if (isRackForProduct && columnConfig.containsKey("rack")) {
                    rackName = recarr[(Integer) columnConfig.get("rack")].replaceAll("\"", "").trim();
                    if (StringUtil.isNullOrEmpty(rackName)) {
                        failureMsg += "Product Rack is not available.";
                    } else {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put(Constants.companyKey, companyID);
                        dataMap.put("type", 2); // type for rack is 2
                        dataMap.put("name", rackName);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.buildQueryForgetStoreMasters(dataMap);
                        List list = cntResult.getEntityList();
                        if (list.isEmpty()) {
                            if (!masterPreference.equalsIgnoreCase("1")) {
                                failureMsg += "Rack is not found for " + rackName + ". ";
                            }
                        } else {
                            rackObj = cntResult.getEntityList().get(0) != null ? (StoreMaster) cntResult.getEntityList().get(0) : null;
                        }
                    }
                }
                if (isBinForProduct && columnConfig.containsKey("bin")) {
                    binName = recarr[(Integer) columnConfig.get("bin")].replaceAll("\"", "").trim();
                    if (StringUtil.isNullOrEmpty(binName)) {
                        failureMsg += "Product Bin is not available.";
                    } else {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put(Constants.companyKey, companyID);
                        dataMap.put("type", 3); //type for rack is 3
                        dataMap.put("name", binName);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.buildQueryForgetStoreMasters(dataMap);
                        List list = cntResult.getEntityList();
                        if (list.isEmpty()) {
                            if (!masterPreference.equalsIgnoreCase("1")) {
                                failureMsg += "Bin is not found for " + binName + ". ";
                            }
                        } else {
                            binObj = cntResult.getEntityList().get(0) != null ? (StoreMaster) cntResult.getEntityList().get(0) : null;
                        }
                    }
                }
                // Row , Rack and Bin name validation   

                String mfgdateStr="";
                if (columnConfig.containsKey("mfgdate")) {
                     mfgdateStr = recarr[(Integer) columnConfig.get("mfgdate")].replaceAll("\"", "").trim();
                     if (!StringUtil.isNullOrEmpty(mfgdateStr)) {
                        try {
                        df.parse(mfgdateStr);
                        } catch (Exception ex) {
                            failureMsg += "Incorrect date format for mfg Date, Please specify values in " + dateFormat + " format. ";
                        }
                     } 
                }

                String expdateStr="";
                if (columnConfig.containsKey("expdate")) {
                    expdateStr = recarr[(Integer) columnConfig.get("expdate")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(expdateStr)) {
                        try {
                            df.parse(expdateStr);
                        } catch (Exception ex) {
                            failureMsg += "Incorrect date format for expriy Date, Please specify values in " + dateFormat + " format. ";
                        }
                    }
                }
                
                String expstartStr="";
                if (columnConfig.containsKey("expstart")) {
                    expstartStr = recarr[(Integer) columnConfig.get("expstart")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(expstartStr)) {
                        try {
                            df.parse(expstartStr);
                        } catch (Exception ex) {
                            failureMsg += "Incorrect date format for mfg Date, Please specify values in " + dateFormat + " format. ";
                        }
                    } 
                }

                String expendStr="";
                if (columnConfig.containsKey("expend")) {
                    expendStr = recarr[(Integer) columnConfig.get("expend")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(expendStr)) {
                        try {
                            df.parse(expendStr);
                        } catch (Exception ex) {
                            failureMsg += "Incorrect date format for expriy Date, Please specify values in " + dateFormat + " format. ";
                        }
                    }
                }

                //Batch Quantity


                if (columnConfig.containsKey("batchquantity")) {
                    String quantityStr = recarr[(Integer) columnConfig.get("batchquantity")].replaceAll("\"", "").trim();
                    if (StringUtil.isNullOrEmpty(quantityStr)) {
                        if(!isSerialForProduct){
                            failureMsg += "Batch Quantity is not available. ";
                        }
                    } else {
                        try {
                            batchquantity = authHandler.roundQuantity(Double.parseDouble(quantityStr), companyID);
                        } catch (Exception ex) {
                            failureMsg += "Incorrect numeric value for Batch Quantity, Please ensure that value type of Batch Quantity matches with the Quantity. ";
                        }
                    }
                } else if (isWarehouseForProduct || isLocationForProduct || isBatchForProduct) {
                    failureMsg += "Batch Quantity column is not found. ";
                }
                returnMap.put("isWarehouseForProduct", isWarehouseForProduct);
                returnMap.put("isLocationForProduct", isLocationForProduct);
                returnMap.put("isBatchForProduct", isBatchForProduct);
                returnMap.put("isSerialForProduct", isSerialForProduct);
                returnMap.put("isRowForProduct", isRowForProduct);
                returnMap.put("isRackForProduct", isRackForProduct);
                returnMap.put("isBinForProduct", isBinForProduct);
                returnMap.put("warehouseObj", warehouseObj);
                returnMap.put("locationObj", locationObj);
                returnMap.put("batchObj", productBatchObj);
                returnMap.put("batchName", batchName);
                returnMap.put("serialObj", newBatchSerial);
                returnMap.put("serialName", serialName);
                returnMap.put("rowObj", rowObj);
                returnMap.put("rackObj", rackObj);
                returnMap.put("binObj", binObj);
                returnMap.put("mfgdate", mfgdateStr);
                returnMap.put("expdate", expdateStr);
                returnMap.put("expstart", expstartStr);
                returnMap.put("expend", expendStr);
                returnMap.put("batchquantity", batchquantity);
                returnMap.put("failureMsg", failureMsg);
                 
            }
        } catch (Exception e) {
            try {
                throw ServiceException.FAILURE(e.getMessage(), e);
            } catch (ServiceException ex) {
                Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnMap;

    }
    /*
    Below method is used to validate and get batch detail JSON.
    */
    @Override
    public Map<String, Object> validateAndGetBatchSerialDetail(Map<String, Object> requestMap,Set<String> innerLineLevelDetailSet) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            Product product = null;
            HashMap<String, Integer> columnConfig = new HashMap<>();
            String[] recarr = null;
            String companyID = null;
            boolean isSalesTransaction = false;
            String failureMsg = "";
            String masterPreference = "";
            DateFormat df = null;
            String dateFormat = null;
            double dquantity = 0;
            boolean isLocationForProduct = false;
            boolean isWarehouseForProduct = false;
            boolean isBatchForProduct = false;
            boolean isSerialForProduct = false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;
            String warehouse = null;
            String location = null;
            String serialName = "";
            String batchName = "";
            String rowName = "";
            String rackName = "";
            String binName = "";
            String lineLevelKey = "";
            String innerLineLevelKey = "";
            String storeID = "";
            String inspectionStore = "";
            String jobWorkOutStore = "";
            String packingStore = "";
            InventoryLocation locationObj = null;
            NewProductBatch productBatchObj = null;
            NewBatchSerial newBatchSerial = null;
            StoreMaster rowObj = null;
            StoreMaster rackObj = null;
            StoreMaster binObj = null;
            boolean isnegativestockforlocwar = false;

            JSONObject batchDetailObj = new JSONObject();
            JSONArray batchDetailArr = new JSONArray();
            Map<String, Object> requestParams=new HashMap<>();

            if (requestMap.containsKey("isSalesTransaction") && requestMap.get("isSalesTransaction") != null) {
                isSalesTransaction = (boolean) requestMap.get("isSalesTransaction");
            }
            if (requestMap.containsKey("product") && requestMap.get("product") != null) {
                product = (Product) requestMap.get("product");
            }
            if (requestMap.containsKey("columnConfig") && requestMap.get("columnConfig") != null) {
                columnConfig = (HashMap<String, Integer>) requestMap.get("columnConfig");
            }
            if (requestMap.containsKey("recarr") && requestMap.get("recarr") != null) {
                recarr = (String[]) requestMap.get("recarr");
            }
            if (requestMap.containsKey("companyID") && requestMap.get("companyID") != null) {
                companyID = (String) requestMap.get("companyID");
            }
            if (requestMap.containsKey("failureMsg") && requestMap.get("failureMsg") != null) {
                failureMsg = (String) requestMap.get("failureMsg");
            }
            if (requestMap.containsKey("masterPreference") && requestMap.get("masterPreference") != null) {
                masterPreference = (String) requestMap.get("masterPreference");
            }
            if (requestMap.containsKey("df") && requestMap.get("df") != null) {
                df = (DateFormat) requestMap.get("df");
            }
            if (requestMap.containsKey("dquantity") && requestMap.get("dquantity") != null) {
                dquantity = (Double) requestMap.get("dquantity");
            }
            if (requestMap.containsKey("linelevelkey") && requestMap.get("linelevelkey") != null) {
                lineLevelKey = (String) requestMap.get("linelevelkey");
            }
            if (requestMap.containsKey("inspectionStore") && requestMap.get("inspectionStore") != null) {
                inspectionStore = (String) requestMap.get("inspectionStore");
            }
            if (requestMap.containsKey("jobWorkOutStore") && requestMap.get("jobWorkOutStore") != null) {
                jobWorkOutStore = (String) requestMap.get("jobWorkOutStore");
            }
            if (requestMap.containsKey("packingStore") && requestMap.get("packingStore") != null) {
                packingStore = (String) requestMap.get("packingStore");
            }
            if (requestMap.containsKey("isnegativestockforlocwar") && requestMap.get("isnegativestockforlocwar") != null) {
                isnegativestockforlocwar = (boolean) requestMap.get("isnegativestockforlocwar");
            }

            if (product != null) {
                isWarehouseForProduct = product.isIswarehouseforproduct();
                isLocationForProduct = product.isIslocationforproduct();
                isBatchForProduct = product.isIsBatchForProduct();
                isSerialForProduct = product.isIsSerialForProduct();
                isRowForProduct = product.isIsrowforproduct();
                isRackForProduct = product.isIsrackforproduct();
                isBinForProduct = product.isIsbinforproduct();

                batchDetailObj.put("isWarehouseForProduct", isWarehouseForProduct);
                batchDetailObj.put("isLocationForProduct", isLocationForProduct);
                batchDetailObj.put("isBatchForProduct", isBatchForProduct);
                batchDetailObj.put("isSerialForProduct", isSerialForProduct);
                batchDetailObj.put("isRowForProduct", isRowForProduct);
                batchDetailObj.put("isRackForProduct", isRackForProduct);
                batchDetailObj.put("isBinForProduct", isBinForProduct);
                batchDetailObj.put(Constants.productid, product.getID());
                batchDetailObj.put("quantity", dquantity);
                batchDetailObj.put("id", "");
                requestParams.put("productid", product.getID());
                requestParams.put("companyid", companyID);
                
                /*
                Validate and get Warehouse detail object
                */
                if (isWarehouseForProduct && columnConfig.containsKey("warehouse")) {
                    warehouse = recarr[(Integer) columnConfig.get("warehouse")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(warehouse)) {
                        requestParams.put("abbrev", warehouse);//abbrev is code of Warehouse
                        storeID = accountingHandlerDAOobj.getStoreByTypes(requestParams);
                        String msg=" So please give different warehouse.";
                        if (StringUtil.isNullOrEmpty(storeID)) {
                            failureMsg += "Warehouse is not found or type of warehouse is QA,Scrap,Repair etc.";
                        } else if (inspectionStore.equals(storeID)) {
                            failureMsg += warehouse + " warehouse is already used in QA Inspection store."+msg;
                        } else if (jobWorkOutStore.equals(storeID)) {
                            failureMsg += warehouse + " warehouse is already used in Vendor Job Order Store."+msg;
                        } else if (packingStore.equals(storeID)) {
                            failureMsg += warehouse + " warehouse is already used in Packaging Warehouse for Stock Transfer."+msg;
                        } else {
                            batchDetailObj.put("warehouse", storeID);
                            requestParams.put("store", storeID);
                            innerLineLevelKey+=storeID;
                        }

                    } else {
                        if (!masterPreference.equalsIgnoreCase("1")) {
                            failureMsg += "Product Warehouse is not available. ";
                        }
                    }
                }
                
                /*
                Validate and get Location detail object
                */
                if (isLocationForProduct && columnConfig.containsKey("location")) {
                    location = recarr[(Integer) columnConfig.get("location")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(location)) {
                        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                        filter_names.add("company.companyID");
                        filter_params.add(companyID);
                        filter_names.add("name");
                        filter_params.add(location);
                        filterRequestParams.put("filter_names", filter_names);
                        filterRequestParams.put("filter_params", filter_params);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.getLocationItems(filterRequestParams);
                        List list = cntResult.getEntityList();
                        if (list.isEmpty()) {
                            if (!masterPreference.equalsIgnoreCase("1")) {
                                failureMsg += "Location is not found for " + location + ". ";
                            }
                        } else {
                            locationObj = cntResult.getEntityList().get(0) != null ? (InventoryLocation) cntResult.getEntityList().get(0) : null;
                            batchDetailObj.put("location", locationObj.getId());
                            requestParams.put("location", locationObj.getId());
                            innerLineLevelKey+=locationObj.getId();
                        }
                    } else {
                        if (!masterPreference.equalsIgnoreCase("1")) {
                            failureMsg += "Product Location is not available. ";
                        }
                    }
                }
                /*
                Validate and get ROW detail object
                */
                if (isRowForProduct && columnConfig.containsKey("row")) {
                    rowName = recarr[(Integer) columnConfig.get("row")].replaceAll("\"", "").trim();
                    if (StringUtil.isNullOrEmpty(rowName)) {
                        failureMsg += "Product Row is not available.";
                    } else {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put(Constants.companyKey, companyID);
                        dataMap.put("type", 1);  //Tpe of row is 1
                        dataMap.put("name", rowName);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.buildQueryForgetStoreMasters(dataMap);
                        List list = cntResult.getEntityList();
                        if (list.isEmpty()) {
                            if (!masterPreference.equalsIgnoreCase("1")) {
                                failureMsg += "Row is not found for " + rowName + ". ";
                            }
                        } else {
                            rowObj = cntResult.getEntityList().get(0) != null ? (StoreMaster) cntResult.getEntityList().get(0) : null;
                            batchDetailObj.put("row", rowObj.getId());
                            requestParams.put("row", rowObj);
                        }
                    }
                }

                /*
                Validate and RACK  detail object
                */
                if (isRackForProduct && columnConfig.containsKey("rack")) {
                    rackName = recarr[(Integer) columnConfig.get("rack")].replaceAll("\"", "").trim();
                    if (StringUtil.isNullOrEmpty(rackName)) {
                        failureMsg += "Product Rack is not available.";
                    } else {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put(Constants.companyKey, companyID);
                        dataMap.put("type", 2); // type for rack is 2
                        dataMap.put("name", rackName);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.buildQueryForgetStoreMasters(dataMap);
                        List list = cntResult.getEntityList();
                        if (list.isEmpty()) {
                            if (!masterPreference.equalsIgnoreCase("1")) {
                                failureMsg += "Rack is not found for " + rackName + ". ";
                            }
                        } else {
                            rackObj = cntResult.getEntityList().get(0) != null ? (StoreMaster) cntResult.getEntityList().get(0) : null;
                            batchDetailObj.put("rack", rackObj.getId());
                            requestParams.put("rack", rackObj);
                        }
                    }
                }
                
                /*
                Validate and get BIN detail object
                */
                if (isBinForProduct && columnConfig.containsKey("bin")) {
                    binName = recarr[(Integer) columnConfig.get("bin")].replaceAll("\"", "").trim();
                    if (StringUtil.isNullOrEmpty(binName)) {
                        failureMsg += "Product Bin is not available.";
                    } else {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.put(Constants.companyKey, companyID);
                        dataMap.put("type", 3); //type for rack is 3
                        dataMap.put("name", binName);
                        KwlReturnObject cntResult = accMasterItemsDAOobj.buildQueryForgetStoreMasters(dataMap);
                        List list = cntResult.getEntityList();
                        if (list.isEmpty()) {
                            if (!masterPreference.equalsIgnoreCase("1")) {
                                failureMsg += "Bin is not found for " + binName + ". ";
                            }
                        } else {
                            binObj = cntResult.getEntityList().get(0) != null ? (StoreMaster) cntResult.getEntityList().get(0) : null;
                            batchDetailObj.put("bin", binObj.getId());
                            requestParams.put("bin", binObj);
                        }
                    }
                }
                // Row , Rack and Bin name validation   

                String mfgdateStr = "";
                if (columnConfig.containsKey("mfgdate")) {
                    mfgdateStr = recarr[(Integer) columnConfig.get("mfgdate")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(mfgdateStr)) {
                        try {
                            df.parse(mfgdateStr);
                            batchDetailObj.put("mfgdate", mfgdateStr);
                        } catch (Exception ex) {
                            failureMsg += "Incorrect date format for mfg Date, Please specify values in " + dateFormat + " format. ";
                        }
                    }
                }

                String expdateStr = "";
                if (columnConfig.containsKey("expdate")) {
                    expdateStr = recarr[(Integer) columnConfig.get("expdate")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(expdateStr)) {
                        try {
                            df.parse(expdateStr);
                            batchDetailObj.put("expdate", expdateStr);
                        } catch (Exception ex) {
                            failureMsg += "Incorrect date format for expriy Date, Please specify values in " + dateFormat + " format. ";
                        }
                    }
                }

                String expstartStr = "";
                if (columnConfig.containsKey("expstart")) {
                    expstartStr = recarr[(Integer) columnConfig.get("expstart")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(expstartStr)) {
                        try {
                            df.parse(expstartStr);
                            batchDetailObj.put("expstart", expstartStr);
                        } catch (Exception ex) {
                            failureMsg += "Incorrect date format for mfg Date, Please specify values in " + dateFormat + " format. ";
                        }
                    }
                }

                String expendStr = "";
                if (columnConfig.containsKey("expend")) {
                    expendStr = recarr[(Integer) columnConfig.get("expend")].replaceAll("\"", "").trim();
                    if (!StringUtil.isNullOrEmpty(expendStr)) {
                        try {
                            df.parse(expendStr);
                            batchDetailObj.put("expend", expstartStr);
                        } catch (Exception ex) {
                            failureMsg += "Incorrect date format for expriy Date, Please specify values in " + dateFormat + " format. ";
                        }
                    }
                }
                
                /*
                Validate and get batch detail object
                */
                if ((isBatchForProduct || isSalesTransaction)) {
                    batchName = (isBatchForProduct && columnConfig.containsKey("batch")) ? recarr[(Integer) columnConfig.get("batch")].replaceAll("\"", "").trim() : "";

                    if (!StringUtil.isNullOrEmpty(batchName)) {
                        requestParams.put("batch",batchName);
                    }
                    productBatchObj = (NewProductBatch) accountingHandlerDAOobj.getERPProductBatch(requestParams);
                    if (isBatchForProduct && StringUtil.isNullOrEmpty(batchName)) {
                        failureMsg += "Product ID '" + product.getProductid() + "' has batch activated. Please specify valid batch name. ";
                    }
                    if (isSalesTransaction && isBatchForProduct && productBatchObj == null) {
                        failureMsg += "Product Batch is not available for " + batchName + ". ";
                    }
                    
                    if (productBatchObj != null && (dquantity <= productBatchObj.getQuantitydue() || isnegativestockforlocwar)) {
                        batchDetailObj.put("batch", productBatchObj.getBatchname());
                        batchDetailObj.put("batchname", productBatchObj.getBatchname());
                        batchDetailObj.put("purchasebatchid", productBatchObj.getId());
                        innerLineLevelKey+=productBatchObj.getId();

                    }else if(!isnegativestockforlocwar){
                        failureMsg += "Quantity is not available for product " + product.getName() + ". ";
                    }

                }
                
               /*
                If serial is given by comma(,)separated then iterate it and create the jSON object
                */
                int serialCount = 0;
                if (isSerialForProduct && columnConfig.containsKey("serial")) {
                    serialName = recarr[(Integer) columnConfig.get("serial")].replaceAll("\"", "").trim();
                    if (StringUtil.isNullOrEmpty(serialName)) {
                        failureMsg += "Product ID '" + product.getProductid() + "' has serial activated. Please specify valid serial number. ";
                    } else {
                        String serialNames[] = serialName.split(",");
                        serialCount = serialNames.length;
                        int duplicateCount = 0;
                        KwlReturnObject result = null;

                        if (dquantity != serialCount) {
                            failureMsg += "Quantity and No. of Serial does not match. ";
                        }

                        for (int index = 0; index < serialCount; index++) {
                            JSONObject tempBatchSerialObj = new JSONObject(batchDetailObj.toString());
                            if (productBatchObj != null) {
                                result = accMasterItemsDAOobj.checkDuplicateSerialforProduct(product.getID(), productBatchObj.getId(), serialNames[index], companyID);
                                duplicateCount = result.getRecordTotalCount();
                            } else {
                                result = accMasterItemsDAOobj.checkDuplicateSerialforProduct(product.getID(), "", serialNames[index], companyID);
                                duplicateCount = result.getRecordTotalCount();
                            }

                            if (!isSalesTransaction && duplicateCount > 0) {
                                failureMsg += "Serial " + serialNames[index] + " is already exists. ";
                            } else if (isSalesTransaction && duplicateCount == 0) {
                                failureMsg += "Serial " + serialNames[index] + " is not exists. ";
                            } else if (isSalesTransaction && duplicateCount == 1) {
                                newBatchSerial = result.getEntityList().get(0) != null ? (NewBatchSerial) result.getEntityList().get(0) : null;
                            }
                            // put serial details in batch detail json
                            if(StringUtil.isNullOrEmpty(failureMsg)){
                                if (newBatchSerial != null) {
                                    tempBatchSerialObj.put("serialno", newBatchSerial.getSerialname());
                                    tempBatchSerialObj.put("purchaseserialid", newBatchSerial.getId());
                                    tempBatchSerialObj.put("serialnoid", newBatchSerial.getId());
                                }
                                batchDetailArr.put(tempBatchSerialObj);
                            }
                        }

                    }
                } else if (isSerialForProduct) {
                    failureMsg += "Product ID '" + product.getProductid() + "' has serial activated. Please specify valid serial number. ";
                }else if(StringUtil.isNullOrEmpty(failureMsg)){
                   batchDetailArr.put(batchDetailObj); 
                }
                /*
                1.lineLevelKey- To check duplicate line level row. We creating the key with combination of 'entryNumber + product.getProductid() + unitPrice + discountValue'
                2.innerLineLevelKey- To check duplicate Batch details row.We are creating key with combination of lineLevelKey+'Warehouse+Location+Batch' 
                */
                if (!innerLineLevelDetailSet.contains(lineLevelKey + innerLineLevelKey)) {
                    innerLineLevelDetailSet.add(lineLevelKey + innerLineLevelKey);
                }else{
                    failureMsg += "Product '" + product.getProductid() + "' has duplicate details of Warehouse/Location/Batch Details. ";
                }
                returnMap.put("failureMsg", failureMsg);
                returnMap.put("batchDetailArr", batchDetailArr);
            }
        } catch (Exception e) {
            try {
                throw ServiceException.FAILURE(e.getMessage(), e);
            } catch (ServiceException ex) {
                Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return returnMap;
    }
   
 @Override   
    public JSONObject getProductsByCategory(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        String categoryid = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.start, paramJobj.optString(Constants.start));
            requestParams.put(Constants.limit, paramJobj.optString(Constants.limit));
            requestParams.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("categoryid",null))) {
                categoryid = paramJobj.optString("categoryid");
                String[] ids = (String[]) categoryid.split(",");
                String categoryidarray = "";
                for (int i = 0; i < ids.length; i++) {
                    categoryidarray += ids[i] + ",";
                    MasterItem pref = (MasterItem) kwlCommonTablesDAOObj.getClassObject(MasterItem.class.getName(), ids[i]);
                    if (pref != null && pref.getChildren().size() > 0) {
                        Iterator<MasterItem> chieldValues = pref.getChildren().iterator();
                        while (chieldValues.hasNext()) {
                            MasterItem item = (MasterItem) chieldValues.next();
                            categoryidarray += item.getID() + ",";
                        }

                    }
                }
                categoryidarray = categoryidarray.substring(0, categoryidarray.length() - 1);
                if (categoryidarray.contains(",")) {
                    requestParams.put("categoryids", categoryidarray);
                }
            }
            /**
             * This Function will use when Users Visibility Feature is Enable
             * Append user condition while querying data
             */
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", paramJobj.optString("companyid"));
            if (extraPref != null && extraPref.isUsersVisibilityFlow()) {
                KwlReturnObject object = accountingHandlerDAOobj.getObject(User.class.getName(), paramJobj.optString("userid"));
                User user = object.getEntityList().size() > 0 ? (User) object.getEntityList().get(0) : null;
                if (!AccountingManager.isCompanyAdmin(user)) {
                    /**
                     * if Users visibility enable and current user is not admin
                     */
                    Map<String, Object> reqMap = new HashMap();
                    requestParams.put("isUserVisibilityFlow", true);
                    reqMap.put("companyid", paramJobj.optString("companyid"));
                    reqMap.put("userid", paramJobj.optString("userid"));
                    reqMap.put("jointable", "pcd");
                    reqMap.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                    String custcondition = fieldManagerDAOobj.appendUsersCondition(reqMap);
                    if (!StringUtil.isNullOrEmpty(custcondition)) {
                        /**
                         * If mapping found with dimension
                         */
                        String usercondition = " and (" + custcondition + ")";
                        requestParams.put("appendusercondtion", usercondition);
                    } else {
                        /**
                         * If no Mapping found for current user then return
                         * function call
                         */
                        jobj.put(Constants.RES_success, true);
                        jobj.put(Constants.RES_msg, msg);
                        jobj.put(Constants.RES_data, new com.krawler.utils.json.JSONArray());
                        jobj.put(Constants.RES_TOTALCOUNT, 0);
                        return jobj;
                    }
                }
            }

            requestParams.put("categoryid", !StringUtil.isNullOrEmpty(paramJobj.optString("categoryid",null)) ? paramJobj.optString("categoryid") : "");
            requestParams.put("ss", paramJobj.optString("ss",null));
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("dir",null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("sort",null))) {
                requestParams.put("dir",paramJobj.optString("dir"));
                requestParams.put("sort", paramJobj.optString("sort"));
            }
            KwlReturnObject result = accProductObj.getNewProductList(requestParams);
            JSONArray jArr = getProductsByCategoryJson(paramJobj, result.getEntityList());

            jobj.put(Constants.RES_data, jArr);
            jobj.put(Constants.RES_TOTALCOUNT, result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

 @Override    
   public JSONArray getProductsByCategoryJson(JSONObject paramJobj, List list) throws JSONException, ServiceException, UnsupportedEncodingException, SessionExpiredException, ParseException {
        JSONArray jArr = new JSONArray();
        Producttype producttype = new Producttype();
        String productid = paramJobj.optString(Constants.productid,null);
        String companyid = paramJobj.optString(Constants.companyKey);
        Boolean nonSaleInventory = Boolean.parseBoolean(paramJobj.optString("loadInventory"));
        KwlReturnObject purchaseprice = null, saleprice = null, quantity = null, initialquantity = null, initialprice = null, salespricedatewise = null, purchasepricedatewise = null, initialsalesprice = null;
        Date transactionDate = null;
        String transDate = StringUtil.isNullOrEmpty(paramJobj.optString("transactiondate",null)) ? "" :  paramJobj.optString("transactiondate");
        if (!StringUtil.isNullOrEmpty(transDate)) {  //Used same date formatter which have used to save currency exchange
            transactionDate = authHandler.getDateOnlyFormatter(paramJobj).parse(transDate);
        }
        String companyCurrencyID = paramJobj.optString(Constants.globalCurrencyKey);
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        for (Object object : list) {
            try {
                Object[] row = (Object[]) object;
                String Prodid = row[0].toString();
                String CategoryId = row[1] != null ? row[1].toString() : "";
                KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), Prodid);
                Product product = (Product) prodresult.getEntityList().get(0);

                if (product.getID().equals(productid)) {
                    continue;
                }
                Producttype productType=product.getProducttype() != null ? product.getProducttype():null;
                MasterItem masterItem = null;
                if (!StringUtil.isNullOrEmpty(CategoryId)) {
                    KwlReturnObject catresult = accProductObj.getObject(MasterItem.class.getName(), CategoryId);
                    masterItem = (MasterItem) catresult.getEntityList().get(0);
                }

                Product parentProduct = product.getParent();
                double purchasePriceInBase = 0;
                double salesPriceInBase = 0;
                String productCurrencyId=companyCurrencyID;
                if(product.getCurrency()!=null){
                    productCurrencyId=product.getCurrency().getCurrencyID();
                }
                purchaseprice = accProductObj.getProductPrice(product.getID(), true, null, "", productCurrencyId);
                saleprice = accProductObj.getProductPrice(product.getID(), false, null, "", productCurrencyId);

                Double purchasePrice = purchaseprice.getEntityList().get(0) == null ? 0 : (Double)purchaseprice.getEntityList().get(0);
                Double salesPrice = saleprice.getEntityList().get(0) == null ? 0 : (Double)saleprice.getEntityList().get(0);

                KwlReturnObject pAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, purchasePrice, productCurrencyId, transactionDate, 0);
                purchasePriceInBase= (Double)pAmt.getEntityList().get(0);

                KwlReturnObject sAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, salesPrice, productCurrencyId, transactionDate, 0);
                salesPriceInBase= (Double)sAmt.getEntityList().get(0);

                quantity = accProductObj.getQuantity(product.getID());
                initialquantity = accProductObj.getInitialQuantity(product.getID());
                initialprice = accProductObj.getInitialPrice(product.getID(), true);
                salespricedatewise = accProductObj.getProductPrice(product.getID(), false, null, null, "");
                purchasepricedatewise = accProductObj.getProductPrice(product.getID(), true, null, null, "");
                initialsalesprice = accProductObj.getInitialPrice(product.getID(), false);

                JSONObject obj = new JSONObject();
                obj.put(Constants.productid, product.getID());
                obj.put("productname", product.getName());
                obj.put("description", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                obj.put("desc", product.getDescription());
                UnitOfMeasure uom = product.getUnitOfMeasure();
                obj.put("uomid", uom == null ? "" : uom.getID());
                obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
                obj.put("precision", uom == null ? 0 : (Integer) uom.getAllowedPrecision());
                obj.put("leadtime", product.getLeadTimeInDays());
                obj.put("warrantyperiod", product.getWarrantyperiod());
                obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
                obj.put("supplier", product.getSupplier());
                obj.put("coilcraft", product.getCoilcraft());
                obj.put("interplant", product.getInterplant());
                obj.put("syncable", product.isSyncable());
                obj.put("reorderlevel", product.getReorderLevel());
                obj.put("reorderquantity", product.getReorderQuantity());
                obj.put("purchaseaccountid", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : ""));
                obj.put("salesaccountid", (product.getSalesAccount() != null ? product.getSalesAccount().getID() : ""));
                obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
                obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
                obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
                obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
                obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                obj.put("producttype", (productType != null ? productType.getID() : ""));
                obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
                obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                obj.put("type", (productType != null ?productType.getName() : ""));
                obj.put("pid", product.getProductid());
                obj.put("productcode", product.getProductid());
                obj.put("valuationmethod", product.getValuationMethod() != null ? product.getValuationMethod().ordinal() : ""); //SDP-13951
                obj.put("productweight", product.getProductweight());
                obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
                obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
                obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
                obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
                obj.put("productBrandName",(product.getProductBrand() != null) ? product.getProductBrand().getValue() : "");
                obj.put("licensetype",product.getLicenseType() != null ? product.getLicenseType() : LicenseType.NONE);
                obj.put("itemReusability",product.getItemReusability() != null ? product.getItemReusability().ordinal() : "");
                if (product.getWarrantyperiod() == 0) {
                    obj.put("warranty", "N/A");
                } else {
                    obj.put("warranty", product.getWarrantyperiod());
                }
                if (product.getWarrantyperiodsal() == 0) {
                    obj.put("warrantyperiodsal", "N/A");
                } else {
                    obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
                }
                obj.put("isLocationForProduct", product.isIslocationforproduct());
                obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
                obj.put("isRowForProduct", product.isIsrowforproduct());
                obj.put("isRackForProduct", product.isIsrackforproduct());
                obj.put("isBinForProduct", product.isIsbinforproduct());
                obj.put("isBatchForProduct", product.isIsBatchForProduct());
                obj.put("isSerialForProduct", product.isIsSerialForProduct());

                obj.put("location", (product.getLocation() != null ? product.getLocation().getId() : ""));
                obj.put("warehouse", (product.getWarehouse() != null ? product.getWarehouse().getId() : ""));
                obj.put("locationName", (product.getLocation() != null ? product.getLocation().getName() : ""));
                obj.put("warehouseName", (product.getWarehouse() != null ? product.getWarehouse().getName() : ""));

                obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
                obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
                obj.put("parentuuid", parentProduct == null ? "" : parentProduct.getID());
                obj.put("parentid", parentProduct == null ? "" : parentProduct.getProductid());
                obj.put("parentname", parentProduct == null ? "" : parentProduct.getName());

                obj.put("level", 0);
                obj.put("leaf", true);

                obj.put("purchaseprice", authHandler.round(purchasePriceInBase, companyid));
                obj.put("saleprice", authHandler.round(salesPriceInBase, companyid));
                obj.put("quantity", (quantity.getEntityList().get(0) == null ? 0 : quantity.getEntityList().get(0)));
                obj.put("initialquantity", (initialquantity.getEntityList().get(0) == null ? 0 : initialquantity.getEntityList().get(0)));
                obj.put("initialprice", (initialprice.getEntityList().get(0) == null ? 0 : initialprice.getEntityList().get(0)));
                obj.put("salespricedatewise", (salespricedatewise.getEntityList().get(0) == null ? 0 : salespricedatewise.getEntityList().get(0)));
                obj.put("purchasepricedatewise", (purchasepricedatewise.getEntityList().get(0) == null ? 0 : purchasepricedatewise.getEntityList().get(0)));
                obj.put("initialsalesprice", (initialsalesprice.getEntityList().get(0) == null ? 0 : initialsalesprice.getEntityList().get(0)));
                obj.put("categoryid", masterItem == null ? "" : masterItem.getID());
                obj.put("category", masterItem == null ? "" : masterItem.getValue());
                if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                    // Do Nothing
                } else {
                    jArr.put(obj);
                }
            } catch (UnsupportedEncodingException ex) {
                throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
            }
        }
        return jArr;
    }
   
   @Override    
   public JSONArray getProductsByCategoryJsonForExport(JSONObject paramJobj, List list) throws JSONException, ServiceException, UnsupportedEncodingException, SessionExpiredException, ParseException {
        JSONArray jArr = new JSONArray();
        Producttype producttype = new Producttype();
        String productid = paramJobj.optString(Constants.productid,null);
        String companyid = paramJobj.optString(Constants.companyKey);
        Boolean nonSaleInventory = Boolean.parseBoolean(paramJobj.optString("loadInventory"));
        KwlReturnObject purchaseprice = null, saleprice = null, quantity = null, /*initialquantity = null,*/ initialprice = null, salespricedatewise = null, purchasepricedatewise = null, initialsalesprice = null;
        Date transactionDate = null;
        String transDate = StringUtil.isNullOrEmpty(paramJobj.optString("transactiondate",null)) ? "" :  paramJobj.optString("transactiondate");
        if (!StringUtil.isNullOrEmpty(transDate)) {  //Used same date formatter which have used to save currency exchange
            transactionDate = authHandler.getDateOnlyFormatter(paramJobj).parse(transDate);
        }
        String companyCurrencyID = paramJobj.optString(Constants.globalCurrencyKey);
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        for (Object object : list) {
            try {
                Object[] row = (Object[]) object;
                String Prodid = row[0].toString();
                String CategoryId = row[1] != null ? row[1].toString() : "";
                KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), Prodid);
                Product product = (Product) prodresult.getEntityList().get(0);

                if (product.getID().equals(productid)) {
                    continue;
                }
                Producttype productType=product.getProducttype() != null ? product.getProducttype():null;
                MasterItem masterItem = null;
                if (!StringUtil.isNullOrEmpty(CategoryId)) {
                    KwlReturnObject catresult = accProductObj.getObject(MasterItem.class.getName(), CategoryId);
                    masterItem = (MasterItem) catresult.getEntityList().get(0);
                }

//                Product parentProduct = product.getParent();
                double purchasePriceInBase = 0;
                double salesPriceInBase = 0;
                String productCurrencyId=companyCurrencyID;
                if(product.getCurrency()!=null){
                    productCurrencyId=product.getCurrency().getCurrencyID();
                }
                purchaseprice = accProductObj.getProductPrice(product.getID(), true, null, "", productCurrencyId);
                saleprice = accProductObj.getProductPrice(product.getID(), false, null, "", productCurrencyId);

                Double purchasePrice = purchaseprice.getEntityList().get(0) == null ? 0 : (Double)purchaseprice.getEntityList().get(0);
                Double salesPrice = saleprice.getEntityList().get(0) == null ? 0 : (Double)saleprice.getEntityList().get(0);

                KwlReturnObject pAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, purchasePrice, productCurrencyId, transactionDate, 0);
                purchasePriceInBase= (Double)pAmt.getEntityList().get(0);

                KwlReturnObject sAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, salesPrice, productCurrencyId, transactionDate, 0);
                salesPriceInBase= (Double)sAmt.getEntityList().get(0);

                quantity = accProductObj.getQuantity(product.getID());
//                initialquantity = accProductObj.getInitialQuantity(product.getID());
//                initialprice = accProductObj.getInitialPrice(product.getID(), true);
//                salespricedatewise = accProductObj.getProductPrice(product.getID(), false, null, null, "");
//                purchasepricedatewise = accProductObj.getProductPrice(product.getID(), true, null, null, "");
//                initialsalesprice = accProductObj.getInitialPrice(product.getID(), false);

                JSONObject obj = new JSONObject();
                obj.put(Constants.productid, product.getID());
                obj.put("productname", product.getName());
                obj.put("description", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));
                obj.put("desc", product.getDescription());
                UnitOfMeasure uom = product.getUnitOfMeasure();
                obj.put("uomid", uom == null ? "" : uom.getID());
                obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
//                obj.put("precision", uom == null ? 0 : (Integer) uom.getAllowedPrecision());
                obj.put("leadtime", product.getLeadTimeInDays());
//                obj.put("warrantyperiod", product.getWarrantyperiod());
                obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
//                obj.put("supplier", product.getSupplier());
//                obj.put("coilcraft", product.getCoilcraft());
//                obj.put("interplant", product.getInterplant());
//                obj.put("syncable", product.isSyncable());
                obj.put("reorderlevel", product.getReorderLevel());
                obj.put("reorderquantity", product.getReorderQuantity());
//                obj.put("purchaseaccountid", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : ""));
//                obj.put("salesaccountid", (product.getSalesAccount() != null ? product.getSalesAccount().getID() : ""));
//                obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
//                obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
//                obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
//                obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
//                obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
//                obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
//                obj.put("producttype", (productType != null ? productType.getID() : ""));
//                obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
//                obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
                obj.put("type", (productType != null ?productType.getName() : ""));
                obj.put("pid", product.getProductid());
//                obj.put("productcode", product.getProductid());
                obj.put("valuationmethod", product.getValuationMethod() != null ? product.getValuationMethod().ordinal() : ""); //SDP-13951
//                obj.put("productweight", product.getProductweight());
//                obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
//                obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
//                obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
//                obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
                obj.put("productBrandName",(product.getProductBrand() != null) ? product.getProductBrand().getValue() : "");
                obj.put("licensetype",product.getLicenseType() != null ? product.getLicenseType() : LicenseType.NONE);
                obj.put("itemReusability",product.getItemReusability() != null ? product.getItemReusability().ordinal() : "");
                if (product.getWarrantyperiod() == 0) {
                    obj.put("warranty", "N/A");
                } else {
                    obj.put("warranty", product.getWarrantyperiod());
                }
//                if (product.getWarrantyperiodsal() == 0) {
//                    obj.put("warrantyperiodsal", "N/A");
//                } else {
//                    obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
//                }
//                obj.put("isLocationForProduct", product.isIslocationforproduct());
//                obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
//                obj.put("isRowForProduct", product.isIsrowforproduct());
//                obj.put("isRackForProduct", product.isIsrackforproduct());
//                obj.put("isBinForProduct", product.isIsbinforproduct());
//                obj.put("isBatchForProduct", product.isIsBatchForProduct());
//                obj.put("isSerialForProduct", product.isIsSerialForProduct());

//                obj.put("location", (product.getLocation() != null ? product.getLocation().getId() : ""));
//                obj.put("warehouse", (product.getWarehouse() != null ? product.getWarehouse().getId() : ""));
//                obj.put("locationName", (product.getLocation() != null ? product.getLocation().getName() : ""));
                obj.put("warehouseName", (product.getWarehouse() != null ? product.getWarehouse().getName() : ""));

//                obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
//                obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
                obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
//                obj.put("parentuuid", parentProduct == null ? "" : parentProduct.getID());
//                obj.put("parentid", parentProduct == null ? "" : parentProduct.getProductid());
//                obj.put("parentname", parentProduct == null ? "" : parentProduct.getName());

                obj.put("level", 0);
                obj.put("leaf", true);

                obj.put("purchaseprice", authHandler.round(purchasePriceInBase, companyid));
                obj.put("saleprice", authHandler.round(salesPriceInBase, companyid));
                obj.put("quantity", (quantity.getEntityList().get(0) == null ? 0 : quantity.getEntityList().get(0)));
//                obj.put("initialquantity", (initialquantity.getEntityList().get(0) == null ? 0 : initialquantity.getEntityList().get(0)));
//                obj.put("initialprice", (initialprice.getEntityList().get(0) == null ? 0 : initialprice.getEntityList().get(0)));
//                obj.put("salespricedatewise", (salespricedatewise.getEntityList().get(0) == null ? 0 : salespricedatewise.getEntityList().get(0)));
//                obj.put("purchasepricedatewise", (purchasepricedatewise.getEntityList().get(0) == null ? 0 : purchasepricedatewise.getEntityList().get(0)));
//                obj.put("initialsalesprice", (initialsalesprice.getEntityList().get(0) == null ? 0 : initialsalesprice.getEntityList().get(0)));
                obj.put("categoryid", masterItem == null ? "" : masterItem.getID());
                obj.put("category", masterItem == null ? "" : masterItem.getValue());
                if (nonSaleInventory && obj.get("producttype").equals(producttype.Inventory_Non_Sales)) {
                    // Do Nothing
                } else {
                    jArr.put(obj);
                }
            } catch (UnsupportedEncodingException ex) {
                throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
            }
        }
        return jArr;
    }  
    
   /**
    * Method to create or update Item on AvaTax whenever a Product is created or updated in our system
    * Used in Avalara Integration
    * @param product
    * @param companyid
    * @param isEdit
    * @throws JSONException
    * @throws ServiceException
    * @throws AccountingException 
    */
    @Override
    public void createOrUpdateItemOnAvalaraForProduct(Product product, String companyid, boolean isEdit) throws JSONException, ServiceException, AccountingException {
        String productid = product.getID();
        JSONObject paramsJobj = new JSONObject();
        paramsJobj.put(Constants.companyKey, companyid);
        paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
        paramsJobj.put(Constants.moduleid, Constants.Acc_Product_Master_ModuleId);
        JSONObject productJobj = new JSONObject();
        productJobj.put(Constants.productid, productid);
        productJobj.put("pid", product.getProductid());
        productJobj.put("description", product.getDescription());
        JSONArray productsJarr = new JSONArray();
        productsJarr.put(productJobj);
        paramsJobj.put(Constants.detail, productsJarr.toString());
        JSONObject responseJobj = null;
        if (isEdit) {
            KwlReturnObject tempKwlObj = accountingHandlerDAOobj.getObject(ProductAvalaraIdMapping.class.getName(), productid);
            if (tempKwlObj != null && tempKwlObj.getEntityList() != null && !tempKwlObj.getEntityList().isEmpty() && tempKwlObj.getEntityList().get(0) != null) {
                ProductAvalaraIdMapping productAvalaraIdMapping = (ProductAvalaraIdMapping) tempKwlObj.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(productAvalaraIdMapping.getAvalaraItemId())) {
                    paramsJobj.put("avalaraItemId", productAvalaraIdMapping.getAvalaraItemId());
                    paramsJobj.put(IntegrationConstants.integrationOperationIdKey, IntegrationConstants.avalara_updateItem);
                    responseJobj = integrationCommonService.processIntegrationRequest(paramsJobj);
                }
            }
        } else {
            paramsJobj.put(IntegrationConstants.integrationOperationIdKey, IntegrationConstants.avalara_createItems);
            responseJobj = integrationCommonService.processIntegrationRequest(paramsJobj);
        }
        if (responseJobj != null && responseJobj.optBoolean(Constants.RES_success, false)) {
            String avalaraItemId = null;
            if (isEdit && responseJobj.optJSONObject("resultJobj") != null) {
                JSONObject avalaraResponseJobj = responseJobj.optJSONObject("resultJobj");
                avalaraItemId = avalaraResponseJobj.optString("id");
            } else if (responseJobj.optJSONArray("resultJarr") != null) {
                JSONArray avalaraResponseJarr = responseJobj.optJSONArray("resultJarr");
                avalaraItemId = (avalaraResponseJarr.length() != 0 && avalaraResponseJarr.optJSONObject(0) != null) ? avalaraResponseJarr.optJSONObject(0).optString("id") : null;
            }
            if (!StringUtil.isNullOrEmpty(avalaraItemId)) {
                HashMap paramsMap = new HashMap<String, Object>();
                paramsMap.put("id", productid);
                paramsMap.put("productCreatedOnAvalara", true);
                accProductObj.updateProduct(paramsMap);//update productCreatedOnAvalara flag on successfulk deletion
                paramsJobj = new JSONObject();
                paramsJobj.put("productid", product.getID());
                paramsJobj.put("avalaraItemId", avalaraItemId);
                accProductObj.saveOrUpdateProductAvalaraIdMapping(paramsJobj);
            }
        }
    }
   
    /**
     * Method to delete Item on AvaTax whenever a Product is delete from our system
     * Used in Avalara Integration
     * @param productid
     * @param companyid
     * @throws JSONException
     * @throws ServiceException
     * @throws AccountingException 
     */
    @Override
    public void deleteItemOnAvalaraForProduct(String productid, String companyid) throws JSONException, ServiceException, AccountingException {
        String avalaraItemId = null;
        KwlReturnObject result = accountingHandlerDAOobj.getObject(ProductAvalaraIdMapping.class.getName(), productid);
        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty() && result.getEntityList().get(0) != null) {
            ProductAvalaraIdMapping productAvalaraIdMapping = (ProductAvalaraIdMapping) result.getEntityList().get(0);
            avalaraItemId = productAvalaraIdMapping.getAvalaraItemId();
            if (!StringUtil.isNullOrEmpty(avalaraItemId)) {
                JSONObject paramsJobj = new JSONObject();
                paramsJobj.put("avalaraItemId", avalaraItemId);
                paramsJobj.put(Constants.companyKey, companyid);
                paramsJobj.put(IntegrationConstants.integrationPartyIdKey, IntegrationConstants.integrationPartyId_AVALARA);
                paramsJobj.put(Constants.moduleid, Constants.Acc_Product_Master_ModuleId);
                paramsJobj.put(IntegrationConstants.integrationOperationIdKey, IntegrationConstants.avalara_deleteItem);
                JSONObject responseJobj = integrationCommonService.processIntegrationRequest(paramsJobj);
                if (responseJobj.optBoolean(Constants.RES_success)) {
                    HashMap paramsMap = new HashMap<String, Object>();
                    paramsMap.put(Constants.Acc_id, productid);
                    paramsMap.put("productCreatedOnAvalara", false);
                    accProductObj.updateProduct(paramsMap);//update productCreatedOnAvalara flag on successfull deletion
                }
            }
        }
    }
   
    /**
     * Method to create JSON from Product object
     * @param product
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    @Override
    public JSONObject createProductJsonObject(Product product) throws JSONException, ServiceException {
        JSONObject obj = new JSONObject();
        KwlReturnObject kwlObj;

        obj.put("productid", product.getID());
        obj.put("productname", product.getName());//"<font color='blue'>"+ product.getName()+"</font>");//to give color to a font
        obj.put("desc", product.getDescription());
        obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(product.getSupplier()) ? "" : product.getSupplier());
        UnitOfMeasure uom = product.getUnitOfMeasure();
        obj.put("uomid", uom == null ? "" : uom.getID());
        obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
        obj.put("multiuom", product.isMultiuom());
        obj.put("isAsset", product.isAsset());
        obj.put("isActive", product.isIsActive());
        obj.put("hasAccess", product.isIsActive());
        if (product.isAsset()) {
            obj.put("depreciationRate", product.getDepreciationRate());
            obj.put("depreciationMethod", product.getDepreciationMethod());
            obj.put("depreciationCostLimit", product.getDepreciationCostLimit());
            obj.put("depreciationGL", (product.getDepreciationGLAccount() != null) ? product.getDepreciationGLAccount().getID() : "");
            obj.put("provisionGL", (product.getDepreciationProvisionGLAccount() != null) ? product.getDepreciationProvisionGLAccount().getID() : "");
            obj.put("assetSaleGL", (product.getSellAssetGLAccount() != null) ? product.getSellAssetGLAccount().getID() : "");
            obj.put("depreciationGLAccount", (product.getDepreciationGLAccount() != null) ? product.getDepreciationGLAccount().getID() : "");
            obj.put("depreciationProvisionGLAccount", (product.getDepreciationProvisionGLAccount() != null) ? product.getDepreciationProvisionGLAccount().getID() : "");
        }
        obj.put("uomschematypeid", product.getUomSchemaType() != null ? product.getUomSchemaType().getID() : "");
        obj.put("isBatchForProduct", product.isIsBatchForProduct());
        obj.put("isAutoAssembly", product.isAutoAssembly());
        obj.put("isSerialForProduct", product.isIsSerialForProduct());
        obj.put("isSKUForProduct", product.isIsSKUForProduct());
        obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
        obj.put("isLocationForProduct", product.isIslocationforproduct());
        obj.put("location", product.getLocation() != null ? product.getLocation().getId() : "");
        obj.put("warehouse", product.getWarehouse() != null ? product.getWarehouse().getId() : "");
        obj.put("isRowForProduct", product.isIsrowforproduct());
        obj.put("isRackForProduct", product.isIsrackforproduct());
        obj.put("isBinForProduct", product.isIsbinforproduct());
        obj.put("isRecyclable", product.isRecyclable());
        obj.put("rcmapplicable", product.isRcmApplicable());
        obj.put("activateProductComposition", product.isActivateProductComposition());
        UnitOfMeasure purchaseuom = product.getPurchaseUOM();
        UnitOfMeasure salesuom = product.getSalesUOM();
        obj.put("stockpurchaseuomvalue", purchaseuom == null || product.getPackaging() == null ? 1 : product.getPackaging().getStockUomQtyFactor(purchaseuom));
        obj.put("stocksalesuomvalue", salesuom == null || product.getPackaging() == null ? 1 : product.getPackaging().getStockUomQtyFactor(salesuom));
        obj.put("recycleQuantity", product.getRecycleQuantity());
        obj.put("purchaseacctaxcode", ((product.getPurchaseAccount() != null && (!StringUtil.isNullOrEmpty(product.getPurchaseAccount().getTaxid()))) ? product.getPurchaseAccount().getTaxid() : ""));
        obj.put("salesacctaxcode", ((product.getSalesAccount() != null && (!StringUtil.isNullOrEmpty(product.getSalesAccount().getTaxid()))) ? product.getSalesAccount().getTaxid() : ""));
        obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
        obj.put("type", "Inventory Part");
        obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
        kwlObj = accProductObj.getProductPrice(product.getID(), true, null, "", "");
        obj.put("purchaseprice", kwlObj.getEntityList().get(0));
        kwlObj = accProductObj.getProductPrice(product.getID(), false, null, "", "");
        obj.put("saleprice", kwlObj.getEntityList().get(0));

        String type = "";
        if (product.getProducttype() != null) {
            type = product.getProducttype().getName();
        }
        obj.put("type", type);
        obj.put("pid", product.getProductid());
        obj.put("producttype", (product.getProducttype() != null ? product.getProducttype().getID() : ""));
        KwlReturnObject resultproduct = accProductObj.getQuantity(product.getID());
        obj.put("quantity", (resultproduct.getEntityList().get(0) == null ? 0 : resultproduct.getEntityList().get(0)));
        obj.put("productweightperstockuom", product.getProductWeightPerStockUom());
        obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
        obj.put("productvolumeperstockuom", product.getProductVolumePerStockUom());
        obj.put("productvolumeincludingpakagingperstockuom", product.getProductVolumeIncludingPakagingPerStockUom());
        obj.put("itctype", product.getItcType());

        return obj;
    }
   
    @Override
    public JSONObject getIndividualProductDiscount(JSONObject paramsjobj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray discountMasterJarr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            KwlReturnObject resultOfDiscount = accMasterItemsDAOobj.getDiscountOfProductForPricingBand(paramsjobj);
            List<Object[]> listOfDiscount = resultOfDiscount.getEntityList();
            Iterator listOfDiscountIterator = listOfDiscount.iterator();
            while (listOfDiscountIterator.hasNext()) {
                JSONObject jsonobj = new JSONObject();
                String ids = (String) listOfDiscountIterator.next();
                if (!StringUtil.isNullOrEmpty(ids)) {
                    KwlReturnObject discountMasterReturnObj = accountingHandlerDAOobj.getObject(DiscountMaster.class.getName(), ids);
                    DiscountMaster discountMaster = (DiscountMaster) discountMasterReturnObj.getEntityList().get(0);
                    jsonobj.put("discountid", discountMaster.getId());
                    jsonobj.put("discountname", discountMaster.getName());
                    jsonobj.put("discountdescription", discountMaster.getDescription());
                    jsonobj.put("discountvalue", discountMaster.getValue());
                    jsonobj.put("discountaccount", discountMaster.getAccount());
                    jsonobj.put("discounttype", discountMaster.isDiscounttype() ? Constants.DISCOUNT_MASTER_TYPE_PERCENTAGE : Constants.DISCOUNT_MASTER_TYPE_FLAT);
                    discountMasterJarr.put(jsonobj);
                }
            }
            jobj.put("data", discountMasterJarr);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    /**
     * Function to get Product tax class history.
     *
     * @param reqMap
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getProductGSTHistory(Map<String, Object> reqMap) throws ServiceException, JSONException {
        boolean gsthistorydetails = false;
        if (reqMap.containsKey("gsthistorydetails")) {
            gsthistorydetails = (Boolean) reqMap.get("gsthistorydetails");
        }
         if (reqMap.containsKey("isFixedAsset") && (Boolean) reqMap.get("isFixedAsset")) {
            reqMap.put("moduleid", Constants.Acc_FixedAssets_AssetsGroups_ModuleId);
        }else{
             reqMap.put("moduleid", Constants.Acc_Product_Master_ModuleId);
         }
        reqMap.put("isdetails", true);
        reqMap.put("fieldname", "Custom_" + Constants.GSTProdCategory);
        DateFormat df = null;
        try {
            df = authHandler.getOnlyDateFormat();
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        JSONObject data = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        List<ProductCustomFieldHistory> customFieldHistorys = accProductObj.getGstProductHistory(reqMap);
        for (ProductCustomFieldHistory customFieldHistory : customFieldHistorys) {
            jSONObject = new JSONObject();
            jSONObject.put("id", customFieldHistory.getProduct().getName());
            jSONObject.put("taxclassId", customFieldHistory.getValue());
            jSONObject.put("applydate", customFieldHistory.getApplyDate() != null ? df.format(customFieldHistory.getApplyDate()) : ""); 
            jSONArray.put(jSONObject);  
            if (!gsthistorydetails) {
                break;
            }
        }
        if (jSONArray.length() == 0 && !gsthistorydetails) {
            jSONObject.put("id", "");
            jSONObject.put("taxclassId", "");
            jSONObject.put("applydate", "");
            jSONArray.put(jSONObject);
        }
        data.put("count", jSONArray.length());
        return data.put("data", jSONArray);
    }
    /**
     * Function to get Product's Used history.
     *
     * @param reqMap
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getProductUsedHistory(Map<String, Object> reqMap) throws ServiceException, JSONException {        
        DateFormat df = null;
        try {
            df = authHandler.getOnlyDateFormat();
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccProductModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        JSONObject data = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        List result=accProductObj.getGstProductUsedHistory(reqMap);     
        if(!result.isEmpty()){
            jSONObject.put("isUsedProduct",true);
        }else{
            jSONObject.put("isUsedProduct",false);
        }
        jSONArray.put(jSONObject);
        data.put("count", jSONArray.length());
        return data.put("data", jSONArray);
    }
    /**
     * Save audit trail entry for Product Tax Class History
     * @param paramJObj
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException 
     */
    public JSONObject saveProductGSTHistoryAuditTrail(JSONObject paramJObj) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject returnJSONObj = new JSONObject();
        
        Map<String, Object> auditRequestParamsForGSTHistory = new HashMap<String, Object>();
        auditRequestParamsForGSTHistory.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
        auditRequestParamsForGSTHistory.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
        auditRequestParamsForGSTHistory.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));
        String userName = paramJObj.optString(Constants.userfullname);
        String productID = paramJObj.optString(Constants.productid, "");
        String productName = "";
        if(!StringUtil.isNullOrEmpty(productID)){
            Map<String, Object> map = new HashMap<>();
            map.put("ID", productID);
            Object res = kwlCommonTablesDAOObj.getRequestedObjectFields(Product.class, new String[]{"name"}, map);
            productName = res != null ? (String) res : "";
        }
        String auditMSGForGSTHistory = "";
        String newProductTaxClass = paramJObj.optString("ProductTaxClassValue","");
        
        if(!StringUtil.isNullOrEmpty(newProductTaxClass)){
            Map<String, Object> map = new HashMap<>();
            map.put("id", newProductTaxClass);
            Object res = kwlCommonTablesDAOObj.getRequestedObjectFields(FieldComboData.class, new String[]{"value"}, map);
            newProductTaxClass = res != null ? (String) res : "";
        }
        DateFormat df = authHandler.getDateOnlyFormat();
        String gstapplieddate = paramJObj.optString("gstapplieddate", null);
        Date applyDate = df.parse(gstapplieddate);
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("applyDate", applyDate);
        requestParams.put("productId", productID);
        requestParams.put("isdetails", true);
        requestParams.put("fieldname", "Custom_" + Constants.GSTProdCategory);
        String moduleNameKey = "Product";
        if (paramJObj.optBoolean("isFixedAsset", false)) {
            requestParams.put("moduleid", Constants.Acc_FixedAssets_AssetsGroups_ModuleId);
            moduleNameKey = "Asset Group";
        } else {
            moduleNameKey = "Product";
            requestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);
        }
        String oldProductTaxClass = "";
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
        String newAppliedDateString = sdf.format(applyDate);
        String oldAppliedDateString = "";
        /**
         * Check if same Product Tax Class detail present or not if present then
         * audit trail entry as Product Tax Class
         */
        List<ProductCustomFieldHistory> productHistory = accProductObj.getGstProductHistory(requestParams);
        if (productHistory!=null && !productHistory.isEmpty()) {
            for (ProductCustomFieldHistory gstProductHistory : productHistory) {
                if (gstProductHistory != null) {
                    oldProductTaxClass = gstProductHistory.getValue();
                    if (!StringUtil.isNullOrEmpty(oldProductTaxClass)) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", oldProductTaxClass);
                        Object res = kwlCommonTablesDAOObj.getRequestedObjectFields(FieldComboData.class, new String[]{"value"}, map);
                        oldProductTaxClass = res != null ? (String) res : "";
                    }
                    oldAppliedDateString = sdf.format(gstProductHistory.getApplyDate());
                }
            }
            Object[] msgparams = new Object[]{userName,moduleNameKey,productName,oldProductTaxClass,oldAppliedDateString,newProductTaxClass,newAppliedDateString};
            auditMSGForGSTHistory = messageSource.getMessage("acc.save.product.gstdetails.update.auditTrail", msgparams, Locale.forLanguageTag(paramJObj.optString(Constants.language)));
        }else{
            /**
             * If New Product Tax Class then audit trail entry as added Product Tax Class
             */
            Object[] msgparams = new Object[]{userName,moduleNameKey,productName,newProductTaxClass,newAppliedDateString};
            auditMSGForGSTHistory = messageSource.getMessage("acc.save.product.gstdetails.add.auditTrail", msgparams, Locale.forLanguageTag(paramJObj.optString(Constants.language)));
        }
        auditTrailObj.insertAuditLog("2248", auditMSGForGSTHistory, auditRequestParamsForGSTHistory, productID);
        return returnJSONObj;
    }
    
    /*
     * Method to get assembly products with BOM details.
     * Also we can perform quick search on warehouse.
     */
    @Override
    public JSONObject getAssembyProductBOMDetails(JSONObject paramJobj) throws ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject resultObj = new JSONObject();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            Map<String, JSONObject> serialDetails = null;
            boolean exportfalg = paramJobj.optBoolean("exportfalg",false);
            
            int count = 0;
            paramJobj.put("getSummaryOfAssemblyProduct",true);
            KwlReturnObject retObj = accProductObj.getAssembyProductBOMDetails(paramJobj);
            if (retObj != null && !retObj.getEntityList().isEmpty()) {
                List<Object[]> list = retObj.getEntityList();
                count = retObj.getRecordTotalCount();
                for (Object[] row : list) {
                    String productBuildId = (row[0] !=null && row[0] instanceof String) ? (String)row[0] : "";

                    JSONObject obj = new JSONObject();
                    obj.put("productid", productBuildId);
                    obj.put("billid", productBuildId);    
                    obj.put("mainproductid", (row[0] != null && row[0] instanceof String) ? (String) row[0] : "");
                    obj.put("description", (row[6] != null && row[6] instanceof String) ? (String) row[6] : "");
                    obj.put("productname", (row[1] != null && row[1] instanceof String) ? (String) row[1] : "");
                    double buildQty = (row[4] != null && row[4] instanceof Double) ? (double) row[4] : 0;
                    double consumedQty = (row[5] != null && row[5] instanceof Double) ? (double) row[5] : 0 ;
                    double availableQty = buildQty-consumedQty ;
                    obj.put("buildQty", buildQty);
                    obj.put("consumedQty", consumedQty);
                    obj.put("availableQty", availableQty);
                    obj.put("bomCode", (row[3] != null && row[3] instanceof String) ? (String) row[3] : "");
                    obj.put("bomdetailid", (row[2] != null && row[2] instanceof String) ? (String) row[2] : "");
                    jArr.put(obj);
                }
            }
            resultObj.put(Constants.RES_data, jArr);
            resultObj.put(Constants.RES_TOTALCOUNT, count);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
        }
        return resultObj;
    }
    
}
