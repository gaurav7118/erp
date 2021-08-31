/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.common;

import com.krawler.common.admin.DefaultHeader;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.JSONUtil;
import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;

/**
 *
 * @author krawler
 */
public class FieldManagerServiceImpl implements FieldManagerService{
    
    private fieldManagerDAO fieldManagerDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;

    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    
    /*
     * Get Column Configs of particular module
     */
    @Override
    public JSONArray getColumnHeadersConfigList(JSONObject paramJobj) throws ServiceException {
        JSONArray dataJArr = new JSONArray();
        JSONArray jarrGlobaldefaultFieldColumns = new JSONArray();
        JSONArray jarrLineItemColumnFields = new JSONArray();
        boolean displayUnitPriceAndAmountInSalesDocument=true;
        boolean displayUnitPriceAndAmountInPurchaseDocument=true;
        try {
            //Unit Price and Amount Permission
            JSONObject userPermObj=getUserPermissionsforUnitPriceAndAmount(paramJobj);
            displayUnitPriceAndAmountInSalesDocument=userPermObj.optBoolean(Constants.displayUnitPriceAndAmountInSalesDocument,true);
            displayUnitPriceAndAmountInPurchaseDocument=userPermObj.optBoolean(Constants.displayUnitPriceAndAmountInPurchaseDocument,true);
            
            //*****************ColumnModel*************************
            String moduleIdArray[] = paramJobj.optString(Constants.moduleIds).split(",");
            if (moduleIdArray.length > 0) {
                for (String moduleIdvalue : moduleIdArray) {
                    
//                    int moduleid = Integer.parseInt(moduleIdvalue);
                    String moduleid=moduleIdvalue;
                    //not allowing cash sales moduleid as cash sales and credit sales fields are same 
                    if (!StringUtil.isNullOrEmpty(moduleid) &&!moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Cash_Sales_ModuleId))) {
                    HashMap<String, Object> map = new HashMap<String, Object>();
                        map.put(Constants.filter_names, Arrays.asList("module", "allowinotherapplication"));
                        map.put(Constants.filter_values, Arrays.asList(moduleid, true));
                    map.put("order_by", Arrays.asList("defaultHeader"));
                    map.put("order_type", Arrays.asList("asc"));
                    KwlReturnObject headerResult = fieldManagerDAOobj.getDefaultHeaders(map);
                    List<DefaultHeader> headers = headerResult.getEntityList();

                        boolean isUnitPricenotActivated = false;
                        ExtraCompanyPreferences extraCompanyPreferences = null;
                        KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
                        if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                            extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                        }

                        boolean unitPriceinDO = extraCompanyPreferences.isUnitPriceInDO();
                        boolean unitPriceinGR = extraCompanyPreferences.isUnitPriceInGR();
                        boolean unitPriceinSR = extraCompanyPreferences.isUnitPriceInSR();
                        boolean unitPriceinPR = extraCompanyPreferences.isUnitPriceInPR();

                        if ((moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) && !unitPriceinSR) || (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Delivery_Order_ModuleId)) && !unitPriceinDO)
                                || (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId)) && !unitPriceinGR)
                                || (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Purchase_Return_ModuleId)) && !unitPriceinPR)) {
                            isUnitPricenotActivated = true;
                        }
                           
                    
                    for (DefaultHeader header : headers) { //these are header of main module
                        if (!StringUtil.isNullOrEmpty(header.getDataIndex()) && !header.isIslineitem() && !StringUtil.isNullOrEmpty(header.getXtype())) {
                            if (isUnitPricenotActivated && Constants.EnableDisableUnitPriceFields.contains(header.getId())) {
                                continue;
                            } else {
                                JSONObject jobj = new JSONObject();
                                jobj.put(Constants.Acc_fieldid, header.getId());
                                jobj.put(Constants.fieldtype, header.getXtype() != null ? Integer.parseInt(header.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                                jobj.put(Constants.fieldlabel, header.getDefaultHeader());
                                jobj.put(Constants.islineitem, false);
                                jobj.put(Constants.iscustomflag, false);
                                jobj.put(Constants.moduleid, header.getModule().getId());
                                jobj.put(Constants.xtype, header.getXtype());
                                jobj.put(Constants.dataindex, header.getDataIndex());
                                jobj.put(Constants.ismandatory, header.isMandatory());
                                jobj.put(Constants.isreadonly, header.isIsreadonly());
                                jobj.put(Constants.submoduleflag, header.getSubModuleFlag());
                                if (Constants.UNIT_PRICE_DISABLE_KEYS.contains(header.getDataIndex()) && ((Constants.SALES_MODULE_ID.contains(moduleid) && !displayUnitPriceAndAmountInSalesDocument) || (Constants.PURCHASE_MODULEID.contains(moduleid) && !displayUnitPriceAndAmountInPurchaseDocument))) {
                                    jobj.put(Constants.isreadonly, true);
                                }
                                jarrGlobaldefaultFieldColumns.put(jobj);
                            }
                            
                        } else if (!StringUtil.isNullOrEmpty(header.getDataIndex()) && header.isIslineitem()) {
                           
                            if (isUnitPricenotActivated && Constants.EnableDisableUnitPriceFields.contains(header.getId())) {
                                continue;
                            } else {
                                JSONObject jobj = new JSONObject();
                                jobj.put(Constants.Acc_fieldid, header.getId());
                                jobj.put(Constants.fieldtype, header.getXtype() != null ? Integer.parseInt(header.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                                jobj.put(Constants.fieldlabel, header.getDefaultHeader());
                                jobj.put(Constants.islineitem, true);
                                jobj.put(Constants.iscustomflag, false);
                                jobj.put(Constants.moduleid, header.getModule().getId());
                                jobj.put(Constants.xtype, header.getXtype());
                                jobj.put(Constants.dataindex, header.getDataIndex());
                                jobj.put(Constants.ismandatory, header.isMandatory());
                                jobj.put(Constants.isreadonly, header.isIsreadonly());
                                if (header.getDataIndex().equalsIgnoreCase("rate")) {
                                    Map<String, Object> request_Params = new HashMap<>();
                                    request_Params = JSONUtil.jsonToMap(paramJobj);                                    
                                    JSONObject priceFlagJson = fieldManagerDAOobj.GetUserAmendingPrice((HashMap) request_Params);
                                    if (priceFlagJson.has("SalesOrder") &&priceFlagJson.get("SalesOrder")!=null && moduleIdvalue.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                                        jobj.put(Constants.isreadonly, priceFlagJson.get("SalesOrder"));
                                    }else if (priceFlagJson.has("CInvoice") &&priceFlagJson.get("CInvoice")!=null && moduleIdvalue.equalsIgnoreCase(String.valueOf(Constants.Acc_Invoice_ModuleId))){
                                        jobj.put(Constants.isreadonly, priceFlagJson.get("CInvoice"));
                                    }                                     
                                }
                                if(Constants.UNIT_PRICE_DISABLE_KEYS.contains(header.getDataIndex()) && ((Constants.SALES_MODULE_ID.contains(moduleid) && !displayUnitPriceAndAmountInSalesDocument)||(Constants.PURCHASE_MODULEID.contains(moduleid) && !displayUnitPriceAndAmountInPurchaseDocument))){
                                    jobj.put(Constants.isreadonly, true);
                                }
                                
                                jobj.put(Constants.submoduleflag, header.getSubModuleFlag());
                                jarrLineItemColumnFields.put(jobj);
                            }
                        }
                    }

                    if ((!moduleid.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID))&& !isUnitPricenotActivated ) {// Other than Customer Moduleid because we don't need custom field information to show. Moduleid in String Format is creating problem

                        Map<String, String> globalOtherMap = otherGlobalLineitemsFieldsMap(false);
                        if (globalOtherMap != null) {
                            for (Map.Entry<String, String> extraColsEntry : globalOtherMap.entrySet()) {
                                JSONObject staticamountInfo = new JSONObject(extraColsEntry.getValue());
                                JSONObject jobj = new JSONObject();
                                jobj.put(Constants.Acc_fieldid, extraColsEntry.getKey());
                                jobj.put(Constants.fieldtype, staticamountInfo.get(Constants.xtype)); //TODO advance serch on combo,number,date fields
                                jobj.put(Constants.fieldlabel, staticamountInfo.get("label"));
                                jobj.put(Constants.islineitem, false);
                                jobj.put(Constants.iscustomflag, false);
                                jobj.put(Constants.isreadonly, false);
                                jobj.put(Constants.moduleid, moduleid);
                                jobj.put(Constants.xtype, staticamountInfo.get(Constants.xtype));
                                jobj.put(Constants.dataindex, staticamountInfo.get(Constants.dataindex));
                                jobj.put(Constants.ismandatory, staticamountInfo.get(Constants.ismandatory));
                                jobj.put(Constants.submoduleflag, 0);
                                if (staticamountInfo.has("store") && staticamountInfo.get("store") != null) {
                                    jobj.put(extraColsEntry.getKey(), new JSONArray((String) staticamountInfo.get("store")));
                                }
                                if (Constants.UNIT_PRICE_DISABLE_KEYS.contains(staticamountInfo.optString(Constants.dataindex)) && ((Constants.SALES_MODULE_ID.contains(moduleid) && !displayUnitPriceAndAmountInSalesDocument) || (Constants.PURCHASE_MODULEID.contains(moduleid) && !displayUnitPriceAndAmountInPurchaseDocument))) {
                                    jobj.put(Constants.isreadonly, true);
                                }
                                jarrGlobaldefaultFieldColumns.put(jobj);
                            }
                        }

                        Map<String, String> lineitemOtherMap = otherGlobalLineitemsFieldsMap(true);
                        if (globalOtherMap != null) {
                            for (Map.Entry<String, String> extraColsEntry : lineitemOtherMap.entrySet()) {
                                JSONObject staticamountInfo = new JSONObject(extraColsEntry.getValue());
                                JSONObject jobj = new JSONObject();
                                jobj.put(Constants.Acc_fieldid, extraColsEntry.getKey());
                                jobj.put(Constants.fieldtype, staticamountInfo.get(Constants.xtype)); //TODO advance serch on combo,number,date fields
                                jobj.put(Constants.fieldlabel, staticamountInfo.get("label"));
                                jobj.put(Constants.islineitem, true);
                                jobj.put(Constants.iscustomflag, false);
                                jobj.put(Constants.isreadonly, false);
                                jobj.put(Constants.moduleid, moduleid);
                                jobj.put(Constants.xtype, staticamountInfo.get(Constants.xtype));
                                jobj.put(Constants.dataindex, staticamountInfo.get(Constants.dataindex));
                                jobj.put(Constants.ismandatory, staticamountInfo.get(Constants.ismandatory));
                                jobj.put(Constants.submoduleflag, 0);
                                if (staticamountInfo.has("store") && staticamountInfo.get("store") != null) {
                                    jobj.put(extraColsEntry.getKey(), new JSONArray((String) staticamountInfo.get("store")));
                                }
                                if (Constants.UNIT_PRICE_DISABLE_KEYS.contains(staticamountInfo.optString(Constants.dataindex)) && ((Constants.SALES_MODULE_ID.contains(moduleid) && !displayUnitPriceAndAmountInSalesDocument) || (Constants.PURCHASE_MODULEID.contains(moduleid) && !displayUnitPriceAndAmountInPurchaseDocument))) {
                                    jobj.put(Constants.isreadonly, true);
                                }
                                jarrLineItemColumnFields.put(jobj);
                            }
                        }

                        /*
                         * Global Level Custom Fields
                         */
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        String companyid = (String) paramJobj.get(Constants.companyid);
                        /*
                         * Customcolumn=1-lineitem,0-Global Level;
                         * Customfield=0-Dimension,1-Custom Fields
                         */
                        int modules=Integer.parseInt(moduleid);
                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                        requestParams.put(Constants.filter_values, Arrays.asList(companyid, modules, 0));
                        getCustomFieldsJSON(jarrGlobaldefaultFieldColumns, jarrLineItemColumnFields, requestParams, true);

                        /*
                         * Line Level Custom Fields
                         */
                        requestParams.clear();
                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                        requestParams.put(Constants.filter_values, Arrays.asList(companyid, modules, 1));
                        getCustomFieldsJSON(jarrGlobaldefaultFieldColumns, jarrLineItemColumnFields, requestParams, false);
                        
                        /*
                         * Product Custom Fields
                         */
                        requestParams.clear();
                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                        requestParams.put(Constants.filter_values, Arrays.asList(companyid, 30, 0));
                        requestParams.put(Constants.moduleid,moduleid);
                        if (modules == Constants.Acc_Product_Master_ModuleId) {
                            requestParams.put("isProductmodule", true);
                        }
                        
//                        requestParams.put(Constants.relatedModuleId, String.valueOf(modules));
                        getCustomFieldsJSON(jarrGlobaldefaultFieldColumns, jarrLineItemColumnFields, requestParams, false);
                    }
                    JSONObject jobj = new JSONObject();
                    jobj.put(Constants.globalFields, jarrGlobaldefaultFieldColumns);
                    jobj.put(Constants.lineItemFields, jarrLineItemColumnFields);
                    dataJArr.put(jobj);

                 }//end of cash sales moduleid
                }//end of moduleIDValue 
            }//end of moduleID length

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataJArr;
    }

    /*
     * JSONArray of Custom Field Array
     */
    public void getCustomFieldsJSON(JSONArray jarrGlobaldefaultFieldColumns, JSONArray jarrLineItemColumnFields, HashMap<String, Object> requestParams, boolean isglobalfield) throws ServiceException {
        String moduleid = requestParams.get(Constants.moduleid) != null ? String.valueOf(requestParams.get(Constants.moduleid)) : "";
        requestParams.remove(Constants.moduleid);
        HashMap<String, Object> filterParams = new HashMap(requestParams);
        try {
            KwlReturnObject result = fieldManagerDAOobj.getFieldParams(filterParams);
            List<FieldParams> customlst = result.getEntityList();
            for (FieldParams tmpcontyp : customlst) {
                JSONObject jfieldobj = new JSONObject();
                String relatedModuleIds = tmpcontyp.getRelatedmoduleid() !=null ? tmpcontyp.getRelatedmoduleid():"";
                boolean isModulePresent = true;
                boolean isProductmodule=false; //flag to keep track whether module is of product module or not
                if (requestParams.containsKey("isProductmodule") && requestParams.get("isProductmodule") != null) {
                    isProductmodule = (Boolean) requestParams.get("isProductmodule");
                }
                
                if(!StringUtil.isNullOrEmpty(relatedModuleIds) && !isProductmodule){
                    isModulePresent =false;
                    String[] relatedModuleArray = relatedModuleIds.split(",");
                    for(String relatedModule : relatedModuleArray){
                        if(moduleid.equalsIgnoreCase(relatedModule)){
                            isModulePresent =true;
                            break;
                        }
                    }
                }else if(StringUtil.isNullOrEmpty(relatedModuleIds) && !moduleid.equals(String.valueOf(Constants.Acc_Product_Master_ModuleId)) && tmpcontyp.getModuleid()==Constants.Acc_Product_Master_ModuleId){
                    isModulePresent =false;
                }
                if (isModulePresent) {
                    jfieldobj.put(Constants.iscustomflag, true);//flag to detect whether it is a customfield or not customfield
                    jfieldobj.put(Constants.Acc_maxlength, tmpcontyp.getMaxlength());
                    jfieldobj.put(Constants.Acc_fieldid, tmpcontyp.getId());
                    jfieldobj.put(Constants.moduleid, tmpcontyp.getModuleid());
                    jfieldobj.put(Constants.fieldtype, tmpcontyp.getFieldtype());
                    jfieldobj.put(Constants.xtype, tmpcontyp.getFieldtype());
                    jfieldobj.put(Constants.dataindex, Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                    jfieldobj.put(Constants.ismandatory, tmpcontyp.getIsessential() == 0 ? false : true);
                    jfieldobj.put(Constants.fieldlabel, tmpcontyp.getFieldlabel());

                    jfieldobj.put(Constants.isdimension, tmpcontyp.getCustomfield() == 0 ? true : false);
                    jfieldobj.put(Constants.defaultId, tmpcontyp.getDefaultValue()!=null?tmpcontyp.getDefaultValue():"");
                    jfieldobj.put(Constants.isreadonly, false);
                    jfieldobj.put(Constants.submoduleflag,0);
                    jfieldobj.put("fieldname", tmpcontyp.getFieldname());
                    jfieldobj.put("refcolumn_name", Constants.Custom_Column_Prefix + tmpcontyp.getRefcolnum());
                    jfieldobj.put("column_number", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                    jfieldobj.put("sequence", tmpcontyp.getSequence());
                    jfieldobj.put("fieldtooltip", tmpcontyp.getFieldtooltip() == null ? "" : tmpcontyp.getFieldtooltip());
                    jfieldobj.put("validationtype", tmpcontyp.getValidationtype());
                    jfieldobj.put("comboid", tmpcontyp.getComboid());
                    jfieldobj.put("comboname", tmpcontyp.getComboname());
                    jfieldobj.put("moduleflag", tmpcontyp.getModuleflag());
                    jfieldobj.put("parentid", tmpcontyp.getParentid());
                    jfieldobj.put("sendnotification", tmpcontyp.getsendNotification());
                    jfieldobj.put("notificationdays", tmpcontyp.getnotificationDays());

                    if (isglobalfield) {
                        jfieldobj.put(Constants.islineitem,false);
                        jarrGlobaldefaultFieldColumns.put(jfieldobj);
                    } else {
                        jfieldobj.put(Constants.islineitem, true);
                        jarrLineItemColumnFields.put(jfieldobj);
                    }
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
    
    //Map other Fields
    public Map otherGlobalLineitemsFieldsMap(boolean islineitem) throws ServiceException {
        HashMap<String, String> otherFieldsCOlumnConfig = new HashMap<String, String>();
        try {
            
            JSONArray includeProArray = new JSONArray();
            JSONObject jobj = new JSONObject();
            jobj.put("id", "false");
            jobj.put("name", "No");
            jobj.put("value", false);
            includeProArray.put(jobj);
            jobj = new JSONObject();
            jobj.put("id", "true");
            jobj.put("name", "Yes");
            jobj.put("value", true);
            includeProArray.put(jobj);
            
            JSONArray discountArray = new JSONArray();
            jobj = new JSONObject();
            jobj.put("id", "1");
            jobj.put("name", "Percentage");
            jobj.put("value", true);
            discountArray.put(jobj);
            jobj = new JSONObject();  
            jobj.put("id", "0");
            jobj.put("name", "Flat");
            jobj.put("value", false);
            discountArray.put(jobj);
            
            String includeproString = includeProArray.toString();
            if (islineitem) {
                otherFieldsCOlumnConfig.put(Constants.discountTypeFieldid, "{label:'Discount Type',xtype:'4',ismandatory:'true',dataindex:'discountType',store:'" + discountArray.toString() + "'}");
                //otherFieldsCOlumnConfig.put("7b28437e0-f6-334de444-3354356", "{label:'Include GST',xtype:'12',ismandatory:'true',dataindex:'gstIncluded'}");
            } else {
                otherFieldsCOlumnConfig.put(Constants.includeProductTaxFieldid, "{label:'Include Product Tax',xtype:'4',ismandatory:'true',dataindex:'includeprotax',store:'" + includeproString + "'}");

            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return otherFieldsCOlumnConfig;
    }  
    
    public JSONObject getUserPermissionsforUnitPriceAndAmount(JSONObject paramJObj) throws ServiceException {
        JSONObject requestJSON = paramJObj;
        try {
            //User Permission for display Unit Price and Amount
            boolean displayUnitPriceAndAmountInSalesDocument = true;
            boolean displayUnitPriceAndAmountInPurchaseDocument = true;
            long unitPriceAndAmountSalesPermission = 0;
            long unitPriceAndAmountPurchasePermission = 0;
            long unitPriceAndAmountPermission = 0;

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("userid", requestJSON.optString(Constants.useridKey));
            JSONObject userPermJobj = accountingHandlerDAOobj.getUserPermissionForFeature(params);
            if (userPermJobj.has("Perm") && userPermJobj.getJSONObject("Perm") != null && userPermJobj.getJSONObject("Perm").length() > 0) {
                JSONObject permJobj = userPermJobj.getJSONObject("Perm");
                if (permJobj.has("unitpriceandamount") && permJobj.getJSONObject("unitpriceandamount") != null && permJobj.getJSONObject("unitpriceandamount").length() > 0) {
                    unitPriceAndAmountSalesPermission = permJobj.getJSONObject("unitpriceandamount").optLong("displayunitpriceandamountinsalesdocument", 0);
                    unitPriceAndAmountPurchasePermission = permJobj.getJSONObject("unitpriceandamount").optLong("displayunitpriceandamountinpurchasedocument", 0);
                }
            }
            if (userPermJobj.has("UPerm") && userPermJobj.getJSONObject("UPerm") != null && userPermJobj.getJSONObject("UPerm").length() > 0) {
                unitPriceAndAmountPermission = userPermJobj.getJSONObject("UPerm").getLong("unitpriceandamount");
            }

            if ((unitPriceAndAmountPermission & unitPriceAndAmountSalesPermission) == unitPriceAndAmountSalesPermission) {
                displayUnitPriceAndAmountInSalesDocument = false;
            }
            if ((unitPriceAndAmountPermission & unitPriceAndAmountPurchasePermission) == unitPriceAndAmountPurchasePermission) {
                displayUnitPriceAndAmountInPurchaseDocument = false;
            }
            requestJSON.put(Constants.displayUnitPriceAndAmountInSalesDocument, !displayUnitPriceAndAmountInSalesDocument);
            requestJSON.put(Constants.displayUnitPriceAndAmountInPurchaseDocument, !displayUnitPriceAndAmountInPurchaseDocument);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Exception occurred while populating masters information", "erp23", false);
        }
        return requestJSON;
    }
    
    
}
