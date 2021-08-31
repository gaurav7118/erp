/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.journalentry;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.inventory.AccImportService;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.uom.accUomDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class ImportJournalEntry implements Runnable,MessageSourceAware {

    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;

    public boolean isIsworking() {
        return isworking;
    }

    public void setIsworking(boolean isworking) {
        this.isworking = isworking;
    }

    private HibernateTransactionManager txnManager;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private accJournalEntryDAO accJournalEntryobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accAccountDAO accAccountDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private authHandlerDAO authHandlerDAOObj;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private accProductDAO accProductObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setAccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

   public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    
    public void add(HashMap<String, Object> requestParams) {
        try {
            processQueue.add(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     public String addPendingImportLog(HashMap<String, Object> requestParams) {
        String logId = null;
        try {
            //Insert Integration log
            String fileName = (String) requestParams.get("filename");
            String Module = (String) requestParams.get("modName");
            try {
                List list = importDao.getModuleObject(Module);
                Modules module = (Modules) list.get(0); //Will throw null pointer if no module entry found
                Module = module.getId();
            } catch (Exception ex) {
                throw new DataInvalidateException("Column config not available for module " + Module);
            }
            HashMap<String, Object> logDataMap = new HashMap<String, Object>();
            logDataMap.put("FileName", ImportLog.getActualFileName(fileName));
            logDataMap.put("StorageName", fileName);
            logDataMap.put("Log", "Pending");
            logDataMap.put("Type", fileName.substring(fileName.lastIndexOf(".") + 1));
            logDataMap.put("Module", Module);
            logDataMap.put("ImportDate", new Date());
            logDataMap.put("User", (String) requestParams.get("userid"));
            logDataMap.put("Company", (String) requestParams.get("companyid"));
            ImportLog importlog = (ImportLog) importDao.saveImportLog(logDataMap);
            logId = importlog.getId();
        } catch (Exception ex) {
            logId = null;
        }
        return logId;
    }

    @Override
    public void run() {

        try {
            while (!processQueue.isEmpty() && !isworking) {
                this.isworking = true;
                HashMap<String, Object> requestParams1 = (HashMap<String, Object>) processQueue.get(0);
                try {
                    JSONObject jobj = new JSONObject();
                    String modulename = requestParams1.get("modName").toString();
                    String importflag = requestParams1.get("importflag").toString();

                 
                    JSONObject datajobj = (JSONObject) requestParams1.get("jobj");
                    if (importflag.equalsIgnoreCase(Constants.importproductcsv)) {
                        jobj = importJournalEntryRecordsForAll(requestParams1, datajobj);
                    }
                    User user = (User) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.User", requestParams1.get("userid").toString());
                    Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), user.getCompany().getCompanyID());
                    String htmltxt = "Report for data imported.<br/>";
                    htmltxt += "<br/>Module Name: " + modulename + "<br/>";
                    htmltxt += "<br/>File Name: " + jobj.get("filename") + "<br/>";
                    htmltxt += "Total Records: " + jobj.get("totalrecords") + "<br/>";
                    htmltxt += "Records Imported Successfully: " + jobj.get("successrecords");
                    htmltxt += "<br/>Failed Records: " + jobj.get("failedrecords");
                    htmltxt += "<br/><br/>Please check the import log in the system for more details.";
                    htmltxt += "<br/>For queries, email us at support@deskera.com<br/>";
                    htmltxt += "Deskera Team";

                    String plainMsg = "Report for data imported.\n";
                    plainMsg += "\nModule Name: " + modulename + "\n";
                    plainMsg += "\nFile Name:" + jobj.get("filename") + "\n";
                    plainMsg += "Total Records: " + jobj.get("totalrecords");
                    plainMsg += "\nRecords Imported Successfully: " + jobj.get("successrecords");
                    plainMsg += "\nFailed Records: " + jobj.get("failedrecords");
                    plainMsg += "\n\nPlease check the import log in the system for more details.";

                    plainMsg += "\nFor queries, email us at support@deskera.com\n";
                    plainMsg += "Deskera Team";
                    String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(new String[]{user.getEmailID()}, "Deskera Accounting - Report for data imported", htmltxt, plainMsg, fromEmailId, smtpConfigMap);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    processQueue.remove(requestParams1);
                    this.isworking = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public JSONObject importJournalEntryRecordsForAll( HashMap<String, Object> requestParams1, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String currencyId = (String)requestParams1.get("currencyId");
        String companyid = (String)requestParams1.get("companyid");
        String userId = (String)requestParams1.get("userId");
        String fileName = jobj.getString("filename");
        String masterPreference = (String)requestParams1.get("masterPreference");
        Locale locale= (java.util.Locale)requestParams1.get("locale");
        String prevJENo = "";
        boolean isAlreadyExist = false;
        boolean isRecordFailed = false;
        String customfield = "";

        JSONObject returnObj = new JSONObject();

        try {
            HashMap<String, Object> jeDataMap = (HashMap<String, Object>)requestParams1.get("jeDataMap");
            JSONArray jeDetailArr = new JSONArray();
            String[] recarr = null;
            
            StringBuilder failureMsg = new StringBuilder();

            String dateFormat = null, dateFormatId =(String) requestParams1.get("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {

                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            Boolean isCurrencyCode=extrareferences.isCurrencyCode();
            DateFormat df = new SimpleDateFormat(dateFormat);
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();
            StringBuilder failedPrevRecords = new StringBuilder();

            HashMap<String, Integer> columnConfig = new HashMap<String, Integer>();
            JSONArray jSONArray = jobj.getJSONArray("resjson");
//            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
//                headArrayList.add(jSONObject.get("csvheader"));
                columnConfig.put(jSONObject.getString("dataindex"), jSONObject.getInt("csvindex"));
            }

//            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            HashMap currencyMap = getCurrencyMap(isCurrencyCode);

            while ((record = br.readLine()) != null) {
                recarr = record.split(",");
                failureMsg.setLength(0);
                if (cnt == 0) {
                    failedRecords.append(createCSVrecord(recarr) + "\"Error Message\"");
                }
                if (cnt != 0) {
                    try {
                        currencyId =  (String)requestParams1.get("currencyId");

                        String entryNo = "";
                        if (columnConfig.containsKey("entryno")) {
                            entryNo = recarr[(Integer) columnConfig.get("entryno")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(entryNo)) {
                                entryNo = entryNo.replaceAll("\"", "");
                            } else {
                                failureMsg.append("Entry Number is not available.");
                            }
                        } else {
                            failureMsg.append("Entry Number column is not found.");
                        }

                        Date entryDate = null;
                        if (columnConfig.containsKey("entrydate")) {
                            String entryDateStr = recarr[(Integer) columnConfig.get("entrydate")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(entryDateStr)) {
                                failureMsg.append("Entry Date is not available");
                            } else {
                                entryDate = df.parse(entryDateStr);
                            }
                        } else {
                            failureMsg.append("Entry Date column is not found.");
                        }

                        String memo = "";
                        if (columnConfig.containsKey("memo")) {
                            memo = recarr[(Integer) columnConfig.get("memo")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(memo)) {
                                memo = memo.replaceAll("\"", "");
                            }
                        }

                        String description = "";
                        if (columnConfig.containsKey("description")) {
                            description = recarr[(Integer) columnConfig.get("description")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(description)) {
                                description = description.replaceAll("\"", "");
                            }
                        }

                        String accountID = "";
                        if (columnConfig.containsKey("accountcode")) {
                            String accountCode = recarr[(Integer) columnConfig.get("accountcode")].replaceAll("\"", "").trim();
                            if (!StringUtil.isNullOrEmpty(accountCode)) {
                                Account account = getAccount(accountCode, companyid, false);
                                if (account != null) {
                                    accountID = account.getID();
                                    if (!StringUtil.isNullOrEmpty(account.getUsedIn()) && !account.isWantToPostJe()) {
                                        failureMsg.append("The account code (" + accountCode + ") is a control account. " + messageSource.getMessage("acc.JournalEntry.import.ControlAccount", null, locale));
                                    }
                                } else {
                                    failureMsg.append("Account Code is not found for " + accountCode);
                                }
                            } else {
                                if (columnConfig.containsKey("accountname")) {
                                    String accountName = recarr[(Integer) columnConfig.get("accountname")].replaceAll("\"", "").trim();
                                    if (!StringUtil.isNullOrEmpty(accountName)) {
                                        Account account = getAccount(accountName, companyid, true);
                                        if (account != null) {
                                            accountID = account.getID();
                                            if (!StringUtil.isNullOrEmpty(account.getUsedIn()) && !account.isWantToPostJe()) {
                                                failureMsg.append("The account name (" + accountName + ") is a control account. " + messageSource.getMessage("acc.JournalEntry.import.ControlAccount", null, locale));
                                            }
                                        } else {
                                            failureMsg.append("Account Name is not found for " + accountName);
                                        }
                                    } else {
                                        failureMsg.append("Account is not available");
                                    }
                                } else {
                                    failureMsg.append("Account is not available");
                                }
                            }
                        } else {
                            failureMsg.append("Account Code column is not found.");
                        }

                       if (isCurrencyCode?columnConfig.containsKey("currencyCode"):columnConfig.containsKey("currencyName")) {
                            String currencyStr = isCurrencyCode?recarr[(Integer) columnConfig.get("currencyCode")].replaceAll("\"", "").trim():recarr[(Integer) columnConfig.get("currencyName")].replaceAll("\"", "").trim();
                             if (!StringUtil.isNullOrEmpty(currencyStr)) {
                                currencyId = getCurrencyId(currencyStr, currencyMap);

                                if (StringUtil.isNullOrEmpty(currencyId)) {
                                    failureMsg.append(messageSource.getMessage("acc.field.ImportProductExceptionFormat", null, locale));
                                }
                            } else {
                                if (!masterPreference.equalsIgnoreCase("1")) {
                                    failureMsg.append("Currency is not available.");
                                }
                            }
                        }

                        String debitAmount = "";
                        if (columnConfig.containsKey("d_amount")) {
                            debitAmount = recarr[(Integer) columnConfig.get("d_amount")].replaceAll("\"", "").trim();
                            if (StringUtil.isNullOrEmpty(debitAmount)) {
                                failureMsg.append("Debit Amount is not available");
                            }
                        } else {
                            failureMsg.append("Debit Amount column is not found.");
                        }

                        String creditAmount = "";
                        if (columnConfig.containsKey("c_amount")) {
                            creditAmount = recarr[(Integer) columnConfig.get("c_amount")].replaceAll("\"", "").trim();

                            if (StringUtil.isNullOrEmpty(creditAmount)) {
                                failureMsg.append("Credit Amount is not available");
                            }
                        } else {
                            failureMsg.append("Credit Amount column is not found.");
                        }

                        if (Double.parseDouble(debitAmount) > 0 && Double.parseDouble(creditAmount) > 0) {
                            failureMsg.append("Amount in both Debit and Credit columns are not allowed for " + entryNo);
                        }

                        String exchangeRate = "0.0";
                        if (columnConfig.containsKey("exchangeRate")) {
                            exchangeRate = recarr[(Integer) columnConfig.get("exchangeRate")].replaceAll("\"", "").trim();
                        }

                        isAlreadyExist = false;
                        KwlReturnObject result = accJournalEntryobj.getJECount(entryNo, companyid);
                        int nocount = result.getRecordTotalCount();
                        if (nocount > 0) {
                            isAlreadyExist = true;
                            failureMsg.append("Journal entry number '" + entryNo + "' already exists.");
                        }

                        // In case of NA checks wheather this number can also be generated by a sequence format or not
                        List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_GENERAL_LEDGER_ModuleId, entryNo, companyid);
                        if (!list.isEmpty()) {
                            boolean isvalidEntryNumber = (Boolean) list.get(0);
                            String formatName = (String) list.get(1);
                            if (!isvalidEntryNumber) {
                                failureMsg.append(messageSource.getMessage("acc.common.enterdocumentnumber", null,locale) + " " + entryNo + " " + messageSource.getMessage("acc.common.belongsto", null, locale) + " " + formatName + ". ");
                            }
                        }

                        if (!prevJENo.equalsIgnoreCase(entryNo)) {
                            prevJENo = entryNo;

                            // for saving JE
                            if (jeDetailArr.length() > 0 && !isRecordFailed) {
                                try {
                                    double c_Amount = 0;
                                    double d_Amount = 0;
                                    for (int jedCnt = 0; jedCnt < jeDetailArr.length(); jedCnt++) {
                                        JSONObject jeDetailObj = jeDetailArr.getJSONObject(jedCnt);
                                        if (jeDetailObj.getBoolean("debit")) {
                                            d_Amount += authHandler.round(jeDetailObj.getDouble("amount"), companyid);
                                        } else {
                                            c_Amount += authHandler.round(jeDetailObj.getDouble("amount"), companyid);
                                        }
                                    }
                                    d_Amount = authHandler.round(d_Amount, companyid);
                                    c_Amount = authHandler.round(c_Amount, companyid);

                                    if (d_Amount != c_Amount) {
                                        failureMsg.append(messageSource.getMessage("acc.msgbox.25", null, locale));
                                    }

                                    saveJE(requestParams1, jeDataMap, jeDetailArr, customfield);
                                } catch (Exception ex) {
                                    failed++;
                                    String errorMsg = ex.getMessage(), invalidColumns = "";
                                    try {
                                        JSONObject errorLog = new JSONObject(errorMsg);
                                        errorMsg = errorLog.getString("errorMsg");
                                        invalidColumns = errorLog.getString("invalidColumns");
                                    } catch (JSONException jex) {
                                    }
                                    failedPrevRecords.append(errorMsg.replaceAll("\"", ""));
                                    failedRecords.append(failedPrevRecords);
                                }
                            }

                            isRecordFailed = false;
                            jeDetailArr = new JSONArray();
                            jeDataMap =(HashMap<String, Object>)requestParams1.get("jeDataMap");

                            jeDataMap.put("companyid", companyid);
                            jeDataMap.put("createdby", userId);
                            jeDataMap.put("entrydate", entryDate);
                            jeDataMap.put("memo", memo);
                            jeDataMap.put("entrynumber", entryNo);
                            //System.out.println("Entry No: "+entryNo);
                            jeDataMap.put("autogenerated", false);
                            jeDataMap.put("currencyid", currencyId);
                            jeDataMap.put("externalCurrencyRate", (Double.parseDouble(exchangeRate) > 0) ? (1 / Double.parseDouble(exchangeRate)) : 0.0); // externalCurrencyRate = 1/exchangeRate  => For Other Currency to Base Currency exchange rate.
                            jeDataMap.put("typevalue", 1);
                            jeDataMap.put("istemplate", 0);

                            // For create custom field array
                            JSONArray customJArr = new JSONArray();
                            for (int i = 0; i < jSONArray.length(); i++) {
                                JSONObject jSONObject = jSONArray.getJSONObject(i);

                                if (jSONObject.optBoolean("customflag", false) && !jSONObject.optBoolean("isLineItem", false)) {
                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, jSONObject.getString("columnname")));

                                    KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                    FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                    if (jSONObject.getInt("csvindex") > recarr.length - 1) { // (csv) arrayindexoutofbound when last custom column value is empty.
                                        continue;
                                    }

                                    if (!StringUtil.isNullOrEmpty(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim())) {
                                        JSONObject customJObj = new JSONObject();
                                        customJObj.put("fieldid", params.getId());
                                        customJObj.put("filedid", params.getId());
                                        customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                        customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                        customJObj.put("xtype", params.getFieldtype());

                                        String fieldComboDataStr = "";
                                        if (params.getFieldtype() == 3) { // if field of date type
                                            String dateStr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
                                            customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                                            customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
                                        } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                            String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                            for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                                requestParams = new HashMap<String, Object>();
                                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                                requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));


                                                fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                                if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                    FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                    fieldComboDataStr += fieldComboData.getId() + ",";
                                                }
                                            }

                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else {
                                                throw new DataInvalidateException(" entry not found in master list for " + params.getFieldlabel() + " dropdown.");
                                            }
                                        } else if (params.getFieldtype() == 11) { // if field of check box type 
                                            customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                            customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                        } else if (params.getFieldtype() == 12) { // if field of check list type
                                            requestParams = new HashMap<String, Object>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));


                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();

                                            String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                            int dataArrIndex = 0;

                                            for (FieldComboData fieldComboData : fieldComboDataList) {
                                                if (fieldComboDataArr.length > dataArrIndex && fieldComboDataArr[dataArrIndex] != null && fieldComboDataArr[dataArrIndex].replaceAll("\"", "").trim().equalsIgnoreCase("true")) {
                                                    fieldComboDataStr += fieldComboData.getId() + ",";
                                                }
                                                dataArrIndex++;
                                            }

                                            if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                                customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                                customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            } else {
                                                continue;
                                            }
                                        } else {
                                            customJObj.put("Col" + params.getColnum(), recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                            customJObj.put("fieldDataVal", recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                        }

                                        customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                        customJArr.put(customJObj);
                                    }
                                }
                            }

                            customfield = customJArr.toString();
                        }

                        failedPrevRecords = new StringBuilder();
                        failedPrevRecords.append("\n" + createCSVrecord(recarr));
                        
                        /*
                         If any failure then Skip that JE
                        */
                        if (!StringUtil.isNullOrEmpty(failureMsg.toString())) {
                            throw new AccountingException(failureMsg.toString());
                        }

                        // for JE Details
                        JSONObject jeDetailObj = new JSONObject();

                        if (Double.parseDouble(debitAmount) > 0) {
                            jeDetailObj.put("debit", true);
                            jeDetailObj.put("amount", debitAmount);
                        } else {
                            jeDetailObj.put("debit", false);
                            jeDetailObj.put("amount", creditAmount);
                        }

                        jeDetailObj.put("accountid", accountID);
                        jeDetailObj.put("customerVendorId", accountID);
                        jeDetailObj.put("description", description);
                        jeDetailObj.put("accountpersontype", 0);
//                        jeDetailObj.put("rowid", 1);

                        // Add Custom fields details of line items
                        // For create custom field array
                        JSONArray lineCustomJArr = new JSONArray();
                        for (int i = 0; i < jSONArray.length(); i++) {
                            JSONObject jSONObject = jSONArray.getJSONObject(i);

                            if (jSONObject.optBoolean("customflag", false) && jSONObject.optBoolean("isLineItem", false)) {
                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_GENERAL_LEDGER_ModuleId, jSONObject.getString("columnname")));

                                KwlReturnObject fieldParamsResult = accCommonTablesDAO.getFieldParams(requestParams); // get custom field for module
                                FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                                if (jSONObject.getInt("csvindex") > recarr.length - 1) { // (csv) arrayindexoutofbound when last custom column value is empty.
                                    continue;
                                }

                                if (!StringUtil.isNullOrEmpty(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim())) {
                                    JSONObject customJObj = new JSONObject();
                                    customJObj.put("fieldid", params.getId());
                                    customJObj.put("filedid", params.getId());
                                    customJObj.put("refcolumn_name", "Col" + params.getRefcolnum());
                                    customJObj.put("fieldname", "Custom_" + params.getFieldlabel());
                                    customJObj.put("xtype", params.getFieldtype());

                                    String fieldComboDataStr = "";
                                    if (params.getFieldtype() == 3) { // if field of date type
                                        String dateStr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim();
                                        customJObj.put("Col" + params.getColnum(), df.parse(dateStr).getTime());
                                        customJObj.put("fieldDataVal", df.parse(dateStr).getTime());
                                    } else if (params.getFieldtype() == 4 || params.getFieldtype() == 7) { // if field of MultiSelect Drop Down OR Drop Down
                                        String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                        for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                            requestParams = new HashMap<String, Object>();
                                            requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "value", "deleteflag"));
                                            requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), fieldComboDataArr[dataArrIndex], 0));


                                            fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                            if (fieldParamsResult != null && !fieldParamsResult.getEntityList().isEmpty()) {
                                                FieldComboData fieldComboData = (FieldComboData) fieldParamsResult.getEntityList().get(0);
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                        }

                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            throw new DataInvalidateException(params.getFieldlabel()+" entry not found in master list for "+ params.getFieldlabel()+" dropdown.");
                                        }
                                    } else if (params.getFieldtype() == 11) { // if field of check box type 
                                        customJObj.put("Col" + params.getColnum(), Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                        customJObj.put("fieldDataVal", Boolean.parseBoolean(recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim()));
                                    } else if (params.getFieldtype() == 12) { // if field of check list type
                                        requestParams = new HashMap<String, Object>();
                                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_custom_fieldId, "deleteflag"));
                                        requestParams.put(Constants.filter_values, Arrays.asList(params.getId(), 0));


                                        fieldParamsResult = accCommonTablesDAO.getCustomCombodata(requestParams);
                                        List<FieldComboData> fieldComboDataList = fieldParamsResult.getEntityList();

                                        String[] fieldComboDataArr = recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim().split(";");

                                        int dataArrIndex = 0;

                                        for (FieldComboData fieldComboData : fieldComboDataList) {
                                            if (fieldComboDataArr.length > dataArrIndex && fieldComboDataArr[dataArrIndex] != null && fieldComboDataArr[dataArrIndex].replaceAll("\"", "").trim().equalsIgnoreCase("true")) {
                                                fieldComboDataStr += fieldComboData.getId() + ",";
                                            }
                                            dataArrIndex++;
                                        }

                                        if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                            customJObj.put("Col" + params.getColnum(), fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                            customJObj.put("fieldDataVal", fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1));
                                        } else {
                                            continue;
                                        }
                                    } else {
                                        customJObj.put("Col" + params.getColnum(), recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                        customJObj.put("fieldDataVal", recarr[jSONObject.getInt("csvindex")].replaceAll("\"", "").trim());
                                    }

                                    customJObj.put("Custom_" + params.getFieldlabel(), "Col" + params.getColnum());

                                    lineCustomJArr.put(customJObj);
                                }
                            }
                        }

                        jeDetailObj.put("customfield", lineCustomJArr.toString());
                        jeDetailArr.put(jeDetailObj);

                    } catch (Exception ex) {
                        failed++;
                        isRecordFailed = true;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cnt++;
            }

            // save je for last record
            if (!isAlreadyExist && jeDetailArr.length() > 0 && !isRecordFailed) {
                try {
                    double c_Amount = 0;
                    double d_Amount = 0;
                    for (int jedCnt = 0; jedCnt < jeDetailArr.length(); jedCnt++) {
                        JSONObject jeDetailObj = jeDetailArr.getJSONObject(jedCnt);
                        if (jeDetailObj.getBoolean("debit")) {
                            d_Amount += jeDetailObj.getDouble("amount");
                        } else {
                            c_Amount += jeDetailObj.getDouble("amount");
                        }
                    }

                    if (d_Amount != c_Amount) {
                        throw new AccountingException(messageSource.getMessage("acc.msgbox.25", null, locale));
                    }

                    saveJE(requestParams1, jeDataMap, jeDetailArr, customfield);
                } catch (Exception ex) {
                    failed++;
                    String errorMsg = ex.getMessage(), invalidColumns = "";
                    try {
                        JSONObject errorLog = new JSONObject(errorMsg);
                        errorMsg = errorLog.getString("errorMsg");
                        invalidColumns = errorLog.getString("invalidColumns");
                    } catch (JSONException jex) {
                    }
                    failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                }
            }

            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
//                issuccess = false;
                msg = "Failed to import all the records.";
            } else if (success == total) {
                msg = "All records are imported successfully.";
            } else {
                msg = "Imported " + success + " record" + (success > 1 ? "s" : "") + " successfully";
                msg += (failed == 0 ? "." : " and failed to import " + failed + " record" + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { // if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                // Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Acc_GENERAL_LEDGER_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);

                String tableName = importDao.getTableName(fileName);
                importDao.removeFileTable(tableName); // Remove table after importing all records

                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) { // Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }

    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            java.io.FileOutputStream failurefileOut = new java.io.FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            failurefileOut.write(failedRecords.toString().getBytes());
            failurefileOut.flush();
            failurefileOut.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }

    private void saveJE(HashMap<String, Object> requestParams1, HashMap<String, Object> jeDataMap, JSONArray jeDetailArr, String customfield) throws AccountingException {
        try {
            String companyid = (String)requestParams1.get("companyid");

//            jeDataMap.put("jedetails", jeDetails);
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
            JournalEntry je = (JournalEntry) jeresult.getEntityList().get(0);
            KwlReturnObject jedresult = accJournalEntryobj.getJEDset(jeDetailArr, companyid,je);
            HashSet jeDetails = (HashSet) jedresult.getEntityList().get(0);
            je.setDetails(jeDetails);
            String jeid = je.getID();

            for (JournalEntryDetail jed : je.getDetails()) {
                int srno = 0;
                if (jed.getSrno() != 0) {
                    srno = jed.getSrno();
                    JSONObject jobj = jeDetailArr.getJSONObject(--srno);
                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", jcustomarray);
                        customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId); // Constants.Acc_JEDetail_recdetailId
                        customrequestParams.put("modulerecid", jed.getID());
                        customrequestParams.put("recdetailId", jed.getID());
                        customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                        customrequestParams.put("companyid", companyid);
                        customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            // jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                            JSONObject jedjson = new JSONObject();
                            jedjson.put("accjedetailcustomdata", jed.getID());
                            jedjson.put("jedid", jed.getID());
                            jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjson);
                            System.out.println(jed.getID());
                        }
                    }
                }
            }

            /**
             * ************************************ For saving custom fields *********************************************
             */
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", jeid);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    Map<String, Object> customjeDataMap = new HashMap<String, Object>();
                    customjeDataMap.put("accjecustomdataref", jeid);
                    customjeDataMap.put("jeid", jeid);
                    customjeDataMap.put("istemplate", 0);
                    jeresult = accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
            }

            // Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            Double jeAmount = 0.0;
            int level = 0;

            Set<JournalEntryDetail> jeDetail = je.getDetails();
            for (JournalEntryDetail journalEntryDetail : jeDetail) {
                if (journalEntryDetail.isDebit()) { // As Debit and credit amount for JE are same , any one type can be picked for calculating amount
                    jeAmount = jeAmount + journalEntryDetail.getAmount();
                }
            }
            int journalEntryType = je.getTypeValue();
           // String currentUserId = sessionHandlerImpl.getUserid(request);
            if (journalEntryType != 2) { // Currently , Party Journal Entry is excluded from the approval rules. 
                //int approvalStatusLevel = approveJE(je, sessionHandlerImpl.getCompanyid(request), level, String.valueOf(jeAmount), request, true, currentUserId);
            }
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException(ex.getMessage());
        }
    }

    private Account getAccount(String accountCode, String companyID, boolean isByAccountName) throws AccountingException {
        Account account = null;
        try {
            if (!StringUtil.isNullOrEmpty(accountCode) && !StringUtil.isNullOrEmpty(companyID)) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                filter_names.add("company.companyID");
                filter_params.add(companyID);
                filter_names.add("deleted");
                filter_params.add(false);
                if (isByAccountName) {
                    filter_names.add("name");
                    filter_params.add(accountCode);
                } else {
                    filter_names.add("acccode");
                    filter_params.add(accountCode);
                }

                requestParams.put("filter_names", filter_names);
                requestParams.put("filter_params", filter_params);

                KwlReturnObject retObj = accAccountDAOobj.getAccount(requestParams);
                if (retObj != null && !retObj.getEntityList().isEmpty()) {
                    account = (Account) retObj.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accJournalEntryController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("System Failure while fetching Account");
        }
        return account;
    }
    
    //this function is unused.
    public int approveJE(JournalEntry JE, String companyid, int level, String amount, HttpServletRequest request, boolean fromCreate, String currentUser) throws SessionExpiredException, AccountingException, ServiceException, ScriptException, MessagingException {
        boolean hasAuthority = false;
        if (!fromCreate) { // check if the currently logged in user has authority or not to approve pending JE...but only in case when this method is called from approveJournalEntry method 
            String thisUser = currentUser;
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
            User user = (User) userclass.getEntityList().get(0);
            if (AccountingManager.isCompanyAdmin(user)) {
                hasAuthority = true;
            } else {
                hasAuthority = accJournalEntryobj.checkForRule(level, companyid, amount, thisUser);
            }
        } else {
            hasAuthority = true;
        }
        if (hasAuthority) {
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String requisitionApprovalSubject = "Journal Entry: %s - Approval Notification";
            String requisitionApprovalHtmlMsg = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                    + "a:link, a:visited, a:active {\n"
                    + " 	color: #03C;"
                    + "}\n"
                    + "body {\n"
                    + "	font-family: Arial, Helvetica, sans-serif;"
                    + "	color: #000;"
                    + "	font-size: 13px;"
                    + "}\n"
                    + "</style><body>"
                    + "<p>Hi All,</p>"
                    + "<p></p>"
                    + "<p>%s has created journal entry %S and sent it to you for approval.</p>"
                    + "<p>Please review and approve it (Journal Entry Number: %s).</p>"
                    + "<p></p>"
                    + "<p>Thanks</p>"
                    + "<p>This is an auto generated email. Do not reply<br>";
            String requisitionApprovalPlainMsg = "Hi All,\n\n"
                    + "%s has created journal entry %S and sent it to you for approval.\n"
                    + "Please review and approve it (Journal Entry Number: %s).\n\n"
                    + "Thanks\n\n"
                    + "This is an auto generated email. Do not reply\n";
            int approvalStatus = 11;
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            String jeNumber = JE.getEntryNumber();
            String jeID = JE.getID();
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", level + 1);
            qdDataMap.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator itr = flowresult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                String rule = row[2].toString();
                String creator = (!StringUtil.isNullOrEmpty(row[6].toString())) ? row[6].toString() : "";
                boolean sendForApproval = false;
                String[] creators = creator.split(",");
                for (int i = 0; i < creators.length; i++) {
                    if (creators[i].equals(sessionHandlerImpl.getUserid(request))) {
                        sendForApproval = true;
                        break;
                    }
                }
                rule = rule.replaceAll("[$$]+", amount);
                /*
                 * send mail to approvers on any of one below condition is true
                 * - 1 - If level exist and rule is not set 2 - If level and
                 * expression rule exist 3 - If creator rule exist
                 */
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString())) || sendForApproval) {
                    // send emails
                    try {
                        if (Boolean.parseBoolean(row[3].toString()) && preferences.isSendapprovalmail()) {//If allow to send approval mail in company account preferences
                            String fromName = "User";
                            //String fromEmailId = Constants.ADMIN_EMAILID;
                            KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                            Company company = (Company) returnObject.getEntityList().get(0);
                            String fromEmailId = (!company.isEmailFromCompanyCreator()) ? Constants.ADMIN_EMAILID : authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
                            fromName = sessionHandlerImpl.getUserName(request);
//                            String fromEmailId1 = StringUtil.isNullOrEmpty(JE.getCompany().getEmailID()) ? authHandlerDAOObj.getSysEmailIdByCompanyID(JE.getCompany().getCompanyID()) : JE.getCompany().getEmailID();
//                            if (!StringUtil.isNullOrEmpty(fromEmailId1)) {
//                                fromEmailId = fromEmailId1;
//                            }
                            String subject = String.format(requisitionApprovalSubject, jeNumber);
                            String htmlMsg = String.format(requisitionApprovalHtmlMsg, fromName, jeNumber, jeNumber, jeNumber);
                            String plainMsg = String.format(requisitionApprovalPlainMsg, fromName, jeNumber, jeNumber, jeNumber);
                            ArrayList<String> emailArray = new ArrayList<String>();
                            String[] emails = {};
                            qdDataMap.put("ruleid", row[0].toString());
                            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                            Iterator useritr = userResult.getEntityList().iterator();
                            while (useritr.hasNext()) {
                                Object[] userrow = (Object[]) useritr.next();
                                emailArray.add(userrow[3].toString());
                            }
                            emails = emailArray.toArray(emails);
                            if (!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())) {
                                String[] compPrefMailIds = preferences.getApprovalEmails().split(",");
                                emails = AccountingManager.getMergedMailIds(emails, compPrefMailIds);
                            }
                            if (emails.length > 0) {
                                Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                                SendMailHandler.postMail(emails, subject, htmlMsg, plainMsg, fromEmailId, smtpConfigMap);
                            }
                        }
                    } catch (MessagingException ex) {
                        Logger.getLogger(JournalEntry.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    approvalStatus = level + 1;
                }
            }
            accJournalEntryobj.approvePendingJE(jeID, companyid, approvalStatus);
            return approvalStatus;
        } else {
            return Constants.NoAuthorityToApprove; // It return fixed value 999 which indecates that current logged in user has no authority to approve the transaction
        }
    }

    public HashMap getCurrencyMap(boolean isCurrencyCode) throws ServiceException {
        HashMap currencyMap = new HashMap();
        KwlReturnObject returnObject = accProductObj.getCurrencies();
        List currencyList = returnObject.getEntityList();
        if (currencyList != null && !currencyList.isEmpty()) {
            Iterator iterator = currencyList.iterator();
            while (iterator.hasNext()) {
                KWLCurrency currency = (KWLCurrency) iterator.next();
                if(isCurrencyCode){
                    currencyMap.put(currency.getCurrencyCode(), currency.getCurrencyID());
                }else{
                currencyMap.put(currency.getName(), currency.getCurrencyID());
                }
            }
        }
        return currencyMap;
    }

    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }

    private String getCurrencyId(String currencyName, HashMap currencyMap) {
        String currencyId = "";
        if (currencyMap != null && currencyMap.containsKey(currencyName)) {
            currencyId = currencyMap.get(currencyName).toString();
        }
        return currencyId;
    }
}
