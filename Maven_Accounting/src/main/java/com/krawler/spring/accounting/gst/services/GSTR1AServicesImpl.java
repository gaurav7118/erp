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

import com.krawler.common.util.StringUtil;
import com.krawler.spring.accounting.gst.dto.B2B;
import com.krawler.spring.accounting.gst.dto.InvoiceDto;
import com.krawler.spring.accounting.gst.dto.ItemDetail;
import com.krawler.spring.accounting.gst.dto.ItemDto;
import com.krawler.spring.accounting.gst.services.gstr2.GSTR2ServicesImpl;
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
public class GSTR1AServicesImpl{
    public static void main(String[] args) {
        GSTR1AServicesImpl services = new GSTR1AServicesImpl();
        try {
//            services.getB2BInvoices("29HJKPS9689A8Z4", "072016", 'Y', null, null);
//            services.getB2BAInvoices("29HJKPS9689A8Z4", "072016", 'Y', null, null);
//            services.getCDNInvoices("29HJKPS9689A8Z4", "072016" , null, null);
//            services.getCDNAInvoices("29HJKPS9689A8Z4", "072016" , null, null);
//            services.getNilRatedSupplies("29HJKPS9689A8Z4", "072016");
//            services.getEXP("29HJKPS9689A8Z4", "072016");
//            services.getAT("29HJKPS9689A8Z4", "072016");
//            services.getTXP("29HJKPS9689A8Z4", "072016");
//            services.getHSNSummarydetails("29HJKPS9689A8Z4", "072016");
//            services.getCDNURInvoices("29HJKPS9689A8Z4", "072016");
//            services.getDocIssued("29HJKPS9689A8Z4", "072016");
//            services.saveGSTR1data(new JSONObject());
            services.getGSTR1ASummary("29HJKPS9689A8Z4", "072016");
//            services.fileGSTR1data(new JSONObject());
            
            
        } catch (JSONException ex) {
            Logger.getLogger(GSTR1ServicesImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public JSONObject getB2BInvoices(String gstin, String ret_period, char action_required, String ctin, String from_time) throws JSONException {
        JSONObject response = new JSONObject();
//        response.put(Constants.RES_success, false);
        if (StringUtil.isNullOrEmpty(gstin) || StringUtil.isNullOrEmpty(ret_period)) {
            return response; // Mandatory Fields 1)gstin, 2)ret_period
        }
//        call for db records
        List<B2B> b2bList = new ArrayList<B2B>();
        
        
        B2B b2bObj = new B2B();
        b2bObj.setCtin("06ADECO9084R5Z4");
        b2bObj.setCfs('Y');
        List<InvoiceDto> inv = new ArrayList<InvoiceDto>();
        InvoiceDto invoice = new InvoiceDto();
        invoice.setFlag('A');
        invoice.setChksum("BBUIBUIUIJKKBJKGUYFTFGUY");
        invoice.setUpdby('R');
        invoice.setInum("S008400");
        invoice.setIdt("24-11-2016");
        invoice.setVal(729248.16);
        invoice.setPos("06");
        invoice.setRchrg('N');
//        invoice.setEtin("01AABCE5507R1Z4");
        invoice.setInv_typ("R");
        invoice.setCflag('N');
        invoice.setOpd("112016");
        
        List<ItemDto> itmeList = new ArrayList<ItemDto>();
        ItemDto itemDto = new ItemDto();
        itemDto.setNum(01);
        ItemDetail itemDetail = new ItemDetail();
        itemDetail.setRt(5);
        itemDetail.setTxval(10000);
        itemDetail.setIamt(0);
        itemDetail.setCamt(500);
        itemDetail.setSamt(900);
        itemDetail.setCsamt(500);
        itemDto.setItm_det(itemDetail);
        itmeList.add(itemDto);
        invoice.setItms(itmeList);
        
        inv.add(invoice);
        b2bObj.setInv(inv);
        
        b2bList.add(b2bObj);

        JSONObject jsonObj = new JSONObject( b2bObj );

        JSONArray b2bArray = new JSONArray();
        b2bArray.put(jsonObj);
        response.put("b2b", b2bArray);
        System.out.println("b2b :: "+response);
//        response.put(Constants.RES_success, true);
        return response;

    }
    
    public JSONObject getB2BAInvoices(String gstin, String ret_period, char action_required, String ctin, String from_time) throws JSONException {
        JSONObject response = new JSONObject();
        
        response = getB2BInvoices(gstin, ret_period, action_required, ctin, from_time);
        
        response.put("b2ba", response.get("b2b"));
        response.remove("b2b");
        
        System.out.println("b2ba :: "+response);
//        response.put(Constants.RES_success, true);
        return response;

    }
    
    public JSONObject getCDNInvoices(String gstin, String ret_period, String action_required, String from_time) throws JSONException {
        GSTR2ServicesImpl services = new GSTR2ServicesImpl();
        JSONObject response = services.getCDNInvoices(gstin, ret_period, action_required, from_time);

        System.out.println("cdn :: "+response);
        return response;        
    }
    
    public JSONObject getCDNAInvoices(String gstin, String ret_period, String action_required, String from_time) throws JSONException {
        GSTR2ServicesImpl services = new GSTR2ServicesImpl();
        JSONObject response = services.getCDNInvoices(gstin, ret_period, action_required, from_time);

        response.put("cdna", response.get("cdn"));
        response.remove("cdn");        
        
        System.out.println("cdna :: "+response);
        return response;        
    }    
    
    public JSONObject getGSTR1ASummary(String gstin, String ret_period) throws JSONException  {
        GSTR1ServicesImpl services = new GSTR1ServicesImpl();
        JSONObject response = services.getGSTR1Summary(gstin, ret_period);

//        response.put("cdna", response.get("cdn"));
//        response.remove("cdn");        
//        
        System.out.println("GSTR1ASummary :: "+response);
        return response;        
    }    
    
    public JSONObject submitGSTR1Adata(String gstin, String ret_period) throws JSONException{
        JSONObject request = new JSONObject();
        JSONObject response = new JSONObject();
        request.put("gstin", gstin);
        request.put("ret_period", ret_period);
        
        response.put("ref_id", "LAPN24235325555");
        return response;
    }      
}