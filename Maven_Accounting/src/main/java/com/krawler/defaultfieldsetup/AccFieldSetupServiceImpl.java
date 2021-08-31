/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.defaultfieldsetup;

import com.krawler.common.admin.DefaultHeader;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.masteritems.service.AccMasterItemsService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class AccFieldSetupServiceImpl implements AccFieldSetupServiceDao{

    private auditTrailDAO auditTrailObj;
    private AccFieldSetupDAO accFieldSetupDAOobj;
    private AccMasterItemsService accMasterItemsService;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accAccountDAO accAccountDAOobj;

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
     
    public void setAccFieldSetupDAOobj(AccFieldSetupDAO accFieldSetupDAOobj) {
        this.accFieldSetupDAOobj = accFieldSetupDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setaccMasterItemsService(AccMasterItemsService accMasterItemsService) {
        this.accMasterItemsService = accMasterItemsService;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    @Override
    public KwlReturnObject saveMobileFieldsConfigSettings(JSONObject paramJobj) throws ServiceException, JSONException {
        KwlReturnObject result = null;
        try {
            String data = paramJobj.optString(Constants.data);
            String type = paramJobj.optString(Constants.type);
            String companyid = paramJobj.getString(Constants.companyKey);
            String moduleid = paramJobj.getString(Constants.moduleid);

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.type, type);
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put(Constants.moduleid, moduleid);
            requestParams.put(Constants.data, data);
            result = accFieldSetupDAOobj.saveMobileConfigs(requestParams);
            //         CustomizeReportMapping crm = (CustomizeReportMapping) result.getEntityList().get(0);
//           auditTrailObj.insertAuditLog(AuditAction.ORDERING_CUSTOM_FIELDS_DIMENSION, "User " + paramJobj.optString(Constants.userfullname) + " has updated Ordering of Custom/Dimensions fields", request, crm.getId());

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveCustomizedReportFields : " + ex.getMessage(), ex);
        }
        return result;
    }

    /*
     * To get Single Field Config
     */
  @Override
    public JSONArray getMobileFieldsConfig(JSONObject paramJobj) {
        JSONArray fieldsArray = new JSONArray();
        JSONArray summaryjArray=new JSONArray();
        JSONArray detailjArray=new JSONArray();
        JSONArray addEditjArray=new JSONArray();
        
        try {
            String[] moduleIdArray = paramJobj.optString(Constants.moduleIds).split(",");
            if (moduleIdArray.length > 0) {
                for (String moduleid : moduleIdArray) {
                    JSONObject jobj = new JSONObject();
                    String companyid = paramJobj.getString(Constants.companyKey);
                    Map<String, Object> map = new HashMap<String, Object>();

                    /*
                     * Retrieving Mobile Field Obj
                     */
                    map.put(Constants.hqlquery, "from MobileFieldSetup mfs ");
                    map.put(Constants.filter_names, Arrays.asList("mfs.moduleid.id", "mfs.company.companyID"));
                    map.put(Constants.filter_values, Arrays.asList(moduleid, companyid));
                    KwlReturnObject mobilefieldresultObj = accFieldSetupDAOobj.getFieldsConfigObj(map);
                    List<MobileFieldSetup> mobilefieldObj = mobilefieldresultObj.getEntityList();

                    /*
                     * Retrieving Default Field Obj
                     */
                    map.clear();
                    map.put(Constants.hqlquery, "from DefaultMobileFieldSetUp dmfs ");
                    map.put(Constants.filter_names, Arrays.asList("dmfs.moduleid.id"));
                    map.put(Constants.filter_values, Arrays.asList(moduleid));
                    KwlReturnObject defaultresult = accFieldSetupDAOobj.getFieldsConfigObj(map);
                    List<DefaultMobileFieldSetUp> defFieldObj = defaultresult.getEntityList();

                    if (defFieldObj != null && !defFieldObj.isEmpty()) {
                        for (DefaultMobileFieldSetUp defaultmobfieldsetup : defFieldObj) {
                            if (!StringUtil.isNullOrEmpty(defaultmobfieldsetup.getSummaryreportjson())) {
                                summaryjArray = new JSONArray(defaultmobfieldsetup.getSummaryreportjson());
                            }
                            if (!StringUtil.isNullOrEmpty(defaultmobfieldsetup.getDetailreportjson())) {
                                detailjArray = new JSONArray(defaultmobfieldsetup.getDetailreportjson());
                            }
                            if (!StringUtil.isNullOrEmpty(defaultmobfieldsetup.getFormfieldjson())) {
                                addEditjArray = new JSONArray(defaultmobfieldsetup.getFormfieldjson());
                            }
                        }
                    }

                    if (mobilefieldObj != null && !mobilefieldObj.isEmpty()) {
                        for (MobileFieldSetup mobfieldsetup : mobilefieldObj) {

                            if (!StringUtil.isNullOrEmpty(mobfieldsetup.getSummaryreportjson())) {
                                summaryjArray = new JSONArray(mobfieldsetup.getSummaryreportjson());
                            }
                            if (!StringUtil.isNullOrEmpty(mobfieldsetup.getDetailreportjson())) {
                                detailjArray = new JSONArray(mobfieldsetup.getDetailreportjson());
                            }
                            if (!StringUtil.isNullOrEmpty(mobfieldsetup.getFormfieldjson())) {
                                addEditjArray = new JSONArray(mobfieldsetup.getFormfieldjson());
                            }
                        }
                    }
                    jobj.put(Constants.moduleid, moduleid);
                    jobj.put(Constants.SummaryView, summaryjArray);
                    jobj.put(Constants.DetailView, detailjArray);
                    jobj.put(Constants.AddEditView, addEditjArray);
                    fieldsArray.put(jobj);
                }//end of for
            }//end of moduleids
        } catch (Exception e) {
            Logger.getLogger(AccFieldSetupServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return fieldsArray;
    }
  
    @Override
    public JSONArray getBatchSerialsFieldsJsonArray(JSONArray ColumnConfigArr, String moduleid, JSONObject paramJobj) throws ServiceException {
        JSONArray jarrGlobaldefaultFieldColumns = new JSONArray();
        JSONArray jarrLineItemColumnFields = new JSONArray();
        JSONArray jarrbatchSerialColumnFields = new JSONArray();
        JSONObject response = new JSONObject();
        try {
            for (int i = 0; i < ColumnConfigArr.length(); i++) {
                JSONObject jobj = ColumnConfigArr.getJSONObject(i);
                jarrGlobaldefaultFieldColumns = jobj.getJSONArray(Constants.globalFields);
                jarrLineItemColumnFields = jobj.getJSONArray(Constants.lineItemFields);
            }
            
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            JSONObject levelsjson = accMasterItemsService.getLevels(requestParams);
            
            Map<String, String> lineitembatchSerialMap = batchSerialLineitemsFieldsMap(Constants.ModulesSetForAccountReceivable.contains(Integer.parseInt(moduleid)), paramJobj);
            if (lineitembatchSerialMap != null) {
                for (Map.Entry<String, String> extraColsEntry : lineitembatchSerialMap.entrySet()) {
                    JSONObject staticamountInfo = new JSONObject(extraColsEntry.getValue());
                    JSONObject jobject = new JSONObject();
                    jobject.put(Constants.Acc_fieldid, extraColsEntry.getKey());
                    jobject.put(Constants.fieldtype, staticamountInfo.get(Constants.xtype)); //TODO advance serch on combo,number,date fields
                    jobject.put(Constants.fieldlabel, staticamountInfo.get("label"));
                    jobject.put(Constants.islineitem, true);
                    jobject.put(Constants.iscustomflag, false);
                    jobject.put(Constants.isreadonly, false);
                    jobject.put(Constants.moduleid, moduleid);
                    jobject.put(Constants.xtype, staticamountInfo.get(Constants.xtype));
                    jobject.put(Constants.dataindex, staticamountInfo.get(Constants.dataindex));
                    jobject.put(Constants.ismandatory, staticamountInfo.get(Constants.ismandatory));
                    jobject.put(Constants.submoduleflag, 0);
                    
                    JSONArray levelJArray = levelsjson.getJSONArray(Constants.RES_data);
                    for (int count = 0; count < levelJArray.length(); count++) {
                        JSONObject jobj = levelJArray.getJSONObject(count);
                        if (jobj.has("levelName") && ((String) jobj.get("levelName")).equals((String) staticamountInfo.get("label"))) {
                            jobject.put("levelId", jobj.get("levelId"));
                            jobject.put("parent", jobj.get("parent"));
                        }
                    }
                    
                    if (staticamountInfo.has("store") && staticamountInfo.get("store") != null) {
                        jobject.put(extraColsEntry.getKey(), new JSONArray((String) staticamountInfo.get("store")));
                    }
                    jarrbatchSerialColumnFields.put(jobject);
                }
            }
            
        } catch (Exception e) {
            Logger.getLogger(AccFieldSetupServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                response.put(Constants.globalFields, jarrGlobaldefaultFieldColumns);
                response.put(Constants.lineItemFields, jarrLineItemColumnFields);
                response.put(Constants.batchSerialFields, jarrbatchSerialColumnFields);
                ColumnConfigArr = new JSONArray();
                ColumnConfigArr.put(response);
            } catch (JSONException ex) {
            }
        }
        return ColumnConfigArr;
    }

    //Map batch serial Fields
    public Map batchSerialLineitemsFieldsMap(boolean issalesFlag, JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException {

        HashMap<String, String> batchSerialsFieldsColumnConfig = new HashMap<String, String>();
        JSONObject response = new JSONObject();
        try {
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory()
                    || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level

                Map<String, Object> requestParams = new HashMap<>();
                requestParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
                if (preferences.isIsrowcompulsory()) {
                    requestParams.put("transType", "row");
                    response = accMasterItemsService.getStoreMasters(requestParams);
                    JSONArray rowJsonArray = response.getJSONArray("data");
                    batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.rowTypeFieldid), "{label:'Row',xtype:'4',ismandatory:'false',dataindex:'row',store:'" + rowJsonArray.toString() + "'}");
                }

                if (preferences.isIsrackcompulsory()) {
                    requestParams.put("transType", "rack");
                    response = accMasterItemsService.getStoreMasters(requestParams);
                    JSONArray rackJsonArray = response.getJSONArray("data");
                    batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.rackTypeFieldid), "{label:'Rack',xtype:'4',ismandatory:'false',dataindex:'rack',store:'" + rackJsonArray.toString() + "'}");
                }

                if (preferences.isIsbincompulsory()) {
                    requestParams.put("transType", "bin");
                    response = accMasterItemsService.getStoreMasters(requestParams);
                    JSONArray binJsonArray = response.getJSONArray("data");
                    batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.binTypeFieldid), "{label:'Bin',xtype:'4',ismandatory:'false',dataindex:'bin',store:'" + binJsonArray.toString() + "'}");
                }

                requestParams.put("isForCustomer", false);
                if (preferences.isIswarehousecompulsory()) {    //getWarehouse
                    response = accMasterItemsService.getWarehouseItems(requestParams);
                    JSONArray warehouseJsonArray = response.getJSONArray("data");
                    batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.warehouseTypeFieldid), "{label:'Warehouse',xtype:'4',ismandatory:'false',dataindex:'warehouse',store:'" + warehouseJsonArray.toString() + "'}");
                }

                if (preferences.isIslocationcompulsory()) { //getLocations
                    response = accMasterItemsService.getLocationItemsFromStore(requestParams);
                    JSONArray locationJsonArray = response.getJSONArray("data");
//                    batchSerialsFieldsColumnConfig.put(Constants.locationsTypeFieldid, "{label:'Locations',xtype:'4',ismandatory:'false',dataindex:'location',store:'" + locationJsonArray.toString() + "'}");
                    batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.locationsTypeFieldid), "{label:'Locations',xtype:'4',ismandatory:'false',dataindex:'location',store:'" + locationJsonArray.toString() + "'}");
                }

                if (preferences.isIsSerialCompulsory()) {//Serial
                    if (issalesFlag) {
                        batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.serialTypeFieldid), "{label:'Serial No',xtype:'4',ismandatory:'false',dataindex:'purchaseserialid'}");
                    } else {
                        batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.serialTypeFieldid), "{label:'Serial No',xtype:'1',ismandatory:'false',dataindex:'purchaseserialid'}");
                    }
                }

                if (preferences.isIsBatchCompulsory()) {//Batch
                    if (issalesFlag) {
                        batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.batchTypeFieldid), "{label:'Batch',xtype:'4',ismandatory:'false',dataindex:'purchasebatchid'}");
                    } else {
                        batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.batchTypeFieldid), "{label:'Batch',xtype:'1',ismandatory:'false',dataindex:'purchasebatchid'}");
                    }
                }

                //batchSerialsFieldsColumnConfig.put(Constants.stockfromFieldid, "{label:'Stock From',xtype:'4',ismandatory:'false',dataindex:'stockfrom',store:'" + discountArray.toString() + "'}");
                batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.expDateFieldid), "{label:'Exp Date',xtype:'3',ismandatory:'false',dataindex:'expdate'}");
                batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.mfgDateFieldid), "{label:'Mfg Date',xtype:'3',ismandatory:'false',dataindex:'mfgdate'}");
                batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.warrantyValidFromFieldid), "{label:'Warranty Valid From',xtype:'3',ismandatory:'false',dataindex:'expstart'}");
                batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.warrantyExpiresOnFieldid), "{label:'Warranty Expires On',xtype:'3',ismandatory:'false',dataindex:'expend'}");
                batchSerialsFieldsColumnConfig.put(Constants.availablequantityFieldid, "{label:'Available Quantity',xtype:'2',ismandatory:'false',dataindex:'avlquantity'}");
                batchSerialsFieldsColumnConfig.put(Constants.InventorybatchSerialfieldids.get(Constants.quantityFieldid), "{label:'Quantity',xtype:'2',ismandatory:'false',dataindex:'quantity'}");
            }//end of company preferences

        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return batchSerialsFieldsColumnConfig;
    }
  
  
 @Override  
    public Map getMandatoryFieldsDetails(String moduleid, String billid, String companyid, boolean isrecordflag, JSONObject paramJObj) throws ServiceException, JSONException {
        HashMap<String, Object> returnMap = new HashMap<>();
        boolean isvalidflag = true;
        
        StringBuilder buildMsg=new StringBuilder();
        try {
            if (isrecordflag) {// to check for existing saved record whether all transaction was saved with all mandatory fields.
                HashMap<String, Object> validationMap = accAccountDAOobj.validaterecorsingledHB(moduleid, billid, companyid);
                for (String key : validationMap.keySet()) {
                    String value = (String) validationMap.get(key);
                    if (StringUtil.isNullOrEmpty(value)) {
                        String msg="";
                        isvalidflag = false;
                        msg = "Mandatory field: " + key + " Field is missing.";
                        buildMsg.append(msg + "\n");// to print all the mandatory fields missing.
//                        throw ServiceException.FAILURE("Mandatory field:" + key + " is missing.\n", "", false);
                    }
                }
            } else {// to check for newly saving of transactions.It checks whether all fields are there in request parameters. It is important to have an entry in default_header_rest table else it will false each time
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(Constants.filter_names, Arrays.asList("module", "ismandatory"));
                map.put(Constants.filter_values, Arrays.asList(moduleid, 'T'));
                map.put("order_by", Arrays.asList("defaultHeader"));
                map.put("order_type", Arrays.asList("asc"));
                KwlReturnObject dhEmptyRefModule = accAccountDAOobj.getDefaultHeaders(map);
                if (dhEmptyRefModule != null) {
                    List<DefaultHeader> dhList = dhEmptyRefModule.getEntityList();
                    for (DefaultHeader defaultHeaderObj : dhList) {
                        String jsonKey =null;
                        String msg="";
                        KwlReturnObject jsonkeyobject = accAccountDAOobj.getJsonKeyMapping(defaultHeaderObj.getId());
                        List list = jsonkeyobject.getEntityList();
                        if (list.get(0) != null) {
                            jsonKey = (String) list.get(0);
                        }
                        String dbcolumnname = defaultHeaderObj.getDbcolumnname();

                        if (!StringUtil.isNullOrEmpty(jsonKey)) {
                            dbcolumnname = jsonKey;
                        }
                        if (!paramJObj.has(dbcolumnname) && StringUtil.isNullOrEmpty(paramJObj.optString(dbcolumnname))) {
                            isvalidflag = false;
                            msg="Mandatory field: " + defaultHeaderObj.getDefaultHeader() + " Field is missing.";
                            buildMsg.append(msg+"\n");// to print all the mandatory fields missing.
//                          throw ServiceException.FAILURE("Mandatory field: " + defaultHeaderObj.getDefaultHeader() + "Field is missing.", "", false);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(AccFieldSetupServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            isvalidflag = false;
        } finally {
            returnMap.put(Constants.RES_success, isvalidflag);
            returnMap.put(Constants.RES_msg, buildMsg.toString());
        }
        return returnMap;
    }
}
