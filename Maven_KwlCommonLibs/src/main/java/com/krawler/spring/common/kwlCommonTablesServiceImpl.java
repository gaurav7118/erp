/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.common;

import com.krawler.common.admin.*;
import com.krawler.common.util.Constants;
import com.krawler.common.util.LandingCostAllocationType;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;

/**
 *
 * @author krawler
 */
public class kwlCommonTablesServiceImpl implements kwlCommonTablesService{
    private APICallHandlerService apiCallHandlerService;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    
  @Override  
    public JSONObject getAllTimeZones(JSONObject paramJObj) throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            kmsg = kwlCommonTablesDAOObj.getAllTimeZones();
            jobj = getAllTimeZonesJson(kmsg.getEntityList(), kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }  
    
  @Override  
    public JSONObject getAllTimeZonesJson(List<KWLTimeZone> timeZonelist, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try {
            for ( KWLTimeZone timeZone :timeZonelist) {
                JSONObject obj = new JSONObject();
                obj.put("id", timeZone.getTimeZoneID());
                obj.put("name", timeZone.getName());
                obj.put("difference", timeZone.getDifference());
                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
    
    public JSONObject getAllCurrencies(JSONObject paramJObj)throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            kmsg = kwlCommonTablesDAOObj.getAllTimeZones();
            jobj =getAllCurrenciesJson(kmsg.getEntityList(), kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
  
  
 @Override   
    public JSONObject getAllCurrenciesJson(List<KWLCurrency> currencylist, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try {
            for (KWLCurrency currency : currencylist) {
                JSONObject obj = new JSONObject();
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("symbol", currency.getSymbol());
                obj.put("currencyname", currency.getName());
                obj.put("htmlcode", currency.getHtmlcode());
                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
 
 @Override
    public JSONObject getAllCountries(JSONObject paramJObj)throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            kmsg = kwlCommonTablesDAOObj.getAllCountries();
            jobj =getAllCountriesJson(kmsg.getEntityList(), kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
 
  @Override  
    public JSONObject getAllCountriesJson(List<Country> countryList, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try {
            for (Country country : countryList) {
                JSONObject obj = new JSONObject();
                obj.put("id", country.getID());
                obj.put("name", country.getCountryName());
                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

 @Override    
    public JSONObject getAllStates(JSONObject paramJObj) throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        String countryid = !StringUtil.isNullOrEmpty(paramJObj.optString("countryid",null)) ?paramJObj.optString("countryid") : "";
        try {
            kmsg = kwlCommonTablesDAOObj.getAllStates(countryid);
            jobj = getAllStatesJson(kmsg.getEntityList(), kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
  
  
 @Override   
    public JSONObject getAllStatesJson(List<State> stateList, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try {
            for (State state : stateList) {
                JSONObject obj = new JSONObject();
                obj.put("id", state.getID());
                obj.put("name", state.getStateName());
                jarr.put(obj);
            }
            JSONObject Otherobj = new JSONObject();
            Otherobj.put("id", "1001");//setting this to max id.
            Otherobj.put("name", "Other");//We are keeping state "Other" in Json only, so no need to keep it in database. 
            jarr.put(Otherobj);
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
 
 @Override
    public JSONObject getSubdomainListFromCountry(JSONObject paramJObj) throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            String countryid = !StringUtil.isNullOrEmpty(paramJObj.optString("countryid",null)) ? paramJObj.optString("countryid") : "";
            kmsg = kwlCommonTablesDAOObj.getSubdomainListFromCountry(countryid);
            jobj = getSubdomainListFromCountryJson(kmsg.getEntityList(), kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
 
   @Override 
    public JSONObject getSubdomainListFromCountryJson(List<Company> companyList, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try {
            for (Company company : companyList) {
                JSONObject obj = new JSONObject();
                obj.put("id", company.getCompanyID());
                obj.put("name", company.getSubDomain());
                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
    
  @Override 
    public JSONObject getLandingCostCategory(JSONObject paramJObj) throws ServletException {
        KwlReturnObject kmsg = null;
        JSONObject jobj = new JSONObject();
        try {
            String companyid = paramJObj.optString(Constants.companyKey);
            kmsg = kwlCommonTablesDAOObj.getLandingCostCategoryStore(companyid);
            List<LandingCostCategory> listData = kmsg.getEntityList();
            jobj = getLandingCostCategoryJson(listData);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
  
  @Override 
    public JSONObject getSourceDocumentTermsInLinkingDocument(JSONObject paramJObj) throws ServletException {
        List linkingTermList = null;
        JSONObject jobj = new JSONObject();
        try {
            String companyid = paramJObj.optString(Constants.companyKey);
            int linkcombovalue = Integer.parseInt(paramJObj.optString("fromlinkcombovalue"));
            boolean isCustomer = paramJObj.optBoolean("isCustomer");
            boolean isFromReturn = paramJObj.optBoolean("isFromReturn",false);
            boolean isFromDelivery = paramJObj.optBoolean("isFromDelivery",false);
            String ids = paramJObj.optString("ids");
            
            if(linkcombovalue == 0){
                if(!isFromReturn){
                    if(isCustomer){// if sales side linking 
                        linkingTermList = kwlCommonTablesDAOObj.getCommonQueryForTermsInLinking(Constants.salesordertermmap,ids);
                    }else{// if purchase side linking
                        linkingTermList = kwlCommonTablesDAOObj.getCommonQueryForTermsInLinking(Constants.purchaseordertermmap,ids);
                    }
                }else{
                    if(isCustomer){// if sales side linking 
                        linkingTermList = kwlCommonTablesDAOObj.getCommonQueryForTermsInLinking(Constants.deliveryordertermmap,ids);
                    }else{// if purchase side linking
                        linkingTermList = kwlCommonTablesDAOObj.getCommonQueryForTermsInLinking(Constants.goodsreceiptordertermmap,ids);
                    }
                }
            }else if(linkcombovalue == 1){
                if(isFromDelivery || isFromReturn){
                    if(isCustomer){ // if sales side linking 
                        linkingTermList = kwlCommonTablesDAOObj.getCommonQueryForTermsInLinking(Constants.invoicetermsmap,ids);
                    }else{ // if purchase side linking
                        linkingTermList = kwlCommonTablesDAOObj.getCommonQueryForTermsInLinking(Constants.receipttermsmap,ids);
                    }
                }else{
                    if(isCustomer){// if sales side linking 
                        linkingTermList = kwlCommonTablesDAOObj.getCommonQueryForTermsInLinking(Constants.deliveryordertermmap,ids);
                    }else{// if purchase side linking
                        linkingTermList = kwlCommonTablesDAOObj.getCommonQueryForTermsInLinking(Constants.goodsreceiptordertermmap,ids);
                    }
                }
            }else if(linkcombovalue == 2){
                if(isCustomer){// if sales side linking 
                    linkingTermList = kwlCommonTablesDAOObj.getCommonQueryForTermsInLinking(Constants.quotationtermmap,ids);
                }else{// if purchase side linking
                    linkingTermList = kwlCommonTablesDAOObj.getCommonQueryForTermsInLinking(Constants.vendorquotationtermmap,ids);
                }
            }
            
            jobj = getTermDetailsJson(linkingTermList,companyid);
        } catch (Exception e) {
            Logger.getLogger(kwlCommonTablesServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return jobj;
    }
  
   
    public JSONObject getTermDetailsJson(List linkingTermList,String companyid) {
        JSONObject jObj = new JSONObject();
        try {
            Map<String, Object[]> tempRecordsForLinking = new HashMap<>();
            
            if (linkingTermList != null && !linkingTermList.isEmpty()) {
                Iterator termItr = linkingTermList.iterator();
                while (termItr.hasNext()) {
                    Object[] obj = (Object[]) termItr.next();
                    if (obj[0] != null) {
                        if (!tempRecordsForLinking.containsKey(obj[0].toString())) {
                            tempRecordsForLinking.put(obj[0].toString(), obj);
                        } else {
                            tempRecordsForLinking.remove(obj[0].toString());
                        }
                    }
                }
            }
            jObj.put("termDetails", getTermDetailsJsonArray(tempRecordsForLinking, companyid));
            
        } catch (Exception ex) {
            Logger.getLogger(kwlCommonTablesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jObj;
    }
  
    public JSONArray getTermDetailsJsonArray(Map<String,Object[]> tempRecordsForLinking,String companyid) {
       JSONArray jArr = new JSONArray();
       for (Map.Entry<String, Object[]> entry : tempRecordsForLinking.entrySet()) {
            try {
                JSONObject jObj = new JSONObject();
                Object [] obj = entry.getValue();
                 /* 
                  [0] : Term Id
                  [1] : Sum of Term Amount
                  [2] : Linked Term Tax Id
                  [3] : Sum of Term Tax Amount
                 */
                jObj.put("id",obj[0]);
                jObj.put("termamount",obj[1]);
                jObj.put("termtax",obj[2]);
                jObj.put("termtaxamount",obj[3]);
                jObj.put("termpercentage",0);
                
                if(obj[2] != null){
                    List ll = kwlCommonTablesDAOObj.getPercentAndTaxNameFromTaxid(obj[2].toString(), companyid);
                    if(ll != null && !ll.isEmpty()){
                        Iterator termItr = ll.iterator();
                        
                        while (termItr.hasNext()) {
                            Object[] taxObj = (Object[]) termItr.next();
                            jObj.put("linkedtaxpercentage",taxObj[0] != null ? (Double) taxObj[0] : 0);
                            jObj.put("linkedtaxname",taxObj[1]);
                        }
                    }
                }else{
                    jObj.put("linkedtaxpercentage",0);
                    jObj.put("linkedtaxname","");
                }
                
                jArr.put(jObj);
            } catch (Exception ex) {
                Logger.getLogger(kwlCommonTablesServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
       }
       
       return jArr;
    }
   
    @Override
    public JSONObject getLandingCostCategoryJson(List<LandingCostCategory> listData) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try {
            for (LandingCostCategory landingcostcategory : listData) {
                JSONObject obj = new JSONObject();
                obj.put("id", landingcostcategory.getId());
                obj.put("name", landingcostcategory.getLccName());
                obj.put("allocationtype", landingcostcategory.getLcallocationid());
                obj.put("allocationtypevalue", LandingCostAllocationType.getByValue(landingcostcategory.getLcallocationid()).name());
                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", jarr.length());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
    
    @Override
    public JSONObject getAllDateFormats(JSONObject paramJObj) throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            Map<String, Object> params = new HashMap();
            String[] moduleidarray = (String[]) paramJObj.opt("moduleidarray");
            if (moduleidarray != null) {
                String commaSepratedModuleids = "";
                for (int i = 0; i < moduleidarray.length; i++) {
                    if (!StringUtil.isNullOrEmpty(moduleidarray[i])) {
                        commaSepratedModuleids += moduleidarray[i] + ",";
                    }
                }
                if (commaSepratedModuleids.trim().endsWith(",")) {
                    commaSepratedModuleids = commaSepratedModuleids.substring(0, commaSepratedModuleids.length() - 1);
                }
                params.put("formatids", commaSepratedModuleids);
            }
            kmsg = kwlCommonTablesDAOObj.getAllDateFormats(params);
            jobj = getAllDateFormatsJson(kmsg.getEntityList(), paramJObj, kmsg.getRecordTotalCount());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
    
    
  @Override  
    public JSONObject getAllDateFormatsJson(List ll, JSONObject paramJObj, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                KWLDateFormat dateFormat = (KWLDateFormat) ite.next();
                JSONObject obj = new JSONObject();
                obj.put("formatid", dateFormat.getFormatID());
                obj.put("formalname", dateFormat.getName());
                if (paramJObj.optString("newDate") == "" || paramJObj.optString("newDate",null) == null) {
                    obj.put("name", getFormattedDate(new Date(), dateFormat.getJavaForm()));
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a"); // ERP-39313 SDP-14597 [Cristofori] Time is shown wrongly on Accounting
                    Date temp = formatter.parse(paramJObj.optString("newDate"));
                    obj.put("name", getFormattedDate(temp, dateFormat.getJavaForm()));
                }
                obj.put("javaform", dateFormat.getJavaForm());
                obj.put("scriptform", dateFormat.getScriptForm());
                jarr.put(obj);
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
    
   
    private static String getFormattedDate(Date curDate, String javaForm) {
        SimpleDateFormat sdf = new SimpleDateFormat(javaForm);
        return sdf.format(curDate);
    } 
    
    
  @Override  
    public JSONObject getAllInventoryStores(JSONObject paramJObj) throws ServletException {
        JSONObject jobj = new JSONObject();
        int totalRows = 0;
        JSONArray storeArray = new JSONArray();
        try {
            String companyid = paramJObj.optString(Constants.companyKey);

            String inventoryURL = paramJObj.optString(Constants.inventoryURL);
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", paramJObj.optString(Constants.useridKey));
            userData.put("companyid", companyid);
            userData.put("isStore", paramJObj.optString("isStore"));
            String action = "18";
            JSONObject resObj = apiCallHandlerService.callApp(inventoryURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                storeArray = resObj.getJSONArray("data");
                jobj.put("data", storeArray);
                jobj.put("success", true);
                jobj.put("count", storeArray.length());
            } else {
                jobj.put("success", false);
                jobj.put("count", totalRows);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Logger.getLogger(kwlCommonTablesController.class.getName()).log(Level.SEVERE, null, e);
        }
        return jobj;
    }
    
}
