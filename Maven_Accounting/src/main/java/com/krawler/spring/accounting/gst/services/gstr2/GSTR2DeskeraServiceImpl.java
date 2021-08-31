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
package com.krawler.spring.accounting.gst.services.gstr2;

import com.krawler.common.admin.FieldComboData;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.Date;
import static com.krawler.common.util.Constants.JETYPE_ITC;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.LineLevelTerms;
import com.krawler.spring.accounting.entitygst.AccEntityGstDao;
import com.krawler.spring.accounting.entitygst.GSTR3BConstants;
import static com.krawler.spring.accounting.entitygst.GSTR3BConstants.DATAINDEX_DATE;
import static com.krawler.spring.accounting.entitygst.GSTR3BConstants.DATAINDEX_DOCTYPE;
import static com.krawler.spring.accounting.entitygst.GSTR3BConstants.DATAINDEX_IGSTAMOUNT;
import static com.krawler.spring.accounting.entitygst.GSTR3BConstants.DATAINDEX_TRANSACTION_NUMBER;
import com.krawler.spring.accounting.gst.dto.AT;
import com.krawler.spring.accounting.gst.dto.B2B;
import com.krawler.spring.accounting.gst.dto.CDNR;
import com.krawler.spring.accounting.gst.dto.GSTR2Submission;
import com.krawler.spring.accounting.gst.dto.HSN;
import com.krawler.spring.accounting.gst.dto.InvoiceDto;
import com.krawler.spring.accounting.gst.dto.ItemDetail;
import com.krawler.spring.accounting.gst.dto.ItemDto;
import com.krawler.spring.accounting.gst.dto.gstr2.ITCDetails;
import com.krawler.spring.accounting.gst.services.GSTRConstants;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class GSTR2DeskeraServiceImpl implements GSTR2Service {

    private GSTR2Dao gstr2Dao;
    private AccEntityGstDao accEntityGstDao;
    public void setgstr2Dao(GSTR2Dao gstr2Dao) {
        this.gstr2Dao = gstr2Dao;
    }

    public void setAccEntityGstDao(AccEntityGstDao accEntityGstDao) {
        this.accEntityGstDao = accEntityGstDao;
    }

    @Override
    public JSONObject getB2BInvoices(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        List<Object> invoiceList = gstr2Dao.getInvoiceDataWithDetailsInSql(reqParams);
        setB2BInvoiceDetailsList(array, invoiceList, reqParams);
//        System.out.println("B2B");
//        System.out.println(array.toString());
        return response.put("b2b", array);
    }

    public void setB2BInvoiceDetailsList(JSONArray array,List<Object> invoiceList,JSONObject reqParams) throws JSONException {
        JSONArray bulkData = createJsonForDataFetchedFromDB(invoiceList, reqParams);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        /**
         * create customer wise map i.e Return Map which contains customer id as
         * key and its related data array
         */
        Map<String, JSONArray> customerMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.vendorid);
        List<B2B> b2bList = new ArrayList<B2B>();
        B2B b2bObj = null;
        for (String custkey : customerMap.keySet()) {
            b2bObj = new B2B();
            JSONArray customerArr = customerMap.get(custkey);
            JSONObject custObj = customerArr.getJSONObject(0);
            /**
             * Create invoice map * i.e Return Map which contains Invoice id as
             * key and its related data array
             */
            List<InvoiceDto> invoiceDtos = new ArrayList<InvoiceDto>();
            Map<String, JSONArray> invoiceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(customerArr, GSTRConstants.invoiceid);
            for (String invkey : invoiceMap.keySet()) {

                JSONArray invArr = invoiceMap.get(invkey);
                JSONObject invObj = invArr.getJSONObject(0);
                InvoiceDto invoiceDto = new InvoiceDto();
                InvoiceDto transactionJSON = new InvoiceDto();
                invoiceDto.setInum(invObj.optString(GSTRConstants.invoicenumber));
                invoiceDto.setIdt(invObj.optString(GSTRConstants.entrydate));
                invoiceDto.setInvoiceId(invObj.optString(GSTRConstants.invoiceid, ""));
                invoiceDto.setVal(invObj.optDouble(GSTRConstants.invamountinbase));
                if ((!StringUtil.isNullOrEmpty("CustomerType")) && (reqParams.optString("CustomerType").equalsIgnoreCase(Constants.CUSTVENTYPE_Import))) {
                    invoiceDto.setPos(GSTRConstants.otherTerritory);
                } else {
                    invoiceDto.setPos(invObj.optString(GSTRConstants.pos));
                }
                transactionJSON.setInum(invObj.optString(GSTRConstants.invoicenumber));
                transactionJSON.setIdt(invObj.optString(GSTRConstants.entrydate));
                transactionJSON.setInvoiceId(invObj.optString(GSTRConstants.invoiceid, ""));
                transactionJSON.setVal(invObj.optDouble(GSTRConstants.invamountinbase));
                transactionJSON.setPos(invObj.optString(GSTRConstants.pos));
                /**
                 * Create Invoice details Map * i.e Return Map which contains
                 * Invoice detail id as key and its related data array
                 */
                Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(invArr, GSTRConstants.invoicedetailid);

                List<ItemDto> itemDtos = new ArrayList<ItemDto>();
                int count = 1;
                double inv_iamt=0,inv_camt=0,inv_samt=0,inv_csamt=0;
                for (String invdetailkey : invoiceDetailMap.keySet()) {
                    ItemDto itemDto = new ItemDto();
                    itemDto.setNum(count);
                    ItemDetail itemDetail = new ItemDetail();
                    itemDetail.setNum(count);
                    count++;
                    JSONArray invDetailArr = invoiceDetailMap.get(invdetailkey);
                    for (int index = 0; index < invDetailArr.length(); index++) {
                        JSONObject invdetailObj = invDetailArr.getJSONObject(index);
                        double rate = invdetailObj.optDouble(GSTRConstants.rate);
                        double qty = invdetailObj.optDouble(GSTRConstants.quantity);
                        double discount = 0d;
                        String discpercentage = invdetailObj.optString(GSTRConstants.discpercentage);
                        double discountvalue = invdetailObj.optDouble(GSTRConstants.discountvalue);
                        if (!StringUtil.isNullOrEmpty(discpercentage)) {
                            if (discpercentage.equalsIgnoreCase("T")) {
                                discount = discountvalue * (rate * qty) / 100;
                            } else {
                                discount = discountvalue;
                            }
                        }
                        double taxableamt = (qty * rate) - discount;
                        itemDetail.setTxval(taxableamt);
                        itemDetail.setRt(itemDetail.getRt()+invdetailObj.optDouble(GSTRConstants.taxrate));
                        itemDetail.setQty(qty);
                        itemDetail.setHsn_sc(invdetailObj.optString(GSTRConstants.hsncode));
                        itemDetail.setDesc(invdetailObj.optString(GSTRConstants.productdesc));
                        String term = invdetailObj.optString(GSTRConstants.defaultterm);

                        /**
                         * Iterate applied GST and put its Percentage and Amount
                         * accordingly
                         */
                        double termamount = invdetailObj.optDouble(GSTRConstants.termamount);
                        if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputIGST").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                            itemDetail.setIamt(termamount);
                            inv_iamt = inv_iamt + termamount;
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCGST").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            itemDetail.setCamt(termamount);
                             inv_camt = inv_camt + termamount;
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputSGST").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                            itemDetail.setSamt(termamount);
                            inv_samt = inv_samt + termamount;
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputUTGST").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                            itemDetail.setSamt(termamount);
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCESS").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                            itemDetail.setCsamt(termamount);
                            inv_csamt = inv_csamt + termamount;
                        }

                    }
                    itemDto.setItm_det(itemDetail);
                    itemDtos.add(itemDto);
                }
                
                if (reqParams.has("isGSTR2AComparisonWindow") && reqParams.optBoolean("isGSTR2AComparisonWindow", false)) {
                    count = 1;
                    List<ItemDto> transactionItmDtos = new ArrayList<>();
                    Map<Double, ItemDto> mapItmDto = new HashMap<>();
                    for (ItemDto itmDto : itemDtos) {
                        if (mapItmDto.containsKey(itmDto.getItm_det().getRt())) {
                            ItemDto transactionDto = mapItmDto.get(itmDto.getItm_det().getRt());
                            ItemDetail transactionItm_detail = transactionDto.getItm_det();
                            ITCDetails transactionItc_detail = transactionDto.getItc();
                            
                            ItemDetail itm_detail = itmDto.getItm_det();
                            transactionItm_detail.setCamt(authHandler.round((transactionItm_detail.getCamt() + itm_detail.getCamt()), 2));
                            transactionItm_detail.setCsamt(authHandler.round((transactionItm_detail.getCsamt() + itm_detail.getCsamt()), 2));
                            transactionItm_detail.setIamt(authHandler.round((transactionItm_detail.getIamt() + itm_detail.getIamt()), 2));
                            transactionItm_detail.setSamt(authHandler.round((transactionItm_detail.getSamt() + itm_detail.getSamt()), 2));
                            transactionItm_detail.setAd_amt(authHandler.round((transactionItm_detail.getAd_amt() + itm_detail.getAd_amt()), 2));
                            transactionItm_detail.setTxval(authHandler.round((transactionItm_detail.getTxval()+ itm_detail.getTxval()), 2));
                            transactionItc_detail.setTx_i(authHandler.round((transactionItc_detail.getTx_i() + itm_detail.getIamt()), 2));
                            transactionItc_detail.setTx_cs(authHandler.round((transactionItc_detail.getTx_cs() + itm_detail.getCsamt()), 2));
                            transactionItc_detail.setTx_s(authHandler.round((transactionItc_detail.getTx_s() + itm_detail.getSamt()), 2));
                            transactionItc_detail.setTx_c(authHandler.round((transactionItc_detail.getTx_c() + itm_detail.getCamt()), 2));
                            transactionDto.setItm_det(transactionItm_detail);
                            transactionDto.setItc(transactionItc_detail);
                            
                            mapItmDto.put(transactionItm_detail.getRt(), transactionDto);
                        } else {
                            ItemDto transactionDto = new ItemDto();
                            ItemDetail transactionItm_detail = new ItemDetail();
         		    ITCDetails transactionItc_detail = new ITCDetails();
                            ItemDetail itm_detail = itmDto.getItm_det();
                            transactionItm_detail.setCamt(authHandler.round(itm_detail.getCamt(), 2));
                            transactionItm_detail.setCsamt(authHandler.round(itm_detail.getCsamt(), 2));
                            transactionItm_detail.setIamt(authHandler.round(itm_detail.getIamt(), 2));
                            transactionItm_detail.setSamt(authHandler.round(itm_detail.getSamt(), 2));
                            transactionItm_detail.setAd_amt(authHandler.round(itm_detail.getAd_amt(), 2));
                            transactionItm_detail.setTxval(authHandler.round(itm_detail.getTxval(), 2));
                            transactionItm_detail.setUqc(itm_detail.getUqc());
                            transactionItm_detail.setHsn_sc(itm_detail.getHsn_sc());
                            transactionItm_detail.setQty(itm_detail.getQty());
                            transactionItm_detail.setRt(itm_detail.getRt());
                            transactionItc_detail.setTx_i(authHandler.round(transactionItm_detail.getIamt(), 2));
                            transactionItc_detail.setTx_cs(authHandler.round(transactionItm_detail.getCsamt(), 2));
                            transactionItc_detail.setTx_s(authHandler.round(transactionItm_detail.getSamt(), 2));
                            transactionItc_detail.setTx_c(authHandler.round(transactionItm_detail.getCamt(), 2));
                            transactionItc_detail.setElg("ip");
                            transactionDto.setItm_det(transactionItm_detail);
                            transactionDto.setItc(transactionItc_detail);
                            transactionDto.setNum(count++);
                            mapItmDto.put(transactionItm_detail.getRt(), transactionDto);
                        }
                    }
                    transactionItmDtos.addAll(mapItmDto.values());
                    transactionJSON.setItms(transactionItmDtos);
                }
                invoiceDto.setItms(itemDtos);
                invoiceDto.setInv_iamt(inv_iamt);
                invoiceDto.setInv_camt(inv_camt);
                invoiceDto.setInv_samt(inv_samt);
                invoiceDto.setInv_csamt(inv_csamt);                
                invoiceDto.setSupplierinvoiceno(invObj.optString(GSTRConstants.supplierinvoiceno, ""));
                invoiceDto.setVendorname(invObj.optString(GSTRConstants.vendorname, ""));
                transactionJSON.setInv_iamt(inv_iamt);
                transactionJSON.setInv_camt(inv_camt);
                transactionJSON.setInv_samt(inv_samt);
                transactionJSON.setInv_csamt(inv_csamt);                
                transactionJSON.setSupplierinvoiceno(invObj.optString(GSTRConstants.supplierinvoiceno, ""));
                transactionJSON.setVendorname(invObj.optString(GSTRConstants.vendorname, ""));
                if (reqParams.has("isGSTR2AComparisonWindow") && reqParams.optBoolean("isGSTR2AComparisonWindow", false)) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("supplierInvoiceNo", invObj.optString(GSTRConstants.supplierinvoiceno, ""));
                        Date idt = df.parse(invoiceDto.getIdt());
                        json.put("creationDate", idt);
                        json.put("transactionJson", new JSONObject(transactionJSON));
                        json.put("gstRegNumber", custObj.optString(GSTRConstants.gstin));
                        json.put("companyid", reqParams.optString(Constants.companyKey));
                        json.put("invoiceid", invObj.optString(GSTRConstants.invoiceid));
                        json.put("type", GSTR2Submission.B2B);
                        if (reqParams.has("month")) {
                            json.put("month", reqParams.optInt("month", 0));
                        }
                        if (reqParams.has("year")) {
                            json.put("year", reqParams.optInt("year", 0));
                        }
                        json.put("systemTransaction", true);
                        json.put("entityid", reqParams.optString("entityid", ""));
                        GSTR2Submission gstr2Submission = accEntityGstDao.saveOrGetGSTR2Submission(json);
                        invoiceDto.setGstrsubmissionid(gstr2Submission.getID());
                        invoiceDto.setSubmissionstatus(GSTR2Submission.getSubmissionStatus(gstr2Submission.getFlag()));
                    } catch (Exception ex) {
                        Logger.getLogger(GSTR2DeskeraServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
                    }
                }
                invoiceDtos.add(invoiceDto);
            }
            b2bObj.setCtin(custObj.optString(GSTRConstants.gstin));
            b2bObj.setInv(invoiceDtos);
            b2bList.add(b2bObj);
            JSONObject jSONObject = new JSONObject(b2bObj);
            array.put(jSONObject);
        }
    }
    
    @Override
    public JSONObject getImportofGoodsInvoices(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getImportofServicesBills(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getCDNInvoices(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        reqParams.put("cdnr", true);
        JSONObject params = new JSONObject(reqParams, JSONObject.getNames(reqParams));
//        params.put("isGSTINnull", false);
//        params.put("registrationType", Constants.GSTRegType_Regular+","+Constants.GSTRegType_Regular_ECommerce);
        params.put("CustomerType", Constants.CUSTVENTYPE_NA);
        params.put("taxClassType", FieldComboData.TaxClass_Percenatge+","+FieldComboData.TaxClass_Exempted+","+FieldComboData.TaxClass_Non_GST_Product+","+FieldComboData.TaxClass_ZeroPercenatge);
//        params.put("zerorated", false);
        List<Object> invoiceList = gstr2Dao.getCNDNWithInvoiceDetailsInSql(params);
        params.put("CustomerType", Constants.CUSTVENTYPE_NA + "," + Constants.CUSTVENTYPE_SEZ + "," + Constants.CUSTVENTYPE_SEZWOPAY);
        List<Object> dnList = gstr2Dao.getDNAgainstVendor(params);
        if (dnList != null && dnList.size() > 0) {
            invoiceList.addAll(dnList);
        }
        
        if (!StringUtil.isNullOrEmpty(reqParams.optString("cnentitycolnum", null)) && !StringUtil.isNullOrEmpty(reqParams.optString("cnentityValue", null))) {
            String dnentitycolnum = reqParams.optString("entitycolnum");
            String dnentityValue = reqParams.optString("entityValue");
            params.put("entitycolnum", reqParams.optString("cnentitycolnum"));
            params.put("entityValue", reqParams.optString("cnentityValue"));
            List<Object> cnAgainstVendor = gstr2Dao.getCNAgainstVendor(params);
            if (cnAgainstVendor != null && cnAgainstVendor.size() > 0) {
                invoiceList.addAll(cnAgainstVendor);
            }
            params.put("entitycolnum", dnentitycolnum);
            params.put("entityValue", dnentityValue);
        }
        JSONArray bulkData = createJsonForDataFetchedFromDB(invoiceList, reqParams);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        
        /**
         * create customer wise map i.e Return Map which contains customer id as
         * key and its related data array
         */
        Map<String, JSONArray> customerMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.vendorid);
        List<CDNR> b2bList = new ArrayList<CDNR>();
        CDNR cdnrObj = null;
        for (String custkey : customerMap.keySet()) {
            cdnrObj = new CDNR();
            JSONArray customerArr = customerMap.get(custkey);
            JSONObject custObj = customerArr.getJSONObject(0);
            /**
             * Create invoice map * i.e Return Map which contains Invoice id as
             * key and its related data array
             */
            List<InvoiceDto> invoiceDtos = new ArrayList<InvoiceDto>();
            Map<String, JSONArray> invoiceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(customerArr, GSTRConstants.invoiceid);
            for (String invkey : invoiceMap.keySet()) {
                JSONArray invArr = invoiceMap.get(invkey);
                JSONObject invObj = invArr.getJSONObject(0);
                InvoiceDto invoiceDto = new InvoiceDto();
                invoiceDto.setInum(invObj.optString(GSTRConstants.invoicenumber));
                invoiceDto.setIdt(invObj.optString(GSTRConstants.entrydate));
                invoiceDto.setNt_num(invObj.optString(GSTRConstants.cndnnumber));
                invoiceDto.setNt_dt(invObj.optString(GSTRConstants.cndndate));
                invoiceDto.setNtty("D");
                invoiceDto.setVal(invObj.optDouble(GSTRConstants.invamountinbase));
                invoiceDto.setPos(invObj.optString(GSTRConstants.pos));
                invoiceDto.setVendorname(invObj.optString(GSTRConstants.vendorname));
                invoiceDto.setInv_typ(!StringUtil.isNullOrEmpty(invObj.optString(GSTRConstants.Doc_Type))?"Debit Voucher":"Credit Voucher");
                InvoiceDto transactionJSON = new InvoiceDto();
                transactionJSON.setInum(invObj.optString(GSTRConstants.invoicenumber));
                transactionJSON.setIdt(invObj.optString(GSTRConstants.entrydate));
                transactionJSON.setNt_num(invObj.optString(GSTRConstants.cndnnumber));
                transactionJSON.setNt_dt(invObj.optString(GSTRConstants.cndndate));
                transactionJSON.setInvoiceId(invObj.optString(GSTRConstants.invoiceid, ""));
                transactionJSON.setVal(invObj.optDouble(GSTRConstants.invamountinbase));
                transactionJSON.setPos(invObj.optString(GSTRConstants.pos));
                /**
                 * Create Invoice details Map * i.e Return Map which contains
                 * Invoice detail id as key and its related data array
                 */
                Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(invArr, GSTRConstants.invoicedetailid);

                List<ItemDto> itemDtos = new ArrayList<ItemDto>();
                int count = 1;
                for (String invdetailkey : invoiceDetailMap.keySet()) {
                    ItemDto itemDto = new ItemDto();
                    itemDto.setNum(count);
                    ItemDetail itemDetail = new ItemDetail();
                    itemDetail.setNum(count);
                    count++;
                    JSONArray invDetailArr = invoiceDetailMap.get(invdetailkey);
                    for (int index = 0; index < invDetailArr.length(); index++) {
                        JSONObject invdetailObj = invDetailArr.getJSONObject(index);
                        double rate = invdetailObj.optDouble(GSTRConstants.rate);
                        double qty = invdetailObj.optDouble(GSTRConstants.quantity);
                        double discount = 0d;
                        int discpercentage = invObj.optInt(GSTRConstants.discpercentage);
                        double discountvalue = invObj.optDouble(GSTRConstants.discountvalue);
                        if (discpercentage == 1) {
                            discount = discountvalue * (rate * qty) / 100;
                        } else {
                            discount = discountvalue;
                        }
                        double taxableamt = (qty * rate) - discount;
                        itemDetail.setTxval(taxableamt);
                        itemDetail.setRt(itemDetail.getRt()+invdetailObj.optDouble(GSTRConstants.taxrate));
                        itemDetail.setQty(qty);
                        itemDetail.setHsn_sc(invdetailObj.optString(GSTRConstants.hsncode));
                        itemDetail.setDesc(invdetailObj.optString(GSTRConstants.productdesc));
                        String term = invdetailObj.optString(GSTRConstants.term);
                        String defaultterm = invdetailObj.optString(GSTRConstants.defaultterm);

                        /**
                         * Iterate applied GST and put its Percentage and Amount
                         * accordingly
                         */
                        double termamount = invdetailObj.optDouble(GSTRConstants.termamount);
                        if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputIGST").toString()) || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                            itemDetail.setIamt(termamount);
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCGST").toString()) || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            itemDetail.setCamt(termamount);
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputSGST").toString()) || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                            itemDetail.setSamt(termamount);
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputUTGST").toString()) || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                            itemDetail.setSamt(termamount);
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCESS").toString()) || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                            itemDetail.setCsamt(termamount);
                        }
                    }
                    itemDto.setItm_det(itemDetail);
                    itemDtos.add(itemDto);
                }
                
                if (reqParams.has("isGSTR2AComparisonWindow") && reqParams.optBoolean("isGSTR2AComparisonWindow", false)) {
                    count = 1;
                    List<ItemDto> transactionItmDtos = new ArrayList<>();
                    Map<Double, ItemDto> mapItmDto = new HashMap<>();
                    for (ItemDto itmDto : itemDtos) {
                        if (mapItmDto.containsKey(itmDto.getItm_det().getRt())) {
                            ItemDto transactionDto = mapItmDto.get(itmDto.getItm_det().getRt());
                            ItemDetail transactionItm_detail = transactionDto.getItm_det();
                            ITCDetails transactionItc_detail = transactionDto.getItc();
                            
                            ItemDetail itm_detail = itmDto.getItm_det();
                            transactionItm_detail.setCamt(authHandler.round((transactionItm_detail.getCamt() + itm_detail.getCamt()), 2));
                            transactionItm_detail.setCsamt(authHandler.round((transactionItm_detail.getCsamt() + itm_detail.getCsamt()), 2));
                            transactionItm_detail.setIamt(authHandler.round((transactionItm_detail.getIamt() + itm_detail.getIamt()), 2));
                            transactionItm_detail.setSamt(authHandler.round((transactionItm_detail.getSamt() + itm_detail.getSamt()), 2));
                            transactionItm_detail.setAd_amt(authHandler.round((transactionItm_detail.getAd_amt() + itm_detail.getAd_amt()), 2));
                            transactionItm_detail.setTxval(authHandler.round((transactionItm_detail.getTxval() + itm_detail.getTxval()), 2));
                            transactionItc_detail.setTx_i(authHandler.round((transactionItc_detail.getTx_i() + itm_detail.getIamt()), 2));
                            transactionItc_detail.setTx_cs(authHandler.round((transactionItc_detail.getTx_cs() + itm_detail.getCsamt()), 2));
                            transactionItc_detail.setTx_s(authHandler.round((transactionItc_detail.getTx_s() + itm_detail.getSamt()), 2));
                            transactionItc_detail.setTx_c(authHandler.round((transactionItc_detail.getTx_c() + itm_detail.getCamt()), 2));
                            transactionDto.setItm_det(transactionItm_detail);
                            transactionDto.setItc(transactionItc_detail);
                            
                            mapItmDto.put(transactionItm_detail.getRt(), transactionDto);
                        } else {
                            ItemDto transactionDto = new ItemDto();
                            ItemDetail transactionItm_detail = new ItemDetail();         
			    ITCDetails transactionItc_detail = new ITCDetails();
                            ItemDetail itm_detail = itmDto.getItm_det();
                            transactionItm_detail.setCamt(authHandler.round(itm_detail.getCamt(), 2));
                            transactionItm_detail.setCsamt(authHandler.round(itm_detail.getCsamt(), 2));
                            transactionItm_detail.setIamt(authHandler.round(itm_detail.getIamt(), 2));
                            transactionItm_detail.setSamt(authHandler.round(itm_detail.getSamt(), 2));
                            transactionItm_detail.setAd_amt(authHandler.round(itm_detail.getAd_amt(), 2));
                            transactionItm_detail.setTxval(authHandler.round(itm_detail.getTxval(), 2));
                            transactionItm_detail.setUqc(itm_detail.getUqc());
                            transactionItm_detail.setHsn_sc(itm_detail.getHsn_sc());
                            transactionItm_detail.setQty(itm_detail.getQty());
                            transactionItm_detail.setRt(itm_detail.getRt());
                            transactionItc_detail.setTx_i(authHandler.round(transactionItm_detail.getIamt(), 2));
                            transactionItc_detail.setTx_cs(authHandler.round(transactionItm_detail.getCsamt(), 2));
                            transactionItc_detail.setTx_s(authHandler.round(transactionItm_detail.getSamt(), 2));
                            transactionItc_detail.setTx_c(authHandler.round(transactionItm_detail.getCamt(), 2));
                            transactionItc_detail.setElg("ip");
                            transactionDto.setItm_det(transactionItm_detail);
                            transactionDto.setItc(transactionItc_detail);
                            transactionDto.setNum(count++);
                            mapItmDto.put(transactionItm_detail.getRt(), transactionDto);
                        }
                    }
                    transactionItmDtos.addAll(mapItmDto.values());
                    transactionJSON.setItms(transactionItmDtos);
                }
                invoiceDto.setItms(itemDtos);
                if (reqParams.has("isGSTR2AComparisonWindow") && reqParams.optBoolean("isGSTR2AComparisonWindow", false)) {
                    try {
                        JSONObject json = new JSONObject();
                        json.put("supplierInvoiceNo", invObj.optString(GSTRConstants.cndnnumber, ""));
                        Date idt = df.parse(invoiceDto.getIdt());
                        json.put("creationDate", idt);
                        json.put("transactionJson", new JSONObject(transactionJSON));
                        json.put("gstRegNumber", custObj.optString(GSTRConstants.gstin));
                        json.put("companyid", reqParams.optString(Constants.companyKey));
                        json.put("invoiceid", invObj.optString(GSTRConstants.cnid));
                        json.put("type", GSTR2Submission.CDNR);
                        if (reqParams.has("month")) {
                            json.put("month", reqParams.optInt("month", 0));
                        }
                        if (reqParams.has("year")) {
                            json.put("year", reqParams.optInt("year", 0));
                        }
                        json.put("systemTransaction", true);
                        json.put("entityid", reqParams.optString("entityid", ""));
                        GSTR2Submission gstr2Submission = accEntityGstDao.saveOrGetGSTR2Submission(json);
                        invoiceDto.setGstrsubmissionid(gstr2Submission.getID());
                        invoiceDto.setSubmissionstatus(GSTR2Submission.getSubmissionStatus(gstr2Submission.getFlag()));
                    } catch (Exception ex) {
                        Logger.getLogger(GSTR2DeskeraServiceImpl.class.getName()).log(Level.WARNING, ex.getMessage());
                    }
                }
                invoiceDtos.add(invoiceDto);
            }
            cdnrObj.setCtin(custObj.optString(GSTRConstants.gstin));
            cdnrObj.setNt(invoiceDtos);
            b2bList.add(cdnrObj);
            JSONObject jSONObject = new JSONObject(cdnrObj);
            array.put(jSONObject);
        }
//        System.out.println("merged");
//        System.out.println(array.toString());
        return response.put("cndr", array);
    }

    /**
     * Function to set Credit Note Debit Note data for purchase side to use
     * further for GST computation report
     * @param array
     * @param invoiceList
     * @param reqParams
     * @throws JSONException
     */
    public void setDNCNDetailsList(JSONArray array, List<Object> invoiceList, JSONObject reqParams) throws JSONException {
        JSONArray bulkData = createJsonForDataFetchedFromDB(invoiceList, reqParams);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");

        /**
         * create customer wise map i.e Return Map which contains customer id as
         * key and its related data array
         */
        Map<String, JSONArray> customerMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.vendorid);
        List<CDNR> b2bList = new ArrayList<CDNR>();
        CDNR cdnrObj = null;
        for (String custkey : customerMap.keySet()) {
            cdnrObj = new CDNR();
            JSONArray customerArr = customerMap.get(custkey);
            JSONObject custObj = customerArr.getJSONObject(0);
            /**
             * Create invoice map * i.e Return Map which contains Invoice id as
             * key and its related data array
             */
            List<InvoiceDto> invoiceDtos = new ArrayList<InvoiceDto>();
            Map<String, JSONArray> invoiceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(customerArr, GSTRConstants.invoiceid);
            for (String invkey : invoiceMap.keySet()) {
                JSONArray invArr = invoiceMap.get(invkey);
                JSONObject invObj = invArr.getJSONObject(0);
                InvoiceDto invoiceDto = new InvoiceDto();
                invoiceDto.setInum(invObj.optString(GSTRConstants.invoicenumber));
                invoiceDto.setIdt(invObj.optString(GSTRConstants.entrydate));
                invoiceDto.setNt_num(invObj.optString(GSTRConstants.cndnnumber));
                invoiceDto.setNt_dt(invObj.optString(GSTRConstants.cndndate));
                invoiceDto.setNtty("D");
                invoiceDto.setVal(invObj.optDouble(GSTRConstants.invamountinbase));
                invoiceDto.setPos(invObj.optString(GSTRConstants.pos));
                invoiceDto.setVendorname(invObj.optString(GSTRConstants.vendorname));
                invoiceDto.setInv_typ(!StringUtil.isNullOrEmpty(invObj.optString(GSTRConstants.Doc_Type)) ? "Debit Voucher" : "Credit Voucher");
                InvoiceDto transactionJSON = new InvoiceDto();
                transactionJSON.setInum(invObj.optString(GSTRConstants.invoicenumber));
                transactionJSON.setIdt(invObj.optString(GSTRConstants.entrydate));
                transactionJSON.setNt_num(invObj.optString(GSTRConstants.cndnnumber));
                transactionJSON.setNt_dt(invObj.optString(GSTRConstants.cndndate));
                transactionJSON.setInvoiceId(invObj.optString(GSTRConstants.invoiceid, ""));
                transactionJSON.setVal(invObj.optDouble(GSTRConstants.invamountinbase));
                transactionJSON.setPos(invObj.optString(GSTRConstants.pos));
                /**
                 * Create Invoice details Map * i.e Return Map which contains
                 * Invoice detail id as key and its related data array
                 */
                Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(invArr, GSTRConstants.invoicedetailid);

                List<ItemDto> itemDtos = new ArrayList<ItemDto>();
                int count = 1;
                for (String invdetailkey : invoiceDetailMap.keySet()) {
                    ItemDto itemDto = new ItemDto();
                    itemDto.setNum(count);
                    ItemDetail itemDetail = new ItemDetail();
                    itemDetail.setNum(count);
                    count++;
                    JSONArray invDetailArr = invoiceDetailMap.get(invdetailkey);
                    double igst = 0;
                    double cgst = 0;
                    double sgst = 0;
                    double csgst = 0;
                    Set cntermset = new HashSet();
                    for (int index = 0; index < invDetailArr.length(); index++) {
                        JSONObject invdetailObj = invDetailArr.getJSONObject(index);
                        double rate = invdetailObj.optDouble(GSTRConstants.rate);
                        double qty = invdetailObj.optDouble(GSTRConstants.quantity);
                        double discount = 0d;
                        int discpercentage = invObj.optInt(GSTRConstants.discpercentage);
                        double discountvalue = invObj.optDouble(GSTRConstants.discountvalue);
                        if (discpercentage == 1) {
                            discount = discountvalue * (rate * qty) / 100;
                        } else {
                            discount = discountvalue;
                        }
                        double taxableamt = (qty * rate) - discount;
                        if (invoiceDto.getInv_typ().equalsIgnoreCase("Credit Voucher")) {
                            itemDetail.setTxval(taxableamt);
                        } else {
                            itemDetail.setTxval(-taxableamt);
                        }
                        itemDetail.setRt(itemDetail.getRt() + invdetailObj.optDouble(GSTRConstants.taxrate));
                        itemDetail.setQty(qty);
                        itemDetail.setHsn_sc(invdetailObj.optString(GSTRConstants.hsncode));
                        itemDetail.setDesc(invdetailObj.optString(GSTRConstants.productdesc));
                        String term = invdetailObj.optString(GSTRConstants.term);
                        String defaultterm = invdetailObj.optString(GSTRConstants.defaultterm);
                        if (cntermset.add(defaultterm)) {
                        /**
                         * Iterate applied GST and put its Percentage and Amount
                         * accordingly
                         */
                        double termamount = invdetailObj.optDouble(GSTRConstants.termamount);
                        if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputIGST").toString()) || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                            igst = termamount;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCGST").toString()) || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            cgst = termamount;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputSGST").toString()) || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                            sgst = termamount;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputUTGST").toString()) || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                            sgst = termamount;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCESS").toString()) || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                            csgst = termamount;
                        }
                        }
                    }
                    if (invoiceDto.getInv_typ().equalsIgnoreCase("Credit Voucher")) {
                        itemDetail.setIamt(igst);
                        itemDetail.setCamt(cgst);
                        itemDetail.setSamt(sgst);
                        itemDetail.setCsamt(csgst);
                    } else {
                        itemDetail.setIamt(-igst);
                        itemDetail.setCamt(-cgst);
                        itemDetail.setSamt(-sgst);
                        itemDetail.setCsamt(-csgst);
                    }
                    itemDto.setItm_det(itemDetail);
                    itemDtos.add(itemDto);
                }
                invoiceDto.setItms(itemDtos);
                invoiceDtos.add(invoiceDto);
            }
            cdnrObj.setCtin(custObj.optString(GSTRConstants.gstin));
            cdnrObj.setNt(invoiceDtos);
            b2bList.add(cdnrObj);
            JSONObject jSONObject = new JSONObject(cdnrObj);
            array.put(jSONObject);
        }
    }
    @Override
    public JSONObject getNilRatedSupplies(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getTaxliabilityunderReverseChargeSummary(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getTaxPaidUnderReverseCharge(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getHSNSummaryofInwardsupplies(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getB2BUnregisteredInvoice(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getITCReversalDetails(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getCDNUnregisteredData(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JSONObject getGSTR2Summary(JSONObject json) throws JSONException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private JSONArray createJsonForDataFetchedFromDB(List<Object> invoiceList, JSONObject params) throws JSONException {
        JSONArray bulkData = new JSONArray();
        int index = 0;
        for (Object object : invoiceList) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            index = 0;
           jSONObject.put(GSTRConstants.vendorid, data[0].toString());
            jSONObject.put(GSTRConstants.gstin, data[1].toString());
            jSONObject.put(GSTRConstants.invoiceid, data[2] != null ? data[2].toString() : "");
            jSONObject.put(GSTRConstants.invoicenumber, data[3] != null ? data[3].toString() : "");
            jSONObject.put(GSTRConstants.jeid, data[4] != null ? data[4].toString() : "");
            jSONObject.put(GSTRConstants.entrydate, data[5] != null ? data[5].toString() : "");
            jSONObject.put(GSTRConstants.invoicedetailid, data[6] != null ? data[6].toString() : "");
            jSONObject.put(GSTRConstants.rate, (Double) data[7]);
            if (data[8] instanceof BigDecimal) {
                jSONObject.put(GSTRConstants.quantity, ((BigDecimal) data[8]).doubleValue());
            } else {
                jSONObject.put(GSTRConstants.quantity, (Double) data[8]);
            }
            jSONObject.put(GSTRConstants.productid, data[9].toString());
            jSONObject.put(GSTRConstants.term, data[10]!=null?data[10].toString():"");
            jSONObject.put(GSTRConstants.termamount, data[11]!=null?(Double) data[11]:0);
            jSONObject.put(GSTRConstants.invamountinbase, (Double) data[12]);
            jSONObject.put(GSTRConstants.hsncode, data[13]!=null?data[13].toString():"");
            jSONObject.put(GSTRConstants.pos, data[14]!=null?data[14].toString():"");
            jSONObject.put(GSTRConstants.productdesc, (String)data[15]);
            jSONObject.put(GSTRConstants.posid, data[16]!=null?data[16].toString():"");
            jSONObject.put(GSTRConstants.taxrate, data[17]!=null?(Double) data[17]:0);
            jSONObject.put(GSTRConstants.defaultterm, data[18]!=null?(String) data[18]:"");
            /**
             * Additional data come only in case of Return
             */
            if (params.optBoolean("cdnr") || params.optBoolean("cdnur")) {
                jSONObject.put(GSTRConstants.cndnnumber, (String) data[19]);
                jSONObject.put(GSTRConstants.cndndate, data[20].toString());
                if (data[21] instanceof BigInteger) {
                    jSONObject.put(GSTRConstants.discpercentage, ((BigInteger) data[21]).intValue());
                } else {
                    jSONObject.put(GSTRConstants.discpercentage, data[21] != null ? (Integer) data[21] : "");
                }
                if (data[22] instanceof BigDecimal) {
                    jSONObject.put(GSTRConstants.discountvalue, ((BigDecimal) data[22]).doubleValue());
                } else {
                    jSONObject.put(GSTRConstants.discountvalue, data[22] != null ? (Double) data[22] : 0);
                }
                jSONObject.put(GSTRConstants.cnid, data[23].toString());
                jSONObject.put(GSTRConstants.cnamountinbase, (Double) data[24]);
                jSONObject.put(GSTRConstants.vendorname, data[25] != null ? (String) data[25] : "");
                if(data.length > 26){
                jSONObject.put(GSTRConstants.Doc_Type,data[26]!= null ? (String) data[26] : "");
                }
            } else {
                jSONObject.put(GSTRConstants.discpercentage, data[19] != null ? (Character) data[19] : "");
                jSONObject.put(GSTRConstants.discountvalue, data[20] != null ? (Double) data[20] : 0);
                if (data.length > 20) {
                    /**
                     * Supplier Invoice Number for GoodsReceipt.
                     */
                    jSONObject.put(GSTRConstants.supplierinvoiceno, data[21] != null ? (String) data[21] : "");
                }
                if (data.length > 21) {
                    /**
                     * Supplier Invoice Number for GoodsReceipt.
                     */
                    jSONObject.put(GSTRConstants.vendorname, data[22] != null ? (String) data[22] : "");
                }
                }
            bulkData.put(jSONObject);
        }
        return bulkData;
    }
    public JSONObject getHSNSummarydetails(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        reqParams.put("hsn", true);
        List<Object> receiptList = gstr2Dao.getHSNWiseInvoiceDataWithDetailsInSql(reqParams);
        JSONArray bulkData = createHSNJsonForDataFetchedFromDB(receiptList, reqParams);
        Map<String, JSONArray> hsnMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.hsnid);
        for (String hsnkey : hsnMap.keySet()) {
            JSONArray hsnArr = hsnMap.get(hsnkey);
            Map<String, JSONArray> uqcMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(hsnArr, GSTRConstants.UQC);
            for (String uqckey : uqcMap.keySet()) {
                HSN hsn = new HSN();
                List<ItemDetail> itemDetails = new ArrayList();
                JSONArray uqcArr = uqcMap.get(uqckey);
                Map<String, JSONArray> invMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(uqcArr, GSTRConstants.invoiceid);
                for (String invoiceKey : invMap.keySet()) {
                    double camt = 0d;
                    double samt = 0d;
                    double iamt = 0d;
                    double csamt = 0d;
                    double totalinvoiceamt = 0d;
                    double totaltaxableamt = 0d;
                    double totalqty = 0d;
                    JSONArray invArr = invMap.get(invoiceKey);
                    ItemDetail itemDetail = new ItemDetail();
                    Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(invArr, GSTRConstants.invoicedetailid);
                    for (String invoiceDetailKey : invoiceDetailMap.keySet()) {
                    JSONArray invDetailArr = invoiceDetailMap.get(invoiceDetailKey);
                    JSONObject invDetailobj = invDetailArr.getJSONObject(0);
                    double rate = invDetailobj.optDouble(GSTRConstants.rate);
                    double qty = invDetailobj.optDouble(GSTRConstants.quantity);
                    double discount = 0d;
                    String discpercentage = invDetailobj.optString(GSTRConstants.discpercentage);
                    double discountvalue = invDetailobj.optDouble(GSTRConstants.discountvalue);
                    if (!StringUtil.isNullOrEmpty(discpercentage)) {
                        if (discpercentage.equalsIgnoreCase("T")) {
                            discount = discountvalue * (rate * qty) / 100;
                        } else {
                        discount = discountvalue;
                    }
                }
                double taxableamt = (qty * rate) - discount;
                totaltaxableamt += taxableamt;
                totalinvoiceamt += taxableamt;
                totalqty += qty;
                for (int index = 0; index < invDetailArr.length(); index++) {
                    JSONObject invdetailObj = invDetailArr.getJSONObject(index);
                    String defaultterm = invdetailObj.optString(GSTRConstants.defaultterm);

                    /**
                     * Iterate applied GST and put its Percentage and Amount
                     * accordingly
                     */
                    double termamount = invdetailObj.optDouble(GSTRConstants.termamount, 0);
                    totalinvoiceamt += termamount;
                    rate += invdetailObj.optDouble(GSTRConstants.taxrate);
                    /**
                     * calculate tax amount by considering partial case
                     */
                    if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputIGST").toString())) {
                        iamt += termamount;
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCGST").toString())) {
                            camt += termamount;
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputSGST").toString())) {
                            samt += termamount;
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputUTGST").toString())) {
                            samt += termamount;
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCESS").toString())) {
                            csamt += termamount;
                        }
                    }
                    itemDetail.setRt(rate);
                    itemDetail.setHsn_sc(invDetailobj.optString(GSTRConstants.hsncode));
                    itemDetail.setDesc(invDetailobj.optString(GSTRConstants.Description));
                    itemDetail.setUqc(invDetailobj.optString(GSTRConstants.UQC));
                    itemDetail.setInvnum(invDetailobj.optString(GSTRConstants.invoicenumber));
                }
                itemDetail.setCamt(camt);
                itemDetail.setSamt(samt);
                itemDetail.setQty(totalqty);
                itemDetail.setTxval(totaltaxableamt);
                itemDetail.setVal(totalinvoiceamt);
                itemDetail.setIamt(iamt);
                itemDetail.setCsamt(csamt);
                itemDetails.add(itemDetail);
            }
                hsn.setData(itemDetails);
                JSONObject jSONObject = new JSONObject(hsn);
                array.put(jSONObject);
            }

        }
        /**
         * Get count of no invoices
         */
        Map<String, JSONArray> invMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.invoiceid);
        response.put("count", invMap.size());
        return response.put("hsn", array);
    }

    private JSONArray createHSNJsonForDataFetchedFromDB(List<Object> invoiceList, JSONObject params) throws JSONException {
        JSONArray bulkData = new JSONArray();
        for (Object object : invoiceList) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(GSTRConstants.invoiceid, data[0].toString());
            jSONObject.put(GSTRConstants.invoicenumber, data[1].toString());
            jSONObject.put(GSTRConstants.invoicedetailid, data[2].toString());
            jSONObject.put(GSTRConstants.rate, (Double) data[3]);
            jSONObject.put(GSTRConstants.quantity, (Double) data[4]);
            jSONObject.put(GSTRConstants.term, data[5] != null ? data[5].toString() : "");
            jSONObject.put(GSTRConstants.termamount, (Double) data[6]);
            jSONObject.put(GSTRConstants.invamountinbase, (Double) data[7]);
            jSONObject.put(GSTRConstants.hsncode, data[8]!=null ? data[8].toString():"");
            jSONObject.put(GSTRConstants.taxrate, (Double) data[9]);
            jSONObject.put(GSTRConstants.defaultterm, data[10] != null ? (String) data[10] : "");
            jSONObject.put(GSTRConstants.hsnid, data[11]!=null ? (String) data[11] : "");
            jSONObject.put(GSTRConstants.discpercentage, data[12] != null ? (Character) data[12] : "");
            jSONObject.put(GSTRConstants.discountvalue, data[13] != null ? (Double) data[13] : 0);
            bulkData.put(jSONObject);
        }
        return bulkData;
    }
    /**
     * Desc : Tax liability arising on account of receipt of consideration for
     * which invoices have not been issued in the same tax period.
     *
     * @param gstin
     * @param ret_period
     * @return
     * @throws JSONException
     */
    public JSONObject getRCMOnAdvance(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        reqParams.put("atadj", false);
        reqParams.put("at", true);
        int noofdocument = 0;
        double taxableamt = 0d;
        DateFormat df = null;
        Date enddate = null;
        Date startdate = null;
        if (reqParams.opt("userdf") != null) {
            df = (DateFormat) reqParams.opt("userdf");
        }
        try {
            enddate = df.parse(reqParams.optString("enddate"));
            startdate = df.parse(reqParams.optString("startdate"));
        } catch (ParseException ex) {
            Logger.getLogger(GSTR2DeskeraServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<Object> receiptList = gstr2Dao.getAdvanceDetailsInSql(reqParams);
        JSONArray bulkData = createJsonForAdvanceDataFetchedFromDB(receiptList, reqParams);
        Map<String, JSONArray> posMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.posid);
        for (String poskey : posMap.keySet()) {
            /**
             * Get POS wise data for all advance
             */
            AT at = new AT();
            JSONArray posArr = posMap.get(poskey);
            JSONObject posObj = posArr.getJSONObject(0);
            at.setPos(posObj.optString(GSTRConstants.pos));
            double camt = 0d;
            double samt = 0d;
            double iamt = 0d;
            double csamt = 0d;
            double advanceamt = 0d;
            ItemDetail itemDetail = new ItemDetail();
            List<ItemDetail> itemDetails = new ArrayList();
            Map<String, JSONArray> advanceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(posArr, GSTRConstants.receiptadvanceid);
            for (String advdetailkey : advanceMap.keySet()) {
                /**
                 * Iterate all rows for advance details
                 */
                JSONArray adDetailArr = advanceMap.get(advdetailkey);
                double taxableamount = 0;
                double adjustamount = 0d;
                for (int i = 0; i < adDetailArr.length(); i++) {
                    JSONObject advObj = adDetailArr.getJSONObject(i);
                    if (i == 0) {
                        /**
                         * Need to take for only one row only because it repeat
                         * for other rows
                         */
                        taxableamount = advObj.optDouble(GSTRConstants.receiptamount);
                    }
                    String defaultterm = advObj.optString(GSTRConstants.defaultterm);
                    if (advObj.opt(GSTRConstants.receiptlinkdate) != null && (startdate.before((Date) advObj.opt(GSTRConstants.receiptlinkdate))
                            && enddate.after((Date) advObj.opt(GSTRConstants.receiptlinkdate)) || startdate.equals((Date) advObj.opt(GSTRConstants.receiptlinkdate))
                            || enddate.equals((Date) advObj.opt(GSTRConstants.receiptlinkdate)))) {
                        /**
                         * If Advance adjust in the same month then only
                         * consider adjust amount in calculations
                         */
                        if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())
                                || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            adjustamount += advObj.optDouble(GSTRConstants.adjustedamount);
                        }
                    }
                }
                itemDetail.setTxval(taxableamount - adjustamount);
                if (taxableamount - adjustamount > 0) {
                    noofdocument++;
                } else {
                    /**
                     * If Advance totally utilized in the same month then no
                     * need to consider such document
                     */
                    continue;
                }
                taxableamt += taxableamount - adjustamount;
                advanceamt += taxableamount - adjustamount;
                Set<String> termSet = new HashSet();
                double rate = 0d;
                for (int index = 0; index < adDetailArr.length(); index++) {
                    JSONObject invdetailObj = adDetailArr.getJSONObject(index);
                    String defaultterm = invdetailObj.optString(GSTRConstants.defaultterm);
                    if (termSet.add(defaultterm)) {
                        /**
                         * Avoid duplicate entries which come due to join
                         */
                        double termamount = (taxableamount - adjustamount) * invdetailObj.optDouble(GSTRConstants.taxrate) / 100;
                        rate += invdetailObj.optDouble(GSTRConstants.taxrate);
                        if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                            iamt += termamount;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            camt += termamount;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                            samt += termamount;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                            samt += termamount;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                            csamt += termamount;
                        }
                    }
                }

                itemDetail.setRt(rate);
            }
            itemDetail.setCamt(camt);
            itemDetail.setSamt(samt);
            itemDetail.setAd_amt(advanceamt);
            itemDetail.setIamt(iamt);
            itemDetail.setCsamt(csamt);
            itemDetails.add(itemDetail);
            at.setItms(itemDetails);
            JSONObject jSONObject = new JSONObject(at);
            array.put(jSONObject);
        }
//        System.out.println("merged");
//        System.out.println(array.toString());
        response.put("count", noofdocument);
        response.put("taxableamt", taxableamt);
        return response.put("at", array);
    }

    /**
     * @desc : Adjustment of tax liability for tax already paid on advance
     * receipt of consideration and invoices issued in the current period for
     * the supplies.
     * @param reqParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getTaxPaidOnAdvance(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        reqParams.remove("at");
        reqParams.put("atadj", true);
        int noofdocument = 0;
        double taxableamt = 0d;
        List<Object> receiptList = gstr2Dao.getAdvanceDetailsInSql(reqParams);
        JSONArray bulkData = createJsonForAdvanceDataFetchedFromDB(receiptList, reqParams);
        Map<String, JSONArray> posMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.posid);
        for (String poskey : posMap.keySet()) {
            AT at = new AT();
            JSONArray posArr = posMap.get(poskey);
            JSONObject posObj = posArr.getJSONObject(0);
            at.setPos(posObj.optString(GSTRConstants.pos));
            at.setVendorName(posObj.optString(GSTRConstants.vendorname));
            at.setGstin(posObj.optString(GSTRConstants.gstin));
            double camt = 0d;
            double samt = 0d;
            double iamt = 0d;
            double csamt = 0d;
            double advanceamt = 0d;
            String adt="";
            String anum="";
            ItemDetail itemDetail = new ItemDetail();
            List<ItemDetail> itemDetails = new ArrayList();
            Map<String, JSONArray> advanceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(posArr, GSTRConstants.receiptadvanceid);
            for (String advdetailkey : advanceMap.keySet()) {
                noofdocument++;
                double rate = 0d;
                JSONArray adDetailArr = advanceMap.get(advdetailkey);
                Set<String> termSet = new HashSet();
                for (int index = 0; index < adDetailArr.length(); index++) {
                    JSONObject invdetailObj = adDetailArr.getJSONObject(index);
                    String defaultterm = invdetailObj.optString(GSTRConstants.defaultterm);

                    /**
                     * Iterate applied GST and put its Percentage and Amount
                     * accordingly
                     */
                    if (termSet.add(defaultterm)) {
                        rate += invdetailObj.optDouble(GSTRConstants.taxrate);
                    }
                    double termamount = (invdetailObj.optDouble(GSTRConstants.adjustedamount)) * invdetailObj.optDouble(GSTRConstants.taxrate) / 100;

                    /**
                     * calculate tax amount by considering partial case
                     */
                    if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                        iamt += termamount;
                        advanceamt += invdetailObj.optDouble(GSTRConstants.adjustedamount);
                        taxableamt += invdetailObj.optDouble(GSTRConstants.adjustedamount);
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                        camt += termamount;
                        advanceamt += invdetailObj.optDouble(GSTRConstants.adjustedamount);
                        taxableamt += invdetailObj.optDouble(GSTRConstants.adjustedamount);
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                        samt += termamount;
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                        samt += termamount;
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                        csamt += termamount;
                    }
                    anum = invdetailObj.optString(GSTRConstants.invoicenumber);
                    adt = invdetailObj.optString(GSTRConstants.receiptlinkdate);
                }

                itemDetail.setRt(rate);
            }
            itemDetail.setCamt(camt);
            itemDetail.setSamt(samt);
            itemDetail.setAd_amt(advanceamt);
            itemDetail.setIamt(iamt);
            itemDetail.setCsamt(csamt);
            itemDetail.setTxval(advanceamt);
            itemDetail.setAnum(anum);
            itemDetail.setAdt(adt);
            itemDetails.add(itemDetail);
            at.setItms(itemDetails);
            JSONObject jSONObject = new JSONObject(at);
            array.put(jSONObject);
        }
        response.put("count", noofdocument);
        response.put("taxableamt", taxableamt);
//        System.out.println("merged");
//        System.out.println(array.toString());
        return response.put("atadj", array);
    }

    public JSONArray createJsonForAdvanceDataFetchedFromDB(List<Object> invoiceList, JSONObject params) throws JSONException {
        JSONArray bulkData = new JSONArray();
        for (Object object : invoiceList) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(GSTRConstants.pos, data[0].toString());
            jSONObject.put(GSTRConstants.posid, data[1].toString());
            jSONObject.put(GSTRConstants.receiptamountdue, (Double) data[2]);
            jSONObject.put(GSTRConstants.receipttaxamount, (Double) data[3]);
            jSONObject.put(GSTRConstants.taxrate, (Double) data[4]);
            jSONObject.put(GSTRConstants.termamount, (Double) data[5]);
            jSONObject.put(GSTRConstants.defaultterm, (String) data[6]);
            jSONObject.put(GSTRConstants.receiptadvanceid, (String) data[7]);
            jSONObject.put(GSTRConstants.adjustedamount, data[8] != null ? (Double) data[8] : 0);
            jSONObject.put(GSTRConstants.receiptamount, data[9] != null ? (Double) data[9] : 0);
            jSONObject.put(GSTRConstants.receiptlinkdate, data[10] != null ? (Date) data[10] : null);
            jSONObject.put(GSTRConstants.vendorname, data[11] != null && data.length>10? (String) data[11]:"");
            jSONObject.put(GSTRConstants.gstin, data[12] != null && data.length>11? (String) data[12]:"");
            jSONObject.put(GSTRConstants.invoicenumber, data[13] != null && data.length>12 ? (String) data[13]:"");
//            jSONObject.put(GSTRConstants.entrydate, data[14] != null ? (Date) data[14]:"");
            bulkData.put(jSONObject);
        }
        return bulkData;
    }
    /**
     * Function to create data of ITC JE.
     * @param reqParams
     * @param columnDataArr
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getITCJournalEntryDetails(JSONObject reqParams, JSONArray columnDataArr) throws ServiceException, JSONException {
        JSONObject dataObj = new JSONObject();
        String companyId = reqParams.optString("companyid");
        reqParams.put("gstrjetype", Constants.JETYPE_ITC);
        List<Object> jeList = gstr2Dao.getITCJournalEntryDetails(reqParams);
        JSONArray bulkData = createJsonForITCJEDataFetchedFromDB(jeList, reqParams);
        double totalTaxableAmount, totalIGST, totalCGST, totalSGST, totalCess;
        totalCGST = totalIGST = totalSGST = totalTaxableAmount = totalCess = 0;
        Map<String, JSONArray> JEMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.JEID);
        for (String jekey : JEMap.keySet()) {
            JSONObject jSONObject = new JSONObject();
            double igst = 0, cgst = 0, sgst = 0, cess = 0, totalamt = 0d;
            JSONArray jeArr = JEMap.get(jekey);
            for (int index = 0; index < jeArr.length(); index++) {
                JSONObject jeObj = jeArr.getJSONObject(index);
                String jeNo = jeObj.optString(GSTRConstants.JENUMBER);
                String defaultAccountId = jeObj.optString(GSTRConstants.JE_DEFAULT_ACC);
                if (!StringUtil.isNullOrEmpty(defaultAccountId)) {
                    if (defaultAccountId.equalsIgnoreCase(GSTRConstants.GST_ACCOUNT_Name.get("InputCGST"))) {
                        cgst += jeObj.optDouble(GSTRConstants.JEDAMOUNT);
                    } else if (defaultAccountId.equalsIgnoreCase(GSTRConstants.GST_ACCOUNT_Name.get("InputSGST"))) {
                        sgst += jeObj.optDouble(GSTRConstants.JEDAMOUNT);
                    } else if (defaultAccountId.equalsIgnoreCase(GSTRConstants.GST_ACCOUNT_Name.get("InputIGST"))) {
                        igst += jeObj.optDouble(GSTRConstants.JEDAMOUNT);
                    } else if (defaultAccountId.equalsIgnoreCase(GSTRConstants.GST_ACCOUNT_Name.get("InputUTGST"))) {
                        sgst += jeObj.optDouble(GSTRConstants.JEDAMOUNT);
                    } else if (defaultAccountId.equalsIgnoreCase(GSTRConstants.GST_ACCOUNT_Name.get("InputCESS"))) {
                        cess += jeObj.optDouble(GSTRConstants.JEDAMOUNT);
                    }
                }
                jSONObject.put(DATAINDEX_TRANSACTION_NUMBER, jeNo);
                jSONObject.put(DATAINDEX_DATE, jeObj.optString(GSTRConstants.JEDATE));
            }
            totalamt = sgst + cgst + igst + cess;
            totalCGST += cgst;
            totalSGST += sgst;
            totalIGST += igst;
            totalCess += cess;
            jSONObject.put(DATAINDEX_DOCTYPE, "Journal Entry");
            jSONObject.put(DATAINDEX_IGSTAMOUNT, authHandler.formattedAmount(igst, companyId));
            jSONObject.put(GSTR3BConstants.DATAINDEX_SGSTAMOUNT, authHandler.formattedAmount(sgst, companyId));
            jSONObject.put(GSTR3BConstants.DATAINDEX_CGSTAMOUNT, authHandler.formattedAmount(cgst, companyId));
            jSONObject.put(GSTR3BConstants.DATAINDEX_CESS, authHandler.formattedAmount(cess, companyId));
            jSONObject.put(GSTR3BConstants.DATAINDEX_TOTAL_AMOUNT, authHandler.formattedAmount(totalamt, companyId));
            jSONObject.put(GSTR3BConstants.DATAINDEX_TOTAL_TAX, authHandler.formattedAmount(totalamt, companyId));
            jSONObject.put(GSTR3BConstants.DATAINDEX_TAXABLE_AMOUNT, authHandler.formattedAmount(0, companyId));
            if (!(reqParams.has("reportid") && reqParams.optInt("reportid", 0) == Constants.GSTR3B_Summary_Report) && reqParams.optBoolean(GSTR3BConstants.DETAILED_VIEW_REPORT, false)) {
                columnDataArr.put(jSONObject);
            }
        }
        if (reqParams.has("reportid") && reqParams.optInt("reportid", 0) == Constants.GSTR3B_Summary_Report) {
            dataObj.put("taxableamt", authHandler.formattedAmount(totalTaxableAmount, companyId));
            dataObj.put("igst", authHandler.formattedAmount(totalIGST, companyId));
            dataObj.put("cgst", authHandler.formattedAmount(totalCGST, companyId));
            dataObj.put("sgst", authHandler.formattedAmount(totalSGST, companyId));
            dataObj.put("csgst", authHandler.formattedAmount(totalCess, companyId));
            dataObj.put("totaltax", authHandler.formattedAmount((totalIGST + totalCGST + totalSGST + totalCess), companyId));
            dataObj.put("totalamount", authHandler.formattedAmount((totalTaxableAmount + totalIGST + totalCGST + totalSGST + totalCess), companyId));
//            columnDataArr.put(dataObj);
        } else {
            dataObj.put("data", columnDataArr);
        }

        return dataObj;
    }
    /**
     * Function to create JSON for ITC type of documents.
     *
     * @param invoiceList
     * @param params
     * @return
     * @throws JSONException
     */
    public JSONArray createJsonForITCJEDataFetchedFromDB(List<Object> invoiceList, JSONObject params) throws JSONException {
        JSONArray bulkData = new JSONArray();
        for (Object object : invoiceList) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(GSTRConstants.JEID, data[0].toString());
            jSONObject.put(GSTRConstants.JENUMBER, data[1].toString());
            jSONObject.put(GSTRConstants.JEDATE, data[2] != null ? data[2].toString() : null);
            jSONObject.put(GSTRConstants.JE_DEFAULT_ACC, (String) data[3]);
            jSONObject.put(GSTRConstants.JEDAMOUNT, (Double) data[4]);
            bulkData.put(jSONObject);
        }
        return bulkData;
    }
}
