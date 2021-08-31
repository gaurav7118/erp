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

package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.CommonIndonesianNumberToWords;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.hibernate.ObjectNotFoundException;

import com.krawler.hql.accounting.Discount;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.entitygst.AccEntityGstDao;
import com.krawler.spring.accounting.handler.CommonEnglishNumberToWords;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
 
/**
 *
 * @author krawler
 */
public class accGoodsReceiptCMN implements GoodsReceiptCMNConstants{

    private accDebitNoteDAO accDebitNoteobj;
    private accAccountDAO accAccountDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accProductDAO accProductObj;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
    private CommonEnglishNumberToWords EnglishNumberToWordsOjb = new CommonEnglishNumberToWords();
    private CommonIndonesianNumberToWords IndonesianNumberToWordsOjb = new CommonIndonesianNumberToWords();
    private AccCommonTablesDAO accCommonTablesDAO; 
    private fieldDataManager fieldDataManagercntrl;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private fieldManagerDAO fieldManagerDAOobj;
    private AccEntityGstDao accEntityGstDao;
    
    public void setAccEntityGstDao(AccEntityGstDao accEntityGstDao) {
        this.accEntityGstDao = accEntityGstDao;
    }
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
     public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }

    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {  //Neeraj
        this.accCurrencyDAOobj = accCurrencyobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setaccSalesOrderServiceDAO(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
    public void setAccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
   public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }
    
    public void setAccMasterItemsDAOobj(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }    
    public JSONArray getGoodsReceiptRows(HttpServletRequest request, String[] greceipts) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            String companyid=sessionHandlerImpl.getCompanyid(request);
            Map<String, Object> requestParams = new HashMap<String, Object>();
            DateFormat userDateFormat= (DateFormat) request.getAttribute(Constants.userdf);
            requestParams.put(Constants.userdf,userDateFormat);
            requestParams.put(COMPANYID, companyid);
            requestParams.put("DateFormat", df);
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(BILLS, request.getParameter(BILLS)==null?"":request.getParameter(BILLS).split(","));
            boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag")))?false:Boolean.parseBoolean(request.getParameter("linkingFlag"));
            requestParams.put("linkingFlag", linkingFlag);
            boolean islinkPItoCN = (StringUtil.isNullOrEmpty(request.getParameter("islinkPItoCN")))?false:Boolean.parseBoolean(request.getParameter("islinkPItoCN"));
            requestParams.put("islinkPItoCN", islinkPItoCN);
            String isexpenseinvStr=(String)request.getParameter("isexpenseinv");
            boolean doflag = request.getParameter("doflag")!=null?true:false;
            boolean isConsignment=(StringUtil.isNullOrEmpty(request.getParameter("isConsignment")))?false:Boolean.parseBoolean(request.getParameter("isConsignment"));
            boolean salesPurchaseReturnflag = false;
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("salesPurchaseReturnflag"))){
                salesPurchaseReturnflag = Boolean.parseBoolean(request.getParameter("salesPurchaseReturnflag"));
                requestParams.put("salesPurchaseReturnflag", salesPurchaseReturnflag);
            }
            boolean istdsapplicable = false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("istdsapplicable"))){
                istdsapplicable = Boolean.parseBoolean(request.getParameter("istdsapplicable"));
                requestParams.put("istdsapplicable", istdsapplicable);
            }
            boolean isExport = false;
            if (request.getAttribute("isExport") != null) {
                isExport = (boolean) request.getAttribute("isExport");
            }
            requestParams.put("isExport", isExport);
            Map<String, Object> ProductFieldsRequestParams = new HashMap();
            ProductFieldsRequestParams.put("companyid", companyid);
            ProductFieldsRequestParams.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);

            List masterFieldsResultList = CommonFunctions.getproductmastersFieldsToShowLineLevel(ProductFieldsRequestParams, accountingHandlerDAOobj);
            requestParams.put("ProductMastersFields", masterFieldsResultList);
            KwlReturnObject capresult = accountingHandlerDAOobj.loadObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            capresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) capresult.getEntityList().get(0);
            String dtype = request.getParameter("dtype");
            
            boolean isForReport = false;
            
            if(!StringUtil.isNullOrEmpty(dtype) && dtype.equals("report")){
                isForReport = true;
            }
            boolean isCopyInvoice=false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("isCopyInvoice"))){
                isCopyInvoice = Boolean.parseBoolean(request.getParameter("isCopyInvoice"));
            }
            requestParams.put("isForReport", isForReport);
            requestParams.put("doflag", doflag);
            requestParams.put("isCopyInvoice", request.getParameter("isCopyInvoice"));
            String currencyid = (String) requestParams.get(GCURRENCYID);
            KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            greceipts = (greceipts == null)? (String[]) requestParams.get(BILLS) : greceipts;
            int i = 0;
            boolean isEdit = (StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            HashMap<String, Object> grRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("goodsReceipt.ID");
            order_by.add(SRNO);
            order_type.add("asc");
            grRequestParams.put( FILTER_NAMES,filter_names);
            grRequestParams.put( FILTER_PARAMS,filter_params);
            grRequestParams.put( ORDER_BY,order_by);
            grRequestParams.put( ORDER_TYPE,order_type);
            String allocationType = request.getParameter("allocationType");
            if (!StringUtil.isNullOrEmpty(allocationType) && allocationType.equals("4")) {
                int hsncolnum = 0, producttaxcolnum = 0;
                HashMap fieldparams = new HashMap<>();
                fieldparams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "fieldname"));
                fieldparams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, "Custom_" + Constants.HSN_SACCODE));

                KwlReturnObject kwlReturnObjectGstCust = fieldManagerDAOobj.getFieldParams(fieldparams);
                List<FieldParams> fieldParamses = kwlReturnObjectGstCust.getEntityList();
                for (FieldParams fieldParams : fieldParamses) {
                    hsncolnum = fieldParams.getColnum();
                }
                fieldparams = new HashMap<>();
                fieldparams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "fieldname"));
                fieldparams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, "Custom_" + Constants.GSTProdCategory));

                kwlReturnObjectGstCust = fieldManagerDAOobj.getFieldParams(fieldparams);
                fieldParamses = kwlReturnObjectGstCust.getEntityList();
                for (FieldParams fieldParams : fieldParamses) {
                    producttaxcolnum = fieldParams.getColnum();
                }
                requestParams.put("producttaxcolnum", producttaxcolnum);
                requestParams.put("hsncolnum", hsncolnum);
                
                int assethsncolnum = 0, assetproducttaxcolnum = 0;
                fieldparams = new HashMap<>();
                fieldparams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "fieldname"));
                fieldparams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_FixedAssets_AssetsGroups_ModuleId, "Custom_" + Constants.HSN_SACCODE));

                kwlReturnObjectGstCust = fieldManagerDAOobj.getFieldParams(fieldparams);
                fieldParamses = kwlReturnObjectGstCust.getEntityList();
                for (FieldParams fieldParams : fieldParamses) {
                    assethsncolnum = fieldParams.getColnum();
                }
                fieldparams = new HashMap<>();
                fieldparams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "fieldname"));
                fieldparams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_FixedAssets_AssetsGroups_ModuleId, "Custom_" + Constants.GSTProdCategory));

                kwlReturnObjectGstCust = fieldManagerDAOobj.getFieldParams(fieldparams);
                fieldParamses = kwlReturnObjectGstCust.getEntityList();
                for (FieldParams fieldParams : fieldParamses) {
                    assetproducttaxcolnum = fieldParams.getColnum();
                }
                requestParams.put("assetproducttaxcolnum", assetproducttaxcolnum);
                requestParams.put("assethsncolnum", assethsncolnum);
            }
            while (greceipts != null && i < greceipts.length) {
                KwlReturnObject grresult = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), greceipts[i]);
                GoodsReceipt gReceipt = (GoodsReceipt) grresult.getEntityList().get(0);
                int moduleid = gReceipt.isFixedAssetInvoice() ? Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId :isConsignment?Constants.Acc_Consignment_GoodsReceipt_ModuleId:  Constants.Acc_Vendor_Invoice_ModuleId;
                
                HashMap<String, Object> fieldrequestParams = new HashMap();
                HashMap<String, String> customFieldMap = new HashMap<String, String>();
                HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid,moduleid, 1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
                filter_params.clear();
                filter_params.add(gReceipt.getID());
                if(gReceipt.isIsExpenseType()){
                    KwlReturnObject grdresult = accGoodsReceiptobj.getExpenseGRDetails(grRequestParams);
                    List<ExpenseGRDetail> expenseGRDetailList = grdresult.getEntityList();

                    if (expenseGRDetailList != null && !expenseGRDetailList.isEmpty())
                    {
                        for (ExpenseGRDetail expenseGRDetail: expenseGRDetailList)
                        {
                            jArr=getExpenseGRRow(requestParams,expenseGRDetail,gReceipt,currency,jArr,FieldMap, customFieldMap, customDateFieldMap);
                        }
                    }
                }
                else{
                    Map<GoodsReceiptDetail, Object[]> hm = applyDebitNotes(requestParams, gReceipt);
                    KwlReturnObject grdresult = accGoodsReceiptobj.getGoodsReceiptDetails(grRequestParams);
                    List<GoodsReceiptDetail> goodsReceiptDetailList= grdresult.getEntityList();
                    if (goodsReceiptDetailList != null && !goodsReceiptDetailList.isEmpty()){
                        Map<GoodsReceiptDetail, GoodsReceiptOrderDetails> goodsReceiptAndOrderMap = new HashMap<>();
                        StringBuilder documentIDs = new StringBuilder();
                          List names = new ArrayList(), params = new ArrayList(), orderby = new ArrayList(), ordertype = new ArrayList();
                            names.add("videtails.ID");
                            orderby.add("srno");
                            ordertype.add("asc");
                            for (GoodsReceiptDetail goodsReceiptDetail : goodsReceiptDetailList) {
                                HashMap<String, Object> doRequestParams = new HashMap<String, Object>();
                                doRequestParams.put("filter_names", names);
                                doRequestParams.put("filter_params", params);
                                doRequestParams.put("order_by", order_by);
                                doRequestParams.put("order_type", order_type);
                                params.clear();
                                params.add(goodsReceiptDetail.getID());
                                KwlReturnObject podresult = accGoodsReceiptobj.getGoodsReceiptOrderDetails(doRequestParams);
                                Iterator itr = podresult.getEntityList().iterator();
                                if (podresult.getEntityList().size() > 0) {
                                    GoodsReceiptOrderDetails goodsReceiptOrderDetails = (GoodsReceiptOrderDetails) itr.next();
                                    if (goodsReceiptOrderDetails != null) {
                                        if (!isCopyInvoice && goodsReceiptOrderDetails != null && goodsReceiptOrderDetails.getProduct() != null) {  //only if auto generate GRN option is on
                                            if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                                                if (goodsReceiptOrderDetails.getProduct().isIsBatchForProduct() || goodsReceiptOrderDetails.getProduct().isIsSerialForProduct() || goodsReceiptOrderDetails.getProduct().isIslocationforproduct() || goodsReceiptOrderDetails.getProduct().isIswarehouseforproduct() || goodsReceiptOrderDetails.getProduct().isIsrowforproduct() || goodsReceiptOrderDetails.getProduct().isIsrackforproduct() || goodsReceiptOrderDetails.getProduct().isIsbinforproduct()) {  //product level batch and serial no on or not
                                                    documentIDs.append("'" + goodsReceiptOrderDetails.getID() + "'").append(",");
                                                }
                                            }
                                        }
                                        if (isCopyInvoice && goodsReceiptOrderDetails.getProduct() != null && goodsReceiptOrderDetails.getProduct().isIslocationforproduct() && goodsReceiptOrderDetails.getProduct().isIswarehouseforproduct() && !goodsReceiptOrderDetails.getProduct().isIsBatchForProduct() && !goodsReceiptOrderDetails.getProduct().isIsSerialForProduct()) {//if copy invoice is true and product is without batch serial then append document id
                                            documentIDs.append("'" + goodsReceiptOrderDetails.getID() + "'").append(",");
                                        }
                                        goodsReceiptAndOrderMap.put(goodsReceiptDetail, goodsReceiptOrderDetails);
                                    }
                                }
                            }                        
                        Map<String, List<Object[]>> baMap = new HashMap<>();
                        if (documentIDs.length() > 0) {
                            Map<String, Object> batchSerialReqMap = new HashMap<>();
                            batchSerialReqMap.put(Constants.companyKey, companyid);
                            batchSerialReqMap.put(Constants.df, df);
                            batchSerialReqMap.put("linkingFlag", linkingFlag);
                            batchSerialReqMap.put("isEdit", isEdit);
                            batchSerialReqMap.put("isConsignment", isConsignment);
                            batchSerialReqMap.put("moduleID", moduleid);
                            batchSerialReqMap.put("documentIds", documentIDs.substring(0, documentIDs.length() - 1));
                            baMap = accInvoiceServiceDAO.getBatchDetailsMap(batchSerialReqMap);
                        }
                        
                        HashMap<String, Object> assParams = new HashMap<String, Object>();
                        assParams.put("companyId", companyid);
                        assParams.put("invrecord", true);
                        KwlReturnObject assResult = accProductObj.getAssetDetails(assParams);
                        List<AssetDetails> assetList = assResult.getEntityList();
                        List<String> assetNameList = new ArrayList<String>();
                        StringBuilder assetDocumentIDs = new StringBuilder();
                        for (AssetDetails ad : assetList) {
                            assetNameList.add(ad.getAssetId());
                            assetDocumentIDs.append("'" + ad.getId() + "'").append(",");
                        }

                        Map<String, List<Object[]>> assetBaMap = new HashMap<>();
                        if (assetDocumentIDs.length() > 0) {
                            Map<String, Object> batchSerialReqMap = new HashMap<>();
                            batchSerialReqMap.put(Constants.companyKey, companyid);
                            batchSerialReqMap.put(Constants.df, df);
                            batchSerialReqMap.put("linkingFlag", linkingFlag);
                            batchSerialReqMap.put("isEdit", isEdit);
                            batchSerialReqMap.put("isConsignment", isConsignment);
                            batchSerialReqMap.put("moduleID", moduleid);
                            batchSerialReqMap.put("documentIds", assetDocumentIDs.substring(0, assetDocumentIDs.length() - 1));
                            assetBaMap = accInvoiceServiceDAO.getBatchDetailsMap(batchSerialReqMap);
                        }
                        
                        for (GoodsReceiptDetail row: goodsReceiptDetailList){
                            jArr=getGRRow(requestParams,row,hm,gReceipt,currency,jArr,FieldMap,request, customFieldMap, customDateFieldMap,preferences,extraCompanyPreferences,goodsReceiptAndOrderMap,baMap,assetBaMap);
                        }
                    }
                }
                i++;
            }
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptRows : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    public JSONArray getExpenseGRRow(Map<String, Object> requestParams,ExpenseGRDetail row, GoodsReceipt gReceipt, KWLCurrency currency,JSONArray jArr,HashMap<String, Integer> FieldMap,HashMap<String, String> customFieldMap,HashMap<String, String> customDateFieldMap) throws ServiceException {
        try {
            String companyid=(String) requestParams.get(COMPANYID);
            JSONObject obj = new JSONObject();
            obj.put(BILLID, gReceipt.getID());
            obj.put(BILLNO, gReceipt.getGoodsReceiptNumber());
            String currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
            obj.put(CURRENCYSYMBOL, (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
//            KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
            KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, gReceipt.getCreationDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
            obj.put(OLDCURRENCYRATE, (Double) bAmt.getEntityList().get(0));
            obj.put(SRNO, row.getSrno());
            obj.put(ROWID, row.getID());
            obj.put(ACCOUNTID, row.getAccount().getID());
            obj.put("productname", row.getAccount().getName());
            obj.put("pid", row.getAccount().getAcccode());
            obj.put(DESC, StringUtil.isNullOrEmpty(row.getDescription())?"": StringUtil.DecodeText(row.getDescription()));
            obj.put("debit", row.isIsdebit());
            obj.put("rateIncludingGstEx", row.getRateExcludingGst());
            obj.put("gstCurrencyRate", row.getGstCurrencyRate());
            obj.put(ACCOUNTNAME, row.getAccount().getName());
//            obj.put(RATEINBASE,accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,(row.getRate()),row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getJournalEntry().getEntryDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
            obj.put(RATEINBASE,accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,(row.getRate()),row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getCreationDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
            obj.put(GoodsReceiptCMNConstants.ISEXPENSEINV, gReceipt.isIsExpenseType());
            Discount disc = row.getDiscount();
            obj.put(RATE, row.getRate());
            obj.put(Constants.unitpriceForExcelFile, row.getRate());
            obj.put(Constants.amountForExcelFile, row.getAmount());
            obj.put("isgstincluded", gReceipt.isGstIncluded());
            obj.put("includeprotax", row.getTax()!=null?true:false);
            DateFormat userDateFormat=null;
            if(requestParams.containsKey(Constants.userdf) && (requestParams.get(Constants.userdf)!= null)){
                userDateFormat=(DateFormat)requestParams.get(Constants.userdf);
            }
            boolean isForReport = false;
            if (row.getExpensePODetail() != null) {// When Expense PO is linked in Expense PI 
                obj.put("linkto", row.getExpensePODetail().getPurchaseOrder().getPurchaseOrderNumber());
                obj.put("linkid", row.getExpensePODetail().getPurchaseOrder().getID());
                obj.put("savedrowid", row.getExpensePODetail().getID());
                obj.put("linktype", 0);
            }
            if(requestParams.containsKey("isForReport")){
                isForReport = (Boolean) requestParams.get("isForReport");
            }
            
            boolean isExport=false;
            if (requestParams.containsKey("isExport") && requestParams.get("isExport") != null) { // True in case of Export
                isExport = (Boolean) requestParams.get("isExport");
            }
            boolean isCopyInvoice=false;
            if(requestParams.containsKey("isCopyInvoice") && requestParams.get("isCopyInvoice") != null){
                isCopyInvoice = Boolean.valueOf((String)requestParams.get("isCopyInvoice"));
            }

//** Code for cal Debit Note amount[PS]
//                    double amount = 0;
//                    if (hm.containsKey(row)) {
//                        Object[] val = (Object[]) hm.get(row);
//                      //  amount = (Double) val[0];//without invoice tax
//                        remainingquantity = (Integer) val[1];
//                        obj.put("remainingquantity", remainingquantity);
//                        obj.put("remquantity", 0);
//
//                    }
///

//** Code for Cal Payment Received [PS]
            double rowTaxAmount=0;
            Map amthm = getExpenseGRAmount(gReceipt);
            Object[] val = (Object[]) amthm.get(row);
            double amount = (Double) val[0];
            rowTaxAmount = (Double) val[1];
            obj.put( ORIGNALAMOUNT,amount);
            obj.put( AMOUNT,amount);
            
            /*
               TDS Details for each line
            */
            if(isCopyInvoice && row.getTdsJEMapping() != null && !StringUtil.isNullOrEmpty(row.getTdsJEMapping().getID())){
                obj.put("appliedTDS", "");
                obj.put("tdsamount", 0.0);
            }else if(row.getGoodsReceipt().isIsTDSApplicable() && row.getNatureOfPayment()!=null){
                JSONArray jrrAppliedTDS=new JSONArray();
                JSONObject jobjAppliedTDS=new JSONObject();
                KwlReturnObject deducteeTypeObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), row.getGoodsReceipt().getVendor().getDeducteeType());
                MasterItem deducteeTypeMI = (MasterItem) deducteeTypeObj.getEntityList().get(0);
                jobjAppliedTDS.put( "tdsAssessableAmount",row.getTdsAssessableAmount());// On which TDS is Applied
                jobjAppliedTDS.put( "deducteetypename",deducteeTypeMI.getValue());
                jobjAppliedTDS.put( "accountid",row.getAccount().getID());
                jobjAppliedTDS.put( "amount",amount);
                jobjAppliedTDS.put( "enteramount",amount);
                jobjAppliedTDS.put( "ruleid",row.getTdsRuleId());
                jobjAppliedTDS.put( "tdsamount",row.getTdsLineAmount());
                jobjAppliedTDS.put( "tdspercentage",row.getTdsRate());
                jobjAppliedTDS.put( "natureofpayment",row.getNatureOfPayment()!=null?row.getNatureOfPayment().getID():"");
                jobjAppliedTDS.put( "natureofpaymentName", row.getNatureOfPayment()!=null?(row.getNatureOfPayment().getCode() +" - "+ row.getNatureOfPayment().getValue()):"");
                jobjAppliedTDS.put("tdsaccountid", row.getTdsPayableAccount()!=null?row.getTdsPayableAccount().getID():"");
                if (row.getGoodsReceiptDetailPaymentMapping() != null && row.getGoodsReceiptDetailPaymentMapping().size() > 0) {
                    JSONArray jrrAdvancePaymentTDS = new JSONArray();
                    Set<GoodsReceiptDetailPaymentMapping> paymentDetailsSet = row.getGoodsReceiptDetailPaymentMapping();
                    for (GoodsReceiptDetailPaymentMapping mappingDetails : paymentDetailsSet) {
                        JSONObject jobjAdvancePaymentTDS = new JSONObject();
                        jobjAdvancePaymentTDS.put("goodsReceiptDetailsAdvancePaymentId", mappingDetails.getPayment());
                        jobjAdvancePaymentTDS.put("adjustedAdvanceTDSamount", mappingDetails.getAdvanceAdjustedAmount());
                        jobjAdvancePaymentTDS.put("paymentamount", mappingDetails.getPaymentAmount());
                        jrrAdvancePaymentTDS.put(jobjAdvancePaymentTDS);
                    }
                    jobjAppliedTDS.put("advancePaymentDetails", jrrAdvancePaymentTDS.toString());
                }
                jrrAppliedTDS.put(jobjAppliedTDS);
                obj.put("appliedTDS", jrrAppliedTDS.toString());
                obj.put("tdsamount", row.getTdsLineAmount());
                obj.put("tdsjemappingID", row.getTdsJEMapping() != null ? row.getTdsJEMapping().getID() : "");
            }
///
            /*
             * disc.isInPercent()=1 (discountispercent is percentage)
             * disc.isInPercent()=0 (discountispercent is flat)
             */
            if (disc != null) {
                
                /* In exported file showing discount value with proper % or currency symbol 
                 same as expander
                        
                 */
                if (isExport) {
                    obj.put(PRDISCOUNT, disc.isInPercent() ? (disc.getDiscount() + "%") : (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol())+ " " + disc.getDiscount());
                } else {
                    obj.put(PRDISCOUNT, disc.getDiscount());
                }
               
                /*
                 * If discount in percent then calculate
                 */
                double discountValue=0.0;
                obj.put("discountvalue", disc.getDiscountValue());
                obj.put("discountispercent", disc.isInPercent() ? 1 : 0); 
                
            } else {
                obj.put(PRDISCOUNT, 0);
                obj.put("discountvalue", 0);
                obj.put("discountispercent", 1); 
            }
            double taxPercent = 0;
            double rowTaxPercent = 0;
            if (row.getTax() != null) {
//                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), row.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getCreationDate(), row.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
            }
              // ## Get Custom Field Data 
            Map<String, Object> variableMap = new HashMap<String, Object>();
            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
            Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
            Detailfilter_params.add(row.getID());
            invDetailRequestParams.put("filter_names", Detailfilter_names);
            invDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accGoodsReceiptobj.getGoodsReceiptCustomData(invDetailRequestParams);
            if (idcustresult.getEntityList().size() > 0) {
                AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                Date dateFromDB=null;
                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                    String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                    String valueForReport = "";
                    if (customFieldMap.containsKey(varEntry.getKey()) && isForReport && coldata != null) {
                        try {
                            String[] valueData = coldata.split(",");
                            for (String value : valueData) {
                                FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), value);
                        if (fieldComboData != null) {
                                    valueForReport += fieldComboData.getValue() + ",";
                            }
                            }
                            if (valueForReport.length() > 1) {
                                valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                            }
                            obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                        } catch (Exception ex) {
                            obj.put(varEntry.getKey(), coldata);
                        }
                    } else if (customDateFieldMap.containsKey(varEntry.getKey()) && isForReport) {
                        DateFormat df2 = userDateFormat != null?userDateFormat:new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            dateFromDB = defaultDateFormat.parse(coldata);
                            coldata = df2.format(dateFromDB);
                        } catch (ParseException p) {
                        }
                        obj.put(varEntry.getKey(), coldata);
                    } else {
                        obj.put(varEntry.getKey(), coldata != null ? coldata : "");
                    }
                    }
                }
            
            obj.put( PRTAXPERCENT,rowTaxPercent);
            if (gReceipt.isGstIncluded()) {
                obj.put("isIncludingGst", true);
            }else{
                obj.put("isIncludingGst", false);
            }
            obj.put("rowTaxAmount", rowTaxAmount);
            obj.put("taxamount", rowTaxAmount);
            //Get Product Amount entered manual Landed Cost Category in  Expense PI Line level .
//            Set<LccManualWiseProductAmount> manualProductDetailsSet = row.getLccmanualwiseproductamount() != null ? (Set<LccManualWiseProductAmount>) row.getLccmanualwiseproductamount() : null;
//            if (manualProductDetailsSet != null && !manualProductDetailsSet.isEmpty()) {
//                JSONArray manuProductDetailsJArr=new JSONArray();
//                for(LccManualWiseProductAmount lccManualWiseProductAmountObj:manualProductDetailsSet){
//                    JSONObject manuProductDetailsJOBJ=new JSONObject();
//                    manuProductDetailsJOBJ.put("id", lccManualWiseProductAmountObj.getID());
//                    manuProductDetailsJOBJ.put("billid", lccManualWiseProductAmountObj.getGrdetailid().getGoodsReceipt().getID());
//                    manuProductDetailsJOBJ.put("rowid", lccManualWiseProductAmountObj.getGrdetailid().getID());
//                    manuProductDetailsJOBJ.put("productid", lccManualWiseProductAmountObj.getGrdetailid().getInventory().getProduct().getID());
//                    manuProductDetailsJOBJ.put("billno", lccManualWiseProductAmountObj.getGrdetailid().getGoodsReceipt().getGoodsReceiptNumber());
//                    manuProductDetailsJOBJ.put("productname", lccManualWiseProductAmountObj.getGrdetailid().getInventory().getProduct().getName());
//                    manuProductDetailsJOBJ.put("enterpercentage", lccManualWiseProductAmountObj.getPercentage());
//                    manuProductDetailsJOBJ.put("enteramount", lccManualWiseProductAmountObj.getAmount());
//                    manuProductDetailsJArr.put(manuProductDetailsJOBJ);
//                }
//                //manuProductDetailsJOBJTemp.put("data", manuProductDetailsJArr);
//                obj.put("manualLandedCostCategory", manuProductDetailsJArr.toString());
//            }
            
            obj.put(PRTAXID, row.getTax()== null?"None":row.getTax().getID());
            obj.put(Constants.isUserModifiedTaxAmount, row.isIsUserModifiedTaxAmount());
//            obj.put(PRTAXID, row.getTax() != null ? (isCopyInvoice ? (row.getTax().isActivated() ? row.getTax().getID() : "") : row.getTax().getID()) : "None");//ERP-38656
            if (gReceipt.getTax() != null) {
//                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getCreationDate(), gReceipt.getTax().getID());
                taxPercent = (Double) perresult.getEntityList().get(0);
            }
            obj.put( TAXPERCENT,taxPercent);
            jArr.put(obj);
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptRows : "+ex.getMessage(), ex);
        }
        return jArr;
    }

    
    public JSONArray getGRRow(Map<String, Object> requestParams,GoodsReceiptDetail row,Map dnhm, GoodsReceipt gReceipt, KWLCurrency currency,JSONArray jArr, HashMap<String, Integer> FieldMap,HttpServletRequest request,HashMap<String, String> customFieldMap,HashMap<String, String> customDateFieldMap, CompanyAccountPreferences preferences, ExtraCompanyPreferences extraCompanyPreferences,Map<GoodsReceiptDetail, GoodsReceiptOrderDetails> goodsReceiptAndOrderMap,Map<String, List<Object[]>> baMap, Map<String, List<Object[]>> assetBaMap) throws ServiceException {
        try {
            String companyid= sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            KwlReturnObject extraCompanyObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPref = (ExtraCompanyPreferences) extraCompanyObj.getEntityList().get(0);
            int countryid = company.getCountry() != null ? Integer.parseInt(company.getCountry().getID()) : 0;
            String gcurrencyid = sessionHandlerImpl.getCurrencyID(request);
            boolean doflag = requestParams.containsKey("doflag") ? (Boolean)requestParams.get("doflag") :  false;
            boolean salesPurchaseReturnflag = false;
            boolean isCopyInvoice = false;
            boolean isBatchForProduct=false;
            boolean isSerialForProduct=false;
            boolean isWarehouseForProduct=false;
            boolean isLocationForProduct=false;
            boolean isRowForProduct = false;
            boolean isRackForProduct = false;
            boolean isBinForProduct = false;
            DateFormat userDateFormat=null;
            if(requestParams.containsKey(Constants.userdf) && requestParams.get(Constants.userdf) != null){
                userDateFormat = (DateFormat)requestParams.get(Constants.userdf);
            }
            if(requestParams.containsKey("salesPurchaseReturnflag") && requestParams.get("salesPurchaseReturnflag") != null){
                salesPurchaseReturnflag = (Boolean)requestParams.get("salesPurchaseReturnflag");
            }
            List productMasterFieldsResultList = null;
            if (requestParams.containsKey("ProductMastersFields")) {
                productMasterFieldsResultList = (List) requestParams.get("ProductMastersFields");
            }
            boolean isForDOGROLinking = (StringUtil.isNullOrEmpty(request.getParameter("isForDOGROLinking")))?false:Boolean.parseBoolean(request.getParameter("isForDOGROLinking"));
            //calculated to get Batch Details from GoodsReceiptOrderDetail--NeerajD
            if(requestParams.containsKey("isCopyInvoice") && requestParams.get("isCopyInvoice") != null){
                isCopyInvoice = Boolean.valueOf((String)requestParams.get("isCopyInvoice"));
            }
            
            GoodsReceiptOrderDetails grorderdetailrow=new GoodsReceiptOrderDetails();
            HashMap<String, Object> doRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("videtails.ID");
            order_by.add("srno");
            order_type.add("asc");
            doRequestParams.put("filter_names", filter_names);
            doRequestParams.put("filter_params", filter_params);
            doRequestParams.put("order_by", order_by);
            doRequestParams.put("order_type", order_type);

            filter_params.clear();
            filter_params.add(row.getID());
            if (!isCopyInvoice) {//do not load batch details for copy invoice
               if(goodsReceiptAndOrderMap.containsKey(row)){
                   grorderdetailrow = goodsReceiptAndOrderMap.get(row);
               }
            }
            if (isCopyInvoice && !row.getInventory().getProduct().isIsBatchForProduct() && !row.getInventory().getProduct().isIsSerialForProduct()) {//load batch details only for without batch serial product 
               if(goodsReceiptAndOrderMap.containsKey(row)){
                   grorderdetailrow = goodsReceiptAndOrderMap.get(row);
               }
            }
            DateFormat df = null;
            
            if (requestParams.containsKey("DateFormat") && requestParams.get("DateFormat") != null) {
                df = (DateFormat) requestParams.get("DateFormat");
            }
                        
            boolean isForReport = false;
            
            if(requestParams.containsKey("isForReport")){
                isForReport = (Boolean) requestParams.get("isForReport");
            }
            boolean isExport = false;
            if (request.getAttribute("isExport") != null) {
                isExport = (boolean) request.getAttribute("isExport");
            }
            
            boolean linkingFlag = requestParams.containsKey("linkingFlag")?(Boolean)requestParams.get("linkingFlag"):false;
            boolean islinkPItoCN = requestParams.containsKey("islinkPItoCN")?(Boolean)requestParams.get("islinkPItoCN"):false;
            JSONObject obj = new JSONObject();
                                
            String allocationType = request.getParameter("allocationType");
            int hsncolnum = 0,producttaxcolnum=0;
            int assethsncolnum = 0,assetproducttaxcolnum=0;
            String entityID = "";
            if (!StringUtil.isNullOrEmpty(allocationType) && allocationType.equals(Constants.LANDED_COST_ALLOCATIONTYPE_CUSTOMDUTY)) {

                if (requestParams.containsKey("producttaxcolnum") && requestParams.get("producttaxcolnum") != null) {
                    producttaxcolnum = Integer.parseInt(requestParams.get("producttaxcolnum").toString());
                }
                if (requestParams.containsKey("hsncolnum") && requestParams.get("hsncolnum") != null) {
                    hsncolnum = Integer.parseInt(requestParams.get("hsncolnum").toString());
                }
                if (requestParams.containsKey("assetproducttaxcolnum") && requestParams.get("assetproducttaxcolnum") != null) {
                    assetproducttaxcolnum = Integer.parseInt(requestParams.get("assetproducttaxcolnum").toString());
                }
                if (requestParams.containsKey("assethsncolnum") && requestParams.get("assethsncolnum") != null) {
                    assethsncolnum = Integer.parseInt(requestParams.get("assethsncolnum").toString());
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("entityValue"))) {
                    entityID = fieldDataManagercntrl.getValuesForLinkRecordsWithoutInsert(Constants.GSTModule, companyid, "Custom_Entity", request.getParameter("entityValue"), 0);
                }
            }
            // ## Get Custom Field Data 
            Map<String, Object> variableMap = new HashMap<String, Object>();
            HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
            Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
            Detailfilter_params.add(row.getID());
            invDetailRequestParams.put("filter_names", Detailfilter_names);
            invDetailRequestParams.put("filter_params", Detailfilter_params);
            String description="";
            KwlReturnObject idcustresult = accGoodsReceiptobj.getGoodsReceiptCustomData(invDetailRequestParams);
            if (idcustresult.getEntityList().size() > 0) {
                AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();

                params.put("isExport", isExport);
                params.put("isForReport", isForReport);
                params.put(Constants.userdf, userDateFormat);
                if (linkingFlag && (isForDOGROLinking || salesPurchaseReturnflag) && !isForReport) {
                    params.put("isLink", true);
                    int moduleId = gReceipt.isFixedAssetInvoice() ? Constants.Acc_FixedAssets_GoodsReceipt_ModuleId : Constants.Acc_Goods_Receipt_ModuleId;
                    if (salesPurchaseReturnflag) {
                        moduleId = Constants.Acc_Purchase_Return_ModuleId;
                    }
                    if(islinkPItoCN){
                         moduleId = Constants.Acc_Credit_Note_ModuleId;
                    }
                    params.put("linkModuleId", moduleId);
                    params.put("companyid", companyid);
                }
                fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
            }
            
            HashMap fieldrequestParams = new HashMap();
            variableMap = new HashMap<String, Object>();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
            HashMap replaceFieldMap = new HashMap<String, String>();
            FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);

            Detailfilter_names.add("productId");
            Detailfilter_params.add(row.getInventory().getProduct().getID());
            invDetailRequestParams.put("filter_names", Detailfilter_names);
            invDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresultForProduct = accInvoiceDAOobj.getInvoiceDetailsCustomDataForProduct(invDetailRequestParams);
            AccJEDetailsProductCustomData accJEDetailsProductCustomData = null;
            if (idcustresultForProduct.getEntityList().size() > 0) {
                accJEDetailsProductCustomData = (AccJEDetailsProductCustomData) idcustresultForProduct.getEntityList().get(0);
            }

            if (accJEDetailsProductCustomData != null) {
                JSONObject params = new JSONObject();
                params.put("isExport", isExport);
                params.put("isForReport", isForReport);
                params.put(Constants.userdf, userDateFormat);
                
                setCustomColumnValuesForProduct(accJEDetailsProductCustomData, FieldMap, replaceFieldMap, variableMap, params);
                for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                    String coldata = varEntry.getValue().toString();
                    if (!StringUtil.isNullOrEmpty(coldata)) {
                        obj.put(varEntry.getKey(), coldata);
                    }
                }
            }

            // ## End Custom Field Data
                    
            obj.put(BILLID, gReceipt.getID());
            obj.put(BILLNO, gReceipt.getGoodsReceiptNumber());
            String currencyid = (gReceipt.getCurrency() == null ? currency.getCurrencyID() : gReceipt.getCurrency().getCurrencyID());
            obj.put(CURRENCYSYMBOL, (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol()));
            obj.put("currencyCode", (gReceipt.getCurrency() == null ? currency.getCurrencyCode() : gReceipt.getCurrency().getCurrencyCode()));
//            KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
            KwlReturnObject bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams, 1.0, currencyid, gReceipt.getCreationDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
            obj.put(OLDCURRENCYRATE, (Double) bAmt.getEntityList().get(0));
            if (gReceipt.isFixedAssetInvoice()) {
                obj.put("externalcurrencyrate", gReceipt.getJournalEntry().getExternalCurrencyRate());
            }
            Inventory inv = row.getInventory();
            Product prod = inv.getProduct();
            if (productMasterFieldsResultList != null) {
                CommonFunctions.getterMethodForProductsData( prod, productMasterFieldsResultList, obj);
            }
            obj.put(SRNO, row.getSrno());
            obj.put(ROWID, row.getID());
            obj.put("joborderdetail", row.getPurchaseorderdetail()!=null?row.getPurchaseorderdetail().getID():"");
            obj.put("isJobWorkOutProd", row.getGoodsReceipt().isIsJobWorkOutInv());
            obj.put("originalTransactionRowid", row.getID());
            obj.put(PRODUCTID, prod.getID());
            obj.put("location", prod.getLocation()!=null ? prod.getLocation().getId() : "");    // product default location (ERP-37135)
            obj.put("warehouse", prod.getWarehouse()!=null ? prod.getWarehouse().getId() : "");  // product default warehouse
            obj.put("purchasetaxId", prod.getPurchasetaxid());
            obj.put("salestaxId", prod.getSalestaxid());
            obj.put("barcodetype", prod.getBarcodefield());  //ERM-304
            obj.put("salesAccountId",prod.getPurchaseReturnAccount().getID());
            obj.put("isAsset",prod.isAsset());
            obj.put("discountAccountId",preferences.getDiscountReceived().getID());
            obj.put("accountId",preferences.getDiscountReceived().getID());
            obj.put("natureofpayment",row.getNatureOfPayment()!=null?row.getNatureOfPayment().getID():"");
            obj.put("productname", prod.getProductName());
            obj.put(PRODUCTNAME, prod.getName());
            obj.put("productType",prod.getProducttype()==null?"":prod.getProducttype().getName());
            obj.put("gstCurrencyRate", row.getGstCurrencyRate());
            String uom=inv.getUom()!=null?inv.getUom().getNameEmptyforNA():prod.getUnitOfMeasure()==null?"":prod.getUnitOfMeasure().getNameEmptyforNA();
            obj.put(UNITNAME, uom);
            obj.put("uomname", uom);
            obj.put("multiuom", prod.isMultiuom());
            obj.put("baseuomname", prod.getUnitOfMeasure()==null?"":prod.getUnitOfMeasure().getNameEmptyforNA());
            obj.put("productaccountid", (row.getPurchaseJED()!=null && !StringUtil.isNullOrEmpty(row.getPurchaseJED().getID())) ? (row.getPurchaseJED().getAccount() != null ? row.getPurchaseJED().getAccount().getID(): "" ): "");
            obj.put("productsalesaccountid", prod.getSalesAccount() == null?"":prod.getSalesAccount().getID());
            obj.put("productpurchaseaccountid", prod.getPurchaseAccount()==null?"":prod.getPurchaseAccount().getID());
            obj.put("hasAccess",prod.isIsActive());
            String productsBaseUomId = (prod.getUnitOfMeasure() == null) ? "" : prod.getUnitOfMeasure().getID();
            String selectedUomId = (inv.getUom() != null) ? inv.getUom().getID() : "";

            if (prod.isblockLooseSell() && !productsBaseUomId.equals(selectedUomId)) {
                // Get Available Quantity of Product For Selected UOM

                KwlReturnObject qtyResult = accProductObj.getAvailableQuantityInSelectedUOM(prod.getID(), selectedUomId);
                double availableQuantity = qtyResult.getEntityList().get(0) == null ? 0 : (Double) qtyResult.getEntityList().get(0);
                obj.put("availableQtyInSelectedUOM", availableQuantity);
                obj.put("isAnotherUOMSelected", true);

                // Getting Open PO/SO count

                HashMap<String, Object> orderParams = new HashMap<String, Object>();
                orderParams.put("companyid", companyid);
                orderParams.put("gcurrencyid", gcurrencyid);
                orderParams.put("df", authHandler.getDateOnlyFormatter(request));
                orderParams.put("pendingapproval", false);
                orderParams.put("startdate", authHandler.getDates(preferences.getFinancialYearFrom(), true));
                orderParams.put("enddate", authHandler.getDates(preferences.getFinancialYearFrom(), false));
                orderParams.put("currentuomid", selectedUomId);
                orderParams.put("productId", prod.getID());

                double pocountinselecteduom = accSalesOrderServiceDAOobj.getPOCount(orderParams);

                double socountinselecteduom = accSalesOrderServiceDAOobj.getSOCount(orderParams);

                obj.put("pocountinselecteduom", pocountinselecteduom);
                obj.put("socountinselecteduom", socountinselecteduom);

            }
            
            if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {  //This is sats specific code 		 
                obj.put("dependentType", row.getDependentType());
                try {
                    String numberVal = row.getDependentType();
                    obj.put("dependentTypeNo", Integer.parseInt(numberVal));
                } catch (Exception e) {
                }
                obj.put("inouttime", StringUtil.isNullOrEmpty(row.getInouttime()) ? "" : row.getInouttime().replaceAll("%20", " "));
                if (!StringUtil.isNullOrEmpty(row.getInouttime())) {
                    String interVal = getTimeIntervalForProduct(row.getInouttime());
                    obj.put("timeinterval", interVal);
                }
                obj.put("parentid", ((prod.getParent() != null) ? prod.getParent().getID() : ""));
                obj.put("parentname", ((prod.getParent() != null) ? prod.getParent().getName() : ""));
                if (prod.getParent() != null) {
                    obj.put("issubproduct", true);
                }
                if (prod.getChildren().size() > 0) {
                    obj.put("isparentproduct", true);
                } else {
                    obj.put("isparentproduct", false);
                }
            }
				
//            obj.put(RATEINBASE,accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,(row.getRate()),row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getJournalEntry().getEntryDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
            obj.put(RATEINBASE,accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,(row.getRate()),row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getCreationDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
            obj.put(PID,prod.getProductid());
            obj.put(TYPE,prod.getProducttype()==null?"":prod.getProducttype().getName());
            obj.put("permit", row.getPermit() !=null?row.getPermit():"");
            obj.put("supplierpartnumber", StringUtil.isNullOrEmpty(row.getSupplierpartnumber())?"":row.getSupplierpartnumber());
            obj.put("invstore",(StringUtil.isNullOrEmpty(row.getInvstoreid()))?"":row.getInvstoreid());
            obj.put("invlocation",(StringUtil.isNullOrEmpty(row.getInvlocid()))?"":row.getInvlocid());
            
            if (gReceipt.isFixedAssetInvoice()) {
                getAssetDetailForGRRows(obj, companyid, linkingFlag, row, df,request, assetBaMap);
            }
            Discount disc = row.getDiscount();
            if (disc != null ) {
      
                /* In exported file showing discount value with proper % or currency symbol 
                 same as expander
                        
                 */
                if (isExport) {
                    obj.put(PRDISCOUNT, disc.isInPercent() ? (disc.getDiscount() + "%") : (gReceipt.getCurrency() == null ? currency.getSymbol() : gReceipt.getCurrency().getSymbol())+ " " + disc.getDiscount());
                } else {
                    obj.put(PRDISCOUNT, disc.getDiscount());
                }
                
                obj.put("discountvalue", disc.getDiscountValue());
//                obj.put("discountvalueinbase",accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,(disc.getDiscountValue()),row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getJournalEntry().getEntryDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
                obj.put("discountvalueinbase",accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,(disc.getDiscountValue()),row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getCreationDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
                obj.put("discountispercent", disc.isInPercent() ? 1 : 0); 
            } else {
                obj.put(PRDISCOUNT, 0);
                obj.put("discountispercent", 1);
            }
            if (!linkingFlag) {
                if (row.getGoodsReceiptOrderDetails() != null) {
                    obj.put("linkto", row.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber());
                    obj.put("grodate", row.getGoodsReceiptOrderDetails().getGrOrder().getOrderDate());
                    obj.put("linkid", row.getGoodsReceiptOrderDetails().getGrOrder().getID());
                    obj.put("rowid", row.getGoodsReceiptOrderDetails().getID());
                    obj.put("savedrowid", row.getGoodsReceiptOrderDetails().getID());
                    /*
                       ERM-1037
                       Field used for comparing dates to restrict linking of future doument date in Goods receipt document editing
                    */
                    obj.put("linkDate", row.getGoodsReceiptOrderDetails().getGrOrder().getOrderDate());
                    obj.put("docrowid", row.getID());
                    obj.put("originalTransactionRowid", row.getID());
                    obj.put("linktype", 1);
                } else if (row.getPurchaseorderdetail() != null) {
                    obj.put("linkto", row.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber());
                    obj.put("linkid", row.getPurchaseorderdetail().getPurchaseOrder().getID());
                    /*
                       ERM-1037
                       Field used for comparing dates to restrict linking of future doument date in Goods receipt document editing
                    */
                    obj.put("linkDate", row.getPurchaseorderdetail().getPurchaseOrder().getOrderDate());
                    obj.put("rowid", row.getPurchaseorderdetail().getID());
                    obj.put("savedrowid", row.getPurchaseorderdetail().getID());
                    obj.put("docrowid", row.getID());
                    obj.put("linktype", 0);
                } else if (row.getVendorQuotationDetail() != null) {
                    obj.put("linkto", row.getVendorQuotationDetail().getVendorquotation().getQuotationNumber());
                    obj.put("linkid", row.getVendorQuotationDetail().getVendorquotation().getID());
                    /*
                       ERM-1037
                       Field used for comparing dates to restrict linking of future doument date in Goods receipt document editing
                    */
                    obj.put("linkDate", row.getVendorQuotationDetail().getVendorquotation().getQuotationDate());
                    obj.put("rowid", row.getVendorQuotationDetail().getID());
                    obj.put("savedrowid", row.getVendorQuotationDetail().getID());
                    obj.put("docrowid", row.getID());
                    obj.put("linktype", 2);
                } else {
                    obj.put("linkto", "");
                    obj.put("linkid", "");
                    obj.put("linktype", -1);
                }
            }
            
            if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                description = row.getDescription();
            } else if (!StringUtil.isNullOrEmpty(inv.getProduct().getDescription())) {
                description = inv.getProduct().getDescription();
            } else {
                description = "";
            }
            
            obj.put(DESC, StringUtil.DecodeText(description));                    
            obj.put("description", StringUtil.DecodeText(description));
            double unitprice = row.getRate();
            if (row.getGoodsReceipt().isIsJobWorkOutInv() && row.getInventory().getProduct().getProducttype().getID().equalsIgnoreCase(Producttype.ASSEMBLY)) {
                /**
                 * If GR is Job Work Out then return purchase price = Sum of
                 * Purchase Price of All components (Which are define while
                 * Assembly )
                 */
                unitprice = accProductObj.getSumOfPurchasePriceForSubassebmblyItems(row.getInventory().getProduct().getID());
            }
            obj.put(RATE, unitprice);
            obj.put("priceSource", row.getPriceSource() != null? row.getPriceSource() : "");
             if (row.getPricingBandMasterid() != null) {
                KwlReturnObject PricebandResult = accountingHandlerDAOobj.getObject(PricingBandMaster.class.getName(), row.getPricingBandMasterid());
                PricingBandMaster pricingBandMaster = PricebandResult != null ? (PricingBandMaster) PricebandResult.getEntityList().get(0) : null;
                obj.put("pricingbandmasterid", pricingBandMaster != null ? pricingBandMaster.getID() : "");
                obj.put("pricingbandmastername", pricingBandMaster != null ? pricingBandMaster.getName() : "");
            }
            /**
             * get the volume discount discount for the given product according its quantity.
             */
            HashMap<String, Object> pricingDiscountRequestParams = new HashMap<String, Object>();
            pricingDiscountRequestParams.put("isPricePolicyUseDiscount", true);
            pricingDiscountRequestParams.put("productID", row.getInventory().getProduct().getID());
            pricingDiscountRequestParams.put("isPurchase", true);
            pricingDiscountRequestParams.put("companyID", companyid);
            pricingDiscountRequestParams.put("currencyID", currencyid);
            Double qty = Double.valueOf(row.getInventory().getQuantity());
            pricingDiscountRequestParams.put("quantity", Integer.valueOf(qty.intValue()));
            /**
             * check Volume discount matches with qty
             */
            KwlReturnObject volDiscresult = accProductObj.getProductPriceFromPriceListVolumeDiscount(pricingDiscountRequestParams);
            if (volDiscresult != null && volDiscresult.getEntityList() != null && !volDiscresult.getEntityList().isEmpty()) {
                Object[] rowObj = (Object[]) volDiscresult.getEntityList().get(0);

                KwlReturnObject detailResult = accountingHandlerDAOobj.getObject(PricingBandMasterDetail.class.getName(), (String) rowObj[5]);
                PricingBandMasterDetail pricingBandMasterDetail = detailResult != null ? (PricingBandMasterDetail) detailResult.getEntityList().get(0) : null;
                if (pricingBandMasterDetail != null) {
                    obj.put("volumdiscountid", pricingBandMasterDetail.getPricingBandMaster().getID());
                }
            }
            isBatchForProduct = prod.isIsBatchForProduct();
            isSerialForProduct = prod.isIsSerialForProduct();
            isLocationForProduct = prod.isIslocationforproduct();
            isWarehouseForProduct = prod.isIswarehouseforproduct();
            isRowForProduct = prod.isIsrowforproduct();
            isRackForProduct = prod.isIsrackforproduct();
            isBinForProduct = prod.isIsbinforproduct();
            if (grorderdetailrow!= null &&  grorderdetailrow.getProduct() !=null) {
                    Product product = grorderdetailrow.getProduct();
                       if(extraCompanyPreferences!=null && extraCompanyPreferences.getUomSchemaType()==Constants.PackagingUOM){
                        obj.put("caseuom", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null)?product.getPackaging().getCasingUoM().getID():"");
                        obj.put("caseuomvalue", (product.getPackaging()!=null && product.getPackaging().getCasingUoM()!=null)?product.getPackaging().getCasingUomValue():1);
                        obj.put("inneruom", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null)?product.getPackaging().getInnerUoM().getID():"");
                        obj.put("inneruomvalue", (product.getPackaging()!=null && product.getPackaging().getInnerUoM()!=null)?product.getPackaging().getInnerUomValue():1);
                        obj.put("stockuom", (product.getUnitOfMeasure()!=null)?product.getUnitOfMeasure().getID():"");
                   }
            }
            if ((isForDOGROLinking ||salesPurchaseReturnflag )&& ((isLocationForProduct && inv.getProduct().getLocation() != null && !StringUtil.isNullOrEmpty(inv.getProduct().getLocation().getId())) || (isWarehouseForProduct && inv.getProduct().getWarehouse() != null && !StringUtil.isNullOrEmpty(inv.getProduct().getWarehouse().getId()))) &&  !isBatchForProduct && !isSerialForProduct) {
                obj.put("batchdetails", getdefaultBatchJson(inv.getProduct(), request, row.getID(), inv.getQuantity()));
            }
            if (grorderdetailrow != null && grorderdetailrow.getProduct() != null) {  //only if auto generate GRN option is on
                if (!isCopyInvoice && preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory() || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory() || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                    if (isBatchForProduct || isSerialForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                        obj.put("batchdetails", getNewBatchJson(grorderdetailrow.getProduct(), request, grorderdetailrow.getID(), false, baMap));
                    }
                } 
                if (isCopyInvoice && isLocationForProduct && isWarehouseForProduct && !isSerialForProduct && !isBatchForProduct) {  //copy batch details for without batch serial product
                    obj.put("batchdetails", getNewBatchJson(grorderdetailrow.getProduct(), request, grorderdetailrow.getID(), false, baMap));
                }
            }
            if (grorderdetailrow != null && !linkingFlag && !isCopyInvoice) {
                obj.put("grorowid", grorderdetailrow.getID());
            }
            obj.put("isLocationForProduct", isLocationForProduct);
            obj.put("isWarehouseForProduct", isWarehouseForProduct);
            obj.put("isBatchForProduct", isBatchForProduct);
            obj.put("isSerialForProduct", isSerialForProduct);
            obj.put("isRackForProduct", isRackForProduct);              //SDP-63
            obj.put("isRowForProduct", isRowForProduct);
            obj.put("isBinForProduct", isBinForProduct);
            double quantity = row.getInventory().getQuantity();
            if (storageHandlerImpl.GetSATSCompanyId().contains(companyid) && row.getShowquantity() != null) {  //This is sats specific code 	 
                obj.put("showquantity", StringUtil.DecodeText(row.getShowquantity()));
            }
            boolean cnAgainstVenGstflag=!StringUtil.isNullOrEmpty(request.getParameter("cnAgainstVenGstflag"))?Boolean.parseBoolean(request.getParameter("cnAgainstVenGstflag")):false;
            if(salesPurchaseReturnflag && !cnAgainstVenGstflag){            //should not get details of purchase return for credit noteagainst vendor for malaysian country ERP-27284 / ERP-28249
                quantity = getGoodsReceiptDetailStatusForPR(row);  
            }
            obj.put(QUANTITY, quantity);            
            obj.put("dquantity", quantity);
            double baseuomrate = row.getInventory().getBaseuomrate();
            double invoiceRowProductQty = quantity*baseuomrate;
            double remainedQty = invoiceRowProductQty;// which has not been linked yet
            if(row.getInventory().getUom()!=null) {
                obj.put("uomid", row.getInventory().getUom().getID());                        
            } else {
                obj.put("uomid", row.getInventory().getProduct().getUnitOfMeasure()!=null?row.getInventory().getProduct().getUnitOfMeasure().getID():"");                        
            }
            obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(quantity,baseuomrate, companyid));
            obj.put("baseuomrate", baseuomrate);
            obj.put("isconsignment", gReceipt.isIsconsignment());
                    
            double remainingquantity = 0;

            double amount = 0;
            if (dnhm.containsKey(row)) {
                Object[] val = (Object[]) dnhm.get(row);
                remainingquantity = (Double) val[1];
                obj.put( REMAININGQUANTITY,remainingquantity);
                obj.put(REMQUANTITY, 0);

            }
            double rowTaxAmount=0;
            Map amthm = getGoodsReceiptProductAmount(gReceipt, companyid);
            Object[] val = (Object[]) amthm.get(row);
            amount = (Double) val[0];
            rowTaxAmount = (Double) val[2];
            obj.put( ORIGNALAMOUNT,amount);
            obj.put( AMOUNT, amount);
//            obj.put( AMOUNTINBASE , accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,amount,row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getJournalEntry().getEntryDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
            obj.put( AMOUNTINBASE , accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,amount,row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getCreationDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
            double taxPercent = 0;
            double rowTaxPercent = 0;
            if (row.getTax() != null) {
//                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), row.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getCreationDate(), row.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
            }
            obj.put( PRTAXPERCENT,rowTaxPercent);
            if (extraCompanyPref.getLineLevelTermFlag()==1) {
                obj.put("taxamount", row.getRowTermAmount());
                obj.put("taxamountforlinking", row.getRowTermAmount());
                obj.put("rowTaxAmount", row.getRowTermAmount());
            } else {
                obj.put("taxamount", rowTaxAmount);
                obj.put("taxamountforlinking", rowTaxAmount);
                obj.put("rowTaxAmount", rowTaxAmount);
            }
            obj.put("rowTaxPercent", rowTaxPercent);
//            obj.put("taxamountinbase", accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,rowTaxAmount,row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getJournalEntry().getEntryDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
            obj.put("taxamountinbase", accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,rowTaxAmount,row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getCreationDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
            obj.put("rateIncludingGst", authHandler.roundUnitPrice(row.getRateincludegst(), companyid));
            obj.put("israteIncludingGst", gReceipt.isGstIncluded());
            obj.put("includeprotax", row.getTax()!=null?true:false);
            obj.put(Constants.isUserModifiedTaxAmount, row.isIsUserModifiedTaxAmount());
            
            if(isCopyInvoice && row.getTdsJEMapping() != null && !StringUtil.isNullOrEmpty(row.getTdsJEMapping().getID())){
                obj.put("appliedTDS", "");
                obj.put("tdsamount", 0.0);
            }else if (row.getGoodsReceipt().isIsTDSApplicable() && row.getNatureOfPayment()!=null) {// TDS Details
                JSONArray jrrAppliedTDS = new JSONArray();
                JSONObject jobjAppliedTDS = new JSONObject();
                KwlReturnObject deducteeTypeObj = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), row.getGoodsReceipt().getVendor().getDeducteeType());
                MasterItem deducteeTypeMI = (MasterItem) deducteeTypeObj.getEntityList().get(0);
                if (deducteeTypeMI != null) {
                    jobjAppliedTDS.put("tdsAssessableAmount", row.getTdsAssessableAmount());// On which TDS is Applied
                    jobjAppliedTDS.put("deducteetypename", deducteeTypeMI.getValue());
                    jobjAppliedTDS.put("amount", amount);
                    jobjAppliedTDS.put("enteramount", amount);
                    jobjAppliedTDS.put("ruleid", row.getTdsRuleId());
                    jobjAppliedTDS.put("tdsamount", row.getTdsLineAmount());
                    jobjAppliedTDS.put("rowid", row.getTdsRuleId());
                    jobjAppliedTDS.put("tdspercentage", row.getTdsRate());
                    jobjAppliedTDS.put("natureofpayment", row.getNatureOfPayment()!=null?row.getNatureOfPayment().getID():"");
                    jobjAppliedTDS.put("natureofpaymentName", row.getNatureOfPayment()!=null?row.getNatureOfPayment().getCode() + " - " + row.getNatureOfPayment().getValue():"");
                    jobjAppliedTDS.put("tdsaccountid", row.getTdsPayableAccount()!=null?row.getTdsPayableAccount().getID():"");
                    if (row.getGoodsReceiptDetailPaymentMapping() != null && row.getGoodsReceiptDetailPaymentMapping().size()>0) {
                        JSONArray jrrAdvancePaymentTDS = new JSONArray();
                        Set<GoodsReceiptDetailPaymentMapping> paymentDetailsSet = row.getGoodsReceiptDetailPaymentMapping();
                        for (GoodsReceiptDetailPaymentMapping mappingDetails : paymentDetailsSet) {
                            JSONObject jobjAdvancePaymentTDS = new JSONObject();
                            jobjAdvancePaymentTDS.put("goodsReceiptDetailsAdvancePaymentId",mappingDetails.getPayment());
                            jobjAdvancePaymentTDS.put("adjustedAdvanceTDSamount",mappingDetails.getAdvanceAdjustedAmount());
                            jobjAdvancePaymentTDS.put("paymentamount",mappingDetails.getPaymentAmount());
                            jrrAdvancePaymentTDS.put(jobjAdvancePaymentTDS);
                        }
                        jobjAppliedTDS.put("advancePaymentDetails", jrrAdvancePaymentTDS.toString());
                    }
                    
                    jrrAppliedTDS.put(jobjAppliedTDS);
                    obj.put("appliedTDS", jrrAppliedTDS.toString());
                    obj.put("tdsamount", row.getTdsLineAmount());
                    obj.put("tdsjemappingID", row.getTdsJEMapping() != null ? row.getTdsJEMapping().getID() : "");
                }
            }
            
            obj.put(PRTAXID, row.getTax()== null?"None":row.getTax().getID());
//            obj.put(PRTAXID, row.getTax() != null ? (isCopyInvoice || linkingFlag ? (row.getTax().isActivated() ? row.getTax().getID() : "") : row.getTax().getID()) : "None");//ERP-38656
            
            double discountValueForExcel = 0, amountForExcelFile = 0;
            double rowamountwithgst = 0;
            if (row.getGoodsReceipt().isGstIncluded()) {//if gstincluded is the case
                rowamountwithgst = authHandler.round(row.getRateincludegst() * quantity, companyid);
                discountValueForExcel = obj.optDouble("discountvalue", 0.0);
                amountForExcelFile = rowamountwithgst - discountValueForExcel;
                obj.put(Constants.amountForExcelFile, authHandler.formattedAmount(amountForExcelFile, companyid));
            } else {
                rowamountwithgst = authHandler.round(row.getRate()* quantity, companyid);
                discountValueForExcel = obj.optDouble("discountvalue", 0.0);
                amountForExcelFile = rowamountwithgst- discountValueForExcel + rowTaxAmount;
                obj.put(Constants.amountForExcelFile, authHandler.formattedAmount(amountForExcelFile, companyid));
            }
            
            obj.put("recTermAmount", row.getRowTermAmount());
            obj.put("OtherTermNonTaxableAmount", row.getOtherTermNonTaxableAmount());
            JSONArray TermdetailsjArr = new JSONArray();
            if(extraCompanyPref.getLineLevelTermFlag()==1){ // For India Country 
                HashMap<String, Object> GoodsReceiptDetailParams = new HashMap<String, Object>();
                GoodsReceiptDetailParams.put("GoodsReceiptDetailid", row.getID());
                KwlReturnObject grdTermMapresult = accGoodsReceiptobj.getGoodsReceiptdetailTermMap(GoodsReceiptDetailParams);
                List<ReceiptDetailTermsMap> ReceiptDetailTermsMapList = grdTermMapresult.getEntityList();
                for (ReceiptDetailTermsMap invoicedetailTermMap : ReceiptDetailTermsMapList) {
                    LineLevelTerms mt = invoicedetailTermMap.getTerm();
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("id", mt.getId());
                    jsonobj.put("termid", mt.getId());
                    /**
                     * ERP-32829 
                     */
                    jsonobj.put("productentitytermid", invoicedetailTermMap.getEntitybasedLineLevelTermRate()!=null?invoicedetailTermMap.getEntitybasedLineLevelTermRate().getId():"");
                    jsonobj.put("isDefault", invoicedetailTermMap.isIsGSTApplied());
                    jsonobj.put("term", mt.getTerm());
                    jsonobj.put("glaccount", mt.getAccount().getID());
                    jsonobj.put("glaccountname", mt.getAccount().getAccountName());
                    jsonobj.put("acccode", mt.getAccount().getAccountCode());
                    jsonobj.put("accountid", mt.getAccount().getID());
                    jsonobj.put("IsOtherTermTaxable", mt.isOtherTermTaxable());
                    jsonobj.put("sign", mt.getSign());
                    jsonobj.put("formula", mt.getFormula());
                    jsonobj.put("formType", !StringUtil.isNullOrEmpty(mt.getFormType())?mt.getFormType():"1");
                    jsonobj.put("formulaids", mt.getFormula());
                    jsonobj.put("originalTermPercentage", mt.getPercentage()); // For service abatement calculation
                    jsonobj.put("termpercentage", invoicedetailTermMap.getPercentage());
                    jsonobj.put("termamount", invoicedetailTermMap.getTermamount());
                    jsonobj.put("assessablevalue", invoicedetailTermMap.getAssessablevalue());
                    jsonobj.put("purchasevalueorsalevalue", invoicedetailTermMap.getPurchaseValueOrSaleValue());
                    jsonobj.put("deductionorabatementpercent", invoicedetailTermMap.getDeductionOrAbatementPercent());
                    jsonobj.put("taxtype", invoicedetailTermMap.getTaxType());
                    jsonobj.put("taxvalue", invoicedetailTermMap.getTaxType() == IndiaComplianceConstants.Term_TaxType_Flat ? invoicedetailTermMap.getTermamount() : invoicedetailTermMap.getPercentage());
                    jsonobj.put("includeInTDSCalculation", mt.isIncludeInTDSCalculation());
//                    jsonobj.put("taxvalue", invoicedetailTermMap.getTaxType()==0 ? invoicedetailTermMap.getTermamount() : invoicedetailTermMap.getPercentage());
                    jsonobj.put("termtype", invoicedetailTermMap.getTerm().getTermType());
                    jsonobj.put("termsequence", invoicedetailTermMap.getTerm().getTermSequence());
                    jsonobj.put("payableaccountid", mt.getPayableAccount() != null ? mt.getPayableAccount().getID() : "");
                    jsonobj.put(IndiaComplianceConstants.GST_CESS_TYPE, invoicedetailTermMap.getEntitybasedLineLevelTermRate()!=null && invoicedetailTermMap.getEntitybasedLineLevelTermRate().getCessType()!=null ? invoicedetailTermMap.getEntitybasedLineLevelTermRate().getCessType().getId() : "");
                    jsonobj.put(IndiaComplianceConstants.GST_CESS_VALUATION_AMOUNT, invoicedetailTermMap.getEntitybasedLineLevelTermRate()!=null ? invoicedetailTermMap.getEntitybasedLineLevelTermRate().getValuationAmount() : 0.0);
                    jsonobj.put(IndiaComplianceConstants.DEFAULT_TERMID, mt!=null && mt.getDefaultTerms()!=null ? mt.getDefaultTerms().getId() : "");
                    TermdetailsjArr.put(jsonobj);
                }
                if (!StringUtil.isNullOrEmpty(allocationType) && allocationType.equals(Constants.LANDED_COST_ALLOCATIONTYPE_CUSTOMDUTY)) {
                    JSONObject reqParams = new JSONObject();
                    reqParams.put("productid", prod.getID());
                    reqParams.put("isFixedAsset", gReceipt.isFixedAssetInvoice());
                    if (!StringUtil.isNullOrEmpty(request.getParameter("billdate"))) {
                        reqParams.put("applieddate", request.getParameter("billdate"));
                        reqParams.put("df", authHandler.getDateOnlyFormat());
                    }
                    reqParams.put("companyid", companyid);
                    accEntityGstDao.getProductTaxClassOnDate(reqParams);
                    if (gReceipt.isFixedAssetInvoice()) {
                        List list = fieldManagerDAOobj.getFieldComboValue(assethsncolnum, prod.getID());
                        if (list != null && !list.isEmpty()) {
                            Object[] arr = (Object[]) list.get(0);
                            String hsncode = (String) arr[0];
                            obj.put("hsncode", hsncode);
                        }
                        list = fieldManagerDAOobj.getFieldComboValue(assetproducttaxcolnum, prod.getID());
                        if (list != null && !list.isEmpty()) {
                            Object[] arr = (Object[]) list.get(0);
                            String producttaxclassvalue = (String) arr[0];
                            obj.put("producttaxclass", producttaxclassvalue);
                        }
                    } else {
                        List list = fieldManagerDAOobj.getFieldComboValue(hsncolnum, prod.getID());
                        if (list != null && !list.isEmpty()) {
                            Object[] arr = (Object[]) list.get(0);
                            String hsncode = (String) arr[0];
                            obj.put("hsncode", hsncode);
                        }
                        list = fieldManagerDAOobj.getFieldComboValue(producttaxcolnum, prod.getID());
                        if (list != null && !list.isEmpty()) {
                            Object[] arr = (Object[]) list.get(0);
                            String producttaxclassvalue = (String) arr[0];
                            obj.put("producttaxclass", producttaxclassvalue);
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(reqParams.optString("productcategory", null))) {
                        Map<String, Object> mapData = new HashMap<>();
                        mapData.put("isProdCategoryPresent", true);
                        mapData.put("productcategory", reqParams.optString("productcategory", null));
                        mapData.put("salesOrPurchase", false);
                        mapData.put("entity", entityID);
                        mapData.put("companyid", companyid);
                        if (!StringUtil.isNullOrEmpty(request.getParameter("billdate"))) {
                            mapData.put("applieddate", authHandler.getDateOnlyFormat().parse(request.getParameter("billdate")));
                        }
                        mapData.put("defaulttermid", LineLevelTerms.GSTName.get("InputIGST"));
                        KwlReturnObject result = accEntityGstDao.getEntityBasedTermRate(mapData);
                        if (result.getEntityList() != null && result.getEntityList().size() > 0 && result.getEntityList().get(0) != null) {
                            ArrayList<EntitybasedLineLevelTermRate> productTermDetails = (ArrayList<EntitybasedLineLevelTermRate>) result.getEntityList();
                            if (productTermDetails != null && !productTermDetails.isEmpty()) {
                                EntitybasedLineLevelTermRate elltr = productTermDetails.get(0);
                                if (elltr != null) {
                                    obj.put("igstrate", elltr.getPercentage());
                                }
                            }
                        }
                    }
                }
                KwlReturnObject kwlReturnObject = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences extraPreferences = (ExtraCompanyPreferences) kwlReturnObject.getEntityList().get(0);                
                boolean carryin = true;
                String uomid= prod.getUnitOfMeasure()==null?"":prod.getUnitOfMeasure().getID();
                // Excise AND VAT special type TAX ------ START-------
                if(extraPreferences.isExciseApplicable()){
                    String reortingUOM= (row.getReportingUOMExcise()!=null)?row.getReportingUOMExcise().getID():"";
                    String valuationType=!StringUtil.isNullOrEmpty(row.getExciseValuationType())?row.getExciseValuationType():"";
                    obj.put("valuationType",valuationType);
                    if ((Constants.QUENTITY).equals(valuationType)) {
                           obj.put("compairwithUOM", 1);
                           obj.put("reortingUOMExcise",reortingUOM);
                        
                        if (row.getReportingSchemaTypeExcise() != null && !reortingUOM.equals(uomid)) {
                            String reportinguomschema = row.getReportingSchemaTypeExcise().getID();
                             obj.put("reortingUOMSchemaExcise",reportinguomschema);
                            HashMap<String, Object> hsMap = new HashMap<String, Object>();
                            hsMap.put("uomschematypeid", reportinguomschema);
                            hsMap.put("currentuomid", uomid);
                            hsMap.put("carryin", carryin);
                            hsMap.put("companyid", companyid);
                            KwlReturnObject convertor = accProductObj.getProductBaseUOMRate(hsMap);
                            List list = convertor.getEntityList();
                            Iterator itrList = list.iterator();
                            if (itrList.hasNext()) {
                                UOMSchema rowUOMExcise = (UOMSchema) itrList.next();
                                if (rowUOMExcise != null) {
                                    obj.put("compairwithUOM", rowUOMExcise.getBaseuomrate());
                                }
                            }     
                        }
                        
                    }else if((Constants.MRP).equals(valuationType)){
                        obj.put("productMRP",row.getMrpIndia());
                    }
                }
                if(extraPreferences.isEnableVatCst()){
                    String reortingUOMVAT= (row.getReportingUOMVAT()!=null)? row.getReportingUOMVAT().getID():"";
                    String valuationTypeVAT=!StringUtil.isNullOrEmpty(row.getVatValuationType())?row.getVatValuationType():"";
                    obj.put("valuationTypeVAT",valuationTypeVAT);
                    if ((Constants.QUENTITY).equals(valuationTypeVAT)) {
                           obj.put("reportingUOMVAT",reortingUOMVAT);
                           obj.put("compairwithUOMVAT", 1);
                        
                        if (row.getReportingSchemaVAT() != null && !reortingUOMVAT.equals(uomid)) {
                            String reportinguomschema = row.getReportingSchemaVAT().getID();
                            obj.put("reportingUOMSchemaVAT",reportinguomschema);
                            HashMap<String, Object> hsMap = new HashMap<String, Object>();
                            hsMap.put("uomschematypeid", reportinguomschema);
                            hsMap.put("currentuomid", uomid);
                            hsMap.put("carryin", carryin);
                            hsMap.put("companyid", companyid);
                            KwlReturnObject convertor = accProductObj.getProductBaseUOMRate(hsMap);
                            List list = convertor.getEntityList();
                            Iterator itrList = list.iterator();
                            if (itrList.hasNext()) {
                                UOMSchema rowUOMVAT = (UOMSchema) itrList.next();
                                if (rowUOMVAT != null) {
                                    obj.put("compairwithUOMVAT", rowUOMVAT.getBaseuomrate());
                                }
                            }
                        }
                        
                    }else if((Constants.MRP).equals(valuationTypeVAT)){
                        obj.put("productMRP",row.getMrpIndia());
                    }
                }
                if (row.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                    /**
                     * Put GST Tax Class History.
                     */
                    obj.put("refdocid", row.getID());
                    fieldDataManagercntrl.getGSTTaxClassHistory(obj);
                }
                obj.put("itctype", row.getItcType());  // used for India GST ITC Flow.

            }
            obj.put("LineTermdetails",TermdetailsjArr.toString());
            if (extraCompanyPreferences.isExciseApplicable() && company.getCountry()!=null && Integer.parseInt(company.getCountry().getID())==Constants.indian_country_id) {
                HashMap<String, Object> GoodsReceiptDetailParams = new HashMap<String, Object>();
                JSONArray dealerTermdetailsArr = new JSONArray();
                GoodsReceiptDetailParams.put("GoodsReceiptDetailid", row.getID());
                GoodsReceiptDetailParams.put("companyid", companyid);
                KwlReturnObject grdDealerExciseresult = accGoodsReceiptobj.getDealerExciseDetails(GoodsReceiptDetailParams);
                List<DealerExciseDetails> dealerExciseresultList = grdDealerExciseresult.getEntityList();
                if(dealerExciseresultList.size()>0){
                   if(dealerExciseresultList.get(0)!=null){
                        JSONObject jobjDealerExcise = new JSONObject();
                        jobjDealerExcise.put("RG23DEntryNumber", dealerExciseresultList.get(0).getRG23DEntryNumber());
                        jobjDealerExcise.put("SupplierRG23DEntry", dealerExciseresultList.get(0).getSupplierRG23DEntry());
                        jobjDealerExcise.put("AssessableValue", dealerExciseresultList.get(0).getAssessableValue());
                        jobjDealerExcise.put("PLARG23DEntry", dealerExciseresultList.get(0).getPLARG23DEntry());
                        jobjDealerExcise.put("ManuAssessableValue", dealerExciseresultList.get(0).getManuAssessableValue());
                        jobjDealerExcise.put("sequenceformat", dealerExciseresultList.get(0).getSeqformat().getID());
                        jobjDealerExcise.put("seqnumber", dealerExciseresultList.get(0).getSeqnumber());
                        jobjDealerExcise.put("datePreffixValue", dealerExciseresultList.get(0).getDatePreffixValue());
                        jobjDealerExcise.put("dateSuffixValue", dealerExciseresultList.get(0).getDateSuffixValue());
                        jobjDealerExcise.put("ManuInvoiceNumber", dealerExciseresultList.get(0).getInvoicenoManufacture());
                        jobjDealerExcise.put("ManuInvoiceDate", dealerExciseresultList.get(0).getInvoiceDateManufacture());
                        jobjDealerExcise.put("id", dealerExciseresultList.get(0).getId());
                        dealerTermdetailsArr = getDealerExciseTerms(companyid,jobjDealerExcise.getString("id"));
                        obj.put("dealerExciseTerms",dealerTermdetailsArr.toString());
                        jobjDealerExcise.put("dealerExciseTerms", dealerTermdetailsArr.toString());
                        JSONArray jrr = new JSONArray();
                        jrr.put(jobjDealerExcise);
                        obj.put("dealerExciseDetails", jrr.toString());
                   } 
                }
                //                else if (isCopyInvoice) {
//                    Map<String, Object> mapData = new HashMap<String, Object>();
//                    mapData.put("productid", prod.getID());
//                    mapData.put("salesOrPurchase", true);
//                    KwlReturnObject result6 = accProductObj.getProductTermDetails(mapData);
//                    if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
//                        ArrayList<ProductTermsMap> productTermDetail = (ArrayList<ProductTermsMap>) result6.getEntityList();
//                        JSONArray productTermJsonArry = fetchProductTermMapDetails(productTermDetail);
//                        obj.put("LineTermdetails", productTermJsonArry.toString());
//                        JSONArray dealerExciseTerm = new JSONArray();
//                        for (int i = 0; i < productTermJsonArry.length(); i++) {
//                            JSONObject productTermJsonObj = productTermJsonArry.getJSONObject(i);
//                            if (productTermJsonObj.getInt("termtype") == IndiaComplianceConstants.LINELEVELTERMTYPE_Excise_DUTY) {
//                                dealerExciseTerm.put(productTermJsonObj);
//                            }
//                        }
//                        obj.put("dealerExciseTerms", dealerExciseTerm.toString());
//
//                    }
//                }
            }
            
            if (gReceipt.getTax() != null) {
//                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getCreationDate(), gReceipt.getTax().getID());
                taxPercent = (Double) perresult.getEntityList().get(0);
            }
            obj.put( TAXPERCENT,taxPercent);
            JSONObject jObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
            }
            if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                obj = accProductObj.getProductDisplayUOM(prod, quantity, baseuomrate, true, obj);
            }
            if (doflag && !salesPurchaseReturnflag) {
                double doQuantity = getVIQuantityForGRO(row);
                if (doQuantity > 0) {
                    obj.put("quantity", doQuantity);
                    obj.put("dquantity", doQuantity);
                    obj.put("baseuomquantity", authHandler.calculateBaseUOMQuatity(doQuantity,baseuomrate, companyid));
                    obj.put("baseuomrate", baseuomrate);
                    remainedQty = authHandler.calculateBaseUOMQuatity(doQuantity,baseuomrate, companyid);
                    if (salesPurchaseReturnflag || isForDOGROLinking) {// in case of linking in normal transactions not lease consignment etc.
                        if (row.getTax() != null && invoiceRowProductQty > 0) {
                            double taxAmt = (rowTaxAmount / invoiceRowProductQty) * remainedQty;
                            obj.put("rowTaxAmount", taxAmt);
                            obj.put("taxamount", taxAmt);
                            obj.put("taxamountforlinking", taxAmt);
//                            obj.put("taxamountinbase", accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,taxAmt,row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getJournalEntry().getEntryDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
                            obj.put("taxamountinbase", accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,taxAmt,row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getCreationDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
                        }
                    }
                    jArr.put(obj);
                }
            } else {
                if (salesPurchaseReturnflag || isForDOGROLinking) {// // in case of linking in normal transactions not lease consignment etc.
                    if (row.getTax() != null && invoiceRowProductQty > 0) {
                        double taxAmt = (rowTaxAmount / invoiceRowProductQty) * remainedQty;
                        obj.put("rowTaxAmount", taxAmt);
                        obj.put("taxamount", taxAmt);
                        obj.put("taxamountforlinking", taxAmt);
//                        obj.put("taxamountinbase", accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,taxAmt,row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getJournalEntry().getEntryDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
                        obj.put("taxamountinbase", accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams,taxAmt,row.getGoodsReceipt().getCurrency().getCurrencyID(),row.getGoodsReceipt().getCreationDate(),row.getGoodsReceipt().getExternalCurrencyRate()).getEntityList().get(0));
                    }
                }
                if (salesPurchaseReturnflag) {  //in Purchase return do not show the row which is already used
                    if (cnAgainstVenGstflag) {  //always display product in line level for credit note against vendor only for malaysian country ERP-27284 / ERP-28249
                        jArr.put(obj);
                    } else if (remainedQty > 0) {
                        jArr.put(obj);
                    }
                } else {
                    if (!StringUtil.isNullOrEmpty(allocationType) && allocationType.equals(Constants.LANDED_COST_ALLOCATIONTYPE_CUSTOMDUTY) && gReceipt.isFixedAssetInvoice()) {
                        HashMap<String, Object> assetDetailsParams = new HashMap<String, Object>();
                        assetDetailsParams.put("companyid", companyid);
                        assetDetailsParams.put("invoiceDetailId", row.getID());
                        assetDetailsParams.put("moduleId", Constants.Acc_Vendor_Invoice_ModuleId);
                        KwlReturnObject assetInvMapObj = accProductObj.getAssetInvoiceDetailMapping(assetDetailsParams);
                        List<AssetInvoiceDetailMapping> assetInvMapList = assetInvMapObj.getEntityList();
                        for (AssetInvoiceDetailMapping invoiceDetailMapping : assetInvMapList) {
                            AssetDetails assetDetails = invoiceDetailMapping.getAssetDetails();
                            JSONObject tempJSON = new JSONObject(obj, JSONObject.getNames(obj));
                            tempJSON.put("productname",  assetDetails.getAssetId());
                            tempJSON.put("assetId", assetDetails.getId());
                            jArr.put(tempJSON);
                        }
                    } else {
                        jArr.put(obj);
                    }
                }
            }
        } catch(Exception ex) {
            throw ServiceException.FAILURE("accGoodsReceiptController.getGoodsReceiptRows : "+ex.getMessage(), ex);
        }
        return jArr;
    }
//    public JSONArray fetchProductTermMapDetails(ArrayList<ProductTermsMap> productTermDetail) {
//
//        JSONArray productTermJsonArry = new JSONArray();
//        try {
//            for (ProductTermsMap productTermsMapObj : productTermDetail) {
//                JSONObject productTermJsonObj = new JSONObject();
//                productTermJsonObj.put("productid", productTermsMapObj.getProduct().getID());
//                productTermJsonObj.put("termid", productTermsMapObj.getTerm().getId());
//                productTermJsonObj.put("term", productTermsMapObj.getTerm().getTerm());
//                productTermJsonObj.put("termtype", productTermsMapObj.getTerm().getTermType());
//                productTermJsonObj.put("termsequence", productTermsMapObj.getTerm().getTermSequence());
//                productTermJsonObj.put("formula", productTermsMapObj.getTerm().getFormula());
//                productTermJsonObj.put("formulaids", productTermsMapObj.getTerm().getFormula());
//                productTermJsonObj.put("termpercentage", productTermsMapObj.getPercentage());
//                productTermJsonObj.put("originalTermPercentage", productTermsMapObj.getTerm().getPercentage()); // For Service tax term abatment calculation
//                productTermJsonObj.put("termamount", "0.0");
//                productTermJsonObj.put("glaccountname", productTermsMapObj.getAccount().getAccountName());
//                productTermJsonObj.put("accountid", productTermsMapObj.getAccount().getID());
//                productTermJsonObj.put("glaccount", productTermsMapObj.getAccount().getID());
//                productTermJsonObj.put("IsOtherTermTaxable", productTermsMapObj.getTerm().isOtherTermTaxable());
//                productTermJsonObj.put("sign", productTermsMapObj.getTerm().getSign());
//                productTermJsonObj.put("taxtype", productTermsMapObj.getTaxType());
//                productTermJsonObj.put("purchasevalueorsalevalue", productTermsMapObj.getPurchaseValueOrSaleValue());
//                productTermJsonObj.put("isDefault", productTermsMapObj.isIsDefault());
//                productTermJsonObj.put("deductionorabatementpercent", productTermsMapObj.getDeductionOrAbatementPercent());
//                productTermJsonObj.put("taxvalue", productTermsMapObj.getPercentage());
//                productTermJsonObj.put("formType", productTermsMapObj.getFormType());
//                productTermJsonObj.put("isIsAdditionalTax", productTermsMapObj.getTerm().isIsAdditionalTax());
//                productTermJsonObj.put("includeInTDSCalculation", productTermsMapObj.getTerm().isIncludeInTDSCalculation());
//                productTermJsonArry.put(productTermJsonObj);
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(accGoodsReceiptCMN.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return productTermJsonArry;
//    }
    
    public JSONArray getDealerExciseTerms(String companyid, String dealerExciseDetailid) throws JSONException {
            JSONArray jrr = new JSONArray();
        try {
            HashMap<String, Object> dealerExciseTermParam = new HashMap<String, Object>();
            dealerExciseTermParam.put("dealerExciseDetailid", dealerExciseDetailid);
            dealerExciseTermParam.put("companyid", companyid);
            KwlReturnObject grdDealerExciseresult = accGoodsReceiptobj.getDealerExciseTermDetails(dealerExciseTermParam);
            List dealerExciseTermresultList = grdDealerExciseresult.getEntityList();
            if (dealerExciseTermresultList.size() > 0) {
               for(int i=0;i<dealerExciseTermresultList.size();i++){
                   DealerExciseTerms dealerExciseTerms = (DealerExciseTerms) dealerExciseTermresultList.get(i);
                   LineLevelTerms dealerExciseresultList = dealerExciseTerms.getLineLevelTerm();
                if (dealerExciseresultList != null) {
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("id", dealerExciseresultList.getId());
                    jsonobj.put("termid", dealerExciseresultList.getId());
                    jsonobj.put("term", dealerExciseresultList.getTerm());
                    jsonobj.put("formulaids", dealerExciseresultList.getFormula());
                    jsonobj.put("termpercentage", dealerExciseresultList.getPercentage());
                    //As duty Amount & Manu. Duty Amount is editable, so fetch back only from "DealerExciseTerms".
                    jsonobj.put("termamount", dealerExciseTerms.getDutyAmount());
                    jsonobj.put("manufactureTermAmount", dealerExciseTerms.getManuImpDutyAmount());
                    jsonobj.put("taxtype", dealerExciseresultList.getTaxType());
                    jsonobj.put("termtype", dealerExciseresultList.getTermType());
                    jrr.put(jsonobj);
                }
               } 
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jrr;
    }
        public String getdefaultBatchJson(Product product, HttpServletRequest request, String documentid, double quantity) throws JSONException {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        if (product.getLocation() != null && !StringUtil.isNullOrEmpty(product.getLocation().getId())) {
            jobj.put("location", product.getLocation().getId());
        }
        if (product.getWarehouse() != null && !StringUtil.isNullOrEmpty(product.getWarehouse().getId())) {
            jobj.put("warehouse", product.getWarehouse().getId());
        }
        jobj.put("documentid", "");
        if(!StringUtil.isNullOrEmpty(product.getID())){
        jobj.put("productid", product.getID());
        }
        jobj.put("quantity", quantity);
        jobj.put("purchasebatchid", "");
        jarr.put(jobj);
        return jarr.toString();
    }
    
    public double getGoodsReceiptDetailStatusForPR(GoodsReceiptDetail sod) throws ServiceException {
        double result = sod.getInventory().getQuantity();
        
        KwlReturnObject idresult = accGoodsReceiptobj.getPurchaseReturnIDFromVendorInvoiceDetails(sod.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0;
        while (ite1.hasNext()) {
            PurchaseReturnDetail ge = (PurchaseReturnDetail) ite1.next();
            qua += ge.getInventory().getQuantity();
        }
        result = result - qua;
        return result;
    } 
    
    
    private String getNewBatchJson(Product product, HttpServletRequest request, String documentid, boolean isCallFromGR, Map<String, List<Object[]>> baMap) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        try {
            DateFormat df = authHandler.getDateOnlyFormatter(request);
            KwlReturnObject kmsg = null;
            boolean linkingFlag = (StringUtil.isNullOrEmpty(request.getParameter("linkingFlag"))) ? false : Boolean.parseBoolean(request.getParameter("linkingFlag"));
            boolean isEdit = (StringUtil.isNullOrEmpty(request.getParameter("isEdit"))) ? false : Boolean.parseBoolean(request.getParameter("isEdit"));
            String moduleID = request.getParameter("moduleid");
            boolean isBatch = false;
            List<Object[]> batchserialdetails = null;
            if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
                kmsg = accCommonTablesDAO.getOnlySerialDetails(documentid, linkingFlag, moduleID, false, isEdit);
                batchserialdetails = kmsg.getEntityList();
            } else {
                isBatch = true;
                if (isCallFromGR && baMap.containsKey(documentid)) {
                    batchserialdetails = baMap.get(documentid);
                } else {
                    kmsg = accCommonTablesDAO.getBatchSerialDetails(documentid, !product.isIsSerialForProduct(), linkingFlag, moduleID, false, isEdit, "");
                    batchserialdetails = kmsg.getEntityList();
                }
            }
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.SerialWindow_ModuleId, 1));
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
            HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
            double ActbatchQty = 1;
            double batchQty = 0;
            if (batchserialdetails != null && !batchserialdetails.isEmpty()) {
                for (Object[] objArr : batchserialdetails) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", objArr[0] != null ? (String) objArr[0] : "");
                    obj.put("batch", objArr[1] != null ? (String) objArr[1] : "");
                    obj.put("batchname", objArr[1] != null ? (String) objArr[1] : "");
                    obj.put("location", objArr[2] != null ? (String) objArr[2] : "");
                    obj.put("warehouse", objArr[3] != null ? (String) objArr[3] : "");
                    if (isBatch) {
                        obj.put("row", objArr[15] != null ? (String) objArr[15] : "");
                        obj.put("rack", objArr[16] != null ? (String) objArr[16] : "");
                        obj.put("bin", objArr[17] != null ? (String) objArr[17] : "");
                    }
                    if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && product.isIsSerialForProduct()) {
                        ActbatchQty = accCommonTablesDAO.getBatchQuantity(documentid, (String) objArr[0]);
                        if (batchQty == 0) {
                            batchQty = ActbatchQty;
                        }
                        if (batchQty == ActbatchQty) {
                            obj.put("isreadyonly", false);
                            obj.put("quantity", ActbatchQty);
                        } else {
                            obj.put("isreadyonly", true);
                            obj.put("quantity", "");
                        }
                    } else {
                        obj.put("isreadyonly", false);
                        obj.put("quantity", ActbatchQty);
                    }
                    if (!product.isIsBatchForProduct() && !product.isIslocationforproduct() && !product.isIswarehouseforproduct() && !product.isIsrowforproduct() && !product.isIsrackforproduct() && !product.isIsbinforproduct() && product.isIsSerialForProduct()) {
                        obj.put("mfgdate", "");
                        obj.put("expdate", "");
                    } else {
                        obj.put("mfgdate", objArr[4] != null ? df.format(objArr[4]) : "");
                        obj.put("expdate", objArr[5] != null ? df.format(objArr[5]) : "");
                    }
                    if ((product.isIsBatchForProduct() || product.isIslocationforproduct() || product.isIswarehouseforproduct() || product.isIsrowforproduct() || product.isIsrackforproduct() || product.isIsbinforproduct()) && !product.isIsSerialForProduct()) {
                        obj.put("quantity", objArr[11] != null ? objArr[11] : "");
                        obj.put("avlquantity", objArr[6] != null ? objArr[6] : "");
                    }
                    obj.put("balance", 0);
                    obj.put("productid",product.getID());
                    obj.put("asset", "");
                    obj.put("serialnoid", objArr[7] != null ? (String) objArr[7] : "");
                    obj.put("serialno", objArr[8] != null ? (String) objArr[8] : "");
                    obj.put("purchasebatchid", objArr[0] != null ? (String) objArr[0] : "");
                    obj.put("purchaseserialid", objArr[7] != null ? (String) objArr[7] : "");
                    obj.put("expstart", (objArr[9] != null && !objArr[9].toString().equalsIgnoreCase("")) ? df.format(objArr[9]) : "");
                    obj.put("expend", (objArr[10] != null && !objArr[10].toString().equalsIgnoreCase("")) ? df.format(objArr[10]) : "");
                    obj.put("documentid", documentid != null ? documentid : "");
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", objArr[0]);
                    hashMap.put(Constants.companyKey, product.getCompany().getCompanyID());
                    /**
                     * Get document count attached to batch  and attachment id's
                     */
                    obj.put("attachment", 0);
                    obj.put("attachmentids", "");
                    KwlReturnObject object = accMasterItemsDAOobj.getBatchDocuments(hashMap);
                    if (object.getEntityList() != null && object.getEntityList().size()>0) {
                        obj.put("attachment", object.getEntityList().size());
                        List<Object[]> attachmentDetails = object.getEntityList();
                        String docids="";
                        for (Object[] attachmentArray : attachmentDetails) {
                            docids = docids + attachmentArray[3] + ",";
                        }
                        if (!StringUtil.isNullOrEmpty(docids)) {
                            docids = docids.substring(0, docids.length() - 1);
                        }
                        obj.put("attachmentids", docids);
                    }
                    if (objArr[14] != null && !objArr[14].toString().equalsIgnoreCase("")) {    //Get SerialDocumentMappingId
                        KwlReturnObject result1 = accountingHandlerDAOobj.getObject(SerialDocumentMapping.class.getName(), objArr[14].toString());
                        SerialDocumentMapping sdm = (SerialDocumentMapping) result1.getEntityList().get(0);
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        SerialCustomData serialCustomData = (SerialCustomData) sdm.getSerialCustomData();
                        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                        AccountingManager.setCustomColumnValues(serialCustomData, fieldMap, replaceFieldMap, variableMap);
                        DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                        for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                            String coldata = varEntry.getValue().toString();
                            String valueForReport = "";
                            if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                                try {
                                    String[] valueData = coldata.split(",");
                                    for (String value : valueData) {
                                        FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), value);
                                        if (fieldComboData != null) {
                                            // valueForReport += fieldComboData.getValue() + ",";
                                            valueForReport += value + ",";
                                        }
                                    }
                                    if (valueForReport.length() > 1) {
                                        valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                                    }
                                    obj.put(varEntry.getKey(), valueForReport);//fieldComboData.getValue()!=null ?fieldComboData.getValue():"");
                                } catch (Exception ex) {
                                    obj.put(varEntry.getKey(), coldata);
                                }
                            } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                                DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                try {
                                    dateFromDB = defaultDateFormat.parse(coldata);
                                    coldata = df2.format(dateFromDB);

                                } catch (Exception e) {
                                }
                                //This code change is made because date will not be long value now,it will be date in String form refer ERP-32324 

                                obj.put(varEntry.getKey(), coldata);

                            } else {
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    obj.put(varEntry.getKey(), coldata);
                                }
                            }
                        }
                    }
                    jSONArray.put(obj);
                    batchQty--;
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptCMN.class.getName()).log(Level.INFO, ex.getMessage());
        }

        return jSONArray.toString();
    }

    public void getAssetDetailForGRRows(JSONObject obj, String companyid, boolean linkingFlag, GoodsReceiptDetail row, DateFormat df, HttpServletRequest request, Map<String, List<Object[]>> assetBaMap) throws JSONException, ServiceException, SessionExpiredException {
        boolean isBatchForProduct = false;
        boolean isSerialForProduct = false;
        boolean isLocationForProduct = false;
        boolean isWarehouseForProduct = false;
        boolean isRowForProduct = false;
        boolean isRackForProduct = false;
        boolean isBinForProduct = false;
        
        // Load company preference object only once no need to load for each iteration
        KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences pref = (CompanyAccountPreferences) cap.getEntityList().get(0);
        
        HashMap<String, Object> assParams = new HashMap<String, Object>();
        assParams.put("companyId", companyid);
        assParams.put("invrecord", true);
        KwlReturnObject assResult = accProductObj.getAssetDetails(assParams);
        List <AssetDetails> assetList = assResult.getEntityList();
        List<String> assetNameList = new ArrayList<String>();
       
        for (AssetDetails ad : assetList) {
            assetNameList.add(ad.getAssetId());
        }

        Inventory inv = row.getInventory();
        Product prod = inv.getProduct();
        
        JSONArray assetDetailsJArr = new JSONArray();
        HashMap<String, Object> assetDetailsParams = new HashMap<String, Object>();
        assetDetailsParams.put("companyid", companyid);
        assetDetailsParams.put("invoiceDetailId", row.getID());
        assetDetailsParams.put("moduleId", Constants.Acc_Vendor_Invoice_ModuleId);
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
//        fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_FixedAssets_Details_ModuleId, 1));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), 121, 1));
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, String> replaceFieldMap1 = new HashMap<String, String>();
        HashMap<String, Integer> fieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap1, customFieldMap, customDateFieldMap);
        KwlReturnObject assetInvMapObj = accProductObj.getAssetInvoiceDetailMapping(assetDetailsParams);
        List<AssetInvoiceDetailMapping> assetInvMapList = assetInvMapObj.getEntityList();
        for (AssetInvoiceDetailMapping invoiceDetailMapping : assetInvMapList ) {
            AssetDetails assetDetails = invoiceDetailMapping.getAssetDetails();
            JSONObject assetDetailsJOBJ = new JSONObject();

            // <-- Following statement must be first statement in putting assetDetailsJOBJ
            if (linkingFlag) {

                if (assetNameList.contains(assetDetails.getAssetId())) {// don't put assets which are included in GR
                    continue;
                }

                assetDetailsJOBJ.put("assetId", assetDetails.getId());
            } else {
                if ((row.getGoodsReceiptOrderDetails() != null) || (row.getVendorQuotationDetail() != null) || (row.getPurchaseorderdetail() != null)) { // in case of linking while editing the Asset Details combo get load so i need to send id of asset.
                    assetDetailsJOBJ.put("assetId", assetDetails.getId());
                } else {
                    assetDetailsJOBJ.put("assetId", assetDetails.getAssetId());
                }
            }

            assetDetailsJOBJ.put("assetdetailId", assetDetails.getId());
            assetDetailsJOBJ.put("assetName", assetDetails.getAssetId());
            assetDetailsJOBJ.put("location", (assetDetails.getLocation() != null) ? assetDetails.getLocation().getId() : "");
            assetDetailsJOBJ.put("department", (assetDetails.getDepartment() != null) ? assetDetails.getDepartment().getId() : "");
            assetDetailsJOBJ.put("assetdescription", (assetDetails.getAssetDescription() != null) ? assetDetails.getAssetDescription() : "");
            assetDetailsJOBJ.put("assetUser", (assetDetails.getAssetUser() != null) ? assetDetails.getAssetUser().getUserID() : "");
            assetDetailsJOBJ.put("cost", assetDetails.getCost());
            assetDetailsJOBJ.put("costInForeignCurrency", assetDetails.getCostInForeignCurrency());
            assetDetailsJOBJ.put("salvageValueInForeignCurrency", assetDetails.getSalvageValueInForeignCurrency());
            assetDetailsJOBJ.put("salvageRate", assetDetails.getSalvageRate());
            assetDetailsJOBJ.put("salvageValue", assetDetails.getSalvageValue());
            assetDetailsJOBJ.put("accumulatedDepreciation", assetDetails.getAccumulatedDepreciation());
//            assetDetailsJOBJ.put("wdv", assetDetails.getWdv());
            assetDetailsJOBJ.put("assetLife", assetDetails.getAssetLife());
            assetDetailsJOBJ.put("elapsedLife", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("nominalValue", assetDetails.getElapsedLife());
            assetDetailsJOBJ.put("installationDate", df.format(assetDetails.getInstallationDate()));
            assetDetailsJOBJ.put("purchaseDate", df.format(assetDetails.getPurchaseDate()));
            addMachine(assetDetailsJOBJ,companyid);
            Map<String, Object> variableMap = new HashMap<String, Object>();
            AssetDetailsCustomData jeDetailCustom = (AssetDetailsCustomData) assetDetails.getAssetDetailsCustomData();
            replaceFieldMap1 = new HashMap<String, String>();
            if (jeDetailCustom != null) {
                AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMap, replaceFieldMap1, variableMap);
                JSONObject params = new JSONObject();
                params.put("companyid", companyid);
                params.put("getCustomFieldArray", true);
                fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, assetDetailsJOBJ, params);
            }
            if (prod != null ) {
                isBatchForProduct = prod.isIsBatchForProduct();
                isSerialForProduct = prod.isIsSerialForProduct();
                isLocationForProduct = prod.isIslocationforproduct();
                isWarehouseForProduct = prod.isIswarehouseforproduct();
                isRowForProduct = prod.isIsrowforproduct();
                isRackForProduct = prod.isIsrackforproduct();
                isBinForProduct = prod.isIsbinforproduct();
            }
            boolean isEdit = request.getParameter("isEdit") != null ? Boolean.parseBoolean(request.getParameter("isEdit")) : false;
            if (isEdit) {
                if (pref.isIsBatchCompulsory() || pref.isIsSerialCompulsory() || pref.isIslocationcompulsory() || pref.isIswarehousecompulsory() || pref.isIsrowcompulsory() || pref.isIsrackcompulsory() || pref.isIsbincompulsory()) {  //check if company level option is on then only we will check productt level
                    if (isBatchForProduct || isSerialForProduct || isLocationForProduct || isWarehouseForProduct || isRowForProduct || isRackForProduct || isBinForProduct) {  //product level batch and serial no on or not
                        assetDetailsJOBJ.put("batchdetails", getNewBatchJson(assetDetails.getProduct(), request, assetDetails.getId(), false, assetBaMap));
                    }
                }
            }
            assetDetailsJArr.put(assetDetailsJOBJ);
        }
        obj.put("assetDetails", assetDetailsJArr.toString());
    }
    public void addMachine(JSONObject jSONObject, String companyId) throws ServiceException {
        try {
            Map<String, Object> requestParams = new HashMap();
            requestParams.put("companyId", companyId);
            requestParams.put("assetDetails", jSONObject.optString("assetdetailId"));
            KwlReturnObject result = accProductObj.getMachineId(requestParams);
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                try {
                    jSONObject.put("machine", (String) itr.next());
                } catch (com.krawler.utils.json.base.JSONException ex) {
                    throw ServiceException.FAILURE(ex.getMessage(), ex);
                }
            }
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
 private String getTimeIntervalForProduct(String inouttime) throws ParseException
            {
                 Date date = new Date();
                 if(!StringUtil.isNullOrEmpty(inouttime))
                     inouttime=inouttime.replaceAll("%20", " ");
                     inouttime=inouttime.replaceAll("%25", " ");
                        String inoutTime = inouttime;
                        String inoutTimeArray[] = inoutTime.split(",");
                        String inTimeArray[] = inoutTimeArray[0].split(" ");
                        String outTimeArray[] = inoutTimeArray[1].split(" ");
                        int inHour = 0;
                        String inDateValue = "";
                        String outDateValue = "";
                        int inMinutes = 0;
                        int outHour = 0;
                        int outMinutes = 0;
                        if (inoutTimeArray.length > 1) {
                           inDateValue= inTimeArray[0];
                           outDateValue= outTimeArray[0];
                            inHour = Integer.parseInt(inTimeArray[1].split(":")[0]);
                            inMinutes = Integer.parseInt(inTimeArray[1].split(":")[1]);
                            outHour = Integer.parseInt(outTimeArray[1].split(":")[0]);
                            outMinutes = Integer.parseInt(outTimeArray[1].split(":")[1]);
                        }
                       DateFormat df = new SimpleDateFormat("yyyy-mm-dd");
                        Date inDate = df.parse(inDateValue);
                        Date outDate = df.parse(outDateValue);                       
                        inDate.setHours(inHour);
                        inDate.setMinutes(inMinutes);
                        inDate.setSeconds(00);

                        outDate.setHours(outHour);
                        outDate.setMinutes(outMinutes);
                        outDate.setSeconds(00);
                        double timeDiff=((double)(outDate.getTime()-inDate.getTime()))/3600000;
                        DecimalFormat df1 = new DecimalFormat("#.##");
                        timeDiff = Double.valueOf(df1.format(timeDiff));
                       return timeDiff+" Hrs";
                
            }

       private void setCustomColumnValuesForProduct(AccJEDetailsProductCustomData accJEDetailsProductCustomData, HashMap<String, Integer> fieldMap, Map<String, String> replaceFieldMap,
            Map<String, Object> variableMap, JSONObject params) {
        for (Map.Entry<String, Integer> field : fieldMap.entrySet()) {
            Integer colnumber = field.getValue();
            if (colnumber > 0) { // colnumber will be 0 if key is part of reference map
                Integer isref = fieldMap.get(field.getKey() + "#" + colnumber);// added '#' while creating map collection for custom fields.
                // Without this change, it creates problem if two custom columns having name like XYZ and XYZ1
                String coldata = null;
                boolean isForReport = params.optBoolean("isForReport", false);
                boolean isExport = params.optBoolean("isExport", false);
                DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                Date dateFromDB=null;
                if (isref != null) {
                    try {
                        if (accJEDetailsProductCustomData != null) {
                            coldata = accJEDetailsProductCustomData.getCol(colnumber);
                        }
//                        if (StringUtil.isNullOrEmpty(coldata)) {
//                            coldata = customData.getCol(colnumber);
//                        }
//                        String coldataVal = null;
                        if (!StringUtil.isNullOrEmpty(coldata)) {
                            if (coldata.length() > 1) {
                                if (isref == 1) {
//                                        coldataVal = fieldDataManagercntrl.getMultiSelectColData(coldata);
//                                        if(isExport) {
//                                            coldata = coldataVal;
//                                        }
//                                        variablesMapForCustomData.put(field.getKey(), coldataVal);
                                } else if (isref == 0 || isref == 7) {
//                                        coldataVal = customData.getRefCol(colnumber);
//                                        if(isExport) {
//                                            coldata = coldataVal;
//                                        }
//                                        variablesMapForCustomData.put(field.getKey(), coldataVal);
                                    if (isForReport) {
                                        String valueForReport = "";
                                        String[] valueData = coldata.split(",");
                                        for (String value : valueData) {
                                            KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                            FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                            if (fieldComboData != null) {
                                                valueForReport += fieldComboData.getValue() + ",";
                                            }
                                        }
                                        if (valueForReport.length() > 1) {
                                            coldata = valueForReport.substring(0, valueForReport.length() - 1);
                                        }
                                    } else {
                                        coldata = coldata;
                                    }
                                } else if (isref == 3 && isExport) {
                                    DateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
                                    DateFormat userDateFormat = params.has(Constants.userdf) ? (DateFormat) params.get(Constants.userdf) : null;
                                    try {
                                        dateFromDB = defaultDateFormat.parse(coldata);
                                        coldata = userDateFormat != null ? userDateFormat.format(dateFromDB) : df2.format(dateFromDB);
                                    } catch (Exception e) {
                                        //This code change is made because date will not be long value now,it will be date in String form refer ERP-32324 
                                    }
                                }
                            }
                            variableMap.put(field.getKey(), coldata);
//                                try {
//                                    variableMapForFormula.put(replaceFieldMap.get(field.getKey()), Double.parseDouble(coldata));
//                                } catch (Exception ex) {
//                                    variableMapForFormula.put(replaceFieldMap.get(field.getKey()), 0);
//                                }
                        }
                    } catch (IllegalArgumentException ex) {
                        ex.printStackTrace();
                    } catch (ObjectNotFoundException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    /*Function will be called only for delivery order flow to fetch product quantity remained for invoice in DO*/
    public double getVIQuantityForGRO(GoodsReceiptDetail ivDetail) throws ServiceException {        
        double result = ivDetail.getInventory().getQuantity();
//        double result = ivDetail.getInventory().getActquantity();
        KwlReturnObject idresult = accGoodsReceiptobj.getGDOIDFromVendorInvoiceDetails(ivDetail.getID());
        List list = idresult.getEntityList();
        Iterator ite1 = list.iterator();
        double qua = 0;
        while (ite1.hasNext()) {
            GoodsReceiptOrderDetails ge = (GoodsReceiptOrderDetails) ite1.next();
            qua += ge.getInventory().getQuantity();
        }
        result = result - qua;
        return result;
    }
    
    public Map<GoodsReceiptDetail, Object[]> applyDebitNotes(Map request, GoodsReceipt gReceipt, String baseCurrencyId) throws ServiceException {
        Map<GoodsReceiptDetail, Object[]> hm = new HashMap<GoodsReceiptDetail, Object[]>();
        String accName="";
        String accID="";
        String companyid = (String) request.get("companyid");
        Set<GoodsReceiptDetail> grRows = gReceipt.getRows();
//        KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), (String) request.get(GCURRENCYID));
//        KWLCurrency currency = baseCurrency;
        double amount;
        double quantity;
        double disc = (gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue()) / grRows.size();
//        Iterator itr = grRows.iterator();
//        while (itr.hasNext()) {
//            GoodsReceiptDetail temp = (GoodsReceiptDetail) itr.next();
        if (grRows != null && !grRows.isEmpty()){
            for ( GoodsReceiptDetail temp: grRows){
//                quantity = (gReceipt.getPendingapproval() == 1)? temp.getInventory().getActquantity() : (temp.getInventory().isInvrecord() ? temp.getInventory().getQuantity() : temp.getInventory().getActquantity());
                quantity = temp.getInventory().getQuantity();
                amount = authHandler.round(temp.getRate() * quantity, companyid);
                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                double rowTaxPercent = 0;
                double rowTaxAmount = 0;
                boolean isRowTaxApplicable=false;
                if (temp.getTax() != null) {
    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
//                    KwlReturnObject perresult = accTaxObj.getTaxPercent((String) request.get(COMPANYID), gReceipt.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent((String) request.get(COMPANYID), gReceipt.getCreationDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                }
                double ramount=amount-rdisc;
                if(temp.isWasRowTaxFieldEditable()){ //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                    if (isRowTaxApplicable) {
                        rowTaxAmount = temp.getRowTaxAmount() + temp.getRowTermTaxAmount();
                    }
                }else{// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                    rowTaxAmount = (amount-rdisc)*rowTaxPercent/100;
                }
                ramount+=rowTaxAmount;//ramount*rowTaxPercent/100;
                ramount-=disc;
                accName = temp.getInventory().getProduct().getPurchaseAccount().getName();// required for 1099 report[PS]
                accID = temp.getInventory().getProduct().getPurchaseAccount().getID();
                hm.put(temp, new Object[]{ramount, quantity, 0.0,accName,accID,0.0 });

                if (gReceipt == null) {
                    gReceipt = temp.getGoodsReceipt();
                }
            }
        }
//        String query = "select dn, dnr, dnd from DebitNote dn left join dn.rows dnr left join dn.discounts dnd where dn.deleted=false and (dnr.goodsReceiptRow.goodsReceipt.ID=? or dnd.goodsReceipt.ID=?) order by dn.sequence";
//        Iterator dnitr = HibernateUtil.executeQuery(session, query, new Object[]{gReceipt.getID(), gReceipt.getID()}).iterator();
        KwlReturnObject result = accDebitNoteobj.getDNFromGReceipt(gReceipt.getID());
//         Iterator dnitr = result.getEntityList().iterator();
//         while (dnitr.hasNext()) {
//            Object[] dnrow = (Object[]) dnitr.next();
        List<Object[]> list= result.getEntityList();
        double taxAmount=0, discountAmount = 0, deductDiscount = 0;
        if (list != null && !list.isEmpty()){
            for (Object[] dnrow : list){
            	taxAmount=0;
            	discountAmount = 0;
                deductDiscount = 0;
                DebitNoteDetail dnr = (DebitNoteDetail) dnrow[1];
                GoodsReceiptDetail temp = dnr.getGoodsReceiptRow();
                if (!hm.containsKey(temp)) {
                    continue;
                }
                Object[] val = (Object[]) hm.get(temp);
                String fromcurrencyid = (dnr.getDebitNote().getCurrency() == null ? baseCurrencyId : dnr.getDebitNote().getCurrency().getCurrencyID());
                String tocurrencyid = (gReceipt.getCurrency() == null ? baseCurrencyId : gReceipt.getCurrency().getCurrencyID());
                double baseDiscount = 0;
                if(dnr.getPaidinvflag()!=1) {//Ignore dn detail rows amount which are inserted in table for paid invoice row 
                    if(dnr.getDiscount()!=null){
//                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, gReceipt.getJournalEntry().getEntryDate(), 0);
                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, gReceipt.getCreationDate(), 0);
                        baseDiscount = (Double) bAmt.getEntityList().get(0);
                    }
                }
                double v = (Double) val[0] - baseDiscount;
                if (dnr.getTaxAmount() != null) {
                    if(dnr.getPaidinvflag()!=1) {
                        taxAmount = dnr.getTaxAmount() + (Double) val[2];
                    }
                }
                double q = (Double) val[1];
                if (temp.getInventory() != null) {
                    q -= dnr.getQuantity();
                }
                if (dnr.getTotalDiscount() != null) {
                    if(dnr.getPaidinvflag()!=1) {
                	discountAmount = dnr.getTotalDiscount();
                        deductDiscount = (Double) val[5] + dnr.getTotalDiscount();
                    }
                }                
                hm.put(temp, new Object[]{v,q,taxAmount-discountAmount,accName,accID, deductDiscount});             
            }
         }
        return hm;
    }
    
    public List getInvoiceDiscountAmountInfo(Map<String, Object> request, GoodsReceipt gReceipt) throws ServiceException {
        List ll = new ArrayList();
        String accNames = "";
        boolean belongsTo1099 = false;
        ArrayList acclist = new ArrayList();
        String currencyFilterForTrans = "";
        double amountdue=0;
        if (request.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = request.get("currencyfilterfortrans") != null ? (String) request.get("currencyfilterfortrans") : "";
        }
        double amountDueOriginal = 0;

        /*
            We refered applyDebitNotes() and found that deductDiscount value is calculated from Debitnote row level discount when VI selected. But this functionality is not exist in current code when checked on 6th Oct 2014 
        */
        double deductDiscount = 0;
        Set<GoodsReceiptDetail> grRows = gReceipt.getRows();
        if (grRows != null && !grRows.isEmpty()){
            for ( GoodsReceiptDetail temp: grRows){
                accNames += temp.getInventory().getProduct().getPurchaseAccount().getName();// required for 1099 report[PS]
                accNames += ",";
                acclist.add(temp.getInventory().getProduct().getPurchaseAccount().getID());
            }
        }
        accNames = accNames.substring(0, Math.max(0, accNames.length() - 1));
        
        KwlReturnObject result = accTaxObj.belongsTo1099(gReceipt.getCompany().getCompanyID(), acclist);
        List l = result.getEntityList();
        if (l.size() > 0) {
            belongsTo1099 = true;
        }
        boolean invoiceAmtDueEqualsInvoiceAmt = false;
        if (request.containsKey("invoiceAmtDueEqualsInvoiceAmt") && request.get("invoiceAmtDueEqualsInvoiceAmt") != null) { // check if orignal invoice amount equals to invoice amount due
            invoiceAmtDueEqualsInvoiceAmt = Boolean.parseBoolean(request.get("invoiceAmtDueEqualsInvoiceAmt").toString()); // if equal, then payment hasn't been made for invoice. Skip calculation and take amount due from invoice table
        }
        if (!invoiceAmtDueEqualsInvoiceAmt) {
            Date grCreationDate = null;
            double grExternalCurrencyRate = 0d;
            grCreationDate = gReceipt.getCreationDate();
            if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                grExternalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
            } else {
//                grCreationDate = gReceipt.getJournalEntry().getEntryDate();
                grExternalCurrencyRate = gReceipt.getJournalEntry().getExternalCurrencyRate();
            }
            String baseCurrencyID = (String) request.get(GCURRENCYID);
            String currencyid = (gReceipt.getCurrency() == null ? baseCurrencyID : gReceipt.getCurrency().getCurrencyID());
            if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                amountdue = gReceipt.getOpeningBalanceAmountDue();
            } else {
                amountdue = gReceipt.getInvoiceamountdue();
            }
        
            if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {
                amountDueOriginal = amountdue;
                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, grCreationDate, grExternalCurrencyRate);
                    amountdue = (Double) bAmt.getEntityList().get(0);
                }
            } else {
                amountdue = 0;
            }
        }
        
        ll.add(0);
        ll.add(amountdue);
        ll.add(accNames);
        ll.add(belongsTo1099);
        ll.add(deductDiscount);
        ll.add(amountDueOriginal);
        return ll;
    }
    
    public Map<GoodsReceiptDetail, Object[]> applyDebitNotes(Map request, GoodsReceipt gReceipt) throws ServiceException {
        Map<GoodsReceiptDetail, Object[]> hm = new HashMap<GoodsReceiptDetail, Object[]>();
        String accName="";
        String accID="";
        String companyid = (String) request.get("companyid");
        Set<GoodsReceiptDetail> grRows = gReceipt.getRows();
        String gcurrencyID=(String) request.get(GCURRENCYID);
        boolean isNewGST=((request.containsKey(Constants.isNewGST)) && (!StringUtil.isNullOrEmpty(request.get(Constants.isNewGST).toString()))?(boolean)(request.get(Constants.isNewGST)):false);         
//        KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), gcurrencyID);
//        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        double amount;
        double quantity;
        double disc = (gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue()) / grRows.size();
//        Iterator itr = grRows.iterator();
//        while (itr.hasNext()) {
//            GoodsReceiptDetail temp = (GoodsReceiptDetail) itr.next();
        if (grRows != null && !grRows.isEmpty()){
            for ( GoodsReceiptDetail temp: grRows){
//                quantity = (gReceipt.getPendingapproval() == 1)? temp.getInventory().getActquantity() : (temp.getInventory().isInvrecord() ? temp.getInventory().getQuantity() : temp.getInventory().getActquantity());
                quantity = temp.getInventory().getQuantity();
                amount = authHandler.round(temp.getRate() * quantity, companyid);
                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                double rowTaxPercent = 0;
                double rowTaxAmount = 0;
                boolean isRowTaxApplicable=false;
                if (temp.getTax() != null) {
    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
//                    KwlReturnObject perresult = accTaxObj.getTaxPercent((String) request.get(COMPANYID), gReceipt.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent((String) request.get(COMPANYID), gReceipt.getCreationDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                }
                double ramount=amount-rdisc;
                if (isNewGST) {
                    rowTaxAmount = (temp.getRowTermAmount() - temp.getTdsLineAmount());
                } else {
                    if (temp.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                        if (isRowTaxApplicable) {
                            rowTaxAmount = temp.getRowTaxAmount() + temp.getRowTermTaxAmount();
                        }
                    } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                        rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
                    }
                }
                ramount+=rowTaxAmount;//ramount*rowTaxPercent/100;
                ramount-=disc;
                accName = temp.getInventory().getProduct().getPurchaseAccount().getName();// required for 1099 report[PS]
                accID = temp.getInventory().getProduct().getPurchaseAccount().getID();
                hm.put(temp, new Object[]{ramount, quantity, 0.0,accName,accID,0.0 });

                if (gReceipt == null) {
                    gReceipt = temp.getGoodsReceipt();
                }
            }
        }
        /* TODO - Not In Use accCreditNoteobj.getCNRowsDiscountFromInvoice(). To Check record is exist on any database [SagarM]
            select dn.id as id143_0_ from debitnote dn left outer join dndetails dnd on dn.id=dnd.debitnote left outer join dndiscount dndiscount 
            on dn.id=dndiscount.debitnote, grdetails grd where dnd.goodsReceiptRow=grd.id and dn.deleteflag='F' and 
            (grd.goodsreceipt in (select id from goodsreceipt) or dndiscount.goodsreceipt  in (select id from goodsreceipt) ) order by dn.seq
        */
//        String query = "select dn, dnr, dnd from DebitNote dn left join dn.rows dnr left join dn.discounts dnd where dn.deleted=false and (dnr.goodsReceiptRow.goodsReceipt.ID=? or dnd.goodsReceipt.ID=?) order by dn.sequence";
//        Iterator dnitr = HibernateUtil.executeQuery(session, query, new Object[]{gReceipt.getID(), gReceipt.getID()}).iterator();
        KwlReturnObject result = accDebitNoteobj.getDNFromGReceipt(gReceipt.getID());
//         Iterator dnitr = result.getEntityList().iterator();
//         while (dnitr.hasNext()) {
//            Object[] dnrow = (Object[]) dnitr.next();
        List<Object[]> list= result.getEntityList();
        String tocurrencyid = (gReceipt.getCurrency() == null ? gcurrencyID : gReceipt.getCurrency().getCurrencyID());

        double taxAmount=0, discountAmount = 0, deductDiscount = 0;
        if (list != null && !list.isEmpty()){
            for (Object[] dnrow : list){
            	taxAmount=0;
            	discountAmount = 0;
                deductDiscount = 0;
                DebitNoteDetail dnr = (DebitNoteDetail) dnrow[1];
                GoodsReceiptDetail temp = dnr.getGoodsReceiptRow();
                if (!hm.containsKey(temp)) {
                    continue;
                }
                Object[] val = (Object[]) hm.get(temp);
                String fromcurrencyid = (dnr.getDebitNote().getCurrency() == null ? gcurrencyID : dnr.getDebitNote().getCurrency().getCurrencyID());
                double baseDiscount = 0;
                if(dnr.getPaidinvflag()!=1) {//Ignore dn detail rows amount which are inserted in table for paid invoice row 
                    if(dnr.getDiscount()!=null){
//                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, gReceipt.getJournalEntry().getEntryDate(), 0);
                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, tocurrencyid, gReceipt.getCreationDate(), 0);
                        baseDiscount = (Double) bAmt.getEntityList().get(0);
                    }
                }
                double v = (Double) val[0] - baseDiscount;
                if (dnr.getTaxAmount() != null) {
                    if(dnr.getPaidinvflag()!=1) {
                        taxAmount = dnr.getTaxAmount() + (Double) val[2];
                    }
                }
                double q = (Double) val[1];
                if (temp.getInventory() != null) {
                    q -= dnr.getQuantity();
                }
                if (dnr.getTotalDiscount() != null) {
                    if(dnr.getPaidinvflag()!=1) {
                	discountAmount = dnr.getTotalDiscount();
                        deductDiscount = (Double) val[5] + dnr.getTotalDiscount();
                    }
                }                
                hm.put(temp, new Object[]{v,q,taxAmount-discountAmount,accName,accID, deductDiscount});             
            }
         }
        return hm;
    }
    
  public List getGRAmountDue(Map<String, Object> request, GoodsReceipt gReceipt, String transactionCurrencyId, String baseCurrencyID, 
            JournalEntry GoodsReceiptJE, List<ReceiptTermsMap> receiptTermMapList, List<ReceiptDetail> receiptDetailList, 
            List<PaymentDetail> paymentDetailsList, List<DebitNoteDetail>dnDetailList) throws ServiceException {
        List ll = new ArrayList();
        String accNames = "";
        double amountdue = 0;
        boolean belongsTo1099 = false;
        double amount = 0, ramount = 0, contraamount = 0, deductDiscount = 0, termAmount = 0;
        String currencyFilterForTrans = "";
        if (request.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = request.get("currencyfilterfortrans") != null ? (String) request.get("currencyfilterfortrans") : "";
        }
        String companyid = (String) request.get(COMPANYID);
        ArrayList acclist = new ArrayList();
        Iterator itrCn = applyDebitNotes(request, gReceipt).values().iterator();
        String currencyid = transactionCurrencyId;
        while (itrCn.hasNext()) {
            Object[] temp = (Object[]) itrCn.next();
            amount += (Double) temp[0] - (Double) temp[2];
            accNames += (String) temp[3];// required for 1099 report[PS]
            accNames += ",";
            acclist.add((String) temp[4]);
            deductDiscount += (Double) temp[5];
        }
        accNames = accNames.substring(0, Math.max(0, accNames.length() - 1));

        KwlReturnObject result = accTaxObj.belongsTo1099(companyid, acclist);
        List l = result.getEntityList();
        if (l.size() > 0) {
            belongsTo1099 = true;
        }
        JournalEntryDetail tempd = gReceipt.getTaxEntry();
        if (tempd != null) {
            amount += tempd.getAmount();
        }
        double grExternalCurrencyRate = 0d;
        Date grCreationDate = null;
        grCreationDate = gReceipt.getCreationDate();
        if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
            grExternalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
        } else {
//            grCreationDate = GoodsReceiptJE.getEntryDate();
            grExternalCurrencyRate = GoodsReceiptJE.getExternalCurrencyRate();
        }

        //Get amount knock off using otherwise credit notes.
        double cnAmountOW = 0;
        if (dnDetailList != null && !dnDetailList.isEmpty()) {
            for (DebitNoteDetail dnr : dnDetailList) {

                boolean isopeningBalanceInvoice = gReceipt.isIsOpeningBalenceInvoice();

                String fromcurrencyid = (dnr.getDebitNote().getCurrency() == null ? baseCurrencyID : dnr.getDebitNote().getCurrency().getCurrencyID());
                if (dnr.getDiscount() != null) {
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if gReceipt is opening balance gReceipt and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, currencyid, grCreationDate, grExternalCurrencyRate);
                    } else if (fromcurrencyid.equals(baseCurrencyID)) {
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(request, dnr.getDiscount().getDiscountValue(), currencyid, grCreationDate, grExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, currencyid, grCreationDate, grExternalCurrencyRate);
                    }
                    cnAmountOW += (Double) bAmt.getEntityList().get(0);
                }
            }
        }

//        result = accVendorPaymentobj.getPaymentsFromGReceipt(gReceipt.getID(), companyid);
//        List<PaymentDetail> list = result.getEntityList();
        if (paymentDetailsList != null && !paymentDetailsList.isEmpty()) {
            for (PaymentDetail pd : paymentDetailsList) {

                Date paymentCreationDate = null;
                double externalCurrencyRate = 0d;
                boolean isopeningBalancePayment = pd.getPayment().isIsOpeningBalencePayment();

                paymentCreationDate = pd.getPayment().getCreationDate();
                if (pd.getPayment().isIsOpeningBalencePayment() && !pd.getPayment().isNormalPayment()) {
                    externalCurrencyRate = pd.getPayment().getExchangeRateForOpeningTransaction();
                } else {
//                    paymentCreationDate = pd.getPayment().getJournalEntry().getEntryDate();
                    externalCurrencyRate = pd.getPayment().getJournalEntry().getExternalCurrencyRate();
                }

                if (pd.getFromCurrency() != null && pd.getToCurrency() != null) {//Comment
//                    String fromcurrencyid = (pd.getPayment().getCurrency() == null ? baseCurrencyID : pd.getPayment().getCurrency().getCurrencyID());
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, pd.getAmount(), fromcurrencyid, currencyid, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
//                    double previousAmount = (Double) bAmt.getEntityList().get(0);
//                        double externaltransactionCurrencyRate = pd.getAmount() / amount;
//                        ramount += pd.getAmount() / externaltransactionCurrencyRate;
//                        ramount = authHandler.round(ramount, companyid);
                        double ExchangeRate = pd.getAmountDueInPaymentCurrency() / pd.getAmountDueInGrCurrency();
                        ramount += (pd.getAmount()+pd.getDiscountAmount()) / ExchangeRate;
                        ramount = authHandler.round(ramount, companyid);
                } else {
//                    ramount += pd.getAmount();
                    String fromcurrencyid = (pd.getPayment().getCurrency() == null ? baseCurrencyID : pd.getPayment().getCurrency().getCurrencyID());
                    KwlReturnObject bAmt = null;
                    if (isopeningBalancePayment && pd.getPayment().isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
                    }
                    ramount += (Double) bAmt.getEntityList().get(0);
                }

            }
        }

        if (receiptDetailList != null && !receiptDetailList.isEmpty()) {
            for (ReceiptDetail pd : receiptDetailList) {
                contraamount += pd.getAmount();
                String fromcurrencyid = (pd.getReceipt().getCurrency() == null ? baseCurrencyID : pd.getReceipt().getCurrency().getCurrencyID());
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, contraamount, fromcurrencyid, currencyid, grCreationDate, grExternalCurrencyRate);
                contraamount = (Double) bAmt.getEntityList().get(0);
            }
        }

        // Get amount from Invoice Terms 
        HashMap<String, Object> requestParam = new HashMap();
        if (receiptTermMapList != null && !receiptTermMapList.isEmpty()) {
            for (ReceiptTermsMap invoiceTerMap : receiptTermMapList) {
                InvoiceTermsSales mt = invoiceTerMap.getTerm();
                termAmount += invoiceTerMap.getTermamount();
            }
        }

        KwlReturnObject resultDn = accDebitNoteobj.getDistintDNFromGReceipt(gReceipt.getID());
        List listDn = resultDn.getEntityList();
        DebitNote debitNote = null;
        double termAmountDn = 0;
        if (listDn != null && !listDn.isEmpty()) {
            for (Object dn : listDn) {
                debitNote = (DebitNote) dn;
                if (debitNote != null) {
                    requestParam.put("debitNoteId", debitNote.getID());
                    KwlReturnObject curresult = accDebitNoteobj.getDebitNoteTermMap(requestParam);
                    List<DebitNoteTermsMap> termMapDn = curresult.getEntityList();
                    for (DebitNoteTermsMap debitNoteTermsMap : termMapDn) {
                        InvoiceTermsSales mt = debitNoteTermsMap.getTerm();
                        termAmountDn += debitNoteTermsMap.getTermamount();
                    }
                }
            }
        }
        amount = authHandler.round(amount, companyid);
        amountdue = amount - cnAmountOW - ramount - contraamount + termAmount - termAmountDn;
        double amountDueOriginal = 0;
        if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {
            amountDueOriginal = amountdue;
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, grCreationDate, grExternalCurrencyRate);
                amountdue = (Double) bAmt.getEntityList().get(0);
            }
        } else {
            amount = 0;
            amountdue = 0;
        }
        ll.add(amount);
        ll.add(amountdue);
        ll.add(accNames);
        ll.add(belongsTo1099);
        ll.add(deductDiscount);
        ll.add(amountDueOriginal);
        return ll;
    }
    
//    public double getAmountDueOfGRBeforeClaimedDate(HashMap<String, Object> requestParams) throws ServiceException {
//        double totalAmtDue = 0d;
//        String companyid = (String) requestParams.get("companyid");
//        String invoiceId = (String) requestParams.get("invoiceId");
//
//        // get Received Amount for this invoice before claimed date
//
//        KwlReturnObject invoicePaidAmtObj = accPaymentDAOobj.getPaymentAmountofBadDebtGoodsReceipt(invoiceId, false);
//        double paidAmt = (Double) invoicePaidAmtObj.getEntityList().get(0);
//
//        // get Credited Amount for invoice
//
//        DebitNote debitNote = null;
//
//        KwlReturnObject cnResult = accDebitNoteobj.getDNFromGoodsReceiptOtherwise(invoiceId, companyid);
//        List<DebitNoteDetail> dnds = cnResult.getEntityList();
//        HashSet<String> dnSet = new HashSet<String>();
//
//        for (DebitNoteDetail noteDetail : dnds) {
//            debitNote = noteDetail.getDebitNote();
//            dnSet.add(debitNote.getID());
//        }
//
//        double dnAmt = 0;
//
//        for (String dnId : dnSet) {
//            KwlReturnObject dnReturnObj = kwlCommonTablesDAOObj.getObject(DebitNote.class.getName(), dnId);
//            DebitNote dn = (DebitNote) dnReturnObj.getEntityList().get(0);
//            dnAmt = dn.getDnamount();
//        }
//
//        // get Debited Amount of Customer Invoice
//
//        totalAmtDue = dnAmt + paidAmt;
//
//        return totalAmtDue;
//
//    }
    
     public List getGRAmountDue(Map<String, Object> request, GoodsReceipt gReceipt) throws ServiceException {
        List ll = new ArrayList();
        String accNames = "";
        double amountdue = 0;
        double amountdueinbase = 0;
        boolean belongsTo1099 = false;
        double amount = 0, ramount = 0, contraamount = 0, deductDiscount = 0, termAmount = 0,GoodsReceiptLineLevelTermAmt = 0, GoodsReceiptTDSAmt = 0 ;
        boolean isAged = (request.containsKey("isAged") && request.get("isAged") != null) ? Boolean.parseBoolean(request.get("isAged").toString()) : false;
        List<Double> knockedOffAmountList = new ArrayList();//variable used to hold knocked off amounts in invoice currency
        String currencyid = (String) request.get(GCURRENCYID);
        String currencyFilterForTrans = "";
        if (request.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = request.get("currencyfilterfortrans") != null ? (String) request.get("currencyfilterfortrans") : "";
        }
        String companyid = (String) request.get(COMPANYID);
        int countryid = 0;
        if(request.containsKey("countryid")){
            countryid = (int)request.get("countryid");
        }
        ArrayList acclist = new ArrayList();
        KwlReturnObject curresult = null;// accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
//        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID=currencyid;
        double amountDueOriginal = 0;
        boolean invoiceAmtDueEqualsInvoiceAmt = false;
        if (request.containsKey("invoiceAmtDueEqualsInvoiceAmt") && request.get("invoiceAmtDueEqualsInvoiceAmt") != null) { // check if orignal invoice amount equals to invoice amount due
            invoiceAmtDueEqualsInvoiceAmt = Boolean.parseBoolean(request.get("invoiceAmtDueEqualsInvoiceAmt").toString()); // if equal, then payment hasn't been made for invoice. Skip calculation and take amount due from invoice table
        }
        if (invoiceAmtDueEqualsInvoiceAmt && gReceipt.isNormalInvoice()) {
            amount=gReceipt.getInvoiceAmount();
            amountdue=gReceipt.getInvoiceamountdue();
            amountdueinbase = authHandler.round(gReceipt.getInvoiceAmountDueInBase(), companyid);
        }
        else {
            Iterator itrCn = applyDebitNotes(request, gReceipt).values().iterator();
            currencyid = (gReceipt.getCurrency() == null ?baseCurrencyID : gReceipt.getCurrency().getCurrencyID());
            while (itrCn.hasNext()) {
                Object[] temp = (Object[]) itrCn.next();
//                amount += (Double) temp[0] - (Double) temp[2];
                accNames += (String) temp[3];// required for 1099 report[PS]
                accNames += ",";
                acclist.add((String) temp[4]);
                deductDiscount += (Double) temp[5];
            }
            accNames = accNames.substring(0, Math.max(0, accNames.length() - 1));

            KwlReturnObject result = accTaxObj.belongsTo1099(companyid, acclist);
            List l = result.getEntityList();
            if (l.size() > 0) {
                belongsTo1099 = true;
            }       
        
//            JournalEntryDetail tempd = gReceipt.getTaxEntry();
//            if (tempd != null) {
//                amount += tempd.getAmount();
//            }
            double grExternalCurrencyRate = 0d;
            Date grCreationDate = null;
            grCreationDate = gReceipt.getCreationDate();
            if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
                grExternalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
                amount = gReceipt.getOriginalOpeningBalanceAmount();
            } else {
//                grCreationDate = gReceipt.getJournalEntry().getEntryDate();
                grExternalCurrencyRate = gReceipt.getJournalEntry().getExternalCurrencyRate();
                amount=gReceipt.getInvoiceAmount();
            }

            HashMap<String, Object> reqParams1 = new HashMap();
            reqParams1.put("grid", gReceipt.getID());
            reqParams1.put("companyid", companyid);
            if (request.containsKey(Constants.df) && request.get(Constants.df) != null) {
                reqParams1.put(Constants.df, request.get(Constants.df));
            }
            if (request.containsKey("asofdate") && request.get("asofdate") != null) {
                reqParams1.put("asofdate", request.get("asofdate"));
            }

            //Get amount knock off using otherwise credit notes.
//        KwlReturnObject dnresult = accDebitNoteobj.getDNRowsFromVendorInvoice(gReceipt.getID());
            KwlReturnObject dnresult = accDebitNoteobj.getDNRowsFromVendorInvoice(reqParams1);
            List<DebitNoteDetail> dnlist = dnresult.getEntityList();
            double cnAmountOW = 0;
            for (DebitNoteDetail dnr : dnlist) {
                boolean isopeningBalanceInvoice = gReceipt.isIsOpeningBalenceInvoice();

                String fromcurrencyid = (dnr.getDebitNote().getCurrency() == null ? baseCurrencyID : dnr.getDebitNote().getCurrency().getCurrencyID());
                if (dnr.getDiscount() != null) {
                    double extarnalCurrencyrate = 0;
                    extarnalCurrencyrate = dnr.getExchangeRateForTransaction();
                    if (extarnalCurrencyrate == 0) { // For Old records if exchage rate is ZERO , It will Go by Old way
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if gReceipt is opening balance gReceipt and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, currencyid, grCreationDate, grExternalCurrencyRate);
                    } else if (fromcurrencyid.equals(baseCurrencyID)) {
                        bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(request, dnr.getDiscount().getDiscountValue(), currencyid, grCreationDate, grExternalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, currencyid, grCreationDate, grExternalCurrencyRate);
                    }
                    cnAmountOW += (Double) bAmt.getEntityList().get(0);
                    } else {
                        // Use same Exchage rate used for Linking to Convert amount in invoice currency.
                        cnAmountOW += dnr.getDiscount().getDiscountValue() / extarnalCurrencyrate;
                }
            }
            }
            knockedOffAmountList.add(cnAmountOW);
//          cnAmountOW = authHandler.round(cnAmountOW, Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
//        result = accVendorPaymentobj.getPaymentsFromGReceipt(gReceipt.getID(), companyid);
            result = accVendorPaymentobj.getPaymentsFromGReceipt(reqParams1);
            List<PaymentDetail> list = result.getEntityList();
            if (list != null && !list.isEmpty()) {
                for (PaymentDetail pd : list) {
                    double ExchangeRate = 0d;
                    Date paymentCreationDate = null;
                    double externalCurrencyRate = 0d;
                    boolean isopeningBalancePayment = pd.getPayment().isIsOpeningBalencePayment();

                    paymentCreationDate = pd.getPayment().getCreationDate();
                    if (pd.getPayment().isIsOpeningBalencePayment() && !pd.getPayment().isNormalPayment()) {
                        externalCurrencyRate = pd.getPayment().getExchangeRateForOpeningTransaction();
                    } else {
//                        paymentCreationDate = pd.getPayment().getJournalEntry().getEntryDate();
                        externalCurrencyRate = pd.getPayment().getJournalEntry().getExternalCurrencyRate();
                    }

                    if (pd.getFromCurrency() != null && pd.getToCurrency() != null) {//Comment
//                    String fromcurrencyid = (pd.getPayment().getCurrency() == null ? baseCurrencyID : pd.getPayment().getCurrency().getCurrencyID());
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, pd.getAmount(), fromcurrencyid, currencyid, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
//                    double previousAmount = (Double) bAmt.getEntityList().get(0);
//                        ramount += pd.getAmount() / pd.getExchangeRateForTransaction();
                        ExchangeRate = pd.getExchangeRateForTransaction();
                        if(pd.getAmountDueInGrCurrency() != 0){
                            ExchangeRate = pd.getAmountDueInPaymentCurrency() / pd.getAmountDueInGrCurrency();
                        }
                        ramount += (pd.getAmount() + pd.getDiscountAmount()) / ExchangeRate;
                        ramount = authHandler.round(ramount, companyid);
                    } else {
//                    ramount += pd.getAmount();
                        String fromcurrencyid = (pd.getPayment().getCurrency() == null ? baseCurrencyID : pd.getPayment().getCurrency().getCurrencyID());
                        KwlReturnObject bAmt = null;
                        if (isopeningBalancePayment && pd.getPayment().isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
                        }
                        ramount += (Double) bAmt.getEntityList().get(0);
                    }

                }
            }
            knockedOffAmountList.add(ramount);
            
            result = accVendorPaymentobj.getLinkedDetailsPayment(reqParams1);
            List<LinkDetailPayment> linkedDetaisPayments = result.getEntityList();
            double amtlinkedToPayment = 0;
            if (linkedDetaisPayments != null && !linkedDetaisPayments.isEmpty()) {
                for (LinkDetailPayment ldp : linkedDetaisPayments) {
                    amtlinkedToPayment += ldp.getAmountInGrCurrency();
                }
            }
            knockedOffAmountList.add(amtlinkedToPayment);
            
            result = accVendorPaymentobj.getContraPayReceiptFromGReceipt(gReceipt.getID(), companyid);
            List<ReceiptDetail> list1 = result.getEntityList();
            if (list1 != null && !list1.isEmpty()) {
                for (ReceiptDetail pd : list1) {
                    contraamount += pd.getAmount();
                    String fromcurrencyid = (pd.getReceipt().getCurrency() == null ? baseCurrencyID : pd.getReceipt().getCurrency().getCurrencyID());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, contraamount, fromcurrencyid, currencyid, grCreationDate, grExternalCurrencyRate);
                    contraamount = (Double) bAmt.getEntityList().get(0);
                }
            }
            knockedOffAmountList.add(contraamount);
            
            // Get amount from Invoice Terms 
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("invoiceid", gReceipt.getID());
            curresult = accGoodsReceiptobj.getInvoiceTermMap(requestParam);
            List<ReceiptTermsMap> termMap = curresult.getEntityList();
            for (ReceiptTermsMap invoiceTerMap : termMap) {
                InvoiceTermsSales mt = invoiceTerMap.getTerm();
                termAmount += invoiceTerMap.getTermamount();
            }

            if (countryid == Constants.indian_country_id) {
                // Get amount From GoodsReceiptInvoice Detail Terms (Only For India)
                HashMap<String, Object> goodsreceiptdetailTermmap = new HashMap();
                goodsreceiptdetailTermmap.put("GoodsReceiptid", gReceipt.getID());
                curresult = accGoodsReceiptobj.getGoodsReceiptdetailTermMap(goodsreceiptdetailTermmap);
                List<ReceiptDetailTermsMap> GoodsReceiptLineLevelTermMap = curresult.getEntityList();
                for (ReceiptDetailTermsMap receiptDetailTermsMap : GoodsReceiptLineLevelTermMap) {
                    GoodsReceiptLineLevelTermAmt += receiptDetailTermsMap.getTermamount();
                }
                
                //Get TDS Amt From GoodsReceiptInvoice(Only For India)
                GoodsReceiptTDSAmt += gReceipt.getTdsAmount();
            }
            KwlReturnObject resultDn = accDebitNoteobj.getDistintDNFromGReceipt(gReceipt.getID());
            List listDn = resultDn.getEntityList();
            DebitNote debitNote = null;
            double termAmountDn = 0;
            if (listDn != null && !listDn.isEmpty()) {
                for (Object dn : listDn) {
                    debitNote = (DebitNote) dn;
                    if (debitNote != null) {
                        requestParam.put("debitNoteId", debitNote.getID());
                        curresult = accDebitNoteobj.getDebitNoteTermMap(requestParam);
                        List<DebitNoteTermsMap> termMapDn = curresult.getEntityList();
                        for (DebitNoteTermsMap debitNoteTermsMap : termMapDn) {
                            InvoiceTermsSales mt = debitNoteTermsMap.getTerm();
                            termAmountDn += debitNoteTermsMap.getTermamount();
                        }
                    }
                }
            }
            knockedOffAmountList.add(termAmountDn);
            // below calculation of amountdueinbase is done for removing rounding off issues in balance sheet and Aged Report
            double knockedOffAmtInBase=0;
            for (double knockedOffAmount : knockedOffAmountList) {
                if (knockedOffAmount != 0) {
                    KwlReturnObject grAmtInBaseResult = null;
                    if (gReceipt.isIsOpeningBalenceInvoice() && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, knockedOffAmount, currencyid, grCreationDate, grExternalCurrencyRate);
                    } else {
                        grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(request, knockedOffAmount, currencyid, grCreationDate, grExternalCurrencyRate);
                    }
                    if (grAmtInBaseResult != null) {
                        //Doing round off each value before summing for matching aged amount with balance sheet in base currency 
                        knockedOffAmtInBase += authHandler.round((Double) grAmtInBaseResult.getEntityList().get(0), companyid);
                    }
                }
            }
            amountdueinbase= authHandler.round((gReceipt.getInvoiceAmountInBase()-knockedOffAmtInBase), companyid);
            
//            amount = authHandler.round(amount, 2);
            /**
             * remove 'termamount' because its already calculated in amount.
             */
            amountdue = amount - cnAmountOW - ramount - contraamount - termAmountDn - amtlinkedToPayment;
            
//            if(countryid == Constants.indian_country_id ){
            if(countryid == Constants.indian_country_id && !isAged){
                //Adding GoodsReceiptInvoice LineLevel Term Amt & subtracting GoodsReceiptInvoice TDS Amt.
                amountdue += (GoodsReceiptLineLevelTermAmt - GoodsReceiptTDSAmt);
            }
            if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {
                amountDueOriginal = amountdue;
                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, grCreationDate, grExternalCurrencyRate);
                    amountdue = (Double) bAmt.getEntityList().get(0);
                }
            } else {
                amount = 0;
                amountdue = 0;
            }
        }
        ll.add(amount);
        ll.add(amountdue);
        ll.add(accNames);
        ll.add(belongsTo1099);
        ll.add(deductDiscount);
        ll.add(amountDueOriginal);
        ll.add(amountdueinbase);
        return ll;
    }
    
    public List getBillingGRAmountDue(Map<String, Object> request, BillingGoodsReceipt gReceipt) throws ServiceException {
            List ll = new ArrayList();
        try {
            String accNames="";
             double amountdue=0;
             boolean belongsTo1099=false;
             double amount = 0, ramount = 0;
             String currencyid = (String) request.get(GCURRENCYID);
             String companyid = (String) request.get(COMPANYID);
             ArrayList acclist=new ArrayList();
             KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
             KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
             String baseCurrencyID=baseCurrency.getCurrencyID();
             Iterator itrCn = applyBillingDebitNotes(request, gReceipt).values().iterator();
             currencyid = (gReceipt.getCurrency() == null ?baseCurrencyID : gReceipt.getCurrency().getCurrencyID());
             while (itrCn.hasNext()) {
                 Object[] temp = (Object[]) itrCn.next();
                 amount += (Double) temp[0] - (Double) temp[2];
             }

             JournalEntryDetail tempd = gReceipt.getTaxEntry();
             if (tempd != null) {
                 amount += tempd.getAmount();
             }
            KwlReturnObject result = accVendorPaymentobj.getBillingPaymentsFromGReceipt(gReceipt.getID(), companyid);
            List<BillingPaymentDetail> list= result.getEntityList();
             if (list != null && !list.isEmpty()){
                 for (BillingPaymentDetail pd : list){
                     ramount += pd.getAmount();
                     String fromcurrencyid = (pd.getBillingPayment().getCurrency() == null ? baseCurrencyID : pd.getBillingPayment().getCurrency().getCurrencyID());
                     KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, ramount, fromcurrencyid, currencyid, gReceipt.getJournalEntry().getEntryDate(),gReceipt.getJournalEntry().getExternalCurrencyRate());
                     ramount = (Double) bAmt.getEntityList().get(0);
                 }
             }
             amountdue=amount-ramount;
             ll.add(amount);
             ll.add(amountdue);
             ll.add(accNames);
             ll.add(belongsTo1099);
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accGoodsReceiptCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ll;
    }
    public List getExpGRAmountDue(Map<String, Object> request, GoodsReceipt gReceipt) throws ServiceException {
        List ll = new ArrayList();
        List<Double> knockedOffAmountList = new ArrayList();//variable used to hold knocked off amounts in invoice currency
        double amount = 0, ramount = 0, contraamount = 0, amountdue = 0,amountdueinbase = 0, termAmount = 0;
        boolean belongsTo1099 = false;
        String currencyid = (String) request.get(GCURRENCYID);
        String accNames = "";
        String currencyFilterForTrans = "";
        if (request.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = request.get("currencyfilterfortrans") != null ? (String) request.get("currencyfilterfortrans") : "";
        }
        double amountDueOriginal = 0;
        String companyid = (String) request.get(COMPANYID);
        KwlReturnObject curresult = null;// accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
//        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID = currencyid;
        currencyid = (gReceipt.getCurrency() == null ? baseCurrencyID : gReceipt.getCurrency().getCurrencyID());
        Set<ExpenseGRDetail> grRows = gReceipt.getExpenserows();
        ArrayList acclist = new ArrayList();
        if (grRows != null && !grRows.isEmpty()) {
            for (ExpenseGRDetail temp : grRows) {
//                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
//                double rowTaxPercent = 0;
//                double rowTaxAmount = 0;
//                boolean isRowTaxApplicable = false;
//                if (temp.getTax() != null) {
//                    KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, gReceipt.getJournalEntry().getEntryDate(), temp.getTax().getID());
//                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
//                    isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
//                }
//                double rate = temp.getRate();
//                if (gReceipt.isGstIncluded()) {
//                    ramount = temp.getRateIncludingGst();
//                } else {
//                    ramount = temp.getRate() - rdisc;
//                }
//               
//                if (temp.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
//                    if (isRowTaxApplicable) {
//                        rowTaxAmount = temp.getRowTaxAmount();
//                    }
//                } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
//                    rowTaxAmount = ramount * rowTaxPercent / 100;
//                }
//                if (temp.isIsdebit()) {
//                    amount += ramount + rowTaxAmount;
//                } else {
//                    amount -= (ramount + rowTaxAmount);
//                }
//                amount += ramount + rowTaxAmount;//amount+=ramount+ramount*rowTaxPercent/100;
                accNames += temp.getAccount().getName();// required for 1099 report[PS]
                accNames += ",";
                acclist.add((String) temp.getAccount().getID());
            }
        }
        amount = gReceipt.getInvoiceAmount();
        accNames = accNames.substring(0, Math.max(0, accNames.length() - 1));
        KwlReturnObject result = accTaxObj.belongsTo1099(companyid, acclist);
        List l = result.getEntityList();
        if (l.size() > 0) {
            belongsTo1099 = true;
        }
        boolean invoiceAmtDueEqualsInvoiceAmt = false;
        if (request.containsKey("invoiceAmtDueEqualsInvoiceAmt") && request.get("invoiceAmtDueEqualsInvoiceAmt") != null) { // check if orignal invoice amount equals to invoice amount due
            invoiceAmtDueEqualsInvoiceAmt = Boolean.parseBoolean(request.get("invoiceAmtDueEqualsInvoiceAmt").toString()); // if equal, then payment hasn't been made for invoice. Skip calculation and take amount due from invoice table
        }
        if (!invoiceAmtDueEqualsInvoiceAmt) {
//            double disc = (gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue());//main gr discount
//            amount -= disc; //discount on invoice[PS]
//            JournalEntryDetail tempd = gReceipt.getTaxEntry();
//            if (tempd != null) {
//                amount += tempd.getAmount(); //tax on invoice[PS]
//            }

            HashMap<String, Object> reqParams1 = new HashMap();
            reqParams1.put("grid",gReceipt.getID());
            reqParams1.put("companyid",companyid);
            if(request.containsKey(Constants.df) && request.get(Constants.df)!=null){
                reqParams1.put(Constants.df, request.get(Constants.df));
            }
            if(request.containsKey("asofdate") && request.get("asofdate")!=null){
                reqParams1.put("asofdate", request.get("asofdate"));
            }


            double dnAmountOW = 0;
            KwlReturnObject dnresult = accDebitNoteobj.getDNRowsFromVendorInvoice(reqParams1);
            List<DebitNoteDetail> dnlist = dnresult.getEntityList();
            for (DebitNoteDetail dnr : dnlist) {
                String fromcurrencyid = (dnr.getDebitNote().getCurrency() == null ? baseCurrencyID : dnr.getDebitNote().getCurrency().getCurrencyID());
                if (dnr.getDiscount() != null) {
                    double extarnalCurrencyrate = 0;
                    extarnalCurrencyrate = dnr.getExchangeRateForTransaction();
                    if (extarnalCurrencyrate == 0) { // For Old records if exchage rate is ZERO , It will Go by Old way
                        KwlReturnObject bAmt = null;
//                        Date grCreationDate = gReceipt.getJournalEntry().getEntryDate();
                        Date grCreationDate = gReceipt.getCreationDate();
                        double grExternalCurrencyRate = gReceipt.getJournalEntry().getExternalCurrencyRate();
                        if (fromcurrencyid.equals(baseCurrencyID)) {
                            bAmt = accCurrencyDAOobj.getBaseToCurrencyAmount(request, dnr.getDiscount().getDiscountValue(), currencyid, grCreationDate, grExternalCurrencyRate);
                        } else {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, currencyid, grCreationDate, grExternalCurrencyRate);
                        }
                        dnAmountOW += (Double) bAmt.getEntityList().get(0);
                    } else {
                        // Use same Exchage rate used for Linking to Convert amount in invoice currency.
                        dnAmountOW += dnr.getDiscount().getDiscountValue() / extarnalCurrencyrate;
                    }
                }
            }
            knockedOffAmountList.add(dnAmountOW);

            result = accVendorPaymentobj.getPaymentsFromGReceipt(reqParams1);
            List<PaymentDetail> list = result.getEntityList();
            ramount = 0;
            if (list != null && !list.isEmpty()) {
                for (PaymentDetail pd : list) {
                    if (pd.getFromCurrency() != null && pd.getToCurrency() != null) {
                        // Applying exchnage rate by calculating (amount due in payment currency / amountdue in Expense PI Currency)
                        double exchangeRate = pd.getExchangeRateForTransaction();
                        if (pd.getAmountDueInGrCurrency() != 0) {
                            exchangeRate = pd.getAmountDueInPaymentCurrency() / pd.getAmountDueInGrCurrency();
                        }
                        ramount += (pd.getAmount() + pd.getDiscountAmount()) / exchangeRate;
                        ramount = authHandler.round(ramount, companyid);
//                        ramount += pd.getAmount() / pd.getExchangeRateForTransaction();
                    } else {
                        String fromcurrencyid = (pd.getPayment().getCurrency() == null ? baseCurrencyID : pd.getPayment().getCurrency().getCurrencyID());
//                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, pd.getAmount(), fromcurrencyid, currencyid, pd.getPayment().getJournalEntry().getEntryDate(), pd.getPayment().getJournalEntry().getExternalCurrencyRate());
                        KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, (pd.getAmount()+pd.getDiscountAmount()), fromcurrencyid, currencyid, pd.getPayment().getCreationDate(), pd.getPayment().getJournalEntry().getExternalCurrencyRate());
                        ramount += (Double) bAmt.getEntityList().get(0);
                    }
                }
            }
            knockedOffAmountList.add(ramount);

            result= accVendorPaymentobj.getLinkedDetailsPayment(reqParams1);
            List<LinkDetailPayment> linkedDetaisPayments = result.getEntityList();
            double amtlinkedToPayment= 0;
            if(linkedDetaisPayments!=null && !linkedDetaisPayments.isEmpty()){
                for(LinkDetailPayment ldp: linkedDetaisPayments){
                    amtlinkedToPayment+=ldp.getAmountInGrCurrency();
                }
            }
            knockedOffAmountList.add(amtlinkedToPayment);

            result = accVendorPaymentobj.getContraPayReceiptFromGReceipt(gReceipt.getID(), companyid);
            List<ReceiptDetail> list1 = result.getEntityList();
            contraamount = 0;
            if (list1 != null && !list1.isEmpty()) {
                for (ReceiptDetail pd : list1) {
                    contraamount = pd.getAmount();
                    String fromcurrencyid = (pd.getReceipt().getCurrency() == null ? baseCurrencyID : pd.getReceipt().getCurrency().getCurrencyID());
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, contraamount, fromcurrencyid, currencyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, contraamount, fromcurrencyid, currencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                    contraamount = (Double) bAmt.getEntityList().get(0);
                }
            }
            knockedOffAmountList.add(contraamount);
            // Get amount from Invoice Terms 
//            HashMap<String, Object> requestParam = new HashMap();
//            requestParam.put("invoiceid", gReceipt.getID());
//            curresult = accGoodsReceiptobj.getInvoiceTermMap(requestParam);
//            List<ReceiptTermsMap> termMap = curresult.getEntityList();
//            for (ReceiptTermsMap invoiceTerMap : termMap) {
//                InvoiceTermsSales mt = invoiceTerMap.getTerm();
//                termAmount += invoiceTerMap.getTermamount();
//            }
            // below calculation of amountdueinbase is done for removing rounding off issues in balance sheet and Aged Report
            double knockedOffAmtInBase = 0;
            for (double knockedOffAmount : knockedOffAmountList) {
                if (knockedOffAmount != 0) {
                    KwlReturnObject grAmtInBaseResult = null;
                    if (gReceipt.isIsOpeningBalenceInvoice() && gReceipt.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
//                        grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, knockedOffAmount, currencyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                        grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(request, knockedOffAmount, currencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                    } else {
//                        grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(request, knockedOffAmount, currencyid, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                        grAmtInBaseResult = accCurrencyDAOobj.getCurrencyToBaseAmount(request, knockedOffAmount, currencyid, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                    }
                    if (grAmtInBaseResult != null) {
                        //Doing round off each value before summing for matching aged amount with balance sheet in base currency 
                        knockedOffAmtInBase += authHandler.round((Double) grAmtInBaseResult.getEntityList().get(0), companyid);
                    }
                }
            }
            amountdueinbase = authHandler.round((gReceipt.getInvoiceAmountInBase() - knockedOffAmtInBase), companyid);
            amountdue = amount - dnAmountOW - ramount - contraamount -amtlinkedToPayment;
            if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {//amount due difference for revaluation.
                amountDueOriginal = amountdue;
                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, gReceipt.getCreationDate(), gReceipt.getJournalEntry().getExternalCurrencyRate());
                    amountdue = (Double) bAmt.getEntityList().get(0);
                }
            } else {
                amount = 0;
                amountdue = 0;
            }
        }
        if (amountdueinbase == 0 && invoiceAmtDueEqualsInvoiceAmt) {//invoiceAmtDueEqualsInvoiceAmt is coming true only for normal invoice in case of opening it came as false so no need to handle opening case in below code
            amountdueinbase = authHandler.round(gReceipt.getInvoiceAmountDueInBase(), companyid);
        }
        
        ll.add(amount);
        ll.add(amountdue);
        ll.add(accNames);
        ll.add(belongsTo1099);
        ll.add(amountDueOriginal);
        ll.add(amountdueinbase);
        return ll;
    }
    
    List getUpdatedExpGRAmountDue(HashMap<String, Object> request, GoodsReceipt gReceipt) throws ServiceException {
        double amountdue = 0;
        List ll = new ArrayList();
        boolean belongsTo1099 = false;
        String accNames = "";
        String currencyFilterForTrans = "";
        double amountDueOriginal = 0;
        String companyid = (String) request.get(COMPANYID);
        String currencyid = (gReceipt.getCurrency() == null ? (String) request.get(GCURRENCYID) : gReceipt.getCurrency().getCurrencyID());

        if (request.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = request.get("currencyfilterfortrans") != null ? (String) request.get("currencyfilterfortrans") : "";
        }

        Set<ExpenseGRDetail> grRows = gReceipt.getExpenserows();
        ArrayList acclist = new ArrayList();
        if (grRows != null && !grRows.isEmpty()) {
            for (ExpenseGRDetail temp : grRows) {
                accNames += temp.getAccount().getName();// required for 1099 report[PS]
                accNames += ",";
                acclist.add((String) temp.getAccount().getID());
            }
        }
        accNames = accNames.substring(0, Math.max(0, accNames.length() - 1));
        KwlReturnObject result = accTaxObj.belongsTo1099(companyid, acclist);
        List l = result.getEntityList();
        if (l.size() > 0) {
            belongsTo1099 = true;
        }
        boolean invoiceAmtDueEqualsInvoiceAmt = false;
        if (request.containsKey("invoiceAmtDueEqualsInvoiceAmt") && request.get("invoiceAmtDueEqualsInvoiceAmt") != null) { // check if orignal invoice amount equals to invoice amount due
             invoiceAmtDueEqualsInvoiceAmt = Boolean.parseBoolean(request.get("invoiceAmtDueEqualsInvoiceAmt").toString()); // if equal, then payment hasn't been made for invoice. Skip calculation and take amount due from invoice table
        }
        if(!invoiceAmtDueEqualsInvoiceAmt){
//            Date grCreationDate = gReceipt.getJournalEntry().getEntryDate();
            Date grCreationDate = gReceipt.getCreationDate();
            double grExternalCurrencyRate = gReceipt.getJournalEntry().getExternalCurrencyRate();
            amountdue = gReceipt.getInvoiceamountdue();
            if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {//amount due difference for revaluation.
                amountDueOriginal = amountdue;
                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, grCreationDate, grExternalCurrencyRate);
                    amountdue = (Double) bAmt.getEntityList().get(0);
                }
            } else {
                amountdue = 0;
            }
        }
        ll.add(0.0);
        ll.add(amountdue);
        ll.add(accNames);
        ll.add(belongsTo1099);
        ll.add(amountDueOriginal);
        return ll;
    }

  public Map getGoodsReceiptProductAmount(GoodsReceipt gr, String companyid) throws ServiceException {
        Map<GoodsReceiptDetail, Object[]> hm = new HashMap<GoodsReceiptDetail, Object[]>();
        Set<GoodsReceiptDetail> invRows = gr.getRows();
        double amount;
        double quantity;
//        Iterator itr = invRows.iterator();
//        while (itr.hasNext()) {
//        GoodsReceiptDetail temp = (GoodsReceiptDetail) itr.next();
         if (invRows != null && !invRows.isEmpty())
        {
            for (GoodsReceiptDetail temp: invRows)
            {
//                quantity = (gr.getPendingapproval() == 1)? temp.getInventory().getActquantity() : (temp.getInventory().isInvrecord() ? temp.getInventory().getQuantity() : temp.getInventory().getActquantity());
                quantity = temp.getInventory().getQuantity();
                amount = authHandler.round(temp.getRate() * quantity, companyid);
                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                 double rowTaxPercent = 0;
                 double rowTaxAmount=0;
                 boolean isRowTaxApplicable=false;
                if (temp.getTax() != null) {
    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
//                    KwlReturnObject perresult = accTaxObj.getTaxPercent( gr.getCompany().getCompanyID(), gr.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent( gr.getCompany().getCompanyID(), gr.getCreationDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                }
                if (temp.isWasRowTaxFieldEditable()) {//After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                    if (isRowTaxApplicable) {
                        rowTaxAmount = temp.getRowTaxAmount();
                    }
                } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                    rowTaxAmount = (amount - rdisc) * rowTaxPercent / 100;
                }
                 double ramount=amount - rdisc;
                 double amountWithoutTax=amount - rdisc;
                 ramount+=rowTaxAmount;//ramount+=ramount*rowTaxPercent/100;
                hm.put(temp, new Object[]{ramount, quantity,rowTaxAmount,amountWithoutTax});
                if (gr == null) {
                    gr = temp.getGoodsReceipt();
                }
            }
         }
        return hm;
    }
  
    public Map getGRDetailsTermAmount(GoodsReceiptDetail grd) throws ServiceException {
        Map<String, Double> map = new HashMap<>();
        double termAmount = 0;
        double taxAmount = grd.getRowTermTaxAmount();
        String grdetailID = grd.getID();
        KwlReturnObject listTerms = accGoodsReceiptobj.getReceiptTermMapFromGRDetail(grdetailID);
        List<ReceiptTermsMap> receiptTermMaps = (List) listTerms.getEntityList();
        for (ReceiptTermsMap rtm : receiptTermMaps) {
            termAmount += rtm.getTermamount();
        }
        map.put("termamount", termAmount);
        map.put("taxamount", taxAmount);
        return map;
    }

    public Map getExpenseGRAmount(GoodsReceipt gr) throws ServiceException {
        Map<ExpenseGRDetail, Object[]> hm = new HashMap<ExpenseGRDetail, Object[]>();
        Set<ExpenseGRDetail> invRows = gr.getExpenserows();
//        Iterator itr = invRows.iterator();
//        while (itr.hasNext()) {
//            ExpenseGRDetail temp = (ExpenseGRDetail) itr.next();
        if (invRows != null && !invRows.isEmpty())
        {
            for (ExpenseGRDetail temp: invRows)
            {
                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                double rowTaxPercent = 0;
                double rowTaxAmount=0;
                boolean isRowTaxApplicable=false;
                if (temp.getTax() != null) {

    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
//                    KwlReturnObject perresult = accTaxObj.getTaxPercent( gr.getCompany().getCompanyID(), gr.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent( gr.getCompany().getCompanyID(), gr.getCreationDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                }
                if (temp.isWasRowTaxFieldEditable()) {//After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                    if (isRowTaxApplicable) {
                        rowTaxAmount = temp.getRowTaxAmount();
                    }
                } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                    rowTaxAmount = (temp.getRate() - rdisc) * rowTaxPercent / 100;
                }
                double rate = 0.0;
                /*
                 * If Including Gst is true
                 */
                if (temp.getGoodsReceipt().isGstIncluded()) {
                    rate = temp.getRateExcludingGst();
                } else {
                    /*
                     * If Including Gst is false
                     */
                    rate = temp.getRate();
                }
                double ramount = rate - rdisc;
                ramount += rowTaxAmount;//ramount+=ramount*rowTaxPercent/100;
                hm.put(temp, new Object[]{ramount, rowTaxAmount});
                if (gr == null) {
                    gr = temp.getGoodsReceipt();
                }
            }
        }
        return hm;
    }

    public Map getBillingGoodsReceiptProductAmount(BillingGoodsReceipt gr) throws ServiceException{
        Map<BillingGoodsReceiptDetail, Object[]> hm=new HashMap<BillingGoodsReceiptDetail, Object[]>();
        Set<BillingGoodsReceiptDetail> invRows=gr.getRows();
        double amount;
        double quantity;
//        Iterator itr=invRows.iterator();
//        while(itr.hasNext()){
//            BillingGoodsReceiptDetail temp=(BillingGoodsReceiptDetail)itr.next();
//

        if(invRows!=null && !invRows.isEmpty()){
            for(BillingGoodsReceiptDetail temp:invRows){
                quantity=temp.getQuantity();
                amount=temp.getRate()*quantity;
                double rdisc=(temp.getDiscount()==null?0:temp.getDiscount().getDiscountValue());
                             double rowTaxPercent = 0;
                             double rowTaxAmount = 0;
                             boolean isRowTaxApplicable=false;
                if (temp.getTax() != null) {
    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent( gr.getCompany().getCompanyID(), gr.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                }
                 double ramount=amount - rdisc;
                 if (temp.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                    if (isRowTaxApplicable) {
                        rowTaxAmount = temp.getRowTaxAmount();
                    }
                } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                    rowTaxAmount = ramount * rowTaxPercent / 100;
                }
                 ramount+=rowTaxAmount;//ramount+=ramount*rowTaxPercent/100;
                hm.put(temp, new Object[]{ramount, quantity});

               // hm.put(temp, new Object[]{amount-rdisc,quantity});
                if(gr==null)gr=temp.getBillingGoodsReceipt();
            }
        }
        return hm;
    }

    public Map applyBillingDebitNotes(Map request, BillingGoodsReceipt gReceipt) throws ServiceException, SessionExpiredException {
        Map<BillingGoodsReceiptDetail, Object[]> hm = new HashMap<BillingGoodsReceiptDetail, Object[]>();
        Set<BillingGoodsReceiptDetail> grRows = gReceipt.getRows();
        KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), (String) request.get(GCURRENCYID));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        double amount;
        double quantity;
        double disc = 0;//(gReceipt.getDiscount() == null ? 0 : gReceipt.getDiscount().getDiscountValue()) / grRows.size();
        if(grRows!=null && !grRows.isEmpty()){
            for(BillingGoodsReceiptDetail temp:grRows){
                quantity = temp.getQuantity();
                amount = temp.getRate() * quantity;
                double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
                double rowTaxPercent = 0;
                double rowTaxAmount=0;
                boolean isRowTaxApplicable=false;
                if (temp.getTax() != null) {
                    KwlReturnObject perresult = accTaxObj.getTaxPercent((String) request.get(COMPANYID), gReceipt.getJournalEntry().getEntryDate(), temp.getTax().getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    isRowTaxApplicable = (Boolean) perresult.getEntityList().get(1);
                }
                double ramount=amount-rdisc;
                if(temp.isWasRowTaxFieldEditable()){//After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                    if (isRowTaxApplicable) {
                        rowTaxAmount = temp.getRowTaxAmount();
                    }
                }else{// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                    rowTaxAmount = ramount*rowTaxPercent/100;
                }
                
                ramount+=rowTaxAmount;//ramount+=ramount*rowTaxPercent/100;
                ramount-=disc;
                hm.put(temp, new Object[]{ramount, quantity, 0.0, 0.0,rowTaxAmount});
                if (gReceipt == null) gReceipt = temp.getBillingGoodsReceipt();
            }
        }
        KwlReturnObject llObject = accDebitNoteobj.getDNRFromBDN(gReceipt.getID());        
        
        List<Object[]> list = llObject.getEntityList();
        if(list!=null && !list.isEmpty()){
            for(Object[] dnrow:list){
                double taxAmount=0, totalDiscount = 0;
                BillingDebitNoteDetail dnr = (BillingDebitNoteDetail) dnrow[1];
                BillingGoodsReceiptDetail temp = dnr.getGoodsReceiptRow();
                if (!hm.containsKey(temp))continue;
                Object[] val = (Object[]) hm.get(temp);
                String fromcurrencyid=(dnr.getDebitNote().getCurrency()==null?currency.getCurrencyID(): dnr.getDebitNote().getCurrency().getCurrencyID());
                String tocurrencyid=(gReceipt.getCurrency()==null?currency.getCurrencyID(): gReceipt.getCurrency().getCurrencyID());
                double v=(Double)val[0]-(dnr.getDiscount()==null?0:(Double)accCurrencyDAOobj.getOneCurrencyToOther(request,dnr.getDiscount().getDiscountValue(),fromcurrencyid,tocurrencyid,gReceipt.getJournalEntry().getEntryDate(), gReceipt.getJournalEntry().getExternalCurrencyRate()).getEntityList().get(0));
                if(dnr.getTaxAmount()!=null) {
                    taxAmount = (Double)val[2] + dnr.getTaxAmount();
                } else {
                    taxAmount = (Double)val[2];
                }
                if(dnr.getTotalDiscount()!=null) {
                    totalDiscount = (Double)val[3] + dnr.getTotalDiscount();
                } else {
                    totalDiscount = (Double)val[3];
                }
                double q=(Double)val[1];
                q-=dnr.getQuantity();
                hm.put(temp, new Object[]{v,q,taxAmount, totalDiscount,0.0});
            }
        }
        return hm;
    }


    public JSONArray getBillingGoodsReceiptRows(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        JSONArray jArr = new JSONArray();
		try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(COMPANYID, sessionHandlerImpl.getCompanyid(request));
            requestParams.put(GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            requestParams.put(BILLS, request.getParameterValues(BILLS));

            String companyid = (String) requestParams.get(COMPANYID);
            String currencyid = (String) requestParams.get(GCURRENCYID);
            KwlReturnObject curresult = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);

//            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
            String[] invoices=request.getParameterValues(BILLS);
            int i=0;

            HashMap<String, Object> grRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("billingGoodsReceipt.ID");
            order_by.add(SRNO);
            order_type.add("asc");
            grRequestParams.put( FILTER_NAMES,filter_names);
            grRequestParams.put( FILTER_PARAMS,filter_params);
            grRequestParams.put( ORDER_BY,order_by);
            grRequestParams.put( ORDER_TYPE,order_type);

            while(invoices!=null&&i<invoices.length){
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Vendor_Invoice_ModuleId,1));
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParams, replaceFieldMap);

//                BillingGoodsReceipt invoice=(BillingGoodsReceipt)session.get(BillingGoodsReceipt.class, invoices[i]);
                BillingGoodsReceipt invoice = (BillingGoodsReceipt) kwlCommonTablesDAOObj.getClassObject(BillingGoodsReceipt.class.getName(), invoices[i]);
//                Iterator itr = invoice.getRows().iterator();
                filter_params.clear();
                filter_params.add(invoice.getID());
                KwlReturnObject grdresult = accGoodsReceiptobj.getBillingGoodsReceiptDetails(grRequestParams);
//                Iterator itr = grdresult.getEntityList().iterator();
//                while(itr.hasNext()) {
//                    BillingGoodsReceiptDetail row=(BillingGoodsReceiptDetail)itr.next();

                Map hm= applyBillingDebitNotes(requestParams,invoice);
                List<BillingGoodsReceiptDetail> list=grdresult.getEntityList();
                if (list != null && !list.isEmpty())
                {
                    for (BillingGoodsReceiptDetail row: list)
                    {
                        JSONObject obj = new JSONObject();
                        
                        // ## Get Custom Column Data 
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                        ArrayList Detailfilter_names = new ArrayList(),Detailfilter_params = new ArrayList();
//                        Detailfilter_names.add("jedetail.ID");
//                        Detailfilter_params.add(row.getDebtorEntry().getID());
                        Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                        Detailfilter_params.add(row.getID());
                        invDetailRequestParams.put("filter_names", Detailfilter_names);
                        invDetailRequestParams.put("filter_params", Detailfilter_params);
                        KwlReturnObject idcustresult = accGoodsReceiptobj.getGoodsReceiptCustomData(invDetailRequestParams);
                        if(idcustresult.getEntityList().size()>0) {
                            AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                            replaceFieldMap.clear();
                            AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap,variableMap);
                            for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                                String coldata = varEntry.getValue()!=null?varEntry.getValue().toString():"";
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    obj.put(varEntry.getKey(), coldata);
                                }
                            }
                        }
                        // ## End Custom Field Data
            
                        obj.put(BILLID, invoice.getID());
                        obj.put(BILLNO, invoice.getBillingGoodsReceiptNumber());
                        currencyid=(invoice.getCurrency()==null?currency.getCurrencyID(): invoice.getCurrency().getCurrencyID());
                        obj.put(CURRENCYSYMBOL, (invoice.getCurrency()==null?currency.getCurrencyID():invoice.getCurrency().getSymbol()));
                        obj.put(OLDCURRENCYRATE, accCurrencyDAOobj.getBaseToCurrencyAmount(requestParams,1.0,currencyid,invoice.getJournalEntry().getEntryDate(),invoice.getJournalEntry().getExternalCurrencyRate()));
                        obj.put(SRNO, row.getSrno());
                        obj.put(ROWID, row.getID());
                        obj.put(PRODUCTDETAIL,row.getProductDetail());
                        obj.put(QUANTITY,row.getQuantity());
                        obj.put("creditoraccount", row.getDebtorEntry()== null?"":row.getDebtorEntry().getAccount().getID());
                        Discount disc= row.getDiscount();
                        if (disc != null ) {
                            obj.put(PRDISCOUNT, disc.getDiscount());
                            obj.put("discountispercent", disc.isInPercent() ? 1 : 0); 
                        } else {
                            obj.put(PRDISCOUNT, 0);
                            obj.put("discountispercent", 1);
                        }
                        obj.put(RATE, row.getRate());
                        double remainingquantity=0;
                        double amount=0;
                        double rowTaxAmount=0;
                        if(hm.containsKey(row)){
                            Object[] val=(Object[])hm.get(row);
                            amount=(Double)val[0];
                            rowTaxAmount=(Double)val[4];
                            remainingquantity=(Double)val[1];
                            obj.put( REMAININGQUANTITY,remainingquantity);
                            obj.put(REMQUANTITY, 0);
                            obj.put( AMOUNT,amount);
                            obj.put("rowTaxAmount", rowTaxAmount);
                            obj.put("taxamount", rowTaxAmount);
                        }
                        Map amthm=getBillingGoodsReceiptProductAmount(invoice);
                        Object[] val=(Object[])amthm.get(row);
                        amount=(Double)val[0];
                        obj.put( ORIGNALAMOUNT,amount);
                        double taxPercent = 0;
                        double rowTaxPercent = 0;
                        if (row.getPurchaseOrderDetail() != null) {
                            obj.put("linkto", row.getPurchaseOrderDetail().getPurchaseOrder().getPurchaseOrderNumber());
                            obj.put("linkid", row.getPurchaseOrderDetail().getPurchaseOrder().getID());
                            obj.put("rowid", row.getPurchaseOrderDetail().getID());
                            obj.put("savedrowid", row.getPurchaseOrderDetail().getID());
                            obj.put("linktype", 0);
                        } else {
                            obj.put("linkto", "");
                            obj.put("linkid", "");
                            obj.put("linktype", -1);
                        }
                        if (row.getTax() != null) {
    //                            percent = CompanyHandler.getTaxPercent(session, request, invoice.getJournalEntry().getEntryDate(), invoice.getTax().getID());
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), row.getTax().getID());
                            rowTaxPercent = (Double) perresult.getEntityList().get(0);
                        }
                        obj.put( PRTAXPERCENT,rowTaxPercent);
                        obj.put(PRTAXID, row.getTax()== null?"None":row.getTax().getID());
                        if(invoice.getTax()!=null){
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(),invoice.getTax().getID());
                            taxPercent = (Double) perresult.getEntityList().get(0);
                        }
                        obj.put( TAXPERCENT,taxPercent);
                        jArr.put(obj);
                    }
                }
            i++;

            }
        } catch (JSONException e) {
                throw ServiceException.FAILURE("accGoodsReceiptCMN.getBillingGoodsReceiptRows", e);
        }
        return jArr;
    }

    public List getOpenDebitNotes_vendor(HashMap<String, Object> requestParams, String vendorid) throws ServiceException {
        List ll = new ArrayList();
        String currencyid = (String) requestParams.get("gcurrencyid");
        String companyid = (String) requestParams.get("companyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID=baseCurrency.getCurrencyID();
        
        //Get amount knock off using otherwise open debit notes.
        KwlReturnObject result = accDebitNoteobj.getDNRowsOpen_vendor(vendorid);
        List list = result.getEntityList();
        Iterator dnitr = list.iterator();
        double dnAmountOW = 0;
        while (dnitr.hasNext()) {
            DebitNote dn = (DebitNote) dnitr.next();
            String fromcurrencyid=(dn.getCurrency()==null?baseCurrencyID: dn.getCurrency().getCurrencyID());
            double dnamountdue = dn.getDnamountdue();
            Date dnCreationDate = null;
            double externalCurrencyRate = 0;
            if(dn.isNormalDN()){
//                dnCreationDate = dn.getJournalEntry().getEntryDate();
                externalCurrencyRate = dn.getJournalEntry().getExternalCurrencyRate();
            }else if(dn.isIsOpeningBalenceDN() && !dn.isNormalDN()){
                dnamountdue = dn.getOpeningBalanceAmountDue();
                externalCurrencyRate = dn.getExchangeRateForOpeningTransaction();
            }
            dnCreationDate = dn.getCreationDate();
            if(dnamountdue > 0){
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, dnamountdue, fromcurrencyid, currencyid,dnCreationDate , externalCurrencyRate);
                dnAmountOW += (Double) bAmt.getEntityList().get(0);
            }
        }
        
        ll.add(dnAmountOW);
        return ll;
    }
    
    public List getOpenCreditNotes_vendor(HashMap<String, Object> requestParams, String vendorid) throws ServiceException {
        List ll = new ArrayList();
        String currencyid = (String) requestParams.get("gcurrencyid");
        String companyid = (String) requestParams.get("companyid");
        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID=baseCurrency.getCurrencyID();
        
        //Get amount of otherwise open credit notes.
        KwlReturnObject result = accDebitNoteobj.getCNRowsOpen_vendor(vendorid);
        List list = result.getEntityList();
        Iterator dnitr = list.iterator();
        double dnAmountOW = 0;
        while (dnitr.hasNext()) {
            CreditNote dn = (CreditNote) dnitr.next();
            String fromcurrencyid=(dn.getCurrency()==null?baseCurrencyID: dn.getCurrency().getCurrencyID());
            double dnamountdue = dn.getCnamountdue();
//            Date cnCreationDate = (dn.isNormalCN())?dn.getJournalEntry().getEntryDate():dn.getCreationDate();
            Date cnCreationDate = dn.getCreationDate();
            double externalCurrencyRate = (dn.isNormalCN())?dn.getJournalEntry().getExternalCurrencyRate():dn.getExchangeRateForOpeningTransaction();
            boolean isopeningBalanceCN = dn.isIsOpeningBalenceCN();
            if(dnamountdue > 0){
                KwlReturnObject bAmt = null;
                if (isopeningBalanceCN && dn.isConversionRateFromCurrencyToBase()) {// if CN is opening balance CN and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, dnamountdue, fromcurrencyid, currencyid, cnCreationDate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(requestParams, dnamountdue, fromcurrencyid, currencyid, cnCreationDate, externalCurrencyRate);
                }
                
                dnAmountOW += (Double) bAmt.getEntityList().get(0);
            }
        }
        
        ll.add(dnAmountOW);
        return ll;
    }

     public JSONArray getGoodsReceiptDetailsItemJSON(JSONObject requestObj, String invid, HashMap<String, Object> paramMap) {
        JSONArray jArr = new JSONArray();
        GoodsReceiptDetail row = null;
        ExpenseGRDetail exprow = null;
        PdfTemplateConfig config = null;
        double totaltax = 0, subtotal = 0, totalDiscount = 0;
        double totalAmount = 0;
        Date invoiceCreationDate = null;
        Date invoiceUpdationDate = null, transactionDate = null;
        String allTerms = "";
        StringBuilder appendtermString = new StringBuilder();
        String billAddr = "", shipAddr = "", createdby = "", vendortransactionalAddr = "", updatedby = "";
        double taxPercent = 0,amountdue = 0.0;
        double discountamount = 0;
        int quantitydigitafterdecimal = 2, amountdigitafterdecimal = 2, unitpricedigitafterdecimal = 2, count = 0;
        String currencyid = "", uom = "", vendorTitle = "";
        String mainTaxName = "", netinword = "", globallevelcustomfields = "", globalleveldimensions = "", gstAmountInWords = "";
        String GROref = "";
        String POref = "";
        String VQouteRef = "";
        Tax mainTax = null;
        JSONObject summaryData = new JSONObject();
        double totalQuantity = 0, totalOtherTermNonTaxable= 0.0;;
        double revExchangeRate = 0.0,totalExcise = 0.0,totaleducationCess=0.0,totalHCess=0.0;
        double globalLevelExchangedRateTotalAmount = 0, globalLevelExchangedRateSubTotal = 0, globalLevelExchangedRateTotalTax = 0,globalLevelExchangedRateSubTotalwithDiscount = 0,globalLevelExchangedRateTermAmount = 0, subTotalWithDiscount = 0;
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            String companyid = requestObj.optString(Constants.companyKey);
            KwlReturnObject idresult = null;
            boolean isexpenseinv = false;
            
            java.util.Date entryDate = null;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestObj);
            HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("goodsReceipt.ID");  //goodsreceipt is the database name
            order_by.add("srno");
            order_type.add("asc");
            invRequestParams.put("filter_names", filter_names);
            invRequestParams.put("filter_params", filter_params);
            invRequestParams.put("order_by", order_by);
            invRequestParams.put("order_type", order_type);
            KwlReturnObject result = kwlCommonTablesDAOObj.getObject(GoodsReceipt.class.getName(), invid);
            GoodsReceipt goodsReceipt = (GoodsReceipt) result.getEntityList().get(0);
            filter_params.clear();
            filter_params.add(goodsReceipt.getID());
            isexpenseinv = goodsReceipt.isIsExpenseType();
            if (isexpenseinv) {
               idresult = accGoodsReceiptobj.getExpenseGRDetails(invRequestParams);
            } else {
               idresult = accGoodsReceiptobj.getGoodsReceiptDetails(invRequestParams);
            }
       
            Iterator itr = idresult.getEntityList().iterator();
            int rowcnt = 0;
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, requestObj.optInt("moduleid"));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            currencyid = goodsReceipt.getCurrency().getCurrencyID();
            createdby = goodsReceipt.getCreatedby() != null ? goodsReceipt.getCreatedby().getFullName() : "";
            updatedby = goodsReceipt.getModifiedby() != null ? goodsReceipt.getModifiedby().getFullName() : "";
            //Document Currency
            summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, currencyid);
            /**
             * get customer title (Mr./Mrs.)
             */
            vendorTitle = goodsReceipt.getVendor().getTitle();
            if(!StringUtil.isNullOrEmpty(vendorTitle)){
                KwlReturnObject masterItemResult = kwlCommonTablesDAOObj.getObject(MasterItem.class.getName(), vendorTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                vendorTitle = masterItem.getValue();
            }
            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                 if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }
            KwlReturnObject resultcompany = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) resultcompany.getEntityList().get(0); 
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            /*
            * Declaration of Variables to fetch Line level taxes and its information like
            * Tax Code, Tax Amount etc.
            */        
            String allLineLevelTax="", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "", lineLevelTaxSign = "";
            Set<String> lineLevelTaxesGST = new HashSet<String>();
            Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
            Map<String,Double> lineLevelTaxNames = new LinkedHashMap<String,Double>();
            int countryid = 0;
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            /*
            * 
             * ExchangeRate values
             */
             double externalCurrencyRate = goodsReceipt.getExchangeRateDetail()!=null?goodsReceipt.getExchangeRateDetail().getExchangeRate():1;
            if (externalCurrencyRate != 0) {
                revExchangeRate = 1 / externalCurrencyRate;
            }
            
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
            while (itr.hasNext()) {//product row
                if (!isexpenseinv) {
                rowcnt++;
                row = (GoodsReceiptDetail) itr.next();
                JSONObject obj = new JSONObject();
                JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                Inventory inv = row.getInventory();
                Product prod = inv.getProduct();
                String proddesc="",discountname="",rowTaxName = "",basqtyuom="",BaseQtyWithUOM="";
                double rate = 0,rowTaxPercent=0,rowTaxAmt=0,rowamountwithtax=0,rowdiscountvalue=0,rowamountwithouttax=0,quantity = 0,baseqty=0; 
                double rowamountwithgst = 0;
                //product type
                obj.put("type", prod.getProducttype() == null ? "" : prod.getProducttype().getName());
                obj.put(CustomDesignerConstants.IN_ProductCode,prod.getProductid());
                
                Discount disc = row.getDiscount();
                if (disc != null) {
                    obj.put("prdiscount", disc.getDiscount());
                    obj.put("discountispercent", disc.isInPercent() ? 1 : 0);
                } else {
                    obj.put("prdiscount", 0);
                    obj.put("discountispercent", 1);
                }
//                entryDate = goodsReceipt.getJournalEntry().getEntryDate();
                entryDate = goodsReceipt.getCreationDate();
                   
                rate = row.getRate();
                quantity = (goodsReceipt.getPendingapproval() == 1 || goodsReceipt.getIstemplate() == 2) ? row.getInventory().getActquantity() : (row.getInventory().isInvrecord() ? row.getInventory().getQuantity() : row.getInventory().getQuantity());
                rowamountwithouttax = rate * quantity;
                
                /*
                 * In include GST case calculations are in reverse order
                 * In other cases calculations are in forward order
                 */
                if (row.getGoodsReceipt().isGstIncluded()) {//if gstincluded is the case
                    rowamountwithgst = row.getRateincludegst() * quantity;
                    rowdiscountvalue = (row.getDiscount() != null)? row.getDiscount().getDiscountValue() : 0;
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithgst - row.getRowTaxAmount(), companyid));//Subtotal
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.formattingDecimalForAmount((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()), companyid));//Subtotal
                    subtotal += authHandler.round(rowamountwithgst - row.getRowTaxAmount(), companyid);
                    subTotalWithDiscount = authHandler.round((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()), companyid);
                } else{
                    rowdiscountvalue = (row.getDiscount() != null)? row.getDiscount().getDiscountValue() : 0;
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithouttax, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.formattingDecimalForAmount((rowamountwithouttax-rowdiscountvalue), companyid));//Subtotal
                    subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                    subTotalWithDiscount = authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid);
                }
                obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                
                 /*Discount Section*/   
                Discount discount = row.getDiscount();
                if (discount != null) {
                    if (discount.isInPercent()) {
                        discountname = CustomDesignHandler.getAmountinCommaDecimal(row.getDiscount().getDiscount(), 0,countryid) + "%";//to return 0 no of zeros
                    } else {
                        discountname = goodsReceipt.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount().getDiscountValue(), companyid);//to show as it is in UI
                    }
                } else {
                    discountname = "0 %";
                }
                totalDiscount += authHandler.round(rowdiscountvalue,companyid);
                obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                obj.put(CustomDesignerConstants.Discountname, discountname);// Discount Name
                
                KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                    cateogry += categoryName + " ";
                } 
                if ( StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);
                HashMap<String, Object> params = new HashMap<String, Object>();
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("companyid");
                filter_names.add("fieldtype");
                filter_names.add("customcolumn");
                filter_names.add("moduleid");
                filter_params.add(companyid);
                filter_params.add(4);
                filter_params.add(1);
                filter_params.add(requestObj.optInt("moduleid"));
                params.put("filter_names", filter_names);
                params.put("filter_values", filter_params);
                
                KwlReturnObject fieldparams =  accAccountDAOobj.getFieldParams(params);
                List fieldParamsList = fieldparams.getEntityList();
                
                JSONArray jsonarr = new JSONArray();
                for(int cnt=0; cnt < fieldParamsList.size(); cnt++){
                    FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("comboName", fieldParamsObj.getFieldname());
                    jsonarr.put(tempObj);
                }
                
                obj.put("groupingComboList", jsonarr);
                
                /*Row tax section*/
                if (row != null && row.getTax() != null) {
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", row.getTax().getID());
                     requestParams.put("companyid", companyid);
                    KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                    List taxList = result1.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    rowTaxName = row.getTax().getName();
                    discountamount += discountamount * rowTaxPercent / 100;
                    rowTaxAmt=row.getRowTaxAmount();
//                    rowTaxAmt+=row.getRowTermTaxAmount();
                } 
                totaltax += authHandler.round(rowTaxAmt,companyid);//Calculate tax amount from line item
                obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);//Row TaxAmount
                obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);
                
                if (row.getGoodsReceipt().isGstIncluded()) {
                    rowamountwithtax = rowamountwithouttax; //Amount will be equal to rowamountwithouttax because tax gets added in gst
                } else {
                    if (rowdiscountvalue != 0) {
                        rowamountwithouttax -= rowdiscountvalue;//deducting discount if any
                    }
                    rowamountwithtax = rowamountwithouttax + rowTaxAmt;
                }
                
                String lineLevelTax = "";
                String lineLevelTaxPercent = "";
                String lineLevelTaxAmount = "";
                double lineLevelTaxAmountTotal = 0;
                if(extraCompanyPreferences.isIsNewGST()) { // 
                    HashMap<String, Object> GoodsReceiptDetailParams = new HashMap<String, Object>();
                    GoodsReceiptDetailParams.put("GoodsReceiptDetailid", row.getID());
                    GoodsReceiptDetailParams.put("orderbyadditionaltax", true);
                    // GST
                    GoodsReceiptDetailParams.put("termtype", 7);
                    KwlReturnObject grdTermMapresult = accGoodsReceiptobj.getGoodsReceiptdetailTermMap(GoodsReceiptDetailParams);
                    List<ReceiptDetailTermsMap> gst = grdTermMapresult.getEntityList();
                    obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                    obj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.TAXABLE_VALUE, subTotalWithDiscount);
                    for (ReceiptDetailTermsMap receiptdetailTermMap : gst) {
                        LineLevelTerms mt = receiptdetailTermMap.getTerm();
                        if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                            obj.put(CustomDesignerConstants.CGSTPERCENT, receiptdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, receiptdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, receiptdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                            obj.put(CustomDesignerConstants.IGSTPERCENT, receiptdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.IGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, receiptdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, receiptdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                            obj.put(CustomDesignerConstants.SGSTPERCENT, receiptdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.SGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, receiptdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, receiptdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                            obj.put(CustomDesignerConstants.UTGSTPERCENT, receiptdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.UTGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, receiptdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, receiptdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                            obj.put(CustomDesignerConstants.CESSPERCENT, receiptdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CESSAMOUNT, receiptdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, receiptdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, receiptdetailTermMap.getTermamount());
                        } 
                        lineLevelTax += mt.getTerm();
                        lineLevelTax += "!## ";
                        lineLevelTaxPercent += authHandler.formattingDecimalForAmount(receiptdetailTermMap.getPercentage(), companyid);
                        lineLevelTaxPercent += "!## ";
                        lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(receiptdetailTermMap.getTermamount(), amountdigitafterdecimal,countryid);
                        lineLevelTaxAmount += "!## ";
                        /*
                         * calculating total of line level taxes
                         */
                        lineLevelTaxAmountTotal += receiptdetailTermMap.getTermamount();
                        if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                            double value = lineLevelTaxNames.get(mt.getTerm());
                            lineLevelTaxNames.put(mt.getTerm(), receiptdetailTermMap.getTermamount() + value);
                        } else {
                            lineLevelTaxNames.put(mt.getTerm(), receiptdetailTermMap.getTermamount());
                        }
                        if (mt.isIsAdditionalTax()) {
                            String term = "";
                            if (mt.getTerm() != null) {
                                term = mt.getTerm().toLowerCase();
                            }
                            if (term.contains(IndiaComplianceConstants.EDUCATION_CESS) && !term.contains(IndiaComplianceConstants.HCESS) && !term.contains(IndiaComplianceConstants.HIGHER_EDUCATION_CESS)) {
                                totaleducationCess += receiptdetailTermMap.getTermamount();
                            }
                            if (term.contains(IndiaComplianceConstants.HCESS) || term.contains(IndiaComplianceConstants.HIGHER_EDUCATION_CESS)) {
                                totalHCess += receiptdetailTermMap.getTermamount();
                            }
                        }
                    }
                    
                    if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                        lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                        lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                        lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                    }
                    obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                    obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                    obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                    /*
                     * putting subtotal+tax
                     */
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));       
                    //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                    ExportRecordHandler.setHsnSacProductDimensionField(prod, obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, obj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                    gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                } else {
                    /*
                    * Fetching distinct taxes used at line level, feetched in the set
                    * Also, fetched the information related to tax in different maps
                    */
                    boolean isRowTaxApplicable = false;
                    double rowTaxPercentGST = 0.0;
                    double linelevelTaxAmount = 0.0; // Line level Tax + Term tax amount
                    if (row.getTax() != null) {
                        String taxCode = row.getTax().getTaxCode();
                        if (!lineLevelTaxesGST.contains(taxCode)) {
                            lineLevelTaxesGST.add(taxCode);
//                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceipt.getJournalEntry().getEntryDate(), row.getTax().getID());
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceipt.getCreationDate(), row.getTax().getID());
                            rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                            lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                            lineLevelTaxAmountGST.put(taxCode, 0.0);
                            lineLevelTaxBasicGST.put(taxCode, 0.0);
                        }
                        linelevelTaxAmount = row.getRowTaxAmount();
                        lineLevelTaxAmountGST.put(taxCode,(Double) lineLevelTaxAmountGST.get(taxCode) + linelevelTaxAmount);
                        lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL,0.0));
                        /*
                         * putting subtotal+tax
                         */
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(linelevelTaxAmount, companyid)); 
                    }
                }
                
                obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
                obj.put("gstCurrencyRate", goodsReceipt.getGstCurrencyRate() == 0 ? "" : goodsReceipt.getGstCurrencyRate());
                summaryData.put("gstCurrencyRate", goodsReceipt.getGstCurrencyRate() == 0 ? "" : goodsReceipt.getGstCurrencyRate());
                transactionDate = goodsReceipt.getJournalEntry().getEntryDate();
                obj.put("transactiondate", transactionDate); 
                summaryData.put("transactiondate", transactionDate); 
                
                double exchangerateunitprice = 0, exchangeratelineitemsubtotal = 0, exchangeratelineitemdiscount = 0, exchangeratelineitemamount = 0, exchangeratelineitemtax = 0, exchangeratelineitemsubtotalwithdiscount = 0; 
                  /*<--------------------Base Currency Unit Price,Base currency Subtotal ----------->*/
                /*Exchange Rate Section---- We have not given amount with tax because it is matched with UI*/
                if (externalCurrencyRate != 0) {
                    //Unit Price   
                    exchangerateunitprice = authHandler.round((rate * revExchangeRate), companyid);  //exchanged rate unit rate
                    //SUbTotal (rate*quantity)
                    exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                    //Exhanged Rate Discount
                    exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue * revExchangeRate), companyid);//exchange rate total discount
                    //Exhanged Rate Sub Total with Discount
                    if (row.getGoodsReceipt().isGstIncluded()) {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                    } else {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                    }
                    
                    //Exhanged Rate Tax Amount
                    exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round((lineLevelTaxAmountTotal * revExchangeRate), companyid) : authHandler.round((rowTaxAmt * revExchangeRate), companyid);
                    //Exhanged Rate Amount
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);
                    
                    //Total Exchanged Rate (RATE*Quantity)
                    globalLevelExchangedRateSubTotal = globalLevelExchangedRateSubTotal + exchangeratelineitemsubtotal;
                    //Overall Subtotal with Discount
                    globalLevelExchangedRateSubTotalwithDiscount = globalLevelExchangedRateSubTotalwithDiscount + exchangeratelineitemsubtotalwithdiscount;
                    //Overall Total Tax
                    globalLevelExchangedRateTotalTax = globalLevelExchangedRateTotalTax + exchangeratelineitemtax;

                } else {
                    //Unit Price   
                    exchangerateunitprice = authHandler.round(rate, companyid) ;  //exchanged rate unit rate
                    //SUbTotal (rate*quantity)
                    exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity),companyid);
                    //Exhanged Rate Discount
                    exchangeratelineitemdiscount = authHandler.round(rowdiscountvalue, companyid) ;//exchange rate total discount
                    //Exhanged Rate Sub Total with Discount
                    if (row.getGoodsReceipt().isGstIncluded()) {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                    } else {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                    }
                    
                    //Exhanged Rate Tax Amount
                    exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round(lineLevelTaxAmountTotal, companyid) : authHandler.round(rowTaxAmt, companyid);
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);
                    
                    //Total Exchanged Rate (RATE*Quantity)
                    globalLevelExchangedRateSubTotal = globalLevelExchangedRateSubTotal + exchangeratelineitemsubtotal;
                    //Overall Subtotal with Discount
                    globalLevelExchangedRateSubTotalwithDiscount = globalLevelExchangedRateSubTotalwithDiscount + exchangeratelineitemsubtotalwithdiscount;
                    //Overall Total Tax
                    globalLevelExchangedRateTotalTax = globalLevelExchangedRateTotalTax + exchangeratelineitemtax;
                }
                
                obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(rowamountwithtax, companyid)); // Amount
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                /*
                 * Following code is to check whether the image is predent for product or not. 
                 * If Image is not present sent s.gif instead of product id
                 */
                String fileName = null;
                fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+prod.getID() + ".png";
                File file = new File(fileName);
                FileInputStream in = null; 
                String filePathString = "";
                try {
                    in = new FileInputStream(file);
                } catch(java.io.FileNotFoundException ex) {
                    //catched exception if file not found, and to continue for rest of the products
                   filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                if(in != null){
                    filePathString = baseUrl + "productimage?fname=" + prod.getID() + ".png&isDocumentDesignerPrint=true";
                } else{
                    filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                obj.put(CustomDesignerConstants.imageTag,filePathString);
                /* Put frid values in Line Item Grid
                 */
                if (row.getGoodsReceiptOrderDetails() != null) {
                    obj.put(CustomDesignerConstants.Link_No, row.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber());
                } else if (row.getPurchaseorderdetail()!= null) {
                    obj.put(CustomDesignerConstants.Link_No, row.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber());
                } else if (row.getVendorQuotationDetail()!= null) {
                    obj.put(CustomDesignerConstants.Link_No, row.getVendorQuotationDetail().getVendorquotation().getQuotationNumber());
                } else {
                    obj.put(CustomDesignerConstants.Link_No, "");
                }
                obj.put("currencysymbol",goodsReceipt.getCurrency().getSymbol());
                obj.put("currencycode",goodsReceipt.getCurrency().getCurrencyCode());
                obj.put("isGstIncluded",goodsReceipt.isGstIncluded());//used for headercurrency & record currency
                
                /*
                 * to get the linkig information upto 2-3 levels (Mayur B).
                 */
                if (row.getGoodsReceiptOrderDetails() != null) {
                    if(GROref.indexOf(row.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber())==-1){
                    GROref += row.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber()+",";
                    }
                } else if (row.getPurchaseorderdetail() != null) {
                    if(POref.indexOf(row.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber())==-1){
                        POref += row.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber()+",";
                    }
                } else if (row.getVendorQuotationDetail() != null) {
                    if(VQouteRef.indexOf(row.getVendorQuotationDetail().getVendorquotation().getQuotationNumber())==-1)
                        VQouteRef += row.getVendorQuotationDetail().getVendorquotation().getQuotationNumber()+",";
                }
                
                

                //TDS Line Level Fields
                if (extraCompanyPreferences.isTDSapplicable()) {
                    obj.put(CustomDesignerConstants.TDS_RATE, row.getTdsRate());
                    obj.put(CustomDesignerConstants.TDS_AMOUNT, authHandler.round(row.getTdsLineAmount(), companyid));
                }

                /*
                 * get custom line data
                 */
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(row.getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresult = accGoodsReceiptobj.getGoodsReceiptCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, jeDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                   /*Product Level Custom Fields Evaluation*/
                    invDetailRequestParams.clear();
                    Detailfilter_names.add("productId");
                    Detailfilter_params.add(row.getInventory().getProduct().getID());
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresultForProduct = accInvoiceDAOobj.getInvoiceDetailsCustomDataForProduct(invDetailRequestParams);
                    AccJEDetailsProductCustomData accJEDetailsProductCustomData = null;
                    if (idcustresultForProduct.getEntityList().size() > 0) {
                        accJEDetailsProductCustomData = (AccJEDetailsProductCustomData) idcustresultForProduct.getEntityList().get(0);
                    }
                    replaceFieldMap = new HashMap<String, String>();
                    if (accJEDetailsProductCustomData != null) {
                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, accJEDetailsProductCustomData, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                    }
                    /*
                     * Set All Line level Dimension & All LIne level Custom
                     * Field Values
                     */
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                
                totalQuantity+=quantity;
                uom = row.getInventory().getUom() == null ? (row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getInventory().getUom().getNameEmptyforNA();
                
                 //Calculating base qty with UOM
                basqtyuom=prod.getUnitOfMeasure()==null?"":prod.getUnitOfMeasure().getNameEmptyforNA();
                double baseuomqty = row.getInventory().getBaseuomrate()*quantity;
                
                if(!basqtyuom.equals(uom)){
                    BaseQtyWithUOM =authHandler.formattingDecimalForQuantity(baseuomqty, companyid)+ " " + basqtyuom; 
                }else{
                  BaseQtyWithUOM=authHandler.formattingDecimalForQuantity(quantity, companyid)+ " " + uom;
                  baseuomqty=quantity;
                }

                obj.put(CustomDesignerConstants.BaseQty, authHandler.formattingDecimalForQuantity(baseuomqty, companyid));
                obj.put(CustomDesignerConstants.BaseQtyWithUOM, BaseQtyWithUOM);
                
                proddesc=StringUtil.isNullOrEmpty(row.getDescription()) ? (StringUtil.isNullOrEmpty(inv.getProduct().getDescription())?"":inv.getProduct().getDescription()) : row.getDescription();
                proddesc = StringUtil.DecodeText(proddesc);
                obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr No
                obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                obj.put(CustomDesignerConstants.ProductDescription,proddesc.replaceAll("\n", "<br>"));//Product Description
                obj.put(CustomDesignerConstants.HSCode, (prod.getHSCode()!=null) ? prod.getHSCode().replaceAll("\n", "<br>") : "" );//get HSCode of ProductGrid
                obj.put(CustomDesignerConstants.QuantitywithUOM,authHandler.formattingDecimalForQuantity(quantity, companyid)+ " " + uom); // Quantity
                obj.put(CustomDesignerConstants.IN_Currency, goodsReceipt.getCurrency().getCurrencyCode());
                obj.put(CustomDesignerConstants.IN_Quantity,authHandler.formattingDecimalForQuantity(quantity, companyid)); // Quantity
                obj.put(CustomDesignerConstants.IN_UOM, uom);
                obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(prod.getProductweight(), companyid));//Product Weight
                obj.put(CustomDesignerConstants.AdditionalDescription, prod.getAdditionalDesc() != null ? prod.getAdditionalDesc().replaceAll("\n", "<br>") :"");  //product Addtional description
                obj.put(CustomDesignerConstants.PartNumber, StringUtil.isNullOrEmpty(prod.getCoilcraft()) ? "" : prod.getCoilcraft()); //Part Number
                obj.put(CustomDesignerConstants.SupplierPartNumber, StringUtil.isNullOrEmpty(prod.getSupplier()) ? "" : prod.getSupplier());
                obj.put(CustomDesignerConstants.CustomerPartNumber, StringUtil.isNullOrEmpty(prod.getInterplant()) ? "" : prod.getInterplant());
                JSONObject jObj = new JSONObject();
                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                }
                if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                    obj = accProductObj.getProductDisplayUOM(prod, quantity,row.getInventory().getBaseuomrate(), true, obj);
                }
                //GST Exchange Rate
                if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !companyAccountPreferences.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                    obj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, row.getGstCurrencyRate());
                }
                jArr.put(obj);
            } else {//EXPENSE
                    exprow = (ExpenseGRDetail) itr.next();
                    rowcnt++;
                    JSONObject obj = new JSONObject();
                    String accountdesc = "", discountname = "";
                    double rate = 0,amount=0, rowTaxPercent = 0, rowTaxAmount = 0, rowamountwithtax = 0, rowdiscountvalue = 0, rowamountwithouttax = 0, quantity = 0;
                    rate = exprow.getRate();
                    Discount disc = exprow.getDiscount();
                    if (disc != null) {   //For Discount Row
                        //If discount is selected, then line level total ll be "Total-Discount"
                        rowdiscountvalue = exprow.getDiscount().getDiscountValue();
                        rate=rate-rowdiscountvalue;
                       
                        if (exprow.getDiscount().isInPercent()) {
                            discountname = CustomDesignHandler.getAmountinCommaDecimal(exprow.getDiscount().getDiscount(), 0,countryid) + "%";//to return 0 no of zeros
                        } else {
                            discountname = "0 %";
                        }
                        totalDiscount += rowdiscountvalue;
                        obj.put(CustomDesignerConstants.Discountname, discountname);// Discount Name
                        obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                    }
                    boolean isRowTaxApplicable = false;
                    String rowTaxName = "";
                    HashMap<String, Object> requestParams1 = AccountingManager.getGlobalParams(requestObj);
                    if (exprow != null && exprow.getTax() != null) {
//                        requestParams1.put("transactiondate", exprow.getGoodsReceipt().getJournalEntry().getEntryDate());
                        requestParams1.put("transactiondate", exprow.getGoodsReceipt().getCreationDate());
                        requestParams1.put("taxid", exprow.getTax().getID());
                        KwlReturnObject result1 = accTaxObj.getTax(requestParams1);
                        List taxList = result1.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        if (taxObj[1] != null) {
                            isRowTaxApplicable = true;
                        }
                        rowTaxName = exprow.getTax().getName();
                        if (exprow.isWasRowTaxFieldEditable()) { //After made row tax field editable tax calculation will be take place according to row tax amount. -From DATE - 28 -Jan-2014
                            if (isRowTaxApplicable) {
                                rowTaxAmount = exprow.getRowTaxAmount();
                            }
                        } else {// for earlier invoices it will be done according to row tax percent as selected in Tax combo column.
                            rowTaxAmount = rate * rowTaxPercent / 100;
                        }
//                        totaltax += rowTaxAmount;

                    }
                    if (!StringUtil.isNullOrEmpty(rowTaxName)) {   //For Tax Row
                        obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);
                    }
                                    
                    /*
                     * get custom line data
                     */
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                    Detailfilter_params.add(exprow.getID());
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accGoodsReceiptobj.getGoodsReceiptCustomData(invDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustColValuesForExport(requestObj, jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        for (Map.Entry<String, String> varEntry : replaceFieldMap.entrySet()) {//for dropdown,multidropdown,date,numeric field & text field
                            String coldata1 = "";
                            String valueForReport = "";
                            FieldComboData fieldComboData = null;
                            String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                            for (Map.Entry<String, Object> varEntry1 : variableMap.entrySet()) {
                                coldata1 = varEntry1.getValue() != null ? varEntry1.getValue().toString() : "";
                                if (coldata.endsWith(coldata1)) {
                                    if (FieldMap.containsKey(varEntry1.getKey())) {
                                        String[] valueData = coldata.split(",");
                                        for (String value : valueData) {
                                            fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), value);
                                            if (fieldComboData != null) {
                                                valueForReport += fieldComboData.getValue() + ",";
                                            }
                                        }
                                        if (valueForReport.length() > 1) {
                                            valueForReport = valueForReport.substring(0, valueForReport.length() - 1);
                                        }
                                        if (fieldComboData != null) {
                                            obj.put(varEntry.getKey(), valueForReport);
                                            obj.put(varEntry1.getKey(), valueForReport);
                                        } else {
                                            obj.put(varEntry.getKey(), varEntry.getValue() != null ? varEntry.getValue().toString() : ""); //Deepak If Custom field is not combofield.
                                            obj.put(varEntry1.getKey(), varEntry.getValue() != null ? varEntry.getValue().toString() : ""); //Deepak If Custom field is not combofield.
                                        }
                                    }
                                }
                            }
                        }
                    }
                    /*
                     * Set All Line level Dimension & All LIne level Custom
                     * Field Values
                     */
                    obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                    obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                    
                    boolean isGSTIncluded = goodsReceipt.isGstIncluded();
                    
                    if(isGSTIncluded){
                        amount = rate;
                    } else{
                        amount = rowTaxAmount + rate;
                    }
                    
                    if (exprow.isIsdebit()) {
                        totaltax += rowTaxAmount;//Calculate tax amount from line item
                        if (isGSTIncluded) {
                            subtotal += (rate - rowTaxAmount);
                        } else {
                            subtotal += rate;//discounted subtotal
                        }
                    } else {
                        totaltax -= rowTaxAmount;//Calculate tax amount from line item
                        if (isGSTIncluded) {
                            subtotal -= (rate - rowTaxAmount);
                        } else {
                            subtotal -= rate;//discounted subtotal
                        }
                    }
                    accountdesc = StringUtil.isNullOrEmpty(exprow.getDescription()) ? "" : exprow.getDescription();
                    accountdesc = StringUtil.DecodeText(accountdesc);
                    obj.put("currencysymbol", goodsReceipt.getCurrency().getSymbol());
                    obj.put("currencycode", goodsReceipt.getCurrency().getCurrencyCode());
                    obj.put("isGstIncluded", goodsReceipt.isGstIncluded());//used for headercurrency & record currency
                    obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr No
                    obj.put(CustomDesignerConstants.ProductName, exprow.getAccount().getName());
                    obj.put(CustomDesignerConstants.IN_ProductCode, exprow.getAccount().getAccountCode());
                    obj.put(CustomDesignerConstants.ProductDescription, accountdesc.replaceAll("\n", "<br>"));//Account Description
                    obj.put(CustomDesignerConstants.RATEINCLUDINGGST, authHandler.formattingDecimalForUnitPrice(exprow.getRate(), companyid));// Rate
                    if(isGSTIncluded){
                        obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(exprow.getRateExcludingGst(), companyid));// Rate
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.formattingDecimalForAmount((exprow.getRate() - rowdiscountvalue - rowTaxAmount), companyid));//Subtotal-disc
                    } else{
                        obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(exprow.getRate(), companyid));// Rate
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.formattingDecimalForAmount((exprow.getRate()-rowdiscountvalue), companyid));//Subtotal-disc
                    }
                    obj.put(CustomDesignerConstants.IN_Currency, goodsReceipt.getCurrency().getCurrencyCode());
                    obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmount);//Row TaxAmount
                    obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(amount, companyid)); // Amount
                    
                    //GST Exchange Rate
                    if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !companyAccountPreferences.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                        obj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, exprow.getGstCurrencyRate());
                    }
                    
                    jArr.put(obj);
                }
            }
            detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
            detailsTableData.put("isDetailsTableData", true);
            jArr.put(detailsTableData);
            //Get Amount Due for Balance Due Field
            if (goodsReceipt.isIsExpenseType()) {//For Expense Type
                List ll = getExpGRAmountDue(requestParams, goodsReceipt);
                amountdue = (Double) ll.get(1);
            } else {
                if (Constants.InvoiceAmountDueFlag) {
                    List ll = getInvoiceDiscountAmountInfo(requestParams, goodsReceipt);
                    amountdue = (Double) ll.get(1);
                } else {
                    List ll = getGRAmountDue(requestParams, goodsReceipt);
                    amountdue = (Double) ll.get(1);
                }
            }
            invoiceCreationDate = new Date(goodsReceipt.getCreatedon());
            invoiceUpdationDate = new Date(goodsReceipt.getUpdatedon());
            String invoiceCreationDateStr = invoiceCreationDate!= null?IndiaComplianceConstants.INDIAN_TEMPLATE_DATE_FORMATTER.format(invoiceCreationDate):"";
            String invoiceUpdationDateStr = invoiceUpdationDate!= null?IndiaComplianceConstants.INDIAN_TEMPLATE_DATE_FORMATTER.format(invoiceUpdationDate):"";

            //Auto generated true for GoodsReceiptOrder && if vendor invoice is linked to any GRO.
            KwlReturnObject InvoiceDo = accInvoiceDAOobj.getDOFromInvoices(invid, companyid, false);
            count = InvoiceDo.getRecordTotalCount();
            List InvoiceDoList = InvoiceDo.getEntityList();
            Iterator itrdo = InvoiceDoList.iterator();
            while (itrdo.hasNext()) {
                Object[] oj = (Object[]) itrdo.next();
                    GROref += (oj[0] != null ? oj[0].toString() : "0") + ", ";
            }
            
            //remove comma
            if (!StringUtil.isNullOrEmpty(GROref)) {//removing comma
                GROref = GROref.substring(0, GROref.length() - 2);
            } else if (!StringUtil.isNullOrEmpty(POref)) {
                POref = POref.substring(0, POref.length() - 1);
            } else if (!StringUtil.isNullOrEmpty(VQouteRef)) {
                VQouteRef = VQouteRef.substring(0, VQouteRef.length() - 1);
            }
            if (!StringUtil.isNullOrEmpty(invid)) {
                List receiptTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.receipttermsmap, invid);
                if(receiptTermMapList != null && !receiptTermMapList.isEmpty()){
                    Iterator termItr = receiptTermMapList.iterator();
                    while (termItr.hasNext()) {
                        Object[] obj = (Object[]) termItr.next();
                        /* 
                         * [0] : Sum of termamount  
                         * [1] : Sum of termamountinbase 
                         * [2] : Sum of termTaxamount 
                         * [3] : Sum of termTaxamountinbase 
                         * [4] : Sum of termamountexcludingtax 
                         * [5] : Sum of termamountexcludingtaxinbase
                         */                        
                        if(obj[2] != null){
                            totaltax += (Double) obj[2];
                        }
                    }
                }
            }
            mainTax = goodsReceipt.getTax();
            if (mainTax != null) { //Get Overall Tax percent && total tax amount
                requestParams.put("transactiondate", entryDate);
                requestParams.put("taxid", mainTax.getID());
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                mainTaxName = mainTax.getName();
                totaltax += goodsReceipt.getTaxEntry() != null ? goodsReceipt.getTaxEntry().getAmount() : 0;
                if (externalCurrencyRate != 0) {
                    globalLevelExchangedRateTotalTax = authHandler.round((totaltax * revExchangeRate), companyid);//exchanged rate total tax amount 
                }else {
                    globalLevelExchangedRateTotalTax = authHandler.round((totaltax), companyid);//exchanged rate total tax amount 
                }
                // Get global level tax details for GST Summary
                allLineLevelTax = mainTax.getTaxCode();
                allLineLevelTaxAmount =  String.valueOf(totaltax);
                allLineLevelTaxBasic = String.valueOf(subtotal);
            }
            if (goodsReceipt.getVendorEntry() != null) {
                totalAmount = goodsReceipt.getVendorEntry().getAmount();//Total amount of transaction
            }
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId);
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(totalAmount)), indoCurrency);
            }
            mainTaxName = goodsReceipt.getTax() != null ? goodsReceipt.getTax().getName() : "";

            /*Below Summary Terms Calculation*/
            String term = "", termsNameWithoutPercent= "",termsamount = "",termssign="",termsName="";
            double totalTermAmount = 0;  
            if (!StringUtil.isNullOrEmpty(goodsReceipt.getID())) {
                HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("invoiceid", goodsReceipt.getID());
            KwlReturnObject curresult = null;
            curresult = accGoodsReceiptobj.getInvoiceTermMap(requestParam);
            List<ReceiptTermsMap> termMap = curresult.getEntityList();
            for (ReceiptTermsMap receiptTermsMap : termMap) {
                    InvoiceTermsSales mt = receiptTermsMap.getTerm();
                    double termAmnt = 0;
                    if(goodsReceipt.isGstIncluded()){
                        termAmnt = receiptTermsMap.getTermAmountExcludingTax();
                    }else{
                        termAmnt = receiptTermsMap.getTermamount();
                    }
                    String termName = (mt.getTerm() + (termAmnt > 0 ? "(+)" : "(-)"));
                    summaryData.put(mt.getTerm(),termAmnt);
                    summaryData.put(CustomDesignerConstants.BaseCurrency+mt.getTerm(),(termAmnt*revExchangeRate));
                    totalTermAmount += termAmnt;
                    double exchangeratetermamount = 0;
                    if (externalCurrencyRate != 0) {
                        exchangeratetermamount = (termAmnt * revExchangeRate);
                    } else {
                        exchangeratetermamount = termAmnt;
                    }
                    globalLevelExchangedRateTermAmount += exchangeratetermamount;
                    double tempTermValue = (termAmnt > 0 ? termAmnt : (termAmnt * -1));
                    term += "<div> <table><tr><td>" + termName + " : </td><td>" + Double.toString(tempTermValue) + "</td></tr></table></div><br>";
                    termsNameWithoutPercent += mt.getTerm() +"!## ";
                    if(termAmnt > 0){
                        termssign +="+!## ";
                    } else{
                        termssign +="-&nbsp;!## ";
                    }
                    termsName += mt.getTerm() + " " + receiptTermsMap.getPercentage() + "%, ";
                    termsamount += CustomDesignHandler.getAmountinCommaDecimal(Math.abs(termAmnt), amountdigitafterdecimal,countryid) +"!## ";
                    if (!StringUtil.isNullOrEmpty(String.valueOf(termAmnt))) {
                        String allTermsPlaceholder = CustomDesignerConstants.AllTermsKeyValuePair;
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsLabel, termName);
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsValue, CustomDesignHandler.getAmountinCommaDecimal(termAmnt, amountdigitafterdecimal, countryid));
                        appendtermString.append(allTermsPlaceholder);
                    }
                }
                if (!StringUtil.isNullOrEmpty(termsName)) {
                    termsName = termsName.substring(0, termsName.length() - 2);
                }
                if (!StringUtil.isNullOrEmpty(term) && term.indexOf("<br>") != -1) {
                    term = term.substring(0, term.lastIndexOf("<br>"));
                }
                if(!StringUtil.isNullOrEmpty(appendtermString.toString())){
                    allTerms=appendtermString.toString();
                }
                if (!StringUtil.isNullOrEmpty(termsNameWithoutPercent)) {
                    termsNameWithoutPercent = termsNameWithoutPercent.substring(0, termsNameWithoutPercent.length() - 4);
                }
                if (!StringUtil.isNullOrEmpty(termssign)) {
                    termssign = termssign.substring(0, termssign.length() - 4);
                }
                if (!StringUtil.isNullOrEmpty(termsamount)) {
                    termsamount = termsamount.substring(0, termsamount.length() - 4);
                }
            }
            globalLevelExchangedRateTermAmount = authHandler.round(globalLevelExchangedRateTermAmount, companyid);
            /* Base Currency Total Amount*/
            globalLevelExchangedRateTotalAmount = globalLevelExchangedRateSubTotalwithDiscount + globalLevelExchangedRateTotalTax + globalLevelExchangedRateTermAmount;
                
            DateFormat df = authHandler.getUserDateFormatterJson(requestObj);//User Date Formatter
            Date date = new Date();
            String printedOn = df.format(date);
            
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("vendorid", goodsReceipt.getVendor().getID());
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData,companyAccountPreferences, extraCompanyPreferences);
            billAddr = CommonFunctions.getTotalBillingShippingAddress(goodsReceipt.getBillingShippingAddresses(), true);
            shipAddr=CommonFunctions.getTotalBillingShippingAddress(goodsReceipt.getBillingShippingAddresses(), false);
            vendortransactionalAddr=CommonFunctions.getTotalVendorTransactionalShippingAddress(goodsReceipt.getBillingShippingAddresses(), true); 
            /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", goodsReceipt.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            double TotalLineLevelTaxAmount = 0;
            if(extraCompanyPreferences.isIsNewGST()) { // For new Gst flow 
                    if (!lineLevelTaxNames.isEmpty()) {
                        Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                        while (lineTax.hasNext()) {
                            Map.Entry tax = (Map.Entry) lineTax.next();
                            allLineLevelTax += tax.getKey();
                            allLineLevelTax += "!## ";
                            double tax_amount = (double) tax.getValue();
                            allLineLevelTaxAmount += tax.getValue().toString();
                            TotalLineLevelTaxAmount += tax_amount;
                            allLineLevelTaxAmount += "!## ";
                            if (tax_amount > 0) {
                                lineLevelTaxSign += "+";
                                lineLevelTaxSign += "!## ";
                            } else {
                                lineLevelTaxSign += "-&nbsp;";
                                lineLevelTaxSign += "!## ";
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTax)) {
                        allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)) {
                        allLineLevelTaxAmount = allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxSign)) {
                        lineLevelTaxSign = lineLevelTaxSign.substring(0, lineLevelTaxSign.length() - 4);
                    }
                    totaltax = TotalLineLevelTaxAmount;
            } else {
                /*
                * Putting all line taxes and its information in summary data separated by !##
                */
                for ( String key : lineLevelTaxesGST ) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount +=  lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            gstAmountInWords = EnglishNumberToWordsOjb.convert(totaltax, currency,countryLanguageId);
            extraparams.put("approvestatuslevel", goodsReceipt.getApprovestatuslevel());
            String userId = requestObj.optString(Constants.useridKey);
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            //Details like company details
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
            
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(authHandler.round(totalAmount,companyid), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, !isexpenseinv ? authHandler.formattedAmount((subtotal-totalDiscount), companyid) : authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount(((subtotal - totalDiscount) + totaltax), companyid)); //ERP-25162
            summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, authHandler.formattedAmount(totalDiscount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltax, companyid));
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, taxPercent);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword+" Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsDescription_fieldTypeId,term);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, termsName);
            summaryData.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId,totalTermAmount);
            summaryData.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId,VQouteRef);
            summaryData.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId,POref);
            summaryData.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId,GROref);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term,goodsReceipt.getVendor().getDebitTerm()!=null?(goodsReceipt.getVendor().getDebitTerm().getTermname()!=null?goodsReceipt.getVendor().getDebitTerm().getTermname():""):"");
            summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode,goodsReceipt.getVendor().getAccount()!=null?(goodsReceipt.getVendor().getAccount().getAcccode()!=null?goodsReceipt.getVendor().getAccount().getAcccode():""):"");
            summaryData.put(CustomDesignerConstants.CustomerVendor_Code,goodsReceipt.getVendor().getAcccode()!=null?goodsReceipt.getVendor().getAcccode():"");
            summaryData.put(CustomDesignerConstants.BillTo,billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.ShipTo,shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.Createdby,createdby);
            summaryData.put(CustomDesignerConstants.Updatedby,updatedby);
            
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTotalAmount, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateSubTotal, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTotalTax, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateSubTotalwithDiscount, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTermAmount, companyid));
            
            //GST Exchange Rate
            if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !companyAccountPreferences.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, goodsReceipt.getGstCurrencyRate() != 0.0 ? goodsReceipt.getGstCurrencyRate() : "");
            } else {
                summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
            }
            
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, 1);
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, authHandler.formattedAmount(globalLevelExchangedRateTotalAmount, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, authHandler.formattedAmount(globalLevelExchangedRateSubTotal, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, authHandler.formattedAmount(globalLevelExchangedRateTotalTax, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, authHandler.formattedAmount(globalLevelExchangedRateSubTotalwithDiscount, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, authHandler.formattedAmount(globalLevelExchangedRateTermAmount, companyid));
            
            summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);
            summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorCompanyEmail3_fieldTypeId, goodsReceipt.getCompany() != null ? (goodsReceipt.getCompany().getEmailID() != null ? goodsReceipt.getCompany().getEmailID() : "") : "");
            summaryData.put(CustomDesignerConstants.CustomDesignTemplate_Print, printedOn);
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term,goodsReceipt.getVendor().getDebitTerm()!=null?(goodsReceipt.getVendor().getDebitTerm().getTermname()!=null?goodsReceipt.getVendor().getDebitTerm().getTermname():""):"");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPreText_fieldTypeId, config == null ? "" : config.getPdfPreText());
            summaryData.put(CustomDesignerConstants.VendorTransactionalShipTo, vendortransactionalAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.AllTerms, allTerms);
            summaryData.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, authHandler.formattedAmount(((subtotal-totalDiscount) + totalTermAmount), companyid));
            summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName())?user.getFirstName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName())?user.getLastName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName())?user.getFullName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID())?user.getEmailID():"");
            summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress())?user.getAddress().replaceAll(Constants.REGEX_LINE_BREAK, "<br>"):"");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber())?user.getContactNumber():"");
            summaryData.put(CustomDesignerConstants.InvoiceAmountDue, authHandler.formattedAmount(amountdue, companyid));
            summaryData.put(CustomDesignerConstants.VENDOR_TITLE, vendorTitle);
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTermSigns, lineLevelTaxSign);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);
            summaryData.put(CustomDesignerConstants.INVOICE_CREATION_DATE, invoiceCreationDateStr);
            summaryData.put(CustomDesignerConstants.INVOICE_UPDATION_DATE, invoiceUpdationDateStr);
            summaryData.put(CustomDesignerConstants.AllTermNames, termsNameWithoutPercent);
            summaryData.put(CustomDesignerConstants.AllTermSigns, termssign);
            summaryData.put(CustomDesignerConstants.AllTermAmounts, termsamount);
            summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords + Constants.ONLY);
            if(countryid == Constants.indian_country_id){
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, goodsReceipt.getVendor().getVATTINnumber()!= null ? goodsReceipt.getVendor().getVATTINnumber() : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, goodsReceipt.getVendor().getCSTTINnumber()!= null ? goodsReceipt.getVendor().getCSTTINnumber() : "");
                summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
                summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
                summaryData.put(CustomDesignerConstants.RANGE_CODE_COMPANY, !StringUtil.isNullOrEmpty(extraCompanyPreferences.getExciseRangeCode()) ? extraCompanyPreferences.getExciseRangeCode() : "");
                summaryData.put(CustomDesignerConstants.DIVISION_CODE_COMPANY, !StringUtil.isNullOrEmpty(extraCompanyPreferences.getExciseDivisionCode())? extraCompanyPreferences.getExciseDivisionCode(): "");
                summaryData.put(CustomDesignerConstants.COMMISSIONERATE_CODE_COMPANY, !StringUtil.isNullOrEmpty(extraCompanyPreferences.getExciseCommissionerateCode()) ? extraCompanyPreferences.getExciseCommissionerateCode() : "");
                summaryData.put(CustomDesignerConstants.EXCISE_IN_WORDS, EnglishNumberToWordsOjb.convert(totalExcise, currency,countryLanguageId));
                summaryData.put(CustomDesignerConstants.EDUCATION_CESS_IN_WORDS, EnglishNumberToWordsOjb.convert(totaleducationCess, currency,countryLanguageId));
                summaryData.put(CustomDesignerConstants.H_CESS_IN_WORDS, EnglishNumberToWordsOjb.convert(totalHCess, currency,countryLanguageId));
                summaryData.put(CustomDesignerConstants.OTHER_CHARGES, totalOtherTermNonTaxable);
                summaryData.put(CustomDesignerConstants.TDS_AMOUNT, goodsReceipt.getTdsAmount());
                summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, goodsReceipt.getVendor().getGSTIN() != null ? goodsReceipt.getVendor().getGSTIN() : "");
            }
            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE,null,ex);
        }
        return jArr;
    }

    private HashMap getGoodsReceiptProductAmount1(GoodsReceipt goodsreceipt) throws ServiceException {
        HashMap hm = new HashMap();
        Set invRows = goodsreceipt.getRows();
        Iterator itr = invRows.iterator();
        double amount;
        double quantity;
        while (itr.hasNext()) {
            GoodsReceiptDetail temp = (GoodsReceiptDetail) itr.next();
            //For the case of update inventory from DO

            double quantityTemp = (goodsreceipt.getPendingapproval() == 1) ? temp.getInventory().getActquantity() : (temp.getInventory().isInvrecord() ? temp.getInventory().getQuantity() : temp.getInventory().getActquantity());
            quantity = quantityTemp;
            amount = authHandler.round(temp.getRate() * quantity, goodsreceipt.getCompany().getCompanyID());
//            if (temp.getPartamount() != 0) {
//                amount = amount * (temp.getPartamount() / 100);
//            }
            double rdisc = (temp.getDiscount() == null ? 0 : temp.getDiscount().getDiscountValue());
            double rowTaxPercent = 0;
            if (temp.getTax() != null) {

//                KwlReturnObject perresult = accTaxObj.getTaxPercent(goodsreceipt.getCompany().getCompanyID(), goodsreceipt.getJournalEntry().getEntryDate(), temp.getTax().getID());
                KwlReturnObject perresult = accTaxObj.getTaxPercent(goodsreceipt.getCompany().getCompanyID(), goodsreceipt.getCreationDate(), temp.getTax().getID());
                rowTaxPercent = (Double) perresult.getEntityList().get(0);
            }
            double ramount = amount - rdisc;
            ramount += ramount * rowTaxPercent / 100;


            hm.put(temp, new Object[]{ramount, quantity});

            if (goodsreceipt == null) {
                goodsreceipt = temp.getGoodsReceipt();
            }
        }
        return hm;
    }
    //Vendor Quotation
    public JSONArray getVendorQuotationDetailsItemJSON(JSONObject requestObj, String invid, HashMap<String, Object> paramMap) {
        
        JSONArray jArr = new JSONArray();
        VendorQuotation venquotation = null;
        java.util.Date entryDate = null, transactionDate = null;
        double totaltax = 0,totalAmount = 0,subtotal=0,totalDiscount=0;
        double totalQuantity = 0;
        double revExchangeRate = 0.0;
        double globalLevelExchangedRateTotalAmount = 0, globalLevelExchangedRateSubTotal = 0, globalLevelExchangedRateTotalTax = 0, globalLevelExchangedRateSubTotalwithDiscount = 0,globalLevelExchangedRateTermAmount = 0, subTotalWithDiscount = 0;
        JSONObject summaryData = new JSONObject();  
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            String companyid = requestObj.optString(Constants.companyKey);
            Tax mainTax = null;
            PdfTemplateConfig config=null; 
            String currencyid = "", vendorTitle = "";
            String allTerms="";
            StringBuilder appendtermString = new StringBuilder();
            String  shipAddr = "",billAddrress = "", billAddr = "", createdby = "", vendortransactionalAddr = "", updatedby = "";
            String mainTaxName = "",globallevelcustomfields="",globalleveldimensions="";
            double rowTaxPercent = 0,taxPercent = 0;
            boolean isgstincluded=false; //flag to check includedgst
            int quantitydigitafterdecimal=2,amountdigitafterdecimal=2,unitpricedigitafterdecimal=2;

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestObj);
            HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            venquotation = (VendorQuotation) kwlCommonTablesDAOObj.getClassObject(VendorQuotation.class.getName(), invid);
            currencyid = (venquotation.getCurrency() == null) ? currencyid : venquotation.getCurrency().getCurrencyID();
            // Get Company Post Text
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, requestObj.optInt("moduleid"));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            //Document Currency
            summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, currencyid);
            createdby = venquotation.getCreatedby() != null ? venquotation.getCreatedby().getFullName() : "";
            updatedby = venquotation.getModifiedby() != null ? venquotation.getModifiedby().getFullName() : "";
            billAddr = venquotation.getVendor() != null ? venquotation.getVendor().getAddress() : "";
            /**
             * get customer title (Mr./Mrs.)
             */
            vendorTitle = venquotation.getVendor().getTitle();
            if(!StringUtil.isNullOrEmpty(vendorTitle)){
                KwlReturnObject masterItemResult = kwlCommonTablesDAOObj.getObject(MasterItem.class.getName(), vendorTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                vendorTitle = masterItem.getValue();
            }
             Set<VendorQuotationDetail> list= venquotation.getRows();

            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }
            
            KwlReturnObject resultcompany = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) resultcompany.getEntityList().get(0); 
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            /*
            * Declaration of Variables to fetch Line level taxes and its information like
            * Tax Code, Tax Amount etc.
            */ 
            String allLineLevelTax="", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "", lineLevelTaxSign = "";
            Set<String> lineLevelTaxesGST = new HashSet<String>();
            Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
            double TotalLineLevelTaxAmount = 0;
            Map<String,Double> lineLevelTaxNames = new LinkedHashMap<String,Double>();
            int countryid = 0;
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            /*
             * ExchangeRate values
             */
            double externalCurrencyRate = venquotation.getExternalCurrencyRate();

            if (externalCurrencyRate != 0) {
                revExchangeRate = 1 / externalCurrencyRate;
            }
            int rowcnt = 0;
            Set<String> uniqueProductTaxList = new HashSet<String>();
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
           for (VendorQuotationDetail row:list) {//product row
                JSONObject obj = new JSONObject();
                JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                String prodDesc = "",uom = "",prodName = "",rowTaxName = "",discountname="";
                double rowamountwithouttax=0,rowamountwithtax = 0,rowTaxAmt=0,quantity = 0,rate = 0,rowdiscountvalue=0;
                double rowamountwithgst = 0;
                rowcnt++;
                
                if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                    prodDesc = row.getDescription();
                } else {
                    if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                        prodDesc = row.getProduct().getDescription();
                    }
                }
                prodDesc = StringUtil.DecodeText(prodDesc);
                prodName = row.getProduct().getName();
                uom = row.getUom() == null ? "" : row.getUom().getNameEmptyforNA(); //ERP-25533
                quantity = row.getQuantity();
                
                if (row.getVendorquotation().isGstIncluded()) {//if gstincluded is the case.
                    isgstincluded = true;
                }
                rate = row.getRate();
                
                rowamountwithouttax =rate * quantity;
                
                /*
                 * In include GST case calculations are in reverse order
                 * In other cases calculations are in forward order
                 */
                if (row.getVendorquotation().isGstIncluded()) {//if gstincluded is the case
                    rowamountwithgst = row.getRateincludegst() * quantity;
                    rowdiscountvalue = (row.getDiscountispercent() == 1)? rowamountwithgst * row.getDiscount()/100 : row.getDiscount();
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithgst - row.getRowTaxAmount(), companyid));//Subtotal
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.formattingDecimalForAmount((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()), companyid));//Subtotal
                    subtotal += authHandler.round(rowamountwithgst - row.getRowTaxAmount(), companyid);
                    subTotalWithDiscount = authHandler.round((rowamountwithgst - rowdiscountvalue - row.getRowTaxAmount()), companyid);
                } else{
                    rowdiscountvalue = (row.getDiscountispercent() == 1)? rowamountwithouttax*row.getDiscount()/100 : row.getDiscount();
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithouttax, companyid));//Subtotal
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount,authHandler.formattingDecimalForAmount((rowamountwithouttax-rowdiscountvalue), companyid));//Subtotal
                    subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                    subTotalWithDiscount = authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid);
                }
                
                KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(row.getProduct().getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                    cateogry += categoryName + " ";
                } 
                if ( StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);
                HashMap<String, Object> params = new HashMap<String, Object>();
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("companyid");
                filter_names.add("fieldtype");
                filter_names.add("customcolumn");
                filter_names.add("moduleid");
                filter_params.add(companyid);
                filter_params.add(4);
                filter_params.add(1);
                filter_params.add(requestObj.optInt("moduleid"));
                params.put("filter_names", filter_names);
                params.put("filter_values", filter_params);
                
                KwlReturnObject fieldparams =  accAccountDAOobj.getFieldParams(params);
                List fieldParamsList = fieldparams.getEntityList();
                
                JSONArray jsonarr = new JSONArray();
                for(int cnt=0; cnt < fieldParamsList.size(); cnt++){
                    FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("comboName", fieldParamsObj.getFieldname());
                    jsonarr.put(tempObj);
                }
                
                obj.put("groupingComboList", jsonarr);
                
                /*Discount Section*/
                totalDiscount +=authHandler.round(rowdiscountvalue,companyid);
               if (row.getDiscountispercent() == 1) {
                   double discountpercent = row.getDiscount();
                   discountname = (long) discountpercent == discountpercent ? (long) discountpercent + "%" : discountpercent + "%"; // ERP-27882
               } else {
                   discountname = venquotation.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount(), companyid);//to show as it is in UI
               }
                
                obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                obj.put(CustomDesignerConstants.Discountname, discountname);// Discount
                 
                /*Row Tax Section*/
                if (row != null && row.getTax() != null) {
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", row.getTax().getID());
                    uniqueProductTaxList.add(row.getTax().getID());
                    KwlReturnObject result = accTaxObj.getTax(requestParams);
                    List taxList = result.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    rowTaxName = row.getTax().getName();
                    rowTaxAmt=row.getRowTaxAmount();
                } 
                totaltax +=authHandler.round(rowTaxAmt,companyid);
                obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);   //Tax percent
                obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);//Tax Name
                
                if (row.getVendorquotation().isGstIncluded()) {
                    rowamountwithtax = rowamountwithouttax; //Amount will be equal to rowamountwithouttax because tax gets added in gst
                } else {
                    if (rowdiscountvalue != 0) {
                        rowamountwithouttax -= rowdiscountvalue;//deducting discount if any
                    }
                    rowamountwithtax = rowamountwithouttax + rowTaxAmt;
                }
                
                String lineLevelTax = "";
                String lineLevelTaxPercent = "";
                String lineLevelTaxAmount = "";
                double lineLevelTaxAmountTotal = 0;
                /*
                 * Check For new Gst flow 
                 */
                if(extraCompanyPreferences.isIsNewGST()) { // 
                    HashMap<String, Object> VendorQuotationDetailParams = new HashMap<String, Object>();
                    VendorQuotationDetailParams.put("vendorquotationdetails", row.getID());
                    VendorQuotationDetailParams.put("orderbyadditionaltax", true);
                    // GST
                    VendorQuotationDetailParams.put("termtype", 7);
                    KwlReturnObject grdTermMapresult = accPurchaseOrderobj.getVendorQuotationProductTermDetails(VendorQuotationDetailParams);
                    List<VendorQuotationDetailsTermMap> gst = grdTermMapresult.getEntityList();
                    obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                    obj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.TAXABLE_VALUE, subTotalWithDiscount);
                    
                    
                    for (VendorQuotationDetailsTermMap vendorquotationdetailTermMap : gst) {
                        LineLevelTerms mt = vendorquotationdetailTermMap.getTerm();
                        if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                            obj.put(CustomDesignerConstants.CGSTPERCENT, vendorquotationdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CGSTAMOUNT, vendorquotationdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, vendorquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, vendorquotationdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                            obj.put(CustomDesignerConstants.IGSTPERCENT, vendorquotationdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.IGSTAMOUNT,vendorquotationdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, vendorquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT,vendorquotationdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                            obj.put(CustomDesignerConstants.SGSTPERCENT, vendorquotationdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.SGSTAMOUNT, vendorquotationdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, vendorquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, vendorquotationdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                            obj.put(CustomDesignerConstants.UTGSTPERCENT, vendorquotationdetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.UTGSTAMOUNT, vendorquotationdetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, vendorquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, vendorquotationdetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                            obj.put(CustomDesignerConstants.CESSPERCENT, vendorquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, vendorquotationdetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, vendorquotationdetailTermMap.getTermamount());
                            obj.put(CustomDesignerConstants.CESSAMOUNT, vendorquotationdetailTermMap.getTermamount());
                        }
                        lineLevelTax += mt.getTerm();
                        lineLevelTax += "!## ";
                        lineLevelTaxPercent += authHandler.formattingDecimalForAmount(vendorquotationdetailTermMap.getPercentage(), companyid);
                        lineLevelTaxPercent += "!## ";
                        lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(vendorquotationdetailTermMap.getTermamount(), amountdigitafterdecimal,countryid);
                        lineLevelTaxAmount += "!## ";
                        /*
                         * calculating total of line level taxes
                         */
                        lineLevelTaxAmountTotal += vendorquotationdetailTermMap.getTermamount();
                        if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                            double value = lineLevelTaxNames.get(mt.getTerm());
                            lineLevelTaxNames.put(mt.getTerm(), vendorquotationdetailTermMap.getTermamount() + value);
                        } else {
                            lineLevelTaxNames.put(mt.getTerm(), vendorquotationdetailTermMap.getTermamount());
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                        lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                        lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                        lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                    }
                    obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                    obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                    obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                    /*
                     * putting subtotal+tax
                     */
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));       
                    //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                    ExportRecordHandler.setHsnSacProductDimensionField(row.getProduct(), obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, obj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                    gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                } else {
                    /*
                    * Fetching distinct taxes used at line level, feetched in the set
                    * Also, fetched the information related to tax in different maps
                    */
                    boolean isRowTaxApplicable = false;
                    double rowTaxPercentGST = 0.0;
                    if (row.getTax() != null) {
                        String taxCode = row.getTax().getTaxCode();
                        if (!lineLevelTaxesGST.contains(taxCode)) {
                            lineLevelTaxesGST.add(taxCode);
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, venquotation.getQuotationDate(), row.getTax().getID());
                            rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                            lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                            lineLevelTaxAmountGST.put(taxCode, 0.0);
                            lineLevelTaxBasicGST.put(taxCode, 0.0);
                        }
                        lineLevelTaxAmountGST.put(taxCode,(Double) lineLevelTaxAmountGST.get(taxCode) + row.getRowTaxAmount());
                        lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL,0.0));
                        /*
                         * putting subtotal+tax
                         */
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(row.getRowTaxAmount(), companyid)); 
                    }
                }
                
               obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
               obj.put("gstCurrencyRate", "");
               summaryData.put("gstCurrencyRate", "");
               transactionDate = venquotation.getQuotationDate();
               obj.put("transactiondate", transactionDate); // Amount
               summaryData.put("transactiondate", transactionDate); // Amount
                
                  double exchangerateunitprice = 0, exchangeratelineitemsubtotal = 0, exchangeratelineitemdiscount = 0, exchangeratelineitemamount = 0, exchangeratelineitemtax = 0, exchangeratelineitemsubtotalwithdiscount = 0; 
                  /*<--------------------Base Currency Unit Price,Base currency Subtotal ----------->*/
                /*Exchange Rate Section---- We have not given amount with tax because it is matched with UI*/
                if (externalCurrencyRate != 0) {
                    //Unit Price   
                    exchangerateunitprice = authHandler.round((rate * revExchangeRate), companyid);  //exchanged rate unit rate
                    //SUbTotal (rate*quantity)
                    exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                    //Exhanged Rate Discount
                    exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue * revExchangeRate), companyid);//exchange rate total discount
                    if (row.getVendorquotation().isGstIncluded()) {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                    } else {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                    }
                    
                    //Exhanged Rate Tax Amount
                    exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round((lineLevelTaxAmountTotal * revExchangeRate), companyid) : authHandler.round((rowTaxAmt * revExchangeRate), companyid);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                    //Total Exchanged Rate (RATE*Quantity)
                    globalLevelExchangedRateSubTotal = globalLevelExchangedRateSubTotal + exchangeratelineitemsubtotal;
                    //Overall Subtotal with Discount
                    globalLevelExchangedRateSubTotalwithDiscount = globalLevelExchangedRateSubTotalwithDiscount + exchangeratelineitemsubtotalwithdiscount;
                    //Overall Total Tax
                    globalLevelExchangedRateTotalTax = globalLevelExchangedRateTotalTax + exchangeratelineitemtax;

                } else {
                    //Unit Price   
                    exchangerateunitprice = authHandler.round(rate, companyid);  //exchanged rate unit rate
                    //SUbTotal (rate*quantity)
                    exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                    //Exhanged Rate Discount
                    exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue), companyid);//exchange rate total discount
                    if (row.getVendorquotation().isGstIncluded()) {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                    } else {
                        exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                        exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                    }
                    
                    //Exhanged Rate Tax Amount
                    exchangeratelineitemtax = extraCompanyPreferences.isIsNewGST() ? authHandler.round(lineLevelTaxAmountTotal, companyid) : authHandler.round(rowTaxAmt, companyid);
                    
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);
                    
                    obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                    obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                    obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                    obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                    //Total Exchanged Rate (RATE*Quantity)
                    globalLevelExchangedRateSubTotal = globalLevelExchangedRateSubTotal + exchangeratelineitemsubtotal;
                    //Overall Subtotal with Discount
                    globalLevelExchangedRateSubTotalwithDiscount = globalLevelExchangedRateSubTotalwithDiscount + exchangeratelineitemsubtotalwithdiscount;
                    //Overall Total Tax
                    globalLevelExchangedRateTotalTax = globalLevelExchangedRateTotalTax + exchangeratelineitemtax;
                }
                
                totalQuantity+=quantity;
                obj.put(CustomDesignerConstants.Amount,authHandler.formattingDecimalForAmount(rowamountwithtax, companyid)); // Amount of single product
                
                obj.put(CustomDesignerConstants.SrNO, rowcnt);  // Sr No
                obj.put(CustomDesignerConstants.ProductName, prodName.replaceAll("\n", "<br>"));  //product name
                obj.put(CustomDesignerConstants.ProductDescription, prodDesc.replaceAll("\n", "<br>"));  //product description
                obj.put(CustomDesignerConstants.HSCode, (row.getProduct().getHSCode()!=null) ? row.getProduct().getHSCode().replaceAll("\n", "<br>") : "" );//get HSCode of ProductGrid
                obj.put(CustomDesignerConstants.Rate,authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                obj.put(CustomDesignerConstants.QuantitywithUOM,authHandler.formattingDecimalForQuantity(quantity, companyid) +" "+ uom);//Quantity
                obj.put(CustomDesignerConstants.IN_Quantity,authHandler.formattingDecimalForQuantity(quantity, companyid));  //Quantity 
                obj.put(CustomDesignerConstants.IN_Currency, venquotation.getCurrency().getCurrencyCode());//Currency code    
                obj.put(CustomDesignerConstants.IN_ProductCode,row.getProduct().getProductid()==null?"":row.getProduct().getProductid());//Product Code
                obj.put(CustomDesignerConstants.IN_UOM, uom);  //Unit of measeurement
                obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(row.getProduct().getProductweight(), companyid));//Product Weight
                obj.put(CustomDesignerConstants.AdditionalDescription, row.getProduct().getAdditionalDesc() != null ? row.getProduct().getAdditionalDesc().replaceAll("\n", "<br>") :"");  //product Addtional description
                obj.put("currencysymbol",venquotation.getCurrency().getSymbol());  //Currency Symbol
                obj.put("currencycode",venquotation.getCurrency().getCurrencyCode());  //Currency Symbol
                obj.put("isGstIncluded",isgstincluded);
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                /*
                 * Following code is to check whether the image is predent for product or not. 
                 * If Image is not present sent s.gif instead of product id
                 */
                String fileName = null;
                fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+row.getProduct().getID() + ".png";
                File file = new File(fileName);
                FileInputStream in = null; 
                String filePathString = "";
                try {
                    in = new FileInputStream(file);
                } catch(java.io.FileNotFoundException ex) {
                    //catched exception if file not found, and to continue for rest of the products
                   filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                if(in != null){
                    filePathString = baseUrl + "productimage?fname=" + row.getProduct().getID() + ".png&isDocumentDesignerPrint=true";
                } else{
                    filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                obj.put(CustomDesignerConstants.imageTag,filePathString);

                //Amol D. Change: Get custom line data and add Custom fields in pdf 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                VendorQuotationDetailCustomData vendorQuotDetailCustomData = (VendorQuotationDetailCustomData) row.getVendorQuotationDetailCustomData();
                replaceFieldMap = new HashMap<String, String>();
                if (vendorQuotDetailCustomData != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, vendorQuotDetailCustomData, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                
                

                /*Product Level Custom Fields Evaluation*/
                VendorQuotationDetailsProductCustomData qProcuctDetailCustom = (VendorQuotationDetailsProductCustomData) row.getVQDetailsProductCustomData();
                if (qProcuctDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, qProcuctDetailCustom, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                /*
                 * Set All Line level Dimension & All LIne level Custom Field Values
                 */
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
               obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                
                JSONObject jObj = new JSONObject();
                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                }
                if (jObj.has("isDisplayUOM") && jObj.get("isDisplayUOM") != null && (Boolean) jObj.get("isDisplayUOM") != false) {
                    obj = accProductObj.getProductDisplayUOM(row.getProduct(), quantity,row.getBaseuomrate(), true, obj);
                }
               jArr.put(obj);
            }
            
            detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
            detailsTableData.put("isDetailsTableData", true);
            jArr.put(detailsTableData);
            
            if (!StringUtil.isNullOrEmpty(invid)) {
                List vendorQuotationTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.vendorquotationtermmap, invid);
                if(vendorQuotationTermMapList != null && !vendorQuotationTermMapList.isEmpty()){
                    Iterator termItr = vendorQuotationTermMapList.iterator();
                    while (termItr.hasNext()) {
                        Object[] obj = (Object[]) termItr.next();
                        /* 
                         * [0] : Sum of termamount  
                         * [1] : Sum of termamountinbase 
                         * [2] : Sum of termTaxamount 
                         * [3] : Sum of termTaxamountinbase 
                         * [4] : Sum of termamountexcludingtax 
                         * [5] : Sum of termamountexcludingtaxinbase
                         */                   
                        if(obj[2] != null){
                            totaltax += (Double) obj[2];
                        }
                    }
                }
                
            }
            
             mainTax = venquotation.getTax();
                if (mainTax != null) { //Get overall tax
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", mainTax.getID());
                    KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                    List taxList = result1.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    mainTaxName = mainTax.getName();
                    totalAmount = subtotal-totalDiscount;
                    totaltax += (taxPercent == 0 ? 0 : totalAmount * taxPercent / 100);//overall tax calculate
                    if (externalCurrencyRate != 0) {
                        globalLevelExchangedRateTotalTax = authHandler.round((totaltax * revExchangeRate), companyid);//exchanged rate total tax amount 
                    }else{
                       globalLevelExchangedRateTotalTax = authHandler.round((totaltax), companyid);//exchanged rate total tax amount 
                    }
                }

            totalAmount = subtotal + totaltax -totalDiscount;
            /*Summary Terms Calculation*/
//            JSONObject summaryData = new JSONObject();    
            String term = "",termsName = "";
            double totalTermAmount = 0;  
//            boolean isTaxTermMapped = false;
//            double lineleveltermTaxAmount = 0;
//            double termAmountBeforeTax = 0;
//            double termAmountAfterTax = 0;
            if (!StringUtil.isNullOrEmpty(venquotation.getID())) {
                Map<String, Object> taxListParams = new HashMap<String, Object>();
                taxListParams.put("companyid", companyid);
                boolean isApplyTaxToTerms=venquotation.isApplyTaxToTerms();

                HashMap<String, Object> filterrequestParams = new HashMap();
                filterrequestParams.put("taxid", venquotation.getTax()==null?"":venquotation.getTax().getID());
                
                HashMap<String, Object> requestParam = new HashMap();
                requestParam.put("vendorQuotation", venquotation.getID());
                KwlReturnObject curresult1 = null;
                curresult1 = accPurchaseOrderobj.getVendorQuotationTermMap(requestParam);
                List<VendorQuotationTermMap> termMap = curresult1.getEntityList();
                for (VendorQuotationTermMap vendorQuotationTermMap : termMap) {

                    double termAmnt = 0;
                    if(venquotation.isGstIncluded()){
                        termAmnt = vendorQuotationTermMap.getTermAmountExcludingTax();
                    }else{
                        termAmnt = vendorQuotationTermMap.getTermamount();
                    }

                    filterrequestParams.put("term", vendorQuotationTermMap.getTerm() == null ? "" : vendorQuotationTermMap.getTerm().getId());
                    filterrequestParams.put("companyid", companyid);

//                    if (uniqueProductTaxList.size() > 0 && isApplyTaxToTerms) {
//                        for (String taxId : uniqueProductTaxList) {
//                            filterrequestParams.put("taxid", taxId);
//                            taxListParams.put("taxid", taxId);
//                            isTaxTermMapped = accTaxObj.isTermMappedwithTax(filterrequestParams);
//                            if (isTaxTermMapped) {
//                                KwlReturnObject taxListResult = accTaxObj.getTaxList(taxListParams);
//                                if (taxListResult != null && taxListResult.getEntityList() != null) {
//                                    List<TaxList> taxListPercent = taxListResult.getEntityList();
//                                    lineleveltermTaxAmount += (termAmnt * (taxListPercent.get(0).getPercent())) / 100;
//                                }
////                                    break;
//                            }
//                        }
//                    } else {
//                        isTaxTermMapped = accTaxObj.isTermMappedwithTax(filterrequestParams);
//                    }

                    InvoiceTermsSales mt = vendorQuotationTermMap.getTerm();
                    termsName += mt.getTerm() + " " + vendorQuotationTermMap.getPercentage() + "%, ";                    
//                    if(isTaxTermMapped){ // If term is mapped with tax
//                        termAmountBeforeTax += vendorQuotationTermMap.getTermamount(); // term amount for adding before tax calculation
//                    } else{
//                        termAmountAfterTax += vendorQuotationTermMap.getTermamount(); // term amount for adding after tax calculation
//                    }

                    summaryData.put(mt.getTerm(), termAmnt);
                    summaryData.put(CustomDesignerConstants.BaseCurrency + mt.getTerm(), (termAmnt * revExchangeRate));//Base currency exchange rate term value-ERP-13451
                    totalTermAmount += termAmnt;
                    double exchangeratetermamount = 0;
                    if (externalCurrencyRate != 0) {
                        exchangeratetermamount = (termAmnt * revExchangeRate);
                    } else {
                        exchangeratetermamount = termAmnt;
                    }
                    globalLevelExchangedRateTermAmount += exchangeratetermamount;
                    String termName = (mt.getTerm() + (termAmnt > 0 ? "(+)" : "(-)"));
                    double tempTermValue = (termAmnt > 0 ? termAmnt : (termAmnt * -1));
                    term += "<div> <table><tr><td>" + termName + " : </td><td>" + Double.toString(tempTermValue) + "</td></tr></table></div><br>";
                     if (!StringUtil.isNullOrEmpty(String.valueOf(termAmnt))) {
                        String allTermsPlaceholder = CustomDesignerConstants.AllTermsKeyValuePair;
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsLabel, termName);
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsValue, CustomDesignHandler.getAmountinCommaDecimal(termAmnt, amountdigitafterdecimal, countryid));
                        appendtermString.append(allTermsPlaceholder);
                    }
                }
                if (!StringUtil.isNullOrEmpty(term) && term.indexOf("<br>") != -1) {
                    term = term.substring(0, term.lastIndexOf("<br>"));
                }
                if(!StringUtil.isNullOrEmpty(appendtermString.toString())){
                    allTerms=appendtermString.toString();
                }
            }
            if (!StringUtil.isNullOrEmpty(termsName)) {
                termsName = termsName.substring(0, termsName.length() - 2);
            }
//            if(isTaxTermMapped){ // If tax mapped with any term
//                if (mainTax != null) {
//                    totaltax = (taxPercent == 0 ? 0 : ((subtotal - totalDiscount) + termAmountBeforeTax) * taxPercent /100); // first add term into subtotal then calculate tax
//                }
//                totaltax += lineleveltermTaxAmount;
//                totalAmount = subtotal - totalDiscount + termAmountBeforeTax + totaltax + termAmountAfterTax; // first add mapped terms amount in subtotal then add total tax then add unmapped terms amount for total amount
//            } else{
                totalAmount = totalAmount + totalTermAmount;
//            }
            globalLevelExchangedRateTermAmount = authHandler.round(globalLevelExchangedRateTermAmount, companyid);
            /* Base Currency Total Amount*/
            globalLevelExchangedRateTotalAmount = globalLevelExchangedRateSubTotalwithDiscount + globalLevelExchangedRateTotalTax + globalLevelExchangedRateTermAmount;
            
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("vendorid", venquotation.getVendor().getID());
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData,companyAccountPreferences,extraCompanyPreferences);
            billAddrress = CommonFunctions.getTotalBillingShippingAddress(venquotation.getBillingShippingAddresses(), true);
            shipAddr=CommonFunctions.getTotalBillingShippingAddress(venquotation.getBillingShippingAddresses(), false);
            vendortransactionalAddr=CommonFunctions.getTotalVendorTransactionalShippingAddress(venquotation.getBillingShippingAddresses(), true); 
              /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            DateFormat df = authHandler.getUserDateFormatterJson(requestObj);//User Date Formatter
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_Vendor_Quotation_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", venquotation.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            /*
             * Check for For new GST flow
             */
            if(extraCompanyPreferences.isIsNewGST()) { // For new GST flow
                    if (!lineLevelTaxNames.isEmpty()) {
                        Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                        while (lineTax.hasNext()) {
                            Map.Entry tax = (Map.Entry) lineTax.next();
                            allLineLevelTax += tax.getKey();
                            allLineLevelTax += "!## ";
                            double taxamount = (double) tax.getValue();
                            allLineLevelTaxAmount += tax.getValue().toString();
                            TotalLineLevelTaxAmount += taxamount;
                            allLineLevelTaxAmount += "!## ";
                            if (taxamount > 0) {
                                lineLevelTaxSign += "+";
                                lineLevelTaxSign += "!## ";
                            } else {
                                lineLevelTaxSign += "-&nbsp;";
                                lineLevelTaxSign += "!## ";
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTax)) {
                        allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)) {
                        allLineLevelTaxAmount = allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxSign)) {
                        lineLevelTaxSign = lineLevelTaxSign.substring(0, lineLevelTaxSign.length() - 4);
                    }
                    totaltax = TotalLineLevelTaxAmount;
                    totalAmount = totalAmount + TotalLineLevelTaxAmount;
            } else {
                /*
                * Putting all line taxes and its information in summary data separated by !##
                */
                for ( String key : lineLevelTaxesGST ) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount +=  lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            
            /**
             * SDP-13772
             * Add Rounding Adjustment in Total Amount
             */
            if(venquotation.isIsRoundingAdjustmentApplied()){
                totalAmount = totalAmount + (venquotation.getRoundingadjustmentamount());
            }

            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency,countryLanguageId);
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(authHandler.formattedAmount(totalAmount, companyid))), indoCurrency);
            }

            String gstAmountInWords = EnglishNumberToWordsOjb.convert(totaltax, currency,countryLanguageId);
            String userId = requestObj.optString(Constants.useridKey);
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            extraparams.put("approvestatuslevel", venquotation.getApprovestatuslevel());

            //Details like company details
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
            String systemcurrencysymbol = venquotation.getVendor().getCurrency().getSymbol();
            String customcurrencysymbol = accCommonTablesDAO.getCustomCurrencySymbol(systemcurrencysymbol, companyid);//Take custom currency symbol
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltax, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(authHandler.round(totalAmount, companyid), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, authHandler.formattedAmount(totalDiscount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, authHandler.formattedAmount((subtotal-totalDiscount), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount(((subtotal - totalDiscount) + totaltax), companyid)); //ERP-25162
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, taxPercent);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword+" Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsDescription_fieldTypeId,term);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, termsName);
            summaryData.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId,totalTermAmount);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId,config == null ? "" : config.getPdfPostText());
            summaryData.put(CustomDesignerConstants.CustomerVendor_Code,venquotation.getVendor().getAcccode()!=null?venquotation.getVendor().getAcccode():"");
            summaryData.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId,customcurrencysymbol);
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term,venquotation.getVendor().getDebitTerm()!=null?(venquotation.getVendor().getDebitTerm().getTermname()!=null?venquotation.getVendor().getDebitTerm().getTermname():""):"");
            summaryData.put(CustomDesignerConstants.BillTo,billAddrress.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.ShipTo,shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.Createdby, createdby);
            summaryData.put(CustomDesignerConstants.Updatedby, updatedby);
            
           /*Exchange Rate Calculation*/
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalAmount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTotalAmount, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotal_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateSubTotal, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTotalTax_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTotalTax, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalSubTotalwithDiscount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateSubTotalwithDiscount, companyid));
            summaryData.put(CustomDesignerConstants.BaseCurrencyGlobalTermAmount_fieldTypeId, authHandler.formattedAmount(globalLevelExchangedRateTermAmount, companyid));
            
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyExchangeRate, 1);
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyAmount, authHandler.formattedAmount(globalLevelExchangedRateTotalAmount, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotal, authHandler.formattedAmount(globalLevelExchangedRateSubTotal, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTaxAmount, authHandler.formattedAmount(globalLevelExchangedRateTotalTax, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencySubTotalWithDicount, authHandler.formattedAmount(globalLevelExchangedRateSubTotalwithDiscount, companyid));
            summaryData.put(CustomDesignerConstants.GlobalSpecificCurrencyTermAmount, authHandler.formattedAmount(globalLevelExchangedRateTermAmount, companyid));
            
            summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);
            summaryData.put(CustomDesignerConstants.VendorTransactionalShipTo, vendortransactionalAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.AllTerms, allTerms);
            summaryData.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, authHandler.formattedAmount(((subtotal-totalDiscount) + totalTermAmount), companyid));
            summaryData.put(CustomDesignerConstants.VENDOR_TITLE, vendorTitle);
//            if(countryid == Constants.indian_country_id) {
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTermSigns, lineLevelTaxSign);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);
//            }
            summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, venquotation.getVendor().getVATTINnumber()!= null ? venquotation.getVendor().getVATTINnumber() : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, venquotation.getVendor().getCSTTINnumber()!= null ? venquotation.getVendor().getCSTTINnumber() : "");
            summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
            summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName()) ? user.getFullName() : "");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber())?user.getContactNumber():"");
            summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, venquotation.getVendor().getGSTIN() != null ? venquotation.getVendor().getGSTIN() : "");
            summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords + Constants.ONLY);

            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE,null,ex);
        }
        return jArr;
    }
    public String getBatchJson(ProductBatch productBatch,HttpServletRequest request,boolean isBatchForProduct,boolean isSerialForProduct) throws ServiceException, SessionExpiredException, JSONException {
        JSONArray jSONArray = new JSONArray();
        DateFormat df = authHandler.getDateOnlyFormatter(request);
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("batch.id");
        filter_params.add(productBatch.getId());

        filter_names.add("company.companyID");
        filter_params.add(sessionHandlerImpl.getCompanyid(request));

        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_params", filter_params);
        KwlReturnObject kmsg = accCommonTablesDAO.getSerialForBatch(filterRequestParams);
        String purchasebatchid = accCommonTablesDAO.getPurchaseBatchId(productBatch.getId());

        List list = kmsg.getEntityList();
        Iterator iter = list.iterator();
        int i = 1;
        while (iter.hasNext()) {
            BatchSerial batchSerial = (BatchSerial) iter.next();
            JSONObject obj = new JSONObject();
            if (i == 1) {
                obj.put("id", productBatch.getId());
                obj.put("batch", productBatch.getName());
                obj.put("batchname", productBatch.getName());
                obj.put("location", productBatch.getLocation().getId());
                obj.put("warehouse", productBatch.getWarehouse().getId());
                obj.put("mfgdate", productBatch.getMfgdate() != null ? authHandler.getDateOnlyFormatter(request).format(productBatch.getMfgdate()) : "");
                obj.put("expdate", productBatch.getExpdate() != null ? authHandler.getDateOnlyFormatter(request).format(productBatch.getExpdate()) : "");
                obj.put("quantity", productBatch.getQuantity());
                obj.put("balance", productBatch.getBalance());
                obj.put("asset", productBatch.getAsset());
            } else {
                obj.put("id", "");
                obj.put("batch", "");
                obj.put("batchname", "");
                obj.put("location", "");
                obj.put("warehouse", "");
                obj.put("mfgdate", "");
                obj.put("expdate", "");
                obj.put("quantity", "");
                obj.put("balance", "");
            }
            i++;
            String purchaseserialid = accCommonTablesDAO.getPurchaseSerialId(batchSerial.getId());
            obj.put("serialnoid", batchSerial.getId());
            obj.put("serialno", batchSerial.getName());
            obj.put("purchasebatchid", purchasebatchid);
            obj.put("purchaseserialid", purchaseserialid);
            obj.put("expstart", batchSerial.getExpfromdate() != null ? authHandler.getDateOnlyFormatter(request).format(batchSerial.getExpfromdate()) : "");
            obj.put("expend", batchSerial.getExptodate() != null ? authHandler.getDateOnlyFormatter(request).format(batchSerial.getExptodate()) : "");
            jSONArray.put(obj);
         }
        if (isBatchForProduct && !isSerialForProduct) //only in batch case
          {
              JSONObject Jobj = new JSONObject();
              Jobj = getOnlyBatchDetail(productBatch, request);
              purchasebatchid = productBatch.getId();
              
              if (!StringUtil.isNullOrEmpty(purchasebatchid)) {
                  Jobj.put("purchasebatchid", purchasebatchid);
              }
              jSONArray.put(Jobj);
          }

          return jSONArray.toString();
    }
   
    public JSONObject getOnlyBatchDetail(ProductBatch productBatch, HttpServletRequest request) throws JSONException, SessionExpiredException {

        JSONObject obj = new JSONObject();
        obj.put("id", productBatch.getId());
        obj.put("batch", productBatch.getName());
        obj.put("batchname", productBatch.getName());
        obj.put("location", productBatch.getLocation().getId());
        obj.put("warehouse", productBatch.getWarehouse().getId());
        obj.put("mfgdate", productBatch.getMfgdate() != null ? authHandler.getDateOnlyFormatter(request).format(productBatch.getMfgdate()) : "");
        obj.put("expdate", productBatch.getExpdate() != null ? authHandler.getDateOnlyFormatter(request).format(productBatch.getExpdate()) : "");
        obj.put("quantity", productBatch.getQuantity());
        obj.put("balance", productBatch.getBalance());
        obj.put("asset", productBatch.getAsset());
        obj.put("expstart", "");
        obj.put("expend", "");

        return obj;
    }
    
    public List getAmountDueCalculatedForOpeningGoodsReceipt(Map<String, Object> request, GoodsReceipt gReceipt) throws ServiceException {
        List ll = new ArrayList();
        double amountdue = 0;
        double totalAmountOfInvoice = 0, ramount = 0;

        String currencyid = (String) request.get(GCURRENCYID);
        String currencyFilterForTrans = "";
        if (request.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = request.get("currencyfilterfortrans") != null ? (String) request.get("currencyfilterfortrans") : "";
        }
        String companyid = (String) request.get(COMPANYID);

        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        KWLCurrency baseCurrency = (KWLCurrency) curresult.getEntityList().get(0);
        String baseCurrencyID = baseCurrency.getCurrencyID();

        currencyid = (gReceipt.getCurrency() == null ? baseCurrencyID : gReceipt.getCurrency().getCurrencyID());

        double grExternalCurrencyRate = 0d;
        Date grCreationDate = null;
        grCreationDate = gReceipt.getCreationDate();
        grExternalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
        totalAmountOfInvoice = gReceipt.getOriginalOpeningBalanceAmount();

        HashMap<String, Object> reqParams1 = new HashMap();
        reqParams1.put("grid", gReceipt.getID());
        reqParams1.put("companyid", companyid);
        if (request.containsKey(Constants.df) && request.get(Constants.df) != null) {
            reqParams1.put(Constants.df, request.get(Constants.df));
        }

        //Get invoice amount used in debit note against PI or debit note made otherwise and then linked to invoice

        KwlReturnObject dnresult = accDebitNoteobj.getDNRowsFromVendorInvoice(reqParams1);
        List<DebitNoteDetail> dnlist = dnresult.getEntityList();
        double invoiceAmountUsedInDebitNote = 0;
        for (DebitNoteDetail dnr : dnlist) {
            if (dnr.getDiscount() != null) {
                invoiceAmountUsedInDebitNote += dnr.getDiscount().getAmountinInvCurrency();
            }
        }

        //Get invoice amount used in payment
        KwlReturnObject result = null;
        result = accVendorPaymentobj.getPaymentsFromGReceipt(reqParams1);
        List<PaymentDetail> list = result.getEntityList();
        if (list != null && !list.isEmpty()) {
            for (PaymentDetail pd : list) {

                Date paymentCreationDate = null;
                double externalCurrencyRate = 0d;
                boolean isopeningBalancePayment = pd.getPayment().isIsOpeningBalencePayment();

                paymentCreationDate = pd.getPayment().getCreationDate();
                if (pd.getPayment().isIsOpeningBalencePayment() && !pd.getPayment().isNormalPayment()) {
                    externalCurrencyRate = pd.getPayment().getExchangeRateForOpeningTransaction();
                } else {
//                    paymentCreationDate = pd.getPayment().getJournalEntry().getEntryDate();
                    externalCurrencyRate = pd.getPayment().getJournalEntry().getExternalCurrencyRate();
                }

                if (pd.getFromCurrency() != null && pd.getToCurrency() != null) {
                    ramount += pd.getAmount() / pd.getExchangeRateForTransaction();
                } else {
                    String fromcurrencyid = (pd.getPayment().getCurrency() == null ? baseCurrencyID : pd.getPayment().getCurrency().getCurrencyID());
                    KwlReturnObject bAmt = null;
                    if (isopeningBalancePayment && pd.getPayment().isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
                    }
                    ramount += (Double) bAmt.getEntityList().get(0);
                }

            }
        }

        //Get invoice amount in advance payment linked to invoice
        result = accVendorPaymentobj.getLinkedDetailsPayment(reqParams1);
        List<LinkDetailPayment> linkedDetaisPayments = result.getEntityList();
        double amtlinkedToPayment = 0;
        if (linkedDetaisPayments != null && !linkedDetaisPayments.isEmpty()) {
            for (LinkDetailPayment ldp : linkedDetaisPayments) {
                amtlinkedToPayment += ldp.getAmountInGrCurrency();
            }
        }

        totalAmountOfInvoice = authHandler.round(totalAmountOfInvoice, companyid);
        amountdue = totalAmountOfInvoice - invoiceAmountUsedInDebitNote - ramount - amtlinkedToPayment;
        double amountDueOriginal = 0;
        if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {
            amountDueOriginal = amountdue;
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, grCreationDate, grExternalCurrencyRate);
                amountdue = (Double) bAmt.getEntityList().get(0);
            }
        } else {
            totalAmountOfInvoice = 0;
            amountdue = 0;
        }
        ll.add(totalAmountOfInvoice);
        ll.add(amountdue);
        ll.add(amountDueOriginal);
        return ll;
    }    
    /**
     * Function to get the GR Amount Details
     * @param map
     * @param GoodsReceipt
     * @return list 
     * @throws ServiceException
     */
    public List getGRAmountDueForMonthlyAgedPayable(Map<String, Object> request, GoodsReceipt gReceipt) throws ServiceException {
        List ll = new ArrayList();
        StringBuilder accNames = new StringBuilder();
        double amountdue = 0;
        boolean belongsTo1099 = false;
        double amount = 0, ramount = 0, contraamount = 0, deductDiscount = 0, termAmount = 0;
        String currencyid = (String) request.get(GCURRENCYID);
        String currencyFilterForTrans = "";
        if (request.containsKey("currencyfilterfortrans")) {
            currencyFilterForTrans = request.get("currencyfilterfortrans") != null ? (String) request.get("currencyfilterfortrans") : "";
        }
        String companyid = (String) request.get(COMPANYID);
        ArrayList acclist = new ArrayList();
        KwlReturnObject curresult = null;// accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
        String baseCurrencyID = currencyid;
        currencyid = (gReceipt.getCurrency() == null ? baseCurrencyID : gReceipt.getCurrency().getCurrencyID());
        Map<GoodsReceiptDetail, Object[]> map= applyDebitNotes(request, gReceipt);
        for (Map.Entry<GoodsReceiptDetail, Object[]> entry : map.entrySet()) {
            Object[] temp = entry.getValue();
            amount += (Double) temp[0] - (Double) temp[2];
            accNames.append((String) temp[3]).append(",");
            acclist.add((String) temp[4]);
            deductDiscount += (Double) temp[5];
        }

        belongsTo1099 = accTaxObj.belongsTo1099Count(companyid, acclist);
        double amountDueOriginal = 0;
        JournalEntryDetail tempd = gReceipt.getTaxEntry();
        if (tempd != null) {
            amount += tempd.getAmount();
        }
        double grExternalCurrencyRate = 0d;
        Date grCreationDate = null;
        grCreationDate = gReceipt.getCreationDate();
        if (gReceipt.isIsOpeningBalenceInvoice() && !gReceipt.isNormalInvoice()) {
            grExternalCurrencyRate = gReceipt.getExchangeRateForOpeningTransaction();
            amount = gReceipt.getOriginalOpeningBalanceAmount();
        } else {
//            grCreationDate = gReceipt.getJournalEntry().getEntryDate();
            grExternalCurrencyRate = gReceipt.getJournalEntry().getExternalCurrencyRate();
        }

        HashMap<String, Object> reqParams1 = new HashMap();
        reqParams1.put("grid", gReceipt.getID());
        reqParams1.put("companyid", companyid);
        reqParams1.put("isMonthlyAgedPayable", true);
        if (request.containsKey(Constants.df) && request.get(Constants.df) != null) {
            reqParams1.put(Constants.df, request.get(Constants.df));
        }
        if (request.containsKey("asofdate") && request.get("asofdate") != null) {
            reqParams1.put("asofdate", request.get("asofdate"));
        }

        //Get amount knock off using otherwise credit notes.
        KwlReturnObject dnresult = accDebitNoteobj.getDNRowsFromVendorInvoice(reqParams1);
        List<String> dnlist = dnresult.getEntityList();
        double cnAmountOW = 0;
        for (String dnid : dnlist) {
            boolean isopeningBalanceInvoice = gReceipt.isIsOpeningBalenceInvoice();
            KwlReturnObject debitNoteDetail = kwlCommonTablesDAOObj.getObject(DebitNoteDetail.class.getName(), dnid);
            DebitNoteDetail dnr = (DebitNoteDetail) debitNoteDetail.getEntityList().get(0);
            String fromcurrencyid = (dnr.getDebitNote().getCurrency() == null ? baseCurrencyID : dnr.getDebitNote().getCurrency().getCurrencyID());
            if (dnr.getDiscount() != null) {
                KwlReturnObject bAmt = null;
                if (isopeningBalanceInvoice && gReceipt.isConversionRateFromCurrencyToBase()) {// if gReceipt is opening balance gReceipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, currencyid, grCreationDate, grExternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, dnr.getDiscount().getDiscountValue(), fromcurrencyid, currencyid, grCreationDate, grExternalCurrencyRate);
                }
                cnAmountOW += (Double) bAmt.getEntityList().get(0);
            }
        }
        KwlReturnObject result = accVendorPaymentobj.getPaymentsFromGReceipt(reqParams1);
        List<String> list = result.getEntityList();
        if (list != null && !list.isEmpty()) {
            for (String paymentDetailID : list) {
                KwlReturnObject paymentDetails = kwlCommonTablesDAOObj.getObject(PaymentDetail.class.getName(), paymentDetailID);
                PaymentDetail pd = (PaymentDetail) paymentDetails.getEntityList().get(0);
                Date paymentCreationDate = null;
                double externalCurrencyRate = 0d;
                boolean isopeningBalancePayment = pd.getPayment().isIsOpeningBalencePayment();

                paymentCreationDate = pd.getPayment().getCreationDate();
                if (pd.getPayment().isIsOpeningBalencePayment() && !pd.getPayment().isNormalPayment()) {
                    externalCurrencyRate = pd.getPayment().getExchangeRateForOpeningTransaction();
                } else {
//                    paymentCreationDate = pd.getPayment().getJournalEntry().getEntryDate();
                    externalCurrencyRate = pd.getPayment().getJournalEntry().getExternalCurrencyRate();
                }

                if (pd.getFromCurrency() != null && pd.getToCurrency() != null) {//Comment
                    ramount += pd.getAmount() / pd.getExchangeRateForTransaction();
                } else {
                    String fromcurrencyid = (pd.getPayment().getCurrency() == null ? baseCurrencyID : pd.getPayment().getCurrency().getCurrencyID());
                    KwlReturnObject bAmt = null;
                    if (isopeningBalancePayment && pd.getPayment().isConversionRateFromCurrencyToBase()) {// if payment is opening balance payment and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, pd.getAmount(), fromcurrencyid, currencyid, paymentCreationDate, externalCurrencyRate);
                    }
                    ramount += (Double) bAmt.getEntityList().get(0);
                }

            }
        }
        result = accVendorPaymentobj.getLinkedDetailsPayment(reqParams1);
        List<Double> amountInGrCurrencyList = result.getEntityList();
        double amtlinkedToPayment = 0;
        if (amountInGrCurrencyList != null && !amountInGrCurrencyList.isEmpty()) {
            for (double amountInGrCurrency : amountInGrCurrencyList) {
                amtlinkedToPayment += amountInGrCurrency;
            }
        }
        result = accVendorPaymentobj.getContraPayReceiptIDFromGReceipt(gReceipt.getID(), companyid);
        List<String> list1 = result.getEntityList();
        if (list1 != null && !list1.isEmpty()) {
            for (String receiptDetailID : list1) {
                KwlReturnObject receiptDetail = kwlCommonTablesDAOObj.getObject(ReceiptDetail.class.getName(), receiptDetailID);
                ReceiptDetail pd = (ReceiptDetail) receiptDetail.getEntityList().get(0);
                contraamount += pd.getAmount();
                String fromcurrencyid = (pd.getReceipt().getCurrency() == null ? baseCurrencyID : pd.getReceipt().getCurrency().getCurrencyID());
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOther(request, contraamount, fromcurrencyid, currencyid, grCreationDate, grExternalCurrencyRate);
                contraamount = (Double) bAmt.getEntityList().get(0);
            }
        }

        // Get amount from Invoice Terms 
        HashMap<String, Object> requestParam = new HashMap();
        requestParam.put("invoiceid", gReceipt.getID());
        curresult = accGoodsReceiptobj.getInvoiceTermMap(requestParam);
        List<ReceiptTermsMap> termMap = curresult.getEntityList();
        for (ReceiptTermsMap invoiceTerMap : termMap) {
            termAmount += invoiceTerMap.getTermamount();
        }

        KwlReturnObject resultDn = accDebitNoteobj.getDistintDNFromGReceipt(gReceipt.getID());
        List listDn = resultDn.getEntityList();
        DebitNote debitNote = null;
        double termAmountDn = 0;
        if (listDn != null && !listDn.isEmpty()) {
            for (Object dn : listDn) {
                debitNote = (DebitNote) dn;
                if (debitNote != null) {
                    requestParam.put("debitNoteId", debitNote.getID());
                    curresult = accDebitNoteobj.getDebitNoteTermMap(requestParam);
                    List<DebitNoteTermsMap> termMapDn = curresult.getEntityList();
                    if (termMapDn != null && !termMapDn.isEmpty()) {
                        for (DebitNoteTermsMap debitNoteTermsMap : termMapDn) {
                            termAmountDn += debitNoteTermsMap.getTermamount();
                        }
                    }
                }
            }
        }
        amount += termAmount;
        amount = authHandler.round(amount, companyid);
        amountdue = amount - cnAmountOW - ramount - contraamount - termAmountDn - amtlinkedToPayment;
        if (amountdue != 0 && Math.abs(amountdue) >= 0.000001) {
            amountDueOriginal = amountdue;
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
                KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(request, amountdue, currencyid, currencyFilterForTrans, grCreationDate, grExternalCurrencyRate);
                amountdue = (Double) bAmt.getEntityList().get(0);
            }
        } else {
            amount = 0;
            amountdue = 0;
        }
        ll.add(amount);
        ll.add(amountdue);
        ll.add(accNames.substring(0, Math.max(0, accNames.length() - 1)));
        ll.add(belongsTo1099);
        ll.add(deductDiscount);
        ll.add(amountDueOriginal);
        return ll;
    }
    
    public JSONArray getGRODetailsItemJSON(JSONObject requestObj, String SOID, HashMap<String, Object> paramMap) {
        JSONArray jArr = new JSONArray();
        String Invref = "";
        String POref = "";
        String POrefdate = "";
        String VQouteRef = "";
        Tax mainTax = null;
        String allTerms = "";
        boolean isLocationForProduct = false,isbatchforproduct=false,isserialforproduct=false,isWarehouseForProduct=false;
        StringBuilder appendtermString = new StringBuilder();
        double totalDiscount = 0;
        double totaltax = 0, discountTotalQuotation = 0, Amountwithoutterm = 0,rowdiscountvalue=0;
        double totalAmount = 0,TotalLineLevelTaxAmount = 0;
        double taxPercent = 0;
        String billAddr = "", shipAddr = "", createdby = "", vendorbillAddr = "", vendorshipAddr = "", vendortransactionalAddr = "", updatedby = "";
        String mainTaxName = "", globallevelcustomfields = "", globalleveldimensions = "", vendorTitle = "",allLineLevelTax="", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "";
        /*line leval tax in Good receipts order*/
        Map<String,Double> lineLevelTaxNames = new LinkedHashMap<String,Double>();
        Set<String> lineLevelTaxesGST = new HashSet<String>();
        Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
        Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
        Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
        JSONObject summaryData = new JSONObject();
        int quantitydigitafterdecimal = 2, amountdigitafterdecimal = 2, unitpricedigitafterdecimal = 2;
        double totalQuantity = 0, subTotalWithDiscount = 0;
        /*
         * a variable to get debit term of goods receipt order
         */
        String netDebitTerm = "", netVendorTerm;
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            String companyid = requestObj.optString(Constants.companyKey);
            java.util.Date entryDate = null;
            PdfTemplateConfig config = null;
            double subtotal = 0;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestObj);
            HashMap<String, Object> soRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), SOID);
            GoodsReceiptOrder goodsReceiptOrder = (GoodsReceiptOrder) objItr.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), requestObj.optString(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            createdby = goodsReceiptOrder.getCreatedby() != null ? goodsReceiptOrder.getCreatedby().getFullName() : "";
            updatedby = goodsReceiptOrder.getModifiedby() != null ? goodsReceiptOrder.getModifiedby().getFullName() : "";
            /*
             * getting debit term of goods receipt order
             */
            netDebitTerm=goodsReceiptOrder.getTerm() != null ? goodsReceiptOrder.getTerm().getTermname() : "";
            /*
             * getting debit term of vendor
             */
            netVendorTerm = goodsReceiptOrder.getVendor() !=null?goodsReceiptOrder.getVendor().getDebitTerm().getTermname(): "";
            /**
             * get customer title (Mr./Mrs.)
             */
            
            //document currency
            if (goodsReceiptOrder != null && goodsReceiptOrder.getCurrency() != null && !StringUtil.isNullOrEmpty(goodsReceiptOrder.getCurrency().getCurrencyID())) {
                summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, goodsReceiptOrder.getCurrency().getCurrencyID());
            }
            
            double externalCurrencyRate = goodsReceiptOrder.getExternalCurrencyRate();
            double revExchangeRate = 0.0;
            if (externalCurrencyRate != 0) {
                revExchangeRate = 1 / externalCurrencyRate;
            }
            vendorTitle = goodsReceiptOrder.getVendor().getTitle();
            if(!StringUtil.isNullOrEmpty(vendorTitle)){
                KwlReturnObject masterItemResult = kwlCommonTablesDAOObj.getObject(MasterItem.class.getName(), vendorTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                vendorTitle = masterItem.getValue();
            }
//            String currencyid = currencyid = (deliveryOrder.getCurrency()==null)? currency.getCurrencyID() : salesOrder.getCurrency().getCurrencyID();;
            filter_names.add("grOrder.ID");
            filter_params.add(goodsReceiptOrder.getID());
            soRequestParams.put("filter_names", filter_names);
            soRequestParams.put("filter_params", filter_params);
            soRequestParams.put("order_by", order_by);
            soRequestParams.put("order_type", order_type);
            KwlReturnObject podresult = accGoodsReceiptobj.getGoodsReceiptOrderDetails(soRequestParams);
            Iterator itr = podresult.getEntityList().iterator();
            String netinword = "", gstAmountInWords = "";
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, requestObj.optInt("moduleid"));
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }

            KwlReturnObject resultcompany = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) resultcompany.getEntityList().get(0);
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            int countryid = 0;
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            int rowcnt = 0;
            Set<String> uniqueProductTaxList = new HashSet<String>();
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
            while (itr.hasNext()) {
                String proddesc = "";
                rowcnt++;
                DateFormat df1 = authHandler.getDateOnlyFormat();
                DateFormat df = authHandler.getUserDateFormatterJson(requestObj);
                GoodsReceiptOrderDetails row = (GoodsReceiptOrderDetails) itr.next();
                if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
                    Product product = (Product) prodresult.getEntityList().get(0);
                    isLocationForProduct = product.isIslocationforproduct();
                    isbatchforproduct = product.isIsBatchForProduct();
                    isserialforproduct = product.isIsSerialForProduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();

                }
                JSONObject obj = new JSONObject();
                JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                Product prod = row.getProduct();
                obj.put(CustomDesignerConstants.SrNO, rowcnt);  //Sr No
                obj.put(CustomDesignerConstants.ProductName, prod.getName());// productname
                obj.put("type", prod.getProducttype() == null ? "" : prod.getProducttype().getName());
                proddesc = StringUtil.isNullOrEmpty(row.getDescription()) ? (StringUtil.isNullOrEmpty(row.getProduct().getDescription()) ? "" : StringUtil.DecodeText(row.getProduct().getDescription())) : StringUtil.DecodeText(row.getDescription());
                obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>"));//Product Description
                obj.put(CustomDesignerConstants.AdditionalDescription, !StringUtil.isNullOrEmpty(prod.getAdditionalDesc()) ? prod.getAdditionalDesc().replaceAll(Constants.REGEX_LINE_BREAK, "<br>") :"");  //product Addtional description ERP-39061
                double rate = 0;
                /**
                 * goodsReceiptOrder.isGstIncluded() is true then rate is updated to rateIncludegst ERP-38438
                */
                rate = goodsReceiptOrder.isGstIncluded() ? row.getRateincludegst() : row.getRate();
                obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(row.getRate(), companyid));// Rate
                obj.put(CustomDesignerConstants.RATEINCLUDINGGST,  authHandler.formattingDecimalForUnitPrice(row.getRateincludegst(), companyid));// Rate incluging GST
                obj.put(CustomDesignerConstants.GR_ProductCode, row.getProduct().getProductid() == null ? "" : row.getProduct().getProductid());//Product Code
                obj.put(CustomDesignerConstants.HSCode, (row.getProduct().getHSCode() != null) ? row.getProduct().getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid
                String uom = row.getUom() == null ? "" : row.getUom().getNameEmptyforNA(); //ERP-25533
                double quantity = row.getActualQuantity();
                double receivedQuantity = row.getDeliveredQuantity();
                totalQuantity += quantity;
//                obj.put("4", quantity + " " + uom); // Quantity
                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {
                    obj.put("4", row.getProduct().getProductid() == null ? "" : row.getProduct().getProductid());
                }
                obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(authHandler.round((receivedQuantity * rate), companyid), companyid)); // Amount
                obj.put(CustomDesignerConstants.GR_ActualQuantityWithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom); // Actual Quantity
                obj.put(CustomDesignerConstants.GR_ReceivedQuantityWithUOM, authHandler.formattingDecimalForQuantity(receivedQuantity, companyid) + " " + uom); //Received Quantity                              
                obj.put(CustomDesignerConstants.PartNumber, StringUtil.isNullOrEmpty(prod.getCoilcraft()) ? "" : prod.getCoilcraft()); //Part Number
                obj.put(CustomDesignerConstants.SupplierPartNumber, StringUtil.isNullOrEmpty(prod.getSupplier()) ? "" : prod.getSupplier());
                obj.put(CustomDesignerConstants.CustomerPartNumber, StringUtil.isNullOrEmpty(prod.getInterplant()) ? "" : prod.getInterplant());
                double rowTaxPercent = 0,rowTaxAmt=0;
                String rowTaxName = "";
                entryDate = goodsReceiptOrder.getOrderDate();
                obj.put("7", rowTaxPercent);// Tax
                if (storageHandlerImpl.GetSATSCompanyId().contains(companyid)) {
                    obj.put("11", row.getRemark() == null ? "" : row.getRemark());//get Remarks of Product Grid
                    obj.put("12", (row.getPartno() != null) ? row.getPartno() : "");//getSerialnumber of ProductGrid
                }

                KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                    cateogry += categoryName + " ";
                }
                if (StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);
                HashMap<String, Object> params = new HashMap<String, Object>();
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("companyid");
                filter_names.add("fieldtype");
                filter_names.add("customcolumn");
                filter_names.add("moduleid");
                filter_params.add(companyid);
                filter_params.add(4);
                filter_params.add(1);
                filter_params.add(requestObj.optInt("moduleid"));
                params.put("filter_names", filter_names);
                params.put("filter_values", filter_params);

                KwlReturnObject fieldparams = accAccountDAOobj.getFieldParams(params);
                List fieldParamsList = fieldparams.getEntityList();

                JSONArray jsonarr = new JSONArray();
                for (int cnt = 0; cnt < fieldParamsList.size(); cnt++) {
                    FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("comboName", fieldParamsObj.getFieldname());
                    jsonarr.put(tempObj);
                }

                obj.put("groupingComboList", jsonarr);

                /*product linking*/
                if (row.getVidetails() != null) {
                    obj.put(CustomDesignerConstants.GR_VIPONo, row.getVidetails().getGoodsReceipt().getGoodsReceiptNumber());
                } else if (row.getPodetails() != null) {
                    obj.put(CustomDesignerConstants.GR_VIPONo, row.getPodetails().getPurchaseOrder().getPurchaseOrderNumber());
                } else {
                    obj.put(CustomDesignerConstants.GR_VIPONo, "");
                }

                obj.put(CustomDesignerConstants.GR_Remarks, row.getRemark() == null ? "" : row.getRemark());//get Remarks of Product Grid
                obj.put(CustomDesignerConstants.GR_SerialNumber, (row.getPartno() != null) ? row.getPartno() : "");//getSerialnumber of ProductGrid
                double amount = authHandler.round((receivedQuantity *rate), companyid);
                totalAmount += amount;//no gst in gro therefore subtotal=totalamount
                subtotal += amount;
                rowdiscountvalue = (row.getDiscountispercent() == 1) ? amount * row.getDiscount() / 100 : row.getDiscount();
                totalDiscount +=authHandler.round(rowdiscountvalue, companyid);
                //Sub Total
                subTotalWithDiscount = authHandler.round((amount - rowdiscountvalue), companyid);
                //Sub Total - Discount
                if(goodsReceiptOrder.isGstIncluded()){
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(subtotal - row.getRowTaxAmount(), companyid));
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount(subTotalWithDiscount - row.getRowTaxAmount(), companyid));
                } else{
                    obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(subtotal, companyid));
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount((subTotalWithDiscount - rowdiscountvalue), companyid));
                }
                
                obj.put(CustomDesignerConstants.GR_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                
                /*
                 * to get the linkig information upto 2-3 levels (Mayur B).
                 */
                obj.put(CustomDesignerConstants.GR_ReceivedQuantity, authHandler.formattingDecimalForQuantity(receivedQuantity, companyid));
                obj.put(CustomDesignerConstants.GR_UOM, uom);
                obj.put(CustomDesignerConstants.GR_ActualQuantity, authHandler.formattingDecimalForQuantity(quantity, companyid));
                obj.put("currencysymbol", goodsReceiptOrder.getCurrency().getSymbol());//used for headercurrency & record currency
                obj.put("currencycode", goodsReceiptOrder.getCurrency().getCurrencyCode());//used for headercurrencyCode & record currency
                obj.put("basecurrencysymbol", goodsReceiptOrder.getCompany().getCurrency().getSymbol());//used for headercurrency & record currency
                obj.put("basecurrencycode", goodsReceiptOrder.getCompany().getCurrency().getCurrencyCode());//used for headercurrency & record currency
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                /*
                 * Following code is to check whether the image is predent for product or not. 
                 * If Image is not present sent s.gif instead of product id
                 */
                String fileName = null;
                fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+prod.getID() + ".png";
                File file = new File(fileName);
                FileInputStream in = null; 
                String filePathString = "";
                try {
                    in = new FileInputStream(file);
                } catch(java.io.FileNotFoundException ex) {
                    //catched exception if file not found, and to continue for rest of the products
                   filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                if(in != null){
                    filePathString = baseUrl + "productimage?fname=" + prod.getID() + ".png&isDocumentDesignerPrint=true";
                } else{
                    filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                obj.put(CustomDesignerConstants.imageTag, filePathString);
                if (row.getVidetails() != null) {
                    if (Invref.indexOf(row.getVidetails().getGoodsReceipt().getGoodsReceiptNumber()) == -1) {
                        Invref += row.getVidetails().getGoodsReceipt().getGoodsReceiptNumber() + ", ";
                    }
                    if (row.getVidetails().getPurchaseorderdetail() != null) {
                        if (POref.indexOf(row.getVidetails().getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber()) == -1) {
                            POref += row.getVidetails().getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber() + ", ";
                            POrefdate += row.getVidetails().getPurchaseorderdetail().getPurchaseOrder().getOrderDate() + ",";
                        }
                        if (row.getVidetails().getPurchaseorderdetail().getVqdetail() != null) {
                            if (VQouteRef.indexOf(row.getVidetails().getPurchaseorderdetail().getVqdetail().getVendorquotation().getQuotationNumber()) == -1) {
                                VQouteRef += row.getVidetails().getPurchaseorderdetail().getVqdetail().getVendorquotation().getQuotationNumber() + ", ";
                            }
                        }
                    }
                    if (row.getVidetails().getVendorQuotationDetail() != null) {
                        if (VQouteRef.indexOf(row.getVidetails().getVendorQuotationDetail().getVendorquotation().getQuotationNumber()) == -1) {
                            VQouteRef += row.getVidetails().getVendorQuotationDetail().getVendorquotation().getQuotationNumber() + ", ";
                        }
                    }
                } else if (row.getPodetails() != null) {
                    if (POref.indexOf(row.getPodetails().getPurchaseOrder().getPurchaseOrderNumber()) == -1) {
                        POref += row.getPodetails().getPurchaseOrder().getPurchaseOrderNumber() + ", ";
                        POrefdate += row.getPodetails().getPurchaseOrder().getOrderDate() + ", ";
                    }
                    if (row.getPodetails().getVqdetail() != null) {
                        if (VQouteRef.indexOf(row.getPodetails().getVqdetail().getVendorquotation().getQuotationNumber()) == -1) {
                            VQouteRef = row.getPodetails().getVqdetail().getVendorquotation().getQuotationNumber() + ", ";
                        }
                    }
                } else if (row.getSecuritydetails() != null) {
                    if (row.getSecuritydetails().getPodetail()!= null) {
                        if (POref.indexOf(row.getSecuritydetails().getPodetail().getPurchaseOrder().getPurchaseOrderNumber()) == -1) {
                            POref += row.getSecuritydetails().getPodetail().getPurchaseOrder().getPurchaseOrderNumber() + ", ";
                            POrefdate += row.getSecuritydetails().getPodetail().getPurchaseOrder().getOrderDate() + ", ";
                        }
                    }
                }
                if (companyAccountPreferences.isIsBatchCompulsory() || companyAccountPreferences.isIsSerialCompulsory()
                        || companyAccountPreferences.isIslocationcompulsory() || companyAccountPreferences.isIswarehousecompulsory() || companyAccountPreferences.isIsrowcompulsory()
                        || companyAccountPreferences.isIsrackcompulsory() || companyAccountPreferences.isIsbincompulsory()) {
                    if (isbatchforproduct || isserialforproduct || isLocationForProduct || isWarehouseForProduct) {  //product level batch and serial no on or not but now only location is checked
                        String batchdetails = accInvoiceServiceDAO.getNewBatchJson(row.getProduct(), requestObj, row.getID());
                        JSONArray locjArr = new JSONArray(batchdetails);
                        String location = "";
                        String warehouseName = "", locationName = "", batchname = "", warehouse = "", serialnumber = "",batchexpdate = "",batchmfgdate = "";
                        String locationnamenew = "", batchnamenew = "", warehousenew = "", serialnumbernew = "", batchexpdatenew = "", batchesmfgdatenew = "",warehousenewName="";
                        Set<String> batchnames = new LinkedHashSet();
                        Set<String> serialnumbers = new LinkedHashSet();
                        Set<String> locationnames = new LinkedHashSet();
                        Set<String> warehouses = new LinkedHashSet();
                        LinkedList<String> batchesexpirydate = new LinkedList();
                        LinkedList<String> batchesmfgdate = new LinkedList();
                        Date date = null;
                        for (int i = 0; i < locjArr.length(); i++) {
                            JSONObject jSONObject = new JSONObject(locjArr.get(i).toString());
                            location = jSONObject.optString("location", "");
                            batchname = jSONObject.optString("batchname", "");
                            warehouse = jSONObject.optString("warehouse", "");
                            serialnumber = jSONObject.optString("serialno", "");
                            batchexpdate = jSONObject.optString("expdate", "");
                            batchmfgdate = jSONObject.optString("mfgdate", "");
                            serialnumbers.add(serialnumber);
                            batchnames.add(batchname);
                            //warehouses.add(warehouse);
                            
                             if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
                                date = df1.parse(batchmfgdate);
                                batchmfgdate = df.format(date);
                                if (batchesmfgdate.contains(batchmfgdate)) {
                                    batchesmfgdate.add(" ");
                                } else {
                                    batchesmfgdate.add(batchmfgdate);
                                }
                            } else {
                                batchesmfgdate.add(" ");
                            }
                             if (!StringUtil.isNullOrEmpty(warehouse)) {
//                                if (warehouses.contains(warehouse)) {
                                    //warehouses.add(" ");.
                                    KwlReturnObject warehouseResult = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouse);
                                    InventoryWarehouse warehouseobj = (InventoryWarehouse) warehouseResult.getEntityList().get(0);
                                    if (warehouseobj != null) {
                                     warehouseName = warehouseobj.getName();
                                     warehouses.add(warehouseName);
                                 }
//                                } else {
//                                    KwlReturnObject warehouseResult = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), warehouse);
//                                    InventoryWarehouse warehouseobj = (InventoryWarehouse) warehouseResult.getEntityList().get(0);
//                                    warehouseName = warehouseobj.getName();
//                                    warehouses.add(warehouseName);
//                                }
                            }
                             
                             if (!StringUtil.isNullOrEmpty(batchexpdate)) {
                                date = df1.parse(batchexpdate);
                                batchexpdate = df.format(date);
                                if (batchesexpirydate.contains(batchexpdate)) {
                                     batchesexpirydate.add(" ");
                                } else {
                                    batchesexpirydate.add(batchexpdate);
                                }
                            } else {
                                batchesexpirydate.add(" ");
                            }

                            if (!StringUtil.isNullOrEmpty(location)) {
                                KwlReturnObject loc = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), location);
                                InventoryLocation localist = (InventoryLocation) loc.getEntityList().get(0);
                                locationName = localist.getName();
                                locationnames.add(locationName);
//                                locationName = locationName.concat(",");
                            }
//
//                            if (!StringUtil.isNullOrEmpty(batchname)) {
//                                batchname = batchname.concat(",");
//                            }
//                            if (!StringUtil.isNullOrEmpty(serialnumber)) {
//                                serialnumber = serialnumber.concat(",");
//                            }
//                             if (!StringUtil.isNullOrEmpty(batchexpdate)) {
//                                batchexpdate = batchexpdate.concat("<br>");
//                            }
//                             
//                             if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
//                                batchmfgdate = batchmfgdate.concat("<br>");
//                            }
                        }
                        for (String str : warehouses) {
                            String wno = "";
                            wno = str;
                            if(!StringUtil.isNullOrEmpty(wno)){
                                warehousenewName += wno.concat("!##");
                            }
                        }
                        for (String str : batchnames) {
                            String bno = "";
                            bno = str;
                            if(!StringUtil.isNullOrEmpty(bno)){
                                batchnamenew += bno.concat("!##");
                            }
                        }

                        for (String str : serialnumbers) {
                            String sno = "";
                            sno = str;
                            if(!StringUtil.isNullOrEmpty(sno)){
                                serialnumbernew += sno.concat("!##");
                            }
                        }
                        for (String str : locationnames) {
                            String lno = "";
                            lno = str;
                            if(!StringUtil.isNullOrEmpty(lno)){
                                locationnamenew += lno.concat("!##");
                            }
                        }
                        for (String str : batchesexpirydate) {
                            String bexp = "";
                            bexp = str;
                            if(!StringUtil.isNullOrEmpty(bexp)){
                                batchexpdatenew += bexp.concat("!##");
                            }
                        }
                        for (String str : batchesmfgdate) {
                            String bmfg = "";
                            bmfg = str;
                            if (!StringUtil.isNullOrEmpty(bmfg) && !bmfg.equals(" ")) {
                                batchesmfgdatenew += bmfg.concat("!##");
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(warehousenewName)) {
                            warehousenewName = warehousenewName.substring(0, warehousenewName.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(locationnamenew)) {
                            locationnamenew = locationnamenew.substring(0, locationnamenew.length() - 3);
                        }
                        
                        if (!StringUtil.isNullOrEmpty(serialnumbernew)) {
                            serialnumbernew = serialnumbernew.substring(0, serialnumbernew.length() - 3);
                        }

                        if (!StringUtil.isNullOrEmpty(batchnamenew)) {
                            batchnamenew = batchnamenew.substring(0, batchnamenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(batchesmfgdatenew)) {
                            batchesmfgdatenew = batchesmfgdatenew.substring(0, batchesmfgdatenew.length() - 3);
                        }
                        obj.put(CustomDesignerConstants.GR_SerialNumber, serialnumbernew);
                        obj.put(CustomDesignerConstants.BatchNumber, batchnamenew);
                        obj.put(CustomDesignerConstants.GR_Loc, locationnamenew);
                        obj.put(CustomDesignerConstants.Warehouse, warehousenewName);
                        obj.put(CustomDesignerConstants.BatchNumberExp, batchexpdatenew);
                        obj.put(CustomDesignerConstants.ManufacturingDate, batchesmfgdatenew);// Batch Manufacturing Date
                    } else { //if not activated location for product level then take default location for product.
                        obj.put(CustomDesignerConstants.GR_Loc, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                    }
                } else { //if not activated location for product level then take default location for product.
                    obj.put(CustomDesignerConstants.GR_Loc, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                }
                //Calculate row tax amount
                if (row != null && row.getTax() != null) {
                    requestParams.put("transactiondate", entryDate);
                    requestParams.put("taxid", row.getTax().getID());
                    KwlReturnObject resulttax = accTaxObj.getTax(requestParams);
                    List taxList = resulttax.getEntityList();
                    Object[] taxObj = (Object[]) taxList.get(0);
                    rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                    rowTaxName = row.getTax().getName();
                    rowTaxAmt = row.getRowTaxAmount();
                    uniqueProductTaxList.add(row.getTax().getID());
                }
                totaltax +=authHandler.round(rowTaxAmt, companyid);
                /*
                 * LineLevelTax fields for Indian and USA country 
                 */
                String lineLevelTax = "";
                String lineLevelTaxPercent = "";
                String lineLevelTaxAmount = "";
                double lineLevelTaxAmountTotal = 0;
                /*
                 *Check For new GST flow
                 */
                if(extraCompanyPreferences.isIsNewGST()) { // 
                    HashMap<String, Object> GRDetailParams = new HashMap<String, Object>();
                    GRDetailParams.put("goodsReceiptOrderID", row.getID());
                    GRDetailParams.put("orderbyadditionaltax", true);
                    // GST
                    GRDetailParams.put("termtype", 7);
                    KwlReturnObject grdTermMapresult = accGoodsReceiptobj.getGRODetailsTermMap(GRDetailParams);
                    List<ReceiptOrderDetailTermMap> gst = grdTermMapresult.getEntityList();
                    obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                    obj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.TAXABLE_VALUE, subTotalWithDiscount);
                    
                    for (ReceiptOrderDetailTermMap receiptOrderDetailTermMap : gst) {
                        LineLevelTerms mt = receiptOrderDetailTermMap.getTerm();
                        if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                            obj.put(CustomDesignerConstants.CGSTPERCENT, receiptOrderDetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CGSTAMOUNT, receiptOrderDetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, receiptOrderDetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, receiptOrderDetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                            obj.put(CustomDesignerConstants.IGSTPERCENT, receiptOrderDetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.IGSTAMOUNT, receiptOrderDetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, receiptOrderDetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, receiptOrderDetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                            obj.put(CustomDesignerConstants.SGSTPERCENT, receiptOrderDetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.SGSTAMOUNT, receiptOrderDetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, receiptOrderDetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, receiptOrderDetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                            obj.put(CustomDesignerConstants.UTGSTPERCENT, receiptOrderDetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.UTGSTAMOUNT, receiptOrderDetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, receiptOrderDetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, receiptOrderDetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                            obj.put(CustomDesignerConstants.CESSPERCENT, receiptOrderDetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CESSAMOUNT, receiptOrderDetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, receiptOrderDetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, receiptOrderDetailTermMap.getTermamount());
                        }
                        lineLevelTax += mt.getTerm();
                        lineLevelTax += "!## ";

                        lineLevelTaxPercent += authHandler.formattingDecimalForAmount(receiptOrderDetailTermMap.getPercentage(), companyid);
                        lineLevelTaxPercent += "!## ";
                        lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(receiptOrderDetailTermMap.getTermamount(), amountdigitafterdecimal, countryid);
                        lineLevelTaxAmount += "!## ";
                        /*
                         * calculating total of line level taxes
                         */
                        lineLevelTaxAmountTotal += receiptOrderDetailTermMap.getTermamount();
                        if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                            double value = lineLevelTaxNames.get(mt.getTerm());
                            lineLevelTaxNames.put(mt.getTerm(), receiptOrderDetailTermMap.getTermamount() + value);

                        } else {
                            lineLevelTaxNames.put(mt.getTerm(), receiptOrderDetailTermMap.getTermamount());
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                        lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                        lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                        lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                    }
                    obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                    obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                    obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                    /*
                     * putting subtotal+tax
                     */
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((amount-rowdiscountvalue), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));       
                    //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                    ExportRecordHandler.setHsnSacProductDimensionField(row.getProduct(), obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, obj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                    gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                } else {/*Not indian and us then take row level tax */
                    /*
                     * Fetching distinct taxes used at line level, feetched in the set
                     * Also, fetched the information related to tax in different maps
                     */
                    double rowTaxPercentGST = 0.0;
                    if (row != null && row.getTax() != null) {
                        String taxCode = row.getTax().getTaxCode();
                        if (!lineLevelTaxesGST.contains(taxCode)) {
                            lineLevelTaxesGST.add(taxCode);
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, (goodsReceiptOrder.getInventoryJE() != null ) ? goodsReceiptOrder.getInventoryJE().getEntryDate() : goodsReceiptOrder.getOrderDate(), row.getTax().getID());
                            rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                            lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                            lineLevelTaxAmountGST.put(taxCode, 0.0);
                            lineLevelTaxBasicGST.put(taxCode, 0.0);
                            /*
                             * putting subtotal+tax
                             */
                            obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((amount-rowdiscountvalue), companyid) + authHandler.round(0.0, companyid)); 
                        }
                    }
                }               
                /*
                 * get custom line data
                 */
                Map<String, Object> variableMap = new HashMap<String, Object>();
                GoodsReceiptOrderDetailsCustomDate jeDetailCustom = (GoodsReceiptOrderDetailsCustomDate) row.getGoodsReceiptOrderDetailsCustomDate();
                replaceFieldMap = new HashMap<String, String>();
                if (jeDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, jeDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                /*Set All Line level Dimension & All LIne level Custom Field Values*/
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                
                JSONObject jObj = null;
                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                    if (jObj.has(Constants.isDisplayUOM) && jObj.get(Constants.isDisplayUOM) != null && (Boolean) jObj.get(Constants.isDisplayUOM) != false) {
                        obj = accProductObj.getProductDisplayUOM(row.getProduct(), receivedQuantity, row.getBaseuomrate(), true, obj);
                    }
                }
                jArr.put(obj);
            }
            detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
            detailsTableData.put("isDetailsTableData", true);
            jArr.put(detailsTableData);
            if (goodsReceiptOrder.isGstIncluded()) {
                subtotal = subtotal - totaltax;
            } 
            totalAmount = totalAmount - totalDiscount;
            if (!StringUtil.isNullOrEmpty(SOID)) {
                List grOrderTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.goodsreceiptordertermmap, SOID);
                if(grOrderTermMapList != null && !grOrderTermMapList.isEmpty()){
                    Iterator termItr = grOrderTermMapList.iterator();
                    while (termItr.hasNext()) {
                        Object[] obj = (Object[]) termItr.next();
                        /* 
                         * [0] : Sum of termamount  
                         * [1] : Sum of termamountinbase 
                         * [2] : Sum of termTaxamount 
                         * [3] : Sum of termTaxamountinbase 
                         * [4] : Sum of termamountexcludingtax 
                         * [5] : Sum of termamountexcludingtaxinbase
                         */            
                        if(obj[2] != null){
                            totaltax += (Double) obj[2];
                        }
                    }
                }
            }
            mainTax = goodsReceiptOrder.getTax();
            if (mainTax != null) { //Get tax percent
                requestParams.put("transactiondate", entryDate);
                requestParams.put("taxid", mainTax.getID());
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                mainTaxName = mainTax.getName();
                totalAmount = subtotal;
                totaltax += (taxPercent == 0 ? 0 : totalAmount * taxPercent / 100);//overall tax calculate
                // Get global level tax details for GST Summary
                allLineLevelTax = mainTax.getTaxCode();
                allLineLevelTaxAmount = String.valueOf(totaltax);
                allLineLevelTaxBasic = String.valueOf(subtotal);
            }
            
            totalAmount = subtotal + totaltax - totalDiscount;
            /*
             *calculation for all line level tax and total tax.
            */
            if(extraCompanyPreferences.isIsNewGST()) {
                if (!lineLevelTaxNames.isEmpty()) {
                    Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                    while (lineTax.hasNext()) {
                        Map.Entry tax = (Map.Entry) lineTax.next();
                        allLineLevelTax += tax.getKey();
                        allLineLevelTax += "!## ";
                        double taxamount = (double) tax.getValue();
                        allLineLevelTaxAmount += tax.getValue().toString();
                        TotalLineLevelTaxAmount += taxamount;
                        allLineLevelTaxAmount += "!## ";
                    }
                }
                if (!StringUtil.isNullOrEmpty(allLineLevelTax)) {
                    allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length() - 4);
                }
                if (!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)) {
                    allLineLevelTaxAmount = allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length() - 4);
                }
                /*Calculating total for line level tax */
                totaltax = TotalLineLevelTaxAmount;
            } else {
                /*
                 * Putting all line taxes and its information in summary data separated by !##
                 */
                for (String key : lineLevelTaxesGST) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount += lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            
            String term = "", termsname = "";
            double totalTermAmount = 0;
//            boolean isTaxTermMapped = false;
//            double lineleveltermTaxAmount = 0;
//            double termAmountBeforeTax = 0;
//            double termAmountAfterTax = 0;
            if (!StringUtil.isNullOrEmpty(SOID)) {
                HashMap<String, Object> requestParam = new HashMap();
                requestParam.put("goodsReceiptID", SOID);
                KwlReturnObject curres = null;
                Map<String, Object> taxListParams = new HashMap<String, Object>();
                taxListParams.put("companyid", companyid);
                boolean isApplyTaxToTerms=goodsReceiptOrder.isApplyTaxToTerms();

                HashMap<String, Object> filterrequestParams = new HashMap();
                filterrequestParams.put("taxid", goodsReceiptOrder.getTax()==null?"":goodsReceiptOrder.getTax().getID());

                /*
                 * Terms calculation
                 */
                curres = accGoodsReceiptobj.getGRTermMap(requestParam);
                List<GoodsReceiptOrderTermMap> termMap = curres.getEntityList();
                for (GoodsReceiptOrderTermMap invoiceTerMap : termMap) {
                    InvoiceTermsSales mt = invoiceTerMap.getTerm();
                    double termAmnt = 0;
                    if(goodsReceiptOrder.isGstIncluded()){
                        termAmnt = invoiceTerMap.getTermAmountExcludingTax();
                    }else{
                        termAmnt = invoiceTerMap.getTermamount();
                    }
                    filterrequestParams.put("term", invoiceTerMap.getTerm() == null ? "" : invoiceTerMap.getTerm().getId());
                    filterrequestParams.put("companyid", companyid);

//                    if (uniqueProductTaxList.size() > 0 && isApplyTaxToTerms) {
//                        for (String taxId : uniqueProductTaxList) {
//                            filterrequestParams.put("taxid", taxId);
//                            taxListParams.put("taxid", taxId);
//                            isTaxTermMapped = accTaxObj.isTermMappedwithTax(filterrequestParams);
//                            if (isTaxTermMapped) {
//                                KwlReturnObject taxListResult = accTaxObj.getTaxList(taxListParams);
//                                if (taxListResult != null && taxListResult.getEntityList() != null) {
//                                    List<TaxList> taxListPercent = taxListResult.getEntityList();
//                                    lineleveltermTaxAmount += (termAmnt * (taxListPercent.get(0).getPercent())) / 100;
//                                }
////                                    break;
//                            }
//                        }
//                    } else {
//                        isTaxTermMapped = accTaxObj.isTermMappedwithTax(filterrequestParams);
//                    }
//                    if(isTaxTermMapped){ // If term is mapped with tax
//                        termAmountBeforeTax += invoiceTerMap.getTermamount(); // term amount for adding before tax calculation
//                    } else{
//                        termAmountAfterTax += invoiceTerMap.getTermamount(); // term amount for adding after tax calculation
//                    }
                    String termName = (mt.getTerm() + (termAmnt > 0 ? "(+)" : "(-)"));
                    summaryData.put(mt.getTerm(), termAmnt);
                    totalTermAmount += termAmnt;
                    term += "<div> <table><tr><td>" + termName + " : </td><td>" + Double.toString(termAmnt) + "</td></tr></table></div><br>";
                    termsname += mt.getTerm() + " " + invoiceTerMap.getPercentage() + "%, ";
                    if (!StringUtil.isNullOrEmpty(String.valueOf(termAmnt))) {
                        String allTermsPlaceholder = CustomDesignerConstants.AllTermsKeyValuePair;
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsLabel, termName);
                        allTermsPlaceholder = allTermsPlaceholder.replace(CustomDesignerConstants.AllTermsValue, CustomDesignHandler.getAmountinCommaDecimal(termAmnt, amountdigitafterdecimal, countryid));
                        appendtermString.append(allTermsPlaceholder);
                    }
                }
                if (!StringUtil.isNullOrEmpty(termsname)) {
                    termsname = termsname.substring(0, termsname.length() - 2);
                }
                if (!StringUtil.isNullOrEmpty(term) && term.indexOf("<br>") != -1) {
                    term = term.substring(0, term.lastIndexOf("<br>"));
                }
                if (!StringUtil.isNullOrEmpty(appendtermString.toString())) {
                    allTerms = appendtermString.toString();
                }
            }
            Amountwithoutterm = totalAmount + totaltax;//Amount after adition of subtotal and total tax without invoice term
//            if(isTaxTermMapped){ // If tax mapped with any term
//                if (mainTax != null) {
//                    totaltax = (taxPercent == 0 ? 0 : ((subtotal - totalDiscount) + termAmountBeforeTax) * taxPercent /100); // first add term into subtotal then calculate tax
//                }
//                totaltax += lineleveltermTaxAmount;
//                totalAmount = subtotal - totalDiscount + termAmountBeforeTax + totaltax + termAmountAfterTax; // first add mapped terms amount in subtotal then add total tax then add unmapped terms amount for total amount
//            } else{
                totalAmount = totalAmount + totalTermAmount;//Total amount including the total terms.
//            }
            netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalAmount)), currency, countryLanguageId);
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(authHandler.formattedAmount(totalAmount, companyid))), indoCurrency);
            }
            gstAmountInWords = EnglishNumberToWordsOjb.convert(totaltax, currency, countryLanguageId);
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("vendorid", goodsReceiptOrder.getVendor().getID());
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
            addrRequestParams.put("isDefaultAddress", true);
            addrRequestParams.put("isBillingAddress", true);
            addrRequestParams.put("isSeparator", true);
            vendorbillAddr = accountingHandlerDAOobj.getVendorAddress(addrRequestParams);
            addrRequestParams.put("isBillingAddress", false);
            vendorshipAddr = accountingHandlerDAOobj.getVendorAddress(addrRequestParams);
            billAddr = CommonFunctions.getTotalBillingShippingAddress(goodsReceiptOrder.getBillingShippingAddresses(), true);
            shipAddr = CommonFunctions.getTotalBillingShippingAddress(goodsReceiptOrder.getBillingShippingAddresses(), false);
            vendortransactionalAddr = CommonFunctions.getTotalVendorTransactionalShippingAddress(goodsReceiptOrder.getBillingShippingAddresses(), true);
            /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            DateFormat df = authHandler.getUserDateFormatterJson(requestObj);//User Date Formatter
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_Goods_Receipt_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", goodsReceiptOrder.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            extraparams.put("approvestatuslevel", goodsReceiptOrder.getApprovestatuslevel());
            String userId = requestObj.optString(Constants.useridKey);
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            //Details like company details
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);

            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(authHandler.round(totalAmount,companyid), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, authHandler.formattedAmount(totalDiscount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltax, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, authHandler.formattedAmount((subtotal - totalDiscount), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount((subtotal+ totaltax), companyid)); //ERP-25162
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, taxPercent);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignVendorQuoteRefNumber_fieldTypeId, VQouteRef.equals("") ? "" : VQouteRef.substring(0, VQouteRef.length() - 2));
            summaryData.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, POref.equals("") ? "" : POref.substring(0, POref.length() - 2));
            summaryData.put(CustomDesignerConstants.CSUTOMDESIGNER_PO_REF_DATE,POrefdate.equals("")?"":POrefdate.substring(0, POrefdate.length()-2)); 
            summaryData.put(CustomDesignerConstants.CustomDesignVenInvRefNumber_fieldTypeId, Invref.equals("") ? "" : Invref.substring(0, Invref.length() - 2));
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term, goodsReceiptOrder.getVendor().getDebitTerm() != null ? (goodsReceiptOrder.getVendor().getDebitTerm().getTermdays() != 0 ? goodsReceiptOrder.getVendor().getDebitTerm().getTermdays() : 0) : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_Code, goodsReceiptOrder.getVendor().getAcccode() != null ? goodsReceiptOrder.getVendor().getAcccode() : "");
            summaryData.put(CustomDesignerConstants.BillTo, billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.Createdby, createdby);
            summaryData.put(CustomDesignerConstants.Updatedby, updatedby);
            summaryData.put(CustomDesignerConstants.VendorBillTo, vendorbillAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.VendorShipTo, vendorshipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);
            summaryData.put(CustomDesignerConstants.VendorTransactionalShipTo, vendortransactionalAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsName_fieldTypeId, termsname);
            summaryData.put(CustomDesignerConstants.TotalAmountWithoutTerm, Amountwithoutterm);
            summaryData.put(CustomDesignerConstants.CustomDesignSummaryTermsValue_fieldTypeId, totalTermAmount);
            summaryData.put(CustomDesignerConstants.AllTerms, allTerms);
            summaryData.put(CustomDesignerConstants.AMOUNT_BEFORE_TAX, authHandler.formattedAmount(((subtotal-totalDiscount) + totalTermAmount), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, goodsReceiptOrder.getTerm() != null ? goodsReceiptOrder.getTerm().getTermdays() : "");
            summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName())?user.getFirstName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName())?user.getLastName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName())?user.getFullName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID())?user.getEmailID():"");
            summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress())?user.getAddress().replaceAll(Constants.REGEX_LINE_BREAK, "<br>"):"");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber())?user.getContactNumber():"");
            summaryData.put(CustomDesignerConstants.VENDOR_TITLE, vendorTitle);
            summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, goodsReceiptOrder.getVendor().getVATTINnumber()!= null ? goodsReceiptOrder.getVendor().getVATTINnumber() : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, goodsReceiptOrder.getVendor().getCSTTINnumber()!= null ? goodsReceiptOrder.getVendor().getCSTTINnumber() : "");
            summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
            summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
            summaryData.put(CustomDesignerConstants.CustomDesignNetDebitTerm_fieldTypeId, netDebitTerm);
            summaryData.put(CustomDesignerConstants.CustomDesignNetVendorTerm_fieldTypeId, netVendorTerm);
            summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, goodsReceiptOrder.getVendor().getGSTIN() != null ? goodsReceiptOrder.getVendor().getGSTIN() : "");
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);
            summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords + Constants.ONLY);
            jArr.put(summaryData);

        } catch (Exception ex) {
            System.out.println(ex);
        }
        return jArr;
    }
    
    public JSONArray getPurchaseReturnDetailsItemJSON(JSONObject requestObj, String billids,int moduleid) throws SessionExpiredException, ServiceException, JSONException {
        JSONArray jArr = new JSONArray();
        try {
            String deditnotenumber = "";
            PdfTemplateConfig config = null;
            int quantitydigitafterdecimal = 2, amountdigitafterdecimal = 2, unitpricedigitafterdecimal = 2;
            String Accountcode = "", Vendorterms = "", Createdby = "", updatedby = "", vendorTitle = "";
            String GRORefNo= "",PORefNo= "",PIRefNo= "",GRORefDate="",PORefDate = "", PIRefDate = "";
            String billAddr = "", shipAddr = "",requestType="",globallevelcustomfields="",globalleveldimensions="";
            String reflinknumber = "", mainTaxName = "", salesPerson = "", purchaseReturnNumber = "";
            double subtotal = 0, total = 0;
            double totaltaxamount = 0, rowtaxamount = 0,taxPercent = 0, totaldiscount = 0;
            double totalQuantity = 0;
            boolean isLocationForProduct = false,isbatchforproduct=false,isserialforproduct=false,isWarehouseForProduct=false;
            String companyid = requestObj.optString(Constants.companyKey);
            Tax mainTax = null;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestObj);
            JSONObject summaryData = new JSONObject();
            
            PurchaseReturn pr = null;
            pr = (PurchaseReturn) kwlCommonTablesDAOObj.getClassObject(PurchaseReturn.class.getName(), billids);
            DebitNote deditNote = null;
            if (pr.isIsNoteAlso()) {
                KwlReturnObject deditnoteresult = accDebitNoteobj.getDebitNoteIdFromPRId(pr.getID(), companyid);
                if (!deditnoteresult.getEntityList().isEmpty()) {
                    deditNote = (DebitNote) deditnoteresult.getEntityList().get(0);
                    deditnotenumber = deditNote != null ? deditNote.getDebitNoteNumber(): "";
                }
            }
            
            double externalcurrency = pr.getExternalCurrencyRate();
            double revExchangeRate = 1.0;
            if (externalcurrency != 0) {
                revExchangeRate = 1 / externalcurrency;
            }
            
            //document currency
            if (pr != null && pr.getCurrency() != null && !StringUtil.isNullOrEmpty(pr.getCurrency().getCurrencyID())) {
                summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, pr.getCurrency().getCurrencyID());
            }
            /**
             * get customer title (Mr./Mrs.)
             */
            vendorTitle = pr.getVendor().getTitle();
            if(!StringUtil.isNullOrEmpty(vendorTitle)){
                KwlReturnObject masterItemResult = kwlCommonTablesDAOObj.getObject(MasterItem.class.getName(), vendorTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                vendorTitle = masterItem.getValue();
            }
            boolean isConsignment = requestObj.optBoolean("isConsignment", false);
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat df1 = authHandler.getDateOnlyFormat();
            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                }
            }
            //getting main company posttext
            KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, Constants.Acc_Purchase_Return_ModuleId);
            if (templateConfig.getEntityList().size() > 0) {
                config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
            }
            /*
             * get Line Item Data
             */
           
            Accountcode = pr.getVendor().getAcccode() != null ? pr.getVendor().getAcccode() : "";
            
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("companyid", companyid);
            addressParams.put("isDefaultAddress", true);    //always true to get defaultaddress
            addressParams.put("isBillingAddress", true);    //true to get billing address
            addressParams.put("vendorid", pr.getVendor().getID());
            VendorAddressDetails vendorAddressDetails = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
            Vendorterms = pr.getVendor().getDebitTerm()!= null ? (pr.getVendor().getDebitTerm().getTermname()) : "";
            if (pr.getCreatedby() != null) {
                Createdby = StringUtil.getFullName(pr.getCreatedby());
            }
            updatedby = pr.getModifiedby() != null ? pr.getModifiedby().getFullName() : "";
            
            KwlReturnObject resultcompany = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) resultcompany.getEntityList().get(0); 
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            /*
            * Declaration of Variables to fetch Line level taxes and its information like
            * Tax Code, Tax Amount etc.
            */ 
            String allLineLevelTax="", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "", lineLevelTaxSign = "";
            Set<String> lineLevelTaxesGST = new HashSet<String>();
            Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
            double TotalLineLevelTaxAmount = 0, subTotalWithDiscount = 0;
            Map<String,Double> lineLevelTaxNames = new LinkedHashMap<String,Double>();
            int countryid = 0;
            if( extraCompanyPreferences.getCompany()!= null && extraCompanyPreferences.getCompany().getCountry() != null ){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            order_by.add("srno");
            order_type.add("asc");
            invRequestParams.put("order_by", order_by);
            invRequestParams.put("order_type", order_type);
            KwlReturnObject idresult = null;
            Iterator itr = null;
            filter_names.add("purchaseReturn.ID");
            filter_params.add(pr.getID());
            invRequestParams.put("filter_names", filter_names);
            invRequestParams.put("filter_params", filter_params);
            idresult = accGoodsReceiptobj.getPurchaseReturnDetails(invRequestParams);
            itr = idresult.getEntityList().iterator();
            int i = 0;
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
            while (itr.hasNext()) {
                i++;
                JSONObject obj = new JSONObject();
                JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                String prodId = "";
                String prodName = "";
                String prodDesc = "";
                double Uprice = 0;
                double amount = 0;
                double returnquantity = 0, actualquantity = 0,rowamountwithouttax=0,rate = 0,rowdiscountvalue=0;
                String Uom = "", discountname = "";
                PurchaseReturnDetail row = (PurchaseReturnDetail) itr.next();
                prodId = row.getProduct().getProductid() != null ? row.getProduct().getProductid() : "";
                prodName = row.getProduct().getName() != null ? row.getProduct().getName() : "";
                actualquantity = row.getActualQuantity();
                returnquantity = row.getReturnQuantity();
                if (!StringUtil.isNullOrEmpty(row.getDescription())) {
                    prodDesc = row.getDescription();
                } else {
                    if (!StringUtil.isNullOrEmpty(row.getProduct().getDescription())) {
                        prodDesc = row.getProduct().getDescription();
                }
                }
                prodDesc = StringUtil.DecodeText(prodDesc);
                obj.put(CustomDesignerConstants.AdditionalDescription, !StringUtil.isNullOrEmpty(row.getProduct().getAdditionalDesc()) ? row.getProduct().getAdditionalDesc().replaceAll(Constants.REGEX_LINE_BREAK, "<br>") :"");  //product Addtional description ERP-39061
                /*To get the linking information upto 2-3 levels */
                if ((row.getGrdetails()!= null) && (!(row.getGrdetails().equals("undefined")))) {
                    if(row.getGrdetails().getGrOrder()!=null){
                        if(row.getGrdetails().getGrOrder().getGoodsReceiptOrderNumber()!=null){
                            reflinknumber = row.getGrdetails().getGrOrder().getGoodsReceiptOrderNumber()+ ",";
                            salesPerson = row.getGrdetails().getGrOrder().getMasterAgent()!=null?row.getGrdetails().getGrOrder().getMasterAgent().getValue():"";
                        }
                    }
                } else if (row.getVidetails() != null) {
                    if(row.getVidetails().getGoodsReceipt()!=null){
                        if(row.getVidetails().getGoodsReceipt().getGoodsReceiptNumber()!=null){
                            reflinknumber = row.getVidetails().getGoodsReceipt().getGoodsReceiptNumber()+ ",";
                            salesPerson = row.getVidetails().getGoodsReceipt().getMasterAgent()!=null?row.getVidetails().getGoodsReceipt().getMasterAgent().getValue():"";
                        }
                    }
                }

                purchaseReturnNumber = row.getPurchaseReturn() != null ? !StringUtil.isNullOrEmpty(row.getPurchaseReturn().getPurchaseReturnNumber()) ? row.getPurchaseReturn().getPurchaseReturnNumber() : "" : "";
                rate = row.getRate();
                rowamountwithouttax = rate * returnquantity;
                obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithouttax, companyid));//subtotal

                rowdiscountvalue = (row.getDiscountispercent() == 1) ? rowamountwithouttax * row.getDiscount() / 100 : row.getDiscount();

                if(row.getDiscountispercent() == 1){
                    discountname = CustomDesignHandler.getAmountinCommaDecimal(row.getDiscount(),0,countryid)+"%";//to return 0 no of zeros
                }else{
                    discountname = pr.getCurrency().getSymbol()+" "+authHandler.formattingDecimalForAmount(row.getDiscount(), companyid);//to show as it is in UI
                }
                obj.put(CustomDesignerConstants.Discountname, discountname);//Discount Name
                obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));//Discount
                totaldiscount += rowdiscountvalue;
                obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount((rowamountwithouttax - rowdiscountvalue), companyid));//Subtotal with Discount
                subTotalWithDiscount = authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid);
                
                KwlReturnObject productCategories = null;
                productCategories = accProductObj.getProductCategoryForDetailsReport(row.getProduct().getID());
                List productCategoryList = productCategories.getEntityList();
                String cateogry = "";
                Iterator catIte = productCategoryList.iterator();
                while (catIte.hasNext()) {
                    ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                    String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                    cateogry += categoryName + " ";
                } 
                if ( StringUtil.isNullOrEmpty(cateogry)) {
                    cateogry = "None";
                }
                obj.put("productCategory", cateogry);
                HashMap<String, Object> params = new HashMap<String, Object>();
                filter_names = new ArrayList();
                filter_params = new ArrayList();
                filter_names.add("companyid");
                filter_names.add("fieldtype");
                filter_names.add("customcolumn");
                filter_names.add("moduleid");
                filter_params.add(companyid);
                filter_params.add(4);
                filter_params.add(1);
                filter_params.add(requestObj.optInt("moduleid"));
                params.put("filter_names", filter_names);
                params.put("filter_values", filter_params);
                
                KwlReturnObject fieldparams =  accAccountDAOobj.getFieldParams(params);
                List fieldParamsList = fieldparams.getEntityList();
                
                JSONArray jsonarr = new JSONArray();
                for(int cnt=0; cnt < fieldParamsList.size(); cnt++){
                    FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                    JSONObject tempObj = new JSONObject();
                    tempObj.put("comboName", fieldParamsObj.getFieldname());
                    jsonarr.put(tempObj);
                }
                
                obj.put("groupingComboList", jsonarr);
                
                Uprice = row.getRate();
                amount = Uprice * returnquantity;
                subtotal += amount;
                rowtaxamount = row.getRowTaxAmount();
                totaltaxamount += row.getRowTaxAmount();
                Uom = row.getUom() == null ? "" : row.getUom().getNameEmptyforNA();
                totalQuantity+=returnquantity;
                obj.put(CustomDesignerConstants.SrNO, i);// Sr No
                obj.put(CustomDesignerConstants.ProductName, prodName);// productname
                obj.put(CustomDesignerConstants.IN_ProductCode, prodId);
                obj.put(CustomDesignerConstants.ProductDescription, prodDesc.replaceAll("\n", "<br>"));//Product Description
                obj.put(CustomDesignerConstants.IN_Tax, rowtaxamount);//Tax amount
                obj.put(CustomDesignerConstants.SR_ProductTax, (row.getTax() != null)?row.getTax().getName():"");//Product Tax Name
                obj.put(CustomDesignerConstants.HSCode, (row.getProduct().getHSCode()!=null) ? row.getProduct().getHSCode().replaceAll("\n", "<br>") : "" );//get HSCode of ProductGrid
                obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(Uprice, companyid));// Rate
                obj.put(CustomDesignerConstants.SR_ReturnQuantitywithUOM, authHandler.formattingDecimalForQuantity(returnquantity, companyid) + " " + Uom); // Quantity
                obj.put(CustomDesignerConstants.SR_ReturnQuantity, authHandler.formattingDecimalForQuantity(returnquantity, companyid));
                obj.put(CustomDesignerConstants.SR_ActualQuantitywithUOM, authHandler.formattingDecimalForQuantity(actualquantity, companyid) + " " + Uom); // Quantity
                obj.put(CustomDesignerConstants.SR_ActualQuantity, authHandler.formattingDecimalForQuantity(actualquantity, companyid));
                obj.put(CustomDesignerConstants.SR_Remark, row.getRemark() == null ? "" : row.getRemark());//get Remarks of Product Grid
                obj.put(CustomDesignerConstants.SR_SerialNumber, (row.getPartno() != null) ? row.getPartno() : "");//getSerialnumber of ProductGrid
                obj.put(CustomDesignerConstants.SR_Reason, (row.getReason() != null) ? row.getReason().getValue() : "");//getReason of ProductGrid
                obj.put(CustomDesignerConstants.IN_UOM, Uom);
                obj.put(CustomDesignerConstants.IN_Currency, pr.getCurrency().getCurrencyCode());
                obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(amount, companyid));
                obj.put("currencysymbol",pr.getCurrency().getSymbol());
                obj.put("currencycode",pr.getCurrency().getCurrencyCode());
                
                String baseUrl = URLUtil.getDomainURL(requestObj.optString("cdomain"), false);
                /*
                 * Following code is to check whether the image is predent for product or not. 
                 * If Image is not present sent s.gif instead of product id
                 */
                String fileName = null;
                fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages+row.getProduct().getID() + ".png";
                File file = new File(fileName);
                FileInputStream in = null; 
                String filePathString = "";
                try {
                    in = new FileInputStream(file);
                } catch(java.io.FileNotFoundException ex) {
                    //catched exception if file not found, and to continue for rest of the products
                   filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                if(in != null){
                    filePathString = baseUrl + "productimage?fname=" + row.getProduct().getID() + ".png&isDocumentDesignerPrint=true";
                } else{
                    filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                }
                obj.put(CustomDesignerConstants.imageTag,filePathString);
                //Calculating Batch & Serial Number of Product                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(row.getProduct().getID())) {
                    KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), row.getProduct().getID());
                    Product product = (Product) prodresult.getEntityList().get(0);
                    isLocationForProduct = product.isIslocationforproduct();
                    isbatchforproduct = product.isIsBatchForProduct();
                    isserialforproduct = product.isIsSerialForProduct();
                    isWarehouseForProduct = product.isIswarehouseforproduct();

                }
                if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory()
                        || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory()
                        || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {
                    if (isbatchforproduct || isserialforproduct || isLocationForProduct || isWarehouseForProduct) {  //product level batch and serial no on or not but now only location is checked
                        String batchdetails = accInvoiceServiceDAO.getNewBatchJson(row.getProduct(), requestObj, row.getID());
                        JSONArray locjArr = new JSONArray(batchdetails);
                        String location = "";
                        String locationName = "", batchname = "", warehouse = "", serialnumber = "",batchexpdate = "",batchmfgdate = "";
                        String locationnamenew = "", batchnamenew = "", warehousenew = "", serialnumbernew = "",batchexpdatenew= "",batchesmfgdatenew= "";
                        Set<String> batchnames = new LinkedHashSet();
                        Set<String> serialnumbers = new LinkedHashSet();
                        Set<String> locationnames = new LinkedHashSet();
                        Set<String> warehouses = new LinkedHashSet();
                        Set<String> batchesexpirydate = new LinkedHashSet();
                        Set<String> batchesmfgdate = new LinkedHashSet();
                        df = authHandler.getUserDateFormatterWithoutTimeZone(requestObj);//User Date Formatter
                        Date date = null;
                        
                        for (int count = 0; count < locjArr.length(); count++) {
                            JSONObject jSONObject = new JSONObject(locjArr.get(count).toString());
                            location = jSONObject.optString("location", "");
                            batchname = jSONObject.optString("batchname", "");
                            warehouse = jSONObject.optString("warehouse", "");
                            serialnumber = jSONObject.optString("serialno", "");
                            batchmfgdate = jSONObject.optString("mfgdate", "");
                            batchexpdate = jSONObject.optString("expdate", "");
                            serialnumbers.add(serialnumber);
                            batchnames.add(batchname);
                            warehouses.add(warehouse);

                            if (!StringUtil.isNullOrEmpty(location)) {
                                KwlReturnObject loc = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), location);
                                InventoryLocation localist = (InventoryLocation) loc.getEntityList().get(0);
                                locationName = localist.getName();
                                locationnames.add(locationName);
//                                locationName = locationName.concat(",");
                            }

//                            if (!StringUtil.isNullOrEmpty(batchname)) {
//                                batchname = batchname.concat(",");
//                            }
//                            if (!StringUtil.isNullOrEmpty(serialnumber)) {
//                                serialnumber = serialnumber.concat(",");
//                            }
                            if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
                                date = df1.parse(batchmfgdate);
                                batchmfgdate = df.format(date);
                                if (batchesmfgdate.contains(batchmfgdate)) {
                                    batchesmfgdate.add(" ");
                                } else {
                                    batchesmfgdate.add(batchmfgdate);
                                }
                            } else {
                                batchesmfgdate.add(" ");
                            }
                            if (!StringUtil.isNullOrEmpty(batchexpdate)) {
                                date = df1.parse(batchexpdate);
                                batchexpdate = df.format(date);
                                if (batchesexpirydate.contains(batchexpdate)) {
                                     batchesexpirydate.add(" ");
                                } else {
                                    batchesexpirydate.add(batchexpdate);
                                }
                            } else {
                                batchesexpirydate.add(" ");
                            }
                        }

                        for (String str : batchnames) {
                            String bno = "";
                            bno = str;
                            if(!StringUtil.isNullOrEmpty(bno)){
                                batchnamenew += bno.concat("!##");
                            }
                        }

                        for (String str : serialnumbers) {
                            String sno = "";
                            sno = str;
                            if(!StringUtil.isNullOrEmpty(sno)){
                                serialnumbernew += sno.concat("!##");
                            }
                        }
                        for (String str : locationnames) {
                            String lno = "";
                            lno = str;
                            if(!StringUtil.isNullOrEmpty(lno)){
                                locationnamenew += lno.concat("!##");
                            }
                        }
                        for (String str : batchesexpirydate) {
                            String bexp = "";
                            bexp = str;
                            if(!StringUtil.isNullOrEmpty(bexp)){
                                batchexpdatenew += bexp.concat("!##");
                            }
                        }
                        for (String str : batchesmfgdate) {
                            String bmfg = "";
                            bmfg = str;
                            if (!StringUtil.isNullOrEmpty(bmfg) && !bmfg.equals(" ")) {
                                batchesmfgdatenew += bmfg.concat("!##");
                            }
                        }


                        if (!StringUtil.isNullOrEmpty(locationnamenew)) {
                            locationnamenew = locationnamenew.substring(0, locationnamenew.length() - 3);
                        }

                        if (!StringUtil.isNullOrEmpty(serialnumbernew)) {
                            serialnumbernew = serialnumbernew.substring(0, serialnumbernew.length() - 3);
                        }

                        if (!StringUtil.isNullOrEmpty(batchnamenew)) {
                            batchnamenew = batchnamenew.substring(0, batchnamenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(batchesmfgdatenew)) {
                            batchesmfgdatenew = batchesmfgdatenew.substring(0, batchesmfgdatenew.length() - 3);
                        }
                        if (!StringUtil.isNullOrEmpty(batchexpdatenew)) {
                            batchexpdatenew = batchexpdatenew.substring(0, batchexpdatenew.length() - 3);
                        }

                        obj.put(CustomDesignerConstants.SerialNumber, serialnumbernew);
                        obj.put(CustomDesignerConstants.BatchNumber, batchnamenew);
                        obj.put(CustomDesignerConstants.IN_Loc, locationnamenew);
                        obj.put(CustomDesignerConstants.ManufacturingDate_Batch, batchesmfgdatenew);// Batch Manufacturing Date
                        obj.put(CustomDesignerConstants.BatchNumberExp, batchexpdatenew);
                        obj.put(CustomDesignerConstants.SR_SerialNumber, serialnumbernew);

                    } else { //if not activated location for product level then take default location for product.
                        obj.put(CustomDesignerConstants.IN_Loc, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                    }
                } else { //if not activated location for product level then take default location for product.
                    obj.put(CustomDesignerConstants.IN_Loc, row.getProduct().getLocation() == null ? "" : row.getProduct().getLocation().getName());
                }
                
                if (row.getGrdetails() != null) {
                    if(GRORefNo.indexOf(row.getGrdetails().getGrOrder().getGoodsReceiptOrderNumber())==-1){
                        GRORefNo += row.getGrdetails().getGrOrder().getGoodsReceiptOrderNumber()+", ";
                        GRORefDate += row.getGrdetails().getGrOrder().getOrderDate() + ", ";
                    }
                    if (row.getGrdetails().getPodetails() != null) {
                        if(PORefNo.indexOf(row.getGrdetails().getPodetails().getPurchaseOrder().getPurchaseOrderNumber())==-1){
                            PORefNo += row.getGrdetails().getPodetails().getPurchaseOrder().getPurchaseOrderNumber()+", ";
                            PORefDate += row.getGrdetails().getPodetails().getPurchaseOrder().getOrderDate()+", ";
                        }
                    }
                    if (row.getGrdetails().getVidetails() != null) {
                        if(PIRefNo.indexOf(row.getGrdetails().getVidetails().getGoodsReceipt().getGoodsReceiptNumber())==-1){
                            PIRefNo += row.getGrdetails().getVidetails().getGoodsReceipt().getGoodsReceiptNumber()+", ";
//                            PIRefDate += row.getGrdetails().getVidetails().getGoodsReceipt().getJournalEntry().getEntryDate()+", ";
                            PIRefDate += row.getGrdetails().getVidetails().getGoodsReceipt().getCreationDate()+", ";
                        }
                        if (row.getGrdetails().getVidetails().getPurchaseorderdetail() != null) {
                            if(PORefNo.indexOf(row.getGrdetails().getVidetails().getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber())==-1){
                                PORefNo += row.getGrdetails().getVidetails().getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber()+", ";
                                PORefDate += row.getGrdetails().getVidetails().getPurchaseorderdetail().getPurchaseOrder().getOrderDate()+", ";
                            }
                        }
                    }
                } else if (row.getVidetails() != null) {
                    if(PIRefNo.indexOf(row.getVidetails().getGoodsReceipt().getGoodsReceiptNumber())==-1){
                        PIRefNo += row.getVidetails().getGoodsReceipt().getGoodsReceiptNumber()+", ";
//                        PIRefDate += row.getVidetails().getGoodsReceipt().getJournalEntry().getEntryDate()+", ";
                        PIRefDate += row.getVidetails().getGoodsReceipt().getCreationDate()+", ";
                    }
                    if (row.getVidetails().getPurchaseorderdetail()!=null) {
                        if(PORefNo.indexOf(row.getVidetails().getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber())==-1){
                            PORefNo += row.getVidetails().getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber()+", ";
                            PORefDate += row.getVidetails().getPurchaseorderdetail().getPurchaseOrder().getOrderDate()+", ";
                        }
                    }
                    if (row.getVidetails().getGoodsReceiptOrderDetails() != null) {
                        if(GRORefNo.indexOf(row.getVidetails().getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber())==-1){
                            GRORefNo += row.getVidetails().getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber()+", ";
                            GRORefDate += row.getVidetails().getGoodsReceiptOrderDetails().getGrOrder().getOrderDate()+", ";
                        }
                        if (row.getVidetails().getGoodsReceiptOrderDetails().getPodetails() != null) {
                            if(PORefNo.indexOf(row.getVidetails().getGoodsReceiptOrderDetails().getPodetails().getPurchaseOrder().getPurchaseOrderNumber())==-1){
                                PORefNo += row.getVidetails().getGoodsReceiptOrderDetails().getPodetails().getPurchaseOrder().getPurchaseOrderNumber()+", ";
                                PORefDate += row.getVidetails().getGoodsReceiptOrderDetails().getPodetails().getPurchaseOrder().getOrderDate()+", ";
                            }
                        }
                    }
                } 
                
                String lineLevelTax = "";
                String lineLevelTaxPercent = "";
                String lineLevelTaxAmount = "";
                double lineLevelTaxAmountTotal= 0;
                /*
                 * Check for new GST 
                 */
                if(extraCompanyPreferences.isIsNewGST()){
                    HashMap<String, Object> PurchaseReturnDetailParams = new HashMap<String, Object>();
                    PurchaseReturnDetailParams.put("PurchaseReturnDetailid", row.getID());
                    PurchaseReturnDetailParams.put("orderbyadditionaltax", true);
                    // GST
                    PurchaseReturnDetailParams.put("termtype", 7);
                    KwlReturnObject grdTermMapresult = accGoodsReceiptobj.getPurchaseReturnDetailTermMap(PurchaseReturnDetailParams);
                    List<PurchaseReturnDetailsTermMap> gst = grdTermMapresult.getEntityList();
                    obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                    obj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.TAXABLE_VALUE, subTotalWithDiscount);
                    
                    for (PurchaseReturnDetailsTermMap purchasereturndetailTermMap : gst) {
                        LineLevelTerms mt = purchasereturndetailTermMap.getTerm();
                        if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                            obj.put(CustomDesignerConstants.CGSTPERCENT, purchasereturndetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CGSTAMOUNT, purchasereturndetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, purchasereturndetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, purchasereturndetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                            obj.put(CustomDesignerConstants.IGSTPERCENT, purchasereturndetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.IGSTAMOUNT, purchasereturndetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, purchasereturndetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, purchasereturndetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                            obj.put(CustomDesignerConstants.SGSTPERCENT, purchasereturndetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.SGSTAMOUNT, purchasereturndetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, purchasereturndetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, purchasereturndetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                            obj.put(CustomDesignerConstants.UTGSTPERCENT, purchasereturndetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.UTGSTAMOUNT, purchasereturndetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, purchasereturndetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, purchasereturndetailTermMap.getTermamount());
                        } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                            obj.put(CustomDesignerConstants.CESSPERCENT, purchasereturndetailTermMap.getPercentage());
                            obj.put(CustomDesignerConstants.CESSAMOUNT, purchasereturndetailTermMap.getTermamount());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, purchasereturndetailTermMap.getPercentage());
                            gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, purchasereturndetailTermMap.getTermamount());
                        }
                        /*
                         * Tax Amount is not prining for indian Comapany ERP-37440
                         */
                        rowtaxamount += purchasereturndetailTermMap.getTermamount(); 
                        lineLevelTax += mt.getTerm();
                        lineLevelTax += "!## ";
                        lineLevelTaxPercent += authHandler.formattingDecimalForAmount(purchasereturndetailTermMap.getPercentage(), companyid);
                        lineLevelTaxPercent += "!## ";
                        lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(purchasereturndetailTermMap.getTermamount(), amountdigitafterdecimal,countryid);
                        lineLevelTaxAmount += "!## ";
                        /*
                         * calculating total of line level taxes
                         */
                        lineLevelTaxAmountTotal += purchasereturndetailTermMap.getTermamount();
                        if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                            double value = lineLevelTaxNames.get(mt.getTerm());
                            lineLevelTaxNames.put(mt.getTerm(), purchasereturndetailTermMap.getTermamount() + value);
                        } else {
                            lineLevelTaxNames.put(mt.getTerm(), purchasereturndetailTermMap.getTermamount());
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                        lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                        lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                        lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                    }
                    /*
                     * Tax Amount is not prining for indian Comapany ERP-37440
                     */
                    obj.put(CustomDesignerConstants.IN_Tax, rowtaxamount); 
                    obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                    obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                    obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                    /*
                     * putting subtotal+tax
                     */
                    obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));       
                    //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                    ExportRecordHandler.setHsnSacProductDimensionField(row.getProduct(), obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, obj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                    gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                } else {
                    /*
                    * Fetching distinct taxes used at line level, feetched in the set
                    * Also, fetched the information related to tax in different maps
                    */
                    boolean isRowTaxApplicable = false;
                    double rowTaxPercentGST = 0.0;
                    if (row.getTax() != null) {
                        String taxCode = row.getTax().getTaxCode();
                        if (!lineLevelTaxesGST.contains(taxCode)) {
                            lineLevelTaxesGST.add(taxCode);
                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, pr.getOrderDate(), row.getTax().getID());
                            rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                            lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                            lineLevelTaxAmountGST.put(taxCode, 0.0);
                            lineLevelTaxBasicGST.put(taxCode, 0.0);
                        }
                        lineLevelTaxAmountGST.put(taxCode,(Double) lineLevelTaxAmountGST.get(taxCode) + row.getRowTaxAmount());
                        lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL,0.0));
                        /*
                         * putting subtotal+tax
                         */
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax-rowdiscountvalue), companyid) + authHandler.round(row.getRowTaxAmount(), companyid)); 
                    }
                }
                
//                        /*
//                         * get custom line data
//                         */
                /**
                 * change module id for asset module
                 */
                boolean isFixedAsset = requestObj.optBoolean(Constants.isFixedAsset, false);
                if(isFixedAsset){
                    moduleid = Constants.Acc_FixedAssets_Purchase_Return_ModuleId;
                }
                HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> fieldrequestParamsProduct = new HashMap();
                fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));
                HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, replaceFieldMap);
                /*
                 * Dimensions----Customcolumn=1-lineitem;Customfield=0=Dimension
                 */
                fieldrequestParamsProduct.clear();
                HashMap<String, String> dimensionFieldMap = new HashMap<String, String>();
                fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 0));
                HashMap<String, Integer> DimensionFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, dimensionFieldMap);
                
                fieldrequestParamsProduct.clear();
                HashMap<String, String> customfieldFieldMap = new HashMap<String, String>();
                fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn, Constants.customfield));
                fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1, 1));
                HashMap<String, Integer> LineLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, customfieldFieldMap);
                /**
                 * reset module id
                 */
                if(isFixedAsset){
                    moduleid = Constants.Acc_Purchase_Return_ModuleId;
                }
                //For product custom field
                fieldrequestParamsProduct.clear();
                HashMap<String, String> productCustomfieldFieldMap = new HashMap<String, String>();
                fieldrequestParamsProduct.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParamsProduct.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                HashMap<String, Integer> ProductLevelCustomFieldMap = accAccountDAOobj.getFieldParamsMap(fieldrequestParamsProduct, productCustomfieldFieldMap);
                
                PurchaseReturnDetailCustomDate prDetailCustom = (PurchaseReturnDetailCustomDate) row.getPurchaseReturnDetailCustomDate();
                replaceFieldMap = new HashMap<String, String>();
                if (prDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, prDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
              
                PurchaseReturnDetailProductCustomData prProductDetailCustom = (PurchaseReturnDetailProductCustomData) row.getPurchaseReturnDetailProductCustomData();
                if (prProductDetailCustom != null) {
                    ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestObj, prProductDetailCustom, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
                }
                /*
                 * Set Dimension Values & Custom Fields 
                 */
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
                obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                
                JSONObject jObj = null;
                if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                    jObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                    if (jObj.has(Constants.isDisplayUOM) && jObj.get(Constants.isDisplayUOM) != null && (Boolean) jObj.get(Constants.isDisplayUOM) != false) {
                        obj = accProductObj.getProductDisplayUOM(row.getProduct(), returnquantity, row.getBaseuomrate(), true, obj);
                    }
                }
                jArr.put(obj);
            }
            detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
            detailsTableData.put("isDetailsTableData", true);
            jArr.put(detailsTableData);
            if (!StringUtil.isNullOrEmpty(reflinknumber)) {
                reflinknumber = reflinknumber.substring(0, reflinknumber.length() - 1);
            }
            mainTax = pr.getTax();
            Date entryDate = pr.getOrderDate();
            if (mainTax != null) { //Get Overall Tax percent && total tax amount
                requestParams.put("transactiondate", entryDate);
                requestParams.put("taxid", mainTax.getID());
                KwlReturnObject result = accTaxObj.getTax(requestParams);
                List taxList = result.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                mainTaxName = mainTax.getName();
                totaltaxamount += (taxPercent == 0 ? 0 : (subtotal-totaldiscount) * taxPercent / 100);//overall tax calculate
            }
           // total = subtotal - totaldiscount + totaltaxamount;
            total = pr.getTotalamount();
            String netinword = EnglishNumberToWordsOjb.convert(total, pr.getCurrency(),countryLanguageId) + " Only.";
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if(countryid == Constants.INDONESIAN_COUNTRY_ID){
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(total)), indoCurrency);
            }
            
                        /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            df = authHandler.getUserDateFormatterJson(requestObj);//User Date Formatter
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_Purchase_Return_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", pr.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }
            /*
             * Check for Indian and USA tax calculation
             */
            if(extraCompanyPreferences.isIsNewGST()){
                    if (!lineLevelTaxNames.isEmpty()) {
                        Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                        while (lineTax.hasNext()) {
                            Map.Entry tax = (Map.Entry) lineTax.next();
                            allLineLevelTax += tax.getKey();
                            allLineLevelTax += "!## ";
                            double taxamount = (double) tax.getValue();
                            allLineLevelTaxAmount += tax.getValue().toString();
                            TotalLineLevelTaxAmount += taxamount;
                            allLineLevelTaxAmount += "!## ";
                            if (taxamount > 0) {
                                lineLevelTaxSign += "+";
                                lineLevelTaxSign += "!## ";
                            } else {
                                lineLevelTaxSign += "-&nbsp;";
                                lineLevelTaxSign += "!## ";
                            }
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTax)) {
                        allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)) {
                        allLineLevelTaxAmount = allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length() - 4);
                    }
                    if (!StringUtil.isNullOrEmpty(lineLevelTaxSign)) {
                        lineLevelTaxSign = lineLevelTaxSign.substring(0, lineLevelTaxSign.length() - 4);
                    }
                //Add in Total Tax amount.
                totaltaxamount = totaltaxamount + TotalLineLevelTaxAmount;
            } else {
                /*
                * Putting all line taxes and its information in summary data separated by !##
                */
                for ( String key : lineLevelTaxesGST ) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount +=  lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            
            String gstAmountInWords = EnglishNumberToWordsOjb.convert(totaltaxamount, pr.getCurrency(),countryLanguageId) + Constants.ONLY;
            String userId = requestObj.optString(Constants.useridKey);
            KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userresult.getEntityList().get(0);
            //Details like company details
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
            if (!StringUtil.isNullOrEmpty(GRORefNo)) {//removing comma
                GRORefNo = GRORefNo.substring(0, GRORefNo.length() - 2);
            } 
            if (!StringUtil.isNullOrEmpty(PORefNo)) {
                PORefNo = PORefNo.substring(0, PORefNo.length() - 2);
            }
            if (!StringUtil.isNullOrEmpty(PIRefNo)) {
                PIRefNo = PIRefNo.substring(0, PIRefNo.length() - 2);
            }
            if (!StringUtil.isNullOrEmpty(GRORefDate)) {//removing comma
                GRORefDate = GRORefDate.substring(0, GRORefDate.length() - 2);
            } 
            if (!StringUtil.isNullOrEmpty(PORefDate)) {
                PORefDate = PORefDate.substring(0, PORefDate.length() - 2);
            }
            if (!StringUtil.isNullOrEmpty(PIRefDate)) {
                PIRefDate = PIRefDate.substring(0, PIRefDate.length() - 2);
            }
            summaryData.put("summarydata", true);
            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
            addrRequestParams.put("vendorid", pr.getVendor().getID());
            addrRequestParams.put("companyid", companyid);
            KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
            List<AddressDetails> addressResultList = addressResult.getEntityList();
            CommonFunctions.getAddressSummaryData(addressResultList, summaryData,companyAccountPreferences, extraCompanyPreferences);
            addrRequestParams.put("isDefaultAddress", true);
            addrRequestParams.put("isBillingAddress", true);
            addrRequestParams.put("isSeparator", true);
            billAddr = accountingHandlerDAOobj.getVendorAddress(addrRequestParams);
            addrRequestParams.put("isBillingAddress", false);
            shipAddr = accountingHandlerDAOobj.getVendorAddress(addrRequestParams);
            String systemcurrencysymbol = pr.getVendor().getCurrency().getSymbol();
            String customcurrencysymbol = accCommonTablesDAO.getCustomCurrencySymbol(systemcurrencysymbol, companyid);//Take custom currency symbol
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, authHandler.formattedAmount(total, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTermsDescription_fieldTypeId, Vendorterms);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltaxamount, companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, authHandler.formattedAmount((subtotal-totaldiscount), companyid));
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, authHandler.formattedAmount(((subtotal - totaldiscount) + totaltaxamount), companyid)); //ERP-25162
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, taxPercent);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword);
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, pr.getPostText() == null ? "" : pr.getPostText());
            summaryData.put(CustomDesignerConstants.CustomerVendor_Term, Vendorterms);
            summaryData.put(CustomDesignerConstants.CustomerVendor_Code, Accountcode);
            summaryData.put(CustomDesignerConstants.CustomDesignDN_VendorInvoiceNo, deditnotenumber);
            summaryData.put(CustomDesignerConstants.SR_LinkTo,reflinknumber);
            summaryData.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, customcurrencysymbol);
            summaryData.put(CustomDesignerConstants.BillTo, billAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr.replaceAll(Constants.REGEX_LINE_BREAK, "<br>"));
            summaryData.put(CustomDesignerConstants.AppendRequestType,requestType);
            summaryData.put(CustomDesignerConstants.Createdby,Createdby);
            summaryData.put(CustomDesignerConstants.Updatedby, updatedby);
            summaryData.put(CustomDesignerConstants.TotalQuantity, totalQuantity);
            summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, authHandler.formattedAmount(totaldiscount, companyid));
            summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode, Accountcode);
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.LinkedSalesPerson, salesPerson);
            summaryData.put(CustomDesignerConstants.PurchaseReturnNumber, purchaseReturnNumber);
            summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName())?user.getFirstName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName())?user.getLastName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName())?user.getFullName():"");
            summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID())?user.getEmailID():"");
            summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress())?user.getAddress().replaceAll(Constants.REGEX_LINE_BREAK, "<br>"):"");
            summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber())?user.getContactNumber():"");
            summaryData.put(CustomDesignerConstants.VENDOR_TITLE, vendorTitle);
//            if(countryid == Constants.indian_country_id) {
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTermSigns, lineLevelTaxSign);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);
            summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, pr.getVendor().getVATTINnumber()!= null ? pr.getVendor().getVATTINnumber() : "");
            summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, pr.getVendor().getCSTTINnumber()!= null ? pr.getVendor().getCSTTINnumber() : "");
            summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
            summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
            summaryData.put(CustomDesignerConstants.CompanyPANNumber, extraCompanyPreferences.getPanNumber()!= null ? extraCompanyPreferences.getPanNumber() : "");
            summaryData.put(CustomDesignerConstants.CUSTOMER_PAN_NO, pr.getVendor().getPANnumber()!= null ? pr.getVendor().getPANnumber() : "");
            summaryData.put(CustomDesignerConstants.TAXNAME, mainTaxName);
            summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, pr.getVendor().getGSTIN() != null ? pr.getVendor().getGSTIN() : "");
            summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignPORefNumber_fieldTypeId, PORefNo);
            summaryData.put(CustomDesignerConstants.CSUTOMDESIGNER_PO_REF_DATE, PORefDate);
            summaryData.put(CustomDesignerConstants.CustomDesignVenInvRefNumber_fieldTypeId, PIRefNo);
            summaryData.put(CustomDesignerConstants.CustomDesignVenInvRefDate_fieldTypeId, PIRefDate);
            summaryData.put(CustomDesignerConstants.CustomDesignGRORefNumber_fieldTypeId, GRORefNo);
            summaryData.put(CustomDesignerConstants.CustomDesignGRORefDate_fieldTypeId, GRORefDate);
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);

            jArr.put(summaryData);
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    /*
     * Function to fetch product details for Undercharged DN
     */
    public JSONArray getUnderchargeDNDetailsItemJSON(JSONObject requestJobj, Map<String, Object> summaryParams ) throws ServiceException {
        JSONArray jArr = new JSONArray();

        int rowcnt = 0;
        double totaltax = 0, subtotal = 0, subTotalWithDiscount = 0,totalDiscount=0;
        int amountdigitafterdecimal = 2;
        boolean isLocationForProduct = false, isbatchforproduct = false, isserialforproduct = false, isWarehouseForProduct = false, isPartialInvoice = false;

        Map<String, Double> lineLevelTaxNames = new LinkedHashMap<String, Double>();
        Set<String> lineLevelTaxesGST = new HashSet<String>();
        Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
        Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
        Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();


        try {
            DebitNote dn = (DebitNote) summaryParams.get("debitNote");

            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestJobj);

            Set<DebitNoteAgainstCustomerGst> rowsGst = dn.getRowsGst();
            InvoiceDetail row = null;

            DateFormat df1 = authHandler.getDateOnlyFormat();
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(requestJobj);//User Date Formatter

            CompanyAccountPreferences preference = (CompanyAccountPreferences) summaryParams.get("companyAccountPreferences");
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) summaryParams.get("extraCompanyPreferences");

            String companyid = requestJobj.optString(Constants.companyKey);
            List<PricingBandMasterDetail> list = CommonFunctions.getRRPFieldsForAllModulesLineItem(requestJobj, accountingHandlerDAOobj, accMasterItemsDAOobj);


            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
            }

            int countryid = 0;
            if (extraCompanyPreferences.getCompany() != null && extraCompanyPreferences.getCompany().getCountry() != null) {
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }

            String invoiceNos = "", invAmounts = "", invTaxAmounts = "", invAmountDues = "", invoicedates = "", invoiceSalesPerson = "";
            double invoicesubtotal = 0, invoicetotaltax = 0, invoicetotalamount = 0;

            // List to identify processed invoices while calculating summary data for CN
            List<String> processedInv = new ArrayList<String>();
            for (DebitNoteAgainstCustomerGst detail : rowsGst) {
                if (detail.getCidetails() != null) {

                    row = detail.getCidetails();

                    rowcnt++;
                    /*
                     * Fetching Job Work Order Qty, Number and Date.
                     */
                    Inventory inv = row.getInventory();
                    Product prod = inv.getProduct();
                    Invoice invoice = row.getInvoice();

                    double externalCurrencyRate = invoice.getExternalCurrencyRate(), revExchangeRate = 0;

                    if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                        externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                    }

                    if (externalCurrencyRate != 0) {
                        revExchangeRate = 1 / externalCurrencyRate;
                    }


                    String uom = row.getInventory().getUom() == null ? (row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getInventory().getUom().getNameEmptyforNA();
                    String challanDate = "";
                    JSONObject obj = new JSONObject();
                    Discount disc = row.getDiscount();
                    String rowTaxName = "", proddesc = "", discountname = "0";
                    double rate = 0, rowTaxPercent = 0, rowTaxAmt = 0, rowamountwithtax = 0, rowdiscountvalue = 0, rowamountwithouttax = 0;
                    double exchangerateunitprice = 0, exchangeratelineitemsubtotal = 0, exchangeratelineitemdiscount = 0, exchangeratelineitemamount = 0, exchangeratelineitemtax = 0, exchangeratelineitemsubtotalwithdiscount = 0;
                    isPartialInvoice = invoice.isPartialinv();

                    proddesc = StringUtil.isNullOrEmpty(row.getDescription()) ? (StringUtil.isNullOrEmpty(inv.getProduct().getDescription()) ? "" : inv.getProduct().getDescription()) : row.getDescription();
                    proddesc = StringUtil.DecodeText(proddesc);
                    obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr No
                    obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                    obj.put(CustomDesignerConstants.IN_ProductCode, prod.getProductid() == null ? "" : prod.getProductid());
                    obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>"));//Product Description
                    obj.put(CustomDesignerConstants.HSCode, (prod.getHSCode() != null) ? prod.getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid
                    obj.put(CustomDesignerConstants.ProductBarcode, prod.getBarcode() == null ? "" : prod.getBarcode());//Product Bar Code
                    obj.put(CustomDesignerConstants.AdditionalDescription, prod.getAdditionalDesc() != null ? prod.getAdditionalDesc().replaceAll("\n", "<br>") : "");  //product Addtional description
                    //used to get the line level total amount
                    for (PricingBandMasterDetail pricingBandMasterDetailObj : list) {
                        if (pricingBandMasterDetailObj.getProduct().equals(prod.getID())) {
                            obj.put(CustomDesignerConstants.RRP, authHandler.formattingDecimalForAmount(pricingBandMasterDetailObj.getSalesPrice(), companyid));//RRP
                        }
                    }
                    KwlReturnObject productCategories = null;
                    productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                    List productCategoryList = productCategories.getEntityList();
                    String cateogry = "";
                    Iterator catIte = productCategoryList.iterator();
                    while (catIte.hasNext()) {
                        ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                        String categoryName = pcm.getProductCategory() != null ? (!StringUtil.isNullOrEmpty(pcm.getProductCategory().getValue().toString()) ? pcm.getProductCategory().getValue().toString() : "") : "";
                        cateogry += categoryName + " ";
                    }
                    if (StringUtil.isNullOrEmpty(cateogry)) {
                        cateogry = "None";
                    }
                    obj.put("productCategory", cateogry);


                    double quantity = detail.getReturnQuantity();
                    rate = detail.getRate();
                    rate = authHandler.roundUnitPrice(rate, companyid);
                    rowamountwithouttax = rate * quantity;

                    if (isPartialInvoice) {
                        if (row.getPartamount() > 0) {
                            rowamountwithouttax = (rowamountwithouttax * row.getPartamount()) / 100;
                        }
                        obj.put(CustomDesignerConstants.PartialAmount, authHandler.formattingDecimalForAmount(row.getPartamount(), companyid) + "%");//Partial Amount
                    }

                    /*
                     * In include GST case calculations are in reverse order In
                     * other cases calculations are in forward order
                     */
                    double rowamountwithgst = 0;
                    double discountBeforePartial = 0.0;
                    double rowtaxamountincludeGST = 0.0;
                    if (row.getInvoice().isGstIncluded()) {//if gstincluded is the case
                        rowamountwithgst = row.getRateincludegst() * quantity;

                        rowdiscountvalue = authHandler.round((rowamountwithouttax * detail.getDiscount()) / 100, companyid);

                        obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithouttax, companyid));//Subtotal
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.round((rowamountwithgst - (rowamountwithgst * rowdiscountvalue) / 100 - row.getRowTaxAmount()), companyid));//Subtotal
                        subtotal += authHandler.round(rowamountwithouttax, companyid);
                        subTotalWithDiscount += authHandler.round(rowamountwithouttax - rowdiscountvalue, companyid);
                        /**
                         * Calculate amount before deducting partial payment
                         */
                        if (isPartialInvoice) {
                            double rowamtwithouttax = 0.0;
                            if (detail.getTax() != null) {
                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), detail.getTax().getID());
                                double rowtaxpercent = (Double) perresult.getEntityList().get(0);
                                rowamtwithouttax = (rowamountwithgst * 100) / (100 + rowtaxpercent);
                                rowtaxamountincludeGST = rowamtwithouttax * rowtaxpercent / 100;
                            } else {
                                rowamtwithouttax = rowamountwithgst;
                            }
                            if (row.getDiscount() != null && row.getDiscount().isInPercent()) {
                                discountBeforePartial = row.getDiscount() != null ? ((rowamountwithgst - row.getRowTaxAmount()) * row.getDiscount().getDiscount()) / 100 : 0.0;
                            } else {
                                discountBeforePartial = row.getDiscount() != null ? row.getDiscount().getDiscountValue() : 0.0;
                            }
                            obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount((rowamtwithouttax - discountBeforePartial), companyid));//Subtotal
                        } else {
                            obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount((rowamountwithgst - (rowamountwithgst * rowdiscountvalue) / 100 - row.getRowTaxAmount()), companyid));//Subtotal
                        }
                    } else {
                        rowdiscountvalue = authHandler.round((rowamountwithouttax * detail.getDiscount()) / 100, companyid);

                        obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithouttax, companyid));//Subtotal
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid));//Subtotal
                        subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                        subTotalWithDiscount += authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid);
                        /**
                         * Calculate amount before deducting partial payment
                         */
                        if (isPartialInvoice) {
                            if (row.getDiscount() != null && row.getDiscount().isInPercent()) {
                                discountBeforePartial = row.getDiscount() != null ? ((rate * quantity) * row.getDiscount().getDiscount()) / 100 : 0.0;
                            } else {
                                discountBeforePartial = row.getDiscount() != null ? row.getDiscount().getDiscountValue() : 0.0;
                            }
                            obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount(((rate * quantity) - discountBeforePartial), companyid));//Subtotal
                        } else {
                            obj.put(CustomDesignerConstants.AMOUNT_BEFORE_PARTIAL_PAYMENT, authHandler.formattingDecimalForAmount((rowamountwithouttax - rowdiscountvalue), companyid));//Subtotal
                        }
                    }

                    /*
                     * Discount Section
                     */
//                    Discount discount = row.getDiscount();
//                    if (discount != null) {
//                        if (discount.isInPercent()) {
//                            double discountpercent = row.getDiscount().getDiscount();
//                            discountname = (long) discountpercent == discountpercent ? (long) discountpercent + "%" : discountpercent + "%"; // ERP-27882
//                        } else {
//                            discountname = invoice.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount().getDiscountValue(), companyid);//to show as it is in UI
//                        }
//                    } else {
//                        discountname = "0 %";
//                    }
                    discountname = detail.getDiscount() + " %";
                    totalDiscount += authHandler.round(rowdiscountvalue, companyid);

                    obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                    obj.put(CustomDesignerConstants.Discountname, discountname);// Discount
                    obj.put(CustomDesignerConstants.QuantitywithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom); // Quantity
                    obj.put(CustomDesignerConstants.JobWorkChallanDate, challanDate);
                    obj.put(CustomDesignerConstants.IN_Quantity, authHandler.formattingDecimalForQuantity(quantity, companyid));
                    obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate
                    obj.put(CustomDesignerConstants.RATEINCLUDINGGST, authHandler.formattingDecimalForUnitPrice(row.getRateincludegst(), companyid));// Rate Including GST
                    obj.put(CustomDesignerConstants.IN_Currency, invoice.getCurrency().getCurrencyCode());
                    obj.put(CustomDesignerConstants.IN_UOM, uom);
                    obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(prod.getProductweight(), companyid));//Product Weight
                    obj.put("currencysymbol", invoice.getCurrency().getSymbol());//used for headercurrency & record currency
                    obj.put("basecurrencysymbol", invoice.getCompany().getCurrency().getSymbol());//used for headercurrency & record currency
                    obj.put("basecurrencycode", invoice.getCompany().getCurrency().getCurrencyCode());//used for headercurrency & record currency
                    obj.put("isGstIncluded", invoice.isGstIncluded());//used for headercurrency & record currency
                    obj.put("currencycode", invoice.getCurrency().getCurrencyCode());//used for headercurrencyCode & record currency
                    String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(requestJobj.optString("cdomain", ""), false);
                    /*
                     * Following code is to check whether the image is predent
                     * for product or not. If Image is not present sent s.gif
                     * instead of product id
                     */
                    String fileName = null;
                    fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages + prod.getID() + ".png";
                    File file = new File(fileName);
                    FileInputStream in = null;
                    String filePathString = "";
                    try {
                        in = new FileInputStream(file);
                    } catch (java.io.FileNotFoundException ex) {
                        //catched exception if file not found, and to continue for rest of the products
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    if (in != null) {
                        filePathString = baseUrl + "productimage?fname=" + prod.getID() + ".png&isDocumentDesignerPrint=true";
                    } else {
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    obj.put(CustomDesignerConstants.imageTag, filePathString);

                    String lineLevelTax = "";
                    String lineLevelTaxPercent = "";
                    String lineLevelTaxAmount = "";
                    if (extraCompanyPreferences.isIsNewGST()) { // for New gst check 
                        HashMap<String, Object> InvoiceDetailParams = new HashMap<String, Object>();
                        InvoiceDetailParams.put("InvoiceDetailid", row.getID());
                        InvoiceDetailParams.put("orderbyadditionaltax", true);
                        // GST
                        InvoiceDetailParams.put("termtype", 7);
                        KwlReturnObject grdTermMapresult = accInvoiceDAOobj.getInvoicedetailTermMap(InvoiceDetailParams);
                        List<InvoiceDetailTermsMap> gst = grdTermMapresult.getEntityList();

                        for (InvoiceDetailTermsMap invoicedetailTermMap : gst) {
                            LineLevelTerms mt = invoicedetailTermMap.getTerm();
                            //Put tax rate and amount in respective fields
                            if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                                obj.put(CustomDesignerConstants.CGSTPERCENT, invoicedetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.CGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                                obj.put(CustomDesignerConstants.IGSTPERCENT, invoicedetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.IGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                                obj.put(CustomDesignerConstants.SGSTPERCENT, invoicedetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.SGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                                obj.put(CustomDesignerConstants.UTGSTPERCENT, invoicedetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.UTGSTAMOUNT, invoicedetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                                obj.put(CustomDesignerConstants.CESSPERCENT, invoicedetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.CESSAMOUNT, invoicedetailTermMap.getTermamount());
                            }
                            lineLevelTax += mt.getTerm();
                            lineLevelTax += "!## ";

                            lineLevelTaxPercent += authHandler.formattingDecimalForAmount(invoicedetailTermMap.getPercentage(), companyid);
                            lineLevelTaxPercent += "!## ";
                            lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(invoicedetailTermMap.getTermamount(), amountdigitafterdecimal, countryid);
                            lineLevelTaxAmount += "!## ";
                            if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                                double value = lineLevelTaxNames.get(mt.getTerm());
                                lineLevelTaxNames.put(mt.getTerm(), invoicedetailTermMap.getTermamount() + value);

                            } else {
                                lineLevelTaxNames.put(mt.getTerm(), invoicedetailTermMap.getTermamount());
                            }
                        }

                        if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                            lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                        }
                        if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                            lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                        }
                        if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                            lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                        }
                        obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                        obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                        obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                        //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                        ExportRecordHandler.setHsnSacProductDimensionField(prod, obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    } else {
                        /*
                         * Fetching distinct taxes used at line level, feetched
                         * in the set Also, fetched the information related to
                         * tax in different maps
                         */
                        boolean isRowTaxApplicable = false;
                        double rowTaxPercentGST = 0.0;
                        if (detail.getTax() != null) {
                            String taxCode = detail.getTax().getTaxCode();
                            if (!lineLevelTaxesGST.contains(taxCode)) {
                                lineLevelTaxesGST.add(taxCode);
//                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getJournalEntry().getEntryDate(), detail.getTax().getID());
                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, invoice.getCreationDate(), detail.getTax().getID());
                                rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                                lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                                lineLevelTaxAmountGST.put(taxCode, 0.0);
                                lineLevelTaxBasicGST.put(taxCode, 0.0);
                            }
                            double taxAmount = (((rowamountwithouttax - rowdiscountvalue) * rowTaxPercentGST) / 100);
                            lineLevelTaxAmountGST.put(taxCode, (Double) lineLevelTaxAmountGST.get(taxCode) + (taxAmount));
                            lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL, 0.0));

                        }
                    }

                    KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                    CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                    if (!StringUtil.isNullOrEmpty(prod.getID())) {
                        KwlReturnObject prodresult = accProductObj.getObject(Product.class.getName(), prod.getID());
                        Product product = (Product) prodresult.getEntityList().get(0);
                        isLocationForProduct = product.isIslocationforproduct();
                        isbatchforproduct = product.isIsBatchForProduct();
                        isserialforproduct = product.isIsSerialForProduct();
                        isWarehouseForProduct = product.isIswarehouseforproduct();

                    }
                    if (preferences.isIsBatchCompulsory() || preferences.isIsSerialCompulsory()
                            || preferences.isIslocationcompulsory() || preferences.isIswarehousecompulsory() || preferences.isIsrowcompulsory()
                            || preferences.isIsrackcompulsory() || preferences.isIsbincompulsory()) {
                        if (isbatchforproduct || isserialforproduct || isLocationForProduct || isWarehouseForProduct) {  //product level batch and serial no on or not but now only location is checked
                            String rowid = "";
                            KwlReturnObject res = accInvoiceDAOobj.getDOFromInvoiceNew(invoice.getID(), companyid);
                            if (res.getEntityList().size() > 0 && res.getEntityList().size() >= rowcnt) {
                                String DOdetailID = res.getEntityList().get(rowcnt - 1).toString();
                                DeliveryOrderDetail deliveryOrderDetailObj = (DeliveryOrderDetail) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.DeliveryOrderDetail", DOdetailID);
                                if (deliveryOrderDetailObj != null) {
                                    rowid = deliveryOrderDetailObj.getID();
                                }
                            } else {
                                if (row.getDeliveryOrderDetail() != null) {
                                    rowid = row.getDeliveryOrderDetail().getID();
                                }
                            }

                            String batchdetails = accInvoiceServiceDAO.getNewBatchJson(prod, requestJobj, rowid);
                            JSONArray locjArr = new JSONArray(batchdetails);
                            String location = "";
                            String locationName = "", batchname = "", warehouse = "", serialnumber = "", serialexpdate = "", batchexpdate = "", batchmfgdate = "";
                            String locationnamenew = "", batchnamenew = "", warehousenew = "", serialnumbernew = "", serialexpdatenew = "", batchexpdatenew = "", batchesmfgdatenew = "";
                            LinkedList<String> batchnames = new LinkedList();
                            LinkedList<String> serialnumbers = new LinkedList();
                            LinkedList<String> locationnames = new LinkedList();
                            LinkedList<String> warehouses = new LinkedList();
                            LinkedList<String> serialsexpdate = new LinkedList();
                            LinkedList<String> batchesexpirydate = new LinkedList();
                            LinkedList<String> batchesmfgdate = new LinkedList();

                            for (int i = 0; i < locjArr.length(); i++) {
                                JSONObject jSONObject = new JSONObject(locjArr.get(i).toString());
                                location = jSONObject.optString("location", "");
                                batchname = jSONObject.optString("batchname", "");
                                warehouse = jSONObject.optString("warehouse", "");
                                serialnumber = jSONObject.optString("serialno", "");
                                serialexpdate = jSONObject.optString("expend", "");
                                batchexpdate = jSONObject.optString("expdate", "");
                                batchmfgdate = jSONObject.optString("mfgdate", "");
                                Date date = null;
                                if (!StringUtil.isNullOrEmpty(serialexpdate)) {
                                    date = df1.parse(serialexpdate);
                                    serialexpdate = df.format(date);
                                    serialsexpdate.add(serialexpdate);
                                } else {
                                    serialsexpdate.add(" ");
                                }

                                if (!StringUtil.isNullOrEmpty(batchexpdate)) {
                                    date = df1.parse(batchexpdate);
                                    batchexpdate = df.format(date);
                                    if (batchesexpirydate.contains(batchexpdate)) {
                                        batchesexpirydate.add(" ");
                                    } else {
                                        batchesexpirydate.add(batchexpdate);
                                    }
                                } else {
                                    batchesexpirydate.add(" ");
                                }
                                if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
                                    date = df1.parse(batchmfgdate);
                                    batchmfgdate = df.format(date);
                                    if (batchesmfgdate.contains(batchmfgdate)) {
                                        batchesmfgdate.add(" ");
                                    } else {
                                        batchesmfgdate.add(batchmfgdate);
                                    }
                                } else {
                                    batchesmfgdate.add(" ");
                                }
                                serialnumbers.add(serialnumber);

                                if (!StringUtil.isNullOrEmpty(batchname)) {
                                    if (batchnames.contains(batchname)) {
                                        batchnames.add(" ");
                                    } else {
                                        batchnames.add(batchname);
                                    }
                                }

                                if (!StringUtil.isNullOrEmpty(warehouse)) {
                                    if (warehouses.contains(warehouse)) {
                                        warehouses.add(" ");
                                    } else {
                                        warehouses.add(warehouse);
                                    }
                                }

                                if (!StringUtil.isNullOrEmpty(location)) {
                                    KwlReturnObject loc = accountingHandlerDAOobj.getObject(InventoryLocation.class.getName(), location);
                                    InventoryLocation localist = (InventoryLocation) loc.getEntityList().get(0);
                                    locationName = localist.getName();
                                    if (locationnames.contains(locationName)) {
                                        locationnames.add(" ");
                                    } else {
                                        locationnames.add(locationName);
                                    }
//                                locationName = locationName.concat(",");
                                }

//                                if (!StringUtil.isNullOrEmpty(batchname)) {
//                                    batchname = batchname.concat(",");
//                                }
//                                if (!StringUtil.isNullOrEmpty(serialnumber)) {
//                                    serialnumber = serialnumber.concat(",");
//                                }
//                                if (!StringUtil.isNullOrEmpty(serialexpdate)) {
//                                    serialexpdate = serialexpdate.concat("<br>");
//                                }
//                                if (!StringUtil.isNullOrEmpty(batchexpdate)) {
//                                    batchexpdate = batchexpdate.concat("<br>");
//                                }
//                                if (!StringUtil.isNullOrEmpty(batchmfgdate)) {
//                                    batchmfgdate = batchmfgdate.concat("<br>");
//                                }
                            }

                            for (String str : batchnames) {
                                String bno = "";
                                bno = str;
                                if (!StringUtil.isNullOrEmpty(bno) && !bno.equals(" ")) {
                                    batchnamenew += bno.concat("!##");
                                }
                            }

                            for (String str : serialnumbers) {
                                String sno = "";
                                sno = str;
                                if (!StringUtil.isNullOrEmpty(sno) && !sno.equals(" ")) {
                                    serialnumbernew += sno.concat("!##");
                                }
                            }
                            for (String str : locationnames) {
                                String lno = "";
                                lno = str;
                                if (!StringUtil.isNullOrEmpty(lno) && !lno.equals(" ")) {
                                    locationnamenew += lno.concat("!##");
                                }
                            }
                            for (String str : serialsexpdate) {
                                String sexp = "";
                                sexp = str;
                                if (!StringUtil.isNullOrEmpty(sexp) && !sexp.equals(" ")) {
                                    serialexpdatenew += sexp.concat("!##");
                                }
                            }

                            for (String str : batchesexpirydate) {
                                String bexp = "";
                                bexp = str;
                                if (!StringUtil.isNullOrEmpty(bexp) && !bexp.equals(" ")) {
                                    batchexpdatenew += bexp.concat("!##");
                                }
                            }
                            for (String str : batchesmfgdate) {
                                String bmfg = "";
                                bmfg = str;
                                if (!StringUtil.isNullOrEmpty(bmfg) && !bmfg.equals(" ")) {
                                    batchesmfgdatenew += bmfg.concat("!##");
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(batchesmfgdatenew)) {
                                batchesmfgdatenew = batchesmfgdatenew.substring(0, batchesmfgdatenew.length() - 3);
                            }
                            obj.put(CustomDesignerConstants.SerialNumber, serialnumbernew);
                            obj.put(CustomDesignerConstants.SerialNumberExp, serialexpdatenew);
                            obj.put(CustomDesignerConstants.BatchNumber, batchnamenew);
                            obj.put(CustomDesignerConstants.JobWorkBalanceQty, "");
                            obj.put(CustomDesignerConstants.JobWorkConsumeQty, "");
                            obj.put(CustomDesignerConstants.BatchNumberExp, batchexpdatenew);
                            obj.put(CustomDesignerConstants.IN_Loc, locationnamenew);
                            obj.put(CustomDesignerConstants.ManufacturingDate, batchesmfgdatenew);// Batch Manufacturing Date
                        } else { //if not activated location for product level then take default location for product.
                            obj.put(CustomDesignerConstants.IN_Loc, prod.getLocation() == null ? "" : prod.getLocation().getName());
                        }
                    } else { //if not activated location for product level then take default location for product.
                        obj.put(CustomDesignerConstants.IN_Loc, prod.getLocation() == null ? "" : prod.getLocation().getName());
                    }
                    /*
                     * Row tax section
                     */
//                entryDate = invoice.getJournalEntry().getEntryDate();
                    Date entryDate = invoice.getCreationDate();
                    if (detail != null && detail.getTax() != null) {

                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", detail.getTax().getID());
                        KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                        List taxList = result1.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        rowTaxName = detail.getTax().getName();
                        rowTaxAmt = detail.getRowTaxAmount() ;
                    }

                    totaltax += rowTaxAmt;//Calculate tax amount from line item
                    obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);//Row TaxAmount
                    obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);

                    if (row.getInvoice().isGstIncluded()) {
                        rowamountwithtax = rowamountwithouttax - rowdiscountvalue + rowTaxAmt; //Amount will be equal to rowamountwithouttax because tax gets added in gst
                    } else {
                        if (rowdiscountvalue != 0) {
                            rowamountwithouttax -= rowdiscountvalue;//deducting discount if any
                        }
                        rowamountwithtax = rowamountwithouttax + rowTaxAmt;
                    }
                    obj.put(CustomDesignerConstants.Amount, authHandler.round(rowamountwithtax, companyid)); // Amount

                    obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
                    obj.put("gstCurrencyRate", invoice.getGstCurrencyRate() == 0 ? "" : invoice.getGstCurrencyRate());

                    obj.put("transactiondate", invoice.getJournalEntry().getEntryDate());

                    /*
                     * <--------------------Base Currency Unit Price,Base
                     * currency Subtotal ----------->
                     */
                    /*
                     * Exchange Rate Section---- We have not given amount with
                     * tax because it is matched with UI
                     */
                    if (externalCurrencyRate != 0) {
                        //Unit Price   
                        exchangerateunitprice = authHandler.round((rate * revExchangeRate), companyid);  //exchanged rate unit rate
                        //SUbTotal (rate*quantity)
                        exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                        //Exhanged Rate Discount
                        exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue * revExchangeRate), companyid);//exchange rate total discount
                        if (row.getInvoice().isGstIncluded()) {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                        } else {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                        }

                        //Exhanged Rate Tax Amount
                        exchangeratelineitemtax = authHandler.round((rowTaxAmt * revExchangeRate), companyid);

                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);

                        obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);
                        //Total Exchanged Rate (RATE*Quantity)
                    } else {
                        //Unit Price   
                        exchangerateunitprice = authHandler.round(rate, companyid);  //exchanged rate unit rate
                        //SUbTotal (rate*quantity)
                        exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                        //Exhanged Rate Discount
                        exchangeratelineitemdiscount = authHandler.round(rowdiscountvalue, companyid);//exchange rate total discount
                        if (row.getInvoice().isGstIncluded()) {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                        } else {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                        }

                        //Exhanged Rate Tax Amount
                        exchangeratelineitemtax = authHandler.round(rowTaxAmt, companyid);

                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);

                        obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                    }

                    /*
                     * to get the linkig information upto 2-3 levels (Mayur B).
                     */
                    if (row.getDeliveryOrderDetail() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber());
                    } else if (row.getSalesorderdetail() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getSalesorderdetail().getSalesOrder().getSalesOrderNumber());
                    } else if (row.getQuotationDetail() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getQuotationDetail().getQuotation().getquotationNumber());
                    }

//                    /*
//                     * get custom line data
//                     */
//                    Map<String, Object> variableMap = new HashMap<String, Object>();
//                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
//                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
//                    Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
//                    Detailfilter_params.add(row.getID());
//                    invDetailRequestParams.put("filter_names", Detailfilter_names);
//                    invDetailRequestParams.put("filter_params", Detailfilter_params);
//                    KwlReturnObject idcustresult = accInvoiceDAOobj.getInvoiceDetailsCustomData(invDetailRequestParams);
//                    if (idcustresult.getEntityList().size() > 0) {
//                        AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
//                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestJobj, jeDetailCustom, FieldMap, obj, kwlCommonTablesDAOObj, variableMap);
//                    }

//                    /*
//                     * Product Level Custom Fields Evaluation
//                     */
//                    invDetailRequestParams.clear();
//                    Detailfilter_names.add("productId");
//                    Detailfilter_params.add(row.getInventory().getProduct().getID());
//                    invDetailRequestParams.put("filter_names", Detailfilter_names);
//                    invDetailRequestParams.put("filter_params", Detailfilter_params);
//                    KwlReturnObject idcustresultForProduct = accInvoiceDAOobj.getInvoiceDetailsCustomDataForProductNew(invDetailRequestParams);
//                    AccJEDetailsProductCustomData accJEDetailsProductCustomData = null;
//                    String customDataID = "";
//                    if (idcustresultForProduct.getEntityList().size() > 0) {
////                    accJEDetailsProductCustomData = (AccJEDetailsProductCustomData) idcustresultForProduct.getEntityList().get(0);
//                        customDataID = idcustresultForProduct.getEntityList().get(0).toString();
//                        accJEDetailsProductCustomData = (AccJEDetailsProductCustomData) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.AccJEDetailsProductCustomData", customDataID);
//                    }
//                    replaceFieldMap = new HashMap<String, String>();
//                    if (accJEDetailsProductCustomData != null) {
//                        ExportRecordHandler.AssignLineItemCustomfieldsDimensionValues(requestJobj, accJEDetailsProductCustomData, ProductLevelCustomFieldMap, obj, kwlCommonTablesDAOObj, variableMap);
//                    }

                    /*
                     * Set All Line level Dimension & All LIne level Custom
                     * Field Values
                     */
//                    obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(DimensionFieldMap, variableMap, obj, false);//for dimensions
//                    obj = CustomDesignHandler.setAllLinelevelDimensionCustomFieldValues(LineLevelCustomFieldMap, variableMap, obj, true);//for customfields
                    //GST Exchange Rate
                    if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !preference.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                        obj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, row.getGstCurrencyRate());
                    }
                    jArr.put(obj);
                    
                    //Calculate summary data for CN
                    if (!processedInv.contains(invoice.getID())) {

                        if (!StringUtil.isNullOrEmpty(invoiceNos)) {
                            invoiceNos += ", ";
                            invAmounts += ", ";
                            invTaxAmounts += ", ";
                            invAmountDues += ", ";
//                        invEnterAmounts += ", ";
                            invoicedates += ", ";
                            invoiceSalesPerson += ", ";
                        }
                        double invamount = 0, invtax = 0;

                        if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                            invamount = invoice.getOriginalOpeningBalanceAmount();
                        } else {
                            invamount = invoice.getCustomerEntry().getAmount();//Invoice Sub Total
                        }

                        invtax = invoice.getTaxEntry() == null ? 0 : invoice.getTaxEntry().getAmount();

                        invoiceNos += invoice.getInvoiceNumber();
                        invAmounts += invamount;
                        invTaxAmounts += invtax;
                        invAmountDues += invoice.getInvoiceamountdue();
//                            invEnterAmounts += (StringUtil.isNullOrEmpty(Double.toString(jObj.getDouble(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId))) ? "-" : Double.toString(jObj.getDouble(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId)));
                        invoicedates += invoice.getJournalEntry() != null ? invoice.getJournalEntry().getEntryDate() : invoice.getCreationDate();
                        invoiceSalesPerson += invoice.getMasterSalesPerson() == null ? " - " : invoice.getMasterSalesPerson().getValue();

                        invoicesubtotal += invamount - invtax;//Invoice Sub Total
                        invoicetotaltax += invtax; //Invoice Total Tax
                        invoicetotalamount += invamount; //Invoice Total Amount

                        processedInv.add(invoice.getID());
                    }
                }
            }
          
            //Calculate total tax if global tax is applied 
            if (dn.getTax() != null) {
                requestParams.put("transactiondate", dn.getJournalEntry().getEntryDate());
                requestParams.put("taxid", dn.getTax().getID());
                requestParams.put("companyid", companyid);
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                double taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                totaltax = (subTotalWithDiscount * taxPercent) / 100;

            }


            summaryParams.put("lineLevelTaxesGST", lineLevelTaxesGST);
            summaryParams.put("lineLevelTaxAmountGST", lineLevelTaxAmountGST);
            summaryParams.put("lineLevelTaxBasicGST", lineLevelTaxBasicGST);
            summaryParams.put("invoicesubtotal", invoicesubtotal);
            summaryParams.put("invoicetotaltax", invoicetotaltax);
            summaryParams.put("invoicetotalamount", invoicetotalamount);
            summaryParams.put("invoiceNos", invoiceNos);
            summaryParams.put("invAmounts", invAmounts);
            summaryParams.put("invTaxAmounts", invTaxAmounts);
            summaryParams.put("invAmountDues", invAmountDues);
            summaryParams.put("invoicedates", invoicedates);
            summaryParams.put("totaltax", totaltax);
            summaryParams.put("totalDiscount", totalDiscount);
            summaryParams.put("subTotal", subtotal);
            summaryParams.put("subTotalWithDiscount", subTotalWithDiscount);
            summaryParams.put("subTotalWithTax", subTotalWithDiscount + totaltax);
            summaryParams.put("totalwithtax", subTotalWithDiscount + totaltax);
            summaryParams.put("salespersonvalue", invoiceSalesPerson);
            summaryParams.put("lineLevelTaxNames", lineLevelTaxNames);
            
            //Getting summary data for Overcharged Cn
            JSONObject summaryData = getDNSummaryData(requestJobj, summaryParams);

            jArr.put(summaryData);

        } catch (Exception e) {
            Logger.getLogger(accGoodsReceiptCMN.class.getName()).log(Level.SEVERE, null, e);
        }

        return jArr;
    }
    
    /*
     * Function to fetch product details for Overcharged DN
     */
    public JSONArray getOverchargeDNDetailsItemJSON(JSONObject requestJobj, Map<String, Object> summaryParams ) throws ServiceException {

        JSONArray jArr = new JSONArray();
        try {
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) summaryParams.get("companyAccountPreferences");
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) summaryParams.get("extraCompanyPreferences");
            String companyid = requestJobj.optString(Constants.companyKey);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(requestJobj);

            int countryid = 0;
            if (extraCompanyPreferences.getCompany() != null && extraCompanyPreferences.getCompany().getCountry() != null) {
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }

            DebitNote dn = (DebitNote) summaryParams.get("debitNote");
            Set<DebitNoteAgainstCustomerGst> rowsGst = dn.getRowsGst();
            GoodsReceiptDetail row = null;
            GoodsReceipt goodsReceipt = null;
            int rowcnt = 0;
            String currencyid = "", uom = "", vendorTitle = "";
            Date entryDate = null;
            double subtotal = 0, subTotalWithDiscount = 0, totalDiscount = 0, totaltax = 0, discountamount = 0, revExchangeRate = 0.0;
            int amountdigitafterdecimal = 2;

            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(companyid);
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                }
            }

            /*
             * Declaration of Variables to fetch Line level taxes and its
             * information like Tax Code, Tax Amount etc.
             */
            Set<String> lineLevelTaxesGST = new HashSet<String>();
            Map<String, Object> lineLevelTaxAmountGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxPercentGST = new HashMap<String, Object>();
            Map<String, Object> lineLevelTaxBasicGST = new HashMap<String, Object>();
            Map<String, Double> lineLevelTaxNames = new LinkedHashMap<String, Double>();

            String invoiceNos = "", invAmounts = "", invTaxAmounts = "", invAmountDues = "", invoicedates = "", invoiceSalesPerson = "";
            double invoicesubtotal = 0, invoicetotaltax = 0, invoicetotalamount = 0;


            // List to identify processed invoices while calculating summary data for CN
            List<String> processedInv = new ArrayList<>();


            for (DebitNoteAgainstCustomerGst detail : rowsGst) {
                if (detail.getGrdetail() != null) {
                    rowcnt++;
                    row = detail.getGrdetail();
                    goodsReceipt = row.getGoodsReceipt();

                    double externalCurrencyRate = goodsReceipt.getExchangeRateDetail() != null ? goodsReceipt.getExchangeRateDetail().getExchangeRate() : 1;
                    if (externalCurrencyRate != 0) {
                        revExchangeRate = 1 / externalCurrencyRate;
                    }
                    
                    JSONObject obj = new JSONObject();
                    Inventory inv = row.getInventory();
                    Product prod = inv.getProduct();
                    String proddesc = "", discountname = "", rowTaxName = "", basqtyuom = "", BaseQtyWithUOM = "";
                    double rate = 0, rowTaxPercent = 0, rowTaxAmt = 0, rowamountwithtax = 0, rowdiscountvalue = 0, rowamountwithouttax = 0, quantity = 0, baseqty = 0;
                    double rowamountwithgst = 0;
                    //product type
                    obj.put("type", prod.getProducttype() == null ? "" : prod.getProducttype().getName());
                    obj.put(CustomDesignerConstants.IN_ProductCode, prod.getProductid());

                    double disc = detail.getDiscount();
                    obj.put("prdiscount", disc);
                    obj.put("discountispercent", 1);

//                entryDate = goodsReceipt.getJournalEntry().getEntryDate();
                    entryDate = goodsReceipt.getCreationDate();

                    rate = detail.getRate();
                    quantity = (goodsReceipt.getPendingapproval() == 1 || goodsReceipt.getIstemplate() == 2) ? row.getInventory().getActquantity() : (row.getInventory().isInvrecord() ? detail.getReturnQuantity() : detail.getReturnQuantity());
                    rowamountwithouttax = rate * quantity;

                    /*
                     * In include GST case calculations are in reverse order In
                     * other cases calculations are in forward order
                     */
                    if (row.getGoodsReceipt().isGstIncluded()) {//if gstincluded is the case
                        
                        rowamountwithgst = row.getRateincludegst() * quantity;
                        rowdiscountvalue = authHandler.round((rowamountwithouttax * detail.getDiscount()) / 100, companyid);
                        obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.round(rowamountwithouttax, companyid));//Subtotal
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.round((rowamountwithgst - (rowamountwithgst * rowdiscountvalue) / 100 - row.getRowTaxAmount()), companyid));//Subtotal
                        subtotal += authHandler.round(rowamountwithouttax, companyid);
                        subTotalWithDiscount += authHandler.round(rowamountwithouttax - rowdiscountvalue, companyid);
                    } else {
                        rowdiscountvalue = authHandler.round((rowamountwithouttax * detail.getDiscount()) / 100, companyid);
                        obj.put(CustomDesignerConstants.SUBTOTAL, authHandler.formattingDecimalForAmount(rowamountwithouttax, companyid));//Subtotal
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithDiscount, authHandler.formattingDecimalForAmount((rowamountwithouttax - rowdiscountvalue), companyid));//Subtotal
                        subtotal += authHandler.round(rowamountwithouttax, companyid);//rounded becuase totaltax & amount are rounded and saved in db
                        subTotalWithDiscount += authHandler.round((rowamountwithouttax - rowdiscountvalue), companyid);
                    }
                    obj.put(CustomDesignerConstants.Rate, authHandler.formattingDecimalForUnitPrice(rate, companyid));// Rate

                    /*
                     * Discount Section
                     */
//                    Discount discount = row.getDiscount();
//                    if (discount != null) {
//                        if (discount.isInPercent()) {
//                            discountname = CustomDesignHandler.getAmountinCommaDecimal(row.getDiscount().getDiscount(), 0, countryid) + "%";//to return 0 no of zeros
//                        } else {
//                            discountname = goodsReceipt.getCurrency().getSymbol() + " " + authHandler.formattingDecimalForAmount(row.getDiscount().getDiscountValue(), companyid);//to show as it is in UI
//                        }
//                    } else {
                        discountname = detail.getDiscount()+ " %";
//                    }
                    totalDiscount += authHandler.round(rowdiscountvalue, companyid);
                    obj.put(CustomDesignerConstants.IN_Discount, authHandler.formattingDecimalForAmount(rowdiscountvalue, companyid));// Discount
                    obj.put(CustomDesignerConstants.Discountname, discountname);// Discount Name

                    KwlReturnObject productCategories = null;
                    productCategories = accProductObj.getProductCategoryForDetailsReport(prod.getID());
                    List productCategoryList = productCategories.getEntityList();
                    String cateogry = "";
                    Iterator catIte = productCategoryList.iterator();
                    while (catIte.hasNext()) {
                        ProductCategoryMapping pcm = (ProductCategoryMapping) catIte.next();
                        String categoryName = pcm.getProductCategory() != null ? pcm.getProductCategory().getValue().toString() : "";
                        cateogry += categoryName + " ";
                    }
                    if (StringUtil.isNullOrEmpty(cateogry)) {
                        cateogry = "None";
                    }
                    obj.put("productCategory", cateogry);

                    /*
                     * Row tax section
                     */
                    if (row != null && detail.getTax() != null) {
                        requestParams.put("transactiondate", entryDate);
                        requestParams.put("taxid", detail.getTax().getID());
                        requestParams.put("companyid", companyid);
                        KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                        List taxList = result1.getEntityList();
                        Object[] taxObj = (Object[]) taxList.get(0);
                        rowTaxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];
                        rowTaxName = detail.getTax().getName();
                        discountamount += discountamount * rowTaxPercent / 100;
                        rowTaxAmt = detail.getRowTaxAmount();
                    }
                    totaltax += authHandler.round(rowTaxAmt, companyid);//Calculate tax amount from line item
                    obj.put(CustomDesignerConstants.IN_Tax, rowTaxAmt);//Row TaxAmount
                    obj.put(CustomDesignerConstants.IN_ProductTax, rowTaxName);

                    if (row.getGoodsReceipt().isGstIncluded()) {
                        rowamountwithtax = rowamountwithouttax - rowdiscountvalue + rowTaxAmt; 
                    } else {
                        if (rowdiscountvalue != 0) {
                            rowamountwithouttax -= rowdiscountvalue;//deducting discount if any
                        }
                        rowamountwithtax = rowamountwithouttax + rowTaxAmt;
                    }

                    obj.put(CustomDesignerConstants.SpecificCurrencyExchangeRate, 1);
                    obj.put("gstCurrencyRate", goodsReceipt.getGstCurrencyRate() == 0 ? "" : goodsReceipt.getGstCurrencyRate());
                    obj.put("transactiondate", goodsReceipt.getJournalEntry().getEntryDate());

                    double exchangerateunitprice = 0, exchangeratelineitemsubtotal = 0, exchangeratelineitemdiscount = 0, exchangeratelineitemamount = 0, exchangeratelineitemtax = 0, exchangeratelineitemsubtotalwithdiscount = 0;
                    /*
                     * <--------------------Base Currency Unit Price,Base
                     * currency Subtotal ----------->
                     */
                    /*
                     * Exchange Rate Section---- We have not given amount with
                     * tax because it is matched with UI
                     */
                    if (externalCurrencyRate != 0) {
                        //Unit Price   
                        exchangerateunitprice = authHandler.round((rate * revExchangeRate), companyid);  //exchanged rate unit rate
                        //SUbTotal (rate*quantity)
                        exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                        //Exhanged Rate Discount
                        exchangeratelineitemdiscount = authHandler.round((rowdiscountvalue * revExchangeRate), companyid);//exchange rate total discount
                        if (row.getGoodsReceipt().isGstIncluded()) {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                        } else {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                        }

                        //Exhanged Rate Tax Amount
                        exchangeratelineitemtax = authHandler.round((rowTaxAmt * revExchangeRate), companyid);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                        obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);


                    } else {
                        //Unit Price   
                        exchangerateunitprice = authHandler.round(rate, companyid);  //exchanged rate unit rate
                        //SUbTotal (rate*quantity)
                        exchangeratelineitemsubtotal = authHandler.round((exchangerateunitprice * quantity), companyid);
                        //Exhanged Rate Discount
                        exchangeratelineitemdiscount = authHandler.round(rowdiscountvalue, companyid);//exchange rate total discount
                        if (row.getGoodsReceipt().isGstIncluded()) {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round(exchangeratelineitemsubtotal, companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal + exchangeratelineitemtax), companyid);
                        } else {
                            exchangeratelineitemsubtotalwithdiscount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount), companyid);
                            exchangeratelineitemamount = authHandler.round((exchangeratelineitemsubtotal - exchangeratelineitemdiscount + exchangeratelineitemtax), companyid);
                        }

                        //Exhanged Rate Tax Amount
                        exchangeratelineitemtax = authHandler.round(rowTaxAmt, companyid);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemSubTotalWithDiscount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemTax, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.BaseCurrencyLineItemAmount, exchangeratelineitemamount);

                        obj.put(CustomDesignerConstants.SpecificCurrencyUnitPrice, exchangerateunitprice);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotal, exchangeratelineitemsubtotal);
                        obj.put(CustomDesignerConstants.SpecificCurrencyDiscount, exchangeratelineitemdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencySubTotalWithDicount, exchangeratelineitemsubtotalwithdiscount);
                        obj.put(CustomDesignerConstants.SpecificCurrencyTaxAmount, exchangeratelineitemtax);
                        obj.put(CustomDesignerConstants.SpecificCurrencyAmount, exchangeratelineitemamount);

                    }

                    obj.put(CustomDesignerConstants.Amount, authHandler.formattingDecimalForAmount(rowamountwithtax, companyid)); // Amount
                    String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(requestJobj.optString("cdomain", ""), false);
                    /*
                     * Following code is to check whether the image is predent
                     * for product or not. If Image is not present sent s.gif
                     * instead of product id
                     */
                    String fileName = null;
                    fileName = storageHandlerImpl.GetProfileImgStorePath() + Constants.ProductImages + prod.getID() + ".png";
                    File file = new File(fileName);
                    FileInputStream in = null;
                    String filePathString = "";
                    try {
                        in = new FileInputStream(file);
                    } catch (java.io.FileNotFoundException ex) {
                        //catched exception if file not found, and to continue for rest of the products
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    if (in != null) {
                        filePathString = baseUrl + "productimage?fname=" + prod.getID() + ".png&isDocumentDesignerPrint=true";
                    } else {
                        filePathString = baseUrl + "productimage?fname=s.gif&isDocumentDesignerPrint=true";
                    }
                    obj.put(CustomDesignerConstants.imageTag, filePathString);
                    /*
                     * Put frid values in Line Item Grid
                     */
                    if (row.getGoodsReceiptOrderDetails() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getGoodsReceiptOrderDetails().getGrOrder().getGoodsReceiptOrderNumber());
                    } else if (row.getPurchaseorderdetail() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getPurchaseorderdetail().getPurchaseOrder().getPurchaseOrderNumber());
                    } else if (row.getVendorQuotationDetail() != null) {
                        obj.put(CustomDesignerConstants.Link_No, row.getVendorQuotationDetail().getVendorquotation().getQuotationNumber());
                    } else {
                        obj.put(CustomDesignerConstants.Link_No, "");
                    }
                    obj.put("currencysymbol", goodsReceipt.getCurrency().getSymbol());
                    obj.put("currencycode", goodsReceipt.getCurrency().getCurrencyCode());
                    obj.put("isGstIncluded", goodsReceipt.isGstIncluded());//used for headercurrency & record currency


                    String lineLevelTax = "";
                    String lineLevelTaxPercent = "";
                    String lineLevelTaxAmount = "";
                    double lineLevelTaxAmountTotal = 0;
                    if (extraCompanyPreferences.isIsNewGST()) { // For India Country 
                        HashMap<String, Object> GoodsReceiptDetailParams = new HashMap<String, Object>();
                        GoodsReceiptDetailParams.put("GoodsReceiptDetailid", row.getID());
                        GoodsReceiptDetailParams.put("orderbyadditionaltax", true);
                        // GST
                        GoodsReceiptDetailParams.put("termtype", 2);
                        KwlReturnObject grdTermMapresult = accGoodsReceiptobj.getGoodsReceiptdetailTermMap(GoodsReceiptDetailParams);
                        List<ReceiptDetailTermsMap> gst = grdTermMapresult.getEntityList();
                        //CGST fields
                        obj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                        obj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                        //IGST fields
                        obj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                        obj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                        //SGST fields
                        obj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                        obj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                        //UTGST fields
                        obj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                        obj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                        //CESS fields
                        obj.put(CustomDesignerConstants.CESSPERCENT, 0);
                        obj.put(CustomDesignerConstants.CESSAMOUNT, 0);

                        for (ReceiptDetailTermsMap receiptdetailTermMap : gst) {
                            LineLevelTerms mt = receiptdetailTermMap.getTerm();
                            /**
                             * Put respective GST field in constants CGST, SGST,
                             * IGST, UTGST, CESS
                             */
                            if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                                obj.put(CustomDesignerConstants.CGSTPERCENT, receiptdetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.CGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                                obj.put(CustomDesignerConstants.IGSTPERCENT, receiptdetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.IGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                                obj.put(CustomDesignerConstants.SGSTPERCENT, receiptdetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.SGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                                obj.put(CustomDesignerConstants.UTGSTPERCENT, receiptdetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.UTGSTAMOUNT, receiptdetailTermMap.getTermamount());
                            } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                                obj.put(CustomDesignerConstants.CESSPERCENT, receiptdetailTermMap.getPercentage());
                                obj.put(CustomDesignerConstants.CESSAMOUNT, receiptdetailTermMap.getTermamount());
                            }
                            lineLevelTax += mt.getTerm();
                            lineLevelTax += "!## ";
                            lineLevelTaxPercent += authHandler.formattingDecimalForAmount(receiptdetailTermMap.getPercentage(), companyid);
                            lineLevelTaxPercent += "!## ";
                            lineLevelTaxAmount += CustomDesignHandler.getAmountinCommaDecimal(receiptdetailTermMap.getTermamount(), amountdigitafterdecimal, countryid);
                            lineLevelTaxAmount += "!## ";
                            /*
                             * calculating total of line level taxes
                             */
                            lineLevelTaxAmountTotal += receiptdetailTermMap.getTermamount();
                            if (lineLevelTaxNames.containsKey(mt.getTerm()) && lineLevelTaxNames.get(mt.getTerm()) != null) {
                                double value = lineLevelTaxNames.get(mt.getTerm());
                                lineLevelTaxNames.put(mt.getTerm(), receiptdetailTermMap.getTermamount() + value);
                            } else {
                                lineLevelTaxNames.put(mt.getTerm(), receiptdetailTermMap.getTermamount());
                            }
                            if (mt.isIsAdditionalTax()) {
                                String term = "";
                                if (mt.getTerm() != null) {
                                    term = mt.getTerm().toLowerCase();
                                }
                            }
                        }

                        if (!StringUtil.isNullOrEmpty(lineLevelTax)) {
                            lineLevelTax = lineLevelTax.substring(0, lineLevelTax.length() - 4);
                        }
                        if (!StringUtil.isNullOrEmpty(lineLevelTaxPercent)) {
                            lineLevelTaxPercent = lineLevelTaxPercent.substring(0, lineLevelTaxPercent.length() - 4);
                        }
                        if (!StringUtil.isNullOrEmpty(lineLevelTaxAmount)) {
                            lineLevelTaxAmount = lineLevelTaxAmount.substring(0, lineLevelTaxAmount.length() - 4);
                        }
                        obj.put(CustomDesignerConstants.LineLevelTax, lineLevelTax);
                        obj.put(CustomDesignerConstants.LineLevelTaxAmount, lineLevelTaxAmount);
                        obj.put(CustomDesignerConstants.LineLevelTaxPercent, lineLevelTaxPercent);
                        /*
                         * putting subtotal+tax
                         */
                        obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round(lineLevelTaxAmountTotal, companyid));
                        //set product level "Product Tax Class" dimension value in HSN/SAC Code field
                        ExportRecordHandler.setHsnSacProductDimensionField(prod, obj, companyid, accAccountDAOobj, kwlCommonTablesDAOObj);
                    } else {
                        /*
                         * Fetching distinct taxes used at line level, feetched
                         * in the set Also, fetched the information related to
                         * tax in different maps
                         */
                        boolean isRowTaxApplicable = false;
                        double rowTaxPercentGST = 0.0;
                        if (detail.getTax() != null) {
                            String taxCode = detail.getTax().getTaxCode();
                            if (!lineLevelTaxesGST.contains(taxCode)) {
                                lineLevelTaxesGST.add(taxCode);
//                            KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceipt.getJournalEntry().getEntryDate(), row.getTax().getID());
                                KwlReturnObject perresult = accTaxObj.getTaxPercent(companyid, goodsReceipt.getCreationDate(), detail.getTax().getID());
                                rowTaxPercentGST = (Double) perresult.getEntityList().get(0);
                                lineLevelTaxPercentGST.put(taxCode, rowTaxPercentGST);
                                lineLevelTaxAmountGST.put(taxCode, 0.0);
                                lineLevelTaxBasicGST.put(taxCode, 0.0);
                            }
                            lineLevelTaxAmountGST.put(taxCode, (Double) lineLevelTaxAmountGST.get(taxCode) + (row.getRowTaxAmount()));
                            lineLevelTaxBasicGST.put(taxCode, (Double) lineLevelTaxBasicGST.get(taxCode) + obj.optDouble(CustomDesignerConstants.SUBTOTAL, 0.0));
                            /*
                             * putting subtotal+tax
                             */
                            obj.put(CustomDesignerConstants.LineItemSubTotalWithTax, authHandler.round((rowamountwithouttax), companyid) + authHandler.round((row.getRowTaxAmount()), companyid));
                        }
                    }


                    uom = row.getInventory().getUom() == null ? (row.getInventory().getProduct().getUnitOfMeasure() == null ? "" : row.getInventory().getProduct().getUnitOfMeasure().getNameEmptyforNA()) : row.getInventory().getUom().getNameEmptyforNA();

                    //Calculating base qty with UOM
                    basqtyuom = prod.getUnitOfMeasure() == null ? "" : prod.getUnitOfMeasure().getNameEmptyforNA();
                    double baseuomqty = row.getInventory().getBaseuomrate() * quantity;

                    if (!basqtyuom.equals(uom)) {
                        BaseQtyWithUOM = authHandler.formattingDecimalForQuantity(baseuomqty, companyid) + " " + basqtyuom;
                    } else {
                        BaseQtyWithUOM = authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom;
                        baseuomqty = quantity;
                    }

                    obj.put(CustomDesignerConstants.BaseQty, authHandler.formattingDecimalForQuantity(baseuomqty, companyid));
                    obj.put(CustomDesignerConstants.BaseQtyWithUOM, BaseQtyWithUOM);

                    proddesc = StringUtil.isNullOrEmpty(row.getDescription()) ? (StringUtil.isNullOrEmpty(inv.getProduct().getDescription()) ? "" : inv.getProduct().getDescription()) : row.getDescription();
                    proddesc = StringUtil.DecodeText(proddesc);
                    obj.put(CustomDesignerConstants.SrNO, rowcnt);// Sr No
                    obj.put(CustomDesignerConstants.ProductName, prod.getName().replaceAll("\n", "<br>"));// productname
                    obj.put(CustomDesignerConstants.ProductDescription, proddesc.replaceAll("\n", "<br>"));//Product Description
                    obj.put(CustomDesignerConstants.HSCode, (prod.getHSCode() != null) ? prod.getHSCode().replaceAll("\n", "<br>") : "");//get HSCode of ProductGrid
                    obj.put(CustomDesignerConstants.QuantitywithUOM, authHandler.formattingDecimalForQuantity(quantity, companyid) + " " + uom); // Quantity
                    obj.put(CustomDesignerConstants.IN_Currency, goodsReceipt.getCurrency().getCurrencyCode());
                    obj.put(CustomDesignerConstants.IN_Quantity, authHandler.formattingDecimalForQuantity(quantity, companyid)); // Quantity
                    obj.put(CustomDesignerConstants.IN_UOM, uom);
                    obj.put(CustomDesignerConstants.PRODUCTNETWEIGHT, authHandler.formattingDecimalForAmount(prod.getProductweight(), companyid));//Product Weight
                    obj.put(CustomDesignerConstants.AdditionalDescription, prod.getAdditionalDesc() != null ? prod.getAdditionalDesc().replaceAll("\n", "<br>") : "");  //product Addtional description
                    //GST Exchange Rate
                    if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !companyAccountPreferences.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                        obj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, row.getGstCurrencyRate());
                    }
                    jArr.put(obj);

                    if (!processedInv.contains(goodsReceipt.getID())) {

                        if (!StringUtil.isNullOrEmpty(invoiceNos)) {
                            invoiceNos += ", ";
                            invAmounts += ", ";
                            invTaxAmounts += ", ";
                            invAmountDues += ", ";
//                        invEnterAmounts += " , ";
                            invoicedates += ", ";
                            invoiceSalesPerson += ", ";
                        }
                        double invamount = 0, invtax = 0;

                        if (goodsReceipt.isIsOpeningBalenceInvoice() && !goodsReceipt.isNormalInvoice()) {
                            invamount = goodsReceipt.getOriginalOpeningBalanceAmount();
                        } else {
                            invamount = goodsReceipt.getVendorEntry().getAmount();//Invoice Sub Total
                        }

                        invtax = goodsReceipt.getTaxEntry() == null ? 0 : goodsReceipt.getTaxEntry().getAmount();

                        invoiceNos += goodsReceipt.getGoodsReceiptNumber();
                        invAmounts += invamount;
                        invTaxAmounts += invtax;
                        invAmountDues += goodsReceipt.getInvoiceamountdue();
//                            invEnterAmounts += (StringUtil.isNullOrEmpty(Double.toString(jObj.getDouble(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId))) ? "-" : Double.toString(jObj.getDouble(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId)));
                        invoicedates += goodsReceipt.getJournalEntry() != null ? goodsReceipt.getJournalEntry().getEntryDate() : goodsReceipt.getCreationDate();
                        invoiceSalesPerson += goodsReceipt.getMasterSalesPerson() == null ? " - " : goodsReceipt.getMasterSalesPerson().getValue();

                        invoicesubtotal += invamount - invtax;//Invoice Sub Total
                        invoicetotaltax += invtax; //Invoice Total Tax
                        invoicetotalamount += invamount; //Invoice Total Amount

                        processedInv.add(goodsReceipt.getID());
                    }
                }
            }
            
            //Calculate total tax if Global tax is applied
            if (dn.getTax() != null) {
                requestParams.put("transactiondate", dn.getJournalEntry().getEntryDate());
                requestParams.put("taxid", dn.getTax().getID());
                requestParams.put("companyid", companyid);
                KwlReturnObject result1 = accTaxObj.getTax(requestParams);
                List taxList = result1.getEntityList();
                Object[] taxObj = (Object[]) taxList.get(0);
                double taxPercent = taxObj[1] == null ? 0 : (Double) taxObj[1];

                totaltax = (subTotalWithDiscount * taxPercent) / 100;

            }

            summaryParams.put("lineLevelTaxesGST", lineLevelTaxesGST);
            summaryParams.put("lineLevelTaxAmountGST", lineLevelTaxAmountGST);
            summaryParams.put("lineLevelTaxBasicGST", lineLevelTaxBasicGST);
            summaryParams.put("invoicesubtotal", invoicesubtotal);
            summaryParams.put("invoicetotaltax", invoicetotaltax);
            summaryParams.put("invoicetotalamount", invoicetotalamount);
            summaryParams.put("invoiceNos", invoiceNos);
            summaryParams.put("invAmounts", invAmounts);
            summaryParams.put("invTaxAmounts", invTaxAmounts);
            summaryParams.put("invAmountDues", invAmountDues);
            summaryParams.put("invoicedates", invoicedates);
            summaryParams.put("totaltax", totaltax);
            summaryParams.put("totalDiscount", totalDiscount);
            summaryParams.put("subTotal", subtotal);
            summaryParams.put("subTotalWithDiscount", subTotalWithDiscount);
            summaryParams.put("subTotalWithTax", subTotalWithDiscount + totaltax);
            summaryParams.put("totalwithtax", subTotalWithDiscount + totaltax);
            summaryParams.put("salespersonvalue", invoiceSalesPerson);
            summaryParams.put("lineLevelTaxNames", lineLevelTaxNames);
            
            //getting summary data for Undercharged CN
            JSONObject summaryData = getDNSummaryData(requestJobj, summaryParams);

            jArr.put(summaryData);

        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    public JSONObject getDNSummaryData(JSONObject paramsJobj,Map<String,Object> requestParams) throws ServiceException, SessionExpiredException {
        JSONObject summaryData = new JSONObject();
        try {
            String allLineLevelTax = "", allLineLevelTaxAmount = "", allLineLevelTaxBasic = "";
            String globallevelcustomfields = "", globalleveldimensions = "", billAddr = "", shipAddr = "";
            double bascurrencyaccountsubtotal = 0, bascurrencyaccounttotaltax = 0, basecurrencyaccounttotalamount = 0;
            double bascurrencyinvoicesubtotal = 0, bascurrencyinvoicetotaltax = 0, basecurrencyinvoicetotalamount = 0;

            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) requestParams.get("companyAccountPreferences");
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) requestParams.get("extraCompanyPreferences");
            PdfTemplateConfig config = paramsJobj.has("pdfConfig") ? (PdfTemplateConfig) requestParams.get("pdfConfig") : null;
            Set<String> lineLevelTaxesGST = (HashSet<String>) requestParams.get("lineLevelTaxesGST");
            Map<String, Object> lineLevelTaxAmountGST = (Map<String, Object>) requestParams.get("lineLevelTaxAmountGST");
            Map<String, Object> lineLevelTaxBasicGST = (Map<String, Object>) requestParams.get("lineLevelTaxBasicGST");
            Map<String, Object> lineLevelTaxNames = (Map<String, Object>) requestParams.get("lineLevelTaxNames");

            DebitNote debitNote = (DebitNote) requestParams.get("debitNote");
            String companyid = paramsJobj.getString("companyid");
            String currencyid = (debitNote.getCurrency() == null) ? "" : debitNote.getCurrency().getCurrencyID();
            int countryid = 0;
            if (extraCompanyPreferences.getCompany() != null && extraCompanyPreferences.getCompany().getCountry() != null && !StringUtil.isNullOrEmpty(extraCompanyPreferences.getCompany().getCountry().getID())) {
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            double externalcurrency = debitNote.getExternalCurrencyRate();
            double revExchangeRate = 0.0;
            if (externalcurrency != 0) {
                revExchangeRate = 1 / externalcurrency;
            }
            double accountsubtotal = requestParams.containsKey("accountsubtotal") ? (Double)requestParams.get("accountsubtotal"):0;
            double accounttotaltax = requestParams.containsKey("accounttotaltax") ? (Double)requestParams.get("accounttotaltax"):0;
            double accounttotalamount = requestParams.containsKey("accounttotalamount") ? (Double)requestParams.get("accounttotalamount"):0;
            double invoicesubtotal = requestParams.containsKey("invoicesubtotal") ? (Double)requestParams.get("invoicesubtotal"):0;
            double invoicetotaltax = requestParams.containsKey("invoicetotaltax") ? (Double)requestParams.get("invoicetotaltax"):0;
            double invoicetotalamount = requestParams.containsKey("invoicetotalamount") ? (Double)requestParams.get("invoicetotalamount"):0;
            double totalwithtax = requestParams.containsKey("totalwithtax") ? (Double)requestParams.get("totalwithtax"):0;
            String GSTExchangeRate = requestParams.containsKey("GSTExchangeRate") ? (String)requestParams.get("GSTExchangeRate"):"";

            String templateSubtype = requestParams.containsKey("templateSubtype") ? (String)requestParams.get("templateSubtype"):"";

            /**
             * get customer/vendor title (Mr./Mrs.)
             */
            String customerOrVendorTitle = "", VATTInnumber = "", CSTTInNumber = "", custOrVendorPanNumber = "", gstin = "";
            if (debitNote.getCustomer() != null) {
                customerOrVendorTitle = debitNote.getCustomer().getTitle();
                VATTInnumber = debitNote.getCustomer().getVATTINnumber() != null ? debitNote.getCustomer().getVATTINnumber() : "";
                CSTTInNumber = debitNote.getCustomer().getCSTTINnumber() != null ? debitNote.getCustomer().getCSTTINnumber() : "";
                custOrVendorPanNumber = debitNote.getCustomer().getPANnumber() != null ? debitNote.getCustomer().getPANnumber() : "";
                gstin = debitNote.getCustomer().getGSTIN() != null ? debitNote.getCustomer().getGSTIN() : "";
            } else if (debitNote.getVendor() != null) {
                customerOrVendorTitle = debitNote.getVendor().getTitle();
                VATTInnumber = debitNote.getVendor().getVATTINnumber() != null ? debitNote.getVendor().getVATTINnumber() : "";
                CSTTInNumber = debitNote.getVendor().getCSTTINnumber() != null ? debitNote.getVendor().getCSTTINnumber() : "";
                custOrVendorPanNumber = debitNote.getVendor().getPANnumber() != null ? debitNote.getVendor().getPANnumber() : "";
                gstin = debitNote.getVendor().getGSTIN() != null ? debitNote.getVendor().getGSTIN() : "";
            }

            if (!StringUtil.isNullOrEmpty(customerOrVendorTitle)) {
                KwlReturnObject masterItemResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), customerOrVendorTitle);
                MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                customerOrVendorTitle = masterItem.getValue();
            }
            
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), currencyid);
            String netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(totalwithtax)), currency, countryLanguageId);
            /*
             * Get amount in indonesian words.
             */
            String indonesianAmountInWords = "";
            if (countryid == Constants.INDONESIAN_COUNTRY_ID) {
                KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(String.valueOf(totalwithtax)), indoCurrency);
            }

            
            summaryData.put("summarydata", true);
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only.");
            summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());

            String invoiceNos = (requestParams.containsKey("invoiceNos") && requestParams.get("invoiceNos") !=null) ? (String)requestParams.get("invoiceNos"):"";
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceNo_fieldTypeId, invoiceNos);
            String invAmounts = (requestParams.containsKey("invAmounts") && requestParams.get("invAmounts") !=null) ? (String)requestParams.get("invAmounts"):"";
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceAmount_fieldTypeId, invAmounts);
            String invTaxAmounts = (requestParams.containsKey("invTaxAmounts") && requestParams.get("invTaxAmounts") !=null) ? (String)requestParams.get("invTaxAmounts"):"";
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceTax_fieldTypeId, invTaxAmounts);
            summaryData.put(CustomDesignerConstants.InvoiceTax, invTaxAmounts);
            String invAmountDues = (requestParams.containsKey("invAmountDues") && requestParams.get("invAmountDues") !=null) ? (String)requestParams.get("invAmountDues"):"";
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceAmountDue_fieldTypeId, invAmountDues);
            String invEnterAmounts = (requestParams.containsKey("invEnterAmounts") && requestParams.get("invEnterAmounts") !=null) ? (String)requestParams.get("invEnterAmounts"):"";
            summaryData.put(CustomDesignerConstants.CNDN_InvoiceEnterAmount_fieldTypeId, invEnterAmounts);
            String invoicedates = (requestParams.containsKey("invoicedates") && requestParams.get("invoicedates") !=null) ? (String)requestParams.get("invoicedates"):"";
            summaryData.put(CustomDesignerConstants.CNDN_INvoiceDates_fieldTypeId, invoicedates);

            String accCodes = (requestParams.containsKey("accCodes") && requestParams.get("accCodes") !=null) ? (String)requestParams.get("accCodes"):"";
            summaryData.put(CustomDesignerConstants.CNDN_AccountCode_fieldTypeId, accCodes);
            String accNames = (requestParams.containsKey("accNames") && requestParams.get("accNames") !=null) ? (String)requestParams.get("accNames"):"";
            summaryData.put(CustomDesignerConstants.CNDN_Account_fieldTypeId, accNames);
            String accAmounts = (requestParams.containsKey("accAmounts") && requestParams.get("accAmounts") !=null) ? (String)requestParams.get("accAmounts"):"";
            summaryData.put(CustomDesignerConstants.CNDN_AccountAmount_fieldTypeId, accAmounts);
            
            String accTaxNames = (requestParams.containsKey("accTaxNames") && requestParams.get("accTaxNames") !=null) ? (String)requestParams.get("accTaxNames"):"";
            summaryData.put(CustomDesignerConstants.CNDN_AccountTax_fieldTypeId, accTaxNames);
            
            String accTaxAmounts = (requestParams.containsKey("accTaxAmounts") && requestParams.get("accTaxAmounts") !=null) ? (String)requestParams.get("accTaxAmounts"):"";
            summaryData.put(CustomDesignerConstants.CNDN_AccountTaxAmount_fieldTypeId, accTaxAmounts);
            
            String accAmountWithTaxes = (requestParams.containsKey("accAmountWithTaxes") && requestParams.get("accAmountWithTaxes") !=null) ? (String)requestParams.get("accAmountWithTaxes"):"";
            summaryData.put(CustomDesignerConstants.CNDN_AccountTotalAmount_fieldTypeId, accAmountWithTaxes);
            
            String accDescriptions = (requestParams.containsKey("accDescriptions") && requestParams.get("accDescriptions") !=null) ? (String)requestParams.get("accDescriptions"):"";
            summaryData.put(CustomDesignerConstants.CNDN_AccountDescription_fieldTypeId, accDescriptions);
            
            String accTaxPercent = (requestParams.containsKey("accTaxPercent") && requestParams.get("accTaxPercent") !=null) ? (String)requestParams.get("accTaxPercent"):"";
            summaryData.put(CustomDesignerConstants.SummaryTaxPercent, accTaxPercent);  //ERP-20872
            
            String accReason = (requestParams.containsKey("accReason") && requestParams.get("accReason") !=null) ? (String)requestParams.get("accReason"):"";
            summaryData.put(CustomDesignerConstants.CNDN_AccountReason_fieldTypeId, accReason);
            
            String accAmountExcludeGST = (requestParams.containsKey("accAmountExcludeGST") && requestParams.get("accAmountExcludeGST") !=null) ? (String)requestParams.get("accAmountExcludeGST"):"";
            summaryData.put(CustomDesignerConstants.CNDN_AccountAmountExcludeGST_fieldTypeId, accAmountExcludeGST);
            //COMPANY INFO
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyUEN_fieldTypeId, companyAccountPreferences.getCompanyUEN() != null ? companyAccountPreferences.getCompanyUEN() : "");
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyGRN_No_fieldTypeId, companyAccountPreferences.getGstNumber() != null ? companyAccountPreferences.getGstNumber() : "");

            if (extraCompanyPreferences.isIsNewGST()) { // for New gst check 
                if (!lineLevelTaxNames.isEmpty()) {
                    Iterator lineTax = lineLevelTaxNames.entrySet().iterator();
                    while (lineTax.hasNext()) {
                        Map.Entry tax = (Map.Entry) lineTax.next();
                        allLineLevelTax += tax.getKey();
                        allLineLevelTax += "!##";
                        double taxamount = (double) tax.getValue();
                        allLineLevelTaxAmount += tax.getValue().toString();
                        allLineLevelTaxAmount += "!##";
//                                TotalLineLevelTaxAmount += taxamount;
                    }
                }
                if (!StringUtil.isNullOrEmpty(allLineLevelTax)) {
                    allLineLevelTax = allLineLevelTax.substring(0, allLineLevelTax.length() - 3);
                }
                if (!StringUtil.isNullOrEmpty(allLineLevelTaxAmount)) {
                    allLineLevelTaxAmount = allLineLevelTaxAmount.substring(0, allLineLevelTaxAmount.length() - 3);
                }
            } else {
                /*
                 * Putting all line taxes and its information in summary data
                 * separated by !##
                 */
                for (String key : lineLevelTaxesGST) {
                    allLineLevelTax += key + "!##";
                    allLineLevelTaxAmount += lineLevelTaxAmountGST.get(key).toString() + "!##";
                    allLineLevelTaxBasic += lineLevelTaxBasicGST.get(key).toString() + "!##";
                }
            }
            summaryData.put(CustomDesignerConstants.AllLineLevelTax, allLineLevelTax);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxAmount, allLineLevelTaxAmount);
            summaryData.put(CustomDesignerConstants.AllLineLevelTaxBasic, allLineLevelTaxBasic);

            /*
             * All Global Section Custom Field and DImensions
             */
            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
            HashMap<String, Object> extraparams = new HashMap<String, Object>();
            DateFormat df = authHandler.getUserDateFormatterJson(paramsJobj);//User Date Formatter
            extraparams.put(Constants.companyid, companyid);
            extraparams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 1);
            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
            extraparams.put("billid", debitNote.getID());
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globallevelcustomfields = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }

            returnvalues.clear();
            //global level dimensionfields
            extraparams.put(Constants.customcolumn, 0);
            extraparams.put(Constants.customfield, 0);
            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
            returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
            if (returnvalues.containsKey("returnValue")) {
                globalleveldimensions = (String) returnvalues.get("returnValue");
            }
            if (returnvalues.containsKey("summaryData")) {
                summaryData = (JSONObject) returnvalues.get("summaryData");
            }

            //Details like company details
            CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);

            //Base Currency values in Subtotal, Total Tax and Total Amount  for Account
            if (externalcurrency != 0) {
                bascurrencyaccountsubtotal = accountsubtotal * revExchangeRate;  //Base currency account subtotal
                bascurrencyaccounttotaltax = accounttotaltax * revExchangeRate;//Base currency account total tax
                basecurrencyaccounttotalamount = accounttotalamount * revExchangeRate;//Base currency account total amount
            } else {
                bascurrencyaccountsubtotal = accountsubtotal;
                bascurrencyaccounttotaltax = accounttotaltax;
                basecurrencyaccounttotalamount = accounttotalamount;
            }

            //Base Currency values in Subtotal, Total Tax and Total Amount  for Invoice
            if (externalcurrency != 0) {
                bascurrencyinvoicesubtotal = invoicesubtotal * revExchangeRate;  //Base currency account subtotal
                bascurrencyinvoicetotaltax = invoicetotaltax * revExchangeRate;//Base currency account total tax
                basecurrencyinvoicetotalamount = invoicetotalamount * revExchangeRate;//Base currency account total amount
            } else {
                bascurrencyinvoicesubtotal = invoicesubtotal;
                bascurrencyinvoicetotaltax = invoicetotaltax;
                basecurrencyinvoicetotalamount = invoicetotalamount;
            }

            if (debitNote.getVendor() != null) {//for otherwise and Against Customer
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("vendorid", debitNote.getVendor().getID());
                addrRequestParams.put("companyid", companyid);
                KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                List<AddressDetails> addressResultList = addressResult.getEntityList();
                CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
                addrRequestParams.put("isBillingAddress", true);
                addrRequestParams.put("isDefaultAddress", true);
                addrRequestParams.put("isSeparator", true);
                billAddr = accountingHandlerDAOobj.getVendorAddress(addrRequestParams);
                addrRequestParams.put("isBillingAddress", false);
                shipAddr = accountingHandlerDAOobj.getVendorAddress(addrRequestParams);
                summaryData.put(CustomDesignerConstants.CustomerVendor_Code, debitNote.getVendor() != null ? (debitNote.getVendor().getAcccode() != null ? debitNote.getVendor().getAcccode() : "") : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_Term, debitNote.getVendor().getDebitTerm() != null ? debitNote.getVendor().getDebitTerm().getTermname() : "");
            } else {//for 3rd condition against Customer
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("customerid", debitNote.getCustomer().getID());
                addrRequestParams.put("companyid", companyid);
                KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
                List<AddressDetails> addressResultList = addressResult.getEntityList();
                CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
                addrRequestParams.put("isBillingAddress", true);
                addrRequestParams.put("isDefaultAddress", true);
                addrRequestParams.put("isSeparator", true);
                billAddr = accountingHandlerDAOobj.getCustomerAddress(addrRequestParams);
                addrRequestParams.put("isBillingAddress", false);
                shipAddr = accountingHandlerDAOobj.getCustomerAddress(addrRequestParams);
                summaryData.put(CustomDesignerConstants.CustomerVendor_Code, debitNote.getCustomer() != null ? (debitNote.getCustomer().getAcccode() != null ? debitNote.getCustomer().getAcccode() : "") : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_Term, debitNote.getCustomer().getCreditTerm() != null ? debitNote.getCustomer().getCreditTerm().getTermname() : "");
            }
            
            String debitterm = debitNote.getCustomer() == null ? String.valueOf(debitNote.getVendor().getDebitTerm().getTermdays()) : String.valueOf(debitNote.getCustomer().getCreditTerm().getTermdays());
            String venAccountNo = debitNote.getCustomer() == null ? (debitNote.getVendor().getAccount().getAccountName() != null ? debitNote.getVendor().getAccount().getAccountName() : "") : (debitNote.getCustomer().getAccount().getAccountName() != null ? debitNote.getCustomer().getAccount().getAccountName() : "");
            
            summaryData.put(CustomDesignerConstants.Createdby, debitNote.getCreatedby() != null ? debitNote.getCreatedby().getFullName() : "");
            summaryData.put(CustomDesignerConstants.Updatedby, debitNote.getModifiedby() != null ? debitNote.getModifiedby().getFullName() : "");
            
            summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, String.valueOf(authHandler.formattedAmount(requestParams.containsKey("subTotal") ? (Double)requestParams.get("subTotal"):0, companyid)));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, String.valueOf(authHandler.formattedAmount(requestParams.containsKey("totaltax") ? (Double)requestParams.get("totaltax"):0, companyid)));
            summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, String.valueOf(authHandler.formattedAmount(totalwithtax, companyid)));
            
            if (templateSubtype.equals(CustomDesignerConstants.OVERCHARGE_SUBTYPE) || templateSubtype.equals(CustomDesignerConstants.UNDERCHARGE_SUBTYPE)) {
                summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithDiscount_fieldTypeId, String.valueOf(authHandler.formattedAmount((requestParams.containsKey("subTotalWithDiscount") ? (Double)requestParams.get("subTotalWithDiscount"):0), companyid)));
                summaryData.put(CustomDesignerConstants.CustomDesignSubTotalWithTax_fieldTypeId, String.valueOf(authHandler.formattedAmount((requestParams.containsKey("subTotalWithTax") ? (Double)requestParams.get("subTotalWithTax"):0), companyid)));
                summaryData.put(CustomDesignerConstants.CustomDesignTotalDiscount_fieldTypeId, String.valueOf(authHandler.formattedAmount((requestParams.containsKey("totalDiscount") ? (Double)requestParams.get("totalDiscount"):0), companyid)));
            }
            
            summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, netinword + " Only.");
            summaryData.put(CustomDesignerConstants.CustomDesignCreditTerm_fieldTypeId, debitterm);
            summaryData.put(CustomDesignerConstants.Customer_AccountNo_fieldTypeId, venAccountNo);//Vendor Account Number
            summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());

            summaryData.put(CustomDesignerConstants.SrNo, 1);
            summaryData.put(CustomDesignerConstants.BillTo, billAddr);
            summaryData.put(CustomDesignerConstants.ShipTo, shipAddr);
            summaryData.put(CustomDesignerConstants.AccountSubTotal, authHandler.formattedAmount(accountsubtotal, companyid));//Account Sub Total
            summaryData.put(CustomDesignerConstants.AccountTotalTax, authHandler.formattedAmount(accounttotaltax, companyid)); //Account Total Tax
            summaryData.put(CustomDesignerConstants.AccountTotalAmount, authHandler.formattedAmount(accounttotalamount, companyid)); //Account Total Amount
            summaryData.put(CustomDesignerConstants.InvoiceSubTotal, authHandler.formattedAmount(invoicesubtotal, companyid));//Invoice SubTotal
            summaryData.put(CustomDesignerConstants.InvoiceTotalTax, authHandler.formattedAmount(invoicetotaltax, companyid)); //Invoice Total Tax
            summaryData.put(CustomDesignerConstants.InvoiceTotalAmount, authHandler.formattedAmount(invoicetotalamount, companyid)); //Invoice Total Amount
            summaryData.put(CustomDesignerConstants.BaseCurrencyAccountSubTotal, authHandler.formattedAmount(bascurrencyaccountsubtotal, companyid));//Base Currency Account Sub Total
            summaryData.put(CustomDesignerConstants.BaseCurrencyAccountTotalTax, authHandler.formattedAmount(bascurrencyaccounttotaltax, companyid)); //Base Currency Account Total Tax
            summaryData.put(CustomDesignerConstants.BaseCurrencyAccountTotalAmount, authHandler.formattedAmount(basecurrencyaccounttotalamount, companyid)); //Base Currency Account Total Amount
            summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, revExchangeRate);

            //GST Exchange Rate
            if (countryid == Integer.parseInt(Constants.SINGAPOREID) && !companyAccountPreferences.getCompany().getCurrency().getCurrencyID().equalsIgnoreCase(Constants.SGDID)) {
                summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, !StringUtil.isNullOrEmpty(GSTExchangeRate) ? GSTExchangeRate : "");
            } else {
                summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
            }

            //Sub-Total, Total tax, And Total Amount in Base currency for Invoice
            summaryData.put(CustomDesignerConstants.BaseCurrencyInvoiceSubTotal, authHandler.formattedAmount(bascurrencyinvoicesubtotal, companyid));//Base Currency Invoice SubTotal
            summaryData.put(CustomDesignerConstants.BaseCurrencyInvoiceTotalTax, authHandler.formattedAmount(bascurrencyinvoicetotaltax, companyid)); //Base Currency Invoice Total Tax
            summaryData.put(CustomDesignerConstants.BaseCurrencyInvoiceTotal, authHandler.formattedAmount(basecurrencyinvoicetotalamount, companyid)); //Base Currency Invoice Total Amount
            summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
            summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
            summaryData.put(CustomDesignerConstants.AllDimensions, "");
            summaryData.put(CustomDesignerConstants.AllLinelevelCustomFields, "");
            summaryData.put(CustomDesignerConstants.CUSTOMER_OR_VENDOR_TITLE, customerOrVendorTitle);
            summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO, VATTInnumber);
            summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, CSTTInNumber);
            summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber() != null ? extraCompanyPreferences.getVatNumber() : "");
            summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber() != null ? extraCompanyPreferences.getCstNumber() : "");
            summaryData.put(CustomDesignerConstants.CompanyPANNumber, extraCompanyPreferences.getPanNumber() != null ? extraCompanyPreferences.getPanNumber() : "");
            summaryData.put(CustomDesignerConstants.CUSTOMER_PAN_NO, custOrVendorPanNumber);
            summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, gstin);
        } catch (JSONException e) {
            Logger.getLogger(accGoodsReceiptCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return summaryData;
    }
}
