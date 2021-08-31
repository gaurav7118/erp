/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.gst.services;

import com.krawler.common.admin.FieldComboData;
import static com.krawler.common.admin.FieldComboData.ValueTypeMap;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.InvoiceDetailTermsMap;
import com.krawler.hql.accounting.LineLevelTerms;
import com.krawler.spring.accounting.entitygst.AccEntityGstDao;
import com.krawler.spring.accounting.entitygst.GSTR3BConstants;
import com.krawler.spring.accounting.gst.dto.AT;
import com.krawler.spring.accounting.gst.dto.B2B;
import com.krawler.spring.accounting.gst.dto.B2CL;
import com.krawler.spring.accounting.gst.dto.B2CS;
import com.krawler.spring.accounting.gst.dto.CDNR;
import com.krawler.spring.accounting.gst.dto.CDNUR;
import com.krawler.spring.accounting.gst.dto.EXP;
import com.krawler.spring.accounting.gst.dto.HSN;
import com.krawler.spring.accounting.gst.dto.InvoiceDto;
import com.krawler.spring.accounting.gst.dto.ItemDetail;
import com.krawler.spring.accounting.gst.dto.ItemDto;
import com.krawler.spring.accounting.gst.services.gstr2.GSTR2Dao;
import com.krawler.spring.accounting.gst.services.gstr2.GSTR2DeskeraServiceImpl;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.common.KwlReturnObject;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GSTR1DeskeraServicesImpl extends BaseDAO implements GSTR1ServiceDao {

    private AccEntityGstDao accEntityGstDao;
    private GSTR2Dao gstr2Dao;

    public void setAccEntityGstDao(AccEntityGstDao accEntityGstDao) {
        this.accEntityGstDao = accEntityGstDao;
    }

    public void setgstr2Dao(GSTR2Dao gstr2Dao) {
        this.gstr2Dao = gstr2Dao;
    }

    /**
     * @Desc : Function which convert invoice data to Json Array for processing
     * @param invoiceList
     * @return
     * @throws JSONException
     */
    private JSONArray createJsonForDataFetchedFromDB(List<Object> invoiceList, JSONObject params) throws JSONException {
        JSONArray bulkData = new JSONArray();
        for (Object object : invoiceList) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(GSTRConstants.customerid, data[0].toString()!=null?data[0].toString():"");
            jSONObject.put(GSTRConstants.gstin, data[1]!=null?data[1].toString():"");
            jSONObject.put(GSTRConstants.invoiceid, data[2] != null ? data[2].toString() : "");
            jSONObject.put(GSTRConstants.invoicenumber, data[3] != null ? data[3].toString() : "");
            jSONObject.put(GSTRConstants.jeid, data[4] != null ? data[4].toString() : "");
            jSONObject.put(GSTRConstants.entrydate, data[5] != null ? data[5].toString() : "");
            jSONObject.put(GSTRConstants.invoicedetailid, data[6] != null ? data[6].toString() : "");
            jSONObject.put(GSTRConstants.rate, (Double) data[7]!=0.0?(Double) data[7]:0.0);
            if (data[8] instanceof BigDecimal) {
                jSONObject.put(GSTRConstants.quantity, ((BigDecimal) data[8]).doubleValue());
            } else {
                jSONObject.put(GSTRConstants.quantity, (Double) data[8]!=0.0?(Double) data[8]:0.0);
            }
            jSONObject.put(GSTRConstants.productid, data[9].toString()!=null?data[9].toString():"");
            jSONObject.put(GSTRConstants.term, data[10]!=null?data[10].toString():"");
            jSONObject.put(GSTRConstants.termamount, data[11]!=null?(Double) data[11]:0);
            jSONObject.put(GSTRConstants.invamountinbase, (Double) data[12]!=0.0?(Double) data[12]:0.0);
            jSONObject.put(GSTRConstants.hsncode, data[13]!=null?data[13].toString():"");
            jSONObject.put(GSTRConstants.pos, data[14]!=null?data[14].toString():"");
            jSONObject.put(GSTRConstants.productdesc, (String)data[15]!=null?(String)data[15]:"");
            jSONObject.put(GSTRConstants.posid, data[16]!=null?data[16].toString():"");
            if (data[17] instanceof BigDecimal) {
                jSONObject.put(GSTRConstants.taxrate, data[17] != null ? ((BigDecimal) data[17]).doubleValue() : 0);
            } else {
                jSONObject.put(GSTRConstants.taxrate,  data[17]!=null?(Double) data[17]:0);
            }
            jSONObject.put(GSTRConstants.defaultterm, data[18]!=null?(String) data[18]:"");
            /**
             * Additional data come only in case of Return
             */
            if (params.optBoolean("cdnr") || params.optBoolean("cdnur") || params.optBoolean("isb2cs")) {
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
                if (params.optBoolean("cdnur") || params.optBoolean("isb2cs")) {
                    jSONObject.put(GSTRConstants.cnid, data[23].toString());
                    jSONObject.put(GSTRConstants.cnamountinbase, (Double) data[24]);
                    jSONObject.put(GSTRConstants.statecode, data[25]!=null? Integer.parseInt(data[25].toString()):0); // No need to stcode in unregistered
                    if(data.length > 26 && data[26]!=null){
                        jSONObject.put(GSTRConstants.customername, (String) data[26] != null ? (String) data[26] : "");
                        
                    }
                    if(data.length>27 && data[27] != null){
                        jSONObject.put(GSTRConstants.TYPE, (String) data[27] != null ? (String) data[27] : "");
                    }
                    if (data.length > 28 && (data[28] != null && data[28].equals("D"))) {
                        jSONObject.put(GSTRConstants.NT_TYPE,"D");
                    }
                    if (data.length > 29 && data[29] != null) {
                        jSONObject.put(GSTRConstants.CNDNREASON, (String) data[29]);
                    }
                }else{
                    jSONObject.put(GSTRConstants.statecode, (data[23]!=null && !((String)data[23]).isEmpty()) ? Integer.parseInt((String)data[23]):0);
                    if (data.length > 24 && data[24] != null) {
                    jSONObject.put(GSTRConstants.customername, data[24]!= null ? data[24].toString():"");
                 }
                    if (data.length > 25 && data[25] != null) {
                        jSONObject.put(GSTRConstants.NT_TYPE, data[25] != null && !StringUtil.isNullOrEmpty(data[25].toString())? data[25].toString() : "C");
                    }
                    if (data.length > 26 && data[26] != null) {
                        jSONObject.put(GSTRConstants.CNDNREASON, (String) data[26]);
                    }
               }
            } else {
                jSONObject.put(GSTRConstants.discpercentage, data[19] != null ? (Character) data[19] : "");
                jSONObject.put(GSTRConstants.discountvalue, data[20] != null ? (Double) data[20] : 0);
                jSONObject.put(GSTRConstants.statecode, (data[21]!=null && !((String)data[21]).isEmpty()) ? Integer.parseInt((String)data[21]):0);
                jSONObject.put(GSTRConstants.customername, data.length>22 && data[22]!= null ? data[22].toString():"");
                if (params.has("isb2cs") && params.optBoolean("isb2cs")) {
                    jSONObject.put(GSTRConstants.cnrate, data[23] != null ? Double.parseDouble(data[23].toString()) : 0);
                    jSONObject.put(GSTRConstants.cnquantity, data[24] != null && !data[24].toString().equalsIgnoreCase("") ? (Double) data[24] : 1);
                    jSONObject.put(GSTRConstants.cnterm, data[25] != null ? data[25].toString() : "");
                    jSONObject.put(GSTRConstants.cntermamount, data[26] != null ? (Double) data[26] : 0);
                    jSONObject.put(GSTRConstants.cntaxrate, data[27] != null ? (Double) data[27] : 0);
                    jSONObject.put(GSTRConstants.cndefaultterm, data[28] != null ? (String) data[28] : "");
                    jSONObject.put(GSTRConstants.cndiscpercentage, data[29] != null && !data[29].toString().equalsIgnoreCase("")? (Integer) data[29] : "");
                    jSONObject.put(GSTRConstants.cndiscountvalue, data[30] != null && !data[30].toString().equalsIgnoreCase("") ? (Double) data[30] : 0);
                    jSONObject.put(GSTRConstants.cnid, data[31] != null ? data[31].toString() : "");
                    jSONObject.put(GSTRConstants.cndid, data[32] != null ? data[32].toString() : "");
                    jSONObject.put(GSTRConstants.cndnnumber, data[33] != null ? data[33].toString() : "");
                    jSONObject.put(GSTRConstants.cndndate, data[34] != null ? data[34].toString() : "");
                    jSONObject.put(GSTRConstants.ECommerceGstin, (data[35] != null && !((String) data[35]).isEmpty()) ? (String) data[35] : "");

                }
                if (params.has("b2bCustVenType")) { // if invoice has gst registration type
                    jSONObject.put(GSTRConstants.gstregtype, (data[23]!=null && !((String)data[23]).isEmpty()) ? (String)data[23]:"");
                    jSONObject.put(GSTRConstants.gstcusttype, (data[24]!=null && !((String)data[24]).isEmpty()) ? (String)data[24]:"");
                    jSONObject.put(GSTRConstants.ECommerceGstin, (data[25]!=null && !((String)data[25]).isEmpty()) ? (String)data[25]:"");
                }
                 if(params.has("export")){ // if invoice has gst customer type
                    jSONObject.put(GSTRConstants.exportType, (data.length>23 && data[23]!=null && !((String)data[23]).isEmpty()) ? (String)data[23]:"");
                    jSONObject.put(GSTRConstants.GSTR1_SHIPPING_PORT, data.length>24?(data[24]!=null && !((String)data[24]).isEmpty()) ? (String)data[24]:"":"");
                     jSONObject.put(GSTRConstants.GSTR1_SHIPPING_DATE, data.length > 25 ? (data[25] != null && !(data[25].toString()).isEmpty()) ? data[25] instanceof Date ? new SimpleDateFormat(GSTRConstants.GSTR1_DATEFORMAT).format(data[25]) : "" : "" : "");
                    jSONObject.put(GSTRConstants.GSTR1_SHIPPING_BILL_NO, data.length>26?(data[26]!=null && !((String)data[26]).isEmpty()) ? (String)data[26]:"":"");
                }
                 if(params.has("ecom")&& !params.has("isb2cs")&& !params.has("b2bCustVenType") && !params.has("export")){ // if invoice has E-Commerce Operator
                    jSONObject.put(GSTRConstants.ECommerceGstin, (data.length>23 && data[23]!=null && !((String)data[23]).isEmpty()) ? (String)data[23]:"");
                }
            }
            bulkData.put(jSONObject);
        }
        return bulkData;
    }

    /**
     * @Desc : Function which convert invoice data to Json Array for processing
     * @param invoiceList
     * @return
     * @throws JSONException
     */
    private JSONArray createJsonForMismatchReportDataFetchedFromDB(List<Object> invoiceList, JSONObject params) throws JSONException {
        JSONArray bulkData = new JSONArray();
        for (Object object : invoiceList) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(GSTRConstants.customerid, data[0] != null && data[0].toString()!=null? data[0].toString():"");
            jSONObject.put(GSTRConstants.gstin, data[1]!=null?data[1].toString():"");
            jSONObject.put(GSTRConstants.invoiceid, data[2] != null ? data[2].toString() : "");
            jSONObject.put(GSTRConstants.invoicenumber, data[3] != null ? data[3].toString() : "");
            jSONObject.put(GSTRConstants.jeid, data[4] != null ? data[4].toString() : "");
            jSONObject.put(GSTRConstants.entrydate, data[5] != null ? data[5].toString() : "");
            jSONObject.put(GSTRConstants.invoicedetailid, data[6] != null ? data[6].toString() : "");
            jSONObject.put(GSTRConstants.rate, data[7] !=null && (Double) data[7]!=0.0 ? (Double) data[7]:0.0);
            if (data[8] instanceof BigDecimal) {
                jSONObject.put(GSTRConstants.quantity, data[8] !=null  ? ((BigDecimal) data[8]).doubleValue() : 0.0);
            } else {
                jSONObject.put(GSTRConstants.quantity, data[8] !=null && (Double) data[8]!=0.0 ? (Double) data[8]:0.0);
            }
            jSONObject.put(GSTRConstants.productid, data[9]!=null?data[9].toString():"");
            jSONObject.put(GSTRConstants.term, data[10]!=null?data[10].toString():"");
            jSONObject.put(GSTRConstants.termamount, data[11]!=null?(Double) data[11]:0);
            jSONObject.put(GSTRConstants.invamountinbase, data[12] !=null && (Double) data[12]!=0.0?(Double) data[12]:0.0);
            jSONObject.put(GSTRConstants.hsncode, data[13]!=null?data[13].toString():"");
            jSONObject.put(GSTRConstants.pos, data[14]!=null?data[14].toString():"");
            jSONObject.put(GSTRConstants.productdesc, data[15]!=null ? (String)data[15]:"");
            jSONObject.put(GSTRConstants.posid, data[16]!=null?data[16].toString():"");
            if (data[17] instanceof BigDecimal) {
                jSONObject.put(GSTRConstants.taxrate, data[17] != null ? ((BigDecimal) data[17]).doubleValue() : 0);
            } else {
                jSONObject.put(GSTRConstants.taxrate,  data[17]!=null?(Double) data[17]:0);
            }
            jSONObject.put(GSTRConstants.defaultterm, data[18]!=null?(String) data[18]:"");       
            jSONObject.put(GSTRConstants.discpercentage, data[19] != null && !StringUtil.isNullOrEmpty(data[19].toString())? (Character) data[19] : "");
                jSONObject.put(GSTRConstants.discountvalue, data[20] != null && !StringUtil.isNullOrEmpty(data[20].toString())? (Double) data[20] : 0);
            jSONObject.put(GSTRConstants.statecode, data[21] != null ? Double.parseDouble(data[21].toString()) : 0);
            jSONObject.put(GSTRConstants.eltrTaxRate, data[22] != null ? (Double) data[22] : 0);
            jSONObject.put(GSTRConstants.invTermRate, data[23] != null ? (Double) data[23] : 0);
            jSONObject.put(GSTRConstants.INV_TERM_TYPE, data.length>24 ? data[24] != null ? (int) data[24] : 0:0);
            /**
             * two column added GST Registration type and Customer/ Vendor Type and its name
             * ERP-39237
             */
            jSONObject.put(GSTRConstants.GSTREG_TYPEID, data.length>25 && data[25] != null ? (String) data[25] : "");
            jSONObject.put(GSTRConstants.GSTCustomer_Vendor_TYPEID, data.length>26 && data[26] != null ? (String) data[26] : "");
            jSONObject.put(GSTRConstants.GSTDETAILS_TYPEVALUE, data.length>27 && data[27] != null ? (String) data[27] : "");
            bulkData.put(jSONObject);    
            }   
        return bulkData;
    }
    /**
     * @Desc : Details of invoices of Taxable supplies made to other registered
     * taxpayers
     * @param reqParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getB2BInvoices(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        /**
         * Add Additional parameter to reqParams 
         */
        JSONObject params = new JSONObject(reqParams, JSONObject.getNames(reqParams));
        params.put("isGSTINnull", false);
        params.put("registrationType", Constants.GSTRegType_Regular+","+Constants.GSTRegType_Regular_ECommerce+","+Constants.GSTRegType_Composition+","+Constants.GSTRegType_Composition_ECommerce);
        params.put("CustomerType", Constants.CUSTVENTYPE_NA  + "," + Constants.CUSTVENTYPE_SEZ + "," + Constants.CUSTVENTYPE_SEZWOPAY);
        params.put("taxClassType", FieldComboData.TaxClass_Percenatge);
    //    params.put("zerorated", false);
        params.put("b2bCustVenType", true);
        params.put("typeofjoinisleft", true);
        
        List<Object> invoiceList = accEntityGstDao.getInvoiceDataWithDetailsInSql(params);
        setB2BInvoiceList(array, invoiceList, params);
//        System.out.println("B2B");
//        System.out.println(array.toString());
        return response.put("b2b", array);
    }

    public void setB2BInvoiceList(JSONArray array,List<Object> invoiceList,JSONObject params) throws JSONException {
        JSONArray bulkData = createJsonForDataFetchedFromDB(invoiceList, params);

        /**
         * create customer wise map i.e Return Map which contains customer id as
         * key and its related data array
         */
        Map<String, JSONArray> customerMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.customerid);
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
                invoiceDto.setInum(invObj.optString(GSTRConstants.invoicenumber));
                invoiceDto.setIdt(invObj.optString(GSTRConstants.entrydate));

                invoiceDto.setVal(invObj.optDouble(GSTRConstants.invamountinbase));
                invoiceDto.setPos(invObj.optString(GSTRConstants.pos));
                invoiceDto.setStcode(invObj.optInt(GSTRConstants.statecode));
                invoiceDto.setCustomerName(invObj.optString(GSTRConstants.customername));
                String gstcustType = invObj.optString(GSTRConstants.gstcusttype);
                String gstregType = invObj.optString(GSTRConstants.gstregtype);
                if (!StringUtil.isNullOrEmpty(gstregType) && !StringUtil.isNullOrEmpty(gstcustType)) {
                    if (gstcustType.equals(Constants.CUSTVENTYPE_SEZ)) {
                        invoiceDto.setInv_typ(GSTRConstants.SEZWPAYB2B);
                    } else if (gstcustType.equals(Constants.CUSTVENTYPE_SEZWOPAY)) {
                        invoiceDto.setInv_typ(GSTRConstants.SEZWOPAYB2B);
                    } else if (gstcustType.equals(Constants.CUSTVENTYPE_DEEMED_EXPORT)) {
                        invoiceDto.setInv_typ(GSTRConstants.DeemedExportB2B);
                    } else if (gstregType.equals(Constants.GSTRegType_Regular) || gstregType.equals(Constants.GSTRegType_Regular_ECommerce) || gstregType.equals(Constants.GSTRegType_Composition) || gstregType.equals(Constants.GSTRegType_Composition_ECommerce)) {
                        invoiceDto.setInv_typ(GSTRConstants.RegularB2B);
                    }
                }
                invoiceDto.setEtin(invObj.optString(GSTRConstants.ECommerceGstin));
                if(invObj.optString(GSTRConstants.gstregtype).equals(Constants.GSTRegType_Regular_ECommerce)||invObj.optString(GSTRConstants.gstregtype).equals(Constants.GSTRegType_Composition_ECommerce)){
                    invoiceDto.setEtin(invObj.optString(GSTRConstants.gstin));
                }
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
                        if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                            itemDetail.setIamt(termamount);
                            inv_iamt = inv_iamt + termamount;
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            itemDetail.setCamt(termamount);
                            inv_camt = inv_camt + termamount;
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                            itemDetail.setSamt(termamount);
                            inv_samt = inv_samt + termamount;
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                            itemDetail.setSamt(termamount);
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                            itemDetail.setCsamt(termamount);
                            inv_csamt = inv_csamt + termamount;
                        }

                    }
                    itemDto.setItm_det(itemDetail);
                    itemDtos.add(itemDto);
                }
                invoiceDto.setItms(itemDtos);
                invoiceDto.setInv_iamt(inv_iamt);
                invoiceDto.setInv_camt(inv_camt);
                invoiceDto.setInv_samt(inv_samt);
                invoiceDto.setInv_csamt(inv_csamt);
                invoiceDtos.add(invoiceDto);
            }
            b2bObj.setCtin(custObj.optString(GSTRConstants.gstin));
            b2bObj.setInv(invoiceDtos);
            b2bList.add(b2bObj);
            JSONObject jSONObject = new JSONObject(b2bObj);
            array.put(jSONObject);
        }
    }
    /**
     * @Desc : Invoices for Taxable outward supplies to consumers where a)The
     * place of supply is outside the state where the supplier is registered and
     * b)The total invoice value is more that Rs 2,50,000
     * @param reqParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getB2CLInvoices(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        JSONObject params = new JSONObject(reqParams, JSONObject.getNames(reqParams));
        params.put("isGSTINnull", true);
        params.put("registrationType", Constants.GSTRegType_Unregistered);
        params.put("CustomerType", Constants.CUSTVENTYPE_NA);
        params.put("taxClassType", FieldComboData.TaxClass_Percenatge);
        params.put("greaterlimit", true);
        params.put("limitamount", Constants.GST_LIMIT_AMT);
        params.put("zerorated", false);
        params.put("interstate", true);
//        params.put("isRCMApplicable", false);
        List<Object> invoiceList = accEntityGstDao.getInvoiceDataWithDetailsInSql(params);
        JSONArray bulkData = createJsonForDataFetchedFromDB(invoiceList, reqParams);

        /**
         * create location i.e. POS wise map i.e Return Map which contains
         * customer id as key and its related data array
         */
        Map<String, JSONArray> posMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.posid);
        List<B2CL> b2clList = new ArrayList<B2CL>();
        B2CL b2clObj = null;
        for (String poskey : posMap.keySet()) {
            b2clObj = new B2CL();
            JSONArray posArr = posMap.get(poskey);
            JSONObject posObj = posArr.getJSONObject(0);
            /**
             * Create invoice map * i.e Return Map which contains Invoice id as
             * key and its related data array
             */
            List<InvoiceDto> invoiceDtos = new ArrayList<InvoiceDto>();
            Map<String, JSONArray> invoiceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(posArr, GSTRConstants.invoiceid);
            for (String invkey : invoiceMap.keySet()) {

                JSONArray invArr = invoiceMap.get(invkey);
                JSONObject invObj = invArr.getJSONObject(0);
                InvoiceDto invoiceDto = new InvoiceDto();
                invoiceDto.setInum(invObj.optString(GSTRConstants.invoicenumber));
                invoiceDto.setIdt(invObj.optString(GSTRConstants.entrydate));
                invoiceDto.setVal(invObj.optDouble(GSTRConstants.invamountinbase));
                invoiceDto.setPos(invObj.optString(GSTRConstants.pos));
                invoiceDto.setStcode(invObj.optInt(GSTRConstants.statecode));
                invoiceDto.setEtin(invObj.optString(GSTRConstants.ECommerceGstin));
                invoiceDto.setCustomerName(invObj.optString(GSTRConstants.customername));
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
//                    double discount = invoiceDetail.getDiscount() != null ? invoiceDetail.getDiscount().getDiscountValue() : 0d;
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
                        if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                            itemDetail.setIamt(termamount);
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            itemDetail.setCamt(termamount);
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                            itemDetail.setSamt(termamount);
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                            itemDetail.setSamt(termamount);
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                            itemDetail.setCsamt(termamount);
                        }

                    }
                    itemDto.setItm_det(itemDetail);
                    itemDtos.add(itemDto);
                }
                invoiceDto.setItms(itemDtos);
                invoiceDtos.add(invoiceDto);
            }
            b2clObj.setPos(posObj.optString(GSTRConstants.pos));
            b2clObj.setInv(invoiceDtos);
            b2clList.add(b2clObj);
            JSONObject jSONObject = new JSONObject(b2clObj);
            array.put(jSONObject);
        }
//        System.out.println("merged");
//        System.out.println(array.toString());
        return response.put("b2cl", array);
    }

    /**
     * @Desc : Supplies made to consumers and unregistered persons of the
     * following nature a) Intra-State: any value b) Inter-State: Invoice value
     * Rs 2.5 lakh or less
     * @param reqParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getB2CSInvoices(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        int noofdocument = 0;
        JSONObject params = new JSONObject(reqParams, JSONObject.getNames(reqParams));
        params.put("isGSTINnull", true);
        params.put("registrationType", Constants.GSTRegType_Unregistered);
        params.put("CustomerType", Constants.CUSTVENTYPE_NA);
        params.put("taxClassType", FieldComboData.TaxClass_Percenatge);
        params.put("isb2cs", true);
//        params.put("greaterlimit", false);
        params.put("limitamount", Constants.GST_LIMIT_AMT);
//        params.put("zerorated", false);
//        params.put("isRCMApplicable", false);
        List<Object> invoiceList = accEntityGstDao.getInvoiceDataWithDetailsInSql(params);
        params.remove("isb2cs");
        JSONArray bulkData = createJsonForDataFetchedFromDB(invoiceList, params);

        /**
         * create location i.e. POS wise map i.e Return Map which contains
         * customer id as key and its related data array
         */
        Map<String, JSONArray> invoiceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.invoiceid);
        B2CS b2csObj = null;
        for (String invkey : invoiceMap.keySet()) {
            noofdocument++;
            JSONArray invArr = invoiceMap.get(invkey);
            double finalTaxamount = 0;
//            for (int index = 0; index < invArr.length(); index++) {
            Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(invArr, GSTRConstants.invoicedetailid);
            for (String invdetailkey : invoiceDetailMap.keySet()) {
                JSONArray invDetailArr = invoiceDetailMap.get(invdetailkey);
                b2csObj = new B2CS();
                JSONObject invObj = invDetailArr.getJSONObject(0);
                double rate = invObj.optDouble(GSTRConstants.rate);
                double qty = invObj.optDouble(GSTRConstants.quantity);
                double discount = 0d;
                String discpercentage = invObj.optString(GSTRConstants.discpercentage);
                double discountvalue = invObj.optDouble(GSTRConstants.discountvalue);
                if (!StringUtil.isNullOrEmpty(discpercentage)) {
                    if (discpercentage.equalsIgnoreCase("T")) {
                        discount = discountvalue * (rate * qty) / 100;
                    } else {
                        discount = discountvalue;
                    }
                }
                double taxableamt = (qty * rate) - discount;
                String type = invObj.optString(GSTRConstants.ECommerceGstin) != "" ? "E" : "OE";
                b2csObj.setTyp(type);
                b2csObj.setTxval(taxableamt);
                b2csObj.setPos(invObj.optString(GSTRConstants.pos));
                b2csObj.setStcode(invObj.optInt(GSTRConstants.statecode));
                b2csObj.setEtin(invObj.optString(GSTRConstants.ECommerceGstin));
                b2csObj.setInvid(invObj.optString(GSTRConstants.invoiceid));
                b2csObj.setInum(invObj.optString(GSTRConstants.invoicenumber));
                b2csObj.setIdt(invObj.optString(GSTRConstants.entrydate));
                b2csObj.setDoctype(B2CS.DOCTYPE_SALES);;
                b2csObj.setGstin(invObj.optString(GSTRConstants.gstin));
                b2csObj.setCustomerName(invObj.optString(GSTRConstants.customername));
                double iamt = 0d;
                double camt = 0d;
                double samt = 0d;
                double csamt = 0d;
                float taxrate = 0;
                Set invtermset = new HashSet();
                Set cntermset = new HashSet();
                for (int dindex = 0; dindex < invDetailArr.length(); dindex++) {
                    JSONObject invdetailObj = invDetailArr.getJSONObject(dindex);
                    String term = invdetailObj.optString(GSTRConstants.defaultterm);
                    if (invtermset.add(term)) {
                        /**
                         * Iterate applied GST and put its Percentage and Amount
                         * accordingly
                         */
                        double termamount = invdetailObj.optDouble(GSTRConstants.termamount);
                        taxrate += invdetailObj.optDouble(GSTRConstants.taxrate);
                        if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                            iamt += termamount;
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            camt += termamount;
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                            samt += termamount;
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                            samt += termamount;
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                            csamt += termamount;
                        }
                    }
                }
                b2csObj.setCsamt(csamt);
                b2csObj.setSamt(samt);
                b2csObj.setCamt(camt);
                b2csObj.setIamt(iamt);
                b2csObj.setRt(taxrate);
                JSONObject jSONObject = new JSONObject(b2csObj);
                array.put(jSONObject);
            }
        }
        
        /**
         * Code to get CN/DN/Refund
         */
        params.put("isb2cs", true);
        params.put("entitycolnum", reqParams.optString("cnentitycolnum"));
        params.put("entityValue", reqParams.optString("cnentityValue"));
        invoiceList = accEntityGstDao.getCNDNWithInvoiceDetailsInSql(params);
        bulkData = createJsonForDataFetchedFromDB(invoiceList, params);
        setDataToB2CSPOJO(bulkData, array, noofdocument, B2CS.DOCTYPE_CN);

        invoiceList = accEntityGstDao.getCNAgainstCustomer(params);
        bulkData = createJsonForDataFetchedFromDB(invoiceList, params);
        setDataToB2CSPOJO(bulkData, array, noofdocument, B2CS.DOCTYPE_CN);

        params.put("entitycolnum", reqParams.optString("paymententitycolnum"));
        params.put("entityValue", reqParams.optString("paymententityValue"));
        invoiceList = accEntityGstDao.getCashRefundWithInvoiceDetailsInSql(params);
        bulkData = createJsonForDataFetchedFromDB(invoiceList, params);
        setDataToB2CSPOJO(bulkData, array, noofdocument, B2CS.DOCTYPE_CSR);
        if (!StringUtil.isNullOrEmpty(reqParams.optString("dnentitycolnum", null)) && !StringUtil.isNullOrEmpty(reqParams.optString("dnentityValue", null))) {
            params.put("entitycolnum", reqParams.optString("dnentitycolnum"));
            params.put("entityValue", reqParams.optString("dnentityValue"));
            invoiceList = accEntityGstDao.getDNAgainstCustomer(params);
            bulkData = createJsonForDataFetchedFromDB(invoiceList, params);
            setDataToB2CSPOJO(bulkData, array, noofdocument, B2CS.DOCTYPE_DN);
        }
        response.put("count", noofdocument);
        return response.put("b2cs", array);
    }
    public void setDataToB2CSPOJO(JSONArray bulkData,JSONArray array,int noofdocument,String doctype) throws JSONException{
        B2CS b2csObj = null;     
        Map<String, JSONArray> cnMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.invoiceid);
        for (String invkey : cnMap.keySet()) {
            if(StringUtil.isNullOrEmpty(invkey)){
                continue;
            }
            noofdocument++;
            JSONArray invArr = cnMap.get(invkey);
            double finalTaxamount = 0;
            Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(invArr, GSTRConstants.invoicedetailid);
            for (String invdetailkey : invoiceDetailMap.keySet()) {
                JSONArray invDetailArr = invoiceDetailMap.get(invdetailkey);
                b2csObj = new B2CS();
                JSONObject invObj = invDetailArr.getJSONObject(0);
                double cnrate = invObj.optDouble(GSTRConstants.rate);
                double cnqty = invObj.optDouble(GSTRConstants.quantity);
                double cndiscount = 0d;
                int cndiscpercentage = invObj.optInt(GSTRConstants.discpercentage);
                double cndiscountvalue = invObj.optDouble(GSTRConstants.discountvalue);
                if (cndiscpercentage == 1) {
                    cndiscount = cndiscountvalue * (cnrate * cnqty) / 100;
                } else {
                    cndiscount = cndiscountvalue;
                }
                double cntaxableamt = (cnqty * cnrate) - cndiscount;
                String type = invObj.optString(GSTRConstants.ECommerceGstin) != "" ? "E" : "OE";
                b2csObj.setTyp(type);
                if (doctype.equalsIgnoreCase(B2CS.DOCTYPE_DN)) {
                    /**
                     * Show positive amount for DN
                     */
                    b2csObj.setTxval(cntaxableamt);
                } else {
                    b2csObj.setTxval(-cntaxableamt);
                }
                b2csObj.setPos(invObj.optString(GSTRConstants.pos));
                b2csObj.setStcode(invObj.optInt(GSTRConstants.statecode));
                b2csObj.setEtin(invObj.optString(GSTRConstants.ECommerceGstin));
                b2csObj.setInvid(invObj.optString(GSTRConstants.invoiceid));
                b2csObj.setInum(invObj.optString(GSTRConstants.cndnnumber));
                b2csObj.setIdt(invObj.optString(GSTRConstants.cndndate));
                b2csObj.setDoctype(doctype);;
                b2csObj.setGstin(invObj.optString(GSTRConstants.gstin));
                b2csObj.setCustomerName(invObj.optString(GSTRConstants.customername));
                double iamt = 0d;
                double camt = 0d;
                double samt = 0d;
                double csamt = 0d;
                float taxrate = 0;
                Set invtermset = new HashSet();
                Set cntermset = new HashSet();
                for (int dindex = 0; dindex < invDetailArr.length(); dindex++) {
                    JSONObject invdetailObj = invDetailArr.getJSONObject(dindex);

                    /**
                     * For CN term
                     */
                    String cnterm = invdetailObj.optString(GSTRConstants.defaultterm);
                    if (cntermset.add(cnterm)) {
                        /**
                         * Iterate applied GST and put its Percentage and Amount
                         * accordingly
                         */
                        double cntermamount = invdetailObj.optDouble(GSTRConstants.termamount);
                        if (cnterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                            iamt += cntermamount;
                        } else if (cnterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            camt += cntermamount;
                        } else if (cnterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                            samt += cntermamount;
                        } else if (cnterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                            samt += cntermamount;
                        } else if (cnterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                            csamt += cntermamount;
                        }
                    }
                }
                /**
                 * Minus the amount for CN
                 */
                if (doctype.equalsIgnoreCase(B2CS.DOCTYPE_DN)) {
                    /**
                     * Show positive amount for DN
                     */
                    b2csObj.setCsamt(csamt);
                    b2csObj.setSamt(samt);
                    b2csObj.setCamt(camt);
                    b2csObj.setIamt(iamt);
                } else {
                    b2csObj.setCsamt(-csamt);
                    b2csObj.setSamt(-samt);
                    b2csObj.setCamt(-camt);
                    b2csObj.setIamt(-iamt);
                }
                b2csObj.setRt(taxrate);
                JSONObject jSONObject = new JSONObject(b2csObj);
                array.put(jSONObject);
            }
//            }

        }  
    }

    /**
     * @Desc : Function to get CNDN invoices Credit/ Debit Notes/Refund vouchers
     * issued to the registered taxpayers during the tax period. Debit or credit
     * note issued against invoice will be reported here against original
     * invoice, hence fill the details of original invoice also which was
     * furnished in B2B,B2CL section of earlier/current period tax period.
     * @param reqParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getCDNRInvoices(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        reqParams.put("cdnr", true);
        JSONObject params = new JSONObject(reqParams, JSONObject.getNames(reqParams));
        params.put("isGSTINnull", false);
        params.put("typeofjoinisleft", true);
        params.put("registrationType", Constants.GSTRegType_Regular+","+Constants.GSTRegType_Regular_ECommerce+","+Constants.GSTRegType_Composition+","+Constants.GSTRegType_Composition_ECommerce);
        params.put("CustomerType", Constants.CUSTVENTYPE_NA + "," + Constants.CUSTVENTYPE_SEZ + "," + Constants.CUSTVENTYPE_SEZWOPAY);
        params.put("cdnr", true);
        params.put("taxClassType", FieldComboData.TaxClass_Percenatge);
        List<Object> invoiceList = accEntityGstDao.getCNDNWithInvoiceDetailsInSql(params);
        params.put("CustomerType", Constants.CUSTVENTYPE_NA + "," + Constants.CUSTVENTYPE_SEZ + "," + Constants.CUSTVENTYPE_SEZWOPAY);
        List<Object> cnlist = accEntityGstDao.getCNAgainstCustomer(params);
        if (cnlist != null && cnlist.size() > 0) {
            invoiceList.addAll(cnlist);
        }
        /**
         * Add data for cash sales refund
         */
        params.put("CustomerType", Constants.CUSTVENTYPE_NA + "," + Constants.CUSTVENTYPE_SEZ + "," + Constants.CUSTVENTYPE_SEZWOPAY);
        params.put("entitycolnum", reqParams.optString("paymententitycolnum"));
        params.put("entityValue", reqParams.optString("paymententityValue"));
        List<Object> refundlist = accEntityGstDao.getCashRefundWithInvoiceDetailsInSql(params);
        if (refundlist != null && refundlist.size() > 0) {
            invoiceList.addAll(refundlist);
        }        
        if (!StringUtil.isNullOrEmpty(reqParams.optString("dnentitycolnum", null)) && !StringUtil.isNullOrEmpty(reqParams.optString("dnentityValue", null))) {
            String dnentitycolnum = reqParams.optString("entitycolnum");
            String dnentityValue = reqParams.optString("entityValue");
            params.put("entitycolnum", reqParams.optString("dnentitycolnum"));
            params.put("entityValue", reqParams.optString("dnentityValue"));
            List<Object> dnAgainstCustomer = accEntityGstDao.getDNAgainstCustomer(params);
            if (dnAgainstCustomer != null && dnAgainstCustomer.size() > 0) {
                invoiceList.addAll(dnAgainstCustomer);
            }
            params.put("entitycolnum", dnentitycolnum);
            params.put("entityValue", dnentityValue);
        }
        setB2BCNDetailsList(array, invoiceList, reqParams);
        return response.put("cndr", array);
    }

    /**
     *
     * @param array
     * @param invoiceList
     * @param reqParams
     * @throws JSONException
     */
    @Override
    public void setB2BCNDetailsList(JSONArray array, List<Object> invoiceList, JSONObject reqParams) throws JSONException {
        JSONArray bulkData = createJsonForDataFetchedFromDB(invoiceList, reqParams);

        /**
         * create customer wise map i.e Return Map which contains customer id as
         * key and its related data array
         */
        Map<String, JSONArray> customerMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.customerid);
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
                invoiceDto.setNtty(invObj.optString(GSTRConstants.NT_TYPE,"C"));
                invoiceDto.setVal(invObj.optDouble(GSTRConstants.invamountinbase));
                invoiceDto.setPos(invObj.optString(GSTRConstants.pos));
                invoiceDto.setStcode(invObj.optInt(GSTRConstants.statecode));
                invoiceDto.setCustomerName(invObj.optString(GSTRConstants.customername));
                invoiceDto.setRsn(invObj.optString(GSTRConstants.CNDNREASON,""));
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
                    double iamt = 0d;
                    double camt = 0d;
                    double samt = 0d;
                    double csamt = 0d;
                    JSONArray invDetailArr = invoiceDetailMap.get(invdetailkey);
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
                        itemDetail.setTxval(invObj.optString(GSTRConstants.NT_TYPE).equalsIgnoreCase("D") ? taxableamt : -taxableamt);
                        itemDetail.setRt(itemDetail.getRt() + invdetailObj.optDouble(GSTRConstants.taxrate));
                        itemDetail.setQty(qty);
                        itemDetail.setHsn_sc(invdetailObj.optString(GSTRConstants.hsncode));
                        itemDetail.setDesc(invdetailObj.optString(GSTRConstants.productdesc));
                        String term = invdetailObj.optString(GSTRConstants.term);
                        String defaultterm = invdetailObj.optString(GSTRConstants.defaultterm);
                        if (cntermset.add(defaultterm)) {
                            /**
                             * Iterate applied GST and put its Percentage and
                             * Amount accordingly
                             */
                            double termamount = invdetailObj.optDouble(GSTRConstants.termamount);
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
                    if (invObj.optString(GSTRConstants.NT_TYPE).equalsIgnoreCase("D")) {
                        itemDetail.setIamt(iamt);
                        itemDetail.setCamt(camt);
                        itemDetail.setSamt(samt);
                        itemDetail.setCsamt(csamt);
                        itemDto.setItm_det(itemDetail);
                        itemDtos.add(itemDto);
                    } else {
                        itemDetail.setIamt(-iamt);
                        itemDetail.setCamt(-camt);
                        itemDetail.setSamt(-samt);
                        itemDetail.setCsamt(-csamt); 
                        itemDto.setItm_det(itemDetail);
                        itemDtos.add(itemDto);
                    }  
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
    /**
     * @desc Credit/ Debit Notes/Refund vouchers issued to the unregistered
     * persons against interstate invoice value is more than Rs 2.5 lakh
     * @param reqParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getCDNURInvoices(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        reqParams.put("cdnur", true);
        reqParams.put("exportwithoutlimit", true);
        List<CDNUR>cdnurs=new ArrayList<>();
        JSONObject params = new JSONObject(reqParams, JSONObject.getNames(reqParams));
        params.put("isGSTINnull", true);
        params.put("registrationType", Constants.GSTRegType_Unregistered);
        params.put("CustomerType", Constants.CUSTVENTYPE_NA + "," + Constants.CUSTVENTYPE_Export + "," + Constants.CUSTVENTYPE_ExportWOPAY);
        params.put("interstate", true);
        params.put("greaterlimit", true);
        params.put("limitamount", Constants.GST_LIMIT_AMT);
        params.put("typeofjoinisleft", true);
        params.put("taxClassType", FieldComboData.TaxClass_Percenatge);
        List<Object> invoiceList = accEntityGstDao.getCNDNWithInvoiceDetailsInSql(params);
        params.put("CustomerType", Constants.CUSTVENTYPE_NA + "," + Constants.CUSTVENTYPE_Export + "," + Constants.CUSTVENTYPE_ExportWOPAY + "," + Constants.CUSTVENTYPE_SEZ +  "," + Constants.CUSTVENTYPE_SEZWOPAY);
        List<Object> cnList = accEntityGstDao.getCNAgainstCustomer(params);
        if (cnList != null && !cnList.isEmpty()) {
            invoiceList.addAll(cnList);
        }
        /**
         * Add data for cash sales refund
         */
        params.put("CustomerType", Constants.CUSTVENTYPE_NA + "," + Constants.CUSTVENTYPE_Export + "," + Constants.CUSTVENTYPE_ExportWOPAY);
        params.put("entitycolnum", reqParams.optString("paymententitycolnum"));
        params.put("entityValue", reqParams.optString("paymententityValue"));
        List<Object> refundlist = accEntityGstDao.getCashRefundWithInvoiceDetailsInSql(params);
        if (refundlist != null && refundlist.size() > 0) {
            invoiceList.addAll(refundlist);
        }  
        if (!StringUtil.isNullOrEmpty(reqParams.optString("dnentitycolnum", null)) && !StringUtil.isNullOrEmpty(reqParams.optString("dnentityValue", null))) {
            params.put("entitycolnum", reqParams.optString("dnentitycolnum"));
            params.put("entityValue", reqParams.optString("dnentityValue"));
            List<Object> dnAgainstCustomer = accEntityGstDao.getDNAgainstCustomer(params);
            if (dnAgainstCustomer != null && dnAgainstCustomer.size() > 0) {
                invoiceList.addAll(dnAgainstCustomer);
            }
        }
        JSONArray bulkData = createJsonForDataFetchedFromDB(invoiceList, reqParams);
        CDNUR cdnur =null;
        Map<String, JSONArray> invoiceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.invoiceid);
        for (String invkey : invoiceMap.keySet()) {
            JSONArray invArr = invoiceMap.get(invkey);
            JSONObject invObj = invArr.getJSONObject(0);
            cdnur = new CDNUR();
            cdnur.setNt_num(invObj.optString(GSTRConstants.cndnnumber));
            cdnur.setNtty(invObj.optString(GSTRConstants.NT_TYPE,"C"));// If credit note then put 'C'
            cdnur.setNt_dt(invObj.optString(GSTRConstants.cndndate));
            cdnur.setInum(invObj.optString(GSTRConstants.invoicenumber));
            cdnur.setIdt(invObj.optString(GSTRConstants.entrydate));
            cdnur.setVal(invObj.optDouble(GSTRConstants.cnamountinbase));
            cdnur.setStcode(invObj.optInt(GSTRConstants.statecode));
            cdnur.setCustomerName(invObj.optString(GSTRConstants.customername));
            cdnur.setRsn(invObj.optString(GSTRConstants.CNDNREASON,""));
            /*
             If Customer Type= Export (WPAY) then set ExportWPAY , 
             If Customer Type= Export (WOPAY) then set ExportWOPAY 
             If Customer Type= NA then set B2CL 
            */
            if (invObj.optString(GSTRConstants.TYPE).equals(Constants.CUSTVENTYPE_Export)) {
                cdnur.setTyp(GSTRConstants.ExportWPAYCDNUR);
                cdnur.setPos(GSTRConstants.otherTerritory);
            } else if (invObj.optString(GSTRConstants.TYPE).equals(Constants.CUSTVENTYPE_ExportWOPAY)) {
                cdnur.setTyp(GSTRConstants.ExportWOPAYCDNUR);
                cdnur.setPos(GSTRConstants.otherTerritory);
            } else if (invObj.optString(GSTRConstants.TYPE).equals(Constants.CUSTVENTYPE_NA)) {
                cdnur.setTyp(GSTRConstants.B2cl);
                cdnur.setPos(invObj.optString(GSTRConstants.pos));
            } else {
                cdnur.setPos(invObj.optString(GSTRConstants.pos));
            }
            List<ItemDto> itemDtos = new ArrayList<ItemDto>();
            int count = 1;
            Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(invArr, GSTRConstants.invoicedetailid);
            for (String invdetailkey : invoiceDetailMap.keySet()) {
                ItemDto itemDto = new ItemDto();
                ItemDetail itemDetail = new ItemDetail();
                itemDetail.setNum(count);
                count++;
                double iamt = 0d;
                double camt = 0d;
                double samt = 0d;
                double csamt = 0d;
                Set cntermset = new HashSet();
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
                    itemDetail.setTxval(invObj.optString(GSTRConstants.NT_TYPE).equalsIgnoreCase("D")?taxableamt:-taxableamt);
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
                    if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                        iamt+=termamount;
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                        camt+=termamount;
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                        samt+=termamount;
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                        samt+=termamount;
                    } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                        csamt+=termamount;
                    }
                    }
                }                   
                 if (invObj.optString(GSTRConstants.NT_TYPE).equalsIgnoreCase("D")) {
                        itemDetail.setIamt(iamt);
                        itemDetail.setCamt(camt);
                        itemDetail.setSamt(samt);
                        itemDetail.setCsamt(csamt);
                    } else {
                        itemDetail.setIamt(-iamt);
                        itemDetail.setCamt(-camt);
                        itemDetail.setSamt(-samt);
                        itemDetail.setCsamt(-csamt);   
                    } 
                  itemDto.setItm_det(itemDetail);
                  itemDtos.add(itemDto);
            }
               cdnur.setItms(itemDtos);
               cdnurs.add(cdnur);
            JSONObject cdnurObject = new JSONObject(cdnur);
            array.put(cdnurObject);
        }
//        System.out.println("merged");
//        System.out.println(array.toString());
//        System.out.println(cdnurs.toString());
        return response.put("cdnur", array);
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
    public JSONObject getAT(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        reqParams.put("atadj", false);
        reqParams.put("at", true);
        int noofdocument = 0;
        double taxableamt = 0d;
        double taxableamt3b = 0d;
        double igst3b = 0d;
        double cgst3b = 0d;
        double sgst3b = 0d;
        double csgst3b = 0d;
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
        List<Object> receiptList = accEntityGstDao.getAdvanceDetailsInSql(reqParams);
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
            at.setStcode(posObj.optInt(GSTRConstants.statecode));//set stateCode i.e. 27 For Maharashtra
            at.setCustomerName(posObj.optString(GSTRConstants.customername));
                      
            List<ItemDetail> itemDetails = new ArrayList();
            Map<String, JSONArray> advanceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(posArr, GSTRConstants.receiptadvanceid);
            for (String advdetailkey : advanceMap.keySet()) {
                ItemDetail itemDetail = new ItemDetail();
                double camt = 0d;
                double samt = 0d;
                double iamt = 0d;
                double csamt = 0d;
                double advanceamt = 0d;
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
                        itemDetail.setAid(advObj.optString(GSTRConstants.receiptid));
                        itemDetail.setAnum(advObj.optString(GSTRConstants.receiptnumber));
                        itemDetail.setAdt(advObj.optString(GSTRConstants.receiptdate));
                        itemDetail.setGstin(advObj.optString(GSTRConstants.gstin));
                        //itemDetail.setTxval(taxableamount);
                        itemDetail.setHsn_sc(!StringUtil.isNullOrEmpty(advObj.optString(GSTRConstants.HSN_SC))?advObj.optString(GSTRConstants.HSN_SC):"");
                    }
                    String defaultterm = advObj.optString(GSTRConstants.defaultterm);
                    if (advObj.opt(GSTRConstants.receiptlinkdate) != null && (startdate.before((Date) advObj.opt(GSTRConstants.receiptlinkdate))
                            && enddate.after((Date) advObj.opt(GSTRConstants.receiptlinkdate)) || startdate.equals((Date) advObj.opt(GSTRConstants.receiptlinkdate))
                            || enddate.equals((Date) advObj.opt(GSTRConstants.receiptlinkdate)))) {
                        /**
                         * If Advance adjust in the same month then only
                         * consider adjust amount in calculations
                         */
                        if (defaultterm.isEmpty() || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())
                                || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            adjustamount += advObj.optDouble(GSTRConstants.adjustedamount);
                        }
                    }
                    if (advObj.opt(GSTRConstants.refunddate) != null && (startdate.before((Date) advObj.opt(GSTRConstants.refunddate))
                            && enddate.after((Date) advObj.opt(GSTRConstants.refunddate)) || startdate.equals((Date) advObj.opt(GSTRConstants.refunddate))
                            || enddate.equals((Date) advObj.opt(GSTRConstants.refunddate)))) {
                        /**
                         * If refund created for advance then show advance
                         * amount minus refund amount
                         */
                        if (defaultterm.isEmpty() || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())
                                || defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            adjustamount += advObj.optDouble(GSTRConstants.refundamount);
                        }
                    }
                }
                if (taxableamount - adjustamount > 0) {
                    noofdocument++;
                } else {
                    /**
                     * If Advance totally utilized in the same month then no
                     * need to consider such document
                     */
                    continue;
                }
                taxableamt3b+=taxableamount;
                if (reqParams.optBoolean(GSTR3BConstants.DETAILED_VIEW_REPORT, false)) {
                    /**
                     * In GSTR3B Detailed Report, we don't need to subtract
                     * adjusted amount.
                     */
                    taxableamt += taxableamount;
                    advanceamt += taxableamount;
                } else {
                    taxableamt += taxableamount - adjustamount;
                    advanceamt += taxableamount - adjustamount;
                }
                Set<String> termSet = new HashSet();
                double rate = 0d;
                for (int index = 0; index < adDetailArr.length(); index++) {
                    JSONObject invdetailObj = adDetailArr.getJSONObject(index);
                    String defaultterm = invdetailObj.optString(GSTRConstants.defaultterm);
                    if (termSet.add(defaultterm)) {
                        /**
                         * Avoid duplicate entries which come due to join
                         */
                        double termamount;
                        if (reqParams.optBoolean(GSTR3BConstants.DETAILED_VIEW_REPORT, false)) {
                            /**
                             * In GSTR3B Detailed Report, we don't need to
                             * subtract adjusted amount.
                             */
                            termamount = (taxableamount) * invdetailObj.optDouble(GSTRConstants.taxrate) / 100;
                        } else {
                            termamount = (taxableamount - adjustamount) * invdetailObj.optDouble(GSTRConstants.taxrate) / 100;
                        }
                        double termamount3b = (taxableamount) * invdetailObj.optDouble(GSTRConstants.taxrate) / 100;
                        rate += invdetailObj.optDouble(GSTRConstants.taxrate);
                        if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                            iamt += termamount;
                            igst3b+=termamount3b;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            camt += termamount;
                            cgst3b+=termamount3b;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                            samt += termamount;
                            sgst3b+=termamount3b;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                            samt += termamount;
                            sgst3b+=termamount3b;
                        } else if (defaultterm.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                            csamt += termamount;
                            csgst3b+=termamount3b;
                        }
                    }
                }
                itemDetail.setRt(rate);
                itemDetail.setCamt(camt);
                itemDetail.setSamt(samt);
                itemDetail.setAd_amt(advanceamt);
                itemDetail.setIamt(iamt);
                itemDetail.setCsamt(csamt);
                /**
                 * Taxable amount not showing proper in report 
                 */
                if (reqParams.optBoolean(GSTR3BConstants.DETAILED_VIEW_REPORT, false)) {
                    /**
                     * In GSTR3B Detailed Report, we don't need to subtract
                     * adjusted amount.
                     */
                    itemDetail.setTxval(taxableamount);
                } else {
                    itemDetail.setTxval(taxableamount - adjustamount);
                }
                itemDetails.add(itemDetail);
            }
            at.setItms(itemDetails);
            JSONObject jSONObject = new JSONObject(at);
            array.put(jSONObject);
        }
//        System.out.println("merged");
//        System.out.println(array.toString());
        response.put("count", noofdocument);
        response.put("taxableamt", taxableamt);
        response.put("csgst3b", csgst3b);
        response.put("cgst3b", cgst3b);
        response.put("sgst3b", sgst3b);
        response.put("igst3b", igst3b);
        response.put("taxableamt3b", taxableamt3b);
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
    public JSONObject getAdvanceReceiptAdjustmentMerged(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        reqParams.remove("at");
        reqParams.put("atadj", true);
        int noofdocument = 0;
        double taxableamt = 0d;
        List<Object> receiptList = accEntityGstDao.getAdvanceDetailsInSql(reqParams);
        JSONArray bulkData = createJsonForAdvanceDataFetchedFromDB(receiptList, reqParams);
        Map<String, JSONArray> posMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.posid);
        for (String poskey : posMap.keySet()) {
            AT at = new AT();
            JSONArray posArr = posMap.get(poskey);
            JSONObject posObj = posArr.getJSONObject(0);
            at.setPos(posObj.optString(GSTRConstants.pos));
            at.setStcode(posObj.optInt(GSTRConstants.statecode));// Set StateCode i.e. 27 for Maharashtra
            at.setCustomerName(posObj.optString(GSTRConstants.customername));
            List<ItemDetail> itemDetails = new ArrayList();
            Map<String, JSONArray> advanceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(posArr, GSTRConstants.receiptadvanceid);
            for (String advdetailkey : advanceMap.keySet()) {
                ItemDetail itemDetail = new ItemDetail();
                double camt = 0d;
                double samt = 0d;
                double iamt = 0d;
                double csamt = 0d;
                double advanceamt = 0d;
                noofdocument++;
                double rate = 0d;
                JSONArray adDetailArr = advanceMap.get(advdetailkey);
                Set<String> termSet = new HashSet();
                for (int index = 0; index < adDetailArr.length(); index++) {
                    JSONObject invdetailObj = adDetailArr.getJSONObject(index);
                    if (index == 0) {
                        /**
                         * Need to take for only one row only because it repeat
                         * for other rows for different taxes (CGST, IGST etc)
                         */
                        itemDetail.setAid(invdetailObj.optString(GSTRConstants.receiptid));
                        itemDetail.setAnum(invdetailObj.optString(GSTRConstants.receiptnumber));
                        itemDetail.setAdt(invdetailObj.optString(GSTRConstants.receiptdate));
                        itemDetail.setGstin(!StringUtil.isNullOrEmpty(invdetailObj.optString(GSTRConstants.gstin))?invdetailObj.optString(GSTRConstants.gstin):"");
                    }
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
                    } else {
                        advanceamt += invdetailObj.optDouble(GSTRConstants.adjustedamount);
                        taxableamt += invdetailObj.optDouble(GSTRConstants.adjustedamount);
                    }
                }
                itemDetail.setCamt(-camt);
                itemDetail.setSamt(-samt);
                itemDetail.setIamt(-iamt);
                itemDetail.setCsamt(-csamt);
                itemDetail.setRt(rate);
                itemDetail.setAd_amt(-advanceamt);
                itemDetail.setTxval(-advanceamt);
                itemDetails.add(itemDetail);
            }
            at.setItms(itemDetails);
            JSONObject jSONObject = new JSONObject(at);
            array.put(jSONObject);
        }
        response.put("count", noofdocument);
        response.put("taxableamt", -taxableamt);
//        System.out.println("merged");
//        System.out.println(array.toString());
        return response.put("atadj", array);
    }

    /**
     * @Desc : Create json for Advance details
     * @param invoiceList
     * @param params
     * @return
     * @throws JSONException
     */
    public JSONArray createJsonForAdvanceDataFetchedFromDB(List<Object> invoiceList, JSONObject params) throws JSONException {
        JSONArray bulkData = new JSONArray();
        for (Object object : invoiceList) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(GSTRConstants.pos, data[0]!=null ?data[0].toString():"");
            jSONObject.put(GSTRConstants.posid, data[1]!=null?data[1].toString():"");
            jSONObject.put(GSTRConstants.receiptamountdue, data[2]!=null?(Double) data[2]:0);
            jSONObject.put(GSTRConstants.receipttaxamount, data[3]!=null?(Double) data[3]:0);
            jSONObject.put(GSTRConstants.taxrate, data[4]!=null?(Double) data[4]:0);
            jSONObject.put(GSTRConstants.termamount, data[5]!=null?(Double) data[5]:0);
            jSONObject.put(GSTRConstants.defaultterm, data[6]!=null?(String) data[6]:"");
            jSONObject.put(GSTRConstants.receiptadvanceid, data[7]!=null?(String) data[7]:"");
            jSONObject.put(GSTRConstants.adjustedamount, data[8] != null ? (Double) data[8] : 0);
            jSONObject.put(GSTRConstants.receiptamount, data[9] != null ? (Double) data[9] : 0);
            jSONObject.put(GSTRConstants.receiptlinkdate, data[10] != null ? (Date) data[10] : null);
            jSONObject.put(GSTRConstants.receiptdate, data[11]!=null?data[11].toString():"");
            jSONObject.put(GSTRConstants.receiptnumber, data[12]!=null?(String) data[12]:"");
            jSONObject.put(GSTRConstants.receiptid, data[13]!=null?(String) data[13]:"");
            jSONObject.put(GSTRConstants.gstin, data[14]!=null?(String) data[14]:"");
            jSONObject.put(GSTRConstants.statecode, data[15]!=null?Integer.parseInt((String)data[15]):0);
             jSONObject.put(GSTRConstants.customername,data.length>16 && data[16] != null ? (String) data[16] : "");
            jSONObject.put(GSTRConstants.refundamount,data.length>17 && data[17] != null ? (Double) data[17] : 0);
            jSONObject.put(GSTRConstants.refunddate, data.length>18 && data[18] != null ? (Date) data[18] : null);
            
            bulkData.put(jSONObject);
        }
        return bulkData;
    }

    /**
     * @Desc :HSN wise summary of goods /services supplied during the tax period
     * @param reqParams
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    public JSONObject getHSNSummarydetails(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        reqParams.put("hsn", true);
        List<Object> receiptList = accEntityGstDao.getHSNWiseInvoiceDataWithDetailsInSql(reqParams);
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
                    double termamount = invdetailObj.optDouble(GSTRConstants.termamount,0);
                    totalinvoiceamt += termamount;
                    rate += invdetailObj.optDouble(GSTRConstants.taxrate);
                    /**
                     * calculate tax amount by considering partial case
                     */
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
//        System.out.println("HSN");
//       System.out.println(array.toString());
        return response.put("hsn", array);
    }

    /**
     * Create Josn for HSN wise data
     *
     * @param invoiceList
     * @param params
     * @return
     * @throws JSONException
     */
    private JSONArray createHSNJsonForDataFetchedFromDB(List<Object> invoiceList, JSONObject params) throws JSONException {
        JSONArray bulkData = new JSONArray();
        for (Object object : invoiceList) {
            Object[] data = (Object[]) object;
            JSONObject jSONObject = new JSONObject();
            jSONObject.put(GSTRConstants.invoiceid, data[0]!=null?data[0].toString():"");
            jSONObject.put(GSTRConstants.invoicenumber, data[1]!=null?data[1].toString():"");
            jSONObject.put(GSTRConstants.invoicedetailid, data[2]!=null?data[2].toString():"");
            jSONObject.put(GSTRConstants.rate, data[3]!=null?(Double) data[3]:0);
            jSONObject.put(GSTRConstants.quantity, data[4]!=null?(Double) data[4]:0);
            jSONObject.put(GSTRConstants.term, data[5]!=null?data[5].toString():"");
            jSONObject.put(GSTRConstants.termamount, data[6]!=null?(Double) data[6]:0);
            jSONObject.put(GSTRConstants.invamountinbase, data[7]!=null?(Double) data[7]:0);
            jSONObject.put(GSTRConstants.hsncode, data[8]!=null ? data[8].toString() : "");
            jSONObject.put(GSTRConstants.taxrate, data[9]!=null?(Double) data[9]:0);
            jSONObject.put(GSTRConstants.defaultterm, data[10]!=null?(String) data[10]:"");
            jSONObject.put(GSTRConstants.hsnid, data[11]!=null ? (String) data[11]  : "");
            jSONObject.put(GSTRConstants.discpercentage, data[12] != null ? (Character) data[12] : "");
            jSONObject.put(GSTRConstants.discountvalue, data[13] != null ? (Double) data[13] : 0);
            jSONObject.put(GSTRConstants.Description, data[14]!=null?(String) data[14]:"");
            jSONObject.put(GSTRConstants.UQC, data[15] != null ? (String) data[15] :"");
            bulkData.put(jSONObject);
        }
        return bulkData;
    }
    
/**
 * @Desc : Exports supplies including supplies to SEZ/SEZ Developer or deemed exports
 * @param reqParams
 * @return
 * @throws JSONException
 * @throws ServiceException 
 */    
    public JSONObject getExportTypeInvoice(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();   
        reqParams.put("export", true);
        JSONObject params = new JSONObject(reqParams, JSONObject.getNames(reqParams));
        params.put("CustomerType", Constants.CUSTVENTYPE_Export+","+Constants.CUSTVENTYPE_ExportWOPAY);
//        params.put("taxClassType", FieldComboData.TaxClass_Percenatge);
        params.put("typeofjoinisleft", true);
        List<Object> invoiceList = accEntityGstDao.getInvoiceDataWithDetailsInSql(params);
        reqParams.put("cdnr", false);
        reqParams.put("cdnur", false);
    
        JSONArray bulkData = createJsonForDataFetchedFromDB(invoiceList, reqParams);

        /**
         * create customer wise map i.e Return Map which contains customer id as
         * key and its related data array
         */
        Map<String, JSONArray> customerMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.customerid);
        List<EXP> expList = new ArrayList<EXP>();
        EXP expObj = null;
        for (String custkey : customerMap.keySet()) {
            expObj = new EXP();
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
                invoiceDto.setVal(invObj.optDouble(GSTRConstants.invamountinbase));
                if ((!StringUtil.isNullOrEmpty(invObj.optString(GSTRConstants.exportType)) && ((invObj.optString(GSTRConstants.exportType).equalsIgnoreCase(Constants.CUSTVENTYPE_Export) || invObj.optString(GSTRConstants.exportType).equalsIgnoreCase(Constants.CUSTVENTYPE_ExportWOPAY))))) {
                    invoiceDto.setPos(GSTRConstants.otherTerritory);
                } else {
                    invoiceDto.setPos(invObj.optString(GSTRConstants.pos));
                }
                invoiceDto.setExport_type(invObj.optString(GSTRConstants.exportType));
                invoiceDto.setCustomerName(invObj.optString(GSTRConstants.customername));
                invoiceDto.setSbpcode(invObj.optString(GSTRConstants.GSTR1_SHIPPING_PORT,""));
                invoiceDto.setSbdt(invObj.optString(GSTRConstants.GSTR1_SHIPPING_DATE,""));
                invoiceDto.setSbnum(invObj.optString(GSTRConstants.GSTR1_SHIPPING_BILL_NO,""));
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
                        if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString())) {
                            itemDetail.setIamt(termamount);
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString())) {
                            itemDetail.setCamt(termamount);
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString())) {
                            itemDetail.setSamt(termamount);
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString())) {
                            itemDetail.setSamt(termamount);
                        } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString())) {
                            itemDetail.setCsamt(termamount);
                        }
                    }
                    itemDto.setItm_det(itemDetail);
                    itemDtos.add(itemDto);
                }
                invoiceDto.setItms(itemDtos);
                invoiceDtos.add(invoiceDto);
            }
            expObj.setInv(invoiceDtos);
            expList.add(expObj);
            JSONObject jSONObject = new JSONObject(expObj);
            array.put(jSONObject);
        }
//        System.out.println("merged");
//        System.out.println(array.toString());
        return response.put("exp", array);
    }
    
    public JSONObject getDocumentDetails(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        JSONObject dataObject = new JSONObject();
        String companyname=reqParams.optString("companyname");
        /**
         * Add Additional parameter to reqParams
         */
        JSONObject params = new JSONObject(reqParams, JSONObject.getNames(reqParams));
        if (reqParams.has("b2bdocs") && reqParams.optBoolean("b2bdocs")) {
//            params.put("isGSTINnull", false);
//            params.put("registrationType", Constants.GSTRegType_Regular + "," + Constants.GSTRegType_Regular_ECommerce + "," + Constants.GSTRegType_Composition + "," + Constants.GSTRegType_Composition_ECommerce + "," + Constants.GSTRegType_Composition);
            params.put("isDocumentDetails", true);
            params.put("transType", "Invoices for outward supply");
            params.put("typeofjoinisleft", true);
            List<Object> b2bList = accEntityGstDao.getInvoiceDataWithDetailsInSql(params);
            array = createDocsJsonForDataFetched(b2bList, params);
            if (array.length() != 0) {
                array.optJSONObject(0).put("natureofdocument", "Invoices for outward supply");
            } else {
                JSONObject tempObj = new JSONObject();
                tempObj.put("natureofdocument", "Invoices for outward supply");
                tempObj.put("fromInv", " ");
                tempObj.put("toInv", " ");
                tempObj.put("noofinvoices", "0");
                tempObj.put("companyname",companyname);
                array.put(tempObj);
            }
        }
        if (reqParams.has("b2cldocs") && reqParams.optBoolean("b2cldocs")) {
//            params.put("isGSTINnull", true);
//            params.put("isDocumentDetails", true);
//            params.put("registrationType", Constants.GSTRegType_Unregistered + "," + Constants.GSTRegType_Consumer);
//            List<Object> b2cl = accEntityGstDao.getInvoiceDataWithDetailsInSql(params);
//            array = createDocsJsonForDataFetched(b2cl, params);
//            if (array.length() != 0) {
//                array.optJSONObject(0).put("natureofdocument", "Invoices for inward supply from unregistered person");
//            } else {
                JSONObject tempObj = new JSONObject();
                tempObj.put("natureofdocument", "Invoices for inward supply from unregistered person");
                tempObj.put("fromInv", " ");
                tempObj.put("toInv", " ");
                tempObj.put("noofinvoices", "0");
                tempObj.put("companyname",companyname);
                array.put(tempObj);
//            }

        }
        if (reqParams.has("cdnrdocs") && reqParams.optBoolean("cdnrdocs")) {
//            params.put("isGSTINnull", false);
            params.put("isDocumentDetails", true);
            params.put("cdnr", true);
            params.put("typeofjoinisleft",true);
//            params.put("registrationType", Constants.GSTRegType_Regular + "," + Constants.GSTRegType_Regular_ECommerce + "," + Constants.GSTRegType_Composition + "," + Constants.GSTRegType_Composition_ECommerce);
            List<Object> cdnr = accEntityGstDao.getCNDNWithInvoiceDetailsInSql(params);
            List<Object> cdnrtemp = accEntityGstDao.getCNAgainstCustomer(params);
            cdnr.addAll(cdnrtemp);
            array = createDocsJsonForDataFetched(cdnr, params);
            if (array.length() != 0) {
                array.optJSONObject(0).put("natureofdocument", "Credit Note");
            } else {
                JSONObject tempObj = new JSONObject();
                tempObj.put("natureofdocument", "Credit Note");
                tempObj.put("fromInv", " ");
                tempObj.put("toInv", " ");
                tempObj.put("noofinvoices", "0");
                tempObj.put("companyname",companyname);
                array.put(tempObj);
            }
        } else if (reqParams.has("dndocs") && reqParams.optBoolean("dndocs")) {
            params.put("isDocumentDetails", true);
            params.put("cdnr", true);
            params.put("typeofjoinisleft",true);
            List<Object> cdnr = accEntityGstDao.getDNAgainstCustomer(params);
            array = createDocsJsonForDataFetched(cdnr, params);
            if (array.length() != 0) {
                array.optJSONObject(0).put("natureofdocument", "Debit Note");
            } else {
                JSONObject tempObj = new JSONObject();
                tempObj.put("natureofdocument", "Debit Note");
                tempObj.put("fromInv", " ");
                tempObj.put("toInv", " ");
                tempObj.put("noofinvoices", "0");
                tempObj.put("companyname", companyname);
                array.put(tempObj);
            }
        }
        if (reqParams.has("advancedocs") && reqParams.optBoolean("advancedocs")) {
            params.put("isDocumentDetails", true);
            params.put("typeofjoinisleft",true);
            List<Object> advanceReceipt = accEntityGstDao.getAdvanceDetailsInSql(params);
            array = createDocsJsonForDataFetched(advanceReceipt, params);
            if (array.length() != 0) {
                array.optJSONObject(0).put("natureofdocument", "Receipt Voucher");
            } else {
                JSONObject tempObj = new JSONObject();
                tempObj.put("natureofdocument", "Receipt Voucher");
                tempObj.put("fromInv", " ");
                tempObj.put("toInv", " ");
                tempObj.put("noofinvoices", "0");
                tempObj.put("companyname",companyname);
                array.put(tempObj);
            }
        }

        if (reqParams.has("paymentdocs") && reqParams.optBoolean("paymentdocs")) {
            params.put("isDocumentDetails", true);
            params.put("typeofjoinisleft",true);
            List<Object> makePayment = accEntityGstDao.getPaymentData(params);
            array = createDocsJsonForDataFetched(makePayment, params);
            if (array.length() != 0) {
                array.optJSONObject(0).put("natureofdocument", "Refund Voucher");
            } else {
                JSONObject tempObj = new JSONObject();
                tempObj.put("natureofdocument", "Refund Voucher");
                tempObj.put("fromInv", " ");
                tempObj.put("toInv", " ");
                tempObj.put("noofinvoices", "0");
                tempObj.put("companyname",companyname);
                array.put(tempObj);
            }
        }
        if (reqParams.has("dodocs") && reqParams.optBoolean("dodocs")) {
            params.put("isDocumentDetails", true);
            params.put("typeofjoinisleft",true);
            List<Object> deliveryOrder = accEntityGstDao.getDelieveryOrderData(params);
            array = createDocsJsonForDataFetched(deliveryOrder, params);
            if (array.length() != 0) {
                array.optJSONObject(0).put("natureofdocument", "Delivery Challan in case other than by way of supply");
            } else {
                JSONObject tempObj = new JSONObject();
                tempObj.put("natureofdocument", "Delivery Challan in case other than by way of supply");
                tempObj.put("fromInv", " ");
                tempObj.put("toInv", " ");
                tempObj.put("noofinvoices", "0");
                tempObj.put("companyname",companyname);
                array.put(tempObj);
            }
        }
        return response.put("docs", array);
    }
      private JSONArray createDocsJsonForDataFetched(List<Object> invoiceList, JSONObject params) throws JSONException {
          JSONArray bulkData = new JSONArray();
          String companyname=params.optString("companyname");
          Set<String> seqFormatId = new HashSet<>();
             int count =1;
             String firstInvoiceNumber = null;
           
             Map<String, String> prevSeqInvoice = new HashMap<>();
             String lastIdxInvoiceNumber = null;
             for (Object obj : invoiceList) {
                 Object[] objArr = (Object[]) obj;
                 if (invoiceList.indexOf(obj) == invoiceList.size() - 1) {
                     lastIdxInvoiceNumber = (String) objArr[0];
                 }
                 if (seqFormatId.add((String) objArr[1])) {
                     //new 
                     // reinitialize count
                     if (firstInvoiceNumber != null) {
                         // For First time don't add JSON
                         JSONObject json = new JSONObject();
                         json.put("fromInv", firstInvoiceNumber);
                         for (Map.Entry<String, String> entrySet : prevSeqInvoice.entrySet()) {
                             String value = entrySet.getValue();
                             json.put("toInv", value);
                         }
                         json.put("noofinvoices", count);
                         json.put("companyname",companyname);
                         bulkData.put(json);
                         firstInvoiceNumber = (String) objArr[0];
                         count = 1;
                     } else {
                         firstInvoiceNumber = (String) objArr[0];
                     }
                 } else {
                     // update count seqformat present
                     count++;
                 }
                 prevSeqInvoice.clear();
                 prevSeqInvoice.put((String) objArr[1], (String) objArr[0]);
             }
             if (!StringUtil.isNullOrEmpty(lastIdxInvoiceNumber)) {
                 JSONObject json = new JSONObject();
                 json.put("fromInv", firstInvoiceNumber);
                 json.put("toInv", lastIdxInvoiceNumber);
                 json.put("noofinvoices", count);
                 json.put("companyname",companyname);
                 bulkData.put(json);
             }
          return bulkData;
      }
    public JSONObject getGSTMissingInvoice(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        JSONArray array = new JSONArray();
        String reason="";
        /**
         * Get Invoice data in Mismatch report
         */
        List<Object> invoiceList = new ArrayList();
        if (reqParams.optBoolean("isInvoice")) {
            invoiceList = accEntityGstDao.getGSTMissingInvoice(reqParams);
            if (!reqParams.optBoolean("isRCMApplicable", false)) {
                List<Object> goodsReceiptList = accEntityGstDao.getGSTMissingPurchaseInvoice(reqParams);
               invoiceList.addAll(goodsReceiptList);
            }
        }
        /**
         * Get sales order data.
         */
        List<Object> sorderList = accEntityGstDao.getGSTMissingSalesOrder(reqParams);
        invoiceList.addAll(sorderList);
        /**
         * Get purchase order data.
         */
        List<Object> porderList = gstr2Dao.getGSTMissingPurchaseOrder(reqParams);
        invoiceList.addAll(porderList);
        /**
         * Get Goods Receipt Order data.
         */
        List<Object> GRNList = gstr2Dao.getGSTMissingGRN(reqParams);
        invoiceList.addAll(GRNList);
        /**
         * Get Delivery order data.
         */
        List<Object> dorderList = accEntityGstDao.getMissingDelieveryOrderData(reqParams);
        invoiceList.addAll(dorderList);
        
        /**
         * Get CN/ DN data in mismatch report for All section except  - RCM Sales to Unregistered Person
         */
        if (!reqParams.optBoolean("isRCMApplicable", false)) {
            reqParams.put("entitycolnum", reqParams.optInt("cnentitycolnum"));
            reqParams.put("entityValue", reqParams.optString("cnentityValue"));
            List<Object> cnList = accEntityGstDao.getGSTMissingCN(reqParams);
            invoiceList.addAll(cnList);
            reqParams.put("entitycolnum", reqParams.optInt("dnentitycolnum"));
            reqParams.put("entityValue", reqParams.optString("dnentityValue"));
            List<Object> dnList = gstr2Dao.getGSTMissingDN(reqParams);
            invoiceList.addAll(dnList);
        }
        JSONArray bulkData = createJsonForMismatchReportDataFetchedFromDB(invoiceList, reqParams);
        Set<String> invoiceno = new HashSet();
        JSONObject dataObj = new JSONObject();
        Map<String, JSONArray> invoiceMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(bulkData, GSTRConstants.invoiceid);
        for (String invkey : invoiceMap.keySet()) {
            JSONArray invArr = invoiceMap.get(invkey);
            JSONObject invObj = invArr.getJSONObject(0);
            if (invoiceno.add(invObj.optString(GSTRConstants.invoicenumber))) {
                dataObj = new JSONObject();
            }
            dataObj.put("billno", invObj.optString(GSTRConstants.invoicenumber));
            dataObj.put("returnno", invObj.optString(GSTRConstants.posid));
            dataObj.put("customer", invObj.optString(GSTRConstants.customerid));
            dataObj.put("billdate", invObj.optString(GSTRConstants.entrydate));
            dataObj.put("amount", invObj.optString(GSTRConstants.invamountinbase));
            dataObj.put("gstin",invObj.optString(GSTRConstants.gstin));
            dataObj.put("gsttypename", invObj.optString(GSTRConstants.GSTDETAILS_TYPEVALUE, ""));
            // Provide check for HSN/SAC Code should not be greater than 8 digits (For India) ERM-1092
            if(reqParams.has("ishsninvalid") && reqParams.optBoolean("ishsninvalid") && !StringUtil.isNullOrEmpty(invObj.optString(GSTRConstants.hsncode)) && invObj.optString(GSTRConstants.hsncode).length() > 8){
                reason=GSTRConstants.HSN_Invalid;
                dataObj.put("reason", reason);
            }
            /**
             * Create Invoice details Map * i.e Return Map which contains
             * Invoice detail id as key and its related data array
             */
            boolean addrec = true;
            if (reqParams.optBoolean("istaxclassmismatch")) {
                addrec = false;
            }
            String productId = "";
            Set<String> productid = new HashSet();
            Map<String, JSONArray> invoiceDetailMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(invArr, GSTRConstants.invoicedetailid);
            List<ItemDto> itemDtos = new ArrayList<ItemDto>();
            int count = 1;
            double inv_iamt = 0, inv_camt = 0, inv_samt = 0, inv_csamt = 0,termamount=0;
            for (String invdetailkey : invoiceDetailMap.keySet()) {
                count++;
                JSONArray invDetailArr = invoiceDetailMap.get(invdetailkey);
                for (int index = 0; index < invDetailArr.length(); index++) {
                    JSONObject invdetailObj = invDetailArr.getJSONObject(index);
                    double rate = invdetailObj.optDouble(GSTRConstants.rate);
                    productId = invdetailObj.optString(GSTRConstants.productid);
                    if (!reqParams.optBoolean("istaxclassmismatch")) {
                        productid.add(productId);
                    }
                    String term = invdetailObj.optString(GSTRConstants.defaultterm);

                    termamount = invdetailObj.optDouble(GSTRConstants.termamount);
                    if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputIGST").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputIGST").toString())) {
                        inv_iamt = inv_iamt + termamount;
                    } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCGST").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCGST").toString())) {
                        inv_camt = inv_camt + termamount;
                    } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputSGST").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputSGST").toString())) {
                        inv_samt = inv_samt + termamount;
                    } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputUTGST").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputUTGST").toString())) {
                    } else if (term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCESS").toString())) {
                        inv_csamt = inv_csamt + termamount;
                    }
                    if (reqParams.optBoolean("istaxclassmismatch")) {
                        int invtermtype = invdetailObj.optInt(GSTRConstants.INV_TERM_TYPE);
                        if (invtermtype == 2) {
                            addrec = true;
                            productid.add(productId);
                            dataObj.put("istaxroundedreason", true);
                        } else {
                            double eltrTaxRate = invdetailObj.optDouble(GSTRConstants.eltrTaxRate);
                            double invTermRate = invdetailObj.optDouble(GSTRConstants.invTermRate);
                            if ((eltrTaxRate != invTermRate) && !(term.equalsIgnoreCase(LineLevelTerms.GSTName.get("OutputCESS").toString()) || term.equalsIgnoreCase(LineLevelTerms.GSTName.get("InputCESS").toString()))) {
                                addrec = true;
                                productid.add(productId);
                                dataObj.put("istaxclassreason", true);
                            }
                        }
                    }
                }
            }
            /**
             * If not product id then remove [] (empty bracket) and show empty value
             */
            dataObj.put("productid", productid.toString().equalsIgnoreCase("[]")? "" : productid.toString());
            dataObj.put("taxamount", inv_iamt+inv_camt+inv_samt+inv_csamt);
            if (invObj.optInt(GSTRConstants.statecode) == 1) {
                dataObj.put("typeofinvoice", "SI");
            } else if (invObj.optInt(GSTRConstants.statecode) == 3) {
                dataObj.put("typeofinvoice", "CN");
            }  else if (invObj.optInt(GSTRConstants.statecode) == 4) {
                dataObj.put("typeofinvoice", "DN");
            } else if (invObj.optInt(GSTRConstants.statecode) == Constants.Acc_Sales_Order_ModuleId) {
                dataObj.put("typeofinvoice", "SO");
            } else if (invObj.optInt(GSTRConstants.statecode) == Constants.Acc_Purchase_Order_ModuleId) {
                dataObj.put("typeofinvoice", "PO");
            } else if (invObj.optInt(GSTRConstants.statecode) == Constants.Acc_Delivery_Order_ModuleId) {
                dataObj.put("typeofinvoice", "DO");
            } else if (invObj.optInt(GSTRConstants.statecode) == Constants.Acc_Goods_Receipt_ModuleId) {
                dataObj.put("typeofinvoice", "GRN");
            } else {
                dataObj.put("typeofinvoice", "PI");
            }

            if (addrec) {
                if (StringUtil.isNullOrEmpty(reason)) {
                    String ReasonForMismatch = getReasonForMismatch(reqParams, invObj);
                    dataObj.put("reason", ReasonForMismatch);
                }
                array.put(dataObj);
                reason="";
            }
        }

        return response.put("data", array);
    }
    public String getReasonForMismatch(JSONObject reqParams, JSONObject invObj) throws JSONException {
        String reason = "";
        String section = reqParams.optString("section");
        if(StringUtil.isNullOrEmpty(section)){
            section=reqParams.optString("typeofinvoice");
        }
        switch (section) {
            case GSTRConstants.GSTMisMatch_SECTION_HSNNotAvailable: {
                reason = GSTRConstants.HSN_NotAvailable;
            }
            break;
            case GSTRConstants.GSTMisMatch_SECTION_UQCNotAvailable: {
                /**
                 * UQC Not Available
                 */
                reason = GSTRConstants.UQC_NotAvailable;
            }
            break;
            case GSTRConstants.GSTMisMatch_SECTION_GSTINBlank: {
                /**
                 * GSTIN Blank
                 */
                reason = GSTRConstants.GSTIN_NotAvailable;
            }
            break;
            case GSTRConstants.GSTMisMatch_SECTION_GSTINnonBlank: {
                /**
                 * GSTIN non Blank
                 */
                reason = GSTRConstants.GSTIN_Available;
            }
            break;
            case GSTRConstants.GSTMisMatch_SECTION_GSTINInvalid:{
                /**
                 * GSTIN invalid
                 */
                reason = GSTRConstants.GSTIN_Invalid;
            }
            break;
            case GSTRConstants.GSTMisMatch_SECTION_GSTRegistrationTypeblank: {
                /**
                 * GST Registration Type blank
                 */

                reason = GSTRConstants.GST_Registration_NotAvailable;
                /**
                 * If GST Customer/ Vendor Registration type is wrong combination
                 * GST combination more details - ERP-35464
                 */
                if (!StringUtil.isNullOrEmpty(invObj.optString(GSTRConstants.GSTREG_TYPEID, ""))) {
                    reason = GSTRConstants.GST_Registration_WronglyTagged;
            }
            }
            break;
            case GSTRConstants.GSTMisMatch_SECTION_CustomerVendorTypeblank: {
                /**
                 * Customer/Vendor Type blank
                 */
                reason = GSTRConstants.GST_Customertype_NotAvailable;
                /**
                 * If GST Customer/ Vendor type is wrong combination
                 * GST combination more details - ERP-35464
                 */
                if (!StringUtil.isNullOrEmpty(invObj.optString(GSTRConstants.GSTCustomer_Vendor_TYPEID, ""))) {
                    reason = GSTRConstants.GST_Customertype_WronglyTagged;
            }
            }
            break;
            case GSTRConstants.GSTMisMatch_SECTION_RCMSalestoUnregisteredPerson: {
                /**
                 * RCM Sales to Unregistered Person
                 */
                reason = GSTRConstants.RCM_Sale;
            }
            break;
            case GSTRConstants.GSTMisMatch_SECTION_StateMismatch: {
                /**
                 * State Mismatch
                 */
                reason = GSTRConstants.BillingState_Mismatch;
            }
            break;
            case GSTRConstants.GSTMisMatch_SECTION_ManuallyenteredInvoiceNumber: {
                /**
                 * Manually entered Invoice Number
                 */
                reason = GSTRConstants.Manually_EnteredInvNo;
            }
            break;
            case GSTRConstants.Sec_GST_History_Not_Present: {
                /**
                 * GST-History not present
                 */
                reason = GSTRConstants.GST_HistoryNotPresent;
            }
//            case GSTRConstants.GSTMisMatch_SECTION_INVALIDCN: {
//                /**
//                 * CN/DN without Linking Invoice
//                 */
//                reason = GSTRConstants.CNWithoutLinkingInvoice;
//        }
        }

        return reason;
    }
}
