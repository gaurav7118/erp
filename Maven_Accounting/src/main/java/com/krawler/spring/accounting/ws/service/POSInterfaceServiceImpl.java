/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.User;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.pos.*;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.vendorpayment.service.AccVendorPaymentModuleService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.text.ParseException;
import javax.ws.rs.PUT;
import org.springframework.transaction.annotation.Isolation;

/**
 *
 * @author krawler
 */
public class POSInterfaceServiceImpl implements POSInterfaceService {
    private WSUtilService wsUtilService;
    private MessageSource messageSource;
    private AccPOSInterfaceDAO accPOSInterfaceDAO;
    private TransactionService transactionService;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccPOSInterfaceService accPOSInterfaceService ;
    private AccVendorPaymentModuleService accVendorPaymentModuleServiceObj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accReceiptDAO accReceiptDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
//    private AccGroupCompanyDAO accGroupCompanyDAO;

//    public void setaccGroupCompanyDAO(AccGroupCompanyDAO accGroupCompanyDAO) {
//        this.accGroupCompanyDAO = accGroupCompanyDAO;
//    }
//     
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
        
    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    
    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    
    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void settransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
     
    public void setwsUtilService(WSUtilService wsUtilService) {
        this.wsUtilService = wsUtilService;
    }

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setaccPOSInterfaceDAO(AccPOSInterfaceDAO accPOSInterfaceDAO) {
        this.accPOSInterfaceDAO = accPOSInterfaceDAO;
    }
    public void setaccPOSInterfaceService(AccPOSInterfaceService accPOSInterfaceService) {
        this.accPOSInterfaceService = accPOSInterfaceService;
    }
    
    public void setAccVendorPaymentModuleServiceObj(AccVendorPaymentModuleService accVendorPaymentModuleServiceObj) {
        this.accVendorPaymentModuleServiceObj = accVendorPaymentModuleServiceObj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
    
    @Override
    public JSONObject saveCompanywiseCurrencyDenomination(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        String message = "";
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj != null) {
                if (!paramJobj.has("denominations") && paramJobj.getJSONArray("denominations").length()<1) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }

                JSONObject jobj = accPOSInterfaceDAO.saveCompanywiseCurrencyDenomination(paramJobj);
                if (jobj.has(Constants.RES_success) && jobj.optBoolean(Constants.RES_success)) {
                    isSuccess = jobj.optBoolean(Constants.RES_success);
                    message = messageSource.getMessage("acc.common.erp39", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                } else {
                    message = messageSource.getMessage("acc.common.msg1", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                }
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            response.put(Constants.RES_success, isSuccess);
            response.put(Constants.RES_MESSAGE, message);
        }
        return response;
    }
    
     @Override
    public JSONObject deleteCurrencyDenominations(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        String message = "";
        try {
            if (paramJobj != null) {
                if (!paramJobj.has(Constants.companyKey)) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
                JSONObject jobj = accPOSInterfaceDAO.deleteCurrencyDenominations(paramJobj);
                if (jobj.has(Constants.RES_success) && jobj.optBoolean(Constants.RES_success)) {
                    isSuccess = jobj.optBoolean(Constants.RES_success);
                    message = messageSource.getMessage("acc.common.erp42", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                } else {
                    message = messageSource.getMessage("acc.common.msg1", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            response.put(Constants.RES_success, isSuccess);
            response.put(Constants.RES_MESSAGE, message);
        }
        return response;
    }   
    
    
  @Override
    public JSONObject getCompanywiseCurrencyDenomination(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        JSONArray jArray = new JSONArray();
        try {
            if (paramJobj != null) {
                Map<String, Object> requestParams = new HashMap<String, Object>();
                List mapaccresult = accPOSInterfaceDAO.getCompanywiseCurrencyDenomination(paramJobj);
                Iterator<Object[]> itr1 = mapaccresult.iterator();
                while (itr1.hasNext()) {
                    JSONObject jobj = new JSONObject();
                    Object[] row = (Object[]) itr1.next();
                    if (row[0] != null) {
                        jobj.put(Constants.Acc_id, row[0].toString());
                    }
                    if (row[1] != null) {
                        jobj.put("cashdenominationjson", row[1].toString());
                    }
                    if (row[2] != null) {
                        jobj.put(Constants.currencyKey, row[2].toString());
                    }
                    jArray.put(jobj);
                }
            }

        } catch (JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            response.put(Constants.RES_success, isSuccess);
            response.put(Constants.data, jArray);
            response.put(Constants.RES_TOTALCOUNT, jArray.length());
        }
        return response;
    }
    
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getRegisterDetails(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject returnJson = new JSONObject();
        List<CompanyRegister> crList = null;
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
 
            Date transactionDate = null;//register openingdate
            DateFormat df = authHandler.getDateOnlyFormat();
            String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
            if (paramJobj.has("transactiondate") && paramJobj.get("transactiondate") != null) {
                String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("transactiondate"), originalDateFormat);
                transactionDate = df.parse(convertedDate);
                paramJobj.put("transactiondate", transactionDate);
            }
            if (paramJobj.has("startdate") && paramJobj.get("startdate") != null) {
                String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("startdate"), originalDateFormat);
                Date startdate = df.parse(convertedDate);
                paramJobj.put("startdate", startdate);
            }
            if (paramJobj.has("enddate") && paramJobj.get("enddate") != null) {
                String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("enddate"), originalDateFormat);
                Date enddate = df.parse(convertedDate);
                paramJobj.put("enddate", enddate);
            }
            KwlReturnObject returnObj = accPOSInterfaceDAO.getRegisterDetails(paramJobj);
            if (returnObj.getRecordTotalCount() != 0 && returnObj.getEntityList() != null && returnObj.getEntityList().get(0) != null) {
                crList = returnObj.getEntityList();
            }

            JSONArray registerArr = new JSONArray();
            if (crList != null) {
                for (CompanyRegister companyRegObj : crList) { //these are header of main module
                    JSONObject jObj = new JSONObject();
                    jObj.put("isopen", companyRegObj.getIsopen());
                    if (companyRegObj.getUserid() != null) {
                        jObj.put(Constants.useridKey, companyRegObj.getUserid().getUserID());
                        KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(User.class.getName(), companyRegObj.getUserid().getUserID());
                        User userObj = (User) curreslt.getEntityList().get(0);
                        if (userObj != null) {
                            jObj.put(Constants.userfullname, userObj.getFullName());
                        }
                    }
                    
                    jObj.put("id", companyRegObj.getID());
                    jObj.put("locationid", companyRegObj.getLocationid());
                    jObj.put("cashdenomination", !StringUtil.isNullOrEmpty(companyRegObj.getCurrencydenominationsjson()) ? companyRegObj.getCurrencydenominationsjson() : "");
                    jObj.put("isopen", companyRegObj.getIsopen());
                    jObj.put("transactiondate", companyRegObj.getTransactionDate());
                    jObj.put("transactiondateinlong", companyRegObj.getTransactionDateinLong());
                    jObj.put("previousclosedbalance", companyRegObj.getPreviousclosedbalance());
                    jObj.put("openingamount", companyRegObj.getOpeningamount());
                    jObj.put("finalopeningamount", companyRegObj.getFinalopeningamount());
                    jObj.put("addedamount", companyRegObj.getAddedamount());
                    jObj.put("variance", companyRegObj.getVariance());
                    jObj.put("closingamount", companyRegObj.getClosingamount());
//                    jObj.put("depositedamount", companyRegObj.getDepositedamount());
                    jObj.put("finalamount", companyRegObj.getFinalamount());
                    
                    //Before closing register
                    if (paramJobj.optBoolean(PaymentDetailPos.BEFORE_CLOSE_FLAG)) {
                        
                        //Fetching cash out information
                        paramJobj.put("isdeposit",false)  ;
                        KwlReturnObject cashReturnObj = accPOSInterfaceDAO.getPOSCashOutDetails(paramJobj);
                        
                        if (cashReturnObj.getEntityList() != null && cashReturnObj.getRecordTotalCount() > 0 && cashReturnObj.getEntityList().size() == 1 && cashReturnObj.getEntityList().get(0) != null) {
                            List<Object[]> detailList = cashReturnObj.getEntityList();
                            if (detailList.size() == 1) {
                                Object[] row = (Object[]) cashReturnObj.getEntityList().get(0);
                                if (row[0] != null) {
                                    double amount = (Double) row[0];
                                    jObj.put("cashoutamount", amount);
                                } else {
                                    jObj.put("cashoutamount", 0);
                                }
                            }
                        } else { //end of payReturnObj.getEntityList()
                            jObj.put("cashoutamount", 0);
                        }
                        
                        //Fetching deposited amount
                        paramJobj.put("isdeposit", true);
                        cashReturnObj = accPOSInterfaceDAO.getPOSCashOutDetails(paramJobj);

                        if (cashReturnObj.getEntityList() != null && cashReturnObj.getRecordTotalCount() > 0 && cashReturnObj.getEntityList().size() == 1 && cashReturnObj.getEntityList().get(0) != null) {
                            List<Object[]> detailList = cashReturnObj.getEntityList();
                            if (detailList.size() == 1) {
                                Object[] row = (Object[]) cashReturnObj.getEntityList().get(0);
                                if (row[0] != null) {
                                    double amount = (Double) row[0];
                                    jObj.put("depositedamount", amount);
                                } else {
                                    jObj.put("depositedamount", 0);
                                }
                            }
                        } else { //end of payReturnObj.getEntityList()
                            jObj.put("depositedamount", 0);
                        }
                        
                        paramJobj.put(PaymentDetailPos.IS_SUMMATION_FLAG, true);
                        //Cash
                        paramJobj.put(PaymentDetailPos.Payment_Method__Type, 0);
                        KwlReturnObject payReturnObj = accPOSInterfaceDAO.getPOSPaymentMethodDetails(paramJobj);

                        if (payReturnObj.getEntityList() != null && payReturnObj.getRecordTotalCount() > 0 && payReturnObj.getEntityList().size() == 1 && payReturnObj.getEntityList().get(0) != null) {
                            List<Object[]> detailList = payReturnObj.getEntityList();
                            for (Object[] row : detailList) {
                                if (row.length >= 1 && row[0] != null) {
                                        double amount = (Double) row[0];
                                        jObj.put("bycash", amount);
                                } else {
                                        jObj.put("bycash", 0);
                                    }
                            }
                        } else { //end of payReturnObj.getEntityList()
                            jObj.put("bycash", 0);
                        }

                        //Card 
                        paramJobj.put(PaymentDetailPos.Payment_Method__Type, 1);
                        payReturnObj = accPOSInterfaceDAO.getPOSPaymentMethodDetails(paramJobj);
                        if (payReturnObj.getEntityList() != null && payReturnObj.getRecordTotalCount() > 0 && payReturnObj.getEntityList().size() == 1 && payReturnObj.getEntityList().get(0) != null) {
                            List<Object[]> detailList = payReturnObj.getEntityList();
                            for (Object[] row : detailList) {
                                    if (row.length >= 1 && row[0] != null) {
                                        double amount = (Double) row[0];
                                        jObj.put("bycard", amount);
                                    } else {
                                        jObj.put("bycard", 0);
                                    }
                                }
                        } else {//end of payReturnObj.getEntityList()
                            jObj.put("bycard", 0);
                        }

                        //Cheque
                        paramJobj.put(PaymentDetailPos.Payment_Method__Type, 2);
                        payReturnObj = accPOSInterfaceDAO.getPOSPaymentMethodDetails(paramJobj);

                        if (payReturnObj.getEntityList() != null && payReturnObj.getRecordTotalCount() > 0 && payReturnObj.getEntityList().size() == 1 && payReturnObj.getEntityList().get(0) != null) {
                            List<Object[]> detailList = payReturnObj.getEntityList();
                            for (Object[] row : detailList) {
                                if (row.length >= 1 && row[0] != null) {
                                    double amount = (Double) row[0];
                                    jObj.put("bycheque", amount);
                                } else {
                                    jObj.put("bycheque", 0);
                                }
                            }
                        } else {//end of payReturnObj.getEntityList()
                            jObj.put("bycheque", 0);
                        }
                        //Gift card
                        paramJobj.put(PaymentDetailPos.Payment_Method__Type, 3);
                        payReturnObj = accPOSInterfaceDAO.getPOSPaymentMethodDetails(paramJobj);

                        if (payReturnObj.getEntityList() != null && payReturnObj.getRecordTotalCount() > 0 && payReturnObj.getEntityList().size() == 1 && payReturnObj.getEntityList().get(0) != null) {
                            List<Object[]> detailList = payReturnObj.getEntityList();
                            for (Object[] row : detailList) {
                                if (row.length >= 1 && row[0] != null) {
                                        double amount = (Double) row[0];
                                        jObj.put("bygiftcard", amount);
                                } else {
                                    jObj.put("bygiftcard", 0);
                                }
                            }
                        } else {//end of payReturnObj.getEntityList()
                            jObj.put("bygiftcard", 0);
                        }
                    } else {
                        jObj.put("cashoutamount", companyRegObj.getCashoutamount());
                        jObj.put("bycash", companyRegObj.getByCash());
                        jObj.put("bycheque", companyRegObj.getByCheque());
                        jObj.put("bycard", companyRegObj.getByCard());
                        jObj.put("bygiftcard", companyRegObj.getByGiftCard());
                    }

                    registerArr.put(jObj);
                }
            }
            
            
            //When opening register on next day need to provide previously closed balance.
            if (!paramJobj.optBoolean(PaymentDetailPos.BEFORE_CLOSE_FLAG) && paramJobj.has("transactiondateinlong") && paramJobj.get("transactiondateinlong")!=null) {
                KwlReturnObject pcReturnObj = accPOSInterfaceDAO.getPreviousClosedBalanceDetails(paramJobj);
                if (pcReturnObj.getEntityList() != null && pcReturnObj.getRecordTotalCount() > 0 && pcReturnObj.getEntityList().size() == 1 && pcReturnObj.getEntityList().get(0) != null) {
                    List<Object[]> detailList = pcReturnObj.getEntityList();
                    for (Object[] row : detailList) {
                        if (row.length >= 1 && row[0] != null) {
                            double amount = (Double) row[0];
                            returnJson.put("previousclosedbalance", amount);
                        } else { //end of previous closed balance
                            returnJson.put("previousclosedbalance", 0);
                        }
                    }
                } else { //end of payReturnObj.getEntityList()
                    returnJson.put("previousclosedbalance", 0);
                }
            }
            
            returnJson.put(Constants.data, registerArr);
            returnJson.put(Constants.RES_success, true);
            returnJson.put(Constants.RES_TOTALCOUNT, registerArr.length());

        } catch (Exception e) {
            Logger.getLogger(POSInterfaceServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnJson;
    }
    
    @Override
    public JSONObject saveOpenandCloseRegisterDetaisls(JSONObject paramJobj) throws ServiceException, JSONException,SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        String message = "";
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (paramJobj != null) {
                if (!paramJobj.has("isopen")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
                int isopen = 0;
                if (paramJobj.has("isopen") && paramJobj.get("isopen") != null) {
                    isopen = (Integer) paramJobj.get("isopen");
                }
                
                
                Date transactionDate=null;//register openingdate
                DateFormat df = authHandler.getDateOnlyFormat();
                String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
                if (paramJobj.has("transactiondate")&& paramJobj.get("transactiondate")!=null) {
                    String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("transactiondate"), originalDateFormat);
                    transactionDate = df.parse(convertedDate);
                    paramJobj.put("transactiondate", transactionDate);
                }
                
                JSONObject jobj = accPOSInterfaceDAO.openandCloseRegister(paramJobj);
                if (jobj.has(Constants.RES_success) && jobj.optBoolean(Constants.RES_success)) {
                    isSuccess = jobj.optBoolean(Constants.RES_success);
                    if (isopen == 0) {
                        message = messageSource.getMessage("acc.common.erp41", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                    } else {
                        message = messageSource.getMessage("acc.common.erp44", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                    }
                    
                } else {
                    if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.RES_MESSAGE, null))) {
                        message = jobj.optString(Constants.RES_MESSAGE);
                    } else {
                        message = messageSource.getMessage("acc.common.erp44", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                    }
                    
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            response.put(Constants.RES_success, isSuccess);
            response.put(Constants.RES_MESSAGE, message);
        }
        return response;
    }
   
    //Saving Invoice with autogenerated DO
   public JSONObject saveInvoiceWithAutoDO(JSONObject paramJobj,String invoiceSeqFormat,String doSeqFormat) throws ServiceException, JSONException, SessionExpiredException,ParseException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;

        boolean isInvoice = false;
        String invid = null;
        String msg = null;

        try {
            if (paramJobj != null) {
                JSONObject tempReqJson=new JSONObject(paramJobj.toString());
                //not saving paydetails in Invoice as it is credit sales. Paydetail is only required for Receive Payment
                 tempReqJson.remove("paydetails");
                 //it calculates injsoncreate of save invoice in transactionserveiceimpl
                 tempReqJson.put("invoiceid",paramJobj.optString(Constants.billid));
                //If payment amount entered is greater than invoice amount then proceed with transaction
                tempReqJson.put(Constants.sequenceformat,invoiceSeqFormat);
                tempReqJson.put(Constants.sequenceformatDo,doSeqFormat);
                tempReqJson.put(Constants.isForPos,true);
                tempReqJson.put(Constants.moduleid,String.valueOf(Constants.Acc_Invoice_ModuleId));
                tempReqJson.remove(Constants.isdefaultHeaderMap);
                tempReqJson.remove("paydetail");
                JSONObject invoiceResponseJson = transactionService.saveInvoice(tempReqJson);
                
                if (invoiceResponseJson.has("invoiceNo") && !StringUtil.isNullOrEmpty(invoiceResponseJson.optString("invoiceNo", null)) && invoiceResponseJson.has(Constants.RES_success) && invoiceResponseJson.optBoolean(Constants.RES_success)) {
                    System.out.println("->Invoice saved successfully.");
                    response.put("invoicenumber", invoiceResponseJson.optString("invoiceNo"));
                    response.put("invoiceNo", invoiceResponseJson.optString("invoiceNo"));
                    if (invoiceResponseJson.has("invid") && !StringUtil.isNullOrEmpty(invoiceResponseJson.optString("invid", null))) {
                        invid = invoiceResponseJson.optString("invid", null);
                        response.put("invoiceid", invid);
                        response.put("invid", invid);
                    }
                    isInvoice = true;
                    isSuccess = true;
                    if (invoiceResponseJson.has("requestParamsJson") && invoiceResponseJson.getJSONObject("requestParamsJson") != null) {
                        tempReqJson = invoiceResponseJson.getJSONObject("requestParamsJson");
                        response.put("requestParamsJson", tempReqJson);
                    }
                } else {
                    isSuccess = false;
                }
            }
        } catch (JSONException ex) {
            isInvoice=false;
            msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            System.out.println("Exception-> " + ex.getMessage().toString());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            response.put(Constants.RES_success, isSuccess);
            response.put("isInvoice", isInvoice);
            response.put(Constants.RES_MESSAGE, msg);
        }
        return response;
    }  
    
    public JSONObject saveReceivePayment(JSONObject paramJobj, String receiveSeqFormat, JSONObject invoiceResponseJson, boolean incash) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        String invid = null;
        String receiptid = null;
        String receiptNumber = null;
        String msg = null;
        JSONArray pDetailsArray=new JSONArray();
        boolean isEdit = paramJobj.optBoolean(Constants.isEdit);
        String companyid = paramJobj.optString(Constants.companyKey);
        String userid = paramJobj.optString(Constants.useridKey);
        String locationid = paramJobj.optString("storeid");
        //Fetching temporaryjson from invoice response
        JSONObject tempReqJson = new JSONObject(paramJobj.toString());
        try {
            if (invoiceResponseJson.has("requestParamsJson") && invoiceResponseJson.get("requestParamsJson") != null) {
                tempReqJson = invoiceResponseJson.optJSONObject("requestParamsJson");
            }
            if (invoiceResponseJson.has("invid") && invoiceResponseJson.get("invid") != null) {
                invid = invoiceResponseJson.optString("invid");
            }
            
            //Payment details in array
            String paydetails = paramJobj.optString("paydetail");
            if (!StringUtil.isNullOrEmpty(paydetails)) {
                pDetailsArray = new JSONArray(paydetails);
            }
            tempReqJson.put(Constants.isForPos, true);

            
            
            /*---------------------------Edit case--------------------------*/
            if (isEdit && !StringUtil.isNullOrEmpty(invid)) {
                KwlReturnObject rpresult = accInvoiceDAOobj.getPaymentReceiptsLinkedWithInvoice(invid, companyid);
                List<String> listr = rpresult.getEntityList();
                int receiptCount = listr.size();
                StringBuilder rids = new StringBuilder();
                //If previously generated was only one receipt payment record then we take the same sequenceformat and edit the existing record.
                //Here we are checking for length ==1 because if more than 1 records are created then to tracking of receive payment becomes difficult.
                if (pDetailsArray.length() == receiptCount && pDetailsArray.length() == 1) {
                    receiptid = (String) listr.get(0);
                    rids.append(receiptid + ",");
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                    Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                    if (receipt != null) {
                        receiveSeqFormat = receipt.getSeqformat().getID();
                        tempReqJson.put(Constants.billid,receipt.getID());
                        tempReqJson.put("no",receipt.getReceiptNumber());
                    }

                } else {//else we delete the existing record and insert a new record for receive payment with new payment records
                    tempReqJson.remove(Constants.isEdit);
                    
                    for (String orderid : listr) {
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), orderid);
                        Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                        if (receipt != null) {
                            rids.append(receipt.getID() + ",");
                            paramJobj.put(Constants.deletepermanentflag, true);
                            paramJobj.put("receiptno", receipt.getReceiptNumber());
                            JSONObject deletejson = transactionService.deleteReceivePayment(paramJobj);
                        }
                    }
                }
                //in edit case we delete payment entry from pos that we use to display the payment amounts in cash,card & checques on the basis of storid
                if (rids.length() > 0) {
                    String rObjids = rids.toString();
                    rObjids = rObjids.substring(0, rObjids.length() - 1);
                    JSONObject deletejson = new JSONObject();
                    deletejson.put(Constants.companyKey, companyid);
                    deletejson.put(Constants.useridKey, userid);
                    deletejson.put("invoiceid", invid);
                    deletejson.put("receiptid", rObjids);
                    deletejson.put(POSERPMapping.StoreId, locationid);
                    JSONObject deleteObj = accPOSInterfaceDAO.deletePaymentMethodEntry(deletejson);
                }
                
                //in case of case 2 where receiptpayment are deleted then receiptid is blank and we need a receiptid to make new transaction
                if (StringUtil.isNullOrEmpty(receiptid)) {
                    //Fetching receiptid from pos_erp mapping to make new entry according to new sequenceformat.
                    KwlReturnObject returnObj = accPOSInterfaceDAO.getPOSConfigDetails(paramJobj);
                    if (returnObj.getEntityList() != null && returnObj.getRecordTotalCount() > 0 && returnObj.getEntityList().size() == 1 && returnObj.getEntityList().get(0) != null) {
                        List<Object[]> detailList = returnObj.getEntityList();

                        if (detailList.size() == 1) {
                            Object[] row = (Object[]) returnObj.getEntityList().get(0);
                            if (row[11] != null) {
                                receiveSeqFormat = row[11].toString();
                            } else {
                                throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.receivePaymentSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                            }
                        }
                    }//end of if (returnObj.getEntityList() != null && returnObj.getRecordTotalCount() > 0 && returnObj.getEntityList().size() == 1 && returnObj.getEntityList().get(0) != null) 
                }
            }
            
            
            /*---------------------------Create Case---------------------------------*/
            
            StringBuilder receiptNoString = new StringBuilder();
            StringBuilder receiptIdString = new StringBuilder();
            
            double invoiceReceiveAmount = 0.0;
            //Multiple payment method handled
            if (pDetailsArray.length()>0) {
                for (int j = 0; j < pDetailsArray.length(); j++) {
                    JSONObject paymentJson = pDetailsArray.getJSONObject(j);

                    if (!incash) {//when cash sales is created then receive payment is not made
                        if (paymentJson.has("pmtmethod") && !StringUtil.isNullOrEmpty(paymentJson.optString("pmtmethod", null))) {
                            tempReqJson.put("pmtmethod", paymentJson.optString("pmtmethod"));
                            tempReqJson.put("paymentmethodid", paymentJson.optString("pmtmethod"));

                            //For Cash Payment
                            if (paymentJson.has("paymentmethodtype") && paymentJson.optInt("paymentmethodtype") == 0) {
                                tempReqJson.put("paydetail", "");
                            } else {
                                tempReqJson.put("paydetail", paymentJson);
                            }
                        } else {
                            tempReqJson.put("pmtmethodvalue", "Cash");
                            tempReqJson.put("paydetail", "");
                            tempReqJson = wsUtilService.manipulateGlobalLevelFieldsNew(tempReqJson, paramJobj.optString(Constants.companyKey));

                        }
                        if (paymentJson.has("amount") && !StringUtil.isNullOrEmpty(paymentJson.optString("amount", null))) {
                            invoiceReceiveAmount = paymentJson.optDouble("amount", 0.0);
                            //updating the enteramount with payment method as multiple methods will be made.
                            invoiceResponseJson.put("amount", invoiceReceiveAmount);
                        }

                        /*
                         * Receive Payment Section.--creatingdetails
                         */
                        JSONObject rpBuildJson = transactionService.jsonCreateReceivePayment(tempReqJson, invoiceResponseJson);
                        //SEQUENCE format is replaced according to mapped configuration from erp-pos mapping
                        rpBuildJson.put(Constants.sequenceformat, receiveSeqFormat);
                        if (tempReqJson.has(Constants.useridKey) && !StringUtil.isNullOrEmpty(tempReqJson.optString(Constants.useridKey, null))) {
                            rpBuildJson.put(Constants.lid, tempReqJson.optString(Constants.useridKey, null));
                        }
                        if (tempReqJson.has(Constants.currencyName) && !StringUtil.isNullOrEmpty(tempReqJson.optString(Constants.currencyName, null))) {
                            rpBuildJson.put(Constants.currencyKey, tempReqJson.optString(Constants.currencyName, null));
                        }
                        rpBuildJson.put("billdate", paramJobj.optString("billdate"));
                        rpBuildJson.put(Constants.customfield, paramJobj.optString(Constants.customfield));
                        JSONObject rpResponseJson = transactionService.saveReceiptPayment(rpBuildJson);

                        if (rpResponseJson.has("billno") && !StringUtil.isNullOrEmpty(rpResponseJson.optString("billno", null)) && rpResponseJson.has(Constants.RES_success) && rpResponseJson.optBoolean(Constants.RES_success)) {
                            System.out.println("->Receipt saved successfully.");
                            if (rpResponseJson.has("requestParamsJson") && rpResponseJson.getJSONObject("requestParamsJson") != null) {
                                tempReqJson = rpResponseJson.getJSONObject("requestParamsJson");
                                tempReqJson.remove(Constants.detail);
                                tempReqJson.remove("Details");
                            }
                            receiptNumber = rpResponseJson.optString("billno");
                            if (receiptNoString.length() > 0) {
                                receiptNoString.append(",");
                            }
                            receiptNoString.append(receiptNumber);
                            receiptid = rpResponseJson.optString("paymentid");
                            if (receiptIdString.length() > 0) {
                                receiptIdString.append(",");
                            }
                            receiptIdString.append(receiptid);

                            
                            //Saving in pos detail type to track the payment made by cash,card and checque while closing register
                            if (!StringUtil.isNullOrEmpty(receiptid)) {
                                HashMap<String, Object> reqParams = new HashMap<>();
                                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                                Receipt receipt = (Receipt) receiptObj.getEntityList().get(0);
                                long transactiondate = tempReqJson.optLong("transactiondateinlong");
                                if (receipt != null) {
                                    reqParams.put("amount", receipt.getDepositAmount());
                                }
                                int paymentmethodtype = paymentJson.optInt(PaymentDetailPos.Payment_Method__Type, 0);
                                reqParams.put(Constants.companyKey, companyid);
                                reqParams.put(Constants.useridKey, userid);
                                reqParams.put("location", locationid);
                                reqParams.put("transactiondateinlong", transactiondate);
                                String creationdate = null;
                                if (paramJobj.has("billdate") && !StringUtil.isNullOrEmpty(paramJobj.optString("billdate"))) {
                                    creationdate = paramJobj.optString("billdate") == null ? null : paramJobj.optString("billdate");
                                }
                                Date transdate = null;
                                if (!StringUtil.isNullOrEmpty(creationdate)) {
                                    transdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).parse(creationdate);
                                } else {
                                    transdate = new Date();
                                }
                                reqParams.put(PaymentDetailPos.Payment_Method__Type, paymentmethodtype);
                                reqParams.put(PaymentDetailPos.Payment_Method__Name, paymentJson.optString(PaymentDetailPos.Payment_Method__Name));
                                reqParams.put(PaymentDetailPos.RECEIPT_ID, receiptid);
                                reqParams.put(PaymentDetailPos.INVOICE_ID, invid);
                                reqParams.put("transactiondate", transdate);
                                JSONObject saveObj = accPOSInterfaceDAO.savePaymentMethodType(reqParams);
                            }
                            isSuccess = true;
                        } else {
                            isSuccess = false;
                            JSONObject deletejson = transactionService.jsonDeleteInvoice(invid, rpBuildJson);
                            deletejson = transactionService.deleteInvoice(deletejson);
                        }
                    }

                    if (!isSuccess) {
                        msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                    } else {
                        msg = messageSource.getMessage("acc.RestSerivce.successMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                    }
                }//end of for (int j = 0; j < pDetailsArray.length(); j++)

                response.put("receiptnumber", receiptNoString.toString());
                response.put("paymentid", receiptIdString.toString());

            }//end of if (!StringUtil.isNullOrEmpty(paydetails)) 
        } catch (JSONException ex) {
            JSONObject deletejson = transactionService.jsonDeleteInvoice(invid, paramJobj);
            deletejson = transactionService.deleteInvoice(deletejson);
            msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            System.out.println("Exception-> " + ex.getMessage().toString());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            response.put(Constants.RES_success, isSuccess);
            response.put(Constants.RES_MESSAGE, msg);
        }
        return response;
    }
    
    
    @Override
    public JSONObject saveInvoiceDOandRP(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException,ParseException,AccountingException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        boolean incash = false;
        boolean isInvoice = false;
        String invid = null;
        String msg = null;

        try {
            if (paramJobj != null) {
                paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
                paramJobj.put(Constants.isdefaultHeaderMap, true);
                paramJobj.put(Constants.fromLinkComboAutoDO, Constants.CUSTOMER_INVOICE);// to save autogeneratedo
                String companyid = paramJobj.optString(Constants.companyKey);
                boolean isEdit = paramJobj.optBoolean(Constants.isEdit);
                invid = paramJobj.optString(Constants.billid);

                //If payment amount entered is greater than invoice amount then proceed with transaction
                String pmDetails = paramJobj.optString("paydetail");
                double subtotal = Double.parseDouble(paramJobj.optString("subTotal"));
                double taxamount = Double.parseDouble(paramJobj.optString("taxamount"));
                double totalamountCal = subtotal + taxamount;
                //Multiple payment method handled
                if (!StringUtil.isNullOrEmpty(pmDetails)) {
                    JSONArray pDetailsArray = new JSONArray(pmDetails);
                    for (int j = 0; j < pDetailsArray.length(); j++) {
                        JSONObject paymentJson = pDetailsArray.getJSONObject(j);
                        double paymentamount = paymentJson.optDouble("amount");
                        if (totalamountCal < paymentamount) {
                            throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.amountGreaterThanInvoices", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                        } else {
                            totalamountCal = totalamountCal - paymentamount;
                        }
                    }
                }

                String invoiceSeqFormat = null;
                String receiveSeqFormat = null;
                String doSeqFormat = null;
                String walkinCustomerId = null;
                
                /* In edit case finding the delivery order sequenceformat id and invoice id*/
                if (isEdit && !StringUtil.isNullOrEmpty(invid)) {
                    KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                    Invoice invObj = (Invoice) customerResult.getEntityList().get(0);
                    if (invObj != null) {
                        if (invObj.getSeqformat() != null) {
                            invoiceSeqFormat = invObj.getSeqformat().getID();
                        }
                        // if allowing to edit after the popup message, then this flag is passed. 
                        paramJobj.put(Constants.IS_INVOICE_ALLOW_TO_EDIT, "true");
                        //customer value
                        paramJobj.put(Constants.customerName, invObj.getCustomer().getID());
                        paramJobj.put("CustomerName", invObj.getCustomer().getID());
                        KwlReturnObject cusResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), invObj.getCustomer().getID());
                        Customer cObj = (Customer) cusResult.getEntityList().get(0);
                        if (cObj != null) {
                            paramJobj.put("acccode", cObj.getAcccode());
                        }
                        
                        //Fetching the delivery order linked to Invoices
                        KwlReturnObject doresult = accInvoiceDAOobj.getDOFromOrToInvoices(invObj.getID(),companyid);//destination companyid

                        List<Object[]> listdo = doresult.getEntityList();
                        for (Object[] oj : listdo) {
                            DeliveryOrder deliveryOrder = (DeliveryOrder) oj[0];
                            if (deliveryOrder != null) {
                                doSeqFormat = deliveryOrder.getSeqformat().getID();
                                paramJobj.put("doid", deliveryOrder.getID());
                                paramJobj.put("deliveryOrderNo", deliveryOrder.getDeliveryOrderNumber());
                            }
                        }
                    }
                }
                
                // Do not execute for edit mode as we are fetching sequenceformat and customerid from invoice
                if (!isEdit) {
                    //fetch invoice,do, receive payment and walk-in customer from erp_pos mapping
                    KwlReturnObject returnObj = accPOSInterfaceDAO.getPOSConfigDetails(paramJobj);
                    if (returnObj.getEntityList() != null && returnObj.getRecordTotalCount() > 0 && returnObj.getEntityList().size() == 1 && returnObj.getEntityList().get(0) != null) {
                        List<Object[]> detailList = returnObj.getEntityList();

                        if (detailList.size() == 1) {
                            Object[] row = (Object[]) returnObj.getEntityList().get(0);
                            if (row[7] != null) {
                                invoiceSeqFormat = row[7].toString();
                            } else {
                                throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.invoiceSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                            }

                            if (row[6] != null) {
                                doSeqFormat = row[6].toString();
                            } else {
                                throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.doSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                            }

                            if (row[11] != null) {
                                receiveSeqFormat = row[11].toString();
                            } else {
                                throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.receivePaymentSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                            }

                            if (row[1] != null) {
                                walkinCustomerId = row[1].toString();
                            }
                            //Walkin Customer
                            if (!paramJobj.has(Constants.customerName) && !StringUtil.isNullOrEmpty(walkinCustomerId)) {
                                paramJobj.put(Constants.customerName, walkinCustomerId);
                                paramJobj.put("CustomerName", walkinCustomerId);
                                KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), walkinCustomerId);
                                Customer cObj = (Customer) customerResult.getEntityList().get(0);
                                if (cObj != null) {
                                    paramJobj.put("acccode", cObj.getAcccode());
                                } else {
                                    throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.defaultCustomerPOS", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                                }
                            }
                        }
                    } else {//end of resdata
                        throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.settingForStoreMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                    }
                }
                
                /*--------------------Save Invoice with autogenerated DO-------------------------------*/
                JSONObject invResponse=saveInvoiceWithAutoDO(paramJobj,invoiceSeqFormat,doSeqFormat);
                isSuccess=invResponse.optBoolean(Constants.RES_success,false);
                incash=invResponse.optBoolean("inCash");
                invid=invResponse.optString("invoiceid");
                isInvoice=invResponse.optBoolean("isInvoice");
                String invoicenumber=invResponse.optString("invoicenumber");
                
                
                /*---------------------Save Receive Payment with MultiPayment Method----------------*/
                if (isSuccess && !StringUtil.isNullOrEmpty(invid)) {
                    response.put("invoiceid", invid);
                    response.put("invoicenumber", invoicenumber);
                    JSONObject rpResponseJson = saveReceivePayment(paramJobj, receiveSeqFormat, invResponse, incash);
                    isSuccess = rpResponseJson.optBoolean(Constants.RES_success, false);
                    String receiptno = rpResponseJson.optString("receiptnumber");
                    String receiptid = rpResponseJson.optString("paymentid");
                    if (isSuccess && !StringUtil.isNullOrEmpty(receiptno)) {
                        response.put("receiptnumber", receiptno);
                        response.put("paymentid", receiptid);
                    } else {
                        throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.receiptnotSavedMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                    }
                } else {
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.invoicenotSavedMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }

                if (!isSuccess) {
                    msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                } else {
                    msg = messageSource.getMessage("acc.RestSerivce.successMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                }
            }
        } catch (JSONException ex) {
            if (isInvoice) {
                JSONObject deletejson = transactionService.jsonDeleteInvoice(invid, paramJobj);
                deletejson = transactionService.deleteInvoice(deletejson);
            }
//            if (isReceipt) {
//                JSONObject deletejson = transactionService.jsonDeleteReceivePayment(receiptid, paramJobj, receiptNumber);
//                deletejson = transactionService.deleteReceivePayment(deletejson);
//            }
            msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            System.out.println("Exception-> " + ex.getMessage().toString());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            response.put(Constants.RES_success, isSuccess);
            response.put(Constants.RES_MESSAGE, msg);
        }
        return response;
    }
    
    private JSONObject jsonCreateMakePaymentForCashOutAccount(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        JSONObject receivePaymentJson = paramJobj;

        boolean isForPOS = paramJobj.optBoolean(Constants.isForPos, false);
        receivePaymentJson.put("amount", paramJobj.optString("totalamount"));
        receivePaymentJson.put(Constants.isForPos,true);
        paramJobj.put(Constants.isdefaultHeaderMap, true);
        
        KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), paramJobj.optString(Constants.companyKey));
        Company companyObj = (Company) companyresult.getEntityList().get(0);
        if (companyObj != null) {
            paramJobj.put(Constants.globalCurrencyKey, companyObj.getCurrency().getCurrencyID());
            paramJobj.put(Constants.currencyKey, companyObj.getCurrency().getCurrencyID());
        }
        receivePaymentJson.put("memo", "Cash out transaction");
        receivePaymentJson.put("externalcurrencyrate", "1");
        receivePaymentJson.put("iscustomer", false);

        JSONObject returnJObj = accPOSInterfaceService.getERPPOSMappingDetails(paramJobj);
        if (returnJObj.has(Constants.RES_data) && returnJObj.get(Constants.RES_data) != null) {
            JSONArray storeArray = returnJObj.getJSONArray(Constants.RES_data);
            for (int i = 0; i < storeArray.length(); i++) {
                JSONObject sObj = storeArray.getJSONObject(i);
                if (sObj.has(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT) && sObj.get(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT) != null) {
                    receivePaymentJson.put(Constants.sequenceformat, (String) sObj.get(POSERPMapping.MAKEPAYMENT_SEQUENCEFORMAT));
                } else {
                    throw ServiceException.FAILURE("Please set the sequenceformat for Make Payment", "", false);

                }
                String paymentid = sObj.optString(POSERPMapping.PAYMENT_METHOD_ID);
                String[] paymentidArray = paymentid.split(",");
                if (paymentidArray.length > 0) {
                    for (String paymentMthd : paymentidArray) {
                        KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paymentMthd);
                        PaymentMethod pmObj = (PaymentMethod) curreslt.getEntityList().get(0);
                        if (pmObj != null) {
                            if (pmObj.getDetailType() == 0) {
                                receivePaymentJson.put("pmtmethod", paymentMthd);
                                receivePaymentJson.put("paymentmethodid", paymentMthd);
                            }
                        }
                    }
                }
                
                receivePaymentJson.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                receivePaymentJson = wsUtilService.manipulateGlobalLevelFieldsNew(receivePaymentJson, receivePaymentJson.optString(Constants.companyKey));

                JSONArray profield = new JSONArray();
                JSONObject product = new JSONObject();
                if (sObj.has(POSERPMapping.CASHOUT_ACCOUNT_Name) && sObj.get(POSERPMapping.CASHOUT_ACCOUNT_Name) != null) {
                    product.put("documentno", sObj.optString(POSERPMapping.CASHOUT_ACCOUNT_Name));
                    product.put("documentid", sObj.optString(POSERPMapping.CASHOUT_ACCOUNT_ID));
                } else {
                    throw ServiceException.FAILURE("Please set the cashout account for the store", "", false);

                }
                product.put("type", Constants.GLPayment);
                product.put("debit", "true");

                product.put("enteramount", paramJobj.optString("totalamount"));
                product.put("amount", paramJobj.optString("totalamount"));
                product.put("invoicecreationdate", paramJobj.optString("creationdate"));
                product.put("exchangeratefortransaction", paramJobj.optString("externalcurrencyrate"));
                profield.put(product);
                receivePaymentJson.put("Details", profield);
                receivePaymentJson.put(Constants.detail, profield);
            }//end of for loop
        }//end of resdata
        return receivePaymentJson;
    }
    
   @Override
    public JSONObject saveCashOutDetails(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        String message = "";
        StringBuilder msgBuildString = new StringBuilder();
        try {
            if (paramJobj != null) {
                paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
                String companyid = paramJobj.optString(Constants.companyKey);

                if (!paramJobj.has(Constants.useridKey) || !paramJobj.has("reasondetails") || !paramJobj.has(Constants.storeid)) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }

                String creationdate = null;
                if (paramJobj.has("transactiondate") && !StringUtil.isNullOrEmpty(paramJobj.optString("transactiondate"))) {
                    creationdate = paramJobj.optString("transactiondate") == null ? null : paramJobj.optString("transactiondate");
                    paramJobj.put("creationdate", creationdate);
                }
                Date transdate=null;

                if (!StringUtil.isNullOrEmpty(creationdate)) {
                    transdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).parse(creationdate);
                    DateFormat df = authHandler.getDateOnlyFormat();
                    creationdate = df.format(transdate);
                    paramJobj.put("creationdate", creationdate);
//            paramJobj.remove("transactiondate");
                }
                
                paramJobj = jsonCreateMakePaymentForCashOutAccount(paramJobj);

                HashMap<String, Object> hashMap = accVendorPaymentModuleServiceObj.saveVendorPayment(paramJobj);
                response = (JSONObject) hashMap.get("jobj");
                if (response.has(Constants.RES_msg)) {
                    response.put(Constants.RES_MESSAGE, response.getString(Constants.RES_msg));
                    response.remove(Constants.RES_msg);
                }
                if (response.has(Constants.RES_success)) {
                    response.put(Constants.RES_success, (Boolean) response.get(Constants.RES_success));
                } else {
                    response.put(Constants.RES_success, false);
                }

                if (response.optBoolean(Constants.RES_success)) {
                    String userid = paramJobj.optString(Constants.useridKey);
                    String locationid = paramJobj.optString(Constants.storeid);
                    long transactiondateinlong = paramJobj.optLong("transactiondateinlong");
                    
                    String details = paramJobj.optString("reasondetails", "[{}]");
                    JSONArray cashOutDetailsArray = new JSONArray(details);
                    if (cashOutDetailsArray.length() > 0) {
                        for (int i = 0; i < cashOutDetailsArray.length(); i++) {
                            HashMap<String, Object> reqParams = new HashMap<>();
                            JSONObject jObj = cashOutDetailsArray.getJSONObject(i);
                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), jObj.optString("reason"));
                            MasterItem msObj = (MasterItem) curreslt.getEntityList().get(0);
                            if (msObj != null) {
                                reqParams.put(Constants.companyKey, companyid);
                                reqParams.put("reason", jObj.optString("reason"));
                                reqParams.put(Constants.useridKey, userid);
                                reqParams.put("location", locationid);
                                reqParams.put("transactiondateinlong", transactiondateinlong);
                                reqParams.put("transactiondate", transdate);
                                reqParams.put("amount", jObj.optDouble("amount", 0.0));
                                if (jObj.has("id") && !StringUtil.isNullOrEmpty(jObj.optString("id", null))) {
                                    reqParams.put("id", jObj.optString("id"));
                                }
                                JSONObject saveObj = accPOSInterfaceDAO.saveCashOutDetails(reqParams);
                                if (saveObj.optBoolean(Constants.RES_success)) {
                                    if (msgBuildString.length() > 0) {
                                        msgBuildString.append(",");
                                    }
                                    msgBuildString.append(msObj.getValue());
                                }
                            } else {
                                throw ServiceException.FAILURE("Missing reason details", "", false);
                            }
                        }
                    }
                    if (msgBuildString.length() > 0) {
                        isSuccess = true;
                    }
                    message = "Cash out transaction has been saved successfully.";

                }//end of optBoolean of response
            }//end of paramJobj
        } catch (JSONException ex) {
            message=ex.getMessage();
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            response.put(Constants.RES_success, isSuccess);
        }
        return response;
    }
    
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getClosedBalanceDetails(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject returnJson = new JSONObject();
        List<CompanyRegister> crList = null;
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);

            Date transactionDate = null;//register openingdate
            DateFormat df = authHandler.getDateOnlyFormat();
            String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
            if (paramJobj.has("transactiondate") && paramJobj.get("transactiondate") != null) {
                String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("transactiondate"), originalDateFormat);
                transactionDate = df.parse(convertedDate);
                paramJobj.put("transactiondate", transactionDate);
            }
            KwlReturnObject returnObj = accPOSInterfaceDAO.getRegisterDetails(paramJobj);
            if (returnObj.getRecordTotalCount() != 0 && returnObj.getEntityList() != null && returnObj.getEntityList().get(0) != null) {
                crList = returnObj.getEntityList();
            }

            JSONArray registerArr = new JSONArray();
            if (crList != null) {
                for (CompanyRegister companyRegObj : crList) { //these are header of main module
                    JSONObject jObj = new JSONObject();
                    jObj.put("isopen", companyRegObj.getIsopen());
                    jObj.put("id", companyRegObj.getID());
                    jObj.put("locationid", companyRegObj.getLocationid());
                    jObj.put("cashdenomination", !StringUtil.isNullOrEmpty(companyRegObj.getCurrencydenominationsjson()) ? companyRegObj.getCurrencydenominationsjson() : "");
                    jObj.put("isopen", companyRegObj.getIsopen());
                    jObj.put("transactionDate", companyRegObj.getTransactionDate());
                    jObj.put("previousclosedbalance", companyRegObj.getPreviousclosedbalance());
                    jObj.put("openingamount", companyRegObj.getOpeningamount());
                    jObj.put("finalopeningamount", companyRegObj.getFinalopeningamount());
                    jObj.put("addedamount", companyRegObj.getAddedamount());
                    jObj.put("variance", companyRegObj.getVariance());
                    jObj.put("closingamount", companyRegObj.getClosingamount());
                    jObj.put("depositedamount", companyRegObj.getDepositedamount());
                    jObj.put("finalamount", companyRegObj.getFinalamount());
                    jObj.put("cashoutamount", companyRegObj.getCashoutamount());
                    jObj.put("bycash", companyRegObj.getByCash());
                    jObj.put("byCheque", companyRegObj.getByCheque());
                    jObj.put("byCard", companyRegObj.getByCard());
                    jObj.put("byGiftCard", companyRegObj.getByGiftCard());
                    registerArr.put(jObj);
                }
            }
            returnJson.put(Constants.data, registerArr);
            returnJson.put(Constants.RES_success, true);
            returnJson.put(Constants.RES_TOTALCOUNT, registerArr.length());

        } catch (Exception e) {
            Logger.getLogger(POSInterfaceServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnJson;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getERPPOSMappingDetails(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject returnJson = new JSONObject();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            returnJson = accPOSInterfaceService.getERPPOSMappingDetails(paramJobj);
        } catch (Exception e) {
            Logger.getLogger(POSInterfaceServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnJson;
    }  
    
    @Override
    public JSONObject saveSalesReturnwithCN(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException, UnsupportedEncodingException,AccountingException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        String msg = null;

        try {
            if (paramJobj != null) {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            paramJobj.put(Constants.isdefaultHeaderMap, true);
            paramJobj.put(Constants.isForPos, true);
            paramJobj.put(Constants.moduleid, Constants.Acc_Sales_Return_ModuleId);
            double subtotal=Double.parseDouble(paramJobj.optString("subTotal"));
            double taxamount=Double.parseDouble(paramJobj.optString("taxamount"));
            double totalamount=subtotal+taxamount;
            
                String linkNUmbers = null;
                StringBuilder amountsBuilderString=new StringBuilder();
                JSONArray invoiceDetailsArray=new JSONArray();
                
                //Link information details putting invoicedetails
                if (paramJobj.has("invoiceids") && !StringUtil.isNullOrEmpty(paramJobj.optString("invoiceids", null))) {
                    paramJobj.put("fromLinkCombo", Constants.CUSTOMER_INVOICE);
                    paramJobj.put("linkNumber", paramJobj.optString("invoiceids"));
                    linkNUmbers = paramJobj.optString("invoiceids");

                    // to calculate linked detailsid and fetching in invoice details
                    String[] linkNumbers = linkNUmbers.split(",");
                    for (int io = 0; io < linkNumbers.length; io++) {
                        String linkid = linkNumbers[io];
                        KwlReturnObject invResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), linkid);
                        Invoice invObj = (Invoice) invResult.getEntityList().get(0);
                        if (invObj != null) {
                            if (totalamount < invObj.getInvoiceamountdue()) {
                                amountsBuilderString.append(totalamount + ",");
                            } else {
                                amountsBuilderString.append(invObj.getInvoiceamountdue() + ",");
                            }
                            
                            JSONObject invJsonObj = new JSONObject();
                            invJsonObj.put(Constants.billid, invObj.getID());
                            invJsonObj.put(Constants.billno, invObj.getInvoiceNumber());
                            invJsonObj.put("amountDueOriginal", invObj.getOriginalOpeningBalanceAmount());
                            invJsonObj.put("exchangeratefortransaction", "1");
                            invoiceDetailsArray.put(invJsonObj);
                        }
                    }
                    if (amountsBuilderString.length() > 0) {
                        paramJobj.put("amounts", amountsBuilderString.toString());
                        paramJobj.put("invoiceDueAmounts", amountsBuilderString.toString());
                    }

                    if (invoiceDetailsArray.length() > 0) {
                        paramJobj.put("invoicedetails", invoiceDetailsArray.toString());
                    }
                }
                JSONObject tempReqJson = new JSONObject(paramJobj.toString());
                tempReqJson= wsUtilService.manipulateGlobalLevelFieldsNew(tempReqJson, paramJobj.optString(Constants.companyKey));
                JSONArray detailArr = new JSONArray(tempReqJson.optString(Constants.detail, "[{}]"));
                JSONArray detailResultArr = new JSONArray();

                for (int i = 0; i < detailArr.length(); i++) {
                    //detailObj holds information about each row at line level
                    JSONObject detailObj = detailArr.getJSONObject(i);
                    // to calculate linked detailsid at product level
                    String[] linkNumbers = linkNUmbers.split(",");
                    for (int io = 0; io < linkNumbers.length; io++) {
                        String linkid = linkNumbers[io];
                        JSONObject returnObj = accPOSInterfaceDAO.getInvoiceDetailsid(linkid, tempReqJson.optString(Constants.companyKey), detailObj.optString(Constants.productid));
                        String detailsLinkid = returnObj.optString(Constants.Acc_id);
                        if (!StringUtil.isNullOrEmpty(detailsLinkid)) {// if sodetails id is not present for that salesorder
                            detailObj.put("rowid", detailsLinkid);
                        } else {
                            detailObj.put("rowid", "");
                        }
                    }
                    detailResultArr.put(detailObj);
                }
                // if detail id array is modified 
                if (detailResultArr.length() > 0) {
                    tempReqJson.put(Constants.detail, detailResultArr.toString());
                }
                //not saving paydetails in Invoice as it is credit sales. Paydetail is only required for Receive Payment
                response = transactionService.saveSalesReturn(tempReqJson);
            }
        } catch (JSONException ex) {
            msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            System.out.println("Exception-> " + ex.getMessage().toString());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } 
        return response;
    }
   
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCashoutReports(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject returnJson = new JSONObject();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);

            Date transactionDate = null;//register openingdate
            DateFormat df = authHandler.getDateOnlyFormat();
            String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
            if (paramJobj.has("transactiondate") && paramJobj.get("transactiondate") != null) {
                String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("transactiondate"), originalDateFormat);
                transactionDate = df.parse(convertedDate);
                paramJobj.put("transactiondate", transactionDate);
            }
            if (paramJobj.has("startdate") && paramJobj.get("startdate") != null) {
                String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("startdate"), originalDateFormat);
                Date startdate = df.parse(convertedDate);
                paramJobj.put("startdate", startdate);
            }
            if (paramJobj.has("enddate") && paramJobj.get("enddate") != null) {
                String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("enddate"), originalDateFormat);
                Date enddate = df.parse(convertedDate);
                paramJobj.put("enddate", enddate);
            }
            paramJobj.put(Constants.isForReport, true);
            KwlReturnObject returnObj = accPOSInterfaceDAO.getPOSCashOutDetails(paramJobj);
            JSONArray jArray = new JSONArray();
            if (returnObj.getEntityList() != null && returnObj.getRecordTotalCount() > 0 && returnObj.getEntityList().get(0) != null) {
                List<CashOut> cashOutList = returnObj.getEntityList();
                for (CashOut cashOutObj : cashOutList) {
                    JSONObject jobj = new JSONObject();
                    jobj.put(Constants.Acc_id, cashOutObj.getID());
                    jobj.put("storeid", cashOutObj.getStoreid());
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Store.class.getName(), cashOutObj.getStoreid());
                    Store storeObj = (Store) curreslt.getEntityList().get(0);
                    if (storeObj != null) {
                        jobj.put(POSERPMapping.Store_Name, storeObj.getAbbreviation());
                    }
                    jobj.put(Constants.amount, cashOutObj.getAmount());
                    jobj.put("transactiondate", cashOutObj.getTransactionDate());
                    jobj.put("transactiondateinlong", cashOutObj.getTransactionDateinLong());

                    if (cashOutObj.getUserid() != null) {
                        jobj.put("userid", cashOutObj.getUserid().getUserID());
                        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), cashOutObj.getUserid().getUserID());
                        User userObj = (User) userResult.getEntityList().get(0);
                        if (userObj != null) {
                            jobj.put(Constants.userfullname, userObj.getFullName());
                        }
                    }
                    if (cashOutObj.getReason() != null) {
                        jobj.put("reasonid", cashOutObj.getReason().getID());
                        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), cashOutObj.getReason().getID());
                        MasterItem masterObj = (MasterItem) userResult.getEntityList().get(0);
                        if (masterObj != null) {
                            jobj.put("reasonname", masterObj.getValue());
                        }
                    }
                    jArray.put(jobj);
                }
                
//                List<Object[]> detailList = returnObj.getEntityList();
//                for (Object[] row : detailList) {
//                    JSONObject jobj = new JSONObject();
//                    if (row.length >= 1) {
//                        if (row[0] != null) {
//                            String id = (String) row[0];
//                            jobj.put(Constants.Acc_id, id);
//                        }
//                    }
//                    if (row.length >= 2) {
//                        if (row[1] != null) {
//                            String storeid = (String) row[1];
//                            jobj.put("storeid", storeid);
//                            KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Store.class.getName(), storeid);
//                            Store storeObj = (Store) curreslt.getEntityList().get(0);
//                            if (storeObj != null) {
//                                jobj.put(POSERPMapping.Store_Name, storeObj.getAbbreviation());
//                            }
//                        }
//                    }
//
//                    if (row.length >= 3) {
//                        if (row[2] != null) {
//                            double cashOutAmount = (Double) row[2];
//                            jobj.put(Constants.amount, cashOutAmount);
//                        }
//                    }
//                    if (row.length >= 4) {
//                        if (row[3] != null) {
//                            jobj.put("transactiondate", row[3].toString());
//                        }
//                    }
//                    if (row.length >= 5) {
//                        if (row[4] != null) {
//                            jobj.put("userid", row[4].toString());
//                              KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(User.class.getName(), row[4].toString());
//                            User userObj = (User) curreslt.getEntityList().get(0);
//                            if (userObj != null) {
//                                jobj.put(Constants.userfullname, userObj.getFullName());
//                            }
//                        }
//                    }
//                    
//                    if (row.length >= 6) {
//                        if (row[5] != null) {
//                            jobj.put("reasonid", row[5].toString());
//                        }
//                    }
//
//                    jArray.put(jobj);
//                }
            }
            returnJson.put(Constants.data, jArray);
            returnJson.put(Constants.RES_success, true);
            returnJson.put(Constants.RES_TOTALCOUNT, jArray.length());

        } catch (Exception e) {
            Logger.getLogger(POSInterfaceServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnJson;
    }
  
    @Override
//    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
    public JSONObject saveAdvanceReceiptPayment(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        JSONObject transResponse = new JSONObject();
        String receiveSeqFormat = null;
        String walkinCustomerId = null;
        boolean isEdit = paramJobj.optBoolean(Constants.isEdit);
        try {
            if (paramJobj != null) {
                paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
                paramJobj.put(Constants.isdefaultHeaderMap, true);
                paramJobj.put(Constants.isForPos, true);
           
                String companyid = paramJobj.optString(Constants.companyKey);
                
                //not required in case of edit as we fetching information from document of receive payment
                if (!isEdit) {
                    //Get receive payment,sales ordersequenceformatid and walkin customerid from POS erp config table
                    KwlReturnObject returnObj = accPOSInterfaceDAO.getPOSConfigDetails(paramJobj);
                    if (returnObj.getEntityList() != null && returnObj.getRecordTotalCount() > 0 && returnObj.getEntityList().size() == 1 && returnObj.getEntityList().get(0) != null) {
                        List<Object[]> detailList = returnObj.getEntityList();

                        if (detailList.size() == 1) {
                            Object[] row = (Object[]) returnObj.getEntityList().get(0);
                            //receive payment sequenceformatid 
                            if (row[11] != null) {
                                receiveSeqFormat = (String) row[11];
                            } else {
                                throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.receivePaymentSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                            }
                            //Walkin customerid
                            if (row[1] != null) {
                                walkinCustomerId = (String) row[1];
                            }
                            //Walkin Customer
                            if (!paramJobj.has(Constants.customerName) && !StringUtil.isNullOrEmpty(walkinCustomerId)) {
                                paramJobj.put(Constants.customerName, walkinCustomerId);
                                paramJobj.put("CustomerName", walkinCustomerId);
                                KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), walkinCustomerId);
                                Customer cObj = (Customer) customerResult.getEntityList().get(0);
                                if (cObj != null) {
                                    paramJobj.put("acccode", cObj.getAcccode());
                                    //used to pass iscustomer value in transaction serviceimpl
                                    paramJobj.put("customervalue", cObj.getAcccode());
                                    paramJobj.put("term", cObj.getCreditTerm() != null ? cObj.getCreditTerm().getID() : "");
                                } else {
                                    throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.defaultCustomerPOS", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                                }
                            }
                        }
                    } else {//end of resdata
                        throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.settingForStoreMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                    }
                }
                transResponse = saveAdvanceReceivePayment(paramJobj, receiveSeqFormat, walkinCustomerId);
            }
        } catch (ServiceException |AccountingException ex) {
            System.out.println("Exception-> " + ex.getMessage().toString());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return transResponse;
    }
    
    public JSONObject saveAdvanceReceivePayment(JSONObject paramJobj, String receiveSeqFormat, String walkinCustomerId) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
        JSONObject transResponse = new JSONObject();
        boolean isSuccess = false;
        String invid = null;
        String receiptNumber = null;
        String msg = null;
        boolean isEdit = paramJobj.optBoolean(Constants.isEdit);
        String receiptid = paramJobj.optString("receiptid", null);
        String companyid = paramJobj.optString(Constants.companyKey);
        String userid = paramJobj.optString(Constants.useridKey);
        try {

            StringBuilder receiptNoString = new StringBuilder();
            StringBuilder receiptIdString = new StringBuilder();
            String paydetails = paramJobj.optString("paydetail");
            //Multiple payment method handled
            if (!StringUtil.isNullOrEmpty(paydetails)) {
                JSONArray pDetailsArray = new JSONArray(paydetails);
                for (int j = 0; j < pDetailsArray.length(); j++) {

                    //receive payment json for saving
                    JSONObject rpJson = new JSONObject(paramJobj.toString());
                    
                    //Edit case
                    if (isEdit && !StringUtil.isNullOrEmpty(receiptid)) {
                        KwlReturnObject rpResult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                        Receipt rpObj = (Receipt) rpResult.getEntityList().get(0);
                        if (rpObj != null) {
                                                        
                            //Checking Active Days Case where it doesn't allow to edit if the document date doesn't come in active days .
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("userID", userid);
                            requestParams.put("companyID", companyid);
                            requestParams.put("moduleID", Constants.Acc_Receive_Payment_ModuleId);
                            CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, rpObj.getCreationDate());
                            
                            //Checking if Advance Payment is linked to any Sales Order or not. If linked not allowing to edit 
                            JSONObject params = new JSONObject();
                            StringBuilder linkedNoString = new StringBuilder();
                            params.put("receiptid", receiptid);
                            List<Object[]> resultList = accReceiptDAOobj.getAdvanceReceiptUsedSalesOrder(params);
                            for (int i = 0; i < resultList.size(); i++) {
                                Object[] objArray = (Object[]) resultList.get(i);
                                if (objArray[2] != null) {
                                    KwlReturnObject spResult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), objArray[2].toString());
                                    SalesOrder sObj = (SalesOrder) spResult.getEntityList().get(0);
                                    if (sObj != null) {
                                        linkedNoString.append(sObj.getSalesOrderNumber() + ",");
                                    }
                                }
                            }
                            
                            //Building message to show user the linked transaction info.
                            if (linkedNoString.length() > 0) {
                                String transactionno = linkedNoString.toString();
                                transactionno = transactionno.substring(0, transactionno.length() - 1);
                                String errorMsg = messageSource.getMessage("acc.RestSerivce.editReceiptlinkedMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))) + transactionno + "." + messageSource.getMessage("acc.RestSerivce.cannotProceedToEdit", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                                //message =Selected receive payment is already used in Sales Order :SO1,SO2 so cannot proceed to edit.
                                throw ServiceException.FAILURE(errorMsg, "", false);
                            }
                            
                            //Checking if Advance Payment is linked to any Invoice or not. If linked not allowing to edit 
                            
                            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                            filter_names.clear();
                            filter_params.clear();
                            filter_names.add("receipt.ID");
                            filter_params.add(rpObj.getID());
                            requestParams.put("filter_names", filter_names);
                            requestParams.put("filter_params", filter_params);
                            KwlReturnObject invDetailsResult = accInvoiceDAOobj.getLinkDetailReceipts(requestParams);
                            List<LinkDetailReceipt> rpdetails = invDetailsResult.getEntityList();
                            if (rpdetails != null && rpdetails.size() > 0) {
                                String errorMsg = messageSource.getMessage("acc.RestSerivce.editReceiptlinkedInvoicesMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)))+ "." + messageSource.getMessage("acc.RestSerivce.cannotProceedToEdit", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                                msg=new String(errorMsg);
                                throw ServiceException.FAILURE(errorMsg, "", false);
                            }
                            rpJson.put(Constants.sequenceformat, rpObj.getSeqformat().getID());
                            receiveSeqFormat=rpObj.getSeqformat().getID();
                            rpJson.put(Constants.billid, rpObj.getID());
                            rpJson.remove("customervalue");
                            rpJson.put("CustomerName", rpObj.getCustomer().getID());
                            rpJson.put(Constants.customerName, rpObj.getCustomer().getID());
                            rpJson.put("no", rpObj.getReceiptNumber());
                            
                            if (rpObj.getCustomer() != null) {
                                KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), rpObj.getCustomer().getID());
                                Customer cObj = (Customer) customerResult.getEntityList().get(0);
                                if (cObj != null) {
                                    rpJson.put("acccode", cObj.getAcccode());
                                    //used to pass iscustomer value in transaction serviceimpl
                                    rpJson.put("customervalue", cObj.getAcccode());
                                }
                            }
                        }
                    }
                    JSONObject paymentJson = pDetailsArray.getJSONObject(j);

                    if (paymentJson.has("pmtmethod") && !StringUtil.isNullOrEmpty(paymentJson.optString("pmtmethod", null))) {
                        rpJson.put("pmtmethod", paymentJson.optString("pmtmethod"));
                        rpJson.put("paymentmethodid", paymentJson.optString("pmtmethod"));
                        //For Cash Payment
                        if (paymentJson.has("paymentmethodtype") && paymentJson.optInt("paymentmethodtype") == 0) {
                            rpJson.put("paydetail", "");
                        } else {
                            rpJson.put("paydetail", paymentJson);
                        }
                    } else {
                        rpJson.put("pmtmethodvalue", "Cash");
                        rpJson.put("paydetail", "");
                        rpJson = wsUtilService.manipulateGlobalLevelFieldsNew(rpJson, paramJobj.optString(Constants.companyKey));
                    }


                    /********--------------------Save Receive Payment------------------------------********/
                    //Total amount
                    rpJson.put("amount", paymentJson.optDouble("amount", 0.0));
                    //Creating json for advance payment
                    JSONArray advanceDetailArray = new JSONArray();
                    JSONObject advanceJson = new JSONObject();
                    advanceJson.put("type", Constants.AdvancePayment);
                    advanceJson.put("enteramount", paymentJson.optDouble("amount", 0.0));
                    advanceDetailArray.put(advanceJson);
                    rpJson.put("details", advanceDetailArray);
                    rpJson.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);

                    //SEQUENCE format is replaced according to mapped configuration
                    rpJson.put(Constants.sequenceformat, receiveSeqFormat);
                    if (rpJson.has(Constants.currencyName) && !StringUtil.isNullOrEmpty(rpJson.optString(Constants.currencyName, null))) {
                        rpJson.put(Constants.currencyKey, rpJson.optString(Constants.currencyName, null));
                    }
                    JSONObject rpResponseJson = transactionService.saveReceiptPayment(rpJson);

                    if (rpResponseJson.has("billno") && !StringUtil.isNullOrEmpty(rpResponseJson.optString("billno", null)) && rpResponseJson.has(Constants.RES_success) && rpResponseJson.optBoolean(Constants.RES_success)) {
                        System.out.println("->Receipt saved successfully.");
                        if (rpResponseJson.has("requestParamsJson") && rpResponseJson.getJSONObject("requestParamsJson") != null) {
                            rpJson = rpResponseJson.getJSONObject("requestParamsJson");
                            rpJson.remove(Constants.detail);
                            rpJson.remove("Details");
                        }
                        receiptNumber = rpResponseJson.optString("billno");
                        receiptid = rpResponseJson.optString("paymentid");
                        receiptNoString.append(receiptNumber + ",");
                        receiptIdString.append(receiptid + ",");
                        isSuccess = true;
                    }
                    if (!isSuccess) {
                        msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                    } else {
                        msg = messageSource.getMessage("acc.RestSerivce.successMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                    }
                }//end of for (int j = 0; j < pDetailsArray.length(); j++)

                if (receiptNoString.length() > 0) {
                    String rnos = receiptNoString.toString();
                    receiptNumber = rnos.substring(0, rnos.length() - 1);
                    transResponse.put("receiptno", receiptNumber);
                }

                if (receiptIdString.length() > 0) {
                    String rids = receiptIdString.toString();
                    receiptid = rids.substring(0, rids.length() - 1);
                    transResponse.put("receiptid", receiptid);
                }
            }//end of if (!StringUtil.isNullOrEmpty(paydetails)) 
        } catch (JSONException ex) {
            JSONObject deletejson = transactionService.jsonDeleteInvoice(invid, paramJobj);
            deletejson = transactionService.deleteInvoice(deletejson);
            msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            System.out.println("Exception-> " + ex.getMessage().toString());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            transResponse.put(Constants.RES_success, isSuccess);
            transResponse.put(Constants.RES_MESSAGE, msg);
        }
        return transResponse;
    }
    
     //Case:link advance receipts TO SalesOrder-ERP-39694
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveSalesOrderLinkedAdvanceReceipts(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException, UnsupportedEncodingException,AccountingException {
        JSONObject transResponse = new JSONObject();
        boolean isSuccess = false;
        String msg = null;
        boolean isReceipt = false;
        String receiptNumber = null, receiptid = null;
        try {
            if (paramJobj != null) {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            paramJobj.put(Constants.isdefaultHeaderMap, true);
            //Credit Term
            paramJobj.put("term", paramJobj.optString("terms", null));
            paramJobj.put(Constants.isForPos, true);
            
                String soSeqFormat = null;
                String receiveSeqFormat = null;
                String walkinCustomerId = null;
                 //Handled for edit case: fetching receiptid on the basis of salesorderid
                boolean isEdit = paramJobj.optBoolean(Constants.isEdit);
                String billid = paramJobj.optString(Constants.billid);
                String companyid = paramJobj.optString(Constants.companyKey);
                String userid = paramJobj.optString(Constants.useridKey);
                boolean islinkAdvancepayment=paramJobj.optBoolean(Constants.islinkadvanceflag);
                
                // to link advance receipt to salesorder. When advance Receipt is linked to Sales Order in POS from sales order report
                if (islinkAdvancepayment) {
                    transResponse= transactionService.savelinkAdvanceReceiptToSalesOrder(paramJobj);
                } else { // to save sales order with advance receipt

                    //Fetch information from POS ERP mapping table for create case only
                    if (!isEdit) {
                        //Get receive payment,sales ordersequenceformatid and walkin customerid from POS erp config table
                        KwlReturnObject returnObj = accPOSInterfaceDAO.getPOSConfigDetails(paramJobj);
                        if (returnObj.getEntityList() != null && returnObj.getRecordTotalCount() > 0 && returnObj.getEntityList().size() == 1 && returnObj.getEntityList().get(0) != null) {
                            List<Object[]> detailList = returnObj.getEntityList();

                            if (detailList.size() == 1) {
                                Object[] row = (Object[]) returnObj.getEntityList().get(0);
                                //receive payment sequenceformatid 
                                if (row[11] != null) {
                                    receiveSeqFormat = (String) row[11];
                                } else {
                                    throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.receivePaymentSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                                }
                                //sales order sequenceformatid
                                if (row[12] != null) {
                                    soSeqFormat = (String) row[12];
                                } else {
                                    throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.salesOrderSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                                }

                                //Walkin customerid
                                if (row[1] != null) {
                                    walkinCustomerId = (String) row[1];
                                }
                                //Walkin Customer
                                if (!paramJobj.has(Constants.customerName) && !StringUtil.isNullOrEmpty(walkinCustomerId)) {
                                    paramJobj.put(Constants.customerName, walkinCustomerId);
                                    paramJobj.put("CustomerName", walkinCustomerId);
                                    KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), walkinCustomerId);
                                    Customer cObj = (Customer) customerResult.getEntityList().get(0);
                                    if (cObj != null) {
                                        paramJobj.put("acccode", cObj.getAcccode());
                                        //used to pass iscustomer value in transaction serviceimpl
                                        paramJobj.put("customervalue", cObj.getAcccode());
                                        paramJobj.put("term", cObj.getCreditTerm() != null ? cObj.getCreditTerm().getID() : "");
                                    } else {
                                        throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.defaultCustomerPOS", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                                    }
                                }
                            }
                        } else {//end of resdata
                            throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.settingForStoreMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                        }
                    } else if (isEdit && !StringUtil.isNullOrEmpty(billid)) {//For edit case

                        KwlReturnObject rpResult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), billid);
                        SalesOrder soObj = (SalesOrder) rpResult.getEntityList().get(0);
                        if (soObj != null) {

                            //Checking Active Days Case where it doesn't allow to edit if the document date doesn't come in active days .
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("userID", userid);
                            requestParams.put("companyID", companyid);
                            requestParams.put("moduleID", Constants.Acc_Sales_Order_ModuleId);
                            CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, soObj.getOrderDate());


                            //if it is linked to any invoice then it should not allow to edit the transaction        
                            requestParams = new HashMap<String, Object>();
                            requestParams.put("soid", soObj.getID());
                            requestParams.put("companyid", companyid);
                            if (!StringUtil.isNullOrEmpty(soObj.getID())) {
                                KwlReturnObject result = accInvoiceDAOobj.getInvoicesMerged(requestParams);
                                List list = result.getEntityList();
                                if (list.size() > 0) {
                                    throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.editSalesOrderInvoiceMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                                }
                            }


                            //Link advance receive payment
                            JSONObject params = new JSONObject();
                            params.put("docid", "'" + billid + "'");
                            params.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                            KwlReturnObject podresult = accSalesOrderDAOobj.getLinkedDocByModuleId(params);
                            if (podresult.getEntityList() != null && !podresult.getEntityList().isEmpty()) {
                                List<Object[]> list = podresult.getEntityList();
                                for (Object[] advanceDetail : list) {
                                    if (advanceDetail[5] != null) {
                                        receiptid = advanceDetail[5].toString();
                                        receiptNumber = advanceDetail[4].toString();
//                                    paramJobj.put("receiptid", advanceDetail[3].toString());
//                                    paramJobj.put("receiptno", advanceDetail[4].toString());
                                    }
                                }
                            }

                            paramJobj.put(Constants.customerName, soObj.getCustomer().getID());
                            paramJobj.put(Constants.currencyName, soObj.getCurrency().getCurrencyID());
                            paramJobj.put("salesOrderNumber", soObj.getSalesOrderNumber());
                            paramJobj.put(Constants.billid, soObj.getID());
                            soSeqFormat = soObj.getSeqformat().getID();
                        } else {
                            throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.validRecordMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                        }
                    } else {
                        throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.settingForStoreMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                    }

                    /*
                     * ----------------------------------Save Advance Receipt----------------------------
                     */
                    JSONObject rpAfterSaveJson = saveAdvanceReceivePayment(paramJobj, receiveSeqFormat, walkinCustomerId);
                    if (rpAfterSaveJson.optBoolean(Constants.RES_success) && !StringUtil.isNullOrEmpty(rpAfterSaveJson.optString("receiptid", null))) {
                        receiptid = rpAfterSaveJson.optString("receiptid");
                        receiptNumber = rpAfterSaveJson.optString("receiptno");
                        transResponse.put("receiptno", receiptNumber);
                        transResponse.put("receiptid", receiptid);
                    }


                    /**
                     * *******--------------------Save Sales Order------------------------------*********
                     */
                    JSONObject salesOrderJsonObj = new JSONObject(paramJobj.toString());
                    if (!StringUtil.isNullOrEmpty(receiptid)) {
                        salesOrderJsonObj.put("receiptid", receiptid);
                        salesOrderJsonObj.put("receiptno", receiptNumber);
                    }
                    salesOrderJsonObj.remove("paydetail");
                    salesOrderJsonObj.put(Constants.sequenceformat, soSeqFormat);
                    salesOrderJsonObj.put("OrderDate", paramJobj.optString("billdate"));
                    salesOrderJsonObj.put(Constants.moduleid, String.valueOf(Constants.Acc_Sales_Order_ModuleId));
                    //building json for Custom field of SO

                    JSONObject soResponse = transactionService.saveSalesOrder(salesOrderJsonObj);
                    if (soResponse.optBoolean(Constants.RES_success)) {
                        transResponse.put("salesorderid", soResponse.optString("billid"));
                        transResponse.put("salesorderno", soResponse.optString("billno"));
                        isSuccess = true;
                    }
                    transResponse.put(Constants.RES_success, isSuccess);
                    transResponse.put(Constants.RES_msg, msg);
                }
            }
        } catch (Exception ex) {
            if (isReceipt) {
                JSONObject deletejson = transactionService.jsonDeleteReceivePayment(receiptid, paramJobj, receiptNumber);
                deletejson = transactionService.deleteReceivePayment(deletejson);
                transResponse.remove("receiptno");
                transResponse.remove("receiptid");
            }
            msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            System.out.println("Exception-> " + ex.getMessage().toString());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return transResponse;
    }
    
    @Override
    public JSONObject saveReceivePaymentAgainstInvoice(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;

        String receiptid = null;
        String receiptNumber = null;
        String msg = null;

        try {
            if (paramJobj != null) {
                paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
                paramJobj.put(Constants.isdefaultHeaderMap, true);
                paramJobj.put(Constants.isForPos, true);
                paramJobj.put("iscustomer", true);
                JSONObject tempReqJson = new JSONObject(paramJobj.toString());

                //If payment amount entered is greater than invoice amount then proceed with transaction
                String paydetails = paramJobj.optString("paydetail");
                String receiveSeqFormat = null;
                String walkinCustomerId = null;
                
                
                //Fetching receive payment sequenceformat and walkin customerid configuration of POS 
                JSONObject returnJObj = accPOSInterfaceService.getERPPOSMappingDetails(paramJobj);
                if (returnJObj.has(Constants.RES_data) && returnJObj.get(Constants.RES_data) != null && returnJObj.getJSONArray(Constants.RES_data).length() == 1) {
                    JSONArray storeArray = returnJObj.getJSONArray(Constants.RES_data);
                    for (int i = 0; i < storeArray.length(); i++) {
                        JSONObject sObj = storeArray.getJSONObject(i);

                        if (sObj.has(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT) && sObj.get(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT) != null) {
                            receiveSeqFormat = (String) sObj.get(POSERPMapping.RECEIVEPAYMENT_SEQUENCEFORMAT);
                        } else {
                            throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.receivePaymentSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                        }

                        if (sObj.has(POSERPMapping.WalkinCustomer) && sObj.get(POSERPMapping.WalkinCustomer) != null) {
                            walkinCustomerId = (String) sObj.get(POSERPMapping.WalkinCustomer);
                        }
                        //Walkin Customer
                        if (!paramJobj.has("customervalue") && !StringUtil.isNullOrEmpty(walkinCustomerId)) {
                            paramJobj.put(Constants.customerName, walkinCustomerId);
                            paramJobj.put("CustomerName", walkinCustomerId);
                            tempReqJson.put(Constants.customerName, walkinCustomerId);
                            tempReqJson.put("CustomerName", walkinCustomerId);
                            KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), walkinCustomerId);
                            Customer cObj = (Customer) customerResult.getEntityList().get(0);
                            if (cObj != null) {
                                paramJobj.put("acccode", cObj.getAcccode());
                                tempReqJson.put("acccode", cObj.getAcccode());
                            } else {
                                throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.defaultCustomerPOS", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                            }
                        }
                    }//end of for loop
                } else {//end of resdata
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.receivePaymentSequenceFormatMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }

                
               /**************Building Global Field JSON*********************/ 
                //If invoice and transaction is successful
                StringBuilder receiptNoString = new StringBuilder();
                StringBuilder receiptIdString = new StringBuilder();
                double invoiceReceiveAmount = 0.0;
                //Multiple payment method handled
                if (!StringUtil.isNullOrEmpty(paydetails)) {
                    JSONArray pDetailsArray = new JSONArray(paydetails);
                    for (int j = 0; j < pDetailsArray.length(); j++) {
                        JSONObject paymentJson = pDetailsArray.getJSONObject(j);

                            if (paymentJson.has("pmtmethod") && !StringUtil.isNullOrEmpty(paymentJson.optString("pmtmethod", null))) {
                                tempReqJson.put("pmtmethod", paymentJson.optString("pmtmethod"));
                                tempReqJson.put("paymentmethodid", paymentJson.optString("pmtmethod"));

                                //For Cash Payment
                                if (paymentJson.has("paymentmethodtype") && paymentJson.optInt("paymentmethodtype") == 0) {
                                    tempReqJson.put("paydetail", "");
                                } else {
                                    tempReqJson.put("paydetail", paymentJson);
                                }
                            } else {
                                tempReqJson.put("pmtmethodvalue", "Cash");
                                tempReqJson.put("paydetail", "");
                                tempReqJson = wsUtilService.manipulateGlobalLevelFieldsNew(tempReqJson, paramJobj.optString(Constants.companyKey));
                            }

                            if (paymentJson.has("amount") && !StringUtil.isNullOrEmpty(paymentJson.optString("amount", null))) {
                                invoiceReceiveAmount = paymentJson.optDouble("amount", 0.0);
                                //updating the enteramount with payment method as multiple methods will be made.
                                JSONArray details = tempReqJson.optJSONArray("details");
                                JSONArray modifiedDetailsArray=new  JSONArray();
                                //The amount paid in payment method will the enter amount in invoice
                                if (details.length() > 0) {
                                    for (int i = 0; i < details.length(); i++) {
                                        JSONObject jobj = details.getJSONObject(i);
                                        if (jobj.optInt("documenttype", 0) == Constants.PaymentAgainstInvoice) {
                                            jobj.put("enteramount", invoiceReceiveAmount);
                                            modifiedDetailsArray.put(jobj);
                                        }
                                    }
                                    tempReqJson.put("details", modifiedDetailsArray);
                                }
                            }
                            tempReqJson.put(Constants.sequenceformat, receiveSeqFormat);
                            if (tempReqJson.has(Constants.currencyName) && !StringUtil.isNullOrEmpty(tempReqJson.optString(Constants.currencyName, null))) {
                                tempReqJson.put(Constants.currencyKey, tempReqJson.optString(Constants.currencyName, null));
                            }
                            if(!tempReqJson.has(Constants.currencyKey) ){
                                tempReqJson.put(Constants.currencyKey, tempReqJson.optString(Constants.globalCurrencyKey, null));
                            }
                            
                            //Saving receive payment
                            JSONObject rpResponseJson = transactionService.saveReceiptPayment(tempReqJson);

                            if (rpResponseJson.has("billno") && !StringUtil.isNullOrEmpty(rpResponseJson.optString("billno", null)) && rpResponseJson.has(Constants.RES_success) && rpResponseJson.optBoolean(Constants.RES_success)) {
                                System.out.println("->Receipt saved successfully.");
                                receiptNumber = rpResponseJson.optString("billno");
                                if (receiptNoString.length() > 0) {
                                    receiptNoString.append(",");
                                }
                                receiptNoString.append(receiptNumber);
                                receiptid = rpResponseJson.optString("paymentid");
                                if (receiptIdString.length() > 0) {
                                    receiptIdString.append(",");
                                }
                                receiptIdString.append(receiptid);
                                isSuccess=true;
                            }
                        if (!isSuccess) {
                            msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                        } else {
                            if (!StringUtil.isNullOrEmpty(response.optString(Constants.RES_MESSAGE, null))) {
                                msg = response.optString(Constants.RES_MESSAGE, null);
                            } else {
                                msg = messageSource.getMessage("acc.RestSerivce.successMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                            }
                            
                        }
                    }//end of for (int j = 0; j < pDetailsArray.length(); j++)
                    response.put("receiptnumber", receiptNoString.toString());
                    response.put("paymentIds", receiptIdString.toString());
                }//end of if (!StringUtil.isNullOrEmpty(paydetails)) 

            }
        } catch (JSONException ex) {
            msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            System.out.println("Exception-> " + ex.getMessage().toString());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            response.put(Constants.RES_success, isSuccess);
            response.put(Constants.RES_MESSAGE, msg);
        }
        return response;
    }
    
    /*@Description: Save Cash Out Transaction Deposit type. It is funds transfer=ERP-40061*/
    @Override
    @Transactional(propagation = Propagation.REQUIRED , rollbackFor = {ServiceException.class, SessionExpiredException.class, JSONException.class, AccountingException.class})
    public JSONObject saveCashOutTransactionDepositType(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException, UnsupportedEncodingException {
        JSONObject transResponse = new JSONObject();
        boolean isSuccess = false;
        String msg = null;
        double amount=0;
        String journalEntryNo = null, journalEntryId = null;

        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            paramJobj.put(Constants.isdefaultHeaderMap, true);
            paramJobj.put(Constants.isForPos, true);
            String companyid = paramJobj.optString(Constants.companyKey);
            
            //to save in deposit amount
            String storeid = paramJobj.optString(Constants.storeid);
            String userid = paramJobj.optString(Constants.useridKey);
            
            if (paramJobj != null) {
                String depositAccountId = null;
                String cashPaymentAccountId = null;
                
                //Get deposit account id from POS erp config table
                KwlReturnObject returnObj = accPOSInterfaceDAO.getPOSConfigDetails(paramJobj);
                if (returnObj.getEntityList() != null && returnObj.getRecordTotalCount() > 0 && returnObj.getEntityList().size() == 1 && returnObj.getEntityList().get(0) != null) {
                    List<Object[]> detailList = returnObj.getEntityList();
                    
                    if (detailList.size() == 1) {
                        Object[] row = (Object[]) returnObj.getEntityList().get(0);
                        if (row[13] != null) {
                            depositAccountId = (String) row[13];
                        } else {
                            throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.depositAccountMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                        }
                    }
                } else {//end of resdata
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.RestSerivce.depositAccountMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
                
                //GET Default SequenceFormat for journal entry 
                paramJobj.put(Constants.moduleid,Constants.Acc_GENERAL_LEDGER_ModuleId);
                paramJobj.put("typevalue",Constants.FUND_TRANSFER_JOURNAL_ENTRY);//Funds Transfer case
                paramJobj = wsUtilService.getSequenceFormatId(paramJobj, String.valueOf(Constants.Acc_GENERAL_LEDGER_ModuleId));
                
                //JSON for receive payment
                StringBuilder journalEntryNoString = new StringBuilder();
                StringBuilder journalEntryIdString = new StringBuilder();
                String paydetails = paramJobj.optString("paydetail");
                
                //Multiple payment method handled
                if (!StringUtil.isNullOrEmpty(paydetails)) {
                    JSONArray pDetailsArray = new JSONArray(paydetails);
                    for (int j = 0; j < pDetailsArray.length(); j++) {

                        //receive payment json for saving
                        JSONObject rpJson = new JSONObject(paramJobj.toString());
                        JSONObject paymentJson = pDetailsArray.getJSONObject(j);

                        
                        /*-----------------------------Creating Global Field Json------------------------------*/
                        
                        if (paymentJson.has("pmtmethod") && !StringUtil.isNullOrEmpty(paymentJson.optString("pmtmethod", null))) {
                            String paymentMethodid = paymentJson.optString("pmtmethod");
                            KwlReturnObject olddebittermresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paymentMethodid);
                            PaymentMethod pmtMehodObj = (PaymentMethod) olddebittermresult.getEntityList().get(0);
                            if (pmtMehodObj != null && pmtMehodObj.getAccount() != null) {
                                cashPaymentAccountId = pmtMehodObj.getAccount().getID();
                            }

                            rpJson.put("pmtmethod", paymentJson.optString("pmtmethod"));
                            rpJson.put("paymentmethodid", paymentJson.optString("pmtmethod"));
                            //For Cash Payment paydetail is empty. For Cheque, it is jsonobject and its details like cheque number and cheque is passed
                            if (paymentJson.has("paymentmethodtype") && paymentJson.optInt("paymentmethodtype") == 0) {
                                rpJson.put("paydetail", "");
                            } else {
                                rpJson.put("paydetail", paymentJson);
                            }
                        } else {
                            rpJson.put("pmtmethodvalue", "Cash");
                            rpJson.put("paydetail", "");
                        }
                        
                        
                        //Building custom field jsonarray on the basis of Name and value. For e.g. name=Outlet and value=Location
                       rpJson = wsUtilService.manipulateGlobalLevelFieldsNew(rpJson, paramJobj.optString(Constants.companyKey));
                       amount +=paymentJson.optDouble("amount", 0.0);
                       rpJson.put("amount", paymentJson.optDouble("amount", 0.0));
                        
                        /*-----------------------------Creating Detail Level JSON-----------------------------*/
                        JSONArray accountDetailArray = new JSONArray();
                        
                        //Putting cash payment accountid for Cash Payment Case
                        JSONObject accountJson = new JSONObject();
                        accountJson.put("accountid",cashPaymentAccountId);
                        accountJson.put("customerVendorId",cashPaymentAccountId);
                        accountJson.put("rowid","1");
                        accountJson.put("amount", paymentJson.optDouble("amount", 0.0));
                        accountJson.put("exchangeratefortransaction", "1");
                        accountJson.put("debit", "false");
                        accountDetailArray.put(accountJson);
                        
                        
                        //Putting deposit account id Deposit case
                        accountJson = new JSONObject();
                        accountJson.put("accountid",depositAccountId);
                        accountJson.put("customerVendorId",depositAccountId);
                        accountJson.put("rowid","2");
                        accountJson.put("amount", paymentJson.optDouble("amount", 0.0));
                        accountJson.put("exchangeratefortransaction", "1");
                        accountJson.put("debit", "true");
                        accountDetailArray.put(accountJson);
                        
                        rpJson.put("detail", accountDetailArray);
                        rpJson.put(Constants.currencyKey, rpJson.optString(Constants.globalCurrencyKey, null));
                        
                        //Saving Journal Entry
                        JSONObject rpResponseJson = transactionService.saveJournalEntry(rpJson);

                        //BUilding message for response
                        if (rpResponseJson.has("billno") && !StringUtil.isNullOrEmpty(rpResponseJson.optString("billno", null)) && rpResponseJson.has(Constants.RES_success) && rpResponseJson.optBoolean(Constants.RES_success)) {
                            System.out.println("->Journal saved successfully.");
                            journalEntryNo = rpResponseJson.optString("billno");
                            journalEntryNoString.append(journalEntryNo);
                            if (journalEntryNoString.length() > 0) {
                                journalEntryNoString.append(",");
                            }
                            journalEntryId = rpResponseJson.optString(Constants.Acc_id);
                            journalEntryIdString.append(journalEntryId);
                            if (journalEntryIdString.length() > 0) {
                                journalEntryIdString.append(",");
                            }
                            
                            Date transactionDate = new Date();
                            DateFormat df = authHandler.getDateOnlyFormat();
                            String originalDateFormat = paramJobj.optString("dateformat", authHandler.getDateOnlyFormatPattern());
                            if (paramJobj.has("billdate") && paramJobj.get("billdate") != null) {
                                String convertedDate = WSServiceUtil.getGlobalFormattedDate(paramJobj.getString("billdate"), originalDateFormat);
                                transactionDate = df.parse(convertedDate);
                            }
                            /*---------Saving Deposit amount in store-------------*/
                            HashMap<String, Object> reqParams = new HashMap<>();
                            reqParams.put(Constants.companyKey, companyid);
                            reqParams.put(Constants.useridKey, userid);
                            reqParams.put("location", storeid);
                            long transactiondateinlong=transactionDate.getTime();
                            reqParams.put("transactiondateinlong", transactiondateinlong);
                            reqParams.put("transactiondate", transactionDate);
                            reqParams.put("isdeposit", true);
                            reqParams.put("amount", amount);

                            JSONObject saveObj = accPOSInterfaceDAO.saveCashOutDetails(reqParams);
                            isSuccess = true;
                        }
                    }//end of for (int j = 0; j < pDetailsArray.length(); j++)
                }//end of if (!StringUtil.isNullOrEmpty(paydetails)) 
                if (journalEntryNoString.length() > 0) {
                    String journalEntryNos = journalEntryNoString.toString().substring(0, (journalEntryNoString.toString().length() - 1));
                    transResponse.put("journalentrynos", journalEntryNos);
                }
                if (journalEntryIdString.length() > 0) {
                    String journalEntryIds = journalEntryIdString.toString().substring(0, (journalEntryIdString.length() - 1));
                    transResponse.put("journalentryids", journalEntryIds);
                }
                
                transResponse.put(Constants.RES_success, isSuccess);
                
                transResponse.put("journalentryids", journalEntryIdString.toString());
                if (!isSuccess) {
                    msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                } else {
                    msg = messageSource.getMessage("acc.RestSerivce.successMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                }
                transResponse.put(Constants.RES_msg, msg);
            }
        } catch (Exception ex) {
            msg = messageSource.getMessage("acc.RestSerivce.rollBackMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            System.out.println("Exception-> " + ex.getMessage().toString());
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return transResponse;
    }  
    
    
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deleteInvoiceDOandRP(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        boolean isSuccess = false;
        String message = "";
        StringBuilder msgString=new StringBuilder();
        
        try {
            if (paramJobj != null) {
                paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
                if (!paramJobj.has(Constants.companyKey)) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
                
                                
                //Fetching Receive Payment details where receive payment against invoice has been  generated.
                String invid=paramJobj.optString(Constants.billid);
                String companyid=paramJobj.optString(Constants.companyKey);
                String userid=paramJobj.optString(Constants.useridKey);
                String invoicenumber=null;
                //To fetch respected columns
                Map<String, Object> params = new HashMap<>();
                
                /*Building messages for deleted records*/
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invid);
                Invoice invoiceObj = (Invoice) objItr.getEntityList().get(0);
                if (invoiceObj != null) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("userID", userid);
                    requestParams.put("companyID", companyid);
                    requestParams.put("moduleID", Constants.Acc_Invoice_ModuleId);
                    CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, invoiceObj.getCreationDate());
                    invoicenumber=invoiceObj.getInvoiceNumber();
                }

                KwlReturnObject rpresult = accInvoiceDAOobj.getPaymentReceiptsLinkedWithInvoice(invid, companyid);
                List<String> listr = rpresult.getEntityList();
                StringBuilder rnos = new StringBuilder();
               

                /*Delete Receive Payment*/
                for (String rpid : listr) {
                    params.put("ID", rpid); 
                    Object mtdresult = kwlCommonTablesDAOObj.getRequestedObjectFields(Receipt.class, new String[]{"receiptNumber"}, params);
                    String receiptnumber = StringUtil.isNullOrEmpty((String) mtdresult) ? "" : mtdresult.toString();

                    if (!StringUtil.isNullOrEmpty(receiptnumber)) {
                        paramJobj.put(Constants.deletepermanentflag, true);
                        paramJobj.put("receiptno", receiptnumber);
                        JSONObject deleteResponseJson = transactionService.deleteReceivePayment(paramJobj);
                        if (deleteResponseJson.has(Constants.RES_success) && deleteResponseJson.optBoolean(Constants.RES_success)) {
                            rnos.append(receiptnumber + ",");
                        }
                    }
                }
                
                //putting deleted receiptnos in response
                String receiptnumbers=null;
                if (rnos.length() > 0) {
                    receiptnumbers=rnos.toString();
                    receiptnumbers=receiptnumbers.substring(0,receiptnumbers.length()-1);
                }

                /*--------------------------Delete Delivery Order-----------------------------*/
                
                 StringBuilder deliveryOrderNos = new StringBuilder();
                //Fetching the delivery order linked to Invoices
                KwlReturnObject doresult = accInvoiceDAOobj.getDOFromOrToInvoices(invid, companyid);
                List<Object[]> listdo = doresult.getEntityList();
                for (Object[] oj : listdo) {
                    DeliveryOrder deliveryOrder = (DeliveryOrder) oj[0];
                    if (deliveryOrder != null) {
                        paramJobj.put("doid", deliveryOrder.getID());
                        String deliveryOrderNo=deliveryOrder.getDeliveryOrderNumber();
                        paramJobj.put("deliveryorderno",deliveryOrderNo );
                        paramJobj.put(Constants.deletepermanentflag, true);
                        JSONObject deleteResponseJson = transactionService.deleteDeliveryOrdersJSON(paramJobj);
                         if (deleteResponseJson.has(Constants.RES_success) && deleteResponseJson.optBoolean(Constants.RES_success)) {
                            deliveryOrderNos.append(deliveryOrderNo + ",");
                        }
                    }
                }
                
                //putting deleted delivery order nos in response
                String donumbers = null;
                if (rnos.length() > 0) {
                    donumbers = deliveryOrderNos.toString();
                    donumbers = deliveryOrderNos.substring(0, donumbers.length() - 1);
                }
                
                /*-----------------------------Delete Invoice--------------------------------*/
                    JSONObject deletejson = transactionService.jsonDeleteInvoice(invid, paramJobj);
                    JSONObject returnObj = transactionService.deleteInvoice(deletejson);
                    if (returnObj.has(Constants.RES_success) && returnObj.optBoolean(Constants.RES_success)) {
                        isSuccess = returnObj.optBoolean(Constants.RES_success);
                        
                        /*Building messages for deleted records after successfully delete of Transactions*/
                        if (!StringUtil.isNullOrEmpty(invoicenumber)) {
                            response.put("invoicenos", invoicenumber);
                            
                            //Invoice Nos delete message build 
                            msgString.append("Invoice Number : "+invoicenumber+" "); 
                            //Delivery Order Nos delete message build 
                            if (!StringUtil.isNullOrEmpty(donumbers)) {
                                msgString.append(",Delivery Order Numbers : " + donumbers + " ");
                                response.put("deliveryordernos", donumbers);
                            }
                            //Receipt Numbers delete message build 
                            if (!StringUtil.isNullOrEmpty(receiptnumbers)) {
                               msgString.append(" and Receipt Numbers : "+receiptnumbers +" "); 
                               response.put("receiptnos", receiptnumbers);
                            }
                            msgString.append(messageSource.getMessage("acc.RestSerivce.deleteInvoiceDOandRPMsg", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                            message = msgString.toString();
                        }
                       
                    } else if (returnObj.has(Constants.RES_MESSAGE) && !StringUtil.isNullOrEmpty(returnObj.optString(Constants.RES_MESSAGE,null))){
                         message = returnObj.optString(Constants.RES_MESSAGE);
                    }else {
                        message = messageSource.getMessage("acc.common.msg1", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                    }
            }
        } catch (Exception e) {
            //Active Date Range Period
            message=e.getMessage();
            throw ServiceException.FAILURE(e.getMessage(), e);
        } finally {
            response.put(Constants.RES_success, isSuccess);
            response.put(Constants.RES_MESSAGE, message);
        }
        return response;
    }  
    
}
