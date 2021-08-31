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
package com.krawler.spring.accounting.customDesign;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
//import com.lowagie.text.Table;
import java.io.StringWriter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
/**
 *
 * @author krawler
 */
public class CustomDesignController extends MultiActionController {

    private CustomDesignDAO customDesignDAOObj;
    private VelocityEngine velocityEngine;
    public ImportHandler importHandler;
    private CustomDesignServiceDao customDesignServiceobj;

    public void setCustomDesignServiceobj(CustomDesignServiceDao customDesignServiceobj) {
        this.customDesignServiceobj = customDesignServiceobj;
    }
    public void setImportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setcustomDesignDAO(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }

    public void setvelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public ModelAndView getDesignTemplateList(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put(Constants.moduleArray, request.getParameterValues(Constants.moduleid));
        jobj = customDesignServiceobj.getDesignTemplateList(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView getDesignTemplate(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = customDesignServiceobj.getDesignTemplate(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView getJobOrderDesignTemplate(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = customDesignServiceobj.getJobOrderDesignTemplate(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView getQAApprovalDesignTemplate(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = customDesignServiceobj.getQAApprovalDesignTemplate(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView saveDesignTemplate(HttpServletRequest request, HttpServletResponse response) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = customDesignServiceobj.saveDesignTemplate(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
                    
    public ModelAndView createTemplate(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = customDesignServiceobj.createTemplate(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    /*Neeraj delete function*/
    public ModelAndView deleteCustomTemplatemodule(HttpServletRequest request, HttpServletResponse response) throws SecurityException, ServiceException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = customDesignServiceobj.deleteCustomTemplatemodule(paramJobj);
        return new ModelAndView(Constants.jsonView, "model", jobj.toString());
    }

    public ModelAndView getActiveDesignTemplateList(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        paramJobj.put(Constants.moduleArray, request.getParameterValues(Constants.moduleid));
        jobj = customDesignServiceobj.getActiveDesignTemplateList(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView saveActiveModeTemplate(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException  {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = customDesignServiceobj.saveActiveModeTemplate(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public ModelAndView copyTemplate(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jobj = customDesignServiceobj.copyTemplate(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }

    public ModelAndView getGroupingFields(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jarr = customDesignServiceobj.getGroupingFields(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jarr.toString());
    }
    /**
     * Get fields of Details Table based on SubType
     * @param request
     * @param response
     * @return
     * @throws JSONException
     * @throws SessionExpiredException 
     */
    public ModelAndView getDetailsTableFields(HttpServletRequest request, HttpServletResponse response) throws JSONException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        jarr = customDesignServiceobj.getDetailsTableFields(paramJobj);
        return new ModelAndView(Constants.jsonView, Constants.model, jarr.toString());
    }
    
        public ModelAndView getGlobalFieldsData(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = customDesignServiceobj.getGlobalFieldsData(paramJobj);
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView getLineFieldsData(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, JSONException {

        JSONObject jobj = new JSONObject();
        if (sessionHandlerImpl.isValidSession(request, response)) {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = customDesignServiceobj.getLineFieldsData(paramJobj);
        } else {
            jobj.put("success", false);
            jobj.put("msg", "timeout");
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
    
    public ModelAndView showSamplePreview(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView-empty";
        try {
            int moduleid = 0;
            String fontstylevalue = "";
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            String fileType = paramJobj.optString("filetype",null);
            JSONArray lineItems = new JSONArray();
            String alignment = "", backgroundcolor = "", changedlabel = "";
            String pagelayoutproperty = "", pagefooterhtml = "", pageheaderhtml = "";
            Boolean showtotal = false,checkfooterheader=false;
            List totallist = new ArrayList();
            int aboveLineItemObject_YAxis = 0;
            String companyid = paramJobj.getString(Constants.companyKey);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.moduleid,null))) {
                moduleid = Integer.parseInt(paramJobj.optString(Constants.moduleid,"0"));
            }
            String templateid = paramJobj.optString("templateid",null);
            String fieldIds = "";
            int lineItem_YAxis = 0;
            int lineitemheight = 0;
            int lineitemwidth = 850;

            /*
             * below "bottomItems" variable used to identify items which are
             * appearing after line item grid
             */

            HashMap<String, Integer> bottomItems = new HashMap<String, Integer>();
            String html = "", json = "[]";
            if (!StringUtil.isNullOrEmpty(templateid)) {
                KwlReturnObject result = customDesignDAOObj.getDesignTemplate(templateid);
                List list = result.getEntityList();
                Object[] rows = (Object[]) list.get(0);
                html = rows[2].toString();
                json = rows[1].toString();
                pagelayoutproperty = rows[4] != null ? rows[4].toString() : "";
            } else {
                html = paramJobj.optString("html",null);
                json = paramJobj.optString("json",null);
            }
            JSONArray jArr = new JSONArray(json);
            for (int cnt = 0; cnt < jArr.length(); cnt++) {
                JSONObject jObj = jArr.getJSONObject(cnt);
                if (!StringUtil.isNullOrEmpty(jObj.optString("fieldid", ""))) {
                    fieldIds += "'" + jObj.getString("fieldid") + "',";
                } else if (!StringUtil.isNullOrEmpty(jObj.optString("lineitems", ""))) {
                    lineItems = new JSONArray(jObj.optString("lineitems", "[]"));
                    lineitemheight = jObj.optInt("height", 60);
                    lineitemwidth = jObj.optInt("width", 850);
                    lineItem_YAxis = jObj.optInt("y", -1);
                }
                bottomItems.put(jObj.getString("id"), jObj.optInt("y", 0));
            }
            if (!fieldIds.isEmpty()) {
                fieldIds = fieldIds.substring(0, fieldIds.length() - 1);

                KwlReturnObject result = customDesignDAOObj.getDummyValue(fieldIds);
                List list = result.getEntityList();
                for (int cnt = 0; cnt < list.size(); cnt++) {
                    Object[] rows = (Object[]) list.get(cnt);
                    html = html.replace("#" + rows[0].toString() + "#", rows[1].toString());
                }
            }


            /*
             * Replace static Global Fields like - Total amount, total discount,
             * Today's Date, Amount in Word, Curreny Symbol
             */
            if (moduleid != 14 && moduleid != 16) {
                HashMap<String, String> summaryFields = LineItemColumnModuleMapping.InvoiceProductSummaryItems;
                for (Map.Entry<String, String> entry : summaryFields.entrySet()) {
                    JSONObject staticColInfo = new JSONObject(summaryFields.get(entry.getKey()));
                    if (staticColInfo.getString("xtype").equals("2")) {
                        html = html.replace("#" + staticColInfo.getString("label") + "#", "0.00");
                    } else {
                        html = html.replace("#" + staticColInfo.getString("label") + "#", staticColInfo.getString("label"));
                    }
                }
            }
            /*
             * Gloabla Custom fields are not stored in default header. So we
             * don't have dummy value. Removing start and end #
             */

            KwlReturnObject result = customDesignDAOObj.getGlobalCustomFields(companyid, moduleid);
            List list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                if (row[2].toString().equals("2")) {
                    html = html.replace("#" + row[1].toString() + "#", "0.00");
                } else {
                    html = html.replace("#" + row[1].toString() + "#", row[1].toString());
                }
            }


            /*
             * Create jsoup object for html reader
             */

            Document jsoupDoc = Jsoup.parse(html);
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-south x-component-handle x-component-handle-south x-component-handle-south-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-north x-component-handle x-component-handle-north x-component-handle-north-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-east x-component-handle x-component-handle-east x-component-handle-east-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-west x-component-handle x-component-handle-west x-component-handle-west-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-northeast x-component-handle x-component-handle-northeast x-component-handle-northeast-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-northwest x-component-handle x-component-handle-northwest x-component-handle-northwest-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-southeast x-component-handle x-component-handle-southeast x-component-handle-southeast-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-southwest x-component-handle x-component-handle-southwest x-component-handle-southwest-br x-unselectable").remove();

            /*
             * find out top and bottom items corresponding to Line items
             */
            HashMap<String, String> topHTMLItems = new HashMap<String, String>();
            HashMap<String, String> bottomHTMLItems = new HashMap<String, String>();
            if (lineItems.length() > 0) {
                ValueComparator bvc = new ValueComparator(bottomItems);
                TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
                sorted_map.putAll(bottomItems);
                for (Map.Entry<String, Integer> entry : sorted_map.entrySet()) {
//                    System.out.println(entry.getKey() + "/" + entry.getValue());
                    Element itemListElement = jsoupDoc.getElementById(entry.getKey());
                    if (lineItem_YAxis == entry.getValue()) // if line items
                    {
                        continue;
                    }
                    if (lineItem_YAxis > entry.getValue()) {
                        sorted_map.remove(entry.getKey());
                        if (!itemListElement.getElementsByTag("img").isEmpty()) {
                            org.jsoup.nodes.Element parent = itemListElement.parent();
                            topHTMLItems.put(entry.getKey(), parent.outerHtml());
                        } else {
                            topHTMLItems.put(entry.getKey(), itemListElement.outerHtml());
                        }
                    } else {
                        Attributes styleAtt;
                        if (!itemListElement.getElementsByTag("img").isEmpty()) {
                            Element parent = itemListElement.parent();
                            styleAtt = parent.attributes();
                        } else {
                            styleAtt = itemListElement.attributes();
                        }
                        List<Attribute> attList = styleAtt.asList();
                        for (Attribute a : attList) {
                            if (a.getKey().equals("style")) {
                                String newValue = "";
                                String[] items = a.getValue().trim().split(";");
                                for (String item : items) {
                                    String[] itemValues = item.split(":");
                                    if (!itemValues[0].trim().equals("top")) {
                                        newValue = newValue.concat(item).concat(";");
//                                        top = itemValues[1];              
                                    } else {
                                        int ch = 0;
                                        ch = ((int) (Double.parseDouble(itemValues[1].replace("px", "").trim())) - (lineItem_YAxis + lineitemheight));
                                        newValue = newValue.concat("top:" + ch).concat("px;");
                                    }
                                }
                                a.setValue(newValue);
                                break;
                            }
                        }
                        if (!itemListElement.getElementsByTag("img").isEmpty()) {
                            Element parent = itemListElement.parent();
                            bottomHTMLItems.put(entry.getKey(), parent.outerHtml());
                        } else {
                            bottomHTMLItems.put(entry.getKey(), itemListElement.outerHtml());
                        }

                    }
                    jsoupDoc.getElementById(entry.getKey()).remove();
                }
            }
            String buildhtml = html;

            /*
             * In below loop we are building HTML by appending Top Items, line
             * Iems and Bottom Items respectively
             */

            if (lineItems.length() > 0) {
                buildhtml = "<div style='display:table-cell;height:100%;vertical-align:top;'>";

                /*
                 * append Top Items
                 */
                String topDiv = " <div style='position:relative; width:" + CustomDesignHandler.pageWidth + "; height:" + lineItem_YAxis + "px;'>";
                for (Map.Entry<String, String> entry : topHTMLItems.entrySet()) {
                    String topitem = entry.getValue();
                    topDiv = topDiv.concat(entry.getValue());
                }
                topDiv += "</div>";
                buildhtml += topDiv;

                /*
                 * append Line Items
                 */
                jsoupDoc = Jsoup.parse(html);
                Elements itemListElement = jsoupDoc.getElementsByClass("tpl-content");
                if (!itemListElement.isEmpty()) {
                    String top = "0px", left = "0px", tablewidth = String.valueOf(lineitemwidth);
                    Element mainDiv = itemListElement.first();
                    Attributes styleAtt = mainDiv.attributes();
                    List<Attribute> attList = styleAtt.asList();
                    for (Attribute a : attList) {
                        if (a.getKey().equals("style")) {
                            String[] items = a.getValue().trim().split(";");
                            for (String item : items) {
                                String[] itemValues = item.split(":");
                                if (itemValues[0].trim().equals("top")) {
                                    top = itemValues[1];
                                } else if (itemValues[0].trim().equals("left")) {
                                    left = itemValues[1];
                                } else if (itemValues[0].trim().equals("width")) {
                                    tablewidth = itemValues[1];
                                }
                            }
                        }
                    }

                    /* Configure Line Item Data- get line item columns configuration */

                    ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                    ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
                    for (int headerCnt = 0; headerCnt < lineItems.length(); headerCnt++) {
                        CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        JSONObject colInfo = lineItems.getJSONObject(headerCnt);
                        String headerproperty = colInfo.optString("headerproperty", "");
                        if (!headerproperty.equals("")) {
                            JSONObject jobjheader = new JSONObject(headerproperty);
                            alignment = jobjheader.getString("alignment");
                            backgroundcolor = jobjheader.getString("backgroundcolor");
                            changedlabel = jobjheader.getString("changedlabel");
                        }
                        if (colInfo.getString("xtype").equals("2")) {
                            if (!headerproperty.equals("")) {
                                headerprop.setData(changedlabel);
                                headerprop.setAlign(alignment);
                                headerprop.setBgcolor(backgroundcolor);
                            } else {
                                headerprop.setData(colInfo.getString("label"));
                                headerprop.setAlign("left");
                                headerprop.setBgcolor("#C0C0C0");
                            }
                        } else if (!headerproperty.equals("")) {//if headerproperty is not empty
                            headerprop.setData(changedlabel);
                            headerprop.setAlign(alignment);
                            headerprop.setBgcolor(backgroundcolor);
                        } else {//headerproperty is empty
                            headerprop.setData(colInfo.optString("label", ""));
                            headerprop.setAlign("left");
                            headerprop.setBgcolor("#C0C0C0");
                        }
                        if (colInfo.getString("xtype").equals("2")) {
                            prop.setAlign("left");
                            prop.setData("0.00");
                        } else {
                            prop.setAlign("left");
                            prop.setData(colInfo.getString("label"));
                        }
                        headerprop.setData(colInfo.getString("label"));
                        headerprop.setWidth(colInfo.getString("colwidth").concat("%"));

                        prodlist.add(prop);
                        headerlist.add(headerprop);

                    }

                    //show total
                    ArrayList<CustomDesignLineItemProp> rowtotallist = new ArrayList();
                    for (int count = 0; count < lineItems.length(); count++) {
                        rowtotallist.add(count, new CustomDesignLineItemProp());
                    }
                    ArrayList<Double> headerTotal = new ArrayList<Double>();
                    for (int count = 0; count < lineItems.length(); count++) {
                        headerTotal.add(count, 0.0d);
                    }
                    for (int headerCnt = 0; headerCnt < lineItems.length(); headerCnt++) {
                        JSONObject colInfo = lineItems.getJSONObject(headerCnt);
                        if (colInfo.optString("showtotal", "false").equals("true")) {
                            showtotal = true;
                            break;
                        }
                    }

                    //calculation for summarydata i.e. amount in words

                    ArrayList finalData = new ArrayList();

                    finalData.add(prodlist);
                    finalData.add(prodlist.clone());

                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalData);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);

                    StringWriter writer = new StringWriter();
                    /*
                     * get the Template
                     */
                    int lineItemTopSpacing = lineItem_YAxis - aboveLineItemObject_YAxis;
                    String defaultTemplate = "oldborder1.vm";
                    String tableBorderColor = "#0000000";
                    String borderstylemode = "borderstylemode1";
                    if (!StringUtil.isNullOrEmpty(pagelayoutproperty)) {
                        try {
                            JSONArray jArr1 = new JSONArray(pagelayoutproperty);
                            for (int cnt = 0; cnt < jArr1.length(); cnt++) {
                                JSONObject jObj = jArr1.getJSONObject(cnt);
                                if (!StringUtil.isNullOrEmpty(jObj.optString("tableproperties", ""))) {
                                    JSONObject tablePropArray = new JSONObject(jObj.optString("tableproperties", ""));
                                    for (int tablecnt = 0; tablecnt < tablePropArray.length(); tablecnt++) {
                                        tableBorderColor = tablePropArray.optString("bordercolor", "");
                                        borderstylemode = tablePropArray.optString("borderstylemode", "");
                                        context.put("tablebordercolor", tableBorderColor);

                                        /*
                                         * use the output in your email body
                                         */

                                        if (borderstylemode.equals("borderstylemode1")) {
                                            defaultTemplate = "oldborder1.vm";
                                        } else if (borderstylemode.equals("borderstylemode2")) {
                                            defaultTemplate = "oldborder2.vm";
                                        } else if (borderstylemode.equals("borderstylemode3")) {
                                            defaultTemplate = "oldborder3.vm";
                                        } else if (borderstylemode.equals("borderstylemode4")) {
                                            defaultTemplate = "oldborder4.vm";
                                        }
                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(jObj.optString("pagefontstyle", ""))) {
                                    JSONObject fontPropArray = new JSONObject(jObj.optString("pagefontstyle", ""));
                                    fontstylevalue = fontPropArray.optString("fontstyle", "");
                                    context.put("fontfamily", fontPropArray.optString("fontstyle", ""));
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(CustomDesignHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (StringUtil.isNullOrEmpty(fontstylevalue)) {
                        fontstylevalue = "sans-serif";
                    }
                    velocityEngine.mergeTemplate(defaultTemplate, "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    buildhtml = buildhtml.concat(tablehtml);
                }

                /*
                 * append Bottom Items
                 */

                String bottomDiv = " <div style='position:relative; width:" + CustomDesignHandler.pageWidth + ";'>";
                for (Map.Entry<String, String> entry : bottomHTMLItems.entrySet()) {
                    String bottomitem = entry.getValue();
                    bottomDiv = bottomDiv.concat(entry.getValue());
                }
                bottomDiv += "</div>";
                buildhtml += bottomDiv;
            }

            /*
             * replaced deprecated font tag with span
             */
            String recordids = "";
            buildhtml = CustomDesignHandler.replaceImagePathWithAbsolute(paramJobj.optString(Constants.RES_CDOMAIN), buildhtml);
            CustomDesignHandler.writeFinalDataToFile("invoice.pdf", fileType, buildhtml, pagelayoutproperty, pagefooterhtml, response, moduleid, recordids, request, fontstylevalue, pageheaderhtml,checkfooterheader);

        } catch (Exception ex) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public ModelAndView showSamplePreviewNew(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView-empty";
        try {
            int moduleid = 0;
            boolean isDefaultTemplate = false;
            String fontstylevalue = "";
            String pagefontsize = "";
            String fileType = "print";
            JSONArray lineItems = new JSONArray();
            JSONArray headerItems = new JSONArray();
            JSONArray numberFieldArray = new JSONArray();
            String alignment = "", backgroundcolor = "", changedlabel = "", headerStyle = "";
            String pagelayoutproperty = "", pagefooterhtml = "", pagefooterJSON="", pageheaderhtml = "", pageheaderJSON = "";
            
            Boolean showtotal = false,checkfooterheader=false, isShowSamplePreviewWithoutSave;
            String companyid = AccountingManager.getCompanyidFromRequest(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid))) {
                moduleid = Integer.parseInt(request.getParameter(Constants.moduleid));
            }
            String templateid = request.getParameter("templateid");
            String fieldIds = "";
            int lineitemwidth = 850;

            /*
             * below "bottomItems" variable used to identify items which are
             * appearing after line item grid
             */

            String html = "", json = "[]";
            
            isShowSamplePreviewWithoutSave = (!StringUtil.isNullOrEmpty(request.getParameter("isShowSamplePreviewWithoutSave")))?Boolean.parseBoolean(request.getParameter("isShowSamplePreviewWithoutSave")):false;
            
            if(isShowSamplePreviewWithoutSave){
                    KwlReturnObject result = customDesignDAOObj.getNewDesignTemplate(companyid, templateid, true);
                    List list = result.getEntityList();
                    Object[] rows = (Object[]) list.get(0);
                    html = rows[0].toString();
                    json = rows[1].toString();
                    pagelayoutproperty = rows[3] != null ? rows[3].toString() : "";
                    pagefooterhtml = rows[4] != null ? rows[4].toString() : "";
                    pagefooterJSON = rows[5] != null ? rows[5].toString() : "";
                    pageheaderhtml = rows[7] != null ? rows[7].toString() : "";
                    pageheaderJSON = rows[8] != null ? rows[8].toString() : "";
            } else {
                if (!StringUtil.isNullOrEmpty(templateid)) {
                    KwlReturnObject result = customDesignDAOObj.getNewDesignTemplate(companyid, templateid, false);
                    List list = result.getEntityList();
                    Object[] rows = (Object[]) list.get(0);
                    html = rows[0].toString();
                    json = rows[1].toString();
                    pagelayoutproperty = rows[3] != null ? rows[3].toString() : "";
                    pagefooterhtml = rows[4] != null ? rows[4].toString() : "";
                    pagefooterJSON = rows[5] != null ? rows[5].toString() : "";
                    pageheaderhtml = rows[8] != null ? rows[8].toString() : "";
                    pageheaderJSON = rows[9] != null ? rows[9].toString() : "";
                    isDefaultTemplate = rows[13] != null ? Boolean.TRUE.equals(Boolean.parseBoolean(rows[13].toString())) : false;
                } else {
                    html = request.getParameter("html");
                    json = request.getParameter("json");
                }
            }
            JSONArray jArr = new JSONArray(json);
            boolean isLineItemSummaryTable = false, bold = false, italic = false, underline = false;
            int widthOfTable = 100;
            String marginTop="";
            String marginBottom="";
            String marginLeft="";
            String marginRight="";
            String lineItemFirstRowHTML = "";
            String lineItemLastRowHTML = "";
            boolean isLastRowPresent = false;
            boolean isFirstRowPresent = false;

            JSONObject LineItemSummaryTableInfo = new JSONObject();
            String lineitemTableParentRowID = "" , fontsize ="", align="center", bordercolor="";
            for (int cnt = 0; cnt < jArr.length(); cnt++) {
                JSONArray colJArr = jArr.getJSONObject(cnt).getJSONArray("data");
                for (int colcnt = 0; colcnt < colJArr.length(); colcnt++) {
                    JSONArray itemsJArr = colJArr.getJSONObject(colcnt).getJSONArray("data");
                    for (int itemCnt = 0; itemCnt < itemsJArr.length(); itemCnt++) {
                        JSONObject jObj = itemsJArr.getJSONObject(itemCnt);
                        if (jObj.optInt("fieldType", 0) == 11 ) {
                            lineItems = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).getJSONArray("lineitems");
                            headerItems = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optJSONArray("headeritems");
                            lineitemTableParentRowID = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("parentrowid", "");
                            isLineItemSummaryTable = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optBoolean("isSummaryTable", false);
                            fontsize = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("fontsize", "");
                            align = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("align", "center");
                            bordercolor = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("bordercolor", "");
                            bold = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optBoolean("bold", false);
                            italic = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optBoolean("italic", false);
                            underline = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optBoolean("underline", false);
                            lineItemFirstRowHTML = jObj.optString("firstRowHTML", "");
                            lineItemLastRowHTML = jObj.optString("lastRowHTML", "");
                            isFirstRowPresent = !StringUtil.isNullOrEmpty(jObj.optString("isFirstRowPresent", "")) ? jObj.optBoolean("isFirstRowPresent", false) : false;
                            isLastRowPresent = !StringUtil.isNullOrEmpty(jObj.optString("isLastRowPresent", "")) ? jObj.optBoolean("isLastRowPresent", false) : false;
                            if (isLineItemSummaryTable) {
                                LineItemSummaryTableInfo = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).getJSONObject("summaryInfo");
                            }
                            widthOfTable = jObj.optInt("tablewidth", 100);
                            marginTop = jObj.optString("marginTop", "");
                            marginBottom = jObj.optString("marginBottom", "");
                            marginLeft = jObj.optString("marginLeft", "");
                            marginRight = jObj.optString("marginRight", "");
                            break;
                        } else if (!StringUtil.isNullOrEmpty(jObj.optString("fieldid", ""))) {
                            fieldIds += "'" + jObj.getString("fieldid") + "',";
                            if (jObj.has("fieldType") && !StringUtil.isNullOrEmpty(jObj.getString("fieldType"))) {
                                if (Integer.parseInt(jObj.getString("fieldType")) == 2 || Integer.parseInt(jObj.getString("fieldType")) == 17) {
                                    if (jObj.has("label") && !StringUtil.isNullOrEmpty(jObj.getString("label"))) {
                                        JSONObject numberField = new JSONObject();
                                        numberField.put("label",jObj.getString("label"));
                                        numberField.put("fieldType",jObj.getString("fieldType"));
                                        numberField.put("decimalPrecision",jObj.optString("decimalPrecision","2"));
                                        numberFieldArray.put(numberField);
                                    }
                                }
                            }
                        }
                        if (jObj.optInt("fieldType", 0) == 12) {
                            JSONArray placeHolders = jObj.optJSONArray("cellplaceholder");
                            if (placeHolders != null) {
                                for (int phIndex = 0; phIndex < placeHolders.length(); phIndex++) {
                                    JSONObject placeHolder = placeHolders.getJSONObject(phIndex);
                                    if (!StringUtil.isNullOrEmpty(placeHolder.optString("decimalPrecision", ""))) {
                                        JSONObject numberField = new JSONObject();
                                        numberField.put("label", placeHolder.getString("label"));
                                        numberField.put("fieldType", "17");
                                        numberField.put("decimalPrecision", placeHolder.optString("decimalPrecision", "2"));
                                        numberFieldArray.put(numberField);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            if(!StringUtil.isNullOrEmpty(pageheaderhtml) || !StringUtil.isNullOrEmpty(pagefooterhtml)) {
                checkfooterheader = true;
            }
            
            if (!fieldIds.isEmpty()) {
                fieldIds = fieldIds.substring(0, fieldIds.length() - 1);
                KwlReturnObject result = customDesignDAOObj.getDummyValue(fieldIds);
                List list = result.getEntityList();
                for (int cnt = 0; cnt < list.size(); cnt++) {
                    Object[] rows = (Object[]) list.get(cnt);
                    html = html.replace("#" + rows[0].toString() + "#", rows[1].toString());
                    if(checkfooterheader) {
                        pageheaderhtml = pageheaderhtml.replace("#" + rows[0].toString() + "#", rows[1].toString());
                        pagefooterhtml = pagefooterhtml.replace("#" + rows[0].toString() + "#", rows[1].toString());
                    }
                }
            }


            /*
             * Replace static Global Fields like - Total amount, total discount,
             * Today's Date, Amount in Word, Curreny Symbol
             */
            if (moduleid != 14 && moduleid != 16) {
                HashMap<String, String> summaryFields = LineItemColumnModuleMapping.InvoiceProductSummaryItems;
                for (Map.Entry<String, String> entry : summaryFields.entrySet()) {
                    JSONObject staticColInfo = new JSONObject(summaryFields.get(entry.getKey()));
                    if (staticColInfo.getString("xtype").equals("2") || staticColInfo.getString("xtype").equals("17") ) {
                        int decimalPrecision = 2;
                        for (int fieldCnt = 0; fieldCnt < numberFieldArray.length(); fieldCnt++) {
                            JSONObject fieldJson = numberFieldArray.getJSONObject(fieldCnt);
                            if (staticColInfo.getString("label").equals(fieldJson.getString(("label")))) {
                                if(fieldJson.has(("decimalPrecision"))){
                                    decimalPrecision = Integer.parseInt(fieldJson.getString(("decimalPrecision")));
                                }
                                break;
                            }
                        }
                        html = html.replace("#" + staticColInfo.getString("label") + "#", authHandler.formattingdecimal(0.00, decimalPrecision));
                        if(checkfooterheader) {
                            pageheaderhtml = pageheaderhtml.replace("#" + staticColInfo.getString("label") + "#", authHandler.formattingdecimal(0.00, decimalPrecision));
                            pagefooterhtml = pagefooterhtml.replace("#" + staticColInfo.getString("label") + "#", authHandler.formattingdecimal(0.00, decimalPrecision));
                        }
                    } else {
                        html = html.replace("#" + staticColInfo.getString("label") + "#", staticColInfo.getString("label"));
                        if(checkfooterheader) {
                            pageheaderhtml = pageheaderhtml.replace("#" + staticColInfo.getString("label") + "#", staticColInfo.getString("label"));
                            pagefooterhtml = pagefooterhtml.replace("#" + staticColInfo.getString("label") + "#", staticColInfo.getString("label"));
                        }
                    }
                }
            }
            /*
             * Gloabla Custom fields are not stored in default header. So we
             * don't have dummy value. Removing start and end #
             */

            KwlReturnObject result = customDesignDAOObj.getGlobalCustomFields(companyid, moduleid);
            List list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                Object[] row = (Object[]) list.get(cnt);
                int decimalPrecision = 2;
                for (int fieldCnt = 0; fieldCnt < numberFieldArray.length(); fieldCnt++) {
                    JSONObject fieldJson = numberFieldArray.getJSONObject(fieldCnt);
                    if (row[1].toString().equals(fieldJson.getString(("label")))) {
                        if (fieldJson.has(("decimalPrecision"))) {
                            decimalPrecision = Integer.parseInt(fieldJson.getString(("decimalPrecision")));
                        }
                        break;
                    }
                }
                if (row[2].toString().equals("2")) {
                    html = html.replace("#" + row[1].toString() + "#", authHandler.formattingdecimal(0.00, decimalPrecision));
                    if(checkfooterheader) {
                        pageheaderhtml = pageheaderhtml.replace("#" + row[1].toString() + "#", authHandler.formattingdecimal(0.00, decimalPrecision));
                        pagefooterhtml = pagefooterhtml.replace("#" + row[1].toString() + "#", authHandler.formattingdecimal(0.00, decimalPrecision));
                    }
                } else {
                    html = html.replace("#" + row[1].toString() + "#", row[1].toString());
                    if(checkfooterheader) {
                        pageheaderhtml = pageheaderhtml.replace("#" + row[1].toString() + "#", row[1].toString());
                        pagefooterhtml = pagefooterhtml.replace("#" + row[1].toString() + "#", row[1].toString());
                    }
                }
            }


            /*
             * Create jsoup object for html reader
             */

            Document jsoupDoc = Jsoup.parse(html);
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-south x-component-handle x-component-handle-south x-component-handle-south-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-north x-component-handle x-component-handle-north x-component-handle-north-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-east x-component-handle x-component-handle-east x-component-handle-east-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-west x-component-handle x-component-handle-west x-component-handle-west-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-northeast x-component-handle x-component-handle-northeast x-component-handle-northeast-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-northwest x-component-handle x-component-handle-northwest x-component-handle-northwest-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-southeast x-component-handle x-component-handle-southeast x-component-handle-southeast-br x-unselectable").remove();
            jsoupDoc.getElementsByClass("x-resizable-handle x-resizable-handle-southwest x-component-handle x-component-handle-southwest x-component-handle-southwest-br x-unselectable").remove();

            String buildhtml = html;

            /*
             * In below loop we are building HTML by appending Top Items, line
             * Iems and Bottom Items respectively
             */

            if (lineItems.length() > 0) {
                /*
                 * append Line Items
                 */
                jsoupDoc = Jsoup.parse(html);
                org.jsoup.nodes.Element itemListElement = jsoupDoc.getElementById(lineitemTableParentRowID);
                if (itemListElement!=null) {
                    String top = "0px", left = "0px", tablewidth = String.valueOf(lineitemwidth);

                    ArrayList<CustomDesignLineItemProp> prodlist = new ArrayList();
                    ArrayList<CustomDesignLineItemProp> headerlist = new ArrayList();
                    JSONArray columnarr = new JSONArray();
                    Set colnoset = new HashSet();
                    int fieldcnt = 0;
                    for (int cnt = 0; cnt < lineItems.length(); cnt++) {
                        if (!(colnoset.contains(lineItems.getJSONObject(cnt).optInt("colno",cnt)))) {
                            colnoset.add(lineItems.getJSONObject(cnt).optInt("colno",cnt));
                            columnarr.put(lineItems.getJSONObject(cnt));
                        }
                    }
                    for (int cnt1 = 0; cnt1 < columnarr.length(); cnt1++) {
                        fieldcnt = 0;
                        JSONArray fields = new JSONArray();
                        for (int cnt = 0; cnt < lineItems.length(); cnt++) {
                            if (columnarr.getJSONObject(cnt1).optInt("colno",cnt1) == lineItems.getJSONObject(cnt).optInt("colno",cnt)) {
                                fields.put(cnt);
                            }
                        }
                        columnarr.getJSONObject(cnt1).put("fields", fields);
                    }
                    JSONArray headerJsonArr = null;
                    if ( headerItems != null && headerItems.length() > 0 ) {
                        headerJsonArr = headerItems;
                    } else {
                        headerJsonArr = columnarr;
                    }
                    for (int headerCnt = 0; headerCnt < headerJsonArr.length(); headerCnt++) {
                        CustomDesignLineItemProp prop = new CustomDesignLineItemProp();
                        CustomDesignLineItemProp headerprop = new CustomDesignLineItemProp();
                        JSONObject colInfo = headerJsonArr.getJSONObject(headerCnt);
                        String headerproperty = colInfo.optString("headerproperty", "");
                        if (!headerproperty.equals("")) {
                            JSONObject jobjheader = new JSONObject(headerproperty);
                            alignment = jobjheader.getString("alignment");
                            backgroundcolor = jobjheader.getString("backgroundcolor");
                            changedlabel = jobjheader.getString("changedlabel");
                            headerStyle = jobjheader.getString("style");
                        }
                        if (colInfo.getString("xtype").equals("2")) {
                            if (!headerproperty.equals("")) {
                                headerprop.setData(changedlabel);
                                headerprop.setAlign(alignment);
                                headerprop.setBgcolor(backgroundcolor);
                                headerprop.setStyle(headerStyle);
                            } else {
                                headerprop.setData(colInfo.getString("label"));
                                headerprop.setAlign("left");
                                headerprop.setBgcolor("#C0C0C0");
                                headerprop.setStyle("padding: 1px;"+colInfo.optString("style","") + "  position:unset; " );
                            }
                        } else if (!headerproperty.equals("")) {//if headerproperty is not empty
                            headerprop.setData(changedlabel);
                            headerprop.setAlign(alignment);
                            headerprop.setBgcolor(backgroundcolor);
                            headerprop.setStyle(headerStyle);
                        } else {//headerproperty is empty
                            headerprop.setData(colInfo.optString("label", ""));
                            headerprop.setAlign("left");
                            headerprop.setBgcolor("#C0C0C0");
                            headerprop.setStyle(" padding: 1px; " +colInfo.optString("style","") + "  position:unset; " );
                        }
                        int fieldsLength = columnarr.getJSONObject(headerCnt).getJSONArray("fields").length();
                        JSONArray fields = columnarr.getJSONObject(headerCnt).getJSONArray("fields");
                        String prodData = "";
                        for ( int fieldCnt = 0 ; fieldCnt < fieldsLength; fieldCnt++ ) {
                            JSONObject prodInfo = lineItems.getJSONObject(fields.getInt(fieldCnt));
                            String style = prodInfo.optString("style", "");
                            if (fieldCnt == fieldsLength - 1) {
                                if(prodInfo.optInt("type",0)==3){
                                    String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(request.getParameter("cdomain"), false);
                                    prodData += "<div style =\"" + style + "\"><img src='../../../images/designer/product-image.png' style='height:100%;width:100%;'></img></div><!-- -->";
                                } else {
                                    if (colInfo.getString("xtype").equals("2")) {
                                        prodData += "<div style =\"" + style + "\">0.00</div>";
                                    } else {
                                        prodData += "<div style =\"" + style + "\">" + prodInfo.optString("columnname", prodInfo.optString("label", "")) + "</div>";
                                    }
                                }
                            } else {
                                if(prodInfo.optInt("type",0)==3){
                                    prodData += "<div style =\"" + style + "\"><img src='../../../images/designer/product-image.png' style='height:100%;width:100%;'></img></div><!-- -->";
                                } else {
                                    if (colInfo.getString("xtype").equals("2")) {
                                        prodData += "<div style =\"" + style + "\">0.00</div><!-- -->";
                                    } else {
                                        prodData += "<div style =\"" + style + "\">" + prodInfo.optString("columnname", prodInfo.optString("label", "")) + "</div><!-- -->";
                                    }
                                }
                            }
                        }
                        prop.setAlign("left");
                        prop.setData(prodData);
                        headerprop.setData(colInfo.getString("label"));
                        headerprop.setWidth(colInfo.getString("colwidth").concat("%"));

                        prodlist.add(prop);
                        headerlist.add(headerprop);

                    }

                    ArrayList<CustomDesignLineItemProp> rowtotallist = new ArrayList();
                    for (int count = 0; count < lineItems.length(); count++) {
                        rowtotallist.add(count, new CustomDesignLineItemProp());
                    }
                    ArrayList<Double> headerTotal = new ArrayList<Double>();
                    for (int count = 0; count < lineItems.length(); count++) {
                        headerTotal.add(count, 0.0d);
                    }
                    for (int headerCnt = 0; headerCnt < lineItems.length(); headerCnt++) {
                        JSONObject colInfo = lineItems.getJSONObject(headerCnt);
                        if (colInfo.optString("showtotal", "false").equals("true")) {
                            showtotal = true;
                            break;
                        }
                    }

                    //calculation for summarydata i.e. amount in words

                    ArrayList finalData = new ArrayList();

                    finalData.add(prodlist);
                    for (int count = 0; count < 50; count++) {
                        finalData.add(prodlist.clone());
                    }
                    
                    VelocityEngine ve = new VelocityEngine();
                    ve.init();
                    VelocityContext context = new VelocityContext();
                    context.put("tableHeader", headerlist);
                    context.put("prodList", finalData);
                    context.put("numberOfRows", (finalData.size() > 0) ? finalData.size() - 1 : 0);
                    context.put("headerCount", (headerlist.size() > 0) ? headerlist.size() - 1 : 0);
                    context.put("top", top);
                    context.put("left", left);
                    context.put("width", tablewidth);
                    context.put("fontsize", fontsize);
                    context.put("bold", bold?"bold":"normal");
                    context.put("italic", italic?"italic":"normal");
                    context.put("underline", underline?"underline":"");
                    context.put("bordercolor", bordercolor);
                    context.put("align", align);
                    context.put("tablewidth", widthOfTable);
                    context.put("margin", marginTop + " " + marginRight + " " + marginBottom + " " + marginLeft + " ");
                    context.put("lineItemFirstRowHTML", lineItemFirstRowHTML);
                    context.put("lineItemLastRowHTML", lineItemLastRowHTML);
                    context.put("isFirstRowPresent",isFirstRowPresent);
                    context.put("isLastRowPresent", isLastRowPresent);

                    StringWriter writer = new StringWriter();
                    /*
                     * get the Template
                     */
                    String defaultTemplate = "border1.vm";
                    String tableBorderColor = "#0000000";
                    String borderstylemode = "borderstylemode1";
                    if (!StringUtil.isNullOrEmpty(pagelayoutproperty)) {
                        try {
                            JSONArray jArr1 = new JSONArray(pagelayoutproperty);
                            for (int cnt = 0; cnt < jArr1.length(); cnt++) {
                                JSONObject jObj = jArr1.getJSONObject(cnt);
                                if (!StringUtil.isNullOrEmpty(jObj.optString("tableproperties", ""))) {
                                    JSONObject tablePropArray = new JSONObject(jObj.optString("tableproperties", ""));
                                    for (int tablecnt = 0; tablecnt < tablePropArray.length(); tablecnt++) {
                                        tableBorderColor = tablePropArray.optString("bordercolor", "");
                                        borderstylemode = tablePropArray.optString("borderstylemode", "");
                                        boolean isRoundBorder = !StringUtil.isNullOrEmpty(tablePropArray.optString("isRoundBorder", ""))?tablePropArray.optBoolean("isRoundBorder"):false;
                                        context.put("tablebordercolor", tableBorderColor);
                                        context.put("isroundborder", isRoundBorder);
                                       

                                        /*
                                         * use the output in your email body
                                         */

                                        if (borderstylemode.equals("borderstylemode1")) {
                                            defaultTemplate = "border1.vm";
                                        } else if (borderstylemode.equals("borderstylemode2")) {
                                            defaultTemplate = "border2.vm";
                                        } else if (borderstylemode.equals("borderstylemode3")) {
                                            defaultTemplate = "border3.vm";
                                        } else if (borderstylemode.equals("borderstylemode4")) {
                                            defaultTemplate = "border4.vm";
                                        }else if (borderstylemode.equals("borderstylemode5")) {
                                            defaultTemplate = "border5.vm";
                                        }else if (borderstylemode.equals("borderstylemode6")) {
                                            defaultTemplate = "border6.vm";
                                        }else if (borderstylemode.equals("borderstylemode7")) {
                                            defaultTemplate = "border7.vm";
                                        }else if (borderstylemode.equals("borderstylemode8")) {
                                            defaultTemplate = "border8.vm";
                                        }else if (borderstylemode.equals("borderstylemode9")) {
                                            defaultTemplate = "border9.vm";
                                        }else if (borderstylemode.equals("borderstylemode10")) {
                                            defaultTemplate = "border10.vm";
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(CustomDesignHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if(isLineItemSummaryTable) {
                        context.put("issummary", isLineItemSummaryTable);
                        Document jsoupSummaryTableDoc = Jsoup.parse(LineItemSummaryTableInfo.getString("html"));
                        int summaryTableColCnt = LineItemSummaryTableInfo.getInt("columnCount");
                        int summaryTableRowCnt = LineItemSummaryTableInfo.getInt("rowCount");
                        int totalLineTableColumnCnt = headerlist.size();
                        int emptyCells = totalLineTableColumnCnt - summaryTableColCnt;
                        
                        ArrayList finalSummaryData = new ArrayList();
                        
                        
                        org.jsoup.nodes.Element summaryTable = jsoupSummaryTableDoc.getElementById("summaryTableID");
                        Elements summaryTableRows = summaryTable.getElementsByTag("tr");
                        for (org.jsoup.nodes.Element row : summaryTableRows) {
                            Elements cells = row.select("td");
                            ArrayList<SummaryTableCellProperty> summaryRowData = new ArrayList();
                            for(int blankcellcnt =0; blankcellcnt < emptyCells; blankcellcnt ++) {
                                SummaryTableCellProperty prop = new SummaryTableCellProperty();
                                prop.setData("");
                                summaryRowData.add(prop);
                            }
                            if ( cells.size() > 0 ) {
                                for ( Element cell : cells ) {
                                    String style = cell.attr("style");
                                    String data = cell.text();
                                    String colspan = cell.attr("colspan");
                                    String rowspan = cell.attr("rowspan");
                                    SummaryTableCellProperty prop = new SummaryTableCellProperty();
                                    prop.setColspan((colspan!=null)?colspan:"");
                                    prop.setRowspan((rowspan!=null)?rowspan:"");
                                    prop.setStyle((style!=null)?style:"");
                                    prop.setData((data!=null)?data:"");
                                    summaryRowData.add(prop);
                                }
                            }
                            finalSummaryData.add(summaryRowData);
                        }
                        context.put("summaryTable", finalSummaryData);
                        context.put("issummarytable", finalSummaryData.size()>0 ? true : false);
                    }
                    velocityEngine.mergeTemplate(defaultTemplate, "UTF-8", context, writer);
                    String tablehtml = writer.toString();
                    Set<String> classSet = itemListElement.classNames();
                    itemListElement.removeAttr("class");
                    itemListElement.removeAttr("style");
                    if ( classSet.contains("joinnextrow") ) {
                        itemListElement.addClass("joinnextrow");
                    }
                    itemListElement.html(tablehtml);
                    buildhtml = jsoupDoc.body().html();
                }
            }

            /*
             *  Remove header and footer section from main html
             */
            if(checkfooterheader) {
                if(!StringUtil.isNullOrEmpty(pageheaderJSON)) {
                    JSONObject headerJSON = new JSONObject(pageheaderJSON);
                    if(!StringUtil.isNullOrEmpty(headerJSON.optString("id", ""))) {
                        jsoupDoc = Jsoup.parse(buildhtml);
                        org.jsoup.nodes.Element headerElement = jsoupDoc.getElementById(headerJSON.getString("id"));
                        headerElement.remove();
                        buildhtml = jsoupDoc.body().html();
                    }
                }
                if(!StringUtil.isNullOrEmpty(pagefooterJSON)) {
                    JSONObject footerJSON = new JSONObject(pagefooterJSON);
                    if(!StringUtil.isNullOrEmpty(footerJSON.optString("id", ""))) {
                        jsoupDoc = Jsoup.parse(buildhtml);
                        org.jsoup.nodes.Element footerElement = jsoupDoc.getElementById(footerJSON.getString("id"));
                        footerElement.remove();
                        buildhtml = jsoupDoc.body().html();
                    }
                }
            }
            
            
            /*
             * replaced deprecated font tag with span
             */
            String recordids = "";
            if(isDefaultTemplate) {
                buildhtml = CustomDesignHandler.replaceDefaultImagePathWithAbsolute(request.getParameter("cdomain"), companyid, buildhtml);
                pageheaderhtml = CustomDesignHandler.replaceDefaultImagePathWithAbsolute(request.getParameter("cdomain"), companyid, pageheaderhtml);
                pagefooterhtml = CustomDesignHandler.replaceDefaultImagePathWithAbsolute(request.getParameter("cdomain"), companyid, pagefooterhtml);
            } else {
                buildhtml = CustomDesignHandler.replaceImagePathWithAbsolute(request.getParameter("cdomain"), buildhtml);
                pageheaderhtml = CustomDesignHandler.replaceImagePathWithAbsolute(request.getParameter("cdomain"), pageheaderhtml);
                pagefooterhtml = CustomDesignHandler.replaceImagePathWithAbsolute(request.getParameter("cdomain"), pagefooterhtml);
            }
            
            jArr = new JSONArray(json);
            StringBuilder finalRowsInTr = new StringBuilder();
            for (int cnt = 0; cnt < jArr.length(); cnt++) {
                jsoupDoc = Jsoup.parse(buildhtml);
                if(!jArr.getJSONObject(cnt).optBoolean("isheader",false) && !jArr.getJSONObject(cnt).optBoolean("isfooter",false)) {
                    org.jsoup.nodes.Element footerElement = jsoupDoc.getElementById(jArr.getJSONObject(cnt).getString("id"));
                    finalRowsInTr = finalRowsInTr.append("<tr><td>").append(footerElement.outerHtml()).append("</td></tr>");
                }
            }
            
            if (!StringUtil.isNullOrEmpty(pagelayoutproperty)) {
                JSONArray jArr1 = new JSONArray(pagelayoutproperty);
                for (int cnt = 0; cnt < jArr1.length(); cnt++) {
                    JSONObject jObj = jArr1.getJSONObject(cnt);
                    if (!StringUtil.isNullOrEmpty(jObj.optString("pagelayoutsettings", ""))) {
                        JSONObject pagePropertyArray = new JSONObject(jObj.optString("pagelayoutsettings", ""));
                        fontstylevalue = pagePropertyArray.optString("pagefont", "");
                        pagefontsize = pagePropertyArray.optString("pagefontsize", "12");
                    }
                }
            }
            if (StringUtil.isNullOrEmpty(fontstylevalue)) {
                fontstylevalue = "sans-serif";
            }
            String buildHtml = finalRowsInTr.toString();
            buildHtml = buildHtml.replaceAll("\\s*\n\\s*<!-- -->\\s*\n\\s*", "");
            ArrayList<String> buildhtm = new ArrayList<String>();
            ArrayList<String> pageFooterHtml = new ArrayList<String>();
            ArrayList<String> fontStyleValue = new ArrayList<String>();
            ArrayList<String> pageHeaderHtml = new ArrayList<String>();
            ArrayList<String> pageFontSize = new ArrayList<String>();
            buildhtm.add(buildHtml);
            pageFooterHtml.add(pagefooterhtml);
            fontStyleValue.add(fontstylevalue);
            pageHeaderHtml.add(pageheaderhtml);
            pageFontSize.add(pagefontsize);
            CustomDesignHandler.writeFinalDataToFileNew("SamplePreview.pdf", fileType, buildhtm, pagelayoutproperty, pageFooterHtml, response, moduleid, recordids, request, fontStyleValue, pageHeaderHtml,checkfooterheader,pageFontSize,"{}");
        } catch (Exception ex) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    

 
    class ValueComparator implements Comparator<String> {

        Map<String, Integer> base;

        public ValueComparator(Map<String, Integer> base) {
            this.base = base;
        }

        // Note: this comparator imposes orderings that are inconsistent with equals.    
        public int compare(String a, String b) {
            if (base.get(a) >= base.get(b)) {
                return -1;
            } else {
                return 1;
            } // returning 0 would merge keys
        }
    }  
 
}
