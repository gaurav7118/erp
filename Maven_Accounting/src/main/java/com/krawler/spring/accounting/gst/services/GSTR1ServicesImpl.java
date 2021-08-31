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
package com.krawler.spring.accounting.gst.services;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.spring.accounting.gst.auth.AESEncryption;
import com.krawler.spring.accounting.gst.auth.ApiCallGSTN;
import com.krawler.spring.accounting.gst.dto.AT;
import com.krawler.spring.accounting.gst.dto.B2B;
import com.krawler.spring.accounting.gst.dto.B2CL;
import com.krawler.spring.accounting.gst.dto.B2CS;
import com.krawler.spring.accounting.gst.dto.CDNR;
import com.krawler.spring.accounting.gst.dto.CDNUR;
import com.krawler.spring.accounting.gst.dto.DocDto;
import com.krawler.spring.accounting.gst.dto.DocIssued;
import com.krawler.spring.accounting.gst.dto.Docs;
import com.krawler.spring.accounting.gst.dto.EXP;
import com.krawler.spring.accounting.gst.dto.HSN;
import com.krawler.spring.accounting.gst.dto.InvoiceDto;
import com.krawler.spring.accounting.gst.dto.ItemDetail;
import com.krawler.spring.accounting.gst.dto.ItemDto;
import com.krawler.spring.accounting.gst.dto.ItemDto_Nil;
import com.krawler.spring.accounting.gst.dto.NilData;
import com.krawler.spring.accounting.gst.dto.TXP;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

/**
 *
 * @author krawler
 */
public class GSTR1ServicesImpl {

//    private static final String NSDLURL = "http://test.nsdlgsp.co.in/";
//    private static final String CONTENTTYPE = "application/json";
//    private static final String encrypted_appKey = "HmD/EB7zYQL+IvaqMjS051fTqlD0bcsWLTBNBqFiQ6EJfEAtdfLtAFUPcpydmeqtJU4tXEEh3UT98YqieNICjLpxuX1u0go9GeVDI7s1opxWjJ0+sgS0nxtvJqmHiyYDRsoaBgNv9G/QSMrA6mMoUr4ZbaodXZCJljI/gGExEgujP7EstbDi53RHKaeB3ldOnZeYMDOR2bzFuRmrw5a2USLvSALBvBdTYAwL/EXXBEca60B5iIjZEvaslI1mdAqcFNNpg7JOWzcosFHBElQettQsB4CYl5DDHbId+HvUlfWaCt6DSbgAGe7IC/JxNnmeUy/3+4hu8AskSLRoLyf9IQ==";
//    private static final String appKey = "NdYNBbUGT1Szb8xV8zHSR7k6HO9Pdsz3DZZ6FDWnD90=";
//    private static byte[] ek = new byte[64];
//    static {
//        try {
//            ek = AESEncryption.generatePaddedSek(sek, appKey);
//        } catch (Exception ex) {
//        }
//    }
    public static void main(String[] args) {
        GSTR1ServicesImpl GSTR1Services = new GSTR1ServicesImpl();
//        try {
//            GSTR1Services.getB2BInvoices(GSTRConstants.aspgstin, "072017", 'Y', GSTRConstants.aspgstin, null);
//            GSTR1Services.getB2BCLInvoices("29HJKPS9689A8Z4", "072016", 'Y', null, null);
//            GSTR1Services.getB2CSInvoices("29HJKPS9689A8Z4", "072016");
//            GSTR1Services.getCDNRInvoices("29HJKPS9689A8Z4", "072016" , null, null);
//            GSTR1Services.getNilRatedSupplies("29HJKPS9689A8Z4", "072016");
//            GSTR1Services.getEXP("29HJKPS9689A8Z4", "072016");
//            GSTR1Services.getAT("29HJKPS9689A8Z4", "072016");
//            GSTR1Services.getTXP("29HJKPS9689A8Z4", "072016");
//            GSTR1Services.getHSNSummarydetails("29HJKPS9689A8Z4", "072016");
//            GSTR1Services.getCDNURInvoices("29HJKPS9689A8Z4", "072016");
//            GSTR1Services.getDocIssued("29HJKPS9689A8Z4", "072016");
//            GSTR1Services.saveGSTR1data(new JSONObject());
        GSTR1Services.RETURNSTATUS();
//        GSTR1Services.RETSUMMARY();

//            GSTR1Services.getGSTR1Summary("29HJKPS9689A8Z4", "072016");
//            GSTR1Services.fileGSTR1data(new JSONObject());
//
//        } catch (JSONException ex) {
//            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public JSONObject getB2BInvoices(String gstin, String ret_period, char action_required, String ctin, String from_time) throws JSONException {
        JSONObject response = new JSONObject();
//        response.put(Constants.RES_success, false);
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
//        call for db records
        List<B2B> b2bList = new ArrayList<B2B>();

//        https://<domain-name>/
        String endpoint = GSTRConstants.baseURL + GSTRConstants.RETURNS;

        endpoint = endpoint + "?ret_period=" + ret_period + "&action_required=Y&gstin=" + gstin + "&action=B2B";

        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-Type", GSTRConstants.contentType);
        header.put("state-cd", GSTRConstants.stateCd);
        header.put("ip-usr", GSTRConstants.ip);
        header.put("auth-token", GSTRConstants.authToken); //GSTRConstants.authToken
        header.put("username", GSTRConstants.username);
        header.put("gstin", GSTRConstants.aspgstin);
        header.put("ret_period", ret_period);
        header.put("message-id", "12345");
        header.put("aspid", GSTRConstants.aspId);
        header.put("asp-secret", GSTRConstants.encryptAspSecret); //GSTRConstants.encryptAspSecret
        header.put("session-id", GSTRConstants.sessionId); //GSTRConstants.sessionId
        header.put("filler1", "filler1");
        header.put("filler1", "filler1");
        header.put("message-id", "zxzxzxzxzxzxzxzxdac");
        header.put("txn", "12345");

        ApiCallGSTN ApiCallGSTN = new ApiCallGSTN();

        String gData = null;
        String gRek = null;
        JSONObject gstrRespObj = null;
        try {
            byte[] ek = AESEncryption.generatePaddedSek(GSTRConstants.sek, GSTRConstants.appKey);
            gstrRespObj = ApiCallGSTN.restMethod(endpoint, "", header, GSTRConstants.GET);

            System.out.println("gstrRespObj : " + gstrRespObj.toString());

            String cd = gstrRespObj.getString("status_cd");
            String decGSTRData = null;
            if (cd.equals("1")) {
                gData = gstrRespObj.getString("data");
                gRek = gstrRespObj.getString("rek");
                byte[] decRek = AESEncryption.genDecryptedREK(gRek, ek);
                decGSTRData = AESEncryption.decryptGstrData(gData, decRek);
            }
            response = new JSONObject(decGSTRData);
        } catch (ServiceException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalBlockSizeException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BadPaddingException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("B2B Decrypted Response : " + response);
        return response;

    }

    public JSONObject getB2CLInvoices(String gstin, String ret_period, String state_cd) throws JSONException {
        JSONObject response = new JSONObject();
//        response.put(Constants.RES_success, false);
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
        B2CL b2cl = new B2CL();
        b2cl.setPos("04");
        List<InvoiceDto> inv = new ArrayList<InvoiceDto>();
        InvoiceDto invoice = new InvoiceDto();
        invoice.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        invoice.setInum("92661");
        invoice.setIdt("10-01-2016");
        invoice.setVal(784586.33);
        invoice.setEtin("27AHQPA8875L1ZU");

        List<ItemDto> itmeList = new ArrayList<ItemDto>();
        ItemDto itemDto = new ItemDto();
        itemDto.setNum(01);
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setRt(0);
        itemDetail.setTxval(0);
        itemDetail.setIamt(0);
        itemDto.setItm_det(itemDetail);
        itmeList.add(itemDto);
        invoice.setItms(itmeList);

        inv.add(invoice);
        b2cl.setInv(inv);

        JSONObject jsonObj = new JSONObject(b2cl);
//        System.out.println("jsonObj"+jsonObj);

        JSONArray b2clArray = new JSONArray();
        b2clArray.put(jsonObj);
        response.put("b2cl", b2clArray);
        System.out.println("b2cl :: " + response);
//        response.put(Constants.RES_success, true);

        return response;
    }

    public JSONObject getB2CSInvoices(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
        List<B2CS> listB2CS = new ArrayList<B2CS>();
        B2CS b2cs = new B2CS();
        b2cs.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        b2cs.setSply_ty("INTER");
        b2cs.setRt(5);
        b2cs.setTyp("E");
        b2cs.setEtin("20ABCDE7588L1ZJ");
        b2cs.setPos("05");
        b2cs.setTxval(110);
        b2cs.setIamt(10);
        b2cs.setCsamt(10);
        listB2CS.add(b2cs);

        b2cs = new B2CS();
        b2cs.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        b2cs.setSply_ty("INTER");
        b2cs.setRt(5);
        b2cs.setTyp("E");
        b2cs.setPos("05");
        b2cs.setTxval(440);
        b2cs.setIamt(40);
        b2cs.setCsamt(10);
        listB2CS.add(b2cs);

        b2cs = new B2CS();
        b2cs.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        b2cs.setSply_ty("INTRA");
        b2cs.setRt(0);
        b2cs.setTyp("E");
        b2cs.setEtin("20ABCDE7588L1ZJ");
        b2cs.setTxval(0);
        b2cs.setIamt(0);
        b2cs.setCamt(0);
        b2cs.setCsamt(0);
        listB2CS.add(b2cs);

        b2cs = new B2CS();
        b2cs.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        b2cs.setSply_ty("INTER");
        b2cs.setRt(5);
        b2cs.setTyp("0E");
        b2cs.setTxval(100);
        b2cs.setIamt(10);
        b2cs.setCamt(10);
        b2cs.setCsamt(10);
        listB2CS.add(b2cs);

        response.put("b2cs", listB2CS);
        System.out.println("b2cs :: " + response);
        return response;
    }

    public JSONObject getCDNRInvoices(String gstin, String ret_period, String action_required, String from_time) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<CDNR> listCDNR = new ArrayList<CDNR>();
        CDNR cdnr = new CDNR();
        cdnr.setCtin("01AAAAP1208Q1ZS");
        cdnr.setCfs("Y");

        List<InvoiceDto> listItemDto = new ArrayList<InvoiceDto>();

        InvoiceDto itemDetails = new InvoiceDto();
        itemDetails.setChksum("adsdsfsjskssssq");
        itemDetails.setCflag('N');
        itemDetails.setUpdby('S');
//        itemDetails.setNtty('C');
        itemDetails.setNt_num("533515");
        itemDetails.setNt_dt("23-09-2016");
        itemDetails.setRsn("Not Mentioned");
        itemDetails.setP_gst('N');
        itemDetails.setInum("915914");
        itemDetails.setIdt("23-09-2016");
        itemDetails.setVal(123123);

        List<ItemDto> itms = new ArrayList<ItemDto>();
        ItemDto itemDto = new ItemDto();
        itemDto.setNum(1);
        ItemDetail itm_det = new ItemDetail();
        itm_det.setRt(10);
        itm_det.setTxval(5225.28);
        itm_det.setIamt(845.22);
        itm_det.setCsamt(789.52);
        itemDto.setItm_det(itm_det);
        itms.add(itemDto);

        itemDetails.setItms(itms);
        listItemDto.add(itemDetails);
        cdnr.setNt(listItemDto);
        listCDNR.add(cdnr);

        response.put("cdnr", listCDNR);
        System.out.println("cdnr :: " + response);
        return response;
    }

    public JSONObject getNilRatedSupplies(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
        NilData nilData = new NilData();
        nilData.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");

        List<ItemDto_Nil> inv = new ArrayList<ItemDto_Nil>();
        ItemDto_Nil itemDto_Nil = new ItemDto_Nil();
        itemDto_Nil.setSply_ty("INTRB2B");
        itemDto_Nil.setExpt_amt(123.45);
        itemDto_Nil.setNil_amt(1470.85);
        itemDto_Nil.setNgsup_amt(1258.5);
        inv.add(itemDto_Nil);

        itemDto_Nil = new ItemDto_Nil();
        itemDto_Nil.setSply_ty("INTRB2C");
        itemDto_Nil.setExpt_amt(123.45);
        itemDto_Nil.setNil_amt(1470.85);
        itemDto_Nil.setNgsup_amt(1258.5);
        inv.add(itemDto_Nil);

        itemDto_Nil = new ItemDto_Nil();
        itemDto_Nil.setSply_ty("INTRAB2B");
        itemDto_Nil.setExpt_amt(123.45);
        itemDto_Nil.setNil_amt(1470.85);
        itemDto_Nil.setNgsup_amt(1258.5);
        inv.add(itemDto_Nil);

        itemDto_Nil = new ItemDto_Nil();
        itemDto_Nil.setSply_ty("INTRAB2C");
        itemDto_Nil.setExpt_amt(123.45);
        itemDto_Nil.setNil_amt(1470.85);
        itemDto_Nil.setNgsup_amt(1258.5);
        inv.add(itemDto_Nil);

        nilData.setInv(inv);

        JSONObject jsonObj = new JSONObject(nilData);
        response.put("nil", jsonObj);
        System.out.println("nil :: " + response);
        return response;
    }

    public JSONObject getEXP(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<EXP> listEXP = new ArrayList<EXP>();
        EXP exp = new EXP();
        exp.setEx_tp("WPAY");

        List<InvoiceDto> inv = new ArrayList<InvoiceDto>();
        InvoiceDto invoiceDto = new InvoiceDto();
        invoiceDto.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        invoiceDto.setInum("81542");
        invoiceDto.setIdt("12-02-2016");
        invoiceDto.setVal(995048.36);
        invoiceDto.setSbpcode("ASB991");
//        invoiceDto.setSbnum(7896542);
        invoiceDto.setSbdt("04-10-2016");

//        List<ItemDetail> itms = new ArrayList<ItemDetail>();
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setTxval(10000);
        itemDetail.setRt(5);
        itemDetail.setIamt(833.33);

        List<ItemDto> itms = new ArrayList<ItemDto>();
        ItemDto item = new ItemDto();
        item.setItm_det(itemDetail);
        itms.add(item);

        invoiceDto.setItms(itms);
        inv.add(invoiceDto);
        exp.setInv(inv);
        listEXP.add(exp);

        exp = new EXP();
        exp.setEx_tp("WOPAY");

        inv = new ArrayList<InvoiceDto>();
        invoiceDto = new InvoiceDto();
        invoiceDto.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        invoiceDto.setInum("81542");
        invoiceDto.setIdt("12-02-2016");
        invoiceDto.setVal(995048.36);
        invoiceDto.setSbpcode("ASB881");
//        invoiceDto.setSbnum(7896542);
        invoiceDto.setSbdt("04-10-2016");

        itms = new ArrayList<ItemDto>();
        itemDetail = new ItemDetail();
        itemDetail.setTxval(10000);
        itemDetail.setRt(5);
        itemDetail.setIamt(833.33);
        item = new ItemDto();
        item.setItm_det(itemDetail);
        itms.add(item);

        invoiceDto.setItms(itms);
        inv.add(invoiceDto);
        exp.setInv(inv);
        listEXP.add(exp);

        response.put("exp", listEXP);
        System.out.println("exp :: " + response);
        return response;
    }

    public JSONObject getAT(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<AT> listAT = new ArrayList<AT>();
        AT at = new AT();
        at.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        at.setPos("05");
        at.setSply_ty("INTER");

        List<ItemDetail> itms = new ArrayList<ItemDetail>();
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setRt(5);
        itemDetail.setAd_amt(100);
        itemDetail.setIamt(9400);
        itemDetail.setCsamt(500);
        itms.add(itemDetail);

        at.setItms(itms);

        listAT.add(at);

        response.put("at", listAT);
        System.out.println("at :: " + response);
        return response;
    }

    public JSONObject getTXP(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<TXP> txpd = new ArrayList<TXP>();
        TXP txp = new TXP();
        txp.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        txp.setPos("05");
        txp.setSply_ty("INTER");

        List<ItemDetail> itms = new ArrayList<ItemDetail>();
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setRt(5);
        itemDetail.setAd_amt(100);
        itemDetail.setIamt(9400);
        itemDetail.setCsamt(500);
        itms.add(itemDetail);

        txp.setItms(itms);

        txpd.add(txp);

        response.put("txpd", txpd);
        System.out.println("at :: " + response);
        return response;
    }

    public JSONObject getHSNSummarydetails(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        List<HSN> listhsn = new ArrayList<HSN>();
        HSN hsn = new HSN();
        hsn.setChksum(gstin);

        List<ItemDetail> data = new ArrayList<ItemDetail>();
        ItemDetail itemDto_Hsn = new ItemDetail();
        itemDto_Hsn.setNum(1);
        itemDto_Hsn.setHsn_sc("1009");
        itemDto_Hsn.setDesc("Goods Description");
        itemDto_Hsn.setUqc("kg");
        itemDto_Hsn.setQty(2.05);
        itemDto_Hsn.setVal(995048.36);
        itemDto_Hsn.setTxval(10.23);
        itemDto_Hsn.setIamt(14.52);
        itemDto_Hsn.setCamt(78.52);
        itemDto_Hsn.setSamt(12.9);
        itemDto_Hsn.setCsamt(500);
        data.add(itemDto_Hsn);

        hsn.setData(data);

        JSONObject jobj = new JSONObject(hsn);
        response.put("hsn", jobj);
        System.out.println("hsn :: " + response);
        return response;
    }

    public JSONObject getCDNURInvoices(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
        List<CDNUR> listCDNUR = new ArrayList<CDNUR>();
        CDNUR cdnur = new CDNUR();
        cdnur.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        cdnur.setTyp("B2CL");
//        cdnur.setNtty('C');
        cdnur.setNt_num("533515");
        cdnur.setNt_dt("23-09-2016");
        cdnur.setRsn("Not Mentioned");
        cdnur.setP_gst('N');
        cdnur.setInum("915914");
        cdnur.setVal(123123);
        cdnur.setIdt("23-09-2016");

        List<ItemDto> itms = new ArrayList<ItemDto>();
        ItemDto itemDto = new ItemDto();
        itemDto.setNum(1);
        ItemDetail itm_det = new ItemDetail();
        itm_det.setRt(10);
        itm_det.setTxval(5225.28);
        itm_det.setIamt(845.22);
        itm_det.setCsamt(789.52);
        itemDto.setItm_det(itm_det);
        itms.add(itemDto);

        cdnur.setItms(itms);

        listCDNUR.add(cdnur);

        response.put("cdnur", listCDNUR);
        System.out.println("cdnur :: " + response);
        return response;
    }

    public JSONObject getDocIssued(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }

        DocIssued docIssued = new DocIssued();
        docIssued.setChksum("ASDFGJKLPTBBJKBJKBBJKBB");
        List<DocDto> doc_det = new ArrayList<DocDto>();
        DocDto docDto = new DocDto();
        docDto.setDoc_num(1);
        List<Docs> docs = new ArrayList<Docs>();
        Docs doc = new Docs();
        doc.setNum(1);
        doc.setFrom("20");
        doc.setTo("29");
        doc.setTotnum(20);
        doc.setCancel(3);
        doc.setNet_issue(17);
        docs.add(doc);
        docDto.setDocs(docs);
        doc_det.add(docDto);

        docIssued.setDoc_det(doc_det);

        JSONObject jobj = new JSONObject(docIssued);
        response.put("doc_issue", jobj);
        System.out.println("hsn :: " + response);
        return response;
    }

    public JSONObject saveGSTR1data(JSONObject request) throws JSONException {

        JSONObject response = new JSONObject();
        request.put("message-id", "message");
        request.put("ip-usr", "192.168.0.35");
        request.put("gstin", "27AHQPA7588L1ZJ");
//        request.put("fp", "122016");
//        request.put("gt", 3782969.01);
//        request.put("cur_gt", 3782969.01);
        String gData = null;
        String gRek = null;
        try {
            byte[] ek = AESEncryption.generatePaddedSek(GSTRConstants.sek, GSTRConstants.appKey);
            String B2B = "{ \"gstin\": " + GSTRConstants.aspgstinmodifed + ", \"fp\": \"072017\", \"gt\": 3782969.01, \"cur_gt\": 3782969.01, \"b2b\": [ { \"ctin\": \"27GSPMH1271G1ZP\", \"inv\": [ { \"inum\": \"9005\", \"idt\": \"01-07-2017\", \"val\": 729248.16, \"pos\": \"27\", \"rchrg\": \"N\", \"inv_typ\": \"R\", \"itms\": [ { \"num\": 1, \"itm_det\": { \"rt\": 5, \"txval\": 10000, \"camt\": 500, \"samt\": 900 } } ] }  ] } ] }";
            String data = B2B;

            byte[] base64Data = AESEncryption.getJsonBase64Payload(data);
            String hmac = AESEncryption.BCHmac(base64Data, ek);
            String action = "RETSAVE";
            String encData = AESEncryption.encryptEK(base64Data, ek);

            JSONObject obj = new JSONObject();

            obj.put("data", encData);
            System.out.println("Enc DAta " + encData);
            obj.put("hmac", hmac);
            System.out.println("HMAC " + hmac);
            obj.put("action", action);

            String endpoint = GSTRConstants.baseURL + GSTRConstants.GSTR1URL;

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("ret_period", "072017");
            headers.put("state-cd", ConfigReader.getinstance().get("state-cd"));
            headers.put("gstin", GSTRConstants.aspgstin);
            headers.put("message-id", request.optString("message-id"));
            headers.put("ip-usr", request.getString("ip-usr"));
            headers.put("aspid", ConfigReader.getinstance().get("aspid")); //"27AACCK5779R034790"
            headers.put("asp-secret", GSTRConstants.encryptAspSecret);
            headers.put("session-id", GSTRConstants.sessionId);
            headers.put("auth-token", GSTRConstants.authToken);
            headers.put("username", ConfigReader.getinstance().get("username"));//"nsdl.mh.2"
            headers.put("filler1", "filler1");
            headers.put("filler2", "filler2");
            headers.put("txn", "1234");
            headers.put("Content-Type", GSTRConstants.contentType);

            ApiCallGSTN ApiCallGSTN = new ApiCallGSTN();

            try {
                JSONObject gstrRespObj = ApiCallGSTN.restMethod(endpoint, obj.toString(), headers, GSTRConstants.POST);

                String cd = gstrRespObj.getString("status_cd");
                if (cd.equals("1")) {
                    gData = gstrRespObj.getString("data");
                    gRek = gstrRespObj.getString("rek");
                    byte[] decRek = AESEncryption.genDecryptedREK(gRek, ek);
                    String decGSTRData = AESEncryption.decryptGstrData(gData, decRek);
                    System.out.println("GSTR Decrypted Response : " + decGSTRData);
                    JSONObject gObj = new JSONObject(decGSTRData);
                    String refNumber = gObj.getString("reference_id");
                    System.out.println("refNumber :: " + refNumber);
                }

                System.out.println("gstrRespObj : " + gstrRespObj.toString());
            } catch (ServiceException ex) {
                Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalBlockSizeException ex) {
                Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadPaddingException ex) {
                Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

        } catch (Exception ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    public JSONObject getGSTR1Summary(String gstin, String ret_period) throws JSONException {
        JSONObject response = new JSONObject();
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
        String gData = null;
        String gRek = null;

        String endpoint = GSTRConstants.baseURL + GSTRConstants.RETURNS;

        endpoint = endpoint + "?ret_period=" + "072017" + "&gstin=" + GSTRConstants.gstin + "&action=RETSUM";

        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-Type", GSTRConstants.contentType);
        header.put("state-cd", GSTRConstants.stateCd);
        header.put("ip-usr", GSTRConstants.ip);
        header.put("auth-token", GSTRConstants.authToken); //GSTRConstants.authToken
        header.put("username", GSTRConstants.username);
        header.put("gstin", GSTRConstants.aspgstin);
        header.put("ret_period", "072017");
        header.put("message-id", "12345");
        header.put("aspid", GSTRConstants.aspId);
        header.put("asp-secret", GSTRConstants.encryptAspSecret); //GSTRConstants.aspSecret
        header.put("session-id", GSTRConstants.sessionId); //GSTRConstants.sessionId
        header.put("filler1", "filler1");
        header.put("filler1", "filler1");
        header.put("message-id", "zxzxzxzxzxzxzxzxdac");
        header.put("txn", "12345");

        ApiCallGSTN ApiCallGSTN = new ApiCallGSTN();

        try {
            JSONObject gstrRespObj = ApiCallGSTN.restMethod(endpoint, "", header, GSTRConstants.GET);
            System.out.println("gstrRespObj : " + gstrRespObj.toString());
            String cd = gstrRespObj.getString("status_cd");
            byte[] ek = AESEncryption.generatePaddedSek(GSTRConstants.sek, GSTRConstants.appKey);
            if (cd.equals("1")) {
                gData = gstrRespObj.getString("data");
                gRek = gstrRespObj.getString("rek");
                byte[] decRek = AESEncryption.genDecryptedREK(gRek, ek);
                String decGSTRData = AESEncryption.decryptGstrData(gData, decRek);

                System.out.println("RETURNSTATUS Decrypted Response : " + decGSTRData);
                response = new JSONObject(decGSTRData);
            }

        } catch (JSONException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("GSTR1Summary :: " + response);
        return response;
    }

    public JSONObject fileGSTR1data(JSONObject request) throws JSONException {
        JSONObject response = new JSONObject();
        request = getGSTR1Summary("37ABCDE9552F3Z4", "072016");

        response.put("ack_num", "ASDFSDF1241343");
        return response;
    }

    public JSONObject submitGSTR1data(String gstin, String ret_period) throws JSONException {
        JSONObject request = new JSONObject();
        JSONObject response = new JSONObject();
        request.put("gstin", gstin);
        request.put("ret_period", ret_period);

        response.put("ref_id", "LAPN24235325555");
        return response;
    }

    public static void RETURNSTATUS() {
        String gData = null;
        String gRek = null;
        String decGSTRData = null;

        String endpoint = GSTRConstants.baseURL + GSTRConstants.RETURNS;

        endpoint = endpoint + "?ret_period=" + "072017" + "&ref_id=" + "6ff3505a-adf3-4552-b0bb-bec39d3c7950" + "&gstin=" + GSTRConstants.aspgstinmodifed + "&action=RETSTATUS"; // 6PM 13sep
//        endpoint = endpoint + "?ret_period=" + "072017" + "&ref_id=" + "b80c27cd-80fe-4634-a5e0-bb41d8cd608b" + "&gstin=" + GSTRConstants.aspgstin + "&action=RETSTATUS";
//        endpoint = endpoint + "?ret_period=" + "072017" + "&ref_id=" + "14d6d27f-d88e-46bf-a373-b6599a8d826e" + "&gstin=" + GSTRConstants.aspgstin + "&action=RETSTATUS";

        Map<String, String> header = new HashMap<String, String>();
        header.put("Content-Type", GSTRConstants.contentType);
        header.put("state-cd", GSTRConstants.stateCd);
        header.put("ip-usr", GSTRConstants.ip);
        header.put("auth-token", GSTRConstants.authToken); //GSTRConstants.authToken
        header.put("username", GSTRConstants.username);
        header.put("gstin", GSTRConstants.aspgstin); //GSTRConstants.aspgstin
        header.put("ret_period", "072017");
        header.put("message-id", "12345");
        header.put("aspid", GSTRConstants.aspId);
        header.put("asp-secret", GSTRConstants.encryptAspSecret); //GSTRConstants.encryptAspSecret
        header.put("session-id", GSTRConstants.sessionId); //GSTRConstants.sessionId
        header.put("filler1", "filler1");
        header.put("filler1", "filler1");
        header.put("message-id", "zxzxzxzxzxzxzxzxdac");
        header.put("txn", "12345");

        ApiCallGSTN ApiCallGSTN = new ApiCallGSTN();

        try {
            JSONObject gstrRespObj = ApiCallGSTN.restMethod(endpoint, "", header, GSTRConstants.GET);
            System.out.println("gstrRespObj : " + gstrRespObj.toString());

            String cd = gstrRespObj.getString("status_cd");
            byte[] ek = AESEncryption.generatePaddedSek(GSTRConstants.sek, GSTRConstants.appKey);
            if (cd.equals("1")) {
                gData = gstrRespObj.getString("data");
                gRek = gstrRespObj.getString("rek");
                byte[] decRek = AESEncryption.genDecryptedREK(gRek, ek);
                decGSTRData = AESEncryption.decryptGstrData(gData, decRek);

                System.out.println("RETURNSTATUS Decrypted Response : " + decGSTRData);

            }

        } catch (JSONException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void RETSUMMARY() {
        System.out.println("@@inside RETSUMMARY...");
        String gData = null;
        String gRek = null;
        try {
            String endpoint = GSTRConstants.baseURL + GSTRConstants.GSTR1URL + "?ret_period=072017&gstin=" + GSTRConstants.aspgstin + "&action=RETSUM";

            Map<String, String> header = new HashMap<String, String>();
            header.put("Content-Type", GSTRConstants.contentType);
            header.put("state-cd", GSTRConstants.stateCd);
            header.put("ip-usr", GSTRConstants.ip);
            header.put("auth-token", GSTRConstants.authToken); //GSTRConstants.authToken
            header.put("username", GSTRConstants.username);
            header.put("gstin", GSTRConstants.aspgstin); //GSTRConstants.aspgstin
            header.put("ret_period", "072017");
            header.put("message-id", "12345");
            header.put("aspid", GSTRConstants.aspId);
            header.put("asp-secret", GSTRConstants.encryptAspSecret); //GSTRConstants.encryptAspSecret
            header.put("session-id", GSTRConstants.sessionId); //GSTRConstants.sessionId
            header.put("filler1", "filler1");
            header.put("filler1", "filler1");
            header.put("message-id", "zxzxzxzxzxzxzxzxdac");
            header.put("txn", "12345");
            ApiCallGSTN ApiCallGSTN = new ApiCallGSTN();
            JSONObject gstrRespObj = ApiCallGSTN.restMethod(endpoint, "", header, GSTRConstants.GET);
            System.out.println("gstrRespObj : " + gstrRespObj.toString());
            String decGSTRData = null;
            String cd = gstrRespObj.getString("status_cd");



            byte[] ek = AESEncryption.generatePaddedSek(GSTRConstants.sek, GSTRConstants.appKey);
            if (cd.equals("1")) {
                gData = gstrRespObj.getString("data");
                gRek = gstrRespObj.getString("rek");
                byte[] decRek = AESEncryption.genDecryptedREK(gRek, ek);
                decGSTRData = AESEncryption.decryptGstrData(gData, decRek);

                System.out.println("RETSUM Decrypted Response : " + decGSTRData);

            }

        } catch (JSONException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
                public static void RETSUMMARY_OLD() {
        System.out.println("@@inside RETSUMMARY...");
        String gData = null;
        String gRek = null;
        try {
                        URL url = new URL(GSTRConstants.baseURL + "/NGCSGSP/callApi/taxpayerapi/v0.3/returns/gstr1?ret_period=072017&gstin=" + GSTRConstants.aspgstin + "&action=RETSUM");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//			HttpURLConnection conn = GSTR1Fields.getProxyConnection(url);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", GSTRConstants.contentType);
            conn.setRequestProperty("state-cd", GSTRConstants.stateCd);
            conn.setRequestProperty("ip-usr", GSTRConstants.ip);
            conn.setRequestProperty("auth-token", GSTRConstants.authToken);
            conn.setRequestProperty("username", GSTRConstants.username);
            conn.setRequestProperty("gstin", GSTRConstants.aspgstin);
            conn.setRequestProperty("ret_period", "072017");
            conn.setRequestProperty("message-id", "12345");
            conn.setRequestProperty("aspid", GSTRConstants.aspId);
            conn.setRequestProperty("asp-secret", GSTRConstants.encryptAspSecret);
            conn.setRequestProperty("session-id", GSTRConstants.sessionId);
            conn.setRequestProperty("filler1", "filler1");
            conn.setRequestProperty("filler1", "filler1");
            conn.setRequestProperty("message-id", "zxzxzxzxzxzxzxzxdac");
            conn.setRequestProperty("txn", "1234");
            conn.setDoOutput(true);

            BufferedReader br = null;

            if (conn.getResponseCode() == 200) {
                br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            String output = null;
            String response = "";
            while ((output = br.readLine()) != null) {
                response = response + output;
            }

            System.out.println("RETSUM Encrypted Response : " + response);
            byte[] ek = AESEncryption.generatePaddedSek(GSTRConstants.sek, GSTRConstants.appKey);
            String decGSTRData = null;
            JSONObject gstrRespObj = new JSONObject(response);
            String cd = gstrRespObj.getString("status_cd");
            if (cd.equals("1")) {
                gData = gstrRespObj.getString("data");
                gRek = gstrRespObj.getString("rek");
                byte[] decRek = AESEncryption.genDecryptedREK(gRek, ek);
                decGSTRData = AESEncryption.decryptGstrData(gData, decRek);
                JSONObject gObj = new JSONObject(decGSTRData);
            }

            System.out.println("RETSUM Decrypted Response : " + decGSTRData);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
