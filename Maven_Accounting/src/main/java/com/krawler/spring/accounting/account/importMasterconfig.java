/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.account;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.ImportLog;
import com.krawler.common.admin.Modules;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.CsvReader;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.MasterGroup;
import com.krawler.hql.accounting.MasterItem;
import static com.krawler.spring.accounting.account.accAccountControllerCMN.getActualFileName;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class importMasterconfig implements Runnable {

    ArrayList processQueue = new ArrayList();
    private boolean isworking = false;
    private ImportDAO importDao;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private HibernateTransactionManager txnManager;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private authHandlerDAO authHandlerDAOObj;

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
    
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public boolean isIsworking() {
        return isworking;
    }

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setIsworking(boolean isworking) {
        this.isworking = isworking;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    private int importLimit = 1500;

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

                    boolean allowropagatechildcompanies = requestParams1.containsKey("allowropagatechildcompanies") ? Boolean.parseBoolean(requestParams1.get("allowropagatechildcompanies").toString()) : false;
                    if (allowropagatechildcompanies) {
                        Object childcompanies = requestParams1.containsKey("childcompanylist") ? requestParams1.get("childcompanylist") : "";
                        List childcompanylist = (List) childcompanies;
                        for (Object childObj : childcompanylist) {
                            try {
                                Object[] childdataOBj = (Object[]) childObj;
                                String childCompanyID = (String) childdataOBj[0];
                                requestParams1.put("companyid", childCompanyID);
                                jobj = ImportCustomFieldsData(requestParams1);
                                sendMail(requestParams1, jobj);
                            } catch (Exception ex) {
                                Logger.getLogger(ImportHandler.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    } else {

                        jobj = ImportCustomFieldsData(requestParams1);
                        sendMail(requestParams1, jobj);
                    }
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
    
    public void sendMail(Map<String, Object> requestParams1, JSONObject jobj) throws ServiceException {
        try {
            User user = (User) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.User", requestParams1.get("userId").toString());
            Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), user.getCompany().getCompanyID());
            String htmltxt = "Report for data imported.<br/>";
            //htmltxt += "<br/>Module Name: " + modulename + "<br/>";
            htmltxt += "<br/>File Name: " + jobj.get("filename") + "<br/>";
            htmltxt += "Total Records: " + jobj.get("totalrecords") + "<br/>";
            htmltxt += "Records Imported Successfully: " + jobj.get("successrecords");
            htmltxt += "<br/>Failed Records: " + jobj.get("failedrecords");
            htmltxt += "<br/><br/>Please check the import log in the system for more details.";
            htmltxt += "<br/>For queries, email us at support@deskera.com<br/>";
            htmltxt += "Deskera Team";

            String plainMsg = "Report for data imported.\n";
            //plainMsg += "\nModule Name: " + modulename + "\n";
            plainMsg += "\nFile Name:" + jobj.get("filename") + "\n";
            plainMsg += "Total Records: " + jobj.get("totalrecords");
            plainMsg += "\nRecords Imported Successfully: " + jobj.get("successrecords");
            plainMsg += "\nFailed Records: " + jobj.get("failedrecords");
            plainMsg += "\n\nPlease check the import log in the system for more details.";

            plainMsg += "\nFor queries, email us at support@deskera.com\n";
            plainMsg += "Deskera Team";
            
            Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
            String fromEmailId = (!company.isEmailFromCompanyCreator())?Constants.ADMIN_EMAILID:authHandlerDAOObj.getSysEmailIdByCompanyID(company.getCompanyID());
            SendMailHandler.postMail(new String[]{user.getEmailID()}, "Deskera Accounting - Report for data imported", htmltxt, plainMsg, fromEmailId, smtpConfigMap);
        } catch (Exception ex) {
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("Importproduct.sendMail : " + ex.getMessage(), ex);
        }

    }
    public JSONObject ImportCustomFieldsData(HashMap<String, Object> requestParamsobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        CsvReader csvReader = null;
        int total = 0, failed = 0;
        String companyid = requestParamsobj.get("companyid").toString();     //sessionHandlerImpl.getCompanyid(request);
        String userId = requestParamsobj.get("userId").toString();  //sessionHandlerImpl.getUserid(request);
        JSONObject jobj = new JSONObject();
        jobj = (JSONObject) requestParamsobj.get("jobj");
        String fileName = jobj.getString("name");
        String importfile = requestParamsobj.get("titleMsg").toString().trim();
        boolean bothcd = importfile.equalsIgnoreCase("Default Fields and Custom Fields/Dimension data") ? true : false;
        boolean defaultcd = importfile.equalsIgnoreCase("Default Fields data") ? true : false;
        boolean customefielddimention = importfile.equalsIgnoreCase("Custom Fields/Dimension data") ? true : false;
        if (bothcd) {
            defaultcd = true;
            customefielddimention = true;
        }
        JSONObject returnObj = new JSONObject();

        try {

            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            //br = new BufferedReader(new InputStreamReader(fileInputStream));
            csvReader = new CsvReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cont = 0;

            StringBuilder failedRecords = new StringBuilder();

            JSONArray jSONArray = jobj.getJSONArray("Header");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("header"));
            }

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            while (csvReader.readRecord()) {
                if (cont != 0) {
                    record = csvReader.getRawRecord();
                    String[] recarr = record.split(",");
                    try {

                        String master_item = recarr[0].trim();
                        if (!StringUtil.isNullOrEmpty(master_item)) {
                            master_item = master_item.replaceAll("\"", "");
                            master_item = master_item.trim();
                        } else {
                            throw new AccountingException("Master item is not Available");
                        }
                        String custom_field_dimension_name = recarr[1].trim();
                        if (!StringUtil.isNullOrEmpty(custom_field_dimension_name)) {
                            custom_field_dimension_name = custom_field_dimension_name.replaceAll("\"", "");
                            custom_field_dimension_name = custom_field_dimension_name.trim();
                        } else {
                            throw new AccountingException(" Custom fields/Dimension name is not Available");
                        }
                        String item_parent = recarr[2].trim();
                        if (!StringUtil.isNullOrEmpty(item_parent)) {
                            item_parent = item_parent.replaceAll("\"", "");
                            item_parent = item_parent.trim();
                        }

                        String parent_dimension = recarr[3].trim();
                        if (!StringUtil.isNullOrEmpty(parent_dimension)) {
                            parent_dimension = parent_dimension.replaceAll("\"", "");
                            parent_dimension = parent_dimension.trim();
                        }
                        String parent_dimension_value = recarr[4].trim();
                        if (!StringUtil.isNullOrEmpty(parent_dimension_value)) {
                            parent_dimension_value = parent_dimension_value.replaceAll("\"", "");
                            parent_dimension_value = parent_dimension_value.trim();
                        }
                        String is_master_group_item = recarr[5].trim();
                        if (!StringUtil.isNullOrEmpty(is_master_group_item)) {
                            is_master_group_item = is_master_group_item.replaceAll("\"", "");
                            is_master_group_item = is_master_group_item.trim();
                        }
                        if (checkForValidation(is_master_group_item)) {
                            throw new AccountingException("Please give valid  Value - Value should be 'Yes' or 'No' ");
                        }
//                            if("No".equalsIgnoreCase(item_parent) && "No".equalsIgnoreCase(parent_dimension_value) && "No".equalsIgnoreCase(is_master_group_item)){
//                                throw new AccountingException("No Mapping");
//                            }
                        HashMap<String, String> getMastergroupidByName = getMasterGroupMap();
                        //HashMap<String, String> getMastergroupidByName=getMasterGroupMap();
                        if (defaultcd) {
                            if ("Yes".equalsIgnoreCase(is_master_group_item)) {
                                KwlReturnObject result = null;
                                if (!getMastergroupidByName.containsKey(custom_field_dimension_name)) {
                                    throw new AccountingException("Please give valid  Master Group Name");
                                }
                                boolean isPresent = false;
                                HashMap requestParam = requestParamsobj;   // AccountingManager.getGlobalParams(request);
                                requestParam.put("name", master_item);
                                requestParam.put("groupid", getMastergroupidByName.get(custom_field_dimension_name));
                                requestParam.put("companyid", companyid);
                                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                                filter_names.add("masterGroup.groupName");
                                filter_params.add(custom_field_dimension_name);
                                filter_names.add("company.companyID");
                                filter_params.add(companyid);
                                filter_names.add("value");
                                filter_params.add(master_item);
                                filterRequestParams.put("filter_names", filter_names);
                                filterRequestParams.put("filter_params", filter_params);
                                KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                                int count = cntResult.getRecordTotalCount();
                                if (count >= 1) {
                                    isPresent = true;
                                }
                                if (!"No".equalsIgnoreCase(item_parent) && !StringUtil.isNullOrEmpty(item_parent)) {
                                    HashMap<String, Object> parentfilterRequestParams = new HashMap<String, Object>();
                                    ArrayList parentfilter_names = new ArrayList(), parentfilter_params = new ArrayList();
                                    parentfilter_names.add("masterGroup.groupName");
                                    parentfilter_params.add(custom_field_dimension_name);
                                    parentfilter_names.add("company.companyID");
                                    parentfilter_params.add(companyid);
                                    parentfilter_names.add("value");
                                    parentfilter_params.add(item_parent);
                                    parentfilterRequestParams.put("filter_names", filter_names);
                                    parentfilterRequestParams.put("filter_params", filter_params);
                                    KwlReturnObject checkifparentpresent = accMasterItemsDAOobj.getMasterItems(parentfilterRequestParams);
                                    try {
                                        MasterItem mi = (MasterItem) checkifparentpresent.getEntityList().get(0);
                                        requestParam.put("parentid", mi.getID());
                                    } catch (Exception e) {
                                        throw new AccountingException("Parent Master item is not present");
                                    }
                                }
                                if (isPresent) {
                                    throw new AccountingException("Master item entry for " + master_item + " already exists.");
                                } else {
                                    result = accMasterItemsDAOobj.addMasterItem(requestParam);
                                }
                            } else if (!bothcd) {
                                throw new AccountingException("Please give valid  Master Group item ");
                            }
                        }
                        if (customefielddimention) {
                            if ("No".equalsIgnoreCase(is_master_group_item)) {
                                HashMap<String, String> externalparentchild = new HashMap<String, String>();
                                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                                filter_names.add("field.company.companyID");
                                filter_params.add(companyid);
                                filter_names.add("field.fieldlabel");
                                filter_params.add(custom_field_dimension_name);
                                filter_names.add("value");
                                filter_params.add(master_item);
                                filterRequestParams.put("filter_names", filter_names);
                                filterRequestParams.put("filter_params", filter_params);
                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                requestParams.put("filter_names", Arrays.asList("companyid"));
                                requestParams.put("filter_values", Arrays.asList(companyid));//sessionHandlerImpl.getCompanyid(request)
                                requestParams.put("search_values", custom_field_dimension_name);
                                KwlReturnObject customefielddimentionresult = accMasterItemsDAOobj.getFieldParamsUsingSql(requestParams);
                                if (customefielddimentionresult.getRecordTotalCount() <= 0) {
                                    throw new AccountingException("Please Give valid Custom fields/Dimension name");
                                }
                                KwlReturnObject childResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams); // get first child
                                if (childResult.getRecordTotalCount() > 0) {
                                    throw new AccountingException("Master item entry for " + master_item + " already exists.");
                                }
                                List<FieldComboData> lstchild = Collections.EMPTY_LIST;
                                if (childResult.getEntityList() != null && !childResult.getEntityList().isEmpty()) {
                                    lstchild = childResult.getEntityList();
                                    for (FieldComboData fcd : lstchild) {
                                        if (!StringUtil.isNullOrEmpty(parent_dimension_value) && !"no".equalsIgnoreCase(parent_dimension_value)) {
                                            try {
                                                externalparentchild.put(fcd.getField().getParentid(), fcd.getId());
                                            } catch (Exception e) {
                                                throw new AccountingException("No parent dimension is set for this dimension");
                                            }
                                        }
                                    }
                                }
                                KwlReturnObject cntResult = null;
                                if (!StringUtil.isNullOrEmpty(item_parent) && !"no".equalsIgnoreCase(item_parent)) {
                                    HashMap<String, Object> filterRequestParams1 = new HashMap<String, Object>();
                                    HashMap<String, String> parentchild = new HashMap<String, String>();
                                    ArrayList filter_names1 = new ArrayList(), filter_params1 = new ArrayList();
                                    filter_names1.add("field.company.companyID");
                                    filter_params1.add(companyid);
                                    filter_names1.add("field.fieldlabel");
                                    filter_params1.add(custom_field_dimension_name);
                                    filter_names1.add("value");
                                    filter_params1.add(item_parent);
                                    filterRequestParams1.put("filter_names", filter_names1);
                                    filterRequestParams1.put("filter_params", filter_params1);
                                    cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams1); // get first parents
                                    if (cntResult.getRecordTotalCount() <= 0) {
                                        throw new AccountingException("Please Give valid Item parent name");
                                    }
                                    List<FieldComboData> lstFieldComboData = cntResult.getEntityList();
                                    if (cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty() && "no".equalsIgnoreCase(parent_dimension_value)) {
                                        Object obj1[] = (Object[]) customefielddimentionresult.getEntityList().get(0);
                                        String ModuleId[] = obj1[2].toString().split(",");
                                        int i = 0;
                                        for (FieldComboData fcd : lstFieldComboData) {
                                            parentchild.put(fcd.getFieldid(), fcd.getId());
                                            HashMap requestParam = requestParamsobj;//AccountingManager.getGlobalParams(request);
                                            requestParam.put("name", master_item);
                                            requestParam.put("groupid", ModuleId[i++]);
                                            requestParam.put("parentid", parentchild.get(fcd.getFieldid()));
                                            KwlReturnObject result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                                        }
                                        for (FieldComboData fcd : lstchild) {
                                            HashMap requestParam = requestParamsobj; // AccountingManager.getGlobalParams(request);
                                            requestParam.put("id", fcd.getId());
                                            requestParam.put("parentid", parentchild.get(fcd.getFieldid()));
                                            KwlReturnObject result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, true);
                                        }
                                    }
                                }
                                if (!StringUtil.isNullOrEmpty(parent_dimension_value) && !"no".equalsIgnoreCase(parent_dimension_value)) {
                                    HashMap<String, Object> filterRequestParams1 = new HashMap<String, Object>();
                                    String[] parentdimenstionvalue = parent_dimension_value.split(";");
                                    boolean check = false;
                                    String parentValue = "";
                                    Object obj1[] = (Object[]) customefielddimentionresult.getEntityList().get(0);
                                    for (int count = 0; count < parentdimenstionvalue.length; count++) {
                                        ArrayList filter_names1 = new ArrayList(), filter_params1 = new ArrayList();
                                        filter_names1.add("field.company.companyID");
                                        filter_params1.add(companyid);
                                        filter_names1.add("field.fieldlabel");
                                        filter_params1.add(parent_dimension);
                                        filter_names1.add("value");
                                        filter_params1.add(parentdimenstionvalue[count]);
                                        HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                                        requestParams1.put("filter_names", Arrays.asList("companyid"));
                                        requestParams1.put("filter_values", Arrays.asList(companyid));//sessionHandlerImpl.getCompanyid(request)
                                        requestParams1.put("search_values", parent_dimension);
                                        KwlReturnObject parentcustomefielddimentionresult = accMasterItemsDAOobj.getFieldParamsUsingSql(requestParams1);//check parent dimention
                                        if (parentcustomefielddimentionresult.getRecordTotalCount() <= 0) {
                                            throw new AccountingException("Please give valid parent dimension");
                                        }

                                        HashMap<String, Object> filterRequestParams2 = new HashMap<String, Object>();
                                        ArrayList filter_names2 = new ArrayList(), filter_params2 = new ArrayList();
                                        FieldParams parentFieldParams = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), obj1[1].toString());
                                        String grouptid = parentFieldParams.getParentid();
                                        filter_names2.add("field.id");
                                        filter_params2.add(grouptid);
                                        filterRequestParams2.put("filter_names", filter_names2);
                                        filterRequestParams2.put("filter_params", filter_params2);
                                        KwlReturnObject cntResult1 = accMasterItemsDAOobj.getMasterItemsForCustomHire(filterRequestParams2);    // get external parent dimesion
                                        List list = cntResult1.getEntityList();
                                        Iterator itr = list.iterator();
                                        String parentid = null;
                                        while (itr.hasNext()) {
                                            Object[] row = (Object[]) itr.next();
                                            FieldComboData fieldComboData = (FieldComboData) row[0];
                                            if (fieldComboData.getValue().equalsIgnoreCase(parentdimenstionvalue[count])) {  //Find parent dimension value
                                                check = true;
                                                parentid = fieldComboData.getId();
                                                parentValue += parentid + ",";
                                            }
                                        }
                                        if (!check) {
                                            throw new AccountingException("Please give valid parent dimension value");
                                        }
                                    }
                                    //List<FieldComboData> lstFieldComboData = cntResult1.getEntityList();
                                    HashMap requestParam = requestParamsobj;      //AccountingManager.getGlobalParams(request);
                                    String recordID = null;
                                    if (!StringUtil.isNullOrEmpty(item_parent) && "no".equalsIgnoreCase(item_parent)) {
                                        String ModuleId[] = obj1[2].toString().split(",");
                                        for (int i = 0; i < ModuleId.length; i++) {
                                            requestParam.put("name", master_item);
                                            requestParam.put("groupid", ModuleId[i]);
                                            KwlReturnObject result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                                            FieldComboData fieldComboData = ((FieldComboData) result.getEntityList().get(0));
                                            recordID = fieldComboData.getId();
                                            if (!StringUtil.isNullOrEmpty(recordID)) {
                                                HashMap<String, Object> extparentdimension = new HashMap<String, Object>();
                                                extparentdimension.put("chieldValueId", recordID);
                                                accMasterItemsDAOobj.deleteMasterCustomItemMapping(recordID);
                                                String parentValueArray[] = parentValue.split(",");
                                                for (int cnt = 0; cnt < parentValueArray.length; cnt++) {
                                                    extparentdimension.put("parentValueid", parentValueArray[cnt]);
                                                    accMasterItemsDAOobj.addUpdateMasterCustomItemMapping(extparentdimension);
                                                }
                                            }
                                        }
                                    } else {
                                        HashMap<String, String> parentchild = new HashMap<String, String>();
                                        List<FieldComboData> lstFieldComboData1 = cntResult.getEntityList();
                                        int i = 0;
                                        if (cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty()) {
                                            String ModuleId[] = obj1[2].toString().split(",");
                                            for (FieldComboData fcd : lstFieldComboData1) {
                                                parentchild.put(fcd.getFieldid(), fcd.getId());
                                                HashMap requestParam1 = requestParamsobj;  // AccountingManager.getGlobalParams(request);
                                                requestParam1.put("name", master_item);
                                                requestParam1.put("groupid", ModuleId[i++]);
                                                requestParam1.put("parentid", parentchild.get(fcd.getFieldid()));
                                                KwlReturnObject result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam1, false);
                                                FieldComboData fieldComboData = ((FieldComboData) result.getEntityList().get(0));
                                                recordID = fieldComboData.getId();
                                                if (!StringUtil.isNullOrEmpty(recordID)) {
                                                    HashMap<String, Object> extparentdimension = new HashMap<String, Object>();
                                                    extparentdimension.put("chieldValueId", recordID);
                                                    accMasterItemsDAOobj.deleteMasterCustomItemMapping(recordID);
                                                    String parentValueArray[] = parentValue.split(",");
                                                    for (int cnt = 0; cnt < parentValueArray.length; cnt++) {
                                                        extparentdimension.put("parentValueid", parentValueArray[cnt]);
                                                        accMasterItemsDAOobj.addUpdateMasterCustomItemMapping(extparentdimension);
                                                    }
                                                }

                                            }
                                        }
                                    }

                                    // }
                                }
                                if ("no".equalsIgnoreCase(parent_dimension_value) && "no".equalsIgnoreCase(item_parent)) {
                                    HashMap requestParam = requestParamsobj;   //AccountingManager.getGlobalParams(request);
                                    Object obj1[] = (Object[]) customefielddimentionresult.getEntityList().get(0);
                                    String ModuleId[] = obj1[2].toString().split(",");
                                    for (int i = 0; i < ModuleId.length; i++) {
                                        requestParam.put("name", master_item);
                                        requestParam.put("groupid", ModuleId[i]);
                                        KwlReturnObject result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                                    }
                                }

                            } else if (!bothcd) {
                                throw new AccountingException("Please give valid  Master Group item ");
                            }
                        }
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
                    total++;
                }
                cont++;
            }
            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = "Empty file.";
            } else if (success == 0) {
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
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module", Constants.Master_Configuration_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }

    public HashMap getMasterGroupMap() throws ServiceException {
        HashMap MasterGroupMap = new HashMap();
        KwlReturnObject mastergroupresult = accMasterItemsDAOobj.getMasterGroups();
        List<MasterGroup> mastergroupList = mastergroupresult.getEntityList();
        for (MasterGroup mg : mastergroupList) {
            MasterGroupMap.put(mg.getGroupName(), mg.getID());
        }
        return MasterGroupMap;
    }

    public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
            rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }

    public boolean checkForValidation(String storageName) {
        if ("Yes".equalsIgnoreCase(storageName) || "No".equalsIgnoreCase(storageName)) {
            return false;
        }
        return true;
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
}
