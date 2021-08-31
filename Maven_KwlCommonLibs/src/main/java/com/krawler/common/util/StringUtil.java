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
package com.krawler.common.util;

import com.krawler.common.admin.DefaultHeader;
import com.krawler.common.admin.DefaultHeaderModuleJoinReference;
import com.krawler.common.admin.User;
import com.krawler.common.admin.ServerSpecificOptions;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import static com.krawler.esp.web.resource.Links.loginpageFull;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.CompanySessionClass;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * @author schemers
 */
public class StringUtil {
    //CRM - Specific Functions

    public static String sizeRenderer(String value) {
        Double size = Double.parseDouble(value);
        String text = "";
        Double val;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (size >= 1 && size < 1024) {
            text = size + " Bytes";
        } else if (size > 1024 && size < 1048576) {
            val = (size / 1024);
            text = decimalFormat.format(val);
            text += " KB";
        } else if (size > 1048576) {
            val = (size / 1048576);
            text = decimalFormat.format(val);
            text += " MB";
        }
        return text;
    }

    public static String getMyAdvanceSearchString(String Searchjson, String appendCase) throws JSONException, JSONException {
        StringBuilder myResult = new StringBuilder();
        int any = 0;
        JSONObject jobj = new JSONObject(Searchjson);
        int count = jobj.getJSONArray("root").length();
        for (int i = 0; i < count; i++) {
            JSONObject jobj1 = jobj.getJSONArray("root").getJSONObject(i);
            any++;
            myResult.append(" ");
            if (i == 0) {
                myResult.append(appendCase);
                myResult.append(" (( ");
            } else {
                myResult.append(" ( ");
            }
            myResult.append(jobj1.getString("column") + " like ? or " + jobj1.getString("column") + " like ?");
            if (i + 1 < count) {
                myResult.append(") ");
            } else {
                myResult.append(")) ");
            }
            if (i + 1 < count) {
                myResult.append(" and ");
            }
        }
        if (any == 0) {
            myResult.append(" ");
        }
        return myResult.toString();
    }

    public static Map<String, Object> getMyAdvanceSearchString(Map<String, Object> requestParams, boolean isSqlQuery) throws JSONException, JSONException {
        StringBuilder myResult = new StringBuilder();
//        StringBuilder searchjoin = new StringBuilder();
        int any = 0;
        String Searchjson = requestParams.get(Constants.Searchjson).toString();
        String appendCase = requestParams.get(Constants.appendCase).toString();
        String filterConjuctionCriteria = Constants.and;
        if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
            filterConjuctionCriteria = requestParams.get("filterConjuctionCriteria").toString();
        }
        boolean isOpeningBalance=false; 
        if (requestParams.containsKey("isOpeningBalance") && requestParams.get("isOpeningBalance") != null) {
            isOpeningBalance =Boolean.FALSE.parseBoolean(requestParams.get("isOpeningBalance").toString());
        }
        //        Map<String, Object> SearchParams = new HashMap<String, Object> ();
        int moduleid = Integer.parseInt(requestParams.get(Constants.moduleid).toString());
//        SearchParams.put(Constants.moduleid,moduleid);
//        SearchParams.put(Constants.joinname,Constants.inner);
        JSONObject jobj = new JSONObject(Searchjson);
        String oldcolumnheader = "";
        JSONArray customFieldArray = jobj.getJSONArray(Constants.root);
        customFieldArray = getCustomFieldSearchArray(customFieldArray);
        int count = customFieldArray.length();
        for (int i = 0; i < count; i++) {
            JSONObject jobj1 = customFieldArray.getJSONObject(i);
            any++;
            Boolean iscustomcolumndata = Boolean.parseBoolean(jobj1.getString(Constants.iscustomcolumndata));
            Boolean isfrmpmproduct = Boolean.parseBoolean(jobj1.getString(Constants.isfrmpmproduct));
            Boolean isForProductMasterOnly = Boolean.parseBoolean(jobj1.optString(Constants.isForProductMasterOnly,"false"));
            String columnheader = jobj1.getString("columnheader");
            if (i == 0) {
                oldcolumnheader = columnheader;
            }

            if (moduleid >= 100) {
                if (!oldcolumnheader.equalsIgnoreCase(columnheader)) {
                    String myResultStr = "";
                    myResultStr = myResult.substring(0, myResult.lastIndexOf("or"));
                    myResult = new StringBuilder();
                    myResult.append(myResultStr);
                    myResult.append(") ");
                    myResult.append(filterConjuctionCriteria);
                    oldcolumnheader = columnheader;
                    myResult.append(" ( ");
                }
            }
            myResult.append(Constants.space);
            if (i == 0) {
                myResult.append(appendCase);
                myResult.append(" ((( ");
            } else {
                myResult.append(" ( ");
            }
            String trimedStr = jobj1.getString(Constants.searchText).trim();
            int textlength = trimedStr.split(",").length;
            if (jobj1.getString(Constants.xtype).equalsIgnoreCase(Constants.datefield)) {
                textlength = trimedStr.split(",\"").length;
            }
            for (int searchlength = 0; searchlength < textlength; searchlength++) {
                if (searchlength > 0) {
                    myResult.append(" or ");
                }
                if (jobj1.has(Constants.xtype) && jobj1.has(Constants.xfield) && jobj1.has(Constants.searchText) && jobj1.has(Constants.column)) {
                    String field = Constants.space;
                    String dbName = "";
                    field = jobj1.getString(Constants.xfield);
                    Boolean iscustomcolumn = Boolean.parseBoolean(jobj1.getString(Constants.iscustomcolumn));
                    String xtype = jobj1.getString(Constants.xtype);
                    if (iscustomcolumn) {
                        String moduleref = getmoduledataRefName(moduleid);
                        if (isOpeningBalance) {
                            moduleref = getmoduledataRefNameForOpeningTransaction(moduleid); // for Opening type we use different tables for custom data
                        }
                        if (iscustomcolumndata) {
                            moduleref = getmoduledataRefName(101);
                        }
                        if (isfrmpmproduct) {
                            moduleref = getmoduledataRefName(30);
                        }
                        if (isForProductMasterOnly && isfrmpmproduct) {
                            moduleref = Constants.accProductCustomData;
                        }
                        String fieldtype = jobj1.getString(Constants.fieldtype);
                        if (fieldtype.equals(Constants.seven) || fieldtype.equals(Constants.twelve)) {
                            field = jobj1.getString(Constants.refdbname);
                        }
                        field = field.replaceFirst(Constants.C, Constants.c);
                        if (xtype.equals(Constants.Combo)) {
                            if (isSqlQuery) {
                                dbName = moduleref + Constants.dot + field;
                            } else {
                                if (fieldtype.equals(Constants.eight)) {
                                    field = Constants.Ref + field + Constants.dotID;
                                }
                                if (fieldtype.equals(Constants.four)) {
                                    field = Constants.Ref + field + Constants.dotid;
                                }
                            }
                        }
                        if (isSqlQuery) {
                            dbName = moduleref + Constants.dot + field;
                        } else {
                            dbName = Constants.cdot + moduleref + Constants.dot + field;
                        }
                        String arraySearchstr[] = null;
                        if (jobj1.getString(Constants.xtype).equalsIgnoreCase(Constants.datefield) || StringUtil.equal(jobj1.getString(Constants.xtype), Constants.numberfield)) {
                            /*
                             Split input text for number and date type
                             */
                            trimedStr = replaceSearchquery(trimedStr);
                            arraySearchstr = trimedStr.split(",");
                            if (jobj1.getString(Constants.xtype).equalsIgnoreCase(Constants.datefield)) {
                                arraySearchstr = trimedStr.split(",\"");
                            }
                        }
                        if (StringUtil.equalIgnoreCase(jobj1.getString(Constants.xtype), Constants.Datefield)) {
                            if (arraySearchstr[searchlength].contains("To")) {
                                String[] tempParams = arraySearchstr[searchlength].split("To");
                                if (tempParams.length == 2) {
                                    /*
                                     do search for range and less than data
                                     */
                                    if (StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                        myResult.append(dbName + " <= ?");
                                    } else {
                                        myResult.append(dbName + " >= ? and " + dbName + " <= ? ");
                                    }
                                } else {
                                    /*
                                     do search for greater than data
                                     */
                                    myResult.append(dbName + " >= ? ");
                                }
                            }

                        } else if ((StringUtil.equal(jobj1.getString(Constants.xtype), Constants.numberfield) && jobj1.getString(Constants.searchText).contains("To"))) {
                            String[] tempParams = arraySearchstr[searchlength].split("To");
                            if (tempParams.length == 2) {
                                /*
                                 do search for range and less than data
                                 */
                                if (StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                    myResult.append("ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " <= ? ");
                                } else {
                                    myResult.append("ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " >= ? and " + "ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " <= ? ");
                                }
                            } else {
                                /*
                                 do search for greater than data
                                 */
                                myResult.append("ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " >= ? ");
                            }

                        } else {
                            myResult.append(dbName + Constants.likeq + Constants.or + dbName + Constants.likeq);
                        }

                    } else {
                        if (StringUtil.equal(xtype, Constants.Combo) && StringUtil.equal(jobj1.getString(Constants.searchText), Constants.NineNine)) {
                            myResult.append(jobj1.getString(Constants.column) + Constants.isnull);
                        } else if (StringUtil.equalIgnoreCase(jobj1.getString(Constants.xtype), Constants.Datefield) || (StringUtil.equal(jobj1.getString(Constants.xtype), Constants.numberfield) && jobj1.getString(Constants.searchText).contains("To"))) {
                            myResult.append(jobj1.getString(Constants.column) + " >= ? and " + jobj1.getString(Constants.column) + " <= ? ");
                        } else {
                            myResult.append(jobj1.getString(Constants.column) + Constants.likeq + Constants.or + jobj1.getString(Constants.column) + Constants.likeq);
                        }
                    }
                    /*
                     * Uncomment following code to build joinquery if search is
                     * made on combo
                     * if(StringUtil.equal(jobj1.getString(Constants.xtype),Constants.Combo)
                     * ||
                     * StringUtil.equal(jobj1.getString(Constants.xtype),Constants.select)){
                     * SearchParams.put(xtype,jobj1.get(Constants.xtype));
                     * SearchParams.put(Constants.xfield,jobj1.get(Constants.xfield));
                     * SearchParams.put(Constants.fieldtype,jobj1.get(Constants.fieldtype));
                     * SearchParams.put(Constants.iscustomcolumn,iscustomcolumn);
                     * searchjoin.append(getJoinQuery(SearchParams)); }
                     */

                } else {
                    //   requestParams.put(Constants.searchjoin, "");
                    requestParams.put(Constants.myResult, "");
                    return requestParams;
                }
            }
            if (i + 1 < count) {
                myResult.append(") ");
            } else {
                myResult.append("))) ");
            }
            if (i + 1 < count) {
                if (moduleid >= 100) {
                    if (oldcolumnheader.equalsIgnoreCase(columnheader)) {
                        myResult.append("or");
                    }
                } else {
                    myResult.append(filterConjuctionCriteria);
                }
            }
        }
        if (any == 0) {
            myResult.append(Constants.space);
        }
        //   requestParams.put(Constants.searchjoin, searchjoin);
        requestParams.put(Constants.myResult, myResult);
        return requestParams;
    }
    
    public static boolean isAppDeployed(String appNumber,ServerSpecificOptions serverSpecificOptions ){
        boolean deploymentFlag=false;
        if (serverSpecificOptions != null) {
            try {
                if (serverSpecificOptions.getDeployedAppDetails() != null) {
                    JSONArray jsonArray = new JSONArray(serverSpecificOptions.getDeployedAppDetails());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        if (obj.get("deskeraapplicationid").equals(appNumber)) {
                            deploymentFlag = obj.optBoolean("iscustomdatechanged");
                        }
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return deploymentFlag;
    }

    public static Map<String, Object> getAdvanceSearchString(Map<String, Object> requestParams, boolean isSqlQuery) throws JSONException, JSONException {
        StringBuilder myResult = new StringBuilder();
        int any = 0;
        String Searchjson = requestParams.get(Constants.Searchjson).toString();
        String appendCase = requestParams.get(Constants.appendCase).toString();
        Boolean isReportBuilder = (requestParams.containsKey("isReportBuilder") && (boolean)requestParams.get("isReportBuilder")? true : false);
        String filterConjuctionCriteria = Constants.and;
        if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
            filterConjuctionCriteria = requestParams.get("filterConjuctionCriteria").toString();
        }
        boolean isOpeningBalance = false;
        if (requestParams.containsKey("isOpeningBalance") && requestParams.get("isOpeningBalance") != null) {
            isOpeningBalance = Boolean.FALSE.parseBoolean(requestParams.get("isOpeningBalance").toString());
        }
        boolean isPaymentFromInvoice = false;
        if (requestParams.containsKey("isPaymentFromInvoice") && requestParams.get("isPaymentFromInvoice") != null) {
            isPaymentFromInvoice = Boolean.FALSE.parseBoolean(requestParams.get("isPaymentFromInvoice").toString());
        }

        int moduleid = Integer.parseInt(requestParams.get(Constants.moduleid).toString());

        JSONObject jobj = new JSONObject(Searchjson);
        String oldcolumnheader = "";
        JSONArray customFieldArray = jobj.getJSONArray(Constants.root);
        customFieldArray = getCustomFieldSearchArray(customFieldArray);
        int count = customFieldArray.length();
        for (int i = 0; i < count; i++) {
            JSONObject jobj1 = customFieldArray.getJSONObject(i);
            any++;
            Boolean iscustomcolumndata = Boolean.parseBoolean(jobj1.getString(Constants.iscustomcolumndata));
            Boolean isfrmpmproduct = Boolean.parseBoolean(jobj1.getString(Constants.isfrmpmproduct));
            Boolean isForProductMasterOnly = Boolean.parseBoolean(jobj1.optString(Constants.isForProductMasterOnly, "false"));
            Boolean isForProductMasterSearch = Boolean.parseBoolean(jobj1.optString(Constants.isForProductMasterSearch, "false"));
            String columnheader = jobj1.getString("columnheader");
            if (i == 0) {
                oldcolumnheader = columnheader;
            }

            if (moduleid >= 100) {
                if (!oldcolumnheader.equalsIgnoreCase(columnheader)) {
                    String myResultStr = "";
                    myResultStr = myResult.substring(0, myResult.lastIndexOf("or"));
                    myResult = new StringBuilder();
                    myResult.append(myResultStr);
                    myResult.append(") ");
                    myResult.append(filterConjuctionCriteria);
                    oldcolumnheader = columnheader;
                    myResult.append("( ");
                }
            }
            myResult.append(Constants.space);
            if (i == 0) {
                myResult.append(appendCase);
                myResult.append(" ((( ");
            } else {
                myResult.append(" ( ");
            }
            String trimedStr = jobj1.getString(Constants.searchText).trim();
            int textlength = trimedStr.split(",").length;
            if (jobj1.getString(Constants.xtype).equalsIgnoreCase(Constants.datefield)) {
                textlength = trimedStr.split(",\"").length;
            }
            for (int searchlength = 0; searchlength < textlength; searchlength++) {
                if (searchlength > 0) {
                    myResult.append(" or ");
                }
                if (jobj1.has(Constants.xtype) && jobj1.has(Constants.xfield) && jobj1.has(Constants.searchText) && jobj1.has(Constants.column)) {
                    String field = Constants.space;
                    String dbName = "";
                    field = jobj1.getString(Constants.xfield);
                    Boolean iscustomcolumn = Boolean.parseBoolean(jobj1.getString(Constants.iscustomcolumn));
                    String xtype = jobj1.getString(Constants.xtype);
                    if (iscustomcolumn) {
                        String moduleref = "";
                        if (!StringUtil.isNullOrEmpty(jobj1.getString(Constants.moduleid))) {
                            int moduleID = Integer.parseInt(jobj1.getString(Constants.moduleid));
                            moduleref = getmoduledataRefName(Integer.parseInt(jobj1.getString(Constants.moduleid)));
                            if (isOpeningBalance && moduleID != Constants.Acc_Customer_ModuleId && moduleID != Constants.Acc_Vendor_ModuleId) {
                                moduleref = getmoduledataRefNameForOpeningTransaction(moduleid); // for Opening type we use different tables for custom data
                            }
                            if (iscustomcolumndata && !(moduleID == Constants.Acc_Customer_ModuleId || moduleID == Constants.Acc_Vendor_ModuleId
                                    || moduleID == Constants.Account_Statement_ModuleId)) {
                                moduleref = getmoduledataRefName(101);
                            }
                            if (isfrmpmproduct) {
                                moduleref = getmoduledataRefName(30);
                            }
                            if (isForProductMasterOnly && isfrmpmproduct) {
                                moduleref = Constants.accProductCustomData;
                            }
                            if (isPaymentFromInvoice && (moduleID == Constants.Acc_Vendor_Invoice_ModuleId || moduleID == Constants.Acc_Invoice_ModuleId)) {
                                moduleref = "accinvoicecustomdata";
                            }
                            if(isForProductMasterSearch && moduleID == Constants.Acc_Product_Master_ModuleId){
                                moduleref = Constants.accProductCustomData;
                            }
                        }
                        String fieldtype = jobj1.getString(Constants.fieldtype);
                        if (fieldtype.equals(Constants.seven) || fieldtype.equals(Constants.twelve)) {
                            field = jobj1.getString(Constants.refdbname);
                        }
                        field = field.replaceFirst(Constants.C, Constants.c);
                        if (xtype.equals(Constants.Combo)) {
                            if (isSqlQuery) {
                                dbName = moduleref + Constants.dot + field;
                            } else {
                                if (fieldtype.equals(Constants.eight)) {
                                    field = Constants.Ref + field + Constants.dotID;
                                }
                                if (fieldtype.equals(Constants.four)) {
                                    field = Constants.Ref + field + Constants.dotid;
                                }
                            }
                        }
                        if (isSqlQuery) {
                            dbName = moduleref + Constants.dot + field;
                        } else {
                            dbName = Constants.cdot + moduleref + Constants.dot + field;
                        }
                        String arraySearchstr[] = null;
                        if (jobj1.getString(Constants.xtype).equalsIgnoreCase(Constants.datefield) || StringUtil.equal(jobj1.getString(Constants.xtype), Constants.numberfield)) {
                            /*
                             Split input text for number and date type
                             */
                            trimedStr = replaceSearchquery(trimedStr);
                            arraySearchstr = trimedStr.split(",");
                            if (jobj1.getString(Constants.xtype).equalsIgnoreCase(Constants.datefield)) {
                                String a = ",";
                                arraySearchstr = trimedStr.split(",\"");
                            }
                        }

                        if (StringUtil.equalIgnoreCase(jobj1.getString(Constants.xtype), Constants.Datefield)) {
                            if (arraySearchstr[searchlength].contains("To")) {
                                String[] tempParams = arraySearchstr[searchlength].split("To");
                                if (tempParams.length == 2) {
                                    /*
                                     do search for range and less than data
                                     */
                                    if (StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                        myResult.append(dbName + " <= ?");
                                    } else {
                                        myResult.append(dbName + " >= ? and " + dbName + " <= ? ");
                                    }
                                } else {
                                    /*
                                     do search for greater than data
                                     */
                                    myResult.append(dbName + " >= ? ");
                                }
                            } else if (StringUtil.equal(jobj1.getString(Constants.searchText), Constants.blankSearchKey)) {
                                // create search String for blank value search.  e.g column is null or column = ''
                                myResult.append(dbName + Constants.isnull + Constants.or + dbName + Constants.equalsTo + Constants.whiteSpace);
                            }

                        } else if ((StringUtil.equal(jobj1.getString(Constants.xtype), Constants.numberfield) && jobj1.getString(Constants.searchText).contains("To"))) {
                            String[] tempParams = arraySearchstr[searchlength].split("To");
                            if (tempParams.length == 2) {
                                /*
                                 do search for range and less than data
                                 */
                                if (StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                    myResult.append("ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " <= ? ");
                                } else {
                                    myResult.append("ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " >= ? and " + "ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " <= ? ");
                                }
                            } else {
                                /*
                                 do search for greater than data
                                 */
                                myResult.append("ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " >= ? ");
                            }

                        } else {
                            if (isReportBuilder && isForProductMasterOnly) {
                                myResult.append(" product" + Constants.dot + field + Constants.likeq + Constants.or + " product" + Constants.dot + field + Constants.likeq);
                            } else if (StringUtil.equal(jobj1.getString(Constants.searchText), Constants.blankSearchKey)) {
                                // create search String for blank value search.  e.g column is null or column = ''
                                myResult.append(dbName + Constants.isnull + Constants.or + dbName + Constants.equalsTo + Constants.whiteSpace);
                            } else if (StringUtil.equal(jobj1.getString(Constants.searchText), Constants.blankSearchId)) {
                                // create search String for blank value search.  e.g column is null or column = '' or column = 1234.  1234 is 'None' value
                                myResult.append(dbName + Constants.isnull + Constants.or + dbName + Constants.equalsTo + Constants.whiteSpace + Constants.or + dbName + Constants.equalsTo + "'"+Constants.NONEID+"'");
                            } else {
                                myResult.append(dbName + Constants.likeq + Constants.or + dbName + Constants.likeq);
                            }
                        }

                    } else {
                        if (StringUtil.equal(xtype, Constants.Combo) && StringUtil.equal(jobj1.getString(Constants.searchText), Constants.NineNine)) {
                            myResult.append(jobj1.getString(Constants.column) + Constants.isnull);
                        } else if (StringUtil.equalIgnoreCase(jobj1.getString(Constants.xtype), Constants.Datefield) || (StringUtil.equal(jobj1.getString(Constants.xtype), Constants.numberfield) && jobj1.getString(Constants.searchText).contains("To"))) {
                            myResult.append(jobj1.getString(Constants.column) + " >= ? and " + jobj1.getString(Constants.column) + " <= ? ");
                        /**
                         * For search on address fields append customeraddressdetails table
                         */
                        } else if (jobj1.getString(Constants.modulename).equals("Address_Fields")) {
                            myResult.append("customeraddressdetails."+jobj1.getString(Constants.column) + Constants.likeq + Constants.or + "customeraddressdetails."+jobj1.getString(Constants.column) + Constants.likeq);
                        } else {
                            myResult.append(jobj1.getString(Constants.column) + Constants.likeq + Constants.or + jobj1.getString(Constants.column) + Constants.likeq);
                        }
                    }
                    /*
                     * Uncomment following code to build joinquery if search is
                     * made on combo
                     * if(StringUtil.equal(jobj1.getString(Constants.xtype),Constants.Combo)
                     * ||
                     * StringUtil.equal(jobj1.getString(Constants.xtype),Constants.select)){
                     * SearchParams.put(xtype,jobj1.get(Constants.xtype));
                     * SearchParams.put(Constants.xfield,jobj1.get(Constants.xfield));
                     * SearchParams.put(Constants.fieldtype,jobj1.get(Constants.fieldtype));
                     * SearchParams.put(Constants.iscustomcolumn,iscustomcolumn);
                     * searchjoin.append(getJoinQuery(SearchParams)); }
                     */

                } else {
                    //   requestParams.put(Constants.searchjoin, "");
                    requestParams.put(Constants.myResult, "");
                    return requestParams;
                }
            }
            if (i + 1 < count) {
                myResult.append(") ");
            } else {
                myResult.append("))) ");
            }
            if (i + 1 < count) {
                if (moduleid >= 100) {
                    if (oldcolumnheader.equalsIgnoreCase(columnheader)) {
                        myResult.append("or");
                    }
                } else {
                    myResult.append(filterConjuctionCriteria);
                }
            }
        }
        if (any == 0) {
            myResult.append(Constants.space);
        }
        //   requestParams.put(Constants.searchjoin, searchjoin);
        requestParams.put(Constants.myResult, myResult);
        return requestParams;
    }
    /**
     * @info get module-wise filter search String
     * @param requestParams
     * @param isSqlQuery
     * @return requestParams
     * @throws JSONException
     */
    public static Map<String, Object> getProductMasterSearchString(Map<String, Object> requestParams, boolean isSqlQuery) throws JSONException {
        StringBuilder myResult = new StringBuilder();
        int any = 0;
        String Searchjson = requestParams.get(Constants.Searchjson).toString();
        String appendCase = requestParams.get(Constants.appendCase).toString();
        String filterConjuctionCriteria = Constants.and;
        if (requestParams.containsKey("filterConjuctionCriteria") && requestParams.get("filterConjuctionCriteria") != null) {
            filterConjuctionCriteria = requestParams.get("filterConjuctionCriteria").toString();
        }
        boolean isOpeningBalance = false;
        if (requestParams.containsKey("isOpeningBalance") && requestParams.get("isOpeningBalance") != null) {
            isOpeningBalance = Boolean.FALSE.parseBoolean(requestParams.get("isOpeningBalance").toString());
        }
        boolean isPaymentFromInvoice = false;
        if (requestParams.containsKey("isPaymentFromInvoice") && requestParams.get("isPaymentFromInvoice") != null) {
            isPaymentFromInvoice = Boolean.FALSE.parseBoolean(requestParams.get("isPaymentFromInvoice").toString());
        }

        JSONObject jobj = new JSONObject(Searchjson);
        String oldcolumnheader = "";
        JSONArray customFieldArray = jobj.getJSONArray(Constants.root);
        customFieldArray = getCustomFieldSearchArray(customFieldArray);
        int count = customFieldArray.length();
        for (int i = 0; i < count; i++) {
            JSONObject jobj1 = customFieldArray.getJSONObject(i);
            any++;
            Boolean isfrmpmproduct = Boolean.parseBoolean(jobj1.getString(Constants.isfrmpmproduct));
            Boolean isForProductMasterOnly = Boolean.parseBoolean(jobj1.optString(Constants.isForProductMasterOnly, "false"));
            Boolean isForProductMasterSearch = Boolean.parseBoolean(jobj1.optString(Constants.isForProductMasterSearch, "false"));
            Boolean isLineLevel = Boolean.parseBoolean(jobj1.optString("isLineLevel", "false"));
            String columnheader = jobj1.getString("columnheader");

            int moduleID = Integer.parseInt(jobj1.getString(Constants.moduleid));

            if (i == 0) {
                oldcolumnheader = columnheader;
            }

            if (moduleID >= 100) {
                if (!oldcolumnheader.equalsIgnoreCase(columnheader)) {
                    String myResultStr = "";
                    myResultStr = myResult.substring(0, myResult.lastIndexOf("or"));
                    myResult = new StringBuilder();
                    myResult.append(myResultStr);
                    myResult.append(") ");
                    myResult.append(filterConjuctionCriteria);
                    oldcolumnheader = columnheader;
                    myResult.append("( ");
                }
            }
            myResult.append(Constants.space);
            if (i == 0) {
                myResult.append(appendCase);
                myResult.append(" ((( ");
            } else {
                myResult.append(" ( ");
            }
            String trimedStr = jobj1.getString(Constants.searchText).trim();
            int textlength = trimedStr.split(",").length;
            if (jobj1.getString(Constants.xtype).equalsIgnoreCase(Constants.datefield)) {
                textlength = trimedStr.split(",\"").length;
            }
            for (int searchlength = 0; searchlength < textlength; searchlength++) {
                if (searchlength > 0) {
                    myResult.append(" or ");
                }
                if (jobj1.has(Constants.xtype) && jobj1.has(Constants.xfield) && jobj1.has(Constants.searchText) && jobj1.has(Constants.column)) {
                    String field = Constants.space;
                    String dbName = "";
                    field = jobj1.getString(Constants.xfield);
                    Boolean iscustomcolumn = Boolean.parseBoolean(jobj1.getString(Constants.iscustomcolumn));
                    String xtype = jobj1.getString(Constants.xtype);
                    if (iscustomcolumn) {
                        String moduleref = "";
                        if (!StringUtil.isNullOrEmpty(jobj1.getString(Constants.moduleid))) {
                            moduleref = getmoduledataRefName(Integer.parseInt(jobj1.getString(Constants.moduleid)), isLineLevel);
                            if (isOpeningBalance && moduleID != Constants.Acc_Customer_ModuleId && moduleID != Constants.Acc_Vendor_ModuleId) {
                                moduleref = getmoduledataRefNameForOpeningTransaction(moduleID); // for Opening type we use different tables for custom data
                            }
                            if (isfrmpmproduct) {
                                moduleref = getmoduledataRefName(30);
                            }
                            if (isForProductMasterOnly && isfrmpmproduct) {
                                moduleref = Constants.accProductCustomData;
                            }
                            if (isPaymentFromInvoice && (moduleID == Constants.Acc_Vendor_Invoice_ModuleId || moduleID == Constants.Acc_Invoice_ModuleId)) {
                                moduleref = "accinvoicecustomdata";
                            }
                            if (isForProductMasterSearch) {
                                moduleref = getmoduledataRefName(moduleID);
                            }
                        }
                        String fieldtype = jobj1.getString(Constants.fieldtype);
                        if (fieldtype.equals(Constants.seven) || fieldtype.equals(Constants.twelve)) {
                            field = jobj1.getString(Constants.refdbname);
                        }
                        field = field.replaceFirst(Constants.C, Constants.c);
                        if (xtype.equals(Constants.Combo)) {
                            if (isSqlQuery) {
                                dbName = moduleref + Constants.dot + field;
                            } else {
                                if (fieldtype.equals(Constants.eight)) {
                                    field = Constants.Ref + field + Constants.dotID;
                                }
                                if (fieldtype.equals(Constants.four)) {
                                    field = Constants.Ref + field + Constants.dotid;
                                }
                            }
                        }
                        if (isSqlQuery) {
                            dbName = moduleref + Constants.dot + field;
                        } else {
                            dbName = Constants.cdot + moduleref + Constants.dot + field;
                        }
                        String arraySearchstr[] = null;
                        if (jobj1.getString(Constants.xtype).equalsIgnoreCase(Constants.datefield) || StringUtil.equal(jobj1.getString(Constants.xtype), Constants.numberfield)) {
                            /*
                             Split input text for number and date type
                             */
                            trimedStr = replaceSearchquery(trimedStr);
                            arraySearchstr = trimedStr.split(",");
                            if (jobj1.getString(Constants.xtype).equalsIgnoreCase(Constants.datefield)) {
                                String a = ",";
                                arraySearchstr = trimedStr.split(",\"");
                            }
                        }

                        if (StringUtil.equalIgnoreCase(jobj1.getString(Constants.xtype), Constants.Datefield)) {
                            if (arraySearchstr[searchlength].contains("To")) {
                                String[] tempParams = arraySearchstr[searchlength].split("To");
                                if (tempParams.length == 2) {
                                    /*
                                     do search for range and less than data
                                     */
                                    if (StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                        myResult.append(dbName + " <= ?");
                                    } else {
                                        myResult.append(dbName + " >= ? and " + dbName + " <= ? ");
                                    }
                                } else {
                                    /*
                                     do search for greater than data
                                     */
                                    myResult.append(dbName + " >= ? ");
                                }
                            }

                        } else if ((StringUtil.equal(jobj1.getString(Constants.xtype), Constants.numberfield) && jobj1.getString(Constants.searchText).contains("To"))) {
                            String[] tempParams = arraySearchstr[searchlength].split("To");
                            if (tempParams.length == 2) {
                                /*
                                 do search for range and less than data
                                 */
                                if (StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                    myResult.append("ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " <= ? ");
                                } else {
                                    myResult.append("ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " >= ? and " + "ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " <= ? ");
                                }
                            } else {
                                /*
                                 do search for greater than data
                                 */
                                myResult.append("ifnull(CONVERT(" + dbName + ",DECIMAL(64,4)),0)" + " >= ? ");
                            }

                        } else {
                            myResult.append(dbName + Constants.likeq + Constants.or + dbName + Constants.likeq);
                        }

                    } else {
                        if (StringUtil.equal(xtype, Constants.Combo) && StringUtil.equal(jobj1.getString(Constants.searchText), Constants.NineNine)) {
                            myResult.append(jobj1.getString(Constants.column) + Constants.isnull);
                        } else if (StringUtil.equalIgnoreCase(jobj1.getString(Constants.xtype), Constants.Datefield) || (StringUtil.equal(jobj1.getString(Constants.xtype), Constants.numberfield) && jobj1.getString(Constants.searchText).contains("To"))) {
                            myResult.append(jobj1.getString(Constants.column) + " >= ? and " + jobj1.getString(Constants.column) + " <= ? ");
                            /**
                             * For search on address fields append
                             * customeraddressdetails table
                             */
                        } else if (jobj1.getString(Constants.modulename).equals("Address_Fields")) {
                            myResult.append("customeraddressdetails." + jobj1.getString(Constants.column) + Constants.likeq + Constants.or + "customeraddressdetails." + jobj1.getString(Constants.column) + Constants.likeq);
                        } else {
                            myResult.append(jobj1.getString(Constants.column) + Constants.likeq + Constants.or + jobj1.getString(Constants.column) + Constants.likeq);
                        }
                    }

                } else {
                    requestParams.put(Constants.myResult, "");
                    return requestParams;
                }
            }
            if (i + 1 < count) {
                myResult.append(") ");
            } else {
                myResult.append("))) ");
            }
            if (i + 1 < count) {
                if (moduleID >= 100) {
                    if (oldcolumnheader.equalsIgnoreCase(columnheader)) {
                        myResult.append("or");
                    }
                } else {
                    myResult.append(filterConjuctionCriteria);
                }
            }
        }
        if (any == 0) {
            myResult.append(Constants.space);
        }
        requestParams.put(Constants.myResult, myResult);
        return requestParams;
    }
    /**
     *
     * @info Append Search string based on conjuction criterion
     * @return requestParams
     * @throws JSONException
     * @throws ParseException,JSONException
     */
    public static Map<String, Object> getAppendSearchString(JSONArray masterJson, StringBuilder joinString, Map<String, Object> requestParams, ArrayList searchParams) throws JSONException, ParseException {
        HashMap<String, Object> advRequestParams = new HashMap();

        JSONObject putSearchJson = new JSONObject();
        String tableJoin = "";
        String productFilterString = "";
        putSearchJson.put("root", masterJson);
        advRequestParams.clear();
        advRequestParams.put(Constants.Searchjson, putSearchJson);
        advRequestParams.put(Constants.appendCase, "AND");
        advRequestParams.put("filterConjuctionCriteria", com.krawler.common.util.Constants.or); // Always use OR
        productFilterString = String.valueOf(StringUtil.getProductMasterSearchString(advRequestParams, true).get(Constants.myResult));

        if (productFilterString.contains(Constants.accProductCustomData) || productFilterString.contains(Constants.Acc_common_pojo_refForProductJEDETAILS)) {
            productFilterString = productFilterString.replaceAll(Constants.Acc_common_pojo_refForProductJEDETAILS, Constants.accProductCustomData);
            tableJoin = " left join accproductcustomdata on accproductcustomdata.productId=t1.pid  ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }

        if (productFilterString.contains(Constants.DeliveryOrder_CustomData_Query)) {
            tableJoin = " left join deliveryordercustomdata on deliveryordercustomdata.deliveryOrderId=t1.customreference ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }
        if (productFilterString.contains(Constants.DeliveryOrder_DetailCustomData_Query)) {
            tableJoin = " left join dodetailscustomdata on t1.linecustomreference=dodetailscustomdata.dodetailsid ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }
        if (productFilterString.contains(Constants.GoodsReceiptOrder_CustomData_Query)) {
            tableJoin = " left join grordercustomdata on grordercustomdata.goodsreceiptorderid=t1.customreference ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }
        if (productFilterString.contains(Constants.GoodsReceiptOrder_DetailCustomData_Query)) {
            tableJoin = " left join grodetailscustomdata on t1.linecustomreference=grodetailscustomdata.grodetailsid ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }
        if (productFilterString.contains(Constants.SalesReturn_CustomData_Query)) {
            tableJoin = " left join salesreturncustomdata on salesreturncustomdata.salesreturnid=t1.customreference ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }
        if (productFilterString.contains(Constants.SalesReturn_DetailCustomData_Query)) {
            tableJoin = " left join srdetailscustomdata on t1.linecustomreference=srdetailscustomdata.srdetailsid ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }
        if (productFilterString.contains(Constants.PurchaseReturn_CustomData_Query)) {
            tableJoin = " left join purchasereturncustomdata on purchasereturncustomdata.purchasereturnid=t1.customreference ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }
        if (productFilterString.contains(Constants.PurchaseReturn_DetailCustomData_Query)) {
            tableJoin = " left join prdetailscustomdata on t1.linecustomreference=prdetailscustomdata.prdetailsid ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }
        if (productFilterString.contains(Constants.Stock_Adjustment)) {
            tableJoin = " left join in_stockadjustment_customdata on t1.customreference=in_stockadjustment_customdata.stockadjustmentid ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }
        if (productFilterString.contains(Constants.Inter_Store_Location)) {
            tableJoin = " left join in_interstoretransfer_customdata on t1.customreference=in_interstoretransfer_customdata.istid ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }
        if (productFilterString.contains(Constants.Acc_StockRequest_Query)) {
            productFilterString = productFilterString.replaceAll(Constants.Acc_StockRequest_Query, Constants.Acc_StockIssueRequest);
            tableJoin = " left join stockcustomdata on t1.customreference=stockcustomdata.stockId ";
            if (!joinString.toString().contains(tableJoin)) {
                joinString.append(tableJoin);
            }
        }

        requestParams.put("productFilterString", productFilterString);
        insertParamAdvanceSearchString1(searchParams, putSearchJson.toString());

        return requestParams;
    }
    /**
     * 
     * @info Common method written to apply product master search on all modules .  
     * @return requestParams
     * @throws JSONException
     * @throws ParseException 
     */
    public static Map<String, Object> getProductMasterAdvanceSearchString(JSONArray masterJson, ArrayList params, Map<String, Object> requestParams) throws JSONException, ParseException {
        HashMap<String, Object> advRequestParams = new HashMap();

        JSONObject putSearchJson = new JSONObject();
        String productFilterString = "";
        String productMasterJoin = " ";
        String moduleSpecificJoin = " ";
        putSearchJson.put("root", masterJson);
        advRequestParams.clear();
        advRequestParams.put(Constants.Searchjson, putSearchJson);
        advRequestParams.put(Constants.appendCase, "AND");
        advRequestParams.put(Constants.moduleid, Constants.Acc_Product_Master_ModuleId);
        advRequestParams.put("filterConjuctionCriteria", com.krawler.common.util.Constants.or); // Always use OR
        productFilterString = String.valueOf(StringUtil.getAdvanceSearchString(advRequestParams, true).get(Constants.myResult));

        if (productFilterString.contains(Constants.accProductCustomData)) {
            productMasterJoin = " left join accproductcustomdata on accproductcustomdata.productId=p.id  ";
        }

        if (productFilterString.contains("deliveryordercustomdata")) {
            moduleSpecificJoin = " left join deliveryordercustomdata on deliveryordercustomdata.deliveryOrderId=do.accdeliveryordercustomdataref ";
        }
        if (productFilterString.contains("grordercustomdata")) {
            moduleSpecificJoin = " left join grordercustomdata on grordercustomdata.goodsreceiptorderid=gro.accgrordercustomdataref ";
        }
        if (productFilterString.contains("salesreturncustomdata")) {
            moduleSpecificJoin = " left join salesreturncustomdata on salesreturncustomdata.salesreturnid=sr.accsalesreturncustomdataref ";
        }
        if (productFilterString.contains("purchasereturncustomdata")) {
            moduleSpecificJoin = " left join purchasereturncustomdata on purchasereturncustomdata.purchasereturnid=pr.accpurchasereturncustomdataref ";
        }
        if (productFilterString.contains("in_stockadjustment_customdata")) {
            moduleSpecificJoin = " left join in_stockadjustment_customdata on sa.id=in_stockadjustment_customdata.stockadjustmentid ";
        }
        if (productFilterString.contains("in_interstoretransfer_customdata")) {
            moduleSpecificJoin = " left join in_interstoretransfer_customdata on sa.id=in_interstoretransfer_customdata.istid ";
        }
        
        requestParams.put("productFilterString", productFilterString);
        requestParams.put("productMasterJoin", productMasterJoin);
        requestParams.put("moduleSpecificJoin", moduleSpecificJoin);

        insertParamAdvanceSearchString1(params, putSearchJson.toString());

        return requestParams;
    }

    public static JSONArray getCustomFieldSearchArray(JSONArray array) throws JSONException {
        JSONArray customFieldArray = new JSONArray();
        for (int i = 0; i < array.length(); i++) {
            JSONObject jobj1 = array.getJSONObject(i);
            if (jobj1.optBoolean("isdefaultfield") == true) {
                continue;
            }
            customFieldArray.put(jobj1);
        }
        return customFieldArray;
    }
    
    public static String getStringRangeFilterForAdvanceSearch(JSONObject jObj, String filterConjunction) {
        String filterstring = "";
        if (jObj.optBoolean("isRangeSearchField", false)) {
            filterstring = " " + jObj.optString("search") + " ";
        }
        return filterstring;
    }
    /**
     * @info getmoduledataRefName method is overridden to get line or global table name
     * @param moduleid
     * @param isLineLevel if field is line or global
     * @return
     */
    public static String getmoduledataRefName(int moduleid, boolean isLineLevel) {
        String module = "";
        switch (moduleid) {
            case 1:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 38:
            case 39:
            case 2:
            case 52:
            case 93:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 3:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 4:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 5:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 6:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 7:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 8:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 9:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 10:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 11:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 12:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 13:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 14:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 15:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 16:
                module = isLineLevel ? Constants.Acc_common_pojo_refForJEDETAILS : Constants.Acc_common_pojo_ref_Query;
                break;
            case 18:
                module = isLineLevel ? Constants.PurchaseOrder_DetailCustomData_Query : Constants.PurchaseOrder_CustomData_Query;
                break;
            case 20:
            case 50:
                module = isLineLevel ? Constants.PurchaseOrder_DetailCustomData_Query : Constants.SalesOrder_CustomData_Query;
                break;
            case 22:
            case 65:
                module = isLineLevel ? Constants.PurchaseOrder_DetailCustomData_Query : Constants.Quotation_CustomData_Query;
                break;
            case 23:
                module = isLineLevel ? Constants.PurchaseOrder_DetailCustomData_Query : Constants.VenderQuotation_CustomData_Query;
                break;
            case 24:
                module = Constants.Acc_common_pojo_ref;
                break;
            case 25:
                module = Constants.Acc_common_pojo_Customer_Query;
                break;
            case 26:
                module = Constants.Acc_common_pojo_Vendor_Query;
                break;
            case 41:
            case 27:
            case 51:
            case 67:
                module = isLineLevel ? Constants.DeliveryOrder_DetailCustomData_Query : Constants.DeliveryOrder_CustomData_Query;
                break;
            case 40:
            case 28:
                module = isLineLevel ? Constants.GoodsReceiptOrder_DetailCustomData_Query : Constants.GoodsReceiptOrder_CustomData_Query;
                break;
            case 29:
            case 98:
            case 53:
            case 68:
                module = isLineLevel ? Constants.SalesReturn_DetailCustomData_Query : Constants.SalesReturn_CustomData_Query;
                break;
            case 31:
            case 96:
                module = isLineLevel ? Constants.PurchaseReturn_DetailCustomData_Query : Constants.PurchaseReturn_CustomData_Query;
                break;
            case 32:
                module = isLineLevel ? Constants.PurchaseRequisition_DetailCustomData_Query : Constants.PurchaseRequisition_CustomData_Query;
                break;
            case 100:
                module = Constants.Acc_common_pojo_ref;
                break;
            case 101:
                module = Constants.Acc_common_pojo_refForJEDETAILS;
                break;
            case 102:
                module = Constants.Acc_common_pojo_VenCust_Query;
                break;
            case 42:
            case 30:
                module = Constants.Acc_common_pojo_refForProductJEDETAILS;
                break;
            case 33:
                module = isLineLevel ? Constants.RFQ_DetailCustomData_Query : Constants.RFQ_CustomData_Query;
                break;
            case 34:
                module = Constants.Acc_common_pojo_VenCust_Query;
                break;
            case 35:
                module = Constants.Contract_CustomData_Query;
                break;
            case 36:
            case Constants.VENDOR_JOB_WORKORDER_MODULEID:
                module = Constants.SalesOrder_CustomData_Query;
                break;
            case 92:
            case Constants.Acc_Stock_Request_ModuleId:
                module = Constants.Acc_StockRequest_Query;
                break;
            case 121:
                module = Constants.Acc_AssetDetail_Query;
                break;
            case Constants.Inventory_Stock_Adjustment_ModuleId:
                module = Constants.Stock_Adjustment;
                break;
            case Constants.Acc_InterStore_ModuleId:
            case Constants.Acc_InterLocation_ModuleId:
                module = Constants.Inter_Store_Location;
                break;
            case Constants.Labour_Master:
                module = Constants.Labour_MasterClass;
                break;
            case Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId:
                module = Constants.PurchaseRequisition_CustomData_Query;
                break;
            case Constants.MRP_WORK_CENTRE_MODULEID:
                module = Constants.WorkCentreCustomData_MasterClass;
                break;
            case Constants.MRP_Machine_Management_ModuleId:
                module = Constants.MachineCustomData_MasterClass;
                break;
            case Constants.MRP_WORK_ORDER_MODULEID:
                module = Constants.WorkOrderCustomData_MasterClass;
                break;
            case Constants.MRP_Contract:
                module = Constants.MRPContractCustomData_MasterClass;
                break;
            case Constants.MRP_RouteCode:
                module = Constants.MRPRoutingTemplate_MasterClass;
                break;
            case Constants.MRP_JOB_WORK_MODULEID:
                module = Constants.MRPJobWork_MasterClass;
                break;
        }
        return module;
    }
    public static String getmoduledataRefName(int moduleid) {
        String module = "";
        switch (moduleid) {
            case 1:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 38:
            case 39:
            case 2:
            case 52:
            case 93:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 3:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 4:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 5:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 6:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 7:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 8:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 9:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 10:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 11:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 12:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 13:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 14:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 15:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 16:
                module = Constants.Acc_common_pojo_ref_Query;
                break;
            case 18:
                module = Constants.PurchaseOrder_CustomData_Query;
                break;
            case 20:
            case 50:
                module = Constants.SalesOrder_CustomData_Query;
                break;
            case 22:
            case 65:
                module = Constants.Quotation_CustomData_Query;
                break;
            case 23:
                module = Constants.VenderQuotation_CustomData_Query;
                break;
            case 24:
                module = Constants.Acc_common_pojo_ref;
                break;
            case 25:
                module = Constants.Acc_common_pojo_Customer_Query;
                break;
            case 26:
                module = Constants.Acc_common_pojo_Vendor_Query;
                break;
            case 41:
            case 27:
            case 51:
            case 67:
                module = Constants.DeliveryOrder_CustomData_Query;
                break;
            case 40:
            case 28:
                module = Constants.GoodsReceiptOrder_CustomData_Query;
                break;
            case 29:
            case 98:
            case 53:
            case 68:
                module = Constants.SalesReturn_CustomData_Query;
                break;
            case 31:
            case 96:
                module = Constants.PurchaseReturn_CustomData_Query;
                break;
            case 32:
                module = Constants.PurchaseRequisition_CustomData_Query;
                break;
            case 100:
                module = Constants.Acc_common_pojo_ref;
                break;
            case 101:
                module = Constants.Acc_common_pojo_refForJEDETAILS;
                break;
            case 102:
                module = Constants.Acc_common_pojo_VenCust_Query;
                break;
            case 42:
            case 30:
                module = Constants.Acc_common_pojo_refForProductJEDETAILS;
                break;
            case 33:
                module = Constants.RFQ_CustomData_Query;
                break;
            case 34:
                module = Constants.Acc_common_pojo_VenCust_Query;
                break;
            case 35:
                module = Constants.Contract_CustomData_Query;
                break;
            case 36:
            case Constants.VENDOR_JOB_WORKORDER_MODULEID:
                module = Constants.SalesOrder_CustomData_Query;
                break;
            case 92:
                module = Constants.Acc_StockRequest_Query;
                break;
            case 121:
                module = Constants.Acc_AssetDetail_Query;
                break;
            case Constants.Inventory_Stock_Adjustment_ModuleId:
                module = Constants.Stock_Adjustment;
                break;
            case Constants.Acc_InterStore_ModuleId:
            case Constants.Acc_InterLocation_ModuleId:
                module = Constants.Inter_Store_Location;
                break;
            case Constants.Labour_Master:
                module = Constants.Labour_MasterClass;
                break;
            case Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId:
                module = Constants.PurchaseRequisition_CustomData_Query;
                break;
            case Constants.MRP_WORK_CENTRE_MODULEID:
                module = Constants.WorkCentreCustomData_MasterClass;
                break;
            case Constants.MRP_Machine_Management_ModuleId:
                module = Constants.MachineCustomData_MasterClass;
                break;
            case Constants.MRP_WORK_ORDER_MODULEID:
                module = Constants.WorkOrderCustomData_MasterClass;
                break;
            case Constants.MRP_Contract:
                module = Constants.MRPContractCustomData_MasterClass;
                break;
            case Constants.MRP_RouteCode:
                module = Constants.MRPRoutingTemplate_MasterClass;
                break;
            case Constants.MRP_JOB_WORK_MODULEID:
                module = Constants.MRPJobWork_MasterClass;
                break;
        }
        return module;
    }

    public static String getmoduledataRefNameForOpeningTransaction(int moduleid) {
        String module = "";
        switch (moduleid) {
            case 2:
                module = Constants.OpeningBalanceInvoiceCustomData_CustomData_Query;
                break;
            case 6:
                module = Constants.OpeningBalanceVendorInvoiceCustomData_CustomData_Query;
                break;
            case 10:
                module = Constants.OpeningBalanceDebitNoteCustomData_CustomData_Query;
                break;
            case 12:
                module = Constants.OpeningBalanceCreditNoteCustomData_CustomData_Query;
                break;
            case 14:
                module = Constants.OpeningBalanceMakePaymentCustomData_CustomData_Query;
                break;
            case 16:
                module = Constants.OpeningBalanceReceiptCustomData_CustomData_Query;
                break;
            case 34:
                module = Constants.Acc_common_pojo_VenCust_Query;
                break;
                
        }
        return module;
    }
    public static void insertParamAdvanceSearchString(ArrayList al, String Searchjson)
            throws JSONException, JSONException {

        JSONObject jobj = new JSONObject(Searchjson);
        int count = jobj.getJSONArray("root").length();

        for (int i = 0; i < count; i++) {
            JSONObject jobj1 = jobj.getJSONArray("root").getJSONObject(i);
            String trimedStr = jobj1.getString("searchText").trim();
            al.add(trimedStr + "%");
            al.add("% " + trimedStr + "%");
        }

    }

    public static String sNull(String s) {
        String ret = null;
        if (!(StringUtil.isNullOrEmpty(s))) {
            ret = s;
        } else {
            ret = "";
        }
        return ret;
    }

    public static boolean bNull(String s) {
        if (StringUtil.isNullOrEmpty(s)) {
            return false;
        } else {
            return true;
        }
        // return true;
    }

    public static String filterQuery(ArrayList filter_names, String appendCase) {
        StringBuilder filterQuery = new StringBuilder();
//        String filterQuery = "";
        String oper = "";
        String op = "";
        for (int i = 0; i < filter_names.size(); i++) {
            oper = "";
            op = "";
            if (filter_names.get(i).toString().length() >= 5) {
                op = filter_names.get(i).toString().substring(0, 5);
            }
            if (op.equals("ISNOT")) {
                oper = " is not ";
                String opstr = filter_names.get(i).toString();
                filter_names.set(i, opstr.substring(5, opstr.length()));
            } else if (op.equals("NOTIN")) {
                oper = " not in(" + i + ")";
                String opstr = filter_names.get(i).toString();
                filter_names.set(i, opstr.substring(5, opstr.length()));
            } else if (op.equals("ISNUL")) {
                oper = " is null";
                String opstr = filter_names.get(i).toString();
                filter_names.set(i, opstr.substring(6, opstr.length()));
            } else if (op.equals("IS!NU")) {
                oper = " is not null";
                String opstr = filter_names.get(i).toString();
                filter_names.set(i, opstr.substring(7, opstr.length()));
            } else {
                if (filter_names.get(i).toString().length() >= 4) {
                    op = filter_names.get(i).toString().substring(0, 4);
                }
                if (op.equals("LIKE")) {
                    oper = " like ";
                    String opstr = filter_names.get(i).toString();
                    filter_names.set(i, opstr.substring(4, opstr.length()));
                } else {
                    op = filter_names.get(i).toString().substring(0, 2);
                    if (op.equals("<=")) {
                        oper = " <= ";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    } else if (op.equals(">=")) {
                        oper = " >= ";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    } else if (op.equals("IS")) {
                        oper = " is ";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    } else if (op.equals("IN")) {
                        oper = " in(" + i + ")";
                        String opstr = filter_names.get(i).toString();
                        filter_names.set(i, opstr.substring(2, opstr.length()));
                    } else {

                        op = filter_names.get(i).toString().substring(0, 1);
                        if (op.equals("!")) {
                            oper = " != ";
                            String opstr = filter_names.get(i).toString();
                            filter_names.set(i, opstr.substring(1, opstr.length()));
                        } else if (op.equals("<")) {
                            oper = " < ";
                            String opstr = filter_names.get(i).toString();
                            filter_names.set(i, opstr.substring(1, opstr.length()));
                        } else if (op.equals(">")) {
                            oper = " > ";
                            String opstr = filter_names.get(i).toString();
                            filter_names.set(i, opstr.substring(1, opstr.length()));
                        } else {
                            oper = " = ";
                        }
                    }
                }
            }

            if (i == 0) {
//                filterQuery += " where "+filter_names.get(i)+" = ? ";
                if (op.equals("ISNUL") || op.equals("IS!NU")) {
                    filterQuery.append(" " + appendCase + " " + filter_names.get(i) + oper);
                } else if (!op.equals("IN") && !op.equals("NOTIN")) {
                    filterQuery.append(" " + appendCase + " " + filter_names.get(i) + oper + " ? ");
                } else {
                    filterQuery.append(" " + appendCase + " " + filter_names.get(i) + oper);
                }
            } else {
//                filterQuery += " and "+filter_names.get(i)+" = ? ";
                if (op.equals("ISNUL") || op.equals("IS!NU")) {
                    filterQuery.append(" and " + filter_names.get(i) + oper);
                } else if (!op.equals("IN") && !op.equals("NOTIN")) {
                    filterQuery.append(" and " + filter_names.get(i) + oper + " ? ");
                } else {
                    filterQuery.append(" and " + " " + filter_names.get(i) + oper);
                }
            }
        }
        return filterQuery.toString();
    }

    public static String orderQuery(ArrayList field_names, ArrayList field_order) {
        StringBuilder orderQuery = new StringBuilder();
        if (field_names != null) {
            for (int i = 0; i < field_names.size(); i++) {
                if (i == 0) {
                    orderQuery.append(" order by ");
                    orderQuery.append(" " + field_names.get(i) + " " + field_order.get(i));
                } else {
                    orderQuery.append(", " + field_names.get(i) + " " + field_order.get(i));
                }
            }
        }
        return orderQuery.toString();
    }

    public static String groupQuery(ArrayList field_names) throws JSONException, JSONException {
        StringBuilder orderQuery = new StringBuilder();
        if (field_names != null) {
            for (int i = 0; i < field_names.size(); i++) {
                if (i == 0) {
                    orderQuery.append(" group by ");
                    orderQuery.append(" " + field_names.get(i));
                } else {
                    orderQuery.append(", " + field_names.get(i));
                }
            }
        }
        return orderQuery.toString();
    }

    public static String hNull(String s) {
        String ret = null;
        if (!(StringUtil.isNullOrEmpty(s))) {
            ret = s;
        } else {
            ret = "";
        }
        return ret;
    }

//    Commented unused code - Mayur B
//    
//    public static KwlReturnObject buildNExecuteQuery(HibernateTemplate hibernateTemplate, String initialQuery, HashMap<String, Object> requestParams) throws ServiceException {
//        return buildNExecuteQuery(hibernateTemplate, initialQuery, requestParams, "");
//    }
//
//    public static KwlReturnObject buildNExecuteQuery(HibernateTemplate hibernateTemplate, String initialQuery, HashMap<String, Object> requestParams, String quickSearch) throws ServiceException {
//        List list = new ArrayList();
//
//        String filter = "";
//        //Get Filter String
//        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
//        if (requestParams.containsKey("filter_names") && requestParams.containsKey("filter_params")) {
//            filter_names = (ArrayList) requestParams.get("filter_names");
//            filter_params = (ArrayList) requestParams.get("filter_params");
//            //if(filter_names.size() != filter_params.size()) { //throw "size not same" exception}
//            filter = filterQuery(filter_names, "where");
//            int ind = filter.indexOf("("); // Insert in/not in params in filter query.
//            if (ind > -1) {
//                int index = Integer.valueOf(filter.substring(ind + 1, filter.indexOf(")")));
//                filter = filter.replaceAll("(" + index + ")", filter_params.get(index).toString());
//                filter_params.remove(index);
//            }
//        }
//
//        String query = initialQuery + filter;
//        //Add Advance Search Filter
//        String conditionalQuery = "";
//        if (requestParams.containsKey("ss") && !isNullOrEmpty(requestParams.get("ss").toString())) {
//            String[] ssfieldnames = (String[]) requestParams.get("ss_names");
//            String ss = (String) requestParams.get("ss");
//            conditionalQuery = StringUtil.getSearchString(ss, (isNullOrEmpty(filter) ? " where " : " and "), ssfieldnames);
//            try {
//                StringUtil.insertParamSearchString(filter_params, ss, 1);
//            } catch (SQLException ex) {
//                Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            query += conditionalQuery;
//        }
//        //Add Quick Search Filter
//        if (!isNullOrEmpty(quickSearch)) {
//            query += ((isNullOrEmpty(filter) && isNullOrEmpty(conditionalQuery)) ? " where " : " and ") + quickSearch;
//        }
//
//        //Add Order By Clause
//        if (requestParams.get("order_by") != null && requestParams.get("order_type") != null) {
//            ArrayList orderby = new ArrayList((List<String>) requestParams.get("order_by"));
//            ArrayList ordertype = new ArrayList((List<Object>) requestParams.get("order_type"));
//            //if(filter_names.size() != filter_params.size()) { //throw "size not same" exception}
//            query += orderQuery(orderby, ordertype);
//        }
//
//        list = HibernateUtil.executeQuery(hibernateTemplate, query, filter_params.toArray());
//        int count = list.size();
//
//        //Execute Paging Query
//        boolean allflag = false;
//        if (requestParams.containsKey("allflag") && requestParams.get("allflag") != null) {
//            allflag = Boolean.parseBoolean(requestParams.get("allflag").toString());
//        }
//        if (!allflag) {
//            if (requestParams.containsKey("start") && requestParams.get("start") != null && requestParams.containsKey("limit") && requestParams.get("limit") != null) {
//                int start = Integer.parseInt(requestParams.get("start").toString());
//                int limit = Integer.parseInt(requestParams.get("limit").toString());
//                list = HibernateUtil.executeQueryPaging(hibernateTemplate, query, filter_params.toArray(), new Integer[]{start, limit});
//            }
//        }
//
//        return new KwlReturnObject(true, null, null, list, count);
//    }

    /**
     * A user-friendly equal that handles one or both nulls easily
     *
     * @return
     */
    public static boolean equal(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return s1 == s2;
        }
        return s1.equals(s2);
    }
    public static boolean equalIgnoreCase(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return s1 == s2;
        }
        return s1.equalsIgnoreCase(s2);
    }
    public static String padString(String s1, int length, String pad_string, int pad_type) {
        String padStr = "";
        if (length < s1.length()) {
            padStr = s1;
        } else {
            int z = (length - s1.length()) / pad_string.length();
            for (int i = 0; i <= z; i++) {
                padStr += pad_string;
            }
            if (pad_type == 1) {
                padStr += s1;
                padStr = padStr.substring((padStr.length() - length));
            } else {
                padStr = s1 + padStr;
                padStr = padStr.substring(0, length);
            }
        }
        return padStr;
    }

    public static long hexadecimalToDecimal(String hex) throws NumberFormatException {
        long res = 0;
        if (hex.isEmpty()) {
            throw new NumberFormatException("Empty string is not a hexadecimal number");
        }
        for (int i = 0; i < hex.length(); i++) {
            char n = hex.charAt(hex.length() - (i + 1));
            int f = (int) n - 48;
            if (f > 9) {
                f = f - 7;
                if (f > 15) {
                    f = f - 32;
                }
            }
            if (f < 0 || f > 15) {
                throw new NumberFormatException("Not a hexadecimal number");
            } else {
                res += f * Math.round(Math.pow(2.0, (4 * i)));
            }
        }
        return res;
    }

    public static String stripControlCharacters(String raw) {
        if (raw == null) {
            return null;
        }
        int i;
        for (i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            // invalid control characters
            if (c < 0x20 && c != 0x09 && c != 0x0A && c != 0x0D) {
                break;
            }
            // byte-order markers and high/low surrogates
            if (c == 0xFFFE || c == 0xFFFF || (c > 0xD7FF && c < 0xE000)) {
                break;
            }
        }
        if (i >= raw.length()) {
            return raw;
        }
        StringBuilder sb = new StringBuilder(raw.substring(0, i));
        for (; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= 0x20 || c == 0x09 || c == 0x0A || c == 0x0D) {
                if (c != 0xFFFE && c != 0xFFFF && (c <= 0xD7FF || c >= 0xE000)) {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public static boolean isAsciiString(String str) {
        if (str == null) {
            return false;
        }
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);
            if ((c < 0x20 || c >= 0x7F) && c != '\r' && c != '\n' && c != '\t') {
                return false;
            }
        }
        return true;
    }

    /**
     * add the name/value mapping to the map. If an entry doesn't exist, value
     * remains a String. If an entry already exists as a String, convert to
     * String[] and add new value. If entry already exists as a String[], grow
     * array and add new value.
     *
     * @param result result map
     * @param name
     * @param value
     */
    public static void addToMultiMap(Map<String, Object> result, String name,
            String value) {
        Object currentValue = result.get(name);
        if (currentValue == null) {
            result.put(name, value);
        } else if (currentValue instanceof String) {
            result.put(name, new String[]{(String) currentValue, value});
        } else if (currentValue instanceof String[]) {
            String[] ov = (String[]) currentValue;
            String[] nv = new String[ov.length + 1];
            System.arraycopy(ov, 0, nv, 0, ov.length);
            nv[ov.length] = value;
            result.put(name, nv);
        }
    }

    /**
     * Convert an array of the form:
     *
     * a1 v1 a2 v2 a2 v3
     *
     * to a map of the form:
     *
     * a1 -> v1 a2 -> [v2, v3]
     */
    public static Map<String, Object> keyValueArrayToMultiMap(String[] args,
            int offset) {
        Map<String, Object> attrs = new HashMap<String, Object>();
        for (int i = offset; i < args.length; i += 2) {
            String n = args[i];
            if (i + 1 >= args.length) {
                throw new IllegalArgumentException("not enough arguments");
            }
            String v = args[i + 1];
            addToMultiMap(attrs, n, v);
        }
        return attrs;
    }
    private static final int TERM_WHITESPACE = 1;
    private static final int TERM_SINGLEQUOTE = 2;
    private static final int TERM_DBLQUOTE = 3;

    /**
     * open the specified file and return the first line in the file, without
     * the end of line character(s).
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static String readSingleLineFromFile(String file) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));
            return in.readLine();
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    /**
     * read a line from "in", using readLine(). A trailing '\\' on the line will
     * be treated as continuation and the next line will be read and appended to
     * the line, without the \\.
     *
     * @param in
     * @return complete line or null on end of file.
     * @throws IOException
     */
    public static String readLine(BufferedReader in) throws IOException {
        String line;
        StringBuilder sb = null;

        while ((line = in.readLine()) != null) {
            if (line.length() == 0) {
                break;
            } else if (line.charAt(line.length() - 1) == '\\') {
                if (sb == null) {
                    sb = new StringBuilder();
                }
                sb.append(line.substring(0, line.length() - 1));
            } else {
                break;
            }
        }

        if (line == null) {
            if (sb == null) {
                return null;
            } else {
                return sb.toString();
            }
        } else {
            if (sb == null) {
                return line;
            } else {
                sb.append(line);
                return sb.toString();
            }
        }
    }

    public static List<String> parseSieveStringList(String value)
            throws ServiceException {
        List<String> result = new ArrayList<String>();
        if (value == null) {
            return result;
        }
        value = value.trim();
        if (value.length() == 0) {
            return result;
        }
        int i = 0;
        boolean inStr = false;
        boolean inList = false;
        StringBuilder sb = null;
        while (i < value.length()) {
            char ch = value.charAt(i++);
            if (inStr) {
                if (ch == '"') {
                    result.add(sb.toString());
                    inStr = false;
                } else {
                    if (ch == '\\' && i < value.length()) {
                        ch = value.charAt(i++);
                    }
                    sb.append(ch);
                }
            } else {
                if (ch == '"') {
                    inStr = true;
                    sb = new StringBuilder();
                } else if (ch == '[' && !inList) {
                    inList = true;
                } else if (ch == ']' && inList) {
                    inList = false;
                } else if (!Character.isWhitespace(ch)) {
                    throw ServiceException.INVALID_REQUEST(
                            "unable to parse string list: " + value, null);
                }
            }
        }
        if (inStr || inList) {
            throw ServiceException.INVALID_REQUEST(
                    "unable to parse string list2: " + value, null);
        }
        return result;
    }

    /**
     * split a line into array of Strings, using a shell-style syntax for
     * tokenizing words.
     *
     * @param line
     * @return
     */
    public static String[] parseLine(String line) {
        ArrayList<String> result = new ArrayList<String>();

        int i = 0;

        StringBuilder sb = new StringBuilder(32);
        int term = TERM_WHITESPACE;
        boolean inStr = false;

        scan:
        while (i < line.length()) {
            char ch = line.charAt(i++);
            boolean escapedTerm = false;

            if (ch == '\\' && i < line.length()) {
                ch = line.charAt(i++);
                switch (ch) {
                    case '\\':
                        break;
                    case 'n':
                        ch = '\n';
                        escapedTerm = true;
                        break;
                    case 't':
                        ch = '\t';
                        escapedTerm = true;
                        break;
                    case 'r':
                        ch = '\r';
                        escapedTerm = true;
                        break;
                    case '\'':
                        ch = '\'';
                        escapedTerm = true;
                        break;
                    case '"':
                        ch = '"';
                        escapedTerm = true;
                        break;
                    default:
                        escapedTerm = Character.isWhitespace(ch);
                        break;
                }
            }

            if (inStr) {
                if (!escapedTerm
                        && ((term == TERM_WHITESPACE && Character.isWhitespace(ch))
                        || (term == TERM_SINGLEQUOTE && ch == '\'') || (term == TERM_DBLQUOTE && ch == '"'))) {
                    inStr = false;
                    result.add(sb.toString());
                    sb = new StringBuilder(32);
                    term = TERM_WHITESPACE;
                    continue scan;
                }
                sb.append(ch);
            } else {
                if (!escapedTerm) {
                    switch (ch) {
                        case '\'':
                            term = TERM_SINGLEQUOTE;
                            inStr = true;
                            continue scan;
                        case '"':
                            term = TERM_DBLQUOTE;
                            inStr = true;
                            continue scan;
                        default:
                            if (Character.isWhitespace(ch)) {
                                continue scan;
                            }
                            inStr = true;
                            sb.append(ch);
                            break;
                    }
                } else {
                    // we had an escaped terminator, start a new string
                    inStr = true;
                    sb.append(ch);
                }
            }
        }

        if (sb.length() > 0) {
            result.add(sb.toString());
        }

        return result.toArray(new String[result.size()]);
    }

    private static void dump(String line) {
        String[] result = parseLine(line);
        System.out.println("line: " + line);
        for (int i = 0; i < result.length; i++) {
            System.out.println(i + ": (" + result[i] + ")");
        }
        System.out.println();
    }

    public static void main(String args[]) {
        dump("this is a test");
        dump("this is 'a nother' test");
        dump("this is\\ test");
        dump("first Roland last 'Schemers' full 'Roland Schemers'");
        dump("multi 'Roland\\nSchemers'");
        dump("a");
        dump("");
        dump("\\  \\ ");
        dump("backslash \\\\");
        dump("backslash \\f");
        dump("a           b");
    }
    // A pattern that matches the beginning of a string followed by ${KEY_NAME}
    // followed
    // by the end. There are three groups: the beginning, KEY_NAME and the end.
    // Pattern.DOTALL is required in case one of the values in the map has a
    // newline
    // in it.
    private static Pattern templatePattern = Pattern.compile(
            "(.*)\\$\\{([^\\)]+)\\}(.*)", Pattern.DOTALL);

    /**
     * Substitutes all occurrences of the specified values into a template. Keys
     * for the values are specified in the template as <code>${KEY_NAME}</code>.
     *
     * @param template the template
     * @param vars a <code>Map</code> filled with keys and values. The keys must
     * be <code>String</code>s.
     * @return the template with substituted values
     */
    public static String fillTemplate(String template, Map vars) {
        if (template == null) {
            return null;
        }

        String line = template;
        Matcher matcher = templatePattern.matcher(line);

        // Substitute multiple variables per line
        while (matcher.matches()) {
            String key = matcher.group(2);
            Object value = vars.get(key);
            if (value == null) {
                KrawlerLog.misc.info("fillTemplate(): could not find key '"
                        + key + "'");
                value = "";
            }
            line = matcher.group(1) + value + matcher.group(3);
            matcher.reset(line);
        }
        return line;
    }

    /**
     * Joins an array of <code>short</code>s, separated by a delimiter.
     */
    public static String join(String delimiter, short[] array) {
        if (array == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            buf.append(array[i]);
            if (i + 1 < array.length) {
                buf.append(delimiter);
            }
        }
        return buf.toString();
    }

    /**
     * Joins an array of objects, separated by a delimiter.
     */
    public static String join(String delimiter, Object[] array) {
        if (array == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();

        for (int i = 0; i < array.length; i++) {
            buf.append(array[i]);
            if (i + 1 < array.length) {
                buf.append(delimiter);
            }
        }
        return buf.toString();
    }

    public static <E> String join(String delimiter, Collection<E> col) {
        if (col == null) {
            return null;
        }
        Object[] array = new Object[col.size()];
        col.toArray(array);
        return join(delimiter, array);
    }

    /**
     * Returns the simple class name (the name after the last dot) from a
     * fully-qualified class name. Behavior is the same as
     * {@link #getExtension}.
     */
    public static String getSimpleClassName(String className) {
        return getExtension(className);
    }

    /**
     * Returns the simple class name (the name after the last dot) for the
     * specified object.
     */
    public static String getSimpleClassName(Object o) {
        if (o == null) {
            return null;
        }
        return getExtension(o.getClass().getName());
    }

    /**
     * Returns the extension portion of the given filename. <ul> <li>If
     * <code>filename</code> contains one or more dots, returns all characters
     * after the last dot.</li> <li>If <code>filename</code> contains no dot,
     * returns <code>filename</code>.</li> <li>If <code>filename</code> is
     * <code>null</code>, returns <code>null</code>.</li> <li>If
     * <code>filename</code> ends with a dot, returns an empty
     * <code>String</code>.</li> </ul>
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return null;
        }
        int lastDot = filename.lastIndexOf(".");
        if (lastDot == -1) {
            return filename;
        }
        if (lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1, filename.length());
    }

    /**
     * Returns <code>true</code> if the secified string is <code>null</code> or
     * its length is <code>0</code>.
     */
    public static boolean isNullOrEmpty(String s) {
        if (s == null || s.length() == 0) {
            return true;
        }
        return false;
    }
    private static final String[] JS_CHAR_ENCODINGS = {"\\u0000", "\\u0001",
        "\\u0002", "\\u0003", "\\u0004", "\\u0005", "\\u0006", "\\u0007",
        "\\b", "\\t", "\\n", "\\u000B", "\\f", "\\r", "\\u000E", "\\u000F",
        "\\u0010", "\\u0011", "\\u0012", "\\u0013", "\\u0014", "\\u0015",
        "\\u0016", "\\u0017", "\\u0018", "\\u0019", "\\u001A", "\\u001B",
        "\\u001C", "\\u001D", "\\u001E", "\\u001F"};
    
    /**
     * Returns <code>true</code> if the secified string is <code>null</code> or
     * its length is <code>0</code>.
     */
    public static boolean isNullOrEmptyWithTrim(String s) {
        if (s == null || s.length() == 0 || s.trim().length() == 0) {
            return true;
        }
        return false;
    }

    public static String jsEncode(Object obj) {
        if (obj == null) {
            return "";
        }
        String replacement, str = obj.toString();
        StringBuilder sb = null;
        int i, last, length = str.length();
        for (i = 0, last = -1; i < length; i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\\':
                    replacement = "\\\\";
                    break;
                case '"':
                    replacement = "\\\"";
                    break;
                case '\u2028':
                    replacement = "\\u2028";
                    break;
                case '\u2029':
                    replacement = "\\u2029";
                    break;
                case '\'':
                    replacement = "\\\'";
                    break;
                default:
                    if (c >= ' ') {
                        continue;
                    }
                    replacement = JS_CHAR_ENCODINGS[c];
                    break;
            }
            if (sb == null) {
                sb = new StringBuilder(str.substring(0, i));
            } else {
                sb.append(str.substring(last, i));
            }
            sb.append(replacement);
            last = i + 1;
        }
        return (sb == null ? str : sb.append(str.substring(last, i)).toString());
    }

    public static String jsEncodeKey(String key) {
        return '"' + key + '"';
    }
    //
    // HTML methods
    //
    private static final Pattern PAT_AMP = Pattern.compile("&",
            Pattern.MULTILINE);
    private static final Pattern PAT_LT = Pattern.compile("<",
            Pattern.MULTILINE);
    private static final Pattern PAT_GT = Pattern.compile(">",
            Pattern.MULTILINE);
    private static final Pattern PAT_DBLQT = Pattern.compile("\"",
            Pattern.MULTILINE);

    /**
     * Escapes special characters with their HTML equivalents.
     */
    public static String escapeHtml(String text) {
        if (text == null || text.length() == 0) {
            return "";
        }
        String s = replaceAll(text, PAT_AMP, "&amp;");
        s = replaceAll(s, PAT_LT, "&lt;");
        s = replaceAll(s, PAT_GT, "&gt;");
        s = replaceAll(s, PAT_DBLQT, "&quot;");
        return s;
    }

    private static String replaceAll(String text, Pattern pattern,
            String replace) {
        Matcher m = pattern.matcher(text);
        StringBuffer sb = null;
        while (m.find()) {
            if (sb == null) {
                sb = new StringBuffer();
            }
            m.appendReplacement(sb, replace);
        }
        if (sb != null) {
            m.appendTail(sb);
        }
        return sb == null ? text : sb.toString();
    }

    public static boolean stringCompareInLowercase(String strToCompareWith,
            String strTobeCompare) {
        return strToCompareWith.equalsIgnoreCase(strTobeCompare);

    }

    public static String getMySearchString(String searchString, String appendCase, String[] searchParams) {
        StringBuilder myResult = new StringBuilder();

        if (!isNullOrEmpty(searchString)) {
            for (int i = 0; i < searchParams.length; i++) {
                myResult.append(" ");
                if (i == 0) {
                    myResult.append(appendCase);
                    myResult.append(" (( ");
                } else {
                    myResult.append(" ( ");
                }
                myResult.append(searchParams[i] + " like ? or " + searchParams[i] + " like ?");
                if (i + 1 < searchParams.length) {
                    myResult.append(") ");
                } else {
                    myResult.append(")) ");
                }
                if (i + 1 < searchParams.length) {
                    myResult.append(" or ");
                }
            }
        } else {
            myResult.append(" ");
        }
        return myResult.toString();
    }

    public static int insertParamSearchString(int cnt, java.sql.PreparedStatement pstmt, String searchString, int searchParamsLength)
            throws SQLException {
        int i = 0;
        if (!isNullOrEmpty(searchString)) {
            String trimedStr = searchString.trim();
            for (i = 0; i < searchParamsLength * 2; i++) {
                if (i % 2 == 0) {
                    pstmt.setString(cnt + i, trimedStr + "%");
                } else {
                    pstmt.setString(cnt + i, "% " + trimedStr + "%");
                }
            }
        }
        return (cnt + i);
    }
    
    public static Map insertParamSearchStringMap(ArrayList al, String searchString, int searchParamsLength)
            throws SQLException {
        Map<String, Object> map = new HashMap<>();
        if(searchString.contains("\\")){
            searchString = searchString.replaceAll("\\\\", "\\\\\\\\");
        }
        map.put("al", al);
        map.put("searchString", searchString);
        map.put("searchParamsLength", searchParamsLength);
        return map;

    }

//    public static Map insertParamSearchString(ArrayList al, String searchString, int searchParamsLength)
//            throws SQLException {
//        Map<String, Object> map = new HashMap<>();
//        map.put("al", al);
//        map.put("searchString", searchString);
//        map.put("searchParamsLength", searchParamsLength);
//        return map;
//
//    }

    public static void insertParamSearchString(Map map)
            throws SQLException {
        ArrayList al = (ArrayList) map.get("al");
        String searchString = (String) map.get("searchString");
        if(!isNullOrEmpty(searchString)) {
            
            int searchPatternFlag = Constants.PRODUCT_SEARCH_ANYWHERE;
            if(map.containsKey(Constants.PRODUCT_SEARCH_FLAG) && map.get(Constants.PRODUCT_SEARCH_FLAG) != null){
                if((int)map.get(Constants.PRODUCT_SEARCH_FLAG) == Constants.PRODUCT_SEARCH_STARTSWITH) {
                    searchPatternFlag = Constants.PRODUCT_SEARCH_STARTSWITH;
                }
            }

            int searchParamsLength = (int) map.get("searchParamsLength");
            int i = 0;
            if (searchPatternFlag == Constants.PRODUCT_SEARCH_STARTSWITH) {
                String trimedStr = searchString.trim();
                for (i = 0; i < searchParamsLength * 2; i++) {
                    al.add("%" + trimedStr + "%");
                }
            } else {
                String trimedStr = searchString.trim();
                for (i = 0; i < searchParamsLength * 2; i++) {
                    al.add("%" + trimedStr + "%");
                }
            }
        }
    }

    public static String serverHTMLStripper(String stripTags)
            throws IllegalStateException, IndexOutOfBoundsException {
        Pattern p = Pattern.compile("<[^>]*>");
        Matcher m = p.matcher(stripTags);
        StringBuffer sb = new StringBuffer();
        if (!isNullOrEmpty(stripTags)) {
            while (m.find()) {
                m.appendReplacement(sb, "");
            }
            m.appendTail(sb);
            stripTags = sb.toString();
        }
        return stripTags.trim();
    }
    /*
    To remove Html tags & "&nbsp"
    */
    public static String replaceFullHTML(String stripTags)
            throws IllegalStateException, IndexOutOfBoundsException {
        Pattern p = Pattern.compile("<[^>]*>");
        Matcher m = p.matcher(stripTags);
        StringBuffer sb = new StringBuffer();
        if (!isNullOrEmpty(stripTags)) {
            while (m.find()) {
                m.appendReplacement(sb, "");
            }
            m.appendTail(sb);
            stripTags = sb.toString();
        }
        stripTags = stripTags.replaceAll("&nbsp;"," ");
        return stripTags.trim();
    }

    public static String checkForNull(String rsString) {
        return rsString != null ? rsString : "";
    }

    public static boolean serverValidateEmail(String email) {
        boolean result = true;
        String emailCheck = "^[\\w_\\-%\\.]+@[\\w_\\-%\\.]+\\.[a-zA-Z]{2,6}$";
        if (!isNullOrEmpty(email)) {
            result = email.matches(emailCheck);
        }
        return result;
    }

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static int getDaysDiff(String Estartts, String Eendts) {
        double days = 0;
        double diffInMilleseconds = 0;
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.text.SimpleDateFormat sdf1 = new java.text.SimpleDateFormat("yyyy-MM-dd 00:00:00");

            if (Estartts.compareTo("") != 0 && Eendts.compareTo("") != 0) {
                java.util.Date sdt = sdf.parse(Estartts);
                java.util.Date edt = sdf.parse(Eendts);

                Estartts = sdf1.format(sdt);
                Eendts = sdf1.format(edt);

                java.util.Date dt1 = sdf1.parse(Estartts);
                java.util.Date dt2 = sdf1.parse(Eendts);

                diffInMilleseconds = dt2.getTime() - dt1.getTime();
                days = Math.round(diffInMilleseconds / (1000 * 60 * 60 * 24));
            }
        } catch (ParseException ex) {
            days = 0;
            throw ServiceException.FAILURE("crmReports.getDayDiff", ex);
        } finally {
            return (int) days;
        }
    }

    public static String getSearchString(String searchString, String appendCase, String[] searchParams) {
        StringBuilder myResult = new StringBuilder();

        if (!isNullOrEmpty(searchString)) {
            for (int i = 0; i < searchParams.length; i++) {
                myResult.append(" ");
                if (i == 0) {
                    myResult.append(appendCase);
                    myResult.append(" (( ");
                } else {
                    myResult.append(" ( ");
                }
                myResult.append(searchParams[i] + " like ? or " + searchParams[i] + " like ?");
                if (i + 1 < searchParams.length) {
                    myResult.append(") ");
                } else {
                    myResult.append(")) ");
                }
                if (i + 1 < searchParams.length) {
                    myResult.append(" or ");
                }
            }
        } else {
            myResult.append(" ");
        }
        return myResult.toString();
    }

    public static double getDouble(String str) {
        double value = 0.0;
        try {
            value = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            //if wrong format will be given, 0.0 will be used as default
        } catch (NullPointerException e) {
            //if no value will be given, 0.0 will be used as default
        } catch (Exception e) {
            //On any general exception, 0.0 will be used as default
        }
        return value;
    }

    public static int getInteger(String str) {
        int value = 0;
        try {
            value = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            //if wrong format will be given, 0.0 will be used as default
        } catch (NullPointerException e) {
            //if no value will be given, 0.0 will be used as default
        } catch (Exception e) {
            //On any general exception, 0.0 will be used as default
        }
        return value;
    }

    public static boolean getBoolean(String str) {
        return "true".equalsIgnoreCase(str);
    }

    public static List getPagedList(List list, int start, int limit) {
        List pagedList = new ArrayList();
        if (list.size() >= start) {
            int end = Math.min(list.size(), start + limit);
            for (int i = start; i < end; i++) {
                pagedList.add(list.get(i));
            }
        } else {
            pagedList = list;
        }
        return pagedList;
    }

    public static JSONArray getPagedJSON(JSONArray jArr, int start, int limit) throws JSONException {
        JSONArray pagedjArr = new JSONArray();
        if (jArr.length() >= start) {
            int end = Math.min(jArr.length(), start + limit);
            for (int i = start; i < end; i++) {
                pagedjArr.put(jArr.getJSONObject(i));
            }
        } else {
            pagedjArr = jArr;
        }
        return pagedjArr;
    }

    /**
     * Method used to perform paging on JSONArray object with respect to the
     * key.
     *
     * @param dataJsonArr JSONArray which consists of records
     * @param start Offset for the paging
     * @param limit Limit for the paging
     * @param key Key on which paging should be done
     * @return JSONObject consists of keys: Constants.RES_TOTALCOUNT - total
     * number of records, Constants.PAGED_JSON - Paged JSON according to the
     * key.
     */
    public static JSONObject getPagedJSONWithKey(JSONArray dataJsonArr, int start, int limit, String key) {
        JSONObject jobj = new JSONObject();
        try {
            JSONArray pagedJson = new JSONArray();
            int totalCount = 0;
            String prevKeyValue = ""; // previous product Id required to update the product counter
            int pagingCounter = 0; // records to be shown
            boolean recordLimitExceeded = false; // has product count is exceeded 
            for (int i = 0; i < dataJsonArr.length(); i++) {
                JSONObject productJSON = dataJsonArr.getJSONObject(i);
                String currentKeyVal = productJSON.optString(key);
                if (pagingCounter >= (start + limit)) {
                    if (!prevKeyValue.equals(currentKeyVal)) {
                        totalCount++;
                    }
                    recordLimitExceeded = true;
                }
                if (!recordLimitExceeded) {
                    if (StringUtil.isNullOrEmpty(prevKeyValue)) {
                        prevKeyValue = currentKeyVal;
                        pagingCounter++;
                        totalCount++;
                    }
                    if (start > pagingCounter) {
                        if (!prevKeyValue.equals(currentKeyVal)) {
                            pagingCounter++;
                            totalCount++;
                        }
                    } else {
                        pagedJson.put(productJSON);
                        if (!prevKeyValue.equals(currentKeyVal)) {
                            pagingCounter++;
                            totalCount++;
                        }
                    }
                }
                prevKeyValue = currentKeyVal;
            }
            jobj.put(Constants.PAGED_JSON, pagedJson);
            jobj.put(Constants.RES_TOTALCOUNT, totalCount);
        } catch (Exception ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.WARNING, ex.getMessage());
        }
        return jobj;
    }
    public static JSONArray getPagedJSONForAgedWIthTotal(JSONArray jArr, int start, int limit) throws JSONException {
        JSONArray pagedjArr = new JSONArray();
        double amountdueinbaseGrandTotal = 0.0;
        double amountdueinbasePageTotal = 0.0;

        double totalInBaseGrandTotal = 0.0;
        double totalInBasePageTotal = 0.0;

        if (jArr.length() >= start) {
            int end = Math.min(jArr.length(), start + limit);
            //If page is second or greater then the second page then start is always greater than 0, Below loop is to calculate till start
            if(start > 0){
                for(int i = 0 ; i < start; i ++){
                    JSONObject tempJobj = jArr.getJSONObject(i);
                    amountdueinbaseGrandTotal += tempJobj.optDouble("amountdueinbase", 0.0);
                    totalInBaseGrandTotal += tempJobj.optDouble("totalinbase", 0.0);
                }
            } 
            for (int i = start; i < end; i++) {//Loop For paged Total
                JSONObject tempJobj = jArr.getJSONObject(i);
                amountdueinbasePageTotal += tempJobj.optDouble("amountdueinbase", 0.0);
                totalInBasePageTotal += tempJobj.optDouble("totalinbase", 0.0);
                pagedjArr.put(tempJobj);
            }

            amountdueinbaseGrandTotal += amountdueinbasePageTotal;
            totalInBaseGrandTotal += totalInBasePageTotal;
            for (int i = end; i < jArr.length(); i++) {// Loop For grand Total
                JSONObject tempJobj = jArr.getJSONObject(i);
                amountdueinbaseGrandTotal += tempJobj.optDouble("amountdueinbase", 0.0);
                totalInBaseGrandTotal += tempJobj.optDouble("totalinbase", 0.0);
            }
        } else {
            pagedjArr = jArr;
        } 
        JSONObject jtemp = new JSONObject();
        jtemp.put("pagedAmountdueinbase", amountdueinbasePageTotal);
        jtemp.put("pagedTotalInBase", totalInBasePageTotal);
        jtemp.put("grandAmountdueinbase", amountdueinbaseGrandTotal);
        jtemp.put("grandTotalInBase", totalInBaseGrandTotal);
        pagedjArr.put((pagedjArr.length()), jtemp);
        return pagedjArr;
    }

    public static JSONArray getPagedJSONWithPageTotalAmount(JSONArray jArr, int start, int limit) throws JSONException {
        JSONArray pagedjArr = new JSONArray();
        if (jArr.length() >= start) {
            int end = Math.min(jArr.length(), start + limit);
            double pageTotal=0;
            for (int i = start; i < end; i++) {
                 JSONObject tempJobj = jArr.getJSONObject(i);
                pageTotal += tempJobj.optDouble("ledgerFinalValuation", 0.0);
                
                if(i==end-1){
                   tempJobj.put("pagetotal", pageTotal);
                }
                pagedjArr.put(jArr.getJSONObject(i));
            }
            
        } else {
            pagedjArr = jArr;
        }
        return pagedjArr;
    }
    
    public static String abbreviate(String str, int maxWidth) {
        return abbreviate(str, 0, maxWidth);
    }

    public static String abbreviate(String str, int offset, int maxWidth) {
        if (str == null) {
            return null;
        }
        if (maxWidth < 4) {
            throw new IllegalArgumentException("Minimum abbreviation width is 4");
        }
        if (str.length() <= maxWidth) {
            return str;
        }
        if (offset > str.length()) {
            offset = str.length();
        }
        if ((str.length() - offset) < (maxWidth - 3)) {
            offset = str.length() - (maxWidth - 3);
        }
        if (offset <= 4) {
            return str.substring(0, maxWidth - 3) + "...";
        }
        if (maxWidth < 7) {
            throw new IllegalArgumentException("Minimum abbreviation width with offset is 7");
        }
        if ((offset + (maxWidth - 3)) < str.length()) {
            return "..." + abbreviate(str.substring(offset), maxWidth - 3);
        }
        return "..." + str.substring(str.length() - (maxWidth - 3));
    }

    public static String getSearchquery(String ss, String[] searchcol, ArrayList params) {
        boolean success = false;
        String searchQuery = "";
        try {

            if (ss != null && !StringUtil.isNullOrEmpty(ss)) {
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, searchcol.length);
                StringUtil.insertParamSearchString(SearchStringMap);
                searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
            }

        } catch (Exception e) {
            success = false;
            e.printStackTrace();
        } finally {
            return searchQuery;
        }
    }

//    public static KwlReturnObject getPagingquery(HashMap<String, Object> requestParams, String[] searchcol, HibernateTemplate hibernateTemplate, String hql, ArrayList params) {
//        boolean success = false;
//        List lst = null;
//        int count = 0;
//        try {
//            String allflag = "true";
//            if (requestParams.containsKey("allflag")) {
//                allflag = requestParams.get("allflag").toString();
//            }
//            int start = 0;
//            int limit = 0;
//
//            if (allflag.equals("false")) {
//                start = Integer.parseInt(requestParams.get("start").toString());
//                limit = Integer.parseInt(requestParams.get("limit").toString());
//            }
//
//            lst = HibernateUtil.executeQuery(hibernateTemplate, hql, params.toArray());
//            count = lst.size();
//            if (allflag.equals("false")) {
//                lst = HibernateUtil.executeQueryPaging(hibernateTemplate, hql, params.toArray(), new Integer[]{start, limit});
//            }
//            success = true;
//        } catch (Exception e) {
//            success = false;
//            e.printStackTrace();
//        } finally {
//            return new KwlReturnObject(success, "", "-1", lst, count);
//        }
//    }

    public static boolean checkResultobjList(KwlReturnObject result) {
        if (result.getEntityList() != null && !result.getEntityList().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkpaging(HashMap<String, Object> requestParams, HttpServletRequest request) {
        if (request.getParameter("start") != null && request.getParameter("limit") != null) {
            requestParams.put("start", Integer.valueOf(request.getParameter("start")));
            requestParams.put("limit", Integer.valueOf(request.getParameter("limit")));
            requestParams.put("allflag", false);
            return true;
        } else if (requestParams.containsKey("allflag") && !(Boolean) requestParams.get("allflag")) {
            requestParams.put("start", 0);
            requestParams.put("limit", 15);
            return true;
        } else {
            return false;
        }
    }

    public static String makeExternalRequest(String urlstr, String postdata) {
        String result = "";
        try {
            URL url = new URL(urlstr);
            try {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                DataOutputStream d = new DataOutputStream(conn.getOutputStream());
                String data = postdata;
                OutputStreamWriter ow = new OutputStreamWriter(d);
                ow.write(data);
                ow.close();
                BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder stringbufff = new StringBuilder();
                while ((inputLine = input.readLine()) != null) {
                    stringbufff.append(inputLine);
                }
                result = stringbufff.toString();
                input.close();
            } catch (IOException ex) {
                System.out.print(ex);
            }

        } catch (MalformedURLException ex) {
            System.out.print(ex);
        } finally {
            return result;
        }
    }

    public static String convertToTwoDecimal(double value) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("0.00");
        return df.format(value);
    }

    public static LinkedHashMap sortHashMapByValuesD(HashMap passedMap) {
        List mapKeys = new ArrayList(passedMap.keySet());
        List mapValues = new ArrayList(passedMap.values());
        Collections.sort(mapValues);
        Collections.sort(mapKeys);
        Collections.reverse(mapValues);
        LinkedHashMap sortedMap
                = new LinkedHashMap();

        Iterator valueIt = mapValues.iterator();
        while (valueIt.hasNext()) {
            Object val = valueIt.next();
            Iterator keyIt = mapKeys.iterator();

            while (keyIt.hasNext()) {
                Object key = keyIt.next();
                String comp1 = passedMap.get(key).toString();
                String comp2 = val.toString();
                if (comp1.equals(comp2)) {
                    passedMap.remove(key);
                    mapKeys.remove(key);
                    sortedMap.put((String) key, (Double) val);
                    break;
                }
            }
        }
        return sortedMap;
    }

    public static Map<String, Object> sortMapByKeyValue(Map<String, Object> unsortMap) {

        // Convert Map to List
        List<Map.Entry<String, Object>> list = new LinkedList<Map.Entry<String, Object>>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
            @Override
            public int compare(Map.Entry<String, Object> entry1, Map.Entry<String, Object> entry2) {
                return (entry1.getKey()).compareTo(entry2.getKey());
            }
        });

        // Convert sorted map back to a Map
        Map<String, Object> sortedMap = new LinkedHashMap<String, Object>();
        for (Iterator<Map.Entry<String, Object>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    public static HashMap<Integer, String> sortMapByIntegerKeyValue(Map<Integer, String> unsortMap) {

        // Convert Map to List
        List<Map.Entry<Integer, String>> list = new LinkedList<Map.Entry<Integer, String>>(unsortMap.entrySet());

        // Sort list with comparator, to compare the Map values
        Collections.sort(list, new Comparator<Map.Entry<Integer, String>>() {
            @Override
            public int compare(Map.Entry<Integer, String> entry1, Map.Entry<Integer, String> entry2) {
                return (entry1.getKey()).compareTo(entry2.getKey());
            }
        });

        // Convert sorted map back to a Map
        HashMap<Integer, String> sortedMap = new LinkedHashMap<Integer, String>();
        for (Iterator<Map.Entry<Integer, String>> it = list.iterator(); it.hasNext();) {
            Map.Entry<Integer, String> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    /**
     * Sort JSON array on given key label with case Sensitive or Not (depend
     * upon isCase value).
     *
     * @param array
     * @param key
     * @param isCase
     * @param asc
     * @return Sorted JSON Array
     */
    public static JSONArray sortJsonArray(JSONArray array, final String key, final boolean isCase, final boolean asc) {
        List<JSONObject> jsonsList = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsonsList.add(array.getJSONObject(i));
            }
            Collections.sort(jsonsList, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject deskera_1, JSONObject deskera_2) {
                    String deskeraCompareString1 = "", deskeraCompareString2 = "";
                    try {
                        deskeraCompareString1 = deskera_1.getString(key); //Key must be present in JSON
                        deskeraCompareString2 = deskera_2.getString(key); //Key must be present in JSON
                    } catch (JSONException ex) {
                        Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (asc) { // Sorting Order
                        return isCase ? deskeraCompareString1.compareTo(deskeraCompareString2) : deskeraCompareString1.compareToIgnoreCase(deskeraCompareString2);
                    } else {
                        return isCase ? -deskeraCompareString1.compareTo(deskeraCompareString2) : -deskeraCompareString1.compareToIgnoreCase(deskeraCompareString2);
                    }
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONArray(jsonsList);
    }
    public static JSONArray findJsonArray(JSONArray array, JSONObject resobj) {
        JSONArray returnArray = new JSONArray();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jobj = array.getJSONObject(i);
                Iterator<?> keys = resobj.keys();
                int count = 0;
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (resobj.get(key) instanceof Boolean) {
                        count++;
                    } else if (resobj.get(key) instanceof Integer && jobj.optInt(key, -1) == resobj.optInt(key, -2)) {
                        count++;
                    } else if (resobj.get(key) instanceof Double) {
                        count++;
                    } else if (resobj.get(key) instanceof String) {
                        count++;
                    }
                }
                if (count == resobj.length()) {
                    returnArray.put(jobj);
                    break;
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnArray;
    }
    /**
     * Find All JSON Object from JSON Array
     * @param array
     * @param resobj
     * @return 
     */
    public static JSONArray findAllJsonObjectFromJsonArray(JSONArray array, JSONObject resobj) {
        JSONArray returnArray = new JSONArray();
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject jobj = array.getJSONObject(i);
                Iterator<?> keys = resobj.keys();
                int count = 0;
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (resobj.get(key) instanceof Boolean) {
                        count++;
                    } else if (resobj.get(key) instanceof Integer && jobj.optInt(key, -1) == resobj.optInt(key, -2)) {
                        count++;
                    } else if (resobj.get(key) instanceof Double) {
                        count++;
                    } else if (resobj.get(key) instanceof String) {
                        count++;
                    }
                }
                if (count == resobj.length()) {
                    returnArray.put(jobj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnArray;
    }

    public static String replaceSearchquery(String replaceSS) {
        return replaceSS.replaceAll(Constants.percent, "\\\\%").replaceAll("_", "\\\\_");
    }

    public static void insertParamAdvanceSearchString1(ArrayList al, String Searchjson)
            throws JSONException, JSONException, ParseException {

        JSONObject jobj = new JSONObject(Searchjson);
        int count = jobj.getJSONArray(Constants.root).length();

        for (int i = 0; i < count; i++) {
            JSONObject jobj1 = jobj.getJSONArray(Constants.root).getJSONObject(i);
            if (jobj1.optBoolean("isdefaultfield") == true) {
                continue;
            }
            String trimedStr = jobj1.getString(Constants.searchText).trim();
            trimedStr = replaceSearchquery(trimedStr);
            String arraySearchstr[] = trimedStr.split(",");
            if (jobj1.getString(Constants.xtype).equalsIgnoreCase(Constants.datefield)) {
                String a = ",";
                arraySearchstr = trimedStr.split(",\"");
            }
            for (int searchlength = 0; searchlength < arraySearchstr.length; searchlength++) {
                if (jobj1.has(Constants.xtype)) {
                    if (((!StringUtil.equal(jobj1.getString(Constants.xtype), Constants.Combo)) || (!StringUtil.equal(trimedStr, Constants.NineNine))) && !(StringUtil.equal(trimedStr, Constants.blankSearchKey) || StringUtil.equal(trimedStr, Constants.blankSearchId))) {
                        Boolean iscustomcolumn = Boolean.parseBoolean(jobj1.getString(Constants.iscustomcolumn));
                        if (StringUtil.equalIgnoreCase(jobj1.getString(Constants.xtype), Constants.Datefield)) {
                            if (arraySearchstr[searchlength].contains("To")) {
                                String[] tempParams = arraySearchstr[searchlength].split("To");
                                if (tempParams.length == 2 || tempParams.length == 1) {
                                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);//Removed time zone difference from date  format 
//                                    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
//                                    Calendar cst = Calendar.getInstance();
                                    if (!StringUtil.isNullOrEmpty(tempParams[0].trim())) {
//                                        Date fromDate = sdf.parse(StringUtil.checkForNull(tempParams[0]).trim());
                                        SimpleDateFormat sqlFormat = new SimpleDateFormat("YYYY-MM-dd"); //// Database Format.
                                        Date fromDate = sdf.parse(tempParams[0]);
                                        String queryDate = sqlFormat.format(fromDate);
                                        //This Code is commented because Date is no longer stored in milliseconds now and is stored as Date Object.
//                                        cst.setTime(fromDate);
//                                        cst.set(Calendar.HOUR_OF_DAY, 0);
//                                        cst.set(Calendar.MINUTE, 0);
//                                        cst.set(Calendar.SECOND, 0);
//                                        cst.set(Calendar.MILLISECOND, 0);
//                                        long sday = cst.getTimeInMillis();
//                                        if (iscustomcolumn) {
//                                            al.add(queryDate);
//                                        } else {
                                        al.add(fromDate);
//                                        }
                                    }
                                    if (tempParams.length == 2 && !StringUtil.isNullOrEmpty(tempParams[1].trim())) {
                                        Date toDate = sdf.parse(StringUtil.checkForNull(tempParams[1]).trim());
                                        SimpleDateFormat sqlFormat = new SimpleDateFormat("YYYY-MM-dd"); // Database Format.
                                        String queryDate = sqlFormat.format(toDate);
                                        //This Code is commented because Date is no longer stored in milliseconds now and is stored as Date Object.
//                                        cst.setTime(toDate);
//                                        cst.set(Calendar.HOUR_OF_DAY, 23);
//                                        cst.set(Calendar.MINUTE, 59);
//                                        cst.set(Calendar.SECOND, 59);
//                                        cst.set(Calendar.MILLISECOND, 0);
//                                        long eday = cst.getTimeInMillis();
//                                        if (iscustomcolumn) {
//                                            al.add(queryDate);
//                                        } else {
                                        al.add(toDate);
//                                        }
                                    }
                                }
                            } else {
                                Date fromDate = new SimpleDateFormat(Constants.MMMMdyyyy).parse(arraySearchstr[searchlength]);
                                 SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
                                 String queryDateFrom=sdf.format(fromDate);
                                Calendar cst = Calendar.getInstance();
                                cst.setTime(fromDate);
                                cst.add(Calendar.DATE, 1);
                                Date toDate=new Date(cst.getTimeInMillis());
                                String queryDateTo=sdf.format(toDate);
//                                eday = eday - 1;
//                                if (iscustomcolumn) {
//                                    al.add(new Long(sday).toString());
//                                    al.add(new Long(eday).toString());
//                                } else {
                                    al.add(queryDateFrom);
                                    al.add(queryDateTo);
//                                    al.add(eday);
//                                }
                            }
                        } else if (StringUtil.equal(jobj1.getString(Constants.xtype), Constants.numberfield) && arraySearchstr[searchlength].contains("To")) {

                            String[] tempParams = arraySearchstr[searchlength].split("To");
                            if (tempParams.length == 2 || tempParams.length == 1) {
                                try {
                                    if (!StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                            al.add((StringUtil.checkForNull(tempParams[0]).trim()));
                                        }
                                    if (tempParams.length == 2 && !StringUtil.isNullOrEmpty(tempParams[1].trim())) {
                                            al.add((StringUtil.checkForNull(tempParams[1]).trim()));
                                        }

                                } catch (Exception ex) {
                                    if (!StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                        al.add((StringUtil.checkForNull(tempParams[0]).trim()));
                                    }
                                    if (tempParams.length == 2 && !StringUtil.isNullOrEmpty(tempParams[1].trim())) {
                                        al.add((StringUtil.checkForNull(tempParams[1]).trim()));
                                    }
                                }
                            }
                        } else {
                            if (iscustomcolumn) {
                                String fieldtype = jobj1.getString(Constants.fieldtype);
                                if (fieldtype.equals(Constants.seven) || fieldtype.equals(Constants.twelve)) {
                                    arraySearchstr[searchlength] = Constants.percent + arraySearchstr[searchlength];
                                }
                            }
                                al.add(Constants.percent + arraySearchstr[searchlength] + Constants.percent);
                                al.add("% " + arraySearchstr[searchlength] + Constants.percent);
                            }
                        }
                    }
                }
            }
        }
    /**
     * 
     * @param Array1
     * @param Array2
     * @return Array1 merged with elements from Array2
     * @throws JSONException 
     */
    public static JSONArray concatJSONArray(JSONArray Array1, JSONArray Array2) throws JSONException {
        for (int i = 0; i < Array2.length(); i++) {
            if(Array2.optJSONObject(i)!= null){
            Array1.put(Array2.optJSONObject(i));
            }
        }
        return Array1;
    }

    public static String getXtypeVal(int a) throws ServiceException, JSONException {
        String xtype = "";
        switch (a) {
            case 1:
                xtype = "textfield";
                break;
            case 2:
                xtype = "numberfield";
                break;
            case 3:
                xtype = "Datefield";
                break;
            case 4:
                xtype = "Combo";
                break;
            case 5:
                xtype = "Timefield";
                break;
            case 6:
                xtype = "Checkbox";
                break;
            case 7:
                xtype = "Multiselect Combo";
                break;
            case 8:
                xtype = "Ref. Combo";
                break;
        }
        return xtype;
    }

    /**
     * Function to round off a double value upto a specific decimal place. e.g.
     * roundDoubleTo(4.45,1) will give 4.5 val - the value to be rounded off pow
     * - the number of places after the decimal upto which val should be rounded
     */
    public static double roundDoubleTo(double val, int pow) {
        double p = (double) Math.pow(10, pow);
        val = val * p;
        double tmp = (double) (Math.round(val));
        return (Double) tmp / p;
    }

    public static double convertTwoPreciFormat(double val) {
        java.text.DecimalFormat decif = new java.text.DecimalFormat();
        decif.setMaximumFractionDigits(2);
        decif.setGroupingUsed(false);
        return Double.valueOf(decif.format(val));
    }

    public static boolean isNullObject(Object s) {
        if (s == null) {
            return true;
        }
        return false;
    }

//    public static Map<String, Object> buildSqlDefaultFieldAdvSearch(JSONArray defaultSearchFieldArray, ArrayList params, String moduleid, ArrayList tableArray, String filterConjuctionCriteria, HibernateTemplate hibernateTemplate) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        try {
//            String conditionSQL = "";
//            String searchJoin = "";
//   
//            String query = "from DefaultHeaderModuleJoinReference where module=?";
//            List<DefaultHeaderModuleJoinReference> refHeader = HibernateUtil.executeQuery(hibernateTemplate, query, moduleid);
//
//            for (int i = 0; i < defaultSearchFieldArray.length(); i++) {
//                JSONObject jsonobj = defaultSearchFieldArray.getJSONObject(i);
//
//                String fieldId = jsonobj.getString("column");
//                String searchText = jsonobj.getString(Constants.searchText).trim();
//
//                query = "from DefaultHeader where id=?";
//                List<DefaultHeader> headerlist = HibernateUtil.executeQuery(hibernateTemplate, query, fieldId);
//                DefaultHeader header = headerlist.get(0);
//
//                String headerTableName = header.getDbTableName();
//                String headercolumnName = header.getDbcolumnname();
//                if (StringUtil.isNullOrEmpty(headerTableName)) {   // Search Field from reference module
//                    headerTableName = header.getReftablename();
//                    headercolumnName = header.getReftabledatacolumn();
//                }
//                /*
//                    Handle HQL search query 
//                */
//                if (moduleid.equalsIgnoreCase("" + Constants.Acc_Make_Payment_ModuleId) || moduleid.equalsIgnoreCase("" + Constants.Acc_Receive_Payment_ModuleId)) {
//                    headercolumnName = getRecordNameOfDefaultheader(header);
//                    headerTableName = headerTableName + "Ref";
//                }
//                String refModule = header.getModule().getId();
//                String xtype=header.getXtype();
//                if (!moduleid.equalsIgnoreCase(refModule)) { //for same module no need to add any of join 
//                    searchJoin += getHeaderReferenceJoin(refHeader, tableArray, refModule);
//                    for (DefaultHeaderModuleJoinReference reference : refHeader) { //used for finding where condition
//                        if (reference.getRefModule().equalsIgnoreCase(refModule)) {
//                            String refTableName = reference.getRefModuleTableName();
//                            conditionSQL=getDefaultHeaderConditionString(conditionSQL,refTableName,headercolumnName,xtype,searchText,filterConjuctionCriteria,params);
//                        }
//                    }
//                } else {
//                    conditionSQL=getDefaultHeaderConditionString(conditionSQL,headerTableName,headercolumnName,xtype,searchText,filterConjuctionCriteria,params);
//                }
//                if (!conditionSQL.equals("")) {         // when conjuction criteria applied for multiple fields then need to append
//                conditionSQL += ")";
//            }
//            }
//            if (!conditionSQL.equals("")) {
//                conditionSQL += ")";
//            }
//            map.put("searchjoin", searchJoin);
//            map.put("condition", conditionSQL);
//
//        } catch (Exception ex) {
//            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return map;
//    }
//   
    public static String getRecordNameOfDefaultheader(DefaultHeader defaultHeader) {
        String recordName = defaultHeader.getRecordname();
        return recordName;
    }
    public static String getHeaderReferenceJoin(List<DefaultHeaderModuleJoinReference> refHeader, ArrayList tableArray, String refModuleID) {
        String joinString = "";
        if (refHeader != null) {
            for (DefaultHeaderModuleJoinReference reference : refHeader) {
                String refModule = reference.getRefModule();
                if (refModule.equalsIgnoreCase(refModuleID)) {
                    if (reference.getParentRefModule() != null && !tableArray.contains(reference.getParentRefModuleTableName())) {
                        joinString = getHeaderReferenceJoin(refHeader, tableArray, reference.getParentRefModule());
                    }
                    if (!tableArray.contains(reference.getRefModuleTableName())) {
                        joinString += " " + reference.getJoinQuery() + " ";
                        tableArray.add(reference.getRefModuleTableName());
                    }
                }
            }
        }
        return joinString;
    }
    public static String getDefaultHeaderConditionString(String conditionSQL, String tableName, String columnName, String xtype, String searchText, String filterConjuctionCriteria, ArrayList params) throws ParseException {
        String ANDOR = conditionSQL.equals("") ? "AND (" : filterConjuctionCriteria;
        String columnAlias = tableName + "." + columnName;
        if (columnName.contains("(") && columnName.contains(")")) {
            columnAlias = columnName;
        }
        if (xtype.equalsIgnoreCase("4") || xtype.equalsIgnoreCase("7")) {//combobox and multi select 
            String[] serchStrings = searchText.split(",");
            for (int j = 0; j < serchStrings.length; j++) {
                if (j == 0) {
                    conditionSQL += ANDOR + " (((" + columnAlias + " LIKE '%" + serchStrings[j] + "%')";
                } else {
                    conditionSQL += " OR" + " (" + columnAlias + " LIKE '%" + serchStrings[j] + "%')";
                }
            }
            conditionSQL += ")";
        } else if (xtype.equalsIgnoreCase("3") || xtype.equalsIgnoreCase("2")) {  // date field
            String[] serchStrings = searchText.split(",");
            serchStrings = searchText.split(",\"");
            for (int searchlength = 0; searchlength < serchStrings.length; searchlength++) {
                if (serchStrings[searchlength].contains("To")) {
                    boolean lessthanSearch = false;
                    boolean greaterthanSearch = false;
                    String[] tempParams = serchStrings[searchlength].split("To");
                    if (tempParams.length == 2) {
                        /*
                         If user search Range or Less than.
                         */
                        if (xtype.equalsIgnoreCase("3")) {
                            if (!StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                Date fromDate = new SimpleDateFormat(Constants.MMMMdyyyy).parse(StringUtil.checkForNull(tempParams[0]).trim());
                                String fromDate1 = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa").format(fromDate);
                                Date from = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa").parse(fromDate1.toString());
                                params.add(from);
                                greaterthanSearch = true;
                            }
                            if (!StringUtil.isNullOrEmpty(tempParams[1].trim())) {
                                Date toDate = new SimpleDateFormat(Constants.MMMMdyyyy).parse(StringUtil.checkForNull(tempParams[1]).trim());
                                String toDate1 = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa").format(toDate);
                                Date to = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa").parse(toDate1.toString());
                                params.add(to);
                                lessthanSearch = true;
                            }
                        } else {
                            if (!StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                double from = Double.parseDouble(StringUtil.checkForNull(tempParams[0]).trim());
                                params.add(from);
                                greaterthanSearch = true;
                            }
                            if (!StringUtil.isNullOrEmpty(tempParams[1].trim())) {
                                double to = Double.parseDouble(StringUtil.checkForNull(tempParams[1]).trim());
                                params.add(to);
                                lessthanSearch = true;
                            }

                        }
                    } else if (tempParams.length == 1) {
                        /*
                         If User Search only Greater than
                         */
                        if (xtype.equalsIgnoreCase("3")) {
                            if (!StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                Date fromDate = new SimpleDateFormat(Constants.MMMMdyyyy).parse(StringUtil.checkForNull(tempParams[0]).trim());
                                String fromDate1 = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa").format(fromDate);
                                Date from = new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa").parse(fromDate1.toString());
                                params.add(from);
                                greaterthanSearch = true;
                            }
                        } else {
                            if (!StringUtil.isNullOrEmpty(tempParams[0].trim())) {
                                double from = Double.parseDouble(StringUtil.checkForNull(tempParams[0]).trim());
                                params.add(from);
                                greaterthanSearch = true;
                            }
                        }

                    }

                    if (lessthanSearch && greaterthanSearch) {
                        conditionSQL += ANDOR + " ((" + columnAlias + " >=? and " + columnAlias + " <=? )";
                    } else if (lessthanSearch) {
                        conditionSQL += ANDOR + " ((" + columnAlias + " <=? )";
                    } else {
                        conditionSQL += ANDOR + " ((" + columnAlias + " >=? )";
                    }

                } else {
                    Date fromDate = new SimpleDateFormat(Constants.MMMMdyyyy).parse(serchStrings[searchlength]);
                    Calendar cst = Calendar.getInstance();
                    cst.setTime(fromDate);
                    long sday = cst.getTimeInMillis();
                    cst.add(Calendar.DATE, 1);
                    long eday = cst.getTimeInMillis();
                    eday = eday - 1;

                    conditionSQL += ANDOR + " ((" + columnAlias + " >=" + sday + "and " + columnName + " <=" + eday + " )";;
                }
            }
        } else {
            conditionSQL += ANDOR + " ((" + columnAlias + " LIKE '%" + searchText + "%') ";
        }
        return conditionSQL;
    }
    public static void seperateCostomAndDefaultSerachJson(JSONObject serachJobj, JSONArray customSearchFieldArray, JSONArray defaultSearchFieldArray) {
        try {
            JSONArray array = serachJobj.getJSONArray(Constants.root);
            int count = array.length();
            for (int i = 0; i < count; i++) {
                JSONObject jobj = array.getJSONObject(i);
                boolean isdefaultfield = jobj.optBoolean("isdefaultfield", false);
                if (isdefaultfield) {
                    defaultSearchFieldArray.put(jobj);
                } else {
                    customSearchFieldArray.put(jobj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static String combineCustomAndDefaultSearch(String searchDefaultFieldSQL, String mySearchFilterString, String filterConjuctionCriteria) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(searchDefaultFieldSQL) && !StringUtil.isNullOrEmpty(mySearchFilterString)) {
            searchDefaultFieldSQL = searchDefaultFieldSQL.replace("AND (", "(");
            mySearchFilterString = mySearchFilterString.replace("and (", "(");
            stringBuilder.append("AND ");
            stringBuilder.append("(");
            stringBuilder.append(searchDefaultFieldSQL);
            stringBuilder.append(filterConjuctionCriteria);
            stringBuilder.append(mySearchFilterString);
            stringBuilder.append(")");
        } else if (!StringUtil.isNullOrEmpty(searchDefaultFieldSQL)) {
            return searchDefaultFieldSQL;
        } else if (!StringUtil.isNullOrEmpty(mySearchFilterString)) {
            return mySearchFilterString;
        }
        return stringBuilder.toString();
    }
    /**
     *
     * @param mySearchFilterString1
     * @param mySearchFilterString2
     * @return combine search query of mySearchFilterString1 &
     * mySearchFilterString2
     * @Desc : used when advance search on two different modules
     */
    public static String combineTwoCustomSearchStrings(String mySearchFilterString1, String mySearchFilterString2) {
        String finalSearchString = "";
        if (!StringUtil.isNullOrEmpty(mySearchFilterString1) && !StringUtil.isNullOrEmpty(mySearchFilterString2)) {
            mySearchFilterString1 = mySearchFilterString1.replace(")))", "");
            mySearchFilterString2 = mySearchFilterString2.replace("and (((", " or ");
            finalSearchString = mySearchFilterString1.concat(mySearchFilterString2);
        } else if (!StringUtil.isNullOrEmpty(mySearchFilterString1)) {
            return mySearchFilterString1;
        } else if (!StringUtil.isNullOrEmpty(mySearchFilterString2)) {
            return mySearchFilterString2;
        }
        return finalSearchString;
    }

    public static String combineCustomSearchStrings(String searchDefaultFieldSQL, String mySearchFilterString, String filterConjuctionCriteria) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!StringUtil.isNullOrEmpty(searchDefaultFieldSQL) && !StringUtil.isNullOrEmpty(mySearchFilterString)) {
            searchDefaultFieldSQL = searchDefaultFieldSQL.replace("AND (", "(");
            mySearchFilterString = mySearchFilterString.replace("AND (", "(");
            stringBuilder.append("AND ");
            stringBuilder.append("(");
            stringBuilder.append(searchDefaultFieldSQL);
            stringBuilder.append(filterConjuctionCriteria);
            stringBuilder.append(mySearchFilterString);
            stringBuilder.append(")");
        } else if (!StringUtil.isNullOrEmpty(searchDefaultFieldSQL)) {
            return searchDefaultFieldSQL;
        } else if (!StringUtil.isNullOrEmpty(mySearchFilterString)) {
            return mySearchFilterString;
        }
        return stringBuilder.toString();
    }

    public static String getUsedInValue(String existing, String purpose) {
        String usedin = existing;
        if (!StringUtil.isNullOrEmpty(usedin)) {
            if (usedin.indexOf(purpose) == -1) {
                usedin += ", " + purpose;
            }
        } else {
            usedin = purpose;
        }
        return usedin;
    }

    public static String toUpperCaseFirstLetterOfString(String str) {
        if (str.length() > 0) {
            String result = str.substring(0, 1).toUpperCase() + str.substring(1);
            return result;
        } else {
            return "";
        }
    }

    public static String getFullName(User user) {
        String fullname = user.getFirstName();
        if (fullname != null && user.getLastName() != null) {
            fullname += " " + user.getLastName();
        }
        if (StringUtil.isNullOrEmpty(user.getFirstName()) && StringUtil.isNullOrEmpty(user.getLastName())) {
            fullname = user.getUserLogin().getUserName();
        }
        return fullname;
    }

    public static String generateNewPassword() {
        return RandomStringUtils.random(8, true, true);
    }

    public static String getSHA1(String inStr) throws ServiceException, UnsupportedEncodingException, NoSuchAlgorithmException {
        String outStr = inStr;
        try {
            byte[] theTextToDigestAsBytes = inStr.getBytes("utf-8");

            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] digest = sha.digest(theTextToDigestAsBytes);

            StringBuffer sb = new StringBuffer();
            for (byte b : digest) {
                String h = Integer.toHexString(b & 0xff);
                if (h.length() == 1) {
                    sb.append("0" + h);
                } else {
                    sb.append(h);
                }
            }
            outStr = sb.toString();
        } catch (UnsupportedEncodingException e) {
            throw ServiceException.FAILURE("Auth.getSHA1", e);
        } catch (NoSuchAlgorithmException e) {
            throw ServiceException.FAILURE("Auth.getSHA1", e);
        }
        return outStr;
    }

    //Convert Request Paramater to JSON Object
    public static JSONObject convertRequestToJsonObject(HttpServletRequest request) throws JSONException, SessionExpiredException {

        JSONObject paramJObj = new JSONObject();
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (companySessionObj != null) {
                paramJObj.put(Constants.browsertz, companySessionObj.getBrowsertz());
                paramJObj.put(Constants.useridKey, companySessionObj.getUserid());
                paramJObj.put(Constants.lid, companySessionObj.getUserid());
                paramJObj.put(Constants.timezoneID, companySessionObj.getTimezoneid());
                paramJObj.put(Constants.timezonedifference, companySessionObj.getTzdiff());
                paramJObj.put(Constants.timezoneid, companySessionObj.getTimezoneid());
                paramJObj.put(Constants.callwith, companySessionObj.getCallwith());
                paramJObj.put(Constants.timeformat, companySessionObj.getTimeformat());
                paramJObj.put(Constants.companyPreferences, companySessionObj.getCompanyPreferences());
                paramJObj.put(Constants.username, companySessionObj.getUsername());
                paramJObj.put(Constants.usermailId, companySessionObj.getUserEmailid());
                paramJObj.put(Constants.userdateformat, companySessionObj.getUserdateformat());
                paramJObj.put(Constants.userfullname, companySessionObj.getUserfullname());
                paramJObj.put(Constants.roleid, companySessionObj.getRoleid());
                paramJObj.put(Constants.dateformatid, companySessionObj.getDateformatid());
                paramJObj.put(Constants.companyKey, companySessionObj.getCompanyid());
                paramJObj.put(Constants.companyname, companySessionObj.getCompany());
                paramJObj.put(Constants.COMPANY_SUBDOMAIN, companySessionObj.getCdomain());
                paramJObj.put(Constants.globalCurrencyKey, companySessionObj.getCurrencyid());
                paramJObj.put(Constants.userSessionId, companySessionObj.getUserSessionId());
                paramJObj.put(Constants.userid, companySessionObj.getUserid());
                paramJObj.put(Constants.PAGE_URL, URLUtil.getPageURL(request, loginpageFull));
                paramJObj.put(Constants.COUNTRY_ID, companySessionObj.getCountryId());
//                if (StringUtil.isNullOrEmpty((String) request.getParameter(Constants.currencyKey))) { // SDP-5707
//                    paramJObj.put(Constants.currencyKey, companySessionObj.getCurrencyid());
//                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.POSOFLAG))) {
                    paramJObj.put(Constants.POSOFLAG, request.getParameter(Constants.POSOFLAG));
                }
                paramJObj.put(Constants.initialized, companySessionObj.getInitialized());
            }
        } else {
            Enumeration<String> sessionAttributes = request.getSession().getAttributeNames();
            while (sessionAttributes.hasMoreElements()) {
                String attribute = sessionAttributes.nextElement();
                paramJObj.put(attribute, request.getSession().getAttribute(attribute));
            }
        }
        int customerpermCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
        paramJObj.put(Constants.PermCode_Customer, customerpermCode);

        if (!StringUtil.isNullOrEmpty(request.getContextPath())) {
            paramJObj.put(Constants.contextPath, request.getContextPath());
        }

        paramJObj.put(Constants.language, RequestContextUtils.getLocale(request).getLanguage() + "_" + RequestContextUtils.getLocale(request).getCountry());
        ServletContext context = request.getSession().getServletContext();
        String crmURL = context.getInitParameter(Constants.crmURL);
        String inventoryURL = context.getInitParameter(Constants.inventoryURL);
        String posURL = context.getInitParameter(Constants.posURL);
        paramJObj.put(Constants.crmURL, crmURL);
        paramJObj.put(Constants.inventoryURL, inventoryURL);
        paramJObj.put(Constants.posURL, posURL);
        paramJObj.put(Constants.reqHeader, getIpAddress(request));
        paramJObj.put(Constants.remoteIPAddress, request.getRemoteAddr());
        
        paramJObj.put(Constants.JRXML_REAL_PATH_KEY,context.getRealPath("jrxml"));    
        paramJObj.put(Constants.REQUEST_URI, request.getRequestURI());
        paramJObj.put(Constants.SERVLET_PATH, request.getServletPath());
        paramJObj.put(Constants.REAL_PATH, request.getRealPath(""));
        
                
        Enumeration<String> attributes = request.getAttributeNames();
        while (attributes.hasMoreElements()) {
            String attribute = attributes.nextElement();
            paramJObj.put(attribute, request.getAttribute(attribute));
        }
        Enumeration<String> parameters = request.getParameterNames();
        while (parameters.hasMoreElements()) {
            String parameter = parameters.nextElement();
            paramJObj.put(parameter, request.getParameter(parameter));
        }
        return paramJObj;
    }
//Convert Request Paramater to Map Object
    public static HashMap convertRequestToMapObject(HttpServletRequest request) throws JSONException, SessionExpiredException {

        HashMap requestParam = new HashMap();
        String subdomain = URLUtil.getDomainName(request);
        if (!StringUtil.isNullOrEmpty(subdomain)) {
            CompanySessionClass companySessionObj = sessionHandlerImpl.getCompanySessionObj(request);
            if (companySessionObj != null) {
                requestParam.put(Constants.browsertz, companySessionObj.getBrowsertz());
                requestParam.put(Constants.useridKey, companySessionObj.getUserid());
                requestParam.put(Constants.lid, companySessionObj.getUserid());
                requestParam.put(Constants.timezoneID, companySessionObj.getTimezoneid());
                requestParam.put(Constants.timezonedifference, companySessionObj.getTzdiff());
                requestParam.put(Constants.timezoneid, companySessionObj.getTimezoneid());
                requestParam.put(Constants.callwith, companySessionObj.getCallwith());
                requestParam.put(Constants.timeformat, companySessionObj.getTimeformat());
                requestParam.put(Constants.companyPreferences, companySessionObj.getCompanyPreferences());
                requestParam.put(Constants.username, companySessionObj.getUsername());
                requestParam.put(Constants.usermailId, companySessionObj.getUserEmailid());
                requestParam.put(Constants.userdateformat, companySessionObj.getUserdateformat());
                requestParam.put(Constants.userfullname, companySessionObj.getUserfullname());
                requestParam.put(Constants.roleid, companySessionObj.getRoleid());
                requestParam.put(Constants.dateformatid, companySessionObj.getDateformatid());
                requestParam.put(Constants.companyKey, companySessionObj.getCompanyid());
                requestParam.put(Constants.companyname, companySessionObj.getCompany());
                requestParam.put(Constants.COMPANY_SUBDOMAIN, companySessionObj.getCdomain());
                requestParam.put(Constants.globalCurrencyKey, companySessionObj.getCurrencyid());
                requestParam.put(Constants.userSessionId, companySessionObj.getUserSessionId());
                requestParam.put(Constants.userid, companySessionObj.getUserid());
                requestParam.put(Constants.COUNTRY_ID, companySessionObj.getCountryId());
//                if (StringUtil.isNullOrEmpty((String) request.getParameter(Constants.currencyKey))) { // SDP-5707
//                    paramJObj.put(Constants.currencyKey, companySessionObj.getCurrencyid());
//                }
                if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.POSOFLAG))) {
                    requestParam.put(Constants.POSOFLAG, request.getParameter(Constants.POSOFLAG));
                }
                requestParam.put(Constants.initialized, companySessionObj.getInitialized());
            }
        } else {
            Enumeration<String> sessionAttributes = request.getSession().getAttributeNames();
            while (sessionAttributes.hasMoreElements()) {
                String attribute = sessionAttributes.nextElement();
                requestParam.put(attribute, request.getSession().getAttribute(attribute));
            }
        }
        int customerpermCode = sessionHandlerImpl.getPerms(request, Constants.CUSTOMER_PERMCODE);
        requestParam.put(Constants.PermCode_Customer, customerpermCode);

        if (!StringUtil.isNullOrEmpty(request.getContextPath())) {
            requestParam.put(Constants.contextPath, request.getContextPath());
        }

        requestParam.put(Constants.language, RequestContextUtils.getLocale(request).getLanguage() + "_" + RequestContextUtils.getLocale(request).getCountry());
        ServletContext context = request.getSession().getServletContext();
        String crmURL = context.getInitParameter(Constants.crmURL);
        String inventoryURL = context.getInitParameter(Constants.inventoryURL);
        requestParam.put(Constants.crmURL, crmURL);
        requestParam.put(Constants.inventoryURL, inventoryURL);
        requestParam.put(Constants.reqHeader, getIpAddress(request));
        requestParam.put(Constants.remoteIPAddress, request.getRemoteAddr());
        
        requestParam.put(Constants.JRXML_REAL_PATH_KEY,context.getRealPath("jrxml"));    
        requestParam.put(Constants.REQUEST_URI, request.getRequestURI());
        requestParam.put(Constants.SERVLET_PATH, request.getServletPath());
        requestParam.put(Constants.REAL_PATH, request.getRealPath(""));
        
                
        Enumeration<String> attributes = request.getAttributeNames();
        while (attributes.hasMoreElements()) {
            String attribute = attributes.nextElement();
            requestParam.put(attribute, request.getAttribute(attribute));
        }
        Enumeration<String> parameters = request.getParameterNames();
        while (parameters.hasMoreElements()) {
            String parameter = parameters.nextElement();
            requestParam.put(parameter, request.getParameter(parameter));
        }
        return requestParam;
    }
    public static JSONObject getErrorResponse(String errorCode, JSONObject jobj, String errorMessage, MessageSource messageSource) {
        JSONObject response = new JSONObject();
        try {

             if (StringUtil.isNullOrEmpty(errorCode)) {
                response.put(Constants.RES_MESSAGE, errorMessage);
            } else if (errorCode.equals(ServiceException.FAILURE)) {
                response.put(Constants.RES_MESSAGE, errorMessage);
                response.put(Constants.RES_ERROR_CODE, errorCode);
            } else {
                String language = Constants.RES_DEF_LANGUAGE;
                if (jobj != null && jobj.has("language") && jobj.getString("language") != null) {
                    language = jobj.getString("language");
                }
                Object[] paramValues = null;
                if (errorCode != null && errorCode.contains("{") && errorCode.contains("}")) {
                    String paramValue = errorCode.substring(errorCode.indexOf("{") + 1, errorCode.indexOf("}"));
                    errorCode = errorCode.substring(0, errorCode.indexOf("{"));
                    List<String> params = new ArrayList<String>();
                    for (String param : paramValue.split(";")) {
                        params.add(param);
                    }
                    paramValues = params.toArray();
                }
                response.put(Constants.RES_MESSAGE, messageSource.getMessage(errorCode, paramValues, Locale.forLanguageTag(language)));
                response.put(Constants.RES_ERROR_CODE, errorCode);
            }
            response.put(Constants.RES_success, false);
          
        } catch (JSONException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    
    public static String getIpAddress(HttpServletRequest request) {
        String ipaddr = "";
        if (StringUtil.isNullOrEmpty(request.getHeader("x-real-ip"))) {
            ipaddr = request.getRemoteAddr();
        } else {
            ipaddr = request.getHeader("x-real-ip");
        }
        return ipaddr;
    }

    public static String getJsonStringFromInputstream(InputStream incomingData) {
        String result = "";
        StringBuilder crunchifyBuilder = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(incomingData));
            String line = null;
            while ((line = in.readLine()) != null) {
                crunchifyBuilder.append(line);
            }
        } catch (Exception e) {
            System.out.println("Error Parsing: - ");
        }
        result = crunchifyBuilder.toString();
        return result;
    }

    public static Locale getLocale(String languageValue) {
        String language, country = null;
        Locale locale = null;
        if (!isNullOrEmpty(languageValue)) {
            if (languageValue.contains("_")) {
                String[] tokens = languageValue.split("_");
                language = tokens[0];
                country = tokens[1];
                locale = new Locale(language, country);
            } else {
                locale = new Locale(languageValue);
            }
        }
        return locale;
    }

    public static Map<String, String> jsonStringtoMap(String jsonString) throws JSONException {
        Map<String, String> resultMap = new HashMap<String, String>();
        JSONObject jsonObject = new JSONObject(jsonString);
        Iterator<String> keyset = jsonObject.keys();
        while (keyset.hasNext()) {
            String key = (String) keyset.next();
            String value = jsonObject.getString(key);
            if (!StringUtil.isNullOrEmpty(key)) {
                resultMap.put(key, StringUtil.isNullOrEmpty(value) ? "" : value);
            }
        }
        return resultMap;
    }

    public static String getMySearchFilterString(Map<String, Object> requestParams, ArrayList paramslist) {
        String mySearchFilterString = "";
        try {
            String appendCase = "and";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey(Constants.Filter_Criteria) && requestParams.get(Constants.Filter_Criteria) != null) {
                if (requestParams.get(Constants.Filter_Criteria).toString().trim().equalsIgnoreCase(Constants.or.trim())) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            String Searchjson = "";
            String searchDefaultFieldSQL = "";
            if (requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null) {
                Searchjson = requestParams.get(Constants.Acc_Search_Json).toString();
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    JSONObject serachJobj = new JSONObject(Searchjson);
                    JSONArray customSearchFieldArray = new JSONArray();
                    JSONArray defaultSearchFieldArray = new JSONArray();
                    StringUtil.seperateCostomAndDefaultSerachJson(serachJobj, customSearchFieldArray, defaultSearchFieldArray);
                    if (customSearchFieldArray.length() > 0) {
                        requestParams.put(Constants.Searchjson, Searchjson);
                        requestParams.put(Constants.appendCase, appendCase);
                        requestParams.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                        mySearchFilterString = String.valueOf(StringUtil.getAdvanceSearchString(requestParams, false).get(Constants.myResult));
                        StringUtil.insertParamAdvanceSearchString1(paramslist, Searchjson);
                    }
                    mySearchFilterString = StringUtil.combineCustomAndDefaultSearch(searchDefaultFieldSQL, mySearchFilterString, filterConjuctionCriteria);
                }
            }
        } catch (JSONException | ParseException ex) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        return mySearchFilterString;
    }

    public static String appendParametersToURL(String url, Map<String, String> parameters) {
        String requestDataParameters = "";
        try {
            for (Map.Entry<String, String> parameter : parameters.entrySet()) {
                requestDataParameters += "&" + parameter.getKey() + "=" + URLEncoder.encode(parameter.getValue(), "UTF-8");
            }
            if (!url.contains("?") && !isNullOrEmpty(requestDataParameters)) {
                requestDataParameters = "?" + requestDataParameters.substring(1);
            }
        } catch (Exception e) {
            Logger.getLogger(StringUtil.class.getName()).log(Level.SEVERE, null, e);
            requestDataParameters = "";
        }
        return url + requestDataParameters;
    }

    /* Function is used to decode string contains special character*/
    public static String decodeString(String string) {

        String data = string;

        try {

            if (data != null) {

                data =DecodeText(data);
            }
        } catch (Exception e) {

            e.printStackTrace();

        }

        return data;

    }

    public static String replaceUsedIn(String CurrentUsedIn, String replacing) {
        CurrentUsedIn = CurrentUsedIn.replace(replacing, "");
        CurrentUsedIn = CurrentUsedIn.trim();
        if (CurrentUsedIn.startsWith(",")) {
            CurrentUsedIn = CurrentUsedIn.substring(1);
        }
        if (CurrentUsedIn.endsWith(",")) {
            CurrentUsedIn = CurrentUsedIn.substring(0, CurrentUsedIn.length() - 1);
        }
        return CurrentUsedIn;
    }

    public static String DecodeText(String stringToDecode)
    {
        String retunrString="";
        if(!isNullOrEmpty(stringToDecode)){
            try {
                retunrString = URLDecoder.decode(stringToDecode, Constants.ENCODING);
            } catch (Exception ex) {
                retunrString=stringToDecode;
            }
        }
        return retunrString;
    }
    
    public static String getModuleName(String moduleidStr) {
        String moduleName = " ";
        try {
            int moduleid = Integer.parseInt(moduleidStr);
            switch (moduleid) {
                case (Constants.Acc_Invoice_ModuleId):
                    moduleName = "Invoice/Cash Sales";
                    break;
                case (Constants.Acc_BillingInvoice_ModuleId):
                    moduleName = "Billing Invoice";
                    break;
                case (Constants.Acc_Cash_Sales_ModuleId):
                    moduleName = "Cash Sales";
                    break;
                case (Constants.Acc_Billing_Cash_Sales_ModuleId):
                    moduleName = "Billing Cash Sales";
                    break;
                case (Constants.Acc_Vendor_Invoice_ModuleId):
                    moduleName = "Purchase Invoice/Cash Purchase";
                    break;
                case (Constants.Acc_Debit_Note_ModuleId):
                    moduleName = "Debit Note";
                    break;
                case (Constants.Acc_Credit_Note_ModuleId):
                    moduleName = "Credit Note";
                    break;
                case (Constants.Acc_Make_Payment_ModuleId):
                    moduleName = "Make Payment";
                    break;
                case (Constants.Acc_Receive_Payment_ModuleId):
                    moduleName = "Receive Payment";
                    break;
                case (Constants.Acc_GENERAL_LEDGER_ModuleId):
                    moduleName = "Journal Entry";
                    break;
                case (Constants.Acc_Product_Master_ModuleId):
                    moduleName = "Products & Services";
                    break;
                case (Constants.Acc_Purchase_Order_ModuleId):
                    moduleName = "Purchase Order";
                    break;
                case (Constants.Acc_Sales_Order_ModuleId):
                    moduleName = "Sales Order";
                    break;
                case (Constants.Acc_Customer_Quotation_ModuleId):
                    moduleName = "Customer Quotation";
                    break;
                case (Constants.Acc_Vendor_Quotation_ModuleId):
                    moduleName = "Vendor Quotation";
                    break;
                case (Constants.Acc_Delivery_Order_ModuleId):
                    moduleName = "Delivery Order";
                    break;
                case (Constants.Acc_Goods_Receipt_ModuleId):
                    moduleName = "Goods Receipt Order";
                    break;
                case (Constants.Acc_Sales_Return_ModuleId):
                    moduleName = "Sales Return";
                    break;
                case (Constants.Acc_Purchase_Return_ModuleId):
                    moduleName = "Purchase Return";
                    break;
                case (Constants.Account_Statement_ModuleId):
                    moduleName = "GL Accounts";
                    break;
                case (Constants.Acc_Vendor_ModuleId):
                    moduleName = "Vendor";
                    break;
                case (Constants.Acc_Customer_ModuleId):
                    moduleName = "Customer";
                    break;
                case (Constants.Acc_Purchase_Requisition_ModuleId):
                    moduleName = "Purchase Requisition";
                    break;
                case (Constants.Acc_Lease_Order_ModuleId):
                    moduleName = "Lease Order";
                    break;
                case (Constants.Acc_Contract_Order_ModuleId):
                    moduleName = "Contract Order";
                    break;
                case (Constants.Acc_RFQ_ModuleId):
                    moduleName = "Request For Quotation";
                    break;
                case (Constants.Acc_FixedAssets_DisposalInvoice_ModuleId):
                    moduleName = "FA Disposal Invoice";
                    break;
                case (Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId):
                    moduleName = "FA Purchase Invoice";
                    break;
                case (Constants.Acc_FixedAssets_GoodsReceipt_ModuleId):
                    moduleName = "FA Goods Receipt";
                    break;
                case (Constants.Acc_FixedAssets_DeliveryOrder_ModuleId):
                    moduleName = "FA Delivery Order";
                    break;
                case (Constants.Acc_FixedAssets_AssetsGroups_ModuleId):
                    moduleName = "FA Assets Group";
                    break;
                case (Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId):
                    moduleName = "FA Purchase Requisition";
                    break;
                case (Constants.Acc_FixedAssets_RFQ_ModuleId):
                    moduleName = "FA RFQ";
                    break;
                case (Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId):
                    moduleName = "FA Vendor Quotation";
                    break;
                case (Constants.Acc_FixedAssets_Purchase_Order_ModuleId):
                    moduleName = "FA Purchase Order";
                    break;
                case (Constants.SerialWindow_ModuleId):
                    moduleName = "Serial Window";
                    break;
                case (Constants.Acc_ConsignmentRequest_ModuleId):
                    moduleName = "Consignment Request";
                    break;
                case (Constants.Acc_ConsignmentDeliveryOrder_ModuleId):
                    moduleName = "Consignment DeliveryOrder";
                    break;
                case (Constants.Acc_ConsignmentInvoice_ModuleId):
                    moduleName = "Consignment Invoice";
                    break;
                case (Constants.Acc_ConsignmentSalesReturn_ModuleId):
                    moduleName = "Consignment SalesReturn";
                    break;
                case (Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId):
                    moduleName = "Consignment GoodsReceiptOrder";
                    break;
                case (Constants.Acc_Consignment_GoodsReceipt_ModuleId):
                    moduleName = "Consignment GoodsReceipt";
                    break;
                case (Constants.Acc_ConsignmentPurchaseReturn_ModuleId):
                    moduleName = "Consignment PurchaseReturn";
                    break;
                case (Constants.Acc_ConsignmentVendorRequest_ModuleId):
                    moduleName = "Consignment VendorRequest";
                    break;
                case (Constants.Inventory_ModuleId):
                    moduleName = "Inventory Window";
                    break;
                case (Constants.LEASE_INVOICE_MODULEID):
                    moduleName = "Lease Invoice";
                    break;
                case (Constants.Acc_Lease_Contract):
                    moduleName = "Lease Contract";
                    break;
                case (Constants.Acc_Lease_Quotation):
                    moduleName = "Lease Quotation";
                    break;
                case (Constants.Acc_Lease_DO):
                    moduleName = "Lease Delivery Order";
                    break;
                case (Constants.Acc_Lease_Return):
                    moduleName = "Lease Return";
                    break;
                case (Constants.Acc_FixedAssets_Purchase_Return_ModuleId):
                    moduleName = "FA Purchase Return";
                    break;
                case (Constants.Acc_FixedAssets_Sales_Return_ModuleId):
                    moduleName = "FA Sales Return";
                    break;
                case (Constants.Acc_FixedAssets_Details_ModuleId):
                    moduleName = "FA Details";
                    break;
                case (Constants.Inventory_Stock_Adjustment_ModuleId):
                    moduleName = "Stock Adjustment";
                    break;
                case (Constants.Acc_Stock_Request_ModuleId):
                    moduleName = "Stock Request";
                    break;
                case (Constants.Acc_InterStore_ModuleId):
                    moduleName = "Inter Store Transfer";
                    break;
                case (Constants.Acc_InterLocation_ModuleId):
                    moduleName = "Inter Location Transfer";
                    break;
                case (Constants.Labour_Master):
                    moduleName = "Labour";
                    break;
                case (Constants.MRP_WORK_CENTRE_MODULEID):
                    moduleName = "Work Center Master";
                    break;
                case (Constants.MRP_Machine_Management_ModuleId):
                    moduleName = "Machine Master";
                    break;
                case (Constants.MRP_WORK_ORDER_MODULEID):
                    moduleName = "Work Order";
                    break;
                case (Constants.VENDOR_JOB_WORKORDER_MODULEID):
                    moduleName = "Vendor Job Work Order";
                    break;
                case (Constants.MRP_Contract):
                    moduleName = "Master Contract";
                    break;
                case (Constants.MRP_RouteCode):
                    moduleName = "Routing Template";
                    break;
                case (Constants.MRP_JOB_WORK_MODULEID):
                    moduleName = "Job Work";
                    break;
                case (Constants.Acc_CycleCount_ModuleId):
                    moduleName = "Cycle Count";
                    break;
            }

        } catch (NumberFormatException e) {
            if (moduleidStr.equals(Constants.CUSTOMER_MODULE_UUID)) {
                moduleName = "Customer";
            }
        } finally {
            return moduleName;
        }
    }
    
    /** 
    * Returns true if found date format as MMMM d, yyyy. Returns false if found other date Format.
    * @param checkDate input to be validated.
    * @return return true for date format MMMM d, yyyy, otherwise, return false.
    */
    public static boolean isValidDateOnlyFormat(String checkDate){
        try{
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            dateFormat.parse(checkDate);
            return true;
        }catch(ParseException | SessionExpiredException ex){
            return false;
        }
    }
    /** 
    * Is used to validate date as in case when user select yyyy-MM-dd format in import and the import file contains date in yy-MM-dd format than parse exception is not thrown instead 00 are postfixed in year
    * for example the input file contains date as 17-12-31 than in Db it is stored as 0017-12-31 so validating date on the basis of length of format and the length of date in case format is yyyy-MM-dd
    * @param params input to be validated.
    * @return return true if date is valid or when passed format is not available in case
    */
    public static boolean isDateMatchingWithDateFormat(JSONObject params) {
        boolean isDateMatching = false;
        try {
            String date = params.optString("date");
            String format = params.optString("format");
            if (!isNullOrEmpty(date)) {
                switch (format) {
                    case Constants.yyyyMMdd:
                        if (date.length() == format.length()) {
                            isDateMatching = true;
                        }
                        break;
                    default:
                        isDateMatching = true;
                }

            }
        } catch (Exception ex) {
            isDateMatching = false;
            Logger.getLogger(StringUtil.class.getName() + ":isDateMatchingWithDateFormat").log(Level.SEVERE, null, ex);
        }
        return isDateMatching;
    }
    
    public static boolean isMalaysianPurchaseTax(String taxName) {
        boolean isPurchaseTax = false;
        if (taxName.equals(Constants.MALAYSIAN_GST_TX_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_IM_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_IS_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_BL_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_NR_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_ZP_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_EP_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_OP_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_TX_IES_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_TX_ES_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_TX_RE_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_GP_N43_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_AJP_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_TX_CG_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_RP_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_TX_FRS_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_TX_NC_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_NP_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_IM_CG_CODE) || taxName.equals(Constants.MALAYSIAN_GST_TX0_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_TX_CG0_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_TX_ES0_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_TX_IES0_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_TX_RE0_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_IM0_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_IM_CG0_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_BL0_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_TX_NC0_TAX_CODE)
                || taxName.equals(Constants.MALAYSIAN_GST_AJP0_TAX_CODE) || taxName.equals(Constants.MALAYSIAN_GST_TX_FRS0_TAX_CODE)) {
            isPurchaseTax = true;
        }
        return isPurchaseTax;
    }
    
    
    public static boolean isSequenceFormatValid(String SequenceNumber) {
        Boolean Match;
        String pattern = "^[A-Za-z0-9/-]*$";
        Pattern r = Pattern.compile(pattern);    // Create a Pattern object
        Matcher m = r.matcher(SequenceNumber); // Now create matcher object.
        Match = m.matches();
        return Match;
    }
    /**
     * Method to get the check digit for the gstin (with checksum digit)
     *
     * @param gstin
     * @return : status i.e. Valid or Invalid Algorithm : 
     * Step 1- Compute place value for digit i.e.digit 0 & its place value 0,digit 1 & its place value  1,digit A its place value 10,digit Z its place value 36 respectively * Factor (For "Odd" place factor is 1,For even place factor is 2) 
     * Step 2- Divide by 36 calculate quotient and find remainder 
     * Step 3- Repeat step 2 for all digits and find value of (Quotient + Remainder) 
     * Step 4- Divide addition by 36 and find remainder Step 5- Subtract remainder from 36 and obtain the place value of "Check-sum" digit 
     * Step 6- Find and assign appropriate value for obtained place value of "Check-sum" digit
     */
    public static boolean isGSTINValid(String gstin) {
        int factor = 2;
        int sum = 0;
        int checkCodePoint = 0;
        char[] cpChars = null;
        char[] inputChars;
        String gstinWOCheckDigit = null;
        String finalGstin = null;
        boolean isValidFormat = true;
        boolean isValidGSTIN = true;
        
        if (!gstin.matches(Constants.GSTINFORMAT_REGEX)) {
            isValidFormat = false;
        }
        if (gstin != null) {
            gstinWOCheckDigit = gstin.substring(0, gstin.length() - 1);
            cpChars = Constants.GSTN_CODEPOINT_CHARS.toCharArray();
            inputChars = gstinWOCheckDigit.trim().toUpperCase().toCharArray();
            int mod = cpChars.length;
            for (int i = inputChars.length - 1; i >= 0; i--) {
                int codePoint = -1;
                for (int j = 0; j < cpChars.length; j++) {
                    if (cpChars[j] == inputChars[i]) {
                        codePoint = j;
                    }
                }
                int digit = factor * codePoint;
                factor = (factor == 2) ? 1 : 2;
                digit = (digit / mod) + (digit % mod);
                sum += digit;
            }
            checkCodePoint = (mod - (sum % mod)) % mod;
            finalGstin = gstinWOCheckDigit + cpChars[checkCodePoint];
        }
        if (!(gstin.equals(finalGstin) && isValidFormat)) {
            isValidGSTIN = false;
        }
        return isValidGSTIN;
    }
    
    public static JSONArray addJSONObjectToPosInArray(int pos, JSONObject jsonObj, JSONArray jsonArr) throws JSONException {
        for (int i = jsonArr.length(); i > pos; i--) {
            jsonArr.put(i, jsonArr.get(i - 1));
}
        jsonArr.put(pos, jsonObj);
        return  jsonArr;
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != null) {
            retMap = toMap(json);
        }
        return retMap;
    }
    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();
        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }
    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }
     /**
     * Excel Amount formating for Export files
     * Note :  "Region and Language" setting need to be proper for View format in 
     * any country specific amount format column in excel
     * This setting available in Each OS(windows/Linux (Centos 6,7)/ etc)
     * @param wb
     * @param companyid
     * @return 
     */
    public static HSSFCellStyle getCommaSepratedAmountStyle(HSSFWorkbook wb, String companyid) {
        HSSFCellStyle cellStyleAmount = wb.createCellStyle();
        String str = authHandler.getCompleteDFStringForAmount("#,##0.", companyid);
        cellStyleAmount.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(str));
        return cellStyleAmount;
    }
    /**
     * Escaping Arithmetic Operators from given input string.
     * Usages: String.replaceAll(String regex, String replacement).
     * If we want to consider first argument as String then use this method.
     * @param inputString
     * @return String
     */
    public static String escapeArithmeticOperators (String inputString) {
        StringBuilder sb = null;
        if (!isNullOrEmpty(inputString)) {
            String replacement;
            sb = new StringBuilder();
            for (int i = 0; i < inputString.length(); i++) {
                char c = inputString.charAt(i);
                switch (c) {
                    case '+':
                        replacement = "\\+";
                        break;
                    case '-':
                        replacement = "\\-";
                        break;
                    case '*':
                        replacement = "\\*";
                        break;
                    case '/':
                        replacement = "\\/";
                        break;
                    case '(':
                        replacement = "\\(";
                        break;
                    case ')':
                        replacement = "\\)";
                        break;
                    default:
                        sb.append(c);
                        continue;
                }
                sb.append(replacement);
            }
        }
        return sb == null ? inputString : sb.toString();
    }
}
    
class ValueComparator implements Comparator {

    Map base;

    public ValueComparator(Map base) {
        this.base = base;
    }

    public int compare(Object a, Object b) {

        if ((Double) base.get(a) < (Double) base.get(b)) {
            return 1;
        } else if ((Double) base.get(a) == (Double) base.get(b)) {
            return 0;
        } else {
            return -1;
        }
    }
}
