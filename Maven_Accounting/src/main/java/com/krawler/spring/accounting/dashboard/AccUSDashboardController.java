/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.dashboard;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AccUSDashboardController extends MultiActionController implements MessageSourceAware {

    private MessageSource messageSource;
    private AccUSDashboardService accUSDashboardService;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setAccUSDashboardService(AccUSDashboardService accUSDashboardService) {
        this.accUSDashboardService = accUSDashboardService;
    }

    public ModelAndView saveDashboard(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);

            jobj = accUSDashboardService.saveDashboard(paramJobj);

            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(AccUSDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccUSDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView getDashboard(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);

            jobj = accUSDashboardService.getDashboard(paramJobj);
            
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(AccUSDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccUSDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView setActiveDashboard(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);

            jobj = accUSDashboardService.setActiveDashboard(paramJobj);

            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(AccUSDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccUSDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    /*
     * Function to get inventory details of product. Used from configured
     * product view.
     */
    public ModelAndView getProductViewInvDetails(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);

            jobj = accUSDashboardService.getProductViewInvDetails(paramJobj);

            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(AccUSDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccUSDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getWidgets(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            JSONArray dataArr = new JSONArray();
            JSONObject dataObj = null;
            JSONObject properties = new JSONObject();
            JSONObject source = new JSONObject();
            JSONObject customEditor = new JSONObject();
            source.put("No of Persons",5);
            source.put("Type","Pie");
            source.put("Show Legends",true);
            source.put("Title","Top Products");
            customEditor.put("Type","{\"xtype\": \"combo\","
                + "\"store\": [\"Pie\", \"Bar\", \"Line\"],"
                + "\"forceSelection\": true,"
                + "\"allowBlank\": false"
                + "}");
            properties.put("source",source);
            properties.put("customEditor",customEditor);

            dataObj = new JSONObject();
            dataObj.put("id", "1");
            dataObj.put("name", "Top Products Chart");
            dataObj.put("chartname", "Top Products");
            dataObj.put("type", "chart");
            dataObj.put("subtype", "pie");
            dataObj.put("properties", properties);
            dataObj.put("dataUrl", "ACCInvoiceCMN/getTopProductsGraphical.do");
            dataObj.put("params", "{isTopProducts: true,isTopCustomers: false,isTopAgents: false,countNumber: 5,isForChart: true}");
            dataObj.put("description", "Top Products in current financial year.");

            dataArr.put(dataObj);
            
            properties = new JSONObject();
            source = new JSONObject();
            customEditor = new JSONObject();
            source.put("No of Persons",5);
            source.put("Type","Pie");
            source.put("Show Legends",true);
            source.put("Title","Top Customers");
            customEditor.put("Type","{\"xtype\": \"combo\","
                + "\"store\": [\"Pie\", \"Bar\", \"Line\"],"
                + "\"forceSelection\": true,"
                + "\"allowBlank\": false"
                + "}");
            properties.put("source",source);
            properties.put("customEditor",customEditor);
            
            dataObj = new JSONObject();
            dataObj.put("id", "2");
            dataObj.put("name", "Top Customers Chart");
            dataObj.put("chartname", "Top Customers");
            dataObj.put("type", "chart");
            dataObj.put("subtype", "pie");
            dataObj.put("properties", properties);
            dataObj.put("dataUrl", "ACCInvoiceCMN/getTopCustomersGraphical.do");
            dataObj.put("params", "{isTopProducts: false,isTopCustomers: true,isTopAgents: false,countNumber: 5,isForChart: true}");
            dataObj.put("description", "Top Customers in current financial year.");

            dataArr.put(dataObj);
            
            properties = new JSONObject();
            source = new JSONObject();
            customEditor = new JSONObject();
            source.put("No of Persons",5);
            source.put("Type","Pie");
            source.put("Show Legends",true);
            source.put("Title","Top Sales Representative");
            customEditor.put("Type","{\"xtype\": \"combo\","
                + "\"store\": [\"Pie\", \"Bar\", \"Line\"],"
                + "\"forceSelection\": true,"
                + "\"allowBlank\": false"
                + "}");
            properties.put("source",source);
            properties.put("customEditor",customEditor);
            
            dataObj = new JSONObject();
            dataObj.put("id", "3");
            dataObj.put("name", "Top Sales Representative Chart");
            dataObj.put("chartname", "Top Sales Representative");
            dataObj.put("type", "chart");
            dataObj.put("subtype", "pie");
            dataObj.put("properties", properties);
            dataObj.put("dataUrl", "ACCInvoiceCMN/getSalesReportRepGraphical.do");
            dataObj.put("params", "{isTopProducts: false,isTopCustomers: false,isTopAgents: true,countNumber: 5,isForChart: true}");
            dataObj.put("description", "Top Sales Representative in current financial year.");

            dataArr.put(dataObj);
            
            dataObj = new JSONObject();
            dataObj.put("id", "4");
            dataObj.put("name", "Aged Receivables Chart");
            dataObj.put("chartname", "Aged Receivables");
            dataObj.put("type", "chart");
            dataObj.put("subtype", "clustered_bar");
            dataObj.put("dataUrl", "ACCGoodsReceiptCMN/getAccountReceivedChartGraphical.do");
            dataObj.put("params", "{personlimit: 1,creditonly: true,persongroup: false,isagedgraph: true,withinventory: true,nondeleted: true,deleted: false}");
            dataObj.put("description", "Aged Receivables in current financial year.");
            dataArr.put(dataObj);
            
            dataObj = new JSONObject();
            dataObj.put("id", "5");
            dataObj.put("name", "Aged Payable Chart");
            dataObj.put("chartname", "Aged Payable");
            dataObj.put("type", "chart");
            dataObj.put("subtype", "clustered_bar");
            dataObj.put("dataUrl", "ACCGoodsReceiptCMN/getAccountPayableChartGraphical.do");
            dataObj.put("params", "{personlimit: 1,creditonly: true,persongroup: false,isagedgraph: true,withinventory: true,nondeleted: true,deleted: false}");
            dataObj.put("description", "Aged Payable in current financial year.");

            dataArr.put(dataObj);
            
            dataObj = new JSONObject();
            dataObj.put("id", "6");
            dataObj.put("name", "Monthly Trend");
            dataObj.put("chartname", "Monthly Trend");
            dataObj.put("type", "chart");
            dataObj.put("subtype", "clustered_line");
            dataObj.put("dataUrl", "ACCInvoiceCMN/getMonthlySalesReportGraphical.do");
            dataObj.put("params", "{consolidateFlag: false,creditonly: false,dir: 'ASC',enddate: 'December, 2016',getRepeateInvoice: false,limit: 5,mode: 18,nondeleted: true,start: 0,stdate: 'January, 2016'}");
            dataObj.put("description", "Monthly Trend");

            dataArr.put(dataObj);
            
            dataObj = new JSONObject();
            dataObj.put("id", "7");
            dataObj.put("name", "Monthly Trading & Profit/Loss");
//            dataObj.put("chartname", "Monthly Trend");
            dataObj.put("type", "grid");
            dataObj.put("subtype", "hierachical");
            dataObj.put("dataUrl", "ACCReports/getMonthlyTradingAndProfitLoss.do?isWidgetRequest=true&isHierachicalGrid=true&Nature=0&costcenter=&enddate=December, 2016&mode=65&nondeleted=true&reportView=MonthlyTradingAndProfitLoss&singleGrid=true&stdate=January, 2016");
//            dataObj.put("dataUrl", "ACCReports/getMonthlyTradingAndProfitLoss.do");
            dataObj.put("params", "{Nature:0,enddate:'December, 2016',isHierachicalGrid:true,isWidgetRequest:true,mode:65,nondeleted:true,reportView:'MonthlyTradingAndProfitLoss',singleGrid:true,stdate:'January, 2016'}");
            dataObj.put("description", "Monthly Trading & Profit/Loss");

            dataArr.put(dataObj);
            
            dataObj = new JSONObject();
            dataObj.put("id", "8");
            dataObj.put("name", "Yearly Trading & Profit/Loss");
//            dataObj.put("chartname", "Monthly Trend");
            dataObj.put("type", "grid");
            dataObj.put("subtype", "hierachical");
            dataObj.put("dataUrl", "ACCOtherReports/getYearlyTradingAndProfitLoss.do?isWidgetRequest=true&isHierachicalGrid=true&Nature=0&costcenter=&enddate=December, 2016&mode=65&nondeleted=true&reportView=MonthlyTradingAndProfitLoss&singleGrid=true&stdate=January, 2016");
//            dataObj.put("dataUrl", "ACCReports/getMonthlyTradingAndProfitLoss.do");
            dataObj.put("params", "{Nature:0,enddate:'December, 2016',isHierachicalGrid:true,isWidgetRequest:true,mode:65,nondeleted:true,reportView:'MonthlyTradingAndProfitLoss',singleGrid:true,stdate:'January, 2016'}");
            dataObj.put("description", "Yearly Trading & Profit/Loss");

            dataArr.put(dataObj);

            jobj.put(Constants.RES_success, true);
            jobj.put("valid", true);
            jobj.put(Constants.RES_data, dataArr);

            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(AccUSDashboardController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccUSDashboardController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
}