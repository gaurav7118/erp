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

//import com.krawler.accounting.utils.NormalizeHtml;
import com.krawler.common.admin.AccCustomData;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.awt.Dimension;
import java.awt.Insets;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//import org.zefer.pd4ml.PD4Constants;
//import org.zefer.pd4ml.PD4ML;
//import org.zefer.pd4ml.PD4PageMark;

public class CustomDesignHandler {

    public static final String pageWidth = "850px";
    private static final TreeMap<Integer, String> FONT_MAP = initializeFontMap();
    private static final String[] FONT_VALUES = FONT_MAP.values().toArray(new String[0]);
    private static final String MEDIUM_FONT = "medium;";
    private static final String CHARSET = "UTF-8";
    private static final int INDENT_AMOUNT = 4;
    private static final String ldquo = String.valueOf('\u201C');
    private static final String rdquo = String.valueOf('\u201D');
    private static final String lsquo = String.valueOf('\u2018');
    private static final String rsquo = String.valueOf('\u2019');
    private static final String figureDash = String.valueOf('\u2012');
    private static final String enDash = String.valueOf('\u2013');
    private static final String emDash = String.valueOf('\u2014');
    private static final String NON_BREAK_SPACE = String.valueOf('\u00A0');
    private static final String NON_BREAK_FIGURE_SPACE = String.valueOf('\u2007');
    private static final String NON_BREAK_NARROW_SPACE = String.valueOf('\u202F');
    private static final String NON_BREAK_WORD_JOINER = String.valueOf('\u2060');
    private static final String NON_BREAK_ZERO_WIDTH = String.valueOf('\uFEFF');
    private static ArrayList<String> allowedAttributes = null;

    private static TreeMap<Integer, String> initializeFontMap() {
        TreeMap<Integer, String> map = new TreeMap<Integer, String>();
        map.put(new Integer(8), "xx-small;");
        map.put(new Integer(11), "small;");
        map.put(new Integer(13), "medium;");
        map.put(new Integer(16), "large;");
        map.put(new Integer(20), "x-large;");
        map.put(new Integer(28), "xx-large;");
        map.put(new Integer(Integer.MAX_VALUE), "300%;");
        return map;
    }
//    public static void generatePD4ML(HttpServletResponse response, String fileName, String html, VelocityEngine velocityEngine,List finalData, List headerlist) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
//        // Find out line items if exist then replace html code with table data
//        Document jsoupDoc = Jsoup.parse(html);
//        Elements itemListElement = jsoupDoc.getElementsByClass("tpl-content");
//        if(!itemListElement.isEmpty()) {
//            String top="0px",left="0px",tablewidth="300px";
//            Element mainDiv = itemListElement.first();
//            Attributes styleAtt = mainDiv.attributes();
//            List<Attribute> attList = styleAtt.asList();
//            for(Attribute a : attList) {
//                if(a.getKey().equals("style")){
//                    String[] items = a.getValue().trim().split(";");
//                    for(String item: items){
//                        String[] itemValues = item.split(":");
//                        if(itemValues[0].trim().equals("top")){
//                            top = itemValues[1];
//                        } else if(itemValues[0].trim().equals("left")){
//                            left = itemValues[1];
//                        } else if(itemValues[0].trim().equals("width")){
//                            tablewidth = itemValues[1];
//                        }
//                    }
//                }
//            }
//            VelocityContext context = new VelocityContext();  
//            context.put("tableHeader", headerlist); 
//            context.put("prodList", finalData); 
//            context.put("top", top); 
//            context.put("left", left); 
//            context.put("width", tablewidth); 
//            if(headerlist.size()>0) {
//                context.put("cellwidth", Integer.parseInt(tablewidth.replace("px", "").trim())/headerlist.size()); 
//            }
//            StringWriter writer = new StringWriter();  
//            /* 
//            *   get the Template   
//            */  
//            velocityEngine.mergeTemplate("lineitems.vm","UTF-8" , context, writer);
//            String tablehtml = writer.toString();
//            jsoupDoc.getElementsByClass("tpl-content").first().before(tablehtml);
//            jsoupDoc.getElementsByClass("tpl-content").first().remove();
////            jsoupDoc.select("div").first().append(tablehtml);
//            html = jsoupDoc.html();
//        }
//        html = html.replaceAll("border-color:#B5B8C8;border-style:solid;border-width:1px;", "");
//        html = html.replaceAll("border-color: rgb(181, 184, 200); border-style: solid; border-width: 1px;", "");
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        PD4ML pd4ml = getPD4MLHeaderFooter();
//        pd4ml.render(new StringReader(html), baos); 
//        baos.close();  
//        FileUtils.writeStringToFile(new File("C:\\Users\\krawler\\Desktop\\export_Output\\test.html"), html);
//        writeDataToFile(fileName, baos, response);
//    }

//    public static void generatePD4ML(HttpServletResponse response, String fileName, String html, 
//            VelocityEngine velocityEngine,JSONArray lineItemsArr, Map<String, String> summaryFields, List finalLineItemData, List headerlist) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
//        // Find out line items if exist then replace html code with table data
//        Document jsoupDoc = Jsoup.parse(html);
//        Elements itemListElement = jsoupDoc.getElementsByClass("tpl-content");
//        if(!itemListElement.isEmpty()) {
//            String top="0px",left="0px",tablewidth="300px";
//            Element mainDiv = itemListElement.first();
//            Attributes styleAtt = mainDiv.attributes();
//            List<Attribute> attList = styleAtt.asList();
//            for(Attribute a : attList) {
//                if(a.getKey().equals("style")){
//                    String[] items = a.getValue().trim().split(";");
//                    for(String item: items){
//                        String[] itemValues = item.split(":");
//                        if(itemValues[0].trim().equals("top")){
//                            top = itemValues[1];
//                        } else if(itemValues[0].trim().equals("left")){
//                            left = itemValues[1];
//                        } else if(itemValues[0].trim().equals("width")){
//                            tablewidth = itemValues[1];
//                        }
//                    }
//                }
//            }
//            VelocityContext context = new VelocityContext();  
//            context.put("tableHeader", headerlist); 
//            context.put("prodList", finalLineItemData); 
//            context.put("top", top); 
//            context.put("left", left); 
//            context.put("width", tablewidth); 
//            if(headerlist.size()>0) {
//                context.put("cellwidth", Integer.parseInt(tablewidth.replace("px", "").trim())/headerlist.size()); 
//            }
//            StringWriter writer = new StringWriter();  
//            /* 
//            *   get the Template   
//            */  
//            velocityEngine.mergeTemplate("lineitems.vm","UTF-8" , context, writer);
//            String tablehtml = writer.toString();
//            jsoupDoc.getElementsByClass("tpl-content").first().before(tablehtml);
//            jsoupDoc.getElementsByClass("tpl-content").first().remove();
////            jsoupDoc.select("div").first().append(tablehtml);
//            html = jsoupDoc.html();
//        }
//        html = html.replaceAll("border-color:#B5B8C8;border-style:solid;border-width:1px;", "");
//        html = html.replaceAll("border-color: rgb(181, 184, 200); border-style: solid; border-width: 1px;", "");
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        PD4ML pd4ml = getPD4MLHeaderFooter();
//        pd4ml.render(new StringReader(html), baos); 
//        baos.close();  
//        FileUtils.writeStringToFile(new File("C:\\Users\\krawler\\Desktop\\export_Output\\test.html"), html);
//        writeDataToFile(fileName, baos, response);
//    }
    public static String getLineDataByVelocityEngine(VelocityEngine velocityEngine, String buildhtml, List finalLineItemData, List headerlist,
            String top, String left, String tablewidth, boolean showtotal, List totalList, String tableProperty, String tableTopPadding) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
//        VelocityEngine ve = new VelocityEngine();  
//        ve.init();  
        VelocityContext context = new VelocityContext();
        context.put("tableHeader", headerlist);
        context.put("prodList", finalLineItemData);
        context.put("top", top);
        context.put("left", left);
        context.put("width", tablewidth);
        context.put("showtotal", showtotal);
        context.put("totalList", totalList);
        context.put("tabletoppadding", tableTopPadding);
//        if(headerlist.size()>0) {
//            context.put("cellwidth", Integer.parseInt(tablewidth.replace("px", "").trim())/headerlist.size()+"px"); 
//        }
        StringWriter writer = new StringWriter();
        /*
         * get the Template
         */
        String defaultTemplate = "oldborder1.vm";
        String tableBorderColor = "#0000000";
        String borderstylemode = "borderstylemode1";
        if (!StringUtil.isNullOrEmpty(tableProperty)) {
            try {
                JSONArray jArr1 = new JSONArray(tableProperty);
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
                        context.put("fontfamily", fontPropArray.optString("fontstyle", "sans-serif"));
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(CustomDesignHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        velocityEngine.mergeTemplate(defaultTemplate, "UTF-8", context, writer);
        String tablehtml = writer.toString();
        buildhtml = buildhtml.concat(tablehtml);
        return buildhtml;
    }
    
    
    
     public static String getLineDataHTMLByVelocityEngine(VelocityEngine velocityEngine, List finalLineItemData, List headerlist,
            String top, String left, String tablewidth, boolean showtotal, List totalList, String tableProperty, String tableTopPadding) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
//        VelocityEngine ve = new VelocityEngine();  
//        ve.init(); 
        VelocityContext context = new VelocityContext();
        context.put("tableHeader", headerlist);
        context.put("prodList", finalLineItemData);
        context.put("numberOfRows", (finalLineItemData.size() > 0)?finalLineItemData.size()-1:0);
        context.put("headerCount", (headerlist.size() > 0)?headerlist.size()-1:0);
        context.put("top", top);
        context.put("left", left);
        context.put("width", tablewidth);
        context.put("showtotal", showtotal);
        context.put("totalList", totalList);
        context.put("tabletoppadding", tableTopPadding);
//        if(headerlist.size()>0) {
//            context.put("cellwidth", Integer.parseInt(tablewidth.replace("px", "").trim())/headerlist.size()+"px"); 
//        }
        StringWriter writer = new StringWriter();
        /*
         * get the Template
         */
        String defaultTemplate = "border1.vm";
        String tableBorderColor = "#0000000";
        String borderstylemode = "borderstylemode1";
        if (!StringUtil.isNullOrEmpty(tableProperty)) {
            try {
                JSONArray jArr1 = new JSONArray(tableProperty);
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
                    if (!StringUtil.isNullOrEmpty(jObj.optString("pagefontstyle", ""))) {
                        JSONObject fontPropArray = new JSONObject(jObj.optString("pagefontstyle", ""));
                        context.put("fontfamily", fontPropArray.optString("fontstyle", "sans-serif"));
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(CustomDesignHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        velocityEngine.mergeTemplate(defaultTemplate, "UTF-8", context, writer);
        String tablehtml = writer.toString();
        return tablehtml;
    }


     public static String getLineDataHTMLByVelocityEngineNew(VelocityEngine velocityEngine, List finalLineItemData, List headerlist,
            String top, String left, String tablewidth, boolean showtotal, List totalList, String tableProperty, String tableTopPadding,
            ArrayList finalSummaryData, JSONObject LineItemHeaderProperties,int widthOfTable, String margin,String  lineItemFirstRowHTML, String lineItemLastRowHTML,boolean isFirstRowPresent, boolean isLastRowPresent, boolean isExtendLineItem) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
//        VelocityEngine ve = new VelocityEngine();
//        ve.init();
         String detailsTableSubType_id = "";
        VelocityContext context = new VelocityContext();
        context.put("tableHeader", headerlist);
        context.put("prodList", finalLineItemData);
        context.put("numberOfRows", (finalLineItemData.size() > 0)?finalLineItemData.size()-1:0);
        context.put("headerCount", (headerlist.size() > 0)?headerlist.size()-1:0);
        context.put("top", top);
        context.put("left", left);
        context.put("width", tablewidth);
        context.put("showtotal", showtotal);
        context.put("totalList", totalList);
        context.put("tabletoppadding", tableTopPadding);
        context.put("summaryTable", finalSummaryData);
        context.put("tablewidth", widthOfTable);
        context.put("margin", margin);
        context.put("lineItemFirstRowHTML",lineItemFirstRowHTML);
        context.put("lineItemLastRowHTML",lineItemLastRowHTML);
        context.put("isFirstRowPresent",isFirstRowPresent);
        context.put("isLastRowPresent",isLastRowPresent);
        context.put("isExtendLineItem",isExtendLineItem);
        //set flag of Details Table
        context.put("isDetailsTable", LineItemHeaderProperties.optBoolean("isDetailsTable", false));
        //set ID of Details Table
        context.put("detailsTableId", LineItemHeaderProperties.optString("detailsTableId", "idDetailsTable"));
        context.put("issummarytable", finalSummaryData.size()>0 ? true : false);
        if(LineItemHeaderProperties.has("fontsize") && !StringUtil.isNullOrEmpty(LineItemHeaderProperties.getString("fontsize"))){
            context.put("fontsize",LineItemHeaderProperties.getString("fontsize"));
        }
        if(LineItemHeaderProperties.has("align") && !StringUtil.isNullOrEmpty(LineItemHeaderProperties.getString("align"))){
            context.put("align",LineItemHeaderProperties.getString("align"));
        }
        if(LineItemHeaderProperties.has("bold") && !StringUtil.isNullOrEmpty(LineItemHeaderProperties.getString("bold"))){
            if(LineItemHeaderProperties.getBoolean("bold")){
                context.put("bold","bold");
            } else{
                context.put("bold","normal");
            }
        }
        if(LineItemHeaderProperties.has("italic") && !StringUtil.isNullOrEmpty(LineItemHeaderProperties.getString("italic"))){
            if(LineItemHeaderProperties.getBoolean("italic")){
                context.put("italic","italic");
            } else{
                context.put("italic","normal");
            }
        }
        if(LineItemHeaderProperties.has("bordercolor") && !StringUtil.isNullOrEmpty(LineItemHeaderProperties.getString("bordercolor"))){
            context.put("bordercolor",LineItemHeaderProperties.getString("bordercolor"));
        }
        if(LineItemHeaderProperties.has("taskBreak") && LineItemHeaderProperties.get("taskBreak") != null && LineItemHeaderProperties.optBoolean("isDetailsTable", false)){
            context.put("taskBreak", LineItemHeaderProperties.getJSONArray("taskBreak"));
        }
        if(LineItemHeaderProperties.has("taskTableHeader") && LineItemHeaderProperties.get("taskTableHeader") != null && LineItemHeaderProperties.optBoolean("isDetailsTable", false)){
            context.put("taskTableHeader", LineItemHeaderProperties.getJSONArray("taskTableHeader"));
        }
        if(LineItemHeaderProperties.has("detailsTableSubType_id") && !StringUtil.isNullOrEmpty(LineItemHeaderProperties.getString("detailsTableSubType_id")) && LineItemHeaderProperties.optBoolean("isDetailsTable", false)){
            detailsTableSubType_id = LineItemHeaderProperties.getString("detailsTableSubType_id");
        }
        if(LineItemHeaderProperties.has("underline") && !StringUtil.isNullOrEmpty(LineItemHeaderProperties.getString("underline"))){
            if(LineItemHeaderProperties.getBoolean("underline")){
                context.put("underline","underline");
            } else{
                context.put("underline","");
            }
        }
//        if(headerlist.size()>0) {
//            context.put("cellwidth", Integer.parseInt(tablewidth.replace("px", "").trim())/headerlist.size()+"px");
//        }
        StringWriter writer = new StringWriter();
        /*
         * get the Template
         */
        String defaultTemplate = "border1.vm";
        String tableBorderColor = "#0000000";
        String borderstylemode = "borderstylemode1";
        if (!StringUtil.isNullOrEmpty(tableProperty)) {
            try {
                JSONArray jArr1 = new JSONArray(tableProperty);
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
                    if (!StringUtil.isNullOrEmpty(jObj.optString("pagefontstyle", ""))) { 
                        JSONObject fontPropArray = new JSONObject(jObj.optString("pagefontstyle", ""));
                        context.put("fontfamily", fontPropArray.optString("fontstyle", "sans-serif"));
                    }                    
                }
            } catch (Exception ex) {
                Logger.getLogger(CustomDesignHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /**
         * Browser specific change.
         * If Mozilla version is from 55 to below 57 then border collapse property changes for Border Type 1. ----- ERP-36234
         */
        if(defaultTemplate.equals("border1.vm")){
            if(LineItemHeaderProperties.has("browserVersion") && !StringUtil.isNullOrEmpty(LineItemHeaderProperties.getString("browserVersion")) && LineItemHeaderProperties.optDouble("browserVersion") >= 55 && LineItemHeaderProperties.optDouble("browserVersion") < 57){
                context.put("extraClassName", "newborder1");
                context.put("borderCollapse", "");
            } else{
                context.put("extraClassName", "");
                context.put("borderCollapse", "border-collapse:collapse");
            }
        }
        
        if(detailsTableSubType_id.equals("tasks")){
            defaultTemplate = "workOrderTaskTable.vm";
        }
        velocityEngine.mergeTemplate(defaultTemplate, "UTF-8", context, writer);
        String tablehtml = writer.toString();
        return tablehtml;
    }
     public static String getAgeingTableHTMLByVelocityEngineNew(VelocityEngine velocityEngine, List ageingTableDataList, List headerlist ,String ageingTableStyle,List rowDataList) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
//        VelocityEngine ve = new VelocityEngine();
//        ve.init();
        VelocityContext context = new VelocityContext();
        context.put("tableHeader", headerlist);
//        context.put("tabledata", ageingTableDataList);
        context.put("tableStyle", ageingTableStyle);
        context.put("tableRowData", rowDataList);
        context.put("headerCount", (headerlist.size() > 0)?headerlist.size()-1:0);
        StringWriter writer = new StringWriter();
        String defaultTemplate = "ageingTable.vm";
        velocityEngine.mergeTemplate(defaultTemplate, "UTF-8", context, writer);
        String tablehtml = writer.toString();
        return tablehtml;
    }
     
     public static String getGroupingSummaryTableHTMLByVelocityEngineNew(VelocityEngine velocityEngine, List headerlist ,String groupingSummaryTableStyle,List rowDataList) throws ResourceNotFoundException, ParseErrorException, MethodInvocationException, Exception {
        VelocityContext context = new VelocityContext();
        context.put("tableHeader", headerlist);
        context.put("tableStyle", groupingSummaryTableStyle);
        context.put("tableRowData", rowDataList);
        context.put("headerCount", (headerlist.size() > 0)?headerlist.size()-1:0);
        StringWriter writer = new StringWriter();
        String defaultTemplate = "groupingSummaryTable.vm";
        velocityEngine.mergeTemplate(defaultTemplate, "UTF-8", context, writer);
        String tablehtml = writer.toString();
        return tablehtml;
    }

    public static String replaceImagePathWithAbsolute(String cdomain, String buildhtml) {
        String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(cdomain, false);
        buildhtml = buildhtml.replaceAll("src=\"[^\"]*?video.jsp", "src=\"" + baseUrl + "video.jsp");
        return buildhtml;
    }
    
    public static String replaceDefaultImagePathWithAbsolute(String cdomain, String companyid, String buildhtml) {
        String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(cdomain, false);
        buildhtml = buildhtml.replaceAll("src=\"[^\"]*?video.jsp?", "src=\"" + baseUrl + "defaulttemplateimageload.jsp?cid="+companyid+"&");
        return buildhtml;
    }

    public static void writeFinalDataToFile(String filename, String fileType, String buildhtml, HttpServletResponse response) throws IOException {
        buildhtml = CustomDesignHandler.processFontTags(buildhtml);
        buildhtml = buildhtml.replaceAll("border-color:#B5B8C8;border-style:solid;border-width:1px;", "");
        buildhtml = buildhtml.replaceAll("border-color: rgb(181, 184, 200)", "border-color: white");
        buildhtml = buildhtml.replaceAll("border-width: 1px;", "border-width: 0px;");
        buildhtml = addExportPD4MLCSS(buildhtml);
//        buildhtml = "<div style='width: 800; height:800; margin: 0px auto; position: relative; border: 2px solid;'>"+buildhtml+"</div>";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        PD4ML pd4ml = CustomDesignHandler.getPD4MLHeaderFooter();
//        pd4ml.render(new StringReader(buildhtml), baos); 
        baos.close();
//        FileUtils.writeStringToFile(new File("/Users/sagar/Desktop/test.html"), buildhtml);
        if (StringUtil.equal(fileType, "print")) {
            response.getOutputStream().write(baos.toByteArray());
            response.getOutputStream().flush();
        } else {
            writeDataToFile(filename, baos, response);
        }
    }

    
    /*Common Function to get details of Line Item*/
    public static HashMap<String, Object> getLineItemsDetails(JSONArray jArr) throws JSONException {
        JSONArray customizedlineItems = new JSONArray();
        JSONArray customizedheaderItems = new JSONArray();
        JSONArray groupingItems = new JSONArray();
        JSONArray groupingAfterItems = new JSONArray();
        JSONArray columndata = new JSONArray();
        int lineitemheight = 0;
        int lineitemwidth = 0;
        boolean isLineItemSummaryTable = false;
        boolean isGroupingRowPresent = false;
        boolean isGroupingApplied = false;
        boolean isGroupingAfterRowPresent = false;
        boolean islineitemrepeat = false;
        boolean isconsolidated = false;
        JSONObject LineItemSummaryTableInfo = new JSONObject();
        String lineitemTableParentRowID = "";
        String firstRowHTML = "";
        String lastRowHTML = "";
        String pageSize = "a4";
        String pageOrientation = "portrait";
        String adjustPageHeight = "0";
        String sortfield = "";
        String sortfieldxtype = "";
        String sortorder = "";
        boolean isFirstRowPresent = false;
        boolean isLastRowPresent = false;
        boolean isLineItemPresent = false;
        boolean isExtendLineItem = false;
        HashMap<String, Object> requestParam = new HashMap();
        for (int cnt = 0; cnt < jArr.length(); cnt++) {
            JSONArray colJArr = jArr.getJSONObject(cnt).getJSONArray("data");
            for (int colcnt = 0; colcnt < colJArr.length(); colcnt++) {
                JSONArray itemsJArr = colJArr.getJSONObject(colcnt).getJSONArray("data");
                for (int itemCnt = 0; itemCnt < itemsJArr.length(); itemCnt++) {
                    JSONObject jObj = itemsJArr.getJSONObject(itemCnt);
                    if (jObj.optInt("fieldType", 0) == 11) {
                        customizedlineItems = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).getJSONArray("lineitems");
                        customizedheaderItems = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optJSONArray("headeritems");
                        groupingItems = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optJSONArray("groupingItems");
                        groupingAfterItems = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optJSONArray("groupingAfterItems");
                        if(new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).has("columndata")){
                            columndata = new JSONArray(new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).getString("columndata"));
                        }
//                        lineitemheight = jObj.optInt("height", 60);
//                        lineitemwidth = jObj.optInt("width", 850);
                        isLineItemPresent = true;
                        firstRowHTML = jObj.optString("firstRowHTML","");
                        lastRowHTML = jObj.optString("lastRowHTML","");
                        isFirstRowPresent = !StringUtil.isNullOrEmpty(jObj.optString("isFirstRowPresent", ""))?jObj.optBoolean("isFirstRowPresent",false):false;
                        isLastRowPresent = !StringUtil.isNullOrEmpty(jObj.optString("isLastRowPresent", ""))?jObj.optBoolean("isLastRowPresent",false):false;
                        isGroupingRowPresent = !StringUtil.isNullOrEmpty(jObj.optString("isGroupingRowPresent", ""))?jObj.optBoolean("isGroupingRowPresent",false):false;
                        isGroupingApplied = !StringUtil.isNullOrEmpty(jObj.optString("isGroupingApplied", ""))?jObj.optBoolean("isGroupingApplied",false):false;
                        isExtendLineItem = !StringUtil.isNullOrEmpty(jObj.optString("isExtendLineItem", ""))?jObj.optBoolean("isExtendLineItem",false):false;
                        pageSize = !StringUtil.isNullOrEmpty(jObj.optString("pageSize", ""))?jObj.optString("pageSize","a4"):"a4";
                        pageOrientation = !StringUtil.isNullOrEmpty(jObj.optString("pageOrientation", ""))?jObj.optString("pageOrientation","portrait"):"portrait";
                        adjustPageHeight = !StringUtil.isNullOrEmpty(jObj.optString("adjustPageHeight", ""))?jObj.optString("adjustPageHeight","0"):"0";
                        isGroupingAfterRowPresent = !StringUtil.isNullOrEmpty(jObj.optString("isGroupingAfterRowPresent", ""))?jObj.optBoolean("isGroupingAfterRowPresent",false):false;
                        islineitemrepeat = !StringUtil.isNullOrEmpty(jObj.optString("islineitemrepeat", ""))?jObj.optBoolean("islineitemrepeat",false):false;
                        isconsolidated = !StringUtil.isNullOrEmpty(jObj.optString("isconsolidated", ""))?jObj.optBoolean("isconsolidated",false):false;
                        sortfield = !StringUtil.isNullOrEmpty(jObj.optString("sortfield",""))?jObj.optString("sortfield",""):"";
                        sortfieldxtype = !StringUtil.isNullOrEmpty(jObj.optString("sortfieldxtype",""))?jObj.optString("sortfieldxtype",""):"";
                        sortorder = !StringUtil.isNullOrEmpty(jObj.optString("sortorder",""))?jObj.optString("sortorder",""):"";
                        
                        lineitemTableParentRowID = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("parentrowid", "");
                        isLineItemSummaryTable = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optBoolean("isSummaryTable", false);
                        if(isLineItemSummaryTable) {
                            LineItemSummaryTableInfo = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).getJSONObject("summaryInfo");
                        }
                        break;
                    }
                }
            }
        }
        requestParam.put(Constants.Customedlineitems, customizedlineItems);
        requestParam.put(Constants.customizedheaderItems, customizedheaderItems);
        requestParam.put(Constants.lineitemHeight, lineitemheight);
        requestParam.put(Constants.lineitemWidth, lineitemwidth);
        requestParam.put(Constants.isLineItemPresent, isLineItemPresent);
        requestParam.put(Constants.isLineItemSummaryTable, isLineItemSummaryTable);
        requestParam.put(Constants.LineItemSummaryTableInfo, LineItemSummaryTableInfo.toString());
        requestParam.put(Constants.lineitemTableParentRowID, lineitemTableParentRowID);
        requestParam.put(Constants.lineItemColumns, columndata);
        requestParam.put(Constants.lineItemFirstRowHTML, firstRowHTML);
        requestParam.put(Constants.lineItemLastRowHTML, lastRowHTML);
        requestParam.put(Constants.ISFIRSTROWPRESENT, isFirstRowPresent);
        requestParam.put("isconsolidated", isconsolidated);
        requestParam.put(Constants.ISLASTROWPRESENT, isLastRowPresent);
        requestParam.put(Constants.GROUPINGITEMS, groupingItems);
        requestParam.put(Constants.ISGROUPINGROWPRESENT, isGroupingRowPresent);
        requestParam.put(Constants.ISGROUPINGAPPLIED, isGroupingApplied);
        requestParam.put(Constants.GROUPINGAFTERITEMS, groupingAfterItems);
        requestParam.put(Constants.ISGROUPINGAFTERROWPRESENT, isGroupingAfterRowPresent);
        requestParam.put(Constants.isExtendLineItem, isExtendLineItem);
        requestParam.put(Constants.pageSize, pageSize);
        requestParam.put(Constants.pageOrientation, pageOrientation);
        requestParam.put(Constants.adjustPageHeight, adjustPageHeight);
        requestParam.put(Constants.SORTFIELD, sortfield);
        requestParam.put(Constants.SORTFIELDXTYPE, sortfieldxtype);
        requestParam.put(Constants.SORTORDER, sortorder);
        return requestParam;
    }
    /**
     * Common Function to get details of all Details Table present in template
     * @param jArr
     * @return
     * @throws JSONException 
     */
    public static JSONArray getAllDetailsTableInfo(JSONArray jArr) throws JSONException {
        JSONArray customizedDetailsTableCols = new JSONArray();
        JSONArray customizedDetailsTableHeaders = new JSONArray();
        JSONArray columndata = new JSONArray();
        String detailsTableID = "";
        String detailsTableParentRowID = "";
        boolean isDetailsTablePresent = false;
        String detailsTableSubType_id = "";
        String detailsTableSubType_value = "";
        String consolidatedfield = "";
        String summationfields = "";
        JSONArray allDetailsTableInfo = new JSONArray();
        //Iterate JSON for getting details
        for (int cnt = 0; cnt < jArr.length(); cnt++) {
            JSONArray colJArr = jArr.getJSONObject(cnt).getJSONArray("data");
            for (int colcnt = 0; colcnt < colJArr.length(); colcnt++) {
                JSONArray itemsJArr = colJArr.getJSONObject(colcnt).getJSONArray("data");
                for (int itemCnt = 0; itemCnt < itemsJArr.length(); itemCnt++) {
                    JSONObject jObj = itemsJArr.getJSONObject(itemCnt);
                    //if Details Table then fetch data from JSON
                    if (jObj.optInt("fieldType", 0) == 20) {
                        customizedDetailsTableCols = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).getJSONArray("detailsTableCols");
                        customizedDetailsTableHeaders = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optJSONArray("detailsTableHeaders");
                        if(new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).has("columndata")){
                            columndata = new JSONArray(new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).getString("columndata"));
                        }
                        detailsTableParentRowID = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("parentrowid", "");
                        detailsTableSubType_id = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("detailsTableSubType_id", "");
                        detailsTableSubType_value = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("detailsTableSubType_value", "");
                        detailsTableID = new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("id", "");
                        consolidatedfield = jObj.optString("consolidatedfield", "");
                        summationfields = jObj.optString("summationfields", "");
                        isDetailsTablePresent = true;
                        //set details in map
                        HashMap<String, Object> requestParam = new HashMap();
                        requestParam.put(Constants.customizedDetailsTableCols, customizedDetailsTableCols);
                        requestParam.put(Constants.customizedDetailsTableHeaders, customizedDetailsTableHeaders);
                        requestParam.put(Constants.isDetailsTablePresent, isDetailsTablePresent);
                        requestParam.put(Constants.detailsTableParentRowID, detailsTableParentRowID);
                        requestParam.put(Constants.detailsTableColumns, columndata);
                        requestParam.put(Constants.detailsTableSubType_id, detailsTableSubType_id);
                        requestParam.put(Constants.detailsTableSubType_value, detailsTableSubType_value);
                        requestParam.put(Constants.detailsTableID, detailsTableID);
                        requestParam.put(Constants.consolidatedfield, consolidatedfield);
                        requestParam.put(Constants.summationfields, summationfields);
                        //put details map in JSONArray
                        allDetailsTableInfo.put(requestParam);
                    }
                }
            }
        }
        return allDetailsTableInfo;
    }
    
    public static JSONArray getConsolidatedDetailsTableArray(JSONArray detailsTableArr, String consolidatedfield, String summationfields) throws JSONException{
        JSONArray newDetailsTableArr = new JSONArray();
        ArrayList<String> detailsTableDataArrList = new ArrayList<String>();
        JSONObject detailsTableDataJobj = new JSONObject();
        //remove " from summation fields string
        summationfields = summationfields.replaceAll("\"", "");
        //consolidate data
        for(int ind = 0; ind < detailsTableArr.length(); ind++){detailsTableArr.toString();
            JSONObject detailsTableObj = detailsTableArr.optJSONObject(ind);
            String consolideOnValue = detailsTableObj.optString(consolidatedfield, "");
            //If consolide field value is blank then put value as !##
            if(StringUtil.isNullOrEmpty(consolideOnValue)){
                consolideOnValue = "!##";
            }
            //If value is already present in map then consolidate it's data else put it in object as it is
            if(detailsTableDataArrList.contains(consolideOnValue)){
                JSONObject dataObj = detailsTableDataJobj.getJSONObject(consolideOnValue);
                Iterator itr = dataObj.keys();
                JSONObject afterSummationObj = new JSONObject();
                //do summation of fields
                while(itr.hasNext()){
                    String key = (String) itr.next();
                    String newValStr = dataObj.optString(key, "");
                    //Check that field needs summation or not
                    if(summationfields.contains(key+",") || summationfields.contains(key+"]")){
                        double oldVal = dataObj.optDouble(key, 0);
                        double newVal = oldVal + detailsTableObj.optDouble(key, 0);
                        newValStr = newVal + "";
                    }
                    //put updated value in object
                    afterSummationObj.put(key, newValStr);
                }
                //put updated object after summation done
                detailsTableDataJobj.put(consolideOnValue, afterSummationObj);
            } else{
                //add consolide value in array list because it is new occurrence of that value
                detailsTableDataArrList.add(consolideOnValue);
                //put object as it is
                detailsTableDataJobj.put(consolideOnValue, detailsTableObj);
            }
        }
        //create consolide json array to return
        for(int ind = 0; ind < detailsTableDataArrList.size(); ind++){
            newDetailsTableArr.put(detailsTableDataJobj.getJSONObject(detailsTableDataArrList.get(ind)));
        }
        
        return newDetailsTableArr;
    }
    
    public static HashMap<String, Object> getChecklistTableDetails(JSONArray jArr) throws JSONException {
        boolean isChecklistTablePresent = false;
        JSONArray checklistTableHeaderItems = new JSONArray();
        JSONArray checklistTableDataItems = new JSONArray();
        String checklistTableParentRowid = "";
        String checklistValue = "";
        String marginTop="";
        String marginBottom="";
        String marginLeft="";
        String marginRight="";
        String tableWidth="";
        String style="";
        HashMap<String, Object> requestParam = new HashMap();
        for (int cnt = 0; cnt < jArr.length(); cnt++) {
            JSONArray colJArr = jArr.getJSONObject(cnt).getJSONArray("data");
            for (int colcnt = 0; colcnt < colJArr.length(); colcnt++) {
                JSONArray itemsJArr = colJArr.getJSONObject(colcnt).getJSONArray("data");
                for (int itemCnt = 0; itemCnt < itemsJArr.length(); itemCnt++) {
                    JSONObject jObj = itemsJArr.getJSONObject(itemCnt);
                    if (jObj.optInt("fieldType", 0) == 18) {
                        isChecklistTablePresent = true;jObj.toString();
                        checklistTableHeaderItems = !StringUtil.isNullOrEmpty(jObj.optString("headerItems", ""))?jObj.optJSONArray("headerItems"):new JSONArray();
                        checklistTableDataItems = !StringUtil.isNullOrEmpty(jObj.optString("dataItems", ""))?jObj.optJSONArray("dataItems"):new JSONArray();
                        checklistTableParentRowid = !StringUtil.isNullOrEmpty(jObj.optString("parentrowid", ""))?jObj.optString("parentrowid",""):"";
                        checklistValue = !StringUtil.isNullOrEmpty(jObj.optString("checklistValue", "rating"))?jObj.optString("checklistValue","rating"):"rating";
                        marginTop= !StringUtil.isNullOrEmpty(jObj.optString("marginTop", ""))?jObj.optString("marginTop",""):"";
                        marginBottom= !StringUtil.isNullOrEmpty(jObj.optString("marginBottom", ""))?jObj.optString("marginBottom",""):"";
                        marginLeft= !StringUtil.isNullOrEmpty(jObj.optString("marginLeft", ""))?jObj.optString("marginLeft",""):"";
                        marginRight= !StringUtil.isNullOrEmpty(jObj.optString("marginRight", ""))?jObj.optString("marginRight",""):"";
                        tableWidth= !StringUtil.isNullOrEmpty(jObj.optString("tableWidth", ""))?jObj.optString("tableWidth",""):"";
                    }
                }
            }
        }
        style="width:"+tableWidth+"%;margin-top:"+marginTop+";"+"margin-bottom:"+marginBottom+";"+"margin-left:"+marginLeft+";"+"margin-right:"+marginRight+";";
        requestParam.put(Constants.isChecklistTablePresent, isChecklistTablePresent);
        requestParam.put("checklistTableHeaderItems", checklistTableHeaderItems);
        requestParam.put("checklistTableDataItems", checklistTableDataItems);
        requestParam.put("checklistTableParentRowid", checklistTableParentRowid);
        requestParam.put("checklistValue", checklistValue);
        requestParam.put("checklistTableStyle", style);
        return requestParam;
    }
    
    public static HashMap<String, Object> getAgeingTableDetails(JSONArray jArr) throws JSONException {

        boolean isAgeingTablePresent = false;
        JSONArray ageingTableHeaderItems = new JSONArray();
        JSONArray ageingTableDataItems = new JSONArray();
        String ageingTableParentRowid = "";
        String marginTop="";
        String marginBottom="";
        String marginLeft="";
        String marginRight="";
        String tableWidth="";
        String style="";
        String ismulticurrency="";
        String isCustomerVendorCurrency="";
        String isincludecurrent="";
        String intervalType="";
        String intervalPlaceHolder=""; //ERP-28745
        String intervalText="";
        
        int noofinterval = 3;
        int interval = 30;
        HashMap<String, Object> requestParam = new HashMap();
        for (int cnt = 0; cnt < jArr.length(); cnt++) {
            JSONArray colJArr = jArr.getJSONObject(cnt).getJSONArray("data");
            for (int colcnt = 0; colcnt < colJArr.length(); colcnt++) {
                JSONArray itemsJArr = colJArr.getJSONObject(colcnt).getJSONArray("data");
                for (int itemCnt = 0; itemCnt < itemsJArr.length(); itemCnt++) {
                    JSONObject jObj = itemsJArr.getJSONObject(itemCnt);
                    if (jObj.optInt("fieldType", 0) == 18) {
                        isAgeingTablePresent = true;
                        ageingTableHeaderItems = !StringUtil.isNullOrEmpty(jObj.optString("headerItems", ""))?jObj.optJSONArray("headerItems"):new JSONArray();
                        ageingTableDataItems = !StringUtil.isNullOrEmpty(jObj.optString("dataItems", ""))?jObj.optJSONArray("dataItems"):new JSONArray();
                        noofinterval = !StringUtil.isNullOrEmpty(jObj.optString("noofintervals", ""))?jObj.optInt("noofintervals"):3;
                        interval = !StringUtil.isNullOrEmpty(jObj.optString("interval", ""))?jObj.optInt("interval"):30;
                        intervalText = !StringUtil.isNullOrEmpty(jObj.optString("intervalText", ""))?jObj.optString("intervalText","#From# - #To# Days"):"#From# - #To# Days";
                        intervalType = !StringUtil.isNullOrEmpty(jObj.optString("intervalType", ""))?jObj.optString("intervalType","Days"):"Days";
                        intervalPlaceHolder = !StringUtil.isNullOrEmpty(jObj.optString("intervalPlaceHolder", ""))?jObj.optString("intervalPlaceHolder",""):""; //ERP-28745
                        ageingTableParentRowid = !StringUtil.isNullOrEmpty(jObj.optString("parentrowid", ""))?jObj.optString("parentrowid",""):"";
                        marginTop= !StringUtil.isNullOrEmpty(jObj.optString("marginTop", ""))?jObj.optString("marginTop",""):"";
                        marginBottom= !StringUtil.isNullOrEmpty(jObj.optString("marginBottom", ""))?jObj.optString("marginBottom",""):"";
                        marginLeft= !StringUtil.isNullOrEmpty(jObj.optString("marginLeft", ""))?jObj.optString("marginLeft",""):"";
                        marginRight= !StringUtil.isNullOrEmpty(jObj.optString("marginRight", ""))?jObj.optString("marginRight",""):"";
                        tableWidth= !StringUtil.isNullOrEmpty(jObj.optString("tableWidth", ""))?jObj.optString("tableWidth",""):"";
                        ismulticurrency= !StringUtil.isNullOrEmpty(jObj.optString("ismulticurrency", ""))?jObj.optString("ismulticurrency",""):"";
                        isCustomerVendorCurrency= !StringUtil.isNullOrEmpty(jObj.optString("isCustomerVendorCurrency", ""))?jObj.optString("isCustomerVendorCurrency",""):"";
                        isincludecurrent= !StringUtil.isNullOrEmpty(jObj.optString("isincludecurrent", ""))?jObj.optString("isincludecurrent",""):"";
                    }
                }
            }
        }
        style="width:"+tableWidth+"%;margin-top:"+marginTop+";"+"margin-bottom:"+marginBottom+";"+"margin-left:"+marginLeft+";"+"margin-right:"+marginRight+";";
        requestParam.put(Constants.isAgeingTablePresent, isAgeingTablePresent);
        requestParam.put("ageingTableHeaderItems", ageingTableHeaderItems);
        requestParam.put("ageingTableDataItems", ageingTableDataItems);
        requestParam.put("noofintervals", noofinterval);
        requestParam.put("interval", interval);
        requestParam.put("ageingTableParentRowid", ageingTableParentRowid);
        requestParam.put("ageingTableStyle", style);
        requestParam.put("ismulticurrency", ismulticurrency);
        requestParam.put("isCustomerVendorCurrency", isCustomerVendorCurrency);
        requestParam.put("intervalText", intervalText);
        requestParam.put("intervalType", intervalType);
        requestParam.put("intervalPlaceHolder", intervalPlaceHolder); //ERP-28745
        requestParam.put("isincludecurrent", isincludecurrent);
        return requestParam;
    }
    
    public static HashMap<String, Object> getGroupingSummaryTableDetails(JSONArray jArr) throws JSONException {
        HashMap<String, Object> requestParam = new HashMap();
        boolean isGroupingSummaryTablePresent = false;
        JSONArray groupingSummaryTableHeaderItems = new JSONArray();
        JSONArray groupingSummaryTableDataItems = new JSONArray();
        String groupingSummaryTableParentRowid = "";
        String marginTop="";
        String marginBottom="";
        String marginLeft="";
        String marginRight="";
        String tableWidth="";
        String style="";
        String groupingOnDisplayValue="";
        String groupingOnValue="";
        
        for (int cnt = 0; cnt < jArr.length(); cnt++) {
            JSONArray colJArr = jArr.getJSONObject(cnt).getJSONArray("data");
            for (int colcnt = 0; colcnt < colJArr.length(); colcnt++) {
                JSONArray itemsJArr = colJArr.getJSONObject(colcnt).getJSONArray("data");
                for (int itemCnt = 0; itemCnt < itemsJArr.length(); itemCnt++) {
                    JSONObject jObj = itemsJArr.getJSONObject(itemCnt);
                    if (jObj.optInt("fieldType", 0) == 19) {
                        isGroupingSummaryTablePresent = true;
                        groupingSummaryTableHeaderItems = !StringUtil.isNullOrEmpty(jObj.optString("headerItems", ""))?jObj.optJSONArray("headerItems"):new JSONArray();
                        groupingSummaryTableDataItems = !StringUtil.isNullOrEmpty(jObj.optString("dataItems", ""))?jObj.optJSONArray("dataItems"):new JSONArray();
                        groupingSummaryTableParentRowid = !StringUtil.isNullOrEmpty(jObj.optString("parentrowid", ""))?jObj.optString("parentrowid",""):"";
                        groupingOnDisplayValue = !StringUtil.isNullOrEmpty(jObj.optString("groupingOnDisplayValue", ""))?jObj.optString("groupingOnDisplayValue",""):"";
                        groupingOnValue = !StringUtil.isNullOrEmpty(jObj.optString("groupingOnValue", ""))?jObj.optString("groupingOnValue",""):"";
                        
                        marginTop= !StringUtil.isNullOrEmpty(jObj.optString("marginTop", ""))?jObj.optString("marginTop",""):"";
                        marginBottom= !StringUtil.isNullOrEmpty(jObj.optString("marginBottom", ""))?jObj.optString("marginBottom",""):"";
                        marginLeft= !StringUtil.isNullOrEmpty(jObj.optString("marginLeft", ""))?jObj.optString("marginLeft",""):"";
                        marginRight= !StringUtil.isNullOrEmpty(jObj.optString("marginRight", ""))?jObj.optString("marginRight",""):"";
                        tableWidth= !StringUtil.isNullOrEmpty(jObj.optString("tableWidth", ""))?jObj.optString("tableWidth",""):"";
                    }
                }
            }
        }
        style="width:"+tableWidth+"%;margin-top:"+marginTop+";"+"margin-bottom:"+marginBottom+";"+"margin-left:"+marginLeft+";"+"margin-right:"+marginRight+";";
        requestParam.put(Constants.isGroupingSummaryTablePresent, isGroupingSummaryTablePresent);
        requestParam.put("groupingSummaryTableHeaderItems", groupingSummaryTableHeaderItems);
        requestParam.put("groupingSummaryTableDataItems", groupingSummaryTableDataItems);
        requestParam.put("groupingSummaryTableParentRowid", groupingSummaryTableParentRowid);
        requestParam.put("groupingSummaryTableStyle", style);
        requestParam.put("groupingOnDisplayValue", groupingOnDisplayValue);
        requestParam.put("groupingOnValue", groupingOnValue);
        
        return requestParam;
    }
    
      public static void writeFinalDataToFile(String filename, String fileType, String buildhtml, String pagelayoutproperty, String pagefooterhtml, HttpServletResponse response, int moduleid, String recordids, HttpServletRequest request, String fontstylevalue, String pageheaderhtml,Boolean checkfooterflag) throws IOException, JSONException {
        String moduleID = Integer.toString(moduleid);
        String contextpath = "", printbuttondisplay = "",footerdiv="",finalhtml="",footer="";
        if (!StringUtil.isNullOrEmpty(request.getContextPath())) {
            contextpath = request.getContextPath();
        }
        buildhtml = removeObjectBorderStyles(buildhtml);
        pagefooterhtml = removeObjectBorderStyles(pagefooterhtml);
        pageheaderhtml = removeObjectBorderStyles(pageheaderhtml);
        String footerHeight = "", headerHeight = "", header = "";
        if (!StringUtil.isNullOrEmpty(pagelayoutproperty)) {
            JSONArray jArr1 = new JSONArray(pagelayoutproperty);
            for (int cnt = 0; cnt < jArr1.length(); cnt++) {
                try {
                    JSONObject jObj = jArr1.getJSONObject(cnt);
                    if (!StringUtil.isNullOrEmpty(jObj.optString("pagefooter", ""))) {
                        JSONObject footerConfig = new JSONObject(jObj.optString("pagefooter", ""));
                        footerHeight = footerConfig.optString("footerheight","0");
                        headerHeight = footerConfig.optString("headerheight","0");

                    }
                } catch (Exception ex) {
                    Logger.getLogger(CustomDesignHandler.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        String url = "";
        String commonParams = "?moduleid=" + moduleid + "&recordids=" + recordids + "&isprinted=true";
        switch (moduleid) {
            case Constants.Acc_Invoice_ModuleId:
                url = contextpath + "/ACCInvoice/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Vendor_Invoice_ModuleId:
                url = contextpath + "/ACCGoodsReceipt/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Purchase_Order_ModuleId:
                url = contextpath + "/ACCPurchaseOrder/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Sales_Order_ModuleId:
                url = contextpath + "/ACCSalesOrder/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Customer_Quotation_ModuleId:
                url = contextpath + "/ACCSalesOrder/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=true";
                break;
            case Constants.Acc_Vendor_Quotation_ModuleId:
                url = contextpath + "/ACCPurchaseOrder/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=true";
                break;
            case Constants.Acc_Delivery_Order_ModuleId:
                url = contextpath + "/ACCInvoice/updateDeliveryOrderPrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Goods_Receipt_ModuleId:
                url = contextpath + "/ACCGoodsReceipt/updateGoodsReceiptOrderPrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Credit_Note_ModuleId:
                url = contextpath + "/ACCCreditNote/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Debit_Note_ModuleId:
                url = contextpath + "/ACCDebitNote/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Receive_Payment_ModuleId:
                url = contextpath + "/ACCReceipt/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Make_Payment_ModuleId:
                url = contextpath + "/ACCVendorPayment/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;

        }

      //Android don't want to see the print button in html while in application it is required-Neeraj D
        if (StringUtil.isNullOrEmpty(request.getParameter("isAndroidIphoneCall"))) {
            printbuttondisplay = "<div style='position: relative; width: 950px;'><button id = 'print' title='Print Invoice' onclick= \" window.print(); updatePrint(\'" + moduleID + "\',\'" + recordids + "\'); \" style='color: rgb(8, 55, 114);' href='#'> Print </button></div>";
        }

         if (StringUtil.equal(fileType, "print")&& !checkfooterflag) {
            String htmlhead = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
                    + "<style type=\"text/css\">"
                    + "@media print {button#print {display: none;}}"
                    + "html,body { height:100%; padding:0; margin:0;font-family:" + fontstylevalue + ";}"
                    //                    + "html,body {width: 800; margin: 0px auto; position: relative; height:100%; padding:0; }"
                    //                    + ".header{height:"+headerHeight+"px;line-height: 2px;}"
                    + "#footer { background:#fff; width:100%; height:" + footerHeight + "px; position:fixed; bottom:0; left:0;font-family:" + fontstylevalue + "; }"
                    + "#header { background:#fff; width:100%; height:" + headerHeight + "px; position:fixed; top:0; left:0;font-family:" + fontstylevalue + "; }"
                    //                    + "#header{background:#fff; width:100%; height:"+headerHeight+"px;position:fixed;top:0; left:0;font-family:" + fontstylevalue + "; }"
                    + "thead {display: table-header-group;}"
                    + "tfoot {display: table-footer-group;}"
                    +" tbody {display: table-row-group;}"
                    + "</style>"
                    + "<script type=\"text/javascript\">"
                    + "   function calheightfun(){"
                    + " var heightmiddlediv=document.getElementsByClassName('middlediv')[0].scrollHeight;"
                    + " document.getElementsByClassName('middlediv')[0].style.setProperty('height',heightmiddlediv);"
                    + " var bottomdiv=document.getElementsByClassName('bottomdiv')[0].scrollHeight;"
                    + " document.getElementsByClassName('bottomdiv')[0].style.setProperty('height',bottomdiv);"
                    + "               };"
                    + "    function updatePrint(moduleid,invoiceIDs){"
                    + "        var xhr = new XMLHttpRequest();"
                    + "        xhr.open('POST','" + url + "', true);"
                    + "        xhr.send(null);"
                    + "        xhr.onload = function () {"
                    + "           console.log(this.responseText);"
                    + "};"
                    + "}"
                    + "</script>"
                    + "</head><body onload='calheightfun()'><table>";
             if (!StringUtil.isNullOrEmpty(headerHeight)) {//if Header height is not given then don't print html  
                header = "<thead><tr><td><td><div style='position:relative;height:" + headerHeight + "px'><div id='header'>" + pageheaderhtml + "</div></div></td></tr></thead>";
             }
            footer = "<div style='position:relative;height:" + footerHeight + "px'><div id='footer'>" + pagefooterhtml + "</div></div>";
            buildhtml = header + "<tbody><tr><td>"+ buildhtml + "</td></tr><tr><td>"+printbuttondisplay+"</td></tr></tbody><tfoot><tr><td>";
            buildhtml = htmlhead + buildhtml + footer + "</td></tr></tfoot></table></body></html>";
             
            buildhtml = CustomDesignHandler.processFontTags(buildhtml);
            //          buildhtml = "<div style='width: 800; height:800; margin: 0px auto; position: relative; border: 2px solid;'>"+buildhtml+"</div>";
            response.getOutputStream().write(buildhtml.getBytes());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        } else if (StringUtil.equal(fileType, "print")&& checkfooterflag){
             String htmlhead = "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
                    + "<style type=\"text/css\">"
                    + "@media print {button#print {display: none;} #footer {visibility: hidden;} "
                    + "div.divFooter {position: fixed;bottom: 0;} #pageFooter {display: block;}	}"	
                    + "html,body { height:100%; padding:0; margin:0;font-family:" + fontstylevalue + ";}"
                    
                    +"@media screen {#pageFooter {display: none;}  }"	
                    //                    + "html,body {width: 800; margin: 0px auto; position: relative; height:100%; padding:0; }"
                    //                    + ".header{height:"+headerHeight+"px;line-height: 2px;}"
                    + "#footer,#pageFooter { background:#fff; width:100%; height:" + footerHeight + "px;font-family:" + fontstylevalue + "; }"
                    + "#header { background:#fff; width:100%; height:" + headerHeight + "px;font-family:" + fontstylevalue + "; }"
                    //                    + "#header{background:#fff; width:100%; height:"+headerHeight+"px;position:fixed;top:0; left:0;font-family:" + fontstylevalue + "; }"
                    + "thead {display: table-header-group;}"
                    + "tfoot {display: table-footer-group;}"
//                    +" tbody {display: table-row-group;}"
                    + "</style>"
                    + "<script type=\"text/javascript\">"
                    + "   function calheightfun(){"
//                    + " var heightmiddlediv=document.getElementsByClassName('middlediv')[0].scrollHeight;"
//                    + " document.getElementsByClassName('middlediv')[0].style.setProperty('height',heightmiddlediv);"
                    + " var bottomdiv=document.getElementsByClassName('bottomdiv')[0].scrollHeight;"
                    + " document.getElementsByClassName('bottomdiv')[0].style.setProperty('height',bottomdiv);"
                    + " };"
                    + "    function updatePrint(moduleid,invoiceIDs){"
                    + "        var xhr = new XMLHttpRequest();"
                    + "        xhr.open('POST','" + url + "', true);"
                    + "        xhr.send(null);"
                    + "        xhr.onload = function () {"
                    + "           console.log(this.responseText);"
                    + "};"
                    + "}"
                    + "</script>"
                    + "</head>";
             /*
              * Header Section
              */
             if (!StringUtil.isNullOrEmpty(headerHeight)) {//if Header height is not given then don't print html  
                 header = "<thead><tr><td><div id='header' style='position:relative;height:" + headerHeight + "px'>" + pageheaderhtml + "</div></td></tr></thead>";
             }
             
            /*Footer Section*/
            if (!StringUtil.isNullOrEmpty(footerHeight)) {//if Header height is not given then don't print html  
//                footer = "<tfoot><tr><td><div id='footer' style='position:relative;height:" + footerHeight + "px'>" + pagefooterhtml + "</div></td></tr></tfoot>";
                footerdiv="<div class='divFooter' id='pageFooter'><div style='position:relative;height:" + footerHeight + "px'>" + pagefooterhtml + "</div></div>";
            }
            
            if (!StringUtil.isNullOrEmpty(footerdiv)) {//if Header height is not given then don't print html  
                footer = "<tfoot><tr><td><div id='footer' style='position:relative;height:" + footerHeight + "px'>" + pagefooterhtml + "</div></td></tr></tfoot>";
            }
            
            /*Tbody section*/
            finalhtml= htmlhead+"<body onload='calheightfun()'>"+printbuttondisplay+footerdiv+"<table>"+header+"<tbody>"+buildhtml+"</tbody>"+footer+"</table></body></html>";
            finalhtml = CustomDesignHandler.processFontTags(finalhtml);
            
            response.getOutputStream().write(finalhtml.getBytes());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }else {
            buildhtml = CustomDesignHandler.processFontTags(buildhtml);
            buildhtml = addExportPD4MLCSS(buildhtml);
//            buildhtml = "<div style='width: 800; height:800; margin: 0px auto; position: relative; border: 2px solid;'>"+buildhtml+"</div>";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            PD4ML pd4ml = CustomDesignHandler.getPD4MLHeaderFooter(pagelayoutproperty,pagefooterhtml);
//            pd4ml.render(new StringReader(buildhtml), baos); 
            baos.close();
//            FileUtils.writeStringToFile(new File("/home/krawler/Desktop/test.html"), buildhtml);
            writeDataToFile(filename, baos, response);
        }
    }
      
    public static void writeFinalDataToFileNew(String filename, String fileType, ArrayList<String> buildhtml, String pagelayoutproperty,
            ArrayList<String> pagefooterhtml, HttpServletResponse response, int moduleid, String recordids,
            HttpServletRequest request, ArrayList<String> fontstylevalue, ArrayList<String> pageheaderhtml, Boolean checkfooterflag, ArrayList<String> pagefontsize,String extLIJobjString) throws IOException, JSONException, SessionExpiredException, ParseException {
        String moduleID = Integer.toString(moduleid);
        String contextpath = "", finalFooter = "", finalHeader = "";
        if (!StringUtil.isNullOrEmpty(request.getContextPath())) {
            contextpath = request.getContextPath();
        }
        String html = "";
        JSONObject extLIJobj = new JSONObject();
        if(!StringUtil.isNullOrEmpty(extLIJobjString)) {
            extLIJobj=new JSONObject(extLIJobjString);
        }       
        boolean isExtendLineItem=extLIJobj.optBoolean("isExtendLineItem",false);
        boolean isPrePrented = extLIJobj.optBoolean("isPrePrinted",false);
        boolean isMultipleTransaction = extLIJobj.optBoolean(CustomDesignerConstants.IS_MULTIPLE_TRANSACTION,false);
        boolean isExtendedGlobalTable = extLIJobj.optBoolean("isExtendedGlobalTable",false);
        String pageSize=extLIJobj.optString("pageSize","a4");
        String pageOrientation = extLIJobj.optString("pageOrientation", "portrait");
        String adjustPageHeight=extLIJobj.optString("adjustPageHeight","0");
        String footer = "";
        String header = "";
        String fontSize = pagefontsize.get(0) != null ? pagefontsize.get(0): "12";
        String fontValue = fontstylevalue.get(0) != null ?  fontstylevalue.get(0) : "calibri";
        boolean isPageBorderIncluded = false;
        String pageMarginUnit = "px";
        String pageTopMargin = "72px";
        String pageRightMargin = "49px";
        String pageBottomMargin = "140px";
        String pageLeftMargin = "72px";
        double pageTopMarginDbl = 0, pageRightMarginDbl = 0, pageBottomMarginDbl = 0, pageLeftMarginDbl = 0;
        String pageborderType = "solid";
        boolean bottomPageBorder = false;
        String bottomPageBorderStyle = "";
        boolean leftPageBorder = false;
        String leftPageBorderStyle = "";
        boolean rightPageBorder = false;
        String rightPageBorderStyle = "";
        boolean topPageBorder = false;
        String topPageBorderStyle = "";
        String watermarkProperties = "";
        if (!StringUtil.isNullOrEmpty(pagelayoutproperty)) {
            JSONArray jArr1 = new JSONArray(pagelayoutproperty);
            for (int cnt = 0; cnt < jArr1.length(); cnt++) {
                try {
                    if (jArr1.get(cnt) != null && jArr1.get(cnt).getClass().equals(JSONObject.class)) {
                        JSONObject temp = jArr1.getJSONObject(cnt);
                        if (temp.has("pagelayoutsettings")) {
                            JSONObject pageLayoutSetting = temp.getJSONObject("pagelayoutsettings");
                            isPageBorderIncluded = pageLayoutSetting.getBoolean("pageBorderIncluded");
                            pageBottomMargin = pageLayoutSetting.getString("pagebottom");
                            pageLeftMargin = pageLayoutSetting.getString("pageleft");
                            pageRightMargin = pageLayoutSetting.getString("pageright");
                            pageTopMargin = pageLayoutSetting.getString("pagetop");
                            pageMarginUnit = pageLayoutSetting.getString("pagemarginunit");
                            pageBottomMarginDbl = Double.parseDouble(pageBottomMargin.substring(0, pageBottomMargin.length() - 2));
                            pageLeftMarginDbl = Double.parseDouble(pageLeftMargin.substring(0, pageLeftMargin.length() - 2));
                            pageRightMarginDbl = Double.parseDouble(pageRightMargin.substring(0, pageRightMargin.length() - 2));
                            pageTopMarginDbl = Double.parseDouble(pageTopMargin.substring(0, pageTopMargin.length() - 2));
                            bottomPageBorder = pageLayoutSetting.getBoolean("bottomPageBorder");
                            leftPageBorder = pageLayoutSetting.getBoolean("leftPageBorder");
                            rightPageBorder = pageLayoutSetting.getBoolean("rightPageBorder");
                            topPageBorder = pageLayoutSetting.getBoolean("topPageBorder");
                        } else if (temp.has("watermarkProperties")) {
                            watermarkProperties = temp.toString();
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
        
        if ( topPageBorder ) {
            topPageBorderStyle = "border-top: 1px solid; "; // ERP-18286 : Dotted page border is applying for only first page in print
        } else {
            topPageBorderStyle = "border-top: none; ";
        }
        
        if ( bottomPageBorder ) {
            bottomPageBorderStyle = "border-bottom: 1px solid; ";   // ERP-18286 : Dotted page border is applying for only first page in print
        } else {
            bottomPageBorderStyle = "border-bottom: none; ";
        }
        
        if ( rightPageBorder ) {
            rightPageBorderStyle = "border-right: 1px solid; "; // ERP-18286 : Dotted page border is applying for only first page in print
        } else {
            rightPageBorderStyle = "border-right: none; ";
        }
        
        if ( leftPageBorder ) {
            leftPageBorderStyle = "border-left: 1px solid; ";   // ERP-18286 : Dotted page border is applying for only first page in print
        } else {
            leftPageBorderStyle = "border-left: none; ";
        }
        
        double pageWidth = 21.00;
        String pageWidthStr = "21cm";
        /*
            Removed below check because it causes the page margine problem- SDP-12937
        */
//        if (isExtendLineItem || isExtendedGlobalTable) {
            switch(pageSize){
                case "a4":
                    switch(pageMarginUnit){
                        case "px":
                            pageWidth = 794;
                            break;
                        case "cm":
                            pageWidth = 21;
                            break;
                        case "mm":
                            pageWidth = 210;
                            break;
                        case "in":
                            pageWidth = 8.27;
                            break;
                    }
                    break;
                case "letter":
                    switch(pageMarginUnit){
                        case "px":
                            pageWidth = 816;
                            break;
                        case "cm":
                            pageWidth = 21.59;
                            break;
                        case "mm":
                            pageWidth = 215.9;
                            break;
                        case "in":
                            pageWidth = 8.5;
                            break;
                    }
                    break;
                case "a3":
                    switch(pageMarginUnit){
                        case "px":
                            pageWidth = 1122;
                            break;
                        case "cm":
                            pageWidth = 29.7;
                            break;
                        case "mm":
                            pageWidth = 297;
                            break;
                        case "in":
                            pageWidth = 11.69;
                            break;
                    }
                    break;
            }
//        }
        
//        if (isExtendLineItem) {
//            if (pageOrientation.equalsIgnoreCase("portrait")) {
//                if (pageSize.equalsIgnoreCase("a4")) {
//                    pageWidth = pageWidth - pageLeftMarginDbl - pageRightMarginDbl;
//                } else if (pageSize.equalsIgnoreCase("letter")) {
//                    pageWidth = pageWidth - pageLeftMarginDbl - pageRightMarginDbl;
//                } else if (pageSize.equalsIgnoreCase("a3")) {
//                    pageWidth = pageWidth - pageLeftMarginDbl - pageRightMarginDbl;
//                }
//            } else {
//                if (pageSize.equalsIgnoreCase("a4")) {
//                    pageWidth = pageWidth - pageLeftMarginDbl - pageRightMarginDbl;
//                } else if (pageSize.equalsIgnoreCase("letter")) {
//                    pageWidth = pageWidth - pageLeftMarginDbl - pageRightMarginDbl;
//                } else if (pageSize.equalsIgnoreCase("a3")) {
//                    pageWidth = pageWidth - pageLeftMarginDbl - pageRightMarginDbl;
//                }
//
//            }
//        }
//        double rMargin = Double.parseDouble(pageRightMargin.replace("px", "").replace("cm", "").replace("mm", "").replace("in", ""));
//        double lMargin = Double.parseDouble(pageLeftMargin.replace("px", "").replace("cm", "").replace("mm", "").replace("in", ""));
        /*
            Commented  below code because it causes the page margine problem- SDP-12937
        */
        /*
        rMargin = rMargin * 0.0265;
        lMargin = lMargin * 0.0265;
        */
//        pageWidth = pageWidth - (lMargin + rMargin);
        /*
            Removed below check because it causes the page margine problem- SDP-12937
        */
//        if (isExtendLineItem || isExtendedGlobalTable) {
        pageWidth = pageWidth - pageLeftMarginDbl - pageRightMarginDbl; // deducting left and right marging from page width
        pageWidthStr = pageWidth + pageMarginUnit;
//        }

        String url = "";
        String commonParams = "?moduleid=" + moduleid + "&recordids=" + recordids + "&isprinted=true";
        switch (moduleid) {
            case Constants.Acc_Invoice_ModuleId:
                url = contextpath + "/ACCInvoice/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Vendor_Invoice_ModuleId:
                url = contextpath + "/ACCGoodsReceipt/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Purchase_Order_ModuleId:
                url = contextpath + "/ACCPurchaseOrder/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Sales_Order_ModuleId:
                url = contextpath + "/ACCSalesOrder/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Customer_Quotation_ModuleId:
                url = contextpath + "/ACCSalesOrder/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=true";
                break;
            case Constants.Acc_Vendor_Quotation_ModuleId:
                url = contextpath + "/ACCPurchaseOrder/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=true";
                break;
            case Constants.Acc_Delivery_Order_ModuleId:
                url = contextpath + "/ACCInvoice/updateDeliveryOrderPrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Goods_Receipt_ModuleId:
                url = contextpath + "/ACCGoodsReceipt/updateGoodsReceiptOrderPrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Credit_Note_ModuleId:
                url = contextpath + "/ACCCreditNote/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Debit_Note_ModuleId:
                url = contextpath + "/ACCDebitNote/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Receive_Payment_ModuleId:
                url = contextpath + "/ACCReceipt/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;
            case Constants.Acc_Make_Payment_ModuleId:
                url = contextpath + "/ACCVendorPayment/updatePrint.do" + commonParams + "&withInv=false&quotationFlag=false";
                break;

        }

        //Android don't want to see the print button in html while in application it is required-Neeraj D
//        if (StringUtil.isNullOrEmpty(request.getParameter("isAndroidIphoneCall"))) {
//            printbuttondisplay = "<div style='position: relative; width: 950px;'><button id = 'print' title='Print Invoice' onclick= \" window.print(); updatePrint(\'" + moduleID + "\',\'" + recordids + "\'); \" style='color: rgb(8, 55, 114);' href='#'> Print </button></div>";
//        }
        boolean isAutoPopulate = request.getParameter("isAutoPopulate") != null ? Boolean.parseBoolean(request.getParameter("isAutoPopulate")) : false;
        String path = HttpUtils.getRequestURL(request).toString();
        String servPath = request.getServletPath();
        String uri = path.replace(servPath, "/");
        String htmlhead = "<!DOCTYPE><html><head>"
                + "<script type=\"text/javascript\" src=\"" + uri + "lib/ext-4/ext-all.js\"></script>"
                + "<script type=\"text/javascript\" src=\"" + uri + "lib/jquery.js\"></script>"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + uri + "lib/ext-4/ext-theme-classic-all.css\"/ >"
                + "<script type=\"text/javascript\" src=\"" + uri + "scripts/designer/ExtWatermark.js\"></script>";
                if (isExtendLineItem || isExtendedGlobalTable) {
                    htmlhead += "<script type=\"text/javascript\" src=\"" + uri + "scripts/designer/extendedLineItem.js\"></script>";
                } else {
                    htmlhead += "<script type=\"text/javascript\" src=\"" + uri + "scripts/designer/addborderatEOP.js\"></script>";
                }
                if(isPrePrented){
                    htmlhead += "<script type=\"text/javascript\" src=\"" + uri + "scripts/designer/prePrintedFormat.js\"></script>";
                }
                htmlhead += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + uri + "style/custom-template-designer.css\"/>"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + uri + "style/documentdesignerprint.css\"/>"
                +   "<link href=\""+ uri +"images/favicon.png\" rel=\"shortcut icon\"/>"
                //                    + "<script src='http://codepen.io/assets/libs/fullpage/jquery.js'></script>"
                + "<style type=\"text/css\">"
                + "thead {display: table-header-group;}"
                + "tfoot {display: table-footer-group;}"
                + "tbody {display: table-row-group;}"
                + ".divFooter {position: fixed; bottom: 0; display: block;}"
                + " @page { margin-top:" + pageTopMargin + "; margin-bottom:" + pageBottomMargin + "; margin-right:" + pageRightMargin + "; margin-left:" + pageLeftMargin + "; } "
                + " @media screen {#page1 { right: 0;left: 0;margin-right: auto;margin-left: auto;width: "+pageWidthStr+";} #pageFooter { display: none;}}"
                + " @media print {#footer {visibility: hidden !important;} #pagenumberspan:after {counter-increment:page; content: \" \" counter(page); }"
                + " #watermark {position: fixed;} "
                + " .customPageBorder { position: fixed; z-index: 9999; "
                + topPageBorderStyle + bottomPageBorderStyle + rightPageBorderStyle + leftPageBorderStyle 
                + "bottom: 0; height: 100%; left: 0; right: 0; top: 0; width: 100%;} } "
                + "body * {font-size:" + fontSize + ";}"
                + "</style>"
                + "</head>";
                if ( isExtendLineItem ) {
                    if(isAutoPopulate){
                        htmlhead +=  "<body onload = 'loadExtendedItem(\""+pageSize+"\",\""+pageOrientation+"\",\"0\",\""+adjustPageHeight+"\"); self.print();'  class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                    } else{
                        htmlhead +=  "<body onload = 'loadExtendedItem(\""+pageSize+"\",\""+pageOrientation+"\",\"0\",\""+adjustPageHeight+"\")'  class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                    }
                } else if( isPrePrented ){
                    htmlhead += "<body onload = 'loadPrePrintedPage()' class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                } else if (isExtendedGlobalTable) {
                    if(isAutoPopulate){
                        htmlhead +=  "<body onload = 'loadExtendedItem(\""+pageSize+"\",\""+pageOrientation+"\",\"1\",\""+adjustPageHeight+"\"); self.print();'  class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                    } else{
                        htmlhead +=  "<body onload = 'loadExtendedItem(\""+pageSize+"\",\""+pageOrientation+"\",\"1\",\""+adjustPageHeight+"\")'  class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                    }
                } else {
                    if(isAutoPopulate){
                        htmlhead += "<body onload = 'addborder(\""+pageSize+"\",\""+pageOrientation+"\"); self.print();'  class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                    } else{
                        htmlhead += "<body onload = 'addborder(\""+pageSize+"\",\""+pageOrientation+"\")' class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                    }
                }
//                    + "<div id='page1'>";
//        if (StringUtil.equal(fileType, "print")&& !checkfooterflag) {
//            buildhtml = htmlhead + buildhtml + "</page></body></html>";
//
//            buildhtml = CustomDesignHandler.processFontTags(buildhtml);
//            //          buildhtml = "<div style='width: 800; height:800; margin: 0px auto; position: relative; border: 2px solid;'>"+buildhtml+"</div>";
//            response.getOutputStream().write(buildhtml.getBytes());
//            response.getOutputStream().flush();
//            response.getOutputStream().close();
//        } else
        String footerdiv = "";
        String divStr = "";
        for (int index = 0; index < buildhtml.size(); index++) {
            html = removeObjectBorderStyles(buildhtml.get(index).toString());
            footer = removeObjectBorderStyles(pagefooterhtml.get(index).toString());
            header = removeObjectBorderStyles(pageheaderhtml.get(index).toString());
            Pattern p = Pattern.compile("\\>\\s+\\<");
            Matcher m = p.matcher(html); 
            html = m.replaceAll("><");
            Matcher m1 = p.matcher(header); 
            header = m1.replaceAll("><");
            Matcher m2 = p.matcher(footer); 
            footer = m2.replaceAll("><");
            Date newDate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            String currentDate=authHandler.getUTCToUserLocalDateFormatter_NEW(request, newDate);
            Format monthFormatter = new SimpleDateFormat("MMMM"); 
            Format yearFormatter = new SimpleDateFormat("YYYY"); 
            String currentMonth = monthFormatter.format(newDate);
            String currentYear = yearFormatter.format(newDate);
            
            String fullHtml = "";
            if (StringUtil.equal(fileType, "print")) {
                if (!StringUtil.isNullOrEmpty(header)) {
                    header = header.replace("#Page Number#", "");
                    header = header.replace("#Current Date#", currentDate);
                    header = header.replace("#Current Month#", currentMonth);
                    header = header.replace("#Current Year#", currentYear);
                    finalHeader = "<thead  align='left' style='display: table-header-group;position:relative;'><tr><td>" + header + "</td></tr></thead>";
                }
                if (!StringUtil.isNullOrEmpty(footer)) {
                    footer = footer.replace("#Page Number#", "");
                    footer = footer.replace("#Current Date#", currentDate);
                    footer = footer.replace("#Current Month#", currentMonth);
                    footer = footer.replace("#Current Year#", currentYear);
                    footerdiv = "<div class='divFooter' id='pageFooter'><div style='position:relative'>" + footer + "</div></div>";
                    /*
                     * replaced the id with another value as it was get duplicated
                     */
                    footer = footer.replace("pagenumberspan", "pagenumberspannew");
                }
                if (!StringUtil.isNullOrEmpty(footer)) {
                    footer = footer.replace("#Page Number#", "");
                    footer = footer.replace("#Current Date#", currentDate);
                    footer = footer.replace("#Current Month#", currentMonth);
                    footer = footer.replace("#Current Year#", currentYear);
                    finalFooter = "<tfoot id='footer' align='left' style='display: table-footer-group;position:relative;'><tr><td>" + footer + "</td></tr></tfoot>";
                }
                html = html.replaceAll("#Current Date#", currentDate);
                html = html.replaceAll("#Current Month#", currentMonth);
                html = html.replaceAll("#Current Year#", currentYear);
                String border = "<div class=\"customPageBorder\"></div>";
                if(!isPrePrented){
                    fullHtml = "<div id='page1'>" + footerdiv;
                    if(isMultipleTransaction){
                        fullHtml += "<table style='width:100%;'>";
                } else{
                        fullHtml += "<table style='width:100%;page-break-after: always;'>";
                    }
                    fullHtml += finalHeader + "<tbody>" + html + "</tbody>" + finalFooter + "</table></div>";
                } else{
                    fullHtml = "<div id='page1'><table style='width:100%;page-break-after: always;'><tbody>" + html + "</tbody></table></div> " ;
                }
                if ( isPageBorderIncluded ) {
                    fullHtml += border; 
                }
            } else {
                if (!StringUtil.isNullOrEmpty(header)) {
                    finalHeader = "<thead align='left' style='display: table-header-group'><tr><td>" + header + "</td></tr></thead>";
                }
                if (!StringUtil.isNullOrEmpty(footer)) {
                    footerdiv = "<div class='divFooter' id='pageFooter'><div style='position:relative'>" + footer + "</div></div>";
                }
                if (!StringUtil.isNullOrEmpty(footer)) {
                    finalFooter = "<tfoot id='footer' align='left' style='display: table-footer-group'><tr><td>" + footer + "</td></tr></tfoot>";
                }
                fullHtml = "<div id='page1'>" + footerdiv + "<table style='width:100%'>" + finalHeader + "<tbody>" + html + "</tbody>" + finalFooter + "</table>";
            }
            divStr += fullHtml;
        }
        String watermark = "";
        if (!StringUtil.isNullOrEmpty(watermarkProperties)) {
            watermark = "<div id='watermark' class='watermark' properties='" + watermarkProperties + "'></div>";
        }
        String finalHtml = htmlhead + divStr + watermark + "</body></html>";
        
        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(finalHtml.getBytes());
        response.getOutputStream().flush();
        response.getOutputStream().close();

//        finalHtml = CustomDesignHandler.processFontTags(finalHtml);
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        writeDataToFile(filename, baos, response);
//        baos.close();
//        }
    }
    
    
  /*Request dependency Removed*/  
    public static void writeFinalDataToFileJSONNew(String filename, String fileType, ArrayList<String> buildhtml, String pagelayoutproperty,
            ArrayList<String> pagefooterhtml, HttpServletResponse response, int moduleid, String recordids,
           JSONObject paramJobj, ArrayList<String> fontstylevalue, ArrayList<String> pageheaderhtml, Boolean checkfooterflag, ArrayList<String> pagefontsize,String extLIJobjString) throws IOException, JSONException, SessionExpiredException, ParseException {
        
        String  finalFooter = "", finalHeader = "";
        String html = "";
        JSONObject extLIJobj=new JSONObject(extLIJobjString);
        boolean isExtendLineItem=extLIJobj.optBoolean("isExtendLineItem",false);
        boolean isPrePrented = extLIJobj.optBoolean("isPrePrinted",false);
        String pageSize=extLIJobj.optString("pageSize","a4");
        String pageOrientation=extLIJobj.optString("pageOrientation","portrait");
        String footer = "";
        String header = "";
        String fontSize = pagefontsize.get(0) != null ? pagefontsize.get(0): "12";
        String fontValue = fontstylevalue.get(0) != null ?  fontstylevalue.get(0) : "calibri";
        boolean isPageBorderIncluded = false;
        String pageTopMargin = "72px";
        String pageRightMargin = "49px";
        String pageBottomMargin = "140px";
        String pageLeftMargin = "72px";
        boolean bottomPageBorder = false;
        String bottomPageBorderStyle = "";
        boolean leftPageBorder = false;
        String leftPageBorderStyle = "";
        boolean rightPageBorder = false;
        String rightPageBorderStyle = "";
        boolean topPageBorder = false;
        String topPageBorderStyle = "";
        String watermarkProperties = "{}";
        
        if (!StringUtil.isNullOrEmpty(pagelayoutproperty)) {
            JSONArray jArr1 = new JSONArray(pagelayoutproperty);
            for (int cnt = 0; cnt < jArr1.length(); cnt++) {
                try {
                    if (jArr1.get(cnt) != null && jArr1.get(cnt).getClass().equals(JSONObject.class)) {
                        JSONObject temp = jArr1.getJSONObject(cnt);
                        if (temp.has("pagelayoutsettings")) {
                            JSONObject pageLayoutSetting = temp.getJSONObject("pagelayoutsettings");
                            isPageBorderIncluded = pageLayoutSetting.getBoolean("pageBorderIncluded");
                            pageBottomMargin = pageLayoutSetting.getString("pagebottom");
                            pageLeftMargin = pageLayoutSetting.getString("pageleft");
                            pageRightMargin = pageLayoutSetting.getString("pageright");
                            pageTopMargin = pageLayoutSetting.getString("pagetop");
                            bottomPageBorder = pageLayoutSetting.getBoolean("bottomPageBorder");
                            leftPageBorder = pageLayoutSetting.getBoolean("leftPageBorder");
                            rightPageBorder = pageLayoutSetting.getBoolean("rightPageBorder");
                            topPageBorder = pageLayoutSetting.getBoolean("topPageBorder");
                        } else if (temp.has("watermarkProperties")) {
                            watermarkProperties = temp.toString();
                        }
                    }
                } catch (Exception ex) {
                }
            }
        }
        
        if ( topPageBorder ) {
            topPageBorderStyle = "border-top: 1px solid; "; // ERP-18286 : Dotted page border is applying for only first page in print
        } else {
            topPageBorderStyle = "border-top: none; ";
        }
        
        if ( bottomPageBorder ) {
            bottomPageBorderStyle = "border-bottom: 1px solid; ";   // ERP-18286 : Dotted page border is applying for only first page in print
        } else {
            bottomPageBorderStyle = "border-bottom: none; ";
        }
        
        if ( rightPageBorder ) {
            rightPageBorderStyle = "border-right: 1px solid; "; // ERP-18286 : Dotted page border is applying for only first page in print
        } else {
            rightPageBorderStyle = "border-right: none; ";
        }
        
        if ( leftPageBorder ) {
            leftPageBorderStyle = "border-left: 1px solid; ";   // ERP-18286 : Dotted page border is applying for only first page in print
        } else {
            leftPageBorderStyle = "border-left: none; ";
        }
        
        double pageWidth = 21.00;
        if (isExtendLineItem) {
            if (pageOrientation.equalsIgnoreCase("portrait")) {
                if (pageSize.equalsIgnoreCase("a4")) {
                    pageWidth = 21.00;
                } else if (pageSize.equalsIgnoreCase("letter")) {
                    pageWidth = 21.59;
                } else if (pageSize.equalsIgnoreCase("a3")) {
                    pageWidth = 29.7;
                }
            } else {
                if (pageSize.equalsIgnoreCase("a4")) {
                    pageWidth = 29.7;
                } else if (pageSize.equalsIgnoreCase("letter")) {
                    pageWidth = 27.94;
                } else if (pageSize.equalsIgnoreCase("a3")) {
                    pageWidth = 42.00;
                }

            }
        }
        double rMargin = Double.parseDouble(pageRightMargin.replace("px", "").replace("cm", "").replace("mm", "").replace("in", ""));
        double lMargin = Double.parseDouble(pageLeftMargin.replace("px", "").replace("cm", "").replace("mm", "").replace("in", ""));
        rMargin = rMargin * 0.0265;
        lMargin = lMargin * 0.0265;
        pageWidth = pageWidth - (lMargin + rMargin);
        
        boolean isAutoPopulate = paramJobj.optString("isAutoPopulate") != null ? Boolean.parseBoolean(paramJobj.optString("isAutoPopulate")) : false;
        String uri = ConfigReader.getinstance().get("accURL");
        String htmlhead = "<html><head>"
                + "<script type=\"text/javascript\" src=\"" + uri + "lib/ext-4/ext-all.js\"></script>"
                + "<script type=\"text/javascript\" src=\"" + uri + "lib/jquery.js\"></script>"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + uri + "lib/ext-4/ext-theme-classic-all.css\"/ >"
                 +   "<link href=\""+ uri +"images/favicon.png\" rel=\"shortcut icon\"/>";
//                + "<script type=\"text/javascript\" src=\"" + uri + "scripts/designer/ExtLabelFormatter2.js\"></script>";
                if (isExtendLineItem) {
                    htmlhead += "<script type=\"text/javascript\" src=\"" + uri + "scripts/designer/extendedLineItem.js\"></script>";
                }
                if(isPrePrented){
                    htmlhead += "<script type=\"text/javascript\" src=\"" + uri + "scripts/designer/prePrintedFormat.js\"></script>";
                }
                htmlhead += "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + uri + "style/custom-template-designer.css\"/>"
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + uri + "style/documentdesignerprint.css\"/>"
                //                    + "<script src='http://codepen.io/assets/libs/fullpage/jquery.js'></script>"
                + "<style type=\"text/css\">"
                + "thead {display: table-header-group;}"
                + "tfoot {display: table-footer-group;}"
                + "tbody {display: table-row-group;}"
                + ".divFooter {position: fixed; bottom: 0; display: block;}"
                + " @page { margin-top:" + pageTopMargin + "; margin-bottom:" + pageBottomMargin + "; margin-right:" + pageRightMargin + "; margin-left:" + pageLeftMargin + "; } "
                + " @media screen {#page1 { right: 0;left: 0;margin-right: auto;margin-left: auto;width: "+pageWidth+"cm;} #pageFooter { display: none;}}"
                + " @media print {#footer {visibility: hidden !important;} #pagenumberspan:after {counter-increment:page; content: \" \" counter(page); } .customPageBorder { position: fixed; z-index: 9999; "
                + topPageBorderStyle + bottomPageBorderStyle + rightPageBorderStyle + leftPageBorderStyle 
                + "bottom: 0; height: 100%; left: 0; right: 0; top: 0; width: 100%;} } "
                + "body * {font-size:" + fontSize + ";}"
                + "</style>"
                + "</head>";
                if ( isExtendLineItem ) {
                    if(isAutoPopulate){
                        htmlhead +=  "<body onload = 'loadExtendedItem(\""+pageSize+"\",\""+pageOrientation+"\"); self.print();'  class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                    } else{
                        htmlhead +=  "<body onload = 'loadExtendedItem(\""+pageSize+"\",\""+pageOrientation+"\")'  class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                    }
                } else if( isPrePrented ){
                    htmlhead += "<body onload = 'loadPrePrintedPage()' class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                } else{
                    if(isAutoPopulate){
                        htmlhead += "<body onload = 'self.print()' class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                    } else{
                        htmlhead += "<body class = ' x-border-box ' style='font-family:" + fontValue + ";'>";
                    }
                }

        String footerdiv = "";
        String divStr = "";
        for (int index = 0; index < buildhtml.size(); index++) {
            html = removeObjectBorderStyles(buildhtml.get(index).toString());
            footer = removeObjectBorderStyles(pagefooterhtml.get(index).toString());
            header = removeObjectBorderStyles(pageheaderhtml.get(index).toString());
            Pattern p = Pattern.compile("\\>\\s+\\<");
            Matcher m = p.matcher(html); 
            html = m.replaceAll("><");
            Matcher m1 = p.matcher(header); 
            header = m1.replaceAll("><");
            Matcher m2 = p.matcher(footer); 
            footer = m2.replaceAll("><");
            Date newDate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            String currentDate=authHandler.getUTCToUserLocalDateFormatter_NEWJson(paramJobj, newDate);
            Format monthFormatter = new SimpleDateFormat("MMMM"); 
            Format yearFormatter = new SimpleDateFormat("YYYY"); 
            String currentMonth = monthFormatter.format(newDate);
            String currentYear = yearFormatter.format(newDate);
            
            String fullHtml = "";
            if (StringUtil.equal(fileType, "print")) {
                if (!StringUtil.isNullOrEmpty(header)) {
                    header = header.replace("#Page Number#", "");
                    header = header.replace("#Current Date#", currentDate);
                    header = header.replace("#Current Month#", currentMonth);
                    header = header.replace("#Current Year#", currentYear);
                    finalHeader = "<thead  align='left' style='display: table-header-group;position:relative;'><tr><td>" + header + "</td></tr></thead>";
                }
                if (!StringUtil.isNullOrEmpty(footer)) {
                    footer = footer.replace("#Page Number#", "");
                    footer = footer.replace("#Current Date#", currentDate);
                    footer = footer.replace("#Current Month#", currentMonth);
                    footer = footer.replace("#Current Year#", currentYear);
                    footerdiv = "<div class='divFooter' id='pageFooter'><div style='position:relative'>" + footer + "</div></div>";
                }
                if (!StringUtil.isNullOrEmpty(footer)) {
                    footer = footer.replace("#Page Number#", "");
                    footer = footer.replace("#Current Date#", currentDate);
                    footer = footer.replace("#Current Month#", currentMonth);
                    footer = footer.replace("#Current Year#", currentYear);
                    finalFooter = "<tfoot id='footer' align='left' style='display: table-footer-group;position:relative;'><tr><td>" + footer + "</td></tr></tfoot>";
                }
                html = html.replaceAll("#Current Date#", currentDate);
                html = html.replaceAll("#Current Month#", currentMonth);
                html = html.replaceAll("#Current year#", currentYear);
                String border = "<div class=\"customPageBorder\"></div>";
                if(!isPrePrented){
                fullHtml = "<div id='page1'>" + footerdiv + "<table style='width:100%;page-break-after: always;'>" + finalHeader + "<tbody>" + html + "</tbody>" + finalFooter + "</table>"
                        + "</div> " ;
                } else{
                    fullHtml = "<div id='page1'><table style='width:100%;page-break-after: always;'><tbody>" + html + "</tbody></table></div> " ;
                }
                if ( isPageBorderIncluded ) {
                    fullHtml += border; 
                }
            } else {
                if (!StringUtil.isNullOrEmpty(header)) {
                    finalHeader = "<thead align='left' style='display: table-header-group'><tr><td>" + header + "</td></tr></thead>";
                }
                if (!StringUtil.isNullOrEmpty(footer)) {
                    footerdiv = "<div class='divFooter' id='pageFooter'><div style='position:relative'>" + footer + "</div></div>";
                }
                if (!StringUtil.isNullOrEmpty(footer)) {
                    finalFooter = "<tfoot id='footer' align='left' style='display: table-footer-group'><tr><td>" + footer + "</td></tr></tfoot>";
                }
                fullHtml = "<div id='page1'>" + footerdiv + "<table style='width:100%'>" + finalHeader + "<tbody>" + html + "</tbody>" + finalFooter + "</table>";
            }
            divStr += fullHtml;
        }
        String finalHtml = htmlhead + divStr + "</body></html>";
        response.setContentType("text/html;charset=UTF-8");
        response.getOutputStream().write(finalHtml.getBytes());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }
            
    private static String removeObjectBorderStyles(String html) {
        html = html.replaceAll("#B5B8C8", "#FFFFFF");
        html = html.replaceAll("181, 184, 200", "255, 255, 255");
//        html = html.replaceAll("border-width: 1px;", "border-width: 0px;");
        return html;
    }

    private static String addExportPD4MLCSS(String htmlContent) {
//        String htmlhead = "<html><head><LINK href='../style/view.css' type=text/css rel=StyleSheet></head><body>";
        String htmlhead = "<html><head></head><style>body {font-family: Tahoma, Verdana, Arial, Helvetica, sans-sarif; margin: 0; padding: 0; border: 0 none; overflow: auto; font-size: 12px;}</style><body>";
        return (htmlhead + htmlContent + "</body></html>");
    }

    private static void writeDataToFile(String filename, ByteArrayOutputStream baos, HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setContentType("application/octet-stream");
        response.setContentLength(baos.size());
        response.getOutputStream().write(baos.toByteArray());
        response.getOutputStream().flush();
        response.getOutputStream().close();
    }
//    public static PD4ML getPD4MLHeaderFooter() {
//            PD4ML pd4ml = new PD4ML();
//            int topValue = 10;
//            int leftValue = 10;
//            int rightValue = 10;
//            int bottomValue = 10;
//            int HTMLWidth = 900;
//            Dimension format = PD4Constants.A4;
//            boolean landscapeValue = false;
//
//            pd4ml.enableDebugInfo();
//            pd4ml.setHtmlWidth(HTMLWidth);
//            pd4ml.setPageSize( landscapeValue ? pd4ml.changePageOrientation( format ): format );
//            pd4ml.setPageInsetsMM( new Insets(topValue, leftValue,bottomValue, rightValue) );
//            // Set Footer and Header
////            PD4PageMark header = new PD4PageMark();
////    //                header.setWatermark( "/images/bnm_logo.gif", new java.awt.Rectangle(10,10,120,25), 50 );
////            header.setAreaHeight( -1 );
////    //                header.setHtmlTemplate("<html><style>*{margin:0;outline:none;}.top{width:100%;	margin:auto;}.bnm-logo-center{width:92%;	text-align:center;	float:left;	padding-top:10px;	padding-bottom:10px;}.sulit-txt{	text-align:left;	float:right;	width:7%;	padding-right:0px;	padding-top: 10px;	font-family:Arial, Helvetica, sans-serif;color:#333333;	text-decoration:none;}.top-hr{width:90%;float:left;}"
////    //                + "</style><body>"
////    //                + "<div class='top'><div class='bnm-logo-center'><img src='/images/bnm_logo.gif' border='0'  /></div> <div class='sulit-txt'>SULIT</div>  <hr class='top-hr' /></div></body></html>");
////            header.setHtmlTemplate("<html><style>*{margin:0;outline:none;}.top{width:100%;	margin:auto;}.bnm-logo-center{width:94%;	text-align:center;	float:left;	padding-top:10px;	padding-bottom:10px;}.sulit-txt{	text-align:float;	float:right;	width:5%;	padding-right:0px;	padding-top: 20px;font-size: 12px;	font-family:Arial, Helvetica, sans-serif;color:#333333;	text-decoration:none;}.top-hr{width:100%;float:left;margin-bottom:10px}"
////                + "</style><body>"
////                + "<div class='top'><div class='bnm-logo-center'><img src='images/deskera-logo.jpg' border='0'  /></div> <div class='sulit-txt'>INVOICE</div></div> <hr class='top-hr' /></body></html>");
////            pd4ml.setPageHeader( header );
////
////            PD4PageMark footer = new PD4PageMark();
////            footer.setAreaHeight( 50 );
////            footer.setHtmlTemplate( "<html><style>*{margin:0;outline:none;}.bottom{width:100%;	margin:auto;}.bnm-version-left{width:80%;	text-align:left;	float:left;	padding-right:10px;	padding-top: 5px;font-size: 12px;	font-family:Arial, Helvetica, sans-serif;color:#333333;	text-decoration:none;}.pdfpaging-txt{	text-align:right;	float:right;	width:10%;	padding-right:0px;	padding-top: 5px;font-size: 12px;	font-family:Arial, Helvetica, sans-serif;color:#333333;	text-decoration:none;}"
////            + "</style><body>"
////            + "<div class='bottom'><div class='bnm-version-left'> THANK YOU FOR YOUR BUSINESS</div>    <div class='pdfpaging-txt'>Page $[page]</div></div></body></html>" );
////
////            pd4ml.setPageFooter( footer );
//            return pd4ml;
//    }
//
    //    public String numberFormatter(double values, String compSymbol) {
//        NumberFormat numberFormatter;
//        java.util.Locale currentLocale = java.util.Locale.US;
//        numberFormatter = NumberFormat.getNumberInstance(currentLocale);
//        numberFormatter.setMinimumFractionDigits(2);
//        numberFormatter.setMaximumFractionDigits(2);
//        return (compSymbol + numberFormatter.format(values));
//    }

//    public static PD4ML getPD4MLHeaderFooter(String pagelayoutproperty,String pagefooterhtml) throws JSONException{
//         PD4ML pd4ml = new PD4ML();
//            int topValue = 10;
//            int leftValue = 10;
//            int rightValue = 10;
//            int bottomValue = 10;
//            int HTMLWidth = 900;
//            Dimension format = PD4Constants.A4;
//            boolean landscapeValue = false;
//            String pageFooterFormat="";
//            String pageNumberFormat="";
//            String pageNumberAlign="";
//            String halign = "C";
//
//            if (!StringUtil.isNullOrEmpty(pagelayoutproperty)) {
//                try {
//                    JSONArray jArr1 = new JSONArray(pagelayoutproperty);
//                    for (int cnt = 0; cnt < jArr1.length(); cnt++) {
//                        JSONObject jObj = jArr1.getJSONObject(cnt);
//                        if(!StringUtil.isNullOrEmpty(jObj.optString("isportrait",""))){
//                            if (!StringUtil.isNullOrEmpty(jObj.optString("top"))) {
//                                topValue=Integer.parseInt(jObj.getString("top"));
//                            }
//                            if (!StringUtil.isNullOrEmpty(jObj.optString("right",""))) {
//                                rightValue=Integer.parseInt(jObj.getString("right"));
//                            }
//                            if (!StringUtil.isNullOrEmpty(jObj.optString("bottom",""))) {
//                                bottomValue=Integer.parseInt(jObj.getString("bottom"));
//                            }
//                            if (!StringUtil.isNullOrEmpty(jObj.optString("left",""))) {
//                                leftValue=Integer.parseInt(jObj.getString("left"));
//                            }
//                        }
////                        if(!StringUtil.isNullOrEmpty(jObj.optString("pagefooter",""))){
////                            JSONObject footerConfig = new JSONObject(jObj.optString("pagefooter",""));
////                            if (footerConfig.getBoolean("ispagefooter")) {
////                                pageFooterFormat=footerConfig.getString("text");
////                            }
////                            halign = footerConfig.getString("halign");
////                        }
//                        if(!StringUtil.isNullOrEmpty(jObj.optString("pagenumber",""))){
//                            JSONObject pageNumberConfig = new JSONObject(jObj.optString("pagenumber",""));
//                            if (pageNumberConfig.getBoolean("ispagenumber")) {
//                                pageNumberFormat=pageNumberConfig.getString("pagenumberformat");
//                                pageNumberAlign=pageNumberConfig.getString("pagenumberalign");
//                            }
//                        }
//                    }
//                } catch(Exception ex) {
//                    Logger.getLogger(CustomDesignHandler.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//
//            pd4ml.enableDebugInfo();
//            pd4ml.protectPhysicalUnitDimensions();
//            pd4ml.setHtmlWidth(HTMLWidth);
//            pd4ml.setPageSize( landscapeValue ? pd4ml.changePageOrientation( format ): format );
//            pd4ml.setPageInsets(new Insets(topValue, leftValue,bottomValue, rightValue) );
////            Map m = new java.util.HashMap();  
////            m.put(PD4Constants.PD4ML_ABSOLUTE_ADDRESS_SPACE, "document");  
////            pd4ml.setDynamicParams(m); 
//            // Set Footer and Header
////            PD4PageMark header = new PD4PageMark();
////    //                header.setWatermark( "/images/bnm_logo.gif", new java.awt.Rectangle(10,10,120,25), 50 );
////            header.setAreaHeight( -1 );
////    //                header.setHtmlTemplate("<html><style>*{margin:0;outline:none;}.top{width:100%;	margin:auto;}.bnm-logo-center{width:92%;	text-align:center;	float:left;	padding-top:10px;	padding-bottom:10px;}.sulit-txt{	text-align:left;	float:right;	width:7%;	padding-right:0px;	padding-top: 10px;	font-family:Arial, Helvetica, sans-serif;color:#333333;	text-decoration:none;}.top-hr{width:90%;float:left;}"
////    //                + "</style><body>"
////    //                + "<div class='top'><div class='bnm-logo-center'><img src='/images/bnm_logo.gif' border='0'  /></div> <div class='sulit-txt'>SULIT</div>  <hr class='top-hr' /></div></body></html>");
////            header.setHtmlTemplate("<html><style>*{margin:0;outline:none;}.top{width:100%;	margin:auto;}.bnm-logo-center{width:94%;	text-align:center;	float:left;	padding-top:10px;	padding-bottom:10px;}.sulit-txt{	text-align:float;	float:right;	width:5%;	padding-right:0px;	padding-top: 20px;font-size: 12px;	font-family:Arial, Helvetica, sans-serif;color:#333333;	text-decoration:none;}.top-hr{width:100%;float:left;margin-bottom:10px}"
////                + "</style><body>"
////                + "<div class='top'><div class='bnm-logo-center'><img src='images/deskera-logo.jpg' border='0'  /></div> <div class='sulit-txt'>INVOICE</div></div> <hr class='top-hr' /></body></html>");
////            pd4ml.setPageHeader( header );
////
//            PD4PageMark footer = new PD4PageMark();
//            footer.setAreaHeight(-1);
//            footer.setTitleAlignment(halign.equals("L") ? PD4PageMark.LEFT_ALIGN: (halign.equals("R") ? PD4PageMark.RIGHT_ALIGN : PD4PageMark.CENTER_ALIGN));
////            footer.setHtmlTemplate(pageFooterFormat);
//            footer.setHtmlTemplate(pagefooterhtml);
//            String pageTemplate="";
//            if(pageNumberFormat.equals("1")){
//                pageTemplate = "$[page]";
//            }else if(pageNumberFormat.equals("2")){
//                pageTemplate="Page $[page] of $[total]";
//            }
//            footer.setPageNumberTemplate(pageTemplate);
//            if(pageNumberAlign.equals("BL")){
//                footer.setPageNumberAlignment(PD4PageMark.LEFT_ALIGN);  
//            }else if(pageNumberAlign.equals("BR")){
//                footer.setPageNumberAlignment(PD4PageMark.RIGHT_ALIGN); 
//            }else{
//                footer.setPageNumberAlignment(PD4PageMark.CENTER_ALIGN); 
//            }  
//            footer.setInitialPageNumber(1);  
//            pd4ml.setPageFooter( footer );
//            return pd4ml;
//    }
//    
    @Deprecated
    public static String getFormattedAmount(double value) {
        String str = authHandler.getCompleteDFStringForAmount("###0.");
        DecimalFormat df = new DecimalFormat(str);
        return df.format(value);
    }

    public static String getAmountinCommaForm(double value, int countryId) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
        String str = nf.format(value);
        if(countryId == Constants.indian_country_id){
           str = indianFormat(value,-1); // decimal value = -1 
        }
        return str;
    }
    public static String getAmountinCommaDecimal(double value,int decimal,int countryId) {//get amount in comma form and setting decimals.
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(decimal);// set the decimal poiints.
        nf.setMaximumFractionDigits(decimal);
        nf.setGroupingUsed(true);//property to show in comma format
        String str = nf.format(value);
        if(countryId == Constants.indian_country_id){
           str = indianFormat(value,decimal);
        }
        return str;
    }
    /**
     * Replace amount with space if amount is zero(0)
     **/
    public static String getZeroAmountAsBlank(String value) {//get amount as blank if value is Zero(0)
        String returnValue = "";
        String tempValue = value;
        tempValue = tempValue.replaceAll("(,)|(\\()|(\\))", "");//replace comma(,) and brackates from value
        if(Double.parseDouble(tempValue) == 0){
            returnValue = "";
        } else{
            returnValue = value;
        }
        return returnValue;
    }

    public static Map getSortedMap(Map<String, Integer> Items) {
        ValueComparator bvc = new ValueComparator(Items);
        TreeMap<String, Integer> sorted_map = new TreeMap<String, Integer>(bvc);
        sorted_map.putAll(Items);
        return sorted_map;
    }

    static class ValueComparator implements Comparator<String> {

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

    public static String processFontTags(String buildhtml) {
        Document jsoupDoc = Jsoup.parse(buildhtml);
        Elements font = jsoupDoc.select("font");
        for (Element e : font) {
            Attributes attrs = e.attributes().clone();
            StringBuilder styleString = new StringBuilder();
            String theSizeString = "";
            for (Attribute attribute : attrs) {
                String attributeName = attribute.getKey();
                String attributeValue = attribute.getValue();
                if (attributeName.equalsIgnoreCase("face")) {
                    styleString.append(" font-family: ");
                    styleString.append(attributeValue + ";");
                } else if (attributeName.equalsIgnoreCase("size")) {
                    int theSize = Integer.valueOf(org.apache.commons.lang.StringEscapeUtils.unescapeJava(attributeValue.trim()).replaceAll("\"", ""));
                    if (theSize <= FONT_VALUES.length) {
                        theSizeString = FONT_VALUES[theSize - 1];
                    }
                } else if (attributeName.equalsIgnoreCase("color")) {
                    styleString.append(" color: ");
                    styleString.append("#" + attributeValue + ";");
                } else if (attributeName.equalsIgnoreCase("style")) {
                    // possible that font size specified here (font-size: xxpt)
                    int size = attributeValue.indexOf("font-size:");
                    if (size != -1) {
                        size += "font-size:".length();
                        theSizeString = getFontSize(attributeValue.substring(size));
                    }
                }
                e.removeAttr(attributeName);
            }
            if (theSizeString.length() > 0) {
                styleString.append(" font-size: ");
                styleString.append(theSizeString + ";");
            }
            e.tagName("span");
            e.attr("style", styleString.toString());
        }
        return jsoupDoc.outerHtml();
    }

    /**
     * Expected format of the input is font-size: NNpt Note that there may be
     * other information after the pt font-size: NNpt font-family: .... If the
     * string is not formatted correctly, return a medium font as default
     */
    private static String getFontSize(String inputStyle) {
        int theSize = 1;
        String theReturn;
        int thePointStart = inputStyle.indexOf(' '), thePointEnd = inputStyle.lastIndexOf("pt");
        if (thePointStart == -1) {
            thePointStart = 0;
        }
        while ((inputStyle.charAt(thePointStart) == ' ') && (thePointStart < inputStyle.length())) {
            thePointStart++;
        }
        if (thePointStart >= thePointEnd) {
            theReturn = MEDIUM_FONT; // average middle font
        } else {
            theSize = Integer.valueOf(inputStyle.substring(thePointStart, thePointEnd));
            theReturn = FONT_MAP.ceilingEntry(theSize).getValue();
        }
        return theReturn;
    }

    public static boolean isArray(final Object obj) {
        return obj instanceof Object[];
    }

    public static JSONObject getJSONObjectFromItemID(JSONArray jArr, String ObjectID) throws JSONException {
        JSONObject finalJObj = new JSONObject();
        for (int cnt = 0; cnt < jArr.length(); cnt++) {
            JSONObject jObj = jArr.getJSONObject(cnt);
            if (!StringUtil.isNullOrEmpty(jObj.optString("id", "")) && jObj.optString("id", "").equals(ObjectID)) {
                finalJObj = jObj;
                break;
            }
        }
        return finalJObj;
    }

    public static JSONArray sortJsonArrayOnFieldNames(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    String lid = "", rid = "";
                    try {
                        lid = lhs.getString("label");
                        rid = rhs.getString("label");
                    } catch (JSONException ex) {
                        Logger.getLogger(CustomDesignHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return lid.compareTo(rid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(CustomDesignHandler.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }
        return new JSONArray(jsons);
    }

    public static ArrayList<String> getSelectedBillIDs(String invoiceIds) {
        ArrayList<String> SOIDList = new ArrayList<String>(Arrays.asList(invoiceIds.split(",")));
        return SOIDList;
    }
    
    public static String buildSqlQuery(String JSON, String mainTable) {
        String finalQuery = "";
        try {
            ArrayList<String> refTableList = new ArrayList<String>();
            HashMap<String, String> joinMap = new HashMap<String, String>();
            HashMap<String, String> dataIndexList = new HashMap<String, String>();
            HashMap<String, String> dataIndexReftableMap = new HashMap<String, String>();
            JSONArray jArr = new JSONArray(JSON);
            for (int cnt = 0; cnt < jArr.length(); cnt++) {
                JSONObject jObj = jArr.getJSONObject(cnt);
                boolean isCreatedAliase = false;
                if (!StringUtil.isNullOrEmpty(jObj.optString("dbcolumnname", "")) && !jObj.optBoolean("customfield", false)) {
                    String refTable = jObj.getString("reftablename");
                    String refTable1 = jObj.getString("reftablename");
                    if (!refTableList.contains(refTable) && !refTable.equals(mainTable)) {
                        refTableList.add(refTable);
                        if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                            joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + mainTable + "." + jObj.getString("dbcolumnname") + " ");
                        } else { // otherwise fetch join constraint from constant variable
                            joinMap.put(refTable, CustomDesignerConstants.CustomDesignJoinMap.get(mainTable.concat("_").concat(refTable)));
                        }
                    } else if (refTableList.contains(refTable)) {
                        String colname1 = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                        colname1 = refTable + "." + colname1;
                        if (dataIndexList.containsKey(colname1)) {
                            refTable1 = refTable + "1";
                            isCreatedAliase = true;
                            refTableList.add(refTable);
                            if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                joinMap.put(refTable1, " left join " + refTable + " as " + refTable1 + " on " + refTable1 + "." + jObj.getString("reftablefk") + " = " + mainTable + "." + jObj.getString("dbcolumnname") + " ");
                            }
                        }
                    }
                    dataIndexReftableMap.put("#" + jObj.getString("label") + "#", refTable);
                    if (mainTable.equals(refTable)) {
                        dataIndexList.put(refTable.concat(".").concat(jObj.getString("dbcolumnname")), "#" + jObj.getString("label") + "#");
                    } else if (isCreatedAliase) {
                        String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                        dataIndexList.put(refTable1.concat(".").concat(colname), "#" + jObj.getString("label") + "#");
                        isCreatedAliase = false;
                    } else {
                        String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                        dataIndexList.put(refTable.concat(".").concat(colname), "#" + jObj.getString("label") + "#");
                    }
                } else if (!StringUtil.isNullOrEmpty(jObj.optString("cellplaceholder", ""))) {
                    JSONArray cellFieldsArr = new JSONArray(jObj.optString("cellplaceholder", ""));
                    for (int fieldCnt = 0; fieldCnt < cellFieldsArr.length(); fieldCnt++) {
                        jObj = cellFieldsArr.getJSONObject(fieldCnt);
                        if (!StringUtil.isNullOrEmpty(jObj.optString("dbcolumnname", "")) && !jObj.optBoolean("customfield", false)) {
                            String refTable = jObj.getString("reftablename");
                            String refTable1=jObj.getString("reftablename");;
                            if (!refTableList.contains(refTable) && !refTable.equals(mainTable)) {
                                refTableList.add(refTable);
                                if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                    joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + mainTable + "." + jObj.getString("dbcolumnname") + " ");
                                } else { // otherwise fetch join constraint from constant variable
                                    joinMap.put(refTable, CustomDesignerConstants.CustomDesignJoinMap.get(mainTable.concat("_").concat(refTable)));
                                }
                            }
                            else if(refTableList.contains(refTable)){
                                String colname1 = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                                colname1=refTable+"."+colname1;
                                    if (dataIndexList.containsKey(colname1)) {
                                        refTable1 = refTable + "1";
                                        isCreatedAliase = true;
                                        refTableList.add(refTable);
                                        if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                            joinMap.put(refTable1, " left join " + refTable + " as " + refTable1 + " on " + refTable1 + "." + jObj.getString("reftablefk") + " = " + mainTable + "." + jObj.getString("dbcolumnname") + " ");
                                        } 
                                    }
                            }
                            dataIndexReftableMap.put("#" + jObj.getString("label") + "#", refTable);
                            if (mainTable.equals(refTable)) {
                               dataIndexList.put(refTable.concat(".").concat(jObj.getString("dbcolumnname")), "#" + jObj.getString("label") + "#" );
                            } else if(isCreatedAliase) {
                                String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                                dataIndexList.put(refTable1.concat(".").concat(colname), "#" + jObj.getString("label") + "#");
                                isCreatedAliase = false;
                            }else
                            {
                                String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                                dataIndexList.put(refTable.concat(".").concat(colname), "#" + jObj.getString("label") + "#");
                            }
                        }
                    }
                }
            }
            if (!dataIndexList.isEmpty()) {
                StringBuilder selectQuery = buildSelectQuery(dataIndexList);
                StringBuilder joinQuery = buildJoinQuery(mainTable, joinMap);
                finalQuery = "select " + selectQuery.append(joinQuery);
            }
        } catch (JSONException ex) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalQuery;
    }
    
    public static StringBuilder buildSelectQuery(HashMap<String, String> dataIndexList) {
        StringBuilder selectQuery = new StringBuilder();
        Set<String> keys = dataIndexList.keySet();
        boolean isFirst = true;
        for (String fieldname : keys) {
            {
                if (isFirst) {
                    if (dataIndexList.get(fieldname).equals(CustomDesignerConstants.Posttext)) {
                        selectQuery = buildPostTextQuery(dataIndexList, isFirst, fieldname, selectQuery);
                    } else {
                        selectQuery.append(" ").append(fieldname).append(" as `").append(dataIndexList.get(fieldname)).append("` ");
                    }
                    isFirst = false;
                } else {
                    if (dataIndexList.get(fieldname).equals(CustomDesignerConstants.Posttext)) {
                        selectQuery = buildPostTextQuery(dataIndexList, isFirst, fieldname, selectQuery);
                    } else {
                        selectQuery.append(", ").append(fieldname).append(" as `").append(dataIndexList.get(fieldname)).append("` ");
                    }
                }
            }
        }
        return selectQuery;
    }

    public static StringBuilder buildPostTextQuery(HashMap<String, String> dataIndexList, boolean isFirst, String fieldname, StringBuilder selectQuery) {
        if (isFirst) {
            selectQuery.append(" CASE  when(").append(fieldname).append(" is null or ").append(fieldname).append("='' or ").append(fieldname).append("='<br>') then pdftemplateconfig.pdfposttext  else ").append(fieldname).append(" END ").append(" as '").append(dataIndexList.get(fieldname)).append("' ");
        } else {
            selectQuery.append(", CASE  when(").append(fieldname).append(" is null or ").append(fieldname).append("='' or ").append(fieldname).append("='<br>') then pdftemplateconfig.pdfposttext  else ").append(fieldname).append(" END ").append(" as '").append(dataIndexList.get(fieldname)).append("' ");
        }
        return selectQuery;
    }

    public static StringBuilder buildJoinQuery(String mainTable, HashMap<String, String> joinMap) {
        StringBuilder joinQuery = new StringBuilder();
        joinQuery.append(" from " + mainTable + " ");
        Set<String> keys = joinMap.keySet();
        boolean isFirst = true;
        for (String fieldname : keys) {
            joinQuery.append(joinMap.get(fieldname));
        }
        return joinQuery;
    }
    
    
    public static String buildSqlQueryNew(String JSON, String mainTable, int moduleid) {
        String finalQuery = "";
        try {
            ArrayList<String> refTableList = new ArrayList<String>();
            HashMap<String, String> joinMap = new HashMap<String, String>();
            HashMap<String, String> dataIndexList = new HashMap<String, String>();
            HashMap<String, String> dataIndexReftableMap = new HashMap<String, String>();
            JSONArray jArr = new JSONArray(JSON);
            for (int cnt = 0; cnt < jArr.length(); cnt++) { // Iterate over rows
                JSONArray colJArr = jArr.getJSONObject(cnt).getJSONArray("data");
                for (int colcnt = 0; colcnt < colJArr.length(); colcnt++) {
                    JSONArray itemsJArr = colJArr.getJSONObject(colcnt).getJSONArray("data");
                    for (int itemCnt = 0; itemCnt < itemsJArr.length(); itemCnt++) {
                        JSONObject jObj = itemsJArr.getJSONObject(itemCnt);
                        boolean isCreatedAliase = false;
                        if (!StringUtil.isNullOrEmpty(jObj.optString("dbcolumnname", "")) && !jObj.optBoolean("customfield", false)) {
                            String refTable = jObj.getString("reftablename");
                            String refTable1 = jObj.getString("reftablename");
                            if (!refTableList.contains(refTable) && !refTable.equals(mainTable)) {
                                refTableList.add(refTable);
                                if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                    joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + mainTable + "." + jObj.getString("dbcolumnname") + " ");
                                } else { // otherwise fetch join constraint from constant variable
                                    joinMap.put(refTable, CustomDesignerConstants.CustomDesignJoinMap.get(mainTable.concat("_").concat(refTable)));
                                }
                            } else if (refTableList.contains(refTable)) {
                                String colname1 = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                                colname1 = refTable + "." + colname1;
                                if (dataIndexList.containsKey(colname1)) {
                                    refTable1 = refTable + "1";
                                    isCreatedAliase = true;
                                    refTableList.add(refTable);
                                    if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                        joinMap.put(refTable1, " left join " + refTable + " as " + refTable1 + " on " + refTable1 + "." + jObj.getString("reftablefk") + " = " + mainTable + "." + jObj.getString("dbcolumnname") + " ");
                                    }
                                }
                            }
                            dataIndexReftableMap.put("#" + jObj.getString("label") + "#", refTable);
                            if (mainTable.equals(refTable)) {
                                if (dataIndexReftableMap.containsKey(CustomDesignerConstants.Posttext)) {
                                    joinMap.put(refTable1, " left join pdftemplateconfig on pdftemplateconfig.company  = " + mainTable + "." + "company and module =" + moduleid);
                                }
                                dataIndexList.put(refTable.concat(".").concat(jObj.getString("dbcolumnname")), "#" + jObj.getString("label") + "#");
                            } else if (isCreatedAliase) {
                                String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                                if (colname.contains("(") && colname.contains(")") && colname.contains(",")) {//reftabledatacolumn contain (,) then it directly take reftabledatacolumn
                                    dataIndexList.put(colname, "#" + jObj.getString("label") + "#");
                                } else {
                                    dataIndexList.put(refTable1.concat(".").concat(colname), "#" + jObj.getString("label") + "#");
                                }
                                isCreatedAliase = false;
                            } else {
                                String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                                if (colname.contains("(") && colname.contains(")") && colname.contains(",")) {//reftabledatacolumn contain (,) then it directly take reftabledatacolumn
                                    dataIndexList.put(colname, "#" + jObj.getString("label") + "#");
                                } else {
                                dataIndexList.put(refTable.concat(".").concat(colname), "#" + jObj.getString("label") + "#");
                            }
                            }
                        } else if (!StringUtil.isNullOrEmpty(jObj.optString("cellplaceholder", ""))
                                || (jObj.optInt("fieldType", 0) == 11 && !StringUtil.isNullOrEmpty(new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("cellplaceholder", "")))) {
                            JSONArray cellFieldsArr = new JSONArray();
                            if (jObj.optInt("fieldType", 0) == 11) {
                                cellFieldsArr = new JSONArray(new JSONArray(jObj.optString("data", "[]")).getJSONObject(0).optString("cellplaceholder", ""));
                            } else {
                                cellFieldsArr = new JSONArray(jObj.optString("cellplaceholder", ""));
                            }
                            for (int fieldCnt = 0; fieldCnt < cellFieldsArr.length(); fieldCnt++) {
                                jObj = cellFieldsArr.getJSONObject(fieldCnt);
                                if (!StringUtil.isNullOrEmpty(jObj.optString("dbcolumnname", "")) && !jObj.optBoolean("customfield", false)) {
                                    String refTable = jObj.getString("reftablename");
                                    String refTable1 = jObj.getString("reftablename");;
                                    if (!refTableList.contains(refTable) && !refTable.equals(mainTable)) {
                                        refTableList.add(refTable);
                                        if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                            joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + mainTable + "." + jObj.getString("dbcolumnname") + " ");
                                        } else { // otherwise fetch join constraint from constant variable
                                            joinMap.put(refTable, CustomDesignerConstants.CustomDesignJoinMap.get(mainTable.concat("_").concat(refTable)));
                                        }
                                    } else if (refTableList.contains(refTable)) {
                                        String colname1 = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                                        colname1 = refTable + "." + colname1;
                                        if (dataIndexList.containsKey(colname1)) {
                                            if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                                refTable1 = refTable + "1";
                                                joinMap.put(refTable1, " left join " + refTable + " as " + refTable1 + " on " + refTable1 + "." + jObj.getString("reftablefk") + " = " + mainTable + "." + jObj.getString("dbcolumnname") + " ");
                                            }
                                            isCreatedAliase = true;
                                            refTableList.add(refTable);
                                        }
                                    }
                                    dataIndexReftableMap.put("#" + jObj.getString("label") + "#", refTable);
                                    if (mainTable.equals(refTable)) {
                                        if (dataIndexReftableMap.containsKey(CustomDesignerConstants.Posttext)) {
                                            joinMap.put(refTable1, " left join pdftemplateconfig on pdftemplateconfig.company  = " + mainTable + "." + "company and module =" + moduleid);
                                        }
                                        dataIndexList.put(refTable.concat(".").concat(jObj.getString("dbcolumnname")), "#" + jObj.getString("label") + "#");
                                    } else if (isCreatedAliase) {
                                        String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                                        if (colname.contains("(") && colname.contains(")") && colname.contains(",")) {//reftabledatacolumn contain (,) then it directly take reftabledatacolumn
                                            dataIndexList.put(colname, "#" + jObj.getString("label") + "#");
                                        } else {
                                            dataIndexList.put(refTable1.concat(".").concat(colname), "#" + jObj.getString("label") + "#");
                                        }
                                        isCreatedAliase = false;
                                    } else {
                                        String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                                        if (colname.contains("(") && colname.contains(")") && colname.contains(",")) {//reftabledatacolumn contain (,) then it directly take reftabledatacolumn
                                            dataIndexList.put(colname, "#" + jObj.getString("label") + "#");
                                        } else {
                                        dataIndexList.put(refTable.concat(".").concat(colname), "#" + jObj.getString("label") + "#");
                                    }
                                }
                            }
                            }
                        } else if (jObj.optInt("fieldType", 0) == Constants.Custom_design_LineItem_FieldType) {
                            try {
                                JSONArray LineLvlColJarr = null;
                                for (int rowIndex = 1; rowIndex <= Constants.Custom_design_LineItem_DefaultRowsCount; rowIndex++) {
                                    LineLvlColJarr = jObj.getJSONArray("data").getJSONArray(rowIndex);
                                    if (LineLvlColJarr != null) {
                                        for (int colIndex = 0; colIndex < LineLvlColJarr.length(); colIndex++) {
                                            JSONArray LineLvlitemsJArr = LineLvlColJarr.getJSONObject(colIndex).getJSONArray("data");
                                            for (int itemIndex = 0; itemIndex < LineLvlitemsJArr.length(); itemIndex++) {
                                                JSONObject LineLvljObj = LineLvlitemsJArr.getJSONObject(itemIndex);
                                                if (!StringUtil.isNullOrEmpty(LineLvljObj.optString("dbcolumnname", "")) && !LineLvljObj.optBoolean("customfield", false)) {
                                                    String refTable = LineLvljObj.getString("reftablename");
                                                    String refTable1 = LineLvljObj.getString("reftablename");
                                                    if (!refTableList.contains(refTable) && !refTable.equals(mainTable)) {
                                                        refTableList.add(refTable);
                                                        if (!StringUtil.isNullOrEmpty(LineLvljObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                                            joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + LineLvljObj.getString("reftablefk") + " = " + mainTable + "." + LineLvljObj.getString("dbcolumnname") + " ");
                                                        } else { // otherwise fetch join constraint from constant variable
                                                            joinMap.put(refTable, CustomDesignerConstants.CustomDesignJoinMap.get(mainTable.concat("_").concat(refTable)));
                                                        }
                                                    } else if (refTableList.contains(refTable)) {
                                                        String colname1 = LineLvljObj.optString("reftabledatacolumn", "").length() > 0 ? LineLvljObj.optString("reftabledatacolumn", "") : LineLvljObj.getString("dbcolumnname");
                                                        colname1 = refTable + "." + colname1;
                                                        if (dataIndexList.containsKey(colname1)) {
                                                            refTable1 = refTable + "1";
                                                            isCreatedAliase = true;
                                                            refTableList.add(refTable);
                                                            if (!StringUtil.isNullOrEmpty(LineLvljObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                                                joinMap.put(refTable1, " left join " + refTable + " as " + refTable1 + " on " + refTable1 + "." + LineLvljObj.getString("reftablefk") + " = " + mainTable + "." + LineLvljObj.getString("dbcolumnname") + " ");
                                                            }
                                                        }
                                                    }
                                                    dataIndexReftableMap.put("#" + LineLvljObj.getString("label") + "#", refTable);
                                                    if (mainTable.equals(refTable)) {
                                                        if (dataIndexReftableMap.containsKey(CustomDesignerConstants.Posttext)) {
                                                            joinMap.put(refTable1, " left join pdftemplateconfig on pdftemplateconfig.company  = " + mainTable + "." + "company and module =" + moduleid);
                                                        }
                                                        dataIndexList.put(refTable.concat(".").concat(LineLvljObj.getString("dbcolumnname")), "#" + LineLvljObj.getString("label") + "#");
                                                    } else if (isCreatedAliase) {
                                                        String colname = LineLvljObj.optString("reftabledatacolumn", "").length() > 0 ? LineLvljObj.optString("reftabledatacolumn", "") : LineLvljObj.getString("dbcolumnname");
                                                        dataIndexList.put(refTable1.concat(".").concat(colname), "#" + LineLvljObj.getString("label") + "#");
                                                        isCreatedAliase = false;
                                                    } else {
                                                        String colname = LineLvljObj.optString("reftabledatacolumn", "").length() > 0 ? LineLvljObj.optString("reftabledatacolumn", "") : LineLvljObj.getString("dbcolumnname");
                                                        dataIndexList.put(refTable.concat(".").concat(colname), "#" + LineLvljObj.getString("label") + "#");
                                                    }
                                                } else if (!StringUtil.isNullOrEmpty(LineLvljObj.optString("cellplaceholder", ""))) {
                                                    JSONArray cellFieldsArr = new JSONArray();
                                                    cellFieldsArr = new JSONArray(LineLvljObj.optString("cellplaceholder", ""));
                                                    JSONObject jobj = new JSONObject();
                                                    for (int fieldCnt = 0; fieldCnt < cellFieldsArr.length(); fieldCnt++) {
                                                        jobj = cellFieldsArr.getJSONObject(fieldCnt);
                                                        if (!StringUtil.isNullOrEmpty(jobj.optString("dbcolumnname", "")) && !jobj.optBoolean("customfield", false)) {
                                                            String refTable = jobj.getString("reftablename");
                                                            String refTable1 = jobj.getString("reftablename");;
                                                            if (!refTableList.contains(refTable) && !refTable.equals(mainTable)) {
                                                                refTableList.add(refTable);
                                                                if (!StringUtil.isNullOrEmpty(jobj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                                                    joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jobj.getString("reftablefk") + " = " + mainTable + "." + jobj.getString("dbcolumnname") + " ");
                                                                } else { // otherwise fetch join constraint from constant variable
                                                                    joinMap.put(refTable, CustomDesignerConstants.CustomDesignJoinMap.get(mainTable.concat("_").concat(refTable)));
                                                                }
                                                            } else if (refTableList.contains(refTable)) {
                                                                String colname1 = jobj.optString("reftabledatacolumn", "").length() > 0 ? jobj.optString("reftabledatacolumn", "") : jobj.getString("dbcolumnname");
                                                                colname1 = refTable + "." + colname1;
                                                                if (dataIndexList.containsKey(colname1)) {
                                                                    if (!StringUtil.isNullOrEmpty(jobj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                                                        refTable1 = refTable + "1";
                                                                        joinMap.put(refTable1, " left join " + refTable + " as " + refTable1 + " on " + refTable1 + "." + jobj.getString("reftablefk") + " = " + mainTable + "." + jobj.getString("dbcolumnname") + " ");
                                                                    }
                                                                    isCreatedAliase = true;
                                                                    refTableList.add(refTable);
                                                                }
                                                            }
                                                            dataIndexReftableMap.put("#" + jobj.getString("label") + "#", refTable);
                                                            if (mainTable.equals(refTable)) {
                                                                if (dataIndexReftableMap.containsKey(CustomDesignerConstants.Posttext)) {
                                                                    joinMap.put(refTable1, " left join pdftemplateconfig on pdftemplateconfig.company  = " + mainTable + "." + "company and module =" + moduleid);
                                                                }
                                                                dataIndexList.put(refTable.concat(".").concat(jobj.getString("dbcolumnname")), "#" + jobj.getString("label") + "#");
                                                            } else if (isCreatedAliase) {
                                                                String colname = jobj.optString("reftabledatacolumn", "").length() > 0 ? jobj.optString("reftabledatacolumn", "") : jobj.getString("dbcolumnname");
                                                                dataIndexList.put(refTable1.concat(".").concat(colname), "#" + jobj.getString("label") + "#");
                                                                isCreatedAliase = false;
                                                            } else {
                                                                String colname = jobj.optString("reftabledatacolumn", "").length() > 0 ? jobj.optString("reftabledatacolumn", "") : jobj.getString("dbcolumnname");
                                                                dataIndexList.put(refTable.concat(".").concat(colname), "#" + jobj.getString("label") + "#");
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                }
                            } catch (Exception ex) {
                            }
                        }
                    }
                }
            }

            if (!dataIndexList.isEmpty()) {
                StringBuilder selectQuery = buildSelectQuery(dataIndexList);
                StringBuilder joinQuery = buildJoinQuery(mainTable, joinMap);
                finalQuery = "select " + selectQuery.append(joinQuery);
            }
        } catch (JSONException ex) {
            Logger.getLogger(CustomDesignController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalQuery;
    }
   
    /*
     * Common function to set Dimension and Customfield values
     */
    public static JSONObject setAllLinelevelDimensionCustomFieldValues(HashMap<String, Integer> customfielddimensionFieldMap, Map<String, Object> variableMap, JSONObject obj, boolean iscustomfield) {
        JSONObject object = obj;
            try {
                /*
                 * Putting all values for All Dimensions
                 */
                StringBuilder appendimensionString = new StringBuilder();
                
            for (Map.Entry<String, Integer> varEntry : customfielddimensionFieldMap.entrySet()) {//for dropdown,multidropdown,date,numeric field & text field
                    String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                    if (variableMap.containsKey(varEntry.getKey())) {//VariableMap contains without hashvalues
                        String value = "";
                        if (object.has("col" + coldata)) {
                            value = (String) obj.get("col" + coldata);
                        }
                    if (!StringUtil.isNullOrEmpty(value)) {
                        if (iscustomfield) {
                            String dimensionPlaceholder = CustomDesignerConstants.CustomFieldKeyValuePair;
                            dimensionPlaceholder = dimensionPlaceholder.replace(CustomDesignerConstants.CustomFieldLabel, varEntry.getKey().substring(7, varEntry.getKey().length()));
                            dimensionPlaceholder = dimensionPlaceholder.replace(CustomDesignerConstants.CustomFieldValue, value);
                            appendimensionString.append(dimensionPlaceholder);

                        } else {
                            String dimensionPlaceholder = CustomDesignerConstants.DimensionKeyValuePair;
                            dimensionPlaceholder = dimensionPlaceholder.replace(CustomDesignerConstants.DimensionLabel, varEntry.getKey().substring(7, varEntry.getKey().length()));
                            if(value.contains("<br>"))//Remove Description From all Line Level Dimension Field
                            value = value.substring(0, value.indexOf("<br>"));
                            dimensionPlaceholder = dimensionPlaceholder.replace(CustomDesignerConstants.DimensionValue, value);
                            appendimensionString.append(dimensionPlaceholder);
                        }
                    }
                }
            }
                
            if (iscustomfield) {
                object.put(CustomDesignerConstants.AllLinelevelCustomFields, appendimensionString.toString());
            } else {
                object.put(CustomDesignerConstants.AllDimensions, appendimensionString.toString());
            }

            } catch (Exception ex) {
                Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        return object;
    }    
       
     /*  for Make Payment and Receive Payment-setting values for All LIne level Dimensions and CustomFields*/
    public static HashMap<String, Object> setDimensionCustomfieldValuesforMPRP(HashMap<String, Integer> customfielddimensionFieldMap, HashMap<String, Object> extraparams) {
        JSONObject data = new JSONObject();
        JSONObject tempobj = new JSONObject();
        HashMap<String, Object> returnvalues = new HashMap<String, Object>();
        boolean iscustomfield = false;
        StringBuilder appendimensionString = new StringBuilder();

            try {
            if (extraparams.containsKey("data")) {
                data = (JSONObject) extraparams.get("data");
                        }

            if (extraparams.containsKey("tempobj")) {
                tempobj = (JSONObject) extraparams.get("tempobj");
            }

            if (extraparams.containsKey(CustomDesignerConstants.isCustomfield)) {
                iscustomfield = Boolean.parseBoolean((String) extraparams.get(CustomDesignerConstants.isCustomfield));
            }

            if (iscustomfield) { //For line level CustomFields
                   for (Map.Entry<String, Integer> field : customfielddimensionFieldMap.entrySet()) {//checks when field has any value
                    if (data.has(field.getKey())) {
                        String dimensionPlaceholder = CustomDesignerConstants.CustomFieldKeyValuePair;
                        dimensionPlaceholder = dimensionPlaceholder.replace(CustomDesignerConstants.CustomFieldLabel, field.getKey().substring(7, field.getKey().length()));
                        dimensionPlaceholder = dimensionPlaceholder.replace(CustomDesignerConstants.CustomFieldValue, data.getString(field.getKey()));
                        appendimensionString.append(dimensionPlaceholder);
                    }
                }
                tempobj.put(CustomDesignerConstants.AllLinelevelCustomFields, appendimensionString.toString());
            } else {//For line level DimensionFields
                for (Map.Entry<String, Integer> field : customfielddimensionFieldMap.entrySet()) {//checks when field has any value
                    if (data.has(field.getKey())) {
                        String dimensionPlaceholder = CustomDesignerConstants.DimensionKeyValuePair;
                        dimensionPlaceholder = dimensionPlaceholder.replace(CustomDesignerConstants.DimensionLabel, field.getKey().substring(7, field.getKey().length()));
                        dimensionPlaceholder = dimensionPlaceholder.replace(CustomDesignerConstants.DimensionValue, data.getString(field.getKey()));
                        appendimensionString.append(dimensionPlaceholder);
                    }
                }
                tempobj.put(CustomDesignerConstants.AllDimensions, appendimensionString.toString());
            }
            returnvalues.put("tempobj", tempobj);

            } catch (Exception ex) {
                Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        return returnvalues;
    }   
    public static String getErrorHtmlForDD(HttpServletRequest request) {
        String html = "";
        try {
            String path = HttpUtils.getRequestURL(request).toString();
            String servPath = request.getServletPath();
            String uri = path.replace(servPath, "/");
            StringBuilder sb = new StringBuilder();
            String moduleid=request.getParameter("moduleid");
            sb.append("<html>");
            sb.append("<head><link href=\"" + uri + "images/favicon.png\" rel=\"shortcut icon\"/>");
            sb.append("<body><div style='padding: 10px; border: 2px solid; width: 95%; font-size: 25px; height: 150px;'><b>Document Designer Printing Error!</b>");
            sb.append("<p style='font-size: 15px;'>Below may be the reason(s):</p>");
            /*  When printing job order, if transaction don't have product with activated job order item then following message in if condition in printed.*/
            if (!StringUtil.isNullOrEmpty(moduleid) && (moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId)))) {
                sb.append("<ol style='margin:10px'><li style='font-size:15px'>Job Order Item is not activated for any product in transaction.</li>");
            } else {
                sb.append("<ol style='margin:10px'><li style='font-size:15px'>No data to print. Please apply correct filters.</li>");
                sb.append("<li style='font-size:15px; '>Session Expired. Please reload the application.</li></ol><p></p></div>");
            }
            sb.append("</body></html>");
            
            html = sb.toString();
            
        }catch (Exception ex) {
            Logger.getLogger(AccountingManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return html;
    }
    
    public static String indianFormat(double value , int decimal) {
        Double n = Double.parseDouble(Double.toString(value));
        DecimalFormat formatter = new DecimalFormat("##,###");
        if(decimal != -1){
            formatter.setMinimumFractionDigits(decimal);// set the decimal points.
            formatter.setMaximumFractionDigits(decimal);
        }
        boolean negFlag = n < 0 ? true : false;
        n = Math.abs(n);
        
        // *********** block to get value after decimal **********
        NumberFormat nf = NumberFormat.getInstance();
        if(decimal != -1){
            nf.setMinimumFractionDigits(decimal);// set the decimal poiints.
            nf.setMaximumFractionDigits(decimal);
        }
        nf.setGroupingUsed(true);//property to show in comma format
        String str = nf.format(n);
        //********************************************************
        
        long longValue = (long)value;
        
        String returnValue = "";
        if (longValue > 9999) {
            formatter.applyPattern("#,##");
            returnValue = formatter.format((longValue/ 1000)) + ",";
            formatter.applyPattern("#,###");
            long val = longValue - (longValue / 1000) * 1000;
            String valStr = String.valueOf(val);
            if (valStr.length() == 2) {
                returnValue += "0"+valStr;
            } else if (valStr.length() == 1) {
                returnValue += "00"+valStr;
            } else {
                returnValue += valStr;
            }
        } else if (longValue >= 1000 && longValue <= 9999) {
            formatter.applyPattern("#,###");
            returnValue = formatter.format(longValue);
        } else {
            returnValue += str;
        }
        if(str.contains(".")){
            String number = str.substring(str.indexOf(".")).substring(1);
            if(!returnValue.contains(".")){
                returnValue+="."+number;
            }
        }
        
        if (negFlag == true) {
            return "-" + returnValue;
        } else {
            return returnValue;
        }
        
    }
    
    /*
     * Function to Remove Indonesia Extra Fields.
     */
    public static HashMap<String, String> removeIndonesiaExtraFields(HashMap<String, String> map, HashMap<String, String> removeMap) {
        Iterator it = removeMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if (map.containsKey(pair.getKey())) {
                map.remove(pair.getKey());
            }
        }
        return map;
    }

    /*
     * Common function to Add or remove the country specific extra fields.
     */
    public static HashMap<String, String> addRemoveSpecificExtraFields(HashMap<String, String> extraFields,int countryId){
        switch (countryId) {
            case Constants.INDONESIAN_COUNTRY_ID: 
                extraFields.putAll(CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndonesia);
                break;
            default:
                extraFields = removeIndonesiaExtraFields(extraFields, CustomDesignerConstants.CustomDesignCommonExtraFieldsForIndonesia);
        }    
        return  extraFields;
    }
    /**
     * Get extra fields related to template subtype
     * @param moduleid
     * @param templatesybtype
     * @param countryid
     * @return 
     */
    public static HashMap<String, String> getExtraFieldsForSubtype(int moduleid, String templatesybtype, int countryid) {
        HashMap<String, String> map = null;
        switch (templatesybtype) {
            case CustomDesignerConstants.JOB_WORK :
                switch (moduleid) {
                    case Constants.Acc_Stock_Adjustment_ModuleId :
                        map = (HashMap<String, String>) CustomDesignerConstants.CustomDesignExtraFields_For_JobWorkStockIn.clone();
                        break;
                    case Constants.Acc_InterStore_ModuleId :
                        map = (HashMap<String, String>) CustomDesignerConstants.CustomDesignExtraFields_For_JobWorkStockOutTransfer.clone();
                        break;
                }
                break;
        }
        return map;
    }
    
}
