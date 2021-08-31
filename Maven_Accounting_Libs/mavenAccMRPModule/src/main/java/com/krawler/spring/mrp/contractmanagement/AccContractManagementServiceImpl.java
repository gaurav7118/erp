/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.mrp.contractmanagement;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.CustomizeReportMapping;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.StaticValues;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;

/**
 *
 * @author krawler
 */
public class AccContractManagementServiceImpl implements AccContractManagementServiceDAO {

    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private AccContractManagementDAO accContractManagementDAOObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private accAccountDAO accAccountDAOobj;
    private exportMPXDAOImpl exportDaoObj;

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

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
     public exportMPXDAOImpl getExportDaoObj() {
        return exportDaoObj;
    }

    public void setExportDaoObj(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public JSONObject getContractMasterDetails(Map<String, Object> requestParams) {

        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        Locale requestcontextutilsobj = null;

        try {

            int count = 0;//result.getRecordTotalCount();
            int colwidth = 150;
            if (requestParams.containsKey("requestcontextutilsobj")) {
                requestcontextutilsobj = (Locale) requestParams.get("requestcontextutilsobj");
            }

            // Column Model
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            String StoreRec ="";
            //Contract Details Record
            StoreRec+= "id,billid,contractname,contractid,customername,buyerid,creationdate,sellertype,contacttermid,contactterm,paymentterm,contractstatus,subcontractname,subcontractid,subcontract,contractstartdate,contractenddate,seqformat,customerid,sellertypeid,contractstatusid";
            //Billing Contract Record
            StoreRec+= ",billingaddresscombo,billingaliasname,billingaddress,billingcounty,billingcity,billingstate,billingcountry,billingpostal,billingphone,billingmobile"
                    + ",billingfax,billingemail,billingrecipientname,billingcontactperson,billingcontactpersonnumber,billingcontactpersondesignation,billingwebsite";
            //Payment Contract Record
            StoreRec+= ",paymentmethodid,paymentmethodname,accountname,detailtype,autopopulate,showincscp,bankname,bankaccountnumber,bankaddress,paymenttermid,paymenttermname,paymenttermdays,paymenttermdate";
            //Documents Required Record
            StoreRec+= ",contractorTeeName,PANNumber,TANNumber,dateOfAggrement,countryAggrement,countryAggrementname,stateAggrement,previousContractId,remarks,attachment,deleted";
            
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
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header1", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "contractname");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jobjTemp.put("sortable", true);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header2", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "contractid");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header3", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "customername");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header5", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "creationdate");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header6", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "sellertype");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.Lease.contractterm", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "contactterm");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header11", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "paymentterm");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header12", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "contractstatus");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header17", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "attachment");
            jobjTemp.put("width", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header13", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "subcontractname");
            jobjTemp.put("pdfwidth", 75); 
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header14", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "subcontract");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header15", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "contractstartdate");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.contractMasterGrid.header16", null, requestcontextutilsobj));
            jobjTemp.put("dataIndex", "contractenddate");
            jobjTemp.put("align", "center");
            jobjTemp.put("width", colwidth);
            jobjTemp.put("pdfwidth", 75);
            jarrColumns.put(jobjTemp);
            
            /*
             Add Custom Fields in Column Model
             */
            String companyId = (String) requestParams.get("companyid");
            requestParams.put("reportId", Constants.MRP_Contract);
            requestParams.put("companyId", companyId);
            putCustomColumnForMasterContract(jarrColumns, jarrRecords, requestParams);
            dataJArr = createMasterContractJSON(requestParams);

            commData.put("success", true);
            commData.put("coldata", dataJArr);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", count);
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);

            jobj.put("valid", true);
            if (false) {
                jobj.put("data", dataJArr);
            } else {
                jobj.put("data", commData);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jobj;
    }
    
    public void putCustomColumnForMasterContract(JSONArray jarrColumns, JSONArray jarrRecords, Map<String, Object> requestParams) throws ServiceException {
        try {
            HashMap<String, Object> requestParams1 = new HashMap<String, Object>(requestParams);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(requestParams1);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List arrayList = new ArrayList();
            for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                String column = "Custom_" + customizeReportMapping.getDataIndex();
                if (!arrayList.contains(customizeReportMapping.getDataIndex())) {
                    JSONObject jobjTemp = new JSONObject();
                    jobjTemp.put("name", column);
                    jarrRecords.put(jobjTemp);
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", customizeReportMapping.getDataHeader());
                    jobjTemp.put("dataIndex", column);
                    jobjTemp.put("width", 150);
                    jobjTemp.put("pdfwidth", 150);
                    jobjTemp.put("custom", "true");
                    jarrColumns.put(jobjTemp);
                    arrayList.add(customizeReportMapping.getDataIndex());
                }
            }
        } catch (ServiceException | JSONException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    @Override
    public void deleteMasterContractData(Map<String, Object> params){
        Map<String, Object> requestParams = new HashMap();
        try {
            //Delete parent mapping data for master contract if exists
            accContractManagementDAOObj.deleteMasterContractMapping(params);
            
            //Delete mastercontractdocument mapping data for master contract if exists
            accContractManagementDAOObj.deleteMasterContractDocumentMapping(params);
            
            //Delete shipping contract & billing contract data for master contract if exists
            accContractManagementDAOObj.deleteBillingAddressDetails(params);

            //Delete CustomData for master contract if exists
            accContractManagementDAOObj.deleteMasterContractCustomData(params);

            //Delete MasterContractDetailsCustomData for master contract if exists
            accContractManagementDAOObj.deleteMasterContractDetailsCustomData(params);
            
            if(params.containsKey("mrpContractID")){
                requestParams.put("mrpcontractid", (String) params.get("mrpContractID"));
            }
            KwlReturnObject result = accContractManagementDAOObj.getMasterContractRows(requestParams);
            List<MRPContractDetails> list = result.getEntityList();
            if (list.size() > 0) {
                for (MRPContractDetails mrpContractDetails : list) {
                    requestParams.clear();
                    requestParams.put("mrpContractDetailsID", mrpContractDetails.getID());
                    accContractManagementDAOObj.deleteShippingAddressDetails(requestParams);
                }
            }
            
            //Delete product grid data for master contract
            accContractManagementDAOObj.deleteMasterContractDetails(params);
            
        } catch (ServiceException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public JSONObject getTemporarySavedFiles(Map<String, Object> requestParams) {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject kmsg = accContractManagementDAOObj.getTemporarySavedFiles(requestParams);
            List ll = kmsg.getEntityList();

            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                JSONObject temp = new JSONObject();
                Object[] row = (Object[]) ite.next();
                temp.put("id", row[0]);
                temp.put("filename", row[1]);
                temp.put("imgname", row[1]);
                jobj.append("data", temp);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    /**
     *
     * @param requestParams
     * @return
     * @throws ServiceException
     */
    @Override
    public JSONObject getMasterContracts(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject result = accContractManagementDAOObj.getMasterContracts(requestParams);
            JSONArray jarr = getMasterContractJsonforRequest(result.getEntityList(), requestParams);
            jobj.put("data", jarr);
            jobj.put("count", result.getRecordTotalCount());

        } catch (NumberFormatException | JSONException ex) {
            throw ServiceException.FAILURE("getMasterContractsJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }
    
    @Override
    public JSONArray getMasterContractLinkingInformation(Map<String, Object> requestParams,JSONArray jArr) throws ServiceException {
        try {
                DateFormat df = (DateFormat) requestParams.get("df");
                KwlReturnObject result = accContractManagementDAOObj.getMRPContractLinkedinSO(requestParams);
                List listso1 = result.getEntityList();
                Iterator itrso = listso1.iterator();
                while (itrso.hasNext()) {
                    Object[] oj = (Object[]) itrso.next();
                    String orderid = oj[0].toString();
                    BigInteger type = (BigInteger) oj[1];                    
                    JSONObject obj = new JSONObject();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(MRPContract.class.getName(), orderid);
                    MRPContract mRPContract = (MRPContract) objItr.getEntityList().get(0);
                    Customer customer = mRPContract.getCustomer();
                    if (mRPContract != null && customer != null) {
                        obj.put("billid", mRPContract.getID());
                        obj.put("companyid", mRPContract.getCompany().getCompanyID());
                        obj.put("companyname", mRPContract.getCompany().getCompanyName());
                        obj.put("withoutinventory", false);
                        obj.put("personid", customer.getID());
                        obj.put("transactionNo", mRPContract.getContractid());
                        obj.put("date", df.format(mRPContract.getCreationdate()));
                        obj.put("personname", customer.getName());
                        obj.put("aliasname", customer.getAliasname());
                        obj.put("personcode", customer.getAcccode() == null ? "" : customer.getAcccode());
                        obj.put("personemail", customer.getEmail());
                        obj.put("billno", mRPContract.getContractid());
                        obj.put(Constants.SEQUENCEFORMATID, mRPContract.getSeqformat() == null ? "" : mRPContract.getSeqformat().getID());
                        obj.put("mergedCategoryData", "Master Contract");  //type of data
                        obj.put("type", type.intValue());
                        jArr.put(obj);
                    }
                    
                }
        } catch (NumberFormatException | JSONException ex) {
            throw ServiceException.FAILURE("getMasterContractLinkingInformation : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getMasterContractJsonforRequest(List list, Map<String, Object> requestParams) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {

            String companyid = (String) requestParams.get("companyid");
            int moduleid = 0;
            DateFormat df = (DateFormat) requestParams.get("df");
            if (requestParams.containsKey("requestModuleid")) {
                moduleid = Integer.parseInt(requestParams.get("requestModuleid").toString());
            }
            DateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd");
//            Date today = sdf1.parse(sdf1.format(new Date()));

            String currencyid = (String) requestParams.get("gcurrencyid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);
//            HashMap<String, String> customFieldMap = new HashMap<>();
//            HashMap<String, String> customDateFieldMap = new HashMap<>();
//            HashMap<String, Object> fieldrequestParams = new HashMap();
//            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
//            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Contract_Order_ModuleId ));
//            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
//            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
//            KwlReturnObject cpresult = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), companyid);
//            CompanyAccountPreferences pref = (CompanyAccountPreferences) cpresult.getEntityList().get(0);
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                MRPContract mRPContract = (MRPContract) itr.next();
                //Set<MRPContractDetails> contractDetailses = mRPContract.getRows();

                KWLCurrency currency = null;
                Customer customer = mRPContract.getCustomer();
                currency = customer.getCurrency() == null ? kwlcurrency : customer.getCurrency();
                JSONObject obj = new JSONObject();
                obj.put("billid", mRPContract.getID());
                obj.put("personid", customer.getID());
                obj.put("billno", mRPContract.getContractid());
                obj.put("currencysymbol", currency.getSymbol());
                obj.put("currencyid", currency.getCurrencyID());
                obj.put("personname", customer.getName());
                obj.put("contractname",mRPContract.getContractname());


//                    Map<String, Object> variableMap = new HashMap<String, Object>();
//                    SalesOrderCustomData jeDetailCustom = (SalesOrderCustomData) salesOrder.getSoCustomData();
//                    replaceFieldMap = new HashMap<String, String>();
//                    if (jeDetailCustom != null) {
//                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
//                        JSONObject params = new JSONObject();
//                        params.put("companyid", companyid);
//                        boolean linkflag = false;
//                        if (requestParams.containsKey("linkflag") && requestParams.get("linkflag") != null) {
//                            linkflag = Boolean.FALSE.parseBoolean(requestParams.get("linkflag").toString());
//                        }
//                        if (doflag || exceptFlagINV || linkflag) {
//                            int moduleId = doflag ? ((!isConsignment) ? Constants.Acc_Delivery_Order_ModuleId : Constants.Acc_ConsignmentDeliveryOrder_ModuleId) : exceptFlagINV ? Constants.Acc_Invoice_ModuleId : Constants.Acc_Purchase_Order_ModuleId;
//                            if (isLeaseSO) {
//                                moduleId = Constants.Acc_Lease_DO;
//                            }
//                            params.put("linkModuleId", moduleId);
//                            params.put("isLink", true);
//                            params.put("customcolumn", 0);
//                        }
//                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
//                    }
                jArr.put(obj);

            }
        } catch (NumberFormatException | JSONException ex) {
            throw ServiceException.FAILURE("getMasterContractsJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public JSONObject exportContractMaster(Map<String, Object> requestParams) throws ServiceException {
    
        JSONObject jobj = new JSONObject();
        JSONArray dataJArr = new JSONArray();
        HttpServletRequest request=null;
        HttpServletResponse response=null;
        try {
            
            dataJArr=createMasterContractJSON(requestParams);
            jobj.put("data", dataJArr);
            request=(HttpServletRequest)requestParams.get("request");
            response=(HttpServletResponse)requestParams.get("response");
            exportDaoObj.processRequest(request, response, jobj);
            
        } catch (Exception ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
}
        return jobj;
    }
    
    
    public JSONArray createMasterContractJSON(Map<String, Object> requestParams) throws ServiceException {
        JSONArray dataJArr = new JSONArray();
        Map<String, Object> requestParams1=new HashMap<>();
        int count=0;
        DateFormat userdf = null;
        try {
            
            if(requestParams.containsKey(Constants.userdf)){
                userdf = (DateFormat) requestParams.get(Constants.userdf);
            }
            
            KwlReturnObject returnResult = accContractManagementDAOObj.getMasterContractDetails(requestParams);
            
            List<MRPContract> list = returnResult.getEntityList();
            List<String> list1=null;
            MRPContract mRPParentContratcID=null;
            count = returnResult.getRecordTotalCount();
            String parentcontractid="",parentcontract="";
            for (MRPContract mRPContract : list) {
                parentcontractid="";
                parentcontract="";
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("id", mRPContract.getID());
                jSONObject.put("billid", mRPContract.getID());
                jSONObject.put("contractname", mRPContract.getContractname());
                jSONObject.put("contractid", mRPContract.getContractid());
                jSONObject.put("customerid", mRPContract.getCustomer().getID());
                jSONObject.put("customername", mRPContract.getCustomer().getName());
                jSONObject.put("creationdate", userdf != null ? userdf.format(mRPContract.getCreationdate()) : mRPContract.getCreationdate());
                jSONObject.put("sellertype", (mRPContract.getSellertype()==null)?"":mRPContract.getSellertype().getValue());
                jSONObject.put("sellertypeid", (mRPContract.getSellertype()==null)?"":mRPContract.getSellertype().getID());
                String contractterm ="";
                if(mRPContract.getContractterm().equals("1")){
                    contractterm = "Day";
                }else if(mRPContract.getContractterm().equals("2")){
                    contractterm = "Week";
                }else if(mRPContract.getContractterm().equals("3")){
                    contractterm = "Month";
                }else if(mRPContract.getContractterm().equals("4")){
                    contractterm = "Year";
                }
                jSONObject.put("contacttermid", mRPContract.getContractterm());
                jSONObject.put("contactterm", contractterm);
                jSONObject.put("paymentterm", mRPContract.getPaymenttermname()==null?"":mRPContract.getPaymenttermname().getTermname());
                jSONObject.put("contractstatusid", mRPContract.getContractstatus()==null?"":mRPContract.getContractstatus().getID());
                jSONObject.put("contractstatus", mRPContract.getContractstatus()==null?"":mRPContract.getContractstatus().getValue());
                requestParams1.put("contractid", mRPContract.getID());
                KwlReturnObject returnObject = accContractManagementDAOObj.getParentContractID(requestParams1);
                list1 = returnObject.getEntityList();
                if (list1.size() > 0) {
                    parentcontractid="";
                    parentcontract="";
                    for (String parentID : list1) {
                        KwlReturnObject returnObject1 = accountingHandlerDAOobj.getObject(MRPContract.class.getName(), parentID);
                        mRPParentContratcID = (MRPContract) returnObject1.getEntityList().get(0);
                        if (mRPParentContratcID != null) {
                        parentcontractid += mRPParentContratcID.getID() + ",";
                        parentcontract += mRPParentContratcID.getContractid() + ",";
                   
                }
                    }
                    parentcontractid = (!StringUtil.isNullOrEmpty(parentcontractid))?(parentcontractid.substring(0, parentcontractid.length() - 1)):"";
                    parentcontract = (!StringUtil.isNullOrEmpty(parentcontract))?(parentcontract.substring(0, parentcontract.length() - 1)):"";
                }
                jSONObject.put("subcontractid", parentcontractid);
                jSONObject.put("subcontract", parentcontract);
                jSONObject.put("subcontractname", mRPContract.getParentcontractname());
                jSONObject.put("contractstartdate", userdf != null ? userdf.format(mRPContract.getContractstartdate()) : mRPContract.getContractstartdate());
                jSONObject.put("contractenddate", userdf != null ? userdf.format(mRPContract.getContractenddate()) : mRPContract.getContractenddate());
                jSONObject.put("seqformat", mRPContract.getSeqformat()!=null ? mRPContract.getSeqformat().getID() : "");
                jSONObject.put("deleted", mRPContract.isDeleteflag());

                
                //Billing Contract JSON
                Map<String, Object> reqParams = new HashMap<>();
                reqParams.put("mrpContractID", mRPContract.getID());
                KwlReturnObject kwlreturn = accContractManagementDAOObj.getAddressDetails(reqParams);
                List kwlreturnlist = kwlreturn.getEntityList();
                if(kwlreturnlist.size()>0){
                    MRPAddressDetails mrpAddressDetails = (MRPAddressDetails) kwlreturnlist.get(0);
                    jSONObject.put("billingaddresscombo", mrpAddressDetails.getAddresscombo());
                    jSONObject.put("billingaliasname", mrpAddressDetails.getAliasname());
                    jSONObject.put("billingaddress", mrpAddressDetails.getAddress());
                    jSONObject.put("billingcounty", mrpAddressDetails.getCounty());
                    jSONObject.put("billingcity", mrpAddressDetails.getCity());
                    jSONObject.put("billingstate", mrpAddressDetails.getState());
                    jSONObject.put("billingcountry", mrpAddressDetails.getCountry());
                    jSONObject.put("billingpostal", mrpAddressDetails.getPostalcode());
                    jSONObject.put("billingphone", mrpAddressDetails.getPhone());
                    jSONObject.put("billingmobile", mrpAddressDetails.getMobilenumber());
                    jSONObject.put("billingfax", mrpAddressDetails.getFax());
                    jSONObject.put("billingemail", mrpAddressDetails.getEmailid());
                    jSONObject.put("billingrecipientname", mrpAddressDetails.getRecipientname());
                    jSONObject.put("billingcontactperson", mrpAddressDetails.getContactperson());
                    jSONObject.put("billingcontactpersonnumber", mrpAddressDetails.getContactpersonnumber());
                    jSONObject.put("billingcontactpersondesignation", mrpAddressDetails.getContactpersondesignation());
                    jSONObject.put("billingwebsite", mrpAddressDetails.getWebsite());
                }
                
                //Payment Term JSON
                jSONObject.put("paymentmethodid", mRPContract.getPaymentmethodname()!=null ? mRPContract.getPaymentmethodname().getID() : "");
                jSONObject.put("paymentmethodname", mRPContract.getPaymentmethodname()!=null ? mRPContract.getPaymentmethodname().getMethodName() : "");
                jSONObject.put("accountname", mRPContract.getAccountname());
                jSONObject.put("detailtype", mRPContract.getDetailstype());
                jSONObject.put("autopopulate", mRPContract.getAutopopulate());
                jSONObject.put("showincscp", mRPContract.getShownincsorcp());
                jSONObject.put("bankname", mRPContract.getBankname());
                jSONObject.put("bankaccountnumber", mRPContract.getBankaccountnumber());
                jSONObject.put("bankaddress", mRPContract.getBankaddress());
                jSONObject.put("paymenttermid", mRPContract.getPaymenttermname()!=null ? mRPContract.getPaymenttermname().getID() : "");
                jSONObject.put("paymenttermname", mRPContract.getPaymenttermname()!=null ? mRPContract.getPaymenttermname().getTermname() : "");
                jSONObject.put("paymenttermdays", mRPContract.getPaymenttermname()!=null ? mRPContract.getPaymenttermname().getTermdays() : "");
                jSONObject.put("paymenttermdate", mRPContract.getPaymenttermdate());
                
                //Document Required JSON
                jSONObject.put("contractorTeeName", mRPContract.getContractorteename());
                jSONObject.put("PANNumber", mRPContract.getPannumber());
                jSONObject.put("TANNumber", mRPContract.getTannumber());
                jSONObject.put("dateOfAggrement", mRPContract.getDateofaggrement());
                jSONObject.put("countryAggrement", mRPContract.getCountryaggrement()!=null ? mRPContract.getCountryaggrement().getID() : "");
                jSONObject.put("countryAggrementname", mRPContract.getCountryaggrement()!=null ? mRPContract.getCountryaggrement().getValue() : "");
                jSONObject.put("stateAggrement", mRPContract.getStateaggrement());
                jSONObject.put("previousContractId", mRPContract.getPreviouscontractid());
                jSONObject.put("remarks", mRPContract.getDocumentrequiredremarks());
                KwlReturnObject kwlReturnObj = accContractManagementDAOObj.getMasterContractAttachments(reqParams);
                int attachment = kwlReturnObj.getRecordTotalCount();
                jSONObject.put("attachment", attachment);
                
                /*
                 Add Global Custom data for document
                 */
                String companyId = (String) requestParams.get("companyid");
                Map globalMap = new HashMap();
                globalMap.put("moduleid", Constants.MRP_Contract);
                globalMap.put("companyid", companyId);
                globalMap.put("contractid", mRPContract.getID());
                globalMap.put(Constants.userdf, userdf);
                putGlobalCustomDetailsForMasterContract(jSONObject, globalMap);
                dataJArr.put(jSONObject);
            }
            
            
        } catch (Exception ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return dataJArr;
    }
    
    public void putGlobalCustomDetailsForMasterContract(JSONObject jSONObject, Map<String, Object> map) throws ServiceException, JSONException {

        String companyId = "";
        int moduleid = 0;
        String contractId = "";
        if (map.containsKey("companyid")) {
            companyId = map.get("companyid").toString();
        }
        if (map.containsKey("moduleid")) {
            moduleid = Integer.parseInt(map.get("moduleid").toString());
        }
        if (map.containsKey("contractid")) {
            contractId = map.get("contractid").toString();
        }
        // ## Get Custom Field Data 
        HashMap<String, Object> fieldrequestParams = new HashMap();
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyId, moduleid));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        Map<String, Object> variableMap = new HashMap<String, Object>();

        KwlReturnObject customObjresult = null;
        try {
            /*
            Temporaty handle eception need to handle case
            */
        customObjresult = accountingHandlerDAOobj.getObject(MRPContractCustomData.class.getName(), contractId);
        } catch (Exception e) {
            Logger.getLogger(AccContractManagementDAOImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        
        replaceFieldMap = new HashMap<String, String>();
        if (customObjresult != null && customObjresult.getEntityList().size() > 0) {
            MRPContractCustomData mrpContractCustomData = (MRPContractCustomData) customObjresult.getEntityList().get(0);
            if (mrpContractCustomData != null) {
                AccountingManager.setCustomColumnValues(mrpContractCustomData, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", companyId);
                params.put("isExport", true);
                params.put(Constants.userdf, map.get(Constants.userdf));
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
            }
        }
    }
    
    @Override
    public JSONObject deleteMasterContracts(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject returnResult = null;
        String[] arrayOfID = null;
        try {
            if (requestParams.containsKey("idsfordelete") && requestParams.get("idsfordelete") != null) {

                arrayOfID = (String[]) requestParams.get("idsfordelete");
                for (int count = 0; count < arrayOfID.length; count++) {
                    requestParams.put("id", arrayOfID[count]);
                    returnResult = accContractManagementDAOObj.deleteMasterContracts(requestParams);
                }
            }
            jobj.put("msg", returnResult.getMsg());
        } catch (Exception ex) {
            Logger.getLogger(AccContractManagementDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject deleteMasterContractsPermanently(Map<String, Object> requestParams) throws ServiceException {

        JSONObject jobj = new JSONObject();
        MRPContract mrpContract = null;
        String[] arrayOfID = null;
        KwlReturnObject returnResult = null;
        String companyId = null;

        try {
            if (requestParams.containsKey("idsfordelete") && requestParams.get("idsfordelete") != null) {
                arrayOfID = (String[]) requestParams.get("idsfordelete");
                companyId = (String) requestParams.get("companyid");

                for (String arrayOfID1 : arrayOfID) {
                    requestParams.put("id", arrayOfID1);
//                    returnResult = accContractManagementDAOObj.deleteMasterContractsPermanently(requestParams);
                    
                     Map<String, Object> params = new HashMap<>();
                
                    params.clear();
                    params.put("mrpContractID", arrayOfID1);
                    params.put("companyid", companyId);
                    //Delete Master Contract records from all tables
                    deleteMasterContractData(params);
                    
                    //Delete product grid data for master contract
                    accContractManagementDAOObj.deleteMasterContract(params);
                
                }
                jobj.put("msg", "Master Contract(s) has been deleted successfully.");
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject getMasterContractRows(Map<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            KwlReturnObject result = accContractManagementDAOObj.getMasterContractRows(requestParams);
            JSONArray jarr = getMasterContractRowsJson(result.getEntityList());
            jobj.put("data", jarr);
        } catch (ServiceException | JSONException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    public JSONArray getMasterContractRowsJson(List<MRPContractDetails> list) throws ServiceException{
        JSONArray jarr = new JSONArray();
        try {
            if (list.size() > 0) {
                for (MRPContractDetails mrpContractDetails : list) {
                    JSONObject jobject = new JSONObject();
                    //Contract Details JSON
                    jobject.put("rowid", mrpContractDetails.getID());
                    jobject.put("productid", mrpContractDetails.getProduct()!=null ? mrpContractDetails.getProduct().getID() : "");
                    jobject.put("productname", mrpContractDetails.getProduct()!=null ? mrpContractDetails.getProduct().getProductName() : "");
                    jobject.put("desc", mrpContractDetails.getDescription());
                    jobject.put("quantity", mrpContractDetails.getQuantity());
                    jobject.put("uomid", mrpContractDetails.getUom()!=null ? mrpContractDetails.getUom().getID() : "");
                    jobject.put("uomname", mrpContractDetails.getUom()!=null ? mrpContractDetails.getUom().getNameEmptyforNA() : "");
                    jobject.put("baseuomquantity", mrpContractDetails.getBaseuomquantity());
                    jobject.put("baseuomrate", mrpContractDetails.getBaseuomrate());
                    jobject.put("baseuomname", ((mrpContractDetails.getProduct() != null) && (mrpContractDetails.getProduct().getUnitOfMeasure() != null)) ? mrpContractDetails.getProduct().getUnitOfMeasure().getNameEmptyforNA() : "" );
                    jobject.put("baseuomid", ((mrpContractDetails.getProduct() != null) && (mrpContractDetails.getProduct().getUnitOfMeasure() != null)) ? mrpContractDetails.getProduct().getUnitOfMeasure().getID() : "");
                    jobject.put("rate", mrpContractDetails.getRate());
                    jobject.put("discamount", mrpContractDetails.getTotalamount());
                    
                    //Shipment Contract JSON
                    jobject.put("deliverymode", mrpContractDetails.getDeliverymode()!=null ? mrpContractDetails.getDeliverymode().getID() : "");
                    jobject.put("totalnoofunit", mrpContractDetails.getTotalnoofunit());
                    jobject.put("totalquantity", mrpContractDetails.getTotalquantity());
                    jobject.put("shippingperiodfrom", mrpContractDetails.getShippingperiodfrom());
                    jobject.put("shippingperiodto", mrpContractDetails.getShippingperiodto());
                    jobject.put("partialshipmentallowed", mrpContractDetails.getPartialshipmentallowed());
                    jobject.put("shipmentstatus", mrpContractDetails.getShipmentstatus()!=null ? mrpContractDetails.getShipmentstatus().getID() : "");
                    jobject.put("shippingagent", mrpContractDetails.getShippingagent());
                    jobject.put("loadingportcountry", mrpContractDetails.getLoadingportcountry());
                    jobject.put("loadingport", mrpContractDetails.getLoadingport());
                    jobject.put("transshipmentallowed", mrpContractDetails.getTransshipmentallowed());
                    jobject.put("dischargeportcountry", mrpContractDetails.getDischargeportcountry());
                    jobject.put("dischargeport", mrpContractDetails.getDischargeport());
                    jobject.put("finaldestination", mrpContractDetails.getFinaldestination());
                    jobject.put("postalcode", mrpContractDetails.getPostalcode());
                    jobject.put("budgetfreightcost", mrpContractDetails.getBudgetfreightcost());
                    jobject.put("shipmentcontratremarks", mrpContractDetails.getShipmentcontractremarks());
                    //Shipping Address Details JSON
                    Map<String, Object> requestParams = new HashMap<>();
                    requestParams.put("mrpContractDetailsID", mrpContractDetails.getID());
                    KwlReturnObject kwlreturn = accContractManagementDAOObj.getAddressDetails(requestParams);
                    List kwlreturnlist = kwlreturn.getEntityList();
                    if(kwlreturnlist.size()>0){
                        MRPAddressDetails mrpAddressDetails = (MRPAddressDetails) kwlreturnlist.get(0);
                        jobject.put("shippingaddrscombo", mrpAddressDetails.getAddresscombo());
                        jobject.put("shippingaliasname", mrpAddressDetails.getAliasname());
                        jobject.put("shippingaddress", mrpAddressDetails.getAddress());
                        jobject.put("shippingcity", mrpAddressDetails.getCity());
                        jobject.put("shippingstate", mrpAddressDetails.getState());
                        jobject.put("shippingcountry", mrpAddressDetails.getCountry());
                        jobject.put("shippingpostalcode", mrpAddressDetails.getPostalcode());
                        jobject.put("shippingphone", mrpAddressDetails.getPhone());
                        jobject.put("shippingmobile", mrpAddressDetails.getMobilenumber());
                        jobject.put("shippingfax", mrpAddressDetails.getFax());
                        jobject.put("shippingemail", mrpAddressDetails.getEmailid());
                        jobject.put("shippingrecipientname", mrpAddressDetails.getRecipientname());
                        jobject.put("shippingcontactperson", mrpAddressDetails.getContactperson());
                        jobject.put("shippingcontactpersonnumber", mrpAddressDetails.getContactpersonnumber());
                        jobject.put("shippingcontactcersondesignation", mrpAddressDetails.getContactpersondesignation());
                        jobject.put("shippingwebsite", mrpAddressDetails.getWebsite());
                        jobject.put("shippingroute", mrpAddressDetails.getRoute());
                    }
                    
                    //Packaging Contract JSON
                    jobject.put("unitweightvalue", mrpContractDetails.getUnitweightvalue());
                    jobject.put("unitweight", mrpContractDetails.getUnitweight());
                    jobject.put("packagingtype", mrpContractDetails.getPackagingtype());
                    jobject.put("certificaterequirement", mrpContractDetails.getCertificaterequirement());
                    jobject.put("shippingmarksdetails", mrpContractDetails.getShippingmarksdetails());
                    jobject.put("shipmentmode", mrpContractDetails.getShipmentmode());
                    jobject.put("percontainerload", mrpContractDetails.getPercontainerload());
                    jobject.put("palletmaterial", mrpContractDetails.getPalletmaterial());
                    jobject.put("packagingprofiletype", mrpContractDetails.getPackagingprofiletype()!=null ? mrpContractDetails.getPackagingprofiletype().getID() : "");
                    jobject.put("marking", mrpContractDetails.getMarking());
                    jobject.put("drumorbagdetails", mrpContractDetails.getDrumorbagdetails());
                    jobject.put("drumorbagsize", mrpContractDetails.getDrumorbagsize());
                    jobject.put("numberoflayers", mrpContractDetails.getNumberoflayers());
                    jobject.put("heatingpad", mrpContractDetails.getHeatingpad());
                    jobject.put("palletloadcontainer", mrpContractDetails.getPalletloadcontainer());
                    
                    String companyId = (String) mrpContractDetails.getCompany().getCompanyID();
                    
                    HashMap<String, Object> fieldrequestParams1 = new HashMap();
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    fieldrequestParams1.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    fieldrequestParams1.put(Constants.filter_values, Arrays.asList(companyId,Constants.MRP_Contract, 1));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams1, replaceFieldMap, customFieldMap, customDateFieldMap);

                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    MRPContractDetailsCustomData contractDetailsCustomData = (MRPContractDetailsCustomData) mrpContractDetails.getAccMRPContractDetailsCustomData();
                    AccountingManager.setCustomColumnValues(contractDetailsCustomData, FieldMap, replaceFieldMap, variableMap);

                    if (contractDetailsCustomData != null) {
                        JSONObject params = new JSONObject();
                        boolean isExport = false;
                        params.put("isExport", isExport);
                        params.put("linkModuleId", Constants.MRP_Contract);
                        params.put("isLink", false);
                        params.put("companyid", companyId);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, jobject, params);
                    }
                    jarr.put(jobject);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccContractManagementServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jarr;
    }
}
