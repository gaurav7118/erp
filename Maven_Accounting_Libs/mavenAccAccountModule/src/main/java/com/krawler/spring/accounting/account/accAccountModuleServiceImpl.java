/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.account;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.BankAccountCOAMapping;
import com.krawler.hql.accounting.Group;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.dialect.IngresDialect;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class accAccountModuleServiceImpl implements accAccountModuleService, MessageSourceAware {

    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private ImportHandler importHandler;
    private accCurrencyDAO accCurrencyDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private MessageSource messageSource;
    private accMasterItemsDAO accMasterItemsDAOObj;
    private profileHandlerDAO profileHandlerDAOObj;
    private fieldDataManager fieldDataManager;
    private AccCommonTablesDAO accCommonTablesDAO;

    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManager = fieldDataManagercntrl;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOObj) {
        this.accMasterItemsDAOObj = accMasterItemsDAOObj;
    }

    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj) {
        this.profileHandlerDAOObj = profileHandlerDAOObj;
    }

    @Override
    public JSONObject getGroups(HttpServletRequest request, boolean isForExport) throws ServiceException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("groupid", request.getParameter("groupid"));
            String[] groups = request.getParameterValues("group");
            String[] groupsAfterAdding = groups;
            requestParams.put("group", groupsAfterAdding);
            requestParams.put("ignore", request.getParameter("ignore"));
            requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
            requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
            requestParams.put("nature", request.getParameterValues("nature"));
            requestParams.put("isMasterGroup", request.getParameter("isMasterGroup"));
            requestParams.put("defaultgroup", request.getParameter("defaultgroup"));
            String isRevaluation = request.getParameter("isRevaluation");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = accAccountDAOobj.getGroups(requestParams);
            List ll = result.getEntityList();
            Iterator itr = ll.iterator();

            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                try {
                    Object[] row = (Object[]) itr.next();
                    Group group = (Group) row[0];
                    JSONObject obj = new JSONObject();
                    obj.put("groupid", group.getID());
                    obj.put("groupname", group.getName());
                    obj.put("mastergroupid", group.getID());
                    if (isForExport) {
                        obj.put("nature", (group.getNature() == 0) ? "Liability" : (group.getNature() == 1) ? "Asset" : (group.getNature() == 2) ? "Expenses" : "Income");
                        obj.put("affectgp", group.isAffectGrossProfit() ? "Yes" : "No");
                    } else {
                        obj.put("nature", group.getNature());
                        obj.put("affectgp", group.isAffectGrossProfit());
                    }
                    obj.put("naturename", (group.getNature() == Constants.Liability) ? "Liability" : (group.getNature() == Constants.Asset) ? "Asset" : (group.getNature() == Constants.Expences) ? "Expenses" : (group.getNature() == Constants.Income) ? "Income" : "");

                    obj.put("displayorder", group.getDisplayOrder());
                    obj.put("isMasterGroupD", group.isIsMasterGroup());
                    obj.put("companyid", (group.getCompany() == null ? null : companyid));
                    obj.put("isCostOfGoodsSoldGroup", group.isCostOfGoodsSoldGroup());
                    Group parentGroup = (Group) row[3];
                    obj.put("parentid", parentGroup != null ? parentGroup.getID() : "");
                    obj.put("parentname", parentGroup != null ? parentGroup.getName() : "");
                    obj.put("level", row[1]);
                    obj.put("leaf", row[2]);
                    jArr.put(obj);
                } catch (JSONException ex) {
                    Logger.getLogger(accAccountModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                }
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (JSONException ex) {
            Logger.getLogger(accAccountModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountModuleServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getGroups : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    
    /**
     * To insert 'AvaTax Exmption Code' dimension entry into database for company
     * Dimension added by method: 'AvaTax Exemption Code'
     * @param requestJobj
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws SessionExpiredException 
     */
    @Override
    public JSONObject insertFieldsForAvalara(JSONObject requestJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        String companyid = requestJobj.optString(Constants.companyKey);
        //Modules in which AvaTax Exempton Code dimension is to be created
        int[] moduleIdsForExemptionCode = {Constants.Acc_Invoice_ModuleId, Constants.Acc_Customer_ModuleId, Constants.Acc_Sales_Order_ModuleId, Constants.Acc_Customer_Quotation_ModuleId, Constants.Acc_Sales_Return_ModuleId, Constants.Acc_Delivery_Order_ModuleId};
        for (int moduleid : moduleIdsForExemptionCode) {
            HashMap paramsMap = new HashMap<String, Object>();
            List filter_names = new ArrayList<String>();
            filter_names.add("fieldlabel");
            filter_names.add("company.companyID");
            filter_names.add(Constants.moduleid);
            List filter_values = new ArrayList<String>();
            filter_values.add(IntegrationConstants.avataxExemptionCode);
            filter_values.add(companyid);
            filter_values.add(moduleid);
            paramsMap.put("filter_names", filter_names);
            paramsMap.put("filter_values", filter_values);
            KwlReturnObject kwlObj = accCommonTablesDAO.getFieldParams(paramsMap);//to check if field already exists in database
            boolean isFieldAlreadyExists = (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty() && kwlObj.getEntityList().get(0) != null);
            if (!isFieldAlreadyExists) {//create field only if it doesn't already exist
                paramsMap = new HashMap<String, Object>();
                paramsMap.put("Maxlength", 100);
                paramsMap.put("Isessential", 0);
                paramsMap.put("sendNotification", 0);
                paramsMap.put("isforproject", 0);
                paramsMap.put("Fieldtype", 4);
                paramsMap.put("Isforeclaim", 0);
                paramsMap.put("Customregex", "");
                paramsMap.put("Fieldname", Constants.Custom_Record_Prefix + IntegrationConstants.avataxExemptionCode);
                paramsMap.put("Fieldlabel", IntegrationConstants.avataxExemptionCode);
                paramsMap.put("Fieldtooltip", "");
                paramsMap.put("Companyid", companyid);
                paramsMap.put("Moduleid", moduleid);
                paramsMap.put("Customfield", 0);
                paramsMap.put("Customcolumn", 0);
                paramsMap.put("IsActivated", 1);//For newly created default activation will be 1
                paramsMap.put("Moduleflag", 0);
                paramsMap.put("Iseditable", "true");
                paramsMap.put("isfortask", 0);
                paramsMap.put("DefaultValue", "");
                paramsMap.put("IsAutoPopulateDefaultValue", false);
                paramsMap.put("GSTConfigType", 0);
                paramsMap.put("GSTMappingColnum", 0);
                int colnum = (Integer) accAccountDAOobj.getcolumn_number(companyid, moduleid, 4, 0).get("column_number");
                paramsMap.put("Colnum", colnum);
                kwlObj = accAccountDAOobj.insertfield(paramsMap);
                if (!kwlObj.isSuccessFlag()) {
                    throw new AccountingException();
                }
            }
        }
        return returnJobj;
    }
    @Override
    public JSONObject getLinkedTermTax(HashMap<String, Object>  requestParams) throws JSONException, ServiceException, SessionExpiredException{
        JSONObject returnJobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        KwlReturnObject linkedTermTaxResult = accAccountDAOobj.getLinkedTermTax(requestParams);
        List<Object[]> linkedTermList = linkedTermTaxResult.getEntityList();
        if (linkedTermList != null && !linkedTermList.isEmpty()) {
            for (Object[] row : linkedTermList) {
                JSONObject jobj = new JSONObject();
                jobj.put("termtax", row[0]);
                jobj.put("linkedtaxname", row[1]);
                jobj.put("linkedtaxpercentage", row[2]);
                jobj.put("hasAccess", row[3]);
                jarr.put(jobj);
            }
            /*
             * ERP-40242 : None record added in tax combo
             */
            JSONObject jobjForNone = new JSONObject();
            jobjForNone.put("termtax", "None");
            jobjForNone.put("linkedtaxname", "None");
            jobjForNone.put("linkedtaxpercentage", 0);
            jobjForNone.put("hasAccess", true);
            jarr.put(jobjForNone);
            returnJobj.put("data", jarr);
            returnJobj.put("count", linkedTermTaxResult.getRecordTotalCount());
        }
        return returnJobj;
    }
     @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = { ServiceException.class,Exception.class})
    public JSONObject setInvoiceTermsSalesActive(JSONObject paramJObj)throws ServiceException{
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        try {
            String companyid = paramJObj.getString("companyid");
            HashMap hashMap = new HashMap();
            hashMap.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(paramJObj.getString("isTermActive"))) {
                boolean isTermActive = Boolean.parseBoolean(paramJObj.getString("isTermActive"));
                hashMap.put("isTermActive", isTermActive);
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.getString("termId"))) {
                String termId = paramJObj.getString("termId");
                hashMap.put("termId", termId);
            }
            accAccountDAOobj.setInvoiceTermsSalesActive(hashMap);
            isSuccess = true;
        } catch (ServiceException ex) {
            isSuccess = false;
             throw ServiceException.FAILURE("accAccountDAOImpl.setInvoiceTermsSalesActive:" + "", new Exception());
           
        } catch (JSONException ex) {
            try {
                jobj.put("isSuccess", isSuccess);
            } catch (JSONException ex1) {
                Logger.getLogger(accAccountModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accAccountModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } 
            return jobj;
        
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = { ServiceException.class,Exception.class})
    public JSONObject saveBankAccountMappingDetails(JSONObject paramJObj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean isSuccess = false;
        try {
            String companyid = paramJObj.optString(Constants.companyKey);
            JSONArray jArr = new JSONArray(paramJObj.optString("data"));
            Map<String, Object> bankDetailsMap = null;
            KwlReturnObject kwlObj = accAccountDAOobj.getBankAccountMappingDetails(paramJObj);
            List<BankAccountCOAMapping> list = kwlObj.getEntityList();
            List<String> idsList = new ArrayList();
            for (BankAccountCOAMapping temp: list) {
                if (temp != null) {
                    idsList.add(temp.getID());
                }
            }
            
            for (int index = 0; index < jArr.length(); index++) {
                JSONObject tempObj = jArr.getJSONObject(index);
                bankDetailsMap = new HashMap<>();
                String deskeraAccountId=tempObj.optString(IntegrationConstants.DESKERA_ACCOUNT_ID,"");
                if (!StringUtil.isNullOrEmpty(deskeraAccountId)) {
                    idsList.remove(deskeraAccountId);
                    bankDetailsMap.put(IntegrationConstants.BANK_ID, StringUtil.DecodeText(tempObj.optString(IntegrationConstants.BANK_ID)));
                    bankDetailsMap.put(IntegrationConstants.BANK_ACCOUNT_NAME, StringUtil.DecodeText(tempObj.optString(IntegrationConstants.BANK_ACCOUNT_NAME)));
                    bankDetailsMap.put(IntegrationConstants.BANK_ACCOUNT_NUMBER, tempObj.optString(IntegrationConstants.BANK_ACCOUNT_NUMBER));
                    bankDetailsMap.put(IntegrationConstants.BANK_DETAILS, StringUtil.DecodeText(tempObj.optString(IntegrationConstants.BANK_DETAILS)));
                    bankDetailsMap.put(IntegrationConstants.DESKERA_ACCOUNT_ID, tempObj.optString(IntegrationConstants.DESKERA_ACCOUNT_ID));
                    bankDetailsMap.put(Constants.companyKey, companyid);
                    accAccountDAOobj.saveOrUpdateBankAccountMappingDetails(bankDetailsMap);
                }
            }
            if (!idsList.isEmpty()) {
                String ids = StringUtil.join("','", idsList.toArray());
                if (!StringUtil.isNullOrEmpty(ids)) {
                    ids = "'" + ids + "'";
                    JSONObject paramJobj = new JSONObject();
                    paramJobj.put(Constants.companyKey, companyid);
                    paramJobj.put("ids", ids);
                    accAccountDAOobj.deleteBankAccountMappingDetails(paramJobj);;
                }
            }
            isSuccess = true;
        } catch (ServiceException ex) {
            isSuccess = false;
            throw ServiceException.FAILURE("accAccountModuleServiceImpl.saveBankAccountMappingDetails:" + "", new Exception());

        } catch (JSONException ex) {
            try {
                jobj.put("isSuccess", isSuccess);
            } catch (JSONException ex1) {
                Logger.getLogger(accAccountModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(accAccountModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accAccountModuleServiceImpl.saveBankAccountMappingDetails:" + "", new Exception());
        }
        return jobj;

    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getBankAccountMappingDetails(JSONObject paramJObj) throws ServiceException {

        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        try {
            int count = 0;
            JSONObject commData = new JSONObject();
            JSONObject temp;
            if (paramJObj.has("isReadOnly") && paramJObj.optBoolean("isReadOnly")) {
                    KwlReturnObject mappingKwlObj = accAccountDAOobj.getBankAccountMappingDetails(paramJObj);
                    List<BankAccountCOAMapping> mappingList = mappingKwlObj.getEntityList();
                   if (mappingList != null && !mappingList.isEmpty()) {
                    for (BankAccountCOAMapping bankAccountCOAMapping : mappingList) {
                        temp = new JSONObject();
                        temp.put(IntegrationConstants.BANK_ID, bankAccountCOAMapping.getBankID());
                        temp.put(IntegrationConstants.BANK_ACCOUNT_NUMBER, bankAccountCOAMapping.getBankAccountNumber());
                        temp.put(IntegrationConstants.BANK_ACCOUNT_NAME, bankAccountCOAMapping.getBankAccountName());
                        temp.put(IntegrationConstants.BANK_DETAILS, bankAccountCOAMapping.getBankAccountDetails());
                        temp.put(IntegrationConstants.DESKERA_ACCOUNT_ID, bankAccountCOAMapping.getDeskeraAccount().getAccountName());
                        temp.put(IntegrationConstants.DESKERA_ACCOUNT_NAME, bankAccountCOAMapping.getDeskeraAccount().getAccountName());
                        dataJArr.put(temp);
                    }
                }
            } else {
                dataJArr = createBankAccountMappingDetailsJson(paramJObj);
                for (int i = 0; i < dataJArr.length(); i++) {
                    temp = dataJArr.optJSONObject(i);
                    paramJObj.put(IntegrationConstants.BANK_ACCOUNT_NUMBER, temp.optString(IntegrationConstants.BANK_ACCOUNT_NUMBER));
                    KwlReturnObject mappingKwlObj = accAccountDAOobj.getBankAccountMappingDetails(paramJObj);
                    List<BankAccountCOAMapping> mappingList = mappingKwlObj.getEntityList();
                    if (mappingList != null && !mappingList.isEmpty()) {
                        for (BankAccountCOAMapping bankAccountCOAMapping : mappingList) {
                            if (paramJObj.has("isReadOnly") && paramJObj.optBoolean("isReadOnly")) {
                                temp.put(IntegrationConstants.DESKERA_ACCOUNT_ID, bankAccountCOAMapping.getDeskeraAccount().getAccountName());//In report view we put account name
                            } else {
                                temp.put(IntegrationConstants.DESKERA_ACCOUNT_ID, bankAccountCOAMapping.getID());//To set value for account combo we need account id
                            }
                            temp.put(IntegrationConstants.DESKERA_ACCOUNT_NAME, bankAccountCOAMapping.getDeskeraAccount().getAccountName());
                        }
                    }

                }

            }
            
            count = dataJArr.length();
            commData.put("coldata", dataJArr);
            commData.put("totalCount", count);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            jobj.put("valid", true);
            jobj.put("data", commData);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

        return jobj;
    }
    
    public JSONArray createBankAccountMappingDetailsJson(JSONObject paramJObj) throws ServiceException {

        JSONArray dataJArr = new JSONArray();
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("bankid", "5");
            jSONObject.put("bankaccountname", "3RD PARTY AC 100000088");
            jSONObject.put("bankaccountnumber", "100000088");
            jSONObject.put("bankaccountdetails", "Bank Details DBS");
            dataJArr.put(jSONObject);

            jSONObject = new JSONObject();
            jSONObject.put("bankid", "5");
            jSONObject.put("bankaccountname", "KAREN GALLEGOS");
            jSONObject.put("bankaccountnumber", "00010567000004");
            jSONObject.put("bankaccountdetails", "Bank Details DBS");
            dataJArr.put(jSONObject);
        } catch (Exception ex) {
            Logger.getLogger(accAccountModuleServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataJArr;
    }
    
    @Override
    public  JSONObject getBankAccountMappingGridInfo(JSONObject paramJObj) throws JSONException, ServiceException {
        int colWidth = 100;
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jobjTemp = new JSONObject();

        String StoreRec = "id,bankid,bankaccountname,bankaccountnumber,bankaccountdetails,deskeraaccount,deskeraaccountname";
        String[] recArr = StoreRec.split(",");
        for (String rec : recArr) {
            jobjTemp = new JSONObject();
            jobjTemp.put("name", rec);
            jarrRecords.put(jobjTemp);
        }

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "");
        jobjTemp.put("dataIndex", "id");
        jobjTemp.put("hidden", true);
        jobjTemp.put("hideable", false);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "Bank"); 
        jobjTemp.put("dataIndex", IntegrationConstants.BANK_ID);
        jobjTemp.put("sortable", true);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "Bank Account Name"); 
        jobjTemp.put("dataIndex", IntegrationConstants.BANK_ACCOUNT_NAME);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "Bank Account Number"); 
        jobjTemp.put("dataIndex", IntegrationConstants.BANK_ACCOUNT_NUMBER);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "Bank Details"); 
        jobjTemp.put("dataIndex", IntegrationConstants.BANK_DETAILS);
        jarrColumns.put(jobjTemp);

        jobjTemp = new JSONObject();
        jobjTemp.put("header", "Deskera Account"); 
        jobjTemp.put("dataIndex", IntegrationConstants.DESKERA_ACCOUNT_ID);
        jarrColumns.put(jobjTemp);
        
        jarrRecords.put(jobjTemp);
        jobjTemp = new JSONObject();
        jobjTemp.put("name", IntegrationConstants.DESKERA_ACCOUNT_NAME);

        jMeta.put("totalProperty", "totalCount");
        jMeta.put("root", "coldata");
        jMeta.put("fields", jarrRecords);
        commData.put("columns", jarrColumns);
        commData.put("metadata", jMeta);
        return commData;
    }
}
