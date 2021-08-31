/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.common;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExciseDetailsTemplateMap;
import com.krawler.common.admin.ModuleTemplate;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.FileUploadHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.servlets.ProfileImageServlet;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.fileupload.FileItem;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class CommonFnControllerServiceImpl implements CommonFnControllerService,MessageSourceAware{

    
    private profileHandlerDAO profileHandlerDAOObj;
    private HibernateTransactionManager txnManager;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj1) {
        this.profileHandlerDAOObj = profileHandlerDAOObj1;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    
    public void setaccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAO) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAO;
    }

    @Override
    public JSONObject saveUsers(HashMap<String, Object> requestMap,JSONObject paramJObj,HashMap hm ) {
        String msg = "";
        Boolean success = false;
        JSONObject jobj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        boolean emailidExist = false;
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            if (requestMap.get("emailID") != null && requestMap.get("emailID").toString() != "") {
                emailidExist = profileHandlerDAOObj.checkDuplicateEmailID(requestMap.get("emailID").toString(),paramJObj.optString(Constants.companyKey), requestMap.get("userid").toString());
            }

            if (!emailidExist) {
                KwlReturnObject rtObj = profileHandlerDAOObj.saveUser(requestMap);
                User usr = (User) rtObj.getEntityList().get(0);
                String imageName =null;

                if (paramJObj.optBoolean(Constants.isdefaultHeaderMap)) {
                    imageName = paramJObj.optString("userimage");
                } else {
                    imageName = ((FileItem) (hm.get("userimage"))).getName();
                }
                if (StringUtil.isNullOrEmpty(imageName) == false) {
                    String fileName = usr.getUserID() + FileUploadHandler.getImageExt();
                    usr.setImage(ProfileImageServlet.ImgBasePath + fileName);
                    new FileUploadHandler().uploadImage((FileItem) hm.get("userimage"),
                            fileName,
                            StorageHandler.GetProfileImgStorePath(), 100, 100, false, false);
                }
                success = true;
                msg = messageSource.getMessage("acc.rem.189", null, Locale.forLanguageTag(paramJObj.getString("language")));
            } else {
                success = false;
                msg = "Email ID already Exists. Please enter another Email ID.";
            }
            jobj.put("Timezone", checkCompanyAndUserTimezone(paramJObj));
            txnManager.commit(status);
        } catch (ServiceException ex) {
            success = false;
            msg = ex.getMessage();
            txnManager.rollback(status);
            Logger.getLogger(CommonFnControllerServiceImpl.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            txnManager.rollback(status);
            Logger.getLogger(CommonFnControllerServiceImpl.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(CommonFnControllerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    /**
     *
     * @param request
     * @param response
     * @return = Return True if User Time zone different than company
     * @throws SessionExpiredException
     * @throws ServiceException
     */
    @Override
    public boolean checkCompanyAndUserTimezone(JSONObject paramJObj) throws SessionExpiredException, ServiceException {
        String userid = paramJObj.optString(Constants.useridKey);
        String companyid = paramJObj.optString(Constants.companyKey);
        KwlReturnObject uresult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
        User user = (User) uresult.getEntityList().get(0);
        /*
         * Compare User Timezone and Company Timezone if same then return false
         * other wise return true
         */
        if (user.getTimeZone() != null && user.getCompany().getTimeZone() != null && user.getTimeZone().getTimeZoneID() == user.getCompany().getTimeZone().getTimeZoneID()) {
            return false;
        } else {
            return true;
        }

    }

    @Override
    public HashMap<String, Object> generateMap(HashMap requestMap) throws UnsupportedEncodingException {
        HashMap<String, Object> params = new HashMap();
        params.put("userid", getUTFString(requestMap.get("userid")));
        params.put("firstName", getUTFString(requestMap.get("fname")));
        params.put("lastName", getUTFString(requestMap.get("lname")));
        params.put("emailID", getUTFString(requestMap.get("emailid")));
        params.put("address", getUTFString(requestMap.get("address")));
        params.put("contactno", getUTFString(requestMap.get("contactno")));
        params.put("aboutUser", getUTFString(requestMap.get("aboutuser")));
        params.put("dateformat", getUTFString(getFKvalue(requestMap.get("formatid"))));
        params.put("timeZone", getUTFString(getFKvalue(requestMap.get("tzid"))));
        return params;
    }

    @Override
    public ArrayList<String> getUserApprovalEmail(HashMap requestMap) {
        ArrayList<String> emailArray = new ArrayList<>();
        try {
            String ruleId = "";
            int level = (int) requestMap.get("level");
            int moduleid = (int) requestMap.get(Constants.moduleid);
            String companyid = (String) requestMap.get(Constants.companyKey);
            HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
            qdDataMap.put(Constants.companyKey, companyid);
            qdDataMap.put("level", level);
            qdDataMap.put(Constants.moduleid,moduleid);
            KwlReturnObject flowresult;
            flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(qdDataMap);

            Iterator itr = flowresult.getEntityList().iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                ruleId = row[0].toString();
            }
            HashMap<String, Object> dataMap = new HashMap<>();
            dataMap.put("ruleid", ruleId);
            dataMap.put(Constants.companyKey, companyid);
            dataMap.put("checkdeptwiseapprover", true);
            //Getting Emailid's of approver that are tagged while setting approve rule
            KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(dataMap);
            Iterator useritr = userResult.getEntityList().iterator();
            while (useritr.hasNext()) {
                Object[] userrow = (Object[]) useritr.next();
                if(userrow[3] != null){
                emailArray.add(userrow[3].toString());
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(CommonFnControllerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return emailArray;
    }
    
    public String getApprovalstatusmsg(HashMap requestMap){
        boolean flag = false;
        String approvalpendingStatusmsg = "";
        String ruleid = "", userRoleName = "";
        int level = (int) requestMap.get("level");
        String amountinbase=  "";
        String userid=  "";
        String multipleRuleids="";
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine = mgr.getEngineByName("JavaScript");
        double totalProfitMargin = 0;
        double totalProfitMarginPerc = 0;   
        KwlReturnObject flowresult;
        JSONArray productDiscountMapList = null;
        boolean isLimitExceeding = false; 
        try {
             if (requestMap.containsKey("totalAmount") && requestMap.get("totalAmount") != null) {
               amountinbase=requestMap.get("totalAmount").toString();  
             }
             if (requestMap.containsKey(Constants.useridKey) && requestMap.get(Constants.useridKey) != null) {
               userid=requestMap.get(Constants.useridKey).toString();  
             }
            
            if (requestMap.containsKey("ApproveMap") && requestMap.get("ApproveMap") != null) {
                HashMap<String, Object> soApproveMap = (HashMap<String, Object>) requestMap.get("ApproveMap");

                if (soApproveMap.containsKey("totalProfitMargin") && soApproveMap.get("totalProfitMargin") != null) {
                    totalProfitMargin = Double.parseDouble(soApproveMap.get("totalProfitMargin").toString());
                }

                if (soApproveMap.containsKey("totalProfitMarginPerc") && soApproveMap.get("totalProfitMarginPerc") != null) {
                    totalProfitMarginPerc = Double.parseDouble(soApproveMap.get("totalProfitMarginPerc").toString());
                }

                if (soApproveMap.containsKey("productDiscountMapList") && soApproveMap.get("productDiscountMapList") != null) {
                    productDiscountMapList = new JSONArray(soApproveMap.get("productDiscountMapList").toString());
                }

                if (soApproveMap.containsKey("isLimitExceeding") && soApproveMap.get("isLimitExceeding") != null) {
                    isLimitExceeding = Boolean.parseBoolean(soApproveMap.get("isLimitExceeding").toString());
                }
                if (soApproveMap.containsKey("totalAmount") && soApproveMap.get("totalAmount") != null) {
                    amountinbase = soApproveMap.get("totalAmount").toString();
                }
            }
            requestMap.put("level", level + 1);
            flowresult = accMultiLevelApprovalDAOObj.getMultiApprovalRuleData(requestMap);
//            Iterator ruleitr = flowresult.getEntityList().iterator();
            List ruleitr = flowresult.getEntityList();
            
            for (Object obj : ruleitr) {
                Object[] rulerow = (Object[]) obj;
                String rule="";
                ruleid = (String) rulerow[0];
                rule=(String) rulerow[2];
                int appliedUpon = Integer.parseInt(rulerow[5].toString());
                String discountRule = "";
                if (rulerow[7] != null) {
                    discountRule = rulerow[7].toString();
                }
                
                boolean sendForApproval = false;
                if (requestMap.containsKey("moduleid")&&Integer.parseInt(requestMap.get("moduleid").toString()) == Constants.Acc_GENERAL_LEDGER_ModuleId) {
                String creator = (!StringUtil.isNullOrEmpty(rulerow[6].toString())) ? rulerow[6].toString() : "";
                String[] creators = creator.split(",");
                for (int i = 0; i < creators.length; i++) {
                    if (creators[i].equals(userid)) {
                        sendForApproval = true;
                        break;
                    }
                }
                }
                if (appliedUpon == Constants.Profit_Margin_Amount) {
                    rule = rule.replaceAll("[$$]+", String.valueOf(totalProfitMargin));
                } else if (appliedUpon == Constants.Specific_Products || appliedUpon == Constants.Specific_Products_Discount) {
                    if (productDiscountMapList != null) {
                        sendForApproval = AccountingManager.checkForProductAndProductDiscountRule(productDiscountMapList, appliedUpon, rule, discountRule);
                    }
                }else if(appliedUpon ==Constants.Specific_Products_Category){
                    /*
                     * Check If Rule is apply on product
                     * category from multiapproverule window
                     */
                    sendForApproval = accountingHandlerDAOobj.checkForProductCategoryForProduct(productDiscountMapList, appliedUpon, rule);
                }else if (appliedUpon == Constants.SO_CREDIT_LIMIT && isLimitExceeding) {
                    /*
                     * Check If Rule is apply on SO Credit limit
                     * category from multiapproverule window ERM-396
                     */
                    sendForApproval = true;
                } else {
                    rule = rule.replaceAll("[$$]+", amountinbase);
                }
//                rule = rule.replaceAll("[$$]+", amountinbase);
                try {
                    if(StringUtil.isNullOrEmpty(rule) || (!StringUtil.isNullOrEmpty(rule) && appliedUpon != Constants.Specific_Products && appliedUpon != Constants.Specific_Products_Discount && appliedUpon !=Constants.Specific_Products_Category && Boolean.parseBoolean(engine.eval(rule).toString()))  || sendForApproval){
                         flag=true;
                         multipleRuleids+=ruleid + ",";
                    }} catch (ScriptException ex) {
                    Logger.getLogger(CommonFnControllerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            String[] multipleRuleidsArray = multipleRuleids.split(",");
            for (int multiRule = 0; multiRule < multipleRuleidsArray.length; multiRule++) {
                ruleid = multipleRuleidsArray[multiRule];
            if (!StringUtil.isNullOrEmpty(ruleid)) {
                requestMap.put("ruleid", ruleid);
                KwlReturnObject userResult = accMultiLevelApprovalDAOObj.getApprovalRuleTargetUsers(requestMap);
                List useritr = userResult.getEntityList();
                for (Object obj : useritr) {
                    Object[] userrow = (Object[]) obj;
                    String fname = userrow[1].toString();
                    String lname = userrow[2].toString();
                    String userName = fname + " " + lname;
                    userRoleName += userName + " , ";
                }
            }
            }
            if (!StringUtil.isNullOrEmpty(userRoleName)) {
                userRoleName = userRoleName.substring(0, userRoleName.length() - 2);
            }
            if (flag) {
                approvalpendingStatusmsg = "  but it is still pending with " + (StringUtil.isNullOrEmpty(userRoleName) ? "" : userRoleName) + " for approval at level " + (level + 1);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(CommonFnControllerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnControllerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return approvalpendingStatusmsg;
    }

    @Override
    public HashMap<String, Object> generateMapJSON(JSONObject paramJObj) throws UnsupportedEncodingException, JSONException {
        HashMap<String, Object> params = new HashMap();
        params.put("userid", getUTFString(paramJObj.get("userid")));
        params.put("firstName", getUTFString(paramJObj.get("fname")));
        params.put("lastName", getUTFString(paramJObj.get("lname")));
        params.put("emailID", getUTFString(paramJObj.get("emailid")));
        params.put("address", getUTFString(paramJObj.get("address")));
        params.put("contactno", getUTFString(paramJObj.get("contactno")));
        params.put("aboutUser", getUTFString(paramJObj.get("aboutuser")));
        params.put("dateformat", getUTFString(getFKvalue(paramJObj.get("formatid"))));
        params.put("timeZone", getUTFString(getFKvalue(paramJObj.get("tzid"))));
        return params;
    }

    
    public String getUTFString(Object str) throws UnsupportedEncodingException {
        return str == null ? null : (new String(str.toString().getBytes("ISO-8859-1"), "UTF-8"));
    }

    public String getFKvalue(Object str) {
        return str == null ? null : StringUtil.isNullOrEmpty(str.toString()) ? null : str.toString();
    }

    public JSONObject getModuleTemplate(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        try {
            String start = paramJobj.optString(Constants.start);   //ERP-13664 [SJ]
            String limit = paramJobj.optString(Constants.limit);
            String companyid = paramJobj.optString(Constants.companyid);
            HashMap hashMap = new HashMap();
            hashMap.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("moduleId"))) {
                int moduleId = Integer.parseInt(paramJobj.optString("moduleId"));
                hashMap.put("moduleId", moduleId);
            }
            KwlReturnObject result = accountingHandlerDAOobj.getModuleTemplates(hashMap);
            List list = result.getEntityList();
            Iterator it = list.iterator();
            JSONArray jarr = new JSONArray();
            while (it.hasNext()) {
                ModuleTemplate mt = (ModuleTemplate) it.next();
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("templateId", mt.getTemplateId());
                jsonobj.put("templateName", mt.getTemplateName());
                String moduleName = getModuleName(mt.getModuleId());
                jsonobj.put("moduleName", moduleName);
                jsonobj.put("moduleId", mt.getModuleId());
                jsonobj.put("moduleRecordId", mt.getModuleRecordId());
                jsonobj.put("deletionFlag", true);
                jsonobj.put("isdefaulttemplate", mt.isIsdefaulttemplate());
                if (mt.isIsdefaulttemplate()) {
                    jobj.put("defaultId", mt.getTemplateId());           //TemplateID of default template is sent 
                }
                KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) cmp.getEntityList().get(0);
                int countryid = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
                if (Constants.indian_country_id == countryid) {
                    HashMap tmpHashMap = new HashMap();
                    tmpHashMap.put("companyunitid", mt.getTemplateId()); //ERP-27180
                    tmpHashMap.put("companyid", companyid);
                    KwlReturnObject exciseTemp = accountingHandlerDAOobj.getExciseTemplatesMap(tmpHashMap);
                    if (exciseTemp.getEntityList().size() > 0) {
                        ExciseDetailsTemplateMap moduleTemp = (ExciseDetailsTemplateMap) exciseTemp.getEntityList().get(0);
                        if (moduleTemp != null) {
//                        jsonobj.put("manufacturerType", moduleTemp.getManufacturerType());
                            jsonobj.put("registrationType", moduleTemp.getRegistrationType());
                            jsonobj.put("UnitName", moduleTemp.getUnitname());
                            jsonobj.put("ECCNo", moduleTemp.getECCNo());
                            jsonobj.put("companyunitid", moduleTemp.getId());
                        }
                    }
                }
                jarr.put(jsonobj);
            }
            JSONArray pagedJson = jarr;//ERP-13664 [SJ]
                int cunt=pagedJson.length();
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(jarr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", jarr.length());
            isSuccess = true;
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", isSuccess);
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
            return jobj;
        }

    }
    public String getModuleName(int moduleId) {
        String moduleName = "";

        switch (moduleId) {
            case Constants.Acc_Invoice_ModuleId:
                moduleName = "Customer Invoice";
                break;
            case Constants.Acc_BillingInvoice_ModuleId:
                moduleName = "Customer Invoice Without Inventory";
                break;
            case Constants.Acc_Cash_Sales_ModuleId:
                moduleName = "Cash Sales";
                break;
            case Constants.Acc_Billing_Cash_Sales_ModuleId:
                moduleName = "Cash Sales Without Inventory";
                break;
            case Constants.Acc_Vendor_Invoice_ModuleId:
                moduleName = "Vendor Invoice";
                break;
            case Constants.Acc_Vendor_BillingInvoice_ModuleId:
                moduleName = "Vendor Invoice Without Inventory";
                break;
            case Constants.Acc_Cash_Purchase_ModuleId:
                moduleName = "Cash Purchase";
                break;
            case Constants.Acc_BillingCash_Purchase_ModuleId:
                moduleName = "Cash Purchase Without Inventory";
                break;
            case Constants.Acc_Make_Payment_ModuleId:
                moduleName = "Make Payment";
                break;
            case Constants.Acc_BillingMake_Payment_ModuleId:
                moduleName = "Make Payment Without Inventory";
                break;
            case Constants.Acc_Purchase_Order_ModuleId:
                moduleName = "Purchase Order";
                break;
            case Constants.Acc_BillingPurchase_Order_ModuleId:
                moduleName = "Purchase Order Without Inventory";
                break;
            case Constants.Acc_Sales_Order_ModuleId:
                moduleName = "Sales Order";
                break;
            case Constants.Acc_GENERAL_LEDGER_ModuleId:
                moduleName = "Journal Entry";
                break;
            case Constants.Acc_BillingSales_Order_ModuleId:
                moduleName = "Sales Order Without Inventory";
                break;
            case (Constants.Account_Statement_ModuleId):
                moduleName = "GL Accounts";
                break;
            case (Constants.Acc_Customer_Quotation_ModuleId):
                moduleName = "Customer Quotation";
                break;
            case (Constants.Acc_Stock_Request_ModuleId):
                moduleName = "Stock Request";
                break;
        }
        return moduleName;
    }
}
