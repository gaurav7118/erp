/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.hql.accounting.InvoiceDocuments;
import com.krawler.hql.accounting.InvoiceDocumentCompMap;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;

/**
 *
 * @author krawler
 */
public class AccContractManagementServiceImplCMN implements AccContractManagementServiceDAOCMN {

    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private AccContractManagementDAO accContractManagementDAOObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccContractManagementServiceDAO accContractManagementServiceDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private accAccountDAO accAccountDAOobj;

    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setAccContractManagementDAOObj(AccContractManagementDAO accContractManagementDAOObj) {
        this.accContractManagementDAOObj = accContractManagementDAOObj;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public accCompanyPreferencesDAO getAccCompanyPreferencesObj() {
        return accCompanyPreferencesObj;
    }

    public void setAccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public AccContractManagementServiceDAO getAccContractManagementServiceDAOObj() {
        return accContractManagementServiceDAOObj;
    }

    public void setAccContractManagementServiceDAOObj(AccContractManagementServiceDAO accContractManagementServiceDAOObj) {
        this.accContractManagementServiceDAOObj = accContractManagementServiceDAOObj;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    @Override
    public JSONObject getMasterContractRows(Map<String, Object> requestParams) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        String[] sos = null;
        try {
            String companyid = (String) requestParams.get("companyid");
            String gcurrencyid = (String) requestParams.get("gcurrencyid");
            DateFormat df = (DateFormat) requestParams.get("dataFormatValue");
            String description = "";
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), (String) requestParams.get("gcurrencyid"));
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
            String closeflag = (String) requestParams.get("closeflag");
            boolean soflag = requestParams.containsKey("sopolinkflag") && requestParams.get("sopolinkflag") == null ? false : Boolean.FALSE.parseBoolean((String) requestParams.get("sopolinkflag"));

            boolean isForLinking = false;
            if (requestParams.containsKey("isForLinking") && requestParams.get("isForLinking") != null) {
                isForLinking = (Boolean) requestParams.get("isForLinking");
            }

            KwlReturnObject cpresult = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cpresult.getEntityList().get(0);

            KwlReturnObject extracapresult = accountingHandlerDAOobj.loadObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            Map<String, Object> ProductFieldsRequestParams = new HashMap();
            ProductFieldsRequestParams.put("companyid", companyid);
            ProductFieldsRequestParams.put("moduleid", Constants.Acc_Customer_Quotation_ModuleId);

            List masterFieldsResultList = CommonFunctions.getproductmastersFieldsToShowLineLevel(ProductFieldsRequestParams, accountingHandlerDAOobj);
            if (!StringUtil.isNullOrEmpty((String) requestParams.get("bills"))) {
                sos = (String[]) ((String) requestParams.get("bills")).split(",");
            }
            String dType = (String) requestParams.get("dtype");
            boolean isOrder = (Boolean) requestParams.get("isOrder");
            boolean isReport = false;
            boolean customIsReport = false;
            boolean isExport = false;
            if (!StringUtil.isNullOrEmpty(dType) && StringUtil.equal(dType, "report")) {
                isReport = true;
                customIsReport = true;
            }
            if (!StringUtil.isNullOrEmpty((String) requestParams.get("copyInvoice")) && Boolean.parseBoolean((String) requestParams.get("copyInvoice"))) {
                isReport = true;
            }
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) {
                isExport = (boolean) requestParams.get("isExport");
            }
            int i = 0;
            JSONArray jArr = new JSONArray();
            double addobj = 1;

//            HashMap<String, Object> fieldrequestParams = new HashMap();
//            HashMap<String, String> customFieldMap = new HashMap<String, String>();
//            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
//            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
//            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, isLeaseFixedAsset?Constants.Acc_Lease_Quotation:Constants.Acc_Customer_Quotation_ModuleId, 1));
//            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
//            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            HashMap<String, Object> detailRequestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("mrpcontract.ID");
            order_by.add("srno");
            order_type.add("asc");
            detailRequestParams.put("filter_names", filter_names);
            detailRequestParams.put("filter_params", filter_params);
            detailRequestParams.put("order_by", order_by);
            detailRequestParams.put("order_type", order_type);

            while (sos != null && i < sos.length) {
                KwlReturnObject result = accountingHandlerDAOobj.getObject(MRPContract.class.getName(), sos[i]);
                MRPContract mrpContract = (MRPContract) result.getEntityList().get(0);
                filter_params.clear();
                filter_params.add(mrpContract.getID());
                KwlReturnObject podresult = accContractManagementDAOObj.getMRPContractDetails(detailRequestParams);
                Iterator itr = podresult.getEntityList().iterator();
                while (itr.hasNext()) {
                    MRPContractDetails row = (MRPContractDetails) itr.next();
                    JSONObject obj = new JSONObject();
                    Product rowProduct = row.getProduct();

                    CommonFunctions.getterMethodForProductsData(row.getProduct(), masterFieldsResultList, obj);
                    obj.put("billid", mrpContract.getID());
                    obj.put("billno", mrpContract.getContractid());
                    obj.put("srno", row.getSrno());
                    obj.put("rowid", row.getID());
                    obj.put("originalTransactionRowid", row.getID());
                    obj.put("productid", rowProduct.getID());
                    obj.put("productname", rowProduct.getName());
                    String uom = row.getUom() != null ? row.getUom().getNameEmptyforNA() : rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA();
                    obj.put("unitname", uom);
                    obj.put("uomname", uom);
                    obj.put("baseuomname", rowProduct.getUnitOfMeasure() == null ? "" : rowProduct.getUnitOfMeasure().getNameEmptyforNA());
                    obj.put("multiuom", rowProduct.isMultiuom());

                    if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                        description = row.getDescription();
                    } else if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                        description = row.getProduct().getDescription();
                    } else {
                        description = "";
                    }
                    obj.put("desc", StringUtil.DecodeText(description));
                    if (rowProduct.isAsset()) {  //For Fixed Asset Group, type will be "Asset"
                        obj.put("type", "Asset");
                    } else {
                        obj.put("type", rowProduct.getProducttype() == null ? "" : rowProduct.getProducttype().getName());
                    }
                    obj.put("pid", rowProduct.getProductid());
                    obj.put("memo", row.getDescription());



                    if (extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == Constants.PackagingUOM && rowProduct != null) {
                        Product product = rowProduct;
                        obj.put("caseuom", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUoM().getID() : "");
                        obj.put("caseuomvalue", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUomValue() : 1);
                        obj.put("inneruom", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUoM().getID() : "");
                        obj.put("inneruomvalue", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUomValue() : 1);
                        obj.put("stockuom", (product.getUnitOfMeasure() != null) ? product.getUnitOfMeasure().getID() : "");
                    }


//                    if (row.getVendorquotationdetails() != null) {
//                        KwlReturnObject vqdetailsresult = accSalesOrderDAOobj.getVendorQuotationDetails(row.getVendorquotationdetails(), companyid);
//                        if (!vqdetailsresult.getEntityList().isEmpty()) {
//                            Object vq[] = (Object[]) vqdetailsresult.getEntityList().get(0);
//                            obj.put("linkto", vq[1]);
//                            obj.put("linkid", vq[0]);
//                            obj.put("rowid", row.getVendorquotationdetails());
//                            obj.put("savedrowid", row.getID());
//                            obj.put("linktype", 0);
//                        }
//                    } 




//                    Map<String, Object> variableMap = new HashMap<String, Object>();
//                    QuotationDetailCustomData quotationDetailCustomData = (QuotationDetailCustomData) row.getQuotationDetailCustomData();
//                    AccountingManager.setCustomColumnValues(quotationDetailCustomData, FieldMap, replaceFieldMap, variableMap);
//                    if (quotationDetailCustomData != null) {
//                        JSONObject params = new JSONObject();
////                        boolean isExport = false;
////                        if (customIsReport) {
////                            isExport = true;
////                        }
//                        if (requestParams.get("sopolinkflag") != null) {
//                            isOrder = Boolean.FALSE.parseBoolean(requestParams.get("sopolinkflag").toString());
//                        }
//                        params.put("isExport", isExport);
//                        if ((isForLinking || isOrder)) {
//                            int moduleId = isOrder ? Constants.Acc_Sales_Order_ModuleId : Constants.Acc_Invoice_ModuleId;
//                            params.put("linkModuleId", moduleId);
//                            params.put("isLink", isForLinking);
//                            params.put("companyid", companyid);
//                        }
//                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
//                    }




//                    KwlReturnObject bAmt = accCurrencyobj.getBaseToCurrencyAmount(requestParams, row.getRate(), currency.getCurrencyID(), so.getQuotationDate(), 0);
//                    
//                    // Get Product level Custom field data
//                    HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
//                    HashMap<String, String> customProductFieldMap = new HashMap<String, String>();
//                    HashMap<String, String> customProductDateFieldMap = new HashMap<String, String>();
//                    Map<String, Object> variableMapProduct = new HashMap<String, Object>();
//                    fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
//                    fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
//                    HashMap<String, String> replaceFieldMapProduct = new HashMap<String, String>();
//                    HashMap<String, Integer> FieldMapProduct = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParamsProduct, replaceFieldMapProduct, customProductFieldMap, customProductDateFieldMap);
//                    QuotationDetailsProductCustomData quotationDetailProductCustomData = (QuotationDetailsProductCustomData) row.getQuotationDetailProductCustomData();
//                    AccountingManager.setCustomColumnValues(quotationDetailProductCustomData, FieldMapProduct, replaceFieldMapProduct, variableMapProduct);
//                    
//                    if (quotationDetailProductCustomData != null) {
//                        JSONObject params = new JSONObject();
////                        boolean isExport = false;
////                        if (customIsReport) {
////                            isExport = true;
////                        }
//                        if (isForLinking) {
//                            isExport = false;
//                        }
//                        params.put("isExport", isExport);
//                        fieldDataManagercntrl.getLineLevelCustomData(variableMapProduct, customProductFieldMap, customProductDateFieldMap, obj, params);
//                    }
                    
//                    obj.put("orderrate", row.getRate());//obj.put("orderrate", (Double) bAmt.getEntityList().get(0));
                    double baseuomrate = row.getBaseuomrate();
                    double quantity = 0;
                    double invoiceRowProductQty = authHandler.calculateBaseUOMQuatity(row.getQuantity(), baseuomrate, companyid);
                    double remainedQty = invoiceRowProductQty;// which has not been linked yet
//                    if (closeflag != null) {
////                        addobj = (soflag ||isLeaseFixedAsset) ? getQuotationDetailStatusSO(row) : getQuotationDetailStatusINV(row);
//                        quantity = row.getQuantity();
//                        obj.put("quantity", addobj);
//                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(addobj, baseuomrate));
//                        remainedQty = authHandler.calculateBaseUOMQuatity(addobj,baseuomrate);
//
//                    } else {
                    quantity = row.getQuantity();
                    obj.put("quantity", quantity);
                    obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, baseuomrate, companyid));

//                    }

                    if (row.getUom() != null) {
                        obj.put("uomid", row.getUom().getID());
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());
                    } else {
                        obj.put("uomid", rowProduct.getUnitOfMeasure() != null ? rowProduct.getUnitOfMeasure().getID() : "");
                        obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity, row.getBaseuomrate(), companyid));
                        obj.put("baseuomrate", row.getBaseuomrate());
                    }
                    if (addobj > 0) {
                        jArr.put(obj);
                    }
                }
                i++;
                jobj.put("data", jArr);
            }
//        } catch (ParseException ex) {
//            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (JSONException je) {
            throw ServiceException.FAILURE(je.getMessage(), je);
        }/* catch (UnsupportedEncodingException ex) {
            Logger.getLogger(AccContractManagementServiceImplCMN.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return jobj;
    }
    
    @Override
    public int saveFileMapping(Map<String, Object> filemap) throws ServiceException {
        int count = 0;
        try {
            count = accContractManagementDAOObj.saveFileMapping(filemap);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("saveFileMapping : " + ex.getMessage(), ex);
        }
        return count;
    }
    
    @Override
    public JSONObject saveMasterContract(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject returnResult = null;
        JSONObject jobj = new JSONObject();
        MRPContract mrpContract = null;
        MRPContractDetails mrpContractDetails = null;
        try {
            Map<String, Object> reqParams = new HashMap<>();
            String companyid = "";
            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = requestParams.get("companyid").toString();
            }
            reqParams.put("companyid", companyid);
            
            if(requestParams.containsKey(Constants.df) && requestParams.get(Constants.df)!=null){
                reqParams.put(Constants.df, requestParams.get(Constants.df));
            }

            Locale locale = null;
            if (requestParams.containsKey("locale") && requestParams.get("locale") != null) {
                locale = (Locale) requestParams.get("locale");
            }
            reqParams.put("locale", locale);
            
            String mastercontractid = "";
            if (requestParams.containsKey("mastercontractid") && requestParams.get("mastercontractid") != null) {
                mastercontractid = requestParams.get("mastercontractid").toString();
            }
            reqParams.put("mastercontractid", mastercontractid);

            JSONObject dataJobj = new JSONObject(StringUtil.DecodeText(requestParams.get("contractdetailsdata").toString()));
            if(dataJobj.has("contractDetailsJsonObject") && dataJobj.getJSONArray("contractDetailsJsonObject")!=null){
                JSONArray dataArr = dataJobj.getJSONArray("contractDetailsJsonObject");
                for (int recCount = 0; recCount < dataArr.length(); recCount++) {
                    JSONObject dataObj = dataArr.getJSONObject(recCount);
                    //Put Contract Details data in map
                    getContractParameters(reqParams, dataObj);
                }
            }

            JSONObject paymentTermsDataObj = new JSONObject(StringUtil.DecodeText(requestParams.get("paymenttermsdata").toString()));
            if(paymentTermsDataObj.has("paymentTermsObject") &&  paymentTermsDataObj.getJSONArray("paymentTermsObject")!=null){
                JSONArray paymentTermsDataArr = paymentTermsDataObj.getJSONArray("paymentTermsObject");
                for (int recCount = 0; recCount < paymentTermsDataArr.length(); recCount++) {
                    JSONObject dataObj = paymentTermsDataArr.getJSONObject(recCount);
                    //Put Payment Terms data in map
                    getPaymentTermsParameters(reqParams, dataObj);
                }
            }

            JSONObject documentRequiredDataObj = new JSONObject(StringUtil.DecodeText(requestParams.get("documentrequireddata").toString()));
            if(documentRequiredDataObj.has("documentRequiredObject") && documentRequiredDataObj.getJSONArray("documentRequiredObject")!=null){
                JSONArray documentRequiredDataArr = documentRequiredDataObj.getJSONArray("documentRequiredObject");
                for (int recCount = 0; recCount < documentRequiredDataArr.length(); recCount++) {
                    JSONObject dataObj = documentRequiredDataArr.getJSONObject(recCount);
                    //Put Document Required data in map
                    getDocumentRequiredParameters(reqParams, dataObj);
                }
            }

            //Save global data for Master Contract
            returnResult = accContractManagementDAOObj.saveMasterContract(reqParams);
            List list = returnResult.getEntityList();
            if (list != null) {
                mrpContract = (MRPContract) list.get(0);
                
                Map<String, Object> params = new HashMap<>();
                
                params.clear();
                params.put("mrpContractID", mrpContract.getID());
                params.put("companyid", companyid);
                //Delete Master Contract records from all tables
                accContractManagementServiceDAOObj.deleteMasterContractData(params);
                
                /*
                 Save Global level Custom Field Data
                 */
                String customfield = (String) requestParams.get("customfield");
                if (!StringUtil.isNullOrEmpty(customfield)) {

                    JSONArray jcustomarray = new JSONArray(customfield);
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", jcustomarray);
                    customrequestParams.put("modulename", Constants.Acc_MRPMasterContract_Modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_MRPMasterContract_Id);
                    customrequestParams.put("modulerecid", mrpContract.getID());
                    customrequestParams.put("moduleid", Constants.MRP_Contract);
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("customdataclasspath", Constants.Acc_MRPMasterContract_CustomData_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        reqParams.put("accmrpcontractcustomdataref", mrpContract.getID());
                        reqParams.put("mastercontractid", mrpContract.getID());
                        returnResult = accContractManagementDAOObj.saveMasterContract(reqParams);
                    }
                }
                
                JSONObject billingContractDataObj = new JSONObject(StringUtil.DecodeText(requestParams.get("billingcontractdata").toString()));
                if(billingContractDataObj.has("billingContractObject") && billingContractDataObj.getJSONArray("billingContractObject")!=null){
                    JSONArray billingContractDataArr = billingContractDataObj.getJSONArray("billingContractObject");
                    for (int recCount = 0; recCount < billingContractDataArr.length(); recCount++) {
                        JSONObject dataObj = billingContractDataArr.getJSONObject(recCount);
                        reqParams.put("mrpContractID", mrpContract.getID());
                        //Put billing contract data in map
                        getBillingContractParameters(reqParams, dataObj);
                    }
                    //Save billling contract data
                    accContractManagementDAOObj.saveContractAddressDetails(reqParams);
                }
                    
                String parentcontractid[] = null;
                if(reqParams.containsKey("parentcontractid") && reqParams.get("parentcontractid")!=null){
                    parentcontractid = reqParams.get("parentcontractid").toString().split(",");
                    for(String parentcontract : parentcontractid){
                        params.clear();
                        params.put("mrpContractID", mrpContract.getID());
                        params.put("parentcontractid", parentcontract);
                        //Save parent mapping for master contract
                        accContractManagementDAOObj.saveContractMapping(params);
                    }
                }

                KwlReturnObject kmsg = null;
                JSONObject detailsObj = new JSONObject(StringUtil.DecodeText(requestParams.get("details").toString()));
                if(detailsObj.has("detailsObject") && detailsObj.getJSONArray("detailsObject")!=null){
                    JSONArray detailsArr = detailsObj.getJSONArray("detailsObject");
                    for (int recCount = 0; recCount < detailsArr.length(); recCount++) {

                        JSONObject dataObj = detailsArr.getJSONObject(recCount);
                        params.clear();
                        params.put("mrpContractID", mrpContract.getID());
                        params.put("companyid", companyid);
                        //Put product grid data in map
                        getContractDetailsParameters(params, dataObj);
                        //Save Product grid data
                        kmsg = accContractManagementDAOObj.saveContractDetails(params);
                        List ll = kmsg.getEntityList();
                        if (ll != null) {
                            mrpContractDetails = (MRPContractDetails) ll.get(0);
                            if(dataObj.has("shippingaddress") && dataObj.getJSONArray("shippingaddress")!=null){
                                JSONArray shippingAddressArray = dataObj.getJSONArray("shippingaddress");
                                for (int count = 0; count < shippingAddressArray.length(); count++) {
                                    JSONObject shippingAddressObj = shippingAddressArray.getJSONObject(count);
                                    Map<String, Object> shippingaddressparams = new HashMap<>();
                                    shippingaddressparams.put("mrpContractDetailsID", mrpContractDetails.getID());
                                    shippingaddressparams.put("companyid", companyid);
                                    //Put shipping address data in map
                                    getShippingAddressParameters(shippingaddressparams, shippingAddressObj);
                                    //Save shipping address data for product
                                    accContractManagementDAOObj.saveContractAddressDetails(shippingaddressparams);
                                }
                            }
                            
                            /*
                             Save line level Custom Field Data
                             */
                            customfield = (String) params.get("customfield");
                            if (!StringUtil.isNullOrEmpty(customfield)) {
                                JSONArray jcustomarray = new JSONArray(customfield);
                                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                                customrequestParams.put("customarray", jcustomarray);
                                customrequestParams.put("modulename", Constants.Acc_MRPMasterContractDetails_Modulename);
                                customrequestParams.put("moduleprimarykey", Constants.Acc_MRPMasterContractDetails_Id);
                                customrequestParams.put("modulerecid", mrpContractDetails.getID());
                                customrequestParams.put("moduleid", Constants.MRP_Contract);
                                customrequestParams.put("companyid", companyid);
                                customrequestParams.put("customdataclasspath", Constants.Acc_MRPMasterContractDetails_CustomData_classpath);
                                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    reqParams.put("accmrpcontractdetailscustomdataref", mrpContractDetails.getID());
                                    reqParams.put("contractdetailsid", mrpContractDetails.getID());
                                    returnResult = accContractManagementDAOObj.saveContractDetails(reqParams);
                                }
                            }
                        }
                    }
                }
                
                if (reqParams.containsKey("savedFilesMappingId")) {
                    String savedFilesMappingId = (String) reqParams.get("savedFilesMappingId");
                    Map<String, Object> fileMap = new HashMap<>();
                    fileMap.put("id", savedFilesMappingId);
                    fileMap.put("companyid", companyid);
                    KwlReturnObject mappedFilesResult = accContractManagementDAOObj.getMappedFilesResult(fileMap);
                    List mappedFiles = mappedFilesResult.getEntityList();
                    Iterator itr = mappedFiles.iterator();
                    KwlReturnObject objectResult = null;
                    while (itr.hasNext()) {
                        Object[] row = (Object[]) itr.next();
                        String id = (String) row[0];
                        String documentId = (String) row[1];
                        objectResult = accountingHandlerDAOobj.getObject(InvoiceDocuments.class.getName(), documentId);
                        KwlReturnObject cmp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                        Company company = (Company) cmp.getEntityList().get(0);
                        InvoiceDocuments documents = (InvoiceDocuments) objectResult.getEntityList().get(0);
                        InvoiceDocumentCompMap contractDocumentsMapping = new InvoiceDocumentCompMap();
                        contractDocumentsMapping.setInvoiceID(mrpContract.getID());
                        contractDocumentsMapping.setCompany(company);
                        contractDocumentsMapping.setDocument(documents);
                        accContractManagementDAOObj.SaveUpdateObject(contractDocumentsMapping);
                    }
                    accContractManagementDAOObj.deleteTemporaryMappedFiles(savedFilesMappingId, companyid);
                }
                
                jobj.put("msg", "Contract - " + mrpContract.getID() + " saved successfully.");
            }
        } catch (JSONException | AccountingException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jobj;
    }
    
    public void getContractParameters(Map<String, Object> requestParams, JSONObject jobj) throws JSONException, ServiceException, AccountingException {
        String sequenceformat = "", nextAutoNumber = "", contractid = " ", id = "", companyid = "";
        boolean isMasterContractIDPresent = false;
        try {
            DateFormat df =null;
            if (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) {
                df=(DateFormat)requestParams.get(Constants.df);
            }
            String mastercontractid = "";
            if (requestParams.containsKey("mastercontractid") && requestParams.get("mastercontractid") != null) {
                mastercontractid = requestParams.get("mastercontractid").toString();
            }
            
            Locale locale = null;
            if (requestParams.containsKey("locale") && requestParams.get("locale") != null) {
                locale = (Locale) requestParams.get("locale");
            }

            if (jobj.has("sequenceformat") && !StringUtil.isNullOrEmpty(jobj.getString("sequenceformat"))) {
                sequenceformat = jobj.getString("sequenceformat");
            }

            if (jobj.has("contractid") && !StringUtil.isNullOrEmpty(jobj.getString("contractid"))) {
                contractid = jobj.getString("contractid");
                requestParams.put("contractid", contractid);
            }

            if (requestParams.containsKey("companyid") && requestParams.get("companyid") != null) {
                companyid = (String) requestParams.get("companyid");
            }

            Date creationDate = null;
            if (jobj.has("creationdate") && !StringUtil.isNullOrEmpty(jobj.getString("creationdate")) && df != null) {
                String CMCreationDate = jobj.getString("creationdate");
                try {
                    creationDate = df.parse(CMCreationDate);
                } catch (ParseException ex) {
                    creationDate = null;
                }
            }
            /*
             * Sequence Format Code.
             */
            synchronized (this) {
                isMasterContractIDPresent = isMasterContractIDAlreadyPresent(requestParams);
                if (isMasterContractIDPresent) {
                    if (StringUtil.isNullOrEmpty(mastercontractid)) {
                        if (sequenceformat.equals("NA")) {
                            throw new AccountingException(messageSource.getMessage("acc.mastercontract.field.contractid", null, locale) + " " + contractid + " " + messageSource.getMessage("acc.field.alreadyexists.", null, locale));
                        }
                    } else {
                        nextAutoNumber = contractid;
                        requestParams.put("mastercontractid", mastercontractid);
                    }
                } else {
                    boolean seqformat_oldflag = StringUtil.getBoolean(jobj.getString("seqformat_oldflag"));
                    String nextAutoNoInt = "";
                    String datePrefix = "";
                    String dateSuffix = "";
                    if (!sequenceformat.equals("NA")) {
                        if (seqformat_oldflag) {
                            nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_MRPCONTRACT, sequenceformat);
                        } else {
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_MRPCONTRACT, sequenceformat, seqformat_oldflag,creationDate);
                            nextAutoNumber = (String) seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                            nextAutoNoInt = (String) seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                            datePrefix = (String) seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                            dateSuffix = (String) seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                            requestParams.put(Constants.SEQFORMAT, sequenceformat);
                            requestParams.put(Constants.SEQNUMBER, nextAutoNoInt);
                            requestParams.put(Constants.DATEPREFIX, datePrefix);
                            requestParams.put(Constants.DATESUFFIX, dateSuffix);
                        }
                        contractid = nextAutoNumber;
                    }
                }

                if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                    List list = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.MRP_Contract, contractid, companyid);
                    if (!list.isEmpty()) {
                        boolean isvalidEntryNumber = (Boolean) list.get(0);
                        String formatName = (String) list.get(1);
                        if (!isvalidEntryNumber) {
                            throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, locale) + " <b>" + contractid + "</b> " + messageSource.getMessage("acc.common.belongsto", null, locale) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, locale) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, locale));
                        }
                    }
                }

            }


            if (!StringUtil.isNullOrEmpty(contractid)) {
//                requestParams.remove("contractid");
                requestParams.put("contractid", contractid);
                requestParams.put("autogen", nextAutoNumber.equals(contractid));
            }
            if (jobj.has("contractname") && !StringUtil.isNullOrEmpty(jobj.getString("contractname"))) {
                requestParams.put("contractname", jobj.getString("contractname"));
            }
            if (jobj.has("customer") && !StringUtil.isNullOrEmpty(jobj.getString("customer"))) {
                requestParams.put("customer", jobj.getString("customer"));
            }
            if (jobj.has("sellertype") && !StringUtil.isNullOrEmpty(jobj.getString("sellertype"))) {
                requestParams.put("sellertype", jobj.getString("sellertype"));
            }
            if (jobj.has("contractterm") && !StringUtil.isNullOrEmpty(jobj.getString("contractterm"))) {
                requestParams.put("contractterm", jobj.getString("contractterm"));
            }
            if (jobj.has("contractstatus") && !StringUtil.isNullOrEmpty(jobj.getString("contractstatus"))) {
                requestParams.put("contractstatus", jobj.getString("contractstatus"));
            }
            if (jobj.has("parentcontractid") && !StringUtil.isNullOrEmpty(jobj.getString("parentcontractid"))) {
                requestParams.put("parentcontractid", jobj.getString("parentcontractid"));
            }
            if (jobj.has("parentcontractname") && !StringUtil.isNullOrEmpty(jobj.getString("parentcontractname"))) {
                requestParams.put("parentcontractname", jobj.getString("parentcontractname"));
            }
            if (jobj.has("creationdate") && !StringUtil.isNullOrEmpty(jobj.getString("creationdate"))) {
                requestParams.put("creationdate", jobj.getString("creationdate"));
            }
            if (jobj.has("contractstartdate") && !StringUtil.isNullOrEmpty(jobj.getString("contractstartdate"))) {
                requestParams.put("contractstartdate", jobj.getString("contractstartdate"));
            }
            if (jobj.has("contractenddate") && !StringUtil.isNullOrEmpty(jobj.getString("contractenddate"))) {
                requestParams.put("contractenddate", jobj.getString("contractenddate"));
            }
        } catch (ServiceException | JSONException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isMasterContractIDAlreadyPresent(Map<String, Object> requestParams) throws ServiceException {
        boolean isMasterContractIDPresent = false;
        try {
            KwlReturnObject returnResult = accContractManagementDAOObj.isMasterContractIDAlreadyPresent(requestParams);
            if (returnResult.getRecordTotalCount() > 0) {
                isMasterContractIDPresent = true;
            }
        } catch (Exception ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return isMasterContractIDPresent;
    }

    public void getPaymentTermsParameters(Map<String, Object> requestParams, JSONObject jobj) throws JSONException {
        try {
            if (jobj.has("paymentmethodname") && !StringUtil.isNullOrEmpty(jobj.getString("paymentmethodname"))) {
                requestParams.put("paymentmethodname", jobj.getString("paymentmethodname"));
            }
            if (jobj.has("accountname") && !StringUtil.isNullOrEmpty(jobj.getString("accountname"))) {
                requestParams.put("accountname", jobj.getString("accountname"));
            }
            if (jobj.has("detailtype") && !StringUtil.isNullOrEmpty(jobj.getString("detailtype"))) {
                requestParams.put("detailtype", jobj.getString("detailtype"));
            }
            if (jobj.has("autopopulate") && !StringUtil.isNullOrEmpty(jobj.getString("autopopulate"))) {
                requestParams.put("autopopulate", jobj.getString("autopopulate"));
            }
            if (jobj.has("showincscp") && !StringUtil.isNullOrEmpty(jobj.getString("showincscp"))) {
                requestParams.put("showincscp", jobj.getString("showincscp"));
            }
            if (jobj.has("bankname") && !StringUtil.isNullOrEmpty(jobj.getString("bankname"))) {
                requestParams.put("bankname", jobj.getString("bankname"));
            }
            if (jobj.has("bankaccountnumber") && !StringUtil.isNullOrEmpty(jobj.getString("bankaccountnumber"))) {
                requestParams.put("bankaccountnumber", jobj.getString("bankaccountnumber"));
            }
            if (jobj.has("bankaddress") && !StringUtil.isNullOrEmpty(jobj.getString("bankaddress"))) {
                requestParams.put("bankaddress", jobj.getString("bankaddress"));
            }
            if (jobj.has("paymenttermname") && !StringUtil.isNullOrEmpty(jobj.getString("paymenttermname"))) {
                requestParams.put("paymenttermname", jobj.getString("paymenttermname"));
            }
            if (jobj.has("paymenttermdate") && !StringUtil.isNullOrEmpty(jobj.getString("paymenttermdate"))) {
                requestParams.put("paymenttermdate", jobj.getString("paymenttermdate"));
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getBillingContractParameters(Map<String, Object> requestParams, JSONObject jobj) throws JSONException {
        try {
            if (jobj.has("billingaddresscombo") && !StringUtil.isNullOrEmpty(jobj.getString("billingaddresscombo"))) {
                requestParams.put("addresscombo", jobj.getString("billingaddresscombo"));
            }
            if (jobj.has("billingaliasname") && !StringUtil.isNullOrEmpty(jobj.getString("billingaliasname"))) {
                requestParams.put("aliasname", jobj.getString("billingaliasname"));
            }
            if (jobj.has("billingaddress") && !StringUtil.isNullOrEmpty(jobj.getString("billingaddress"))) {
                requestParams.put("address", jobj.getString("billingaddress"));
            }
            if (jobj.has("billingcounty") && !StringUtil.isNullOrEmpty(jobj.getString("billingcounty"))) {
                requestParams.put("county", jobj.getString("billingcounty"));
            }
            if (jobj.has("billingcity") && !StringUtil.isNullOrEmpty(jobj.getString("billingcity"))) {
                requestParams.put("city", jobj.getString("billingcity"));
            }
            if (jobj.has("billingstate") && !StringUtil.isNullOrEmpty(jobj.getString("billingstate"))) {
                requestParams.put("state", jobj.getString("billingstate"));
            }
            if (jobj.has("billingcountry") && !StringUtil.isNullOrEmpty(jobj.getString("billingcountry"))) {
                requestParams.put("country", jobj.getString("billingcountry"));
            }
            if (jobj.has("billingpostal") && !StringUtil.isNullOrEmpty(jobj.getString("billingpostal"))) {
                requestParams.put("postal", jobj.getString("billingpostal"));
            }
            if (jobj.has("billingphone") && !StringUtil.isNullOrEmpty(jobj.getString("billingphone"))) {
                requestParams.put("phone", jobj.getString("billingphone"));
            }
            if (jobj.has("billingmobile") && !StringUtil.isNullOrEmpty(jobj.getString("billingmobile"))) {
                requestParams.put("mobile", jobj.getString("billingmobile"));
            }
            if (jobj.has("billingfax") && !StringUtil.isNullOrEmpty(jobj.getString("billingfax"))) {
                requestParams.put("fax", jobj.getString("billingfax"));
            }
            if (jobj.has("billingemail") && !StringUtil.isNullOrEmpty(jobj.getString("billingemail"))) {
                requestParams.put("email", jobj.getString("billingemail"));
            }
            if (jobj.has("billingrecipientname") && !StringUtil.isNullOrEmpty(jobj.getString("billingrecipientname"))) {
                requestParams.put("recipientname", jobj.getString("billingrecipientname"));
            }
            if (jobj.has("billingcontactperson") && !StringUtil.isNullOrEmpty(jobj.getString("billingcontactperson"))) {
                requestParams.put("contactperson", jobj.getString("billingcontactperson"));
            }
            if (jobj.has("billingcontactpersonnumber") && !StringUtil.isNullOrEmpty(jobj.getString("billingcontactpersonnumber"))) {
                requestParams.put("contactpersonnumber", jobj.getString("billingcontactpersonnumber"));
            }
            if (jobj.has("billingcontactpersondesignation") && !StringUtil.isNullOrEmpty(jobj.getString("billingcontactpersondesignation"))) {
                requestParams.put("contactpersondesignation", jobj.getString("billingcontactpersondesignation"));
            }
            if (jobj.has("billingwebsite") && !StringUtil.isNullOrEmpty(jobj.getString("billingwebsite"))) {
                requestParams.put("website", jobj.getString("billingwebsite"));
            }
            if (jobj.has("route") && !StringUtil.isNullOrEmpty(jobj.getString("route"))) {
                requestParams.put("route", jobj.getString("route"));
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getDocumentRequiredParameters(Map<String, Object> requestParams, JSONObject jobj) throws JSONException {
        try {
            if (jobj.has("customerName") && !StringUtil.isNullOrEmpty(jobj.getString("customerName"))) {
                requestParams.put("customerName", jobj.getString("customerName"));
            }
            if (jobj.has("contractorTeeName") && !StringUtil.isNullOrEmpty(jobj.getString("contractorTeeName"))) {
                requestParams.put("contractorTeeName", jobj.getString("contractorTeeName"));
            }
            if (jobj.has("PANNumber") && !StringUtil.isNullOrEmpty(jobj.getString("PANNumber"))) {
                requestParams.put("PANNumber", jobj.getString("PANNumber"));
            }
            if (jobj.has("TANNumber") && !StringUtil.isNullOrEmpty(jobj.getString("TANNumber"))) {
                requestParams.put("TANNumber", jobj.getString("TANNumber"));
            }
            if (jobj.has("dateOfAggrement") && !StringUtil.isNullOrEmpty(jobj.getString("dateOfAggrement"))) {
                requestParams.put("dateOfAggrement", jobj.getString("dateOfAggrement"));
            }
            if (jobj.has("countryAggrement") && !StringUtil.isNullOrEmpty(jobj.getString("countryAggrement"))) {
                requestParams.put("countryAggrement", jobj.getString("countryAggrement"));
            }
            if (jobj.has("stateAggrement") && !StringUtil.isNullOrEmpty(jobj.getString("stateAggrement"))) {
                requestParams.put("stateAggrement", jobj.getString("stateAggrement"));
            }
            if (jobj.has("previousContractId") && !StringUtil.isNullOrEmpty(jobj.getString("previousContractId"))) {
                requestParams.put("previousContractId", jobj.getString("previousContractId"));
            }
            if (jobj.has("remarks") && !StringUtil.isNullOrEmpty(jobj.getString("remarks"))) {
                requestParams.put("remarks", jobj.getString("remarks"));
            }
            if (jobj.has("savedFilesMappingId") && !StringUtil.isNullOrEmpty(jobj.getString("savedFilesMappingId"))) {
                requestParams.put("savedFilesMappingId", jobj.getString("savedFilesMappingId"));
            }

        } catch (JSONException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getContractDetailsParameters(Map<String, Object> requestParams, JSONObject jobj) throws JSONException {
        try {
            if (jobj.has("productid") && !StringUtil.isNullOrEmpty(jobj.getString("productid"))) {
                requestParams.put("productid", jobj.getString("productid"));
            }
            if (jobj.has("quantity") && !StringUtil.isNullOrEmpty(jobj.getString("quantity"))) {
                requestParams.put("quantity", jobj.getString("quantity"));
            }
            if (jobj.has("desc") && !StringUtil.isNullOrEmpty(jobj.getString("desc"))) {
                requestParams.put("desc", jobj.getString("desc"));
            }
            if (jobj.has("uomid") && !StringUtil.isNullOrEmpty(jobj.getString("uomid"))) {
                requestParams.put("uomid", jobj.getString("uomid"));
            }
            if (jobj.has("baseuomrate") && !StringUtil.isNullOrEmpty(jobj.getString("baseuomrate"))) {
                requestParams.put("baseuomrate", jobj.getString("baseuomrate"));
            }
            if (jobj.has("baseuomquantity") && !StringUtil.isNullOrEmpty(jobj.getString("baseuomquantity"))) {
                requestParams.put("baseuomquantity", jobj.getString("baseuomquantity"));
            }
            if (jobj.has("rate") && !StringUtil.isNullOrEmpty(jobj.getString("rate"))) {
                requestParams.put("rate", jobj.getString("rate"));
            }
            if (jobj.has("discamount") && !StringUtil.isNullOrEmpty(jobj.getString("discamount"))) {
                requestParams.put("discamount", jobj.getString("discamount"));
            }


            if (jobj.has("deliverymode") && !StringUtil.isNullOrEmpty(jobj.getString("deliverymode"))) {
                requestParams.put("deliverymode", jobj.getString("deliverymode"));
            }
            if (jobj.has("totalnoofunit") && !StringUtil.isNullOrEmpty(jobj.getString("totalnoofunit"))) {
                requestParams.put("totalnoofunit", jobj.getString("totalnoofunit"));
            }
            if (jobj.has("totalquantity") && !StringUtil.isNullOrEmpty(jobj.getString("totalquantity"))) {
                requestParams.put("totalquantity", jobj.getString("totalquantity"));
            }
            if (jobj.has("shippingperiodfrom") && !StringUtil.isNullOrEmpty(jobj.getString("shippingperiodfrom"))) {
                requestParams.put("shippingperiodfrom", jobj.getString("shippingperiodfrom"));
            }
            if (jobj.has("shippingperiodto") && !StringUtil.isNullOrEmpty(jobj.getString("shippingperiodto"))) {
                requestParams.put("shippingperiodto", jobj.getString("shippingperiodto"));
            }
            if (jobj.has("partialshipmentallowed") && !StringUtil.isNullOrEmpty(jobj.getString("partialshipmentallowed"))) {
                requestParams.put("partialshipmentallowed", jobj.getString("partialshipmentallowed"));
            }
            if (jobj.has("shipmentstatus") && !StringUtil.isNullOrEmpty(jobj.getString("shipmentstatus"))) {
                requestParams.put("shipmentstatus", jobj.getString("shipmentstatus"));
            }
            if (jobj.has("shippingagent") && !StringUtil.isNullOrEmpty(jobj.getString("shippingagent"))) {
                requestParams.put("shippingagent", jobj.getString("shippingagent"));
            }
            if (jobj.has("loadingportcountry") && !StringUtil.isNullOrEmpty(jobj.getString("loadingportcountry"))) {
                requestParams.put("loadingportcountry", jobj.getString("loadingportcountry"));
            }
            if (jobj.has("loadingport") && !StringUtil.isNullOrEmpty(jobj.getString("loadingport"))) {
                requestParams.put("loadingport", jobj.getString("loadingport"));
            }
            if (jobj.has("transshipmentallowed") && !StringUtil.isNullOrEmpty(jobj.getString("transshipmentallowed"))) {
                requestParams.put("transshipmentallowed", jobj.getString("transshipmentallowed"));
            }
            if (jobj.has("dischargeportcountry") && !StringUtil.isNullOrEmpty(jobj.getString("dischargeportcountry"))) {
                requestParams.put("dischargeportcountry", jobj.getString("dischargeportcountry"));
            }
            if (jobj.has("dischargeport") && !StringUtil.isNullOrEmpty(jobj.getString("dischargeport"))) {
                requestParams.put("dischargeport", jobj.getString("dischargeport"));
            }
            if (jobj.has("finaldestination") && !StringUtil.isNullOrEmpty(jobj.getString("finaldestination"))) {
                requestParams.put("finaldestination", jobj.getString("finaldestination"));
            }
            if (jobj.has("postalcode") && !StringUtil.isNullOrEmpty(jobj.getString("postalcode"))) {
                requestParams.put("postalcode", jobj.getString("postalcode"));
            }
            if (jobj.has("budgetfreightcost") && !StringUtil.isNullOrEmpty(jobj.getString("budgetfreightcost"))) {
                requestParams.put("budgetfreightcost", jobj.getString("budgetfreightcost"));
            }
            if (jobj.has("shipmentcontratremarks") && !StringUtil.isNullOrEmpty(jobj.getString("shipmentcontratremarks"))) {
                requestParams.put("shipmentcontractremarks", jobj.getString("shipmentcontratremarks"));
            }


            if (jobj.has("unitweightvalue") && !StringUtil.isNullOrEmpty(jobj.getString("unitweightvalue"))) {
                requestParams.put("unitweightvalue", jobj.getString("unitweightvalue"));
            }
            if (jobj.has("unitweight") && !StringUtil.isNullOrEmpty(jobj.getString("unitweight"))) {
                requestParams.put("unitweight", jobj.getString("unitweight"));
            }
            if (jobj.has("packagingtype") && !StringUtil.isNullOrEmpty(jobj.getString("packagingtype"))) {
                requestParams.put("packagingtype", jobj.getString("packagingtype"));
            }
            if (jobj.has("certificaterequirement") && !StringUtil.isNullOrEmpty(jobj.getString("certificaterequirement"))) {
                requestParams.put("certificaterequirement", jobj.getString("certificaterequirement"));
            }
            if (jobj.has("certificate") && !StringUtil.isNullOrEmpty(jobj.getString("certificate"))) {
                requestParams.put("certificate", jobj.getString("certificate"));
            }
            if (jobj.has("shippingmarksdetails") && !StringUtil.isNullOrEmpty(jobj.getString("shippingmarksdetails"))) {
                requestParams.put("shippingmarksdetails", jobj.getString("shippingmarksdetails"));
            }
            if (jobj.has("shipmentmode") && !StringUtil.isNullOrEmpty(jobj.getString("shipmentmode"))) {
                requestParams.put("shipmentmode", jobj.getString("shipmentmode"));
            }
            if (jobj.has("percontainerload") && !StringUtil.isNullOrEmpty(jobj.getString("percontainerload"))) {
                requestParams.put("percontainerload", jobj.getString("percontainerload"));
            }
            if (jobj.has("palletmaterial") && !StringUtil.isNullOrEmpty(jobj.getString("palletmaterial"))) {
                requestParams.put("palletmaterial", jobj.getString("palletmaterial"));
            }
            if (jobj.has("packagingprofiletype") && !StringUtil.isNullOrEmpty(jobj.getString("packagingprofiletype"))) {
                requestParams.put("packagingprofiletype", jobj.getString("packagingprofiletype"));
            }
            if (jobj.has("marking") && !StringUtil.isNullOrEmpty(jobj.getString("marking"))) {
                requestParams.put("marking", jobj.getString("marking"));
            }
            if (jobj.has("drumorbagdetails") && !StringUtil.isNullOrEmpty(jobj.getString("drumorbagdetails"))) {
                requestParams.put("drumorbagdetails", jobj.getString("drumorbagdetails"));
            }
            if (jobj.has("drumorbagsize") && !StringUtil.isNullOrEmpty(jobj.getString("drumorbagsize"))) {
                requestParams.put("drumorbagsize", jobj.getString("drumorbagsize"));
            }
            if (jobj.has("numberoflayers") && !StringUtil.isNullOrEmpty(jobj.getString("numberoflayers"))) {
                requestParams.put("numberoflayers", jobj.getString("numberoflayers"));
            }
            if (jobj.has("heatingpad") && !StringUtil.isNullOrEmpty(jobj.getString("heatingpad"))) {
                requestParams.put("heatingpad", jobj.getString("heatingpad"));
            }
            if (jobj.has("palletloadcontainer") && !StringUtil.isNullOrEmpty(jobj.getString("palletloadcontainer"))) {
                requestParams.put("palletloadcontainer", jobj.getString("palletloadcontainer"));
            }
            if (jobj.has("customfield") && !StringUtil.isNullOrEmpty(jobj.getString("customfield"))) {
                requestParams.put("customfield", jobj.getString("customfield"));
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void getShippingAddressParameters(Map<String, Object> requestParams, JSONObject jobj) throws JSONException {
        try {
            if (jobj.has("shippingaddrscombo") && !StringUtil.isNullOrEmpty(jobj.getString("shippingaddrscombo"))) {
                requestParams.put("addresscombo", jobj.getString("shippingaddrscombo"));
            }
            if (jobj.has("shippingaliasname") && !StringUtil.isNullOrEmpty(jobj.getString("shippingaliasname"))) {
                requestParams.put("aliasname", jobj.getString("shippingaliasname"));
            }
            if (jobj.has("shippingaddress") && !StringUtil.isNullOrEmpty(jobj.getString("shippingaddress"))) {
                requestParams.put("address", jobj.getString("shippingaddress"));
            }
            if (jobj.has("shippingcity") && !StringUtil.isNullOrEmpty(jobj.getString("shippingcity"))) {
                requestParams.put("city", jobj.getString("shippingcity"));
            }
            if (jobj.has("shippingstate") && !StringUtil.isNullOrEmpty(jobj.getString("shippingstate"))) {
                requestParams.put("state", jobj.getString("shippingstate"));
            }
            if (jobj.has("shippingcountry") && !StringUtil.isNullOrEmpty(jobj.getString("shippingcountry"))) {
                requestParams.put("country", jobj.getString("shippingcountry"));
            }
            if (jobj.has("shippingpostalcode") && !StringUtil.isNullOrEmpty(jobj.getString("shippingpostalcode"))) {
                requestParams.put("postalcode", jobj.getString("shippingpostalcode"));
            }
            if (jobj.has("shippingphone") && !StringUtil.isNullOrEmpty(jobj.getString("shippingphone"))) {
                requestParams.put("phone", jobj.getString("shippingphone"));
            }
            if (jobj.has("shippingmobile") && !StringUtil.isNullOrEmpty(jobj.getString("shippingmobile"))) {
                requestParams.put("mobile", jobj.getString("shippingmobile"));
            }
            if (jobj.has("shippingfax") && !StringUtil.isNullOrEmpty(jobj.getString("shippingfax"))) {
                requestParams.put("fax", jobj.getString("shippingfax"));
            }
            if (jobj.has("shippingemail") && !StringUtil.isNullOrEmpty(jobj.getString("shippingemail"))) {
                requestParams.put("email", jobj.getString("shippingemail"));
            }
            if (jobj.has("shippingrecipientname") && !StringUtil.isNullOrEmpty(jobj.getString("shippingrecipientname"))) {
                requestParams.put("recipientname", jobj.getString("shippingrecipientname"));
            }
            if (jobj.has("shippingcontactperson") && !StringUtil.isNullOrEmpty(jobj.getString("shippingcontactperson"))) {
                requestParams.put("contactperson", jobj.getString("shippingcontactperson"));
            }
            if (jobj.has("shippingcontactpersonnumber") && !StringUtil.isNullOrEmpty(jobj.getString("shippingcontactpersonnumber"))) {
                requestParams.put("contactpersonnumber", jobj.getString("shippingcontactpersonnumber"));
            }
            if (jobj.has("shippingcontactcersondesignation") && !StringUtil.isNullOrEmpty(jobj.getString("shippingcontactcersondesignation"))) {
                requestParams.put("contactcersondesignation", jobj.getString("shippingcontactcersondesignation"));
            }
            if (jobj.has("shippingwebsite") && !StringUtil.isNullOrEmpty(jobj.getString("shippingwebsite"))) {
                requestParams.put("website", jobj.getString("shippingwebsite"));
            }
            if (jobj.has("shippingroute") && !StringUtil.isNullOrEmpty(jobj.getString("shippingroute"))) {
                requestParams.put("route", jobj.getString("shippingroute"));
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
