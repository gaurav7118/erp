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

import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.gst.dto.HSN;
import com.krawler.spring.accounting.gst.dto.B2B;
import com.krawler.spring.accounting.gst.dto.CDNR;
import com.krawler.spring.accounting.gst.dto.CDNUR;
import com.krawler.spring.accounting.gst.dto.gstr2.ITCDetails;
import com.krawler.spring.accounting.gst.dto.InvoiceDto;
import com.krawler.spring.accounting.gst.dto.ItemDetail;
import com.krawler.spring.accounting.gst.dto.TXP;
import com.krawler.spring.accounting.gst.dto.gstr2.IMPGInvoiceData;
import com.krawler.spring.accounting.gst.dto.gstr2.IMPGItem;
import com.krawler.spring.accounting.gst.dto.gstr2.IMPSInvoiceData;
import com.krawler.spring.accounting.gst.dto.gstr2.ITCReversal;
import com.krawler.spring.accounting.gst.dto.ItemDto;
import com.krawler.spring.accounting.gst.dto.gstr1.GSTR_Summary;
import com.krawler.spring.accounting.gst.dto.gstr1.GSTR_SummaryData;
import com.krawler.spring.accounting.gst.dto.gstr2.NILItem;
import com.krawler.spring.accounting.gst.dto.gstr2.NilRatedData;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class GSTR2ServicesImpl {

    public static void main(String[] args) {
        try {
            GSTR2ServicesImpl services = new GSTR2ServicesImpl();
//            services.getB2BInvoices("29HJKPS9689A8Z4", "072016", 'Y', null, null);
//            services.getImportofGoodsInvoices("29HJKPS9689A8Z4", "072016");
//            services.getImportofServicesBills("29HJKPS9689A8Z4", "072016");
//            services.getCDNInvoices("29HJKPS9689A8Z4", "072016", null, null);
//            services.getNilRatedSupplies("29HJKPS9689A8Z4", "072016");
//            services.getTaxliabilityunderReverseChargeSummary("29HJKPS9689A8Z4", "072016");
//            services.getTaxPaidUnderReverseCharge("29HJKPS9689A8Z4", "072016");
//            services.getHSNSummaryofInwardsupplies("29HJKPS9689A8Z4", "072016");
//            services.getITCReversalDetails("29HJKPS9689A8Z4", "072016");
//            services.getB2BUnregisteredInvoice("29HJKPS9689A8Z4", "072016");
//            services.getCDNUnregisteredData("29HJKPS9689A8Z4", "072016", "Y", null, null);
            services.getGSTR2Summary("29HJKPS9689A8Z4", "072016");
        } catch (JSONException ex) {
            Logger.getLogger(GSTR2ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JSONObject getB2BInvoices(String gstin, String ret_period, char action_required, String ctin, String from_time) throws JSONException {
        JSONObject response = new JSONObject();
//        response.put(Constants.RES_success, false);
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        B2B b2bObj = new B2B();
        b2bObj.setCtin("01AABCE2207R1Z5");
        b2bObj.setCfs('Y');
        List<InvoiceDto> inv = new ArrayList<InvoiceDto>();
        InvoiceDto invoice = new InvoiceDto();
        invoice.setFlag('A');
        invoice.setCflag('U');
        invoice.setOpd("2017-05");
        invoice.setChksum("AflJufPlFStqKBZ");
        invoice.setInum("S008400");
        invoice.setIdt("24-11-2016");
        invoice.setVal(729248.16);
        invoice.setPos("06");
        invoice.setRchrg('N');
        invoice.setUpdby('R');
        invoice.setInv_typ("R");

        List<ItemDto> itmeList = new ArrayList<ItemDto>();
        ItemDto itemDto = new ItemDto();
        itemDto.setNum(01);
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setRt(5);
        itemDetail.setTxval(6210.99);
        itemDetail.setIamt(0);
        itemDetail.setCamt(614.44);
        itemDetail.setSamt(5.68);
        itemDetail.setCsamt(621.09);
        itemDto.setItm_det(itemDetail);

        ITCDetails itc = new ITCDetails();
        itc.setElg("ip");
        itc.setTx_i(147.2);
        itc.setTx_c(159.3);
        itc.setTx_cs(0);
        itc.setTx_s(159.3);
        itemDto.setItc(itc);

        itmeList.add(itemDto);
        invoice.setItms(itmeList);

        // add second record
        itemDto = new ItemDto();
        itemDto.setNum(02);
        itemDetail = new ItemDetail();
        itemDetail.setRt(28);
        itemDetail.setTxval(1000.05);
        itemDetail.setIamt(0);
        itemDetail.setCamt(887.44);
        itemDetail.setSamt(5.68);
        itemDetail.setCsamt(50.12);
        itemDto.setItm_det(itemDetail);

        itc = new ITCDetails();
        itc.setElg("ip");
        itc.setTx_i(147.2);
        itc.setTx_c(159.3);
        itc.setTx_cs(0);
        itc.setTx_s(159.3);
        itemDto.setItc(itc);

        itmeList.add(itemDto);
        invoice.setItms(itmeList);

        inv.add(invoice);
        b2bObj.setInv(inv);

        JSONObject jsonObj = new JSONObject(b2bObj);

        JSONArray b2bArray = new JSONArray();
        b2bArray.put(jsonObj);
        response.put("b2b", b2bArray);
        System.out.println("b2b :: " + response);
//        response.put(Constants.RES_success, true);
        return response;

    }

    public JSONObject getImportofGoodsInvoices(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<IMPGInvoiceData> imp_gList = new ArrayList<IMPGInvoiceData>();
        IMPGInvoiceData imp_g = new IMPGInvoiceData();
        imp_g.setIs_sez("Y");
        imp_g.setStin("01AABCE2207R1Z5");
        imp_g.setBoe_num("2566232");
        imp_g.setBoe_dt("18-4-2016");
        imp_g.setBoe_val(338203.29);
        imp_g.setChksum("AflJufPlFStqKBZ");
        imp_g.setPort_code("002409");

        List<IMPGItem> itms = new ArrayList<IMPGItem>();
        IMPGItem item = new IMPGItem();
        item.setNum(1);
        item.setRt(10.5);
        item.setIamt(159.3);
        item.setTxval(582.88);
        item.setCsamt(159.3);
        item.setElg("ip");
        item.setTx_i(123.02);
        item.setTx_cs(0);
        itms.add(item);

        item = new IMPGItem();
        item.setNum(2);
        item.setRt(10.5);
        item.setIamt(159.3);
        item.setTxval(582.88);
        item.setCsamt(159.9);
        item.setElg("ip");
        item.setTx_i(123.02);
        item.setTx_cs(0);
        itms.add(item);
        imp_g.setItms(itms);

        imp_gList.add(imp_g);
        response.put("imp_g", imp_gList);
        System.out.println("imp_g : " + response);
        return response;
    }

    public JSONObject getImportofServicesBills(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<IMPSInvoiceData> imp_sList = new ArrayList<IMPSInvoiceData>();
        IMPSInvoiceData imp_s = new IMPSInvoiceData();
        imp_s.setInum("85619");
        imp_s.setIdt("22-03-2016");
        imp_s.setIval(962559.86);
        imp_s.setChksum("AflJufPlFStqKBZ");
        imp_s.setPos("28");

        List<IMPGItem> itms = new ArrayList<IMPGItem>();
        IMPGItem item = new IMPGItem();
        item.setNum(1);
        item.setTxval(582.88);
        item.setElg("is");
        item.setRt(10.5);
        item.setIamt(159.3);
        item.setCsamt(0);
        item.setTx_i(50);
        item.setTx_cs(0);
        itms.add(item);

        imp_s.setItms(itms);

        imp_sList.add(imp_s);
        response.put("imp_s", imp_sList);
        System.out.println("imp_s : " + response);
        return response;
    }

    public JSONObject getCDNInvoices(String gstin, String ret_period, String action_required, String from_time) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<CDNR> listCDNR = new ArrayList<CDNR>();
        CDNR cdn = new CDNR();
        cdn.setCtin("01AAAAP1208Q1ZS");
        cdn.setCfs("Y");

        List<InvoiceDto> listInvoiceDto = new ArrayList<InvoiceDto>();

        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setFlag('A');
        invoiceDto.setChksum("adsdsfsjskssssq");
        invoiceDto.setNtty("C");
        invoiceDto.setNt_num("533515");
        invoiceDto.setNt_dt("23-09-2016");
        invoiceDto.setInum("915914");
        invoiceDto.setRsn("Not Mentioned");
        invoiceDto.setP_gst('N');
        invoiceDto.setIdt("23-09-2016");
        invoiceDto.setUpdby('S');
        invoiceDto.setOpd("2016-10");
        invoiceDto.setCflag('N');

        List<ItemDto> itms = new ArrayList<ItemDto>();
        ItemDto itemDto = new ItemDto();
        itemDto.setNum(1);
        ItemDetail itm_det = new ItemDetail();
        itm_det.setRt(10.1);
        itm_det.setTxval(6210.99);
        itm_det.setIamt(0);
        itm_det.setCamt(614.44);
        itm_det.setSamt(5.68);
        itm_det.setCsamt(621.09);
        itemDto.setItm_det(itm_det);

        ITCDetails itc = new ITCDetails();
        itc.setElg("ip");
        itc.setTx_c(159.3);
        itc.setTx_cs(159.3);
        itc.setTx_s(0);
        itemDto.setItc(itc);

        itms.add(itemDto);

        invoiceDto.setItms(itms);
        listInvoiceDto.add(invoiceDto);
        cdn.setNt(listInvoiceDto);
        listCDNR.add(cdn);

        response.put("cdn", listCDNR);
        System.out.println("cdn :: " + response);
        return response;
    }

    public JSONObject getNilRatedSupplies(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
        NilRatedData nil_supplies = new NilRatedData();
        nil_supplies.setChksum(gstin);
        NILItem item = new NILItem();
        item.setCpddr(0);
        item.setExptdsply(5394970.87);
        item.setNgsply(992.93);
        item.setNilsply(0);
        nil_supplies.setInter(item);

        item = new NILItem();
        item.setCpddr(1000);
        item.setExptdsply(5394970.87);
        item.setNgsply(992.93);
        item.setNilsply(0);
        nil_supplies.setIntra(item);

        JSONObject jobj = new JSONObject(nil_supplies);
        response.put("nil_supplies", jobj);
        System.out.println("nil_supplies : " + response);
        return response;
    }

    /**
     * Advance Tax Paid for reverse charge supplies
     *
     * @param gstin
     * @param ret_period
     * @return
     * @throws JSONException
     */
    public JSONObject getTaxliabilityunderReverseChargeSummary(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<TXP> advanceTaxPaidReverseChangeList = new ArrayList<TXP>();
        TXP txp = new TXP();
        txp.setChksum("ASDFGJKLPTKBBJKBB");
        txp.setPos("05");
        txp.setSply_ty("INTER");
        List<ItemDetail> itms = new ArrayList<ItemDetail>();
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setNum(1);
        itemDetail.setRt(5);
        itemDetail.setAd_amt(100);
        itemDetail.setIamt(9400);
        itemDetail.setCamt(0);
        itemDetail.setSamt(0);
        itemDetail.setCsamt(500);
        itms.add(itemDetail);
        txp.setItms(itms);

        advanceTaxPaidReverseChangeList.add(txp);

        txp = new TXP();
        txp.setChksum("ASDFGJKLPTKBBJKBB");
        txp.setPos("05");
        txp.setSply_ty("INTRA");
        itms = new ArrayList<ItemDetail>();
        itemDetail = new ItemDetail();
        itemDetail.setNum(2);
        itemDetail.setRt(5);
        itemDetail.setAd_amt(100);
        itemDetail.setIamt(0);
        itemDetail.setCamt(0);
        itemDetail.setSamt(0);
        itemDetail.setCsamt(500);
        itms.add(itemDetail);
        txp.setItms(itms);

        advanceTaxPaidReverseChangeList.add(txp);

        response.put("txi", advanceTaxPaidReverseChangeList);
        System.out.println("txi : " + response);
        return response;
    }

    public JSONObject getTaxPaidUnderReverseCharge(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<TXP> advanceTaxPaidReverseChangeList = new ArrayList<TXP>();
        TXP txp = new TXP();
        txp.setChksum("ASDFGJKLPTKBBJKBB");
        txp.setPos("05");
        txp.setSply_ty("INTER");
        List<ItemDetail> itms = new ArrayList<ItemDetail>();
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setNum(1);
        itemDetail.setRt(5);
        itemDetail.setAd_amt(10000);
        itemDetail.setIamt(9400);
        itemDetail.setCamt(0);
        itemDetail.setSamt(0);
        itemDetail.setCsamt(500);
        itms.add(itemDetail);
        txp.setItms(itms);

        advanceTaxPaidReverseChangeList.add(txp);

        txp = new TXP();
        txp.setChksum("ASDFGJKLPTKBBJKBB");
        txp.setPos("05");
        txp.setSply_ty("INTRA");
        itms = new ArrayList<ItemDetail>();
        itemDetail = new ItemDetail();
        itemDetail.setNum(2);
        itemDetail.setRt(5);
        itemDetail.setAd_amt(100);
        itemDetail.setIamt(0);
        itemDetail.setCamt(0);
        itemDetail.setSamt(0);
        itemDetail.setCsamt(500);
        itms.add(itemDetail);
        txp.setItms(itms);

        advanceTaxPaidReverseChangeList.add(txp);

        response.put("txpd", advanceTaxPaidReverseChangeList);
        System.out.println("txpd : " + response);
        return response;
    }

    public JSONObject getHSNSummaryofInwardsupplies(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<HSN> listhsn = new ArrayList<HSN>();
        HSN hsn = new HSN();
        hsn.setChksum(gstin);

        List<ItemDetail> data = new ArrayList<ItemDetail>();
        ItemDetail itemDetail_Hsn = new ItemDetail();
        itemDetail_Hsn.setNum(1);
        itemDetail_Hsn.setHsn_sc("40561111");
        itemDetail_Hsn.setDesc("Goods Description");
        itemDetail_Hsn.setUqc("kg");
        itemDetail_Hsn.setQty(80);
        itemDetail_Hsn.setVal(9000.5);
        itemDetail_Hsn.setTxval(8451.65);
        itemDetail_Hsn.setIamt(0);
        itemDetail_Hsn.setCamt(0.83);
        itemDetail_Hsn.setSamt(6736920.69);
        itemDetail_Hsn.setCsamt(0);
        data.add(itemDetail_Hsn);

        hsn.setData(data);

        JSONObject jobj = new JSONObject(hsn);

        JSONArray det = jobj.getJSONArray("data");
        jobj.remove("data");
        jobj.put("det", det);

        response.put("hsnsum", jobj);
        System.out.println("hsnsum :: " + response);
        return response;
    }
    
    public JSONObject getB2BUnregisteredInvoice(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<B2B> listb2bur = new ArrayList<B2B>();

        B2B b2bur = new B2B();
        List<InvoiceDto> b2burInvoiceList = new ArrayList<InvoiceDto>();
        InvoiceDto invoice = new InvoiceDto();
        invoice.setChksum("AflJufPlFStqKBZ");
        invoice.setInum("S008400");
        invoice.setIdt("24-11-2016");
        invoice.setVal(729248.16);

        List<ItemDto> itms = new ArrayList<ItemDto>();
        ItemDto item = new ItemDto();
        item.setNum(1);
        ItemDetail itm_det = new ItemDetail();
        itm_det.setRt(5);
        itm_det.setTxval(6210.99);
        itm_det.setCamt(614.44);
        itm_det.setSamt(5.68);
        itm_det.setCsamt(621.09);
        item.setItm_det(itm_det);
        ITCDetails itc = new ITCDetails();
        itc.setElg("ip");
        itc.setTx_c(159.3);
        itc.setTx_cs(159.3);
        itc.setTx_s(0);
        item.setItc(itc);
        itms.add(item);

        item = new ItemDto();
        item.setNum(2);
        itm_det = new ItemDetail();
        itm_det.setRt(28);
        itm_det.setTxval(1005.05);
        itm_det.setCamt(887.44);
        itm_det.setSamt(5.68);
        itm_det.setCsamt(50.12);
        item.setItm_det(itm_det);
        itc = new ITCDetails();
        itc.setElg("ip");
        itc.setTx_c(159.3);
        itc.setTx_cs(159.3);
        itc.setTx_s(0);
        item.setItc(itc);
        itms.add(item);

        invoice.setItms(itms);
        b2burInvoiceList.add(invoice);

        b2bur.setInv(b2burInvoiceList);
        listb2bur.add(b2bur);

        response.put("b2bur", listb2bur);
        System.out.println("response : " + response);
        return response;
    }
    
    public JSONObject getITCReversalDetails(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
        ITCReversal itc_rvsl = new ITCReversal();
        itc_rvsl.setChksum(gstin);
        ItemDetail item = new ItemDetail();
        item.setIamt(0);
        item.setCamt(13);
        item.setSamt(13);
        item.setCsamt(12);
        itc_rvsl.setRule2_2(item);
        item = new ItemDetail();
        item.setIamt(0);
        item.setCamt(13);
        item.setSamt(13);
        item.setCsamt(12);
        itc_rvsl.setRule7_1_m(item);
        item = new ItemDetail();
        item.setIamt(0);
        item.setCamt(13);
        item.setSamt(13);
        item.setCsamt(12);
        itc_rvsl.setRule8_1_h(item);
        item = new ItemDetail();
        item.setIamt(0);
        item.setCamt(13);
        item.setSamt(13);
        item.setCsamt(12);
        itc_rvsl.setRule7_2_a(item);
        item = new ItemDetail();
        item.setIamt(0);
        item.setCamt(13);
        item.setSamt(13);
        item.setCsamt(12);
        itc_rvsl.setRule7_2_b(item);
        item = new ItemDetail();
        item.setIamt(0);
        item.setCamt(13);
        item.setSamt(13);
        item.setCsamt(12);
        itc_rvsl.setRevitc(item);
        item = new ItemDetail();
        item.setIamt(0);
        item.setCamt(13);
        item.setSamt(13);
        item.setCsamt(12);
        itc_rvsl.setOther(item);
        
        JSONObject jobj = new JSONObject(itc_rvsl);
        response.put("itc_rvsl", jobj);
        System.out.println("itc_rvsl : "+response);
        return response;
    }    

    public JSONObject getCDNUnregisteredData(String gstin, String ret_period, String action_required, String ctin, String from_time ) throws JSONException{
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
        List<CDNUR> listCDNUR = new ArrayList<CDNUR>();
        CDNUR cdnur = new CDNUR();
        cdnur.setRtin("01AAAAP1208Q1ZS");
        cdnur.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        cdnur.setNtty("C");
        cdnur.setNt_num("533515");
        cdnur.setNt_dt("23-09-2016");
        cdnur.setRsn("Not Mentioned");
        cdnur.setP_gst('N');
        cdnur.setInum("915914");
        cdnur.setIdt("23-09-2016");
        
        List<ItemDto> itms = new ArrayList<ItemDto>();
        ItemDto itemDto = new ItemDto();
        itemDto.setNum(1);
        ItemDetail itm_det = new  ItemDetail();
        itm_det.setRt(10);
        itm_det.setTxval(5225.28);
        itm_det.setIamt(845.22);
        itm_det.setCsamt(789.52);
        itemDto.setItm_det(itm_det);
        ITCDetails itc_det = new ITCDetails();
        itc_det.setElg("ip");
        itc_det.setTx_s(159.03);
        itc_det.setTx_s(159.03);
        itc_det.setTx_cs(0);
        
        itms.add(itemDto);
        
        cdnur.setItms(itms);
        
        listCDNUR.add(cdnur);
        
        response.put("cdnur", listCDNUR);
        System.out.println("cdnur :: "+response);
        return response;           
        
    }
    
    public JSONObject getGSTR2Summary(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
        
        GSTR_SummaryData summaryData = new GSTR_SummaryData();
        summaryData.setGstin(gstin);
        summaryData.setRet_period(ret_period);
        summaryData.setChksum("AflJufPlFStqKBZ");
        
        List<GSTR_Summary> sec_sum = new ArrayList<GSTR_Summary> ();
        
        GSTR_Summary b2B_CDNR_CounterPartySummary = new GSTR_Summary();
        b2B_CDNR_CounterPartySummary.setSec_nm("b2b");
        b2B_CDNR_CounterPartySummary.setChksum("AflJufPlFStqKBZ");
        b2B_CDNR_CounterPartySummary.setTtl_rec(10);
        b2B_CDNR_CounterPartySummary.setTtl_val(12345);
        b2B_CDNR_CounterPartySummary.setTtl_txpd_igst(124.99);
        b2B_CDNR_CounterPartySummary.setTtl_txpd_sgst(5589.87);
        b2B_CDNR_CounterPartySummary.setTtl_txpd_cgst(1234);
        b2B_CDNR_CounterPartySummary.setTtl_txpd_cess(4562);
        b2B_CDNR_CounterPartySummary.setTtl_itcavld_igst(124.99);
        b2B_CDNR_CounterPartySummary.setTtl_itcavld_sgst(5589.87);
        b2B_CDNR_CounterPartySummary.setTtl_itcavld_cgst(1234);
        b2B_CDNR_CounterPartySummary.setTtl_itcavld_cess(4562);
        
        List<GSTR_Summary> cpty_sum = new ArrayList<GSTR_Summary> ();
        GSTR_Summary CounterPartySummary = new GSTR_Summary();
        CounterPartySummary.setCtin("20GRRHF2562D3A3");
        CounterPartySummary.setChksum("AflJufPlFStqKBZ");
        CounterPartySummary.setTtl_rec(10);
        CounterPartySummary.setTtl_val(12345);
        CounterPartySummary.setTtl_txpd_igst(124.99);
        CounterPartySummary.setTtl_txpd_sgst(5589.87);
        CounterPartySummary.setTtl_txpd_cgst(1234);
        CounterPartySummary.setTtl_txpd_cess(4562);
        CounterPartySummary.setTtl_itcavld_igst(124.99);
        CounterPartySummary.setTtl_itcavld_sgst(5589.87);
        CounterPartySummary.setTtl_itcavld_cgst(1234);
        CounterPartySummary.setTtl_itcavld_cess(4562);
        
        cpty_sum.add(CounterPartySummary);
        
        b2B_CDNR_CounterPartySummary.setCpty_sum(cpty_sum);
        
        sec_sum.add(b2B_CDNR_CounterPartySummary);
        
        response.put("section_summary", sec_sum);
        System.out.println("section_summary : "+response);
        return response;
    }
    
    public JSONObject saveGSTR2data(JSONObject request) throws JSONException{
        JSONObject response = new JSONObject();
        
        request.put("gstin", "27AHQPA7588L1ZJ");
        request.put("fp", "122016");
        request.put("gt", 3782969.01);
        request.put("cur_gt", 3782969.01);
        
        String gstin = "29HJKPS9689A8Z4";
        String fp = "072016";
        
        JSONObject b2b = getB2BInvoices(gstin, fp, 'Y', null, null);
        request.put("b2b", b2b.getJSONArray("b2b"));

        JSONObject b2bur = getB2BUnregisteredInvoice(gstin, fp);
        request.put("b2bur", b2bur.getJSONArray("b2bur"));

        JSONObject cdn = getCDNInvoices(gstin, fp , null, null);
        request.put("cdn", cdn.getJSONArray("cdn"));
        
        JSONObject hsnsum = getHSNSummaryofInwardsupplies(gstin, fp);
        request.put("hsnsum", hsnsum.getJSONObject("hsnsum"));

        JSONObject imp_g = getImportofGoodsInvoices(gstin, fp);
        request.put("imp_g", imp_g.getJSONObject("imp_g"));

        JSONObject imp_s = getImportofServicesBills(gstin, fp);
        request.put("imp_s", imp_s.getJSONObject("imp_s"));
        
        JSONObject nil_supplies = getNilRatedSupplies(gstin, fp);
        request.put("nil_supplies", nil_supplies.getJSONObject("nil_supplies"));
        
        JSONObject txi = getTaxliabilityunderReverseChargeSummary(gstin, fp);
        request.put("txi", txi.getJSONArray("txi"));

        JSONObject txpd = getTaxPaidUnderReverseCharge(gstin, fp);
        request.put("txpd", txpd.getJSONArray("txpd"));

        JSONObject itc_rvsl = getITCReversalDetails(gstin, fp);
        request.put("itc_rvsl", itc_rvsl.getJSONArray("itc_rvsl"));

        JSONObject cdnur = getCDNUnregisteredData(gstin, fp, "Y", null, null);
        request.put("cdnur", cdnur.getJSONArray("cdnur"));
        
        System.out.println("request : "+request);
        response.put("ref_id", "LAPN24235325555");
        return response;
    }  
    
    public JSONObject fileGSTR2data(JSONObject request) throws JSONException{
        JSONObject response = new JSONObject();
        request = getGSTR2Summary("37ABCDE9552F3Z4", "072016");
        response.put("ack_num", "ASDFSDF1241343");
        return response;
    }    
    
    public JSONObject submitGSTR2data(String gstin, String ret_period) throws JSONException{
        JSONObject request = new JSONObject();
        JSONObject response = new JSONObject();
        request.put("gstin", gstin);
        request.put("ret_period", ret_period);
        response.put("ref_id", "LAPN24235325555");
        return response;
    }        
}
