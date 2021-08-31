/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.accountservice;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.CustomizeReportHeader;
import com.krawler.common.admin.CustomizeReportMapping;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.ProductFieldsAndModulesMapping;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.Group;
import com.krawler.hql.accounting.InvoiceTermsSales;
import com.krawler.hql.accounting.LayoutGroup;
import com.krawler.hql.accounting.Templatepnl;
import com.krawler.spring.accounting.account.UpdateExistingRecordsWithDefaultValue;
import com.krawler.spring.accounting.account.accAccountController;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

/**
 *
 * @author krawler
 */
public class AccAccountServiceImpl implements AccAccountService, MessageSourceAware {

    private auditTrailDAO auditTrailObj;
    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private UpdateExistingRecordsWithDefaultValue updateExistingRecordsWithDefaultValue;
    private MessageSource messageSource;
    
    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setUpdateExistingRecordsWithDefaultValue(UpdateExistingRecordsWithDefaultValue updateExistingRecordsWithDefaultValue) {
        this.updateExistingRecordsWithDefaultValue = updateExistingRecordsWithDefaultValue;
    }
    
    @Override
    public ModelAndView getTransactionFormFields(HttpServletRequest request, HttpServletResponse response) {
        KwlReturnObject result = null;
        KwlReturnObject resultMapping = null;
        JSONObject jresult = new JSONObject();
        try {
            String moduleId = request.getParameter(Constants.moduleid);
            int module= Integer.parseInt(moduleId);
            String reportId = request.getParameter("reportId") != null ? request.getParameter("reportId") : "";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            KwlReturnObject cpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cpresult.getEntityList().get(0);
            String countryid = sessionHandlerImpl.getCountryId(request);
            boolean isFormField = true;

            HashMap<String, Object> requestParams = new HashMap<String, Object>();

            if (!StringUtil.isNullOrEmpty(moduleId)) {
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.company, Constants.moduleid, "formField"));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Integer.parseInt(moduleId), isFormField));
            }
            // Get Custom Fields
            resultMapping = accAccountDAOobj.getCustomizeReportMapping(requestParams);
            List<CustomizeReportMapping> defaultlstMapping = resultMapping.getEntityList();
            Set<String> customizeReportMappingSet = new HashSet();
            Set<String> customizeReportMappingSetForReadOnlyField = new HashSet();
            Set<String> customizeReportMappingSetForReportField = new HashSet();
            Set<String> customizeReportMappingSetForFieldLabel = new HashSet();
            Set<String> customizeReportMappingSetForMandatory = new HashSet<String>();
            
             
            Map<String,Object> modulemapping=new HashMap<>();
            for (CustomizeReportMapping customizeReportMapping : defaultlstMapping) {
                if (customizeReportMapping.isHidden()) {
                    customizeReportMappingSet.add(customizeReportMapping.getDataIndex());
                }
                if (customizeReportMapping.isReadOnlyField()) {
                    customizeReportMappingSetForReadOnlyField.add(customizeReportMapping.getDataIndex());
                }
                if (customizeReportMapping.isReportField()) {
                    customizeReportMappingSetForReportField.add(customizeReportMapping.getDataIndex());
                }
                if ( customizeReportMapping.isUserManadatoryField() ) {
                    customizeReportMappingSetForMandatory.add(customizeReportMapping.getDataIndex());
                }
                if ( customizeReportMapping.getModulesMapping()!=null ) {
                    modulemapping.put(customizeReportMapping.getDataHeader(), customizeReportMapping.getModulesMapping());
                }
                customizeReportMappingSetForFieldLabel.add(customizeReportMapping.getDataIndex());
            }
           
            // Get Default Fields
            
            requestParams.clear();
            requestParams.put("reportId", reportId);
            requestParams.put("moduleId", moduleId);
            requestParams.put("companyid", companyid);
            requestParams.put("userId", userId);
            requestParams.put("isFormField", true);
            requestParams.put("isReportField", true);
            requestParams.put("countryId", company.getCountry().getID());
            result = accAccountDAOobj.getCustomizeReportHeader(requestParams);
            List<CustomizeReportHeader> defaultlst = result.getEntityList();


            for (CustomizeReportHeader customizeReportHeader : defaultlst) {
                JSONObject jobj = new JSONObject();
                jobj.put("id", customizeReportHeader.getId());
                if(customizeReportHeader.getDataHeader().equals("Including GST") && company.getCountry().getID().equals(""+Constants.INDONESIAN_COUNTRYID)){   // Check for Indonesia company
                    jobj.put("fieldname", "Including VAT");
                }else if (customizeReportHeader.getDataHeader().equals("Including GST") && company.getCountry().getID().equals(""+Constants.indian_country_id)) {   // Check for India company - ERP-21052
                    jobj.put("fieldname", "Including Tax");
                } else {
                    jobj.put("fieldname", customizeReportHeader.getDataHeader());
                }
                
                jobj.put("isFormField", customizeReportHeader.isFormField());
                jobj.put("isReportField", customizeReportHeader.isReportField());
                jobj.put("isManadatoryField", customizeReportHeader.isManadatoryField());
                //jobj.put("isUserManadatoryField", customizeReportHeader.isUserManadatoryField());
                jobj.put("fieldDataIndex", customizeReportHeader.getDataIndex());
                jobj.put("parentid",customizeReportHeader.getParentid());
                /*
                 * changed a type of isLineField to int , so that it will
                 * be used to categorise between Billing Address Field(s)
                 * and Shipping Address Field(s)
                 */
                if(customizeReportHeader.isLineField() == 1) {
                    if((module!=Constants.Acc_Product_ModuleId && module!=Constants.Acc_Contract_Order_ModuleId && module!=Constants.Acc_Lease_Contract && module != Constants.Acc_Purchase_Requisition_ModuleId && module != Constants.Acc_RFQ_ModuleId)){
                       jobj.put("columntype", "Line Item(s)");
                    }else if(customizeReportHeader.isReportField()){
                       jobj.put("columntype", "Report Item(s)");
                    } else{
                       jobj.put("columntype", "Line Item(s)");
                    }
                }
                /*
                 * for Billing Address Fields the value has been set with 2
                 */
                else if(customizeReportHeader.isLineField() == 2) {
                    jobj.put("columntype", "Billing Address Field(s)");
                }
                /*
                 * for Shipping Address Fields the value has been set with 3
                 */
                else if(customizeReportHeader.isLineField() == 3) {
                    jobj.put("columntype", "Shipping Address Field(s)");
                }
                else {
                    jobj.put("columntype", "Default Field(s)");
                }
                if (customizeReportMappingSet.contains(customizeReportHeader.getDataIndex())) {
                    jobj.put("hidecol", true);
                } else {
                    jobj.put("hidecol", false);
                }
                if (customizeReportMappingSetForMandatory.contains(customizeReportHeader.getDataIndex())) {
                    jobj.put("isUserManadatoryField", true);
                } else {
                    jobj.put("isUserManadatoryField", false);
                }
                if (customizeReportMappingSetForReadOnlyField.contains(customizeReportHeader.getDataIndex())) {
                    jobj.put("isreadonlycol", true);
                } else {
                    jobj.put("isreadonlycol", false);
                }
                if (!StringUtil.isNullOrEmpty(moduleId) && !StringUtil.isNullOrEmpty(countryid) && Integer.parseInt(countryid) == Constants.indian_country_id && (Integer.parseInt(moduleId) == Constants.Acc_Credit_Note_ModuleId || Integer.parseInt(moduleId) == Constants.Acc_Debit_Note_ModuleId) && (customizeReportHeader.getDataIndex().equals("prtaxid") || customizeReportHeader.getDataIndex().equals("taxamount"))) {
                    /**
                     * Hide prtaxid and taxamount for debit note & credit note
                     * module for indian country.
                     */
                    jobj.put("hidecol", true);
                    jobj.put("isreadonlycol", true);
                }
                if (customizeReportMappingSetForReportField.contains(customizeReportHeader.getDataIndex())) {
                    jobj.put("hidefieldfromreport", true);
                } else {
                    jobj.put("hidefieldfromreport", false);
                }
                if (customizeReportMappingSetForFieldLabel.contains(customizeReportHeader.getDataIndex())) {
                    for (CustomizeReportMapping crmObj : defaultlstMapping) {
                        if(crmObj.getDataIndex().equals(customizeReportHeader.getDataIndex())){
                            jobj.put("fieldlabeltext", crmObj.getFieldLabelText());
                            break;
                        }
                    }
                }else{
                    jobj.put("fieldlabeltext", "");
                }
                if (module == Constants.Acc_Product_ModuleId && modulemapping.containsKey(customizeReportHeader.getDataHeader())) {
                    StringBuffer sf = new StringBuffer();
                    Set<Object> modmappingSet = (Set) modulemapping.get(customizeReportHeader.getDataHeader());
                    for (Object crmObj : modmappingSet) {
                        ProductFieldsAndModulesMapping modulemappingObj = (ProductFieldsAndModulesMapping) crmObj;
                        if (modulemappingObj != null) {
                            sf.append(modulemappingObj.getModuleid() + ",");
                        }
                    }
                    if (sf.length() > 0) {
                        String moduleidString = sf.substring(0, (sf.length() - 1));
                        jobj.put("modulestoshowintheirforms", moduleidString);
                    }
                }
                
                        jresult.append("data", jobj);
            }

            if (module == Constants.Acc_Contract_Order_ModuleId || module == Constants.Acc_Lease_Contract) {
                requestParams.clear();
                if (!StringUtil.isNullOrEmpty(moduleId)) {
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, Integer.parseInt(moduleId)));
                }
                requestParams.put("order_by", Arrays.asList("sequence"));
                requestParams.put("order_type", Arrays.asList("asc"));
                result = accAccountDAOobj.getFieldParams(requestParams);
                List<FieldParams> customlst = result.getEntityList();
                for (FieldParams tmpcontyp : customlst) {
                    if (tmpcontyp.getCustomfield() == 0 && tmpcontyp.getParent() != null) {
                        continue;//For Ordering of Custom/Dimension fields, in case fo Dimension fields no need to set sequence for childs.
                    }
                    JSONObject jobj = new JSONObject();
                    jobj.put("id", tmpcontyp.getId());
                    jobj.put("fieldname", tmpcontyp.getFieldlabel());
                    if (tmpcontyp.getCustomcolumn() == 1 && Integer.parseInt(moduleId) != Constants.Acc_Customer_ModuleId && Integer.parseInt(moduleId) != Constants.Acc_Vendor_ModuleId) {
                        jobj.put("columntype", "Line Item(s)");
                    } else {
                        if (tmpcontyp.getCustomfield() == 1) {
                            jobj.put("columntype", "Custom Field(s)");
                        } else {
                            jobj.put("columntype", "Dimension Field(s)");
                        }
                    }
                    jobj.put("fieldDataIndex", tmpcontyp.getFieldlabel());
                    jobj.put("sequence", tmpcontyp.getSequence());
                    
                    requestParams.clear();
                    if (!StringUtil.isNullOrEmpty(moduleId)) {
                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.company, Constants.moduleid, "formField"));
                        requestParams.put(Constants.filter_values, Arrays.asList(companyid, Integer.parseInt(moduleId), isFormField));
                    }
                    resultMapping = accAccountDAOobj.getCustomizeReportMapping(requestParams);
                    List<CustomizeReportMapping> defaultlstMapping1 = resultMapping.getEntityList();
                    for (CustomizeReportMapping customizeReportMapping : defaultlstMapping1) {
                        if (customizeReportMapping.getDataHeader().equalsIgnoreCase(tmpcontyp.getFieldlabel())) {
                                jobj.put("hidecol", customizeReportMapping.isHidden());
                                break;
                        } 
                    }
                    jresult.append("data", jobj);
                }
            }
            
            jresult.put("count", jresult.getJSONArray("data").length());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(AccAccountServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
         } catch (JSONException ex) {
            Logger.getLogger(AccAccountServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccAccountServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jresult.put("success", result.isSuccessFlag());
                jresult.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jresult.toString());
    }
    
    @Override
    public JSONArray getInvoiceTerms(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONArray jarr = new JSONArray();
        try {
            HashMap<String, String> termNameID = new HashMap();
            termNameID.put("Basic", "Basic");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap hashMap = new HashMap();
            hashMap.put("companyid", companyid);
            if (request.getParameter("isSalesOrPurchase") != null) {
                hashMap.put("salesOrPurchaseFlag", request.getParameter("isSalesOrPurchase"));
            }
            KwlReturnObject result = accAccountDAOobj.getInvoiceTerms(hashMap);
            List<InvoiceTermsSales> list = result.getEntityList();
            for (InvoiceTermsSales mt : list) {
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                termNameID.put(mt.getId(), mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("glaccountname", !StringUtil.isNullOrEmpty(mt.getAccount().getName()) ? mt.getAccount().getName() : "");
                jsonobj.put("accode", !StringUtil.isNullOrEmpty(mt.getAccount().getAcccode()) ? mt.getAccount().getAcccode() : "");
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("category", mt.getCategory());
                jsonobj.put("includegst", mt.getIncludegst());
                jsonobj.put("includeprofit", mt.getIncludeprofit());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("formulaids", mt.getFormula());
                jsonobj.put("suppressamnt", mt.getSupressamount());
                jsonobj.put("salesorpurchase", mt.isSalesOrPurchase());
                jsonobj.put("creator", mt.getCreator());
                jarr.put(jsonobj);
            }
            if (jarr.length() > 0) {
                for (int cnt = 0; cnt < jarr.length(); cnt++) {
                    JSONObject jsonobj = jarr.getJSONObject(cnt);
                    String[] formula = jsonobj.getString("formula").split(",");
                    String formulaName = "";
                    for (int frmCnt = 0; frmCnt < formula.length; frmCnt++) {

                        formulaName = formulaName.concat(termNameID.get(formula[frmCnt])).concat(",");
                    }
                    if (!StringUtil.isNullOrEmpty(formulaName)) {
                        formulaName = formulaName.substring(0, formulaName.length() - 1);
                    }
                    jsonobj.put("formula", formulaName);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return jarr;
        }
    }
    @Override
    public synchronized void UpdateExistingRecordsWithDefaultValue(HashMap<Integer, HashMap<String, Object>>modulerequestParams, ArrayList moduleArr, String companyid) throws ServiceException {
        try {
            HashMap<String,Object>requestParams = new HashMap<>();
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put("modulerequestParams", modulerequestParams);
            updateExistingRecordsWithDefaultValue.add(requestParams);
            updateExistingRecordsWithDefaultValue.add(moduleArr);
            Thread t = new Thread(updateExistingRecordsWithDefaultValue);
            t.start();
        } catch (Exception ex) {
            Logger.getLogger(AccAccountServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Code is moved from accAccountControllerCMN.
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws AccountingException 
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, AccountingException.class})
    public JSONObject deleteCustomTemplate(JSONObject requestJobj, boolean isAdminSubdomain) throws ServiceException, AccountingException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        String templatename = "";
        try {
            String templateid = requestJobj.optString("templateid");
            if (isAdminSubdomain) {
                KwlReturnObject tempresult = accountingHandlerDAOobj.getObject(DefaultTemplatePnL.class.getName(), templateid);
                templatename = ((DefaultTemplatePnL) tempresult.getEntityList().get(0)).getName();
            } else {
                KwlReturnObject tempresult = accountingHandlerDAOobj.getObject(Templatepnl.class.getName(), templateid);
                templatename = ((Templatepnl) tempresult.getEntityList().get(0)).getName();
            }
            deleteCustomTemplateNew(requestJobj, isAdminSubdomain);
            Map<String, Object> auditParamsMap = new HashMap();
            auditParamsMap.put(Constants.companyKey, requestJobj.optString(Constants.companyKey));
            auditParamsMap.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
            auditParamsMap.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
            auditParamsMap.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
            msg = messageSource.getMessage("acc.field.Customtemplatedeletedsuccessfully", null, Locale.forLanguageTag(requestJobj.optString(Constants.language)));
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.CUSTOMTEMPLATE_DELETED, "User " + requestJobj.optString(Constants.userfullname) + " has deleted  custom layout template " + templatename, auditParamsMap, templateid);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(AccAccountServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("AccAccountServiceImpl.deleteCustomTemplate" + ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(AccAccountServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("AccAccountServiceImpl.deleteCustomTemplate" + ex.getMessage(), ex);
            }
        }
        return jobj;
    }

    public void deleteCustomTemplateNew(JSONObject paramJobj, boolean isAdminSubdomain) throws ServiceException, AccountingException {
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            String templateid = paramJobj.optString("templateid");
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("templateid", templateid);
            requestParams.put("isdecorder", true);
            requestParams.put("isAdminSubdomain", isAdminSubdomain);
            KwlReturnObject result = accAccountDAOobj.getCustomLayoutGroups(requestParams);
            List list = result.getEntityList();
            Iterator itr = list.iterator();

            if (isAdminSubdomain) {
                while (itr.hasNext()) {
                    Object listObj = itr.next();
                    DefaultLayoutGroup group = (DefaultLayoutGroup) listObj;
                    accAccountDAOobj.deleteDefaultLayoutGroupAccount(group.getID());
                    accAccountDAOobj.deleteDefaultLayoutGroupsofTotalGroup(group.getID());
                    accAccountDAOobj.deleteDefaultLayoutGroup(group.getID());
                }
                accAccountDAOobj.deleteDefaultCustomTemplate(templateid);
            } else {
                while (itr.hasNext()) {
                    Object listObj = itr.next();
                    LayoutGroup group = (LayoutGroup) listObj;
                    accAccountDAOobj.deleteLayoutGroupAccount(group.getID(), companyid);
                    accAccountDAOobj.deleteLayoutGroupsofTotalGroup(group.getID(), companyid);
                    accAccountDAOobj.deleteLayoutGroup(group.getID(), companyid);
                }
                //accAccountDAOobj.deleteAccountMapPnL(templateid, companyid);
                accAccountDAOobj.deleteCustomTemplate(templateid, companyid);
            }

        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("AccAccountServiceImpl.deleteCustomTemplate : " + ex.getMessage(), ex);
        }

    }
}
