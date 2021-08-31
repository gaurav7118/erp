/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.machinemanagement;

import com.krawler.common.admin.CustomizeReportMapping;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public class MachineManagementHandler {
    
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private MessageSource messageSource;
    
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public AccountingHandlerDAO getAccountingHandlerDAOobj() {
        return accountingHandlerDAOobj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    public  JSONObject getMachineMasterRegistryGridInfo(Map<String, Object> requestParams) throws JSONException, ServiceException {
        int colWidth = 100;
        JSONObject jobj = new JSONObject();
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();
        Locale requestcontextutilsobj = null;
        
        if (requestParams.containsKey("requestcontextutilsobj")) {
            requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
        }

        String StoreRec = "id,machinename,machineid,machinetype,deleted,issubstitute,machineserialno,substitutemachinename,substitutemachineid,activemachinenames,activemachineids,machineoperatingcapacity,ageofmachine,process,workcenter,assignedworkorder,"
                + "assignedroutecode,breakdowntrackingid,insuranceduedate,sequenceformatid,isassetmachine,shifttiming";

        String[] recArr = StoreRec.split(",");
        for (String rec : recArr) {
            jobjTemp = new JSONObject();
            jobjTemp.put("name", rec);
            jarrRecords.put(jobjTemp);
        }

        jobjTemp = new JSONObject();

        jobjTemp.put("header", "");
        jobjTemp.put("dataIndex", "id");
        jobjTemp.put("hidden", true);
        jobjTemp.put("hideable", false);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header1", null, requestcontextutilsobj)); //Machine Name
        jobjTemp.put("dataIndex", "machinename");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("sortable", true);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header2", null, requestcontextutilsobj)); //"Machine ID"
        jobjTemp.put("dataIndex", "machineid");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header15", null, requestcontextutilsobj)); //"Machine Type"
        jobjTemp.put("dataIndex", "machinetype");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header3", null, requestcontextutilsobj)); // "Substitute Machine Name",
        jobjTemp.put("dataIndex", "substitutemachinename");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth + 35);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header4", null, requestcontextutilsobj)); // "Substitute Machine ID",
        jobjTemp.put("dataIndex", "substitutemachineid");
        jobjTemp.put("align", "left");
        jobjTemp.put("width", colWidth + 30);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header5", null, requestcontextutilsobj)); // "Operating Capacity",
        jobjTemp.put("dataIndex", "machineoperatingcapacity");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth + 10);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header6", null, requestcontextutilsobj)); // "Age",
        jobjTemp.put("dataIndex", "ageofmachine");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "machineusescount");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "vendorid");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "vendorname");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "hasmachineonlease");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "purchaseaccountid");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "purchaseaccount");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "processid");
        jarrRecords.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "fullMachineTime");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "fullManTime");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "partMachineTime");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "partManTime");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "fulltimeratio");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "parttimeratio");
        jarrRecords.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header7", null, requestcontextutilsobj)); // "Process",
        jobjTemp.put("dataIndex", "process");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "workcenterid");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header8", null, requestcontextutilsobj)); // "Work Centre ID",
        jobjTemp.put("dataIndex", "workcenter");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "dateofpurchase");
        jobjTemp.put("align", "center");
        jobjTemp.put("type", "date");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header9", null, requestcontextutilsobj)); // "Date of Purchase",
        jobjTemp.put("dataIndex", "dateofpurchase");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "dateofinstallation");
        jobjTemp.put("align", "center");
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("type", "date");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header14", null, requestcontextutilsobj)); // "Date of Installation",
        jobjTemp.put("dataIndex", "dateofinstallation");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "maintenanceschedule");
        jobjTemp.put("align", "center");
        jobjTemp.put("pdfwidth", 75);
        jobjTemp.put("type", "date");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header10", null, requestcontextutilsobj)); // "Maintenace Schedule",
        jobjTemp.put("dataIndex", "maintenanceschedule");
        jobjTemp.put("width", colWidth + 30);
        jarrColumns.put(jobjTemp);

//        jobjTemp = new JSONObject();
//        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header11", null, requestcontextutilsobj)); // "Assigned Work Order",
//        jobjTemp.put("dataIndex", "assignedworkorder");
//        jobjTemp.put("width", colWidth + 30);
//        jobjTemp.put("align", "center");
//        jobjTemp.put("pdfwidth", 75);
//        jarrColumns.put(jobjTemp);

//        jobjTemp = new JSONObject();
//        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header12", null, requestcontextutilsobj)); // "Assigned Route Code",
//        jobjTemp.put("dataIndex", "assignedroutecode");
//        jobjTemp.put("width", colWidth + 30);
//        jobjTemp.put("align", "center");
//        jobjTemp.put("pdfwidth", 75);
//        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header13", null, requestcontextutilsobj)); // "Break Down Tracking ID",
        jobjTemp.put("dataIndex", "breakdowntrackingid");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth + 35);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
      
        
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "assetid");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "assetdetailId");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "productname");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "product");
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", "startdateoflease");
        jobjTemp.put("type", "date");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "enddateoflease");
        jobjTemp.put("type", "date");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "machineprice");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "leaseyears");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "depreciationmethod");
        jarrRecords.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("name", "depreciationrate");
        jarrRecords.put(jobjTemp);

        if (requestParams.containsKey("isleasemachine")) {

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header16", null, requestcontextutilsobj)); // "Start Date Of Lease",
            jobjTemp.put("dataIndex", "startdateoflease");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colWidth + 10);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header17", null, requestcontextutilsobj)); // "End Date Of Lease",
            jobjTemp.put("dataIndex", "enddateoflease");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colWidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header18", null, requestcontextutilsobj)); // "Machine Price",
            jobjTemp.put("dataIndex", "machineprice");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colWidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header19", null, requestcontextutilsobj)); // "Lease Years",
            jobjTemp.put("dataIndex", "leaseyears");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colWidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header20", null, requestcontextutilsobj)); // "Depreciation Method",
            jobjTemp.put("dataIndex", "depreciationmethod");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colWidth + 10);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header21", null, requestcontextutilsobj)); // "Depreciation Rate",
            jobjTemp.put("dataIndex", "depreciationrate");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colWidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

        }
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header22", null, requestcontextutilsobj)); // "Depreciation Rate",
        jobjTemp.put("dataIndex", "fulltimeratio");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header23", null, requestcontextutilsobj)); // "Depreciation Rate",
        jobjTemp.put("dataIndex", "parttimeratio");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        
        jobjTemp = new JSONObject();
        jobjTemp.put("header", messageSource.getMessage("acc.machineMasterGrid.header24", null, requestcontextutilsobj)); // Shift Timings
        jobjTemp.put("dataIndex", "shifttiming");
        jobjTemp.put("align", "center");
        jobjTemp.put("width", colWidth);
        jobjTemp.put("pdfwidth", 75);
        jarrColumns.put(jobjTemp);
        
        /*
         Add Custom Fields in Column Model
         */
        String companyId = (String) requestParams.get("companyid");
        requestParams.put("companyId", companyId);
        requestParams.put("reportId", Constants.MRP_Machine_Management_ModuleId);
        putCustomColumnForMachine(jarrColumns, jarrRecords, requestParams);
        
        jMeta.put("totalProperty", "totalCount");
        jMeta.put("root", "coldata");
        jMeta.put("fields", jarrRecords);
        commData.put("columns", jarrColumns);
        commData.put("metadata", jMeta);
        return commData;
    }
    
        public void putCustomColumnForMachine(JSONArray jarrColumns, JSONArray jarrRecords, Map<String, Object> requestParams) throws ServiceException {
        try {
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>(requestParams);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(requestParams1);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List arrayList = new ArrayList();
            for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                String column = "Custom_" + customizeReportMapping.getDataIndex();
                if (!arrayList.contains(customizeReportMapping.getDataIndex())) {
                    JSONObject jobjTemp = new JSONObject();
                    jobjTemp.put("name", column);
                    jarrRecords.put(jobjTemp);
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", customizeReportMapping.getDataHeader());
                    jobjTemp.put("dataIndex", column);
                    jobjTemp.put("width", 150);
                    jobjTemp.put("pdfwidth", 150);
                    jobjTemp.put("custom", "true");
                    jarrColumns.put(jobjTemp);
                    arrayList.add(customizeReportMapping.getDataIndex());
                }
            }
        } catch (ServiceException | JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
}
