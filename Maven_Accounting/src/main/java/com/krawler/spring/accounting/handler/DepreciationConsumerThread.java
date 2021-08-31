/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.handler;

import com.itextpdf.text.log.SysoLogger;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class DepreciationConsumerThread implements Runnable {
    
    private BlockingQueue<JSONObject> queue;
    private List<Map<String, Object>> list = new ArrayList();
    
    private HibernateTransactionManager txnManager;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private auditTrailDAO auditTrailObj;
    private accAccountDAO accAccountDAOobj;
    private fieldManagerDAO fieldManagerDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setAccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    

    public DepreciationConsumerThread(BlockingQueue<JSONObject> q) {
        this.queue = q;
    }

    public void add(Map<String, Object> requestParams) {
        try {
            list.clear();
            list.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(DepreciationConsumerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        
        try {
            Map<String, Object> requestParams = list.get(0);
            
            saveAssetDepreciation(requestParams);
                    
//            System.out.println("End : "+new Date());
            
        } catch (Exception ex) {
            Logger.getLogger(CompanySetupThread.class.getName()).log(Level.INFO, ex.getMessage());
        }
    }
    
    public void saveAssetDepreciation(Map<String, Object> requestParams) throws InterruptedException{
        List<AssetDetails> assetdetailList = new ArrayList();
        String companyid = "";
        DateFormat df = null;
        Calendar Cal = Calendar.getInstance();
        Date depreciationDate=null;
        String currencyid = "", costcenter = "", gcurrencyid = "", userfullname = "", reqHeader = "", remoteIPAddress = "", userid = "";
        DateFormat userdf = null;
        int postOption = 0;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("DCT_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        int count = 0;
        try {
            if (requestParams.containsKey("assetdetailList") && requestParams.get("assetdetailList") != null) {
                assetdetailList = (List<AssetDetails>) requestParams.get("assetdetailList");
            }
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            if (requestParams.containsKey("df") && requestParams.get("df") != null) {
                df = (DateFormat) requestParams.get("df");
            }
            if (requestParams.containsKey("Cal") && requestParams.get("Cal") != null) {
                Cal = (Calendar) requestParams.get("Cal");
            }
            if (requestParams.containsKey("currencyid") && requestParams.get("currencyid") != null) {
                currencyid = requestParams.get("currencyid").toString();
            }
            if (requestParams.containsKey("costcenter") && requestParams.get("costcenter") != null) {
                costcenter = requestParams.get("costcenter").toString();
            }
            if (requestParams.containsKey("gcurrencyid") && requestParams.get("gcurrencyid") != null) {
                gcurrencyid = requestParams.get("gcurrencyid").toString();
            }
            if (requestParams.containsKey("userdf") && requestParams.get("userdf") != null) {
                userdf = (DateFormat) requestParams.get("userdf");
            }
            if (requestParams.containsKey("postOption") && requestParams.get("postOption") != null) {
                postOption = Integer.parseInt(requestParams.get("postOption").toString());
            }
            
            if (requestParams.containsKey("userfullname") && requestParams.get("userfullname") != null) {
                userfullname = requestParams.get("userfullname").toString();
            }
            if (requestParams.containsKey("reqHeader") && requestParams.get("reqHeader") != null) {
                reqHeader = requestParams.get("reqHeader").toString();
            }
            if (requestParams.containsKey("remoteIPAddress") && requestParams.get("remoteIPAddress") != null) {
                remoteIPAddress = requestParams.get("remoteIPAddress").toString();
            }
            if (requestParams.containsKey("userid") && requestParams.get("userid") != null) {
                userid = requestParams.get("userid").toString();
            }
            
            Map<String, Object> auditRequestParams = new HashMap<>();
            auditRequestParams.put(Constants.reqHeader, reqHeader);
            auditRequestParams.put(Constants.remoteIPAddress, remoteIPAddress);
            auditRequestParams.put(Constants.useridKey, userid);
            
            String previousje="";
            String temppreviousje="";
            JSONObject jobj = null;
            while ((jobj = queue.take()) != null && !(jobj.has(Constants.END_OF_DEPRECIATION_QUEUE) && Boolean.parseBoolean(jobj.getString(Constants.END_OF_DEPRECIATION_QUEUE)))) {
                for (AssetDetails ad : assetdetailList) {
                    if (jobj.getString("assetDetailsId").equals(ad.getId()) && ad != null) {
                        int period = Integer.parseInt(StringUtil.DecodeText(jobj.optString("period")));
                        HashMap<String, Object> reqParams = new HashMap();
                        boolean isposted = false;
                        reqParams.put("period", period);
                        reqParams.put("id", ad.getId());
                        isposted = accProductObj.isDepreciationPosted(reqParams);
                        if(isposted){
                            continue;
                        }
                        count++;
                        if(count == Constants.BATCH_LIMIT){
                            count = 0;
                            txnManager.commit(status);
                            status = txnManager.getTransaction(def);
                        }
                        
                        String assetValue = ad.getAssetId();
                        String productId = jobj.getString("assetGroupId");
                        Product product = (Product) kwlCommonTablesDAOObj.getClassObject(Product.class.getName(), productId);
                        String accountid = "";
                        if (product.getDepreciationProvisionGLAccount() != null) {
                            accountid = product.getDepreciationProvisionGLAccount().getID();
                        } else {
                            accountid = product.getPurchaseAccount().getID();// this is containing value of Asset Controlling Account.
                        }
                        JSONArray customArray = new JSONArray();
                        JSONArray customGlobalArray = new JSONArray();
                        AssetDetailsCustomData assetDetailsCustomData = (AssetDetailsCustomData) kwlCommonTablesDAOObj.getClassObject(AssetDetailsCustomData.class.getName(), ad.getId());
                        if (assetDetailsCustomData != null) {
                            /*
                             * Create custom field data array for Journal Entry Details (Line Level)
                             */
                            int customColumn = 1;
                            HashMap<String, Object> params = new HashMap<String, Object>();
                            params.put("companyId", companyid);
                            params.put("assetId", ad.getId());
                            createCustomArray(params, customArray, customColumn);
                            
                            /*
                             * Create custom field data array for Journal Entry (Global Level)
                             */
                            customColumn = 0;
                            createCustomArray(params, customGlobalArray, customColumn);
                        }
                        double perioddepreciation = Double.parseDouble(StringUtil.DecodeText(jobj.optString("perioddepreciation")));
                        double perioddepreciationInBase = authHandler.round(perioddepreciation, companyid);
                        // Convert AMOUNT in base currency before posing JE.                    
                        String jeentryNumber = "";
                        String jeIntegerPart = "";
                        String jeSeqFormatId = "";
                        String jeDatePrefix = "";
                        String jeDateAfterPrefix = "";
                        String jeDateSuffix = "";
                        boolean jeautogenflag = false;
                        String date = df.format(Cal.getTime());
                        Date etryDate = null;
                        try {
                            etryDate = df.parse(date);
                        } catch (ParseException ex) {
                            etryDate = Cal.getTime();
                        }
                        synchronized (this) {
                            HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                            JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                            JEFormatParams.put("modulename", "autojournalentry");
                            JEFormatParams.put("companyid", companyid);
                            JEFormatParams.put("isdefaultFormat", true);

                            KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                            SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, etryDate);
                            jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            jeDatePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            jeDateAfterPrefix = (String) seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            jeDateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                            if (previousje.equals(jeIntegerPart)) {
                                jeIntegerPart = "" + (Integer.parseInt(temppreviousje) + 1);
                                String prefix = format.getPrefix();
                                String suffix = format.getSuffix();
                                int numberofdigit = format.getNumberofdigit();
                                boolean showleadingzero = format.isShowleadingzero();
                                String nextNumTemp = jeIntegerPart + "";
                                if (showleadingzero) {
                                    while (nextNumTemp.length() < numberofdigit) {
                                        nextNumTemp = "0" + nextNumTemp;
                                    }
                                }
                                jeentryNumber = jeDatePrefix + prefix + jeDateAfterPrefix + nextNumTemp + suffix + jeDateSuffix;
                                jeIntegerPart = nextNumTemp;
                                temppreviousje = jeIntegerPart;
                            } else {
                                jeentryNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //nexjeentryNumbert auto generated number
                                jeIntegerPart = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                previousje = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                                temppreviousje = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            }

                            jeSeqFormatId = format.getID();
                            jeautogenflag = true;
                        }
                        Map<String, Object> jeDataMap = new HashMap();//AccountingManager.getGlobalParams(request);
                        jeDataMap.put(Constants.df, df);
                        jeDataMap.put(Constants.globalCurrencyKey, gcurrencyid);
                        jeDataMap.put(Constants.companyKey, companyid);
                        jeDataMap.put(Constants.userdf, userdf);
                        jeDataMap.put("entrynumber", jeentryNumber);
                        jeDataMap.put("autogenerated", jeautogenflag);
                        jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                        jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                        jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                        jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                        jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                        jeDataMap.put("entrydate", etryDate);

                        String buildMsg = "";
                        if (jobj.has("fromyear") && !StringUtil.isNullOrEmpty(jobj.getString("fromyear"))) {
                            buildMsg = " for the Year " + jobj.getString("fromyear");
                        } else if (jobj.has("frommonth") && !StringUtil.isNullOrEmpty(jobj.getString("frommonth"))) {
                            try {
                                depreciationDate = df.parse(jobj.getString("frommonth"));
                                buildMsg = " for the month of  " + df.format(depreciationDate);
                            } catch (Exception ex) {
                                buildMsg = " for the month of  " + assetValue;
                            }
                        }

                        if (ad.isCreatedFromOpeningForm()) {
                            jeDataMap.put("memo", "Opening Depreciation for Asset ID " + assetValue + buildMsg);
                        } else {
                            jeDataMap.put("memo", "Fixed Asset Depreciation for Asset ID " + assetValue + buildMsg);
                        }
                        jeDataMap.put("currencyid", currencyid);
                        jeDataMap.put("costcenterid", costcenter);

                        HashSet jeDetails = new HashSet();
                        KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails

                        JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
                        String jeid = journalEntry.getID();
                        jeDataMap.put("jeid", jeid);

                        if (perioddepreciationInBase >= 0) {
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("amount", perioddepreciationInBase);
                            jedjson.put("accountid", accountid);
                            jedjson.put("debit", false);
                            jedjson.put("jeid", jeid);
                            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customArray", customArray);
                            customrequestParams.put("jedId", jed.getID());
                            customrequestParams.put("companyId", companyid);
                            saveJECustomData(customrequestParams);
                            jedjson = new JSONObject();
                            jedjson.put("srno", jeDetails.size() + 1);
                            jedjson.put("companyid", companyid);
                            jedjson.put("amount", perioddepreciationInBase);
                            jedjson.put("accountid", product.getDepreciationGLAccount().getID());
                            jedjson.put("debit", true);
                            jedjson.put("jeid", jeid);
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jeDetails.add(jed);
                            customrequestParams.put("jedId", jed.getID());
                            saveJECustomData(customrequestParams);
                        }

                        jeDataMap.put("jedetails", jeDetails);
                        jeDataMap.put("externalCurrencyRate", 0.0);

                        jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
                        
                        
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customArray", customGlobalArray);
                        customrequestParams.put("jeid", jeid);
                        customrequestParams.put("companyId", companyid);
                        saveJEGlobalCustomData(customrequestParams);//Save global level Dimension/Custom Field's value

                        HashMap<String, Object> ddMap = new HashMap<String, Object>();
                        ddMap.put("depreciationCreditToAccountId", accountid);
                        ddMap.put("depreciationGLAccountId", product.getDepreciationGLAccount().getID());
                        ddMap.put("productId", productId);
                        ddMap.put("assetId", ad.getId());
                        ddMap.put("period", period);
                        ddMap.put("companyid", companyid);
                        ddMap.put("jeid", jeid);
                        ddMap.put("periodamount", perioddepreciation);
                        ddMap.put("accamount", jobj.optDouble("accdepreciation", 0));
                        ddMap.put("netbookvalue", jobj.optDouble("netbookvalue", 0));

                        // add depreciation detail
                        accProductObj.addDepreciationDetail(ddMap);
                        ad.setPostOption(postOption);
                        /*
                         * Insert entry into Audit Trail
                         */
                        auditTrailObj.insertAuditLog(AuditAction.POSTED_DEPRECIATION, "User " + userfullname + " has Posted Depreciation for Asset ID " + assetValue + buildMsg +" with JE number "+jeentryNumber, auditRequestParams, assetValue);
                    }
                }
            }
            txnManager.commit(status);
        }catch (NumberFormatException | JSONException | ServiceException | AccountingException | TransactionException ex) { //| UnsupportedEncodingException
            txnManager.rollback(status);
            Logger.getLogger(CompanySetupThread.class.getName()).log(Level.INFO, ex.getMessage());
        }
    }
    
    public void createCustomArray(HashMap<String, Object> params, JSONArray customArray, int customColumn) {
        String companyId = "";
        String assetId = "";
        if (params.containsKey("companyId")) {
            companyId = params.get("companyId").toString();
        }
        if (params.containsKey("assetId")) {
            assetId = params.get("assetId").toString();
        }
        /*
        Get Line level custom fields of Journal Entry
        */
        HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
        requestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.customcolumn, "moduleid"));
        requestParams1.put(Constants.filter_values, Arrays.asList(companyId, customColumn, Constants.Acc_GENERAL_LEDGER_ModuleId));
        KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParams1);
        List<FieldParams> list = result.getEntityList();
        for (FieldParams fieldParams : list) {
            try {
                int col= fieldManagerDAOobj.getColumnFromFieldParams(fieldParams.getFieldlabel(),companyId,Constants.Acc_FixedAssets_Details_ModuleId,customColumn);
               /*
                Get column from Field Params for custom field
                If Field not present in Asset Deatils
                */
                if(col==0){
                    continue;
                }
                /*
                get Value from AssetCustom data 
                */
                String value = accProductObj.getfieldComboIdFromAssetDetail(assetId, "col" + col);
                if (!StringUtil.isNullOrEmpty(value)) {
                    if (fieldParams.getFieldtype() == 4 || fieldParams.getFieldtype() == 7) {
                        String[] valueArr = value.split(",");
                        String val = "";
                        for (int i = 0; i < valueArr.length; i++) {
                            FieldComboData comboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), valueArr[i]);
                            if (comboData != null) {
                                val += comboData.getValue() + ",";
                            }
                        }
                        val = val.substring(0, val.length() - 1);
                        if (!StringUtil.isNullOrEmpty(val)) {
                            value = fieldManagerDAOobj.getIdsUsingParamsValue(fieldParams.getId(), val);
                        }
                    }
                    JSONObject jSONObject = new JSONObject();
                    jSONObject.put("filedid", fieldParams.getId());
                    jSONObject.put("Custom_"+fieldParams.getFieldlabel(), "Col" + fieldParams.getColnum());
                    jSONObject.put("xtype", "" + fieldParams.getFieldtype());
                    jSONObject.put("refcolumn_name", "Col" + fieldParams.getRefcolnum());
                    jSONObject.put("Col" + fieldParams.getColnum(), value);
                    jSONObject.put("fieldname", "Custom_"+fieldParams.getFieldlabel());
                    customArray.put(jSONObject);
                }
            } catch (ServiceException ex) {
                Logger.getLogger(DepreciationConsumerThread.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                Logger.getLogger(DepreciationConsumerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void saveJECustomData(HashMap<String, Object> customrequestParams) throws JSONException {
        try {
            JSONArray customArray = null;
            String jedId = "";
            String companyId = "";
            if (customrequestParams.containsKey("customArray")) {
                customArray = new JSONArray(customrequestParams.get("customArray").toString());
            }
            if (customrequestParams.containsKey("jedId")) {
                jedId = customrequestParams.get("jedId").toString();
            }
            if (customrequestParams.containsKey("companyId")) {
                companyId = customrequestParams.get("companyId").toString();
            }
            if (customArray.length() > 0 && !StringUtil.isNullOrEmpty(jedId)) {
                customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", customArray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", jedId);
                customrequestParams.put("recdetailId", jedId);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyId);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    JSONObject jedjson1 = new JSONObject();
                    jedjson1.put("accjedetailcustomdata", jedId);
                    jedjson1.put("jedid", jedId);
                    KwlReturnObject jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson1);
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(DepreciationConsumerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void saveJEGlobalCustomData(HashMap<String, Object> customrequestParams) throws JSONException, AccountingException {
        try {
            JSONArray customArray = null;
            String jeId = "";
            String companyId = "";
            if (customrequestParams.containsKey("customArray")) {
                customArray = new JSONArray(customrequestParams.get("customArray").toString());
            }
            if (customrequestParams.containsKey("jeid")) {
                jeId = customrequestParams.get("jeid").toString();
            }
            if (customrequestParams.containsKey("companyId")) {
                companyId = customrequestParams.get("companyId").toString();
            }
            if (customArray.length() > 0 && !StringUtil.isNullOrEmpty(jeId)) {
                customrequestParams = new HashMap();
                customrequestParams.put("customarray", customArray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", jeId);
//                customrequestParams.put("recdetailId", jeId);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyId);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    AccJECustomData accJECustomData = (AccJECustomData) customDataresult.getEntityList().get(0);
                    Map<String, Object> dataMap = new HashMap();
                    dataMap.put("accjecustomdataref", accJECustomData.getJournalentryId());
                    dataMap.put("jeid", jeId);
                    KwlReturnObject jeresult = accJournalEntryobj.updateCustomFieldJournalEntry(dataMap);
                }
            }

        } catch (ServiceException ex) {
            Logger.getLogger(DepreciationConsumerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}