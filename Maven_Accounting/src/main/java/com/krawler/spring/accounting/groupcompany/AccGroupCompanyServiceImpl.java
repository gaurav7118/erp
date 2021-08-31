/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.groupcompany;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.tax.service.AccTaxService;
import com.krawler.spring.accounting.vendor.accVendorControllerService;
import com.krawler.spring.accounting.ws.service.MasterService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class AccGroupCompanyServiceImpl implements AccGroupCompanyService {

    private AccGroupCompanyDAO accGroupCompanyDAO;
    private MasterService masterService;
    private accVendorControllerService accVendorControllerServiceObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccTaxService accTaxService;
    private auditTrailDAO auditTrailObj;
    
    
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
    
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

        public void setAccTaxService(AccTaxService accTaxService) {
        this.accTaxService = accTaxService;
    }
 
    public void setMasterService(MasterService masterService) {
        this.masterService = masterService;
    }

    public void setaccGroupCompanyDAO(AccGroupCompanyDAO accGroupCompanyDAO) {
        this.accGroupCompanyDAO = accGroupCompanyDAO;
    }
    
     public void setaccVendorControllerService(accVendorControllerService accVendorControllerServiceObj) {
        this.accVendorControllerServiceObj = accVendorControllerServiceObj;
    }
     
     public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    @Override
    public JSONObject getSubdomains(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        JSONArray jArray = new JSONArray();
        try {
            boolean isSubdomainPresent = accGroupCompanyDAO.isSubdomainpresent(paramJobj);
            if (isSubdomainPresent) {
                KwlReturnObject result = accGroupCompanyDAO.fetchSubdomains(paramJobj);
                List<GroupCompanySubdomainMapping> multiSubdomainObj = result.getEntityList();
                if (multiSubdomainObj.size() > 0) {
                    issuccess = true;
                    for (GroupCompanySubdomainMapping multCSObj : multiSubdomainObj) {
                        JSONObject jobj = new JSONObject();
                        jobj.put(Constants.Acc_id, multCSObj.getID());
                        jobj.put(Constants.RES_CDOMAIN, multCSObj.getSubdomain());
                        jobj.put(Constants.companyKey, multCSObj.getCompanyId());
                        jobj.put("contextUrl", multCSObj.getContextUrl());
                        jobj.put("isparent", multCSObj.isIsparent());
                        jArray.put(jobj);
                    }
                    response.put(Constants.data, jArray);
                }
            }
            
        } catch (Exception ex) {
            issuccess = false;
        } finally {
            response.put(Constants.RES_MESSAGE, msg);
            response.put(Constants.RES_success, issuccess);

        }
        return response;
    }
    
    
    @Override
    public JSONObject getTax(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        StringBuilder notInQuery=new StringBuilder();
        JSONArray mappingjArray=new JSONArray();
        boolean isSourceFlag = Boolean.parseBoolean(paramJobj.optString("isSourceFlag"));
        try {
            String sourcecompanyid = paramJobj.optString("sourcecompanyid");
            String destinationcompanyid = paramJobj.optString("destinationcompanyid");
            paramJobj.put(Constants.isdefaultHeaderMap, true);
            
            HashMap<String, Object> fieldrequestParams = new HashMap();
            String sourcesubdomain = "", destinationsubdomain = "";
            if (!StringUtil.isNullOrEmpty(sourcecompanyid)) {
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sourcecompanyid);
                Company sourceCompany = (Company) soresult.getEntityList().get(0);
                if (sourceCompany != null) {
                    sourcesubdomain = sourceCompany.getSubDomain();
                }
            }

            if (!StringUtil.isNullOrEmpty(destinationcompanyid)) {
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Company.class.getName(), destinationcompanyid);
                Company destinationCompany = (Company) soresult.getEntityList().get(0);
                if (destinationCompany != null) {
                    destinationsubdomain = destinationCompany.getSubDomain();
                }
            }

            if (!StringUtil.isNullOrEmpty(sourcesubdomain) && !StringUtil.isNullOrEmpty(destinationsubdomain)) {
                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, sourcesubdomain);
                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, destinationsubdomain);
                KwlReturnObject lineTaxObj = accGroupCompanyDAO.fetchTaxMappingDetails(fieldrequestParams);
                List<GroupCompanyTaxMapping> lineleveltaxObj = lineTaxObj.getEntityList();
                if (lineleveltaxObj.size() > 0) {
                    for (GroupCompanyTaxMapping taxObj : lineleveltaxObj) {
                        String taxCode = "";
                        JSONObject jObj = new JSONObject();
                        String taxID = "";
                        if (isSourceFlag) {
                            taxCode = taxObj.getSourceTaxCode();
                            taxID = taxObj.getSourceTaxId();
                        } else {
                            taxCode = taxObj.getDestinationTaxCode();
                            taxID = taxObj.getDestinationTaxId();
                        }
                        if (notInQuery.length() > 0) {
                            notInQuery.append("," + taxCode);
                        } else {
                            notInQuery.append(taxCode);
                        }
                        jObj.put("taxid", taxID);
                        jObj.put("taxcode", taxCode);
                        mappingjArray.put(jObj);
                    }
                    paramJobj.put("notinquery", notInQuery.toString());
                }
            }
            
            if (isSourceFlag) {//Purchase Side
                paramJobj.put(Constants.companyKey, sourcecompanyid);
                paramJobj.put("taxtypeid", "1");
                JSONObject purchaseTax = accTaxService.getTax(paramJobj);

                if (notInQuery.length() > 0 && purchaseTax.has(Constants.RES_success) && purchaseTax.optBoolean(Constants.RES_success)) {
                    if (purchaseTax.has(Constants.data)) {
                        response.put("modifiedData", purchaseTax.optJSONArray(Constants.data));
                    }
                    response.put("mappedData", mappingjArray);
                    response.put(Constants.RES_success, true);
                    response.put(Constants.isEdit, true);
                } else {
                    response = purchaseTax;
                }
            }
            
            if (!isSourceFlag) {//Sales Side
                paramJobj.put(Constants.companyKey, destinationcompanyid);
                paramJobj.put("taxtypeid", "2");
                JSONObject salesTax = accTaxService.getTax(paramJobj);
                if (notInQuery.length() > 0 && salesTax.has(Constants.RES_success) && salesTax.optBoolean(Constants.RES_success)) {
                    if (salesTax.has(Constants.data)) {
                        response.put("modifiedData", salesTax.optJSONArray(Constants.data));
                    }
                    response.put("mappedData", mappingjArray);
                    response.put(Constants.RES_success, true);
                    response.put(Constants.isEdit, true);
                } else {
                    response = salesTax;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
   @Override 
    public JSONObject getCustomerVendorRecords(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        StringBuilder notInQuery=new StringBuilder();
        JSONArray mappingjArray=new JSONArray();
        boolean isSourceFlag = Boolean.parseBoolean(paramJobj.optString("isSourceFlag"));
        try {

            String sourcecompanyid = paramJobj.optString("sourcecompanyid");
            String destinationcompanyid = paramJobj.optString("destinationcompanyid");

            HashMap<String, Object> fieldrequestParams = new HashMap();
            String sourcesubdomain = "", destinationsubdomain = "";
            if (!StringUtil.isNullOrEmpty(sourcecompanyid)) {
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sourcecompanyid);
                Company sourceCompany = (Company) soresult.getEntityList().get(0);
                if (sourceCompany != null) {
                    sourcesubdomain = sourceCompany.getSubDomain();
                }
            }

            if (!StringUtil.isNullOrEmpty(destinationcompanyid)) {
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Company.class.getName(), destinationcompanyid);
                Company destinationCompany = (Company) soresult.getEntityList().get(0);
                if (destinationCompany != null) {
                    destinationsubdomain = destinationCompany.getSubDomain();
                }
            }
            
            if (!StringUtil.isNullOrEmpty(sourcesubdomain) && !StringUtil.isNullOrEmpty(destinationsubdomain)) {
                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, sourcesubdomain);
                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, destinationsubdomain);
                KwlReturnObject multiCustomerVendorReturnObj = accGroupCompanyDAO.fetchCustomerVendorDetails(fieldrequestParams);
                List<GroupCompanyCustomerVendorMapping> multiVendCusObj = multiCustomerVendorReturnObj.getEntityList();
                if (multiVendCusObj.size() > 0) {
                    for (GroupCompanyCustomerVendorMapping gcpM : multiVendCusObj) {
                        JSONObject jObj = new JSONObject();
                        String masterCode ="";
                        String masterID="";
                        if (isSourceFlag) {
                            masterCode = gcpM.getSourceMasterCode();
                            masterID = gcpM.getSourceMasterId();
                        } else {
                            masterCode = gcpM.getDestinationMasterCode();
                            masterID = gcpM.getDestinationMasterId();
                        }
                        if (notInQuery.length() > 0) {
                            notInQuery.append("," + masterCode);
                        } else {
                            notInQuery.append(masterCode);
                        }
                        jObj.put("accid",masterID );
                        jObj.put("acccode", masterCode);
                        mappingjArray.put(jObj);
                    }
                    paramJobj.put("notinquery",notInQuery.toString());
                }
            }
            paramJobj.put(Constants.isdefaultHeaderMap, true);
            if (isSourceFlag) {//Purchase Side
                paramJobj.put(Constants.companyKey, sourcecompanyid);
                JSONObject vendorResponse = accVendorControllerServiceObj.getVendorsForCombo(paramJobj);
                if (notInQuery.length() > 0 && vendorResponse.has(Constants.RES_success) && vendorResponse.optBoolean(Constants.RES_success)) {
                    if (vendorResponse.has(Constants.data)) {
                        response.put("modifiedData", vendorResponse.optJSONArray(Constants.data));
                    }
                    response.put("mappedData", mappingjArray);
                    response.put(Constants.RES_success, true);
                    response.put(Constants.isEdit, true);
                } else {
                    response = vendorResponse;
                }
            }

            if (!isSourceFlag) {//Sales Side
                paramJobj.put(Constants.companyKey, destinationcompanyid);
                JSONObject customerResponse = masterService.getCustomers(paramJobj);
                 if (notInQuery.length() > 0 && customerResponse.has(Constants.RES_success) && customerResponse.optBoolean(Constants.RES_success)) {
                    if (customerResponse.has(Constants.data)) {
                        response.put("modifiedData", customerResponse.optJSONArray(Constants.data));
                    }
                    response.put("mappedData", mappingjArray);
                    response.put(Constants.RES_success, true);
                    response.put(Constants.isEdit, true);
                } else {
                    response = customerResponse;
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
   
 @Override  
    public JSONObject getInvoiceTermsRecords(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        JSONArray mappingjArray = new JSONArray();
        StringBuilder notInQuery = new StringBuilder();
        boolean isSourceFlag = Boolean.parseBoolean(paramJobj.optString("isSourceFlag"));
        try {

            String sourcecompanyid = paramJobj.optString("sourcecompanyid");
            String destinationcompanyid = paramJobj.optString("destinationcompanyid");

            paramJobj.put(Constants.isdefaultHeaderMap, true);
            
              HashMap<String, Object> fieldrequestParams = new HashMap();
            String sourcesubdomain = "", destinationsubdomain = "";
            if (!StringUtil.isNullOrEmpty(sourcecompanyid)) {
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sourcecompanyid);
                Company sourceCompany = (Company) soresult.getEntityList().get(0);
                if (sourceCompany != null) {
                    sourcesubdomain = sourceCompany.getSubDomain();
                }
            }

            if (!StringUtil.isNullOrEmpty(destinationcompanyid)) {
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Company.class.getName(), destinationcompanyid);
                Company destinationCompany = (Company) soresult.getEntityList().get(0);
                if (destinationCompany != null) {
                    destinationsubdomain = destinationCompany.getSubDomain();
                }
            }
            
            if (!StringUtil.isNullOrEmpty(sourcesubdomain) && !StringUtil.isNullOrEmpty(destinationsubdomain)) {
                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, sourcesubdomain);
                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, destinationsubdomain);
                KwlReturnObject multiCompanyTermObj = accGroupCompanyDAO.fetchTermMappingDetails(fieldrequestParams);
                List<GroupCompanyTermMapping> multitermObj = multiCompanyTermObj.getEntityList();
                if (multitermObj.size() > 0) {
                    for (GroupCompanyTermMapping termObj : multitermObj) {
                        JSONObject jObj = new JSONObject();
                        String termName = "";
                        String termID = "";
                        if (isSourceFlag) {
                            termName = termObj.getSourceTermName();
                            termID = termObj.getSourceTermId();
                        } else {
                            termName = termObj.getDestinationTermName();
                            termID = termObj.getDestinationTermId();
                        }
                        if (notInQuery.length() > 0) {
                            notInQuery.append("," + termName);
                        } else {
                            notInQuery.append(termName);
                        }
                        jObj.put("id", termID);
                        jObj.put("term", termName);
                        mappingjArray.put(jObj);
                    }
                    paramJobj.put("notinquery", notInQuery.toString());
                }
            }
            
            if (isSourceFlag) {//Purchase Side
                paramJobj.put("isSalesOrPurchase", "false");
                paramJobj.put(Constants.companyKey, sourcecompanyid);
                JSONArray jArray = masterService.getInvoiceTerms(paramJobj);
                
                 if (notInQuery.length() > 0) {
                    response.put("modifiedData", jArray);
                    response.put("mappedData", mappingjArray);
                    response.put(Constants.RES_success, true);
                    response.put(Constants.isEdit, true);
                } else {
                    response.put(Constants.data, jArray);
                    response.put(Constants.RES_success, true);
                    response.put(Constants.RES_TOTALCOUNT, jArray.length());
                }
            }

            if (!isSourceFlag) {//Sales Side
                paramJobj.put(Constants.companyKey, destinationcompanyid);
                paramJobj.put("isSalesOrPurchase", "true");
                JSONArray jArray = masterService.getInvoiceTerms(paramJobj);
                
                if (notInQuery.length() > 0) {
                    response.put("modifiedData", jArray);
                    response.put("mappedData", mappingjArray);
                    response.put(Constants.RES_success, true);
                    response.put(Constants.isEdit, true);
                } else {
                    response.put(Constants.data, jArray);
                    response.put(Constants.RES_success, true);
                    response.put(Constants.RES_TOTALCOUNT, jArray.length());
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
    
    @Override 
   public JSONObject getPurchaseAndSalesModules(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        JSONArray modulejArray=new JSONArray();
        JSONArray mappingjArray=new JSONArray();
        Set<Integer> mappingSet=new HashSet<>();//to check mapping column
        boolean isSourceFlag = Boolean.parseBoolean(paramJobj.optString("isSourceFlag"));
        try {
            
            HashMap<String, Object> fieldrequestParams = new HashMap();
            String sourcecompanyid = paramJobj.optString("sourcecompanyid");
            String destinationcompanyid = paramJobj.optString("destinationcompanyid");
            String sourcesubdomain = "", destinationsubdomain = "";
            if (!StringUtil.isNullOrEmpty(sourcecompanyid)) {
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sourcecompanyid);
                Company sourceCompany = (Company) soresult.getEntityList().get(0);
                if (sourceCompany != null) {
                    sourcesubdomain = sourceCompany.getSubDomain();
                }
            }

            if (!StringUtil.isNullOrEmpty(destinationcompanyid)) {
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Company.class.getName(), destinationcompanyid);
                Company destinationCompany = (Company) soresult.getEntityList().get(0);
                if (destinationCompany != null) {
                    destinationsubdomain = destinationCompany.getSubDomain();
                }
            }
            if (!StringUtil.isNullOrEmpty(sourcesubdomain) && !StringUtil.isNullOrEmpty(destinationsubdomain)) {
                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, sourcesubdomain);
                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, destinationsubdomain);
                KwlReturnObject multiCompanyreturnObj = accGroupCompanyDAO.fetchMultiCompanyDetails(fieldrequestParams);
                List<GroupCompanyProcessMapping> multiCompanyObj = multiCompanyreturnObj.getEntityList();
                if (multiCompanyObj.size() > 0) {
                    for (GroupCompanyProcessMapping gcpM : multiCompanyObj) {
                        JSONObject jObj = new JSONObject();
                        int modulemapped = Integer.parseInt(gcpM.getDestinationModule());
                        if (isSourceFlag) {
                            modulemapped = Integer.parseInt(gcpM.getSourceModule());
                        }
                        mappingSet.add(modulemapped);
                    }
                }
            }
            
            if (isSourceFlag) {//Purchase Side
                JSONObject jObj = new JSONObject();
                jObj.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
                jObj.put(Constants.modulename, "Purchase Order");
                   
                if (mappingSet.contains(Constants.Acc_Purchase_Order_ModuleId)) {
                    mappingjArray.put(jObj);
                } else {
                    modulejArray.put(jObj);
                }
                
                jObj = new JSONObject();
                jObj.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                jObj.put(Constants.modulename, "Vendor Invoice");
                
                if (mappingSet.contains(Constants.Acc_Vendor_Invoice_ModuleId)) {
                    mappingjArray.put(jObj);
                } else {
                    modulejArray.put(jObj);
                }
                jObj = new JSONObject();
                jObj.put(Constants.moduleid, Constants.Acc_Purchase_Return_ModuleId);
                jObj.put(Constants.modulename, "Purchase Return");
                
                if (mappingSet.contains(Constants.Acc_Purchase_Return_ModuleId)) {
                    mappingjArray.put(jObj);
                } else {
                    modulejArray.put(jObj);
                }
                
                jObj = new JSONObject();
                jObj.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                jObj.put(Constants.modulename, "Make Payment");
                
                 if (mappingSet.contains(Constants.Acc_Make_Payment_ModuleId)) {
                    mappingjArray.put(jObj);
                } else {
                    modulejArray.put(jObj);
                }
                 
                jObj = new JSONObject();
                jObj.put(Constants.moduleid, Constants.Acc_Goods_Receipt_ModuleId);
                jObj.put(Constants.modulename, Constants.GOODS_RECEIPT_ORDER);

                if (mappingSet.contains(Constants.Acc_Goods_Receipt_ModuleId)) {
                    mappingjArray.put(jObj);
                } else {
                    modulejArray.put(jObj);
                }
            } else {
                JSONObject jObj = new JSONObject();
                jObj.put(Constants.moduleid, Constants.Acc_Sales_Order_ModuleId);
                jObj.put(Constants.modulename, "Sales Order");

                if (mappingSet.contains(Constants.Acc_Sales_Order_ModuleId)) {
                    mappingjArray.put(jObj);
                } else {
                    modulejArray.put(jObj);
                }

                jObj = new JSONObject();
                jObj.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
                jObj.put(Constants.modulename, "Sales Invoice");
                if (mappingSet.contains(Constants.Acc_Invoice_ModuleId)) {
                    mappingjArray.put(jObj);
                } else {
                    modulejArray.put(jObj);
                }

                jObj = new JSONObject();
                jObj.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
                jObj.put(Constants.modulename, "Sales Return");

                if (mappingSet.contains(Constants.Acc_Sales_Return_ModuleId)) {
                    mappingjArray.put(jObj);
                } else {
                    modulejArray.put(jObj);
                }
                jObj = new JSONObject();
                jObj.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                jObj.put(Constants.modulename, "Receive Payment");
                if (mappingSet.contains(Constants.Acc_Receive_Payment_ModuleId)) {
                    mappingjArray.put(jObj);
                } else {
                    modulejArray.put(jObj);
                }
                
                jObj = new JSONObject();
                jObj.put(Constants.moduleid, Constants.Acc_Delivery_Order_ModuleId);
                jObj.put(Constants.modulename, Constants.Delivery_Order);
                if (mappingSet.contains(Constants.Acc_Delivery_Order_ModuleId)) {
                    mappingjArray.put(jObj);
                } else {
                    modulejArray.put(jObj);
                }
                
            }
            if (mappingSet.size() > 0) {
                response.put("modifiedData", modulejArray);
                response.put("mappedData", mappingjArray);
                response.put(Constants.RES_success, true);
                response.put(Constants.isEdit, true);
            } else {
                response.put(Constants.data, modulejArray);
                response.put(Constants.RES_success, true);
                response.put(Constants.RES_TOTALCOUNT, modulejArray.length());
            }
            
        } catch (Exception ex) {
            Logger.getLogger(AccGroupCompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }   
   
@Override
     @Transactional(isolation = Isolation.READ_UNCOMMITTED, rollbackFor = {ServiceException.class})
      public JSONObject saveGroupCompanyWizardSettings(JSONObject paramJobj) throws JSONException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        String auditID="";
        String sms=" created ";
        String message = " Configuration has been saved successfully";
        StringBuilder messagebuildString = new StringBuilder();
        try {
             auditID = AuditAction.GROUP_COMPANY_CREATED; 
            if (!paramJobj.has("sourcecompanyid") || !paramJobj.has("destinationcompanyid")) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }

            String moduleMapping = paramJobj.optString("purchaseSalesModuleMappingKey");
            String invoiceTermsMapping = paramJobj.optString("purchaseSalesInvoiceTermsMappingKey");
            String taxMapping = paramJobj.optString("purchaseSalesTaxMappingKey");
            String vendorCustomerMapping = paramJobj.optString("vendorcustomerMappingKey");
            String sourcecompanyid = paramJobj.optString("sourcecompanyid");
            String destinationcompanyid = paramJobj.optString("destinationcompanyid");
            String sourcesubdomain = "", destinationsubdomain = "";

            if (!StringUtil.isNullOrEmpty(sourcecompanyid)) {
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sourcecompanyid);
                Company sourceCompany = (Company) soresult.getEntityList().get(0);
                if (sourceCompany != null) {
                    sourcesubdomain = sourceCompany.getSubDomain();
                    paramJobj.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, sourcesubdomain);
                    paramJobj.put(GroupCompanyProcessMapping.SOURCE_COMPANYID, sourcecompanyid);
                }
            }

            if (!StringUtil.isNullOrEmpty(destinationcompanyid)) {
                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Company.class.getName(), destinationcompanyid);
                Company destinationCompany = (Company) soresult.getEntityList().get(0);
                if (destinationCompany != null) {
                    destinationsubdomain = destinationCompany.getSubDomain();
                    paramJobj.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, destinationsubdomain);
                    paramJobj.put(GroupCompanyProcessMapping.DESTINATION_COMPANYID, destinationcompanyid);
                }
            }
            Map<String, Object> fieldrequestParams = new HashMap<String, Object>();

            if (!StringUtil.isNullOrEmpty(sourcesubdomain) && !StringUtil.isNullOrEmpty(destinationsubdomain)) {

                if (!StringUtil.isNullOrEmpty(moduleMapping)) {
                    fieldrequestParams.put("isModuleMapping", true);
                    JSONObject returnObj = accGroupCompanyDAO.deleteExistingRecordsofTable(paramJobj, fieldrequestParams);
                    if (returnObj.has(Constants.RES_success) && returnObj.optBoolean(Constants.RES_success)) {
                        if (returnObj.optInt(Constants.RES_TOTALCOUNT, 0) > 0) {
                            message = " Configuration has been updated successfully";
                            auditID = AuditAction.GROUP_COMPANY_UPDATE; 
                            sms=" updated ";
                        }

                        returnObj = accGroupCompanyDAO.insertRecordsInTable(moduleMapping, fieldrequestParams, paramJobj);
                        if (returnObj.has(Constants.RES_success) && returnObj.optBoolean(Constants.RES_success)) {
                            isSuccess = true;
                            messagebuildString.append(" Module");
                        }

                    }
                }
                fieldrequestParams.clear();
                if (!StringUtil.isNullOrEmpty(vendorCustomerMapping)) {
                    fieldrequestParams.put("isVendCustMapping", true);
                    JSONObject returnObj = accGroupCompanyDAO.deleteExistingRecordsofTable(paramJobj, fieldrequestParams);
                    if (returnObj.has(Constants.RES_success) && returnObj.optBoolean(Constants.RES_success)) {
                        returnObj = accGroupCompanyDAO.insertRecordsInTable(vendorCustomerMapping, fieldrequestParams, paramJobj);
                        if (returnObj.has(Constants.RES_success) && returnObj.optBoolean(Constants.RES_success)) {
                            if (returnObj.optInt(Constants.RES_TOTALCOUNT, 0) > 0) {
                                message = " Configuration has been updated successfully";
                                auditID = AuditAction.GROUP_COMPANY_UPDATE; 
                                sms=" updated ";
                            }

                            isSuccess = true;
                            if (messagebuildString.length() > 0) {
                                messagebuildString.append(",");
                            }
                            messagebuildString.append(" Vendor / Customer");
                        }
                    }
                }
                fieldrequestParams.clear();
                if (!StringUtil.isNullOrEmpty(taxMapping)) {
                    fieldrequestParams.put("isTaxMapping", true);
                    JSONObject returnObj = accGroupCompanyDAO.deleteExistingRecordsofTable(paramJobj, fieldrequestParams);
                    if (returnObj.has(Constants.RES_success) && returnObj.optBoolean(Constants.RES_success)) {
                        returnObj = accGroupCompanyDAO.insertRecordsInTable(taxMapping, fieldrequestParams, paramJobj);
                        if (returnObj.has(Constants.RES_success) && returnObj.optBoolean(Constants.RES_success)) {
                            if (returnObj.optInt(Constants.RES_TOTALCOUNT, 0) > 0) {
                                message = " Configuration has been updated successfully";
                                auditID = AuditAction.GROUP_COMPANY_UPDATE; 
                                sms=" updated ";
                            }

                            isSuccess = true;
                            if (messagebuildString.length() > 0) {
                                messagebuildString.append(",");
                            }
                            messagebuildString.append(" Tax");
                        }
                    }
                }
                fieldrequestParams.clear();
                if (!StringUtil.isNullOrEmpty(invoiceTermsMapping)) {
                    fieldrequestParams.put("isInvoiceTermsMapping", true);
                    JSONObject returnObj = accGroupCompanyDAO.deleteExistingRecordsofTable(paramJobj, fieldrequestParams);
                    if (returnObj.has(Constants.RES_success) && returnObj.optBoolean(Constants.RES_success)) {
                        returnObj = accGroupCompanyDAO.insertRecordsInTable(invoiceTermsMapping, fieldrequestParams, paramJobj);
                        if (returnObj.has(Constants.RES_success) && returnObj.optBoolean(Constants.RES_success)) {
                            if (returnObj.optInt(Constants.RES_TOTALCOUNT, 0) > 0) {
                                message = " Configuration has been updated successfully";
                                auditID = AuditAction.GROUP_COMPANY_UPDATE; 
                                sms=" updated ";
                            }
                            isSuccess = true;
                            if (messagebuildString.length() > 0) {
                                messagebuildString.append(",");
                            }
                            messagebuildString.append(" Invoice Terms Mapping ");
                            
                        }
                    }
                }
            }
            
             Map<String, Object> auditRequestParams=new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
            
            String additionalsauditmessage = "User " + paramJobj.optString(Constants.userfullname) + " has " +sms + messagebuildString.toString() + "mapping for destination subdomain " + destinationsubdomain;
            auditTrailObj.insertAuditLog(auditID, additionalsauditmessage, auditRequestParams, destinationsubdomain);
            messagebuildString.append(message);
            
        } catch (Exception ex) {
            try {
                isSuccess = false;
                messagebuildString.append(" Some error occured while saving the configuration");
                throw ServiceException.FAILURE(messagebuildString.toString(), "", false);
            } catch (ServiceException ex1) {
                Logger.getLogger(AccGroupCompanyDAOImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } finally {
            response.put(Constants.RES_msg, messagebuildString.toString());
            response.put(Constants.RES_success, isSuccess);
        }
        return response;
    }
}
