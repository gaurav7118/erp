/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.spring.mrp.taskProgressManagement;

import com.krawler.common.service.ServiceException;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

/**
 *
 * @author krawler
 */
public class AccTaskProgressManagementServiceImpl implements AccTaskProgressManagementServiceDAO {
    
    
    private MessageSource messageSource;
    
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

   @Override
    public JSONObject getTaskProgressDetails(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        Locale requestcontextutilsobj=null;
        
        try {

            int count = 0;//result.getRecordTotalCount();

            if(requestParams.containsKey("requestcontextutilsobj")){
                requestcontextutilsobj=(Locale)requestParams.get("requestcontextutilsobj");
            }
            
            // Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            String StoreRec = "id,taskname,duration,startdate,enddate,progress,resourcename,assignedworkorder,assignedroutecode,machineid,workcentre,materialconsumed";
         
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
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header1", null, requestcontextutilsobj)); //"Task Name"
            jobjTemp.put("dataIndex", "taskname");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header2", null, requestcontextutilsobj)); //"Duration"
            jobjTemp.put("dataIndex", "duration");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header3", null, requestcontextutilsobj)); // "Start Date",
            jobjTemp.put("dataIndex", "startdate");
            jobjTemp.put("align", "center");
            jobjTemp.put("type", "date");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header4", null, requestcontextutilsobj)); // "End Date",
            jobjTemp.put("dataIndex", "enddate");
            jobjTemp.put("align", "center");
            jobjTemp.put("type", "date");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header5", null, requestcontextutilsobj)); // "Projress(%)",
            jobjTemp.put("dataIndex", "progress");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header6", null, requestcontextutilsobj)); // "Resource Name",
            jobjTemp.put("dataIndex", "resourcename");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header7", null, requestcontextutilsobj)); // "Assigned Work Order",
            jobjTemp.put("dataIndex", "assignedworkorder");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header8", null, requestcontextutilsobj)); // "Assigned Route Code",
            jobjTemp.put("dataIndex", "assignedroutecode");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header9", null, requestcontextutilsobj)); // "Machine ID",
            jobjTemp.put("dataIndex", "machineid");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header10", null, requestcontextutilsobj)); // "Work Centre",
            jobjTemp.put("dataIndex", "workcentre");
            jobjTemp.put("align", "left");
            jobjTemp.put("width", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.header11", null, requestcontextutilsobj)); // "Material Consumed",
            jobjTemp.put("dataIndex", "materialconsumed");
            jobjTemp.put("width", 75);
            jobjTemp.put("align", "center");
            jobjTemp.put("renderer", "this.linkViewEditRenderer");
            jarrColumns.put(jobjTemp);


            JSONObject jSONObject = new JSONObject();
            jSONObject.put("id", "1");
            jSONObject.put("taskname", "Cutting");
            jSONObject.put("duration", "3 Days");
            jSONObject.put("startdate", "2016-03-12");
            jSONObject.put("enddate", "2016-03-15");
            jSONObject.put("progress", "50");
            jSONObject.put("resourcename", "Alex Wu");
            jSONObject.put("assignedworkorder", "WO001");
            jSONObject.put("assignedroutecode", "Alex Wu");
            jSONObject.put("machineid", "M001");
            jSONObject.put("workcentre", "CNC Cutting");
            jSONObject.put("materialconsumed", "View/Edit Details");
            dataJArr.put(jSONObject);
            
            jSONObject = new JSONObject();
            jSONObject.put("id", "2");
            jSONObject.put("taskname", "Moulding");
            jSONObject.put("duration", "4 Days");
            jSONObject.put("startdate", "2016-03-15");
            jSONObject.put("enddate", "2016-03-19");
            jSONObject.put("progress", "45");
            jSONObject.put("resourcename", "John Smith");
            jSONObject.put("assignedworkorder", "WO002");
            jSONObject.put("assignedroutecode", "John Smith");
            jSONObject.put("machineid", "M002");
            jSONObject.put("workcentre", "Heating & Moulding");
            jSONObject.put("materialconsumed", "View/Edit Details");
            dataJArr.put(jSONObject);

            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", count);
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (false) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccTaskProgressManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jobj;
    }
   
    @Override
    public JSONObject getMaterialConsumedDetails(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        Locale requestcontextutilsobj=null;
        
        try {

            int count = 0;//result.getRecordTotalCount();

            if(requestParams.containsKey("requestcontextutilsobj")){
                requestcontextutilsobj=(Locale)requestParams.get("requestcontextutilsobj");
            }
            
            // Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            String StoreRec = "id,productid,desc,producttype,purchaseprice,quantity,percentage,actualquantity,total,action";
         
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
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.materialConsumed.header1", null, requestcontextutilsobj)); //"Inventory ID"
            jobjTemp.put("dataIndex", "productid");
            jobjTemp.put("editor", "this.productEditor");
            jobjTemp.put("renderer", "this.productRenderer(this.productEditor)");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.materialConsumed.header2", null, requestcontextutilsobj)); //"Description"
            jobjTemp.put("dataIndex", "desc");
            jobjTemp.put("width", 100);
            jobjTemp.put("pdfwidth", 100);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.materialConsumed.header3", null, requestcontextutilsobj)); // "Product Type",
            jobjTemp.put("dataIndex", "producttype");
            jobjTemp.put("type", "date");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.materialConsumed.header4", null, requestcontextutilsobj)); // "Initial Purchase Price",
            jobjTemp.put("dataIndex", "purchaseprice");
            jobjTemp.put("renderer", "WtfGlobal.currencyRenderer");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 75);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.materialConsumed.header5", null, requestcontextutilsobj)); // "Quantity",
            jobjTemp.put("dataIndex", "quantity");
            jobjTemp.put("renderer", "WtfGlobal.quantityRenderer");            
            jobjTemp.put("editor", "this.quantityEditor");            
            jobjTemp.put("width", 50);
            jobjTemp.put("pdfwidth", 50);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.materialConsumed.header6", null, requestcontextutilsobj)); // "Percentage",
            jobjTemp.put("dataIndex", "percentage");
            jobjTemp.put("editor", "this.percentageEditor");            
            jobjTemp.put("renderer", "this.percentageRenderer.createDelegate(this)");
            jobjTemp.put("width", 50);
            jobjTemp.put("pdfwidth", 50);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.materialConsumed.header7", null, requestcontextutilsobj)); // "Actual Quantity",
            jobjTemp.put("dataIndex", "actualquantity");
            jobjTemp.put("renderer", "this.actualQuantityRenderer.createDelegate(this)");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 65);
            jobjTemp.put("pdfwidth", 65);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.materialConsumed.header8", null, requestcontextutilsobj)); // "Total",
            jobjTemp.put("renderer", "this.subTotalRenderer.createDelegate(this)");
            jobjTemp.put("dataIndex", "total");
            jobjTemp.put("align", "right");
            jobjTemp.put("width", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.taskProgressGrid.materialConsumed.header9", null, requestcontextutilsobj)); // "Action",
            jobjTemp.put("renderer", "this.deleteRenderer.createDelegate(this)");
            jobjTemp.put("width", 35);
            jarrColumns.put(jobjTemp);

            JSONObject jSONObject = new JSONObject();
            jSONObject.put("id", "1");
            jSONObject.put("productid", "IP-LR-LS-2451");
            jSONObject.put("desc", "Sample Description");
            jSONObject.put("producttype", "Inventory Part");
            jSONObject.put("purchaseprice", "500");
            jSONObject.put("quantity", "1");
            jSONObject.put("percentage", "20");
            jSONObject.put("actualquantity", "0");
            jSONObject.put("total", "0");
            dataJArr.put(jSONObject);

            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", count);
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (false) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (JSONException | NoSuchMessageException ex) {
            Logger.getLogger(AccTaskProgressManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jobj;
    }

}
