/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.spring.accounting.salesorder;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.Acc_Contract_Order_ModuleId;
import static com.krawler.common.util.Constants.Acc_Lease_Contract;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
//import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.*;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 *
 * @author krawler
 */
public class accContractController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private fieldDataManager fieldDataManagercntrl;
    private accAccountDAO accAccountDAOobj;
    private String salesOrderId;
    private String salesOrderNumber;
    private accTaxDAO accTaxObj;
    private AccProductModuleService accProductModuleService;
    private accProductDAO accProductObj;
    private exportMPXDAOImpl exportDaoObj;
    private APICallHandlerService apiCallHandlerService; 

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    
    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }
    
    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
    public ModelAndView saveContract(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String billid = "";
        String billno = "";
        String amount = "";
        int pendingApproval = 0;
        boolean issuccess = false;
        boolean isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));;
        boolean isEdit = Boolean.parseBoolean(request.getParameter("isEdit"));;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("COntract_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            List li = saveContract(request);
//              String pendingstatus = (String) li.get(4);
//            if(StringUtil.equal("Pending Approval", pendingstatus)) {
//                pendingApproval=1;
//            }
//           pendingApproval=Integer.parseInt((String)li.get(0));
            billid = (String) li.get(0);
            billno = (String) li.get(1);
//            amount = (String) li.get(3);
//            boolean pendingApprovalFlag = false;
            issuccess = true;
//            if (pendingApproval == 1) {
//                pendingApprovalFlag = true;
//            }
            int istemplate = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("istemplate"))) {
                istemplate = Integer.parseInt(request.getParameter("istemplate"));
            }
            if (istemplate == 1) {
                msg = messageSource.getMessage("acc.field.SalesOrderandTemplatehasbeensavedsuccessfully" + ".", null, RequestContextUtils.getLocale(request));  //+ (pendingApprovalFlag ? messageSource.getMessage("acc.field.butSalesOrderispendingforApproval", null, RequestContextUtils.getLocale(request)) : 
            } else if (istemplate == 2) {
                msg = messageSource.getMessage("acc.field.SalesOrderTemplatehasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.co.save", null, RequestContextUtils.getLocale(request)) + ((pendingApproval == 1) ? messageSource.getMessage("acc.field.butpendingforApproval", null, RequestContextUtils.getLocale(request)) : ".") + "<br/>" + messageSource.getMessage("acc.field.DocumentNo", null, RequestContextUtils.getLocale(request)) + ": <b>" + billno + "</b>";   //"Sales order has been saved successfully";
            }

//            jobj.put("pendingApproval", pendingApprovalFlag);
            jobj.put("SOID", salesOrderId);
            jobj.put("billid", billid);
            jobj.put("billno", billno);
//            jobj.put("amount",amount);

            if(!isEdit) {
                auditTrailObj.insertAuditLog("77", " User " + sessionHandlerImpl.getUserFullName(request) + " has added a new"+(isNormalContract ? "":" Lease")+" contract Details " + salesOrderNumber, request, salesOrderId);
            } else {
                auditTrailObj.insertAuditLog("77", " User " + sessionHandlerImpl.getUserFullName(request) + " has updated a "+(isNormalContract ? "":" Lease")+" contract Details " + salesOrderNumber, request, salesOrderId);
            }
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public List saveContract(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException, JSONException, UnsupportedEncodingException {
        Contract contract = null;
        List newList = new ArrayList();
        int pendingApprovalFlag = 0;
        try {
            int istemplate = request.getParameter("istemplate") != null ? Integer.parseInt(request.getParameter("istemplate")) : 0;
            String taxid = null;
            String customfield = request.getParameter("customfield");
            boolean isNormalContract = false;// if contract is created normaly not from lease module
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNormalContract"))) {
                isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));
            }
//            taxid = request.getParameter("taxid");
//             double taxamount = StringUtil.getDouble(request.getParameter("taxamount"));
            String sequenceformat = request.getParameter("sequenceformat");
            String companyid = sessionHandlerImpl.getCompanyid(request);

            String createdby = sessionHandlerImpl.getUserid(request);
            String modifiedby = sessionHandlerImpl.getUserid(request);

            long createdon = System.currentTimeMillis();
            long updatedon = createdon;

//            boolean isOpeningBalanceOrder = request.getParameter("isOpeningBalanceOrder")!=null?Boolean.parseBoolean(request.getParameter("isOpeningBalanceOrder")):false;

            String entryNumber = request.getParameter("number");
            String soid = request.getParameter("invoiceid");//Need to change
            String costCenterId = request.getParameter("costcenter");
            String nextAutoNumber = "";
            String[] deletedRecsArr = (String[]) request.getParameterValues("deletedServiceDates");
//            String deletedRecs = "";
//            if (deletedRecsArr != null && deletedRecsArr.length > 0) {
//                for (int i = 0; i < deletedRecsArr.length; i++) {
//                    if (i == 0) {
//                        deletedRecs = deletedRecsArr[i];
//                    } else {
//                        deletedRecs += "," + deletedRecsArr[i];
//                    }
//                }
//            }
            boolean isEdit = request.getParameter("isEdit") != null ? StringUtil.getBoolean(request.getParameter("isEdit")) : false;
            boolean isCopy = request.getParameter("copyInv") != null ? StringUtil.getBoolean(request.getParameter("copyInv")) : false;
            boolean isLinkedTransaction = request.getParameter("isLinkedTransaction") != null ? StringUtil.getBoolean(request.getParameter("isLinkedTransaction")) : false;
            boolean islockQuantity = request.getParameter("islockQuantity") != null ? StringUtil.getBoolean(request.getParameter("islockQuantity")) : false;
            double externalCurrencyRate = StringUtil.getDouble(request.getParameter("externalcurrencyrate"));
            HashMap<String, Object> GlobalParams = AccountingManager.getGlobalParams(request);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            HashMap<String, Object> soDataMap = new HashMap<String, Object>();
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            synchronized (this) {
                KwlReturnObject socnt = accSalesOrderDAOobj.getContractCount(entryNumber, companyid,false,soid);
                if (socnt.getRecordTotalCount() > 0 && istemplate != 2) {
                    if (StringUtil.isNullOrEmpty(soid)) {
                    if (sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.contractNumber", null, RequestContextUtils.getLocale(request)) + " " + entryNumber + " " + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request)));
                    }
                    } else {
                        nextAutoNumber = entryNumber;
                        soDataMap.put("id", soid);
                        KwlReturnObject result = accSalesOrderDAOobj.getReplacementAndMaintenance(soid);
                        List list = result.getEntityList();

                        result = accSalesOrderDAOobj.getInvoiceAndDeliveryOrderOfContract(soid);
                        List list1 = result.getEntityList();

                        list.addAll(list1);


                        if (!list.isEmpty()) {
                            throw new AccountingException("Selected record is currently used So it cannot be edited.");
                        }
                        accSalesOrderDAOobj.deleteContractDetails(soid, companyid);
//                        accSalesOrderDAOobj.deleteServiceDetails(soid, companyid, deletedRecs);
                        accSalesOrderDAOobj.deletecontractFiles(soid);
                    }
                } else {
                    boolean seqformat_oldflag = StringUtil.getBoolean(request.getParameter("seqformat_oldflag"));
                    String nextAutoNoInt = "";
                    String datePrefix = "";
                    String dateafterPrefix = "";
                    String dateSuffix = "";
                    if (!sequenceformat.equals("NA")) {
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_CONTRACT, sequenceformat);
                        } else {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_CONTRACT, sequenceformat, seqformat_oldflag, null);// There is no creation date of Contract hence putting null for server date
                            nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateafterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);  //Date After Prefix Part
                            dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                            soDataMap.put(Constants.SEQFORMAT, sequenceformat);
                            soDataMap.put(Constants.SEQNUMBER, nextAutoNoInt);
                            soDataMap.put(Constants.DATEPREFIX, datePrefix);
                            soDataMap.put(Constants.DATEAFTERPREFIX, dateafterPrefix);
                            soDataMap.put(Constants.DATESUFFIX, dateSuffix);
                          
                        }
                        entryNumber = nextAutoNumber;
                    }
                }
                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Contract_Order_ModuleId, entryNumber, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, RequestContextUtils.getLocale(request)) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, RequestContextUtils.getLocale(request)) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, RequestContextUtils.getLocale(request)));
                        }
                    }
                }
            }
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String CustomerId = request.getParameter("customer");
            KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), CustomerId);
            Customer customer = (Customer) custresult.getEntityList().get(0);
            String customerCRMID = customer.getCrmaccountid();  //customer account id in crm 
//            if(!StringUtil.isNullOrEmpty(soid)){//Edit PO Case for updating address detail
//                Map<String, Object> addressParams = new HashMap<String, Object>();
//                String billingAddress=request.getParameter(Constants.BILLING_ADDRESS);
//                if(!StringUtil.isNullOrEmpty(billingAddress)){  //handling the cases when no address coming in edit case 
//                    addressParams = AccountingManager.getAddressParams(request);
//                } else{
//                    addressParams=getCustomerDefaultAddressParams(customer);
//                }
//                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soid);
//                SalesOrder so=(SalesOrder)returnObject.getEntityList().get(0);
//                addressParams.put("id", so.getBillingShippingAddresses()==null?"":so.getBillingShippingAddresses().getID());
//                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams,companyid);
//                BillingShippingAddresses bsa=(BillingShippingAddresses) addressresult.getEntityList().get(0);
//                soDataMap.put("billshipAddressid",bsa.getID());
//            } else{ //Other Cases for saving address detail
//                boolean isDefaultAddress = request.getParameter("defaultAdress") != null? Boolean.parseBoolean(request.getParameter("defaultAdress")): false;
//                Map<String, Object> addressParams=new HashMap<String,Object>();
//                if(isDefaultAddress){
//                 addressParams = getCustomerDefaultAddressParams(customer);  
//                }else{
//                 addressParams = AccountingManager.getAddressParams(request);
//                }
//                KwlReturnObject addressresult = accountingHandlerDAOobj.saveAddressDetail(addressParams,companyid);
//                BillingShippingAddresses bsa=(BillingShippingAddresses) addressresult.getEntityList().get(0);
//                soDataMap.put("billshipAddressid",bsa.getID());
//            }    
            double leaseAmount = StringUtil.isNullOrEmpty(request.getParameter("leaseAmount")) ? 0.0 : Double.parseDouble(request.getParameter("leaseAmount"));
            double securityDeposite = StringUtil.isNullOrEmpty(request.getParameter("securityDeposite")) ? 0.0 : Double.parseDouble(request.getParameter("securityDeposite"));
//            int agreedservices = StringUtil.isNullOrEmpty(request.getParameter("agreedservices")) ? 0 : Integer.parseInt(request.getParameter("agreedservices"));
            soDataMap.put("entrynumber", entryNumber);
            soDataMap.put("isNormalContract", isNormalContract);
            soDataMap.put("autogenerated", nextAutoNumber.equals(entryNumber));
            soDataMap.put("emailid", request.getParameter("emailid"));
            soDataMap.put("numberOfPeriods", request.getParameter("numberOfPeriods"));
            soDataMap.put("frequencyType", request.getParameter("frequencyType"));
            soDataMap.put("memo", request.getParameter("memo"));
            soDataMap.put("contactperson", request.getParameter("contactperson"));
//            soDataMap.put("leaseStatus", request.getParameter("leaseStatus"));
            soDataMap.put("leaseAmount", leaseAmount);
            soDataMap.put("securityDeposite", securityDeposite);
            soDataMap.put("termtype", request.getParameter("leaseTermType"));
            soDataMap.put("termvalue", request.getParameter("termvalue"));
            soDataMap.put("posttext", request.getParameter("posttext"));
//            soDataMap.put("agreedservices", agreedservices);
//            soDataMap.put("isOpeningBalanceOrder",isOpeningBalanceOrder);
            soDataMap.put("customerid", request.getParameter("customer"));
            soDataMap.put("salesorder", request.getParameter("salesorderno"));  //ERP-30712-Sales Order ID
            soDataMap.put("sono", request.getParameter("sono"));        //ERP-30712-Sales Order Number

            soDataMap.put("signinDate", StringUtil.isNullOrEmpty(request.getParameter("signinDate")) ? "" : df.parse(request.getParameter("signinDate")));
            soDataMap.put("originalendDate", StringUtil.isNullOrEmpty(request.getParameter("originalendDate")) ? "" : df.parse(request.getParameter("originalendDate")));
            soDataMap.put("enddate", StringUtil.isNullOrEmpty(request.getParameter("enddate")) ? "" : df.parse(request.getParameter("enddate")));
            soDataMap.put("signdate", StringUtil.isNullOrEmpty(request.getParameter("signinDate")) ? "" : df.parse(request.getParameter("signinDate")));
            soDataMap.put("moveindate", StringUtil.isNullOrEmpty(request.getParameter("moveindate")) ? "" : df.parse(request.getParameter("moveindate")));
            soDataMap.put("moveoutdate", StringUtil.isNullOrEmpty(request.getParameter("moveoutdate")) ? "" : df.parse(request.getParameter("moveoutdate")));
            soDataMap.put("startdate", StringUtil.isNullOrEmpty(request.getParameter("startdate")) ? "" : df.parse(request.getParameter("startdate")));
            soDataMap.put("currencyid", currencyid);
//            if (!StringUtil.isNullOrEmpty(request.getParameter("perdiscount"))) {
//                soDataMap.put("perDiscount", StringUtil.getBoolean(request.getParameter("perdiscount")));
//            }
//            if(!StringUtil.isNullOrEmpty(request.getParameter("discount"))){
//                soDataMap.put("discount", StringUtil.getDouble(request.getParameter("discount")));
//            }
//            if(request.getParameter("shipdate") != null && !StringUtil.isNullOrEmpty(request.getParameter("shipdate"))){
//                soDataMap.put("shipdate", df.parse(request.getParameter("shipdate")));
//            }
//            soDataMap.put("shipvia", request.getParameter("shipvia"));
//            soDataMap.put("fob", request.getParameter("fob"));
            soDataMap.put("termid", request.getParameter("termid"));
//            soDataMap.put("shipaddress", request.getParameter("shipaddress"));
//            soDataMap.put("billto", request.getParameter("billto"));
            soDataMap.put("isfavourite", request.getParameter("isfavourite"));
            soDataMap.put("salesPerson", request.getParameter("salesPerson"));
            if (!StringUtil.isNullOrEmpty(costCenterId)) {
                soDataMap.put("costCenterId", costCenterId);
            }
            soDataMap.put("companyid", companyid);

            soDataMap.put("createdby", createdby);
            soDataMap.put("modifiedby", modifiedby);
            soDataMap.put("createdon", createdon);
            soDataMap.put("updatedon", updatedon);


            KwlReturnObject soresult = accSalesOrderDAOobj.saveContract(soDataMap);
            contract = (Contract) soresult.getEntityList().get(0);

//            if (StringUtil.isNullOrEmpty(soid)) {
                if(isEdit) {    //refer ticket ERP-17512
                HashMap<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("contractid", contract.getID());
                accSalesOrderDAOobj.deleteContractDates(dataMap);
                }
                addContractDates(request, contract);
//            }
            // Save PO Custom Data

            if (!StringUtil.isNullOrEmpty(customfield)) {
                HashMap<String, Object> SOMap = new HashMap<String, Object>();
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", "contract");
                customrequestParams.put("moduleprimarykey", "contractID");
                customrequestParams.put("modulerecid", contract.getID());
                customrequestParams.put("moduleid", isNormalContract?Constants.Acc_Contract_Order_ModuleId:Constants.Acc_Lease_Contract);
                customrequestParams.put("companyid", companyid);
                SOMap.put("id", contract.getID());
                customrequestParams.put("customdataclasspath", Constants.Acc_Contract_Order_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    SOMap.put("contractcustomdataref", contract.getID());
                    accSalesOrderDAOobj.saveContract(SOMap);
                }
            }

            salesOrderId = contract.getID();
            salesOrderNumber = contract.getContractNumber();
            soDataMap.put("id", contract.getID());
            List rowDetails = saveContractRows(request, contract, companyid, currencyid, GlobalParams, externalCurrencyRate);
            String billid = contract.getID();
//            String pendingApprovalFlagnew=String.valueOf(pendingApprovalFlag);
            String billno = contract.getContractNumber();
//            newList.add(pendingApprovalFlagnew);
            newList.add(billid);
            newList.add(billno);
            String salesorder = request.getParameter("salesorderno");
            String fileidstr = request.getParameter("fileidstr");
            if (!StringUtil.isNullOrEmpty(salesorder)) {
                HashMap<String, Object> SOMap = new HashMap<String, Object>();
                SOMap.put("id", salesorder);
                SOMap.put("contractid", contract.getID());
                SOMap.put("companyid", companyid);
                SOMap.put("orderdate", new Date(createdon));
                SOMap.put("isEdit", isEdit);
                SOMap.put("islockQuantity", islockQuantity);
                SOMap.put("isLinkedTransaction", isLinkedTransaction);
                SOMap.put("isCopy", isCopy);
                accSalesOrderDAOobj.saveSalesOrder(SOMap);
            }
            // Save Activity Schedule
            JSONArray crmActivityArray = new JSONArray();
            
            boolean isScheduleIncluded = false;
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("isScheduleIncluded"))) {
                isScheduleIncluded = Boolean.parseBoolean(request.getParameter("isScheduleIncluded"));
            }
            
            String deletedRecs = "";
            
            if (isScheduleIncluded) {
                
                String scheduleId = request.getParameter("scheduleId");
                
                
                if(!StringUtil.isNullOrEmpty(scheduleId)){// calculate deleted services records in case of edit
                    
                    
//                    AssetMaintenanceSchedulerObject schedulerObject = 
                    KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceSchedulerObject.class.getName(), scheduleId);
                    AssetMaintenanceSchedulerObject maintenanceSchedulerObject = (AssetMaintenanceSchedulerObject) scObj.getEntityList().get(0);
                    
                    
                    Set<AssetMaintenanceScheduler> maintenanceSchedulers = maintenanceSchedulerObject.getAssetMaintenanceSchedulers();
                    
                    Iterator mainIt = maintenanceSchedulers.iterator();
                    
                    while (mainIt.hasNext()) {
                        AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) mainIt.next();
                        
//                        if(maintenanceSchedulers.size()==0){
//                            deletedRecs = scheduler.getId();
//                        }else{
                            deletedRecs += scheduler.getId()+",";
//                        }
                        
                    }
                    
                    if(deletedRecs.length()>0){
                        deletedRecs = deletedRecs.substring(0,deletedRecs.length()-1);
                    }
                    
                }
                

                AssetMaintenanceSchedulerObject schedulerObject = accProductModuleService.saveAssetMaintenanceSchedule(request, contract.getID());

                Set<AssetMaintenanceScheduler> maintenanceSchedulers = schedulerObject.getAssetMaintenanceSchedulers();

                Iterator mainIt = maintenanceSchedulers.iterator();

                while (mainIt.hasNext()) {
                    AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) mainIt.next();

                    JSONObject crmObj = new JSONObject();

                    Calendar tempCalendarStartDate = Calendar.getInstance();
                    tempCalendarStartDate.setTimeInMillis(scheduler.getStartDate().getTime());
                    tempCalendarStartDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                    crmObj.put("serviceStartDate", scheduler.getStartDate());
                    crmObj.put("serviceStartDate2", tempCalendarStartDate.getTimeInMillis());
                    crmObj.put("serviceStartDate_yyyyMMdd", scheduler.getStartDate() != null ? authHandler.getFormatedDate(scheduler.getStartDate(), Constants.yyyyMMdd) : "");
//                    crmObj.put("servicedate", scheduler.getStartDate());
//                    crmObj.put("servicedate2", tempCalendarStartDate.getTimeInMillis());

                    Calendar tempCalendarEndDate = Calendar.getInstance();
                    tempCalendarEndDate.setTimeInMillis(scheduler.getEndDate().getTime());
                    tempCalendarEndDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                    crmObj.put("serviceEndDate", scheduler.getEndDate());
                    crmObj.put("serviceEndDate2", tempCalendarEndDate.getTimeInMillis());
                    crmObj.put("serviceEndDate_yyyyMMdd", scheduler.getEndDate() != null ? authHandler.getFormatedDate(scheduler.getEndDate(), Constants.yyyyMMdd) : "");

                    crmObj.put("serviceId", scheduler.getId());
                    crmActivityArray.put(crmObj);
                }
                int agreedServices = 0;
                if(maintenanceSchedulers!=null && maintenanceSchedulers.size()>0){
                    agreedServices = maintenanceSchedulers.size();
                }
                soDataMap.put("agreedservices", agreedServices);
                soresult = accSalesOrderDAOobj.saveContract(soDataMap);
            }
            boolean isActivitysuccess = false;
            if (!StringUtil.isNullOrEmpty(customerCRMID)) {
                isActivitysuccess = createActivityInCRM(companyid, billid, billno, customerCRMID, request, crmActivityArray, deletedRecs, isEdit);
                newList.add(isActivitysuccess);
            }
            if (!isActivitysuccess && isEdit && !StringUtil.isNullOrEmpty(customerCRMID)) {
                throw new AccountingException(messageSource.getMessage("acc.contract.activityexists", null, RequestContextUtils.getLocale(request)));
            }
            if (!StringUtil.isNullOrEmpty(fileidstr)) {
                String[] fileidstrArray = fileidstr.split(",");
                for (int cnt = 0; cnt < fileidstrArray.length; cnt++) {
                    accSalesOrderDAOobj.updateContractFiles(billid, fileidstrArray[cnt]);
                }

            }

            String moduleName =Constants.moduleID_NameMap.get(Acc_Lease_Contract);
            if(isNormalContract){
                moduleName = Constants.moduleID_NameMap.get(Acc_Contract_Order_ModuleId);
            }
            //Send Mail when Purchase Requisition is generated or modified.
            DocumentEmailSettings documentEmailSettings = null;
            KwlReturnObject documentEmailresult = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), sessionHandlerImpl.getCompanyid(request));
            documentEmailSettings = documentEmailresult != null ? (DocumentEmailSettings) documentEmailresult.getEntityList().get(0) : null;
            if (documentEmailSettings != null) {
                boolean sendmail = false;
                boolean isEditMail = false;
                if (StringUtil.isNullOrEmpty(soid)) { 
                    if (documentEmailSettings.isLeaseContractGenerationMail()) { ////Create New Case
                        sendmail = true;
                    }
                } else {
                    isEditMail = true;
                    if (documentEmailSettings.isLeaseContractUpdationMail()) { // edit case  
                         sendmail = true;
                    }
                }
                if (sendmail) {
                    String userMailId="",userName="",currentUserid="";
                    String createdByEmail = "";
                    String createdById = "";
                    HashMap<String, Object> requestParams= AccountingManager.getEmailNotificationParams(request);
                    if(requestParams.containsKey("userfullName")&& requestParams.get("userfullName")!=null){
                        userName=(String)requestParams.get("userfullName");
                    }
                    if(requestParams.containsKey("usermailid")&& requestParams.get("usermailid")!=null){
                        userMailId=(String)requestParams.get("usermailid");
                    }
                    if(requestParams.containsKey(Constants.useridKey)&& requestParams.get(Constants.useridKey)!=null){
                        currentUserid=(String)requestParams.get(Constants.useridKey);
                    }
                    List<String> mailIds = new ArrayList();
                    if (!StringUtil.isNullOrEmpty(userMailId)) {
                        mailIds.add(userMailId);
                    }
                    /*
                     if Edit mail option is true then get userid and Email id of document creator.
                     */
                    if (isEditMail) {
                        if (contract != null && contract.getCreatedby() != null) {
                            createdByEmail = contract.getCreatedby().getEmailID();
                            createdById = contract.getCreatedby().getUserID();
                        }
                        /*
                         if current user userid == document creator userid then don't add creator email ID in List.
                         */
                        if (!StringUtil.isNullOrEmpty(createdByEmail) && !(currentUserid.equalsIgnoreCase(createdById))) {
                            mailIds.add(createdByEmail);
                        }
                    }
                    String[] temp = new String[mailIds.size()];
                    String[] tomailids = mailIds.toArray(temp);
                    String contractNumber = entryNumber;
                    accountingHandlerDAOobj.sendSaveTransactionEmails(contractNumber, moduleName, tomailids, userName, isEditMail, companyid);
                }
            }

//            double soAmount = (Double) rowDetails.get(1);   // + taxamount;
//            ArrayList<String> prodList = (ArrayList<String>) rowDetails.get(2);

//            KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, soAmount, currencyid, df.parse(request.getParameter("billdate")),externalCurrencyRate);
//            soAmount =(Double)bAmt.getEntityList().get(0);
//            String salesamount=String.valueOf(soAmount);
//            newList.add(salesamount);
//            ArrayList amountApprove = (accountingHandlerDAOobj.getApprovalFlagForAmount(soAmount, Constants.SALES_ORDER, Constants.TRANS_AMOUNT,companyid));
//            boolean amountExceed = (Boolean)(amountApprove.get(0));
//            
//            ArrayList prodApprove = (accountingHandlerDAOobj.getApprovalFlagForProducts(prodList, Constants.SALES_ORDER, Constants.TRANS_PRODUCT,companyid));
//            boolean prodExists = (Boolean) (prodApprove.get(0));
//            boolean pendingApprovalFlagForDisc = false;
//            int approvalLevelForDisc = 1;
//            Set<SalesOrderDetail> sodetails = null;
//            sodetails = (HashSet<SalesOrderDetail>) rowDetails.get(0);
//
//            Iterator invitr = sodetails.iterator();
//            while (invitr.hasNext()) {
//                SalesOrderDetail ivd = (SalesOrderDetail) invitr.next();
//                String productId = ivd.getProduct().getID();
//
//                double discountVal = ivd.getDiscount();
//                KwlReturnObject dAmount = accCurrencyDAOobj.getCurrencyToBaseAmount(GlobalParams, discountVal, currencyid, df.parse(request.getParameter("billdate")), externalCurrencyRate);
//                double discAmountinBase = (Double) dAmount.getEntityList().get(0);
//                ArrayList prodApproveDisc = (accountingHandlerDAOobj.getApprovalFlagForProductsDiscount(discAmountinBase, productId, Constants.SALES_ORDER, Constants.TRANS_DISCOUNT, companyid, pendingApprovalFlagForDisc, approvalLevelForDisc));
//                pendingApprovalFlagForDisc = (Boolean) prodApproveDisc.get(0);
//                approvalLevelForDisc = (Integer) prodApproveDisc.get(1);
//
//
//            }

//            pendingApprovalFlag = (istemplate!=2?((amountExceed || prodExists || pendingApprovalFlagForDisc)? 1 : 0):0);//No need of approval if transaction is just created as template.
//            int approvalLevel = ((Integer)(amountApprove.get(1)) >  (Integer)(prodApprove.get(1))) ? (Integer)(amountApprove.get(1)) : (Integer)(prodApprove.get(1));
//            
//            if (approvalLevelForDisc > approvalLevel) {
//                approvalLevel = approvalLevelForDisc;
//            }
//            salesOrder.setPendingapproval(pendingApprovalFlag);            
//            salesOrder.setApprovallevel(approvalLevel);
//            contract.setIstemplate(istemplate);
//            newList.add((pendingApprovalFlag == 1)? "Pending Approval" : "Approved");

//           if(pendingApprovalFlag == 1 && preferences.isSendapprovalmail()){ //this for send approval email
//                String[] emails = {};
//                String invoiceNumber = contract.getSalesOrderNumber();
//                String userName = sessionHandlerImpl.getUserFullName(request);
//                String moduleName = "Sales Order";
//                emails = accountingHandlerDAOobj.getApprovalUserList(request,moduleName,1);
//                if(!StringUtil.isNullOrEmpty(preferences.getApprovalEmails())){
//                  String[] compPrefMailIds = preferences.getApprovalEmails().split(";");  
//                  emails=AccountingManager.getMergedMailIds(emails, compPrefMailIds);
//                }
//                String fromEmailId = Constants.ADMIN_EMAILID;                
//                accountingHandlerDAOobj.sendApprovalEmails(invoiceNumber, userName, emails, fromEmailId, moduleName);
//            }
            //Save record as template
//            if(!StringUtil.isNullOrEmpty(request.getParameter("templatename")) && (istemplate == 1 || istemplate == 2)) {
//                HashMap<String, Object> hashMap = new HashMap<String, Object>();                
//                hashMap.put("templatename", request.getParameter("templatename"));
//                hashMap.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
//                hashMap.put("modulerecordid", salesOrder.getID());
//                hashMap.put("companyid", companyid);
//                accountingHandlerDAOobj.saveModuleTemplate(hashMap);
//            }


//             String salesOrderTerms = request.getParameter("invoicetermsmap");
//            if(StringUtil.isAsciiString(salesOrderTerms)) {
//                mapInvoiceTerms(salesOrderTerms,salesOrder.getID(), sessionHandlerImpl.getUserid(request),false);
//            } 

        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveContract " + ex.getMessage(), ex);
        }
        return newList;
    }

    private boolean createActivityInCRM(String companyid, String billid, String billno, String customerCRMID, HttpServletRequest request, JSONArray crmActivityArray, String deleteServiceArray, boolean isEdit) {
        boolean isActivitysuccess = false;
        //Session session=null;
        try {

            String crmReqmsg = "";
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String action = "203";
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/activity";            
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("action", action);

            //session = HibernateUtil.getCurrentSession();
            JSONObject pjobj = new JSONObject();
            pjobj.put("contractid", (billid != null) ? billid : "");
            pjobj.put("contractno", (billno != null) ? billno : "");
            pjobj.put("isEdit", isEdit ? isEdit : false);
            pjobj.put("customerid", (customerCRMID != null) ? customerCRMID : "");
//            pjobj.put("agreedservices", agreedservices);// no use
            pjobj.put("deleteServiceArray", deleteServiceArray);
//            JSONArray jArr = new JSONArray(request.getParameter("servicedetail"));
            pjobj.put("servicedetail", crmActivityArray);

            userData.put("data", pjobj);

            JSONObject resObj = apiCallHandlerService.restPostMethod(crmURL, userData.toString());
//            JSONObject resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                isActivitysuccess = resObj.getBoolean("success");
                crmReqmsg = resObj.getString(Constants.RES_MESSAGE);
            }

        } catch (Exception ex) {
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, "accContractController.createActivityInCRM", ex);
        }
//        finally{
//            HibernateUtil.closeSession(session);
//        }
        return isActivitysuccess;
    }

    public List saveContractRows(HttpServletRequest request, Contract contract, String companyid, String currencyid, HashMap<String, Object> GlobalParams, double externalCurrencyRate) throws ServiceException, AccountingException, UnsupportedEncodingException {
        HashSet rows = new HashSet();
//        HashSet aggredservicerows = new HashSet();
        List ll = new ArrayList();
        ArrayList<String> prodList = new ArrayList<String>();
        try {
            double totalAmount = 0.0;
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            boolean isNormalContract = false;// if contract is created normaly not from lease module
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNormalContract"))) {
                isNormalContract = Boolean.parseBoolean(request.getParameter("isNormalContract"));
            }
            DateFormat df = authHandler.getDateOnlyFormat(request);
            for (int i = 0; i < jArr.length(); i++) {
                double rowAmount = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                HashMap<String, Object> sodDataMap = new HashMap<String, Object>();
                sodDataMap.put("srno", i + 1);
                sodDataMap.put("companyid", companyid);
                sodDataMap.put("soid", contract.getID());
                sodDataMap.put("productid", jobj.getString("productid"));

                prodList.add(jobj.getString("productid"));

                sodDataMap.put("rate", jobj.getDouble("rate"));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                sodDataMap.put("unitPricePerInvoice", jobj.optDouble("unitPricePerInvoice", 0));//CompanyHandler.getCalCurrencyAmount(session,request,jobj.getDouble("rate"),request.getParameter("currencyid"),null));
                sodDataMap.put("quantity", jobj.getDouble("quantity"));
                if (jobj.has("uomid")) {
                    sodDataMap.put("uomid", jobj.getString("uomid"));
                }
                if (jobj.has("baseuomquantity") && jobj.get("baseuomquantity") != null) {
                    sodDataMap.put("baseuomquantity", jobj.getDouble("baseuomquantity"));
                    sodDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                } else {
                    sodDataMap.put("baseuomquantity", jobj.getDouble("quantity"));
                    sodDataMap.put("baseuomrate", jobj.getDouble("baseuomrate"));
                }
                sodDataMap.put("remark", jobj.optString("remark"));
                String rowtaxid = jobj.getString("prtaxid");
                
                /*-------------When No Tax is applied in Lease Order Product then "None" Value is coming for "prtaxid" 
                 from getSalesOrderrows() function As check is Applied ,if taxid is null then send "None" value for "partaxid" from getSalesOrderrows() function 
                 while selecting Lease Order in Contract form-------------*/
                if (rowtaxid.equalsIgnoreCase("None")) {
                    rowtaxid = "";
                }
                //try {
                    sodDataMap.put("desc", StringUtil.DecodeText(jobj.optString("desc")));
               /*} catch (UnsupportedEncodingException ex) {
                    sodDataMap.put("desc", jobj.optString("desc"));
                }*/

                if (jobj.has("prdiscount") && jobj.get("prdiscount") != null) {
                    sodDataMap.put("discount", jobj.getDouble("prdiscount"));
                }
                if (jobj.has("discountispercent") && jobj.get("discountispercent") != null) {
                    sodDataMap.put("discountispercent", jobj.getInt("discountispercent"));
                }
//                String linkmode=request.getParameter("linkNumber");
//                if (!StringUtil.isNullOrEmpty(linkmode)) 
//                 {
//                
//                       KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(QuotationDetail.class.getName(), jobj.getString("rowid"));
//                        QuotationDetail qod = (QuotationDetail) rdresult.getEntityList().get(0);
//                        //qod.getID();
//                        sodDataMap.put("quotationdetailid",qod.getID());                
//                    
//                }



                KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, jobj.getDouble("rate"), currencyid, df.parse(request.getParameter("startdate")), externalCurrencyRate);

//                rowAmount = (Double) bAmt.getEntityList().get(0) * jobj.getDouble("quantity");
                rowAmount = (Double) jobj.optDouble("rate", 0.0) * jobj.getDouble("quantity");

                if (!StringUtil.isNullOrEmpty(rowtaxid)) {
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                    Tax rowtax = (Tax) txresult.getEntityList().get(0);
                    if (rowtax == null) {
                        throw new AccountingException(messageSource.getMessage("acc.so.taxcode", null, RequestContextUtils.getLocale(request)));
                    } else {
                        sodDataMap.put("rowtaxid", rowtaxid);
                        double rowtaxamount = StringUtil.getDouble(jobj.getString("taxamount"));
                        sodDataMap.put("rowTaxAmount", rowtaxamount);
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(GlobalParams, rowtaxamount, currencyid, df.parse(request.getParameter("startdate")), externalCurrencyRate);
                        rowtaxamount = (Double) bAmt.getEntityList().get(0);

                        rowAmount = rowAmount + rowtaxamount;
                    }
                        
                 }
                //  row.setTax(rowtax);

                KwlReturnObject result = accSalesOrderDAOobj.saveContractDetails(sodDataMap);
                ContractDetail row = (ContractDetail) result.getEntityList().get(0);



//              Save contract Details Custom Data
                String customfield = jobj.getString("customfield");
                if (!StringUtil.isNullOrEmpty(customfield)) {
                    HashMap<String, Object> SOMap = new HashMap<String, Object>();
                    JSONArray jcustomarray = new JSONArray(customfield);

                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", "SalesContractDetail");
                    customrequestParams.put("moduleprimarykey", "ScDetailID");
                    customrequestParams.put("modulerecid", row.getID());
                    customrequestParams.put("moduleid", isNormalContract?Constants.Acc_Contract_Order_ModuleId:Constants.Acc_Lease_Contract);
                    customrequestParams.put("companyid", companyid);
                    SOMap.put("id", row.getID());
                    customrequestParams.put("customdataclasspath", Constants.Acc_Contract_Details_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        SOMap.put("contractcustomdataref", row.getID());
                        accSalesOrderDAOobj.updateContractReference(SOMap);
                    }
                }

                rows.add(row);
//                rows.add(servicerow);
                totalAmount += rowAmount;
            }
            ll.add(rows);
            ll.add(totalAmount);
            ll.add(prodList);
            jArr = new JSONArray(request.getParameter("servicedetail"));
//             SimpleDateFormat sdf=new SimpleDateFormat("EEE, dd MMM yyyy");
            

            JSONArray crmActivityArray = new JSONArray();
            
            

//            for (int i = 0; i < jArr.length(); i++) {
//                JSONObject crmObj = new JSONObject();
//                JSONObject jobj = jArr.getJSONObject(i);
//                HashMap<String, Object> sodDataMap = new HashMap<String, Object>();
//                sodDataMap.put("srno", i + 1);
//                sodDataMap.put("companyid", companyid);
//                sodDataMap.put("soid", contract.getID());
//                String servicedate = jobj.getString("servicedate");
//                String serviceid = jobj.getString("id");
//                if (jobj.has("servicedate")) {
////                        String servicedate=jobj.getString("servicedate");
//                    if (!StringUtil.isNullOrEmpty(servicedate)) {
////                            Date newDate=sdf.parse(servicedate);
//                        sodDataMap.put("serviceDate", df.parse(servicedate));
//                        sodDataMap.put("id", serviceid);
//                        KwlReturnObject result = accSalesOrderDAOobj.saveContractServiceDetails(sodDataMap);
//                        ServiceDetail servicerow = (ServiceDetail) result.getEntityList().get(0);
//                        Calendar tempCalendar = Calendar.getInstance();
//                        tempCalendar.setTimeInMillis(df.parse(servicedate).getTime());
//                        tempCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
//                        crmObj.put("servicedate", servicedate);
//                        crmObj.put("servicedate2", tempCalendar.getTimeInMillis());
//                        crmObj.put("serviceId", servicerow.getID());
//                        crmActivityArray.put(crmObj);
//                        aggredservicerows.add(servicerow);
//
//                    }
//
//                }
//            }
//            ll.add(aggredservicerows);
//            ll.add(crmActivityArray);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveContratctRows : " + ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveContratctRows : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveContratctRows : " + ex.getMessage(), ex);
        }
        return ll;
    }

    public ModelAndView deleteSalesOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteSalesOrders(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.so.del", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView changeContractStatus(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            changeContractStatus(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.contract.terminated", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView changeContractSRStatus(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            changeContractSRStatus(request);
            txnManager.commit(status);
            issuccess = true;
            int statusid = Integer.parseInt(request.getParameter("status"));
            if (statusid == 1) {
                msg = messageSource.getMessage("acc.contract.open", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully";
            } else {
                msg = messageSource.getMessage("acc.contract.closed", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully";
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void changeContractSRStatus(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String contractid = request.getParameter("contractid");
            int statusid = Integer.parseInt(request.getParameter("status"));
            if (!StringUtil.isNullOrEmpty(contractid)) {
                String contractidArray[] = contractid.split(",");
                for (int i = 0; i < contractidArray.length; i++) {
                    accSalesOrderDAOobj.changeContractSRStatus(contractidArray[i], statusid);
                }
            }
        } catch (Exception ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public void changeContractStatus(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String contractid = request.getParameter("contractid");
            if (!StringUtil.isNullOrEmpty(contractid)) {
                boolean terminate = true;
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) cap.getEntityList().get(0);
                if (preferences.isActivateCRMIntegration()) {
                    terminate = terminateActivityInCRM(companyid, contractid, request);
                }
                if (terminate) {
                    String contractidArray[] = contractid.split(",");
                    for (int i = 0; i < contractidArray.length; i++) {
                        accSalesOrderDAOobj.changeContractStatus(contractidArray[i], companyid);
                    }
                }else{
                    throw new AccountingException(" Activities at CRM side not deleted successfully ");
                }
            }
        } catch (Exception ex) {
            throw new AccountingException(ex.getMessage());
        }
    }

    public ModelAndView addContractDates(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = messageSource.getMessage("acc.contractregister.ContractsDatesaddedSuccessfully", null, RequestContextUtils.getLocale(request));
        String billid = "";
        String billno = "";
        String amount = "";
        int pendingApproval = 0;
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("COntract_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {

            String contractId = request.getParameter("contract");
            Contract contract = (Contract) kwlCommonTablesDAOObj.getClassObject(Contract.class.getName(), contractId);
            if (contract != null) {
                addContractDates(request, contract);
                
                // update status of contract in case of renew
                
                updateContractStatus(request, contract);
                
                // Audit trail entry
                
                
                auditTrailObj.insertAuditLog(Constants.Acc_Contract_Order_ModuleId+"", " User "+sessionHandlerImpl.getUserFullName(request) +" has renewed Contrcat  "+ contract.getContractNumber(), request, contractId); 
            }
            issuccess = true;

            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void addContractDates(HttpServletRequest request, Contract contract) throws SessionExpiredException, AccountingException, ServiceException, ParseException {
        HashMap<String, Object> soDataMap = new HashMap<String, Object>();
        HashMap<String, Object> contractData = new HashMap<String, Object>();
        DateFormat df = authHandler.getDateOnlyFormat(request);
        // Save Activity Schedule
        if (contract != null) {
            JSONArray crmActivityArray = new JSONArray();
            boolean isScheduleIncluded = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isScheduleIncluded"))) {
                isScheduleIncluded = Boolean.parseBoolean(request.getParameter("isScheduleIncluded"));
            }
            String deletedRecs = "";
            String isRenewContract=request.getParameter("isRenewContract");
            if (isScheduleIncluded && !StringUtil.isNullOrEmpty(isRenewContract)) {
                String scheduleId = request.getParameter("scheduleId");
                if (!StringUtil.isNullOrEmpty(scheduleId)) {// calculate deleted services records in case of edit
                    KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceSchedulerObject.class.getName(), scheduleId);
                    AssetMaintenanceSchedulerObject maintenanceSchedulerObject = (AssetMaintenanceSchedulerObject) scObj.getEntityList().get(0);
                    Set<AssetMaintenanceScheduler> maintenanceSchedulers = maintenanceSchedulerObject.getAssetMaintenanceSchedulers();
                    Iterator mainIt = maintenanceSchedulers.iterator();
                    while (mainIt.hasNext()) {
                        AssetMaintenanceScheduler scheduler = (AssetMaintenanceScheduler) mainIt.next();
                        deletedRecs += scheduler.getId() + ",";
                    }
                    if (deletedRecs.length() > 0) {
                        deletedRecs = deletedRecs.substring(0, deletedRecs.length() - 1);
                    }
                }
                AssetMaintenanceSchedulerObject schedulerObject = accProductModuleService.saveAssetMaintenanceSchedule(request, contract.getID());
                Set<AssetMaintenanceScheduler> maintenanceSchedulers = schedulerObject.getAssetMaintenanceSchedulers();
                 for(AssetMaintenanceScheduler scheduler : maintenanceSchedulers){
                    try {
                        JSONObject crmObj = new JSONObject();
                        Calendar tempCalendarStartDate = Calendar.getInstance();
                        tempCalendarStartDate.setTimeInMillis(scheduler.getStartDate().getTime());
                        tempCalendarStartDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                        crmObj.put("serviceStartDate", scheduler.getStartDate());
                        crmObj.put("serviceStartDate2", tempCalendarStartDate.getTimeInMillis());
                        Calendar tempCalendarEndDate = Calendar.getInstance();
                        tempCalendarEndDate.setTimeInMillis(scheduler.getEndDate().getTime());
                        tempCalendarEndDate.setTimeZone(TimeZone.getTimeZone("GMT"));
                        crmObj.put("serviceEndDate", scheduler.getEndDate());
                        crmObj.put("serviceEndDate2", tempCalendarEndDate.getTimeInMillis());
                        crmObj.put("serviceId", scheduler.getId());
                        crmActivityArray.put(crmObj);
                    } catch (JSONException ex) {
                        Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                int agreedServices = 0;
                if (maintenanceSchedulers != null && maintenanceSchedulers.size() > 0) {
                    agreedServices = maintenanceSchedulers.size();
                }
                contractData.put("id", contract.getID());
                contractData.put("agreedservices", agreedServices);
                KwlReturnObject soresult = accSalesOrderDAOobj.saveContract(contractData);
                String companyid = contract.getCompany().getCompanyID();
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) cap.getEntityList().get(0);
                if (preferences.isActivateCRMIntegration()) {
                    boolean isActivitysuccess = false;
                    String CustomerId = request.getParameter("customer");
                    KwlReturnObject custresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), CustomerId);
                    Customer customer = (Customer) custresult.getEntityList().get(0);
                    String customerCRMID = customer.getCrmaccountid();  //customer account id in crm 
                    String billid = contract.getID();
                    String billno = contract.getContractNumber();
                    if (!StringUtil.isNullOrEmpty(customerCRMID)) {
                        isActivitysuccess = createActivityInCRM(companyid, billid, billno, customerCRMID, request, crmActivityArray, deletedRecs, true);
                    }
                    if (!isActivitysuccess && !StringUtil.isNullOrEmpty(customerCRMID)) {
                        throw new AccountingException(messageSource.getMessage("acc.contract.activityexists", null, RequestContextUtils.getLocale(request)));
                    }
                }
            }
            
            // save contract dates
            soDataMap.put("enddate", StringUtil.isNullOrEmpty(request.getParameter("enddate")) ? "" : df.parse(request.getParameter("enddate")));
            soDataMap.put("startdate", StringUtil.isNullOrEmpty(request.getParameter("startdate")) ? "" : df.parse(request.getParameter("startdate")));
            soDataMap.put("contractid", contract.getID());
            soDataMap.put(Constants.df, df);
            KwlReturnObject soresult = accSalesOrderDAOobj.getContractDates(soDataMap);
            int count1 = soresult.getRecordTotalCount();
            if (count1 > 0) {
                throw new AccountingException(messageSource.getMessage("acc.contract.contractsDatesExists", null, RequestContextUtils.getLocale(request)));
            } else {
                KwlReturnObject coDates = accSalesOrderDAOobj.saveContractDates(soDataMap);
            }
        }

    }

    public void updateContractStatus(HttpServletRequest request, Contract contract) throws SessionExpiredException, AccountingException, ServiceException, ParseException {
        if (contract != null) {
            HashMap<String, Object> soDataMap = new HashMap<String, Object>();
            soDataMap.put("id", contract.getID());
            soDataMap.put("contractStatus", 1);// in case of renew it will be active again

            KwlReturnObject coDates = accSalesOrderDAOobj.saveContract(soDataMap);
            System.out.println("Contraced Renewed : " + contract.getContractNumber());
        }
    }

    public void deleteSalesOrders(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(jobj.getString("billid"))) {
                    String soid = StringUtil.DecodeText(jobj.optString("billid"));
                    String sono = jobj.getString("billno");
//                    KwlReturnObject result =accSalesOrderDAOobj.getSOforinvoice(soid, companyid);  //for cheching SO is used in invoice or not
//                    int count1 = result.getRecordTotalCount();
//                    if (count1 > 0) {
//                        throw new AccountingException("Selected record(s) is currently used in the Invoices(s). So it cannot be deleted.");
//                    }
//                    else
//                    {
                    accSalesOrderDAOobj.deleteSalesOrder(soid, companyid);
                    auditTrailObj.insertAuditLog("77", " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a sales Order " + sono, request, soid);
                }
            }
            //           }
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        } */catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
    }

    public ModelAndView deleteSalesOrdersPermanent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransaction = deleteSalesOrdersPermanent(request);
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = messageSource.getMessage("acc.so.del", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully;
            } else {
                msg = messageSource.getMessage("acc.field.SalesOrdersexcept", null, RequestContextUtils.getLocale(request)) + linkedTransaction.substring(0, linkedTransaction.length() - 2) + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully;

            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteSalesOrdersPermanent(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String soid = "", sono = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                soid = StringUtil.DecodeText(jobj.optString("billid"));
                sono = jobj.getString("billno");

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("soid", soid);
                requestParams.put("companyid", companyid);
                requestParams.put("sono", sono);
                if (!StringUtil.isNullOrEmpty(soid)) {
                    KwlReturnObject result = accSalesOrderDAOobj.getSOforinvoice(soid, companyid, true);  //for cheching SO is used in invoice or not
                    int count1 = result.getRecordTotalCount();
                    if (count1 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Invoices(s). So it cannot be deleted.");
                        linkedTransaction += sono + ", ";
                        continue;
                    }
                    KwlReturnObject resultd = accSalesOrderDAOobj.getDOforinvoice(soid, companyid, true);  //for cheching SO is used in DO or not
                    int count2 = resultd.getRecordTotalCount();
                    if (count2 > 0) {
//                            throw new AccountingException("Selected record(s) is currently used in the Delivery Order(s). So it cannot be deleted.");
                        linkedTransaction += sono + ", ";
                        continue;
                    }

                    accSalesOrderDAOobj.deleteSalesOrdersPermanent(requestParams);
                    auditTrailObj.insertAuditLog("77", " User " + sessionHandlerImpl.getUserFullName(request) + " has deleted a sales Order Permanantly" + sono, request, soid);
                }
            }
        } /*catch (UnsupportedEncodingException ex) {
            throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, RequestContextUtils.getLocale(request)), ex);
        }*/ catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, RequestContextUtils.getLocale(request)));
        }
        return linkedTransaction;
    }

    public ModelAndView updatePrint(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);

        try {
            KwlReturnObject result = null;

            boolean withInventory = Boolean.parseBoolean(request.getParameter("withInv"));
            boolean quotationFlag = Boolean.parseBoolean(request.getParameter("quotationFlag"));
            String recordids = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("recordids"))) {
                recordids = request.getParameter("recordids");
            }
            ArrayList<String> SOIDList = CustomDesignHandler.getSelectedBillIDs(recordids);
            for (int cnt = 0; cnt < SOIDList.size(); cnt++) {
                HashMap<String, Object> soDataMap = new HashMap<String, Object>();
                soDataMap.put("id", SOIDList.get(cnt));
                soDataMap.put("isprinted", request.getParameter("isprinted"));
                if (!StringUtil.isNullOrEmpty(SOIDList.get(cnt))) {
                    if (withInventory) {
                        result = accSalesOrderDAOobj.saveBillingSalesOrder(soDataMap);
                    } else {
                        if (quotationFlag) {
                            result = accSalesOrderDAOobj.saveQuotation(soDataMap);
                        } else {
                            result = accSalesOrderDAOobj.saveSalesOrder(soDataMap);
                        }
                    }
                }
            }
            msg = messageSource.getMessage("acc.field.setsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (AccountingException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getContractOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = getContractOrdersMap(request);

            KwlReturnObject result = accSalesOrderDAOobj.getContractOrders(requestParams);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractOrdersJsonMerged(request, requestParams, list, DataJArr);

            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "accContractController.getContractOrders : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView exportContractOrders(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray DataJArr = new JSONArray();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = getContractOrdersMap(request);
            // Export all records no limit should apply. 
            requestParams.remove("start");
            requestParams.remove("limit");
            KwlReturnObject result = accSalesOrderDAOobj.getContractOrders(requestParams);
            requestParams.put("isExport",true);
            List list = result.getEntityList();
            int totalCount = result.getRecordTotalCount();
            DataJArr = getContractOrdersJsonMerged(request, requestParams, list, DataJArr);

            jobj.put("data", DataJArr);
            jobj.put("totalCount", totalCount);

            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }
    
    public static HashMap<String, Object> getContractOrdersMap(HttpServletRequest request) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("start", request.getParameter("start"));
        requestParams.put("limit", request.getParameter("limit"));
        requestParams.put("viewfilter", StringUtil.isNullOrEmpty(request.getParameter("viewfilter"))?"0":request.getParameter("viewfilter"));
        requestParams.put("ss", request.getParameter("ss"));
        requestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
        requestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
        requestParams.put("isNormalContract",(!StringUtil.isNullOrEmpty(request.getParameter("isNormalContract")))?Boolean.parseBoolean(request.getParameter("isNormalContract")):false);
        return requestParams;
    }

    public JSONArray getContractOrdersJsonMerged(HttpServletRequest req, HashMap<String, Object> request, List list, JSONArray jArr) throws ServiceException {
        try {

            String companyid=sessionHandlerImpl.getCompanyid(req);
            DateFormat df = authHandler.getDateOnlyFormat(req);
             boolean detailedXls =false;
             /*
              *isSchedule is true is user edit total event
              */
             boolean isScheduleEdit =false;
            if (list != null && !list.isEmpty()) {
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                boolean isNormalContract=Boolean.FALSE.parseBoolean(req.getParameter("isNormalContract"));
                boolean isExport = request.containsKey("isExport")?!StringUtil.isNullOrEmpty(request.get("isExport").toString()):false ? Boolean.parseBoolean(request.get("isExport").toString()) : false;
                boolean isForReport = StringUtil.isNullOrEmpty(req.getParameter("isForReport")) ? false : Boolean.parseBoolean(req.getParameter("isForReport"));
                if (req.getParameter("type") != null && req.getParameter("type").equals("detailedXls")) {
                    detailedXls = true;
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, isNormalContract?Constants.Acc_Contract_Order_ModuleId : Constants.Acc_Lease_Contract));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Contract contract = (Contract) itr.next();
                    JSONObject jobj = new JSONObject();
                    String soNumber = "";
                    String contractInvoice = "";
                    String contractDO = "";
                    String contractDOlinkto = "";
                    if (contract.getSalesOrderNumber() != null) {
                        String soID = contract.getSalesOrderNumber();
                        KwlReturnObject result = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), soID);
                        SalesOrder so = (SalesOrder) result.getEntityList().get(0);
                        if (so != null) {
                            soNumber = so.getSalesOrderNumber();
                        }
                    }

                    KwlReturnObject contractinvoiceresult = accSalesOrderDAOobj.getContractInvoice(contract);
                    List lst = contractinvoiceresult.getEntityList();
                    Iterator iter = lst.iterator();
                    while (iter.hasNext()) {
                        contractInvoice = (String) iter.next();
                    }
                    KwlReturnObject contractDOresult = accSalesOrderDAOobj.getContractDO(contract);
                    List lstdo = contractDOresult.getEntityList();
                    Iterator iterdo = lstdo.iterator();
                    while (iterdo.hasNext()) {
                        Object[] row = (Object[]) iterdo.next();
                        contractDO = row[0] != null ? (String) row[0] : "";
                        contractDOlinkto = row[1] != null ? (String) row[1] : "";

                    }
                    KwlReturnObject contractendate = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
                    lst = contractendate.getEntityList();
                    iter = lst.iterator();
                    while (iter.hasNext()) {
                        Object[] row = (Object[]) iter.next();
                        jobj.put("startdate", row[0] != null ? df.format(row[0]) : null);
                        jobj.put("enddate", row[1] != null ? df.format(row[1]) : null);
                    }
                    //*** Attachments Documents SJ[ERP-16331] 
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", contract.getID());
                    hashMap.put("companyid", contract.getCompany().getCompanyID());
                    KwlReturnObject object =accountingHandlerDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    jobj.put("attachment", attachemntcount);
                    //*** Attachments Documents SJ[ERP-16331]     
                    jobj.put("cid", contract.getID());
                    jobj.put("billid", contract.getID());
                    jobj.put("contractid", contract.getContractNumber());
                    jobj.put("sequenceformatid", (contract.getSeqformat()!=null)?contract.getSeqformat().getID():"");
                    jobj.put("invoiceid", contractInvoice);
                    jobj.put("doid", contractDO);
                    jobj.put("dolinkto", contractDOlinkto);
                    jobj.put("salesorder", soNumber);
                    jobj.put("salesorderid", contract.getSalesOrderNumber());
                    jobj.put("accname", (contract.getCustomer() != null) ? contract.getCustomer().getName() : "");
                    jobj.put("accid", (contract.getCustomer() != null) ? contract.getCustomer().getID() : "");
                    jobj.put(Constants.HAS_ACCESS, contract.getCustomer() != null ? contract.getCustomer().isActivate() : true);
                    HashMap<String, Object> addressParams = new HashMap<String, Object>();
                    addressParams.put("companyid", companyid);
                    addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
                    addressParams.put("isBillingAddress", true);    //true to get billing address
                    addressParams.put("customerid", contract.getCustomer().getID());
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    jobj.put("billingAddress1", customerAddressDetails!=null?customerAddressDetails.getAddress():"");
                    jobj.put("contactperson", contract.getContactPerson());
                    jobj.put("emailid", contract.getEmailID());
                    jobj.put("agreedservices", contract.getAgreedServices());
                    jobj.put("currencyid", contract.getCurrency().getCurrencyID());
                    jobj.put("currencyname", contract.getCurrency().getName());
                    jobj.put("currencysymbol", isExport ? contract.getCurrency().getCurrencyCode():contract.getCurrency().getSymbol()); //ERP-30701 : For Export, show currency code instead of symbol(HTML Code)
                    jobj.put("contactperson", contract.getContactPerson());
                    jobj.put("leaseterm", contract.getTermType());
                    jobj.put("termvalue", contract.getTermValue());
                    jobj.put("leasestatus", contract.getCstatus());
                    jobj.put("goodsreturnststus", contract.getSrstatus());
                    if (isExport) {
                        if (contract.getCstatus() == 1) {
                            jobj.put("leasestatus", "Active");
                        } else if (contract.getCstatus() == 2) {
                            jobj.put("leasestatus", "Terminated");
                        } else if (contract.getCstatus() == 3) {
                            jobj.put("leasestatus", "Expired");
                        } else if (contract.getCstatus() == 4) {
                            jobj.put("leasestatus", "Renew");
                        } else {
                            jobj.put("leasestatus", "");
                        }
                        if(contract.getSrstatus() == 1){
                            jobj.put("goodsreturnststus", "Pending");
                        } else if(contract.getSrstatus() == 2){
                            jobj.put("goodsreturnststus", "Pending & Closed");
                        } else if(contract.getSrstatus() == 3){
                            jobj.put("goodsreturnststus", "Done");
                        } else if(contract.getSrstatus() == 4){
                            jobj.put("goodsreturnststus", "Done & Closed");
                        } else {
                            jobj.put("goodsreturnststus", "");
                        }
                    }
                    jobj.put("leaseamount", contract.getAmount());
                    jobj.put("securitydeposite", contract.getDepositAmount());
                    jobj.put("frequencyType", contract.getInvoiceFrequency());
                    jobj.put("numberOfPeriods", contract.getNumberOfPeriods());
                    jobj.put("orgenddate", contract.getOriginalEndDate() != null ? df.format(contract.getOriginalEndDate()) : "");
                    jobj.put("signindate", contract.getSignDate() != null ? df.format(contract.getSignDate()) : "");
                    jobj.put("moveindate", contract.getMoveDate() != null ? df.format(contract.getMoveDate()) : "");
                    jobj.put("moveoutdate", contract.getMoveOutDate() != null ? df.format(contract.getMoveOutDate()) : "");
                    jobj.put("memo", contract.getMemo());
                    
                    
                    KwlReturnObject result = accSalesOrderDAOobj.getReplacementAndMaintenance(contract.getID());

                    List maintenancelist = result.getEntityList();

                    result = accSalesOrderDAOobj.getInvoiceAndDeliveryOrderOfContract(contract.getID());
                    List indolist = result.getEntityList();

                    maintenancelist.addAll(indolist);

                    if(!maintenancelist.isEmpty()){
                        jobj.put("usedintransaction", true);
                    }else{
                        jobj.put("usedintransaction", false);
                    }
                    
                    Set<AssetMaintenanceSchedulerObject> schedulerObjects = contract.getSchedulerObjects();

                    if (schedulerObjects != null && !schedulerObjects.isEmpty()) {
                        Iterator it = schedulerObjects.iterator();

                        if (it.hasNext()) {
                            AssetMaintenanceSchedulerObject schedulerObject = (AssetMaintenanceSchedulerObject) it.next();

                            jobj.put("scheduleId", schedulerObject.getId());
                            jobj.put("scheduleNumber", schedulerObject.getScheduleName());
//                            jobj.put("assetDetailsId", schedulerObject.getAssetDetails().getId());
                            jobj.put("scheduleStartDate", df.format(schedulerObject.getStartDate()));
                            jobj.put("scheduleEndDate", (schedulerObject.getEndDate() != null) ? df.format(schedulerObject.getEndDate()) : "");
                            jobj.put("totalEvents", schedulerObject.getTotalEvents());
                            jobj.put("eventDuration", schedulerObject.getScheduleDuration());
                            jobj.put("frequency", schedulerObject.getFrequency());
                            jobj.put("frequencyType", schedulerObject.getFrequencyType());
                            jobj.put("isAdhoc", schedulerObject.isAdHoc());
                            jobj.put("scheduleStopCondition", schedulerObject.getScheduleStopCondition());


                            if (schedulerObject.isAdHoc() && schedulerObject.getAssetMaintenanceSchedulers() != null) {
                                JSONArray adhocArray = new JSONArray();
                                Set<AssetMaintenanceScheduler> scheduleDetails = schedulerObject.getAssetMaintenanceSchedulers();
                                for (AssetMaintenanceScheduler details : scheduleDetails) {
                                    JSONObject adjobj = new JSONObject();
                                    adjobj.put("eventStartDate", (details.getStartDate() != null) ? df.format(details.getStartDate()) : "");
                                    adjobj.put("eventEndDate", (details.getEndDate() != null) ? df.format(details.getEndDate()) : "");

                                    adhocArray.put(adjobj);
                                }
                                jobj.put("adHocEventDetails", adhocArray.toString());
                            }
                            /*
                             * To show data in edit grid to display and edit event 
                             */
                            if (!schedulerObject.isAdHoc() && schedulerObject.getAssetMaintenanceSchedulers() != null) {
                                JSONArray adhocArray = new JSONArray();
                                Set<AssetMaintenanceScheduler> scheduleDetails = schedulerObject.getAssetMaintenanceSchedulers();
                                for (AssetMaintenanceScheduler details : scheduleDetails) {
                                    JSONObject adjobj = new JSONObject();
                                    adjobj.put("eventStartDate", (details.getStartDate() != null) ? df.format(details.getStartDate()) : "");
                                    adjobj.put("eventEndDate", (details.getEndDate() != null) ? df.format(details.getEndDate()) : "");
                                    if (details.isIsScheduleEdit()) {
                                        isScheduleEdit = true;
                                    }
                                   
                                    adhocArray.put(adjobj);
                                }
                                if (isScheduleEdit) {
                                    jobj.put("isScheduleEdit", isScheduleEdit);
                                }
                                jobj.put("adHocEventDetailsEdit", adhocArray.toString());
                            }

                        }
                    }

                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    ContractCustomData contractCustom = (ContractCustomData) contract.getContractCustomData();
                    replaceFieldMap = new HashMap<String, String>();
                    if (contractCustom != null) {
                        AccountingManager.setCustomColumnValues(contractCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        params.put("isExport", isForReport);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jobj, params);
                        
//                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
//
//                            String coldata = varEntry.getValue().toString();
//                            if (customFieldMap.containsKey(varEntry.getKey())) {
//                                String value = "";
//                                String Ids[] = coldata.split(",");
//                                for (int i = 0; i < Ids.length; i++) {
//                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
//                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
//                                    if (fieldComboData != null) {
//                                        if ((fieldComboData.getField().getFieldtype() == 12 || fieldComboData.getField().getFieldtype() == 7)) {
//                                            value += Ids[i] != null ? Ids[i] + "," : ",";
//                                        } else {
//                                            value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
//                                        }
//                                    }
//                                }
//                                if (!StringUtil.isNullOrEmpty(value)) {
//                                    value = value.substring(0, value.length() - 1);
//                                }
//                                jobj.put(varEntry.getKey(), value);
//                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
//                                jobj.put(varEntry.getKey(), coldata);
//                            } else {
//                                if (!StringUtil.isNullOrEmpty(coldata)) {
//                                    jobj.put(varEntry.getKey(), coldata);
//                                }
//                            }
//                        }
                    }
                    jArr.put(jobj);
                    if(detailedXls){ // Get Contract Rows Json for Export
                        req.setAttribute("contractid", contract.getID());
                        JSONArray jrowArry = getContractOrderRows(req).getJSONArray("data");
                        for (int i=0; i < jrowArry.length();i++){
                            jArr.put(jrowArry.get(i));
                }
            }
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("accContractController.getContractOrdersJsonMerged : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView getContractOrderRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getContractOrderRows(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accContractController.getContractOrderRows:" + ex.getMessage();
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getContractOrderRows(HttpServletRequest request) throws SessionExpiredException, ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
        try {
            String contractid;
            if (request.getAttribute("contractid") != null) {
                contractid = request.getAttribute("contractid").toString();
            } else {
                contractid = request.getParameter("contractid");
            }
            DateFormat userDateFormat=new SimpleDateFormat(sessionHandlerImpl.getUserDateFormat(request));
            boolean isExport = false;// if contract is created normaly not from lease module
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("isForReport"))) {
                isExport = Boolean.parseBoolean(request.getParameter("isForReport"));
            } else if (!StringUtil.isNullOrEmpty(request.getParameter("isExport"))) {
                isExport = Boolean.parseBoolean(request.getParameter("isExport"));
            }
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray();
            String description="";

            HashMap<String, Object> contaractRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("contract.ID");
            filter_params.add(contractid);
            filter_names.add("company.companyID");
            filter_params.add(companyid);
            order_by.add("srno");
            order_type.add("asc");
            contaractRequestParams.put("filter_names", filter_names);
            contaractRequestParams.put("filter_params", filter_params);
            contaractRequestParams.put("order_by", order_by);
            contaractRequestParams.put("order_type", order_type);
            
            HashMap<String, Object> fieldrequestParams1 = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
            fieldrequestParams1.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Contract_Order_ModuleId,1));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap, customFieldMap, customDateFieldMap);
            
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("id", companyid);
            Object preferencesArray = (Object) kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"lineLevelTermFlag"}, paramsMap);     
            int lineLevelTermFlag =(Integer) preferencesArray;         
        
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Contract.class.getName(), contractid);
            Contract contract = (Contract) result.getEntityList().get(0);

            KWLCurrency currency = null;
            if (contract.getCurrency() != null) {
                currency = contract.getCurrency();
            }

            KwlReturnObject codresult = accSalesOrderDAOobj.getContractOrderDetails(contaractRequestParams);
            Iterator itr = codresult.getEntityList().iterator();

            while (itr.hasNext()) {
                ContractDetail row = (ContractDetail) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("srno", row.getSrno());
                obj.put("rowid", (row.getID() != null)? row.getID() : "");
                obj.put("pid", (row.getProduct() != null)? row.getProduct().getProductid() : "");
                obj.put("productcode", (row.getProduct() !=null)? row.getProduct().getProductid() : "");
                obj.put("productid", (row.getProduct() !=null)? row.getProduct().getID() : "");
                obj.put("productname", (row.getProduct() !=null)? row.getProduct().getName() : "");
                obj.put("quantity", row.getQuantity());
                String uom = row.getUom() != null ? row.getUom().getNameEmptyforNA() : row.getProduct().getUnitOfMeasure() == null ? "" : row.getProduct().getUnitOfMeasure().getNameEmptyforNA();
                obj.put("unitname", uom);
                obj.put("uomname", uom);
                obj.put("baseuomname", row.getProduct().getUnitOfMeasure()==null?"":row.getProduct().getUnitOfMeasure().getNameEmptyforNA());
                obj.put("multiuom", row.getProduct().isMultiuom());
                if (row.getUom() != null) {
                    obj.put("uomid", row.getUom().getID());
                } else {
                    obj.put("uomid", row.getProduct().getUnitOfMeasure() != null ? row.getProduct().getUnitOfMeasure().getID() : "");
                }
                
                if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                    description = row.getDescription();
                } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                    description = row.getProduct().getDescription();
                } else {
                    description = "";
                }
                obj.put("desc", description);
                obj.put("description", description);
                obj.put("type",row.getProduct().getProducttype()==null?"":row.getProduct().getProducttype().getName());
                obj.put("memo", row.getRemark());
                obj.put("rate", authHandler.round(row.getRate(), companyid));
                obj.put("unitPricePerInvoice", row.getUnitPricePerInvoice());
                obj.put("discountispercent", row.getDiscountispercent());
                obj.put("prdiscount", row.getDiscount());
                obj.put("currencysymbol", (isExport && currency != null) ? currency.getCurrencyCode(): (currency != null ? currency.getSymbol() : "")); //ERP-30701 : For Export, show currency code instead of symbol(HTML Code)
                double baseuomrate = row.getBaseuomrate();
                obj.put("baseuomrate", baseuomrate);
                double quantity = row.getQuantity();
                obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity,baseuomrate, companyid));
                
                Map<String, Object> variableMap = new HashMap<String, Object>();
                ContractDetailCustomData contractDetailCustom = (ContractDetailCustomData) row.getContractdetailcustomdata();
                AccountingManager.setCustomColumnValues(contractDetailCustom, FieldMap, replaceFieldMap, variableMap);
                if (contractDetailCustom != null) {
                    JSONObject params = new JSONObject();
                    params.put("isExport", isExport);
                    params.put("isForReport", isExport);
                    params.put(Constants.userdf, userDateFormat);
                    fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                }

                double rowTaxPercent = 0;
                double rowTaxAmount = 0;
                if (row.getTax() != null) {
//                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                        KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, row.getContract().getOrderDate(), row.getTax().getID());
                        rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    
                        rowTaxAmount = row.getRowTaxAmount();
                        
                }
                obj.put("prtaxpercent", rowTaxPercent);
                obj.put("rowTaxAmount", rowTaxAmount);
                obj.put("recTermAmount", row.getRowTermAmount());
                obj.put("prtaxid",row.getTax()==null?"": row.getTax().getID());
                
                
                double tempAmount = row.getRate() * row.getQuantity();
                double amount = 0;
                if (row.getDiscountispercent() == 0) {
                    amount = tempAmount - row.getDiscount();//Flat
                } else {
                    amount = tempAmount - (tempAmount * (row.getDiscount() / 100));//Percentage Value
                }
                obj.put("amount", authHandler.round((amount+rowTaxAmount), companyid));
                
                JSONArray TermdetailsjArr = new JSONArray();
                if (lineLevelTermFlag == 1) { // For India Country 
                    
                        HashMap<String, Object> contractDetailParams = new HashMap<String, Object>();
                        contractDetailParams.put("contractDetailId", row.getID());
                        KwlReturnObject contractDetailTermsMapresult = accSalesOrderDAOobj.getContractDetailTermsMap(contractDetailParams);
                        List<ContractDetailTermsMap> contractDetailTermsMapList = contractDetailTermsMapresult.getEntityList();
                        for (ContractDetailTermsMap invoicedetailTermMap : contractDetailTermsMapList) {
                            LineLevelTerms mt = invoicedetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms();
                            com.krawler.utils.json.base.JSONObject jsonObj = new com.krawler.utils.json.base.JSONObject();
                            jsonObj.put("id", mt.getId());

                            /**
                             * ERM-886
                             */
                            jsonObj.put("productentitytermid", invoicedetailTermMap.getEntitybasedLineLevelTermRate() != null ? invoicedetailTermMap.getEntitybasedLineLevelTermRate().getId() : "");
                            jsonObj.put("isDefault", invoicedetailTermMap.isIsGSTApplied());
                            jsonObj.put("term", mt.getTerm());
                            jsonObj.put("formulaids", mt.getFormula());
                            jsonObj.put("termamount", invoicedetailTermMap.getTermamount());
                            jsonObj.put("termpercentage", invoicedetailTermMap.getPercentage());
                            jsonObj.put("originalTermPercentage", mt.getPercentage());
                            jsonObj.put("glaccountname", mt.getAccount().getName());
                            jsonObj.put("accountid", mt.getAccount().getID());
                            jsonObj.put("glaccount", mt.getAccount().getID());
                            jsonObj.put("formType", !StringUtil.isNullOrEmpty(mt.getFormType()) ? mt.getFormType() : "1");
                            jsonObj.put("IsOtherTermTaxable", mt.isOtherTermTaxable());
                            jsonObj.put("assessablevalue", invoicedetailTermMap.getAssessablevalue());
                            jsonObj.put("purchasevalueorsalevalue", invoicedetailTermMap.getPurchaseValueOrSaleValue());
                            jsonObj.put("deductionorabatementpercent", invoicedetailTermMap.getDeductionOrAbatementPercent());
                            jsonObj.put("taxtype", invoicedetailTermMap.getTaxType());
                            jsonObj.put("taxvalue", invoicedetailTermMap.getPercentage());
                            jsonObj.put("termtype", mt.getTermType());
                            jsonObj.put("termsequence", mt.getTermSequence());
                            jsonObj.put(IndiaComplianceConstants.GST_CESS_TYPE, invoicedetailTermMap.getEntitybasedLineLevelTermRate() != null && invoicedetailTermMap.getEntitybasedLineLevelTermRate().getCessType() != null ? invoicedetailTermMap.getEntitybasedLineLevelTermRate().getCessType().getId() : "");
                            jsonObj.put(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT, invoicedetailTermMap.getEntitybasedLineLevelTermRate() != null ? invoicedetailTermMap.getEntitybasedLineLevelTermRate().getValuationAmount() : 0.0);
                            jsonObj.put(IndiaComplianceConstants.DEFAULT_TERMID, mt.getDefaultTerms() != null && mt.getDefaultTerms() != null ? mt.getDefaultTerms() : "");
                            TermdetailsjArr.put(jsonObj);
                        }                    
                }
                obj.put("LineTermdetails", TermdetailsjArr);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }

    public ModelAndView getAggredServicesRows(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getAggredServicesRows(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accContractController.getAggredServicesRows:" + ex.getMessage();
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getAggredServicesRows(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String contractid = request.getParameter("contractid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray();
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> serviceRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("contract.ID");
            filter_params.add(contractid);
            filter_names.add("company.companyID");
            filter_params.add(companyid);
//            order_by.add("srno");
//            order_type.add("asc");
            serviceRequestParams.put("filter_names", filter_names);
            serviceRequestParams.put("filter_params", filter_params);
//            serviceRequestParams.put("order_by", order_by);
//            serviceRequestParams.put("order_type", order_type);


            KwlReturnObject codresult = accSalesOrderDAOobj.getContractAgreedServices(serviceRequestParams);
            Iterator itr = codresult.getEntityList().iterator();

            while (itr.hasNext()) {
                ServiceDetail row = (ServiceDetail) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("rowid", (row.getID() != null) ? row.getID() : "");
                obj.put("id", (row != null) ? row.getID() : "");
                obj.put("servicedate", (row.getServiceDate() != null) ? df.format(row.getServiceDate()) : "");

                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }

    public ModelAndView getContractFiles(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = getContractFiles(request);
            issuccess = true;
        } catch (Exception ex) {
            msg = "accContractController.getContractFiles:" + ex.getMessage();
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getContractFiles(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            String contractid = request.getParameter("contractid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray();
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> serviceRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("contractid");
            filter_params.add(contractid);
//            filter_names.add("company.companyID");
//            filter_params.add(companyid);
//            order_by.add("srno");
//            order_type.add("asc");
            serviceRequestParams.put("filter_names", filter_names);
            serviceRequestParams.put("filter_params", filter_params);
//            serviceRequestParams.put("order_by", order_by);
//            serviceRequestParams.put("order_type", order_type);


            KwlReturnObject codresult = accSalesOrderDAOobj.getContractFiles(serviceRequestParams);
            Iterator itr = codresult.getEntityList().iterator();

            while (itr.hasNext()) {
                ContractFiles row = (ContractFiles) itr.next();
                JSONObject obj = new JSONObject();

                obj.put("id", (row.getId() != null) ? row.getId() : "");
//                obj.put("filename", (row.getName()!= null)? row.getName() : "");
//                obj.put("state", (row.getDeleted()!= 0)? row.getDeleted() : 0);
//                obj.put("type", row.getType());
//                obj.put("extn", row.getExtn()!=null?row.getExtn():"");

                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }
        return jobj;
    }
    public ModelAndView deleteContract(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String linkedTransaction = deleteContract(request);
            txnManager.commit(status);
            issuccess = true;
            msg = messageSource.getMessage("acc.contract.deleted", null, RequestContextUtils.getLocale(request));   //"Sales Order has been deleted successfully";
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accSalesOrderController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String deleteContract(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String contractid = request.getParameter("contractid");
            String contractno = request.getParameter("contractno");
            String constarctStatus = StringUtil.isNullOrEmpty(request.getParameter("contractstatus"))?"1":request.getParameter("contractstatus").toString();// 2 - Terminate , 1 -Active
            String scheduleName = StringUtil.isNullOrEmpty(request.getParameter("scheduleName"))?"":request.getParameter("scheduleName").toString();
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("id", companyid);
            Object preferencesArray = (Object) kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"lineLevelTermFlag"}, paramsMap);     
            int lineLevelTermFlag =(Integer) preferencesArray;      
            if (!StringUtil.isNullOrEmpty(contractid)) {
                KwlReturnObject result = accSalesOrderDAOobj.getReplacementAndMaintenance(contractid);
                int replacementCount = result.getRecordTotalCount();

                result = accSalesOrderDAOobj.getInvoiceAndDeliveryOrderOfContract(contractid);
                int invoiceAndDOCount = result.getRecordTotalCount();
                boolean isdeleted = deleteActivityInCRM(companyid, contractid, contractno,request );
               if (replacementCount > 0 || invoiceAndDOCount > 0 || !isdeleted) {
                    throw new AccountingException("Sorry, you cannot delete the contract as it is in use of some transactions.");
                }

                String scheduleId = request.getParameter("scheduleId");
                if (!StringUtil.isNullOrEmpty(scheduleId)) {

                    AssetMaintenanceSchedulerObject schedulerObject = null;
                    KwlReturnObject scObj = accountingHandlerDAOobj.getObject(AssetMaintenanceSchedulerObject.class.getName(), scheduleId);
                    schedulerObject = (AssetMaintenanceSchedulerObject) scObj.getEntityList().get(0);
                    if (schedulerObject != null) {
                        Date currentDate = new Date();
                        if (!constarctStatus.equals("2") && (df.parse(df.format(schedulerObject.getStartDate())).equals(currentDate) || df.parse(df.format(schedulerObject.getStartDate())).before(currentDate))) {// if schedule start date is equal to current date or before current date it should not be edit. i.e if on schedule has been started then it cannot be commit
                            throw new AccountingException("Schedule has been started for contract so cannot be deleted.");
                        }

                        // Check any work order exist for this schedule or not, if exist then it cannot be edit
                        if (!schedulerObject.getAssetMaintenanceSchedulers().isEmpty()) {
                            Set<AssetMaintenanceScheduler> maintenanceSchedulers = schedulerObject.getAssetMaintenanceSchedulers();
                            for (AssetMaintenanceScheduler maintenanceScheduler : maintenanceSchedulers) {
                                HashMap<String, Object> woMap = new HashMap<String, Object>();

                                woMap.put("scheduleId", maintenanceScheduler.getId());

                                woMap.put("companyId", companyid);

                                KwlReturnObject woResult = accProductObj.getAssetMaintenanceWorkOrders(woMap);

                                if (woResult != null && !woResult.getEntityList().isEmpty()) {
                                    AssetMaintenanceWorkOrder workOrder = (AssetMaintenanceWorkOrder) woResult.getEntityList().get(0);
                                    throw new AccountingException("Schedule number has Work Order  Linked with it. so it cannot be deleted");
                                }
                            }
                        }
                    }

                    //delete data of this schedule
                    HashMap<String, Object> dataMap = new HashMap<String, Object>();

                    dataMap.put("id", scheduleId);

                    dataMap.put("companyId", companyid);

                    KwlReturnObject result1 = accProductObj.deleteAssetMaintenanceSchedule(dataMap);
                }
                if (replacementCount > 0 || invoiceAndDOCount > 0) {
                    throw new AccountingException("Sorry, you cannot delete the contract as it in use of some transactions.");
                } else {
                    // Delete entry from table contractdetailtermsmap for india country
                    if (lineLevelTermFlag == 1) {

                        List resultList = accSalesOrderDAOobj.selectContractDetails(contractid, companyid);
                        if (!resultList.isEmpty()) {
                            String contractDetailsID = "";
                            for (Object l : resultList) {
                                contractDetailsID = l.toString()!=null?l.toString():"";                            
                                accSalesOrderDAOobj.deleteContractDetailTermsMap(contractDetailsID, companyid);
                            }
                        }
                    }
                    //update order 
                    accSalesOrderDAOobj.updateLeaseOrder(contractid, companyid);
                    accSalesOrderDAOobj.deletecontractMaintenanceSchedule(contractid, companyid);// delete Maintainence schedule for contact
                    accSalesOrderDAOobj.deletecontractFiles(contractid);
                    accSalesOrderDAOobj.deleteContracts(contractid, companyid);
                                                             
                    auditTrailObj.insertAuditLog(Constants.Acc_Contract_Order_ModuleId+"", "User "+sessionHandlerImpl.getUserFullName(request) +" has deleted a Lease Contrcat  "+ contractno+" with status as "+(constarctStatus.equals("2")?" Terminated":" Active")+((!StringUtil.isNullOrEmpty(scheduleName))?" and Maintenance Schedule"+scheduleName+".":"."), request, contractid); 

                }

            }
        } catch (Exception ex) {
            throw new AccountingException(ex.getMessage());
        }
        return null;
    }

    private boolean deleteActivityInCRM(String companyid, String billid, String billno, HttpServletRequest request) {
        boolean isdeleted = false;
        boolean contractnotused=true;
        //Session session = null;
        try {

            String crmReqmsg = "";
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String action = "220";
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/checkcontract";            
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("action", action);
            userData.put("contractid", (billid != null) ? billid : "");
            userData.put("contractno", (billno != null) ? billno : "");

            //session = HibernateUtil.getCurrentSession();
//            JSONObject pjobj = new JSONObject();
//            pjobj.put("contractid", (billid != null) ? billid : "");
//            pjobj.put("contractno", (billno != null) ? billno : "");
//            pjobj.put("companyid", companyid);
//            userData.put("data", pjobj);

            JSONObject resObj = apiCallHandlerService.restGetMethod(crmURL, userData.toString());
//            JSONObject resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
            if (!resObj.isNull("issuccess") && resObj.getBoolean("issuccess")) {
                isdeleted = resObj.getBoolean("issuccess");
                contractnotused = resObj.getBoolean("contractnotused");
            }

        } catch (Exception ex) {
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, "accContractController.createActivityInCRM", ex);
        }
//        finally {
//            HibernateUtil.closeSession(session);
//        }
        return contractnotused;
    }
    private boolean terminateActivityInCRM(String companyid, String billid, HttpServletRequest request) {
        boolean isdeleted = false;
        boolean contractterminated = false;
        //Session session = null;
        try {

            String crmReqmsg = "";
//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String action = "221";
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/accountcontractactivity";
            
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("action", action);
            userData.put("contractid", (billid != null) ? billid : "");

            //session = HibernateUtil.getCurrentSession();

            JSONObject resObj = apiCallHandlerService.restDeleteMethod(crmURL, userData.toString());
//            JSONObject resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
            if (!resObj.isNull(Constants.RES_success) && resObj.getBoolean(Constants.RES_success)) {
                isdeleted = resObj.getBoolean(Constants.RES_success);
                contractterminated = resObj.getBoolean("contractterminated");
            }

        } catch (Exception ex) {
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, "accContractController.createActivityInCRM", ex);
        }
//        finally {
//            HibernateUtil.closeSession(session);
//        }
        return contractterminated;
    }
}
