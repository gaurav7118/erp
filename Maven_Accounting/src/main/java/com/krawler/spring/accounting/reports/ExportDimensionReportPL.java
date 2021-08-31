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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  0in 2110-1301, USA.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;

import com.krawler.common.admin.*;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileOutputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import java.text.SimpleDateFormat;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
/**
 *
 * @author krawler
 */
public class ExportDimensionReportPL implements Runnable{
    
    private HibernateTransactionManager txnManager;
    private AccReportsService accReportsService;
    private AccFinancialReportsService accFinancialReportsService;
    private ExportXlsServiceHandler exportXlsservicehandlerobj;
    private Company company;
    private String filename;
    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;
    private accProductDAO accProductObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    

    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }
    public void setAccFinancialReportsService(AccFinancialReportsService accFinancialReportsService) {
        this.accFinancialReportsService = accFinancialReportsService;
    }


    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ExportXlsServiceHandler getExportXlsservicehandlerobj() {
        return exportXlsservicehandlerobj;
    }

    public void setExportXlsservicehandlerobj(ExportXlsServiceHandler exportXlsservicehandlerobj) {
        this.exportXlsservicehandlerobj = exportXlsservicehandlerobj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setAccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
        
    public void add(HashMap<String, Object> requestParams) {
        try {
            processQueue.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(ExportDimensionReportPL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            while (!processQueue.isEmpty() && !isworking) {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setName("ExportDImensionReportPropagation_Tx");
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
                TransactionStatus status = txnManager.getTransaction(def);
                FileOutputStream outputStream = null;
                HashMap<String, Object> requestParams = (HashMap<String, Object>) processQueue.get(0);
                JSONObject paramJobj = requestParams.containsKey("paramsJsonObject") ? (JSONObject) requestParams.get("paramsJsonObject") : new JSONObject();
                isworking = true;
                
                String companyid = paramJobj.getString(Constants.companyKey);
                KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                company = (Company) cmpresult.getEntityList().get(0);
                try {
                    JSONObject jobj = new JSONObject();
                    String fileType = paramJobj.optString("filetype", "csv");
                    Date requestTime = new Date();
                    SimpleDateFormat sdfTemp = new SimpleDateFormat("ddMMyyyy_hhmmssaa");
                    filename = "Dimension_Report_ProfitAndLoss_" + (sdfTemp.format(requestTime)).toString();
                    String filePath = StorageHandler.GetDocStorePath() + filename + "."+fileType;
                    HashMap<String, Object> exportDetails = new HashMap<String, Object>();
                    exportDetails.put("fileName", filename + "." + fileType);
                    exportDetails.put("requestTime", requestTime);
                    exportDetails.put("status", 1);
                    exportDetails.put("companyId", companyid);
                    exportDetails.put("fileType", fileType);
                    KwlReturnObject resultExportObj = accProductObj.saveProductExportDetails(exportDetails);
                    ProductExportDetail productExportDetail = (ProductExportDetail) resultExportObj.getEntityList().get(0);
                    txnManager.commit(status); 
                    status = txnManager.getTransaction(def);//defining new status to update
                    
                    paramJobj.put("isForTradingAndProfitLoss", true);
                    boolean isCustomLayout = !StringUtil.isNullOrEmpty(paramJobj.optString("isCustomLayout", null)) ? Boolean.parseBoolean(paramJobj.optString("isCustomLayout")) : false;
                    JSONObject fobj1 = accFinancialReportsService.getDimesionBasedProfitLoss(paramJobj, true);
                    JSONObject fobj = new JSONObject();
                    JSONArray jArrL = fobj1.getJSONArray("refleft");
                    JSONArray jArrR = fobj1.getJSONArray("refright");
                    fobj.put("left", jArrL);
                    fobj.put("right", jArrR);
                    jobj.put(Constants.data, fobj);
                    if (!isCustomLayout) {
                        jobj = accFinancialReportsService.getNewMonthlyMYOBtradingreport(paramJobj, jobj, true);
                    } else {// sorting JSON
                        requestParams = new HashMap<>();
                        requestParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
                        requestParams.put("templateid", paramJobj.optString("templateid", null));
                        jobj = accReportsService.getOrderedJSONForDimensionBasedCustomLayout(requestParams, jobj, true);
                    }
                    JSONArray jarrColumns = fobj1.getJSONArray("jarrColumns");
                    JSONObject gridconfig = new JSONObject();
                    JSONArray jarrRecords = fobj1.getJSONArray("jarrRecords");
                    gridconfig.put(Constants.data, jarrColumns);
                    gridconfig.put("headers", jarrRecords);

                    paramJobj.put("gridconfig", gridconfig);
                    jobj.put(Constants.data, jobj.getJSONArray(Constants.data));
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                    exportXlsservicehandlerobj.GenerateXLSFile(paramJobj, jobj, productExportDetail,filename,fileType);
                    txnManager.commit(status);
                } catch (JSONException ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);
                } catch (Exception ex) {
                    txnManager.rollback(status);
                    Logger.getLogger(ExportLedger.class.getName()).log(Level.SEVERE, null, ex);

                } finally {
                    processQueue.remove(requestParams);
                    isworking = false;
                    System.out.println("Done");
                    if (outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
