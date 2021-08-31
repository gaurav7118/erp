/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.payment;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.Cheque;
import com.krawler.hql.accounting.ChequeSequenceFormat;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author Atul
 */
public class accPaymentService {

    private accPaymentDAO accPaymentDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCompanyPreferencesDAO companyPreferencesDAO;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private MessageSource messageSource;

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    /**
     * @param accMultiLevelApprovalDAOObj the accMultiLevelApprovalDAOObj to set
     */
    public void setAccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAOObj;
    }
    
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO companyPreferencesDAO) {
        this.companyPreferencesDAO = companyPreferencesDAO;
    }
   public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

     
     
     public String getNextChequeNumber(HttpServletRequest request, String bankAccountId) throws AccountingException, JSONException {
        String nextChequeNumber = "";
        try {
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            String companyId = sessionHandlerImpl.getCompanyid(request);
            dataMap.put("companyId", companyId);
            dataMap.put("bankAccountId", bankAccountId);
            dataMap.put(Constants.language,  RequestContextUtils.getLocale(request).getLanguage()+"_"+RequestContextUtils.getLocale(request).getCountry());
            KwlReturnObject cqresult = companyPreferencesDAO.getMaxChequeSequenceNumber(dataMap);
            List returnList = cqresult.getEntityList();
            BigInteger maxSequenceNumber = new BigInteger("0");

            if (!returnList.isEmpty()) {
                if (returnList.get(0) != null) {
                    maxSequenceNumber = (BigInteger) returnList.get(0);
                }
            }

            nextChequeNumber = getNextFormatedChequeNumber(maxSequenceNumber, companyId, bankAccountId,dataMap);

        } catch (ServiceException ex) {
            Logger.getLogger(accPaymentService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accPaymentService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return nextChequeNumber;
    }
    
     /**
     * Description: This Method is used to get cheque no.
     * @param paramJobj
     * @param bankAccountId
     */
    
    public String getNextChequeNumberForPayment(JSONObject paramJobj, String bankAccountId) throws AccountingException, JSONException {
        String nextChequeNumber = "";
        try {
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            String companyId = paramJobj.optString(Constants.companyKey);
            dataMap.put("companyId", companyId);
            dataMap.put("bankAccountId", bankAccountId);
            dataMap.put(Constants.language, paramJobj.get(Constants.language));
            KwlReturnObject cqresult = companyPreferencesDAO.getMaxChequeSequenceNumber(dataMap);
            List returnList = cqresult.getEntityList();
            BigInteger maxSequenceNumber = new BigInteger("0");

            if (!returnList.isEmpty()) {
                if (returnList.get(0) != null) {
                    maxSequenceNumber = (BigInteger) returnList.get(0);
                }
            }

            nextChequeNumber = getNextFormatedChequeNumber(maxSequenceNumber, companyId, bankAccountId,dataMap);

        } catch (ServiceException ex) {
            Logger.getLogger(accPaymentService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return nextChequeNumber;
    }

//    private String getCalculatedNextChequeNumber(String nextChequeNumber, String companyId) throws ServiceException {
//        int nextNumber = Integer.parseInt(nextChequeNumber);
//        String nextFormattedChequeNo = getNextFormatedChequeNumber(nextNumber, companyId);
//        return nextFormattedChequeNo;
//
//    }
    public String getNextFormatedChequeNumber(BigInteger maxSequenceNumber, String companyId, String bankAccountId,HashMap map) throws ServiceException, AccountingException, JSONException {
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("bankAccountId", bankAccountId);
        dataMap.put("companyid", companyId);
        String sequenceformatid="";
        if(map.containsKey("sequenceformatid") && map.get("sequenceformatid")!=null){
            sequenceformatid = map.get("sequenceformatid").toString();
            dataMap.put("id", sequenceformatid);
        }
        
        String language="";
        if(map.containsKey(Constants.language) && map.get(Constants.language)!=null){
            language=map.get(Constants.language).toString();
        }
        boolean isSequenceformat=false;
        String formatedNumber = "";
        BigInteger endNumber = BigInteger.valueOf(0);

        ChequeSequenceFormat chequeSequenceFormat = null;
        KwlReturnObject chequeSequenceFormatObj = companyPreferencesDAO.getChequeSequenceFormatList(dataMap);

        BigInteger nextSequenceNumber = maxSequenceNumber.add(BigInteger.valueOf(1));
        BigInteger startFrom = BigInteger.valueOf(1);
        List chequeSequenceFormatList = chequeSequenceFormatObj.getEntityList();
        if (!chequeSequenceFormatList.isEmpty()) {
            for (int i = 0; i < chequeSequenceFormatList.size(); i++) {
                chequeSequenceFormat = (ChequeSequenceFormat) chequeSequenceFormatList.get(i);
                if (chequeSequenceFormat != null && chequeSequenceFormat.isIsactivate()) {
                    endNumber = chequeSequenceFormat.getChequeEndNumber();
                    isSequenceformat=true;
                    if (endNumber.compareTo(nextSequenceNumber)>0) {
                        break;
                    }
                }
            }
        }
        
        int zeroToAppend = 0;

        if (chequeSequenceFormat != null) {
            startFrom = chequeSequenceFormat.getStartFrom();
        }


        if (startFrom.compareTo(maxSequenceNumber) > 0) {
            nextSequenceNumber = startFrom;
        }

        formatedNumber = nextSequenceNumber + "";

        char[] digits = (nextSequenceNumber + "").toCharArray();
        if (chequeSequenceFormat != null) {
            if (chequeSequenceFormat.isShowLeadingZero()) {
                if (chequeSequenceFormat.getNumberOfDigits() > digits.length) {
                    zeroToAppend = chequeSequenceFormat.getNumberOfDigits() - digits.length;
                }
            }
        }

        if (zeroToAppend < 0) {
            zeroToAppend = 0;
        }

        for (int i = 0; i < zeroToAppend; i++) {
            formatedNumber = "0" + formatedNumber;
        }

        HashMap<String, Object> chqMap = new HashMap<String, Object>();

        // Cheque nextChequeNumber is in db or not if available then generate next number manually
        chqMap.put("companyId", companyId);
        chqMap.put("sequenceNumber", nextSequenceNumber);
        chqMap.put("nextChequeNumber", formatedNumber);
        chqMap.put("bankAccountId", bankAccountId);
        chqMap.put("sequenceformatid", sequenceformatid);
        
//        if (chequeSequenceFormat != null && chequeSequenceFormat.isIsactivate() && endNumber != null && nextSequenceNumber.compareTo(endNumber)>0) {
//            throw new AccountingException(messageSource.getMessage("acc.chequesequenceformate.alert", null, StringUtil.isNullOrEmpty(language)?null:Locale.forLanguageTag(language)));
//        }
        boolean isChequeNumberAvailable = isChequeNumberAvailable(chqMap);// This method used to check wheather generated number is already present or not. If present then it returns true otherwise it returns false.
        if (isChequeNumberAvailable) {
            formatedNumber = getNextFormatedChequeNumber(nextSequenceNumber, companyId, bankAccountId,map);
        }
        return formatedNumber;
    }


    public List approveMakePayment(List payment, HashMap<String, Object> cnApproveMap, boolean isMailApplicable) throws ServiceException {
        List returnList = new ArrayList();
        try {
            boolean hasAuthority = false;
            String companyid = "";

            List mailParamList = new ArrayList();
            int returnStatus;

            if (cnApproveMap.containsKey("companyid") && cnApproveMap.get("companyid") != null) {
                companyid = cnApproveMap.get("companyid").toString();
            }
            String currentUser = "";
            if (cnApproveMap.containsKey("currentUser") && cnApproveMap.get("currentUser") != null) {
                currentUser = cnApproveMap.get("currentUser").toString();
            }
            int level = 0;
            if (cnApproveMap.containsKey("level") && cnApproveMap.get("level") != null) {
                level = Integer.parseInt(cnApproveMap.get("level").toString());
            }
            String amount = "";
            if (cnApproveMap.containsKey("totalAmount") && cnApproveMap.get("totalAmount") != null) {
                amount = cnApproveMap.get("totalAmount").toString();
            }
            boolean fromCreate = false;
            if (cnApproveMap.containsKey("fromCreate") && cnApproveMap.get("fromCreate") != null) {
                fromCreate = Boolean.parseBoolean(cnApproveMap.get("fromCreate").toString());
            }
            int moduleid = 0;
            if (cnApproveMap.containsKey("moduleid") && cnApproveMap.get("moduleid") != null) {
                moduleid = Integer.parseInt(cnApproveMap.get("moduleid").toString());
            }

            if (!fromCreate) {
                String thisUser = currentUser;
                KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), thisUser);
                User user = (User) userclass.getEntityList().get(0);

                if (AccountingManager.isCompanyAdmin(user)) {
                    hasAuthority = true;
                } else {
                    
                    
                    /*
                     If "Send approval documents to next level" is disabled from system preferences & pending document is edited then
                     1. When user is authorised then document is always goes at first level
                     2. When user is not authorised then document remains at same level
                 
                     */
                    boolean isEditedPendingDocumentWithCheckOff = false;
                    if (cnApproveMap.containsKey("isEditedPendingDocumentWithCheckOff") && cnApproveMap.get("isEditedPendingDocumentWithCheckOff") != null) {
                        level = Integer.parseInt(cnApproveMap.get("documentLevel").toString());//Actual level of document for fetching rule at that level for the user
                        cnApproveMap.put("level", level);

                        isEditedPendingDocumentWithCheckOff = true;
                    }

                    hasAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(cnApproveMap);

                    /*---If User is authorised at this level then sending document to first level that's why assigning "level=0" ------ */
                    if (isEditedPendingDocumentWithCheckOff && hasAuthority) {
                        level = 0;
                    }
                                                          
                }
            } else {
                hasAuthority = true;
            }

            if (hasAuthority) {
                ScriptEngineManager mgr = new ScriptEngineManager();
                ScriptEngine engine = mgr.getEngineByName("JavaScript");
                int approvalStatus = 11;
//                String cnNumber = creditnote.getCreditNoteNumber();
                String cnID = (String) payment.get(0);
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("level", level + 1);
                qdDataMap.put("moduleid", moduleid);
                KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                Iterator itr = flowresult.getEntityList().iterator();
                String fromName = "User";
//                fromName = creditnote.getCreatedby().getFirstName().concat(" ").concat(creditnote.getCreatedby().getLastName());
                while (itr.hasNext()) {
                    Object[] row = (Object[]) itr.next();
                    HashMap<String, Object> recMap = new HashMap();
                    String rule = "";
                    if (row[2] != null) {
                        rule = row[2].toString();
                    }
                    rule = rule.replaceAll("[$$]+", amount);
                    if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                        boolean hasApprover = Boolean.parseBoolean(row[3].toString());
                        approvalStatus = level + 1;
                        recMap.put("ruleid", row[0].toString());
                        recMap.put("fromName", fromName);
                        recMap.put("hasApprover", hasApprover);
                        mailParamList.add(recMap);
                    }
                }
                accPaymentDAOobj.approvePendingMakePayment(cnID, companyid, approvalStatus);
                returnStatus = approvalStatus;
            } else {
                returnStatus = Constants.NoAuthorityToApprove; //if not have approval permission then return one fix value like 999
            }
            returnList.add(returnStatus);
            returnList.add(mailParamList);
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return returnList;
    }
    
    public JSONObject getMakePaymentApprovalPendingJsonData(JSONObject obj, Double totalAmount, int approvallevel, String companyid, String userid, String userName) throws ServiceException {
        try {
            double amountInBase = authHandler.round(totalAmount, companyid);
            ScriptEngineManager mgr = new ScriptEngineManager();
            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            String multipleRuleids="";
            String approvalStatus = "";
            if (approvallevel < 0) {//will be negartive for rejected
                approvalStatus = "Rejected";
            } else if (approvallevel < 11) {//will be less than 11 for pending record 
                String ruleid = "", userRoleName = "";
                HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
                qdDataMap.put("companyid", companyid);
                qdDataMap.put("level", approvallevel);
                qdDataMap.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
                Iterator ruleitr = flowresult.getEntityList().iterator();
                while (ruleitr.hasNext()) {
                        Object[] rulerow = (Object[]) ruleitr.next();
                        ruleid = rulerow[0].toString();
                        int appliedUpon = Integer.parseInt(rulerow[5].toString());
                        String rule = "";
                        if (rulerow[2] != null) {
                            rule = rulerow[2].toString();
                        }
                        if (appliedUpon == Constants.Total_Amount) {
                            /*
                             Added to get condition of approval rule i.e set when creating approval rule 
                             */
                            rule = rule.replaceAll("[$$]+", String.valueOf(amountInBase));
                        }
                        /*
                         Added to check if record falls in total amount approval rule 
                         */
                        if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon == Constants.Total_Amount && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                            multipleRuleids += ruleid + ",";
                        }

                    }
                /*
                 Added to get multiple ruleid if record falls in multiple approval rule 
                 */
                    String[] multipleRuleidsArray = multipleRuleids.split(",");
                for (int multiRule = 0; multiRule < multipleRuleidsArray.length; multiRule++) {
                    ruleid = multipleRuleidsArray[multiRule];
                    if (!StringUtil.isNullOrEmpty(ruleid)) {
                        qdDataMap.put("ruleid", ruleid);
                        KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(qdDataMap);
                        Iterator useritr = userResult.getEntityList().iterator();
                        while (useritr.hasNext()) {
                            Object[] userrow = (Object[]) useritr.next();
                            String approvalID = userrow[0].toString();
                            String approvalName = userrow[1].toString();
                            /*
                             Addded so duplicate approve's can be eleminated 
                             */
                            if (userRoleName.contains(approvalName)) {
                                continue;
                            }
                            KwlReturnObject kmsg = null;
                            String roleName = "Company User";
                            userRoleName += roleName + " " + approvalName + ",";
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(userRoleName)) {
                    userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                }
                approvalStatus = "Pending Approval" + (StringUtil.isNullOrEmpty(userRoleName) ? "" : " by " + userRoleName) + " at Level - " + approvallevel;
            } else {
                approvalStatus = "Approved";
            }
            obj.put("approvalstatusinfo", approvalStatus);
            obj.put("approvalLevel", approvallevel);

            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User user = (User) userclass.getEntityList().get(0);
            boolean hasApprovalAuthority = false;
            if (AccountingManager.isCompanyAdmin(user)) {
                hasApprovalAuthority = true;
            } else {
                HashMap<String, Object> cnApproveMap = new HashMap<String, Object>();
                cnApproveMap.put("companyid", companyid);
                cnApproveMap.put("level", approvallevel);
                cnApproveMap.put("totalAmount", String.valueOf(amountInBase));
                cnApproveMap.put("currentUser", userid);
                cnApproveMap.put("fromCreate", false);
                cnApproveMap.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                hasApprovalAuthority = accountingHandlerDAOobj.checkForMultiLevelApprovalRules(cnApproveMap);
            }
            obj.put("hasApprovalAuthority", hasApprovalAuthority);

            int nextApprovalLevel = 11;
//            ScriptEngineManager mgr = new ScriptEngineManager();
//            ScriptEngine engine = mgr.getEngineByName("JavaScript");
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put("companyid", companyid);
            qdDataMap.put("level", approvallevel + 1);
            qdDataMap.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
            KwlReturnObject flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);
            Iterator approvalRuleItr = flowresult.getEntityList().iterator();
            while (approvalRuleItr.hasNext()) {
                Object[] rowObj = (Object[]) approvalRuleItr.next();
                String rule = "";
                if (rowObj[2] != null) {
                    rule = rowObj[2].toString();
                }
                rule = rule.replaceAll("[$$]+", String.valueOf(amountInBase));
                if (StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && Boolean.parseBoolean(engine.eval(rule).toString()))) {
                    nextApprovalLevel = approvallevel + 1;
                }
            }
            obj.put("isFinalLevelApproval", nextApprovalLevel == 11 ? true : false);
//            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getMakePaymentJson : " + ex.getMessage(), ex);
        }
        return obj;
    }
    
    public boolean isChequeNumberAvailable(HashMap<String, Object> dataMap) throws ServiceException, JSONException {
        // First check that entered check no. is available or not if that is not available 
        // then check its sequence number is available or not
//        boolean isChequeNumberAvailable = companyPreferencesDAO.isChequeNumberAvailable(dataMap);
        JSONObject job = companyPreferencesDAO.isChequeNumberAvailable(dataMap);
        boolean isChequeNumberAvailable = job.optBoolean("isChequeNumberAvailable", false);

        boolean checkForNextSequenceNumberAlso = true;

        if (dataMap.containsKey("checkForNextSequenceNumberAlso")) {
            checkForNextSequenceNumberAlso = (Boolean) dataMap.get("checkForNextSequenceNumberAlso");
        }

//        if (!isChequeNumberAvailable && checkForNextSequenceNumberAlso) {
//            isChequeNumberAvailable = accPaymentDAOobj.isChequeSequenceNumberAvailable(dataMap);
//        }
        return isChequeNumberAvailable;
    }
    public JSONArray sortJson(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject ja, JSONObject jb) {
                    double sr1 = 0, sr2 = 0;
                    try {
                        sr1 = Integer.parseInt(ja.optString("srNoForRow", "0"));
                        sr2 = Integer.parseInt(jb.optString("srNoForRow", "0"));
                    } catch (Exception ex) {
                        Logger.getLogger(accPaymentService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (sr1 > sr2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });

        } catch (JSONException ex) {
            Logger.getLogger(accPaymentService.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accPaymentService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONArray(jsons);
    }
     public String getNextChequeNumberForRecurredPayment(String companyId, String bankAccountId) throws AccountingException, JSONException {
        String nextChequeNumber = "";
        try {
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("companyId", companyId);
            dataMap.put("bankAccountId", bankAccountId);
            KwlReturnObject cqresult = companyPreferencesDAO.getMaxChequeSequenceNumber(dataMap);
            List returnList = cqresult.getEntityList();
            BigInteger maxSequenceNumber = new BigInteger("0");

            if (!returnList.isEmpty()) {
                if (returnList.get(0) != null) {
                    maxSequenceNumber = (BigInteger) returnList.get(0);
                }
            }

            nextChequeNumber = getNextFormatedChequeNumber(maxSequenceNumber, companyId, bankAccountId,dataMap);

        } catch (ServiceException ex) {
            Logger.getLogger(accPaymentService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return nextChequeNumber;
    }
//    public String getNextFormatedChequeNumber(int maxSequenceNumber, String companyId) throws ServiceException {
//
//        KwlReturnObject prefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
//        ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) prefresult.getEntityList().get(0);
//        int startFrom = 1;
//        int nextSequenceNumber = maxSequenceNumber + 1;
//        if (preferences != null) {
//            startFrom = preferences.getChequeNoStartFrom();
//        }
//
//        if (startFrom > maxSequenceNumber) {
//            nextSequenceNumber = startFrom;
//        }
//
//        int zeroToAppend = 0;
//        char[] digits = (nextSequenceNumber + "").toCharArray();
//
//        if (preferences.isShowLeadingZero()) {
//            if (preferences.getChequeDigitNumbers() > digits.length) {
//                zeroToAppend = preferences.getChequeDigitNumbers() - digits.length;
//            }
//        }
//
//        String formatedNumber = nextSequenceNumber + "";
//
//        for (int i = 0; i < zeroToAppend; i++) {
//            formatedNumber = "0" + formatedNumber;
//        }
//
//        HashMap<String, Object> dataMap = new HashMap<String, Object>();
//
//        // Cheque nextChequeNumber is in db or not if available then generate next number manually
//        dataMap.put("companyId", companyId);
//        dataMap.put("nextChequeNumber", formatedNumber);
//        boolean isChequeNumberAvailable = accPaymentDAOobj.isChequeNumberAvailable(dataMap);
//
//        if (isChequeNumberAvailable) {
//            formatedNumber = getNextFormatedChequeNumber(nextSequenceNumber, companyId);
//        }
//
//        return formatedNumber;
//    }
}
