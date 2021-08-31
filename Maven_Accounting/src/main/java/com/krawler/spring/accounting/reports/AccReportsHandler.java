/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author sagar
 */
public class AccReportsHandler {
    public static JSONArray sortJsonArrayOnJEDetailSerialNo(JSONArray array) throws JSONException {
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
                        lid = lhs.getString("srno");
                        rid = rhs.getString("srno");
                    } catch (JSONException ex) {
                        Logger.getLogger(AccReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return Integer.valueOf(lid).compareTo(Integer.valueOf(rid));
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(AccReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }

        return new JSONArray(jsons);
    }
        public static JSONArray sortJsonArrayByGSTType(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
}
            Collections.sort(jsons, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    String lid = "", rid = "";
                    lid = lhs.optString("taxname",""+1);
                    rid = rhs.optString("taxname",""+1);
                    return lid.compareTo(rid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(AccReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }

        return new JSONArray(jsons);
    }
        
    public static Map<String, Object[]> getcompanyMaxDateProductPriceListMap(List<Object[]> companyMaxDateProductPriceList) {
        Map<String, Object[]> map = new HashMap<String, Object[]>();
        try {
            for(Object [] obj : companyMaxDateProductPriceList) {
                if(!map.containsKey(obj[3].toString())) {
                    map.put(obj[3].toString(), obj);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccReportsServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return map;
        }
        
    }
    public static JSONArray calculateSubtotal(HttpServletRequest request, JSONArray DataJArr) throws JSONException {
        double customertotal = 0;
        double total = 0;
        String temp = "";
        JSONArray array = new JSONArray();
        String headers[] = null;
        String tax = "";
        boolean firstrecpur = true;
        boolean firstrecsale = true;
        if (request.getParameter("header") != null) {
            String head = request.getParameter("header");
            headers = (String[]) head.split(",");
            tax = headers[0];
        } else {
            tax = "taxcode";
        }

        for (int i = 0; i < DataJArr.length(); i++) {
            JSONObject jSONObject = DataJArr.getJSONObject(i);
            String type = jSONObject.optString("taxtype", "" + 1);
            int taxtype = Integer.parseInt(type);
            if (taxtype == 1) {      
                if (firstrecpur) {      //first record
                    JSONObject jSONObject1 = new JSONObject();
                    if (i != 0) {
                        JSONObject jSONObject2 = new JSONObject();
                        array.put(jSONObject2);
                    }
                    jSONObject1.put(tax, "PURCHASE");
                    array.put(jSONObject1);
                    firstrecpur = false;
                }
                    array.put(jSONObject);
            } else if (taxtype == 2) {            
                if (firstrecsale) {
                    JSONObject jSONObject1 = new JSONObject();
                    if (i != 0) {
                        JSONObject jSONObject2 = new JSONObject();
                        array.put(jSONObject2);
                    }
                    jSONObject1.put(tax, "SALES");
                    array.put(jSONObject1);
                    firstrecsale = false;
                }
                array.put(jSONObject);
            }

        }
        return array;
    }
 public static JSONArray sortJsonArrayByGSTDate(JSONArray array,final DateFormat dateFormat) throws JSONException {
          List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
                }
            Collections.sort(jsons, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    Date lid =null;Date rid=null;
                    try {
                         lid=dateFormat.parse(lhs.optString("invdate"));
                         rid= dateFormat.parse(rhs.optString("invdate"));
                        
                    } catch (ParseException ex) {
                        Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return lid.compareTo(rid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new JSONArray(jsons);
    
    }
 
 public static JSONArray sortPurchaseJsonArrayByGSTDate(JSONArray array,final DateFormat dateFormat) throws JSONException {
          List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
                }
            Collections.sort(jsons, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    Date lid =null;Date rid=null;
                    try {
                         lid=dateFormat.parse(lhs.optString("grdate"));
                         rid= dateFormat.parse(rhs.optString("grdate"));
                        
                    } catch (ParseException ex) {
                        Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return lid.compareTo(rid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(accReportsController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new JSONArray(jsons);
    
    }
    public static JSONArray  calculatetotal(HttpServletRequest request, JSONArray DataJArr) throws JSONException {
        JSONArray array = new JSONArray();
        String subtotalCol = "invname";
        String temp = "";
        String type="";
        boolean isInOut = StringUtil.isNullOrEmpty(request.getParameter("isInOut")) ? false : Boolean.parseBoolean(request.getParameter("isInOut"));
        if(isInOut){
            JSONObject  jSONObject1=new JSONObject();
            jSONObject1.put("taxcode", "PURCHASE");
            array.put(jSONObject1);
        }
        for (int i = 0; i < DataJArr.length(); i++) {
            JSONObject jSONObject = DataJArr.getJSONObject(i);
            String id = jSONObject.getString("taxcode");
            String taxtype=jSONObject.getString("taxtype");
            if (!StringUtil.isNullOrEmpty(temp)) {      // not first record
                if (id.equalsIgnoreCase(temp)) {        
                    array.put(jSONObject);
                } else {       
                    JSONObject jSONObject1 = new JSONObject();
                    jSONObject1.put(subtotalCol, "Subtotal");
                    double invamount=Double.parseDouble(DataJArr.getJSONObject(i-1).getString("totalsale"));
                    double invamountexcludingtax=Double.parseDouble(DataJArr.getJSONObject(i-1).getString("totalsaleexcludingtax"));
                    double taxamount=invamount-invamountexcludingtax;
                    jSONObject1.put("invamt", invamount);
                    jSONObject1.put("invtaxamount", taxamount);
                    jSONObject1.put("gramtexcludingtax",invamountexcludingtax);
                     array.put(jSONObject1);
                    temp=jSONObject.getString("taxcode");
                    if (taxtype.equalsIgnoreCase(type)) {
                        array.put(jSONObject);
                    }
                    
                }
                if(!taxtype.equalsIgnoreCase(type)){  //sales GrandTotal
                   JSONObject jSONObject1 = new JSONObject();
                    jSONObject1.put(subtotalCol, "GrandTotal");
                    jSONObject1.put("invamt", DataJArr.getJSONObject(i-1).getString("totalinvamt"));
                    jSONObject1.put("invtaxamount", DataJArr.getJSONObject(i-1).getString("totalcategorycost"));
                    jSONObject1.put("gramtexcludingtax", DataJArr.getJSONObject(i-1).getString("totalgramtexcludingtax"));
                    array.put(jSONObject1);  
                    type=jSONObject.getString("taxtype");
                     if (isInOut) {
                        JSONObject jSONObject2 = new JSONObject();
                        jSONObject2.put("taxcode", "Sales");
                        array.put(jSONObject2);
                    }
                    array.put(jSONObject);
                   
                   
                }
                if (i == DataJArr.length() - 1) {       // put last record with its total
                    JSONObject jSONObject1 = new JSONObject();
                    jSONObject1.put(subtotalCol, "Subtotal");
                    double invamount=Double.parseDouble(DataJArr.getJSONObject(i).getString("totalsale"));//ERP-36142
                    double invamountexcludingtax=Double.parseDouble(DataJArr.getJSONObject(i).getString("totalsaleexcludingtax"));//ERP-36142
                    double taxamount=invamount-invamountexcludingtax;
                    jSONObject1.put("invamt", invamount);
                    jSONObject1.put("invtaxamount",taxamount);
                    jSONObject1.put("gramtexcludingtax",invamountexcludingtax);
                    array.put(jSONObject1);
                   }
         } else {
                array.put(jSONObject);
                temp = jSONObject.getString("taxcode");
                type=jSONObject.getString("taxtype");
            }
        }
        JSONObject jSONObject1 = new JSONObject();//purchase GrandTotal
        jSONObject1.put(subtotalCol, "GrandTotal");
        jSONObject1.put("invamt", DataJArr.getJSONObject(DataJArr.length() - 1).getString("totalinvamt"));
        jSONObject1.put("invtaxamount", DataJArr.getJSONObject(DataJArr.length() - 1).getString("totalcategorycost"));
        jSONObject1.put("gramtexcludingtax", DataJArr.getJSONObject(DataJArr.length() - 1).getString("totalgramtexcludingtax"));
        array.put(jSONObject1);
      return array;
    }
    /**
     * @param isInOut
     * @param isSales
     * @param DataJArr
     * @return JSONArray
     * @desc Sub Total & grand total calculated separately.
     * @throws JSONException 
     */
    public static JSONArray calculateTotalForGSTReport(JSONArray DataJArr,boolean isInOut,boolean isSales) throws JSONException {
        JSONArray array = new JSONArray();
        String subtotalCol = "invname";
        String temp = "";
        if (isInOut) {
            JSONObject jSONObject1 = new JSONObject();
            jSONObject1.put("taxcode", isSales ? "Sales" : "Purchase");//ERP-9339
            array.put(jSONObject1);
        }
        double totalsale = 0;
        double totalsaleexcludingtax = 0;
        for (int i = 0; i < DataJArr.length(); i++) {
            JSONObject jSONObject = DataJArr.getJSONObject(i);
            String id = jSONObject.getString("taxcode");
            if (!StringUtil.isNullOrEmpty(temp)) {//Not first record
                if (id.equalsIgnoreCase(temp)) {
                    array.put(jSONObject);
                    totalsale += jSONObject.optDouble("invamt", 0);
                    totalsaleexcludingtax += jSONObject.optDouble("gramtexcludingtax", 0);
                } else {
                    JSONObject jSONObject1 = new JSONObject();
                    jSONObject1.put(subtotalCol, "Subtotal");
//                    double invamount = Double.parseDouble(DataJArr.getJSONObject(i - 1).getString("totalsale"));
//                    double invamountexcludingtax = Double.parseDouble(DataJArr.getJSONObject(i - 1).getString("totalsaleexcludingtax"));
//                    double taxamount = invamount - invamountexcludingtax;
//                    jSONObject1.put("invamt", invamount);
//                    jSONObject1.put("invtaxamount", taxamount);
//                    jSONObject1.put("gramtexcludingtax", invamountexcludingtax);
                    
                    jSONObject1.put("invamt", totalsale);
                    jSONObject1.put("invtaxamount", totalsale - totalsaleexcludingtax);
                    jSONObject1.put("gramtexcludingtax", totalsaleexcludingtax);
                    array.put(jSONObject1);
                    
                    temp = jSONObject.getString("taxcode");
                    array.put(jSONObject);
                    
                    totalsale = jSONObject.optDouble("invamt", 0);
                    totalsaleexcludingtax = jSONObject.optDouble("gramtexcludingtax", 0);
                }
                if (i == DataJArr.length() - 1) {
                    //Calculated subtotal of last record.
                    JSONObject jSONObject1 = new JSONObject();
                    jSONObject1.put(subtotalCol, "Subtotal");
//                    double invamount = Double.parseDouble(DataJArr.getJSONObject(i).getString("totalsale"));
//                    double invamountexcludingtax = Double.parseDouble(DataJArr.getJSONObject(i).getString("totalsaleexcludingtax"));
//                    double taxamount = invamount - invamountexcludingtax;
//                    jSONObject1.put("invamt", invamount);
//                    jSONObject1.put("invtaxamount", taxamount);
//                    jSONObject1.put("gramtexcludingtax", invamountexcludingtax);

                    jSONObject1.put("invamt", totalsale);
                    jSONObject1.put("invtaxamount", totalsale - totalsaleexcludingtax);
                    jSONObject1.put("gramtexcludingtax", totalsaleexcludingtax);
                    array.put(jSONObject1);

                    //Calculated GrandTotal.
                    jSONObject1 = new JSONObject();
                    jSONObject1.put(subtotalCol, "Grand Total");
                    jSONObject1.put("invamt", DataJArr.getJSONObject(i).getString("totalinvamt"));
                    jSONObject1.put("invtaxamount", DataJArr.getJSONObject(i).getString("totalcategorycost"));
                    jSONObject1.put("gramtexcludingtax", DataJArr.getJSONObject(i).getString("totalgramtexcludingtax"));
                    array.put(jSONObject1);
                }
            } else {
                array.put(jSONObject);//Put first record.
                temp = jSONObject.getString("taxcode");
                totalsale = jSONObject.optDouble("invamt", 0);
                totalsaleexcludingtax = jSONObject.optDouble("gramtexcludingtax", 0);
            }
        }
        return array;
    }
    
    public static JSONArray getConvertedJSONArray(JSONObject jobj, JSONArray getjArr) throws JSONException {
        JSONArray jArr = new JSONArray();
        for (int i = 0; i < getjArr.length(); i++) {
            JSONObject tempObj = getjArr.getJSONObject(i);
            if (tempObj.has("accountid")) {
                String key = tempObj.getString("accountid");
                if (jobj.has(key)) {
                    JSONObject putObj = jobj.getJSONObject(key);
                        jArr.put(putObj);
                }
            }
        }
        return jArr;
    }
    public static JSONArray getAccountsConvertedJSONArray(JSONObject paramJobj,JSONObject jobj, JSONArray getjArr,boolean isShowAllAccounts,String totalIndex) throws JSONException {
        JSONArray jArr = new JSONArray();
        int getjArrLength = getjArr.length();
        int monthCount = paramJobj.optInt("monthCount",-1);
        boolean monthYearFormat = paramJobj.optBoolean("monthYearFormat",false);
        boolean showZeroAmountAsBlank = paramJobj.optBoolean("showZeroAmountAsBlank", false);
        for (int i = 0; i < getjArrLength; i++) {
            JSONObject tempObj = getjArr.getJSONObject(i);
            if (tempObj.has("accountid")) {
                String key = tempObj.getString("accountid");
                if (jobj.has(key)) {
                    JSONObject putObj = jobj.getJSONObject(key);
                    Double total = putObj.optDouble(totalIndex);
                    int level = putObj.optInt("level");
                    if (monthCount != -1) {
                        if(tempObj.optBoolean("isaccountgroup", false)) {
                            putObj.put("amount_" + (monthCount), "");
                        } else {
                            putObj.put("amount_" + (monthCount), (putObj.optBoolean("isparent", false)) ? "" : (!showZeroAmountAsBlank || putObj.optDouble("totalamount", 0) != 0) ? putObj.optDouble("totalamount", 0) : "");
                        }
                        if (monthYearFormat && putObj.has("accountid") && (putObj.getString("accountid").equals(Constants.Finish_Products_account) || putObj.getString("accountid").equals(Constants.Raw_Materials_account) || putObj.getString("accountid").equals(Constants.Total_Closing_Stock_account))) {
                            //monthYearFormat flag is used to bypass this block when this function called from other report except monthly revenue ,monthly trading and profit loss.
                            //Total column for Closing Stock,Finish Products,Raw Materials entity will show amount in last month's column value and not the sum of all column's amount like other normal accounts.
                            if (!StringUtil.isNullOrEmpty(putObj.optString("amount_" + String.valueOf(monthCount - 1)))) {
                                putObj.put("amount_" + monthCount, (!showZeroAmountAsBlank || putObj.getDouble("amount_" + String.valueOf(monthCount - 1)) != 0) ? putObj.getDouble("amount_" + String.valueOf(monthCount - 1)) : "");
                            } else {
                                putObj.put("amount_" + monthCount, "");
                            }
                        }
                        if (monthYearFormat && putObj.has("accountid") && putObj.getString("accountid").equals(Constants.Opening_Stock_account)) {
                            //Total column for Opening Stock entity will show amount in first month's column value and not the sum of all column's amount like other normal accounts.
                            if (!StringUtil.isNullOrEmpty(putObj.getString("amount_0"))) {
                                putObj.put("amount_" + monthCount, (!showZeroAmountAsBlank || putObj.getDouble("amount_0") != 0) ? putObj.getDouble("amount_0") : "");
                            } else {
                                putObj.put("amount_" + monthCount, "");
                            }

                        }
                    }
                    
                    if (!isShowAllAccounts && putObj.optBoolean("accountflag", false)) {
                        
                        /*Put all accounts from account hierarchy.
                         *  haschild flag will be true only for accounts having child accounts. 
                         */
                        if (putObj.optBoolean("haschild", false)) {
                            if (!key.contains("Total")) {
                                jArr.put(putObj);
                                continue;
                            } else {
                                /**
                                 * When Total of account occurs then traverse
                                 * from total account to parent account and
                                 * remove accounts having zero balance
                                 *
                                 */
                                JSONObject reverseJobj = null;
                                for (int reverseCnt = jArr.length() - 1; reverseCnt >= 0; reverseCnt--) {
                                    reverseJobj = jArr.getJSONObject(reverseCnt);
                                    if(reverseJobj.has("ischecked") && reverseJobj.getBoolean("ischecked")){
                                        continue;
                                    }
                                    reverseJobj.put("ischecked",true);
                                    if (reverseJobj.optInt("level", 0) == level) {
                                        if (total == 0.0 ) {
                                            jArr.remove(reverseCnt);
                                        }
                                        break;
                                    } else {
                                        if (reverseJobj.optDouble(totalIndex, 0) == 0) {
                                            jArr.remove(reverseCnt);
//                                            reverseCnt--;
                                        }
                                    }
                                }
                            }
                        }
                        if (total != 0.0 || level == 0) {
                            jArr.put(putObj);
                        }
                    } else {
                        jArr.put(putObj);
                    }
                }
            }
        }
        return jArr;
    }
}
