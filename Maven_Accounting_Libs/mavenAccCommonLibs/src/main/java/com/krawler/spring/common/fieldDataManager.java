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
package com.krawler.spring.common;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import com.krawler.common.admin.*;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.util.FieldConstants;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import org.jsoup.Jsoup;

/**
 *
 * @author krawler
 */
public class fieldDataManager {

    private fieldDataManagerDAO fieldDataManagerDAOobj;
    private fieldManagerDAO fieldManagerDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;

    public void setFieldDataManagerDAO(fieldDataManagerDAO fieldDataManagerDAOobj) {
        this.fieldDataManagerDAOobj = fieldDataManagerDAOobj;
    }

    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public String getMultiSelectColData(String id) {
        String data = "";
        if (!StringUtil.isNullOrEmpty(id) && id.length() > 1) {
            String[] mids = id.split(Constants.Custom_Column_Sep);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            for (int i = 0; i < mids.length; i++) {
                requestParams.clear();
                requestParams.put("dataid", mids[i]);
                //   data = data + fieldManagerDAOobj.getFieldComboDatadata(requestParams) + ",";
            }
            if (!StringUtil.isNullOrEmpty(data) && data.length() > 1) {
                data = data.substring(0, data.length() - 1);
            }
        }
        return data;
    }

    public KwlReturnObject setCustomData(HashMap<String, Object> customrequestParams) throws JSONException, ServiceException {
        KwlReturnObject result = null;
        boolean atleatonefield = false;
        JSONArray jarray = (JSONArray) customrequestParams.get("customarray");
        boolean allowautonomap = customrequestParams.containsKey("allowautonomap") ? true : false;
        String modulename = (String) customrequestParams.get("modulename");
        String moduleprimarykey = (String) customrequestParams.get("moduleprimarykey");
        String modulerecid = (String) customrequestParams.get("modulerecid");
        String customdataclasspath = (String) customrequestParams.get("customdataclasspath");
        String companyid = (String) customrequestParams.get("companyid");
        String moduleid = String.valueOf(customrequestParams.get("moduleid"));

        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(modulename, modulerecid);
        requestParams.put(moduleprimarykey, modulerecid);
        requestParams.put("Company", companyid);
        requestParams.put("ModuleId", moduleid);
        if (customrequestParams.containsKey("recdetailId") && customrequestParams.get("recdetailId") != null) {
            requestParams.put("RecdetailId", customrequestParams.get("recdetailId"));
        }
        if (customrequestParams.containsKey("productId") && customrequestParams.get("productId") != null) {
            requestParams.put("ProductId", customrequestParams.get("productId"));
        }
        if (jarray != null) {
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jobj = jarray.getJSONObject(i);
                if (jobj.has(Constants.Acc_custom_field)) {
                    String fieldname = jobj.optString(Constants.Acc_custom_field, "");
                    String fielddbname = jobj.optString(fieldname, "");
                    String fieldValue = jobj.optString(fielddbname, "");
                    Date actualDate=null;
                    atleatonefield = true;
                    fielddbname = fielddbname.replace("c", "C");
                    Integer xtype = Integer.parseInt(jobj.getString("xtype"));
                    if (xtype == 9 && !allowautonomap) {
                        continue; // if autonumber then continue;
                    }
                    if (!StringUtil.isNullOrEmpty(fieldValue) && !StringUtil.isNullOrEmpty(fieldValue.trim()) && !fieldValue.equalsIgnoreCase(Constants.field_data_undefined) && !fieldValue.equalsIgnoreCase(Constants.Custom_Column_Default_value)) {
                        if (xtype == 5 && (fieldValue.contains("Hrs") || fieldValue.contains("hrs"))) {
                            DateFormat sdf = new SimpleDateFormat("hhmm 'Hrs'");
                            DateFormat sdf1 = new SimpleDateFormat("hhmm 'hrs'");
                            DateFormat sdf2 = new SimpleDateFormat("hh:mm a");
                            String temp = "";
                            try {
                                temp = sdf2.format(sdf.parse(fieldValue));
                            } catch (ParseException ex) {
                                try {
                                    temp = sdf2.format(sdf1.parse(fieldValue));
                                } catch (ParseException ex1) {
                                    temp = fieldValue;
                                }
                            }
                            fieldValue = temp;
                        }
                        if (xtype == 3) {//Date Field
                            String temp = "";

                            try {
                           
                                DateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
                                actualDate = sdf.parse(fieldValue);
                                //temp = ((Long) sdf.parse(fieldValue).getTime()).toString();
                            } catch (ParseException ex) {
                                try {
                                    //Long longDate = Long.parseLong(fieldValue);
                                    actualDate = new Date(Long.parseLong(fieldValue));
                                } catch(Exception e)  {
                                    actualDate = null;
                                }
                                
                            } 

                            //fieldValue = temp;
                            requestParams.put(fielddbname, actualDate);//Initially fieldvalue was Passed as it is
                        }else{
                                                                                                
                            requestParams.put(fielddbname, fieldValue);
                        }
                        
                        if (xtype == 7) {
                            String reffielddbname = jobj.getString("refcolumn_name");
                            if (!StringUtil.isNullOrEmpty(reffielddbname)) {
                                requestParams.put(reffielddbname, fieldValue.split(Constants.Custom_Column_Sep)[0]);
                            }
                        }
                    } else {
//                        if (xtype == 7 || xtype == 8 || xtype == 4 || xtype == 3) {
//                            requestParams.put(fielddbname, null);
                            if (xtype == 7) {
                                String reffielddbname = jobj.getString("refcolumn_name");
                                requestParams.put(reffielddbname, "");
                            }
//                        } else {
                         if (xtype == 3) {
                            requestParams.put(fielddbname, null);
                        } else {
                            requestParams.put(fielddbname, "");
//                        }
                        }
                }
            }
        }
        }
        if (atleatonefield) {
            requestParams.put("customdataclasspath", customdataclasspath);
            requestParams.put("moduleprimarykey", moduleprimarykey);
            result = fieldDataManagerDAOobj.setCustomData(requestParams);
        }

        return result;
    }
 
    /*
     * This method Sets Product Specific Custom Field Values.
     */
    public void setRichTextAreaForProduct(HashMap<String, Object> paramJobj) throws ServiceException, JSONException {

        JSONArray jarray = new JSONArray();
        String productId="",companyid="";
        if (paramJobj.containsKey("customarray") && paramJobj.get("customarray") != null) {
            jarray = (JSONArray) paramJobj.get("customarray");
        }
        if (paramJobj.containsKey("productIdForRichRext") && paramJobj.get("productIdForRichRext") != null) {
            productId = String.valueOf(paramJobj.get("productIdForRichRext"));
        }
        if (paramJobj.containsKey("companyid") && paramJobj.get("companyid") != null) {
            companyid = String.valueOf(paramJobj.get("companyid"));
        }
        if (jarray != null) {
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jobj = jarray.getJSONObject(i);
                if (jobj.has(Constants.RichTextFieldChanged) && !jobj.optBoolean(Constants.RichTextFieldChanged)) {
                    
                    String fieldname = jobj.optString(Constants.Acc_custom_field, "");
                    String fielddbname = jobj.optString(fieldname, "");
                    String fieldValue = jobj.optString(fielddbname, "");                    
                    if (StringUtil.isNullOrEmpty(fieldValue)) {
                        HashMap<String, Object> richTextParams=new HashMap<>();
                        richTextParams.put("fieldname", fieldname);                        
                        richTextParams.put("productId", productId);
                        richTextParams.put("companyid", companyid);
                        fieldValue=getRichTextAreaForProduct(richTextParams);
                        jarray.getJSONObject(i).put(fielddbname, fieldValue);
                    }
                }
            }
        }
    }
    /*
     * This Method Returns Value for Rich Text Area for Product
     */
    public String getRichTextAreaForProduct(HashMap<String, Object> paramJobj) throws ServiceException {    
        String fieldName = "",fieldValue="";
        String msg = "",productid="";                                       
        int colNum = 0;
        if (paramJobj.containsKey("fieldname") && paramJobj.get("fieldname") != null) {
                fieldName = String.valueOf(paramJobj.get("fieldname")).replace("Custom_", "");
            }
            if (paramJobj.containsKey("productId") && paramJobj.get("productId") != null) {
                productid = String.valueOf(paramJobj.get("productId"));
            }
            String companyid = String.valueOf(paramJobj.get("companyid"));
            /*
             * First retrive column number for field.
             */
            colNum = fieldManagerDAOobj.getColumnFromFieldParams(fieldName, companyid, Constants.Acc_Product_Master_ModuleId, 0);
            if (colNum > 0) {
                /*
                 * Fetch Value for field against product
                 */                
                String Hql = "select col"+colNum+" from AccProductCustomData where productId=? and companyid=? ";
                List list = fieldDataManagerDAOobj.executeQuery(Hql, new Object[]{productid, companyid});
                if (list.size() > 0) {
                    for (Object str : list) {
                        fieldValue = String.valueOf(str);
                    }
                }                
            }
        return fieldValue;
    }
    public JSONArray createJSONArrForCustomFieldValueFromOtherSource(HashMap<String, Object> customrequestParams) throws JSONException, ServiceException {

        JSONArray jarray = new JSONArray(customrequestParams.get("customarray").toString());
        String companyid = (String) customrequestParams.get("companyid");
        int moduleid = Integer.parseInt(String.valueOf(customrequestParams.get("moduleid")));
        String mapWithFieldType = String.valueOf(customrequestParams.get("mapWithFieldType"));
        JSONArray resultJSONArray = new JSONArray();
        HashMap<String, JSONObject> hashfield = new HashMap<String, JSONObject>();

        String Hql = "select fp.fieldlabel,fp.fieldname,fp.refcolnum,fp.fieldtype,fp.colnum,fp.mapwithtype,fp.moduleid from FieldParams fp  where fp.company.companyID=? and fp.moduleid=? and fp.mapwithtype in (" + mapWithFieldType + ")";
        List list = fieldDataManagerDAOobj.executeQuery(Hql, new Object[]{companyid, moduleid});
        Iterator ite = list.iterator();
        while (ite.hasNext()) {
            Object[] row = (Object[]) ite.next();
            JSONObject FieldParams = new JSONObject();
            FieldParams.put("fieldlabel", (String) row[0]);
            FieldParams.put("fieldname", (String) row[1]);
            FieldParams.put("refcolnum", (Integer) row[2]);
            FieldParams.put("fieldtype", (Integer) row[3]);
            FieldParams.put("colnum", (Integer) row[4]);
            FieldParams.put("moduleid", (Integer) row[6]);
            hashfield.put(String.valueOf(row[5]), FieldParams);
        }
        for (int i = 0; i < jarray.length(); i++) {
            JSONObject jobj = jarray.getJSONObject(i);
            JSONObject resultjobj = new JSONObject();
            String fieldname = jobj.getString(Constants.Acc_custom_field);
            String fieldvalue = jobj.getString(Constants.Acc_custom_field_value);
            String mapwithtype = jobj.getString(Constants.Acc_custom_field_mapwithtype);
            if (hashfield.containsKey(mapwithtype)) {
                JSONObject jFieldParams = hashfield.get(mapwithtype);
                resultjobj.put("refcolumn_name", "Col" + jFieldParams.getInt("refcolnum"));
                resultjobj.put("fieldname", jFieldParams.getString("fieldname"));
                resultjobj.put("xtype", jFieldParams.getInt("fieldtype"));
                resultjobj.put("Col" + jFieldParams.getInt("colnum"), fieldvalue + "-" + jFieldParams.getInt("moduleid"));
                resultjobj.put(jFieldParams.getString("fieldname"), "Col" + jFieldParams.getInt("colnum"));
                resultJSONArray.put(resultjobj);
            }
        }

        return resultJSONArray;
    }

    public JSONObject applyColumnFormulae(String companyid, String currencyid, JSONObject tmpObj, String moduleid, String modName) throws ServiceException, JSONException {
        String fieldname = "";
        try {
            /*
             * Some tested formulaes - 1. revenue+price - All module fields 2.
             * ((revenue+price)*NumField1)+NumField1 - Module + custome fields
             * 3. NumField1/NumField3 - All custom fields 4.
             * (NumField1+NumField3)+((#~100#+#250#)*#3#) - custom fields +
             * constants
             */

            String operatorRegex = "[\\+\\-\\*\\/]"; // Regex to get find the operators in the formulae.
            String bracketRegex = "[\\(\\)]"; // Regex to get find the operators in the formulae.
            String numberRegex = "^\\#\\~?([0-9]*|\\d*\\.\\d{1}?\\d*)\\#$";
            /**
             * For constant numbers 1. Use format : #no. value# 2. Negative
             * Constants : #~ No. Value# 3. #(No. Value)# : will not work.
             * Instead use: (#No. Value#)
             */
            String operandArr[] = null;
            String calStr = "";
            String Hql = "select formulae,fieldname from CustomColumnFormulae where companyid.companyID=? and moduleid=? ";
            List list = fieldDataManagerDAOobj.executeQuery(Hql, new Object[]{companyid, moduleid});
            Iterator ite = list.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                String formulae = (String) row[0];
                fieldname = (String) row[1];

                Object invoker = null;
                String primaryKey = "";
                java.lang.reflect.Method objMethod;
                calStr = formulae.replaceAll(operatorRegex, ",");
                calStr = calStr.replaceAll(bracketRegex, "");
                operandArr = calStr.split(",");
                Class cl = Class.forName("com.krawler.crm.database.tables.Crm" + modName + "");
                if (modName.equals("Lead")) {
                    primaryKey = "leadid";
                } else if (modName.equals("Contact")) {
                    primaryKey = "contactid";
                } else if (modName.equals("Product")) {
                    primaryKey = "productid";
                } else if (modName.equals("Account")) {
                    primaryKey = "accountid";
                } else if (modName.equals("Opportunity")) {
                    primaryKey = "oppid";
                } else if (modName.equals("Case")) {
                    primaryKey = "caseid";
                }
                invoker = fieldDataManagerDAOobj.get(cl, tmpObj.getString(primaryKey));
                if (invoker != null) {
                    for (int i = 0; i < operandArr.length; i++) {
                        String operand = "";
                        try { //For module columns - Value is fetched from database.
                            String methodStr = operandArr[i].substring(0, 1).toUpperCase() + operandArr[i].substring(1).toLowerCase(); // Make first letter of operand capital.
                            objMethod = cl.getMethod("get" + methodStr + ""); // Gets the value of the operand
                            operand = (String) objMethod.invoke(invoker);
                        } catch (NoSuchMethodException ex) {
                            if (operandArr[i].matches(numberRegex)) {// For constant numbers in the format of #no. value#
                                operand = operandArr[i].substring(1, operandArr[i].length() - 1);
                                if (operand.substring(0, 1).equals("~")) {
                                    operand = operand.replace("~", "-");
                                }
                            } else {
                                //For custom columns - taking column value from already created json.
                                //This will not work if $ or something like that appended in json value of custom column.
                                if (tmpObj.has(operandArr[i])) {
                                    if (StringUtil.isNullOrEmpty(tmpObj.getString(operandArr[i]))) {
                                        operand = "0";
                                    } else { // this is to find numbers from strings
                                        Pattern numberPattern = Pattern.compile("(\\d+)(((.+)(\\d+))*)");
                                        Matcher m = numberPattern.matcher(tmpObj.getString(operandArr[i]));
                                        if (m.find()) {
                                            operand = m.group(0);
                                        } else {
                                            operand = tmpObj.getString(operandArr[i]);
                                        }
                                    }
                                } else {
                                    operand = "0";
                                }
                            }
                        }
                        if (StringUtil.isNullOrEmpty(operand)) {
                            operand = "0";
                        } else if (operand.substring(0, 1).equals("-")) {
                            operand = "(" + operand + ")";
                        }
                        formulae = formulae.replaceAll(operandArr[i].toString(), operand); //Put the value in the formulae.
                    }
                }
                try {
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine engine = mgr.getEngineByName("js");
                    double ans = (Double) engine.eval(formulae);
                    String custom_ans = !String.valueOf(ans).equals("NaN") ? String.valueOf(ans) : "";
                    tmpObj.put(fieldname, currencyRender(custom_ans, currencyid, companyid));
                } catch (ScriptException e) {
                    System.out.println(formulae);
                    tmpObj.put(fieldname, "");
                }
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (SecurityException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            ex.printStackTrace();
            tmpObj.put(fieldname, "");
        } catch (Exception ex) {
            ex.printStackTrace();
            tmpObj.put(fieldname, "");
        } finally {
        }
        return tmpObj;
    }

    public JSONObject applyColumnFormulae(List lst, String currencyid, JSONObject tmpObj, String modName) throws ServiceException, JSONException {
        String fieldname = "";
        try {
            String compnayid = tmpObj.optString("companyid");
            /*
             * Some tested formulaes - 1. revenue+price - All module fields 2.
             * ((revenue+price)*NumField1)+NumField1 - Module + custome fields
             * 3. NumField1/NumField3 - All custom fields 4.
             * (NumField1+NumField3)+((#~100#+#250#)*#3#) - custom fields +
             * constants
             */

            String operatorRegex = "[\\+\\-\\*\\/]"; // Regex to get find the operators in the formulae.
            String bracketRegex = "[\\(\\)]"; // Regex to get find the operators in the formulae.
            String numberRegex = "^\\#\\~?([0-9]*|\\d*\\.\\d{1}?\\d*)\\#$";
            /**
             * For constant numbers 1. Use format : #no. value# 2. Negative
             * Constants : #~ No. Value# 3. #(No. Value)# : will not work.
             * Instead use: (#No. Value#)
             */
            String operandArr[] = null;
            String calStr = "";
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                String formulae = (String) row[0];
                fieldname = (String) row[1];

                Object invoker = null;
                String primaryKey = "";
                java.lang.reflect.Method objMethod;
                calStr = formulae.replaceAll(operatorRegex, ",");
                calStr = calStr.replaceAll(bracketRegex, "");
                operandArr = calStr.split(",");
                Class cl = Class.forName("com.krawler.crm.database.tables.Crm" + modName + "");
                if (modName.equals("Lead")) {
                    primaryKey = "leadid";
                } else if (modName.equals("Contact")) {
                    primaryKey = "contactid";
                } else if (modName.equals("Product")) {
                    primaryKey = "productid";
                } else if (modName.equals("Account")) {
                    primaryKey = "accountid";
                } else if (modName.equals("Opportunity")) {
                    primaryKey = "oppid";
                } else if (modName.equals("Case")) {
                    primaryKey = "caseid";
                }
                invoker = fieldDataManagerDAOobj.get(cl, tmpObj.getString(primaryKey));
                if (invoker != null) {
                    for (int i = 0; i < operandArr.length; i++) {
                        String operand = "";
                        try { //For module columns - Value is fetched from database.
                            String methodStr = operandArr[i].substring(0, 1).toUpperCase() + operandArr[i].substring(1).toLowerCase(); // Make first letter of operand capital.
                            objMethod = cl.getMethod("get" + methodStr + ""); // Gets the value of the operand
                            operand = (String) objMethod.invoke(invoker);
                        } catch (NoSuchMethodException ex) {
                            if (operandArr[i].matches(numberRegex)) {// For constant numbers in the format of #no. value#
                                operand = operandArr[i].substring(1, operandArr[i].length() - 1);
                                if (operand.substring(0, 1).equals("~")) {
                                    operand = operand.replace("~", "-");
                                }
                            } else {
                                //For custom columns - taking column value from already created json.
                                //This will not work if $ or something like that appended in json value of custom column.
                                if (tmpObj.has(operandArr[i])) {
                                    if (StringUtil.isNullOrEmpty(tmpObj.getString(operandArr[i]))) {
                                        operand = "0";
                                    } else { // this is to find numbers from strings
                                        Pattern numberPattern = Pattern.compile("(\\d+)(((.+)(\\d+))*)");
                                        Matcher m = numberPattern.matcher(tmpObj.getString(operandArr[i]));
                                        if (m.find()) {
                                            operand = m.group(0);
                                        } else {
                                            operand = tmpObj.getString(operandArr[i]);
                                        }
                                    }
                                } else {
                                    operand = "0";
                                }
                            }
                        }
                        if (StringUtil.isNullOrEmpty(operand)) {
                            operand = "0";
                        } else if (operand.substring(0, 1).equals("-")) {
                            operand = "(" + operand + ")";
                        }
                        formulae = formulae.replaceAll(operandArr[i].toString(), operand); //Put the value in the formulae.
                    }
                }
                try {
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine engine = mgr.getEngineByName("js");
                    double ans = (Double) engine.eval(formulae);
                    String custom_ans = !String.valueOf(ans).equals("NaN") ? String.valueOf(ans) : "";
                    tmpObj.put(fieldname, currencyRender(custom_ans, currencyid, compnayid));
                } catch (ScriptException e) {
                    System.out.println(formulae);
                    tmpObj.put(fieldname, "");
                }
            }
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (SecurityException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            ex.printStackTrace();
            throw ServiceException.FAILURE("fieldManager.applyColumnFormulae : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            ex.printStackTrace();
            tmpObj.put(fieldname, "");
        } catch (Exception ex) {
            ex.printStackTrace();
            tmpObj.put(fieldname, "");
        } finally {
        }
        return tmpObj;
    }

    @Deprecated
    public String currencyRender(String currency, String currencyID) throws SessionExpiredException {
        if (!StringUtil.isNullOrEmpty(currency)) {
            KWLCurrency cur = (KWLCurrency) fieldDataManagerDAOobj.get(KWLCurrency.class, currencyID);
            String symbol = cur.getHtmlcode();
            char temp = (char) Integer.parseInt(symbol, 16);
            symbol = Character.toString(temp);
            float v = 0;
            String str = authHandler.getCompleteDFStringForAmount("#,##0.");
            DecimalFormat decimalFormat = new DecimalFormat(str);
            if (currency.equals("")) {
                return symbol;
            }
            v = Float.parseFloat(currency);
            String fmt = decimalFormat.format(v);
            fmt = symbol + " " + fmt;
            return fmt;
        } else {
            return "";
        }
    }
    
    public String currencyRender(String currency, String currencyID, String companyid) throws SessionExpiredException {
        if (!StringUtil.isNullOrEmpty(currency)) {
            KWLCurrency cur = (KWLCurrency) fieldDataManagerDAOobj.get(KWLCurrency.class, currencyID);
            String symbol = cur.getHtmlcode();
            char temp = (char) Integer.parseInt(symbol, 16);
            symbol = Character.toString(temp);
            float v = 0;
            String str = authHandler.getCompleteDFStringForAmount("#,##0.", companyid);
            DecimalFormat decimalFormat = new DecimalFormat(str);
            if (currency.equals("")) {
                return symbol;
            }
            v = Float.parseFloat(currency);
            String fmt = decimalFormat.format(v);
            fmt = symbol + " " + fmt;
            return fmt;
        } else {
            return "";
        }
    }

    public List getCustomColumnFormulae(Object[] params) {
        return fieldDataManagerDAOobj.getCustomColumnFormulae(params);
    }

    /*
        Params Details
        jarray = 
    
    */
    public JSONArray getComboValueIdsForCurrentModule(JSONArray jarray, int moduleId, String companyid, int customcolumn) {
        JSONArray modifiedJson = new JSONArray();
        try {
            // used for auto generated record
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            HashMap<String, Object> map = new HashMap<String, Object>();
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleId, customcolumn));
            requestParams.put("isActivated", 1);
            if (customcolumn == 0) {
                requestParams.remove(Constants.customcolumn);           // for global level : line dimentions also appears
            }
            KwlReturnObject result = fieldManagerDAOobj.getFieldParams(requestParams); // get custom field for module
            List<FieldParams> l = result.getEntityList();
            for(FieldParams tmpcontyp : l) {
                map.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
                map.put(tmpcontyp.getFieldname() + "column", tmpcontyp.getColnum());
                map.put(tmpcontyp.getFieldname()+"refcolumn_name", tmpcontyp.getRefcolnum()); // get refcolnum

            }
            if (jarray != null) {
                for (int i = 0; i < jarray.length(); i++) {
                    try {
                        JSONObject jobj = jarray.getJSONObject(i);
                        Integer xtype = Integer.parseInt(jobj.has(Constants.xtype)?jobj.getString(Constants.xtype):"0");
                        if (jobj.has(Constants.Acc_custom_field)) {
                            String fieldname = jobj.getString(Constants.Acc_custom_field);
                            String fielddbname = jobj.getString(fieldname);
                            String fieldValue = jobj.getString(fielddbname);

                            if (!StringUtil.isNullOrEmpty(fieldValue)) {
                                if (map.containsKey(fieldname)) {
                                    jobj.remove(fielddbname);
                                    fielddbname = map.get(fieldname + "column").toString();
                                    fielddbname = "col" + fielddbname;
                                    jobj.put(fieldname, fielddbname);
                                    jobj.put(fielddbname, fieldValue);
                                    String refcolumn_name="Col"+map.get(fieldname+"refcolumn_name").toString();
                                    jobj.put("refcolumn_name", refcolumn_name);
                                    if (xtype == 4 || xtype == 7 || xtype == 12) {
                                        String fieldid = "";
                                        if (customcolumn == 1) {
                                            fieldid = jobj.getString("filedid");
                                        } else {
                                            fieldid = jobj.getString("fieldid");
                                        }
                                        String values = fieldManagerDAOobj.getParamsValue(fieldid, fieldValue);     //get values from ids
                                        fieldid = map.get(fieldname).toString();
                                        String ids = fieldManagerDAOobj.getIdsUsingParamsValue(fieldid, values);     // get ids for module using values and field id
                                        jobj.put(fielddbname, ids);

                                    }
                                    modifiedJson.put(jobj);
                                }
                            }
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return modifiedJson;
    }
   public JSONArray GetJsonArrayUsingFieldIds(JSONArray jarray, int moduleId, String companyid, int customcolumn, boolean iscallFromCRM) {
        JSONArray modifiedJson = new JSONArray();
        try {
            // used for auto generated record
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
            HashMap<String, Object> map = new HashMap<String, Object>();
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleId, customcolumn));
            requestParams.put("isActivated", 1);
            String field_Label = "";
            if (customcolumn == 0) {
                requestParams.remove(Constants.customcolumn);           // for global level : line dimentions also appears
            }

            if (iscallFromCRM && moduleId == Constants.Acc_Customer_Quotation_ModuleId) {
                requestParams.put("isCallFromCRM", true);
                requestParams.put("companyId", companyid);
                requestParams.put(Constants.filter_names, null);
                requestParams.put(Constants.filter_values, null);
            }
            KwlReturnObject result = fieldManagerDAOobj.getFieldParams(requestParams); // get custom field for module
            List<FieldParams> l = result.getEntityList();
            for (FieldParams tmpcontyp : l) {
                map.put(tmpcontyp.getFieldname(), tmpcontyp.getId());
                map.put(tmpcontyp.getFieldname() + "column", tmpcontyp.getColnum());

            }
            if (jarray != null) {
                for (int i = 0; i < jarray.length(); i++) {
                    try {
                        JSONObject jobj = jarray.getJSONObject(i);
                        String fieldId = jobj.getString("fieldid");

                        /*If product custom field sent from CRM is not matched with 
                        
                         Custom field of ERP Product then execute continue */
                        if (iscallFromCRM && !map.containsValue(fieldId)) {
                            continue;
                        }
                        String value = jobj.getString("value");
                        String value_old = value;
                        KwlReturnObject fieldParams = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldId);
                        FieldParams fieldParams1 = (FieldParams) fieldParams.getEntityList().get(0);

                        field_Label = fieldParams1.getFieldlabel();

                        /* Field Type->4=dropdown  & Field Type->7=multiselect dropdown*/
                        if ((fieldParams1.getFieldtype() == Constants.SINGLESELECTCOMBO || fieldParams1.getFieldtype() == Constants.MULTISELECTCOMBO)) {

                            requestParams1.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid));
                            requestParams1.put(Constants.filter_values, Arrays.asList(field_Label,companyid));
                            KwlReturnObject result1 = fieldManagerDAOobj.getFieldParams(requestParams1); // get custom field module wise from fieldlabel
                            List<FieldParams> fieldParamsList = result1.getEntityList();
                            for (FieldParams fieldparams1 : fieldParamsList) {
                                
                                fieldId = fieldparams1.getId();

                                JSONObject newjob = new JSONObject();
                                // if (fieldParams1 != null) {
                                newjob.put("fieldid", fieldparams1.getId());
                                newjob.put("xtype", fieldparams1.getFieldtype());
                                newjob.put("fieldname", fieldparams1.getFieldname());
                                newjob.put(fieldparams1.getFieldname(), "Col" + fieldparams1.getColnum());
                                newjob.put("refcolumn_name", "Col" + fieldparams1.getRefcolnum());
                                //if (fieldParams1.getFieldtype() == 4 || fieldParams1.getFieldtype() == 7) {
                                String ids = fieldManagerDAOobj.getIdsUsingParamsValue(fieldId, value_old);     // get ids for module using values and field id  
                                value = ids;

                                // }
                                newjob.put("Col" + fieldparams1.getColnum(), value);
                                newjob.put("fieldDataVal", value);
                                
                                /*If fetched moduleid is not matched with moduleid coming from CRM then json 
                                 is not being put because it should be saved only for relevant module
                                */
                                if (fieldparams1.getModuleid() != moduleId && !(iscallFromCRM && fieldparams1.getModuleid()==Constants.Acc_Product_Master_ModuleId && fieldparams1.getRelatedmoduleid().contains("22"))) {
                                    continue;
                                }
                                
                                modifiedJson.put(newjob);
                                // }
                            }
                        } else {

                            JSONObject newjob = new JSONObject();
                            if (fieldParams1 != null) {
                                newjob.put("fieldid", fieldParams1.getId());
                                newjob.put("xtype", fieldParams1.getFieldtype());
                                newjob.put("fieldname", fieldParams1.getFieldname());
                                newjob.put(fieldParams1.getFieldname(), "Col" + fieldParams1.getColnum());
                                newjob.put("refcolumn_name", "Col" + fieldParams1.getRefcolnum());
//                                if (fieldParams1.getFieldtype() == 4 || fieldParams1.getFieldtype() == 7) {
//                                    String ids = fieldManagerDAOobj.getIdsUsingParamsValue(fieldId, value);     // get ids for module using values and field id  
//                                    value = ids;
//
//                                }
                                newjob.put("Col" + fieldParams1.getColnum(), value);
                                newjob.put("fieldDataVal", value);
                                modifiedJson.put(newjob);
                            }
                        }

                    } catch (JSONException ex) {
                        Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return modifiedJson;
    }
    
    
    /* Function is used to make custom field array to save in Database*/
    public JSONArray GetJsonArrayUsingFieldIds(JSONArray jarray, int moduleId, String companyid,boolean isGlobal) {
        JSONArray modifiedJson = new JSONArray();
        try {
          
            /*Iteration on custom & dimension field for line as well as global*/
            if (jarray != null) {
                for (int i = 0; i < jarray.length(); i++) {
                    try {
                        JSONObject jobj = jarray.getJSONObject(i);
                        String fieldId = jobj.optString("fieldid","");
                        String value = StringUtil.decodeString(jobj.optString("value",""));
                        /* We will not save null or empty value for custom or dimension field value*/
                        if (StringUtil.isNullOrEmpty(value)) {
                            continue;
                        }
                        KwlReturnObject fieldParams = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldId);
                        FieldParams fieldParams1 = (FieldParams) fieldParams.getEntityList().get(0);
                        JSONObject newjob = new JSONObject();
                        if (fieldParams1 != null) {
                            
                            /*
                            
                             isGlobal->true means Global Level Field
                             fieldParams1.getCustomcolumn()==1 ->Means Line level field
                             
                             */
                            if (isGlobal && fieldParams1.getCustomcolumn() == 1) {
                                continue;
                            }

                            /*
                            
                             isGlobal->false means Line Level Field
                             fieldParams1.getCustomcolumn()==0 ->Means Global level field
                             
                             */
                            if (!isGlobal && fieldParams1.getCustomcolumn() == 0) {
                                continue;
                            }
                            
                            newjob.put("fieldid", fieldParams1.getId());
                            newjob.put("xtype", fieldParams1.getFieldtype());
                            newjob.put("fieldname", fieldParams1.getFieldname());
                            newjob.put(fieldParams1.getFieldname(), "Col" + fieldParams1.getColnum());
                            newjob.put("refcolumn_name", "Col" + fieldParams1.getRefcolnum());
                            if (fieldParams1.getFieldtype() == 4 || fieldParams1.getFieldtype() == 7) {
                                String ids = fieldManagerDAOobj.getIdsUsingParamsValue(fieldId, value);     // get ids for module using values and field id  
                                value = ids;

                            }
                            newjob.put("Col" + fieldParams1.getColnum(), value);
                            newjob.put("fieldDataVal", value);    
                            modifiedJson.put(newjob);
                                             

                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return modifiedJson;
    }
            
    public String getValuesForLinkRecords(int moduleId, String companyid, String FieldName, String value,int customcolumn) {
        JSONArray modifiedJson = new JSONArray();
        String finalValue = "";
        try {
            // used for Link record
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            HashMap<String, Object> map = new HashMap<String, Object>();
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, "fieldname"));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleId, customcolumn, FieldName));
            requestParams.put("isActivated", 1);

            KwlReturnObject result = fieldManagerDAOobj.getFieldParams(requestParams); // get custom field for module
            List<FieldParams> l = result.getEntityList();
            String fieldId = "";
            for (FieldParams tmpcontyp : l) {
                fieldId = tmpcontyp.getId();
            }
            if (!StringUtil.isNullOrEmpty(fieldId)) {
                String ids = fieldManagerDAOobj.getIdsUsingParamsValue(fieldId, value);
                finalValue = ids;
            }

        } catch (ServiceException ex) {
            Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalValue;
    }
    /**
     * Get Value of Dimension without inserting value if not found
     * @param moduleId
     * @param companyid
     * @param FieldName
     * @param value
     * @param customcolumn
     * @return 
     */
    public String getValuesForLinkRecordsWithoutInsert(int moduleId, String companyid, String FieldName, String value,int customcolumn) {
        JSONArray modifiedJson = new JSONArray();
        String finalValue = "";
        try {
            // used for Link record
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            HashMap<String, Object> map = new HashMap<String, Object>();
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, "fieldname"));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleId, customcolumn, FieldName));
            requestParams.put("isActivated", 1);

            KwlReturnObject result = fieldManagerDAOobj.getFieldParams(requestParams); // get custom field for module
            List<FieldParams> l = result.getEntityList();
            String fieldId = "";
            for (FieldParams tmpcontyp : l) {
                fieldId = tmpcontyp.getId();
            }
            if (!StringUtil.isNullOrEmpty(fieldId)) {
                String ids = fieldManagerDAOobj.getIdsUsingParamsValueWithoutInsert(fieldId, value);
                finalValue = ids;
            }

        } catch (ServiceException ex) {
            Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return finalValue;
    }
    public void addCustomData(Map<String, Object> variableMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, JSONObject obj, JSONObject params) throws JSONException {
        boolean isLink = params.optBoolean("isLink", false);
        /*
        IsJEReport  used for Return value instead of id for single select drop down and return id for multi select 
        where as we need to return data using link flag for JE report i.e. include module wise data
        */
        boolean isJEReport = params.optBoolean("isJEReport", false);
        boolean idlinkcase=true;
        /*
         * To display value of check list and drop down in grid
         */
        boolean isReturnDropdownCheckListVal = params.optBoolean("isReturnDropdownCheckListVal", false);
        boolean isExport = params.optBoolean("isExport", false);
        boolean isdefaultHeaderMap = params.optBoolean(Constants.isdefaultHeaderMap, false);
        DateFormat userdf = params.has(Constants.userdf)?(DateFormat)params.get(Constants.userdf):null;
        boolean isSplitOpeningBalanceAmount = params.optBoolean("isSplitOpeningBalanceAmount", false);
        String accountid = params.has("accountid") && !StringUtil.isNullOrEmpty(params.getString("accountid")) ? params.getString("accountid") : "" ;
        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
            String colValue = "";
            String colValueForMulti = "";
            String colDescription = "";// To show Item Description for Diamensions -  Landplus templates
            if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                try {
                    String[] valueData = coldata.split(",");
                    for (String value : valueData) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                        if (fieldComboData != null) {
                            if ((fieldComboData.getField().getFieldtype() == 12 || fieldComboData.getField().getFieldtype() == 7) && !isExport && !isLink) {
                                idlinkcase=true;
                                if(isReturnDropdownCheckListVal){
                                    colValue += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                }else{
                                    colValue += value != null ? value + "," : ",";
                                }
                                colValueForMulti += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                            } else {
                                if (isJEReport) {
                                    idlinkcase = false;
                                }
                                colValue += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                            }
                            if(isSplitOpeningBalanceAmount){
                                colDescription = fieldComboData.getItemdescription()==null?"":fieldComboData.getItemdescription();
                                HashMap<String, Object> requestparams = new HashMap<String, Object>();
                                requestparams.put("accountid", accountid);
                                requestparams.put("fieldid", fieldComboData.getField().getId());
                                KwlReturnObject res = accCommonTablesDAO.getDistributedOpeningBalance(requestparams);
                                List<Object[]> ll = res.getEntityList();
                                if(ll.size()>0){// Opening Balance is distrributed among the Dimensions
                                    colValue = "";//We have to show dimension/custom column as empty if COA has distributed amount.
                                }
                            }
                        }
                    }
                    if (colValue.length() > 1) {
                        colValue = colValue.substring(0, colValue.length() - 1);
                    }
                    if (colValueForMulti.length() > 1) {
                        colValueForMulti = colValueForMulti.substring(0, colValueForMulti.length() - 1);
                    }
                    obj.put(varEntry.getKey() + "_Value", coldata);
                    obj.put(varEntry.getKey() + "_colValueForMulti", colValueForMulti);
                    if (isLink && idlinkcase) {
                        int linkModuleId = params.optInt("linkModuleId");
                        String companyid = params.optString("companyid");
                        int customcolumn = params.optInt("customcolumn", 1);
                        obj.put(varEntry.getKey() + "_linkValue", colValue);
                        colValue = getValuesForLinkRecords(linkModuleId, companyid, varEntry.getKey(), colValue, customcolumn);
                    }
                    if (isdefaultHeaderMap) { //Used for Android Services
                        obj.put(varEntry.getKey(), coldata);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                        obj.put(varEntry.getKey() + "Value", colValue);
                    } else {
                        obj.put(varEntry.getKey(), colValue);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                        obj.put(varEntry.getKey() + "_Description", colDescription);
                    }
                } catch (Exception ex) {
                    obj.put(varEntry.getKey()+"_Description", colDescription);
                    obj.put(varEntry.getKey(), coldata);
                    obj.put(varEntry.getKey() + "_Value", coldata);
                }
            } else if (customDateFieldMap.containsKey(varEntry.getKey()) && isExport) {
                DateFormat df2 = null;
                if (userdf != null) {
                    df2 = userdf;
                } else if (isdefaultHeaderMap) {
                    df2 = new SimpleDateFormat(Constants.MMMMdyyyy);
                } else {
                    df2 = new SimpleDateFormat(Constants.MMMMdyyyy);
                }
                //long milliSeconds = Long.parseLong(coldata);
//                if(params.has("browsertz") && params.get("browsertz")!=null && !StringUtil.isNullOrEmpty(params.get("browsertz").toString())){
//                        //df2.setTimeZone(TimeZone.getTimeZone("GMT" + params.get("browsertz")));
//                         //TimeZone is removed while Converting Long Custom Date Values Into Date Object
//                    }
                try {
                    //coldata = df2.format(new java.util.Date(milliSeconds));
//                coldata = df2.format(milliSeconds);
                    if (!StringUtil.isNullOrEmpty(coldata)) {
                        Date dateFromDB;
                        if (userdf != null) {
                            dateFromDB = new SimpleDateFormat(Constants.MMMMdyyyy).parse(coldata);
                            coldata = df2.format(dateFromDB);
                        } 
                    }
                    
                } catch (ParseException ex) {
                    Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                obj.put(varEntry.getKey(), coldata);
                    obj.put(varEntry.getKey() + "_Value", coldata);
            } else {
                /**
                 * Remove Double quote(") from customeJSONString.
                 */
                obj.put(varEntry.getKey(), coldata != null ? (coldata.contains("\"") ? coldata.replaceAll("\"", "\'") : coldata) : "");
                obj.put(varEntry.getKey() + "_Value", coldata);
            }
        }
    }
    public void getLineLevelCustomData(Map<String, Object> variableMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, JSONObject obj, JSONObject params) throws JSONException, ServiceException {
        boolean isLink = params.optBoolean("isLink", false);
        boolean isExport = params.optBoolean("isExport", false);
        boolean isForReport = params.optBoolean("isForReport", false);
        DateFormat userdf = params.has(Constants.userdf)?(DateFormat)params.get(Constants.userdf):null;
        HashSet<String> keyValuePairMap = new HashSet<String>();
        JSONArray array=new JSONArray();
        boolean getCustomFieldArray = params.optBoolean("getCustomFieldArray", false);
        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
            String colValue = "";
            if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                try {
                    String[] valueData = coldata.split(",");
                    for (String value : valueData) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                        /*
                            isForReport-flag used to show value in combo directly.
                        */
                        if (fieldComboData != null) {
                            if (!isForReport && !isLink) {
                                colValue += value != null ? value + "," : ",";
                            } else {
                                colValue += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                            }
                        } else if (!isForReport && value.equals(Constants.NONEID) && !isLink){
                            colValue  = Constants.NONEID + ",";
                        }
                    }
                    if (colValue.length() > 1) {
                        colValue = colValue.substring(0, colValue.length() - 1);
                    }
                    if (isLink && !StringUtil.isNullOrEmpty(colValue)) {
                        int linkModuleId = params.optInt("linkModuleId");
                        String companyid = params.optString("companyid");
                        int customcolumn = params.optInt("customcolumn", 1);
                        colValue = getValuesForLinkRecords(linkModuleId, companyid, varEntry.getKey(), colValue, customcolumn);
                    }
                    /**
                     * set None value in generate PO from SO (vice versa)case if colValue is empty.
                     */
                    if (StringUtil.isNullOrEmpty(colValue) && isLink) {
                        colValue = Constants.NONEID;
                    }
                    obj.put(varEntry.getKey(), colValue);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                    keyValuePairMap.add(varEntry.getKey().split("_")[1] + " : " + colValue);
                } catch (Exception ex) {
                    obj.put(varEntry.getKey(), coldata);
                    keyValuePairMap.add(varEntry.getKey().split("_")[1] + " : " + coldata);
                }
            } else if (customDateFieldMap.containsKey(varEntry.getKey()) && isExport) {
                DateFormat df2 = null;
                if (userdf != null) {
                    df2 = userdf;
                } else {
                    df2 = new SimpleDateFormat(Constants.MMMMdyyyy);
                }
                //long milliSeconds = Long.parseLong(coldata);
               // coldata = df2.format(milliSeconds);
                if (!StringUtil.isNullOrEmpty(coldata)) {
                    Date dateFromDB;
                    try {
                        if (userdf != null) {
                            dateFromDB = new SimpleDateFormat(Constants.MMMMdyyyy).parse(coldata);
                            coldata = df2.format(dateFromDB);
                        }
                    } catch (ParseException ex) {                      
                        Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                    
                obj.put(varEntry.getKey(), coldata);
                keyValuePairMap.add(varEntry.getKey().split("_")[1] + " : " + coldata);
            } else {
                obj.put(varEntry.getKey(), coldata != null ? coldata : "");
                keyValuePairMap.add(varEntry.getKey().split("_")[1] + " : " + (coldata != null ? coldata : ""));
            }
            if (getCustomFieldArray) {
                String fieldName = varEntry.getKey();
                String data=varEntry.getValue().toString();
                String companyid = params.optString("companyid");
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.Acc_custom_field));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_FixedAssets_Details_ModuleId, fieldName));
                requestParams.put("isActivated", 1);
                KwlReturnObject result = fieldManagerDAOobj.getFieldParams(requestParams); // get custom field for module
                List<FieldParams> l = result.getEntityList();
                for (FieldParams fieldParams : l) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("filedid", fieldParams.getId());
                    jSONObject.put(fieldName, "Col" + fieldParams.getColnum());
                    jSONObject.put("xtype", ""+fieldParams.getFieldtype());
                    jSONObject.put("refcolumn_name", "Col"+fieldParams.getRefcolnum());
                    jSONObject.put("Col" + fieldParams.getColnum(), data);
                    jSONObject.put("fieldname", fieldName);
                    array.put(jSONObject);
                }
            }
        }
        if (getCustomFieldArray) {
            obj.put("customfield", array);
        }
        String KVP = "";
        for (String l : keyValuePairMap) {
            KVP += l + "\n";
        }
        if (KVP.length() > 2) {
            KVP = KVP.substring(0, KVP.length() - 1);
        }
        obj.put("allCustomFieldKeyValuePairString", KVP);
    }
    
    /**
     * getLineLevelCustomData function is overloaded to handle custom Rich Text box.
     * @param variableMap
     * @param customFieldMap
     * @param customDateFieldMap
     * @param obj
     * @param params
     * @param customRichTextMap
     * @throws JSONException
     * @throws ServiceException 
     */
    public void getLineLevelCustomData(Map<String, Object> variableMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, JSONObject obj, JSONObject params, HashMap<String, String> customRichTextMap) throws JSONException, ServiceException {
        boolean isLink = params.optBoolean("isLink", false);
        boolean isExport = params.optBoolean("isExport", false);
        boolean isForReport = params.optBoolean("isForReport", false);
        DateFormat userdf = params.has("userdf") ? (DateFormat) params.get("userdf") : null;
        HashSet<String> keyValuePairMap = new HashSet<String>();
        JSONArray array = new JSONArray();
        boolean getCustomFieldArray = params.optBoolean("getCustomFieldArray", false);
        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
            String colValue = "";
            if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                try {
                    String[] valueData = coldata.split(",");
                    for (String value : valueData) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                        /*
                         isForReport-flag used to show value in combo directly.
                         */
                        if (fieldComboData != null) {
                            if (!isForReport && !isLink) {
                                colValue += value != null ? value + "," : ",";
                            } else {
                                colValue += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                            }
                        }
                    }
                    if (colValue.length() > 1) {
                        colValue = colValue.substring(0, colValue.length() - 1);
                    }
                    if (isLink) {
                        int linkModuleId = params.optInt("linkModuleId");
                        String companyid = params.optString("companyid");
                        int customcolumn = params.optInt("customcolumn", 1);
                        colValue = getValuesForLinkRecords(linkModuleId, companyid, varEntry.getKey(), colValue, customcolumn);
                    }
                    obj.put(varEntry.getKey(), colValue);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                    keyValuePairMap.add(varEntry.getKey().split("_")[1] + " : " + colValue);
                } catch (Exception ex) {
                    obj.put(varEntry.getKey(), coldata);
                    keyValuePairMap.add(varEntry.getKey().split("_")[1] + " : " + coldata);
                }
            } else if (customDateFieldMap.containsKey(varEntry.getKey()) && isExport) {
                DateFormat df2 = null;
                if (userdf != null) {
                    df2 = userdf;
                } else {
                    df2 = new SimpleDateFormat(Constants.MMMMdyyyy);
                }
                //long milliSeconds = Long.parseLong(coldata);
                //coldata = df2.format(milliSeconds);
                if(!StringUtil.isNullOrEmpty(coldata)) {
                    Date dateFromDB;
                    try {
                        if (userdf != null) {
                            dateFromDB = new SimpleDateFormat(Constants.MMMMdyyyy).parse(coldata);
                            coldata = df2.format(dateFromDB);
                        }
                    } catch (ParseException ex) {
                        Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                obj.put(varEntry.getKey(), coldata);
                keyValuePairMap.add(varEntry.getKey().split("_")[1] + " : " + coldata);
            } else if (customRichTextMap.containsKey(varEntry.getKey()) && isForReport) {
                coldata = Jsoup.parse(coldata).text();
                obj.put(varEntry.getKey(), coldata);
                keyValuePairMap.add(varEntry.getKey().split("_")[1] + " : " + coldata);
            } else {
                obj.put(varEntry.getKey(), coldata != null ? coldata : "");
                keyValuePairMap.add(varEntry.getKey().split("_")[1] + " : " + (coldata != null ? coldata : ""));
            }
            if (getCustomFieldArray) {
                String fieldName = varEntry.getKey();
                String data = varEntry.getValue().toString();
                String companyid = params.optString("companyid");
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.Acc_custom_field));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_FixedAssets_Details_ModuleId, fieldName));
                requestParams.put("isActivated", 1);
                KwlReturnObject result = fieldManagerDAOobj.getFieldParams(requestParams); // get custom field for module
                List<FieldParams> l = result.getEntityList();
                for (FieldParams fieldParams : l) {
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("filedid", fieldParams.getId());
                    jSONObject.put(fieldName, "Col" + fieldParams.getColnum());
                    jSONObject.put("xtype", "" + fieldParams.getFieldtype());
                    jSONObject.put("refcolumn_name", "Col" + fieldParams.getRefcolnum());
                    jSONObject.put("Col" + fieldParams.getColnum(), data);
                    jSONObject.put("fieldname", fieldName);
                    array.put(jSONObject);
                }
            }
        }
        if (getCustomFieldArray) {
            obj.put("customfield", array);
        }
        String KVP = "";
        for (String l : keyValuePairMap) {
            KVP += l + "\n";
        }
        if (KVP.length() > 2) {
            KVP = KVP.substring(0, KVP.length() - 1);
        }
        obj.put("allCustomFieldKeyValuePairString", KVP);
    }
    /**
     *
     * @param variableMap
     * @param customFieldMap
     * @param customDateFieldMap
     * @param obj
     * @param params = Key Used for distinguish same field of different modules
     * @throws JSONException
     * @throws ServiceException
     * Description : Written New Function to return
     *               custom data to handle multiple module data export
     */
    public void getLineLevelCustomDataWithKey(Map<String, Object> variableMap, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, JSONObject obj, JSONObject params) throws JSONException, ServiceException {
        boolean isLink = params.optBoolean("isLink", false);
        boolean isExport = params.optBoolean("isExport", false);
        String key = params.optString("key", "");
        boolean isdefaultHeaderMap = params.optBoolean(Constants.isdefaultHeaderMap, false);
        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
            String colValue = "";
            if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                try {
                    String[] valueData = coldata.split(",");
                    for (String value : valueData) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                        if (fieldComboData != null) {
                            if (!isExport && !isLink) {
                                colValue += value != null ? value + "," : ",";
                            } else {
                                colValue += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                            }
                        }
                    }
                    if (colValue.length() > 1) {
                        colValue = colValue.substring(0, colValue.length() - 1);
                    }
                    if (isdefaultHeaderMap) { //Used for Android Services
                        obj.put(varEntry.getKey(), coldata);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                        obj.put(varEntry.getKey() + "Value", colValue);
                    } else {
                        obj.put(varEntry.getKey(), colValue);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                        obj.put(key + varEntry.getKey(), colValue);
                    }
                } catch (Exception ex) {
                    obj.put(varEntry.getKey(), coldata);
                    obj.put(key + varEntry.getKey(), coldata);
                }
            } else if (customDateFieldMap.containsKey(varEntry.getKey()) && isExport) {
                DateFormat df2 =null;
                if (isdefaultHeaderMap) {
                    df2 = new SimpleDateFormat(Constants.MMMMdyyyy);
                } else {
                    df2 = new SimpleDateFormat(Constants.yyyyMMdd);
                }
                //long milliSeconds = Long.parseLong(coldata);
//                if (params.has("browsertz") && params.get("browsertz") != null && !StringUtil.isNullOrEmpty(params.get("browsertz").toString())) {
//                    df2.setTimeZone(TimeZone.getTimeZone("GMT" + params.get("browsertz")));
//                }
                //coldata = df2.format(new java.util.Date(milliSeconds));
//                coldata = df2.format(milliSeconds);
                if (!StringUtil.isNullOrEmpty(coldata)) {
                    Date dateFromDB;
                    try {
                        if (!isdefaultHeaderMap) {
                            dateFromDB = new SimpleDateFormat(Constants.MMMMdyyyy).parse(coldata);
                            coldata = df2.format(dateFromDB);
                        } 
                    } catch (ParseException ex) {
                        Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                    
                obj.put(varEntry.getKey(), coldata);
                obj.put(key + varEntry.getKey(), coldata);
            } else {
                obj.put(varEntry.getKey(), coldata != null ? coldata : "");
                obj.put(key + varEntry.getKey(), coldata != null ? coldata : "");
            }
        }
    }
    public JSONArray getCustomFieldForOeningTransactionsRecords(List headArrayList, HashMap<String, FieldParams> customFieldParamMap, String[] recarr, HashMap<String, Integer> columnConfig ,HttpServletRequest request) throws ServiceException, AccountingException {
        JSONArray customJArr = new JSONArray();
        String dateFormat = null, dateFormatId = request.getParameter("dateFormat");
        if (!StringUtil.isNullOrEmpty(dateFormatId)) {
            KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
            KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);
            dateFormat = kdf != null ? kdf.getJavaForm() : null;
        }
        DateFormat df = new SimpleDateFormat(dateFormat);

        for (int K = 0; K < headArrayList.size(); K++) {
            HashMap<String, Object> requestParamsCF = new HashMap<String, Object>();
            FieldParams params = null;
            if (customFieldParamMap.containsKey(headArrayList.get(K).toString())) {
                params = (FieldParams) customFieldParamMap.get(headArrayList.get(K).toString());
            }
            if (params != null) {
                try {
                    JSONObject customJObj = new JSONObject();
                    customJObj.put("fieldid", params.getId());
                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                    customJObj.put("xtype", params.getFieldtype());
                    String fieldComboDataStr = "";
                    if (params.getFieldtype() == 3) { // if field of date type
                        int index = headArrayList.indexOf(headArrayList.get(K).toString());
                        String dateStr = index != -1 ? recarr[(Integer) columnConfig.get(params.getFieldlabel())].replaceAll("\"", "").trim() : "";
                        customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                        customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
                    } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7 || params.getFieldtype() == 12) {
                        int index = headArrayList.indexOf(headArrayList.get(K).toString());
                        String comboRecords = index != -1 ? recarr[(Integer) columnConfig.get(params.getFieldlabel())].replaceAll("\"", "").trim() : "";
                        String[] fieldComboDataArr = comboRecords.split(";");

                        for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                            requestParamsCF = new HashMap<String, Object>();
                            String fieldComboValue = fieldComboDataArr[dataArrIndex].replaceAll("\"", ""); // to Remove Double quotes
                            requestParamsCF.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                            requestParamsCF.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboValue, 0));
                            String comboRecID = "";
                            KwlReturnObject fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParamsCF);
                            if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                comboRecID = fieldComboData.getId();
                            }
                            if (!StringUtil.isNullOrEmpty(comboRecID)) {
                                fieldComboDataStr += comboRecID + ",";
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                        } else {
                            continue;
                        }
                    } else {
                        int index = headArrayList.indexOf(headArrayList.get(K).toString());
                        String rec = index != -1 ? recarr[(Integer) columnConfig.get(params.getFieldlabel())].replaceAll("\"", "").trim() : "";
                        if (params.getFieldtype() == 11) {
                            rec = rec.toLowerCase();
                        }
                        if (params.getMaxlength() < rec.length()) {
                            throw new AccountingException("Data given in '" + params.getFieldlabel() + "' field is exceeding the field length.");
                        }
                        customJObj.put("Col" + params.getColnum(), rec);
                        customJObj.put("fieldDataVal", rec);
                    }
                    customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());
                    customJArr.put(customJObj);
                } catch (JSONException ex) {
                    Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ParseException ex) {
//                    Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
                    throw new AccountingException("Please enter date in "+dateFormat+" format");
                }
            }
        }
        return customJArr;
    }
    /**
     * 
     * @param requestParams
     * @return
     * @throws SessionExpiredException
     * @throws UnsupportedEncodingException
     * @throws ServiceException 
     * @Desc : Create Advance Search JSON for Module
     */
    public String getSearchJsonByModuleID(HashMap<String, Object> requestParams) throws SessionExpiredException, UnsupportedEncodingException, ServiceException {
        JSONObject resultObj = null;
        JSONArray dataJArrObj = new JSONArray();
        boolean removeProductCustomFilter = false;   //Default true
        if (requestParams.containsKey("removeProductCustomFilter")) {
            removeProductCustomFilter = Boolean.parseBoolean(requestParams.get("removeProductCustomFilter").toString());
        }
        String Searchjson =  requestParams.get(Constants.Acc_Search_Json).toString();
        String filterCriteria = requestParams.get(Constants.Filter_Criteria).toString();
        String companyid = requestParams.get(Constants.companyKey)!=null? requestParams.get(Constants.companyKey).toString() : "";
        String module = requestParams.get(Constants.moduleid)!=null? requestParams.get(Constants.moduleid).toString(): "0";
        if(module.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID)){
            module = String.valueOf(Constants.Acc_Customer_ModuleId);
        }
        if(module.equalsIgnoreCase(Constants.Vendor_MODULE_UUID)){
            module = String.valueOf(Constants.Acc_Vendor_ModuleId);
        }
        
        int moduleid = Integer.parseInt(module);
        try{
            JSONObject jObj = new JSONObject(Searchjson);
            if(!StringUtil.isNullOrEmpty(Searchjson) && !StringUtil.isNullOrEmpty(filterCriteria) && moduleid!=0){
                int count = jObj.getJSONArray(Constants.root).length();
                for (int i = 0; i < count; i++) {
                    JSONObject jobj1 = jObj.getJSONArray(Constants.root).getJSONObject(i);
                    boolean mastersearch=false;
                    int mastermoduleid = 0;
                    if(jobj1.optString(Constants.moduleid).equalsIgnoreCase(""+Constants.Acc_Customer_ModuleId)){
                        mastersearch=true;
                        mastermoduleid = Constants.Acc_Customer_ModuleId;
                    } else if(jobj1.optString(Constants.moduleid).equalsIgnoreCase(""+Constants.Acc_Vendor_ModuleId)){
                        mastersearch=true;
                        mastermoduleid = Constants.Acc_Vendor_ModuleId;
                    }
                    if(removeProductCustomFilter && jobj1.optBoolean("isfrmpmproduct",false)){
                        jobj1.put("isfrmpmproduct", false);
                    }
                        String fieldlabel = jobj1.get("columnheader")!=null? jobj1.get("columnheader").toString() : "";
                        fieldlabel = StringUtil.DecodeText(fieldlabel);
                        String searchText = jobj1.get("combosearch")!=null? jobj1.get("combosearch").toString() : "";
                        searchText = StringUtil.DecodeText(searchText);

                        KwlReturnObject result = null;
                        HashMap<String, Object> reqPar = new HashMap<String, Object>();
                        reqPar.put("filter_names", Arrays.asList(Constants.companyKey, "fieldlabel"));
                        reqPar.put("filter_values", Arrays.asList(companyid, fieldlabel));
                        reqPar.put(Constants.moduleid, mastersearch?mastermoduleid:moduleid);
                        if(requestParams.containsKey("isActivated") && (Integer) requestParams.get("isActivated") != null){
                            int activatedFlag = (Integer) requestParams.get("isActivated");
                            reqPar.put("isActivated", activatedFlag);
                        }
                        result = fieldManagerDAOobj.getFieldParams(reqPar);
                        List lst = result.getEntityList();
                        Iterator ite = lst.iterator();
                        while (ite.hasNext()) {
                            FieldParams tmpcontyp = new FieldParams();
                            tmpcontyp = (FieldParams) ite.next();
                            String fieldid = tmpcontyp.getId();
                            int columnNo=tmpcontyp.getColnum();
                            jobj1.remove("column");
                            jobj1.put("column",fieldid);
                            jobj1.remove("refdbname");
                            jobj1.put("refdbname","Col"+columnNo);
                            jobj1.remove("xfield");
                            jobj1.put("xfield", "Col"+columnNo);
                            if (!mastersearch) {
                                jobj1.remove(Constants.moduleid);
                                jobj1.put(Constants.moduleid, tmpcontyp.getModuleid());
                            }

                            if(((jobj1.getString("fieldtype").equalsIgnoreCase("4") || jobj1.getString("fieldtype").equalsIgnoreCase("7") || jobj1.getString("fieldtype").equalsIgnoreCase("12")) && jobj1.getString("xtype").equalsIgnoreCase("select")) && !mastersearch){
                            HashMap<String, Object> reqParams = new HashMap<String, Object>();
                            reqParams.put(Constants.filter_names, Arrays.asList("fieldid", FieldConstants.Crm_deleteflag));
                            reqParams.put(Constants.filter_values, Arrays.asList(fieldid, 0));
                            reqParams.put("searchText", searchText);
                            ArrayList order_by = new ArrayList();
                            ArrayList order_type = new ArrayList();
                            order_by.add("itemsequence");
                            order_type.add("asc");
                            reqParams.put("order_by", order_by);
                            reqParams.put("order_type", order_type);

                            KwlReturnObject result1 = accCommonTablesDAO.getCustomCombodata(reqParams);
                            List lst1 = result1.getEntityList();
                            String comboDataIds = "";
                            Iterator ite1 = lst1.iterator();
                            while (ite1.hasNext()) {
                                Object row = (Object) ite1.next();
                                FieldComboData comboDataObj = (FieldComboData) row;
                                if(comboDataObj!=null){
                                    comboDataIds = comboDataIds + comboDataObj.getId() + ","; 
                                }
                            }
                            comboDataIds = comboDataIds.length()>0 ? comboDataIds.substring(0,comboDataIds.length()-1) : "";
                            jobj1.remove("searchText");
                            jobj1.remove("search");
                            jobj1.put("searchText",comboDataIds);   
                            jobj1.put("search",comboDataIds);   
                        }
                    }
                        dataJArrObj.put(jobj1);
                }
                jObj.put(Constants.root, dataJArrObj);
            } else {
                jObj = new JSONObject(Searchjson);
            }
            resultObj = jObj;
        }        
        catch (JSONException ex) {
            Logger.getLogger(FieldManagerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultObj.toString();
    }
    
    public JSONObject getSearchJsonForModuleID(Map requestParams) throws SessionExpiredException, UnsupportedEncodingException, ServiceException {
        JSONObject resultObj = null;
        JSONArray dataJArrObj = new JSONArray();
        String Searchjson = requestParams.get(Constants.Acc_Search_Json).toString();
        String moduleid = requestParams.get(Constants.moduleid) != null ? requestParams.get(Constants.moduleid).toString() : "0";
        if(moduleid.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID)){
            moduleid = String.valueOf(Constants.Acc_Customer_ModuleId);
        }

        try {
            JSONObject jObj = new JSONObject(Searchjson);
            if (!StringUtil.isNullOrEmpty(Searchjson) && !moduleid.equals("0")) {
                int count = jObj.getJSONArray(Constants.root).length();
                for (int i = 0; i < count; i++) {
                    JSONObject jobj1 = jObj.getJSONArray(Constants.root).getJSONObject(i);
                    if (jobj1.optString(Constants.moduleid).equalsIgnoreCase(moduleid)) {
                        dataJArrObj.put(jobj1);
                    }
                }
                jObj.put(Constants.root, dataJArrObj);
            } else {
                jObj = new JSONObject(Searchjson);
            }
            resultObj = jObj;
        } catch (JSONException ex) {
            Logger.getLogger(fieldDataManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultObj;
    }
    
    public JSONObject getAdvanceSearchJson(String fcdid) throws ServiceException, JSONException {
        JSONObject cntObj = new JSONObject();
        List<String> combosearchItems = new ArrayList<>();
        if (!StringUtil.isNullOrEmpty(fcdid)) {
            combosearchItems = Arrays.asList(fcdid.split("\\s*,\\s*"));
}
        String searchtext = "";
        String combosearch = "";
        FieldParams fieldParams = null;
        for (Iterator<String> it = combosearchItems.iterator(); it.hasNext();) {
            String string = it.next();
            KwlReturnObject fieldCmbData = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), fcdid);
            FieldComboData fieldComboData = (FieldComboData) fieldCmbData.getEntityList().get(0);
            fieldParams = fieldComboData.getField();
            searchtext += fieldComboData.getId() + ",";
            combosearch += fieldComboData.getValue() + ",";
        }
        if (!StringUtil.isNullOrEmpty(searchtext) && searchtext.length() > 1) {
            searchtext = searchtext.substring(0, searchtext.length() - 1);
        }
        if (!StringUtil.isNullOrEmpty(combosearch) && combosearch.length() > 1) {
            combosearch = combosearch.substring(0, combosearch.length() - 1);
        }

        cntObj.put("searchText", searchtext);
        cntObj.put("search", searchtext);
        cntObj.put("combosearch", combosearch);
        cntObj.put("column", fieldParams.getId());
        cntObj.put("refdbname", "Col" + fieldParams.getColnum());
        cntObj.put("xfield", "Col" + fieldParams.getColnum());
        cntObj.put(Constants.iscustomcolumn, true);
        cntObj.put("iscustomcolumndata", fieldParams.getCustomcolumn() == 1 ? true : false);
        cntObj.put("isfrmpmproduct", false);
        cntObj.put("fieldtype", fieldParams.getFieldtype());
        cntObj.put("columnheader", fieldParams.getFieldlabel());
        cntObj.put(Constants.xtype, "select");
        cntObj.put("isinterval", false);
        cntObj.put("interval", false);
        cntObj.put("isbefore", "");
        cntObj.put("isdefaultfield", false);
        cntObj.put(Constants.moduleid, fieldParams.getModuleid());

        return cntObj;
    }
    /**
     *
     * @param params
     * @return
     * @Desc : Get Which address field map to dimension field
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONArray getAddressDimensionMapping(JSONObject params) throws ServiceException, JSONException {
        JSONArray currentAddressDetailrec = new JSONArray();
        KwlReturnObject kwlReturnObject = fieldManagerDAOobj.getAddressDimensionMapping(params);
        List<AddressFieldDimensionMapping> addressFieldDimensionMappings = kwlReturnObject.getEntityList();
        for (AddressFieldDimensionMapping addressFieldDimensionMapping : addressFieldDimensionMappings) {
            JSONObject object = new JSONObject();
            object.put("addreesField", addressFieldDimensionMapping.getAddressField());
            object.put("dimField", addressFieldDimensionMapping.getDimension().getFieldlabel());
            currentAddressDetailrec.put(object);
        }
        return currentAddressDetailrec;
    }
    /**
     * Function to get GST Fields document history
     *
     * @param reqParams
     * @throws ServiceException
     * @throws JSONException
     */
    public void getGSTDocumentHistory(JSONObject reqParams) throws ServiceException, JSONException {
        List list = fieldManagerDAOobj.getGSTDocumentHistory(reqParams);
        for (Object string : list) {
            Object[] data = (Object[]) string;
            reqParams.put("gstdochistoryid", (String) data[0]);
            reqParams.put("CustomerVendorTypeId", (String) data[1]);
            reqParams.put("GSTINRegistrationTypeId", (String) data[2]);
            reqParams.put("gstin", (String) data[3]);
            reqParams.put("GSTINRegTypeDefaultMstrID", data.length>4 && data[4]!=null ? (String) data[4] : "");
            reqParams.put("CustVenTypeDefaultMstrID", data.length>5 && data[5]!=null ? (String) data[5] : "");
        }
    }

    /**
     * Function to get Product Tax class history data
     *
     * @param reqParams
     * @throws ServiceException
     * @throws JSONException
     */
    public void getGSTTaxClassHistory(JSONObject reqParams) throws ServiceException, JSONException {
        List list = fieldManagerDAOobj.getGSTTaxClassHistory(reqParams);
        for (Object string : list) {
            Object[] data = (Object[]) string;
            reqParams.put("taxclasshistoryid", (String) data[0]);
            reqParams.put("taxclass", (String) data[1]);
        }
    }

    /**
     * Function to save GST Fields document history
     *
     * @param params
     * @throws ServiceException
     */
    public void createRequestMapToSaveDocHistory(JSONObject params) throws ServiceException {
        Map<String, Object> reqMap = new HashMap();
        if (!StringUtil.isNullOrEmpty(params.optString("gstdochistoryid"))) {
            reqMap.put("gstdochistoryid", params.optString("gstdochistoryid"));
        }
        reqMap.put("custventype", params.optString("CustomerVendorTypeId"));
        reqMap.put("gstrtype", params.optString("GSTINRegistrationTypeId"));
        reqMap.put("gstin", params.optString("gstin"));
        reqMap.put("docid", params.optString("docid"));
        reqMap.put("moduleid", params.optInt("moduleid"));
        fieldManagerDAOobj.saveGstDocumentHistory(reqMap);
    }

    /**
     * Function to save Product tax class document history
     *
     * @param params
     * @throws ServiceException
     */
    public void createRequestMapToSaveTaxClassHistory(JSONObject params) throws ServiceException {
        Map<String, Object> reqMap = new HashMap();
        reqMap.put("taxclass", params.optString("taxclass"));
        reqMap.put("detaildocid", params.optString("detaildocid"));
        reqMap.put("moduleid", params.optInt("moduleid"));
        fieldManagerDAOobj.saveGstTaxClassHistory(reqMap);
    }
    
    public boolean isTaxActivated(JSONObject paramJobj) throws ServiceException, JSONException {
        boolean isTaxActivated = true;
        String taxId = paramJobj.optString(Constants.TAXID);
        String companyId = paramJobj.optString(Constants.companyKey);
        if (!StringUtil.isNullOrEmpty(taxId) && !isNone(taxId)) {
            isTaxActivated = fieldManagerDAOobj.isTaxActivated(companyId, taxId);
            return isTaxActivated;
        } else if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.detail)) || !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.EXPENSE_DETAIL)) || !StringUtil.isNullOrEmpty(paramJobj.optString("details"))) {
            JSONArray jArr = !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.detail)) ? new JSONArray(paramJobj.optString(Constants.detail)) : (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.EXPENSE_DETAIL)) ? new JSONArray(paramJobj.optString(Constants.EXPENSE_DETAIL)): new JSONArray(paramJobj.optString("details")));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject taxObj = jArr.getJSONObject(i);
                taxId = taxObj.optString("prtaxid");
                if (!StringUtil.isNullOrEmpty(taxId) && !isNone(taxId) && !fieldManagerDAOobj.isTaxActivated(companyId, taxId)) {
                    isTaxActivated = false;
                    break;
                }
            }
        }
        return isTaxActivated;
    }

    private boolean isNone(String value) {
        /**
         * If data contain value is "None" or "1234" or "-1" then its return
         * true.
         */
        return value.equalsIgnoreCase(Constants.NONE) || value.equalsIgnoreCase(Constants.NONEID) || value.equalsIgnoreCase(Constants.NONEID_1);
    }
}
